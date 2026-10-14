package com.kpn.mobile.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true) data class Page<T>(val count:Int=0,val next:String?=null,val previous:String?=null,val results:List<T> = emptyList())
@JsonClass(generateAdapter = true) data class TokenPair(val access:String,val refresh:String)
@JsonClass(generateAdapter = true) data class RefreshRequest(val refresh:String)
@JsonClass(generateAdapter = true) data class AccessToken(val access:String,val refresh:String?=null)
@JsonClass(generateAdapter = true) data class LoginRequest(val username:String,val password:String)
@JsonClass(generateAdapter = true) data class TelegramMembership(val telegram_user_id:Long?=null,val telegram_username:String?=null,val telegram_status:String?=null,val is_verified:Boolean=false)
@JsonClass(generateAdapter = true) data class TelegramLink(val url:String,val web_url:String?=null,val expires_at:String?=null)
@JsonClass(generateAdapter = true) data class AccessState(val state:String="PENDING_APPROVAL",val telegram_required:Boolean=false,val telegram_verified:Boolean=false)
@JsonClass(generateAdapter = true) data class UserDto(
 val id:Int=0,val username:String="",val email:String="",val facebook_url:String="",val twitter_url:String="",val instagram_url:String="",val tiktok_url:String="",val first_name:String="",val last_name:String="",val phone:String="",val bio:String="",val photo:String?=null,val gender:String?=null,
 val role:String="GENERAL",val tier:String=role,val zone:Int?=null,val zone_name:String?=null,val lga:Int?=null,val lga_name:String?=null,val ward:Int?=null,val ward_name:String?=null,val role_definition:Int?=null,val role_title:String?=null,val seat_number:Int?=null,val status:String="PENDING",val reporter_level:String="MEMBER",val is_trusted_reporter:Boolean=false,val telegram_membership:TelegramMembership?=null,val access:AccessState=AccessState(),val dashboard:String="member",val capabilities:List<String> = emptyList(),val mobile_tools:List<String> = emptyList()
){val displayName:String get()=("$first_name $last_name").trim().ifBlank{username}
 val jurisdictionLabel:String get()=when(tier){"STATE"->"Kebbi State";"ZONAL"->zone_name ?: "Zone not assigned";"LGA"->lga_name ?: "LGA not assigned";"WARD"->ward_name ?: "Ward not assigned";else->"KPN membership"}
}
typealias User=UserDto
@JsonClass(generateAdapter = true) data class ProfileDto(val username:String="",val email:String="",val first_name:String="",val last_name:String="",val phone:String="",val bio:String="",val gender:String?=null,val photo:String?=null,val zone:Int?=null,val lga:Int?=null,val ward:Int?=null)
@JsonClass(generateAdapter = true) data class RoleDefinition(val id:Int,val title:String,val tier:String,val seat_number:Int?=null)
@JsonClass(generateAdapter = true) data class Zone(val id:Int,val name:String)
@JsonClass(generateAdapter = true) data class Lga(val id:Int,val name:String,val zone:Int)
@JsonClass(generateAdapter = true) data class Ward(val id:Int,val name:String,val lga:Int)
@JsonClass(generateAdapter = true) data class Article(
 val id:Int=0,val slug:String="",val title:String="",val subheadline:String="",val category:String="GENERAL",val location:String="",val lga:Int?=null,val lga_name:String?=null,val ward:Int?=null,val ward_name:String?=null,val verification_status:String="DEVELOPING",val reporter_credit:String="KPN_REPORTER",val author_name:String?=null,val image:String?=null,val featured_image:String?=null,val meta_description:String="",val content:String="",val content_json:Any?=null,val views:Int=0,val read_time:Int=1,val published_at:String?=null,val status:String?=null,val rejection_note:String?=null
)
@JsonClass(generateAdapter = true) data class Opportunity(val id:Int=0,val slug:String="",val title:String="",val category:String="",val organization:String="",val description:String="",val requirements:String="",val benefits:String="",val deadline:String?=null,val status:String="OPEN",val is_featured:Boolean=false,val external_apply_link:String="",val created_at:String?=null,val updated_at:String?=null)
@JsonClass(generateAdapter = true) data class Initiative(val id:Int=0,val title:String="",val description:String="",val status:String="",val category:String="",val location_text:String="",val people_reached:Int=0,val image:String?=null)
@JsonClass(generateAdapter = true) data class Advocacy(val id:Int=0,val title:String="",val issue:String="",val background:String="",val kpn_position:String="",val actions_taken:String="",val government_response:String="",val outcome:String="",val is_active:Boolean=true,val image:String?=null)
@JsonClass(generateAdapter = true) data class Patron(val id:Int=0,val patron_type:String="PATRON",val full_name:String="",val title:String="",val bio:String="",val photo:String?=null,val is_published:Boolean=true,val order:Int=0){val isGrandPatron get()=patron_type=="GRAND"}
@JsonClass(generateAdapter = true) data class Leader(val id:Int,val first_name:String="",val last_name:String="",val role:String="",val role_definition:Int?=null,val photo:String?=null){val name get()=("$first_name $last_name").trim()}
@JsonClass(generateAdapter = true) data class LeadershipLocation(val label:String="",val zone:Int?=null,val zone_name:String?=null,val lga:Int?=null,val lga_name:String?=null,val ward:Int?=null,val ward_name:String?=null)
@JsonClass(generateAdapter = true) data class LeadershipHolder(val id:Int,val name:String="",val photo:String?=null)
@JsonClass(generateAdapter = true) data class LeadershipSeat(val seat_key:String,val role_definition:Int,val role_title:String,val tier:String,val seat_number:Int?=null,val location:LeadershipLocation=LeadershipLocation(),val status:String="VACANT",val holder:LeadershipHolder?=null)
@JsonClass(generateAdapter = true) data class Impact(val verified_members:Int=0,val trusted_reporters:Int=0,val community_reports:Int=0,val opportunities:Int=0,val impact_communities_reached:Int=0,val active_lgas:Int=0,val active_wards:Int=0,val zones_total:Int=0,val lgas_total:Int=0,val wards_total:Int=0)
@JsonClass(generateAdapter = true) data class Member(val id:Int,val username:String="",val first_name:String="",val last_name:String="",val email:String="",val phone:String="",val photo:String?=null,val gender:String?=null,val role:String="",val role_title:String?=null,val zone:Int?=null,val zone_name:String?=null,val lga:Int?=null,val lga_name:String?=null,val ward:Int?=null,val ward_name:String?=null,val status:String="",val reporter_level:String="",val created_at:String?=null,val archived_at:String?=null){val name get()=("$first_name $last_name").trim().ifBlank{username}}
@JsonClass(generateAdapter = true) data class MemberDecision(val decision:String)
@JsonClass(generateAdapter = true) data class ReportItem(val id:Int=0,val title:String="",val report_type:String="",val content:String="",val period:String="",val submitted_by:Int?=null,val submitted_by_name:String?=null,val submitted_to:Int?=null,val submitted_to_name:String?=null,val status:String="DRAFT",val is_reviewed:Boolean=false,val is_escalated:Boolean=false,val review_notes:String="",val deadline:String?=null,val created_at:String?=null,val submitted_at:String?=null)
@JsonClass(generateAdapter = true) data class ReportCreate(val title:String,val content:String,val period:String="",val deadline:String?=null)
@JsonClass(generateAdapter = true) data class ReportReview(val action:String,val notes:String="")
@JsonClass(generateAdapter = true) data class NotificationItem(val id:Int,val event:String="INFO",val notif_type:String="INFO",val title:String,val message:String,val link:String="",val target_type:String?=null,val target_id:String?=null,val is_read:Boolean=false,val created_at:String?=null)
@JsonClass(generateAdapter = true) data class DeviceRegistration(val id:Int?=null,val token:String,val device_id:String="",val platform:String="ANDROID",val app_version:String="1.0.0",val is_active:Boolean=true)
@JsonClass(generateAdapter = true) data class EventItem(val id:Int=0,val title:String="",val description:String="",val location:String="",val start_date:String="",val end_date:String="",val created_by:Int?=null)
@JsonClass(generateAdapter = true) data class AttendanceRecord(val attendee:Int,val present:Boolean=true,val notes:String="")
@JsonClass(generateAdapter = true) data class AttendanceRequest(val records:List<AttendanceRecord>)
@JsonClass(generateAdapter = true) data class MeetingMinutes(val id:Int?=null,val event:Int?=null,val content:String="",val summary:String="",val attendees_present:List<Int> = emptyList(),val is_published:Boolean=false)
@JsonClass(generateAdapter = true) data class WardMeeting(val id:Int=0,val title:String="",val meeting_type:String="GENERAL",val date:String="",val time:String?=null,val location:String="",val agenda:String="",val minutes:String="",val ward:Int?=null)
@JsonClass(generateAdapter = true) data class ProgramItem(val id:Int=0,val title:String="",val description:String="",val program_type:String="OTHER",val status:String="PLANNED",val zone:Int?=null,val lga:Int?=null,val start_date:String?=null,val end_date:String?=null,val location:String?=null,val target_participants:Int?=null,val target_beneficiaries:Int?=null,val budget:String?=null,val notes:String="")
@JsonClass(generateAdapter = true) data class FinanceSummary(val scope:String="GENERAL",val total_income:String="0",val total_expenses:String="0",val balance:String="0",val pending_donations:Int=0)
@JsonClass(generateAdapter = true) data class Donation(val id:Int=0,val donor_name:String="",val amount:String="0",val reference:String="",val notes:String="",val status:String="UNVERIFIED",val created_at:String?=null)
@JsonClass(generateAdapter = true) data class Expense(val id:Int=0,val description:String="",val amount:String="0",val category:String="OTHER",val notes:String="",val date:String="")
@JsonClass(generateAdapter = true) data class Discipline(val id:Int=0,val user:Int,val action_type:String,val reason:String,val is_approved:Boolean=false,val legal_opinion:String="",val legal_approved:Boolean=false,val created_at:String?=null)
@JsonClass(generateAdapter = true) data class DisciplineDecision(val decision:String)
@JsonClass(generateAdapter = true) data class MediaItem(val id:Int=0,val title:String="",val description:String="",val media_type:String="PHOTO",val file:String?=null,val thumbnail:String?=null,val uploaded_by:Int?=null,val status:String="PENDING",val approved_by:Int?=null,val created_at:String?=null)
@JsonClass(generateAdapter = true) data class DashboardEnvelope(val title:String="My KPN",val role_title:String?=null,val dashboard_key:String?=null,val jurisdiction:String?=null,val blocks:List<DashboardBlock> = emptyList(),val work_sections:List<DashboardWorkSection> = emptyList())
@JsonClass(generateAdapter = true) data class DashboardBlock(val key:String="",val type:String="metrics",val title:String="",val available:Boolean?=null,val reason:String?=null,val items:List<DashboardItem> = emptyList())
@JsonClass(generateAdapter = true) data class DashboardItem(val key:String="",val label:String="",val value:Any?=null,val currency:String?=null)
typealias DashboardData=DashboardEnvelope
@JsonClass(generateAdapter = true) data class ApiErrorEnvelope(val error:ApiError)
@JsonClass(generateAdapter = true) data class ApiError(val code:String="api_error",val message:String="Request failed",val details:Any?=null)
@JsonClass(generateAdapter = true) data class RegistrationRequest(val username:String,val email:String,val password:String,val password_confirm:String,val first_name:String,val last_name:String,val phone:String,val gender:String,val zone:Int?=null,val lga:Int?=null,val ward:Int?=null,val role_definition:Int,val bio:String="")
typealias ProfileUpdate=Map<String,String>
@JsonClass(generateAdapter = true) data class PasswordChange(val old_password:String,val new_password:String)
@JsonClass(generateAdapter = true) data class PasswordResetRequest(val email:String)
@JsonClass(generateAdapter = true) data class PasswordResetResponse(val message:String="")
@JsonClass(generateAdapter = true) data class LogoutRequest(val refresh:String)
@JsonClass(generateAdapter = true) data class DeviceDeactivateRequest(val token:String)
@JsonClass(generateAdapter = true) data class ContentPage(val name:String?=null,val short_name:String?=null,val motto:String?=null,val title:String?=null,val description:String?=null,val vision:String?=null,val mission:String?=null,val message:String?=null,val address:String?=null,val phone:String?=null,val email:String?=null,val source:String?=null,val pillars:List<String> = emptyList(),val sections:List<ContentSection> = emptyList())
@JsonClass(generateAdapter = true) data class ContentSection(val title:String="",val body:String?=null,val items:List<String> = emptyList())
@JsonClass(generateAdapter = true) data class FaqItem(val id:Int,val question:String,val answer:String)
@JsonClass(generateAdapter = true) data class ContactRequest(val name:String,val email:String,val message:String)
@JsonClass(generateAdapter = true) data class CommunityReport(val id:Int=0,val reporter_name:String="",val reporter_phone:String="",val submitted_by:Int?=null,val ward:Int?=null,val lga:Int?=null,val zone:Int?=null,val location_details:String="",val incident_date:String="",val incident_time:String?=null,val category:String="COMMUNITY",val what_happened:String="",val who_was_involved:String="",val why_is_it_important:String="",val evidence_image:String?=null,val evidence_video:String?=null,val status:String="PENDING",val info_status:String="",val internal_notes:String="",val created_at:String?=null)
@JsonClass(generateAdapter = true) data class CommunityReportDecision(val action:String, val info_status:String="", val internal_notes:String="")
@JsonClass(generateAdapter = true) data class AnnouncementDto(val id:Int=0,val title:String="",val content:String="",val scope:String="GENERAL",val priority:String="NORMAL",val target_zone:Int?=null,val target_lga:Int?=null,val target_ward:Int?=null,val is_active:Boolean=true,val expires_at:String?=null,val created_by:Int?=null,val created_at:String?=null,val updated_at:String?=null)
@JsonClass(generateAdapter = true) data class FinancialReportDto(val id:Int=0,val title:String="",val report_period:String="",val total_income:String="0",val total_expenses:String="0",val report_file:String?=null,val summary:String="",val prepared_by:Int?=null,val created_at:String?=null)
@JsonClass(generateAdapter = true) data class AuditReportDto(val id:Int=0,val title:String="",val audit_period:String="",val findings:String="",val recommendations:String="",val compliance_status:String="PENDING",val status:String="DRAFT",val report_file:String?=null,val submitted_by:Int?=null,val submitted_to:Int?=null,val reviewed_by:Int?=null,val review_notes:String="",val created_at:String?=null,val submitted_at:String?=null,val reviewed_at:String?=null)
@JsonClass(generateAdapter = true) data class CommunityOutreach(val id:Int=0,val organization:String="",val contact_person:String="",val contact_phone:String="",val contact_email:String="",val engagement_type:String="MEETING",val status:String="PLANNED",val date:String="",val location:String="",val purpose:String="",val notes:String="",val follow_up_date:String?=null,val follow_up_notes:String="",val created_by:Int?=null,val created_at:String?=null,val updated_at:String?=null)
@JsonClass(generateAdapter = true) data class LegalOpinionRequest(val legal_opinion:String, val legal_approved:Boolean)

@JsonClass(generateAdapter = true) data class HomeContent(val organization:String="",val motto:String="",val introduction:String="",val pillars:List<HomePillar> = emptyList())
@JsonClass(generateAdapter = true) data class HomePillar(val title:String,val body:String,val route:String)
@JsonClass(generateAdapter = true) data class VacancyResponse(val vacant_roles:List<RoleDefinition> = emptyList())
@JsonClass(generateAdapter = true) data class NotificationSummary(val unread_count:Int=0)
@JsonClass(generateAdapter = true) data class PublicPerson(val id:Int,val name:String,val role_title:String?=null,val tier:String="",val jurisdiction:String="",val bio:String="",val photo:String?=null)
@JsonClass(generateAdapter = true) data class ResourceChoice(val value:String,val label:String)
@JsonClass(generateAdapter = true) data class ResourceField(val name:String,val label:String,val default_value:String?=null,val required:Boolean=false,val type:String="text",val choices:List<ResourceChoice> = emptyList())
@JsonClass(generateAdapter = true) data class ResourceSpec(val kind:String,val title:String,val fields:List<ResourceField> = emptyList(),val can_edit:Boolean=false,val can_delete:Boolean=false)
@JsonClass(generateAdapter = true) data class ResourceCatalog(val resources:List<ResourceSpec> = emptyList())
@JsonClass(generateAdapter = true) data class ParticipantSet(val members:List<Int> = emptyList())

@JsonClass(generateAdapter = true) data class DashboardWorkSection(val key:String,val title:String,val route:String,val empty_message:String="No work items available.",val items:List<DashboardWorkItem> = emptyList())
@JsonClass(generateAdapter = true) data class DashboardWorkItem(val id:Int,val title:String,val subtitle:String="",val status:String="",val route:String)

@JsonClass(generateAdapter = true) data class MemberAdministrationForm(val member:Member,val action:String,val title:String,val actions:List<ResourceChoice> = emptyList(),val fields:List<ResourceField> = emptyList())

@JsonClass(generateAdapter = true) data class MobilizationFilters(val fields:List<ResourceField> = emptyList())
