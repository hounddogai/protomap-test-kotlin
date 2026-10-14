package com.acme.transport.driverapp.api

import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.ConsentRecordApprovalServiceGrpcKt
import com.acme.customers.v1.ConsentRecordSyncServiceGrpcKt
import com.acme.customers.v1.ConsentRecordWorkflowServiceGrpcKt
import com.acme.customers.v1.CustomerWorkflowServiceGrpcKt
import com.acme.customers.v1.ListPendingConsentRecordsRequestKt
import com.acme.customers.v1.PullConsentRecordsResponse
import com.acme.customers.v1.RetryCustomerWorkflowRequestKt
import com.acme.customers.v1.StartConsentRecordWorkflowRequestKt
import com.acme.customers.v1.consentRecord
import com.acme.customers.v1.consentRecordMeasurement
import com.acme.customers.v1.customer
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.householdRef
import com.acme.customers.v1.listPendingConsentRecordsRequest
import com.acme.customers.v1.pullConsentRecordsRequest
import com.acme.customers.v1.retryCustomerWorkflowRequest
import com.acme.customers.v1.startConsentRecordWorkflowRequest
import com.acme.dispatch.v1.DispatchAssignmentApprovalServiceGrpcKt
import com.acme.dispatch.v1.EscalateDispatchAssignmentRequestKt
import com.acme.dispatch.v1.dispatchAssignment
import com.acme.dispatch.v1.dispatchAssignmentComment
import com.acme.dispatch.v1.escalateDispatchAssignmentRequest
import com.acme.driverapp.v1.AdvanceDriverHomeWorkflowRequest
import com.acme.driverapp.v1.AdvanceDriverHomeWorkflowResponse
import com.acme.driverapp.v1.CancelDriverHomeWorkflowRequest
import com.acme.driverapp.v1.CancelDriverHomeWorkflowResponse
import com.acme.driverapp.v1.CommentDriverInboxApprovalRequest
import com.acme.driverapp.v1.DelegateDriverHomeApprovalRequestKt
import com.acme.driverapp.v1.DeleteDriverHomeReportRequest
import com.acme.driverapp.v1.DeleteDriverHomeReportResponse
import com.acme.driverapp.v1.DriverHomeApprovalServiceGrpcKt
import com.acme.driverapp.v1.DriverHomeReportingServiceGrpcKt
import com.acme.driverapp.v1.DriverHomeStatusChange
import com.acme.driverapp.v1.DriverHomeWorkflowServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxApprovalServiceGrpcKt
import com.acme.driverapp.v1.EscalateDriverInboxRequest
import com.acme.driverapp.v1.EscalateDriverInboxRequestKt
import com.acme.driverapp.v1.EscalateDriverInboxResponse
import com.acme.driverapp.v1.InspectDriverHomeWorkflowRequest
import com.acme.driverapp.v1.InspectDriverHomeWorkflowResponse
import com.acme.driverapp.v1.ListPendingDriverInboxesRequestKt
import com.acme.driverapp.v1.ListPendingDriverInboxesResponse
import com.acme.driverapp.v1.PauseDriverHomeWorkflowRequest
import com.acme.driverapp.v1.PauseDriverHomeWorkflowResponse
import com.acme.driverapp.v1.ResumeDriverHomeWorkflowRequest
import com.acme.driverapp.v1.ResumeDriverHomeWorkflowResponse
import com.acme.driverapp.v1.ResumeDriverHomeWorkflowResponseKt
import com.acme.driverapp.v1.RetryDriverHomeWorkflowRequest
import com.acme.driverapp.v1.RetryDriverHomeWorkflowResponse
import com.acme.driverapp.v1.RetryDriverHomeWorkflowResponseKt
import com.acme.driverapp.v1.ScheduleDriverHomeReportRequest
import com.acme.driverapp.v1.ScheduleDriverHomeReportRequestKt
import com.acme.driverapp.v1.SignalDriverHomeWorkflowRequest
import com.acme.driverapp.v1.SignalDriverHomeWorkflowResponse
import com.acme.driverapp.v1.SignalDriverHomeWorkflowResponseKt
import com.acme.driverapp.v1.StartDriverHomeWorkflowRequest
import com.acme.driverapp.v1.StartDriverHomeWorkflowResponse
import com.acme.driverapp.v1.StartDriverHomeWorkflowResponseKt
import com.acme.driverapp.v1.SubmitDriverHomeResponse
import com.acme.driverapp.v1.SubmitDriverInboxRequest
import com.acme.driverapp.v1.SubmitDriverInboxRequestKt
import com.acme.driverapp.v1.SubmitDriverInboxResponse
import com.acme.driverapp.v1.advanceDriverHomeWorkflowResponse
import com.acme.driverapp.v1.cancelDriverHomeWorkflowResponse
import com.acme.driverapp.v1.commentDriverInboxApprovalRequest
import com.acme.driverapp.v1.delegateDriverHomeApprovalRequest
import com.acme.driverapp.v1.delegateDriverInboxApprovalRequest
import com.acme.driverapp.v1.deleteDriverHomeReportRequest
import com.acme.driverapp.v1.driverHome
import com.acme.driverapp.v1.driverHomeComment
import com.acme.driverapp.v1.driverInbox
import com.acme.driverapp.v1.escalateDriverInboxRequest
import com.acme.driverapp.v1.inspectDriverHomeWorkflowResponse
import com.acme.driverapp.v1.listPendingDriverInboxesRequest
import com.acme.driverapp.v1.pauseDriverHomeWorkflowResponse
import com.acme.driverapp.v1.resumeDriverHomeWorkflowResponse
import com.acme.driverapp.v1.retryDriverHomeWorkflowResponse
import com.acme.driverapp.v1.scheduleDriverHomeReportRequest
import com.acme.driverapp.v1.signalDriverHomeWorkflowResponse
import com.acme.driverapp.v1.startDriverHomeWorkflowResponse
import com.acme.driverapp.v1.submitDriverHomeRequest
import com.acme.driverapp.v1.submitDriverInboxRequest
import com.acme.edgegateway.v1.RouteBindingQueryServiceGrpcKt
import com.acme.edgegateway.v1.copy as edgegatewayCopy
import com.acme.edgegateway.v1.resolveRouteBindingsRequest
import com.acme.fuel.v1.FuelPolicyApprovalServiceGrpcKt
import com.acme.fuel.v1.commentFuelPolicyApprovalRequest
import com.acme.fuel.v1.fuelPolicy
import com.acme.identity.v1.userRef
import com.acme.metrics.v1.BrowseMetricSeriesesRequestKt
import com.acme.metrics.v1.BrowseMetricSeriesesResponse
import com.acme.metrics.v1.CountMetricSeriesesRequestKt
import com.acme.metrics.v1.MetricSeriesQueryServiceGrpcKt
import com.acme.metrics.v1.browseMetricSeriesesRequest
import com.acme.metrics.v1.countMetricSeriesesRequest
import com.acme.telematics.v1.CountGeoFenceTotalsRequestKt
import com.acme.telematics.v1.GeoFenceReportingServiceGrpcKt
import com.acme.telematics.v1.countGeoFenceTotalsRequest
import com.acme.transport.driverapp.DriverappRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.venues.v1.UpdateVenueRequest
import com.acme.venues.v1.VenueServiceGrpcKt
import com.acme.venues.v1.updateVenueRequest
import com.acme.venues.v1.venue
import com.google.protobuf.Any as ProtoAny
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

/** Runs the long-lived workflow that takes a driver home from start to finish. */
class DriverHomeWorkflowGrpcService(
    private val store: DriverappRepository,
    private val workers: CoroutineScope,
    private val customersConsentRecordApprovalService: ConsentRecordApprovalServiceGrpcKt.ConsentRecordApprovalServiceCoroutineStub,
    private val customersConsentRecordSyncService: ConsentRecordSyncServiceGrpcKt.ConsentRecordSyncServiceCoroutineStub,
    private val customersConsentRecordWorkflowService: ConsentRecordWorkflowServiceGrpcKt.ConsentRecordWorkflowServiceCoroutineStub,
    private val customersCustomerWorkflowService: CustomerWorkflowServiceGrpcKt.CustomerWorkflowServiceCoroutineStub,
    private val dispatchDispatchAssignmentApprovalService: DispatchAssignmentApprovalServiceGrpcKt.DispatchAssignmentApprovalServiceCoroutineStub,
    private val driverappDriverHomeApprovalService: DriverHomeApprovalServiceGrpcKt.DriverHomeApprovalServiceCoroutineStub,
    private val driverappDriverHomeReportingService: DriverHomeReportingServiceGrpcKt.DriverHomeReportingServiceCoroutineStub,
    private val driverappDriverInboxApprovalService: DriverInboxApprovalServiceGrpcKt.DriverInboxApprovalServiceCoroutineStub,
    private val edgegatewayRouteBindingQueryService: RouteBindingQueryServiceGrpcKt.RouteBindingQueryServiceCoroutineStub,
    private val fuelFuelPolicyApprovalService: FuelPolicyApprovalServiceGrpcKt.FuelPolicyApprovalServiceCoroutineStub,
    private val metricsMetricSeriesQueryService: MetricSeriesQueryServiceGrpcKt.MetricSeriesQueryServiceCoroutineStub,
    private val telematicsGeoFenceReportingService: GeoFenceReportingServiceGrpcKt.GeoFenceReportingServiceCoroutineStub,
    private val venuesVenueService: VenueServiceGrpcKt.VenueServiceCoroutineStub,
) : DriverHomeWorkflowServiceGrpcKt.DriverHomeWorkflowServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DriverHomeWorkflowGrpcService::class.java)

    private val invokePauseDriverHomeWorkflow0: suspend (EscalateDriverInboxRequest) -> EscalateDriverInboxResponse = { payload ->
        driverappDriverInboxApprovalService.escalateDriverInbox(payload)
    }

    private val clientForRetryDriverHomeWorkflow0 by lazy { customersCustomerWorkflowService }

    /** Advances a driver home. */
    override suspend fun advanceDriverHomeWorkflow(request: AdvanceDriverHomeWorkflowRequest): AdvanceDriverHomeWorkflowResponse {
        return handleAdvanceDriverHomeWorkflow(request)
    }

    /** Cancels a driver home. */
    override suspend fun cancelDriverHomeWorkflow(request: CancelDriverHomeWorkflowRequest): CancelDriverHomeWorkflowResponse = handleCancelDriverHomeWorkflow(request)

    /** Returns a driver home. */
    override suspend fun inspectDriverHomeWorkflow(request: InspectDriverHomeWorkflowRequest): InspectDriverHomeWorkflowResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeWorkflowService/InspectDriverHomeWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "InspectDriverHomeWorkflow", request)
            log.atInfo().addKeyValue("operation", "InspectDriverHomeWorkflow").log("Handling transport request")
            val outbound0 = submitDriverInboxRequest {
                this.context = request.context
                this.details = SubmitDriverInboxRequestKt.details {
                    this.approver = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val adapter0 = OutboundPort<SubmitDriverInboxRequest, SubmitDriverInboxResponse> { payload ->
                driverappDriverInboxApprovalService.submitDriverInbox(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            inspectDriverHomeWorkflowResponse {
                this.driverHome = driverHome {
                    this.audit = auditInfo {
                        this.createdAt = reply0.driverInbox.audit.createdAt
                        this.updatedAt = reply0.driverInbox.audit.updatedAt
                    }
                    this.driverHomeId = request.driverHomeId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Pauses a driver home. */
    override suspend fun pauseDriverHomeWorkflow(request: PauseDriverHomeWorkflowRequest): PauseDriverHomeWorkflowResponse {
        return handlePauseDriverHomeWorkflow(request)
    }

    /** Resumes a driver home. */
    override suspend fun resumeDriverHomeWorkflow(request: ResumeDriverHomeWorkflowRequest): ResumeDriverHomeWorkflowResponse = handleResumeDriverHomeWorkflow(request)

    /** Retrys a driver home. */
    override suspend fun retryDriverHomeWorkflow(request: RetryDriverHomeWorkflowRequest): RetryDriverHomeWorkflowResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeWorkflowService/RetryDriverHomeWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RetryDriverHomeWorkflow", request)
            log.atInfo().addKeyValue("operation", "RetryDriverHomeWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverHome.driverHomeId).log("Recorded audit detail")
            val outbound0 = retryCustomerWorkflowRequest {
                this.context = request.context
                this.customer = customer {
                    this.audit = auditInfo {
                        this.createdAt = request.driverHome.audit.createdAt
                    }
                    this.contact = contactInfo {
                        this.phone = request.details.notify.phone
                    }
                    this.relationshipManager = userRef {
                        this.userId = request.driverHome.assignee.userId
                    }
                }
                this.details = RetryCustomerWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.email = request.driverHome.assignee.email
                    }
                }
            }
            val reply0 = clientForRetryDriverHomeWorkflow0.retryCustomerWorkflow(outbound0)
            val outbound1 = listPendingConsentRecordsRequest {
                this.context = request.context
                this.filter = ListPendingConsentRecordsRequestKt.filter {
                    this.createdAfter = request.driverHome.sla.dueAt
                    this.ownerId = request.driverHome.customer.customerId
                    this.region = reply0.customer.primaryAddress.region
                }
            }
            val reply1 = flowOf(outbound1).map { value -> customersConsentRecordApprovalService.listPendingConsentRecords(value) }.single()
            val outbound2 = submitDriverInboxRequest {
                this.context = request.context
                this.driverInbox = driverInbox {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.customer.audit.updatedAt
                    }
                }
            }
            val reply2 = withTimeout(5_000) { driverappDriverInboxApprovalService.submitDriverInbox(outbound2) }
            retryDriverHomeWorkflowResponse {
                this.driverHome = driverHome {
                    this.audit = auditInfo {
                        this.createdAt = reply0.customer.audit.createdAt
                    }
                    this.customFields.putAll(request.driverHome.customFieldsMap)
                    this.driverHomeId = request.driverHomeId
                }
                this.outcome = RetryDriverHomeWorkflowResponseKt.outcome {
                    this.status = reply2.result.status
                }
                this.result = operationResult {
                    this.status = reply1.result.status
                }
            }
        }

    /** Signals a driver home. */
    override suspend fun signalDriverHomeWorkflow(request: SignalDriverHomeWorkflowRequest): SignalDriverHomeWorkflowResponse {
        return handleSignalDriverHomeWorkflow(request)
    }

    /** Starts a driver home. */
    override suspend fun startDriverHomeWorkflow(request: StartDriverHomeWorkflowRequest): StartDriverHomeWorkflowResponse = handleStartDriverHomeWorkflow(request)

    private suspend fun handleAdvanceDriverHomeWorkflow(request: AdvanceDriverHomeWorkflowRequest): AdvanceDriverHomeWorkflowResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeWorkflowService/AdvanceDriverHomeWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "AdvanceDriverHomeWorkflow", request)
            log.atInfo().addKeyValue("operation", "AdvanceDriverHomeWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverHome.subject).log("Recorded audit detail")
            val outbound0 = startConsentRecordWorkflowRequest {
                this.consentRecord = consentRecord {
                    this.subject = householdRef {
                        this.displayName = request.driverHome.assignee.displayName
                    }
                }
                this.context = request.context
                this.details = StartConsentRecordWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.phone = request.details.notify.phone
                    }
                }
            }
            val reply0 = customersConsentRecordWorkflowService.startConsentRecordWorkflow(outbound0)
            val outbound1 = scheduleDriverHomeReportRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.details = ScheduleDriverHomeReportRequestKt.details {
                    this.filters.putAll(reply0.result.detailsMap)
                }
                this.driverHomeId = request.driverHome.driverHomeId
                this.reason = request.reason
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = driverappDriverHomeReportingService.scheduleDriverHomeReport(envelope1.unpack(ScheduleDriverHomeReportRequest::class.java))
            advanceDriverHomeWorkflowResponse {
                this.driverHome = driverHome {
                    this.driverHomeId = request.driverHomeId
                    this.status = request.driverHome.status
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.message = reply0.outcome.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleCancelDriverHomeWorkflow(request: CancelDriverHomeWorkflowRequest): CancelDriverHomeWorkflowResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeWorkflowService/CancelDriverHomeWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CancelDriverHomeWorkflow", request)
            log.atInfo().addKeyValue("operation", "CancelDriverHomeWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverHome.driverHomeId).log("Recorded audit detail")
            val outbound0 = delegateDriverInboxApprovalRequest {
                this.context = request.context
                this.driverInbox = driverInbox {
                    this.audit = auditInfo {
                        this.createdBy = request.driverHome.audit.createdBy
                    }
                    this.counterparty = customerRef {
                        this.displayName = request.driverHome.assignee.displayName
                    }
                }
            }
            val reply0 = driverappDriverInboxApprovalService.delegateDriverInboxApproval(WireCopies.json(outbound0))
            var reply1 = ListPendingDriverInboxesResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.driverHome.commentsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound1 = listPendingDriverInboxesRequest {
                                this.context = request.context
                                this.filter = ListPendingDriverInboxesRequestKt.filter {
                                    this.createdAfter = item.createdAt
                                    this.labels.putAll(reply0.result.detailsMap)
                                    this.ownerId = item.commentId
                                    this.region = reply0.driverInbox.billingAddress.region
                                }
                            }
                            driverappDriverInboxApprovalService.listPendingDriverInboxes(outbound1)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply1 = it }
                }
            }
            val outbound2 = updateVenueRequest {
                this.context = request.context
                this.venue = venue {
                    this.address = postalAddress {
                        this.countryCode = reply0.driverInbox.billingAddress.countryCode
                        this.locality = reply0.driverInbox.billingAddress.locality
                    }
                    this.audit = auditInfo {
                        this.updatedAt = request.driverHome.audit.updatedAt
                        this.updatedBy = reply0.driverInbox.audit.updatedBy
                    }
                }
            }
            val command2: Submission<UpdateVenueRequest> = Submission.Live(outbound2)
            val payload2 = when (val command = command2) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply2 = venuesVenueService.updateVenue(payload2)
            cancelDriverHomeWorkflowResponse {
                this.driverHome = driverHome {
                    this.assignee = userRef {
                        this.email = reply0.driverInbox.counterparty.email
                    }
                    this.customFields.putAll(request.driverHome.customFieldsMap)
                    this.driverHomeId = request.driverHomeId
                    this.requester = contactInfo {
                        this.email = reply0.driverInbox.counterparty.email
                    }
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply2.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handlePauseDriverHomeWorkflow(request: PauseDriverHomeWorkflowRequest): PauseDriverHomeWorkflowResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeWorkflowService/PauseDriverHomeWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PauseDriverHomeWorkflow", request)
            log.atInfo().addKeyValue("operation", "PauseDriverHomeWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverHome.subject).log("Recorded audit detail")
            val outbound0 = escalateDriverInboxRequest {
                this.context = request.context
                this.details = EscalateDriverInboxRequestKt.details {
                    this.approver = userRef {
                        this.email = request.driverHome.requester.email
                    }
                }
                this.driverInbox = driverInbox {
                    this.audit = auditInfo {
                        this.updatedAt = request.driverHome.audit.updatedAt
                    }
                    this.counterparty = customerRef {
                        this.displayName = request.driverHome.customer.displayName
                        this.email = request.driverHome.customer.email
                    }
                }
            }
            val reply0 = invokePauseDriverHomeWorkflow0(outbound0)
            val outbound1 = deleteDriverHomeReportRequest {
                this.context = request.context
                this.driverHomeId = request.driverHome.driverHomeId
                this.etag = request.driverHome.audit.etag
                this.reason = request.reason
            }
            val invoke1: suspend (DeleteDriverHomeReportRequest, Metadata) -> DeleteDriverHomeReportResponse = driverappDriverHomeReportingService::deleteDriverHomeReport
            val reply1 = invoke1(outbound1, Metadata())
            var reply2 = PullConsentRecordsResponse.getDefaultInstance()
            request.driverHome.historyList.asFlow().map { item ->
                val outbound2 = pullConsentRecordsRequest {
                    this.consentRecord = consentRecord {
                        this.correlationId = reply0.result.correlationId
                        this.measurements.addAll(listOf(consentRecordMeasurement {
                            this.observedAt = item.changedAt
                        }))
                        this.subject = householdRef {
                            this.displayName = reply0.driverInbox.counterparty.displayName
                        }
                    }
                    this.context = request.context
                }
                customersConsentRecordSyncService.pullConsentRecords(outbound2)
            }.collect { reply2 = it }
            val outbound3 = RequestFactory.create {
                scheduleDriverHomeReportRequest {
                    this.context = request.context
                    this.driverHomeId = request.driverHome.driverHomeId
                }
            }
            val reply3 = driverappDriverHomeReportingService.scheduleDriverHomeReport(outbound3)
            pauseDriverHomeWorkflowResponse {
                this.driverHome = driverHome {
                    this.driverHomeId = request.driverHomeId
                    this.status = request.driverHome.status
                }
                this.result = operationResult {
                    this.correlationId = reply3.result.correlationId
                    this.message = reply0.result.message
                    this.referenceIds.addAll(reply3.result.referenceIdsList)
                    this.status = reply1.result.status
                }
            }
        }

    private suspend fun handleResumeDriverHomeWorkflow(request: ResumeDriverHomeWorkflowRequest): ResumeDriverHomeWorkflowResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeWorkflowService/ResumeDriverHomeWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResumeDriverHomeWorkflow", request)
            log.atInfo().addKeyValue("operation", "ResumeDriverHomeWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverHome.driverHomeId).log("Recorded audit detail")
            val outbound0 = countGeoFenceTotalsRequest {
                this.context = request.context
                this.filter = CountGeoFenceTotalsRequestKt.filter {
                    this.createdAfter = request.driverHome.audit.createdAt
                    this.labels.putAll(request.details.parametersMap)
                    this.ownerId = request.driverHome.sla.policyId
                }
            }
            val reply0 = ClientRegistry.lookup("acme.telematics.v1.GeoFenceReportingService", GeoFenceReportingServiceGrpcKt.GeoFenceReportingServiceCoroutineStub::class.java).countGeoFenceTotals(outbound0)
            val outbound1 = commentFuelPolicyApprovalRequest {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.updatedAt = request.driverHome.audit.updatedAt
                    }
                }
            }
            val reply1 = fuelFuelPolicyApprovalService.commentFuelPolicyApproval(WireCopies.reflective(outbound1))
            var reply2 = BrowseMetricSeriesesResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<DriverHomeStatusChange>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound2 = browseMetricSeriesesRequest {
                            this.context = request.context
                            this.filter = BrowseMetricSeriesesRequestKt.filter {
                                this.createdAfter = item.changedAt
                            }
                        }
                        reply2 = metricsMetricSeriesQueryService.browseMetricSerieses(outbound2)
                    }
                }
                try {
                    request.driverHome.historyList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            resumeDriverHomeWorkflowResponse {
                this.driverHome = driverHome {
                    this.driverHomeId = request.driverHomeId
                    this.status = request.driverHome.status
                }
                this.outcome = ResumeDriverHomeWorkflowResponseKt.outcome {
                    this.referenceId = reply1.outcome.referenceId
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSignalDriverHomeWorkflow(request: SignalDriverHomeWorkflowRequest): SignalDriverHomeWorkflowResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeWorkflowService/SignalDriverHomeWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SignalDriverHomeWorkflow", request)
            log.atInfo().addKeyValue("operation", "SignalDriverHomeWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverHome.driverHomeId).log("Recorded audit detail")
            val configure0: CountMetricSeriesesRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.filter = CountMetricSeriesesRequestKt.filter {
                    this.createdAfter = request.driverHome.sla.dueAt
                    this.labels.putAll(request.driverHome.customFieldsMap)
                    this.ownerId = request.driverHome.assignee.userId
                }
            }
            val outbound0 = countMetricSeriesesRequest(configure0)
            val reply0 = metricsMetricSeriesQueryService.countMetricSerieses(outbound0)
            val outbound1 = escalateDispatchAssignmentRequest {
                this.context = request.context
                this.details = EscalateDispatchAssignmentRequestKt.details {
                    this.approver = userRef {
                        this.email = request.details.notify.email
                    }
                }
                this.dispatchAssignment = dispatchAssignment {
                    this.comments.addAll(listOf(dispatchAssignmentComment {
                        this.createdAt = request.driverHome.audit.createdAt
                    }))
                }
            }
            val reply1 = dispatchDispatchAssignmentApprovalService.escalateDispatchAssignment(outbound1)
            val outbound2 = delegateDriverHomeApprovalRequest {
                this.context = request.context
                this.details = DelegateDriverHomeApprovalRequestKt.details {
                    this.approver = userRef {
                        this.email = reply1.dispatchAssignment.assignee.email
                    }
                }
                this.driverHome = driverHome {
                    this.audit = auditInfo {
                        this.createdBy = request.driverHome.audit.createdBy
                        this.etag = reply1.dispatchAssignment.audit.etag
                    }
                    this.requester = contactInfo {
                        this.phone = request.details.notify.phone
                    }
                }
            }
            val reply2 = driverappDriverHomeApprovalService.delegateDriverHomeApproval(outbound2)
            signalDriverHomeWorkflowResponse {
                this.driverHome = driverHome {
                    this.assignee = userRef {
                        this.displayName = reply1.dispatchAssignment.customer.displayName
                    }
                    this.driverHomeId = request.driverHomeId
                    this.requester = contactInfo {
                        this.phone = reply2.driverHome.requester.phone
                    }
                    this.status = request.driverHome.status
                }
                this.outcome = SignalDriverHomeWorkflowResponseKt.outcome {
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }

    private fun StartDriverHomeWorkflowRequest.toCommentDriverInboxApprovalFromStartDriverHomeWorkflow1(): CommentDriverInboxApprovalRequest {
        val source = this
        return commentDriverInboxApprovalRequest {
            this.context = source.context
            this.driverInbox = driverInbox {
                this.audit = auditInfo {
                    this.etag = source.driverHome.audit.etag
                }
            }
        }
    }

    private suspend fun handleStartDriverHomeWorkflow(request: StartDriverHomeWorkflowRequest): StartDriverHomeWorkflowResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeWorkflowService/StartDriverHomeWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StartDriverHomeWorkflow", request)
            log.atInfo().addKeyValue("operation", "StartDriverHomeWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverHome.driverHomeId).log("Recorded audit detail")
            val outbound0 = resolveRouteBindingsRequest {}.edgegatewayCopy {
                this.context = request.context
            }
            val reply0 = edgegatewayRouteBindingQueryService.resolveRouteBindings(outbound0)
            val outbound1 = request.toCommentDriverInboxApprovalFromStartDriverHomeWorkflow1()
            val reply1 = driverappDriverInboxApprovalService.commentDriverInboxApproval(outbound1)
            var reply2 = SubmitDriverHomeResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.driverHome.commentsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound2 = submitDriverHomeRequest {
                                this.context = request.context
                                this.driverHome = driverHome {
                                    this.comments.addAll(listOf(driverHomeComment {
                                        this.authorEmail = item.authorEmail
                                        this.visibility = item.visibility
                                    }))
                                }
                            }
                            driverappDriverHomeApprovalService.submitDriverHome(outbound2)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply2 = it }
                }
            }
            startDriverHomeWorkflowResponse {
                this.driverHome = driverHome {
                    this.driverHomeId = request.driverHomeId
                    this.requester = request.driverHome.requester
                }
                this.outcome = StartDriverHomeWorkflowResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }
}
