package com.kpn.mobile.data
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Call
import retrofit2.http.*

interface KpnApi {
 @GET("content/home/") suspend fun homeContent():HomeContent
 @GET("registration/vacant-roles/") suspend fun vacantRoles(@Query("zone") zone:Int?=null,@Query("lga") lga:Int?=null,@Query("ward") ward:Int?=null):VacancyResponse
 @Multipart @POST("auth/register/") suspend fun registerPhoto(@PartMap fields:Map<String,@JvmSuppressWildcards RequestBody>,@Part photo:MultipartBody.Part):UserDto
 @Multipart @PATCH("profile/") suspend fun updateProfilePhoto(@PartMap fields:Map<String,@JvmSuppressWildcards RequestBody>,@Part photo:MultipartBody.Part):ProfileDto
 @GET("notifications/summary/") suspend fun notificationSummary():NotificationSummary
 @POST("notifications/{id}/state/") suspend fun notificationState(@Path("id") id:Int,@Body state:Map<String,Boolean>):Map<String,@JvmSuppressWildcards Any?>
 @GET("leadership/people/{id}/") suspend fun publicPerson(@Path("id") id:Int):PublicPerson
 @GET("management/catalog/") suspend fun resourceCatalog():ResourceCatalog
 @GET("management/{kind}/") suspend fun resources(@Path("kind") kind:String,@Query("page") page:Int=1):Page<Map<String,@JvmSuppressWildcards Any?>>
 @Multipart @POST("management/{kind}/") suspend fun createResource(@Path("kind") kind:String,@PartMap fields:Map<String,@JvmSuppressWildcards RequestBody>,@Part files:List<MultipartBody.Part>):Map<String,@JvmSuppressWildcards Any?>
 @Multipart @PATCH("management/{kind}/{id}/") suspend fun editResource(@Path("kind") kind:String,@Path("id") id:Int,@PartMap fields:Map<String,@JvmSuppressWildcards RequestBody>,@Part files:List<MultipartBody.Part>):Map<String,@JvmSuppressWildcards Any?>
 @DELETE("management/{kind}/{id}/") suspend fun deleteResource(@Path("kind") kind:String,@Path("id") id:Int):Response<Unit>
 @GET("events/{id}/attendance/") suspend fun eventAttendance(@Path("id") id:Int):List<AttendanceRecord>
 @GET("programs/{kind}/{id}/participants/") suspend fun programParticipants(@Path("kind") kind:String,@Path("id") id:Int):ParticipantSet
 @PUT("programs/{kind}/{id}/participants/") suspend fun saveParticipants(@Path("kind") kind:String,@Path("id") id:Int,@Body body:ParticipantSet):ParticipantSet
 @PATCH("programs/{kind}/{id}/") suspend fun editProgram(@Path("kind") kind:String,@Path("id") id:Int,@Body body:ProgramItem):ProgramItem
 @GET("ward-meetings/{id}/attendance/") suspend fun wardAttendance(@Path("id") id:Int):List<AttendanceRecord>
 @PUT("ward-meetings/{id}/attendance/") suspend fun saveWardAttendance(@Path("id") id:Int,@Body body:AttendanceRequest):List<AttendanceRecord>
 @PATCH("ward-meetings/{id}/") suspend fun editWardMeeting(@Path("id") id:Int,@Body body:WardMeeting):WardMeeting

 @POST("auth/login/") suspend fun login(@Body body:LoginRequest):TokenPair
 @POST("auth/refresh/") suspend fun refresh(@Body body:RefreshRequest):AccessToken
 @POST("auth/refresh/") fun refreshCall(@Body body:RefreshRequest):Call<AccessToken>
 @GET("auth/me/") suspend fun me():UserDto
 @POST("auth/logout/") suspend fun logout(@Body body:LogoutRequest):Response<Unit>
 @POST("auth/password-reset/") suspend fun requestPasswordReset(@Body body:PasswordResetRequest):PasswordResetResponse
 @POST("auth/register/") suspend fun register(@Body body:RegistrationRequest):User
 @GET("newsroom/") suspend fun articles(@Query("page") page:Int=1,@Query("category") category:String?=null,@Query("q") q:String?=null):Page<Article>
 @GET("newsroom/{slug}/") suspend fun article(@Path("slug") slug:String):Article
 @GET("opportunities/") suspend fun opportunities(@Query("page") page:Int=1,@Query("category") category:String?=null,@Query("status") status:String?=null):Page<Opportunity>
 @GET("community/") suspend fun initiatives(@Query("page") page:Int=1):Page<Initiative>
 @GET("civic/") suspend fun civic(@Query("page") page:Int=1):Page<Article>
 @GET("advocacy/") suspend fun advocacy(@Query("page") page:Int=1):Page<Advocacy>
 @GET("patrons/") suspend fun patrons(@Query("page") page:Int=1):Page<Patron>
 @GET("leadership-directory/") suspend fun leaders(@Query("page") page:Int=1):Page<Leader>
 @GET("leadership/") suspend fun leadershipSeats(@Query("page") page:Int=1,@Query("tier") tier:String?=null,@Query("status") status:String="FILLED",@Query("zone") zone:Int?=null,@Query("lga") lga:Int?=null,@Query("ward") ward:Int?=null):Page<LeadershipSeat>
 @GET("impact/") suspend fun impact():Impact
 @GET("content/about/") suspend fun about():ContentPage
 @GET("content/contact/") suspend fun contact():ContentPage
 @POST("content/contact/") suspend fun sendContact(@Body body:ContactRequest):Map<String,String>
 @GET("content/support/") suspend fun support():ContentPage
 @GET("content/code-of-conduct/") suspend fun codeOfConduct():ContentPage
 @GET("content/faq/") suspend fun faq(@Query("page") page:Int=1):Page<FaqItem>
 @GET("gallery/") suspend fun gallery(@Query("page") page:Int=1):Page<MediaItem>
 @GET("locations/zones/") suspend fun zones(@Query("page") page:Int=1):Page<Zone>
 @GET("locations/lgas/") suspend fun lgas(@Query("zone") zone:Int?,@Query("page") page:Int=1):Page<Lga>
 @GET("locations/wards/") suspend fun wards(@Query("lga") lga:Int,@Query("page") page:Int=1):Page<Ward>
 @GET("roles/") suspend fun roles(@Query("tier") tier:String?=null,@Query("page") page:Int=1):Page<RoleDefinition>
 @GET("profile/") suspend fun profile():ProfileDto
 @PATCH("profile/") suspend fun updateProfile(@Body body:ProfileUpdate):ProfileDto
 @POST("profile/password/") suspend fun changePassword(@Body body:PasswordChange):Response<Unit>
 @GET("members/mobilization/") suspend fun mobilization(@QueryMap filters:Map<String,String>,@Query("page") page:Int=1):Page<Member>
 @GET("members/mobilization/filters/") suspend fun mobilizationFilters(@QueryMap filters:Map<String,String>):MobilizationFilters
 @Streaming @GET("members/mobilization/export/{format}/") suspend fun mobilizationExport(@Path("format") format:String,@QueryMap filters:Map<String,String>):okhttp3.ResponseBody
 @GET("members/{id}/administration/") suspend fun memberAdministration(@Path("id") id:Int,@Query("action") action:String,@QueryMap values:Map<String,String> = emptyMap()):MemberAdministrationForm
 @POST("members/{id}/administration/") suspend fun administerMember(@Path("id") id:Int,@Body values:Map<String,String>):Member
 @Streaming @GET("members/export/{format}/") suspend fun memberExport(@Path("format") format:String):okhttp3.ResponseBody
 @GET("members/") suspend fun members(@Query("page") page:Int=1,@Query("status") status:String?=null):Page<Member>
 @GET("members/pending/") suspend fun pendingMembers(@Query("page") page:Int=1):Page<Member>
 @GET("members/{id}/") suspend fun member(@Path("id") id:Int):Member
 @POST("members/{id}/decision/") suspend fun decideMember(@Path("id") id:Int,@Body body:MemberDecision):Member
 @GET("reports/") suspend fun reports(@Query("page") page:Int=1):Page<ReportItem>
 @POST("reports/") suspend fun createReport(@Body body:ReportCreate):ReportItem
 @GET("reports/{id}/") suspend fun report(@Path("id") id:Int):ReportItem
 @POST("reports/{id}/review/") suspend fun reviewReport(@Path("id") id:Int,@Body body:ReportReview):ReportItem
 @POST("reports/{id}/escalate/") suspend fun escalateReport(@Path("id") id:Int):ReportItem
 @GET("articles/mine/") suspend fun myArticles(@Query("page") page:Int=1):Page<Article>
 @GET("articles/mine/{id}/") suspend fun myArticle(@Path("id") id:Int):Article
 @Multipart @POST("articles/images/") suspend fun uploadArticleImage(@Part image:okhttp3.MultipartBody.Part):Map<String,String>
 @Multipart @POST("articles/mine/") suspend fun createArticleMultipart(@PartMap fields:Map<String,@JvmSuppressWildcards okhttp3.RequestBody>,@Part files:List<okhttp3.MultipartBody.Part>):Article
 @Multipart @PATCH("articles/mine/{id}/") suspend fun editArticleMultipart(@Path("id") id:Int,@PartMap fields:Map<String,@JvmSuppressWildcards okhttp3.RequestBody>,@Part files:List<okhttp3.MultipartBody.Part>):Article
 @POST("articles/mine/") suspend fun createArticle(@Body body:Map<String,@JvmSuppressWildcards Any?>):Article
 @PATCH("articles/mine/{id}/") suspend fun editArticle(@Path("id") id:Int,@Body body:Map<String,@JvmSuppressWildcards Any?>):Article
 @DELETE("articles/mine/{id}/") suspend fun deleteArticle(@Path("id") id:Int):Response<Unit>
 @POST("articles/{id}/submit/") suspend fun submitArticle(@Path("id") id:Int):Article
 @GET("articles/review/") suspend fun articleQueue(@Query("page") page:Int=1):Page<Article>
 @POST("articles/{id}/decision/") suspend fun decideArticle(@Path("id") id:Int,@Body body:Map<String,String>):Article
 @GET("media/mine/") suspend fun myMedia(@Query("page") page:Int=1):Page<MediaItem>
 @Multipart @POST("media/mine/") suspend fun uploadMedia(@Part file:MultipartBody.Part,@Part("title") title:RequestBody,@Part("description") description:RequestBody,@Part("media_type") type:RequestBody):MediaItem
 @GET("media/review/") suspend fun mediaQueue(@Query("page") page:Int=1):Page<MediaItem>
 @POST("media/{id}/decision/") suspend fun decideMedia(@Path("id") id:Int,@Body body:Map<String,String>):MediaItem
 @GET("events/") suspend fun events(@Query("page") page:Int=1):Page<EventItem>
 @POST("events/") suspend fun createEvent(@Body body:EventItem):EventItem
 @PATCH("events/{id}/") suspend fun editEvent(@Path("id") id:Int,@Body body:EventItem):EventItem
 @GET("events/{id}/") suspend fun event(@Path("id") id:Int):EventItem
 @PUT("events/{id}/attendance/") suspend fun attendance(@Path("id") id:Int,@Body body:AttendanceRequest):List<AttendanceRecord>
 @GET("events/{id}/minutes/") suspend fun minutes(@Path("id") id:Int):MeetingMinutes
 @PUT("events/{id}/minutes/") suspend fun saveMinutes(@Path("id") id:Int,@Body body:MeetingMinutes):MeetingMinutes
 @GET("ward-meetings/") suspend fun wardMeetings(@Query("page") page:Int=1):Page<WardMeeting>
 @POST("ward-meetings/") suspend fun createWardMeeting(@Body body:WardMeeting):WardMeeting
 @GET("programs/{kind}/") suspend fun programs(@Path("kind") kind:String,@Query("page") page:Int=1):Page<ProgramItem>
 @POST("programs/{kind}/") suspend fun createProgram(@Path("kind") kind:String,@Body body:ProgramItem):ProgramItem
 @GET("finance/summary/") suspend fun finance():FinanceSummary
 @GET("finance/donations/") suspend fun donations(@Query("page") page:Int=1):Page<Donation>
 @POST("finance/donations/") suspend fun createDonation(@Body body:Donation):Donation
 @GET("finance/expenses/") suspend fun expenses(@Query("page") page:Int=1):Page<Expense>
 @POST("finance/expenses/") suspend fun createExpense(@Body body:Expense):Expense
 @GET("discipline/") suspend fun discipline(@Query("page") page:Int=1):Page<Discipline>
 @POST("discipline/") suspend fun createDiscipline(@Body body:Discipline):Discipline
 @POST("discipline/{id}/decision/") suspend fun decideDiscipline(@Path("id") id:Int,@Body body:DisciplineDecision):Response<Discipline>
 @GET("notifications/") suspend fun notifications(@Query("page") page:Int=1):Page<NotificationItem>
 @POST("notifications/{id}/read/") suspend fun readNotification(@Path("id") id:Int):Response<Unit>
 @POST("notifications/read-all/") suspend fun readAll():Response<Unit>
 @GET("devices/") suspend fun devices():Page<DeviceRegistration>
 @POST("devices/") suspend fun registerDevice(@Body body:DeviceRegistration):DeviceRegistration
 @DELETE("devices/{id}/") suspend fun removeDevice(@Path("id") id:Int):Response<Unit>
 @POST("devices/deactivate/") suspend fun deactivateDevice(@Body body:DeviceDeactivateRequest):Response<Unit>
 @GET("dashboards/me/") suspend fun myDashboard():DashboardEnvelope
 @GET("telegram/status/") suspend fun telegramStatus():Map<String,@JvmSuppressWildcards Any?>
 @POST("telegram/link/") suspend fun telegramLink():TelegramLink
 @GET("dashboards/secretariat/") suspend fun secretariatDashboard():DashboardData
 @GET("dashboards/finance/") suspend fun financeDashboard():DashboardData
 @GET("dashboards/community/") suspend fun communityDashboard():DashboardData
 @GET("dashboards/zone/") suspend fun zoneDashboard():DashboardData
 @GET("dashboards/lga/") suspend fun lgaDashboard():DashboardData
 @GET("dashboards/ward/") suspend fun wardDashboard():DashboardData
 @GET("community-reports/") suspend fun communityReports(@Query("page") page:Int=1):Page<CommunityReport>
 @POST("community-reports/") suspend fun createCommunityReport(@Body body:CommunityReport):CommunityReport
 @Multipart @POST("community-reports/") suspend fun createCommunityReportMultipart(@PartMap fields:Map<String,@JvmSuppressWildcards okhttp3.RequestBody>,@Part evidence:List<okhttp3.MultipartBody.Part>):CommunityReport
 @POST("community-reports/{id}/review/") suspend fun reviewCommunityReport(@Path("id") id:Int,@Body body:CommunityReportDecision):CommunityReport
 @GET("announcements/") suspend fun announcements(@Query("mine") mine:Boolean=false, @Query("page") page:Int=1):Page<AnnouncementDto>
 @PUT("announcements/{id}/") suspend fun updateAnnouncement(@Path("id") id:Int,@Body body:AnnouncementDto):AnnouncementDto
 @DELETE("announcements/{id}/") suspend fun deleteAnnouncement(@Path("id") id:Int):Response<Unit>
 @POST("announcements/") suspend fun createAnnouncement(@Body body:AnnouncementDto):AnnouncementDto
 @GET("announcements/received/") suspend fun receivedAnnouncements(@Query("page") page:Int=1):Page<AnnouncementDto>
 @GET("announcements/{id}/") suspend fun announcement(@Path("id") id:Int):AnnouncementDto
 @GET("reporters/") suspend fun reporters(@Query("page") page:Int=1):Page<UserDto>
 @POST("reporters/{id}/{action}/") suspend fun reporterAction(@Path("id") id:Int, @Path("action") action:String):UserDto
 @POST("finance/donations/{id}/verify/") suspend fun verifyDonation(@Path("id") id:Int):Donation
 @POST("finance/donations/{id}/record/") suspend fun recordDonation(@Path("id") id:Int):Donation
 @GET("finance/reports/") suspend fun financialReports(@Query("page") page:Int=1):Page<FinancialReportDto>
 @POST("finance/reports/") suspend fun createFinancialReport(@Body body:FinancialReportDto):FinancialReportDto
 @GET("audit-reports/") suspend fun auditReports(@Query("page") page:Int=1):Page<AuditReportDto>
 @POST("audit-reports/") suspend fun createAuditReport(@Body body:AuditReportDto):AuditReportDto
 @POST("audit-reports/{id}/submit/") suspend fun submitAuditReport(@Path("id") id:Int):AuditReportDto
 @POST("discipline/{id}/legal-opinion/") suspend fun submitLegalOpinion(@Path("id") id:Int, @Body body:LegalOpinionRequest):Discipline
 @GET("outreach/") suspend fun outreachList(@Query("page") page:Int=1):Page<CommunityOutreach>
 @POST("outreach/") suspend fun createOutreach(@Body body:CommunityOutreach):CommunityOutreach
 @GET("outreach/{id}/") suspend fun outreach(@Path("id") id:Int):CommunityOutreach
 @POST("telegram/disconnect/") suspend fun telegramDisconnect():Response<Unit>
}
