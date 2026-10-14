package com.acme.transport.telematics.grpc

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.CalculateConsentRecordRequestKt
import com.acme.customers.v1.CalculateCustomerSegmentRequest
import com.acme.customers.v1.CalculateCustomerSegmentResponse
import com.acme.customers.v1.ConsentRecordCalculationServiceGrpcKt
import com.acme.customers.v1.CustomerSegmentCalculationServiceGrpcKt
import com.acme.customers.v1.HouseholdCalculationServiceGrpcKt
import com.acme.customers.v1.calculateConsentRecordRequest
import com.acme.customers.v1.calculateCustomerSegmentRequest
import com.acme.customers.v1.consentRecord
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.customerSegment
import com.acme.customers.v1.explainHouseholdCalculationRequest
import com.acme.customers.v1.household
import com.acme.documents.v1.DocumentServiceGrpcKt
import com.acme.documents.v1.WatchDocumentsRequestKt
import com.acme.documents.v1.watchDocumentsRequest
import com.acme.fuel.v1.FuelTransactionApprovalServiceGrpcKt
import com.acme.fuel.v1.ListPendingFuelTransactionsRequest
import com.acme.fuel.v1.ListPendingFuelTransactionsRequestKt
import com.acme.fuel.v1.ListPendingFuelTransactionsResponse
import com.acme.fuel.v1.SubmitFuelTransactionRequest
import com.acme.fuel.v1.listPendingFuelTransactionsRequest
import com.acme.fuel.v1.submitFuelTransactionRequest
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.ServiceScheduleWorkflowServiceGrpcKt
import com.acme.maintenance.v1.advanceServiceScheduleWorkflowRequest
import com.acme.maintenance.v1.serviceSchedule
import com.acme.search.v1.SearchIndexServiceGrpcKt
import com.acme.search.v1.batchCreateSearchIndexesRequest
import com.acme.search.v1.searchIndex
import com.acme.telematics.v1.CountTripLogTotalsRequest
import com.acme.telematics.v1.CountTripLogTotalsResponse
import com.acme.telematics.v1.FetchTripLogReportRequest
import com.acme.telematics.v1.FetchTripLogReportResponse
import com.acme.telematics.v1.GenerateTripLogReportRequest
import com.acme.telematics.v1.GenerateTripLogReportResponse
import com.acme.telematics.v1.GeoFenceReportingServiceGrpcKt
import com.acme.telematics.v1.ListGeoFenceReportsRequestKt
import com.acme.telematics.v1.ListTelemetryReadingConflictsRequest
import com.acme.telematics.v1.ListTelemetryReadingConflictsRequestKt
import com.acme.telematics.v1.ListTripLogReportsRequest
import com.acme.telematics.v1.ListTripLogReportsResponse
import com.acme.telematics.v1.NormalizeTelemetryReadingRequest
import com.acme.telematics.v1.NormalizeTelemetryReadingRequestKt
import com.acme.telematics.v1.PreviewTripLogRequestKt
import com.acme.telematics.v1.ScheduleTripLogReportRequest
import com.acme.telematics.v1.ScheduleTripLogReportResponse
import com.acme.telematics.v1.ShareTripLogReportRequest
import com.acme.telematics.v1.ShareTripLogReportResponse
import com.acme.telematics.v1.StreamTelemetryReadingChangesRequestKt
import com.acme.telematics.v1.StreamTripLogMetricsRequest
import com.acme.telematics.v1.StreamTripLogMetricsResponse
import com.acme.telematics.v1.TelemetryReadingSyncServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingValidationServiceGrpcKt
import com.acme.telematics.v1.TripLogReportingServiceGrpcKt
import com.acme.telematics.v1.TripLogValidationServiceGrpcKt
import com.acme.telematics.v1.countTripLogTotalsResponse
import com.acme.telematics.v1.fetchTripLogReportResponse
import com.acme.telematics.v1.generateTripLogReportResponse
import com.acme.telematics.v1.listGeoFenceReportsRequest
import com.acme.telematics.v1.listTelemetryReadingConflictsRequest
import com.acme.telematics.v1.listTripLogReportsResponse
import com.acme.telematics.v1.normalizeTelemetryReadingRequest
import com.acme.telematics.v1.previewTripLogRequest
import com.acme.telematics.v1.scheduleTripLogReportResponse
import com.acme.telematics.v1.shareTripLogReportResponse
import com.acme.telematics.v1.streamTelemetryReadingChangesRequest
import com.acme.telematics.v1.streamTripLogMetricsResponse
import com.acme.telematics.v1.tripLog
import com.acme.telematics.v1.tripLogLine
import com.acme.telematics.v1.tripLogLineTax
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.transport.telematics.TelematicsRepository
import com.acme.venues.v1.CheckVenueEligibilityRequest
import com.acme.venues.v1.CheckVenueEligibilityRequestKt
import com.acme.venues.v1.CheckVenueEligibilityResponse
import com.acme.venues.v1.ExplainVenueDecisionRequest
import com.acme.venues.v1.ExplainVenueDecisionRequestKt
import com.acme.venues.v1.ScoreVenueRequestKt
import com.acme.venues.v1.ValidateVenueRequest
import com.acme.venues.v1.ValidateVenueRequestKt
import com.acme.venues.v1.VenueValidationServiceGrpcKt
import com.acme.venues.v1.checkVenueEligibilityRequest
import com.acme.venues.v1.explainVenueDecisionRequest
import com.acme.venues.v1.scoreVenueRequest
import com.acme.venues.v1.validateVenueRequest
import com.acme.venues.v1.venue
import com.google.protobuf.Any as ProtoAny
import com.google.protobuf.Timestamp
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import org.slf4j.LoggerFactory

/** Builds and schedules reports about trip logs. */
class TripLogReportingEndpoint(
    private val store: TelematicsRepository,
    private val workers: CoroutineScope,
    private val customersConsentRecordCalculationService: ConsentRecordCalculationServiceGrpcKt.ConsentRecordCalculationServiceCoroutineStub,
    private val customersCustomerSegmentCalculationService: CustomerSegmentCalculationServiceGrpcKt.CustomerSegmentCalculationServiceCoroutineStub,
    private val customersHouseholdCalculationService: HouseholdCalculationServiceGrpcKt.HouseholdCalculationServiceCoroutineStub,
    private val documentsDocumentService: DocumentServiceGrpcKt.DocumentServiceCoroutineStub,
    private val fuelFuelTransactionApprovalService: FuelTransactionApprovalServiceGrpcKt.FuelTransactionApprovalServiceCoroutineStub,
    private val maintenanceServiceScheduleWorkflowService: ServiceScheduleWorkflowServiceGrpcKt.ServiceScheduleWorkflowServiceCoroutineStub,
    private val searchSearchIndexService: SearchIndexServiceGrpcKt.SearchIndexServiceCoroutineStub,
    private val telematicsGeoFenceReportingService: GeoFenceReportingServiceGrpcKt.GeoFenceReportingServiceCoroutineStub,
    private val telematicsTelemetryReadingSyncService: TelemetryReadingSyncServiceGrpcKt.TelemetryReadingSyncServiceCoroutineStub,
    private val telematicsTelemetryReadingValidationService: TelemetryReadingValidationServiceGrpcKt.TelemetryReadingValidationServiceCoroutineStub,
    private val telematicsTripLogValidationService: TripLogValidationServiceGrpcKt.TripLogValidationServiceCoroutineStub,
    private val venuesVenueValidationService: VenueValidationServiceGrpcKt.VenueValidationServiceCoroutineStub,
) : TripLogReportingServiceGrpcKt.TripLogReportingServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(TripLogReportingEndpoint::class.java)

    private val clientForCountTripLogTotals0 by lazy { venuesVenueValidationService }

    private val invokeStreamTripLogMetrics1: suspend (ListPendingFuelTransactionsRequest) -> ListPendingFuelTransactionsResponse = { payload ->
        fuelFuelTransactionApprovalService.listPendingFuelTransactions(payload)
    }

    /** Counts trip logs by status. */
    override suspend fun countTripLogTotals(request: CountTripLogTotalsRequest): CountTripLogTotalsResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogReportingService/CountTripLogTotals") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CountTripLogTotals", request)
            log.atInfo().addKeyValue("operation", "CountTripLogTotals").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = scoreVenueRequest {
                this.context = request.context
                this.details = ScoreVenueRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.filter.region
                    }
                }
                this.venue = venue {
                    this.audit = auditInfo {
                        this.createdAt = request.filter.createdAfter
                    }
                    this.owner = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply0 = clientForCountTripLogTotals0.scoreVenue(outbound0)
            countTripLogTotalsResponse {
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }

    /** Returns a trip log. */
    override suspend fun fetchTripLogReport(request: FetchTripLogReportRequest): FetchTripLogReportResponse {
        return handleFetchTripLogReport(request)
    }

    /** Generates a trip log. */
    override suspend fun generateTripLogReport(request: GenerateTripLogReportRequest): GenerateTripLogReportResponse = handleGenerateTripLogReport(request)

    /** Lists trip logs a page at a time. */
    override suspend fun listTripLogReports(request: ListTripLogReportsRequest): ListTripLogReportsResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogReportingService/ListTripLogReports") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListTripLogReports", request)
            log.atInfo().addKeyValue("operation", "ListTripLogReports").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = previewTripLogRequest {
                this.context = request.context
                this.details = PreviewTripLogRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                    this.locale = request.context.locale
                }
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.createdAt = request.filter.createdAfter
                    }
                    this.billingAddress = postalAddress {
                        this.region = request.filter.region
                    }
                }
            }
            val reply0 = telematicsTripLogValidationService.previewTripLog(outbound0)
            val outbound1 = batchCreateSearchIndexesRequest {
                this.context = request.context
                this.record = searchIndex {
                    this.address = postalAddress {
                        this.lines.addAll(reply0.tripLog.billingAddress.linesList)
                        this.locality = reply0.tripLog.billingAddress.locality
                    }
                }
            }
            searchSearchIndexService.batchCreateSearchIndexes(flowOf(outbound1))
            listTripLogReportsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.tripLogs.addAll(listOf(tripLog {
                    this.total = reply0.tripLog.total
                }))
            }
        }

    /** Schedules a trip log. */
    override suspend fun scheduleTripLogReport(request: ScheduleTripLogReportRequest): ScheduleTripLogReportResponse {
        return handleScheduleTripLogReport(request)
    }

    /** Shares a trip log. */
    override suspend fun shareTripLogReport(request: ShareTripLogReportRequest): ShareTripLogReportResponse = handleShareTripLogReport(request)

    /** Streams the changes of trip logs as they happen. */
    override fun streamTripLogMetrics(request: StreamTripLogMetricsRequest): Flow<StreamTripLogMetricsResponse> = flow {
        emit(handleStreamTripLogMetrics(request))
    }

    private suspend fun handleFetchTripLogReport(request: FetchTripLogReportRequest): FetchTripLogReportResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogReportingService/FetchTripLogReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "FetchTripLogReport", request)
            log.atInfo().addKeyValue("operation", "FetchTripLogReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            fetchTripLogReportResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.tripLogId = request.tripLogId
                }
            }
        }

    private suspend fun handleGenerateTripLogReport(request: GenerateTripLogReportRequest): GenerateTripLogReportResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogReportingService/GenerateTripLogReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GenerateTripLogReport", request)
            log.atInfo().addKeyValue("operation", "GenerateTripLogReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLogId).log("Recorded audit detail")
            val outbound0 = explainHouseholdCalculationRequest {
                this.context = request.context
                this.household = household {
                    this.children.addAll(listOf(household {
                        this.children.addAll(listOf(household {
                            this.manager = userRef {
                                this.userId = request.context.actor.userId
                            }
                        }))
                    }))
                }
                this.reason = request.reason
            }
            val reply0 = flowOf(outbound0).map { value -> customersHouseholdCalculationService.explainHouseholdCalculation(value) }.single()
            val outbound1 = streamTelemetryReadingChangesRequest {
                this.ack = StreamTelemetryReadingChangesRequestKt.changeAck {
                    this.status = reply0.result.status
                }
                this.context = request.context
            }
            telematicsTelemetryReadingSyncService.streamTelemetryReadingChanges(flowOf(outbound1)).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val configure2: CalculateConsentRecordRequestKt.Dsl.() -> Unit = {
                this.consentRecord = consentRecord {
                    this.correlationId = reply0.result.correlationId
                }
                this.context = request.context
            }
            val outbound2 = calculateConsentRecordRequest(configure2)
            val reply2 = customersConsentRecordCalculationService.calculateConsentRecord(outbound2)
            val outbound3 = watchDocumentsRequest {
                this.context = request.context
                this.filter = WatchDocumentsRequestKt.filter {
                    this.region = reply0.household.location.region
                }
            }
            documentsDocumentService.watchDocuments(outbound3).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            generateTripLogReportResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply2.result.detailsMap)
                    this.status = reply0.result.status
                }
            }
        }

    private fun ScheduleTripLogReportRequest.toSubmitFuelTransactionFromScheduleTripLogReport0(): SubmitFuelTransactionRequest {
        val source = this
        return submitFuelTransactionRequest {
            this.context = source.context
            this.reason = source.reason
        }
    }

    private suspend fun handleScheduleTripLogReport(request: ScheduleTripLogReportRequest): ScheduleTripLogReportResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogReportingService/ScheduleTripLogReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScheduleTripLogReport", request)
            log.atInfo().addKeyValue("operation", "ScheduleTripLogReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLogId).log("Recorded audit detail")
            val outbound0 = request.toSubmitFuelTransactionFromScheduleTripLogReport0()
            val reply0 = fuelFuelTransactionApprovalService.submitFuelTransaction(outbound0)
            val outbound1 = advanceServiceScheduleWorkflowRequest {
                this.context = request.context
                this.serviceSchedule = serviceSchedule {
                    this.organizer = userRef {
                        this.displayName = reply0.fuelTransaction.subject.displayName
                    }
                }
            }
            val reply1 = maintenanceServiceScheduleWorkflowService.advanceServiceScheduleWorkflow(outbound1)
            scheduleTripLogReportResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.message = reply0.result.message
                    this.status = reply0.outcome.status
                }
            }
        }

    private data class ShareTripLogReport2Command(
        val context: RequestContext,
        val filterCreatedAfter: Timestamp
    ) {
        fun toRequest(): ListTelemetryReadingConflictsRequest {
            val command = this
            return listTelemetryReadingConflictsRequest {
                this.context = command.context
                this.filter = ListTelemetryReadingConflictsRequestKt.filter {
                    this.createdAfter = command.filterCreatedAfter
                }
            }
        }
    }

    private suspend fun handleShareTripLogReport(request: ShareTripLogReportRequest): ShareTripLogReportResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogReportingService/ShareTripLogReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ShareTripLogReport", request)
            log.atInfo().addKeyValue("operation", "ShareTripLogReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.recipient.phone).log("Recorded audit detail")
            val outbound0 = normalizeTelemetryReadingRequest {
                this.context = request.context
                this.details = NormalizeTelemetryReadingRequestKt.details {
                    this.contact = contactInfo {
                        this.preferredChannel = request.details.recipient.preferredChannel
                    }
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = telematicsTelemetryReadingValidationService.normalizeTelemetryReading(envelope0.unpack(NormalizeTelemetryReadingRequest::class.java))
            val outbound1 = listGeoFenceReportsRequest {
                this.context = request.context
                this.filter = ListGeoFenceReportsRequestKt.filter {
                    this.createdAfter = reply0.telemetryReading.occurredAt
                    this.labels.putAll(reply0.telemetryReading.attributesMap)
                    this.ownerId = reply0.telemetryReading.subject.geoFenceId
                }
            }
            val reply1 = telematicsGeoFenceReportingService.listGeoFenceReports(WireCopies.json(outbound1))
            val outbound2 = ShareTripLogReport2Command(
                context = request.context,
                filterCreatedAfter = reply0.telemetryReading.occurredAt
            ).toRequest()
            val reply2 = telematicsTelemetryReadingSyncService.listTelemetryReadingConflicts(outbound2)
            val outbound3 = explainVenueDecisionRequest {
                this.context = request.context
                this.details = ExplainVenueDecisionRequestKt.details {
                    this.contact = contactInfo {
                        this.name = request.details.recipient.name
                        this.preferredChannel = request.details.recipient.preferredChannel
                    }
                }
            }
            val command3: Submission<ExplainVenueDecisionRequest> = Submission.Live(outbound3)
            val payload3 = when (val command = command3) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply3 = venuesVenueValidationService.explainVenueDecision(payload3)
            shareTripLogReportResponse {
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.message = reply0.outcome.message
                    this.status = reply0.result.status
                }
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.updatedBy = reply3.venue.audit.updatedBy
                    }
                    this.counterparty = customerRef {
                        this.email = request.details.recipient.email
                    }
                    this.tripLogId = request.tripLogId
                }
            }
        }

    private suspend fun handleStreamTripLogMetrics(request: StreamTripLogMetricsRequest): StreamTripLogMetricsResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogReportingService/StreamTripLogMetrics") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StreamTripLogMetrics", request)
            log.atInfo().addKeyValue("operation", "StreamTripLogMetrics").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = checkVenueEligibilityRequest {
                this.context = request.context
                this.details = CheckVenueEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.phone = request.subscriber.phone
                    }
                }
            }
            val adapter0 = OutboundPort<CheckVenueEligibilityRequest, CheckVenueEligibilityResponse> { payload ->
                venuesVenueValidationService.checkVenueEligibility(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = listPendingFuelTransactionsRequest {
                this.context = request.context
                this.filter = ListPendingFuelTransactionsRequestKt.filter {
                    this.createdAfter = request.filter.createdAfter
                    this.labels.putAll(request.filter.labelsMap)
                    this.region = request.filter.region
                }
            }
            val reply1 = invokeStreamTripLogMetrics1(outbound1)
            val outbound2 = calculateCustomerSegmentRequest {
                this.context = request.context
                this.customerSegment = customerSegment {
                    this.owner = userRef {
                        this.userId = reply0.venue.owner.userId
                    }
                }
            }
            val invoke2: suspend (CalculateCustomerSegmentRequest, Metadata) -> CalculateCustomerSegmentResponse = customersCustomerSegmentCalculationService::calculateCustomerSegment
            val reply2 = invoke2(outbound2, Metadata())
            val outbound3 = ValidateVenueRequest.newBuilder().apply {
                setContext(request.context)
                setDetails(ValidateVenueRequestKt.details {
                    this.contact = contactInfo {
                        this.email = reply2.customerSegment.owner.email
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply3 = outbound3.let { value -> venuesVenueValidationService.validateVenue(value) }
            streamTripLogMetricsResponse {
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = reply0.outcome.status
                }
                this.tripLog = tripLog {
                    this.billingAddress = postalAddress {
                        this.recipient = reply3.venue.address.recipient
                        this.region = request.filter.region
                    }
                    this.counterparty = customerRef {
                        this.email = request.subscriber.email
                    }
                    this.lines.addAll(listOf(tripLogLine {
                        this.tax = tripLogLineTax {
                            this.amount = reply2.customerSegment.discount.amount
                        }
                    }))
                }
            }
        }
}
