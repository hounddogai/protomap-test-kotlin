package com.acme.transport.driverapp.adapters

import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.ValidateAuditEventResponse
import com.acme.audit.v1.auditEvent
import com.acme.audit.v1.validateAuditEventRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.ConsentRecordSyncServiceGrpcKt
import com.acme.customers.v1.CustomerSegmentImportServiceGrpcKt
import com.acme.customers.v1.CustomerWorkflowServiceGrpcKt
import com.acme.customers.v1.PushConsentRecordRequestKt
import com.acme.customers.v1.PushConsentRecordResponse
import com.acme.customers.v1.RetryCustomerWorkflowRequest
import com.acme.customers.v1.RetryCustomerWorkflowRequestKt
import com.acme.customers.v1.ValidateCustomerSegmentImportRequest
import com.acme.customers.v1.ValidateCustomerSegmentImportResponse
import com.acme.customers.v1.customer
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.customerSegment
import com.acme.customers.v1.pushConsentRecordRequest
import com.acme.customers.v1.retryCustomerWorkflowRequest
import com.acme.customers.v1.validateCustomerSegmentImportRequest
import com.acme.dispatch.v1.DispatchAssignmentApprovalServiceGrpcKt
import com.acme.dispatch.v1.SubmitDispatchAssignmentRequest
import com.acme.dispatch.v1.dispatchAssignment
import com.acme.dispatch.v1.dispatchAssignmentComment
import com.acme.dispatch.v1.submitDispatchAssignmentRequest
import com.acme.driverapp.v1.ApproveDriverHomeRequestKt
import com.acme.driverapp.v1.ApproveDriverInboxRequest
import com.acme.driverapp.v1.ApproveDriverInboxResponse
import com.acme.driverapp.v1.ApproveDriverInboxResponseKt
import com.acme.driverapp.v1.CommentDriverInboxApprovalRequest
import com.acme.driverapp.v1.CommentDriverInboxApprovalResponse
import com.acme.driverapp.v1.CommentDriverInboxApprovalResponseKt
import com.acme.driverapp.v1.DelegateDriverInboxApprovalRequest
import com.acme.driverapp.v1.DelegateDriverInboxApprovalResponse
import com.acme.driverapp.v1.DriverHomeApprovalServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxApprovalServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxAttachment
import com.acme.driverapp.v1.EscalateDriverHomeRequestKt
import com.acme.driverapp.v1.EscalateDriverHomeResponse
import com.acme.driverapp.v1.EscalateDriverInboxRequest
import com.acme.driverapp.v1.EscalateDriverInboxResponse
import com.acme.driverapp.v1.EscalateDriverInboxResponseKt
import com.acme.driverapp.v1.GetDriverInboxApprovalStatusRequest
import com.acme.driverapp.v1.GetDriverInboxApprovalStatusResponse
import com.acme.driverapp.v1.ListPendingDriverHomesRequestKt
import com.acme.driverapp.v1.ListPendingDriverInboxesRequest
import com.acme.driverapp.v1.ListPendingDriverInboxesResponse
import com.acme.driverapp.v1.RecallDriverInboxRequest
import com.acme.driverapp.v1.RecallDriverInboxResponse
import com.acme.driverapp.v1.RecallDriverInboxResponseKt
import com.acme.driverapp.v1.RejectDriverHomeRequest
import com.acme.driverapp.v1.RejectDriverHomeRequestKt
import com.acme.driverapp.v1.RejectDriverHomeResponse
import com.acme.driverapp.v1.RejectDriverInboxRequest
import com.acme.driverapp.v1.RejectDriverInboxResponse
import com.acme.driverapp.v1.RejectDriverInboxResponseKt
import com.acme.driverapp.v1.SubmitDriverHomeRequestKt
import com.acme.driverapp.v1.SubmitDriverInboxRequest
import com.acme.driverapp.v1.SubmitDriverInboxResponse
import com.acme.driverapp.v1.SubmitDriverInboxResponseKt
import com.acme.driverapp.v1.approveDriverHomeRequest
import com.acme.driverapp.v1.approveDriverInboxResponse
import com.acme.driverapp.v1.commentDriverInboxApprovalResponse
import com.acme.driverapp.v1.copy as driverappCopy
import com.acme.driverapp.v1.delegateDriverHomeApprovalRequest
import com.acme.driverapp.v1.delegateDriverInboxApprovalResponse
import com.acme.driverapp.v1.driverHome
import com.acme.driverapp.v1.driverHomeChannelOrigin
import com.acme.driverapp.v1.driverInbox
import com.acme.driverapp.v1.driverInboxLine
import com.acme.driverapp.v1.escalateDriverHomeRequest
import com.acme.driverapp.v1.escalateDriverInboxResponse
import com.acme.driverapp.v1.getDriverInboxApprovalStatusResponse
import com.acme.driverapp.v1.listPendingDriverHomesRequest
import com.acme.driverapp.v1.listPendingDriverInboxesResponse
import com.acme.driverapp.v1.recallDriverInboxResponse
import com.acme.driverapp.v1.rejectDriverHomeRequest
import com.acme.driverapp.v1.rejectDriverInboxResponse
import com.acme.driverapp.v1.submitDriverHomeRequest
import com.acme.driverapp.v1.submitDriverInboxResponse
import com.acme.drivers.v1.CommentDutyStatusApprovalRequestKt
import com.acme.drivers.v1.DutyStatusApprovalServiceGrpcKt
import com.acme.drivers.v1.commentDutyStatusApprovalRequest
import com.acme.drivers.v1.driverRef
import com.acme.fanportal.v1.FanSessionQueryServiceGrpcKt
import com.acme.fanportal.v1.FindFanSessionsRequest
import com.acme.fanportal.v1.FindFanSessionsRequestKt
import com.acme.fanportal.v1.FindFanSessionsResponse
import com.acme.fanportal.v1.findFanSessionsRequest
import com.acme.fleet.v1.VehicleGroupWorkflowServiceGrpcKt
import com.acme.fleet.v1.pauseVehicleGroupWorkflowRequest
import com.acme.fleet.v1.resumeVehicleGroupWorkflowRequest
import com.acme.fleet.v1.vehicleGroup
import com.acme.identity.v1.SuggestUsersRequest
import com.acme.identity.v1.SuggestUsersRequestKt
import com.acme.identity.v1.UserQueryServiceGrpcKt
import com.acme.identity.v1.suggestUsersRequest
import com.acme.identity.v1.userRef
import com.acme.routing.v1.EscalateStopRequest
import com.acme.routing.v1.StopApprovalServiceGrpcKt
import com.acme.routing.v1.escalateStopRequest
import com.acme.routing.v1.stop
import com.acme.routing.v1.stopParticipant
import com.acme.search.v1.CheckSearchIndexEligibilityRequest
import com.acme.search.v1.CheckSearchIndexEligibilityRequestKt
import com.acme.search.v1.SearchIndexComponent
import com.acme.search.v1.SearchIndexValidationServiceGrpcKt
import com.acme.search.v1.checkSearchIndexEligibilityRequest
import com.acme.search.v1.searchIndex
import com.acme.transport.driverapp.DriverappRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.DockLifecycleServiceGrpcKt
import com.acme.yards.v1.ScheduleDockExpiryRequest
import com.acme.yards.v1.ScheduleDockExpiryResponse
import com.acme.yards.v1.dock
import com.acme.yards.v1.scheduleDockExpiryRequest
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Routes driver inboxes through approval, with escalation and delegation. */
class DriverInboxApprovalController(
    private val store: DriverappRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventValidationService: AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub,
    private val customersConsentRecordSyncService: ConsentRecordSyncServiceGrpcKt.ConsentRecordSyncServiceCoroutineStub,
    private val customersCustomerSegmentImportService: CustomerSegmentImportServiceGrpcKt.CustomerSegmentImportServiceCoroutineStub,
    private val customersCustomerWorkflowService: CustomerWorkflowServiceGrpcKt.CustomerWorkflowServiceCoroutineStub,
    private val dispatchDispatchAssignmentApprovalService: DispatchAssignmentApprovalServiceGrpcKt.DispatchAssignmentApprovalServiceCoroutineStub,
    private val driverappDriverHomeApprovalService: DriverHomeApprovalServiceGrpcKt.DriverHomeApprovalServiceCoroutineStub,
    private val driversDutyStatusApprovalService: DutyStatusApprovalServiceGrpcKt.DutyStatusApprovalServiceCoroutineStub,
    private val fanportalFanSessionQueryService: FanSessionQueryServiceGrpcKt.FanSessionQueryServiceCoroutineStub,
    private val fleetVehicleGroupWorkflowService: VehicleGroupWorkflowServiceGrpcKt.VehicleGroupWorkflowServiceCoroutineStub,
    private val identityUserQueryService: UserQueryServiceGrpcKt.UserQueryServiceCoroutineStub,
    private val routingStopApprovalService: StopApprovalServiceGrpcKt.StopApprovalServiceCoroutineStub,
    private val searchSearchIndexValidationService: SearchIndexValidationServiceGrpcKt.SearchIndexValidationServiceCoroutineStub,
    private val yardsDockLifecycleService: DockLifecycleServiceGrpcKt.DockLifecycleServiceCoroutineStub,
) : DriverInboxApprovalServiceGrpcKt.DriverInboxApprovalServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DriverInboxApprovalController::class.java)

    private val invokeCommentDriverInboxApproval2: suspend (ValidateCustomerSegmentImportRequest) -> ValidateCustomerSegmentImportResponse = { payload ->
        customersCustomerSegmentImportService.validateCustomerSegmentImport(payload)
    }

    private val workerForEscalateDriverInbox1 = ReplyMailbox<ScheduleDockExpiryRequest, ScheduleDockExpiryResponse>(workers) { payload ->
        withTimeout(5_000) { yardsDockLifecycleService.scheduleDockExpiry(payload) }
    }

    private val clientForGetDriverInboxApprovalStatus0 by lazy { driverappDriverHomeApprovalService }

    /** Approves a driver inbox. */
    override suspend fun approveDriverInbox(request: ApproveDriverInboxRequest): ApproveDriverInboxResponse = handleApproveDriverInbox(request)

    /** Comments a driver inbox. */
    override suspend fun commentDriverInboxApproval(request: CommentDriverInboxApprovalRequest): CommentDriverInboxApprovalResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxApprovalService/CommentDriverInboxApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CommentDriverInboxApproval", request)
            log.atInfo().addKeyValue("operation", "CommentDriverInboxApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverInbox.driverInboxId).log("Recorded audit detail")
            val outbound0 = retryCustomerWorkflowRequest {
                this.context = request.context
                this.customer = customer {
                    this.audit = auditInfo {
                        this.updatedBy = request.driverInbox.audit.updatedBy
                    }
                    this.primaryAddress = postalAddress {
                        this.countryCode = request.driverInbox.billingAddress.countryCode
                    }
                }
                this.details = RetryCustomerWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.email = request.details.approver.email
                    }
                }
            }
            val command0: Submission<RetryCustomerWorkflowRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = customersCustomerWorkflowService.retryCustomerWorkflow(payload0)
            val outbound1 = rejectDriverHomeRequest {
                this.context = request.context
                this.details = RejectDriverHomeRequestKt.details {
                    this.approver = userRef {
                        this.displayName = request.details.approver.displayName
                    }
                }
                this.driverHome = driverHome {
                    this.assignee = userRef {
                        this.userId = reply0.customer.relationshipManager.userId
                    }
                }
            }
            val adapter1 = OutboundPort<RejectDriverHomeRequest, RejectDriverHomeResponse> { payload ->
                driverappDriverHomeApprovalService.rejectDriverHome(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            val outbound2 = validateCustomerSegmentImportRequest {
                this.context = request.context
                this.customerSegment = customerSegment {
                    this.audit = auditInfo {
                        this.createdBy = request.driverInbox.audit.createdBy
                    }
                    this.owner = userRef {
                        this.userId = reply0.customer.relationshipManager.userId
                    }
                }
            }
            val reply2 = invokeCommentDriverInboxApproval2(outbound2)
            commentDriverInboxApprovalResponse {
                this.driverInbox = driverInbox {
                    this.billingAddress = postalAddress {
                        this.locality = reply0.customer.primaryAddress.locality
                    }
                    this.counterparty = customerRef {
                        this.displayName = reply0.customer.relationshipManager.displayName
                        this.email = reply1.driverHome.assignee.email
                    }
                    this.driverInboxId = request.driverInboxId
                }
                this.outcome = CommentDriverInboxApprovalResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.status = reply2.result.status
                }
            }
        }

    /** Delegates a driver inbox. */
    override suspend fun delegateDriverInboxApproval(request: DelegateDriverInboxApprovalRequest): DelegateDriverInboxApprovalResponse {
        return handleDelegateDriverInboxApproval(request)
    }

    /** Escalates a driver inbox. */
    override suspend fun escalateDriverInbox(request: EscalateDriverInboxRequest): EscalateDriverInboxResponse = handleEscalateDriverInbox(request)

    /** Returns a driver inbox. */
    override suspend fun getDriverInboxApprovalStatus(request: GetDriverInboxApprovalStatusRequest): GetDriverInboxApprovalStatusResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxApprovalService/GetDriverInboxApprovalStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetDriverInboxApprovalStatus", request)
            log.atInfo().addKeyValue("operation", "GetDriverInboxApprovalStatus").log("Handling transport request")
            val outbound0 = submitDriverHomeRequest {
                this.context = request.context
                this.details = SubmitDriverHomeRequestKt.details {
                    this.approver = userRef {
                        this.userId = request.context.actor.userId
                    }
                }
                this.driverHome = driverHome {
                    this.audit = auditInfo {
                        this.updatedAt = request.context.deadline
                    }
                    this.requester = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = clientForGetDriverInboxApprovalStatus0.submitDriverHome(outbound0)
            getDriverInboxApprovalStatusResponse {
                this.driverInbox = driverInbox {
                    this.audit = auditInfo {
                        this.createdAt = reply0.driverHome.audit.createdAt
                    }
                    this.counterparty = customerRef {
                        this.customerId = reply0.driverHome.customer.customerId
                    }
                    this.driverInboxId = request.driverInboxId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Lists driver inboxes a page at a time. */
    override suspend fun listPendingDriverInboxes(request: ListPendingDriverInboxesRequest): ListPendingDriverInboxesResponse {
        return handleListPendingDriverInboxes(request)
    }

    /** Recalls a driver inbox. */
    override suspend fun recallDriverInbox(request: RecallDriverInboxRequest): RecallDriverInboxResponse = handleRecallDriverInbox(request)

    /** Rejects a driver inbox. */
    override suspend fun rejectDriverInbox(request: RejectDriverInboxRequest): RejectDriverInboxResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxApprovalService/RejectDriverInbox") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RejectDriverInbox", request)
            log.atInfo().addKeyValue("operation", "RejectDriverInbox").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverInbox.driverInboxId).log("Recorded audit detail")
            val outbound0 = commentDutyStatusApprovalRequest {
                this.context = request.context
                this.details = CommentDutyStatusApprovalRequestKt.details {
                    this.approver = userRef {
                        this.displayName = request.driverInbox.counterparty.displayName
                    }
                }
            }
            val reply0 = driversDutyStatusApprovalService.commentDutyStatusApproval(outbound0)
            val outbound1 = escalateDriverHomeRequest {}.driverappCopy {
                this.context = request.context
                this.driverHome = driverHome {
                    this.requester = contactInfo {
                        this.email = request.driverInbox.counterparty.email
                    }
                }
            }
            val reply1 = driverappDriverHomeApprovalService.escalateDriverHome(outbound1)
            val outbound2 = request.toCheckSearchIndexEligibilityFromRejectDriverInbox2(reply1)
            val reply2 = searchSearchIndexValidationService.checkSearchIndexEligibility(outbound2)
            rejectDriverInboxResponse {
                this.driverInbox = driverInbox {
                    this.billingAddress = postalAddress {
                        this.postalCode = reply2.searchIndex.address.postalCode
                    }
                    this.driverInboxId = request.driverInboxId
                    this.lines.addAll(listOf(driverInboxLine {
                        this.quantity = (reply2.searchIndex.componentsMap["primary"] ?: SearchIndexComponent.getDefaultInstance()).quantity
                    }))
                }
                this.outcome = RejectDriverInboxResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = reply1.result.status
                }
            }
        }

    /** Submits a driver inbox. */
    override suspend fun submitDriverInbox(request: SubmitDriverInboxRequest): SubmitDriverInboxResponse {
        return handleSubmitDriverInbox(request)
    }

    private data class ApproveDriverInbox2Command(
        val context: RequestContext,
        val stopParticipantsContactEmail: String
    ) {
        fun toRequest(): EscalateStopRequest {
            val command = this
            return escalateStopRequest {
                this.context = command.context
                this.stop = stop {
                    this.participants.addAll(listOf(stopParticipant {
                        this.contact = contactInfo {
                            this.email = command.stopParticipantsContactEmail
                        }
                    }))
                }
            }
        }
    }

    private suspend fun handleApproveDriverInbox(request: ApproveDriverInboxRequest): ApproveDriverInboxResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxApprovalService/ApproveDriverInbox") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ApproveDriverInbox", request)
            log.atInfo().addKeyValue("operation", "ApproveDriverInbox").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverInbox.driverInboxId).log("Recorded audit detail")
            val outbound0 = submitDispatchAssignmentRequest {
                this.context = request.context
                this.dispatchAssignment = dispatchAssignment {
                    this.comments.addAll(listOf(dispatchAssignmentComment {
                        this.createdAt = request.driverInbox.audit.createdAt
                    }))
                    this.customer = driverRef {
                        this.email = request.driverInbox.counterparty.email
                    }
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = dispatchDispatchAssignmentApprovalService.submitDispatchAssignment(envelope0.unpack(SubmitDispatchAssignmentRequest::class.java))
            val outbound1 = escalateDriverHomeRequest {
                this.context = request.context
                this.driverHome = driverHome {
                    this.assignee = userRef {
                        this.email = request.details.approver.email
                    }
                    this.requester = contactInfo {
                        this.email = request.driverInbox.counterparty.email
                    }
                }
            }
            val reply1 = driverappDriverHomeApprovalService.escalateDriverHome(WireCopies.json(outbound1))
            val outbound2 = ApproveDriverInbox2Command(
                context = request.context,
                stopParticipantsContactEmail = reply0.dispatchAssignment.customer.email
            ).toRequest()
            val reply2 = routingStopApprovalService.escalateStop(outbound2)
            approveDriverInboxResponse {
                this.driverInbox = driverInbox {
                    this.counterparty = customerRef {
                        this.customerId = reply1.driverHome.customer.customerId
                        this.email = reply2.stop.organizer.email
                    }
                    this.driverInboxId = request.driverInboxId
                    this.dueAt = reply0.dispatchAssignment.sla.dueAt
                    this.number = request.driverInbox.number
                }
                this.outcome = ApproveDriverInboxResponseKt.outcome {
                    this.nextSteps.addAll(reply0.outcome.nextStepsList)
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleDelegateDriverInboxApproval(request: DelegateDriverInboxApprovalRequest): DelegateDriverInboxApprovalResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxApprovalService/DelegateDriverInboxApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DelegateDriverInboxApproval", request)
            log.atInfo().addKeyValue("operation", "DelegateDriverInboxApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverInbox.driverInboxId).log("Recorded audit detail")
            val outbound0 = findFanSessionsRequest {
                this.context = request.context
                this.filter = FindFanSessionsRequestKt.filter {
                    this.region = request.driverInbox.billingAddress.region
                }
            }
            val invoke0: suspend (FindFanSessionsRequest, Metadata) -> FindFanSessionsResponse = fanportalFanSessionQueryService::findFanSessions
            val reply0 = invoke0(outbound0, Metadata())
            var reply1 = ValidateAuditEventResponse.getDefaultInstance()
            request.driverInbox.linesList.asFlow().map { item ->
                val outbound1 = validateAuditEventRequest {
                    this.auditEvent = auditEvent {
                        this.attributes.putAll(item.attributesMap)
                    }
                    this.context = request.context
                }
                auditAuditEventValidationService.validateAuditEvent(outbound1)
            }.collect { reply1 = it }
            val outbound2 = RequestFactory.create {
                resumeVehicleGroupWorkflowRequest {
                    this.context = request.context
                    this.vehicleGroup = vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.children.addAll(listOf(vehicleGroup {
                                this.audit = auditInfo {
                                    this.updatedAt = request.driverInbox.audit.updatedAt
                                }
                                this.location = postalAddress {
                                    this.locality = request.driverInbox.billingAddress.locality
                                }
                            }))
                        }))
                    }
                }
            }
            val reply2 = fleetVehicleGroupWorkflowService.resumeVehicleGroupWorkflow(outbound2)
            val outbound3 = escalateStopRequest {
                this.context = request.context
                this.stop = stop {
                    this.organizer = userRef {
                        this.displayName = reply2.vehicleGroup.manager.displayName
                    }
                    this.participants.addAll(listOf(stopParticipant {
                        this.contact = contactInfo {
                            this.email = request.details.approver.email
                        }
                    }))
                }
            }
            val reply3 = ClientRegistry.lookup("acme.routing.v1.StopApprovalService", StopApprovalServiceGrpcKt.StopApprovalServiceCoroutineStub::class.java).escalateStop(outbound3)
            delegateDriverInboxApprovalResponse {
                this.driverInbox = driverInbox {
                    this.billingAddress = postalAddress {
                        this.countryCode = reply3.stop.location.countryCode
                    }
                    this.counterparty = customerRef {
                        this.displayName = reply3.stop.organizer.displayName
                    }
                    this.driverInboxId = request.driverInboxId
                    this.number = request.driverInbox.number
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.message = reply0.result.message
                    this.status = reply2.outcome.status
                }
            }
        }

    private suspend fun handleEscalateDriverInbox(request: EscalateDriverInboxRequest): EscalateDriverInboxResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxApprovalService/EscalateDriverInbox") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EscalateDriverInbox", request)
            log.atInfo().addKeyValue("operation", "EscalateDriverInbox").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverInbox.driverInboxId).log("Recorded audit detail")
            val outbound0 = delegateDriverHomeApprovalRequest {
                this.context = request.context
                this.driverHome = driverHome {
                    this.assignee = userRef {
                        this.displayName = request.details.approver.displayName
                    }
                    this.audit = auditInfo {
                        this.createdAt = request.driverInbox.audit.createdAt
                        this.createdBy = request.driverInbox.audit.createdBy
                    }
                }
            }
            val reply0 = driverappDriverHomeApprovalService.delegateDriverHomeApproval(WireCopies.reflective(outbound0))
            val outbound1 = scheduleDockExpiryRequest {
                this.context = request.context
                this.dock = dock {
                    this.address = postalAddress {
                        this.region = request.driverInbox.billingAddress.region
                    }
                }
            }
            val reply1 = workerForEscalateDriverInbox1.submit(outbound1)
            escalateDriverInboxResponse {
                this.driverInbox = driverInbox {
                    this.billingAddress = postalAddress {
                        this.recipient = reply1.dock.address.recipient
                    }
                    this.driverInboxId = request.driverInboxId
                }
                this.outcome = EscalateDriverInboxResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.message = reply0.outcome.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleListPendingDriverInboxes(request: ListPendingDriverInboxesRequest): ListPendingDriverInboxesResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxApprovalService/ListPendingDriverInboxes") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListPendingDriverInboxes", request)
            log.atInfo().addKeyValue("operation", "ListPendingDriverInboxes").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            listPendingDriverInboxesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleRecallDriverInbox(request: RecallDriverInboxRequest): RecallDriverInboxResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxApprovalService/RecallDriverInbox") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecallDriverInbox", request)
            log.atInfo().addKeyValue("operation", "RecallDriverInbox").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverInbox.driverInboxId).log("Recorded audit detail")
            val outbound0 = approveDriverHomeRequest {
                this.context = request.context
                this.details = ApproveDriverHomeRequestKt.details {
                    this.approver = userRef {
                        this.displayName = request.driverInbox.counterparty.displayName
                    }
                }
                this.driverHome = driverHome {
                    this.audit = auditInfo {
                        this.etag = request.driverInbox.audit.etag
                    }
                    this.requester = contactInfo {
                        this.email = request.details.approver.email
                    }
                }
            }
            val reply0 = flowOf(outbound0).map { value -> driverappDriverHomeApprovalService.approveDriverHome(value) }.single()
            var reply1 = PushConsentRecordResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<DriverInboxAttachment>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound1 = pushConsentRecordRequest {
                            this.context = request.context
                            this.details = PushConsentRecordRequestKt.details {
                                this.checksum = item.checksum
                            }
                        }
                        reply1 = customersConsentRecordSyncService.pushConsentRecord(outbound1)
                    }
                }
                try {
                    request.driverInbox.attachmentsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val configure2: ListPendingDriverHomesRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.filter = ListPendingDriverHomesRequestKt.filter {
                    this.createdAfter = reply0.driverHome.sla.dueAt
                    this.labels.putAll(reply1.consentRecord.attributesMap)
                    this.ownerId = request.driverInbox.counterparty.customerId
                    this.region = request.driverInbox.billingAddress.region
                }
            }
            val outbound2 = listPendingDriverHomesRequest(configure2)
            val reply2 = driverappDriverHomeApprovalService.listPendingDriverHomes(outbound2)
            val outbound3 = escalateDriverHomeRequest {
                this.context = request.context
                this.details = EscalateDriverHomeRequestKt.details {
                    this.approver = userRef {
                        this.displayName = reply0.driverHome.assignee.displayName
                    }
                }
                this.driverHome = driverHome {
                    this.channel = driverHomeChannelOrigin {
                        this.address = reply0.driverHome.channel.address
                    }
                    this.requester = contactInfo {
                        this.email = request.driverInbox.counterparty.email
                        this.preferredChannel = reply0.driverHome.requester.preferredChannel
                    }
                }
            }
            val reply3 = driverappDriverHomeApprovalService.escalateDriverHome(outbound3)
            recallDriverInboxResponse {
                this.driverInbox = driverInbox {
                    this.audit = auditInfo {
                        this.etag = reply0.driverHome.audit.etag
                    }
                    this.driverInboxId = request.driverInboxId
                    this.status = request.driverInbox.status
                }
                this.outcome = RecallDriverInboxResponseKt.outcome {
                    this.status = reply2.result.status
                }
                this.result = operationResult {
                    this.status = reply3.result.status
                }
            }
        }

    private fun RejectDriverInboxRequest.toCheckSearchIndexEligibilityFromRejectDriverInbox2(reply1: EscalateDriverHomeResponse): CheckSearchIndexEligibilityRequest {
        val source = this
        return checkSearchIndexEligibilityRequest {
            this.context = source.context
            this.details = CheckSearchIndexEligibilityRequestKt.details {
                this.contact = contactInfo {
                    this.email = reply1.driverHome.customer.email
                    this.preferredChannel = reply1.driverHome.requester.preferredChannel
                }
            }
            this.searchIndex = searchIndex {
                this.owner = userRef {
                    this.displayName = reply1.driverHome.assignee.displayName
                }
            }
        }
    }

    private suspend fun handleSubmitDriverInbox(request: SubmitDriverInboxRequest): SubmitDriverInboxResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxApprovalService/SubmitDriverInbox") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SubmitDriverInbox", request)
            log.atInfo().addKeyValue("operation", "SubmitDriverInbox").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverInbox.driverInboxId).log("Recorded audit detail")
            val outbound0 = pauseVehicleGroupWorkflowRequest {
                this.context = request.context
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.audit = auditInfo {
                                this.updatedBy = request.driverInbox.audit.updatedBy
                            }
                            this.location = postalAddress {
                                this.postalCode = request.driverInbox.billingAddress.postalCode
                            }
                            this.manager = userRef {
                                this.displayName = request.details.approver.displayName
                            }
                        }))
                    }))
                }
            }
            val reply0 = fleetVehicleGroupWorkflowService.pauseVehicleGroupWorkflow(outbound0)
            val outbound1 = suggestUsersRequest {
                this.context = request.context
                this.filter = SuggestUsersRequestKt.filter {
                    this.region = reply0.vehicleGroup.location.region
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = identityUserQueryService.suggestUsers(envelope1.unpack(SuggestUsersRequest::class.java))
            submitDriverInboxResponse {
                this.driverInbox = driverInbox {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.vehicleGroup.audit.updatedAt
                    }
                    this.driverInboxId = request.driverInboxId
                    this.lines.addAll(listOf(driverInboxLine {
                        this.attributes.putAll(reply0.vehicleGroup.attributesMap)
                    }))
                }
                this.outcome = SubmitDriverInboxResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.status = "ok"
                }
            }
        }
}
