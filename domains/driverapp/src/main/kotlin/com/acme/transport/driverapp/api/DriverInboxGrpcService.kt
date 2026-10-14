package com.acme.transport.driverapp.api

import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.CustomerSegmentSyncServiceGrpcKt
import com.acme.customers.v1.HouseholdReportingServiceGrpcKt
import com.acme.customers.v1.MapPreferenceImportColumnsRequest
import com.acme.customers.v1.MapPreferenceImportColumnsResponse
import com.acme.customers.v1.PreferenceImportServiceGrpcKt
import com.acme.customers.v1.PullCustomerSegmentsRequest
import com.acme.customers.v1.PullCustomerSegmentsResponse
import com.acme.customers.v1.RetryPreferenceImportRequest
import com.acme.customers.v1.RetryPreferenceImportRequestKt
import com.acme.customers.v1.ScheduleHouseholdReportRequestKt
import com.acme.customers.v1.copy as customersCopy
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.customerSegment
import com.acme.customers.v1.mapPreferenceImportColumnsRequest
import com.acme.customers.v1.preference
import com.acme.customers.v1.pullCustomerSegmentsRequest
import com.acme.customers.v1.retryPreferenceImportRequest
import com.acme.customers.v1.scheduleHouseholdReportRequest
import com.acme.driverapp.v1.BatchCreateDriverInboxesRequest
import com.acme.driverapp.v1.BatchCreateDriverInboxesResponse
import com.acme.driverapp.v1.BatchCreateDriverInboxesResponseKt
import com.acme.driverapp.v1.BatchGetDriverInboxesRequest
import com.acme.driverapp.v1.BatchGetDriverInboxesResponse
import com.acme.driverapp.v1.BrowseDriverInboxesRequestKt
import com.acme.driverapp.v1.CloneDriverInboxRequest
import com.acme.driverapp.v1.CloneDriverInboxResponse
import com.acme.driverapp.v1.CreateDriverInboxRequest
import com.acme.driverapp.v1.CreateDriverInboxResponse
import com.acme.driverapp.v1.DeleteDriverInboxReportRequest
import com.acme.driverapp.v1.DeleteDriverInboxRequest
import com.acme.driverapp.v1.DeleteDriverInboxResponse
import com.acme.driverapp.v1.DisconnectDriverHomeSourceRequestKt
import com.acme.driverapp.v1.DriverHomeApprovalServiceGrpcKt
import com.acme.driverapp.v1.DriverHomeIntegrationServiceGrpcKt
import com.acme.driverapp.v1.DriverHomeServiceGrpcKt
import com.acme.driverapp.v1.DriverHomeWorkflowServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxApprovalServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxIntegrationServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxQueryServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxReportingServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxServiceGrpcKt
import com.acme.driverapp.v1.GetDriverInboxRequest
import com.acme.driverapp.v1.GetDriverInboxResponse
import com.acme.driverapp.v1.ListDriverInboxReportsRequest
import com.acme.driverapp.v1.ListDriverInboxReportsRequestKt
import com.acme.driverapp.v1.ListDriverInboxesRequest
import com.acme.driverapp.v1.ListDriverInboxesResponse
import com.acme.driverapp.v1.ListPendingDriverInboxesRequest
import com.acme.driverapp.v1.ListPendingDriverInboxesRequestKt
import com.acme.driverapp.v1.SearchDriverHomesRequestKt
import com.acme.driverapp.v1.SearchDriverInboxesRequest
import com.acme.driverapp.v1.SearchDriverInboxesResponse
import com.acme.driverapp.v1.UpdateDriverInboxRequest
import com.acme.driverapp.v1.UpdateDriverInboxResponse
import com.acme.driverapp.v1.WatchDriverInboxesRequest
import com.acme.driverapp.v1.WatchDriverInboxesResponse
import com.acme.driverapp.v1.approveDriverHomeRequest
import com.acme.driverapp.v1.batchCreateDriverInboxesResponse
import com.acme.driverapp.v1.batchGetDriverInboxesResponse
import com.acme.driverapp.v1.browseDriverInboxesRequest
import com.acme.driverapp.v1.cloneDriverInboxResponse
import com.acme.driverapp.v1.commentDriverInboxApprovalRequest
import com.acme.driverapp.v1.createDriverInboxResponse
import com.acme.driverapp.v1.deleteDriverInboxReportRequest
import com.acme.driverapp.v1.deleteDriverInboxResponse
import com.acme.driverapp.v1.disconnectDriverHomeSourceRequest
import com.acme.driverapp.v1.disconnectDriverInboxSourceRequest
import com.acme.driverapp.v1.driverHome
import com.acme.driverapp.v1.driverHomeSla
import com.acme.driverapp.v1.driverInbox
import com.acme.driverapp.v1.getDriverInboxResponse
import com.acme.driverapp.v1.listDriverInboxReportsRequest
import com.acme.driverapp.v1.listDriverInboxesResponse
import com.acme.driverapp.v1.listPendingDriverInboxesRequest
import com.acme.driverapp.v1.resumeDriverHomeWorkflowRequest
import com.acme.driverapp.v1.searchDriverHomesRequest
import com.acme.driverapp.v1.searchDriverInboxesResponse
import com.acme.driverapp.v1.updateDriverHomeRequest
import com.acme.driverapp.v1.updateDriverInboxResponse
import com.acme.driverapp.v1.watchDriverInboxesResponse
import com.acme.drivers.v1.DriverLicenseApprovalServiceGrpcKt
import com.acme.drivers.v1.RejectDriverLicenseRequest
import com.acme.drivers.v1.RejectDriverLicenseResponse
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverLicenseBankSettlement
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.rejectDriverLicenseRequest
import com.acme.fuel.v1.FuelTransactionReportingServiceGrpcKt
import com.acme.fuel.v1.GenerateFuelTransactionReportRequestKt
import com.acme.fuel.v1.generateFuelTransactionReportRequest
import com.acme.fuel.v1.streamFuelTransactionMetricsRequest
import com.acme.identity.v1.userRef
import com.acme.metrics.v1.MetricSeriesValidationServiceGrpcKt
import com.acme.metrics.v1.PreviewMetricSeriesRequest
import com.acme.metrics.v1.PreviewMetricSeriesRequestKt
import com.acme.metrics.v1.PreviewMetricSeriesResponse
import com.acme.metrics.v1.metricSeries
import com.acme.metrics.v1.previewMetricSeriesRequest
import com.acme.notifications.v1.NotificationServiceGrpcKt
import com.acme.notifications.v1.createNotificationRequest
import com.acme.notifications.v1.notification
import com.acme.telematics.v1.GeoFenceReportingServiceGrpcKt
import com.acme.telematics.v1.StreamGeoFenceMetricsRequestKt
import com.acme.telematics.v1.streamGeoFenceMetricsRequest
import com.acme.transport.driverapp.DriverappRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
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

/** Manages driver inboxes: creates, reads, changes, and lists them. */
class DriverInboxGrpcService(
    private val store: DriverappRepository,
    private val workers: CoroutineScope,
    private val customersCustomerSegmentSyncService: CustomerSegmentSyncServiceGrpcKt.CustomerSegmentSyncServiceCoroutineStub,
    private val customersHouseholdReportingService: HouseholdReportingServiceGrpcKt.HouseholdReportingServiceCoroutineStub,
    private val customersPreferenceImportService: PreferenceImportServiceGrpcKt.PreferenceImportServiceCoroutineStub,
    private val driverappDriverHomeApprovalService: DriverHomeApprovalServiceGrpcKt.DriverHomeApprovalServiceCoroutineStub,
    private val driverappDriverHomeIntegrationService: DriverHomeIntegrationServiceGrpcKt.DriverHomeIntegrationServiceCoroutineStub,
    private val driverappDriverHomeService: DriverHomeServiceGrpcKt.DriverHomeServiceCoroutineStub,
    private val driverappDriverHomeWorkflowService: DriverHomeWorkflowServiceGrpcKt.DriverHomeWorkflowServiceCoroutineStub,
    private val driverappDriverInboxApprovalService: DriverInboxApprovalServiceGrpcKt.DriverInboxApprovalServiceCoroutineStub,
    private val driverappDriverInboxIntegrationService: DriverInboxIntegrationServiceGrpcKt.DriverInboxIntegrationServiceCoroutineStub,
    private val driverappDriverInboxQueryService: DriverInboxQueryServiceGrpcKt.DriverInboxQueryServiceCoroutineStub,
    private val driverappDriverInboxReportingService: DriverInboxReportingServiceGrpcKt.DriverInboxReportingServiceCoroutineStub,
    private val driversDriverLicenseApprovalService: DriverLicenseApprovalServiceGrpcKt.DriverLicenseApprovalServiceCoroutineStub,
    private val fuelFuelTransactionReportingService: FuelTransactionReportingServiceGrpcKt.FuelTransactionReportingServiceCoroutineStub,
    private val metricsMetricSeriesValidationService: MetricSeriesValidationServiceGrpcKt.MetricSeriesValidationServiceCoroutineStub,
    private val notificationsNotificationService: NotificationServiceGrpcKt.NotificationServiceCoroutineStub,
    private val telematicsGeoFenceReportingService: GeoFenceReportingServiceGrpcKt.GeoFenceReportingServiceCoroutineStub,
) : DriverInboxServiceGrpcKt.DriverInboxServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DriverInboxGrpcService::class.java)

    private val workerForBatchCreateDriverInboxes0 = ReplyMailbox<PullCustomerSegmentsRequest, PullCustomerSegmentsResponse>(workers) { payload ->
        withTimeout(5_000) { customersCustomerSegmentSyncService.pullCustomerSegments(payload) }
    }

    private val workerForWatchDriverInboxes2 = ReplyMailbox<MapPreferenceImportColumnsRequest, MapPreferenceImportColumnsResponse>(workers) { payload ->
        withTimeout(5_000) { customersPreferenceImportService.mapPreferenceImportColumns(payload) }
    }

    /** Takes a stream of driver inboxes and answers once at the end. */
    override suspend fun batchCreateDriverInboxes(requests: Flow<BatchCreateDriverInboxesRequest>): BatchCreateDriverInboxesResponse {
        var answer = BatchCreateDriverInboxesResponse.getDefaultInstance()
        requests.map { request -> handleBatchCreateDriverInboxes(request) }.collect { answer = it }
        return answer
    }

    /** Returns several driver inboxes at once. */
    override suspend fun batchGetDriverInboxes(request: BatchGetDriverInboxesRequest): BatchGetDriverInboxesResponse = handleBatchGetDriverInboxes(request)

    /** Creates a driver inbox. */
    override suspend fun cloneDriverInbox(request: CloneDriverInboxRequest): CloneDriverInboxResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxService/CloneDriverInbox") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CloneDriverInbox", request)
            log.atInfo().addKeyValue("operation", "CloneDriverInbox").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverInbox.driverInboxId).log("Recorded audit detail")
            val outbound0 = createNotificationRequest {
                this.context = request.context
                this.notification = notification {
                    this.audit = auditInfo {
                        this.updatedBy = request.driverInbox.audit.updatedBy
                    }
                }
            }
            val reply0 = withTimeout(5_000) { notificationsNotificationService.createNotification(outbound0) }
            val configure1: GenerateFuelTransactionReportRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.details = GenerateFuelTransactionReportRequestKt.details {
                    this.filters.putAll(reply0.notification.customFieldsMap)
                }
            }
            val outbound1 = generateFuelTransactionReportRequest(configure1)
            val reply1 = fuelFuelTransactionReportingService.generateFuelTransactionReport(outbound1)
            val outbound2 = approveDriverHomeRequest {
                this.context = request.context
                this.driverHome = driverHome {
                    this.customer = customerRef {
                        this.displayName = request.driverInbox.counterparty.displayName
                    }
                    this.sla = driverHomeSla {
                        this.policyId = reply0.notification.sla.policyId
                    }
                }
            }
            val reply2 = driverappDriverHomeApprovalService.approveDriverHome(outbound2)
            val outbound3 = retryPreferenceImportRequest {
                this.context = request.context
                this.details = RetryPreferenceImportRequestKt.details {
                    this.notify = contactInfo {
                        this.email = reply2.driverHome.requester.email
                        this.phone = reply2.driverHome.requester.phone
                    }
                }
                this.preference = preference {
                    this.audit = auditInfo {
                        this.etag = reply0.notification.audit.etag
                    }
                    this.owner = userRef {
                        this.userId = reply0.notification.assignee.userId
                    }
                }
            }
            val reply3 = customersPreferenceImportService.retryPreferenceImport(outbound3)
            cloneDriverInboxResponse {
                this.driverInbox = driverInbox {
                    this.audit = auditInfo {
                        this.etag = reply0.notification.audit.etag
                    }
                    this.counterparty = customerRef {
                        this.email = reply0.notification.requester.email
                    }
                    this.metadata.putAll(request.driverInbox.metadataMap)
                    this.status = request.driverInbox.status
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.details.putAll(reply1.result.detailsMap)
                    this.message = reply3.outcome.message
                    this.status = "ok"
                }
            }
        }

    /** Creates a driver inbox. */
    override suspend fun createDriverInbox(request: CreateDriverInboxRequest): CreateDriverInboxResponse {
        return handleCreateDriverInbox(request)
    }

    /** Deletes a driver inbox. */
    override suspend fun deleteDriverInbox(request: DeleteDriverInboxRequest): DeleteDriverInboxResponse = handleDeleteDriverInbox(request)

    /** Returns a driver inbox. */
    override suspend fun getDriverInbox(request: GetDriverInboxRequest): GetDriverInboxResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxService/GetDriverInbox") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetDriverInbox", request)
            log.atInfo().addKeyValue("operation", "GetDriverInbox").log("Handling transport request")
            val outbound0 = deleteDriverInboxReportRequest {
                this.context = request.context
                this.driverInboxId = request.driverInboxId
            }
            val command0: Submission<DeleteDriverInboxReportRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = driverappDriverInboxReportingService.deleteDriverInboxReport(payload0)
            val outbound1 = previewMetricSeriesRequest {
                this.context = request.context
                this.details = PreviewMetricSeriesRequestKt.details {
                    this.locale = request.context.locale
                }
                this.metricSeries = metricSeries {
                    this.owner = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val adapter1 = OutboundPort<PreviewMetricSeriesRequest, PreviewMetricSeriesResponse> { payload ->
                metricsMetricSeriesValidationService.previewMetricSeries(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            getDriverInboxResponse {
                this.driverInbox = driverInbox {
                    this.billingAddress = postalAddress {
                        this.recipient = reply1.metricSeries.address.recipient
                    }
                    this.driverInboxId = request.driverInboxId
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    /** Lists driver inboxes a page at a time. */
    override suspend fun listDriverInboxes(request: ListDriverInboxesRequest): ListDriverInboxesResponse {
        return handleListDriverInboxes(request)
    }

    /** Lists driver inboxes a page at a time. */
    override suspend fun searchDriverInboxes(request: SearchDriverInboxesRequest): SearchDriverInboxesResponse = handleSearchDriverInboxes(request)

    /** Changes a driver inbox. */
    override suspend fun updateDriverInbox(request: UpdateDriverInboxRequest): UpdateDriverInboxResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxService/UpdateDriverInbox") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "UpdateDriverInbox", request)
            log.atInfo().addKeyValue("operation", "UpdateDriverInbox").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverInbox.driverInboxId).log("Recorded audit detail")
            val outbound0 = rejectDriverLicenseRequest {
                this.context = request.context
                this.driverLicense = driverLicense {
                    this.bank = driverLicenseBankSettlement {
                        this.accountHolder = request.driverInbox.bank.accountHolder
                    }
                    this.billingAddress = postalAddress {
                        this.postalCode = request.driverInbox.billingAddress.postalCode
                    }
                    this.counterparty = driverRef {
                        this.displayName = request.driverInbox.counterparty.displayName
                    }
                }
            }
            val invoke0: suspend (RejectDriverLicenseRequest, Metadata) -> RejectDriverLicenseResponse = driversDriverLicenseApprovalService::rejectDriverLicense
            val reply0 = invoke0(outbound0, Metadata())
            val outbound1 = ListDriverInboxReportsRequest.newBuilder().apply {
                setContext(request.context)
                setFilter(ListDriverInboxReportsRequestKt.filter {
                    this.createdAfter = reply0.driverLicense.issuedAt
                    this.region = reply0.driverLicense.billingAddress.region
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> driverappDriverInboxReportingService.listDriverInboxReports(value) }
            val outbound2 = RequestFactory.create {
                browseDriverInboxesRequest {
                    this.context = request.context
                    this.filter = BrowseDriverInboxesRequestKt.filter {
                        this.createdAfter = request.driverInbox.audit.updatedAt
                        this.region = request.driverInbox.billingAddress.region
                    }
                }
            }
            val reply2 = driverappDriverInboxQueryService.browseDriverInboxes(outbound2)
            updateDriverInboxResponse {
                this.driverInbox = driverInbox {
                    this.billingAddress = request.driverInbox.billingAddress
                    this.dueAt = reply0.driverLicense.dueAt
                    this.number = request.driverInbox.number
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.status = reply1.result.status
                }
            }
        }

    /** Streams the changes of driver inboxes as they happen. */
    override fun watchDriverInboxes(request: WatchDriverInboxesRequest): Flow<WatchDriverInboxesResponse> = flow {
        emit(handleWatchDriverInboxes(request))
    }

    private suspend fun handleBatchCreateDriverInboxes(request: BatchCreateDriverInboxesRequest): BatchCreateDriverInboxesResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxService/BatchCreateDriverInboxes") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BatchCreateDriverInboxes", request)
            log.atInfo().addKeyValue("operation", "BatchCreateDriverInboxes").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.header.source).log("Recorded audit detail")
            val outbound0 = pullCustomerSegmentsRequest {
                this.context = request.context
                this.customerSegment = customerSegment {
                    this.audit = auditInfo {
                        this.etag = request.record.audit.etag
                        this.updatedAt = request.record.audit.updatedAt
                    }
                }
            }
            val reply0 = workerForBatchCreateDriverInboxes0.submit(outbound0)
            val outbound1 = commentDriverInboxApprovalRequest {
                this.context = request.context
                this.driverInbox = driverInbox {
                    this.audit = auditInfo {
                        this.updatedBy = when (request.chunkCase) {
                            BatchCreateDriverInboxesRequest.ChunkCase.RECORD -> request.record.audit.updatedBy
                            else -> ""
                        }
                    }
                }
            }
            val reply1 = driverappDriverInboxApprovalService.commentDriverInboxApproval(outbound1)
            val outbound2 = searchDriverHomesRequest {
                this.context = request.context
                this.filter = SearchDriverHomesRequestKt.filter {
                    this.createdAfter = reply0.customerSegment.audit.createdAt
                    this.labels.putAll(reply0.customerSegment.parametersMap)
                    this.ownerId = reply0.customerSegment.owner.userId
                    this.region = request.record.billingAddress.region
                }
            }
            val reply2 = flowOf(outbound2).map { value -> driverappDriverHomeService.searchDriverHomes(value) }.single()
            batchCreateDriverInboxesResponse {
                this.rejections.addAll(listOf(BatchCreateDriverInboxesResponseKt.rejection {
                    this.reason = reply0.customerSegment.block.reason
                }))
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.details.putAll(reply1.result.detailsMap)
                    this.message = reply0.outcome.message
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleBatchGetDriverInboxes(request: BatchGetDriverInboxesRequest): BatchGetDriverInboxesResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxService/BatchGetDriverInboxes") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BatchGetDriverInboxes", request)
            log.atInfo().addKeyValue("operation", "BatchGetDriverInboxes").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            batchGetDriverInboxesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private fun CreateDriverInboxRequest.toRetryPreferenceImportFromCreateDriverInbox1(): RetryPreferenceImportRequest {
        val source = this
        return retryPreferenceImportRequest {
            this.context = source.context
            this.preference = preference {
                this.audit = auditInfo {
                    this.updatedBy = source.driverInbox.audit.updatedBy
                }
                this.owner = userRef {
                    this.email = source.driverInbox.counterparty.email
                }
            }
        }
    }

    private suspend fun handleCreateDriverInbox(request: CreateDriverInboxRequest): CreateDriverInboxResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxService/CreateDriverInbox") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CreateDriverInbox", request)
            log.atInfo().addKeyValue("operation", "CreateDriverInbox").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverInbox.number).log("Recorded audit detail")
            val outbound0 = scheduleHouseholdReportRequest {}.customersCopy {
                this.annotations.putAll(request.context.baggageMap)
                this.context = request.context
                this.details = ScheduleHouseholdReportRequestKt.details {
                    this.filters.putAll(request.driverInbox.metadataMap)
                }
            }
            val reply0 = customersHouseholdReportingService.scheduleHouseholdReport(outbound0)
            val outbound1 = request.toRetryPreferenceImportFromCreateDriverInbox1()
            val reply1 = customersPreferenceImportService.retryPreferenceImport(outbound1)
            val outbound2 = updateDriverHomeRequest {
                this.context = request.context
                this.driverHome = driverHome {
                    this.audit = auditInfo {
                        this.createdAt = request.driverInbox.audit.createdAt
                        this.etag = reply1.preference.audit.etag
                        this.updatedAt = reply1.preference.audit.updatedAt
                    }
                    this.customer = customerRef {
                        this.email = request.driverInbox.counterparty.email
                    }
                }
            }
            val reply2 = driverappDriverHomeService.updateDriverHome(outbound2)
            createDriverInboxResponse {
                this.driverInbox = driverInbox {
                    this.audit = auditInfo {
                        this.createdAt = reply1.preference.audit.createdAt
                        this.createdBy = reply2.driverHome.audit.createdBy
                        this.etag = reply1.preference.audit.etag
                    }
                    this.counterparty = customerRef {
                        this.email = reply2.driverHome.assignee.email
                    }
                    this.driverInboxId = request.driverInbox.driverInboxId
                    this.dueAt = request.driverInbox.dueAt
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleDeleteDriverInbox(request: DeleteDriverInboxRequest): DeleteDriverInboxResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxService/DeleteDriverInbox") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DeleteDriverInbox", request)
            log.atInfo().addKeyValue("operation", "DeleteDriverInbox").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverInboxId).log("Recorded audit detail")
            val outbound0 = listPendingDriverInboxesRequest {
                this.context = request.context
                this.filter = ListPendingDriverInboxesRequestKt.filter {
                    this.createdAfter = request.context.deadline
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = driverappDriverInboxApprovalService.listPendingDriverInboxes(envelope0.unpack(ListPendingDriverInboxesRequest::class.java))
            val outbound1 = disconnectDriverInboxSourceRequest {
                this.context = request.context
                this.reason = request.reason
            }
            val reply1 = driverappDriverInboxIntegrationService.disconnectDriverInboxSource(WireCopies.json(outbound1))
            val outbound2 = streamFuelTransactionMetricsRequest {
                this.context = request.context
                this.subscriber = contactInfo {
                    this.email = reply1.driverInbox.counterparty.email
                }
            }
            fuelFuelTransactionReportingService.streamFuelTransactionMetrics(outbound2).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            deleteDriverInboxResponse {
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleListDriverInboxes(request: ListDriverInboxesRequest): ListDriverInboxesResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxService/ListDriverInboxes") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListDriverInboxes", request)
            log.atInfo().addKeyValue("operation", "ListDriverInboxes").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            listDriverInboxesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSearchDriverInboxes(request: SearchDriverInboxesRequest): SearchDriverInboxesResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxService/SearchDriverInboxes") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SearchDriverInboxes", request)
            log.atInfo().addKeyValue("operation", "SearchDriverInboxes").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = streamGeoFenceMetricsRequest {
                this.context = request.context
                this.filter = StreamGeoFenceMetricsRequestKt.filter {
                    this.query = request.filter.query
                }
            }
            telematicsGeoFenceReportingService.streamGeoFenceMetrics(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            searchDriverInboxesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleWatchDriverInboxes(request: WatchDriverInboxesRequest): WatchDriverInboxesResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxService/WatchDriverInboxes") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "WatchDriverInboxes", request)
            log.atInfo().addKeyValue("operation", "WatchDriverInboxes").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = disconnectDriverHomeSourceRequest {
                this.context = request.context
                this.details = DisconnectDriverHomeSourceRequestKt.details {
                    this.receivedAt = request.filter.createdAfter
                }
            }
            val reply0 = ClientRegistry.lookup("acme.driverapp.v1.DriverHomeIntegrationService", DriverHomeIntegrationServiceGrpcKt.DriverHomeIntegrationServiceCoroutineStub::class.java).disconnectDriverHomeSource(outbound0)
            val outbound1 = resumeDriverHomeWorkflowRequest {
                this.context = request.context
                this.driverHome = driverHome {
                    this.sla = driverHomeSla {
                        this.pausedFor = reply0.driverHome.sla.pausedFor
                        this.policyId = reply0.driverHome.sla.policyId
                    }
                }
            }
            val reply1 = driverappDriverHomeWorkflowService.resumeDriverHomeWorkflow(WireCopies.reflective(outbound1))
            val outbound2 = mapPreferenceImportColumnsRequest {
                this.context = request.context
                this.preference = preference {
                    this.audit = auditInfo {
                        this.etag = reply1.driverHome.audit.etag
                    }
                    this.owner = userRef {
                        this.email = reply1.driverHome.assignee.email
                        this.userId = reply0.driverHome.assignee.userId
                    }
                }
            }
            val reply2 = workerForWatchDriverInboxes2.submit(outbound2)
            watchDriverInboxesResponse {
                this.driverInbox = driverInbox {
                    this.audit = auditInfo {
                        this.etag = reply0.driverHome.audit.etag
                        this.updatedBy = reply2.preference.audit.updatedBy
                    }
                    this.billingAddress = postalAddress {
                        this.region = request.filter.region
                    }
                    this.counterparty = customerRef {
                        this.email = request.subscriber.email
                    }
                }
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.referenceIds.addAll((reply0.result.referenceIdsList) + (reply1.result.referenceIdsList))
                    this.status = reply1.result.status
                }
            }
        }
}
