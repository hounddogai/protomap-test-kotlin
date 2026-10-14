package com.acme.transport.telematics.api

import com.acme.certificates.v1.CertificateQueryServiceGrpcKt
import com.acme.certificates.v1.ExportCertificatesRequestKt
import com.acme.certificates.v1.exportCertificatesRequest
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.CustomerQueryServiceGrpcKt
import com.acme.customers.v1.CustomerValidationServiceGrpcKt
import com.acme.customers.v1.EstimatePreferenceRequestKt
import com.acme.customers.v1.ExplainPreferenceDecisionRequest
import com.acme.customers.v1.FindCustomersRequestKt
import com.acme.customers.v1.PreferenceCalculationServiceGrpcKt
import com.acme.customers.v1.PreferenceValidationServiceGrpcKt
import com.acme.customers.v1.customer
import com.acme.customers.v1.estimatePreferenceRequest
import com.acme.customers.v1.explainPreferenceDecisionRequest
import com.acme.customers.v1.findCustomersRequest
import com.acme.customers.v1.preference
import com.acme.customers.v1.validateCustomerRequest
import com.acme.deliveries.v1.DeliveryExceptionApprovalServiceGrpcKt
import com.acme.deliveries.v1.approveDeliveryExceptionRequest
import com.acme.deliveries.v1.deliveryException
import com.acme.featureflags.v1.FeatureFlagServiceGrpcKt
import com.acme.featureflags.v1.batchGetFeatureFlagsRequest
import com.acme.fuel.v1.FuelCardImportServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyApprovalServiceGrpcKt
import com.acme.fuel.v1.ListPendingFuelPoliciesRequestKt
import com.acme.fuel.v1.ValidateFuelCardImportRequest
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.fuelCardBankFunding
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.listPendingFuelPoliciesRequest
import com.acme.fuel.v1.recallFuelPolicyRequest
import com.acme.fuel.v1.validateFuelCardImportRequest
import com.acme.telematics.v1.GeoFenceSyncServiceGrpcKt
import com.acme.telematics.v1.GetTripLogSyncStateRequest
import com.acme.telematics.v1.GetTripLogSyncStateResponse
import com.acme.telematics.v1.ListTripLogConflictsRequest
import com.acme.telematics.v1.ListTripLogConflictsResponse
import com.acme.telematics.v1.MapTelemetryReadingImportColumnsRequestKt
import com.acme.telematics.v1.PullTripLogsRequest
import com.acme.telematics.v1.PullTripLogsResponse
import com.acme.telematics.v1.PushTripLogRequest
import com.acme.telematics.v1.PushTripLogResponse
import com.acme.telematics.v1.ReconcileTripLogsRequest
import com.acme.telematics.v1.ReconcileTripLogsResponse
import com.acme.telematics.v1.StreamTripLogChangesRequest
import com.acme.telematics.v1.StreamTripLogChangesResponse
import com.acme.telematics.v1.SyncTripLogsRequest
import com.acme.telematics.v1.SyncTripLogsResponse
import com.acme.telematics.v1.SyncTripLogsResponseKt
import com.acme.telematics.v1.TelemetryReadingImportServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingSyncServiceGrpcKt
import com.acme.telematics.v1.TripLogSyncServiceGrpcKt
import com.acme.telematics.v1.copy as telematicsCopy
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.geoFenceRef
import com.acme.telematics.v1.getTripLogSyncStateResponse
import com.acme.telematics.v1.listTripLogConflictsResponse
import com.acme.telematics.v1.mapTelemetryReadingImportColumnsRequest
import com.acme.telematics.v1.pullTripLogsResponse
import com.acme.telematics.v1.pushGeoFenceRequest
import com.acme.telematics.v1.pushTripLogResponse
import com.acme.telematics.v1.reconcileTripLogsResponse
import com.acme.telematics.v1.streamTripLogChangesResponse
import com.acme.telematics.v1.syncGeoFencesRequest
import com.acme.telematics.v1.syncTelemetryReadingsRequest
import com.acme.telematics.v1.syncTripLogsResponse
import com.acme.telematics.v1.telemetryReading
import com.acme.telematics.v1.telemetryReadingMeasurement
import com.acme.telematics.v1.tripLog
import com.acme.telematics.v1.tripLogLine
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.transport.telematics.TelematicsRepository
import com.acme.yards.v1.ShareYardMoveReportRequestKt
import com.acme.yards.v1.YardMoveReportingServiceGrpcKt
import com.acme.yards.v1.shareYardMoveReportRequest
import com.google.protobuf.Any as ProtoAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Keeps trip logs in step with the systems of record in other regions. */
class TripLogSyncGrpcService(
    private val store: TelematicsRepository,
    private val workers: CoroutineScope,
    private val certificatesCertificateQueryService: CertificateQueryServiceGrpcKt.CertificateQueryServiceCoroutineStub,
    private val customersCustomerQueryService: CustomerQueryServiceGrpcKt.CustomerQueryServiceCoroutineStub,
    private val customersCustomerValidationService: CustomerValidationServiceGrpcKt.CustomerValidationServiceCoroutineStub,
    private val customersPreferenceCalculationService: PreferenceCalculationServiceGrpcKt.PreferenceCalculationServiceCoroutineStub,
    private val customersPreferenceValidationService: PreferenceValidationServiceGrpcKt.PreferenceValidationServiceCoroutineStub,
    private val deliveriesDeliveryExceptionApprovalService: DeliveryExceptionApprovalServiceGrpcKt.DeliveryExceptionApprovalServiceCoroutineStub,
    private val featureflagsFeatureFlagService: FeatureFlagServiceGrpcKt.FeatureFlagServiceCoroutineStub,
    private val fuelFuelCardImportService: FuelCardImportServiceGrpcKt.FuelCardImportServiceCoroutineStub,
    private val fuelFuelPolicyApprovalService: FuelPolicyApprovalServiceGrpcKt.FuelPolicyApprovalServiceCoroutineStub,
    private val telematicsGeoFenceSyncService: GeoFenceSyncServiceGrpcKt.GeoFenceSyncServiceCoroutineStub,
    private val telematicsTelemetryReadingImportService: TelemetryReadingImportServiceGrpcKt.TelemetryReadingImportServiceCoroutineStub,
    private val telematicsTelemetryReadingSyncService: TelemetryReadingSyncServiceGrpcKt.TelemetryReadingSyncServiceCoroutineStub,
    private val yardsYardMoveReportingService: YardMoveReportingServiceGrpcKt.YardMoveReportingServiceCoroutineStub,
) : TripLogSyncServiceGrpcKt.TripLogSyncServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(TripLogSyncGrpcService::class.java)

    private val clientForPullTripLogs2 by lazy { customersCustomerQueryService }

    /** Returns a trip log. */
    override suspend fun getTripLogSyncState(request: GetTripLogSyncStateRequest): GetTripLogSyncStateResponse {
        return handleGetTripLogSyncState(request)
    }

    /** Lists trip logs a page at a time. */
    override suspend fun listTripLogConflicts(request: ListTripLogConflictsRequest): ListTripLogConflictsResponse = handleListTripLogConflicts(request)

    /** Pulls a trip log. */
    override suspend fun pullTripLogs(request: PullTripLogsRequest): PullTripLogsResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogSyncService/PullTripLogs") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PullTripLogs", request)
            log.atInfo().addKeyValue("operation", "PullTripLogs").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLog.tripLogId).log("Recorded audit detail")
            val outbound0 = pushGeoFenceRequest {
                this.context = request.context
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.createdAt = request.tripLog.audit.createdAt
                        this.createdBy = request.tripLog.audit.createdBy
                        this.etag = request.tripLog.audit.etag
                        this.updatedBy = request.tripLog.audit.updatedBy
                    }
                }
            }
            val reply0 = telematicsGeoFenceSyncService.pushGeoFence(WireCopies.reflective(outbound0))
            val outbound1 = exportCertificatesRequest {
                this.context = request.context
                this.filter = ExportCertificatesRequestKt.filter {
                    this.region = request.tripLog.billingAddress.region
                }
            }
            certificatesCertificateQueryService.exportCertificates(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound2 = findCustomersRequest {
                this.context = request.context
                this.filter = FindCustomersRequestKt.filter {
                    this.createdAfter = request.tripLog.audit.updatedAt
                    this.region = request.tripLog.billingAddress.region
                }
            }
            val reply2 = clientForPullTripLogs2.findCustomers(outbound2)
            val outbound3 = approveDeliveryExceptionRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.audit = auditInfo {
                        this.createdBy = request.tripLog.audit.createdBy
                        this.updatedBy = reply0.geoFence.audit.updatedBy
                    }
                }
            }
            val reply3 = flowOf(outbound3).map { value -> deliveriesDeliveryExceptionApprovalService.approveDeliveryException(value) }.single()
            pullTripLogsResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply2.result.detailsMap)
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.etag = reply3.deliveryException.audit.etag
                    }
                    this.counterparty = request.tripLog.counterparty
                    this.dueAt = reply3.deliveryException.sla.dueAt
                    this.tripLogId = request.tripLogId
                }
            }
        }

    /** Pushs a trip log. */
    override suspend fun pushTripLog(request: PushTripLogRequest): PushTripLogResponse {
        return handlePushTripLog(request)
    }

    /** Reconciles a trip log. */
    override suspend fun reconcileTripLogs(request: ReconcileTripLogsRequest): ReconcileTripLogsResponse = handleReconcileTripLogs(request)

    /** Exchanges changes of trip logs with a peer. */
    override fun streamTripLogChanges(requests: Flow<StreamTripLogChangesRequest>): Flow<StreamTripLogChangesResponse> =
        requests.map { request -> handleStreamTripLogChanges(request) }

    /** Syncs a trip log. */
    override suspend fun syncTripLogs(request: SyncTripLogsRequest): SyncTripLogsResponse {
        return handleSyncTripLogs(request)
    }

    private suspend fun handleGetTripLogSyncState(request: GetTripLogSyncStateRequest): GetTripLogSyncStateResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogSyncService/GetTripLogSyncState") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetTripLogSyncState", request)
            log.atInfo().addKeyValue("operation", "GetTripLogSyncState").log("Handling transport request")
            val outbound0 = RequestFactory.create {
                syncTelemetryReadingsRequest {
                    this.context = request.context
                    this.telemetryReading = telemetryReading {
                        this.measurements.addAll(listOf(telemetryReadingMeasurement {
                            this.observedAt = request.context.deadline
                        }))
                    }
                }
            }
            val reply0 = telematicsTelemetryReadingSyncService.syncTelemetryReadings(outbound0)
            val outbound1 = shareYardMoveReportRequest {
                this.context = request.context
                this.details = ShareYardMoveReportRequestKt.details {
                    this.recipient = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply1 = ClientRegistry.lookup("acme.yards.v1.YardMoveReportingService", YardMoveReportingServiceGrpcKt.YardMoveReportingServiceCoroutineStub::class.java).shareYardMoveReport(outbound1)
            getTripLogSyncStateResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.message = reply0.result.message
                    this.status = reply1.outcome.status
                }
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.updatedAt = reply1.yardMove.audit.updatedAt
                    }
                    this.tripLogId = request.tripLogId
                }
            }
        }

    private suspend fun handleListTripLogConflicts(request: ListTripLogConflictsRequest): ListTripLogConflictsResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogSyncService/ListTripLogConflicts") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListTripLogConflicts", request)
            log.atInfo().addKeyValue("operation", "ListTripLogConflicts").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            listTripLogConflictsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handlePushTripLog(request: PushTripLogRequest): PushTripLogResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogSyncService/PushTripLog") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PushTripLog", request)
            log.atInfo().addKeyValue("operation", "PushTripLog").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLog.tripLogId).log("Recorded audit detail")
            val outbound0 = listPendingFuelPoliciesRequest {
                this.context = request.context
                this.filter = ListPendingFuelPoliciesRequestKt.filter {
                    this.createdAfter = request.tripLog.audit.createdAt
                    this.labels.putAll(request.annotationsMap)
                    this.region = request.tripLog.billingAddress.region
                }
            }
            val reply0 = withTimeout(5_000) { fuelFuelPolicyApprovalService.listPendingFuelPolicies(outbound0) }
            pushTripLogResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.issuedAt = request.tripLog.issuedAt
                    this.tripLogId = request.tripLogId
                }
            }
        }

    private suspend fun handleReconcileTripLogs(request: ReconcileTripLogsRequest): ReconcileTripLogsResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogSyncService/ReconcileTripLogs") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ReconcileTripLogs", request)
            log.atInfo().addKeyValue("operation", "ReconcileTripLogs").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLog.tripLogId).log("Recorded audit detail")
            val configure0: EstimatePreferenceRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.details = EstimatePreferenceRequestKt.details {
                    this.destination = postalAddress {
                        this.recipient = request.tripLog.billingAddress.recipient
                    }
                }
                this.preference = preference {
                    this.audit = auditInfo {
                        this.createdAt = request.tripLog.audit.createdAt
                    }
                }
            }
            val outbound0 = estimatePreferenceRequest(configure0)
            val reply0 = customersPreferenceCalculationService.estimatePreference(outbound0)
            val outbound1 = batchGetFeatureFlagsRequest {
                this.context = request.context
            }
            val reply1 = featureflagsFeatureFlagService.batchGetFeatureFlags(outbound1)
            reconcileTripLogsResponse {
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.createdAt = reply0.preference.audit.createdAt
                        this.updatedBy = reply0.preference.audit.updatedBy
                    }
                    this.billingAddress = request.tripLog.billingAddress
                    this.tripLogId = request.tripLogId
                }
            }
        }

    private fun StreamTripLogChangesRequest.toValidateFuelCardImportFromStreamTripLogChanges2(): ValidateFuelCardImportRequest {
        val source = this
        return validateFuelCardImportRequest {
            this.context = source.context
            this.fuelCard = fuelCard {
                this.bank = fuelCardBankFunding {
                    this.bic = source.change.bank.bic
                }
            }
        }
    }

    private suspend fun handleStreamTripLogChanges(request: StreamTripLogChangesRequest): StreamTripLogChangesResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogSyncService/StreamTripLogChanges") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StreamTripLogChanges", request)
            log.atInfo().addKeyValue("operation", "StreamTripLogChanges").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.change.tripLogId).log("Recorded audit detail")
            val outbound0 = recallFuelPolicyRequest {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.createdBy = request.change.audit.createdBy
                    }
                }
            }
            val reply0 = fuelFuelPolicyApprovalService.recallFuelPolicy(outbound0)
            val outbound1 = mapTelemetryReadingImportColumnsRequest {}.telematicsCopy {
                this.context = request.context
                this.details = MapTelemetryReadingImportColumnsRequestKt.details {
                    this.contact = contactInfo {
                        this.email = reply0.fuelPolicy.owner.email
                    }
                }
                this.reason = reply0.fuelPolicy.block.reason
                this.telemetryReading = telemetryReading {
                    this.correlationId = reply0.result.correlationId
                    this.subject = geoFenceRef {
                        this.version = reply0.fuelPolicy.version
                    }
                }
            }
            val reply1 = telematicsTelemetryReadingImportService.mapTelemetryReadingImportColumns(outbound1)
            val outbound2 = request.toValidateFuelCardImportFromStreamTripLogChanges2()
            val reply2 = fuelFuelCardImportService.validateFuelCardImport(outbound2)
            val outbound3 = validateCustomerRequest {
                this.context = request.context
                this.customer = customer {
                    this.audit = auditInfo {
                        this.createdBy = when (request.frameCase) {
                            StreamTripLogChangesRequest.FrameCase.CHANGE -> request.change.audit.createdBy
                            else -> ""
                        }
                        this.updatedAt = reply0.fuelPolicy.audit.updatedAt
                    }
                    this.contact = contactInfo {
                        this.phone = reply2.fuelCard.contact.phone
                        this.preferredChannel = reply2.fuelCard.contact.preferredChannel
                    }
                }
            }
            val reply3 = customersCustomerValidationService.validateCustomer(outbound3)
            streamTripLogChangesResponse {
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = reply2.outcome.status
                }
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.createdBy = reply0.fuelPolicy.audit.createdBy
                        this.updatedBy = reply0.fuelPolicy.audit.updatedBy
                    }
                    this.counterparty = request.change.counterparty
                    this.lines.addAll(listOf(tripLogLine {
                        this.attributes.putAll(reply1.telemetryReading.attributesMap)
                    }))
                    this.total = request.change.total
                }
            }
        }

    private suspend fun handleSyncTripLogs(request: SyncTripLogsRequest): SyncTripLogsResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogSyncService/SyncTripLogs") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SyncTripLogs", request)
            log.atInfo().addKeyValue("operation", "SyncTripLogs").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLog.tripLogId).log("Recorded audit detail")
            val outbound0 = explainPreferenceDecisionRequest {
                this.context = request.context
                this.preference = preference {
                    this.audit = auditInfo {
                        this.createdAt = request.tripLog.audit.createdAt
                        this.updatedBy = request.tripLog.audit.updatedBy
                    }
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = customersPreferenceValidationService.explainPreferenceDecision(envelope0.unpack(ExplainPreferenceDecisionRequest::class.java))
            val outbound1 = syncGeoFencesRequest {
                this.context = request.context
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.etag = request.tripLog.audit.etag
                        this.updatedBy = request.tripLog.audit.updatedBy
                    }
                }
            }
            val reply1 = telematicsGeoFenceSyncService.syncGeoFences(WireCopies.json(outbound1))
            syncTripLogsResponse {
                this.outcome = SyncTripLogsResponseKt.outcome {
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.preference.audit.updatedBy
                    }
                    this.notes = request.tripLog.notes
                    this.tripLogId = request.tripLogId
                }
            }
        }
}
