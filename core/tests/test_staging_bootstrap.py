import io
import json
from unittest.mock import patch
from django.contrib.auth import get_user_model
from django.core.management import call_command
from django.core.management.base import CommandError
from django.test import TestCase, override_settings
from leadership.models import Zone, LGA, Ward, RoleDefinition
from telegram_integration.models import TelegramMembership
from core.management.commands.bootstrap_staging_qa import REFERENCE, fixture_specs, TARGET_SERVICE, TARGET_SITE, OPT_IN, MARKER

@override_settings(SITE_URL=TARGET_SITE)
class StagingBootstrapTests(TestCase):
    def setUp(self):
        self.specs = fixture_specs(json.loads(REFERENCE.read_text()))
        self.passwords = {s['username']: 'private-test-password-'+str(i).zfill(16) for i,s in enumerate(self.specs)}
        self.env = patch.dict('os.environ', {'RENDER_SERVICE_ID':TARGET_SERVICE, 'KPN_STAGING_QA_BOOTSTRAP':OPT_IN, 'KPN_STAGING_QA_PASSWORDS':json.dumps(self.passwords)})
        self.env.start();self.addCleanup(self.env.stop)

    def run_seed(self):
        output=io.StringIO();call_command('bootstrap_staging_qa', apply=True, stdout=output)
        return json.loads(output.getvalue().split('STAGING_QA_RESULT ')[1])

    def test_creates_all_roles_and_simulated_memberships_without_outbound(self):
        with patch('django.core.mail.send_mail', side_effect=AssertionError('No outbound mail')), patch('requests.sessions.Session.request', side_effect=AssertionError('No provider network')):
            result=self.run_seed()
        self.assertEqual(result['created_users'],42)
        self.assertEqual([Zone.objects.count(),LGA.objects.count(),Ward.objects.count(),RoleDefinition.objects.count()],[3,21,225,41])
        User=get_user_model()
        for spec in self.specs:
            user=User.objects.get(username=spec['username'])
            self.assertTrue(user.check_password(self.passwords[user.username]))
            self.assertFalse(user.is_superuser);self.assertFalse(user.is_staff)
            self.assertEqual(user.status,'VERIFIED');self.assertEqual(user.role,spec['tier'])
            self.assertTrue(user.telegram_membership.is_verified)
            self.assertLess(user.telegram_membership.telegram_user_id,0)
            self.assertEqual(user.bio,MARKER)
            if user.role_definition:self.assertEqual(user.role_definition.title,spec['title'])

    def test_idempotent_and_does_not_reset_lifecycle_or_password(self):
        self.run_seed();User=get_user_model();u=User.objects.get(username='qa_general_01')
        u.status='SUSPENDED';u.set_password('changed-private-password');u.save()
        self.assertEqual(self.run_seed()['created_users'],0)
        u.refresh_from_db();self.assertEqual(u.status,'SUSPENDED');self.assertTrue(u.check_password('changed-private-password'))
        self.assertEqual(TelegramMembership.objects.count(),42)

    def test_production_site_refused(self):
        with override_settings(SITE_URL='https://www.kpn.com.ng'),self.assertRaises(CommandError):self.run_seed()
        self.assertEqual(get_user_model().objects.count(),0);self.assertEqual(Zone.objects.count(),0)

    def test_wrong_service_refused(self):
        with patch.dict('os.environ',{'RENDER_SERVICE_ID':'different-service'}),self.assertRaises(CommandError):self.run_seed()
        self.assertEqual(Zone.objects.count(),0)

    def test_missing_optin_refused(self):
        with patch.dict('os.environ',{'KPN_STAGING_QA_BOOTSTRAP':''}),self.assertRaises(CommandError):self.run_seed()
        self.assertEqual(Zone.objects.count(),0)

    def test_apply_required(self):
        with self.assertRaises(CommandError):call_command('bootstrap_staging_qa',stdout=io.StringIO())
        self.assertEqual(Zone.objects.count(),0)

    def test_incomplete_credentials_refused(self):
        with patch.dict('os.environ',{'KPN_STAGING_QA_PASSWORDS':'{}'}),self.assertRaises(CommandError):self.run_seed()
        self.assertEqual(Zone.objects.count(),0)

    def test_unrelated_username_collision_preserved(self):
        User=get_user_model();u=User.objects.create(username='qa_state_01',email='owner@example.invalid',bio='Not a fixture')
        with self.assertRaises(CommandError):self.run_seed()
        u.refresh_from_db();self.assertEqual(u.bio,'Not a fixture');self.assertEqual(User.objects.count(),1);self.assertEqual(Zone.objects.count(),0)

    def test_occupied_seat_refuses_all_new_writes(self):
        User=get_user_model();role=RoleDefinition.objects.create(tier='STATE',title='President',seat_number=1)
        User.objects.create(username='existing_president',role='STATE',role_definition=role,status='VERIFIED')
        with self.assertRaises(CommandError):self.run_seed()
        self.assertEqual(User.objects.count(),1);self.assertEqual(Zone.objects.count(),0);self.assertEqual(TelegramMembership.objects.count(),0)
