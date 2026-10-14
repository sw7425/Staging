"""Explicit, service-bound synthetic fixture import. Never real Telegram verification."""
import json
import os
from pathlib import Path

from django.conf import settings
from django.contrib.auth import get_user_model
from django.core.management.base import BaseCommand, CommandError
from django.db import connection, transaction
from django.utils import timezone
from leadership.models import Zone, LGA, Ward, RoleDefinition
from telegram_integration.models import TelegramMembership

TARGET_SITE = 'https://kpn-staging.onrender.com'
TARGET_SERVICE = 'srv-db7evlg473hc73d54b9g'
OPT_IN = 'kpn-staging-fixtures-v1'
MARKER = 'STAGING_QA_FIXTURE_V1 — synthetic person; Telegram verification is SIMULATED, not provider-confirmed.'
REFERENCE = Path(__file__).resolve().parents[2] / 'qa_data' / 'staging_reference_v1.json'


def fixture_specs(reference):
    rows = []
    for tier in ('STATE', 'ZONAL', 'LGA', 'WARD'):
        for number, title in enumerate(reference['roles'][tier], 1):
            rows.append({'username': f'qa_{tier.lower()}_{number:02d}', 'tier': tier, 'title': title, 'number': number})
    rows.append({'username': 'qa_general_01', 'tier': 'GENERAL', 'title': 'General Member', 'number': 1})
    return rows


class Command(BaseCommand):
    help = 'Create explicitly synthetic QA users ONLY in the designated staging service, without outbound messages.'

    def add_arguments(self, parser):
        parser.add_argument('--apply', action='store_true', help='Required explicit write opt-in.')

    def handle(self, *args, **options):
        if not options['apply']:
            raise CommandError('No write performed. --apply is required.')
        if (getattr(settings, 'SITE_URL', '').rstrip('/') != TARGET_SITE
                or os.environ.get('RENDER_SERVICE_ID') != TARGET_SERVICE
                or os.environ.get('KPN_STAGING_QA_BOOTSTRAP') != OPT_IN):
            raise CommandError('No write performed. Exact staging URL, service ID and explicit fixture opt-in are required.')
        reference = json.loads(REFERENCE.read_text())
        specs = fixture_specs(reference)
        try:
            passwords = json.loads(os.environ.get('KPN_STAGING_QA_PASSWORDS', '{}'))
        except (TypeError, ValueError):
            raise CommandError('Invalid private staging QA credential map.') from None
        if (not isinstance(passwords, dict) or set(passwords) != {s['username'] for s in specs}
                or any(not isinstance(p, str) or len(p) < 24 for p in passwords.values())
                or len(set(passwords.values())) != len(specs)):
            raise CommandError('A complete private map of unique passwords, at least 24 characters each, is required.')
        lock_id = 1263554098
        locked = False
        try:
            if connection.vendor == 'postgresql':
                with connection.cursor() as cursor:
                    cursor.execute('SELECT pg_advisory_lock(%s)', [lock_id])
                locked = True
            with transaction.atomic():
                result = self.seed(reference, specs, passwords)
        finally:
            if locked:
                with connection.cursor() as cursor:
                    cursor.execute('SELECT pg_advisory_unlock(%s)', [lock_id])
        self.stdout.write('STAGING_QA_RESULT ' + json.dumps(result, sort_keys=True))

    def seed(self, reference, specs, passwords):
        User = get_user_model()
        # Preflight collisions before creating any fixtures; all writes are atomic.
        for spec in specs:
            existing = User.objects.filter(username=spec['username']).first()
            if existing and (existing.bio != MARKER or existing.email != spec['username']+'@kpn-staging.invalid'):
                raise CommandError('Fixture username belongs to a non-fixture user; refusing all writes.')
        for zone_name, lgas in reference['locations'].items():
            zone, _ = Zone.objects.get_or_create(name=zone_name)
            for lga_name, wards in lgas.items():
                lga, _ = LGA.objects.get_or_create(name=lga_name, zone=zone)
                for ward_name in wards:
                    Ward.objects.get_or_create(name=ward_name, lga=lga)
        # One representative existing-reference scope; NOT one person in every geographic seat.
        zone = Zone.objects.get(name='Kebbi Central')
        lga = LGA.objects.get(name='Birnin Kebbi', zone=zone)
        ward = Ward.objects.get(name='Nassarawa I', lga=lga)
        now = timezone.now()
        created_count = 0
        for index, spec in enumerate(specs, 1):
            role = None
            if spec['tier'] != 'GENERAL':
                role, _ = RoleDefinition.objects.get_or_create(
                    tier=spec['tier'], title=spec['title'], defaults={'seat_number': spec['number']})
                if role.seat_number != spec['number']:
                    raise CommandError('Existing role definition differs from the reference; refusing to overwrite.')
                occupied = User.objects.filter(status='VERIFIED', role=spec['tier'], role_definition=role)
                if spec['tier'] == 'ZONAL': occupied = occupied.filter(zone=zone)
                elif spec['tier'] == 'LGA': occupied = occupied.filter(lga=lga)
                elif spec['tier'] == 'WARD': occupied = occupied.filter(ward=ward)
                if occupied.exclude(username=spec['username']).exists():
                    raise CommandError('A target seat is occupied by another user; refusing all writes.')
            user = User.objects.filter(username=spec['username']).first()
            # On retry, do not overwrite passwords or lifecycle changes to existing fixture users.
            if user:
                continue
            user = User(
                username=spec['username'], email=spec['username']+'@kpn-staging.invalid',
                first_name='QA', last_name=spec['title'], phone='00000000000', bio=MARKER,
                role=spec['tier'], role_definition=role, status='VERIFIED',
                zone=zone, lga=lga, ward=ward, is_active=True, is_staff=False, is_superuser=False,
                date_approved=now,
            )
            user.set_password(passwords[spec['username']])
            user.save()
            # Negative fixture IDs deliberately cannot represent real Telegram user identities.
            TelegramMembership.objects.create(
                user=user, telegram_user_id=-(9000000000000 + index),
                telegram_username=spec['username']+'_simulated', telegram_first_name='QA_SIMULATED',
                telegram_last_name=spec['title'], telegram_status='member', is_verified=True,
                first_verified_at=now, last_verified_at=now, last_checked_at=now,
            )
            created_count += 1
        qa = User.objects.filter(username__in=[s['username'] for s in specs], bio=MARKER)
        return {
            'fixture_version': 'v1', 'created_users': created_count, 'fixture_users_present': qa.count(),
            'expected_users': len(specs), 'role_definitions': RoleDefinition.objects.count(),
            'zones': Zone.objects.count(), 'lgas': LGA.objects.count(), 'wards': Ward.objects.count(),
            'simulated_telegram_memberships': TelegramMembership.objects.filter(user__in=qa, is_verified=True, telegram_user_id__lt=0).count(),
            'outbound_messages_sent': 0, 'telegram_provider_verification': 'NOT TESTED — flags simulated',
        }
