package com.acme.transport.yards.api

import com.acme.appointments.v1.AppointmentServiceGrpcKt
import com.acme.appointments.v1.CloneAppointmentRequest
import com.acme.appointments.v1.CloneAppointmentResponse
import com.acme.appointments.v1.ListAppointmentsRequestKt
import com.acme.appointments.v1.appointment
import com.acme.appointments.v1.cloneAppointmentRequest
import com.acme.appointments.v1.listAppointmentsRequest
import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.ValidateAuditEventRequestKt
import com.acme.audit.v1.auditEvent
import com.acme.audit.v1.auditTrailRef
import com.acme.audit.v1.validateAuditEventRequest
import com.acme.boxoffice.v1.BoxOfficeSaleServiceGrpcKt
import com.acme.boxoffice.v1.ListBoxOfficeSalesRequest
import com.acme.boxoffice.v1.ListBoxOfficeSalesRequestKt
import com.acme.boxoffice.v1.ListBoxOfficeSalesResponse
import com.acme.boxoffice.v1.listBoxOfficeSalesRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.documents.v1.DocumentServiceGrpcKt
import com.acme.documents.v1.WatchDocumentsRequestKt
import com.acme.documents.v1.watchDocumentsRequest
import com.acme.featureflags.v1.ExplainFeatureFlagDecisionRequest
import com.acme.featureflags.v1.ExplainFeatureFlagDecisionRequestKt
import com.acme.featureflags.v1.ExplainFeatureFlagDecisionResponse
import com.acme.featureflags.v1.FeatureFlagValidationServiceGrpcKt
import com.acme.featureflags.v1.ScoreFeatureFlagRequest
import com.acme.featureflags.v1.ScoreFeatureFlagRequestKt
import com.acme.featureflags.v1.checkFeatureFlagEligibilityRequest
import com.acme.featureflags.v1.copy as featureflagsCopy
import com.acme.featureflags.v1.explainFeatureFlagDecisionRequest
import com.acme.featureflags.v1.featureFlag
import com.acme.featureflags.v1.normalizeFeatureFlagRequest
import com.acme.featureflags.v1.scoreFeatureFlagRequest
import com.acme.identity.v1.userRef
import com.acme.keymanagement.v1.CryptoKeyQueryServiceGrpcKt
import com.acme.keymanagement.v1.resolveCryptoKeysRequest
import com.acme.ledger.v1.LedgerAccountServiceGrpcKt
import com.acme.ledger.v1.WatchLedgerAccountsRequestKt
import com.acme.ledger.v1.watchLedgerAccountsRequest
import com.acme.notifications.v1.CloneNotificationRequest
import com.acme.notifications.v1.NotificationServiceGrpcKt
import com.acme.notifications.v1.cloneNotificationRequest
import com.acme.notifications.v1.notification
import com.acme.notifications.v1.notificationComment
import com.acme.notifications.v1.notificationTemplateRef
import com.acme.patients.v1.PatientServiceGrpcKt
import com.acme.patients.v1.batchGetPatientsRequest
import com.acme.patients.v1.patient
import com.acme.patients.v1.updatePatientRequest
import com.acme.ratelimiting.v1.CloneRateLimitRequest
import com.acme.ratelimiting.v1.RateLimitServiceGrpcKt
import com.acme.ratelimiting.v1.cloneRateLimitRequest
import com.acme.ratelimiting.v1.rateLimit
import com.acme.search.v1.CountSearchIndexesRequestKt
import com.acme.search.v1.FindSearchIndexesRequest
import com.acme.search.v1.FindSearchIndexesRequestKt
import com.acme.search.v1.SearchIndexQueryServiceGrpcKt
import com.acme.search.v1.countSearchIndexesRequest
import com.acme.search.v1.findSearchIndexesRequest
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.transport.yards.YardsRepository
import com.acme.venues.v1.SuggestVenuesRequest
import com.acme.venues.v1.SuggestVenuesRequestKt
import com.acme.venues.v1.SuggestVenuesResponse
import com.acme.venues.v1.VenueQueryServiceGrpcKt
import com.acme.venues.v1.suggestVenuesRequest
import com.acme.yards.v1.CalculateYardRequest
import com.acme.yards.v1.CalculateYardResponse
import com.acme.yards.v1.CompareYardOptionsRequest
import com.acme.yards.v1.CompareYardOptionsResponse
import com.acme.yards.v1.EstimateYardRequest
import com.acme.yards.v1.EstimateYardResponse
import com.acme.yards.v1.EstimateYardResponseKt
import com.acme.yards.v1.ExplainYardCalculationRequest
import com.acme.yards.v1.ExplainYardCalculationResponse
import com.acme.yards.v1.ExplainYardCalculationResponseKt
import com.acme.yards.v1.GetYardRatesRequest
import com.acme.yards.v1.GetYardRatesResponse
import com.acme.yards.v1.QuoteYardRequest
import com.acme.yards.v1.QuoteYardResponse
import com.acme.yards.v1.QuoteYardResponseKt
import com.acme.yards.v1.RecalculateYardsRequest
import com.acme.yards.v1.RecalculateYardsResponse
import com.acme.yards.v1.SimulateYardRequest
import com.acme.yards.v1.SimulateYardResponse
import com.acme.yards.v1.YardCalculationServiceGrpcKt
import com.acme.yards.v1.YardComponent
import com.acme.yards.v1.calculateYardResponse
import com.acme.yards.v1.compareYardOptionsResponse
import com.acme.yards.v1.estimateYardResponse
import com.acme.yards.v1.explainYardCalculationResponse
import com.acme.yards.v1.getYardRatesResponse
import com.acme.yards.v1.quoteYardResponse
import com.acme.yards.v1.recalculateYardsResponse
import com.acme.yards.v1.simulateYardResponse
import com.acme.yards.v1.yard
import com.google.protobuf.Any as ProtoAny
import com.google.protobuf.Timestamp
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Computes the figures of a yard, such as totals, taxes, and estimates. */
class YardCalculationGrpcService(
    private val store: YardsRepository,
    private val workers: CoroutineScope,
    private val appointmentsAppointmentService: AppointmentServiceGrpcKt.AppointmentServiceCoroutineStub,
    private val auditAuditEventValidationService: AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub,
    private val boxofficeBoxOfficeSaleService: BoxOfficeSaleServiceGrpcKt.BoxOfficeSaleServiceCoroutineStub,
    private val documentsDocumentService: DocumentServiceGrpcKt.DocumentServiceCoroutineStub,
    private val featureflagsFeatureFlagValidationService: FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub,
    private val keymanagementCryptoKeyQueryService: CryptoKeyQueryServiceGrpcKt.CryptoKeyQueryServiceCoroutineStub,
    private val ledgerLedgerAccountService: LedgerAccountServiceGrpcKt.LedgerAccountServiceCoroutineStub,
    private val notificationsNotificationService: NotificationServiceGrpcKt.NotificationServiceCoroutineStub,
    private val patientsPatientService: PatientServiceGrpcKt.PatientServiceCoroutineStub,
    private val ratelimitingRateLimitService: RateLimitServiceGrpcKt.RateLimitServiceCoroutineStub,
    private val searchSearchIndexQueryService: SearchIndexQueryServiceGrpcKt.SearchIndexQueryServiceCoroutineStub,
    private val venuesVenueQueryService: VenueQueryServiceGrpcKt.VenueQueryServiceCoroutineStub,
) : YardCalculationServiceGrpcKt.YardCalculationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(YardCalculationGrpcService::class.java)

    private val workerForCalculateYard3 = ReplyMailbox<ListBoxOfficeSalesRequest, ListBoxOfficeSalesResponse>(workers) { payload ->
        withTimeout(5_000) { boxofficeBoxOfficeSaleService.listBoxOfficeSales(payload) }
    }

    private val invokeSimulateYard0: suspend (ExplainFeatureFlagDecisionRequest) -> ExplainFeatureFlagDecisionResponse = { payload ->
        featureflagsFeatureFlagValidationService.explainFeatureFlagDecision(payload)
    }

    /** Calculates a yard. */
    override suspend fun calculateYard(request: CalculateYardRequest): CalculateYardResponse {
        return handleCalculateYard(request)
    }

    /** Compares a yard. */
    override suspend fun compareYardOptions(request: CompareYardOptionsRequest): CompareYardOptionsResponse = handleCompareYardOptions(request)

    /** Estimates a yard. */
    override suspend fun estimateYard(request: EstimateYardRequest): EstimateYardResponse =
        RpcBoundary.run("acme.yards.v1.YardCalculationService/EstimateYard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EstimateYard", request)
            log.atInfo().addKeyValue("operation", "EstimateYard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yard.yardId).log("Recorded audit detail")
            val outbound0 = checkFeatureFlagEligibilityRequest {
                this.context = request.context
                this.featureFlag = featureFlag {
                    this.audit = auditInfo {
                        this.etag = request.yard.audit.etag
                        this.updatedBy = request.yard.audit.updatedBy
                    }
                }
            }
            val reply0 = flowOf(outbound0).map { value -> featureflagsFeatureFlagValidationService.checkFeatureFlagEligibility(value) }.single()
            val outbound1 = normalizeFeatureFlagRequest {
                this.context = request.context
                this.featureFlag = featureFlag {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.featureFlag.audit.updatedBy
                    }
                    this.owner = userRef {
                        this.userId = request.yard.owner.userId
                    }
                }
            }
            val reply1 = withTimeout(5_000) { featureflagsFeatureFlagValidationService.normalizeFeatureFlag(outbound1) }
            estimateYardResponse {
                this.breakdown = EstimateYardResponseKt.breakdown {
                    this.currencyCode = request.details.currencyCode
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.yard = yard {
                    this.audit = auditInfo {
                        this.createdBy = reply0.featureFlag.audit.createdBy
                        this.updatedBy = reply0.featureFlag.audit.updatedBy
                    }
                    this.owner = reply1.featureFlag.owner
                    this.yardId = request.yardId
                }
            }
        }

    /** Explains a yard. */
    override suspend fun explainYardCalculation(request: ExplainYardCalculationRequest): ExplainYardCalculationResponse {
        return handleExplainYardCalculation(request)
    }

    /** Returns a yard. */
    override suspend fun getYardRates(request: GetYardRatesRequest): GetYardRatesResponse = handleGetYardRates(request)

    /** Quotes a yard. */
    override suspend fun quoteYard(request: QuoteYardRequest): QuoteYardResponse =
        RpcBoundary.run("acme.yards.v1.YardCalculationService/QuoteYard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "QuoteYard", request)
            log.atInfo().addKeyValue("operation", "QuoteYard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yard.yardId).log("Recorded audit detail")
            val outbound0 = request.toScoreFeatureFlagFromQuoteYard0()
            val reply0 = featureflagsFeatureFlagValidationService.scoreFeatureFlag(outbound0)
            val outbound1 = validateAuditEventRequest {
                this.auditEvent = auditEvent {
                    this.subject = auditTrailRef {
                        this.displayName = reply0.featureFlag.owner.displayName
                    }
                }
                this.context = request.context
                this.details = ValidateAuditEventRequestKt.details {
                    this.contact = contactInfo {
                        this.email = reply0.featureFlag.owner.email
                    }
                }
            }
            val reply1 = auditAuditEventValidationService.validateAuditEvent(outbound1)
            val outbound2 = cloneRateLimitRequest {
                this.context = request.context
                this.rateLimit = rateLimit {
                    this.owner = userRef {
                        this.displayName = reply0.featureFlag.owner.displayName
                        this.userId = reply0.featureFlag.owner.userId
                    }
                }
            }
            val envelope2 = ProtoAny.pack(outbound2)
            val reply2 = ratelimitingRateLimitService.cloneRateLimit(envelope2.unpack(CloneRateLimitRequest::class.java))
            val outbound3 = updatePatientRequest {
                this.context = request.context
                this.patient = patient {
                    this.audit = auditInfo {
                        this.createdAt = request.yard.audit.createdAt
                    }
                    this.primaryAddress = postalAddress {
                        this.postalCode = request.details.destination.postalCode
                    }
                }
            }
            val reply3 = patientsPatientService.updatePatient(WireCopies.json(outbound3))
            quoteYardResponse {
                this.breakdown = QuoteYardResponseKt.breakdown {
                    this.total = reply0.breakdown.total
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.details.putAll(reply3.result.detailsMap)
                    this.referenceIds.addAll(reply2.result.referenceIdsList)
                    this.status = "ok"
                }
                this.yard = yard {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.featureFlag.audit.updatedBy
                    }
                    this.name = request.yard.name
                    this.yardId = request.yardId
                }
            }
        }

    /** Recalculates a yard. */
    override suspend fun recalculateYards(request: RecalculateYardsRequest): RecalculateYardsResponse {
        return handleRecalculateYards(request)
    }

    /** Simulates a yard. */
    override suspend fun simulateYard(request: SimulateYardRequest): SimulateYardResponse = handleSimulateYard(request)

    private suspend fun handleCalculateYard(request: CalculateYardRequest): CalculateYardResponse =
        RpcBoundary.run("acme.yards.v1.YardCalculationService/CalculateYard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CalculateYard", request)
            log.atInfo().addKeyValue("operation", "CalculateYard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yard.yardId).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                batchGetPatientsRequest {
                    this.context = request.context
                }
            }
            val reply0 = patientsPatientService.batchGetPatients(outbound0)
            val outbound1 = resolveCryptoKeysRequest {
                this.context = request.context
            }
            val reply1 = ClientRegistry.lookup("acme.keymanagement.v1.CryptoKeyQueryService", CryptoKeyQueryServiceGrpcKt.CryptoKeyQueryServiceCoroutineStub::class.java).resolveCryptoKeys(outbound1)
            val outbound2 = watchLedgerAccountsRequest {
                this.context = request.context
                this.filter = WatchLedgerAccountsRequestKt.filter {
                    this.region = request.details.destination.region
                }
                this.subscriber = contactInfo {
                    this.email = request.yard.owner.email
                }
            }
            ledgerLedgerAccountService.watchLedgerAccounts(outbound2).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound3 = listBoxOfficeSalesRequest {
                this.context = request.context
                this.filter = ListBoxOfficeSalesRequestKt.filter {
                    this.createdAfter = request.yard.audit.updatedAt
                    this.labels.putAll(request.yard.specsMap)
                    this.ownerId = (request.yard.componentsMap["primary"] ?: YardComponent.getDefaultInstance()).componentId
                    this.region = request.details.destination.region
                }
            }
            val reply3 = workerForCalculateYard3.submit(outbound3)
            calculateYardResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply1.result.message
                    this.referenceIds.addAll(reply3.result.referenceIdsList)
                    this.status = "ok"
                }
                this.yard = yard {
                    this.name = request.yard.name
                    this.yardId = request.yardId
                }
            }
        }

    private suspend fun handleCompareYardOptions(request: CompareYardOptionsRequest): CompareYardOptionsResponse =
        RpcBoundary.run("acme.yards.v1.YardCalculationService/CompareYardOptions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CompareYardOptions", request)
            log.atInfo().addKeyValue("operation", "CompareYardOptions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yard.yardId).log("Recorded audit detail")
            val outbound0 = watchDocumentsRequest {
                this.context = request.context
                this.filter = WatchDocumentsRequestKt.filter {
                    this.region = request.yard.address.region
                }
            }
            documentsDocumentService.watchDocuments(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            compareYardOptionsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.yard = yard {
                    this.capacity = request.yard.capacity
                    this.yardId = request.yardId
                }
            }
        }

    private suspend fun handleExplainYardCalculation(request: ExplainYardCalculationRequest): ExplainYardCalculationResponse =
        RpcBoundary.run("acme.yards.v1.YardCalculationService/ExplainYardCalculation") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainYardCalculation", request)
            log.atInfo().addKeyValue("operation", "ExplainYardCalculation").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yard.yardId).log("Recorded audit detail")
            val configure0: ListAppointmentsRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.filter = ListAppointmentsRequestKt.filter {
                    this.region = request.yard.address.region
                }
            }
            val outbound0 = listAppointmentsRequest(configure0)
            val reply0 = appointmentsAppointmentService.listAppointments(outbound0)
            val outbound1 = countSearchIndexesRequest {
                this.context = request.context
                this.filter = CountSearchIndexesRequestKt.filter {
                    this.createdAfter = request.yard.audit.createdAt
                    this.labels.putAll(request.yard.specsMap)
                    this.ownerId = request.yard.owner.userId
                    this.region = request.yard.address.region
                }
            }
            val reply1 = searchSearchIndexQueryService.countSearchIndexes(outbound1)
            val outbound2 = normalizeFeatureFlagRequest {
                this.context = request.context
                this.featureFlag = featureFlag {
                    this.audit = auditInfo {
                        this.createdBy = request.yard.audit.createdBy
                    }
                }
            }
            val reply2 = featureflagsFeatureFlagValidationService.normalizeFeatureFlag(outbound2)
            val outbound3 = explainFeatureFlagDecisionRequest {}.featureflagsCopy {
                this.context = request.context
                this.details = ExplainFeatureFlagDecisionRequestKt.details {
                    this.contact = contactInfo {
                        this.email = reply2.featureFlag.owner.email
                        this.phone = request.details.contact.phone
                    }
                }
                this.featureFlag = featureFlag {
                    this.audit = auditInfo {
                        this.createdAt = reply2.featureFlag.audit.createdAt
                    }
                }
            }
            val reply3 = featureflagsFeatureFlagValidationService.explainFeatureFlagDecision(outbound3)
            explainYardCalculationResponse {
                this.outcome = ExplainYardCalculationResponseKt.outcome {
                    this.message = reply2.outcome.message
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.message = reply1.result.message
                    this.status = reply0.result.status
                }
                this.yard = yard {
                    this.audit = auditInfo {
                        this.etag = reply2.featureFlag.audit.etag
                    }
                    this.code = request.yard.code
                    this.name = reply3.featureFlag.name
                    this.owner = userRef {
                        this.displayName = reply3.featureFlag.owner.displayName
                    }
                    this.yardId = request.yardId
                }
            }
        }

    private suspend fun handleGetYardRates(request: GetYardRatesRequest): GetYardRatesResponse =
        RpcBoundary.run("acme.yards.v1.YardCalculationService/GetYardRates") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetYardRates", request)
            log.atInfo().addKeyValue("operation", "GetYardRates").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            getYardRatesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.yard = yard {
                    this.yardId = request.yardId
                }
            }
        }

    private fun QuoteYardRequest.toScoreFeatureFlagFromQuoteYard0(): ScoreFeatureFlagRequest {
        val source = this
        return scoreFeatureFlagRequest {
            this.context = source.context
            this.details = ScoreFeatureFlagRequestKt.details {
                this.destination = postalAddress {
                    this.locality = source.details.destination.locality
                    this.recipient = source.yard.address.recipient
                }
            }
        }
    }

    private data class RecalculateYards0Command(
        val context: RequestContext,
        val filterRegion: String,
        val filterCreatedAfter: Timestamp,
        val filterOwnerId: String
    ) {
        fun toRequest(): FindSearchIndexesRequest {
            val command = this
            return findSearchIndexesRequest {
                this.context = command.context
                this.filter = FindSearchIndexesRequestKt.filter {
                    this.createdAfter = command.filterCreatedAfter
                    this.ownerId = command.filterOwnerId
                    this.region = command.filterRegion
                }
            }
        }
    }

    private suspend fun handleRecalculateYards(request: RecalculateYardsRequest): RecalculateYardsResponse =
        RpcBoundary.run("acme.yards.v1.YardCalculationService/RecalculateYards") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecalculateYards", request)
            log.atInfo().addKeyValue("operation", "RecalculateYards").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yard.yardId).log("Recorded audit detail")
            val outbound0 = RecalculateYards0Command(
                context = request.context,
                filterRegion = request.details.destination.region,
                filterCreatedAfter = request.yard.audit.updatedAt,
                filterOwnerId = request.yard.owner.userId
            ).toRequest()
            val reply0 = searchSearchIndexQueryService.findSearchIndexes(outbound0)
            val outbound1 = cloneNotificationRequest {
                this.context = request.context
                this.notification = notification {
                    this.assignee = userRef {
                        this.email = request.yard.owner.email
                        this.userId = request.yard.owner.userId
                    }
                    this.comments.addAll(listOf(notificationComment {
                        this.createdAt = request.yard.audit.createdAt
                    }))
                    this.customer = notificationTemplateRef {
                        this.displayName = request.yard.owner.displayName
                    }
                }
            }
            val command1: Submission<CloneNotificationRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = notificationsNotificationService.cloneNotification(payload1)
            val outbound2 = suggestVenuesRequest {
                this.context = request.context
                this.filter = SuggestVenuesRequestKt.filter {
                    this.region = request.yard.address.region
                }
            }
            val adapter2 = OutboundPort<SuggestVenuesRequest, SuggestVenuesResponse> { payload ->
                venuesVenueQueryService.suggestVenues(payload)
            }
            val port2 = DelegatingPort(adapter2)
            val reply2 = port2.execute(outbound2)
            recalculateYardsResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply2.result.detailsMap)
                    this.status = reply1.result.status
                }
                this.yard = yard {
                    this.capacity = request.yard.capacity
                    this.owner = userRef {
                        this.email = reply1.notification.requester.email
                    }
                    this.yardId = request.yardId
                }
            }
        }

    private suspend fun handleSimulateYard(request: SimulateYardRequest): SimulateYardResponse =
        RpcBoundary.run("acme.yards.v1.YardCalculationService/SimulateYard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SimulateYard", request)
            log.atInfo().addKeyValue("operation", "SimulateYard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yard.yardId).log("Recorded audit detail")
            val outbound0 = explainFeatureFlagDecisionRequest {
                this.context = request.context
                this.featureFlag = featureFlag {
                    this.audit = auditInfo {
                        this.updatedBy = request.yard.audit.updatedBy
                    }
                }
            }
            val reply0 = invokeSimulateYard0(outbound0)
            val outbound1 = cloneAppointmentRequest {
                this.appointment = appointment {
                    this.location = postalAddress {
                        this.lines.addAll(request.details.destination.linesList)
                    }
                    this.organizer = userRef {
                        this.email = request.yard.owner.email
                        this.userId = reply0.featureFlag.owner.userId
                    }
                }
                this.context = request.context
            }
            val invoke1: suspend (CloneAppointmentRequest, Metadata) -> CloneAppointmentResponse = appointmentsAppointmentService::cloneAppointment
            val reply1 = invoke1(outbound1, Metadata())
            simulateYardResponse {
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = "ok"
                }
                this.yard = yard {
                    this.audit = auditInfo {
                        this.etag = reply0.featureFlag.audit.etag
                        this.updatedAt = reply1.appointment.audit.updatedAt
                        this.updatedBy = reply0.featureFlag.audit.updatedBy
                    }
                    this.name = request.yard.name
                    this.yardId = request.yardId
                }
            }
        }
}
