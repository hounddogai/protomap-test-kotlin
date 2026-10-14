package com.acme.transport.yards.api

import com.acme.broadcasts.v1.BroadcastQueryServiceGrpcKt
import com.acme.broadcasts.v1.SummarizeBroadcastsRequest
import com.acme.broadcasts.v1.SummarizeBroadcastsResponse
import com.acme.broadcasts.v1.summarizeBroadcastsRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.DeliveryExceptionCalculationServiceGrpcKt
import com.acme.deliveries.v1.EstimateDeliveryExceptionRequest
import com.acme.deliveries.v1.EstimateDeliveryExceptionResponse
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.estimateDeliveryExceptionRequest
import com.acme.dispatch.v1.DispatchOrderServiceGrpcKt
import com.acme.dispatch.v1.watchDispatchOrdersRequest
import com.acme.documents.v1.DocumentQueryServiceGrpcKt
import com.acme.documents.v1.SuggestDocumentsRequest
import com.acme.documents.v1.SuggestDocumentsRequestKt
import com.acme.documents.v1.suggestDocumentsRequest
import com.acme.fleet.v1.ExplainVehicleDecisionRequestKt
import com.acme.fleet.v1.VehicleValidationServiceGrpcKt
import com.acme.fleet.v1.explainVehicleDecisionRequest
import com.acme.fleet.v1.vehicle
import com.acme.identity.v1.userRef
import com.acme.ratelimiting.v1.CloneRateLimitRequest
import com.acme.ratelimiting.v1.RateLimitServiceGrpcKt
import com.acme.ratelimiting.v1.cloneRateLimitRequest
import com.acme.ratelimiting.v1.rateLimit
import com.acme.tours.v1.SummarizeToursRequest
import com.acme.tours.v1.TourQueryServiceGrpcKt
import com.acme.tours.v1.summarizeToursRequest
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.transport.yards.YardsRepository
import com.acme.yards.v1.ActivateYardMoveRequest
import com.acme.yards.v1.ActivateYardMoveResponse
import com.acme.yards.v1.ActivateYardMoveResponseKt
import com.acme.yards.v1.CountDocksRequestKt
import com.acme.yards.v1.DockQueryServiceGrpcKt
import com.acme.yards.v1.DockValidationServiceGrpcKt
import com.acme.yards.v1.EstimateYardRequest
import com.acme.yards.v1.EstimateYardResponse
import com.acme.yards.v1.ExpireYardMoveRequest
import com.acme.yards.v1.ExpireYardMoveResponse
import com.acme.yards.v1.ExpireYardMoveResponseKt
import com.acme.yards.v1.GetYardMoveLifecycleRequest
import com.acme.yards.v1.GetYardMoveLifecycleResponse
import com.acme.yards.v1.ListYardMoveReportsRequestKt
import com.acme.yards.v1.ListYardMoveReportsResponse
import com.acme.yards.v1.NormalizeYardMoveRequestKt
import com.acme.yards.v1.RenewYardMoveRequest
import com.acme.yards.v1.RenewYardMoveResponse
import com.acme.yards.v1.RenewYardMoveResponseKt
import com.acme.yards.v1.ScheduleYardMoveExpiryRequest
import com.acme.yards.v1.ScheduleYardMoveExpiryResponse
import com.acme.yards.v1.ScoreDockRequest
import com.acme.yards.v1.ScoreDockRequestKt
import com.acme.yards.v1.ScoreDockResponse
import com.acme.yards.v1.SuggestDocksRequest
import com.acme.yards.v1.SuggestDocksRequestKt
import com.acme.yards.v1.SuspendYardMoveRequest
import com.acme.yards.v1.SuspendYardMoveResponse
import com.acme.yards.v1.SuspendYardMoveResponseKt
import com.acme.yards.v1.TerminateYardMoveRequest
import com.acme.yards.v1.TerminateYardMoveResponse
import com.acme.yards.v1.TerminateYardMoveResponseKt
import com.acme.yards.v1.YardCalculationServiceGrpcKt
import com.acme.yards.v1.YardMoveLifecycleServiceGrpcKt
import com.acme.yards.v1.YardMoveReportingServiceGrpcKt
import com.acme.yards.v1.YardMoveValidationServiceGrpcKt
import com.acme.yards.v1.activateYardMoveResponse
import com.acme.yards.v1.countDocksRequest
import com.acme.yards.v1.dock
import com.acme.yards.v1.dockDimensions
import com.acme.yards.v1.estimateYardRequest
import com.acme.yards.v1.expireYardMoveResponse
import com.acme.yards.v1.getYardMoveLifecycleResponse
import com.acme.yards.v1.listYardMoveReportsRequest
import com.acme.yards.v1.normalizeYardMoveRequest
import com.acme.yards.v1.renewYardMoveResponse
import com.acme.yards.v1.scheduleYardMoveExpiryResponse
import com.acme.yards.v1.scoreDockRequest
import com.acme.yards.v1.suggestDocksRequest
import com.acme.yards.v1.suspendYardMoveResponse
import com.acme.yards.v1.terminateYardMoveResponse
import com.acme.yards.v1.yard
import com.acme.yards.v1.yardMove
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Moves a yard move between the states of its lifecycle. */
class YardMoveLifecycleGrpcService(
    private val store: YardsRepository,
    private val workers: CoroutineScope,
    private val broadcastsBroadcastQueryService: BroadcastQueryServiceGrpcKt.BroadcastQueryServiceCoroutineStub,
    private val deliveriesDeliveryExceptionCalculationService: DeliveryExceptionCalculationServiceGrpcKt.DeliveryExceptionCalculationServiceCoroutineStub,
    private val dispatchDispatchOrderService: DispatchOrderServiceGrpcKt.DispatchOrderServiceCoroutineStub,
    private val documentsDocumentQueryService: DocumentQueryServiceGrpcKt.DocumentQueryServiceCoroutineStub,
    private val fleetVehicleValidationService: VehicleValidationServiceGrpcKt.VehicleValidationServiceCoroutineStub,
    private val ratelimitingRateLimitService: RateLimitServiceGrpcKt.RateLimitServiceCoroutineStub,
    private val toursTourQueryService: TourQueryServiceGrpcKt.TourQueryServiceCoroutineStub,
    private val yardsDockQueryService: DockQueryServiceGrpcKt.DockQueryServiceCoroutineStub,
    private val yardsDockValidationService: DockValidationServiceGrpcKt.DockValidationServiceCoroutineStub,
    private val yardsYardCalculationService: YardCalculationServiceGrpcKt.YardCalculationServiceCoroutineStub,
    private val yardsYardMoveReportingService: YardMoveReportingServiceGrpcKt.YardMoveReportingServiceCoroutineStub,
    private val yardsYardMoveValidationService: YardMoveValidationServiceGrpcKt.YardMoveValidationServiceCoroutineStub,
) : YardMoveLifecycleServiceGrpcKt.YardMoveLifecycleServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(YardMoveLifecycleGrpcService::class.java)

    private val invokeExpireYardMove3: suspend (ScoreDockRequest) -> ScoreDockResponse = { payload ->
        yardsDockValidationService.scoreDock(payload)
    }

    private val workerForTerminateYardMove0 = ReplyMailbox<SummarizeBroadcastsRequest, SummarizeBroadcastsResponse>(workers) { payload ->
        withTimeout(5_000) { broadcastsBroadcastQueryService.summarizeBroadcasts(payload) }
    }

    /** Activates a yard move. */
    override suspend fun activateYardMove(request: ActivateYardMoveRequest): ActivateYardMoveResponse {
        return handleActivateYardMove(request)
    }

    /** Expires a yard move. */
    override suspend fun expireYardMove(request: ExpireYardMoveRequest): ExpireYardMoveResponse = handleExpireYardMove(request)

    /** Returns a yard move. */
    override suspend fun getYardMoveLifecycle(request: GetYardMoveLifecycleRequest): GetYardMoveLifecycleResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveLifecycleService/GetYardMoveLifecycle") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetYardMoveLifecycle", request)
            log.atInfo().addKeyValue("operation", "GetYardMoveLifecycle").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            getYardMoveLifecycleResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.yardMove = yardMove {
                    this.yardMoveId = request.yardMoveId
                }
            }
        }

    /** Renews a yard move. */
    override suspend fun renewYardMove(request: RenewYardMoveRequest): RenewYardMoveResponse {
        return handleRenewYardMove(request)
    }

    /** Schedules a yard move. */
    override suspend fun scheduleYardMoveExpiry(request: ScheduleYardMoveExpiryRequest): ScheduleYardMoveExpiryResponse = handleScheduleYardMoveExpiry(request)

    /** Suspends a yard move. */
    override suspend fun suspendYardMove(request: SuspendYardMoveRequest): SuspendYardMoveResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveLifecycleService/SuspendYardMove") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SuspendYardMove", request)
            log.atInfo().addKeyValue("operation", "SuspendYardMove").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = estimateYardRequest {
                this.context = request.context
                this.yard = yard {
                    this.owner = userRef {
                        this.email = request.details.newOwner.email
                        this.userId = request.details.newOwner.userId
                    }
                }
            }
            val reply0 = yardsYardCalculationService.estimateYard(WireCopies.reflective(outbound0))
            suspendYardMoveResponse {
                this.outcome = SuspendYardMoveResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.yardMove = yardMove {
                    this.audit = auditInfo {
                        this.createdAt = reply0.yard.audit.createdAt
                        this.etag = reply0.yard.audit.etag
                    }
                    this.yardMoveId = request.yardMoveId
                }
            }
        }

    /** Terminates a yard move. */
    override suspend fun terminateYardMove(request: TerminateYardMoveRequest): TerminateYardMoveResponse {
        return handleTerminateYardMove(request)
    }

    private suspend fun handleActivateYardMove(request: ActivateYardMoveRequest): ActivateYardMoveResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveLifecycleService/ActivateYardMove") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ActivateYardMove", request)
            log.atInfo().addKeyValue("operation", "ActivateYardMove").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = normalizeYardMoveRequest {
                this.context = request.context
                this.details = NormalizeYardMoveRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.details.notifyContact.email
                        this.name = request.details.notifyContact.name
                    }
                }
            }
            val reply0 = yardsYardMoveValidationService.normalizeYardMove(outbound0)
            val outbound1 = cloneRateLimitRequest {
                this.context = request.context
                this.rateLimit = rateLimit {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.yardMove.audit.updatedBy
                    }
                    this.owner = userRef {
                        this.userId = reply0.yardMove.assignee.userId
                    }
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = ratelimitingRateLimitService.cloneRateLimit(envelope1.unpack(CloneRateLimitRequest::class.java))
            val outbound2 = explainVehicleDecisionRequest {
                this.context = request.context
                this.details = ExplainVehicleDecisionRequestKt.details {
                    this.contact = contactInfo {
                        this.preferredChannel = reply0.yardMove.requester.preferredChannel
                    }
                }
                this.vehicle = vehicle {
                    this.audit = auditInfo {
                        this.createdBy = reply1.rateLimit.audit.createdBy
                    }
                    this.owner = userRef {
                        this.displayName = reply1.rateLimit.owner.displayName
                    }
                }
            }
            val reply2 = fleetVehicleValidationService.explainVehicleDecision(WireCopies.json(outbound2))
            activateYardMoveResponse {
                this.outcome = ActivateYardMoveResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.message = reply1.result.message
                    this.status = reply2.outcome.status
                }
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
                this.yardMove = yardMove {
                    this.assignee = userRef {
                        this.email = reply0.yardMove.assignee.email
                    }
                    this.priority = reply1.rateLimit.priority
                    this.yardMoveId = request.yardMoveId
                }
            }
        }

    private data class ExpireYardMove0Command(
        val context: RequestContext,
        val reason: String,
        val annotations: Map<String, String>
    ) {
        fun toRequest(): SummarizeToursRequest {
            val command = this
            return summarizeToursRequest {
                this.annotations.putAll(command.annotations)
                this.context = command.context
                this.reason = command.reason
            }
        }
    }

    private suspend fun handleExpireYardMove(request: ExpireYardMoveRequest): ExpireYardMoveResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveLifecycleService/ExpireYardMove") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExpireYardMove", request)
            log.atInfo().addKeyValue("operation", "ExpireYardMove").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = ExpireYardMove0Command(
                context = request.context,
                reason = request.reason,
                annotations = request.annotationsMap
            ).toRequest()
            val reply0 = toursTourQueryService.summarizeTours(outbound0)
            val outbound1 = suggestDocumentsRequest {
                this.context = request.context
                this.filter = SuggestDocumentsRequestKt.filter {
                    this.createdAfter = request.details.effectiveAt
                    this.labels.putAll(reply0.result.detailsMap)
                }
            }
            val command1: Submission<SuggestDocumentsRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = documentsDocumentQueryService.suggestDocuments(payload1)
            val outbound2 = estimateYardRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.yard = yard {
                    this.owner = userRef {
                        this.displayName = request.details.newOwner.displayName
                        this.email = request.details.newOwner.email
                        this.userId = request.details.newOwner.userId
                    }
                }
            }
            val adapter2 = OutboundPort<EstimateYardRequest, EstimateYardResponse> { payload ->
                yardsYardCalculationService.estimateYard(payload)
            }
            val port2 = DelegatingPort(adapter2)
            val reply2 = port2.execute(outbound2)
            val outbound3 = scoreDockRequest {
                this.context = request.context
                this.details = ScoreDockRequestKt.details {
                    this.destination = postalAddress {
                        this.recipient = reply2.yard.address.recipient
                    }
                }
                this.dock = dock {
                    this.dimensions = dockDimensions {
                        this.length = reply2.yard.dimensions.length
                    }
                    this.owner = userRef {
                        this.userId = reply2.yard.owner.userId
                    }
                }
            }
            val reply3 = invokeExpireYardMove3(outbound3)
            expireYardMoveResponse {
                this.outcome = ExpireYardMoveResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.referenceIds.addAll((reply0.result.referenceIdsList) + (reply1.result.referenceIdsList))
                    this.status = "ok"
                }
                this.yardMove = yardMove {
                    this.audit = auditInfo {
                        this.etag = reply2.yard.audit.etag
                    }
                    this.requester = contactInfo {
                        this.email = reply3.dock.owner.email
                    }
                    this.yardMoveId = request.yardMoveId
                }
            }
        }

    private suspend fun handleRenewYardMove(request: RenewYardMoveRequest): RenewYardMoveResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveLifecycleService/RenewYardMove") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RenewYardMove", request)
            log.atInfo().addKeyValue("operation", "RenewYardMove").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = estimateDeliveryExceptionRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.requester = contactInfo {
                        this.preferredChannel = request.details.notifyContact.preferredChannel
                    }
                }
            }
            val invoke0: suspend (EstimateDeliveryExceptionRequest, Metadata) -> EstimateDeliveryExceptionResponse = deliveriesDeliveryExceptionCalculationService::estimateDeliveryException
            val reply0 = invoke0(outbound0, Metadata())
            val outbound1 = SuggestDocksRequest.newBuilder().apply {
                setContext(request.context)
                setFilter(SuggestDocksRequestKt.filter {
                    this.createdAfter = reply0.deliveryException.sla.dueAt
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> yardsDockQueryService.suggestDocks(value) }
            renewYardMoveResponse {
                this.outcome = RenewYardMoveResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.message = reply1.result.message
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.yardMove = yardMove {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.deliveryException.audit.updatedAt
                    }
                    this.yardMoveId = request.yardMoveId
                }
            }
        }

    private suspend fun handleScheduleYardMoveExpiry(request: ScheduleYardMoveExpiryRequest): ScheduleYardMoveExpiryResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveLifecycleService/ScheduleYardMoveExpiry") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScheduleYardMoveExpiry", request)
            log.atInfo().addKeyValue("operation", "ScheduleYardMoveExpiry").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yardMove.yardMoveId).log("Recorded audit detail")
            var reply0 = ListYardMoveReportsResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.yardMove.commentsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound0 = listYardMoveReportsRequest {
                                this.context = request.context
                                this.filter = ListYardMoveReportsRequestKt.filter {
                                    this.createdAfter = item.createdAt
                                    this.labels.putAll(request.details.parametersMap)
                                    this.ownerId = item.commentId
                                }
                            }
                            yardsYardMoveReportingService.listYardMoveReports(outbound0)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply0 = it }
                }
            }
            val outbound1 = countDocksRequest {
                this.context = request.context
                this.filter = CountDocksRequestKt.filter {
                    this.createdAfter = request.yardMove.audit.createdAt
                }
            }
            val reply1 = ClientRegistry.lookup("acme.yards.v1.DockQueryService", DockQueryServiceGrpcKt.DockQueryServiceCoroutineStub::class.java).countDocks(outbound1)
            scheduleYardMoveExpiryResponse {
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.status = "ok"
                }
                this.yardMove = yardMove {
                    this.priority = request.yardMove.priority
                    this.yardMoveId = request.yardMoveId
                }
            }
        }

    private suspend fun handleTerminateYardMove(request: TerminateYardMoveRequest): TerminateYardMoveResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveLifecycleService/TerminateYardMove") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "TerminateYardMove", request)
            log.atInfo().addKeyValue("operation", "TerminateYardMove").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = summarizeBroadcastsRequest {
                this.context = request.context
                this.reason = request.reason
            }
            val reply0 = workerForTerminateYardMove0.submit(outbound0)
            val outbound1 = watchDispatchOrdersRequest {
                this.context = request.context
                this.subscriber = contactInfo {
                    this.email = request.details.newOwner.email
                    this.name = request.details.notifyContact.name
                }
            }
            dispatchDispatchOrderService.watchDispatchOrders(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            terminateYardMoveResponse {
                this.outcome = TerminateYardMoveResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
                this.yardMove = yardMove {
                    this.yardMoveId = request.yardMoveId
                }
            }
        }
}
