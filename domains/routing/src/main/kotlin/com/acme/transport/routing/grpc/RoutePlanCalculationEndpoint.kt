package com.acme.transport.routing.grpc

import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.DeliveryExceptionCalculationServiceGrpcKt
import com.acme.deliveries.v1.QuoteDeliveryExceptionRequestKt
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.quoteDeliveryExceptionRequest
import com.acme.drivers.v1.driverRef
import com.acme.identity.v1.UserValidationServiceGrpcKt
import com.acme.identity.v1.lintUsersRequest
import com.acme.identity.v1.user
import com.acme.ledger.v1.LedgerAccountValidationServiceGrpcKt
import com.acme.ledger.v1.LintLedgerAccountsRequestKt
import com.acme.ledger.v1.ledgerAccount
import com.acme.ledger.v1.ledgerAccountCardFunding
import com.acme.ledger.v1.lintLedgerAccountsRequest
import com.acme.ledger.v1.postingBatchRef
import com.acme.notifications.v1.BrowseNotificationsRequest
import com.acme.notifications.v1.BrowseNotificationsRequestKt
import com.acme.notifications.v1.BrowseNotificationsResponse
import com.acme.notifications.v1.FindNotificationsRequest
import com.acme.notifications.v1.FindNotificationsRequestKt
import com.acme.notifications.v1.NotificationQueryServiceGrpcKt
import com.acme.notifications.v1.browseNotificationsRequest
import com.acme.notifications.v1.findNotificationsRequest
import com.acme.routing.v1.CalculateRoutePlanRequest
import com.acme.routing.v1.CalculateRoutePlanResponse
import com.acme.routing.v1.CalculateRouteResponse
import com.acme.routing.v1.CheckRouteEligibilityRequestKt
import com.acme.routing.v1.CompareRoutePlanOptionsRequest
import com.acme.routing.v1.CompareRoutePlanOptionsResponse
import com.acme.routing.v1.EstimateRoutePlanRequest
import com.acme.routing.v1.EstimateRoutePlanResponse
import com.acme.routing.v1.EstimateRouteRequestKt
import com.acme.routing.v1.EstimateStopRequestKt
import com.acme.routing.v1.ExplainRouteDecisionResponse
import com.acme.routing.v1.ExplainRoutePlanCalculationRequest
import com.acme.routing.v1.ExplainRoutePlanCalculationResponse
import com.acme.routing.v1.ExplainRoutePlanCalculationResponseKt
import com.acme.routing.v1.GetRoutePlanRatesRequest
import com.acme.routing.v1.GetRoutePlanRatesResponse
import com.acme.routing.v1.PreviewRouteResponse
import com.acme.routing.v1.QuoteRoutePlanRequest
import com.acme.routing.v1.QuoteRoutePlanResponse
import com.acme.routing.v1.RecalculateRoutePlansRequest
import com.acme.routing.v1.RecalculateRoutePlansResponse
import com.acme.routing.v1.RouteCalculationServiceGrpcKt
import com.acme.routing.v1.RoutePlan
import com.acme.routing.v1.RoutePlanAttachment
import com.acme.routing.v1.RoutePlanCalculationServiceGrpcKt
import com.acme.routing.v1.RouteValidationServiceGrpcKt
import com.acme.routing.v1.ScoreRouteRequest
import com.acme.routing.v1.ScoreRouteRequestKt
import com.acme.routing.v1.ScoreRouteResponse
import com.acme.routing.v1.SimulateRoutePlanRequest
import com.acme.routing.v1.SimulateRoutePlanResponse
import com.acme.routing.v1.SimulateRoutePlanResponseKt
import com.acme.routing.v1.StopCalculationServiceGrpcKt
import com.acme.routing.v1.ValidateRouteResponse
import com.acme.routing.v1.calculateRoutePlanResponse
import com.acme.routing.v1.calculateRouteRequest
import com.acme.routing.v1.checkRouteEligibilityRequest
import com.acme.routing.v1.compareRoutePlanOptionsResponse
import com.acme.routing.v1.copy as routingCopy
import com.acme.routing.v1.estimateRoutePlanResponse
import com.acme.routing.v1.estimateRouteRequest
import com.acme.routing.v1.estimateStopRequest
import com.acme.routing.v1.explainRouteDecisionRequest
import com.acme.routing.v1.explainRoutePlanCalculationResponse
import com.acme.routing.v1.getRoutePlanRatesResponse
import com.acme.routing.v1.lintRoutesRequest
import com.acme.routing.v1.previewRouteRequest
import com.acme.routing.v1.quoteRoutePlanResponse
import com.acme.routing.v1.recalculateRoutePlansResponse
import com.acme.routing.v1.route
import com.acme.routing.v1.routeAttachment
import com.acme.routing.v1.routeCardSettlement
import com.acme.routing.v1.routeLine
import com.acme.routing.v1.routeLineTax
import com.acme.routing.v1.routePlan
import com.acme.routing.v1.scoreRouteRequest
import com.acme.routing.v1.simulateRoutePlanResponse
import com.acme.routing.v1.stop
import com.acme.routing.v1.stopParticipant
import com.acme.routing.v1.validateRouteRequest
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.transport.routing.RoutingRepository
import com.acme.yards.v1.SearchYardsRequest
import com.acme.yards.v1.SearchYardsRequestKt
import com.acme.yards.v1.SearchYardsResponse
import com.acme.yards.v1.YardServiceGrpcKt
import com.acme.yards.v1.searchYardsRequest
import com.acme.yards.v1.watchYardsRequest
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

/** Computes the figures of a route plan, such as totals, taxes, and estimates. */
class RoutePlanCalculationEndpoint(
    private val store: RoutingRepository,
    private val workers: CoroutineScope,
    private val deliveriesDeliveryExceptionCalculationService: DeliveryExceptionCalculationServiceGrpcKt.DeliveryExceptionCalculationServiceCoroutineStub,
    private val identityUserValidationService: UserValidationServiceGrpcKt.UserValidationServiceCoroutineStub,
    private val ledgerLedgerAccountValidationService: LedgerAccountValidationServiceGrpcKt.LedgerAccountValidationServiceCoroutineStub,
    private val notificationsNotificationQueryService: NotificationQueryServiceGrpcKt.NotificationQueryServiceCoroutineStub,
    private val routingRouteCalculationService: RouteCalculationServiceGrpcKt.RouteCalculationServiceCoroutineStub,
    private val routingRouteValidationService: RouteValidationServiceGrpcKt.RouteValidationServiceCoroutineStub,
    private val routingStopCalculationService: StopCalculationServiceGrpcKt.StopCalculationServiceCoroutineStub,
    private val yardsYardService: YardServiceGrpcKt.YardServiceCoroutineStub,
) : RoutePlanCalculationServiceGrpcKt.RoutePlanCalculationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(RoutePlanCalculationEndpoint::class.java)

    private val invokeCalculateRoutePlan1: suspend (ScoreRouteRequest) -> ScoreRouteResponse = { payload ->
        routingRouteValidationService.scoreRoute(payload)
    }

    private val clientForExplainRoutePlanCalculation0 by lazy { identityUserValidationService }

    /** Calculates a route plan. */
    override suspend fun calculateRoutePlan(request: CalculateRoutePlanRequest): CalculateRoutePlanResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanCalculationService/CalculateRoutePlan") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CalculateRoutePlan", request)
            log.atInfo().addKeyValue("operation", "CalculateRoutePlan").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.routePlan.routePlanId).log("Recorded audit detail")
            val outbound0 = searchYardsRequest {
                this.context = request.context
                this.filter = SearchYardsRequestKt.filter {
                    this.region = request.details.destination.region
                }
            }
            val adapter0 = OutboundPort<SearchYardsRequest, SearchYardsResponse> { payload ->
                yardsYardService.searchYards(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = scoreRouteRequest {
                this.context = request.context
                this.details = ScoreRouteRequestKt.details {
                    this.destination = postalAddress {
                        this.locality = request.routePlan.billingAddress.locality
                    }
                }
                this.route = route {
                    this.billingAddress = postalAddress {
                        this.region = request.details.destination.region
                    }
                }
            }
            val reply1 = invokeCalculateRoutePlan1(outbound1)
            val outbound2 = browseNotificationsRequest {
                this.context = request.context
                this.filter = BrowseNotificationsRequestKt.filter {
                    this.createdAfter = request.routePlan.issuedAt
                    this.labels.putAll(reply0.result.detailsMap)
                    this.ownerId = reply1.route.counterparty.driverId
                    this.region = reply1.route.billingAddress.region
                }
            }
            val invoke2: suspend (BrowseNotificationsRequest, Metadata) -> BrowseNotificationsResponse = notificationsNotificationQueryService::browseNotifications
            val reply2 = invoke2(outbound2, Metadata())
            var reply3 = ExplainRouteDecisionResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.routePlan.linesList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound3 = explainRouteDecisionRequest {
                                this.context = request.context
                                this.route = route {
                                    this.lines.addAll(listOf(routeLine {
                                        this.tax = routeLineTax {
                                            this.amount = item.tax.amount
                                            this.exemptions.addAll(item.tax.exemptionsList)
                                            this.jurisdiction = item.tax.jurisdiction
                                            this.rate = item.tax.rate
                                        }
                                    }))
                                }
                            }
                            routingRouteValidationService.explainRouteDecision(outbound3)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply3 = it }
                }
            }
            calculateRoutePlanResponse {
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.message = reply0.result.message
                    this.status = "ok"
                }
                this.routePlan = routePlan {
                    this.billingAddress = postalAddress {
                        this.recipient = reply1.route.billingAddress.recipient
                    }
                    this.notes = request.routePlan.notes
                    this.routePlanId = request.routePlanId
                }
            }
        }

    /** Compares a route plan. */
    override suspend fun compareRoutePlanOptions(request: CompareRoutePlanOptionsRequest): CompareRoutePlanOptionsResponse {
        return handleCompareRoutePlanOptions(request)
    }

    /** Estimates a route plan. */
    override suspend fun estimateRoutePlan(request: EstimateRoutePlanRequest): EstimateRoutePlanResponse = handleEstimateRoutePlan(request)

    /** Explains a route plan. */
    override suspend fun explainRoutePlanCalculation(request: ExplainRoutePlanCalculationRequest): ExplainRoutePlanCalculationResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanCalculationService/ExplainRoutePlanCalculation") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainRoutePlanCalculation", request)
            log.atInfo().addKeyValue("operation", "ExplainRoutePlanCalculation").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.routePlan.routePlanId).log("Recorded audit detail")
            val outbound0 = lintUsersRequest {
                this.context = request.context
                this.user = user {
                    this.audit = auditInfo {
                        this.etag = request.routePlan.audit.etag
                        this.updatedAt = request.routePlan.audit.updatedAt
                    }
                    this.primaryAddress = postalAddress {
                        this.lines.addAll(request.routePlan.billingAddress.linesList)
                    }
                }
            }
            val reply0 = clientForExplainRoutePlanCalculation0.lintUsers(outbound0)
            val outbound1 = watchYardsRequest {
                this.context = request.context
                this.subscriber = contactInfo {
                    this.email = reply0.user.relationshipManager.email
                }
            }
            yardsYardService.watchYards(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            explainRoutePlanCalculationResponse {
                this.outcome = ExplainRoutePlanCalculationResponseKt.outcome {
                    this.status = reply0.outcome.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.routePlan = routePlan {
                    this.billingAddress = request.routePlan.billingAddress
                    this.routePlanId = request.routePlanId
                }
            }
        }

    /** Returns a route plan. */
    override suspend fun getRoutePlanRates(request: GetRoutePlanRatesRequest): GetRoutePlanRatesResponse {
        return handleGetRoutePlanRates(request)
    }

    /** Quotes a route plan. */
    override suspend fun quoteRoutePlan(request: QuoteRoutePlanRequest): QuoteRoutePlanResponse = handleQuoteRoutePlan(request)

    /** Recalculates a route plan. */
    override suspend fun recalculateRoutePlans(request: RecalculateRoutePlansRequest): RecalculateRoutePlansResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanCalculationService/RecalculateRoutePlans") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecalculateRoutePlans", request)
            log.atInfo().addKeyValue("operation", "RecalculateRoutePlans").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.routePlan.routePlanId).log("Recorded audit detail")
            val outbound0 = estimateStopRequest {}.routingCopy {
                this.context = request.context
                this.details = EstimateStopRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(request.details.destination.linesList)
                    }
                }
                this.stop = stop {
                    this.participants.addAll(listOf(stopParticipant {
                        this.contact = contactInfo {
                            this.email = request.routePlan.counterparty.email
                        }
                    }))
                }
            }
            val reply0 = routingStopCalculationService.estimateStop(outbound0)
            val outbound1 = request.toFindNotificationsFromRecalculateRoutePlans1()
            val reply1 = notificationsNotificationQueryService.findNotifications(outbound1)
            recalculateRoutePlansResponse {
                this.result = operationResult {
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = reply0.result.status
                }
                this.routePlan = routePlan {
                    this.routePlanId = request.routePlanId
                    this.status = request.routePlan.status
                }
            }
        }

    /** Simulates a route plan. */
    override suspend fun simulateRoutePlan(request: SimulateRoutePlanRequest): SimulateRoutePlanResponse {
        return handleSimulateRoutePlan(request)
    }

    private suspend fun handleCompareRoutePlanOptions(request: CompareRoutePlanOptionsRequest): CompareRoutePlanOptionsResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanCalculationService/CompareRoutePlanOptions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CompareRoutePlanOptions", request)
            log.atInfo().addKeyValue("operation", "CompareRoutePlanOptions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.routePlan.routePlanId).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                quoteDeliveryExceptionRequest {
                    this.context = request.context
                    this.deliveryException = deliveryException {
                        this.audit = auditInfo {
                            this.createdBy = request.routePlan.audit.createdBy
                        }
                    }
                    this.details = QuoteDeliveryExceptionRequestKt.details {
                        this.destination = postalAddress {
                            this.postalCode = request.routePlan.billingAddress.postalCode
                            this.recipient = request.routePlan.billingAddress.recipient
                        }
                    }
                }
            }
            val reply0 = deliveriesDeliveryExceptionCalculationService.quoteDeliveryException(outbound0)
            val outbound1 = lintRoutesRequest {
                this.context = request.context
                this.route = route {
                    this.audit = auditInfo {
                        this.updatedBy = request.routePlan.audit.updatedBy
                    }
                    this.billingAddress = postalAddress {
                        this.region = request.details.destination.region
                    }
                    this.counterparty = driverRef {
                        this.displayName = reply0.deliveryException.customer.displayName
                    }
                }
            }
            val reply1 = ClientRegistry.lookup("acme.routing.v1.RouteValidationService", RouteValidationServiceGrpcKt.RouteValidationServiceCoroutineStub::class.java).lintRoutes(outbound1)
            val outbound2 = explainRouteDecisionRequest {
                this.context = request.context
                this.route = route {
                    this.billingAddress = postalAddress {
                        this.lines.addAll(reply1.route.billingAddress.linesList)
                        this.region = request.routePlan.billingAddress.region
                    }
                    this.card = routeCardSettlement {
                        this.last4 = request.routePlan.card.last4
                        this.token = reply1.route.card.token
                    }
                }
            }
            val reply2 = routingRouteValidationService.explainRouteDecision(WireCopies.reflective(outbound2))
            compareRoutePlanOptionsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.routePlan = routePlan {
                    this.audit = auditInfo {
                        this.createdAt = reply2.route.audit.createdAt
                    }
                    this.billingAddress = postalAddress {
                        this.region = reply1.route.billingAddress.region
                    }
                    this.counterparty = driverRef {
                        this.displayName = reply1.route.counterparty.displayName
                    }
                    this.dueAt = reply0.deliveryException.sla.dueAt
                    this.routePlanId = request.routePlanId
                    this.total = request.routePlan.total
                }
            }
        }

    private suspend fun handleEstimateRoutePlan(request: EstimateRoutePlanRequest): EstimateRoutePlanResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanCalculationService/EstimateRoutePlan") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EstimateRoutePlan", request)
            log.atInfo().addKeyValue("operation", "EstimateRoutePlan").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.routePlan.routePlanId).log("Recorded audit detail")
            var reply0 = ValidateRouteResponse.getDefaultInstance()
            request.routePlan.linesList.asFlow().map { item ->
                val outbound0 = validateRouteRequest {
                    this.context = request.context
                    this.route = route {
                        this.lines.addAll(listOf(routeLine {
                            this.amount = item.tax.amount
                            this.tax = routeLineTax {
                                this.jurisdiction = item.tax.jurisdiction
                            }
                        }))
                    }
                }
                routingRouteValidationService.validateRoute(outbound0)
            }.collect { reply0 = it }
            estimateRoutePlanResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.routePlan = routePlan {
                    this.number = request.routePlan.number
                    this.routePlanId = request.routePlanId
                }
            }
        }

    private suspend fun handleGetRoutePlanRates(request: GetRoutePlanRatesRequest): GetRoutePlanRatesResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanCalculationService/GetRoutePlanRates") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetRoutePlanRates", request)
            log.atInfo().addKeyValue("operation", "GetRoutePlanRates").log("Handling transport request")
            val outbound0 = checkRouteEligibilityRequest {
                this.context = request.context
                this.details = CheckRouteEligibilityRequestKt.details {
                    this.locale = request.context.locale
                }
                this.route = route {
                    this.audit = auditInfo {
                        this.createdAt = request.context.deadline
                    }
                    this.counterparty = driverRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = withTimeout(5_000) { routingRouteValidationService.checkRouteEligibility(outbound0) }
            getRoutePlanRatesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.routePlan = routePlan {
                    this.number = reply0.route.number
                    this.routePlanId = request.routePlanId
                }
            }
        }

    private suspend fun handleQuoteRoutePlan(request: QuoteRoutePlanRequest): QuoteRoutePlanResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanCalculationService/QuoteRoutePlan") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "QuoteRoutePlan", request)
            log.atInfo().addKeyValue("operation", "QuoteRoutePlan").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.routePlan.routePlanId).log("Recorded audit detail")
            val configure0: LintLedgerAccountsRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.ledgerAccount = ledgerAccount {
                    this.audit = auditInfo {
                        this.updatedBy = request.routePlan.audit.updatedBy
                    }
                    this.card = ledgerAccountCardFunding {
                        this.last4 = request.routePlan.card.last4
                        this.network = request.routePlan.card.network
                    }
                    this.holder = postingBatchRef {
                        this.number = request.routePlan.number
                    }
                }
            }
            val outbound0 = lintLedgerAccountsRequest(configure0)
            val reply0 = ledgerLedgerAccountValidationService.lintLedgerAccounts(outbound0)
            var reply1 = PreviewRouteResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<RoutePlanAttachment>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound1 = previewRouteRequest {
                            this.context = request.context
                            this.route = route {
                                this.attachments.addAll(listOf(routeAttachment {
                                    this.sizeBytes = item.sizeBytes
                                }))
                            }
                        }
                        reply1 = routingRouteValidationService.previewRoute(outbound1)
                    }
                }
                try {
                    request.routePlan.attachmentsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            var reply2 = CalculateRouteResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.routePlan.linesList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound2 = calculateRouteRequest {
                                this.context = request.context
                                this.route = route {
                                    this.lines.addAll(listOf(routeLine {
                                        this.tax = routeLineTax {
                                            this.amount = item.tax.amount
                                        }
                                    }))
                                }
                            }
                            routingRouteCalculationService.calculateRoute(outbound2)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply2 = it }
                }
            }
            quoteRoutePlanResponse {
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
                this.routePlan = routePlan {
                    this.notes = request.routePlan.notes
                    this.routePlanId = request.routePlanId
                }
            }
        }

    private fun RecalculateRoutePlansRequest.toFindNotificationsFromRecalculateRoutePlans1(): FindNotificationsRequest {
        val source = this
        return findNotificationsRequest {
            this.context = source.context
            this.filter = FindNotificationsRequestKt.filter {
                this.region = source.routePlan.billingAddress.region
            }
        }
    }

    private suspend fun handleSimulateRoutePlan(request: SimulateRoutePlanRequest): SimulateRoutePlanResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanCalculationService/SimulateRoutePlan") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SimulateRoutePlan", request)
            log.atInfo().addKeyValue("operation", "SimulateRoutePlan").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.routePlan.routePlanId).log("Recorded audit detail")
            val outbound0 = estimateRouteRequest {
                this.context = request.context
                this.details = EstimateRouteRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.details.destination.region
                    }
                }
                this.route = route {
                    this.card = routeCardSettlement {
                        this.holderName = when (request.routePlan.settlementCase) {
                            RoutePlan.SettlementCase.CARD -> request.routePlan.card.holderName
                            else -> ""
                        }
                    }
                }
            }
            val reply0 = routingRouteCalculationService.estimateRoute(outbound0)
            simulateRoutePlanResponse {
                this.breakdown = SimulateRoutePlanResponseKt.breakdown {
                    this.total = request.routePlan.total
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.routePlan = routePlan {
                    this.billingAddress = reply0.route.billingAddress
                    this.routePlanId = request.routePlanId
                }
            }
        }
}
