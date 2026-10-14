# Staging-only synthetic QA fixtures

These are forced database fixtures, not successful real registrations or Telegram confirmations.

## Safety

- Exact target: https://kpn-staging.onrender.com, Render service srv-db7evlg473hc73d54b9g.
- Requires explicit KPN_STAGING_QA_BOOTSTRAP=kpn-staging-fixtures-v1, a complete private password map, and --apply.
- One user per current leadership role definition (41), plus one general member. Not one person in every geographic seat.
- All identities labelled STAGING_QA_FIXTURE_V1. Emails use kpn-staging.invalid; phone values are synthetic zeros; no photos or real personal data.
- VERIFIED application status and Telegram membership flags are simulated. Negative Telegram fixture IDs cannot represent real Telegram users. No Telegram or email provider is contacted during initialization.
- No superusers or Django staff/admin privileges are created.
- Unrelated username collisions, occupied seats and mismatched role definitions abort the atomic import. Existing fixture passwords and lifecycle states are not reset on retry.
- Source-backed locations: 3 zones, 21 LGAs, 225 wards. Current role titles taken from update_roles.py, not the legacy role names in seed_data.py. Reference geography was not independently validated.
- Representative QA jurisdiction: Kebbi Central / Birnin Kebbi / Nassarawa I.

After confirmation, set KPN_STAGING_QA_BOOTSTRAP=disabled. Credentials stay private in Render's KPN_STAGING_QA_PASSWORDS environment value and are never committed here. Disable all QA accounts before any public release; do not copy their records into production.

## Verification

Nine initializer tests passed locally using isolated SQLite, Python 3.13 and Django 5.2.17. This is separate from earlier PostgreSQL acceptance. Live creation counts and representative authentication must be verified after deployment. These fixtures intentionally bypass onboarding and real Telegram checks; those remain independent acceptance gates.

| Username | Tier | Role |
|---|---|---|
| qa_state_01 | STATE | President |
| qa_state_02 | STATE | Vice President |
| qa_state_03 | STATE | General Secretary |
| qa_state_04 | STATE | Assistant General Secretary |
| qa_state_05 | STATE | Director of Monitoring & Compliance |
| qa_state_06 | STATE | Director of Legal Affairs & Ethics |
| qa_state_07 | STATE | Director of Finance |
| qa_state_08 | STATE | Finance Operations Officer |
| qa_state_09 | STATE | Director of Community Engagement |
| qa_state_10 | STATE | Assistant Director of Community Engagement |
| qa_state_11 | STATE | Director of Programmes & Events |
| qa_state_12 | STATE | Assistant Director of Programmes & Events |
| qa_state_13 | STATE | Director of Audit & Accountability |
| qa_state_14 | STATE | Director of Member Support & Welfare |
| qa_state_15 | STATE | Director of Youth Development |
| qa_state_16 | STATE | Director of Women's Development |
| qa_state_17 | STATE | Assistant Director of Women's Development |
| qa_state_18 | STATE | Director of Media & Communications |
| qa_state_19 | STATE | Assistant Director of Media & Communications |
| qa_state_20 | STATE | Director of Public Relations & Partnerships |
| qa_zonal_01 | ZONAL | Senatorial Director |
| qa_zonal_02 | ZONAL | Senatorial Administrative Officer |
| qa_zonal_03 | ZONAL | Senatorial Communications Officer |
| qa_lga_01 | LGA | LGA Network Lead |
| qa_lga_02 | LGA | LGA Administrative Officer |
| qa_lga_03 | LGA | LGA Programmes Officer |
| qa_lga_04 | LGA | LGA Finance Officer |
| qa_lga_05 | LGA | LGA Communications Officer |
| qa_lga_06 | LGA | LGA Monitoring Officer |
| qa_lga_07 | LGA | LGA Women's Development Officer |
| qa_lga_08 | LGA | LGA Member Support Officer |
| qa_lga_09 | LGA | LGA Community Engagement Officer |
| qa_lga_10 | LGA | LGA Adviser |
| qa_ward_01 | WARD | Ward Community Lead |
| qa_ward_02 | WARD | Ward Administrative Officer |
| qa_ward_03 | WARD | Ward Programmes Officer |
| qa_ward_04 | WARD | Ward Finance Officer |
| qa_ward_05 | WARD | Ward Communications Officer |
| qa_ward_06 | WARD | Ward Monitoring Officer |
| qa_ward_07 | WARD | Ward Community Support Officer |
| qa_ward_08 | WARD | Ward Adviser |
| qa_general_01 | GENERAL | General Member |
