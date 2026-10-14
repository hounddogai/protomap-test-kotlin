package com.acme.transport.telematics.api

import com.acme.authorization.v1.AccessPolicyValidationServiceGrpcKt
import com.acme.authorization.v1.CheckAccessPolicyEligibilityRequestKt
import com.acme.authorization.v1.accessPolicy
import com.acme.authorization.v1.checkAccessPolicyEligibilityRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.customers.v1.customerRef
import com.acme.documents.v1.DocumentServiceGrpcKt
import com.acme.documents.v1.batchCreateDocumentsRequest
import com.acme.documents.v1.document
import com.acme.documents.v1.documentFolderRef
import com.acme.fuel.v1.FuelPolicyValidationServiceGrpcKt
import com.acme.fuel.v1.LintFuelPoliciesRequest
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.fuelPolicyBlockAction
import com.acme.fuel.v1.lintFuelPoliciesRequest
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.ExportServiceSchedulesRequestKt
import com.acme.maintenance.v1.ServiceScheduleQueryServiceGrpcKt
import com.acme.maintenance.v1.exportServiceSchedulesRequest
import com.acme.search.v1.SearchIndexQueryServiceGrpcKt
import com.acme.search.v1.resolveSearchIndexesRequest
import com.acme.telematics.v1.BrowseTelemetryReadingsRequest
import com.acme.telematics.v1.BrowseTelemetryReadingsResponse
import com.acme.telematics.v1.CountTelemetryReadingsRequest
import com.acme.telematics.v1.CountTelemetryReadingsResponse
import com.acme.telematics.v1.ExplainTelemetryReadingCalculationRequestKt
import com.acme.telematics.v1.ExportTelemetryReadingsRequest
import com.acme.telematics.v1.ExportTelemetryReadingsResponse
import com.acme.telematics.v1.FindTelemetryReadingsRequest
import com.acme.telematics.v1.FindTelemetryReadingsResponse
import com.acme.telematics.v1.GeoFenceValidationServiceGrpcKt
import com.acme.telematics.v1.LookupTelemetryReadingRequest
import com.acme.telematics.v1.LookupTelemetryReadingResponse
import com.acme.telematics.v1.NormalizeTelemetryReadingRequest
import com.acme.telematics.v1.NormalizeTelemetryReadingRequestKt
import com.acme.telematics.v1.PeekTelemetryReadingRequest
import com.acme.telematics.v1.PeekTelemetryReadingResponse
import com.acme.telematics.v1.PreviewGeoFenceRequest
import com.acme.telematics.v1.QuoteTripLogResponse
import com.acme.telematics.v1.ResolveTelemetryReadingsRequest
import com.acme.telematics.v1.ResolveTelemetryReadingsResponse
import com.acme.telematics.v1.SuggestTelemetryReadingsRequest
import com.acme.telematics.v1.SuggestTelemetryReadingsResponse
import com.acme.telematics.v1.SummarizeTelemetryReadingsRequest
import com.acme.telematics.v1.SummarizeTelemetryReadingsResponse
import com.acme.telematics.v1.TelemetryReadingCalculationServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingQueryServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingValidationServiceGrpcKt
import com.acme.telematics.v1.TripLogCalculationServiceGrpcKt
import com.acme.telematics.v1.ValidateTelemetryReadingRequest
import com.acme.telematics.v1.ValidateTelemetryReadingRequestKt
import com.acme.telematics.v1.browseTelemetryReadingsResponse
import com.acme.telematics.v1.copy as telematicsCopy
import com.acme.telematics.v1.countTelemetryReadingsResponse
import com.acme.telematics.v1.explainTelemetryReadingCalculationRequest
import com.acme.telematics.v1.exportTelemetryReadingsResponse
import com.acme.telematics.v1.findTelemetryReadingsResponse
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.geoFenceRef
import com.acme.telematics.v1.geoFenceRouteAction
import com.acme.telematics.v1.lookupTelemetryReadingResponse
import com.acme.telematics.v1.normalizeTelemetryReadingRequest
import com.acme.telematics.v1.peekTelemetryReadingResponse
import com.acme.telematics.v1.previewGeoFenceRequest
import com.acme.telematics.v1.quoteTripLogRequest
import com.acme.telematics.v1.resolveTelemetryReadingsResponse
import com.acme.telematics.v1.suggestTelemetryReadingsResponse
import com.acme.telematics.v1.summarizeTelemetryReadingsResponse
import com.acme.telematics.v1.telemetryReading
import com.acme.telematics.v1.telemetryReadingMeasurement
import com.acme.telematics.v1.tripLog
import com.acme.telematics.v1.tripLogLine
import com.acme.telematics.v1.validateTelemetryReadingRequest
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.telematics.TelematicsRepository
import com.google.protobuf.Any as ProtoAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Answers read-only questions about telemetry readings, for screens and reports. */
class TelemetryReadingQueryGrpcService(
    private val store: TelematicsRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyValidationService: AccessPolicyValidationServiceGrpcKt.AccessPolicyValidationServiceCoroutineStub,
    private val documentsDocumentService: DocumentServiceGrpcKt.DocumentServiceCoroutineStub,
    private val fuelFuelPolicyValidationService: FuelPolicyValidationServiceGrpcKt.FuelPolicyValidationServiceCoroutineStub,
    private val maintenanceServiceScheduleQueryService: ServiceScheduleQueryServiceGrpcKt.ServiceScheduleQueryServiceCoroutineStub,
    private val searchSearchIndexQueryService: SearchIndexQueryServiceGrpcKt.SearchIndexQueryServiceCoroutineStub,
    private val telematicsGeoFenceValidationService: GeoFenceValidationServiceGrpcKt.GeoFenceValidationServiceCoroutineStub,
    private val telematicsTelemetryReadingCalculationService: TelemetryReadingCalculationServiceGrpcKt.TelemetryReadingCalculationServiceCoroutineStub,
    private val telematicsTelemetryReadingValidationService: TelemetryReadingValidationServiceGrpcKt.TelemetryReadingValidationServiceCoroutineStub,
    private val telematicsTripLogCalculationService: TripLogCalculationServiceGrpcKt.TripLogCalculationServiceCoroutineStub,
) : TelemetryReadingQueryServiceGrpcKt.TelemetryReadingQueryServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(TelemetryReadingQueryGrpcService::class.java)

    private val clientForCountTelemetryReadings0 by lazy { searchSearchIndexQueryService }

    /** Lists telemetry readings a page at a time. */
    override suspend fun browseTelemetryReadings(request: BrowseTelemetryReadingsRequest): BrowseTelemetryReadingsResponse {
        return handleBrowseTelemetryReadings(request)
    }

    /** Counts telemetry readings by status. */
    override suspend fun countTelemetryReadings(request: CountTelemetryReadingsRequest): CountTelemetryReadingsResponse = handleCountTelemetryReadings(request)

    /** Streams every matching telemetry reading. */
    override fun exportTelemetryReadings(request: ExportTelemetryReadingsRequest): Flow<ExportTelemetryReadingsResponse> = flow {
        emit(handleExportTelemetryReadings(request))
    }

    /** Lists telemetry readings a page at a time. */
    override suspend fun findTelemetryReadings(request: FindTelemetryReadingsRequest): FindTelemetryReadingsResponse {
        return handleFindTelemetryReadings(request)
    }

    /** Returns a telemetry reading. */
    override suspend fun lookupTelemetryReading(request: LookupTelemetryReadingRequest): LookupTelemetryReadingResponse = handleLookupTelemetryReading(request)

    /** Returns a telemetry reading. */
    override suspend fun peekTelemetryReading(request: PeekTelemetryReadingRequest): PeekTelemetryReadingResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingQueryService/PeekTelemetryReading") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PeekTelemetryReading", request)
            log.atInfo().addKeyValue("operation", "PeekTelemetryReading").log("Handling transport request")
            val outbound0 = quoteTripLogRequest {
                this.context = request.context
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.createdAt = request.context.deadline
                    }
                    this.counterparty = customerRef {
                        this.email = request.context.actor.email
                    }
                    this.metadata.putAll(request.context.baggageMap)
                }
            }
            val reply0 = telematicsTripLogCalculationService.quoteTripLog(outbound0)
            peekTelemetryReadingResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.correlationId = reply0.result.correlationId
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    /** Returns several telemetry readings at once. */
    override suspend fun resolveTelemetryReadings(request: ResolveTelemetryReadingsRequest): ResolveTelemetryReadingsResponse {
        return handleResolveTelemetryReadings(request)
    }

    /** Lists telemetry readings a page at a time. */
    override suspend fun suggestTelemetryReadings(request: SuggestTelemetryReadingsRequest): SuggestTelemetryReadingsResponse = handleSuggestTelemetryReadings(request)

    /** Summarizes a telemetry reading. */
    override suspend fun summarizeTelemetryReadings(request: SummarizeTelemetryReadingsRequest): SummarizeTelemetryReadingsResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingQueryService/SummarizeTelemetryReadings") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SummarizeTelemetryReadings", request)
            log.atInfo().addKeyValue("operation", "SummarizeTelemetryReadings").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReadingId).log("Recorded audit detail")
            val outbound0 = lintFuelPoliciesRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.block = fuelPolicyBlockAction {
                        this.reason = request.reason
                    }
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = fuelFuelPolicyValidationService.lintFuelPolicies(envelope0.unpack(LintFuelPoliciesRequest::class.java))
            val outbound1 = exportServiceSchedulesRequest {
                this.context = request.context
                this.format = request.details.format
            }
            maintenanceServiceScheduleQueryService.exportServiceSchedules(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound2 = SummarizeTelemetryReadings2Command(
                context = request.context,
                detailsContactEmail = reply0.fuelPolicy.owner.email,
                telemetryReadingCorrelationId = reply0.result.correlationId
            ).toRequest()
            val reply2 = telematicsTelemetryReadingValidationService.validateTelemetryReading(outbound2)
            val outbound3 = previewGeoFenceRequest {
                this.context = request.context
                this.geoFence = geoFence {
                    this.owner = userRef {
                        this.displayName = reply2.telemetryReading.subject.displayName
                        this.userId = reply0.fuelPolicy.owner.userId
                    }
                    this.route = geoFenceRouteAction {
                        this.weight = reply0.fuelPolicy.route.weight
                    }
                }
            }
            val command3: Submission<PreviewGeoFenceRequest> = Submission.Live(outbound3)
            val payload3 = when (val command = command3) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply3 = telematicsGeoFenceValidationService.previewGeoFence(payload3)
            summarizeTelemetryReadingsResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.details.putAll(reply3.result.detailsMap)
                    this.message = reply2.result.message
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleBrowseTelemetryReadings(request: BrowseTelemetryReadingsRequest): BrowseTelemetryReadingsResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingQueryService/BrowseTelemetryReadings") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BrowseTelemetryReadings", request)
            log.atInfo().addKeyValue("operation", "BrowseTelemetryReadings").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            browseTelemetryReadingsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleCountTelemetryReadings(request: CountTelemetryReadingsRequest): CountTelemetryReadingsResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingQueryService/CountTelemetryReadings") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CountTelemetryReadings", request)
            log.atInfo().addKeyValue("operation", "CountTelemetryReadings").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = resolveSearchIndexesRequest {
                this.context = request.context
            }
            val reply0 = clientForCountTelemetryReadings0.resolveSearchIndexes(outbound0)
            countTelemetryReadingsResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleExportTelemetryReadings(request: ExportTelemetryReadingsRequest): ExportTelemetryReadingsResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingQueryService/ExportTelemetryReadings") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExportTelemetryReadings", request)
            log.atInfo().addKeyValue("operation", "ExportTelemetryReadings").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = checkAccessPolicyEligibilityRequest {
                this.accessPolicy = accessPolicy {
                    this.owner = userRef {
                        this.userId = request.context.actor.userId
                    }
                }
                this.context = request.context
                this.details = CheckAccessPolicyEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = flowOf(outbound0).map { value -> authorizationAccessPolicyValidationService.checkAccessPolicyEligibility(value) }.single()
            exportTelemetryReadingsResponse {
                this.result = operationResult {
                    this.status = reply0.outcome.status
                }
            }
        }

    private suspend fun handleFindTelemetryReadings(request: FindTelemetryReadingsRequest): FindTelemetryReadingsResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingQueryService/FindTelemetryReadings") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "FindTelemetryReadings", request)
            log.atInfo().addKeyValue("operation", "FindTelemetryReadings").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = explainTelemetryReadingCalculationRequest {
                this.context = request.context
                this.details = ExplainTelemetryReadingCalculationRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                    this.locale = request.context.locale
                }
            }
            val reply0 = withTimeout(5_000) { telematicsTelemetryReadingCalculationService.explainTelemetryReadingCalculation(outbound0) }
            val outbound1 = batchCreateDocumentsRequest {
                this.context = request.context
                this.record = document {
                    this.counterparty = documentFolderRef {
                        this.displayName = reply0.telemetryReading.subject.displayName
                    }
                }
            }
            documentsDocumentService.batchCreateDocuments(flowOf(outbound1))
            findTelemetryReadingsResponse {
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
                this.telemetryReadings.addAll(listOf(telemetryReading {
                    this.subject = reply0.telemetryReading.subject
                }))
            }
        }

    private suspend fun handleLookupTelemetryReading(request: LookupTelemetryReadingRequest): LookupTelemetryReadingResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingQueryService/LookupTelemetryReading") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LookupTelemetryReading", request)
            log.atInfo().addKeyValue("operation", "LookupTelemetryReading").log("Handling transport request")
            val outbound0 = quoteTripLogRequest {
                this.context = request.context
                this.tripLog = tripLog {
                    this.counterparty = customerRef {
                        this.email = request.context.actor.email
                    }
                    this.dueAt = request.context.deadline
                    this.lines.addAll(listOf(tripLogLine {
                        this.attributes.putAll(request.context.baggageMap)
                    }))
                }
            }
            val reply0 = telematicsTripLogCalculationService.quoteTripLog(outbound0)
            lookupTelemetryReadingResponse {
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = reply0.result.status
                }
                this.telemetryReading = telemetryReading {
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    private fun ResolveTelemetryReadingsRequest.toNormalizeTelemetryReadingFromResolveTelemetryReadings1(reply0: QuoteTripLogResponse): NormalizeTelemetryReadingRequest {
        val source = this
        return normalizeTelemetryReadingRequest {
            this.context = source.context
            this.details = NormalizeTelemetryReadingRequestKt.details {
                this.contact = contactInfo {
                    this.email = reply0.tripLog.counterparty.email
                }
            }
            this.telemetryReading = telemetryReading {
                this.correlationId = reply0.result.correlationId
                this.measurements.addAll(listOf(telemetryReadingMeasurement {
                    this.observedAt = reply0.tripLog.audit.createdAt
                }))
                this.subject = geoFenceRef {
                    this.displayName = reply0.tripLog.counterparty.displayName
                }
            }
        }
    }

    private suspend fun handleResolveTelemetryReadings(request: ResolveTelemetryReadingsRequest): ResolveTelemetryReadingsResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingQueryService/ResolveTelemetryReadings") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResolveTelemetryReadings", request)
            log.atInfo().addKeyValue("operation", "ResolveTelemetryReadings").log("Handling transport request")
            val outbound0 = quoteTripLogRequest {}.telematicsCopy {
                this.context = request.context
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.createdAt = request.context.deadline
                    }
                    this.counterparty = customerRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = telematicsTripLogCalculationService.quoteTripLog(outbound0)
            val outbound1 = request.toNormalizeTelemetryReadingFromResolveTelemetryReadings1(reply0)
            val reply1 = telematicsTelemetryReadingValidationService.normalizeTelemetryReading(outbound1)
            resolveTelemetryReadingsResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
                this.telemetryReadings.addAll(listOf(telemetryReading {
                    this.attributes.putAll(reply1.telemetryReading.attributesMap)
                }))
            }
        }

    private suspend fun handleSuggestTelemetryReadings(request: SuggestTelemetryReadingsRequest): SuggestTelemetryReadingsResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingQueryService/SuggestTelemetryReadings") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SuggestTelemetryReadings", request)
            log.atInfo().addKeyValue("operation", "SuggestTelemetryReadings").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = exportServiceSchedulesRequest {
                this.context = request.context
                this.filter = ExportServiceSchedulesRequestKt.filter {
                    this.labels.putAll(request.filter.labelsMap)
                }
            }
            maintenanceServiceScheduleQueryService.exportServiceSchedules(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            suggestTelemetryReadingsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private data class SummarizeTelemetryReadings2Command(
        val context: RequestContext,
        val detailsContactEmail: String,
        val telemetryReadingCorrelationId: String
    ) {
        fun toRequest(): ValidateTelemetryReadingRequest {
            val command = this
            return validateTelemetryReadingRequest {
                this.context = command.context
                this.details = ValidateTelemetryReadingRequestKt.details {
                    this.contact = contactInfo {
                        this.email = command.detailsContactEmail
                    }
                }
                this.telemetryReading = telemetryReading {
                    this.correlationId = command.telemetryReadingCorrelationId
                }
            }
        }
    }
}
