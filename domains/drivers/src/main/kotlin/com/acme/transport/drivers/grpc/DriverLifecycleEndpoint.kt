package com.acme.transport.drivers.grpc

import com.acme.authorization.v1.AccessPolicyValidationServiceGrpcKt
import com.acme.authorization.v1.LintAccessPoliciesRequestKt
import com.acme.authorization.v1.NormalizeAccessPolicyRequest
import com.acme.authorization.v1.accessPolicy
import com.acme.authorization.v1.lintAccessPoliciesRequest
import com.acme.authorization.v1.normalizeAccessPolicyRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.customerRef
import com.acme.drivers.v1.ActivateDriverRequest
import com.acme.drivers.v1.ActivateDriverResponse
import com.acme.drivers.v1.ActivateDriverResponseKt
import com.acme.drivers.v1.BrowseDriverLicensesRequest
import com.acme.drivers.v1.BrowseDriverLicensesRequestKt
import com.acme.drivers.v1.DriverLicenseCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseQueryServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseReportingServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseValidationServiceGrpcKt
import com.acme.drivers.v1.DriverLifecycleServiceGrpcKt
import com.acme.drivers.v1.DutyStatusValidationServiceGrpcKt
import com.acme.drivers.v1.ExpireDriverRequest
import com.acme.drivers.v1.ExpireDriverResponse
import com.acme.drivers.v1.ExpireDriverResponseKt
import com.acme.drivers.v1.ExplainDriverLicenseCalculationRequestKt
import com.acme.drivers.v1.GetDriverLifecycleRequest
import com.acme.drivers.v1.GetDriverLifecycleResponse
import com.acme.drivers.v1.NormalizeDriverLicenseRequestKt
import com.acme.drivers.v1.RenewDriverRequest
import com.acme.drivers.v1.RenewDriverResponse
import com.acme.drivers.v1.RenewDriverResponseKt
import com.acme.drivers.v1.ScheduleDriverExpiryRequest
import com.acme.drivers.v1.ScheduleDriverExpiryResponse
import com.acme.drivers.v1.ScheduleDriverExpiryResponseKt
import com.acme.drivers.v1.SuspendDriverRequest
import com.acme.drivers.v1.SuspendDriverResponse
import com.acme.drivers.v1.SuspendDriverResponseKt
import com.acme.drivers.v1.TerminateDriverRequest
import com.acme.drivers.v1.TerminateDriverResponse
import com.acme.drivers.v1.TerminateDriverResponseKt
import com.acme.drivers.v1.ValidateDutyStatusRequest
import com.acme.drivers.v1.ValidateDutyStatusRequestKt
import com.acme.drivers.v1.ValidateDutyStatusResponse
import com.acme.drivers.v1.activateDriverResponse
import com.acme.drivers.v1.browseDriverLicensesRequest
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverLicenseCardSettlement
import com.acme.drivers.v1.driverLicenseRef
import com.acme.drivers.v1.driverPreferences
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.dutyStatus
import com.acme.drivers.v1.expireDriverResponse
import com.acme.drivers.v1.explainDriverLicenseCalculationRequest
import com.acme.drivers.v1.getDriverLifecycleResponse
import com.acme.drivers.v1.normalizeDriverLicenseRequest
import com.acme.drivers.v1.previewDriverLicenseRequest
import com.acme.drivers.v1.renewDriverResponse
import com.acme.drivers.v1.scheduleDriverExpiryResponse
import com.acme.drivers.v1.streamDriverLicenseMetricsRequest
import com.acme.drivers.v1.summarizeDriverLicensesRequest
import com.acme.drivers.v1.suspendDriverResponse
import com.acme.drivers.v1.terminateDriverResponse
import com.acme.drivers.v1.validateDutyStatusRequest
import com.acme.fuel.v1.FuelTransactionExportServiceGrpcKt
import com.acme.fuel.v1.ListFuelTransactionExportsRequest
import com.acme.fuel.v1.ListFuelTransactionExportsRequestKt
import com.acme.fuel.v1.ListFuelTransactionExportsResponse
import com.acme.fuel.v1.listFuelTransactionExportsRequest
import com.acme.identity.v1.UserValidationServiceGrpcKt
import com.acme.identity.v1.copy as identityCopy
import com.acme.identity.v1.user
import com.acme.identity.v1.userRef
import com.acme.identity.v1.validateUserRequest
import com.acme.notifications.v1.LintNotificationsRequest
import com.acme.notifications.v1.NotificationValidationServiceGrpcKt
import com.acme.notifications.v1.lintNotificationsRequest
import com.acme.notifications.v1.notification
import com.acme.performance.v1.ReviewServiceGrpcKt
import com.acme.performance.v1.SearchReviewsRequest
import com.acme.performance.v1.SearchReviewsRequestKt
import com.acme.performance.v1.searchReviewsRequest
import com.acme.search.v1.BrowseSearchIndexesRequestKt
import com.acme.search.v1.SearchIndexQueryServiceGrpcKt
import com.acme.search.v1.browseSearchIndexesRequest
import com.acme.telematics.v1.GeoFenceImportServiceGrpcKt
import com.acme.telematics.v1.TripLogSyncServiceGrpcKt
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.pullTripLogsRequest
import com.acme.telematics.v1.retryGeoFenceImportRequest
import com.acme.telematics.v1.tripLog
import com.acme.transport.drivers.DriversRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.venues.v1.SummarizeVenuesRequest
import com.acme.venues.v1.SummarizeVenuesResponse
import com.acme.venues.v1.VenueQueryServiceGrpcKt
import com.acme.venues.v1.summarizeVenuesRequest
import com.google.protobuf.Any as ProtoAny
import com.google.protobuf.Timestamp
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Moves a driver between the states of its lifecycle. */
class DriverLifecycleEndpoint(
    private val store: DriversRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyValidationService: AccessPolicyValidationServiceGrpcKt.AccessPolicyValidationServiceCoroutineStub,
    private val driversDriverLicenseCalculationService: DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub,
    private val driversDriverLicenseQueryService: DriverLicenseQueryServiceGrpcKt.DriverLicenseQueryServiceCoroutineStub,
    private val driversDriverLicenseReportingService: DriverLicenseReportingServiceGrpcKt.DriverLicenseReportingServiceCoroutineStub,
    private val driversDriverLicenseValidationService: DriverLicenseValidationServiceGrpcKt.DriverLicenseValidationServiceCoroutineStub,
    private val driversDutyStatusValidationService: DutyStatusValidationServiceGrpcKt.DutyStatusValidationServiceCoroutineStub,
    private val fuelFuelTransactionExportService: FuelTransactionExportServiceGrpcKt.FuelTransactionExportServiceCoroutineStub,
    private val identityUserValidationService: UserValidationServiceGrpcKt.UserValidationServiceCoroutineStub,
    private val notificationsNotificationValidationService: NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub,
    private val performanceReviewService: ReviewServiceGrpcKt.ReviewServiceCoroutineStub,
    private val searchSearchIndexQueryService: SearchIndexQueryServiceGrpcKt.SearchIndexQueryServiceCoroutineStub,
    private val telematicsGeoFenceImportService: GeoFenceImportServiceGrpcKt.GeoFenceImportServiceCoroutineStub,
    private val telematicsTripLogSyncService: TripLogSyncServiceGrpcKt.TripLogSyncServiceCoroutineStub,
    private val venuesVenueQueryService: VenueQueryServiceGrpcKt.VenueQueryServiceCoroutineStub,
) : DriverLifecycleServiceGrpcKt.DriverLifecycleServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DriverLifecycleEndpoint::class.java)

    private val invokeScheduleDriverExpiry1: suspend (ListFuelTransactionExportsRequest) -> ListFuelTransactionExportsResponse = { payload ->
        fuelFuelTransactionExportService.listFuelTransactionExports(payload)
    }

    private val workerForSuspendDriver3 = ReplyMailbox<SummarizeVenuesRequest, SummarizeVenuesResponse>(workers) { payload ->
        withTimeout(5_000) { venuesVenueQueryService.summarizeVenues(payload) }
    }

    private val clientForTerminateDriver0 by lazy { driversDriverLicenseCalculationService }

    /** Activates a driver. */
    override suspend fun activateDriver(request: ActivateDriverRequest): ActivateDriverResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLifecycleService/ActivateDriver") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ActivateDriver", request)
            log.atInfo().addKeyValue("operation", "ActivateDriver").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = validateUserRequest {}.identityCopy {
                this.context = request.context
                this.user = user {
                    this.contact = contactInfo {
                        this.email = request.details.newOwner.email
                    }
                    this.relationshipManager = userRef {
                        this.displayName = request.details.newOwner.displayName
                        this.email = request.details.notifyContact.email
                    }
                }
            }
            val reply0 = identityUserValidationService.validateUser(outbound0)
            activateDriverResponse {
                this.driver = driver {
                    this.driverId = request.driverId
                    this.name = reply0.user.name
                    this.preferences = driverPreferences {
                        this.language = reply0.user.preferences.language
                    }
                }
                this.outcome = ActivateDriverResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Expires a driver. */
    override suspend fun expireDriver(request: ExpireDriverRequest): ExpireDriverResponse {
        return handleExpireDriver(request)
    }

    /** Returns a driver. */
    override suspend fun getDriverLifecycle(request: GetDriverLifecycleRequest): GetDriverLifecycleResponse = handleGetDriverLifecycle(request)

    /** Renews a driver. */
    override suspend fun renewDriver(request: RenewDriverRequest): RenewDriverResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLifecycleService/RenewDriver") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RenewDriver", request)
            log.atInfo().addKeyValue("operation", "RenewDriver").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = normalizeAccessPolicyRequest {
                this.accessPolicy = accessPolicy {
                    this.owner = userRef {
                        this.email = request.details.notifyContact.email
                        this.userId = request.details.newOwner.userId
                    }
                }
                this.context = request.context
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = authorizationAccessPolicyValidationService.normalizeAccessPolicy(envelope0.unpack(NormalizeAccessPolicyRequest::class.java))
            val outbound1 = browseSearchIndexesRequest {
                this.context = request.context
                this.filter = BrowseSearchIndexesRequestKt.filter {
                    this.createdAfter = reply0.outcome.effectiveAt
                    this.ownerId = reply0.accessPolicy.owner.userId
                }
            }
            val reply1 = searchSearchIndexQueryService.browseSearchIndexes(WireCopies.json(outbound1))
            val outbound2 = RenewDriver2Command(
                context = request.context,
                filterCreatedAfter = reply0.accessPolicy.audit.createdAt,
                filterLabels = reply0.result.detailsMap
            ).toRequest()
            val reply2 = driversDriverLicenseQueryService.browseDriverLicenses(outbound2)
            val outbound3 = lintNotificationsRequest {
                this.context = request.context
                this.notification = notification {
                    this.requester = contactInfo {
                        this.email = reply0.accessPolicy.owner.email
                    }
                }
            }
            val command3: Submission<LintNotificationsRequest> = Submission.Live(outbound3)
            val payload3 = when (val command = command3) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply3 = notificationsNotificationValidationService.lintNotifications(payload3)
            renewDriverResponse {
                this.driver = driver {
                    this.driverId = request.driverId
                    this.relationshipManager = userRef {
                        this.displayName = reply3.notification.customer.displayName
                    }
                }
                this.outcome = RenewDriverResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.message = reply0.result.message
                    this.referenceId = reply0.outcome.referenceId
                }
                this.result = operationResult {
                    this.details.putAll(reply2.result.detailsMap)
                    this.message = reply1.result.message
                    this.status = reply1.result.status
                }
            }
        }

    /** Schedules a driver. */
    override suspend fun scheduleDriverExpiry(request: ScheduleDriverExpiryRequest): ScheduleDriverExpiryResponse {
        return handleScheduleDriverExpiry(request)
    }

    /** Suspends a driver. */
    override suspend fun suspendDriver(request: SuspendDriverRequest): SuspendDriverResponse = handleSuspendDriver(request)

    /** Terminates a driver. */
    override suspend fun terminateDriver(request: TerminateDriverRequest): TerminateDriverResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLifecycleService/TerminateDriver") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "TerminateDriver", request)
            log.atInfo().addKeyValue("operation", "TerminateDriver").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = explainDriverLicenseCalculationRequest {
                this.context = request.context
                this.details = ExplainDriverLicenseCalculationRequestKt.details {
                    this.contact = contactInfo {
                        this.preferredChannel = request.details.notifyContact.preferredChannel
                    }
                }
                this.driverLicense = driverLicense {
                    this.counterparty = driverRef {
                        this.displayName = request.details.newOwner.displayName
                        this.email = request.details.notifyContact.email
                    }
                }
            }
            val reply0 = clientForTerminateDriver0.explainDriverLicenseCalculation(outbound0)
            val outbound1 = retryGeoFenceImportRequest {
                this.context = request.context
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.driverLicense.audit.updatedBy
                    }
                    this.owner = userRef {
                        this.email = request.details.newOwner.email
                    }
                }
            }
            val reply1 = flowOf(outbound1).map { value -> telematicsGeoFenceImportService.retryGeoFenceImport(value) }.single()
            val outbound2 = previewDriverLicenseRequest {
                this.context = request.context
                this.driverLicense = driverLicense {
                    this.billingAddress = postalAddress {
                        this.locality = reply0.driverLicense.billingAddress.locality
                        this.postalCode = reply0.driverLicense.billingAddress.postalCode
                    }
                    this.card = driverLicenseCardSettlement {
                        this.token = reply0.driverLicense.card.token
                    }
                    this.counterparty = driverRef {
                        this.email = request.details.newOwner.email
                    }
                }
            }
            val reply2 = withTimeout(5_000) { driversDriverLicenseValidationService.previewDriverLicense(outbound2) }
            terminateDriverResponse {
                this.driver = driver {
                    this.audit = auditInfo {
                        this.etag = reply2.driverLicense.audit.etag
                    }
                    this.driverId = request.driverId
                    this.relationshipManager = userRef {
                        this.displayName = reply0.driverLicense.counterparty.displayName
                    }
                }
                this.outcome = TerminateDriverResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private fun ExpireDriverRequest.toValidateDutyStatusFromExpireDriver0(): ValidateDutyStatusRequest {
        val source = this
        return validateDutyStatusRequest {
            this.context = source.context
            this.details = ValidateDutyStatusRequestKt.details {
                this.contact = contactInfo {
                    this.name = source.details.notifyContact.name
                    this.phone = source.details.notifyContact.phone
                }
            }
            this.dutyStatus = dutyStatus {
                this.subject = driverLicenseRef {
                    this.displayName = source.details.newOwner.displayName
                }
            }
        }
    }

    private suspend fun handleExpireDriver(request: ExpireDriverRequest): ExpireDriverResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLifecycleService/ExpireDriver") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExpireDriver", request)
            log.atInfo().addKeyValue("operation", "ExpireDriver").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = request.toValidateDutyStatusFromExpireDriver0()
            val reply0 = driversDutyStatusValidationService.validateDutyStatus(outbound0)
            expireDriverResponse {
                this.driver = driver {
                    this.contact = contactInfo {
                        this.phone = request.details.notifyContact.phone
                    }
                    this.driverId = request.driverId
                }
                this.outcome = ExpireDriverResponseKt.outcome {
                    this.effectiveAt = reply0.outcome.effectiveAt
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleGetDriverLifecycle(request: GetDriverLifecycleRequest): GetDriverLifecycleResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLifecycleService/GetDriverLifecycle") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetDriverLifecycle", request)
            log.atInfo().addKeyValue("operation", "GetDriverLifecycle").log("Handling transport request")
            val outbound0 = lintAccessPoliciesRequest {
                this.accessPolicy = accessPolicy {
                    this.owner = userRef {
                        this.userId = request.context.actor.userId
                    }
                }
                this.context = request.context
                this.details = LintAccessPoliciesRequestKt.details {
                    this.locale = request.context.locale
                }
            }
            val reply0 = authorizationAccessPolicyValidationService.lintAccessPolicies(outbound0)
            getDriverLifecycleResponse {
                this.driver = driver {
                    this.driverId = request.driverId
                    this.relationshipManager = userRef {
                        this.displayName = reply0.accessPolicy.owner.displayName
                    }
                }
                this.result = operationResult {
                    this.message = reply0.outcome.message
                    this.status = "ok"
                }
            }
        }

    private data class RenewDriver2Command(
        val context: RequestContext,
        val filterCreatedAfter: Timestamp,
        val filterLabels: Map<String, String>
    ) {
        fun toRequest(): BrowseDriverLicensesRequest {
            val command = this
            return browseDriverLicensesRequest {
                this.context = command.context
                this.filter = BrowseDriverLicensesRequestKt.filter {
                    this.createdAfter = command.filterCreatedAfter
                    this.labels.putAll(command.filterLabels)
                }
            }
        }
    }

    private suspend fun handleScheduleDriverExpiry(request: ScheduleDriverExpiryRequest): ScheduleDriverExpiryResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLifecycleService/ScheduleDriverExpiry") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScheduleDriverExpiry", request)
            log.atInfo().addKeyValue("operation", "ScheduleDriverExpiry").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driver.driverId).log("Recorded audit detail")
            val outbound0 = streamDriverLicenseMetricsRequest {
                this.context = request.context
                this.subscriber = contactInfo {
                    this.email = request.details.notify.email
                    this.preferredChannel = request.details.notify.preferredChannel
                }
            }
            driversDriverLicenseReportingService.streamDriverLicenseMetrics(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = listFuelTransactionExportsRequest {
                this.context = request.context
                this.filter = ListFuelTransactionExportsRequestKt.filter {
                    this.createdAfter = request.driver.audit.updatedAt
                    this.region = request.driver.primaryAddress.region
                }
            }
            val reply1 = invokeScheduleDriverExpiry1(outbound1)
            val outbound2 = validateDutyStatusRequest {
                this.context = request.context
                this.details = ValidateDutyStatusRequestKt.details {
                    this.contact = contactInfo {
                        this.phone = request.driver.contact.phone
                        this.preferredChannel = request.details.notify.preferredChannel
                    }
                }
                this.dutyStatus = dutyStatus {
                    this.subject = driverLicenseRef {
                        this.displayName = request.driver.relationshipManager.displayName
                        this.number = request.driver.nationalId.number
                    }
                }
            }
            val invoke2: suspend (ValidateDutyStatusRequest, Metadata) -> ValidateDutyStatusResponse = driversDutyStatusValidationService::validateDutyStatus
            val reply2 = invoke2(outbound2, Metadata())
            val outbound3 = SearchReviewsRequest.newBuilder().apply {
                setContext(request.context)
                setFilter(SearchReviewsRequestKt.filter {
                    this.region = request.driver.primaryAddress.region
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply3 = outbound3.let { value -> performanceReviewService.searchReviews(value) }
            scheduleDriverExpiryResponse {
                this.driver = driver {
                    this.driverId = request.driverId
                    this.status = request.driver.status
                }
                this.outcome = ScheduleDriverExpiryResponseKt.outcome {
                    this.message = reply1.result.message
                }
                this.result = operationResult {
                    this.details.putAll(reply3.result.detailsMap)
                    this.message = reply3.result.message
                    this.referenceIds.addAll(reply2.result.referenceIdsList)
                    this.status = reply1.result.status
                }
            }
        }

    private suspend fun handleSuspendDriver(request: SuspendDriverRequest): SuspendDriverResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLifecycleService/SuspendDriver") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SuspendDriver", request)
            log.atInfo().addKeyValue("operation", "SuspendDriver").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                summarizeDriverLicensesRequest {
                    this.annotations.putAll(request.annotationsMap)
                    this.context = request.context
                    this.reason = request.reason
                }
            }
            val reply0 = driversDriverLicenseQueryService.summarizeDriverLicenses(outbound0)
            val outbound1 = pullTripLogsRequest {
                this.context = request.context
                this.tripLog = tripLog {
                    this.counterparty = customerRef {
                        this.displayName = request.details.newOwner.displayName
                        this.email = request.details.notifyContact.email
                    }
                }
            }
            val reply1 = ClientRegistry.lookup("acme.telematics.v1.TripLogSyncService", TripLogSyncServiceGrpcKt.TripLogSyncServiceCoroutineStub::class.java).pullTripLogs(outbound1)
            val outbound2 = normalizeDriverLicenseRequest {
                this.context = request.context
                this.details = NormalizeDriverLicenseRequestKt.details {
                    this.contact = contactInfo {
                        this.name = request.details.notifyContact.name
                    }
                }
            }
            val reply2 = driversDriverLicenseValidationService.normalizeDriverLicense(WireCopies.reflective(outbound2))
            val outbound3 = summarizeVenuesRequest {
                this.context = request.context
                this.reason = request.reason
            }
            val reply3 = workerForSuspendDriver3.submit(outbound3)
            suspendDriverResponse {
                this.driver = driver {
                    this.driverId = request.driverId
                }
                this.outcome = SuspendDriverResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.message = reply0.result.message
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.correlationId = reply3.result.correlationId
                    this.status = reply3.result.status
                }
            }
        }
}
