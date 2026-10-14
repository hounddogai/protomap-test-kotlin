package com.acme.transport.fuel.adapters

import com.acme.billing.v1.BillingAccountServiceGrpcKt
import com.acme.billing.v1.CreateBillingAccountRequest
import com.acme.billing.v1.CreateBillingAccountResponse
import com.acme.billing.v1.billingAccount
import com.acme.billing.v1.createBillingAccountRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.CustomerCalculationServiceGrpcKt
import com.acme.customers.v1.CustomerQueryServiceGrpcKt
import com.acme.customers.v1.SummarizeCustomersRequestKt
import com.acme.customers.v1.customer
import com.acme.customers.v1.explainCustomerCalculationRequest
import com.acme.customers.v1.summarizeCustomersRequest
import com.acme.deliveries.v1.DeliveryWorkflowServiceGrpcKt
import com.acme.deliveries.v1.ListDeliveryWorkflowsRequest
import com.acme.deliveries.v1.ListDeliveryWorkflowsRequestKt
import com.acme.deliveries.v1.listDeliveryWorkflowsRequest
import com.acme.drivers.v1.driverRef
import com.acme.fuel.v1.CountFuelPolicyTotalsRequest
import com.acme.fuel.v1.CountFuelPolicyTotalsResponse
import com.acme.fuel.v1.DeleteFuelPolicyReportRequest
import com.acme.fuel.v1.DeleteFuelPolicyReportResponse
import com.acme.fuel.v1.FetchFuelPolicyReportRequest
import com.acme.fuel.v1.FetchFuelPolicyReportResponse
import com.acme.fuel.v1.FuelCardImportServiceGrpcKt
import com.acme.fuel.v1.FuelCardLifecycleServiceGrpcKt
import com.acme.fuel.v1.FuelCardReportingServiceGrpcKt
import com.acme.fuel.v1.FuelCardSyncServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyExportServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyHistoryServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyReportingServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionReportingServiceGrpcKt
import com.acme.fuel.v1.GenerateFuelCardReportResponse
import com.acme.fuel.v1.GenerateFuelPolicyReportRequest
import com.acme.fuel.v1.GenerateFuelPolicyReportResponse
import com.acme.fuel.v1.ListFuelCardImportsRequestKt
import com.acme.fuel.v1.ListFuelPolicyReportsRequest
import com.acme.fuel.v1.ListFuelPolicyReportsResponse
import com.acme.fuel.v1.ListFuelPolicyVersionsRequest
import com.acme.fuel.v1.ListFuelPolicyVersionsRequestKt
import com.acme.fuel.v1.ListFuelTransactionReportsRequestKt
import com.acme.fuel.v1.ScheduleFuelCardExpiryRequest
import com.acme.fuel.v1.ScheduleFuelCardExpiryRequestKt
import com.acme.fuel.v1.ScheduleFuelPolicyExportRequest
import com.acme.fuel.v1.ScheduleFuelPolicyExportRequestKt
import com.acme.fuel.v1.ScheduleFuelPolicyReportRequest
import com.acme.fuel.v1.ScheduleFuelPolicyReportResponse
import com.acme.fuel.v1.ScheduleFuelTransactionReportRequestKt
import com.acme.fuel.v1.ScheduleFuelTransactionReportResponse
import com.acme.fuel.v1.ShareFuelPolicyReportRequest
import com.acme.fuel.v1.ShareFuelPolicyReportResponse
import com.acme.fuel.v1.StreamFuelPolicyMetricsRequest
import com.acme.fuel.v1.StreamFuelPolicyMetricsResponse
import com.acme.fuel.v1.StreamFuelTransactionMetricsRequestKt
import com.acme.fuel.v1.copy as fuelCopy
import com.acme.fuel.v1.countFuelPolicyTotalsResponse
import com.acme.fuel.v1.deleteFuelPolicyReportResponse
import com.acme.fuel.v1.fetchFuelPolicyReportResponse
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.generateFuelCardReportRequest
import com.acme.fuel.v1.generateFuelPolicyReportResponse
import com.acme.fuel.v1.listFuelCardImportsRequest
import com.acme.fuel.v1.listFuelPolicyReportsResponse
import com.acme.fuel.v1.listFuelPolicyVersionsRequest
import com.acme.fuel.v1.listFuelTransactionReportsRequest
import com.acme.fuel.v1.scheduleFuelCardExpiryRequest
import com.acme.fuel.v1.scheduleFuelPolicyExportRequest
import com.acme.fuel.v1.scheduleFuelPolicyReportResponse
import com.acme.fuel.v1.scheduleFuelTransactionReportRequest
import com.acme.fuel.v1.shareFuelPolicyReportResponse
import com.acme.fuel.v1.streamFuelCardChangesRequest
import com.acme.fuel.v1.streamFuelPolicyMetricsResponse
import com.acme.fuel.v1.streamFuelTransactionMetricsRequest
import com.acme.identity.v1.userRef
import com.acme.metrics.v1.MetricSeriesValidationServiceGrpcKt
import com.acme.metrics.v1.ValidateMetricSeriesRequest
import com.acme.metrics.v1.ValidateMetricSeriesResponse
import com.acme.metrics.v1.metricSeries
import com.acme.metrics.v1.validateMetricSeriesRequest
import com.acme.transport.fuel.FuelRepository
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.CommentYardApprovalRequest
import com.acme.yards.v1.CommentYardApprovalResponse
import com.acme.yards.v1.YardApprovalServiceGrpcKt
import com.acme.yards.v1.commentYardApprovalRequest
import com.acme.yards.v1.yard
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.slf4j.LoggerFactory

/** Builds and schedules reports about fuel policies. */
class FuelPolicyReportingController(
    private val store: FuelRepository,
    private val workers: CoroutineScope,
    private val billingBillingAccountService: BillingAccountServiceGrpcKt.BillingAccountServiceCoroutineStub,
    private val customersCustomerCalculationService: CustomerCalculationServiceGrpcKt.CustomerCalculationServiceCoroutineStub,
    private val customersCustomerQueryService: CustomerQueryServiceGrpcKt.CustomerQueryServiceCoroutineStub,
    private val deliveriesDeliveryWorkflowService: DeliveryWorkflowServiceGrpcKt.DeliveryWorkflowServiceCoroutineStub,
    private val fuelFuelCardImportService: FuelCardImportServiceGrpcKt.FuelCardImportServiceCoroutineStub,
    private val fuelFuelCardLifecycleService: FuelCardLifecycleServiceGrpcKt.FuelCardLifecycleServiceCoroutineStub,
    private val fuelFuelCardReportingService: FuelCardReportingServiceGrpcKt.FuelCardReportingServiceCoroutineStub,
    private val fuelFuelCardSyncService: FuelCardSyncServiceGrpcKt.FuelCardSyncServiceCoroutineStub,
    private val fuelFuelPolicyExportService: FuelPolicyExportServiceGrpcKt.FuelPolicyExportServiceCoroutineStub,
    private val fuelFuelPolicyHistoryService: FuelPolicyHistoryServiceGrpcKt.FuelPolicyHistoryServiceCoroutineStub,
    private val fuelFuelTransactionReportingService: FuelTransactionReportingServiceGrpcKt.FuelTransactionReportingServiceCoroutineStub,
    private val metricsMetricSeriesValidationService: MetricSeriesValidationServiceGrpcKt.MetricSeriesValidationServiceCoroutineStub,
    private val yardsYardApprovalService: YardApprovalServiceGrpcKt.YardApprovalServiceCoroutineStub,
) : FuelPolicyReportingServiceGrpcKt.FuelPolicyReportingServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(FuelPolicyReportingController::class.java)

    private val invokeShareFuelPolicyReport0: suspend (CreateBillingAccountRequest) -> CreateBillingAccountResponse = { payload ->
        billingBillingAccountService.createBillingAccount(payload)
    }

    /** Counts fuel policies by status. */
    override suspend fun countFuelPolicyTotals(request: CountFuelPolicyTotalsRequest): CountFuelPolicyTotalsResponse = handleCountFuelPolicyTotals(request)

    /** Deletes a fuel policy. */
    override suspend fun deleteFuelPolicyReport(request: DeleteFuelPolicyReportRequest): DeleteFuelPolicyReportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyReportingService/DeleteFuelPolicyReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DeleteFuelPolicyReport", request)
            log.atInfo().addKeyValue("operation", "DeleteFuelPolicyReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicyId).log("Recorded audit detail")
            val outbound0 = generateFuelCardReportRequest {
                this.annotations.putAll(request.context.baggageMap)
                this.context = request.context
                this.reason = request.reason
            }
            val reply0 = fuelFuelCardReportingService.generateFuelCardReport(outbound0)
            val outbound1 = scheduleFuelTransactionReportRequest {}.fuelCopy {
                this.context = request.context
                this.details = ScheduleFuelTransactionReportRequestKt.details {
                    this.period = reply0.period
                }
                this.reason = request.reason
            }
            val reply1 = fuelFuelTransactionReportingService.scheduleFuelTransactionReport(outbound1)
            val outbound2 = request.toListDeliveryWorkflowsFromDeleteFuelPolicyReport2(reply0, reply1)
            val reply2 = deliveriesDeliveryWorkflowService.listDeliveryWorkflows(outbound2)
            val outbound3 = summarizeCustomersRequest {
                this.context = request.context
                this.details = SummarizeCustomersRequestKt.details {
                    this.period = reply0.period
                }
            }
            val reply3 = customersCustomerQueryService.summarizeCustomers(outbound3)
            deleteFuelPolicyReportResponse {
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.message = reply0.result.message
                    this.status = reply1.result.status
                }
            }
        }

    /** Returns a fuel policy. */
    override suspend fun fetchFuelPolicyReport(request: FetchFuelPolicyReportRequest): FetchFuelPolicyReportResponse {
        return handleFetchFuelPolicyReport(request)
    }

    /** Generates a fuel policy. */
    override suspend fun generateFuelPolicyReport(request: GenerateFuelPolicyReportRequest): GenerateFuelPolicyReportResponse = handleGenerateFuelPolicyReport(request)

    /** Lists fuel policies a page at a time. */
    override suspend fun listFuelPolicyReports(request: ListFuelPolicyReportsRequest): ListFuelPolicyReportsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyReportingService/ListFuelPolicyReports") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListFuelPolicyReports", request)
            log.atInfo().addKeyValue("operation", "ListFuelPolicyReports").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            listFuelPolicyReportsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Schedules a fuel policy. */
    override suspend fun scheduleFuelPolicyReport(request: ScheduleFuelPolicyReportRequest): ScheduleFuelPolicyReportResponse {
        return handleScheduleFuelPolicyReport(request)
    }

    /** Shares a fuel policy. */
    override suspend fun shareFuelPolicyReport(request: ShareFuelPolicyReportRequest): ShareFuelPolicyReportResponse = handleShareFuelPolicyReport(request)

    /** Streams the changes of fuel policies as they happen. */
    override fun streamFuelPolicyMetrics(request: StreamFuelPolicyMetricsRequest): Flow<StreamFuelPolicyMetricsResponse> = flow {
        emit(handleStreamFuelPolicyMetrics(request))
    }

    private suspend fun handleCountFuelPolicyTotals(request: CountFuelPolicyTotalsRequest): CountFuelPolicyTotalsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyReportingService/CountFuelPolicyTotals") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CountFuelPolicyTotals", request)
            log.atInfo().addKeyValue("operation", "CountFuelPolicyTotals").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = explainCustomerCalculationRequest {
                this.context = request.context
                this.customer = customer {
                    this.primaryAddress = postalAddress {
                        this.region = request.filter.region
                    }
                }
            }
            val reply0 = customersCustomerCalculationService.explainCustomerCalculation(outbound0)
            countFuelPolicyTotalsResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private fun DeleteFuelPolicyReportRequest.toListDeliveryWorkflowsFromDeleteFuelPolicyReport2(reply0: GenerateFuelCardReportResponse, reply1: ScheduleFuelTransactionReportResponse): ListDeliveryWorkflowsRequest {
        val source = this
        return listDeliveryWorkflowsRequest {
            this.context = source.context
            this.filter = ListDeliveryWorkflowsRequestKt.filter {
                this.labels.putAll(reply0.result.detailsMap)
                this.ownerId = reply1.result.correlationId
            }
        }
    }

    private suspend fun handleFetchFuelPolicyReport(request: FetchFuelPolicyReportRequest): FetchFuelPolicyReportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyReportingService/FetchFuelPolicyReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "FetchFuelPolicyReport", request)
            log.atInfo().addKeyValue("operation", "FetchFuelPolicyReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            fetchFuelPolicyReportResponse {
                this.fuelPolicy = fuelPolicy {
                    this.fuelPolicyId = request.fuelPolicyId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private data class GenerateFuelPolicyReport2Command(
        val context: RequestContext,
        val filterLabels: Map<String, String>,
        val filterOwnerId: String
    ) {
        fun toRequest(): ListFuelPolicyVersionsRequest {
            val command = this
            return listFuelPolicyVersionsRequest {
                this.context = command.context
                this.filter = ListFuelPolicyVersionsRequestKt.filter {
                    this.labels.putAll(command.filterLabels)
                    this.ownerId = command.filterOwnerId
                }
            }
        }
    }

    private suspend fun handleGenerateFuelPolicyReport(request: GenerateFuelPolicyReportRequest): GenerateFuelPolicyReportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyReportingService/GenerateFuelPolicyReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GenerateFuelPolicyReport", request)
            log.atInfo().addKeyValue("operation", "GenerateFuelPolicyReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.format).log("Recorded audit detail")
            val outbound0 = streamFuelCardChangesRequest {
                this.change = fuelCard {
                    this.holder = driverRef {
                        this.email = request.context.actor.email
                    }
                }
                this.context = request.context
            }
            fuelFuelCardSyncService.streamFuelCardChanges(flowOf(outbound0)).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = listFuelTransactionReportsRequest {
                this.context = request.context
                this.filter = ListFuelTransactionReportsRequestKt.filter {
                    this.createdAfter = request.context.deadline
                    this.labels.putAll(request.details.filtersMap)
                    this.ownerId = request.fuelPolicyId
                }
            }
            val reply1 = fuelFuelTransactionReportingService.listFuelTransactionReports(WireCopies.json(outbound1))
            val outbound2 = GenerateFuelPolicyReport2Command(
                context = request.context,
                filterLabels = request.annotationsMap,
                filterOwnerId = request.fuelPolicyId
            ).toRequest()
            val reply2 = fuelFuelPolicyHistoryService.listFuelPolicyVersions(outbound2)
            generateFuelPolicyReportResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = reply1.result.status
                }
            }
        }

    private suspend fun handleScheduleFuelPolicyReport(request: ScheduleFuelPolicyReportRequest): ScheduleFuelPolicyReportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyReportingService/ScheduleFuelPolicyReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScheduleFuelPolicyReport", request)
            log.atInfo().addKeyValue("operation", "ScheduleFuelPolicyReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicyId).log("Recorded audit detail")
            val outbound0 = scheduleFuelPolicyExportRequest {
                this.context = request.context
                this.details = ScheduleFuelPolicyExportRequestKt.details {
                    this.filters.putAll(request.details.filtersMap)
                    this.recipientEmails.addAll(request.details.recipientEmailsList)
                }
            }
            val command0: Submission<ScheduleFuelPolicyExportRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = fuelFuelPolicyExportService.scheduleFuelPolicyExport(payload0)
            val outbound1 = commentYardApprovalRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.yard = yard {
                    this.owner = userRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val adapter1 = OutboundPort<CommentYardApprovalRequest, CommentYardApprovalResponse> { payload ->
                yardsYardApprovalService.commentYardApproval(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            scheduleFuelPolicyReportResponse {
                this.downloadUri = reply0.downloadUri
                this.period = request.details.period
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = reply1.result.status
                }
            }
        }

    private suspend fun handleShareFuelPolicyReport(request: ShareFuelPolicyReportRequest): ShareFuelPolicyReportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyReportingService/ShareFuelPolicyReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ShareFuelPolicyReport", request)
            log.atInfo().addKeyValue("operation", "ShareFuelPolicyReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.templateId).log("Recorded audit detail")
            val outbound0 = createBillingAccountRequest {
                this.billingAccount = billingAccount {
                    this.contact = contactInfo {
                        this.email = request.details.recipient.email
                        this.name = request.details.recipient.name
                        this.phone = request.details.recipient.phone
                        this.preferredChannel = request.details.recipient.preferredChannel
                    }
                }
                this.context = request.context
            }
            val reply0 = invokeShareFuelPolicyReport0(outbound0)
            val outbound1 = validateMetricSeriesRequest {
                this.context = request.context
                this.metricSeries = metricSeries {
                    this.audit = auditInfo {
                        this.createdAt = reply0.billingAccount.audit.createdAt
                    }
                    this.owner = userRef {
                        this.displayName = reply0.billingAccount.holder.displayName
                        this.email = request.details.recipient.email
                    }
                }
            }
            val invoke1: suspend (ValidateMetricSeriesRequest, Metadata) -> ValidateMetricSeriesResponse = metricsMetricSeriesValidationService::validateMetricSeries
            val reply1 = invoke1(outbound1, Metadata())
            shareFuelPolicyReportResponse {
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.updatedAt = reply1.metricSeries.audit.updatedAt
                        this.updatedBy = reply0.billingAccount.audit.updatedBy
                    }
                    this.fuelPolicyId = request.fuelPolicyId
                    this.owner = userRef {
                        this.email = request.details.recipient.email
                    }
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleStreamFuelPolicyMetrics(request: StreamFuelPolicyMetricsRequest): StreamFuelPolicyMetricsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyReportingService/StreamFuelPolicyMetrics") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StreamFuelPolicyMetrics", request)
            log.atInfo().addKeyValue("operation", "StreamFuelPolicyMetrics").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = ScheduleFuelCardExpiryRequest.newBuilder().apply {
                setContext(request.context)
                setDetails(ScheduleFuelCardExpiryRequestKt.details {
                    this.notify = contactInfo {
                        this.email = request.subscriber.email
                        this.name = request.subscriber.name
                        this.preferredChannel = request.subscriber.preferredChannel
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> fuelFuelCardLifecycleService.scheduleFuelCardExpiry(value) }
            val outbound1 = RequestFactory.create {
                listFuelCardImportsRequest {
                    this.context = request.context
                    this.filter = ListFuelCardImportsRequestKt.filter {
                        this.labels.putAll(reply0.fuelCard.labelsMap)
                    }
                }
            }
            val reply1 = fuelFuelCardImportService.listFuelCardImports(outbound1)
            val outbound2 = streamFuelTransactionMetricsRequest {
                this.context = request.context
                this.filter = StreamFuelTransactionMetricsRequestKt.filter {
                    this.labels.putAll(reply0.fuelCard.labelsMap)
                }
            }
            fuelFuelTransactionReportingService.streamFuelTransactionMetrics(outbound2).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            streamFuelPolicyMetricsResponse {
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.fuelCard.audit.updatedBy
                    }
                    this.owner = userRef {
                        this.email = request.subscriber.email
                    }
                }
                this.result = operationResult {
                    this.status = reply1.result.status
                }
            }
        }
}
