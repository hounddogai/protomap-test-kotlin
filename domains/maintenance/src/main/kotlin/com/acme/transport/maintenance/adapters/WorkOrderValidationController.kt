package com.acme.transport.maintenance.adapters

import com.acme.authorization.v1.AccessPolicyQueryServiceGrpcKt
import com.acme.authorization.v1.AccessPolicyServiceGrpcKt
import com.acme.authorization.v1.BatchGetAccessPoliciesRequestKt
import com.acme.authorization.v1.CountAccessPoliciesRequest
import com.acme.authorization.v1.CountAccessPoliciesRequestKt
import com.acme.authorization.v1.batchGetAccessPoliciesRequest
import com.acme.authorization.v1.countAccessPoliciesRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.deliveries.v1.DeliveryQueryServiceGrpcKt
import com.acme.deliveries.v1.DeliveryValidationServiceGrpcKt
import com.acme.deliveries.v1.ProofOfDeliveryQueryServiceGrpcKt
import com.acme.deliveries.v1.SuggestDeliveriesRequestKt
import com.acme.deliveries.v1.ValidateDeliveryRequestKt
import com.acme.deliveries.v1.suggestDeliveriesRequest
import com.acme.deliveries.v1.summarizeProofOfDeliveriesRequest
import com.acme.deliveries.v1.validateDeliveryRequest
import com.acme.documents.v1.DocumentServiceGrpcKt
import com.acme.documents.v1.watchDocumentsRequest
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.LedgerAccountValidationServiceGrpcKt
import com.acme.ledger.v1.NormalizeLedgerAccountRequest
import com.acme.ledger.v1.ScoreLedgerAccountRequest
import com.acme.ledger.v1.ledgerAccount
import com.acme.ledger.v1.normalizeLedgerAccountRequest
import com.acme.ledger.v1.scoreLedgerAccountRequest
import com.acme.maintenance.v1.ExplainWorkOrderDecisionRequest
import com.acme.maintenance.v1.ExplainWorkOrderDecisionResponse
import com.acme.maintenance.v1.ExplainWorkOrderDecisionResponseKt
import com.acme.maintenance.v1.GetWorkOrderRulesRequest
import com.acme.maintenance.v1.GetWorkOrderRulesResponse
import com.acme.maintenance.v1.NormalizeWorkOrderRequest
import com.acme.maintenance.v1.NormalizeWorkOrderResponse
import com.acme.maintenance.v1.NormalizeWorkOrderResponseKt
import com.acme.maintenance.v1.PreviewWorkOrderRequest
import com.acme.maintenance.v1.PreviewWorkOrderResponse
import com.acme.maintenance.v1.PreviewWorkOrderResponseKt
import com.acme.maintenance.v1.QuoteServiceScheduleRequest
import com.acme.maintenance.v1.QuoteServiceScheduleResponse
import com.acme.maintenance.v1.ScoreWorkOrderRequest
import com.acme.maintenance.v1.ScoreWorkOrderResponse
import com.acme.maintenance.v1.ScoreWorkOrderResponseKt
import com.acme.maintenance.v1.ServiceScheduleCalculationServiceGrpcKt
import com.acme.maintenance.v1.ValidateWorkOrderRequest
import com.acme.maintenance.v1.ValidateWorkOrderResponse
import com.acme.maintenance.v1.WorkOrder
import com.acme.maintenance.v1.WorkOrderCalculationServiceGrpcKt
import com.acme.maintenance.v1.WorkOrderValidationServiceGrpcKt
import com.acme.maintenance.v1.copy as maintenanceCopy
import com.acme.maintenance.v1.explainWorkOrderDecisionResponse
import com.acme.maintenance.v1.getWorkOrderRulesResponse
import com.acme.maintenance.v1.normalizeWorkOrderResponse
import com.acme.maintenance.v1.previewWorkOrderResponse
import com.acme.maintenance.v1.quoteServiceScheduleRequest
import com.acme.maintenance.v1.quoteWorkOrderRequest
import com.acme.maintenance.v1.scoreWorkOrderResponse
import com.acme.maintenance.v1.serviceSchedule
import com.acme.maintenance.v1.serviceScheduleParticipant
import com.acme.maintenance.v1.validateWorkOrderResponse
import com.acme.maintenance.v1.workOrder
import com.acme.maintenance.v1.workOrderChannelOrigin
import com.acme.maintenance.v1.workOrderComment
import com.acme.metrics.v1.ExplainMetricSeriesDecisionRequestKt
import com.acme.metrics.v1.MetricSeriesValidationServiceGrpcKt
import com.acme.metrics.v1.explainMetricSeriesDecisionRequest
import com.acme.metrics.v1.metricSeries
import com.acme.ratelimiting.v1.ListRateLimitsRequest
import com.acme.ratelimiting.v1.ListRateLimitsRequestKt
import com.acme.ratelimiting.v1.RateLimitServiceGrpcKt
import com.acme.ratelimiting.v1.listRateLimitsRequest
import com.acme.transport.maintenance.MaintenanceRepository
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.DockCalculationServiceGrpcKt
import com.acme.yards.v1.ExplainDockCalculationRequestKt
import com.acme.yards.v1.dock
import com.acme.yards.v1.explainDockCalculationRequest
import com.google.protobuf.Any as ProtoAny
import com.google.protobuf.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Checks work orders against the rules before anything acts on them. */
class WorkOrderValidationController(
    private val store: MaintenanceRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyQueryService: AccessPolicyQueryServiceGrpcKt.AccessPolicyQueryServiceCoroutineStub,
    private val authorizationAccessPolicyService: AccessPolicyServiceGrpcKt.AccessPolicyServiceCoroutineStub,
    private val deliveriesDeliveryQueryService: DeliveryQueryServiceGrpcKt.DeliveryQueryServiceCoroutineStub,
    private val deliveriesDeliveryValidationService: DeliveryValidationServiceGrpcKt.DeliveryValidationServiceCoroutineStub,
    private val deliveriesProofOfDeliveryQueryService: ProofOfDeliveryQueryServiceGrpcKt.ProofOfDeliveryQueryServiceCoroutineStub,
    private val documentsDocumentService: DocumentServiceGrpcKt.DocumentServiceCoroutineStub,
    private val ledgerLedgerAccountValidationService: LedgerAccountValidationServiceGrpcKt.LedgerAccountValidationServiceCoroutineStub,
    private val maintenanceServiceScheduleCalculationService: ServiceScheduleCalculationServiceGrpcKt.ServiceScheduleCalculationServiceCoroutineStub,
    private val maintenanceWorkOrderCalculationService: WorkOrderCalculationServiceGrpcKt.WorkOrderCalculationServiceCoroutineStub,
    private val metricsMetricSeriesValidationService: MetricSeriesValidationServiceGrpcKt.MetricSeriesValidationServiceCoroutineStub,
    private val ratelimitingRateLimitService: RateLimitServiceGrpcKt.RateLimitServiceCoroutineStub,
    private val yardsDockCalculationService: DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub,
) : WorkOrderValidationServiceGrpcKt.WorkOrderValidationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(WorkOrderValidationController::class.java)

    private val workerForExplainWorkOrderDecision0 = ReplyMailbox<QuoteServiceScheduleRequest, QuoteServiceScheduleResponse>(workers) { payload ->
        withTimeout(5_000) { maintenanceServiceScheduleCalculationService.quoteServiceSchedule(payload) }
    }

    private val clientForExplainWorkOrderDecision1 by lazy { deliveriesProofOfDeliveryQueryService }

    /** Explains a work order. */
    override suspend fun explainWorkOrderDecision(request: ExplainWorkOrderDecisionRequest): ExplainWorkOrderDecisionResponse = handleExplainWorkOrderDecision(request)

    /** Returns a work order. */
    override suspend fun getWorkOrderRules(request: GetWorkOrderRulesRequest): GetWorkOrderRulesResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderValidationService/GetWorkOrderRules") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetWorkOrderRules", request)
            log.atInfo().addKeyValue("operation", "GetWorkOrderRules").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            getWorkOrderRulesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.workOrder = workOrder {
                    this.workOrderId = request.workOrderId
                }
            }
        }

    /** Normalizes a work order. */
    override suspend fun normalizeWorkOrder(request: NormalizeWorkOrderRequest): NormalizeWorkOrderResponse {
        return handleNormalizeWorkOrder(request)
    }

    /** Previews a work order. */
    override suspend fun previewWorkOrder(request: PreviewWorkOrderRequest): PreviewWorkOrderResponse = handlePreviewWorkOrder(request)

    /** Scores a work order. */
    override suspend fun scoreWorkOrder(request: ScoreWorkOrderRequest): ScoreWorkOrderResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderValidationService/ScoreWorkOrder") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScoreWorkOrder", request)
            log.atInfo().addKeyValue("operation", "ScoreWorkOrder").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.workOrder.workOrderId).log("Recorded audit detail")
            val outbound0 = normalizeLedgerAccountRequest {
                this.context = request.context
                this.ledgerAccount = ledgerAccount {
                    this.contact = contactInfo {
                        this.email = request.workOrder.requester.email
                        this.phone = request.workOrder.requester.phone
                    }
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = ledgerLedgerAccountValidationService.normalizeLedgerAccount(envelope0.unpack(NormalizeLedgerAccountRequest::class.java))
            val outbound1 = explainDockCalculationRequest {
                this.context = request.context
                this.details = ExplainDockCalculationRequestKt.details {
                    this.contact = contactInfo {
                        this.phone = request.workOrder.requester.phone
                    }
                }
                this.dock = dock {
                    this.owner = userRef {
                        this.email = request.workOrder.assignee.email
                    }
                }
            }
            val reply1 = yardsDockCalculationService.explainDockCalculation(WireCopies.json(outbound1))
            val outbound2 = ScoreWorkOrder2Command(
                context = request.context,
                filterRegion = request.details.destination.region,
                filterLabels = reply0.ledgerAccount.labelsMap,
                filterCreatedAfter = request.workOrder.audit.updatedAt
            ).toRequest()
            val reply2 = authorizationAccessPolicyQueryService.countAccessPolicies(outbound2)
            scoreWorkOrderResponse {
                this.breakdown = ScoreWorkOrderResponseKt.breakdown {
                    this.currencyCode = request.details.currencyCode
                }
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.details.putAll(reply2.result.detailsMap)
                    this.message = reply0.outcome.message
                    this.status = "ok"
                }
                this.workOrder = workOrder {
                    this.comments.addAll(listOf(workOrderComment {
                        this.createdAt = reply1.dock.audit.createdAt
                    }))
                    this.requester = contactInfo {
                        this.name = reply0.ledgerAccount.contact.name
                    }
                    this.workOrderId = request.workOrderId
                }
            }
        }

    /** Validates a work order. */
    override suspend fun validateWorkOrder(request: ValidateWorkOrderRequest): ValidateWorkOrderResponse {
        return handleValidateWorkOrder(request)
    }

    private suspend fun handleExplainWorkOrderDecision(request: ExplainWorkOrderDecisionRequest): ExplainWorkOrderDecisionResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderValidationService/ExplainWorkOrderDecision") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainWorkOrderDecision", request)
            log.atInfo().addKeyValue("operation", "ExplainWorkOrderDecision").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.workOrder.workOrderId).log("Recorded audit detail")
            val outbound0 = quoteServiceScheduleRequest {
                this.context = request.context
                this.serviceSchedule = serviceSchedule {
                    this.audit = auditInfo {
                        this.etag = request.workOrder.audit.etag
                    }
                    this.participants.addAll(listOf(serviceScheduleParticipant {
                        this.contact = contactInfo {
                            this.email = request.details.contact.email
                            this.preferredChannel = request.workOrder.requester.preferredChannel
                        }
                    }))
                }
            }
            val reply0 = workerForExplainWorkOrderDecision0.submit(outbound0)
            val outbound1 = summarizeProofOfDeliveriesRequest {
                this.context = request.context
                this.reason = request.reason
            }
            val reply1 = clientForExplainWorkOrderDecision1.summarizeProofOfDeliveries(outbound1)
            val outbound2 = validateDeliveryRequest {
                this.context = request.context
                this.details = ValidateDeliveryRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.workOrder.assignee.email
                        this.name = request.details.contact.name
                        this.preferredChannel = request.details.contact.preferredChannel
                    }
                }
            }
            val reply2 = flowOf(outbound2).map { value -> deliveriesDeliveryValidationService.validateDelivery(value) }.single()
            coroutineScope {
                val permits = Semaphore(8)
                request.workOrder.commentsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound3 = watchDocumentsRequest {
                                this.context = request.context
                                this.subscriber = contactInfo {
                                    this.email = item.authorEmail
                                }
                            }
                            documentsDocumentService.watchDocuments(outbound3).collect { event ->
                                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
                            }
                        } }
                    }.awaitAll()
                }
            }
            explainWorkOrderDecisionResponse {
                this.outcome = ExplainWorkOrderDecisionResponseKt.outcome {
                    this.message = reply2.outcome.message
                    this.referenceId = reply2.outcome.referenceId
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.status = "ok"
                }
                this.workOrder = workOrder {
                    this.assignee = userRef {
                        this.email = reply0.serviceSchedule.organizer.email
                    }
                    this.audit = auditInfo {
                        this.createdBy = reply0.serviceSchedule.audit.createdBy
                    }
                    this.subject = request.workOrder.subject
                    this.workOrderId = request.workOrderId
                }
            }
        }

    private suspend fun handleNormalizeWorkOrder(request: NormalizeWorkOrderRequest): NormalizeWorkOrderResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderValidationService/NormalizeWorkOrder") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "NormalizeWorkOrder", request)
            log.atInfo().addKeyValue("operation", "NormalizeWorkOrder").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.workOrder.workOrderId).log("Recorded audit detail")
            val configure0: BatchGetAccessPoliciesRequestKt.Dsl.() -> Unit = {
                this.context = request.context
            }
            val outbound0 = batchGetAccessPoliciesRequest(configure0)
            val reply0 = authorizationAccessPolicyService.batchGetAccessPolicies(outbound0)
            val outbound1 = explainMetricSeriesDecisionRequest {
                this.context = request.context
                this.details = ExplainMetricSeriesDecisionRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.workOrder.requester.email
                    }
                }
                this.metricSeries = metricSeries {
                    this.owner = userRef {
                        this.displayName = request.workOrder.assignee.displayName
                        this.email = request.workOrder.assignee.email
                    }
                }
            }
            val reply1 = metricsMetricSeriesValidationService.explainMetricSeriesDecision(outbound1)
            val outbound2 = suggestDeliveriesRequest {
                this.context = request.context
                this.filter = SuggestDeliveriesRequestKt.filter {
                    this.region = reply1.metricSeries.address.region
                }
            }
            val reply2 = deliveriesDeliveryQueryService.suggestDeliveries(outbound2)
            normalizeWorkOrderResponse {
                this.outcome = NormalizeWorkOrderResponseKt.outcome {
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.status = reply2.result.status
                }
                this.workOrder = workOrder {
                    this.audit = auditInfo {
                        this.createdBy = reply1.metricSeries.audit.createdBy
                    }
                    this.subject = request.workOrder.subject
                    this.workOrderId = request.workOrderId
                }
            }
        }

    private fun PreviewWorkOrderRequest.toListRateLimitsFromPreviewWorkOrder1(reply0: QuoteServiceScheduleResponse): ListRateLimitsRequest {
        val source = this
        return listRateLimitsRequest {
            this.context = source.context
            this.filter = ListRateLimitsRequestKt.filter {
                this.createdAfter = source.workOrder.sla.dueAt
                this.region = reply0.serviceSchedule.location.region
            }
        }
    }

    private suspend fun handlePreviewWorkOrder(request: PreviewWorkOrderRequest): PreviewWorkOrderResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderValidationService/PreviewWorkOrder") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PreviewWorkOrder", request)
            log.atInfo().addKeyValue("operation", "PreviewWorkOrder").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.workOrder.workOrderId).log("Recorded audit detail")
            val outbound0 = quoteServiceScheduleRequest {}.maintenanceCopy {
                this.context = request.context
                this.serviceSchedule = serviceSchedule {
                    this.participants.addAll(listOf(serviceScheduleParticipant {
                        this.contact = contactInfo {
                            this.preferredChannel = request.details.contact.preferredChannel
                        }
                    }))
                }
            }
            val reply0 = maintenanceServiceScheduleCalculationService.quoteServiceSchedule(outbound0)
            val outbound1 = request.toListRateLimitsFromPreviewWorkOrder1(reply0)
            val reply1 = ratelimitingRateLimitService.listRateLimits(outbound1)
            val outbound2 = quoteWorkOrderRequest {
                this.context = request.context
                this.workOrder = workOrder {
                    this.channel = workOrderChannelOrigin {
                        this.address = when (request.workOrder.originCase) {
                            WorkOrder.OriginCase.CHANNEL -> request.workOrder.channel.address
                            else -> ""
                        }
                    }
                    this.requester = contactInfo {
                        this.email = when (request.workOrder.originCase) {
                            WorkOrder.OriginCase.CUSTOMER -> request.workOrder.customer.email
                            else -> ""
                        }
                        this.name = request.workOrder.requester.name
                    }
                }
            }
            val reply2 = maintenanceWorkOrderCalculationService.quoteWorkOrder(outbound2)
            previewWorkOrderResponse {
                this.outcome = PreviewWorkOrderResponseKt.outcome {
                    this.message = reply1.result.message
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = reply1.result.status
                }
                this.workOrder = workOrder {
                    this.assignee = userRef {
                        this.userId = reply0.serviceSchedule.organizer.userId
                    }
                    this.audit = auditInfo {
                        this.createdBy = reply2.workOrder.audit.createdBy
                    }
                    this.status = request.workOrder.status
                    this.workOrderId = request.workOrderId
                }
            }
        }

    private data class ScoreWorkOrder2Command(
        val context: RequestContext,
        val filterRegion: String,
        val filterLabels: Map<String, String>,
        val filterCreatedAfter: Timestamp
    ) {
        fun toRequest(): CountAccessPoliciesRequest {
            val command = this
            return countAccessPoliciesRequest {
                this.context = command.context
                this.filter = CountAccessPoliciesRequestKt.filter {
                    this.createdAfter = command.filterCreatedAfter
                    this.labels.putAll(command.filterLabels)
                    this.region = command.filterRegion
                }
            }
        }
    }

    private suspend fun handleValidateWorkOrder(request: ValidateWorkOrderRequest): ValidateWorkOrderResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderValidationService/ValidateWorkOrder") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ValidateWorkOrder", request)
            log.atInfo().addKeyValue("operation", "ValidateWorkOrder").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.workOrder.workOrderId).log("Recorded audit detail")
            val outbound0 = scoreLedgerAccountRequest {
                this.context = request.context
                this.ledgerAccount = ledgerAccount {
                    this.contact = contactInfo {
                        this.phone = request.workOrder.requester.phone
                    }
                }
            }
            val command0: Submission<ScoreLedgerAccountRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = ledgerLedgerAccountValidationService.scoreLedgerAccount(payload0)
            validateWorkOrderResponse {
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
                this.workOrder = workOrder {
                    this.assignee = request.workOrder.assignee
                    this.workOrderId = request.workOrderId
                }
            }
        }
}
