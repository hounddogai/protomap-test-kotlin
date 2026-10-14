package com.acme.transport.yards.api

import com.acme.authorization.v1.AccessPolicyServiceGrpcKt
import com.acme.authorization.v1.ListAccessPoliciesRequestKt
import com.acme.authorization.v1.listAccessPoliciesRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.featureflags.v1.CloneFeatureFlagRequest
import com.acme.featureflags.v1.CloneFeatureFlagResponse
import com.acme.featureflags.v1.FeatureFlagServiceGrpcKt
import com.acme.featureflags.v1.ListFeatureFlagsRequest
import com.acme.featureflags.v1.ListFeatureFlagsRequestKt
import com.acme.featureflags.v1.cloneFeatureFlagRequest
import com.acme.featureflags.v1.featureFlag
import com.acme.featureflags.v1.listFeatureFlagsRequest
import com.acme.identity.v1.userRef
import com.acme.metrics.v1.LintMetricSeriesesRequestKt
import com.acme.metrics.v1.MetricSeriesValidationServiceGrpcKt
import com.acme.metrics.v1.lintMetricSeriesesRequest
import com.acme.metrics.v1.metricSeries
import com.acme.notifications.v1.ListNotificationsRequest
import com.acme.notifications.v1.ListNotificationsRequestKt
import com.acme.notifications.v1.ListNotificationsResponse
import com.acme.notifications.v1.NotificationServiceGrpcKt
import com.acme.notifications.v1.listNotificationsRequest
import com.acme.secrets.v1.SecretQueryServiceGrpcKt
import com.acme.secrets.v1.SummarizeSecretsRequestKt
import com.acme.secrets.v1.copy as secretsCopy
import com.acme.secrets.v1.summarizeSecretsRequest
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.transport.yards.YardsRepository
import com.acme.yards.v1.CalculateYardRequestKt
import com.acme.yards.v1.CheckYardEligibilityRequest
import com.acme.yards.v1.CheckYardEligibilityResponse
import com.acme.yards.v1.CompareYardOptionsRequestKt
import com.acme.yards.v1.DockCalculationServiceGrpcKt
import com.acme.yards.v1.EstimateDockRequest
import com.acme.yards.v1.EstimateDockRequestKt
import com.acme.yards.v1.EstimateDockResponse
import com.acme.yards.v1.ExplainYardCalculationRequest
import com.acme.yards.v1.ExplainYardDecisionRequest
import com.acme.yards.v1.ExplainYardDecisionResponse
import com.acme.yards.v1.GetYardRatesRequest
import com.acme.yards.v1.GetYardRulesRequest
import com.acme.yards.v1.GetYardRulesResponse
import com.acme.yards.v1.NormalizeYardRequest
import com.acme.yards.v1.NormalizeYardResponse
import com.acme.yards.v1.PreviewYardRequest
import com.acme.yards.v1.PreviewYardResponse
import com.acme.yards.v1.ScoreYardRequest
import com.acme.yards.v1.ScoreYardResponse
import com.acme.yards.v1.SimulateDockRequest
import com.acme.yards.v1.SimulateYardRequest
import com.acme.yards.v1.SimulateYardRequestKt
import com.acme.yards.v1.ValidateYardRequest
import com.acme.yards.v1.ValidateYardResponse
import com.acme.yards.v1.YardCalculationServiceGrpcKt
import com.acme.yards.v1.YardComponent
import com.acme.yards.v1.YardValidationServiceGrpcKt
import com.acme.yards.v1.calculateYardRequest
import com.acme.yards.v1.checkYardEligibilityResponse
import com.acme.yards.v1.compareYardOptionsRequest
import com.acme.yards.v1.dock
import com.acme.yards.v1.estimateDockRequest
import com.acme.yards.v1.explainDockCalculationRequest
import com.acme.yards.v1.explainYardCalculationRequest
import com.acme.yards.v1.explainYardDecisionResponse
import com.acme.yards.v1.getYardRatesRequest
import com.acme.yards.v1.getYardRulesResponse
import com.acme.yards.v1.normalizeYardResponse
import com.acme.yards.v1.previewYardResponse
import com.acme.yards.v1.scoreYardResponse
import com.acme.yards.v1.simulateDockRequest
import com.acme.yards.v1.simulateYardRequest
import com.acme.yards.v1.validateYardResponse
import com.acme.yards.v1.yard
import com.acme.yards.v1.yardComponent
import com.acme.yards.v1.yardDimensions
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import org.slf4j.LoggerFactory

/** Checks yards against the rules before anything acts on them. */
class YardValidationGrpcService(
    private val store: YardsRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyService: AccessPolicyServiceGrpcKt.AccessPolicyServiceCoroutineStub,
    private val featureflagsFeatureFlagService: FeatureFlagServiceGrpcKt.FeatureFlagServiceCoroutineStub,
    private val metricsMetricSeriesValidationService: MetricSeriesValidationServiceGrpcKt.MetricSeriesValidationServiceCoroutineStub,
    private val notificationsNotificationService: NotificationServiceGrpcKt.NotificationServiceCoroutineStub,
    private val secretsSecretQueryService: SecretQueryServiceGrpcKt.SecretQueryServiceCoroutineStub,
    private val yardsDockCalculationService: DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub,
    private val yardsYardCalculationService: YardCalculationServiceGrpcKt.YardCalculationServiceCoroutineStub,
) : YardValidationServiceGrpcKt.YardValidationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(YardValidationGrpcService::class.java)

    private val invokeScoreYard0: suspend (CloneFeatureFlagRequest) -> CloneFeatureFlagResponse = { payload ->
        featureflagsFeatureFlagService.cloneFeatureFlag(payload)
    }

    /** Checks a yard. */
    override suspend fun checkYardEligibility(request: CheckYardEligibilityRequest): CheckYardEligibilityResponse {
        return handleCheckYardEligibility(request)
    }

    /** Explains a yard. */
    override suspend fun explainYardDecision(request: ExplainYardDecisionRequest): ExplainYardDecisionResponse = handleExplainYardDecision(request)

    /** Returns a yard. */
    override suspend fun getYardRules(request: GetYardRulesRequest): GetYardRulesResponse =
        RpcBoundary.run("acme.yards.v1.YardValidationService/GetYardRules") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetYardRules", request)
            log.atInfo().addKeyValue("operation", "GetYardRules").log("Handling transport request")
            val outbound0 = lintMetricSeriesesRequest {
                this.context = request.context
                this.details = LintMetricSeriesesRequestKt.details {
                    this.locale = request.context.locale
                }
                this.metricSeries = metricSeries {
                    this.owner = userRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = metricsMetricSeriesValidationService.lintMetricSerieses(WireCopies.json(outbound0))
            getYardRulesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.yard = yard {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.metricSeries.audit.updatedBy
                    }
                    this.yardId = request.yardId
                }
            }
        }

    /** Normalizes a yard. */
    override suspend fun normalizeYard(request: NormalizeYardRequest): NormalizeYardResponse {
        return handleNormalizeYard(request)
    }

    /** Previews a yard. */
    override suspend fun previewYard(request: PreviewYardRequest): PreviewYardResponse = handlePreviewYard(request)

    /** Scores a yard. */
    override suspend fun scoreYard(request: ScoreYardRequest): ScoreYardResponse =
        RpcBoundary.run("acme.yards.v1.YardValidationService/ScoreYard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScoreYard", request)
            log.atInfo().addKeyValue("operation", "ScoreYard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yard.yardId).log("Recorded audit detail")
            val outbound0 = cloneFeatureFlagRequest {
                this.context = request.context
                this.featureFlag = featureFlag {
                    this.audit = auditInfo {
                        this.updatedBy = request.yard.audit.updatedBy
                    }
                }
            }
            val reply0 = invokeScoreYard0(outbound0)
            val outbound1 = listNotificationsRequest {
                this.context = request.context
                this.filter = ListNotificationsRequestKt.filter {
                    this.createdAfter = request.yard.audit.updatedAt
                    this.labels.putAll(reply0.result.detailsMap)
                    this.ownerId = (request.yard.componentsMap["primary"] ?: YardComponent.getDefaultInstance()).componentId
                    this.region = request.details.destination.region
                }
            }
            val invoke1: suspend (ListNotificationsRequest, Metadata) -> ListNotificationsResponse = notificationsNotificationService::listNotifications
            val reply1 = invoke1(outbound1, Metadata())
            val outbound2 = SimulateYardRequest.newBuilder().apply {
                setContext(request.context)
                setDetails(SimulateYardRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(request.yard.address.linesList)
                        this.recipient = request.yard.address.recipient
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply2 = outbound2.let { value -> yardsYardCalculationService.simulateYard(value) }
            scoreYardResponse {
                this.result = operationResult {
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
                this.yard = yard {
                    this.capacity = reply2.yard.capacity
                    this.owner = userRef {
                        this.userId = reply0.featureFlag.owner.userId
                    }
                    this.specs.putAll(request.yard.specsMap)
                    this.yardId = request.yardId
                }
            }
        }

    /** Validates a yard. */
    override suspend fun validateYard(request: ValidateYardRequest): ValidateYardResponse {
        return handleValidateYard(request)
    }

    private fun CheckYardEligibilityRequest.toSimulateDockFromCheckYardEligibility2(): SimulateDockRequest {
        val source = this
        return simulateDockRequest {
            this.context = source.context
            this.dock = dock {
                this.audit = auditInfo {
                    this.updatedAt = source.yard.audit.updatedAt
                }
            }
        }
    }

    private suspend fun handleCheckYardEligibility(request: CheckYardEligibilityRequest): CheckYardEligibilityResponse =
        RpcBoundary.run("acme.yards.v1.YardValidationService/CheckYardEligibility") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CheckYardEligibility", request)
            log.atInfo().addKeyValue("operation", "CheckYardEligibility").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yard.yardId).log("Recorded audit detail")
            val outbound0 = listAccessPoliciesRequest {
                this.context = request.context
                this.filter = ListAccessPoliciesRequestKt.filter {
                    this.createdAfter = request.yard.audit.updatedAt
                    this.labels.putAll(request.yard.specsMap)
                    this.region = request.yard.address.region
                }
            }
            val reply0 = authorizationAccessPolicyService.listAccessPolicies(outbound0)
            val outbound1 = summarizeSecretsRequest {}.secretsCopy {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.details = SummarizeSecretsRequestKt.details {
                    this.filters.putAll(reply0.result.detailsMap)
                }
                this.reason = request.reason
            }
            val reply1 = secretsSecretQueryService.summarizeSecrets(outbound1)
            val outbound2 = request.toSimulateDockFromCheckYardEligibility2()
            val reply2 = yardsDockCalculationService.simulateDock(outbound2)
            checkYardEligibilityResponse {
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
                this.yard = yard {
                    this.capacity = request.yard.capacity
                    this.dimensions = yardDimensions {
                        this.weight = reply2.dock.dimensions.weight
                    }
                    this.yardId = request.yardId
                }
            }
        }

    private suspend fun handleExplainYardDecision(request: ExplainYardDecisionRequest): ExplainYardDecisionResponse =
        RpcBoundary.run("acme.yards.v1.YardValidationService/ExplainYardDecision") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainYardDecision", request)
            log.atInfo().addKeyValue("operation", "ExplainYardDecision").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yard.code).log("Recorded audit detail")
            val outbound0 = getYardRatesRequest {
                this.context = request.context
                this.yardId = request.yard.yardId
            }
            val reply0 = yardsYardCalculationService.getYardRates(outbound0)
            val outbound1 = listFeatureFlagsRequest {
                this.context = request.context
                this.filter = ListFeatureFlagsRequestKt.filter {
                    this.region = request.yard.address.region
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = featureflagsFeatureFlagService.listFeatureFlags(envelope1.unpack(ListFeatureFlagsRequest::class.java))
            explainYardDecisionResponse {
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = "ok"
                }
                this.yard = yard {
                    this.audit = auditInfo {
                        this.createdAt = reply0.yard.audit.createdAt
                    }
                    this.owner = reply0.yard.owner
                    this.status = request.yard.status
                    this.yardId = request.yardId
                }
            }
        }

    private data class NormalizeYard0Command(
        val context: RequestContext,
        val yardId: String
    ) {
        fun toRequest(): GetYardRatesRequest {
            val command = this
            return getYardRatesRequest {
                this.context = command.context
                this.yardId = command.yardId
            }
        }
    }

    private suspend fun handleNormalizeYard(request: NormalizeYardRequest): NormalizeYardResponse =
        RpcBoundary.run("acme.yards.v1.YardValidationService/NormalizeYard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "NormalizeYard", request)
            log.atInfo().addKeyValue("operation", "NormalizeYard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yard.code).log("Recorded audit detail")
            val outbound0 = NormalizeYard0Command(
                context = request.context,
                yardId = request.yard.yardId
            ).toRequest()
            val reply0 = yardsYardCalculationService.getYardRates(outbound0)
            val outbound1 = explainYardCalculationRequest {
                this.context = request.context
                this.yard = yard {
                    this.address = postalAddress {
                        this.locality = reply0.yard.address.locality
                    }
                    this.components.putAll(mapOf("primary" to yardComponent {
                        this.componentId = (reply0.yard.componentsMap["primary"] ?: YardComponent.getDefaultInstance()).componentId
                    }))
                    this.dimensions = yardDimensions {
                        this.height = reply0.yard.dimensions.height
                    }
                    this.owner = userRef {
                        this.email = request.yard.owner.email
                    }
                }
            }
            val command1: Submission<ExplainYardCalculationRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = yardsYardCalculationService.explainYardCalculation(payload1)
            normalizeYardResponse {
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.status = "ok"
                }
                this.yard = yard {
                    this.code = reply0.yard.code
                    this.components.putAll(mapOf("primary" to yardComponent {
                        this.description = (reply0.yard.componentsMap["primary"] ?: YardComponent.getDefaultInstance()).description
                    }))
                    this.status = request.yard.status
                    this.yardId = request.yardId
                }
            }
        }

    private suspend fun handlePreviewYard(request: PreviewYardRequest): PreviewYardResponse =
        RpcBoundary.run("acme.yards.v1.YardValidationService/PreviewYard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PreviewYard", request)
            log.atInfo().addKeyValue("operation", "PreviewYard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yard.yardId).log("Recorded audit detail")
            val outbound0 = estimateDockRequest {
                this.context = request.context
                this.details = EstimateDockRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(request.yard.address.linesList)
                    }
                }
            }
            val adapter0 = OutboundPort<EstimateDockRequest, EstimateDockResponse> { payload ->
                yardsDockCalculationService.estimateDock(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            previewYardResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.yard = yard {
                    this.audit = auditInfo {
                        this.createdAt = reply0.dock.audit.createdAt
                    }
                    this.capacity = request.yard.capacity
                    this.owner = userRef {
                        this.email = reply0.dock.owner.email
                    }
                    this.yardId = request.yardId
                }
            }
        }

    private suspend fun handleValidateYard(request: ValidateYardRequest): ValidateYardResponse =
        RpcBoundary.run("acme.yards.v1.YardValidationService/ValidateYard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ValidateYard", request)
            log.atInfo().addKeyValue("operation", "ValidateYard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yard.yardId).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                compareYardOptionsRequest {
                    this.context = request.context
                    this.details = CompareYardOptionsRequestKt.details {
                        this.destination = postalAddress {
                            this.recipient = request.yard.address.recipient
                            this.region = request.yard.address.region
                        }
                    }
                    this.yard = yard {
                        this.address = postalAddress {
                            this.postalCode = request.yard.address.postalCode
                        }
                        this.owner = userRef {
                            this.email = request.details.contact.email
                        }
                    }
                }
            }
            val reply0 = yardsYardCalculationService.compareYardOptions(outbound0)
            val outbound1 = calculateYardRequest {
                this.context = request.context
                this.details = CalculateYardRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(reply0.yard.address.linesList)
                    }
                }
                this.yard = yard {
                    this.dimensions = yardDimensions {
                        this.unit = request.yard.dimensions.unit
                        this.width = reply0.yard.dimensions.width
                    }
                }
            }
            val reply1 = ClientRegistry.lookup("acme.yards.v1.YardCalculationService", YardCalculationServiceGrpcKt.YardCalculationServiceCoroutineStub::class.java).calculateYard(outbound1)
            val outbound2 = explainDockCalculationRequest {
                this.context = request.context
                this.dock = dock {
                    this.address = postalAddress {
                        this.countryCode = reply0.yard.address.countryCode
                    }
                }
            }
            val reply2 = yardsDockCalculationService.explainDockCalculation(WireCopies.reflective(outbound2))
            validateYardResponse {
                this.result = operationResult {
                    this.status = reply2.outcome.status
                }
                this.yard = yard {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.yard.audit.updatedAt
                    }
                    this.capacity = request.yard.capacity
                    this.dimensions = yardDimensions {
                        this.width = reply0.yard.dimensions.width
                    }
                    this.owner = reply1.yard.owner
                    this.yardId = request.yardId
                }
            }
        }
}
