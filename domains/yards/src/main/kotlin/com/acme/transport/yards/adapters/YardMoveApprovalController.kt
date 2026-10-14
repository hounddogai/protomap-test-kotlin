package com.acme.transport.yards.adapters

import com.acme.authorization.v1.AccessPolicyServiceGrpcKt
import com.acme.authorization.v1.BatchGetAccessPoliciesRequest
import com.acme.authorization.v1.UpdateAccessPolicyRequestKt
import com.acme.authorization.v1.accessPolicy
import com.acme.authorization.v1.batchGetAccessPoliciesRequest
import com.acme.authorization.v1.updateAccessPolicyRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.DeliveryExceptionValidationServiceGrpcKt
import com.acme.deliveries.v1.PreviewDeliveryExceptionRequest
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.previewDeliveryExceptionRequest
import com.acme.documents.v1.DocumentServiceGrpcKt
import com.acme.documents.v1.batchGetDocumentsRequest
import com.acme.featureflags.v1.ExportFeatureFlagsRequestKt
import com.acme.featureflags.v1.FeatureFlagQueryServiceGrpcKt
import com.acme.featureflags.v1.exportFeatureFlagsRequest
import com.acme.fuel.v1.CalculateFuelPolicyRequest
import com.acme.fuel.v1.CalculateFuelPolicyRequestKt
import com.acme.fuel.v1.CalculateFuelPolicyResponse
import com.acme.fuel.v1.FindFuelTransactionsRequestKt
import com.acme.fuel.v1.FuelPolicyCalculationServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionQueryServiceGrpcKt
import com.acme.fuel.v1.calculateFuelPolicyRequest
import com.acme.fuel.v1.findFuelTransactionsRequest
import com.acme.fuel.v1.fuelPolicy
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.BrowseServiceSchedulesRequestKt
import com.acme.maintenance.v1.ServiceScheduleQueryServiceGrpcKt
import com.acme.maintenance.v1.browseServiceSchedulesRequest
import com.acme.maintenance.v1.copy as maintenanceCopy
import com.acme.search.v1.ExportSearchIndexesRequestKt
import com.acme.search.v1.SearchIndexQueryServiceGrpcKt
import com.acme.search.v1.exportSearchIndexesRequest
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.transport.yards.YardsRepository
import com.acme.yards.v1.ApproveDockRequestKt
import com.acme.yards.v1.ApproveYardMoveRequest
import com.acme.yards.v1.ApproveYardMoveResponse
import com.acme.yards.v1.CheckYardEligibilityRequest
import com.acme.yards.v1.CheckYardEligibilityRequestKt
import com.acme.yards.v1.CommentYardMoveApprovalRequest
import com.acme.yards.v1.CommentYardMoveApprovalResponse
import com.acme.yards.v1.DelegateYardMoveApprovalRequest
import com.acme.yards.v1.DelegateYardMoveApprovalResponse
import com.acme.yards.v1.DelegateYardMoveApprovalResponseKt
import com.acme.yards.v1.DockApprovalServiceGrpcKt
import com.acme.yards.v1.DockCalculationServiceGrpcKt
import com.acme.yards.v1.DockValidationServiceGrpcKt
import com.acme.yards.v1.EscalateDockRequestKt
import com.acme.yards.v1.EscalateYardRequestKt
import com.acme.yards.v1.EscalateYardResponse
import com.acme.yards.v1.ListPendingDocksRequestKt
import com.acme.yards.v1.ListPendingYardMovesRequest
import com.acme.yards.v1.ListPendingYardMovesResponse
import com.acme.yards.v1.RecallYardMoveRequest
import com.acme.yards.v1.RecallYardMoveResponse
import com.acme.yards.v1.RecallYardMoveResponseKt
import com.acme.yards.v1.RecallYardRequest
import com.acme.yards.v1.RecallYardRequestKt
import com.acme.yards.v1.RejectYardMoveRequest
import com.acme.yards.v1.RejectYardMoveResponse
import com.acme.yards.v1.SubmitYardMoveRequest
import com.acme.yards.v1.SubmitYardMoveResponse
import com.acme.yards.v1.SuspendYardMoveRequestKt
import com.acme.yards.v1.YardApprovalServiceGrpcKt
import com.acme.yards.v1.YardMoveApprovalServiceGrpcKt
import com.acme.yards.v1.YardMoveLifecycleServiceGrpcKt
import com.acme.yards.v1.YardValidationServiceGrpcKt
import com.acme.yards.v1.approveDockRequest
import com.acme.yards.v1.approveYardMoveResponse
import com.acme.yards.v1.checkYardEligibilityRequest
import com.acme.yards.v1.commentYardMoveApprovalResponse
import com.acme.yards.v1.delegateYardMoveApprovalResponse
import com.acme.yards.v1.dock
import com.acme.yards.v1.escalateDockRequest
import com.acme.yards.v1.escalateYardRequest
import com.acme.yards.v1.listPendingDocksRequest
import com.acme.yards.v1.listPendingYardMovesResponse
import com.acme.yards.v1.previewDockRequest
import com.acme.yards.v1.recallYardMoveResponse
import com.acme.yards.v1.recallYardRequest
import com.acme.yards.v1.rejectYardMoveResponse
import com.acme.yards.v1.simulateDockRequest
import com.acme.yards.v1.submitYardMoveResponse
import com.acme.yards.v1.suspendYardMoveRequest
import com.acme.yards.v1.yard
import com.acme.yards.v1.yardMove
import com.acme.yards.v1.yardMoveComment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Routes yard moves through approval, with escalation and delegation. */
class YardMoveApprovalController(
    private val store: YardsRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyService: AccessPolicyServiceGrpcKt.AccessPolicyServiceCoroutineStub,
    private val deliveriesDeliveryExceptionValidationService: DeliveryExceptionValidationServiceGrpcKt.DeliveryExceptionValidationServiceCoroutineStub,
    private val documentsDocumentService: DocumentServiceGrpcKt.DocumentServiceCoroutineStub,
    private val featureflagsFeatureFlagQueryService: FeatureFlagQueryServiceGrpcKt.FeatureFlagQueryServiceCoroutineStub,
    private val fuelFuelPolicyCalculationService: FuelPolicyCalculationServiceGrpcKt.FuelPolicyCalculationServiceCoroutineStub,
    private val fuelFuelTransactionQueryService: FuelTransactionQueryServiceGrpcKt.FuelTransactionQueryServiceCoroutineStub,
    private val maintenanceServiceScheduleQueryService: ServiceScheduleQueryServiceGrpcKt.ServiceScheduleQueryServiceCoroutineStub,
    private val searchSearchIndexQueryService: SearchIndexQueryServiceGrpcKt.SearchIndexQueryServiceCoroutineStub,
    private val yardsDockApprovalService: DockApprovalServiceGrpcKt.DockApprovalServiceCoroutineStub,
    private val yardsDockCalculationService: DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub,
    private val yardsDockValidationService: DockValidationServiceGrpcKt.DockValidationServiceCoroutineStub,
    private val yardsYardApprovalService: YardApprovalServiceGrpcKt.YardApprovalServiceCoroutineStub,
    private val yardsYardMoveLifecycleService: YardMoveLifecycleServiceGrpcKt.YardMoveLifecycleServiceCoroutineStub,
    private val yardsYardValidationService: YardValidationServiceGrpcKt.YardValidationServiceCoroutineStub,
) : YardMoveApprovalServiceGrpcKt.YardMoveApprovalServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(YardMoveApprovalController::class.java)

    private val workerForCommentYardMoveApproval2 = ReplyMailbox<CalculateFuelPolicyRequest, CalculateFuelPolicyResponse>(workers) { payload ->
        withTimeout(5_000) { fuelFuelPolicyCalculationService.calculateFuelPolicy(payload) }
    }

    /** Approves a yard move. */
    override suspend fun approveYardMove(request: ApproveYardMoveRequest): ApproveYardMoveResponse = handleApproveYardMove(request)

    /** Comments a yard move. */
    override suspend fun commentYardMoveApproval(request: CommentYardMoveApprovalRequest): CommentYardMoveApprovalResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveApprovalService/CommentYardMoveApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CommentYardMoveApproval", request)
            log.atInfo().addKeyValue("operation", "CommentYardMoveApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yardMove.yardMoveId).log("Recorded audit detail")
            val outbound0 = simulateDockRequest {
                this.context = request.context
                this.dock = dock {
                    this.owner = userRef {
                        this.userId = request.yardMove.assignee.userId
                    }
                }
            }
            val reply0 = ClientRegistry.lookup("acme.yards.v1.DockCalculationService", DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub::class.java).simulateDock(outbound0)
            val outbound1 = findFuelTransactionsRequest {
                this.context = request.context
                this.filter = FindFuelTransactionsRequestKt.filter {
                    this.createdAfter = request.yardMove.sla.dueAt
                    this.ownerId = request.details.approver.userId
                    this.region = reply0.dock.address.region
                }
            }
            val reply1 = fuelFuelTransactionQueryService.findFuelTransactions(WireCopies.reflective(outbound1))
            val outbound2 = calculateFuelPolicyRequest {
                this.context = request.context
                this.details = CalculateFuelPolicyRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = reply0.dock.address.countryCode
                    }
                }
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.etag = reply0.dock.audit.etag
                        this.updatedBy = request.yardMove.audit.updatedBy
                    }
                }
            }
            val reply2 = workerForCommentYardMoveApproval2.submit(outbound2)
            commentYardMoveApprovalResponse {
                this.result = operationResult {
                    this.message = reply2.result.message
                    this.status = reply1.result.status
                }
                this.yardMove = yardMove {
                    this.assignee = userRef {
                        this.email = reply0.dock.owner.email
                    }
                    this.comments.addAll(listOf(yardMoveComment {
                        this.createdAt = reply0.dock.audit.createdAt
                    }))
                    this.status = request.yardMove.status
                    this.yardMoveId = request.yardMoveId
                }
            }
        }

    /** Delegates a yard move. */
    override suspend fun delegateYardMoveApproval(request: DelegateYardMoveApprovalRequest): DelegateYardMoveApprovalResponse {
        return handleDelegateYardMoveApproval(request)
    }

    /** Lists yard moves a page at a time. */
    override suspend fun listPendingYardMoves(request: ListPendingYardMovesRequest): ListPendingYardMovesResponse = handleListPendingYardMoves(request)

    /** Recalls a yard move. */
    override suspend fun recallYardMove(request: RecallYardMoveRequest): RecallYardMoveResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveApprovalService/RecallYardMove") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecallYardMove", request)
            log.atInfo().addKeyValue("operation", "RecallYardMove").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yardMove.yardMoveId).log("Recorded audit detail")
            val outbound0 = batchGetDocumentsRequest {
                this.context = request.context
            }
            val reply0 = documentsDocumentService.batchGetDocuments(outbound0)
            val outbound1 = browseServiceSchedulesRequest {}.maintenanceCopy {
                this.context = request.context
                this.filter = BrowseServiceSchedulesRequestKt.filter {
                    this.createdAfter = request.yardMove.audit.createdAt
                }
            }
            val reply1 = maintenanceServiceScheduleQueryService.browseServiceSchedules(outbound1)
            val outbound2 = request.toRecallYardFromRecallYardMove2()
            val reply2 = yardsYardApprovalService.recallYard(outbound2)
            recallYardMoveResponse {
                this.outcome = RecallYardMoveResponseKt.outcome {
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
                this.yardMove = yardMove {
                    this.assignee = userRef {
                        this.displayName = reply2.yard.owner.displayName
                    }
                    this.requester = request.yardMove.requester
                    this.yardMoveId = request.yardMoveId
                }
            }
        }

    /** Rejects a yard move. */
    override suspend fun rejectYardMove(request: RejectYardMoveRequest): RejectYardMoveResponse {
        return handleRejectYardMove(request)
    }

    /** Submits a yard move. */
    override suspend fun submitYardMove(request: SubmitYardMoveRequest): SubmitYardMoveResponse = handleSubmitYardMove(request)

    private suspend fun handleApproveYardMove(request: ApproveYardMoveRequest): ApproveYardMoveResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveApprovalService/ApproveYardMove") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ApproveYardMove", request)
            log.atInfo().addKeyValue("operation", "ApproveYardMove").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yardMove.subject).log("Recorded audit detail")
            val outbound0 = PreviewDeliveryExceptionRequest.newBuilder().apply {
                setContext(request.context)
                setDeliveryException(deliveryException {
                    this.assignee = userRef {
                        this.email = request.yardMove.requester.email
                    }
                    this.requester = contactInfo {
                        this.phone = request.yardMove.requester.phone
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> deliveriesDeliveryExceptionValidationService.previewDeliveryException(value) }
            val outbound1 = RequestFactory.create {
                suspendYardMoveRequest {
                    this.context = request.context
                    this.details = SuspendYardMoveRequestKt.details {
                        this.notifyContact = contactInfo {
                            this.email = request.yardMove.assignee.email
                        }
                    }
                    this.yardMoveId = request.yardMove.yardMoveId
                }
            }
            val reply1 = yardsYardMoveLifecycleService.suspendYardMove(outbound1)
            approveYardMoveResponse {
                this.result = operationResult {
                    this.status = reply0.outcome.status
                }
                this.yardMove = yardMove {
                    this.assignee = userRef {
                        this.email = reply1.yardMove.requester.email
                    }
                    this.priority = request.yardMove.priority
                    this.yardMoveId = request.yardMoveId
                }
            }
        }

    private suspend fun handleDelegateYardMoveApproval(request: DelegateYardMoveApprovalRequest): DelegateYardMoveApprovalResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveApprovalService/DelegateYardMoveApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DelegateYardMoveApproval", request)
            log.atInfo().addKeyValue("operation", "DelegateYardMoveApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yardMove.yardMoveId).log("Recorded audit detail")
            var reply0 = EscalateYardResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.yardMove.commentsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound0 = escalateYardRequest {
                                this.context = request.context
                                this.details = EscalateYardRequestKt.details {
                                    this.approver = userRef {
                                        this.displayName = request.details.approver.displayName
                                    }
                                    this.comment = item.commentId
                                }
                                this.yard = yard {
                                    this.audit = auditInfo {
                                        this.createdAt = item.createdAt
                                    }
                                    this.owner = userRef {
                                        this.email = item.authorEmail
                                    }
                                }
                            }
                            yardsYardApprovalService.escalateYard(outbound0)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply0 = it }
                }
            }
            val outbound1 = listPendingDocksRequest {
                this.context = request.context
                this.filter = ListPendingDocksRequestKt.filter {
                    this.region = reply0.yard.address.region
                }
            }
            val reply1 = flowOf(outbound1).map { value -> yardsDockApprovalService.listPendingDocks(value) }.single()
            val outbound2 = escalateDockRequest {
                this.context = request.context
                this.details = EscalateDockRequestKt.details {
                    this.approver = userRef {
                        this.displayName = reply0.yard.owner.displayName
                        this.userId = request.details.approver.userId
                    }
                }
                this.dock = dock {
                    this.address = postalAddress {
                        this.recipient = reply0.yard.address.recipient
                    }
                }
            }
            val reply2 = withTimeout(5_000) { yardsDockApprovalService.escalateDock(outbound2) }
            delegateYardMoveApprovalResponse {
                this.outcome = DelegateYardMoveApprovalResponseKt.outcome {
                    this.message = reply2.outcome.message
                }
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = "ok"
                }
                this.yardMove = yardMove {
                    this.assignee = request.yardMove.assignee
                    this.yardMoveId = request.yardMoveId
                }
            }
        }

    private suspend fun handleListPendingYardMoves(request: ListPendingYardMovesRequest): ListPendingYardMovesResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveApprovalService/ListPendingYardMoves") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListPendingYardMoves", request)
            log.atInfo().addKeyValue("operation", "ListPendingYardMoves").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val configure0: UpdateAccessPolicyRequestKt.Dsl.() -> Unit = {
                this.accessPolicy = accessPolicy {
                    this.audit = auditInfo {
                        this.createdAt = request.context.deadline
                        this.updatedAt = request.filter.createdAfter
                    }
                    this.owner = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                }
                this.context = request.context
            }
            val outbound0 = updateAccessPolicyRequest(configure0)
            val reply0 = authorizationAccessPolicyService.updateAccessPolicy(outbound0)
            val outbound1 = previewDockRequest {
                this.context = request.context
                this.dock = dock {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.accessPolicy.audit.updatedBy
                    }
                }
            }
            val reply1 = yardsDockValidationService.previewDock(outbound1)
            listPendingYardMovesResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = reply1.result.status
                }
                this.yardMoves.addAll(listOf(yardMove {
                    this.assignee = userRef {
                        this.email = reply0.accessPolicy.owner.email
                    }
                    this.comments.addAll(listOf(yardMoveComment {
                        this.createdAt = reply1.dock.audit.createdAt
                    }))
                }))
            }
        }

    private fun RecallYardMoveRequest.toRecallYardFromRecallYardMove2(): RecallYardRequest {
        val source = this
        return recallYardRequest {
            this.context = source.context
            this.details = RecallYardRequestKt.details {
                this.approver = userRef {
                    this.userId = source.details.approver.userId
                }
            }
            this.yard = yard {
                this.owner = userRef {
                    this.displayName = source.yardMove.assignee.displayName
                }
            }
        }
    }

    private suspend fun handleRejectYardMove(request: RejectYardMoveRequest): RejectYardMoveResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveApprovalService/RejectYardMove") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RejectYardMove", request)
            log.atInfo().addKeyValue("operation", "RejectYardMove").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yardMove.yardMoveId).log("Recorded audit detail")
            val outbound0 = approveDockRequest {
                this.context = request.context
                this.details = ApproveDockRequestKt.details {
                    this.approver = userRef {
                        this.email = request.yardMove.assignee.email
                    }
                }
                this.dock = dock {
                    this.audit = auditInfo {
                        this.updatedBy = request.yardMove.audit.updatedBy
                    }
                    this.owner = userRef {
                        this.displayName = request.yardMove.assignee.displayName
                    }
                }
            }
            val reply0 = yardsDockApprovalService.approveDock(outbound0)
            val outbound1 = exportSearchIndexesRequest {
                this.context = request.context
                this.filter = ExportSearchIndexesRequestKt.filter {
                    this.createdAfter = reply0.dock.audit.updatedAt
                    this.ownerId = request.yardMove.sla.policyId
                    this.region = reply0.dock.address.region
                }
            }
            searchSearchIndexQueryService.exportSearchIndexes(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            request.yardMove.commentsList.asFlow().map { item ->
                val outbound2 = exportFeatureFlagsRequest {
                    this.context = request.context
                    this.filter = ExportFeatureFlagsRequestKt.filter {
                        this.createdAfter = item.createdAt
                        this.region = reply0.dock.address.region
                    }
                }
                featureflagsFeatureFlagQueryService.exportFeatureFlags(outbound2).collect { event ->
                    log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
                }
            }.collect()
            rejectYardMoveResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.yardMove = yardMove {
                    this.audit = auditInfo {
                        this.createdBy = reply0.dock.audit.createdBy
                    }
                    this.priority = request.yardMove.priority
                    this.yardMoveId = request.yardMoveId
                }
            }
        }

    private data class SubmitYardMove0Command(
        val context: RequestContext,
        val yardAuditEtag: String,
        val yardOwnerEmail: String,
        val yardOwnerDisplayName: String,
        val detailsContactPhone: String
    ) {
        fun toRequest(): CheckYardEligibilityRequest {
            val command = this
            return checkYardEligibilityRequest {
                this.context = command.context
                this.details = CheckYardEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.phone = command.detailsContactPhone
                    }
                }
                this.yard = yard {
                    this.audit = auditInfo {
                        this.etag = command.yardAuditEtag
                    }
                    this.owner = userRef {
                        this.displayName = command.yardOwnerDisplayName
                        this.email = command.yardOwnerEmail
                    }
                }
            }
        }
    }

    private suspend fun handleSubmitYardMove(request: SubmitYardMoveRequest): SubmitYardMoveResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveApprovalService/SubmitYardMove") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SubmitYardMove", request)
            log.atInfo().addKeyValue("operation", "SubmitYardMove").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yardMove.yardMoveId).log("Recorded audit detail")
            val outbound0 = SubmitYardMove0Command(
                context = request.context,
                yardAuditEtag = request.yardMove.audit.etag,
                yardOwnerEmail = request.details.approver.email,
                yardOwnerDisplayName = request.details.approver.displayName,
                detailsContactPhone = request.yardMove.requester.phone
            ).toRequest()
            val reply0 = yardsYardValidationService.checkYardEligibility(outbound0)
            val outbound1 = batchGetAccessPoliciesRequest {
                this.context = request.context
            }
            val command1: Submission<BatchGetAccessPoliciesRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = authorizationAccessPolicyService.batchGetAccessPolicies(payload1)
            submitYardMoveResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = reply1.result.status
                }
                this.yardMove = yardMove {
                    this.subject = request.yardMove.subject
                    this.yardMoveId = request.yardMoveId
                }
            }
        }
}
