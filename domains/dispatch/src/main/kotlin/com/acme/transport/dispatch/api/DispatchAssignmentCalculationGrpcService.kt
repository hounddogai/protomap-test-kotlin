package com.acme.transport.dispatch.api

import com.acme.authorization.v1.AccessPolicyServiceGrpcKt
import com.acme.authorization.v1.CreateAccessPolicyRequestKt
import com.acme.authorization.v1.accessPolicy
import com.acme.authorization.v1.createAccessPolicyRequest
import com.acme.boxoffice.v1.BoxOfficeSaleServiceGrpcKt
import com.acme.boxoffice.v1.boxOfficeSale
import com.acme.boxoffice.v1.createBoxOfficeSaleRequest
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.CompareDeliveryExceptionOptionsRequest
import com.acme.deliveries.v1.CompareDeliveryExceptionOptionsRequestKt
import com.acme.deliveries.v1.DeliveryExceptionCalculationServiceGrpcKt
import com.acme.deliveries.v1.DeliveryQueryServiceGrpcKt
import com.acme.deliveries.v1.ProofOfDeliveryValidationServiceGrpcKt
import com.acme.deliveries.v1.SuggestDeliveriesRequestKt
import com.acme.deliveries.v1.SuggestDeliveriesResponse
import com.acme.deliveries.v1.ValidateProofOfDeliveryRequest
import com.acme.deliveries.v1.ValidateProofOfDeliveryResponse
import com.acme.deliveries.v1.compareDeliveryExceptionOptionsRequest
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.suggestDeliveriesRequest
import com.acme.deliveries.v1.validateProofOfDeliveryRequest
import com.acme.dispatch.v1.CalculateDispatchAssignmentRequest
import com.acme.dispatch.v1.CalculateDispatchAssignmentResponse
import com.acme.dispatch.v1.CompareDispatchAssignmentOptionsRequest
import com.acme.dispatch.v1.CompareDispatchAssignmentOptionsResponse
import com.acme.dispatch.v1.DispatchAssignmentCalculationServiceGrpcKt
import com.acme.dispatch.v1.EstimateDispatchAssignmentRequest
import com.acme.dispatch.v1.EstimateDispatchAssignmentResponse
import com.acme.dispatch.v1.ExplainDispatchAssignmentCalculationRequest
import com.acme.dispatch.v1.ExplainDispatchAssignmentCalculationResponse
import com.acme.dispatch.v1.GetDispatchAssignmentRatesRequest
import com.acme.dispatch.v1.GetDispatchAssignmentRatesResponse
import com.acme.dispatch.v1.QuoteDispatchAssignmentRequest
import com.acme.dispatch.v1.QuoteDispatchAssignmentResponse
import com.acme.dispatch.v1.RecalculateDispatchAssignmentsRequest
import com.acme.dispatch.v1.RecalculateDispatchAssignmentsResponse
import com.acme.dispatch.v1.SimulateDispatchAssignmentRequest
import com.acme.dispatch.v1.SimulateDispatchAssignmentResponse
import com.acme.dispatch.v1.calculateDispatchAssignmentResponse
import com.acme.dispatch.v1.compareDispatchAssignmentOptionsResponse
import com.acme.dispatch.v1.dispatchAssignment
import com.acme.dispatch.v1.dispatchAssignmentComment
import com.acme.dispatch.v1.estimateDispatchAssignmentResponse
import com.acme.dispatch.v1.explainDispatchAssignmentCalculationResponse
import com.acme.dispatch.v1.getDispatchAssignmentRatesResponse
import com.acme.dispatch.v1.quoteDispatchAssignmentResponse
import com.acme.dispatch.v1.recalculateDispatchAssignmentsResponse
import com.acme.dispatch.v1.simulateDispatchAssignmentResponse
import com.acme.documents.v1.BatchGetDocumentsRequest
import com.acme.documents.v1.BatchGetDocumentsResponse
import com.acme.documents.v1.DocumentServiceGrpcKt
import com.acme.documents.v1.batchGetDocumentsRequest
import com.acme.fuel.v1.CountFuelCardsRequestKt
import com.acme.fuel.v1.CountFuelCardsResponse
import com.acme.fuel.v1.FuelCardQueryServiceGrpcKt
import com.acme.fuel.v1.countFuelCardsRequest
import com.acme.identity.v1.UserServiceGrpcKt
import com.acme.identity.v1.getUserRequest
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.LedgerAccountServiceGrpcKt
import com.acme.ledger.v1.LedgerAccountValidationServiceGrpcKt
import com.acme.ledger.v1.ScoreLedgerAccountRequestKt
import com.acme.ledger.v1.SearchLedgerAccountsRequest
import com.acme.ledger.v1.SearchLedgerAccountsRequestKt
import com.acme.ledger.v1.ledgerAccount
import com.acme.ledger.v1.postingBatchRef
import com.acme.ledger.v1.scoreLedgerAccountRequest
import com.acme.ledger.v1.searchLedgerAccountsRequest
import com.acme.metrics.v1.ListMetricSeriesesRequestKt
import com.acme.metrics.v1.MetricSeriesServiceGrpcKt
import com.acme.metrics.v1.copy as metricsCopy
import com.acme.metrics.v1.listMetricSeriesesRequest
import com.acme.seating.v1.CompareSeatMapOptionsRequest
import com.acme.seating.v1.ResolveSeatMapsRequest
import com.acme.seating.v1.SeatMapCalculationServiceGrpcKt
import com.acme.seating.v1.SeatMapQueryServiceGrpcKt
import com.acme.seating.v1.compareSeatMapOptionsRequest
import com.acme.seating.v1.estimateSeatMapRequest
import com.acme.seating.v1.resolveSeatMapsRequest
import com.acme.seating.v1.seatMap
import com.acme.telematics.v1.TelemetryReadingServiceGrpcKt
import com.acme.telematics.v1.WatchTelemetryReadingsRequestKt
import com.acme.telematics.v1.watchTelemetryReadingsRequest
import com.acme.transport.dispatch.DispatchRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.venues.v1.LintVenuesRequestKt
import com.acme.venues.v1.ScoreVenueRequestKt
import com.acme.venues.v1.VenueValidationServiceGrpcKt
import com.acme.venues.v1.lintVenuesRequest
import com.acme.venues.v1.normalizeVenueRequest
import com.acme.venues.v1.scoreVenueRequest
import com.acme.venues.v1.venue
import com.acme.yards.v1.CheckYardEligibilityRequestKt
import com.acme.yards.v1.YardValidationServiceGrpcKt
import com.acme.yards.v1.checkYardEligibilityRequest
import com.acme.yards.v1.explainYardDecisionRequest
import com.acme.yards.v1.yard
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Computes the figures of a dispatch assignment, such as totals, taxes, and estimates. */
class DispatchAssignmentCalculationGrpcService(
    private val store: DispatchRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyService: AccessPolicyServiceGrpcKt.AccessPolicyServiceCoroutineStub,
    private val boxofficeBoxOfficeSaleService: BoxOfficeSaleServiceGrpcKt.BoxOfficeSaleServiceCoroutineStub,
    private val deliveriesDeliveryExceptionCalculationService: DeliveryExceptionCalculationServiceGrpcKt.DeliveryExceptionCalculationServiceCoroutineStub,
    private val deliveriesDeliveryQueryService: DeliveryQueryServiceGrpcKt.DeliveryQueryServiceCoroutineStub,
    private val deliveriesProofOfDeliveryValidationService: ProofOfDeliveryValidationServiceGrpcKt.ProofOfDeliveryValidationServiceCoroutineStub,
    private val documentsDocumentService: DocumentServiceGrpcKt.DocumentServiceCoroutineStub,
    private val fuelFuelCardQueryService: FuelCardQueryServiceGrpcKt.FuelCardQueryServiceCoroutineStub,
    private val identityUserService: UserServiceGrpcKt.UserServiceCoroutineStub,
    private val ledgerLedgerAccountService: LedgerAccountServiceGrpcKt.LedgerAccountServiceCoroutineStub,
    private val ledgerLedgerAccountValidationService: LedgerAccountValidationServiceGrpcKt.LedgerAccountValidationServiceCoroutineStub,
    private val metricsMetricSeriesService: MetricSeriesServiceGrpcKt.MetricSeriesServiceCoroutineStub,
    private val seatingSeatMapCalculationService: SeatMapCalculationServiceGrpcKt.SeatMapCalculationServiceCoroutineStub,
    private val seatingSeatMapQueryService: SeatMapQueryServiceGrpcKt.SeatMapQueryServiceCoroutineStub,
    private val telematicsTelemetryReadingService: TelemetryReadingServiceGrpcKt.TelemetryReadingServiceCoroutineStub,
    private val venuesVenueValidationService: VenueValidationServiceGrpcKt.VenueValidationServiceCoroutineStub,
    private val yardsYardValidationService: YardValidationServiceGrpcKt.YardValidationServiceCoroutineStub,
) : DispatchAssignmentCalculationServiceGrpcKt.DispatchAssignmentCalculationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DispatchAssignmentCalculationGrpcService::class.java)

    private val workerForRecalculateDispatchAssignments3 = ReplyMailbox<BatchGetDocumentsRequest, BatchGetDocumentsResponse>(workers) { payload ->
        withTimeout(5_000) { documentsDocumentService.batchGetDocuments(payload) }
    }

    private val clientForSimulateDispatchAssignment0 by lazy { seatingSeatMapCalculationService }

    /** Calculates a dispatch assignment. */
    override suspend fun calculateDispatchAssignment(request: CalculateDispatchAssignmentRequest): CalculateDispatchAssignmentResponse {
        return handleCalculateDispatchAssignment(request)
    }

    /** Compares a dispatch assignment. */
    override suspend fun compareDispatchAssignmentOptions(request: CompareDispatchAssignmentOptionsRequest): CompareDispatchAssignmentOptionsResponse = handleCompareDispatchAssignmentOptions(request)

    /** Estimates a dispatch assignment. */
    override suspend fun estimateDispatchAssignment(request: EstimateDispatchAssignmentRequest): EstimateDispatchAssignmentResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentCalculationService/EstimateDispatchAssignment") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EstimateDispatchAssignment", request)
            log.atInfo().addKeyValue("operation", "EstimateDispatchAssignment").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchAssignment.dispatchAssignmentId).log("Recorded audit detail")
            val outbound0 = checkYardEligibilityRequest {
                this.context = request.context
                this.details = CheckYardEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.preferredChannel = request.dispatchAssignment.requester.preferredChannel
                    }
                }
                this.yard = yard {
                    this.address = postalAddress {
                        this.locality = request.details.destination.locality
                        this.postalCode = request.details.destination.postalCode
                    }
                    this.owner = userRef {
                        this.email = request.dispatchAssignment.customer.email
                    }
                }
            }
            val reply0 = yardsYardValidationService.checkYardEligibility(WireCopies.json(outbound0))
            val outbound1 = watchTelemetryReadingsRequest {
                this.context = request.context
                this.filter = WatchTelemetryReadingsRequestKt.filter {
                    this.region = request.details.destination.region
                }
                this.subscriber = contactInfo {
                    this.email = request.dispatchAssignment.customer.email
                    this.name = request.dispatchAssignment.requester.name
                    this.preferredChannel = request.dispatchAssignment.requester.preferredChannel
                }
            }
            telematicsTelemetryReadingService.watchTelemetryReadings(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound2 = compareSeatMapOptionsRequest {
                this.context = request.context
                this.seatMap = seatMap {
                    this.children.addAll(listOf(seatMap {
                        this.children.addAll(listOf(seatMap {
                            this.location = postalAddress {
                                this.recipient = request.details.destination.recipient
                            }
                            this.manager = userRef {
                                this.email = reply0.yard.owner.email
                                this.userId = reply0.yard.owner.userId
                            }
                        }))
                    }))
                }
            }
            val command2: Submission<CompareSeatMapOptionsRequest> = Submission.Live(outbound2)
            val payload2 = when (val command = command2) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply2 = seatingSeatMapCalculationService.compareSeatMapOptions(payload2)
            estimateDispatchAssignmentResponse {
                this.dispatchAssignment = dispatchAssignment {
                    this.audit = auditInfo {
                        this.updatedBy = reply2.seatMap.audit.updatedBy
                    }
                    this.comments.addAll(listOf(dispatchAssignmentComment {
                        this.createdAt = reply2.seatMap.audit.createdAt
                    }))
                    this.dispatchAssignmentId = request.dispatchAssignmentId
                    this.requester = request.dispatchAssignment.requester
                }
                this.result = operationResult {
                    this.status = reply0.outcome.status
                }
            }
        }

    /** Explains a dispatch assignment. */
    override suspend fun explainDispatchAssignmentCalculation(request: ExplainDispatchAssignmentCalculationRequest): ExplainDispatchAssignmentCalculationResponse {
        return handleExplainDispatchAssignmentCalculation(request)
    }

    /** Returns a dispatch assignment. */
    override suspend fun getDispatchAssignmentRates(request: GetDispatchAssignmentRatesRequest): GetDispatchAssignmentRatesResponse = handleGetDispatchAssignmentRates(request)

    /** Quotes a dispatch assignment. */
    override suspend fun quoteDispatchAssignment(request: QuoteDispatchAssignmentRequest): QuoteDispatchAssignmentResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentCalculationService/QuoteDispatchAssignment") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "QuoteDispatchAssignment", request)
            log.atInfo().addKeyValue("operation", "QuoteDispatchAssignment").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchAssignment.dispatchAssignmentId).log("Recorded audit detail")
            val outbound0 = validateProofOfDeliveryRequest {
                this.context = request.context
                this.proofOfDelivery = proofOfDelivery {
                    this.billingAddress = postalAddress {
                        this.postalCode = request.details.destination.postalCode
                    }
                }
            }
            val invoke0: suspend (ValidateProofOfDeliveryRequest, Metadata) -> ValidateProofOfDeliveryResponse = deliveriesProofOfDeliveryValidationService::validateProofOfDelivery
            val reply0 = invoke0(outbound0, Metadata())
            val outbound1 = CompareDeliveryExceptionOptionsRequest.newBuilder().apply {
                setContext(request.context)
                setDeliveryException(deliveryException {
                    this.assignee = userRef {
                        this.email = request.dispatchAssignment.assignee.email
                    }
                    this.audit = auditInfo {
                        this.createdAt = request.dispatchAssignment.audit.createdAt
                    }
                })
                setDetails(CompareDeliveryExceptionOptionsRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = request.details.destination.countryCode
                        this.lines.addAll(request.details.destination.linesList)
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> deliveriesDeliveryExceptionCalculationService.compareDeliveryExceptionOptions(value) }
            quoteDispatchAssignmentResponse {
                this.dispatchAssignment = dispatchAssignment {
                    this.audit = auditInfo {
                        this.updatedBy = reply1.deliveryException.audit.updatedBy
                    }
                    this.dispatchAssignmentId = request.dispatchAssignmentId
                    this.status = request.dispatchAssignment.status
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
            }
        }

    /** Recalculates a dispatch assignment. */
    override suspend fun recalculateDispatchAssignments(request: RecalculateDispatchAssignmentsRequest): RecalculateDispatchAssignmentsResponse {
        return handleRecalculateDispatchAssignments(request)
    }

    /** Simulates a dispatch assignment. */
    override suspend fun simulateDispatchAssignment(request: SimulateDispatchAssignmentRequest): SimulateDispatchAssignmentResponse = handleSimulateDispatchAssignment(request)

    private suspend fun handleCalculateDispatchAssignment(request: CalculateDispatchAssignmentRequest): CalculateDispatchAssignmentResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentCalculationService/CalculateDispatchAssignment") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CalculateDispatchAssignment", request)
            log.atInfo().addKeyValue("operation", "CalculateDispatchAssignment").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchAssignment.dispatchAssignmentId).log("Recorded audit detail")
            val configure0: CreateAccessPolicyRequestKt.Dsl.() -> Unit = {
                this.accessPolicy = accessPolicy {
                    this.audit = auditInfo {
                        this.updatedAt = request.dispatchAssignment.audit.updatedAt
                    }
                }
                this.context = request.context
            }
            val outbound0 = createAccessPolicyRequest(configure0)
            val reply0 = authorizationAccessPolicyService.createAccessPolicy(outbound0)
            val outbound1 = scoreLedgerAccountRequest {
                this.context = request.context
                this.details = ScoreLedgerAccountRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = request.details.destination.countryCode
                    }
                }
                this.ledgerAccount = ledgerAccount {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.accessPolicy.audit.updatedAt
                    }
                    this.contact = contactInfo {
                        this.email = request.dispatchAssignment.assignee.email
                    }
                    this.holder = postingBatchRef {
                        this.displayName = reply0.accessPolicy.owner.displayName
                    }
                }
            }
            val reply1 = ledgerLedgerAccountValidationService.scoreLedgerAccount(outbound1)
            val outbound2 = scoreVenueRequest {
                this.context = request.context
                this.details = ScoreVenueRequestKt.details {
                    this.destination = postalAddress {
                        this.postalCode = request.details.destination.postalCode
                    }
                }
                this.venue = venue {
                    this.address = postalAddress {
                        this.countryCode = request.details.destination.countryCode
                    }
                    this.audit = auditInfo {
                        this.createdAt = reply1.ledgerAccount.audit.createdAt
                    }
                    this.owner = userRef {
                        this.displayName = request.dispatchAssignment.customer.displayName
                    }
                }
            }
            val reply2 = venuesVenueValidationService.scoreVenue(outbound2)
            val outbound3 = listMetricSeriesesRequest {}.metricsCopy {
                this.context = request.context
                this.filter = ListMetricSeriesesRequestKt.filter {
                    this.labels.putAll(reply1.ledgerAccount.labelsMap)
                    this.region = reply2.venue.address.region
                }
            }
            val reply3 = metricsMetricSeriesService.listMetricSerieses(outbound3)
            calculateDispatchAssignmentResponse {
                this.dispatchAssignment = dispatchAssignment {
                    this.dispatchAssignmentId = request.dispatchAssignmentId
                    this.priority = request.dispatchAssignment.priority
                    this.requester = contactInfo {
                        this.name = reply1.ledgerAccount.contact.name
                    }
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply3.result.message
                    this.status = "ok"
                }
            }
        }

    private fun CompareDispatchAssignmentOptionsRequest.toResolveSeatMapsFromCompareDispatchAssignmentOptions0(): ResolveSeatMapsRequest {
        val source = this
        return resolveSeatMapsRequest {
            this.context = source.context
        }
    }

    private suspend fun handleCompareDispatchAssignmentOptions(request: CompareDispatchAssignmentOptionsRequest): CompareDispatchAssignmentOptionsResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentCalculationService/CompareDispatchAssignmentOptions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CompareDispatchAssignmentOptions", request)
            log.atInfo().addKeyValue("operation", "CompareDispatchAssignmentOptions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchAssignment.dispatchAssignmentId).log("Recorded audit detail")
            val outbound0 = request.toResolveSeatMapsFromCompareDispatchAssignmentOptions0()
            val reply0 = seatingSeatMapQueryService.resolveSeatMaps(outbound0)
            val outbound1 = normalizeVenueRequest {
                this.context = request.context
                this.venue = venue {
                    this.audit = auditInfo {
                        this.updatedAt = request.dispatchAssignment.audit.updatedAt
                    }
                }
            }
            val reply1 = venuesVenueValidationService.normalizeVenue(outbound1)
            val outbound2 = searchLedgerAccountsRequest {
                this.context = request.context
                this.filter = SearchLedgerAccountsRequestKt.filter {
                    this.region = request.details.destination.region
                }
            }
            val envelope2 = ProtoAny.pack(outbound2)
            val reply2 = ledgerLedgerAccountService.searchLedgerAccounts(envelope2.unpack(SearchLedgerAccountsRequest::class.java))
            compareDispatchAssignmentOptionsResponse {
                this.dispatchAssignment = dispatchAssignment {
                    this.assignee = request.dispatchAssignment.assignee
                    this.audit = auditInfo {
                        this.etag = reply1.venue.audit.etag
                        this.updatedBy = reply1.venue.audit.updatedBy
                    }
                    this.dispatchAssignmentId = request.dispatchAssignmentId
                }
                this.result = operationResult {
                    this.message = reply2.result.message
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleExplainDispatchAssignmentCalculation(request: ExplainDispatchAssignmentCalculationRequest): ExplainDispatchAssignmentCalculationResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentCalculationService/ExplainDispatchAssignmentCalculation") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainDispatchAssignmentCalculation", request)
            log.atInfo().addKeyValue("operation", "ExplainDispatchAssignmentCalculation").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchAssignment.dispatchAssignmentId).log("Recorded audit detail")
            var reply0 = SuggestDeliveriesResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.dispatchAssignment.historyList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound0 = suggestDeliveriesRequest {
                                this.context = request.context
                                this.filter = SuggestDeliveriesRequestKt.filter {
                                    this.createdAfter = item.changedAt
                                    this.ownerId = request.dispatchAssignment.sla.policyId
                                }
                            }
                            deliveriesDeliveryQueryService.suggestDeliveries(outbound0)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply0 = it }
                }
            }
            var reply1 = CountFuelCardsResponse.getDefaultInstance()
            request.dispatchAssignment.commentsList.asFlow().map { item ->
                val outbound1 = countFuelCardsRequest {
                    this.context = request.context
                    this.filter = CountFuelCardsRequestKt.filter {
                        this.createdAfter = item.createdAt
                        this.ownerId = item.commentId
                    }
                }
                fuelFuelCardQueryService.countFuelCards(outbound1)
            }.collect { reply1 = it }
            explainDispatchAssignmentCalculationResponse {
                this.dispatchAssignment = dispatchAssignment {
                    this.dispatchAssignmentId = request.dispatchAssignmentId
                    this.requester = request.dispatchAssignment.requester
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleGetDispatchAssignmentRates(request: GetDispatchAssignmentRatesRequest): GetDispatchAssignmentRatesResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentCalculationService/GetDispatchAssignmentRates") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetDispatchAssignmentRates", request)
            log.atInfo().addKeyValue("operation", "GetDispatchAssignmentRates").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            getDispatchAssignmentRatesResponse {
                this.dispatchAssignment = dispatchAssignment {
                    this.dispatchAssignmentId = request.dispatchAssignmentId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleRecalculateDispatchAssignments(request: RecalculateDispatchAssignmentsRequest): RecalculateDispatchAssignmentsResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentCalculationService/RecalculateDispatchAssignments") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecalculateDispatchAssignments", request)
            log.atInfo().addKeyValue("operation", "RecalculateDispatchAssignments").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchAssignment.dispatchAssignmentId).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                getUserRequest {
                    this.context = request.context
                    this.userId = request.dispatchAssignment.assignee.userId
                }
            }
            val reply0 = identityUserService.getUser(outbound0)
            val outbound1 = createBoxOfficeSaleRequest {
                this.boxOfficeSale = boxOfficeSale {
                    this.audit = auditInfo {
                        this.createdBy = request.dispatchAssignment.audit.createdBy
                        this.etag = reply0.user.audit.etag
                    }
                    this.billingAddress = postalAddress {
                        this.recipient = reply0.user.primaryAddress.recipient
                    }
                }
                this.context = request.context
            }
            val reply1 = ClientRegistry.lookup("acme.boxoffice.v1.BoxOfficeSaleService", BoxOfficeSaleServiceGrpcKt.BoxOfficeSaleServiceCoroutineStub::class.java).createBoxOfficeSale(outbound1)
            val outbound2 = explainYardDecisionRequest {
                this.context = request.context
                this.yard = yard {
                    this.address = postalAddress {
                        this.countryCode = reply0.user.primaryAddress.countryCode
                        this.region = reply1.boxOfficeSale.billingAddress.region
                    }
                    this.owner = userRef {
                        this.userId = reply0.user.relationshipManager.userId
                    }
                }
            }
            val reply2 = yardsYardValidationService.explainYardDecision(WireCopies.reflective(outbound2))
            val outbound3 = batchGetDocumentsRequest {
                this.context = request.context
            }
            val reply3 = workerForRecalculateDispatchAssignments3.submit(outbound3)
            recalculateDispatchAssignmentsResponse {
                this.dispatchAssignment = dispatchAssignment {
                    this.comments.addAll(listOf(dispatchAssignmentComment {
                        this.createdAt = reply0.user.audit.createdAt
                    }))
                    this.dispatchAssignmentId = request.dispatchAssignmentId
                    this.priority = request.dispatchAssignment.priority
                }
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.details.putAll(reply1.result.detailsMap)
                    this.message = reply3.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSimulateDispatchAssignment(request: SimulateDispatchAssignmentRequest): SimulateDispatchAssignmentResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentCalculationService/SimulateDispatchAssignment") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SimulateDispatchAssignment", request)
            log.atInfo().addKeyValue("operation", "SimulateDispatchAssignment").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchAssignment.dispatchAssignmentId).log("Recorded audit detail")
            val outbound0 = estimateSeatMapRequest {
                this.context = request.context
                this.seatMap = seatMap {
                    this.children.addAll(listOf(seatMap {
                        this.children.addAll(listOf(seatMap {
                            this.audit = auditInfo {
                                this.createdAt = request.dispatchAssignment.audit.createdAt
                            }
                        }))
                    }))
                }
            }
            val reply0 = clientForSimulateDispatchAssignment0.estimateSeatMap(outbound0)
            val outbound1 = lintVenuesRequest {
                this.context = request.context
                this.details = LintVenuesRequestKt.details {
                    this.contact = contactInfo {
                        this.phone = request.dispatchAssignment.requester.phone
                    }
                }
                this.venue = venue {
                    this.address = postalAddress {
                        this.postalCode = reply0.seatMap.location.postalCode
                    }
                    this.owner = userRef {
                        this.userId = reply0.seatMap.manager.userId
                    }
                }
            }
            val reply1 = flowOf(outbound1).map { value -> venuesVenueValidationService.lintVenues(value) }.single()
            simulateDispatchAssignmentResponse {
                this.dispatchAssignment = dispatchAssignment {
                    this.assignee = userRef {
                        this.email = reply0.seatMap.manager.email
                    }
                    this.dispatchAssignmentId = request.dispatchAssignmentId
                    this.status = request.dispatchAssignment.status
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.status = "ok"
                }
            }
        }
}
