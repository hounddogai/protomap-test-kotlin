package com.acme.transport.telematics.api

import com.acme.authorization.v1.AccessPolicyQueryServiceGrpcKt
import com.acme.authorization.v1.FindAccessPoliciesRequest
import com.acme.authorization.v1.FindAccessPoliciesRequestKt
import com.acme.authorization.v1.findAccessPoliciesRequest
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.customers.v1.CustomerQueryServiceGrpcKt
import com.acme.customers.v1.CustomerSegmentValidationServiceGrpcKt
import com.acme.customers.v1.CustomerServiceGrpcKt
import com.acme.customers.v1.SuggestCustomersRequestKt
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.customerSegment
import com.acme.customers.v1.explainCustomerSegmentDecisionRequest
import com.acme.customers.v1.getCustomerRequest
import com.acme.customers.v1.suggestCustomersRequest
import com.acme.drivers.v1.driverRef
import com.acme.fuel.v1.FuelCardWorkflowServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionWorkflowServiceGrpcKt
import com.acme.fuel.v1.SignalFuelTransactionWorkflowRequestKt
import com.acme.fuel.v1.StartFuelCardWorkflowRequestKt
import com.acme.fuel.v1.StartFuelTransactionWorkflowRequest
import com.acme.fuel.v1.StartFuelTransactionWorkflowRequestKt
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.pauseFuelCardWorkflowRequest
import com.acme.fuel.v1.retryFuelTransactionWorkflowRequest
import com.acme.fuel.v1.signalFuelTransactionWorkflowRequest
import com.acme.fuel.v1.startFuelCardWorkflowRequest
import com.acme.fuel.v1.startFuelTransactionWorkflowRequest
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.CommentPartApprovalResponse
import com.acme.maintenance.v1.PartApprovalServiceGrpcKt
import com.acme.maintenance.v1.RetryWorkOrderWorkflowRequest
import com.acme.maintenance.v1.RetryWorkOrderWorkflowRequestKt
import com.acme.maintenance.v1.WorkOrderWorkflowServiceGrpcKt
import com.acme.maintenance.v1.commentPartApprovalRequest
import com.acme.maintenance.v1.part
import com.acme.maintenance.v1.partComponent
import com.acme.maintenance.v1.retryWorkOrderWorkflowRequest
import com.acme.maintenance.v1.workOrder
import com.acme.notifications.v1.NotificationQueryServiceGrpcKt
import com.acme.notifications.v1.ResolveNotificationsRequest
import com.acme.notifications.v1.resolveNotificationsRequest
import com.acme.telematics.v1.ApproveGeoFenceResponse
import com.acme.telematics.v1.ApproveTripLogRequest
import com.acme.telematics.v1.ApproveTripLogResponse
import com.acme.telematics.v1.ApproveTripLogResponseKt
import com.acme.telematics.v1.CommentTelemetryReadingApprovalRequest
import com.acme.telematics.v1.CommentTelemetryReadingApprovalRequestKt
import com.acme.telematics.v1.CommentTelemetryReadingApprovalResponse
import com.acme.telematics.v1.CommentTripLogApprovalRequest
import com.acme.telematics.v1.CommentTripLogApprovalResponse
import com.acme.telematics.v1.CommentTripLogApprovalResponseKt
import com.acme.telematics.v1.DelegateTelemetryReadingApprovalRequestKt
import com.acme.telematics.v1.DelegateTripLogApprovalRequest
import com.acme.telematics.v1.DelegateTripLogApprovalResponse
import com.acme.telematics.v1.DelegateTripLogApprovalResponseKt
import com.acme.telematics.v1.EscalateTripLogRequest
import com.acme.telematics.v1.EscalateTripLogResponse
import com.acme.telematics.v1.EscalateTripLogResponseKt
import com.acme.telematics.v1.GeoFenceApprovalServiceGrpcKt
import com.acme.telematics.v1.GetTripLogApprovalStatusRequest
import com.acme.telematics.v1.GetTripLogApprovalStatusResponse
import com.acme.telematics.v1.ListPendingTelemetryReadingsRequestKt
import com.acme.telematics.v1.ListPendingTripLogsRequest
import com.acme.telematics.v1.ListPendingTripLogsResponse
import com.acme.telematics.v1.MapTelemetryReadingImportColumnsRequest
import com.acme.telematics.v1.MapTelemetryReadingImportColumnsResponse
import com.acme.telematics.v1.RecallTripLogRequest
import com.acme.telematics.v1.RecallTripLogResponse
import com.acme.telematics.v1.RecallTripLogResponseKt
import com.acme.telematics.v1.RejectTelemetryReadingResponse
import com.acme.telematics.v1.RejectTripLogRequest
import com.acme.telematics.v1.RejectTripLogResponse
import com.acme.telematics.v1.RejectTripLogResponseKt
import com.acme.telematics.v1.ScheduleTelemetryReadingExpiryRequest
import com.acme.telematics.v1.ScheduleTelemetryReadingExpiryRequestKt
import com.acme.telematics.v1.ScheduleTelemetryReadingExpiryResponse
import com.acme.telematics.v1.SubmitTelemetryReadingRequestKt
import com.acme.telematics.v1.SubmitTripLogRequest
import com.acme.telematics.v1.SubmitTripLogResponse
import com.acme.telematics.v1.TelemetryReadingApprovalServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingImportServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingLifecycleServiceGrpcKt
import com.acme.telematics.v1.TripLogApprovalServiceGrpcKt
import com.acme.telematics.v1.TripLogLine
import com.acme.telematics.v1.approveGeoFenceRequest
import com.acme.telematics.v1.approveTripLogResponse
import com.acme.telematics.v1.commentTelemetryReadingApprovalRequest
import com.acme.telematics.v1.commentTripLogApprovalResponse
import com.acme.telematics.v1.copy as telematicsCopy
import com.acme.telematics.v1.delegateTelemetryReadingApprovalRequest
import com.acme.telematics.v1.delegateTripLogApprovalResponse
import com.acme.telematics.v1.escalateTripLogResponse
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.geoFenceDiscountAction
import com.acme.telematics.v1.geoFenceRef
import com.acme.telematics.v1.getTripLogApprovalStatusResponse
import com.acme.telematics.v1.listPendingTelemetryReadingsRequest
import com.acme.telematics.v1.listPendingTripLogsResponse
import com.acme.telematics.v1.mapTelemetryReadingImportColumnsRequest
import com.acme.telematics.v1.recallTripLogResponse
import com.acme.telematics.v1.rejectTelemetryReadingRequest
import com.acme.telematics.v1.rejectTripLogResponse
import com.acme.telematics.v1.scheduleTelemetryReadingExpiryRequest
import com.acme.telematics.v1.submitTelemetryReadingRequest
import com.acme.telematics.v1.submitTripLogResponse
import com.acme.telematics.v1.telemetryReading
import com.acme.telematics.v1.tripLog
import com.acme.telematics.v1.tripLogLine
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.transport.telematics.TelematicsRepository
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Routes trip logs through approval, with escalation and delegation. */
class TripLogApprovalGrpcService(
    private val store: TelematicsRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyQueryService: AccessPolicyQueryServiceGrpcKt.AccessPolicyQueryServiceCoroutineStub,
    private val customersCustomerQueryService: CustomerQueryServiceGrpcKt.CustomerQueryServiceCoroutineStub,
    private val customersCustomerSegmentValidationService: CustomerSegmentValidationServiceGrpcKt.CustomerSegmentValidationServiceCoroutineStub,
    private val customersCustomerService: CustomerServiceGrpcKt.CustomerServiceCoroutineStub,
    private val fuelFuelCardWorkflowService: FuelCardWorkflowServiceGrpcKt.FuelCardWorkflowServiceCoroutineStub,
    private val fuelFuelTransactionWorkflowService: FuelTransactionWorkflowServiceGrpcKt.FuelTransactionWorkflowServiceCoroutineStub,
    private val maintenancePartApprovalService: PartApprovalServiceGrpcKt.PartApprovalServiceCoroutineStub,
    private val maintenanceWorkOrderWorkflowService: WorkOrderWorkflowServiceGrpcKt.WorkOrderWorkflowServiceCoroutineStub,
    private val notificationsNotificationQueryService: NotificationQueryServiceGrpcKt.NotificationQueryServiceCoroutineStub,
    private val telematicsGeoFenceApprovalService: GeoFenceApprovalServiceGrpcKt.GeoFenceApprovalServiceCoroutineStub,
    private val telematicsTelemetryReadingApprovalService: TelemetryReadingApprovalServiceGrpcKt.TelemetryReadingApprovalServiceCoroutineStub,
    private val telematicsTelemetryReadingImportService: TelemetryReadingImportServiceGrpcKt.TelemetryReadingImportServiceCoroutineStub,
    private val telematicsTelemetryReadingLifecycleService: TelemetryReadingLifecycleServiceGrpcKt.TelemetryReadingLifecycleServiceCoroutineStub,
) : TripLogApprovalServiceGrpcKt.TripLogApprovalServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(TripLogApprovalGrpcService::class.java)

    private val invokeEscalateTripLog1: suspend (CommentTelemetryReadingApprovalRequest) -> CommentTelemetryReadingApprovalResponse = { payload ->
        telematicsTelemetryReadingApprovalService.commentTelemetryReadingApproval(payload)
    }

    private val workerForRecallTripLog1 = ReplyMailbox<MapTelemetryReadingImportColumnsRequest, MapTelemetryReadingImportColumnsResponse>(workers) { payload ->
        withTimeout(5_000) { telematicsTelemetryReadingImportService.mapTelemetryReadingImportColumns(payload) }
    }

    private val clientForRecallTripLog2 by lazy { telematicsTelemetryReadingApprovalService }

    /** Approves a trip log. */
    override suspend fun approveTripLog(request: ApproveTripLogRequest): ApproveTripLogResponse {
        return handleApproveTripLog(request)
    }

    /** Comments a trip log. */
    override suspend fun commentTripLogApproval(request: CommentTripLogApprovalRequest): CommentTripLogApprovalResponse = handleCommentTripLogApproval(request)

    /** Delegates a trip log. */
    override suspend fun delegateTripLogApproval(request: DelegateTripLogApprovalRequest): DelegateTripLogApprovalResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogApprovalService/DelegateTripLogApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DelegateTripLogApproval", request)
            log.atInfo().addKeyValue("operation", "DelegateTripLogApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLog.tripLogId).log("Recorded audit detail")
            val outbound0 = suggestCustomersRequest {
                this.context = request.context
                this.filter = SuggestCustomersRequestKt.filter {
                    this.createdAfter = request.tripLog.audit.updatedAt
                    this.region = request.tripLog.billingAddress.region
                }
            }
            val reply0 = customersCustomerQueryService.suggestCustomers(WireCopies.json(outbound0))
            var reply1 = ApproveGeoFenceResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.tripLog.linesList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound1 = approveGeoFenceRequest {
                                this.context = request.context
                                this.geoFence = geoFence {
                                    this.description = item.description
                                    this.discount = geoFenceDiscountAction {
                                        this.amount = item.tax.amount
                                    }
                                    this.owner = userRef {
                                        this.displayName = request.details.approver.displayName
                                    }
                                }
                            }
                            telematicsGeoFenceApprovalService.approveGeoFence(outbound1)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply1 = it }
                }
            }
            val outbound2 = startFuelTransactionWorkflowRequest {
                this.context = request.context
                this.details = StartFuelTransactionWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.email = request.tripLog.counterparty.email
                    }
                }
                this.fuelTransaction = fuelTransaction {
                    this.subject = customerRef {
                        this.displayName = request.details.approver.displayName
                        this.email = request.details.approver.email
                    }
                }
            }
            val command2: Submission<StartFuelTransactionWorkflowRequest> = Submission.Live(outbound2)
            val payload2 = when (val command = command2) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply2 = fuelFuelTransactionWorkflowService.startFuelTransactionWorkflow(payload2)
            delegateTripLogApprovalResponse {
                this.outcome = DelegateTripLogApprovalResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.status = reply2.outcome.status
                }
                this.tripLog = tripLog {
                    this.metadata.putAll(request.tripLog.metadataMap)
                    this.tripLogId = request.tripLogId
                }
            }
        }

    /** Escalates a trip log. */
    override suspend fun escalateTripLog(request: EscalateTripLogRequest): EscalateTripLogResponse {
        return handleEscalateTripLog(request)
    }

    /** Returns a trip log. */
    override suspend fun getTripLogApprovalStatus(request: GetTripLogApprovalStatusRequest): GetTripLogApprovalStatusResponse = handleGetTripLogApprovalStatus(request)

    /** Lists trip logs a page at a time. */
    override suspend fun listPendingTripLogs(request: ListPendingTripLogsRequest): ListPendingTripLogsResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogApprovalService/ListPendingTripLogs") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListPendingTripLogs", request)
            log.atInfo().addKeyValue("operation", "ListPendingTripLogs").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                explainCustomerSegmentDecisionRequest {
                    this.context = request.context
                    this.customerSegment = customerSegment {
                        this.owner = userRef {
                            this.email = request.context.actor.email
                            this.userId = request.context.actor.userId
                        }
                    }
                }
            }
            val reply0 = customersCustomerSegmentValidationService.explainCustomerSegmentDecision(outbound0)
            val outbound1 = retryFuelTransactionWorkflowRequest {
                this.context = request.context
                this.fuelTransaction = fuelTransaction {
                    this.subject = customerRef {
                        this.displayName = reply0.customerSegment.owner.displayName
                        this.email = reply0.customerSegment.owner.email
                    }
                }
            }
            val reply1 = ClientRegistry.lookup("acme.fuel.v1.FuelTransactionWorkflowService", FuelTransactionWorkflowServiceGrpcKt.FuelTransactionWorkflowServiceCoroutineStub::class.java).retryFuelTransactionWorkflow(outbound1)
            listPendingTripLogsResponse {
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = reply1.outcome.status
                }
                this.tripLogs.addAll(listOf(tripLog {
                    this.counterparty = customerRef {
                        this.email = reply0.customerSegment.owner.email
                    }
                    this.lines.addAll(listOf(tripLogLine {
                        this.description = reply0.customerSegment.description
                    }))
                }))
            }
        }

    /** Recalls a trip log. */
    override suspend fun recallTripLog(request: RecallTripLogRequest): RecallTripLogResponse {
        return handleRecallTripLog(request)
    }

    /** Rejects a trip log. */
    override suspend fun rejectTripLog(request: RejectTripLogRequest): RejectTripLogResponse = handleRejectTripLog(request)

    /** Submits a trip log. */
    override suspend fun submitTripLog(request: SubmitTripLogRequest): SubmitTripLogResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogApprovalService/SubmitTripLog") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SubmitTripLog", request)
            log.atInfo().addKeyValue("operation", "SubmitTripLog").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLog.tripLogId).log("Recorded audit detail")
            val outbound0 = getCustomerRequest {
                this.context = request.context
                this.customerId = request.tripLog.counterparty.customerId
            }
            val reply0 = withTimeout(5_000) { customersCustomerService.getCustomer(outbound0) }
            val configure1: StartFuelCardWorkflowRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.updatedAt = request.tripLog.audit.updatedAt
                    }
                    this.contact = contactInfo {
                        this.email = request.tripLog.counterparty.email
                        this.name = reply0.customer.contact.name
                    }
                    this.holder = driverRef {
                        this.email = reply0.customer.contact.email
                    }
                }
            }
            val outbound1 = startFuelCardWorkflowRequest(configure1)
            val reply1 = fuelFuelCardWorkflowService.startFuelCardWorkflow(outbound1)
            submitTripLogResponse {
                this.result = operationResult {
                    this.message = reply1.outcome.message
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.counterparty = customerRef {
                        this.displayName = reply0.customer.relationshipManager.displayName
                    }
                    this.dueAt = request.tripLog.dueAt
                    this.tripLogId = request.tripLogId
                }
            }
        }

    private fun ApproveTripLogRequest.toResolveNotificationsFromApproveTripLog2(): ResolveNotificationsRequest {
        val source = this
        return resolveNotificationsRequest {
            this.context = source.context
        }
    }

    private suspend fun handleApproveTripLog(request: ApproveTripLogRequest): ApproveTripLogResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogApprovalService/ApproveTripLog") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ApproveTripLog", request)
            log.atInfo().addKeyValue("operation", "ApproveTripLog").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLog.tripLogId).log("Recorded audit detail")
            var reply0 = CommentPartApprovalResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<TripLogLine>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound0 = commentPartApprovalRequest {
                            this.context = request.context
                            this.part = part {
                                this.components.putAll(mapOf("primary" to partComponent {
                                    this.quantity = item.quantity
                                }))
                            }
                        }
                        reply0 = maintenancePartApprovalService.commentPartApproval(outbound0)
                    }
                }
                try {
                    request.tripLog.linesList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound1 = listPendingTelemetryReadingsRequest {}.telematicsCopy {
                this.context = request.context
                this.filter = ListPendingTelemetryReadingsRequestKt.filter {
                    this.createdAfter = request.details.effectiveAt
                    this.region = reply0.part.address.region
                }
            }
            val reply1 = telematicsTelemetryReadingApprovalService.listPendingTelemetryReadings(outbound1)
            val outbound2 = request.toResolveNotificationsFromApproveTripLog2()
            val reply2 = notificationsNotificationQueryService.resolveNotifications(outbound2)
            approveTripLogResponse {
                this.outcome = ApproveTripLogResponseKt.outcome {
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.status = reply1.result.status
                }
                this.tripLog = tripLog {
                    this.issuedAt = request.tripLog.issuedAt
                    this.tripLogId = request.tripLogId
                }
            }
        }

    private suspend fun handleCommentTripLogApproval(request: CommentTripLogApprovalRequest): CommentTripLogApprovalResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogApprovalService/CommentTripLogApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CommentTripLogApproval", request)
            log.atInfo().addKeyValue("operation", "CommentTripLogApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLog.tripLogId).log("Recorded audit detail")
            val outbound0 = submitTelemetryReadingRequest {
                this.context = request.context
                this.details = SubmitTelemetryReadingRequestKt.details {
                    this.approver = userRef {
                        this.displayName = request.tripLog.counterparty.displayName
                        this.email = request.details.approver.email
                    }
                    this.comment = request.details.comment
                }
            }
            val reply0 = telematicsTelemetryReadingApprovalService.submitTelemetryReading(outbound0)
            val outbound1 = findAccessPoliciesRequest {
                this.context = request.context
                this.filter = FindAccessPoliciesRequestKt.filter {
                    this.createdAfter = request.tripLog.audit.createdAt
                    this.labels.putAll(reply0.telemetryReading.attributesMap)
                    this.ownerId = request.tripLogId
                    this.region = request.tripLog.billingAddress.region
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = authorizationAccessPolicyQueryService.findAccessPolicies(envelope1.unpack(FindAccessPoliciesRequest::class.java))
            commentTripLogApprovalResponse {
                this.outcome = CommentTripLogApprovalResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.tripLogId = request.tripLogId
                }
            }
        }

    private suspend fun handleEscalateTripLog(request: EscalateTripLogRequest): EscalateTripLogResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogApprovalService/EscalateTripLog") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EscalateTripLog", request)
            log.atInfo().addKeyValue("operation", "EscalateTripLog").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLog.tripLogId).log("Recorded audit detail")
            var reply0 = RejectTelemetryReadingResponse.getDefaultInstance()
            request.tripLog.linesList.asFlow().map { item ->
                val outbound0 = rejectTelemetryReadingRequest {
                    this.context = request.context
                    this.telemetryReading = telemetryReading {
                        this.attributes.putAll(item.attributesMap)
                    }
                }
                telematicsTelemetryReadingApprovalService.rejectTelemetryReading(outbound0)
            }.collect { reply0 = it }
            val outbound1 = commentTelemetryReadingApprovalRequest {
                this.context = request.context
                this.details = CommentTelemetryReadingApprovalRequestKt.details {
                    this.approver = userRef {
                        this.displayName = request.tripLog.counterparty.displayName
                    }
                }
                this.telemetryReading = telemetryReading {
                    this.subject = geoFenceRef {
                        this.displayName = reply0.telemetryReading.subject.displayName
                    }
                }
            }
            val reply1 = invokeEscalateTripLog1(outbound1)
            val outbound2 = scheduleTelemetryReadingExpiryRequest {
                this.context = request.context
                this.details = ScheduleTelemetryReadingExpiryRequestKt.details {
                    this.notify = contactInfo {
                        this.email = request.tripLog.counterparty.email
                    }
                }
                this.telemetryReading = telemetryReading {
                    this.subject = geoFenceRef {
                        this.displayName = reply0.telemetryReading.subject.displayName
                    }
                }
            }
            val invoke2: suspend (ScheduleTelemetryReadingExpiryRequest, Metadata) -> ScheduleTelemetryReadingExpiryResponse = telematicsTelemetryReadingLifecycleService::scheduleTelemetryReadingExpiry
            val reply2 = invoke2(outbound2, Metadata())
            escalateTripLogResponse {
                this.outcome = EscalateTripLogResponseKt.outcome {
                    this.effectiveAt = reply2.outcome.effectiveAt
                    this.message = reply2.result.message
                    this.nextSteps.addAll(reply1.outcome.nextStepsList)
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.total = request.tripLog.total
                    this.tripLogId = request.tripLogId
                }
            }
        }

    private suspend fun handleGetTripLogApprovalStatus(request: GetTripLogApprovalStatusRequest): GetTripLogApprovalStatusResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogApprovalService/GetTripLogApprovalStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetTripLogApprovalStatus", request)
            log.atInfo().addKeyValue("operation", "GetTripLogApprovalStatus").log("Handling transport request")
            val outbound0 = RetryWorkOrderWorkflowRequest.newBuilder().apply {
                setContext(request.context)
                setDetails(RetryWorkOrderWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.email = request.context.actor.email
                    }
                })
                setWorkOrder(workOrder {
                    this.assignee = userRef {
                        this.userId = request.context.actor.userId
                    }
                    this.audit = auditInfo {
                        this.updatedAt = request.context.deadline
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> maintenanceWorkOrderWorkflowService.retryWorkOrderWorkflow(value) }
            getTripLogApprovalStatusResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.createdAt = reply0.workOrder.audit.createdAt
                    }
                    this.tripLogId = request.tripLogId
                }
            }
        }

    private suspend fun handleRecallTripLog(request: RecallTripLogRequest): RecallTripLogResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogApprovalService/RecallTripLog") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecallTripLog", request)
            log.atInfo().addKeyValue("operation", "RecallTripLog").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLog.tripLogId).log("Recorded audit detail")
            val outbound0 = pauseFuelCardWorkflowRequest {
                this.context = request.context
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.etag = request.tripLog.audit.etag
                        this.updatedBy = request.tripLog.audit.updatedBy
                    }
                    this.holder = driverRef {
                        this.displayName = request.details.approver.displayName
                    }
                }
            }
            val reply0 = fuelFuelCardWorkflowService.pauseFuelCardWorkflow(WireCopies.reflective(outbound0))
            val outbound1 = mapTelemetryReadingImportColumnsRequest {
                this.context = request.context
                this.telemetryReading = telemetryReading {
                    this.subject = geoFenceRef {
                        this.displayName = reply0.fuelCard.holder.displayName
                    }
                }
            }
            val reply1 = workerForRecallTripLog1.submit(outbound1)
            val outbound2 = delegateTelemetryReadingApprovalRequest {
                this.context = request.context
                this.details = DelegateTelemetryReadingApprovalRequestKt.details {
                    this.approver = userRef {
                        this.email = reply0.fuelCard.holder.email
                    }
                }
            }
            val reply2 = clientForRecallTripLog2.delegateTelemetryReadingApproval(outbound2)
            recallTripLogResponse {
                this.outcome = RecallTripLogResponseKt.outcome {
                    this.effectiveAt = reply0.outcome.effectiveAt
                    this.nextSteps.addAll(reply1.outcome.nextStepsList)
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.message = reply0.outcome.message
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.billingAddress = request.tripLog.billingAddress
                    this.counterparty = customerRef {
                        this.displayName = reply2.telemetryReading.subject.displayName
                    }
                    this.tripLogId = request.tripLogId
                }
            }
        }

    private suspend fun handleRejectTripLog(request: RejectTripLogRequest): RejectTripLogResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogApprovalService/RejectTripLog") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RejectTripLog", request)
            log.atInfo().addKeyValue("operation", "RejectTripLog").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLog.tripLogId).log("Recorded audit detail")
            val outbound0 = signalFuelTransactionWorkflowRequest {
                this.context = request.context
                this.details = SignalFuelTransactionWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.email = request.tripLog.counterparty.email
                    }
                }
                this.fuelTransaction = fuelTransaction {
                    this.subject = customerRef {
                        this.displayName = request.details.approver.displayName
                        this.email = request.details.approver.email
                    }
                }
            }
            val reply0 = flowOf(outbound0).map { value -> fuelFuelTransactionWorkflowService.signalFuelTransactionWorkflow(value) }.single()
            rejectTripLogResponse {
                this.outcome = RejectTripLogResponseKt.outcome {
                    this.effectiveAt = reply0.outcome.effectiveAt
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.dueAt = request.tripLog.dueAt
                    this.tripLogId = request.tripLogId
                }
            }
        }
}
