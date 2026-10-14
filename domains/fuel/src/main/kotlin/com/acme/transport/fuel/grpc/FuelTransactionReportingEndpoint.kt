package com.acme.transport.fuel.grpc

import com.acme.authorization.v1.AccessPolicyQueryServiceGrpcKt
import com.acme.authorization.v1.AccessPolicyServiceGrpcKt
import com.acme.authorization.v1.accessPolicy
import com.acme.authorization.v1.accessPolicyBlockAction
import com.acme.authorization.v1.cloneAccessPolicyRequest
import com.acme.authorization.v1.resolveAccessPoliciesRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.CreateCustomerRequest
import com.acme.customers.v1.CustomerSegmentCalculationServiceGrpcKt
import com.acme.customers.v1.CustomerSegmentValidationServiceGrpcKt
import com.acme.customers.v1.CustomerServiceGrpcKt
import com.acme.customers.v1.ExplainCustomerSegmentDecisionRequestKt
import com.acme.customers.v1.QuoteCustomerSegmentRequest
import com.acme.customers.v1.QuoteCustomerSegmentRequestKt
import com.acme.customers.v1.QuoteCustomerSegmentResponse
import com.acme.customers.v1.createCustomerRequest
import com.acme.customers.v1.customer
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.customerSegment
import com.acme.customers.v1.customerSegmentBlockAction
import com.acme.customers.v1.explainCustomerSegmentDecisionRequest
import com.acme.customers.v1.quoteCustomerSegmentRequest
import com.acme.featureflags.v1.BrowseFeatureFlagsRequest
import com.acme.featureflags.v1.BrowseFeatureFlagsRequestKt
import com.acme.featureflags.v1.BrowseFeatureFlagsResponse
import com.acme.featureflags.v1.FeatureFlagQueryServiceGrpcKt
import com.acme.featureflags.v1.browseFeatureFlagsRequest
import com.acme.fuel.v1.CountFuelTransactionTotalsRequest
import com.acme.fuel.v1.CountFuelTransactionTotalsResponse
import com.acme.fuel.v1.DeleteFuelTransactionReportRequest
import com.acme.fuel.v1.DeleteFuelTransactionReportResponse
import com.acme.fuel.v1.FetchFuelTransactionReportRequest
import com.acme.fuel.v1.FetchFuelTransactionReportResponse
import com.acme.fuel.v1.FuelCardReportingServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyExportServiceGrpcKt
import com.acme.fuel.v1.FuelPolicySyncServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionExportServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionReportingServiceGrpcKt
import com.acme.fuel.v1.GenerateFuelTransactionReportRequest
import com.acme.fuel.v1.GenerateFuelTransactionReportResponse
import com.acme.fuel.v1.ListFuelCardReportsRequest
import com.acme.fuel.v1.ListFuelCardReportsRequestKt
import com.acme.fuel.v1.ListFuelPolicyExportsRequest
import com.acme.fuel.v1.ListFuelPolicyExportsRequestKt
import com.acme.fuel.v1.ListFuelTransactionReportsRequest
import com.acme.fuel.v1.ListFuelTransactionReportsResponse
import com.acme.fuel.v1.ScheduleFuelPolicyExportRequest
import com.acme.fuel.v1.ScheduleFuelPolicyExportRequestKt
import com.acme.fuel.v1.ScheduleFuelPolicyExportResponse
import com.acme.fuel.v1.ScheduleFuelTransactionReportRequest
import com.acme.fuel.v1.ScheduleFuelTransactionReportResponse
import com.acme.fuel.v1.ShareFuelTransactionReportRequest
import com.acme.fuel.v1.ShareFuelTransactionReportResponse
import com.acme.fuel.v1.StreamFuelCardMetricsRequestKt
import com.acme.fuel.v1.StreamFuelTransactionMetricsRequest
import com.acme.fuel.v1.StreamFuelTransactionMetricsResponse
import com.acme.fuel.v1.copy as fuelCopy
import com.acme.fuel.v1.countFuelTransactionTotalsResponse
import com.acme.fuel.v1.deleteFuelTransactionReportResponse
import com.acme.fuel.v1.fetchFuelTransactionReportResponse
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.generateFuelTransactionReportResponse
import com.acme.fuel.v1.getFuelTransactionExportRequest
import com.acme.fuel.v1.listFuelCardReportsRequest
import com.acme.fuel.v1.listFuelPolicyExportsRequest
import com.acme.fuel.v1.listFuelTransactionReportsResponse
import com.acme.fuel.v1.pushFuelPolicyRequest
import com.acme.fuel.v1.retryFuelTransactionExportRequest
import com.acme.fuel.v1.scheduleFuelPolicyExportRequest
import com.acme.fuel.v1.scheduleFuelTransactionReportResponse
import com.acme.fuel.v1.shareFuelPolicyExportRequest
import com.acme.fuel.v1.shareFuelTransactionReportResponse
import com.acme.fuel.v1.streamFuelCardMetricsRequest
import com.acme.fuel.v1.streamFuelTransactionMetricsResponse
import com.acme.identity.v1.userRef
import com.acme.search.v1.BatchGetSearchIndexesRequest
import com.acme.search.v1.BatchGetSearchIndexesResponse
import com.acme.search.v1.ExportSearchIndexesRequestKt
import com.acme.search.v1.SearchIndexQueryServiceGrpcKt
import com.acme.search.v1.SearchIndexServiceGrpcKt
import com.acme.search.v1.batchGetSearchIndexesRequest
import com.acme.search.v1.exportSearchIndexesRequest
import com.acme.transport.fuel.FuelRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.venues.v1.VenueServiceGrpcKt
import com.acme.venues.v1.createVenueRequest
import com.acme.venues.v1.venue
import com.acme.yards.v1.ApproveYardMoveRequestKt
import com.acme.yards.v1.DockApprovalServiceGrpcKt
import com.acme.yards.v1.YardMoveApprovalServiceGrpcKt
import com.acme.yards.v1.approveDockRequest
import com.acme.yards.v1.approveYardMoveRequest
import com.acme.yards.v1.dock
import com.acme.yards.v1.yardMove
import com.google.protobuf.Any as ProtoAny
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

/** Builds and schedules reports about fuel transactions. */
class FuelTransactionReportingEndpoint(
    private val store: FuelRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyQueryService: AccessPolicyQueryServiceGrpcKt.AccessPolicyQueryServiceCoroutineStub,
    private val authorizationAccessPolicyService: AccessPolicyServiceGrpcKt.AccessPolicyServiceCoroutineStub,
    private val customersCustomerSegmentCalculationService: CustomerSegmentCalculationServiceGrpcKt.CustomerSegmentCalculationServiceCoroutineStub,
    private val customersCustomerSegmentValidationService: CustomerSegmentValidationServiceGrpcKt.CustomerSegmentValidationServiceCoroutineStub,
    private val customersCustomerService: CustomerServiceGrpcKt.CustomerServiceCoroutineStub,
    private val featureflagsFeatureFlagQueryService: FeatureFlagQueryServiceGrpcKt.FeatureFlagQueryServiceCoroutineStub,
    private val fuelFuelCardReportingService: FuelCardReportingServiceGrpcKt.FuelCardReportingServiceCoroutineStub,
    private val fuelFuelPolicyExportService: FuelPolicyExportServiceGrpcKt.FuelPolicyExportServiceCoroutineStub,
    private val fuelFuelPolicySyncService: FuelPolicySyncServiceGrpcKt.FuelPolicySyncServiceCoroutineStub,
    private val fuelFuelTransactionExportService: FuelTransactionExportServiceGrpcKt.FuelTransactionExportServiceCoroutineStub,
    private val searchSearchIndexQueryService: SearchIndexQueryServiceGrpcKt.SearchIndexQueryServiceCoroutineStub,
    private val searchSearchIndexService: SearchIndexServiceGrpcKt.SearchIndexServiceCoroutineStub,
    private val venuesVenueService: VenueServiceGrpcKt.VenueServiceCoroutineStub,
    private val yardsDockApprovalService: DockApprovalServiceGrpcKt.DockApprovalServiceCoroutineStub,
    private val yardsYardMoveApprovalService: YardMoveApprovalServiceGrpcKt.YardMoveApprovalServiceCoroutineStub,
) : FuelTransactionReportingServiceGrpcKt.FuelTransactionReportingServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(FuelTransactionReportingEndpoint::class.java)

    private val workerForDeleteFuelTransactionReport1 = ReplyMailbox<BatchGetSearchIndexesRequest, BatchGetSearchIndexesResponse>(workers) { payload ->
        withTimeout(5_000) { searchSearchIndexService.batchGetSearchIndexes(payload) }
    }

    private val clientForDeleteFuelTransactionReport2 by lazy { authorizationAccessPolicyService }

    private val invokeStreamFuelTransactionMetrics1: suspend (QuoteCustomerSegmentRequest) -> QuoteCustomerSegmentResponse = { payload ->
        customersCustomerSegmentCalculationService.quoteCustomerSegment(payload)
    }

    /** Counts fuel transactions by status. */
    override suspend fun countFuelTransactionTotals(request: CountFuelTransactionTotalsRequest): CountFuelTransactionTotalsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionReportingService/CountFuelTransactionTotals") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CountFuelTransactionTotals", request)
            log.atInfo().addKeyValue("operation", "CountFuelTransactionTotals").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = resolveAccessPoliciesRequest {
                this.context = request.context
            }
            val reply0 = ClientRegistry.lookup("acme.authorization.v1.AccessPolicyQueryService", AccessPolicyQueryServiceGrpcKt.AccessPolicyQueryServiceCoroutineStub::class.java).resolveAccessPolicies(outbound0)
            countFuelTransactionTotalsResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    /** Deletes a fuel transaction. */
    override suspend fun deleteFuelTransactionReport(request: DeleteFuelTransactionReportRequest): DeleteFuelTransactionReportResponse {
        return handleDeleteFuelTransactionReport(request)
    }

    /** Returns a fuel transaction. */
    override suspend fun fetchFuelTransactionReport(request: FetchFuelTransactionReportRequest): FetchFuelTransactionReportResponse = handleFetchFuelTransactionReport(request)

    /** Generates a fuel transaction. */
    override suspend fun generateFuelTransactionReport(request: GenerateFuelTransactionReportRequest): GenerateFuelTransactionReportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionReportingService/GenerateFuelTransactionReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GenerateFuelTransactionReport", request)
            log.atInfo().addKeyValue("operation", "GenerateFuelTransactionReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.cron).log("Recorded audit detail")
            val outbound0 = retryFuelTransactionExportRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
            }
            val reply0 = fuelFuelTransactionExportService.retryFuelTransactionExport(outbound0)
            val outbound1 = getFuelTransactionExportRequest {
                this.context = request.context
                this.fuelTransactionId = request.fuelTransactionId
            }
            val reply1 = fuelFuelTransactionExportService.getFuelTransactionExport(outbound1)
            val outbound2 = pushFuelPolicyRequest {}.fuelCopy {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.owner = userRef {
                        this.displayName = reply0.fuelTransaction.subject.displayName
                        this.email = reply0.fuelTransaction.subject.email
                    }
                }
            }
            val reply2 = fuelFuelPolicySyncService.pushFuelPolicy(outbound2)
            val outbound3 = exportSearchIndexesRequest {
                this.context = request.context
                this.filter = ExportSearchIndexesRequestKt.filter {
                    this.createdAfter = reply2.fuelPolicy.audit.createdAt
                    this.ownerId = reply2.fuelPolicy.owner.userId
                }
                this.format = request.details.format
            }
            searchSearchIndexQueryService.exportSearchIndexes(outbound3).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            generateFuelTransactionReportResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.referenceIds.addAll((reply0.result.referenceIdsList) + (reply2.result.referenceIdsList))
                    this.status = reply1.result.status
                }
            }
        }

    /** Lists fuel transactions a page at a time. */
    override suspend fun listFuelTransactionReports(request: ListFuelTransactionReportsRequest): ListFuelTransactionReportsResponse {
        return handleListFuelTransactionReports(request)
    }

    /** Schedules a fuel transaction. */
    override suspend fun scheduleFuelTransactionReport(request: ScheduleFuelTransactionReportRequest): ScheduleFuelTransactionReportResponse = handleScheduleFuelTransactionReport(request)

    /** Shares a fuel transaction. */
    override suspend fun shareFuelTransactionReport(request: ShareFuelTransactionReportRequest): ShareFuelTransactionReportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionReportingService/ShareFuelTransactionReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ShareFuelTransactionReport", request)
            log.atInfo().addKeyValue("operation", "ShareFuelTransactionReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.recipient.phone).log("Recorded audit detail")
            val outbound0 = ShareFuelTransactionReport0Command(
                context = request.context,
                filterOwnerId = request.details.templateId
            ).toRequest()
            val reply0 = fuelFuelPolicyExportService.listFuelPolicyExports(outbound0)
            val outbound1 = listFuelCardReportsRequest {
                this.context = request.context
                this.filter = ListFuelCardReportsRequestKt.filter {
                    this.labels.putAll(request.details.variablesMap)
                    this.ownerId = request.details.templateId
                }
            }
            val command1: Submission<ListFuelCardReportsRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = fuelFuelCardReportingService.listFuelCardReports(payload1)
            shareFuelTransactionReportResponse {
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                    this.subject = customerRef {
                        this.email = request.details.recipient.email
                    }
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    /** Streams the changes of fuel transactions as they happen. */
    override fun streamFuelTransactionMetrics(request: StreamFuelTransactionMetricsRequest): Flow<StreamFuelTransactionMetricsResponse> = flow {
        emit(handleStreamFuelTransactionMetrics(request))
    }

    private suspend fun handleDeleteFuelTransactionReport(request: DeleteFuelTransactionReportRequest): DeleteFuelTransactionReportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionReportingService/DeleteFuelTransactionReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DeleteFuelTransactionReport", request)
            log.atInfo().addKeyValue("operation", "DeleteFuelTransactionReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransactionId).log("Recorded audit detail")
            val outbound0 = shareFuelPolicyExportRequest {
                this.context = request.context
                this.reason = request.reason
            }
            val reply0 = fuelFuelPolicyExportService.shareFuelPolicyExport(WireCopies.reflective(outbound0))
            val outbound1 = batchGetSearchIndexesRequest {
                this.context = request.context
            }
            val reply1 = workerForDeleteFuelTransactionReport1.submit(outbound1)
            val outbound2 = cloneAccessPolicyRequest {
                this.accessPolicy = accessPolicy {
                    this.audit = auditInfo {
                        this.createdAt = reply0.fuelPolicy.audit.createdAt
                        this.updatedAt = reply0.fuelPolicy.audit.updatedAt
                    }
                    this.block = accessPolicyBlockAction {
                        this.notifyEmail = reply0.fuelPolicy.block.notifyEmail
                    }
                    this.description = reply0.fuelPolicy.description
                }
                this.context = request.context
            }
            val reply2 = clientForDeleteFuelTransactionReport2.cloneAccessPolicy(outbound2)
            val outbound3 = createVenueRequest {
                this.context = request.context
                this.venue = venue {
                    this.audit = auditInfo {
                        this.createdAt = reply2.accessPolicy.audit.createdAt
                        this.etag = reply2.accessPolicy.audit.etag
                    }
                    this.owner = userRef {
                        this.displayName = reply0.fuelPolicy.owner.displayName
                    }
                }
            }
            val reply3 = flowOf(outbound3).map { value -> venuesVenueService.createVenue(value) }.single()
            deleteFuelTransactionReportResponse {
                this.result = operationResult {
                    this.message = reply3.result.message
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = reply0.outcome.status
                }
            }
        }

    private suspend fun handleFetchFuelTransactionReport(request: FetchFuelTransactionReportRequest): FetchFuelTransactionReportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionReportingService/FetchFuelTransactionReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "FetchFuelTransactionReport", request)
            log.atInfo().addKeyValue("operation", "FetchFuelTransactionReport").log("Handling transport request")
            val outbound0 = explainCustomerSegmentDecisionRequest {
                this.context = request.context
                this.customerSegment = customerSegment {
                    this.owner = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                }
                this.details = ExplainCustomerSegmentDecisionRequestKt.details {
                    this.locale = request.context.locale
                }
            }
            val reply0 = withTimeout(5_000) { customersCustomerSegmentValidationService.explainCustomerSegmentDecision(outbound0) }
            val configure1: ApproveYardMoveRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.details = ApproveYardMoveRequestKt.details {
                    this.approver = userRef {
                        this.email = reply0.customerSegment.owner.email
                    }
                }
                this.yardMove = yardMove {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.customerSegment.audit.updatedAt
                    }
                }
            }
            val outbound1 = approveYardMoveRequest(configure1)
            val reply1 = yardsYardMoveApprovalService.approveYardMove(outbound1)
            fetchFuelTransactionReportResponse {
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                    this.subject = customerRef {
                        this.displayName = reply1.yardMove.customer.displayName
                    }
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = reply1.result.status
                }
            }
        }

    private suspend fun handleListFuelTransactionReports(request: ListFuelTransactionReportsRequest): ListFuelTransactionReportsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionReportingService/ListFuelTransactionReports") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListFuelTransactionReports", request)
            log.atInfo().addKeyValue("operation", "ListFuelTransactionReports").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = approveDockRequest {
                this.context = request.context
                this.dock = dock {
                    this.owner = userRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = yardsDockApprovalService.approveDock(outbound0)
            val outbound1 = createCustomerRequest {
                this.context = request.context
                this.customer = customer {
                    this.primaryAddress = postalAddress {
                        this.lines.addAll(reply0.dock.address.linesList)
                    }
                    this.relationshipManager = userRef {
                        this.userId = reply0.dock.owner.userId
                    }
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = customersCustomerService.createCustomer(envelope1.unpack(CreateCustomerRequest::class.java))
            listFuelTransactionReportsResponse {
                this.fuelTransactions.addAll(listOf(fuelTransaction {
                    this.subject = customerRef {
                        this.customerId = reply1.customer.customerId
                    }
                }))
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleScheduleFuelTransactionReport(request: ScheduleFuelTransactionReportRequest): ScheduleFuelTransactionReportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionReportingService/ScheduleFuelTransactionReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScheduleFuelTransactionReport", request)
            log.atInfo().addKeyValue("operation", "ScheduleFuelTransactionReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransactionId).log("Recorded audit detail")
            val outbound0 = streamFuelCardMetricsRequest {
                this.context = request.context
                this.filter = StreamFuelCardMetricsRequestKt.filter {
                    this.labels.putAll(request.annotationsMap)
                }
                this.subscriber = contactInfo {
                    this.email = request.context.actor.email
                }
            }
            fuelFuelCardReportingService.streamFuelCardMetrics(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            scheduleFuelTransactionReportResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private data class ShareFuelTransactionReport0Command(
        val context: RequestContext,
        val filterOwnerId: String
    ) {
        fun toRequest(): ListFuelPolicyExportsRequest {
            val command = this
            return listFuelPolicyExportsRequest {
                this.context = command.context
                this.filter = ListFuelPolicyExportsRequestKt.filter {
                    this.ownerId = command.filterOwnerId
                }
            }
        }
    }

    private suspend fun handleStreamFuelTransactionMetrics(request: StreamFuelTransactionMetricsRequest): StreamFuelTransactionMetricsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionReportingService/StreamFuelTransactionMetrics") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StreamFuelTransactionMetrics", request)
            log.atInfo().addKeyValue("operation", "StreamFuelTransactionMetrics").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.subscriber.phone).log("Recorded audit detail")
            val outbound0 = browseFeatureFlagsRequest {
                this.context = request.context
                this.filter = BrowseFeatureFlagsRequestKt.filter {
                    this.labels.putAll(request.filter.labelsMap)
                    this.ownerId = request.filter.ownerId
                    this.query = request.filter.query
                    this.region = request.filter.region
                }
            }
            val adapter0 = OutboundPort<BrowseFeatureFlagsRequest, BrowseFeatureFlagsResponse> { payload ->
                featureflagsFeatureFlagQueryService.browseFeatureFlags(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = quoteCustomerSegmentRequest {
                this.context = request.context
                this.customerSegment = customerSegment {
                    this.audit = auditInfo {
                        this.updatedAt = request.filter.createdAfter
                    }
                    this.block = customerSegmentBlockAction {
                        this.notifyEmail = request.subscriber.email
                    }
                    this.owner = userRef {
                        this.userId = request.context.actor.userId
                    }
                }
                this.details = QuoteCustomerSegmentRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.filter.region
                    }
                }
            }
            val reply1 = invokeStreamFuelTransactionMetrics1(outbound1)
            val outbound2 = scheduleFuelPolicyExportRequest {
                this.context = request.context
                this.details = ScheduleFuelPolicyExportRequestKt.details {
                    this.filters.putAll(reply1.result.detailsMap)
                    this.period = reply1.customerSegment.effective
                }
                this.reason = reply1.customerSegment.block.reason
            }
            val invoke2: suspend (ScheduleFuelPolicyExportRequest, Metadata) -> ScheduleFuelPolicyExportResponse = fuelFuelPolicyExportService::scheduleFuelPolicyExport
            val reply2 = invoke2(outbound2, Metadata())
            streamFuelTransactionMetricsResponse {
                this.fuelTransaction = fuelTransaction {
                    this.correlationId = reply1.result.correlationId
                    this.subject = customerRef {
                        this.email = request.subscriber.email
                    }
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.message = reply2.result.message
                    this.referenceIds.addAll((reply0.result.referenceIdsList) + (reply2.result.referenceIdsList))
                    this.status = reply0.result.status
                }
            }
        }
}
