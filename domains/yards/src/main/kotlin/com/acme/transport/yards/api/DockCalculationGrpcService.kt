package com.acme.transport.yards.api

import com.acme.authorization.v1.AccessPolicyQueryServiceGrpcKt
import com.acme.authorization.v1.FindAccessPoliciesRequestKt
import com.acme.authorization.v1.findAccessPoliciesRequest
import com.acme.careplans.v1.CarePlanQueryServiceGrpcKt
import com.acme.careplans.v1.SuggestCarePlansRequest
import com.acme.careplans.v1.SuggestCarePlansRequestKt
import com.acme.careplans.v1.suggestCarePlansRequest
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.identity.v1.userRef
import com.acme.keymanagement.v1.CompareCryptoKeyOptionsRequest
import com.acme.keymanagement.v1.CompareCryptoKeyOptionsRequestKt
import com.acme.keymanagement.v1.CompareCryptoKeyOptionsResponse
import com.acme.keymanagement.v1.CryptoKeyCalculationServiceGrpcKt
import com.acme.keymanagement.v1.EstimateCryptoKeyRequestKt
import com.acme.keymanagement.v1.compareCryptoKeyOptionsRequest
import com.acme.keymanagement.v1.cryptoKey
import com.acme.keymanagement.v1.estimateCryptoKeyRequest
import com.acme.ledger.v1.BrowseLedgerAccountsRequest
import com.acme.ledger.v1.BrowseLedgerAccountsRequestKt
import com.acme.ledger.v1.CloneLedgerAccountRequest
import com.acme.ledger.v1.CloneLedgerAccountResponse
import com.acme.ledger.v1.LedgerAccountQueryServiceGrpcKt
import com.acme.ledger.v1.LedgerAccountServiceGrpcKt
import com.acme.ledger.v1.browseLedgerAccountsRequest
import com.acme.ledger.v1.cloneLedgerAccountRequest
import com.acme.ledger.v1.ledgerAccount
import com.acme.ledger.v1.postingBatchRef
import com.acme.ratelimiting.v1.RateLimitCalculationServiceGrpcKt
import com.acme.ratelimiting.v1.SimulateRateLimitRequest
import com.acme.ratelimiting.v1.SimulateRateLimitRequestKt
import com.acme.ratelimiting.v1.SimulateRateLimitResponse
import com.acme.ratelimiting.v1.rateLimit
import com.acme.ratelimiting.v1.simulateRateLimitRequest
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.transport.yards.YardsRepository
import com.acme.venues.v1.VenueServiceGrpcKt
import com.acme.venues.v1.updateVenueRequest
import com.acme.venues.v1.venue
import com.acme.yards.v1.CalculateDockRequest
import com.acme.yards.v1.CalculateDockResponse
import com.acme.yards.v1.CompareDockOptionsRequest
import com.acme.yards.v1.CompareDockOptionsResponse
import com.acme.yards.v1.CompareYardOptionsRequest
import com.acme.yards.v1.CompareYardOptionsRequestKt
import com.acme.yards.v1.CompareYardOptionsResponse
import com.acme.yards.v1.DockCalculationServiceGrpcKt
import com.acme.yards.v1.DockComponent
import com.acme.yards.v1.EstimateDockRequest
import com.acme.yards.v1.EstimateDockResponse
import com.acme.yards.v1.EstimateYardResponse
import com.acme.yards.v1.ExplainDockCalculationRequest
import com.acme.yards.v1.ExplainDockCalculationResponse
import com.acme.yards.v1.GetDockRatesRequest
import com.acme.yards.v1.GetDockRatesResponse
import com.acme.yards.v1.QuoteDockRequest
import com.acme.yards.v1.QuoteDockResponse
import com.acme.yards.v1.QuoteYardRequest
import com.acme.yards.v1.QuoteYardRequestKt
import com.acme.yards.v1.RecalculateDocksRequest
import com.acme.yards.v1.RecalculateDocksResponse
import com.acme.yards.v1.RecalculateYardsRequestKt
import com.acme.yards.v1.SimulateDockRequest
import com.acme.yards.v1.SimulateDockResponse
import com.acme.yards.v1.SimulateDockResponseKt
import com.acme.yards.v1.SimulateYardRequest
import com.acme.yards.v1.SimulateYardRequestKt
import com.acme.yards.v1.Yard
import com.acme.yards.v1.YardCalculationServiceGrpcKt
import com.acme.yards.v1.YardComponent
import com.acme.yards.v1.calculateDockResponse
import com.acme.yards.v1.compareDockOptionsResponse
import com.acme.yards.v1.compareYardOptionsRequest
import com.acme.yards.v1.copy as yardsCopy
import com.acme.yards.v1.dock
import com.acme.yards.v1.dockComponent
import com.acme.yards.v1.dockDimensions
import com.acme.yards.v1.estimateDockResponse
import com.acme.yards.v1.estimateYardRequest
import com.acme.yards.v1.explainDockCalculationResponse
import com.acme.yards.v1.explainYardCalculationRequest
import com.acme.yards.v1.getDockRatesResponse
import com.acme.yards.v1.quoteDockResponse
import com.acme.yards.v1.quoteYardRequest
import com.acme.yards.v1.recalculateDocksResponse
import com.acme.yards.v1.recalculateYardsRequest
import com.acme.yards.v1.simulateDockResponse
import com.acme.yards.v1.simulateYardRequest
import com.acme.yards.v1.yard
import com.acme.yards.v1.yardComponent
import com.acme.yards.v1.yardDimensions
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Computes the figures of a dock, such as totals, taxes, and estimates. */
class DockCalculationGrpcService(
    private val store: YardsRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyQueryService: AccessPolicyQueryServiceGrpcKt.AccessPolicyQueryServiceCoroutineStub,
    private val careplansCarePlanQueryService: CarePlanQueryServiceGrpcKt.CarePlanQueryServiceCoroutineStub,
    private val keymanagementCryptoKeyCalculationService: CryptoKeyCalculationServiceGrpcKt.CryptoKeyCalculationServiceCoroutineStub,
    private val ledgerLedgerAccountQueryService: LedgerAccountQueryServiceGrpcKt.LedgerAccountQueryServiceCoroutineStub,
    private val ledgerLedgerAccountService: LedgerAccountServiceGrpcKt.LedgerAccountServiceCoroutineStub,
    private val ratelimitingRateLimitCalculationService: RateLimitCalculationServiceGrpcKt.RateLimitCalculationServiceCoroutineStub,
    private val venuesVenueService: VenueServiceGrpcKt.VenueServiceCoroutineStub,
    private val yardsYardCalculationService: YardCalculationServiceGrpcKt.YardCalculationServiceCoroutineStub,
) : DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DockCalculationGrpcService::class.java)

    private val workerForCalculateDock0 = ReplyMailbox<CompareYardOptionsRequest, CompareYardOptionsResponse>(workers) { payload ->
        withTimeout(5_000) { yardsYardCalculationService.compareYardOptions(payload) }
    }

    private val clientForCalculateDock1 by lazy { keymanagementCryptoKeyCalculationService }

    private val invokeRecalculateDocks2: suspend (SimulateRateLimitRequest) -> SimulateRateLimitResponse = { payload ->
        ratelimitingRateLimitCalculationService.simulateRateLimit(payload)
    }

    /** Calculates a dock. */
    override suspend fun calculateDock(request: CalculateDockRequest): CalculateDockResponse {
        return handleCalculateDock(request)
    }

    /** Compares a dock. */
    override suspend fun compareDockOptions(request: CompareDockOptionsRequest): CompareDockOptionsResponse = handleCompareDockOptions(request)

    /** Estimates a dock. */
    override suspend fun estimateDock(request: EstimateDockRequest): EstimateDockResponse =
        RpcBoundary.run("acme.yards.v1.DockCalculationService/EstimateDock") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EstimateDock", request)
            log.atInfo().addKeyValue("operation", "EstimateDock").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dock.dockId).log("Recorded audit detail")
            val outbound0 = updateVenueRequest {
                this.context = request.context
                this.venue = venue {
                    this.address = postalAddress {
                        this.region = request.details.destination.region
                    }
                }
            }
            val reply0 = withTimeout(5_000) { venuesVenueService.updateVenue(outbound0) }
            val configure1: QuoteYardRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.yard = yard {
                    this.address = postalAddress {
                        this.lines.addAll(request.details.destination.linesList)
                    }
                    this.audit = auditInfo {
                        this.createdAt = reply0.venue.audit.createdAt
                    }
                }
            }
            val outbound1 = quoteYardRequest(configure1)
            val reply1 = yardsYardCalculationService.quoteYard(outbound1)
            val outbound2 = findAccessPoliciesRequest {
                this.context = request.context
                this.filter = FindAccessPoliciesRequestKt.filter {
                    this.createdAfter = request.dock.audit.updatedAt
                    this.ownerId = reply1.yard.owner.userId
                    this.region = reply0.venue.address.region
                }
            }
            val reply2 = authorizationAccessPolicyQueryService.findAccessPolicies(outbound2)
            estimateDockResponse {
                this.dock = dock {
                    this.dimensions = dockDimensions {
                        this.unit = reply0.venue.dimensions.unit
                        this.weight = reply0.venue.dimensions.weight
                    }
                    this.dockId = request.dockId
                    this.owner = request.dock.owner
                }
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.details.putAll(reply1.result.detailsMap)
                    this.message = reply2.result.message
                    this.status = "ok"
                }
            }
        }

    /** Explains a dock. */
    override suspend fun explainDockCalculation(request: ExplainDockCalculationRequest): ExplainDockCalculationResponse {
        return handleExplainDockCalculation(request)
    }

    /** Returns a dock. */
    override suspend fun getDockRates(request: GetDockRatesRequest): GetDockRatesResponse = handleGetDockRates(request)

    /** Quotes a dock. */
    override suspend fun quoteDock(request: QuoteDockRequest): QuoteDockResponse =
        RpcBoundary.run("acme.yards.v1.DockCalculationService/QuoteDock") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "QuoteDock", request)
            log.atInfo().addKeyValue("operation", "QuoteDock").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dock.dockId).log("Recorded audit detail")
            val outbound0 = simulateYardRequest {
                this.context = request.context
                this.yard = yard {
                    this.address = postalAddress {
                        this.region = request.dock.address.region
                    }
                    this.dimensions = yardDimensions {
                        this.weight = request.dock.dimensions.weight
                    }
                }
            }
            val reply0 = yardsYardCalculationService.simulateYard(WireCopies.json(outbound0))
            val outbound1 = recalculateYardsRequest {
                this.context = request.context
                this.details = RecalculateYardsRequestKt.details {
                    this.destination = postalAddress {
                        this.locality = request.details.destination.locality
                    }
                }
                this.yard = yard {
                    this.address = postalAddress {
                        this.countryCode = request.details.destination.countryCode
                        this.locality = when (reply0.yard.locatorCase) {
                            Yard.LocatorCase.ADDRESS -> reply0.yard.address.locality
                            else -> ""
                        }
                    }
                }
            }
            val reply1 = yardsYardCalculationService.recalculateYards(outbound1)
            quoteDockResponse {
                this.dock = dock {
                    this.components.putAll(mapOf("primary" to dockComponent {
                        this.componentId = (reply1.yard.componentsMap["primary"] ?: YardComponent.getDefaultInstance()).componentId
                    }))
                    this.dockId = request.dockId
                    this.specs.putAll(request.dock.specsMap)
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    /** Recalculates a dock. */
    override suspend fun recalculateDocks(request: RecalculateDocksRequest): RecalculateDocksResponse {
        return handleRecalculateDocks(request)
    }

    /** Simulates a dock. */
    override suspend fun simulateDock(request: SimulateDockRequest): SimulateDockResponse = handleSimulateDock(request)

    private suspend fun handleCalculateDock(request: CalculateDockRequest): CalculateDockResponse =
        RpcBoundary.run("acme.yards.v1.DockCalculationService/CalculateDock") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CalculateDock", request)
            log.atInfo().addKeyValue("operation", "CalculateDock").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dock.dockId).log("Recorded audit detail")
            val outbound0 = compareYardOptionsRequest {
                this.context = request.context
                this.details = CompareYardOptionsRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(request.details.destination.linesList)
                        this.locality = request.details.destination.locality
                    }
                }
            }
            val reply0 = workerForCalculateDock0.submit(outbound0)
            val outbound1 = estimateCryptoKeyRequest {
                this.context = request.context
                this.cryptoKey = cryptoKey {
                    this.contact = contactInfo {
                        this.email = request.dock.owner.email
                    }
                }
                this.details = EstimateCryptoKeyRequestKt.details {
                    this.destination = postalAddress {
                        this.locality = request.details.destination.locality
                    }
                }
            }
            val reply1 = clientForCalculateDock1.estimateCryptoKey(outbound1)
            calculateDockResponse {
                this.dock = dock {
                    this.audit = auditInfo {
                        this.createdAt = reply0.yard.audit.createdAt
                        this.etag = reply1.cryptoKey.audit.etag
                    }
                    this.dimensions = dockDimensions {
                        this.width = reply0.yard.dimensions.width
                    }
                    this.dockId = request.dockId
                    this.status = request.dock.status
                }
                this.result = operationResult {
                    this.status = reply1.result.status
                }
            }
        }

    private suspend fun handleCompareDockOptions(request: CompareDockOptionsRequest): CompareDockOptionsResponse =
        RpcBoundary.run("acme.yards.v1.DockCalculationService/CompareDockOptions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CompareDockOptions", request)
            log.atInfo().addKeyValue("operation", "CompareDockOptions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dock.dockId).log("Recorded audit detail")
            val outbound0 = estimateYardRequest {
                this.context = request.context
                this.yard = yard {
                    this.owner = userRef {
                        this.userId = request.dock.owner.userId
                    }
                }
            }
            val reply0 = flowOf(outbound0).map { value -> yardsYardCalculationService.estimateYard(value) }.single()
            compareDockOptionsResponse {
                this.dock = dock {
                    this.components.putAll(mapOf("primary" to dockComponent {
                        this.quantity = (reply0.yard.componentsMap["primary"] ?: YardComponent.getDefaultInstance()).quantity
                    }))
                    this.dockId = request.dockId
                    this.owner = request.dock.owner
                    this.tags.addAll(reply0.yard.tagsList)
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private fun ExplainDockCalculationRequest.toQuoteYardFromExplainDockCalculation2(reply1: EstimateYardResponse): QuoteYardRequest {
        val source = this
        return quoteYardRequest {
            this.context = source.context
            this.yard = yard {
                this.address = postalAddress {
                    this.countryCode = source.dock.address.countryCode
                    this.locality = reply1.yard.address.locality
                }
                this.components.putAll(mapOf("primary" to yardComponent {
                    this.componentId = (reply1.yard.componentsMap["primary"] ?: YardComponent.getDefaultInstance()).componentId
                }))
                this.owner = userRef {
                    this.email = source.dock.owner.email
                }
            }
        }
    }

    private suspend fun handleExplainDockCalculation(request: ExplainDockCalculationRequest): ExplainDockCalculationResponse =
        RpcBoundary.run("acme.yards.v1.DockCalculationService/ExplainDockCalculation") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainDockCalculation", request)
            log.atInfo().addKeyValue("operation", "ExplainDockCalculation").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dock.dockId).log("Recorded audit detail")
            val outbound0 = recalculateYardsRequest {
                this.context = request.context
                this.details = RecalculateYardsRequestKt.details {
                    this.destination = postalAddress {
                        this.recipient = request.dock.address.recipient
                    }
                }
                this.yard = yard {
                    this.audit = auditInfo {
                        this.etag = request.dock.audit.etag
                    }
                }
            }
            val reply0 = yardsYardCalculationService.recalculateYards(outbound0)
            val outbound1 = estimateYardRequest {}.yardsCopy {
                this.context = request.context
                this.yard = yard {
                    this.address = postalAddress {
                        this.locality = reply0.yard.address.locality
                    }
                    this.components.putAll(mapOf("primary" to yardComponent {
                        this.componentId = (request.dock.componentsMap["primary"] ?: DockComponent.getDefaultInstance()).componentId
                    }))
                }
            }
            val reply1 = yardsYardCalculationService.estimateYard(outbound1)
            val outbound2 = request.toQuoteYardFromExplainDockCalculation2(reply1)
            val reply2 = yardsYardCalculationService.quoteYard(outbound2)
            val outbound3 = explainYardCalculationRequest {
                this.context = request.context
                this.yard = yard {
                    this.audit = auditInfo {
                        this.createdBy = reply1.yard.audit.createdBy
                    }
                    this.dimensions = yardDimensions {
                        this.unit = reply0.yard.dimensions.unit
                    }
                    this.owner = userRef {
                        this.email = request.details.contact.email
                    }
                }
            }
            val reply3 = yardsYardCalculationService.explainYardCalculation(outbound3)
            explainDockCalculationResponse {
                this.dock = dock {
                    this.audit = auditInfo {
                        this.updatedAt = reply3.yard.audit.updatedAt
                    }
                    this.dimensions = dockDimensions {
                        this.height = reply1.yard.dimensions.height
                        this.length = reply1.yard.dimensions.length
                    }
                    this.dockId = request.dockId
                    this.name = request.dock.name
                }
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.message = reply0.result.message
                    this.status = reply2.result.status
                }
            }
        }

    private suspend fun handleGetDockRates(request: GetDockRatesRequest): GetDockRatesResponse =
        RpcBoundary.run("acme.yards.v1.DockCalculationService/GetDockRates") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetDockRates", request)
            log.atInfo().addKeyValue("operation", "GetDockRates").log("Handling transport request")
            val outbound0 = suggestCarePlansRequest {
                this.context = request.context
                this.filter = SuggestCarePlansRequestKt.filter {
                    this.ownerId = request.dockId
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = careplansCarePlanQueryService.suggestCarePlans(envelope0.unpack(SuggestCarePlansRequest::class.java))
            getDockRatesResponse {
                this.dock = dock {
                    this.dockId = request.dockId
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleRecalculateDocks(request: RecalculateDocksRequest): RecalculateDocksResponse =
        RpcBoundary.run("acme.yards.v1.DockCalculationService/RecalculateDocks") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecalculateDocks", request)
            log.atInfo().addKeyValue("operation", "RecalculateDocks").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dock.dockId).log("Recorded audit detail")
            val outbound0 = browseLedgerAccountsRequest {
                this.context = request.context
                this.filter = BrowseLedgerAccountsRequestKt.filter {
                    this.region = request.dock.address.region
                }
            }
            val command0: Submission<BrowseLedgerAccountsRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = ledgerLedgerAccountQueryService.browseLedgerAccounts(payload0)
            val outbound1 = compareCryptoKeyOptionsRequest {
                this.context = request.context
                this.cryptoKey = cryptoKey {
                    this.audit = auditInfo {
                        this.createdAt = request.dock.audit.createdAt
                    }
                    this.contact = contactInfo {
                        this.email = request.dock.owner.email
                    }
                }
                this.details = CompareCryptoKeyOptionsRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = request.details.destination.countryCode
                        this.recipient = request.dock.address.recipient
                    }
                }
            }
            val adapter1 = OutboundPort<CompareCryptoKeyOptionsRequest, CompareCryptoKeyOptionsResponse> { payload ->
                keymanagementCryptoKeyCalculationService.compareCryptoKeyOptions(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            val outbound2 = simulateRateLimitRequest {
                this.context = request.context
                this.details = SimulateRateLimitRequestKt.details {
                    this.destination = postalAddress {
                        this.recipient = request.dock.address.recipient
                        this.region = request.details.destination.region
                    }
                }
                this.rateLimit = rateLimit {
                    this.audit = auditInfo {
                        this.updatedBy = reply1.cryptoKey.audit.updatedBy
                    }
                }
            }
            val reply2 = invokeRecalculateDocks2(outbound2)
            val outbound3 = cloneLedgerAccountRequest {
                this.context = request.context
                this.ledgerAccount = ledgerAccount {
                    this.audit = auditInfo {
                        this.updatedAt = request.dock.audit.updatedAt
                    }
                    this.holder = postingBatchRef {
                        this.displayName = request.dock.owner.displayName
                    }
                }
            }
            val invoke3: suspend (CloneLedgerAccountRequest, Metadata) -> CloneLedgerAccountResponse = ledgerLedgerAccountService::cloneLedgerAccount
            val reply3 = invoke3(outbound3, Metadata())
            recalculateDocksResponse {
                this.dock = dock {
                    this.audit = auditInfo {
                        this.createdAt = reply2.rateLimit.audit.createdAt
                    }
                    this.dockId = request.dockId
                    this.owner = userRef {
                        this.email = reply3.ledgerAccount.contact.email
                    }
                    this.status = request.dock.status
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSimulateDock(request: SimulateDockRequest): SimulateDockResponse =
        RpcBoundary.run("acme.yards.v1.DockCalculationService/SimulateDock") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SimulateDock", request)
            log.atInfo().addKeyValue("operation", "SimulateDock").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dock.dockId).log("Recorded audit detail")
            val outbound0 = SimulateYardRequest.newBuilder().apply {
                setContext(request.context)
                setDetails(SimulateYardRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = request.dock.address.countryCode
                    }
                })
                setYard(yard {
                    this.address = postalAddress {
                        this.recipient = request.details.destination.recipient
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> yardsYardCalculationService.simulateYard(value) }
            simulateDockResponse {
                this.breakdown = SimulateDockResponseKt.breakdown {
                    this.currencyCode = reply0.breakdown.currencyCode
                }
                this.dock = dock {
                    this.dockId = request.dockId
                    this.specs.putAll(request.dock.specsMap)
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }
}
