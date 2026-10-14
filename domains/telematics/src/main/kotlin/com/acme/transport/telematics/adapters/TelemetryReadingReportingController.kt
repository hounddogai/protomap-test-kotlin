package com.acme.transport.telematics.adapters

import com.acme.authorization.v1.AccessPolicyValidationServiceGrpcKt
import com.acme.authorization.v1.PreviewAccessPolicyRequestKt
import com.acme.authorization.v1.accessPolicy
import com.acme.authorization.v1.previewAccessPolicyRequest
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.pageRequest
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.CheckCustomerSegmentEligibilityResponse
import com.acme.customers.v1.ConsentRecordValidationServiceGrpcKt
import com.acme.customers.v1.CustomerSegmentValidationServiceGrpcKt
import com.acme.customers.v1.ExplainConsentRecordDecisionRequest
import com.acme.customers.v1.ExplainConsentRecordDecisionRequestKt
import com.acme.customers.v1.ExplainConsentRecordDecisionResponse
import com.acme.customers.v1.ExportHouseholdsRequestKt
import com.acme.customers.v1.HouseholdQueryServiceGrpcKt
import com.acme.customers.v1.checkCustomerSegmentEligibilityRequest
import com.acme.customers.v1.consentRecord
import com.acme.customers.v1.consentRecordMeasurement
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.customerSegment
import com.acme.customers.v1.explainConsentRecordDecisionRequest
import com.acme.customers.v1.exportHouseholdsRequest
import com.acme.identity.v1.FindUsersRequestKt
import com.acme.identity.v1.UserQueryServiceGrpcKt
import com.acme.identity.v1.copy as identityCopy
import com.acme.identity.v1.findUsersRequest
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.LedgerAccountServiceGrpcKt
import com.acme.ledger.v1.SearchLedgerAccountsRequestKt
import com.acme.ledger.v1.searchLedgerAccountsRequest
import com.acme.notifications.v1.NotificationServiceGrpcKt
import com.acme.notifications.v1.searchNotificationsRequest
import com.acme.telematics.v1.CancelTelemetryReadingImportRequest
import com.acme.telematics.v1.CancelTelemetryReadingImportRequestKt
import com.acme.telematics.v1.CountTelemetryReadingTotalsRequest
import com.acme.telematics.v1.CountTelemetryReadingTotalsResponse
import com.acme.telematics.v1.DeleteTelemetryReadingReportRequest
import com.acme.telematics.v1.DeleteTelemetryReadingReportResponse
import com.acme.telematics.v1.ExplainTelemetryReadingDecisionRequestKt
import com.acme.telematics.v1.FetchTelemetryReadingReportRequest
import com.acme.telematics.v1.FetchTelemetryReadingReportResponse
import com.acme.telematics.v1.GenerateTelemetryReadingReportRequest
import com.acme.telematics.v1.GenerateTelemetryReadingReportResponse
import com.acme.telematics.v1.GeoFenceValidationServiceGrpcKt
import com.acme.telematics.v1.GetTelemetryReadingRulesRequest
import com.acme.telematics.v1.GetTelemetryReadingRulesResponse
import com.acme.telematics.v1.LintGeoFencesRequest
import com.acme.telematics.v1.ListTelemetryReadingImportsRequestKt
import com.acme.telematics.v1.ListTelemetryReadingReportsRequest
import com.acme.telematics.v1.ListTelemetryReadingReportsResponse
import com.acme.telematics.v1.MapTripLogImportColumnsRequest
import com.acme.telematics.v1.MapTripLogImportColumnsResponse
import com.acme.telematics.v1.ScheduleTelemetryReadingReportRequest
import com.acme.telematics.v1.ScheduleTelemetryReadingReportResponse
import com.acme.telematics.v1.ShareTelemetryReadingReportRequest
import com.acme.telematics.v1.ShareTelemetryReadingReportResponse
import com.acme.telematics.v1.ShareTelemetryReadingReportResponseKt
import com.acme.telematics.v1.StreamTelemetryReadingMetricsRequest
import com.acme.telematics.v1.StreamTelemetryReadingMetricsResponse
import com.acme.telematics.v1.TelemetryReadingImportServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingReportingServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingValidationServiceGrpcKt
import com.acme.telematics.v1.TripLogCalculationServiceGrpcKt
import com.acme.telematics.v1.TripLogImportServiceGrpcKt
import com.acme.telematics.v1.TripLogValidationServiceGrpcKt
import com.acme.telematics.v1.cancelTelemetryReadingImportRequest
import com.acme.telematics.v1.countTelemetryReadingTotalsResponse
import com.acme.telematics.v1.deleteTelemetryReadingReportResponse
import com.acme.telematics.v1.explainTelemetryReadingDecisionRequest
import com.acme.telematics.v1.fetchTelemetryReadingReportResponse
import com.acme.telematics.v1.generateTelemetryReadingReportResponse
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.getTelemetryReadingRulesRequest
import com.acme.telematics.v1.lintGeoFencesRequest
import com.acme.telematics.v1.listTelemetryReadingImportsRequest
import com.acme.telematics.v1.listTelemetryReadingReportsResponse
import com.acme.telematics.v1.mapTripLogImportColumnsRequest
import com.acme.telematics.v1.previewTripLogRequest
import com.acme.telematics.v1.recalculateTripLogsRequest
import com.acme.telematics.v1.scheduleTelemetryReadingReportResponse
import com.acme.telematics.v1.shareTelemetryReadingReportResponse
import com.acme.telematics.v1.streamTelemetryReadingMetricsResponse
import com.acme.telematics.v1.telemetryReading
import com.acme.telematics.v1.tripLog
import com.acme.telematics.v1.tripLogLine
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.transport.telematics.TelematicsRepository
import com.acme.yards.v1.ListPendingYardMovesRequestKt
import com.acme.yards.v1.YardMoveApprovalServiceGrpcKt
import com.acme.yards.v1.listPendingYardMovesRequest
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Builds and schedules reports about telemetry readings. */
class TelemetryReadingReportingController(
    private val store: TelematicsRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyValidationService: AccessPolicyValidationServiceGrpcKt.AccessPolicyValidationServiceCoroutineStub,
    private val customersConsentRecordValidationService: ConsentRecordValidationServiceGrpcKt.ConsentRecordValidationServiceCoroutineStub,
    private val customersCustomerSegmentValidationService: CustomerSegmentValidationServiceGrpcKt.CustomerSegmentValidationServiceCoroutineStub,
    private val customersHouseholdQueryService: HouseholdQueryServiceGrpcKt.HouseholdQueryServiceCoroutineStub,
    private val identityUserQueryService: UserQueryServiceGrpcKt.UserQueryServiceCoroutineStub,
    private val ledgerLedgerAccountService: LedgerAccountServiceGrpcKt.LedgerAccountServiceCoroutineStub,
    private val notificationsNotificationService: NotificationServiceGrpcKt.NotificationServiceCoroutineStub,
    private val telematicsGeoFenceValidationService: GeoFenceValidationServiceGrpcKt.GeoFenceValidationServiceCoroutineStub,
    private val telematicsTelemetryReadingImportService: TelemetryReadingImportServiceGrpcKt.TelemetryReadingImportServiceCoroutineStub,
    private val telematicsTelemetryReadingValidationService: TelemetryReadingValidationServiceGrpcKt.TelemetryReadingValidationServiceCoroutineStub,
    private val telematicsTripLogCalculationService: TripLogCalculationServiceGrpcKt.TripLogCalculationServiceCoroutineStub,
    private val telematicsTripLogImportService: TripLogImportServiceGrpcKt.TripLogImportServiceCoroutineStub,
    private val telematicsTripLogValidationService: TripLogValidationServiceGrpcKt.TripLogValidationServiceCoroutineStub,
    private val yardsYardMoveApprovalService: YardMoveApprovalServiceGrpcKt.YardMoveApprovalServiceCoroutineStub,
) : TelemetryReadingReportingServiceGrpcKt.TelemetryReadingReportingServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(TelemetryReadingReportingController::class.java)

    private val invokeDeleteTelemetryReadingReport0: suspend (GetTelemetryReadingRulesRequest) -> GetTelemetryReadingRulesResponse = { payload ->
        telematicsTelemetryReadingValidationService.getTelemetryReadingRules(payload)
    }

    private val clientForListTelemetryReadingReports0 by lazy { notificationsNotificationService }

    /** Counts telemetry readings by status. */
    override suspend fun countTelemetryReadingTotals(request: CountTelemetryReadingTotalsRequest): CountTelemetryReadingTotalsResponse = handleCountTelemetryReadingTotals(request)

    /** Deletes a telemetry reading. */
    override suspend fun deleteTelemetryReadingReport(request: DeleteTelemetryReadingReportRequest): DeleteTelemetryReadingReportResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingReportingService/DeleteTelemetryReadingReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DeleteTelemetryReadingReport", request)
            log.atInfo().addKeyValue("operation", "DeleteTelemetryReadingReport").log("Handling transport request")
            val outbound0 = getTelemetryReadingRulesRequest {
                this.context = request.context
                this.telemetryReadingId = request.telemetryReadingId
            }
            val reply0 = invokeDeleteTelemetryReadingReport0(outbound0)
            val outbound1 = mapTripLogImportColumnsRequest {
                this.context = request.context
                this.reason = request.reason
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.etag = request.etag
                    }
                    this.counterparty = customerRef {
                        this.displayName = reply0.telemetryReading.subject.displayName
                    }
                    this.lines.addAll(listOf(tripLogLine {
                        this.attributes.putAll(reply0.telemetryReading.attributesMap)
                    }))
                }
            }
            val invoke1: suspend (MapTripLogImportColumnsRequest, Metadata) -> MapTripLogImportColumnsResponse = telematicsTripLogImportService::mapTripLogImportColumns
            val reply1 = invoke1(outbound1, Metadata())
            val outbound2 = CancelTelemetryReadingImportRequest.newBuilder().apply {
                setContext(request.context)
                setDetails(CancelTelemetryReadingImportRequestKt.details {
                    this.newOwner = userRef {
                        this.displayName = reply0.telemetryReading.subject.displayName
                        this.email = reply1.tripLog.counterparty.email
                    }
                })
                setTelemetryReadingId(request.telemetryReadingId)
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply2 = outbound2.let { value -> telematicsTelemetryReadingImportService.cancelTelemetryReadingImport(value) }
            val outbound3 = RequestFactory.create {
                listTelemetryReadingImportsRequest {
                    this.context = request.context
                    this.filter = ListTelemetryReadingImportsRequestKt.filter {
                        this.createdAfter = reply1.tripLog.audit.updatedAt
                        this.ownerId = reply0.telemetryReading.subject.geoFenceId
                        this.region = reply1.tripLog.billingAddress.region
                    }
                }
            }
            val reply3 = telematicsTelemetryReadingImportService.listTelemetryReadingImports(outbound3)
            deleteTelemetryReadingReportResponse {
                this.effectiveAt = reply1.outcome.effectiveAt
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply2.outcome.message
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = reply2.outcome.status
                }
            }
        }

    /** Returns a telemetry reading. */
    override suspend fun fetchTelemetryReadingReport(request: FetchTelemetryReadingReportRequest): FetchTelemetryReadingReportResponse {
        return handleFetchTelemetryReadingReport(request)
    }

    /** Generates a telemetry reading. */
    override suspend fun generateTelemetryReadingReport(request: GenerateTelemetryReadingReportRequest): GenerateTelemetryReadingReportResponse = handleGenerateTelemetryReadingReport(request)

    /** Lists telemetry readings a page at a time. */
    override suspend fun listTelemetryReadingReports(request: ListTelemetryReadingReportsRequest): ListTelemetryReadingReportsResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingReportingService/ListTelemetryReadingReports") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListTelemetryReadingReports", request)
            log.atInfo().addKeyValue("operation", "ListTelemetryReadingReports").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = searchNotificationsRequest {
                this.context = request.context
                this.page = pageRequest {
                    this.pageSize = request.page.pageSize
                }
            }
            val reply0 = clientForListTelemetryReadingReports0.searchNotifications(outbound0)
            listTelemetryReadingReportsResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = reply0.result.status
                }
            }
        }

    /** Schedules a telemetry reading. */
    override suspend fun scheduleTelemetryReadingReport(request: ScheduleTelemetryReadingReportRequest): ScheduleTelemetryReadingReportResponse {
        return handleScheduleTelemetryReadingReport(request)
    }

    /** Shares a telemetry reading. */
    override suspend fun shareTelemetryReadingReport(request: ShareTelemetryReadingReportRequest): ShareTelemetryReadingReportResponse = handleShareTelemetryReadingReport(request)

    /** Streams the changes of telemetry readings as they happen. */
    override fun streamTelemetryReadingMetrics(request: StreamTelemetryReadingMetricsRequest): Flow<StreamTelemetryReadingMetricsResponse> = flow {
        emit(handleStreamTelemetryReadingMetrics(request))
    }

    private suspend fun handleCountTelemetryReadingTotals(request: CountTelemetryReadingTotalsRequest): CountTelemetryReadingTotalsResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingReportingService/CountTelemetryReadingTotals") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CountTelemetryReadingTotals", request)
            log.atInfo().addKeyValue("operation", "CountTelemetryReadingTotals").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = explainConsentRecordDecisionRequest {
                this.consentRecord = consentRecord {
                    this.measurements.addAll(listOf(consentRecordMeasurement {
                        this.observedAt = request.filter.createdAfter
                    }))
                }
                this.context = request.context
                this.details = ExplainConsentRecordDecisionRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                    this.locale = request.context.locale
                }
            }
            val adapter0 = OutboundPort<ExplainConsentRecordDecisionRequest, ExplainConsentRecordDecisionResponse> { payload ->
                customersConsentRecordValidationService.explainConsentRecordDecision(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            countTelemetryReadingTotalsResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleFetchTelemetryReadingReport(request: FetchTelemetryReadingReportRequest): FetchTelemetryReadingReportResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingReportingService/FetchTelemetryReadingReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "FetchTelemetryReadingReport", request)
            log.atInfo().addKeyValue("operation", "FetchTelemetryReadingReport").log("Handling transport request")
            val outbound0 = searchLedgerAccountsRequest {
                this.context = request.context
                this.filter = SearchLedgerAccountsRequestKt.filter {
                    this.ownerId = request.telemetryReadingId
                }
            }
            val reply0 = ClientRegistry.lookup("acme.ledger.v1.LedgerAccountService", LedgerAccountServiceGrpcKt.LedgerAccountServiceCoroutineStub::class.java).searchLedgerAccounts(outbound0)
            fetchTelemetryReadingReportResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    private suspend fun handleGenerateTelemetryReadingReport(request: GenerateTelemetryReadingReportRequest): GenerateTelemetryReadingReportResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingReportingService/GenerateTelemetryReadingReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GenerateTelemetryReadingReport", request)
            log.atInfo().addKeyValue("operation", "GenerateTelemetryReadingReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReadingId).log("Recorded audit detail")
            val outbound0 = previewAccessPolicyRequest {
                this.accessPolicy = accessPolicy {
                    this.owner = userRef {
                        this.userId = request.context.actor.userId
                    }
                }
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.details = PreviewAccessPolicyRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
                this.reason = request.reason
            }
            val reply0 = authorizationAccessPolicyValidationService.previewAccessPolicy(WireCopies.reflective(outbound0))
            val outbound1 = exportHouseholdsRequest {
                this.context = request.context
                this.filter = ExportHouseholdsRequestKt.filter {
                    this.createdAfter = reply0.accessPolicy.audit.updatedAt
                }
                this.format = request.details.format
            }
            customersHouseholdQueryService.exportHouseholds(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            generateTelemetryReadingReportResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleScheduleTelemetryReadingReport(request: ScheduleTelemetryReadingReportRequest): ScheduleTelemetryReadingReportResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingReportingService/ScheduleTelemetryReadingReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScheduleTelemetryReadingReport", request)
            log.atInfo().addKeyValue("operation", "ScheduleTelemetryReadingReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReadingId).log("Recorded audit detail")
            val outbound0 = recalculateTripLogsRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.tripLog = tripLog {
                    this.counterparty = customerRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = flowOf(outbound0).map { value -> telematicsTripLogCalculationService.recalculateTripLogs(value) }.single()
            val outbound1 = previewTripLogRequest {
                this.context = request.context
                this.tripLog = tripLog {
                    this.billingAddress = postalAddress {
                        this.recipient = reply0.tripLog.billingAddress.recipient
                    }
                }
            }
            val reply1 = withTimeout(5_000) { telematicsTripLogValidationService.previewTripLog(outbound1) }
            scheduleTelemetryReadingReportResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.message = reply0.result.message
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleShareTelemetryReadingReport(request: ShareTelemetryReadingReportRequest): ShareTelemetryReadingReportResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingReportingService/ShareTelemetryReadingReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ShareTelemetryReadingReport", request)
            log.atInfo().addKeyValue("operation", "ShareTelemetryReadingReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.recipient.phone).log("Recorded audit detail")
            val configure0: ExplainTelemetryReadingDecisionRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.details = ExplainTelemetryReadingDecisionRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.details.recipient.email
                    }
                }
            }
            val outbound0 = explainTelemetryReadingDecisionRequest(configure0)
            val reply0 = telematicsTelemetryReadingValidationService.explainTelemetryReadingDecision(outbound0)
            shareTelemetryReadingReportResponse {
                this.outcome = ShareTelemetryReadingReportResponseKt.outcome {
                    this.message = reply0.outcome.message
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    private fun StreamTelemetryReadingMetricsRequest.toLintGeoFencesFromStreamTelemetryReadingMetrics3(reply0: CheckCustomerSegmentEligibilityResponse): LintGeoFencesRequest {
        val source = this
        return lintGeoFencesRequest {
            this.context = source.context
            this.geoFence = geoFence {
                this.audit = auditInfo {
                    this.createdAt = reply0.customerSegment.audit.createdAt
                }
            }
        }
    }

    private suspend fun handleStreamTelemetryReadingMetrics(request: StreamTelemetryReadingMetricsRequest): StreamTelemetryReadingMetricsResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingReportingService/StreamTelemetryReadingMetrics") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StreamTelemetryReadingMetrics", request)
            log.atInfo().addKeyValue("operation", "StreamTelemetryReadingMetrics").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.subscriber.phone).log("Recorded audit detail")
            val outbound0 = checkCustomerSegmentEligibilityRequest {
                this.context = request.context
                this.customerSegment = customerSegment {
                    this.owner = userRef {
                        this.email = request.subscriber.email
                    }
                }
            }
            val reply0 = customersCustomerSegmentValidationService.checkCustomerSegmentEligibility(outbound0)
            val outbound1 = listPendingYardMovesRequest {
                this.context = request.context
                this.filter = ListPendingYardMovesRequestKt.filter {
                    this.createdAfter = request.filter.createdAfter
                    this.labels.putAll(request.filter.labelsMap)
                    this.ownerId = request.filter.ownerId
                    this.query = request.filter.query
                }
            }
            val reply1 = yardsYardMoveApprovalService.listPendingYardMoves(outbound1)
            val outbound2 = findUsersRequest {}.identityCopy {
                this.context = request.context
                this.filter = FindUsersRequestKt.filter {
                    this.ownerId = request.filter.ownerId
                    this.query = request.filter.query
                    this.region = request.filter.region
                }
            }
            val reply2 = identityUserQueryService.findUsers(outbound2)
            val outbound3 = request.toLintGeoFencesFromStreamTelemetryReadingMetrics3(reply0)
            val reply3 = telematicsGeoFenceValidationService.lintGeoFences(outbound3)
            streamTelemetryReadingMetricsResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = reply1.result.status
                }
                this.telemetryReading = telemetryReading {
                    this.correlationId = reply2.result.correlationId
                }
            }
        }
}
