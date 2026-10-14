package com.acme.transport.telematics.adapters

import com.acme.authorization.v1.AccessPolicyQueryServiceGrpcKt
import com.acme.authorization.v1.resolveAccessPoliciesRequest
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.deliveries.v1.DeliveryExceptionCalculationServiceGrpcKt
import com.acme.deliveries.v1.FindProofOfDeliveriesRequestKt
import com.acme.deliveries.v1.FindProofOfDeliveriesResponse
import com.acme.deliveries.v1.ProofOfDeliveryQueryServiceGrpcKt
import com.acme.deliveries.v1.QuoteDeliveryExceptionRequest
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.deliveryExceptionComment
import com.acme.deliveries.v1.findProofOfDeliveriesRequest
import com.acme.deliveries.v1.quoteDeliveryExceptionRequest
import com.acme.fuel.v1.FuelPolicyCalculationServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyValidationServiceGrpcKt
import com.acme.fuel.v1.LintFuelPoliciesRequest
import com.acme.fuel.v1.LintFuelPoliciesRequestKt
import com.acme.fuel.v1.SimulateFuelPolicyResponse
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.fuelPolicyRouteAction
import com.acme.fuel.v1.lintFuelPoliciesRequest
import com.acme.fuel.v1.simulateFuelPolicyRequest
import com.acme.identity.v1.userRef
import com.acme.search.v1.SearchIndexValidationServiceGrpcKt
import com.acme.search.v1.explainSearchIndexDecisionRequest
import com.acme.search.v1.searchIndex
import com.acme.telematics.v1.CalculateGeoFenceResponse
import com.acme.telematics.v1.CheckTelemetryReadingEligibilityRequest
import com.acme.telematics.v1.CheckTelemetryReadingEligibilityResponse
import com.acme.telematics.v1.EstimateGeoFenceRequest
import com.acme.telematics.v1.ExplainTelemetryReadingDecisionRequest
import com.acme.telematics.v1.ExplainTelemetryReadingDecisionResponse
import com.acme.telematics.v1.ExplainTelemetryReadingDecisionResponseKt
import com.acme.telematics.v1.GeoFenceCalculationServiceGrpcKt
import com.acme.telematics.v1.GetGeoFenceRatesRequest
import com.acme.telematics.v1.GetGeoFenceRatesRequestKt
import com.acme.telematics.v1.GetGeoFenceRatesResponse
import com.acme.telematics.v1.GetTelemetryReadingRulesRequest
import com.acme.telematics.v1.GetTelemetryReadingRulesResponse
import com.acme.telematics.v1.NormalizeTelemetryReadingRequest
import com.acme.telematics.v1.NormalizeTelemetryReadingResponse
import com.acme.telematics.v1.NormalizeTelemetryReadingResponseKt
import com.acme.telematics.v1.PreviewTelemetryReadingRequest
import com.acme.telematics.v1.PreviewTelemetryReadingResponse
import com.acme.telematics.v1.PreviewTelemetryReadingResponseKt
import com.acme.telematics.v1.ScoreTelemetryReadingRequest
import com.acme.telematics.v1.ScoreTelemetryReadingResponse
import com.acme.telematics.v1.SimulateGeoFenceRequest
import com.acme.telematics.v1.SimulateGeoFenceResponse
import com.acme.telematics.v1.TelemetryReadingCalculationServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingMeasurement
import com.acme.telematics.v1.TelemetryReadingValidationServiceGrpcKt
import com.acme.telematics.v1.ValidateTelemetryReadingRequest
import com.acme.telematics.v1.ValidateTelemetryReadingResponse
import com.acme.telematics.v1.ValidateTelemetryReadingResponseKt
import com.acme.telematics.v1.calculateGeoFenceRequest
import com.acme.telematics.v1.calculateTelemetryReadingRequest
import com.acme.telematics.v1.checkTelemetryReadingEligibilityResponse
import com.acme.telematics.v1.compareGeoFenceOptionsRequest
import com.acme.telematics.v1.estimateGeoFenceRequest
import com.acme.telematics.v1.estimateTelemetryReadingRequest
import com.acme.telematics.v1.explainTelemetryReadingDecisionResponse
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.geoFenceRef
import com.acme.telematics.v1.geoFenceRouteAction
import com.acme.telematics.v1.getGeoFenceRatesRequest
import com.acme.telematics.v1.getTelemetryReadingRulesResponse
import com.acme.telematics.v1.normalizeTelemetryReadingResponse
import com.acme.telematics.v1.previewTelemetryReadingResponse
import com.acme.telematics.v1.recalculateGeoFencesRequest
import com.acme.telematics.v1.scoreTelemetryReadingResponse
import com.acme.telematics.v1.simulateGeoFenceRequest
import com.acme.telematics.v1.telemetryReading
import com.acme.telematics.v1.validateTelemetryReadingResponse
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.telematics.TelematicsRepository
import com.acme.yards.v1.YardMove
import com.acme.yards.v1.YardMoveCalculationServiceGrpcKt
import com.acme.yards.v1.quoteYardMoveRequest
import com.acme.yards.v1.yardMove
import com.acme.yards.v1.yardRef
import com.google.protobuf.Any as ProtoAny
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

/** Checks telemetry readings against the rules before anything acts on them. */
class TelemetryReadingValidationController(
    private val store: TelematicsRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyQueryService: AccessPolicyQueryServiceGrpcKt.AccessPolicyQueryServiceCoroutineStub,
    private val deliveriesDeliveryExceptionCalculationService: DeliveryExceptionCalculationServiceGrpcKt.DeliveryExceptionCalculationServiceCoroutineStub,
    private val deliveriesProofOfDeliveryQueryService: ProofOfDeliveryQueryServiceGrpcKt.ProofOfDeliveryQueryServiceCoroutineStub,
    private val fuelFuelPolicyCalculationService: FuelPolicyCalculationServiceGrpcKt.FuelPolicyCalculationServiceCoroutineStub,
    private val fuelFuelPolicyValidationService: FuelPolicyValidationServiceGrpcKt.FuelPolicyValidationServiceCoroutineStub,
    private val searchSearchIndexValidationService: SearchIndexValidationServiceGrpcKt.SearchIndexValidationServiceCoroutineStub,
    private val telematicsGeoFenceCalculationService: GeoFenceCalculationServiceGrpcKt.GeoFenceCalculationServiceCoroutineStub,
    private val telematicsTelemetryReadingCalculationService: TelemetryReadingCalculationServiceGrpcKt.TelemetryReadingCalculationServiceCoroutineStub,
    private val yardsYardMoveCalculationService: YardMoveCalculationServiceGrpcKt.YardMoveCalculationServiceCoroutineStub,
) : TelemetryReadingValidationServiceGrpcKt.TelemetryReadingValidationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(TelemetryReadingValidationController::class.java)

    private val invokeCheckTelemetryReadingEligibility0: suspend (GetGeoFenceRatesRequest) -> GetGeoFenceRatesResponse = { payload ->
        telematicsGeoFenceCalculationService.getGeoFenceRates(payload)
    }

    private val workerForNormalizeTelemetryReading1 = ReplyMailbox<SimulateGeoFenceRequest, SimulateGeoFenceResponse>(workers) { payload ->
        withTimeout(5_000) { telematicsGeoFenceCalculationService.simulateGeoFence(payload) }
    }

    private val clientForNormalizeTelemetryReading2 by lazy { fuelFuelPolicyCalculationService }

    /** Checks a telemetry reading. */
    override suspend fun checkTelemetryReadingEligibility(request: CheckTelemetryReadingEligibilityRequest): CheckTelemetryReadingEligibilityResponse = handleCheckTelemetryReadingEligibility(request)

    /** Explains a telemetry reading. */
    override suspend fun explainTelemetryReadingDecision(request: ExplainTelemetryReadingDecisionRequest): ExplainTelemetryReadingDecisionResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingValidationService/ExplainTelemetryReadingDecision") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainTelemetryReadingDecision", request)
            log.atInfo().addKeyValue("operation", "ExplainTelemetryReadingDecision").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReading.telemetryReadingId).log("Recorded audit detail")
            var reply0 = SimulateFuelPolicyResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<TelemetryReadingMeasurement>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound0 = simulateFuelPolicyRequest {
                            this.context = request.context
                            this.fuelPolicy = fuelPolicy {
                                this.audit = auditInfo {
                                    this.updatedAt = item.observedAt
                                }
                                this.version = request.telemetryReading.subject.version
                            }
                        }
                        reply0 = fuelFuelPolicyCalculationService.simulateFuelPolicy(outbound0)
                    }
                }
                try {
                    request.telemetryReading.measurementsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound1 = LintFuelPoliciesRequest.newBuilder().apply {
                setContext(request.context)
                setDetails(LintFuelPoliciesRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.details.contact.email
                    }
                })
                setFuelPolicy(fuelPolicy {
                    this.audit = auditInfo {
                        this.createdAt = reply0.fuelPolicy.audit.createdAt
                    }
                    this.owner = userRef {
                        this.displayName = reply0.fuelPolicy.owner.displayName
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> fuelFuelPolicyValidationService.lintFuelPolicies(value) }
            val outbound2 = RequestFactory.create {
                estimateTelemetryReadingRequest {
                    this.context = request.context
                    this.telemetryReading = telemetryReading {
                        this.correlationId = reply0.result.correlationId
                        this.subject = geoFenceRef {
                            this.displayName = reply0.fuelPolicy.owner.displayName
                            this.version = request.telemetryReading.subject.version
                        }
                    }
                }
            }
            val reply2 = telematicsTelemetryReadingCalculationService.estimateTelemetryReading(outbound2)
            explainTelemetryReadingDecisionResponse {
                this.outcome = ExplainTelemetryReadingDecisionResponseKt.outcome {
                    this.status = reply1.outcome.status
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.occurredAt = request.telemetryReading.occurredAt
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    /** Returns a telemetry reading. */
    override suspend fun getTelemetryReadingRules(request: GetTelemetryReadingRulesRequest): GetTelemetryReadingRulesResponse {
        return handleGetTelemetryReadingRules(request)
    }

    /** Normalizes a telemetry reading. */
    override suspend fun normalizeTelemetryReading(request: NormalizeTelemetryReadingRequest): NormalizeTelemetryReadingResponse = handleNormalizeTelemetryReading(request)

    /** Previews a telemetry reading. */
    override suspend fun previewTelemetryReading(request: PreviewTelemetryReadingRequest): PreviewTelemetryReadingResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingValidationService/PreviewTelemetryReading") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PreviewTelemetryReading", request)
            log.atInfo().addKeyValue("operation", "PreviewTelemetryReading").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReading.telemetryReadingId).log("Recorded audit detail")
            val outbound0 = quoteYardMoveRequest {
                this.context = request.context
                this.yardMove = yardMove {
                    this.assignee = userRef {
                        this.email = request.details.contact.email
                    }
                    this.customer = yardRef {
                        this.displayName = request.telemetryReading.subject.displayName
                    }
                    this.requester = contactInfo {
                        this.name = request.details.contact.name
                        this.phone = request.details.contact.phone
                    }
                }
            }
            val reply0 = flowOf(outbound0).map { value -> yardsYardMoveCalculationService.quoteYardMove(value) }.single()
            val outbound1 = explainSearchIndexDecisionRequest {
                this.context = request.context
                this.searchIndex = searchIndex {
                    this.audit = auditInfo {
                        this.createdAt = reply0.yardMove.audit.createdAt
                    }
                    this.owner = userRef {
                        this.displayName = when (reply0.yardMove.originCase) {
                            YardMove.OriginCase.CUSTOMER -> reply0.yardMove.customer.displayName
                            else -> ""
                        }
                        this.email = request.details.contact.email
                    }
                }
            }
            val reply1 = searchSearchIndexValidationService.explainSearchIndexDecision(outbound1)
            val configure2: GetGeoFenceRatesRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.geoFenceId = request.telemetryReading.subject.geoFenceId
            }
            val outbound2 = getGeoFenceRatesRequest(configure2)
            val reply2 = telematicsGeoFenceCalculationService.getGeoFenceRates(outbound2)
            val outbound3 = recalculateGeoFencesRequest {
                this.context = request.context
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.updatedBy = reply2.geoFence.audit.updatedBy
                    }
                    this.owner = userRef {
                        this.email = reply2.geoFence.owner.email
                    }
                }
            }
            val reply3 = telematicsGeoFenceCalculationService.recalculateGeoFences(outbound3)
            previewTelemetryReadingResponse {
                this.outcome = PreviewTelemetryReadingResponseKt.outcome {
                    this.nextSteps.addAll(reply1.outcome.nextStepsList)
                }
                this.result = operationResult {
                    this.message = reply2.result.message
                    this.status = reply0.result.status
                }
                this.telemetryReading = telemetryReading {
                    this.occurredAt = request.telemetryReading.occurredAt
                    this.subject = geoFenceRef {
                        this.displayName = reply3.geoFence.owner.displayName
                    }
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    /** Scores a telemetry reading. */
    override suspend fun scoreTelemetryReading(request: ScoreTelemetryReadingRequest): ScoreTelemetryReadingResponse {
        return handleScoreTelemetryReading(request)
    }

    /** Validates a telemetry reading. */
    override suspend fun validateTelemetryReading(request: ValidateTelemetryReadingRequest): ValidateTelemetryReadingResponse = handleValidateTelemetryReading(request)

    private suspend fun handleCheckTelemetryReadingEligibility(request: CheckTelemetryReadingEligibilityRequest): CheckTelemetryReadingEligibilityResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingValidationService/CheckTelemetryReadingEligibility") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CheckTelemetryReadingEligibility", request)
            log.atInfo().addKeyValue("operation", "CheckTelemetryReadingEligibility").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReading.telemetryReadingId).log("Recorded audit detail")
            val outbound0 = getGeoFenceRatesRequest {
                this.context = request.context
                this.geoFenceId = request.telemetryReading.subject.geoFenceId
            }
            val reply0 = invokeCheckTelemetryReadingEligibility0(outbound0)
            checkTelemetryReadingEligibilityResponse {
                this.result = operationResult {
                    this.status = reply0.result.status
                }
                this.telemetryReading = telemetryReading {
                    this.correlationId = request.telemetryReading.correlationId
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    private suspend fun handleGetTelemetryReadingRules(request: GetTelemetryReadingRulesRequest): GetTelemetryReadingRulesResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingValidationService/GetTelemetryReadingRules") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetTelemetryReadingRules", request)
            log.atInfo().addKeyValue("operation", "GetTelemetryReadingRules").log("Handling transport request")
            val outbound0 = compareGeoFenceOptionsRequest {
                this.context = request.context
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.updatedAt = request.context.deadline
                    }
                    this.owner = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                    this.scopes.addAll(request.context.actor.rolesList)
                }
            }
            val reply0 = ClientRegistry.lookup("acme.telematics.v1.GeoFenceCalculationService", GeoFenceCalculationServiceGrpcKt.GeoFenceCalculationServiceCoroutineStub::class.java).compareGeoFenceOptions(outbound0)
            getTelemetryReadingRulesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.correlationId = reply0.result.correlationId
                    this.subject = geoFenceRef {
                        this.version = reply0.geoFence.version
                    }
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    private suspend fun handleNormalizeTelemetryReading(request: NormalizeTelemetryReadingRequest): NormalizeTelemetryReadingResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingValidationService/NormalizeTelemetryReading") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "NormalizeTelemetryReading", request)
            log.atInfo().addKeyValue("operation", "NormalizeTelemetryReading").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReading.telemetryReadingId).log("Recorded audit detail")
            var reply0 = CalculateGeoFenceResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.telemetryReading.measurementsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound0 = calculateGeoFenceRequest {
                                this.context = request.context
                                this.geoFence = geoFence {
                                    this.audit = auditInfo {
                                        this.createdAt = item.observedAt
                                    }
                                    this.geoFenceId = request.telemetryReading.subject.geoFenceId
                                    this.version = request.telemetryReading.subject.version
                                }
                            }
                            telematicsGeoFenceCalculationService.calculateGeoFence(outbound0)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply0 = it }
                }
            }
            val outbound1 = simulateGeoFenceRequest {
                this.context = request.context
                this.geoFence = geoFence {
                    this.route = geoFenceRouteAction {
                        this.target = reply0.geoFence.route.target
                    }
                }
            }
            val reply1 = workerForNormalizeTelemetryReading1.submit(outbound1)
            val outbound2 = simulateFuelPolicyRequest {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.createdAt = reply0.geoFence.audit.createdAt
                        this.updatedAt = reply0.geoFence.audit.updatedAt
                    }
                    this.route = fuelPolicyRouteAction {
                        this.weight = reply0.geoFence.route.weight
                    }
                }
            }
            val reply2 = clientForNormalizeTelemetryReading2.simulateFuelPolicy(outbound2)
            normalizeTelemetryReadingResponse {
                this.outcome = NormalizeTelemetryReadingResponseKt.outcome {
                    this.message = reply2.result.message
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.occurredAt = request.telemetryReading.occurredAt
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    private suspend fun handleScoreTelemetryReading(request: ScoreTelemetryReadingRequest): ScoreTelemetryReadingResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingValidationService/ScoreTelemetryReading") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScoreTelemetryReading", request)
            log.atInfo().addKeyValue("operation", "ScoreTelemetryReading").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReading.telemetryReadingId).log("Recorded audit detail")
            val outbound0 = calculateTelemetryReadingRequest {
                this.context = request.context
                this.telemetryReading = telemetryReading {
                    this.subject = geoFenceRef {
                        this.displayName = request.telemetryReading.subject.displayName
                    }
                }
            }
            val reply0 = telematicsTelemetryReadingCalculationService.calculateTelemetryReading(outbound0)
            var reply1 = FindProofOfDeliveriesResponse.getDefaultInstance()
            request.telemetryReading.measurementsList.asFlow().map { item ->
                val outbound1 = findProofOfDeliveriesRequest {
                    this.context = request.context
                    this.filter = FindProofOfDeliveriesRequestKt.filter {
                        this.createdAfter = item.observedAt
                        this.ownerId = request.telemetryReading.subject.geoFenceId
                        this.region = request.details.destination.region
                    }
                }
                deliveriesProofOfDeliveryQueryService.findProofOfDeliveries(outbound1)
            }.collect { reply1 = it }
            scoreTelemetryReadingResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.occurredAt = request.telemetryReading.occurredAt
                    this.source = reply0.telemetryReading.source
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    private fun ValidateTelemetryReadingRequest.toEstimateGeoFenceFromValidateTelemetryReading0(): EstimateGeoFenceRequest {
        val source = this
        return estimateGeoFenceRequest {
            this.context = source.context
            this.geoFence = geoFence {
                this.owner = userRef {
                    this.displayName = source.telemetryReading.subject.displayName
                    this.email = source.details.contact.email
                }
                this.version = source.telemetryReading.subject.version
            }
            this.geoFenceId = source.telemetryReading.subject.geoFenceId
        }
    }

    private suspend fun handleValidateTelemetryReading(request: ValidateTelemetryReadingRequest): ValidateTelemetryReadingResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingValidationService/ValidateTelemetryReading") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ValidateTelemetryReading", request)
            log.atInfo().addKeyValue("operation", "ValidateTelemetryReading").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReading.telemetryReadingId).log("Recorded audit detail")
            val outbound0 = request.toEstimateGeoFenceFromValidateTelemetryReading0()
            val reply0 = telematicsGeoFenceCalculationService.estimateGeoFence(outbound0)
            val outbound1 = resolveAccessPoliciesRequest {
                this.context = request.context
            }
            val reply1 = authorizationAccessPolicyQueryService.resolveAccessPolicies(outbound1)
            val outbound2 = quoteDeliveryExceptionRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.assignee = userRef {
                        this.userId = reply0.geoFence.owner.userId
                    }
                    this.audit = auditInfo {
                        this.createdBy = reply0.geoFence.audit.createdBy
                        this.etag = reply0.geoFence.audit.etag
                    }
                    this.comments.addAll(listOf(deliveryExceptionComment {
                        this.createdAt = reply0.geoFence.audit.createdAt
                    }))
                }
            }
            val envelope2 = ProtoAny.pack(outbound2)
            val reply2 = deliveriesDeliveryExceptionCalculationService.quoteDeliveryException(envelope2.unpack(QuoteDeliveryExceptionRequest::class.java))
            validateTelemetryReadingResponse {
                this.outcome = ValidateTelemetryReadingResponseKt.outcome {
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.attributes.putAll(request.telemetryReading.attributesMap)
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }
}
