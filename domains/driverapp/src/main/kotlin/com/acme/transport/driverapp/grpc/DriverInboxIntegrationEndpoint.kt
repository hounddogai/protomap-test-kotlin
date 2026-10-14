package com.acme.transport.driverapp.grpc

import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.LintAuditEventsRequestKt
import com.acme.audit.v1.auditEvent
import com.acme.audit.v1.auditTrailRef
import com.acme.audit.v1.lintAuditEventsRequest
import com.acme.authorization.v1.AccessPolicyValidationServiceGrpcKt
import com.acme.authorization.v1.ScoreAccessPolicyRequest
import com.acme.authorization.v1.accessPolicy
import com.acme.authorization.v1.scoreAccessPolicyRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.customers.v1.CustomerWorkflowServiceGrpcKt
import com.acme.customers.v1.ResumeCustomerWorkflowRequestKt
import com.acme.customers.v1.customer
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.resumeCustomerWorkflowRequest
import com.acme.driverapp.v1.ConnectDriverInboxSourceRequest
import com.acme.driverapp.v1.ConnectDriverInboxSourceResponse
import com.acme.driverapp.v1.ConnectDriverInboxSourceResponseKt
import com.acme.driverapp.v1.DisconnectDriverInboxSourceRequest
import com.acme.driverapp.v1.DisconnectDriverInboxSourceResponse
import com.acme.driverapp.v1.DisconnectDriverInboxSourceResponseKt
import com.acme.driverapp.v1.DriverHomeApprovalServiceGrpcKt
import com.acme.driverapp.v1.DriverHomeIntegrationServiceGrpcKt
import com.acme.driverapp.v1.DriverHomeWorkflowServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxIntegrationServiceGrpcKt
import com.acme.driverapp.v1.IngestDriverInboxEventsRequest
import com.acme.driverapp.v1.IngestDriverInboxEventsResponse
import com.acme.driverapp.v1.ListDriverHomeSourcesRequestKt
import com.acme.driverapp.v1.ListDriverInboxSourcesRequest
import com.acme.driverapp.v1.ListDriverInboxSourcesResponse
import com.acme.driverapp.v1.ListPendingDriverHomesRequest
import com.acme.driverapp.v1.ListPendingDriverHomesRequestKt
import com.acme.driverapp.v1.ListPendingDriverHomesResponse
import com.acme.driverapp.v1.RelayDriverInboxMessagesRequest
import com.acme.driverapp.v1.RelayDriverInboxMessagesResponse
import com.acme.driverapp.v1.ResumeDriverHomeWorkflowRequest
import com.acme.driverapp.v1.ResumeDriverHomeWorkflowResponse
import com.acme.driverapp.v1.SignalDriverHomeWorkflowRequest
import com.acme.driverapp.v1.TestDriverInboxConnectionRequest
import com.acme.driverapp.v1.TestDriverInboxConnectionResponse
import com.acme.driverapp.v1.TestDriverInboxConnectionResponseKt
import com.acme.driverapp.v1.advanceDriverHomeWorkflowRequest
import com.acme.driverapp.v1.connectDriverInboxSourceResponse
import com.acme.driverapp.v1.disconnectDriverInboxSourceResponse
import com.acme.driverapp.v1.driverHome
import com.acme.driverapp.v1.driverHomeComment
import com.acme.driverapp.v1.driverInbox
import com.acme.driverapp.v1.ingestDriverInboxEventsResponse
import com.acme.driverapp.v1.listDriverHomeSourcesRequest
import com.acme.driverapp.v1.listDriverInboxSourcesResponse
import com.acme.driverapp.v1.listPendingDriverHomesRequest
import com.acme.driverapp.v1.relayDriverInboxMessagesResponse
import com.acme.driverapp.v1.resumeDriverHomeWorkflowRequest
import com.acme.driverapp.v1.signalDriverHomeWorkflowRequest
import com.acme.driverapp.v1.testDriverInboxConnectionResponse
import com.acme.fuel.v1.CountFuelPolicyTotalsRequestKt
import com.acme.fuel.v1.FuelPolicyReportingServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionSyncServiceGrpcKt
import com.acme.fuel.v1.ResolveFuelTransactionConflictRequest
import com.acme.fuel.v1.ResolveFuelTransactionConflictResponse
import com.acme.fuel.v1.countFuelPolicyTotalsRequest
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.resolveFuelTransactionConflictRequest
import com.acme.identity.v1.userRef
import com.acme.telematics.v1.ScheduleTelemetryReadingReportRequest
import com.acme.telematics.v1.ScheduleTelemetryReadingReportResponse
import com.acme.telematics.v1.TelemetryReadingReportingServiceGrpcKt
import com.acme.telematics.v1.scheduleTelemetryReadingReportRequest
import com.acme.transport.driverapp.DriverappRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Connects external systems that send driver inbox events. */
class DriverInboxIntegrationEndpoint(
    private val store: DriverappRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventValidationService: AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub,
    private val authorizationAccessPolicyValidationService: AccessPolicyValidationServiceGrpcKt.AccessPolicyValidationServiceCoroutineStub,
    private val customersCustomerWorkflowService: CustomerWorkflowServiceGrpcKt.CustomerWorkflowServiceCoroutineStub,
    private val driverappDriverHomeApprovalService: DriverHomeApprovalServiceGrpcKt.DriverHomeApprovalServiceCoroutineStub,
    private val driverappDriverHomeIntegrationService: DriverHomeIntegrationServiceGrpcKt.DriverHomeIntegrationServiceCoroutineStub,
    private val driverappDriverHomeWorkflowService: DriverHomeWorkflowServiceGrpcKt.DriverHomeWorkflowServiceCoroutineStub,
    private val fuelFuelPolicyReportingService: FuelPolicyReportingServiceGrpcKt.FuelPolicyReportingServiceCoroutineStub,
    private val fuelFuelTransactionSyncService: FuelTransactionSyncServiceGrpcKt.FuelTransactionSyncServiceCoroutineStub,
    private val telematicsTelemetryReadingReportingService: TelemetryReadingReportingServiceGrpcKt.TelemetryReadingReportingServiceCoroutineStub,
) : DriverInboxIntegrationServiceGrpcKt.DriverInboxIntegrationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DriverInboxIntegrationEndpoint::class.java)

    private val invokeDisconnectDriverInboxSource1: suspend (ResumeDriverHomeWorkflowRequest) -> ResumeDriverHomeWorkflowResponse = { payload ->
        driverappDriverHomeWorkflowService.resumeDriverHomeWorkflow(payload)
    }

    private val workerForTestDriverInboxConnection1 = ReplyMailbox<ListPendingDriverHomesRequest, ListPendingDriverHomesResponse>(workers) { payload ->
        withTimeout(5_000) { driverappDriverHomeApprovalService.listPendingDriverHomes(payload) }
    }

    private val clientForTestDriverInboxConnection2 by lazy { fuelFuelPolicyReportingService }

    /** Connects a driver inbox. */
    override suspend fun connectDriverInboxSource(request: ConnectDriverInboxSourceRequest): ConnectDriverInboxSourceResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxIntegrationService/ConnectDriverInboxSource") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ConnectDriverInboxSource", request)
            log.atInfo().addKeyValue("operation", "ConnectDriverInboxSource").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.endpoint).log("Recorded audit detail")
            val outbound0 = advanceDriverHomeWorkflowRequest {
                this.context = request.context
                this.driverHome = driverHome {
                    this.customer = customerRef {
                        this.email = request.context.actor.email
                    }
                }
                this.reason = request.reason
            }
            val reply0 = driverappDriverHomeWorkflowService.advanceDriverHomeWorkflow(WireCopies.json(outbound0))
            val outbound1 = ConnectDriverInboxSource1Command(
                context = request.context,
                accessPolicyOwnerEmail = reply0.driverHome.assignee.email
            ).toRequest()
            val reply1 = authorizationAccessPolicyValidationService.scoreAccessPolicy(outbound1)
            val outbound2 = signalDriverHomeWorkflowRequest {
                this.context = request.context
                this.driverHome = driverHome {
                    this.comments.addAll(listOf(driverHomeComment {
                        this.createdAt = reply0.driverHome.audit.createdAt
                    }))
                    this.customer = customerRef {
                        this.displayName = reply0.driverHome.customer.displayName
                    }
                }
            }
            val command2: Submission<SignalDriverHomeWorkflowRequest> = Submission.Live(outbound2)
            val payload2 = when (val command = command2) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply2 = driverappDriverHomeWorkflowService.signalDriverHomeWorkflow(payload2)
            connectDriverInboxSourceResponse {
                this.driverInbox = driverInbox {
                    this.counterparty = customerRef {
                        this.customerId = reply0.driverHome.customer.customerId
                        this.displayName = reply0.driverHome.assignee.displayName
                    }
                    this.driverInboxId = request.driverInboxId
                }
                this.outcome = ConnectDriverInboxSourceResponseKt.outcome {
                    this.message = reply1.result.message
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Disconnects a driver inbox. */
    override suspend fun disconnectDriverInboxSource(request: DisconnectDriverInboxSourceRequest): DisconnectDriverInboxSourceResponse {
        return handleDisconnectDriverInboxSource(request)
    }

    /** Takes a stream of driver inboxes and answers once at the end. */
    override suspend fun ingestDriverInboxEvents(requests: Flow<IngestDriverInboxEventsRequest>): IngestDriverInboxEventsResponse {
        var answer = IngestDriverInboxEventsResponse.getDefaultInstance()
        requests.map { request -> handleIngestDriverInboxEvents(request) }.collect { answer = it }
        return answer
    }

    /** Lists driver inboxes a page at a time. */
    override suspend fun listDriverInboxSources(request: ListDriverInboxSourcesRequest): ListDriverInboxSourcesResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxIntegrationService/ListDriverInboxSources") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListDriverInboxSources", request)
            log.atInfo().addKeyValue("operation", "ListDriverInboxSources").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            listDriverInboxSourcesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Exchanges changes of driver inboxes with a peer. */
    override fun relayDriverInboxMessages(requests: Flow<RelayDriverInboxMessagesRequest>): Flow<RelayDriverInboxMessagesResponse> =
        requests.map { request -> handleRelayDriverInboxMessages(request) }

    /** Tests a driver inbox. */
    override suspend fun testDriverInboxConnection(request: TestDriverInboxConnectionRequest): TestDriverInboxConnectionResponse = handleTestDriverInboxConnection(request)

    private data class ConnectDriverInboxSource1Command(
        val context: RequestContext,
        val accessPolicyOwnerEmail: String
    ) {
        fun toRequest(): ScoreAccessPolicyRequest {
            val command = this
            return scoreAccessPolicyRequest {
                this.accessPolicy = accessPolicy {
                    this.owner = userRef {
                        this.email = command.accessPolicyOwnerEmail
                    }
                }
                this.context = command.context
            }
        }
    }

    private suspend fun handleDisconnectDriverInboxSource(request: DisconnectDriverInboxSourceRequest): DisconnectDriverInboxSourceResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxIntegrationService/DisconnectDriverInboxSource") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DisconnectDriverInboxSource", request)
            log.atInfo().addKeyValue("operation", "DisconnectDriverInboxSource").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.endpoint).log("Recorded audit detail")
            val outbound0 = scheduleTelemetryReadingReportRequest {
                this.context = request.context
                this.reason = request.reason
            }
            val adapter0 = OutboundPort<ScheduleTelemetryReadingReportRequest, ScheduleTelemetryReadingReportResponse> { payload ->
                telematicsTelemetryReadingReportingService.scheduleTelemetryReadingReport(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = resumeDriverHomeWorkflowRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.driverHome = driverHome {
                    this.assignee = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply1 = invokeDisconnectDriverInboxSource1(outbound1)
            disconnectDriverInboxSourceResponse {
                this.driverInbox = driverInbox {
                    this.driverInboxId = request.driverInboxId
                }
                this.outcome = DisconnectDriverInboxSourceResponseKt.outcome {
                    this.referenceId = reply1.outcome.referenceId
                    this.status = reply1.outcome.status
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleIngestDriverInboxEvents(request: IngestDriverInboxEventsRequest): IngestDriverInboxEventsResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxIntegrationService/IngestDriverInboxEvents") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "IngestDriverInboxEvents", request)
            log.atInfo().addKeyValue("operation", "IngestDriverInboxEvents").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.header.source).log("Recorded audit detail")
            val outbound0 = resolveFuelTransactionConflictRequest {
                this.context = request.context
                this.fuelTransaction = fuelTransaction {
                    this.subject = customerRef {
                        this.customerId = request.record.counterparty.customerId
                        this.displayName = request.record.counterparty.displayName
                        this.email = request.header.submittedBy.email
                    }
                }
            }
            val invoke0: suspend (ResolveFuelTransactionConflictRequest, Metadata) -> ResolveFuelTransactionConflictResponse = fuelFuelTransactionSyncService::resolveFuelTransactionConflict
            val reply0 = invoke0(outbound0, Metadata())
            val outbound1 = ResumeDriverHomeWorkflowRequest.newBuilder().apply {
                setContext(request.context)
                setDriverHome(driverHome {
                    this.audit = auditInfo {
                        this.createdBy = request.record.audit.createdBy
                    }
                    this.requester = contactInfo {
                        this.phone = request.header.submittedBy.phone
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> driverappDriverHomeWorkflowService.resumeDriverHomeWorkflow(value) }
            ingestDriverInboxEventsResponse {
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = reply1.result.status
                }
            }
        }

    private suspend fun handleRelayDriverInboxMessages(request: RelayDriverInboxMessagesRequest): RelayDriverInboxMessagesResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxIntegrationService/RelayDriverInboxMessages") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RelayDriverInboxMessages", request)
            log.atInfo().addKeyValue("operation", "RelayDriverInboxMessages").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.change.driverInboxId).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                lintAuditEventsRequest {
                    this.auditEvent = auditEvent {
                        this.sequence = request.ack.sequence
                        this.subject = auditTrailRef {
                            this.displayName = request.change.counterparty.displayName
                            this.number = request.change.number
                        }
                    }
                    this.context = request.context
                    this.details = LintAuditEventsRequestKt.details {
                        this.contact = contactInfo {
                            this.email = request.change.counterparty.email
                        }
                    }
                }
            }
            val reply0 = auditAuditEventValidationService.lintAuditEvents(outbound0)
            val outbound1 = listDriverHomeSourcesRequest {
                this.context = request.context
                this.filter = ListDriverHomeSourcesRequestKt.filter {
                    this.region = request.change.billingAddress.region
                }
            }
            val reply1 = ClientRegistry.lookup("acme.driverapp.v1.DriverHomeIntegrationService", DriverHomeIntegrationServiceGrpcKt.DriverHomeIntegrationServiceCoroutineStub::class.java).listDriverHomeSources(outbound1)
            relayDriverInboxMessagesResponse {
                this.driverInbox = driverInbox {
                    this.notes = request.change.notes
                    this.status = request.change.status
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.details.putAll(reply1.result.detailsMap)
                    this.message = reply0.outcome.message
                    this.status = reply0.outcome.status
                }
            }
        }

    private suspend fun handleTestDriverInboxConnection(request: TestDriverInboxConnectionRequest): TestDriverInboxConnectionResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxIntegrationService/TestDriverInboxConnection") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "TestDriverInboxConnection", request)
            log.atInfo().addKeyValue("operation", "TestDriverInboxConnection").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.endpoint).log("Recorded audit detail")
            val outbound0 = resumeCustomerWorkflowRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.customer = customer {
                    this.relationshipManager = userRef {
                        this.userId = request.context.actor.userId
                    }
                }
                this.details = ResumeCustomerWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
                this.reason = request.reason
            }
            val reply0 = customersCustomerWorkflowService.resumeCustomerWorkflow(WireCopies.reflective(outbound0))
            val outbound1 = listPendingDriverHomesRequest {
                this.context = request.context
                this.filter = ListPendingDriverHomesRequestKt.filter {
                    this.createdAfter = reply0.customer.audit.updatedAt
                    this.labels.putAll(reply0.customer.attributesMap)
                    this.ownerId = reply0.customer.relationshipManager.userId
                    this.region = reply0.customer.primaryAddress.region
                }
            }
            val reply1 = workerForTestDriverInboxConnection1.submit(outbound1)
            val outbound2 = countFuelPolicyTotalsRequest {
                this.context = request.context
                this.filter = CountFuelPolicyTotalsRequestKt.filter {
                    this.createdAfter = reply0.customer.passport.expiresAt
                    this.ownerId = reply0.outcome.referenceId
                    this.region = reply0.customer.primaryAddress.region
                }
            }
            val reply2 = clientForTestDriverInboxConnection2.countFuelPolicyTotals(outbound2)
            testDriverInboxConnectionResponse {
                this.driverInbox = driverInbox {
                    this.driverInboxId = request.driverInboxId
                    this.number = reply0.customer.passport.number
                }
                this.outcome = TestDriverInboxConnectionResponseKt.outcome {
                    this.status = reply2.result.status
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = reply0.result.status
                }
            }
        }
}
