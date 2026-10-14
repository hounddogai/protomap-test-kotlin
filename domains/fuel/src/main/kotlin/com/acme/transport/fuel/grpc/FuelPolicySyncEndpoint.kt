package com.acme.transport.fuel.grpc

import com.acme.budgeting.v1.BudgetServiceGrpcKt
import com.acme.budgeting.v1.watchBudgetsRequest
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.pageRequest
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.CalculateCustomerSegmentRequestKt
import com.acme.customers.v1.CustomerSegmentCalculationServiceGrpcKt
import com.acme.customers.v1.EstimateCustomerSegmentResponse
import com.acme.customers.v1.ExplainCustomerSegmentCalculationResponse
import com.acme.customers.v1.HouseholdQueryServiceGrpcKt
import com.acme.customers.v1.RecalculateCustomerSegmentsRequest
import com.acme.customers.v1.RecalculateCustomerSegmentsResponse
import com.acme.customers.v1.SuggestHouseholdsRequestKt
import com.acme.customers.v1.calculateCustomerSegmentRequest
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.customerSegment
import com.acme.customers.v1.customerSegmentCondition
import com.acme.customers.v1.estimateCustomerSegmentRequest
import com.acme.customers.v1.explainCustomerSegmentCalculationRequest
import com.acme.customers.v1.recalculateCustomerSegmentsRequest
import com.acme.customers.v1.suggestHouseholdsRequest
import com.acme.datasets.v1.DatasetServiceGrpcKt
import com.acme.datasets.v1.watchDatasetsRequest
import com.acme.drivers.v1.DriverLicenseCalculationServiceGrpcKt
import com.acme.drivers.v1.QuoteDriverLicenseResponse
import com.acme.drivers.v1.SimulateDriverLicenseRequest
import com.acme.drivers.v1.copy as driversCopy
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverLicenseLine
import com.acme.drivers.v1.driverLicenseLineTax
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.quoteDriverLicenseRequest
import com.acme.drivers.v1.simulateDriverLicenseRequest
import com.acme.fuel.v1.FuelCardImportServiceGrpcKt
import com.acme.fuel.v1.FuelPolicy
import com.acme.fuel.v1.FuelPolicyCondition
import com.acme.fuel.v1.FuelPolicyExportServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyImportServiceGrpcKt
import com.acme.fuel.v1.FuelPolicySyncServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionCalculationServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionExportServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionSyncServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionValidationServiceGrpcKt
import com.acme.fuel.v1.GetFuelPolicySyncStateRequest
import com.acme.fuel.v1.GetFuelPolicySyncStateResponse
import com.acme.fuel.v1.ListFuelPolicyConflictsRequest
import com.acme.fuel.v1.ListFuelPolicyConflictsResponse
import com.acme.fuel.v1.ListFuelTransactionConflictsRequest
import com.acme.fuel.v1.ListFuelTransactionConflictsRequestKt
import com.acme.fuel.v1.MapFuelCardImportColumnsRequest
import com.acme.fuel.v1.NormalizeFuelTransactionRequestKt
import com.acme.fuel.v1.PullFuelPoliciesRequest
import com.acme.fuel.v1.PullFuelPoliciesResponse
import com.acme.fuel.v1.PullFuelPoliciesResponseKt
import com.acme.fuel.v1.PushFuelPolicyRequest
import com.acme.fuel.v1.PushFuelPolicyResponse
import com.acme.fuel.v1.PushFuelPolicyResponseKt
import com.acme.fuel.v1.ReconcileFuelPoliciesRequest
import com.acme.fuel.v1.ReconcileFuelPoliciesResponse
import com.acme.fuel.v1.ResolveFuelPolicyConflictRequest
import com.acme.fuel.v1.ResolveFuelPolicyConflictResponse
import com.acme.fuel.v1.ResolveFuelPolicyConflictResponseKt
import com.acme.fuel.v1.RetryFuelPolicyImportRequest
import com.acme.fuel.v1.RetryFuelPolicyImportResponse
import com.acme.fuel.v1.RetryFuelTransactionExportRequest
import com.acme.fuel.v1.StreamFuelPolicyChangesRequest
import com.acme.fuel.v1.StreamFuelPolicyChangesResponse
import com.acme.fuel.v1.SyncFuelPoliciesRequest
import com.acme.fuel.v1.SyncFuelPoliciesResponse
import com.acme.fuel.v1.estimateFuelTransactionRequest
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.fuelCardLimit
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.fuelTransactionMeasurement
import com.acme.fuel.v1.getFuelPolicyExportRequest
import com.acme.fuel.v1.getFuelPolicySyncStateResponse
import com.acme.fuel.v1.listFuelPolicyConflictsResponse
import com.acme.fuel.v1.listFuelTransactionConflictsRequest
import com.acme.fuel.v1.mapFuelCardImportColumnsRequest
import com.acme.fuel.v1.normalizeFuelTransactionRequest
import com.acme.fuel.v1.pullFuelPoliciesResponse
import com.acme.fuel.v1.pushFuelPolicyResponse
import com.acme.fuel.v1.reconcileFuelPoliciesResponse
import com.acme.fuel.v1.resolveFuelPolicyConflictResponse
import com.acme.fuel.v1.retryFuelPolicyImportRequest
import com.acme.fuel.v1.retryFuelTransactionExportRequest
import com.acme.fuel.v1.streamFuelPolicyChangesResponse
import com.acme.fuel.v1.syncFuelPoliciesResponse
import com.acme.identity.v1.userRef
import com.acme.metrics.v1.MetricSeriesServiceGrpcKt
import com.acme.metrics.v1.SearchMetricSeriesesRequestKt
import com.acme.metrics.v1.searchMetricSeriesesRequest
import com.acme.ticketing.v1.BrowseTicketsRequestKt
import com.acme.ticketing.v1.TicketQueryServiceGrpcKt
import com.acme.ticketing.v1.browseTicketsRequest
import com.acme.transport.fuel.FuelRepository
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.DockLifecycleServiceGrpcKt
import com.acme.yards.v1.RejectYardRequest
import com.acme.yards.v1.RejectYardResponse
import com.acme.yards.v1.StartYardMoveWorkflowRequestKt
import com.acme.yards.v1.YardApprovalServiceGrpcKt
import com.acme.yards.v1.YardMoveWorkflowServiceGrpcKt
import com.acme.yards.v1.cancelYardMoveWorkflowRequest
import com.acme.yards.v1.dock
import com.acme.yards.v1.rejectYardRequest
import com.acme.yards.v1.scheduleDockExpiryRequest
import com.acme.yards.v1.startYardMoveWorkflowRequest
import com.acme.yards.v1.yard
import com.acme.yards.v1.yardMove
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Keeps fuel policies in step with the systems of record in other regions. */
class FuelPolicySyncEndpoint(
    private val store: FuelRepository,
    private val workers: CoroutineScope,
    private val budgetingBudgetService: BudgetServiceGrpcKt.BudgetServiceCoroutineStub,
    private val customersCustomerSegmentCalculationService: CustomerSegmentCalculationServiceGrpcKt.CustomerSegmentCalculationServiceCoroutineStub,
    private val customersHouseholdQueryService: HouseholdQueryServiceGrpcKt.HouseholdQueryServiceCoroutineStub,
    private val datasetsDatasetService: DatasetServiceGrpcKt.DatasetServiceCoroutineStub,
    private val driversDriverLicenseCalculationService: DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub,
    private val fuelFuelCardImportService: FuelCardImportServiceGrpcKt.FuelCardImportServiceCoroutineStub,
    private val fuelFuelPolicyExportService: FuelPolicyExportServiceGrpcKt.FuelPolicyExportServiceCoroutineStub,
    private val fuelFuelPolicyImportService: FuelPolicyImportServiceGrpcKt.FuelPolicyImportServiceCoroutineStub,
    private val fuelFuelTransactionCalculationService: FuelTransactionCalculationServiceGrpcKt.FuelTransactionCalculationServiceCoroutineStub,
    private val fuelFuelTransactionExportService: FuelTransactionExportServiceGrpcKt.FuelTransactionExportServiceCoroutineStub,
    private val fuelFuelTransactionSyncService: FuelTransactionSyncServiceGrpcKt.FuelTransactionSyncServiceCoroutineStub,
    private val fuelFuelTransactionValidationService: FuelTransactionValidationServiceGrpcKt.FuelTransactionValidationServiceCoroutineStub,
    private val metricsMetricSeriesService: MetricSeriesServiceGrpcKt.MetricSeriesServiceCoroutineStub,
    private val ticketingTicketQueryService: TicketQueryServiceGrpcKt.TicketQueryServiceCoroutineStub,
    private val yardsDockLifecycleService: DockLifecycleServiceGrpcKt.DockLifecycleServiceCoroutineStub,
    private val yardsYardApprovalService: YardApprovalServiceGrpcKt.YardApprovalServiceCoroutineStub,
    private val yardsYardMoveWorkflowService: YardMoveWorkflowServiceGrpcKt.YardMoveWorkflowServiceCoroutineStub,
) : FuelPolicySyncServiceGrpcKt.FuelPolicySyncServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(FuelPolicySyncEndpoint::class.java)

    private val workerForGetFuelPolicySyncState1 = ReplyMailbox<RetryFuelPolicyImportRequest, RetryFuelPolicyImportResponse>(workers) { payload ->
        withTimeout(5_000) { fuelFuelPolicyImportService.retryFuelPolicyImport(payload) }
    }

    private val clientForListFuelPolicyConflicts0 by lazy { customersHouseholdQueryService }

    /** Returns a fuel policy. */
    override suspend fun getFuelPolicySyncState(request: GetFuelPolicySyncStateRequest): GetFuelPolicySyncStateResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicySyncService/GetFuelPolicySyncState") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetFuelPolicySyncState", request)
            log.atInfo().addKeyValue("operation", "GetFuelPolicySyncState").log("Handling transport request")
            val outbound0 = getFuelPolicyExportRequest {
                this.context = request.context
                this.fuelPolicyId = request.fuelPolicyId
            }
            val reply0 = fuelFuelPolicyExportService.getFuelPolicyExport(WireCopies.reflective(outbound0))
            val outbound1 = retryFuelPolicyImportRequest {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.createdBy = reply0.fuelPolicy.audit.createdBy
                    }
                }
            }
            val reply1 = workerForGetFuelPolicySyncState1.submit(outbound1)
            getFuelPolicySyncStateResponse {
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.createdAt = reply0.fuelPolicy.audit.createdAt
                    }
                    this.fuelPolicyId = request.fuelPolicyId
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.message = reply1.outcome.message
                    this.status = "ok"
                }
            }
        }

    /** Lists fuel policies a page at a time. */
    override suspend fun listFuelPolicyConflicts(request: ListFuelPolicyConflictsRequest): ListFuelPolicyConflictsResponse {
        return handleListFuelPolicyConflicts(request)
    }

    /** Pulls a fuel policy. */
    override suspend fun pullFuelPolicies(request: PullFuelPoliciesRequest): PullFuelPoliciesResponse = handlePullFuelPolicies(request)

    /** Pushs a fuel policy. */
    override suspend fun pushFuelPolicy(request: PushFuelPolicyRequest): PushFuelPolicyResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicySyncService/PushFuelPolicy") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PushFuelPolicy", request)
            log.atInfo().addKeyValue("operation", "PushFuelPolicy").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicy.fuelPolicyId).log("Recorded audit detail")
            val outbound0 = browseTicketsRequest {
                this.context = request.context
                this.filter = BrowseTicketsRequestKt.filter {
                    this.createdAfter = request.fuelPolicy.audit.createdAt
                    this.labels.putAll(request.annotationsMap)
                }
            }
            val reply0 = ticketingTicketQueryService.browseTickets(outbound0)
            val outbound1 = cancelYardMoveWorkflowRequest {
                this.context = request.context
                this.yardMove = yardMove {
                    this.audit = auditInfo {
                        this.createdAt = request.fuelPolicy.audit.createdAt
                    }
                }
            }
            val reply1 = yardsYardMoveWorkflowService.cancelYardMoveWorkflow(outbound1)
            val outbound2 = quoteDriverLicenseRequest {}.driversCopy {
                this.context = request.context
                this.driverLicense = driverLicense {
                    this.audit = auditInfo {
                        this.updatedAt = reply1.yardMove.audit.updatedAt
                    }
                    this.counterparty = driverRef {
                        this.displayName = reply1.yardMove.customer.displayName
                    }
                }
            }
            val reply2 = driversDriverLicenseCalculationService.quoteDriverLicense(outbound2)
            val outbound3 = request.toListFuelTransactionConflictsFromPushFuelPolicy3(reply2)
            val reply3 = fuelFuelTransactionSyncService.listFuelTransactionConflicts(outbound3)
            pushFuelPolicyResponse {
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.etag = reply2.driverLicense.audit.etag
                    }
                    this.fuelPolicyId = request.fuelPolicyId
                    this.status = request.fuelPolicy.status
                }
                this.outcome = PushFuelPolicyResponseKt.outcome {
                    this.message = reply2.result.message
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.details.putAll(reply3.result.detailsMap)
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    /** Reconciles a fuel policy. */
    override suspend fun reconcileFuelPolicies(request: ReconcileFuelPoliciesRequest): ReconcileFuelPoliciesResponse {
        return handleReconcileFuelPolicies(request)
    }

    /** Resolves a fuel policy. */
    override suspend fun resolveFuelPolicyConflict(request: ResolveFuelPolicyConflictRequest): ResolveFuelPolicyConflictResponse = handleResolveFuelPolicyConflict(request)

    /** Exchanges changes of fuel policies with a peer. */
    override fun streamFuelPolicyChanges(requests: Flow<StreamFuelPolicyChangesRequest>): Flow<StreamFuelPolicyChangesResponse> =
        requests.map { request -> handleStreamFuelPolicyChanges(request) }

    /** Syncs a fuel policy. */
    override suspend fun syncFuelPolicies(request: SyncFuelPoliciesRequest): SyncFuelPoliciesResponse {
        return handleSyncFuelPolicies(request)
    }

    private suspend fun handleListFuelPolicyConflicts(request: ListFuelPolicyConflictsRequest): ListFuelPolicyConflictsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicySyncService/ListFuelPolicyConflicts") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListFuelPolicyConflicts", request)
            log.atInfo().addKeyValue("operation", "ListFuelPolicyConflicts").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = suggestHouseholdsRequest {
                this.context = request.context
                this.filter = SuggestHouseholdsRequestKt.filter {
                    this.createdAfter = request.filter.createdAfter
                    this.region = request.filter.region
                }
                this.page = pageRequest {
                    this.pageSize = request.page.pageSize
                }
            }
            val reply0 = clientForListFuelPolicyConflicts0.suggestHouseholds(outbound0)
            val outbound1 = calculateCustomerSegmentRequest {
                this.context = request.context
                this.customerSegment = customerSegment {
                    this.owner = userRef {
                        this.email = request.context.actor.email
                    }
                }
                this.details = CalculateCustomerSegmentRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.filter.region
                    }
                }
            }
            val reply1 = flowOf(outbound1).map { value -> customersCustomerSegmentCalculationService.calculateCustomerSegment(value) }.single()
            listFuelPolicyConflictsResponse {
                this.fuelPolicies.addAll(listOf(fuelPolicy {
                    this.effective = reply1.customerSegment.effective
                }))
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handlePullFuelPolicies(request: PullFuelPoliciesRequest): PullFuelPoliciesResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicySyncService/PullFuelPolicies") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PullFuelPolicies", request)
            log.atInfo().addKeyValue("operation", "PullFuelPolicies").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicy.fuelPolicyId).log("Recorded audit detail")
            val outbound0 = scheduleDockExpiryRequest {
                this.context = request.context
                this.dock = dock {
                    this.name = request.fuelPolicy.name
                    this.owner = userRef {
                        this.displayName = request.fuelPolicy.owner.displayName
                        this.email = request.fuelPolicy.owner.email
                        this.userId = request.fuelPolicy.owner.userId
                    }
                }
            }
            val reply0 = withTimeout(5_000) { yardsDockLifecycleService.scheduleDockExpiry(outbound0) }
            val configure1: NormalizeFuelTransactionRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.fuelTransaction = fuelTransaction {
                    this.measurements.addAll(listOf(fuelTransactionMeasurement {
                        this.unit = reply0.dock.dimensions.unit
                    }))
                    this.subject = customerRef {
                        this.email = request.fuelPolicy.owner.email
                    }
                }
                this.reason = request.fuelPolicy.block.reason
            }
            val outbound1 = normalizeFuelTransactionRequest(configure1)
            val reply1 = fuelFuelTransactionValidationService.normalizeFuelTransaction(outbound1)
            pullFuelPoliciesResponse {
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.dock.audit.updatedAt
                    }
                    this.fuelPolicyId = request.fuelPolicyId
                    this.name = request.fuelPolicy.name
                }
                this.outcome = PullFuelPoliciesResponseKt.outcome {
                    this.effectiveAt = reply0.outcome.effectiveAt
                    this.message = reply1.outcome.message
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private fun PushFuelPolicyRequest.toListFuelTransactionConflictsFromPushFuelPolicy3(reply2: QuoteDriverLicenseResponse): ListFuelTransactionConflictsRequest {
        val source = this
        return listFuelTransactionConflictsRequest {
            this.context = source.context
            this.filter = ListFuelTransactionConflictsRequestKt.filter {
                this.region = reply2.driverLicense.billingAddress.region
            }
        }
    }

    private suspend fun handleReconcileFuelPolicies(request: ReconcileFuelPoliciesRequest): ReconcileFuelPoliciesResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicySyncService/ReconcileFuelPolicies") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ReconcileFuelPolicies", request)
            log.atInfo().addKeyValue("operation", "ReconcileFuelPolicies").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicy.fuelPolicyId).log("Recorded audit detail")
            val outbound0 = estimateFuelTransactionRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.fuelTransaction = fuelTransaction {
                    this.subject = customerRef {
                        this.displayName = request.fuelPolicy.owner.displayName
                        this.email = request.fuelPolicy.owner.email
                    }
                }
                this.reason = when (request.fuelPolicy.actionCase) {
                    FuelPolicy.ActionCase.BLOCK -> request.fuelPolicy.block.reason
                    else -> ""
                }
            }
            val reply0 = fuelFuelTransactionCalculationService.estimateFuelTransaction(outbound0)
            val outbound1 = mapFuelCardImportColumnsRequest {
                this.context = request.context
                this.fuelCard = fuelCard {
                    this.contact = contactInfo {
                        this.email = request.fuelPolicy.owner.email
                    }
                    this.holder = driverRef {
                        this.email = reply0.fuelTransaction.subject.email
                    }
                    this.limits.addAll(listOf(fuelCardLimit {
                        this.amount = request.fuelPolicy.discount.amount
                        this.name = request.fuelPolicy.name
                    }))
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = fuelFuelCardImportService.mapFuelCardImportColumns(envelope1.unpack(MapFuelCardImportColumnsRequest::class.java))
            val outbound2 = watchDatasetsRequest {
                this.context = request.context
                this.subscriber = contactInfo {
                    this.email = reply0.fuelTransaction.subject.email
                    this.name = reply1.fuelCard.contact.name
                    this.phone = reply1.fuelCard.contact.phone
                    this.preferredChannel = reply1.fuelCard.contact.preferredChannel
                }
            }
            datasetsDatasetService.watchDatasets(outbound2).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound3 = watchBudgetsRequest {
                this.context = request.context
                this.subscriber = contactInfo {
                    this.email = reply1.fuelCard.holder.email
                    this.name = reply1.fuelCard.contact.name
                    this.phone = reply1.fuelCard.contact.phone
                    this.preferredChannel = reply1.fuelCard.contact.preferredChannel
                }
            }
            budgetingBudgetService.watchBudgets(outbound3).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            reconcileFuelPoliciesResponse {
                this.fuelPolicy = fuelPolicy {
                    this.fuelPolicyId = request.fuelPolicyId
                    this.owner = userRef {
                        this.displayName = reply0.fuelTransaction.subject.displayName
                    }
                    this.priority = request.fuelPolicy.priority
                }
                this.result = operationResult {
                    this.correlationId = reply0.fuelTransaction.correlationId
                    this.status = reply1.result.status
                }
            }
        }

    private suspend fun handleResolveFuelPolicyConflict(request: ResolveFuelPolicyConflictRequest): ResolveFuelPolicyConflictResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicySyncService/ResolveFuelPolicyConflict") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResolveFuelPolicyConflict", request)
            log.atInfo().addKeyValue("operation", "ResolveFuelPolicyConflict").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicy.fuelPolicyId).log("Recorded audit detail")
            val outbound0 = retryFuelTransactionExportRequest {
                this.context = request.context
                this.fuelTransaction = fuelTransaction {
                    this.subject = customerRef {
                        this.displayName = request.fuelPolicy.owner.displayName
                    }
                }
            }
            val command0: Submission<RetryFuelTransactionExportRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = fuelFuelTransactionExportService.retryFuelTransactionExport(payload0)
            val outbound1 = rejectYardRequest {
                this.context = request.context
                this.yard = yard {
                    this.owner = userRef {
                        this.email = reply0.fuelTransaction.subject.email
                    }
                }
            }
            val adapter1 = OutboundPort<RejectYardRequest, RejectYardResponse> { payload ->
                yardsYardApprovalService.rejectYard(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            resolveFuelPolicyConflictResponse {
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.updatedBy = reply1.yard.audit.updatedBy
                    }
                    this.description = request.fuelPolicy.description
                    this.fuelPolicyId = request.fuelPolicyId
                    this.owner = userRef {
                        this.email = reply1.yard.owner.email
                    }
                }
                this.outcome = ResolveFuelPolicyConflictResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.message = reply0.outcome.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleStreamFuelPolicyChanges(request: StreamFuelPolicyChangesRequest): StreamFuelPolicyChangesResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicySyncService/StreamFuelPolicyChanges") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StreamFuelPolicyChanges", request)
            log.atInfo().addKeyValue("operation", "StreamFuelPolicyChanges").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.change.fuelPolicyId).log("Recorded audit detail")
            var reply0 = EstimateCustomerSegmentResponse.getDefaultInstance()
            request.change.conditionsList.asFlow().map { item ->
                val outbound0 = estimateCustomerSegmentRequest {
                    this.context = request.context
                    this.customerSegment = customerSegment {
                        this.conditions.addAll(listOf(customerSegmentCondition {
                            this.attribute = item.attribute
                            this.negate = item.negate
                            this.operator = item.operator
                            this.value = item.value
                        }))
                    }
                }
                customersCustomerSegmentCalculationService.estimateCustomerSegment(outbound0)
            }.collect { reply0 = it }
            val outbound1 = recalculateCustomerSegmentsRequest {
                this.context = request.context
                this.customerSegment = customerSegment {
                    this.owner = userRef {
                        this.displayName = reply0.customerSegment.owner.displayName
                        this.email = request.change.owner.email
                    }
                }
            }
            val invoke1: suspend (RecalculateCustomerSegmentsRequest, Metadata) -> RecalculateCustomerSegmentsResponse = customersCustomerSegmentCalculationService::recalculateCustomerSegments
            val reply1 = invoke1(outbound1, Metadata())
            val outbound2 = SimulateDriverLicenseRequest.newBuilder().apply {
                setContext(request.context)
                setDriverLicense(driverLicense {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.customerSegment.audit.updatedBy
                    }
                    this.counterparty = driverRef {
                        this.displayName = request.change.owner.displayName
                        this.email = reply0.customerSegment.owner.email
                    }
                    this.lines.addAll(listOf(driverLicenseLine {
                        this.tax = driverLicenseLineTax {
                            this.amount = reply1.customerSegment.discount.amount
                        }
                    }))
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply2 = outbound2.let { value -> driversDriverLicenseCalculationService.simulateDriverLicense(value) }
            val outbound3 = RequestFactory.create {
                searchMetricSeriesesRequest {
                    this.context = request.context
                    this.filter = SearchMetricSeriesesRequestKt.filter {
                        this.createdAfter = reply0.customerSegment.audit.createdAt
                        this.ownerId = reply0.customerSegment.owner.userId
                        this.region = reply2.driverLicense.billingAddress.region
                    }
                }
            }
            val reply3 = metricsMetricSeriesService.searchMetricSerieses(outbound3)
            streamFuelPolicyChangesResponse {
                this.fuelPolicy = fuelPolicy {
                    this.effective = request.change.effective
                    this.name = request.change.name
                    this.owner = userRef {
                        this.userId = reply1.customerSegment.owner.userId
                    }
                    this.priority = reply1.customerSegment.priority
                }
                this.result = operationResult {
                    this.message = reply2.result.message
                    this.referenceIds.addAll(reply2.result.referenceIdsList)
                    this.status = reply3.result.status
                }
            }
        }

    private suspend fun handleSyncFuelPolicies(request: SyncFuelPoliciesRequest): SyncFuelPoliciesResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicySyncService/SyncFuelPolicies") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SyncFuelPolicies", request)
            log.atInfo().addKeyValue("operation", "SyncFuelPolicies").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicy.fuelPolicyId).log("Recorded audit detail")
            var reply0 = ExplainCustomerSegmentCalculationResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<FuelPolicyCondition>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound0 = explainCustomerSegmentCalculationRequest {
                            this.context = request.context
                            this.customerSegment = customerSegment {
                                this.conditions.addAll(listOf(customerSegmentCondition {
                                    this.attribute = item.attribute
                                    this.operator = item.operator
                                    this.value = item.value
                                }))
                            }
                        }
                        reply0 = customersCustomerSegmentCalculationService.explainCustomerSegmentCalculation(outbound0)
                    }
                }
                try {
                    request.fuelPolicy.conditionsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound1 = startYardMoveWorkflowRequest {
                this.context = request.context
                this.details = StartYardMoveWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.email = request.fuelPolicy.owner.email
                    }
                }
            }
            val reply1 = yardsYardMoveWorkflowService.startYardMoveWorkflow(WireCopies.reflective(outbound1))
            syncFuelPoliciesResponse {
                this.fuelPolicy = fuelPolicy {
                    this.fuelPolicyId = request.fuelPolicyId
                    this.owner = userRef {
                        this.displayName = reply1.yardMove.assignee.displayName
                    }
                    this.status = request.fuelPolicy.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }
}
