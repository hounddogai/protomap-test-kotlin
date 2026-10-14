package com.acme.transport.telematics.grpc

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.customers.v1.CheckPreferenceEligibilityRequestKt
import com.acme.customers.v1.ConsentRecordValidationServiceGrpcKt
import com.acme.customers.v1.HouseholdCalculationServiceGrpcKt
import com.acme.customers.v1.PreferenceValidationServiceGrpcKt
import com.acme.customers.v1.ScoreConsentRecordRequest
import com.acme.customers.v1.checkPreferenceEligibilityRequest
import com.acme.customers.v1.consentRecord
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.household
import com.acme.customers.v1.householdRef
import com.acme.customers.v1.recalculateHouseholdsRequest
import com.acme.customers.v1.scoreConsentRecordRequest
import com.acme.fuel.v1.FuelTransactionReportingServiceGrpcKt
import com.acme.fuel.v1.streamFuelTransactionMetricsRequest
import com.acme.identity.v1.UserValidationServiceGrpcKt
import com.acme.identity.v1.ValidateUserRequestKt
import com.acme.identity.v1.user
import com.acme.identity.v1.userRef
import com.acme.identity.v1.validateUserRequest
import com.acme.telematics.v1.ActivateGeoFenceRequest
import com.acme.telematics.v1.ActivateGeoFenceResponse
import com.acme.telematics.v1.ActivateGeoFenceResponseKt
import com.acme.telematics.v1.ExpireGeoFenceRequest
import com.acme.telematics.v1.ExpireGeoFenceResponse
import com.acme.telematics.v1.ExpireGeoFenceResponseKt
import com.acme.telematics.v1.GenerateTripLogReportRequest
import com.acme.telematics.v1.GeoFenceImportServiceGrpcKt
import com.acme.telematics.v1.GeoFenceLifecycleServiceGrpcKt
import com.acme.telematics.v1.GeoFenceSyncServiceGrpcKt
import com.acme.telematics.v1.GeoFenceValidationServiceGrpcKt
import com.acme.telematics.v1.GetGeoFenceLifecycleRequest
import com.acme.telematics.v1.GetGeoFenceLifecycleResponse
import com.acme.telematics.v1.ListGeoFenceConflictsRequestKt
import com.acme.telematics.v1.ListGeoFenceImportsRequestKt
import com.acme.telematics.v1.MapTelemetryReadingImportColumnsRequest
import com.acme.telematics.v1.MapTelemetryReadingImportColumnsRequestKt
import com.acme.telematics.v1.MapTelemetryReadingImportColumnsResponse
import com.acme.telematics.v1.PullTelemetryReadingsRequest
import com.acme.telematics.v1.PullTripLogsRequest
import com.acme.telematics.v1.PullTripLogsResponse
import com.acme.telematics.v1.ReinstateGeoFenceRequest
import com.acme.telematics.v1.ReinstateGeoFenceResponse
import com.acme.telematics.v1.ReinstateGeoFenceResponseKt
import com.acme.telematics.v1.ScheduleGeoFenceExpiryRequest
import com.acme.telematics.v1.ScheduleGeoFenceExpiryResponse
import com.acme.telematics.v1.ScheduleGeoFenceExpiryResponseKt
import com.acme.telematics.v1.ShareTripLogExportRequestKt
import com.acme.telematics.v1.ShareTripLogReportRequestKt
import com.acme.telematics.v1.SuspendGeoFenceRequest
import com.acme.telematics.v1.SuspendGeoFenceResponse
import com.acme.telematics.v1.SuspendGeoFenceResponseKt
import com.acme.telematics.v1.TelemetryReadingImportServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingSyncServiceGrpcKt
import com.acme.telematics.v1.TerminateGeoFenceRequest
import com.acme.telematics.v1.TerminateGeoFenceResponse
import com.acme.telematics.v1.TerminateGeoFenceResponseKt
import com.acme.telematics.v1.TripLogExportServiceGrpcKt
import com.acme.telematics.v1.TripLogReportingServiceGrpcKt
import com.acme.telematics.v1.TripLogSyncServiceGrpcKt
import com.acme.telematics.v1.ValidateGeoFenceRequest
import com.acme.telematics.v1.ValidateGeoFenceRequestKt
import com.acme.telematics.v1.ValidateGeoFenceResponse
import com.acme.telematics.v1.activateGeoFenceResponse
import com.acme.telematics.v1.expireGeoFenceResponse
import com.acme.telematics.v1.generateTripLogReportRequest
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.geoFenceRef
import com.acme.telematics.v1.getGeoFenceLifecycleResponse
import com.acme.telematics.v1.listGeoFenceConflictsRequest
import com.acme.telematics.v1.listGeoFenceImportsRequest
import com.acme.telematics.v1.mapTelemetryReadingImportColumnsRequest
import com.acme.telematics.v1.pullTelemetryReadingsRequest
import com.acme.telematics.v1.pullTripLogsRequest
import com.acme.telematics.v1.reinstateGeoFenceResponse
import com.acme.telematics.v1.scheduleGeoFenceExpiryResponse
import com.acme.telematics.v1.shareTripLogExportRequest
import com.acme.telematics.v1.shareTripLogReportRequest
import com.acme.telematics.v1.streamTripLogMetricsRequest
import com.acme.telematics.v1.suspendGeoFenceResponse
import com.acme.telematics.v1.telemetryReading
import com.acme.telematics.v1.terminateGeoFenceResponse
import com.acme.telematics.v1.tripLog
import com.acme.telematics.v1.validateGeoFenceRequest
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.transport.telematics.TelematicsRepository
import com.acme.yards.v1.CommentYardMoveApprovalRequest
import com.acme.yards.v1.CommentYardMoveApprovalRequestKt
import com.acme.yards.v1.CommentYardMoveApprovalResponse
import com.acme.yards.v1.DelegateYardMoveApprovalRequestKt
import com.acme.yards.v1.SignalYardWorkflowRequestKt
import com.acme.yards.v1.YardMoveApprovalServiceGrpcKt
import com.acme.yards.v1.YardWorkflowServiceGrpcKt
import com.acme.yards.v1.commentYardMoveApprovalRequest
import com.acme.yards.v1.delegateYardMoveApprovalRequest
import com.acme.yards.v1.signalYardWorkflowRequest
import com.acme.yards.v1.yard
import com.acme.yards.v1.yardMove
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Moves a geo fence between the states of its lifecycle. */
class GeoFenceLifecycleEndpoint(
    private val store: TelematicsRepository,
    private val workers: CoroutineScope,
    private val customersConsentRecordValidationService: ConsentRecordValidationServiceGrpcKt.ConsentRecordValidationServiceCoroutineStub,
    private val customersHouseholdCalculationService: HouseholdCalculationServiceGrpcKt.HouseholdCalculationServiceCoroutineStub,
    private val customersPreferenceValidationService: PreferenceValidationServiceGrpcKt.PreferenceValidationServiceCoroutineStub,
    private val fuelFuelTransactionReportingService: FuelTransactionReportingServiceGrpcKt.FuelTransactionReportingServiceCoroutineStub,
    private val identityUserValidationService: UserValidationServiceGrpcKt.UserValidationServiceCoroutineStub,
    private val telematicsGeoFenceImportService: GeoFenceImportServiceGrpcKt.GeoFenceImportServiceCoroutineStub,
    private val telematicsGeoFenceSyncService: GeoFenceSyncServiceGrpcKt.GeoFenceSyncServiceCoroutineStub,
    private val telematicsGeoFenceValidationService: GeoFenceValidationServiceGrpcKt.GeoFenceValidationServiceCoroutineStub,
    private val telematicsTelemetryReadingImportService: TelemetryReadingImportServiceGrpcKt.TelemetryReadingImportServiceCoroutineStub,
    private val telematicsTelemetryReadingSyncService: TelemetryReadingSyncServiceGrpcKt.TelemetryReadingSyncServiceCoroutineStub,
    private val telematicsTripLogExportService: TripLogExportServiceGrpcKt.TripLogExportServiceCoroutineStub,
    private val telematicsTripLogReportingService: TripLogReportingServiceGrpcKt.TripLogReportingServiceCoroutineStub,
    private val telematicsTripLogSyncService: TripLogSyncServiceGrpcKt.TripLogSyncServiceCoroutineStub,
    private val yardsYardMoveApprovalService: YardMoveApprovalServiceGrpcKt.YardMoveApprovalServiceCoroutineStub,
    private val yardsYardWorkflowService: YardWorkflowServiceGrpcKt.YardWorkflowServiceCoroutineStub,
) : GeoFenceLifecycleServiceGrpcKt.GeoFenceLifecycleServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(GeoFenceLifecycleEndpoint::class.java)

    private val invokeExpireGeoFence3: suspend (PullTripLogsRequest) -> PullTripLogsResponse = { payload ->
        telematicsTripLogSyncService.pullTripLogs(payload)
    }

    private val workerForReinstateGeoFence3 = ReplyMailbox<MapTelemetryReadingImportColumnsRequest, MapTelemetryReadingImportColumnsResponse>(workers) { payload ->
        withTimeout(5_000) { telematicsTelemetryReadingImportService.mapTelemetryReadingImportColumns(payload) }
    }

    private val clientForScheduleGeoFenceExpiry0 by lazy { yardsYardWorkflowService }

    /** Activates a geo fence. */
    override suspend fun activateGeoFence(request: ActivateGeoFenceRequest): ActivateGeoFenceResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceLifecycleService/ActivateGeoFence") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ActivateGeoFence", request)
            log.atInfo().addKeyValue("operation", "ActivateGeoFence").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = listGeoFenceImportsRequest {
                this.context = request.context
                this.filter = ListGeoFenceImportsRequestKt.filter {
                    this.createdAfter = request.details.effectiveAt
                }
            }
            val reply0 = telematicsGeoFenceImportService.listGeoFenceImports(outbound0)
            val outbound1 = generateTripLogReportRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.reason = request.reason
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = telematicsTripLogReportingService.generateTripLogReport(envelope1.unpack(GenerateTripLogReportRequest::class.java))
            val outbound2 = shareTripLogExportRequest {
                this.context = request.context
                this.details = ShareTripLogExportRequestKt.details {
                    this.recipient = contactInfo {
                        this.email = request.details.notifyContact.email
                        this.preferredChannel = request.details.notifyContact.preferredChannel
                    }
                }
            }
            val reply2 = telematicsTripLogExportService.shareTripLogExport(WireCopies.json(outbound2))
            activateGeoFenceResponse {
                this.geoFence = geoFence {
                    this.geoFenceId = request.geoFenceId
                }
                this.outcome = ActivateGeoFenceResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.details.putAll(reply2.result.detailsMap)
                    this.status = reply0.result.status
                }
            }
        }

    /** Expires a geo fence. */
    override suspend fun expireGeoFence(request: ExpireGeoFenceRequest): ExpireGeoFenceResponse {
        return handleExpireGeoFence(request)
    }

    /** Returns a geo fence. */
    override suspend fun getGeoFenceLifecycle(request: GetGeoFenceLifecycleRequest): GetGeoFenceLifecycleResponse = handleGetGeoFenceLifecycle(request)

    /** Reinstates a geo fence. */
    override suspend fun reinstateGeoFence(request: ReinstateGeoFenceRequest): ReinstateGeoFenceResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceLifecycleService/ReinstateGeoFence") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ReinstateGeoFence", request)
            log.atInfo().addKeyValue("operation", "ReinstateGeoFence").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                listGeoFenceImportsRequest {
                    this.context = request.context
                    this.filter = ListGeoFenceImportsRequestKt.filter {
                        this.createdAfter = request.details.effectiveAt
                        this.ownerId = request.details.newOwner.userId
                    }
                }
            }
            val reply0 = telematicsGeoFenceImportService.listGeoFenceImports(outbound0)
            val outbound1 = checkPreferenceEligibilityRequest {
                this.context = request.context
                this.details = CheckPreferenceEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.details.notifyContact.email
                        this.preferredChannel = request.details.notifyContact.preferredChannel
                    }
                }
            }
            val reply1 = ClientRegistry.lookup("acme.customers.v1.PreferenceValidationService", PreferenceValidationServiceGrpcKt.PreferenceValidationServiceCoroutineStub::class.java).checkPreferenceEligibility(outbound1)
            val outbound2 = shareTripLogReportRequest {
                this.context = request.context
                this.details = ShareTripLogReportRequestKt.details {
                    this.recipient = contactInfo {
                        this.name = request.details.notifyContact.name
                        this.phone = request.details.notifyContact.phone
                    }
                }
            }
            val reply2 = telematicsTripLogReportingService.shareTripLogReport(WireCopies.reflective(outbound2))
            val outbound3 = mapTelemetryReadingImportColumnsRequest {
                this.context = request.context
                this.details = MapTelemetryReadingImportColumnsRequestKt.details {
                    this.contact = contactInfo {
                        this.email = reply1.preference.owner.email
                        this.phone = request.details.notifyContact.phone
                    }
                }
                this.telemetryReading = telemetryReading {
                    this.subject = geoFenceRef {
                        this.displayName = reply2.tripLog.counterparty.displayName
                    }
                }
            }
            val reply3 = workerForReinstateGeoFence3.submit(outbound3)
            reinstateGeoFenceResponse {
                this.geoFence = geoFence {
                    this.geoFenceId = request.geoFenceId
                }
                this.outcome = ReinstateGeoFenceResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.message = reply1.outcome.message
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = reply3.result.status
                }
            }
        }

    /** Schedules a geo fence. */
    override suspend fun scheduleGeoFenceExpiry(request: ScheduleGeoFenceExpiryRequest): ScheduleGeoFenceExpiryResponse {
        return handleScheduleGeoFenceExpiry(request)
    }

    /** Suspends a geo fence. */
    override suspend fun suspendGeoFence(request: SuspendGeoFenceRequest): SuspendGeoFenceResponse = handleSuspendGeoFence(request)

    /** Terminates a geo fence. */
    override suspend fun terminateGeoFence(request: TerminateGeoFenceRequest): TerminateGeoFenceResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceLifecycleService/TerminateGeoFence") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "TerminateGeoFence", request)
            log.atInfo().addKeyValue("operation", "TerminateGeoFence").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = delegateYardMoveApprovalRequest {
                this.context = request.context
                this.details = DelegateYardMoveApprovalRequestKt.details {
                    this.approver = userRef {
                        this.userId = request.details.newOwner.userId
                    }
                }
                this.yardMove = yardMove {
                    this.assignee = userRef {
                        this.email = request.details.notifyContact.email
                    }
                    this.requester = contactInfo {
                        this.phone = request.details.notifyContact.phone
                    }
                }
            }
            val reply0 = yardsYardMoveApprovalService.delegateYardMoveApproval(outbound0)
            val outbound1 = validateUserRequest {
                this.context = request.context
                this.details = ValidateUserRequestKt.details {
                    this.contact = contactInfo {
                        this.phone = request.details.notifyContact.phone
                    }
                }
                this.user = user {
                    this.audit = auditInfo {
                        this.etag = reply0.yardMove.audit.etag
                    }
                    this.contact = contactInfo {
                        this.email = request.details.notifyContact.email
                    }
                    this.relationshipManager = userRef {
                        this.email = reply0.yardMove.requester.email
                    }
                }
            }
            val reply1 = identityUserValidationService.validateUser(outbound1)
            terminateGeoFenceResponse {
                this.geoFence = geoFence {
                    this.geoFenceId = request.geoFenceId
                    this.owner = userRef {
                        this.userId = reply1.user.relationshipManager.userId
                    }
                }
                this.outcome = TerminateGeoFenceResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.status = reply0.outcome.status
                }
            }
        }

    private data class ExpireGeoFence0Command(
        val context: RequestContext,
        val consentRecordSubjectDisplayName: String
    ) {
        fun toRequest(): ScoreConsentRecordRequest {
            val command = this
            return scoreConsentRecordRequest {
                this.consentRecord = consentRecord {
                    this.subject = householdRef {
                        this.displayName = command.consentRecordSubjectDisplayName
                    }
                }
                this.context = command.context
            }
        }
    }

    private suspend fun handleExpireGeoFence(request: ExpireGeoFenceRequest): ExpireGeoFenceResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceLifecycleService/ExpireGeoFence") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExpireGeoFence", request)
            log.atInfo().addKeyValue("operation", "ExpireGeoFence").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = ExpireGeoFence0Command(
                context = request.context,
                consentRecordSubjectDisplayName = request.details.newOwner.displayName
            ).toRequest()
            val reply0 = customersConsentRecordValidationService.scoreConsentRecord(outbound0)
            val outbound1 = pullTelemetryReadingsRequest {
                this.context = request.context
                this.telemetryReading = telemetryReading {
                    this.correlationId = reply0.result.correlationId
                    this.subject = geoFenceRef {
                        this.displayName = request.details.newOwner.displayName
                        this.geoFenceId = request.geoFenceId
                    }
                }
            }
            val command1: Submission<PullTelemetryReadingsRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = telematicsTelemetryReadingSyncService.pullTelemetryReadings(payload1)
            val outbound2 = validateGeoFenceRequest {
                this.context = request.context
                this.details = ValidateGeoFenceRequestKt.details {
                    this.contact = contactInfo {
                        this.name = request.details.notifyContact.name
                    }
                }
                this.geoFence = geoFence {
                    this.owner = userRef {
                        this.displayName = reply0.consentRecord.subject.displayName
                    }
                }
            }
            val adapter2 = OutboundPort<ValidateGeoFenceRequest, ValidateGeoFenceResponse> { payload ->
                telematicsGeoFenceValidationService.validateGeoFence(payload)
            }
            val port2 = DelegatingPort(adapter2)
            val reply2 = port2.execute(outbound2)
            val outbound3 = pullTripLogsRequest {
                this.context = request.context
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.createdBy = reply2.geoFence.audit.createdBy
                        this.etag = reply2.geoFence.audit.etag
                    }
                    this.counterparty = customerRef {
                        this.displayName = reply2.geoFence.owner.displayName
                        this.email = request.details.newOwner.email
                    }
                }
            }
            val reply3 = invokeExpireGeoFence3(outbound3)
            expireGeoFenceResponse {
                this.geoFence = geoFence {
                    this.geoFenceId = request.geoFenceId
                    this.owner = userRef {
                        this.displayName = reply0.consentRecord.subject.displayName
                    }
                    this.priority = reply2.geoFence.priority
                }
                this.outcome = ExpireGeoFenceResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.correlationId = reply1.telemetryReading.correlationId
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleGetGeoFenceLifecycle(request: GetGeoFenceLifecycleRequest): GetGeoFenceLifecycleResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceLifecycleService/GetGeoFenceLifecycle") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetGeoFenceLifecycle", request)
            log.atInfo().addKeyValue("operation", "GetGeoFenceLifecycle").log("Handling transport request")
            val outbound0 = commentYardMoveApprovalRequest {
                this.context = request.context
                this.details = CommentYardMoveApprovalRequestKt.details {
                    this.approver = userRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val invoke0: suspend (CommentYardMoveApprovalRequest, Metadata) -> CommentYardMoveApprovalResponse = yardsYardMoveApprovalService::commentYardMoveApproval
            val reply0 = invoke0(outbound0, Metadata())
            val outbound1 = streamTripLogMetricsRequest {
                this.context = request.context
                this.subscriber = contactInfo {
                    this.phone = reply0.yardMove.requester.phone
                }
            }
            telematicsTripLogReportingService.streamTripLogMetrics(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            getGeoFenceLifecycleResponse {
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.createdBy = reply0.yardMove.audit.createdBy
                    }
                    this.geoFenceId = request.geoFenceId
                    this.owner = userRef {
                        this.displayName = reply0.yardMove.assignee.displayName
                    }
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleScheduleGeoFenceExpiry(request: ScheduleGeoFenceExpiryRequest): ScheduleGeoFenceExpiryResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceLifecycleService/ScheduleGeoFenceExpiry") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScheduleGeoFenceExpiry", request)
            log.atInfo().addKeyValue("operation", "ScheduleGeoFenceExpiry").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.geoFence.geoFenceId).log("Recorded audit detail")
            val outbound0 = signalYardWorkflowRequest {
                this.context = request.context
                this.details = SignalYardWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.name = request.details.notify.name
                        this.preferredChannel = request.details.notify.preferredChannel
                    }
                }
                this.yard = yard {
                    this.owner = userRef {
                        this.email = request.geoFence.owner.email
                    }
                }
            }
            val reply0 = clientForScheduleGeoFenceExpiry0.signalYardWorkflow(outbound0)
            scheduleGeoFenceExpiryResponse {
                this.geoFence = geoFence {
                    this.geoFenceId = request.geoFenceId
                    this.owner = request.geoFence.owner
                }
                this.outcome = ScheduleGeoFenceExpiryResponseKt.outcome {
                    this.referenceId = reply0.outcome.referenceId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSuspendGeoFence(request: SuspendGeoFenceRequest): SuspendGeoFenceResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceLifecycleService/SuspendGeoFence") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SuspendGeoFence", request)
            log.atInfo().addKeyValue("operation", "SuspendGeoFence").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = recalculateHouseholdsRequest {
                this.context = request.context
                this.household = household {
                    this.children.addAll(listOf(household {
                        this.children.addAll(listOf(household {
                            this.manager = userRef {
                                this.displayName = request.details.newOwner.displayName
                                this.email = request.details.newOwner.email
                            }
                        }))
                        this.manager = userRef {
                            this.userId = request.details.newOwner.userId
                        }
                    }))
                }
            }
            val reply0 = flowOf(outbound0).map { value -> customersHouseholdCalculationService.recalculateHouseholds(value) }.single()
            val outbound1 = streamFuelTransactionMetricsRequest {
                this.context = request.context
                this.subscriber = contactInfo {
                    this.email = reply0.household.manager.email
                }
            }
            fuelFuelTransactionReportingService.streamFuelTransactionMetrics(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val configure2: ListGeoFenceConflictsRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.filter = ListGeoFenceConflictsRequestKt.filter {
                    this.createdAfter = reply0.household.audit.updatedAt
                    this.labels.putAll(reply0.household.attributesMap)
                    this.region = reply0.household.location.region
                }
            }
            val outbound2 = listGeoFenceConflictsRequest(configure2)
            val reply2 = telematicsGeoFenceSyncService.listGeoFenceConflicts(outbound2)
            suspendGeoFenceResponse {
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.household.audit.updatedAt
                    }
                    this.geoFenceId = request.geoFenceId
                    this.owner = userRef {
                        this.email = reply0.household.manager.email
                    }
                }
                this.outcome = SuspendGeoFenceResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.details.putAll(reply2.result.detailsMap)
                    this.status = "ok"
                }
            }
        }
}
