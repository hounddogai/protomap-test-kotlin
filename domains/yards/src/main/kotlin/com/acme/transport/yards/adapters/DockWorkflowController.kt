package com.acme.transport.yards.adapters

import com.acme.audit.v1.AuditEventServiceGrpcKt
import com.acme.audit.v1.BatchGetAuditEventsRequest
import com.acme.audit.v1.batchGetAuditEventsRequest
import com.acme.chat.v1.ChatRoomCalculationServiceGrpcKt
import com.acme.chat.v1.EstimateChatRoomRequestKt
import com.acme.chat.v1.chatRoom
import com.acme.chat.v1.chatRoomDimensions
import com.acme.chat.v1.estimateChatRoomRequest
import com.acme.chat.v1.recalculateChatRoomsRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.featureflags.v1.FeatureFlagValidationServiceGrpcKt
import com.acme.featureflags.v1.ValidateFeatureFlagRequest
import com.acme.featureflags.v1.ValidateFeatureFlagRequestKt
import com.acme.featureflags.v1.ValidateFeatureFlagResponse
import com.acme.featureflags.v1.featureFlag
import com.acme.featureflags.v1.validateFeatureFlagRequest
import com.acme.identity.v1.FindUsersRequestKt
import com.acme.identity.v1.ScoreUserRequest
import com.acme.identity.v1.ScoreUserRequestKt
import com.acme.identity.v1.ScoreUserResponse
import com.acme.identity.v1.UserQueryServiceGrpcKt
import com.acme.identity.v1.UserValidationServiceGrpcKt
import com.acme.identity.v1.findUsersRequest
import com.acme.identity.v1.scoreUserRequest
import com.acme.identity.v1.user
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.LedgerAccountQueryServiceGrpcKt
import com.acme.ledger.v1.SummarizeLedgerAccountsRequest
import com.acme.ledger.v1.SummarizeLedgerAccountsRequestKt
import com.acme.ledger.v1.summarizeLedgerAccountsRequest
import com.acme.maintenance.v1.ServiceScheduleValidationServiceGrpcKt
import com.acme.maintenance.v1.previewServiceScheduleRequest
import com.acme.maintenance.v1.serviceSchedule
import com.acme.maintenance.v1.serviceScheduleParticipant
import com.acme.metrics.v1.MetricSeriesValidationServiceGrpcKt
import com.acme.metrics.v1.lintMetricSeriesesRequest
import com.acme.metrics.v1.metricSeries
import com.acme.risk.v1.CreateRiskRegisterRequest
import com.acme.risk.v1.RiskRegisterServiceGrpcKt
import com.acme.risk.v1.createRiskRegisterRequest
import com.acme.risk.v1.riskRegister
import com.acme.search.v1.PreviewSearchIndexResponse
import com.acme.search.v1.SearchIndexComponent
import com.acme.search.v1.SearchIndexValidationServiceGrpcKt
import com.acme.search.v1.copy as searchCopy
import com.acme.search.v1.previewSearchIndexRequest
import com.acme.search.v1.searchIndex
import com.acme.telematics.v1.SuggestTelemetryReadingsRequest
import com.acme.telematics.v1.SuggestTelemetryReadingsRequestKt
import com.acme.telematics.v1.TelemetryReadingQueryServiceGrpcKt
import com.acme.telematics.v1.suggestTelemetryReadingsRequest
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.transport.yards.YardsRepository
import com.acme.yards.v1.AdvanceDockWorkflowRequest
import com.acme.yards.v1.AdvanceDockWorkflowResponse
import com.acme.yards.v1.AdvanceDockWorkflowResponseKt
import com.acme.yards.v1.CancelDockWorkflowRequest
import com.acme.yards.v1.CancelDockWorkflowResponse
import com.acme.yards.v1.CancelDockWorkflowResponseKt
import com.acme.yards.v1.Dock
import com.acme.yards.v1.DockLifecycleServiceGrpcKt
import com.acme.yards.v1.DockWorkflowServiceGrpcKt
import com.acme.yards.v1.ExpireDockRequest
import com.acme.yards.v1.ExpireDockRequestKt
import com.acme.yards.v1.ExpireDockResponse
import com.acme.yards.v1.ListDockWorkflowsRequest
import com.acme.yards.v1.ListDockWorkflowsResponse
import com.acme.yards.v1.ListYardMoveReportsRequestKt
import com.acme.yards.v1.PauseDockWorkflowRequest
import com.acme.yards.v1.PauseDockWorkflowResponse
import com.acme.yards.v1.ResumeDockWorkflowRequest
import com.acme.yards.v1.ResumeDockWorkflowResponse
import com.acme.yards.v1.RetryDockWorkflowRequest
import com.acme.yards.v1.RetryDockWorkflowResponse
import com.acme.yards.v1.RetryDockWorkflowResponseKt
import com.acme.yards.v1.SignalDockWorkflowRequest
import com.acme.yards.v1.SignalDockWorkflowResponse
import com.acme.yards.v1.SignalDockWorkflowResponseKt
import com.acme.yards.v1.StartDockWorkflowRequest
import com.acme.yards.v1.StartDockWorkflowResponse
import com.acme.yards.v1.StartDockWorkflowResponseKt
import com.acme.yards.v1.SubmitYardMoveRequest
import com.acme.yards.v1.YardMoveApprovalServiceGrpcKt
import com.acme.yards.v1.YardMoveReportingServiceGrpcKt
import com.acme.yards.v1.YardMoveValidationServiceGrpcKt
import com.acme.yards.v1.advanceDockWorkflowResponse
import com.acme.yards.v1.cancelDockWorkflowResponse
import com.acme.yards.v1.dock
import com.acme.yards.v1.dockDimensions
import com.acme.yards.v1.expireDockRequest
import com.acme.yards.v1.listDockWorkflowsResponse
import com.acme.yards.v1.listYardMoveReportsRequest
import com.acme.yards.v1.pauseDockWorkflowResponse
import com.acme.yards.v1.resumeDockWorkflowResponse
import com.acme.yards.v1.retryDockWorkflowResponse
import com.acme.yards.v1.scheduleDockExpiryRequest
import com.acme.yards.v1.signalDockWorkflowResponse
import com.acme.yards.v1.startDockWorkflowResponse
import com.acme.yards.v1.submitYardMoveRequest
import com.acme.yards.v1.validateYardMoveRequest
import com.acme.yards.v1.yardMove
import com.acme.yards.v1.yardRef
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Runs the long-lived workflow that takes a dock from start to finish. */
class DockWorkflowController(
    private val store: YardsRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventService: AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub,
    private val chatChatRoomCalculationService: ChatRoomCalculationServiceGrpcKt.ChatRoomCalculationServiceCoroutineStub,
    private val featureflagsFeatureFlagValidationService: FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub,
    private val identityUserQueryService: UserQueryServiceGrpcKt.UserQueryServiceCoroutineStub,
    private val identityUserValidationService: UserValidationServiceGrpcKt.UserValidationServiceCoroutineStub,
    private val ledgerLedgerAccountQueryService: LedgerAccountQueryServiceGrpcKt.LedgerAccountQueryServiceCoroutineStub,
    private val maintenanceServiceScheduleValidationService: ServiceScheduleValidationServiceGrpcKt.ServiceScheduleValidationServiceCoroutineStub,
    private val metricsMetricSeriesValidationService: MetricSeriesValidationServiceGrpcKt.MetricSeriesValidationServiceCoroutineStub,
    private val riskRiskRegisterService: RiskRegisterServiceGrpcKt.RiskRegisterServiceCoroutineStub,
    private val searchSearchIndexValidationService: SearchIndexValidationServiceGrpcKt.SearchIndexValidationServiceCoroutineStub,
    private val telematicsTelemetryReadingQueryService: TelemetryReadingQueryServiceGrpcKt.TelemetryReadingQueryServiceCoroutineStub,
    private val yardsDockLifecycleService: DockLifecycleServiceGrpcKt.DockLifecycleServiceCoroutineStub,
    private val yardsYardMoveApprovalService: YardMoveApprovalServiceGrpcKt.YardMoveApprovalServiceCoroutineStub,
    private val yardsYardMoveReportingService: YardMoveReportingServiceGrpcKt.YardMoveReportingServiceCoroutineStub,
    private val yardsYardMoveValidationService: YardMoveValidationServiceGrpcKt.YardMoveValidationServiceCoroutineStub,
) : DockWorkflowServiceGrpcKt.DockWorkflowServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DockWorkflowController::class.java)

    private val workerForStartDockWorkflow1 = ReplyMailbox<ScoreUserRequest, ScoreUserResponse>(workers) { payload ->
        withTimeout(5_000) { identityUserValidationService.scoreUser(payload) }
    }

    /** Advances a dock. */
    override suspend fun advanceDockWorkflow(request: AdvanceDockWorkflowRequest): AdvanceDockWorkflowResponse = handleAdvanceDockWorkflow(request)

    /** Cancels a dock. */
    override suspend fun cancelDockWorkflow(request: CancelDockWorkflowRequest): CancelDockWorkflowResponse =
        RpcBoundary.run("acme.yards.v1.DockWorkflowService/CancelDockWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CancelDockWorkflow", request)
            log.atInfo().addKeyValue("operation", "CancelDockWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dock.dockId).log("Recorded audit detail")
            val outbound0 = previewSearchIndexRequest {}.searchCopy {
                this.context = request.context
                this.searchIndex = searchIndex {
                    this.audit = auditInfo {
                        this.etag = request.dock.audit.etag
                    }
                }
            }
            val reply0 = searchSearchIndexValidationService.previewSearchIndex(outbound0)
            val outbound1 = request.toSuggestTelemetryReadingsFromCancelDockWorkflow1(reply0)
            val reply1 = telematicsTelemetryReadingQueryService.suggestTelemetryReadings(outbound1)
            val outbound2 = recalculateChatRoomsRequest {
                this.chatRoom = chatRoom {
                    this.dimensions = chatRoomDimensions {
                        this.height = reply0.searchIndex.dimensions.height
                    }
                }
                this.context = request.context
            }
            val reply2 = chatChatRoomCalculationService.recalculateChatRooms(outbound2)
            val outbound3 = createRiskRegisterRequest {
                this.context = request.context
                this.riskRegister = riskRegister {
                    this.children.addAll(listOf(riskRegister {
                        this.children.addAll(listOf(riskRegister {
                            this.location = postalAddress {
                                this.countryCode = request.dock.address.countryCode
                                this.lines.addAll(reply0.searchIndex.address.linesList)
                            }
                        }))
                    }))
                }
            }
            val envelope3 = ProtoAny.pack(outbound3)
            val reply3 = riskRiskRegisterService.createRiskRegister(envelope3.unpack(CreateRiskRegisterRequest::class.java))
            cancelDockWorkflowResponse {
                this.dock = dock {
                    this.dockId = request.dockId
                    this.name = request.dock.name
                    this.owner = reply0.searchIndex.owner
                }
                this.outcome = CancelDockWorkflowResponseKt.outcome {
                    this.status = reply2.result.status
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.message = reply1.result.message
                    this.status = reply3.result.status
                }
            }
        }

    /** Lists docks a page at a time. */
    override suspend fun listDockWorkflows(request: ListDockWorkflowsRequest): ListDockWorkflowsResponse {
        return handleListDockWorkflows(request)
    }

    /** Pauses a dock. */
    override suspend fun pauseDockWorkflow(request: PauseDockWorkflowRequest): PauseDockWorkflowResponse = handlePauseDockWorkflow(request)

    /** Resumes a dock. */
    override suspend fun resumeDockWorkflow(request: ResumeDockWorkflowRequest): ResumeDockWorkflowResponse =
        RpcBoundary.run("acme.yards.v1.DockWorkflowService/ResumeDockWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResumeDockWorkflow", request)
            log.atInfo().addKeyValue("operation", "ResumeDockWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dock.dockId).log("Recorded audit detail")
            val outbound0 = expireDockRequest {
                this.context = request.context
                this.details = ExpireDockRequestKt.details {
                    this.newOwner = userRef {
                        this.email = request.dock.owner.email
                    }
                    this.notifyContact = contactInfo {
                        this.name = request.details.notify.name
                    }
                }
                this.dockId = request.dockId
            }
            val invoke0: suspend (ExpireDockRequest, Metadata) -> ExpireDockResponse = yardsDockLifecycleService::expireDock
            val reply0 = invoke0(outbound0, Metadata())
            resumeDockWorkflowResponse {
                this.dock = dock {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.dock.audit.updatedBy
                    }
                    this.dimensions = dockDimensions {
                        this.length = reply0.dock.dimensions.length
                    }
                    this.dockId = request.dockId
                    this.name = request.dock.name
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Retrys a dock. */
    override suspend fun retryDockWorkflow(request: RetryDockWorkflowRequest): RetryDockWorkflowResponse {
        return handleRetryDockWorkflow(request)
    }

    /** Signals a dock. */
    override suspend fun signalDockWorkflow(request: SignalDockWorkflowRequest): SignalDockWorkflowResponse = handleSignalDockWorkflow(request)

    /** Starts a dock. */
    override suspend fun startDockWorkflow(request: StartDockWorkflowRequest): StartDockWorkflowResponse =
        RpcBoundary.run("acme.yards.v1.DockWorkflowService/StartDockWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StartDockWorkflow", request)
            log.atInfo().addKeyValue("operation", "StartDockWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dock.dockId).log("Recorded audit detail")
            val outbound0 = validateYardMoveRequest {
                this.context = request.context
                this.yardMove = yardMove {
                    this.assignee = userRef {
                        this.email = request.dock.owner.email
                    }
                    this.customer = yardRef {
                        this.displayName = request.dock.owner.displayName
                    }
                    this.requester = contactInfo {
                        this.phone = request.details.notify.phone
                    }
                }
            }
            val reply0 = yardsYardMoveValidationService.validateYardMove(WireCopies.reflective(outbound0))
            val outbound1 = scoreUserRequest {
                this.context = request.context
                this.details = ScoreUserRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.dock.address.region
                    }
                }
                this.user = user {
                    this.relationshipManager = userRef {
                        this.displayName = reply0.yardMove.customer.displayName
                    }
                }
            }
            val reply1 = workerForStartDockWorkflow1.submit(outbound1)
            startDockWorkflowResponse {
                this.dock = dock {
                    this.audit = auditInfo {
                        this.updatedBy = reply1.user.audit.updatedBy
                    }
                    this.code = request.dock.code
                    this.dockId = request.dockId
                }
                this.outcome = StartDockWorkflowResponseKt.outcome {
                    this.nextSteps.addAll(reply0.outcome.nextStepsList)
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleAdvanceDockWorkflow(request: AdvanceDockWorkflowRequest): AdvanceDockWorkflowResponse =
        RpcBoundary.run("acme.yards.v1.DockWorkflowService/AdvanceDockWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "AdvanceDockWorkflow", request)
            log.atInfo().addKeyValue("operation", "AdvanceDockWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dock.dockId).log("Recorded audit detail")
            val outbound0 = scheduleDockExpiryRequest {
                this.context = request.context
                this.dock = dock {
                    this.address = postalAddress {
                        this.locality = request.dock.address.locality
                    }
                    this.dimensions = dockDimensions {
                        this.weight = request.dock.dimensions.weight
                    }
                }
            }
            val reply0 = yardsDockLifecycleService.scheduleDockExpiry(outbound0)
            val outbound1 = listYardMoveReportsRequest {
                this.context = request.context
                this.filter = ListYardMoveReportsRequestKt.filter {
                    this.region = reply0.dock.address.region
                }
            }
            val reply1 = yardsYardMoveReportingService.listYardMoveReports(outbound1)
            advanceDockWorkflowResponse {
                this.dock = dock {
                    this.audit = auditInfo {
                        this.createdBy = reply0.dock.audit.createdBy
                    }
                    this.dockId = request.dockId
                    this.specs.putAll(request.dock.specsMap)
                }
                this.outcome = AdvanceDockWorkflowResponseKt.outcome {
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    private fun CancelDockWorkflowRequest.toSuggestTelemetryReadingsFromCancelDockWorkflow1(reply0: PreviewSearchIndexResponse): SuggestTelemetryReadingsRequest {
        val source = this
        return suggestTelemetryReadingsRequest {
            this.context = source.context
            this.filter = SuggestTelemetryReadingsRequestKt.filter {
                this.createdAfter = source.dock.audit.updatedAt
                this.labels.putAll(source.dock.specsMap)
                this.ownerId = (reply0.searchIndex.componentsMap["primary"] ?: SearchIndexComponent.getDefaultInstance()).componentId
                this.region = source.dock.address.region
            }
        }
    }

    private suspend fun handleListDockWorkflows(request: ListDockWorkflowsRequest): ListDockWorkflowsResponse =
        RpcBoundary.run("acme.yards.v1.DockWorkflowService/ListDockWorkflows") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListDockWorkflows", request)
            log.atInfo().addKeyValue("operation", "ListDockWorkflows").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = lintMetricSeriesesRequest {
                this.context = request.context
                this.metricSeries = metricSeries {
                    this.address = postalAddress {
                        this.region = request.filter.region
                    }
                    this.owner = userRef {
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply0 = metricsMetricSeriesValidationService.lintMetricSerieses(WireCopies.json(outbound0))
            listDockWorkflowsResponse {
                this.docks.addAll(listOf(dock {
                    this.owner = userRef {
                        this.displayName = reply0.metricSeries.owner.displayName
                    }
                }))
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private data class PauseDockWorkflow0Command(
        val context: RequestContext
    ) {
        fun toRequest(): BatchGetAuditEventsRequest {
            val command = this
            return batchGetAuditEventsRequest {
                this.context = command.context
            }
        }
    }

    private suspend fun handlePauseDockWorkflow(request: PauseDockWorkflowRequest): PauseDockWorkflowResponse =
        RpcBoundary.run("acme.yards.v1.DockWorkflowService/PauseDockWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PauseDockWorkflow", request)
            log.atInfo().addKeyValue("operation", "PauseDockWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dock.dockId).log("Recorded audit detail")
            val outbound0 = PauseDockWorkflow0Command(
                context = request.context
            ).toRequest()
            val reply0 = auditAuditEventService.batchGetAuditEvents(outbound0)
            val outbound1 = submitYardMoveRequest {
                this.context = request.context
                this.yardMove = yardMove {
                    this.requester = contactInfo {
                        this.phone = request.details.notify.phone
                    }
                }
            }
            val command1: Submission<SubmitYardMoveRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = yardsYardMoveApprovalService.submitYardMove(payload1)
            val outbound2 = validateFeatureFlagRequest {
                this.context = request.context
                this.details = ValidateFeatureFlagRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.dock.owner.email
                        this.name = request.details.notify.name
                    }
                }
                this.featureFlag = featureFlag {
                    this.audit = auditInfo {
                        this.createdBy = request.dock.audit.createdBy
                        this.updatedAt = request.dock.audit.updatedAt
                    }
                }
            }
            val adapter2 = OutboundPort<ValidateFeatureFlagRequest, ValidateFeatureFlagResponse> { payload ->
                featureflagsFeatureFlagValidationService.validateFeatureFlag(payload)
            }
            val port2 = DelegatingPort(adapter2)
            val reply2 = port2.execute(outbound2)
            val outbound3 = findUsersRequest {
                this.context = request.context
                this.filter = FindUsersRequestKt.filter {
                    this.region = when (request.dock.locatorCase) {
                        Dock.LocatorCase.ADDRESS -> request.dock.address.region
                        else -> ""
                    }
                }
            }
            val reply3 = identityUserQueryService.findUsers(outbound3)
            pauseDockWorkflowResponse {
                this.dock = dock {
                    this.dockId = request.dockId
                    this.owner = userRef {
                        this.email = reply1.yardMove.assignee.email
                    }
                    this.specs.putAll(request.dock.specsMap)
                }
                this.result = operationResult {
                    this.details.putAll(reply3.result.detailsMap)
                    this.message = reply1.outcome.message
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleRetryDockWorkflow(request: RetryDockWorkflowRequest): RetryDockWorkflowResponse =
        RpcBoundary.run("acme.yards.v1.DockWorkflowService/RetryDockWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RetryDockWorkflow", request)
            log.atInfo().addKeyValue("operation", "RetryDockWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dock.dockId).log("Recorded audit detail")
            val outbound0 = SummarizeLedgerAccountsRequest.newBuilder().apply {
                putAllAnnotations(request.annotationsMap)
                setContext(request.context)
                setDetails(SummarizeLedgerAccountsRequestKt.details {
                    this.filters.putAll(request.dock.specsMap)
                })
                setReason(request.reason)
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> ledgerLedgerAccountQueryService.summarizeLedgerAccounts(value) }
            retryDockWorkflowResponse {
                this.dock = dock {
                    this.dockId = request.dockId
                    this.specs.putAll(request.dock.specsMap)
                }
                this.outcome = RetryDockWorkflowResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleSignalDockWorkflow(request: SignalDockWorkflowRequest): SignalDockWorkflowResponse =
        RpcBoundary.run("acme.yards.v1.DockWorkflowService/SignalDockWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SignalDockWorkflow", request)
            log.atInfo().addKeyValue("operation", "SignalDockWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dock.dockId).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                previewServiceScheduleRequest {
                    this.context = request.context
                    this.serviceSchedule = serviceSchedule {
                        this.organizer = userRef {
                            this.userId = request.dock.owner.userId
                        }
                        this.participants.addAll(listOf(serviceScheduleParticipant {
                            this.contact = contactInfo {
                                this.name = request.details.notify.name
                                this.preferredChannel = request.details.notify.preferredChannel
                            }
                        }))
                    }
                }
            }
            val reply0 = maintenanceServiceScheduleValidationService.previewServiceSchedule(outbound0)
            val outbound1 = estimateChatRoomRequest {
                this.chatRoom = chatRoom {
                    this.address = postalAddress {
                        this.region = request.dock.address.region
                    }
                    this.audit = auditInfo {
                        this.createdBy = request.dock.audit.createdBy
                        this.updatedAt = reply0.serviceSchedule.audit.updatedAt
                    }
                }
                this.context = request.context
                this.details = EstimateChatRoomRequestKt.details {
                    this.destination = postalAddress {
                        this.region = reply0.serviceSchedule.location.region
                    }
                }
            }
            val reply1 = ClientRegistry.lookup("acme.chat.v1.ChatRoomCalculationService", ChatRoomCalculationServiceGrpcKt.ChatRoomCalculationServiceCoroutineStub::class.java).estimateChatRoom(outbound1)
            signalDockWorkflowResponse {
                this.dock = dock {
                    this.dockId = request.dockId
                    this.owner = userRef {
                        this.userId = reply1.chatRoom.owner.userId
                    }
                    this.specs.putAll(request.dock.specsMap)
                }
                this.outcome = SignalDockWorkflowResponseKt.outcome {
                    this.effectiveAt = reply0.outcome.effectiveAt
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.status = "ok"
                }
            }
        }
}
