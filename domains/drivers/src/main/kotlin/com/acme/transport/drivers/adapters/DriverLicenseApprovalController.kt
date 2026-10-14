package com.acme.transport.drivers.adapters

import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.dispatch.v1.DispatchAssignmentApprovalServiceGrpcKt
import com.acme.dispatch.v1.ListPendingDispatchAssignmentsRequest
import com.acme.dispatch.v1.ListPendingDispatchAssignmentsRequestKt
import com.acme.dispatch.v1.ListPendingDispatchAssignmentsResponse
import com.acme.dispatch.v1.listPendingDispatchAssignmentsRequest
import com.acme.drivers.v1.ApproveDriverLicenseRequest
import com.acme.drivers.v1.ApproveDriverLicenseResponse
import com.acme.drivers.v1.ApproveDriverLicenseResponseKt
import com.acme.drivers.v1.CalculateDutyStatusRequestKt
import com.acme.drivers.v1.CommentDriverLicenseApprovalRequest
import com.acme.drivers.v1.CommentDriverLicenseApprovalResponse
import com.acme.drivers.v1.DelegateDriverLicenseApprovalRequest
import com.acme.drivers.v1.DelegateDriverLicenseApprovalResponse
import com.acme.drivers.v1.DelegateDriverLicenseApprovalResponseKt
import com.acme.drivers.v1.DriverCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseApprovalServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseLine
import com.acme.drivers.v1.DriverLicenseReportingServiceGrpcKt
import com.acme.drivers.v1.DriverLifecycleServiceGrpcKt
import com.acme.drivers.v1.DutyStatusCalculationServiceGrpcKt
import com.acme.drivers.v1.EscalateDriverLicenseRequest
import com.acme.drivers.v1.EscalateDriverLicenseResponse
import com.acme.drivers.v1.ExplainDutyStatusCalculationRequest
import com.acme.drivers.v1.ExplainDutyStatusCalculationRequestKt
import com.acme.drivers.v1.ExplainDutyStatusCalculationResponse
import com.acme.drivers.v1.GetDriverLicenseApprovalStatusRequest
import com.acme.drivers.v1.GetDriverLicenseApprovalStatusResponse
import com.acme.drivers.v1.ListDriverLicenseReportsRequestKt
import com.acme.drivers.v1.ListPendingDriverLicensesRequest
import com.acme.drivers.v1.ListPendingDriverLicensesResponse
import com.acme.drivers.v1.QuoteDriverRequest
import com.acme.drivers.v1.RecallDriverLicenseRequest
import com.acme.drivers.v1.RecallDriverLicenseResponse
import com.acme.drivers.v1.RecallDriverLicenseResponseKt
import com.acme.drivers.v1.RejectDriverLicenseRequest
import com.acme.drivers.v1.RejectDriverLicenseResponse
import com.acme.drivers.v1.RejectDriverLicenseResponseKt
import com.acme.drivers.v1.StreamDriverLicenseMetricsRequestKt
import com.acme.drivers.v1.SubmitDriverLicenseRequest
import com.acme.drivers.v1.SubmitDriverLicenseResponse
import com.acme.drivers.v1.SuspendDriverRequestKt
import com.acme.drivers.v1.approveDriverLicenseResponse
import com.acme.drivers.v1.calculateDutyStatusRequest
import com.acme.drivers.v1.commentDriverLicenseApprovalResponse
import com.acme.drivers.v1.copy as driversCopy
import com.acme.drivers.v1.delegateDriverLicenseApprovalResponse
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverLicenseRef
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.dutyStatus
import com.acme.drivers.v1.escalateDriverLicenseResponse
import com.acme.drivers.v1.explainDutyStatusCalculationRequest
import com.acme.drivers.v1.getDriverLicenseApprovalStatusResponse
import com.acme.drivers.v1.listDriverLicenseReportsRequest
import com.acme.drivers.v1.listPendingDriverLicensesResponse
import com.acme.drivers.v1.quoteDriverRequest
import com.acme.drivers.v1.recallDriverLicenseResponse
import com.acme.drivers.v1.rejectDriverLicenseResponse
import com.acme.drivers.v1.streamDriverLicenseMetricsRequest
import com.acme.drivers.v1.submitDriverLicenseResponse
import com.acme.drivers.v1.suspendDriverRequest
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.WorkOrderApprovalServiceGrpcKt
import com.acme.maintenance.v1.escalateWorkOrderRequest
import com.acme.maintenance.v1.workOrder
import com.acme.metrics.v1.BrowseMetricSeriesesRequest
import com.acme.metrics.v1.BrowseMetricSeriesesRequestKt
import com.acme.metrics.v1.BrowseMetricSeriesesResponse
import com.acme.metrics.v1.MetricSeriesQueryServiceGrpcKt
import com.acme.metrics.v1.MetricSeriesServiceGrpcKt
import com.acme.metrics.v1.MetricSeriesValidationServiceGrpcKt
import com.acme.metrics.v1.browseMetricSeriesesRequest
import com.acme.metrics.v1.cloneMetricSeriesRequest
import com.acme.metrics.v1.explainMetricSeriesDecisionRequest
import com.acme.metrics.v1.metricSeries
import com.acme.notifications.v1.BatchGetNotificationsRequest
import com.acme.notifications.v1.NotificationQueryServiceGrpcKt
import com.acme.notifications.v1.NotificationServiceGrpcKt
import com.acme.notifications.v1.SearchNotificationsRequest
import com.acme.notifications.v1.SearchNotificationsRequestKt
import com.acme.notifications.v1.batchGetNotificationsRequest
import com.acme.notifications.v1.searchNotificationsRequest
import com.acme.notifications.v1.summarizeNotificationsRequest
import com.acme.search.v1.ExportSearchIndexesRequestKt
import com.acme.search.v1.SearchIndexQueryServiceGrpcKt
import com.acme.search.v1.exportSearchIndexesRequest
import com.acme.telematics.v1.GeoFenceSyncServiceGrpcKt
import com.acme.telematics.v1.ResolveGeoFenceConflictResponse
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.geoFenceDiscountAction
import com.acme.telematics.v1.pullGeoFencesRequest
import com.acme.telematics.v1.resolveGeoFenceConflictRequest
import com.acme.transport.drivers.DriversRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.venues.v1.VenueValidationServiceGrpcKt
import com.acme.venues.v1.scoreVenueRequest
import com.acme.venues.v1.venue
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Routes driver licenses through approval, with escalation and delegation. */
class DriverLicenseApprovalController(
    private val store: DriversRepository,
    private val workers: CoroutineScope,
    private val dispatchDispatchAssignmentApprovalService: DispatchAssignmentApprovalServiceGrpcKt.DispatchAssignmentApprovalServiceCoroutineStub,
    private val driversDriverCalculationService: DriverCalculationServiceGrpcKt.DriverCalculationServiceCoroutineStub,
    private val driversDriverLicenseReportingService: DriverLicenseReportingServiceGrpcKt.DriverLicenseReportingServiceCoroutineStub,
    private val driversDriverLifecycleService: DriverLifecycleServiceGrpcKt.DriverLifecycleServiceCoroutineStub,
    private val driversDutyStatusCalculationService: DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineStub,
    private val maintenanceWorkOrderApprovalService: WorkOrderApprovalServiceGrpcKt.WorkOrderApprovalServiceCoroutineStub,
    private val metricsMetricSeriesQueryService: MetricSeriesQueryServiceGrpcKt.MetricSeriesQueryServiceCoroutineStub,
    private val metricsMetricSeriesService: MetricSeriesServiceGrpcKt.MetricSeriesServiceCoroutineStub,
    private val metricsMetricSeriesValidationService: MetricSeriesValidationServiceGrpcKt.MetricSeriesValidationServiceCoroutineStub,
    private val notificationsNotificationQueryService: NotificationQueryServiceGrpcKt.NotificationQueryServiceCoroutineStub,
    private val notificationsNotificationService: NotificationServiceGrpcKt.NotificationServiceCoroutineStub,
    private val searchSearchIndexQueryService: SearchIndexQueryServiceGrpcKt.SearchIndexQueryServiceCoroutineStub,
    private val telematicsGeoFenceSyncService: GeoFenceSyncServiceGrpcKt.GeoFenceSyncServiceCoroutineStub,
    private val venuesVenueValidationService: VenueValidationServiceGrpcKt.VenueValidationServiceCoroutineStub,
) : DriverLicenseApprovalServiceGrpcKt.DriverLicenseApprovalServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DriverLicenseApprovalController::class.java)

    private val workerForRecallDriverLicense1 = ReplyMailbox<ListPendingDispatchAssignmentsRequest, ListPendingDispatchAssignmentsResponse>(workers) { payload ->
        withTimeout(5_000) { dispatchDispatchAssignmentApprovalService.listPendingDispatchAssignments(payload) }
    }

    private val clientForRecallDriverLicense2 by lazy { driversDriverLicenseReportingService }

    /** Approves a driver license. */
    override suspend fun approveDriverLicense(request: ApproveDriverLicenseRequest): ApproveDriverLicenseResponse = handleApproveDriverLicense(request)

    /** Comments a driver license. */
    override suspend fun commentDriverLicenseApproval(request: CommentDriverLicenseApprovalRequest): CommentDriverLicenseApprovalResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseApprovalService/CommentDriverLicenseApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CommentDriverLicenseApproval", request)
            log.atInfo().addKeyValue("operation", "CommentDriverLicenseApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverLicense.driverLicenseId).log("Recorded audit detail")
            val outbound0 = streamDriverLicenseMetricsRequest {
                this.context = request.context
                this.filter = StreamDriverLicenseMetricsRequestKt.filter {
                    this.region = request.driverLicense.billingAddress.region
                }
                this.subscriber = contactInfo {
                    this.email = request.driverLicense.counterparty.email
                }
            }
            driversDriverLicenseReportingService.streamDriverLicenseMetrics(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = quoteDriverRequest {
                this.context = request.context
                this.driver = driver {
                    this.audit = auditInfo {
                        this.createdBy = request.driverLicense.audit.createdBy
                        this.updatedAt = request.driverLicense.audit.updatedAt
                    }
                    this.primaryAddress = postalAddress {
                        this.locality = request.driverLicense.billingAddress.locality
                    }
                    this.relationshipManager = userRef {
                        this.email = request.details.approver.email
                    }
                }
            }
            val command1: Submission<QuoteDriverRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = driversDriverCalculationService.quoteDriver(payload1)
            val outbound2 = browseMetricSeriesesRequest {
                this.context = request.context
                this.filter = BrowseMetricSeriesesRequestKt.filter {
                    this.createdAfter = request.driverLicense.audit.updatedAt
                    this.ownerId = request.details.approver.userId
                    this.region = reply1.driver.primaryAddress.region
                }
            }
            val adapter2 = OutboundPort<BrowseMetricSeriesesRequest, BrowseMetricSeriesesResponse> { payload ->
                metricsMetricSeriesQueryService.browseMetricSerieses(payload)
            }
            val port2 = DelegatingPort(adapter2)
            val reply2 = port2.execute(outbound2)
            var reply3 = ResolveGeoFenceConflictResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<DriverLicenseLine>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound3 = resolveGeoFenceConflictRequest {
                            this.context = request.context
                            this.geoFence = geoFence {
                                this.audit = auditInfo {
                                    this.createdBy = request.driverLicense.audit.createdBy
                                }
                                this.description = item.description
                                this.discount = geoFenceDiscountAction {
                                    this.amount = item.amount
                                }
                            }
                        }
                        reply3 = telematicsGeoFenceSyncService.resolveGeoFenceConflict(outbound3)
                    }
                }
                try {
                    request.driverLicense.linesList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            commentDriverLicenseApprovalResponse {
                this.driverLicense = driverLicense {
                    this.billingAddress = postalAddress {
                        this.recipient = reply1.driver.primaryAddress.recipient
                    }
                    this.counterparty = driverRef {
                        this.displayName = reply1.driver.relationshipManager.displayName
                    }
                    this.driverLicenseId = request.driverLicenseId
                    this.dueAt = request.driverLicense.dueAt
                }
                this.result = operationResult {
                    this.details.putAll(reply2.result.detailsMap)
                    this.referenceIds.addAll(reply2.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    /** Delegates a driver license. */
    override suspend fun delegateDriverLicenseApproval(request: DelegateDriverLicenseApprovalRequest): DelegateDriverLicenseApprovalResponse {
        return handleDelegateDriverLicenseApproval(request)
    }

    /** Escalates a driver license. */
    override suspend fun escalateDriverLicense(request: EscalateDriverLicenseRequest): EscalateDriverLicenseResponse = handleEscalateDriverLicense(request)

    /** Returns a driver license. */
    override suspend fun getDriverLicenseApprovalStatus(request: GetDriverLicenseApprovalStatusRequest): GetDriverLicenseApprovalStatusResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseApprovalService/GetDriverLicenseApprovalStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetDriverLicenseApprovalStatus", request)
            log.atInfo().addKeyValue("operation", "GetDriverLicenseApprovalStatus").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            getDriverLicenseApprovalStatusResponse {
                this.driverLicense = driverLicense {
                    this.driverLicenseId = request.driverLicenseId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Lists driver licenses a page at a time. */
    override suspend fun listPendingDriverLicenses(request: ListPendingDriverLicensesRequest): ListPendingDriverLicensesResponse {
        return handleListPendingDriverLicenses(request)
    }

    /** Recalls a driver license. */
    override suspend fun recallDriverLicense(request: RecallDriverLicenseRequest): RecallDriverLicenseResponse = handleRecallDriverLicense(request)

    /** Rejects a driver license. */
    override suspend fun rejectDriverLicense(request: RejectDriverLicenseRequest): RejectDriverLicenseResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseApprovalService/RejectDriverLicense") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RejectDriverLicense", request)
            log.atInfo().addKeyValue("operation", "RejectDriverLicense").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverLicense.driverLicenseId).log("Recorded audit detail")
            val outbound0 = escalateWorkOrderRequest {
                this.context = request.context
                this.workOrder = workOrder {
                    this.assignee = userRef {
                        this.displayName = request.details.approver.displayName
                    }
                }
            }
            val reply0 = withTimeout(5_000) { maintenanceWorkOrderApprovalService.escalateWorkOrder(outbound0) }
            val outbound1 = exportSearchIndexesRequest {
                this.context = request.context
                this.filter = ExportSearchIndexesRequestKt.filter {
                    this.createdAfter = request.driverLicense.audit.createdAt
                    this.region = request.driverLicense.billingAddress.region
                }
            }
            searchSearchIndexQueryService.exportSearchIndexes(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound2 = explainMetricSeriesDecisionRequest {
                this.context = request.context
                this.metricSeries = metricSeries {
                    this.audit = auditInfo {
                        this.createdBy = reply0.workOrder.audit.createdBy
                    }
                    this.owner = userRef {
                        this.email = request.details.approver.email
                    }
                }
            }
            val reply2 = metricsMetricSeriesValidationService.explainMetricSeriesDecision(outbound2)
            val outbound3 = summarizeNotificationsRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.reason = request.reason
            }
            val reply3 = notificationsNotificationQueryService.summarizeNotifications(outbound3)
            rejectDriverLicenseResponse {
                this.driverLicense = driverLicense {
                    this.audit = auditInfo {
                        this.updatedBy = reply2.metricSeries.audit.updatedBy
                    }
                    this.driverLicenseId = request.driverLicenseId
                    this.total = request.driverLicense.total
                }
                this.outcome = RejectDriverLicenseResponseKt.outcome {
                    this.status = reply3.result.status
                }
                this.result = operationResult {
                    this.message = reply3.result.message
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    /** Submits a driver license. */
    override suspend fun submitDriverLicense(request: SubmitDriverLicenseRequest): SubmitDriverLicenseResponse {
        return handleSubmitDriverLicense(request)
    }

    private suspend fun handleApproveDriverLicense(request: ApproveDriverLicenseRequest): ApproveDriverLicenseResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseApprovalService/ApproveDriverLicense") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ApproveDriverLicense", request)
            log.atInfo().addKeyValue("operation", "ApproveDriverLicense").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverLicense.driverLicenseId).log("Recorded audit detail")
            val outbound0 = batchGetNotificationsRequest {
                this.context = request.context
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = notificationsNotificationService.batchGetNotifications(envelope0.unpack(BatchGetNotificationsRequest::class.java))
            val outbound1 = scoreVenueRequest {
                this.context = request.context
                this.venue = venue {
                    this.audit = auditInfo {
                        this.createdAt = request.driverLicense.audit.createdAt
                        this.etag = request.driverLicense.audit.etag
                    }
                }
            }
            val reply1 = venuesVenueValidationService.scoreVenue(WireCopies.json(outbound1))
            approveDriverLicenseResponse {
                this.driverLicense = driverLicense {
                    this.billingAddress = postalAddress {
                        this.region = reply1.venue.address.region
                    }
                    this.driverLicenseId = request.driverLicenseId
                }
                this.outcome = ApproveDriverLicenseResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleDelegateDriverLicenseApproval(request: DelegateDriverLicenseApprovalRequest): DelegateDriverLicenseApprovalResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseApprovalService/DelegateDriverLicenseApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DelegateDriverLicenseApproval", request)
            log.atInfo().addKeyValue("operation", "DelegateDriverLicenseApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverLicense.driverLicenseId).log("Recorded audit detail")
            val outbound0 = explainDutyStatusCalculationRequest {
                this.context = request.context
                this.details = ExplainDutyStatusCalculationRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.driverLicense.counterparty.email
                    }
                }
            }
            val invoke0: suspend (ExplainDutyStatusCalculationRequest, Metadata) -> ExplainDutyStatusCalculationResponse = driversDutyStatusCalculationService::explainDutyStatusCalculation
            val reply0 = invoke0(outbound0, Metadata())
            val outbound1 = SearchNotificationsRequest.newBuilder().apply {
                setContext(request.context)
                setFilter(SearchNotificationsRequestKt.filter {
                    this.createdAfter = reply0.outcome.effectiveAt
                    this.labels.putAll(reply0.dutyStatus.attributesMap)
                    this.ownerId = reply0.dutyStatus.subject.driverLicenseId
                    this.region = request.driverLicense.billingAddress.region
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> notificationsNotificationService.searchNotifications(value) }
            delegateDriverLicenseApprovalResponse {
                this.driverLicense = driverLicense {
                    this.driverLicenseId = request.driverLicenseId
                    this.notes = request.driverLicense.notes
                }
                this.outcome = DelegateDriverLicenseApprovalResponseKt.outcome {
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleEscalateDriverLicense(request: EscalateDriverLicenseRequest): EscalateDriverLicenseResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseApprovalService/EscalateDriverLicense") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EscalateDriverLicense", request)
            log.atInfo().addKeyValue("operation", "EscalateDriverLicense").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverLicense.driverLicenseId).log("Recorded audit detail")
            var reply0 = ListPendingDispatchAssignmentsResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.driverLicense.linesList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound0 = listPendingDispatchAssignmentsRequest {
                                this.context = request.context
                                this.filter = ListPendingDispatchAssignmentsRequestKt.filter {
                                    this.labels.putAll(item.attributesMap)
                                    this.region = request.driverLicense.billingAddress.region
                                }
                            }
                            dispatchDispatchAssignmentApprovalService.listPendingDispatchAssignments(outbound0)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply0 = it }
                }
            }
            escalateDriverLicenseResponse {
                this.driverLicense = driverLicense {
                    this.driverLicenseId = request.driverLicenseId
                    this.status = request.driverLicense.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleListPendingDriverLicenses(request: ListPendingDriverLicensesRequest): ListPendingDriverLicensesResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseApprovalService/ListPendingDriverLicenses") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListPendingDriverLicenses", request)
            log.atInfo().addKeyValue("operation", "ListPendingDriverLicenses").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = pullGeoFencesRequest {
                this.context = request.context
                this.geoFence = geoFence {
                    this.owner = userRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = ClientRegistry.lookup("acme.telematics.v1.GeoFenceSyncService", GeoFenceSyncServiceGrpcKt.GeoFenceSyncServiceCoroutineStub::class.java).pullGeoFences(outbound0)
            listPendingDriverLicensesResponse {
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleRecallDriverLicense(request: RecallDriverLicenseRequest): RecallDriverLicenseResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseApprovalService/RecallDriverLicense") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecallDriverLicense", request)
            log.atInfo().addKeyValue("operation", "RecallDriverLicense").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverLicense.driverLicenseId).log("Recorded audit detail")
            val outbound0 = suspendDriverRequest {
                this.context = request.context
                this.details = SuspendDriverRequestKt.details {
                    this.effectiveAt = request.details.effectiveAt
                    this.newOwner = userRef {
                        this.displayName = request.details.approver.displayName
                        this.email = request.details.approver.email
                        this.userId = request.details.approver.userId
                    }
                }
                this.driverId = request.driverLicense.counterparty.driverId
            }
            val reply0 = driversDriverLifecycleService.suspendDriver(WireCopies.reflective(outbound0))
            val outbound1 = listPendingDispatchAssignmentsRequest {
                this.context = request.context
                this.filter = ListPendingDispatchAssignmentsRequestKt.filter {
                    this.createdAfter = request.driverLicense.audit.createdAt
                    this.region = reply0.driver.primaryAddress.region
                }
            }
            val reply1 = workerForRecallDriverLicense1.submit(outbound1)
            val outbound2 = listDriverLicenseReportsRequest {
                this.context = request.context
                this.filter = ListDriverLicenseReportsRequestKt.filter {
                    this.createdAfter = reply0.driver.audit.createdAt
                    this.region = request.driverLicense.billingAddress.region
                }
            }
            val reply2 = clientForRecallDriverLicense2.listDriverLicenseReports(outbound2)
            val outbound3 = cloneMetricSeriesRequest {
                this.context = request.context
                this.metricSeries = metricSeries {
                    this.address = postalAddress {
                        this.countryCode = reply0.driver.primaryAddress.countryCode
                        this.postalCode = request.driverLicense.billingAddress.postalCode
                    }
                    this.owner = userRef {
                        this.displayName = reply0.driver.relationshipManager.displayName
                        this.email = reply0.driver.contact.email
                    }
                }
            }
            val reply3 = flowOf(outbound3).map { value -> metricsMetricSeriesService.cloneMetricSeries(value) }.single()
            recallDriverLicenseResponse {
                this.driverLicense = driverLicense {
                    this.billingAddress = postalAddress {
                        this.postalCode = reply3.metricSeries.address.postalCode
                    }
                    this.counterparty = driverRef {
                        this.displayName = reply3.metricSeries.owner.displayName
                    }
                    this.driverLicenseId = request.driverLicenseId
                    this.status = request.driverLicense.status
                }
                this.outcome = RecallDriverLicenseResponseKt.outcome {
                    this.status = reply2.result.status
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply1.result.message
                    this.referenceIds.addAll(reply2.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSubmitDriverLicense(request: SubmitDriverLicenseRequest): SubmitDriverLicenseResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseApprovalService/SubmitDriverLicense") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SubmitDriverLicense", request)
            log.atInfo().addKeyValue("operation", "SubmitDriverLicense").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverLicense.driverLicenseId).log("Recorded audit detail")
            val outbound0 = calculateDutyStatusRequest {}.driversCopy {
                this.context = request.context
                this.details = CalculateDutyStatusRequestKt.details {
                    this.destination = postalAddress {
                        this.locality = request.driverLicense.billingAddress.locality
                        this.recipient = request.driverLicense.billingAddress.recipient
                        this.region = request.driverLicense.billingAddress.region
                    }
                }
                this.dutyStatus = dutyStatus {
                    this.subject = driverLicenseRef {
                        this.number = request.driverLicense.number
                    }
                }
            }
            val reply0 = driversDutyStatusCalculationService.calculateDutyStatus(outbound0)
            submitDriverLicenseResponse {
                this.driverLicense = driverLicense {
                    this.driverLicenseId = request.driverLicenseId
                    this.metadata.putAll(request.driverLicense.metadataMap)
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
            }
        }
}
