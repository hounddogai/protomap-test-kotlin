package com.acme.transport.telematics.api

import com.acme.authorization.v1.AccessPolicyQueryServiceGrpcKt
import com.acme.authorization.v1.ExportAccessPoliciesRequestKt
import com.acme.authorization.v1.exportAccessPoliciesRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.featureflags.v1.CountFeatureFlagsRequestKt
import com.acme.featureflags.v1.FeatureFlagQueryServiceGrpcKt
import com.acme.featureflags.v1.copy as featureflagsCopy
import com.acme.featureflags.v1.countFeatureFlagsRequest
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.BatchGetWorkOrdersRequestKt
import com.acme.maintenance.v1.LintServiceSchedulesRequest
import com.acme.maintenance.v1.LintServiceSchedulesResponse
import com.acme.maintenance.v1.PreviewServiceScheduleResponse
import com.acme.maintenance.v1.ServiceScheduleValidationServiceGrpcKt
import com.acme.maintenance.v1.WorkOrderServiceGrpcKt
import com.acme.maintenance.v1.batchGetWorkOrdersRequest
import com.acme.maintenance.v1.lintServiceSchedulesRequest
import com.acme.maintenance.v1.previewServiceScheduleRequest
import com.acme.maintenance.v1.serviceSchedule
import com.acme.maintenance.v1.serviceScheduleParticipant
import com.acme.maintenance.v1.validateServiceScheduleRequest
import com.acme.metrics.v1.MetricSeriesServiceGrpcKt
import com.acme.metrics.v1.SearchMetricSeriesesRequest
import com.acme.metrics.v1.SearchMetricSeriesesRequestKt
import com.acme.metrics.v1.searchMetricSeriesesRequest
import com.acme.performers.v1.PerformerQueryServiceGrpcKt
import com.acme.performers.v1.SuggestPerformersRequest
import com.acme.performers.v1.SuggestPerformersRequestKt
import com.acme.performers.v1.suggestPerformersRequest
import com.acme.telematics.v1.CalculateGeoFenceRequest
import com.acme.telematics.v1.CalculateGeoFenceResponse
import com.acme.telematics.v1.CompareGeoFenceOptionsRequest
import com.acme.telematics.v1.CompareGeoFenceOptionsResponse
import com.acme.telematics.v1.CompareGeoFenceOptionsResponseKt
import com.acme.telematics.v1.CompareTelemetryReadingOptionsRequest
import com.acme.telematics.v1.CompareTelemetryReadingOptionsRequestKt
import com.acme.telematics.v1.CompareTelemetryReadingOptionsResponse
import com.acme.telematics.v1.EstimateGeoFenceRequest
import com.acme.telematics.v1.EstimateGeoFenceResponse
import com.acme.telematics.v1.EstimateGeoFenceResponseKt
import com.acme.telematics.v1.EstimateTelemetryReadingRequest
import com.acme.telematics.v1.EstimateTelemetryReadingRequestKt
import com.acme.telematics.v1.EstimateTelemetryReadingResponse
import com.acme.telematics.v1.ExplainGeoFenceCalculationRequest
import com.acme.telematics.v1.ExplainGeoFenceCalculationResponse
import com.acme.telematics.v1.ExplainGeoFenceCalculationResponseKt
import com.acme.telematics.v1.ExplainTelemetryReadingCalculationRequest
import com.acme.telematics.v1.ExplainTelemetryReadingCalculationResponse
import com.acme.telematics.v1.GeoFenceCalculationServiceGrpcKt
import com.acme.telematics.v1.GetGeoFenceRatesRequest
import com.acme.telematics.v1.GetGeoFenceRatesResponse
import com.acme.telematics.v1.QuoteGeoFenceRequest
import com.acme.telematics.v1.QuoteGeoFenceResponse
import com.acme.telematics.v1.RecalculateGeoFencesRequest
import com.acme.telematics.v1.RecalculateGeoFencesResponse
import com.acme.telematics.v1.RecalculateGeoFencesResponseKt
import com.acme.telematics.v1.SimulateGeoFenceRequest
import com.acme.telematics.v1.SimulateGeoFenceResponse
import com.acme.telematics.v1.SimulateTelemetryReadingRequest
import com.acme.telematics.v1.SimulateTelemetryReadingRequestKt
import com.acme.telematics.v1.TelemetryReadingCalculationServiceGrpcKt
import com.acme.telematics.v1.calculateGeoFenceResponse
import com.acme.telematics.v1.calculateTelemetryReadingRequest
import com.acme.telematics.v1.compareGeoFenceOptionsResponse
import com.acme.telematics.v1.compareTelemetryReadingOptionsRequest
import com.acme.telematics.v1.estimateGeoFenceResponse
import com.acme.telematics.v1.estimateTelemetryReadingRequest
import com.acme.telematics.v1.explainGeoFenceCalculationResponse
import com.acme.telematics.v1.explainTelemetryReadingCalculationRequest
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.geoFenceRef
import com.acme.telematics.v1.getGeoFenceRatesResponse
import com.acme.telematics.v1.quoteGeoFenceResponse
import com.acme.telematics.v1.recalculateGeoFencesResponse
import com.acme.telematics.v1.recalculateTelemetryReadingsRequest
import com.acme.telematics.v1.simulateGeoFenceResponse
import com.acme.telematics.v1.simulateTelemetryReadingRequest
import com.acme.telematics.v1.telemetryReading
import com.acme.telematics.v1.telemetryReadingMeasurement
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.transport.telematics.TelematicsRepository
import com.acme.yards.v1.CountYardsRequestKt
import com.acme.yards.v1.YardQueryServiceGrpcKt
import com.acme.yards.v1.countYardsRequest
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Computes the figures of a geo fence, such as totals, taxes, and estimates. */
class GeoFenceCalculationGrpcService(
    private val store: TelematicsRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyQueryService: AccessPolicyQueryServiceGrpcKt.AccessPolicyQueryServiceCoroutineStub,
    private val featureflagsFeatureFlagQueryService: FeatureFlagQueryServiceGrpcKt.FeatureFlagQueryServiceCoroutineStub,
    private val maintenanceServiceScheduleValidationService: ServiceScheduleValidationServiceGrpcKt.ServiceScheduleValidationServiceCoroutineStub,
    private val maintenanceWorkOrderService: WorkOrderServiceGrpcKt.WorkOrderServiceCoroutineStub,
    private val metricsMetricSeriesService: MetricSeriesServiceGrpcKt.MetricSeriesServiceCoroutineStub,
    private val performersPerformerQueryService: PerformerQueryServiceGrpcKt.PerformerQueryServiceCoroutineStub,
    private val telematicsTelemetryReadingCalculationService: TelemetryReadingCalculationServiceGrpcKt.TelemetryReadingCalculationServiceCoroutineStub,
    private val yardsYardQueryService: YardQueryServiceGrpcKt.YardQueryServiceCoroutineStub,
) : GeoFenceCalculationServiceGrpcKt.GeoFenceCalculationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(GeoFenceCalculationGrpcService::class.java)

    private val invokeCompareGeoFenceOptions1: suspend (CompareTelemetryReadingOptionsRequest) -> CompareTelemetryReadingOptionsResponse = { payload ->
        telematicsTelemetryReadingCalculationService.compareTelemetryReadingOptions(payload)
    }

    private val workerForExplainGeoFenceCalculation0 = ReplyMailbox<ExplainTelemetryReadingCalculationRequest, ExplainTelemetryReadingCalculationResponse>(workers) { payload ->
        withTimeout(5_000) { telematicsTelemetryReadingCalculationService.explainTelemetryReadingCalculation(payload) }
    }

    private val clientForExplainGeoFenceCalculation1 by lazy { telematicsTelemetryReadingCalculationService }

    /** Calculates a geo fence. */
    override suspend fun calculateGeoFence(request: CalculateGeoFenceRequest): CalculateGeoFenceResponse {
        return handleCalculateGeoFence(request)
    }

    /** Compares a geo fence. */
    override suspend fun compareGeoFenceOptions(request: CompareGeoFenceOptionsRequest): CompareGeoFenceOptionsResponse = handleCompareGeoFenceOptions(request)

    /** Estimates a geo fence. */
    override suspend fun estimateGeoFence(request: EstimateGeoFenceRequest): EstimateGeoFenceResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceCalculationService/EstimateGeoFence") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EstimateGeoFence", request)
            log.atInfo().addKeyValue("operation", "EstimateGeoFence").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.geoFence.geoFenceId).log("Recorded audit detail")
            val outbound0 = CompareTelemetryReadingOptionsRequest.newBuilder().apply {
                setContext(request.context)
                setTelemetryReading(telemetryReading {
                    this.subject = geoFenceRef {
                        this.displayName = request.geoFence.owner.displayName
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> telematicsTelemetryReadingCalculationService.compareTelemetryReadingOptions(value) }
            val outbound1 = RequestFactory.create {
                estimateTelemetryReadingRequest {
                    this.context = request.context
                    this.details = EstimateTelemetryReadingRequestKt.details {
                        this.destination = postalAddress {
                            this.locality = request.details.destination.locality
                            this.postalCode = request.details.destination.postalCode
                        }
                    }
                    this.telemetryReading = telemetryReading {
                        this.subject = geoFenceRef {
                            this.geoFenceId = reply0.telemetryReading.subject.geoFenceId
                        }
                    }
                }
            }
            val reply1 = telematicsTelemetryReadingCalculationService.estimateTelemetryReading(outbound1)
            val outbound2 = simulateTelemetryReadingRequest {
                this.context = request.context
                this.details = SimulateTelemetryReadingRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(request.details.destination.linesList)
                    }
                }
            }
            val reply2 = ClientRegistry.lookup("acme.telematics.v1.TelemetryReadingCalculationService", TelemetryReadingCalculationServiceGrpcKt.TelemetryReadingCalculationServiceCoroutineStub::class.java).simulateTelemetryReading(outbound2)
            val outbound3 = countYardsRequest {
                this.context = request.context
                this.filter = CountYardsRequestKt.filter {
                    this.createdAfter = reply1.telemetryReading.occurredAt
                    this.ownerId = reply2.telemetryReading.subject.geoFenceId
                    this.region = request.details.destination.region
                }
            }
            val reply3 = yardsYardQueryService.countYards(WireCopies.reflective(outbound3))
            estimateGeoFenceResponse {
                this.breakdown = EstimateGeoFenceResponseKt.breakdown {
                    this.currencyCode = reply0.breakdown.currencyCode
                    this.total = reply0.breakdown.total
                }
                this.geoFence = geoFence {
                    this.geoFenceId = request.geoFenceId
                    this.status = request.geoFence.status
                    this.version = reply1.telemetryReading.subject.version
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply3.result.referenceIdsList)
                    this.status = reply2.result.status
                }
            }
        }

    /** Explains a geo fence. */
    override suspend fun explainGeoFenceCalculation(request: ExplainGeoFenceCalculationRequest): ExplainGeoFenceCalculationResponse {
        return handleExplainGeoFenceCalculation(request)
    }

    /** Returns a geo fence. */
    override suspend fun getGeoFenceRates(request: GetGeoFenceRatesRequest): GetGeoFenceRatesResponse = handleGetGeoFenceRates(request)

    /** Quotes a geo fence. */
    override suspend fun quoteGeoFence(request: QuoteGeoFenceRequest): QuoteGeoFenceResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceCalculationService/QuoteGeoFence") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "QuoteGeoFence", request)
            log.atInfo().addKeyValue("operation", "QuoteGeoFence").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.geoFence.geoFenceId).log("Recorded audit detail")
            val outbound0 = validateServiceScheduleRequest {
                this.context = request.context
                this.serviceSchedule = serviceSchedule {
                    this.organizer = userRef {
                        this.displayName = request.geoFence.owner.displayName
                    }
                    this.participants.addAll(listOf(serviceScheduleParticipant {
                        this.contact = contactInfo {
                            this.email = request.geoFence.owner.email
                        }
                    }))
                }
            }
            val reply0 = withTimeout(5_000) { maintenanceServiceScheduleValidationService.validateServiceSchedule(outbound0) }
            val configure1: BatchGetWorkOrdersRequestKt.Dsl.() -> Unit = {
                this.context = request.context
            }
            val outbound1 = batchGetWorkOrdersRequest(configure1)
            val reply1 = maintenanceWorkOrderService.batchGetWorkOrders(outbound1)
            quoteGeoFenceResponse {
                this.geoFence = geoFence {
                    this.effective = request.geoFence.effective
                    this.geoFenceId = request.geoFenceId
                    this.owner = userRef {
                        this.displayName = reply0.serviceSchedule.organizer.displayName
                    }
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = reply1.result.status
                }
            }
        }

    /** Recalculates a geo fence. */
    override suspend fun recalculateGeoFences(request: RecalculateGeoFencesRequest): RecalculateGeoFencesResponse {
        return handleRecalculateGeoFences(request)
    }

    /** Simulates a geo fence. */
    override suspend fun simulateGeoFence(request: SimulateGeoFenceRequest): SimulateGeoFenceResponse = handleSimulateGeoFence(request)

    private data class CalculateGeoFence0Command(
        val context: RequestContext,
        val detailsDestinationPostalCode: String,
        val telemetryReadingSubjectGeoFenceId: String
    ) {
        fun toRequest(): SimulateTelemetryReadingRequest {
            val command = this
            return simulateTelemetryReadingRequest {
                this.context = command.context
                this.details = SimulateTelemetryReadingRequestKt.details {
                    this.destination = postalAddress {
                        this.postalCode = command.detailsDestinationPostalCode
                    }
                }
                this.telemetryReading = telemetryReading {
                    this.subject = geoFenceRef {
                        this.geoFenceId = command.telemetryReadingSubjectGeoFenceId
                    }
                }
            }
        }
    }

    private suspend fun handleCalculateGeoFence(request: CalculateGeoFenceRequest): CalculateGeoFenceResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceCalculationService/CalculateGeoFence") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CalculateGeoFence", request)
            log.atInfo().addKeyValue("operation", "CalculateGeoFence").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.geoFence.description).log("Recorded audit detail")
            val outbound0 = CalculateGeoFence0Command(
                context = request.context,
                detailsDestinationPostalCode = request.details.destination.postalCode,
                telemetryReadingSubjectGeoFenceId = request.geoFence.geoFenceId
            ).toRequest()
            val reply0 = telematicsTelemetryReadingCalculationService.simulateTelemetryReading(outbound0)
            val outbound1 = suggestPerformersRequest {
                this.context = request.context
                this.filter = SuggestPerformersRequestKt.filter {
                    this.region = request.details.destination.region
                }
            }
            val command1: Submission<SuggestPerformersRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = performersPerformerQueryService.suggestPerformers(payload1)
            calculateGeoFenceResponse {
                this.geoFence = geoFence {
                    this.geoFenceId = request.geoFenceId
                    this.name = request.geoFence.name
                }
                this.result = operationResult {
                    this.correlationId = reply0.telemetryReading.correlationId
                    this.message = reply1.result.message
                    this.status = reply1.result.status
                }
            }
        }

    private suspend fun handleCompareGeoFenceOptions(request: CompareGeoFenceOptionsRequest): CompareGeoFenceOptionsResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceCalculationService/CompareGeoFenceOptions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CompareGeoFenceOptions", request)
            log.atInfo().addKeyValue("operation", "CompareGeoFenceOptions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.geoFence.geoFenceId).log("Recorded audit detail")
            val outbound0 = estimateTelemetryReadingRequest {
                this.context = request.context
                this.details = EstimateTelemetryReadingRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(request.details.destination.linesList)
                        this.recipient = request.details.destination.recipient
                    }
                }
            }
            val adapter0 = OutboundPort<EstimateTelemetryReadingRequest, EstimateTelemetryReadingResponse> { payload ->
                telematicsTelemetryReadingCalculationService.estimateTelemetryReading(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = compareTelemetryReadingOptionsRequest {
                this.context = request.context
                this.details = CompareTelemetryReadingOptionsRequestKt.details {
                    this.destination = postalAddress {
                        this.postalCode = request.details.destination.postalCode
                    }
                }
                this.telemetryReading = telemetryReading {
                    this.subject = geoFenceRef {
                        this.displayName = reply0.telemetryReading.subject.displayName
                        this.version = request.geoFence.version
                    }
                }
            }
            val reply1 = invokeCompareGeoFenceOptions1(outbound1)
            val outbound2 = lintServiceSchedulesRequest {
                this.context = request.context
                this.serviceSchedule = serviceSchedule {
                    this.participants.addAll(listOf(serviceScheduleParticipant {
                        this.contact = contactInfo {
                            this.email = request.geoFence.owner.email
                        }
                    }))
                }
            }
            val invoke2: suspend (LintServiceSchedulesRequest, Metadata) -> LintServiceSchedulesResponse = maintenanceServiceScheduleValidationService::lintServiceSchedules
            val reply2 = invoke2(outbound2, Metadata())
            compareGeoFenceOptionsResponse {
                this.breakdown = CompareGeoFenceOptionsResponseKt.breakdown {
                    this.currencyCode = reply1.breakdown.currencyCode
                }
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.updatedBy = reply2.serviceSchedule.audit.updatedBy
                    }
                    this.geoFenceId = request.geoFenceId
                    this.owner = userRef {
                        this.displayName = reply0.telemetryReading.subject.displayName
                    }
                    this.priority = request.geoFence.priority
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleExplainGeoFenceCalculation(request: ExplainGeoFenceCalculationRequest): ExplainGeoFenceCalculationResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceCalculationService/ExplainGeoFenceCalculation") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainGeoFenceCalculation", request)
            log.atInfo().addKeyValue("operation", "ExplainGeoFenceCalculation").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.geoFenceId).log("Recorded audit detail")
            val outbound0 = explainTelemetryReadingCalculationRequest {
                this.context = request.context
                this.telemetryReading = telemetryReading {
                    this.subject = geoFenceRef {
                        this.displayName = request.geoFence.owner.displayName
                    }
                }
            }
            val reply0 = workerForExplainGeoFenceCalculation0.submit(outbound0)
            val outbound1 = calculateTelemetryReadingRequest {
                this.context = request.context
                this.telemetryReading = telemetryReading {
                    this.kind = reply0.telemetryReading.kind
                    this.subject = geoFenceRef {
                        this.displayName = request.geoFence.owner.displayName
                        this.geoFenceId = request.geoFence.geoFenceId
                        this.version = request.geoFence.version
                    }
                }
            }
            val reply1 = clientForExplainGeoFenceCalculation1.calculateTelemetryReading(outbound1)
            explainGeoFenceCalculationResponse {
                this.geoFence = geoFence {
                    this.effective = request.geoFence.effective
                    this.geoFenceId = reply1.telemetryReading.subject.geoFenceId
                    this.priority = request.geoFence.priority
                }
                this.outcome = ExplainGeoFenceCalculationResponseKt.outcome {
                    this.message = reply0.outcome.message
                }
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleGetGeoFenceRates(request: GetGeoFenceRatesRequest): GetGeoFenceRatesResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceCalculationService/GetGeoFenceRates") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetGeoFenceRates", request)
            log.atInfo().addKeyValue("operation", "GetGeoFenceRates").log("Handling transport request")
            val outbound0 = recalculateTelemetryReadingsRequest {
                this.context = request.context
                this.telemetryReading = telemetryReading {
                    this.attributes.putAll(request.context.baggageMap)
                    this.measurements.addAll(listOf(telemetryReadingMeasurement {
                        this.observedAt = request.context.deadline
                    }))
                    this.source = request.context.actor.userId
                    this.subject = geoFenceRef {
                        this.geoFenceId = request.geoFenceId
                    }
                }
            }
            val reply0 = flowOf(outbound0).map { value -> telematicsTelemetryReadingCalculationService.recalculateTelemetryReadings(value) }.single()
            getGeoFenceRatesResponse {
                this.geoFence = geoFence {
                    this.geoFenceId = request.geoFenceId
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private fun RecalculateGeoFencesRequest.toSearchMetricSeriesesFromRecalculateGeoFences3(reply0: PreviewServiceScheduleResponse): SearchMetricSeriesesRequest {
        val source = this
        return searchMetricSeriesesRequest {
            this.context = source.context
            this.filter = SearchMetricSeriesesRequestKt.filter {
                this.createdAfter = reply0.serviceSchedule.audit.createdAt
                this.region = source.details.destination.region
            }
        }
    }

    private suspend fun handleRecalculateGeoFences(request: RecalculateGeoFencesRequest): RecalculateGeoFencesResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceCalculationService/RecalculateGeoFences") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecalculateGeoFences", request)
            log.atInfo().addKeyValue("operation", "RecalculateGeoFences").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.geoFence.geoFenceId).log("Recorded audit detail")
            val outbound0 = previewServiceScheduleRequest {
                this.context = request.context
                this.serviceSchedule = serviceSchedule {
                    this.audit = auditInfo {
                        this.createdAt = request.geoFence.audit.createdAt
                        this.createdBy = request.geoFence.audit.createdBy
                    }
                    this.participants.addAll(listOf(serviceScheduleParticipant {
                        this.contact = contactInfo {
                            this.email = request.geoFence.owner.email
                        }
                    }))
                }
            }
            val reply0 = maintenanceServiceScheduleValidationService.previewServiceSchedule(outbound0)
            val outbound1 = exportAccessPoliciesRequest {
                this.context = request.context
                this.filter = ExportAccessPoliciesRequestKt.filter {
                    this.createdAfter = request.geoFence.audit.updatedAt
                    this.labels.putAll(reply0.result.detailsMap)
                    this.region = reply0.serviceSchedule.location.region
                }
            }
            authorizationAccessPolicyQueryService.exportAccessPolicies(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound2 = countFeatureFlagsRequest {}.featureflagsCopy {
                this.context = request.context
                this.filter = CountFeatureFlagsRequestKt.filter {
                    this.region = reply0.serviceSchedule.location.region
                }
            }
            val reply2 = featureflagsFeatureFlagQueryService.countFeatureFlags(outbound2)
            val outbound3 = request.toSearchMetricSeriesesFromRecalculateGeoFences3(reply0)
            val reply3 = metricsMetricSeriesService.searchMetricSerieses(outbound3)
            recalculateGeoFencesResponse {
                this.breakdown = RecalculateGeoFencesResponseKt.breakdown {
                    this.currencyCode = request.details.currencyCode
                }
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.serviceSchedule.audit.updatedBy
                    }
                    this.geoFenceId = request.geoFenceId
                    this.owner = userRef {
                        this.email = reply0.serviceSchedule.organizer.email
                    }
                }
                this.result = operationResult {
                    this.correlationId = reply3.result.correlationId
                    this.status = reply2.result.status
                }
            }
        }

    private suspend fun handleSimulateGeoFence(request: SimulateGeoFenceRequest): SimulateGeoFenceResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceCalculationService/SimulateGeoFence") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SimulateGeoFence", request)
            log.atInfo().addKeyValue("operation", "SimulateGeoFence").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.geoFence.geoFenceId).log("Recorded audit detail")
            val outbound0 = previewServiceScheduleRequest {
                this.context = request.context
                this.serviceSchedule = serviceSchedule {
                    this.participants.addAll(listOf(serviceScheduleParticipant {
                        this.contact = contactInfo {
                            this.email = request.geoFence.owner.email
                        }
                    }))
                }
            }
            val reply0 = maintenanceServiceScheduleValidationService.previewServiceSchedule(outbound0)
            val outbound1 = simulateTelemetryReadingRequest {
                this.context = request.context
                this.details = SimulateTelemetryReadingRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(reply0.serviceSchedule.location.linesList)
                        this.locality = request.details.destination.locality
                        this.postalCode = reply0.serviceSchedule.location.postalCode
                    }
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = telematicsTelemetryReadingCalculationService.simulateTelemetryReading(envelope1.unpack(SimulateTelemetryReadingRequest::class.java))
            simulateGeoFenceResponse {
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.serviceSchedule.audit.updatedAt
                    }
                    this.description = request.geoFence.description
                    this.geoFenceId = request.geoFenceId
                    this.owner = userRef {
                        this.displayName = reply1.telemetryReading.subject.displayName
                    }
                }
                this.result = operationResult {
                    this.correlationId = reply1.telemetryReading.correlationId
                    this.status = "ok"
                }
            }
        }
}
