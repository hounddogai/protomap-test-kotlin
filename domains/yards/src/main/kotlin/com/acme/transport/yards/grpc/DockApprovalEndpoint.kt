package com.acme.transport.yards.grpc

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.customerRef
import com.acme.deliveries.v1.BatchGetDeliveriesRequestKt
import com.acme.deliveries.v1.DeliveryServiceGrpcKt
import com.acme.deliveries.v1.batchGetDeliveriesRequest
import com.acme.drivers.v1.driverRef
import com.acme.fleet.v1.CompareInspectionOptionsRequestKt
import com.acme.fleet.v1.InspectionCalculationServiceGrpcKt
import com.acme.fleet.v1.compareInspectionOptionsRequest
import com.acme.fleet.v1.explainInspectionCalculationRequest
import com.acme.fleet.v1.inspection
import com.acme.fleet.v1.vehicleGroupRef
import com.acme.identity.v1.NormalizeUserRequest
import com.acme.identity.v1.UserValidationServiceGrpcKt
import com.acme.identity.v1.normalizeUserRequest
import com.acme.identity.v1.user
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.ExplainWorkOrderDecisionRequest
import com.acme.maintenance.v1.ExplainWorkOrderDecisionRequestKt
import com.acme.maintenance.v1.PreviewWorkOrderRequestKt
import com.acme.maintenance.v1.WorkOrderServiceGrpcKt
import com.acme.maintenance.v1.WorkOrderValidationServiceGrpcKt
import com.acme.maintenance.v1.batchGetWorkOrdersRequest
import com.acme.maintenance.v1.copy as maintenanceCopy
import com.acme.maintenance.v1.createWorkOrderRequest
import com.acme.maintenance.v1.explainWorkOrderDecisionRequest
import com.acme.maintenance.v1.previewWorkOrderRequest
import com.acme.maintenance.v1.workOrder
import com.acme.metrics.v1.MetricSeriesServiceGrpcKt
import com.acme.metrics.v1.SearchMetricSeriesesRequest
import com.acme.metrics.v1.SearchMetricSeriesesRequestKt
import com.acme.metrics.v1.searchMetricSeriesesRequest
import com.acme.routing.v1.RouteServiceGrpcKt
import com.acme.routing.v1.SearchRoutesRequest
import com.acme.routing.v1.SearchRoutesRequestKt
import com.acme.routing.v1.SearchRoutesResponse
import com.acme.routing.v1.searchRoutesRequest
import com.acme.telematics.v1.TripLogValidationServiceGrpcKt
import com.acme.telematics.v1.tripLog
import com.acme.telematics.v1.validateTripLogRequest
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.transport.yards.YardsRepository
import com.acme.yards.v1.ApproveDockRequest
import com.acme.yards.v1.ApproveDockResponse
import com.acme.yards.v1.ApproveDockResponseKt
import com.acme.yards.v1.CommentDockApprovalRequest
import com.acme.yards.v1.CommentDockApprovalResponse
import com.acme.yards.v1.CommentDockApprovalResponseKt
import com.acme.yards.v1.DelegateDockApprovalRequest
import com.acme.yards.v1.DelegateDockApprovalResponse
import com.acme.yards.v1.DelegateDockApprovalResponseKt
import com.acme.yards.v1.DockApprovalServiceGrpcKt
import com.acme.yards.v1.EscalateDockRequest
import com.acme.yards.v1.EscalateDockResponse
import com.acme.yards.v1.EscalateDockResponseKt
import com.acme.yards.v1.GenerateYardMoveReportRequest
import com.acme.yards.v1.GetDockApprovalStatusRequest
import com.acme.yards.v1.GetDockApprovalStatusResponse
import com.acme.yards.v1.ListPendingDocksRequest
import com.acme.yards.v1.ListPendingDocksResponse
import com.acme.yards.v1.ListYardMoveReportsRequestKt
import com.acme.yards.v1.NormalizeYardRequest
import com.acme.yards.v1.NormalizeYardRequestKt
import com.acme.yards.v1.NormalizeYardResponse
import com.acme.yards.v1.QuoteYardRequest
import com.acme.yards.v1.RecallDockRequest
import com.acme.yards.v1.RecallDockResponse
import com.acme.yards.v1.RecallDockResponseKt
import com.acme.yards.v1.RejectDockRequest
import com.acme.yards.v1.RejectDockResponse
import com.acme.yards.v1.RejectDockResponseKt
import com.acme.yards.v1.ShareYardMoveReportRequestKt
import com.acme.yards.v1.StreamYardMoveMetricsRequestKt
import com.acme.yards.v1.SubmitDockRequest
import com.acme.yards.v1.SubmitDockResponse
import com.acme.yards.v1.SubmitDockResponseKt
import com.acme.yards.v1.YardCalculationServiceGrpcKt
import com.acme.yards.v1.YardComponent
import com.acme.yards.v1.YardMoveReportingServiceGrpcKt
import com.acme.yards.v1.YardValidationServiceGrpcKt
import com.acme.yards.v1.approveDockResponse
import com.acme.yards.v1.commentDockApprovalResponse
import com.acme.yards.v1.delegateDockApprovalResponse
import com.acme.yards.v1.dock
import com.acme.yards.v1.dockComponent
import com.acme.yards.v1.escalateDockResponse
import com.acme.yards.v1.generateYardMoveReportRequest
import com.acme.yards.v1.getDockApprovalStatusResponse
import com.acme.yards.v1.listPendingDocksResponse
import com.acme.yards.v1.listYardMoveReportsRequest
import com.acme.yards.v1.normalizeYardRequest
import com.acme.yards.v1.quoteYardRequest
import com.acme.yards.v1.recallDockResponse
import com.acme.yards.v1.rejectDockResponse
import com.acme.yards.v1.shareYardMoveReportRequest
import com.acme.yards.v1.streamYardMoveMetricsRequest
import com.acme.yards.v1.submitDockResponse
import com.acme.yards.v1.yard
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Routes docks through approval, with escalation and delegation. */
class DockApprovalEndpoint(
    private val store: YardsRepository,
    private val workers: CoroutineScope,
    private val deliveriesDeliveryService: DeliveryServiceGrpcKt.DeliveryServiceCoroutineStub,
    private val fleetInspectionCalculationService: InspectionCalculationServiceGrpcKt.InspectionCalculationServiceCoroutineStub,
    private val identityUserValidationService: UserValidationServiceGrpcKt.UserValidationServiceCoroutineStub,
    private val maintenanceWorkOrderService: WorkOrderServiceGrpcKt.WorkOrderServiceCoroutineStub,
    private val maintenanceWorkOrderValidationService: WorkOrderValidationServiceGrpcKt.WorkOrderValidationServiceCoroutineStub,
    private val metricsMetricSeriesService: MetricSeriesServiceGrpcKt.MetricSeriesServiceCoroutineStub,
    private val routingRouteService: RouteServiceGrpcKt.RouteServiceCoroutineStub,
    private val telematicsTripLogValidationService: TripLogValidationServiceGrpcKt.TripLogValidationServiceCoroutineStub,
    private val yardsYardCalculationService: YardCalculationServiceGrpcKt.YardCalculationServiceCoroutineStub,
    private val yardsYardMoveReportingService: YardMoveReportingServiceGrpcKt.YardMoveReportingServiceCoroutineStub,
    private val yardsYardValidationService: YardValidationServiceGrpcKt.YardValidationServiceCoroutineStub,
) : DockApprovalServiceGrpcKt.DockApprovalServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DockApprovalEndpoint::class.java)

    /** Approves a dock. */
    override suspend fun approveDock(request: ApproveDockRequest): ApproveDockResponse =
        RpcBoundary.run("acme.yards.v1.DockApprovalService/ApproveDock") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ApproveDock", request)
            log.atInfo().addKeyValue("operation", "ApproveDock").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dock.dockId).log("Recorded audit detail")
            val outbound0 = listYardMoveReportsRequest {
                this.context = request.context
                this.filter = ListYardMoveReportsRequestKt.filter {
                    this.createdAfter = request.details.effectiveAt
                    this.labels.putAll(request.dock.specsMap)
                    this.ownerId = request.dock.owner.userId
                    this.region = request.dock.address.region
                }
            }
            val reply0 = withTimeout(5_000) { yardsYardMoveReportingService.listYardMoveReports(outbound0) }
            val configure1: BatchGetDeliveriesRequestKt.Dsl.() -> Unit = {
                this.context = request.context
            }
            val outbound1 = batchGetDeliveriesRequest(configure1)
            val reply1 = deliveriesDeliveryService.batchGetDeliveries(outbound1)
            approveDockResponse {
                this.dock = dock {
                    this.dockId = request.dockId
                    this.specs.putAll(request.dock.specsMap)
                }
                this.outcome = ApproveDockResponseKt.outcome {
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = reply0.result.status
                }
            }
        }

    /** Comments a dock. */
    override suspend fun commentDockApproval(request: CommentDockApprovalRequest): CommentDockApprovalResponse {
        return handleCommentDockApproval(request)
    }

    /** Delegates a dock. */
    override suspend fun delegateDockApproval(request: DelegateDockApprovalRequest): DelegateDockApprovalResponse = handleDelegateDockApproval(request)

    /** Escalates a dock. */
    override suspend fun escalateDock(request: EscalateDockRequest): EscalateDockResponse =
        RpcBoundary.run("acme.yards.v1.DockApprovalService/EscalateDock") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EscalateDock", request)
            log.atInfo().addKeyValue("operation", "EscalateDock").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dock.dockId).log("Recorded audit detail")
            val outbound0 = batchGetWorkOrdersRequest {
                this.context = request.context
            }
            val reply0 = maintenanceWorkOrderService.batchGetWorkOrders(outbound0)
            val outbound1 = searchMetricSeriesesRequest {
                this.context = request.context
                this.filter = SearchMetricSeriesesRequestKt.filter {
                    this.region = request.dock.address.region
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = metricsMetricSeriesService.searchMetricSerieses(envelope1.unpack(SearchMetricSeriesesRequest::class.java))
            escalateDockResponse {
                this.dock = dock {
                    this.dockId = request.dockId
                }
                this.outcome = EscalateDockResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    /** Returns a dock. */
    override suspend fun getDockApprovalStatus(request: GetDockApprovalStatusRequest): GetDockApprovalStatusResponse {
        return handleGetDockApprovalStatus(request)
    }

    /** Lists docks a page at a time. */
    override suspend fun listPendingDocks(request: ListPendingDocksRequest): ListPendingDocksResponse = handleListPendingDocks(request)

    /** Recalls a dock. */
    override suspend fun recallDock(request: RecallDockRequest): RecallDockResponse =
        RpcBoundary.run("acme.yards.v1.DockApprovalService/RecallDock") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecallDock", request)
            log.atInfo().addKeyValue("operation", "RecallDock").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dock.dockId).log("Recorded audit detail")
            val outbound0 = RecallDock0Command(
                context = request.context,
                annotations = request.annotationsMap,
                reason = request.reason
            ).toRequest()
            val reply0 = yardsYardMoveReportingService.generateYardMoveReport(outbound0)
            val outbound1 = explainWorkOrderDecisionRequest {
                this.context = request.context
                this.details = ExplainWorkOrderDecisionRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.details.approver.email
                    }
                }
                this.workOrder = workOrder {
                    this.assignee = userRef {
                        this.email = request.dock.owner.email
                    }
                    this.audit = auditInfo {
                        this.createdAt = request.dock.audit.createdAt
                    }
                    this.customer = driverRef {
                        this.displayName = request.details.approver.displayName
                    }
                }
            }
            val command1: Submission<ExplainWorkOrderDecisionRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = maintenanceWorkOrderValidationService.explainWorkOrderDecision(payload1)
            val outbound2 = normalizeYardRequest {
                this.context = request.context
                this.details = NormalizeYardRequestKt.details {
                    this.contact = contactInfo {
                        this.email = reply1.workOrder.requester.email
                    }
                }
                this.yard = yard {
                    this.address = postalAddress {
                        this.recipient = request.dock.address.recipient
                    }
                    this.owner = userRef {
                        this.userId = request.dock.owner.userId
                    }
                }
            }
            val adapter2 = OutboundPort<NormalizeYardRequest, NormalizeYardResponse> { payload ->
                yardsYardValidationService.normalizeYard(payload)
            }
            val port2 = DelegatingPort(adapter2)
            val reply2 = port2.execute(outbound2)
            val outbound3 = streamYardMoveMetricsRequest {
                this.context = request.context
                this.filter = StreamYardMoveMetricsRequestKt.filter {
                    this.region = reply2.yard.address.region
                }
                this.subscriber = contactInfo {
                    this.email = request.details.approver.email
                    this.name = reply1.workOrder.requester.name
                    this.preferredChannel = reply1.workOrder.requester.preferredChannel
                }
            }
            yardsYardMoveReportingService.streamYardMoveMetrics(outbound3).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            recallDockResponse {
                this.dock = dock {
                    this.dockId = request.dockId
                    this.owner = userRef {
                        this.displayName = reply1.workOrder.assignee.displayName
                    }
                }
                this.outcome = RecallDockResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.referenceId = reply2.outcome.referenceId
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.message = reply0.result.message
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    /** Rejects a dock. */
    override suspend fun rejectDock(request: RejectDockRequest): RejectDockResponse {
        return handleRejectDock(request)
    }

    /** Submits a dock. */
    override suspend fun submitDock(request: SubmitDockRequest): SubmitDockResponse = handleSubmitDock(request)

    private suspend fun handleCommentDockApproval(request: CommentDockApprovalRequest): CommentDockApprovalResponse =
        RpcBoundary.run("acme.yards.v1.DockApprovalService/CommentDockApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CommentDockApproval", request)
            log.atInfo().addKeyValue("operation", "CommentDockApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dock.dockId).log("Recorded audit detail")
            val outbound0 = validateTripLogRequest {
                this.context = request.context
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.updatedBy = request.dock.audit.updatedBy
                    }
                    this.billingAddress = postalAddress {
                        this.countryCode = request.dock.address.countryCode
                        this.recipient = request.dock.address.recipient
                    }
                    this.counterparty = customerRef {
                        this.email = request.dock.owner.email
                    }
                }
            }
            val reply0 = telematicsTripLogValidationService.validateTripLog(outbound0)
            val outbound1 = explainInspectionCalculationRequest {
                this.context = request.context
                this.inspection = inspection {
                    this.assignee = userRef {
                        this.userId = request.details.approver.userId
                    }
                    this.audit = auditInfo {
                        this.updatedAt = request.dock.audit.updatedAt
                    }
                    this.customer = vehicleGroupRef {
                        this.displayName = request.details.approver.displayName
                    }
                    this.requester = contactInfo {
                        this.email = request.details.approver.email
                    }
                }
            }
            val reply1 = fleetInspectionCalculationService.explainInspectionCalculation(outbound1)
            val outbound2 = previewWorkOrderRequest {}.maintenanceCopy {
                this.context = request.context
                this.details = PreviewWorkOrderRequestKt.details {
                    this.contact = contactInfo {
                        this.email = reply0.tripLog.counterparty.email
                    }
                }
                this.workOrder = workOrder {
                    this.assignee = userRef {
                        this.userId = request.dock.owner.userId
                    }
                    this.audit = auditInfo {
                        this.updatedAt = request.dock.audit.updatedAt
                    }
                }
            }
            val reply2 = maintenanceWorkOrderValidationService.previewWorkOrder(outbound2)
            commentDockApprovalResponse {
                this.dock = dock {
                    this.capacity = request.dock.capacity
                    this.dockId = request.dockId
                    this.owner = userRef {
                        this.displayName = reply0.tripLog.counterparty.displayName
                    }
                }
                this.outcome = CommentDockApprovalResponseKt.outcome {
                    this.referenceId = reply2.outcome.referenceId
                }
                this.result = operationResult {
                    this.message = reply1.outcome.message
                    this.status = reply0.result.status
                }
            }
        }

    private fun DelegateDockApprovalRequest.toQuoteYardFromDelegateDockApproval0(): QuoteYardRequest {
        val source = this
        return quoteYardRequest {
            this.context = source.context
            this.yard = yard {
                this.address = postalAddress {
                    this.postalCode = source.dock.address.postalCode
                }
            }
        }
    }

    private suspend fun handleDelegateDockApproval(request: DelegateDockApprovalRequest): DelegateDockApprovalResponse =
        RpcBoundary.run("acme.yards.v1.DockApprovalService/DelegateDockApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DelegateDockApproval", request)
            log.atInfo().addKeyValue("operation", "DelegateDockApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dock.dockId).log("Recorded audit detail")
            val outbound0 = request.toQuoteYardFromDelegateDockApproval0()
            val reply0 = yardsYardCalculationService.quoteYard(outbound0)
            delegateDockApprovalResponse {
                this.dock = dock {
                    this.code = request.dock.code
                    this.components.putAll(mapOf("primary" to dockComponent {
                        this.componentId = (reply0.yard.componentsMap["primary"] ?: YardComponent.getDefaultInstance()).componentId
                    }))
                    this.dockId = request.dockId
                }
                this.outcome = DelegateDockApprovalResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleGetDockApprovalStatus(request: GetDockApprovalStatusRequest): GetDockApprovalStatusResponse =
        RpcBoundary.run("acme.yards.v1.DockApprovalService/GetDockApprovalStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetDockApprovalStatus", request)
            log.atInfo().addKeyValue("operation", "GetDockApprovalStatus").log("Handling transport request")
            val outbound0 = shareYardMoveReportRequest {
                this.context = request.context
                this.details = ShareYardMoveReportRequestKt.details {
                    this.recipient = contactInfo {
                        this.email = request.context.actor.email
                    }
                    this.variables.putAll(request.context.baggageMap)
                }
            }
            val reply0 = yardsYardMoveReportingService.shareYardMoveReport(WireCopies.json(outbound0))
            getDockApprovalStatusResponse {
                this.dock = dock {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.yardMove.audit.updatedAt
                    }
                    this.dockId = request.dockId
                }
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleListPendingDocks(request: ListPendingDocksRequest): ListPendingDocksResponse =
        RpcBoundary.run("acme.yards.v1.DockApprovalService/ListPendingDocks") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListPendingDocks", request)
            log.atInfo().addKeyValue("operation", "ListPendingDocks").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            listPendingDocksResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private data class RecallDock0Command(
        val context: RequestContext,
        val annotations: Map<String, String>,
        val reason: String
    ) {
        fun toRequest(): GenerateYardMoveReportRequest {
            val command = this
            return generateYardMoveReportRequest {
                this.annotations.putAll(command.annotations)
                this.context = command.context
                this.reason = command.reason
            }
        }
    }

    private suspend fun handleRejectDock(request: RejectDockRequest): RejectDockResponse =
        RpcBoundary.run("acme.yards.v1.DockApprovalService/RejectDock") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RejectDock", request)
            log.atInfo().addKeyValue("operation", "RejectDock").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dock.dockId).log("Recorded audit detail")
            val outbound0 = searchRoutesRequest {
                this.context = request.context
                this.filter = SearchRoutesRequestKt.filter {
                    this.createdAfter = request.dock.audit.updatedAt
                    this.ownerId = request.details.approver.userId
                    this.region = request.dock.address.region
                }
            }
            val invoke0: suspend (SearchRoutesRequest, Metadata) -> SearchRoutesResponse = routingRouteService::searchRoutes
            val reply0 = invoke0(outbound0, Metadata())
            rejectDockResponse {
                this.dock = dock {
                    this.dockId = request.dockId
                }
                this.outcome = RejectDockResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleSubmitDock(request: SubmitDockRequest): SubmitDockResponse =
        RpcBoundary.run("acme.yards.v1.DockApprovalService/SubmitDock") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SubmitDock", request)
            log.atInfo().addKeyValue("operation", "SubmitDock").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dock.dockId).log("Recorded audit detail")
            val outbound0 = NormalizeUserRequest.newBuilder().apply {
                setContext(request.context)
                setUser(user {
                    this.audit = auditInfo {
                        this.createdBy = request.dock.audit.createdBy
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> identityUserValidationService.normalizeUser(value) }
            val outbound1 = RequestFactory.create {
                compareInspectionOptionsRequest {
                    this.context = request.context
                    this.details = CompareInspectionOptionsRequestKt.details {
                        this.destination = postalAddress {
                            this.lines.addAll(reply0.user.primaryAddress.linesList)
                            this.locality = reply0.user.primaryAddress.locality
                        }
                    }
                    this.inspection = inspection {
                        this.audit = auditInfo {
                            this.createdBy = reply0.user.audit.createdBy
                            this.etag = request.dock.audit.etag
                        }
                    }
                }
            }
            val reply1 = fleetInspectionCalculationService.compareInspectionOptions(outbound1)
            val outbound2 = streamYardMoveMetricsRequest {
                this.context = request.context
                this.subscriber = contactInfo {
                    this.email = reply1.inspection.requester.email
                }
            }
            yardsYardMoveReportingService.streamYardMoveMetrics(outbound2).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound3 = createWorkOrderRequest {
                this.context = request.context
                this.workOrder = workOrder {
                    this.customer = driverRef {
                        this.displayName = reply1.inspection.customer.displayName
                    }
                }
            }
            val reply3 = maintenanceWorkOrderService.createWorkOrder(WireCopies.reflective(outbound3))
            submitDockResponse {
                this.dock = dock {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.user.audit.updatedBy
                    }
                    this.dockId = request.dockId
                    this.owner = userRef {
                        this.email = reply1.inspection.assignee.email
                    }
                    this.status = request.dock.status
                }
                this.outcome = SubmitDockResponseKt.outcome {
                    this.status = reply3.result.status
                }
                this.result = operationResult {
                    this.details.putAll(reply3.result.detailsMap)
                    this.status = "ok"
                }
            }
        }
}
