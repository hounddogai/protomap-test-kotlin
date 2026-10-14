package com.acme.transport.maintenance.api

import com.acme.authorization.v1.AccessPolicyServiceGrpcKt
import com.acme.authorization.v1.BatchGetAccessPoliciesRequest
import com.acme.authorization.v1.batchGetAccessPoliciesRequest
import com.acme.certificates.v1.CertificateQueryServiceGrpcKt
import com.acme.certificates.v1.ExportCertificatesRequestKt
import com.acme.certificates.v1.exportCertificatesRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.CheckDeliveryExceptionEligibilityRequest
import com.acme.deliveries.v1.CheckDeliveryExceptionEligibilityRequestKt
import com.acme.deliveries.v1.CheckDeliveryExceptionEligibilityResponse
import com.acme.deliveries.v1.CompareDeliveryExceptionOptionsRequest
import com.acme.deliveries.v1.CompareDeliveryExceptionOptionsResponse
import com.acme.deliveries.v1.DeliveryExceptionCalculationServiceGrpcKt
import com.acme.deliveries.v1.DeliveryExceptionValidationServiceGrpcKt
import com.acme.deliveries.v1.LintDeliveryExceptionsRequest
import com.acme.deliveries.v1.ProofOfDeliveryValidationServiceGrpcKt
import com.acme.deliveries.v1.QuoteDeliveryExceptionRequest
import com.acme.deliveries.v1.ValidateProofOfDeliveryRequestKt
import com.acme.deliveries.v1.checkDeliveryExceptionEligibilityRequest
import com.acme.deliveries.v1.compareDeliveryExceptionOptionsRequest
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.lintDeliveryExceptionsRequest
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.quoteDeliveryExceptionRequest
import com.acme.deliveries.v1.validateProofOfDeliveryRequest
import com.acme.documents.v1.DocumentServiceGrpcKt
import com.acme.documents.v1.ListDocumentsRequestKt
import com.acme.documents.v1.ListDocumentsResponse
import com.acme.documents.v1.listDocumentsRequest
import com.acme.drivers.v1.driverRef
import com.acme.fuel.v1.FuelCardValidationServiceGrpcKt
import com.acme.fuel.v1.FuelPolicy
import com.acme.fuel.v1.FuelPolicyCalculationServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyValidationServiceGrpcKt
import com.acme.fuel.v1.NormalizeFuelCardResponse
import com.acme.fuel.v1.NormalizeFuelPolicyRequestKt
import com.acme.fuel.v1.PreviewFuelCardRequest
import com.acme.fuel.v1.PreviewFuelCardRequestKt
import com.acme.fuel.v1.PreviewFuelCardResponse
import com.acme.fuel.v1.ScoreFuelPolicyRequest
import com.acme.fuel.v1.ScoreFuelPolicyRequestKt
import com.acme.fuel.v1.ValidateFuelPolicyRequest
import com.acme.fuel.v1.ValidateFuelPolicyRequestKt
import com.acme.fuel.v1.ValidateFuelPolicyResponse
import com.acme.fuel.v1.explainFuelPolicyCalculationRequest
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.fuelPolicyBlockAction
import com.acme.fuel.v1.fuelPolicyDiscountAction
import com.acme.fuel.v1.normalizeFuelCardRequest
import com.acme.fuel.v1.normalizeFuelPolicyRequest
import com.acme.fuel.v1.previewFuelCardRequest
import com.acme.fuel.v1.scoreFuelPolicyRequest
import com.acme.fuel.v1.validateFuelPolicyRequest
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.BrowseLedgerAccountsRequest
import com.acme.ledger.v1.BrowseLedgerAccountsRequestKt
import com.acme.ledger.v1.LedgerAccountQueryServiceGrpcKt
import com.acme.ledger.v1.browseLedgerAccountsRequest
import com.acme.maintenance.v1.CalculateWorkOrderRequest
import com.acme.maintenance.v1.CalculateWorkOrderResponse
import com.acme.maintenance.v1.CompareWorkOrderOptionsRequest
import com.acme.maintenance.v1.CompareWorkOrderOptionsResponse
import com.acme.maintenance.v1.EstimateWorkOrderRequest
import com.acme.maintenance.v1.EstimateWorkOrderResponse
import com.acme.maintenance.v1.ExplainWorkOrderCalculationRequest
import com.acme.maintenance.v1.ExplainWorkOrderCalculationResponse
import com.acme.maintenance.v1.GetWorkOrderRatesRequest
import com.acme.maintenance.v1.GetWorkOrderRatesResponse
import com.acme.maintenance.v1.QuoteWorkOrderRequest
import com.acme.maintenance.v1.QuoteWorkOrderResponse
import com.acme.maintenance.v1.RecalculateWorkOrdersRequest
import com.acme.maintenance.v1.RecalculateWorkOrdersResponse
import com.acme.maintenance.v1.SimulateWorkOrderRequest
import com.acme.maintenance.v1.SimulateWorkOrderResponse
import com.acme.maintenance.v1.WorkOrderCalculationServiceGrpcKt
import com.acme.maintenance.v1.WorkOrderStatusChange
import com.acme.maintenance.v1.calculateWorkOrderResponse
import com.acme.maintenance.v1.compareWorkOrderOptionsResponse
import com.acme.maintenance.v1.estimateWorkOrderResponse
import com.acme.maintenance.v1.explainWorkOrderCalculationResponse
import com.acme.maintenance.v1.getWorkOrderRatesResponse
import com.acme.maintenance.v1.quoteWorkOrderResponse
import com.acme.maintenance.v1.recalculateWorkOrdersResponse
import com.acme.maintenance.v1.simulateWorkOrderResponse
import com.acme.maintenance.v1.workOrder
import com.acme.maintenance.v1.workOrderSla
import com.acme.metrics.v1.BrowseMetricSeriesesRequestKt
import com.acme.metrics.v1.MetricSeriesQueryServiceGrpcKt
import com.acme.metrics.v1.browseMetricSeriesesRequest
import com.acme.search.v1.ResolveSearchIndexesRequestKt
import com.acme.search.v1.SearchIndexQueryServiceGrpcKt
import com.acme.search.v1.SearchIndexServiceGrpcKt
import com.acme.search.v1.SearchSearchIndexesRequestKt
import com.acme.search.v1.SummarizeSearchIndexesRequestKt
import com.acme.search.v1.resolveSearchIndexesRequest
import com.acme.search.v1.searchSearchIndexesRequest
import com.acme.search.v1.summarizeSearchIndexesRequest
import com.acme.transport.maintenance.MaintenanceRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.DockCalculationServiceGrpcKt
import com.acme.yards.v1.SimulateDockRequestKt
import com.acme.yards.v1.SimulateDockResponse
import com.acme.yards.v1.YardMoveCalculationServiceGrpcKt
import com.acme.yards.v1.dock
import com.acme.yards.v1.explainYardMoveCalculationRequest
import com.acme.yards.v1.simulateDockRequest
import com.acme.yards.v1.yardMove
import com.acme.yards.v1.yardMoveComment
import com.acme.yards.v1.yardMoveSla
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Computes the figures of a work order, such as totals, taxes, and estimates. */
class WorkOrderCalculationGrpcService(
    private val store: MaintenanceRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyService: AccessPolicyServiceGrpcKt.AccessPolicyServiceCoroutineStub,
    private val certificatesCertificateQueryService: CertificateQueryServiceGrpcKt.CertificateQueryServiceCoroutineStub,
    private val deliveriesDeliveryExceptionCalculationService: DeliveryExceptionCalculationServiceGrpcKt.DeliveryExceptionCalculationServiceCoroutineStub,
    private val deliveriesDeliveryExceptionValidationService: DeliveryExceptionValidationServiceGrpcKt.DeliveryExceptionValidationServiceCoroutineStub,
    private val deliveriesProofOfDeliveryValidationService: ProofOfDeliveryValidationServiceGrpcKt.ProofOfDeliveryValidationServiceCoroutineStub,
    private val documentsDocumentService: DocumentServiceGrpcKt.DocumentServiceCoroutineStub,
    private val fuelFuelCardValidationService: FuelCardValidationServiceGrpcKt.FuelCardValidationServiceCoroutineStub,
    private val fuelFuelPolicyCalculationService: FuelPolicyCalculationServiceGrpcKt.FuelPolicyCalculationServiceCoroutineStub,
    private val fuelFuelPolicyValidationService: FuelPolicyValidationServiceGrpcKt.FuelPolicyValidationServiceCoroutineStub,
    private val ledgerLedgerAccountQueryService: LedgerAccountQueryServiceGrpcKt.LedgerAccountQueryServiceCoroutineStub,
    private val metricsMetricSeriesQueryService: MetricSeriesQueryServiceGrpcKt.MetricSeriesQueryServiceCoroutineStub,
    private val searchSearchIndexQueryService: SearchIndexQueryServiceGrpcKt.SearchIndexQueryServiceCoroutineStub,
    private val searchSearchIndexService: SearchIndexServiceGrpcKt.SearchIndexServiceCoroutineStub,
    private val yardsDockCalculationService: DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub,
    private val yardsYardMoveCalculationService: YardMoveCalculationServiceGrpcKt.YardMoveCalculationServiceCoroutineStub,
) : WorkOrderCalculationServiceGrpcKt.WorkOrderCalculationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(WorkOrderCalculationGrpcService::class.java)

    private val invokeExplainWorkOrderCalculation2: suspend (CompareDeliveryExceptionOptionsRequest) -> CompareDeliveryExceptionOptionsResponse = { payload ->
        deliveriesDeliveryExceptionCalculationService.compareDeliveryExceptionOptions(payload)
    }

    private val workerForQuoteWorkOrder2 = ReplyMailbox<ValidateFuelPolicyRequest, ValidateFuelPolicyResponse>(workers) { payload ->
        withTimeout(5_000) { fuelFuelPolicyValidationService.validateFuelPolicy(payload) }
    }

    private val clientForQuoteWorkOrder3 by lazy { searchSearchIndexQueryService }

    /** Calculates a work order. */
    override suspend fun calculateWorkOrder(request: CalculateWorkOrderRequest): CalculateWorkOrderResponse {
        return handleCalculateWorkOrder(request)
    }

    /** Compares a work order. */
    override suspend fun compareWorkOrderOptions(request: CompareWorkOrderOptionsRequest): CompareWorkOrderOptionsResponse = handleCompareWorkOrderOptions(request)

    /** Estimates a work order. */
    override suspend fun estimateWorkOrder(request: EstimateWorkOrderRequest): EstimateWorkOrderResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderCalculationService/EstimateWorkOrder") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EstimateWorkOrder", request)
            log.atInfo().addKeyValue("operation", "EstimateWorkOrder").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.workOrder.workOrderId).log("Recorded audit detail")
            val outbound0 = browseLedgerAccountsRequest {
                this.context = request.context
                this.filter = BrowseLedgerAccountsRequestKt.filter {
                    this.createdAfter = request.workOrder.audit.createdAt
                    this.region = request.details.destination.region
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = ledgerLedgerAccountQueryService.browseLedgerAccounts(envelope0.unpack(BrowseLedgerAccountsRequest::class.java))
            val outbound1 = validateProofOfDeliveryRequest {
                this.context = request.context
                this.details = ValidateProofOfDeliveryRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.workOrder.requester.email
                        this.phone = request.workOrder.requester.phone
                    }
                }
                this.proofOfDelivery = proofOfDelivery {
                    this.audit = auditInfo {
                        this.createdBy = request.workOrder.audit.createdBy
                    }
                    this.counterparty = driverRef {
                        this.email = request.workOrder.customer.email
                    }
                }
            }
            val reply1 = deliveriesProofOfDeliveryValidationService.validateProofOfDelivery(WireCopies.json(outbound1))
            val outbound2 = EstimateWorkOrder2Command(
                context = request.context
            ).toRequest()
            val reply2 = authorizationAccessPolicyService.batchGetAccessPolicies(outbound2)
            estimateWorkOrderResponse {
                this.result = operationResult {
                    this.message = reply2.result.message
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = reply0.result.status
                }
                this.workOrder = workOrder {
                    this.audit = auditInfo {
                        this.updatedBy = reply1.proofOfDelivery.audit.updatedBy
                    }
                    this.priority = request.workOrder.priority
                    this.workOrderId = request.workOrderId
                }
            }
        }

    /** Explains a work order. */
    override suspend fun explainWorkOrderCalculation(request: ExplainWorkOrderCalculationRequest): ExplainWorkOrderCalculationResponse {
        return handleExplainWorkOrderCalculation(request)
    }

    /** Returns a work order. */
    override suspend fun getWorkOrderRates(request: GetWorkOrderRatesRequest): GetWorkOrderRatesResponse = handleGetWorkOrderRates(request)

    /** Quotes a work order. */
    override suspend fun quoteWorkOrder(request: QuoteWorkOrderRequest): QuoteWorkOrderResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderCalculationService/QuoteWorkOrder") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "QuoteWorkOrder", request)
            log.atInfo().addKeyValue("operation", "QuoteWorkOrder").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.workOrder.workOrderId).log("Recorded audit detail")
            val outbound0 = searchSearchIndexesRequest {
                this.context = request.context
                this.filter = SearchSearchIndexesRequestKt.filter {
                    this.createdAfter = request.workOrder.audit.updatedAt
                    this.labels.putAll(request.annotationsMap)
                    this.ownerId = request.workOrder.sla.policyId
                    this.region = request.details.destination.region
                }
            }
            val reply0 = ClientRegistry.lookup("acme.search.v1.SearchIndexService", SearchIndexServiceGrpcKt.SearchIndexServiceCoroutineStub::class.java).searchSearchIndexes(outbound0)
            val outbound1 = normalizeFuelPolicyRequest {
                this.context = request.context
                this.details = NormalizeFuelPolicyRequestKt.details {
                    this.contact = contactInfo {
                        this.name = request.workOrder.requester.name
                        this.phone = request.workOrder.requester.phone
                    }
                }
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.etag = request.workOrder.audit.etag
                    }
                    this.owner = userRef {
                        this.userId = request.workOrder.assignee.userId
                    }
                }
            }
            val reply1 = fuelFuelPolicyValidationService.normalizeFuelPolicy(WireCopies.reflective(outbound1))
            val outbound2 = validateFuelPolicyRequest {
                this.context = request.context
                this.details = ValidateFuelPolicyRequestKt.details {
                    this.contact = contactInfo {
                        this.name = request.workOrder.requester.name
                    }
                }
                this.fuelPolicy = fuelPolicy {
                    this.discount = fuelPolicyDiscountAction {
                        this.amount = reply1.fuelPolicy.discount.amount
                    }
                }
            }
            val reply2 = workerForQuoteWorkOrder2.submit(outbound2)
            val outbound3 = summarizeSearchIndexesRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.details = SummarizeSearchIndexesRequestKt.details {
                    this.period = reply2.fuelPolicy.effective
                }
                this.reason = reply2.fuelPolicy.block.reason
            }
            val reply3 = clientForQuoteWorkOrder3.summarizeSearchIndexes(outbound3)
            quoteWorkOrderResponse {
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = reply1.outcome.status
                }
                this.workOrder = workOrder {
                    this.requester = contactInfo {
                        this.email = reply2.fuelPolicy.owner.email
                    }
                    this.subject = request.workOrder.subject
                    this.workOrderId = request.workOrderId
                }
            }
        }

    /** Recalculates a work order. */
    override suspend fun recalculateWorkOrders(request: RecalculateWorkOrdersRequest): RecalculateWorkOrdersResponse {
        return handleRecalculateWorkOrders(request)
    }

    /** Simulates a work order. */
    override suspend fun simulateWorkOrder(request: SimulateWorkOrderRequest): SimulateWorkOrderResponse = handleSimulateWorkOrder(request)

    private suspend fun handleCalculateWorkOrder(request: CalculateWorkOrderRequest): CalculateWorkOrderResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderCalculationService/CalculateWorkOrder") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CalculateWorkOrder", request)
            log.atInfo().addKeyValue("operation", "CalculateWorkOrder").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.workOrder.workOrderId).log("Recorded audit detail")
            var reply0 = SimulateDockResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.workOrder.commentsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound0 = simulateDockRequest {
                                this.context = request.context
                                this.details = SimulateDockRequestKt.details {
                                    this.destination = postalAddress {
                                        this.countryCode = request.details.destination.countryCode
                                        this.locality = request.details.destination.locality
                                    }
                                }
                                this.dock = dock {
                                    this.audit = auditInfo {
                                        this.createdAt = item.createdAt
                                    }
                                    this.owner = userRef {
                                        this.email = item.authorEmail
                                    }
                                }
                            }
                            yardsDockCalculationService.simulateDock(outbound0)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply0 = it }
                }
            }
            var reply1 = NormalizeFuelCardResponse.getDefaultInstance()
            request.workOrder.commentsList.asFlow().map { item ->
                val outbound1 = normalizeFuelCardRequest {
                    this.context = request.context
                    this.fuelCard = fuelCard {
                        this.audit = auditInfo {
                            this.createdAt = item.createdAt
                        }
                        this.contact = contactInfo {
                            this.email = item.authorEmail
                        }
                    }
                }
                fuelFuelCardValidationService.normalizeFuelCard(outbound1)
            }.collect { reply1 = it }
            val outbound2 = validateFuelPolicyRequest {
                this.context = request.context
                this.details = ValidateFuelPolicyRequestKt.details {
                    this.contact = contactInfo {
                        this.email = reply1.fuelCard.contact.email
                        this.phone = request.workOrder.requester.phone
                        this.preferredChannel = request.workOrder.requester.preferredChannel
                    }
                }
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.createdAt = reply1.fuelCard.audit.createdAt
                    }
                }
            }
            val reply2 = fuelFuelPolicyValidationService.validateFuelPolicy(outbound2)
            calculateWorkOrderResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.workOrder = workOrder {
                    this.assignee = userRef {
                        this.userId = reply2.fuelPolicy.owner.userId
                    }
                    this.audit = auditInfo {
                        this.etag = reply2.fuelPolicy.audit.etag
                    }
                    this.requester = request.workOrder.requester
                    this.workOrderId = request.workOrderId
                }
            }
        }

    private fun CompareWorkOrderOptionsRequest.toScoreFuelPolicyFromCompareWorkOrderOptions1(): ScoreFuelPolicyRequest {
        val source = this
        return scoreFuelPolicyRequest {
            this.context = source.context
            this.details = ScoreFuelPolicyRequestKt.details {
                this.destination = postalAddress {
                    this.locality = source.details.destination.locality
                }
            }
        }
    }

    private suspend fun handleCompareWorkOrderOptions(request: CompareWorkOrderOptionsRequest): CompareWorkOrderOptionsResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderCalculationService/CompareWorkOrderOptions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CompareWorkOrderOptions", request)
            log.atInfo().addKeyValue("operation", "CompareWorkOrderOptions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.workOrder.workOrderId).log("Recorded audit detail")
            val outbound0 = exportCertificatesRequest {
                this.context = request.context
                this.filter = ExportCertificatesRequestKt.filter {
                    this.createdAfter = request.workOrder.audit.createdAt
                    this.ownerId = request.workOrder.customer.driverId
                    this.region = request.details.destination.region
                }
            }
            certificatesCertificateQueryService.exportCertificates(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = request.toScoreFuelPolicyFromCompareWorkOrderOptions1()
            val reply1 = fuelFuelPolicyValidationService.scoreFuelPolicy(outbound1)
            val outbound2 = explainFuelPolicyCalculationRequest {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.block = fuelPolicyBlockAction {
                        this.notifyEmail = when (reply1.fuelPolicy.actionCase) {
                            FuelPolicy.ActionCase.BLOCK -> reply1.fuelPolicy.block.notifyEmail
                            else -> ""
                        }
                    }
                }
            }
            val reply2 = fuelFuelPolicyCalculationService.explainFuelPolicyCalculation(outbound2)
            compareWorkOrderOptionsResponse {
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = "ok"
                }
                this.workOrder = workOrder {
                    this.priority = reply2.fuelPolicy.priority
                    this.subject = request.workOrder.subject
                    this.workOrderId = request.workOrderId
                }
            }
        }

    private data class EstimateWorkOrder2Command(
        val context: RequestContext
    ) {
        fun toRequest(): BatchGetAccessPoliciesRequest {
            val command = this
            return batchGetAccessPoliciesRequest {
                this.context = command.context
            }
        }
    }

    private suspend fun handleExplainWorkOrderCalculation(request: ExplainWorkOrderCalculationRequest): ExplainWorkOrderCalculationResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderCalculationService/ExplainWorkOrderCalculation") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainWorkOrderCalculation", request)
            log.atInfo().addKeyValue("operation", "ExplainWorkOrderCalculation").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.workOrder.workOrderId).log("Recorded audit detail")
            val outbound0 = lintDeliveryExceptionsRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.assignee = userRef {
                        this.email = request.workOrder.customer.email
                    }
                }
            }
            val command0: Submission<LintDeliveryExceptionsRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = deliveriesDeliveryExceptionValidationService.lintDeliveryExceptions(payload0)
            val outbound1 = checkDeliveryExceptionEligibilityRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.requester = contactInfo {
                        this.email = reply0.deliveryException.requester.email
                    }
                }
                this.details = CheckDeliveryExceptionEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.phone = request.details.contact.phone
                    }
                }
            }
            val adapter1 = OutboundPort<CheckDeliveryExceptionEligibilityRequest, CheckDeliveryExceptionEligibilityResponse> { payload ->
                deliveriesDeliveryExceptionValidationService.checkDeliveryExceptionEligibility(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            val outbound2 = compareDeliveryExceptionOptionsRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.requester = contactInfo {
                        this.preferredChannel = request.workOrder.requester.preferredChannel
                    }
                }
            }
            val reply2 = invokeExplainWorkOrderCalculation2(outbound2)
            val outbound3 = previewFuelCardRequest {
                this.context = request.context
                this.details = PreviewFuelCardRequestKt.details {
                    this.contact = contactInfo {
                        this.email = reply0.deliveryException.assignee.email
                    }
                }
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.updatedBy = request.workOrder.audit.updatedBy
                    }
                    this.contact = contactInfo {
                        this.preferredChannel = reply1.deliveryException.requester.preferredChannel
                    }
                }
            }
            val invoke3: suspend (PreviewFuelCardRequest, Metadata) -> PreviewFuelCardResponse = fuelFuelCardValidationService::previewFuelCard
            val reply3 = invoke3(outbound3, Metadata())
            explainWorkOrderCalculationResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.referenceIds.addAll(((reply1.result.referenceIdsList) + (reply2.result.referenceIdsList)) + (reply3.result.referenceIdsList))
                    this.status = reply0.result.status
                }
                this.workOrder = workOrder {
                    this.sla = workOrderSla {
                        this.dueAt = reply2.deliveryException.sla.dueAt
                    }
                    this.subject = request.workOrder.subject
                    this.workOrderId = request.workOrderId
                }
            }
        }

    private suspend fun handleGetWorkOrderRates(request: GetWorkOrderRatesRequest): GetWorkOrderRatesResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderCalculationService/GetWorkOrderRates") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetWorkOrderRates", request)
            log.atInfo().addKeyValue("operation", "GetWorkOrderRates").log("Handling transport request")
            val outbound0 = QuoteDeliveryExceptionRequest.newBuilder().apply {
                setContext(request.context)
                setDeliveryException(deliveryException {
                    this.assignee = userRef {
                        this.userId = request.context.actor.userId
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> deliveriesDeliveryExceptionCalculationService.quoteDeliveryException(value) }
            val outbound1 = RequestFactory.create {
                explainYardMoveCalculationRequest {
                    this.context = request.context
                    this.yardMove = yardMove {
                        this.audit = auditInfo {
                            this.createdBy = reply0.deliveryException.audit.createdBy
                        }
                        this.comments.addAll(listOf(yardMoveComment {
                            this.createdAt = reply0.deliveryException.audit.createdAt
                        }))
                        this.requester = contactInfo {
                            this.email = reply0.deliveryException.requester.email
                        }
                        this.sla = yardMoveSla {
                            this.dueAt = reply0.deliveryException.sla.dueAt
                        }
                    }
                }
            }
            val reply1 = yardsYardMoveCalculationService.explainYardMoveCalculation(outbound1)
            getWorkOrderRatesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.workOrder = workOrder {
                    this.audit = auditInfo {
                        this.createdBy = reply1.yardMove.audit.createdBy
                    }
                    this.requester = contactInfo {
                        this.email = reply0.deliveryException.requester.email
                        this.name = reply0.deliveryException.requester.name
                    }
                    this.workOrderId = request.workOrderId
                }
            }
        }

    private suspend fun handleRecalculateWorkOrders(request: RecalculateWorkOrdersRequest): RecalculateWorkOrdersResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderCalculationService/RecalculateWorkOrders") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecalculateWorkOrders", request)
            log.atInfo().addKeyValue("operation", "RecalculateWorkOrders").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.workOrder.workOrderId).log("Recorded audit detail")
            var reply0 = ListDocumentsResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<WorkOrderStatusChange>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound0 = listDocumentsRequest {
                            this.context = request.context
                            this.filter = ListDocumentsRequestKt.filter {
                                this.createdAfter = item.changedAt
                                this.ownerId = request.workOrder.customer.driverId
                                this.region = request.details.destination.region
                            }
                        }
                        reply0 = documentsDocumentService.listDocuments(outbound0)
                    }
                }
                try {
                    request.workOrder.historyList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound1 = explainFuelPolicyCalculationRequest {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.createdAt = request.workOrder.audit.createdAt
                        this.etag = request.workOrder.audit.etag
                    }
                }
            }
            val reply1 = withTimeout(5_000) { fuelFuelPolicyCalculationService.explainFuelPolicyCalculation(outbound1) }
            recalculateWorkOrdersResponse {
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = "ok"
                }
                this.workOrder = workOrder {
                    this.priority = request.workOrder.priority
                    this.workOrderId = request.workOrderId
                }
            }
        }

    private suspend fun handleSimulateWorkOrder(request: SimulateWorkOrderRequest): SimulateWorkOrderResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderCalculationService/SimulateWorkOrder") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SimulateWorkOrder", request)
            log.atInfo().addKeyValue("operation", "SimulateWorkOrder").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.workOrder.workOrderId).log("Recorded audit detail")
            val configure0: ResolveSearchIndexesRequestKt.Dsl.() -> Unit = {
                this.context = request.context
            }
            val outbound0 = resolveSearchIndexesRequest(configure0)
            val reply0 = searchSearchIndexQueryService.resolveSearchIndexes(outbound0)
            val outbound1 = browseMetricSeriesesRequest {
                this.context = request.context
                this.filter = BrowseMetricSeriesesRequestKt.filter {
                    this.region = request.details.destination.region
                }
            }
            val reply1 = metricsMetricSeriesQueryService.browseMetricSerieses(outbound1)
            simulateWorkOrderResponse {
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply0.result.message
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
                this.workOrder = workOrder {
                    this.requester = request.workOrder.requester
                    this.workOrderId = request.workOrderId
                }
            }
        }
}
