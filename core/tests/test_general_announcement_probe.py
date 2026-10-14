import json,os
from io import StringIO
from unittest.mock import patch
from django.core.management import call_command
from django.core.management.base import CommandError
from django.test import TransactionTestCase,override_settings
from core.management.commands.probe_staging_general_announcement import RUN,TARGET_SITE,TARGET_SERVICE,MARKER,REFERENCE,fixture_specs
from leadership.models import Zone,LGA,Ward,RoleDefinition
from staff.models import User,Announcement
from core.models import Notification

@override_settings(SITE_URL=TARGET_SITE,ALLOWED_HOSTS=['kpn-staging.onrender.com','testserver'])
class GeneralAnnouncementProbeTests(TransactionTestCase):
    def setUp(self):
        env=patch.dict(os.environ,{'RENDER_SERVICE_ID':TARGET_SERVICE,'KPN_STAGING_GENERAL_PROBE':RUN});env.start();self.addCleanup(env.stop)
        z=Zone.objects.create(name='Probe zone');l=LGA.objects.create(name='Probe LGA',zone=z);w=Ward.objects.create(name='Probe ward',lga=l)
        for spec in fixture_specs(json.loads(REFERENCE.read_text())):
            rd=RoleDefinition.objects.create(title=spec['title'],tier=spec['tier'],seat_number=spec['number']) if spec['tier']!='GENERAL' else None
            User.objects.create_user(username=spec['username'],phone=spec['username'],email=spec['username']+'@kpn-staging.invalid',role=spec['tier'],role_definition=rd,status='VERIFIED',is_active=True,bio=MARKER,zone=z,lga=l,ward=w)
        User.objects.create_user(username='extra_staging_member',phone='extra',role='GENERAL',status='VERIFIED')
    def probe(self):
        out=StringIO();call_command('probe_staging_general_announcement',broadcast=True,stdout=out)
        return json.loads(out.getvalue().split('STAGING_GENERAL_RESULT ',1)[1])
    @patch('rest_api.push.send_to_devices',return_value={'sent':0,'failed':0,'configured':False})
    def test_general_only_reaches_every_eligible_member(self,provider):
        r=self.probe();self.assertEqual(r['expected_recipients'],43);self.assertEqual(r['inbox_notifications'],43)
        self.assertTrue(r['persisted_inbox_passed']);self.assertEqual(provider.call_count,43)
        self.assertFalse(r['state_broadcast_sent']);self.assertFalse(Announcement.objects.filter(scope='STATE').exists())
        self.assertEqual(r['fcm_provider_accepted'],0);self.assertTrue(r['native_phone_receipt'].startswith('NOT PROVEN'))
    @patch('rest_api.push.send_to_devices',return_value={'sent':0,'failed':0,'configured':False})
    def test_restart_does_not_send_again(self,provider):
        self.probe();calls=provider.call_count;r=self.probe()
        self.assertFalse(r['created_this_run']);self.assertEqual(Announcement.objects.count(),1);self.assertEqual(Notification.objects.count(),43);self.assertEqual(provider.call_count,calls)
    @override_settings(SITE_URL='https://www.kpn.com.ng')
    def test_production_refused(self):
        with self.assertRaises(CommandError):self.probe()
        self.assertEqual(Announcement.objects.count(),0)
    def test_missing_opt_in_refused(self):
        with patch.dict(os.environ,{'KPN_STAGING_GENERAL_PROBE':'disabled'}):
            with self.assertRaises(CommandError):self.probe()
        self.assertEqual(Announcement.objects.count(),0)
    def test_no_flag_refused(self):
        with self.assertRaises(CommandError):call_command('probe_staging_general_announcement')
        self.assertEqual(Announcement.objects.count(),0)
    def test_missing_fixture_refused(self):
        User.objects.filter(username='qa_state_02').update(status='SUSPENDED')
        with self.assertRaises(CommandError):self.probe()
        self.assertEqual(Announcement.objects.count(),0)
