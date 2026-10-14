package com.acme.transport.routing.grpc

import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.dispatch.v1.CountDispatchOrdersRequest
import com.acme.dispatch.v1.CountDispatchOrdersRequestKt
import com.acme.dispatch.v1.CountDispatchOrdersResponse
import com.acme.dispatch.v1.DispatchOrderQueryServiceGrpcKt
import com.acme.dispatch.v1.DispatchOrderValidationServiceGrpcKt
import com.acme.dispatch.v1.NormalizeDispatchOrderRequestKt
import com.acme.dispatch.v1.PreviewDispatchOrderRequest
import com.acme.dispatch.v1.PreviewDispatchOrderResponse
import com.acme.dispatch.v1.countDispatchOrdersRequest
import com.acme.dispatch.v1.dispatchOrder
import com.acme.dispatch.v1.normalizeDispatchOrderRequest
import com.acme.dispatch.v1.previewDispatchOrderRequest
import com.acme.documents.v1.DocumentValidationServiceGrpcKt
import com.acme.documents.v1.PreviewDocumentRequestKt
import com.acme.documents.v1.PreviewDocumentResponse
import com.acme.documents.v1.previewDocumentRequest
import com.acme.drivers.v1.driverRef
import com.acme.featureflags.v1.BrowseFeatureFlagsRequest
import com.acme.featureflags.v1.BrowseFeatureFlagsRequestKt
import com.acme.featureflags.v1.BrowseFeatureFlagsResponse
import com.acme.featureflags.v1.FeatureFlagQueryServiceGrpcKt
import com.acme.featureflags.v1.browseFeatureFlagsRequest
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.LedgerAccountServiceGrpcKt
import com.acme.ledger.v1.watchLedgerAccountsRequest
import com.acme.routing.v1.CalculateStopRequest
import com.acme.routing.v1.CalculateStopResponse
import com.acme.routing.v1.CompareRouteOptionsResponse
import com.acme.routing.v1.CompareStopOptionsRequest
import com.acme.routing.v1.CompareStopOptionsResponse
import com.acme.routing.v1.CompareStopOptionsResponseKt
import com.acme.routing.v1.EstimateRouteRequestKt
import com.acme.routing.v1.EstimateStopRequest
import com.acme.routing.v1.EstimateStopResponse
import com.acme.routing.v1.EstimateStopResponseKt
import com.acme.routing.v1.ExplainRouteCalculationRequestKt
import com.acme.routing.v1.ExplainRouteCalculationResponse
import com.acme.routing.v1.ExplainStopCalculationRequest
import com.acme.routing.v1.ExplainStopCalculationResponse
import com.acme.routing.v1.GetStopRatesRequest
import com.acme.routing.v1.GetStopRatesResponse
import com.acme.routing.v1.QuoteRouteRequest
import com.acme.routing.v1.QuoteRouteRequestKt
import com.acme.routing.v1.QuoteRouteResponse
import com.acme.routing.v1.QuoteStopRequest
import com.acme.routing.v1.QuoteStopResponse
import com.acme.routing.v1.RecalculateRoutesResponse
import com.acme.routing.v1.RouteCalculationServiceGrpcKt
import com.acme.routing.v1.SimulateStopRequest
import com.acme.routing.v1.SimulateStopResponse
import com.acme.routing.v1.StopCalculationServiceGrpcKt
import com.acme.routing.v1.StopParticipant
import com.acme.routing.v1.calculateRouteRequest
import com.acme.routing.v1.calculateStopResponse
import com.acme.routing.v1.compareRouteOptionsRequest
import com.acme.routing.v1.compareStopOptionsResponse
import com.acme.routing.v1.estimateRouteRequest
import com.acme.routing.v1.estimateStopResponse
import com.acme.routing.v1.explainRouteCalculationRequest
import com.acme.routing.v1.explainStopCalculationResponse
import com.acme.routing.v1.getStopRatesResponse
import com.acme.routing.v1.quoteRouteRequest
import com.acme.routing.v1.quoteStopResponse
import com.acme.routing.v1.recalculateRoutesRequest
import com.acme.routing.v1.route
import com.acme.routing.v1.routeLine
import com.acme.routing.v1.routeLineTax
import com.acme.routing.v1.simulateStopResponse
import com.acme.routing.v1.stop
import com.acme.telematics.v1.GeoFenceCalculationServiceGrpcKt
import com.acme.telematics.v1.SimulateGeoFenceRequestKt
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.simulateGeoFenceRequest
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.routing.RoutingRepository
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

/** Computes the figures of a stop, such as totals, taxes, and estimates. */
class StopCalculationEndpoint(
    private val store: RoutingRepository,
    private val workers: CoroutineScope,
    private val dispatchDispatchOrderQueryService: DispatchOrderQueryServiceGrpcKt.DispatchOrderQueryServiceCoroutineStub,
    private val dispatchDispatchOrderValidationService: DispatchOrderValidationServiceGrpcKt.DispatchOrderValidationServiceCoroutineStub,
    private val documentsDocumentValidationService: DocumentValidationServiceGrpcKt.DocumentValidationServiceCoroutineStub,
    private val featureflagsFeatureFlagQueryService: FeatureFlagQueryServiceGrpcKt.FeatureFlagQueryServiceCoroutineStub,
    private val ledgerLedgerAccountService: LedgerAccountServiceGrpcKt.LedgerAccountServiceCoroutineStub,
    private val routingRouteCalculationService: RouteCalculationServiceGrpcKt.RouteCalculationServiceCoroutineStub,
    private val telematicsGeoFenceCalculationService: GeoFenceCalculationServiceGrpcKt.GeoFenceCalculationServiceCoroutineStub,
) : StopCalculationServiceGrpcKt.StopCalculationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(StopCalculationEndpoint::class.java)

    private val invokeCompareStopOptions1: suspend (QuoteRouteRequest) -> QuoteRouteResponse = { payload ->
        routingRouteCalculationService.quoteRoute(payload)
    }

    private val workerForExplainStopCalculation0 = ReplyMailbox<BrowseFeatureFlagsRequest, BrowseFeatureFlagsResponse>(workers) { payload ->
        withTimeout(5_000) { featureflagsFeatureFlagQueryService.browseFeatureFlags(payload) }
    }

    /** Calculates a stop. */
    override suspend fun calculateStop(request: CalculateStopRequest): CalculateStopResponse =
        RpcBoundary.run("acme.routing.v1.StopCalculationService/CalculateStop") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CalculateStop", request)
            log.atInfo().addKeyValue("operation", "CalculateStop").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.stop.stopId).log("Recorded audit detail")
            var reply0 = ExplainRouteCalculationResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<StopParticipant>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound0 = explainRouteCalculationRequest {
                            this.context = request.context
                            this.details = ExplainRouteCalculationRequestKt.details {
                                this.contact = contactInfo {
                                    this.phone = item.contact.phone
                                }
                            }
                            this.route = route {
                                this.counterparty = driverRef {
                                    this.email = item.contact.email
                                }
                            }
                        }
                        reply0 = routingRouteCalculationService.explainRouteCalculation(outbound0)
                    }
                }
                try {
                    request.stop.participantsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            var reply1 = PreviewDocumentResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.stop.participantsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound1 = previewDocumentRequest {
                                this.context = request.context
                                this.details = PreviewDocumentRequestKt.details {
                                    this.contact = contactInfo {
                                        this.name = item.contact.name
                                    }
                                }
                            }
                            documentsDocumentValidationService.previewDocument(outbound1)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply1 = it }
                }
            }
            calculateStopResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.stop = stop {
                    this.organizer = request.stop.organizer
                    this.stopId = request.stopId
                }
            }
        }

    /** Compares a stop. */
    override suspend fun compareStopOptions(request: CompareStopOptionsRequest): CompareStopOptionsResponse {
        return handleCompareStopOptions(request)
    }

    /** Estimates a stop. */
    override suspend fun estimateStop(request: EstimateStopRequest): EstimateStopResponse = handleEstimateStop(request)

    /** Explains a stop. */
    override suspend fun explainStopCalculation(request: ExplainStopCalculationRequest): ExplainStopCalculationResponse =
        RpcBoundary.run("acme.routing.v1.StopCalculationService/ExplainStopCalculation") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainStopCalculation", request)
            log.atInfo().addKeyValue("operation", "ExplainStopCalculation").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.stop.stopId).log("Recorded audit detail")
            val outbound0 = browseFeatureFlagsRequest {
                this.context = request.context
                this.filter = BrowseFeatureFlagsRequestKt.filter {
                    this.region = request.stop.location.region
                }
            }
            val reply0 = workerForExplainStopCalculation0.submit(outbound0)
            var reply1 = CompareRouteOptionsResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.stop.participantsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound1 = compareRouteOptionsRequest {
                                this.context = request.context
                                this.route = route {
                                    this.audit = auditInfo {
                                        this.updatedAt = request.stop.audit.updatedAt
                                    }
                                    this.billingAddress = postalAddress {
                                        this.recipient = request.stop.location.recipient
                                    }
                                    this.counterparty = driverRef {
                                        this.email = item.contact.email
                                    }
                                }
                            }
                            routingRouteCalculationService.compareRouteOptions(outbound1)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply1 = it }
                }
            }
            val outbound2 = calculateRouteRequest {
                this.context = request.context
                this.route = route {
                    this.billingAddress = postalAddress {
                        this.recipient = request.stop.location.recipient
                    }
                }
            }
            val reply2 = flowOf(outbound2).map { value -> routingRouteCalculationService.calculateRoute(value) }.single()
            explainStopCalculationResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
                this.stop = stop {
                    this.location = postalAddress {
                        this.recipient = reply2.route.billingAddress.recipient
                    }
                    this.stopId = request.stopId
                    this.window = request.stop.window
                }
            }
        }

    /** Returns a stop. */
    override suspend fun getStopRates(request: GetStopRatesRequest): GetStopRatesResponse {
        return handleGetStopRates(request)
    }

    /** Quotes a stop. */
    override suspend fun quoteStop(request: QuoteStopRequest): QuoteStopResponse = handleQuoteStop(request)

    /** Simulates a stop. */
    override suspend fun simulateStop(request: SimulateStopRequest): SimulateStopResponse =
        RpcBoundary.run("acme.routing.v1.StopCalculationService/SimulateStop") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SimulateStop", request)
            log.atInfo().addKeyValue("operation", "SimulateStop").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.stop.stopId).log("Recorded audit detail")
            val configure0: NormalizeDispatchOrderRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.dispatchOrder = dispatchOrder {
                    this.billingAddress = postalAddress {
                        this.countryCode = request.details.destination.countryCode
                        this.locality = request.stop.location.locality
                        this.postalCode = request.stop.location.postalCode
                        this.region = request.details.destination.region
                    }
                }
            }
            val outbound0 = normalizeDispatchOrderRequest(configure0)
            val reply0 = dispatchDispatchOrderValidationService.normalizeDispatchOrder(outbound0)
            val outbound1 = estimateRouteRequest {
                this.context = request.context
                this.details = EstimateRouteRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = request.details.destination.countryCode
                    }
                }
            }
            val reply1 = routingRouteCalculationService.estimateRoute(outbound1)
            val outbound2 = quoteRouteRequest {
                this.context = request.context
                this.details = QuoteRouteRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = reply0.dispatchOrder.billingAddress.countryCode
                    }
                }
                this.route = route {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.dispatchOrder.audit.updatedAt
                    }
                    this.counterparty = driverRef {
                        this.displayName = reply0.dispatchOrder.counterparty.displayName
                    }
                }
            }
            val reply2 = routingRouteCalculationService.quoteRoute(outbound2)
            simulateStopResponse {
                this.result = operationResult {
                    this.details.putAll(reply2.result.detailsMap)
                    this.status = reply0.outcome.status
                }
                this.stop = stop {
                    this.audit = auditInfo {
                        this.updatedBy = reply1.route.audit.updatedBy
                    }
                    this.status = request.stop.status
                    this.stopId = request.stopId
                }
            }
        }

    private suspend fun handleCompareStopOptions(request: CompareStopOptionsRequest): CompareStopOptionsResponse =
        RpcBoundary.run("acme.routing.v1.StopCalculationService/CompareStopOptions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CompareStopOptions", request)
            log.atInfo().addKeyValue("operation", "CompareStopOptions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.stop.stopId).log("Recorded audit detail")
            val outbound0 = countDispatchOrdersRequest {
                this.context = request.context
                this.filter = CountDispatchOrdersRequestKt.filter {
                    this.createdAfter = request.stop.audit.updatedAt
                    this.labels.putAll(request.stop.metadataMap)
                    this.ownerId = request.stop.organizer.userId
                    this.region = request.details.destination.region
                }
            }
            val adapter0 = OutboundPort<CountDispatchOrdersRequest, CountDispatchOrdersResponse> { payload ->
                dispatchDispatchOrderQueryService.countDispatchOrders(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = quoteRouteRequest {
                this.context = request.context
                this.route = route {
                    this.audit = auditInfo {
                        this.createdAt = request.stop.audit.createdAt
                    }
                    this.billingAddress = postalAddress {
                        this.lines.addAll(request.stop.location.linesList)
                        this.recipient = request.stop.location.recipient
                        this.region = request.stop.location.region
                    }
                }
            }
            val reply1 = invokeCompareStopOptions1(outbound1)
            val outbound2 = previewDispatchOrderRequest {
                this.context = request.context
                this.dispatchOrder = dispatchOrder {
                    this.billingAddress = postalAddress {
                        this.region = reply1.route.billingAddress.region
                    }
                }
            }
            val invoke2: suspend (PreviewDispatchOrderRequest, Metadata) -> PreviewDispatchOrderResponse = dispatchDispatchOrderValidationService::previewDispatchOrder
            val reply2 = invoke2(outbound2, Metadata())
            request.stop.participantsList.asFlow().map { item ->
                val outbound3 = watchLedgerAccountsRequest {
                    this.context = request.context
                    this.subscriber = contactInfo {
                        this.email = item.contact.email
                        this.preferredChannel = item.contact.preferredChannel
                    }
                }
                ledgerLedgerAccountService.watchLedgerAccounts(outbound3).collect { event ->
                    log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
                }
            }.collect()
            compareStopOptionsResponse {
                this.breakdown = CompareStopOptionsResponseKt.breakdown {
                    this.total = reply2.dispatchOrder.total
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply2.result.referenceIdsList)
                    this.status = reply0.result.status
                }
                this.stop = stop {
                    this.organizer = userRef {
                        this.email = reply1.route.counterparty.email
                    }
                    this.status = request.stop.status
                    this.stopId = request.stopId
                }
            }
        }

    private suspend fun handleEstimateStop(request: EstimateStopRequest): EstimateStopResponse =
        RpcBoundary.run("acme.routing.v1.StopCalculationService/EstimateStop") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EstimateStop", request)
            log.atInfo().addKeyValue("operation", "EstimateStop").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.stop.stopId).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                simulateGeoFenceRequest {
                    this.context = request.context
                    this.details = SimulateGeoFenceRequestKt.details {
                        this.destination = postalAddress {
                            this.lines.addAll(request.stop.location.linesList)
                            this.recipient = request.stop.location.recipient
                        }
                    }
                    this.geoFence = geoFence {
                        this.audit = auditInfo {
                            this.updatedBy = request.stop.audit.updatedBy
                        }
                        this.owner = userRef {
                            this.email = request.stop.organizer.email
                        }
                    }
                }
            }
            val reply0 = telematicsGeoFenceCalculationService.simulateGeoFence(outbound0)
            val outbound1 = explainRouteCalculationRequest {
                this.context = request.context
                this.route = route {
                    this.billingAddress = postalAddress {
                        this.postalCode = request.details.destination.postalCode
                    }
                    this.counterparty = driverRef {
                        this.email = reply0.geoFence.owner.email
                    }
                }
            }
            val reply1 = ClientRegistry.lookup("acme.routing.v1.RouteCalculationService", RouteCalculationServiceGrpcKt.RouteCalculationServiceCoroutineStub::class.java).explainRouteCalculation(outbound1)
            var reply2 = RecalculateRoutesResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<StopParticipant>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound2 = recalculateRoutesRequest {
                            this.context = request.context
                            this.route = route {
                                this.audit = auditInfo {
                                    this.createdAt = reply0.geoFence.audit.createdAt
                                }
                                this.counterparty = driverRef {
                                    this.email = item.contact.email
                                }
                                this.lines.addAll(listOf(routeLine {
                                    this.tax = routeLineTax {
                                        this.amount = reply0.geoFence.discount.amount
                                    }
                                }))
                            }
                        }
                        reply2 = routingRouteCalculationService.recalculateRoutes(outbound2)
                    }
                }
                try {
                    request.stop.participantsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            estimateStopResponse {
                this.breakdown = EstimateStopResponseKt.breakdown {
                    this.currencyCode = request.details.currencyCode
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.status = "ok"
                }
                this.stop = stop {
                    this.audit = auditInfo {
                        this.createdBy = reply0.geoFence.audit.createdBy
                        this.updatedAt = reply0.geoFence.audit.updatedAt
                    }
                    this.stopId = request.stopId
                }
            }
        }

    private suspend fun handleGetStopRates(request: GetStopRatesRequest): GetStopRatesResponse =
        RpcBoundary.run("acme.routing.v1.StopCalculationService/GetStopRates") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetStopRates", request)
            log.atInfo().addKeyValue("operation", "GetStopRates").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            getStopRatesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.stop = stop {
                    this.stopId = request.stopId
                }
            }
        }

    private suspend fun handleQuoteStop(request: QuoteStopRequest): QuoteStopResponse =
        RpcBoundary.run("acme.routing.v1.StopCalculationService/QuoteStop") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "QuoteStop", request)
            log.atInfo().addKeyValue("operation", "QuoteStop").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.stop.stopId).log("Recorded audit detail")
            var reply0 = RecalculateRoutesResponse.getDefaultInstance()
            request.stop.participantsList.asFlow().map { item ->
                val outbound0 = recalculateRoutesRequest {
                    this.context = request.context
                    this.route = route {
                        this.audit = auditInfo {
                            this.updatedAt = request.stop.audit.updatedAt
                        }
                        this.billingAddress = postalAddress {
                            this.locality = request.details.destination.locality
                        }
                        this.counterparty = driverRef {
                            this.email = item.contact.email
                        }
                    }
                }
                routingRouteCalculationService.recalculateRoutes(outbound0)
            }.collect { reply0 = it }
            quoteStopResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.stop = stop {
                    this.status = request.stop.status
                    this.stopId = request.stopId
                }
            }
        }
}
