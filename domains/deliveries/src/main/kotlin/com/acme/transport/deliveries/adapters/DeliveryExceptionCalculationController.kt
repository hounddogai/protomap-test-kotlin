package com.acme.transport.deliveries.adapters

import com.acme.admissions.v1.AdmissionCalculationServiceGrpcKt
import com.acme.admissions.v1.RecalculateAdmissionsRequestKt
import com.acme.admissions.v1.admission
import com.acme.admissions.v1.gateScanRef
import com.acme.admissions.v1.recalculateAdmissionsRequest
import com.acme.common.v1.PersonName
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.CalculateDeliveryExceptionRequest
import com.acme.deliveries.v1.CalculateDeliveryExceptionResponse
import com.acme.deliveries.v1.CalculateDeliveryExceptionResponseKt
import com.acme.deliveries.v1.CalculateProofOfDeliveryResponse
import com.acme.deliveries.v1.CompareDeliveryExceptionOptionsRequest
import com.acme.deliveries.v1.CompareDeliveryExceptionOptionsResponse
import com.acme.deliveries.v1.CompareDeliveryExceptionOptionsResponseKt
import com.acme.deliveries.v1.DeliveryExceptionCalculationServiceGrpcKt
import com.acme.deliveries.v1.EstimateDeliveryExceptionRequest
import com.acme.deliveries.v1.EstimateDeliveryExceptionResponse
import com.acme.deliveries.v1.EstimateProofOfDeliveryRequestKt
import com.acme.deliveries.v1.ExplainProofOfDeliveryCalculationRequest
import com.acme.deliveries.v1.ExplainProofOfDeliveryCalculationRequestKt
import com.acme.deliveries.v1.ExplainProofOfDeliveryCalculationResponse
import com.acme.deliveries.v1.GetDeliveryExceptionRatesRequest
import com.acme.deliveries.v1.GetDeliveryExceptionRatesResponse
import com.acme.deliveries.v1.ProofOfDeliveryCalculationServiceGrpcKt
import com.acme.deliveries.v1.QuoteDeliveryExceptionRequest
import com.acme.deliveries.v1.QuoteDeliveryExceptionResponse
import com.acme.deliveries.v1.SimulateDeliveryExceptionRequest
import com.acme.deliveries.v1.SimulateDeliveryExceptionResponse
import com.acme.deliveries.v1.SimulateDeliveryExceptionResponseKt
import com.acme.deliveries.v1.calculateDeliveryExceptionResponse
import com.acme.deliveries.v1.calculateProofOfDeliveryRequest
import com.acme.deliveries.v1.compareDeliveryExceptionOptionsResponse
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.deliveryExceptionComment
import com.acme.deliveries.v1.estimateDeliveryExceptionResponse
import com.acme.deliveries.v1.estimateProofOfDeliveryRequest
import com.acme.deliveries.v1.explainProofOfDeliveryCalculationRequest
import com.acme.deliveries.v1.getDeliveryExceptionRatesResponse
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.proofOfDeliveryLine
import com.acme.deliveries.v1.quoteDeliveryExceptionResponse
import com.acme.deliveries.v1.simulateDeliveryExceptionResponse
import com.acme.drivers.v1.driverRef
import com.acme.identity.v1.userRef
import com.acme.notifications.v1.NotificationServiceGrpcKt
import com.acme.notifications.v1.SearchNotificationsRequestKt
import com.acme.notifications.v1.SearchNotificationsResponse
import com.acme.notifications.v1.searchNotificationsRequest
import com.acme.performers.v1.ListPerformersRequest
import com.acme.performers.v1.ListPerformersRequestKt
import com.acme.performers.v1.ListPerformersResponse
import com.acme.performers.v1.PerformerServiceGrpcKt
import com.acme.performers.v1.listPerformersRequest
import com.acme.search.v1.CreateSearchIndexRequest
import com.acme.search.v1.SearchIndexServiceGrpcKt
import com.acme.search.v1.UpdateSearchIndexRequest
import com.acme.search.v1.createSearchIndexRequest
import com.acme.search.v1.searchIndex
import com.acme.search.v1.updateSearchIndexRequest
import com.acme.transport.deliveries.DeliveriesRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.venues.v1.SummarizeVenuesRequest
import com.acme.venues.v1.SummarizeVenuesRequestKt
import com.acme.venues.v1.VenueQueryServiceGrpcKt
import com.acme.venues.v1.summarizeVenuesRequest
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.LoggerFactory

/** Computes the figures of a delivery exception, such as totals, taxes, and estimates. */
class DeliveryExceptionCalculationController(
    private val store: DeliveriesRepository,
    private val workers: CoroutineScope,
    private val admissionsAdmissionCalculationService: AdmissionCalculationServiceGrpcKt.AdmissionCalculationServiceCoroutineStub,
    private val deliveriesProofOfDeliveryCalculationService: ProofOfDeliveryCalculationServiceGrpcKt.ProofOfDeliveryCalculationServiceCoroutineStub,
    private val notificationsNotificationService: NotificationServiceGrpcKt.NotificationServiceCoroutineStub,
    private val performersPerformerService: PerformerServiceGrpcKt.PerformerServiceCoroutineStub,
    private val searchSearchIndexService: SearchIndexServiceGrpcKt.SearchIndexServiceCoroutineStub,
    private val venuesVenueQueryService: VenueQueryServiceGrpcKt.VenueQueryServiceCoroutineStub,
) : DeliveryExceptionCalculationServiceGrpcKt.DeliveryExceptionCalculationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DeliveryExceptionCalculationController::class.java)

    private val invokeGetDeliveryExceptionRates0: suspend (ExplainProofOfDeliveryCalculationRequest) -> ExplainProofOfDeliveryCalculationResponse = { payload ->
        deliveriesProofOfDeliveryCalculationService.explainProofOfDeliveryCalculation(payload)
    }

    /** Calculates a delivery exception. */
    override suspend fun calculateDeliveryException(request: CalculateDeliveryExceptionRequest): CalculateDeliveryExceptionResponse = handleCalculateDeliveryException(request)

    /** Compares a delivery exception. */
    override suspend fun compareDeliveryExceptionOptions(request: CompareDeliveryExceptionOptionsRequest): CompareDeliveryExceptionOptionsResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryExceptionCalculationService/CompareDeliveryExceptionOptions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CompareDeliveryExceptionOptions", request)
            log.atInfo().addKeyValue("operation", "CompareDeliveryExceptionOptions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.deliveryException.deliveryExceptionId).log("Recorded audit detail")
            val outbound0 = CompareDeliveryExceptionOptions0Command(
                context = request.context,
                detailsContactEmail = request.deliveryException.assignee.email,
                detailsContactPreferredChannel = request.deliveryException.requester.preferredChannel,
                detailsContactName = request.deliveryException.requester.name,
                detailsContactPhone = request.deliveryException.requester.phone
            ).toRequest()
            val reply0 = deliveriesProofOfDeliveryCalculationService.explainProofOfDeliveryCalculation(outbound0)
            compareDeliveryExceptionOptionsResponse {
                this.breakdown = CompareDeliveryExceptionOptionsResponseKt.breakdown {
                    this.currencyCode = request.details.currencyCode
                }
                this.deliveryException = deliveryException {
                    this.deliveryExceptionId = request.deliveryExceptionId
                }
                this.result = operationResult {
                    this.status = reply0.outcome.status
                }
            }
        }

    /** Estimates a delivery exception. */
    override suspend fun estimateDeliveryException(request: EstimateDeliveryExceptionRequest): EstimateDeliveryExceptionResponse {
        return handleEstimateDeliveryException(request)
    }

    /** Returns a delivery exception. */
    override suspend fun getDeliveryExceptionRates(request: GetDeliveryExceptionRatesRequest): GetDeliveryExceptionRatesResponse = handleGetDeliveryExceptionRates(request)

    /** Quotes a delivery exception. */
    override suspend fun quoteDeliveryException(request: QuoteDeliveryExceptionRequest): QuoteDeliveryExceptionResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryExceptionCalculationService/QuoteDeliveryException") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "QuoteDeliveryException", request)
            log.atInfo().addKeyValue("operation", "QuoteDeliveryException").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.deliveryException.deliveryExceptionId).log("Recorded audit detail")
            val outbound0 = listPerformersRequest {
                this.context = request.context
                this.filter = ListPerformersRequestKt.filter {
                    this.createdAfter = request.deliveryException.audit.updatedAt
                    this.region = request.details.destination.region
                }
            }
            val invoke0: suspend (ListPerformersRequest, Metadata) -> ListPerformersResponse = performersPerformerService::listPerformers
            val reply0 = invoke0(outbound0, Metadata())
            val outbound1 = CreateSearchIndexRequest.newBuilder().apply {
                setContext(request.context)
                setSearchIndex(searchIndex {
                    this.address = postalAddress {
                        this.postalCode = request.details.destination.postalCode
                    }
                    this.owner = userRef {
                        this.userId = request.deliveryException.assignee.userId
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> searchSearchIndexService.createSearchIndex(value) }
            quoteDeliveryExceptionResponse {
                this.deliveryException = deliveryException {
                    this.assignee = request.deliveryException.assignee
                    this.deliveryExceptionId = request.deliveryExceptionId
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.message = reply0.result.message
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    /** Simulates a delivery exception. */
    override suspend fun simulateDeliveryException(request: SimulateDeliveryExceptionRequest): SimulateDeliveryExceptionResponse {
        return handleSimulateDeliveryException(request)
    }

    private suspend fun handleCalculateDeliveryException(request: CalculateDeliveryExceptionRequest): CalculateDeliveryExceptionResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryExceptionCalculationService/CalculateDeliveryException") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CalculateDeliveryException", request)
            log.atInfo().addKeyValue("operation", "CalculateDeliveryException").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.deliveryException.deliveryExceptionId).log("Recorded audit detail")
            val outbound0 = summarizeVenuesRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.details = SummarizeVenuesRequestKt.details {
                    this.filters.putAll(request.deliveryException.customFieldsMap)
                }
                this.reason = request.reason
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = venuesVenueQueryService.summarizeVenues(envelope0.unpack(SummarizeVenuesRequest::class.java))
            val outbound1 = recalculateAdmissionsRequest {
                this.admission = admission {
                    this.subject = gateScanRef {
                        this.displayName = request.deliveryException.customer.displayName
                    }
                }
                this.context = request.context
                this.details = RecalculateAdmissionsRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(request.details.destination.linesList)
                        this.locality = request.details.destination.locality
                    }
                }
            }
            val reply1 = admissionsAdmissionCalculationService.recalculateAdmissions(WireCopies.json(outbound1))
            calculateDeliveryExceptionResponse {
                this.breakdown = CalculateDeliveryExceptionResponseKt.breakdown {
                    this.total = reply1.breakdown.total
                }
                this.deliveryException = deliveryException {
                    this.deliveryExceptionId = request.deliveryExceptionId
                    this.priority = request.deliveryException.priority
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private data class CompareDeliveryExceptionOptions0Command(
        val context: RequestContext,
        val detailsContactEmail: String,
        val detailsContactPreferredChannel: String,
        val detailsContactName: PersonName,
        val detailsContactPhone: String
    ) {
        fun toRequest(): ExplainProofOfDeliveryCalculationRequest {
            val command = this
            return explainProofOfDeliveryCalculationRequest {
                this.context = command.context
                this.details = ExplainProofOfDeliveryCalculationRequestKt.details {
                    this.contact = contactInfo {
                        this.email = command.detailsContactEmail
                        this.name = command.detailsContactName
                        this.phone = command.detailsContactPhone
                        this.preferredChannel = command.detailsContactPreferredChannel
                    }
                }
            }
        }
    }

    private suspend fun handleEstimateDeliveryException(request: EstimateDeliveryExceptionRequest): EstimateDeliveryExceptionResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryExceptionCalculationService/EstimateDeliveryException") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EstimateDeliveryException", request)
            log.atInfo().addKeyValue("operation", "EstimateDeliveryException").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.deliveryException.deliveryExceptionId).log("Recorded audit detail")
            val outbound0 = updateSearchIndexRequest {
                this.context = request.context
                this.searchIndex = searchIndex {
                    this.audit = auditInfo {
                        this.createdAt = request.deliveryException.audit.createdAt
                        this.etag = request.deliveryException.audit.etag
                        this.updatedAt = request.deliveryException.audit.updatedAt
                    }
                }
            }
            val command0: Submission<UpdateSearchIndexRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = searchSearchIndexService.updateSearchIndex(payload0)
            var reply1 = CalculateProofOfDeliveryResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.deliveryException.commentsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound1 = calculateProofOfDeliveryRequest {
                                this.context = request.context
                                this.proofOfDelivery = proofOfDelivery {
                                    this.audit = auditInfo {
                                        this.createdAt = item.createdAt
                                        this.createdBy = request.deliveryException.audit.createdBy
                                    }
                                    this.billingAddress = postalAddress {
                                        this.countryCode = request.details.destination.countryCode
                                    }
                                    this.counterparty = driverRef {
                                        this.email = item.authorEmail
                                    }
                                }
                            }
                            deliveriesProofOfDeliveryCalculationService.calculateProofOfDelivery(outbound1)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply1 = it }
                }
            }
            estimateDeliveryExceptionResponse {
                this.deliveryException = deliveryException {
                    this.comments.addAll(listOf(deliveryExceptionComment {
                        this.createdAt = reply0.searchIndex.audit.createdAt
                    }))
                    this.deliveryExceptionId = request.deliveryExceptionId
                    this.status = request.deliveryException.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleGetDeliveryExceptionRates(request: GetDeliveryExceptionRatesRequest): GetDeliveryExceptionRatesResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryExceptionCalculationService/GetDeliveryExceptionRates") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetDeliveryExceptionRates", request)
            log.atInfo().addKeyValue("operation", "GetDeliveryExceptionRates").log("Handling transport request")
            val outbound0 = explainProofOfDeliveryCalculationRequest {
                this.context = request.context
                this.details = ExplainProofOfDeliveryCalculationRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                    this.locale = request.context.locale
                }
                this.proofOfDelivery = proofOfDelivery {
                    this.audit = auditInfo {
                        this.createdAt = request.context.deadline
                    }
                    this.lines.addAll(listOf(proofOfDeliveryLine {
                        this.attributes.putAll(request.context.baggageMap)
                    }))
                }
            }
            val reply0 = invokeGetDeliveryExceptionRates0(outbound0)
            getDeliveryExceptionRatesResponse {
                this.deliveryException = deliveryException {
                    this.comments.addAll(listOf(deliveryExceptionComment {
                        this.createdAt = reply0.proofOfDelivery.audit.createdAt
                    }))
                    this.deliveryExceptionId = request.deliveryExceptionId
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSimulateDeliveryException(request: SimulateDeliveryExceptionRequest): SimulateDeliveryExceptionResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryExceptionCalculationService/SimulateDeliveryException") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SimulateDeliveryException", request)
            log.atInfo().addKeyValue("operation", "SimulateDeliveryException").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.deliveryException.deliveryExceptionId).log("Recorded audit detail")
            var reply0 = SearchNotificationsResponse.getDefaultInstance()
            request.deliveryException.commentsList.asFlow().map { item ->
                val outbound0 = searchNotificationsRequest {
                    this.context = request.context
                    this.filter = SearchNotificationsRequestKt.filter {
                        this.createdAfter = item.createdAt
                        this.region = request.details.destination.region
                    }
                }
                notificationsNotificationService.searchNotifications(outbound0)
            }.collect { reply0 = it }
            val outbound1 = estimateProofOfDeliveryRequest {
                this.context = request.context
                this.details = EstimateProofOfDeliveryRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(request.details.destination.linesList)
                        this.region = request.details.destination.region
                    }
                }
            }
            val reply1 = ClientRegistry.lookup("acme.deliveries.v1.ProofOfDeliveryCalculationService", ProofOfDeliveryCalculationServiceGrpcKt.ProofOfDeliveryCalculationServiceCoroutineStub::class.java).estimateProofOfDelivery(outbound1)
            simulateDeliveryExceptionResponse {
                this.breakdown = SimulateDeliveryExceptionResponseKt.breakdown {
                    this.currencyCode = request.details.currencyCode
                }
                this.deliveryException = deliveryException {
                    this.audit = auditInfo {
                        this.updatedAt = reply1.proofOfDelivery.audit.updatedAt
                    }
                    this.deliveryExceptionId = request.deliveryExceptionId
                    this.requester = contactInfo {
                        this.email = reply1.proofOfDelivery.counterparty.email
                    }
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }
}
