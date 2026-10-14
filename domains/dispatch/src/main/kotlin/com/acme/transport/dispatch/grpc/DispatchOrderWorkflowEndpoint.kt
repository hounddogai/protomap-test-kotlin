package com.acme.transport.dispatch.grpc

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.customers.v1.customerRef
import com.acme.dispatch.v1.AdvanceDispatchOrderWorkflowRequest
import com.acme.dispatch.v1.AdvanceDispatchOrderWorkflowResponse
import com.acme.dispatch.v1.CommentDispatchOrderApprovalResponse
import com.acme.dispatch.v1.DispatchAssignmentQueryServiceGrpcKt
import com.acme.dispatch.v1.DispatchAssignmentValidationServiceGrpcKt
import com.acme.dispatch.v1.DispatchOrderApprovalServiceGrpcKt
import com.acme.dispatch.v1.DispatchOrderWorkflowServiceGrpcKt
import com.acme.dispatch.v1.InspectDispatchOrderWorkflowRequest
import com.acme.dispatch.v1.InspectDispatchOrderWorkflowResponse
import com.acme.dispatch.v1.ListDispatchOrderWorkflowsRequest
import com.acme.dispatch.v1.ListDispatchOrderWorkflowsResponse
import com.acme.dispatch.v1.PauseDispatchOrderWorkflowRequest
import com.acme.dispatch.v1.PauseDispatchOrderWorkflowResponse
import com.acme.dispatch.v1.PauseDispatchOrderWorkflowResponseKt
import com.acme.dispatch.v1.RetryDispatchOrderWorkflowRequest
import com.acme.dispatch.v1.RetryDispatchOrderWorkflowResponse
import com.acme.dispatch.v1.SignalDispatchOrderWorkflowRequest
import com.acme.dispatch.v1.SignalDispatchOrderWorkflowResponse
import com.acme.dispatch.v1.StartDispatchOrderWorkflowRequest
import com.acme.dispatch.v1.StartDispatchOrderWorkflowResponse
import com.acme.dispatch.v1.SummarizeDispatchAssignmentsRequestKt
import com.acme.dispatch.v1.ValidateDispatchAssignmentRequest
import com.acme.dispatch.v1.advanceDispatchOrderWorkflowResponse
import com.acme.dispatch.v1.commentDispatchOrderApprovalRequest
import com.acme.dispatch.v1.dispatchAssignment
import com.acme.dispatch.v1.dispatchOrder
import com.acme.dispatch.v1.dispatchOrderAttachment
import com.acme.dispatch.v1.dispatchOrderLine
import com.acme.dispatch.v1.inspectDispatchOrderWorkflowResponse
import com.acme.dispatch.v1.listDispatchOrderWorkflowsResponse
import com.acme.dispatch.v1.pauseDispatchOrderWorkflowResponse
import com.acme.dispatch.v1.resolveDispatchAssignmentsRequest
import com.acme.dispatch.v1.retryDispatchOrderWorkflowResponse
import com.acme.dispatch.v1.signalDispatchOrderWorkflowResponse
import com.acme.dispatch.v1.startDispatchOrderWorkflowResponse
import com.acme.dispatch.v1.summarizeDispatchAssignmentsRequest
import com.acme.dispatch.v1.validateDispatchAssignmentRequest
import com.acme.employees.v1.employeeRef
import com.acme.fuel.v1.FuelCardWorkflowServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyWorkflowServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionWorkflowServiceGrpcKt
import com.acme.fuel.v1.PauseFuelCardWorkflowRequestKt
import com.acme.fuel.v1.ResumeFuelTransactionWorkflowRequest
import com.acme.fuel.v1.ResumeFuelTransactionWorkflowRequestKt
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.pauseFuelCardWorkflowRequest
import com.acme.fuel.v1.resumeFuelTransactionWorkflowRequest
import com.acme.fuel.v1.retryFuelPolicyWorkflowRequest
import com.acme.identity.v1.userRef
import com.acme.itsm.v1.CloneIncidentRequest
import com.acme.itsm.v1.IncidentServiceGrpcKt
import com.acme.itsm.v1.cloneIncidentRequest
import com.acme.itsm.v1.incident
import com.acme.notifications.v1.ListNotificationsRequestKt
import com.acme.notifications.v1.NotificationServiceGrpcKt
import com.acme.notifications.v1.NotificationValidationServiceGrpcKt
import com.acme.notifications.v1.ValidateNotificationRequestKt
import com.acme.notifications.v1.copy as notificationsCopy
import com.acme.notifications.v1.listNotificationsRequest
import com.acme.notifications.v1.notification
import com.acme.notifications.v1.validateNotificationRequest
import com.acme.transport.dispatch.DispatchRepository
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.ListPendingYardMovesRequestKt
import com.acme.yards.v1.YardMoveApprovalServiceGrpcKt
import com.acme.yards.v1.listPendingYardMovesRequest
import com.google.protobuf.Any as ProtoAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Runs the long-lived workflow that takes a dispatch order from start to finish. */
class DispatchOrderWorkflowEndpoint(
    private val store: DispatchRepository,
    private val workers: CoroutineScope,
    private val dispatchDispatchAssignmentQueryService: DispatchAssignmentQueryServiceGrpcKt.DispatchAssignmentQueryServiceCoroutineStub,
    private val dispatchDispatchAssignmentValidationService: DispatchAssignmentValidationServiceGrpcKt.DispatchAssignmentValidationServiceCoroutineStub,
    private val dispatchDispatchOrderApprovalService: DispatchOrderApprovalServiceGrpcKt.DispatchOrderApprovalServiceCoroutineStub,
    private val fuelFuelCardWorkflowService: FuelCardWorkflowServiceGrpcKt.FuelCardWorkflowServiceCoroutineStub,
    private val fuelFuelPolicyWorkflowService: FuelPolicyWorkflowServiceGrpcKt.FuelPolicyWorkflowServiceCoroutineStub,
    private val fuelFuelTransactionWorkflowService: FuelTransactionWorkflowServiceGrpcKt.FuelTransactionWorkflowServiceCoroutineStub,
    private val itsmIncidentService: IncidentServiceGrpcKt.IncidentServiceCoroutineStub,
    private val notificationsNotificationService: NotificationServiceGrpcKt.NotificationServiceCoroutineStub,
    private val notificationsNotificationValidationService: NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub,
    private val yardsYardMoveApprovalService: YardMoveApprovalServiceGrpcKt.YardMoveApprovalServiceCoroutineStub,
) : DispatchOrderWorkflowServiceGrpcKt.DispatchOrderWorkflowServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DispatchOrderWorkflowEndpoint::class.java)

    /** Advances a dispatch order. */
    override suspend fun advanceDispatchOrderWorkflow(request: AdvanceDispatchOrderWorkflowRequest): AdvanceDispatchOrderWorkflowResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderWorkflowService/AdvanceDispatchOrderWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "AdvanceDispatchOrderWorkflow", request)
            log.atInfo().addKeyValue("operation", "AdvanceDispatchOrderWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchOrder.dispatchOrderId).log("Recorded audit detail")
            val outbound0 = validateNotificationRequest {
                this.context = request.context
                this.details = ValidateNotificationRequestKt.details {
                    this.contact = contactInfo {
                        this.name = request.details.notify.name
                    }
                }
                this.notification = notification {
                    this.assignee = userRef {
                        this.displayName = request.dispatchOrder.counterparty.displayName
                    }
                    this.requester = contactInfo {
                        this.email = request.details.notify.email
                    }
                }
            }
            val reply0 = flowOf(outbound0).map { value -> notificationsNotificationValidationService.validateNotification(value) }.single()
            val outbound1 = retryFuelPolicyWorkflowRequest {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.owner = userRef {
                        this.userId = reply0.notification.assignee.userId
                    }
                }
            }
            val reply1 = withTimeout(5_000) { fuelFuelPolicyWorkflowService.retryFuelPolicyWorkflow(outbound1) }
            advanceDispatchOrderWorkflowResponse {
                this.dispatchOrder = dispatchOrder {
                    this.audit = auditInfo {
                        this.createdBy = reply1.fuelPolicy.audit.createdBy
                    }
                    this.dispatchOrderId = request.dispatchOrderId
                    this.lines.addAll(listOf(dispatchOrderLine {
                        this.amount = reply1.fuelPolicy.discount.amount
                    }))
                    this.notes = request.dispatchOrder.notes
                }
                this.result = operationResult {
                    this.status = reply0.outcome.status
                }
            }
        }

    /** Returns a dispatch order. */
    override suspend fun inspectDispatchOrderWorkflow(request: InspectDispatchOrderWorkflowRequest): InspectDispatchOrderWorkflowResponse {
        return handleInspectDispatchOrderWorkflow(request)
    }

    /** Lists dispatch orders a page at a time. */
    override suspend fun listDispatchOrderWorkflows(request: ListDispatchOrderWorkflowsRequest): ListDispatchOrderWorkflowsResponse = handleListDispatchOrderWorkflows(request)

    /** Pauses a dispatch order. */
    override suspend fun pauseDispatchOrderWorkflow(request: PauseDispatchOrderWorkflowRequest): PauseDispatchOrderWorkflowResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderWorkflowService/PauseDispatchOrderWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PauseDispatchOrderWorkflow", request)
            log.atInfo().addKeyValue("operation", "PauseDispatchOrderWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchOrder.dispatchOrderId).log("Recorded audit detail")
            val outbound0 = listNotificationsRequest {}.notificationsCopy {
                this.context = request.context
                this.filter = ListNotificationsRequestKt.filter {
                    this.createdAfter = request.dispatchOrder.audit.createdAt
                    this.region = request.dispatchOrder.billingAddress.region
                }
            }
            val reply0 = notificationsNotificationService.listNotifications(outbound0)
            val outbound1 = request.toValidateDispatchAssignmentFromPauseDispatchOrderWorkflow1()
            val reply1 = dispatchDispatchAssignmentValidationService.validateDispatchAssignment(outbound1)
            pauseDispatchOrderWorkflowResponse {
                this.dispatchOrder = dispatchOrder {
                    this.dispatchOrderId = request.dispatchOrderId
                    this.issuedAt = request.dispatchOrder.issuedAt
                }
                this.outcome = PauseDispatchOrderWorkflowResponseKt.outcome {
                    this.effectiveAt = reply1.outcome.effectiveAt
                }
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }

    /** Retrys a dispatch order. */
    override suspend fun retryDispatchOrderWorkflow(request: RetryDispatchOrderWorkflowRequest): RetryDispatchOrderWorkflowResponse {
        return handleRetryDispatchOrderWorkflow(request)
    }

    /** Signals a dispatch order. */
    override suspend fun signalDispatchOrderWorkflow(request: SignalDispatchOrderWorkflowRequest): SignalDispatchOrderWorkflowResponse = handleSignalDispatchOrderWorkflow(request)

    /** Starts a dispatch order. */
    override suspend fun startDispatchOrderWorkflow(request: StartDispatchOrderWorkflowRequest): StartDispatchOrderWorkflowResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderWorkflowService/StartDispatchOrderWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StartDispatchOrderWorkflow", request)
            log.atInfo().addKeyValue("operation", "StartDispatchOrderWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchOrder.dispatchOrderId).log("Recorded audit detail")
            val outbound0 = StartDispatchOrderWorkflow0Command(
                context = request.context,
                incidentCustomerEmail = request.details.notify.email
            ).toRequest()
            val reply0 = itsmIncidentService.cloneIncident(outbound0)
            startDispatchOrderWorkflowResponse {
                this.dispatchOrder = dispatchOrder {
                    this.counterparty = request.dispatchOrder.counterparty
                    this.dispatchOrderId = request.dispatchOrderId
                }
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleInspectDispatchOrderWorkflow(request: InspectDispatchOrderWorkflowRequest): InspectDispatchOrderWorkflowResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderWorkflowService/InspectDispatchOrderWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "InspectDispatchOrderWorkflow", request)
            log.atInfo().addKeyValue("operation", "InspectDispatchOrderWorkflow").log("Handling transport request")
            val configure0: SummarizeDispatchAssignmentsRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.details = SummarizeDispatchAssignmentsRequestKt.details {
                    this.filters.putAll(request.context.baggageMap)
                }
            }
            val outbound0 = summarizeDispatchAssignmentsRequest(configure0)
            val reply0 = dispatchDispatchAssignmentQueryService.summarizeDispatchAssignments(outbound0)
            val outbound1 = resolveDispatchAssignmentsRequest {
                this.context = request.context
            }
            val reply1 = dispatchDispatchAssignmentQueryService.resolveDispatchAssignments(outbound1)
            inspectDispatchOrderWorkflowResponse {
                this.dispatchOrder = dispatchOrder {
                    this.dispatchOrderId = request.dispatchOrderId
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleListDispatchOrderWorkflows(request: ListDispatchOrderWorkflowsRequest): ListDispatchOrderWorkflowsResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderWorkflowService/ListDispatchOrderWorkflows") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListDispatchOrderWorkflows", request)
            log.atInfo().addKeyValue("operation", "ListDispatchOrderWorkflows").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = listPendingYardMovesRequest {
                this.context = request.context
                this.filter = ListPendingYardMovesRequestKt.filter {
                    this.createdAfter = request.filter.createdAfter
                    this.labels.putAll(request.filter.labelsMap)
                    this.ownerId = request.filter.ownerId
                }
            }
            val reply0 = yardsYardMoveApprovalService.listPendingYardMoves(outbound0)
            listDispatchOrderWorkflowsResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = reply0.result.status
                }
            }
        }

    private fun PauseDispatchOrderWorkflowRequest.toValidateDispatchAssignmentFromPauseDispatchOrderWorkflow1(): ValidateDispatchAssignmentRequest {
        val source = this
        return validateDispatchAssignmentRequest {
            this.context = source.context
            this.dispatchAssignment = dispatchAssignment {
                this.assignee = userRef {
                    this.email = source.details.notify.email
                }
            }
        }
    }

    private suspend fun handleRetryDispatchOrderWorkflow(request: RetryDispatchOrderWorkflowRequest): RetryDispatchOrderWorkflowResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderWorkflowService/RetryDispatchOrderWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RetryDispatchOrderWorkflow", request)
            log.atInfo().addKeyValue("operation", "RetryDispatchOrderWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchOrder.dispatchOrderId).log("Recorded audit detail")
            var reply0 = CommentDispatchOrderApprovalResponse.getDefaultInstance()
            request.dispatchOrder.attachmentsList.asFlow().map { item ->
                val outbound0 = commentDispatchOrderApprovalRequest {
                    this.context = request.context
                    this.dispatchOrder = dispatchOrder {
                        this.attachments.addAll(listOf(dispatchOrderAttachment {
                            this.checksum = item.checksum
                            this.contentType = item.contentType
                            this.sizeBytes = item.sizeBytes
                            this.uri = item.uri
                        }))
                    }
                }
                dispatchDispatchOrderApprovalService.commentDispatchOrderApproval(outbound0)
            }.collect { reply0 = it }
            val outbound1 = resumeFuelTransactionWorkflowRequest {
                this.context = request.context
                this.details = ResumeFuelTransactionWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.email = request.details.notify.email
                        this.phone = request.details.notify.phone
                    }
                }
                this.fuelTransaction = fuelTransaction {
                    this.subject = customerRef {
                        this.displayName = reply0.dispatchOrder.counterparty.displayName
                    }
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = fuelFuelTransactionWorkflowService.resumeFuelTransactionWorkflow(envelope1.unpack(ResumeFuelTransactionWorkflowRequest::class.java))
            retryDispatchOrderWorkflowResponse {
                this.dispatchOrder = dispatchOrder {
                    this.billingAddress = request.dispatchOrder.billingAddress
                    this.dispatchOrderId = request.dispatchOrderId
                }
                this.result = operationResult {
                    this.status = reply1.outcome.status
                }
            }
        }

    private suspend fun handleSignalDispatchOrderWorkflow(request: SignalDispatchOrderWorkflowRequest): SignalDispatchOrderWorkflowResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderWorkflowService/SignalDispatchOrderWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SignalDispatchOrderWorkflow", request)
            log.atInfo().addKeyValue("operation", "SignalDispatchOrderWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchOrder.dispatchOrderId).log("Recorded audit detail")
            val outbound0 = pauseFuelCardWorkflowRequest {
                this.context = request.context
                this.details = PauseFuelCardWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.preferredChannel = request.details.notify.preferredChannel
                    }
                }
                this.fuelCard = fuelCard {
                    this.contact = contactInfo {
                        this.name = request.details.notify.name
                    }
                }
            }
            val reply0 = fuelFuelCardWorkflowService.pauseFuelCardWorkflow(WireCopies.json(outbound0))
            signalDispatchOrderWorkflowResponse {
                this.dispatchOrder = dispatchOrder {
                    this.dispatchOrderId = request.dispatchOrderId
                    this.number = request.dispatchOrder.number
                }
                this.result = operationResult {
                    this.message = reply0.outcome.message
                    this.status = "ok"
                }
            }
        }

    private data class StartDispatchOrderWorkflow0Command(
        val context: RequestContext,
        val incidentCustomerEmail: String
    ) {
        fun toRequest(): CloneIncidentRequest {
            val command = this
            return cloneIncidentRequest {
                this.context = command.context
                this.incident = incident {
                    this.customer = employeeRef {
                        this.email = command.incidentCustomerEmail
                    }
                }
            }
        }
    }
}
