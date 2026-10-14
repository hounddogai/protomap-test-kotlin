package com.acme.transport.routing.api

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.drivers.v1.DriverCalculationServiceGrpcKt
import com.acme.drivers.v1.SimulateDriverRequest
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.simulateDriverRequest
import com.acme.fuel.v1.FuelCardWorkflowServiceGrpcKt
import com.acme.fuel.v1.SignalFuelCardWorkflowRequestKt
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.signalFuelCardWorkflowRequest
import com.acme.identity.v1.userRef
import com.acme.inventory.v1.ListStockItemsRequest
import com.acme.inventory.v1.ListStockItemsRequestKt
import com.acme.inventory.v1.ListStockItemsResponse
import com.acme.inventory.v1.StockItemServiceGrpcKt
import com.acme.inventory.v1.listStockItemsRequest
import com.acme.ledger.v1.LedgerAccountServiceGrpcKt
import com.acme.ledger.v1.watchLedgerAccountsRequest
import com.acme.routing.v1.ActivateRoutePlanRequest
import com.acme.routing.v1.ActivateRoutePlanResponse
import com.acme.routing.v1.ActivateRoutePlanResponseKt
import com.acme.routing.v1.ExpireRoutePlanRequest
import com.acme.routing.v1.ExpireRoutePlanResponse
import com.acme.routing.v1.ExpireRoutePlanResponseKt
import com.acme.routing.v1.ExplainStopCalculationRequest
import com.acme.routing.v1.ExplainStopCalculationResponse
import com.acme.routing.v1.FindStopsRequest
import com.acme.routing.v1.FindStopsRequestKt
import com.acme.routing.v1.GetRoutePlanLifecycleRequest
import com.acme.routing.v1.GetRoutePlanLifecycleResponse
import com.acme.routing.v1.RecalculateRoutePlansRequest
import com.acme.routing.v1.ReinstateRoutePlanRequest
import com.acme.routing.v1.ReinstateRoutePlanResponse
import com.acme.routing.v1.ReinstateRoutePlanResponseKt
import com.acme.routing.v1.RoutePlanCalculationServiceGrpcKt
import com.acme.routing.v1.RoutePlanLifecycleServiceGrpcKt
import com.acme.routing.v1.RoutePlanValidationServiceGrpcKt
import com.acme.routing.v1.RouteQueryServiceGrpcKt
import com.acme.routing.v1.ScheduleRoutePlanExpiryRequest
import com.acme.routing.v1.ScheduleRoutePlanExpiryResponse
import com.acme.routing.v1.StopCalculationServiceGrpcKt
import com.acme.routing.v1.StopQueryServiceGrpcKt
import com.acme.routing.v1.SummarizeRoutesRequest
import com.acme.routing.v1.SummarizeRoutesRequestKt
import com.acme.routing.v1.SummarizeRoutesResponse
import com.acme.routing.v1.SuspendRoutePlanRequest
import com.acme.routing.v1.SuspendRoutePlanResponse
import com.acme.routing.v1.SuspendRoutePlanResponseKt
import com.acme.routing.v1.TerminateRoutePlanRequest
import com.acme.routing.v1.TerminateRoutePlanResponse
import com.acme.routing.v1.TerminateRoutePlanResponseKt
import com.acme.routing.v1.activateRoutePlanResponse
import com.acme.routing.v1.compareRoutePlanOptionsRequest
import com.acme.routing.v1.expireRoutePlanResponse
import com.acme.routing.v1.explainRoutePlanDecisionRequest
import com.acme.routing.v1.explainStopCalculationRequest
import com.acme.routing.v1.findStopsRequest
import com.acme.routing.v1.getRoutePlanLifecycleResponse
import com.acme.routing.v1.recalculateRoutePlansRequest
import com.acme.routing.v1.reinstateRoutePlanResponse
import com.acme.routing.v1.routePlan
import com.acme.routing.v1.routePlanCardSettlement
import com.acme.routing.v1.routePlanLine
import com.acme.routing.v1.scheduleRoutePlanExpiryResponse
import com.acme.routing.v1.stop
import com.acme.routing.v1.stopParticipant
import com.acme.routing.v1.summarizeRoutesRequest
import com.acme.routing.v1.suspendRoutePlanResponse
import com.acme.routing.v1.terminateRoutePlanResponse
import com.acme.search.v1.ExportSearchIndexesRequestKt
import com.acme.search.v1.SearchIndexQueryServiceGrpcKt
import com.acme.search.v1.exportSearchIndexesRequest
import com.acme.telematics.v1.GeoFenceWorkflowServiceGrpcKt
import com.acme.telematics.v1.RecallTelemetryReadingRequest
import com.acme.telematics.v1.RecallTelemetryReadingRequestKt
import com.acme.telematics.v1.RecallTelemetryReadingResponse
import com.acme.telematics.v1.TelemetryReadingApprovalServiceGrpcKt
import com.acme.telematics.v1.advanceGeoFenceWorkflowRequest
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.recallTelemetryReadingRequest
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.transport.routing.RoutingRepository
import com.google.protobuf.Timestamp
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Moves a route plan between the states of its lifecycle. */
class RoutePlanLifecycleGrpcService(
    private val store: RoutingRepository,
    private val workers: CoroutineScope,
    private val driversDriverCalculationService: DriverCalculationServiceGrpcKt.DriverCalculationServiceCoroutineStub,
    private val fuelFuelCardWorkflowService: FuelCardWorkflowServiceGrpcKt.FuelCardWorkflowServiceCoroutineStub,
    private val inventoryStockItemService: StockItemServiceGrpcKt.StockItemServiceCoroutineStub,
    private val ledgerLedgerAccountService: LedgerAccountServiceGrpcKt.LedgerAccountServiceCoroutineStub,
    private val routingRoutePlanCalculationService: RoutePlanCalculationServiceGrpcKt.RoutePlanCalculationServiceCoroutineStub,
    private val routingRoutePlanValidationService: RoutePlanValidationServiceGrpcKt.RoutePlanValidationServiceCoroutineStub,
    private val routingRouteQueryService: RouteQueryServiceGrpcKt.RouteQueryServiceCoroutineStub,
    private val routingStopCalculationService: StopCalculationServiceGrpcKt.StopCalculationServiceCoroutineStub,
    private val routingStopQueryService: StopQueryServiceGrpcKt.StopQueryServiceCoroutineStub,
    private val searchSearchIndexQueryService: SearchIndexQueryServiceGrpcKt.SearchIndexQueryServiceCoroutineStub,
    private val telematicsGeoFenceWorkflowService: GeoFenceWorkflowServiceGrpcKt.GeoFenceWorkflowServiceCoroutineStub,
    private val telematicsTelemetryReadingApprovalService: TelemetryReadingApprovalServiceGrpcKt.TelemetryReadingApprovalServiceCoroutineStub,
) : RoutePlanLifecycleServiceGrpcKt.RoutePlanLifecycleServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(RoutePlanLifecycleGrpcService::class.java)

    private val invokeScheduleRoutePlanExpiry0: suspend (ExplainStopCalculationRequest) -> ExplainStopCalculationResponse = { payload ->
        routingStopCalculationService.explainStopCalculation(payload)
    }

    private val workerForTerminateRoutePlan0 = ReplyMailbox<SummarizeRoutesRequest, SummarizeRoutesResponse>(workers) { payload ->
        withTimeout(5_000) { routingRouteQueryService.summarizeRoutes(payload) }
    }

    private val clientForTerminateRoutePlan1 by lazy { routingRoutePlanCalculationService }

    /** Activates a route plan. */
    override suspend fun activateRoutePlan(request: ActivateRoutePlanRequest): ActivateRoutePlanResponse {
        return handleActivateRoutePlan(request)
    }

    /** Expires a route plan. */
    override suspend fun expireRoutePlan(request: ExpireRoutePlanRequest): ExpireRoutePlanResponse = handleExpireRoutePlan(request)

    /** Returns a route plan. */
    override suspend fun getRoutePlanLifecycle(request: GetRoutePlanLifecycleRequest): GetRoutePlanLifecycleResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanLifecycleService/GetRoutePlanLifecycle") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetRoutePlanLifecycle", request)
            log.atInfo().addKeyValue("operation", "GetRoutePlanLifecycle").log("Handling transport request")
            val outbound0 = GetRoutePlanLifecycle0Command(
                context = request.context,
                filterOwnerId = request.routePlanId,
                filterCreatedAfter = request.context.deadline
            ).toRequest()
            val reply0 = routingStopQueryService.findStops(outbound0)
            getRoutePlanLifecycleResponse {
                this.result = operationResult {
                    this.status = reply0.result.status
                }
                this.routePlan = routePlan {
                    this.routePlanId = request.routePlanId
                }
            }
        }

    /** Reinstates a route plan. */
    override suspend fun reinstateRoutePlan(request: ReinstateRoutePlanRequest): ReinstateRoutePlanResponse {
        return handleReinstateRoutePlan(request)
    }

    /** Schedules a route plan. */
    override suspend fun scheduleRoutePlanExpiry(request: ScheduleRoutePlanExpiryRequest): ScheduleRoutePlanExpiryResponse = handleScheduleRoutePlanExpiry(request)

    /** Suspends a route plan. */
    override suspend fun suspendRoutePlan(request: SuspendRoutePlanRequest): SuspendRoutePlanResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanLifecycleService/SuspendRoutePlan") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SuspendRoutePlan", request)
            log.atInfo().addKeyValue("operation", "SuspendRoutePlan").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = RecalculateRoutePlansRequest.newBuilder().apply {
                setContext(request.context)
                setRoutePlan(routePlan {
                    this.counterparty = driverRef {
                        this.displayName = request.details.newOwner.displayName
                        this.email = request.details.newOwner.email
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> routingRoutePlanCalculationService.recalculateRoutePlans(value) }
            val outbound1 = RequestFactory.create {
                explainRoutePlanDecisionRequest {
                    this.context = request.context
                    this.routePlan = routePlan {
                        this.billingAddress = postalAddress {
                            this.countryCode = reply0.routePlan.billingAddress.countryCode
                        }
                        this.card = routePlanCardSettlement {
                            this.network = reply0.routePlan.card.network
                        }
                    }
                }
            }
            val reply1 = routingRoutePlanValidationService.explainRoutePlanDecision(outbound1)
            val outbound2 = advanceGeoFenceWorkflowRequest {
                this.context = request.context
                this.geoFence = geoFence {
                    this.owner = userRef {
                        this.userId = request.details.newOwner.userId
                    }
                }
            }
            val reply2 = ClientRegistry.lookup("acme.telematics.v1.GeoFenceWorkflowService", GeoFenceWorkflowServiceGrpcKt.GeoFenceWorkflowServiceCoroutineStub::class.java).advanceGeoFenceWorkflow(outbound2)
            val outbound3 = signalFuelCardWorkflowRequest {
                this.context = request.context
                this.details = SignalFuelCardWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.email = request.details.notifyContact.email
                    }
                }
                this.fuelCard = fuelCard {
                    this.contact = contactInfo {
                        this.email = reply2.geoFence.owner.email
                    }
                }
            }
            val reply3 = fuelFuelCardWorkflowService.signalFuelCardWorkflow(WireCopies.reflective(outbound3))
            suspendRoutePlanResponse {
                this.outcome = SuspendRoutePlanResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.status = reply1.outcome.status
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
                this.routePlan = routePlan {
                    this.billingAddress = postalAddress {
                        this.countryCode = reply1.routePlan.billingAddress.countryCode
                    }
                    this.counterparty = driverRef {
                        this.email = reply2.geoFence.owner.email
                    }
                    this.lines.addAll(listOf(routePlanLine {
                        this.amount = reply2.geoFence.discount.amount
                    }))
                    this.routePlanId = request.routePlanId
                }
            }
        }

    /** Terminates a route plan. */
    override suspend fun terminateRoutePlan(request: TerminateRoutePlanRequest): TerminateRoutePlanResponse {
        return handleTerminateRoutePlan(request)
    }

    private suspend fun handleActivateRoutePlan(request: ActivateRoutePlanRequest): ActivateRoutePlanResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanLifecycleService/ActivateRoutePlan") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ActivateRoutePlan", request)
            log.atInfo().addKeyValue("operation", "ActivateRoutePlan").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = watchLedgerAccountsRequest {
                this.context = request.context
                this.subscriber = contactInfo {
                    this.email = request.details.newOwner.email
                    this.name = request.details.notifyContact.name
                    this.phone = request.details.notifyContact.phone
                    this.preferredChannel = request.details.notifyContact.preferredChannel
                }
            }
            ledgerLedgerAccountService.watchLedgerAccounts(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            activateRoutePlanResponse {
                this.outcome = ActivateRoutePlanResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.routePlan = routePlan {
                    this.routePlanId = request.routePlanId
                }
            }
        }

    private suspend fun handleExpireRoutePlan(request: ExpireRoutePlanRequest): ExpireRoutePlanResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanLifecycleService/ExpireRoutePlan") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExpireRoutePlan", request)
            log.atInfo().addKeyValue("operation", "ExpireRoutePlan").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = exportSearchIndexesRequest {
                this.context = request.context
                this.filter = ExportSearchIndexesRequestKt.filter {
                    this.createdAfter = request.details.effectiveAt
                    this.ownerId = request.details.newOwner.userId
                }
            }
            searchSearchIndexQueryService.exportSearchIndexes(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            expireRoutePlanResponse {
                this.outcome = ExpireRoutePlanResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.routePlan = routePlan {
                    this.routePlanId = request.routePlanId
                }
            }
        }

    private data class GetRoutePlanLifecycle0Command(
        val context: RequestContext,
        val filterOwnerId: String,
        val filterCreatedAfter: Timestamp
    ) {
        fun toRequest(): FindStopsRequest {
            val command = this
            return findStopsRequest {
                this.context = command.context
                this.filter = FindStopsRequestKt.filter {
                    this.createdAfter = command.filterCreatedAfter
                    this.ownerId = command.filterOwnerId
                }
            }
        }
    }

    private suspend fun handleReinstateRoutePlan(request: ReinstateRoutePlanRequest): ReinstateRoutePlanResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanLifecycleService/ReinstateRoutePlan") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ReinstateRoutePlan", request)
            log.atInfo().addKeyValue("operation", "ReinstateRoutePlan").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = simulateDriverRequest {
                this.context = request.context
                this.driver = driver {
                    this.contact = contactInfo {
                        this.email = request.details.notifyContact.email
                        this.preferredChannel = request.details.notifyContact.preferredChannel
                    }
                    this.name = request.details.notifyContact.name
                    this.relationshipManager = userRef {
                        this.email = request.details.newOwner.email
                    }
                }
            }
            val command0: Submission<SimulateDriverRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = driversDriverCalculationService.simulateDriver(payload0)
            val outbound1 = recallTelemetryReadingRequest {
                this.context = request.context
                this.details = RecallTelemetryReadingRequestKt.details {
                    this.approver = userRef {
                        this.displayName = reply0.driver.relationshipManager.displayName
                        this.email = request.details.newOwner.email
                        this.userId = request.details.newOwner.userId
                    }
                }
            }
            val adapter1 = OutboundPort<RecallTelemetryReadingRequest, RecallTelemetryReadingResponse> { payload ->
                telematicsTelemetryReadingApprovalService.recallTelemetryReading(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            reinstateRoutePlanResponse {
                this.outcome = ReinstateRoutePlanResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.correlationId = reply1.telemetryReading.correlationId
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
                this.routePlan = routePlan {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.driver.audit.updatedAt
                    }
                    this.billingAddress = postalAddress {
                        this.region = reply0.driver.primaryAddress.region
                    }
                    this.routePlanId = request.routePlanId
                }
            }
        }

    private suspend fun handleScheduleRoutePlanExpiry(request: ScheduleRoutePlanExpiryRequest): ScheduleRoutePlanExpiryResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanLifecycleService/ScheduleRoutePlanExpiry") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScheduleRoutePlanExpiry", request)
            log.atInfo().addKeyValue("operation", "ScheduleRoutePlanExpiry").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.routePlan.routePlanId).log("Recorded audit detail")
            val outbound0 = explainStopCalculationRequest {
                this.context = request.context
                this.stop = stop {
                    this.location = postalAddress {
                        this.lines.addAll(request.routePlan.billingAddress.linesList)
                    }
                    this.participants.addAll(listOf(stopParticipant {
                        this.contact = contactInfo {
                            this.email = request.routePlan.counterparty.email
                        }
                    }))
                }
            }
            val reply0 = invokeScheduleRoutePlanExpiry0(outbound0)
            val outbound1 = listStockItemsRequest {
                this.context = request.context
                this.filter = ListStockItemsRequestKt.filter {
                    this.createdAfter = request.routePlan.audit.updatedAt
                    this.labels.putAll(reply0.result.detailsMap)
                    this.ownerId = request.routePlan.counterparty.driverId
                    this.region = request.routePlan.billingAddress.region
                }
            }
            val invoke1: suspend (ListStockItemsRequest, Metadata) -> ListStockItemsResponse = inventoryStockItemService::listStockItems
            val reply1 = invoke1(outbound1, Metadata())
            scheduleRoutePlanExpiryResponse {
                this.result = operationResult {
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = reply1.result.status
                }
                this.routePlan = routePlan {
                    this.billingAddress = postalAddress {
                        this.recipient = reply0.stop.location.recipient
                    }
                    this.notes = request.routePlan.notes
                    this.routePlanId = request.routePlanId
                }
            }
        }

    private suspend fun handleTerminateRoutePlan(request: TerminateRoutePlanRequest): TerminateRoutePlanResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanLifecycleService/TerminateRoutePlan") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "TerminateRoutePlan", request)
            log.atInfo().addKeyValue("operation", "TerminateRoutePlan").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = summarizeRoutesRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.details = SummarizeRoutesRequestKt.details {
                    this.filters.putAll(request.context.baggageMap)
                }
                this.reason = request.reason
            }
            val reply0 = workerForTerminateRoutePlan0.submit(outbound0)
            val outbound1 = compareRoutePlanOptionsRequest {
                this.context = request.context
                this.routePlan = routePlan {
                    this.counterparty = driverRef {
                        this.displayName = request.details.newOwner.displayName
                        this.email = request.details.newOwner.email
                    }
                }
            }
            val reply1 = clientForTerminateRoutePlan1.compareRoutePlanOptions(outbound1)
            terminateRoutePlanResponse {
                this.outcome = TerminateRoutePlanResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = "ok"
                }
                this.routePlan = routePlan {
                    this.routePlanId = request.routePlanId
                }
            }
        }
}
