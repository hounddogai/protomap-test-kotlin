package com.acme.transport.fuel.adapters

import com.acme.audit.v1.AuditEventQueryServiceGrpcKt
import com.acme.audit.v1.AuditEventServiceGrpcKt
import com.acme.audit.v1.CreateAuditEventResponse
import com.acme.audit.v1.FindAuditEventsRequest
import com.acme.audit.v1.FindAuditEventsRequestKt
import com.acme.audit.v1.auditEvent
import com.acme.audit.v1.auditEventMeasurement
import com.acme.audit.v1.createAuditEventRequest
import com.acme.audit.v1.findAuditEventsRequest
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.customers.v1.customerRef
import com.acme.deliveries.v1.EstimateProofOfDeliveryRequest
import com.acme.deliveries.v1.EstimateProofOfDeliveryResponse
import com.acme.deliveries.v1.ProofOfDeliveryCalculationServiceGrpcKt
import com.acme.deliveries.v1.estimateProofOfDeliveryRequest
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.documents.v1.DocumentValidationServiceGrpcKt
import com.acme.documents.v1.NormalizeDocumentRequest
import com.acme.documents.v1.NormalizeDocumentRequestKt
import com.acme.documents.v1.NormalizeDocumentResponse
import com.acme.documents.v1.document
import com.acme.documents.v1.documentFolderRef
import com.acme.documents.v1.documentLine
import com.acme.documents.v1.normalizeDocumentRequest
import com.acme.drivers.v1.driverRef
import com.acme.fuel.v1.ExplainFuelTransactionDecisionRequest
import com.acme.fuel.v1.ExplainFuelTransactionDecisionResponse
import com.acme.fuel.v1.FuelCardCalculationServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionCalculationServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionMeasurement
import com.acme.fuel.v1.FuelTransactionValidationServiceGrpcKt
import com.acme.fuel.v1.GetFuelTransactionRulesRequest
import com.acme.fuel.v1.GetFuelTransactionRulesResponse
import com.acme.fuel.v1.NormalizeFuelTransactionRequest
import com.acme.fuel.v1.NormalizeFuelTransactionResponse
import com.acme.fuel.v1.PreviewFuelTransactionRequest
import com.acme.fuel.v1.PreviewFuelTransactionResponse
import com.acme.fuel.v1.ScoreFuelTransactionRequest
import com.acme.fuel.v1.ScoreFuelTransactionResponse
import com.acme.fuel.v1.ValidateFuelTransactionRequest
import com.acme.fuel.v1.ValidateFuelTransactionResponse
import com.acme.fuel.v1.compareFuelTransactionOptionsRequest
import com.acme.fuel.v1.estimateFuelCardRequest
import com.acme.fuel.v1.estimateFuelTransactionRequest
import com.acme.fuel.v1.explainFuelTransactionDecisionResponse
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.getFuelTransactionRulesResponse
import com.acme.fuel.v1.normalizeFuelTransactionResponse
import com.acme.fuel.v1.previewFuelTransactionResponse
import com.acme.fuel.v1.scoreFuelTransactionResponse
import com.acme.fuel.v1.validateFuelTransactionResponse
import com.acme.identity.v1.BrowseUsersRequestKt
import com.acme.identity.v1.BrowseUsersResponse
import com.acme.identity.v1.UserQueryServiceGrpcKt
import com.acme.identity.v1.browseUsersRequest
import com.acme.metrics.v1.MetricSeriesValidationServiceGrpcKt
import com.acme.metrics.v1.ValidateMetricSeriesResponse
import com.acme.metrics.v1.metricSeries
import com.acme.metrics.v1.metricSeriesDimensions
import com.acme.metrics.v1.validateMetricSeriesRequest
import com.acme.search.v1.ResolveSearchIndexesRequest
import com.acme.search.v1.SearchIndexQueryServiceGrpcKt
import com.acme.search.v1.resolveSearchIndexesRequest
import com.acme.transport.fuel.FuelRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.LoggerFactory

/** Checks fuel transactions against the rules before anything acts on them. */
class FuelTransactionValidationController(
    private val store: FuelRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventQueryService: AuditEventQueryServiceGrpcKt.AuditEventQueryServiceCoroutineStub,
    private val auditAuditEventService: AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub,
    private val deliveriesProofOfDeliveryCalculationService: ProofOfDeliveryCalculationServiceGrpcKt.ProofOfDeliveryCalculationServiceCoroutineStub,
    private val documentsDocumentValidationService: DocumentValidationServiceGrpcKt.DocumentValidationServiceCoroutineStub,
    private val fuelFuelCardCalculationService: FuelCardCalculationServiceGrpcKt.FuelCardCalculationServiceCoroutineStub,
    private val fuelFuelTransactionCalculationService: FuelTransactionCalculationServiceGrpcKt.FuelTransactionCalculationServiceCoroutineStub,
    private val identityUserQueryService: UserQueryServiceGrpcKt.UserQueryServiceCoroutineStub,
    private val metricsMetricSeriesValidationService: MetricSeriesValidationServiceGrpcKt.MetricSeriesValidationServiceCoroutineStub,
    private val searchSearchIndexQueryService: SearchIndexQueryServiceGrpcKt.SearchIndexQueryServiceCoroutineStub,
) : FuelTransactionValidationServiceGrpcKt.FuelTransactionValidationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(FuelTransactionValidationController::class.java)

    /** Explains a fuel transaction. */
    override suspend fun explainFuelTransactionDecision(request: ExplainFuelTransactionDecisionRequest): ExplainFuelTransactionDecisionResponse = handleExplainFuelTransactionDecision(request)

    /** Returns a fuel transaction. */
    override suspend fun getFuelTransactionRules(request: GetFuelTransactionRulesRequest): GetFuelTransactionRulesResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionValidationService/GetFuelTransactionRules") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetFuelTransactionRules", request)
            log.atInfo().addKeyValue("operation", "GetFuelTransactionRules").log("Handling transport request")
            val outbound0 = findAuditEventsRequest {
                this.context = request.context
                this.filter = FindAuditEventsRequestKt.filter {
                    this.createdAfter = request.context.deadline
                    this.ownerId = request.fuelTransactionId
                }
            }
            val command0: Submission<FindAuditEventsRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = auditAuditEventQueryService.findAuditEvents(payload0)
            getFuelTransactionRulesResponse {
                this.fuelTransaction = fuelTransaction {
                    this.correlationId = reply0.result.correlationId
                    this.fuelTransactionId = request.fuelTransactionId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Normalizes a fuel transaction. */
    override suspend fun normalizeFuelTransaction(request: NormalizeFuelTransactionRequest): NormalizeFuelTransactionResponse {
        return handleNormalizeFuelTransaction(request)
    }

    /** Previews a fuel transaction. */
    override suspend fun previewFuelTransaction(request: PreviewFuelTransactionRequest): PreviewFuelTransactionResponse = handlePreviewFuelTransaction(request)

    /** Scores a fuel transaction. */
    override suspend fun scoreFuelTransaction(request: ScoreFuelTransactionRequest): ScoreFuelTransactionResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionValidationService/ScoreFuelTransaction") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScoreFuelTransaction", request)
            log.atInfo().addKeyValue("operation", "ScoreFuelTransaction").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransaction.fuelTransactionId).log("Recorded audit detail")
            val outbound0 = ResolveSearchIndexesRequest.newBuilder().apply {
                setContext(request.context)
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> searchSearchIndexQueryService.resolveSearchIndexes(value) }
            scoreFuelTransactionResponse {
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                    this.subject = request.fuelTransaction.subject
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = reply0.result.status
                }
            }
        }

    /** Validates a fuel transaction. */
    override suspend fun validateFuelTransaction(request: ValidateFuelTransactionRequest): ValidateFuelTransactionResponse {
        return handleValidateFuelTransaction(request)
    }

    private suspend fun handleExplainFuelTransactionDecision(request: ExplainFuelTransactionDecisionRequest): ExplainFuelTransactionDecisionResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionValidationService/ExplainFuelTransactionDecision") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainFuelTransactionDecision", request)
            log.atInfo().addKeyValue("operation", "ExplainFuelTransactionDecision").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransaction.fuelTransactionId).log("Recorded audit detail")
            var reply0 = ValidateMetricSeriesResponse.getDefaultInstance()
            request.fuelTransaction.measurementsList.asFlow().map { item ->
                val outbound0 = validateMetricSeriesRequest {
                    this.context = request.context
                    this.metricSeries = metricSeries {
                        this.dimensions = metricSeriesDimensions {
                            this.unit = item.unit
                        }
                    }
                }
                metricsMetricSeriesValidationService.validateMetricSeries(outbound0)
            }.collect { reply0 = it }
            var reply1 = BrowseUsersResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<FuelTransactionMeasurement>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound1 = browseUsersRequest {
                            this.context = request.context
                            this.filter = BrowseUsersRequestKt.filter {
                                this.createdAfter = item.observedAt
                                this.ownerId = reply0.metricSeries.owner.userId
                                this.region = reply0.metricSeries.address.region
                            }
                        }
                        reply1 = identityUserQueryService.browseUsers(outbound1)
                    }
                }
                try {
                    request.fuelTransaction.measurementsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            explainFuelTransactionDecisionResponse {
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                    this.source = request.fuelTransaction.source
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleNormalizeFuelTransaction(request: NormalizeFuelTransactionRequest): NormalizeFuelTransactionResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionValidationService/NormalizeFuelTransaction") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "NormalizeFuelTransaction", request)
            log.atInfo().addKeyValue("operation", "NormalizeFuelTransaction").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransaction.fuelTransactionId).log("Recorded audit detail")
            val outbound0 = normalizeDocumentRequest {
                this.context = request.context
                this.details = NormalizeDocumentRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.fuelTransaction.subject.email
                    }
                }
                this.document = document {
                    this.counterparty = documentFolderRef {
                        this.displayName = request.fuelTransaction.subject.displayName
                    }
                    this.lines.addAll(listOf(documentLine {
                        this.attributes.putAll(request.fuelTransaction.attributesMap)
                    }))
                }
            }
            val adapter0 = OutboundPort<NormalizeDocumentRequest, NormalizeDocumentResponse> { payload ->
                documentsDocumentValidationService.normalizeDocument(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            var reply1 = CreateAuditEventResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.fuelTransaction.measurementsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound1 = createAuditEventRequest {
                                this.auditEvent = auditEvent {
                                    this.measurements.addAll(listOf(auditEventMeasurement {
                                        this.metric = item.metric
                                        this.observedAt = item.observedAt
                                        this.unit = item.unit
                                        this.value = item.value
                                    }))
                                }
                                this.context = request.context
                            }
                            auditAuditEventService.createAuditEvent(outbound1)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply1 = it }
                }
            }
            normalizeFuelTransactionResponse {
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                    this.occurredAt = request.fuelTransaction.occurredAt
                    this.subject = customerRef {
                        this.displayName = reply0.document.counterparty.displayName
                    }
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handlePreviewFuelTransaction(request: PreviewFuelTransactionRequest): PreviewFuelTransactionResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionValidationService/PreviewFuelTransaction") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PreviewFuelTransaction", request)
            log.atInfo().addKeyValue("operation", "PreviewFuelTransaction").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransaction.fuelTransactionId).log("Recorded audit detail")
            val outbound0 = estimateProofOfDeliveryRequest {
                this.context = request.context
                this.proofOfDelivery = proofOfDelivery {
                    this.counterparty = driverRef {
                        this.displayName = request.fuelTransaction.subject.displayName
                    }
                }
            }
            val invoke0: suspend (EstimateProofOfDeliveryRequest, Metadata) -> EstimateProofOfDeliveryResponse = deliveriesProofOfDeliveryCalculationService::estimateProofOfDelivery
            val reply0 = invoke0(outbound0, Metadata())
            previewFuelTransactionResponse {
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                    this.sequence = request.fuelTransaction.sequence
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleValidateFuelTransaction(request: ValidateFuelTransactionRequest): ValidateFuelTransactionResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionValidationService/ValidateFuelTransaction") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ValidateFuelTransaction", request)
            log.atInfo().addKeyValue("operation", "ValidateFuelTransaction").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransaction.fuelTransactionId).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                compareFuelTransactionOptionsRequest {
                    this.context = request.context
                    this.fuelTransaction = fuelTransaction {
                        this.occurredAt = request.fuelTransaction.occurredAt
                        this.subject = customerRef {
                            this.customerId = request.fuelTransaction.subject.customerId
                            this.displayName = request.fuelTransaction.subject.displayName
                        }
                    }
                }
            }
            val reply0 = fuelFuelTransactionCalculationService.compareFuelTransactionOptions(outbound0)
            val outbound1 = estimateFuelCardRequest {
                this.context = request.context
                this.fuelCard = fuelCard {
                    this.contact = contactInfo {
                        this.email = request.fuelTransaction.subject.email
                        this.phone = request.details.contact.phone
                    }
                }
            }
            val reply1 = ClientRegistry.lookup("acme.fuel.v1.FuelCardCalculationService", FuelCardCalculationServiceGrpcKt.FuelCardCalculationServiceCoroutineStub::class.java).estimateFuelCard(outbound1)
            val outbound2 = estimateFuelTransactionRequest {
                this.context = request.context
                this.fuelTransaction = fuelTransaction {
                    this.occurredAt = request.fuelTransaction.occurredAt
                    this.subject = customerRef {
                        this.customerId = reply0.fuelTransaction.subject.customerId
                        this.displayName = reply0.fuelTransaction.subject.displayName
                        this.email = reply1.fuelCard.contact.email
                    }
                }
            }
            val reply2 = fuelFuelTransactionCalculationService.estimateFuelTransaction(WireCopies.reflective(outbound2))
            validateFuelTransactionResponse {
                this.fuelTransaction = fuelTransaction {
                    this.correlationId = reply0.result.correlationId
                    this.fuelTransactionId = request.fuelTransactionId
                    this.occurredAt = request.fuelTransaction.occurredAt
                    this.subject = customerRef {
                        this.email = reply1.fuelCard.holder.email
                    }
                }
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }
}
