package com.acme.transport.maintenance.adapters

import com.acme.audit.v1.AuditEventServiceGrpcKt
import com.acme.audit.v1.BatchGetAuditEventsRequest
import com.acme.audit.v1.batchGetAuditEventsRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.customerRef
import com.acme.drivers.v1.DriverQueryServiceGrpcKt
import com.acme.drivers.v1.DriverValidationServiceGrpcKt
import com.acme.drivers.v1.DutyStatusValidationServiceGrpcKt
import com.acme.drivers.v1.FindDriversRequest
import com.acme.drivers.v1.FindDriversRequestKt
import com.acme.drivers.v1.FindDriversResponse
import com.acme.drivers.v1.ScoreDutyStatusRequestKt
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.dutyStatus
import com.acme.drivers.v1.dutyStatusMeasurement
import com.acme.drivers.v1.findDriversRequest
import com.acme.drivers.v1.normalizeDriverRequest
import com.acme.drivers.v1.scoreDutyStatusRequest
import com.acme.fuel.v1.FuelTransactionSyncServiceGrpcKt
import com.acme.fuel.v1.SyncFuelTransactionsRequest
import com.acme.fuel.v1.SyncFuelTransactionsResponse
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.fuelTransactionMeasurement
import com.acme.fuel.v1.pullFuelTransactionsRequest
import com.acme.fuel.v1.syncFuelTransactionsRequest
import com.acme.identity.v1.CloneUserRequest
import com.acme.identity.v1.UserServiceGrpcKt
import com.acme.identity.v1.cloneUserRequest
import com.acme.identity.v1.user
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.ActivatePartRequest
import com.acme.maintenance.v1.ActivatePartResponse
import com.acme.maintenance.v1.ActivatePartResponseKt
import com.acme.maintenance.v1.DeletePartReportRequest
import com.acme.maintenance.v1.DeletePartReportResponse
import com.acme.maintenance.v1.ExpirePartRequest
import com.acme.maintenance.v1.ExpirePartResponse
import com.acme.maintenance.v1.ExpirePartResponseKt
import com.acme.maintenance.v1.GetPartLifecycleRequest
import com.acme.maintenance.v1.GetPartLifecycleResponse
import com.acme.maintenance.v1.PartLifecycleServiceGrpcKt
import com.acme.maintenance.v1.PartReportingServiceGrpcKt
import com.acme.maintenance.v1.RenewPartRequest
import com.acme.maintenance.v1.RenewPartResponse
import com.acme.maintenance.v1.RenewPartResponseKt
import com.acme.maintenance.v1.SchedulePartExpiryRequest
import com.acme.maintenance.v1.SchedulePartExpiryResponse
import com.acme.maintenance.v1.ServiceScheduleCalculationServiceGrpcKt
import com.acme.maintenance.v1.SharePartReportRequest
import com.acme.maintenance.v1.SharePartReportRequestKt
import com.acme.maintenance.v1.SuspendPartRequest
import com.acme.maintenance.v1.SuspendPartResponse
import com.acme.maintenance.v1.SuspendPartResponseKt
import com.acme.maintenance.v1.TerminatePartRequest
import com.acme.maintenance.v1.TerminatePartResponse
import com.acme.maintenance.v1.TerminatePartResponseKt
import com.acme.maintenance.v1.activatePartResponse
import com.acme.maintenance.v1.deletePartReportRequest
import com.acme.maintenance.v1.expirePartResponse
import com.acme.maintenance.v1.getPartLifecycleResponse
import com.acme.maintenance.v1.part
import com.acme.maintenance.v1.partDimensions
import com.acme.maintenance.v1.quoteServiceScheduleRequest
import com.acme.maintenance.v1.recalculateServiceSchedulesRequest
import com.acme.maintenance.v1.renewPartResponse
import com.acme.maintenance.v1.schedulePartExpiryResponse
import com.acme.maintenance.v1.serviceSchedule
import com.acme.maintenance.v1.serviceScheduleParticipant
import com.acme.maintenance.v1.sharePartReportRequest
import com.acme.maintenance.v1.suspendPartResponse
import com.acme.maintenance.v1.terminatePartResponse
import com.acme.media.v1.MediaAssetQueryServiceGrpcKt
import com.acme.media.v1.ResolveMediaAssetsRequest
import com.acme.media.v1.ResolveMediaAssetsResponse
import com.acme.media.v1.resolveMediaAssetsRequest
import com.acme.subscriptions.v1.SearchSubscriptionsRequestKt
import com.acme.subscriptions.v1.SubscriptionServiceGrpcKt
import com.acme.subscriptions.v1.searchSubscriptionsRequest
import com.acme.transport.maintenance.MaintenanceRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.venues.v1.ExplainVenueDecisionRequest
import com.acme.venues.v1.ExplainVenueDecisionRequestKt
import com.acme.venues.v1.VenueValidationServiceGrpcKt
import com.acme.venues.v1.explainVenueDecisionRequest
import com.acme.yards.v1.YardApprovalServiceGrpcKt
import com.acme.yards.v1.commentYardApprovalRequest
import com.acme.yards.v1.yard
import com.google.protobuf.Any as ProtoAny
import com.google.protobuf.Timestamp
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Moves a part between the states of its lifecycle. */
class PartLifecycleController(
    private val store: MaintenanceRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventService: AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub,
    private val driversDriverQueryService: DriverQueryServiceGrpcKt.DriverQueryServiceCoroutineStub,
    private val driversDriverValidationService: DriverValidationServiceGrpcKt.DriverValidationServiceCoroutineStub,
    private val driversDutyStatusValidationService: DutyStatusValidationServiceGrpcKt.DutyStatusValidationServiceCoroutineStub,
    private val fuelFuelTransactionSyncService: FuelTransactionSyncServiceGrpcKt.FuelTransactionSyncServiceCoroutineStub,
    private val identityUserService: UserServiceGrpcKt.UserServiceCoroutineStub,
    private val maintenancePartReportingService: PartReportingServiceGrpcKt.PartReportingServiceCoroutineStub,
    private val maintenanceServiceScheduleCalculationService: ServiceScheduleCalculationServiceGrpcKt.ServiceScheduleCalculationServiceCoroutineStub,
    private val mediaMediaAssetQueryService: MediaAssetQueryServiceGrpcKt.MediaAssetQueryServiceCoroutineStub,
    private val subscriptionsSubscriptionService: SubscriptionServiceGrpcKt.SubscriptionServiceCoroutineStub,
    private val venuesVenueValidationService: VenueValidationServiceGrpcKt.VenueValidationServiceCoroutineStub,
    private val yardsYardApprovalService: YardApprovalServiceGrpcKt.YardApprovalServiceCoroutineStub,
) : PartLifecycleServiceGrpcKt.PartLifecycleServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(PartLifecycleController::class.java)

    private val invokeGetPartLifecycle0: suspend (FindDriversRequest) -> FindDriversResponse = { payload ->
        driversDriverQueryService.findDrivers(payload)
    }

    private val workerForSuspendPart3 = ReplyMailbox<SyncFuelTransactionsRequest, SyncFuelTransactionsResponse>(workers) { payload ->
        withTimeout(5_000) { fuelFuelTransactionSyncService.syncFuelTransactions(payload) }
    }

    private val clientForTerminatePart0 by lazy { yardsYardApprovalService }

    /** Activates a part. */
    override suspend fun activatePart(request: ActivatePartRequest): ActivatePartResponse = handleActivatePart(request)

    /** Expires a part. */
    override suspend fun expirePart(request: ExpirePartRequest): ExpirePartResponse =
        RpcBoundary.run("acme.maintenance.v1.PartLifecycleService/ExpirePart") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExpirePart", request)
            log.atInfo().addKeyValue("operation", "ExpirePart").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = sharePartReportRequest {
                this.context = request.context
                this.details = SharePartReportRequestKt.details {
                    this.recipient = contactInfo {
                        this.phone = request.details.notifyContact.phone
                        this.preferredChannel = request.details.notifyContact.preferredChannel
                    }
                }
            }
            val command0: Submission<SharePartReportRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = maintenancePartReportingService.sharePartReport(payload0)
            val outbound1 = resolveMediaAssetsRequest {
                this.context = request.context
            }
            val adapter1 = OutboundPort<ResolveMediaAssetsRequest, ResolveMediaAssetsResponse> { payload ->
                mediaMediaAssetQueryService.resolveMediaAssets(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            expirePartResponse {
                this.outcome = ExpirePartResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.part = part {
                    this.dimensions = partDimensions {
                        this.length = reply0.part.dimensions.length
                    }
                    this.partId = request.partId
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    /** Returns a part. */
    override suspend fun getPartLifecycle(request: GetPartLifecycleRequest): GetPartLifecycleResponse {
        return handleGetPartLifecycle(request)
    }

    /** Renews a part. */
    override suspend fun renewPart(request: RenewPartRequest): RenewPartResponse = handleRenewPart(request)

    /** Schedules a part. */
    override suspend fun schedulePartExpiry(request: SchedulePartExpiryRequest): SchedulePartExpiryResponse =
        RpcBoundary.run("acme.maintenance.v1.PartLifecycleService/SchedulePartExpiry") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SchedulePartExpiry", request)
            log.atInfo().addKeyValue("operation", "SchedulePartExpiry").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.part.partId).log("Recorded audit detail")
            val outbound0 = BatchGetAuditEventsRequest.newBuilder().apply {
                setContext(request.context)
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> auditAuditEventService.batchGetAuditEvents(value) }
            schedulePartExpiryResponse {
                this.part = part {
                    this.partId = request.partId
                    this.specs.putAll(request.part.specsMap)
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    /** Suspends a part. */
    override suspend fun suspendPart(request: SuspendPartRequest): SuspendPartResponse {
        return handleSuspendPart(request)
    }

    /** Terminates a part. */
    override suspend fun terminatePart(request: TerminatePartRequest): TerminatePartResponse = handleTerminatePart(request)

    private data class ActivatePart2Command(
        val context: RequestContext,
        val userAuditCreatedAt: Timestamp,
        val userPrimaryAddressCountryCode: String
    ) {
        fun toRequest(): CloneUserRequest {
            val command = this
            return cloneUserRequest {
                this.context = command.context
                this.user = user {
                    this.audit = auditInfo {
                        this.createdAt = command.userAuditCreatedAt
                    }
                    this.primaryAddress = postalAddress {
                        this.countryCode = command.userPrimaryAddressCountryCode
                    }
                }
            }
        }
    }

    private suspend fun handleActivatePart(request: ActivatePartRequest): ActivatePartResponse =
        RpcBoundary.run("acme.maintenance.v1.PartLifecycleService/ActivatePart") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ActivatePart", request)
            log.atInfo().addKeyValue("operation", "ActivatePart").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = explainVenueDecisionRequest {
                this.context = request.context
                this.details = ExplainVenueDecisionRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.details.newOwner.email
                    }
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = venuesVenueValidationService.explainVenueDecision(envelope0.unpack(ExplainVenueDecisionRequest::class.java))
            val outbound1 = normalizeDriverRequest {
                this.context = request.context
                this.driver = driver {
                    this.primaryAddress = postalAddress {
                        this.region = reply0.venue.address.region
                    }
                }
            }
            val reply1 = driversDriverValidationService.normalizeDriver(WireCopies.json(outbound1))
            val outbound2 = ActivatePart2Command(
                context = request.context,
                userAuditCreatedAt = reply0.venue.audit.createdAt,
                userPrimaryAddressCountryCode = reply0.venue.address.countryCode
            ).toRequest()
            val reply2 = identityUserService.cloneUser(outbound2)
            activatePartResponse {
                this.outcome = ActivatePartResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.part = part {
                    this.audit = auditInfo {
                        this.etag = reply1.driver.audit.etag
                    }
                    this.name = reply0.venue.name
                    this.owner = userRef {
                        this.displayName = reply2.user.relationshipManager.displayName
                        this.userId = reply2.user.relationshipManager.userId
                    }
                    this.partId = request.partId
                }
                this.result = operationResult {
                    this.status = reply1.outcome.status
                }
            }
        }

    private suspend fun handleGetPartLifecycle(request: GetPartLifecycleRequest): GetPartLifecycleResponse =
        RpcBoundary.run("acme.maintenance.v1.PartLifecycleService/GetPartLifecycle") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetPartLifecycle", request)
            log.atInfo().addKeyValue("operation", "GetPartLifecycle").log("Handling transport request")
            val outbound0 = findDriversRequest {
                this.context = request.context
                this.filter = FindDriversRequestKt.filter {
                    this.createdAfter = request.context.deadline
                    this.labels.putAll(request.context.baggageMap)
                    this.ownerId = request.partId
                }
            }
            val reply0 = invokeGetPartLifecycle0(outbound0)
            getPartLifecycleResponse {
                this.part = part {
                    this.partId = request.partId
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleRenewPart(request: RenewPartRequest): RenewPartResponse =
        RpcBoundary.run("acme.maintenance.v1.PartLifecycleService/RenewPart") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RenewPart", request)
            log.atInfo().addKeyValue("operation", "RenewPart").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = deletePartReportRequest {
                this.context = request.context
                this.partId = request.partId
                this.reason = request.reason
            }
            val invoke0: suspend (DeletePartReportRequest, Metadata) -> DeletePartReportResponse = maintenancePartReportingService::deletePartReport
            val reply0 = invoke0(outbound0, Metadata())
            renewPartResponse {
                this.outcome = RenewPartResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.part = part {
                    this.partId = request.partId
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSuspendPart(request: SuspendPartRequest): SuspendPartResponse =
        RpcBoundary.run("acme.maintenance.v1.PartLifecycleService/SuspendPart") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SuspendPart", request)
            log.atInfo().addKeyValue("operation", "SuspendPart").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                quoteServiceScheduleRequest {
                    this.context = request.context
                    this.serviceSchedule = serviceSchedule {
                        this.organizer = userRef {
                            this.email = request.details.notifyContact.email
                        }
                        this.participants.addAll(listOf(serviceScheduleParticipant {
                            this.contact = contactInfo {
                                this.name = request.details.notifyContact.name
                            }
                        }))
                    }
                }
            }
            val reply0 = maintenanceServiceScheduleCalculationService.quoteServiceSchedule(outbound0)
            val outbound1 = searchSubscriptionsRequest {
                this.context = request.context
                this.filter = SearchSubscriptionsRequestKt.filter {
                    this.createdAfter = request.details.effectiveAt
                    this.region = reply0.serviceSchedule.location.region
                }
            }
            val reply1 = ClientRegistry.lookup("acme.subscriptions.v1.SubscriptionService", SubscriptionServiceGrpcKt.SubscriptionServiceCoroutineStub::class.java).searchSubscriptions(outbound1)
            val outbound2 = recalculateServiceSchedulesRequest {
                this.context = request.context
                this.serviceSchedule = serviceSchedule {
                    this.participants.addAll(listOf(serviceScheduleParticipant {
                        this.contact = contactInfo {
                            this.email = request.details.newOwner.email
                            this.name = request.details.notifyContact.name
                        }
                    }))
                }
            }
            val reply2 = maintenanceServiceScheduleCalculationService.recalculateServiceSchedules(WireCopies.reflective(outbound2))
            val outbound3 = syncFuelTransactionsRequest {
                this.context = request.context
                this.fuelTransaction = fuelTransaction {
                    this.subject = customerRef {
                        this.displayName = reply0.serviceSchedule.organizer.displayName
                        this.email = request.details.notifyContact.email
                    }
                }
            }
            val reply3 = workerForSuspendPart3.submit(outbound3)
            suspendPartResponse {
                this.outcome = SuspendPartResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.nextSteps.addAll(reply3.outcome.nextStepsList)
                    this.status = reply1.result.status
                }
                this.part = part {
                    this.audit = auditInfo {
                        this.createdAt = reply2.serviceSchedule.audit.createdAt
                        this.updatedBy = reply0.serviceSchedule.audit.updatedBy
                    }
                    this.owner = userRef {
                        this.email = reply3.fuelTransaction.subject.email
                    }
                    this.partId = request.partId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleTerminatePart(request: TerminatePartRequest): TerminatePartResponse =
        RpcBoundary.run("acme.maintenance.v1.PartLifecycleService/TerminatePart") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "TerminatePart", request)
            log.atInfo().addKeyValue("operation", "TerminatePart").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = commentYardApprovalRequest {
                this.context = request.context
                this.yard = yard {
                    this.owner = userRef {
                        this.email = request.details.newOwner.email
                    }
                }
            }
            val reply0 = clientForTerminatePart0.commentYardApproval(outbound0)
            val outbound1 = pullFuelTransactionsRequest {
                this.context = request.context
                this.fuelTransaction = fuelTransaction {
                    this.measurements.addAll(listOf(fuelTransactionMeasurement {
                        this.unit = reply0.yard.dimensions.unit
                    }))
                    this.subject = customerRef {
                        this.email = request.details.notifyContact.email
                    }
                }
            }
            val reply1 = flowOf(outbound1).map { value -> fuelFuelTransactionSyncService.pullFuelTransactions(value) }.single()
            val outbound2 = scoreDutyStatusRequest {
                this.context = request.context
                this.details = ScoreDutyStatusRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = reply0.yard.address.countryCode
                    }
                }
                this.dutyStatus = dutyStatus {
                    this.measurements.addAll(listOf(dutyStatusMeasurement {
                        this.unit = reply0.yard.dimensions.unit
                    }))
                }
            }
            val reply2 = withTimeout(5_000) { driversDutyStatusValidationService.scoreDutyStatus(outbound2) }
            terminatePartResponse {
                this.outcome = TerminatePartResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.nextSteps.addAll(reply1.outcome.nextStepsList)
                    this.status = reply1.outcome.status
                }
                this.part = part {
                    this.audit = auditInfo {
                        this.createdAt = reply0.yard.audit.createdAt
                    }
                    this.owner = userRef {
                        this.displayName = reply0.yard.owner.displayName
                    }
                    this.partId = request.partId
                }
                this.result = operationResult {
                    this.correlationId = reply2.dutyStatus.correlationId
                    this.details.putAll(reply2.result.detailsMap)
                    this.status = "ok"
                }
            }
        }
}
