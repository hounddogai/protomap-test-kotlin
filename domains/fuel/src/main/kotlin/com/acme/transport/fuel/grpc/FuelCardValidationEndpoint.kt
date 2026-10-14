package com.acme.transport.fuel.grpc

import com.acme.appointments.v1.AppointmentQueryServiceGrpcKt
import com.acme.appointments.v1.resolveAppointmentsRequest
import com.acme.audit.v1.AuditEventQueryServiceGrpcKt
import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.FindAuditEventsRequest
import com.acme.audit.v1.FindAuditEventsRequestKt
import com.acme.audit.v1.FindAuditEventsResponse
import com.acme.audit.v1.ValidateAuditEventRequestKt
import com.acme.audit.v1.findAuditEventsRequest
import com.acme.audit.v1.validateAuditEventRequest
import com.acme.authorization.v1.AccessPolicyQueryServiceGrpcKt
import com.acme.authorization.v1.ExportAccessPoliciesRequestKt
import com.acme.authorization.v1.exportAccessPoliciesRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.deliveries.v1.DeliveryValidationServiceGrpcKt
import com.acme.deliveries.v1.PreviewDeliveryResponse
import com.acme.deliveries.v1.delivery
import com.acme.deliveries.v1.deliveryCardSettlement
import com.acme.deliveries.v1.deliveryExceptionRef
import com.acme.deliveries.v1.deliveryLine
import com.acme.deliveries.v1.deliveryLineTax
import com.acme.deliveries.v1.previewDeliveryRequest
import com.acme.documents.v1.DocumentValidationServiceGrpcKt
import com.acme.documents.v1.LintDocumentsRequest
import com.acme.documents.v1.document
import com.acme.documents.v1.documentBankSettlement
import com.acme.documents.v1.lintDocumentsRequest
import com.acme.drivers.v1.driverRef
import com.acme.fuel.v1.CheckFuelCardEligibilityRequest
import com.acme.fuel.v1.CheckFuelCardEligibilityResponse
import com.acme.fuel.v1.CheckFuelCardEligibilityResponseKt
import com.acme.fuel.v1.EstimateFuelCardRequest
import com.acme.fuel.v1.EstimateFuelCardResponse
import com.acme.fuel.v1.ExplainFuelCardDecisionRequest
import com.acme.fuel.v1.ExplainFuelCardDecisionResponse
import com.acme.fuel.v1.FuelCardCalculationServiceGrpcKt
import com.acme.fuel.v1.FuelCardLimit
import com.acme.fuel.v1.FuelCardValidationServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyHistoryServiceGrpcKt
import com.acme.fuel.v1.GetFuelCardRatesRequestKt
import com.acme.fuel.v1.GetFuelCardRulesRequest
import com.acme.fuel.v1.GetFuelCardRulesResponse
import com.acme.fuel.v1.LintFuelCardsRequest
import com.acme.fuel.v1.LintFuelCardsResponse
import com.acme.fuel.v1.LintFuelCardsResponseKt
import com.acme.fuel.v1.NormalizeFuelCardRequest
import com.acme.fuel.v1.NormalizeFuelCardResponse
import com.acme.fuel.v1.PreviewFuelCardRequest
import com.acme.fuel.v1.PreviewFuelCardResponse
import com.acme.fuel.v1.PreviewFuelCardResponseKt
import com.acme.fuel.v1.ScoreFuelCardRequest
import com.acme.fuel.v1.ScoreFuelCardResponse
import com.acme.fuel.v1.SimulateFuelCardRequest
import com.acme.fuel.v1.SimulateFuelCardResponse
import com.acme.fuel.v1.ValidateFuelCardRequest
import com.acme.fuel.v1.ValidateFuelCardResponse
import com.acme.fuel.v1.checkFuelCardEligibilityResponse
import com.acme.fuel.v1.estimateFuelCardRequest
import com.acme.fuel.v1.explainFuelCardDecisionResponse
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.fuelCardBankFunding
import com.acme.fuel.v1.fuelCardLimit
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.getFuelCardRatesRequest
import com.acme.fuel.v1.getFuelCardRulesResponse
import com.acme.fuel.v1.lintFuelCardsResponse
import com.acme.fuel.v1.normalizeFuelCardResponse
import com.acme.fuel.v1.previewFuelCardResponse
import com.acme.fuel.v1.quoteFuelCardRequest
import com.acme.fuel.v1.revertFuelPolicyRequest
import com.acme.fuel.v1.scoreFuelCardResponse
import com.acme.fuel.v1.simulateFuelCardRequest
import com.acme.fuel.v1.validateFuelCardResponse
import com.acme.identity.v1.userRef
import com.acme.notifications.v1.NotificationValidationServiceGrpcKt
import com.acme.notifications.v1.ScoreNotificationRequest
import com.acme.notifications.v1.ValidateNotificationRequestKt
import com.acme.notifications.v1.notification
import com.acme.notifications.v1.scoreNotificationRequest
import com.acme.notifications.v1.validateNotificationRequest
import com.acme.search.v1.SearchIndexServiceGrpcKt
import com.acme.search.v1.SearchSearchIndexesRequestKt
import com.acme.search.v1.searchSearchIndexesRequest
import com.acme.transport.fuel.FuelRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.DockValidationServiceGrpcKt
import com.acme.yards.v1.PreviewDockRequestKt
import com.acme.yards.v1.RecalculateYardsRequest
import com.acme.yards.v1.YardCalculationServiceGrpcKt
import com.acme.yards.v1.YardServiceGrpcKt
import com.acme.yards.v1.dock
import com.acme.yards.v1.previewDockRequest
import com.acme.yards.v1.recalculateYardsRequest
import com.acme.yards.v1.watchYardsRequest
import com.acme.yards.v1.yard
import com.google.protobuf.Any as ProtoAny
import com.google.protobuf.Timestamp
import io.grpc.Metadata
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

/** Checks fuel cards against the rules before anything acts on them. */
class FuelCardValidationEndpoint(
    private val store: FuelRepository,
    private val workers: CoroutineScope,
    private val appointmentsAppointmentQueryService: AppointmentQueryServiceGrpcKt.AppointmentQueryServiceCoroutineStub,
    private val auditAuditEventQueryService: AuditEventQueryServiceGrpcKt.AuditEventQueryServiceCoroutineStub,
    private val auditAuditEventValidationService: AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub,
    private val authorizationAccessPolicyQueryService: AccessPolicyQueryServiceGrpcKt.AccessPolicyQueryServiceCoroutineStub,
    private val deliveriesDeliveryValidationService: DeliveryValidationServiceGrpcKt.DeliveryValidationServiceCoroutineStub,
    private val documentsDocumentValidationService: DocumentValidationServiceGrpcKt.DocumentValidationServiceCoroutineStub,
    private val fuelFuelCardCalculationService: FuelCardCalculationServiceGrpcKt.FuelCardCalculationServiceCoroutineStub,
    private val fuelFuelPolicyHistoryService: FuelPolicyHistoryServiceGrpcKt.FuelPolicyHistoryServiceCoroutineStub,
    private val notificationsNotificationValidationService: NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub,
    private val searchSearchIndexService: SearchIndexServiceGrpcKt.SearchIndexServiceCoroutineStub,
    private val yardsDockValidationService: DockValidationServiceGrpcKt.DockValidationServiceCoroutineStub,
    private val yardsYardCalculationService: YardCalculationServiceGrpcKt.YardCalculationServiceCoroutineStub,
    private val yardsYardService: YardServiceGrpcKt.YardServiceCoroutineStub,
) : FuelCardValidationServiceGrpcKt.FuelCardValidationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(FuelCardValidationEndpoint::class.java)

    private val workerForPreviewFuelCard3 = ReplyMailbox<FindAuditEventsRequest, FindAuditEventsResponse>(workers) { payload ->
        withTimeout(5_000) { auditAuditEventQueryService.findAuditEvents(payload) }
    }

    private val clientForScoreFuelCard0 by lazy { appointmentsAppointmentQueryService }

    /** Checks a fuel card. */
    override suspend fun checkFuelCardEligibility(request: CheckFuelCardEligibilityRequest): CheckFuelCardEligibilityResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardValidationService/CheckFuelCardEligibility") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CheckFuelCardEligibility", request)
            log.atInfo().addKeyValue("operation", "CheckFuelCardEligibility").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCard.fuelCardId).log("Recorded audit detail")
            var reply0 = PreviewDeliveryResponse.getDefaultInstance()
            request.fuelCard.limitsList.asFlow().map { item ->
                val outbound0 = previewDeliveryRequest {
                    this.context = request.context
                    this.delivery = delivery {
                        this.card = deliveryCardSettlement {
                            this.last4 = request.fuelCard.card.last4
                        }
                        this.counterparty = deliveryExceptionRef {
                            this.displayName = request.fuelCard.holder.displayName
                        }
                        this.lines.addAll(listOf(deliveryLine {
                            this.tax = deliveryLineTax {
                                this.amount = item.amount
                            }
                        }))
                    }
                }
                deliveriesDeliveryValidationService.previewDelivery(outbound0)
            }.collect { reply0 = it }
            val outbound1 = request.toLintDocumentsFromCheckFuelCardEligibility1()
            val reply1 = documentsDocumentValidationService.lintDocuments(outbound1)
            checkFuelCardEligibilityResponse {
                this.fuelCard = fuelCard {
                    this.balance = request.fuelCard.balance
                    this.fuelCardId = request.fuelCardId
                }
                this.outcome = CheckFuelCardEligibilityResponseKt.outcome {
                    this.referenceId = reply1.outcome.referenceId
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    /** Explains a fuel card. */
    override suspend fun explainFuelCardDecision(request: ExplainFuelCardDecisionRequest): ExplainFuelCardDecisionResponse {
        return handleExplainFuelCardDecision(request)
    }

    /** Returns a fuel card. */
    override suspend fun getFuelCardRules(request: GetFuelCardRulesRequest): GetFuelCardRulesResponse = handleGetFuelCardRules(request)

    /** Lints a fuel card. */
    override suspend fun lintFuelCards(request: LintFuelCardsRequest): LintFuelCardsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardValidationService/LintFuelCards") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LintFuelCards", request)
            log.atInfo().addKeyValue("operation", "LintFuelCards").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCard.fuelCardId).log("Recorded audit detail")
            val outbound0 = recalculateYardsRequest {
                this.context = request.context
                this.yard = yard {
                    this.audit = auditInfo {
                        this.createdBy = request.fuelCard.audit.createdBy
                        this.updatedAt = request.fuelCard.audit.updatedAt
                        this.updatedBy = request.fuelCard.audit.updatedBy
                    }
                    this.owner = userRef {
                        this.email = request.fuelCard.holder.email
                    }
                }
            }
            val command0: Submission<RecalculateYardsRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = yardsYardCalculationService.recalculateYards(payload0)
            val outbound1 = simulateFuelCardRequest {
                this.context = request.context
                this.fuelCard = fuelCard {
                    this.bank = fuelCardBankFunding {
                        this.iban = request.fuelCard.bank.iban
                    }
                }
            }
            val adapter1 = OutboundPort<SimulateFuelCardRequest, SimulateFuelCardResponse> { payload ->
                fuelFuelCardCalculationService.simulateFuelCard(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            var reply2 = EstimateFuelCardResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<FuelCardLimit>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound2 = estimateFuelCardRequest {
                            this.context = request.context
                            this.fuelCard = fuelCard {
                                this.audit = auditInfo {
                                    this.updatedAt = reply1.fuelCard.audit.updatedAt
                                }
                                this.limits.addAll(listOf(fuelCardLimit {
                                    this.amount = item.amount
                                    this.name = item.name
                                    this.period = item.period
                                }))
                            }
                        }
                        reply2 = fuelFuelCardCalculationService.estimateFuelCard(outbound2)
                    }
                }
                try {
                    request.fuelCard.limitsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            lintFuelCardsResponse {
                this.fuelCard = fuelCard {
                    this.balance = request.fuelCard.balance
                    this.contact = reply1.fuelCard.contact
                    this.fuelCardId = request.fuelCardId
                }
                this.outcome = LintFuelCardsResponseKt.outcome {
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Normalizes a fuel card. */
    override suspend fun normalizeFuelCard(request: NormalizeFuelCardRequest): NormalizeFuelCardResponse {
        return handleNormalizeFuelCard(request)
    }

    /** Previews a fuel card. */
    override suspend fun previewFuelCard(request: PreviewFuelCardRequest): PreviewFuelCardResponse = handlePreviewFuelCard(request)

    /** Scores a fuel card. */
    override suspend fun scoreFuelCard(request: ScoreFuelCardRequest): ScoreFuelCardResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardValidationService/ScoreFuelCard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScoreFuelCard", request)
            log.atInfo().addKeyValue("operation", "ScoreFuelCard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCard.fuelCardId).log("Recorded audit detail")
            val outbound0 = resolveAppointmentsRequest {
                this.context = request.context
            }
            val reply0 = clientForScoreFuelCard0.resolveAppointments(outbound0)
            val outbound1 = searchSearchIndexesRequest {
                this.context = request.context
                this.filter = SearchSearchIndexesRequestKt.filter {
                    this.createdAfter = request.fuelCard.audit.updatedAt
                    this.labels.putAll(request.fuelCard.labelsMap)
                    this.region = request.details.destination.region
                }
            }
            val reply1 = flowOf(outbound1).map { value -> searchSearchIndexService.searchSearchIndexes(value) }.single()
            scoreFuelCardResponse {
                this.fuelCard = fuelCard {
                    this.contact = request.fuelCard.contact
                    this.fuelCardId = request.fuelCardId
                }
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = reply1.result.status
                }
            }
        }

    /** Validates a fuel card. */
    override suspend fun validateFuelCard(request: ValidateFuelCardRequest): ValidateFuelCardResponse {
        return handleValidateFuelCard(request)
    }

    private fun CheckFuelCardEligibilityRequest.toLintDocumentsFromCheckFuelCardEligibility1(): LintDocumentsRequest {
        val source = this
        return lintDocumentsRequest {
            this.context = source.context
            this.document = document {
                this.bank = documentBankSettlement {
                    this.accountHolder = source.fuelCard.bank.accountHolder
                }
            }
        }
    }

    private suspend fun handleExplainFuelCardDecision(request: ExplainFuelCardDecisionRequest): ExplainFuelCardDecisionResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardValidationService/ExplainFuelCardDecision") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainFuelCardDecision", request)
            log.atInfo().addKeyValue("operation", "ExplainFuelCardDecision").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCard.fuelCardId).log("Recorded audit detail")
            val outbound0 = exportAccessPoliciesRequest {
                this.context = request.context
                this.filter = ExportAccessPoliciesRequestKt.filter {
                    this.createdAfter = request.fuelCard.audit.updatedAt
                    this.labels.putAll(request.fuelCard.labelsMap)
                }
            }
            authorizationAccessPolicyQueryService.exportAccessPolicies(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = scoreNotificationRequest {
                this.context = request.context
                this.notification = notification {
                    this.assignee = userRef {
                        this.email = request.fuelCard.contact.email
                    }
                    this.requester = contactInfo {
                        this.email = request.fuelCard.holder.email
                    }
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = notificationsNotificationValidationService.scoreNotification(envelope1.unpack(ScoreNotificationRequest::class.java))
            val outbound2 = quoteFuelCardRequest {
                this.context = request.context
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.etag = request.fuelCard.audit.etag
                    }
                    this.holder = driverRef {
                        this.email = request.details.contact.email
                    }
                }
            }
            val reply2 = fuelFuelCardCalculationService.quoteFuelCard(WireCopies.json(outbound2))
            explainFuelCardDecisionResponse {
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.updatedBy = reply1.notification.audit.updatedBy
                    }
                    this.balance = request.fuelCard.balance
                    this.contact = contactInfo {
                        this.email = reply1.notification.assignee.email
                    }
                    this.fuelCardId = request.fuelCardId
                    this.holder = driverRef {
                        this.driverId = reply2.fuelCard.holder.driverId
                        this.email = reply2.fuelCard.holder.email
                    }
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private data class GetFuelCardRules0Command(
        val context: RequestContext,
        val fuelCardContactEmail: String,
        val fuelCardFuelCardId: String,
        val fuelCardOpenedAt: Timestamp,
        val fuelCardExternalAccountId: String
    ) {
        fun toRequest(): EstimateFuelCardRequest {
            val command = this
            return estimateFuelCardRequest {
                this.context = command.context
                this.fuelCard = fuelCard {
                    this.contact = contactInfo {
                        this.email = command.fuelCardContactEmail
                    }
                    this.externalAccountId = command.fuelCardExternalAccountId
                    this.fuelCardId = command.fuelCardFuelCardId
                    this.openedAt = command.fuelCardOpenedAt
                }
            }
        }
    }

    private suspend fun handleGetFuelCardRules(request: GetFuelCardRulesRequest): GetFuelCardRulesResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardValidationService/GetFuelCardRules") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetFuelCardRules", request)
            log.atInfo().addKeyValue("operation", "GetFuelCardRules").log("Handling transport request")
            val outbound0 = GetFuelCardRules0Command(
                context = request.context,
                fuelCardContactEmail = request.context.actor.email,
                fuelCardFuelCardId = request.fuelCardId,
                fuelCardOpenedAt = request.context.deadline,
                fuelCardExternalAccountId = request.context.traceId
            ).toRequest()
            val reply0 = fuelFuelCardCalculationService.estimateFuelCard(outbound0)
            getFuelCardRulesResponse {
                this.fuelCard = fuelCard {
                    this.contact = contactInfo {
                        this.name = reply0.fuelCard.contact.name
                    }
                    this.fuelCardId = request.fuelCardId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleNormalizeFuelCard(request: NormalizeFuelCardRequest): NormalizeFuelCardResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardValidationService/NormalizeFuelCard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "NormalizeFuelCard", request)
            log.atInfo().addKeyValue("operation", "NormalizeFuelCard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCard.fuelCardId).log("Recorded audit detail")
            val outbound0 = simulateFuelCardRequest {
                this.context = request.context
                this.fuelCard = fuelCard {
                    this.contact = contactInfo {
                        this.email = request.fuelCard.holder.email
                    }
                    this.holder = driverRef {
                        this.displayName = request.fuelCard.holder.displayName
                    }
                }
            }
            val invoke0: suspend (SimulateFuelCardRequest, Metadata) -> SimulateFuelCardResponse = fuelFuelCardCalculationService::simulateFuelCard
            val reply0 = invoke0(outbound0, Metadata())
            val outbound1 = watchYardsRequest {
                this.context = request.context
                this.subscriber = contactInfo {
                    this.email = reply0.fuelCard.contact.email
                    this.phone = reply0.fuelCard.contact.phone
                    this.preferredChannel = reply0.fuelCard.contact.preferredChannel
                }
            }
            yardsYardService.watchYards(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            normalizeFuelCardResponse {
                this.fuelCard = fuelCard {
                    this.balance = request.fuelCard.balance
                    this.contact = contactInfo {
                        this.phone = reply0.fuelCard.contact.phone
                    }
                    this.fuelCardId = request.fuelCardId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handlePreviewFuelCard(request: PreviewFuelCardRequest): PreviewFuelCardResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardValidationService/PreviewFuelCard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PreviewFuelCard", request)
            log.atInfo().addKeyValue("operation", "PreviewFuelCard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCard.fuelCardId).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                validateNotificationRequest {
                    this.context = request.context
                    this.details = ValidateNotificationRequestKt.details {
                        this.contact = contactInfo {
                            this.name = request.details.contact.name
                        }
                    }
                    this.notification = notification {
                        this.assignee = userRef {
                            this.displayName = request.fuelCard.holder.displayName
                            this.email = request.fuelCard.contact.email
                        }
                        this.requester = contactInfo {
                            this.phone = request.fuelCard.contact.phone
                        }
                    }
                }
            }
            val reply0 = notificationsNotificationValidationService.validateNotification(outbound0)
            val outbound1 = validateAuditEventRequest {
                this.context = request.context
                this.details = ValidateAuditEventRequestKt.details {
                    this.contact = contactInfo {
                        this.phone = reply0.notification.requester.phone
                    }
                }
            }
            val reply1 = ClientRegistry.lookup("acme.audit.v1.AuditEventValidationService", AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub::class.java).validateAuditEvent(outbound1)
            val outbound2 = revertFuelPolicyRequest {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.notification.audit.updatedBy
                    }
                    this.owner = userRef {
                        this.displayName = reply0.notification.customer.displayName
                        this.email = reply0.notification.assignee.email
                    }
                }
            }
            val reply2 = fuelFuelPolicyHistoryService.revertFuelPolicy(WireCopies.reflective(outbound2))
            val outbound3 = findAuditEventsRequest {
                this.context = request.context
                this.filter = FindAuditEventsRequestKt.filter {
                    this.createdAfter = reply2.fuelPolicy.audit.createdAt
                    this.labels.putAll(request.fuelCard.labelsMap)
                    this.ownerId = reply0.notification.customer.notificationTemplateId
                }
            }
            val reply3 = workerForPreviewFuelCard3.submit(outbound3)
            previewFuelCardResponse {
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.createdAt = reply2.fuelPolicy.audit.createdAt
                        this.updatedBy = reply0.notification.audit.updatedBy
                    }
                    this.balance = request.fuelCard.balance
                    this.fuelCardId = request.fuelCardId
                }
                this.outcome = PreviewFuelCardResponseKt.outcome {
                    this.status = reply1.outcome.status
                }
                this.result = operationResult {
                    this.message = reply3.result.message
                    this.status = reply3.result.status
                }
            }
        }

    private suspend fun handleValidateFuelCard(request: ValidateFuelCardRequest): ValidateFuelCardResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardValidationService/ValidateFuelCard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ValidateFuelCard", request)
            log.atInfo().addKeyValue("operation", "ValidateFuelCard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCard.holder.driverId).log("Recorded audit detail")
            val outbound0 = previewDockRequest {
                this.context = request.context
                this.details = PreviewDockRequestKt.details {
                    this.contact = contactInfo {
                        this.name = request.details.contact.name
                        this.phone = request.details.contact.phone
                    }
                }
                this.dock = dock {
                    this.audit = auditInfo {
                        this.createdBy = request.fuelCard.audit.createdBy
                    }
                    this.owner = userRef {
                        this.email = request.details.contact.email
                    }
                }
            }
            val reply0 = withTimeout(5_000) { yardsDockValidationService.previewDock(outbound0) }
            val configure1: GetFuelCardRatesRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.fuelCardId = request.fuelCard.fuelCardId
            }
            val outbound1 = getFuelCardRatesRequest(configure1)
            val reply1 = fuelFuelCardCalculationService.getFuelCardRates(outbound1)
            validateFuelCardResponse {
                this.fuelCard = fuelCard {
                    this.contact = request.details.contact
                    this.fuelCardId = request.fuelCardId
                    this.holder = driverRef {
                        this.email = reply1.fuelCard.contact.email
                    }
                    this.labels.putAll(reply1.fuelCard.labelsMap)
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }
}
