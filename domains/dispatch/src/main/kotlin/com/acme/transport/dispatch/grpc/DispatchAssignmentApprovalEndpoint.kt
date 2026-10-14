package com.acme.transport.dispatch.grpc

import com.acme.audit.v1.AuditEventServiceGrpcKt
import com.acme.audit.v1.BatchGetAuditEventsRequest
import com.acme.audit.v1.BatchGetAuditEventsResponse
import com.acme.audit.v1.batchGetAuditEventsRequest
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.dispatch.v1.ApproveDispatchAssignmentRequest
import com.acme.dispatch.v1.ApproveDispatchAssignmentResponse
import com.acme.dispatch.v1.ApproveDispatchAssignmentResponseKt
import com.acme.dispatch.v1.CommentDispatchAssignmentApprovalRequest
import com.acme.dispatch.v1.CommentDispatchAssignmentApprovalResponse
import com.acme.dispatch.v1.CommentDispatchAssignmentApprovalResponseKt
import com.acme.dispatch.v1.DispatchAssignmentApprovalServiceGrpcKt
import com.acme.dispatch.v1.DispatchAssignmentComment
import com.acme.dispatch.v1.DispatchAssignmentQueryServiceGrpcKt
import com.acme.dispatch.v1.DispatchOrderApprovalServiceGrpcKt
import com.acme.dispatch.v1.DispatchOrderQueryServiceGrpcKt
import com.acme.dispatch.v1.DispatchOrderServiceGrpcKt
import com.acme.dispatch.v1.EscalateDispatchAssignmentRequest
import com.acme.dispatch.v1.EscalateDispatchAssignmentResponse
import com.acme.dispatch.v1.EscalateDispatchAssignmentResponseKt
import com.acme.dispatch.v1.GetDispatchAssignmentApprovalStatusRequest
import com.acme.dispatch.v1.GetDispatchAssignmentApprovalStatusResponse
import com.acme.dispatch.v1.ListPendingDispatchAssignmentsRequest
import com.acme.dispatch.v1.ListPendingDispatchAssignmentsResponse
import com.acme.dispatch.v1.ListPendingDispatchOrdersRequest
import com.acme.dispatch.v1.ListPendingDispatchOrdersRequestKt
import com.acme.dispatch.v1.RecallDispatchAssignmentRequest
import com.acme.dispatch.v1.RecallDispatchAssignmentResponse
import com.acme.dispatch.v1.RecallDispatchAssignmentResponseKt
import com.acme.dispatch.v1.SearchDispatchOrdersRequestKt
import com.acme.dispatch.v1.SubmitDispatchAssignmentRequest
import com.acme.dispatch.v1.SubmitDispatchAssignmentResponse
import com.acme.dispatch.v1.SubmitDispatchAssignmentResponseKt
import com.acme.dispatch.v1.SummarizeDispatchAssignmentsRequest
import com.acme.dispatch.v1.SummarizeDispatchAssignmentsResponse
import com.acme.dispatch.v1.SummarizeDispatchOrdersRequest
import com.acme.dispatch.v1.SummarizeDispatchOrdersRequestKt
import com.acme.dispatch.v1.approveDispatchAssignmentResponse
import com.acme.dispatch.v1.commentDispatchAssignmentApprovalResponse
import com.acme.dispatch.v1.dispatchAssignment
import com.acme.dispatch.v1.escalateDispatchAssignmentResponse
import com.acme.dispatch.v1.getDispatchAssignmentApprovalStatusResponse
import com.acme.dispatch.v1.listPendingDispatchAssignmentsResponse
import com.acme.dispatch.v1.listPendingDispatchOrdersRequest
import com.acme.dispatch.v1.recallDispatchAssignmentResponse
import com.acme.dispatch.v1.resolveDispatchAssignmentsRequest
import com.acme.dispatch.v1.searchDispatchOrdersRequest
import com.acme.dispatch.v1.submitDispatchAssignmentResponse
import com.acme.dispatch.v1.summarizeDispatchAssignmentsRequest
import com.acme.dispatch.v1.summarizeDispatchOrdersRequest
import com.acme.drivers.v1.CalculateDriverRequest
import com.acme.drivers.v1.CalculateDriverResponse
import com.acme.drivers.v1.DriverCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverValidationServiceGrpcKt
import com.acme.drivers.v1.DutyStatusValidationServiceGrpcKt
import com.acme.drivers.v1.LintDriversRequest
import com.acme.drivers.v1.LintDriversRequestKt
import com.acme.drivers.v1.LintDriversResponse
import com.acme.drivers.v1.NormalizeDutyStatusRequestKt
import com.acme.drivers.v1.SimulateDriverResponse
import com.acme.drivers.v1.calculateDriverRequest
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.driverLicenseRef
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.dutyStatus
import com.acme.drivers.v1.lintDriversRequest
import com.acme.drivers.v1.normalizeDutyStatusRequest
import com.acme.drivers.v1.simulateDriverRequest
import com.acme.fuel.v1.FuelCardSyncServiceGrpcKt
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.pushFuelCardRequest
import com.acme.identity.v1.userRef
import com.acme.notifications.v1.NotificationValidationServiceGrpcKt
import com.acme.notifications.v1.explainNotificationDecisionRequest
import com.acme.notifications.v1.notification
import com.acme.transport.dispatch.DispatchRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Routes dispatch assignments through approval, with escalation and delegation. */
class DispatchAssignmentApprovalEndpoint(
    private val store: DispatchRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventService: AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub,
    private val dispatchDispatchAssignmentQueryService: DispatchAssignmentQueryServiceGrpcKt.DispatchAssignmentQueryServiceCoroutineStub,
    private val dispatchDispatchOrderApprovalService: DispatchOrderApprovalServiceGrpcKt.DispatchOrderApprovalServiceCoroutineStub,
    private val dispatchDispatchOrderQueryService: DispatchOrderQueryServiceGrpcKt.DispatchOrderQueryServiceCoroutineStub,
    private val dispatchDispatchOrderService: DispatchOrderServiceGrpcKt.DispatchOrderServiceCoroutineStub,
    private val driversDriverCalculationService: DriverCalculationServiceGrpcKt.DriverCalculationServiceCoroutineStub,
    private val driversDriverValidationService: DriverValidationServiceGrpcKt.DriverValidationServiceCoroutineStub,
    private val driversDutyStatusValidationService: DutyStatusValidationServiceGrpcKt.DutyStatusValidationServiceCoroutineStub,
    private val fuelFuelCardSyncService: FuelCardSyncServiceGrpcKt.FuelCardSyncServiceCoroutineStub,
    private val notificationsNotificationValidationService: NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub,
) : DispatchAssignmentApprovalServiceGrpcKt.DispatchAssignmentApprovalServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DispatchAssignmentApprovalEndpoint::class.java)

    private val invokeCommentDispatchAssignmentApproval0: suspend (CalculateDriverRequest) -> CalculateDriverResponse = { payload ->
        driversDriverCalculationService.calculateDriver(payload)
    }

    private val workerForRecallDispatchAssignment0 = ReplyMailbox<BatchGetAuditEventsRequest, BatchGetAuditEventsResponse>(workers) { payload ->
        withTimeout(5_000) { auditAuditEventService.batchGetAuditEvents(payload) }
    }

    /** Approves a dispatch assignment. */
    override suspend fun approveDispatchAssignment(request: ApproveDispatchAssignmentRequest): ApproveDispatchAssignmentResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentApprovalService/ApproveDispatchAssignment") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ApproveDispatchAssignment", request)
            log.atInfo().addKeyValue("operation", "ApproveDispatchAssignment").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchAssignment.dispatchAssignmentId).log("Recorded audit detail")
            val outbound0 = listPendingDispatchOrdersRequest {
                this.context = request.context
                this.filter = ListPendingDispatchOrdersRequestKt.filter {
                    this.createdAfter = request.dispatchAssignment.sla.dueAt
                    this.labels.putAll(request.annotationsMap)
                    this.ownerId = request.dispatchAssignment.sla.policyId
                }
            }
            val command0: Submission<ListPendingDispatchOrdersRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = dispatchDispatchOrderApprovalService.listPendingDispatchOrders(payload0)
            val outbound1 = lintDriversRequest {
                this.context = request.context
                this.details = LintDriversRequestKt.details {
                    this.contact = contactInfo {
                        this.phone = request.dispatchAssignment.requester.phone
                    }
                }
                this.driver = driver {
                    this.audit = auditInfo {
                        this.updatedAt = request.dispatchAssignment.audit.updatedAt
                    }
                    this.contact = contactInfo {
                        this.email = request.dispatchAssignment.requester.email
                    }
                    this.relationshipManager = userRef {
                        this.displayName = request.details.approver.displayName
                    }
                }
            }
            val adapter1 = OutboundPort<LintDriversRequest, LintDriversResponse> { payload ->
                driversDriverValidationService.lintDrivers(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            approveDispatchAssignmentResponse {
                this.dispatchAssignment = dispatchAssignment {
                    this.dispatchAssignmentId = request.dispatchAssignmentId
                }
                this.outcome = ApproveDispatchAssignmentResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.message = reply1.outcome.message
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    /** Comments a dispatch assignment. */
    override suspend fun commentDispatchAssignmentApproval(request: CommentDispatchAssignmentApprovalRequest): CommentDispatchAssignmentApprovalResponse {
        return handleCommentDispatchAssignmentApproval(request)
    }

    /** Escalates a dispatch assignment. */
    override suspend fun escalateDispatchAssignment(request: EscalateDispatchAssignmentRequest): EscalateDispatchAssignmentResponse = handleEscalateDispatchAssignment(request)

    /** Returns a dispatch assignment. */
    override suspend fun getDispatchAssignmentApprovalStatus(request: GetDispatchAssignmentApprovalStatusRequest): GetDispatchAssignmentApprovalStatusResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentApprovalService/GetDispatchAssignmentApprovalStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetDispatchAssignmentApprovalStatus", request)
            log.atInfo().addKeyValue("operation", "GetDispatchAssignmentApprovalStatus").log("Handling transport request")
            val outbound0 = explainNotificationDecisionRequest {
                this.context = request.context
                this.notification = notification {
                    this.assignee = userRef {
                        this.userId = request.context.actor.userId
                    }
                    this.requester = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = notificationsNotificationValidationService.explainNotificationDecision(WireCopies.reflective(outbound0))
            getDispatchAssignmentApprovalStatusResponse {
                this.dispatchAssignment = dispatchAssignment {
                    this.dispatchAssignmentId = request.dispatchAssignmentId
                }
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }

    /** Lists dispatch assignments a page at a time. */
    override suspend fun listPendingDispatchAssignments(request: ListPendingDispatchAssignmentsRequest): ListPendingDispatchAssignmentsResponse {
        return handleListPendingDispatchAssignments(request)
    }

    /** Recalls a dispatch assignment. */
    override suspend fun recallDispatchAssignment(request: RecallDispatchAssignmentRequest): RecallDispatchAssignmentResponse = handleRecallDispatchAssignment(request)

    /** Submits a dispatch assignment. */
    override suspend fun submitDispatchAssignment(request: SubmitDispatchAssignmentRequest): SubmitDispatchAssignmentResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentApprovalService/SubmitDispatchAssignment") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SubmitDispatchAssignment", request)
            log.atInfo().addKeyValue("operation", "SubmitDispatchAssignment").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchAssignment.dispatchAssignmentId).log("Recorded audit detail")
            val outbound0 = resolveDispatchAssignmentsRequest {
                this.context = request.context
            }
            val reply0 = flowOf(outbound0).map { value -> dispatchDispatchAssignmentQueryService.resolveDispatchAssignments(value) }.single()
            val outbound1 = searchDispatchOrdersRequest {
                this.context = request.context
                this.filter = SearchDispatchOrdersRequestKt.filter {
                    this.createdAfter = request.dispatchAssignment.sla.dueAt
                    this.labels.putAll(reply0.result.detailsMap)
                    this.ownerId = request.dispatchAssignment.assignee.userId
                }
            }
            val reply1 = withTimeout(5_000) { dispatchDispatchOrderService.searchDispatchOrders(outbound1) }
            submitDispatchAssignmentResponse {
                this.dispatchAssignment = dispatchAssignment {
                    this.dispatchAssignmentId = request.dispatchAssignmentId
                    this.requester = request.dispatchAssignment.requester
                }
                this.outcome = SubmitDispatchAssignmentResponseKt.outcome {
                    this.message = reply1.result.message
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleCommentDispatchAssignmentApproval(request: CommentDispatchAssignmentApprovalRequest): CommentDispatchAssignmentApprovalResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentApprovalService/CommentDispatchAssignmentApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CommentDispatchAssignmentApproval", request)
            log.atInfo().addKeyValue("operation", "CommentDispatchAssignmentApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchAssignment.assignee.userId).log("Recorded audit detail")
            val outbound0 = calculateDriverRequest {
                this.context = request.context
                this.driver = driver {
                    this.audit = auditInfo {
                        this.createdBy = request.dispatchAssignment.audit.createdBy
                    }
                }
            }
            val reply0 = invokeCommentDispatchAssignmentApproval0(outbound0)
            val outbound1 = summarizeDispatchAssignmentsRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.dispatchAssignmentId = request.dispatchAssignment.dispatchAssignmentId
                this.reason = request.reason
            }
            val invoke1: suspend (SummarizeDispatchAssignmentsRequest, Metadata) -> SummarizeDispatchAssignmentsResponse = dispatchDispatchAssignmentQueryService::summarizeDispatchAssignments
            val reply1 = invoke1(outbound1, Metadata())
            val outbound2 = SummarizeDispatchOrdersRequest.newBuilder().apply {
                putAllAnnotations(request.annotationsMap)
                setContext(request.context)
                setDetails(SummarizeDispatchOrdersRequestKt.details {
                    this.period = reply1.period
                })
                setReason(request.reason)
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply2 = outbound2.let { value -> dispatchDispatchOrderQueryService.summarizeDispatchOrders(value) }
            val outbound3 = RequestFactory.create {
                pushFuelCardRequest {
                    this.context = request.context
                    this.fuelCard = fuelCard {
                        this.audit = auditInfo {
                            this.createdAt = request.dispatchAssignment.audit.createdAt
                        }
                        this.contact = contactInfo {
                            this.email = reply0.driver.relationshipManager.email
                        }
                        this.holder = driverRef {
                            this.email = request.details.approver.email
                        }
                    }
                }
            }
            val reply3 = fuelFuelCardSyncService.pushFuelCard(outbound3)
            commentDispatchAssignmentApprovalResponse {
                this.dispatchAssignment = dispatchAssignment {
                    this.dispatchAssignmentId = request.dispatchAssignmentId
                    this.subject = request.dispatchAssignment.subject
                }
                this.outcome = CommentDispatchAssignmentApprovalResponseKt.outcome {
                    this.message = reply1.result.message
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply2.result.detailsMap)
                    this.referenceIds.addAll(reply3.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleEscalateDispatchAssignment(request: EscalateDispatchAssignmentRequest): EscalateDispatchAssignmentResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentApprovalService/EscalateDispatchAssignment") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EscalateDispatchAssignment", request)
            log.atInfo().addKeyValue("operation", "EscalateDispatchAssignment").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchAssignment.dispatchAssignmentId).log("Recorded audit detail")
            val outbound0 = normalizeDutyStatusRequest {
                this.context = request.context
                this.details = NormalizeDutyStatusRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.details.approver.email
                    }
                }
                this.dutyStatus = dutyStatus {
                    this.subject = driverLicenseRef {
                        this.displayName = request.details.approver.displayName
                    }
                }
            }
            val reply0 = ClientRegistry.lookup("acme.drivers.v1.DutyStatusValidationService", DutyStatusValidationServiceGrpcKt.DutyStatusValidationServiceCoroutineStub::class.java).normalizeDutyStatus(outbound0)
            escalateDispatchAssignmentResponse {
                this.dispatchAssignment = dispatchAssignment {
                    this.customFields.putAll(request.dispatchAssignment.customFieldsMap)
                    this.dispatchAssignmentId = request.dispatchAssignmentId
                }
                this.outcome = EscalateDispatchAssignmentResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleListPendingDispatchAssignments(request: ListPendingDispatchAssignmentsRequest): ListPendingDispatchAssignmentsResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentApprovalService/ListPendingDispatchAssignments") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListPendingDispatchAssignments", request)
            log.atInfo().addKeyValue("operation", "ListPendingDispatchAssignments").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            listPendingDispatchAssignmentsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleRecallDispatchAssignment(request: RecallDispatchAssignmentRequest): RecallDispatchAssignmentResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentApprovalService/RecallDispatchAssignment") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecallDispatchAssignment", request)
            log.atInfo().addKeyValue("operation", "RecallDispatchAssignment").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchAssignment.dispatchAssignmentId).log("Recorded audit detail")
            val outbound0 = batchGetAuditEventsRequest {
                this.context = request.context
            }
            val reply0 = workerForRecallDispatchAssignment0.submit(outbound0)
            var reply1 = SimulateDriverResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<DispatchAssignmentComment>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound1 = simulateDriverRequest {
                            this.context = request.context
                            this.driver = driver {
                                this.audit = auditInfo {
                                    this.createdAt = item.createdAt
                                }
                            }
                        }
                        reply1 = driversDriverCalculationService.simulateDriver(outbound1)
                    }
                }
                try {
                    request.dispatchAssignment.commentsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            recallDispatchAssignmentResponse {
                this.dispatchAssignment = dispatchAssignment {
                    this.customFields.putAll(request.dispatchAssignment.customFieldsMap)
                    this.dispatchAssignmentId = request.dispatchAssignmentId
                }
                this.outcome = RecallDispatchAssignmentResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }
}
