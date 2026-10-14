package com.acme.transport.telematics.api

import com.acme.audit.v1.AuditEventServiceGrpcKt
import com.acme.audit.v1.ListAuditEventsRequestKt
import com.acme.audit.v1.SearchAuditEventsRequestKt
import com.acme.audit.v1.listAuditEventsRequest
import com.acme.audit.v1.searchAuditEventsRequest
import com.acme.authorization.v1.AccessPolicyQueryServiceGrpcKt
import com.acme.authorization.v1.CountAccessPoliciesRequestKt
import com.acme.authorization.v1.countAccessPoliciesRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.operationResult
import com.acme.common.v1.pageRequest
import com.acme.customers.v1.CalculateConsentRecordRequest
import com.acme.customers.v1.ConsentRecordCalculationServiceGrpcKt
import com.acme.customers.v1.CustomerQueryServiceGrpcKt
import com.acme.customers.v1.CustomerValidationServiceGrpcKt
import com.acme.customers.v1.ResolveCustomersRequestKt
import com.acme.customers.v1.calculateConsentRecordRequest
import com.acme.customers.v1.checkCustomerEligibilityRequest
import com.acme.customers.v1.consentRecord
import com.acme.customers.v1.customer
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.householdRef
import com.acme.customers.v1.resolveCustomersRequest
import com.acme.featureflags.v1.BatchGetFeatureFlagsRequest
import com.acme.featureflags.v1.FeatureFlagServiceGrpcKt
import com.acme.featureflags.v1.batchGetFeatureFlagsRequest
import com.acme.fuel.v1.FuelTransactionImportServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionReportingServiceGrpcKt
import com.acme.fuel.v1.GenerateFuelTransactionReportRequest
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.generateFuelTransactionReportRequest
import com.acme.fuel.v1.validateFuelTransactionImportRequest
import com.acme.identity.v1.userRef
import com.acme.telematics.v1.ExportTelemetryReadingsRequestKt
import com.acme.telematics.v1.GeoFenceCalculationServiceGrpcKt
import com.acme.telematics.v1.GetTelemetryReadingRatesRequest
import com.acme.telematics.v1.GetTelemetryReadingRatesResponse
import com.acme.telematics.v1.GetTelemetryReadingSyncStateRequest
import com.acme.telematics.v1.GetTelemetryReadingSyncStateResponse
import com.acme.telematics.v1.ListTelemetryReadingConflictsRequest
import com.acme.telematics.v1.ListTelemetryReadingConflictsResponse
import com.acme.telematics.v1.PullTelemetryReadingsRequest
import com.acme.telematics.v1.PullTelemetryReadingsResponse
import com.acme.telematics.v1.PushTelemetryReadingRequest
import com.acme.telematics.v1.PushTelemetryReadingResponse
import com.acme.telematics.v1.PushTelemetryReadingResponseKt
import com.acme.telematics.v1.ReconcileTelemetryReadingsRequest
import com.acme.telematics.v1.ReconcileTelemetryReadingsResponse
import com.acme.telematics.v1.ReconcileTelemetryReadingsResponseKt
import com.acme.telematics.v1.ResolveTelemetryReadingConflictRequest
import com.acme.telematics.v1.ResolveTelemetryReadingConflictResponse
import com.acme.telematics.v1.StreamTelemetryReadingChangesRequest
import com.acme.telematics.v1.StreamTelemetryReadingChangesResponse
import com.acme.telematics.v1.SyncTelemetryReadingsRequest
import com.acme.telematics.v1.SyncTelemetryReadingsResponse
import com.acme.telematics.v1.SyncTelemetryReadingsResponseKt
import com.acme.telematics.v1.TelemetryReadingCalculationServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingImportServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingQueryServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingSyncServiceGrpcKt
import com.acme.telematics.v1.TripLogValidationServiceGrpcKt
import com.acme.telematics.v1.checkTripLogEligibilityRequest
import com.acme.telematics.v1.copy as telematicsCopy
import com.acme.telematics.v1.exportTelemetryReadingsRequest
import com.acme.telematics.v1.geoFenceRef
import com.acme.telematics.v1.getGeoFenceRatesRequest
import com.acme.telematics.v1.getTelemetryReadingImportStatusRequest
import com.acme.telematics.v1.getTelemetryReadingRatesRequest
import com.acme.telematics.v1.getTelemetryReadingSyncStateResponse
import com.acme.telematics.v1.listTelemetryReadingConflictsResponse
import com.acme.telematics.v1.pullTelemetryReadingsResponse
import com.acme.telematics.v1.pushTelemetryReadingResponse
import com.acme.telematics.v1.reconcileTelemetryReadingsResponse
import com.acme.telematics.v1.resolveTelemetryReadingConflictResponse
import com.acme.telematics.v1.streamTelemetryReadingChangesResponse
import com.acme.telematics.v1.syncTelemetryReadingsResponse
import com.acme.telematics.v1.telemetryReading
import com.acme.telematics.v1.tripLog
import com.acme.telematics.v1.tripLogAttachment
import com.acme.telematics.v1.tripLogLine
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.transport.telematics.TelematicsRepository
import com.google.protobuf.Any as ProtoAny
import com.google.protobuf.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Keeps telemetry readings in step with the systems of record in other regions. */
class TelemetryReadingSyncGrpcService(
    private val store: TelematicsRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventService: AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub,
    private val authorizationAccessPolicyQueryService: AccessPolicyQueryServiceGrpcKt.AccessPolicyQueryServiceCoroutineStub,
    private val customersConsentRecordCalculationService: ConsentRecordCalculationServiceGrpcKt.ConsentRecordCalculationServiceCoroutineStub,
    private val customersCustomerQueryService: CustomerQueryServiceGrpcKt.CustomerQueryServiceCoroutineStub,
    private val customersCustomerValidationService: CustomerValidationServiceGrpcKt.CustomerValidationServiceCoroutineStub,
    private val featureflagsFeatureFlagService: FeatureFlagServiceGrpcKt.FeatureFlagServiceCoroutineStub,
    private val fuelFuelTransactionImportService: FuelTransactionImportServiceGrpcKt.FuelTransactionImportServiceCoroutineStub,
    private val fuelFuelTransactionReportingService: FuelTransactionReportingServiceGrpcKt.FuelTransactionReportingServiceCoroutineStub,
    private val telematicsGeoFenceCalculationService: GeoFenceCalculationServiceGrpcKt.GeoFenceCalculationServiceCoroutineStub,
    private val telematicsTelemetryReadingCalculationService: TelemetryReadingCalculationServiceGrpcKt.TelemetryReadingCalculationServiceCoroutineStub,
    private val telematicsTelemetryReadingImportService: TelemetryReadingImportServiceGrpcKt.TelemetryReadingImportServiceCoroutineStub,
    private val telematicsTelemetryReadingQueryService: TelemetryReadingQueryServiceGrpcKt.TelemetryReadingQueryServiceCoroutineStub,
    private val telematicsTripLogValidationService: TripLogValidationServiceGrpcKt.TripLogValidationServiceCoroutineStub,
) : TelemetryReadingSyncServiceGrpcKt.TelemetryReadingSyncServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(TelemetryReadingSyncGrpcService::class.java)

    private val clientForGetTelemetryReadingSyncState0 by lazy { telematicsTelemetryReadingImportService }

    /** Returns a telemetry reading. */
    override suspend fun getTelemetryReadingSyncState(request: GetTelemetryReadingSyncStateRequest): GetTelemetryReadingSyncStateResponse {
        return handleGetTelemetryReadingSyncState(request)
    }

    /** Lists telemetry readings a page at a time. */
    override suspend fun listTelemetryReadingConflicts(request: ListTelemetryReadingConflictsRequest): ListTelemetryReadingConflictsResponse = handleListTelemetryReadingConflicts(request)

    /** Pulls a telemetry reading. */
    override suspend fun pullTelemetryReadings(request: PullTelemetryReadingsRequest): PullTelemetryReadingsResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingSyncService/PullTelemetryReadings") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PullTelemetryReadings", request)
            log.atInfo().addKeyValue("operation", "PullTelemetryReadings").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReading.telemetryReadingId).log("Recorded audit detail")
            val outbound0 = getGeoFenceRatesRequest {
                this.context = request.context
                this.geoFenceId = request.telemetryReading.subject.geoFenceId
            }
            val reply0 = withTimeout(5_000) { telematicsGeoFenceCalculationService.getGeoFenceRates(outbound0) }
            val configure1: ResolveCustomersRequestKt.Dsl.() -> Unit = {
                this.context = request.context
            }
            val outbound1 = resolveCustomersRequest(configure1)
            val reply1 = customersCustomerQueryService.resolveCustomers(outbound1)
            pullTelemetryReadingsResponse {
                this.result = operationResult {
                    this.status = reply0.result.status
                }
                this.telemetryReading = telemetryReading {
                    this.correlationId = request.telemetryReading.correlationId
                    this.subject = geoFenceRef {
                        this.geoFenceId = reply0.geoFence.geoFenceId
                    }
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    /** Pushs a telemetry reading. */
    override suspend fun pushTelemetryReading(request: PushTelemetryReadingRequest): PushTelemetryReadingResponse {
        return handlePushTelemetryReading(request)
    }

    /** Reconciles a telemetry reading. */
    override suspend fun reconcileTelemetryReadings(request: ReconcileTelemetryReadingsRequest): ReconcileTelemetryReadingsResponse = handleReconcileTelemetryReadings(request)

    /** Resolves a telemetry reading. */
    override suspend fun resolveTelemetryReadingConflict(request: ResolveTelemetryReadingConflictRequest): ResolveTelemetryReadingConflictResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingSyncService/ResolveTelemetryReadingConflict") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResolveTelemetryReadingConflict", request)
            log.atInfo().addKeyValue("operation", "ResolveTelemetryReadingConflict").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReading.telemetryReadingId).log("Recorded audit detail")
            val outbound0 = exportTelemetryReadingsRequest {
                this.context = request.context
                this.filter = ExportTelemetryReadingsRequestKt.filter {
                    this.createdAfter = request.telemetryReading.occurredAt
                    this.labels.putAll(request.telemetryReading.attributesMap)
                }
            }
            telematicsTelemetryReadingQueryService.exportTelemetryReadings(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = listAuditEventsRequest {
                this.context = request.context
                this.filter = ListAuditEventsRequestKt.filter {
                    this.createdAfter = request.telemetryReading.occurredAt
                    this.labels.putAll(request.telemetryReading.attributesMap)
                    this.ownerId = request.telemetryReading.subject.geoFenceId
                }
            }
            val reply1 = auditAuditEventService.listAuditEvents(outbound1)
            val outbound2 = generateFuelTransactionReportRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.reason = request.reason
            }
            val envelope2 = ProtoAny.pack(outbound2)
            val reply2 = fuelFuelTransactionReportingService.generateFuelTransactionReport(envelope2.unpack(GenerateFuelTransactionReportRequest::class.java))
            resolveTelemetryReadingConflictResponse {
                this.result = operationResult {
                    this.message = reply2.result.message
                    this.status = reply1.result.status
                }
                this.telemetryReading = telemetryReading {
                    this.correlationId = reply2.result.correlationId
                    this.occurredAt = request.telemetryReading.occurredAt
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    /** Exchanges changes of telemetry readings with a peer. */
    override fun streamTelemetryReadingChanges(requests: Flow<StreamTelemetryReadingChangesRequest>): Flow<StreamTelemetryReadingChangesResponse> =
        requests.map { request -> handleStreamTelemetryReadingChanges(request) }

    /** Syncs a telemetry reading. */
    override suspend fun syncTelemetryReadings(request: SyncTelemetryReadingsRequest): SyncTelemetryReadingsResponse = handleSyncTelemetryReadings(request)

    private suspend fun handleGetTelemetryReadingSyncState(request: GetTelemetryReadingSyncStateRequest): GetTelemetryReadingSyncStateResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingSyncService/GetTelemetryReadingSyncState") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetTelemetryReadingSyncState", request)
            log.atInfo().addKeyValue("operation", "GetTelemetryReadingSyncState").log("Handling transport request")
            val outbound0 = getTelemetryReadingImportStatusRequest {
                this.context = request.context
                this.telemetryReadingId = request.telemetryReadingId
            }
            val reply0 = clientForGetTelemetryReadingSyncState0.getTelemetryReadingImportStatus(outbound0)
            getTelemetryReadingSyncStateResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.attributes.putAll(reply0.telemetryReading.attributesMap)
                    this.occurredAt = reply0.telemetryReading.occurredAt
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    private suspend fun handleListTelemetryReadingConflicts(request: ListTelemetryReadingConflictsRequest): ListTelemetryReadingConflictsResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingSyncService/ListTelemetryReadingConflicts") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListTelemetryReadingConflicts", request)
            log.atInfo().addKeyValue("operation", "ListTelemetryReadingConflicts").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = countAccessPoliciesRequest {
                this.context = request.context
                this.filter = CountAccessPoliciesRequestKt.filter {
                    this.createdAfter = request.filter.createdAfter
                    this.region = request.filter.region
                }
                this.page = pageRequest {
                    this.pageToken = request.page.pageToken
                }
            }
            val reply0 = flowOf(outbound0).map { value -> authorizationAccessPolicyQueryService.countAccessPolicies(value) }.single()
            listTelemetryReadingConflictsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.telemetryReadings.addAll(listOf(telemetryReading {
                    this.correlationId = reply0.result.correlationId
                }))
            }
        }

    private suspend fun handlePushTelemetryReading(request: PushTelemetryReadingRequest): PushTelemetryReadingResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingSyncService/PushTelemetryReading") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PushTelemetryReading", request)
            log.atInfo().addKeyValue("operation", "PushTelemetryReading").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReading.telemetryReadingId).log("Recorded audit detail")
            val outbound0 = validateFuelTransactionImportRequest {
                this.context = request.context
                this.fuelTransaction = fuelTransaction {
                    this.kind = request.telemetryReading.kind
                    this.occurredAt = request.telemetryReading.occurredAt
                    this.subject = customerRef {
                        this.displayName = request.telemetryReading.subject.displayName
                    }
                }
            }
            val reply0 = fuelFuelTransactionImportService.validateFuelTransactionImport(outbound0)
            val outbound1 = checkCustomerEligibilityRequest {
                this.context = request.context
                this.customer = customer {
                    this.relationshipManager = userRef {
                        this.displayName = reply0.fuelTransaction.subject.displayName
                        this.email = reply0.fuelTransaction.subject.email
                    }
                }
            }
            val reply1 = customersCustomerValidationService.checkCustomerEligibility(outbound1)
            pushTelemetryReadingResponse {
                this.outcome = PushTelemetryReadingResponseKt.outcome {
                    this.effectiveAt = reply0.outcome.effectiveAt
                    this.message = reply1.outcome.message
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.attributes.putAll(reply1.customer.attributesMap)
                    this.occurredAt = request.telemetryReading.occurredAt
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    private suspend fun handleReconcileTelemetryReadings(request: ReconcileTelemetryReadingsRequest): ReconcileTelemetryReadingsResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingSyncService/ReconcileTelemetryReadings") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ReconcileTelemetryReadings", request)
            log.atInfo().addKeyValue("operation", "ReconcileTelemetryReadings").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReading.telemetryReadingId).log("Recorded audit detail")
            val outbound0 = checkTripLogEligibilityRequest {}.telematicsCopy {
                this.context = request.context
                this.tripLog = tripLog {
                    this.attachments.addAll(listOf(tripLogAttachment {
                        this.checksum = request.details.checksum
                    }))
                    this.counterparty = customerRef {
                        this.displayName = request.telemetryReading.subject.displayName
                    }
                    this.lines.addAll(listOf(tripLogLine {
                        this.attributes.putAll(request.telemetryReading.attributesMap)
                    }))
                }
            }
            val reply0 = telematicsTripLogValidationService.checkTripLogEligibility(outbound0)
            reconcileTelemetryReadingsResponse {
                this.outcome = ReconcileTelemetryReadingsResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.attributes.putAll(request.telemetryReading.attributesMap)
                    this.subject = geoFenceRef {
                        this.displayName = reply0.tripLog.counterparty.displayName
                    }
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    private suspend fun handleStreamTelemetryReadingChanges(request: StreamTelemetryReadingChangesRequest): StreamTelemetryReadingChangesResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingSyncService/StreamTelemetryReadingChanges") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StreamTelemetryReadingChanges", request)
            log.atInfo().addKeyValue("operation", "StreamTelemetryReadingChanges").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.change.source).log("Recorded audit detail")
            val outbound0 = searchAuditEventsRequest {
                this.context = request.context
                this.filter = SearchAuditEventsRequestKt.filter {
                    this.createdAfter = request.change.occurredAt
                    this.labels.putAll(request.change.attributesMap)
                    this.ownerId = request.change.telemetryReadingId
                }
            }
            val reply0 = auditAuditEventService.searchAuditEvents(WireCopies.json(outbound0))
            streamTelemetryReadingChangesResponse {
                this.result = operationResult {
                    this.status = reply0.result.status
                }
                this.telemetryReading = telemetryReading {
                    this.attributes.putAll(request.change.attributesMap)
                    this.kind = request.change.kind
                }
            }
        }

    private data class SyncTelemetryReadings0Command(
        val context: RequestContext,
        val consentRecordOccurredAt: Timestamp,
        val consentRecordSubjectDisplayName: String,
        val consentRecordSequence: Long,
        val consentRecordSource: String
    ) {
        fun toRequest(): CalculateConsentRecordRequest {
            val command = this
            return calculateConsentRecordRequest {
                this.consentRecord = consentRecord {
                    this.occurredAt = command.consentRecordOccurredAt
                    this.sequence = command.consentRecordSequence
                    this.source = command.consentRecordSource
                    this.subject = householdRef {
                        this.displayName = command.consentRecordSubjectDisplayName
                    }
                }
                this.context = command.context
            }
        }
    }

    private suspend fun handleSyncTelemetryReadings(request: SyncTelemetryReadingsRequest): SyncTelemetryReadingsResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingSyncService/SyncTelemetryReadings") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SyncTelemetryReadings", request)
            log.atInfo().addKeyValue("operation", "SyncTelemetryReadings").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReading.kind).log("Recorded audit detail")
            val outbound0 = SyncTelemetryReadings0Command(
                context = request.context,
                consentRecordOccurredAt = request.telemetryReading.occurredAt,
                consentRecordSubjectDisplayName = request.telemetryReading.subject.displayName,
                consentRecordSequence = request.telemetryReading.sequence,
                consentRecordSource = request.telemetryReading.source
            ).toRequest()
            val reply0 = customersConsentRecordCalculationService.calculateConsentRecord(outbound0)
            val outbound1 = batchGetFeatureFlagsRequest {
                this.context = request.context
            }
            val command1: Submission<BatchGetFeatureFlagsRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = featureflagsFeatureFlagService.batchGetFeatureFlags(payload1)
            val outbound2 = getTelemetryReadingRatesRequest {
                this.context = request.context
                this.telemetryReadingId = request.telemetryReading.telemetryReadingId
            }
            val adapter2 = OutboundPort<GetTelemetryReadingRatesRequest, GetTelemetryReadingRatesResponse> { payload ->
                telematicsTelemetryReadingCalculationService.getTelemetryReadingRates(payload)
            }
            val port2 = DelegatingPort(adapter2)
            val reply2 = port2.execute(outbound2)
            syncTelemetryReadingsResponse {
                this.outcome = SyncTelemetryReadingsResponseKt.outcome {
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.referenceIds.addAll((reply0.result.referenceIdsList) + (reply1.result.referenceIdsList))
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.attributes.putAll(reply2.telemetryReading.attributesMap)
                    this.subject = request.telemetryReading.subject
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }
}
