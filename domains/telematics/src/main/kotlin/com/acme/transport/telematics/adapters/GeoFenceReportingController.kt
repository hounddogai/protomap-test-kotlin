package com.acme.transport.telematics.adapters

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.CheckHouseholdEligibilityRequestKt
import com.acme.customers.v1.CustomerValidationServiceGrpcKt
import com.acme.customers.v1.HouseholdValidationServiceGrpcKt
import com.acme.customers.v1.PreferenceCalculationServiceGrpcKt
import com.acme.customers.v1.QuotePreferenceRequest
import com.acme.customers.v1.QuotePreferenceResponse
import com.acme.customers.v1.checkHouseholdEligibilityRequest
import com.acme.customers.v1.customer
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.household
import com.acme.customers.v1.lintCustomersRequest
import com.acme.customers.v1.preference
import com.acme.customers.v1.quotePreferenceRequest
import com.acme.featureflags.v1.FeatureFlagValidationServiceGrpcKt
import com.acme.featureflags.v1.PreviewFeatureFlagRequestKt
import com.acme.featureflags.v1.previewFeatureFlagRequest
import com.acme.fuel.v1.FuelTransactionWorkflowServiceGrpcKt
import com.acme.fuel.v1.copy as fuelCopy
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.signalFuelTransactionWorkflowRequest
import com.acme.identity.v1.userRef
import com.acme.telematics.v1.CheckGeoFenceEligibilityRequest
import com.acme.telematics.v1.CheckGeoFenceEligibilityResponse
import com.acme.telematics.v1.CountGeoFenceTotalsRequest
import com.acme.telematics.v1.CountGeoFenceTotalsResponse
import com.acme.telematics.v1.DeleteGeoFenceReportRequest
import com.acme.telematics.v1.DeleteGeoFenceReportResponse
import com.acme.telematics.v1.ExplainTelemetryReadingDecisionRequest
import com.acme.telematics.v1.ExplainTelemetryReadingDecisionRequestKt
import com.acme.telematics.v1.ExportTelemetryReadingsRequestKt
import com.acme.telematics.v1.FetchGeoFenceReportRequest
import com.acme.telematics.v1.FetchGeoFenceReportResponse
import com.acme.telematics.v1.GenerateGeoFenceReportRequest
import com.acme.telematics.v1.GenerateGeoFenceReportResponse
import com.acme.telematics.v1.GeoFenceImportServiceGrpcKt
import com.acme.telematics.v1.GeoFenceQueryServiceGrpcKt
import com.acme.telematics.v1.GeoFenceReportingServiceGrpcKt
import com.acme.telematics.v1.GeoFenceValidationServiceGrpcKt
import com.acme.telematics.v1.ListGeoFenceReportsRequest
import com.acme.telematics.v1.ListGeoFenceReportsResponse
import com.acme.telematics.v1.ListTelemetryReadingConflictsRequestKt
import com.acme.telematics.v1.PushTelemetryReadingRequest
import com.acme.telematics.v1.ShareGeoFenceReportRequest
import com.acme.telematics.v1.ShareGeoFenceReportResponse
import com.acme.telematics.v1.ShareGeoFenceReportResponseKt
import com.acme.telematics.v1.ShareTelemetryReadingReportRequest
import com.acme.telematics.v1.ShareTelemetryReadingReportRequestKt
import com.acme.telematics.v1.StreamGeoFenceMetricsRequest
import com.acme.telematics.v1.StreamGeoFenceMetricsResponse
import com.acme.telematics.v1.SummarizeGeoFencesRequestKt
import com.acme.telematics.v1.TelemetryReadingQueryServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingReportingServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingSyncServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingValidationServiceGrpcKt
import com.acme.telematics.v1.TripLogCalculationServiceGrpcKt
import com.acme.telematics.v1.checkGeoFenceEligibilityRequest
import com.acme.telematics.v1.countGeoFenceTotalsResponse
import com.acme.telematics.v1.deleteGeoFenceReportResponse
import com.acme.telematics.v1.explainTelemetryReadingDecisionRequest
import com.acme.telematics.v1.exportTelemetryReadingsRequest
import com.acme.telematics.v1.fetchGeoFenceReportResponse
import com.acme.telematics.v1.generateGeoFenceReportResponse
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.geoFenceRef
import com.acme.telematics.v1.listGeoFenceReportsResponse
import com.acme.telematics.v1.listTelemetryReadingConflictsRequest
import com.acme.telematics.v1.mapGeoFenceImportColumnsRequest
import com.acme.telematics.v1.pushTelemetryReadingRequest
import com.acme.telematics.v1.recalculateTripLogsRequest
import com.acme.telematics.v1.shareGeoFenceReportResponse
import com.acme.telematics.v1.shareTelemetryReadingReportRequest
import com.acme.telematics.v1.simulateTripLogRequest
import com.acme.telematics.v1.streamGeoFenceMetricsResponse
import com.acme.telematics.v1.summarizeGeoFencesRequest
import com.acme.telematics.v1.telemetryReading
import com.acme.telematics.v1.tripLog
import com.acme.telematics.v1.tripLogLine
import com.acme.telematics.v1.tripLogLineTax
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
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

/** Builds and schedules reports about geo fences. */
class GeoFenceReportingController(
    private val store: TelematicsRepository,
    private val workers: CoroutineScope,
    private val customersCustomerValidationService: CustomerValidationServiceGrpcKt.CustomerValidationServiceCoroutineStub,
    private val customersHouseholdValidationService: HouseholdValidationServiceGrpcKt.HouseholdValidationServiceCoroutineStub,
    private val customersPreferenceCalculationService: PreferenceCalculationServiceGrpcKt.PreferenceCalculationServiceCoroutineStub,
    private val featureflagsFeatureFlagValidationService: FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub,
    private val fuelFuelTransactionWorkflowService: FuelTransactionWorkflowServiceGrpcKt.FuelTransactionWorkflowServiceCoroutineStub,
    private val telematicsGeoFenceImportService: GeoFenceImportServiceGrpcKt.GeoFenceImportServiceCoroutineStub,
    private val telematicsGeoFenceQueryService: GeoFenceQueryServiceGrpcKt.GeoFenceQueryServiceCoroutineStub,
    private val telematicsGeoFenceValidationService: GeoFenceValidationServiceGrpcKt.GeoFenceValidationServiceCoroutineStub,
    private val telematicsTelemetryReadingQueryService: TelemetryReadingQueryServiceGrpcKt.TelemetryReadingQueryServiceCoroutineStub,
    private val telematicsTelemetryReadingReportingService: TelemetryReadingReportingServiceGrpcKt.TelemetryReadingReportingServiceCoroutineStub,
    private val telematicsTelemetryReadingSyncService: TelemetryReadingSyncServiceGrpcKt.TelemetryReadingSyncServiceCoroutineStub,
    private val telematicsTelemetryReadingValidationService: TelemetryReadingValidationServiceGrpcKt.TelemetryReadingValidationServiceCoroutineStub,
    private val telematicsTripLogCalculationService: TripLogCalculationServiceGrpcKt.TripLogCalculationServiceCoroutineStub,
) : GeoFenceReportingServiceGrpcKt.GeoFenceReportingServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(GeoFenceReportingController::class.java)

    private val workerForCountGeoFenceTotals0 = ReplyMailbox<CheckGeoFenceEligibilityRequest, CheckGeoFenceEligibilityResponse>(workers) { payload ->
        withTimeout(5_000) { telematicsGeoFenceValidationService.checkGeoFenceEligibility(payload) }
    }

    private val clientForDeleteGeoFenceReport0 by lazy { telematicsTripLogCalculationService }

    /** Counts geo fences by status. */
    override suspend fun countGeoFenceTotals(request: CountGeoFenceTotalsRequest): CountGeoFenceTotalsResponse = handleCountGeoFenceTotals(request)

    /** Deletes a geo fence. */
    override suspend fun deleteGeoFenceReport(request: DeleteGeoFenceReportRequest): DeleteGeoFenceReportResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceReportingService/DeleteGeoFenceReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DeleteGeoFenceReport", request)
            log.atInfo().addKeyValue("operation", "DeleteGeoFenceReport").log("Handling transport request")
            val outbound0 = recalculateTripLogsRequest {
                this.context = request.context
                this.reason = request.reason
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.createdAt = request.context.deadline
                        this.etag = request.etag
                    }
                    this.counterparty = customerRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = clientForDeleteGeoFenceReport0.recalculateTripLogs(outbound0)
            val outbound1 = lintCustomersRequest {
                this.context = request.context
                this.customer = customer {
                    this.relationshipManager = userRef {
                        this.email = reply0.tripLog.counterparty.email
                    }
                }
            }
            val reply1 = flowOf(outbound1).map { value -> customersCustomerValidationService.lintCustomers(value) }.single()
            val outbound2 = summarizeGeoFencesRequest {
                this.context = request.context
                this.details = SummarizeGeoFencesRequestKt.details {
                    this.period = reply1.customer.preferences.quietHours
                }
                this.geoFenceId = request.geoFenceId
                this.reason = request.reason
            }
            val reply2 = withTimeout(5_000) { telematicsGeoFenceQueryService.summarizeGeoFences(outbound2) }
            val configure3: CheckHouseholdEligibilityRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.household = household {
                    this.children.addAll(listOf(household {
                        this.children.addAll(listOf(household {
                            this.audit = auditInfo {
                                this.createdBy = reply0.tripLog.audit.createdBy
                                this.etag = reply1.customer.audit.etag
                            }
                            this.location = postalAddress {
                                this.region = reply0.tripLog.billingAddress.region
                            }
                            this.manager = userRef {
                                this.userId = reply1.customer.relationshipManager.userId
                            }
                        }))
                    }))
                }
            }
            val outbound3 = checkHouseholdEligibilityRequest(configure3)
            val reply3 = customersHouseholdValidationService.checkHouseholdEligibility(outbound3)
            deleteGeoFenceReportResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply1.outcome.message
                    this.status = reply2.result.status
                }
            }
        }

    /** Returns a geo fence. */
    override suspend fun fetchGeoFenceReport(request: FetchGeoFenceReportRequest): FetchGeoFenceReportResponse {
        return handleFetchGeoFenceReport(request)
    }

    /** Generates a geo fence. */
    override suspend fun generateGeoFenceReport(request: GenerateGeoFenceReportRequest): GenerateGeoFenceReportResponse = handleGenerateGeoFenceReport(request)

    /** Lists geo fences a page at a time. */
    override suspend fun listGeoFenceReports(request: ListGeoFenceReportsRequest): ListGeoFenceReportsResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceReportingService/ListGeoFenceReports") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListGeoFenceReports", request)
            log.atInfo().addKeyValue("operation", "ListGeoFenceReports").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = listTelemetryReadingConflictsRequest {
                this.context = request.context
                this.filter = ListTelemetryReadingConflictsRequestKt.filter {
                    this.labels.putAll(request.filter.labelsMap)
                }
            }
            val reply0 = telematicsTelemetryReadingSyncService.listTelemetryReadingConflicts(outbound0)
            listGeoFenceReportsResponse {
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    /** Shares a geo fence. */
    override suspend fun shareGeoFenceReport(request: ShareGeoFenceReportRequest): ShareGeoFenceReportResponse {
        return handleShareGeoFenceReport(request)
    }

    /** Streams the changes of geo fences as they happen. */
    override fun streamGeoFenceMetrics(request: StreamGeoFenceMetricsRequest): Flow<StreamGeoFenceMetricsResponse> = flow {
        emit(handleStreamGeoFenceMetrics(request))
    }

    private suspend fun handleCountGeoFenceTotals(request: CountGeoFenceTotalsRequest): CountGeoFenceTotalsResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceReportingService/CountGeoFenceTotals") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CountGeoFenceTotals", request)
            log.atInfo().addKeyValue("operation", "CountGeoFenceTotals").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = checkGeoFenceEligibilityRequest {
                this.context = request.context
                this.geoFence = geoFence {
                    this.owner = userRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = workerForCountGeoFenceTotals0.submit(outbound0)
            countGeoFenceTotalsResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = reply0.outcome.status
                }
            }
        }

    private suspend fun handleFetchGeoFenceReport(request: FetchGeoFenceReportRequest): FetchGeoFenceReportResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceReportingService/FetchGeoFenceReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "FetchGeoFenceReport", request)
            log.atInfo().addKeyValue("operation", "FetchGeoFenceReport").log("Handling transport request")
            val outbound0 = mapGeoFenceImportColumnsRequest {
                this.context = request.context
                this.geoFence = geoFence {
                    this.geoFenceId = request.geoFenceId
                    this.owner = userRef {
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply0 = telematicsGeoFenceImportService.mapGeoFenceImportColumns(outbound0)
            val outbound1 = simulateTripLogRequest {
                this.context = request.context
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.createdAt = reply0.geoFence.audit.createdAt
                        this.updatedAt = reply0.geoFence.audit.updatedAt
                    }
                    this.lines.addAll(listOf(tripLogLine {
                        this.tax = tripLogLineTax {
                            this.amount = reply0.geoFence.discount.amount
                        }
                    }))
                }
            }
            val reply1 = telematicsTripLogCalculationService.simulateTripLog(outbound1)
            fetchGeoFenceReportResponse {
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.updatedAt = reply1.tripLog.audit.updatedAt
                    }
                    this.description = reply0.geoFence.description
                    this.geoFenceId = request.geoFenceId
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = reply0.outcome.status
                }
            }
        }

    private suspend fun handleGenerateGeoFenceReport(request: GenerateGeoFenceReportRequest): GenerateGeoFenceReportResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceReportingService/GenerateGeoFenceReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GenerateGeoFenceReport", request)
            log.atInfo().addKeyValue("operation", "GenerateGeoFenceReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.geoFenceId).log("Recorded audit detail")
            val outbound0 = signalFuelTransactionWorkflowRequest {}.fuelCopy {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.fuelTransaction = fuelTransaction {
                    this.subject = customerRef {
                        this.email = request.context.actor.email
                    }
                }
                this.reason = request.reason
            }
            val reply0 = fuelFuelTransactionWorkflowService.signalFuelTransactionWorkflow(outbound0)
            val outbound1 = exportTelemetryReadingsRequest {
                this.context = request.context
                this.filter = ExportTelemetryReadingsRequestKt.filter {
                    this.createdAfter = reply0.outcome.effectiveAt
                    this.labels.putAll(request.details.filtersMap)
                    this.ownerId = reply0.fuelTransaction.fuelTransactionId
                }
                this.format = request.details.format
            }
            telematicsTelemetryReadingQueryService.exportTelemetryReadings(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            generateGeoFenceReportResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.message = reply0.outcome.message
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private data class ShareGeoFenceReport2Command(
        val context: RequestContext,
        val telemetryReadingSubjectDisplayName: String
    ) {
        fun toRequest(): PushTelemetryReadingRequest {
            val command = this
            return pushTelemetryReadingRequest {
                this.context = command.context
                this.telemetryReading = telemetryReading {
                    this.subject = geoFenceRef {
                        this.displayName = command.telemetryReadingSubjectDisplayName
                    }
                }
            }
        }
    }

    private suspend fun handleShareGeoFenceReport(request: ShareGeoFenceReportRequest): ShareGeoFenceReportResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceReportingService/ShareGeoFenceReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ShareGeoFenceReport", request)
            log.atInfo().addKeyValue("operation", "ShareGeoFenceReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.templateId).log("Recorded audit detail")
            val outbound0 = explainTelemetryReadingDecisionRequest {
                this.context = request.context
                this.details = ExplainTelemetryReadingDecisionRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.details.recipient.email
                        this.name = request.details.recipient.name
                        this.phone = request.details.recipient.phone
                        this.preferredChannel = request.details.recipient.preferredChannel
                    }
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = telematicsTelemetryReadingValidationService.explainTelemetryReadingDecision(envelope0.unpack(ExplainTelemetryReadingDecisionRequest::class.java))
            val outbound1 = previewFeatureFlagRequest {
                this.context = request.context
                this.details = PreviewFeatureFlagRequestKt.details {
                    this.contact = contactInfo {
                        this.name = request.details.recipient.name
                        this.phone = request.details.recipient.phone
                    }
                }
            }
            val reply1 = featureflagsFeatureFlagValidationService.previewFeatureFlag(WireCopies.json(outbound1))
            val outbound2 = ShareGeoFenceReport2Command(
                context = request.context,
                telemetryReadingSubjectDisplayName = reply1.featureFlag.owner.displayName
            ).toRequest()
            val reply2 = telematicsTelemetryReadingSyncService.pushTelemetryReading(outbound2)
            shareGeoFenceReportResponse {
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.createdBy = reply1.featureFlag.audit.createdBy
                    }
                    this.geoFenceId = request.geoFenceId
                    this.owner = userRef {
                        this.displayName = reply2.telemetryReading.subject.displayName
                        this.email = request.details.recipient.email
                    }
                }
                this.outcome = ShareGeoFenceReportResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleStreamGeoFenceMetrics(request: StreamGeoFenceMetricsRequest): StreamGeoFenceMetricsResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceReportingService/StreamGeoFenceMetrics") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StreamGeoFenceMetrics", request)
            log.atInfo().addKeyValue("operation", "StreamGeoFenceMetrics").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = shareTelemetryReadingReportRequest {
                this.context = request.context
                this.details = ShareTelemetryReadingReportRequestKt.details {
                    this.recipient = contactInfo {
                        this.email = request.subscriber.email
                        this.name = request.subscriber.name
                        this.preferredChannel = request.subscriber.preferredChannel
                    }
                }
            }
            val command0: Submission<ShareTelemetryReadingReportRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = telematicsTelemetryReadingReportingService.shareTelemetryReadingReport(payload0)
            val outbound1 = quotePreferenceRequest {
                this.context = request.context
                this.preference = preference {
                    this.owner = userRef {
                        this.displayName = reply0.telemetryReading.subject.displayName
                        this.email = request.subscriber.email
                    }
                }
            }
            val adapter1 = OutboundPort<QuotePreferenceRequest, QuotePreferenceResponse> { payload ->
                customersPreferenceCalculationService.quotePreference(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            streamGeoFenceMetricsResponse {
                this.geoFence = geoFence {
                    this.owner = userRef {
                        this.email = request.subscriber.email
                    }
                    this.priority = reply1.preference.priority
                }
                this.result = operationResult {
                    this.correlationId = reply0.telemetryReading.correlationId
                    this.status = reply0.outcome.status
                }
            }
        }
}
