package com.acme.transport.driverapp.adapters

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.AdvanceCustomerWorkflowRequest
import com.acme.customers.v1.CustomerLifecycleServiceGrpcKt
import com.acme.customers.v1.CustomerWorkflowServiceGrpcKt
import com.acme.customers.v1.ListPreferenceReportsRequest
import com.acme.customers.v1.ListPreferenceReportsRequestKt
import com.acme.customers.v1.PreferenceReportingServiceGrpcKt
import com.acme.customers.v1.ScheduleCustomerExpiryRequest
import com.acme.customers.v1.ScheduleCustomerExpiryRequestKt
import com.acme.customers.v1.ScheduleCustomerExpiryResponse
import com.acme.customers.v1.advanceCustomerWorkflowRequest
import com.acme.customers.v1.copy as customersCopy
import com.acme.customers.v1.customer
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.listPreferenceReportsRequest
import com.acme.customers.v1.scheduleCustomerExpiryRequest
import com.acme.dispatch.v1.DelegateDispatchOrderApprovalRequest
import com.acme.dispatch.v1.DelegateDispatchOrderApprovalRequestKt
import com.acme.dispatch.v1.DelegateDispatchOrderApprovalResponse
import com.acme.dispatch.v1.DispatchOrderApprovalServiceGrpcKt
import com.acme.dispatch.v1.delegateDispatchOrderApprovalRequest
import com.acme.driverapp.v1.AcknowledgeDriverHomeEventRequest
import com.acme.driverapp.v1.AcknowledgeDriverHomeEventResponse
import com.acme.driverapp.v1.AcknowledgeDriverHomeEventResponseKt
import com.acme.driverapp.v1.CommentDriverInboxApprovalRequest
import com.acme.driverapp.v1.CommentDriverInboxApprovalRequestKt
import com.acme.driverapp.v1.CommentDriverInboxApprovalResponse
import com.acme.driverapp.v1.ConnectDriverHomeSourceRequest
import com.acme.driverapp.v1.ConnectDriverHomeSourceResponse
import com.acme.driverapp.v1.DelegateDriverInboxApprovalRequestKt
import com.acme.driverapp.v1.DisconnectDriverHomeSourceRequest
import com.acme.driverapp.v1.DisconnectDriverHomeSourceResponse
import com.acme.driverapp.v1.DisconnectDriverHomeSourceResponseKt
import com.acme.driverapp.v1.DriverHomeApprovalServiceGrpcKt
import com.acme.driverapp.v1.DriverHomeIntegrationServiceGrpcKt
import com.acme.driverapp.v1.DriverHomeReportingServiceGrpcKt
import com.acme.driverapp.v1.DriverHomeWorkflowServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxApprovalServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxReportingServiceGrpcKt
import com.acme.driverapp.v1.IngestDriverHomeEventsRequest
import com.acme.driverapp.v1.IngestDriverHomeEventsResponse
import com.acme.driverapp.v1.ListDriverHomeSourcesRequest
import com.acme.driverapp.v1.ListDriverHomeSourcesResponse
import com.acme.driverapp.v1.PauseDriverHomeWorkflowRequest
import com.acme.driverapp.v1.RejectDriverHomeRequestKt
import com.acme.driverapp.v1.RelayDriverHomeMessagesRequest
import com.acme.driverapp.v1.RelayDriverHomeMessagesResponse
import com.acme.driverapp.v1.ScheduleDriverHomeReportRequestKt
import com.acme.driverapp.v1.ScheduleDriverInboxReportRequestKt
import com.acme.driverapp.v1.ShareDriverHomeReportRequest
import com.acme.driverapp.v1.ShareDriverHomeReportRequestKt
import com.acme.driverapp.v1.ShareDriverHomeReportResponse
import com.acme.driverapp.v1.SubmitDriverHomeRequestKt
import com.acme.driverapp.v1.TestDriverHomeConnectionRequest
import com.acme.driverapp.v1.TestDriverHomeConnectionResponse
import com.acme.driverapp.v1.TestDriverHomeConnectionResponseKt
import com.acme.driverapp.v1.acknowledgeDriverHomeEventResponse
import com.acme.driverapp.v1.commentDriverInboxApprovalRequest
import com.acme.driverapp.v1.connectDriverHomeSourceResponse
import com.acme.driverapp.v1.delegateDriverInboxApprovalRequest
import com.acme.driverapp.v1.disconnectDriverHomeSourceResponse
import com.acme.driverapp.v1.driverHome
import com.acme.driverapp.v1.driverHomeComment
import com.acme.driverapp.v1.driverHomeSla
import com.acme.driverapp.v1.driverInbox
import com.acme.driverapp.v1.ingestDriverHomeEventsResponse
import com.acme.driverapp.v1.listDriverHomeSourcesResponse
import com.acme.driverapp.v1.pauseDriverHomeWorkflowRequest
import com.acme.driverapp.v1.rejectDriverHomeRequest
import com.acme.driverapp.v1.relayDriverHomeMessagesResponse
import com.acme.driverapp.v1.resumeDriverHomeWorkflowRequest
import com.acme.driverapp.v1.scheduleDriverHomeReportRequest
import com.acme.driverapp.v1.scheduleDriverInboxReportRequest
import com.acme.driverapp.v1.shareDriverHomeReportRequest
import com.acme.driverapp.v1.submitDriverHomeRequest
import com.acme.driverapp.v1.testDriverHomeConnectionResponse
import com.acme.featureflags.v1.FeatureFlagServiceGrpcKt
import com.acme.featureflags.v1.FeatureFlagValidationServiceGrpcKt
import com.acme.featureflags.v1.LintFeatureFlagsRequest
import com.acme.featureflags.v1.LintFeatureFlagsRequestKt
import com.acme.featureflags.v1.batchGetFeatureFlagsRequest
import com.acme.featureflags.v1.lintFeatureFlagsRequest
import com.acme.identity.v1.userRef
import com.acme.routing.v1.CommentRoutePlanApprovalRequestKt
import com.acme.routing.v1.RoutePlanApprovalServiceGrpcKt
import com.acme.routing.v1.RouteWorkflowServiceGrpcKt
import com.acme.routing.v1.commentRoutePlanApprovalRequest
import com.acme.routing.v1.retryRouteWorkflowRequest
import com.acme.routing.v1.route
import com.acme.routing.v1.routePlan
import com.acme.telematics.v1.GeoFenceApprovalServiceGrpcKt
import com.acme.telematics.v1.RejectGeoFenceRequestKt
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.rejectGeoFenceRequest
import com.acme.transport.driverapp.DriverappRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.venues.v1.NormalizeVenueRequest
import com.acme.venues.v1.NormalizeVenueRequestKt
import com.acme.venues.v1.NormalizeVenueResponse
import com.acme.venues.v1.VenueValidationServiceGrpcKt
import com.acme.venues.v1.normalizeVenueRequest
import com.acme.venues.v1.venue
import com.acme.yards.v1.RecallYardMoveResponse
import com.acme.yards.v1.YardMoveApprovalServiceGrpcKt
import com.acme.yards.v1.recallYardMoveRequest
import com.acme.yards.v1.yardMove
import com.acme.yards.v1.yardMoveStatusChange
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Connects external systems that send driver home events. */
class DriverHomeIntegrationController(
    private val store: DriverappRepository,
    private val workers: CoroutineScope,
    private val customersCustomerLifecycleService: CustomerLifecycleServiceGrpcKt.CustomerLifecycleServiceCoroutineStub,
    private val customersCustomerWorkflowService: CustomerWorkflowServiceGrpcKt.CustomerWorkflowServiceCoroutineStub,
    private val customersPreferenceReportingService: PreferenceReportingServiceGrpcKt.PreferenceReportingServiceCoroutineStub,
    private val dispatchDispatchOrderApprovalService: DispatchOrderApprovalServiceGrpcKt.DispatchOrderApprovalServiceCoroutineStub,
    private val driverappDriverHomeApprovalService: DriverHomeApprovalServiceGrpcKt.DriverHomeApprovalServiceCoroutineStub,
    private val driverappDriverHomeReportingService: DriverHomeReportingServiceGrpcKt.DriverHomeReportingServiceCoroutineStub,
    private val driverappDriverHomeWorkflowService: DriverHomeWorkflowServiceGrpcKt.DriverHomeWorkflowServiceCoroutineStub,
    private val driverappDriverInboxApprovalService: DriverInboxApprovalServiceGrpcKt.DriverInboxApprovalServiceCoroutineStub,
    private val driverappDriverInboxReportingService: DriverInboxReportingServiceGrpcKt.DriverInboxReportingServiceCoroutineStub,
    private val featureflagsFeatureFlagService: FeatureFlagServiceGrpcKt.FeatureFlagServiceCoroutineStub,
    private val featureflagsFeatureFlagValidationService: FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub,
    private val routingRoutePlanApprovalService: RoutePlanApprovalServiceGrpcKt.RoutePlanApprovalServiceCoroutineStub,
    private val routingRouteWorkflowService: RouteWorkflowServiceGrpcKt.RouteWorkflowServiceCoroutineStub,
    private val telematicsGeoFenceApprovalService: GeoFenceApprovalServiceGrpcKt.GeoFenceApprovalServiceCoroutineStub,
    private val venuesVenueValidationService: VenueValidationServiceGrpcKt.VenueValidationServiceCoroutineStub,
    private val yardsYardMoveApprovalService: YardMoveApprovalServiceGrpcKt.YardMoveApprovalServiceCoroutineStub,
) : DriverHomeIntegrationServiceGrpcKt.DriverHomeIntegrationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DriverHomeIntegrationController::class.java)

    private val invokeAcknowledgeDriverHomeEvent3: suspend (DelegateDispatchOrderApprovalRequest) -> DelegateDispatchOrderApprovalResponse = { payload ->
        dispatchDispatchOrderApprovalService.delegateDispatchOrderApproval(payload)
    }

    private val workerForDisconnectDriverHomeSource1 = ReplyMailbox<CommentDriverInboxApprovalRequest, CommentDriverInboxApprovalResponse>(workers) { payload ->
        withTimeout(5_000) { driverappDriverInboxApprovalService.commentDriverInboxApproval(payload) }
    }

    private val clientForDisconnectDriverHomeSource2 by lazy { driverappDriverHomeApprovalService }

    /** Acknowledges a driver home. */
    override suspend fun acknowledgeDriverHomeEvent(request: AcknowledgeDriverHomeEventRequest): AcknowledgeDriverHomeEventResponse = handleAcknowledgeDriverHomeEvent(request)

    /** Connects a driver home. */
    override suspend fun connectDriverHomeSource(request: ConnectDriverHomeSourceRequest): ConnectDriverHomeSourceResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeIntegrationService/ConnectDriverHomeSource") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ConnectDriverHomeSource", request)
            log.atInfo().addKeyValue("operation", "ConnectDriverHomeSource").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.endpoint).log("Recorded audit detail")
            val outbound0 = shareDriverHomeReportRequest {
                this.context = request.context
                this.details = ShareDriverHomeReportRequestKt.details {
                    this.recipient = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
            }
            val invoke0: suspend (ShareDriverHomeReportRequest, Metadata) -> ShareDriverHomeReportResponse = driverappDriverHomeReportingService::shareDriverHomeReport
            val reply0 = invoke0(outbound0, Metadata())
            val outbound1 = ListPreferenceReportsRequest.newBuilder().apply {
                setContext(request.context)
                setFilter(ListPreferenceReportsRequestKt.filter {
                    this.createdAfter = reply0.driverHome.audit.updatedAt
                    this.ownerId = reply0.driverHome.customer.customerId
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> customersPreferenceReportingService.listPreferenceReports(value) }
            val outbound2 = RequestFactory.create {
                resumeDriverHomeWorkflowRequest {
                    this.context = request.context
                    this.driverHome = driverHome {
                        this.audit = auditInfo {
                            this.updatedAt = reply0.driverHome.audit.updatedAt
                        }
                    }
                }
            }
            val reply2 = driverappDriverHomeWorkflowService.resumeDriverHomeWorkflow(outbound2)
            val outbound3 = delegateDriverInboxApprovalRequest {
                this.context = request.context
                this.details = DelegateDriverInboxApprovalRequestKt.details {
                    this.approver = userRef {
                        this.displayName = reply2.driverHome.assignee.displayName
                        this.email = reply2.driverHome.assignee.email
                    }
                }
                this.driverInbox = driverInbox {
                    this.counterparty = customerRef {
                        this.displayName = reply0.driverHome.assignee.displayName
                        this.email = reply2.driverHome.customer.email
                    }
                }
            }
            val reply3 = ClientRegistry.lookup("acme.driverapp.v1.DriverInboxApprovalService", DriverInboxApprovalServiceGrpcKt.DriverInboxApprovalServiceCoroutineStub::class.java).delegateDriverInboxApproval(outbound3)
            connectDriverHomeSourceResponse {
                this.driverHome = driverHome {
                    this.assignee = userRef {
                        this.displayName = reply0.driverHome.customer.displayName
                    }
                    this.comments.addAll(listOf(driverHomeComment {
                        this.createdAt = reply0.driverHome.audit.createdAt
                    }))
                    this.driverHomeId = request.driverHomeId
                    this.requester = contactInfo {
                        this.email = reply2.driverHome.requester.email
                    }
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.message = reply2.result.message
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    /** Disconnects a driver home. */
    override suspend fun disconnectDriverHomeSource(request: DisconnectDriverHomeSourceRequest): DisconnectDriverHomeSourceResponse {
        return handleDisconnectDriverHomeSource(request)
    }

    /** Takes a stream of driver homes and answers once at the end. */
    override suspend fun ingestDriverHomeEvents(requests: Flow<IngestDriverHomeEventsRequest>): IngestDriverHomeEventsResponse {
        var answer = IngestDriverHomeEventsResponse.getDefaultInstance()
        requests.map { request -> handleIngestDriverHomeEvents(request) }.collect { answer = it }
        return answer
    }

    /** Lists driver homes a page at a time. */
    override suspend fun listDriverHomeSources(request: ListDriverHomeSourcesRequest): ListDriverHomeSourcesResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeIntegrationService/ListDriverHomeSources") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListDriverHomeSources", request)
            log.atInfo().addKeyValue("operation", "ListDriverHomeSources").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = scheduleDriverHomeReportRequest {
                this.annotations.putAll(request.context.baggageMap)
                this.context = request.context
                this.details = ScheduleDriverHomeReportRequestKt.details {
                    this.filters.putAll(request.filter.labelsMap)
                }
            }
            val reply0 = driverappDriverHomeReportingService.scheduleDriverHomeReport(outbound0)
            listDriverHomeSourcesResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    /** Exchanges changes of driver homes with a peer. */
    override fun relayDriverHomeMessages(requests: Flow<RelayDriverHomeMessagesRequest>): Flow<RelayDriverHomeMessagesResponse> =
        requests.map { request -> handleRelayDriverHomeMessages(request) }

    /** Tests a driver home. */
    override suspend fun testDriverHomeConnection(request: TestDriverHomeConnectionRequest): TestDriverHomeConnectionResponse = handleTestDriverHomeConnection(request)

    private data class AcknowledgeDriverHomeEvent0Command(
        val context: RequestContext,
        val driverHomeId: String,
        val driverHomeRequesterEmail: String,
        val driverHomeAssigneeUserId: String,
        val reason: String
    ) {
        fun toRequest(): PauseDriverHomeWorkflowRequest {
            val command = this
            return pauseDriverHomeWorkflowRequest {
                this.context = command.context
                this.driverHome = driverHome {
                    this.assignee = userRef {
                        this.userId = command.driverHomeAssigneeUserId
                    }
                    this.requester = contactInfo {
                        this.email = command.driverHomeRequesterEmail
                    }
                }
                this.driverHomeId = command.driverHomeId
                this.reason = command.reason
            }
        }
    }

    private suspend fun handleAcknowledgeDriverHomeEvent(request: AcknowledgeDriverHomeEventRequest): AcknowledgeDriverHomeEventResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeIntegrationService/AcknowledgeDriverHomeEvent") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "AcknowledgeDriverHomeEvent", request)
            log.atInfo().addKeyValue("operation", "AcknowledgeDriverHomeEvent").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.endpoint).log("Recorded audit detail")
            val outbound0 = AcknowledgeDriverHomeEvent0Command(
                context = request.context,
                driverHomeId = request.driverHomeId,
                driverHomeRequesterEmail = request.context.actor.email,
                driverHomeAssigneeUserId = request.context.actor.userId,
                reason = request.reason
            ).toRequest()
            val reply0 = driverappDriverHomeWorkflowService.pauseDriverHomeWorkflow(outbound0)
            val outbound1 = advanceCustomerWorkflowRequest {
                this.context = request.context
                this.customer = customer {
                    this.audit = auditInfo {
                        this.createdBy = reply0.driverHome.audit.createdBy
                    }
                }
            }
            val command1: Submission<AdvanceCustomerWorkflowRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = customersCustomerWorkflowService.advanceCustomerWorkflow(payload1)
            val outbound2 = normalizeVenueRequest {
                this.context = request.context
                this.details = NormalizeVenueRequestKt.details {
                    this.contact = contactInfo {
                        this.preferredChannel = reply1.customer.contact.preferredChannel
                    }
                }
                this.venue = venue {
                    this.address = postalAddress {
                        this.locality = reply1.customer.primaryAddress.locality
                        this.postalCode = reply1.customer.primaryAddress.postalCode
                    }
                    this.audit = auditInfo {
                        this.updatedBy = reply1.customer.audit.updatedBy
                    }
                }
            }
            val adapter2 = OutboundPort<NormalizeVenueRequest, NormalizeVenueResponse> { payload ->
                venuesVenueValidationService.normalizeVenue(payload)
            }
            val port2 = DelegatingPort(adapter2)
            val reply2 = port2.execute(outbound2)
            val outbound3 = delegateDispatchOrderApprovalRequest {
                this.context = request.context
                this.details = DelegateDispatchOrderApprovalRequestKt.details {
                    this.approver = userRef {
                        this.email = reply0.driverHome.requester.email
                    }
                }
            }
            val reply3 = invokeAcknowledgeDriverHomeEvent3(outbound3)
            acknowledgeDriverHomeEventResponse {
                this.driverHome = driverHome {
                    this.assignee = userRef {
                        this.email = reply0.driverHome.assignee.email
                    }
                    this.driverHomeId = request.driverHomeId
                }
                this.outcome = AcknowledgeDriverHomeEventResponseKt.outcome {
                    this.referenceId = reply2.outcome.referenceId
                    this.status = reply3.outcome.status
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleDisconnectDriverHomeSource(request: DisconnectDriverHomeSourceRequest): DisconnectDriverHomeSourceResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeIntegrationService/DisconnectDriverHomeSource") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DisconnectDriverHomeSource", request)
            log.atInfo().addKeyValue("operation", "DisconnectDriverHomeSource").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.endpoint).log("Recorded audit detail")
            val outbound0 = rejectDriverHomeRequest {
                this.context = request.context
                this.details = RejectDriverHomeRequestKt.details {
                    this.approver = userRef {
                        this.userId = request.context.actor.userId
                    }
                }
                this.driverHome = driverHome {
                    this.driverHomeId = request.driverHomeId
                }
                this.reason = request.reason
            }
            val reply0 = driverappDriverHomeApprovalService.rejectDriverHome(WireCopies.reflective(outbound0))
            val outbound1 = commentDriverInboxApprovalRequest {
                this.context = request.context
                this.details = CommentDriverInboxApprovalRequestKt.details {
                    this.approver = userRef {
                        this.email = reply0.driverHome.assignee.email
                        this.userId = reply0.driverHome.assignee.userId
                    }
                }
                this.driverInbox = driverInbox {
                    this.counterparty = customerRef {
                        this.customerId = reply0.driverHome.customer.customerId
                        this.email = reply0.driverHome.customer.email
                    }
                }
            }
            val reply1 = workerForDisconnectDriverHomeSource1.submit(outbound1)
            val outbound2 = submitDriverHomeRequest {
                this.context = request.context
                this.details = SubmitDriverHomeRequestKt.details {
                    this.approver = userRef {
                        this.displayName = reply0.driverHome.assignee.displayName
                    }
                }
            }
            val reply2 = clientForDisconnectDriverHomeSource2.submitDriverHome(outbound2)
            val outbound3 = batchGetFeatureFlagsRequest {
                this.context = request.context
            }
            val reply3 = flowOf(outbound3).map { value -> featureflagsFeatureFlagService.batchGetFeatureFlags(value) }.single()
            disconnectDriverHomeSourceResponse {
                this.driverHome = driverHome {
                    this.assignee = userRef {
                        this.userId = reply0.driverHome.assignee.userId
                    }
                    this.driverHomeId = request.driverHomeId
                    this.requester = contactInfo {
                        this.email = reply2.driverHome.requester.email
                    }
                }
                this.outcome = DisconnectDriverHomeSourceResponseKt.outcome {
                    this.referenceId = reply1.outcome.referenceId
                }
                this.result = operationResult {
                    this.details.putAll(reply3.result.detailsMap)
                    this.message = reply3.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleIngestDriverHomeEvents(request: IngestDriverHomeEventsRequest): IngestDriverHomeEventsResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeIntegrationService/IngestDriverHomeEvents") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "IngestDriverHomeEvents", request)
            log.atInfo().addKeyValue("operation", "IngestDriverHomeEvents").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.header.source).log("Recorded audit detail")
            var reply0 = RecallYardMoveResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.record.historyList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound0 = recallYardMoveRequest {
                                this.context = request.context
                                this.yardMove = yardMove {
                                    this.history.addAll(listOf(yardMoveStatusChange {
                                        this.changedAt = item.changedAt
                                        this.changedBy = item.changedBy
                                    }))
                                }
                            }
                            yardsYardMoveApprovalService.recallYardMove(outbound0)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply0 = it }
                }
            }
            val outbound1 = commentRoutePlanApprovalRequest {
                this.context = request.context
                this.details = CommentRoutePlanApprovalRequestKt.details {
                    this.approver = userRef {
                        this.email = when (request.chunkCase) {
                            IngestDriverHomeEventsRequest.ChunkCase.RECORD -> request.record.assignee.email
                            else -> ""
                        }
                        this.userId = when (request.chunkCase) {
                            IngestDriverHomeEventsRequest.ChunkCase.RECORD -> request.record.assignee.userId
                            else -> ""
                        }
                    }
                }
                this.routePlan = routePlan {
                    this.audit = auditInfo {
                        this.createdAt = reply0.yardMove.audit.createdAt
                    }
                }
            }
            val reply1 = routingRoutePlanApprovalService.commentRoutePlanApproval(outbound1)
            val outbound2 = scheduleDriverInboxReportRequest {
                this.annotations.putAll(reply0.result.detailsMap)
                this.context = request.context
                this.details = ScheduleDriverInboxReportRequestKt.details {
                    this.filters.putAll(reply1.routePlan.metadataMap)
                    this.format = request.header.format
                }
            }
            val reply2 = driverappDriverInboxReportingService.scheduleDriverInboxReport(outbound2)
            ingestDriverHomeEventsResponse {
                this.result = operationResult {
                    this.message = reply2.result.message
                    this.referenceIds.addAll((reply1.result.referenceIdsList) + (reply2.result.referenceIdsList))
                    this.status = "ok"
                }
            }
        }

    private fun RelayDriverHomeMessagesRequest.toLintFeatureFlagsFromRelayDriverHomeMessages1(reply0: ScheduleCustomerExpiryResponse): LintFeatureFlagsRequest {
        val source = this
        return lintFeatureFlagsRequest {
            this.context = source.context
            this.details = LintFeatureFlagsRequestKt.details {
                this.contact = contactInfo {
                    this.name = reply0.customer.contact.name
                    this.phone = source.change.requester.phone
                    this.preferredChannel = reply0.customer.contact.preferredChannel
                }
            }
        }
    }

    private suspend fun handleRelayDriverHomeMessages(request: RelayDriverHomeMessagesRequest): RelayDriverHomeMessagesResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeIntegrationService/RelayDriverHomeMessages") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RelayDriverHomeMessages", request)
            log.atInfo().addKeyValue("operation", "RelayDriverHomeMessages").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.change.driverHomeId).log("Recorded audit detail")
            val outbound0 = scheduleCustomerExpiryRequest {}.customersCopy {
                this.context = request.context
                this.customer = customer {
                    this.contact = contactInfo {
                        this.name = request.change.requester.name
                    }
                }
                this.details = ScheduleCustomerExpiryRequestKt.details {
                    this.notify = contactInfo {
                        this.email = request.change.requester.email
                    }
                }
            }
            val reply0 = customersCustomerLifecycleService.scheduleCustomerExpiry(outbound0)
            val outbound1 = request.toLintFeatureFlagsFromRelayDriverHomeMessages1(reply0)
            val reply1 = featureflagsFeatureFlagValidationService.lintFeatureFlags(outbound1)
            val outbound2 = rejectGeoFenceRequest {
                this.context = request.context
                this.details = RejectGeoFenceRequestKt.details {
                    this.approver = userRef {
                        this.email = reply0.customer.relationshipManager.email
                    }
                }
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.etag = when (request.frameCase) {
                            RelayDriverHomeMessagesRequest.FrameCase.CHANGE -> request.change.audit.etag
                            else -> ""
                        }
                    }
                }
            }
            val reply2 = telematicsGeoFenceApprovalService.rejectGeoFence(outbound2)
            relayDriverHomeMessagesResponse {
                this.driverHome = driverHome {
                    this.assignee = request.change.assignee
                    this.audit = auditInfo {
                        this.etag = reply1.featureFlag.audit.etag
                    }
                    this.requester = contactInfo {
                        this.name = reply0.customer.contact.name
                    }
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply2.result.referenceIdsList)
                    this.status = "ok"
                }
                this.sequence = request.ack.sequence
            }
        }

    private data class TestDriverHomeConnection2Command(
        val context: RequestContext,
        val detailsRecipientEmail: String,
        val detailsRecipientPhone: String
    ) {
        fun toRequest(): ShareDriverHomeReportRequest {
            val command = this
            return shareDriverHomeReportRequest {
                this.context = command.context
                this.details = ShareDriverHomeReportRequestKt.details {
                    this.recipient = contactInfo {
                        this.email = command.detailsRecipientEmail
                        this.phone = command.detailsRecipientPhone
                    }
                }
            }
        }
    }

    private suspend fun handleTestDriverHomeConnection(request: TestDriverHomeConnectionRequest): TestDriverHomeConnectionResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeIntegrationService/TestDriverHomeConnection") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "TestDriverHomeConnection", request)
            log.atInfo().addKeyValue("operation", "TestDriverHomeConnection").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.endpoint).log("Recorded audit detail")
            val outbound0 = scheduleCustomerExpiryRequest {
                this.context = request.context
                this.customer = customer {
                    this.relationshipManager = userRef {
                        this.userId = request.context.actor.userId
                    }
                }
                this.details = ScheduleCustomerExpiryRequestKt.details {
                    this.notify = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
                this.reason = request.reason
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = customersCustomerLifecycleService.scheduleCustomerExpiry(envelope0.unpack(ScheduleCustomerExpiryRequest::class.java))
            val outbound1 = retryRouteWorkflowRequest {
                this.context = request.context
                this.route = route {
                    this.billingAddress = postalAddress {
                        this.lines.addAll(reply0.customer.primaryAddress.linesList)
                        this.locality = reply0.customer.primaryAddress.locality
                    }
                }
            }
            val reply1 = routingRouteWorkflowService.retryRouteWorkflow(WireCopies.json(outbound1))
            val outbound2 = TestDriverHomeConnection2Command(
                context = request.context,
                detailsRecipientEmail = reply1.route.counterparty.email,
                detailsRecipientPhone = reply0.customer.contact.phone
            ).toRequest()
            val reply2 = driverappDriverHomeReportingService.shareDriverHomeReport(outbound2)
            testDriverHomeConnectionResponse {
                this.driverHome = driverHome {
                    this.driverHomeId = request.driverHomeId
                    this.requester = contactInfo {
                        this.email = reply0.customer.relationshipManager.email
                    }
                    this.sla = driverHomeSla {
                        this.dueAt = reply2.driverHome.sla.dueAt
                    }
                }
                this.outcome = TestDriverHomeConnectionResponseKt.outcome {
                    this.message = reply1.result.message
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }
}
