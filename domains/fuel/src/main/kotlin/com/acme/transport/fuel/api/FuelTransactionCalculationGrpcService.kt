package com.acme.transport.fuel.api

import com.acme.careplans.v1.CarePlanQueryServiceGrpcKt
import com.acme.careplans.v1.SuggestCarePlansRequestKt
import com.acme.careplans.v1.suggestCarePlansRequest
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.customerRef
import com.acme.deliveries.v1.DeliveryExceptionCalculationServiceGrpcKt
import com.acme.deliveries.v1.DeliveryServiceGrpcKt
import com.acme.deliveries.v1.ProofOfDeliveryValidationServiceGrpcKt
import com.acme.deliveries.v1.copy as deliveriesCopy
import com.acme.deliveries.v1.createDeliveryRequest
import com.acme.deliveries.v1.delivery
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.deliveryExceptionComment
import com.acme.deliveries.v1.normalizeProofOfDeliveryRequest
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.quoteDeliveryExceptionRequest
import com.acme.drivers.v1.driverRef
import com.acme.fuel.v1.CalculateFuelCardResponse
import com.acme.fuel.v1.CalculateFuelTransactionRequest
import com.acme.fuel.v1.CalculateFuelTransactionResponse
import com.acme.fuel.v1.CalculateFuelTransactionResponseKt
import com.acme.fuel.v1.CompareFuelTransactionOptionsRequest
import com.acme.fuel.v1.CompareFuelTransactionOptionsResponse
import com.acme.fuel.v1.CompareFuelTransactionOptionsResponseKt
import com.acme.fuel.v1.EstimateFuelCardRequest
import com.acme.fuel.v1.EstimateFuelCardRequestKt
import com.acme.fuel.v1.EstimateFuelCardResponse
import com.acme.fuel.v1.EstimateFuelTransactionRequest
import com.acme.fuel.v1.EstimateFuelTransactionResponse
import com.acme.fuel.v1.ExplainFuelCardCalculationRequest
import com.acme.fuel.v1.ExplainFuelCardCalculationResponse
import com.acme.fuel.v1.ExplainFuelTransactionCalculationRequest
import com.acme.fuel.v1.ExplainFuelTransactionCalculationResponse
import com.acme.fuel.v1.ExplainFuelTransactionCalculationResponseKt
import com.acme.fuel.v1.FuelCardCalculationServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyHistoryServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionCalculationServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionMeasurement
import com.acme.fuel.v1.GetFuelTransactionRatesRequest
import com.acme.fuel.v1.GetFuelTransactionRatesResponse
import com.acme.fuel.v1.ListFuelPolicyVersionsRequest
import com.acme.fuel.v1.ListFuelPolicyVersionsRequestKt
import com.acme.fuel.v1.ListFuelPolicyVersionsResponse
import com.acme.fuel.v1.QuoteFuelCardRequestKt
import com.acme.fuel.v1.QuoteFuelTransactionRequest
import com.acme.fuel.v1.QuoteFuelTransactionResponse
import com.acme.fuel.v1.RecalculateFuelTransactionsRequest
import com.acme.fuel.v1.RecalculateFuelTransactionsResponse
import com.acme.fuel.v1.RecordFuelPolicyVersionResponse
import com.acme.fuel.v1.SimulateFuelCardRequest
import com.acme.fuel.v1.SimulateFuelCardRequestKt
import com.acme.fuel.v1.SimulateFuelCardResponse
import com.acme.fuel.v1.SimulateFuelTransactionRequest
import com.acme.fuel.v1.SimulateFuelTransactionResponse
import com.acme.fuel.v1.SimulateFuelTransactionResponseKt
import com.acme.fuel.v1.calculateFuelCardRequest
import com.acme.fuel.v1.calculateFuelTransactionResponse
import com.acme.fuel.v1.compareFuelTransactionOptionsResponse
import com.acme.fuel.v1.copy as fuelCopy
import com.acme.fuel.v1.estimateFuelCardRequest
import com.acme.fuel.v1.estimateFuelTransactionResponse
import com.acme.fuel.v1.explainFuelCardCalculationRequest
import com.acme.fuel.v1.explainFuelTransactionCalculationResponse
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.fuelCardBankFunding
import com.acme.fuel.v1.fuelCardCardFunding
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.getFuelTransactionRatesResponse
import com.acme.fuel.v1.listFuelPolicyVersionsRequest
import com.acme.fuel.v1.quoteFuelCardRequest
import com.acme.fuel.v1.quoteFuelTransactionResponse
import com.acme.fuel.v1.recalculateFuelTransactionsResponse
import com.acme.fuel.v1.recordFuelPolicyVersionRequest
import com.acme.fuel.v1.simulateFuelCardRequest
import com.acme.fuel.v1.simulateFuelTransactionResponse
import com.acme.identity.v1.ExplainUserDecisionRequestKt
import com.acme.identity.v1.UserValidationServiceGrpcKt
import com.acme.identity.v1.explainUserDecisionRequest
import com.acme.identity.v1.user
import com.acme.identity.v1.userRef
import com.acme.notifications.v1.CountNotificationsRequestKt
import com.acme.notifications.v1.NotificationQueryServiceGrpcKt
import com.acme.notifications.v1.countNotificationsRequest
import com.acme.prescriptions.v1.EstimatePrescriptionRequest
import com.acme.prescriptions.v1.EstimatePrescriptionResponse
import com.acme.prescriptions.v1.PrescriptionCalculationServiceGrpcKt
import com.acme.prescriptions.v1.PrescriptionServiceGrpcKt
import com.acme.prescriptions.v1.WatchPrescriptionsRequestKt
import com.acme.prescriptions.v1.estimatePrescriptionRequest
import com.acme.prescriptions.v1.prescription
import com.acme.prescriptions.v1.watchPrescriptionsRequest
import com.acme.telehealth.v1.SummarizeVideoVisitsRequestKt
import com.acme.telehealth.v1.VideoVisitQueryServiceGrpcKt
import com.acme.telehealth.v1.summarizeVideoVisitsRequest
import com.acme.transport.fuel.FuelRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.ExplainYardMoveCalculationResponse
import com.acme.yards.v1.FindYardsRequest
import com.acme.yards.v1.FindYardsRequestKt
import com.acme.yards.v1.YardMoveCalculationServiceGrpcKt
import com.acme.yards.v1.YardQueryServiceGrpcKt
import com.acme.yards.v1.explainYardMoveCalculationRequest
import com.acme.yards.v1.findYardsRequest
import com.acme.yards.v1.yardMove
import com.acme.yards.v1.yardMoveSla
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
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

/** Computes the figures of a fuel transaction, such as totals, taxes, and estimates. */
class FuelTransactionCalculationGrpcService(
    private val store: FuelRepository,
    private val workers: CoroutineScope,
    private val careplansCarePlanQueryService: CarePlanQueryServiceGrpcKt.CarePlanQueryServiceCoroutineStub,
    private val deliveriesDeliveryExceptionCalculationService: DeliveryExceptionCalculationServiceGrpcKt.DeliveryExceptionCalculationServiceCoroutineStub,
    private val deliveriesDeliveryService: DeliveryServiceGrpcKt.DeliveryServiceCoroutineStub,
    private val deliveriesProofOfDeliveryValidationService: ProofOfDeliveryValidationServiceGrpcKt.ProofOfDeliveryValidationServiceCoroutineStub,
    private val fuelFuelCardCalculationService: FuelCardCalculationServiceGrpcKt.FuelCardCalculationServiceCoroutineStub,
    private val fuelFuelPolicyHistoryService: FuelPolicyHistoryServiceGrpcKt.FuelPolicyHistoryServiceCoroutineStub,
    private val identityUserValidationService: UserValidationServiceGrpcKt.UserValidationServiceCoroutineStub,
    private val notificationsNotificationQueryService: NotificationQueryServiceGrpcKt.NotificationQueryServiceCoroutineStub,
    private val prescriptionsPrescriptionCalculationService: PrescriptionCalculationServiceGrpcKt.PrescriptionCalculationServiceCoroutineStub,
    private val prescriptionsPrescriptionService: PrescriptionServiceGrpcKt.PrescriptionServiceCoroutineStub,
    private val telehealthVideoVisitQueryService: VideoVisitQueryServiceGrpcKt.VideoVisitQueryServiceCoroutineStub,
    private val yardsYardMoveCalculationService: YardMoveCalculationServiceGrpcKt.YardMoveCalculationServiceCoroutineStub,
    private val yardsYardQueryService: YardQueryServiceGrpcKt.YardQueryServiceCoroutineStub,
) : FuelTransactionCalculationServiceGrpcKt.FuelTransactionCalculationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(FuelTransactionCalculationGrpcService::class.java)

    private val invokeExplainFuelTransactionCalculation2: suspend (SimulateFuelCardRequest) -> SimulateFuelCardResponse = { payload ->
        fuelFuelCardCalculationService.simulateFuelCard(payload)
    }

    private val workerForQuoteFuelTransaction3 = ReplyMailbox<ListFuelPolicyVersionsRequest, ListFuelPolicyVersionsResponse>(workers) { payload ->
        withTimeout(5_000) { fuelFuelPolicyHistoryService.listFuelPolicyVersions(payload) }
    }

    private val clientForRecalculateFuelTransactions0 by lazy { fuelFuelCardCalculationService }

    /** Calculates a fuel transaction. */
    override suspend fun calculateFuelTransaction(request: CalculateFuelTransactionRequest): CalculateFuelTransactionResponse {
        return handleCalculateFuelTransaction(request)
    }

    /** Compares a fuel transaction. */
    override suspend fun compareFuelTransactionOptions(request: CompareFuelTransactionOptionsRequest): CompareFuelTransactionOptionsResponse = handleCompareFuelTransactionOptions(request)

    /** Estimates a fuel transaction. */
    override suspend fun estimateFuelTransaction(request: EstimateFuelTransactionRequest): EstimateFuelTransactionResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionCalculationService/EstimateFuelTransaction") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EstimateFuelTransaction", request)
            log.atInfo().addKeyValue("operation", "EstimateFuelTransaction").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransaction.fuelTransactionId).log("Recorded audit detail")
            var reply0 = ExplainFuelCardCalculationResponse.getDefaultInstance()
            request.fuelTransaction.measurementsList.asFlow().map { item ->
                val outbound0 = explainFuelCardCalculationRequest {
                    this.context = request.context
                    this.fuelCard = fuelCard {
                        this.currencyCode = request.details.currencyCode
                        this.holder = driverRef {
                            this.displayName = request.fuelTransaction.subject.displayName
                            this.email = request.fuelTransaction.subject.email
                        }
                        this.openedAt = item.observedAt
                    }
                }
                fuelFuelCardCalculationService.explainFuelCardCalculation(outbound0)
            }.collect { reply0 = it }
            val outbound1 = findYardsRequest {
                this.context = request.context
                this.filter = FindYardsRequestKt.filter {
                    this.labels.putAll(reply0.fuelCard.labelsMap)
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = yardsYardQueryService.findYards(envelope1.unpack(FindYardsRequest::class.java))
            val outbound2 = suggestCarePlansRequest {
                this.context = request.context
                this.filter = SuggestCarePlansRequestKt.filter {
                    this.labels.putAll(reply0.fuelCard.labelsMap)
                    this.region = request.details.destination.region
                }
            }
            val reply2 = careplansCarePlanQueryService.suggestCarePlans(WireCopies.json(outbound2))
            val outbound3 = watchPrescriptionsRequest {
                this.context = request.context
                this.filter = WatchPrescriptionsRequestKt.filter {
                    this.region = request.details.destination.region
                }
                this.subscriber = contactInfo {
                    this.email = request.fuelTransaction.subject.email
                    this.phone = reply0.fuelCard.contact.phone
                    this.preferredChannel = reply0.fuelCard.contact.preferredChannel
                }
            }
            prescriptionsPrescriptionService.watchPrescriptions(outbound3).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            estimateFuelTransactionResponse {
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                    this.subject = request.fuelTransaction.subject
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.referenceIds.addAll(reply2.result.referenceIdsList)
                    this.status = reply1.result.status
                }
            }
        }

    /** Explains a fuel transaction. */
    override suspend fun explainFuelTransactionCalculation(request: ExplainFuelTransactionCalculationRequest): ExplainFuelTransactionCalculationResponse {
        return handleExplainFuelTransactionCalculation(request)
    }

    /** Returns a fuel transaction. */
    override suspend fun getFuelTransactionRates(request: GetFuelTransactionRatesRequest): GetFuelTransactionRatesResponse = handleGetFuelTransactionRates(request)

    /** Quotes a fuel transaction. */
    override suspend fun quoteFuelTransaction(request: QuoteFuelTransactionRequest): QuoteFuelTransactionResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionCalculationService/QuoteFuelTransaction") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "QuoteFuelTransaction", request)
            log.atInfo().addKeyValue("operation", "QuoteFuelTransaction").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransaction.fuelTransactionId).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                explainFuelCardCalculationRequest {
                    this.context = request.context
                    this.fuelCard = fuelCard {
                        this.holder = driverRef {
                            this.displayName = request.fuelTransaction.subject.displayName
                            this.email = request.fuelTransaction.subject.email
                        }
                    }
                }
            }
            val reply0 = fuelFuelCardCalculationService.explainFuelCardCalculation(outbound0)
            val outbound1 = quoteDeliveryExceptionRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.comments.addAll(listOf(deliveryExceptionComment {
                        this.createdAt = reply0.fuelCard.audit.createdAt
                    }))
                    this.requester = contactInfo {
                        this.email = request.fuelTransaction.subject.email
                        this.name = reply0.fuelCard.contact.name
                    }
                }
            }
            val reply1 = ClientRegistry.lookup("acme.deliveries.v1.DeliveryExceptionCalculationService", DeliveryExceptionCalculationServiceGrpcKt.DeliveryExceptionCalculationServiceCoroutineStub::class.java).quoteDeliveryException(outbound1)
            val outbound2 = quoteFuelCardRequest {
                this.context = request.context
                this.details = QuoteFuelCardRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.details.destination.region
                    }
                }
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.fuelCard.audit.updatedAt
                        this.updatedBy = reply1.deliveryException.audit.updatedBy
                    }
                    this.bank = fuelCardBankFunding {
                        this.accountHolder = reply0.fuelCard.bank.accountHolder
                    }
                }
            }
            val reply2 = fuelFuelCardCalculationService.quoteFuelCard(WireCopies.reflective(outbound2))
            val outbound3 = listFuelPolicyVersionsRequest {
                this.context = request.context
                this.filter = ListFuelPolicyVersionsRequestKt.filter {
                    this.labels.putAll(reply2.fuelCard.labelsMap)
                    this.region = request.details.destination.region
                }
            }
            val reply3 = workerForQuoteFuelTransaction3.submit(outbound3)
            quoteFuelTransactionResponse {
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                    this.kind = request.fuelTransaction.kind
                    this.subject = customerRef {
                        this.displayName = reply1.deliveryException.customer.displayName
                    }
                }
                this.result = operationResult {
                    this.message = reply2.result.message
                    this.status = reply0.result.status
                }
            }
        }

    /** Recalculates a fuel transaction. */
    override suspend fun recalculateFuelTransactions(request: RecalculateFuelTransactionsRequest): RecalculateFuelTransactionsResponse {
        return handleRecalculateFuelTransactions(request)
    }

    /** Simulates a fuel transaction. */
    override suspend fun simulateFuelTransaction(request: SimulateFuelTransactionRequest): SimulateFuelTransactionResponse = handleSimulateFuelTransaction(request)

    private suspend fun handleCalculateFuelTransaction(request: CalculateFuelTransactionRequest): CalculateFuelTransactionResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionCalculationService/CalculateFuelTransaction") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CalculateFuelTransaction", request)
            log.atInfo().addKeyValue("operation", "CalculateFuelTransaction").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransaction.fuelTransactionId).log("Recorded audit detail")
            val outbound0 = explainUserDecisionRequest {
                this.context = request.context
                this.details = ExplainUserDecisionRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.fuelTransaction.subject.email
                    }
                }
                this.user = user {
                    this.primaryAddress = postalAddress {
                        this.locality = request.details.destination.locality
                        this.postalCode = request.details.destination.postalCode
                    }
                    this.relationshipManager = userRef {
                        this.displayName = request.fuelTransaction.subject.displayName
                    }
                }
            }
            val reply0 = identityUserValidationService.explainUserDecision(outbound0)
            val outbound1 = quoteFuelCardRequest {
                this.context = request.context
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.user.audit.updatedAt
                    }
                    this.holder = driverRef {
                        this.displayName = reply0.user.relationshipManager.displayName
                        this.email = reply0.user.relationshipManager.email
                    }
                }
            }
            val reply1 = fuelFuelCardCalculationService.quoteFuelCard(outbound1)
            calculateFuelTransactionResponse {
                this.breakdown = CalculateFuelTransactionResponseKt.breakdown {
                    this.total = reply1.breakdown.total
                }
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                    this.occurredAt = request.fuelTransaction.occurredAt
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = reply0.outcome.status
                }
            }
        }

    private fun CompareFuelTransactionOptionsRequest.toEstimateFuelCardFromCompareFuelTransactionOptions1(reply0: RecordFuelPolicyVersionResponse): EstimateFuelCardRequest {
        val source = this
        return estimateFuelCardRequest {
            this.context = source.context
            this.details = EstimateFuelCardRequestKt.details {
                this.destination = postalAddress {
                    this.countryCode = source.details.destination.countryCode
                    this.locality = source.details.destination.locality
                }
            }
            this.fuelCard = fuelCard {
                this.audit = auditInfo {
                    this.createdBy = reply0.fuelPolicy.audit.createdBy
                }
                this.holder = driverRef {
                    this.displayName = source.fuelTransaction.subject.displayName
                }
            }
        }
    }

    private suspend fun handleCompareFuelTransactionOptions(request: CompareFuelTransactionOptionsRequest): CompareFuelTransactionOptionsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionCalculationService/CompareFuelTransactionOptions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CompareFuelTransactionOptions", request)
            log.atInfo().addKeyValue("operation", "CompareFuelTransactionOptions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransaction.fuelTransactionId).log("Recorded audit detail")
            val outbound0 = recordFuelPolicyVersionRequest {}.fuelCopy {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.owner = userRef {
                        this.displayName = request.fuelTransaction.subject.displayName
                        this.email = request.fuelTransaction.subject.email
                    }
                }
            }
            val reply0 = fuelFuelPolicyHistoryService.recordFuelPolicyVersion(outbound0)
            val outbound1 = request.toEstimateFuelCardFromCompareFuelTransactionOptions1(reply0)
            val reply1 = fuelFuelCardCalculationService.estimateFuelCard(outbound1)
            compareFuelTransactionOptionsResponse {
                this.breakdown = CompareFuelTransactionOptionsResponseKt.breakdown {
                    this.currencyCode = reply1.fuelCard.currencyCode
                }
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                    this.subject = request.fuelTransaction.subject
                }
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleExplainFuelTransactionCalculation(request: ExplainFuelTransactionCalculationRequest): ExplainFuelTransactionCalculationResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionCalculationService/ExplainFuelTransactionCalculation") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainFuelTransactionCalculation", request)
            log.atInfo().addKeyValue("operation", "ExplainFuelTransactionCalculation").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransaction.fuelTransactionId).log("Recorded audit detail")
            var reply0 = CalculateFuelCardResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<FuelTransactionMeasurement>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound0 = calculateFuelCardRequest {
                            this.context = request.context
                            this.fuelCard = fuelCard {
                                this.audit = auditInfo {
                                    this.updatedAt = item.observedAt
                                }
                                this.contact = contactInfo {
                                    this.email = request.fuelTransaction.subject.email
                                    this.preferredChannel = request.details.contact.preferredChannel
                                }
                            }
                        }
                        reply0 = fuelFuelCardCalculationService.calculateFuelCard(outbound0)
                    }
                }
                try {
                    request.fuelTransaction.measurementsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound1 = estimatePrescriptionRequest {
                this.context = request.context
                this.prescription = prescription {
                    this.audit = auditInfo {
                        this.createdAt = reply0.fuelCard.audit.createdAt
                    }
                }
            }
            val adapter1 = OutboundPort<EstimatePrescriptionRequest, EstimatePrescriptionResponse> { payload ->
                prescriptionsPrescriptionCalculationService.estimatePrescription(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            val outbound2 = simulateFuelCardRequest {
                this.context = request.context
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.fuelCard.audit.updatedAt
                    }
                    this.card = fuelCardCardFunding {
                        this.last4 = reply0.fuelCard.card.last4
                        this.network = reply0.fuelCard.card.network
                    }
                }
            }
            val reply2 = invokeExplainFuelTransactionCalculation2(outbound2)
            explainFuelTransactionCalculationResponse {
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                    this.sequence = request.fuelTransaction.sequence
                }
                this.outcome = ExplainFuelTransactionCalculationResponseKt.outcome {
                    this.status = reply2.result.status
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.details.putAll(reply2.result.detailsMap)
                    this.status = reply1.result.status
                }
            }
        }

    private suspend fun handleGetFuelTransactionRates(request: GetFuelTransactionRatesRequest): GetFuelTransactionRatesResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionCalculationService/GetFuelTransactionRates") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetFuelTransactionRates", request)
            log.atInfo().addKeyValue("operation", "GetFuelTransactionRates").log("Handling transport request")
            val outbound0 = estimateFuelCardRequest {
                this.context = request.context
                this.fuelCard = fuelCard {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                    this.externalAccountId = request.fuelTransactionId
                }
            }
            val invoke0: suspend (EstimateFuelCardRequest, Metadata) -> EstimateFuelCardResponse = fuelFuelCardCalculationService::estimateFuelCard
            val reply0 = invoke0(outbound0, Metadata())
            val outbound1 = ExplainFuelCardCalculationRequest.newBuilder().apply {
                setContext(request.context)
                setFuelCard(fuelCard {
                    this.audit = auditInfo {
                        this.createdAt = reply0.fuelCard.audit.createdAt
                        this.etag = reply0.fuelCard.audit.etag
                    }
                    this.card = fuelCardCardFunding {
                        this.token = reply0.fuelCard.card.token
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> fuelFuelCardCalculationService.explainFuelCardCalculation(value) }
            getFuelTransactionRatesResponse {
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                    this.subject = customerRef {
                        this.displayName = reply0.fuelCard.holder.displayName
                    }
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleRecalculateFuelTransactions(request: RecalculateFuelTransactionsRequest): RecalculateFuelTransactionsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionCalculationService/RecalculateFuelTransactions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecalculateFuelTransactions", request)
            log.atInfo().addKeyValue("operation", "RecalculateFuelTransactions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransaction.fuelTransactionId).log("Recorded audit detail")
            val outbound0 = simulateFuelCardRequest {
                this.context = request.context
                this.details = SimulateFuelCardRequestKt.details {
                    this.destination = postalAddress {
                        this.postalCode = request.details.destination.postalCode
                    }
                }
                this.fuelCard = fuelCard {
                    this.contact = contactInfo {
                        this.email = request.fuelTransaction.subject.email
                    }
                    this.holder = driverRef {
                        this.displayName = request.fuelTransaction.subject.displayName
                    }
                }
            }
            val reply0 = clientForRecalculateFuelTransactions0.simulateFuelCard(outbound0)
            val outbound1 = countNotificationsRequest {
                this.context = request.context
                this.filter = CountNotificationsRequestKt.filter {
                    this.region = request.details.destination.region
                }
            }
            val reply1 = flowOf(outbound1).map { value -> notificationsNotificationQueryService.countNotifications(value) }.single()
            var reply2 = ExplainYardMoveCalculationResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.fuelTransaction.measurementsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound2 = explainYardMoveCalculationRequest {
                                this.context = request.context
                                this.yardMove = yardMove {
                                    this.assignee = userRef {
                                        this.displayName = reply0.fuelCard.holder.displayName
                                    }
                                    this.audit = auditInfo {
                                        this.createdBy = reply0.fuelCard.audit.createdBy
                                    }
                                    this.sla = yardMoveSla {
                                        this.dueAt = item.observedAt
                                    }
                                }
                            }
                            yardsYardMoveCalculationService.explainYardMoveCalculation(outbound2)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply2 = it }
                }
            }
            recalculateFuelTransactionsResponse {
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                    this.sequence = request.fuelTransaction.sequence
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSimulateFuelTransaction(request: SimulateFuelTransactionRequest): SimulateFuelTransactionResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionCalculationService/SimulateFuelTransaction") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SimulateFuelTransaction", request)
            log.atInfo().addKeyValue("operation", "SimulateFuelTransaction").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransaction.fuelTransactionId).log("Recorded audit detail")
            val configure0: EstimateFuelCardRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.details = EstimateFuelCardRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = request.details.destination.countryCode
                    }
                    this.scenarioIds.addAll(request.details.scenarioIdsList)
                }
            }
            val outbound0 = estimateFuelCardRequest(configure0)
            val reply0 = fuelFuelCardCalculationService.estimateFuelCard(outbound0)
            val outbound1 = summarizeVideoVisitsRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.details = SummarizeVideoVisitsRequestKt.details {
                    this.filters.putAll(request.fuelTransaction.attributesMap)
                }
                this.reason = request.reason
            }
            val reply1 = telehealthVideoVisitQueryService.summarizeVideoVisits(outbound1)
            val outbound2 = createDeliveryRequest {
                this.context = request.context
                this.delivery = delivery {
                    this.billingAddress = postalAddress {
                        this.region = request.details.destination.region
                    }
                }
            }
            val reply2 = deliveriesDeliveryService.createDelivery(outbound2)
            val outbound3 = normalizeProofOfDeliveryRequest {}.deliveriesCopy {
                this.context = request.context
                this.proofOfDelivery = proofOfDelivery {
                    this.audit = auditInfo {
                        this.etag = reply2.delivery.audit.etag
                        this.updatedBy = reply0.fuelCard.audit.updatedBy
                    }
                    this.billingAddress = postalAddress {
                        this.locality = request.details.destination.locality
                    }
                }
            }
            val reply3 = deliveriesProofOfDeliveryValidationService.normalizeProofOfDelivery(outbound3)
            simulateFuelTransactionResponse {
                this.breakdown = SimulateFuelTransactionResponseKt.breakdown {
                    this.total = reply0.breakdown.total
                }
                this.fuelTransaction = fuelTransaction {
                    this.attributes.putAll(request.fuelTransaction.attributesMap)
                    this.correlationId = reply1.result.correlationId
                    this.fuelTransactionId = request.fuelTransactionId
                }
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.message = reply3.outcome.message
                    this.status = reply1.result.status
                }
            }
        }
}
