package com.acme.transport.telematics.adapters

import com.acme.audit.v1.AuditEventQueryServiceGrpcKt
import com.acme.audit.v1.ResolveAuditEventsRequest
import com.acme.audit.v1.ResolveAuditEventsResponse
import com.acme.audit.v1.resolveAuditEventsRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.customerRef
import com.acme.deliveries.v1.DeliveryExceptionValidationServiceGrpcKt
import com.acme.deliveries.v1.NormalizeDeliveryExceptionRequest
import com.acme.deliveries.v1.NormalizeDeliveryExceptionRequestKt
import com.acme.deliveries.v1.NormalizeDeliveryExceptionResponse
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.normalizeDeliveryExceptionRequest
import com.acme.featureflags.v1.FeatureFlagValidationServiceGrpcKt
import com.acme.featureflags.v1.ScoreFeatureFlagRequest
import com.acme.featureflags.v1.featureFlag
import com.acme.featureflags.v1.scoreFeatureFlagRequest
import com.acme.fuel.v1.ExplainFuelTransactionDecisionRequest
import com.acme.fuel.v1.ExplainFuelTransactionDecisionRequestKt
import com.acme.fuel.v1.FuelTransactionCalculationServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionValidationServiceGrpcKt
import com.acme.fuel.v1.RecalculateFuelTransactionsRequestKt
import com.acme.fuel.v1.explainFuelTransactionDecisionRequest
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.recalculateFuelTransactionsRequest
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.LedgerAccountValidationServiceGrpcKt
import com.acme.ledger.v1.NormalizeLedgerAccountRequestKt
import com.acme.ledger.v1.NormalizeLedgerAccountResponse
import com.acme.ledger.v1.ledgerAccount
import com.acme.ledger.v1.ledgerAccountCardFunding
import com.acme.ledger.v1.ledgerAccountLimit
import com.acme.ledger.v1.normalizeLedgerAccountRequest
import com.acme.ledger.v1.previewLedgerAccountRequest
import com.acme.maintenance.v1.WorkOrderQueryServiceGrpcKt
import com.acme.maintenance.v1.resolveWorkOrdersRequest
import com.acme.metrics.v1.MetricSeriesServiceGrpcKt
import com.acme.metrics.v1.metricSeries
import com.acme.metrics.v1.updateMetricSeriesRequest
import com.acme.notifications.v1.BrowseNotificationsRequestKt
import com.acme.notifications.v1.NotificationQueryServiceGrpcKt
import com.acme.notifications.v1.browseNotificationsRequest
import com.acme.telematics.v1.CalculateGeoFenceRequestKt
import com.acme.telematics.v1.CalculateTripLogRequest
import com.acme.telematics.v1.CheckTripLogEligibilityRequest
import com.acme.telematics.v1.CheckTripLogEligibilityResponse
import com.acme.telematics.v1.CheckTripLogEligibilityResponseKt
import com.acme.telematics.v1.EstimateGeoFenceRequestKt
import com.acme.telematics.v1.ExplainGeoFenceDecisionRequestKt
import com.acme.telematics.v1.ExplainTelemetryReadingDecisionRequest
import com.acme.telematics.v1.ExplainTelemetryReadingDecisionRequestKt
import com.acme.telematics.v1.ExplainTripLogDecisionRequest
import com.acme.telematics.v1.ExplainTripLogDecisionResponse
import com.acme.telematics.v1.GeoFenceCalculationServiceGrpcKt
import com.acme.telematics.v1.GeoFenceValidationServiceGrpcKt
import com.acme.telematics.v1.GetTripLogRulesRequest
import com.acme.telematics.v1.GetTripLogRulesResponse
import com.acme.telematics.v1.LintTripLogsRequest
import com.acme.telematics.v1.LintTripLogsResponse
import com.acme.telematics.v1.LintTripLogsResponseKt
import com.acme.telematics.v1.NormalizeTelemetryReadingRequestKt
import com.acme.telematics.v1.NormalizeTripLogRequest
import com.acme.telematics.v1.NormalizeTripLogResponse
import com.acme.telematics.v1.NormalizeTripLogResponseKt
import com.acme.telematics.v1.PreviewTelemetryReadingRequest
import com.acme.telematics.v1.PreviewTelemetryReadingRequestKt
import com.acme.telematics.v1.PreviewTelemetryReadingResponse
import com.acme.telematics.v1.PreviewTripLogRequest
import com.acme.telematics.v1.PreviewTripLogResponse
import com.acme.telematics.v1.ScoreTripLogRequest
import com.acme.telematics.v1.ScoreTripLogResponse
import com.acme.telematics.v1.ScoreTripLogResponseKt
import com.acme.telematics.v1.TelemetryReadingValidationServiceGrpcKt
import com.acme.telematics.v1.TripLogCalculationServiceGrpcKt
import com.acme.telematics.v1.TripLogValidationServiceGrpcKt
import com.acme.telematics.v1.ValidateTripLogRequest
import com.acme.telematics.v1.ValidateTripLogResponse
import com.acme.telematics.v1.ValidateTripLogResponseKt
import com.acme.telematics.v1.calculateGeoFenceRequest
import com.acme.telematics.v1.calculateTripLogRequest
import com.acme.telematics.v1.checkTelemetryReadingEligibilityRequest
import com.acme.telematics.v1.checkTripLogEligibilityResponse
import com.acme.telematics.v1.copy as telematicsCopy
import com.acme.telematics.v1.estimateGeoFenceRequest
import com.acme.telematics.v1.explainGeoFenceDecisionRequest
import com.acme.telematics.v1.explainTelemetryReadingDecisionRequest
import com.acme.telematics.v1.explainTripLogDecisionResponse
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.geoFenceRef
import com.acme.telematics.v1.getTripLogRulesResponse
import com.acme.telematics.v1.lintTripLogsResponse
import com.acme.telematics.v1.normalizeTelemetryReadingRequest
import com.acme.telematics.v1.normalizeTripLogResponse
import com.acme.telematics.v1.previewTelemetryReadingRequest
import com.acme.telematics.v1.previewTripLogResponse
import com.acme.telematics.v1.scoreTripLogResponse
import com.acme.telematics.v1.telemetryReading
import com.acme.telematics.v1.telemetryReadingMeasurement
import com.acme.telematics.v1.tripLog
import com.acme.telematics.v1.tripLogLine
import com.acme.telematics.v1.tripLogLineTax
import com.acme.telematics.v1.validateTripLogResponse
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.transport.telematics.TelematicsRepository
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Checks trip logs against the rules before anything acts on them. */
class TripLogValidationController(
    private val store: TelematicsRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventQueryService: AuditEventQueryServiceGrpcKt.AuditEventQueryServiceCoroutineStub,
    private val deliveriesDeliveryExceptionValidationService: DeliveryExceptionValidationServiceGrpcKt.DeliveryExceptionValidationServiceCoroutineStub,
    private val featureflagsFeatureFlagValidationService: FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub,
    private val fuelFuelTransactionCalculationService: FuelTransactionCalculationServiceGrpcKt.FuelTransactionCalculationServiceCoroutineStub,
    private val fuelFuelTransactionValidationService: FuelTransactionValidationServiceGrpcKt.FuelTransactionValidationServiceCoroutineStub,
    private val ledgerLedgerAccountValidationService: LedgerAccountValidationServiceGrpcKt.LedgerAccountValidationServiceCoroutineStub,
    private val maintenanceWorkOrderQueryService: WorkOrderQueryServiceGrpcKt.WorkOrderQueryServiceCoroutineStub,
    private val metricsMetricSeriesService: MetricSeriesServiceGrpcKt.MetricSeriesServiceCoroutineStub,
    private val notificationsNotificationQueryService: NotificationQueryServiceGrpcKt.NotificationQueryServiceCoroutineStub,
    private val telematicsGeoFenceCalculationService: GeoFenceCalculationServiceGrpcKt.GeoFenceCalculationServiceCoroutineStub,
    private val telematicsGeoFenceValidationService: GeoFenceValidationServiceGrpcKt.GeoFenceValidationServiceCoroutineStub,
    private val telematicsTelemetryReadingValidationService: TelemetryReadingValidationServiceGrpcKt.TelemetryReadingValidationServiceCoroutineStub,
    private val telematicsTripLogCalculationService: TripLogCalculationServiceGrpcKt.TripLogCalculationServiceCoroutineStub,
) : TripLogValidationServiceGrpcKt.TripLogValidationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(TripLogValidationController::class.java)

    private val workerForNormalizeTripLog1 = ReplyMailbox<NormalizeDeliveryExceptionRequest, NormalizeDeliveryExceptionResponse>(workers) { payload ->
        withTimeout(5_000) { deliveriesDeliveryExceptionValidationService.normalizeDeliveryException(payload) }
    }

    private val clientForPreviewTripLog0 by lazy { metricsMetricSeriesService }

    /** Checks a trip log. */
    override suspend fun checkTripLogEligibility(request: CheckTripLogEligibilityRequest): CheckTripLogEligibilityResponse = handleCheckTripLogEligibility(request)

    /** Explains a trip log. */
    override suspend fun explainTripLogDecision(request: ExplainTripLogDecisionRequest): ExplainTripLogDecisionResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogValidationService/ExplainTripLogDecision") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainTripLogDecision", request)
            log.atInfo().addKeyValue("operation", "ExplainTripLogDecision").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLog.tripLogId).log("Recorded audit detail")
            val outbound0 = resolveAuditEventsRequest {
                this.context = request.context
            }
            val adapter0 = OutboundPort<ResolveAuditEventsRequest, ResolveAuditEventsResponse> { payload ->
                auditAuditEventQueryService.resolveAuditEvents(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            var reply1 = NormalizeLedgerAccountResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.tripLog.linesList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound1 = normalizeLedgerAccountRequest {
                                this.context = request.context
                                this.details = NormalizeLedgerAccountRequestKt.details {
                                    this.contact = contactInfo {
                                        this.phone = request.details.contact.phone
                                    }
                                }
                                this.ledgerAccount = ledgerAccount {
                                    this.balance = item.amount
                                    this.card = ledgerAccountCardFunding {
                                        this.holderName = request.tripLog.card.holderName
                                    }
                                    this.limits.addAll(listOf(ledgerAccountLimit {
                                        this.amount = item.tax.amount
                                    }))
                                }
                            }
                            ledgerLedgerAccountValidationService.normalizeLedgerAccount(outbound1)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply1 = it }
                }
            }
            explainTripLogDecisionResponse {
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.billingAddress = request.tripLog.billingAddress
                    this.tripLogId = request.tripLogId
                }
            }
        }

    /** Returns a trip log. */
    override suspend fun getTripLogRules(request: GetTripLogRulesRequest): GetTripLogRulesResponse {
        return handleGetTripLogRules(request)
    }

    /** Lints a trip log. */
    override suspend fun lintTripLogs(request: LintTripLogsRequest): LintTripLogsResponse = handleLintTripLogs(request)

    /** Normalizes a trip log. */
    override suspend fun normalizeTripLog(request: NormalizeTripLogRequest): NormalizeTripLogResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogValidationService/NormalizeTripLog") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "NormalizeTripLog", request)
            log.atInfo().addKeyValue("operation", "NormalizeTripLog").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLog.tripLogId).log("Recorded audit detail")
            val outbound0 = normalizeTelemetryReadingRequest {
                this.context = request.context
                this.details = NormalizeTelemetryReadingRequestKt.details {
                    this.contact = contactInfo {
                        this.name = request.details.contact.name
                        this.preferredChannel = request.details.contact.preferredChannel
                    }
                }
                this.telemetryReading = telemetryReading {
                    this.subject = geoFenceRef {
                        this.displayName = request.tripLog.counterparty.displayName
                    }
                }
            }
            val reply0 = telematicsTelemetryReadingValidationService.normalizeTelemetryReading(WireCopies.reflective(outbound0))
            val outbound1 = normalizeDeliveryExceptionRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.audit = auditInfo {
                        this.etag = request.tripLog.audit.etag
                    }
                }
                this.details = NormalizeDeliveryExceptionRequestKt.details {
                    this.contact = contactInfo {
                        this.phone = request.details.contact.phone
                    }
                }
            }
            val reply1 = workerForNormalizeTripLog1.submit(outbound1)
            normalizeTripLogResponse {
                this.outcome = NormalizeTripLogResponseKt.outcome {
                    this.nextSteps.addAll(reply0.outcome.nextStepsList)
                    this.status = reply1.outcome.status
                }
                this.result = operationResult {
                    this.message = reply0.outcome.message
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.createdAt = reply1.deliveryException.audit.createdAt
                    }
                    this.total = request.tripLog.total
                    this.tripLogId = request.tripLogId
                }
            }
        }

    /** Previews a trip log. */
    override suspend fun previewTripLog(request: PreviewTripLogRequest): PreviewTripLogResponse {
        return handlePreviewTripLog(request)
    }

    /** Scores a trip log. */
    override suspend fun scoreTripLog(request: ScoreTripLogRequest): ScoreTripLogResponse = handleScoreTripLog(request)

    /** Validates a trip log. */
    override suspend fun validateTripLog(request: ValidateTripLogRequest): ValidateTripLogResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogValidationService/ValidateTripLog") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ValidateTripLog", request)
            log.atInfo().addKeyValue("operation", "ValidateTripLog").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLog.tripLogId).log("Recorded audit detail")
            val outbound0 = explainGeoFenceDecisionRequest {}.telematicsCopy {
                this.context = request.context
                this.details = ExplainGeoFenceDecisionRequestKt.details {
                    this.contact = contactInfo {
                        this.name = request.details.contact.name
                        this.preferredChannel = request.details.contact.preferredChannel
                    }
                }
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.createdBy = request.tripLog.audit.createdBy
                    }
                }
            }
            val reply0 = telematicsGeoFenceValidationService.explainGeoFenceDecision(outbound0)
            val outbound1 = request.toExplainTelemetryReadingDecisionFromValidateTripLog1()
            val reply1 = telematicsTelemetryReadingValidationService.explainTelemetryReadingDecision(outbound1)
            val outbound2 = estimateGeoFenceRequest {
                this.context = request.context
                this.details = EstimateGeoFenceRequestKt.details {
                    this.destination = postalAddress {
                        this.locality = request.tripLog.billingAddress.locality
                    }
                }
            }
            val reply2 = telematicsGeoFenceCalculationService.estimateGeoFence(outbound2)
            validateTripLogResponse {
                this.outcome = ValidateTripLogResponseKt.outcome {
                    this.message = reply1.result.message
                }
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.createdAt = reply2.geoFence.audit.createdAt
                        this.updatedBy = reply0.geoFence.audit.updatedBy
                    }
                    this.lines.addAll(listOf(tripLogLine {
                        this.attributes.putAll(reply1.telemetryReading.attributesMap)
                        this.tax = tripLogLineTax {
                            this.amount = reply0.geoFence.discount.amount
                        }
                    }))
                    this.total = request.tripLog.total
                    this.tripLogId = request.tripLogId
                }
            }
        }

    private data class CheckTripLogEligibility0Command(
        val context: RequestContext,
        val detailsContactPreferredChannel: String,
        val detailsContactEmail: String,
        val fuelTransactionSubjectEmail: String
    ) {
        fun toRequest(): ExplainFuelTransactionDecisionRequest {
            val command = this
            return explainFuelTransactionDecisionRequest {
                this.context = command.context
                this.details = ExplainFuelTransactionDecisionRequestKt.details {
                    this.contact = contactInfo {
                        this.email = command.detailsContactEmail
                        this.preferredChannel = command.detailsContactPreferredChannel
                    }
                }
                this.fuelTransaction = fuelTransaction {
                    this.subject = customerRef {
                        this.email = command.fuelTransactionSubjectEmail
                    }
                }
            }
        }
    }

    private suspend fun handleCheckTripLogEligibility(request: CheckTripLogEligibilityRequest): CheckTripLogEligibilityResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogValidationService/CheckTripLogEligibility") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CheckTripLogEligibility", request)
            log.atInfo().addKeyValue("operation", "CheckTripLogEligibility").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLog.tripLogId).log("Recorded audit detail")
            val outbound0 = CheckTripLogEligibility0Command(
                context = request.context,
                detailsContactPreferredChannel = request.details.contact.preferredChannel,
                detailsContactEmail = request.tripLog.counterparty.email,
                fuelTransactionSubjectEmail = request.details.contact.email
            ).toRequest()
            val reply0 = fuelFuelTransactionValidationService.explainFuelTransactionDecision(outbound0)
            val outbound1 = calculateTripLogRequest {
                this.context = request.context
                this.tripLog = tripLog {
                    this.counterparty = customerRef {
                        this.displayName = request.tripLog.counterparty.displayName
                        this.email = request.details.contact.email
                    }
                }
            }
            val command1: Submission<CalculateTripLogRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = telematicsTripLogCalculationService.calculateTripLog(payload1)
            checkTripLogEligibilityResponse {
                this.outcome = CheckTripLogEligibilityResponseKt.outcome {
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.updatedBy = reply1.tripLog.audit.updatedBy
                    }
                    this.issuedAt = request.tripLog.issuedAt
                    this.tripLogId = request.tripLogId
                }
            }
        }

    private suspend fun handleGetTripLogRules(request: GetTripLogRulesRequest): GetTripLogRulesResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogValidationService/GetTripLogRules") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetTripLogRules", request)
            log.atInfo().addKeyValue("operation", "GetTripLogRules").log("Handling transport request")
            val outbound0 = previewTelemetryReadingRequest {
                this.context = request.context
                this.details = PreviewTelemetryReadingRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                    this.locale = request.context.locale
                }
                this.telemetryReading = telemetryReading {
                    this.correlationId = request.tripLogId
                    this.measurements.addAll(listOf(telemetryReadingMeasurement {
                        this.observedAt = request.context.deadline
                    }))
                }
            }
            val invoke0: suspend (PreviewTelemetryReadingRequest, Metadata) -> PreviewTelemetryReadingResponse = telematicsTelemetryReadingValidationService::previewTelemetryReading
            val reply0 = invoke0(outbound0, Metadata())
            val outbound1 = ScoreFeatureFlagRequest.newBuilder().apply {
                setContext(request.context)
                setFeatureFlag(featureFlag {
                    this.owner = userRef {
                        this.displayName = reply0.telemetryReading.subject.displayName
                    }
                    this.version = reply0.telemetryReading.subject.version
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> featureflagsFeatureFlagValidationService.scoreFeatureFlag(value) }
            getTripLogRulesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.updatedAt = reply1.featureFlag.audit.updatedAt
                    }
                    this.counterparty = customerRef {
                        this.displayName = reply0.telemetryReading.subject.displayName
                    }
                    this.lines.addAll(listOf(tripLogLine {
                        this.amount = reply1.featureFlag.discount.amount
                    }))
                    this.tripLogId = request.tripLogId
                }
            }
        }

    private suspend fun handleLintTripLogs(request: LintTripLogsRequest): LintTripLogsResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogValidationService/LintTripLogs") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LintTripLogs", request)
            log.atInfo().addKeyValue("operation", "LintTripLogs").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLog.tripLogId).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                browseNotificationsRequest {
                    this.context = request.context
                    this.filter = BrowseNotificationsRequestKt.filter {
                        this.createdAfter = request.tripLog.audit.updatedAt
                        this.ownerId = request.tripLog.counterparty.customerId
                        this.region = request.tripLog.billingAddress.region
                    }
                }
            }
            val reply0 = notificationsNotificationQueryService.browseNotifications(outbound0)
            val outbound1 = previewLedgerAccountRequest {
                this.context = request.context
                this.ledgerAccount = ledgerAccount {
                    this.audit = auditInfo {
                        this.etag = request.tripLog.audit.etag
                    }
                }
            }
            val reply1 = ClientRegistry.lookup("acme.ledger.v1.LedgerAccountValidationService", LedgerAccountValidationServiceGrpcKt.LedgerAccountValidationServiceCoroutineStub::class.java).previewLedgerAccount(outbound1)
            lintTripLogsResponse {
                this.outcome = LintTripLogsResponseKt.outcome {
                    this.message = reply0.result.message
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.billingAddress = request.tripLog.billingAddress
                    this.tripLogId = request.tripLogId
                }
            }
        }

    private suspend fun handlePreviewTripLog(request: PreviewTripLogRequest): PreviewTripLogResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogValidationService/PreviewTripLog") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PreviewTripLog", request)
            log.atInfo().addKeyValue("operation", "PreviewTripLog").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLog.tripLogId).log("Recorded audit detail")
            val outbound0 = updateMetricSeriesRequest {
                this.context = request.context
                this.metricSeries = metricSeries {
                    this.address = postalAddress {
                        this.locality = request.tripLog.billingAddress.locality
                    }
                    this.owner = userRef {
                        this.displayName = request.tripLog.counterparty.displayName
                    }
                }
            }
            val reply0 = clientForPreviewTripLog0.updateMetricSeries(outbound0)
            val outbound1 = checkTelemetryReadingEligibilityRequest {
                this.context = request.context
                this.telemetryReading = telemetryReading {
                    this.subject = geoFenceRef {
                        this.displayName = reply0.metricSeries.owner.displayName
                    }
                }
            }
            val reply1 = flowOf(outbound1).map { value -> telematicsTelemetryReadingValidationService.checkTelemetryReadingEligibility(value) }.single()
            val outbound2 = resolveWorkOrdersRequest {
                this.context = request.context
            }
            val reply2 = withTimeout(5_000) { maintenanceWorkOrderQueryService.resolveWorkOrders(outbound2) }
            val configure3: PreviewTelemetryReadingRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.details = PreviewTelemetryReadingRequestKt.details {
                    this.contact = contactInfo {
                        this.email = reply0.metricSeries.owner.email
                        this.preferredChannel = request.details.contact.preferredChannel
                    }
                }
                this.telemetryReading = telemetryReading {
                    this.subject = geoFenceRef {
                        this.displayName = reply1.telemetryReading.subject.displayName
                    }
                }
            }
            val outbound3 = previewTelemetryReadingRequest(configure3)
            val reply3 = telematicsTelemetryReadingValidationService.previewTelemetryReading(outbound3)
            previewTripLogResponse {
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.details.putAll(reply1.result.detailsMap)
                    this.message = reply3.outcome.message
                    this.referenceIds.addAll(reply2.result.referenceIdsList)
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.etag = reply0.metricSeries.audit.etag
                    }
                    this.billingAddress = postalAddress {
                        this.recipient = reply0.metricSeries.address.recipient
                    }
                    this.number = request.tripLog.number
                    this.tripLogId = request.tripLogId
                }
            }
        }

    private suspend fun handleScoreTripLog(request: ScoreTripLogRequest): ScoreTripLogResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogValidationService/ScoreTripLog") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScoreTripLog", request)
            log.atInfo().addKeyValue("operation", "ScoreTripLog").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLog.tripLogId).log("Recorded audit detail")
            val outbound0 = calculateGeoFenceRequest {
                this.context = request.context
                this.details = CalculateGeoFenceRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(request.tripLog.billingAddress.linesList)
                        this.locality = request.details.destination.locality
                        this.region = request.details.destination.region
                    }
                    this.includeBreakdown = request.details.includeBreakdown
                }
            }
            val reply0 = telematicsGeoFenceCalculationService.calculateGeoFence(outbound0)
            val outbound1 = recalculateFuelTransactionsRequest {
                this.context = request.context
                this.details = RecalculateFuelTransactionsRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = request.details.destination.countryCode
                        this.locality = request.details.destination.locality
                    }
                }
                this.fuelTransaction = fuelTransaction {
                    this.subject = customerRef {
                        this.displayName = request.tripLog.counterparty.displayName
                    }
                }
            }
            val reply1 = fuelFuelTransactionCalculationService.recalculateFuelTransactions(outbound1)
            scoreTripLogResponse {
                this.breakdown = ScoreTripLogResponseKt.breakdown {
                    this.currencyCode = reply1.breakdown.currencyCode
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.geoFence.audit.updatedBy
                    }
                    this.issuedAt = request.tripLog.issuedAt
                    this.tripLogId = request.tripLogId
                }
            }
        }

    private fun ValidateTripLogRequest.toExplainTelemetryReadingDecisionFromValidateTripLog1(): ExplainTelemetryReadingDecisionRequest {
        val source = this
        return explainTelemetryReadingDecisionRequest {
            this.context = source.context
            this.details = ExplainTelemetryReadingDecisionRequestKt.details {
                this.contact = contactInfo {
                    this.email = source.details.contact.email
                    this.name = source.details.contact.name
                    this.preferredChannel = source.details.contact.preferredChannel
                }
            }
        }
    }
}
