"""Explicitly approved, General-only staging broadcast; never log tokens or credentials."""
import importlib.util,json,os
from datetime import timedelta
from pathlib import Path
from unittest.mock import patch
from django.conf import settings
from django.core.management.base import BaseCommand,CommandError
from django.db import connection,transaction
from django.utils import timezone
from rest_framework.test import APIClient
from rest_framework_simplejwt.tokens import AccessToken
from core.models import Notification
from staff.models import Announcement,User
from rest_api.models import DeviceRegistration
from rest_api import push
from .bootstrap_staging_qa import TARGET_SITE,TARGET_SERVICE,MARKER,REFERENCE,fixture_specs

RUN='kpn-staging-general-receipt-v1'
TITLE='KPN staging test — General announcement'
BODY='KPN staging announcement test. Audience: all verified, active KPN members. This is a test only; no action is required.'

class Command(BaseCommand):
    help='Publish ONE General staging test, verify exact inbox recipients, observe real FCM results, and check fixture API receipt.'
    def add_arguments(self,parser):
        parser.add_argument('--broadcast',action='store_true')
        parser.add_argument('--verify-http',action='store_true')
    def handle(self,*args,**options):
        if not options['broadcast'] or settings.SITE_URL.rstrip('/')!=TARGET_SITE or os.environ.get('RENDER_SERVICE_ID')!=TARGET_SERVICE or os.environ.get('KPN_STAGING_GENERAL_PROBE')!=RUN:
            raise CommandError('No broadcast: exact staging service/site, opt-in, and --broadcast are required.')
        names=[s['username'] for s in fixture_specs(json.loads(REFERENCE.read_text()))]
        qa=list(User.objects.filter(username__in=names,bio=MARKER,is_active=True,status='VERIFIED').select_related('role_definition','zone','lga','ward'))
        if len(qa)!=42:raise CommandError('No broadcast: all 42 known synthetic role fixtures must remain verified and active.')
        author=next(u for u in qa if u.username=='qa_state_01')
        locked=False
        try:
            if connection.vendor=='postgresql':
                with connection.cursor() as c:c.execute('SELECT pg_advisory_lock(%s)',[1263554113])
                locked=True
            result=self.probe(author,qa,options['verify_http'])
        finally:
            if locked:
                with connection.cursor() as c:c.execute('SELECT pg_advisory_unlock(%s)',[1263554113])
        self.stdout.write('STAGING_GENERAL_RESULT '+json.dumps(result,sort_keys=True))
    def probe(self,author,qa,verify_http):
        outcomes=[];original=push.send_to_devices
        def observed(devices,**kwargs):
            result=original(devices,**kwargs)
            outcomes.append({'sent':result['sent'],'failed':result['failed'],'configured':result['configured'],'title_and_body_match':kwargs['title']==TITLE and kwargs['body']==BODY})
            return result
        created=False
        with patch('rest_api.push.send_to_devices',side_effect=observed):
            with transaction.atomic():
                existing=Announcement.objects.filter(created_by=author,title=TITLE,scope='GENERAL')
                if existing.count()>1:raise CommandError('No further broadcast: duplicate audit-labelled General announcements exist.')
                a=existing.first()
                if a and a.content!=BODY:raise CommandError('No further broadcast: audit title belongs to different content.')
                if not a:
                    client=APIClient();client.force_authenticate(author)
                    response=client.post('/api/v1/announcements/',{'title':TITLE,'content':BODY,'scope':'GENERAL','priority':'NORMAL','is_active':True,'expires_at':(timezone.now()+timedelta(days=1)).isoformat()},format='json',secure=True,HTTP_HOST='kpn-staging.onrender.com')
                    if response.status_code!=201:raise CommandError('General announcement API refused the probe; HTTP '+str(response.status_code))
                    a=Announcement.objects.get(pk=response.data['id']);created=True
        eligible=User.objects.filter(is_active=True,status='VERIFIED').select_related('role_definition','zone','lga','ward')
        expected=set(eligible.values_list('pk',flat=True));notices=Notification.objects.filter(event='ANNOUNCEMENT',target_type='announcement',target_id=a.pk)
        received=set(notices.values_list('user_id',flat=True));matches=notices.filter(title=TITLE,message=BODY).count()
        visible=sum(Announcement.for_user(u).filter(pk=a.pk).exists() for u in eligible)
        devices=DeviceRegistration.objects.filter(user_id__in=expected,is_active=True,platform='ANDROID')
        credential=getattr(settings,'FIREBASE_CREDENTIALS_FILE','')
        result={'run':RUN,'target':TARGET_SITE,'scope':'GENERAL','state_broadcast_sent':False,'id':a.pk,'title':TITLE,'created_this_run':created,'expected_recipients':len(expected),'inbox_notifications':notices.count(),'unique_inbox_recipients':len(received),'missing_recipients':len(expected-received),'unexpected_recipients':len(received-expected),'duplicates':notices.count()-len(received),'visible_to_expected_members':visible,'title_and_body_match':matches==len(expected),'already_marked_read':notices.filter(is_read=True).count(),'registered_android_devices':devices.count(),'recipient_users_with_android_devices':devices.values('user_id').distinct().count(),'fcm_dispatch_calls_observed':len(outcomes),'fcm_provider_accepted':sum(r['sent'] for r in outcomes),'fcm_provider_failed':sum(r['failed'] for r in outcomes),'fcm_payload_matches_announcement':all(r['title_and_body_match'] for r in outcomes),'firebase_credential_path_configured':bool(credential),'firebase_credential_file_present':bool(credential and Path(credential).is_file()),'firebase_sdk_installed':importlib.util.find_spec('firebase_admin') is not None,'expires_at':a.expires_at.isoformat() if a.expires_at else None,'persisted_inbox_passed':received==expected and notices.count()==len(expected) and matches==len(expected) and visible==len(expected),'native_phone_receipt':'NOT PROVEN — requires a staging-connected native app and user-visible receipt','http_checks':{'attempted':False},'private_credentials_logged':False}
        self.stdout.write('STAGING_GENERAL_PUBLISHED '+json.dumps({k:result[k] for k in ['run','scope','id','created_this_run','inbox_notifications','fcm_provider_accepted','fcm_provider_failed']}))
        if verify_http:result['http_checks']=self.verify_http(qa,a)
        return result
    def verify_http(self,qa,a):
        import requests
        session=requests.Session();session.headers['User-Agent']='KPN-Staging-General-Receipt-Probe/1.0';checks=passed=0;failures=[]
        for u in qa:
            session.headers['Authorization']='Bearer '+str(AccessToken.for_user(u))
            for path,kind in [('announcements/received/','announcement'),('notifications/','notification')]:
                checks+=1
                try:
                    rows=[];url=TARGET_SITE+'/api/v1/'+path;pages=0
                    while url and pages<30:
                        if not url.startswith(TARGET_SITE+'/api/v1/'):raise RuntimeError('Unexpected pagination origin')
                        response=session.get(url,timeout=15);response.raise_for_status();data=response.json();rows.extend(data['results']);url=data.get('next');pages+=1
                    if url:raise RuntimeError('Pagination incomplete')
                    found=[r for r in rows if r.get('id')==a.pk] if kind=='announcement' else [r for r in rows if r.get('event')=='ANNOUNCEMENT' and r.get('target_type')=='announcement' and str(r.get('target_id'))==str(a.pk)]
                    if len(found)==1 and found[0]['title']==TITLE and found[0].get('content' if kind=='announcement' else 'message')==BODY:passed+=1
                    else:failures.append({'user':u.username,'path':path,'reason':'Missing, duplicate or content mismatch'})
                except Exception as exc:failures.append({'user':u.username,'path':path,'reason':type(exc).__name__})
        session.close()
        return {'attempted':True,'fixture_accounts':len(qa),'checks':checks,'passed':passed,'failed':len(failures),'failures':failures}
