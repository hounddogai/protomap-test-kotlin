package com.acme.transport.dispatch.adapters

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.customerRef
import com.acme.dispatch.v1.CheckDispatchOrderEligibilityRequest
import com.acme.dispatch.v1.CheckDispatchOrderEligibilityResponse
import com.acme.dispatch.v1.CompareDispatchAssignmentOptionsRequest
import com.acme.dispatch.v1.CompareDispatchAssignmentOptionsRequestKt
import com.acme.dispatch.v1.DispatchAssignmentCalculationServiceGrpcKt
import com.acme.dispatch.v1.DispatchOrderValidationServiceGrpcKt
import com.acme.dispatch.v1.EstimateDispatchAssignmentRequest
import com.acme.dispatch.v1.ExplainDispatchAssignmentCalculationRequestKt
import com.acme.dispatch.v1.ExplainDispatchOrderDecisionRequest
import com.acme.dispatch.v1.ExplainDispatchOrderDecisionResponse
import com.acme.dispatch.v1.ExplainDispatchOrderDecisionResponseKt
import com.acme.dispatch.v1.GetDispatchOrderRulesRequest
import com.acme.dispatch.v1.GetDispatchOrderRulesResponse
import com.acme.dispatch.v1.LintDispatchOrdersRequest
import com.acme.dispatch.v1.LintDispatchOrdersResponse
import com.acme.dispatch.v1.LintDispatchOrdersResponseKt
import com.acme.dispatch.v1.NormalizeDispatchOrderRequest
import com.acme.dispatch.v1.NormalizeDispatchOrderResponse
import com.acme.dispatch.v1.NormalizeDispatchOrderResponseKt
import com.acme.dispatch.v1.PreviewDispatchOrderRequest
import com.acme.dispatch.v1.PreviewDispatchOrderResponse
import com.acme.dispatch.v1.ScoreDispatchOrderRequest
import com.acme.dispatch.v1.ScoreDispatchOrderResponse
import com.acme.dispatch.v1.ValidateDispatchOrderRequest
import com.acme.dispatch.v1.ValidateDispatchOrderResponse
import com.acme.dispatch.v1.ValidateDispatchOrderResponseKt
import com.acme.dispatch.v1.calculateDispatchAssignmentRequest
import com.acme.dispatch.v1.checkDispatchOrderEligibilityResponse
import com.acme.dispatch.v1.compareDispatchAssignmentOptionsRequest
import com.acme.dispatch.v1.dispatchAssignment
import com.acme.dispatch.v1.dispatchAssignmentChannelOrigin
import com.acme.dispatch.v1.dispatchAssignmentSla
import com.acme.dispatch.v1.dispatchOrder
import com.acme.dispatch.v1.dispatchOrderLine
import com.acme.dispatch.v1.estimateDispatchAssignmentRequest
import com.acme.dispatch.v1.explainDispatchAssignmentCalculationRequest
import com.acme.dispatch.v1.explainDispatchOrderDecisionResponse
import com.acme.dispatch.v1.getDispatchOrderRulesResponse
import com.acme.dispatch.v1.lintDispatchOrdersResponse
import com.acme.dispatch.v1.normalizeDispatchOrderResponse
import com.acme.dispatch.v1.previewDispatchOrderResponse
import com.acme.dispatch.v1.quoteDispatchAssignmentRequest
import com.acme.dispatch.v1.scoreDispatchOrderResponse
import com.acme.dispatch.v1.validateDispatchOrderResponse
import com.acme.dispatch.v1.zoneRef
import com.acme.drivers.v1.driverRef
import com.acme.featureflags.v1.FeatureFlagServiceGrpcKt
import com.acme.featureflags.v1.FeatureFlagValidationServiceGrpcKt
import com.acme.featureflags.v1.batchGetFeatureFlagsRequest
import com.acme.featureflags.v1.checkFeatureFlagEligibilityRequest
import com.acme.featureflags.v1.cloneFeatureFlagRequest
import com.acme.featureflags.v1.featureFlag
import com.acme.featureflags.v1.featureFlagRouteAction
import com.acme.fuel.v1.FuelPolicyHistoryServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionValidationServiceGrpcKt
import com.acme.fuel.v1.NormalizeFuelTransactionRequest
import com.acme.fuel.v1.NormalizeFuelTransactionRequestKt
import com.acme.fuel.v1.PreviewFuelTransactionRequest
import com.acme.fuel.v1.PreviewFuelTransactionResponse
import com.acme.fuel.v1.RevertFuelPolicyRequest
import com.acme.fuel.v1.RevertFuelPolicyResponse
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.normalizeFuelTransactionRequest
import com.acme.fuel.v1.previewFuelTransactionRequest
import com.acme.fuel.v1.revertFuelPolicyRequest
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.LedgerAccountValidationServiceGrpcKt
import com.acme.ledger.v1.LintLedgerAccountsRequest
import com.acme.ledger.v1.LintLedgerAccountsResponse
import com.acme.ledger.v1.ledgerAccount
import com.acme.ledger.v1.ledgerAccountBankFunding
import com.acme.ledger.v1.lintLedgerAccountsRequest
import com.acme.maintenance.v1.EstimateServiceScheduleRequestKt
import com.acme.maintenance.v1.EstimateServiceScheduleResponse
import com.acme.maintenance.v1.ServiceScheduleCalculationServiceGrpcKt
import com.acme.maintenance.v1.ValidateWorkOrderRequest
import com.acme.maintenance.v1.WorkOrderValidationServiceGrpcKt
import com.acme.maintenance.v1.estimateServiceScheduleRequest
import com.acme.maintenance.v1.serviceSchedule
import com.acme.maintenance.v1.validateWorkOrderRequest
import com.acme.maintenance.v1.workOrder
import com.acme.maintenance.v1.workOrderComment
import com.acme.metrics.v1.MetricSeriesComponent
import com.acme.metrics.v1.MetricSeriesValidationServiceGrpcKt
import com.acme.metrics.v1.lintMetricSeriesesRequest
import com.acme.metrics.v1.metricSeries
import com.acme.ratelimiting.v1.RateLimitServiceGrpcKt
import com.acme.ratelimiting.v1.watchRateLimitsRequest
import com.acme.telematics.v1.ExplainTripLogDecisionResponse
import com.acme.telematics.v1.TripLogValidationServiceGrpcKt
import com.acme.telematics.v1.explainTripLogDecisionRequest
import com.acme.telematics.v1.tripLog
import com.acme.transport.dispatch.DispatchRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.google.protobuf.Any as ProtoAny
import com.google.protobuf.Timestamp
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

/** Checks dispatch orders against the rules before anything acts on them. */
class DispatchOrderValidationController(
    private val store: DispatchRepository,
    private val workers: CoroutineScope,
    private val dispatchDispatchAssignmentCalculationService: DispatchAssignmentCalculationServiceGrpcKt.DispatchAssignmentCalculationServiceCoroutineStub,
    private val featureflagsFeatureFlagService: FeatureFlagServiceGrpcKt.FeatureFlagServiceCoroutineStub,
    private val featureflagsFeatureFlagValidationService: FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub,
    private val fuelFuelPolicyHistoryService: FuelPolicyHistoryServiceGrpcKt.FuelPolicyHistoryServiceCoroutineStub,
    private val fuelFuelTransactionValidationService: FuelTransactionValidationServiceGrpcKt.FuelTransactionValidationServiceCoroutineStub,
    private val ledgerLedgerAccountValidationService: LedgerAccountValidationServiceGrpcKt.LedgerAccountValidationServiceCoroutineStub,
    private val maintenanceServiceScheduleCalculationService: ServiceScheduleCalculationServiceGrpcKt.ServiceScheduleCalculationServiceCoroutineStub,
    private val maintenanceWorkOrderValidationService: WorkOrderValidationServiceGrpcKt.WorkOrderValidationServiceCoroutineStub,
    private val metricsMetricSeriesValidationService: MetricSeriesValidationServiceGrpcKt.MetricSeriesValidationServiceCoroutineStub,
    private val ratelimitingRateLimitService: RateLimitServiceGrpcKt.RateLimitServiceCoroutineStub,
    private val telematicsTripLogValidationService: TripLogValidationServiceGrpcKt.TripLogValidationServiceCoroutineStub,
) : DispatchOrderValidationServiceGrpcKt.DispatchOrderValidationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DispatchOrderValidationController::class.java)

    private val workerForValidateDispatchOrder0 = ReplyMailbox<LintLedgerAccountsRequest, LintLedgerAccountsResponse>(workers) { payload ->
        withTimeout(5_000) { ledgerLedgerAccountValidationService.lintLedgerAccounts(payload) }
    }

    private val clientForValidateDispatchOrder1 by lazy { dispatchDispatchAssignmentCalculationService }

    /** Checks a dispatch order. */
    override suspend fun checkDispatchOrderEligibility(request: CheckDispatchOrderEligibilityRequest): CheckDispatchOrderEligibilityResponse = handleCheckDispatchOrderEligibility(request)

    /** Explains a dispatch order. */
    override suspend fun explainDispatchOrderDecision(request: ExplainDispatchOrderDecisionRequest): ExplainDispatchOrderDecisionResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderValidationService/ExplainDispatchOrderDecision") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainDispatchOrderDecision", request)
            log.atInfo().addKeyValue("operation", "ExplainDispatchOrderDecision").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchOrder.dispatchOrderId).log("Recorded audit detail")
            val outbound0 = explainTripLogDecisionRequest {
                this.context = request.context
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.etag = request.dispatchOrder.audit.etag
                    }
                }
            }
            val reply0 = telematicsTripLogValidationService.explainTripLogDecision(outbound0)
            val outbound1 = watchRateLimitsRequest {
                this.context = request.context
                this.subscriber = contactInfo {
                    this.email = reply0.tripLog.counterparty.email
                    this.name = request.details.contact.name
                }
            }
            ratelimitingRateLimitService.watchRateLimits(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound2 = request.toCompareDispatchAssignmentOptionsFromExplainDispatchOrderDecision2(reply0)
            val reply2 = dispatchDispatchAssignmentCalculationService.compareDispatchAssignmentOptions(outbound2)
            explainDispatchOrderDecisionResponse {
                this.dispatchOrder = dispatchOrder {
                    this.audit = auditInfo {
                        this.updatedBy = reply2.dispatchAssignment.audit.updatedBy
                    }
                    this.counterparty = zoneRef {
                        this.displayName = reply2.dispatchAssignment.customer.displayName
                    }
                    this.dispatchOrderId = request.dispatchOrderId
                    this.notes = request.dispatchOrder.notes
                }
                this.outcome = ExplainDispatchOrderDecisionResponseKt.outcome {
                    this.effectiveAt = reply0.outcome.effectiveAt
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Returns a dispatch order. */
    override suspend fun getDispatchOrderRules(request: GetDispatchOrderRulesRequest): GetDispatchOrderRulesResponse {
        return handleGetDispatchOrderRules(request)
    }

    /** Lints a dispatch order. */
    override suspend fun lintDispatchOrders(request: LintDispatchOrdersRequest): LintDispatchOrdersResponse = handleLintDispatchOrders(request)

    /** Normalizes a dispatch order. */
    override suspend fun normalizeDispatchOrder(request: NormalizeDispatchOrderRequest): NormalizeDispatchOrderResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderValidationService/NormalizeDispatchOrder") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "NormalizeDispatchOrder", request)
            log.atInfo().addKeyValue("operation", "NormalizeDispatchOrder").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchOrder.dispatchOrderId).log("Recorded audit detail")
            val outbound0 = previewFuelTransactionRequest {
                this.context = request.context
                this.fuelTransaction = fuelTransaction {
                    this.subject = customerRef {
                        this.email = request.details.contact.email
                    }
                }
            }
            val adapter0 = OutboundPort<PreviewFuelTransactionRequest, PreviewFuelTransactionResponse> { payload ->
                fuelFuelTransactionValidationService.previewFuelTransaction(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            var reply1 = EstimateServiceScheduleResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.dispatchOrder.linesList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound1 = estimateServiceScheduleRequest {
                                this.context = request.context
                                this.details = EstimateServiceScheduleRequestKt.details {
                                    this.budget = item.tax.amount
                                    this.destination = postalAddress {
                                        this.region = request.dispatchOrder.billingAddress.region
                                    }
                                }
                                this.serviceSchedule = serviceSchedule {
                                    this.audit = auditInfo {
                                        this.etag = request.dispatchOrder.audit.etag
                                    }
                                }
                            }
                            maintenanceServiceScheduleCalculationService.estimateServiceSchedule(outbound1)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply1 = it }
                }
            }
            normalizeDispatchOrderResponse {
                this.dispatchOrder = dispatchOrder {
                    this.dispatchOrderId = request.dispatchOrderId
                    this.notes = request.dispatchOrder.notes
                }
                this.outcome = NormalizeDispatchOrderResponseKt.outcome {
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.message = reply0.outcome.message
                    this.status = "ok"
                }
            }
        }

    /** Previews a dispatch order. */
    override suspend fun previewDispatchOrder(request: PreviewDispatchOrderRequest): PreviewDispatchOrderResponse {
        return handlePreviewDispatchOrder(request)
    }

    /** Scores a dispatch order. */
    override suspend fun scoreDispatchOrder(request: ScoreDispatchOrderRequest): ScoreDispatchOrderResponse = handleScoreDispatchOrder(request)

    /** Validates a dispatch order. */
    override suspend fun validateDispatchOrder(request: ValidateDispatchOrderRequest): ValidateDispatchOrderResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderValidationService/ValidateDispatchOrder") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ValidateDispatchOrder", request)
            log.atInfo().addKeyValue("operation", "ValidateDispatchOrder").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchOrder.dispatchOrderId).log("Recorded audit detail")
            val outbound0 = lintLedgerAccountsRequest {
                this.context = request.context
                this.ledgerAccount = ledgerAccount {
                    this.audit = auditInfo {
                        this.createdBy = request.dispatchOrder.audit.createdBy
                        this.updatedBy = request.dispatchOrder.audit.updatedBy
                    }
                    this.bank = ledgerAccountBankFunding {
                        this.accountHolder = request.dispatchOrder.bank.accountHolder
                    }
                    this.contact = contactInfo {
                        this.email = request.details.contact.email
                    }
                }
            }
            val reply0 = workerForValidateDispatchOrder0.submit(outbound0)
            val outbound1 = explainDispatchAssignmentCalculationRequest {
                this.context = request.context
                this.details = ExplainDispatchAssignmentCalculationRequestKt.details {
                    this.contact = contactInfo {
                        this.preferredChannel = request.details.contact.preferredChannel
                    }
                }
                this.dispatchAssignment = dispatchAssignment {
                    this.assignee = userRef {
                        this.email = request.details.contact.email
                    }
                    this.audit = auditInfo {
                        this.etag = reply0.ledgerAccount.audit.etag
                    }
                    this.customer = driverRef {
                        this.displayName = reply0.ledgerAccount.holder.displayName
                    }
                }
            }
            val reply1 = clientForValidateDispatchOrder1.explainDispatchAssignmentCalculation(outbound1)
            validateDispatchOrderResponse {
                this.dispatchOrder = dispatchOrder {
                    this.audit = auditInfo {
                        this.etag = reply0.ledgerAccount.audit.etag
                    }
                    this.dispatchOrderId = request.dispatchOrderId
                    this.metadata.putAll(request.dispatchOrder.metadataMap)
                }
                this.outcome = ValidateDispatchOrderResponseKt.outcome {
                    this.message = reply0.outcome.message
                    this.nextSteps.addAll(reply1.outcome.nextStepsList)
                }
                this.result = operationResult {
                    this.message = reply1.outcome.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleCheckDispatchOrderEligibility(request: CheckDispatchOrderEligibilityRequest): CheckDispatchOrderEligibilityResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderValidationService/CheckDispatchOrderEligibility") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CheckDispatchOrderEligibility", request)
            log.atInfo().addKeyValue("operation", "CheckDispatchOrderEligibility").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchOrder.dispatchOrderId).log("Recorded audit detail")
            val outbound0 = batchGetFeatureFlagsRequest {
                this.context = request.context
            }
            val reply0 = featureflagsFeatureFlagService.batchGetFeatureFlags(outbound0)
            checkDispatchOrderEligibilityResponse {
                this.dispatchOrder = dispatchOrder {
                    this.dispatchOrderId = request.dispatchOrderId
                    this.total = request.dispatchOrder.total
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = reply0.result.status
                }
            }
        }

    private fun ExplainDispatchOrderDecisionRequest.toCompareDispatchAssignmentOptionsFromExplainDispatchOrderDecision2(reply0: ExplainTripLogDecisionResponse): CompareDispatchAssignmentOptionsRequest {
        val source = this
        return compareDispatchAssignmentOptionsRequest {
            this.context = source.context
            this.details = CompareDispatchAssignmentOptionsRequestKt.details {
                this.destination = postalAddress {
                    this.countryCode = reply0.tripLog.billingAddress.countryCode
                    this.lines.addAll(source.dispatchOrder.billingAddress.linesList)
                }
            }
            this.dispatchAssignment = dispatchAssignment {
                this.customer = driverRef {
                    this.email = source.details.contact.email
                }
                this.requester = contactInfo {
                    this.email = reply0.tripLog.counterparty.email
                }
            }
        }
    }

    private suspend fun handleGetDispatchOrderRules(request: GetDispatchOrderRulesRequest): GetDispatchOrderRulesResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderValidationService/GetDispatchOrderRules") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetDispatchOrderRules", request)
            log.atInfo().addKeyValue("operation", "GetDispatchOrderRules").log("Handling transport request")
            val outbound0 = calculateDispatchAssignmentRequest {
                this.context = request.context
                this.dispatchAssignment = dispatchAssignment {
                    this.assignee = userRef {
                        this.userId = request.context.actor.userId
                    }
                    this.customer = driverRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = dispatchDispatchAssignmentCalculationService.calculateDispatchAssignment(outbound0)
            getDispatchOrderRulesResponse {
                this.dispatchOrder = dispatchOrder {
                    this.dispatchOrderId = request.dispatchOrderId
                    this.dueAt = reply0.dispatchAssignment.sla.dueAt
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private data class LintDispatchOrders2Command(
        val context: RequestContext,
        val dispatchAssignmentSlaDueAt: Timestamp,
        val dispatchAssignmentAuditUpdatedAt: Timestamp
    ) {
        fun toRequest(): CompareDispatchAssignmentOptionsRequest {
            val command = this
            return compareDispatchAssignmentOptionsRequest {
                this.context = command.context
                this.dispatchAssignment = dispatchAssignment {
                    this.audit = auditInfo {
                        this.updatedAt = command.dispatchAssignmentAuditUpdatedAt
                    }
                    this.sla = dispatchAssignmentSla {
                        this.dueAt = command.dispatchAssignmentSlaDueAt
                    }
                }
            }
        }
    }

    private suspend fun handleLintDispatchOrders(request: LintDispatchOrdersRequest): LintDispatchOrdersResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderValidationService/LintDispatchOrders") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LintDispatchOrders", request)
            log.atInfo().addKeyValue("operation", "LintDispatchOrders").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchOrder.dispatchOrderId).log("Recorded audit detail")
            val outbound0 = validateWorkOrderRequest {
                this.context = request.context
                this.workOrder = workOrder {
                    this.assignee = userRef {
                        this.email = request.details.contact.email
                    }
                    this.audit = auditInfo {
                        this.updatedBy = request.dispatchOrder.audit.updatedBy
                    }
                    this.comments.addAll(listOf(workOrderComment {
                        this.createdAt = request.dispatchOrder.audit.createdAt
                    }))
                    this.customer = driverRef {
                        this.displayName = request.dispatchOrder.counterparty.displayName
                    }
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = maintenanceWorkOrderValidationService.validateWorkOrder(envelope0.unpack(ValidateWorkOrderRequest::class.java))
            val outbound1 = quoteDispatchAssignmentRequest {
                this.context = request.context
                this.dispatchAssignment = dispatchAssignment {
                    this.channel = dispatchAssignmentChannelOrigin {
                        this.channel = reply0.workOrder.channel.channel
                    }
                }
            }
            val reply1 = dispatchDispatchAssignmentCalculationService.quoteDispatchAssignment(WireCopies.json(outbound1))
            val outbound2 = LintDispatchOrders2Command(
                context = request.context,
                dispatchAssignmentSlaDueAt = reply0.workOrder.sla.dueAt,
                dispatchAssignmentAuditUpdatedAt = reply0.workOrder.audit.updatedAt
            ).toRequest()
            val reply2 = dispatchDispatchAssignmentCalculationService.compareDispatchAssignmentOptions(outbound2)
            val outbound3 = normalizeFuelTransactionRequest {
                this.context = request.context
                this.details = NormalizeFuelTransactionRequestKt.details {
                    this.contact = contactInfo {
                        this.email = reply1.dispatchAssignment.customer.email
                    }
                }
                this.fuelTransaction = fuelTransaction {
                    this.subject = customerRef {
                        this.displayName = reply1.dispatchAssignment.customer.displayName
                        this.email = reply0.workOrder.customer.email
                    }
                }
            }
            val command3: Submission<NormalizeFuelTransactionRequest> = Submission.Live(outbound3)
            val payload3 = when (val command = command3) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply3 = fuelFuelTransactionValidationService.normalizeFuelTransaction(payload3)
            lintDispatchOrdersResponse {
                this.dispatchOrder = dispatchOrder {
                    this.audit = auditInfo {
                        this.updatedAt = reply1.dispatchAssignment.audit.updatedAt
                    }
                    this.dispatchOrderId = request.dispatchOrderId
                    this.notes = request.dispatchOrder.notes
                }
                this.outcome = LintDispatchOrdersResponseKt.outcome {
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply2.result.referenceIdsList)
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handlePreviewDispatchOrder(request: PreviewDispatchOrderRequest): PreviewDispatchOrderResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderValidationService/PreviewDispatchOrder") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PreviewDispatchOrder", request)
            log.atInfo().addKeyValue("operation", "PreviewDispatchOrder").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchOrder.dispatchOrderId).log("Recorded audit detail")
            val outbound0 = revertFuelPolicyRequest {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.owner = userRef {
                        this.displayName = request.dispatchOrder.counterparty.displayName
                    }
                }
            }
            val invoke0: suspend (RevertFuelPolicyRequest, Metadata) -> RevertFuelPolicyResponse = fuelFuelPolicyHistoryService::revertFuelPolicy
            val reply0 = invoke0(outbound0, Metadata())
            val outbound1 = EstimateDispatchAssignmentRequest.newBuilder().apply {
                setContext(request.context)
                setDispatchAssignment(dispatchAssignment {
                    this.assignee = userRef {
                        this.displayName = reply0.fuelPolicy.owner.displayName
                    }
                    this.audit = auditInfo {
                        this.updatedAt = request.dispatchOrder.audit.updatedAt
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> dispatchDispatchAssignmentCalculationService.estimateDispatchAssignment(value) }
            val outbound2 = RequestFactory.create {
                cloneFeatureFlagRequest {
                    this.context = request.context
                    this.featureFlag = featureFlag {
                        this.route = featureFlagRouteAction {
                            this.weight = reply0.fuelPolicy.route.weight
                        }
                    }
                }
            }
            val reply2 = featureflagsFeatureFlagService.cloneFeatureFlag(outbound2)
            previewDispatchOrderResponse {
                this.dispatchOrder = dispatchOrder {
                    this.audit = auditInfo {
                        this.createdAt = reply0.fuelPolicy.audit.createdAt
                    }
                    this.dispatchOrderId = request.dispatchOrderId
                    this.lines.addAll(listOf(dispatchOrderLine {
                        this.amount = reply2.featureFlag.discount.amount
                    }))
                    this.metadata.putAll(request.dispatchOrder.metadataMap)
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleScoreDispatchOrder(request: ScoreDispatchOrderRequest): ScoreDispatchOrderResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderValidationService/ScoreDispatchOrder") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScoreDispatchOrder", request)
            log.atInfo().addKeyValue("operation", "ScoreDispatchOrder").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchOrder.dispatchOrderId).log("Recorded audit detail")
            val outbound0 = checkFeatureFlagEligibilityRequest {
                this.context = request.context
                this.featureFlag = featureFlag {
                    this.audit = auditInfo {
                        this.etag = request.dispatchOrder.audit.etag
                        this.updatedBy = request.dispatchOrder.audit.updatedBy
                    }
                }
            }
            val reply0 = ClientRegistry.lookup("acme.featureflags.v1.FeatureFlagValidationService", FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub::class.java).checkFeatureFlagEligibility(outbound0)
            val outbound1 = lintMetricSeriesesRequest {
                this.context = request.context
                this.metricSeries = metricSeries {
                    this.owner = userRef {
                        this.displayName = request.dispatchOrder.counterparty.displayName
                        this.email = reply0.featureFlag.owner.email
                    }
                }
            }
            val reply1 = metricsMetricSeriesValidationService.lintMetricSerieses(WireCopies.reflective(outbound1))
            scoreDispatchOrderResponse {
                this.dispatchOrder = dispatchOrder {
                    this.audit = auditInfo {
                        this.createdBy = reply1.metricSeries.audit.createdBy
                    }
                    this.dispatchOrderId = request.dispatchOrderId
                    this.lines.addAll(listOf(dispatchOrderLine {
                        this.quantity = (reply1.metricSeries.componentsMap["primary"] ?: MetricSeriesComponent.getDefaultInstance()).quantity
                    }))
                    this.notes = request.dispatchOrder.notes
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }
}
