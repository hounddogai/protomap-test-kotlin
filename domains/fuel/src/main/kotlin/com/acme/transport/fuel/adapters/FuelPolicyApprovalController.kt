package com.acme.transport.fuel.adapters

import com.acme.audit.v1.AuditEventQueryServiceGrpcKt
import com.acme.audit.v1.CountAuditEventsRequestKt
import com.acme.audit.v1.FindAuditEventsRequestKt
import com.acme.audit.v1.countAuditEventsRequest
import com.acme.audit.v1.findAuditEventsRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.ConsentRecordValidationServiceGrpcKt
import com.acme.customers.v1.CustomerCalculationServiceGrpcKt
import com.acme.customers.v1.CustomerQueryServiceGrpcKt
import com.acme.customers.v1.PreviewConsentRecordRequestKt
import com.acme.customers.v1.QuoteCustomerRequest
import com.acme.customers.v1.QuoteCustomerResponse
import com.acme.customers.v1.SummarizeCustomersRequest
import com.acme.customers.v1.customer
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.previewConsentRecordRequest
import com.acme.customers.v1.quoteCustomerRequest
import com.acme.customers.v1.summarizeCustomersRequest
import com.acme.fuel.v1.ApproveFuelPolicyRequest
import com.acme.fuel.v1.ApproveFuelPolicyResponse
import com.acme.fuel.v1.ApproveFuelPolicyResponseKt
import com.acme.fuel.v1.CancelFuelPolicyImportRequest
import com.acme.fuel.v1.CancelFuelPolicyImportRequestKt
import com.acme.fuel.v1.CommentFuelPolicyApprovalRequest
import com.acme.fuel.v1.CommentFuelPolicyApprovalResponse
import com.acme.fuel.v1.CommentFuelPolicyApprovalResponseKt
import com.acme.fuel.v1.CommentFuelTransactionApprovalRequestKt
import com.acme.fuel.v1.EscalateFuelPolicyRequest
import com.acme.fuel.v1.EscalateFuelPolicyResponse
import com.acme.fuel.v1.EscalateFuelPolicyResponseKt
import com.acme.fuel.v1.FuelPolicyApprovalServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyCalculationServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyImportServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyReportingServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionApprovalServiceGrpcKt
import com.acme.fuel.v1.ListPendingFuelPoliciesRequest
import com.acme.fuel.v1.ListPendingFuelPoliciesResponse
import com.acme.fuel.v1.ListPendingFuelTransactionsRequestKt
import com.acme.fuel.v1.RecallFuelPolicyRequest
import com.acme.fuel.v1.RecallFuelPolicyResponse
import com.acme.fuel.v1.RecallFuelPolicyResponseKt
import com.acme.fuel.v1.RejectFuelPolicyRequest
import com.acme.fuel.v1.RejectFuelPolicyResponse
import com.acme.fuel.v1.SubmitFuelPolicyRequest
import com.acme.fuel.v1.SubmitFuelPolicyResponse
import com.acme.fuel.v1.SubmitFuelPolicyResponseKt
import com.acme.fuel.v1.ValidateFuelPolicyImportRequest
import com.acme.fuel.v1.approveFuelPolicyResponse
import com.acme.fuel.v1.cancelFuelPolicyImportRequest
import com.acme.fuel.v1.commentFuelPolicyApprovalResponse
import com.acme.fuel.v1.commentFuelTransactionApprovalRequest
import com.acme.fuel.v1.copy as fuelCopy
import com.acme.fuel.v1.escalateFuelPolicyResponse
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.fuelPolicyBlockAction
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.generateFuelPolicyReportRequest
import com.acme.fuel.v1.listPendingFuelPoliciesResponse
import com.acme.fuel.v1.listPendingFuelTransactionsRequest
import com.acme.fuel.v1.recalculateFuelPoliciesRequest
import com.acme.fuel.v1.recallFuelPolicyResponse
import com.acme.fuel.v1.rejectFuelPolicyResponse
import com.acme.fuel.v1.submitFuelPolicyResponse
import com.acme.fuel.v1.validateFuelPolicyImportRequest
import com.acme.identity.v1.UserValidationServiceGrpcKt
import com.acme.identity.v1.ValidateUserRequestKt
import com.acme.identity.v1.normalizeUserRequest
import com.acme.identity.v1.user
import com.acme.identity.v1.userRef
import com.acme.identity.v1.validateUserRequest
import com.acme.notifications.v1.NotificationValidationServiceGrpcKt
import com.acme.notifications.v1.lintNotificationsRequest
import com.acme.notifications.v1.notification
import com.acme.transport.fuel.FuelRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.DockLifecycleServiceGrpcKt
import com.acme.yards.v1.ListYardWorkflowsRequestKt
import com.acme.yards.v1.ScheduleDockExpiryRequest
import com.acme.yards.v1.YardWorkflowServiceGrpcKt
import com.acme.yards.v1.dock
import com.acme.yards.v1.listYardWorkflowsRequest
import com.acme.yards.v1.scheduleDockExpiryRequest
import com.google.protobuf.Any as ProtoAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Routes fuel policies through approval, with escalation and delegation. */
class FuelPolicyApprovalController(
    private val store: FuelRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventQueryService: AuditEventQueryServiceGrpcKt.AuditEventQueryServiceCoroutineStub,
    private val customersConsentRecordValidationService: ConsentRecordValidationServiceGrpcKt.ConsentRecordValidationServiceCoroutineStub,
    private val customersCustomerCalculationService: CustomerCalculationServiceGrpcKt.CustomerCalculationServiceCoroutineStub,
    private val customersCustomerQueryService: CustomerQueryServiceGrpcKt.CustomerQueryServiceCoroutineStub,
    private val fuelFuelPolicyCalculationService: FuelPolicyCalculationServiceGrpcKt.FuelPolicyCalculationServiceCoroutineStub,
    private val fuelFuelPolicyImportService: FuelPolicyImportServiceGrpcKt.FuelPolicyImportServiceCoroutineStub,
    private val fuelFuelPolicyReportingService: FuelPolicyReportingServiceGrpcKt.FuelPolicyReportingServiceCoroutineStub,
    private val fuelFuelTransactionApprovalService: FuelTransactionApprovalServiceGrpcKt.FuelTransactionApprovalServiceCoroutineStub,
    private val identityUserValidationService: UserValidationServiceGrpcKt.UserValidationServiceCoroutineStub,
    private val notificationsNotificationValidationService: NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub,
    private val yardsDockLifecycleService: DockLifecycleServiceGrpcKt.DockLifecycleServiceCoroutineStub,
    private val yardsYardWorkflowService: YardWorkflowServiceGrpcKt.YardWorkflowServiceCoroutineStub,
) : FuelPolicyApprovalServiceGrpcKt.FuelPolicyApprovalServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(FuelPolicyApprovalController::class.java)

    private val workerForApproveFuelPolicy3 = ReplyMailbox<QuoteCustomerRequest, QuoteCustomerResponse>(workers) { payload ->
        withTimeout(5_000) { customersCustomerCalculationService.quoteCustomer(payload) }
    }

    private val clientForCommentFuelPolicyApproval0 by lazy { fuelFuelPolicyReportingService }

    /** Approves a fuel policy. */
    override suspend fun approveFuelPolicy(request: ApproveFuelPolicyRequest): ApproveFuelPolicyResponse = handleApproveFuelPolicy(request)

    /** Comments a fuel policy. */
    override suspend fun commentFuelPolicyApproval(request: CommentFuelPolicyApprovalRequest): CommentFuelPolicyApprovalResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyApprovalService/CommentFuelPolicyApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CommentFuelPolicyApproval", request)
            log.atInfo().addKeyValue("operation", "CommentFuelPolicyApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicy.name).log("Recorded audit detail")
            val outbound0 = generateFuelPolicyReportRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.fuelPolicyId = request.fuelPolicy.fuelPolicyId
                this.reason = request.fuelPolicy.block.reason
            }
            val reply0 = clientForCommentFuelPolicyApproval0.generateFuelPolicyReport(outbound0)
            val outbound1 = listYardWorkflowsRequest {
                this.context = request.context
                this.filter = ListYardWorkflowsRequestKt.filter {
                    this.createdAfter = request.fuelPolicy.audit.createdAt
                    this.labels.putAll(request.fuelPolicy.parametersMap)
                    this.ownerId = request.fuelPolicy.owner.userId
                }
            }
            val reply1 = flowOf(outbound1).map { value -> yardsYardWorkflowService.listYardWorkflows(value) }.single()
            commentFuelPolicyApprovalResponse {
                this.fuelPolicy = fuelPolicy {
                    this.fuelPolicyId = request.fuelPolicyId
                    this.owner = request.fuelPolicy.owner
                }
                this.outcome = CommentFuelPolicyApprovalResponseKt.outcome {
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.referenceIds.addAll((reply0.result.referenceIdsList) + (reply1.result.referenceIdsList))
                    this.status = "ok"
                }
            }
        }

    /** Escalates a fuel policy. */
    override suspend fun escalateFuelPolicy(request: EscalateFuelPolicyRequest): EscalateFuelPolicyResponse {
        return handleEscalateFuelPolicy(request)
    }

    /** Lists fuel policies a page at a time. */
    override suspend fun listPendingFuelPolicies(request: ListPendingFuelPoliciesRequest): ListPendingFuelPoliciesResponse = handleListPendingFuelPolicies(request)

    /** Recalls a fuel policy. */
    override suspend fun recallFuelPolicy(request: RecallFuelPolicyRequest): RecallFuelPolicyResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyApprovalService/RecallFuelPolicy") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecallFuelPolicy", request)
            log.atInfo().addKeyValue("operation", "RecallFuelPolicy").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicy.fuelPolicyId).log("Recorded audit detail")
            val outbound0 = lintNotificationsRequest {
                this.context = request.context
                this.notification = notification {
                    this.assignee = userRef {
                        this.displayName = request.details.approver.displayName
                    }
                    this.audit = auditInfo {
                        this.createdBy = request.fuelPolicy.audit.createdBy
                    }
                    this.requester = contactInfo {
                        this.email = request.details.approver.email
                    }
                }
            }
            val reply0 = notificationsNotificationValidationService.lintNotifications(outbound0)
            val outbound1 = listPendingFuelTransactionsRequest {}.fuelCopy {
                this.context = request.context
                this.filter = ListPendingFuelTransactionsRequestKt.filter {
                    this.createdAfter = reply0.notification.audit.updatedAt
                    this.ownerId = reply0.notification.assignee.userId
                }
            }
            val reply1 = fuelFuelTransactionApprovalService.listPendingFuelTransactions(outbound1)
            recallFuelPolicyResponse {
                this.fuelPolicy = fuelPolicy {
                    this.fuelPolicyId = request.fuelPolicyId
                    this.version = request.fuelPolicy.version
                }
                this.outcome = RecallFuelPolicyResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
            }
        }

    /** Rejects a fuel policy. */
    override suspend fun rejectFuelPolicy(request: RejectFuelPolicyRequest): RejectFuelPolicyResponse {
        return handleRejectFuelPolicy(request)
    }

    /** Submits a fuel policy. */
    override suspend fun submitFuelPolicy(request: SubmitFuelPolicyRequest): SubmitFuelPolicyResponse = handleSubmitFuelPolicy(request)

    private suspend fun handleApproveFuelPolicy(request: ApproveFuelPolicyRequest): ApproveFuelPolicyResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyApprovalService/ApproveFuelPolicy") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ApproveFuelPolicy", request)
            log.atInfo().addKeyValue("operation", "ApproveFuelPolicy").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicy.fuelPolicyId).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                countAuditEventsRequest {
                    this.context = request.context
                    this.filter = CountAuditEventsRequestKt.filter {
                        this.ownerId = request.fuelPolicy.owner.userId
                    }
                }
            }
            val reply0 = auditAuditEventQueryService.countAuditEvents(outbound0)
            val outbound1 = previewConsentRecordRequest {
                this.context = request.context
                this.details = PreviewConsentRecordRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.fuelPolicy.owner.email
                    }
                }
            }
            val reply1 = ClientRegistry.lookup("acme.customers.v1.ConsentRecordValidationService", ConsentRecordValidationServiceGrpcKt.ConsentRecordValidationServiceCoroutineStub::class.java).previewConsentRecord(outbound1)
            val outbound2 = findAuditEventsRequest {
                this.context = request.context
                this.filter = FindAuditEventsRequestKt.filter {
                    this.createdAfter = request.details.effectiveAt
                    this.labels.putAll(request.fuelPolicy.parametersMap)
                    this.ownerId = request.details.approver.userId
                }
            }
            val reply2 = auditAuditEventQueryService.findAuditEvents(WireCopies.reflective(outbound2))
            val outbound3 = quoteCustomerRequest {
                this.context = request.context
                this.customer = customer {
                    this.relationshipManager = userRef {
                        this.email = request.fuelPolicy.owner.email
                        this.userId = request.fuelPolicy.owner.userId
                    }
                }
            }
            val reply3 = workerForApproveFuelPolicy3.submit(outbound3)
            approveFuelPolicyResponse {
                this.fuelPolicy = fuelPolicy {
                    this.fuelPolicyId = request.fuelPolicyId
                    this.owner = userRef {
                        this.email = reply3.customer.relationshipManager.email
                    }
                    this.status = request.fuelPolicy.status
                }
                this.outcome = ApproveFuelPolicyResponseKt.outcome {
                    this.referenceId = reply1.outcome.referenceId
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply2.result.message
                    this.status = reply2.result.status
                }
            }
        }

    private suspend fun handleEscalateFuelPolicy(request: EscalateFuelPolicyRequest): EscalateFuelPolicyResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyApprovalService/EscalateFuelPolicy") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EscalateFuelPolicy", request)
            log.atInfo().addKeyValue("operation", "EscalateFuelPolicy").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicy.fuelPolicyId).log("Recorded audit detail")
            val outbound0 = commentFuelTransactionApprovalRequest {
                this.context = request.context
                this.details = CommentFuelTransactionApprovalRequestKt.details {
                    this.approver = userRef {
                        this.displayName = request.details.approver.displayName
                        this.email = request.details.approver.email
                    }
                }
                this.fuelTransaction = fuelTransaction {
                    this.subject = customerRef {
                        this.email = request.fuelPolicy.owner.email
                    }
                }
            }
            val reply0 = withTimeout(5_000) { fuelFuelTransactionApprovalService.commentFuelTransactionApproval(outbound0) }
            val configure1: ValidateUserRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.user = user {
                    this.audit = auditInfo {
                        this.createdBy = request.fuelPolicy.audit.createdBy
                    }
                    this.contact = contactInfo {
                        this.email = reply0.fuelTransaction.subject.email
                    }
                    this.relationshipManager = userRef {
                        this.email = request.fuelPolicy.owner.email
                    }
                }
            }
            val outbound1 = validateUserRequest(configure1)
            val reply1 = identityUserValidationService.validateUser(outbound1)
            escalateFuelPolicyResponse {
                this.fuelPolicy = fuelPolicy {
                    this.fuelPolicyId = request.fuelPolicyId
                    this.owner = userRef {
                        this.userId = reply1.user.userId
                    }
                    this.priority = request.fuelPolicy.priority
                }
                this.outcome = EscalateFuelPolicyResponseKt.outcome {
                    this.status = reply0.outcome.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleListPendingFuelPolicies(request: ListPendingFuelPoliciesRequest): ListPendingFuelPoliciesResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyApprovalService/ListPendingFuelPolicies") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListPendingFuelPolicies", request)
            log.atInfo().addKeyValue("operation", "ListPendingFuelPolicies").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = normalizeUserRequest {
                this.context = request.context
                this.user = user {
                    this.primaryAddress = postalAddress {
                        this.region = request.filter.region
                    }
                }
            }
            val reply0 = identityUserValidationService.normalizeUser(outbound0)
            listPendingFuelPoliciesResponse {
                this.fuelPolicies.addAll(listOf(fuelPolicy {
                    this.owner = userRef {
                        this.displayName = reply0.user.relationshipManager.displayName
                    }
                }))
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private fun RejectFuelPolicyRequest.toSummarizeCustomersFromRejectFuelPolicy0(): SummarizeCustomersRequest {
        val source = this
        return summarizeCustomersRequest {
            this.annotations.putAll(source.annotationsMap)
            this.context = source.context
            this.reason = source.fuelPolicy.block.reason
        }
    }

    private suspend fun handleRejectFuelPolicy(request: RejectFuelPolicyRequest): RejectFuelPolicyResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyApprovalService/RejectFuelPolicy") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RejectFuelPolicy", request)
            log.atInfo().addKeyValue("operation", "RejectFuelPolicy").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicy.name).log("Recorded audit detail")
            val outbound0 = request.toSummarizeCustomersFromRejectFuelPolicy0()
            val reply0 = customersCustomerQueryService.summarizeCustomers(outbound0)
            val outbound1 = listYardWorkflowsRequest {
                this.context = request.context
                this.filter = ListYardWorkflowsRequestKt.filter {
                    this.createdAfter = request.fuelPolicy.audit.updatedAt
                    this.labels.putAll(reply0.summaryMap)
                    this.ownerId = request.fuelPolicy.fuelPolicyId
                }
            }
            val reply1 = yardsYardWorkflowService.listYardWorkflows(outbound1)
            rejectFuelPolicyResponse {
                this.fuelPolicy = fuelPolicy {
                    this.fuelPolicyId = request.fuelPolicyId
                    this.parameters.putAll(request.fuelPolicy.parametersMap)
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.message = reply0.result.message
                    this.status = reply1.result.status
                }
            }
        }

    private data class SubmitFuelPolicy2Command(
        val context: RequestContext,
        val fuelPolicyId: String,
        val detailsNewOwnerEmail: String
    ) {
        fun toRequest(): CancelFuelPolicyImportRequest {
            val command = this
            return cancelFuelPolicyImportRequest {
                this.context = command.context
                this.details = CancelFuelPolicyImportRequestKt.details {
                    this.newOwner = userRef {
                        this.email = command.detailsNewOwnerEmail
                    }
                }
                this.fuelPolicyId = command.fuelPolicyId
            }
        }
    }

    private suspend fun handleSubmitFuelPolicy(request: SubmitFuelPolicyRequest): SubmitFuelPolicyResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyApprovalService/SubmitFuelPolicy") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SubmitFuelPolicy", request)
            log.atInfo().addKeyValue("operation", "SubmitFuelPolicy").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicyId).log("Recorded audit detail")
            val outbound0 = scheduleDockExpiryRequest {
                this.context = request.context
                this.dock = dock {
                    this.audit = auditInfo {
                        this.updatedAt = request.fuelPolicy.audit.updatedAt
                    }
                    this.owner = userRef {
                        this.displayName = request.details.approver.displayName
                    }
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = yardsDockLifecycleService.scheduleDockExpiry(envelope0.unpack(ScheduleDockExpiryRequest::class.java))
            val outbound1 = recalculateFuelPoliciesRequest {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.block = fuelPolicyBlockAction {
                        this.notifyEmail = request.fuelPolicy.block.notifyEmail
                    }
                    this.owner = userRef {
                        this.displayName = request.details.approver.displayName
                    }
                }
            }
            val reply1 = fuelFuelPolicyCalculationService.recalculateFuelPolicies(WireCopies.json(outbound1))
            val outbound2 = SubmitFuelPolicy2Command(
                context = request.context,
                fuelPolicyId = request.fuelPolicy.fuelPolicyId,
                detailsNewOwnerEmail = request.fuelPolicy.owner.email
            ).toRequest()
            val reply2 = fuelFuelPolicyImportService.cancelFuelPolicyImport(outbound2)
            val outbound3 = validateFuelPolicyImportRequest {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.createdBy = reply0.dock.audit.createdBy
                        this.etag = reply2.fuelPolicy.audit.etag
                    }
                    this.block = fuelPolicyBlockAction {
                        this.notifyEmail = reply1.fuelPolicy.block.notifyEmail
                    }
                }
            }
            val command3: Submission<ValidateFuelPolicyImportRequest> = Submission.Live(outbound3)
            val payload3 = when (val command = command3) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply3 = fuelFuelPolicyImportService.validateFuelPolicyImport(payload3)
            submitFuelPolicyResponse {
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.etag = reply0.dock.audit.etag
                    }
                    this.fuelPolicyId = reply2.fuelPolicy.fuelPolicyId
                    this.owner = reply3.fuelPolicy.owner
                    this.parameters.putAll(request.fuelPolicy.parametersMap)
                    this.priority = request.fuelPolicy.priority
                    this.status = reply1.fuelPolicy.status
                    this.version = reply2.fuelPolicy.version
                }
                this.outcome = SubmitFuelPolicyResponseKt.outcome {
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }
}
