package com.acme.transport.telematics.adapters

import com.acme.audit.v1.AuditEventQueryServiceGrpcKt
import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.CountAuditEventsRequestKt
import com.acme.audit.v1.PreviewAuditEventRequest
import com.acme.audit.v1.PreviewAuditEventRequestKt
import com.acme.audit.v1.auditEvent
import com.acme.audit.v1.auditTrailRef
import com.acme.audit.v1.countAuditEventsRequest
import com.acme.audit.v1.previewAuditEventRequest
import com.acme.common.v1.PersonName
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.pageInfo
import com.acme.common.v1.pageRequest
import com.acme.concessions.v1.ConcessionStandServiceGrpcKt
import com.acme.concessions.v1.CreateConcessionStandResponse
import com.acme.concessions.v1.concessionStand
import com.acme.concessions.v1.concessionStandDimensions
import com.acme.concessions.v1.createConcessionStandRequest
import com.acme.customers.v1.CustomerSegmentValidationServiceGrpcKt
import com.acme.customers.v1.LintCustomerSegmentsRequest
import com.acme.customers.v1.LintCustomerSegmentsResponse
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.customerSegment
import com.acme.customers.v1.lintCustomerSegmentsRequest
import com.acme.deliveries.v1.ApproveDeliveryRequestKt
import com.acme.deliveries.v1.DeliveryApprovalServiceGrpcKt
import com.acme.deliveries.v1.DeliveryExceptionApprovalServiceGrpcKt
import com.acme.deliveries.v1.EscalateDeliveryExceptionRequestKt
import com.acme.deliveries.v1.approveDeliveryRequest
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.deliveryRef
import com.acme.deliveries.v1.escalateDeliveryExceptionRequest
import com.acme.documents.v1.DocumentQueryServiceGrpcKt
import com.acme.documents.v1.ExportDocumentsRequestKt
import com.acme.documents.v1.exportDocumentsRequest
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.LedgerAccountValidationServiceGrpcKt
import com.acme.ledger.v1.ValidateLedgerAccountRequest
import com.acme.ledger.v1.ValidateLedgerAccountRequestKt
import com.acme.ledger.v1.ValidateLedgerAccountResponse
import com.acme.ledger.v1.ledgerAccount
import com.acme.ledger.v1.validateLedgerAccountRequest
import com.acme.telematics.v1.CancelTelemetryReadingImportRequest
import com.acme.telematics.v1.CancelTelemetryReadingImportResponse
import com.acme.telematics.v1.CancelTelemetryReadingImportResponseKt
import com.acme.telematics.v1.CountGeoFencesRequest
import com.acme.telematics.v1.CountGeoFencesRequestKt
import com.acme.telematics.v1.CountTelemetryReadingsRequestKt
import com.acme.telematics.v1.ExplainGeoFenceCalculationRequestKt
import com.acme.telematics.v1.ExplainGeoFenceCalculationResponse
import com.acme.telematics.v1.FindTelemetryReadingsRequestKt
import com.acme.telematics.v1.GeoFenceCalculationServiceGrpcKt
import com.acme.telematics.v1.GeoFenceQueryServiceGrpcKt
import com.acme.telematics.v1.GetTelemetryReadingImportStatusRequest
import com.acme.telematics.v1.GetTelemetryReadingImportStatusResponse
import com.acme.telematics.v1.ImportTelemetryReadingsRequest
import com.acme.telematics.v1.ImportTelemetryReadingsResponse
import com.acme.telematics.v1.ListTelemetryReadingImportsRequest
import com.acme.telematics.v1.ListTelemetryReadingImportsResponse
import com.acme.telematics.v1.MapTelemetryReadingImportColumnsRequest
import com.acme.telematics.v1.MapTelemetryReadingImportColumnsResponse
import com.acme.telematics.v1.MapTelemetryReadingImportColumnsResponseKt
import com.acme.telematics.v1.RetryTelemetryReadingImportRequest
import com.acme.telematics.v1.RetryTelemetryReadingImportResponse
import com.acme.telematics.v1.RetryTelemetryReadingImportResponseKt
import com.acme.telematics.v1.SimulateTripLogRequest
import com.acme.telematics.v1.SimulateTripLogResponse
import com.acme.telematics.v1.TelemetryReadingImportServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingQueryServiceGrpcKt
import com.acme.telematics.v1.TripLogCalculationServiceGrpcKt
import com.acme.telematics.v1.ValidateTelemetryReadingImportRequest
import com.acme.telematics.v1.ValidateTelemetryReadingImportResponse
import com.acme.telematics.v1.cancelTelemetryReadingImportResponse
import com.acme.telematics.v1.countGeoFencesRequest
import com.acme.telematics.v1.countTelemetryReadingsRequest
import com.acme.telematics.v1.explainGeoFenceCalculationRequest
import com.acme.telematics.v1.findTelemetryReadingsRequest
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.geoFenceDiscountAction
import com.acme.telematics.v1.geoFenceRef
import com.acme.telematics.v1.getTelemetryReadingImportStatusResponse
import com.acme.telematics.v1.importTelemetryReadingsResponse
import com.acme.telematics.v1.listTelemetryReadingImportsResponse
import com.acme.telematics.v1.mapTelemetryReadingImportColumnsResponse
import com.acme.telematics.v1.quoteGeoFenceRequest
import com.acme.telematics.v1.recalculateGeoFencesRequest
import com.acme.telematics.v1.retryTelemetryReadingImportResponse
import com.acme.telematics.v1.simulateTripLogRequest
import com.acme.telematics.v1.telemetryReading
import com.acme.telematics.v1.tripLog
import com.acme.telematics.v1.tripLogLine
import com.acme.telematics.v1.validateTelemetryReadingImportResponse
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.transport.telematics.TelematicsRepository
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Takes telemetry readings in bulk from partners and legacy systems. */
class TelemetryReadingImportController(
    private val store: TelematicsRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventQueryService: AuditEventQueryServiceGrpcKt.AuditEventQueryServiceCoroutineStub,
    private val auditAuditEventValidationService: AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub,
    private val concessionsConcessionStandService: ConcessionStandServiceGrpcKt.ConcessionStandServiceCoroutineStub,
    private val customersCustomerSegmentValidationService: CustomerSegmentValidationServiceGrpcKt.CustomerSegmentValidationServiceCoroutineStub,
    private val deliveriesDeliveryApprovalService: DeliveryApprovalServiceGrpcKt.DeliveryApprovalServiceCoroutineStub,
    private val deliveriesDeliveryExceptionApprovalService: DeliveryExceptionApprovalServiceGrpcKt.DeliveryExceptionApprovalServiceCoroutineStub,
    private val documentsDocumentQueryService: DocumentQueryServiceGrpcKt.DocumentQueryServiceCoroutineStub,
    private val ledgerLedgerAccountValidationService: LedgerAccountValidationServiceGrpcKt.LedgerAccountValidationServiceCoroutineStub,
    private val telematicsGeoFenceCalculationService: GeoFenceCalculationServiceGrpcKt.GeoFenceCalculationServiceCoroutineStub,
    private val telematicsGeoFenceQueryService: GeoFenceQueryServiceGrpcKt.GeoFenceQueryServiceCoroutineStub,
    private val telematicsTelemetryReadingQueryService: TelemetryReadingQueryServiceGrpcKt.TelemetryReadingQueryServiceCoroutineStub,
    private val telematicsTripLogCalculationService: TripLogCalculationServiceGrpcKt.TripLogCalculationServiceCoroutineStub,
) : TelemetryReadingImportServiceGrpcKt.TelemetryReadingImportServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(TelemetryReadingImportController::class.java)

    private val invokeGetTelemetryReadingImportStatus0: suspend (SimulateTripLogRequest) -> SimulateTripLogResponse = { payload ->
        telematicsTripLogCalculationService.simulateTripLog(payload)
    }

    private val clientForMapTelemetryReadingImportColumns1 by lazy { deliveriesDeliveryApprovalService }

    /** Cancels a telemetry reading. */
    override suspend fun cancelTelemetryReadingImport(request: CancelTelemetryReadingImportRequest): CancelTelemetryReadingImportResponse = handleCancelTelemetryReadingImport(request)

    /** Returns a telemetry reading. */
    override suspend fun getTelemetryReadingImportStatus(request: GetTelemetryReadingImportStatusRequest): GetTelemetryReadingImportStatusResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingImportService/GetTelemetryReadingImportStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetTelemetryReadingImportStatus", request)
            log.atInfo().addKeyValue("operation", "GetTelemetryReadingImportStatus").log("Handling transport request")
            val outbound0 = simulateTripLogRequest {
                this.context = request.context
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.updatedAt = request.context.deadline
                    }
                    this.counterparty = customerRef {
                        this.email = request.context.actor.email
                    }
                    this.lines.addAll(listOf(tripLogLine {
                        this.attributes.putAll(request.context.baggageMap)
                    }))
                }
            }
            val reply0 = invokeGetTelemetryReadingImportStatus0(outbound0)
            getTelemetryReadingImportStatusResponse {
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    /** Takes a stream of telemetry readings and answers once at the end. */
    override suspend fun importTelemetryReadings(requests: Flow<ImportTelemetryReadingsRequest>): ImportTelemetryReadingsResponse {
        var answer = ImportTelemetryReadingsResponse.getDefaultInstance()
        requests.map { request -> handleImportTelemetryReadings(request) }.collect { answer = it }
        return answer
    }

    /** Lists telemetry readings a page at a time. */
    override suspend fun listTelemetryReadingImports(request: ListTelemetryReadingImportsRequest): ListTelemetryReadingImportsResponse = handleListTelemetryReadingImports(request)

    /** Maps a telemetry reading. */
    override suspend fun mapTelemetryReadingImportColumns(request: MapTelemetryReadingImportColumnsRequest): MapTelemetryReadingImportColumnsResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingImportService/MapTelemetryReadingImportColumns") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "MapTelemetryReadingImportColumns", request)
            log.atInfo().addKeyValue("operation", "MapTelemetryReadingImportColumns").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReading.telemetryReadingId).log("Recorded audit detail")
            var reply0 = ExplainGeoFenceCalculationResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.telemetryReading.measurementsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound0 = explainGeoFenceCalculationRequest {
                                this.context = request.context
                                this.details = ExplainGeoFenceCalculationRequestKt.details {
                                    this.contact = contactInfo {
                                        this.phone = request.details.contact.phone
                                        this.preferredChannel = request.details.contact.preferredChannel
                                    }
                                }
                                this.geoFence = geoFence {
                                    this.audit = auditInfo {
                                        this.updatedAt = item.observedAt
                                    }
                                }
                            }
                            telematicsGeoFenceCalculationService.explainGeoFenceCalculation(outbound0)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply0 = it }
                }
            }
            val outbound1 = approveDeliveryRequest {
                this.context = request.context
                this.details = ApproveDeliveryRequestKt.details {
                    this.approver = userRef {
                        this.email = request.details.contact.email
                    }
                }
            }
            val reply1 = clientForMapTelemetryReadingImportColumns1.approveDelivery(outbound1)
            mapTelemetryReadingImportColumnsResponse {
                this.outcome = MapTelemetryReadingImportColumnsResponseKt.outcome {
                    this.status = reply1.outcome.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.subject = request.telemetryReading.subject
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    /** Retrys a telemetry reading. */
    override suspend fun retryTelemetryReadingImport(request: RetryTelemetryReadingImportRequest): RetryTelemetryReadingImportResponse {
        return handleRetryTelemetryReadingImport(request)
    }

    /** Validates a telemetry reading. */
    override suspend fun validateTelemetryReadingImport(request: ValidateTelemetryReadingImportRequest): ValidateTelemetryReadingImportResponse = handleValidateTelemetryReadingImport(request)

    private data class CancelTelemetryReadingImport0Command(
        val context: RequestContext,
        val detailsContactName: PersonName,
        val auditEventSubjectDisplayName: String,
        val detailsContactEmail: String,
        val detailsContactPhone: String
    ) {
        fun toRequest(): PreviewAuditEventRequest {
            val command = this
            return previewAuditEventRequest {
                this.auditEvent = auditEvent {
                    this.subject = auditTrailRef {
                        this.displayName = command.auditEventSubjectDisplayName
                    }
                }
                this.context = command.context
                this.details = PreviewAuditEventRequestKt.details {
                    this.contact = contactInfo {
                        this.email = command.detailsContactEmail
                        this.name = command.detailsContactName
                        this.phone = command.detailsContactPhone
                    }
                }
            }
        }
    }

    private suspend fun handleCancelTelemetryReadingImport(request: CancelTelemetryReadingImportRequest): CancelTelemetryReadingImportResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingImportService/CancelTelemetryReadingImport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CancelTelemetryReadingImport", request)
            log.atInfo().addKeyValue("operation", "CancelTelemetryReadingImport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = CancelTelemetryReadingImport0Command(
                context = request.context,
                detailsContactName = request.details.notifyContact.name,
                auditEventSubjectDisplayName = request.details.newOwner.displayName,
                detailsContactEmail = request.details.newOwner.email,
                detailsContactPhone = request.details.notifyContact.phone
            ).toRequest()
            val reply0 = auditAuditEventValidationService.previewAuditEvent(outbound0)
            val outbound1 = exportDocumentsRequest {
                this.context = request.context
                this.filter = ExportDocumentsRequestKt.filter {
                    this.createdAfter = reply0.outcome.effectiveAt
                    this.labels.putAll(reply0.result.detailsMap)
                    this.ownerId = reply0.result.correlationId
                }
            }
            documentsDocumentQueryService.exportDocuments(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound2 = validateLedgerAccountRequest {
                this.context = request.context
                this.details = ValidateLedgerAccountRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.details.notifyContact.email
                    }
                }
                this.ledgerAccount = ledgerAccount {
                    this.contact = contactInfo {
                        this.email = request.details.newOwner.email
                        this.name = request.details.notifyContact.name
                    }
                }
            }
            val adapter2 = OutboundPort<ValidateLedgerAccountRequest, ValidateLedgerAccountResponse> { payload ->
                ledgerLedgerAccountValidationService.validateLedgerAccount(payload)
            }
            val port2 = DelegatingPort(adapter2)
            val reply2 = port2.execute(outbound2)
            cancelTelemetryReadingImportResponse {
                this.outcome = CancelTelemetryReadingImportResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply2.result.detailsMap)
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    private suspend fun handleImportTelemetryReadings(request: ImportTelemetryReadingsRequest): ImportTelemetryReadingsResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingImportService/ImportTelemetryReadings") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ImportTelemetryReadings", request)
            log.atInfo().addKeyValue("operation", "ImportTelemetryReadings").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.header.source).log("Recorded audit detail")
            val outbound0 = lintCustomerSegmentsRequest {
                this.context = request.context
                this.customerSegment = customerSegment {
                    this.owner = userRef {
                        this.displayName = request.record.subject.displayName
                    }
                }
            }
            val invoke0: suspend (LintCustomerSegmentsRequest, Metadata) -> LintCustomerSegmentsResponse = customersCustomerSegmentValidationService::lintCustomerSegments
            val reply0 = invoke0(outbound0, Metadata())
            val outbound1 = CountGeoFencesRequest.newBuilder().apply {
                setContext(request.context)
                setFilter(CountGeoFencesRequestKt.filter {
                    this.createdAfter = request.record.occurredAt
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> telematicsGeoFenceQueryService.countGeoFences(value) }
            val outbound2 = RequestFactory.create {
                recalculateGeoFencesRequest {
                    this.context = request.context
                    this.geoFence = geoFence {
                        this.audit = auditInfo {
                            this.createdBy = reply0.customerSegment.audit.createdBy
                        }
                        this.discount = geoFenceDiscountAction {
                            this.percent = reply0.customerSegment.discount.percent
                        }
                        this.owner = userRef {
                            this.userId = reply0.customerSegment.owner.userId
                        }
                    }
                }
            }
            val reply2 = telematicsGeoFenceCalculationService.recalculateGeoFences(outbound2)
            val outbound3 = countAuditEventsRequest {
                this.context = request.context
                this.filter = CountAuditEventsRequestKt.filter {
                    this.createdAfter = reply2.geoFence.audit.createdAt
                    this.labels.putAll(reply1.result.detailsMap)
                    this.ownerId = reply1.result.correlationId
                }
            }
            val reply3 = ClientRegistry.lookup("acme.audit.v1.AuditEventQueryService", AuditEventQueryServiceGrpcKt.AuditEventQueryServiceCoroutineStub::class.java).countAuditEvents(outbound3)
            importTelemetryReadingsResponse {
                this.result = operationResult {
                    this.correlationId = reply3.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply2.result.message
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = reply0.outcome.status
                }
            }
        }

    private suspend fun handleListTelemetryReadingImports(request: ListTelemetryReadingImportsRequest): ListTelemetryReadingImportsResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingImportService/ListTelemetryReadingImports") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListTelemetryReadingImports", request)
            log.atInfo().addKeyValue("operation", "ListTelemetryReadingImports").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = findTelemetryReadingsRequest {
                this.context = request.context
                this.filter = FindTelemetryReadingsRequestKt.filter {
                    this.labels.putAll(request.filter.labelsMap)
                    this.region = request.filter.region
                }
                this.page = pageRequest {
                    this.pageSize = request.page.pageSize
                    this.pageToken = request.page.pageToken
                }
            }
            val reply0 = telematicsTelemetryReadingQueryService.findTelemetryReadings(WireCopies.reflective(outbound0))
            listTelemetryReadingImportsResponse {
                this.pageInfo = pageInfo {
                    this.totalSize = reply0.pageInfo.totalSize
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleRetryTelemetryReadingImport(request: RetryTelemetryReadingImportRequest): RetryTelemetryReadingImportResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingImportService/RetryTelemetryReadingImport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RetryTelemetryReadingImport", request)
            log.atInfo().addKeyValue("operation", "RetryTelemetryReadingImport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReading.telemetryReadingId).log("Recorded audit detail")
            var reply0 = CreateConcessionStandResponse.getDefaultInstance()
            request.telemetryReading.measurementsList.asFlow().map { item ->
                val outbound0 = createConcessionStandRequest {
                    this.concessionStand = concessionStand {
                        this.dimensions = concessionStandDimensions {
                            this.unit = item.unit
                        }
                    }
                    this.context = request.context
                }
                concessionsConcessionStandService.createConcessionStand(outbound0)
            }.collect { reply0 = it }
            val outbound1 = countTelemetryReadingsRequest {
                this.context = request.context
                this.filter = CountTelemetryReadingsRequestKt.filter {
                    this.createdAfter = reply0.concessionStand.audit.createdAt
                    this.region = reply0.concessionStand.address.region
                }
            }
            val reply1 = withTimeout(5_000) { telematicsTelemetryReadingQueryService.countTelemetryReadings(outbound1) }
            val configure2: EscalateDeliveryExceptionRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.audit = auditInfo {
                        this.createdAt = reply0.concessionStand.audit.createdAt
                    }
                    this.customer = deliveryRef {
                        this.displayName = request.telemetryReading.subject.displayName
                    }
                }
            }
            val outbound2 = escalateDeliveryExceptionRequest(configure2)
            val reply2 = deliveriesDeliveryExceptionApprovalService.escalateDeliveryException(outbound2)
            retryTelemetryReadingImportResponse {
                this.outcome = RetryTelemetryReadingImportResponseKt.outcome {
                    this.message = reply2.outcome.message
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.correlationId = request.telemetryReading.correlationId
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.correlationId = reply1.result.correlationId
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    private suspend fun handleValidateTelemetryReadingImport(request: ValidateTelemetryReadingImportRequest): ValidateTelemetryReadingImportResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingImportService/ValidateTelemetryReadingImport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ValidateTelemetryReadingImport", request)
            log.atInfo().addKeyValue("operation", "ValidateTelemetryReadingImport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReading.telemetryReadingId).log("Recorded audit detail")
            val outbound0 = quoteGeoFenceRequest {
                this.context = request.context
                this.geoFence = geoFence {
                    this.geoFenceId = request.telemetryReading.subject.geoFenceId
                    this.owner = userRef {
                        this.displayName = request.telemetryReading.subject.displayName
                        this.email = request.details.contact.email
                    }
                    this.version = request.telemetryReading.subject.version
                }
            }
            val reply0 = telematicsGeoFenceCalculationService.quoteGeoFence(outbound0)
            validateTelemetryReadingImportResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.kind = request.telemetryReading.kind
                    this.subject = geoFenceRef {
                        this.geoFenceId = reply0.geoFence.geoFenceId
                        this.version = reply0.geoFence.version
                    }
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }
}
