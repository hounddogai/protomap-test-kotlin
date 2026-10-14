package com.acme.transport.maintenance.adapters

import com.acme.audit.v1.AuditEventQueryServiceGrpcKt
import com.acme.audit.v1.AuditEventServiceGrpcKt
import com.acme.audit.v1.FindAuditEventsRequestKt
import com.acme.audit.v1.FindAuditEventsResponse
import com.acme.audit.v1.batchGetAuditEventsRequest
import com.acme.audit.v1.findAuditEventsRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.pageRequest
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.customerRef
import com.acme.drivers.v1.CalculateDriverRequest
import com.acme.drivers.v1.CalculateDriverResponse
import com.acme.drivers.v1.DriverCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseValidationServiceGrpcKt
import com.acme.drivers.v1.ExplainDriverCalculationRequest
import com.acme.drivers.v1.ExplainDriverLicenseDecisionRequestKt
import com.acme.drivers.v1.LintDriverLicensesRequestKt
import com.acme.drivers.v1.SimulateDriverResponse
import com.acme.drivers.v1.calculateDriverRequest
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.explainDriverCalculationRequest
import com.acme.drivers.v1.explainDriverLicenseDecisionRequest
import com.acme.drivers.v1.lintDriverLicensesRequest
import com.acme.drivers.v1.simulateDriverRequest
import com.acme.fuel.v1.FuelPolicyApprovalServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionImportServiceGrpcKt
import com.acme.fuel.v1.RejectFuelPolicyRequest
import com.acme.fuel.v1.RejectFuelPolicyRequestKt
import com.acme.fuel.v1.RejectFuelPolicyResponse
import com.acme.fuel.v1.RetryFuelTransactionImportRequestKt
import com.acme.fuel.v1.ValidateFuelTransactionImportRequestKt
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.rejectFuelPolicyRequest
import com.acme.fuel.v1.retryFuelTransactionImportRequest
import com.acme.fuel.v1.validateFuelTransactionImportRequest
import com.acme.identity.v1.userRef
import com.acme.keymanagement.v1.CryptoKeyCalculationServiceGrpcKt
import com.acme.keymanagement.v1.RecalculateCryptoKeysRequest
import com.acme.keymanagement.v1.RecalculateCryptoKeysRequestKt
import com.acme.keymanagement.v1.recalculateCryptoKeysRequest
import com.acme.ledger.v1.LedgerAccountValidationServiceGrpcKt
import com.acme.ledger.v1.ledgerAccount
import com.acme.ledger.v1.previewLedgerAccountRequest
import com.acme.lineage.v1.LineageNodeCalculationServiceGrpcKt
import com.acme.lineage.v1.estimateLineageNodeRequest
import com.acme.lineage.v1.lineageNode
import com.acme.maintenance.v1.AdvanceServiceScheduleWorkflowRequest
import com.acme.maintenance.v1.AdvanceServiceScheduleWorkflowResponse
import com.acme.maintenance.v1.AdvanceWorkOrderWorkflowRequestKt
import com.acme.maintenance.v1.CancelServiceScheduleWorkflowRequest
import com.acme.maintenance.v1.CancelServiceScheduleWorkflowResponse
import com.acme.maintenance.v1.CancelServiceScheduleWorkflowResponseKt
import com.acme.maintenance.v1.InspectServiceScheduleWorkflowRequest
import com.acme.maintenance.v1.InspectServiceScheduleWorkflowResponse
import com.acme.maintenance.v1.ListPartReportsRequestKt
import com.acme.maintenance.v1.ListPendingServiceSchedulesRequestKt
import com.acme.maintenance.v1.ListPendingWorkOrdersRequest
import com.acme.maintenance.v1.ListPendingWorkOrdersRequestKt
import com.acme.maintenance.v1.ListServiceScheduleWorkflowsRequest
import com.acme.maintenance.v1.ListServiceScheduleWorkflowsResponse
import com.acme.maintenance.v1.PartReportingServiceGrpcKt
import com.acme.maintenance.v1.PauseServiceScheduleWorkflowRequest
import com.acme.maintenance.v1.PauseServiceScheduleWorkflowResponse
import com.acme.maintenance.v1.PauseServiceScheduleWorkflowResponseKt
import com.acme.maintenance.v1.QuoteWorkOrderResponse
import com.acme.maintenance.v1.ResumeServiceScheduleWorkflowRequest
import com.acme.maintenance.v1.ResumeServiceScheduleWorkflowResponse
import com.acme.maintenance.v1.ResumeServiceScheduleWorkflowResponseKt
import com.acme.maintenance.v1.RetryServiceScheduleWorkflowRequest
import com.acme.maintenance.v1.RetryServiceScheduleWorkflowResponse
import com.acme.maintenance.v1.ServiceScheduleApprovalServiceGrpcKt
import com.acme.maintenance.v1.ServiceScheduleParticipant
import com.acme.maintenance.v1.ServiceScheduleWorkflowServiceGrpcKt
import com.acme.maintenance.v1.SignalServiceScheduleWorkflowRequest
import com.acme.maintenance.v1.SignalServiceScheduleWorkflowResponse
import com.acme.maintenance.v1.SignalServiceScheduleWorkflowResponseKt
import com.acme.maintenance.v1.StartServiceScheduleWorkflowRequest
import com.acme.maintenance.v1.StartServiceScheduleWorkflowResponse
import com.acme.maintenance.v1.StartWorkOrderWorkflowRequest
import com.acme.maintenance.v1.WorkOrderApprovalServiceGrpcKt
import com.acme.maintenance.v1.WorkOrderCalculationServiceGrpcKt
import com.acme.maintenance.v1.WorkOrderWorkflowServiceGrpcKt
import com.acme.maintenance.v1.advanceServiceScheduleWorkflowResponse
import com.acme.maintenance.v1.advanceWorkOrderWorkflowRequest
import com.acme.maintenance.v1.cancelServiceScheduleWorkflowResponse
import com.acme.maintenance.v1.inspectServiceScheduleWorkflowResponse
import com.acme.maintenance.v1.listPartReportsRequest
import com.acme.maintenance.v1.listPendingServiceSchedulesRequest
import com.acme.maintenance.v1.listPendingWorkOrdersRequest
import com.acme.maintenance.v1.listServiceScheduleWorkflowsResponse
import com.acme.maintenance.v1.pauseServiceScheduleWorkflowResponse
import com.acme.maintenance.v1.quoteWorkOrderRequest
import com.acme.maintenance.v1.resumeServiceScheduleWorkflowResponse
import com.acme.maintenance.v1.retryServiceScheduleWorkflowResponse
import com.acme.maintenance.v1.serviceSchedule
import com.acme.maintenance.v1.serviceScheduleParticipant
import com.acme.maintenance.v1.signalServiceScheduleWorkflowResponse
import com.acme.maintenance.v1.startServiceScheduleWorkflowResponse
import com.acme.maintenance.v1.startWorkOrderWorkflowRequest
import com.acme.maintenance.v1.workOrder
import com.acme.transport.maintenance.MaintenanceRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.travel.v1.TripCalculationServiceGrpcKt
import com.acme.travel.v1.quoteTripRequest
import com.acme.travel.v1.trip
import com.acme.venues.v1.SummarizeVenuesRequest
import com.acme.venues.v1.VenueQueryServiceGrpcKt
import com.acme.venues.v1.summarizeVenuesRequest
import com.acme.yards.v1.ListPendingYardMovesRequestKt
import com.acme.yards.v1.YardMoveApprovalServiceGrpcKt
import com.acme.yards.v1.copy as yardsCopy
import com.acme.yards.v1.listPendingYardMovesRequest
import com.google.protobuf.Any as ProtoAny
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

/** Runs the long-lived workflow that takes a service schedule from start to finish. */
class ServiceScheduleWorkflowController(
    private val store: MaintenanceRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventQueryService: AuditEventQueryServiceGrpcKt.AuditEventQueryServiceCoroutineStub,
    private val auditAuditEventService: AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub,
    private val driversDriverCalculationService: DriverCalculationServiceGrpcKt.DriverCalculationServiceCoroutineStub,
    private val driversDriverLicenseValidationService: DriverLicenseValidationServiceGrpcKt.DriverLicenseValidationServiceCoroutineStub,
    private val fuelFuelPolicyApprovalService: FuelPolicyApprovalServiceGrpcKt.FuelPolicyApprovalServiceCoroutineStub,
    private val fuelFuelTransactionImportService: FuelTransactionImportServiceGrpcKt.FuelTransactionImportServiceCoroutineStub,
    private val keymanagementCryptoKeyCalculationService: CryptoKeyCalculationServiceGrpcKt.CryptoKeyCalculationServiceCoroutineStub,
    private val ledgerLedgerAccountValidationService: LedgerAccountValidationServiceGrpcKt.LedgerAccountValidationServiceCoroutineStub,
    private val lineageLineageNodeCalculationService: LineageNodeCalculationServiceGrpcKt.LineageNodeCalculationServiceCoroutineStub,
    private val maintenancePartReportingService: PartReportingServiceGrpcKt.PartReportingServiceCoroutineStub,
    private val maintenanceServiceScheduleApprovalService: ServiceScheduleApprovalServiceGrpcKt.ServiceScheduleApprovalServiceCoroutineStub,
    private val maintenanceWorkOrderApprovalService: WorkOrderApprovalServiceGrpcKt.WorkOrderApprovalServiceCoroutineStub,
    private val maintenanceWorkOrderCalculationService: WorkOrderCalculationServiceGrpcKt.WorkOrderCalculationServiceCoroutineStub,
    private val maintenanceWorkOrderWorkflowService: WorkOrderWorkflowServiceGrpcKt.WorkOrderWorkflowServiceCoroutineStub,
    private val travelTripCalculationService: TripCalculationServiceGrpcKt.TripCalculationServiceCoroutineStub,
    private val venuesVenueQueryService: VenueQueryServiceGrpcKt.VenueQueryServiceCoroutineStub,
    private val yardsYardMoveApprovalService: YardMoveApprovalServiceGrpcKt.YardMoveApprovalServiceCoroutineStub,
) : ServiceScheduleWorkflowServiceGrpcKt.ServiceScheduleWorkflowServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(ServiceScheduleWorkflowController::class.java)

    private val workerForRetryServiceScheduleWorkflow1 = ReplyMailbox<CalculateDriverRequest, CalculateDriverResponse>(workers) { payload ->
        withTimeout(5_000) { driversDriverCalculationService.calculateDriver(payload) }
    }

    private val clientForSignalServiceScheduleWorkflow0 by lazy { lineageLineageNodeCalculationService }

    /** Advances a service schedule. */
    override suspend fun advanceServiceScheduleWorkflow(request: AdvanceServiceScheduleWorkflowRequest): AdvanceServiceScheduleWorkflowResponse = handleAdvanceServiceScheduleWorkflow(request)

    /** Cancels a service schedule. */
    override suspend fun cancelServiceScheduleWorkflow(request: CancelServiceScheduleWorkflowRequest): CancelServiceScheduleWorkflowResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleWorkflowService/CancelServiceScheduleWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CancelServiceScheduleWorkflow", request)
            log.atInfo().addKeyValue("operation", "CancelServiceScheduleWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.serviceSchedule.serviceScheduleId).log("Recorded audit detail")
            val outbound0 = listPendingYardMovesRequest {}.yardsCopy {
                this.context = request.context
                this.filter = ListPendingYardMovesRequestKt.filter {
                    this.createdAfter = request.serviceSchedule.audit.createdAt
                    this.ownerId = request.serviceSchedule.organizer.userId
                    this.region = request.serviceSchedule.location.region
                }
            }
            val reply0 = yardsYardMoveApprovalService.listPendingYardMoves(outbound0)
            val outbound1 = request.toExplainDriverCalculationFromCancelServiceScheduleWorkflow1()
            val reply1 = driversDriverCalculationService.explainDriverCalculation(outbound1)
            val outbound2 = quoteTripRequest {
                this.context = request.context
                this.trip = trip {
                    this.billingAddress = postalAddress {
                        this.lines.addAll(request.serviceSchedule.location.linesList)
                    }
                }
            }
            val reply2 = travelTripCalculationService.quoteTrip(outbound2)
            val outbound3 = startWorkOrderWorkflowRequest {
                this.context = request.context
                this.workOrder = workOrder {
                    this.audit = auditInfo {
                        this.etag = reply1.driver.audit.etag
                    }
                    this.customer = driverRef {
                        this.displayName = reply1.driver.relationshipManager.displayName
                    }
                }
            }
            val envelope3 = ProtoAny.pack(outbound3)
            val reply3 = maintenanceWorkOrderWorkflowService.startWorkOrderWorkflow(envelope3.unpack(StartWorkOrderWorkflowRequest::class.java))
            cancelServiceScheduleWorkflowResponse {
                this.outcome = CancelServiceScheduleWorkflowResponseKt.outcome {
                    this.message = reply1.outcome.message
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
                this.serviceSchedule = serviceSchedule {
                    this.audit = auditInfo {
                        this.updatedAt = reply3.workOrder.audit.updatedAt
                    }
                    this.location = postalAddress {
                        this.postalCode = reply2.trip.billingAddress.postalCode
                    }
                    this.organizer = request.serviceSchedule.organizer
                    this.serviceScheduleId = request.serviceScheduleId
                }
            }
        }

    /** Returns a service schedule. */
    override suspend fun inspectServiceScheduleWorkflow(request: InspectServiceScheduleWorkflowRequest): InspectServiceScheduleWorkflowResponse {
        return handleInspectServiceScheduleWorkflow(request)
    }

    /** Lists service schedules a page at a time. */
    override suspend fun listServiceScheduleWorkflows(request: ListServiceScheduleWorkflowsRequest): ListServiceScheduleWorkflowsResponse = handleListServiceScheduleWorkflows(request)

    /** Pauses a service schedule. */
    override suspend fun pauseServiceScheduleWorkflow(request: PauseServiceScheduleWorkflowRequest): PauseServiceScheduleWorkflowResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleWorkflowService/PauseServiceScheduleWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PauseServiceScheduleWorkflow", request)
            log.atInfo().addKeyValue("operation", "PauseServiceScheduleWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.serviceSchedule.serviceScheduleId).log("Recorded audit detail")
            val outbound0 = listPendingWorkOrdersRequest {
                this.context = request.context
                this.filter = ListPendingWorkOrdersRequestKt.filter {
                    this.createdAfter = request.serviceSchedule.audit.updatedAt
                    this.region = request.serviceSchedule.location.region
                }
            }
            val command0: Submission<ListPendingWorkOrdersRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = maintenanceWorkOrderApprovalService.listPendingWorkOrders(payload0)
            val outbound1 = rejectFuelPolicyRequest {
                this.context = request.context
                this.details = RejectFuelPolicyRequestKt.details {
                    this.approver = userRef {
                        this.email = request.serviceSchedule.organizer.email
                    }
                }
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.etag = request.serviceSchedule.audit.etag
                    }
                    this.owner = userRef {
                        this.displayName = request.serviceSchedule.organizer.displayName
                    }
                }
            }
            val adapter1 = OutboundPort<RejectFuelPolicyRequest, RejectFuelPolicyResponse> { payload ->
                fuelFuelPolicyApprovalService.rejectFuelPolicy(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            var reply2 = QuoteWorkOrderResponse.getDefaultInstance()
            request.serviceSchedule.participantsList.asFlow().map { item ->
                val outbound2 = quoteWorkOrderRequest {
                    this.context = request.context
                    this.workOrder = workOrder {
                        this.requester = contactInfo {
                            this.phone = item.contact.phone
                        }
                    }
                }
                maintenanceWorkOrderCalculationService.quoteWorkOrder(outbound2)
            }.collect { reply2 = it }
            var reply3 = FindAuditEventsResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<ServiceScheduleParticipant>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound3 = findAuditEventsRequest {
                            this.context = request.context
                            this.filter = FindAuditEventsRequestKt.filter {
                                this.ownerId = item.participantId
                            }
                        }
                        reply3 = auditAuditEventQueryService.findAuditEvents(outbound3)
                    }
                }
                try {
                    request.serviceSchedule.participantsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            pauseServiceScheduleWorkflowResponse {
                this.outcome = PauseServiceScheduleWorkflowResponseKt.outcome {
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.message = reply1.outcome.message
                    this.status = "ok"
                }
                this.serviceSchedule = serviceSchedule {
                    this.metadata.putAll(request.serviceSchedule.metadataMap)
                    this.participants.addAll(listOf(serviceScheduleParticipant {
                        this.contact = contactInfo {
                            this.email = reply1.fuelPolicy.owner.email
                        }
                    }))
                    this.serviceScheduleId = request.serviceScheduleId
                }
            }
        }

    /** Resumes a service schedule. */
    override suspend fun resumeServiceScheduleWorkflow(request: ResumeServiceScheduleWorkflowRequest): ResumeServiceScheduleWorkflowResponse {
        return handleResumeServiceScheduleWorkflow(request)
    }

    /** Retrys a service schedule. */
    override suspend fun retryServiceScheduleWorkflow(request: RetryServiceScheduleWorkflowRequest): RetryServiceScheduleWorkflowResponse = handleRetryServiceScheduleWorkflow(request)

    /** Signals a service schedule. */
    override suspend fun signalServiceScheduleWorkflow(request: SignalServiceScheduleWorkflowRequest): SignalServiceScheduleWorkflowResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleWorkflowService/SignalServiceScheduleWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SignalServiceScheduleWorkflow", request)
            log.atInfo().addKeyValue("operation", "SignalServiceScheduleWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.serviceSchedule.serviceScheduleId).log("Recorded audit detail")
            val outbound0 = estimateLineageNodeRequest {
                this.context = request.context
                this.lineageNode = lineageNode {
                    this.children.addAll(listOf(lineageNode {
                        this.children.addAll(listOf(lineageNode {
                            this.location = postalAddress {
                                this.locality = request.serviceSchedule.location.locality
                            }
                        }))
                    }))
                }
            }
            val reply0 = clientForSignalServiceScheduleWorkflow0.estimateLineageNode(outbound0)
            val outbound1 = previewLedgerAccountRequest {
                this.context = request.context
                this.ledgerAccount = ledgerAccount {
                    this.audit = auditInfo {
                        this.createdAt = reply0.lineageNode.audit.createdAt
                        this.updatedAt = request.serviceSchedule.audit.updatedAt
                    }
                    this.contact = contactInfo {
                        this.email = request.serviceSchedule.organizer.email
                    }
                }
            }
            val reply1 = flowOf(outbound1).map { value -> ledgerLedgerAccountValidationService.previewLedgerAccount(value) }.single()
            val outbound2 = batchGetAuditEventsRequest {
                this.context = request.context
            }
            val reply2 = withTimeout(5_000) { auditAuditEventService.batchGetAuditEvents(outbound2) }
            signalServiceScheduleWorkflowResponse {
                this.outcome = SignalServiceScheduleWorkflowResponseKt.outcome {
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.message = reply2.result.message
                    this.status = reply2.result.status
                }
                this.serviceSchedule = serviceSchedule {
                    this.participants.addAll(listOf(serviceScheduleParticipant {
                        this.contact = contactInfo {
                            this.preferredChannel = reply1.ledgerAccount.contact.preferredChannel
                        }
                    }))
                    this.serviceScheduleId = request.serviceScheduleId
                    this.status = request.serviceSchedule.status
                }
            }
        }

    /** Starts a service schedule. */
    override suspend fun startServiceScheduleWorkflow(request: StartServiceScheduleWorkflowRequest): StartServiceScheduleWorkflowResponse {
        return handleStartServiceScheduleWorkflow(request)
    }

    private suspend fun handleAdvanceServiceScheduleWorkflow(request: AdvanceServiceScheduleWorkflowRequest): AdvanceServiceScheduleWorkflowResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleWorkflowService/AdvanceServiceScheduleWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "AdvanceServiceScheduleWorkflow", request)
            log.atInfo().addKeyValue("operation", "AdvanceServiceScheduleWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.serviceSchedule.serviceScheduleId).log("Recorded audit detail")
            val configure0: AdvanceWorkOrderWorkflowRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.workOrder = workOrder {
                    this.assignee = userRef {
                        this.email = request.serviceSchedule.organizer.email
                    }
                    this.audit = auditInfo {
                        this.updatedBy = request.serviceSchedule.audit.updatedBy
                    }
                    this.requester = contactInfo {
                        this.name = request.details.notify.name
                        this.phone = request.details.notify.phone
                    }
                }
            }
            val outbound0 = advanceWorkOrderWorkflowRequest(configure0)
            val reply0 = maintenanceWorkOrderWorkflowService.advanceWorkOrderWorkflow(outbound0)
            val outbound1 = retryFuelTransactionImportRequest {
                this.context = request.context
                this.details = RetryFuelTransactionImportRequestKt.details {
                    this.notify = contactInfo {
                        this.email = request.details.notify.email
                        this.preferredChannel = reply0.workOrder.requester.preferredChannel
                    }
                }
                this.fuelTransaction = fuelTransaction {
                    this.subject = customerRef {
                        this.displayName = request.serviceSchedule.organizer.displayName
                        this.email = reply0.workOrder.assignee.email
                    }
                }
            }
            val reply1 = fuelFuelTransactionImportService.retryFuelTransactionImport(outbound1)
            var reply2 = SimulateDriverResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.serviceSchedule.participantsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound2 = simulateDriverRequest {
                                this.context = request.context
                                this.driver = driver {
                                    this.contact = contactInfo {
                                        this.phone = item.contact.phone
                                    }
                                }
                            }
                            driversDriverCalculationService.simulateDriver(outbound2)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply2 = it }
                }
            }
            advanceServiceScheduleWorkflowResponse {
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = "ok"
                }
                this.serviceSchedule = serviceSchedule {
                    this.audit = auditInfo {
                        this.createdAt = reply0.workOrder.audit.createdAt
                    }
                    this.metadata.putAll(request.serviceSchedule.metadataMap)
                    this.organizer = userRef {
                        this.email = reply1.fuelTransaction.subject.email
                    }
                    this.serviceScheduleId = request.serviceScheduleId
                }
            }
        }

    private fun CancelServiceScheduleWorkflowRequest.toExplainDriverCalculationFromCancelServiceScheduleWorkflow1(): ExplainDriverCalculationRequest {
        val source = this
        return explainDriverCalculationRequest {
            this.context = source.context
            this.driver = driver {
                this.audit = auditInfo {
                    this.updatedAt = source.serviceSchedule.audit.updatedAt
                }
            }
        }
    }

    private suspend fun handleInspectServiceScheduleWorkflow(request: InspectServiceScheduleWorkflowRequest): InspectServiceScheduleWorkflowResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleWorkflowService/InspectServiceScheduleWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "InspectServiceScheduleWorkflow", request)
            log.atInfo().addKeyValue("operation", "InspectServiceScheduleWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            inspectServiceScheduleWorkflowResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.serviceSchedule = serviceSchedule {
                    this.serviceScheduleId = request.serviceScheduleId
                }
            }
        }

    private data class ListServiceScheduleWorkflows1Command(
        val context: RequestContext,
        val detailsDestinationRegion: String
    ) {
        fun toRequest(): RecalculateCryptoKeysRequest {
            val command = this
            return recalculateCryptoKeysRequest {
                this.context = command.context
                this.details = RecalculateCryptoKeysRequestKt.details {
                    this.destination = postalAddress {
                        this.region = command.detailsDestinationRegion
                    }
                }
            }
        }
    }

    private suspend fun handleListServiceScheduleWorkflows(request: ListServiceScheduleWorkflowsRequest): ListServiceScheduleWorkflowsResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleWorkflowService/ListServiceScheduleWorkflows") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListServiceScheduleWorkflows", request)
            log.atInfo().addKeyValue("operation", "ListServiceScheduleWorkflows").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = listPartReportsRequest {
                this.context = request.context
                this.filter = ListPartReportsRequestKt.filter {
                    this.region = request.filter.region
                }
                this.page = pageRequest {
                    this.pageSize = request.page.pageSize
                    this.pageToken = request.page.pageToken
                }
            }
            val reply0 = maintenancePartReportingService.listPartReports(WireCopies.json(outbound0))
            val outbound1 = ListServiceScheduleWorkflows1Command(
                context = request.context,
                detailsDestinationRegion = request.filter.region
            ).toRequest()
            val reply1 = keymanagementCryptoKeyCalculationService.recalculateCryptoKeys(outbound1)
            listServiceScheduleWorkflowsResponse {
                this.result = operationResult {
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = reply0.result.status
                }
                this.serviceSchedules.addAll(listOf(serviceSchedule {
                    this.organizer = userRef {
                        this.email = reply1.cryptoKey.contact.email
                    }
                }))
            }
        }

    private suspend fun handleResumeServiceScheduleWorkflow(request: ResumeServiceScheduleWorkflowRequest): ResumeServiceScheduleWorkflowResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleWorkflowService/ResumeServiceScheduleWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResumeServiceScheduleWorkflow", request)
            log.atInfo().addKeyValue("operation", "ResumeServiceScheduleWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.serviceSchedule.serviceScheduleId).log("Recorded audit detail")
            val outbound0 = SummarizeVenuesRequest.newBuilder().apply {
                putAllAnnotations(request.annotationsMap)
                setContext(request.context)
                setReason(request.reason)
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> venuesVenueQueryService.summarizeVenues(value) }
            val outbound1 = RequestFactory.create {
                lintDriverLicensesRequest {
                    this.context = request.context
                    this.details = LintDriverLicensesRequestKt.details {
                        this.contact = contactInfo {
                            this.email = request.details.notify.email
                            this.name = request.details.notify.name
                        }
                    }
                    this.driverLicense = driverLicense {
                        this.audit = auditInfo {
                            this.createdAt = request.serviceSchedule.audit.createdAt
                        }
                        this.billingAddress = postalAddress {
                            this.region = request.serviceSchedule.location.region
                        }
                    }
                }
            }
            val reply1 = driversDriverLicenseValidationService.lintDriverLicenses(outbound1)
            val outbound2 = listPendingServiceSchedulesRequest {
                this.context = request.context
                this.filter = ListPendingServiceSchedulesRequestKt.filter {
                    this.region = request.serviceSchedule.location.region
                }
            }
            val reply2 = ClientRegistry.lookup("acme.maintenance.v1.ServiceScheduleApprovalService", ServiceScheduleApprovalServiceGrpcKt.ServiceScheduleApprovalServiceCoroutineStub::class.java).listPendingServiceSchedules(outbound2)
            resumeServiceScheduleWorkflowResponse {
                this.outcome = ResumeServiceScheduleWorkflowResponseKt.outcome {
                    this.status = reply2.result.status
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply1.outcome.message
                    this.status = "ok"
                }
                this.serviceSchedule = serviceSchedule {
                    this.location = request.serviceSchedule.location
                    this.serviceScheduleId = request.serviceScheduleId
                }
            }
        }

    private suspend fun handleRetryServiceScheduleWorkflow(request: RetryServiceScheduleWorkflowRequest): RetryServiceScheduleWorkflowResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleWorkflowService/RetryServiceScheduleWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RetryServiceScheduleWorkflow", request)
            log.atInfo().addKeyValue("operation", "RetryServiceScheduleWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.serviceSchedule.serviceScheduleId).log("Recorded audit detail")
            val outbound0 = explainDriverLicenseDecisionRequest {
                this.context = request.context
                this.details = ExplainDriverLicenseDecisionRequestKt.details {
                    this.contact = contactInfo {
                        this.name = request.details.notify.name
                    }
                }
                this.driverLicense = driverLicense {
                    this.billingAddress = postalAddress {
                        this.countryCode = request.serviceSchedule.location.countryCode
                        this.lines.addAll(request.serviceSchedule.location.linesList)
                    }
                }
            }
            val reply0 = driversDriverLicenseValidationService.explainDriverLicenseDecision(WireCopies.reflective(outbound0))
            val outbound1 = calculateDriverRequest {
                this.context = request.context
                this.driver = driver {
                    this.audit = auditInfo {
                        this.updatedAt = request.serviceSchedule.audit.updatedAt
                    }
                }
            }
            val reply1 = workerForRetryServiceScheduleWorkflow1.submit(outbound1)
            retryServiceScheduleWorkflowResponse {
                this.result = operationResult {
                    this.status = reply0.result.status
                }
                this.serviceSchedule = serviceSchedule {
                    this.audit = auditInfo {
                        this.createdBy = reply1.driver.audit.createdBy
                    }
                    this.organizer = request.serviceSchedule.organizer
                    this.serviceScheduleId = request.serviceScheduleId
                }
            }
        }

    private suspend fun handleStartServiceScheduleWorkflow(request: StartServiceScheduleWorkflowRequest): StartServiceScheduleWorkflowResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleWorkflowService/StartServiceScheduleWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StartServiceScheduleWorkflow", request)
            log.atInfo().addKeyValue("operation", "StartServiceScheduleWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.serviceSchedule.serviceScheduleId).log("Recorded audit detail")
            val configure0: ValidateFuelTransactionImportRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.details = ValidateFuelTransactionImportRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.details.notify.email
                    }
                }
                this.fuelTransaction = fuelTransaction {
                    this.subject = customerRef {
                        this.displayName = request.serviceSchedule.organizer.displayName
                        this.email = request.serviceSchedule.organizer.email
                    }
                }
            }
            val outbound0 = validateFuelTransactionImportRequest(configure0)
            val reply0 = fuelFuelTransactionImportService.validateFuelTransactionImport(outbound0)
            startServiceScheduleWorkflowResponse {
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
                this.serviceSchedule = serviceSchedule {
                    this.metadata.putAll(request.serviceSchedule.metadataMap)
                    this.serviceScheduleId = request.serviceScheduleId
                }
            }
        }
}
