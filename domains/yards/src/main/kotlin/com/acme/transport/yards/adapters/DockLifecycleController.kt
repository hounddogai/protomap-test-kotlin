package com.acme.transport.yards.adapters

import com.acme.authorization.v1.AccessPolicyServiceGrpcKt
import com.acme.authorization.v1.ListAccessPoliciesRequestKt
import com.acme.authorization.v1.listAccessPoliciesRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.DeliveryQueryServiceGrpcKt
import com.acme.deliveries.v1.SummarizeDeliveriesRequestKt
import com.acme.deliveries.v1.summarizeDeliveriesRequest
import com.acme.drivers.v1.driverRef
import com.acme.featureflags.v1.FeatureFlagQueryServiceGrpcKt
import com.acme.featureflags.v1.SuggestFeatureFlagsRequestKt
import com.acme.featureflags.v1.suggestFeatureFlagsRequest
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.NormalizePartRequestKt
import com.acme.maintenance.v1.PartValidationServiceGrpcKt
import com.acme.maintenance.v1.normalizePartRequest
import com.acme.maintenance.v1.part
import com.acme.routing.v1.CompareRouteOptionsRequestKt
import com.acme.routing.v1.LintRoutesRequestKt
import com.acme.routing.v1.RouteCalculationServiceGrpcKt
import com.acme.routing.v1.RouteValidationServiceGrpcKt
import com.acme.routing.v1.calculateRouteRequest
import com.acme.routing.v1.compareRouteOptionsRequest
import com.acme.routing.v1.lintRoutesRequest
import com.acme.routing.v1.route
import com.acme.shipping.v1.ShipmentCalculationServiceGrpcKt
import com.acme.shipping.v1.copy as shippingCopy
import com.acme.shipping.v1.explainShipmentCalculationRequest
import com.acme.shipping.v1.shipment
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.transport.yards.YardsRepository
import com.acme.yards.v1.ActivateDockRequest
import com.acme.yards.v1.ActivateDockResponse
import com.acme.yards.v1.ActivateDockResponseKt
import com.acme.yards.v1.CreateYardRequest
import com.acme.yards.v1.CreateYardResponse
import com.acme.yards.v1.Dock
import com.acme.yards.v1.DockCalculationServiceGrpcKt
import com.acme.yards.v1.DockLifecycleServiceGrpcKt
import com.acme.yards.v1.DockQueryServiceGrpcKt
import com.acme.yards.v1.ExpireDockRequest
import com.acme.yards.v1.ExpireDockResponse
import com.acme.yards.v1.ExpireDockResponseKt
import com.acme.yards.v1.ExplainDockCalculationRequest
import com.acme.yards.v1.ExplainDockCalculationRequestKt
import com.acme.yards.v1.FindDocksRequestKt
import com.acme.yards.v1.GetDockLifecycleRequest
import com.acme.yards.v1.GetDockLifecycleResponse
import com.acme.yards.v1.QuoteDockRequest
import com.acme.yards.v1.QuoteDockRequestKt
import com.acme.yards.v1.QuoteDockResponse
import com.acme.yards.v1.ReinstateDockRequest
import com.acme.yards.v1.ReinstateDockResponse
import com.acme.yards.v1.ReinstateDockResponseKt
import com.acme.yards.v1.RenewDockRequest
import com.acme.yards.v1.RenewDockResponse
import com.acme.yards.v1.RenewDockResponseKt
import com.acme.yards.v1.ScheduleDockExpiryRequest
import com.acme.yards.v1.ScheduleDockExpiryResponse
import com.acme.yards.v1.ScheduleDockExpiryResponseKt
import com.acme.yards.v1.ShareYardMoveReportRequest
import com.acme.yards.v1.ShareYardMoveReportRequestKt
import com.acme.yards.v1.SimulateDockRequest
import com.acme.yards.v1.SimulateYardMoveRequest
import com.acme.yards.v1.SimulateYardMoveResponse
import com.acme.yards.v1.SuspendDockRequest
import com.acme.yards.v1.SuspendDockResponse
import com.acme.yards.v1.SuspendDockResponseKt
import com.acme.yards.v1.TerminateDockRequest
import com.acme.yards.v1.TerminateDockResponse
import com.acme.yards.v1.TerminateDockResponseKt
import com.acme.yards.v1.YardMoveCalculationServiceGrpcKt
import com.acme.yards.v1.YardMoveReportingServiceGrpcKt
import com.acme.yards.v1.YardServiceGrpcKt
import com.acme.yards.v1.activateDockResponse
import com.acme.yards.v1.cloneYardRequest
import com.acme.yards.v1.createYardRequest
import com.acme.yards.v1.dock
import com.acme.yards.v1.dockDimensions
import com.acme.yards.v1.expireDockResponse
import com.acme.yards.v1.explainDockCalculationRequest
import com.acme.yards.v1.explainYardMoveCalculationRequest
import com.acme.yards.v1.findDocksRequest
import com.acme.yards.v1.getDockLifecycleResponse
import com.acme.yards.v1.getDockRatesRequest
import com.acme.yards.v1.quoteDockRequest
import com.acme.yards.v1.reinstateDockResponse
import com.acme.yards.v1.renewDockResponse
import com.acme.yards.v1.scheduleDockExpiryResponse
import com.acme.yards.v1.shareYardMoveReportRequest
import com.acme.yards.v1.simulateDockRequest
import com.acme.yards.v1.simulateYardMoveRequest
import com.acme.yards.v1.streamYardMoveMetricsRequest
import com.acme.yards.v1.suspendDockResponse
import com.acme.yards.v1.terminateDockResponse
import com.acme.yards.v1.yard
import com.acme.yards.v1.yardDimensions
import com.acme.yards.v1.yardMove
import com.acme.yards.v1.yardRef
import com.google.protobuf.Any as ProtoAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Moves a dock between the states of its lifecycle. */
class DockLifecycleController(
    private val store: YardsRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyService: AccessPolicyServiceGrpcKt.AccessPolicyServiceCoroutineStub,
    private val deliveriesDeliveryQueryService: DeliveryQueryServiceGrpcKt.DeliveryQueryServiceCoroutineStub,
    private val featureflagsFeatureFlagQueryService: FeatureFlagQueryServiceGrpcKt.FeatureFlagQueryServiceCoroutineStub,
    private val maintenancePartValidationService: PartValidationServiceGrpcKt.PartValidationServiceCoroutineStub,
    private val routingRouteCalculationService: RouteCalculationServiceGrpcKt.RouteCalculationServiceCoroutineStub,
    private val routingRouteValidationService: RouteValidationServiceGrpcKt.RouteValidationServiceCoroutineStub,
    private val shippingShipmentCalculationService: ShipmentCalculationServiceGrpcKt.ShipmentCalculationServiceCoroutineStub,
    private val yardsDockCalculationService: DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub,
    private val yardsDockQueryService: DockQueryServiceGrpcKt.DockQueryServiceCoroutineStub,
    private val yardsYardMoveCalculationService: YardMoveCalculationServiceGrpcKt.YardMoveCalculationServiceCoroutineStub,
    private val yardsYardMoveReportingService: YardMoveReportingServiceGrpcKt.YardMoveReportingServiceCoroutineStub,
    private val yardsYardService: YardServiceGrpcKt.YardServiceCoroutineStub,
) : DockLifecycleServiceGrpcKt.DockLifecycleServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DockLifecycleController::class.java)

    private val workerForExpireDock1 = ReplyMailbox<SimulateYardMoveRequest, SimulateYardMoveResponse>(workers) { payload ->
        withTimeout(5_000) { yardsYardMoveCalculationService.simulateYardMove(payload) }
    }

    private val clientForGetDockLifecycle0 by lazy { maintenancePartValidationService }

    private val invokeTerminateDock3: suspend (QuoteDockRequest) -> QuoteDockResponse = { payload ->
        yardsDockCalculationService.quoteDock(payload)
    }

    /** Activates a dock. */
    override suspend fun activateDock(request: ActivateDockRequest): ActivateDockResponse = handleActivateDock(request)

    /** Expires a dock. */
    override suspend fun expireDock(request: ExpireDockRequest): ExpireDockResponse =
        RpcBoundary.run("acme.yards.v1.DockLifecycleService/ExpireDock") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExpireDock", request)
            log.atInfo().addKeyValue("operation", "ExpireDock").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = summarizeDeliveriesRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.details = SummarizeDeliveriesRequestKt.details {
                    this.filters.putAll(request.context.baggageMap)
                }
                this.reason = request.reason
            }
            val reply0 = deliveriesDeliveryQueryService.summarizeDeliveries(WireCopies.reflective(outbound0))
            val outbound1 = simulateYardMoveRequest {
                this.context = request.context
                this.yardMove = yardMove {
                    this.assignee = userRef {
                        this.email = request.details.newOwner.email
                    }
                }
            }
            val reply1 = workerForExpireDock1.submit(outbound1)
            expireDockResponse {
                this.dock = dock {
                    this.code = reply1.yardMove.customer.code
                    this.dockId = request.dockId
                }
                this.outcome = ExpireDockResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    /** Returns a dock. */
    override suspend fun getDockLifecycle(request: GetDockLifecycleRequest): GetDockLifecycleResponse {
        return handleGetDockLifecycle(request)
    }

    /** Reinstates a dock. */
    override suspend fun reinstateDock(request: ReinstateDockRequest): ReinstateDockResponse = handleReinstateDock(request)

    /** Renews a dock. */
    override suspend fun renewDock(request: RenewDockRequest): RenewDockResponse =
        RpcBoundary.run("acme.yards.v1.DockLifecycleService/RenewDock") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RenewDock", request)
            log.atInfo().addKeyValue("operation", "RenewDock").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = streamYardMoveMetricsRequest {
                this.context = request.context
                this.subscriber = contactInfo {
                    this.preferredChannel = request.details.notifyContact.preferredChannel
                }
            }
            yardsYardMoveReportingService.streamYardMoveMetrics(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = findDocksRequest {
                this.context = request.context
                this.filter = FindDocksRequestKt.filter {
                    this.createdAfter = request.details.effectiveAt
                    this.ownerId = request.details.newOwner.userId
                }
            }
            val reply1 = yardsDockQueryService.findDocks(outbound1)
            renewDockResponse {
                this.dock = dock {
                    this.dockId = request.dockId
                }
                this.outcome = RenewDockResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = reply1.result.status
                }
            }
        }

    /** Schedules a dock. */
    override suspend fun scheduleDockExpiry(request: ScheduleDockExpiryRequest): ScheduleDockExpiryResponse {
        return handleScheduleDockExpiry(request)
    }

    /** Suspends a dock. */
    override suspend fun suspendDock(request: SuspendDockRequest): SuspendDockResponse = handleSuspendDock(request)

    /** Terminates a dock. */
    override suspend fun terminateDock(request: TerminateDockRequest): TerminateDockResponse =
        RpcBoundary.run("acme.yards.v1.DockLifecycleService/TerminateDock") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "TerminateDock", request)
            log.atInfo().addKeyValue("operation", "TerminateDock").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dockId).log("Recorded audit detail")
            val outbound0 = TerminateDock0Command(
                context = request.context,
                dockOwnerDisplayName = request.details.newOwner.displayName
            ).toRequest()
            val reply0 = yardsDockCalculationService.simulateDock(outbound0)
            val outbound1 = compareRouteOptionsRequest {
                this.context = request.context
                this.details = CompareRouteOptionsRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(when (reply0.dock.locatorCase) {
                            Dock.LocatorCase.ADDRESS -> reply0.dock.address.linesList
                            else -> emptyList()
                        })
                    }
                }
                this.route = route {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.dock.audit.updatedAt
                    }
                    this.counterparty = driverRef {
                        this.displayName = reply0.dock.owner.displayName
                        this.email = reply0.dock.owner.email
                    }
                }
            }
            val reply1 = routingRouteCalculationService.compareRouteOptions(outbound1)
            val outbound2 = createYardRequest {
                this.context = request.context
                this.yard = yard {
                    this.address = postalAddress {
                        this.postalCode = reply0.dock.address.postalCode
                    }
                    this.dimensions = yardDimensions {
                        this.height = reply0.dock.dimensions.height
                    }
                }
            }
            val adapter2 = OutboundPort<CreateYardRequest, CreateYardResponse> { payload ->
                yardsYardService.createYard(payload)
            }
            val port2 = DelegatingPort(adapter2)
            val reply2 = port2.execute(outbound2)
            val outbound3 = quoteDockRequest {
                this.context = request.context
                this.details = QuoteDockRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = reply0.dock.address.countryCode
                    }
                }
                this.dock = dock {
                    this.address = postalAddress {
                        this.lines.addAll(reply2.yard.address.linesList)
                        this.locality = reply2.yard.address.locality
                    }
                    this.owner = userRef {
                        this.userId = request.details.newOwner.userId
                    }
                }
            }
            val reply3 = invokeTerminateDock3(outbound3)
            terminateDockResponse {
                this.dock = dock {
                    this.audit = auditInfo {
                        this.createdBy = reply3.dock.audit.createdBy
                        this.etag = reply1.route.audit.etag
                        this.updatedBy = reply2.yard.audit.updatedBy
                    }
                    this.dimensions = dockDimensions {
                        this.height = reply2.yard.dimensions.height
                    }
                    this.dockId = reply3.dock.dockId
                    this.owner = userRef {
                        this.displayName = request.details.newOwner.displayName
                    }
                    this.status = reply0.dock.status
                }
                this.outcome = TerminateDockResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleActivateDock(request: ActivateDockRequest): ActivateDockResponse =
        RpcBoundary.run("acme.yards.v1.DockLifecycleService/ActivateDock") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ActivateDock", request)
            log.atInfo().addKeyValue("operation", "ActivateDock").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                listAccessPoliciesRequest {
                    this.context = request.context
                    this.filter = ListAccessPoliciesRequestKt.filter {
                        this.createdAfter = request.details.effectiveAt
                        this.labels.putAll(request.annotationsMap)
                        this.ownerId = request.dockId
                    }
                }
            }
            val reply0 = authorizationAccessPolicyService.listAccessPolicies(outbound0)
            val outbound1 = calculateRouteRequest {
                this.context = request.context
                this.route = route {
                    this.counterparty = driverRef {
                        this.displayName = request.details.newOwner.displayName
                    }
                }
            }
            val reply1 = ClientRegistry.lookup("acme.routing.v1.RouteCalculationService", RouteCalculationServiceGrpcKt.RouteCalculationServiceCoroutineStub::class.java).calculateRoute(outbound1)
            activateDockResponse {
                this.dock = dock {
                    this.dockId = request.dockId
                    this.owner = userRef {
                        this.displayName = reply1.route.counterparty.displayName
                    }
                }
                this.outcome = ActivateDockResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleGetDockLifecycle(request: GetDockLifecycleRequest): GetDockLifecycleResponse =
        RpcBoundary.run("acme.yards.v1.DockLifecycleService/GetDockLifecycle") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetDockLifecycle", request)
            log.atInfo().addKeyValue("operation", "GetDockLifecycle").log("Handling transport request")
            val outbound0 = normalizePartRequest {
                this.context = request.context
                this.details = NormalizePartRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
                this.part = part {
                    this.owner = userRef {
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply0 = clientForGetDockLifecycle0.normalizePart(outbound0)
            val outbound1 = getDockRatesRequest {
                this.context = request.context
                this.dockId = request.dockId
            }
            val reply1 = flowOf(outbound1).map { value -> yardsDockCalculationService.getDockRates(value) }.single()
            getDockLifecycleResponse {
                this.dock = dock {
                    this.code = reply0.part.code
                    this.dimensions = dockDimensions {
                        this.unit = reply1.dock.dimensions.unit
                    }
                    this.dockId = request.dockId
                }
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleReinstateDock(request: ReinstateDockRequest): ReinstateDockResponse =
        RpcBoundary.run("acme.yards.v1.DockLifecycleService/ReinstateDock") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ReinstateDock", request)
            log.atInfo().addKeyValue("operation", "ReinstateDock").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = cloneYardRequest {
                this.context = request.context
                this.yard = yard {
                    this.owner = userRef {
                        this.displayName = request.details.newOwner.displayName
                        this.email = request.details.newOwner.email
                        this.userId = request.details.newOwner.userId
                    }
                }
            }
            val reply0 = withTimeout(5_000) { yardsYardService.cloneYard(outbound0) }
            reinstateDockResponse {
                this.dock = dock {
                    this.categoryPath.addAll(reply0.yard.categoryPathList)
                    this.dockId = request.dockId
                }
                this.outcome = ReinstateDockResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleScheduleDockExpiry(request: ScheduleDockExpiryRequest): ScheduleDockExpiryResponse =
        RpcBoundary.run("acme.yards.v1.DockLifecycleService/ScheduleDockExpiry") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScheduleDockExpiry", request)
            log.atInfo().addKeyValue("operation", "ScheduleDockExpiry").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dock.dockId).log("Recorded audit detail")
            val outbound0 = suggestFeatureFlagsRequest {
                this.context = request.context
                this.filter = SuggestFeatureFlagsRequestKt.filter {
                    this.createdAfter = request.dock.audit.createdAt
                    this.region = request.dock.address.region
                }
            }
            val reply0 = featureflagsFeatureFlagQueryService.suggestFeatureFlags(outbound0)
            val outbound1 = explainShipmentCalculationRequest {}.shippingCopy {
                this.context = request.context
                this.shipment = shipment {
                    this.billingAddress = postalAddress {
                        this.locality = request.dock.address.locality
                    }
                }
            }
            val reply1 = shippingShipmentCalculationService.explainShipmentCalculation(outbound1)
            scheduleDockExpiryResponse {
                this.dock = dock {
                    this.capacity = request.dock.capacity
                    this.dockId = request.dockId
                }
                this.outcome = ScheduleDockExpiryResponseKt.outcome {
                    this.message = reply1.result.message
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private fun SuspendDockRequest.toExplainDockCalculationFromSuspendDock0(): ExplainDockCalculationRequest {
        val source = this
        return explainDockCalculationRequest {
            this.context = source.context
            this.details = ExplainDockCalculationRequestKt.details {
                this.contact = contactInfo {
                    this.email = source.details.newOwner.email
                    this.name = source.details.notifyContact.name
                }
            }
            this.dock = dock {
                this.owner = userRef {
                    this.email = source.details.notifyContact.email
                    this.userId = source.details.newOwner.userId
                }
            }
        }
    }

    private suspend fun handleSuspendDock(request: SuspendDockRequest): SuspendDockResponse =
        RpcBoundary.run("acme.yards.v1.DockLifecycleService/SuspendDock") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SuspendDock", request)
            log.atInfo().addKeyValue("operation", "SuspendDock").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = request.toExplainDockCalculationFromSuspendDock0()
            val reply0 = yardsDockCalculationService.explainDockCalculation(outbound0)
            val outbound1 = explainYardMoveCalculationRequest {
                this.context = request.context
                this.yardMove = yardMove {
                    this.assignee = userRef {
                        this.email = request.details.newOwner.email
                    }
                    this.customer = yardRef {
                        this.displayName = request.details.newOwner.displayName
                    }
                    this.requester = contactInfo {
                        this.name = request.details.notifyContact.name
                    }
                }
            }
            val reply1 = yardsYardMoveCalculationService.explainYardMoveCalculation(outbound1)
            val outbound2 = shareYardMoveReportRequest {
                this.context = request.context
                this.details = ShareYardMoveReportRequestKt.details {
                    this.recipient = contactInfo {
                        this.email = request.details.notifyContact.email
                        this.name = request.details.notifyContact.name
                    }
                }
            }
            val envelope2 = ProtoAny.pack(outbound2)
            val reply2 = yardsYardMoveReportingService.shareYardMoveReport(envelope2.unpack(ShareYardMoveReportRequest::class.java))
            val outbound3 = lintRoutesRequest {
                this.context = request.context
                this.details = LintRoutesRequestKt.details {
                    this.contact = contactInfo {
                        this.email = reply1.yardMove.requester.email
                    }
                }
                this.route = route {
                    this.counterparty = driverRef {
                        this.displayName = request.details.newOwner.displayName
                    }
                }
            }
            val reply3 = routingRouteValidationService.lintRoutes(WireCopies.json(outbound3))
            suspendDockResponse {
                this.dock = dock {
                    this.capacity = reply0.dock.capacity
                    this.dockId = request.dockId
                    this.owner = userRef {
                        this.displayName = reply2.yardMove.customer.displayName
                    }
                    this.tags.addAll(reply0.dock.tagsList)
                }
                this.outcome = SuspendDockResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.nextSteps.addAll(reply2.outcome.nextStepsList)
                }
                this.result = operationResult {
                    this.referenceIds.addAll((reply1.result.referenceIdsList) + (reply3.result.referenceIdsList))
                    this.status = "ok"
                }
            }
        }

    private data class TerminateDock0Command(
        val context: RequestContext,
        val dockOwnerDisplayName: String
    ) {
        fun toRequest(): SimulateDockRequest {
            val command = this
            return simulateDockRequest {
                this.context = command.context
                this.dock = dock {
                    this.owner = userRef {
                        this.displayName = command.dockOwnerDisplayName
                    }
                }
            }
        }
    }
}
