package com.acme.transport.telematics.api

import com.acme.authorization.v1.AccessPolicyServiceGrpcKt
import com.acme.authorization.v1.CloneAccessPolicyResponse
import com.acme.authorization.v1.accessPolicy
import com.acme.authorization.v1.accessPolicyCondition
import com.acme.authorization.v1.cloneAccessPolicyRequest
import com.acme.common.v1.Money
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.concessions.v1.ConcessionStand
import com.acme.concessions.v1.ConcessionStandCalculationServiceGrpcKt
import com.acme.concessions.v1.compareConcessionStandOptionsRequest
import com.acme.concessions.v1.concessionStand
import com.acme.customers.v1.customerRef
import com.acme.deliveries.v1.DeliveryExceptionValidationServiceGrpcKt
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.deliveryExceptionSla
import com.acme.deliveries.v1.previewDeliveryExceptionRequest
import com.acme.featureflags.v1.CloneFeatureFlagResponse
import com.acme.featureflags.v1.FeatureFlagServiceGrpcKt
import com.acme.featureflags.v1.cloneFeatureFlagRequest
import com.acme.featureflags.v1.featureFlag
import com.acme.featureflags.v1.featureFlagCondition
import com.acme.fuel.v1.FuelPolicyValidationServiceGrpcKt
import com.acme.fuel.v1.ScoreFuelPolicyRequest
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.fuelPolicyDiscountAction
import com.acme.fuel.v1.scoreFuelPolicyRequest
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.WorkOrderCalculationServiceGrpcKt
import com.acme.maintenance.v1.quoteWorkOrderRequest
import com.acme.maintenance.v1.workOrder
import com.acme.metrics.v1.MetricSeriesValidationServiceGrpcKt
import com.acme.metrics.v1.metricSeries
import com.acme.metrics.v1.previewMetricSeriesRequest
import com.acme.notifications.v1.NotificationValidationServiceGrpcKt
import com.acme.notifications.v1.ValidateNotificationRequest
import com.acme.notifications.v1.ValidateNotificationRequestKt
import com.acme.notifications.v1.ValidateNotificationResponse
import com.acme.notifications.v1.explainNotificationDecisionRequest
import com.acme.notifications.v1.notification
import com.acme.notifications.v1.notificationTemplateRef
import com.acme.notifications.v1.validateNotificationRequest
import com.acme.secrets.v1.SecretQueryServiceGrpcKt
import com.acme.secrets.v1.SummarizeSecretsRequest
import com.acme.secrets.v1.SummarizeSecretsResponse
import com.acme.secrets.v1.summarizeSecretsRequest
import com.acme.telematics.v1.CheckGeoFenceEligibilityRequest
import com.acme.telematics.v1.CheckGeoFenceEligibilityResponse
import com.acme.telematics.v1.CheckGeoFenceEligibilityResponseKt
import com.acme.telematics.v1.CheckTelemetryReadingEligibilityRequest
import com.acme.telematics.v1.CheckTelemetryReadingEligibilityRequestKt
import com.acme.telematics.v1.ExplainGeoFenceDecisionRequest
import com.acme.telematics.v1.ExplainGeoFenceDecisionResponse
import com.acme.telematics.v1.ExplainGeoFenceDecisionResponseKt
import com.acme.telematics.v1.ExplainTelemetryReadingDecisionRequestKt
import com.acme.telematics.v1.GeoFenceValidationServiceGrpcKt
import com.acme.telematics.v1.GetGeoFenceRulesRequest
import com.acme.telematics.v1.GetGeoFenceRulesResponse
import com.acme.telematics.v1.LintGeoFencesRequest
import com.acme.telematics.v1.LintGeoFencesResponse
import com.acme.telematics.v1.LintGeoFencesResponseKt
import com.acme.telematics.v1.NormalizeGeoFenceRequest
import com.acme.telematics.v1.NormalizeGeoFenceResponse
import com.acme.telematics.v1.NormalizeGeoFenceResponseKt
import com.acme.telematics.v1.PreviewGeoFenceRequest
import com.acme.telematics.v1.PreviewGeoFenceResponse
import com.acme.telematics.v1.PreviewGeoFenceResponseKt
import com.acme.telematics.v1.PreviewTelemetryReadingRequestKt
import com.acme.telematics.v1.ScoreTelemetryReadingRequestKt
import com.acme.telematics.v1.SimulateTripLogResponse
import com.acme.telematics.v1.TelemetryReadingValidationServiceGrpcKt
import com.acme.telematics.v1.TripLogCalculationServiceGrpcKt
import com.acme.telematics.v1.ValidateGeoFenceRequest
import com.acme.telematics.v1.ValidateGeoFenceResponse
import com.acme.telematics.v1.ValidateGeoFenceResponseKt
import com.acme.telematics.v1.ValidateTelemetryReadingRequest
import com.acme.telematics.v1.ValidateTelemetryReadingRequestKt
import com.acme.telematics.v1.ValidateTelemetryReadingResponse
import com.acme.telematics.v1.checkGeoFenceEligibilityResponse
import com.acme.telematics.v1.checkTelemetryReadingEligibilityRequest
import com.acme.telematics.v1.explainGeoFenceDecisionResponse
import com.acme.telematics.v1.explainTelemetryReadingDecisionRequest
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.geoFenceRef
import com.acme.telematics.v1.getGeoFenceRulesResponse
import com.acme.telematics.v1.lintGeoFencesResponse
import com.acme.telematics.v1.normalizeGeoFenceResponse
import com.acme.telematics.v1.previewGeoFenceResponse
import com.acme.telematics.v1.previewTelemetryReadingRequest
import com.acme.telematics.v1.scoreTelemetryReadingRequest
import com.acme.telematics.v1.simulateTripLogRequest
import com.acme.telematics.v1.telemetryReading
import com.acme.telematics.v1.telemetryReadingMeasurement
import com.acme.telematics.v1.tripLog
import com.acme.telematics.v1.validateGeoFenceResponse
import com.acme.telematics.v1.validateTelemetryReadingRequest
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.transport.telematics.TelematicsRepository
import com.acme.venues.v1.CheckVenueEligibilityRequestKt
import com.acme.venues.v1.PreviewVenueRequest
import com.acme.venues.v1.PreviewVenueResponse
import com.acme.venues.v1.VenueValidationServiceGrpcKt
import com.acme.venues.v1.checkVenueEligibilityRequest
import com.acme.venues.v1.previewVenueRequest
import com.acme.venues.v1.venue
import com.google.protobuf.Any as ProtoAny
import com.google.protobuf.Timestamp
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Checks geo fences against the rules before anything acts on them. */
class GeoFenceValidationGrpcService(
    private val store: TelematicsRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyService: AccessPolicyServiceGrpcKt.AccessPolicyServiceCoroutineStub,
    private val concessionsConcessionStandCalculationService: ConcessionStandCalculationServiceGrpcKt.ConcessionStandCalculationServiceCoroutineStub,
    private val deliveriesDeliveryExceptionValidationService: DeliveryExceptionValidationServiceGrpcKt.DeliveryExceptionValidationServiceCoroutineStub,
    private val featureflagsFeatureFlagService: FeatureFlagServiceGrpcKt.FeatureFlagServiceCoroutineStub,
    private val fuelFuelPolicyValidationService: FuelPolicyValidationServiceGrpcKt.FuelPolicyValidationServiceCoroutineStub,
    private val maintenanceWorkOrderCalculationService: WorkOrderCalculationServiceGrpcKt.WorkOrderCalculationServiceCoroutineStub,
    private val metricsMetricSeriesValidationService: MetricSeriesValidationServiceGrpcKt.MetricSeriesValidationServiceCoroutineStub,
    private val notificationsNotificationValidationService: NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub,
    private val secretsSecretQueryService: SecretQueryServiceGrpcKt.SecretQueryServiceCoroutineStub,
    private val telematicsTelemetryReadingValidationService: TelemetryReadingValidationServiceGrpcKt.TelemetryReadingValidationServiceCoroutineStub,
    private val telematicsTripLogCalculationService: TripLogCalculationServiceGrpcKt.TripLogCalculationServiceCoroutineStub,
    private val venuesVenueValidationService: VenueValidationServiceGrpcKt.VenueValidationServiceCoroutineStub,
) : GeoFenceValidationServiceGrpcKt.GeoFenceValidationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(GeoFenceValidationGrpcService::class.java)

    private val invokeExplainGeoFenceDecision1: suspend (PreviewVenueRequest) -> PreviewVenueResponse = { payload ->
        venuesVenueValidationService.previewVenue(payload)
    }

    private val workerForLintGeoFences1 = ReplyMailbox<ValidateNotificationRequest, ValidateNotificationResponse>(workers) { payload ->
        withTimeout(5_000) { notificationsNotificationValidationService.validateNotification(payload) }
    }

    private val clientForNormalizeGeoFence0 by lazy { maintenanceWorkOrderCalculationService }

    /** Checks a geo fence. */
    override suspend fun checkGeoFenceEligibility(request: CheckGeoFenceEligibilityRequest): CheckGeoFenceEligibilityResponse {
        return handleCheckGeoFenceEligibility(request)
    }

    /** Explains a geo fence. */
    override suspend fun explainGeoFenceDecision(request: ExplainGeoFenceDecisionRequest): ExplainGeoFenceDecisionResponse = handleExplainGeoFenceDecision(request)

    /** Returns a geo fence. */
    override suspend fun getGeoFenceRules(request: GetGeoFenceRulesRequest): GetGeoFenceRulesResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceValidationService/GetGeoFenceRules") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetGeoFenceRules", request)
            log.atInfo().addKeyValue("operation", "GetGeoFenceRules").log("Handling transport request")
            val outbound0 = RequestFactory.create {
                checkVenueEligibilityRequest {
                    this.context = request.context
                    this.details = CheckVenueEligibilityRequestKt.details {
                        this.contact = contactInfo {
                            this.email = request.context.actor.email
                        }
                    }
                    this.venue = venue {
                        this.owner = userRef {
                            this.userId = request.context.actor.userId
                        }
                    }
                }
            }
            val reply0 = venuesVenueValidationService.checkVenueEligibility(outbound0)
            val outbound1 = explainNotificationDecisionRequest {
                this.context = request.context
                this.notification = notification {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.venue.audit.updatedAt
                        this.updatedBy = reply0.venue.audit.updatedBy
                    }
                }
            }
            val reply1 = ClientRegistry.lookup("acme.notifications.v1.NotificationValidationService", NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub::class.java).explainNotificationDecision(outbound1)
            getGeoFenceRulesResponse {
                this.geoFence = geoFence {
                    this.geoFenceId = request.geoFenceId
                }
                this.result = operationResult {
                    this.message = reply0.outcome.message
                    this.status = reply1.outcome.status
                }
            }
        }

    /** Lints a geo fence. */
    override suspend fun lintGeoFences(request: LintGeoFencesRequest): LintGeoFencesResponse {
        return handleLintGeoFences(request)
    }

    /** Normalizes a geo fence. */
    override suspend fun normalizeGeoFence(request: NormalizeGeoFenceRequest): NormalizeGeoFenceResponse = handleNormalizeGeoFence(request)

    /** Previews a geo fence. */
    override suspend fun previewGeoFence(request: PreviewGeoFenceRequest): PreviewGeoFenceResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceValidationService/PreviewGeoFence") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PreviewGeoFence", request)
            log.atInfo().addKeyValue("operation", "PreviewGeoFence").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.geoFence.name).log("Recorded audit detail")
            val outbound0 = previewTelemetryReadingRequest {
                this.context = request.context
                this.details = PreviewTelemetryReadingRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.geoFence.owner.email
                        this.phone = request.details.contact.phone
                        this.preferredChannel = request.details.contact.preferredChannel
                    }
                }
                this.telemetryReading = telemetryReading {
                    this.subject = geoFenceRef {
                        this.geoFenceId = request.geoFence.geoFenceId
                    }
                }
            }
            val reply0 = telematicsTelemetryReadingValidationService.previewTelemetryReading(outbound0)
            previewGeoFenceResponse {
                this.geoFence = geoFence {
                    this.geoFenceId = request.geoFenceId
                    this.status = request.geoFence.status
                }
                this.outcome = PreviewGeoFenceResponseKt.outcome {
                    this.message = reply0.outcome.message
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Validates a geo fence. */
    override suspend fun validateGeoFence(request: ValidateGeoFenceRequest): ValidateGeoFenceResponse {
        return handleValidateGeoFence(request)
    }

    private data class CheckGeoFenceEligibility2Command(
        val context: RequestContext,
        val fuelPolicyAuditCreatedBy: String,
        val fuelPolicyDiscountAmount: Money,
        val fuelPolicyAuditCreatedAt: Timestamp,
        val fuelPolicyOwnerDisplayName: String
    ) {
        fun toRequest(): ScoreFuelPolicyRequest {
            val command = this
            return scoreFuelPolicyRequest {
                this.context = command.context
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.createdAt = command.fuelPolicyAuditCreatedAt
                        this.createdBy = command.fuelPolicyAuditCreatedBy
                    }
                    this.discount = fuelPolicyDiscountAction {
                        this.amount = command.fuelPolicyDiscountAmount
                    }
                    this.owner = userRef {
                        this.displayName = command.fuelPolicyOwnerDisplayName
                    }
                }
            }
        }
    }

    private suspend fun handleCheckGeoFenceEligibility(request: CheckGeoFenceEligibilityRequest): CheckGeoFenceEligibilityResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceValidationService/CheckGeoFenceEligibility") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CheckGeoFenceEligibility", request)
            log.atInfo().addKeyValue("operation", "CheckGeoFenceEligibility").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.geoFenceId).log("Recorded audit detail")
            val outbound0 = validateTelemetryReadingRequest {
                this.context = request.context
                this.details = ValidateTelemetryReadingRequestKt.details {
                    this.contact = contactInfo {
                        this.name = request.details.contact.name
                        this.phone = request.details.contact.phone
                    }
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = telematicsTelemetryReadingValidationService.validateTelemetryReading(envelope0.unpack(ValidateTelemetryReadingRequest::class.java))
            val outbound1 = explainTelemetryReadingDecisionRequest {
                this.context = request.context
                this.details = ExplainTelemetryReadingDecisionRequestKt.details {
                    this.contact = contactInfo {
                        this.phone = request.details.contact.phone
                    }
                }
                this.telemetryReading = telemetryReading {
                    this.subject = geoFenceRef {
                        this.displayName = request.geoFence.owner.displayName
                        this.version = reply0.telemetryReading.subject.version
                    }
                }
            }
            val reply1 = telematicsTelemetryReadingValidationService.explainTelemetryReadingDecision(WireCopies.json(outbound1))
            val outbound2 = CheckGeoFenceEligibility2Command(
                context = request.context,
                fuelPolicyAuditCreatedBy = request.geoFence.audit.createdBy,
                fuelPolicyDiscountAmount = request.geoFence.discount.amount,
                fuelPolicyAuditCreatedAt = request.geoFence.audit.createdAt,
                fuelPolicyOwnerDisplayName = reply1.telemetryReading.subject.displayName
            ).toRequest()
            val reply2 = fuelFuelPolicyValidationService.scoreFuelPolicy(outbound2)
            var reply3 = CloneAccessPolicyResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.geoFence.conditionsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound3 = cloneAccessPolicyRequest {
                                this.accessPolicy = accessPolicy {
                                    this.conditions.addAll(listOf(accessPolicyCondition {
                                        this.attribute = item.attribute
                                        this.value = item.value
                                    }))
                                }
                                this.context = request.context
                            }
                            authorizationAccessPolicyService.cloneAccessPolicy(outbound3)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply3 = it }
                }
            }
            checkGeoFenceEligibilityResponse {
                this.geoFence = geoFence {
                    this.description = reply2.fuelPolicy.description
                    this.geoFenceId = reply0.telemetryReading.subject.geoFenceId
                    this.parameters.putAll(request.geoFence.parametersMap)
                    this.priority = reply2.fuelPolicy.priority
                    this.status = request.geoFence.status
                }
                this.outcome = CheckGeoFenceEligibilityResponseKt.outcome {
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleExplainGeoFenceDecision(request: ExplainGeoFenceDecisionRequest): ExplainGeoFenceDecisionResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceValidationService/ExplainGeoFenceDecision") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainGeoFenceDecision", request)
            log.atInfo().addKeyValue("operation", "ExplainGeoFenceDecision").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.geoFence.geoFenceId).log("Recorded audit detail")
            val outbound0 = validateTelemetryReadingRequest {
                this.context = request.context
                this.details = ValidateTelemetryReadingRequestKt.details {
                    this.contact = contactInfo {
                        this.name = request.details.contact.name
                        this.phone = request.details.contact.phone
                        this.preferredChannel = request.details.contact.preferredChannel
                    }
                }
                this.telemetryReading = telemetryReading {
                    this.subject = geoFenceRef {
                        this.displayName = request.geoFence.owner.displayName
                    }
                }
            }
            val adapter0 = OutboundPort<ValidateTelemetryReadingRequest, ValidateTelemetryReadingResponse> { payload ->
                telematicsTelemetryReadingValidationService.validateTelemetryReading(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = previewVenueRequest {
                this.context = request.context
                this.venue = venue {
                    this.audit = auditInfo {
                        this.updatedBy = request.geoFence.audit.updatedBy
                    }
                }
            }
            val reply1 = invokeExplainGeoFenceDecision1(outbound1)
            val outbound2 = summarizeSecretsRequest {
                this.context = request.context
                this.reason = request.reason
            }
            val invoke2: suspend (SummarizeSecretsRequest, Metadata) -> SummarizeSecretsResponse = secretsSecretQueryService::summarizeSecrets
            val reply2 = invoke2(outbound2, Metadata())
            val outbound3 = CheckTelemetryReadingEligibilityRequest.newBuilder().apply {
                setContext(request.context)
                setDetails(CheckTelemetryReadingEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.geoFence.owner.email
                    }
                })
                setTelemetryReading(telemetryReading {
                    this.subject = geoFenceRef {
                        this.displayName = request.geoFence.owner.displayName
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply3 = outbound3.let { value -> telematicsTelemetryReadingValidationService.checkTelemetryReadingEligibility(value) }
            explainGeoFenceDecisionResponse {
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.updatedAt = reply1.venue.audit.updatedAt
                    }
                    this.geoFenceId = request.geoFenceId
                    this.status = request.geoFence.status
                }
                this.outcome = ExplainGeoFenceDecisionResponseKt.outcome {
                    this.message = reply3.result.message
                    this.nextSteps.addAll(reply3.outcome.nextStepsList)
                }
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply1.outcome.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleLintGeoFences(request: LintGeoFencesRequest): LintGeoFencesResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceValidationService/LintGeoFences") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LintGeoFences", request)
            log.atInfo().addKeyValue("operation", "LintGeoFences").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.geoFence.geoFenceId).log("Recorded audit detail")
            val outbound0 = simulateTripLogRequest {
                this.context = request.context
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.updatedBy = request.geoFence.audit.updatedBy
                    }
                    this.counterparty = customerRef {
                        this.displayName = request.geoFence.owner.displayName
                    }
                }
            }
            val reply0 = telematicsTripLogCalculationService.simulateTripLog(WireCopies.reflective(outbound0))
            val outbound1 = validateNotificationRequest {
                this.context = request.context
                this.details = ValidateNotificationRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.geoFence.owner.email
                    }
                }
            }
            val reply1 = workerForLintGeoFences1.submit(outbound1)
            lintGeoFencesResponse {
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.etag = reply0.tripLog.audit.etag
                    }
                    this.description = request.geoFence.description
                    this.geoFenceId = request.geoFenceId
                }
                this.outcome = LintGeoFencesResponseKt.outcome {
                    this.nextSteps.addAll(reply1.outcome.nextStepsList)
                    this.referenceId = reply1.outcome.referenceId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleNormalizeGeoFence(request: NormalizeGeoFenceRequest): NormalizeGeoFenceResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceValidationService/NormalizeGeoFence") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "NormalizeGeoFence", request)
            log.atInfo().addKeyValue("operation", "NormalizeGeoFence").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.geoFence.geoFenceId).log("Recorded audit detail")
            val outbound0 = quoteWorkOrderRequest {
                this.context = request.context
                this.workOrder = workOrder {
                    this.audit = auditInfo {
                        this.updatedBy = request.geoFence.audit.updatedBy
                    }
                    this.requester = contactInfo {
                        this.phone = request.details.contact.phone
                    }
                }
            }
            val reply0 = clientForNormalizeGeoFence0.quoteWorkOrder(outbound0)
            val outbound1 = previewDeliveryExceptionRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.sla = deliveryExceptionSla {
                        this.breached = reply0.workOrder.sla.breached
                        this.dueAt = reply0.workOrder.sla.dueAt
                    }
                }
            }
            val reply1 = flowOf(outbound1).map { value -> deliveriesDeliveryExceptionValidationService.previewDeliveryException(value) }.single()
            val outbound2 = previewMetricSeriesRequest {
                this.context = request.context
                this.metricSeries = metricSeries {
                    this.owner = userRef {
                        this.email = reply0.workOrder.customer.email
                    }
                }
            }
            val reply2 = withTimeout(5_000) { metricsMetricSeriesValidationService.previewMetricSeries(outbound2) }
            var reply3 = CloneFeatureFlagResponse.getDefaultInstance()
            request.geoFence.conditionsList.asFlow().map { item ->
                val outbound3 = cloneFeatureFlagRequest {
                    this.context = request.context
                    this.featureFlag = featureFlag {
                        this.conditions.addAll(listOf(featureFlagCondition {
                            this.attribute = item.attribute
                            this.negate = item.negate
                            this.value = item.value
                        }))
                    }
                }
                featureflagsFeatureFlagService.cloneFeatureFlag(outbound3)
            }.collect { reply3 = it }
            normalizeGeoFenceResponse {
                this.geoFence = geoFence {
                    this.geoFenceId = request.geoFenceId
                    this.owner = userRef {
                        this.displayName = reply0.workOrder.assignee.displayName
                        this.email = reply1.deliveryException.requester.email
                        this.userId = reply1.deliveryException.assignee.userId
                    }
                    this.version = request.geoFence.version
                }
                this.outcome = NormalizeGeoFenceResponseKt.outcome {
                    this.status = reply2.result.status
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private fun ValidateGeoFenceRequest.toValidateNotificationFromValidateGeoFence2(reply1: SimulateTripLogResponse): ValidateNotificationRequest {
        val source = this
        return validateNotificationRequest {
            this.context = source.context
            this.notification = notification {
                this.assignee = userRef {
                    this.email = source.geoFence.owner.email
                }
                this.audit = auditInfo {
                    this.createdBy = reply1.tripLog.audit.createdBy
                    this.updatedAt = source.geoFence.audit.updatedAt
                }
                this.customer = notificationTemplateRef {
                    this.displayName = reply1.tripLog.counterparty.displayName
                }
            }
        }
    }

    private suspend fun handleValidateGeoFence(request: ValidateGeoFenceRequest): ValidateGeoFenceResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceValidationService/ValidateGeoFence") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ValidateGeoFence", request)
            log.atInfo().addKeyValue("operation", "ValidateGeoFence").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.geoFence.geoFenceId).log("Recorded audit detail")
            val outbound0 = compareConcessionStandOptionsRequest {
                this.concessionStand = concessionStand {
                    this.owner = userRef {
                        this.displayName = request.geoFence.owner.displayName
                    }
                }
                this.context = request.context
            }
            val reply0 = concessionsConcessionStandCalculationService.compareConcessionStandOptions(outbound0)
            val outbound1 = simulateTripLogRequest {
                this.context = request.context
                this.tripLog = tripLog {
                    this.billingAddress = postalAddress {
                        this.region = when (reply0.concessionStand.locatorCase) {
                            ConcessionStand.LocatorCase.ADDRESS -> reply0.concessionStand.address.region
                            else -> ""
                        }
                    }
                }
            }
            val reply1 = telematicsTripLogCalculationService.simulateTripLog(outbound1)
            val outbound2 = request.toValidateNotificationFromValidateGeoFence2(reply1)
            val reply2 = notificationsNotificationValidationService.validateNotification(outbound2)
            val outbound3 = scoreTelemetryReadingRequest {
                this.context = request.context
                this.details = ScoreTelemetryReadingRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = reply1.tripLog.billingAddress.countryCode
                        this.lines.addAll(reply1.tripLog.billingAddress.linesList)
                        this.locality = reply1.tripLog.billingAddress.locality
                    }
                }
                this.telemetryReading = telemetryReading {
                    this.measurements.addAll(listOf(telemetryReadingMeasurement {
                        this.unit = reply0.concessionStand.dimensions.unit
                    }))
                }
            }
            val reply3 = telematicsTelemetryReadingValidationService.scoreTelemetryReading(outbound3)
            validateGeoFenceResponse {
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.updatedBy = reply1.tripLog.audit.updatedBy
                    }
                    this.geoFenceId = request.geoFenceId
                    this.name = request.geoFence.name
                    this.owner = userRef {
                        this.displayName = reply0.concessionStand.owner.displayName
                    }
                }
                this.outcome = ValidateGeoFenceResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.details.putAll(reply2.result.detailsMap)
                    this.status = "ok"
                }
            }
        }
}
