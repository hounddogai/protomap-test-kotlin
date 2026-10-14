package com.acme.transport.routing.adapters

import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.ValidateAuditEventRequest
import com.acme.audit.v1.ValidateAuditEventRequestKt
import com.acme.audit.v1.ValidateAuditEventResponse
import com.acme.audit.v1.validateAuditEventRequest
import com.acme.authorization.v1.AccessPolicyValidationServiceGrpcKt
import com.acme.authorization.v1.CheckAccessPolicyEligibilityRequestKt
import com.acme.authorization.v1.accessPolicy
import com.acme.authorization.v1.checkAccessPolicyEligibilityRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.CompareDeliveryExceptionOptionsRequestKt
import com.acme.deliveries.v1.CompareDeliveryExceptionOptionsResponse
import com.acme.deliveries.v1.DeliveryExceptionCalculationServiceGrpcKt
import com.acme.deliveries.v1.compareDeliveryExceptionOptionsRequest
import com.acme.deliveries.v1.deliveryException
import com.acme.drivers.v1.driverRef
import com.acme.featureflags.v1.CheckFeatureFlagEligibilityRequestKt
import com.acme.featureflags.v1.FeatureFlagValidationServiceGrpcKt
import com.acme.featureflags.v1.checkFeatureFlagEligibilityRequest
import com.acme.featureflags.v1.featureFlag
import com.acme.fuel.v1.FuelPolicyCalculationServiceGrpcKt
import com.acme.fuel.v1.QuoteFuelPolicyRequestKt
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.quoteFuelPolicyRequest
import com.acme.identity.v1.GetUserRequest
import com.acme.identity.v1.PeekUserRequest
import com.acme.identity.v1.UserQueryServiceGrpcKt
import com.acme.identity.v1.UserServiceGrpcKt
import com.acme.identity.v1.getUserRequest
import com.acme.identity.v1.peekUserRequest
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.WorkOrderValidationServiceGrpcKt
import com.acme.maintenance.v1.scoreWorkOrderRequest
import com.acme.maintenance.v1.workOrder
import com.acme.metrics.v1.MetricSeriesValidationServiceGrpcKt
import com.acme.metrics.v1.metricSeries
import com.acme.metrics.v1.validateMetricSeriesRequest
import com.acme.notifications.v1.NotificationValidationServiceGrpcKt
import com.acme.notifications.v1.ValidateNotificationRequestKt
import com.acme.notifications.v1.notification
import com.acme.notifications.v1.notificationTemplateRef
import com.acme.notifications.v1.validateNotificationRequest
import com.acme.ratelimiting.v1.CreateRateLimitRequest
import com.acme.ratelimiting.v1.RateLimitServiceGrpcKt
import com.acme.ratelimiting.v1.createRateLimitRequest
import com.acme.ratelimiting.v1.rateLimit
import com.acme.routing.v1.CheckRoutePlanEligibilityRequest
import com.acme.routing.v1.CheckRoutePlanEligibilityResponse
import com.acme.routing.v1.CheckRoutePlanEligibilityResponseKt
import com.acme.routing.v1.CompareRouteOptionsRequest
import com.acme.routing.v1.CompareRoutePlanOptionsResponse
import com.acme.routing.v1.EstimateStopRequest
import com.acme.routing.v1.EstimateStopRequestKt
import com.acme.routing.v1.ExplainRoutePlanDecisionRequest
import com.acme.routing.v1.ExplainRoutePlanDecisionResponse
import com.acme.routing.v1.ExplainRoutePlanDecisionResponseKt
import com.acme.routing.v1.ExplainStopCalculationRequestKt
import com.acme.routing.v1.GetRoutePlanRulesRequest
import com.acme.routing.v1.GetRoutePlanRulesResponse
import com.acme.routing.v1.LintRoutePlansRequest
import com.acme.routing.v1.LintRoutePlansResponse
import com.acme.routing.v1.LintRoutePlansResponseKt
import com.acme.routing.v1.NormalizeRoutePlanRequest
import com.acme.routing.v1.NormalizeRoutePlanResponse
import com.acme.routing.v1.PreviewRoutePlanRequest
import com.acme.routing.v1.PreviewRoutePlanResponse
import com.acme.routing.v1.RouteCalculationServiceGrpcKt
import com.acme.routing.v1.RoutePlanCalculationServiceGrpcKt
import com.acme.routing.v1.RoutePlanLine
import com.acme.routing.v1.RoutePlanValidationServiceGrpcKt
import com.acme.routing.v1.RouteValidationServiceGrpcKt
import com.acme.routing.v1.ScoreRoutePlanRequest
import com.acme.routing.v1.ScoreRoutePlanResponse
import com.acme.routing.v1.SimulateStopRequestKt
import com.acme.routing.v1.StopCalculationServiceGrpcKt
import com.acme.routing.v1.ValidateRoutePlanRequest
import com.acme.routing.v1.ValidateRoutePlanResponse
import com.acme.routing.v1.ValidateRoutePlanResponseKt
import com.acme.routing.v1.ValidateRouteRequest
import com.acme.routing.v1.ValidateRouteRequestKt
import com.acme.routing.v1.ValidateRouteResponse
import com.acme.routing.v1.checkRoutePlanEligibilityResponse
import com.acme.routing.v1.compareRouteOptionsRequest
import com.acme.routing.v1.compareRoutePlanOptionsRequest
import com.acme.routing.v1.copy as routingCopy
import com.acme.routing.v1.estimateStopRequest
import com.acme.routing.v1.explainRoutePlanDecisionResponse
import com.acme.routing.v1.explainStopCalculationRequest
import com.acme.routing.v1.getRoutePlanRatesRequest
import com.acme.routing.v1.getRoutePlanRulesResponse
import com.acme.routing.v1.lintRoutePlansResponse
import com.acme.routing.v1.normalizeRoutePlanResponse
import com.acme.routing.v1.previewRoutePlanResponse
import com.acme.routing.v1.route
import com.acme.routing.v1.routePlan
import com.acme.routing.v1.routePlanLine
import com.acme.routing.v1.routePlanLineTax
import com.acme.routing.v1.scoreRoutePlanResponse
import com.acme.routing.v1.simulateStopRequest
import com.acme.routing.v1.stop
import com.acme.routing.v1.stopParticipant
import com.acme.routing.v1.validateRoutePlanResponse
import com.acme.routing.v1.validateRouteRequest
import com.acme.search.v1.SearchIndexServiceGrpcKt
import com.acme.search.v1.SearchSearchIndexesRequestKt
import com.acme.search.v1.SearchSearchIndexesResponse
import com.acme.search.v1.searchSearchIndexesRequest
import com.acme.tours.v1.TourServiceGrpcKt
import com.acme.tours.v1.createTourRequest
import com.acme.tours.v1.tour
import com.acme.tours.v1.tourParticipant
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.transport.routing.RoutingRepository
import com.google.protobuf.Any as ProtoAny
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

/** Checks route plans against the rules before anything acts on them. */
class RoutePlanValidationController(
    private val store: RoutingRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventValidationService: AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub,
    private val authorizationAccessPolicyValidationService: AccessPolicyValidationServiceGrpcKt.AccessPolicyValidationServiceCoroutineStub,
    private val deliveriesDeliveryExceptionCalculationService: DeliveryExceptionCalculationServiceGrpcKt.DeliveryExceptionCalculationServiceCoroutineStub,
    private val featureflagsFeatureFlagValidationService: FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub,
    private val fuelFuelPolicyCalculationService: FuelPolicyCalculationServiceGrpcKt.FuelPolicyCalculationServiceCoroutineStub,
    private val identityUserQueryService: UserQueryServiceGrpcKt.UserQueryServiceCoroutineStub,
    private val identityUserService: UserServiceGrpcKt.UserServiceCoroutineStub,
    private val maintenanceWorkOrderValidationService: WorkOrderValidationServiceGrpcKt.WorkOrderValidationServiceCoroutineStub,
    private val metricsMetricSeriesValidationService: MetricSeriesValidationServiceGrpcKt.MetricSeriesValidationServiceCoroutineStub,
    private val notificationsNotificationValidationService: NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub,
    private val ratelimitingRateLimitService: RateLimitServiceGrpcKt.RateLimitServiceCoroutineStub,
    private val routingRouteCalculationService: RouteCalculationServiceGrpcKt.RouteCalculationServiceCoroutineStub,
    private val routingRoutePlanCalculationService: RoutePlanCalculationServiceGrpcKt.RoutePlanCalculationServiceCoroutineStub,
    private val routingRouteValidationService: RouteValidationServiceGrpcKt.RouteValidationServiceCoroutineStub,
    private val routingStopCalculationService: StopCalculationServiceGrpcKt.StopCalculationServiceCoroutineStub,
    private val searchSearchIndexService: SearchIndexServiceGrpcKt.SearchIndexServiceCoroutineStub,
    private val toursTourService: TourServiceGrpcKt.TourServiceCoroutineStub,
) : RoutePlanValidationServiceGrpcKt.RoutePlanValidationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(RoutePlanValidationController::class.java)

    private val workerForValidateRoutePlan1 = ReplyMailbox<ValidateAuditEventRequest, ValidateAuditEventResponse>(workers) { payload ->
        withTimeout(5_000) { auditAuditEventValidationService.validateAuditEvent(payload) }
    }

    private val clientForValidateRoutePlan2 by lazy { maintenanceWorkOrderValidationService }

    /** Checks a route plan. */
    override suspend fun checkRoutePlanEligibility(request: CheckRoutePlanEligibilityRequest): CheckRoutePlanEligibilityResponse = handleCheckRoutePlanEligibility(request)

    /** Explains a route plan. */
    override suspend fun explainRoutePlanDecision(request: ExplainRoutePlanDecisionRequest): ExplainRoutePlanDecisionResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanValidationService/ExplainRoutePlanDecision") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainRoutePlanDecision", request)
            log.atInfo().addKeyValue("operation", "ExplainRoutePlanDecision").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.routePlan.routePlanId).log("Recorded audit detail")
            val outbound0 = checkAccessPolicyEligibilityRequest {
                this.accessPolicy = accessPolicy {
                    this.audit = auditInfo {
                        this.updatedBy = request.routePlan.audit.updatedBy
                    }
                }
                this.context = request.context
                this.details = CheckAccessPolicyEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.phone = request.details.contact.phone
                    }
                }
            }
            val reply0 = authorizationAccessPolicyValidationService.checkAccessPolicyEligibility(outbound0)
            explainRoutePlanDecisionResponse {
                this.outcome = ExplainRoutePlanDecisionResponseKt.outcome {
                    this.nextSteps.addAll(reply0.outcome.nextStepsList)
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.routePlan = routePlan {
                    this.lines.addAll(listOf(routePlanLine {
                        this.tax = routePlanLineTax {
                            this.amount = reply0.accessPolicy.discount.amount
                        }
                    }))
                    this.routePlanId = request.routePlanId
                    this.status = request.routePlan.status
                }
            }
        }

    /** Returns a route plan. */
    override suspend fun getRoutePlanRules(request: GetRoutePlanRulesRequest): GetRoutePlanRulesResponse {
        return handleGetRoutePlanRules(request)
    }

    /** Lints a route plan. */
    override suspend fun lintRoutePlans(request: LintRoutePlansRequest): LintRoutePlansResponse = handleLintRoutePlans(request)

    /** Normalizes a route plan. */
    override suspend fun normalizeRoutePlan(request: NormalizeRoutePlanRequest): NormalizeRoutePlanResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanValidationService/NormalizeRoutePlan") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "NormalizeRoutePlan", request)
            log.atInfo().addKeyValue("operation", "NormalizeRoutePlan").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.routePlan.routePlanId).log("Recorded audit detail")
            val outbound0 = NormalizeRoutePlan0Command(
                context = request.context,
                userId = request.context.actor.userId
            ).toRequest()
            val reply0 = identityUserQueryService.peekUser(outbound0)
            val outbound1 = compareRouteOptionsRequest {
                this.context = request.context
                this.route = route {
                    this.audit = auditInfo {
                        this.createdAt = reply0.user.audit.createdAt
                        this.createdBy = request.routePlan.audit.createdBy
                    }
                }
            }
            val command1: Submission<CompareRouteOptionsRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = routingRouteCalculationService.compareRouteOptions(payload1)
            val outbound2 = validateRouteRequest {
                this.context = request.context
                this.details = ValidateRouteRequestKt.details {
                    this.contact = contactInfo {
                        this.phone = request.details.contact.phone
                    }
                }
                this.route = route {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.user.audit.updatedAt
                    }
                    this.billingAddress = postalAddress {
                        this.locality = reply1.route.billingAddress.locality
                        this.recipient = reply1.route.billingAddress.recipient
                    }
                }
            }
            val adapter2 = OutboundPort<ValidateRouteRequest, ValidateRouteResponse> { payload ->
                routingRouteValidationService.validateRoute(payload)
            }
            val port2 = DelegatingPort(adapter2)
            val reply2 = port2.execute(outbound2)
            normalizeRoutePlanResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply1.result.message
                    this.status = "ok"
                }
                this.routePlan = routePlan {
                    this.number = request.routePlan.number
                    this.routePlanId = request.routePlanId
                    this.total = reply1.route.total
                }
            }
        }

    /** Previews a route plan. */
    override suspend fun previewRoutePlan(request: PreviewRoutePlanRequest): PreviewRoutePlanResponse {
        return handlePreviewRoutePlan(request)
    }

    /** Scores a route plan. */
    override suspend fun scoreRoutePlan(request: ScoreRoutePlanRequest): ScoreRoutePlanResponse = handleScoreRoutePlan(request)

    /** Validates a route plan. */
    override suspend fun validateRoutePlan(request: ValidateRoutePlanRequest): ValidateRoutePlanResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanValidationService/ValidateRoutePlan") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ValidateRoutePlan", request)
            log.atInfo().addKeyValue("operation", "ValidateRoutePlan").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.routePlan.number).log("Recorded audit detail")
            val outbound0 = getRoutePlanRatesRequest {
                this.context = request.context
                this.routePlanId = request.routePlan.routePlanId
            }
            val reply0 = routingRoutePlanCalculationService.getRoutePlanRates(WireCopies.reflective(outbound0))
            val outbound1 = validateAuditEventRequest {
                this.context = request.context
                this.details = ValidateAuditEventRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.details.contact.email
                    }
                }
            }
            val reply1 = workerForValidateRoutePlan1.submit(outbound1)
            val outbound2 = scoreWorkOrderRequest {
                this.context = request.context
                this.workOrder = workOrder {
                    this.customer = driverRef {
                        this.driverId = request.routePlan.counterparty.driverId
                        this.email = reply0.routePlan.counterparty.email
                    }
                }
            }
            val reply2 = clientForValidateRoutePlan2.scoreWorkOrder(outbound2)
            validateRoutePlanResponse {
                this.outcome = ValidateRoutePlanResponseKt.outcome {
                    this.message = reply1.outcome.message
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
                this.routePlan = routePlan {
                    this.audit = auditInfo {
                        this.updatedAt = reply2.workOrder.audit.updatedAt
                    }
                    this.dueAt = reply0.routePlan.dueAt
                    this.notes = request.routePlan.notes
                    this.routePlanId = request.routePlanId
                }
            }
        }

    private suspend fun handleCheckRoutePlanEligibility(request: CheckRoutePlanEligibilityRequest): CheckRoutePlanEligibilityResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanValidationService/CheckRoutePlanEligibility") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CheckRoutePlanEligibility", request)
            log.atInfo().addKeyValue("operation", "CheckRoutePlanEligibility").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.routePlan.routePlanId).log("Recorded audit detail")
            val outbound0 = validateNotificationRequest {
                this.context = request.context
                this.details = ValidateNotificationRequestKt.details {
                    this.contact = contactInfo {
                        this.preferredChannel = request.details.contact.preferredChannel
                    }
                }
                this.notification = notification {
                    this.audit = auditInfo {
                        this.etag = request.routePlan.audit.etag
                    }
                    this.customer = notificationTemplateRef {
                        this.number = request.routePlan.number
                    }
                    this.requester = contactInfo {
                        this.email = request.details.contact.email
                    }
                }
            }
            val reply0 = flowOf(outbound0).map { value -> notificationsNotificationValidationService.validateNotification(value) }.single()
            val outbound1 = getUserRequest {
                this.context = request.context
                this.userId = request.context.actor.userId
            }
            val reply1 = withTimeout(5_000) { identityUserService.getUser(outbound1) }
            val configure2: SimulateStopRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.stop = stop {
                    this.location = postalAddress {
                        this.countryCode = request.routePlan.billingAddress.countryCode
                    }
                    this.organizer = userRef {
                        this.email = reply0.notification.assignee.email
                        this.userId = reply1.user.relationshipManager.userId
                    }
                    this.participants.addAll(listOf(stopParticipant {
                        this.contact = contactInfo {
                            this.email = reply1.user.relationshipManager.email
                        }
                    }))
                }
            }
            val outbound2 = simulateStopRequest(configure2)
            val reply2 = routingStopCalculationService.simulateStop(outbound2)
            val outbound3 = createTourRequest {
                this.context = request.context
                this.tour = tour {
                    this.audit = auditInfo {
                        this.createdAt = reply0.notification.audit.createdAt
                    }
                    this.participants.addAll(listOf(tourParticipant {
                        this.contact = contactInfo {
                            this.email = reply1.user.contact.email
                            this.name = request.details.contact.name
                        }
                    }))
                }
            }
            val reply3 = toursTourService.createTour(outbound3)
            checkRoutePlanEligibilityResponse {
                this.outcome = CheckRoutePlanEligibilityResponseKt.outcome {
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.routePlan = routePlan {
                    this.audit = auditInfo {
                        this.updatedBy = reply3.tour.audit.updatedBy
                    }
                    this.billingAddress = postalAddress {
                        this.countryCode = reply1.user.primaryAddress.countryCode
                        this.locality = reply1.user.primaryAddress.locality
                    }
                    this.issuedAt = request.routePlan.issuedAt
                    this.routePlanId = request.routePlanId
                }
            }
        }

    private fun GetRoutePlanRulesRequest.toGetUserFromGetRoutePlanRules1(): GetUserRequest {
        val source = this
        return getUserRequest {
            this.context = source.context
            this.userId = source.context.actor.userId
        }
    }

    private suspend fun handleGetRoutePlanRules(request: GetRoutePlanRulesRequest): GetRoutePlanRulesResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanValidationService/GetRoutePlanRules") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetRoutePlanRules", request)
            log.atInfo().addKeyValue("operation", "GetRoutePlanRules").log("Handling transport request")
            val outbound0 = explainStopCalculationRequest {}.routingCopy {
                this.context = request.context
                this.details = ExplainStopCalculationRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = routingStopCalculationService.explainStopCalculation(outbound0)
            val outbound1 = request.toGetUserFromGetRoutePlanRules1()
            val reply1 = identityUserService.getUser(outbound1)
            getRoutePlanRulesResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
                this.routePlan = routePlan {
                    this.billingAddress = postalAddress {
                        this.postalCode = reply1.user.primaryAddress.postalCode
                    }
                    this.routePlanId = request.routePlanId
                }
            }
        }

    private suspend fun handleLintRoutePlans(request: LintRoutePlansRequest): LintRoutePlansResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanValidationService/LintRoutePlans") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LintRoutePlans", request)
            log.atInfo().addKeyValue("operation", "LintRoutePlans").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.routePlan.routePlanId).log("Recorded audit detail")
            val outbound0 = quoteFuelPolicyRequest {
                this.context = request.context
                this.details = QuoteFuelPolicyRequestKt.details {
                    this.destination = postalAddress {
                        this.locality = request.routePlan.billingAddress.locality
                        this.region = request.routePlan.billingAddress.region
                    }
                }
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.createdAt = request.routePlan.audit.createdAt
                        this.etag = request.routePlan.audit.etag
                    }
                }
            }
            val reply0 = fuelFuelPolicyCalculationService.quoteFuelPolicy(outbound0)
            val outbound1 = createRateLimitRequest {
                this.context = request.context
                this.rateLimit = rateLimit {
                    this.audit = auditInfo {
                        this.createdAt = request.routePlan.audit.createdAt
                    }
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = ratelimitingRateLimitService.createRateLimit(envelope1.unpack(CreateRateLimitRequest::class.java))
            val outbound2 = validateMetricSeriesRequest {
                this.context = request.context
                this.metricSeries = metricSeries {
                    this.owner = userRef {
                        this.email = reply1.rateLimit.owner.email
                    }
                }
            }
            val reply2 = metricsMetricSeriesValidationService.validateMetricSeries(WireCopies.json(outbound2))
            lintRoutePlansResponse {
                this.outcome = LintRoutePlansResponseKt.outcome {
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.message = reply2.outcome.message
                    this.status = "ok"
                }
                this.routePlan = routePlan {
                    this.audit = auditInfo {
                        this.updatedAt = reply2.metricSeries.audit.updatedAt
                        this.updatedBy = reply0.fuelPolicy.audit.updatedBy
                    }
                    this.number = request.routePlan.number
                    this.routePlanId = request.routePlanId
                }
            }
        }

    private data class NormalizeRoutePlan0Command(
        val context: RequestContext,
        val userId: String
    ) {
        fun toRequest(): PeekUserRequest {
            val command = this
            return peekUserRequest {
                this.context = command.context
                this.userId = command.userId
            }
        }
    }

    private suspend fun handlePreviewRoutePlan(request: PreviewRoutePlanRequest): PreviewRoutePlanResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanValidationService/PreviewRoutePlan") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PreviewRoutePlan", request)
            log.atInfo().addKeyValue("operation", "PreviewRoutePlan").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.routePlan.routePlanId).log("Recorded audit detail")
            var reply0 = CompareDeliveryExceptionOptionsResponse.getDefaultInstance()
            request.routePlan.linesList.asFlow().map { item ->
                val outbound0 = compareDeliveryExceptionOptionsRequest {
                    this.context = request.context
                    this.deliveryException = deliveryException {
                        this.requester = contactInfo {
                            this.email = request.routePlan.counterparty.email
                            this.phone = request.details.contact.phone
                        }
                    }
                    this.details = CompareDeliveryExceptionOptionsRequestKt.details {
                        this.budget = item.tax.amount
                        this.destination = postalAddress {
                            this.lines.addAll(request.routePlan.billingAddress.linesList)
                        }
                    }
                }
                deliveriesDeliveryExceptionCalculationService.compareDeliveryExceptionOptions(outbound0)
            }.collect { reply0 = it }
            var reply1 = CompareRoutePlanOptionsResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<RoutePlanLine>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound1 = compareRoutePlanOptionsRequest {
                            this.context = request.context
                            this.routePlan = routePlan {
                                this.lines.addAll(listOf(routePlanLine {
                                    this.tax = routePlanLineTax {
                                        this.rate = item.tax.rate
                                    }
                                }))
                            }
                        }
                        reply1 = routingRoutePlanCalculationService.compareRoutePlanOptions(outbound1)
                    }
                }
                try {
                    request.routePlan.linesList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            previewRoutePlanResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.routePlan = routePlan {
                    this.dueAt = request.routePlan.dueAt
                    this.routePlanId = request.routePlanId
                }
            }
        }

    private suspend fun handleScoreRoutePlan(request: ScoreRoutePlanRequest): ScoreRoutePlanResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanValidationService/ScoreRoutePlan") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScoreRoutePlan", request)
            log.atInfo().addKeyValue("operation", "ScoreRoutePlan").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.routePlan.routePlanId).log("Recorded audit detail")
            val outbound0 = EstimateStopRequest.newBuilder().apply {
                setContext(request.context)
                setDetails(EstimateStopRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = request.details.destination.countryCode
                        this.lines.addAll(request.routePlan.billingAddress.linesList)
                        this.locality = request.details.destination.locality
                    }
                })
                setStop(stop {
                    this.audit = auditInfo {
                        this.updatedBy = request.routePlan.audit.updatedBy
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> routingStopCalculationService.estimateStop(value) }
            var reply1 = SearchSearchIndexesResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.routePlan.linesList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound1 = searchSearchIndexesRequest {
                                this.context = request.context
                                this.filter = SearchSearchIndexesRequestKt.filter {
                                    this.createdAfter = request.routePlan.audit.createdAt
                                    this.labels.putAll(item.attributesMap)
                                    this.ownerId = item.lineId
                                    this.region = request.details.destination.region
                                }
                            }
                            searchSearchIndexService.searchSearchIndexes(outbound1)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply1 = it }
                }
            }
            val outbound2 = checkFeatureFlagEligibilityRequest {
                this.context = request.context
                this.details = CheckFeatureFlagEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.routePlan.counterparty.email
                    }
                }
                this.featureFlag = featureFlag {
                    this.audit = auditInfo {
                        this.createdAt = request.routePlan.audit.createdAt
                        this.etag = reply0.stop.audit.etag
                        this.updatedAt = reply0.stop.audit.updatedAt
                    }
                }
            }
            val reply2 = ClientRegistry.lookup("acme.featureflags.v1.FeatureFlagValidationService", FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub::class.java).checkFeatureFlagEligibility(outbound2)
            scoreRoutePlanResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
                this.routePlan = routePlan {
                    this.counterparty = driverRef {
                        this.email = reply2.featureFlag.owner.email
                    }
                    this.routePlanId = request.routePlanId
                    this.total = request.routePlan.total
                }
            }
        }
}
