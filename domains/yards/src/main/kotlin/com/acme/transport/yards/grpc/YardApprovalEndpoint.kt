package com.acme.transport.yards.grpc

import com.acme.authorization.v1.AccessPolicyValidationServiceGrpcKt
import com.acme.authorization.v1.PreviewAccessPolicyRequestKt
import com.acme.authorization.v1.accessPolicy
import com.acme.authorization.v1.previewAccessPolicyRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.customerRef
import com.acme.documents.v1.DocumentServiceGrpcKt
import com.acme.documents.v1.ListDocumentsRequest
import com.acme.documents.v1.ListDocumentsRequestKt
import com.acme.documents.v1.batchGetDocumentsRequest
import com.acme.documents.v1.listDocumentsRequest
import com.acme.drivers.v1.CalculateDriverRequest
import com.acme.drivers.v1.CalculateDriverRequestKt
import com.acme.drivers.v1.CalculateDriverResponse
import com.acme.drivers.v1.DriverCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseValidationServiceGrpcKt
import com.acme.drivers.v1.calculateDriverRequest
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.normalizeDriverLicenseRequest
import com.acme.fuel.v1.FuelCardValidationServiceGrpcKt
import com.acme.fuel.v1.LintFuelCardsRequest
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.lintFuelCardsRequest
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.LedgerAccountServiceGrpcKt
import com.acme.ledger.v1.createLedgerAccountRequest
import com.acme.ledger.v1.ledgerAccount
import com.acme.maintenance.v1.WorkOrderCalculationServiceGrpcKt
import com.acme.maintenance.v1.recalculateWorkOrdersRequest
import com.acme.maintenance.v1.workOrder
import com.acme.orders.v1.ListOrdersRequest
import com.acme.orders.v1.ListOrdersRequestKt
import com.acme.orders.v1.OrderServiceGrpcKt
import com.acme.orders.v1.listOrdersRequest
import com.acme.returns.v1.ReturnRequestCalculationServiceGrpcKt
import com.acme.returns.v1.estimateReturnRequestRequest
import com.acme.returns.v1.returnRequest
import com.acme.returns.v1.returnRequestLine
import com.acme.routing.v1.ExplainStopDecisionRequest
import com.acme.routing.v1.StopValidationServiceGrpcKt
import com.acme.routing.v1.explainStopDecisionRequest
import com.acme.routing.v1.stop
import com.acme.routing.v1.stopParticipant
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.transport.yards.YardsRepository
import com.acme.yards.v1.ApproveYardRequest
import com.acme.yards.v1.ApproveYardResponse
import com.acme.yards.v1.ApproveYardResponseKt
import com.acme.yards.v1.CommentYardApprovalRequest
import com.acme.yards.v1.CommentYardApprovalResponse
import com.acme.yards.v1.CommentYardApprovalResponseKt
import com.acme.yards.v1.CompareYardOptionsRequest
import com.acme.yards.v1.CompareYardOptionsResponse
import com.acme.yards.v1.DelegateYardApprovalRequest
import com.acme.yards.v1.DelegateYardApprovalResponse
import com.acme.yards.v1.DelegateYardApprovalResponseKt
import com.acme.yards.v1.DockComponent
import com.acme.yards.v1.DockQueryServiceGrpcKt
import com.acme.yards.v1.DockValidationServiceGrpcKt
import com.acme.yards.v1.EscalateYardRequest
import com.acme.yards.v1.EscalateYardResponse
import com.acme.yards.v1.EscalateYardResponseKt
import com.acme.yards.v1.GetYardApprovalStatusRequest
import com.acme.yards.v1.GetYardApprovalStatusResponse
import com.acme.yards.v1.ListPendingYardsRequest
import com.acme.yards.v1.ListPendingYardsResponse
import com.acme.yards.v1.PreviewDockRequest
import com.acme.yards.v1.PreviewDockResponse
import com.acme.yards.v1.QuoteYardRequestKt
import com.acme.yards.v1.RecallYardRequest
import com.acme.yards.v1.RecallYardResponse
import com.acme.yards.v1.RejectYardRequest
import com.acme.yards.v1.RejectYardResponse
import com.acme.yards.v1.StreamYardMoveMetricsRequestKt
import com.acme.yards.v1.SubmitYardRequest
import com.acme.yards.v1.SubmitYardResponse
import com.acme.yards.v1.SubmitYardResponseKt
import com.acme.yards.v1.SummarizeDocksRequest
import com.acme.yards.v1.SummarizeDocksRequestKt
import com.acme.yards.v1.YardApprovalServiceGrpcKt
import com.acme.yards.v1.YardCalculationServiceGrpcKt
import com.acme.yards.v1.YardComponent
import com.acme.yards.v1.YardMoveReportingServiceGrpcKt
import com.acme.yards.v1.approveYardResponse
import com.acme.yards.v1.commentYardApprovalResponse
import com.acme.yards.v1.compareYardOptionsRequest
import com.acme.yards.v1.copy as yardsCopy
import com.acme.yards.v1.delegateYardApprovalResponse
import com.acme.yards.v1.dock
import com.acme.yards.v1.dockComponent
import com.acme.yards.v1.dockDimensions
import com.acme.yards.v1.escalateYardResponse
import com.acme.yards.v1.getYardApprovalStatusResponse
import com.acme.yards.v1.lintDocksRequest
import com.acme.yards.v1.listPendingYardsResponse
import com.acme.yards.v1.previewDockRequest
import com.acme.yards.v1.quoteYardRequest
import com.acme.yards.v1.recallYardResponse
import com.acme.yards.v1.rejectYardResponse
import com.acme.yards.v1.streamYardMoveMetricsRequest
import com.acme.yards.v1.submitYardResponse
import com.acme.yards.v1.summarizeDocksRequest
import com.acme.yards.v1.yard
import com.acme.yards.v1.yardComponent
import com.acme.yards.v1.yardDimensions
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Routes yards through approval, with escalation and delegation. */
class YardApprovalEndpoint(
    private val store: YardsRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyValidationService: AccessPolicyValidationServiceGrpcKt.AccessPolicyValidationServiceCoroutineStub,
    private val documentsDocumentService: DocumentServiceGrpcKt.DocumentServiceCoroutineStub,
    private val driversDriverCalculationService: DriverCalculationServiceGrpcKt.DriverCalculationServiceCoroutineStub,
    private val driversDriverLicenseValidationService: DriverLicenseValidationServiceGrpcKt.DriverLicenseValidationServiceCoroutineStub,
    private val fuelFuelCardValidationService: FuelCardValidationServiceGrpcKt.FuelCardValidationServiceCoroutineStub,
    private val ledgerLedgerAccountService: LedgerAccountServiceGrpcKt.LedgerAccountServiceCoroutineStub,
    private val maintenanceWorkOrderCalculationService: WorkOrderCalculationServiceGrpcKt.WorkOrderCalculationServiceCoroutineStub,
    private val ordersOrderService: OrderServiceGrpcKt.OrderServiceCoroutineStub,
    private val returnsReturnRequestCalculationService: ReturnRequestCalculationServiceGrpcKt.ReturnRequestCalculationServiceCoroutineStub,
    private val routingStopValidationService: StopValidationServiceGrpcKt.StopValidationServiceCoroutineStub,
    private val yardsDockQueryService: DockQueryServiceGrpcKt.DockQueryServiceCoroutineStub,
    private val yardsDockValidationService: DockValidationServiceGrpcKt.DockValidationServiceCoroutineStub,
    private val yardsYardCalculationService: YardCalculationServiceGrpcKt.YardCalculationServiceCoroutineStub,
    private val yardsYardMoveReportingService: YardMoveReportingServiceGrpcKt.YardMoveReportingServiceCoroutineStub,
) : YardApprovalServiceGrpcKt.YardApprovalServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(YardApprovalEndpoint::class.java)

    private val clientForApproveYard0 by lazy { ledgerLedgerAccountService }

    private val invokeRejectYard0: suspend (PreviewDockRequest) -> PreviewDockResponse = { payload ->
        yardsDockValidationService.previewDock(payload)
    }

    /** Approves a yard. */
    override suspend fun approveYard(request: ApproveYardRequest): ApproveYardResponse =
        RpcBoundary.run("acme.yards.v1.YardApprovalService/ApproveYard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ApproveYard", request)
            log.atInfo().addKeyValue("operation", "ApproveYard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yard.yardId).log("Recorded audit detail")
            val outbound0 = createLedgerAccountRequest {
                this.context = request.context
                this.ledgerAccount = ledgerAccount {
                    this.audit = auditInfo {
                        this.createdBy = request.yard.audit.createdBy
                        this.updatedAt = request.yard.audit.updatedAt
                    }
                }
            }
            val reply0 = clientForApproveYard0.createLedgerAccount(outbound0)
            approveYardResponse {
                this.outcome = ApproveYardResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.yard = yard {
                    this.specs.putAll(request.yard.specsMap)
                    this.yardId = request.yardId
                }
            }
        }

    /** Comments a yard. */
    override suspend fun commentYardApproval(request: CommentYardApprovalRequest): CommentYardApprovalResponse {
        return handleCommentYardApproval(request)
    }

    /** Delegates a yard. */
    override suspend fun delegateYardApproval(request: DelegateYardApprovalRequest): DelegateYardApprovalResponse = handleDelegateYardApproval(request)

    /** Escalates a yard. */
    override suspend fun escalateYard(request: EscalateYardRequest): EscalateYardResponse =
        RpcBoundary.run("acme.yards.v1.YardApprovalService/EscalateYard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EscalateYard", request)
            log.atInfo().addKeyValue("operation", "EscalateYard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yard.yardId).log("Recorded audit detail")
            val outbound0 = quoteYardRequest {
                this.context = request.context
                this.details = QuoteYardRequestKt.details {
                    this.destination = postalAddress {
                        this.postalCode = request.yard.address.postalCode
                    }
                }
                this.yard = yard {
                    this.audit = auditInfo {
                        this.createdBy = request.yard.audit.createdBy
                        this.updatedAt = request.yard.audit.updatedAt
                    }
                    this.dimensions = yardDimensions {
                        this.height = request.yard.dimensions.height
                    }
                }
            }
            val reply0 = yardsYardCalculationService.quoteYard(outbound0)
            val outbound1 = lintDocksRequest {}.yardsCopy {
                this.context = request.context
                this.dock = dock {
                    this.address = postalAddress {
                        this.postalCode = reply0.yard.address.postalCode
                    }
                    this.audit = auditInfo {
                        this.etag = request.yard.audit.etag
                    }
                    this.components.putAll(mapOf("primary" to dockComponent {
                        this.componentId = (reply0.yard.componentsMap["primary"] ?: YardComponent.getDefaultInstance()).componentId
                    }))
                    this.dimensions = dockDimensions {
                        this.weight = reply0.yard.dimensions.weight
                    }
                }
            }
            val reply1 = yardsDockValidationService.lintDocks(outbound1)
            escalateYardResponse {
                this.outcome = EscalateYardResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.yard = yard {
                    this.dimensions = yardDimensions {
                        this.length = reply1.dock.dimensions.length
                    }
                    this.name = reply1.dock.name
                    this.status = request.yard.status
                    this.yardId = request.yardId
                }
            }
        }

    /** Returns a yard. */
    override suspend fun getYardApprovalStatus(request: GetYardApprovalStatusRequest): GetYardApprovalStatusResponse {
        return handleGetYardApprovalStatus(request)
    }

    /** Lists yards a page at a time. */
    override suspend fun listPendingYards(request: ListPendingYardsRequest): ListPendingYardsResponse = handleListPendingYards(request)

    /** Recalls a yard. */
    override suspend fun recallYard(request: RecallYardRequest): RecallYardResponse =
        RpcBoundary.run("acme.yards.v1.YardApprovalService/RecallYard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecallYard", request)
            log.atInfo().addKeyValue("operation", "RecallYard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yard.yardId).log("Recorded audit detail")
            val outbound0 = estimateReturnRequestRequest {
                this.context = request.context
                this.returnRequest = returnRequest {
                    this.counterparty = customerRef {
                        this.displayName = request.details.approver.displayName
                        this.email = request.yard.owner.email
                    }
                    this.lines.addAll(listOf(returnRequestLine {
                        this.quantity = (request.yard.componentsMap["primary"] ?: YardComponent.getDefaultInstance()).quantity
                    }))
                }
            }
            val reply0 = returnsReturnRequestCalculationService.estimateReturnRequest(WireCopies.json(outbound0))
            val outbound1 = RecallYard1Command(
                context = request.context,
                annotations = request.annotationsMap,
                reason = request.reason,
                detailsFilters = reply0.result.detailsMap
            ).toRequest()
            val reply1 = yardsDockQueryService.summarizeDocks(outbound1)
            val outbound2 = explainStopDecisionRequest {
                this.context = request.context
                this.stop = stop {
                    this.location = postalAddress {
                        this.lines.addAll(reply0.returnRequest.billingAddress.linesList)
                    }
                    this.participants.addAll(listOf(stopParticipant {
                        this.contact = contactInfo {
                            this.email = request.details.approver.email
                        }
                    }))
                }
            }
            val command2: Submission<ExplainStopDecisionRequest> = Submission.Live(outbound2)
            val payload2 = when (val command = command2) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply2 = routingStopValidationService.explainStopDecision(payload2)
            val outbound3 = calculateDriverRequest {
                this.context = request.context
                this.details = CalculateDriverRequestKt.details {
                    this.destination = postalAddress {
                        this.postalCode = reply2.stop.location.postalCode
                    }
                }
                this.driver = driver {
                    this.primaryAddress = postalAddress {
                        this.lines.addAll(reply0.returnRequest.billingAddress.linesList)
                    }
                }
            }
            val adapter3 = OutboundPort<CalculateDriverRequest, CalculateDriverResponse> { payload ->
                driversDriverCalculationService.calculateDriver(payload)
            }
            val port3 = DelegatingPort(adapter3)
            val reply3 = port3.execute(outbound3)
            recallYardResponse {
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.status = reply2.outcome.status
                }
                this.yard = yard {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.returnRequest.audit.updatedBy
                    }
                    this.owner = userRef {
                        this.userId = reply3.driver.relationshipManager.userId
                    }
                    this.status = request.yard.status
                    this.yardId = request.yardId
                }
            }
        }

    /** Rejects a yard. */
    override suspend fun rejectYard(request: RejectYardRequest): RejectYardResponse {
        return handleRejectYard(request)
    }

    /** Submits a yard. */
    override suspend fun submitYard(request: SubmitYardRequest): SubmitYardResponse = handleSubmitYard(request)

    private suspend fun handleCommentYardApproval(request: CommentYardApprovalRequest): CommentYardApprovalResponse =
        RpcBoundary.run("acme.yards.v1.YardApprovalService/CommentYardApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CommentYardApproval", request)
            log.atInfo().addKeyValue("operation", "CommentYardApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yard.yardId).log("Recorded audit detail")
            val outbound0 = batchGetDocumentsRequest {
                this.context = request.context
            }
            val reply0 = flowOf(outbound0).map { value -> documentsDocumentService.batchGetDocuments(value) }.single()
            val outbound1 = normalizeDriverLicenseRequest {
                this.context = request.context
                this.driverLicense = driverLicense {
                    this.billingAddress = postalAddress {
                        this.region = request.yard.address.region
                    }
                }
            }
            val reply1 = withTimeout(5_000) { driversDriverLicenseValidationService.normalizeDriverLicense(outbound1) }
            commentYardApprovalResponse {
                this.outcome = CommentYardApprovalResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.message = reply1.result.message
                }
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
                this.yard = yard {
                    this.yardId = request.yardId
                }
            }
        }

    private suspend fun handleDelegateYardApproval(request: DelegateYardApprovalRequest): DelegateYardApprovalResponse =
        RpcBoundary.run("acme.yards.v1.YardApprovalService/DelegateYardApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DelegateYardApproval", request)
            log.atInfo().addKeyValue("operation", "DelegateYardApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yard.yardId).log("Recorded audit detail")
            val configure0: PreviewAccessPolicyRequestKt.Dsl.() -> Unit = {
                this.accessPolicy = accessPolicy {
                    this.audit = auditInfo {
                        this.updatedAt = request.yard.audit.updatedAt
                    }
                    this.name = request.yard.name
                    this.owner = userRef {
                        this.email = request.yard.owner.email
                        this.userId = request.yard.owner.userId
                    }
                }
                this.context = request.context
            }
            val outbound0 = previewAccessPolicyRequest(configure0)
            val reply0 = authorizationAccessPolicyValidationService.previewAccessPolicy(outbound0)
            val outbound1 = recalculateWorkOrdersRequest {
                this.context = request.context
                this.workOrder = workOrder {
                    this.audit = auditInfo {
                        this.etag = request.yard.audit.etag
                    }
                }
            }
            val reply1 = maintenanceWorkOrderCalculationService.recalculateWorkOrders(outbound1)
            delegateYardApprovalResponse {
                this.outcome = DelegateYardApprovalResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
                this.yard = yard {
                    this.owner = userRef {
                        this.userId = reply1.workOrder.assignee.userId
                    }
                    this.yardId = request.yardId
                }
            }
        }

    private fun GetYardApprovalStatusRequest.toListOrdersFromGetYardApprovalStatus0(): ListOrdersRequest {
        val source = this
        return listOrdersRequest {
            this.context = source.context
            this.filter = ListOrdersRequestKt.filter {
                this.createdAfter = source.context.deadline
                this.labels.putAll(source.context.baggageMap)
                this.ownerId = source.yardId
            }
        }
    }

    private suspend fun handleGetYardApprovalStatus(request: GetYardApprovalStatusRequest): GetYardApprovalStatusResponse =
        RpcBoundary.run("acme.yards.v1.YardApprovalService/GetYardApprovalStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetYardApprovalStatus", request)
            log.atInfo().addKeyValue("operation", "GetYardApprovalStatus").log("Handling transport request")
            val outbound0 = request.toListOrdersFromGetYardApprovalStatus0()
            val reply0 = ordersOrderService.listOrders(outbound0)
            getYardApprovalStatusResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.message = reply0.result.message
                    this.status = "ok"
                }
                this.yard = yard {
                    this.yardId = request.yardId
                }
            }
        }

    private suspend fun handleListPendingYards(request: ListPendingYardsRequest): ListPendingYardsResponse =
        RpcBoundary.run("acme.yards.v1.YardApprovalService/ListPendingYards") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListPendingYards", request)
            log.atInfo().addKeyValue("operation", "ListPendingYards").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = streamYardMoveMetricsRequest {
                this.context = request.context
                this.filter = StreamYardMoveMetricsRequestKt.filter {
                    this.labels.putAll(request.filter.labelsMap)
                    this.query = request.filter.query
                }
            }
            yardsYardMoveReportingService.streamYardMoveMetrics(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = lintFuelCardsRequest {
                this.context = request.context
                this.fuelCard = fuelCard {
                    this.holder = driverRef {
                        this.email = request.context.actor.email
                    }
                    this.labels.putAll(request.filter.labelsMap)
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = fuelFuelCardValidationService.lintFuelCards(envelope1.unpack(LintFuelCardsRequest::class.java))
            listPendingYardsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.yards.addAll(listOf(yard {
                    this.audit = auditInfo {
                        this.updatedBy = reply1.fuelCard.audit.updatedBy
                    }
                }))
            }
        }

    private data class RecallYard1Command(
        val context: RequestContext,
        val annotations: Map<String, String>,
        val reason: String,
        val detailsFilters: Map<String, String>
    ) {
        fun toRequest(): SummarizeDocksRequest {
            val command = this
            return summarizeDocksRequest {
                this.annotations.putAll(command.annotations)
                this.context = command.context
                this.details = SummarizeDocksRequestKt.details {
                    this.filters.putAll(command.detailsFilters)
                }
                this.reason = command.reason
            }
        }
    }

    private suspend fun handleRejectYard(request: RejectYardRequest): RejectYardResponse =
        RpcBoundary.run("acme.yards.v1.YardApprovalService/RejectYard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RejectYard", request)
            log.atInfo().addKeyValue("operation", "RejectYard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yard.yardId).log("Recorded audit detail")
            val outbound0 = previewDockRequest {
                this.context = request.context
                this.dock = dock {
                    this.components.putAll(mapOf("primary" to dockComponent {
                        this.quantity = (request.yard.componentsMap["primary"] ?: YardComponent.getDefaultInstance()).quantity
                    }))
                    this.dimensions = dockDimensions {
                        this.length = request.yard.dimensions.length
                    }
                    this.owner = userRef {
                        this.displayName = request.yard.owner.displayName
                        this.userId = request.yard.owner.userId
                    }
                }
            }
            val reply0 = invokeRejectYard0(outbound0)
            val outbound1 = compareYardOptionsRequest {
                this.context = request.context
                this.yard = yard {
                    this.components.putAll(mapOf("primary" to yardComponent {
                        this.componentId = (reply0.dock.componentsMap["primary"] ?: DockComponent.getDefaultInstance()).componentId
                    }))
                }
            }
            val invoke1: suspend (CompareYardOptionsRequest, Metadata) -> CompareYardOptionsResponse = yardsYardCalculationService::compareYardOptions
            val reply1 = invoke1(outbound1, Metadata())
            rejectYardResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
                this.yard = yard {
                    this.dimensions = yardDimensions {
                        this.width = reply0.dock.dimensions.width
                    }
                    this.owner = request.yard.owner
                    this.yardId = request.yardId
                }
            }
        }

    private suspend fun handleSubmitYard(request: SubmitYardRequest): SubmitYardResponse =
        RpcBoundary.run("acme.yards.v1.YardApprovalService/SubmitYard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SubmitYard", request)
            log.atInfo().addKeyValue("operation", "SubmitYard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yard.yardId).log("Recorded audit detail")
            val outbound0 = ListDocumentsRequest.newBuilder().apply {
                setContext(request.context)
                setFilter(ListDocumentsRequestKt.filter {
                    this.region = request.yard.address.region
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> documentsDocumentService.listDocuments(value) }
            submitYardResponse {
                this.outcome = SubmitYardResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
                this.yard = yard {
                    this.yardId = request.yardId
                }
            }
        }
}
