package com.acme.transport.fuel.grpc

import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.customerRef
import com.acme.deliveries.v1.DeliveryQueryServiceGrpcKt
import com.acme.deliveries.v1.SummarizeDeliveriesRequest
import com.acme.deliveries.v1.SummarizeDeliveriesRequestKt
import com.acme.deliveries.v1.SummarizeDeliveriesResponse
import com.acme.deliveries.v1.summarizeDeliveriesRequest
import com.acme.drivers.v1.driverRef
import com.acme.fuel.v1.CalculateFuelPolicyRequest
import com.acme.fuel.v1.CalculateFuelPolicyResponse
import com.acme.fuel.v1.CompareFuelPolicyOptionsRequest
import com.acme.fuel.v1.CompareFuelPolicyOptionsResponse
import com.acme.fuel.v1.EstimateFuelCardRequest
import com.acme.fuel.v1.EstimateFuelPolicyRequest
import com.acme.fuel.v1.EstimateFuelPolicyResponse
import com.acme.fuel.v1.ExplainFuelCardCalculationRequest
import com.acme.fuel.v1.ExplainFuelCardCalculationRequestKt
import com.acme.fuel.v1.ExplainFuelCardCalculationResponse
import com.acme.fuel.v1.ExplainFuelPolicyCalculationRequest
import com.acme.fuel.v1.ExplainFuelPolicyCalculationResponse
import com.acme.fuel.v1.ExplainFuelPolicyCalculationResponseKt
import com.acme.fuel.v1.FuelCardCalculationServiceGrpcKt
import com.acme.fuel.v1.FuelCardValidationServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyCalculationServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionCalculationServiceGrpcKt
import com.acme.fuel.v1.GetFuelPolicyRatesRequest
import com.acme.fuel.v1.GetFuelPolicyRatesResponse
import com.acme.fuel.v1.QuoteFuelPolicyRequest
import com.acme.fuel.v1.QuoteFuelPolicyResponse
import com.acme.fuel.v1.RecalculateFuelPoliciesRequest
import com.acme.fuel.v1.RecalculateFuelPoliciesResponse
import com.acme.fuel.v1.ScoreFuelCardRequestKt
import com.acme.fuel.v1.SimulateFuelCardRequest
import com.acme.fuel.v1.SimulateFuelCardResponse
import com.acme.fuel.v1.SimulateFuelPolicyRequest
import com.acme.fuel.v1.SimulateFuelPolicyResponse
import com.acme.fuel.v1.calculateFuelPolicyResponse
import com.acme.fuel.v1.compareFuelPolicyOptionsResponse
import com.acme.fuel.v1.estimateFuelCardRequest
import com.acme.fuel.v1.estimateFuelPolicyResponse
import com.acme.fuel.v1.explainFuelCardCalculationRequest
import com.acme.fuel.v1.explainFuelCardDecisionRequest
import com.acme.fuel.v1.explainFuelPolicyCalculationResponse
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.fuelCardCardFunding
import com.acme.fuel.v1.fuelCardLimit
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.getFuelPolicyRatesResponse
import com.acme.fuel.v1.quoteFuelPolicyResponse
import com.acme.fuel.v1.recalculateFuelPoliciesResponse
import com.acme.fuel.v1.recalculateFuelTransactionsRequest
import com.acme.fuel.v1.scoreFuelCardRequest
import com.acme.fuel.v1.simulateFuelCardRequest
import com.acme.fuel.v1.simulateFuelPolicyResponse
import com.acme.fuel.v1.validateFuelCardRequest
import com.acme.identity.v1.userRef
import com.acme.keymanagement.v1.CryptoKeyQueryServiceGrpcKt
import com.acme.keymanagement.v1.resolveCryptoKeysRequest
import com.acme.metrics.v1.MetricSeriesQueryServiceGrpcKt
import com.acme.metrics.v1.SuggestMetricSeriesesRequestKt
import com.acme.metrics.v1.suggestMetricSeriesesRequest
import com.acme.notifications.v1.NotificationServiceGrpcKt
import com.acme.notifications.v1.batchGetNotificationsRequest
import com.acme.notifications.v1.copy as notificationsCopy
import com.acme.search.v1.ScoreSearchIndexRequestKt
import com.acme.search.v1.SearchIndex
import com.acme.search.v1.SearchIndexValidationServiceGrpcKt
import com.acme.search.v1.scoreSearchIndexRequest
import com.acme.search.v1.searchIndex
import com.acme.tours.v1.BrowseToursRequestKt
import com.acme.tours.v1.TourQueryServiceGrpcKt
import com.acme.tours.v1.browseToursRequest
import com.acme.transport.fuel.FuelRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.ExplainYardMoveCalculationRequest
import com.acme.yards.v1.ExplainYardMoveCalculationRequestKt
import com.acme.yards.v1.ExplainYardMoveCalculationResponse
import com.acme.yards.v1.YardMoveCalculationServiceGrpcKt
import com.acme.yards.v1.explainYardMoveCalculationRequest
import com.acme.yards.v1.yardMove
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Computes the figures of a fuel policy, such as totals, taxes, and estimates. */
class FuelPolicyCalculationEndpoint(
    private val store: FuelRepository,
    private val workers: CoroutineScope,
    private val deliveriesDeliveryQueryService: DeliveryQueryServiceGrpcKt.DeliveryQueryServiceCoroutineStub,
    private val fuelFuelCardCalculationService: FuelCardCalculationServiceGrpcKt.FuelCardCalculationServiceCoroutineStub,
    private val fuelFuelCardValidationService: FuelCardValidationServiceGrpcKt.FuelCardValidationServiceCoroutineStub,
    private val fuelFuelTransactionCalculationService: FuelTransactionCalculationServiceGrpcKt.FuelTransactionCalculationServiceCoroutineStub,
    private val keymanagementCryptoKeyQueryService: CryptoKeyQueryServiceGrpcKt.CryptoKeyQueryServiceCoroutineStub,
    private val metricsMetricSeriesQueryService: MetricSeriesQueryServiceGrpcKt.MetricSeriesQueryServiceCoroutineStub,
    private val notificationsNotificationService: NotificationServiceGrpcKt.NotificationServiceCoroutineStub,
    private val searchSearchIndexValidationService: SearchIndexValidationServiceGrpcKt.SearchIndexValidationServiceCoroutineStub,
    private val toursTourQueryService: TourQueryServiceGrpcKt.TourQueryServiceCoroutineStub,
    private val yardsYardMoveCalculationService: YardMoveCalculationServiceGrpcKt.YardMoveCalculationServiceCoroutineStub,
) : FuelPolicyCalculationServiceGrpcKt.FuelPolicyCalculationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(FuelPolicyCalculationEndpoint::class.java)

    private val invokeCalculateFuelPolicy1: suspend (SummarizeDeliveriesRequest) -> SummarizeDeliveriesResponse = { payload ->
        deliveriesDeliveryQueryService.summarizeDeliveries(payload)
    }

    private val workerForExplainFuelPolicyCalculation0 = ReplyMailbox<SimulateFuelCardRequest, SimulateFuelCardResponse>(workers) { payload ->
        withTimeout(5_000) { fuelFuelCardCalculationService.simulateFuelCard(payload) }
    }

    private val clientForExplainFuelPolicyCalculation1 by lazy { toursTourQueryService }

    /** Calculates a fuel policy. */
    override suspend fun calculateFuelPolicy(request: CalculateFuelPolicyRequest): CalculateFuelPolicyResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyCalculationService/CalculateFuelPolicy") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CalculateFuelPolicy", request)
            log.atInfo().addKeyValue("operation", "CalculateFuelPolicy").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicy.fuelPolicyId).log("Recorded audit detail")
            val outbound0 = explainFuelCardCalculationRequest {
                this.context = request.context
                this.details = ExplainFuelCardCalculationRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.fuelPolicy.owner.email
                    }
                }
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.createdAt = request.fuelPolicy.audit.createdAt
                        this.createdBy = request.fuelPolicy.audit.createdBy
                    }
                }
            }
            val adapter0 = OutboundPort<ExplainFuelCardCalculationRequest, ExplainFuelCardCalculationResponse> { payload ->
                fuelFuelCardCalculationService.explainFuelCardCalculation(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = summarizeDeliveriesRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.details = SummarizeDeliveriesRequestKt.details {
                    this.filters.putAll(reply0.result.detailsMap)
                    this.period = request.fuelPolicy.effective
                }
                this.reason = request.fuelPolicy.block.reason
            }
            val reply1 = invokeCalculateFuelPolicy1(outbound1)
            calculateFuelPolicyResponse {
                this.fuelPolicy = fuelPolicy {
                    this.fuelPolicyId = request.fuelPolicyId
                    this.priority = request.fuelPolicy.priority
                }
                this.result = operationResult {
                    this.message = reply0.outcome.message
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = reply0.outcome.status
                }
            }
        }

    /** Compares a fuel policy. */
    override suspend fun compareFuelPolicyOptions(request: CompareFuelPolicyOptionsRequest): CompareFuelPolicyOptionsResponse {
        return handleCompareFuelPolicyOptions(request)
    }

    /** Estimates a fuel policy. */
    override suspend fun estimateFuelPolicy(request: EstimateFuelPolicyRequest): EstimateFuelPolicyResponse = handleEstimateFuelPolicy(request)

    /** Explains a fuel policy. */
    override suspend fun explainFuelPolicyCalculation(request: ExplainFuelPolicyCalculationRequest): ExplainFuelPolicyCalculationResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyCalculationService/ExplainFuelPolicyCalculation") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainFuelPolicyCalculation", request)
            log.atInfo().addKeyValue("operation", "ExplainFuelPolicyCalculation").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicy.name).log("Recorded audit detail")
            val outbound0 = simulateFuelCardRequest {
                this.context = request.context
                this.fuelCard = fuelCard {
                    this.holder = driverRef {
                        this.displayName = request.fuelPolicy.owner.displayName
                    }
                }
            }
            val reply0 = workerForExplainFuelPolicyCalculation0.submit(outbound0)
            val outbound1 = browseToursRequest {
                this.context = request.context
                this.filter = BrowseToursRequestKt.filter {
                    this.createdAfter = reply0.fuelCard.audit.createdAt
                    this.labels.putAll(reply0.fuelCard.labelsMap)
                    this.ownerId = request.fuelPolicy.fuelPolicyId
                }
            }
            val reply1 = clientForExplainFuelPolicyCalculation1.browseTours(outbound1)
            explainFuelPolicyCalculationResponse {
                this.fuelPolicy = fuelPolicy {
                    this.effective = request.fuelPolicy.effective
                    this.fuelPolicyId = request.fuelPolicyId
                }
                this.outcome = ExplainFuelPolicyCalculationResponseKt.outcome {
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
            }
        }

    /** Returns a fuel policy. */
    override suspend fun getFuelPolicyRates(request: GetFuelPolicyRatesRequest): GetFuelPolicyRatesResponse {
        return handleGetFuelPolicyRates(request)
    }

    /** Quotes a fuel policy. */
    override suspend fun quoteFuelPolicy(request: QuoteFuelPolicyRequest): QuoteFuelPolicyResponse = handleQuoteFuelPolicy(request)

    /** Recalculates a fuel policy. */
    override suspend fun recalculateFuelPolicies(request: RecalculateFuelPoliciesRequest): RecalculateFuelPoliciesResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyCalculationService/RecalculateFuelPolicies") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecalculateFuelPolicies", request)
            log.atInfo().addKeyValue("operation", "RecalculateFuelPolicies").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicy.fuelPolicyId).log("Recorded audit detail")
            val outbound0 = explainFuelCardDecisionRequest {
                this.context = request.context
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.updatedAt = request.fuelPolicy.audit.updatedAt
                    }
                }
            }
            val reply0 = fuelFuelCardValidationService.explainFuelCardDecision(outbound0)
            val outbound1 = validateFuelCardRequest {
                this.context = request.context
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.updatedAt = request.fuelPolicy.audit.updatedAt
                        this.updatedBy = reply0.fuelCard.audit.updatedBy
                    }
                    this.card = fuelCardCardFunding {
                        this.network = reply0.fuelCard.card.network
                    }
                    this.limits.addAll(listOf(fuelCardLimit {
                        this.amount = request.fuelPolicy.discount.amount
                    }))
                }
            }
            val reply1 = fuelFuelCardValidationService.validateFuelCard(outbound1)
            recalculateFuelPoliciesResponse {
                this.fuelPolicy = fuelPolicy {
                    this.description = request.fuelPolicy.description
                    this.fuelPolicyId = request.fuelPolicyId
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = reply0.result.status
                }
            }
        }

    /** Simulates a fuel policy. */
    override suspend fun simulateFuelPolicy(request: SimulateFuelPolicyRequest): SimulateFuelPolicyResponse {
        return handleSimulateFuelPolicy(request)
    }

    private suspend fun handleCompareFuelPolicyOptions(request: CompareFuelPolicyOptionsRequest): CompareFuelPolicyOptionsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyCalculationService/CompareFuelPolicyOptions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CompareFuelPolicyOptions", request)
            log.atInfo().addKeyValue("operation", "CompareFuelPolicyOptions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicy.fuelPolicyId).log("Recorded audit detail")
            val outbound0 = explainYardMoveCalculationRequest {
                this.context = request.context
                this.details = ExplainYardMoveCalculationRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.fuelPolicy.owner.email
                    }
                }
                this.yardMove = yardMove {
                    this.assignee = userRef {
                        this.userId = request.fuelPolicy.owner.userId
                    }
                    this.audit = auditInfo {
                        this.etag = request.fuelPolicy.audit.etag
                    }
                }
            }
            val invoke0: suspend (ExplainYardMoveCalculationRequest, Metadata) -> ExplainYardMoveCalculationResponse = yardsYardMoveCalculationService::explainYardMoveCalculation
            val reply0 = invoke0(outbound0, Metadata())
            val outbound1 = EstimateFuelCardRequest.newBuilder().apply {
                setContext(request.context)
                setFuelCard(fuelCard {
                    this.audit = auditInfo {
                        this.createdAt = request.fuelPolicy.audit.createdAt
                        this.createdBy = reply0.yardMove.audit.createdBy
                        this.updatedAt = request.fuelPolicy.audit.updatedAt
                        this.updatedBy = reply0.yardMove.audit.updatedBy
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> fuelFuelCardCalculationService.estimateFuelCard(value) }
            compareFuelPolicyOptionsResponse {
                this.fuelPolicy = fuelPolicy {
                    this.fuelPolicyId = request.fuelPolicyId
                    this.version = request.fuelPolicy.version
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply0.outcome.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleEstimateFuelPolicy(request: EstimateFuelPolicyRequest): EstimateFuelPolicyResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyCalculationService/EstimateFuelPolicy") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EstimateFuelPolicy", request)
            log.atInfo().addKeyValue("operation", "EstimateFuelPolicy").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicy.fuelPolicyId).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                resolveCryptoKeysRequest {
                    this.context = request.context
                }
            }
            val reply0 = keymanagementCryptoKeyQueryService.resolveCryptoKeys(outbound0)
            val outbound1 = suggestMetricSeriesesRequest {
                this.context = request.context
                this.filter = SuggestMetricSeriesesRequestKt.filter {
                    this.createdAfter = request.fuelPolicy.audit.createdAt
                    this.region = request.details.destination.region
                }
            }
            val reply1 = ClientRegistry.lookup("acme.metrics.v1.MetricSeriesQueryService", MetricSeriesQueryServiceGrpcKt.MetricSeriesQueryServiceCoroutineStub::class.java).suggestMetricSerieses(outbound1)
            val outbound2 = explainFuelCardCalculationRequest {
                this.context = request.context
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.createdBy = request.fuelPolicy.audit.createdBy
                        this.updatedAt = request.fuelPolicy.audit.updatedAt
                        this.updatedBy = request.fuelPolicy.audit.updatedBy
                    }
                }
            }
            val reply2 = fuelFuelCardCalculationService.explainFuelCardCalculation(WireCopies.reflective(outbound2))
            estimateFuelPolicyResponse {
                this.fuelPolicy = fuelPolicy {
                    this.description = request.fuelPolicy.description
                    this.fuelPolicyId = request.fuelPolicyId
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.referenceIds.addAll(reply2.result.referenceIdsList)
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleGetFuelPolicyRates(request: GetFuelPolicyRatesRequest): GetFuelPolicyRatesResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyCalculationService/GetFuelPolicyRates") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetFuelPolicyRates", request)
            log.atInfo().addKeyValue("operation", "GetFuelPolicyRates").log("Handling transport request")
            val outbound0 = recalculateFuelTransactionsRequest {
                this.context = request.context
                this.fuelTransaction = fuelTransaction {
                    this.correlationId = request.fuelPolicyId
                    this.occurredAt = request.context.deadline
                    this.source = request.context.actor.userId
                    this.subject = customerRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = flowOf(outbound0).map { value -> fuelFuelTransactionCalculationService.recalculateFuelTransactions(value) }.single()
            getFuelPolicyRatesResponse {
                this.fuelPolicy = fuelPolicy {
                    this.fuelPolicyId = request.fuelPolicyId
                }
                this.result = operationResult {
                    this.correlationId = reply0.fuelTransaction.correlationId
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleQuoteFuelPolicy(request: QuoteFuelPolicyRequest): QuoteFuelPolicyResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyCalculationService/QuoteFuelPolicy") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "QuoteFuelPolicy", request)
            log.atInfo().addKeyValue("operation", "QuoteFuelPolicy").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicy.fuelPolicyId).log("Recorded audit detail")
            val outbound0 = scoreSearchIndexRequest {
                this.context = request.context
                this.details = ScoreSearchIndexRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = request.details.destination.countryCode
                        this.region = request.details.destination.region
                    }
                }
                this.searchIndex = searchIndex {
                    this.owner = userRef {
                        this.displayName = request.fuelPolicy.owner.displayName
                    }
                }
            }
            val reply0 = withTimeout(5_000) { searchSearchIndexValidationService.scoreSearchIndex(outbound0) }
            val outbound1 = scoreFuelCardRequest {
                this.context = request.context
                this.details = ScoreFuelCardRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(request.details.destination.linesList)
                        this.recipient = when (reply0.searchIndex.locatorCase) {
                            SearchIndex.LocatorCase.ADDRESS -> reply0.searchIndex.address.recipient
                            else -> ""
                        }
                    }
                }
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.createdBy = request.fuelPolicy.audit.createdBy
                    }
                }
            }
            val reply1 = fuelFuelCardValidationService.scoreFuelCard(outbound1)
            quoteFuelPolicyResponse {
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.searchIndex.audit.updatedAt
                    }
                    this.fuelPolicyId = request.fuelPolicyId
                    this.priority = request.fuelPolicy.priority
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSimulateFuelPolicy(request: SimulateFuelPolicyRequest): SimulateFuelPolicyResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyCalculationService/SimulateFuelPolicy") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SimulateFuelPolicy", request)
            log.atInfo().addKeyValue("operation", "SimulateFuelPolicy").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicy.fuelPolicyId).log("Recorded audit detail")
            val outbound0 = batchGetNotificationsRequest {}.notificationsCopy {
                this.context = request.context
            }
            val reply0 = notificationsNotificationService.batchGetNotifications(outbound0)
            simulateFuelPolicyResponse {
                this.fuelPolicy = fuelPolicy {
                    this.description = request.fuelPolicy.description
                    this.fuelPolicyId = request.fuelPolicyId
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }
}
