package com.acme.transport.deliveries.grpc

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.CheckProofOfDeliveryEligibilityRequest
import com.acme.deliveries.v1.CheckProofOfDeliveryEligibilityResponse
import com.acme.deliveries.v1.CompareDeliveryExceptionOptionsRequest
import com.acme.deliveries.v1.CompareDeliveryExceptionOptionsRequestKt
import com.acme.deliveries.v1.CompareDeliveryExceptionOptionsResponse
import com.acme.deliveries.v1.CompareProofOfDeliveryOptionsRequestKt
import com.acme.deliveries.v1.DeliveryExceptionCalculationServiceGrpcKt
import com.acme.deliveries.v1.DeliveryValidationServiceGrpcKt
import com.acme.deliveries.v1.EstimateDeliveryExceptionRequest
import com.acme.deliveries.v1.EstimateDeliveryExceptionResponse
import com.acme.deliveries.v1.ExplainProofOfDeliveryCalculationRequestKt
import com.acme.deliveries.v1.ExplainProofOfDeliveryCalculationResponse
import com.acme.deliveries.v1.ExplainProofOfDeliveryDecisionRequest
import com.acme.deliveries.v1.ExplainProofOfDeliveryDecisionResponse
import com.acme.deliveries.v1.LintProofOfDeliveriesRequest
import com.acme.deliveries.v1.LintProofOfDeliveriesResponse
import com.acme.deliveries.v1.LintProofOfDeliveriesResponseKt
import com.acme.deliveries.v1.NormalizeProofOfDeliveryRequest
import com.acme.deliveries.v1.NormalizeProofOfDeliveryResponse
import com.acme.deliveries.v1.PreviewProofOfDeliveryRequest
import com.acme.deliveries.v1.PreviewProofOfDeliveryResponse
import com.acme.deliveries.v1.ProofOfDeliveryCalculationServiceGrpcKt
import com.acme.deliveries.v1.ProofOfDeliveryLine
import com.acme.deliveries.v1.ProofOfDeliveryValidationServiceGrpcKt
import com.acme.deliveries.v1.ScoreProofOfDeliveryRequest
import com.acme.deliveries.v1.ScoreProofOfDeliveryResponse
import com.acme.deliveries.v1.ScoreProofOfDeliveryResponseKt
import com.acme.deliveries.v1.ValidateDeliveryRequest
import com.acme.deliveries.v1.ValidateDeliveryResponse
import com.acme.deliveries.v1.ValidateProofOfDeliveryRequest
import com.acme.deliveries.v1.ValidateProofOfDeliveryResponse
import com.acme.deliveries.v1.calculateProofOfDeliveryRequest
import com.acme.deliveries.v1.checkProofOfDeliveryEligibilityResponse
import com.acme.deliveries.v1.compareDeliveryExceptionOptionsRequest
import com.acme.deliveries.v1.compareProofOfDeliveryOptionsRequest
import com.acme.deliveries.v1.delivery
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.deliveryExceptionSla
import com.acme.deliveries.v1.estimateDeliveryExceptionRequest
import com.acme.deliveries.v1.estimateProofOfDeliveryRequest
import com.acme.deliveries.v1.explainProofOfDeliveryCalculationRequest
import com.acme.deliveries.v1.explainProofOfDeliveryDecisionResponse
import com.acme.deliveries.v1.lintProofOfDeliveriesResponse
import com.acme.deliveries.v1.normalizeProofOfDeliveryResponse
import com.acme.deliveries.v1.previewProofOfDeliveryResponse
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.proofOfDeliveryBankSettlement
import com.acme.deliveries.v1.proofOfDeliveryCardSettlement
import com.acme.deliveries.v1.proofOfDeliveryLine
import com.acme.deliveries.v1.proofOfDeliveryLineTax
import com.acme.deliveries.v1.scoreProofOfDeliveryResponse
import com.acme.deliveries.v1.validateDeliveryRequest
import com.acme.deliveries.v1.validateProofOfDeliveryResponse
import com.acme.documents.v1.DocumentQueryServiceGrpcKt
import com.acme.documents.v1.FindDocumentsRequestKt
import com.acme.documents.v1.findDocumentsRequest
import com.acme.drivers.v1.driverRef
import com.acme.identity.v1.ScoreUserRequest
import com.acme.identity.v1.UserValidationServiceGrpcKt
import com.acme.identity.v1.scoreUserRequest
import com.acme.identity.v1.user
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.LedgerAccountServiceGrpcKt
import com.acme.ledger.v1.createLedgerAccountRequest
import com.acme.ledger.v1.ledgerAccount
import com.acme.ledger.v1.ledgerAccountBankFunding
import com.acme.transport.deliveries.DeliveriesRepository
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.BrowseDocksRequestKt
import com.acme.yards.v1.BrowseDocksResponse
import com.acme.yards.v1.DockQueryServiceGrpcKt
import com.acme.yards.v1.PreviewYardRequest
import com.acme.yards.v1.YardMoveCalculationServiceGrpcKt
import com.acme.yards.v1.YardValidationServiceGrpcKt
import com.acme.yards.v1.browseDocksRequest
import com.acme.yards.v1.previewYardRequest
import com.acme.yards.v1.quoteYardMoveRequest
import com.acme.yards.v1.yard
import com.acme.yards.v1.yardMove
import com.google.protobuf.Any as ProtoAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Checks proof of deliveries against the rules before anything acts on them. */
class ProofOfDeliveryValidationEndpoint(
    private val store: DeliveriesRepository,
    private val workers: CoroutineScope,
    private val deliveriesDeliveryExceptionCalculationService: DeliveryExceptionCalculationServiceGrpcKt.DeliveryExceptionCalculationServiceCoroutineStub,
    private val deliveriesDeliveryValidationService: DeliveryValidationServiceGrpcKt.DeliveryValidationServiceCoroutineStub,
    private val deliveriesProofOfDeliveryCalculationService: ProofOfDeliveryCalculationServiceGrpcKt.ProofOfDeliveryCalculationServiceCoroutineStub,
    private val documentsDocumentQueryService: DocumentQueryServiceGrpcKt.DocumentQueryServiceCoroutineStub,
    private val identityUserValidationService: UserValidationServiceGrpcKt.UserValidationServiceCoroutineStub,
    private val ledgerLedgerAccountService: LedgerAccountServiceGrpcKt.LedgerAccountServiceCoroutineStub,
    private val yardsDockQueryService: DockQueryServiceGrpcKt.DockQueryServiceCoroutineStub,
    private val yardsYardMoveCalculationService: YardMoveCalculationServiceGrpcKt.YardMoveCalculationServiceCoroutineStub,
    private val yardsYardValidationService: YardValidationServiceGrpcKt.YardValidationServiceCoroutineStub,
) : ProofOfDeliveryValidationServiceGrpcKt.ProofOfDeliveryValidationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(ProofOfDeliveryValidationEndpoint::class.java)

    private val workerForCheckProofOfDeliveryEligibility1 = ReplyMailbox<ValidateDeliveryRequest, ValidateDeliveryResponse>(workers) { payload ->
        withTimeout(5_000) { deliveriesDeliveryValidationService.validateDelivery(payload) }
    }

    /** Checks a proof of delivery. */
    override suspend fun checkProofOfDeliveryEligibility(request: CheckProofOfDeliveryEligibilityRequest): CheckProofOfDeliveryEligibilityResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryValidationService/CheckProofOfDeliveryEligibility") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CheckProofOfDeliveryEligibility", request)
            log.atInfo().addKeyValue("operation", "CheckProofOfDeliveryEligibility").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.proofOfDelivery.proofOfDeliveryId).log("Recorded audit detail")
            val outbound0 = compareProofOfDeliveryOptionsRequest {
                this.context = request.context
                this.details = CompareProofOfDeliveryOptionsRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = request.proofOfDelivery.billingAddress.countryCode
                    }
                }
                this.proofOfDelivery = proofOfDelivery {
                    this.audit = auditInfo {
                        this.etag = request.proofOfDelivery.audit.etag
                    }
                }
            }
            val reply0 = deliveriesProofOfDeliveryCalculationService.compareProofOfDeliveryOptions(WireCopies.reflective(outbound0))
            val outbound1 = validateDeliveryRequest {
                this.context = request.context
                this.delivery = delivery {
                    this.audit = auditInfo {
                        this.etag = reply0.proofOfDelivery.audit.etag
                    }
                }
            }
            val reply1 = workerForCheckProofOfDeliveryEligibility1.submit(outbound1)
            checkProofOfDeliveryEligibilityResponse {
                this.proofOfDelivery = proofOfDelivery {
                    this.billingAddress = postalAddress {
                        this.lines.addAll(reply0.proofOfDelivery.billingAddress.linesList)
                    }
                    this.counterparty = request.proofOfDelivery.counterparty
                    this.dueAt = reply1.delivery.dueAt
                    this.proofOfDeliveryId = request.proofOfDeliveryId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Explains a proof of delivery. */
    override suspend fun explainProofOfDeliveryDecision(request: ExplainProofOfDeliveryDecisionRequest): ExplainProofOfDeliveryDecisionResponse {
        return handleExplainProofOfDeliveryDecision(request)
    }

    /** Lints a proof of delivery. */
    override suspend fun lintProofOfDeliveries(request: LintProofOfDeliveriesRequest): LintProofOfDeliveriesResponse = handleLintProofOfDeliveries(request)

    /** Normalizes a proof of delivery. */
    override suspend fun normalizeProofOfDelivery(request: NormalizeProofOfDeliveryRequest): NormalizeProofOfDeliveryResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryValidationService/NormalizeProofOfDelivery") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "NormalizeProofOfDelivery", request)
            log.atInfo().addKeyValue("operation", "NormalizeProofOfDelivery").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.proofOfDelivery.proofOfDeliveryId).log("Recorded audit detail")
            val outbound0 = compareDeliveryExceptionOptionsRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.audit = auditInfo {
                        this.createdAt = request.proofOfDelivery.audit.createdAt
                    }
                    this.requester = contactInfo {
                        this.name = request.details.contact.name
                    }
                }
                this.details = CompareDeliveryExceptionOptionsRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(request.proofOfDelivery.billingAddress.linesList)
                        this.region = request.proofOfDelivery.billingAddress.region
                    }
                }
            }
            val reply0 = deliveriesDeliveryExceptionCalculationService.compareDeliveryExceptionOptions(outbound0)
            var reply1 = BrowseDocksResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<ProofOfDeliveryLine>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound1 = browseDocksRequest {
                            this.context = request.context
                            this.filter = BrowseDocksRequestKt.filter {
                                this.ownerId = item.lineId
                                this.region = request.proofOfDelivery.billingAddress.region
                            }
                        }
                        reply1 = yardsDockQueryService.browseDocks(outbound1)
                    }
                }
                try {
                    request.proofOfDelivery.linesList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound2 = request.toPreviewYardFromNormalizeProofOfDelivery2(reply0)
            val reply2 = yardsYardValidationService.previewYard(outbound2)
            normalizeProofOfDeliveryResponse {
                this.proofOfDelivery = proofOfDelivery {
                    this.audit = auditInfo {
                        this.createdAt = reply0.deliveryException.audit.createdAt
                    }
                    this.billingAddress = postalAddress {
                        this.locality = reply2.yard.address.locality
                    }
                    this.number = request.proofOfDelivery.number
                    this.proofOfDeliveryId = request.proofOfDeliveryId
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    /** Previews a proof of delivery. */
    override suspend fun previewProofOfDelivery(request: PreviewProofOfDeliveryRequest): PreviewProofOfDeliveryResponse {
        return handlePreviewProofOfDelivery(request)
    }

    /** Scores a proof of delivery. */
    override suspend fun scoreProofOfDelivery(request: ScoreProofOfDeliveryRequest): ScoreProofOfDeliveryResponse = handleScoreProofOfDelivery(request)

    /** Validates a proof of delivery. */
    override suspend fun validateProofOfDelivery(request: ValidateProofOfDeliveryRequest): ValidateProofOfDeliveryResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryValidationService/ValidateProofOfDelivery") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ValidateProofOfDelivery", request)
            log.atInfo().addKeyValue("operation", "ValidateProofOfDelivery").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.proofOfDelivery.proofOfDeliveryId).log("Recorded audit detail")
            val outbound0 = ValidateProofOfDelivery0Command(
                context = request.context,
                deliveryExceptionRequesterEmail = request.proofOfDelivery.counterparty.email,
                deliveryExceptionAssigneeEmail = request.details.contact.email
            ).toRequest()
            val reply0 = deliveriesDeliveryExceptionCalculationService.compareDeliveryExceptionOptions(outbound0)
            val outbound1 = scoreUserRequest {
                this.context = request.context
                this.user = user {
                    this.audit = auditInfo {
                        this.createdBy = reply0.deliveryException.audit.createdBy
                    }
                    this.contact = contactInfo {
                        this.name = reply0.deliveryException.requester.name
                    }
                    this.primaryAddress = postalAddress {
                        this.lines.addAll(request.proofOfDelivery.billingAddress.linesList)
                    }
                }
            }
            val command1: Submission<ScoreUserRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = identityUserValidationService.scoreUser(payload1)
            val outbound2 = estimateDeliveryExceptionRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.requester = contactInfo {
                        this.preferredChannel = reply0.deliveryException.requester.preferredChannel
                    }
                    this.sla = deliveryExceptionSla {
                        this.dueAt = reply0.deliveryException.sla.dueAt
                    }
                }
            }
            val adapter2 = OutboundPort<EstimateDeliveryExceptionRequest, EstimateDeliveryExceptionResponse> { payload ->
                deliveriesDeliveryExceptionCalculationService.estimateDeliveryException(payload)
            }
            val port2 = DelegatingPort(adapter2)
            val reply2 = port2.execute(outbound2)
            validateProofOfDeliveryResponse {
                this.proofOfDelivery = proofOfDelivery {
                    this.counterparty = driverRef {
                        this.displayName = reply0.deliveryException.customer.displayName
                    }
                    this.dueAt = request.proofOfDelivery.dueAt
                    this.proofOfDeliveryId = request.proofOfDeliveryId
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleExplainProofOfDeliveryDecision(request: ExplainProofOfDeliveryDecisionRequest): ExplainProofOfDeliveryDecisionResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryValidationService/ExplainProofOfDeliveryDecision") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainProofOfDeliveryDecision", request)
            log.atInfo().addKeyValue("operation", "ExplainProofOfDeliveryDecision").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.proofOfDelivery.proofOfDeliveryId).log("Recorded audit detail")
            var reply0 = ExplainProofOfDeliveryCalculationResponse.getDefaultInstance()
            request.proofOfDelivery.linesList.asFlow().map { item ->
                val outbound0 = explainProofOfDeliveryCalculationRequest {
                    this.context = request.context
                    this.proofOfDelivery = proofOfDelivery {
                        this.lines.addAll(listOf(proofOfDeliveryLine {
                            this.amount = item.tax.amount
                            this.tax = proofOfDeliveryLineTax {
                                this.exemptions.addAll(item.tax.exemptionsList)
                                this.jurisdiction = item.tax.jurisdiction
                            }
                        }))
                    }
                }
                deliveriesProofOfDeliveryCalculationService.explainProofOfDeliveryCalculation(outbound0)
            }.collect { reply0 = it }
            explainProofOfDeliveryDecisionResponse {
                this.proofOfDelivery = proofOfDelivery {
                    this.billingAddress = request.proofOfDelivery.billingAddress
                    this.proofOfDeliveryId = request.proofOfDeliveryId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleLintProofOfDeliveries(request: LintProofOfDeliveriesRequest): LintProofOfDeliveriesResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryValidationService/LintProofOfDeliveries") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LintProofOfDeliveries", request)
            log.atInfo().addKeyValue("operation", "LintProofOfDeliveries").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.proofOfDelivery.proofOfDeliveryId).log("Recorded audit detail")
            val outbound0 = calculateProofOfDeliveryRequest {
                this.context = request.context
                this.proofOfDelivery = proofOfDelivery {
                    this.billingAddress = postalAddress {
                        this.region = request.proofOfDelivery.billingAddress.region
                    }
                    this.card = proofOfDeliveryCardSettlement {
                        this.network = request.proofOfDelivery.card.network
                        this.token = request.proofOfDelivery.card.token
                    }
                    this.counterparty = driverRef {
                        this.email = request.details.contact.email
                    }
                }
            }
            val reply0 = flowOf(outbound0).map { value -> deliveriesProofOfDeliveryCalculationService.calculateProofOfDelivery(value) }.single()
            val outbound1 = findDocumentsRequest {
                this.context = request.context
                this.filter = FindDocumentsRequestKt.filter {
                    this.createdAfter = reply0.proofOfDelivery.audit.updatedAt
                    this.ownerId = request.proofOfDeliveryId
                    this.region = request.proofOfDelivery.billingAddress.region
                }
            }
            val reply1 = withTimeout(5_000) { documentsDocumentQueryService.findDocuments(outbound1) }
            val configure2: ExplainProofOfDeliveryCalculationRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.proofOfDelivery = proofOfDelivery {
                    this.bank = proofOfDeliveryBankSettlement {
                        this.accountHolder = request.proofOfDelivery.bank.accountHolder
                    }
                }
            }
            val outbound2 = explainProofOfDeliveryCalculationRequest(configure2)
            val reply2 = deliveriesProofOfDeliveryCalculationService.explainProofOfDeliveryCalculation(outbound2)
            val outbound3 = estimateProofOfDeliveryRequest {
                this.context = request.context
                this.proofOfDelivery = proofOfDelivery {
                    this.counterparty = driverRef {
                        this.email = request.proofOfDelivery.counterparty.email
                    }
                }
            }
            val reply3 = deliveriesProofOfDeliveryCalculationService.estimateProofOfDelivery(outbound3)
            lintProofOfDeliveriesResponse {
                this.outcome = LintProofOfDeliveriesResponseKt.outcome {
                    this.message = reply2.outcome.message
                }
                this.proofOfDelivery = proofOfDelivery {
                    this.billingAddress = reply0.proofOfDelivery.billingAddress
                    this.proofOfDeliveryId = request.proofOfDeliveryId
                    this.status = reply0.proofOfDelivery.status
                    this.total = request.proofOfDelivery.total
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.status = reply3.result.status
                }
            }
        }

    private fun NormalizeProofOfDeliveryRequest.toPreviewYardFromNormalizeProofOfDelivery2(reply0: CompareDeliveryExceptionOptionsResponse): PreviewYardRequest {
        val source = this
        return previewYardRequest {
            this.context = source.context
            this.yard = yard {
                this.audit = auditInfo {
                    this.etag = source.proofOfDelivery.audit.etag
                    this.updatedBy = reply0.deliveryException.audit.updatedBy
                }
                this.owner = userRef {
                    this.displayName = source.proofOfDelivery.counterparty.displayName
                }
            }
        }
    }

    private suspend fun handlePreviewProofOfDelivery(request: PreviewProofOfDeliveryRequest): PreviewProofOfDeliveryResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryValidationService/PreviewProofOfDelivery") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PreviewProofOfDelivery", request)
            log.atInfo().addKeyValue("operation", "PreviewProofOfDelivery").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.proofOfDelivery.proofOfDeliveryId).log("Recorded audit detail")
            val outbound0 = quoteYardMoveRequest {
                this.context = request.context
                this.yardMove = yardMove {
                    this.audit = auditInfo {
                        this.updatedAt = request.proofOfDelivery.audit.updatedAt
                    }
                }
            }
            val reply0 = yardsYardMoveCalculationService.quoteYardMove(outbound0)
            previewProofOfDeliveryResponse {
                this.proofOfDelivery = proofOfDelivery {
                    this.counterparty = request.proofOfDelivery.counterparty
                    this.proofOfDeliveryId = request.proofOfDeliveryId
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleScoreProofOfDelivery(request: ScoreProofOfDeliveryRequest): ScoreProofOfDeliveryResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryValidationService/ScoreProofOfDelivery") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScoreProofOfDelivery", request)
            log.atInfo().addKeyValue("operation", "ScoreProofOfDelivery").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.proofOfDelivery.proofOfDeliveryId).log("Recorded audit detail")
            val outbound0 = compareDeliveryExceptionOptionsRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.audit = auditInfo {
                        this.createdBy = request.proofOfDelivery.audit.createdBy
                        this.updatedBy = request.proofOfDelivery.audit.updatedBy
                    }
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = deliveriesDeliveryExceptionCalculationService.compareDeliveryExceptionOptions(envelope0.unpack(CompareDeliveryExceptionOptionsRequest::class.java))
            val outbound1 = createLedgerAccountRequest {
                this.context = request.context
                this.ledgerAccount = ledgerAccount {
                    this.bank = ledgerAccountBankFunding {
                        this.bic = request.proofOfDelivery.bank.bic
                    }
                    this.contact = contactInfo {
                        this.email = request.proofOfDelivery.counterparty.email
                    }
                }
            }
            val reply1 = ledgerLedgerAccountService.createLedgerAccount(WireCopies.json(outbound1))
            scoreProofOfDeliveryResponse {
                this.breakdown = ScoreProofOfDeliveryResponseKt.breakdown {
                    this.total = request.proofOfDelivery.total
                }
                this.proofOfDelivery = proofOfDelivery {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.deliveryException.audit.updatedAt
                    }
                    this.counterparty = driverRef {
                        this.displayName = reply1.ledgerAccount.holder.displayName
                    }
                    this.proofOfDeliveryId = request.proofOfDeliveryId
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private data class ValidateProofOfDelivery0Command(
        val context: RequestContext,
        val deliveryExceptionRequesterEmail: String,
        val deliveryExceptionAssigneeEmail: String
    ) {
        fun toRequest(): CompareDeliveryExceptionOptionsRequest {
            val command = this
            return compareDeliveryExceptionOptionsRequest {
                this.context = command.context
                this.deliveryException = deliveryException {
                    this.assignee = userRef {
                        this.email = command.deliveryExceptionAssigneeEmail
                    }
                    this.requester = contactInfo {
                        this.email = command.deliveryExceptionRequesterEmail
                    }
                }
            }
        }
    }
}
