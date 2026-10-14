package com.acme.transport.fuel.api

import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.pageRequest
import com.acme.customers.v1.CheckCustomerEligibilityResponse
import com.acme.customers.v1.CheckCustomerSegmentEligibilityRequestKt
import com.acme.customers.v1.CustomerQueryServiceGrpcKt
import com.acme.customers.v1.CustomerSegmentValidationServiceGrpcKt
import com.acme.customers.v1.CustomerValidationServiceGrpcKt
import com.acme.customers.v1.SuggestCustomersRequest
import com.acme.customers.v1.SuggestCustomersRequestKt
import com.acme.customers.v1.checkCustomerEligibilityRequest
import com.acme.customers.v1.checkCustomerSegmentEligibilityRequest
import com.acme.customers.v1.customer
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.customerSegment
import com.acme.customers.v1.resolveCustomersRequest
import com.acme.customers.v1.suggestCustomersRequest
import com.acme.documents.v1.DocumentQueryServiceGrpcKt
import com.acme.documents.v1.ResolveDocumentsRequest
import com.acme.documents.v1.ResolveDocumentsResponse
import com.acme.documents.v1.resolveDocumentsRequest
import com.acme.fuel.v1.CancelFuelTransactionExportRequestKt
import com.acme.fuel.v1.CancelFuelTransactionExportResponse
import com.acme.fuel.v1.FuelCardImportServiceGrpcKt
import com.acme.fuel.v1.FuelCardServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionExportServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionImportServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionMeasurement
import com.acme.fuel.v1.FuelTransactionSyncServiceGrpcKt
import com.acme.fuel.v1.ListFuelCardImportsRequestKt
import com.acme.fuel.v1.ListFuelCardImportsResponse
import com.acme.fuel.v1.ListFuelTransactionConflictsRequest
import com.acme.fuel.v1.ListFuelTransactionConflictsResponse
import com.acme.fuel.v1.MapFuelTransactionImportColumnsResponse
import com.acme.fuel.v1.PullFuelTransactionsRequest
import com.acme.fuel.v1.PullFuelTransactionsResponse
import com.acme.fuel.v1.PushFuelTransactionRequest
import com.acme.fuel.v1.PushFuelTransactionResponse
import com.acme.fuel.v1.PushFuelTransactionResponseKt
import com.acme.fuel.v1.ReconcileFuelTransactionsRequest
import com.acme.fuel.v1.ReconcileFuelTransactionsResponse
import com.acme.fuel.v1.ReconcileFuelTransactionsResponseKt
import com.acme.fuel.v1.ResolveFuelTransactionConflictRequest
import com.acme.fuel.v1.ResolveFuelTransactionConflictResponse
import com.acme.fuel.v1.SearchFuelCardsRequestKt
import com.acme.fuel.v1.StreamFuelTransactionChangesRequest
import com.acme.fuel.v1.StreamFuelTransactionChangesResponse
import com.acme.fuel.v1.SyncFuelTransactionsRequest
import com.acme.fuel.v1.SyncFuelTransactionsResponse
import com.acme.fuel.v1.cancelFuelTransactionExportRequest
import com.acme.fuel.v1.copy as fuelCopy
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.fuelTransactionMeasurement
import com.acme.fuel.v1.listFuelCardImportsRequest
import com.acme.fuel.v1.listFuelTransactionConflictsResponse
import com.acme.fuel.v1.mapFuelTransactionImportColumnsRequest
import com.acme.fuel.v1.pullFuelTransactionsResponse
import com.acme.fuel.v1.pushFuelTransactionResponse
import com.acme.fuel.v1.reconcileFuelTransactionsResponse
import com.acme.fuel.v1.resolveFuelTransactionConflictResponse
import com.acme.fuel.v1.searchFuelCardsRequest
import com.acme.fuel.v1.streamFuelTransactionChangesResponse
import com.acme.fuel.v1.syncFuelTransactionsResponse
import com.acme.identity.v1.userRef
import com.acme.recruiting.v1.JobOpeningServiceGrpcKt
import com.acme.recruiting.v1.ListJobOpeningsRequest
import com.acme.recruiting.v1.ListJobOpeningsRequestKt
import com.acme.recruiting.v1.listJobOpeningsRequest
import com.acme.transport.fuel.FuelRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.travel.v1.ListTripsRequestKt
import com.acme.travel.v1.TripServiceGrpcKt
import com.acme.travel.v1.listTripsRequest
import com.acme.yards.v1.ApproveYardRequestKt
import com.acme.yards.v1.ListYardMoveReportsRequestKt
import com.acme.yards.v1.SubmitYardRequestKt
import com.acme.yards.v1.YardApprovalServiceGrpcKt
import com.acme.yards.v1.YardMoveApprovalServiceGrpcKt
import com.acme.yards.v1.YardMoveReportingServiceGrpcKt
import com.acme.yards.v1.approveYardRequest
import com.acme.yards.v1.delegateYardMoveApprovalRequest
import com.acme.yards.v1.listYardMoveReportsRequest
import com.acme.yards.v1.submitYardRequest
import com.acme.yards.v1.yard
import com.acme.yards.v1.yardMove
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Keeps fuel transactions in step with the systems of record in other regions. */
class FuelTransactionSyncGrpcService(
    private val store: FuelRepository,
    private val workers: CoroutineScope,
    private val customersCustomerQueryService: CustomerQueryServiceGrpcKt.CustomerQueryServiceCoroutineStub,
    private val customersCustomerSegmentValidationService: CustomerSegmentValidationServiceGrpcKt.CustomerSegmentValidationServiceCoroutineStub,
    private val customersCustomerValidationService: CustomerValidationServiceGrpcKt.CustomerValidationServiceCoroutineStub,
    private val documentsDocumentQueryService: DocumentQueryServiceGrpcKt.DocumentQueryServiceCoroutineStub,
    private val fuelFuelCardImportService: FuelCardImportServiceGrpcKt.FuelCardImportServiceCoroutineStub,
    private val fuelFuelCardService: FuelCardServiceGrpcKt.FuelCardServiceCoroutineStub,
    private val fuelFuelTransactionExportService: FuelTransactionExportServiceGrpcKt.FuelTransactionExportServiceCoroutineStub,
    private val fuelFuelTransactionImportService: FuelTransactionImportServiceGrpcKt.FuelTransactionImportServiceCoroutineStub,
    private val recruitingJobOpeningService: JobOpeningServiceGrpcKt.JobOpeningServiceCoroutineStub,
    private val travelTripService: TripServiceGrpcKt.TripServiceCoroutineStub,
    private val yardsYardApprovalService: YardApprovalServiceGrpcKt.YardApprovalServiceCoroutineStub,
    private val yardsYardMoveApprovalService: YardMoveApprovalServiceGrpcKt.YardMoveApprovalServiceCoroutineStub,
    private val yardsYardMoveReportingService: YardMoveReportingServiceGrpcKt.YardMoveReportingServiceCoroutineStub,
) : FuelTransactionSyncServiceGrpcKt.FuelTransactionSyncServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(FuelTransactionSyncGrpcService::class.java)

    private val workerForReconcileFuelTransactions0 = ReplyMailbox<ResolveDocumentsRequest, ResolveDocumentsResponse>(workers) { payload ->
        withTimeout(5_000) { documentsDocumentQueryService.resolveDocuments(payload) }
    }

    private val clientForReconcileFuelTransactions1 by lazy { yardsYardApprovalService }

    /** Lists fuel transactions a page at a time. */
    override suspend fun listFuelTransactionConflicts(request: ListFuelTransactionConflictsRequest): ListFuelTransactionConflictsResponse {
        return handleListFuelTransactionConflicts(request)
    }

    /** Pulls a fuel transaction. */
    override suspend fun pullFuelTransactions(request: PullFuelTransactionsRequest): PullFuelTransactionsResponse = handlePullFuelTransactions(request)

    /** Pushs a fuel transaction. */
    override suspend fun pushFuelTransaction(request: PushFuelTransactionRequest): PushFuelTransactionResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionSyncService/PushFuelTransaction") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PushFuelTransaction", request)
            log.atInfo().addKeyValue("operation", "PushFuelTransaction").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransaction.fuelTransactionId).log("Recorded audit detail")
            val outbound0 = listTripsRequest {
                this.context = request.context
                this.filter = ListTripsRequestKt.filter {
                    this.createdAfter = request.fuelTransaction.occurredAt
                    this.ownerId = request.fuelTransaction.correlationId
                }
            }
            val reply0 = ClientRegistry.lookup("acme.travel.v1.TripService", TripServiceGrpcKt.TripServiceCoroutineStub::class.java).listTrips(outbound0)
            val outbound1 = listYardMoveReportsRequest {
                this.context = request.context
                this.filter = ListYardMoveReportsRequestKt.filter {
                    this.createdAfter = request.fuelTransaction.occurredAt
                    this.labels.putAll(request.fuelTransaction.attributesMap)
                    this.ownerId = request.fuelTransaction.subject.customerId
                }
            }
            val reply1 = yardsYardMoveReportingService.listYardMoveReports(WireCopies.reflective(outbound1))
            pushFuelTransactionResponse {
                this.fuelTransaction = fuelTransaction {
                    this.attributes.putAll(request.fuelTransaction.attributesMap)
                    this.fuelTransactionId = request.fuelTransactionId
                }
                this.outcome = PushFuelTransactionResponseKt.outcome {
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    /** Reconciles a fuel transaction. */
    override suspend fun reconcileFuelTransactions(request: ReconcileFuelTransactionsRequest): ReconcileFuelTransactionsResponse {
        return handleReconcileFuelTransactions(request)
    }

    /** Resolves a fuel transaction. */
    override suspend fun resolveFuelTransactionConflict(request: ResolveFuelTransactionConflictRequest): ResolveFuelTransactionConflictResponse = handleResolveFuelTransactionConflict(request)

    /** Exchanges changes of fuel transactions with a peer. */
    override fun streamFuelTransactionChanges(requests: Flow<StreamFuelTransactionChangesRequest>): Flow<StreamFuelTransactionChangesResponse> =
        requests.map { request -> handleStreamFuelTransactionChanges(request) }

    /** Syncs a fuel transaction. */
    override suspend fun syncFuelTransactions(request: SyncFuelTransactionsRequest): SyncFuelTransactionsResponse {
        return handleSyncFuelTransactions(request)
    }

    private suspend fun handleListFuelTransactionConflicts(request: ListFuelTransactionConflictsRequest): ListFuelTransactionConflictsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionSyncService/ListFuelTransactionConflicts") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListFuelTransactionConflicts", request)
            log.atInfo().addKeyValue("operation", "ListFuelTransactionConflicts").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = ListJobOpeningsRequest.newBuilder().apply {
                setContext(request.context)
                setFilter(ListJobOpeningsRequestKt.filter {
                    this.labels.putAll(request.filter.labelsMap)
                })
                setPage(pageRequest {
                    this.pageSize = request.page.pageSize
                    this.pageToken = request.page.pageToken
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> recruitingJobOpeningService.listJobOpenings(value) }
            listFuelTransactionConflictsResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handlePullFuelTransactions(request: PullFuelTransactionsRequest): PullFuelTransactionsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionSyncService/PullFuelTransactions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PullFuelTransactions", request)
            log.atInfo().addKeyValue("operation", "PullFuelTransactions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransaction.fuelTransactionId).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                submitYardRequest {
                    this.annotations.putAll(request.annotationsMap)
                    this.context = request.context
                    this.details = SubmitYardRequestKt.details {
                        this.approver = userRef {
                            this.displayName = request.fuelTransaction.subject.displayName
                            this.email = request.fuelTransaction.subject.email
                        }
                    }
                }
            }
            val reply0 = yardsYardApprovalService.submitYard(outbound0)
            pullFuelTransactionsResponse {
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                    this.kind = request.fuelTransaction.kind
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleReconcileFuelTransactions(request: ReconcileFuelTransactionsRequest): ReconcileFuelTransactionsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionSyncService/ReconcileFuelTransactions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ReconcileFuelTransactions", request)
            log.atInfo().addKeyValue("operation", "ReconcileFuelTransactions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransaction.fuelTransactionId).log("Recorded audit detail")
            val outbound0 = resolveDocumentsRequest {
                this.context = request.context
            }
            val reply0 = workerForReconcileFuelTransactions0.submit(outbound0)
            val outbound1 = approveYardRequest {
                this.context = request.context
                this.details = ApproveYardRequestKt.details {
                    this.approver = userRef {
                        this.email = request.fuelTransaction.subject.email
                    }
                }
                this.yard = yard {
                    this.owner = userRef {
                        this.displayName = request.fuelTransaction.subject.displayName
                    }
                }
            }
            val reply1 = clientForReconcileFuelTransactions1.approveYard(outbound1)
            val outbound2 = resolveCustomersRequest {
                this.context = request.context
            }
            val reply2 = flowOf(outbound2).map { value -> customersCustomerQueryService.resolveCustomers(value) }.single()
            reconcileFuelTransactionsResponse {
                this.fuelTransaction = fuelTransaction {
                    this.correlationId = reply0.result.correlationId
                    this.fuelTransactionId = request.fuelTransactionId
                    this.measurements.addAll(listOf(fuelTransactionMeasurement {
                        this.unit = reply1.yard.dimensions.unit
                    }))
                }
                this.outcome = ReconcileFuelTransactionsResponseKt.outcome {
                    this.nextSteps.addAll(reply1.outcome.nextStepsList)
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.correlationId = request.fuelTransaction.correlationId
                    this.details.putAll(reply2.result.detailsMap)
                    this.status = reply2.result.status
                }
            }
        }

    private suspend fun handleResolveFuelTransactionConflict(request: ResolveFuelTransactionConflictRequest): ResolveFuelTransactionConflictResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionSyncService/ResolveFuelTransactionConflict") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResolveFuelTransactionConflict", request)
            log.atInfo().addKeyValue("operation", "ResolveFuelTransactionConflict").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransaction.kind).log("Recorded audit detail")
            val outbound0 = delegateYardMoveApprovalRequest {
                this.context = request.context
                this.yardMove = yardMove {
                    this.assignee = userRef {
                        this.displayName = request.fuelTransaction.subject.displayName
                    }
                    this.requester = contactInfo {
                        this.email = request.fuelTransaction.subject.email
                    }
                }
            }
            val reply0 = withTimeout(5_000) { yardsYardMoveApprovalService.delegateYardMoveApproval(outbound0) }
            val configure1: CheckCustomerSegmentEligibilityRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.customerSegment = customerSegment {
                    this.owner = userRef {
                        this.email = request.fuelTransaction.subject.email
                    }
                }
            }
            val outbound1 = checkCustomerSegmentEligibilityRequest(configure1)
            val reply1 = customersCustomerSegmentValidationService.checkCustomerSegmentEligibility(outbound1)
            var reply2 = CancelFuelTransactionExportResponse.getDefaultInstance()
            request.fuelTransaction.measurementsList.asFlow().map { item ->
                val outbound2 = cancelFuelTransactionExportRequest {
                    this.context = request.context
                    this.details = CancelFuelTransactionExportRequestKt.details {
                        this.effectiveAt = item.observedAt
                        this.newOwner = userRef {
                            this.displayName = request.fuelTransaction.subject.displayName
                            this.email = request.fuelTransaction.subject.email
                        }
                        this.notifyContact = contactInfo {
                            this.email = reply1.customerSegment.owner.email
                        }
                    }
                    this.fuelTransactionId = request.fuelTransaction.fuelTransactionId
                }
                fuelFuelTransactionExportService.cancelFuelTransactionExport(outbound2)
            }.collect { reply2 = it }
            val outbound3 = searchFuelCardsRequest {
                this.context = request.context
                this.filter = SearchFuelCardsRequestKt.filter {
                    this.createdAfter = reply1.customerSegment.audit.updatedAt
                    this.labels.putAll(reply0.yardMove.customFieldsMap)
                    this.ownerId = reply1.customerSegment.owner.userId
                }
            }
            val reply3 = fuelFuelCardService.searchFuelCards(outbound3)
            resolveFuelTransactionConflictResponse {
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                    this.sequence = request.fuelTransaction.sequence
                    this.subject = customerRef {
                        this.displayName = reply1.customerSegment.owner.displayName
                    }
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private fun StreamFuelTransactionChangesRequest.toSuggestCustomersFromStreamFuelTransactionChanges1(reply0: MapFuelTransactionImportColumnsResponse): SuggestCustomersRequest {
        val source = this
        return suggestCustomersRequest {
            this.context = source.context
            this.filter = SuggestCustomersRequestKt.filter {
                this.createdAfter = reply0.outcome.effectiveAt
                this.ownerId = reply0.fuelTransaction.correlationId
            }
        }
    }

    private suspend fun handleStreamFuelTransactionChanges(request: StreamFuelTransactionChangesRequest): StreamFuelTransactionChangesResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionSyncService/StreamFuelTransactionChanges") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StreamFuelTransactionChanges", request)
            log.atInfo().addKeyValue("operation", "StreamFuelTransactionChanges").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.change.fuelTransactionId).log("Recorded audit detail")
            val outbound0 = mapFuelTransactionImportColumnsRequest {}.fuelCopy {
                this.context = request.context
                this.fuelTransaction = fuelTransaction {
                    this.subject = customerRef {
                        this.displayName = request.change.subject.displayName
                    }
                }
            }
            val reply0 = fuelFuelTransactionImportService.mapFuelTransactionImportColumns(outbound0)
            val outbound1 = request.toSuggestCustomersFromStreamFuelTransactionChanges1(reply0)
            val reply1 = customersCustomerQueryService.suggestCustomers(outbound1)
            streamFuelTransactionChangesResponse {
                this.fuelTransaction = fuelTransaction {
                    this.kind = request.change.kind
                }
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = reply0.outcome.status
                }
                this.sequence = request.change.sequence
            }
        }

    private suspend fun handleSyncFuelTransactions(request: SyncFuelTransactionsRequest): SyncFuelTransactionsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionSyncService/SyncFuelTransactions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SyncFuelTransactions", request)
            log.atInfo().addKeyValue("operation", "SyncFuelTransactions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransaction.fuelTransactionId).log("Recorded audit detail")
            var reply0 = ListFuelCardImportsResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<FuelTransactionMeasurement>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound0 = listFuelCardImportsRequest {
                            this.context = request.context
                            this.filter = ListFuelCardImportsRequestKt.filter {
                                this.createdAfter = item.observedAt
                            }
                        }
                        reply0 = fuelFuelCardImportService.listFuelCardImports(outbound0)
                    }
                }
                try {
                    request.fuelTransaction.measurementsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            var reply1 = CheckCustomerEligibilityResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.fuelTransaction.measurementsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound1 = checkCustomerEligibilityRequest {
                                this.context = request.context
                                this.customer = customer {
                                    this.audit = auditInfo {
                                        this.createdAt = item.observedAt
                                    }
                                    this.customerId = request.fuelTransaction.subject.customerId
                                    this.relationshipManager = userRef {
                                        this.displayName = request.fuelTransaction.subject.displayName
                                        this.email = request.fuelTransaction.subject.email
                                    }
                                }
                            }
                            customersCustomerValidationService.checkCustomerEligibility(outbound1)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply1 = it }
                }
            }
            syncFuelTransactionsResponse {
                this.fuelTransaction = fuelTransaction {
                    this.attributes.putAll(request.fuelTransaction.attributesMap)
                    this.fuelTransactionId = request.fuelTransactionId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }
}
