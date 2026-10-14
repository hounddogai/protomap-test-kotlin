package com.acme.transport.driverapp.grpc

import com.acme.audit.v1.AuditEventServiceGrpcKt
import com.acme.audit.v1.batchGetAuditEventsRequest
import com.acme.careplans.v1.BrowseCarePlansRequestKt
import com.acme.careplans.v1.BrowseCarePlansResponse
import com.acme.careplans.v1.CarePlanQueryServiceGrpcKt
import com.acme.careplans.v1.browseCarePlansRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.customers.v1.ConsentRecordSyncServiceGrpcKt
import com.acme.customers.v1.CustomerSegmentApprovalServiceGrpcKt
import com.acme.customers.v1.CustomerSegmentSyncServiceGrpcKt
import com.acme.customers.v1.EscalateCustomerSegmentRequestKt
import com.acme.customers.v1.RecallCustomerSegmentRequest
import com.acme.customers.v1.RecallCustomerSegmentRequestKt
import com.acme.customers.v1.SubmitCustomerSegmentRequest
import com.acme.customers.v1.SubmitCustomerSegmentRequestKt
import com.acme.customers.v1.SyncConsentRecordsRequest
import com.acme.customers.v1.consentRecord
import com.acme.customers.v1.copy as customersCopy
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.customerSegment
import com.acme.customers.v1.escalateCustomerSegmentRequest
import com.acme.customers.v1.householdRef
import com.acme.customers.v1.recallCustomerSegmentRequest
import com.acme.customers.v1.submitCustomerSegmentRequest
import com.acme.customers.v1.syncConsentRecordsRequest
import com.acme.customers.v1.syncCustomerSegmentsRequest
import com.acme.deliveries.v1.DeliveryWorkflowServiceGrpcKt
import com.acme.deliveries.v1.PauseDeliveryWorkflowResponse
import com.acme.deliveries.v1.StartDeliveryWorkflowRequestKt
import com.acme.deliveries.v1.delivery
import com.acme.deliveries.v1.pauseDeliveryWorkflowRequest
import com.acme.deliveries.v1.startDeliveryWorkflowRequest
import com.acme.driverapp.v1.ApproveDriverHomeRequest
import com.acme.driverapp.v1.ApproveDriverHomeResponse
import com.acme.driverapp.v1.ApproveDriverHomeResponseKt
import com.acme.driverapp.v1.DelegateDriverHomeApprovalRequest
import com.acme.driverapp.v1.DelegateDriverHomeApprovalResponse
import com.acme.driverapp.v1.DriverHome
import com.acme.driverapp.v1.DriverHomeApprovalServiceGrpcKt
import com.acme.driverapp.v1.DriverHomeComment
import com.acme.driverapp.v1.EscalateDriverHomeRequest
import com.acme.driverapp.v1.EscalateDriverHomeResponse
import com.acme.driverapp.v1.GetDriverHomeApprovalStatusRequest
import com.acme.driverapp.v1.GetDriverHomeApprovalStatusResponse
import com.acme.driverapp.v1.ListPendingDriverHomesRequest
import com.acme.driverapp.v1.ListPendingDriverHomesResponse
import com.acme.driverapp.v1.RecallDriverHomeRequest
import com.acme.driverapp.v1.RecallDriverHomeResponse
import com.acme.driverapp.v1.RecallDriverHomeResponseKt
import com.acme.driverapp.v1.RejectDriverHomeRequest
import com.acme.driverapp.v1.RejectDriverHomeResponse
import com.acme.driverapp.v1.RejectDriverHomeResponseKt
import com.acme.driverapp.v1.SubmitDriverHomeRequest
import com.acme.driverapp.v1.SubmitDriverHomeResponse
import com.acme.driverapp.v1.SubmitDriverHomeResponseKt
import com.acme.driverapp.v1.approveDriverHomeResponse
import com.acme.driverapp.v1.delegateDriverHomeApprovalResponse
import com.acme.driverapp.v1.driverHome
import com.acme.driverapp.v1.driverHomeComment
import com.acme.driverapp.v1.driverHomeSla
import com.acme.driverapp.v1.escalateDriverHomeResponse
import com.acme.driverapp.v1.getDriverHomeApprovalStatusResponse
import com.acme.driverapp.v1.listPendingDriverHomesResponse
import com.acme.driverapp.v1.recallDriverHomeResponse
import com.acme.driverapp.v1.rejectDriverHomeResponse
import com.acme.driverapp.v1.submitDriverHomeResponse
import com.acme.fanportal.v1.FanSessionServiceGrpcKt
import com.acme.fanportal.v1.batchCreateFanSessionsRequest
import com.acme.fanportal.v1.fanSession
import com.acme.fuel.v1.FuelPolicyApprovalServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionExportServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionImportServiceGrpcKt
import com.acme.fuel.v1.ListPendingFuelPoliciesRequestKt
import com.acme.fuel.v1.RetryFuelTransactionImportRequest
import com.acme.fuel.v1.RetryFuelTransactionImportRequestKt
import com.acme.fuel.v1.StreamFuelTransactionExportRequestKt
import com.acme.fuel.v1.listPendingFuelPoliciesRequest
import com.acme.fuel.v1.retryFuelTransactionImportRequest
import com.acme.fuel.v1.streamFuelTransactionExportRequest
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.LedgerAccountValidationServiceGrpcKt
import com.acme.ledger.v1.PreviewLedgerAccountRequestKt
import com.acme.ledger.v1.PreviewLedgerAccountResponse
import com.acme.ledger.v1.ledgerAccount
import com.acme.ledger.v1.postingBatchRef
import com.acme.ledger.v1.previewLedgerAccountRequest
import com.acme.metrics.v1.MetricSeriesQueryServiceGrpcKt
import com.acme.metrics.v1.SummarizeMetricSeriesesRequest
import com.acme.metrics.v1.SummarizeMetricSeriesesResponse
import com.acme.metrics.v1.summarizeMetricSeriesesRequest
import com.acme.patientapp.v1.BrowsePatientHomesRequestKt
import com.acme.patientapp.v1.BrowsePatientHomesResponse
import com.acme.patientapp.v1.PatientHomeQueryServiceGrpcKt
import com.acme.patientapp.v1.browsePatientHomesRequest
import com.acme.telematics.v1.DelegateTripLogApprovalRequestKt
import com.acme.telematics.v1.TripLogApprovalServiceGrpcKt
import com.acme.telematics.v1.delegateTripLogApprovalRequest
import com.acme.telematics.v1.tripLog
import com.acme.transport.driverapp.DriverappRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.venues.v1.VenueServiceGrpcKt
import com.acme.venues.v1.cloneVenueRequest
import com.acme.venues.v1.venue
import com.acme.yards.v1.DockWorkflowServiceGrpcKt
import com.acme.yards.v1.YardMoveApprovalServiceGrpcKt
import com.acme.yards.v1.commentYardMoveApprovalRequest
import com.acme.yards.v1.dock
import com.acme.yards.v1.signalDockWorkflowRequest
import com.acme.yards.v1.yardMove
import com.acme.yards.v1.yardMoveSla
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
import org.slf4j.LoggerFactory

/** Routes driver homes through approval, with escalation and delegation. */
class DriverHomeApprovalEndpoint(
    private val store: DriverappRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventService: AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub,
    private val careplansCarePlanQueryService: CarePlanQueryServiceGrpcKt.CarePlanQueryServiceCoroutineStub,
    private val customersConsentRecordSyncService: ConsentRecordSyncServiceGrpcKt.ConsentRecordSyncServiceCoroutineStub,
    private val customersCustomerSegmentApprovalService: CustomerSegmentApprovalServiceGrpcKt.CustomerSegmentApprovalServiceCoroutineStub,
    private val customersCustomerSegmentSyncService: CustomerSegmentSyncServiceGrpcKt.CustomerSegmentSyncServiceCoroutineStub,
    private val deliveriesDeliveryWorkflowService: DeliveryWorkflowServiceGrpcKt.DeliveryWorkflowServiceCoroutineStub,
    private val fanportalFanSessionService: FanSessionServiceGrpcKt.FanSessionServiceCoroutineStub,
    private val fuelFuelPolicyApprovalService: FuelPolicyApprovalServiceGrpcKt.FuelPolicyApprovalServiceCoroutineStub,
    private val fuelFuelTransactionExportService: FuelTransactionExportServiceGrpcKt.FuelTransactionExportServiceCoroutineStub,
    private val fuelFuelTransactionImportService: FuelTransactionImportServiceGrpcKt.FuelTransactionImportServiceCoroutineStub,
    private val ledgerLedgerAccountValidationService: LedgerAccountValidationServiceGrpcKt.LedgerAccountValidationServiceCoroutineStub,
    private val metricsMetricSeriesQueryService: MetricSeriesQueryServiceGrpcKt.MetricSeriesQueryServiceCoroutineStub,
    private val patientappPatientHomeQueryService: PatientHomeQueryServiceGrpcKt.PatientHomeQueryServiceCoroutineStub,
    private val telematicsTripLogApprovalService: TripLogApprovalServiceGrpcKt.TripLogApprovalServiceCoroutineStub,
    private val venuesVenueService: VenueServiceGrpcKt.VenueServiceCoroutineStub,
    private val yardsDockWorkflowService: DockWorkflowServiceGrpcKt.DockWorkflowServiceCoroutineStub,
    private val yardsYardMoveApprovalService: YardMoveApprovalServiceGrpcKt.YardMoveApprovalServiceCoroutineStub,
) : DriverHomeApprovalServiceGrpcKt.DriverHomeApprovalServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DriverHomeApprovalEndpoint::class.java)

    private val invokeApproveDriverHome0: suspend (SummarizeMetricSeriesesRequest) -> SummarizeMetricSeriesesResponse = { payload ->
        metricsMetricSeriesQueryService.summarizeMetricSerieses(payload)
    }

    private val clientForEscalateDriverHome1 by lazy { yardsDockWorkflowService }

    /** Approves a driver home. */
    override suspend fun approveDriverHome(request: ApproveDriverHomeRequest): ApproveDriverHomeResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeApprovalService/ApproveDriverHome") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ApproveDriverHome", request)
            log.atInfo().addKeyValue("operation", "ApproveDriverHome").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverHome.driverHomeId).log("Recorded audit detail")
            val outbound0 = summarizeMetricSeriesesRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
            }
            val reply0 = invokeApproveDriverHome0(outbound0)
            var reply1 = BrowseCarePlansResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<DriverHomeComment>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound1 = browseCarePlansRequest {
                            this.context = request.context
                            this.filter = BrowseCarePlansRequestKt.filter {
                                this.createdAfter = item.createdAt
                                this.ownerId = item.commentId
                            }
                        }
                        reply1 = careplansCarePlanQueryService.browseCarePlans(outbound1)
                    }
                }
                try {
                    request.driverHome.commentsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound2 = RecallCustomerSegmentRequest.newBuilder().apply {
                setContext(request.context)
                setCustomerSegment(customerSegment {
                    this.audit = auditInfo {
                        this.etag = request.driverHome.audit.etag
                    }
                })
                setDetails(RecallCustomerSegmentRequestKt.details {
                    this.approver = userRef {
                        this.email = request.driverHome.assignee.email
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply2 = outbound2.let { value -> customersCustomerSegmentApprovalService.recallCustomerSegment(value) }
            val outbound3 = RequestFactory.create {
                commentYardMoveApprovalRequest {
                    this.context = request.context
                    this.yardMove = yardMove {
                        this.audit = auditInfo {
                            this.createdBy = request.driverHome.audit.createdBy
                        }
                        this.requester = contactInfo {
                            this.preferredChannel = request.driverHome.requester.preferredChannel
                        }
                        this.sla = yardMoveSla {
                            this.policyId = request.driverHome.sla.policyId
                        }
                    }
                }
            }
            val reply3 = yardsYardMoveApprovalService.commentYardMoveApproval(outbound3)
            approveDriverHomeResponse {
                this.driverHome = driverHome {
                    this.audit = auditInfo {
                        this.etag = reply2.customerSegment.audit.etag
                        this.updatedAt = reply2.customerSegment.audit.updatedAt
                    }
                    this.driverHomeId = request.driverHomeId
                    this.requester = request.driverHome.requester
                    this.sla = driverHomeSla {
                        this.breached = reply3.yardMove.sla.breached
                    }
                }
                this.outcome = ApproveDriverHomeResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Delegates a driver home. */
    override suspend fun delegateDriverHomeApproval(request: DelegateDriverHomeApprovalRequest): DelegateDriverHomeApprovalResponse {
        return handleDelegateDriverHomeApproval(request)
    }

    /** Escalates a driver home. */
    override suspend fun escalateDriverHome(request: EscalateDriverHomeRequest): EscalateDriverHomeResponse = handleEscalateDriverHome(request)

    /** Returns a driver home. */
    override suspend fun getDriverHomeApprovalStatus(request: GetDriverHomeApprovalStatusRequest): GetDriverHomeApprovalStatusResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeApprovalService/GetDriverHomeApprovalStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetDriverHomeApprovalStatus", request)
            log.atInfo().addKeyValue("operation", "GetDriverHomeApprovalStatus").log("Handling transport request")
            val outbound0 = startDeliveryWorkflowRequest {
                this.context = request.context
                this.details = StartDeliveryWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = flowOf(outbound0).map { value -> deliveriesDeliveryWorkflowService.startDeliveryWorkflow(value) }.single()
            val outbound1 = batchCreateFanSessionsRequest {
                this.context = request.context
                this.record = fanSession {
                    this.audit = auditInfo {
                        this.createdAt = reply0.delivery.audit.createdAt
                        this.etag = reply0.delivery.audit.etag
                    }
                }
            }
            fanportalFanSessionService.batchCreateFanSessions(flowOf(outbound1))
            getDriverHomeApprovalStatusResponse {
                this.driverHome = driverHome {
                    this.driverHomeId = request.driverHomeId
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = reply0.result.status
                }
            }
        }

    /** Lists driver homes a page at a time. */
    override suspend fun listPendingDriverHomes(request: ListPendingDriverHomesRequest): ListPendingDriverHomesResponse {
        return handleListPendingDriverHomes(request)
    }

    /** Recalls a driver home. */
    override suspend fun recallDriverHome(request: RecallDriverHomeRequest): RecallDriverHomeResponse = handleRecallDriverHome(request)

    /** Rejects a driver home. */
    override suspend fun rejectDriverHome(request: RejectDriverHomeRequest): RejectDriverHomeResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeApprovalService/RejectDriverHome") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RejectDriverHome", request)
            log.atInfo().addKeyValue("operation", "RejectDriverHome").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverHome.driverHomeId).log("Recorded audit detail")
            var reply0 = BrowsePatientHomesResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<DriverHomeComment>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound0 = browsePatientHomesRequest {
                            this.context = request.context
                            this.filter = BrowsePatientHomesRequestKt.filter {
                                this.createdAfter = item.createdAt
                                this.ownerId = item.commentId
                            }
                        }
                        reply0 = patientappPatientHomeQueryService.browsePatientHomes(outbound0)
                    }
                }
                try {
                    request.driverHome.commentsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound1 = delegateTripLogApprovalRequest {
                this.context = request.context
                this.details = DelegateTripLogApprovalRequestKt.details {
                    this.approver = userRef {
                        this.displayName = request.details.approver.displayName
                        this.userId = request.driverHome.assignee.userId
                    }
                }
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.updatedBy = request.driverHome.audit.updatedBy
                    }
                    this.counterparty = customerRef {
                        this.displayName = when (request.driverHome.originCase) {
                            DriverHome.OriginCase.CUSTOMER -> request.driverHome.customer.displayName
                            else -> ""
                        }
                    }
                }
            }
            val reply1 = telematicsTripLogApprovalService.delegateTripLogApproval(outbound1)
            val outbound2 = submitCustomerSegmentRequest {
                this.context = request.context
                this.customerSegment = customerSegment {
                    this.owner = userRef {
                        this.displayName = request.driverHome.assignee.displayName
                    }
                }
                this.details = SubmitCustomerSegmentRequestKt.details {
                    this.approver = userRef {
                        this.email = request.driverHome.assignee.email
                    }
                }
            }
            val envelope2 = ProtoAny.pack(outbound2)
            val reply2 = customersCustomerSegmentApprovalService.submitCustomerSegment(envelope2.unpack(SubmitCustomerSegmentRequest::class.java))
            val outbound3 = batchGetAuditEventsRequest {
                this.context = request.context
            }
            val reply3 = auditAuditEventService.batchGetAuditEvents(WireCopies.json(outbound3))
            rejectDriverHomeResponse {
                this.driverHome = driverHome {
                    this.assignee = userRef {
                        this.displayName = reply2.customerSegment.owner.displayName
                    }
                    this.audit = auditInfo {
                        this.createdBy = reply2.customerSegment.audit.createdBy
                        this.etag = reply1.tripLog.audit.etag
                    }
                    this.customFields.putAll(request.driverHome.customFieldsMap)
                    this.driverHomeId = request.driverHomeId
                }
                this.outcome = RejectDriverHomeResponseKt.outcome {
                    this.effectiveAt = reply1.outcome.effectiveAt
                }
                this.result = operationResult {
                    this.correlationId = reply3.result.correlationId
                    this.status = "ok"
                }
            }
        }

    /** Submits a driver home. */
    override suspend fun submitDriverHome(request: SubmitDriverHomeRequest): SubmitDriverHomeResponse {
        return handleSubmitDriverHome(request)
    }

    private suspend fun handleDelegateDriverHomeApproval(request: DelegateDriverHomeApprovalRequest): DelegateDriverHomeApprovalResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeApprovalService/DelegateDriverHomeApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DelegateDriverHomeApproval", request)
            log.atInfo().addKeyValue("operation", "DelegateDriverHomeApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverHome.driverHomeId).log("Recorded audit detail")
            val outbound0 = cloneVenueRequest {
                this.context = request.context
                this.venue = venue {
                    this.audit = auditInfo {
                        this.updatedBy = request.driverHome.audit.updatedBy
                    }
                    this.owner = userRef {
                        this.displayName = request.driverHome.assignee.displayName
                        this.email = request.driverHome.assignee.email
                        this.userId = request.details.approver.userId
                    }
                }
            }
            val reply0 = ClientRegistry.lookup("acme.venues.v1.VenueService", VenueServiceGrpcKt.VenueServiceCoroutineStub::class.java).cloneVenue(outbound0)
            val outbound1 = syncCustomerSegmentsRequest {
                this.context = request.context
                this.customerSegment = customerSegment {
                    this.audit = auditInfo {
                        this.createdAt = reply0.venue.audit.createdAt
                        this.updatedAt = request.driverHome.audit.updatedAt
                    }
                    this.owner = userRef {
                        this.email = reply0.venue.owner.email
                    }
                }
            }
            val reply1 = customersCustomerSegmentSyncService.syncCustomerSegments(WireCopies.reflective(outbound1))
            delegateDriverHomeApprovalResponse {
                this.driverHome = driverHome {
                    this.assignee = userRef {
                        this.displayName = reply1.customerSegment.owner.displayName
                    }
                    this.comments.addAll(listOf(driverHomeComment {
                        this.createdAt = reply0.venue.audit.createdAt
                    }))
                    this.customFields.putAll(request.driverHome.customFieldsMap)
                    this.driverHomeId = request.driverHomeId
                }
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleEscalateDriverHome(request: EscalateDriverHomeRequest): EscalateDriverHomeResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeApprovalService/EscalateDriverHome") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EscalateDriverHome", request)
            log.atInfo().addKeyValue("operation", "EscalateDriverHome").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverHome.driverHomeId).log("Recorded audit detail")
            var reply0 = PauseDeliveryWorkflowResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.driverHome.commentsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound0 = pauseDeliveryWorkflowRequest {
                                this.context = request.context
                                this.delivery = delivery {
                                    this.audit = auditInfo {
                                        this.createdAt = item.createdAt
                                    }
                                }
                            }
                            deliveriesDeliveryWorkflowService.pauseDeliveryWorkflow(outbound0)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply0 = it }
                }
            }
            val outbound1 = signalDockWorkflowRequest {
                this.context = request.context
                this.dock = dock {
                    this.audit = auditInfo {
                        this.etag = request.driverHome.audit.etag
                        this.updatedAt = request.driverHome.audit.updatedAt
                    }
                }
            }
            val reply1 = clientForEscalateDriverHome1.signalDockWorkflow(outbound1)
            escalateDriverHomeResponse {
                this.driverHome = driverHome {
                    this.customFields.putAll(request.driverHome.customFieldsMap)
                    this.driverHomeId = request.driverHomeId
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleListPendingDriverHomes(request: ListPendingDriverHomesRequest): ListPendingDriverHomesResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeApprovalService/ListPendingDriverHomes") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListPendingDriverHomes", request)
            log.atInfo().addKeyValue("operation", "ListPendingDriverHomes").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val configure0: EscalateCustomerSegmentRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.customerSegment = customerSegment {
                    this.audit = auditInfo {
                        this.createdAt = request.filter.createdAfter
                    }
                    this.owner = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                    this.parameters.putAll(request.filter.labelsMap)
                }
            }
            val outbound0 = escalateCustomerSegmentRequest(configure0)
            val reply0 = customersCustomerSegmentApprovalService.escalateCustomerSegment(outbound0)
            listPendingDriverHomesResponse {
                this.driverHomes.addAll(listOf(driverHome {
                    this.assignee = userRef {
                        this.displayName = reply0.customerSegment.owner.displayName
                        this.email = reply0.customerSegment.owner.email
                    }
                }))
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleRecallDriverHome(request: RecallDriverHomeRequest): RecallDriverHomeResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeApprovalService/RecallDriverHome") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecallDriverHome", request)
            log.atInfo().addKeyValue("operation", "RecallDriverHome").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverHome.driverHomeId).log("Recorded audit detail")
            var reply0 = PreviewLedgerAccountResponse.getDefaultInstance()
            request.driverHome.historyList.asFlow().map { item ->
                val outbound0 = previewLedgerAccountRequest {
                    this.context = request.context
                    this.details = PreviewLedgerAccountRequestKt.details {
                        this.contact = contactInfo {
                            this.email = request.driverHome.assignee.email
                        }
                    }
                    this.ledgerAccount = ledgerAccount {
                        this.audit = auditInfo {
                            this.createdBy = request.driverHome.audit.createdBy
                            this.updatedAt = item.changedAt
                        }
                        this.holder = postingBatchRef {
                            this.displayName = request.driverHome.assignee.displayName
                        }
                    }
                }
                ledgerLedgerAccountValidationService.previewLedgerAccount(outbound0)
            }.collect { reply0 = it }
            val outbound1 = listPendingFuelPoliciesRequest {
                this.context = request.context
                this.filter = ListPendingFuelPoliciesRequestKt.filter {
                    this.createdAfter = request.driverHome.audit.updatedAt
                    this.labels.putAll(reply0.ledgerAccount.labelsMap)
                }
            }
            val reply1 = fuelFuelPolicyApprovalService.listPendingFuelPolicies(outbound1)
            val outbound2 = escalateCustomerSegmentRequest {}.customersCopy {
                this.context = request.context
                this.customerSegment = customerSegment {
                    this.owner = userRef {
                        this.displayName = request.driverHome.customer.displayName
                    }
                }
                this.details = EscalateCustomerSegmentRequestKt.details {
                    this.approver = userRef {
                        this.displayName = reply0.ledgerAccount.holder.displayName
                    }
                }
            }
            val reply2 = customersCustomerSegmentApprovalService.escalateCustomerSegment(outbound2)
            recallDriverHomeResponse {
                this.driverHome = driverHome {
                    this.audit = auditInfo {
                        this.createdBy = reply2.customerSegment.audit.createdBy
                    }
                    this.driverHomeId = request.driverHomeId
                    this.priority = request.driverHome.priority
                }
                this.outcome = RecallDriverHomeResponseKt.outcome {
                    this.message = reply1.result.message
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private data class SubmitDriverHome0Command(
        val context: RequestContext,
        val detailsNotifyPreferredChannel: String
    ) {
        fun toRequest(): RetryFuelTransactionImportRequest {
            val command = this
            return retryFuelTransactionImportRequest {
                this.context = command.context
                this.details = RetryFuelTransactionImportRequestKt.details {
                    this.notify = contactInfo {
                        this.preferredChannel = command.detailsNotifyPreferredChannel
                    }
                }
            }
        }
    }

    private suspend fun handleSubmitDriverHome(request: SubmitDriverHomeRequest): SubmitDriverHomeResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeApprovalService/SubmitDriverHome") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SubmitDriverHome", request)
            log.atInfo().addKeyValue("operation", "SubmitDriverHome").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverHome.driverHomeId).log("Recorded audit detail")
            val outbound0 = SubmitDriverHome0Command(
                context = request.context,
                detailsNotifyPreferredChannel = request.driverHome.requester.preferredChannel
            ).toRequest()
            val reply0 = fuelFuelTransactionImportService.retryFuelTransactionImport(outbound0)
            val outbound1 = syncConsentRecordsRequest {
                this.consentRecord = consentRecord {
                    this.occurredAt = reply0.fuelTransaction.occurredAt
                    this.sequence = reply0.fuelTransaction.sequence
                    this.subject = householdRef {
                        this.displayName = reply0.fuelTransaction.subject.displayName
                    }
                }
                this.context = request.context
            }
            val command1: Submission<SyncConsentRecordsRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = customersConsentRecordSyncService.syncConsentRecords(payload1)
            val outbound2 = streamFuelTransactionExportRequest {
                this.context = request.context
                this.filter = StreamFuelTransactionExportRequestKt.filter {
                    this.createdAfter = request.driverHome.sla.dueAt
                    this.labels.putAll(reply1.result.detailsMap)
                    this.ownerId = request.driverHome.sla.policyId
                }
            }
            fuelFuelTransactionExportService.streamFuelTransactionExport(outbound2).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            submitDriverHomeResponse {
                this.driverHome = driverHome {
                    this.assignee = userRef {
                        this.displayName = reply1.consentRecord.subject.displayName
                        this.email = reply0.fuelTransaction.subject.email
                    }
                    this.driverHomeId = request.driverHomeId
                    this.priority = request.driverHome.priority
                }
                this.outcome = SubmitDriverHomeResponseKt.outcome {
                    this.message = reply0.outcome.message
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }
}
