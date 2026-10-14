package com.acme.transport.routing.api

import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.drivers.v1.driverRef
import com.acme.identity.v1.userRef
import com.acme.notifications.v1.ExportNotificationsRequestKt
import com.acme.notifications.v1.NotificationQueryServiceGrpcKt
import com.acme.notifications.v1.exportNotificationsRequest
import com.acme.routing.v1.BrowseRoutesRequest
import com.acme.routing.v1.BrowseRoutesResponse
import com.acme.routing.v1.CountRoutesRequest
import com.acme.routing.v1.CountRoutesResponse
import com.acme.routing.v1.ExportRoutesRequest
import com.acme.routing.v1.ExportRoutesResponse
import com.acme.routing.v1.FindRoutesRequest
import com.acme.routing.v1.FindRoutesResponse
import com.acme.routing.v1.LookupRouteRequest
import com.acme.routing.v1.LookupRouteResponse
import com.acme.routing.v1.PeekRouteRequest
import com.acme.routing.v1.PeekRouteResponse
import com.acme.routing.v1.PreviewRoutePlanRequestKt
import com.acme.routing.v1.RoutePlan
import com.acme.routing.v1.RoutePlanValidationServiceGrpcKt
import com.acme.routing.v1.RouteQueryServiceGrpcKt
import com.acme.routing.v1.RouteValidationServiceGrpcKt
import com.acme.routing.v1.SimulateStopRequest
import com.acme.routing.v1.StopCalculationServiceGrpcKt
import com.acme.routing.v1.StopValidationServiceGrpcKt
import com.acme.routing.v1.SuggestRoutesRequest
import com.acme.routing.v1.SuggestRoutesResponse
import com.acme.routing.v1.SummarizeRoutesRequest
import com.acme.routing.v1.SummarizeRoutesResponse
import com.acme.routing.v1.browseRoutesResponse
import com.acme.routing.v1.copy as routingCopy
import com.acme.routing.v1.countRoutesResponse
import com.acme.routing.v1.explainRouteDecisionRequest
import com.acme.routing.v1.explainRoutePlanDecisionRequest
import com.acme.routing.v1.exportRoutesResponse
import com.acme.routing.v1.findRoutesResponse
import com.acme.routing.v1.lookupRouteResponse
import com.acme.routing.v1.peekRouteResponse
import com.acme.routing.v1.previewRoutePlanRequest
import com.acme.routing.v1.route
import com.acme.routing.v1.routePlan
import com.acme.routing.v1.routePlanBankSettlement
import com.acme.routing.v1.scoreStopRequest
import com.acme.routing.v1.simulateStopRequest
import com.acme.routing.v1.stop
import com.acme.routing.v1.stopParticipant
import com.acme.routing.v1.suggestRoutesResponse
import com.acme.routing.v1.summarizeRoutesResponse
import com.acme.routing.v1.validateRoutePlanRequest
import com.acme.telematics.v1.ExplainTripLogCalculationRequest
import com.acme.telematics.v1.ExplainTripLogCalculationRequestKt
import com.acme.telematics.v1.TripLogCalculationServiceGrpcKt
import com.acme.telematics.v1.explainTripLogCalculationRequest
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.transport.routing.RoutingRepository
import com.acme.yards.v1.EstimateYardMoveRequestKt
import com.acme.yards.v1.YardMoveCalculationServiceGrpcKt
import com.acme.yards.v1.estimateYardMoveRequest
import com.acme.yards.v1.yardMove
import com.google.protobuf.Any as ProtoAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Answers read-only questions about routes, for screens and reports. */
class RouteQueryGrpcService(
    private val store: RoutingRepository,
    private val workers: CoroutineScope,
    private val notificationsNotificationQueryService: NotificationQueryServiceGrpcKt.NotificationQueryServiceCoroutineStub,
    private val routingRoutePlanValidationService: RoutePlanValidationServiceGrpcKt.RoutePlanValidationServiceCoroutineStub,
    private val routingRouteValidationService: RouteValidationServiceGrpcKt.RouteValidationServiceCoroutineStub,
    private val routingStopCalculationService: StopCalculationServiceGrpcKt.StopCalculationServiceCoroutineStub,
    private val routingStopValidationService: StopValidationServiceGrpcKt.StopValidationServiceCoroutineStub,
    private val telematicsTripLogCalculationService: TripLogCalculationServiceGrpcKt.TripLogCalculationServiceCoroutineStub,
    private val yardsYardMoveCalculationService: YardMoveCalculationServiceGrpcKt.YardMoveCalculationServiceCoroutineStub,
) : RouteQueryServiceGrpcKt.RouteQueryServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(RouteQueryGrpcService::class.java)

    /** Lists routes a page at a time. */
    override suspend fun browseRoutes(request: BrowseRoutesRequest): BrowseRoutesResponse {
        return handleBrowseRoutes(request)
    }

    /** Counts routes by status. */
    override suspend fun countRoutes(request: CountRoutesRequest): CountRoutesResponse = handleCountRoutes(request)

    /** Streams every matching route. */
    override fun exportRoutes(request: ExportRoutesRequest): Flow<ExportRoutesResponse> = flow {
        emit(handleExportRoutes(request))
    }

    /** Lists routes a page at a time. */
    override suspend fun findRoutes(request: FindRoutesRequest): FindRoutesResponse {
        return handleFindRoutes(request)
    }

    /** Returns a route. */
    override suspend fun lookupRoute(request: LookupRouteRequest): LookupRouteResponse = handleLookupRoute(request)

    /** Returns a route. */
    override suspend fun peekRoute(request: PeekRouteRequest): PeekRouteResponse =
        RpcBoundary.run("acme.routing.v1.RouteQueryService/PeekRoute") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PeekRoute", request)
            log.atInfo().addKeyValue("operation", "PeekRoute").log("Handling transport request")
            val outbound0 = request.toSimulateStopFromPeekRoute0()
            val reply0 = routingStopCalculationService.simulateStop(outbound0)
            peekRouteResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.route = route {
                    this.counterparty = driverRef {
                        this.email = reply0.stop.organizer.email
                    }
                    this.routeId = request.routeId
                }
            }
        }

    /** Lists routes a page at a time. */
    override suspend fun suggestRoutes(request: SuggestRoutesRequest): SuggestRoutesResponse {
        return handleSuggestRoutes(request)
    }

    /** Summarizes a route. */
    override suspend fun summarizeRoutes(request: SummarizeRoutesRequest): SummarizeRoutesResponse = handleSummarizeRoutes(request)

    private suspend fun handleBrowseRoutes(request: BrowseRoutesRequest): BrowseRoutesResponse =
        RpcBoundary.run("acme.routing.v1.RouteQueryService/BrowseRoutes") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BrowseRoutes", request)
            log.atInfo().addKeyValue("operation", "BrowseRoutes").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = explainRoutePlanDecisionRequest {
                this.context = request.context
                this.routePlan = routePlan {
                    this.billingAddress = postalAddress {
                        this.region = request.filter.region
                    }
                    this.counterparty = driverRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = flowOf(outbound0).map { value -> routingRoutePlanValidationService.explainRoutePlanDecision(value) }.single()
            browseRoutesResponse {
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleCountRoutes(request: CountRoutesRequest): CountRoutesResponse =
        RpcBoundary.run("acme.routing.v1.RouteQueryService/CountRoutes") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CountRoutes", request)
            log.atInfo().addKeyValue("operation", "CountRoutes").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            countRoutesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleExportRoutes(request: ExportRoutesRequest): ExportRoutesResponse =
        RpcBoundary.run("acme.routing.v1.RouteQueryService/ExportRoutes") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExportRoutes", request)
            log.atInfo().addKeyValue("operation", "ExportRoutes").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = estimateYardMoveRequest {
                this.context = request.context
                this.details = EstimateYardMoveRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.filter.region
                    }
                }
                this.yardMove = yardMove {
                    this.assignee = userRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = withTimeout(5_000) { yardsYardMoveCalculationService.estimateYardMove(outbound0) }
            exportRoutesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.route = route {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.yardMove.audit.updatedAt
                    }
                    this.billingAddress = postalAddress {
                        this.region = request.filter.region
                    }
                    this.counterparty = driverRef {
                        this.displayName = reply0.yardMove.assignee.displayName
                    }
                }
            }
        }

    private suspend fun handleFindRoutes(request: FindRoutesRequest): FindRoutesResponse =
        RpcBoundary.run("acme.routing.v1.RouteQueryService/FindRoutes") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "FindRoutes", request)
            log.atInfo().addKeyValue("operation", "FindRoutes").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val configure0: PreviewRoutePlanRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.routePlan = routePlan {
                    this.billingAddress = postalAddress {
                        this.region = request.filter.region
                    }
                }
            }
            val outbound0 = previewRoutePlanRequest(configure0)
            val reply0 = routingRoutePlanValidationService.previewRoutePlan(outbound0)
            val outbound1 = explainRoutePlanDecisionRequest {
                this.context = request.context
                this.routePlan = routePlan {
                    this.audit = auditInfo {
                        this.etag = reply0.routePlan.audit.etag
                    }
                    this.bank = routePlanBankSettlement {
                        this.bic = when (reply0.routePlan.settlementCase) {
                            RoutePlan.SettlementCase.BANK -> reply0.routePlan.bank.bic
                            else -> ""
                        }
                    }
                    this.billingAddress = postalAddress {
                        this.lines.addAll(reply0.routePlan.billingAddress.linesList)
                        this.recipient = reply0.routePlan.billingAddress.recipient
                    }
                }
            }
            val reply1 = routingRoutePlanValidationService.explainRoutePlanDecision(outbound1)
            findRoutesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.routes.addAll(listOf(route {
                    this.billingAddress = postalAddress {
                        this.lines.addAll(reply0.routePlan.billingAddress.linesList)
                        this.locality = reply1.routePlan.billingAddress.locality
                        this.postalCode = reply1.routePlan.billingAddress.postalCode
                    }
                    this.counterparty = reply0.routePlan.counterparty
                }))
            }
        }

    private suspend fun handleLookupRoute(request: LookupRouteRequest): LookupRouteResponse =
        RpcBoundary.run("acme.routing.v1.RouteQueryService/LookupRoute") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LookupRoute", request)
            log.atInfo().addKeyValue("operation", "LookupRoute").log("Handling transport request")
            val outbound0 = exportNotificationsRequest {
                this.context = request.context
                this.filter = ExportNotificationsRequestKt.filter {
                    this.createdAfter = request.context.deadline
                    this.labels.putAll(request.context.baggageMap)
                    this.ownerId = request.routeId
                }
            }
            notificationsNotificationQueryService.exportNotifications(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = scoreStopRequest {}.routingCopy {
                this.context = request.context
                this.stop = stop {
                    this.organizer = userRef {
                        this.userId = request.context.actor.userId
                    }
                    this.participants.addAll(listOf(stopParticipant {
                        this.contact = contactInfo {
                            this.email = request.context.actor.email
                        }
                    }))
                }
            }
            val reply1 = routingStopValidationService.scoreStop(outbound1)
            lookupRouteResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.route = route {
                    this.audit = auditInfo {
                        this.createdAt = reply1.stop.audit.createdAt
                    }
                    this.billingAddress = postalAddress {
                        this.postalCode = reply1.stop.location.postalCode
                    }
                    this.routeId = request.routeId
                }
            }
        }

    private fun PeekRouteRequest.toSimulateStopFromPeekRoute0(): SimulateStopRequest {
        val source = this
        return simulateStopRequest {
            this.context = source.context
            this.stop = stop {
                this.audit = auditInfo {
                    this.updatedAt = source.context.deadline
                }
                this.organizer = userRef {
                    this.userId = source.context.actor.userId
                }
                this.participants.addAll(listOf(stopParticipant {
                    this.contact = contactInfo {
                        this.email = source.context.actor.email
                    }
                    this.participantId = source.routeId
                }))
            }
        }
    }

    private suspend fun handleSuggestRoutes(request: SuggestRoutesRequest): SuggestRoutesResponse =
        RpcBoundary.run("acme.routing.v1.RouteQueryService/SuggestRoutes") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SuggestRoutes", request)
            log.atInfo().addKeyValue("operation", "SuggestRoutes").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = explainRouteDecisionRequest {
                this.context = request.context
                this.route = route {
                    this.billingAddress = postalAddress {
                        this.region = request.filter.region
                    }
                }
            }
            val reply0 = routingRouteValidationService.explainRouteDecision(outbound0)
            suggestRoutesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.routes.addAll(listOf(route {
                    this.billingAddress = reply0.route.billingAddress
                }))
            }
        }

    private suspend fun handleSummarizeRoutes(request: SummarizeRoutesRequest): SummarizeRoutesResponse =
        RpcBoundary.run("acme.routing.v1.RouteQueryService/SummarizeRoutes") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SummarizeRoutes", request)
            log.atInfo().addKeyValue("operation", "SummarizeRoutes").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.routeId).log("Recorded audit detail")
            val outbound0 = explainTripLogCalculationRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.details = ExplainTripLogCalculationRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                    this.locale = request.context.locale
                }
                this.reason = request.reason
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = telematicsTripLogCalculationService.explainTripLogCalculation(envelope0.unpack(ExplainTripLogCalculationRequest::class.java))
            val outbound1 = validateRoutePlanRequest {
                this.context = request.context
                this.routePlan = routePlan {
                    this.audit = auditInfo {
                        this.etag = reply0.tripLog.audit.etag
                        this.updatedBy = reply0.tripLog.audit.updatedBy
                    }
                    this.billingAddress = postalAddress {
                        this.region = reply0.tripLog.billingAddress.region
                    }
                }
            }
            val reply1 = routingRoutePlanValidationService.validateRoutePlan(WireCopies.json(outbound1))
            summarizeRoutesResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = reply1.outcome.status
                }
            }
        }
}
