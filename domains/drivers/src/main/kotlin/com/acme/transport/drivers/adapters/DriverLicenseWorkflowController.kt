package com.acme.transport.drivers.adapters

import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.configuration.v1.ConfigSetServiceGrpcKt
import com.acme.configuration.v1.configSet
import com.acme.configuration.v1.updateConfigSetRequest
import com.acme.customers.v1.customerRef
import com.acme.drivers.v1.AdvanceDriverLicenseWorkflowRequest
import com.acme.drivers.v1.AdvanceDriverLicenseWorkflowResponse
import com.acme.drivers.v1.AdvanceDriverLicenseWorkflowResponseKt
import com.acme.drivers.v1.ApproveDutyStatusRequestKt
import com.acme.drivers.v1.ApproveDutyStatusResponse
import com.acme.drivers.v1.CancelDriverLicenseWorkflowRequest
import com.acme.drivers.v1.CancelDriverLicenseWorkflowResponse
import com.acme.drivers.v1.DriverLicenseApprovalServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseLine
import com.acme.drivers.v1.DriverLicenseQueryServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseWorkflowServiceGrpcKt
import com.acme.drivers.v1.DriverLifecycleServiceGrpcKt
import com.acme.drivers.v1.DutyStatusApprovalServiceGrpcKt
import com.acme.drivers.v1.DutyStatusReportingServiceGrpcKt
import com.acme.drivers.v1.FindDriverLicensesRequestKt
import com.acme.drivers.v1.FindDriverLicensesResponse
import com.acme.drivers.v1.GenerateDutyStatusReportRequest
import com.acme.drivers.v1.GenerateDutyStatusReportRequestKt
import com.acme.drivers.v1.GetDriverLifecycleRequest
import com.acme.drivers.v1.GetDriverLifecycleResponse
import com.acme.drivers.v1.InspectDriverLicenseWorkflowRequest
import com.acme.drivers.v1.InspectDriverLicenseWorkflowResponse
import com.acme.drivers.v1.PauseDriverLicenseWorkflowRequest
import com.acme.drivers.v1.PauseDriverLicenseWorkflowResponse
import com.acme.drivers.v1.PauseDriverLicenseWorkflowResponseKt
import com.acme.drivers.v1.ResumeDriverLicenseWorkflowRequest
import com.acme.drivers.v1.ResumeDriverLicenseWorkflowResponse
import com.acme.drivers.v1.RetryDriverLicenseWorkflowRequest
import com.acme.drivers.v1.RetryDriverLicenseWorkflowResponse
import com.acme.drivers.v1.RetryDriverLicenseWorkflowResponseKt
import com.acme.drivers.v1.SignalDriverLicenseWorkflowRequest
import com.acme.drivers.v1.SignalDriverLicenseWorkflowResponse
import com.acme.drivers.v1.SignalDriverLicenseWorkflowResponseKt
import com.acme.drivers.v1.StartDriverLicenseWorkflowRequest
import com.acme.drivers.v1.StartDriverLicenseWorkflowResponse
import com.acme.drivers.v1.StartDriverLicenseWorkflowResponseKt
import com.acme.drivers.v1.advanceDriverLicenseWorkflowResponse
import com.acme.drivers.v1.approveDutyStatusRequest
import com.acme.drivers.v1.cancelDriverLicenseWorkflowResponse
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverLicenseLine
import com.acme.drivers.v1.driverLicenseRef
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.dutyStatus
import com.acme.drivers.v1.findDriverLicensesRequest
import com.acme.drivers.v1.generateDutyStatusReportRequest
import com.acme.drivers.v1.getDriverLifecycleRequest
import com.acme.drivers.v1.inspectDriverLicenseWorkflowResponse
import com.acme.drivers.v1.pauseDriverLicenseWorkflowResponse
import com.acme.drivers.v1.resumeDriverLicenseWorkflowResponse
import com.acme.drivers.v1.retryDriverLicenseWorkflowResponse
import com.acme.drivers.v1.signalDriverLicenseWorkflowResponse
import com.acme.drivers.v1.startDriverLicenseWorkflowResponse
import com.acme.drivers.v1.submitDriverLicenseRequest
import com.acme.drivers.v1.submitDutyStatusRequest
import com.acme.identity.v1.ExplainUserDecisionRequest
import com.acme.identity.v1.ExplainUserDecisionResponse
import com.acme.identity.v1.UserValidationServiceGrpcKt
import com.acme.identity.v1.explainUserDecisionRequest
import com.acme.identity.v1.previewUserRequest
import com.acme.identity.v1.user
import com.acme.identity.v1.userRef
import com.acme.metrics.v1.BrowseMetricSeriesesRequestKt
import com.acme.metrics.v1.MetricSeriesQueryServiceGrpcKt
import com.acme.metrics.v1.MetricSeriesValidationServiceGrpcKt
import com.acme.metrics.v1.ValidateMetricSeriesResponse
import com.acme.metrics.v1.browseMetricSeriesesRequest
import com.acme.metrics.v1.metricSeries
import com.acme.metrics.v1.metricSeriesComponent
import com.acme.metrics.v1.validateMetricSeriesRequest
import com.acme.telematics.v1.MapTripLogImportColumnsRequest
import com.acme.telematics.v1.MapTripLogImportColumnsRequestKt
import com.acme.telematics.v1.MapTripLogImportColumnsResponse
import com.acme.telematics.v1.ReconcileTripLogsRequest
import com.acme.telematics.v1.ReconcileTripLogsResponse
import com.acme.telematics.v1.TripLogImportServiceGrpcKt
import com.acme.telematics.v1.TripLogSyncServiceGrpcKt
import com.acme.telematics.v1.mapTripLogImportColumnsRequest
import com.acme.telematics.v1.reconcileTripLogsRequest
import com.acme.telematics.v1.tripLog
import com.acme.telematics.v1.tripLogCardSettlement
import com.acme.ticketing.v1.BrowseTicketsRequestKt
import com.acme.ticketing.v1.TicketQueryServiceGrpcKt
import com.acme.ticketing.v1.browseTicketsRequest
import com.acme.transport.drivers.DriversRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Runs the long-lived workflow that takes a driver license from start to finish. */
class DriverLicenseWorkflowController(
    private val store: DriversRepository,
    private val workers: CoroutineScope,
    private val configurationConfigSetService: ConfigSetServiceGrpcKt.ConfigSetServiceCoroutineStub,
    private val driversDriverLicenseApprovalService: DriverLicenseApprovalServiceGrpcKt.DriverLicenseApprovalServiceCoroutineStub,
    private val driversDriverLicenseQueryService: DriverLicenseQueryServiceGrpcKt.DriverLicenseQueryServiceCoroutineStub,
    private val driversDriverLifecycleService: DriverLifecycleServiceGrpcKt.DriverLifecycleServiceCoroutineStub,
    private val driversDutyStatusApprovalService: DutyStatusApprovalServiceGrpcKt.DutyStatusApprovalServiceCoroutineStub,
    private val driversDutyStatusReportingService: DutyStatusReportingServiceGrpcKt.DutyStatusReportingServiceCoroutineStub,
    private val identityUserValidationService: UserValidationServiceGrpcKt.UserValidationServiceCoroutineStub,
    private val metricsMetricSeriesQueryService: MetricSeriesQueryServiceGrpcKt.MetricSeriesQueryServiceCoroutineStub,
    private val metricsMetricSeriesValidationService: MetricSeriesValidationServiceGrpcKt.MetricSeriesValidationServiceCoroutineStub,
    private val telematicsTripLogImportService: TripLogImportServiceGrpcKt.TripLogImportServiceCoroutineStub,
    private val telematicsTripLogSyncService: TripLogSyncServiceGrpcKt.TripLogSyncServiceCoroutineStub,
    private val ticketingTicketQueryService: TicketQueryServiceGrpcKt.TicketQueryServiceCoroutineStub,
) : DriverLicenseWorkflowServiceGrpcKt.DriverLicenseWorkflowServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DriverLicenseWorkflowController::class.java)

    private val invokeCancelDriverLicenseWorkflow0: suspend (GetDriverLifecycleRequest) -> GetDriverLifecycleResponse = { payload ->
        driversDriverLifecycleService.getDriverLifecycle(payload)
    }

    private val workerForResumeDriverLicenseWorkflow0 = ReplyMailbox<ReconcileTripLogsRequest, ReconcileTripLogsResponse>(workers) { payload ->
        withTimeout(5_000) { telematicsTripLogSyncService.reconcileTripLogs(payload) }
    }

    private val clientForRetryDriverLicenseWorkflow0 by lazy { identityUserValidationService }

    /** Advances a driver license. */
    override suspend fun advanceDriverLicenseWorkflow(request: AdvanceDriverLicenseWorkflowRequest): AdvanceDriverLicenseWorkflowResponse = handleAdvanceDriverLicenseWorkflow(request)

    /** Cancels a driver license. */
    override suspend fun cancelDriverLicenseWorkflow(request: CancelDriverLicenseWorkflowRequest): CancelDriverLicenseWorkflowResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseWorkflowService/CancelDriverLicenseWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CancelDriverLicenseWorkflow", request)
            log.atInfo().addKeyValue("operation", "CancelDriverLicenseWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverLicense.driverLicenseId).log("Recorded audit detail")
            val outbound0 = getDriverLifecycleRequest {
                this.context = request.context
                this.driverId = request.driverLicense.counterparty.driverId
            }
            val reply0 = invokeCancelDriverLicenseWorkflow0(outbound0)
            val outbound1 = mapTripLogImportColumnsRequest {
                this.context = request.context
                this.details = MapTripLogImportColumnsRequestKt.details {
                    this.contact = contactInfo {
                        this.email = reply0.driver.contact.email
                    }
                }
            }
            val invoke1: suspend (MapTripLogImportColumnsRequest, Metadata) -> MapTripLogImportColumnsResponse = telematicsTripLogImportService::mapTripLogImportColumns
            val reply1 = invoke1(outbound1, Metadata())
            val outbound2 = GenerateDutyStatusReportRequest.newBuilder().apply {
                putAllAnnotations(request.annotationsMap)
                setContext(request.context)
                setDetails(GenerateDutyStatusReportRequestKt.details {
                    this.period = reply0.driver.preferences.quietHours
                })
                setReason(request.reason)
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply2 = outbound2.let { value -> driversDutyStatusReportingService.generateDutyStatusReport(value) }
            cancelDriverLicenseWorkflowResponse {
                this.driverLicense = driverLicense {
                    this.counterparty = request.driverLicense.counterparty
                    this.driverLicenseId = request.driverLicenseId
                }
                this.result = operationResult {
                    this.message = reply2.result.message
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = reply0.result.status
                }
            }
        }

    /** Returns a driver license. */
    override suspend fun inspectDriverLicenseWorkflow(request: InspectDriverLicenseWorkflowRequest): InspectDriverLicenseWorkflowResponse {
        return handleInspectDriverLicenseWorkflow(request)
    }

    /** Pauses a driver license. */
    override suspend fun pauseDriverLicenseWorkflow(request: PauseDriverLicenseWorkflowRequest): PauseDriverLicenseWorkflowResponse = handlePauseDriverLicenseWorkflow(request)

    /** Resumes a driver license. */
    override suspend fun resumeDriverLicenseWorkflow(request: ResumeDriverLicenseWorkflowRequest): ResumeDriverLicenseWorkflowResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseWorkflowService/ResumeDriverLicenseWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResumeDriverLicenseWorkflow", request)
            log.atInfo().addKeyValue("operation", "ResumeDriverLicenseWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverLicense.driverLicenseId).log("Recorded audit detail")
            val outbound0 = reconcileTripLogsRequest {
                this.context = request.context
                this.tripLog = tripLog {
                    this.billingAddress = postalAddress {
                        this.recipient = request.driverLicense.billingAddress.recipient
                    }
                    this.card = tripLogCardSettlement {
                        this.holderName = request.driverLicense.card.holderName
                        this.last4 = request.driverLicense.card.last4
                    }
                    this.counterparty = customerRef {
                        this.email = request.details.notify.email
                    }
                }
            }
            val reply0 = workerForResumeDriverLicenseWorkflow0.submit(outbound0)
            resumeDriverLicenseWorkflowResponse {
                this.driverLicense = driverLicense {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.tripLog.audit.updatedAt
                    }
                    this.driverLicenseId = request.driverLicenseId
                    this.issuedAt = request.driverLicense.issuedAt
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Retrys a driver license. */
    override suspend fun retryDriverLicenseWorkflow(request: RetryDriverLicenseWorkflowRequest): RetryDriverLicenseWorkflowResponse {
        return handleRetryDriverLicenseWorkflow(request)
    }

    /** Signals a driver license. */
    override suspend fun signalDriverLicenseWorkflow(request: SignalDriverLicenseWorkflowRequest): SignalDriverLicenseWorkflowResponse = handleSignalDriverLicenseWorkflow(request)

    /** Starts a driver license. */
    override suspend fun startDriverLicenseWorkflow(request: StartDriverLicenseWorkflowRequest): StartDriverLicenseWorkflowResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseWorkflowService/StartDriverLicenseWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StartDriverLicenseWorkflow", request)
            log.atInfo().addKeyValue("operation", "StartDriverLicenseWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverLicense.driverLicenseId).log("Recorded audit detail")
            val outbound0 = submitDutyStatusRequest {
                this.context = request.context
                this.dutyStatus = dutyStatus {
                    this.subject = driverLicenseRef {
                        this.displayName = request.driverLicense.counterparty.displayName
                        this.number = request.driverLicense.number
                    }
                }
            }
            val reply0 = driversDutyStatusApprovalService.submitDutyStatus(outbound0)
            startDriverLicenseWorkflowResponse {
                this.driverLicense = driverLicense {
                    this.driverLicenseId = request.driverLicenseId
                    this.metadata.putAll(request.driverLicense.metadataMap)
                }
                this.outcome = StartDriverLicenseWorkflowResponseKt.outcome {
                    this.nextSteps.addAll(reply0.outcome.nextStepsList)
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleAdvanceDriverLicenseWorkflow(request: AdvanceDriverLicenseWorkflowRequest): AdvanceDriverLicenseWorkflowResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseWorkflowService/AdvanceDriverLicenseWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "AdvanceDriverLicenseWorkflow", request)
            log.atInfo().addKeyValue("operation", "AdvanceDriverLicenseWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverLicense.driverLicenseId).log("Recorded audit detail")
            var reply0 = FindDriverLicensesResponse.getDefaultInstance()
            request.driverLicense.linesList.asFlow().map { item ->
                val outbound0 = findDriverLicensesRequest {
                    this.context = request.context
                    this.filter = FindDriverLicensesRequestKt.filter {
                        this.createdAfter = request.driverLicense.dueAt
                        this.labels.putAll(item.attributesMap)
                        this.ownerId = item.lineId
                        this.region = request.driverLicense.billingAddress.region
                    }
                }
                driversDriverLicenseQueryService.findDriverLicenses(outbound0)
            }.collect { reply0 = it }
            val outbound1 = explainUserDecisionRequest {
                this.context = request.context
                this.user = user {
                    this.audit = auditInfo {
                        this.updatedAt = request.driverLicense.audit.updatedAt
                    }
                }
            }
            val adapter1 = OutboundPort<ExplainUserDecisionRequest, ExplainUserDecisionResponse> { payload ->
                identityUserValidationService.explainUserDecision(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            advanceDriverLicenseWorkflowResponse {
                this.driverLicense = driverLicense {
                    this.counterparty = driverRef {
                        this.email = reply1.user.contact.email
                    }
                    this.driverLicenseId = request.driverLicenseId
                    this.issuedAt = request.driverLicense.issuedAt
                }
                this.outcome = AdvanceDriverLicenseWorkflowResponseKt.outcome {
                    this.message = reply1.outcome.message
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleInspectDriverLicenseWorkflow(request: InspectDriverLicenseWorkflowRequest): InspectDriverLicenseWorkflowResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseWorkflowService/InspectDriverLicenseWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "InspectDriverLicenseWorkflow", request)
            log.atInfo().addKeyValue("operation", "InspectDriverLicenseWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            inspectDriverLicenseWorkflowResponse {
                this.driverLicense = driverLicense {
                    this.driverLicenseId = request.driverLicenseId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handlePauseDriverLicenseWorkflow(request: PauseDriverLicenseWorkflowRequest): PauseDriverLicenseWorkflowResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseWorkflowService/PauseDriverLicenseWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PauseDriverLicenseWorkflow", request)
            log.atInfo().addKeyValue("operation", "PauseDriverLicenseWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverLicense.driverLicenseId).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                browseMetricSeriesesRequest {
                    this.context = request.context
                    this.filter = BrowseMetricSeriesesRequestKt.filter {
                        this.createdAfter = request.driverLicense.audit.updatedAt
                        this.labels.putAll(request.driverLicense.metadataMap)
                        this.region = request.driverLicense.billingAddress.region
                    }
                }
            }
            val reply0 = metricsMetricSeriesQueryService.browseMetricSerieses(outbound0)
            val outbound1 = approveDutyStatusRequest {
                this.context = request.context
                this.details = ApproveDutyStatusRequestKt.details {
                    this.approver = userRef {
                        this.email = request.details.notify.email
                    }
                }
            }
            val reply1 = ClientRegistry.lookup("acme.drivers.v1.DutyStatusApprovalService", DutyStatusApprovalServiceGrpcKt.DutyStatusApprovalServiceCoroutineStub::class.java).approveDutyStatus(outbound1)
            val outbound2 = submitDriverLicenseRequest {
                this.context = request.context
                this.driverLicense = driverLicense {
                    this.billingAddress = postalAddress {
                        this.lines.addAll(request.driverLicense.billingAddress.linesList)
                        this.locality = request.driverLicense.billingAddress.locality
                    }
                }
            }
            val reply2 = driversDriverLicenseApprovalService.submitDriverLicense(WireCopies.reflective(outbound2))
            pauseDriverLicenseWorkflowResponse {
                this.driverLicense = driverLicense {
                    this.driverLicenseId = request.driverLicenseId
                    this.lines.addAll(listOf(driverLicenseLine {
                        this.attributes.putAll(reply1.dutyStatus.attributesMap)
                    }))
                    this.metadata.putAll(reply2.driverLicense.metadataMap)
                    this.status = request.driverLicense.status
                }
                this.outcome = PauseDriverLicenseWorkflowResponseKt.outcome {
                    this.status = reply1.outcome.status
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleRetryDriverLicenseWorkflow(request: RetryDriverLicenseWorkflowRequest): RetryDriverLicenseWorkflowResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseWorkflowService/RetryDriverLicenseWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RetryDriverLicenseWorkflow", request)
            log.atInfo().addKeyValue("operation", "RetryDriverLicenseWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverLicense.driverLicenseId).log("Recorded audit detail")
            val outbound0 = previewUserRequest {
                this.context = request.context
                this.user = user {
                    this.primaryAddress = postalAddress {
                        this.recipient = request.driverLicense.billingAddress.recipient
                    }
                    this.relationshipManager = userRef {
                        this.email = request.driverLicense.counterparty.email
                    }
                }
            }
            val reply0 = clientForRetryDriverLicenseWorkflow0.previewUser(outbound0)
            var reply1 = ApproveDutyStatusResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<DriverLicenseLine>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound1 = approveDutyStatusRequest {
                            this.context = request.context
                            this.dutyStatus = dutyStatus {
                                this.attributes.putAll(item.attributesMap)
                                this.subject = driverLicenseRef {
                                    this.displayName = request.driverLicense.counterparty.displayName
                                }
                            }
                        }
                        reply1 = driversDutyStatusApprovalService.approveDutyStatus(outbound1)
                    }
                }
                try {
                    request.driverLicense.linesList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            retryDriverLicenseWorkflowResponse {
                this.driverLicense = driverLicense {
                    this.driverLicenseId = request.driverLicenseId
                    this.notes = request.driverLicense.notes
                }
                this.outcome = RetryDriverLicenseWorkflowResponseKt.outcome {
                    this.effectiveAt = reply0.outcome.effectiveAt
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSignalDriverLicenseWorkflow(request: SignalDriverLicenseWorkflowRequest): SignalDriverLicenseWorkflowResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseWorkflowService/SignalDriverLicenseWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SignalDriverLicenseWorkflow", request)
            log.atInfo().addKeyValue("operation", "SignalDriverLicenseWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverLicense.driverLicenseId).log("Recorded audit detail")
            var reply0 = ValidateMetricSeriesResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.driverLicense.linesList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound0 = validateMetricSeriesRequest {
                                this.context = request.context
                                this.metricSeries = metricSeries {
                                    this.components.putAll(mapOf("primary" to metricSeriesComponent {
                                        this.description = item.description
                                    }))
                                }
                            }
                            metricsMetricSeriesValidationService.validateMetricSeries(outbound0)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply0 = it }
                }
            }
            val configure1: BrowseTicketsRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.filter = BrowseTicketsRequestKt.filter {
                    this.region = reply0.metricSeries.address.region
                }
            }
            val outbound1 = browseTicketsRequest(configure1)
            val reply1 = ticketingTicketQueryService.browseTickets(outbound1)
            val outbound2 = updateConfigSetRequest {
                this.configSet = configSet {
                    this.children.addAll(listOf(configSet {
                        this.children.addAll(listOf(configSet {
                            this.audit = auditInfo {
                                this.createdBy = request.driverLicense.audit.createdBy
                            }
                        }))
                    }))
                }
                this.context = request.context
            }
            val reply2 = configurationConfigSetService.updateConfigSet(outbound2)
            signalDriverLicenseWorkflowResponse {
                this.driverLicense = driverLicense {
                    this.billingAddress = postalAddress {
                        this.region = reply2.configSet.location.region
                    }
                    this.counterparty = request.driverLicense.counterparty
                    this.driverLicenseId = request.driverLicenseId
                }
                this.outcome = SignalDriverLicenseWorkflowResponseKt.outcome {
                    this.message = reply1.result.message
                }
                this.result = operationResult {
                    this.message = reply2.result.message
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }
}
