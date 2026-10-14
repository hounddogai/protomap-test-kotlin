package com.acme.transport.telematics.api

import com.acme.audit.v1.AuditEventQueryServiceGrpcKt
import com.acme.audit.v1.AuditEventServiceGrpcKt
import com.acme.audit.v1.BrowseAuditEventsRequestKt
import com.acme.audit.v1.CountAuditEventsRequestKt
import com.acme.audit.v1.SearchAuditEventsRequestKt
import com.acme.audit.v1.browseAuditEventsRequest
import com.acme.audit.v1.countAuditEventsRequest
import com.acme.audit.v1.searchAuditEventsRequest
import com.acme.authorization.v1.AccessPolicyQueryServiceGrpcKt
import com.acme.authorization.v1.resolveAccessPoliciesRequest
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.ExplainProofOfDeliveryDecisionRequest
import com.acme.deliveries.v1.ProofOfDeliveryValidationServiceGrpcKt
import com.acme.deliveries.v1.explainProofOfDeliveryDecisionRequest
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.documents.v1.DocumentValidationServiceGrpcKt
import com.acme.documents.v1.PreviewDocumentRequest
import com.acme.documents.v1.ValidateDocumentResponse
import com.acme.documents.v1.document
import com.acme.documents.v1.documentLine
import com.acme.documents.v1.previewDocumentRequest
import com.acme.documents.v1.validateDocumentRequest
import com.acme.featureflags.v1.ExportFeatureFlagsRequestKt
import com.acme.featureflags.v1.FeatureFlagQueryServiceGrpcKt
import com.acme.featureflags.v1.exportFeatureFlagsRequest
import com.acme.fuel.v1.CountFuelCardsRequest
import com.acme.fuel.v1.CountFuelCardsRequestKt
import com.acme.fuel.v1.CountFuelCardsResponse
import com.acme.fuel.v1.ExportFuelCardsRequestKt
import com.acme.fuel.v1.FuelCardQueryServiceGrpcKt
import com.acme.fuel.v1.FuelCardValidationServiceGrpcKt
import com.acme.fuel.v1.NormalizeFuelCardRequestKt
import com.acme.fuel.v1.countFuelCardsRequest
import com.acme.fuel.v1.exportFuelCardsRequest
import com.acme.fuel.v1.normalizeFuelCardRequest
import com.acme.identity.v1.userRef
import com.acme.ratelimiting.v1.ExportRateLimitsRequestKt
import com.acme.ratelimiting.v1.FindRateLimitsRequestKt
import com.acme.ratelimiting.v1.RateLimitQueryServiceGrpcKt
import com.acme.ratelimiting.v1.copy as ratelimitingCopy
import com.acme.ratelimiting.v1.exportRateLimitsRequest
import com.acme.ratelimiting.v1.findRateLimitsRequest
import com.acme.telematics.v1.CalculateTelemetryReadingRequest
import com.acme.telematics.v1.CalculateTelemetryReadingResponse
import com.acme.telematics.v1.CompareTelemetryReadingOptionsRequest
import com.acme.telematics.v1.CompareTelemetryReadingOptionsResponse
import com.acme.telematics.v1.EstimateTelemetryReadingRequest
import com.acme.telematics.v1.EstimateTelemetryReadingResponse
import com.acme.telematics.v1.ExplainTelemetryReadingCalculationRequest
import com.acme.telematics.v1.ExplainTelemetryReadingCalculationResponse
import com.acme.telematics.v1.ExplainTelemetryReadingCalculationResponseKt
import com.acme.telematics.v1.GetTelemetryReadingRatesRequest
import com.acme.telematics.v1.GetTelemetryReadingRatesResponse
import com.acme.telematics.v1.RecalculateTelemetryReadingsRequest
import com.acme.telematics.v1.RecalculateTelemetryReadingsResponse
import com.acme.telematics.v1.SimulateTelemetryReadingRequest
import com.acme.telematics.v1.SimulateTelemetryReadingResponse
import com.acme.telematics.v1.TelemetryReadingCalculationServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingMeasurement
import com.acme.telematics.v1.calculateTelemetryReadingResponse
import com.acme.telematics.v1.compareTelemetryReadingOptionsResponse
import com.acme.telematics.v1.estimateTelemetryReadingResponse
import com.acme.telematics.v1.explainTelemetryReadingCalculationResponse
import com.acme.telematics.v1.getTelemetryReadingRatesResponse
import com.acme.telematics.v1.recalculateTelemetryReadingsResponse
import com.acme.telematics.v1.simulateTelemetryReadingResponse
import com.acme.telematics.v1.telemetryReading
import com.acme.telematics.v1.telemetryReadingMeasurement
import com.acme.tours.v1.CountToursRequestKt
import com.acme.tours.v1.TourQueryServiceGrpcKt
import com.acme.tours.v1.countToursRequest
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.telematics.TelematicsRepository
import com.acme.yards.v1.CheckYardEligibilityResponse
import com.acme.yards.v1.DockCalculationServiceGrpcKt
import com.acme.yards.v1.EstimateDockRequestKt
import com.acme.yards.v1.EstimateDockResponse
import com.acme.yards.v1.ExplainYardDecisionRequest
import com.acme.yards.v1.ExplainYardDecisionResponse
import com.acme.yards.v1.YardValidationServiceGrpcKt
import com.acme.yards.v1.checkYardEligibilityRequest
import com.acme.yards.v1.dock
import com.acme.yards.v1.dockDimensions
import com.acme.yards.v1.estimateDockRequest
import com.acme.yards.v1.explainYardDecisionRequest
import com.acme.yards.v1.yard
import com.acme.yards.v1.yardDimensions
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

/** Computes the figures of a telemetry reading, such as totals, taxes, and estimates. */
class TelemetryReadingCalculationGrpcService(
    private val store: TelematicsRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventQueryService: AuditEventQueryServiceGrpcKt.AuditEventQueryServiceCoroutineStub,
    private val auditAuditEventService: AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub,
    private val authorizationAccessPolicyQueryService: AccessPolicyQueryServiceGrpcKt.AccessPolicyQueryServiceCoroutineStub,
    private val deliveriesProofOfDeliveryValidationService: ProofOfDeliveryValidationServiceGrpcKt.ProofOfDeliveryValidationServiceCoroutineStub,
    private val documentsDocumentValidationService: DocumentValidationServiceGrpcKt.DocumentValidationServiceCoroutineStub,
    private val featureflagsFeatureFlagQueryService: FeatureFlagQueryServiceGrpcKt.FeatureFlagQueryServiceCoroutineStub,
    private val fuelFuelCardQueryService: FuelCardQueryServiceGrpcKt.FuelCardQueryServiceCoroutineStub,
    private val fuelFuelCardValidationService: FuelCardValidationServiceGrpcKt.FuelCardValidationServiceCoroutineStub,
    private val ratelimitingRateLimitQueryService: RateLimitQueryServiceGrpcKt.RateLimitQueryServiceCoroutineStub,
    private val toursTourQueryService: TourQueryServiceGrpcKt.TourQueryServiceCoroutineStub,
    private val yardsDockCalculationService: DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub,
    private val yardsYardValidationService: YardValidationServiceGrpcKt.YardValidationServiceCoroutineStub,
) : TelemetryReadingCalculationServiceGrpcKt.TelemetryReadingCalculationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(TelemetryReadingCalculationGrpcService::class.java)

    private val workerForEstimateTelemetryReading2 = ReplyMailbox<ExplainYardDecisionRequest, ExplainYardDecisionResponse>(workers) { payload ->
        withTimeout(5_000) { yardsYardValidationService.explainYardDecision(payload) }
    }

    private val clientForEstimateTelemetryReading3 by lazy { auditAuditEventService }

    /** Calculates a telemetry reading. */
    override suspend fun calculateTelemetryReading(request: CalculateTelemetryReadingRequest): CalculateTelemetryReadingResponse {
        return handleCalculateTelemetryReading(request)
    }

    /** Compares a telemetry reading. */
    override suspend fun compareTelemetryReadingOptions(request: CompareTelemetryReadingOptionsRequest): CompareTelemetryReadingOptionsResponse = handleCompareTelemetryReadingOptions(request)

    /** Estimates a telemetry reading. */
    override suspend fun estimateTelemetryReading(request: EstimateTelemetryReadingRequest): EstimateTelemetryReadingResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingCalculationService/EstimateTelemetryReading") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EstimateTelemetryReading", request)
            log.atInfo().addKeyValue("operation", "EstimateTelemetryReading").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReading.telemetryReadingId).log("Recorded audit detail")
            val outbound0 = estimateDockRequest {
                this.context = request.context
                this.details = EstimateDockRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.details.destination.region
                    }
                }
            }
            val reply0 = ClientRegistry.lookup("acme.yards.v1.DockCalculationService", DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub::class.java).estimateDock(outbound0)
            val outbound1 = exportRateLimitsRequest {
                this.context = request.context
                this.filter = ExportRateLimitsRequestKt.filter {
                    this.createdAfter = reply0.dock.audit.createdAt
                    this.region = request.details.destination.region
                }
            }
            ratelimitingRateLimitQueryService.exportRateLimits(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound2 = explainYardDecisionRequest {
                this.context = request.context
                this.yard = yard {
                    this.address = postalAddress {
                        this.recipient = request.details.destination.recipient
                    }
                    this.dimensions = yardDimensions {
                        this.weight = reply0.dock.dimensions.weight
                    }
                }
            }
            val reply2 = workerForEstimateTelemetryReading2.submit(outbound2)
            val outbound3 = searchAuditEventsRequest {
                this.context = request.context
                this.filter = SearchAuditEventsRequestKt.filter {
                    this.createdAfter = reply2.yard.audit.updatedAt
                    this.region = request.details.destination.region
                }
            }
            val reply3 = clientForEstimateTelemetryReading3.searchAuditEvents(outbound3)
            estimateTelemetryReadingResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.correlationId = reply3.result.correlationId
                    this.measurements.addAll(listOf(telemetryReadingMeasurement {
                        this.unit = reply0.dock.dimensions.unit
                    }))
                    this.source = request.telemetryReading.source
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    /** Explains a telemetry reading. */
    override suspend fun explainTelemetryReadingCalculation(request: ExplainTelemetryReadingCalculationRequest): ExplainTelemetryReadingCalculationResponse {
        return handleExplainTelemetryReadingCalculation(request)
    }

    /** Returns a telemetry reading. */
    override suspend fun getTelemetryReadingRates(request: GetTelemetryReadingRatesRequest): GetTelemetryReadingRatesResponse = handleGetTelemetryReadingRates(request)

    /** Recalculates a telemetry reading. */
    override suspend fun recalculateTelemetryReadings(request: RecalculateTelemetryReadingsRequest): RecalculateTelemetryReadingsResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingCalculationService/RecalculateTelemetryReadings") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecalculateTelemetryReadings", request)
            log.atInfo().addKeyValue("operation", "RecalculateTelemetryReadings").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReading.telemetryReadingId).log("Recorded audit detail")
            val outbound0 = request.toExplainProofOfDeliveryDecisionFromRecalculateTelemetryReadings0()
            val reply0 = deliveriesProofOfDeliveryValidationService.explainProofOfDeliveryDecision(outbound0)
            val outbound1 = browseAuditEventsRequest {
                this.context = request.context
                this.filter = BrowseAuditEventsRequestKt.filter {
                    this.region = request.details.destination.region
                }
            }
            val reply1 = auditAuditEventQueryService.browseAuditEvents(outbound1)
            recalculateTelemetryReadingsResponse {
                this.result = operationResult {
                    this.correlationId = request.telemetryReading.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    /** Simulates a telemetry reading. */
    override suspend fun simulateTelemetryReading(request: SimulateTelemetryReadingRequest): SimulateTelemetryReadingResponse {
        return handleSimulateTelemetryReading(request)
    }

    private suspend fun handleCalculateTelemetryReading(request: CalculateTelemetryReadingRequest): CalculateTelemetryReadingResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingCalculationService/CalculateTelemetryReading") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CalculateTelemetryReading", request)
            log.atInfo().addKeyValue("operation", "CalculateTelemetryReading").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReading.telemetryReadingId).log("Recorded audit detail")
            val outbound0 = previewDocumentRequest {
                this.context = request.context
                this.document = document {
                    this.billingAddress = postalAddress {
                        this.locality = request.details.destination.locality
                    }
                }
            }
            val command0: Submission<PreviewDocumentRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = documentsDocumentValidationService.previewDocument(payload0)
            var reply1 = EstimateDockResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<TelemetryReadingMeasurement>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound1 = estimateDockRequest {
                            this.context = request.context
                            this.dock = dock {
                                this.audit = auditInfo {
                                    this.createdAt = reply0.document.audit.createdAt
                                }
                                this.dimensions = dockDimensions {
                                    this.unit = item.unit
                                }
                            }
                        }
                        reply1 = yardsDockCalculationService.estimateDock(outbound1)
                    }
                }
                try {
                    request.telemetryReading.measurementsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            calculateTelemetryReadingResponse {
                this.result = operationResult {
                    this.correlationId = request.telemetryReading.correlationId
                    this.status = reply0.result.status
                }
                this.telemetryReading = telemetryReading {
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    private suspend fun handleCompareTelemetryReadingOptions(request: CompareTelemetryReadingOptionsRequest): CompareTelemetryReadingOptionsResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingCalculationService/CompareTelemetryReadingOptions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CompareTelemetryReadingOptions", request)
            log.atInfo().addKeyValue("operation", "CompareTelemetryReadingOptions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReading.kind).log("Recorded audit detail")
            val outbound0 = exportFeatureFlagsRequest {
                this.context = request.context
                this.filter = ExportFeatureFlagsRequestKt.filter {
                    this.createdAfter = request.telemetryReading.occurredAt
                    this.region = request.details.destination.region
                }
            }
            featureflagsFeatureFlagQueryService.exportFeatureFlags(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = countFuelCardsRequest {
                this.context = request.context
                this.filter = CountFuelCardsRequestKt.filter {
                    this.createdAfter = request.telemetryReading.occurredAt
                    this.ownerId = request.telemetryReading.telemetryReadingId
                    this.region = request.details.destination.region
                }
            }
            val invoke1: suspend (CountFuelCardsRequest, Metadata) -> CountFuelCardsResponse = fuelFuelCardQueryService::countFuelCards
            val reply1 = invoke1(outbound1, Metadata())
            coroutineScope {
                val permits = Semaphore(8)
                request.telemetryReading.measurementsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound2 = exportFuelCardsRequest {
                                this.context = request.context
                                this.filter = ExportFuelCardsRequestKt.filter {
                                    this.createdAfter = item.observedAt
                                    this.ownerId = request.telemetryReading.subject.geoFenceId
                                    this.region = request.details.destination.region
                                }
                            }
                            fuelFuelCardQueryService.exportFuelCards(outbound2).collect { event ->
                                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
                            }
                        } }
                    }.awaitAll()
                }
            }
            var reply3 = ValidateDocumentResponse.getDefaultInstance()
            request.telemetryReading.measurementsList.asFlow().map { item ->
                val outbound3 = validateDocumentRequest {
                    this.context = request.context
                    this.document = document {
                        this.audit = auditInfo {
                            this.updatedAt = item.observedAt
                        }
                        this.billingAddress = postalAddress {
                            this.lines.addAll(request.details.destination.linesList)
                            this.recipient = request.details.destination.recipient
                        }
                        this.lines.addAll(listOf(documentLine {
                            this.attributes.putAll(request.telemetryReading.attributesMap)
                        }))
                    }
                }
                documentsDocumentValidationService.validateDocument(outbound3)
            }.collect { reply3 = it }
            compareTelemetryReadingOptionsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.correlationId = reply1.result.correlationId
                    this.source = request.telemetryReading.source
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    private suspend fun handleExplainTelemetryReadingCalculation(request: ExplainTelemetryReadingCalculationRequest): ExplainTelemetryReadingCalculationResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingCalculationService/ExplainTelemetryReadingCalculation") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainTelemetryReadingCalculation", request)
            log.atInfo().addKeyValue("operation", "ExplainTelemetryReadingCalculation").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReading.telemetryReadingId).log("Recorded audit detail")
            val outbound0 = countToursRequest {
                this.context = request.context
                this.filter = CountToursRequestKt.filter {
                    this.createdAfter = request.telemetryReading.occurredAt
                    this.ownerId = request.telemetryReadingId
                }
            }
            val reply0 = flowOf(outbound0).map { value -> toursTourQueryService.countTours(value) }.single()
            val outbound1 = countAuditEventsRequest {
                this.context = request.context
                this.filter = CountAuditEventsRequestKt.filter {
                    this.labels.putAll(reply0.result.detailsMap)
                    this.ownerId = request.telemetryReading.subject.geoFenceId
                }
            }
            val reply1 = withTimeout(5_000) { auditAuditEventQueryService.countAuditEvents(outbound1) }
            val configure2: NormalizeFuelCardRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.details = NormalizeFuelCardRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.details.contact.email
                    }
                }
            }
            val outbound2 = normalizeFuelCardRequest(configure2)
            val reply2 = fuelFuelCardValidationService.normalizeFuelCard(outbound2)
            val outbound3 = resolveAccessPoliciesRequest {
                this.context = request.context
            }
            val reply3 = authorizationAccessPolicyQueryService.resolveAccessPolicies(outbound3)
            explainTelemetryReadingCalculationResponse {
                this.outcome = ExplainTelemetryReadingCalculationResponseKt.outcome {
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.referenceIds.addAll((((reply0.result.referenceIdsList) + (reply1.result.referenceIdsList)) + (reply2.result.referenceIdsList)) + (reply3.result.referenceIdsList))
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.correlationId = reply0.result.correlationId
                    this.kind = request.telemetryReading.kind
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    private suspend fun handleGetTelemetryReadingRates(request: GetTelemetryReadingRatesRequest): GetTelemetryReadingRatesResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingCalculationService/GetTelemetryReadingRates") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetTelemetryReadingRates", request)
            log.atInfo().addKeyValue("operation", "GetTelemetryReadingRates").log("Handling transport request")
            val outbound0 = browseAuditEventsRequest {
                this.context = request.context
                this.filter = BrowseAuditEventsRequestKt.filter {
                    this.ownerId = request.telemetryReadingId
                }
            }
            val reply0 = auditAuditEventQueryService.browseAuditEvents(outbound0)
            val outbound1 = findRateLimitsRequest {}.ratelimitingCopy {
                this.context = request.context
                this.filter = FindRateLimitsRequestKt.filter {
                    this.createdAfter = request.context.deadline
                    this.ownerId = reply0.result.correlationId
                }
            }
            val reply1 = ratelimitingRateLimitQueryService.findRateLimits(outbound1)
            getTelemetryReadingRatesResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = reply1.result.status
                }
                this.telemetryReading = telemetryReading {
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    private fun RecalculateTelemetryReadingsRequest.toExplainProofOfDeliveryDecisionFromRecalculateTelemetryReadings0(): ExplainProofOfDeliveryDecisionRequest {
        val source = this
        return explainProofOfDeliveryDecisionRequest {
            this.context = source.context
            this.proofOfDelivery = proofOfDelivery {
                this.billingAddress = postalAddress {
                    this.lines.addAll(source.details.destination.linesList)
                    this.postalCode = source.details.destination.postalCode
                }
            }
        }
    }

    private suspend fun handleSimulateTelemetryReading(request: SimulateTelemetryReadingRequest): SimulateTelemetryReadingResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingCalculationService/SimulateTelemetryReading") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SimulateTelemetryReading", request)
            log.atInfo().addKeyValue("operation", "SimulateTelemetryReading").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReading.telemetryReadingId).log("Recorded audit detail")
            val outbound0 = previewDocumentRequest {
                this.context = request.context
                this.document = document {
                    this.billingAddress = postalAddress {
                        this.locality = request.details.destination.locality
                    }
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = documentsDocumentValidationService.previewDocument(envelope0.unpack(PreviewDocumentRequest::class.java))
            var reply1 = CheckYardEligibilityResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<TelemetryReadingMeasurement>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound1 = checkYardEligibilityRequest {
                            this.context = request.context
                            this.yard = yard {
                                this.address = postalAddress {
                                    this.locality = request.details.destination.locality
                                }
                                this.audit = auditInfo {
                                    this.updatedAt = reply0.document.audit.updatedAt
                                }
                                this.dimensions = yardDimensions {
                                    this.unit = item.unit
                                }
                                this.owner = userRef {
                                    this.displayName = reply0.document.counterparty.displayName
                                }
                            }
                        }
                        reply1 = yardsYardValidationService.checkYardEligibility(outbound1)
                    }
                }
                try {
                    request.telemetryReading.measurementsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            simulateTelemetryReadingResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.correlationId = reply0.result.correlationId
                    this.source = request.telemetryReading.source
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }
}
