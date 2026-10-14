package com.acme.transport.yards.grpc

import com.acme.audit.v1.AuditEventQueryServiceGrpcKt
import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.BrowseAuditEventsRequestKt
import com.acme.audit.v1.NormalizeAuditEventRequest
import com.acme.audit.v1.NormalizeAuditEventRequestKt
import com.acme.audit.v1.NormalizeAuditEventResponse
import com.acme.audit.v1.auditEvent
import com.acme.audit.v1.auditTrailRef
import com.acme.audit.v1.browseAuditEventsRequest
import com.acme.audit.v1.normalizeAuditEventRequest
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.identity.v1.ExportUsersRequestKt
import com.acme.identity.v1.ResolveUsersRequest
import com.acme.identity.v1.UserQueryServiceGrpcKt
import com.acme.identity.v1.exportUsersRequest
import com.acme.identity.v1.resolveUsersRequest
import com.acme.identity.v1.userRef
import com.acme.metrics.v1.MetricSeriesComponent
import com.acme.metrics.v1.MetricSeriesQueryServiceGrpcKt
import com.acme.metrics.v1.MetricSeriesValidationServiceGrpcKt
import com.acme.metrics.v1.SuggestMetricSeriesesRequestKt
import com.acme.metrics.v1.SuggestMetricSeriesesResponse
import com.acme.metrics.v1.ValidateMetricSeriesRequest
import com.acme.metrics.v1.ValidateMetricSeriesRequestKt
import com.acme.metrics.v1.ValidateMetricSeriesResponse
import com.acme.metrics.v1.metricSeries
import com.acme.metrics.v1.suggestMetricSeriesesRequest
import com.acme.metrics.v1.validateMetricSeriesRequest
import com.acme.ticketing.v1.TicketServiceGrpcKt
import com.acme.ticketing.v1.WatchTicketsRequestKt
import com.acme.ticketing.v1.watchTicketsRequest
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.transport.yards.YardsRepository
import com.acme.venues.v1.VenueServiceGrpcKt
import com.acme.venues.v1.updateVenueRequest
import com.acme.venues.v1.venue
import com.acme.yards.v1.CalculateYardMoveRequest
import com.acme.yards.v1.CalculateYardMoveResponse
import com.acme.yards.v1.CompareYardMoveOptionsRequest
import com.acme.yards.v1.CompareYardMoveOptionsResponse
import com.acme.yards.v1.CompareYardMoveOptionsResponseKt
import com.acme.yards.v1.DockCalculationServiceGrpcKt
import com.acme.yards.v1.EstimateYardMoveRequest
import com.acme.yards.v1.EstimateYardMoveResponse
import com.acme.yards.v1.EstimateYardRequestKt
import com.acme.yards.v1.ExplainYardCalculationRequest
import com.acme.yards.v1.ExplainYardCalculationResponse
import com.acme.yards.v1.ExplainYardMoveCalculationRequest
import com.acme.yards.v1.ExplainYardMoveCalculationResponse
import com.acme.yards.v1.GetYardMoveRatesRequest
import com.acme.yards.v1.GetYardMoveRatesResponse
import com.acme.yards.v1.QuoteYardMoveRequest
import com.acme.yards.v1.QuoteYardMoveResponse
import com.acme.yards.v1.QuoteYardMoveResponseKt
import com.acme.yards.v1.SimulateYardMoveRequest
import com.acme.yards.v1.SimulateYardMoveResponse
import com.acme.yards.v1.SimulateYardRequestKt
import com.acme.yards.v1.YardCalculationServiceGrpcKt
import com.acme.yards.v1.YardMoveCalculationServiceGrpcKt
import com.acme.yards.v1.YardMoveStatusChange
import com.acme.yards.v1.YardValidationServiceGrpcKt
import com.acme.yards.v1.calculateYardMoveResponse
import com.acme.yards.v1.compareYardMoveOptionsResponse
import com.acme.yards.v1.compareYardOptionsRequest
import com.acme.yards.v1.copy as yardsCopy
import com.acme.yards.v1.dock
import com.acme.yards.v1.estimateDockRequest
import com.acme.yards.v1.estimateYardMoveResponse
import com.acme.yards.v1.estimateYardRequest
import com.acme.yards.v1.explainYardCalculationRequest
import com.acme.yards.v1.explainYardMoveCalculationResponse
import com.acme.yards.v1.getYardMoveRatesResponse
import com.acme.yards.v1.normalizeYardRequest
import com.acme.yards.v1.quoteYardMoveResponse
import com.acme.yards.v1.recalculateYardsRequest
import com.acme.yards.v1.simulateYardMoveResponse
import com.acme.yards.v1.simulateYardRequest
import com.acme.yards.v1.yard
import com.acme.yards.v1.yardDimensions
import com.acme.yards.v1.yardMove
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Computes the figures of a yard move, such as totals, taxes, and estimates. */
class YardMoveCalculationEndpoint(
    private val store: YardsRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventQueryService: AuditEventQueryServiceGrpcKt.AuditEventQueryServiceCoroutineStub,
    private val auditAuditEventValidationService: AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub,
    private val identityUserQueryService: UserQueryServiceGrpcKt.UserQueryServiceCoroutineStub,
    private val metricsMetricSeriesQueryService: MetricSeriesQueryServiceGrpcKt.MetricSeriesQueryServiceCoroutineStub,
    private val metricsMetricSeriesValidationService: MetricSeriesValidationServiceGrpcKt.MetricSeriesValidationServiceCoroutineStub,
    private val ticketingTicketService: TicketServiceGrpcKt.TicketServiceCoroutineStub,
    private val venuesVenueService: VenueServiceGrpcKt.VenueServiceCoroutineStub,
    private val yardsDockCalculationService: DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub,
    private val yardsYardCalculationService: YardCalculationServiceGrpcKt.YardCalculationServiceCoroutineStub,
    private val yardsYardValidationService: YardValidationServiceGrpcKt.YardValidationServiceCoroutineStub,
) : YardMoveCalculationServiceGrpcKt.YardMoveCalculationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(YardMoveCalculationEndpoint::class.java)

    private val invokeCalculateYardMove1: suspend (NormalizeAuditEventRequest) -> NormalizeAuditEventResponse = { payload ->
        auditAuditEventValidationService.normalizeAuditEvent(payload)
    }

    private val workerForExplainYardMoveCalculation1 = ReplyMailbox<ExplainYardCalculationRequest, ExplainYardCalculationResponse>(workers) { payload ->
        withTimeout(5_000) { yardsYardCalculationService.explainYardCalculation(payload) }
    }

    private val clientForExplainYardMoveCalculation2 by lazy { yardsYardCalculationService }

    /** Calculates a yard move. */
    override suspend fun calculateYardMove(request: CalculateYardMoveRequest): CalculateYardMoveResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveCalculationService/CalculateYardMove") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CalculateYardMove", request)
            log.atInfo().addKeyValue("operation", "CalculateYardMove").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yardMove.yardMoveId).log("Recorded audit detail")
            var reply0 = SuggestMetricSeriesesResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<YardMoveStatusChange>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound0 = suggestMetricSeriesesRequest {
                            this.context = request.context
                            this.filter = SuggestMetricSeriesesRequestKt.filter {
                                this.createdAfter = item.changedAt
                                this.region = request.details.destination.region
                            }
                        }
                        reply0 = metricsMetricSeriesQueryService.suggestMetricSerieses(outbound0)
                    }
                }
                try {
                    request.yardMove.historyList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound1 = normalizeAuditEventRequest {
                this.auditEvent = auditEvent {
                    this.subject = auditTrailRef {
                        this.displayName = request.yardMove.customer.displayName
                    }
                }
                this.context = request.context
                this.details = NormalizeAuditEventRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.yardMove.assignee.email
                        this.phone = request.yardMove.requester.phone
                        this.preferredChannel = request.yardMove.requester.preferredChannel
                    }
                }
            }
            val reply1 = invokeCalculateYardMove1(outbound1)
            calculateYardMoveResponse {
                this.result = operationResult {
                    this.status = reply1.result.status
                }
                this.yardMove = yardMove {
                    this.assignee = request.yardMove.assignee
                    this.yardMoveId = request.yardMoveId
                }
            }
        }

    /** Compares a yard move. */
    override suspend fun compareYardMoveOptions(request: CompareYardMoveOptionsRequest): CompareYardMoveOptionsResponse {
        return handleCompareYardMoveOptions(request)
    }

    /** Estimates a yard move. */
    override suspend fun estimateYardMove(request: EstimateYardMoveRequest): EstimateYardMoveResponse = handleEstimateYardMove(request)

    /** Explains a yard move. */
    override suspend fun explainYardMoveCalculation(request: ExplainYardMoveCalculationRequest): ExplainYardMoveCalculationResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveCalculationService/ExplainYardMoveCalculation") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainYardMoveCalculation", request)
            log.atInfo().addKeyValue("operation", "ExplainYardMoveCalculation").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yardMove.yardMoveId).log("Recorded audit detail")
            val outbound0 = estimateDockRequest {
                this.context = request.context
                this.dock = dock {
                    this.audit = auditInfo {
                        this.updatedAt = request.yardMove.audit.updatedAt
                    }
                    this.owner = userRef {
                        this.displayName = request.yardMove.customer.displayName
                        this.userId = request.yardMove.assignee.userId
                    }
                }
            }
            val reply0 = yardsDockCalculationService.estimateDock(WireCopies.reflective(outbound0))
            val outbound1 = explainYardCalculationRequest {
                this.context = request.context
                this.yard = yard {
                    this.audit = auditInfo {
                        this.updatedAt = request.yardMove.audit.updatedAt
                    }
                    this.dimensions = yardDimensions {
                        this.width = reply0.dock.dimensions.width
                    }
                }
            }
            val reply1 = workerForExplainYardMoveCalculation1.submit(outbound1)
            val outbound2 = estimateYardRequest {
                this.context = request.context
                this.yard = yard {
                    this.address = postalAddress {
                        this.locality = reply1.yard.address.locality
                    }
                    this.audit = auditInfo {
                        this.createdBy = reply1.yard.audit.createdBy
                    }
                    this.owner = userRef {
                        this.userId = reply0.dock.owner.userId
                    }
                }
            }
            val reply2 = clientForExplainYardMoveCalculation2.estimateYard(outbound2)
            val outbound3 = normalizeYardRequest {
                this.context = request.context
                this.yard = yard {
                    this.address = postalAddress {
                        this.postalCode = reply0.dock.address.postalCode
                    }
                }
            }
            val reply3 = flowOf(outbound3).map { value -> yardsYardValidationService.normalizeYard(value) }.single()
            explainYardMoveCalculationResponse {
                this.result = operationResult {
                    this.message = reply1.outcome.message
                    this.status = "ok"
                }
                this.yardMove = yardMove {
                    this.assignee = userRef {
                        this.userId = reply2.yard.owner.userId
                    }
                    this.audit = auditInfo {
                        this.createdAt = reply2.yard.audit.createdAt
                        this.createdBy = reply0.dock.audit.createdBy
                        this.updatedAt = reply0.dock.audit.updatedAt
                    }
                    this.customFields.putAll(request.yardMove.customFieldsMap)
                    this.requester = contactInfo {
                        this.email = reply3.yard.owner.email
                    }
                    this.yardMoveId = request.yardMoveId
                }
            }
        }

    /** Returns a yard move. */
    override suspend fun getYardMoveRates(request: GetYardMoveRatesRequest): GetYardMoveRatesResponse {
        return handleGetYardMoveRates(request)
    }

    /** Quotes a yard move. */
    override suspend fun quoteYardMove(request: QuoteYardMoveRequest): QuoteYardMoveResponse = handleQuoteYardMove(request)

    /** Simulates a yard move. */
    override suspend fun simulateYardMove(request: SimulateYardMoveRequest): SimulateYardMoveResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveCalculationService/SimulateYardMove") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SimulateYardMove", request)
            log.atInfo().addKeyValue("operation", "SimulateYardMove").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yardMove.yardMoveId).log("Recorded audit detail")
            val outbound0 = estimateYardRequest {}.yardsCopy {
                this.context = request.context
                this.details = EstimateYardRequestKt.details {
                    this.destination = postalAddress {
                        this.locality = request.details.destination.locality
                    }
                }
                this.yard = yard {
                    this.audit = auditInfo {
                        this.updatedBy = request.yardMove.audit.updatedBy
                    }
                    this.owner = userRef {
                        this.displayName = request.yardMove.customer.displayName
                        this.email = request.yardMove.assignee.email
                    }
                }
            }
            val reply0 = yardsYardCalculationService.estimateYard(outbound0)
            val outbound1 = request.toResolveUsersFromSimulateYardMove1()
            val reply1 = identityUserQueryService.resolveUsers(outbound1)
            simulateYardMoveResponse {
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = "ok"
                }
                this.yardMove = yardMove {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.yard.audit.updatedBy
                    }
                    this.status = request.yardMove.status
                    this.yardMoveId = request.yardMoveId
                }
            }
        }

    private suspend fun handleCompareYardMoveOptions(request: CompareYardMoveOptionsRequest): CompareYardMoveOptionsResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveCalculationService/CompareYardMoveOptions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CompareYardMoveOptions", request)
            log.atInfo().addKeyValue("operation", "CompareYardMoveOptions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yardMove.yardMoveId).log("Recorded audit detail")
            val outbound0 = validateMetricSeriesRequest {
                this.context = request.context
                this.details = ValidateMetricSeriesRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.yardMove.assignee.email
                    }
                }
                this.metricSeries = metricSeries {
                    this.address = postalAddress {
                        this.locality = request.details.destination.locality
                    }
                    this.audit = auditInfo {
                        this.createdAt = request.yardMove.audit.createdAt
                    }
                }
            }
            val invoke0: suspend (ValidateMetricSeriesRequest, Metadata) -> ValidateMetricSeriesResponse = metricsMetricSeriesValidationService::validateMetricSeries
            val reply0 = invoke0(outbound0, Metadata())
            val outbound1 = exportUsersRequest {
                this.context = request.context
                this.filter = ExportUsersRequestKt.filter {
                    this.createdAfter = request.yardMove.audit.createdAt
                    this.ownerId = (reply0.metricSeries.componentsMap["primary"] ?: MetricSeriesComponent.getDefaultInstance()).componentId
                    this.region = reply0.metricSeries.address.region
                }
            }
            identityUserQueryService.exportUsers(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            compareYardMoveOptionsResponse {
                this.breakdown = CompareYardMoveOptionsResponseKt.breakdown {
                    this.currencyCode = request.details.currencyCode
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.yardMove = yardMove {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.metricSeries.audit.updatedBy
                    }
                    this.yardMoveId = request.yardMoveId
                }
            }
        }

    private suspend fun handleEstimateYardMove(request: EstimateYardMoveRequest): EstimateYardMoveResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveCalculationService/EstimateYardMove") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EstimateYardMove", request)
            log.atInfo().addKeyValue("operation", "EstimateYardMove").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yardMove.yardMoveId).log("Recorded audit detail")
            val outbound0 = watchTicketsRequest {
                this.context = request.context
                this.filter = WatchTicketsRequestKt.filter {
                    this.region = request.details.destination.region
                }
                this.subscriber = contactInfo {
                    this.email = request.yardMove.requester.email
                    this.name = request.yardMove.requester.name
                    this.preferredChannel = request.yardMove.requester.preferredChannel
                }
            }
            ticketingTicketService.watchTickets(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = simulateYardRequest {
                this.context = request.context
                this.details = SimulateYardRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(request.details.destination.linesList)
                    }
                }
                this.yard = yard {
                    this.audit = auditInfo {
                        this.createdAt = request.yardMove.audit.createdAt
                    }
                }
            }
            val reply1 = ClientRegistry.lookup("acme.yards.v1.YardCalculationService", YardCalculationServiceGrpcKt.YardCalculationServiceCoroutineStub::class.java).simulateYard(outbound1)
            estimateYardMoveResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.yardMove = yardMove {
                    this.assignee = userRef {
                        this.userId = reply1.yard.owner.userId
                    }
                    this.subject = request.yardMove.subject
                    this.yardMoveId = request.yardMoveId
                }
            }
        }

    private suspend fun handleGetYardMoveRates(request: GetYardMoveRatesRequest): GetYardMoveRatesResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveCalculationService/GetYardMoveRates") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetYardMoveRates", request)
            log.atInfo().addKeyValue("operation", "GetYardMoveRates").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            getYardMoveRatesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.yardMove = yardMove {
                    this.yardMoveId = request.yardMoveId
                }
            }
        }

    private suspend fun handleQuoteYardMove(request: QuoteYardMoveRequest): QuoteYardMoveResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveCalculationService/QuoteYardMove") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "QuoteYardMove", request)
            log.atInfo().addKeyValue("operation", "QuoteYardMove").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yardMove.yardMoveId).log("Recorded audit detail")
            val outbound0 = recalculateYardsRequest {
                this.context = request.context
                this.yard = yard {
                    this.address = postalAddress {
                        this.locality = request.details.destination.locality
                    }
                }
            }
            val reply0 = withTimeout(5_000) { yardsYardCalculationService.recalculateYards(outbound0) }
            val configure1: BrowseAuditEventsRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.filter = BrowseAuditEventsRequestKt.filter {
                    this.region = reply0.yard.address.region
                }
            }
            val outbound1 = browseAuditEventsRequest(configure1)
            val reply1 = auditAuditEventQueryService.browseAuditEvents(outbound1)
            val outbound2 = compareYardOptionsRequest {
                this.context = request.context
                this.yard = yard {
                    this.owner = userRef {
                        this.email = request.yardMove.assignee.email
                    }
                }
            }
            val reply2 = yardsYardCalculationService.compareYardOptions(outbound2)
            val outbound3 = updateVenueRequest {
                this.context = request.context
                this.venue = venue {
                    this.address = postalAddress {
                        this.postalCode = reply2.yard.address.postalCode
                    }
                }
            }
            val reply3 = venuesVenueService.updateVenue(outbound3)
            quoteYardMoveResponse {
                this.breakdown = QuoteYardMoveResponseKt.breakdown {
                    this.currencyCode = reply2.breakdown.currencyCode
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.message = reply1.result.message
                    this.status = reply3.result.status
                }
                this.yardMove = yardMove {
                    this.assignee = userRef {
                        this.userId = reply0.yard.owner.userId
                    }
                    this.requester = contactInfo {
                        this.email = reply2.yard.owner.email
                    }
                    this.status = request.yardMove.status
                    this.tags.addAll(reply0.yard.tagsList)
                    this.yardMoveId = request.yardMoveId
                }
            }
        }

    private fun SimulateYardMoveRequest.toResolveUsersFromSimulateYardMove1(): ResolveUsersRequest {
        val source = this
        return resolveUsersRequest {
            this.context = source.context
        }
    }
}
