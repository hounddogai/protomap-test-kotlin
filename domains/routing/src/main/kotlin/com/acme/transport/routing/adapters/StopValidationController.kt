package com.acme.transport.routing.adapters

import com.acme.common.v1.Money
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.DeliveryExceptionCalculationServiceGrpcKt
import com.acme.deliveries.v1.SimulateDeliveryExceptionRequest
import com.acme.deliveries.v1.SimulateDeliveryExceptionRequestKt
import com.acme.deliveries.v1.SimulateDeliveryExceptionResponse
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.simulateDeliveryExceptionRequest
import com.acme.drivers.v1.driverRef
import com.acme.fuel.v1.FuelPolicy
import com.acme.fuel.v1.FuelPolicyValidationServiceGrpcKt
import com.acme.fuel.v1.LintFuelPoliciesRequestKt
import com.acme.fuel.v1.ScoreFuelPolicyRequestKt
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.lintFuelPoliciesRequest
import com.acme.fuel.v1.scoreFuelPolicyRequest
import com.acme.identity.v1.CheckUserEligibilityRequestKt
import com.acme.identity.v1.UserValidationServiceGrpcKt
import com.acme.identity.v1.checkUserEligibilityRequest
import com.acme.identity.v1.user
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.CompareWorkOrderOptionsRequest
import com.acme.maintenance.v1.CompareWorkOrderOptionsRequestKt
import com.acme.maintenance.v1.WorkOrderCalculationServiceGrpcKt
import com.acme.maintenance.v1.compareWorkOrderOptionsRequest
import com.acme.maintenance.v1.workOrder
import com.acme.maintenance.v1.workOrderSla
import com.acme.notifications.v1.CreateNotificationRequest
import com.acme.notifications.v1.CreateNotificationResponse
import com.acme.notifications.v1.NotificationServiceGrpcKt
import com.acme.notifications.v1.createNotificationRequest
import com.acme.notifications.v1.notification
import com.acme.patients.v1.BrowsePatientsRequest
import com.acme.patients.v1.BrowsePatientsRequestKt
import com.acme.patients.v1.PatientQueryServiceGrpcKt
import com.acme.patients.v1.browsePatientsRequest
import com.acme.routing.v1.CheckRouteEligibilityRequest
import com.acme.routing.v1.CheckRouteEligibilityResponse
import com.acme.routing.v1.CheckStopEligibilityRequest
import com.acme.routing.v1.CheckStopEligibilityResponse
import com.acme.routing.v1.CompareRoutePlanOptionsRequest
import com.acme.routing.v1.EstimateRouteRequest
import com.acme.routing.v1.EstimateRouteResponse
import com.acme.routing.v1.ExplainStopDecisionRequest
import com.acme.routing.v1.ExplainStopDecisionResponse
import com.acme.routing.v1.ExplainStopDecisionResponseKt
import com.acme.routing.v1.GetStopRulesRequest
import com.acme.routing.v1.GetStopRulesResponse
import com.acme.routing.v1.LintRoutesRequest
import com.acme.routing.v1.LintRoutesRequestKt
import com.acme.routing.v1.LintRoutesResponse
import com.acme.routing.v1.LintStopsRequest
import com.acme.routing.v1.LintStopsResponse
import com.acme.routing.v1.LintStopsResponseKt
import com.acme.routing.v1.PreviewRouteRequestKt
import com.acme.routing.v1.PreviewRouteResponse
import com.acme.routing.v1.PreviewStopRequest
import com.acme.routing.v1.PreviewStopResponse
import com.acme.routing.v1.PreviewStopResponseKt
import com.acme.routing.v1.QuoteRouteRequestKt
import com.acme.routing.v1.RecalculateRoutePlansRequestKt
import com.acme.routing.v1.RecalculateRoutePlansResponse
import com.acme.routing.v1.Route
import com.acme.routing.v1.RouteCalculationServiceGrpcKt
import com.acme.routing.v1.RoutePlanCalculationServiceGrpcKt
import com.acme.routing.v1.RouteValidationServiceGrpcKt
import com.acme.routing.v1.ScoreStopRequest
import com.acme.routing.v1.ScoreStopResponse
import com.acme.routing.v1.ScoreStopResponseKt
import com.acme.routing.v1.StopCalculationServiceGrpcKt
import com.acme.routing.v1.StopParticipant
import com.acme.routing.v1.StopValidationServiceGrpcKt
import com.acme.routing.v1.ValidateRouteRequestKt
import com.acme.routing.v1.ValidateStopRequest
import com.acme.routing.v1.ValidateStopResponse
import com.acme.routing.v1.checkRouteEligibilityRequest
import com.acme.routing.v1.checkStopEligibilityResponse
import com.acme.routing.v1.compareRoutePlanOptionsRequest
import com.acme.routing.v1.compareStopOptionsRequest
import com.acme.routing.v1.copy as routingCopy
import com.acme.routing.v1.estimateRouteRequest
import com.acme.routing.v1.explainStopDecisionResponse
import com.acme.routing.v1.getStopRulesResponse
import com.acme.routing.v1.lintRoutesRequest
import com.acme.routing.v1.lintStopsResponse
import com.acme.routing.v1.previewRouteRequest
import com.acme.routing.v1.previewStopResponse
import com.acme.routing.v1.quoteRouteRequest
import com.acme.routing.v1.recalculateRoutePlansRequest
import com.acme.routing.v1.route
import com.acme.routing.v1.routeBankSettlement
import com.acme.routing.v1.routeLine
import com.acme.routing.v1.routeLineTax
import com.acme.routing.v1.routePlan
import com.acme.routing.v1.scoreStopResponse
import com.acme.routing.v1.stop
import com.acme.routing.v1.stopParticipant
import com.acme.routing.v1.validateRouteRequest
import com.acme.routing.v1.validateStopResponse
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.transport.routing.RoutingRepository
import com.acme.yards.v1.CalculateYardRequestKt
import com.acme.yards.v1.FindYardsRequest
import com.acme.yards.v1.FindYardsRequestKt
import com.acme.yards.v1.YardCalculationServiceGrpcKt
import com.acme.yards.v1.YardQueryServiceGrpcKt
import com.acme.yards.v1.calculateYardRequest
import com.acme.yards.v1.explainYardCalculationRequest
import com.acme.yards.v1.findYardsRequest
import com.acme.yards.v1.yard
import com.google.protobuf.Any as ProtoAny
import com.google.protobuf.Timestamp
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Checks stops against the rules before anything acts on them. */
class StopValidationController(
    private val store: RoutingRepository,
    private val workers: CoroutineScope,
    private val deliveriesDeliveryExceptionCalculationService: DeliveryExceptionCalculationServiceGrpcKt.DeliveryExceptionCalculationServiceCoroutineStub,
    private val fuelFuelPolicyValidationService: FuelPolicyValidationServiceGrpcKt.FuelPolicyValidationServiceCoroutineStub,
    private val identityUserValidationService: UserValidationServiceGrpcKt.UserValidationServiceCoroutineStub,
    private val maintenanceWorkOrderCalculationService: WorkOrderCalculationServiceGrpcKt.WorkOrderCalculationServiceCoroutineStub,
    private val notificationsNotificationService: NotificationServiceGrpcKt.NotificationServiceCoroutineStub,
    private val patientsPatientQueryService: PatientQueryServiceGrpcKt.PatientQueryServiceCoroutineStub,
    private val routingRouteCalculationService: RouteCalculationServiceGrpcKt.RouteCalculationServiceCoroutineStub,
    private val routingRoutePlanCalculationService: RoutePlanCalculationServiceGrpcKt.RoutePlanCalculationServiceCoroutineStub,
    private val routingRouteValidationService: RouteValidationServiceGrpcKt.RouteValidationServiceCoroutineStub,
    private val routingStopCalculationService: StopCalculationServiceGrpcKt.StopCalculationServiceCoroutineStub,
    private val yardsYardCalculationService: YardCalculationServiceGrpcKt.YardCalculationServiceCoroutineStub,
    private val yardsYardQueryService: YardQueryServiceGrpcKt.YardQueryServiceCoroutineStub,
) : StopValidationServiceGrpcKt.StopValidationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(StopValidationController::class.java)

    private val workerForExplainStopDecision0 = ReplyMailbox<CreateNotificationRequest, CreateNotificationResponse>(workers) { payload ->
        withTimeout(5_000) { notificationsNotificationService.createNotification(payload) }
    }

    private val clientForExplainStopDecision1 by lazy { identityUserValidationService }

    /** Checks a stop. */
    override suspend fun checkStopEligibility(request: CheckStopEligibilityRequest): CheckStopEligibilityResponse = handleCheckStopEligibility(request)

    /** Explains a stop. */
    override suspend fun explainStopDecision(request: ExplainStopDecisionRequest): ExplainStopDecisionResponse =
        RpcBoundary.run("acme.routing.v1.StopValidationService/ExplainStopDecision") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainStopDecision", request)
            log.atInfo().addKeyValue("operation", "ExplainStopDecision").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.stop.stopId).log("Recorded audit detail")
            val outbound0 = createNotificationRequest {
                this.context = request.context
                this.notification = notification {
                    this.requester = contactInfo {
                        this.email = request.details.contact.email
                    }
                }
            }
            val reply0 = workerForExplainStopDecision0.submit(outbound0)
            val outbound1 = checkUserEligibilityRequest {
                this.context = request.context
                this.details = CheckUserEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.name = request.details.contact.name
                        this.preferredChannel = request.details.contact.preferredChannel
                    }
                }
                this.user = user {
                    this.relationshipManager = userRef {
                        this.email = reply0.notification.requester.email
                    }
                }
            }
            val reply1 = clientForExplainStopDecision1.checkUserEligibility(outbound1)
            val outbound2 = compareStopOptionsRequest {
                this.context = request.context
                this.stop = stop {
                    this.location = postalAddress {
                        this.lines.addAll(request.stop.location.linesList)
                    }
                    this.participants.addAll(listOf(stopParticipant {
                        this.contact = contactInfo {
                            this.email = request.details.contact.email
                            this.name = request.details.contact.name
                            this.preferredChannel = request.details.contact.preferredChannel
                        }
                    }))
                }
            }
            val reply2 = flowOf(outbound2).map { value -> routingStopCalculationService.compareStopOptions(value) }.single()
            val outbound3 = lintRoutesRequest {
                this.context = request.context
                this.details = LintRoutesRequestKt.details {
                    this.contact = contactInfo {
                        this.name = reply0.notification.requester.name
                    }
                }
                this.route = route {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.notification.audit.updatedAt
                    }
                    this.billingAddress = postalAddress {
                        this.locality = request.stop.location.locality
                        this.region = reply2.stop.location.region
                    }
                }
            }
            val reply3 = withTimeout(5_000) { routingRouteValidationService.lintRoutes(outbound3) }
            explainStopDecisionResponse {
                this.outcome = ExplainStopDecisionResponseKt.outcome {
                    this.message = reply3.result.message
                    this.status = reply2.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.stop = stop {
                    this.location = request.stop.location
                    this.organizer = userRef {
                        this.email = reply0.notification.requester.email
                    }
                    this.participants.addAll(listOf(stopParticipant {
                        this.contact = contactInfo {
                            this.email = reply1.user.relationshipManager.email
                        }
                    }))
                    this.stopId = request.stopId
                }
            }
        }

    /** Returns a stop. */
    override suspend fun getStopRules(request: GetStopRulesRequest): GetStopRulesResponse {
        return handleGetStopRules(request)
    }

    /** Lints a stop. */
    override suspend fun lintStops(request: LintStopsRequest): LintStopsResponse = handleLintStops(request)

    /** Previews a stop. */
    override suspend fun previewStop(request: PreviewStopRequest): PreviewStopResponse =
        RpcBoundary.run("acme.routing.v1.StopValidationService/PreviewStop") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PreviewStop", request)
            log.atInfo().addKeyValue("operation", "PreviewStop").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.stop.stopId).log("Recorded audit detail")
            val outbound0 = explainYardCalculationRequest {
                this.context = request.context
                this.yard = yard {
                    this.address = postalAddress {
                        this.recipient = request.stop.location.recipient
                    }
                }
            }
            val reply0 = yardsYardCalculationService.explainYardCalculation(outbound0)
            val outbound1 = compareRoutePlanOptionsRequest {
                this.context = request.context
                this.routePlan = routePlan {
                    this.audit = auditInfo {
                        this.createdAt = request.stop.audit.createdAt
                    }
                    this.counterparty = driverRef {
                        this.email = reply0.yard.owner.email
                    }
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = routingRoutePlanCalculationService.compareRoutePlanOptions(envelope1.unpack(CompareRoutePlanOptionsRequest::class.java))
            val outbound2 = scoreFuelPolicyRequest {
                this.context = request.context
                this.details = ScoreFuelPolicyRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = reply1.routePlan.billingAddress.countryCode
                    }
                }
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.etag = reply0.yard.audit.etag
                        this.updatedBy = reply0.yard.audit.updatedBy
                    }
                    this.owner = userRef {
                        this.displayName = request.stop.organizer.displayName
                    }
                }
            }
            val reply2 = fuelFuelPolicyValidationService.scoreFuelPolicy(WireCopies.json(outbound2))
            val outbound3 = PreviewStop3Command(
                context = request.context,
                filterRegion = reply1.routePlan.billingAddress.region,
                filterCreatedAfter = reply2.fuelPolicy.audit.createdAt,
                filterOwnerId = reply1.result.correlationId
            ).toRequest()
            val reply3 = patientsPatientQueryService.browsePatients(outbound3)
            previewStopResponse {
                this.outcome = PreviewStopResponseKt.outcome {
                    this.message = reply0.outcome.message
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.message = reply3.result.message
                    this.referenceIds.addAll(reply3.result.referenceIdsList)
                    this.status = "ok"
                }
                this.stop = stop {
                    this.audit = auditInfo {
                        this.updatedBy = reply2.fuelPolicy.audit.updatedBy
                    }
                    this.organizer = request.stop.organizer
                    this.stopId = request.stopId
                }
            }
        }

    /** Scores a stop. */
    override suspend fun scoreStop(request: ScoreStopRequest): ScoreStopResponse {
        return handleScoreStop(request)
    }

    /** Validates a stop. */
    override suspend fun validateStop(request: ValidateStopRequest): ValidateStopResponse = handleValidateStop(request)

    private suspend fun handleCheckStopEligibility(request: CheckStopEligibilityRequest): CheckStopEligibilityResponse =
        RpcBoundary.run("acme.routing.v1.StopValidationService/CheckStopEligibility") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CheckStopEligibility", request)
            log.atInfo().addKeyValue("operation", "CheckStopEligibility").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.stop.stopId).log("Recorded audit detail")
            val outbound0 = LintRoutesRequest.newBuilder().apply {
                setContext(request.context)
                setRoute(route {
                    this.audit = auditInfo {
                        this.updatedBy = request.stop.audit.updatedBy
                    }
                    this.counterparty = driverRef {
                        this.email = request.details.contact.email
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> routingRouteValidationService.lintRoutes(value) }
            var reply1 = PreviewRouteResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<StopParticipant>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound1 = previewRouteRequest {
                            this.context = request.context
                            this.details = PreviewRouteRequestKt.details {
                                this.contact = contactInfo {
                                    this.name = item.contact.name
                                    this.phone = item.contact.phone
                                    this.preferredChannel = item.contact.preferredChannel
                                }
                            }
                            this.route = route {
                                this.counterparty = driverRef {
                                    this.email = item.contact.email
                                }
                            }
                        }
                        reply1 = routingRouteValidationService.previewRoute(outbound1)
                    }
                }
                try {
                    request.stop.participantsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound2 = lintFuelPoliciesRequest {
                this.context = request.context
                this.details = LintFuelPoliciesRequestKt.details {
                    this.contact = contactInfo {
                        this.email = reply0.route.counterparty.email
                    }
                }
            }
            val reply2 = ClientRegistry.lookup("acme.fuel.v1.FuelPolicyValidationService", FuelPolicyValidationServiceGrpcKt.FuelPolicyValidationServiceCoroutineStub::class.java).lintFuelPolicies(outbound2)
            val outbound3 = quoteRouteRequest {
                this.context = request.context
                this.details = QuoteRouteRequestKt.details {
                    this.destination = postalAddress {
                        this.locality = reply0.route.billingAddress.locality
                    }
                }
                this.route = route {
                    this.bank = routeBankSettlement {
                        this.iban = when (reply1.route.settlementCase) {
                            Route.SettlementCase.BANK -> reply1.route.bank.iban
                            else -> ""
                        }
                    }
                    this.lines.addAll(listOf(routeLine {
                        this.tax = routeLineTax {
                            this.amount = when (reply2.fuelPolicy.actionCase) {
                                FuelPolicy.ActionCase.DISCOUNT -> reply2.fuelPolicy.discount.amount
                                else -> Money.getDefaultInstance()
                            }
                        }
                    }))
                }
            }
            val reply3 = routingRouteCalculationService.quoteRoute(outbound3)
            checkStopEligibilityResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.stop = stop {
                    this.audit = auditInfo {
                        this.createdAt = reply2.fuelPolicy.audit.createdAt
                        this.updatedBy = reply3.route.audit.updatedBy
                    }
                    this.metadata.putAll(reply0.route.metadataMap)
                    this.organizer = userRef {
                        this.email = reply2.fuelPolicy.owner.email
                    }
                    this.stopId = request.stopId
                    this.window = request.stop.window
                }
            }
        }

    private suspend fun handleGetStopRules(request: GetStopRulesRequest): GetStopRulesResponse =
        RpcBoundary.run("acme.routing.v1.StopValidationService/GetStopRules") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetStopRules", request)
            log.atInfo().addKeyValue("operation", "GetStopRules").log("Handling transport request")
            val configure0: CalculateYardRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.yard = yard {
                    this.owner = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val outbound0 = calculateYardRequest(configure0)
            val reply0 = yardsYardCalculationService.calculateYard(outbound0)
            getStopRulesResponse {
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
                this.stop = stop {
                    this.location = postalAddress {
                        this.lines.addAll(reply0.yard.address.linesList)
                    }
                    this.stopId = request.stopId
                }
            }
        }

    private fun LintStopsRequest.toCompareWorkOrderOptionsFromLintStops3(reply0: SimulateDeliveryExceptionResponse, reply2: RecalculateRoutePlansResponse): CompareWorkOrderOptionsRequest {
        val source = this
        return compareWorkOrderOptionsRequest {
            this.context = source.context
            this.details = CompareWorkOrderOptionsRequestKt.details {
                this.destination = postalAddress {
                    this.lines.addAll(reply2.routePlan.billingAddress.linesList)
                }
            }
            this.workOrder = workOrder {
                this.sla = workOrderSla {
                    this.dueAt = reply0.deliveryException.sla.dueAt
                }
            }
        }
    }

    private suspend fun handleLintStops(request: LintStopsRequest): LintStopsResponse =
        RpcBoundary.run("acme.routing.v1.StopValidationService/LintStops") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LintStops", request)
            log.atInfo().addKeyValue("operation", "LintStops").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.stop.stopId).log("Recorded audit detail")
            val outbound0 = simulateDeliveryExceptionRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.assignee = userRef {
                        this.email = request.details.contact.email
                    }
                    this.requester = contactInfo {
                        this.name = request.details.contact.name
                    }
                }
                this.details = SimulateDeliveryExceptionRequestKt.details {
                    this.destination = postalAddress {
                        this.postalCode = request.stop.location.postalCode
                        this.recipient = request.stop.location.recipient
                    }
                }
            }
            val reply0 = deliveriesDeliveryExceptionCalculationService.simulateDeliveryException(outbound0)
            val outbound1 = calculateYardRequest {
                this.context = request.context
                this.details = CalculateYardRequestKt.details {
                    this.destination = postalAddress {
                        this.postalCode = request.stop.location.postalCode
                    }
                }
                this.yard = yard {
                    this.owner = userRef {
                        this.email = reply0.deliveryException.requester.email
                    }
                }
            }
            val reply1 = yardsYardCalculationService.calculateYard(outbound1)
            val outbound2 = recalculateRoutePlansRequest {}.routingCopy {
                this.context = request.context
                this.details = RecalculateRoutePlansRequestKt.details {
                    this.destination = postalAddress {
                        this.recipient = reply1.yard.address.recipient
                        this.region = request.stop.location.region
                    }
                }
                this.routePlan = routePlan {
                    this.billingAddress = postalAddress {
                        this.postalCode = reply1.yard.address.postalCode
                    }
                    this.counterparty = driverRef {
                        this.displayName = reply0.deliveryException.assignee.displayName
                    }
                }
            }
            val reply2 = routingRoutePlanCalculationService.recalculateRoutePlans(outbound2)
            val outbound3 = request.toCompareWorkOrderOptionsFromLintStops3(reply0, reply2)
            val reply3 = maintenanceWorkOrderCalculationService.compareWorkOrderOptions(outbound3)
            lintStopsResponse {
                this.outcome = LintStopsResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.status = "ok"
                }
                this.stop = stop {
                    this.audit = auditInfo {
                        this.etag = reply3.workOrder.audit.etag
                        this.updatedAt = reply0.deliveryException.audit.updatedAt
                    }
                    this.organizer = userRef {
                        this.email = reply3.workOrder.customer.email
                    }
                    this.stopId = request.stopId
                    this.window = request.stop.window
                }
            }
        }

    private data class PreviewStop3Command(
        val context: RequestContext,
        val filterRegion: String,
        val filterCreatedAfter: Timestamp,
        val filterOwnerId: String
    ) {
        fun toRequest(): BrowsePatientsRequest {
            val command = this
            return browsePatientsRequest {
                this.context = command.context
                this.filter = BrowsePatientsRequestKt.filter {
                    this.createdAfter = command.filterCreatedAfter
                    this.ownerId = command.filterOwnerId
                    this.region = command.filterRegion
                }
            }
        }
    }

    private suspend fun handleScoreStop(request: ScoreStopRequest): ScoreStopResponse =
        RpcBoundary.run("acme.routing.v1.StopValidationService/ScoreStop") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScoreStop", request)
            log.atInfo().addKeyValue("operation", "ScoreStop").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.stop.stopId).log("Recorded audit detail")
            val outbound0 = simulateDeliveryExceptionRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.assignee = userRef {
                        this.displayName = request.stop.organizer.displayName
                    }
                }
            }
            val command0: Submission<SimulateDeliveryExceptionRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = deliveriesDeliveryExceptionCalculationService.simulateDeliveryException(payload0)
            val outbound1 = checkRouteEligibilityRequest {
                this.context = request.context
                this.route = route {
                    this.audit = auditInfo {
                        this.updatedAt = request.stop.audit.updatedAt
                    }
                }
            }
            val adapter1 = OutboundPort<CheckRouteEligibilityRequest, CheckRouteEligibilityResponse> { payload ->
                routingRouteValidationService.checkRouteEligibility(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            scoreStopResponse {
                this.breakdown = ScoreStopResponseKt.breakdown {
                    this.total = reply1.route.total
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.message = reply0.result.message
                    this.status = "ok"
                }
                this.stop = stop {
                    this.stopId = request.stopId
                    this.title = request.stop.title
                }
            }
        }

    private suspend fun handleValidateStop(request: ValidateStopRequest): ValidateStopResponse =
        RpcBoundary.run("acme.routing.v1.StopValidationService/ValidateStop") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ValidateStop", request)
            log.atInfo().addKeyValue("operation", "ValidateStop").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.stop.stopId).log("Recorded audit detail")
            var reply0 = LintRoutesResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.stop.participantsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound0 = lintRoutesRequest {
                                this.context = request.context
                                this.details = LintRoutesRequestKt.details {
                                    this.contact = contactInfo {
                                        this.email = item.contact.email
                                        this.name = item.contact.name
                                        this.phone = item.contact.phone
                                    }
                                }
                            }
                            routingRouteValidationService.lintRoutes(outbound0)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply0 = it }
                }
            }
            val outbound1 = estimateRouteRequest {
                this.context = request.context
                this.route = route {
                    this.audit = auditInfo {
                        this.createdBy = request.stop.audit.createdBy
                    }
                    this.bank = routeBankSettlement {
                        this.accountHolder = reply0.route.bank.accountHolder
                    }
                    this.counterparty = driverRef {
                        this.driverId = reply0.route.counterparty.driverId
                    }
                }
            }
            val invoke1: suspend (EstimateRouteRequest, Metadata) -> EstimateRouteResponse = routingRouteCalculationService::estimateRoute
            val reply1 = invoke1(outbound1, Metadata())
            val outbound2 = FindYardsRequest.newBuilder().apply {
                setContext(request.context)
                setFilter(FindYardsRequestKt.filter {
                    this.createdAfter = request.stop.audit.createdAt
                    this.region = request.stop.location.region
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply2 = outbound2.let { value -> yardsYardQueryService.findYards(value) }
            val outbound3 = RequestFactory.create {
                validateRouteRequest {
                    this.context = request.context
                    this.details = ValidateRouteRequestKt.details {
                        this.contact = contactInfo {
                            this.phone = request.details.contact.phone
                        }
                    }
                }
            }
            val reply3 = routingRouteValidationService.validateRoute(outbound3)
            validateStopResponse {
                this.result = operationResult {
                    this.message = reply2.result.message
                    this.status = "ok"
                }
                this.stop = stop {
                    this.location = postalAddress {
                        this.recipient = reply1.route.billingAddress.recipient
                        this.region = reply1.route.billingAddress.region
                    }
                    this.participants.addAll(listOf(stopParticipant {
                        this.contact = contactInfo {
                            this.email = reply3.route.counterparty.email
                        }
                    }))
                    this.status = request.stop.status
                    this.stopId = request.stopId
                }
            }
        }
}
