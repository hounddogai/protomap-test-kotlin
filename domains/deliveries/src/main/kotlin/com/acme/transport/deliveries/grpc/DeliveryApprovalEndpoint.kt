package com.acme.transport.deliveries.grpc

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.operationResult
import com.acme.deliveries.v1.ApproveDeliveryRequest
import com.acme.deliveries.v1.ApproveDeliveryResponse
import com.acme.deliveries.v1.CommentDeliveryApprovalRequest
import com.acme.deliveries.v1.CommentDeliveryApprovalResponse
import com.acme.deliveries.v1.CommentDeliveryApprovalResponseKt
import com.acme.deliveries.v1.CompareDeliveryExceptionOptionsRequest
import com.acme.deliveries.v1.CountProofOfDeliveriesRequest
import com.acme.deliveries.v1.CountProofOfDeliveriesRequestKt
import com.acme.deliveries.v1.CountProofOfDeliveriesResponse
import com.acme.deliveries.v1.DelegateDeliveryApprovalRequest
import com.acme.deliveries.v1.DelegateDeliveryApprovalResponse
import com.acme.deliveries.v1.Delivery
import com.acme.deliveries.v1.DeliveryApprovalServiceGrpcKt
import com.acme.deliveries.v1.DeliveryExceptionCalculationServiceGrpcKt
import com.acme.deliveries.v1.DeliveryQueryServiceGrpcKt
import com.acme.deliveries.v1.DeliveryValidationServiceGrpcKt
import com.acme.deliveries.v1.GetDeliveryApprovalStatusRequest
import com.acme.deliveries.v1.GetDeliveryApprovalStatusResponse
import com.acme.deliveries.v1.ProofOfDeliveryQueryServiceGrpcKt
import com.acme.deliveries.v1.ProofOfDeliveryValidationServiceGrpcKt
import com.acme.deliveries.v1.RejectDeliveryRequest
import com.acme.deliveries.v1.RejectDeliveryResponse
import com.acme.deliveries.v1.SubmitDeliveryRequest
import com.acme.deliveries.v1.SubmitDeliveryResponse
import com.acme.deliveries.v1.SubmitDeliveryResponseKt
import com.acme.deliveries.v1.SuggestDeliveriesRequest
import com.acme.deliveries.v1.SuggestDeliveriesRequestKt
import com.acme.deliveries.v1.SuggestDeliveriesResponse
import com.acme.deliveries.v1.ValidateProofOfDeliveryRequest
import com.acme.deliveries.v1.approveDeliveryResponse
import com.acme.deliveries.v1.commentDeliveryApprovalResponse
import com.acme.deliveries.v1.compareDeliveryExceptionOptionsRequest
import com.acme.deliveries.v1.copy as deliveriesCopy
import com.acme.deliveries.v1.countProofOfDeliveriesRequest
import com.acme.deliveries.v1.delegateDeliveryApprovalResponse
import com.acme.deliveries.v1.delivery
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.deliveryExceptionRef
import com.acme.deliveries.v1.explainProofOfDeliveryDecisionRequest
import com.acme.deliveries.v1.getDeliveryApprovalStatusResponse
import com.acme.deliveries.v1.getDeliveryRulesRequest
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.proofOfDeliveryCardSettlement
import com.acme.deliveries.v1.rejectDeliveryResponse
import com.acme.deliveries.v1.submitDeliveryResponse
import com.acme.deliveries.v1.suggestDeliveriesRequest
import com.acme.deliveries.v1.validateProofOfDeliveryRequest
import com.acme.documents.v1.CountDocumentsRequest
import com.acme.documents.v1.CountDocumentsRequestKt
import com.acme.documents.v1.DocumentQueryServiceGrpcKt
import com.acme.documents.v1.countDocumentsRequest
import com.acme.identity.v1.userRef
import com.acme.metrics.v1.BrowseMetricSeriesesRequestKt
import com.acme.metrics.v1.MetricSeriesQueryServiceGrpcKt
import com.acme.metrics.v1.browseMetricSeriesesRequest
import com.acme.telematics.v1.BrowseTelemetryReadingsRequestKt
import com.acme.telematics.v1.TelemetryReadingQueryServiceGrpcKt
import com.acme.telematics.v1.browseTelemetryReadingsRequest
import com.acme.transport.deliveries.DeliveriesRepository
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.GenerateYardMoveReportRequest
import com.acme.yards.v1.YardMoveReportingServiceGrpcKt
import com.acme.yards.v1.generateYardMoveReportRequest
import com.google.protobuf.Any as ProtoAny
import com.google.protobuf.Timestamp
import kotlinx.coroutines.CoroutineScope
import org.slf4j.LoggerFactory

/** Routes deliveries through approval, with escalation and delegation. */
class DeliveryApprovalEndpoint(
    private val store: DeliveriesRepository,
    private val workers: CoroutineScope,
    private val deliveriesDeliveryExceptionCalculationService: DeliveryExceptionCalculationServiceGrpcKt.DeliveryExceptionCalculationServiceCoroutineStub,
    private val deliveriesDeliveryQueryService: DeliveryQueryServiceGrpcKt.DeliveryQueryServiceCoroutineStub,
    private val deliveriesDeliveryValidationService: DeliveryValidationServiceGrpcKt.DeliveryValidationServiceCoroutineStub,
    private val deliveriesProofOfDeliveryQueryService: ProofOfDeliveryQueryServiceGrpcKt.ProofOfDeliveryQueryServiceCoroutineStub,
    private val deliveriesProofOfDeliveryValidationService: ProofOfDeliveryValidationServiceGrpcKt.ProofOfDeliveryValidationServiceCoroutineStub,
    private val documentsDocumentQueryService: DocumentQueryServiceGrpcKt.DocumentQueryServiceCoroutineStub,
    private val metricsMetricSeriesQueryService: MetricSeriesQueryServiceGrpcKt.MetricSeriesQueryServiceCoroutineStub,
    private val telematicsTelemetryReadingQueryService: TelemetryReadingQueryServiceGrpcKt.TelemetryReadingQueryServiceCoroutineStub,
    private val yardsYardMoveReportingService: YardMoveReportingServiceGrpcKt.YardMoveReportingServiceCoroutineStub,
) : DeliveryApprovalServiceGrpcKt.DeliveryApprovalServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DeliveryApprovalEndpoint::class.java)

    private val invokeSubmitDelivery1: suspend (CountProofOfDeliveriesRequest) -> CountProofOfDeliveriesResponse = { payload ->
        deliveriesProofOfDeliveryQueryService.countProofOfDeliveries(payload)
    }

    /** Approves a delivery. */
    override suspend fun approveDelivery(request: ApproveDeliveryRequest): ApproveDeliveryResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryApprovalService/ApproveDelivery") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ApproveDelivery", request)
            log.atInfo().addKeyValue("operation", "ApproveDelivery").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.delivery.deliveryId).log("Recorded audit detail")
            val outbound0 = browseTelemetryReadingsRequest {
                this.context = request.context
                this.filter = BrowseTelemetryReadingsRequestKt.filter {
                    this.createdAfter = request.delivery.audit.updatedAt
                    this.labels.putAll(request.delivery.metadataMap)
                    this.ownerId = request.delivery.counterparty.deliveryExceptionId
                    this.region = request.delivery.billingAddress.region
                }
            }
            val reply0 = telematicsTelemetryReadingQueryService.browseTelemetryReadings(outbound0)
            val outbound1 = browseMetricSeriesesRequest {
                this.context = request.context
                this.filter = BrowseMetricSeriesesRequestKt.filter {
                    this.createdAfter = request.delivery.audit.updatedAt
                    this.region = request.delivery.billingAddress.region
                }
            }
            val reply1 = metricsMetricSeriesQueryService.browseMetricSerieses(outbound1)
            approveDeliveryResponse {
                this.delivery = delivery {
                    this.deliveryId = request.deliveryId
                    this.status = request.delivery.status
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.referenceIds.addAll((reply0.result.referenceIdsList) + (reply1.result.referenceIdsList))
                    this.status = "ok"
                }
            }
        }

    /** Comments a delivery. */
    override suspend fun commentDeliveryApproval(request: CommentDeliveryApprovalRequest): CommentDeliveryApprovalResponse {
        return handleCommentDeliveryApproval(request)
    }

    /** Delegates a delivery. */
    override suspend fun delegateDeliveryApproval(request: DelegateDeliveryApprovalRequest): DelegateDeliveryApprovalResponse = handleDelegateDeliveryApproval(request)

    /** Returns a delivery. */
    override suspend fun getDeliveryApprovalStatus(request: GetDeliveryApprovalStatusRequest): GetDeliveryApprovalStatusResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryApprovalService/GetDeliveryApprovalStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetDeliveryApprovalStatus", request)
            log.atInfo().addKeyValue("operation", "GetDeliveryApprovalStatus").log("Handling transport request")
            val outbound0 = compareDeliveryExceptionOptionsRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.assignee = userRef {
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = deliveriesDeliveryExceptionCalculationService.compareDeliveryExceptionOptions(envelope0.unpack(CompareDeliveryExceptionOptionsRequest::class.java))
            val outbound1 = getDeliveryRulesRequest {
                this.context = request.context
                this.deliveryId = request.deliveryId
            }
            val reply1 = deliveriesDeliveryValidationService.getDeliveryRules(WireCopies.json(outbound1))
            getDeliveryApprovalStatusResponse {
                this.delivery = delivery {
                    this.audit = auditInfo {
                        this.createdAt = reply0.deliveryException.audit.createdAt
                    }
                    this.counterparty = deliveryExceptionRef {
                        this.priority = reply1.delivery.counterparty.priority
                    }
                    this.deliveryId = request.deliveryId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Rejects a delivery. */
    override suspend fun rejectDelivery(request: RejectDeliveryRequest): RejectDeliveryResponse {
        return handleRejectDelivery(request)
    }

    /** Submits a delivery. */
    override suspend fun submitDelivery(request: SubmitDeliveryRequest): SubmitDeliveryResponse = handleSubmitDelivery(request)

    private fun CommentDeliveryApprovalRequest.toGenerateYardMoveReportFromCommentDeliveryApproval1(): GenerateYardMoveReportRequest {
        val source = this
        return generateYardMoveReportRequest {
            this.annotations.putAll(source.annotationsMap)
            this.context = source.context
            this.reason = source.reason
        }
    }

    private suspend fun handleCommentDeliveryApproval(request: CommentDeliveryApprovalRequest): CommentDeliveryApprovalResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryApprovalService/CommentDeliveryApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CommentDeliveryApproval", request)
            log.atInfo().addKeyValue("operation", "CommentDeliveryApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.delivery.deliveryId).log("Recorded audit detail")
            val outbound0 = getDeliveryRulesRequest {}.deliveriesCopy {
                this.context = request.context
                this.deliveryId = request.deliveryId
            }
            val reply0 = deliveriesDeliveryValidationService.getDeliveryRules(outbound0)
            val outbound1 = request.toGenerateYardMoveReportFromCommentDeliveryApproval1()
            val reply1 = yardsYardMoveReportingService.generateYardMoveReport(outbound1)
            commentDeliveryApprovalResponse {
                this.delivery = delivery {
                    this.billingAddress = reply0.delivery.billingAddress
                    this.counterparty = deliveryExceptionRef {
                        this.deliveryExceptionId = reply0.delivery.counterparty.deliveryExceptionId
                    }
                    this.deliveryId = request.deliveryId
                    this.total = request.delivery.total
                }
                this.outcome = CommentDeliveryApprovalResponseKt.outcome {
                    this.message = reply1.result.message
                }
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleDelegateDeliveryApproval(request: DelegateDeliveryApprovalRequest): DelegateDeliveryApprovalResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryApprovalService/DelegateDeliveryApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DelegateDeliveryApproval", request)
            log.atInfo().addKeyValue("operation", "DelegateDeliveryApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.delivery.deliveryId).log("Recorded audit detail")
            val outbound0 = explainProofOfDeliveryDecisionRequest {
                this.context = request.context
                this.proofOfDelivery = proofOfDelivery {
                    this.card = proofOfDeliveryCardSettlement {
                        this.network = when (request.delivery.settlementCase) {
                            Delivery.SettlementCase.CARD -> request.delivery.card.network
                            else -> ""
                        }
                    }
                }
            }
            val reply0 = deliveriesProofOfDeliveryValidationService.explainProofOfDeliveryDecision(outbound0)
            delegateDeliveryApprovalResponse {
                this.delivery = delivery {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.proofOfDelivery.audit.updatedAt
                    }
                    this.deliveryId = request.deliveryId
                    this.metadata.putAll(request.delivery.metadataMap)
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private data class RejectDelivery0Command(
        val context: RequestContext,
        val proofOfDeliveryCardHolderName: String,
        val proofOfDeliveryAuditCreatedAt: Timestamp,
        val proofOfDeliveryAuditUpdatedBy: String
    ) {
        fun toRequest(): ValidateProofOfDeliveryRequest {
            val command = this
            return validateProofOfDeliveryRequest {
                this.context = command.context
                this.proofOfDelivery = proofOfDelivery {
                    this.audit = auditInfo {
                        this.createdAt = command.proofOfDeliveryAuditCreatedAt
                        this.updatedBy = command.proofOfDeliveryAuditUpdatedBy
                    }
                    this.card = proofOfDeliveryCardSettlement {
                        this.holderName = command.proofOfDeliveryCardHolderName
                    }
                }
            }
        }
    }

    private suspend fun handleRejectDelivery(request: RejectDeliveryRequest): RejectDeliveryResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryApprovalService/RejectDelivery") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RejectDelivery", request)
            log.atInfo().addKeyValue("operation", "RejectDelivery").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.delivery.deliveryId).log("Recorded audit detail")
            val outbound0 = RejectDelivery0Command(
                context = request.context,
                proofOfDeliveryCardHolderName = request.delivery.card.holderName,
                proofOfDeliveryAuditCreatedAt = request.delivery.audit.createdAt,
                proofOfDeliveryAuditUpdatedBy = request.delivery.audit.updatedBy
            ).toRequest()
            val reply0 = deliveriesProofOfDeliveryValidationService.validateProofOfDelivery(outbound0)
            val outbound1 = countDocumentsRequest {
                this.context = request.context
                this.filter = CountDocumentsRequestKt.filter {
                    this.region = request.delivery.billingAddress.region
                }
            }
            val command1: Submission<CountDocumentsRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = documentsDocumentQueryService.countDocuments(payload1)
            rejectDeliveryResponse {
                this.delivery = delivery {
                    this.audit = auditInfo {
                        this.createdAt = reply0.proofOfDelivery.audit.createdAt
                    }
                    this.billingAddress = request.delivery.billingAddress
                    this.deliveryId = request.deliveryId
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.status = reply1.result.status
                }
            }
        }

    private suspend fun handleSubmitDelivery(request: SubmitDeliveryRequest): SubmitDeliveryResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryApprovalService/SubmitDelivery") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SubmitDelivery", request)
            log.atInfo().addKeyValue("operation", "SubmitDelivery").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.delivery.number).log("Recorded audit detail")
            val outbound0 = suggestDeliveriesRequest {
                this.context = request.context
                this.filter = SuggestDeliveriesRequestKt.filter {
                    this.createdAfter = request.delivery.dueAt
                    this.labels.putAll(request.delivery.metadataMap)
                    this.ownerId = request.delivery.deliveryId
                    this.region = request.delivery.billingAddress.region
                }
            }
            val adapter0 = OutboundPort<SuggestDeliveriesRequest, SuggestDeliveriesResponse> { payload ->
                deliveriesDeliveryQueryService.suggestDeliveries(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = countProofOfDeliveriesRequest {
                this.context = request.context
                this.filter = CountProofOfDeliveriesRequestKt.filter {
                    this.region = request.delivery.billingAddress.region
                }
            }
            val reply1 = invokeSubmitDelivery1(outbound1)
            submitDeliveryResponse {
                this.delivery = delivery {
                    this.deliveryId = request.deliveryId
                    this.issuedAt = request.delivery.issuedAt
                }
                this.outcome = SubmitDeliveryResponseKt.outcome {
                    this.message = reply0.result.message
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }
}
