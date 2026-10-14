package com.acme.transport.fuel.adapters

import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.customers.v1.HouseholdCalculationServiceGrpcKt
import com.acme.customers.v1.QuoteHouseholdRequest
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.household
import com.acme.customers.v1.quoteHouseholdRequest
import com.acme.customers.v1.simulateHouseholdRequest
import com.acme.deliveries.v1.CommentProofOfDeliveryApprovalResponse
import com.acme.deliveries.v1.ProofOfDeliveryApprovalServiceGrpcKt
import com.acme.deliveries.v1.commentProofOfDeliveryApprovalRequest
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.proofOfDeliveryLine
import com.acme.deliveries.v1.proofOfDeliveryLineTax
import com.acme.drivers.v1.CalculateDriverLicenseRequestKt
import com.acme.drivers.v1.DriverLicenseCalculationServiceGrpcKt
import com.acme.drivers.v1.calculateDriverLicenseRequest
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverLicenseCardSettlement
import com.acme.drivers.v1.driverRef
import com.acme.fuel.v1.FuelCardLimit
import com.acme.fuel.v1.FuelCardQueryServiceGrpcKt
import com.acme.fuel.v1.FuelCardSyncServiceGrpcKt
import com.acme.fuel.v1.FuelCardValidationServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyCalculationServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyExportServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionExportServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionImportServiceGrpcKt
import com.acme.fuel.v1.GetFuelCardSyncStateRequest
import com.acme.fuel.v1.GetFuelCardSyncStateResponse
import com.acme.fuel.v1.LintFuelCardsRequest
import com.acme.fuel.v1.LintFuelCardsRequestKt
import com.acme.fuel.v1.LintFuelCardsResponse
import com.acme.fuel.v1.ListFuelCardConflictsRequest
import com.acme.fuel.v1.ListFuelCardConflictsResponse
import com.acme.fuel.v1.ListFuelTransactionExportsRequest
import com.acme.fuel.v1.ListFuelTransactionExportsRequestKt
import com.acme.fuel.v1.ListFuelTransactionExportsResponse
import com.acme.fuel.v1.PullFuelCardsRequest
import com.acme.fuel.v1.PullFuelCardsResponse
import com.acme.fuel.v1.PullFuelCardsResponseKt
import com.acme.fuel.v1.PushFuelCardRequest
import com.acme.fuel.v1.PushFuelCardResponse
import com.acme.fuel.v1.PushFuelCardResponseKt
import com.acme.fuel.v1.ResolveFuelCardConflictRequest
import com.acme.fuel.v1.ResolveFuelCardConflictResponse
import com.acme.fuel.v1.ResolveFuelCardConflictResponseKt
import com.acme.fuel.v1.RetryFuelTransactionExportRequestKt
import com.acme.fuel.v1.SimulateFuelPolicyResponse
import com.acme.fuel.v1.StreamFuelCardChangesRequest
import com.acme.fuel.v1.StreamFuelCardChangesResponse
import com.acme.fuel.v1.SyncFuelCardsRequest
import com.acme.fuel.v1.SyncFuelCardsResponse
import com.acme.fuel.v1.ValidateFuelTransactionImportRequestKt
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.fuelPolicyDiscountAction
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.getFuelCardSyncStateResponse
import com.acme.fuel.v1.lintFuelCardsRequest
import com.acme.fuel.v1.listFuelCardConflictsResponse
import com.acme.fuel.v1.listFuelTransactionExportsRequest
import com.acme.fuel.v1.pullFuelCardsResponse
import com.acme.fuel.v1.pushFuelCardResponse
import com.acme.fuel.v1.resolveFuelCardConflictResponse
import com.acme.fuel.v1.resolveFuelCardsRequest
import com.acme.fuel.v1.retryFuelPolicyExportRequest
import com.acme.fuel.v1.retryFuelTransactionExportRequest
import com.acme.fuel.v1.simulateFuelPolicyRequest
import com.acme.fuel.v1.streamFuelCardChangesResponse
import com.acme.fuel.v1.syncFuelCardsResponse
import com.acme.fuel.v1.validateFuelTransactionImportRequest
import com.acme.identity.v1.userRef
import com.acme.metrics.v1.MetricSeriesValidationServiceGrpcKt
import com.acme.metrics.v1.ValidateMetricSeriesRequestKt
import com.acme.metrics.v1.metricSeries
import com.acme.metrics.v1.validateMetricSeriesRequest
import com.acme.transport.fuel.FuelRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.venues.v1.NormalizeVenueRequest
import com.acme.venues.v1.NormalizeVenueRequestKt
import com.acme.venues.v1.NormalizeVenueResponse
import com.acme.venues.v1.VenueValidationServiceGrpcKt
import com.acme.venues.v1.normalizeVenueRequest
import com.acme.venues.v1.venue
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Keeps fuel cards in step with the systems of record in other regions. */
class FuelCardSyncController(
    private val store: FuelRepository,
    private val workers: CoroutineScope,
    private val customersHouseholdCalculationService: HouseholdCalculationServiceGrpcKt.HouseholdCalculationServiceCoroutineStub,
    private val deliveriesProofOfDeliveryApprovalService: ProofOfDeliveryApprovalServiceGrpcKt.ProofOfDeliveryApprovalServiceCoroutineStub,
    private val driversDriverLicenseCalculationService: DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub,
    private val fuelFuelCardQueryService: FuelCardQueryServiceGrpcKt.FuelCardQueryServiceCoroutineStub,
    private val fuelFuelCardValidationService: FuelCardValidationServiceGrpcKt.FuelCardValidationServiceCoroutineStub,
    private val fuelFuelPolicyCalculationService: FuelPolicyCalculationServiceGrpcKt.FuelPolicyCalculationServiceCoroutineStub,
    private val fuelFuelPolicyExportService: FuelPolicyExportServiceGrpcKt.FuelPolicyExportServiceCoroutineStub,
    private val fuelFuelTransactionExportService: FuelTransactionExportServiceGrpcKt.FuelTransactionExportServiceCoroutineStub,
    private val fuelFuelTransactionImportService: FuelTransactionImportServiceGrpcKt.FuelTransactionImportServiceCoroutineStub,
    private val metricsMetricSeriesValidationService: MetricSeriesValidationServiceGrpcKt.MetricSeriesValidationServiceCoroutineStub,
    private val venuesVenueValidationService: VenueValidationServiceGrpcKt.VenueValidationServiceCoroutineStub,
) : FuelCardSyncServiceGrpcKt.FuelCardSyncServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(FuelCardSyncController::class.java)

    private val invokeGetFuelCardSyncState0: suspend (NormalizeVenueRequest) -> NormalizeVenueResponse = { payload ->
        venuesVenueValidationService.normalizeVenue(payload)
    }

    private val workerForPushFuelCard3 = ReplyMailbox<ListFuelTransactionExportsRequest, ListFuelTransactionExportsResponse>(workers) { payload ->
        withTimeout(5_000) { fuelFuelTransactionExportService.listFuelTransactionExports(payload) }
    }

    private val clientForResolveFuelCardConflict0 by lazy { metricsMetricSeriesValidationService }

    /** Returns a fuel card. */
    override suspend fun getFuelCardSyncState(request: GetFuelCardSyncStateRequest): GetFuelCardSyncStateResponse = handleGetFuelCardSyncState(request)

    /** Lists fuel cards a page at a time. */
    override suspend fun listFuelCardConflicts(request: ListFuelCardConflictsRequest): ListFuelCardConflictsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardSyncService/ListFuelCardConflicts") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListFuelCardConflicts", request)
            log.atInfo().addKeyValue("operation", "ListFuelCardConflicts").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            listFuelCardConflictsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Pulls a fuel card. */
    override suspend fun pullFuelCards(request: PullFuelCardsRequest): PullFuelCardsResponse {
        return handlePullFuelCards(request)
    }

    /** Pushs a fuel card. */
    override suspend fun pushFuelCard(request: PushFuelCardRequest): PushFuelCardResponse = handlePushFuelCard(request)

    /** Resolves a fuel card. */
    override suspend fun resolveFuelCardConflict(request: ResolveFuelCardConflictRequest): ResolveFuelCardConflictResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardSyncService/ResolveFuelCardConflict") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResolveFuelCardConflict", request)
            log.atInfo().addKeyValue("operation", "ResolveFuelCardConflict").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCard.fuelCardId).log("Recorded audit detail")
            val outbound0 = validateMetricSeriesRequest {
                this.context = request.context
                this.details = ValidateMetricSeriesRequestKt.details {
                    this.contact = contactInfo {
                        this.name = request.fuelCard.contact.name
                    }
                }
                this.metricSeries = metricSeries {
                    this.audit = auditInfo {
                        this.createdAt = request.fuelCard.audit.createdAt
                        this.updatedAt = request.fuelCard.audit.updatedAt
                    }
                    this.owner = userRef {
                        this.email = request.fuelCard.holder.email
                    }
                }
            }
            val reply0 = clientForResolveFuelCardConflict0.validateMetricSeries(outbound0)
            val outbound1 = retryFuelTransactionExportRequest {
                this.context = request.context
                this.details = RetryFuelTransactionExportRequestKt.details {
                    this.notify = contactInfo {
                        this.email = request.fuelCard.contact.email
                    }
                }
                this.fuelTransaction = fuelTransaction {
                    this.subject = customerRef {
                        this.displayName = request.fuelCard.holder.displayName
                    }
                }
            }
            val reply1 = flowOf(outbound1).map { value -> fuelFuelTransactionExportService.retryFuelTransactionExport(value) }.single()
            resolveFuelCardConflictResponse {
                this.fuelCard = fuelCard {
                    this.available = request.fuelCard.available
                    this.contact = contactInfo {
                        this.email = reply1.fuelTransaction.subject.email
                    }
                    this.fuelCardId = request.fuelCardId
                }
                this.outcome = ResolveFuelCardConflictResponseKt.outcome {
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    /** Exchanges changes of fuel cards with a peer. */
    override fun streamFuelCardChanges(requests: Flow<StreamFuelCardChangesRequest>): Flow<StreamFuelCardChangesResponse> =
        requests.map { request -> handleStreamFuelCardChanges(request) }

    /** Syncs a fuel card. */
    override suspend fun syncFuelCards(request: SyncFuelCardsRequest): SyncFuelCardsResponse = handleSyncFuelCards(request)

    private suspend fun handleGetFuelCardSyncState(request: GetFuelCardSyncStateRequest): GetFuelCardSyncStateResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardSyncService/GetFuelCardSyncState") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetFuelCardSyncState", request)
            log.atInfo().addKeyValue("operation", "GetFuelCardSyncState").log("Handling transport request")
            val outbound0 = normalizeVenueRequest {
                this.context = request.context
                this.details = NormalizeVenueRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
                this.venue = venue {
                    this.owner = userRef {
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply0 = invokeGetFuelCardSyncState0(outbound0)
            getFuelCardSyncStateResponse {
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.createdBy = reply0.venue.audit.createdBy
                    }
                    this.fuelCardId = request.fuelCardId
                }
                this.result = operationResult {
                    this.status = reply0.outcome.status
                }
            }
        }

    private suspend fun handlePullFuelCards(request: PullFuelCardsRequest): PullFuelCardsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardSyncService/PullFuelCards") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PullFuelCards", request)
            log.atInfo().addKeyValue("operation", "PullFuelCards").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCard.fuelCardId).log("Recorded audit detail")
            val outbound0 = lintFuelCardsRequest {
                this.context = request.context
                this.details = LintFuelCardsRequestKt.details {
                    this.contact = contactInfo {
                        this.name = request.fuelCard.contact.name
                    }
                }
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.updatedAt = request.fuelCard.audit.updatedAt
                    }
                }
            }
            val invoke0: suspend (LintFuelCardsRequest, Metadata) -> LintFuelCardsResponse = fuelFuelCardValidationService::lintFuelCards
            val reply0 = invoke0(outbound0, Metadata())
            val outbound1 = QuoteHouseholdRequest.newBuilder().apply {
                setContext(request.context)
                setHousehold(household {
                    this.children.addAll(listOf(household {
                        this.children.addAll(listOf(household {
                            this.manager = userRef {
                                this.email = request.fuelCard.holder.email
                            }
                        }))
                    }))
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> customersHouseholdCalculationService.quoteHousehold(value) }
            pullFuelCardsResponse {
                this.fuelCard = fuelCard {
                    this.fuelCardId = request.fuelCardId
                    this.openedAt = request.fuelCard.openedAt
                }
                this.outcome = PullFuelCardsResponseKt.outcome {
                    this.effectiveAt = reply0.outcome.effectiveAt
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handlePushFuelCard(request: PushFuelCardRequest): PushFuelCardResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardSyncService/PushFuelCard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PushFuelCard", request)
            log.atInfo().addKeyValue("operation", "PushFuelCard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCard.fuelCardId).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                simulateHouseholdRequest {
                    this.context = request.context
                    this.household = household {
                        this.children.addAll(listOf(household {
                            this.children.addAll(listOf(household {
                                this.audit = auditInfo {
                                    this.createdAt = request.fuelCard.audit.createdAt
                                }
                                this.manager = userRef {
                                    this.displayName = request.fuelCard.holder.displayName
                                }
                            }))
                        }))
                    }
                }
            }
            val reply0 = customersHouseholdCalculationService.simulateHousehold(outbound0)
            val outbound1 = validateFuelTransactionImportRequest {
                this.context = request.context
                this.details = ValidateFuelTransactionImportRequestKt.details {
                    this.contact = contactInfo {
                        this.email = reply0.household.manager.email
                        this.phone = request.fuelCard.contact.phone
                    }
                }
                this.fuelTransaction = fuelTransaction {
                    this.subject = customerRef {
                        this.displayName = request.fuelCard.holder.displayName
                        this.email = request.fuelCard.contact.email
                    }
                }
            }
            val reply1 = ClientRegistry.lookup("acme.fuel.v1.FuelTransactionImportService", FuelTransactionImportServiceGrpcKt.FuelTransactionImportServiceCoroutineStub::class.java).validateFuelTransactionImport(outbound1)
            val outbound2 = resolveFuelCardsRequest {
                this.context = request.context
            }
            val reply2 = fuelFuelCardQueryService.resolveFuelCards(WireCopies.reflective(outbound2))
            val outbound3 = listFuelTransactionExportsRequest {
                this.context = request.context
                this.filter = ListFuelTransactionExportsRequestKt.filter {
                    this.labels.putAll(request.fuelCard.labelsMap)
                    this.region = reply0.household.location.region
                }
            }
            val reply3 = workerForPushFuelCard3.submit(outbound3)
            pushFuelCardResponse {
                this.fuelCard = fuelCard {
                    this.fuelCardId = request.fuelCardId
                    this.openedAt = request.fuelCard.openedAt
                }
                this.outcome = PushFuelCardResponseKt.outcome {
                    this.message = reply0.result.message
                    this.status = reply3.result.status
                }
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = reply1.result.status
                }
            }
        }

    private suspend fun handleStreamFuelCardChanges(request: StreamFuelCardChangesRequest): StreamFuelCardChangesResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardSyncService/StreamFuelCardChanges") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StreamFuelCardChanges", request)
            log.atInfo().addKeyValue("operation", "StreamFuelCardChanges").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.change.fuelCardId).log("Recorded audit detail")
            var reply0 = SimulateFuelPolicyResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<FuelCardLimit>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound0 = simulateFuelPolicyRequest {
                            this.context = request.context
                            this.fuelPolicy = fuelPolicy {
                                this.audit = auditInfo {
                                    this.createdBy = request.change.audit.createdBy
                                }
                                this.discount = fuelPolicyDiscountAction {
                                    this.amount = item.amount
                                }
                                this.name = item.name
                            }
                        }
                        reply0 = fuelFuelPolicyCalculationService.simulateFuelPolicy(outbound0)
                    }
                }
                try {
                    request.change.limitsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val configure1: CalculateDriverLicenseRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.driverLicense = driverLicense {
                    this.card = driverLicenseCardSettlement {
                        this.token = request.change.card.token
                    }
                }
            }
            val outbound1 = calculateDriverLicenseRequest(configure1)
            val reply1 = driversDriverLicenseCalculationService.calculateDriverLicense(outbound1)
            streamFuelCardChangesResponse {
                this.fuelCard = fuelCard {
                    this.contact = contactInfo {
                        this.email = reply1.driverLicense.counterparty.email
                    }
                    this.holder = request.change.holder
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.status = request.ack.status
                }
            }
        }

    private suspend fun handleSyncFuelCards(request: SyncFuelCardsRequest): SyncFuelCardsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardSyncService/SyncFuelCards") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SyncFuelCards", request)
            log.atInfo().addKeyValue("operation", "SyncFuelCards").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCard.fuelCardId).log("Recorded audit detail")
            val outbound0 = retryFuelPolicyExportRequest {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.updatedAt = request.fuelCard.audit.updatedAt
                        this.updatedBy = request.fuelCard.audit.updatedBy
                    }
                }
            }
            val reply0 = fuelFuelPolicyExportService.retryFuelPolicyExport(outbound0)
            var reply1 = CommentProofOfDeliveryApprovalResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.fuelCard.limitsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound1 = commentProofOfDeliveryApprovalRequest {
                                this.context = request.context
                                this.proofOfDelivery = proofOfDelivery {
                                    this.audit = auditInfo {
                                        this.createdBy = reply0.fuelPolicy.audit.createdBy
                                    }
                                    this.counterparty = driverRef {
                                        this.email = request.fuelCard.contact.email
                                    }
                                    this.lines.addAll(listOf(proofOfDeliveryLine {
                                        this.tax = proofOfDeliveryLineTax {
                                            this.amount = item.amount
                                        }
                                    }))
                                }
                            }
                            deliveriesProofOfDeliveryApprovalService.commentProofOfDeliveryApproval(outbound1)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply1 = it }
                }
            }
            syncFuelCardsResponse {
                this.fuelCard = fuelCard {
                    this.balance = request.fuelCard.balance
                    this.fuelCardId = request.fuelCardId
                    this.holder = driverRef {
                        this.email = reply0.fuelPolicy.owner.email
                    }
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }
}
