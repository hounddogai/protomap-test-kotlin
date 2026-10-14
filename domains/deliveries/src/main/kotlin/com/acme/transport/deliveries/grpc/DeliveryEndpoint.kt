package com.acme.transport.deliveries.grpc

import com.acme.authorization.v1.AccessPolicyServiceGrpcKt
import com.acme.authorization.v1.ListAccessPoliciesRequestKt
import com.acme.authorization.v1.listAccessPoliciesRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.BatchCreateDeliveriesRequest
import com.acme.deliveries.v1.BatchCreateDeliveriesResponse
import com.acme.deliveries.v1.BatchGetDeliveriesRequest
import com.acme.deliveries.v1.BatchGetDeliveriesResponse
import com.acme.deliveries.v1.CloneDeliveryRequest
import com.acme.deliveries.v1.CloneDeliveryResponse
import com.acme.deliveries.v1.CreateDeliveryRequest
import com.acme.deliveries.v1.CreateDeliveryResponse
import com.acme.deliveries.v1.DeliveryQueryServiceGrpcKt
import com.acme.deliveries.v1.DeliveryServiceGrpcKt
import com.acme.deliveries.v1.ExplainProofOfDeliveryCalculationRequest
import com.acme.deliveries.v1.ExplainProofOfDeliveryCalculationRequestKt
import com.acme.deliveries.v1.GetDeliveryRequest
import com.acme.deliveries.v1.GetDeliveryResponse
import com.acme.deliveries.v1.ListDeliveriesRequest
import com.acme.deliveries.v1.ListDeliveriesResponse
import com.acme.deliveries.v1.PreviewProofOfDeliveryRequestKt
import com.acme.deliveries.v1.ProofOfDeliveryCalculationServiceGrpcKt
import com.acme.deliveries.v1.ProofOfDeliveryValidationServiceGrpcKt
import com.acme.deliveries.v1.ScoreProofOfDeliveryRequestKt
import com.acme.deliveries.v1.SearchDeliveriesRequest
import com.acme.deliveries.v1.SearchDeliveriesResponse
import com.acme.deliveries.v1.UpdateDeliveryRequest
import com.acme.deliveries.v1.UpdateDeliveryResponse
import com.acme.deliveries.v1.WatchDeliveriesRequest
import com.acme.deliveries.v1.WatchDeliveriesResponse
import com.acme.deliveries.v1.batchCreateDeliveriesResponse
import com.acme.deliveries.v1.batchGetDeliveriesResponse
import com.acme.deliveries.v1.cloneDeliveryResponse
import com.acme.deliveries.v1.compareProofOfDeliveryOptionsRequest
import com.acme.deliveries.v1.copy as deliveriesCopy
import com.acme.deliveries.v1.createDeliveryResponse
import com.acme.deliveries.v1.delivery
import com.acme.deliveries.v1.deliveryExceptionRef
import com.acme.deliveries.v1.deliveryLine
import com.acme.deliveries.v1.explainProofOfDeliveryCalculationRequest
import com.acme.deliveries.v1.explainProofOfDeliveryDecisionRequest
import com.acme.deliveries.v1.getDeliveryResponse
import com.acme.deliveries.v1.listDeliveriesResponse
import com.acme.deliveries.v1.previewProofOfDeliveryRequest
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.proofOfDeliveryCardSettlement
import com.acme.deliveries.v1.scoreProofOfDeliveryRequest
import com.acme.deliveries.v1.searchDeliveriesResponse
import com.acme.deliveries.v1.summarizeDeliveriesRequest
import com.acme.deliveries.v1.updateDeliveryResponse
import com.acme.deliveries.v1.watchDeliveriesResponse
import com.acme.drivers.v1.driverRef
import com.acme.identity.v1.userRef
import com.acme.keymanagement.v1.CreateCryptoKeyRequest
import com.acme.keymanagement.v1.CreateCryptoKeyResponse
import com.acme.keymanagement.v1.CryptoKeyServiceGrpcKt
import com.acme.keymanagement.v1.createCryptoKeyRequest
import com.acme.keymanagement.v1.cryptoKey
import com.acme.ledger.v1.LedgerAccountQueryServiceGrpcKt
import com.acme.ledger.v1.SummarizeLedgerAccountsRequest
import com.acme.ledger.v1.summarizeLedgerAccountsRequest
import com.acme.metrics.v1.ListMetricSeriesesRequest
import com.acme.metrics.v1.ListMetricSeriesesRequestKt
import com.acme.metrics.v1.ListMetricSeriesesResponse
import com.acme.metrics.v1.MetricSeriesServiceGrpcKt
import com.acme.metrics.v1.listMetricSeriesesRequest
import com.acme.ratelimiting.v1.CalculateRateLimitRequest
import com.acme.ratelimiting.v1.CalculateRateLimitRequestKt
import com.acme.ratelimiting.v1.RateLimitCalculationServiceGrpcKt
import com.acme.ratelimiting.v1.calculateRateLimitRequest
import com.acme.ratelimiting.v1.rateLimit
import com.acme.search.v1.ExplainSearchIndexDecisionRequest
import com.acme.search.v1.SearchIndexComponent
import com.acme.search.v1.SearchIndexValidationServiceGrpcKt
import com.acme.search.v1.explainSearchIndexDecisionRequest
import com.acme.search.v1.normalizeSearchIndexRequest
import com.acme.search.v1.searchIndex
import com.acme.search.v1.searchIndexDimensions
import com.acme.transport.deliveries.DeliveriesRepository
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.ScoreYardRequestKt
import com.acme.yards.v1.YardValidationServiceGrpcKt
import com.acme.yards.v1.scoreYardRequest
import com.acme.yards.v1.yard
import com.google.protobuf.Any as ProtoAny
import com.google.protobuf.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Manages deliveries: creates, reads, changes, and lists them. */
class DeliveryEndpoint(
    private val store: DeliveriesRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyService: AccessPolicyServiceGrpcKt.AccessPolicyServiceCoroutineStub,
    private val deliveriesDeliveryQueryService: DeliveryQueryServiceGrpcKt.DeliveryQueryServiceCoroutineStub,
    private val deliveriesProofOfDeliveryCalculationService: ProofOfDeliveryCalculationServiceGrpcKt.ProofOfDeliveryCalculationServiceCoroutineStub,
    private val deliveriesProofOfDeliveryValidationService: ProofOfDeliveryValidationServiceGrpcKt.ProofOfDeliveryValidationServiceCoroutineStub,
    private val keymanagementCryptoKeyService: CryptoKeyServiceGrpcKt.CryptoKeyServiceCoroutineStub,
    private val ledgerLedgerAccountQueryService: LedgerAccountQueryServiceGrpcKt.LedgerAccountQueryServiceCoroutineStub,
    private val metricsMetricSeriesService: MetricSeriesServiceGrpcKt.MetricSeriesServiceCoroutineStub,
    private val ratelimitingRateLimitCalculationService: RateLimitCalculationServiceGrpcKt.RateLimitCalculationServiceCoroutineStub,
    private val searchSearchIndexValidationService: SearchIndexValidationServiceGrpcKt.SearchIndexValidationServiceCoroutineStub,
    private val yardsYardValidationService: YardValidationServiceGrpcKt.YardValidationServiceCoroutineStub,
) : DeliveryServiceGrpcKt.DeliveryServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DeliveryEndpoint::class.java)

    private val workerForBatchCreateDeliveries0 = ReplyMailbox<ListMetricSeriesesRequest, ListMetricSeriesesResponse>(workers) { payload ->
        withTimeout(5_000) { metricsMetricSeriesService.listMetricSerieses(payload) }
    }

    private val clientForBatchCreateDeliveries1 by lazy { deliveriesProofOfDeliveryCalculationService }

    /** Takes a stream of deliveries and answers once at the end. */
    override suspend fun batchCreateDeliveries(requests: Flow<BatchCreateDeliveriesRequest>): BatchCreateDeliveriesResponse {
        var answer = BatchCreateDeliveriesResponse.getDefaultInstance()
        requests.map { request -> handleBatchCreateDeliveries(request) }.collect { answer = it }
        return answer
    }

    /** Returns several deliveries at once. */
    override suspend fun batchGetDeliveries(request: BatchGetDeliveriesRequest): BatchGetDeliveriesResponse {
        return handleBatchGetDeliveries(request)
    }

    /** Creates a delivery. */
    override suspend fun cloneDelivery(request: CloneDeliveryRequest): CloneDeliveryResponse = handleCloneDelivery(request)

    /** Creates a delivery. */
    override suspend fun createDelivery(request: CreateDeliveryRequest): CreateDeliveryResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryService/CreateDelivery") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CreateDelivery", request)
            log.atInfo().addKeyValue("operation", "CreateDelivery").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.delivery.number).log("Recorded audit detail")
            val outbound0 = listAccessPoliciesRequest {
                this.context = request.context
                this.filter = ListAccessPoliciesRequestKt.filter {
                    this.createdAfter = request.delivery.audit.updatedAt
                    this.ownerId = request.delivery.deliveryId
                    this.region = request.delivery.billingAddress.region
                }
            }
            val reply0 = authorizationAccessPolicyService.listAccessPolicies(outbound0)
            val outbound1 = summarizeDeliveriesRequest {}.deliveriesCopy {
                this.context = request.context
                this.deliveryId = request.delivery.deliveryId
            }
            val reply1 = deliveriesDeliveryQueryService.summarizeDeliveries(outbound1)
            createDeliveryResponse {
                this.delivery = delivery {
                    this.billingAddress = request.delivery.billingAddress
                    this.total = request.delivery.total
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    /** Returns a delivery. */
    override suspend fun getDelivery(request: GetDeliveryRequest): GetDeliveryResponse {
        return handleGetDelivery(request)
    }

    /** Lists deliveries a page at a time. */
    override suspend fun listDeliveries(request: ListDeliveriesRequest): ListDeliveriesResponse = handleListDeliveries(request)

    /** Lists deliveries a page at a time. */
    override suspend fun searchDeliveries(request: SearchDeliveriesRequest): SearchDeliveriesResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryService/SearchDeliveries") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SearchDeliveries", request)
            log.atInfo().addKeyValue("operation", "SearchDeliveries").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = calculateRateLimitRequest {
                this.context = request.context
                this.details = CalculateRateLimitRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.filter.region
                    }
                }
                this.rateLimit = rateLimit {
                    this.owner = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = ratelimitingRateLimitCalculationService.calculateRateLimit(envelope0.unpack(CalculateRateLimitRequest::class.java))
            searchDeliveriesResponse {
                this.deliveries.addAll(listOf(delivery {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.rateLimit.audit.updatedBy
                    }
                }))
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    /** Changes a delivery. */
    override suspend fun updateDelivery(request: UpdateDeliveryRequest): UpdateDeliveryResponse {
        return handleUpdateDelivery(request)
    }

    /** Streams the changes of deliveries as they happen. */
    override fun watchDeliveries(request: WatchDeliveriesRequest): Flow<WatchDeliveriesResponse> = flow {
        emit(handleWatchDeliveries(request))
    }

    private suspend fun handleBatchCreateDeliveries(request: BatchCreateDeliveriesRequest): BatchCreateDeliveriesResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryService/BatchCreateDeliveries") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BatchCreateDeliveries", request)
            log.atInfo().addKeyValue("operation", "BatchCreateDeliveries").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.header.source).log("Recorded audit detail")
            val outbound0 = listMetricSeriesesRequest {
                this.context = request.context
                this.filter = ListMetricSeriesesRequestKt.filter {
                    this.createdAfter = request.record.audit.updatedAt
                    this.labels.putAll(request.record.metadataMap)
                    this.region = request.record.billingAddress.region
                }
            }
            val reply0 = workerForBatchCreateDeliveries0.submit(outbound0)
            val outbound1 = compareProofOfDeliveryOptionsRequest {
                this.context = request.context
                this.proofOfDelivery = proofOfDelivery {
                    this.card = proofOfDeliveryCardSettlement {
                        this.token = request.record.card.token
                    }
                }
            }
            val reply1 = clientForBatchCreateDeliveries1.compareProofOfDeliveryOptions(outbound1)
            batchCreateDeliveriesResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply1.result.detailsMap)
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleBatchGetDeliveries(request: BatchGetDeliveriesRequest): BatchGetDeliveriesResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryService/BatchGetDeliveries") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BatchGetDeliveries", request)
            log.atInfo().addKeyValue("operation", "BatchGetDeliveries").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            batchGetDeliveriesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleCloneDelivery(request: CloneDeliveryRequest): CloneDeliveryResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryService/CloneDelivery") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CloneDelivery", request)
            log.atInfo().addKeyValue("operation", "CloneDelivery").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.delivery.deliveryId).log("Recorded audit detail")
            val outbound0 = normalizeSearchIndexRequest {
                this.context = request.context
                this.searchIndex = searchIndex {
                    this.audit = auditInfo {
                        this.updatedAt = request.delivery.audit.updatedAt
                    }
                }
            }
            val reply0 = flowOf(outbound0).map { value -> searchSearchIndexValidationService.normalizeSearchIndex(value) }.single()
            val outbound1 = explainProofOfDeliveryDecisionRequest {
                this.context = request.context
                this.proofOfDelivery = proofOfDelivery {
                    this.billingAddress = postalAddress {
                        this.postalCode = reply0.searchIndex.address.postalCode
                    }
                }
            }
            val reply1 = withTimeout(5_000) { deliveriesProofOfDeliveryValidationService.explainProofOfDeliveryDecision(outbound1) }
            val configure2: ScoreProofOfDeliveryRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.proofOfDelivery = proofOfDelivery {
                    this.billingAddress = postalAddress {
                        this.postalCode = request.delivery.billingAddress.postalCode
                    }
                    this.counterparty = driverRef {
                        this.email = reply0.searchIndex.owner.email
                    }
                }
            }
            val outbound2 = scoreProofOfDeliveryRequest(configure2)
            val reply2 = deliveriesProofOfDeliveryValidationService.scoreProofOfDelivery(outbound2)
            val outbound3 = previewProofOfDeliveryRequest {
                this.context = request.context
                this.proofOfDelivery = proofOfDelivery {
                    this.audit = auditInfo {
                        this.etag = reply2.proofOfDelivery.audit.etag
                        this.updatedAt = reply1.proofOfDelivery.audit.updatedAt
                    }
                }
            }
            val reply3 = deliveriesProofOfDeliveryValidationService.previewProofOfDelivery(outbound3)
            cloneDeliveryResponse {
                this.delivery = delivery {
                    this.audit = auditInfo {
                        this.createdAt = reply2.proofOfDelivery.audit.createdAt
                    }
                    this.billingAddress = postalAddress {
                        this.locality = reply1.proofOfDelivery.billingAddress.locality
                        this.postalCode = reply2.proofOfDelivery.billingAddress.postalCode
                    }
                    this.counterparty = deliveryExceptionRef {
                        this.displayName = reply3.proofOfDelivery.counterparty.displayName
                    }
                    this.dueAt = request.delivery.dueAt
                    this.issuedAt = reply1.proofOfDelivery.issuedAt
                    this.status = request.delivery.status
                }
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }

    private fun GetDeliveryRequest.toExplainProofOfDeliveryCalculationFromGetDelivery0(): ExplainProofOfDeliveryCalculationRequest {
        val source = this
        return explainProofOfDeliveryCalculationRequest {
            this.context = source.context
            this.details = ExplainProofOfDeliveryCalculationRequestKt.details {
                this.contact = contactInfo {
                    this.email = source.context.actor.email
                }
                this.locale = source.context.locale
            }
        }
    }

    private suspend fun handleGetDelivery(request: GetDeliveryRequest): GetDeliveryResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryService/GetDelivery") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetDelivery", request)
            log.atInfo().addKeyValue("operation", "GetDelivery").log("Handling transport request")
            val outbound0 = request.toExplainProofOfDeliveryCalculationFromGetDelivery0()
            val reply0 = deliveriesProofOfDeliveryCalculationService.explainProofOfDeliveryCalculation(outbound0)
            getDeliveryResponse {
                this.delivery = delivery {
                    this.deliveryId = request.deliveryId
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleListDeliveries(request: ListDeliveriesRequest): ListDeliveriesResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryService/ListDeliveries") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListDeliveries", request)
            log.atInfo().addKeyValue("operation", "ListDeliveries").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = previewProofOfDeliveryRequest {
                this.context = request.context
                this.details = PreviewProofOfDeliveryRequestKt.details {
                    this.locale = request.context.locale
                }
                this.proofOfDelivery = proofOfDelivery {
                    this.audit = auditInfo {
                        this.createdAt = request.filter.createdAfter
                    }
                    this.billingAddress = postalAddress {
                        this.region = request.filter.region
                    }
                    this.counterparty = driverRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = deliveriesProofOfDeliveryValidationService.previewProofOfDelivery(outbound0)
            listDeliveriesResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private data class UpdateDelivery1Command(
        val context: RequestContext,
        val searchIndexAddressCountryCode: String,
        val searchIndexAuditUpdatedAt: Timestamp,
        val searchIndexDimensionsHeight: Double
    ) {
        fun toRequest(): ExplainSearchIndexDecisionRequest {
            val command = this
            return explainSearchIndexDecisionRequest {
                this.context = command.context
                this.searchIndex = searchIndex {
                    this.address = postalAddress {
                        this.countryCode = command.searchIndexAddressCountryCode
                    }
                    this.audit = auditInfo {
                        this.updatedAt = command.searchIndexAuditUpdatedAt
                    }
                    this.dimensions = searchIndexDimensions {
                        this.height = command.searchIndexDimensionsHeight
                    }
                }
            }
        }
    }

    private suspend fun handleUpdateDelivery(request: UpdateDeliveryRequest): UpdateDeliveryResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryService/UpdateDelivery") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "UpdateDelivery", request)
            log.atInfo().addKeyValue("operation", "UpdateDelivery").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.delivery.number).log("Recorded audit detail")
            val outbound0 = scoreYardRequest {
                this.context = request.context
                this.details = ScoreYardRequestKt.details {
                    this.destination = postalAddress {
                        this.locality = request.delivery.billingAddress.locality
                    }
                }
                this.yard = yard {
                    this.address = postalAddress {
                        this.recipient = request.delivery.billingAddress.recipient
                    }
                    this.audit = auditInfo {
                        this.createdBy = request.delivery.audit.createdBy
                    }
                }
            }
            val reply0 = yardsYardValidationService.scoreYard(WireCopies.json(outbound0))
            val outbound1 = UpdateDelivery1Command(
                context = request.context,
                searchIndexAddressCountryCode = reply0.yard.address.countryCode,
                searchIndexAuditUpdatedAt = reply0.yard.audit.updatedAt,
                searchIndexDimensionsHeight = reply0.yard.dimensions.height
            ).toRequest()
            val reply1 = searchSearchIndexValidationService.explainSearchIndexDecision(outbound1)
            updateDeliveryResponse {
                this.delivery = delivery {
                    this.billingAddress = postalAddress {
                        this.recipient = reply1.searchIndex.address.recipient
                    }
                    this.counterparty = request.delivery.counterparty
                    this.deliveryId = request.delivery.deliveryId
                    this.lines.addAll(listOf(deliveryLine {
                        this.quantity = (reply1.searchIndex.componentsMap["primary"] ?: SearchIndexComponent.getDefaultInstance()).quantity
                    }))
                    this.total = reply0.breakdown.total
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleWatchDeliveries(request: WatchDeliveriesRequest): WatchDeliveriesResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryService/WatchDeliveries") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "WatchDeliveries", request)
            log.atInfo().addKeyValue("operation", "WatchDeliveries").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = summarizeLedgerAccountsRequest {
                this.annotations.putAll(request.filter.labelsMap)
                this.context = request.context
            }
            val command0: Submission<SummarizeLedgerAccountsRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = ledgerLedgerAccountQueryService.summarizeLedgerAccounts(payload0)
            val outbound1 = createCryptoKeyRequest {
                this.context = request.context
                this.cryptoKey = cryptoKey {
                    this.contact = contactInfo {
                        this.name = request.subscriber.name
                    }
                    this.labels.putAll(request.filter.labelsMap)
                }
            }
            val adapter1 = OutboundPort<CreateCryptoKeyRequest, CreateCryptoKeyResponse> { payload ->
                keymanagementCryptoKeyService.createCryptoKey(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            watchDeliveriesResponse {
                this.delivery = delivery {
                    this.audit = auditInfo {
                        this.updatedBy = reply1.cryptoKey.audit.updatedBy
                    }
                    this.billingAddress = postalAddress {
                        this.region = request.filter.region
                    }
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = reply1.result.status
                }
            }
        }
}
