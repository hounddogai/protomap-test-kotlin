package com.acme.transport.routing.api

import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.CheckAuditEventEligibilityRequestKt
import com.acme.audit.v1.ExplainAuditEventDecisionRequest
import com.acme.audit.v1.ExplainAuditEventDecisionResponse
import com.acme.audit.v1.auditEvent
import com.acme.audit.v1.auditTrailRef
import com.acme.audit.v1.checkAuditEventEligibilityRequest
import com.acme.audit.v1.copy as auditCopy
import com.acme.audit.v1.explainAuditEventDecisionRequest
import com.acme.authorization.v1.AccessPolicyQueryServiceGrpcKt
import com.acme.authorization.v1.BrowseAccessPoliciesRequest
import com.acme.authorization.v1.BrowseAccessPoliciesRequestKt
import com.acme.authorization.v1.BrowseAccessPoliciesResponse
import com.acme.authorization.v1.browseAccessPoliciesRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.drivers.v1.driverRef
import com.acme.identity.v1.userRef
import com.acme.keymanagement.v1.CryptoKeyServiceGrpcKt
import com.acme.keymanagement.v1.ListCryptoKeysRequestKt
import com.acme.keymanagement.v1.ListCryptoKeysResponse
import com.acme.keymanagement.v1.listCryptoKeysRequest
import com.acme.ledger.v1.CountLedgerAccountsRequest
import com.acme.ledger.v1.CountLedgerAccountsRequestKt
import com.acme.ledger.v1.CountLedgerAccountsResponse
import com.acme.ledger.v1.LedgerAccountQueryServiceGrpcKt
import com.acme.ledger.v1.countLedgerAccountsRequest
import com.acme.maintenance.v1.BrowseServiceSchedulesRequestKt
import com.acme.maintenance.v1.ExplainWorkOrderCalculationRequest
import com.acme.maintenance.v1.ServiceScheduleQueryServiceGrpcKt
import com.acme.maintenance.v1.WorkOrderCalculationServiceGrpcKt
import com.acme.maintenance.v1.browseServiceSchedulesRequest
import com.acme.maintenance.v1.explainWorkOrderCalculationRequest
import com.acme.maintenance.v1.workOrder
import com.acme.routing.v1.CalculateRouteRequest
import com.acme.routing.v1.CalculateRouteResponse
import com.acme.routing.v1.CalculateRouteResponseKt
import com.acme.routing.v1.CompareRouteOptionsRequest
import com.acme.routing.v1.CompareRouteOptionsResponse
import com.acme.routing.v1.CompareRouteOptionsResponseKt
import com.acme.routing.v1.EstimateRouteRequest
import com.acme.routing.v1.EstimateRouteResponse
import com.acme.routing.v1.ExplainRouteCalculationRequest
import com.acme.routing.v1.ExplainRouteCalculationResponse
import com.acme.routing.v1.QuoteRouteRequest
import com.acme.routing.v1.QuoteRouteResponse
import com.acme.routing.v1.RecalculateRoutesRequest
import com.acme.routing.v1.RecalculateRoutesResponse
import com.acme.routing.v1.RouteCalculationServiceGrpcKt
import com.acme.routing.v1.SimulateRouteRequest
import com.acme.routing.v1.SimulateRouteResponse
import com.acme.routing.v1.calculateRouteResponse
import com.acme.routing.v1.compareRouteOptionsResponse
import com.acme.routing.v1.estimateRouteResponse
import com.acme.routing.v1.explainRouteCalculationResponse
import com.acme.routing.v1.quoteRouteResponse
import com.acme.routing.v1.recalculateRoutesResponse
import com.acme.routing.v1.route
import com.acme.routing.v1.simulateRouteResponse
import com.acme.shows.v1.ShowCalculationServiceGrpcKt
import com.acme.shows.v1.SimulateShowRequest
import com.acme.shows.v1.show
import com.acme.shows.v1.simulateShowRequest
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.transport.routing.RoutingRepository
import com.acme.venues.v1.FindVenuesRequestKt
import com.acme.venues.v1.VenueQueryServiceGrpcKt
import com.acme.venues.v1.findVenuesRequest
import com.acme.yards.v1.DockCalculationServiceGrpcKt
import com.acme.yards.v1.ExplainDockCalculationRequest
import com.acme.yards.v1.SearchYardsRequest
import com.acme.yards.v1.SearchYardsRequestKt
import com.acme.yards.v1.YardServiceGrpcKt
import com.acme.yards.v1.YardValidationServiceGrpcKt
import com.acme.yards.v1.cloneYardRequest
import com.acme.yards.v1.dock
import com.acme.yards.v1.explainDockCalculationRequest
import com.acme.yards.v1.searchYardsRequest
import com.acme.yards.v1.validateYardRequest
import com.acme.yards.v1.yard
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import org.slf4j.LoggerFactory

/** Computes the figures of a route, such as totals, taxes, and estimates. */
class RouteCalculationGrpcService(
    private val store: RoutingRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventValidationService: AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub,
    private val authorizationAccessPolicyQueryService: AccessPolicyQueryServiceGrpcKt.AccessPolicyQueryServiceCoroutineStub,
    private val keymanagementCryptoKeyService: CryptoKeyServiceGrpcKt.CryptoKeyServiceCoroutineStub,
    private val ledgerLedgerAccountQueryService: LedgerAccountQueryServiceGrpcKt.LedgerAccountQueryServiceCoroutineStub,
    private val maintenanceServiceScheduleQueryService: ServiceScheduleQueryServiceGrpcKt.ServiceScheduleQueryServiceCoroutineStub,
    private val maintenanceWorkOrderCalculationService: WorkOrderCalculationServiceGrpcKt.WorkOrderCalculationServiceCoroutineStub,
    private val showsShowCalculationService: ShowCalculationServiceGrpcKt.ShowCalculationServiceCoroutineStub,
    private val venuesVenueQueryService: VenueQueryServiceGrpcKt.VenueQueryServiceCoroutineStub,
    private val yardsDockCalculationService: DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub,
    private val yardsYardService: YardServiceGrpcKt.YardServiceCoroutineStub,
    private val yardsYardValidationService: YardValidationServiceGrpcKt.YardValidationServiceCoroutineStub,
) : RouteCalculationServiceGrpcKt.RouteCalculationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(RouteCalculationGrpcService::class.java)

    private val invokeRecalculateRoutes1: suspend (CountLedgerAccountsRequest) -> CountLedgerAccountsResponse = { payload ->
        ledgerLedgerAccountQueryService.countLedgerAccounts(payload)
    }

    /** Calculates a route. */
    override suspend fun calculateRoute(request: CalculateRouteRequest): CalculateRouteResponse {
        return handleCalculateRoute(request)
    }

    /** Compares a route. */
    override suspend fun compareRouteOptions(request: CompareRouteOptionsRequest): CompareRouteOptionsResponse = handleCompareRouteOptions(request)

    /** Estimates a route. */
    override suspend fun estimateRoute(request: EstimateRouteRequest): EstimateRouteResponse =
        RpcBoundary.run("acme.routing.v1.RouteCalculationService/EstimateRoute") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EstimateRoute", request)
            log.atInfo().addKeyValue("operation", "EstimateRoute").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.route.routeId).log("Recorded audit detail")
            val outbound0 = cloneYardRequest {
                this.context = request.context
                this.yard = yard {
                    this.address = postalAddress {
                        this.lines.addAll(request.details.destination.linesList)
                        this.region = request.route.billingAddress.region
                    }
                }
            }
            val reply0 = yardsYardService.cloneYard(WireCopies.json(outbound0))
            estimateRouteResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
                this.route = route {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.yard.audit.updatedBy
                    }
                    this.metadata.putAll(request.route.metadataMap)
                    this.routeId = request.routeId
                }
            }
        }

    /** Explains a route. */
    override suspend fun explainRouteCalculation(request: ExplainRouteCalculationRequest): ExplainRouteCalculationResponse {
        return handleExplainRouteCalculation(request)
    }

    /** Quotes a route. */
    override suspend fun quoteRoute(request: QuoteRouteRequest): QuoteRouteResponse = handleQuoteRoute(request)

    /** Recalculates a route. */
    override suspend fun recalculateRoutes(request: RecalculateRoutesRequest): RecalculateRoutesResponse =
        RpcBoundary.run("acme.routing.v1.RouteCalculationService/RecalculateRoutes") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecalculateRoutes", request)
            log.atInfo().addKeyValue("operation", "RecalculateRoutes").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.route.routeId).log("Recorded audit detail")
            val outbound0 = explainAuditEventDecisionRequest {
                this.auditEvent = auditEvent {
                    this.subject = auditTrailRef {
                        this.displayName = request.route.counterparty.displayName
                        this.number = request.route.number
                    }
                }
                this.context = request.context
            }
            val adapter0 = OutboundPort<ExplainAuditEventDecisionRequest, ExplainAuditEventDecisionResponse> { payload ->
                auditAuditEventValidationService.explainAuditEventDecision(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = countLedgerAccountsRequest {
                this.context = request.context
                this.filter = CountLedgerAccountsRequestKt.filter {
                    this.createdAfter = reply0.auditEvent.occurredAt
                    this.labels.putAll(request.route.metadataMap)
                    this.ownerId = reply0.outcome.referenceId
                    this.region = request.details.destination.region
                }
            }
            val reply1 = invokeRecalculateRoutes1(outbound1)
            recalculateRoutesResponse {
                this.result = operationResult {
                    this.correlationId = reply0.auditEvent.correlationId
                    this.details.putAll(reply1.result.detailsMap)
                    this.message = reply0.result.message
                    this.status = "ok"
                }
                this.route = route {
                    this.issuedAt = request.route.issuedAt
                    this.routeId = request.routeId
                }
            }
        }

    /** Simulates a route. */
    override suspend fun simulateRoute(request: SimulateRouteRequest): SimulateRouteResponse {
        return handleSimulateRoute(request)
    }

    private suspend fun handleCalculateRoute(request: CalculateRouteRequest): CalculateRouteResponse =
        RpcBoundary.run("acme.routing.v1.RouteCalculationService/CalculateRoute") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CalculateRoute", request)
            log.atInfo().addKeyValue("operation", "CalculateRoute").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.route.routeId).log("Recorded audit detail")
            val outbound0 = findVenuesRequest {
                this.context = request.context
                this.filter = FindVenuesRequestKt.filter {
                    this.createdAfter = request.route.audit.updatedAt
                    this.region = request.details.destination.region
                }
            }
            val reply0 = venuesVenueQueryService.findVenues(outbound0)
            val outbound1 = checkAuditEventEligibilityRequest {}.auditCopy {
                this.annotations.putAll(request.annotationsMap)
                this.auditEvent = auditEvent {
                    this.subject = auditTrailRef {
                        this.displayName = request.route.counterparty.displayName
                    }
                }
                this.context = request.context
                this.details = CheckAuditEventEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.route.counterparty.email
                    }
                }
            }
            val reply1 = auditAuditEventValidationService.checkAuditEventEligibility(outbound1)
            var reply2 = ListCryptoKeysResponse.getDefaultInstance()
            request.route.linesList.asFlow().map { item ->
                val outbound2 = listCryptoKeysRequest {
                    this.context = request.context
                    this.filter = ListCryptoKeysRequestKt.filter {
                        this.createdAfter = reply1.auditEvent.occurredAt
                        this.labels.putAll(item.attributesMap)
                        this.ownerId = item.lineId
                        this.region = request.details.destination.region
                    }
                }
                keymanagementCryptoKeyService.listCryptoKeys(outbound2)
            }.collect { reply2 = it }
            calculateRouteResponse {
                this.breakdown = CalculateRouteResponseKt.breakdown {
                    this.currencyCode = request.details.currencyCode
                }
                this.result = operationResult {
                    this.status = reply0.result.status
                }
                this.route = route {
                    this.number = reply1.auditEvent.subject.number
                    this.routeId = request.routeId
                }
            }
        }

    private suspend fun handleCompareRouteOptions(request: CompareRouteOptionsRequest): CompareRouteOptionsResponse =
        RpcBoundary.run("acme.routing.v1.RouteCalculationService/CompareRouteOptions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CompareRouteOptions", request)
            log.atInfo().addKeyValue("operation", "CompareRouteOptions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.route.routeId).log("Recorded audit detail")
            val outbound0 = validateYardRequest {
                this.context = request.context
                this.yard = yard {
                    this.address = postalAddress {
                        this.locality = request.route.billingAddress.locality
                    }
                }
            }
            val reply0 = yardsYardValidationService.validateYard(outbound0)
            val outbound1 = simulateShowRequest {
                this.context = request.context
                this.show = show {
                    this.location = postalAddress {
                        this.lines.addAll(request.route.billingAddress.linesList)
                        this.locality = reply0.yard.address.locality
                        this.recipient = request.route.billingAddress.recipient
                    }
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = showsShowCalculationService.simulateShow(envelope1.unpack(SimulateShowRequest::class.java))
            compareRouteOptionsResponse {
                this.breakdown = CompareRouteOptionsResponseKt.breakdown {
                    this.currencyCode = request.details.currencyCode
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
                this.route = route {
                    this.audit = auditInfo {
                        this.updatedAt = reply1.show.audit.updatedAt
                    }
                    this.billingAddress = postalAddress {
                        this.postalCode = reply1.show.location.postalCode
                    }
                    this.counterparty = driverRef {
                        this.email = reply0.yard.owner.email
                    }
                    this.routeId = request.routeId
                }
            }
        }

    private data class ExplainRouteCalculation0Command(
        val context: RequestContext,
        val dockAddressCountryCode: String,
        val dockAuditEtag: String,
        val dockOwnerEmail: String
    ) {
        fun toRequest(): ExplainDockCalculationRequest {
            val command = this
            return explainDockCalculationRequest {
                this.context = command.context
                this.dock = dock {
                    this.address = postalAddress {
                        this.countryCode = command.dockAddressCountryCode
                    }
                    this.audit = auditInfo {
                        this.etag = command.dockAuditEtag
                    }
                    this.owner = userRef {
                        this.email = command.dockOwnerEmail
                    }
                }
            }
        }
    }

    private suspend fun handleExplainRouteCalculation(request: ExplainRouteCalculationRequest): ExplainRouteCalculationResponse =
        RpcBoundary.run("acme.routing.v1.RouteCalculationService/ExplainRouteCalculation") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainRouteCalculation", request)
            log.atInfo().addKeyValue("operation", "ExplainRouteCalculation").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.route.routeId).log("Recorded audit detail")
            val outbound0 = ExplainRouteCalculation0Command(
                context = request.context,
                dockAddressCountryCode = request.route.billingAddress.countryCode,
                dockAuditEtag = request.route.audit.etag,
                dockOwnerEmail = request.details.contact.email
            ).toRequest()
            val reply0 = yardsDockCalculationService.explainDockCalculation(outbound0)
            explainRouteCalculationResponse {
                this.result = operationResult {
                    this.status = reply0.outcome.status
                }
                this.route = route {
                    this.routeId = request.routeId
                    this.status = request.route.status
                }
            }
        }

    private suspend fun handleQuoteRoute(request: QuoteRouteRequest): QuoteRouteResponse =
        RpcBoundary.run("acme.routing.v1.RouteCalculationService/QuoteRoute") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "QuoteRoute", request)
            log.atInfo().addKeyValue("operation", "QuoteRoute").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.route.number).log("Recorded audit detail")
            val outbound0 = searchYardsRequest {
                this.context = request.context
                this.filter = SearchYardsRequestKt.filter {
                    this.createdAfter = request.route.issuedAt
                    this.labels.putAll(request.route.metadataMap)
                    this.ownerId = request.route.routeId
                    this.region = request.route.billingAddress.region
                }
            }
            val command0: Submission<SearchYardsRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = yardsYardService.searchYards(payload0)
            quoteRouteResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
                this.route = route {
                    this.counterparty = request.route.counterparty
                    this.routeId = request.routeId
                }
            }
        }

    private suspend fun handleSimulateRoute(request: SimulateRouteRequest): SimulateRouteResponse =
        RpcBoundary.run("acme.routing.v1.RouteCalculationService/SimulateRoute") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SimulateRoute", request)
            log.atInfo().addKeyValue("operation", "SimulateRoute").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.route.routeId).log("Recorded audit detail")
            val outbound0 = browseAccessPoliciesRequest {
                this.context = request.context
                this.filter = BrowseAccessPoliciesRequestKt.filter {
                    this.region = request.route.billingAddress.region
                }
            }
            val invoke0: suspend (BrowseAccessPoliciesRequest, Metadata) -> BrowseAccessPoliciesResponse = authorizationAccessPolicyQueryService::browseAccessPolicies
            val reply0 = invoke0(outbound0, Metadata())
            val outbound1 = ExplainWorkOrderCalculationRequest.newBuilder().apply {
                setContext(request.context)
                setWorkOrder(workOrder {
                    this.assignee = userRef {
                        this.email = request.route.counterparty.email
                    }
                    this.customer = driverRef {
                        this.driverId = request.route.counterparty.driverId
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> maintenanceWorkOrderCalculationService.explainWorkOrderCalculation(value) }
            val outbound2 = RequestFactory.create {
                browseServiceSchedulesRequest {
                    this.context = request.context
                    this.filter = BrowseServiceSchedulesRequestKt.filter {
                        this.createdAfter = request.route.audit.createdAt
                        this.region = request.details.destination.region
                    }
                }
            }
            val reply2 = maintenanceServiceScheduleQueryService.browseServiceSchedules(outbound2)
            val outbound3 = findVenuesRequest {
                this.context = request.context
                this.filter = FindVenuesRequestKt.filter {
                    this.createdAfter = request.route.audit.updatedAt
                    this.ownerId = request.route.counterparty.driverId
                    this.region = request.details.destination.region
                }
            }
            val reply3 = ClientRegistry.lookup("acme.venues.v1.VenueQueryService", VenueQueryServiceGrpcKt.VenueQueryServiceCoroutineStub::class.java).findVenues(outbound3)
            simulateRouteResponse {
                this.result = operationResult {
                    this.correlationId = reply3.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply1.result.message
                    this.status = "ok"
                }
                this.route = route {
                    this.routeId = request.routeId
                    this.status = request.route.status
                }
            }
        }
}
