package com.acme.transport.telematics.grpc

import com.acme.common.v1.TimeWindow
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.CustomerValidationServiceGrpcKt
import com.acme.customers.v1.HouseholdCalculationServiceGrpcKt
import com.acme.customers.v1.HouseholdValidationServiceGrpcKt
import com.acme.customers.v1.copy as customersCopy
import com.acme.customers.v1.customer
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.explainHouseholdDecisionRequest
import com.acme.customers.v1.household
import com.acme.customers.v1.recalculateHouseholdsRequest
import com.acme.customers.v1.scoreCustomerRequest
import com.acme.documents.v1.DocumentServiceGrpcKt
import com.acme.documents.v1.ListDocumentsRequest
import com.acme.documents.v1.ListDocumentsRequestKt
import com.acme.documents.v1.ListDocumentsResponse
import com.acme.documents.v1.listDocumentsRequest
import com.acme.fuel.v1.FuelTransactionSyncServiceGrpcKt
import com.acme.fuel.v1.PullFuelTransactionsRequest
import com.acme.fuel.v1.PullFuelTransactionsResponse
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.fuelTransactionMeasurement
import com.acme.fuel.v1.pullFuelTransactionsRequest
import com.acme.identity.v1.UserValidationServiceGrpcKt
import com.acme.identity.v1.ValidateUserRequestKt
import com.acme.identity.v1.normalizeUserRequest
import com.acme.identity.v1.user
import com.acme.identity.v1.userRef
import com.acme.identity.v1.validateUserRequest
import com.acme.ledger.v1.LedgerAccountQueryServiceGrpcKt
import com.acme.ledger.v1.ResolveLedgerAccountsRequest
import com.acme.ledger.v1.resolveLedgerAccountsRequest
import com.acme.metrics.v1.MetricSeriesValidationServiceGrpcKt
import com.acme.metrics.v1.metricSeries
import com.acme.metrics.v1.scoreMetricSeriesRequest
import com.acme.telematics.v1.BatchGetTelemetryReadingsRequest
import com.acme.telematics.v1.GeoFence
import com.acme.telematics.v1.GeoFenceImportServiceGrpcKt
import com.acme.telematics.v1.GeoFenceSyncServiceGrpcKt
import com.acme.telematics.v1.GetGeoFenceImportStatusRequest
import com.acme.telematics.v1.GetGeoFenceImportStatusResponse
import com.acme.telematics.v1.GetGeoFenceSyncStateRequest
import com.acme.telematics.v1.GetGeoFenceSyncStateResponse
import com.acme.telematics.v1.ListGeoFenceConflictsRequest
import com.acme.telematics.v1.ListGeoFenceConflictsResponse
import com.acme.telematics.v1.PullGeoFencesRequest
import com.acme.telematics.v1.PullGeoFencesResponse
import com.acme.telematics.v1.PullGeoFencesResponseKt
import com.acme.telematics.v1.PushGeoFenceRequest
import com.acme.telematics.v1.PushGeoFenceResponse
import com.acme.telematics.v1.ReconcileGeoFencesRequest
import com.acme.telematics.v1.ReconcileGeoFencesResponse
import com.acme.telematics.v1.ReconcileGeoFencesResponseKt
import com.acme.telematics.v1.ResolveGeoFenceConflictRequest
import com.acme.telematics.v1.ResolveGeoFenceConflictResponse
import com.acme.telematics.v1.ShareTripLogExportRequestKt
import com.acme.telematics.v1.StreamGeoFenceChangesRequest
import com.acme.telematics.v1.StreamGeoFenceChangesResponse
import com.acme.telematics.v1.SummarizeTelemetryReadingsRequestKt
import com.acme.telematics.v1.SyncGeoFencesRequest
import com.acme.telematics.v1.SyncGeoFencesResponse
import com.acme.telematics.v1.TelemetryReadingQueryServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingServiceGrpcKt
import com.acme.telematics.v1.TripLogExportServiceGrpcKt
import com.acme.telematics.v1.TripLogValidationServiceGrpcKt
import com.acme.telematics.v1.batchGetTelemetryReadingsRequest
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.getGeoFenceImportStatusRequest
import com.acme.telematics.v1.getGeoFenceSyncStateResponse
import com.acme.telematics.v1.listGeoFenceConflictsResponse
import com.acme.telematics.v1.previewTripLogRequest
import com.acme.telematics.v1.pullGeoFencesResponse
import com.acme.telematics.v1.pushGeoFenceResponse
import com.acme.telematics.v1.reconcileGeoFencesResponse
import com.acme.telematics.v1.resolveGeoFenceConflictResponse
import com.acme.telematics.v1.shareTripLogExportRequest
import com.acme.telematics.v1.streamGeoFenceChangesResponse
import com.acme.telematics.v1.summarizeTelemetryReadingsRequest
import com.acme.telematics.v1.syncGeoFencesResponse
import com.acme.telematics.v1.tripLog
import com.acme.telematics.v1.tripLogLine
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.transport.telematics.TelematicsRepository
import com.acme.yards.v1.ListPendingYardsRequestKt
import com.acme.yards.v1.YardApprovalServiceGrpcKt
import com.acme.yards.v1.YardMoveApprovalServiceGrpcKt
import com.acme.yards.v1.listPendingYardsRequest
import com.acme.yards.v1.recallYardMoveRequest
import com.acme.yards.v1.yardMove
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Keeps geo fences in step with the systems of record in other regions. */
class GeoFenceSyncEndpoint(
    private val store: TelematicsRepository,
    private val workers: CoroutineScope,
    private val customersCustomerValidationService: CustomerValidationServiceGrpcKt.CustomerValidationServiceCoroutineStub,
    private val customersHouseholdCalculationService: HouseholdCalculationServiceGrpcKt.HouseholdCalculationServiceCoroutineStub,
    private val customersHouseholdValidationService: HouseholdValidationServiceGrpcKt.HouseholdValidationServiceCoroutineStub,
    private val documentsDocumentService: DocumentServiceGrpcKt.DocumentServiceCoroutineStub,
    private val fuelFuelTransactionSyncService: FuelTransactionSyncServiceGrpcKt.FuelTransactionSyncServiceCoroutineStub,
    private val identityUserValidationService: UserValidationServiceGrpcKt.UserValidationServiceCoroutineStub,
    private val ledgerLedgerAccountQueryService: LedgerAccountQueryServiceGrpcKt.LedgerAccountQueryServiceCoroutineStub,
    private val metricsMetricSeriesValidationService: MetricSeriesValidationServiceGrpcKt.MetricSeriesValidationServiceCoroutineStub,
    private val telematicsGeoFenceImportService: GeoFenceImportServiceGrpcKt.GeoFenceImportServiceCoroutineStub,
    private val telematicsTelemetryReadingQueryService: TelemetryReadingQueryServiceGrpcKt.TelemetryReadingQueryServiceCoroutineStub,
    private val telematicsTelemetryReadingService: TelemetryReadingServiceGrpcKt.TelemetryReadingServiceCoroutineStub,
    private val telematicsTripLogExportService: TripLogExportServiceGrpcKt.TripLogExportServiceCoroutineStub,
    private val telematicsTripLogValidationService: TripLogValidationServiceGrpcKt.TripLogValidationServiceCoroutineStub,
    private val yardsYardApprovalService: YardApprovalServiceGrpcKt.YardApprovalServiceCoroutineStub,
    private val yardsYardMoveApprovalService: YardMoveApprovalServiceGrpcKt.YardMoveApprovalServiceCoroutineStub,
) : GeoFenceSyncServiceGrpcKt.GeoFenceSyncServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(GeoFenceSyncEndpoint::class.java)

    private val invokeListGeoFenceConflicts0: suspend (PullFuelTransactionsRequest) -> PullFuelTransactionsResponse = { payload ->
        fuelFuelTransactionSyncService.pullFuelTransactions(payload)
    }

    private val workerForPushGeoFence1 = ReplyMailbox<GetGeoFenceImportStatusRequest, GetGeoFenceImportStatusResponse>(workers) { payload ->
        withTimeout(5_000) { telematicsGeoFenceImportService.getGeoFenceImportStatus(payload) }
    }

    private val clientForPushGeoFence2 by lazy { identityUserValidationService }

    /** Returns a geo fence. */
    override suspend fun getGeoFenceSyncState(request: GetGeoFenceSyncStateRequest): GetGeoFenceSyncStateResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceSyncService/GetGeoFenceSyncState") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetGeoFenceSyncState", request)
            log.atInfo().addKeyValue("operation", "GetGeoFenceSyncState").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            getGeoFenceSyncStateResponse {
                this.geoFence = geoFence {
                    this.geoFenceId = request.geoFenceId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Lists geo fences a page at a time. */
    override suspend fun listGeoFenceConflicts(request: ListGeoFenceConflictsRequest): ListGeoFenceConflictsResponse {
        return handleListGeoFenceConflicts(request)
    }

    /** Pulls a geo fence. */
    override suspend fun pullGeoFences(request: PullGeoFencesRequest): PullGeoFencesResponse = handlePullGeoFences(request)

    /** Pushs a geo fence. */
    override suspend fun pushGeoFence(request: PushGeoFenceRequest): PushGeoFenceResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceSyncService/PushGeoFence") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PushGeoFence", request)
            log.atInfo().addKeyValue("operation", "PushGeoFence").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.geoFence.geoFenceId).log("Recorded audit detail")
            val outbound0 = recalculateHouseholdsRequest {
                this.context = request.context
                this.household = household {
                    this.children.addAll(listOf(household {
                        this.children.addAll(listOf(household {
                            this.audit = auditInfo {
                                this.createdBy = request.geoFence.audit.createdBy
                                this.etag = request.geoFence.audit.etag
                            }
                            this.manager = userRef {
                                this.email = request.geoFence.owner.email
                                this.userId = request.geoFence.owner.userId
                            }
                        }))
                    }))
                }
            }
            val reply0 = customersHouseholdCalculationService.recalculateHouseholds(WireCopies.reflective(outbound0))
            val outbound1 = getGeoFenceImportStatusRequest {
                this.context = request.context
                this.geoFenceId = request.geoFenceId
            }
            val reply1 = workerForPushGeoFence1.submit(outbound1)
            val outbound2 = validateUserRequest {
                this.context = request.context
                this.details = ValidateUserRequestKt.details {
                    this.contact = contactInfo {
                        this.email = reply0.household.manager.email
                    }
                }
                this.user = user {
                    this.primaryAddress = postalAddress {
                        this.region = reply0.household.location.region
                    }
                }
            }
            val reply2 = clientForPushGeoFence2.validateUser(outbound2)
            pushGeoFenceResponse {
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.createdAt = reply0.household.audit.createdAt
                        this.createdBy = reply0.household.audit.createdBy
                    }
                    this.geoFenceId = request.geoFenceId
                    this.priority = reply1.geoFence.priority
                    this.version = request.geoFence.version
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply2.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    /** Reconciles a geo fence. */
    override suspend fun reconcileGeoFences(request: ReconcileGeoFencesRequest): ReconcileGeoFencesResponse {
        return handleReconcileGeoFences(request)
    }

    /** Resolves a geo fence. */
    override suspend fun resolveGeoFenceConflict(request: ResolveGeoFenceConflictRequest): ResolveGeoFenceConflictResponse = handleResolveGeoFenceConflict(request)

    /** Exchanges changes of geo fences with a peer. */
    override fun streamGeoFenceChanges(requests: Flow<StreamGeoFenceChangesRequest>): Flow<StreamGeoFenceChangesResponse> =
        requests.map { request -> handleStreamGeoFenceChanges(request) }

    /** Syncs a geo fence. */
    override suspend fun syncGeoFences(request: SyncGeoFencesRequest): SyncGeoFencesResponse {
        return handleSyncGeoFences(request)
    }

    private suspend fun handleListGeoFenceConflicts(request: ListGeoFenceConflictsRequest): ListGeoFenceConflictsResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceSyncService/ListGeoFenceConflicts") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListGeoFenceConflicts", request)
            log.atInfo().addKeyValue("operation", "ListGeoFenceConflicts").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = pullFuelTransactionsRequest {
                this.context = request.context
                this.fuelTransaction = fuelTransaction {
                    this.measurements.addAll(listOf(fuelTransactionMeasurement {
                        this.observedAt = request.filter.createdAfter
                    }))
                    this.source = request.filter.ownerId
                    this.subject = customerRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = invokeListGeoFenceConflicts0(outbound0)
            listGeoFenceConflictsResponse {
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handlePullGeoFences(request: PullGeoFencesRequest): PullGeoFencesResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceSyncService/PullGeoFences") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PullGeoFences", request)
            log.atInfo().addKeyValue("operation", "PullGeoFences").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.geoFence.geoFenceId).log("Recorded audit detail")
            val outbound0 = listDocumentsRequest {
                this.context = request.context
                this.filter = ListDocumentsRequestKt.filter {
                    this.createdAfter = request.geoFence.audit.updatedAt
                    this.labels.putAll(request.geoFence.parametersMap)
                    this.ownerId = request.geoFence.owner.userId
                }
            }
            val invoke0: suspend (ListDocumentsRequest, Metadata) -> ListDocumentsResponse = documentsDocumentService::listDocuments
            val reply0 = invoke0(outbound0, Metadata())
            val outbound1 = ResolveLedgerAccountsRequest.newBuilder().apply {
                setContext(request.context)
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> ledgerLedgerAccountQueryService.resolveLedgerAccounts(value) }
            val outbound2 = RequestFactory.create {
                scoreCustomerRequest {
                    this.context = request.context
                    this.customer = customer {
                        this.audit = auditInfo {
                            this.createdBy = request.geoFence.audit.createdBy
                            this.etag = request.geoFence.audit.etag
                            this.updatedAt = request.geoFence.audit.updatedAt
                        }
                        this.relationshipManager = userRef {
                            this.userId = request.geoFence.owner.userId
                        }
                    }
                }
            }
            val reply2 = customersCustomerValidationService.scoreCustomer(outbound2)
            val outbound3 = previewTripLogRequest {
                this.context = request.context
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.createdAt = reply2.customer.audit.createdAt
                    }
                    this.lines.addAll(listOf(tripLogLine {
                        this.amount = request.geoFence.discount.amount
                    }))
                }
            }
            val reply3 = ClientRegistry.lookup("acme.telematics.v1.TripLogValidationService", TripLogValidationServiceGrpcKt.TripLogValidationServiceCoroutineStub::class.java).previewTripLog(outbound3)
            pullGeoFencesResponse {
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.etag = reply2.customer.audit.etag
                    }
                    this.geoFenceId = request.geoFenceId
                    this.owner = userRef {
                        this.userId = reply2.customer.relationshipManager.userId
                    }
                    this.version = request.geoFence.version
                }
                this.outcome = PullGeoFencesResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleReconcileGeoFences(request: ReconcileGeoFencesRequest): ReconcileGeoFencesResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceSyncService/ReconcileGeoFences") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ReconcileGeoFences", request)
            log.atInfo().addKeyValue("operation", "ReconcileGeoFences").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.geoFence.geoFenceId).log("Recorded audit detail")
            val outbound0 = recallYardMoveRequest {
                this.context = request.context
                this.yardMove = yardMove {
                    this.audit = auditInfo {
                        this.updatedBy = request.geoFence.audit.updatedBy
                    }
                }
            }
            val reply0 = flowOf(outbound0).map { value -> yardsYardMoveApprovalService.recallYardMove(value) }.single()
            val outbound1 = normalizeUserRequest {
                this.context = request.context
                this.user = user {
                    this.audit = auditInfo {
                        this.createdBy = reply0.yardMove.audit.createdBy
                        this.etag = request.geoFence.audit.etag
                    }
                }
            }
            val reply1 = withTimeout(5_000) { identityUserValidationService.normalizeUser(outbound1) }
            val configure2: ShareTripLogExportRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.details = ShareTripLogExportRequestKt.details {
                    this.recipient = contactInfo {
                        this.email = reply1.user.relationshipManager.email
                        this.name = reply1.user.contact.name
                        this.phone = reply1.user.contact.phone
                        this.preferredChannel = reply1.user.contact.preferredChannel
                    }
                }
            }
            val outbound2 = shareTripLogExportRequest(configure2)
            val reply2 = telematicsTripLogExportService.shareTripLogExport(outbound2)
            reconcileGeoFencesResponse {
                this.geoFence = geoFence {
                    this.geoFenceId = request.geoFenceId
                    this.owner = userRef {
                        this.displayName = reply1.user.relationshipManager.displayName
                    }
                    this.version = request.geoFence.version
                }
                this.outcome = ReconcileGeoFencesResponseKt.outcome {
                    this.message = reply2.result.message
                }
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleResolveGeoFenceConflict(request: ResolveGeoFenceConflictRequest): ResolveGeoFenceConflictResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceSyncService/ResolveGeoFenceConflict") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResolveGeoFenceConflict", request)
            log.atInfo().addKeyValue("operation", "ResolveGeoFenceConflict").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.geoFence.geoFenceId).log("Recorded audit detail")
            val outbound0 = scoreMetricSeriesRequest {
                this.context = request.context
                this.metricSeries = metricSeries {
                    this.audit = auditInfo {
                        this.createdBy = request.geoFence.audit.createdBy
                        this.etag = request.geoFence.audit.etag
                        this.updatedBy = request.geoFence.audit.updatedBy
                    }
                    this.owner = userRef {
                        this.userId = request.geoFence.owner.userId
                    }
                }
            }
            val reply0 = metricsMetricSeriesValidationService.scoreMetricSeries(outbound0)
            resolveGeoFenceConflictResponse {
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.metricSeries.audit.updatedBy
                    }
                    this.geoFenceId = request.geoFenceId
                    this.name = request.geoFence.name
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleStreamGeoFenceChanges(request: StreamGeoFenceChangesRequest): StreamGeoFenceChangesResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceSyncService/StreamGeoFenceChanges") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StreamGeoFenceChanges", request)
            log.atInfo().addKeyValue("operation", "StreamGeoFenceChanges").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.change.name).log("Recorded audit detail")
            val outbound0 = summarizeTelemetryReadingsRequest {
                this.annotations.putAll(request.context.baggageMap)
                this.context = request.context
                this.details = SummarizeTelemetryReadingsRequestKt.details {
                    this.filters.putAll(when (request.frameCase) {
                        StreamGeoFenceChangesRequest.FrameCase.CHANGE -> request.change.parametersMap
                        else -> emptyMap()
                    })
                    this.period = when (request.frameCase) {
                        StreamGeoFenceChangesRequest.FrameCase.CHANGE -> request.change.effective
                        else -> TimeWindow.getDefaultInstance()
                    }
                }
                this.reason = when (request.frameCase) {
                    StreamGeoFenceChangesRequest.FrameCase.CHANGE -> when (request.change.actionCase) {
                        GeoFence.ActionCase.BLOCK -> request.change.block.reason
                        else -> ""
                    }
                    else -> ""
                }
            }
            val reply0 = telematicsTelemetryReadingQueryService.summarizeTelemetryReadings(outbound0)
            val outbound1 = explainHouseholdDecisionRequest {}.customersCopy {
                this.context = request.context
                this.household = household {
                    this.children.addAll(listOf(household {
                        this.children.addAll(listOf(household {
                            this.audit = auditInfo {
                                this.updatedAt = request.change.audit.updatedAt
                            }
                            this.manager = userRef {
                                this.email = request.change.owner.email
                            }
                        }))
                    }))
                }
            }
            val reply1 = customersHouseholdValidationService.explainHouseholdDecision(outbound1)
            streamGeoFenceChangesResponse {
                this.geoFence = geoFence {
                    this.geoFenceId = request.change.geoFenceId
                    this.status = request.change.status
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = reply0.result.status
                }
            }
        }

    private fun SyncGeoFencesRequest.toBatchGetTelemetryReadingsFromSyncGeoFences0(): BatchGetTelemetryReadingsRequest {
        val source = this
        return batchGetTelemetryReadingsRequest {
            this.context = source.context
        }
    }

    private suspend fun handleSyncGeoFences(request: SyncGeoFencesRequest): SyncGeoFencesResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceSyncService/SyncGeoFences") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SyncGeoFences", request)
            log.atInfo().addKeyValue("operation", "SyncGeoFences").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.geoFence.geoFenceId).log("Recorded audit detail")
            val outbound0 = request.toBatchGetTelemetryReadingsFromSyncGeoFences0()
            val reply0 = telematicsTelemetryReadingService.batchGetTelemetryReadings(outbound0)
            val outbound1 = listPendingYardsRequest {
                this.context = request.context
                this.filter = ListPendingYardsRequestKt.filter {
                    this.createdAfter = request.geoFence.audit.updatedAt
                    this.labels.putAll(request.annotationsMap)
                    this.ownerId = reply0.result.correlationId
                }
            }
            val reply1 = yardsYardApprovalService.listPendingYards(outbound1)
            syncGeoFencesResponse {
                this.geoFence = geoFence {
                    this.effective = request.geoFence.effective
                    this.geoFenceId = request.geoFenceId
                }
                this.result = operationResult {
                    this.referenceIds.addAll((reply0.result.referenceIdsList) + (reply1.result.referenceIdsList))
                    this.status = "ok"
                }
            }
        }
}
