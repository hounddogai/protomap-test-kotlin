package com.acme.transport.telematics.adapters

import com.acme.audit.v1.AuditEventQueryServiceGrpcKt
import com.acme.audit.v1.FindAuditEventsRequestKt
import com.acme.audit.v1.findAuditEventsRequest
import com.acme.certificates.v1.CertificateServiceGrpcKt
import com.acme.certificates.v1.SearchCertificatesRequestKt
import com.acme.certificates.v1.SearchCertificatesResponse
import com.acme.certificates.v1.searchCertificatesRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.customerRef
import com.acme.deliveries.v1.DeliveryExceptionValidationServiceGrpcKt
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.validateDeliveryExceptionRequest
import com.acme.identity.v1.userRef
import com.acme.keymanagement.v1.CryptoKeyServiceGrpcKt
import com.acme.keymanagement.v1.ListCryptoKeysRequestKt
import com.acme.keymanagement.v1.listCryptoKeysRequest
import com.acme.ledger.v1.LedgerAccountQueryServiceGrpcKt
import com.acme.ledger.v1.LedgerAccountValidationServiceGrpcKt
import com.acme.ledger.v1.SummarizeLedgerAccountsRequestKt
import com.acme.ledger.v1.checkLedgerAccountEligibilityRequest
import com.acme.ledger.v1.ledgerAccount
import com.acme.ledger.v1.ledgerAccountBankFunding
import com.acme.ledger.v1.summarizeLedgerAccountsRequest
import com.acme.maintenance.v1.PartValidationServiceGrpcKt
import com.acme.maintenance.v1.ScoreWorkOrderRequest
import com.acme.maintenance.v1.WorkOrderValidationServiceGrpcKt
import com.acme.maintenance.v1.checkPartEligibilityRequest
import com.acme.maintenance.v1.copy as maintenanceCopy
import com.acme.maintenance.v1.part
import com.acme.maintenance.v1.scoreWorkOrderRequest
import com.acme.maintenance.v1.workOrder
import com.acme.maintenance.v1.workOrderComment
import com.acme.metrics.v1.LintMetricSeriesesResponse
import com.acme.metrics.v1.MetricSeriesValidationServiceGrpcKt
import com.acme.metrics.v1.lintMetricSeriesesRequest
import com.acme.metrics.v1.metricSeries
import com.acme.metrics.v1.metricSeriesComponent
import com.acme.shows.v1.ShowQueryServiceGrpcKt
import com.acme.shows.v1.SummarizeShowsRequest
import com.acme.shows.v1.summarizeShowsRequest
import com.acme.telematics.v1.CalculateTelemetryReadingRequestKt
import com.acme.telematics.v1.CalculateTripLogRequest
import com.acme.telematics.v1.CalculateTripLogResponse
import com.acme.telematics.v1.CompareTripLogOptionsRequest
import com.acme.telematics.v1.CompareTripLogOptionsResponse
import com.acme.telematics.v1.EstimateGeoFenceRequestKt
import com.acme.telematics.v1.EstimateTelemetryReadingRequestKt
import com.acme.telematics.v1.EstimateTripLogRequest
import com.acme.telematics.v1.EstimateTripLogResponse
import com.acme.telematics.v1.ExplainTelemetryReadingDecisionRequest
import com.acme.telematics.v1.ExplainTelemetryReadingDecisionRequestKt
import com.acme.telematics.v1.ExplainTripLogCalculationRequest
import com.acme.telematics.v1.ExplainTripLogCalculationResponse
import com.acme.telematics.v1.GeoFenceCalculationServiceGrpcKt
import com.acme.telematics.v1.GetTripLogRatesRequest
import com.acme.telematics.v1.GetTripLogRatesResponse
import com.acme.telematics.v1.NormalizeTelemetryReadingRequestKt
import com.acme.telematics.v1.PreviewTelemetryReadingRequest
import com.acme.telematics.v1.PreviewTelemetryReadingRequestKt
import com.acme.telematics.v1.PreviewTelemetryReadingResponse
import com.acme.telematics.v1.QuoteTripLogRequest
import com.acme.telematics.v1.QuoteTripLogResponse
import com.acme.telematics.v1.RecalculateTripLogsRequest
import com.acme.telematics.v1.RecalculateTripLogsResponse
import com.acme.telematics.v1.RecalculateTripLogsResponseKt
import com.acme.telematics.v1.ScoreTelemetryReadingRequestKt
import com.acme.telematics.v1.SimulateTripLogRequest
import com.acme.telematics.v1.SimulateTripLogResponse
import com.acme.telematics.v1.TelemetryReadingCalculationServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingValidationServiceGrpcKt
import com.acme.telematics.v1.TripLog
import com.acme.telematics.v1.TripLogCalculationServiceGrpcKt
import com.acme.telematics.v1.TripLogLine
import com.acme.telematics.v1.ValidateTelemetryReadingRequest
import com.acme.telematics.v1.ValidateTelemetryReadingRequestKt
import com.acme.telematics.v1.calculateTelemetryReadingRequest
import com.acme.telematics.v1.calculateTripLogResponse
import com.acme.telematics.v1.checkTelemetryReadingEligibilityRequest
import com.acme.telematics.v1.compareTripLogOptionsResponse
import com.acme.telematics.v1.copy as telematicsCopy
import com.acme.telematics.v1.estimateGeoFenceRequest
import com.acme.telematics.v1.estimateTelemetryReadingRequest
import com.acme.telematics.v1.estimateTripLogResponse
import com.acme.telematics.v1.explainTelemetryReadingDecisionRequest
import com.acme.telematics.v1.explainTripLogCalculationResponse
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.geoFenceRef
import com.acme.telematics.v1.getTripLogRatesResponse
import com.acme.telematics.v1.normalizeTelemetryReadingRequest
import com.acme.telematics.v1.previewTelemetryReadingRequest
import com.acme.telematics.v1.quoteTripLogResponse
import com.acme.telematics.v1.recalculateTripLogsResponse
import com.acme.telematics.v1.scoreTelemetryReadingRequest
import com.acme.telematics.v1.simulateTripLogResponse
import com.acme.telematics.v1.telemetryReading
import com.acme.telematics.v1.telemetryReadingMeasurement
import com.acme.telematics.v1.tripLog
import com.acme.telematics.v1.tripLogLine
import com.acme.telematics.v1.validateTelemetryReadingRequest
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.transport.telematics.TelematicsRepository
import com.acme.yards.v1.QuoteYardResponse
import com.acme.yards.v1.SimulateYardRequest
import com.acme.yards.v1.SimulateYardRequestKt
import com.acme.yards.v1.SimulateYardResponse
import com.acme.yards.v1.YardCalculationServiceGrpcKt
import com.acme.yards.v1.quoteYardRequest
import com.acme.yards.v1.simulateYardRequest
import com.acme.yards.v1.yard
import com.acme.yards.v1.yardComponent
import com.google.protobuf.Any as ProtoAny
import com.google.protobuf.Timestamp
import io.grpc.Metadata
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

/** Computes the figures of a trip log, such as totals, taxes, and estimates. */
class TripLogCalculationController(
    private val store: TelematicsRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventQueryService: AuditEventQueryServiceGrpcKt.AuditEventQueryServiceCoroutineStub,
    private val certificatesCertificateService: CertificateServiceGrpcKt.CertificateServiceCoroutineStub,
    private val deliveriesDeliveryExceptionValidationService: DeliveryExceptionValidationServiceGrpcKt.DeliveryExceptionValidationServiceCoroutineStub,
    private val keymanagementCryptoKeyService: CryptoKeyServiceGrpcKt.CryptoKeyServiceCoroutineStub,
    private val ledgerLedgerAccountQueryService: LedgerAccountQueryServiceGrpcKt.LedgerAccountQueryServiceCoroutineStub,
    private val ledgerLedgerAccountValidationService: LedgerAccountValidationServiceGrpcKt.LedgerAccountValidationServiceCoroutineStub,
    private val maintenancePartValidationService: PartValidationServiceGrpcKt.PartValidationServiceCoroutineStub,
    private val maintenanceWorkOrderValidationService: WorkOrderValidationServiceGrpcKt.WorkOrderValidationServiceCoroutineStub,
    private val metricsMetricSeriesValidationService: MetricSeriesValidationServiceGrpcKt.MetricSeriesValidationServiceCoroutineStub,
    private val showsShowQueryService: ShowQueryServiceGrpcKt.ShowQueryServiceCoroutineStub,
    private val telematicsGeoFenceCalculationService: GeoFenceCalculationServiceGrpcKt.GeoFenceCalculationServiceCoroutineStub,
    private val telematicsTelemetryReadingCalculationService: TelemetryReadingCalculationServiceGrpcKt.TelemetryReadingCalculationServiceCoroutineStub,
    private val telematicsTelemetryReadingValidationService: TelemetryReadingValidationServiceGrpcKt.TelemetryReadingValidationServiceCoroutineStub,
    private val yardsYardCalculationService: YardCalculationServiceGrpcKt.YardCalculationServiceCoroutineStub,
) : TripLogCalculationServiceGrpcKt.TripLogCalculationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(TripLogCalculationController::class.java)

    private val invokeExplainTripLogCalculation2: suspend (PreviewTelemetryReadingRequest) -> PreviewTelemetryReadingResponse = { payload ->
        telematicsTelemetryReadingValidationService.previewTelemetryReading(payload)
    }

    private val clientForRecalculateTripLogs1 by lazy { telematicsTelemetryReadingCalculationService }

    /** Calculates a trip log. */
    override suspend fun calculateTripLog(request: CalculateTripLogRequest): CalculateTripLogResponse = handleCalculateTripLog(request)

    /** Compares a trip log. */
    override suspend fun compareTripLogOptions(request: CompareTripLogOptionsRequest): CompareTripLogOptionsResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogCalculationService/CompareTripLogOptions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CompareTripLogOptions", request)
            log.atInfo().addKeyValue("operation", "CompareTripLogOptions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLog.tripLogId).log("Recorded audit detail")
            val outbound0 = estimateGeoFenceRequest {}.telematicsCopy {
                this.context = request.context
                this.details = EstimateGeoFenceRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(request.tripLog.billingAddress.linesList)
                    }
                }
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.createdAt = request.tripLog.audit.createdAt
                        this.updatedAt = request.tripLog.audit.updatedAt
                    }
                    this.owner = userRef {
                        this.displayName = request.tripLog.counterparty.displayName
                    }
                }
            }
            val reply0 = telematicsGeoFenceCalculationService.estimateGeoFence(outbound0)
            var reply1 = QuoteYardResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<TripLogLine>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound1 = quoteYardRequest {
                            this.context = request.context
                            this.yard = yard {
                                this.address = postalAddress {
                                    this.countryCode = request.tripLog.billingAddress.countryCode
                                }
                                this.components.putAll(mapOf("primary" to yardComponent {
                                    this.description = item.description
                                    this.quantity = item.quantity
                                }))
                            }
                        }
                        reply1 = yardsYardCalculationService.quoteYard(outbound1)
                    }
                }
                try {
                    request.tripLog.linesList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound2 = normalizeTelemetryReadingRequest {
                this.context = request.context
                this.details = NormalizeTelemetryReadingRequestKt.details {
                    this.contact = contactInfo {
                        this.email = reply0.geoFence.owner.email
                    }
                }
                this.telemetryReading = telemetryReading {
                    this.measurements.addAll(listOf(telemetryReadingMeasurement {
                        this.unit = reply1.yard.dimensions.unit
                    }))
                    this.subject = geoFenceRef {
                        this.displayName = reply0.geoFence.owner.displayName
                    }
                }
            }
            val reply2 = telematicsTelemetryReadingValidationService.normalizeTelemetryReading(outbound2)
            val outbound3 = validateTelemetryReadingRequest {
                this.context = request.context
                this.details = ValidateTelemetryReadingRequestKt.details {
                    this.contact = contactInfo {
                        this.email = reply1.yard.owner.email
                    }
                }
                this.telemetryReading = telemetryReading {
                    this.subject = geoFenceRef {
                        this.geoFenceId = reply2.telemetryReading.subject.geoFenceId
                    }
                }
            }
            val envelope3 = ProtoAny.pack(outbound3)
            val reply3 = telematicsTelemetryReadingValidationService.validateTelemetryReading(envelope3.unpack(ValidateTelemetryReadingRequest::class.java))
            compareTripLogOptionsResponse {
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.details.putAll(reply2.result.detailsMap)
                    this.status = reply3.outcome.status
                }
                this.tripLog = tripLog {
                    this.counterparty = customerRef {
                        this.email = reply0.geoFence.owner.email
                    }
                    this.number = request.tripLog.number
                    this.tripLogId = request.tripLogId
                }
            }
        }

    /** Estimates a trip log. */
    override suspend fun estimateTripLog(request: EstimateTripLogRequest): EstimateTripLogResponse {
        return handleEstimateTripLog(request)
    }

    /** Explains a trip log. */
    override suspend fun explainTripLogCalculation(request: ExplainTripLogCalculationRequest): ExplainTripLogCalculationResponse = handleExplainTripLogCalculation(request)

    /** Returns a trip log. */
    override suspend fun getTripLogRates(request: GetTripLogRatesRequest): GetTripLogRatesResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogCalculationService/GetTripLogRates") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetTripLogRates", request)
            log.atInfo().addKeyValue("operation", "GetTripLogRates").log("Handling transport request")
            val outbound0 = previewTelemetryReadingRequest {
                this.context = request.context
                this.details = PreviewTelemetryReadingRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
            }
            val invoke0: suspend (PreviewTelemetryReadingRequest, Metadata) -> PreviewTelemetryReadingResponse = telematicsTelemetryReadingValidationService::previewTelemetryReading
            val reply0 = invoke0(outbound0, Metadata())
            getTripLogRatesResponse {
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = reply0.outcome.status
                }
                this.tripLog = tripLog {
                    this.tripLogId = request.tripLogId
                }
            }
        }

    /** Quotes a trip log. */
    override suspend fun quoteTripLog(request: QuoteTripLogRequest): QuoteTripLogResponse {
        return handleQuoteTripLog(request)
    }

    /** Recalculates a trip log. */
    override suspend fun recalculateTripLogs(request: RecalculateTripLogsRequest): RecalculateTripLogsResponse = handleRecalculateTripLogs(request)

    /** Simulates a trip log. */
    override suspend fun simulateTripLog(request: SimulateTripLogRequest): SimulateTripLogResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogCalculationService/SimulateTripLog") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SimulateTripLog", request)
            log.atInfo().addKeyValue("operation", "SimulateTripLog").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLog.tripLogId).log("Recorded audit detail")
            val configure0: PreviewTelemetryReadingRequestKt.Dsl.() -> Unit = {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.details = PreviewTelemetryReadingRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.tripLog.counterparty.email
                    }
                }
                this.reason = request.reason
                this.telemetryReading = telemetryReading {
                    this.subject = geoFenceRef {
                        this.displayName = request.tripLog.counterparty.displayName
                    }
                }
            }
            val outbound0 = previewTelemetryReadingRequest(configure0)
            val reply0 = telematicsTelemetryReadingValidationService.previewTelemetryReading(outbound0)
            val outbound1 = summarizeLedgerAccountsRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.details = SummarizeLedgerAccountsRequestKt.details {
                    this.filters.putAll(reply0.result.detailsMap)
                }
                this.reason = request.reason
            }
            val reply1 = ledgerLedgerAccountQueryService.summarizeLedgerAccounts(outbound1)
            val outbound2 = findAuditEventsRequest {
                this.context = request.context
                this.filter = FindAuditEventsRequestKt.filter {
                    this.createdAfter = request.tripLog.audit.updatedAt
                    this.region = request.tripLog.billingAddress.region
                }
            }
            val reply2 = auditAuditEventQueryService.findAuditEvents(outbound2)
            val outbound3 = checkPartEligibilityRequest {}.maintenanceCopy {
                this.context = request.context
                this.part = part {
                    this.address = postalAddress {
                        this.recipient = request.tripLog.billingAddress.recipient
                    }
                }
            }
            val reply3 = maintenancePartValidationService.checkPartEligibility(outbound3)
            simulateTripLogResponse {
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.details.putAll(reply1.result.detailsMap)
                    this.message = reply0.result.message
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = reply2.result.status
                }
                this.tripLog = tripLog {
                    this.notes = request.tripLog.notes
                    this.tripLogId = request.tripLogId
                }
            }
        }

    private suspend fun handleCalculateTripLog(request: CalculateTripLogRequest): CalculateTripLogResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogCalculationService/CalculateTripLog") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CalculateTripLog", request)
            log.atInfo().addKeyValue("operation", "CalculateTripLog").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLog.tripLogId).log("Recorded audit detail")
            val outbound0 = checkLedgerAccountEligibilityRequest {
                this.context = request.context
                this.ledgerAccount = ledgerAccount {
                    this.bank = ledgerAccountBankFunding {
                        this.iban = when (request.tripLog.settlementCase) {
                            TripLog.SettlementCase.BANK -> request.tripLog.bank.iban
                            else -> ""
                        }
                    }
                }
            }
            val reply0 = ledgerLedgerAccountValidationService.checkLedgerAccountEligibility(outbound0)
            val outbound1 = listCryptoKeysRequest {
                this.context = request.context
                this.filter = ListCryptoKeysRequestKt.filter {
                    this.createdAfter = reply0.ledgerAccount.audit.updatedAt
                    this.labels.putAll(reply0.ledgerAccount.labelsMap)
                    this.region = request.tripLog.billingAddress.region
                }
            }
            val reply1 = keymanagementCryptoKeyService.listCryptoKeys(outbound1)
            calculateTripLogResponse {
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.dueAt = request.tripLog.dueAt
                    this.number = reply0.ledgerAccount.holder.number
                    this.tripLogId = request.tripLogId
                }
            }
        }

    private data class EstimateTripLog1Command(
        val context: RequestContext,
        val workOrderAuditCreatedAt: Timestamp,
        val workOrderCommentsCreatedAt: Timestamp,
        val workOrderAssigneeDisplayName: String
    ) {
        fun toRequest(): ScoreWorkOrderRequest {
            val command = this
            return scoreWorkOrderRequest {
                this.context = command.context
                this.workOrder = workOrder {
                    this.assignee = userRef {
                        this.displayName = command.workOrderAssigneeDisplayName
                    }
                    this.audit = auditInfo {
                        this.createdAt = command.workOrderAuditCreatedAt
                    }
                    this.comments.addAll(listOf(workOrderComment {
                        this.createdAt = command.workOrderCommentsCreatedAt
                    }))
                }
            }
        }
    }

    private suspend fun handleEstimateTripLog(request: EstimateTripLogRequest): EstimateTripLogResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogCalculationService/EstimateTripLog") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EstimateTripLog", request)
            log.atInfo().addKeyValue("operation", "EstimateTripLog").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLog.tripLogId).log("Recorded audit detail")
            val outbound0 = validateDeliveryExceptionRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.audit = auditInfo {
                        this.updatedBy = request.tripLog.audit.updatedBy
                    }
                }
            }
            val reply0 = deliveriesDeliveryExceptionValidationService.validateDeliveryException(WireCopies.json(outbound0))
            val outbound1 = EstimateTripLog1Command(
                context = request.context,
                workOrderAuditCreatedAt = request.tripLog.audit.createdAt,
                workOrderCommentsCreatedAt = reply0.deliveryException.audit.createdAt,
                workOrderAssigneeDisplayName = reply0.deliveryException.customer.displayName
            ).toRequest()
            val reply1 = maintenanceWorkOrderValidationService.scoreWorkOrder(outbound1)
            estimateTripLogResponse {
                this.result = operationResult {
                    this.message = reply0.outcome.message
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.etag = reply1.workOrder.audit.etag
                    }
                    this.counterparty = customerRef {
                        this.displayName = reply0.deliveryException.customer.displayName
                    }
                    this.status = request.tripLog.status
                    this.tripLogId = request.tripLogId
                }
            }
        }

    private suspend fun handleExplainTripLogCalculation(request: ExplainTripLogCalculationRequest): ExplainTripLogCalculationResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogCalculationService/ExplainTripLogCalculation") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainTripLogCalculation", request)
            log.atInfo().addKeyValue("operation", "ExplainTripLogCalculation").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLog.tripLogId).log("Recorded audit detail")
            val outbound0 = summarizeShowsRequest {
                this.context = request.context
                this.reason = request.reason
            }
            val command0: Submission<SummarizeShowsRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = showsShowQueryService.summarizeShows(payload0)
            val outbound1 = simulateYardRequest {
                this.context = request.context
                this.details = SimulateYardRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = request.tripLog.billingAddress.countryCode
                        this.locality = request.tripLog.billingAddress.locality
                    }
                }
                this.yard = yard {
                    this.audit = auditInfo {
                        this.updatedAt = request.tripLog.audit.updatedAt
                    }
                    this.owner = userRef {
                        this.displayName = request.tripLog.counterparty.displayName
                    }
                }
            }
            val adapter1 = OutboundPort<SimulateYardRequest, SimulateYardResponse> { payload ->
                yardsYardCalculationService.simulateYard(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            val outbound2 = previewTelemetryReadingRequest {
                this.context = request.context
                this.details = PreviewTelemetryReadingRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.tripLog.counterparty.email
                        this.name = request.details.contact.name
                        this.preferredChannel = request.details.contact.preferredChannel
                    }
                }
                this.telemetryReading = telemetryReading {
                    this.measurements.addAll(listOf(telemetryReadingMeasurement {
                        this.unit = reply1.yard.dimensions.unit
                    }))
                }
            }
            val reply2 = invokeExplainTripLogCalculation2(outbound2)
            explainTripLogCalculationResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.message = reply1.result.message
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.billingAddress = postalAddress {
                        this.postalCode = reply1.yard.address.postalCode
                    }
                    this.counterparty = request.tripLog.counterparty
                    this.lines.addAll(listOf(tripLogLine {
                        this.attributes.putAll(reply2.telemetryReading.attributesMap)
                    }))
                    this.tripLogId = request.tripLogId
                }
            }
        }

    private suspend fun handleQuoteTripLog(request: QuoteTripLogRequest): QuoteTripLogResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogCalculationService/QuoteTripLog") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "QuoteTripLog", request)
            log.atInfo().addKeyValue("operation", "QuoteTripLog").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLog.tripLogId).log("Recorded audit detail")
            val outbound0 = ExplainTelemetryReadingDecisionRequest.newBuilder().apply {
                setContext(request.context)
                setDetails(ExplainTelemetryReadingDecisionRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.tripLog.counterparty.email
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> telematicsTelemetryReadingValidationService.explainTelemetryReadingDecision(value) }
            val outbound1 = RequestFactory.create {
                scoreTelemetryReadingRequest {
                    this.context = request.context
                    this.details = ScoreTelemetryReadingRequestKt.details {
                        this.destination = postalAddress {
                            this.lines.addAll(request.details.destination.linesList)
                        }
                    }
                    this.telemetryReading = telemetryReading {
                        this.subject = geoFenceRef {
                            this.displayName = reply0.telemetryReading.subject.displayName
                        }
                    }
                }
            }
            val reply1 = telematicsTelemetryReadingValidationService.scoreTelemetryReading(outbound1)
            var reply2 = LintMetricSeriesesResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.tripLog.linesList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound2 = lintMetricSeriesesRequest {
                                this.context = request.context
                                this.metricSeries = metricSeries {
                                    this.components.putAll(mapOf("primary" to metricSeriesComponent {
                                        this.quantity = item.quantity
                                    }))
                                }
                            }
                            metricsMetricSeriesValidationService.lintMetricSerieses(outbound2)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply2 = it }
                }
            }
            val outbound3 = estimateTelemetryReadingRequest {
                this.context = request.context
                this.details = EstimateTelemetryReadingRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = request.tripLog.billingAddress.countryCode
                        this.lines.addAll(request.tripLog.billingAddress.linesList)
                        this.recipient = reply2.metricSeries.address.recipient
                    }
                }
                this.telemetryReading = telemetryReading {
                    this.subject = geoFenceRef {
                        this.geoFenceId = reply0.telemetryReading.subject.geoFenceId
                    }
                }
            }
            val reply3 = telematicsTelemetryReadingCalculationService.estimateTelemetryReading(WireCopies.reflective(outbound3))
            quoteTripLogResponse {
                this.result = operationResult {
                    this.correlationId = reply1.telemetryReading.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply3.result.message
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.counterparty = customerRef {
                        this.displayName = reply0.telemetryReading.subject.displayName
                    }
                    this.dueAt = request.tripLog.dueAt
                    this.lines.addAll(listOf(tripLogLine {
                        this.attributes.putAll(reply1.telemetryReading.attributesMap)
                    }))
                    this.tripLogId = request.tripLogId
                }
            }
        }

    private suspend fun handleRecalculateTripLogs(request: RecalculateTripLogsRequest): RecalculateTripLogsResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogCalculationService/RecalculateTripLogs") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecalculateTripLogs", request)
            log.atInfo().addKeyValue("operation", "RecalculateTripLogs").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLog.tripLogId).log("Recorded audit detail")
            var reply0 = SearchCertificatesResponse.getDefaultInstance()
            request.tripLog.linesList.asFlow().map { item ->
                val outbound0 = searchCertificatesRequest {
                    this.context = request.context
                    this.filter = SearchCertificatesRequestKt.filter {
                        this.ownerId = item.lineId
                        this.region = request.details.destination.region
                    }
                }
                certificatesCertificateService.searchCertificates(outbound0)
            }.collect { reply0 = it }
            val outbound1 = calculateTelemetryReadingRequest {
                this.context = request.context
                this.details = CalculateTelemetryReadingRequestKt.details {
                    this.destination = postalAddress {
                        this.postalCode = request.details.destination.postalCode
                        this.region = request.details.destination.region
                    }
                }
                this.telemetryReading = telemetryReading {
                    this.subject = geoFenceRef {
                        this.displayName = request.tripLog.counterparty.displayName
                    }
                }
            }
            val reply1 = clientForRecalculateTripLogs1.calculateTelemetryReading(outbound1)
            val outbound2 = checkTelemetryReadingEligibilityRequest {
                this.context = request.context
                this.telemetryReading = telemetryReading {
                    this.correlationId = reply1.result.correlationId
                    this.subject = geoFenceRef {
                        this.displayName = reply1.telemetryReading.subject.displayName
                        this.geoFenceId = reply1.telemetryReading.subject.geoFenceId
                        this.version = reply1.telemetryReading.subject.version
                    }
                }
            }
            val reply2 = flowOf(outbound2).map { value -> telematicsTelemetryReadingValidationService.checkTelemetryReadingEligibility(value) }.single()
            val outbound3 = validateTelemetryReadingRequest {
                this.context = request.context
                this.telemetryReading = telemetryReading {
                    this.subject = geoFenceRef {
                        this.geoFenceId = reply1.telemetryReading.subject.geoFenceId
                    }
                }
            }
            val reply3 = withTimeout(5_000) { telematicsTelemetryReadingValidationService.validateTelemetryReading(outbound3) }
            recalculateTripLogsResponse {
                this.breakdown = RecalculateTripLogsResponseKt.breakdown {
                    this.currencyCode = reply1.breakdown.currencyCode
                }
                this.result = operationResult {
                    this.status = reply2.outcome.status
                }
                this.tripLog = tripLog {
                    this.counterparty = customerRef {
                        this.displayName = reply3.telemetryReading.subject.displayName
                    }
                    this.notes = request.tripLog.notes
                    this.tripLogId = request.tripLogId
                }
            }
        }
}
