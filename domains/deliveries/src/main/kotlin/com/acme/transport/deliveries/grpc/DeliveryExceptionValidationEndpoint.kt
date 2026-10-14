package com.acme.transport.deliveries.grpc

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.CheckDeliveryExceptionEligibilityRequest
import com.acme.deliveries.v1.CheckDeliveryExceptionEligibilityResponse
import com.acme.deliveries.v1.DeliveryExceptionCalculationServiceGrpcKt
import com.acme.deliveries.v1.DeliveryExceptionComment
import com.acme.deliveries.v1.DeliveryExceptionValidationServiceGrpcKt
import com.acme.deliveries.v1.DeliveryValidationServiceGrpcKt
import com.acme.deliveries.v1.ExplainDeliveryExceptionDecisionRequest
import com.acme.deliveries.v1.ExplainDeliveryExceptionDecisionResponse
import com.acme.deliveries.v1.ExplainDeliveryExceptionDecisionResponseKt
import com.acme.deliveries.v1.GetDeliveryExceptionRulesRequest
import com.acme.deliveries.v1.GetDeliveryExceptionRulesResponse
import com.acme.deliveries.v1.LintDeliveriesRequest
import com.acme.deliveries.v1.LintDeliveriesResponse
import com.acme.deliveries.v1.LintDeliveryExceptionsRequest
import com.acme.deliveries.v1.LintDeliveryExceptionsResponse
import com.acme.deliveries.v1.NormalizeDeliveryExceptionRequest
import com.acme.deliveries.v1.NormalizeDeliveryExceptionResponse
import com.acme.deliveries.v1.PreviewDeliveryExceptionRequest
import com.acme.deliveries.v1.PreviewDeliveryExceptionResponse
import com.acme.deliveries.v1.PreviewDeliveryRequest
import com.acme.deliveries.v1.ProofOfDeliveryCalculationServiceGrpcKt
import com.acme.deliveries.v1.ScoreDeliveryExceptionRequest
import com.acme.deliveries.v1.ScoreDeliveryExceptionResponse
import com.acme.deliveries.v1.ScoreDeliveryRequest
import com.acme.deliveries.v1.ValidateDeliveryExceptionRequest
import com.acme.deliveries.v1.ValidateDeliveryExceptionResponse
import com.acme.deliveries.v1.checkDeliveryExceptionEligibilityResponse
import com.acme.deliveries.v1.copy as deliveriesCopy
import com.acme.deliveries.v1.delivery
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.deliveryExceptionRef
import com.acme.deliveries.v1.deliveryExceptionSla
import com.acme.deliveries.v1.estimateDeliveryExceptionRequest
import com.acme.deliveries.v1.explainDeliveryExceptionDecisionResponse
import com.acme.deliveries.v1.getDeliveryExceptionRulesResponse
import com.acme.deliveries.v1.lintDeliveriesRequest
import com.acme.deliveries.v1.lintDeliveryExceptionsResponse
import com.acme.deliveries.v1.normalizeDeliveryExceptionResponse
import com.acme.deliveries.v1.previewDeliveryExceptionResponse
import com.acme.deliveries.v1.previewDeliveryRequest
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.quoteProofOfDeliveryRequest
import com.acme.deliveries.v1.scoreDeliveryExceptionResponse
import com.acme.deliveries.v1.scoreDeliveryRequest
import com.acme.deliveries.v1.validateDeliveryExceptionResponse
import com.acme.documents.v1.CreateDocumentRequestKt
import com.acme.documents.v1.DocumentServiceGrpcKt
import com.acme.documents.v1.batchGetDocumentsRequest
import com.acme.documents.v1.createDocumentRequest
import com.acme.documents.v1.document
import com.acme.documents.v1.watchDocumentsRequest
import com.acme.drivers.v1.driverRef
import com.acme.featureflags.v1.FeatureFlagValidationServiceGrpcKt
import com.acme.featureflags.v1.ScoreFeatureFlagResponse
import com.acme.featureflags.v1.featureFlag
import com.acme.featureflags.v1.scoreFeatureFlagRequest
import com.acme.identity.v1.BrowseUsersRequest
import com.acme.identity.v1.BrowseUsersRequestKt
import com.acme.identity.v1.SearchUsersRequest
import com.acme.identity.v1.SearchUsersRequestKt
import com.acme.identity.v1.SearchUsersResponse
import com.acme.identity.v1.UserQueryServiceGrpcKt
import com.acme.identity.v1.UserServiceGrpcKt
import com.acme.identity.v1.browseUsersRequest
import com.acme.identity.v1.searchUsersRequest
import com.acme.identity.v1.userRef
import com.acme.seating.v1.SeatMapServiceGrpcKt
import com.acme.seating.v1.cloneSeatMapRequest
import com.acme.seating.v1.seatMap
import com.acme.transport.deliveries.DeliveriesRepository
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.DockQueryServiceGrpcKt
import com.acme.yards.v1.DockValidationServiceGrpcKt
import com.acme.yards.v1.FindDocksRequestKt
import com.acme.yards.v1.FindDocksResponse
import com.acme.yards.v1.NormalizeYardMoveRequestKt
import com.acme.yards.v1.ScoreDockRequest
import com.acme.yards.v1.YardCalculationServiceGrpcKt
import com.acme.yards.v1.YardMoveValidationServiceGrpcKt
import com.acme.yards.v1.dock
import com.acme.yards.v1.estimateYardRequest
import com.acme.yards.v1.findDocksRequest
import com.acme.yards.v1.normalizeYardMoveRequest
import com.acme.yards.v1.scoreDockRequest
import com.acme.yards.v1.yard
import com.acme.yards.v1.yardMove
import com.google.protobuf.Any as ProtoAny
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

/** Checks delivery exceptions against the rules before anything acts on them. */
class DeliveryExceptionValidationEndpoint(
    private val store: DeliveriesRepository,
    private val workers: CoroutineScope,
    private val deliveriesDeliveryExceptionCalculationService: DeliveryExceptionCalculationServiceGrpcKt.DeliveryExceptionCalculationServiceCoroutineStub,
    private val deliveriesDeliveryValidationService: DeliveryValidationServiceGrpcKt.DeliveryValidationServiceCoroutineStub,
    private val deliveriesProofOfDeliveryCalculationService: ProofOfDeliveryCalculationServiceGrpcKt.ProofOfDeliveryCalculationServiceCoroutineStub,
    private val documentsDocumentService: DocumentServiceGrpcKt.DocumentServiceCoroutineStub,
    private val featureflagsFeatureFlagValidationService: FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub,
    private val identityUserQueryService: UserQueryServiceGrpcKt.UserQueryServiceCoroutineStub,
    private val identityUserService: UserServiceGrpcKt.UserServiceCoroutineStub,
    private val seatingSeatMapService: SeatMapServiceGrpcKt.SeatMapServiceCoroutineStub,
    private val yardsDockQueryService: DockQueryServiceGrpcKt.DockQueryServiceCoroutineStub,
    private val yardsDockValidationService: DockValidationServiceGrpcKt.DockValidationServiceCoroutineStub,
    private val yardsYardCalculationService: YardCalculationServiceGrpcKt.YardCalculationServiceCoroutineStub,
    private val yardsYardMoveValidationService: YardMoveValidationServiceGrpcKt.YardMoveValidationServiceCoroutineStub,
) : DeliveryExceptionValidationServiceGrpcKt.DeliveryExceptionValidationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DeliveryExceptionValidationEndpoint::class.java)

    private val workerForCheckDeliveryExceptionEligibility1 = ReplyMailbox<LintDeliveriesRequest, LintDeliveriesResponse>(workers) { payload ->
        withTimeout(5_000) { deliveriesDeliveryValidationService.lintDeliveries(payload) }
    }

    private val clientForCheckDeliveryExceptionEligibility2 by lazy { yardsDockValidationService }

    /** Checks a delivery exception. */
    override suspend fun checkDeliveryExceptionEligibility(request: CheckDeliveryExceptionEligibilityRequest): CheckDeliveryExceptionEligibilityResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryExceptionValidationService/CheckDeliveryExceptionEligibility") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CheckDeliveryExceptionEligibility", request)
            log.atInfo().addKeyValue("operation", "CheckDeliveryExceptionEligibility").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.deliveryException.deliveryExceptionId).log("Recorded audit detail")
            val outbound0 = quoteProofOfDeliveryRequest {
                this.context = request.context
                this.proofOfDelivery = proofOfDelivery {
                    this.audit = auditInfo {
                        this.updatedAt = request.deliveryException.audit.updatedAt
                    }
                    this.counterparty = driverRef {
                        this.displayName = request.deliveryException.assignee.displayName
                        this.email = request.deliveryException.requester.email
                    }
                }
            }
            val reply0 = deliveriesProofOfDeliveryCalculationService.quoteProofOfDelivery(WireCopies.reflective(outbound0))
            val outbound1 = lintDeliveriesRequest {
                this.context = request.context
                this.delivery = delivery {
                    this.billingAddress = postalAddress {
                        this.countryCode = reply0.proofOfDelivery.billingAddress.countryCode
                    }
                }
            }
            val reply1 = workerForCheckDeliveryExceptionEligibility1.submit(outbound1)
            val outbound2 = scoreDockRequest {
                this.context = request.context
                this.dock = dock {
                    this.audit = auditInfo {
                        this.updatedAt = reply1.delivery.audit.updatedAt
                    }
                }
            }
            val reply2 = clientForCheckDeliveryExceptionEligibility2.scoreDock(outbound2)
            checkDeliveryExceptionEligibilityResponse {
                this.deliveryException = deliveryException {
                    this.assignee = userRef {
                        this.displayName = reply2.dock.owner.displayName
                        this.email = reply2.dock.owner.email
                    }
                    this.deliveryExceptionId = request.deliveryExceptionId
                    this.sla = deliveryExceptionSla {
                        this.dueAt = reply1.delivery.dueAt
                    }
                    this.status = request.deliveryException.status
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    /** Explains a delivery exception. */
    override suspend fun explainDeliveryExceptionDecision(request: ExplainDeliveryExceptionDecisionRequest): ExplainDeliveryExceptionDecisionResponse {
        return handleExplainDeliveryExceptionDecision(request)
    }

    /** Returns a delivery exception. */
    override suspend fun getDeliveryExceptionRules(request: GetDeliveryExceptionRulesRequest): GetDeliveryExceptionRulesResponse = handleGetDeliveryExceptionRules(request)

    /** Lints a delivery exception. */
    override suspend fun lintDeliveryExceptions(request: LintDeliveryExceptionsRequest): LintDeliveryExceptionsResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryExceptionValidationService/LintDeliveryExceptions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LintDeliveryExceptions", request)
            log.atInfo().addKeyValue("operation", "LintDeliveryExceptions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.deliveryException.deliveryExceptionId).log("Recorded audit detail")
            val outbound0 = batchGetDocumentsRequest {
                this.context = request.context
            }
            val reply0 = documentsDocumentService.batchGetDocuments(outbound0)
            val outbound1 = estimateDeliveryExceptionRequest {}.deliveriesCopy {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.requester = contactInfo {
                        this.name = request.deliveryException.requester.name
                    }
                    this.sla = deliveryExceptionSla {
                        this.dueAt = request.deliveryException.sla.dueAt
                    }
                }
            }
            val reply1 = deliveriesDeliveryExceptionCalculationService.estimateDeliveryException(outbound1)
            lintDeliveryExceptionsResponse {
                this.deliveryException = deliveryException {
                    this.deliveryExceptionId = request.deliveryExceptionId
                    this.priority = request.deliveryException.priority
                    this.sla = deliveryExceptionSla {
                        this.pausedFor = reply1.deliveryException.sla.pausedFor
                    }
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    /** Normalizes a delivery exception. */
    override suspend fun normalizeDeliveryException(request: NormalizeDeliveryExceptionRequest): NormalizeDeliveryExceptionResponse {
        return handleNormalizeDeliveryException(request)
    }

    /** Previews a delivery exception. */
    override suspend fun previewDeliveryException(request: PreviewDeliveryExceptionRequest): PreviewDeliveryExceptionResponse = handlePreviewDeliveryException(request)

    /** Scores a delivery exception. */
    override suspend fun scoreDeliveryException(request: ScoreDeliveryExceptionRequest): ScoreDeliveryExceptionResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryExceptionValidationService/ScoreDeliveryException") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScoreDeliveryException", request)
            log.atInfo().addKeyValue("operation", "ScoreDeliveryException").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.deliveryException.deliveryExceptionId).log("Recorded audit detail")
            val outbound0 = browseUsersRequest {
                this.context = request.context
                this.filter = BrowseUsersRequestKt.filter {
                    this.createdAfter = request.deliveryException.sla.dueAt
                    this.ownerId = request.deliveryException.customer.deliveryId
                    this.region = request.details.destination.region
                }
            }
            val command0: Submission<BrowseUsersRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = identityUserQueryService.browseUsers(payload0)
            scoreDeliveryExceptionResponse {
                this.deliveryException = deliveryException {
                    this.assignee = request.deliveryException.assignee
                    this.deliveryExceptionId = request.deliveryExceptionId
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = reply0.result.status
                }
            }
        }

    /** Validates a delivery exception. */
    override suspend fun validateDeliveryException(request: ValidateDeliveryExceptionRequest): ValidateDeliveryExceptionResponse {
        return handleValidateDeliveryException(request)
    }

    private suspend fun handleExplainDeliveryExceptionDecision(request: ExplainDeliveryExceptionDecisionRequest): ExplainDeliveryExceptionDecisionResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryExceptionValidationService/ExplainDeliveryExceptionDecision") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainDeliveryExceptionDecision", request)
            log.atInfo().addKeyValue("operation", "ExplainDeliveryExceptionDecision").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.deliveryException.deliveryExceptionId).log("Recorded audit detail")
            val outbound0 = normalizeYardMoveRequest {
                this.context = request.context
                this.details = NormalizeYardMoveRequestKt.details {
                    this.contact = contactInfo {
                        this.preferredChannel = request.details.contact.preferredChannel
                    }
                }
                this.yardMove = yardMove {
                    this.assignee = userRef {
                        this.email = request.deliveryException.requester.email
                    }
                    this.audit = auditInfo {
                        this.createdBy = request.deliveryException.audit.createdBy
                    }
                    this.requester = contactInfo {
                        this.email = request.details.contact.email
                    }
                }
            }
            val reply0 = flowOf(outbound0).map { value -> yardsYardMoveValidationService.normalizeYardMove(value) }.single()
            val outbound1 = estimateYardRequest {
                this.context = request.context
                this.yard = yard {
                    this.audit = auditInfo {
                        this.createdBy = request.deliveryException.audit.createdBy
                        this.updatedBy = request.deliveryException.audit.updatedBy
                    }
                }
            }
            val reply1 = withTimeout(5_000) { yardsYardCalculationService.estimateYard(outbound1) }
            val configure2: CreateDocumentRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.document = document {
                    this.audit = auditInfo {
                        this.updatedAt = reply1.yard.audit.updatedAt
                    }
                }
            }
            val outbound2 = createDocumentRequest(configure2)
            val reply2 = documentsDocumentService.createDocument(outbound2)
            val outbound3 = cloneSeatMapRequest {
                this.context = request.context
                this.seatMap = seatMap {
                    this.children.addAll(listOf(seatMap {
                        this.children.addAll(listOf(seatMap {
                            this.audit = auditInfo {
                                this.updatedBy = reply2.document.audit.updatedBy
                            }
                            this.manager = userRef {
                                this.displayName = reply2.document.counterparty.displayName
                                this.email = request.deliveryException.assignee.email
                                this.userId = request.deliveryException.assignee.userId
                            }
                        }))
                    }))
                }
            }
            val reply3 = seatingSeatMapService.cloneSeatMap(outbound3)
            explainDeliveryExceptionDecisionResponse {
                this.deliveryException = deliveryException {
                    this.assignee = userRef {
                        this.displayName = reply1.yard.owner.displayName
                        this.email = reply0.yardMove.assignee.email
                    }
                    this.audit = auditInfo {
                        this.etag = reply2.document.audit.etag
                    }
                    this.deliveryExceptionId = request.deliveryExceptionId
                    this.requester = request.deliveryException.requester
                }
                this.outcome = ExplainDeliveryExceptionDecisionResponseKt.outcome {
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.referenceIds.addAll(reply3.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleGetDeliveryExceptionRules(request: GetDeliveryExceptionRulesRequest): GetDeliveryExceptionRulesResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryExceptionValidationService/GetDeliveryExceptionRules") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetDeliveryExceptionRules", request)
            log.atInfo().addKeyValue("operation", "GetDeliveryExceptionRules").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            getDeliveryExceptionRulesResponse {
                this.deliveryException = deliveryException {
                    this.deliveryExceptionId = request.deliveryExceptionId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private fun NormalizeDeliveryExceptionRequest.toScoreDeliveryFromNormalizeDeliveryException0(): ScoreDeliveryRequest {
        val source = this
        return scoreDeliveryRequest {
            this.context = source.context
            this.delivery = delivery {
                this.audit = auditInfo {
                    this.createdAt = source.deliveryException.audit.createdAt
                }
                this.counterparty = deliveryExceptionRef {
                    this.displayName = source.deliveryException.customer.displayName
                }
            }
        }
    }

    private suspend fun handleNormalizeDeliveryException(request: NormalizeDeliveryExceptionRequest): NormalizeDeliveryExceptionResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryExceptionValidationService/NormalizeDeliveryException") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "NormalizeDeliveryException", request)
            log.atInfo().addKeyValue("operation", "NormalizeDeliveryException").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.deliveryException.deliveryExceptionId).log("Recorded audit detail")
            val outbound0 = request.toScoreDeliveryFromNormalizeDeliveryException0()
            val reply0 = deliveriesDeliveryValidationService.scoreDelivery(outbound0)
            coroutineScope {
                val channel = Channel<DeliveryExceptionComment>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound1 = watchDocumentsRequest {
                            this.context = request.context
                            this.subscriber = contactInfo {
                                this.email = item.authorEmail
                                this.preferredChannel = request.deliveryException.requester.preferredChannel
                            }
                        }
                        documentsDocumentService.watchDocuments(outbound1).collect { event ->
                            log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
                        }
                    }
                }
                try {
                    request.deliveryException.commentsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            normalizeDeliveryExceptionResponse {
                this.deliveryException = deliveryException {
                    this.audit = auditInfo {
                        this.createdBy = reply0.delivery.audit.createdBy
                        this.etag = reply0.delivery.audit.etag
                    }
                    this.deliveryExceptionId = request.deliveryExceptionId
                    this.subject = request.deliveryException.subject
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private data class PreviewDeliveryException2Command(
        val context: RequestContext,
        val deliveryAuditUpdatedAt: Timestamp,
        val deliveryBillingAddressCountryCode: String,
        val deliveryAuditCreatedBy: String,
        val deliveryCounterpartyDisplayName: String
    ) {
        fun toRequest(): PreviewDeliveryRequest {
            val command = this
            return previewDeliveryRequest {
                this.context = command.context
                this.delivery = delivery {
                    this.audit = auditInfo {
                        this.createdBy = command.deliveryAuditCreatedBy
                        this.updatedAt = command.deliveryAuditUpdatedAt
                    }
                    this.billingAddress = postalAddress {
                        this.countryCode = command.deliveryBillingAddressCountryCode
                    }
                    this.counterparty = deliveryExceptionRef {
                        this.displayName = command.deliveryCounterpartyDisplayName
                    }
                }
            }
        }
    }

    private suspend fun handlePreviewDeliveryException(request: PreviewDeliveryExceptionRequest): PreviewDeliveryExceptionResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryExceptionValidationService/PreviewDeliveryException") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PreviewDeliveryException", request)
            log.atInfo().addKeyValue("operation", "PreviewDeliveryException").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.deliveryException.deliveryExceptionId).log("Recorded audit detail")
            val outbound0 = scoreDockRequest {
                this.context = request.context
                this.dock = dock {
                    this.audit = auditInfo {
                        this.createdAt = request.deliveryException.audit.createdAt
                        this.updatedBy = request.deliveryException.audit.updatedBy
                    }
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = yardsDockValidationService.scoreDock(envelope0.unpack(ScoreDockRequest::class.java))
            var reply1 = FindDocksResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.deliveryException.historyList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound1 = findDocksRequest {
                                this.context = request.context
                                this.filter = FindDocksRequestKt.filter {
                                    this.createdAfter = item.changedAt
                                    this.ownerId = request.deliveryException.assignee.userId
                                    this.region = reply0.dock.address.region
                                }
                            }
                            yardsDockQueryService.findDocks(outbound1)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply1 = it }
                }
            }
            val outbound2 = PreviewDeliveryException2Command(
                context = request.context,
                deliveryAuditUpdatedAt = request.deliveryException.audit.updatedAt,
                deliveryBillingAddressCountryCode = reply0.dock.address.countryCode,
                deliveryAuditCreatedBy = reply0.dock.audit.createdBy,
                deliveryCounterpartyDisplayName = request.deliveryException.customer.displayName
            ).toRequest()
            val reply2 = deliveriesDeliveryValidationService.previewDelivery(outbound2)
            previewDeliveryExceptionResponse {
                this.deliveryException = deliveryException {
                    this.assignee = userRef {
                        this.userId = reply0.dock.owner.userId
                    }
                    this.audit = auditInfo {
                        this.etag = reply0.dock.audit.etag
                    }
                    this.deliveryExceptionId = request.deliveryExceptionId
                    this.priority = request.deliveryException.priority
                    this.sla = deliveryExceptionSla {
                        this.dueAt = reply2.delivery.dueAt
                    }
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleValidateDeliveryException(request: ValidateDeliveryExceptionRequest): ValidateDeliveryExceptionResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryExceptionValidationService/ValidateDeliveryException") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ValidateDeliveryException", request)
            log.atInfo().addKeyValue("operation", "ValidateDeliveryException").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.deliveryException.deliveryExceptionId).log("Recorded audit detail")
            val outbound0 = searchUsersRequest {
                this.context = request.context
                this.filter = SearchUsersRequestKt.filter {
                    this.createdAfter = request.deliveryException.sla.dueAt
                    this.labels.putAll(request.deliveryException.customFieldsMap)
                    this.ownerId = request.deliveryException.customer.deliveryId
                }
            }
            val adapter0 = OutboundPort<SearchUsersRequest, SearchUsersResponse> { payload ->
                identityUserService.searchUsers(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            var reply1 = ScoreFeatureFlagResponse.getDefaultInstance()
            request.deliveryException.commentsList.asFlow().map { item ->
                val outbound1 = scoreFeatureFlagRequest {
                    this.context = request.context
                    this.featureFlag = featureFlag {
                        this.audit = auditInfo {
                            this.createdAt = item.createdAt
                        }
                    }
                }
                featureflagsFeatureFlagValidationService.scoreFeatureFlag(outbound1)
            }.collect { reply1 = it }
            validateDeliveryExceptionResponse {
                this.deliveryException = deliveryException {
                    this.deliveryExceptionId = request.deliveryExceptionId
                    this.requester = request.deliveryException.requester
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }
}
