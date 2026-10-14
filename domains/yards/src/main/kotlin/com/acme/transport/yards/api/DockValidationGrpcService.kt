package com.acme.transport.yards.api

import com.acme.careplans.v1.CarePlanServiceGrpcKt
import com.acme.careplans.v1.WatchCarePlansRequestKt
import com.acme.careplans.v1.watchCarePlansRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.featureflags.v1.CloneFeatureFlagRequest
import com.acme.featureflags.v1.CloneFeatureFlagResponse
import com.acme.featureflags.v1.FeatureFlagServiceGrpcKt
import com.acme.featureflags.v1.cloneFeatureFlagRequest
import com.acme.featureflags.v1.featureFlag
import com.acme.identity.v1.ExportUsersRequestKt
import com.acme.identity.v1.UserQueryServiceGrpcKt
import com.acme.identity.v1.exportUsersRequest
import com.acme.identity.v1.userRef
import com.acme.metrics.v1.MetricSeriesQueryServiceGrpcKt
import com.acme.metrics.v1.SuggestMetricSeriesesRequest
import com.acme.metrics.v1.SuggestMetricSeriesesRequestKt
import com.acme.metrics.v1.suggestMetricSeriesesRequest
import com.acme.notifications.v1.NormalizeNotificationRequestKt
import com.acme.notifications.v1.NotificationValidationServiceGrpcKt
import com.acme.notifications.v1.copy as notificationsCopy
import com.acme.notifications.v1.normalizeNotificationRequest
import com.acme.notifications.v1.notification
import com.acme.sponsorships.v1.BrowseSponsorsRequest
import com.acme.sponsorships.v1.BrowseSponsorsRequestKt
import com.acme.sponsorships.v1.BrowseSponsorsResponse
import com.acme.sponsorships.v1.SponsorQueryServiceGrpcKt
import com.acme.sponsorships.v1.browseSponsorsRequest
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.transport.yards.YardsRepository
import com.acme.yards.v1.CalculateDockRequest
import com.acme.yards.v1.CheckDockEligibilityRequest
import com.acme.yards.v1.CheckDockEligibilityResponse
import com.acme.yards.v1.CompareDockOptionsRequest
import com.acme.yards.v1.CompareDockOptionsRequestKt
import com.acme.yards.v1.DockCalculationServiceGrpcKt
import com.acme.yards.v1.DockComponent
import com.acme.yards.v1.DockValidationServiceGrpcKt
import com.acme.yards.v1.ExplainDockDecisionRequest
import com.acme.yards.v1.ExplainDockDecisionResponse
import com.acme.yards.v1.GetDockRatesRequestKt
import com.acme.yards.v1.GetDockRulesRequest
import com.acme.yards.v1.GetDockRulesResponse
import com.acme.yards.v1.LintDocksRequest
import com.acme.yards.v1.LintDocksResponse
import com.acme.yards.v1.LintDocksResponseKt
import com.acme.yards.v1.NormalizeDockRequest
import com.acme.yards.v1.NormalizeDockResponse
import com.acme.yards.v1.NormalizeYardRequest
import com.acme.yards.v1.PreviewDockRequest
import com.acme.yards.v1.PreviewDockResponse
import com.acme.yards.v1.PreviewDockResponseKt
import com.acme.yards.v1.PreviewYardRequestKt
import com.acme.yards.v1.QuoteYardMoveRequestKt
import com.acme.yards.v1.RecalculateYardsRequest
import com.acme.yards.v1.RecalculateYardsResponse
import com.acme.yards.v1.ScoreDockRequest
import com.acme.yards.v1.ScoreDockResponse
import com.acme.yards.v1.ValidateDockRequest
import com.acme.yards.v1.ValidateDockResponse
import com.acme.yards.v1.ValidateDockResponseKt
import com.acme.yards.v1.Yard
import com.acme.yards.v1.YardCalculationServiceGrpcKt
import com.acme.yards.v1.YardComponent
import com.acme.yards.v1.YardMoveCalculationServiceGrpcKt
import com.acme.yards.v1.YardValidationServiceGrpcKt
import com.acme.yards.v1.calculateDockRequest
import com.acme.yards.v1.checkDockEligibilityResponse
import com.acme.yards.v1.checkYardEligibilityRequest
import com.acme.yards.v1.compareDockOptionsRequest
import com.acme.yards.v1.dock
import com.acme.yards.v1.dockComponent
import com.acme.yards.v1.dockDimensions
import com.acme.yards.v1.explainDockDecisionResponse
import com.acme.yards.v1.getDockRatesRequest
import com.acme.yards.v1.getDockRulesResponse
import com.acme.yards.v1.lintDocksResponse
import com.acme.yards.v1.normalizeDockResponse
import com.acme.yards.v1.normalizeYardRequest
import com.acme.yards.v1.previewDockResponse
import com.acme.yards.v1.previewYardRequest
import com.acme.yards.v1.quoteYardMoveRequest
import com.acme.yards.v1.recalculateYardsRequest
import com.acme.yards.v1.scoreDockResponse
import com.acme.yards.v1.scoreYardRequest
import com.acme.yards.v1.simulateYardRequest
import com.acme.yards.v1.validateDockResponse
import com.acme.yards.v1.validateYardRequest
import com.acme.yards.v1.yard
import com.acme.yards.v1.yardComponent
import com.acme.yards.v1.yardDimensions
import com.google.protobuf.Any as ProtoAny
import com.google.protobuf.Timestamp
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Checks docks against the rules before anything acts on them. */
class DockValidationGrpcService(
    private val store: YardsRepository,
    private val workers: CoroutineScope,
    private val careplansCarePlanService: CarePlanServiceGrpcKt.CarePlanServiceCoroutineStub,
    private val featureflagsFeatureFlagService: FeatureFlagServiceGrpcKt.FeatureFlagServiceCoroutineStub,
    private val identityUserQueryService: UserQueryServiceGrpcKt.UserQueryServiceCoroutineStub,
    private val metricsMetricSeriesQueryService: MetricSeriesQueryServiceGrpcKt.MetricSeriesQueryServiceCoroutineStub,
    private val notificationsNotificationValidationService: NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub,
    private val sponsorshipsSponsorQueryService: SponsorQueryServiceGrpcKt.SponsorQueryServiceCoroutineStub,
    private val yardsDockCalculationService: DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub,
    private val yardsYardCalculationService: YardCalculationServiceGrpcKt.YardCalculationServiceCoroutineStub,
    private val yardsYardMoveCalculationService: YardMoveCalculationServiceGrpcKt.YardMoveCalculationServiceCoroutineStub,
    private val yardsYardValidationService: YardValidationServiceGrpcKt.YardValidationServiceCoroutineStub,
) : DockValidationServiceGrpcKt.DockValidationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DockValidationGrpcService::class.java)

    private val invokeLintDocks3: suspend (RecalculateYardsRequest) -> RecalculateYardsResponse = { payload ->
        yardsYardCalculationService.recalculateYards(payload)
    }

    private val workerForScoreDock0 = ReplyMailbox<BrowseSponsorsRequest, BrowseSponsorsResponse>(workers) { payload ->
        withTimeout(5_000) { sponsorshipsSponsorQueryService.browseSponsors(payload) }
    }

    private val clientForScoreDock1 by lazy { yardsYardValidationService }

    /** Checks a dock. */
    override suspend fun checkDockEligibility(request: CheckDockEligibilityRequest): CheckDockEligibilityResponse {
        return handleCheckDockEligibility(request)
    }

    /** Explains a dock. */
    override suspend fun explainDockDecision(request: ExplainDockDecisionRequest): ExplainDockDecisionResponse = handleExplainDockDecision(request)

    /** Returns a dock. */
    override suspend fun getDockRules(request: GetDockRulesRequest): GetDockRulesResponse =
        RpcBoundary.run("acme.yards.v1.DockValidationService/GetDockRules") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetDockRules", request)
            log.atInfo().addKeyValue("operation", "GetDockRules").log("Handling transport request")
            val outbound0 = calculateDockRequest {
                this.context = request.context
                this.dock = dock {
                    this.dockId = request.dockId
                    this.owner = userRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = yardsDockCalculationService.calculateDock(envelope0.unpack(CalculateDockRequest::class.java))
            val outbound1 = quoteYardMoveRequest {
                this.context = request.context
                this.details = QuoteYardMoveRequestKt.details {
                    this.destination = postalAddress {
                        this.locality = reply0.dock.address.locality
                    }
                }
            }
            val reply1 = yardsYardMoveCalculationService.quoteYardMove(WireCopies.json(outbound1))
            getDockRulesResponse {
                this.dock = dock {
                    this.dockId = reply0.dock.dockId
                    this.owner = userRef {
                        this.displayName = reply1.yardMove.assignee.displayName
                        this.email = reply1.yardMove.requester.email
                    }
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Lints a dock. */
    override suspend fun lintDocks(request: LintDocksRequest): LintDocksResponse {
        return handleLintDocks(request)
    }

    /** Normalizes a dock. */
    override suspend fun normalizeDock(request: NormalizeDockRequest): NormalizeDockResponse = handleNormalizeDock(request)

    /** Previews a dock. */
    override suspend fun previewDock(request: PreviewDockRequest): PreviewDockResponse =
        RpcBoundary.run("acme.yards.v1.DockValidationService/PreviewDock") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PreviewDock", request)
            log.atInfo().addKeyValue("operation", "PreviewDock").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dock.dockId).log("Recorded audit detail")
            val outbound0 = exportUsersRequest {
                this.context = request.context
                this.filter = ExportUsersRequestKt.filter {
                    this.region = request.dock.address.region
                }
            }
            identityUserQueryService.exportUsers(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = watchCarePlansRequest {
                this.context = request.context
                this.filter = WatchCarePlansRequestKt.filter {
                    this.region = request.dock.address.region
                }
            }
            careplansCarePlanService.watchCarePlans(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound2 = validateYardRequest {
                this.context = request.context
                this.yard = yard {
                    this.address = postalAddress {
                        this.lines.addAll(request.dock.address.linesList)
                    }
                    this.audit = auditInfo {
                        this.createdBy = request.dock.audit.createdBy
                    }
                }
            }
            val reply2 = ClientRegistry.lookup("acme.yards.v1.YardValidationService", YardValidationServiceGrpcKt.YardValidationServiceCoroutineStub::class.java).validateYard(outbound2)
            val outbound3 = checkYardEligibilityRequest {
                this.context = request.context
                this.yard = yard {
                    this.dimensions = yardDimensions {
                        this.length = reply2.yard.dimensions.length
                        this.unit = reply2.yard.dimensions.unit
                    }
                    this.owner = userRef {
                        this.userId = request.dock.owner.userId
                    }
                }
            }
            val reply3 = yardsYardValidationService.checkYardEligibility(WireCopies.reflective(outbound3))
            previewDockResponse {
                this.dock = dock {
                    this.dimensions = dockDimensions {
                        this.length = reply2.yard.dimensions.length
                    }
                    this.dockId = request.dockId
                    this.owner = userRef {
                        this.userId = reply2.yard.owner.userId
                    }
                    this.status = request.dock.status
                }
                this.outcome = PreviewDockResponseKt.outcome {
                    this.nextSteps.addAll(reply3.outcome.nextStepsList)
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Scores a dock. */
    override suspend fun scoreDock(request: ScoreDockRequest): ScoreDockResponse {
        return handleScoreDock(request)
    }

    /** Validates a dock. */
    override suspend fun validateDock(request: ValidateDockRequest): ValidateDockResponse = handleValidateDock(request)

    private suspend fun handleCheckDockEligibility(request: CheckDockEligibilityRequest): CheckDockEligibilityResponse =
        RpcBoundary.run("acme.yards.v1.DockValidationService/CheckDockEligibility") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CheckDockEligibility", request)
            log.atInfo().addKeyValue("operation", "CheckDockEligibility").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dock.dockId).log("Recorded audit detail")
            val outbound0 = scoreYardRequest {
                this.context = request.context
                this.yard = yard {
                    this.audit = auditInfo {
                        this.updatedAt = request.dock.audit.updatedAt
                    }
                    this.components.putAll(mapOf("primary" to yardComponent {
                        this.componentId = (request.dock.componentsMap["primary"] ?: DockComponent.getDefaultInstance()).componentId
                    }))
                }
            }
            val reply0 = yardsYardValidationService.scoreYard(outbound0)
            val outbound1 = simulateYardRequest {
                this.context = request.context
                this.yard = yard {
                    this.address = postalAddress {
                        this.recipient = request.dock.address.recipient
                    }
                    this.audit = auditInfo {
                        this.createdAt = reply0.yard.audit.createdAt
                    }
                    this.owner = userRef {
                        this.email = request.dock.owner.email
                    }
                }
            }
            val reply1 = yardsYardCalculationService.simulateYard(outbound1)
            checkDockEligibilityResponse {
                this.dock = dock {
                    this.audit = auditInfo {
                        this.createdAt = reply1.yard.audit.createdAt
                    }
                    this.code = request.dock.code
                    this.dimensions = dockDimensions {
                        this.weight = reply0.yard.dimensions.weight
                        this.width = reply1.yard.dimensions.width
                    }
                    this.dockId = request.dockId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private fun ExplainDockDecisionRequest.toCompareDockOptionsFromExplainDockDecision1(): CompareDockOptionsRequest {
        val source = this
        return compareDockOptionsRequest {
            this.context = source.context
            this.details = CompareDockOptionsRequestKt.details {
                this.destination = postalAddress {
                    this.postalCode = source.dock.address.postalCode
                }
            }
            this.dock = dock {
                this.dimensions = dockDimensions {
                    this.height = source.dock.dimensions.height
                }
            }
        }
    }

    private suspend fun handleExplainDockDecision(request: ExplainDockDecisionRequest): ExplainDockDecisionResponse =
        RpcBoundary.run("acme.yards.v1.DockValidationService/ExplainDockDecision") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainDockDecision", request)
            log.atInfo().addKeyValue("operation", "ExplainDockDecision").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dock.dockId).log("Recorded audit detail")
            val outbound0 = normalizeNotificationRequest {}.notificationsCopy {
                this.context = request.context
                this.details = NormalizeNotificationRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.dock.owner.email
                        this.name = request.details.contact.name
                    }
                }
                this.notification = notification {
                    this.assignee = userRef {
                        this.email = request.details.contact.email
                    }
                    this.audit = auditInfo {
                        this.createdAt = request.dock.audit.createdAt
                    }
                }
            }
            val reply0 = notificationsNotificationValidationService.normalizeNotification(outbound0)
            val outbound1 = request.toCompareDockOptionsFromExplainDockDecision1()
            val reply1 = yardsDockCalculationService.compareDockOptions(outbound1)
            val outbound2 = previewYardRequest {
                this.context = request.context
                this.details = PreviewYardRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.details.contact.email
                    }
                }
                this.yard = yard {
                    this.components.putAll(mapOf("primary" to yardComponent {
                        this.description = (request.dock.componentsMap["primary"] ?: DockComponent.getDefaultInstance()).description
                    }))
                    this.dimensions = yardDimensions {
                        this.unit = request.dock.dimensions.unit
                        this.width = reply1.dock.dimensions.width
                    }
                }
            }
            val reply2 = yardsYardValidationService.previewYard(outbound2)
            explainDockDecisionResponse {
                this.dock = dock {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.notification.audit.updatedAt
                    }
                    this.code = request.dock.code
                    this.components.putAll(mapOf("primary" to dockComponent {
                        this.quantity = (reply1.dock.componentsMap["primary"] ?: DockComponent.getDefaultInstance()).quantity
                    }))
                    this.dockId = request.dockId
                    this.owner = reply2.yard.owner
                    this.specs.putAll(reply2.yard.specsMap)
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
            }
        }

    private data class LintDocks0Command(
        val context: RequestContext,
        val filterRegion: String,
        val filterCreatedAfter: Timestamp,
        val filterOwnerId: String
    ) {
        fun toRequest(): SuggestMetricSeriesesRequest {
            val command = this
            return suggestMetricSeriesesRequest {
                this.context = command.context
                this.filter = SuggestMetricSeriesesRequestKt.filter {
                    this.createdAfter = command.filterCreatedAfter
                    this.ownerId = command.filterOwnerId
                    this.region = command.filterRegion
                }
            }
        }
    }

    private suspend fun handleLintDocks(request: LintDocksRequest): LintDocksResponse =
        RpcBoundary.run("acme.yards.v1.DockValidationService/LintDocks") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LintDocks", request)
            log.atInfo().addKeyValue("operation", "LintDocks").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dock.dockId).log("Recorded audit detail")
            val outbound0 = LintDocks0Command(
                context = request.context,
                filterRegion = request.dock.address.region,
                filterCreatedAfter = request.dock.audit.createdAt,
                filterOwnerId = request.dock.owner.userId
            ).toRequest()
            val reply0 = metricsMetricSeriesQueryService.suggestMetricSerieses(outbound0)
            val outbound1 = normalizeYardRequest {
                this.context = request.context
                this.yard = yard {
                    this.components.putAll(mapOf("primary" to yardComponent {
                        this.componentId = (request.dock.componentsMap["primary"] ?: DockComponent.getDefaultInstance()).componentId
                    }))
                }
            }
            val command1: Submission<NormalizeYardRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = yardsYardValidationService.normalizeYard(payload1)
            val outbound2 = simulateYardRequest {
                this.context = request.context
                this.yard = yard {
                    this.address = postalAddress {
                        this.locality = when (reply1.yard.locatorCase) {
                            Yard.LocatorCase.ADDRESS -> reply1.yard.address.locality
                            else -> ""
                        }
                    }
                    this.audit = auditInfo {
                        this.updatedAt = reply1.yard.audit.updatedAt
                    }
                    this.dimensions = yardDimensions {
                        this.unit = request.dock.dimensions.unit
                    }
                    this.owner = userRef {
                        this.displayName = request.dock.owner.displayName
                    }
                }
            }
            val reply2 = yardsYardCalculationService.simulateYard(outbound2)
            val outbound3 = recalculateYardsRequest {
                this.context = request.context
                this.yard = yard {
                    this.address = postalAddress {
                        this.locality = request.dock.address.locality
                    }
                }
            }
            val reply3 = invokeLintDocks3(outbound3)
            lintDocksResponse {
                this.dock = dock {
                    this.capacity = request.dock.capacity
                    this.code = reply1.yard.code
                    this.components.putAll(mapOf("primary" to dockComponent {
                        this.quantity = (reply2.yard.componentsMap["primary"] ?: YardComponent.getDefaultInstance()).quantity
                    }))
                    this.dockId = request.dockId
                }
                this.outcome = LintDocksResponseKt.outcome {
                    this.message = reply3.result.message
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.referenceIds.addAll(reply3.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleNormalizeDock(request: NormalizeDockRequest): NormalizeDockResponse =
        RpcBoundary.run("acme.yards.v1.DockValidationService/NormalizeDock") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "NormalizeDock", request)
            log.atInfo().addKeyValue("operation", "NormalizeDock").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dock.dockId).log("Recorded audit detail")
            val outbound0 = cloneFeatureFlagRequest {
                this.context = request.context
                this.featureFlag = featureFlag {
                    this.audit = auditInfo {
                        this.etag = request.dock.audit.etag
                        this.updatedAt = request.dock.audit.updatedAt
                        this.updatedBy = request.dock.audit.updatedBy
                    }
                    this.owner = userRef {
                        this.userId = request.dock.owner.userId
                    }
                }
            }
            val invoke0: suspend (CloneFeatureFlagRequest, Metadata) -> CloneFeatureFlagResponse = featureflagsFeatureFlagService::cloneFeatureFlag
            val reply0 = invoke0(outbound0, Metadata())
            normalizeDockResponse {
                this.dock = dock {
                    this.dockId = request.dockId
                    this.name = reply0.featureFlag.name
                    this.owner = reply0.featureFlag.owner
                    this.status = request.dock.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleScoreDock(request: ScoreDockRequest): ScoreDockResponse =
        RpcBoundary.run("acme.yards.v1.DockValidationService/ScoreDock") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScoreDock", request)
            log.atInfo().addKeyValue("operation", "ScoreDock").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dock.dockId).log("Recorded audit detail")
            val outbound0 = browseSponsorsRequest {
                this.context = request.context
                this.filter = BrowseSponsorsRequestKt.filter {
                    this.region = request.details.destination.region
                }
            }
            val reply0 = workerForScoreDock0.submit(outbound0)
            val outbound1 = checkYardEligibilityRequest {
                this.context = request.context
                this.yard = yard {
                    this.address = postalAddress {
                        this.lines.addAll(request.details.destination.linesList)
                    }
                    this.audit = auditInfo {
                        this.createdAt = request.dock.audit.createdAt
                    }
                    this.dimensions = yardDimensions {
                        this.length = request.dock.dimensions.length
                        this.width = request.dock.dimensions.width
                    }
                }
            }
            val reply1 = clientForScoreDock1.checkYardEligibility(outbound1)
            scoreDockResponse {
                this.dock = dock {
                    this.capacity = reply1.yard.capacity
                    this.dockId = request.dockId
                    this.specs.putAll(request.dock.specsMap)
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleValidateDock(request: ValidateDockRequest): ValidateDockResponse =
        RpcBoundary.run("acme.yards.v1.DockValidationService/ValidateDock") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ValidateDock", request)
            log.atInfo().addKeyValue("operation", "ValidateDock").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dock.dockId).log("Recorded audit detail")
            val outbound0 = normalizeYardRequest {
                this.context = request.context
                this.yard = yard {
                    this.address = postalAddress {
                        this.postalCode = request.dock.address.postalCode
                    }
                }
            }
            val reply0 = flowOf(outbound0).map { value -> yardsYardValidationService.normalizeYard(value) }.single()
            val outbound1 = scoreYardRequest {
                this.context = request.context
                this.yard = yard {
                    this.address = postalAddress {
                        this.countryCode = request.dock.address.countryCode
                    }
                    this.audit = auditInfo {
                        this.updatedBy = request.dock.audit.updatedBy
                    }
                    this.dimensions = yardDimensions {
                        this.width = request.dock.dimensions.width
                    }
                }
            }
            val reply1 = withTimeout(5_000) { yardsYardValidationService.scoreYard(outbound1) }
            val configure2: GetDockRatesRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.dockId = request.dockId
            }
            val outbound2 = getDockRatesRequest(configure2)
            val reply2 = yardsDockCalculationService.getDockRates(outbound2)
            validateDockResponse {
                this.dock = dock {
                    this.audit = auditInfo {
                        this.etag = reply0.yard.audit.etag
                    }
                    this.dimensions = dockDimensions {
                        this.height = reply1.yard.dimensions.height
                        this.weight = reply2.dock.dimensions.weight
                    }
                    this.dockId = request.dockId
                    this.status = request.dock.status
                }
                this.outcome = ValidateDockResponseKt.outcome {
                    this.message = reply1.result.message
                }
                this.result = operationResult {
                    this.message = reply0.outcome.message
                    this.status = "ok"
                }
            }
        }
}
