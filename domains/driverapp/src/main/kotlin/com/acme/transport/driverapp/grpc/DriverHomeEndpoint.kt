package com.acme.transport.driverapp.grpc

import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.pageInfo
import com.acme.common.v1.pageRequest
import com.acme.customers.v1.CustomerSegmentReportingServiceGrpcKt
import com.acme.customers.v1.ShareCustomerSegmentReportRequestKt
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.shareCustomerSegmentReportRequest
import com.acme.driverapp.v1.BatchCreateDriverHomesRequest
import com.acme.driverapp.v1.BatchCreateDriverHomesResponse
import com.acme.driverapp.v1.CloneDriverHomeRequest
import com.acme.driverapp.v1.CloneDriverHomeResponse
import com.acme.driverapp.v1.CreateDriverHomeRequest
import com.acme.driverapp.v1.CreateDriverHomeResponse
import com.acme.driverapp.v1.DriverHomeComment
import com.acme.driverapp.v1.DriverHomeNotificationServiceGrpcKt
import com.acme.driverapp.v1.DriverHomeReportingServiceGrpcKt
import com.acme.driverapp.v1.DriverHomeServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxApprovalServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxNotificationServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxReportingServiceGrpcKt
import com.acme.driverapp.v1.GenerateDriverHomeReportRequestKt
import com.acme.driverapp.v1.GetDriverHomeRequest
import com.acme.driverapp.v1.GetDriverHomeResponse
import com.acme.driverapp.v1.ListDriverHomesRequest
import com.acme.driverapp.v1.ListDriverHomesResponse
import com.acme.driverapp.v1.ListDriverInboxSubscriptionsRequestKt
import com.acme.driverapp.v1.ListPendingDriverInboxesRequestKt
import com.acme.driverapp.v1.ScheduleDriverInboxReportRequest
import com.acme.driverapp.v1.ScheduleDriverInboxReportResponse
import com.acme.driverapp.v1.SearchDriverHomesRequest
import com.acme.driverapp.v1.SearchDriverHomesResponse
import com.acme.driverapp.v1.UpdateDriverHomeRequest
import com.acme.driverapp.v1.UpdateDriverHomeResponse
import com.acme.driverapp.v1.batchCreateDriverHomesResponse
import com.acme.driverapp.v1.cloneDriverHomeResponse
import com.acme.driverapp.v1.copy as driverappCopy
import com.acme.driverapp.v1.createDriverHomeResponse
import com.acme.driverapp.v1.driverHome
import com.acme.driverapp.v1.driverHomeComment
import com.acme.driverapp.v1.generateDriverHomeReportRequest
import com.acme.driverapp.v1.getDriverHomeResponse
import com.acme.driverapp.v1.listDriverHomesResponse
import com.acme.driverapp.v1.listDriverInboxSubscriptionsRequest
import com.acme.driverapp.v1.listPendingDriverInboxesRequest
import com.acme.driverapp.v1.scheduleDriverInboxReportRequest
import com.acme.driverapp.v1.searchDriverHomesResponse
import com.acme.driverapp.v1.sendDriverHomeTestNoticeRequest
import com.acme.driverapp.v1.updateDriverHomeResponse
import com.acme.fuel.v1.FuelPolicyApprovalServiceGrpcKt
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.rejectFuelPolicyRequest
import com.acme.identity.v1.CloneUserRequest
import com.acme.identity.v1.FindUsersRequestKt
import com.acme.identity.v1.FindUsersResponse
import com.acme.identity.v1.UserQueryServiceGrpcKt
import com.acme.identity.v1.UserServiceGrpcKt
import com.acme.identity.v1.cloneUserRequest
import com.acme.identity.v1.findUsersRequest
import com.acme.identity.v1.user
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.WorkOrderWorkflowServiceGrpcKt
import com.acme.maintenance.v1.resumeWorkOrderWorkflowRequest
import com.acme.maintenance.v1.workOrder
import com.acme.notifications.v1.NotificationServiceGrpcKt
import com.acme.notifications.v1.SearchNotificationsRequestKt
import com.acme.notifications.v1.SearchNotificationsResponse
import com.acme.notifications.v1.searchNotificationsRequest
import com.acme.patientapp.v1.PatientHomeQueryServiceGrpcKt
import com.acme.patientapp.v1.SuggestPatientHomesRequestKt
import com.acme.patientapp.v1.suggestPatientHomesRequest
import com.acme.telematics.v1.TripLogSyncServiceGrpcKt
import com.acme.telematics.v1.reconcileTripLogsRequest
import com.acme.telematics.v1.tripLog
import com.acme.transport.driverapp.DriverappRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.google.protobuf.Timestamp
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

/** Manages driver homes: creates, reads, changes, and lists them. */
class DriverHomeEndpoint(
    private val store: DriverappRepository,
    private val workers: CoroutineScope,
    private val customersCustomerSegmentReportingService: CustomerSegmentReportingServiceGrpcKt.CustomerSegmentReportingServiceCoroutineStub,
    private val driverappDriverHomeNotificationService: DriverHomeNotificationServiceGrpcKt.DriverHomeNotificationServiceCoroutineStub,
    private val driverappDriverHomeReportingService: DriverHomeReportingServiceGrpcKt.DriverHomeReportingServiceCoroutineStub,
    private val driverappDriverInboxApprovalService: DriverInboxApprovalServiceGrpcKt.DriverInboxApprovalServiceCoroutineStub,
    private val driverappDriverInboxNotificationService: DriverInboxNotificationServiceGrpcKt.DriverInboxNotificationServiceCoroutineStub,
    private val driverappDriverInboxReportingService: DriverInboxReportingServiceGrpcKt.DriverInboxReportingServiceCoroutineStub,
    private val fuelFuelPolicyApprovalService: FuelPolicyApprovalServiceGrpcKt.FuelPolicyApprovalServiceCoroutineStub,
    private val identityUserQueryService: UserQueryServiceGrpcKt.UserQueryServiceCoroutineStub,
    private val identityUserService: UserServiceGrpcKt.UserServiceCoroutineStub,
    private val maintenanceWorkOrderWorkflowService: WorkOrderWorkflowServiceGrpcKt.WorkOrderWorkflowServiceCoroutineStub,
    private val notificationsNotificationService: NotificationServiceGrpcKt.NotificationServiceCoroutineStub,
    private val patientappPatientHomeQueryService: PatientHomeQueryServiceGrpcKt.PatientHomeQueryServiceCoroutineStub,
    private val telematicsTripLogSyncService: TripLogSyncServiceGrpcKt.TripLogSyncServiceCoroutineStub,
) : DriverHomeServiceGrpcKt.DriverHomeServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DriverHomeEndpoint::class.java)

    private val workerForCreateDriverHome0 = ReplyMailbox<ScheduleDriverInboxReportRequest, ScheduleDriverInboxReportResponse>(workers) { payload ->
        withTimeout(5_000) { driverappDriverInboxReportingService.scheduleDriverInboxReport(payload) }
    }

    private val clientForCreateDriverHome1 by lazy { customersCustomerSegmentReportingService }

    /** Takes a stream of driver homes and answers once at the end. */
    override suspend fun batchCreateDriverHomes(requests: Flow<BatchCreateDriverHomesRequest>): BatchCreateDriverHomesResponse {
        var answer = BatchCreateDriverHomesResponse.getDefaultInstance()
        requests.map { request -> handleBatchCreateDriverHomes(request) }.collect { answer = it }
        return answer
    }

    /** Creates a driver home. */
    override suspend fun cloneDriverHome(request: CloneDriverHomeRequest): CloneDriverHomeResponse {
        return handleCloneDriverHome(request)
    }

    /** Creates a driver home. */
    override suspend fun createDriverHome(request: CreateDriverHomeRequest): CreateDriverHomeResponse = handleCreateDriverHome(request)

    /** Returns a driver home. */
    override suspend fun getDriverHome(request: GetDriverHomeRequest): GetDriverHomeResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeService/GetDriverHome") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetDriverHome", request)
            log.atInfo().addKeyValue("operation", "GetDriverHome").log("Handling transport request")
            val outbound0 = sendDriverHomeTestNoticeRequest {
                this.context = request.context
                this.driverHomeId = request.driverHomeId
            }
            val reply0 = withTimeout(5_000) { driverappDriverHomeNotificationService.sendDriverHomeTestNotice(outbound0) }
            getDriverHomeResponse {
                this.driverHome = driverHome {
                    this.comments.addAll(listOf(driverHomeComment {
                        this.createdAt = reply0.driverHome.audit.createdAt
                    }))
                    this.driverHomeId = request.driverHomeId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Lists driver homes a page at a time. */
    override suspend fun listDriverHomes(request: ListDriverHomesRequest): ListDriverHomesResponse {
        return handleListDriverHomes(request)
    }

    /** Lists driver homes a page at a time. */
    override suspend fun searchDriverHomes(request: SearchDriverHomesRequest): SearchDriverHomesResponse = handleSearchDriverHomes(request)

    /** Changes a driver home. */
    override suspend fun updateDriverHome(request: UpdateDriverHomeRequest): UpdateDriverHomeResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeService/UpdateDriverHome") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "UpdateDriverHome", request)
            log.atInfo().addKeyValue("operation", "UpdateDriverHome").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverHome.subject).log("Recorded audit detail")
            var reply0 = FindUsersResponse.getDefaultInstance()
            request.driverHome.historyList.asFlow().map { item ->
                val outbound0 = findUsersRequest {
                    this.context = request.context
                    this.filter = FindUsersRequestKt.filter {
                        this.createdAfter = item.changedAt
                        this.ownerId = request.driverHome.driverHomeId
                    }
                }
                identityUserQueryService.findUsers(outbound0)
            }.collect { reply0 = it }
            var reply1 = SearchNotificationsResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<DriverHomeComment>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound1 = searchNotificationsRequest {
                            this.context = request.context
                            this.filter = SearchNotificationsRequestKt.filter {
                                this.createdAfter = item.createdAt
                                this.labels.putAll(reply0.result.detailsMap)
                                this.ownerId = item.commentId
                            }
                        }
                        reply1 = notificationsNotificationService.searchNotifications(outbound1)
                    }
                }
                try {
                    request.driverHome.commentsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound2 = generateDriverHomeReportRequest {}.driverappCopy {
                this.annotations.putAll(reply0.result.detailsMap)
                this.context = request.context
                this.details = GenerateDriverHomeReportRequestKt.details {
                    this.filters.putAll(reply1.result.detailsMap)
                }
                this.driverHomeId = request.driverHome.driverHomeId
            }
            val reply2 = driverappDriverHomeReportingService.generateDriverHomeReport(outbound2)
            val outbound3 = request.toCloneUserFromUpdateDriverHome3()
            val reply3 = identityUserService.cloneUser(outbound3)
            updateDriverHomeResponse {
                this.driverHome = driverHome {
                    this.assignee = userRef {
                        this.email = reply3.user.relationshipManager.email
                    }
                    this.audit = auditInfo {
                        this.createdBy = reply3.user.audit.createdBy
                    }
                    this.requester = request.driverHome.requester
                    this.status = request.driverHome.status
                }
                this.result = operationResult {
                    this.details.putAll(reply2.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleBatchCreateDriverHomes(request: BatchCreateDriverHomesRequest): BatchCreateDriverHomesResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeService/BatchCreateDriverHomes") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BatchCreateDriverHomes", request)
            log.atInfo().addKeyValue("operation", "BatchCreateDriverHomes").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.header.source).log("Recorded audit detail")
            val outbound0 = listDriverInboxSubscriptionsRequest {
                this.context = request.context
                this.filter = ListDriverInboxSubscriptionsRequestKt.filter {
                    this.createdAfter = when (request.chunkCase) {
                        BatchCreateDriverHomesRequest.ChunkCase.RECORD -> request.record.audit.updatedAt
                        else -> Timestamp.getDefaultInstance()
                    }
                }
            }
            val reply0 = driverappDriverInboxNotificationService.listDriverInboxSubscriptions(outbound0)
            val outbound1 = RequestFactory.create {
                resumeWorkOrderWorkflowRequest {
                    this.context = request.context
                    this.workOrder = workOrder {
                        this.audit = auditInfo {
                            this.updatedBy = request.record.audit.updatedBy
                        }
                        this.requester = contactInfo {
                            this.email = request.header.submittedBy.email
                            this.phone = request.record.requester.phone
                        }
                    }
                }
            }
            val reply1 = maintenanceWorkOrderWorkflowService.resumeWorkOrderWorkflow(outbound1)
            batchCreateDriverHomesResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = reply1.outcome.status
                }
            }
        }

    private suspend fun handleCloneDriverHome(request: CloneDriverHomeRequest): CloneDriverHomeResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeService/CloneDriverHome") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CloneDriverHome", request)
            log.atInfo().addKeyValue("operation", "CloneDriverHome").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverHome.driverHomeId).log("Recorded audit detail")
            val outbound0 = reconcileTripLogsRequest {
                this.context = request.context
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.etag = request.driverHome.audit.etag
                        this.updatedAt = request.driverHome.audit.updatedAt
                    }
                    this.counterparty = customerRef {
                        this.email = request.driverHome.assignee.email
                    }
                }
            }
            val reply0 = ClientRegistry.lookup("acme.telematics.v1.TripLogSyncService", TripLogSyncServiceGrpcKt.TripLogSyncServiceCoroutineStub::class.java).reconcileTripLogs(outbound0)
            val outbound1 = suggestPatientHomesRequest {
                this.context = request.context
                this.filter = SuggestPatientHomesRequestKt.filter {
                    this.region = reply0.tripLog.billingAddress.region
                }
            }
            val reply1 = patientappPatientHomeQueryService.suggestPatientHomes(WireCopies.reflective(outbound1))
            cloneDriverHomeResponse {
                this.driverHome = driverHome {
                    this.assignee = request.driverHome.assignee
                    this.audit = auditInfo {
                        this.updatedAt = reply0.tripLog.audit.updatedAt
                    }
                    this.priority = request.driverHome.priority
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleCreateDriverHome(request: CreateDriverHomeRequest): CreateDriverHomeResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeService/CreateDriverHome") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CreateDriverHome", request)
            log.atInfo().addKeyValue("operation", "CreateDriverHome").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverHome.driverHomeId).log("Recorded audit detail")
            val outbound0 = scheduleDriverInboxReportRequest {
                this.annotations.putAll(request.driverHome.customFieldsMap)
                this.context = request.context
            }
            val reply0 = workerForCreateDriverHome0.submit(outbound0)
            val outbound1 = shareCustomerSegmentReportRequest {
                this.context = request.context
                this.details = ShareCustomerSegmentReportRequestKt.details {
                    this.recipient = contactInfo {
                        this.email = request.driverHome.requester.email
                    }
                }
            }
            val reply1 = clientForCreateDriverHome1.shareCustomerSegmentReport(outbound1)
            val outbound2 = rejectFuelPolicyRequest {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.updatedAt = request.driverHome.audit.updatedAt
                    }
                }
            }
            val reply2 = flowOf(outbound2).map { value -> fuelFuelPolicyApprovalService.rejectFuelPolicy(value) }.single()
            createDriverHomeResponse {
                this.driverHome = driverHome {
                    this.audit = auditInfo {
                        this.createdBy = reply2.fuelPolicy.audit.createdBy
                        this.etag = reply1.customerSegment.audit.etag
                    }
                    this.customFields.putAll(request.driverHome.customFieldsMap)
                    this.subject = request.driverHome.subject
                }
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleListDriverHomes(request: ListDriverHomesRequest): ListDriverHomesResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeService/ListDriverHomes") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListDriverHomes", request)
            log.atInfo().addKeyValue("operation", "ListDriverHomes").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val configure0: ListPendingDriverInboxesRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.filter = ListPendingDriverInboxesRequestKt.filter {
                    this.region = request.filter.region
                }
                this.orderBy = request.orderBy
                this.page = pageRequest {
                    this.pageSize = request.page.pageSize
                }
            }
            val outbound0 = listPendingDriverInboxesRequest(configure0)
            val reply0 = driverappDriverInboxApprovalService.listPendingDriverInboxes(outbound0)
            listDriverHomesResponse {
                this.pageInfo = pageInfo {
                    this.totalSize = reply0.pageInfo.totalSize
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSearchDriverHomes(request: SearchDriverHomesRequest): SearchDriverHomesResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeService/SearchDriverHomes") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SearchDriverHomes", request)
            log.atInfo().addKeyValue("operation", "SearchDriverHomes").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            searchDriverHomesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private fun UpdateDriverHomeRequest.toCloneUserFromUpdateDriverHome3(): CloneUserRequest {
        val source = this
        return cloneUserRequest {
            this.context = source.context
            this.user = user {
                this.contact = contactInfo {
                    this.email = source.driverHome.assignee.email
                }
                this.relationshipManager = userRef {
                    this.email = source.driverHome.requester.email
                }
            }
        }
    }
}
