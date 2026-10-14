package com.acme.transport.telematics.adapters

import com.acme.common.v1.auditInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.EstimatePreferenceRequest
import com.acme.customers.v1.PreferenceCalculationServiceGrpcKt
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.estimatePreferenceRequest
import com.acme.customers.v1.preference
import com.acme.customers.v1.preferenceBlockAction
import com.acme.featureflags.v1.FeatureFlagServiceGrpcKt
import com.acme.featureflags.v1.ListFeatureFlagsRequest
import com.acme.featureflags.v1.ListFeatureFlagsRequestKt
import com.acme.featureflags.v1.ListFeatureFlagsResponse
import com.acme.featureflags.v1.listFeatureFlagsRequest
import com.acme.identity.v1.userRef
import com.acme.notifications.v1.NotificationServiceGrpcKt
import com.acme.notifications.v1.batchGetNotificationsRequest
import com.acme.telematics.v1.ActivateTripLogRequest
import com.acme.telematics.v1.ActivateTripLogResponse
import com.acme.telematics.v1.ActivateTripLogResponseKt
import com.acme.telematics.v1.GeoFenceImportServiceGrpcKt
import com.acme.telematics.v1.GetTripLogLifecycleRequest
import com.acme.telematics.v1.GetTripLogLifecycleResponse
import com.acme.telematics.v1.PushTripLogRequest
import com.acme.telematics.v1.ReinstateTripLogRequest
import com.acme.telematics.v1.ReinstateTripLogResponse
import com.acme.telematics.v1.ReinstateTripLogResponseKt
import com.acme.telematics.v1.RenewTripLogRequest
import com.acme.telematics.v1.RenewTripLogResponse
import com.acme.telematics.v1.RenewTripLogResponseKt
import com.acme.telematics.v1.SuspendTripLogRequest
import com.acme.telematics.v1.SuspendTripLogResponse
import com.acme.telematics.v1.SuspendTripLogResponseKt
import com.acme.telematics.v1.SyncTelemetryReadingsRequest
import com.acme.telematics.v1.SyncTelemetryReadingsResponse
import com.acme.telematics.v1.SyncTripLogsRequest
import com.acme.telematics.v1.SyncTripLogsResponse
import com.acme.telematics.v1.TelemetryReadingSyncServiceGrpcKt
import com.acme.telematics.v1.TerminateTripLogRequest
import com.acme.telematics.v1.TerminateTripLogResponse
import com.acme.telematics.v1.TerminateTripLogResponseKt
import com.acme.telematics.v1.TripLog
import com.acme.telematics.v1.TripLogLifecycleServiceGrpcKt
import com.acme.telematics.v1.TripLogSyncServiceGrpcKt
import com.acme.telematics.v1.ValidateGeoFenceImportRequest
import com.acme.telematics.v1.ValidateGeoFenceImportResponse
import com.acme.telematics.v1.activateTripLogResponse
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.geoFenceRef
import com.acme.telematics.v1.getTripLogLifecycleResponse
import com.acme.telematics.v1.getTripLogSyncStateRequest
import com.acme.telematics.v1.pushTripLogRequest
import com.acme.telematics.v1.reinstateTripLogResponse
import com.acme.telematics.v1.renewTripLogResponse
import com.acme.telematics.v1.suspendTripLogResponse
import com.acme.telematics.v1.syncTelemetryReadingsRequest
import com.acme.telematics.v1.syncTripLogsRequest
import com.acme.telematics.v1.telemetryReading
import com.acme.telematics.v1.terminateTripLogResponse
import com.acme.telematics.v1.tripLog
import com.acme.telematics.v1.tripLogCardSettlement
import com.acme.telematics.v1.tripLogLine
import com.acme.telematics.v1.tripLogLineTax
import com.acme.telematics.v1.validateGeoFenceImportRequest
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.transport.telematics.TelematicsRepository
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Moves a trip log between the states of its lifecycle. */
class TripLogLifecycleController(
    private val store: TelematicsRepository,
    private val workers: CoroutineScope,
    private val customersPreferenceCalculationService: PreferenceCalculationServiceGrpcKt.PreferenceCalculationServiceCoroutineStub,
    private val featureflagsFeatureFlagService: FeatureFlagServiceGrpcKt.FeatureFlagServiceCoroutineStub,
    private val notificationsNotificationService: NotificationServiceGrpcKt.NotificationServiceCoroutineStub,
    private val telematicsGeoFenceImportService: GeoFenceImportServiceGrpcKt.GeoFenceImportServiceCoroutineStub,
    private val telematicsTelemetryReadingSyncService: TelemetryReadingSyncServiceGrpcKt.TelemetryReadingSyncServiceCoroutineStub,
    private val telematicsTripLogSyncService: TripLogSyncServiceGrpcKt.TripLogSyncServiceCoroutineStub,
) : TripLogLifecycleServiceGrpcKt.TripLogLifecycleServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(TripLogLifecycleController::class.java)

    private val invokeReinstateTripLog0: suspend (ValidateGeoFenceImportRequest) -> ValidateGeoFenceImportResponse = { payload ->
        telematicsGeoFenceImportService.validateGeoFenceImport(payload)
    }

    private val workerForTerminateTripLog0 = ReplyMailbox<SyncTelemetryReadingsRequest, SyncTelemetryReadingsResponse>(workers) { payload ->
        withTimeout(5_000) { telematicsTelemetryReadingSyncService.syncTelemetryReadings(payload) }
    }

    /** Activates a trip log. */
    override suspend fun activateTripLog(request: ActivateTripLogRequest): ActivateTripLogResponse = handleActivateTripLog(request)

    /** Returns a trip log. */
    override suspend fun getTripLogLifecycle(request: GetTripLogLifecycleRequest): GetTripLogLifecycleResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogLifecycleService/GetTripLogLifecycle") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetTripLogLifecycle", request)
            log.atInfo().addKeyValue("operation", "GetTripLogLifecycle").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            getTripLogLifecycleResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.tripLogId = request.tripLogId
                }
            }
        }

    /** Reinstates a trip log. */
    override suspend fun reinstateTripLog(request: ReinstateTripLogRequest): ReinstateTripLogResponse {
        return handleReinstateTripLog(request)
    }

    /** Renews a trip log. */
    override suspend fun renewTripLog(request: RenewTripLogRequest): RenewTripLogResponse = handleRenewTripLog(request)

    /** Suspends a trip log. */
    override suspend fun suspendTripLog(request: SuspendTripLogRequest): SuspendTripLogResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogLifecycleService/SuspendTripLog") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SuspendTripLog", request)
            log.atInfo().addKeyValue("operation", "SuspendTripLog").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = getTripLogSyncStateRequest {
                this.context = request.context
                this.tripLogId = request.tripLogId
            }
            val reply0 = ClientRegistry.lookup("acme.telematics.v1.TripLogSyncService", TripLogSyncServiceGrpcKt.TripLogSyncServiceCoroutineStub::class.java).getTripLogSyncState(outbound0)
            val outbound1 = batchGetNotificationsRequest {
                this.context = request.context
            }
            val reply1 = notificationsNotificationService.batchGetNotifications(WireCopies.reflective(outbound1))
            suspendTripLogResponse {
                this.outcome = SuspendTripLogResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.total = reply0.tripLog.total
                    this.tripLogId = request.tripLogId
                }
            }
        }

    /** Terminates a trip log. */
    override suspend fun terminateTripLog(request: TerminateTripLogRequest): TerminateTripLogResponse {
        return handleTerminateTripLog(request)
    }

    private suspend fun handleActivateTripLog(request: ActivateTripLogRequest): ActivateTripLogResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogLifecycleService/ActivateTripLog") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ActivateTripLog", request)
            log.atInfo().addKeyValue("operation", "ActivateTripLog").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = estimatePreferenceRequest {
                this.context = request.context
                this.preference = preference {
                    this.block = preferenceBlockAction {
                        this.notifyEmail = request.details.newOwner.email
                    }
                    this.owner = userRef {
                        this.displayName = request.details.newOwner.displayName
                        this.email = request.details.notifyContact.email
                        this.userId = request.details.newOwner.userId
                    }
                }
            }
            val command0: Submission<EstimatePreferenceRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = customersPreferenceCalculationService.estimatePreference(payload0)
            val outbound1 = listFeatureFlagsRequest {
                this.context = request.context
                this.filter = ListFeatureFlagsRequestKt.filter {
                    this.createdAfter = reply0.preference.audit.createdAt
                    this.labels.putAll(reply0.preference.parametersMap)
                    this.ownerId = reply0.result.correlationId
                }
            }
            val adapter1 = OutboundPort<ListFeatureFlagsRequest, ListFeatureFlagsResponse> { payload ->
                featureflagsFeatureFlagService.listFeatureFlags(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            activateTripLogResponse {
                this.outcome = ActivateTripLogResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.message = reply1.result.message
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.preference.audit.updatedAt
                    }
                    this.lines.addAll(listOf(tripLogLine {
                        this.tax = tripLogLineTax {
                            this.amount = reply0.preference.discount.amount
                        }
                    }))
                    this.tripLogId = request.tripLogId
                }
            }
        }

    private suspend fun handleReinstateTripLog(request: ReinstateTripLogRequest): ReinstateTripLogResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogLifecycleService/ReinstateTripLog") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ReinstateTripLog", request)
            log.atInfo().addKeyValue("operation", "ReinstateTripLog").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = validateGeoFenceImportRequest {
                this.context = request.context
                this.geoFence = geoFence {
                    this.owner = userRef {
                        this.email = request.details.newOwner.email
                    }
                }
            }
            val reply0 = invokeReinstateTripLog0(outbound0)
            val outbound1 = syncTripLogsRequest {
                this.context = request.context
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.createdAt = reply0.geoFence.audit.createdAt
                    }
                    this.counterparty = customerRef {
                        this.displayName = reply0.geoFence.owner.displayName
                        this.email = request.details.newOwner.email
                    }
                }
            }
            val invoke1: suspend (SyncTripLogsRequest, Metadata) -> SyncTripLogsResponse = telematicsTripLogSyncService::syncTripLogs
            val reply1 = invoke1(outbound1, Metadata())
            reinstateTripLogResponse {
                this.outcome = ReinstateTripLogResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.createdAt = reply1.tripLog.audit.createdAt
                    }
                    this.counterparty = customerRef {
                        this.displayName = reply0.geoFence.owner.displayName
                    }
                    this.tripLogId = request.tripLogId
                }
            }
        }

    private suspend fun handleRenewTripLog(request: RenewTripLogRequest): RenewTripLogResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogLifecycleService/RenewTripLog") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RenewTripLog", request)
            log.atInfo().addKeyValue("operation", "RenewTripLog").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = PushTripLogRequest.newBuilder().apply {
                setContext(request.context)
                setReason(request.reason)
                setTripLog(tripLog {
                    this.counterparty = customerRef {
                        this.displayName = request.details.newOwner.displayName
                        this.email = request.details.newOwner.email
                    }
                    this.tripLogId = request.tripLogId
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> telematicsTripLogSyncService.pushTripLog(value) }
            val outbound1 = syncTripLogsRequest {
                this.context = request.context
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.etag = reply0.tripLog.audit.etag
                    }
                    this.billingAddress = postalAddress {
                        this.postalCode = reply0.tripLog.billingAddress.postalCode
                    }
                    this.card = tripLogCardSettlement {
                        this.token = when (reply0.tripLog.settlementCase) {
                            TripLog.SettlementCase.CARD -> reply0.tripLog.card.token
                            else -> ""
                        }
                    }
                    this.counterparty = customerRef {
                        this.displayName = reply0.tripLog.counterparty.displayName
                    }
                }
            }
            val reply1 = telematicsTripLogSyncService.syncTripLogs(outbound1)
            renewTripLogResponse {
                this.outcome = RenewTripLogResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.createdAt = reply1.tripLog.audit.createdAt
                        this.updatedBy = reply0.tripLog.audit.updatedBy
                    }
                    this.issuedAt = reply1.tripLog.issuedAt
                    this.tripLogId = request.tripLogId
                }
            }
        }

    private suspend fun handleTerminateTripLog(request: TerminateTripLogRequest): TerminateTripLogResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogLifecycleService/TerminateTripLog") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "TerminateTripLog", request)
            log.atInfo().addKeyValue("operation", "TerminateTripLog").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = syncTelemetryReadingsRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.reason = request.reason
                this.telemetryReading = telemetryReading {
                    this.occurredAt = request.details.effectiveAt
                    this.subject = geoFenceRef {
                        this.displayName = request.details.newOwner.displayName
                    }
                }
            }
            val reply0 = workerForTerminateTripLog0.submit(outbound0)
            terminateTripLogResponse {
                this.outcome = TerminateTripLogResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.nextSteps.addAll(reply0.outcome.nextStepsList)
                }
                this.result = operationResult {
                    this.status = reply0.outcome.status
                }
                this.tripLog = tripLog {
                    this.tripLogId = request.tripLogId
                }
            }
        }
}
