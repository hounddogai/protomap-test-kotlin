package com.acme.transport.driverapp.api

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.HouseholdWorkflowServiceGrpcKt
import com.acme.customers.v1.ListPreferenceConflictsRequest
import com.acme.customers.v1.ListPreferenceConflictsRequestKt
import com.acme.customers.v1.ListPreferenceConflictsResponse
import com.acme.customers.v1.PreferenceSyncServiceGrpcKt
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.household
import com.acme.customers.v1.listPreferenceConflictsRequest
import com.acme.customers.v1.pauseHouseholdWorkflowRequest
import com.acme.dispatch.v1.DispatchAssignmentApprovalServiceGrpcKt
import com.acme.dispatch.v1.SubmitDispatchAssignmentRequest
import com.acme.dispatch.v1.dispatchAssignment
import com.acme.dispatch.v1.submitDispatchAssignmentRequest
import com.acme.documents.v1.DocumentServiceGrpcKt
import com.acme.documents.v1.watchDocumentsRequest
import com.acme.driverapp.v1.AdvanceDriverInboxWorkflowRequest
import com.acme.driverapp.v1.AdvanceDriverInboxWorkflowRequestKt
import com.acme.driverapp.v1.DriverHomeApprovalServiceGrpcKt
import com.acme.driverapp.v1.DriverHomeReportingServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxIntegrationServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxNotificationServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxWorkflowServiceGrpcKt
import com.acme.driverapp.v1.GetDriverInboxPreferencesRequest
import com.acme.driverapp.v1.GetDriverInboxPreferencesResponse
import com.acme.driverapp.v1.InspectDriverInboxWorkflowRequest
import com.acme.driverapp.v1.InspectDriverInboxWorkflowRequestKt
import com.acme.driverapp.v1.ListDriverInboxSubscriptionsRequest
import com.acme.driverapp.v1.ListDriverInboxSubscriptionsResponse
import com.acme.driverapp.v1.NotifyDriverInboxWatchersRequest
import com.acme.driverapp.v1.NotifyDriverInboxWatchersResponse
import com.acme.driverapp.v1.ResumeDriverInboxWorkflowRequestKt
import com.acme.driverapp.v1.StreamDriverInboxNoticesRequest
import com.acme.driverapp.v1.StreamDriverInboxNoticesResponse
import com.acme.driverapp.v1.SubscribeDriverInboxUpdatesRequest
import com.acme.driverapp.v1.SubscribeDriverInboxUpdatesResponse
import com.acme.driverapp.v1.UnsubscribeDriverInboxUpdatesRequest
import com.acme.driverapp.v1.UnsubscribeDriverInboxUpdatesResponse
import com.acme.driverapp.v1.advanceDriverInboxWorkflowRequest
import com.acme.driverapp.v1.approveDriverHomeRequest
import com.acme.driverapp.v1.copy as driverappCopy
import com.acme.driverapp.v1.driverHome
import com.acme.driverapp.v1.driverInbox
import com.acme.driverapp.v1.getDriverInboxPreferencesResponse
import com.acme.driverapp.v1.inspectDriverInboxWorkflowRequest
import com.acme.driverapp.v1.listDriverInboxSubscriptionsResponse
import com.acme.driverapp.v1.notifyDriverInboxWatchersResponse
import com.acme.driverapp.v1.resumeDriverInboxWorkflowRequest
import com.acme.driverapp.v1.streamDriverHomeMetricsRequest
import com.acme.driverapp.v1.streamDriverInboxNoticesResponse
import com.acme.driverapp.v1.submitDriverHomeRequest
import com.acme.driverapp.v1.subscribeDriverInboxUpdatesResponse
import com.acme.driverapp.v1.testDriverInboxConnectionRequest
import com.acme.driverapp.v1.unsubscribeDriverInboxUpdatesResponse
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.ListPartWorkflowsRequest
import com.acme.maintenance.v1.ListPartWorkflowsRequestKt
import com.acme.maintenance.v1.PartWorkflowServiceGrpcKt
import com.acme.maintenance.v1.listPartWorkflowsRequest
import com.acme.transport.driverapp.DriverappRepository
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.google.protobuf.Any as ProtoAny
import com.google.protobuf.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Tells the people who follow a driver inbox what changed. */
class DriverInboxNotificationGrpcService(
    private val store: DriverappRepository,
    private val workers: CoroutineScope,
    private val customersHouseholdWorkflowService: HouseholdWorkflowServiceGrpcKt.HouseholdWorkflowServiceCoroutineStub,
    private val customersPreferenceSyncService: PreferenceSyncServiceGrpcKt.PreferenceSyncServiceCoroutineStub,
    private val dispatchDispatchAssignmentApprovalService: DispatchAssignmentApprovalServiceGrpcKt.DispatchAssignmentApprovalServiceCoroutineStub,
    private val documentsDocumentService: DocumentServiceGrpcKt.DocumentServiceCoroutineStub,
    private val driverappDriverHomeApprovalService: DriverHomeApprovalServiceGrpcKt.DriverHomeApprovalServiceCoroutineStub,
    private val driverappDriverHomeReportingService: DriverHomeReportingServiceGrpcKt.DriverHomeReportingServiceCoroutineStub,
    private val driverappDriverInboxIntegrationService: DriverInboxIntegrationServiceGrpcKt.DriverInboxIntegrationServiceCoroutineStub,
    private val driverappDriverInboxWorkflowService: DriverInboxWorkflowServiceGrpcKt.DriverInboxWorkflowServiceCoroutineStub,
    private val maintenancePartWorkflowService: PartWorkflowServiceGrpcKt.PartWorkflowServiceCoroutineStub,
) : DriverInboxNotificationServiceGrpcKt.DriverInboxNotificationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DriverInboxNotificationGrpcService::class.java)

    /** Returns a driver inbox. */
    override suspend fun getDriverInboxPreferences(request: GetDriverInboxPreferencesRequest): GetDriverInboxPreferencesResponse {
        return handleGetDriverInboxPreferences(request)
    }

    /** Lists driver inboxes a page at a time. */
    override suspend fun listDriverInboxSubscriptions(request: ListDriverInboxSubscriptionsRequest): ListDriverInboxSubscriptionsResponse = handleListDriverInboxSubscriptions(request)

    /** Notifys a driver inbox. */
    override suspend fun notifyDriverInboxWatchers(request: NotifyDriverInboxWatchersRequest): NotifyDriverInboxWatchersResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxNotificationService/NotifyDriverInboxWatchers") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "NotifyDriverInboxWatchers", request)
            log.atInfo().addKeyValue("operation", "NotifyDriverInboxWatchers").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.templateId).log("Recorded audit detail")
            val configure0: InspectDriverInboxWorkflowRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.driverInboxId = request.driverInboxId
            }
            val outbound0 = inspectDriverInboxWorkflowRequest(configure0)
            val reply0 = driverappDriverInboxWorkflowService.inspectDriverInboxWorkflow(outbound0)
            val outbound1 = testDriverInboxConnectionRequest {
                this.context = request.context
                this.driverInboxId = reply0.driverInbox.driverInboxId
                this.reason = request.reason
            }
            val reply1 = driverappDriverInboxIntegrationService.testDriverInboxConnection(outbound1)
            val outbound2 = streamDriverHomeMetricsRequest {
                this.context = request.context
                this.subscriber = contactInfo {
                    this.email = reply0.driverInbox.counterparty.email
                    this.phone = request.details.recipient.phone
                    this.preferredChannel = request.details.recipient.preferredChannel
                }
            }
            driverappDriverHomeReportingService.streamDriverHomeMetrics(outbound2).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            notifyDriverInboxWatchersResponse {
                this.driverInbox = driverInbox {
                    this.audit = auditInfo {
                        this.createdAt = reply1.driverInbox.audit.createdAt
                        this.updatedAt = reply0.driverInbox.audit.updatedAt
                    }
                    this.billingAddress = postalAddress {
                        this.region = reply0.driverInbox.billingAddress.region
                    }
                    this.counterparty = customerRef {
                        this.email = request.details.recipient.email
                    }
                    this.driverInboxId = request.driverInboxId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Streams the changes of driver inboxes as they happen. */
    override fun streamDriverInboxNotices(request: StreamDriverInboxNoticesRequest): Flow<StreamDriverInboxNoticesResponse> = flow {
        emit(handleStreamDriverInboxNotices(request))
    }

    /** Subscribes a driver inbox. */
    override suspend fun subscribeDriverInboxUpdates(request: SubscribeDriverInboxUpdatesRequest): SubscribeDriverInboxUpdatesResponse = handleSubscribeDriverInboxUpdates(request)

    /** Unsubscribes a driver inbox. */
    override suspend fun unsubscribeDriverInboxUpdates(request: UnsubscribeDriverInboxUpdatesRequest): UnsubscribeDriverInboxUpdatesResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxNotificationService/UnsubscribeDriverInboxUpdates") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "UnsubscribeDriverInboxUpdates", request)
            log.atInfo().addKeyValue("operation", "UnsubscribeDriverInboxUpdates").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.recipient.preferredChannel).log("Recorded audit detail")
            val outbound0 = advanceDriverInboxWorkflowRequest {
                this.context = request.context
                this.details = AdvanceDriverInboxWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.email = request.details.recipient.email
                        this.phone = request.details.recipient.phone
                    }
                }
            }
            val command0: Submission<AdvanceDriverInboxWorkflowRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = driverappDriverInboxWorkflowService.advanceDriverInboxWorkflow(payload0)
            val outbound1 = listPreferenceConflictsRequest {
                this.context = request.context
                this.filter = ListPreferenceConflictsRequestKt.filter {
                    this.createdAfter = reply0.outcome.effectiveAt
                    this.region = reply0.driverInbox.billingAddress.region
                }
            }
            val adapter1 = OutboundPort<ListPreferenceConflictsRequest, ListPreferenceConflictsResponse> { payload ->
                customersPreferenceSyncService.listPreferenceConflicts(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            unsubscribeDriverInboxUpdatesResponse {
                this.driverInbox = driverInbox {
                    this.counterparty = customerRef {
                        this.email = request.details.recipient.email
                    }
                    this.driverInboxId = request.driverInboxId
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleGetDriverInboxPreferences(request: GetDriverInboxPreferencesRequest): GetDriverInboxPreferencesResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxNotificationService/GetDriverInboxPreferences") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetDriverInboxPreferences", request)
            log.atInfo().addKeyValue("operation", "GetDriverInboxPreferences").log("Handling transport request")
            val outbound0 = watchDocumentsRequest {
                this.context = request.context
                this.subscriber = contactInfo {
                    this.email = request.context.actor.email
                }
            }
            documentsDocumentService.watchDocuments(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = pauseHouseholdWorkflowRequest {
                this.context = request.context
                this.household = household {
                    this.children.addAll(listOf(household {
                        this.children.addAll(listOf(household {
                            this.manager = userRef {
                                this.email = request.context.actor.email
                            }
                        }))
                        this.manager = userRef {
                            this.userId = request.context.actor.userId
                        }
                    }))
                }
            }
            val reply1 = withTimeout(5_000) { customersHouseholdWorkflowService.pauseHouseholdWorkflow(outbound1) }
            getDriverInboxPreferencesResponse {
                this.driverInbox = driverInbox {
                    this.audit = auditInfo {
                        this.createdAt = reply1.household.audit.createdAt
                    }
                    this.driverInboxId = request.driverInboxId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleListDriverInboxSubscriptions(request: ListDriverInboxSubscriptionsRequest): ListDriverInboxSubscriptionsResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxNotificationService/ListDriverInboxSubscriptions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListDriverInboxSubscriptions", request)
            log.atInfo().addKeyValue("operation", "ListDriverInboxSubscriptions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            listDriverInboxSubscriptionsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private fun StreamDriverInboxNoticesRequest.toListPartWorkflowsFromStreamDriverInboxNotices1(): ListPartWorkflowsRequest {
        val source = this
        return listPartWorkflowsRequest {
            this.context = source.context
            this.filter = ListPartWorkflowsRequestKt.filter {
                this.createdAfter = source.filter.createdAfter
                this.labels.putAll(source.filter.labelsMap)
                this.region = source.filter.region
            }
        }
    }

    private suspend fun handleStreamDriverInboxNotices(request: StreamDriverInboxNoticesRequest): StreamDriverInboxNoticesResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxNotificationService/StreamDriverInboxNotices") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StreamDriverInboxNotices", request)
            log.atInfo().addKeyValue("operation", "StreamDriverInboxNotices").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = resumeDriverInboxWorkflowRequest {}.driverappCopy {
                this.context = request.context
                this.details = ResumeDriverInboxWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.name = request.subscriber.name
                        this.preferredChannel = request.subscriber.preferredChannel
                    }
                }
            }
            val reply0 = driverappDriverInboxWorkflowService.resumeDriverInboxWorkflow(outbound0)
            val outbound1 = request.toListPartWorkflowsFromStreamDriverInboxNotices1()
            val reply1 = maintenancePartWorkflowService.listPartWorkflows(outbound1)
            val outbound2 = approveDriverHomeRequest {
                this.context = request.context
                this.driverHome = driverHome {
                    this.customer = customerRef {
                        this.customerId = reply0.driverInbox.counterparty.customerId
                    }
                }
            }
            val reply2 = driverappDriverHomeApprovalService.approveDriverHome(outbound2)
            streamDriverInboxNoticesResponse {
                this.driverInbox = driverInbox {
                    this.billingAddress = postalAddress {
                        this.lines.addAll(reply0.driverInbox.billingAddress.linesList)
                        this.postalCode = reply0.driverInbox.billingAddress.postalCode
                        this.region = request.filter.region
                    }
                    this.counterparty = customerRef {
                        this.email = request.subscriber.email
                    }
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.referenceIds.addAll(reply2.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private data class SubscribeDriverInboxUpdates2Command(
        val context: RequestContext,
        val dispatchAssignmentRequesterEmail: String,
        val dispatchAssignmentAuditUpdatedAt: Timestamp,
        val dispatchAssignmentAuditUpdatedBy: String
    ) {
        fun toRequest(): SubmitDispatchAssignmentRequest {
            val command = this
            return submitDispatchAssignmentRequest {
                this.context = command.context
                this.dispatchAssignment = dispatchAssignment {
                    this.audit = auditInfo {
                        this.updatedAt = command.dispatchAssignmentAuditUpdatedAt
                        this.updatedBy = command.dispatchAssignmentAuditUpdatedBy
                    }
                    this.requester = contactInfo {
                        this.email = command.dispatchAssignmentRequesterEmail
                    }
                }
            }
        }
    }

    private suspend fun handleSubscribeDriverInboxUpdates(request: SubscribeDriverInboxUpdatesRequest): SubscribeDriverInboxUpdatesResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxNotificationService/SubscribeDriverInboxUpdates") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SubscribeDriverInboxUpdates", request)
            log.atInfo().addKeyValue("operation", "SubscribeDriverInboxUpdates").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.recipient.phone).log("Recorded audit detail")
            val outbound0 = inspectDriverInboxWorkflowRequest {
                this.context = request.context
                this.driverInboxId = request.driverInboxId
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = driverappDriverInboxWorkflowService.inspectDriverInboxWorkflow(envelope0.unpack(InspectDriverInboxWorkflowRequest::class.java))
            val outbound1 = submitDriverHomeRequest {
                this.context = request.context
                this.driverHome = driverHome {
                    this.assignee = userRef {
                        this.email = reply0.driverInbox.counterparty.email
                    }
                    this.audit = auditInfo {
                        this.createdAt = reply0.driverInbox.audit.createdAt
                    }
                    this.customer = customerRef {
                        this.displayName = reply0.driverInbox.counterparty.displayName
                        this.email = request.details.recipient.email
                    }
                }
            }
            val reply1 = driverappDriverHomeApprovalService.submitDriverHome(WireCopies.json(outbound1))
            val outbound2 = SubscribeDriverInboxUpdates2Command(
                context = request.context,
                dispatchAssignmentRequesterEmail = reply1.driverHome.assignee.email,
                dispatchAssignmentAuditUpdatedAt = reply1.driverHome.audit.updatedAt,
                dispatchAssignmentAuditUpdatedBy = reply0.driverInbox.audit.updatedBy
            ).toRequest()
            val reply2 = dispatchDispatchAssignmentApprovalService.submitDispatchAssignment(outbound2)
            subscribeDriverInboxUpdatesResponse {
                this.driverInbox = driverInbox {
                    this.audit = auditInfo {
                        this.createdBy = reply2.dispatchAssignment.audit.createdBy
                    }
                    this.counterparty = customerRef {
                        this.displayName = reply0.driverInbox.counterparty.displayName
                        this.email = reply1.driverHome.customer.email
                    }
                    this.driverInboxId = request.driverInboxId
                    this.total = reply0.driverInbox.total
                }
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = "ok"
                }
            }
        }
}
