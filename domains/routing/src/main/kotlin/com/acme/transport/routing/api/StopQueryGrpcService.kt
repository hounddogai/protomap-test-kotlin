package com.acme.transport.routing.api

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.customerRef
import com.acme.drivers.v1.driverRef
import com.acme.fuel.v1.FuelTransactionCalculationServiceGrpcKt
import com.acme.fuel.v1.QuoteFuelTransactionRequest
import com.acme.fuel.v1.QuoteFuelTransactionRequestKt
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.quoteFuelTransactionRequest
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.LedgerAccountValidationServiceGrpcKt
import com.acme.ledger.v1.ScoreLedgerAccountRequest
import com.acme.ledger.v1.ScoreLedgerAccountRequestKt
import com.acme.ledger.v1.ScoreLedgerAccountResponse
import com.acme.ledger.v1.ledgerAccount
import com.acme.ledger.v1.scoreLedgerAccountRequest
import com.acme.routing.v1.BrowseStopsRequest
import com.acme.routing.v1.BrowseStopsResponse
import com.acme.routing.v1.CalculateRoutePlanRequestKt
import com.acme.routing.v1.CountStopsRequest
import com.acme.routing.v1.CountStopsResponse
import com.acme.routing.v1.EstimateStopRequest
import com.acme.routing.v1.EstimateStopRequestKt
import com.acme.routing.v1.ExplainRouteDecisionRequest
import com.acme.routing.v1.ExplainRouteDecisionRequestKt
import com.acme.routing.v1.ExplainRouteDecisionResponse
import com.acme.routing.v1.ExportRoutesRequestKt
import com.acme.routing.v1.FindRoutesRequestKt
import com.acme.routing.v1.FindStopsRequest
import com.acme.routing.v1.FindStopsResponse
import com.acme.routing.v1.LookupStopRequest
import com.acme.routing.v1.LookupStopResponse
import com.acme.routing.v1.PeekStopRequest
import com.acme.routing.v1.PeekStopResponse
import com.acme.routing.v1.QuoteRoutePlanRequest
import com.acme.routing.v1.ResolveStopsRequest
import com.acme.routing.v1.ResolveStopsResponse
import com.acme.routing.v1.RoutePlanCalculationServiceGrpcKt
import com.acme.routing.v1.RouteQueryServiceGrpcKt
import com.acme.routing.v1.RouteValidationServiceGrpcKt
import com.acme.routing.v1.StopCalculationServiceGrpcKt
import com.acme.routing.v1.StopQueryServiceGrpcKt
import com.acme.routing.v1.SuggestStopsRequest
import com.acme.routing.v1.SuggestStopsResponse
import com.acme.routing.v1.browseStopsResponse
import com.acme.routing.v1.calculateRoutePlanRequest
import com.acme.routing.v1.countStopsResponse
import com.acme.routing.v1.estimateStopRequest
import com.acme.routing.v1.explainRouteDecisionRequest
import com.acme.routing.v1.exportRoutesRequest
import com.acme.routing.v1.findRoutesRequest
import com.acme.routing.v1.findStopsResponse
import com.acme.routing.v1.lookupStopResponse
import com.acme.routing.v1.peekStopResponse
import com.acme.routing.v1.quoteRoutePlanRequest
import com.acme.routing.v1.resolveStopsResponse
import com.acme.routing.v1.routePlan
import com.acme.routing.v1.stop
import com.acme.routing.v1.stopParticipant
import com.acme.routing.v1.suggestStopsResponse
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.transport.routing.RoutingRepository
import com.acme.yards.v1.DockValidationServiceGrpcKt
import com.acme.yards.v1.ExplainYardDecisionRequest
import com.acme.yards.v1.ExplainYardDecisionRequestKt
import com.acme.yards.v1.ExplainYardDecisionResponse
import com.acme.yards.v1.PreviewDockRequest
import com.acme.yards.v1.YardValidationServiceGrpcKt
import com.acme.yards.v1.dock
import com.acme.yards.v1.explainYardDecisionRequest
import com.acme.yards.v1.previewDockRequest
import com.acme.yards.v1.yard
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import org.slf4j.LoggerFactory

/** Answers read-only questions about stops, for screens and reports. */
class StopQueryGrpcService(
    private val store: RoutingRepository,
    private val workers: CoroutineScope,
    private val fuelFuelTransactionCalculationService: FuelTransactionCalculationServiceGrpcKt.FuelTransactionCalculationServiceCoroutineStub,
    private val ledgerLedgerAccountValidationService: LedgerAccountValidationServiceGrpcKt.LedgerAccountValidationServiceCoroutineStub,
    private val routingRoutePlanCalculationService: RoutePlanCalculationServiceGrpcKt.RoutePlanCalculationServiceCoroutineStub,
    private val routingRouteQueryService: RouteQueryServiceGrpcKt.RouteQueryServiceCoroutineStub,
    private val routingRouteValidationService: RouteValidationServiceGrpcKt.RouteValidationServiceCoroutineStub,
    private val routingStopCalculationService: StopCalculationServiceGrpcKt.StopCalculationServiceCoroutineStub,
    private val yardsDockValidationService: DockValidationServiceGrpcKt.DockValidationServiceCoroutineStub,
    private val yardsYardValidationService: YardValidationServiceGrpcKt.YardValidationServiceCoroutineStub,
) : StopQueryServiceGrpcKt.StopQueryServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(StopQueryGrpcService::class.java)

    private val invokeResolveStops0: suspend (ExplainRouteDecisionRequest) -> ExplainRouteDecisionResponse = { payload ->
        routingRouteValidationService.explainRouteDecision(payload)
    }

    /** Lists stops a page at a time. */
    override suspend fun browseStops(request: BrowseStopsRequest): BrowseStopsResponse {
        return handleBrowseStops(request)
    }

    /** Counts stops by status. */
    override suspend fun countStops(request: CountStopsRequest): CountStopsResponse = handleCountStops(request)

    /** Lists stops a page at a time. */
    override suspend fun findStops(request: FindStopsRequest): FindStopsResponse =
        RpcBoundary.run("acme.routing.v1.StopQueryService/FindStops") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "FindStops", request)
            log.atInfo().addKeyValue("operation", "FindStops").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = findRoutesRequest {
                this.context = request.context
                this.filter = FindRoutesRequestKt.filter {
                    this.labels.putAll(request.filter.labelsMap)
                    this.query = request.filter.query
                }
            }
            val reply0 = routingRouteQueryService.findRoutes(WireCopies.json(outbound0))
            findStopsResponse {
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    /** Returns a stop. */
    override suspend fun lookupStop(request: LookupStopRequest): LookupStopResponse {
        return handleLookupStop(request)
    }

    /** Returns a stop. */
    override suspend fun peekStop(request: PeekStopRequest): PeekStopResponse = handlePeekStop(request)

    /** Returns several stops at once. */
    override suspend fun resolveStops(request: ResolveStopsRequest): ResolveStopsResponse =
        RpcBoundary.run("acme.routing.v1.StopQueryService/ResolveStops") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResolveStops", request)
            log.atInfo().addKeyValue("operation", "ResolveStops").log("Handling transport request")
            val outbound0 = explainRouteDecisionRequest {
                this.context = request.context
                this.details = ExplainRouteDecisionRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                    this.locale = request.context.locale
                }
            }
            val reply0 = invokeResolveStops0(outbound0)
            resolveStopsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.stops.addAll(listOf(stop {
                    this.location = postalAddress {
                        this.region = reply0.route.billingAddress.region
                    }
                }))
            }
        }

    /** Lists stops a page at a time. */
    override suspend fun suggestStops(request: SuggestStopsRequest): SuggestStopsResponse {
        return handleSuggestStops(request)
    }

    private fun BrowseStopsRequest.toQuoteFuelTransactionFromBrowseStops1(): QuoteFuelTransactionRequest {
        val source = this
        return quoteFuelTransactionRequest {
            this.context = source.context
            this.details = QuoteFuelTransactionRequestKt.details {
                this.destination = postalAddress {
                    this.region = source.filter.region
                }
            }
            this.fuelTransaction = fuelTransaction {
                this.occurredAt = source.filter.createdAfter
                this.subject = customerRef {
                    this.email = source.context.actor.email
                }
            }
        }
    }

    private suspend fun handleBrowseStops(request: BrowseStopsRequest): BrowseStopsResponse =
        RpcBoundary.run("acme.routing.v1.StopQueryService/BrowseStops") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BrowseStops", request)
            log.atInfo().addKeyValue("operation", "BrowseStops").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = exportRoutesRequest {
                this.context = request.context
                this.filter = ExportRoutesRequestKt.filter {
                    this.labels.putAll(request.filter.labelsMap)
                    this.ownerId = request.filter.ownerId
                    this.region = request.filter.region
                }
            }
            routingRouteQueryService.exportRoutes(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = request.toQuoteFuelTransactionFromBrowseStops1()
            val reply1 = fuelFuelTransactionCalculationService.quoteFuelTransaction(outbound1)
            browseStopsResponse {
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleCountStops(request: CountStopsRequest): CountStopsResponse =
        RpcBoundary.run("acme.routing.v1.StopQueryService/CountStops") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CountStops", request)
            log.atInfo().addKeyValue("operation", "CountStops").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = calculateRoutePlanRequest {
                this.context = request.context
                this.details = CalculateRoutePlanRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.filter.region
                    }
                }
                this.routePlan = routePlan {
                    this.audit = auditInfo {
                        this.updatedAt = request.filter.createdAfter
                    }
                    this.counterparty = driverRef {
                        this.email = request.context.actor.email
                    }
                    this.dueAt = request.context.deadline
                }
            }
            val reply0 = routingRoutePlanCalculationService.calculateRoutePlan(outbound0)
            val outbound1 = estimateStopRequest {
                this.context = request.context
                this.details = EstimateStopRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(reply0.routePlan.billingAddress.linesList)
                    }
                }
                this.stop = stop {
                    this.location = postalAddress {
                        this.postalCode = reply0.routePlan.billingAddress.postalCode
                    }
                    this.participants.addAll(listOf(stopParticipant {
                        this.contact = contactInfo {
                            this.email = reply0.routePlan.counterparty.email
                        }
                    }))
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = routingStopCalculationService.estimateStop(envelope1.unpack(EstimateStopRequest::class.java))
            countStopsResponse {
                this.result = operationResult {
                    this.referenceIds.addAll((reply0.result.referenceIdsList) + (reply1.result.referenceIdsList))
                    this.status = "ok"
                }
            }
        }

    private data class LookupStop0Command(
        val context: RequestContext,
        val dockOwnerUserId: String
    ) {
        fun toRequest(): PreviewDockRequest {
            val command = this
            return previewDockRequest {
                this.context = command.context
                this.dock = dock {
                    this.owner = userRef {
                        this.userId = command.dockOwnerUserId
                    }
                }
            }
        }
    }

    private suspend fun handleLookupStop(request: LookupStopRequest): LookupStopResponse =
        RpcBoundary.run("acme.routing.v1.StopQueryService/LookupStop") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LookupStop", request)
            log.atInfo().addKeyValue("operation", "LookupStop").log("Handling transport request")
            val outbound0 = LookupStop0Command(
                context = request.context,
                dockOwnerUserId = request.context.actor.userId
            ).toRequest()
            val reply0 = yardsDockValidationService.previewDock(outbound0)
            lookupStopResponse {
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
                this.stop = stop {
                    this.location = postalAddress {
                        this.recipient = reply0.dock.address.recipient
                    }
                    this.stopId = request.stopId
                }
            }
        }

    private suspend fun handlePeekStop(request: PeekStopRequest): PeekStopResponse =
        RpcBoundary.run("acme.routing.v1.StopQueryService/PeekStop") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PeekStop", request)
            log.atInfo().addKeyValue("operation", "PeekStop").log("Handling transport request")
            val outbound0 = quoteRoutePlanRequest {
                this.context = request.context
                this.routePlan = routePlan {
                    this.audit = auditInfo {
                        this.updatedAt = request.context.deadline
                    }
                    this.counterparty = driverRef {
                        this.email = request.context.actor.email
                    }
                    this.metadata.putAll(request.context.baggageMap)
                }
            }
            val command0: Submission<QuoteRoutePlanRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = routingRoutePlanCalculationService.quoteRoutePlan(payload0)
            val outbound1 = explainYardDecisionRequest {
                this.context = request.context
                this.details = ExplainYardDecisionRequestKt.details {
                    this.contact = contactInfo {
                        this.email = reply0.routePlan.counterparty.email
                    }
                }
                this.yard = yard {
                    this.address = postalAddress {
                        this.lines.addAll(reply0.routePlan.billingAddress.linesList)
                    }
                    this.audit = auditInfo {
                        this.updatedBy = reply0.routePlan.audit.updatedBy
                    }
                    this.owner = userRef {
                        this.displayName = reply0.routePlan.counterparty.displayName
                    }
                }
            }
            val adapter1 = OutboundPort<ExplainYardDecisionRequest, ExplainYardDecisionResponse> { payload ->
                yardsYardValidationService.explainYardDecision(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            peekStopResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.stop = stop {
                    this.audit = auditInfo {
                        this.createdAt = reply1.yard.audit.createdAt
                        this.updatedAt = reply0.routePlan.audit.updatedAt
                    }
                    this.stopId = request.stopId
                }
            }
        }

    private suspend fun handleSuggestStops(request: SuggestStopsRequest): SuggestStopsResponse =
        RpcBoundary.run("acme.routing.v1.StopQueryService/SuggestStops") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SuggestStops", request)
            log.atInfo().addKeyValue("operation", "SuggestStops").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = scoreLedgerAccountRequest {
                this.context = request.context
                this.details = ScoreLedgerAccountRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.filter.region
                    }
                }
                this.ledgerAccount = ledgerAccount {
                    this.labels.putAll(request.filter.labelsMap)
                }
            }
            val invoke0: suspend (ScoreLedgerAccountRequest, Metadata) -> ScoreLedgerAccountResponse = ledgerLedgerAccountValidationService::scoreLedgerAccount
            val reply0 = invoke0(outbound0, Metadata())
            suggestStopsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.stops.addAll(listOf(stop {
                    this.audit = auditInfo {
                        this.etag = reply0.ledgerAccount.audit.etag
                    }
                    this.participants.addAll(listOf(stopParticipant {
                        this.contact = contactInfo {
                            this.name = reply0.ledgerAccount.contact.name
                        }
                    }))
                }))
            }
        }
}
