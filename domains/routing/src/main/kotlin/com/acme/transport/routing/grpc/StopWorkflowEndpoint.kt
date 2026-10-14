package com.acme.transport.routing.grpc

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.documents.v1.DocumentQueryServiceGrpcKt
import com.acme.documents.v1.SuggestDocumentsRequestKt
import com.acme.documents.v1.suggestDocumentsRequest
import com.acme.drivers.v1.BrowseDriverLicensesRequestKt
import com.acme.drivers.v1.DriverCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseQueryServiceGrpcKt
import com.acme.drivers.v1.DriverServiceGrpcKt
import com.acme.drivers.v1.DutyStatusValidationServiceGrpcKt
import com.acme.drivers.v1.EstimateDriverRequest
import com.acme.drivers.v1.EstimateDriverResponse
import com.acme.drivers.v1.NormalizeDutyStatusRequestKt
import com.acme.drivers.v1.NormalizeDutyStatusResponse
import com.acme.drivers.v1.browseDriverLicensesRequest
import com.acme.drivers.v1.cloneDriverRequest
import com.acme.drivers.v1.copy as driversCopy
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.estimateDriverRequest
import com.acme.drivers.v1.normalizeDutyStatusRequest
import com.acme.identity.v1.userRef
import com.acme.routing.v1.AdvanceRouteWorkflowRequestKt
import com.acme.routing.v1.AdvanceRouteWorkflowResponse
import com.acme.routing.v1.AdvanceStopWorkflowRequest
import com.acme.routing.v1.AdvanceStopWorkflowResponse
import com.acme.routing.v1.AdvanceStopWorkflowResponseKt
import com.acme.routing.v1.CancelStopWorkflowRequest
import com.acme.routing.v1.CancelStopWorkflowResponse
import com.acme.routing.v1.CancelStopWorkflowResponseKt
import com.acme.routing.v1.EscalateRouteRequestKt
import com.acme.routing.v1.InspectStopWorkflowRequest
import com.acme.routing.v1.InspectStopWorkflowResponse
import com.acme.routing.v1.ListStopWorkflowsRequest
import com.acme.routing.v1.ListStopWorkflowsResponse
import com.acme.routing.v1.PauseStopWorkflowRequest
import com.acme.routing.v1.PauseStopWorkflowResponse
import com.acme.routing.v1.PreviewRouteRequest
import com.acme.routing.v1.RecallStopResponse
import com.acme.routing.v1.ResumeStopWorkflowRequest
import com.acme.routing.v1.ResumeStopWorkflowResponse
import com.acme.routing.v1.ResumeStopWorkflowResponseKt
import com.acme.routing.v1.RetryStopWorkflowRequest
import com.acme.routing.v1.RetryStopWorkflowResponse
import com.acme.routing.v1.RetryStopWorkflowResponseKt
import com.acme.routing.v1.RouteApprovalServiceGrpcKt
import com.acme.routing.v1.RoutePlanValidationServiceGrpcKt
import com.acme.routing.v1.RouteValidationServiceGrpcKt
import com.acme.routing.v1.RouteWorkflowServiceGrpcKt
import com.acme.routing.v1.ScoreRouteRequestKt
import com.acme.routing.v1.ScoreRouteResponse
import com.acme.routing.v1.SignalStopWorkflowRequest
import com.acme.routing.v1.SignalStopWorkflowResponse
import com.acme.routing.v1.SignalStopWorkflowResponseKt
import com.acme.routing.v1.StartStopWorkflowRequest
import com.acme.routing.v1.StartStopWorkflowResponse
import com.acme.routing.v1.Stop
import com.acme.routing.v1.StopApprovalServiceGrpcKt
import com.acme.routing.v1.StopCalculationServiceGrpcKt
import com.acme.routing.v1.StopParticipant
import com.acme.routing.v1.StopValidationServiceGrpcKt
import com.acme.routing.v1.StopWorkflowServiceGrpcKt
import com.acme.routing.v1.ValidateRoutePlanRequestKt
import com.acme.routing.v1.ValidateRoutePlanResponse
import com.acme.routing.v1.advanceRouteWorkflowRequest
import com.acme.routing.v1.advanceStopWorkflowResponse
import com.acme.routing.v1.cancelStopWorkflowResponse
import com.acme.routing.v1.delegateRouteApprovalRequest
import com.acme.routing.v1.escalateRouteRequest
import com.acme.routing.v1.inspectStopWorkflowResponse
import com.acme.routing.v1.listStopWorkflowsResponse
import com.acme.routing.v1.pauseStopWorkflowResponse
import com.acme.routing.v1.previewRouteRequest
import com.acme.routing.v1.recallStopRequest
import com.acme.routing.v1.resumeStopWorkflowResponse
import com.acme.routing.v1.retryStopWorkflowResponse
import com.acme.routing.v1.route
import com.acme.routing.v1.scoreRouteRequest
import com.acme.routing.v1.scoreStopRequest
import com.acme.routing.v1.signalRouteWorkflowRequest
import com.acme.routing.v1.signalStopWorkflowResponse
import com.acme.routing.v1.simulateStopRequest
import com.acme.routing.v1.startStopWorkflowResponse
import com.acme.routing.v1.stop
import com.acme.routing.v1.stopDailyRule
import com.acme.routing.v1.stopParticipant
import com.acme.routing.v1.validateRoutePlanRequest
import com.acme.secrets.v1.SecretServiceGrpcKt
import com.acme.secrets.v1.secret
import com.acme.secrets.v1.updateSecretRequest
import com.acme.telematics.v1.GeoFenceImportServiceGrpcKt
import com.acme.telematics.v1.RetryGeoFenceImportRequestKt
import com.acme.telematics.v1.RetryGeoFenceImportResponse
import com.acme.telematics.v1.TelemetryReadingImportServiceGrpcKt
import com.acme.telematics.v1.ValidateTelemetryReadingImportRequestKt
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.geoFenceRef
import com.acme.telematics.v1.retryGeoFenceImportRequest
import com.acme.telematics.v1.telemetryReading
import com.acme.telematics.v1.validateTelemetryReadingImportRequest
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.transport.routing.RoutingRepository
import com.google.protobuf.Timestamp
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

/** Runs the long-lived workflow that takes a stop from start to finish. */
class StopWorkflowEndpoint(
    private val store: RoutingRepository,
    private val workers: CoroutineScope,
    private val documentsDocumentQueryService: DocumentQueryServiceGrpcKt.DocumentQueryServiceCoroutineStub,
    private val driversDriverCalculationService: DriverCalculationServiceGrpcKt.DriverCalculationServiceCoroutineStub,
    private val driversDriverLicenseQueryService: DriverLicenseQueryServiceGrpcKt.DriverLicenseQueryServiceCoroutineStub,
    private val driversDriverService: DriverServiceGrpcKt.DriverServiceCoroutineStub,
    private val driversDutyStatusValidationService: DutyStatusValidationServiceGrpcKt.DutyStatusValidationServiceCoroutineStub,
    private val routingRouteApprovalService: RouteApprovalServiceGrpcKt.RouteApprovalServiceCoroutineStub,
    private val routingRoutePlanValidationService: RoutePlanValidationServiceGrpcKt.RoutePlanValidationServiceCoroutineStub,
    private val routingRouteValidationService: RouteValidationServiceGrpcKt.RouteValidationServiceCoroutineStub,
    private val routingRouteWorkflowService: RouteWorkflowServiceGrpcKt.RouteWorkflowServiceCoroutineStub,
    private val routingStopApprovalService: StopApprovalServiceGrpcKt.StopApprovalServiceCoroutineStub,
    private val routingStopCalculationService: StopCalculationServiceGrpcKt.StopCalculationServiceCoroutineStub,
    private val routingStopValidationService: StopValidationServiceGrpcKt.StopValidationServiceCoroutineStub,
    private val secretsSecretService: SecretServiceGrpcKt.SecretServiceCoroutineStub,
    private val telematicsGeoFenceImportService: GeoFenceImportServiceGrpcKt.GeoFenceImportServiceCoroutineStub,
    private val telematicsTelemetryReadingImportService: TelemetryReadingImportServiceGrpcKt.TelemetryReadingImportServiceCoroutineStub,
) : StopWorkflowServiceGrpcKt.StopWorkflowServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(StopWorkflowEndpoint::class.java)

    /** Advances a stop. */
    override suspend fun advanceStopWorkflow(request: AdvanceStopWorkflowRequest): AdvanceStopWorkflowResponse =
        RpcBoundary.run("acme.routing.v1.StopWorkflowService/AdvanceStopWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "AdvanceStopWorkflow", request)
            log.atInfo().addKeyValue("operation", "AdvanceStopWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.stop.stopId).log("Recorded audit detail")
            val outbound0 = simulateStopRequest {
                this.context = request.context
                this.stop = stop {
                    this.audit = auditInfo {
                        this.createdBy = request.stop.audit.createdBy
                        this.updatedBy = request.stop.audit.updatedBy
                    }
                    this.location = postalAddress {
                        this.recipient = request.stop.location.recipient
                        this.region = request.stop.location.region
                    }
                }
            }
            val reply0 = ClientRegistry.lookup("acme.routing.v1.StopCalculationService", StopCalculationServiceGrpcKt.StopCalculationServiceCoroutineStub::class.java).simulateStop(outbound0)
            val outbound1 = escalateRouteRequest {
                this.context = request.context
                this.details = EscalateRouteRequestKt.details {
                    this.approver = userRef {
                        this.userId = reply0.stop.organizer.userId
                    }
                }
                this.route = route {
                    this.billingAddress = postalAddress {
                        this.region = reply0.stop.location.region
                    }
                }
            }
            val reply1 = routingRouteApprovalService.escalateRoute(WireCopies.reflective(outbound1))
            advanceStopWorkflowResponse {
                this.outcome = AdvanceStopWorkflowResponseKt.outcome {
                    this.message = reply1.result.message
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.status = "ok"
                }
                this.stop = stop {
                    this.audit = auditInfo {
                        this.createdAt = reply0.stop.audit.createdAt
                    }
                    this.metadata.putAll(request.stop.metadataMap)
                    this.stopId = request.stopId
                }
            }
        }

    /** Cancels a stop. */
    override suspend fun cancelStopWorkflow(request: CancelStopWorkflowRequest): CancelStopWorkflowResponse {
        return handleCancelStopWorkflow(request)
    }

    /** Returns a stop. */
    override suspend fun inspectStopWorkflow(request: InspectStopWorkflowRequest): InspectStopWorkflowResponse = handleInspectStopWorkflow(request)

    /** Lists stops a page at a time. */
    override suspend fun listStopWorkflows(request: ListStopWorkflowsRequest): ListStopWorkflowsResponse =
        RpcBoundary.run("acme.routing.v1.StopWorkflowService/ListStopWorkflows") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListStopWorkflows", request)
            log.atInfo().addKeyValue("operation", "ListStopWorkflows").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = cloneDriverRequest {
                this.context = request.context
                this.driver = driver {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                    this.primaryAddress = postalAddress {
                        this.region = request.filter.region
                    }
                    this.relationshipManager = userRef {
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply0 = withTimeout(5_000) { driversDriverService.cloneDriver(outbound0) }
            listStopWorkflowsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.stops.addAll(listOf(stop {
                    this.participants.addAll(listOf(stopParticipant {
                        this.contact = contactInfo {
                            this.phone = reply0.driver.contact.phone
                            this.preferredChannel = reply0.driver.contact.preferredChannel
                        }
                    }))
                }))
            }
        }

    /** Pauses a stop. */
    override suspend fun pauseStopWorkflow(request: PauseStopWorkflowRequest): PauseStopWorkflowResponse {
        return handlePauseStopWorkflow(request)
    }

    /** Resumes a stop. */
    override suspend fun resumeStopWorkflow(request: ResumeStopWorkflowRequest): ResumeStopWorkflowResponse = handleResumeStopWorkflow(request)

    /** Retrys a stop. */
    override suspend fun retryStopWorkflow(request: RetryStopWorkflowRequest): RetryStopWorkflowResponse =
        RpcBoundary.run("acme.routing.v1.StopWorkflowService/RetryStopWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RetryStopWorkflow", request)
            log.atInfo().addKeyValue("operation", "RetryStopWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.stop.stopId).log("Recorded audit detail")
            var reply0 = ScoreRouteResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.stop.participantsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound0 = scoreRouteRequest {
                                this.context = request.context
                                this.details = ScoreRouteRequestKt.details {
                                    this.destination = postalAddress {
                                        this.region = request.stop.location.region
                                    }
                                }
                                this.route = route {
                                    this.audit = auditInfo {
                                        this.updatedAt = request.stop.audit.updatedAt
                                    }
                                    this.counterparty = driverRef {
                                        this.email = item.contact.email
                                    }
                                }
                            }
                            routingRouteValidationService.scoreRoute(outbound0)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply0 = it }
                }
            }
            val outbound1 = delegateRouteApprovalRequest {
                this.context = request.context
                this.route = route {
                    this.audit = auditInfo {
                        this.createdBy = request.stop.audit.createdBy
                        this.etag = request.stop.audit.etag
                    }
                    this.billingAddress = postalAddress {
                        this.region = request.stop.location.region
                    }
                    this.counterparty = driverRef {
                        this.email = request.details.notify.email
                    }
                }
            }
            val reply1 = routingRouteApprovalService.delegateRouteApproval(outbound1)
            retryStopWorkflowResponse {
                this.outcome = RetryStopWorkflowResponseKt.outcome {
                    this.nextSteps.addAll(reply1.outcome.nextStepsList)
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.stop = stop {
                    this.location = request.stop.location
                    this.stopId = request.stopId
                }
            }
        }

    /** Signals a stop. */
    override suspend fun signalStopWorkflow(request: SignalStopWorkflowRequest): SignalStopWorkflowResponse {
        return handleSignalStopWorkflow(request)
    }

    /** Starts a stop. */
    override suspend fun startStopWorkflow(request: StartStopWorkflowRequest): StartStopWorkflowResponse = handleStartStopWorkflow(request)

    private suspend fun handleCancelStopWorkflow(request: CancelStopWorkflowRequest): CancelStopWorkflowResponse =
        RpcBoundary.run("acme.routing.v1.StopWorkflowService/CancelStopWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CancelStopWorkflow", request)
            log.atInfo().addKeyValue("operation", "CancelStopWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.stop.stopId).log("Recorded audit detail")
            val outbound0 = scoreStopRequest {
                this.context = request.context
                this.stop = stop {
                    this.daily = stopDailyRule {
                        this.atHour = when (request.stop.recurrenceCase) {
                            Stop.RecurrenceCase.DAILY -> request.stop.daily.atHour
                            else -> 0
                        }
                        this.interval = when (request.stop.recurrenceCase) {
                            Stop.RecurrenceCase.WEEKLY -> request.stop.weekly.interval
                            else -> 0
                        }
                    }
                    this.participants.addAll(listOf(stopParticipant {
                        this.contact = contactInfo {
                            this.name = request.details.notify.name
                        }
                    }))
                }
            }
            val reply0 = routingStopValidationService.scoreStop(outbound0)
            var reply1 = AdvanceRouteWorkflowResponse.getDefaultInstance()
            request.stop.participantsList.asFlow().map { item ->
                val outbound1 = advanceRouteWorkflowRequest {
                    this.context = request.context
                    this.details = AdvanceRouteWorkflowRequestKt.details {
                        this.notify = contactInfo {
                            this.phone = item.contact.phone
                            this.preferredChannel = item.contact.preferredChannel
                        }
                    }
                    this.route = route {
                        this.counterparty = driverRef {
                            this.email = item.contact.email
                        }
                    }
                }
                routingRouteWorkflowService.advanceRouteWorkflow(outbound1)
            }.collect { reply1 = it }
            val outbound2 = updateSecretRequest {
                this.context = request.context
                this.secret = secret {
                    this.holder = userRef {
                        this.email = reply0.stop.organizer.email
                    }
                }
            }
            val reply2 = flowOf(outbound2).map { value -> secretsSecretService.updateSecret(value) }.single()
            cancelStopWorkflowResponse {
                this.outcome = CancelStopWorkflowResponseKt.outcome {
                    this.status = reply2.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.stop = stop {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.stop.audit.updatedAt
                    }
                    this.location = postalAddress {
                        this.locality = reply0.stop.location.locality
                    }
                    this.participants.addAll(listOf(stopParticipant {
                        this.contact = contactInfo {
                            this.preferredChannel = reply2.secret.contact.preferredChannel
                        }
                    }))
                    this.status = request.stop.status
                    this.stopId = request.stopId
                }
            }
        }

    private suspend fun handleInspectStopWorkflow(request: InspectStopWorkflowRequest): InspectStopWorkflowResponse =
        RpcBoundary.run("acme.routing.v1.StopWorkflowService/InspectStopWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "InspectStopWorkflow", request)
            log.atInfo().addKeyValue("operation", "InspectStopWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            inspectStopWorkflowResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.stop = stop {
                    this.stopId = request.stopId
                }
            }
        }

    private suspend fun handlePauseStopWorkflow(request: PauseStopWorkflowRequest): PauseStopWorkflowResponse =
        RpcBoundary.run("acme.routing.v1.StopWorkflowService/PauseStopWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PauseStopWorkflow", request)
            log.atInfo().addKeyValue("operation", "PauseStopWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.stop.stopId).log("Recorded audit detail")
            var reply0 = ValidateRoutePlanResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<StopParticipant>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound0 = validateRoutePlanRequest {
                            this.context = request.context
                            this.details = ValidateRoutePlanRequestKt.details {
                                this.contact = contactInfo {
                                    this.email = item.contact.email
                                    this.name = item.contact.name
                                    this.phone = item.contact.phone
                                }
                            }
                        }
                        reply0 = routingRoutePlanValidationService.validateRoutePlan(outbound0)
                    }
                }
                try {
                    request.stop.participantsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            pauseStopWorkflowResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.stop = stop {
                    this.stopId = request.stopId
                    this.title = request.stop.title
                }
            }
        }

    private suspend fun handleResumeStopWorkflow(request: ResumeStopWorkflowRequest): ResumeStopWorkflowResponse =
        RpcBoundary.run("acme.routing.v1.StopWorkflowService/ResumeStopWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResumeStopWorkflow", request)
            log.atInfo().addKeyValue("operation", "ResumeStopWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.stop.stopId).log("Recorded audit detail")
            val outbound0 = validateTelemetryReadingImportRequest {
                this.context = request.context
                this.details = ValidateTelemetryReadingImportRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.details.notify.email
                        this.phone = request.details.notify.phone
                        this.preferredChannel = request.details.notify.preferredChannel
                    }
                }
                this.telemetryReading = telemetryReading {
                    this.subject = geoFenceRef {
                        this.displayName = request.stop.organizer.displayName
                    }
                }
            }
            val reply0 = telematicsTelemetryReadingImportService.validateTelemetryReadingImport(outbound0)
            val outbound1 = suggestDocumentsRequest {
                this.context = request.context
                this.filter = SuggestDocumentsRequestKt.filter {
                    this.region = request.stop.location.region
                }
            }
            val reply1 = documentsDocumentQueryService.suggestDocuments(outbound1)
            val outbound2 = browseDriverLicensesRequest {}.driversCopy {
                this.context = request.context
                this.filter = BrowseDriverLicensesRequestKt.filter {
                    this.createdAfter = request.stop.audit.createdAt
                    this.region = request.stop.location.region
                }
            }
            val reply2 = driversDriverLicenseQueryService.browseDriverLicenses(outbound2)
            resumeStopWorkflowResponse {
                this.outcome = ResumeStopWorkflowResponseKt.outcome {
                    this.effectiveAt = reply0.outcome.effectiveAt
                    this.status = reply2.result.status
                }
                this.result = operationResult {
                    this.details.putAll(reply2.result.detailsMap)
                    this.message = reply0.result.message
                    this.status = reply1.result.status
                }
                this.stop = stop {
                    this.status = request.stop.status
                    this.stopId = request.stopId
                }
            }
        }

    private data class SignalStopWorkflow2Command(
        val context: RequestContext,
        val routeBillingAddressPostalCode: String,
        val routeBillingAddressLocality: String,
        val routeBillingAddressRegion: String,
        val routeAuditCreatedAt: Timestamp
    ) {
        fun toRequest(): PreviewRouteRequest {
            val command = this
            return previewRouteRequest {
                this.context = command.context
                this.route = route {
                    this.audit = auditInfo {
                        this.createdAt = command.routeAuditCreatedAt
                    }
                    this.billingAddress = postalAddress {
                        this.locality = command.routeBillingAddressLocality
                        this.postalCode = command.routeBillingAddressPostalCode
                        this.region = command.routeBillingAddressRegion
                    }
                }
            }
        }
    }

    private suspend fun handleSignalStopWorkflow(request: SignalStopWorkflowRequest): SignalStopWorkflowResponse =
        RpcBoundary.run("acme.routing.v1.StopWorkflowService/SignalStopWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SignalStopWorkflow", request)
            log.atInfo().addKeyValue("operation", "SignalStopWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.stop.stopId).log("Recorded audit detail")
            var reply0 = RecallStopResponse.getDefaultInstance()
            request.stop.participantsList.asFlow().map { item ->
                val outbound0 = recallStopRequest {
                    this.context = request.context
                    this.stop = stop {
                        this.participants.addAll(listOf(stopParticipant {
                            this.contact = contactInfo {
                                this.email = item.contact.email
                                this.phone = item.contact.phone
                                this.preferredChannel = item.contact.preferredChannel
                            }
                            this.role = item.role
                        }))
                    }
                }
                routingStopApprovalService.recallStop(outbound0)
            }.collect { reply0 = it }
            val outbound1 = signalRouteWorkflowRequest {
                this.context = request.context
                this.route = route {
                    this.billingAddress = postalAddress {
                        this.countryCode = reply0.stop.location.countryCode
                    }
                }
            }
            val reply1 = routingRouteWorkflowService.signalRouteWorkflow(WireCopies.json(outbound1))
            val outbound2 = SignalStopWorkflow2Command(
                context = request.context,
                routeBillingAddressPostalCode = request.stop.location.postalCode,
                routeBillingAddressLocality = reply0.stop.location.locality,
                routeBillingAddressRegion = reply1.route.billingAddress.region,
                routeAuditCreatedAt = request.stop.audit.createdAt
            ).toRequest()
            val reply2 = routingRouteValidationService.previewRoute(outbound2)
            signalStopWorkflowResponse {
                this.outcome = SignalStopWorkflowResponseKt.outcome {
                    this.nextSteps.addAll(reply2.outcome.nextStepsList)
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.message = reply2.outcome.message
                    this.status = "ok"
                }
                this.stop = stop {
                    this.audit = auditInfo {
                        this.etag = reply1.route.audit.etag
                    }
                    this.location = request.stop.location
                    this.stopId = request.stopId
                }
            }
        }

    private suspend fun handleStartStopWorkflow(request: StartStopWorkflowRequest): StartStopWorkflowResponse =
        RpcBoundary.run("acme.routing.v1.StopWorkflowService/StartStopWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StartStopWorkflow", request)
            log.atInfo().addKeyValue("operation", "StartStopWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.stop.stopId).log("Recorded audit detail")
            var reply0 = NormalizeDutyStatusResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<StopParticipant>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound0 = normalizeDutyStatusRequest {
                            this.context = request.context
                            this.details = NormalizeDutyStatusRequestKt.details {
                                this.contact = contactInfo {
                                    this.email = item.contact.email
                                    this.name = item.contact.name
                                }
                            }
                        }
                        reply0 = driversDutyStatusValidationService.normalizeDutyStatus(outbound0)
                    }
                }
                try {
                    request.stop.participantsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound1 = estimateDriverRequest {
                this.context = request.context
                this.driver = driver {
                    this.contact = contactInfo {
                        this.email = request.stop.organizer.email
                    }
                    this.primaryAddress = postalAddress {
                        this.region = request.stop.location.region
                    }
                }
            }
            val adapter1 = OutboundPort<EstimateDriverRequest, EstimateDriverResponse> { payload ->
                driversDriverCalculationService.estimateDriver(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            var reply2 = RetryGeoFenceImportResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.stop.participantsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound2 = retryGeoFenceImportRequest {
                                this.context = request.context
                                this.details = RetryGeoFenceImportRequestKt.details {
                                    this.notify = contactInfo {
                                        this.name = item.contact.name
                                        this.phone = item.contact.phone
                                        this.preferredChannel = item.contact.preferredChannel
                                    }
                                }
                                this.geoFence = geoFence {
                                    this.owner = userRef {
                                        this.email = item.contact.email
                                    }
                                }
                            }
                            telematicsGeoFenceImportService.retryGeoFenceImport(outbound2)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply2 = it }
                }
            }
            startStopWorkflowResponse {
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.status = "ok"
                }
                this.stop = stop {
                    this.location = request.stop.location
                    this.stopId = request.stopId
                }
            }
        }
}
