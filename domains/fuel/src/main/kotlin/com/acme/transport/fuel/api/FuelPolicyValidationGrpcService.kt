package com.acme.transport.fuel.api

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.customerRef
import com.acme.deliveries.v1.CalculateDeliveryExceptionRequest
import com.acme.deliveries.v1.DeliveryExceptionCalculationServiceGrpcKt
import com.acme.deliveries.v1.DeliveryExceptionValidationServiceGrpcKt
import com.acme.deliveries.v1.NormalizeDeliveryExceptionRequest
import com.acme.deliveries.v1.NormalizeDeliveryExceptionResponse
import com.acme.deliveries.v1.calculateDeliveryExceptionRequest
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.deliveryRef
import com.acme.deliveries.v1.normalizeDeliveryExceptionRequest
import com.acme.documents.v1.CreateDocumentRequest
import com.acme.documents.v1.CreateDocumentResponse
import com.acme.documents.v1.DocumentServiceGrpcKt
import com.acme.documents.v1.createDocumentRequest
import com.acme.documents.v1.document
import com.acme.documents.v1.documentFolderRef
import com.acme.documents.v1.documentLine
import com.acme.fuel.v1.ExplainFuelPolicyDecisionRequest
import com.acme.fuel.v1.ExplainFuelPolicyDecisionResponse
import com.acme.fuel.v1.FuelPolicyCalculationServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyHistoryServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyValidationServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionCalculationServiceGrpcKt
import com.acme.fuel.v1.GetFuelPolicyRulesRequest
import com.acme.fuel.v1.GetFuelPolicyRulesResponse
import com.acme.fuel.v1.LintFuelPoliciesRequest
import com.acme.fuel.v1.LintFuelPoliciesResponse
import com.acme.fuel.v1.ListFuelPolicyVersionsRequest
import com.acme.fuel.v1.ListFuelPolicyVersionsRequestKt
import com.acme.fuel.v1.NormalizeFuelPolicyRequest
import com.acme.fuel.v1.NormalizeFuelPolicyResponse
import com.acme.fuel.v1.NormalizeFuelPolicyResponseKt
import com.acme.fuel.v1.PreviewFuelPolicyRequest
import com.acme.fuel.v1.PreviewFuelPolicyResponse
import com.acme.fuel.v1.RevertFuelPolicyRequestKt
import com.acme.fuel.v1.ScoreFuelPolicyRequest
import com.acme.fuel.v1.ScoreFuelPolicyResponse
import com.acme.fuel.v1.ScoreFuelPolicyResponseKt
import com.acme.fuel.v1.ValidateFuelPolicyRequest
import com.acme.fuel.v1.ValidateFuelPolicyResponse
import com.acme.fuel.v1.explainFuelPolicyDecisionResponse
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.getFuelPolicyRatesRequest
import com.acme.fuel.v1.getFuelPolicyRulesResponse
import com.acme.fuel.v1.getFuelPolicyVersionRequest
import com.acme.fuel.v1.lintFuelPoliciesResponse
import com.acme.fuel.v1.listFuelPolicyVersionsRequest
import com.acme.fuel.v1.normalizeFuelPolicyResponse
import com.acme.fuel.v1.previewFuelPolicyResponse
import com.acme.fuel.v1.recalculateFuelTransactionsRequest
import com.acme.fuel.v1.revertFuelPolicyRequest
import com.acme.fuel.v1.scoreFuelPolicyResponse
import com.acme.fuel.v1.validateFuelPolicyResponse
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.ExportLedgerAccountsRequestKt
import com.acme.ledger.v1.LedgerAccountQueryServiceGrpcKt
import com.acme.ledger.v1.LedgerAccountServiceGrpcKt
import com.acme.ledger.v1.SearchLedgerAccountsRequestKt
import com.acme.ledger.v1.exportLedgerAccountsRequest
import com.acme.ledger.v1.searchLedgerAccountsRequest
import com.acme.metrics.v1.MetricSeriesServiceGrpcKt
import com.acme.metrics.v1.UpdateMetricSeriesRequest
import com.acme.metrics.v1.metricSeries
import com.acme.metrics.v1.updateMetricSeriesRequest
import com.acme.notifications.v1.ExportNotificationsRequestKt
import com.acme.notifications.v1.NotificationQueryServiceGrpcKt
import com.acme.notifications.v1.exportNotificationsRequest
import com.acme.shows.v1.ShowCalculationServiceGrpcKt
import com.acme.shows.v1.compareShowOptionsRequest
import com.acme.shows.v1.show
import com.acme.transport.fuel.FuelRepository
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.DockCalculationServiceGrpcKt
import com.acme.yards.v1.dock
import com.acme.yards.v1.quoteDockRequest
import com.google.protobuf.Any as ProtoAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Checks fuel policies against the rules before anything acts on them. */
class FuelPolicyValidationGrpcService(
    private val store: FuelRepository,
    private val workers: CoroutineScope,
    private val deliveriesDeliveryExceptionCalculationService: DeliveryExceptionCalculationServiceGrpcKt.DeliveryExceptionCalculationServiceCoroutineStub,
    private val deliveriesDeliveryExceptionValidationService: DeliveryExceptionValidationServiceGrpcKt.DeliveryExceptionValidationServiceCoroutineStub,
    private val documentsDocumentService: DocumentServiceGrpcKt.DocumentServiceCoroutineStub,
    private val fuelFuelPolicyCalculationService: FuelPolicyCalculationServiceGrpcKt.FuelPolicyCalculationServiceCoroutineStub,
    private val fuelFuelPolicyHistoryService: FuelPolicyHistoryServiceGrpcKt.FuelPolicyHistoryServiceCoroutineStub,
    private val fuelFuelTransactionCalculationService: FuelTransactionCalculationServiceGrpcKt.FuelTransactionCalculationServiceCoroutineStub,
    private val ledgerLedgerAccountQueryService: LedgerAccountQueryServiceGrpcKt.LedgerAccountQueryServiceCoroutineStub,
    private val ledgerLedgerAccountService: LedgerAccountServiceGrpcKt.LedgerAccountServiceCoroutineStub,
    private val metricsMetricSeriesService: MetricSeriesServiceGrpcKt.MetricSeriesServiceCoroutineStub,
    private val notificationsNotificationQueryService: NotificationQueryServiceGrpcKt.NotificationQueryServiceCoroutineStub,
    private val showsShowCalculationService: ShowCalculationServiceGrpcKt.ShowCalculationServiceCoroutineStub,
    private val yardsDockCalculationService: DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub,
) : FuelPolicyValidationServiceGrpcKt.FuelPolicyValidationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(FuelPolicyValidationGrpcService::class.java)

    private val workerForExplainFuelPolicyDecision0 = ReplyMailbox<NormalizeDeliveryExceptionRequest, NormalizeDeliveryExceptionResponse>(workers) { payload ->
        withTimeout(5_000) { deliveriesDeliveryExceptionValidationService.normalizeDeliveryException(payload) }
    }

    private val clientForExplainFuelPolicyDecision1 by lazy { fuelFuelTransactionCalculationService }

    /** Explains a fuel policy. */
    override suspend fun explainFuelPolicyDecision(request: ExplainFuelPolicyDecisionRequest): ExplainFuelPolicyDecisionResponse {
        return handleExplainFuelPolicyDecision(request)
    }

    /** Returns a fuel policy. */
    override suspend fun getFuelPolicyRules(request: GetFuelPolicyRulesRequest): GetFuelPolicyRulesResponse = handleGetFuelPolicyRules(request)

    /** Lints a fuel policy. */
    override suspend fun lintFuelPolicies(request: LintFuelPoliciesRequest): LintFuelPoliciesResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyValidationService/LintFuelPolicies") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LintFuelPolicies", request)
            log.atInfo().addKeyValue("operation", "LintFuelPolicies").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicy.fuelPolicyId).log("Recorded audit detail")
            val configure0: RevertFuelPolicyRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.etag = request.fuelPolicy.audit.etag
                        this.updatedAt = request.fuelPolicy.audit.updatedAt
                    }
                    this.owner = userRef {
                        this.userId = request.fuelPolicy.owner.userId
                    }
                }
            }
            val outbound0 = revertFuelPolicyRequest(configure0)
            val reply0 = fuelFuelPolicyHistoryService.revertFuelPolicy(outbound0)
            lintFuelPoliciesResponse {
                this.fuelPolicy = fuelPolicy {
                    this.fuelPolicyId = request.fuelPolicyId
                    this.owner = request.fuelPolicy.owner
                    this.parameters.putAll(reply0.fuelPolicy.parametersMap)
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    /** Normalizes a fuel policy. */
    override suspend fun normalizeFuelPolicy(request: NormalizeFuelPolicyRequest): NormalizeFuelPolicyResponse {
        return handleNormalizeFuelPolicy(request)
    }

    /** Previews a fuel policy. */
    override suspend fun previewFuelPolicy(request: PreviewFuelPolicyRequest): PreviewFuelPolicyResponse = handlePreviewFuelPolicy(request)

    /** Scores a fuel policy. */
    override suspend fun scoreFuelPolicy(request: ScoreFuelPolicyRequest): ScoreFuelPolicyResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyValidationService/ScoreFuelPolicy") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScoreFuelPolicy", request)
            log.atInfo().addKeyValue("operation", "ScoreFuelPolicy").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicy.fuelPolicyId).log("Recorded audit detail")
            val outbound0 = quoteDockRequest {
                this.context = request.context
                this.dock = dock {
                    this.address = postalAddress {
                        this.lines.addAll(request.details.destination.linesList)
                    }
                    this.audit = auditInfo {
                        this.createdBy = request.fuelPolicy.audit.createdBy
                    }
                }
            }
            val reply0 = yardsDockCalculationService.quoteDock(WireCopies.json(outbound0))
            val outbound1 = ScoreFuelPolicy1Command(
                context = request.context,
                metricSeriesOwnerEmail = request.fuelPolicy.owner.email
            ).toRequest()
            val reply1 = metricsMetricSeriesService.updateMetricSeries(outbound1)
            scoreFuelPolicyResponse {
                this.breakdown = ScoreFuelPolicyResponseKt.breakdown {
                    this.currencyCode = reply0.breakdown.currencyCode
                }
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.updatedAt = reply1.metricSeries.audit.updatedAt
                    }
                    this.fuelPolicyId = request.fuelPolicyId
                    this.version = request.fuelPolicy.version
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Validates a fuel policy. */
    override suspend fun validateFuelPolicy(request: ValidateFuelPolicyRequest): ValidateFuelPolicyResponse {
        return handleValidateFuelPolicy(request)
    }

    private suspend fun handleExplainFuelPolicyDecision(request: ExplainFuelPolicyDecisionRequest): ExplainFuelPolicyDecisionResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyValidationService/ExplainFuelPolicyDecision") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainFuelPolicyDecision", request)
            log.atInfo().addKeyValue("operation", "ExplainFuelPolicyDecision").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicy.fuelPolicyId).log("Recorded audit detail")
            val outbound0 = normalizeDeliveryExceptionRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.customer = deliveryRef {
                        this.displayName = request.fuelPolicy.owner.displayName
                    }
                    this.requester = contactInfo {
                        this.name = request.details.contact.name
                    }
                }
            }
            val reply0 = workerForExplainFuelPolicyDecision0.submit(outbound0)
            val outbound1 = recalculateFuelTransactionsRequest {
                this.context = request.context
                this.fuelTransaction = fuelTransaction {
                    this.correlationId = reply0.result.correlationId
                    this.subject = customerRef {
                        this.displayName = request.fuelPolicy.owner.displayName
                        this.email = request.fuelPolicy.owner.email
                    }
                }
                this.reason = request.fuelPolicy.block.reason
            }
            val reply1 = clientForExplainFuelPolicyDecision1.recalculateFuelTransactions(outbound1)
            val outbound2 = searchLedgerAccountsRequest {
                this.context = request.context
                this.filter = SearchLedgerAccountsRequestKt.filter {
                    this.createdAfter = request.fuelPolicy.audit.createdAt
                    this.labels.putAll(reply1.result.detailsMap)
                    this.ownerId = reply0.deliveryException.assignee.userId
                }
            }
            val reply2 = flowOf(outbound2).map { value -> ledgerLedgerAccountService.searchLedgerAccounts(value) }.single()
            explainFuelPolicyDecisionResponse {
                this.fuelPolicy = fuelPolicy {
                    this.fuelPolicyId = request.fuelPolicyId
                    this.version = request.fuelPolicy.version
                }
                this.result = operationResult {
                    this.correlationId = reply1.fuelTransaction.correlationId
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleGetFuelPolicyRules(request: GetFuelPolicyRulesRequest): GetFuelPolicyRulesResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyValidationService/GetFuelPolicyRules") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetFuelPolicyRules", request)
            log.atInfo().addKeyValue("operation", "GetFuelPolicyRules").log("Handling transport request")
            val outbound0 = getFuelPolicyVersionRequest {
                this.context = request.context
                this.fuelPolicyId = request.fuelPolicyId
            }
            val reply0 = withTimeout(5_000) { fuelFuelPolicyHistoryService.getFuelPolicyVersion(outbound0) }
            getFuelPolicyRulesResponse {
                this.fuelPolicy = fuelPolicy {
                    this.fuelPolicyId = request.fuelPolicyId
                    this.owner = userRef {
                        this.email = reply0.fuelPolicy.owner.email
                    }
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleNormalizeFuelPolicy(request: NormalizeFuelPolicyRequest): NormalizeFuelPolicyResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyValidationService/NormalizeFuelPolicy") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "NormalizeFuelPolicy", request)
            log.atInfo().addKeyValue("operation", "NormalizeFuelPolicy").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicy.fuelPolicyId).log("Recorded audit detail")
            val outbound0 = compareShowOptionsRequest {
                this.context = request.context
                this.show = show {
                    this.audit = auditInfo {
                        this.createdBy = request.fuelPolicy.audit.createdBy
                        this.etag = request.fuelPolicy.audit.etag
                    }
                }
            }
            val reply0 = showsShowCalculationService.compareShowOptions(outbound0)
            val outbound1 = revertFuelPolicyRequest {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.etag = reply0.show.audit.etag
                    }
                }
            }
            val reply1 = fuelFuelPolicyHistoryService.revertFuelPolicy(outbound1)
            val outbound2 = exportLedgerAccountsRequest {
                this.context = request.context
                this.filter = ExportLedgerAccountsRequestKt.filter {
                    this.createdAfter = request.fuelPolicy.audit.updatedAt
                    this.labels.putAll(reply1.fuelPolicy.parametersMap)
                    this.ownerId = request.fuelPolicy.owner.userId
                    this.region = reply0.show.location.region
                }
            }
            ledgerLedgerAccountQueryService.exportLedgerAccounts(outbound2).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            normalizeFuelPolicyResponse {
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.etag = reply0.show.audit.etag
                        this.updatedBy = reply0.show.audit.updatedBy
                    }
                    this.fuelPolicyId = request.fuelPolicyId
                    this.priority = request.fuelPolicy.priority
                }
                this.outcome = NormalizeFuelPolicyResponseKt.outcome {
                    this.message = reply1.result.message
                    this.referenceId = reply1.outcome.referenceId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private fun PreviewFuelPolicyRequest.toCalculateDeliveryExceptionFromPreviewFuelPolicy0(): CalculateDeliveryExceptionRequest {
        val source = this
        return calculateDeliveryExceptionRequest {
            this.context = source.context
            this.deliveryException = deliveryException {
                this.requester = contactInfo {
                    this.phone = source.details.contact.phone
                }
            }
        }
    }

    private suspend fun handlePreviewFuelPolicy(request: PreviewFuelPolicyRequest): PreviewFuelPolicyResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyValidationService/PreviewFuelPolicy") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PreviewFuelPolicy", request)
            log.atInfo().addKeyValue("operation", "PreviewFuelPolicy").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicy.fuelPolicyId).log("Recorded audit detail")
            val outbound0 = request.toCalculateDeliveryExceptionFromPreviewFuelPolicy0()
            val reply0 = deliveriesDeliveryExceptionCalculationService.calculateDeliveryException(outbound0)
            val outbound1 = getFuelPolicyRatesRequest {
                this.context = request.context
                this.fuelPolicyId = request.fuelPolicyId
            }
            val reply1 = fuelFuelPolicyCalculationService.getFuelPolicyRates(outbound1)
            val outbound2 = listFuelPolicyVersionsRequest {
                this.context = request.context
                this.filter = ListFuelPolicyVersionsRequestKt.filter {
                    this.createdAfter = request.fuelPolicy.audit.createdAt
                    this.labels.putAll(reply0.result.detailsMap)
                }
            }
            val envelope2 = ProtoAny.pack(outbound2)
            val reply2 = fuelFuelPolicyHistoryService.listFuelPolicyVersions(envelope2.unpack(ListFuelPolicyVersionsRequest::class.java))
            previewFuelPolicyResponse {
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.deliveryException.audit.updatedBy
                    }
                    this.description = request.fuelPolicy.description
                    this.fuelPolicyId = request.fuelPolicyId
                    this.owner = reply1.fuelPolicy.owner
                }
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.status = "ok"
                }
            }
        }

    private data class ScoreFuelPolicy1Command(
        val context: RequestContext,
        val metricSeriesOwnerEmail: String
    ) {
        fun toRequest(): UpdateMetricSeriesRequest {
            val command = this
            return updateMetricSeriesRequest {
                this.context = command.context
                this.metricSeries = metricSeries {
                    this.owner = userRef {
                        this.email = command.metricSeriesOwnerEmail
                    }
                }
            }
        }
    }

    private suspend fun handleValidateFuelPolicy(request: ValidateFuelPolicyRequest): ValidateFuelPolicyResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyValidationService/ValidateFuelPolicy") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ValidateFuelPolicy", request)
            log.atInfo().addKeyValue("operation", "ValidateFuelPolicy").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicy.fuelPolicyId).log("Recorded audit detail")
            val outbound0 = exportNotificationsRequest {
                this.context = request.context
                this.filter = ExportNotificationsRequestKt.filter {
                    this.createdAfter = request.fuelPolicy.audit.updatedAt
                    this.labels.putAll(request.annotationsMap)
                }
            }
            notificationsNotificationQueryService.exportNotifications(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = createDocumentRequest {
                this.context = request.context
                this.document = document {
                    this.audit = auditInfo {
                        this.createdAt = request.fuelPolicy.audit.createdAt
                        this.updatedAt = request.fuelPolicy.audit.updatedAt
                    }
                    this.counterparty = documentFolderRef {
                        this.displayName = request.fuelPolicy.owner.displayName
                    }
                    this.lines.addAll(listOf(documentLine {
                        this.amount = request.fuelPolicy.discount.amount
                    }))
                }
            }
            val adapter1 = OutboundPort<CreateDocumentRequest, CreateDocumentResponse> { payload ->
                documentsDocumentService.createDocument(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            validateFuelPolicyResponse {
                this.fuelPolicy = fuelPolicy {
                    this.fuelPolicyId = request.fuelPolicyId
                    this.status = request.fuelPolicy.status
                }
                this.result = operationResult {
                    this.status = reply1.result.status
                }
            }
        }
}
