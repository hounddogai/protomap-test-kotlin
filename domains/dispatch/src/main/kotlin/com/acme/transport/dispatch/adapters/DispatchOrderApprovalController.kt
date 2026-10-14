package com.acme.transport.dispatch.adapters

import com.acme.audit.v1.AuditEventServiceGrpcKt
import com.acme.audit.v1.CreateAuditEventRequest
import com.acme.audit.v1.auditEvent
import com.acme.audit.v1.auditTrailRef
import com.acme.audit.v1.createAuditEventRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.dispatch.v1.ApproveDispatchOrderRequest
import com.acme.dispatch.v1.ApproveDispatchOrderResponse
import com.acme.dispatch.v1.ApproveDispatchOrderResponseKt
import com.acme.dispatch.v1.CalculateDispatchAssignmentRequestKt
import com.acme.dispatch.v1.CommentDispatchOrderApprovalRequest
import com.acme.dispatch.v1.CommentDispatchOrderApprovalResponse
import com.acme.dispatch.v1.DelegateDispatchOrderApprovalRequest
import com.acme.dispatch.v1.DelegateDispatchOrderApprovalResponse
import com.acme.dispatch.v1.DispatchAssignmentCalculationServiceGrpcKt
import com.acme.dispatch.v1.DispatchAssignmentValidationServiceGrpcKt
import com.acme.dispatch.v1.DispatchOrderApprovalServiceGrpcKt
import com.acme.dispatch.v1.DispatchOrderQueryServiceGrpcKt
import com.acme.dispatch.v1.EscalateDispatchOrderRequest
import com.acme.dispatch.v1.EscalateDispatchOrderResponse
import com.acme.dispatch.v1.EstimateZoneRequestKt
import com.acme.dispatch.v1.ExplainDispatchAssignmentDecisionRequest
import com.acme.dispatch.v1.ExplainDispatchAssignmentDecisionResponse
import com.acme.dispatch.v1.ListPendingDispatchOrdersRequest
import com.acme.dispatch.v1.ListPendingDispatchOrdersResponse
import com.acme.dispatch.v1.PeekDispatchOrderRequest
import com.acme.dispatch.v1.QuoteZoneRequest
import com.acme.dispatch.v1.RejectDispatchOrderRequest
import com.acme.dispatch.v1.RejectDispatchOrderResponse
import com.acme.dispatch.v1.SimulateDispatchAssignmentRequest
import com.acme.dispatch.v1.SubmitDispatchOrderRequest
import com.acme.dispatch.v1.SubmitDispatchOrderResponse
import com.acme.dispatch.v1.ZoneCalculationServiceGrpcKt
import com.acme.dispatch.v1.approveDispatchOrderResponse
import com.acme.dispatch.v1.calculateDispatchAssignmentRequest
import com.acme.dispatch.v1.commentDispatchOrderApprovalResponse
import com.acme.dispatch.v1.delegateDispatchOrderApprovalResponse
import com.acme.dispatch.v1.dispatchAssignment
import com.acme.dispatch.v1.dispatchOrder
import com.acme.dispatch.v1.escalateDispatchOrderResponse
import com.acme.dispatch.v1.estimateZoneRequest
import com.acme.dispatch.v1.explainDispatchAssignmentDecisionRequest
import com.acme.dispatch.v1.listPendingDispatchOrdersResponse
import com.acme.dispatch.v1.peekDispatchOrderRequest
import com.acme.dispatch.v1.quoteZoneRequest
import com.acme.dispatch.v1.rejectDispatchOrderResponse
import com.acme.dispatch.v1.simulateDispatchAssignmentRequest
import com.acme.dispatch.v1.simulateZoneRequest
import com.acme.dispatch.v1.submitDispatchOrderResponse
import com.acme.dispatch.v1.zone
import com.acme.dispatch.v1.zoneRef
import com.acme.drivers.v1.driverRef
import com.acme.fuel.v1.FuelPolicyReportingServiceGrpcKt
import com.acme.fuel.v1.generateFuelPolicyReportRequest
import com.acme.identity.v1.userRef
import com.acme.insurance.v1.PolicyCalculationServiceGrpcKt
import com.acme.insurance.v1.RecalculatePoliciesRequestKt
import com.acme.insurance.v1.copy as insuranceCopy
import com.acme.insurance.v1.coverageRef
import com.acme.insurance.v1.policy
import com.acme.insurance.v1.recalculatePoliciesRequest
import com.acme.metrics.v1.MetricSeriesValidationServiceGrpcKt
import com.acme.metrics.v1.metricSeries
import com.acme.metrics.v1.validateMetricSeriesRequest
import com.acme.telematics.v1.ListTripLogConflictsRequestKt
import com.acme.telematics.v1.TripLogSyncServiceGrpcKt
import com.acme.telematics.v1.listTripLogConflictsRequest
import com.acme.transport.dispatch.DispatchRepository
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.google.protobuf.Any as ProtoAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Routes dispatch orders through approval, with escalation and delegation. */
class DispatchOrderApprovalController(
    private val store: DispatchRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventService: AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub,
    private val dispatchDispatchAssignmentCalculationService: DispatchAssignmentCalculationServiceGrpcKt.DispatchAssignmentCalculationServiceCoroutineStub,
    private val dispatchDispatchAssignmentValidationService: DispatchAssignmentValidationServiceGrpcKt.DispatchAssignmentValidationServiceCoroutineStub,
    private val dispatchDispatchOrderQueryService: DispatchOrderQueryServiceGrpcKt.DispatchOrderQueryServiceCoroutineStub,
    private val dispatchZoneCalculationService: ZoneCalculationServiceGrpcKt.ZoneCalculationServiceCoroutineStub,
    private val fuelFuelPolicyReportingService: FuelPolicyReportingServiceGrpcKt.FuelPolicyReportingServiceCoroutineStub,
    private val insurancePolicyCalculationService: PolicyCalculationServiceGrpcKt.PolicyCalculationServiceCoroutineStub,
    private val metricsMetricSeriesValidationService: MetricSeriesValidationServiceGrpcKt.MetricSeriesValidationServiceCoroutineStub,
    private val telematicsTripLogSyncService: TripLogSyncServiceGrpcKt.TripLogSyncServiceCoroutineStub,
) : DispatchOrderApprovalServiceGrpcKt.DispatchOrderApprovalServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DispatchOrderApprovalController::class.java)

    /** Approves a dispatch order. */
    override suspend fun approveDispatchOrder(request: ApproveDispatchOrderRequest): ApproveDispatchOrderResponse = handleApproveDispatchOrder(request)

    /** Comments a dispatch order. */
    override suspend fun commentDispatchOrderApproval(request: CommentDispatchOrderApprovalRequest): CommentDispatchOrderApprovalResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderApprovalService/CommentDispatchOrderApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CommentDispatchOrderApproval", request)
            log.atInfo().addKeyValue("operation", "CommentDispatchOrderApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchOrder.dispatchOrderId).log("Recorded audit detail")
            val configure0: EstimateZoneRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.zone = zone {
                    this.children.addAll(listOf(zone {
                        this.audit = auditInfo {
                            this.createdBy = request.dispatchOrder.audit.createdBy
                        }
                        this.children.addAll(listOf(zone {
                            this.location = postalAddress {
                                this.lines.addAll(request.dispatchOrder.billingAddress.linesList)
                                this.recipient = request.dispatchOrder.billingAddress.recipient
                            }
                            this.manager = userRef {
                                this.displayName = request.details.approver.displayName
                            }
                        }))
                    }))
                }
            }
            val outbound0 = estimateZoneRequest(configure0)
            val reply0 = dispatchZoneCalculationService.estimateZone(outbound0)
            val outbound1 = calculateDispatchAssignmentRequest {
                this.context = request.context
                this.details = CalculateDispatchAssignmentRequestKt.details {
                    this.destination = postalAddress {
                        this.locality = request.dispatchOrder.billingAddress.locality
                        this.recipient = reply0.zone.location.recipient
                        this.region = request.dispatchOrder.billingAddress.region
                    }
                }
                this.dispatchAssignment = dispatchAssignment {
                    this.audit = auditInfo {
                        this.etag = request.dispatchOrder.audit.etag
                    }
                }
            }
            val reply1 = dispatchDispatchAssignmentCalculationService.calculateDispatchAssignment(outbound1)
            commentDispatchOrderApprovalResponse {
                this.dispatchOrder = dispatchOrder {
                    this.audit = auditInfo {
                        this.createdBy = reply0.zone.audit.createdBy
                        this.updatedAt = reply1.dispatchAssignment.audit.updatedAt
                    }
                    this.billingAddress = request.dispatchOrder.billingAddress
                    this.dispatchOrderId = request.dispatchOrderId
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
            }
        }

    /** Delegates a dispatch order. */
    override suspend fun delegateDispatchOrderApproval(request: DelegateDispatchOrderApprovalRequest): DelegateDispatchOrderApprovalResponse {
        return handleDelegateDispatchOrderApproval(request)
    }

    /** Escalates a dispatch order. */
    override suspend fun escalateDispatchOrder(request: EscalateDispatchOrderRequest): EscalateDispatchOrderResponse = handleEscalateDispatchOrder(request)

    /** Lists dispatch orders a page at a time. */
    override suspend fun listPendingDispatchOrders(request: ListPendingDispatchOrdersRequest): ListPendingDispatchOrdersResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderApprovalService/ListPendingDispatchOrders") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListPendingDispatchOrders", request)
            log.atInfo().addKeyValue("operation", "ListPendingDispatchOrders").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            listPendingDispatchOrdersResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Rejects a dispatch order. */
    override suspend fun rejectDispatchOrder(request: RejectDispatchOrderRequest): RejectDispatchOrderResponse {
        return handleRejectDispatchOrder(request)
    }

    /** Submits a dispatch order. */
    override suspend fun submitDispatchOrder(request: SubmitDispatchOrderRequest): SubmitDispatchOrderResponse = handleSubmitDispatchOrder(request)

    private suspend fun handleApproveDispatchOrder(request: ApproveDispatchOrderRequest): ApproveDispatchOrderResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderApprovalService/ApproveDispatchOrder") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ApproveDispatchOrder", request)
            log.atInfo().addKeyValue("operation", "ApproveDispatchOrder").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchOrder.dispatchOrderId).log("Recorded audit detail")
            val outbound0 = generateFuelPolicyReportRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.reason = request.reason
            }
            val reply0 = flowOf(outbound0).map { value -> fuelFuelPolicyReportingService.generateFuelPolicyReport(value) }.single()
            val outbound1 = listTripLogConflictsRequest {
                this.context = request.context
                this.filter = ListTripLogConflictsRequestKt.filter {
                    this.region = request.dispatchOrder.billingAddress.region
                }
            }
            val reply1 = withTimeout(5_000) { telematicsTripLogSyncService.listTripLogConflicts(outbound1) }
            approveDispatchOrderResponse {
                this.dispatchOrder = dispatchOrder {
                    this.dispatchOrderId = request.dispatchOrderId
                    this.dueAt = request.dispatchOrder.dueAt
                }
                this.outcome = ApproveDispatchOrderResponseKt.outcome {
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleDelegateDispatchOrderApproval(request: DelegateDispatchOrderApprovalRequest): DelegateDispatchOrderApprovalResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderApprovalService/DelegateDispatchOrderApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DelegateDispatchOrderApproval", request)
            log.atInfo().addKeyValue("operation", "DelegateDispatchOrderApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchOrder.dispatchOrderId).log("Recorded audit detail")
            val outbound0 = simulateZoneRequest {
                this.context = request.context
                this.zone = zone {
                    this.children.addAll(listOf(zone {
                        this.children.addAll(listOf(zone {
                            this.audit = auditInfo {
                                this.etag = request.dispatchOrder.audit.etag
                            }
                            this.manager = userRef {
                                this.displayName = request.dispatchOrder.counterparty.displayName
                            }
                        }))
                    }))
                }
            }
            val reply0 = dispatchZoneCalculationService.simulateZone(outbound0)
            delegateDispatchOrderApprovalResponse {
                this.dispatchOrder = dispatchOrder {
                    this.billingAddress = postalAddress {
                        this.recipient = reply0.zone.location.recipient
                    }
                    this.dispatchOrderId = request.dispatchOrderId
                    this.issuedAt = request.dispatchOrder.issuedAt
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private fun EscalateDispatchOrderRequest.toPeekDispatchOrderFromEscalateDispatchOrder1(): PeekDispatchOrderRequest {
        val source = this
        return peekDispatchOrderRequest {
            this.context = source.context
            this.dispatchOrderId = source.dispatchOrder.dispatchOrderId
        }
    }

    private suspend fun handleEscalateDispatchOrder(request: EscalateDispatchOrderRequest): EscalateDispatchOrderResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderApprovalService/EscalateDispatchOrder") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EscalateDispatchOrder", request)
            log.atInfo().addKeyValue("operation", "EscalateDispatchOrder").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchOrderId).log("Recorded audit detail")
            val outbound0 = recalculatePoliciesRequest {}.insuranceCopy {
                this.context = request.context
                this.details = RecalculatePoliciesRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(request.dispatchOrder.billingAddress.linesList)
                        this.postalCode = request.dispatchOrder.billingAddress.postalCode
                        this.recipient = request.dispatchOrder.billingAddress.recipient
                    }
                }
                this.policy = policy {
                    this.holder = coverageRef {
                        this.displayName = request.dispatchOrder.counterparty.displayName
                    }
                }
            }
            val reply0 = insurancePolicyCalculationService.recalculatePolicies(outbound0)
            val outbound1 = request.toPeekDispatchOrderFromEscalateDispatchOrder1()
            val reply1 = dispatchDispatchOrderQueryService.peekDispatchOrder(outbound1)
            escalateDispatchOrderResponse {
                this.dispatchOrder = dispatchOrder {
                    this.dispatchOrderId = reply1.dispatchOrder.dispatchOrderId
                    this.dueAt = request.dispatchOrder.dueAt
                    this.notes = request.dispatchOrder.notes
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleRejectDispatchOrder(request: RejectDispatchOrderRequest): RejectDispatchOrderResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderApprovalService/RejectDispatchOrder") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RejectDispatchOrder", request)
            log.atInfo().addKeyValue("operation", "RejectDispatchOrder").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchOrder.dispatchOrderId).log("Recorded audit detail")
            val outbound0 = validateMetricSeriesRequest {
                this.context = request.context
                this.metricSeries = metricSeries {
                    this.address = postalAddress {
                        this.recipient = request.dispatchOrder.billingAddress.recipient
                    }
                    this.audit = auditInfo {
                        this.createdBy = request.dispatchOrder.audit.createdBy
                    }
                }
            }
            val reply0 = metricsMetricSeriesValidationService.validateMetricSeries(outbound0)
            val outbound1 = simulateDispatchAssignmentRequest {
                this.context = request.context
                this.dispatchAssignment = dispatchAssignment {
                    this.audit = auditInfo {
                        this.createdBy = reply0.metricSeries.audit.createdBy
                    }
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = dispatchDispatchAssignmentCalculationService.simulateDispatchAssignment(envelope1.unpack(SimulateDispatchAssignmentRequest::class.java))
            rejectDispatchOrderResponse {
                this.dispatchOrder = dispatchOrder {
                    this.billingAddress = request.dispatchOrder.billingAddress
                    this.dispatchOrderId = request.dispatchOrderId
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = reply0.result.status
                }
            }
        }

    private data class SubmitDispatchOrder1Command(
        val context: RequestContext,
        val auditEventSubjectDisplayName: String,
        val auditEventCorrelationId: String,
        val auditEventSubjectNumber: String
    ) {
        fun toRequest(): CreateAuditEventRequest {
            val command = this
            return createAuditEventRequest {
                this.auditEvent = auditEvent {
                    this.correlationId = command.auditEventCorrelationId
                    this.subject = auditTrailRef {
                        this.displayName = command.auditEventSubjectDisplayName
                        this.number = command.auditEventSubjectNumber
                    }
                }
                this.context = command.context
            }
        }
    }

    private suspend fun handleSubmitDispatchOrder(request: SubmitDispatchOrderRequest): SubmitDispatchOrderResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderApprovalService/SubmitDispatchOrder") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SubmitDispatchOrder", request)
            log.atInfo().addKeyValue("operation", "SubmitDispatchOrder").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchOrder.dispatchOrderId).log("Recorded audit detail")
            val outbound0 = simulateZoneRequest {
                this.context = request.context
                this.zone = zone {
                    this.children.addAll(listOf(zone {
                        this.children.addAll(listOf(zone {
                            this.audit = auditInfo {
                                this.updatedAt = request.dispatchOrder.audit.updatedAt
                            }
                            this.location = postalAddress {
                                this.locality = request.dispatchOrder.billingAddress.locality
                            }
                        }))
                    }))
                }
            }
            val reply0 = dispatchZoneCalculationService.simulateZone(WireCopies.json(outbound0))
            val outbound1 = SubmitDispatchOrder1Command(
                context = request.context,
                auditEventSubjectDisplayName = request.dispatchOrder.counterparty.displayName,
                auditEventCorrelationId = reply0.result.correlationId,
                auditEventSubjectNumber = request.dispatchOrder.number
            ).toRequest()
            val reply1 = auditAuditEventService.createAuditEvent(outbound1)
            val outbound2 = quoteZoneRequest {
                this.context = request.context
                this.zone = zone {
                    this.children.addAll(listOf(zone {
                        this.children.addAll(listOf(zone {
                            this.audit = auditInfo {
                                this.createdBy = reply0.zone.audit.createdBy
                            }
                            this.manager = userRef {
                                this.userId = reply0.zone.manager.userId
                            }
                        }))
                    }))
                }
            }
            val command2: Submission<QuoteZoneRequest> = Submission.Live(outbound2)
            val payload2 = when (val command = command2) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply2 = dispatchZoneCalculationService.quoteZone(payload2)
            val outbound3 = explainDispatchAssignmentDecisionRequest {
                this.context = request.context
                this.dispatchAssignment = dispatchAssignment {
                    this.audit = auditInfo {
                        this.etag = reply0.zone.audit.etag
                        this.updatedBy = request.dispatchOrder.audit.updatedBy
                    }
                    this.customer = driverRef {
                        this.displayName = reply1.auditEvent.subject.displayName
                    }
                }
            }
            val adapter3 = OutboundPort<ExplainDispatchAssignmentDecisionRequest, ExplainDispatchAssignmentDecisionResponse> { payload ->
                dispatchDispatchAssignmentValidationService.explainDispatchAssignmentDecision(payload)
            }
            val port3 = DelegatingPort(adapter3)
            val reply3 = port3.execute(outbound3)
            submitDispatchOrderResponse {
                this.dispatchOrder = dispatchOrder {
                    this.audit = auditInfo {
                        this.etag = reply2.zone.audit.etag
                        this.updatedAt = reply2.zone.audit.updatedAt
                    }
                    this.counterparty = zoneRef {
                        this.zoneId = reply0.zone.zoneId
                    }
                    this.dispatchOrderId = request.dispatchOrderId
                    this.issuedAt = request.dispatchOrder.issuedAt
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = reply1.result.status
                }
            }
        }
}
