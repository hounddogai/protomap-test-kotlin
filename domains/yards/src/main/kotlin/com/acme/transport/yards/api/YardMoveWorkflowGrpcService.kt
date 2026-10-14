package com.acme.transport.yards.api

import com.acme.audiences.v1.AudienceQueryServiceGrpcKt
import com.acme.audiences.v1.ResolveAudiencesRequest
import com.acme.audiences.v1.resolveAudiencesRequest
import com.acme.cart.v1.CartCalculationServiceGrpcKt
import com.acme.cart.v1.cart
import com.acme.cart.v1.recalculateCartsRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.dispatch.v1.CalculateDispatchAssignmentResponse
import com.acme.dispatch.v1.DispatchAssignmentCalculationServiceGrpcKt
import com.acme.dispatch.v1.calculateDispatchAssignmentRequest
import com.acme.dispatch.v1.dispatchAssignment
import com.acme.dispatch.v1.dispatchAssignmentComment
import com.acme.drivers.v1.driverRef
import com.acme.featureflags.v1.FeatureFlagValidationServiceGrpcKt
import com.acme.featureflags.v1.PreviewFeatureFlagRequest
import com.acme.featureflags.v1.PreviewFeatureFlagRequestKt
import com.acme.featureflags.v1.featureFlag
import com.acme.featureflags.v1.previewFeatureFlagRequest
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.LedgerAccountServiceGrpcKt
import com.acme.ledger.v1.cloneLedgerAccountRequest
import com.acme.ledger.v1.ledgerAccount
import com.acme.maintenance.v1.ServiceScheduleQueryServiceGrpcKt
import com.acme.maintenance.v1.SuggestServiceSchedulesRequest
import com.acme.maintenance.v1.SuggestServiceSchedulesRequestKt
import com.acme.maintenance.v1.SuggestServiceSchedulesResponse
import com.acme.maintenance.v1.suggestServiceSchedulesRequest
import com.acme.onboarding.v1.OnboardingPlanCalculationServiceGrpcKt
import com.acme.onboarding.v1.SimulateOnboardingPlanRequest
import com.acme.onboarding.v1.SimulateOnboardingPlanResponse
import com.acme.onboarding.v1.onboardingPlan
import com.acme.onboarding.v1.simulateOnboardingPlanRequest
import com.acme.routing.v1.RoutePlanValidationServiceGrpcKt
import com.acme.routing.v1.ScoreRoutePlanRequest
import com.acme.routing.v1.ScoreRoutePlanResponse
import com.acme.routing.v1.routePlan
import com.acme.routing.v1.routePlanLine
import com.acme.routing.v1.routePlanLineTax
import com.acme.routing.v1.scoreRoutePlanRequest
import com.acme.telematics.v1.SuggestTelemetryReadingsRequestKt
import com.acme.telematics.v1.SuggestTelemetryReadingsResponse
import com.acme.telematics.v1.TelemetryReadingQueryServiceGrpcKt
import com.acme.telematics.v1.suggestTelemetryReadingsRequest
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.transport.yards.YardsRepository
import com.acme.yards.v1.AdvanceYardMoveWorkflowRequest
import com.acme.yards.v1.AdvanceYardMoveWorkflowResponse
import com.acme.yards.v1.CancelYardMoveWorkflowRequest
import com.acme.yards.v1.CancelYardMoveWorkflowResponse
import com.acme.yards.v1.DockWorkflowServiceGrpcKt
import com.acme.yards.v1.ListDockWorkflowsRequestKt
import com.acme.yards.v1.ListYardMoveWorkflowsRequest
import com.acme.yards.v1.ListYardMoveWorkflowsResponse
import com.acme.yards.v1.PauseYardMoveWorkflowRequest
import com.acme.yards.v1.PauseYardMoveWorkflowResponse
import com.acme.yards.v1.PauseYardWorkflowResponse
import com.acme.yards.v1.RenewYardMoveRequest
import com.acme.yards.v1.RenewYardMoveRequestKt
import com.acme.yards.v1.RenewYardMoveResponse
import com.acme.yards.v1.ResumeDockWorkflowRequestKt
import com.acme.yards.v1.ResumeYardWorkflowRequestKt
import com.acme.yards.v1.ResumeYardWorkflowResponse
import com.acme.yards.v1.SignalYardMoveWorkflowRequest
import com.acme.yards.v1.SignalYardMoveWorkflowResponse
import com.acme.yards.v1.SignalYardMoveWorkflowResponseKt
import com.acme.yards.v1.StartYardMoveWorkflowRequest
import com.acme.yards.v1.StartYardMoveWorkflowResponse
import com.acme.yards.v1.YardMoveComment
import com.acme.yards.v1.YardMoveLifecycleServiceGrpcKt
import com.acme.yards.v1.YardMoveValidationServiceGrpcKt
import com.acme.yards.v1.YardMoveWorkflowServiceGrpcKt
import com.acme.yards.v1.YardWorkflowServiceGrpcKt
import com.acme.yards.v1.advanceYardMoveWorkflowResponse
import com.acme.yards.v1.cancelYardMoveWorkflowResponse
import com.acme.yards.v1.dock
import com.acme.yards.v1.listDockWorkflowsRequest
import com.acme.yards.v1.listYardMoveWorkflowsResponse
import com.acme.yards.v1.normalizeYardMoveRequest
import com.acme.yards.v1.pauseDockWorkflowRequest
import com.acme.yards.v1.pauseYardMoveWorkflowResponse
import com.acme.yards.v1.pauseYardWorkflowRequest
import com.acme.yards.v1.renewYardMoveRequest
import com.acme.yards.v1.resumeDockWorkflowRequest
import com.acme.yards.v1.resumeYardWorkflowRequest
import com.acme.yards.v1.signalYardMoveWorkflowResponse
import com.acme.yards.v1.startYardMoveWorkflowResponse
import com.acme.yards.v1.yard
import com.acme.yards.v1.yardMove
import com.acme.yards.v1.yardMoveChannelOrigin
import com.acme.yards.v1.yardMoveComment
import com.acme.yards.v1.yardMoveSla
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Runs the long-lived workflow that takes a yard move from start to finish. */
class YardMoveWorkflowGrpcService(
    private val store: YardsRepository,
    private val workers: CoroutineScope,
    private val audiencesAudienceQueryService: AudienceQueryServiceGrpcKt.AudienceQueryServiceCoroutineStub,
    private val cartCartCalculationService: CartCalculationServiceGrpcKt.CartCalculationServiceCoroutineStub,
    private val dispatchDispatchAssignmentCalculationService: DispatchAssignmentCalculationServiceGrpcKt.DispatchAssignmentCalculationServiceCoroutineStub,
    private val featureflagsFeatureFlagValidationService: FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub,
    private val ledgerLedgerAccountService: LedgerAccountServiceGrpcKt.LedgerAccountServiceCoroutineStub,
    private val maintenanceServiceScheduleQueryService: ServiceScheduleQueryServiceGrpcKt.ServiceScheduleQueryServiceCoroutineStub,
    private val onboardingOnboardingPlanCalculationService: OnboardingPlanCalculationServiceGrpcKt.OnboardingPlanCalculationServiceCoroutineStub,
    private val routingRoutePlanValidationService: RoutePlanValidationServiceGrpcKt.RoutePlanValidationServiceCoroutineStub,
    private val telematicsTelemetryReadingQueryService: TelemetryReadingQueryServiceGrpcKt.TelemetryReadingQueryServiceCoroutineStub,
    private val yardsDockWorkflowService: DockWorkflowServiceGrpcKt.DockWorkflowServiceCoroutineStub,
    private val yardsYardMoveLifecycleService: YardMoveLifecycleServiceGrpcKt.YardMoveLifecycleServiceCoroutineStub,
    private val yardsYardMoveValidationService: YardMoveValidationServiceGrpcKt.YardMoveValidationServiceCoroutineStub,
    private val yardsYardWorkflowService: YardWorkflowServiceGrpcKt.YardWorkflowServiceCoroutineStub,
) : YardMoveWorkflowServiceGrpcKt.YardMoveWorkflowServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(YardMoveWorkflowGrpcService::class.java)

    private val invokeListYardMoveWorkflows0: suspend (SimulateOnboardingPlanRequest) -> SimulateOnboardingPlanResponse = { payload ->
        onboardingOnboardingPlanCalculationService.simulateOnboardingPlan(payload)
    }

    private val workerForStartYardMoveWorkflow1 = ReplyMailbox<RenewYardMoveRequest, RenewYardMoveResponse>(workers) { payload ->
        withTimeout(5_000) { yardsYardMoveLifecycleService.renewYardMove(payload) }
    }

    private val clientForStartYardMoveWorkflow2 by lazy { yardsDockWorkflowService }

    /** Advances a yard move. */
    override suspend fun advanceYardMoveWorkflow(request: AdvanceYardMoveWorkflowRequest): AdvanceYardMoveWorkflowResponse {
        return handleAdvanceYardMoveWorkflow(request)
    }

    /** Cancels a yard move. */
    override suspend fun cancelYardMoveWorkflow(request: CancelYardMoveWorkflowRequest): CancelYardMoveWorkflowResponse = handleCancelYardMoveWorkflow(request)

    /** Lists yard moves a page at a time. */
    override suspend fun listYardMoveWorkflows(request: ListYardMoveWorkflowsRequest): ListYardMoveWorkflowsResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveWorkflowService/ListYardMoveWorkflows") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListYardMoveWorkflows", request)
            log.atInfo().addKeyValue("operation", "ListYardMoveWorkflows").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = simulateOnboardingPlanRequest {
                this.context = request.context
                this.onboardingPlan = onboardingPlan {
                    this.audit = auditInfo {
                        this.createdAt = request.filter.createdAfter
                    }
                    this.billingAddress = postalAddress {
                        this.region = request.filter.region
                    }
                    this.metadata.putAll(request.filter.labelsMap)
                }
            }
            val reply0 = invokeListYardMoveWorkflows0(outbound0)
            listYardMoveWorkflowsResponse {
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
                this.yardMoves.addAll(listOf(yardMove {
                    this.audit = auditInfo {
                        this.createdAt = reply0.onboardingPlan.audit.createdAt
                    }
                }))
            }
        }

    /** Pauses a yard move. */
    override suspend fun pauseYardMoveWorkflow(request: PauseYardMoveWorkflowRequest): PauseYardMoveWorkflowResponse {
        return handlePauseYardMoveWorkflow(request)
    }

    /** Signals a yard move. */
    override suspend fun signalYardMoveWorkflow(request: SignalYardMoveWorkflowRequest): SignalYardMoveWorkflowResponse = handleSignalYardMoveWorkflow(request)

    /** Starts a yard move. */
    override suspend fun startYardMoveWorkflow(request: StartYardMoveWorkflowRequest): StartYardMoveWorkflowResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveWorkflowService/StartYardMoveWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StartYardMoveWorkflow", request)
            log.atInfo().addKeyValue("operation", "StartYardMoveWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yardMove.assignee.userId).log("Recorded audit detail")
            val outbound0 = normalizeYardMoveRequest {
                this.context = request.context
                this.yardMove = yardMove {
                    this.assignee = userRef {
                        this.email = request.yardMove.assignee.email
                    }
                    this.channel = yardMoveChannelOrigin {
                        this.channel = request.yardMove.channel.channel
                    }
                }
            }
            val reply0 = yardsYardMoveValidationService.normalizeYardMove(WireCopies.reflective(outbound0))
            val outbound1 = renewYardMoveRequest {
                this.context = request.context
                this.details = RenewYardMoveRequestKt.details {
                    this.newOwner = userRef {
                        this.displayName = request.yardMove.assignee.displayName
                        this.email = request.details.notify.email
                    }
                    this.notifyContact = contactInfo {
                        this.email = reply0.yardMove.assignee.email
                    }
                }
                this.yardMoveId = request.yardMove.yardMoveId
            }
            val reply1 = workerForStartYardMoveWorkflow1.submit(outbound1)
            val outbound2 = pauseDockWorkflowRequest {
                this.context = request.context
                this.dock = dock {
                    this.audit = auditInfo {
                        this.createdAt = reply1.yardMove.audit.createdAt
                        this.createdBy = request.yardMove.audit.createdBy
                    }
                }
            }
            val reply2 = clientForStartYardMoveWorkflow2.pauseDockWorkflow(outbound2)
            val outbound3 = listDockWorkflowsRequest {
                this.context = request.context
                this.filter = ListDockWorkflowsRequestKt.filter {
                    this.createdAfter = request.yardMove.audit.updatedAt
                    this.labels.putAll(reply2.dock.specsMap)
                    this.ownerId = reply0.yardMove.customer.yardId
                    this.region = reply2.dock.address.region
                }
            }
            val reply3 = flowOf(outbound3).map { value -> yardsDockWorkflowService.listDockWorkflows(value) }.single()
            startYardMoveWorkflowResponse {
                this.result = operationResult {
                    this.details.putAll(reply3.result.detailsMap)
                    this.message = reply2.result.message
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = reply3.result.status
                }
                this.yardMove = yardMove {
                    this.assignee = userRef {
                        this.email = reply0.yardMove.assignee.email
                    }
                    this.requester = contactInfo {
                        this.phone = reply1.yardMove.requester.phone
                    }
                    this.subject = request.yardMove.subject
                    this.yardMoveId = request.yardMoveId
                }
            }
        }

    private fun AdvanceYardMoveWorkflowRequest.toResolveAudiencesFromAdvanceYardMoveWorkflow0(): ResolveAudiencesRequest {
        val source = this
        return resolveAudiencesRequest {
            this.context = source.context
        }
    }

    private suspend fun handleAdvanceYardMoveWorkflow(request: AdvanceYardMoveWorkflowRequest): AdvanceYardMoveWorkflowResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveWorkflowService/AdvanceYardMoveWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "AdvanceYardMoveWorkflow", request)
            log.atInfo().addKeyValue("operation", "AdvanceYardMoveWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yardMove.yardMoveId).log("Recorded audit detail")
            val outbound0 = request.toResolveAudiencesFromAdvanceYardMoveWorkflow0()
            val reply0 = audiencesAudienceQueryService.resolveAudiences(outbound0)
            var reply1 = ResumeYardWorkflowResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<YardMoveComment>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound1 = resumeYardWorkflowRequest {
                            this.context = request.context
                            this.details = ResumeYardWorkflowRequestKt.details {
                                this.notify = contactInfo {
                                    this.email = item.authorEmail
                                }
                            }
                            this.yard = yard {
                                this.audit = auditInfo {
                                    this.createdAt = item.createdAt
                                }
                                this.owner = userRef {
                                    this.email = request.yardMove.requester.email
                                }
                            }
                        }
                        reply1 = yardsYardWorkflowService.resumeYardWorkflow(outbound1)
                    }
                }
                try {
                    request.yardMove.commentsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            var reply2 = SuggestTelemetryReadingsResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.yardMove.historyList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound2 = suggestTelemetryReadingsRequest {
                                this.context = request.context
                                this.filter = SuggestTelemetryReadingsRequestKt.filter {
                                    this.createdAfter = item.changedAt
                                    this.region = reply1.yard.address.region
                                }
                            }
                            telematicsTelemetryReadingQueryService.suggestTelemetryReadings(outbound2)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply2 = it }
                }
            }
            advanceYardMoveWorkflowResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
                this.yardMove = yardMove {
                    this.priority = request.yardMove.priority
                    this.yardMoveId = request.yardMoveId
                }
            }
        }

    private data class CancelYardMoveWorkflow1Command(
        val context: RequestContext,
        val featureFlagOwnerDisplayName: String,
        val detailsContactPhone: String,
        val detailsContactEmail: String
    ) {
        fun toRequest(): PreviewFeatureFlagRequest {
            val command = this
            return previewFeatureFlagRequest {
                this.context = command.context
                this.details = PreviewFeatureFlagRequestKt.details {
                    this.contact = contactInfo {
                        this.email = command.detailsContactEmail
                        this.phone = command.detailsContactPhone
                    }
                }
                this.featureFlag = featureFlag {
                    this.owner = userRef {
                        this.displayName = command.featureFlagOwnerDisplayName
                    }
                }
            }
        }
    }

    private suspend fun handleCancelYardMoveWorkflow(request: CancelYardMoveWorkflowRequest): CancelYardMoveWorkflowResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveWorkflowService/CancelYardMoveWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CancelYardMoveWorkflow", request)
            log.atInfo().addKeyValue("operation", "CancelYardMoveWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yardMove.yardMoveId).log("Recorded audit detail")
            val outbound0 = recalculateCartsRequest {
                this.cart = cart {
                    this.audit = auditInfo {
                        this.createdBy = request.yardMove.audit.createdBy
                        this.updatedAt = request.yardMove.audit.updatedAt
                    }
                }
                this.context = request.context
            }
            val reply0 = cartCartCalculationService.recalculateCarts(WireCopies.json(outbound0))
            val outbound1 = CancelYardMoveWorkflow1Command(
                context = request.context,
                featureFlagOwnerDisplayName = request.yardMove.customer.displayName,
                detailsContactPhone = request.details.notify.phone,
                detailsContactEmail = request.yardMove.assignee.email
            ).toRequest()
            val reply1 = featureflagsFeatureFlagValidationService.previewFeatureFlag(outbound1)
            var reply2 = CalculateDispatchAssignmentResponse.getDefaultInstance()
            request.yardMove.commentsList.asFlow().map { item ->
                val outbound2 = calculateDispatchAssignmentRequest {
                    this.context = request.context
                    this.dispatchAssignment = dispatchAssignment {
                        this.audit = auditInfo {
                            this.createdAt = item.createdAt
                        }
                        this.comments.addAll(listOf(dispatchAssignmentComment {
                            this.authorEmail = item.authorEmail
                            this.commentId = item.commentId
                        }))
                    }
                }
                dispatchDispatchAssignmentCalculationService.calculateDispatchAssignment(outbound2)
            }.collect { reply2 = it }
            val outbound3 = scoreRoutePlanRequest {
                this.context = request.context
                this.routePlan = routePlan {
                    this.billingAddress = postalAddress {
                        this.region = reply0.cart.billingAddress.region
                    }
                    this.counterparty = driverRef {
                        this.email = reply2.dispatchAssignment.customer.email
                    }
                    this.lines.addAll(listOf(routePlanLine {
                        this.tax = routePlanLineTax {
                            this.amount = reply1.featureFlag.discount.amount
                        }
                    }))
                }
            }
            val adapter3 = OutboundPort<ScoreRoutePlanRequest, ScoreRoutePlanResponse> { payload ->
                routingRoutePlanValidationService.scoreRoutePlan(payload)
            }
            val port3 = DelegatingPort(adapter3)
            val reply3 = port3.execute(outbound3)
            cancelYardMoveWorkflowResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
                this.yardMove = yardMove {
                    this.priority = request.yardMove.priority
                    this.sla = yardMoveSla {
                        this.dueAt = reply3.routePlan.dueAt
                    }
                    this.yardMoveId = request.yardMoveId
                }
            }
        }

    private suspend fun handlePauseYardMoveWorkflow(request: PauseYardMoveWorkflowRequest): PauseYardMoveWorkflowResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveWorkflowService/PauseYardMoveWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PauseYardMoveWorkflow", request)
            log.atInfo().addKeyValue("operation", "PauseYardMoveWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yardMove.yardMoveId).log("Recorded audit detail")
            val outbound0 = suggestServiceSchedulesRequest {
                this.context = request.context
                this.filter = SuggestServiceSchedulesRequestKt.filter {
                    this.createdAfter = request.yardMove.sla.dueAt
                }
            }
            val invoke0: suspend (SuggestServiceSchedulesRequest, Metadata) -> SuggestServiceSchedulesResponse = maintenanceServiceScheduleQueryService::suggestServiceSchedules
            val reply0 = invoke0(outbound0, Metadata())
            var reply1 = PauseYardWorkflowResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<YardMoveComment>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound1 = pauseYardWorkflowRequest {
                            this.context = request.context
                            this.yard = yard {
                                this.owner = userRef {
                                    this.email = item.authorEmail
                                }
                            }
                        }
                        reply1 = yardsYardWorkflowService.pauseYardWorkflow(outbound1)
                    }
                }
                try {
                    request.yardMove.commentsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            pauseYardMoveWorkflowResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
                this.yardMove = yardMove {
                    this.requester = request.yardMove.requester
                    this.yardMoveId = request.yardMoveId
                }
            }
        }

    private suspend fun handleSignalYardMoveWorkflow(request: SignalYardMoveWorkflowRequest): SignalYardMoveWorkflowResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveWorkflowService/SignalYardMoveWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SignalYardMoveWorkflow", request)
            log.atInfo().addKeyValue("operation", "SignalYardMoveWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yardMove.yardMoveId).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                cloneLedgerAccountRequest {
                    this.context = request.context
                    this.ledgerAccount = ledgerAccount {
                        this.audit = auditInfo {
                            this.updatedBy = request.yardMove.audit.updatedBy
                        }
                        this.contact = contactInfo {
                            this.name = request.details.notify.name
                        }
                    }
                }
            }
            val reply0 = ledgerLedgerAccountService.cloneLedgerAccount(outbound0)
            val outbound1 = resumeDockWorkflowRequest {
                this.context = request.context
                this.details = ResumeDockWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.email = request.yardMove.requester.email
                    }
                }
                this.dock = dock {
                    this.audit = auditInfo {
                        this.createdAt = reply0.ledgerAccount.audit.createdAt
                    }
                    this.owner = userRef {
                        this.displayName = reply0.ledgerAccount.holder.displayName
                    }
                }
            }
            val reply1 = ClientRegistry.lookup("acme.yards.v1.DockWorkflowService", DockWorkflowServiceGrpcKt.DockWorkflowServiceCoroutineStub::class.java).resumeDockWorkflow(outbound1)
            signalYardMoveWorkflowResponse {
                this.outcome = SignalYardMoveWorkflowResponseKt.outcome {
                    this.referenceId = reply1.outcome.referenceId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.yardMove = yardMove {
                    this.assignee = request.yardMove.assignee
                    this.audit = auditInfo {
                        this.etag = reply1.dock.audit.etag
                        this.updatedAt = reply0.ledgerAccount.audit.updatedAt
                    }
                    this.comments.addAll(listOf(yardMoveComment {
                        this.createdAt = reply0.ledgerAccount.audit.createdAt
                    }))
                    this.yardMoveId = request.yardMoveId
                }
            }
        }
}
