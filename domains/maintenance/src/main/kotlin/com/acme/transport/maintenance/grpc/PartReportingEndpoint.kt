package com.acme.transport.maintenance.grpc

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.drivers.v1.CompareDriverLicenseOptionsRequest
import com.acme.drivers.v1.CountDriversRequest
import com.acme.drivers.v1.CountDriversRequestKt
import com.acme.drivers.v1.DriverLicenseCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverQueryServiceGrpcKt
import com.acme.drivers.v1.DriverServiceGrpcKt
import com.acme.drivers.v1.RecalculateDriverLicensesRequest
import com.acme.drivers.v1.WatchDriversRequestKt
import com.acme.drivers.v1.compareDriverLicenseOptionsRequest
import com.acme.drivers.v1.countDriversRequest
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverLicenseLine
import com.acme.drivers.v1.driverLicenseLineTax
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.recalculateDriverLicensesRequest
import com.acme.drivers.v1.watchDriversRequest
import com.acme.featureflags.v1.CheckFeatureFlagEligibilityRequest
import com.acme.featureflags.v1.CheckFeatureFlagEligibilityRequestKt
import com.acme.featureflags.v1.FeatureFlagValidationServiceGrpcKt
import com.acme.featureflags.v1.checkFeatureFlagEligibilityRequest
import com.acme.featureflags.v1.featureFlag
import com.acme.fuel.v1.CancelFuelCardWorkflowRequest
import com.acme.fuel.v1.CancelFuelCardWorkflowResponse
import com.acme.fuel.v1.FuelCardExportServiceGrpcKt
import com.acme.fuel.v1.FuelCardWorkflowServiceGrpcKt
import com.acme.fuel.v1.StreamFuelCardExportRequestKt
import com.acme.fuel.v1.cancelFuelCardWorkflowRequest
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.streamFuelCardExportRequest
import com.acme.identity.v1.ResolveUsersRequest
import com.acme.identity.v1.ResolveUsersResponse
import com.acme.identity.v1.UserQueryServiceGrpcKt
import com.acme.identity.v1.resolveUsersRequest
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.DeletePartReportRequest
import com.acme.maintenance.v1.DeletePartReportResponse
import com.acme.maintenance.v1.FetchPartReportRequest
import com.acme.maintenance.v1.FetchPartReportResponse
import com.acme.maintenance.v1.GeneratePartReportRequest
import com.acme.maintenance.v1.GeneratePartReportResponse
import com.acme.maintenance.v1.ListPartReportsRequest
import com.acme.maintenance.v1.ListPartReportsResponse
import com.acme.maintenance.v1.PartReportingServiceGrpcKt
import com.acme.maintenance.v1.PreviewWorkOrderRequestKt
import com.acme.maintenance.v1.SchedulePartReportRequest
import com.acme.maintenance.v1.SchedulePartReportResponse
import com.acme.maintenance.v1.ServiceScheduleQueryServiceGrpcKt
import com.acme.maintenance.v1.SharePartReportRequest
import com.acme.maintenance.v1.SharePartReportResponse
import com.acme.maintenance.v1.SummarizeServiceSchedulesRequestKt
import com.acme.maintenance.v1.WorkOrderLifecycleServiceGrpcKt
import com.acme.maintenance.v1.WorkOrderValidationServiceGrpcKt
import com.acme.maintenance.v1.copy as maintenanceCopy
import com.acme.maintenance.v1.deletePartReportResponse
import com.acme.maintenance.v1.fetchPartReportResponse
import com.acme.maintenance.v1.generatePartReportResponse
import com.acme.maintenance.v1.listPartReportsResponse
import com.acme.maintenance.v1.part
import com.acme.maintenance.v1.previewWorkOrderRequest
import com.acme.maintenance.v1.schedulePartReportResponse
import com.acme.maintenance.v1.scheduleWorkOrderExpiryRequest
import com.acme.maintenance.v1.sharePartReportResponse
import com.acme.maintenance.v1.summarizeServiceSchedulesRequest
import com.acme.maintenance.v1.workOrder
import com.acme.metrics.v1.MetricSeriesValidationServiceGrpcKt
import com.acme.metrics.v1.metricSeries
import com.acme.metrics.v1.metricSeriesDimensions
import com.acme.metrics.v1.normalizeMetricSeriesRequest
import com.acme.search.v1.CreateSearchIndexRequest
import com.acme.search.v1.SearchIndexServiceGrpcKt
import com.acme.search.v1.createSearchIndexRequest
import com.acme.search.v1.searchIndex
import com.acme.transport.maintenance.MaintenanceRepository
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.RecallYardRequestKt
import com.acme.yards.v1.YardApprovalServiceGrpcKt
import com.acme.yards.v1.recallYardRequest
import com.acme.yards.v1.yard
import com.google.protobuf.Any as ProtoAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import org.slf4j.LoggerFactory

/** Builds and schedules reports about parts. */
class PartReportingEndpoint(
    private val store: MaintenanceRepository,
    private val workers: CoroutineScope,
    private val driversDriverLicenseCalculationService: DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub,
    private val driversDriverQueryService: DriverQueryServiceGrpcKt.DriverQueryServiceCoroutineStub,
    private val driversDriverService: DriverServiceGrpcKt.DriverServiceCoroutineStub,
    private val featureflagsFeatureFlagValidationService: FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub,
    private val fuelFuelCardExportService: FuelCardExportServiceGrpcKt.FuelCardExportServiceCoroutineStub,
    private val fuelFuelCardWorkflowService: FuelCardWorkflowServiceGrpcKt.FuelCardWorkflowServiceCoroutineStub,
    private val identityUserQueryService: UserQueryServiceGrpcKt.UserQueryServiceCoroutineStub,
    private val maintenanceServiceScheduleQueryService: ServiceScheduleQueryServiceGrpcKt.ServiceScheduleQueryServiceCoroutineStub,
    private val maintenanceWorkOrderLifecycleService: WorkOrderLifecycleServiceGrpcKt.WorkOrderLifecycleServiceCoroutineStub,
    private val maintenanceWorkOrderValidationService: WorkOrderValidationServiceGrpcKt.WorkOrderValidationServiceCoroutineStub,
    private val metricsMetricSeriesValidationService: MetricSeriesValidationServiceGrpcKt.MetricSeriesValidationServiceCoroutineStub,
    private val searchSearchIndexService: SearchIndexServiceGrpcKt.SearchIndexServiceCoroutineStub,
    private val yardsYardApprovalService: YardApprovalServiceGrpcKt.YardApprovalServiceCoroutineStub,
) : PartReportingServiceGrpcKt.PartReportingServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(PartReportingEndpoint::class.java)

    private val invokeSchedulePartReport1: suspend (ResolveUsersRequest) -> ResolveUsersResponse = { payload ->
        identityUserQueryService.resolveUsers(payload)
    }

    /** Deletes a part. */
    override suspend fun deletePartReport(request: DeletePartReportRequest): DeletePartReportResponse =
        RpcBoundary.run("acme.maintenance.v1.PartReportingService/DeletePartReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DeletePartReport", request)
            log.atInfo().addKeyValue("operation", "DeletePartReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.partId).log("Recorded audit detail")
            val configure0: RecallYardRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.details = RecallYardRequestKt.details {
                    this.approver = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                }
                this.reason = request.reason
                this.yard = yard {
                    this.audit = auditInfo {
                        this.etag = request.etag
                    }
                }
            }
            val outbound0 = recallYardRequest(configure0)
            val reply0 = yardsYardApprovalService.recallYard(outbound0)
            val outbound1 = normalizeMetricSeriesRequest {
                this.context = request.context
                this.metricSeries = metricSeries {
                    this.address = postalAddress {
                        this.region = reply0.yard.address.region
                    }
                    this.audit = auditInfo {
                        this.etag = reply0.yard.audit.etag
                    }
                    this.dimensions = metricSeriesDimensions {
                        this.weight = reply0.yard.dimensions.weight
                        this.width = reply0.yard.dimensions.width
                    }
                }
            }
            val reply1 = metricsMetricSeriesValidationService.normalizeMetricSeries(outbound1)
            val outbound2 = summarizeServiceSchedulesRequest {
                this.annotations.putAll(reply0.yard.specsMap)
                this.context = request.context
                this.details = SummarizeServiceSchedulesRequestKt.details {
                    this.filters.putAll(reply1.result.detailsMap)
                }
                this.reason = request.reason
            }
            val reply2 = maintenanceServiceScheduleQueryService.summarizeServiceSchedules(outbound2)
            val outbound3 = scheduleWorkOrderExpiryRequest {}.maintenanceCopy {
                this.context = request.context
                this.workOrder = workOrder {
                    this.assignee = userRef {
                        this.email = reply0.yard.owner.email
                        this.userId = reply1.metricSeries.owner.userId
                    }
                    this.audit = auditInfo {
                        this.createdBy = reply1.metricSeries.audit.createdBy
                    }
                    this.customer = driverRef {
                        this.displayName = reply0.yard.owner.displayName
                    }
                }
            }
            val reply3 = maintenanceWorkOrderLifecycleService.scheduleWorkOrderExpiry(outbound3)
            deletePartReportResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply1.result.detailsMap)
                    this.message = reply0.result.message
                    this.status = reply3.outcome.status
                }
            }
        }

    /** Returns a part. */
    override suspend fun fetchPartReport(request: FetchPartReportRequest): FetchPartReportResponse {
        return handleFetchPartReport(request)
    }

    /** Generates a part. */
    override suspend fun generatePartReport(request: GeneratePartReportRequest): GeneratePartReportResponse = handleGeneratePartReport(request)

    /** Lists parts a page at a time. */
    override suspend fun listPartReports(request: ListPartReportsRequest): ListPartReportsResponse =
        RpcBoundary.run("acme.maintenance.v1.PartReportingService/ListPartReports") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListPartReports", request)
            log.atInfo().addKeyValue("operation", "ListPartReports").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = ListPartReports0Command(
                context = request.context,
                featureFlagOwnerEmail = request.context.actor.email,
                featureFlagOwnerUserId = request.context.actor.userId,
                detailsLocale = request.context.locale
            ).toRequest()
            val reply0 = featureflagsFeatureFlagValidationService.checkFeatureFlagEligibility(outbound0)
            val outbound1 = compareDriverLicenseOptionsRequest {
                this.context = request.context
                this.driverLicense = driverLicense {
                    this.audit = auditInfo {
                        this.etag = reply0.featureFlag.audit.etag
                    }
                    this.lines.addAll(listOf(driverLicenseLine {
                        this.tax = driverLicenseLineTax {
                            this.amount = reply0.featureFlag.discount.amount
                        }
                    }))
                }
            }
            val command1: Submission<CompareDriverLicenseOptionsRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = driversDriverLicenseCalculationService.compareDriverLicenseOptions(payload1)
            listPartReportsResponse {
                this.parts.addAll(listOf(part {
                    this.audit = auditInfo {
                        this.createdAt = reply1.driverLicense.audit.createdAt
                        this.createdBy = reply0.featureFlag.audit.createdBy
                        this.updatedAt = reply0.featureFlag.audit.updatedAt
                    }
                }))
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Schedules a part. */
    override suspend fun schedulePartReport(request: SchedulePartReportRequest): SchedulePartReportResponse {
        return handleSchedulePartReport(request)
    }

    /** Shares a part. */
    override suspend fun sharePartReport(request: SharePartReportRequest): SharePartReportResponse = handleSharePartReport(request)

    private fun FetchPartReportRequest.toCreateSearchIndexFromFetchPartReport0(): CreateSearchIndexRequest {
        val source = this
        return createSearchIndexRequest {
            this.context = source.context
            this.searchIndex = searchIndex {
                this.audit = auditInfo {
                    this.updatedAt = source.context.deadline
                }
                this.owner = userRef {
                    this.email = source.context.actor.email
                    this.userId = source.context.actor.userId
                }
            }
        }
    }

    private suspend fun handleFetchPartReport(request: FetchPartReportRequest): FetchPartReportResponse =
        RpcBoundary.run("acme.maintenance.v1.PartReportingService/FetchPartReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "FetchPartReport", request)
            log.atInfo().addKeyValue("operation", "FetchPartReport").log("Handling transport request")
            val outbound0 = request.toCreateSearchIndexFromFetchPartReport0()
            val reply0 = searchSearchIndexService.createSearchIndex(outbound0)
            val outbound1 = watchDriversRequest {
                this.context = request.context
                this.filter = WatchDriversRequestKt.filter {
                    this.region = reply0.searchIndex.address.region
                }
            }
            driversDriverService.watchDrivers(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            fetchPartReportResponse {
                this.part = part {
                    this.audit = auditInfo {
                        this.createdAt = reply0.searchIndex.audit.createdAt
                        this.createdBy = reply0.searchIndex.audit.createdBy
                    }
                    this.partId = request.partId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleGeneratePartReport(request: GeneratePartReportRequest): GeneratePartReportResponse =
        RpcBoundary.run("acme.maintenance.v1.PartReportingService/GeneratePartReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GeneratePartReport", request)
            log.atInfo().addKeyValue("operation", "GeneratePartReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.format).log("Recorded audit detail")
            val outbound0 = countDriversRequest {
                this.context = request.context
                this.filter = CountDriversRequestKt.filter {
                    this.createdAfter = request.context.deadline
                    this.labels.putAll(request.annotationsMap)
                    this.ownerId = request.partId
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = driversDriverQueryService.countDrivers(envelope0.unpack(CountDriversRequest::class.java))
            val outbound1 = previewWorkOrderRequest {
                this.context = request.context
                this.details = PreviewWorkOrderRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
                this.reason = request.reason
            }
            val reply1 = maintenanceWorkOrderValidationService.previewWorkOrder(WireCopies.json(outbound1))
            generatePartReportResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = reply0.result.status
                }
            }
        }

    private data class ListPartReports0Command(
        val context: RequestContext,
        val featureFlagOwnerEmail: String,
        val featureFlagOwnerUserId: String,
        val detailsLocale: String
    ) {
        fun toRequest(): CheckFeatureFlagEligibilityRequest {
            val command = this
            return checkFeatureFlagEligibilityRequest {
                this.context = command.context
                this.details = CheckFeatureFlagEligibilityRequestKt.details {
                    this.locale = command.detailsLocale
                }
                this.featureFlag = featureFlag {
                    this.owner = userRef {
                        this.email = command.featureFlagOwnerEmail
                        this.userId = command.featureFlagOwnerUserId
                    }
                }
            }
        }
    }

    private suspend fun handleSchedulePartReport(request: SchedulePartReportRequest): SchedulePartReportResponse =
        RpcBoundary.run("acme.maintenance.v1.PartReportingService/SchedulePartReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SchedulePartReport", request)
            log.atInfo().addKeyValue("operation", "SchedulePartReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.partId).log("Recorded audit detail")
            val outbound0 = cancelFuelCardWorkflowRequest {
                this.context = request.context
                this.fuelCard = fuelCard {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
            }
            val adapter0 = OutboundPort<CancelFuelCardWorkflowRequest, CancelFuelCardWorkflowResponse> { payload ->
                fuelFuelCardWorkflowService.cancelFuelCardWorkflow(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = resolveUsersRequest {
                this.context = request.context
            }
            val reply1 = invokeSchedulePartReport1(outbound1)
            schedulePartReportResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = reply1.result.status
                }
            }
        }

    private suspend fun handleSharePartReport(request: SharePartReportRequest): SharePartReportResponse =
        RpcBoundary.run("acme.maintenance.v1.PartReportingService/SharePartReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SharePartReport", request)
            log.atInfo().addKeyValue("operation", "SharePartReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.recipient.phone).log("Recorded audit detail")
            val outbound0 = streamFuelCardExportRequest {
                this.context = request.context
                this.filter = StreamFuelCardExportRequestKt.filter {
                    this.labels.putAll(request.annotationsMap)
                    this.ownerId = request.details.templateId
                }
            }
            fuelFuelCardExportService.streamFuelCardExport(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = RecalculateDriverLicensesRequest.newBuilder().apply {
                putAllAnnotations(request.annotationsMap)
                setContext(request.context)
                setDriverLicense(driverLicense {
                    this.counterparty = driverRef {
                        this.email = request.details.recipient.email
                    }
                    this.lines.addAll(listOf(driverLicenseLine {
                        this.attributes.putAll(request.details.variablesMap)
                    }))
                })
                setReason(request.reason)
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> driversDriverLicenseCalculationService.recalculateDriverLicenses(value) }
            sharePartReportResponse {
                this.part = part {
                    this.audit = auditInfo {
                        this.updatedAt = reply1.driverLicense.audit.updatedAt
                        this.updatedBy = reply1.driverLicense.audit.updatedBy
                    }
                    this.owner = userRef {
                        this.email = request.details.recipient.email
                    }
                    this.partId = request.partId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }
}
