package com.acme.transport.yards.adapters

import com.acme.audit.v1.AuditEventServiceGrpcKt
import com.acme.audit.v1.batchGetAuditEventsRequest
import com.acme.authorization.v1.AccessPolicyValidationServiceGrpcKt
import com.acme.authorization.v1.ScoreAccessPolicyRequest
import com.acme.authorization.v1.accessPolicy
import com.acme.authorization.v1.scoreAccessPolicyRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.CompareDeliveryExceptionOptionsRequest
import com.acme.deliveries.v1.CompareDeliveryExceptionOptionsRequestKt
import com.acme.deliveries.v1.DeliveryExceptionCalculationServiceGrpcKt
import com.acme.deliveries.v1.compareDeliveryExceptionOptionsRequest
import com.acme.deliveries.v1.deliveryException
import com.acme.fleet.v1.FindVehiclesRequest
import com.acme.fleet.v1.FindVehiclesRequestKt
import com.acme.fleet.v1.VehicleQueryServiceGrpcKt
import com.acme.fleet.v1.findVehiclesRequest
import com.acme.identity.v1.userRef
import com.acme.payroll.v1.PayRunCalculationServiceGrpcKt
import com.acme.payroll.v1.copy as payrollCopy
import com.acme.payroll.v1.deductionRef
import com.acme.payroll.v1.estimatePayRunRequest
import com.acme.payroll.v1.payRun
import com.acme.routing.v1.FindRoutesRequestKt
import com.acme.routing.v1.RecalculateRoutePlansRequest
import com.acme.routing.v1.RecalculateRoutePlansRequestKt
import com.acme.routing.v1.RoutePlanCalculationServiceGrpcKt
import com.acme.routing.v1.RouteQueryServiceGrpcKt
import com.acme.routing.v1.findRoutesRequest
import com.acme.routing.v1.recalculateRoutePlansRequest
import com.acme.routing.v1.routePlan
import com.acme.telematics.v1.CompareGeoFenceOptionsRequestKt
import com.acme.telematics.v1.GeoFenceCalculationServiceGrpcKt
import com.acme.telematics.v1.ScoreTelemetryReadingRequest
import com.acme.telematics.v1.ScoreTelemetryReadingRequestKt
import com.acme.telematics.v1.ScoreTelemetryReadingResponse
import com.acme.telematics.v1.TelemetryReadingValidationServiceGrpcKt
import com.acme.telematics.v1.compareGeoFenceOptionsRequest
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.scoreTelemetryReadingRequest
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.yards.YardsRepository
import com.acme.yards.v1.AdvanceYardWorkflowRequest
import com.acme.yards.v1.AdvanceYardWorkflowResponse
import com.acme.yards.v1.BrowseYardsRequest
import com.acme.yards.v1.BrowseYardsRequestKt
import com.acme.yards.v1.BrowseYardsResponse
import com.acme.yards.v1.CancelYardWorkflowRequest
import com.acme.yards.v1.CancelYardWorkflowResponse
import com.acme.yards.v1.CompareDockOptionsRequestKt
import com.acme.yards.v1.CountYardMoveTotalsRequest
import com.acme.yards.v1.CountYardMoveTotalsRequestKt
import com.acme.yards.v1.CountYardMoveTotalsResponse
import com.acme.yards.v1.DockCalculationServiceGrpcKt
import com.acme.yards.v1.DockQueryServiceGrpcKt
import com.acme.yards.v1.ExplainYardDecisionRequest
import com.acme.yards.v1.ExplainYardDecisionResponse
import com.acme.yards.v1.GenerateYardMoveReportRequestKt
import com.acme.yards.v1.InspectYardWorkflowRequest
import com.acme.yards.v1.InspectYardWorkflowResponse
import com.acme.yards.v1.ListYardWorkflowsRequest
import com.acme.yards.v1.ListYardWorkflowsResponse
import com.acme.yards.v1.PauseYardWorkflowRequest
import com.acme.yards.v1.PauseYardWorkflowResponse
import com.acme.yards.v1.PauseYardWorkflowResponseKt
import com.acme.yards.v1.ResumeYardWorkflowRequest
import com.acme.yards.v1.ResumeYardWorkflowResponse
import com.acme.yards.v1.ResumeYardWorkflowResponseKt
import com.acme.yards.v1.RetryYardWorkflowRequest
import com.acme.yards.v1.RetryYardWorkflowResponse
import com.acme.yards.v1.SignalYardWorkflowRequest
import com.acme.yards.v1.SignalYardWorkflowResponse
import com.acme.yards.v1.SimulateDockRequestKt
import com.acme.yards.v1.StartYardWorkflowRequest
import com.acme.yards.v1.StartYardWorkflowResponse
import com.acme.yards.v1.StartYardWorkflowResponseKt
import com.acme.yards.v1.SuggestDocksRequestKt
import com.acme.yards.v1.WatchYardsRequestKt
import com.acme.yards.v1.Yard
import com.acme.yards.v1.YardComponent
import com.acme.yards.v1.YardMoveReportingServiceGrpcKt
import com.acme.yards.v1.YardQueryServiceGrpcKt
import com.acme.yards.v1.YardServiceGrpcKt
import com.acme.yards.v1.YardValidationServiceGrpcKt
import com.acme.yards.v1.YardWorkflowServiceGrpcKt
import com.acme.yards.v1.advanceYardWorkflowResponse
import com.acme.yards.v1.batchGetYardsRequest
import com.acme.yards.v1.browseYardsRequest
import com.acme.yards.v1.cancelYardWorkflowResponse
import com.acme.yards.v1.compareDockOptionsRequest
import com.acme.yards.v1.countYardMoveTotalsRequest
import com.acme.yards.v1.dock
import com.acme.yards.v1.dockDimensions
import com.acme.yards.v1.explainYardDecisionRequest
import com.acme.yards.v1.generateYardMoveReportRequest
import com.acme.yards.v1.inspectYardWorkflowResponse
import com.acme.yards.v1.listYardWorkflowsResponse
import com.acme.yards.v1.pauseYardWorkflowResponse
import com.acme.yards.v1.resumeYardWorkflowResponse
import com.acme.yards.v1.retryYardWorkflowResponse
import com.acme.yards.v1.scheduleYardMoveReportRequest
import com.acme.yards.v1.signalYardWorkflowResponse
import com.acme.yards.v1.simulateDockRequest
import com.acme.yards.v1.startYardWorkflowResponse
import com.acme.yards.v1.suggestDocksRequest
import com.acme.yards.v1.watchYardsRequest
import com.acme.yards.v1.yard
import com.acme.yards.v1.yardComponent
import com.google.protobuf.Any as ProtoAny
import com.google.protobuf.Timestamp
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Runs the long-lived workflow that takes a yard from start to finish. */
class YardWorkflowController(
    private val store: YardsRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventService: AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub,
    private val authorizationAccessPolicyValidationService: AccessPolicyValidationServiceGrpcKt.AccessPolicyValidationServiceCoroutineStub,
    private val deliveriesDeliveryExceptionCalculationService: DeliveryExceptionCalculationServiceGrpcKt.DeliveryExceptionCalculationServiceCoroutineStub,
    private val fleetVehicleQueryService: VehicleQueryServiceGrpcKt.VehicleQueryServiceCoroutineStub,
    private val payrollPayRunCalculationService: PayRunCalculationServiceGrpcKt.PayRunCalculationServiceCoroutineStub,
    private val routingRoutePlanCalculationService: RoutePlanCalculationServiceGrpcKt.RoutePlanCalculationServiceCoroutineStub,
    private val routingRouteQueryService: RouteQueryServiceGrpcKt.RouteQueryServiceCoroutineStub,
    private val telematicsGeoFenceCalculationService: GeoFenceCalculationServiceGrpcKt.GeoFenceCalculationServiceCoroutineStub,
    private val telematicsTelemetryReadingValidationService: TelemetryReadingValidationServiceGrpcKt.TelemetryReadingValidationServiceCoroutineStub,
    private val yardsDockCalculationService: DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub,
    private val yardsDockQueryService: DockQueryServiceGrpcKt.DockQueryServiceCoroutineStub,
    private val yardsYardMoveReportingService: YardMoveReportingServiceGrpcKt.YardMoveReportingServiceCoroutineStub,
    private val yardsYardQueryService: YardQueryServiceGrpcKt.YardQueryServiceCoroutineStub,
    private val yardsYardService: YardServiceGrpcKt.YardServiceCoroutineStub,
    private val yardsYardValidationService: YardValidationServiceGrpcKt.YardValidationServiceCoroutineStub,
) : YardWorkflowServiceGrpcKt.YardWorkflowServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(YardWorkflowController::class.java)

    private val workerForAdvanceYardWorkflow0 = ReplyMailbox<CountYardMoveTotalsRequest, CountYardMoveTotalsResponse>(workers) { payload ->
        withTimeout(5_000) { yardsYardMoveReportingService.countYardMoveTotals(payload) }
    }

    private val clientForAdvanceYardWorkflow1 by lazy { auditAuditEventService }

    private val invokeRetryYardWorkflow1: suspend (ExplainYardDecisionRequest) -> ExplainYardDecisionResponse = { payload ->
        yardsYardValidationService.explainYardDecision(payload)
    }

    /** Advances a yard. */
    override suspend fun advanceYardWorkflow(request: AdvanceYardWorkflowRequest): AdvanceYardWorkflowResponse = handleAdvanceYardWorkflow(request)

    /** Cancels a yard. */
    override suspend fun cancelYardWorkflow(request: CancelYardWorkflowRequest): CancelYardWorkflowResponse =
        RpcBoundary.run("acme.yards.v1.YardWorkflowService/CancelYardWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CancelYardWorkflow", request)
            log.atInfo().addKeyValue("operation", "CancelYardWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yard.yardId).log("Recorded audit detail")
            val outbound0 = simulateDockRequest {
                this.context = request.context
                this.details = SimulateDockRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.yard.address.region
                    }
                }
                this.dock = dock {
                    this.address = postalAddress {
                        this.locality = request.yard.address.locality
                    }
                    this.audit = auditInfo {
                        this.createdBy = request.yard.audit.createdBy
                    }
                    this.dimensions = dockDimensions {
                        this.width = request.yard.dimensions.width
                    }
                }
            }
            val reply0 = withTimeout(5_000) { yardsDockCalculationService.simulateDock(outbound0) }
            val configure1: CompareGeoFenceOptionsRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.updatedBy = request.yard.audit.updatedBy
                    }
                }
            }
            val outbound1 = compareGeoFenceOptionsRequest(configure1)
            val reply1 = telematicsGeoFenceCalculationService.compareGeoFenceOptions(outbound1)
            cancelYardWorkflowResponse {
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.status = "ok"
                }
                this.yard = yard {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.dock.audit.updatedAt
                    }
                    this.owner = reply1.geoFence.owner
                    this.status = request.yard.status
                    this.yardId = request.yardId
                }
            }
        }

    /** Returns a yard. */
    override suspend fun inspectYardWorkflow(request: InspectYardWorkflowRequest): InspectYardWorkflowResponse {
        return handleInspectYardWorkflow(request)
    }

    /** Lists yards a page at a time. */
    override suspend fun listYardWorkflows(request: ListYardWorkflowsRequest): ListYardWorkflowsResponse = handleListYardWorkflows(request)

    /** Pauses a yard. */
    override suspend fun pauseYardWorkflow(request: PauseYardWorkflowRequest): PauseYardWorkflowResponse =
        RpcBoundary.run("acme.yards.v1.YardWorkflowService/PauseYardWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PauseYardWorkflow", request)
            log.atInfo().addKeyValue("operation", "PauseYardWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yard.yardId).log("Recorded audit detail")
            val outbound0 = request.toRecalculateRoutePlansFromPauseYardWorkflow0()
            val reply0 = routingRoutePlanCalculationService.recalculateRoutePlans(outbound0)
            val outbound1 = findRoutesRequest {
                this.context = request.context
                this.filter = FindRoutesRequestKt.filter {
                    this.createdAfter = request.yard.audit.createdAt
                    this.labels.putAll(request.details.parametersMap)
                    this.ownerId = reply0.routePlan.counterparty.driverId
                    this.region = when (request.yard.locatorCase) {
                        Yard.LocatorCase.ADDRESS -> request.yard.address.region
                        else -> ""
                    }
                }
            }
            val reply1 = routingRouteQueryService.findRoutes(outbound1)
            val outbound2 = compareDeliveryExceptionOptionsRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.assignee = userRef {
                        this.displayName = reply0.routePlan.counterparty.displayName
                    }
                    this.audit = auditInfo {
                        this.createdAt = request.yard.audit.createdAt
                        this.updatedBy = reply0.routePlan.audit.updatedBy
                    }
                }
                this.details = CompareDeliveryExceptionOptionsRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = reply0.routePlan.billingAddress.countryCode
                    }
                }
            }
            val envelope2 = ProtoAny.pack(outbound2)
            val reply2 = deliveriesDeliveryExceptionCalculationService.compareDeliveryExceptionOptions(envelope2.unpack(CompareDeliveryExceptionOptionsRequest::class.java))
            val outbound3 = watchYardsRequest {
                this.context = request.context
                this.filter = WatchYardsRequestKt.filter {
                    this.region = reply0.routePlan.billingAddress.region
                }
                this.subscriber = contactInfo {
                    this.email = request.yard.owner.email
                    this.name = reply2.deliveryException.requester.name
                    this.phone = request.details.notify.phone
                }
            }
            yardsYardService.watchYards(outbound3).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            pauseYardWorkflowResponse {
                this.outcome = PauseYardWorkflowResponseKt.outcome {
                    this.message = reply1.result.message
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
                this.yard = yard {
                    this.audit = auditInfo {
                        this.createdBy = reply2.deliveryException.audit.createdBy
                        this.updatedAt = reply0.routePlan.audit.updatedAt
                    }
                    this.code = request.yard.code
                    this.yardId = request.yardId
                }
            }
        }

    /** Resumes a yard. */
    override suspend fun resumeYardWorkflow(request: ResumeYardWorkflowRequest): ResumeYardWorkflowResponse {
        return handleResumeYardWorkflow(request)
    }

    /** Retrys a yard. */
    override suspend fun retryYardWorkflow(request: RetryYardWorkflowRequest): RetryYardWorkflowResponse = handleRetryYardWorkflow(request)

    /** Signals a yard. */
    override suspend fun signalYardWorkflow(request: SignalYardWorkflowRequest): SignalYardWorkflowResponse =
        RpcBoundary.run("acme.yards.v1.YardWorkflowService/SignalYardWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SignalYardWorkflow", request)
            log.atInfo().addKeyValue("operation", "SignalYardWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yard.yardId).log("Recorded audit detail")
            val outbound0 = scoreTelemetryReadingRequest {
                this.context = request.context
                this.details = ScoreTelemetryReadingRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = request.yard.address.countryCode
                    }
                }
            }
            val invoke0: suspend (ScoreTelemetryReadingRequest, Metadata) -> ScoreTelemetryReadingResponse = telematicsTelemetryReadingValidationService::scoreTelemetryReading
            val reply0 = invoke0(outbound0, Metadata())
            val outbound1 = ExplainYardDecisionRequest.newBuilder().apply {
                setContext(request.context)
                setYard(yard {
                    this.components.putAll(mapOf("primary" to yardComponent {
                        this.quantity = (request.yard.componentsMap["primary"] ?: YardComponent.getDefaultInstance()).quantity
                    }))
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> yardsYardValidationService.explainYardDecision(value) }
            signalYardWorkflowResponse {
                this.result = operationResult {
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = reply0.result.status
                }
                this.yard = yard {
                    this.code = request.yard.code
                    this.yardId = request.yardId
                }
            }
        }

    /** Starts a yard. */
    override suspend fun startYardWorkflow(request: StartYardWorkflowRequest): StartYardWorkflowResponse {
        return handleStartYardWorkflow(request)
    }

    private suspend fun handleAdvanceYardWorkflow(request: AdvanceYardWorkflowRequest): AdvanceYardWorkflowResponse =
        RpcBoundary.run("acme.yards.v1.YardWorkflowService/AdvanceYardWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "AdvanceYardWorkflow", request)
            log.atInfo().addKeyValue("operation", "AdvanceYardWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yard.yardId).log("Recorded audit detail")
            val outbound0 = countYardMoveTotalsRequest {
                this.context = request.context
                this.filter = CountYardMoveTotalsRequestKt.filter {
                    this.region = request.yard.address.region
                }
            }
            val reply0 = workerForAdvanceYardWorkflow0.submit(outbound0)
            val outbound1 = batchGetAuditEventsRequest {
                this.context = request.context
            }
            val reply1 = clientForAdvanceYardWorkflow1.batchGetAuditEvents(outbound1)
            val outbound2 = batchGetYardsRequest {
                this.context = request.context
            }
            val reply2 = flowOf(outbound2).map { value -> yardsYardService.batchGetYards(value) }.single()
            advanceYardWorkflowResponse {
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = reply1.result.status
                }
                this.yard = yard {
                    this.name = request.yard.name
                    this.yardId = request.yardId
                }
            }
        }

    private suspend fun handleInspectYardWorkflow(request: InspectYardWorkflowRequest): InspectYardWorkflowResponse =
        RpcBoundary.run("acme.yards.v1.YardWorkflowService/InspectYardWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "InspectYardWorkflow", request)
            log.atInfo().addKeyValue("operation", "InspectYardWorkflow").log("Handling transport request")
            val outbound0 = generateYardMoveReportRequest {
                this.context = request.context
                this.details = GenerateYardMoveReportRequestKt.details {
                    this.filters.putAll(request.context.baggageMap)
                }
            }
            val reply0 = yardsYardMoveReportingService.generateYardMoveReport(outbound0)
            inspectYardWorkflowResponse {
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = reply0.result.status
                }
                this.yard = yard {
                    this.yardId = request.yardId
                }
            }
        }

    private suspend fun handleListYardWorkflows(request: ListYardWorkflowsRequest): ListYardWorkflowsResponse =
        RpcBoundary.run("acme.yards.v1.YardWorkflowService/ListYardWorkflows") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListYardWorkflows", request)
            log.atInfo().addKeyValue("operation", "ListYardWorkflows").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = compareDockOptionsRequest {
                this.context = request.context
                this.details = CompareDockOptionsRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.filter.region
                    }
                }
                this.dock = dock {
                    this.owner = userRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = yardsDockCalculationService.compareDockOptions(outbound0)
            val outbound1 = estimatePayRunRequest {}.payrollCopy {
                this.context = request.context
                this.payRun = payRun {
                    this.counterparty = deductionRef {
                        this.displayName = reply0.dock.owner.displayName
                    }
                }
            }
            val reply1 = payrollPayRunCalculationService.estimatePayRun(outbound1)
            listYardWorkflowsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.yards.addAll(listOf(yard {
                    this.audit = auditInfo {
                        this.updatedAt = reply1.payRun.audit.updatedAt
                    }
                    this.tags.addAll(reply0.dock.tagsList)
                }))
            }
        }

    private fun PauseYardWorkflowRequest.toRecalculateRoutePlansFromPauseYardWorkflow0(): RecalculateRoutePlansRequest {
        val source = this
        return recalculateRoutePlansRequest {
            this.context = source.context
            this.details = RecalculateRoutePlansRequestKt.details {
                this.destination = postalAddress {
                    this.locality = source.yard.address.locality
                }
            }
            this.routePlan = routePlan {
                this.audit = auditInfo {
                    this.createdAt = source.yard.audit.createdAt
                }
                this.billingAddress = postalAddress {
                    this.lines.addAll(source.yard.address.linesList)
                    this.region = source.yard.address.region
                }
            }
        }
    }

    private data class ResumeYardWorkflow0Command(
        val context: RequestContext,
        val filterRegion: String,
        val filterCreatedAfter: Timestamp
    ) {
        fun toRequest(): FindVehiclesRequest {
            val command = this
            return findVehiclesRequest {
                this.context = command.context
                this.filter = FindVehiclesRequestKt.filter {
                    this.createdAfter = command.filterCreatedAfter
                    this.region = command.filterRegion
                }
            }
        }
    }

    private suspend fun handleResumeYardWorkflow(request: ResumeYardWorkflowRequest): ResumeYardWorkflowResponse =
        RpcBoundary.run("acme.yards.v1.YardWorkflowService/ResumeYardWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResumeYardWorkflow", request)
            log.atInfo().addKeyValue("operation", "ResumeYardWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yard.yardId).log("Recorded audit detail")
            val outbound0 = ResumeYardWorkflow0Command(
                context = request.context,
                filterRegion = request.yard.address.region,
                filterCreatedAfter = request.yard.audit.createdAt
            ).toRequest()
            val reply0 = fleetVehicleQueryService.findVehicles(outbound0)
            val outbound1 = scoreAccessPolicyRequest {
                this.accessPolicy = accessPolicy {
                    this.description = (request.yard.componentsMap["primary"] ?: YardComponent.getDefaultInstance()).description
                    this.owner = userRef {
                        this.email = request.details.notify.email
                        this.userId = request.yard.owner.userId
                    }
                }
                this.context = request.context
            }
            val command1: Submission<ScoreAccessPolicyRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = authorizationAccessPolicyValidationService.scoreAccessPolicy(payload1)
            resumeYardWorkflowResponse {
                this.outcome = ResumeYardWorkflowResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
                this.yard = yard {
                    this.components.putAll(mapOf("primary" to yardComponent {
                        this.description = reply1.accessPolicy.description
                    }))
                    this.name = reply1.accessPolicy.name
                    this.owner = request.yard.owner
                    this.yardId = request.yardId
                }
            }
        }

    private suspend fun handleRetryYardWorkflow(request: RetryYardWorkflowRequest): RetryYardWorkflowResponse =
        RpcBoundary.run("acme.yards.v1.YardWorkflowService/RetryYardWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RetryYardWorkflow", request)
            log.atInfo().addKeyValue("operation", "RetryYardWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yard.yardId).log("Recorded audit detail")
            val outbound0 = browseYardsRequest {
                this.context = request.context
                this.filter = BrowseYardsRequestKt.filter {
                    this.region = request.yard.address.region
                }
            }
            val adapter0 = OutboundPort<BrowseYardsRequest, BrowseYardsResponse> { payload ->
                yardsYardQueryService.browseYards(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = explainYardDecisionRequest {
                this.context = request.context
                this.yard = yard {
                    this.address = postalAddress {
                        this.locality = request.yard.address.locality
                    }
                    this.audit = auditInfo {
                        this.createdBy = request.yard.audit.createdBy
                    }
                    this.components.putAll(mapOf("primary" to yardComponent {
                        this.quantity = (request.yard.componentsMap["primary"] ?: YardComponent.getDefaultInstance()).quantity
                    }))
                }
            }
            val reply1 = invokeRetryYardWorkflow1(outbound1)
            retryYardWorkflowResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
                this.yard = yard {
                    this.owner = userRef {
                        this.displayName = reply1.yard.owner.displayName
                    }
                    this.specs.putAll(request.yard.specsMap)
                    this.yardId = request.yardId
                }
            }
        }

    private suspend fun handleStartYardWorkflow(request: StartYardWorkflowRequest): StartYardWorkflowResponse =
        RpcBoundary.run("acme.yards.v1.YardWorkflowService/StartYardWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StartYardWorkflow", request)
            log.atInfo().addKeyValue("operation", "StartYardWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yard.yardId).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                suggestDocksRequest {
                    this.context = request.context
                    this.filter = SuggestDocksRequestKt.filter {
                        this.createdAfter = request.yard.audit.updatedAt
                        this.region = request.yard.address.region
                    }
                }
            }
            val reply0 = yardsDockQueryService.suggestDocks(outbound0)
            val outbound1 = scheduleYardMoveReportRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.reason = request.reason
            }
            val reply1 = ClientRegistry.lookup("acme.yards.v1.YardMoveReportingService", YardMoveReportingServiceGrpcKt.YardMoveReportingServiceCoroutineStub::class.java).scheduleYardMoveReport(outbound1)
            val outbound2 = watchYardsRequest {
                this.context = request.context
                this.filter = WatchYardsRequestKt.filter {
                    this.region = request.yard.address.region
                }
                this.subscriber = contactInfo {
                    this.email = request.details.notify.email
                    this.name = request.details.notify.name
                    this.preferredChannel = request.details.notify.preferredChannel
                }
            }
            yardsYardService.watchYards(outbound2).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            startYardWorkflowResponse {
                this.outcome = StartYardWorkflowResponseKt.outcome {
                    this.message = reply0.result.message
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.status = reply1.result.status
                }
                this.yard = yard {
                    this.capacity = request.yard.capacity
                    this.yardId = request.yardId
                }
            }
        }
}
