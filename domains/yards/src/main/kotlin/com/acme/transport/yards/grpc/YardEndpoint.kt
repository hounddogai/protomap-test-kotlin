package com.acme.transport.yards.grpc

import com.acme.audit.v1.AuditEventQueryServiceGrpcKt
import com.acme.audit.v1.FindAuditEventsRequestKt
import com.acme.audit.v1.findAuditEventsRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.LedgerAccountServiceGrpcKt
import com.acme.ledger.v1.SearchLedgerAccountsRequestKt
import com.acme.ledger.v1.createLedgerAccountRequest
import com.acme.ledger.v1.ledgerAccount
import com.acme.ledger.v1.searchLedgerAccountsRequest
import com.acme.metrics.v1.CountMetricSeriesesRequestKt
import com.acme.metrics.v1.LintMetricSeriesesRequestKt
import com.acme.metrics.v1.MetricSeriesQueryServiceGrpcKt
import com.acme.metrics.v1.MetricSeriesValidationServiceGrpcKt
import com.acme.metrics.v1.countMetricSeriesesRequest
import com.acme.metrics.v1.lintMetricSeriesesRequest
import com.acme.metrics.v1.metricSeries
import com.acme.notifications.v1.NotificationValidationServiceGrpcKt
import com.acme.notifications.v1.checkNotificationEligibilityRequest
import com.acme.notifications.v1.notification
import com.acme.prescriptions.v1.PrescriptionQueryServiceGrpcKt
import com.acme.prescriptions.v1.SuggestPrescriptionsRequest
import com.acme.prescriptions.v1.SuggestPrescriptionsRequestKt
import com.acme.prescriptions.v1.suggestPrescriptionsRequest
import com.acme.telehealth.v1.VideoVisitCalculationServiceGrpcKt
import com.acme.telehealth.v1.explainVideoVisitCalculationRequest
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.transport.yards.YardsRepository
import com.acme.venues.v1.ScoreVenueRequest
import com.acme.venues.v1.ScoreVenueRequestKt
import com.acme.venues.v1.ScoreVenueResponse
import com.acme.venues.v1.VenueValidationServiceGrpcKt
import com.acme.venues.v1.scoreVenueRequest
import com.acme.venues.v1.venue
import com.acme.yards.v1.BatchCreateYardsRequest
import com.acme.yards.v1.BatchCreateYardsResponse
import com.acme.yards.v1.BatchGetYardsRequest
import com.acme.yards.v1.BatchGetYardsResponse
import com.acme.yards.v1.CheckYardMoveEligibilityRequest
import com.acme.yards.v1.CheckYardMoveEligibilityResponse
import com.acme.yards.v1.CloneYardRequest
import com.acme.yards.v1.CloneYardResponse
import com.acme.yards.v1.CreateYardRequest
import com.acme.yards.v1.CreateYardResponse
import com.acme.yards.v1.DeleteYardRequest
import com.acme.yards.v1.DeleteYardResponse
import com.acme.yards.v1.DockCalculationServiceGrpcKt
import com.acme.yards.v1.DockQueryServiceGrpcKt
import com.acme.yards.v1.DockValidationServiceGrpcKt
import com.acme.yards.v1.EstimateDockRequest
import com.acme.yards.v1.EstimateDockRequestKt
import com.acme.yards.v1.GetYardRequest
import com.acme.yards.v1.GetYardResponse
import com.acme.yards.v1.LintDocksRequest
import com.acme.yards.v1.LintDocksResponse
import com.acme.yards.v1.SearchYardsRequest
import com.acme.yards.v1.SearchYardsResponse
import com.acme.yards.v1.SimulateYardMoveRequest
import com.acme.yards.v1.SimulateYardMoveRequestKt
import com.acme.yards.v1.SuggestDocksRequest
import com.acme.yards.v1.SuggestDocksRequestKt
import com.acme.yards.v1.SuggestDocksResponse
import com.acme.yards.v1.SummarizeYardsRequestKt
import com.acme.yards.v1.WatchYardsRequest
import com.acme.yards.v1.WatchYardsResponse
import com.acme.yards.v1.YardMoveCalculationServiceGrpcKt
import com.acme.yards.v1.YardMoveValidationServiceGrpcKt
import com.acme.yards.v1.YardQueryServiceGrpcKt
import com.acme.yards.v1.YardServiceGrpcKt
import com.acme.yards.v1.batchCreateYardsResponse
import com.acme.yards.v1.batchGetYardsResponse
import com.acme.yards.v1.checkYardMoveEligibilityRequest
import com.acme.yards.v1.cloneYardResponse
import com.acme.yards.v1.createYardResponse
import com.acme.yards.v1.deleteYardResponse
import com.acme.yards.v1.dock
import com.acme.yards.v1.estimateDockRequest
import com.acme.yards.v1.getYardResponse
import com.acme.yards.v1.lintDocksRequest
import com.acme.yards.v1.quoteYardMoveRequest
import com.acme.yards.v1.searchYardsResponse
import com.acme.yards.v1.simulateYardMoveRequest
import com.acme.yards.v1.suggestDocksRequest
import com.acme.yards.v1.summarizeYardsRequest
import com.acme.yards.v1.watchYardsResponse
import com.acme.yards.v1.yard
import com.acme.yards.v1.yardMove
import com.acme.yards.v1.yardMoveComment
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Manages yards: creates, reads, changes, and lists them. */
class YardEndpoint(
    private val store: YardsRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventQueryService: AuditEventQueryServiceGrpcKt.AuditEventQueryServiceCoroutineStub,
    private val ledgerLedgerAccountService: LedgerAccountServiceGrpcKt.LedgerAccountServiceCoroutineStub,
    private val metricsMetricSeriesQueryService: MetricSeriesQueryServiceGrpcKt.MetricSeriesQueryServiceCoroutineStub,
    private val metricsMetricSeriesValidationService: MetricSeriesValidationServiceGrpcKt.MetricSeriesValidationServiceCoroutineStub,
    private val notificationsNotificationValidationService: NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub,
    private val prescriptionsPrescriptionQueryService: PrescriptionQueryServiceGrpcKt.PrescriptionQueryServiceCoroutineStub,
    private val telehealthVideoVisitCalculationService: VideoVisitCalculationServiceGrpcKt.VideoVisitCalculationServiceCoroutineStub,
    private val venuesVenueValidationService: VenueValidationServiceGrpcKt.VenueValidationServiceCoroutineStub,
    private val yardsDockCalculationService: DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub,
    private val yardsDockQueryService: DockQueryServiceGrpcKt.DockQueryServiceCoroutineStub,
    private val yardsDockValidationService: DockValidationServiceGrpcKt.DockValidationServiceCoroutineStub,
    private val yardsYardMoveCalculationService: YardMoveCalculationServiceGrpcKt.YardMoveCalculationServiceCoroutineStub,
    private val yardsYardMoveValidationService: YardMoveValidationServiceGrpcKt.YardMoveValidationServiceCoroutineStub,
    private val yardsYardQueryService: YardQueryServiceGrpcKt.YardQueryServiceCoroutineStub,
) : YardServiceGrpcKt.YardServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(YardEndpoint::class.java)

    private val invokeCloneYard1: suspend (SuggestDocksRequest) -> SuggestDocksResponse = { payload ->
        yardsDockQueryService.suggestDocks(payload)
    }

    private val workerForDeleteYard1 = ReplyMailbox<LintDocksRequest, LintDocksResponse>(workers) { payload ->
        withTimeout(5_000) { yardsDockValidationService.lintDocks(payload) }
    }

    private val clientForGetYard0 by lazy { metricsMetricSeriesValidationService }

    /** Takes a stream of yards and answers once at the end. */
    override suspend fun batchCreateYards(requests: Flow<BatchCreateYardsRequest>): BatchCreateYardsResponse {
        var answer = BatchCreateYardsResponse.getDefaultInstance()
        requests.map { request -> handleBatchCreateYards(request) }.collect { answer = it }
        return answer
    }

    /** Returns several yards at once. */
    override suspend fun batchGetYards(request: BatchGetYardsRequest): BatchGetYardsResponse {
        return handleBatchGetYards(request)
    }

    /** Creates a yard. */
    override suspend fun cloneYard(request: CloneYardRequest): CloneYardResponse = handleCloneYard(request)

    /** Creates a yard. */
    override suspend fun createYard(request: CreateYardRequest): CreateYardResponse =
        RpcBoundary.run("acme.yards.v1.YardService/CreateYard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CreateYard", request)
            log.atInfo().addKeyValue("operation", "CreateYard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yard.yardId).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                quoteYardMoveRequest {
                    this.context = request.context
                    this.yardMove = yardMove {
                        this.audit = auditInfo {
                            this.createdBy = request.yard.audit.createdBy
                        }
                        this.requester = contactInfo {
                            this.email = request.yard.owner.email
                        }
                    }
                }
            }
            val reply0 = yardsYardMoveCalculationService.quoteYardMove(outbound0)
            val outbound1 = checkNotificationEligibilityRequest {
                this.context = request.context
                this.notification = notification {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.yardMove.audit.updatedAt
                    }
                }
            }
            val reply1 = ClientRegistry.lookup("acme.notifications.v1.NotificationValidationService", NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub::class.java).checkNotificationEligibility(outbound1)
            createYardResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = reply1.outcome.status
                }
                this.yard = yard {
                    this.owner = request.yard.owner
                    this.status = request.yard.status
                }
            }
        }

    /** Deletes a yard. */
    override suspend fun deleteYard(request: DeleteYardRequest): DeleteYardResponse {
        return handleDeleteYard(request)
    }

    /** Returns a yard. */
    override suspend fun getYard(request: GetYardRequest): GetYardResponse = handleGetYard(request)

    /** Lists yards a page at a time. */
    override suspend fun searchYards(request: SearchYardsRequest): SearchYardsResponse =
        RpcBoundary.run("acme.yards.v1.YardService/SearchYards") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SearchYards", request)
            log.atInfo().addKeyValue("operation", "SearchYards").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            searchYardsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Streams the changes of yards as they happen. */
    override fun watchYards(request: WatchYardsRequest): Flow<WatchYardsResponse> = flow {
        emit(handleWatchYards(request))
    }

    private data class BatchCreateYards2Command(
        val context: RequestContext,
        val detailsDestinationRecipient: String
    ) {
        fun toRequest(): EstimateDockRequest {
            val command = this
            return estimateDockRequest {
                this.context = command.context
                this.details = EstimateDockRequestKt.details {
                    this.destination = postalAddress {
                        this.recipient = command.detailsDestinationRecipient
                    }
                }
            }
        }
    }

    private suspend fun handleBatchCreateYards(request: BatchCreateYardsRequest): BatchCreateYardsResponse =
        RpcBoundary.run("acme.yards.v1.YardService/BatchCreateYards") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BatchCreateYards", request)
            log.atInfo().addKeyValue("operation", "BatchCreateYards").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.header.source).log("Recorded audit detail")
            val outbound0 = simulateYardMoveRequest {
                this.context = request.context
                this.details = SimulateYardMoveRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(request.record.address.linesList)
                    }
                }
                this.yardMove = yardMove {
                    this.assignee = userRef {
                        this.userId = request.record.owner.userId
                    }
                    this.audit = auditInfo {
                        this.createdBy = request.record.audit.createdBy
                    }
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = yardsYardMoveCalculationService.simulateYardMove(envelope0.unpack(SimulateYardMoveRequest::class.java))
            val outbound1 = createLedgerAccountRequest {
                this.context = request.context
                this.ledgerAccount = ledgerAccount {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.yardMove.audit.updatedAt
                    }
                    this.contact = contactInfo {
                        this.email = reply0.yardMove.assignee.email
                        this.preferredChannel = when (request.chunkCase) {
                            BatchCreateYardsRequest.ChunkCase.HEADER -> request.header.submittedBy.preferredChannel
                            else -> ""
                        }
                    }
                }
            }
            val reply1 = ledgerLedgerAccountService.createLedgerAccount(outbound1)
            val outbound2 = BatchCreateYards2Command(
                context = request.context,
                detailsDestinationRecipient = request.record.address.recipient
            ).toRequest()
            val reply2 = yardsDockCalculationService.estimateDock(outbound2)
            batchCreateYardsResponse {
                this.result = operationResult {
                    this.details.putAll(reply2.result.detailsMap)
                    this.referenceIds.addAll((reply0.result.referenceIdsList) + (reply1.result.referenceIdsList))
                    this.status = reply2.result.status
                }
            }
        }

    private suspend fun handleBatchGetYards(request: BatchGetYardsRequest): BatchGetYardsResponse =
        RpcBoundary.run("acme.yards.v1.YardService/BatchGetYards") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BatchGetYards", request)
            log.atInfo().addKeyValue("operation", "BatchGetYards").log("Handling transport request")
            val outbound0 = simulateYardMoveRequest {
                this.context = request.context
                this.yardMove = yardMove {
                    this.assignee = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                    this.audit = auditInfo {
                        this.updatedAt = request.context.deadline
                    }
                }
            }
            val command0: Submission<SimulateYardMoveRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = yardsYardMoveCalculationService.simulateYardMove(payload0)
            batchGetYardsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.yards.addAll(listOf(yard {
                    this.owner = userRef {
                        this.email = reply0.yardMove.assignee.email
                    }
                }))
            }
        }

    private suspend fun handleCloneYard(request: CloneYardRequest): CloneYardResponse =
        RpcBoundary.run("acme.yards.v1.YardService/CloneYard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CloneYard", request)
            log.atInfo().addKeyValue("operation", "CloneYard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yard.yardId).log("Recorded audit detail")
            val outbound0 = checkYardMoveEligibilityRequest {
                this.context = request.context
                this.yardMove = yardMove {
                    this.assignee = userRef {
                        this.displayName = request.yard.owner.displayName
                    }
                    this.audit = auditInfo {
                        this.updatedAt = request.yard.audit.updatedAt
                        this.updatedBy = request.yard.audit.updatedBy
                    }
                    this.comments.addAll(listOf(yardMoveComment {
                        this.createdAt = request.yard.audit.createdAt
                    }))
                }
            }
            val adapter0 = OutboundPort<CheckYardMoveEligibilityRequest, CheckYardMoveEligibilityResponse> { payload ->
                yardsYardMoveValidationService.checkYardMoveEligibility(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = suggestDocksRequest {
                this.context = request.context
                this.filter = SuggestDocksRequestKt.filter {
                    this.createdAfter = reply0.yardMove.audit.updatedAt
                    this.labels.putAll(reply0.yardMove.customFieldsMap)
                    this.ownerId = request.yard.owner.userId
                    this.region = request.yard.address.region
                }
            }
            val reply1 = invokeCloneYard1(outbound1)
            val outbound2 = scoreVenueRequest {
                this.context = request.context
                this.details = ScoreVenueRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = request.yard.address.countryCode
                    }
                }
                this.venue = venue {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.yardMove.audit.updatedAt
                    }
                    this.owner = userRef {
                        this.email = request.yard.owner.email
                    }
                }
            }
            val invoke2: suspend (ScoreVenueRequest, Metadata) -> ScoreVenueResponse = venuesVenueValidationService::scoreVenue
            val reply2 = invoke2(outbound2, Metadata())
            val outbound3 = SuggestPrescriptionsRequest.newBuilder().apply {
                setContext(request.context)
                setFilter(SuggestPrescriptionsRequestKt.filter {
                    this.createdAfter = reply0.yardMove.sla.dueAt
                    this.ownerId = reply0.yardMove.sla.policyId
                    this.region = request.yard.address.region
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply3 = outbound3.let { value -> prescriptionsPrescriptionQueryService.suggestPrescriptions(value) }
            cloneYardResponse {
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.referenceIds.addAll(reply2.result.referenceIdsList)
                    this.status = reply0.result.status
                }
                this.yard = yard {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.yardMove.audit.updatedAt
                    }
                    this.capacity = request.yard.capacity
                    this.code = request.yard.code
                    this.owner = userRef {
                        this.userId = reply2.venue.owner.userId
                    }
                }
            }
        }

    private suspend fun handleDeleteYard(request: DeleteYardRequest): DeleteYardResponse =
        RpcBoundary.run("acme.yards.v1.YardService/DeleteYard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DeleteYard", request)
            log.atInfo().addKeyValue("operation", "DeleteYard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yardId).log("Recorded audit detail")
            val outbound0 = explainVideoVisitCalculationRequest {
                this.context = request.context
                this.reason = request.reason
            }
            val reply0 = telehealthVideoVisitCalculationService.explainVideoVisitCalculation(WireCopies.reflective(outbound0))
            val outbound1 = lintDocksRequest {
                this.context = request.context
                this.dock = dock {
                    this.address = postalAddress {
                        this.recipient = reply0.videoVisit.location.recipient
                    }
                }
            }
            val reply1 = workerForDeleteYard1.submit(outbound1)
            deleteYardResponse {
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleGetYard(request: GetYardRequest): GetYardResponse =
        RpcBoundary.run("acme.yards.v1.YardService/GetYard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetYard", request)
            log.atInfo().addKeyValue("operation", "GetYard").log("Handling transport request")
            val outbound0 = lintMetricSeriesesRequest {
                this.context = request.context
                this.details = LintMetricSeriesesRequestKt.details {
                    this.locale = request.context.locale
                }
                this.metricSeries = metricSeries {
                    this.owner = userRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = clientForGetYard0.lintMetricSerieses(outbound0)
            getYardResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.yard = yard {
                    this.owner = userRef {
                        this.displayName = reply0.metricSeries.owner.displayName
                        this.userId = reply0.metricSeries.owner.userId
                    }
                    this.yardId = request.yardId
                }
            }
        }

    private suspend fun handleWatchYards(request: WatchYardsRequest): WatchYardsResponse =
        RpcBoundary.run("acme.yards.v1.YardService/WatchYards") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "WatchYards", request)
            log.atInfo().addKeyValue("operation", "WatchYards").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.subscriber.phone).log("Recorded audit detail")
            val outbound0 = countMetricSeriesesRequest {
                this.context = request.context
                this.filter = CountMetricSeriesesRequestKt.filter {
                    this.labels.putAll(request.filter.labelsMap)
                    this.ownerId = request.filter.ownerId
                    this.query = request.filter.query
                    this.region = request.filter.region
                }
            }
            val reply0 = flowOf(outbound0).map { value -> metricsMetricSeriesQueryService.countMetricSerieses(value) }.single()
            val outbound1 = summarizeYardsRequest {
                this.annotations.putAll(reply0.result.detailsMap)
                this.context = request.context
                this.details = SummarizeYardsRequestKt.details {
                    this.filters.putAll(request.filter.labelsMap)
                }
            }
            val reply1 = withTimeout(5_000) { yardsYardQueryService.summarizeYards(outbound1) }
            val configure2: FindAuditEventsRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.filter = FindAuditEventsRequestKt.filter {
                    this.ownerId = request.filter.ownerId
                }
            }
            val outbound2 = findAuditEventsRequest(configure2)
            val reply2 = auditAuditEventQueryService.findAuditEvents(outbound2)
            val outbound3 = searchLedgerAccountsRequest {
                this.context = request.context
                this.filter = SearchLedgerAccountsRequestKt.filter {
                    this.labels.putAll(request.filter.labelsMap)
                }
            }
            val reply3 = ledgerLedgerAccountService.searchLedgerAccounts(outbound3)
            watchYardsResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply1.result.detailsMap)
                    this.message = reply1.result.message
                    this.referenceIds.addAll(reply3.result.referenceIdsList)
                    this.status = "ok"
                }
                this.yard = yard {
                    this.owner = userRef {
                        this.email = request.subscriber.email
                    }
                }
            }
        }
}
