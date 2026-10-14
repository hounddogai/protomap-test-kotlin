package com.acme.transport.deliveries.grpc

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.ApproveProofOfDeliveryRequest
import com.acme.deliveries.v1.ApproveProofOfDeliveryResponse
import com.acme.deliveries.v1.ApproveProofOfDeliveryResponseKt
import com.acme.deliveries.v1.CheckDeliveryExceptionEligibilityRequestKt
import com.acme.deliveries.v1.CommentDeliveryApprovalRequestKt
import com.acme.deliveries.v1.CommentProofOfDeliveryApprovalRequest
import com.acme.deliveries.v1.CommentProofOfDeliveryApprovalResponse
import com.acme.deliveries.v1.DelegateProofOfDeliveryApprovalRequest
import com.acme.deliveries.v1.DelegateProofOfDeliveryApprovalResponse
import com.acme.deliveries.v1.DelegateProofOfDeliveryApprovalResponseKt
import com.acme.deliveries.v1.DeliveryApprovalServiceGrpcKt
import com.acme.deliveries.v1.DeliveryExceptionValidationServiceGrpcKt
import com.acme.deliveries.v1.DeliveryValidationServiceGrpcKt
import com.acme.deliveries.v1.EscalateProofOfDeliveryRequest
import com.acme.deliveries.v1.EscalateProofOfDeliveryResponse
import com.acme.deliveries.v1.EscalateProofOfDeliveryResponseKt
import com.acme.deliveries.v1.GetProofOfDeliveryApprovalStatusRequest
import com.acme.deliveries.v1.GetProofOfDeliveryApprovalStatusResponse
import com.acme.deliveries.v1.ListPendingProofOfDeliveriesRequest
import com.acme.deliveries.v1.ListPendingProofOfDeliveriesResponse
import com.acme.deliveries.v1.ProofOfDelivery
import com.acme.deliveries.v1.ProofOfDeliveryApprovalServiceGrpcKt
import com.acme.deliveries.v1.ProofOfDeliveryAttachment
import com.acme.deliveries.v1.ProofOfDeliveryValidationServiceGrpcKt
import com.acme.deliveries.v1.SubmitProofOfDeliveryRequest
import com.acme.deliveries.v1.SubmitProofOfDeliveryResponse
import com.acme.deliveries.v1.ValidateProofOfDeliveryResponse
import com.acme.deliveries.v1.approveProofOfDeliveryResponse
import com.acme.deliveries.v1.checkDeliveryExceptionEligibilityRequest
import com.acme.deliveries.v1.commentDeliveryApprovalRequest
import com.acme.deliveries.v1.commentProofOfDeliveryApprovalResponse
import com.acme.deliveries.v1.delegateProofOfDeliveryApprovalResponse
import com.acme.deliveries.v1.delivery
import com.acme.deliveries.v1.deliveryBankSettlement
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.deliveryRef
import com.acme.deliveries.v1.escalateProofOfDeliveryResponse
import com.acme.deliveries.v1.getProofOfDeliveryApprovalStatusResponse
import com.acme.deliveries.v1.listPendingProofOfDeliveriesResponse
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.proofOfDeliveryAttachment
import com.acme.deliveries.v1.scoreDeliveryRequest
import com.acme.deliveries.v1.submitProofOfDeliveryResponse
import com.acme.deliveries.v1.validateProofOfDeliveryRequest
import com.acme.drivers.v1.DriverLicenseQueryServiceGrpcKt
import com.acme.drivers.v1.DriverValidationServiceGrpcKt
import com.acme.drivers.v1.SuggestDriverLicensesRequestKt
import com.acme.drivers.v1.SummarizeDriverLicensesRequest
import com.acme.drivers.v1.SummarizeDriverLicensesRequestKt
import com.acme.drivers.v1.ValidateDriverRequest
import com.acme.drivers.v1.ValidateDriverRequestKt
import com.acme.drivers.v1.ValidateDriverResponse
import com.acme.drivers.v1.copy as driversCopy
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.suggestDriverLicensesRequest
import com.acme.drivers.v1.summarizeDriverLicensesRequest
import com.acme.drivers.v1.validateDriverRequest
import com.acme.identity.v1.LookupUserRequest
import com.acme.identity.v1.UserQueryServiceGrpcKt
import com.acme.identity.v1.lookupUserRequest
import com.acme.identity.v1.userRef
import com.acme.metrics.v1.MetricSeriesServiceGrpcKt
import com.acme.metrics.v1.UpdateMetricSeriesResponse
import com.acme.metrics.v1.metricSeries
import com.acme.metrics.v1.metricSeriesComponent
import com.acme.metrics.v1.updateMetricSeriesRequest
import com.acme.ratelimiting.v1.RateLimitQueryServiceGrpcKt
import com.acme.ratelimiting.v1.SuggestRateLimitsRequestKt
import com.acme.ratelimiting.v1.suggestRateLimitsRequest
import com.acme.transport.deliveries.DeliveriesRepository
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.DelegateDockApprovalResponse
import com.acme.yards.v1.DockApprovalServiceGrpcKt
import com.acme.yards.v1.EstimateYardMoveRequest
import com.acme.yards.v1.YardMoveCalculationServiceGrpcKt
import com.acme.yards.v1.delegateDockApprovalRequest
import com.acme.yards.v1.dock
import com.acme.yards.v1.dockComponent
import com.acme.yards.v1.estimateYardMoveRequest
import com.acme.yards.v1.yardMove
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
import org.slf4j.LoggerFactory

/** Routes proof of deliveries through approval, with escalation and delegation. */
class ProofOfDeliveryApprovalEndpoint(
    private val store: DeliveriesRepository,
    private val workers: CoroutineScope,
    private val deliveriesDeliveryApprovalService: DeliveryApprovalServiceGrpcKt.DeliveryApprovalServiceCoroutineStub,
    private val deliveriesDeliveryExceptionValidationService: DeliveryExceptionValidationServiceGrpcKt.DeliveryExceptionValidationServiceCoroutineStub,
    private val deliveriesDeliveryValidationService: DeliveryValidationServiceGrpcKt.DeliveryValidationServiceCoroutineStub,
    private val deliveriesProofOfDeliveryValidationService: ProofOfDeliveryValidationServiceGrpcKt.ProofOfDeliveryValidationServiceCoroutineStub,
    private val driversDriverLicenseQueryService: DriverLicenseQueryServiceGrpcKt.DriverLicenseQueryServiceCoroutineStub,
    private val driversDriverValidationService: DriverValidationServiceGrpcKt.DriverValidationServiceCoroutineStub,
    private val identityUserQueryService: UserQueryServiceGrpcKt.UserQueryServiceCoroutineStub,
    private val metricsMetricSeriesService: MetricSeriesServiceGrpcKt.MetricSeriesServiceCoroutineStub,
    private val ratelimitingRateLimitQueryService: RateLimitQueryServiceGrpcKt.RateLimitQueryServiceCoroutineStub,
    private val yardsDockApprovalService: DockApprovalServiceGrpcKt.DockApprovalServiceCoroutineStub,
    private val yardsYardMoveCalculationService: YardMoveCalculationServiceGrpcKt.YardMoveCalculationServiceCoroutineStub,
) : ProofOfDeliveryApprovalServiceGrpcKt.ProofOfDeliveryApprovalServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(ProofOfDeliveryApprovalEndpoint::class.java)

    /** Approves a proof of delivery. */
    override suspend fun approveProofOfDelivery(request: ApproveProofOfDeliveryRequest): ApproveProofOfDeliveryResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryApprovalService/ApproveProofOfDelivery") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ApproveProofOfDelivery", request)
            log.atInfo().addKeyValue("operation", "ApproveProofOfDelivery").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.proofOfDelivery.proofOfDeliveryId).log("Recorded audit detail")
            var reply0 = DelegateDockApprovalResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.proofOfDelivery.linesList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound0 = delegateDockApprovalRequest {
                                this.context = request.context
                                this.dock = dock {
                                    this.components.putAll(mapOf("primary" to dockComponent {
                                        this.description = item.description
                                        this.quantity = item.quantity
                                    }))
                                    this.owner = userRef {
                                        this.email = request.details.approver.email
                                    }
                                }
                            }
                            yardsDockApprovalService.delegateDockApproval(outbound0)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply0 = it }
                }
            }
            val configure1: CheckDeliveryExceptionEligibilityRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.audit = auditInfo {
                        this.createdAt = reply0.dock.audit.createdAt
                        this.updatedAt = reply0.dock.audit.updatedAt
                    }
                    this.customer = deliveryRef {
                        this.displayName = request.proofOfDelivery.counterparty.displayName
                    }
                }
            }
            val outbound1 = checkDeliveryExceptionEligibilityRequest(configure1)
            val reply1 = deliveriesDeliveryExceptionValidationService.checkDeliveryExceptionEligibility(outbound1)
            approveProofOfDeliveryResponse {
                this.outcome = ApproveProofOfDeliveryResponseKt.outcome {
                    this.effectiveAt = reply1.outcome.effectiveAt
                }
                this.proofOfDelivery = proofOfDelivery {
                    this.number = request.proofOfDelivery.number
                    this.proofOfDeliveryId = request.proofOfDeliveryId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Comments a proof of delivery. */
    override suspend fun commentProofOfDeliveryApproval(request: CommentProofOfDeliveryApprovalRequest): CommentProofOfDeliveryApprovalResponse {
        return handleCommentProofOfDeliveryApproval(request)
    }

    /** Delegates a proof of delivery. */
    override suspend fun delegateProofOfDeliveryApproval(request: DelegateProofOfDeliveryApprovalRequest): DelegateProofOfDeliveryApprovalResponse = handleDelegateProofOfDeliveryApproval(request)

    /** Escalates a proof of delivery. */
    override suspend fun escalateProofOfDelivery(request: EscalateProofOfDeliveryRequest): EscalateProofOfDeliveryResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryApprovalService/EscalateProofOfDelivery") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EscalateProofOfDelivery", request)
            log.atInfo().addKeyValue("operation", "EscalateProofOfDelivery").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.proofOfDelivery.proofOfDeliveryId).log("Recorded audit detail")
            val outbound0 = request.toSummarizeDriverLicensesFromEscalateProofOfDelivery0()
            val reply0 = driversDriverLicenseQueryService.summarizeDriverLicenses(outbound0)
            val outbound1 = commentDeliveryApprovalRequest {
                this.context = request.context
                this.delivery = delivery {
                    this.audit = auditInfo {
                        this.updatedAt = request.proofOfDelivery.audit.updatedAt
                        this.updatedBy = request.proofOfDelivery.audit.updatedBy
                    }
                    this.bank = deliveryBankSettlement {
                        this.accountHolder = when (request.proofOfDelivery.settlementCase) {
                            ProofOfDelivery.SettlementCase.BANK -> request.proofOfDelivery.bank.accountHolder
                            else -> ""
                        }
                    }
                }
                this.details = CommentDeliveryApprovalRequestKt.details {
                    this.approver = userRef {
                        this.userId = request.details.approver.userId
                    }
                }
            }
            val reply1 = deliveriesDeliveryApprovalService.commentDeliveryApproval(outbound1)
            var reply2 = ValidateProofOfDeliveryResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<ProofOfDeliveryAttachment>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound2 = validateProofOfDeliveryRequest {
                            this.context = request.context
                            this.proofOfDelivery = proofOfDelivery {
                                this.attachments.addAll(listOf(proofOfDeliveryAttachment {
                                    this.sizeBytes = item.sizeBytes
                                    this.uri = item.uri
                                }))
                            }
                        }
                        reply2 = deliveriesProofOfDeliveryValidationService.validateProofOfDelivery(outbound2)
                    }
                }
                try {
                    request.proofOfDelivery.attachmentsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            escalateProofOfDeliveryResponse {
                this.outcome = EscalateProofOfDeliveryResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.proofOfDelivery = proofOfDelivery {
                    this.billingAddress = postalAddress {
                        this.recipient = reply1.delivery.billingAddress.recipient
                    }
                    this.dueAt = reply1.delivery.dueAt
                    this.proofOfDeliveryId = request.proofOfDeliveryId
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
            }
        }

    /** Returns a proof of delivery. */
    override suspend fun getProofOfDeliveryApprovalStatus(request: GetProofOfDeliveryApprovalStatusRequest): GetProofOfDeliveryApprovalStatusResponse {
        return handleGetProofOfDeliveryApprovalStatus(request)
    }

    /** Lists proof of deliveries a page at a time. */
    override suspend fun listPendingProofOfDeliveries(request: ListPendingProofOfDeliveriesRequest): ListPendingProofOfDeliveriesResponse = handleListPendingProofOfDeliveries(request)

    /** Submits a proof of delivery. */
    override suspend fun submitProofOfDelivery(request: SubmitProofOfDeliveryRequest): SubmitProofOfDeliveryResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryApprovalService/SubmitProofOfDelivery") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SubmitProofOfDelivery", request)
            log.atInfo().addKeyValue("operation", "SubmitProofOfDelivery").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.proofOfDelivery.proofOfDeliveryId).log("Recorded audit detail")
            val outbound0 = estimateYardMoveRequest {
                this.context = request.context
                this.yardMove = yardMove {
                    this.assignee = userRef {
                        this.email = request.details.approver.email
                    }
                    this.audit = auditInfo {
                        this.updatedAt = request.proofOfDelivery.audit.updatedAt
                    }
                    this.requester = contactInfo {
                        this.email = request.proofOfDelivery.counterparty.email
                    }
                }
            }
            val command0: Submission<EstimateYardMoveRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = yardsYardMoveCalculationService.estimateYardMove(payload0)
            val outbound1 = validateDriverRequest {
                this.context = request.context
                this.details = ValidateDriverRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.details.approver.email
                        this.name = reply0.yardMove.requester.name
                    }
                }
            }
            val adapter1 = OutboundPort<ValidateDriverRequest, ValidateDriverResponse> { payload ->
                driversDriverValidationService.validateDriver(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            submitProofOfDeliveryResponse {
                this.proofOfDelivery = proofOfDelivery {
                    this.audit = auditInfo {
                        this.updatedAt = reply1.driver.audit.updatedAt
                    }
                    this.counterparty = driverRef {
                        this.email = reply1.driver.relationshipManager.email
                    }
                    this.notes = request.proofOfDelivery.notes
                    this.proofOfDeliveryId = request.proofOfDeliveryId
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleCommentProofOfDeliveryApproval(request: CommentProofOfDeliveryApprovalRequest): CommentProofOfDeliveryApprovalResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryApprovalService/CommentProofOfDeliveryApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CommentProofOfDeliveryApproval", request)
            log.atInfo().addKeyValue("operation", "CommentProofOfDeliveryApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.proofOfDelivery.proofOfDeliveryId).log("Recorded audit detail")
            val outbound0 = scoreDeliveryRequest {
                this.context = request.context
                this.delivery = delivery {
                    this.bank = deliveryBankSettlement {
                        this.iban = request.proofOfDelivery.bank.iban
                    }
                    this.billingAddress = postalAddress {
                        this.recipient = request.proofOfDelivery.billingAddress.recipient
                    }
                }
            }
            val reply0 = deliveriesDeliveryValidationService.scoreDelivery(outbound0)
            var reply1 = UpdateMetricSeriesResponse.getDefaultInstance()
            request.proofOfDelivery.linesList.asFlow().map { item ->
                val outbound1 = updateMetricSeriesRequest {
                    this.context = request.context
                    this.metricSeries = metricSeries {
                        this.components.putAll(mapOf("primary" to metricSeriesComponent {
                            this.description = item.description
                        }))
                    }
                }
                metricsMetricSeriesService.updateMetricSeries(outbound1)
            }.collect { reply1 = it }
            commentProofOfDeliveryApprovalResponse {
                this.proofOfDelivery = proofOfDelivery {
                    this.dueAt = reply0.delivery.dueAt
                    this.metadata.putAll(request.proofOfDelivery.metadataMap)
                    this.proofOfDeliveryId = request.proofOfDeliveryId
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleDelegateProofOfDeliveryApproval(request: DelegateProofOfDeliveryApprovalRequest): DelegateProofOfDeliveryApprovalResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryApprovalService/DelegateProofOfDeliveryApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DelegateProofOfDeliveryApproval", request)
            log.atInfo().addKeyValue("operation", "DelegateProofOfDeliveryApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.proofOfDelivery.proofOfDeliveryId).log("Recorded audit detail")
            val outbound0 = suggestDriverLicensesRequest {}.driversCopy {
                this.context = request.context
                this.filter = SuggestDriverLicensesRequestKt.filter {
                    this.createdAfter = request.proofOfDelivery.issuedAt
                    this.labels.putAll(request.proofOfDelivery.metadataMap)
                    this.ownerId = request.details.approver.userId
                    this.region = request.proofOfDelivery.billingAddress.region
                }
            }
            val reply0 = driversDriverLicenseQueryService.suggestDriverLicenses(outbound0)
            delegateProofOfDeliveryApprovalResponse {
                this.outcome = DelegateProofOfDeliveryApprovalResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.message = reply0.result.message
                }
                this.proofOfDelivery = proofOfDelivery {
                    this.proofOfDeliveryId = request.proofOfDeliveryId
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
            }
        }

    private fun EscalateProofOfDeliveryRequest.toSummarizeDriverLicensesFromEscalateProofOfDelivery0(): SummarizeDriverLicensesRequest {
        val source = this
        return summarizeDriverLicensesRequest {
            this.annotations.putAll(source.annotationsMap)
            this.context = source.context
            this.details = SummarizeDriverLicensesRequestKt.details {
                this.filters.putAll(source.proofOfDelivery.metadataMap)
            }
            this.reason = source.reason
        }
    }

    private suspend fun handleGetProofOfDeliveryApprovalStatus(request: GetProofOfDeliveryApprovalStatusRequest): GetProofOfDeliveryApprovalStatusResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryApprovalService/GetProofOfDeliveryApprovalStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetProofOfDeliveryApprovalStatus", request)
            log.atInfo().addKeyValue("operation", "GetProofOfDeliveryApprovalStatus").log("Handling transport request")
            val outbound0 = suggestRateLimitsRequest {
                this.context = request.context
                this.filter = SuggestRateLimitsRequestKt.filter {
                    this.createdAfter = request.context.deadline
                    this.labels.putAll(request.context.baggageMap)
                    this.ownerId = request.proofOfDeliveryId
                }
            }
            val reply0 = ratelimitingRateLimitQueryService.suggestRateLimits(WireCopies.json(outbound0))
            getProofOfDeliveryApprovalStatusResponse {
                this.proofOfDelivery = proofOfDelivery {
                    this.proofOfDeliveryId = request.proofOfDeliveryId
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private data class ListPendingProofOfDeliveries0Command(
        val context: RequestContext,
        val userId: String
    ) {
        fun toRequest(): LookupUserRequest {
            val command = this
            return lookupUserRequest {
                this.context = command.context
                this.userId = command.userId
            }
        }
    }

    private suspend fun handleListPendingProofOfDeliveries(request: ListPendingProofOfDeliveriesRequest): ListPendingProofOfDeliveriesResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryApprovalService/ListPendingProofOfDeliveries") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListPendingProofOfDeliveries", request)
            log.atInfo().addKeyValue("operation", "ListPendingProofOfDeliveries").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = ListPendingProofOfDeliveries0Command(
                context = request.context,
                userId = request.context.actor.userId
            ).toRequest()
            val reply0 = identityUserQueryService.lookupUser(outbound0)
            listPendingProofOfDeliveriesResponse {
                this.proofOfDeliveries.addAll(listOf(proofOfDelivery {
                    this.audit = auditInfo {
                        this.createdBy = reply0.user.audit.createdBy
                    }
                }))
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }
}
