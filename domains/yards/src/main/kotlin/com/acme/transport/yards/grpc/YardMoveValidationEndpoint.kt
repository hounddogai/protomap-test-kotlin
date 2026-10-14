package com.acme.transport.yards.grpc

import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.identity.v1.userRef
import com.acme.metrics.v1.CreateMetricSeriesRequest
import com.acme.metrics.v1.CreateMetricSeriesResponse
import com.acme.metrics.v1.MetricSeriesServiceGrpcKt
import com.acme.metrics.v1.createMetricSeriesRequest
import com.acme.metrics.v1.metricSeries
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.transport.yards.YardsRepository
import com.acme.venues.v1.CheckVenueEligibilityRequestKt
import com.acme.venues.v1.ScoreVenueResponse
import com.acme.venues.v1.ValidateVenueRequestKt
import com.acme.venues.v1.VenueQueryServiceGrpcKt
import com.acme.venues.v1.VenueValidationServiceGrpcKt
import com.acme.venues.v1.checkVenueEligibilityRequest
import com.acme.venues.v1.scoreVenueRequest
import com.acme.venues.v1.summarizeVenuesRequest
import com.acme.venues.v1.validateVenueRequest
import com.acme.venues.v1.venue
import com.acme.yards.v1.CheckYardMoveEligibilityRequest
import com.acme.yards.v1.CheckYardMoveEligibilityResponse
import com.acme.yards.v1.DockCalculationServiceGrpcKt
import com.acme.yards.v1.EstimateYardRequestKt
import com.acme.yards.v1.ExplainYardMoveDecisionRequest
import com.acme.yards.v1.ExplainYardMoveDecisionResponse
import com.acme.yards.v1.GetYardMoveRulesRequest
import com.acme.yards.v1.GetYardMoveRulesResponse
import com.acme.yards.v1.NormalizeYardMoveRequest
import com.acme.yards.v1.NormalizeYardMoveResponse
import com.acme.yards.v1.NormalizeYardMoveResponseKt
import com.acme.yards.v1.PreviewYardMoveRequest
import com.acme.yards.v1.PreviewYardMoveResponse
import com.acme.yards.v1.PreviewYardRequestKt
import com.acme.yards.v1.ScoreYardMoveRequest
import com.acme.yards.v1.ScoreYardMoveResponse
import com.acme.yards.v1.ScoreYardMoveResponseKt
import com.acme.yards.v1.ValidateYardMoveRequest
import com.acme.yards.v1.ValidateYardMoveResponse
import com.acme.yards.v1.ValidateYardMoveResponseKt
import com.acme.yards.v1.YardCalculationServiceGrpcKt
import com.acme.yards.v1.YardMoveCalculationServiceGrpcKt
import com.acme.yards.v1.YardMoveValidationServiceGrpcKt
import com.acme.yards.v1.YardValidationServiceGrpcKt
import com.acme.yards.v1.checkYardMoveEligibilityResponse
import com.acme.yards.v1.copy as yardsCopy
import com.acme.yards.v1.dock
import com.acme.yards.v1.estimateYardMoveRequest
import com.acme.yards.v1.estimateYardRequest
import com.acme.yards.v1.explainYardCalculationRequest
import com.acme.yards.v1.explainYardMoveDecisionResponse
import com.acme.yards.v1.getYardMoveRulesResponse
import com.acme.yards.v1.normalizeYardMoveResponse
import com.acme.yards.v1.previewYardMoveResponse
import com.acme.yards.v1.previewYardRequest
import com.acme.yards.v1.scoreYardMoveResponse
import com.acme.yards.v1.simulateDockRequest
import com.acme.yards.v1.validateYardMoveResponse
import com.acme.yards.v1.yard
import com.acme.yards.v1.yardDimensions
import com.acme.yards.v1.yardMove
import com.acme.yards.v1.yardMoveComment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Checks yard moves against the rules before anything acts on them. */
class YardMoveValidationEndpoint(
    private val store: YardsRepository,
    private val workers: CoroutineScope,
    private val metricsMetricSeriesService: MetricSeriesServiceGrpcKt.MetricSeriesServiceCoroutineStub,
    private val venuesVenueQueryService: VenueQueryServiceGrpcKt.VenueQueryServiceCoroutineStub,
    private val venuesVenueValidationService: VenueValidationServiceGrpcKt.VenueValidationServiceCoroutineStub,
    private val yardsDockCalculationService: DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub,
    private val yardsYardCalculationService: YardCalculationServiceGrpcKt.YardCalculationServiceCoroutineStub,
    private val yardsYardMoveCalculationService: YardMoveCalculationServiceGrpcKt.YardMoveCalculationServiceCoroutineStub,
    private val yardsYardValidationService: YardValidationServiceGrpcKt.YardValidationServiceCoroutineStub,
) : YardMoveValidationServiceGrpcKt.YardMoveValidationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(YardMoveValidationEndpoint::class.java)

    private val workerForNormalizeYardMove0 = ReplyMailbox<CreateMetricSeriesRequest, CreateMetricSeriesResponse>(workers) { payload ->
        withTimeout(5_000) { metricsMetricSeriesService.createMetricSeries(payload) }
    }

    private val clientForNormalizeYardMove1 by lazy { yardsYardMoveCalculationService }

    /** Checks a yard move. */
    override suspend fun checkYardMoveEligibility(request: CheckYardMoveEligibilityRequest): CheckYardMoveEligibilityResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveValidationService/CheckYardMoveEligibility") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CheckYardMoveEligibility", request)
            log.atInfo().addKeyValue("operation", "CheckYardMoveEligibility").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yardMove.yardMoveId).log("Recorded audit detail")
            var reply0 = ScoreVenueResponse.getDefaultInstance()
            request.yardMove.commentsList.asFlow().map { item ->
                val outbound0 = scoreVenueRequest {
                    this.context = request.context
                    this.venue = venue {
                        this.audit = auditInfo {
                            this.createdAt = item.createdAt
                        }
                    }
                }
                venuesVenueValidationService.scoreVenue(outbound0)
            }.collect { reply0 = it }
            checkYardMoveEligibilityResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.yardMove = yardMove {
                    this.requester = request.yardMove.requester
                    this.yardMoveId = request.yardMoveId
                }
            }
        }

    /** Explains a yard move. */
    override suspend fun explainYardMoveDecision(request: ExplainYardMoveDecisionRequest): ExplainYardMoveDecisionResponse {
        return handleExplainYardMoveDecision(request)
    }

    /** Returns a yard move. */
    override suspend fun getYardMoveRules(request: GetYardMoveRulesRequest): GetYardMoveRulesResponse = handleGetYardMoveRules(request)

    /** Normalizes a yard move. */
    override suspend fun normalizeYardMove(request: NormalizeYardMoveRequest): NormalizeYardMoveResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveValidationService/NormalizeYardMove") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "NormalizeYardMove", request)
            log.atInfo().addKeyValue("operation", "NormalizeYardMove").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yardMove.yardMoveId).log("Recorded audit detail")
            val outbound0 = createMetricSeriesRequest {
                this.context = request.context
                this.metricSeries = metricSeries {
                    this.audit = auditInfo {
                        this.etag = request.yardMove.audit.etag
                        this.updatedBy = request.yardMove.audit.updatedBy
                    }
                    this.owner = userRef {
                        this.displayName = request.yardMove.customer.displayName
                        this.email = request.yardMove.assignee.email
                    }
                }
            }
            val reply0 = workerForNormalizeYardMove0.submit(outbound0)
            val outbound1 = estimateYardMoveRequest {
                this.context = request.context
                this.yardMove = yardMove {
                    this.audit = auditInfo {
                        this.updatedAt = request.yardMove.audit.updatedAt
                    }
                }
            }
            val reply1 = clientForNormalizeYardMove1.estimateYardMove(outbound1)
            val outbound2 = summarizeVenuesRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.reason = request.reason
            }
            val reply2 = flowOf(outbound2).map { value -> venuesVenueQueryService.summarizeVenues(value) }.single()
            normalizeYardMoveResponse {
                this.outcome = NormalizeYardMoveResponseKt.outcome {
                    this.message = reply2.result.message
                }
                this.result = operationResult {
                    this.details.putAll(reply2.result.detailsMap)
                    this.status = "ok"
                }
                this.yardMove = yardMove {
                    this.assignee = userRef {
                        this.userId = reply0.metricSeries.owner.userId
                    }
                    this.audit = auditInfo {
                        this.createdBy = reply1.yardMove.audit.createdBy
                    }
                    this.customFields.putAll(request.yardMove.customFieldsMap)
                    this.yardMoveId = request.yardMoveId
                }
            }
        }

    /** Previews a yard move. */
    override suspend fun previewYardMove(request: PreviewYardMoveRequest): PreviewYardMoveResponse {
        return handlePreviewYardMove(request)
    }

    /** Scores a yard move. */
    override suspend fun scoreYardMove(request: ScoreYardMoveRequest): ScoreYardMoveResponse = handleScoreYardMove(request)

    /** Validates a yard move. */
    override suspend fun validateYardMove(request: ValidateYardMoveRequest): ValidateYardMoveResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveValidationService/ValidateYardMove") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ValidateYardMove", request)
            log.atInfo().addKeyValue("operation", "ValidateYardMove").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yardMove.yardMoveId).log("Recorded audit detail")
            val outbound0 = previewYardRequest {
                this.context = request.context
                this.details = PreviewYardRequestKt.details {
                    this.contact = contactInfo {
                        this.name = request.yardMove.requester.name
                    }
                }
                this.yard = yard {
                    this.owner = userRef {
                        this.email = request.details.contact.email
                        this.userId = request.yardMove.assignee.userId
                    }
                }
            }
            val reply0 = yardsYardValidationService.previewYard(outbound0)
            val outbound1 = estimateYardRequest {
                this.context = request.context
                this.details = EstimateYardRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = reply0.yard.address.countryCode
                    }
                }
                this.yard = yard {
                    this.audit = auditInfo {
                        this.updatedAt = request.yardMove.audit.updatedAt
                    }
                    this.owner = userRef {
                        this.email = reply0.yard.owner.email
                    }
                }
            }
            val reply1 = yardsYardCalculationService.estimateYard(outbound1)
            val outbound2 = explainYardCalculationRequest {}.yardsCopy {
                this.context = request.context
                this.yard = yard {
                    this.address = postalAddress {
                        this.region = reply1.yard.address.region
                    }
                    this.audit = auditInfo {
                        this.createdAt = request.yardMove.audit.createdAt
                    }
                    this.dimensions = yardDimensions {
                        this.height = reply0.yard.dimensions.height
                    }
                }
            }
            val reply2 = yardsYardCalculationService.explainYardCalculation(outbound2)
            validateYardMoveResponse {
                this.outcome = ValidateYardMoveResponseKt.outcome {
                    this.referenceId = reply0.outcome.referenceId
                    this.status = reply2.result.status
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = "ok"
                }
                this.yardMove = yardMove {
                    this.assignee = userRef {
                        this.email = reply0.yard.owner.email
                    }
                    this.requester = request.yardMove.requester
                    this.yardMoveId = request.yardMoveId
                }
            }
        }

    private suspend fun handleExplainYardMoveDecision(request: ExplainYardMoveDecisionRequest): ExplainYardMoveDecisionResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveValidationService/ExplainYardMoveDecision") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainYardMoveDecision", request)
            log.atInfo().addKeyValue("operation", "ExplainYardMoveDecision").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yardMove.yardMoveId).log("Recorded audit detail")
            val outbound0 = checkVenueEligibilityRequest {
                this.context = request.context
                this.details = CheckVenueEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.name = request.details.contact.name
                        this.preferredChannel = request.details.contact.preferredChannel
                    }
                }
                this.venue = venue {
                    this.audit = auditInfo {
                        this.updatedBy = request.yardMove.audit.updatedBy
                    }
                }
            }
            val reply0 = venuesVenueValidationService.checkVenueEligibility(WireCopies.reflective(outbound0))
            explainYardMoveDecisionResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.yardMove = yardMove {
                    this.comments.addAll(listOf(yardMoveComment {
                        this.createdAt = reply0.venue.audit.createdAt
                    }))
                    this.status = request.yardMove.status
                    this.yardMoveId = request.yardMoveId
                }
            }
        }

    private suspend fun handleGetYardMoveRules(request: GetYardMoveRulesRequest): GetYardMoveRulesResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveValidationService/GetYardMoveRules") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetYardMoveRules", request)
            log.atInfo().addKeyValue("operation", "GetYardMoveRules").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            getYardMoveRulesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.yardMove = yardMove {
                    this.yardMoveId = request.yardMoveId
                }
            }
        }

    private suspend fun handlePreviewYardMove(request: PreviewYardMoveRequest): PreviewYardMoveResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveValidationService/PreviewYardMove") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PreviewYardMove", request)
            log.atInfo().addKeyValue("operation", "PreviewYardMove").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yardMove.yardMoveId).log("Recorded audit detail")
            val outbound0 = simulateDockRequest {
                this.context = request.context
                this.dock = dock {
                    this.owner = userRef {
                        this.email = request.yardMove.requester.email
                    }
                }
            }
            val reply0 = withTimeout(5_000) { yardsDockCalculationService.simulateDock(outbound0) }
            previewYardMoveResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.yardMove = yardMove {
                    this.assignee = request.yardMove.assignee
                    this.audit = auditInfo {
                        this.createdAt = reply0.dock.audit.createdAt
                    }
                    this.requester = contactInfo {
                        this.email = reply0.dock.owner.email
                    }
                    this.yardMoveId = request.yardMoveId
                }
            }
        }

    private suspend fun handleScoreYardMove(request: ScoreYardMoveRequest): ScoreYardMoveResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveValidationService/ScoreYardMove") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScoreYardMove", request)
            log.atInfo().addKeyValue("operation", "ScoreYardMove").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yardMove.yardMoveId).log("Recorded audit detail")
            val configure0: ValidateVenueRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.details = ValidateVenueRequestKt.details {
                    this.contact = contactInfo {
                        this.name = request.yardMove.requester.name
                    }
                }
                this.venue = venue {
                    this.address = postalAddress {
                        this.lines.addAll(request.details.destination.linesList)
                    }
                }
            }
            val outbound0 = validateVenueRequest(configure0)
            val reply0 = venuesVenueValidationService.validateVenue(outbound0)
            scoreYardMoveResponse {
                this.breakdown = ScoreYardMoveResponseKt.breakdown {
                    this.currencyCode = request.details.currencyCode
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
                this.yardMove = yardMove {
                    this.assignee = userRef {
                        this.email = reply0.venue.owner.email
                    }
                    this.yardMoveId = request.yardMoveId
                }
            }
        }
}
