package com.acme.transport.deliveries.api

import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.CalculateProofOfDeliveryRequestKt
import com.acme.deliveries.v1.CalculateProofOfDeliveryResponse
import com.acme.deliveries.v1.CheckDeliveryEligibilityRequest
import com.acme.deliveries.v1.CheckDeliveryEligibilityResponse
import com.acme.deliveries.v1.CheckDeliveryEligibilityResponseKt
import com.acme.deliveries.v1.CompareProofOfDeliveryOptionsRequestKt
import com.acme.deliveries.v1.Delivery
import com.acme.deliveries.v1.DeliveryLine
import com.acme.deliveries.v1.DeliveryValidationServiceGrpcKt
import com.acme.deliveries.v1.EstimateProofOfDeliveryRequestKt
import com.acme.deliveries.v1.EstimateProofOfDeliveryResponse
import com.acme.deliveries.v1.ExplainDeliveryDecisionRequest
import com.acme.deliveries.v1.ExplainDeliveryDecisionResponse
import com.acme.deliveries.v1.ExplainDeliveryDecisionResponseKt
import com.acme.deliveries.v1.ExplainProofOfDeliveryCalculationRequest
import com.acme.deliveries.v1.ExplainProofOfDeliveryCalculationRequestKt
import com.acme.deliveries.v1.ExplainProofOfDeliveryCalculationResponse
import com.acme.deliveries.v1.GetDeliveryRulesRequest
import com.acme.deliveries.v1.GetDeliveryRulesResponse
import com.acme.deliveries.v1.LintDeliveriesRequest
import com.acme.deliveries.v1.LintDeliveriesResponse
import com.acme.deliveries.v1.NormalizeDeliveryRequest
import com.acme.deliveries.v1.NormalizeDeliveryResponse
import com.acme.deliveries.v1.PreviewDeliveryRequest
import com.acme.deliveries.v1.PreviewDeliveryResponse
import com.acme.deliveries.v1.ProofOfDeliveryCalculationServiceGrpcKt
import com.acme.deliveries.v1.QuoteProofOfDeliveryRequestKt
import com.acme.deliveries.v1.ScoreDeliveryRequest
import com.acme.deliveries.v1.ScoreDeliveryResponse
import com.acme.deliveries.v1.SimulateProofOfDeliveryRequestKt
import com.acme.deliveries.v1.SimulateProofOfDeliveryResponse
import com.acme.deliveries.v1.ValidateDeliveryRequest
import com.acme.deliveries.v1.ValidateDeliveryResponse
import com.acme.deliveries.v1.ValidateDeliveryResponseKt
import com.acme.deliveries.v1.calculateProofOfDeliveryRequest
import com.acme.deliveries.v1.checkDeliveryEligibilityResponse
import com.acme.deliveries.v1.compareProofOfDeliveryOptionsRequest
import com.acme.deliveries.v1.copy as deliveriesCopy
import com.acme.deliveries.v1.delivery
import com.acme.deliveries.v1.estimateProofOfDeliveryRequest
import com.acme.deliveries.v1.explainDeliveryDecisionResponse
import com.acme.deliveries.v1.explainProofOfDeliveryCalculationRequest
import com.acme.deliveries.v1.getDeliveryRulesResponse
import com.acme.deliveries.v1.lintDeliveriesResponse
import com.acme.deliveries.v1.normalizeDeliveryResponse
import com.acme.deliveries.v1.previewDeliveryResponse
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.proofOfDeliveryAttachment
import com.acme.deliveries.v1.proofOfDeliveryCardSettlement
import com.acme.deliveries.v1.proofOfDeliveryLine
import com.acme.deliveries.v1.proofOfDeliveryLineTax
import com.acme.deliveries.v1.quoteProofOfDeliveryRequest
import com.acme.deliveries.v1.recalculateProofOfDeliveriesRequest
import com.acme.deliveries.v1.scoreDeliveryResponse
import com.acme.deliveries.v1.simulateProofOfDeliveryRequest
import com.acme.deliveries.v1.validateDeliveryResponse
import com.acme.drivers.v1.driverRef
import com.acme.metrics.v1.MetricSeriesServiceGrpcKt
import com.acme.metrics.v1.SearchMetricSeriesesRequest
import com.acme.metrics.v1.SearchMetricSeriesesRequestKt
import com.acme.metrics.v1.SearchMetricSeriesesResponse
import com.acme.metrics.v1.searchMetricSeriesesRequest
import com.acme.performers.v1.ListPerformersRequest
import com.acme.performers.v1.ListPerformersRequestKt
import com.acme.performers.v1.ListPerformersResponse
import com.acme.performers.v1.PerformerServiceGrpcKt
import com.acme.performers.v1.listPerformersRequest
import com.acme.transport.deliveries.DeliveriesRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.ExportYardsRequestKt
import com.acme.yards.v1.ScoreYardRequest
import com.acme.yards.v1.ScoreYardRequestKt
import com.acme.yards.v1.YardQueryServiceGrpcKt
import com.acme.yards.v1.YardValidationServiceGrpcKt
import com.acme.yards.v1.exportYardsRequest
import com.acme.yards.v1.scoreYardRequest
import com.acme.yards.v1.validateYardRequest
import com.acme.yards.v1.yard
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

/** Checks deliveries against the rules before anything acts on them. */
class DeliveryValidationGrpcService(
    private val store: DeliveriesRepository,
    private val workers: CoroutineScope,
    private val deliveriesProofOfDeliveryCalculationService: ProofOfDeliveryCalculationServiceGrpcKt.ProofOfDeliveryCalculationServiceCoroutineStub,
    private val metricsMetricSeriesService: MetricSeriesServiceGrpcKt.MetricSeriesServiceCoroutineStub,
    private val performersPerformerService: PerformerServiceGrpcKt.PerformerServiceCoroutineStub,
    private val yardsYardQueryService: YardQueryServiceGrpcKt.YardQueryServiceCoroutineStub,
    private val yardsYardValidationService: YardValidationServiceGrpcKt.YardValidationServiceCoroutineStub,
) : DeliveryValidationServiceGrpcKt.DeliveryValidationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DeliveryValidationGrpcService::class.java)

    private val invokeCheckDeliveryEligibility0: suspend (SearchMetricSeriesesRequest) -> SearchMetricSeriesesResponse = { payload ->
        metricsMetricSeriesService.searchMetricSerieses(payload)
    }

    private val workerForGetDeliveryRules0 = ReplyMailbox<ExplainProofOfDeliveryCalculationRequest, ExplainProofOfDeliveryCalculationResponse>(workers) { payload ->
        withTimeout(5_000) { deliveriesProofOfDeliveryCalculationService.explainProofOfDeliveryCalculation(payload) }
    }

    private val clientForGetDeliveryRules1 by lazy { deliveriesProofOfDeliveryCalculationService }

    /** Checks a delivery. */
    override suspend fun checkDeliveryEligibility(request: CheckDeliveryEligibilityRequest): CheckDeliveryEligibilityResponse {
        return handleCheckDeliveryEligibility(request)
    }

    /** Explains a delivery. */
    override suspend fun explainDeliveryDecision(request: ExplainDeliveryDecisionRequest): ExplainDeliveryDecisionResponse = handleExplainDeliveryDecision(request)

    /** Returns a delivery. */
    override suspend fun getDeliveryRules(request: GetDeliveryRulesRequest): GetDeliveryRulesResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryValidationService/GetDeliveryRules") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetDeliveryRules", request)
            log.atInfo().addKeyValue("operation", "GetDeliveryRules").log("Handling transport request")
            val outbound0 = explainProofOfDeliveryCalculationRequest {
                this.context = request.context
                this.details = ExplainProofOfDeliveryCalculationRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                    this.locale = request.context.locale
                }
                this.proofOfDelivery = proofOfDelivery {
                    this.audit = auditInfo {
                        this.createdAt = request.context.deadline
                    }
                }
            }
            val reply0 = workerForGetDeliveryRules0.submit(outbound0)
            val outbound1 = estimateProofOfDeliveryRequest {
                this.context = request.context
                this.details = EstimateProofOfDeliveryRequestKt.details {
                    this.destination = postalAddress {
                        this.postalCode = reply0.proofOfDelivery.billingAddress.postalCode
                    }
                }
                this.proofOfDelivery = proofOfDelivery {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.proofOfDelivery.audit.updatedAt
                    }
                }
            }
            val reply1 = clientForGetDeliveryRules1.estimateProofOfDelivery(outbound1)
            getDeliveryRulesResponse {
                this.delivery = delivery {
                    this.billingAddress = postalAddress {
                        this.recipient = reply0.proofOfDelivery.billingAddress.recipient
                    }
                    this.deliveryId = request.deliveryId
                    this.metadata.putAll(reply1.proofOfDelivery.metadataMap)
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    /** Lints a delivery. */
    override suspend fun lintDeliveries(request: LintDeliveriesRequest): LintDeliveriesResponse {
        return handleLintDeliveries(request)
    }

    /** Normalizes a delivery. */
    override suspend fun normalizeDelivery(request: NormalizeDeliveryRequest): NormalizeDeliveryResponse = handleNormalizeDelivery(request)

    /** Previews a delivery. */
    override suspend fun previewDelivery(request: PreviewDeliveryRequest): PreviewDeliveryResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryValidationService/PreviewDelivery") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PreviewDelivery", request)
            log.atInfo().addKeyValue("operation", "PreviewDelivery").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.delivery.deliveryId).log("Recorded audit detail")
            val outbound0 = calculateProofOfDeliveryRequest {
                this.context = request.context
                this.details = CalculateProofOfDeliveryRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.delivery.billingAddress.region
                    }
                }
                this.proofOfDelivery = proofOfDelivery {
                    this.audit = auditInfo {
                        this.etag = request.delivery.audit.etag
                    }
                }
            }
            val reply0 = deliveriesProofOfDeliveryCalculationService.calculateProofOfDelivery(outbound0)
            val outbound1 = compareProofOfDeliveryOptionsRequest {}.deliveriesCopy {
                this.context = request.context
                this.details = CompareProofOfDeliveryOptionsRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = reply0.proofOfDelivery.billingAddress.countryCode
                        this.recipient = reply0.proofOfDelivery.billingAddress.recipient
                    }
                }
            }
            val reply1 = deliveriesProofOfDeliveryCalculationService.compareProofOfDeliveryOptions(outbound1)
            previewDeliveryResponse {
                this.delivery = delivery {
                    this.billingAddress = postalAddress {
                        this.lines.addAll(reply0.proofOfDelivery.billingAddress.linesList)
                        this.region = reply1.proofOfDelivery.billingAddress.region
                    }
                    this.deliveryId = request.deliveryId
                    this.status = request.delivery.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Scores a delivery. */
    override suspend fun scoreDelivery(request: ScoreDeliveryRequest): ScoreDeliveryResponse {
        return handleScoreDelivery(request)
    }

    /** Validates a delivery. */
    override suspend fun validateDelivery(request: ValidateDeliveryRequest): ValidateDeliveryResponse = handleValidateDelivery(request)

    private suspend fun handleCheckDeliveryEligibility(request: CheckDeliveryEligibilityRequest): CheckDeliveryEligibilityResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryValidationService/CheckDeliveryEligibility") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CheckDeliveryEligibility", request)
            log.atInfo().addKeyValue("operation", "CheckDeliveryEligibility").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.delivery.deliveryId).log("Recorded audit detail")
            val outbound0 = searchMetricSeriesesRequest {
                this.context = request.context
                this.filter = SearchMetricSeriesesRequestKt.filter {
                    this.region = request.delivery.billingAddress.region
                }
            }
            val reply0 = invokeCheckDeliveryEligibility0(outbound0)
            val outbound1 = listPerformersRequest {
                this.context = request.context
                this.filter = ListPerformersRequestKt.filter {
                    this.createdAfter = request.delivery.dueAt
                    this.labels.putAll(reply0.result.detailsMap)
                    this.ownerId = request.delivery.counterparty.deliveryExceptionId
                    this.region = request.delivery.billingAddress.region
                }
            }
            val invoke1: suspend (ListPerformersRequest, Metadata) -> ListPerformersResponse = performersPerformerService::listPerformers
            val reply1 = invoke1(outbound1, Metadata())
            val outbound2 = ExplainProofOfDeliveryCalculationRequest.newBuilder().apply {
                setContext(request.context)
                setProofOfDelivery(proofOfDelivery {
                    this.billingAddress = postalAddress {
                        this.locality = request.delivery.billingAddress.locality
                    }
                    this.counterparty = driverRef {
                        this.displayName = request.delivery.counterparty.displayName
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply2 = outbound2.let { value -> deliveriesProofOfDeliveryCalculationService.explainProofOfDeliveryCalculation(value) }
            checkDeliveryEligibilityResponse {
                this.delivery = delivery {
                    this.billingAddress = postalAddress {
                        this.recipient = reply2.proofOfDelivery.billingAddress.recipient
                    }
                    this.deliveryId = request.deliveryId
                    this.dueAt = request.delivery.dueAt
                }
                this.outcome = CheckDeliveryEligibilityResponseKt.outcome {
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.status = reply1.result.status
                }
            }
        }

    private suspend fun handleExplainDeliveryDecision(request: ExplainDeliveryDecisionRequest): ExplainDeliveryDecisionResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryValidationService/ExplainDeliveryDecision") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainDeliveryDecision", request)
            log.atInfo().addKeyValue("operation", "ExplainDeliveryDecision").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.delivery.deliveryId).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                simulateProofOfDeliveryRequest {
                    this.context = request.context
                    this.details = SimulateProofOfDeliveryRequestKt.details {
                        this.destination = postalAddress {
                            this.lines.addAll(request.delivery.billingAddress.linesList)
                        }
                    }
                }
            }
            val reply0 = deliveriesProofOfDeliveryCalculationService.simulateProofOfDelivery(outbound0)
            val outbound1 = recalculateProofOfDeliveriesRequest {
                this.context = request.context
                this.proofOfDelivery = proofOfDelivery {
                    this.audit = auditInfo {
                        this.createdAt = reply0.proofOfDelivery.audit.createdAt
                    }
                    this.billingAddress = postalAddress {
                        this.locality = reply0.proofOfDelivery.billingAddress.locality
                        this.postalCode = reply0.proofOfDelivery.billingAddress.postalCode
                    }
                }
            }
            val reply1 = ClientRegistry.lookup("acme.deliveries.v1.ProofOfDeliveryCalculationService", ProofOfDeliveryCalculationServiceGrpcKt.ProofOfDeliveryCalculationServiceCoroutineStub::class.java).recalculateProofOfDeliveries(outbound1)
            var reply2 = EstimateProofOfDeliveryResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.delivery.linesList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound2 = estimateProofOfDeliveryRequest {
                                this.context = request.context
                                this.proofOfDelivery = proofOfDelivery {
                                    this.lines.addAll(listOf(proofOfDeliveryLine {
                                        this.tax = proofOfDeliveryLineTax {
                                            this.amount = item.amount
                                            this.exemptions.addAll(item.tax.exemptionsList)
                                            this.jurisdiction = item.tax.jurisdiction
                                        }
                                    }))
                                }
                            }
                            deliveriesProofOfDeliveryCalculationService.estimateProofOfDelivery(outbound2)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply2 = it }
                }
            }
            explainDeliveryDecisionResponse {
                this.delivery = delivery {
                    this.billingAddress = postalAddress {
                        this.region = reply0.proofOfDelivery.billingAddress.region
                    }
                    this.deliveryId = request.deliveryId
                    this.dueAt = request.delivery.dueAt
                    this.issuedAt = reply1.proofOfDelivery.issuedAt
                }
                this.outcome = ExplainDeliveryDecisionResponseKt.outcome {
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleLintDeliveries(request: LintDeliveriesRequest): LintDeliveriesResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryValidationService/LintDeliveries") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LintDeliveries", request)
            log.atInfo().addKeyValue("operation", "LintDeliveries").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.delivery.deliveryId).log("Recorded audit detail")
            val outbound0 = quoteProofOfDeliveryRequest {
                this.context = request.context
                this.details = QuoteProofOfDeliveryRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(request.delivery.billingAddress.linesList)
                    }
                }
                this.proofOfDelivery = proofOfDelivery {
                    this.audit = auditInfo {
                        this.updatedBy = request.delivery.audit.updatedBy
                    }
                    this.card = proofOfDeliveryCardSettlement {
                        this.holderName = request.delivery.card.holderName
                    }
                    this.counterparty = driverRef {
                        this.displayName = request.delivery.counterparty.displayName
                    }
                }
            }
            val reply0 = flowOf(outbound0).map { value -> deliveriesProofOfDeliveryCalculationService.quoteProofOfDelivery(value) }.single()
            lintDeliveriesResponse {
                this.delivery = delivery {
                    this.billingAddress = reply0.proofOfDelivery.billingAddress
                    this.deliveryId = request.deliveryId
                    this.notes = request.delivery.notes
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleNormalizeDelivery(request: NormalizeDeliveryRequest): NormalizeDeliveryResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryValidationService/NormalizeDelivery") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "NormalizeDelivery", request)
            log.atInfo().addKeyValue("operation", "NormalizeDelivery").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.delivery.deliveryId).log("Recorded audit detail")
            var reply0 = CalculateProofOfDeliveryResponse.getDefaultInstance()
            request.delivery.attachmentsList.asFlow().map { item ->
                val outbound0 = calculateProofOfDeliveryRequest {
                    this.context = request.context
                    this.proofOfDelivery = proofOfDelivery {
                        this.attachments.addAll(listOf(proofOfDeliveryAttachment {
                            this.checksum = item.checksum
                            this.contentType = item.contentType
                        }))
                    }
                }
                deliveriesProofOfDeliveryCalculationService.calculateProofOfDelivery(outbound0)
            }.collect { reply0 = it }
            val outbound1 = exportYardsRequest {
                this.context = request.context
                this.filter = ExportYardsRequestKt.filter {
                    this.createdAfter = reply0.proofOfDelivery.audit.createdAt
                    this.region = reply0.proofOfDelivery.billingAddress.region
                }
            }
            yardsYardQueryService.exportYards(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound2 = validateYardRequest {
                this.context = request.context
                this.yard = yard {
                    this.address = postalAddress {
                        this.countryCode = reply0.proofOfDelivery.billingAddress.countryCode
                        this.locality = request.delivery.billingAddress.locality
                        this.postalCode = reply0.proofOfDelivery.billingAddress.postalCode
                    }
                    this.audit = auditInfo {
                        this.etag = request.delivery.audit.etag
                    }
                }
            }
            val reply2 = yardsYardValidationService.validateYard(outbound2)
            normalizeDeliveryResponse {
                this.delivery = delivery {
                    this.billingAddress = postalAddress {
                        this.region = reply2.yard.address.region
                    }
                    this.deliveryId = request.deliveryId
                    this.number = request.delivery.number
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleScoreDelivery(request: ScoreDeliveryRequest): ScoreDeliveryResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryValidationService/ScoreDelivery") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScoreDelivery", request)
            log.atInfo().addKeyValue("operation", "ScoreDelivery").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.delivery.deliveryId).log("Recorded audit detail")
            var reply0 = SimulateProofOfDeliveryResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<DeliveryLine>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound0 = simulateProofOfDeliveryRequest {
                            this.context = request.context
                            this.proofOfDelivery = proofOfDelivery {
                                this.lines.addAll(listOf(proofOfDeliveryLine {
                                    this.tax = proofOfDeliveryLineTax {
                                        this.rate = item.tax.rate
                                    }
                                }))
                            }
                        }
                        reply0 = deliveriesProofOfDeliveryCalculationService.simulateProofOfDelivery(outbound0)
                    }
                }
                try {
                    request.delivery.linesList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound1 = recalculateProofOfDeliveriesRequest {
                this.context = request.context
                this.proofOfDelivery = proofOfDelivery {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.proofOfDelivery.audit.updatedBy
                    }
                    this.card = proofOfDeliveryCardSettlement {
                        this.holderName = when (request.delivery.settlementCase) {
                            Delivery.SettlementCase.CARD -> request.delivery.card.holderName
                            else -> ""
                        }
                    }
                }
            }
            val reply1 = deliveriesProofOfDeliveryCalculationService.recalculateProofOfDeliveries(outbound1)
            scoreDeliveryResponse {
                this.delivery = delivery {
                    this.audit = auditInfo {
                        this.etag = reply1.proofOfDelivery.audit.etag
                    }
                    this.counterparty = request.delivery.counterparty
                    this.deliveryId = request.deliveryId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleValidateDelivery(request: ValidateDeliveryRequest): ValidateDeliveryResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryValidationService/ValidateDelivery") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ValidateDelivery", request)
            log.atInfo().addKeyValue("operation", "ValidateDelivery").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.delivery.deliveryId).log("Recorded audit detail")
            val outbound0 = scoreYardRequest {
                this.context = request.context
                this.details = ScoreYardRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.delivery.billingAddress.region
                    }
                }
                this.yard = yard {
                    this.address = postalAddress {
                        this.postalCode = request.delivery.billingAddress.postalCode
                    }
                    this.audit = auditInfo {
                        this.createdAt = request.delivery.audit.createdAt
                    }
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = yardsYardValidationService.scoreYard(envelope0.unpack(ScoreYardRequest::class.java))
            validateDeliveryResponse {
                this.delivery = delivery {
                    this.billingAddress = postalAddress {
                        this.locality = reply0.yard.address.locality
                    }
                    this.deliveryId = request.deliveryId
                    this.number = request.delivery.number
                }
                this.outcome = ValidateDeliveryResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }
}
