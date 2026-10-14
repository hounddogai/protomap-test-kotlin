package com.acme.transport.routing.grpc

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.drivers.v1.driverRef
import com.acme.featureflags.v1.FeatureFlagValidationServiceGrpcKt
import com.acme.featureflags.v1.PreviewFeatureFlagRequest
import com.acme.featureflags.v1.PreviewFeatureFlagRequestKt
import com.acme.featureflags.v1.featureFlag
import com.acme.featureflags.v1.previewFeatureFlagRequest
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.PartValidationServiceGrpcKt
import com.acme.maintenance.v1.PreviewPartRequest
import com.acme.maintenance.v1.part
import com.acme.maintenance.v1.previewPartRequest
import com.acme.notifications.v1.ListNotificationsRequestKt
import com.acme.notifications.v1.NotificationServiceGrpcKt
import com.acme.notifications.v1.listNotificationsRequest
import com.acme.routing.v1.CalculateStopRequest
import com.acme.routing.v1.CalculateStopResponse
import com.acme.routing.v1.CheckRouteEligibilityRequest
import com.acme.routing.v1.CheckRouteEligibilityResponse
import com.acme.routing.v1.CompareStopOptionsRequest
import com.acme.routing.v1.ExplainRouteCalculationRequest
import com.acme.routing.v1.ExplainRouteDecisionRequest
import com.acme.routing.v1.ExplainRouteDecisionResponse
import com.acme.routing.v1.ExplainStopCalculationRequest
import com.acme.routing.v1.ExplainStopCalculationResponse
import com.acme.routing.v1.GetRouteRulesRequest
import com.acme.routing.v1.GetRouteRulesResponse
import com.acme.routing.v1.LintRoutesRequest
import com.acme.routing.v1.LintRoutesResponse
import com.acme.routing.v1.PreviewRouteRequest
import com.acme.routing.v1.PreviewRouteResponse
import com.acme.routing.v1.PreviewRouteResponseKt
import com.acme.routing.v1.QuoteRouteResponse
import com.acme.routing.v1.RouteCalculationServiceGrpcKt
import com.acme.routing.v1.RouteLine
import com.acme.routing.v1.RouteValidationServiceGrpcKt
import com.acme.routing.v1.ScoreRouteRequest
import com.acme.routing.v1.ScoreRouteResponse
import com.acme.routing.v1.StopCalculationServiceGrpcKt
import com.acme.routing.v1.ValidateRouteRequest
import com.acme.routing.v1.ValidateRouteResponse
import com.acme.routing.v1.ValidateRouteResponseKt
import com.acme.routing.v1.calculateStopRequest
import com.acme.routing.v1.checkRouteEligibilityResponse
import com.acme.routing.v1.compareStopOptionsRequest
import com.acme.routing.v1.explainRouteCalculationRequest
import com.acme.routing.v1.explainRouteDecisionResponse
import com.acme.routing.v1.explainStopCalculationRequest
import com.acme.routing.v1.getRouteRulesResponse
import com.acme.routing.v1.lintRoutesResponse
import com.acme.routing.v1.previewRouteResponse
import com.acme.routing.v1.quoteRouteRequest
import com.acme.routing.v1.route
import com.acme.routing.v1.routeLine
import com.acme.routing.v1.routeLineTax
import com.acme.routing.v1.scoreRouteResponse
import com.acme.routing.v1.simulateStopRequest
import com.acme.routing.v1.stop
import com.acme.routing.v1.stopParticipant
import com.acme.routing.v1.validateRouteResponse
import com.acme.telematics.v1.QuoteTripLogResponse
import com.acme.telematics.v1.TripLogCalculationServiceGrpcKt
import com.acme.telematics.v1.quoteTripLogRequest
import com.acme.telematics.v1.tripLog
import com.acme.telematics.v1.tripLogLine
import com.acme.telematics.v1.tripLogLineTax
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.transport.routing.RoutingRepository
import com.google.protobuf.Any as ProtoAny
import com.google.protobuf.Timestamp
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

/** Checks routes against the rules before anything acts on them. */
class RouteValidationEndpoint(
    private val store: RoutingRepository,
    private val workers: CoroutineScope,
    private val featureflagsFeatureFlagValidationService: FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub,
    private val maintenancePartValidationService: PartValidationServiceGrpcKt.PartValidationServiceCoroutineStub,
    private val notificationsNotificationService: NotificationServiceGrpcKt.NotificationServiceCoroutineStub,
    private val routingRouteCalculationService: RouteCalculationServiceGrpcKt.RouteCalculationServiceCoroutineStub,
    private val routingStopCalculationService: StopCalculationServiceGrpcKt.StopCalculationServiceCoroutineStub,
    private val telematicsTripLogCalculationService: TripLogCalculationServiceGrpcKt.TripLogCalculationServiceCoroutineStub,
) : RouteValidationServiceGrpcKt.RouteValidationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(RouteValidationEndpoint::class.java)

    private val invokePreviewRoute0: suspend (ExplainStopCalculationRequest) -> ExplainStopCalculationResponse = { payload ->
        routingStopCalculationService.explainStopCalculation(payload)
    }

    /** Checks a route. */
    override suspend fun checkRouteEligibility(request: CheckRouteEligibilityRequest): CheckRouteEligibilityResponse =
        RpcBoundary.run("acme.routing.v1.RouteValidationService/CheckRouteEligibility") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CheckRouteEligibility", request)
            log.atInfo().addKeyValue("operation", "CheckRouteEligibility").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.route.routeId).log("Recorded audit detail")
            val outbound0 = request.toPreviewPartFromCheckRouteEligibility0()
            val reply0 = maintenancePartValidationService.previewPart(outbound0)
            val outbound1 = previewFeatureFlagRequest {
                this.context = request.context
                this.details = PreviewFeatureFlagRequestKt.details {
                    this.contact = contactInfo {
                        this.preferredChannel = request.details.contact.preferredChannel
                    }
                }
            }
            val reply1 = featureflagsFeatureFlagValidationService.previewFeatureFlag(outbound1)
            checkRouteEligibilityResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.route = route {
                    this.billingAddress = postalAddress {
                        this.recipient = reply0.part.address.recipient
                    }
                    this.counterparty = driverRef {
                        this.email = reply0.part.owner.email
                    }
                    this.dueAt = request.route.dueAt
                    this.lines.addAll(listOf(routeLine {
                        this.amount = reply1.featureFlag.discount.amount
                    }))
                    this.routeId = request.routeId
                }
            }
        }

    /** Explains a route. */
    override suspend fun explainRouteDecision(request: ExplainRouteDecisionRequest): ExplainRouteDecisionResponse {
        return handleExplainRouteDecision(request)
    }

    /** Returns a route. */
    override suspend fun getRouteRules(request: GetRouteRulesRequest): GetRouteRulesResponse = handleGetRouteRules(request)

    /** Lints a route. */
    override suspend fun lintRoutes(request: LintRoutesRequest): LintRoutesResponse =
        RpcBoundary.run("acme.routing.v1.RouteValidationService/LintRoutes") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LintRoutes", request)
            log.atInfo().addKeyValue("operation", "LintRoutes").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.route.routeId).log("Recorded audit detail")
            val outbound0 = calculateStopRequest {
                this.context = request.context
                this.stop = stop {
                    this.participants.addAll(listOf(stopParticipant {
                        this.contact = contactInfo {
                            this.email = request.route.counterparty.email
                        }
                    }))
                }
            }
            val reply0 = routingStopCalculationService.calculateStop(WireCopies.json(outbound0))
            val outbound1 = LintRoutes1Command(
                context = request.context,
                featureFlagAuditCreatedAt = request.route.audit.createdAt
            ).toRequest()
            val reply1 = featureflagsFeatureFlagValidationService.previewFeatureFlag(outbound1)
            val outbound2 = compareStopOptionsRequest {
                this.context = request.context
                this.stop = stop {
                    this.audit = auditInfo {
                        this.updatedBy = request.route.audit.updatedBy
                    }
                    this.location = postalAddress {
                        this.countryCode = reply0.stop.location.countryCode
                    }
                    this.organizer = userRef {
                        this.displayName = request.route.counterparty.displayName
                    }
                    this.participants.addAll(listOf(stopParticipant {
                        this.contact = contactInfo {
                            this.name = request.details.contact.name
                        }
                    }))
                }
            }
            val command2: Submission<CompareStopOptionsRequest> = Submission.Live(outbound2)
            val payload2 = when (val command = command2) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply2 = routingStopCalculationService.compareStopOptions(payload2)
            var reply3 = QuoteRouteResponse.getDefaultInstance()
            request.route.linesList.asFlow().map { item ->
                val outbound3 = quoteRouteRequest {
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
                routingRouteCalculationService.quoteRoute(outbound3)
            }.collect { reply3 = it }
            lintRoutesResponse {
                this.result = operationResult {
                    this.message = reply2.result.message
                    this.status = "ok"
                }
                this.route = route {
                    this.audit = auditInfo {
                        this.createdBy = reply1.featureFlag.audit.createdBy
                        this.updatedBy = reply0.stop.audit.updatedBy
                    }
                    this.billingAddress = postalAddress {
                        this.region = reply0.stop.location.region
                    }
                    this.issuedAt = request.route.issuedAt
                    this.routeId = request.routeId
                }
            }
        }

    /** Previews a route. */
    override suspend fun previewRoute(request: PreviewRouteRequest): PreviewRouteResponse {
        return handlePreviewRoute(request)
    }

    /** Scores a route. */
    override suspend fun scoreRoute(request: ScoreRouteRequest): ScoreRouteResponse = handleScoreRoute(request)

    /** Validates a route. */
    override suspend fun validateRoute(request: ValidateRouteRequest): ValidateRouteResponse =
        RpcBoundary.run("acme.routing.v1.RouteValidationService/ValidateRoute") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ValidateRoute", request)
            log.atInfo().addKeyValue("operation", "ValidateRoute").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.route.routeId).log("Recorded audit detail")
            val outbound0 = simulateStopRequest {
                this.context = request.context
                this.stop = stop {
                    this.participants.addAll(listOf(stopParticipant {
                        this.contact = contactInfo {
                            this.phone = request.details.contact.phone
                        }
                    }))
                }
            }
            val reply0 = ClientRegistry.lookup("acme.routing.v1.StopCalculationService", StopCalculationServiceGrpcKt.StopCalculationServiceCoroutineStub::class.java).simulateStop(outbound0)
            validateRouteResponse {
                this.outcome = ValidateRouteResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.route = route {
                    this.routeId = request.routeId
                    this.status = request.route.status
                }
            }
        }

    private fun CheckRouteEligibilityRequest.toPreviewPartFromCheckRouteEligibility0(): PreviewPartRequest {
        val source = this
        return previewPartRequest {
            this.context = source.context
            this.part = part {
                this.address = postalAddress {
                    this.postalCode = source.route.billingAddress.postalCode
                }
            }
        }
    }

    private suspend fun handleExplainRouteDecision(request: ExplainRouteDecisionRequest): ExplainRouteDecisionResponse =
        RpcBoundary.run("acme.routing.v1.RouteValidationService/ExplainRouteDecision") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainRouteDecision", request)
            log.atInfo().addKeyValue("operation", "ExplainRouteDecision").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.route.routeId).log("Recorded audit detail")
            val outbound0 = explainRouteCalculationRequest {
                this.context = request.context
                this.route = route {
                    this.audit = auditInfo {
                        this.etag = request.route.audit.etag
                    }
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = routingRouteCalculationService.explainRouteCalculation(envelope0.unpack(ExplainRouteCalculationRequest::class.java))
            explainRouteDecisionResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.route = route {
                    this.audit = auditInfo {
                        this.createdBy = reply0.route.audit.createdBy
                    }
                    this.routeId = request.routeId
                    this.status = request.route.status
                }
            }
        }

    private suspend fun handleGetRouteRules(request: GetRouteRulesRequest): GetRouteRulesResponse =
        RpcBoundary.run("acme.routing.v1.RouteValidationService/GetRouteRules") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetRouteRules", request)
            log.atInfo().addKeyValue("operation", "GetRouteRules").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            getRouteRulesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.route = route {
                    this.routeId = request.routeId
                }
            }
        }

    private data class LintRoutes1Command(
        val context: RequestContext,
        val featureFlagAuditCreatedAt: Timestamp
    ) {
        fun toRequest(): PreviewFeatureFlagRequest {
            val command = this
            return previewFeatureFlagRequest {
                this.context = command.context
                this.featureFlag = featureFlag {
                    this.audit = auditInfo {
                        this.createdAt = command.featureFlagAuditCreatedAt
                    }
                }
            }
        }
    }

    private suspend fun handlePreviewRoute(request: PreviewRouteRequest): PreviewRouteResponse =
        RpcBoundary.run("acme.routing.v1.RouteValidationService/PreviewRoute") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PreviewRoute", request)
            log.atInfo().addKeyValue("operation", "PreviewRoute").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.route.routeId).log("Recorded audit detail")
            val outbound0 = explainStopCalculationRequest {
                this.context = request.context
                this.stop = stop {
                    this.location = postalAddress {
                        this.region = request.route.billingAddress.region
                    }
                    this.organizer = userRef {
                        this.email = request.route.counterparty.email
                    }
                }
            }
            val reply0 = invokePreviewRoute0(outbound0)
            val outbound1 = calculateStopRequest {
                this.context = request.context
                this.stop = stop {
                    this.participants.addAll(listOf(stopParticipant {
                        this.contact = contactInfo {
                            this.email = request.route.counterparty.email
                            this.phone = request.details.contact.phone
                        }
                    }))
                }
            }
            val invoke1: suspend (CalculateStopRequest, Metadata) -> CalculateStopResponse = routingStopCalculationService::calculateStop
            val reply1 = invoke1(outbound1, Metadata())
            previewRouteResponse {
                this.outcome = PreviewRouteResponseKt.outcome {
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.message = reply0.outcome.message
                    this.status = "ok"
                }
                this.route = route {
                    this.billingAddress = postalAddress {
                        this.postalCode = reply1.stop.location.postalCode
                    }
                    this.dueAt = request.route.dueAt
                    this.routeId = request.routeId
                }
            }
        }

    private suspend fun handleScoreRoute(request: ScoreRouteRequest): ScoreRouteResponse =
        RpcBoundary.run("acme.routing.v1.RouteValidationService/ScoreRoute") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScoreRoute", request)
            log.atInfo().addKeyValue("operation", "ScoreRoute").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.route.number).log("Recorded audit detail")
            var reply0 = QuoteTripLogResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<RouteLine>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound0 = quoteTripLogRequest {
                            this.context = request.context
                            this.tripLog = tripLog {
                                this.lines.addAll(listOf(tripLogLine {
                                    this.tax = tripLogLineTax {
                                        this.amount = item.amount
                                        this.exemptions.addAll(item.tax.exemptionsList)
                                        this.jurisdiction = item.tax.jurisdiction
                                    }
                                }))
                            }
                        }
                        reply0 = telematicsTripLogCalculationService.quoteTripLog(outbound0)
                    }
                }
                try {
                    request.route.linesList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound1 = RequestFactory.create {
                listNotificationsRequest {
                    this.context = request.context
                    this.filter = ListNotificationsRequestKt.filter {
                        this.createdAfter = reply0.tripLog.issuedAt
                        this.labels.putAll(reply0.tripLog.metadataMap)
                        this.ownerId = request.route.routeId
                        this.region = request.details.destination.region
                    }
                }
            }
            val reply1 = notificationsNotificationService.listNotifications(outbound1)
            scoreRouteResponse {
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
                this.route = route {
                    this.routeId = request.routeId
                    this.status = request.route.status
                }
            }
        }
}
