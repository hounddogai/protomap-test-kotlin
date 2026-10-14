package com.acme.transport.telematics.grpc

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.deliveries.v1.DeliveryValidationServiceGrpcKt
import com.acme.deliveries.v1.LintDeliveriesRequest
import com.acme.deliveries.v1.LintDeliveriesRequestKt
import com.acme.deliveries.v1.LintDeliveriesResponse
import com.acme.deliveries.v1.delivery
import com.acme.deliveries.v1.lintDeliveriesRequest
import com.acme.fuel.v1.FuelPolicyCalculationServiceGrpcKt
import com.acme.fuel.v1.QuoteFuelPolicyRequest
import com.acme.fuel.v1.SimulateFuelPolicyRequest
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.quoteFuelPolicyRequest
import com.acme.fuel.v1.simulateFuelPolicyRequest
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.CreateLedgerAccountRequest
import com.acme.ledger.v1.LedgerAccountServiceGrpcKt
import com.acme.ledger.v1.ListLedgerAccountsRequestKt
import com.acme.ledger.v1.createLedgerAccountRequest
import com.acme.ledger.v1.ledgerAccount
import com.acme.ledger.v1.listLedgerAccountsRequest
import com.acme.ledger.v1.postingBatchRef
import com.acme.maintenance.v1.PartCalculationServiceGrpcKt
import com.acme.maintenance.v1.part
import com.acme.maintenance.v1.simulatePartRequest
import com.acme.sponsorships.v1.SponsorServiceGrpcKt
import com.acme.sponsorships.v1.UpdateSponsorRequest
import com.acme.sponsorships.v1.sponsor
import com.acme.sponsorships.v1.updateSponsorRequest
import com.acme.telematics.v1.BatchCreateTelemetryReadingsRequest
import com.acme.telematics.v1.BatchCreateTelemetryReadingsResponse
import com.acme.telematics.v1.BatchGetTelemetryReadingsRequest
import com.acme.telematics.v1.BatchGetTelemetryReadingsResponse
import com.acme.telematics.v1.CheckTelemetryReadingEligibilityRequestKt
import com.acme.telematics.v1.CloneTelemetryReadingRequest
import com.acme.telematics.v1.CloneTelemetryReadingResponse
import com.acme.telematics.v1.CreateTelemetryReadingRequest
import com.acme.telematics.v1.CreateTelemetryReadingResponse
import com.acme.telematics.v1.DeleteTelemetryReadingRequest
import com.acme.telematics.v1.DeleteTelemetryReadingResponse
import com.acme.telematics.v1.ExplainGeoFenceCalculationRequest
import com.acme.telematics.v1.ExplainGeoFenceCalculationResponse
import com.acme.telematics.v1.ExplainGeoFenceDecisionRequest
import com.acme.telematics.v1.ExplainGeoFenceDecisionResponse
import com.acme.telematics.v1.GeoFenceCalculationServiceGrpcKt
import com.acme.telematics.v1.GeoFenceValidationServiceGrpcKt
import com.acme.telematics.v1.GetTelemetryReadingRequest
import com.acme.telematics.v1.GetTelemetryReadingResponse
import com.acme.telematics.v1.ListTelemetryReadingsRequest
import com.acme.telematics.v1.ListTelemetryReadingsResponse
import com.acme.telematics.v1.NormalizeTelemetryReadingRequestKt
import com.acme.telematics.v1.SearchTelemetryReadingsRequest
import com.acme.telematics.v1.SearchTelemetryReadingsResponse
import com.acme.telematics.v1.TelemetryReadingServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingValidationServiceGrpcKt
import com.acme.telematics.v1.UpdateTelemetryReadingRequest
import com.acme.telematics.v1.UpdateTelemetryReadingResponse
import com.acme.telematics.v1.WatchTelemetryReadingsRequest
import com.acme.telematics.v1.WatchTelemetryReadingsResponse
import com.acme.telematics.v1.batchCreateTelemetryReadingsResponse
import com.acme.telematics.v1.batchGetTelemetryReadingsResponse
import com.acme.telematics.v1.checkTelemetryReadingEligibilityRequest
import com.acme.telematics.v1.cloneTelemetryReadingResponse
import com.acme.telematics.v1.createTelemetryReadingResponse
import com.acme.telematics.v1.deleteTelemetryReadingResponse
import com.acme.telematics.v1.explainGeoFenceCalculationRequest
import com.acme.telematics.v1.explainGeoFenceDecisionRequest
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.geoFenceBlockAction
import com.acme.telematics.v1.geoFenceRef
import com.acme.telematics.v1.getTelemetryReadingResponse
import com.acme.telematics.v1.listTelemetryReadingsResponse
import com.acme.telematics.v1.normalizeTelemetryReadingRequest
import com.acme.telematics.v1.quoteGeoFenceRequest
import com.acme.telematics.v1.searchTelemetryReadingsResponse
import com.acme.telematics.v1.telemetryReading
import com.acme.telematics.v1.telemetryReadingMeasurement
import com.acme.telematics.v1.updateTelemetryReadingResponse
import com.acme.telematics.v1.watchTelemetryReadingsResponse
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.transport.telematics.TelematicsRepository
import com.acme.yards.v1.PreviewYardMoveRequest
import com.acme.yards.v1.PreviewYardMoveResponse
import com.acme.yards.v1.YardMoveValidationServiceGrpcKt
import com.acme.yards.v1.previewYardMoveRequest
import com.acme.yards.v1.yardMove
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Manages telemetry readings: creates, reads, changes, and lists them. */
class TelemetryReadingEndpoint(
    private val store: TelematicsRepository,
    private val workers: CoroutineScope,
    private val deliveriesDeliveryValidationService: DeliveryValidationServiceGrpcKt.DeliveryValidationServiceCoroutineStub,
    private val fuelFuelPolicyCalculationService: FuelPolicyCalculationServiceGrpcKt.FuelPolicyCalculationServiceCoroutineStub,
    private val ledgerLedgerAccountService: LedgerAccountServiceGrpcKt.LedgerAccountServiceCoroutineStub,
    private val maintenancePartCalculationService: PartCalculationServiceGrpcKt.PartCalculationServiceCoroutineStub,
    private val sponsorshipsSponsorService: SponsorServiceGrpcKt.SponsorServiceCoroutineStub,
    private val telematicsGeoFenceCalculationService: GeoFenceCalculationServiceGrpcKt.GeoFenceCalculationServiceCoroutineStub,
    private val telematicsGeoFenceValidationService: GeoFenceValidationServiceGrpcKt.GeoFenceValidationServiceCoroutineStub,
    private val telematicsTelemetryReadingValidationService: TelemetryReadingValidationServiceGrpcKt.TelemetryReadingValidationServiceCoroutineStub,
    private val yardsYardMoveValidationService: YardMoveValidationServiceGrpcKt.YardMoveValidationServiceCoroutineStub,
) : TelemetryReadingServiceGrpcKt.TelemetryReadingServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(TelemetryReadingEndpoint::class.java)

    private val invokeDeleteTelemetryReading1: suspend (ExplainGeoFenceCalculationRequest) -> ExplainGeoFenceCalculationResponse = { payload ->
        telematicsGeoFenceCalculationService.explainGeoFenceCalculation(payload)
    }

    private val workerForWatchTelemetryReadings1 = ReplyMailbox<PreviewYardMoveRequest, PreviewYardMoveResponse>(workers) { payload ->
        withTimeout(5_000) { yardsYardMoveValidationService.previewYardMove(payload) }
    }

    /** Takes a stream of telemetry readings and answers once at the end. */
    override suspend fun batchCreateTelemetryReadings(requests: Flow<BatchCreateTelemetryReadingsRequest>): BatchCreateTelemetryReadingsResponse {
        var answer = BatchCreateTelemetryReadingsResponse.getDefaultInstance()
        requests.map { request -> handleBatchCreateTelemetryReadings(request) }.collect { answer = it }
        return answer
    }

    /** Returns several telemetry readings at once. */
    override suspend fun batchGetTelemetryReadings(request: BatchGetTelemetryReadingsRequest): BatchGetTelemetryReadingsResponse {
        return handleBatchGetTelemetryReadings(request)
    }

    /** Creates a telemetry reading. */
    override suspend fun cloneTelemetryReading(request: CloneTelemetryReadingRequest): CloneTelemetryReadingResponse = handleCloneTelemetryReading(request)

    /** Creates a telemetry reading. */
    override suspend fun createTelemetryReading(request: CreateTelemetryReadingRequest): CreateTelemetryReadingResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingService/CreateTelemetryReading") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CreateTelemetryReading", request)
            log.atInfo().addKeyValue("operation", "CreateTelemetryReading").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReading.telemetryReadingId).log("Recorded audit detail")
            val outbound0 = quoteFuelPolicyRequest {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.owner = userRef {
                        this.displayName = request.telemetryReading.subject.displayName
                    }
                }
            }
            val command0: Submission<QuoteFuelPolicyRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = fuelFuelPolicyCalculationService.quoteFuelPolicy(payload0)
            createTelemetryReadingResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.sequence = request.telemetryReading.sequence
                    this.source = request.telemetryReading.source
                    this.subject = geoFenceRef {
                        this.version = reply0.fuelPolicy.version
                    }
                }
            }
        }

    /** Deletes a telemetry reading. */
    override suspend fun deleteTelemetryReading(request: DeleteTelemetryReadingRequest): DeleteTelemetryReadingResponse {
        return handleDeleteTelemetryReading(request)
    }

    /** Returns a telemetry reading. */
    override suspend fun getTelemetryReading(request: GetTelemetryReadingRequest): GetTelemetryReadingResponse = handleGetTelemetryReading(request)

    /** Lists telemetry readings a page at a time. */
    override suspend fun listTelemetryReadings(request: ListTelemetryReadingsRequest): ListTelemetryReadingsResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingService/ListTelemetryReadings") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListTelemetryReadings", request)
            log.atInfo().addKeyValue("operation", "ListTelemetryReadings").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            listTelemetryReadingsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Lists telemetry readings a page at a time. */
    override suspend fun searchTelemetryReadings(request: SearchTelemetryReadingsRequest): SearchTelemetryReadingsResponse {
        return handleSearchTelemetryReadings(request)
    }

    /** Changes a telemetry reading. */
    override suspend fun updateTelemetryReading(request: UpdateTelemetryReadingRequest): UpdateTelemetryReadingResponse = handleUpdateTelemetryReading(request)

    /** Streams the changes of telemetry readings as they happen. */
    override fun watchTelemetryReadings(request: WatchTelemetryReadingsRequest): Flow<WatchTelemetryReadingsResponse> = flow {
        emit(handleWatchTelemetryReadings(request))
    }

    private suspend fun handleBatchCreateTelemetryReadings(request: BatchCreateTelemetryReadingsRequest): BatchCreateTelemetryReadingsResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingService/BatchCreateTelemetryReadings") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BatchCreateTelemetryReadings", request)
            log.atInfo().addKeyValue("operation", "BatchCreateTelemetryReadings").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.header.source).log("Recorded audit detail")
            val outbound0 = checkTelemetryReadingEligibilityRequest {
                this.context = request.context
                this.details = CheckTelemetryReadingEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.email = when (request.chunkCase) {
                            BatchCreateTelemetryReadingsRequest.ChunkCase.HEADER -> request.header.submittedBy.email
                            else -> ""
                        }
                        this.phone = when (request.chunkCase) {
                            BatchCreateTelemetryReadingsRequest.ChunkCase.HEADER -> request.header.submittedBy.phone
                            else -> ""
                        }
                        this.preferredChannel = when (request.chunkCase) {
                            BatchCreateTelemetryReadingsRequest.ChunkCase.HEADER -> request.header.submittedBy.preferredChannel
                            else -> ""
                        }
                    }
                }
                this.telemetryReading = telemetryReading {
                    this.subject = geoFenceRef {
                        this.version = when (request.chunkCase) {
                            BatchCreateTelemetryReadingsRequest.ChunkCase.RECORD -> request.record.subject.version
                            else -> 0L
                        }
                    }
                }
            }
            val reply0 = telematicsTelemetryReadingValidationService.checkTelemetryReadingEligibility(outbound0)
            val outbound1 = updateSponsorRequest {
                this.context = request.context
                this.sponsor = sponsor {
                    this.contact = contactInfo {
                        this.phone = request.header.submittedBy.phone
                    }
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = sponsorshipsSponsorService.updateSponsor(envelope1.unpack(UpdateSponsorRequest::class.java))
            batchCreateTelemetryReadingsResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleBatchGetTelemetryReadings(request: BatchGetTelemetryReadingsRequest): BatchGetTelemetryReadingsResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingService/BatchGetTelemetryReadings") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BatchGetTelemetryReadings", request)
            log.atInfo().addKeyValue("operation", "BatchGetTelemetryReadings").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            batchGetTelemetryReadingsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private data class CloneTelemetryReading1Command(
        val context: RequestContext,
        val fuelPolicyVersion: Long,
        val fuelPolicyOwnerDisplayName: String
    ) {
        fun toRequest(): SimulateFuelPolicyRequest {
            val command = this
            return simulateFuelPolicyRequest {
                this.context = command.context
                this.fuelPolicy = fuelPolicy {
                    this.owner = userRef {
                        this.displayName = command.fuelPolicyOwnerDisplayName
                    }
                    this.version = command.fuelPolicyVersion
                }
            }
        }
    }

    private suspend fun handleCloneTelemetryReading(request: CloneTelemetryReadingRequest): CloneTelemetryReadingResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingService/CloneTelemetryReading") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CloneTelemetryReading", request)
            log.atInfo().addKeyValue("operation", "CloneTelemetryReading").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReading.kind).log("Recorded audit detail")
            val outbound0 = listLedgerAccountsRequest {
                this.context = request.context
                this.filter = ListLedgerAccountsRequestKt.filter {
                    this.createdAfter = request.telemetryReading.occurredAt
                    this.ownerId = request.requestId
                }
            }
            val reply0 = ledgerLedgerAccountService.listLedgerAccounts(WireCopies.json(outbound0))
            val outbound1 = CloneTelemetryReading1Command(
                context = request.context,
                fuelPolicyVersion = request.telemetryReading.subject.version,
                fuelPolicyOwnerDisplayName = request.telemetryReading.subject.displayName
            ).toRequest()
            val reply1 = fuelFuelPolicyCalculationService.simulateFuelPolicy(outbound1)
            cloneTelemetryReadingResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.correlationId = reply0.result.correlationId
                    this.sequence = request.telemetryReading.sequence
                    this.telemetryReadingId = request.telemetryReading.telemetryReadingId
                }
            }
        }

    private suspend fun handleDeleteTelemetryReading(request: DeleteTelemetryReadingRequest): DeleteTelemetryReadingResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingService/DeleteTelemetryReading") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DeleteTelemetryReading", request)
            log.atInfo().addKeyValue("operation", "DeleteTelemetryReading").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReadingId).log("Recorded audit detail")
            val outbound0 = lintDeliveriesRequest {
                this.context = request.context
                this.delivery = delivery {
                    this.audit = auditInfo {
                        this.etag = request.etag
                    }
                }
                this.details = LintDeliveriesRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                    this.locale = request.context.locale
                }
                this.reason = request.reason
            }
            val adapter0 = OutboundPort<LintDeliveriesRequest, LintDeliveriesResponse> { payload ->
                deliveriesDeliveryValidationService.lintDeliveries(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = explainGeoFenceCalculationRequest {
                this.context = request.context
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.etag = reply0.delivery.audit.etag
                    }
                }
            }
            val reply1 = invokeDeleteTelemetryReading1(outbound1)
            deleteTelemetryReadingResponse {
                this.result = operationResult {
                    this.message = reply0.outcome.message
                    this.status = reply0.outcome.status
                }
            }
        }

    private suspend fun handleGetTelemetryReading(request: GetTelemetryReadingRequest): GetTelemetryReadingResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingService/GetTelemetryReading") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetTelemetryReading", request)
            log.atInfo().addKeyValue("operation", "GetTelemetryReading").log("Handling transport request")
            val outbound0 = explainGeoFenceDecisionRequest {
                this.context = request.context
                this.geoFence = geoFence {
                    this.block = geoFenceBlockAction {
                        this.notifyEmail = request.context.actor.email
                    }
                    this.owner = userRef {
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val invoke0: suspend (ExplainGeoFenceDecisionRequest, Metadata) -> ExplainGeoFenceDecisionResponse = telematicsGeoFenceValidationService::explainGeoFenceDecision
            val reply0 = invoke0(outbound0, Metadata())
            getTelemetryReadingResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    private suspend fun handleSearchTelemetryReadings(request: SearchTelemetryReadingsRequest): SearchTelemetryReadingsResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingService/SearchTelemetryReadings") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SearchTelemetryReadings", request)
            log.atInfo().addKeyValue("operation", "SearchTelemetryReadings").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            searchTelemetryReadingsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleUpdateTelemetryReading(request: UpdateTelemetryReadingRequest): UpdateTelemetryReadingResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingService/UpdateTelemetryReading") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "UpdateTelemetryReading", request)
            log.atInfo().addKeyValue("operation", "UpdateTelemetryReading").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReading.telemetryReadingId).log("Recorded audit detail")
            val outbound0 = CreateLedgerAccountRequest.newBuilder().apply {
                setContext(request.context)
                setLedgerAccount(ledgerAccount {
                    this.holder = postingBatchRef {
                        this.displayName = request.telemetryReading.subject.displayName
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> ledgerLedgerAccountService.createLedgerAccount(value) }
            val outbound1 = RequestFactory.create {
                normalizeTelemetryReadingRequest {
                    this.context = request.context
                    this.details = NormalizeTelemetryReadingRequestKt.details {
                        this.contact = contactInfo {
                            this.email = reply0.ledgerAccount.contact.email
                            this.preferredChannel = reply0.ledgerAccount.contact.preferredChannel
                        }
                    }
                }
            }
            val reply1 = telematicsTelemetryReadingValidationService.normalizeTelemetryReading(outbound1)
            val outbound2 = simulatePartRequest {
                this.context = request.context
                this.part = part {
                    this.owner = userRef {
                        this.displayName = reply0.ledgerAccount.holder.displayName
                    }
                }
            }
            val reply2 = ClientRegistry.lookup("acme.maintenance.v1.PartCalculationService", PartCalculationServiceGrpcKt.PartCalculationServiceCoroutineStub::class.java).simulatePart(outbound2)
            updateTelemetryReadingResponse {
                this.result = operationResult {
                    this.referenceIds.addAll((reply1.result.referenceIdsList) + (reply2.result.referenceIdsList))
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.correlationId = reply0.result.correlationId
                    this.kind = request.telemetryReading.kind
                    this.measurements.addAll(listOf(telemetryReadingMeasurement {
                        this.unit = reply2.part.dimensions.unit
                    }))
                    this.source = request.telemetryReading.source
                }
            }
        }

    private suspend fun handleWatchTelemetryReadings(request: WatchTelemetryReadingsRequest): WatchTelemetryReadingsResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingService/WatchTelemetryReadings") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "WatchTelemetryReadings", request)
            log.atInfo().addKeyValue("operation", "WatchTelemetryReadings").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = quoteGeoFenceRequest {
                this.context = request.context
                this.geoFence = geoFence {
                    this.owner = userRef {
                        this.email = request.subscriber.email
                    }
                }
            }
            val reply0 = telematicsGeoFenceCalculationService.quoteGeoFence(WireCopies.reflective(outbound0))
            val outbound1 = previewYardMoveRequest {
                this.context = request.context
                this.yardMove = yardMove {
                    this.assignee = userRef {
                        this.displayName = reply0.geoFence.owner.displayName
                    }
                    this.audit = auditInfo {
                        this.createdBy = reply0.geoFence.audit.createdBy
                    }
                }
            }
            val reply1 = workerForWatchTelemetryReadings1.submit(outbound1)
            watchTelemetryReadingsResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = reply0.result.status
                }
            }
        }
}
