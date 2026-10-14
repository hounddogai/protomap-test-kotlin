package com.acme.transport.dispatch.adapters

import com.acme.admissions.v1.AdmissionServiceGrpcKt
import com.acme.admissions.v1.admission
import com.acme.admissions.v1.cloneAdmissionRequest
import com.acme.admissions.v1.gateScanRef
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.ExplainProofOfDeliveryDecisionResponse
import com.acme.deliveries.v1.ProofOfDeliveryValidationServiceGrpcKt
import com.acme.deliveries.v1.explainProofOfDeliveryDecisionRequest
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.dispatch.v1.CalculateDispatchAssignmentRequest
import com.acme.dispatch.v1.CalculateDispatchAssignmentResponse
import com.acme.dispatch.v1.CalculateZoneRequest
import com.acme.dispatch.v1.CalculateZoneResponse
import com.acme.dispatch.v1.CalculateZoneResponseKt
import com.acme.dispatch.v1.CompareZoneOptionsRequest
import com.acme.dispatch.v1.CompareZoneOptionsResponse
import com.acme.dispatch.v1.DispatchAssignment
import com.acme.dispatch.v1.DispatchAssignmentCalculationServiceGrpcKt
import com.acme.dispatch.v1.DispatchOrderValidationServiceGrpcKt
import com.acme.dispatch.v1.EstimateDispatchAssignmentRequest
import com.acme.dispatch.v1.EstimateZoneRequest
import com.acme.dispatch.v1.EstimateZoneResponse
import com.acme.dispatch.v1.EstimateZoneResponseKt
import com.acme.dispatch.v1.ExplainDispatchAssignmentCalculationRequest
import com.acme.dispatch.v1.ExplainDispatchAssignmentCalculationResponse
import com.acme.dispatch.v1.ExplainDispatchOrderDecisionRequest
import com.acme.dispatch.v1.ExplainDispatchOrderDecisionRequestKt
import com.acme.dispatch.v1.ExplainZoneCalculationRequest
import com.acme.dispatch.v1.ExplainZoneCalculationResponse
import com.acme.dispatch.v1.GetZoneRatesRequest
import com.acme.dispatch.v1.GetZoneRatesResponse
import com.acme.dispatch.v1.QuoteDispatchAssignmentRequest
import com.acme.dispatch.v1.QuoteDispatchAssignmentRequestKt
import com.acme.dispatch.v1.QuoteZoneRequest
import com.acme.dispatch.v1.QuoteZoneResponse
import com.acme.dispatch.v1.QuoteZoneResponseKt
import com.acme.dispatch.v1.RecalculateDispatchAssignmentsRequestKt
import com.acme.dispatch.v1.RecalculateZonesRequest
import com.acme.dispatch.v1.RecalculateZonesResponse
import com.acme.dispatch.v1.SimulateDispatchAssignmentRequestKt
import com.acme.dispatch.v1.SimulateZoneRequest
import com.acme.dispatch.v1.SimulateZoneResponse
import com.acme.dispatch.v1.ZoneCalculationServiceGrpcKt
import com.acme.dispatch.v1.calculateDispatchAssignmentRequest
import com.acme.dispatch.v1.calculateZoneResponse
import com.acme.dispatch.v1.compareZoneOptionsResponse
import com.acme.dispatch.v1.copy as dispatchCopy
import com.acme.dispatch.v1.dispatchAssignment
import com.acme.dispatch.v1.dispatchAssignmentComment
import com.acme.dispatch.v1.dispatchOrder
import com.acme.dispatch.v1.estimateDispatchAssignmentRequest
import com.acme.dispatch.v1.estimateZoneResponse
import com.acme.dispatch.v1.explainDispatchAssignmentCalculationRequest
import com.acme.dispatch.v1.explainDispatchOrderDecisionRequest
import com.acme.dispatch.v1.explainZoneCalculationResponse
import com.acme.dispatch.v1.getZoneRatesResponse
import com.acme.dispatch.v1.quoteDispatchAssignmentRequest
import com.acme.dispatch.v1.quoteZoneResponse
import com.acme.dispatch.v1.recalculateDispatchAssignmentsRequest
import com.acme.dispatch.v1.recalculateZonesResponse
import com.acme.dispatch.v1.simulateDispatchAssignmentRequest
import com.acme.dispatch.v1.simulateZoneResponse
import com.acme.dispatch.v1.zone
import com.acme.dispatch.v1.zoneRef
import com.acme.documents.v1.DocumentServiceGrpcKt
import com.acme.documents.v1.WatchDocumentsRequestKt
import com.acme.documents.v1.watchDocumentsRequest
import com.acme.drivers.v1.driverRef
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.CheckPartEligibilityRequest
import com.acme.maintenance.v1.CheckPartEligibilityResponse
import com.acme.maintenance.v1.NormalizePartRequest
import com.acme.maintenance.v1.PartValidationServiceGrpcKt
import com.acme.maintenance.v1.WorkOrderValidationServiceGrpcKt
import com.acme.maintenance.v1.checkPartEligibilityRequest
import com.acme.maintenance.v1.normalizePartRequest
import com.acme.maintenance.v1.part
import com.acme.maintenance.v1.scoreWorkOrderRequest
import com.acme.maintenance.v1.workOrder
import com.acme.maintenance.v1.workOrderSla
import com.acme.metrics.v1.MetricSeriesServiceGrpcKt
import com.acme.metrics.v1.UpdateMetricSeriesRequest
import com.acme.metrics.v1.metricSeries
import com.acme.metrics.v1.updateMetricSeriesRequest
import com.acme.telematics.v1.SimulateTelemetryReadingRequestKt
import com.acme.telematics.v1.TelemetryReadingCalculationServiceGrpcKt
import com.acme.telematics.v1.simulateTelemetryReadingRequest
import com.acme.transport.dispatch.DispatchRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.google.protobuf.Any as ProtoAny
import com.google.protobuf.Timestamp
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import org.slf4j.LoggerFactory

/** Computes the figures of a zone, such as totals, taxes, and estimates. */
class ZoneCalculationController(
    private val store: DispatchRepository,
    private val workers: CoroutineScope,
    private val admissionsAdmissionService: AdmissionServiceGrpcKt.AdmissionServiceCoroutineStub,
    private val deliveriesProofOfDeliveryValidationService: ProofOfDeliveryValidationServiceGrpcKt.ProofOfDeliveryValidationServiceCoroutineStub,
    private val dispatchDispatchAssignmentCalculationService: DispatchAssignmentCalculationServiceGrpcKt.DispatchAssignmentCalculationServiceCoroutineStub,
    private val dispatchDispatchOrderValidationService: DispatchOrderValidationServiceGrpcKt.DispatchOrderValidationServiceCoroutineStub,
    private val documentsDocumentService: DocumentServiceGrpcKt.DocumentServiceCoroutineStub,
    private val maintenancePartValidationService: PartValidationServiceGrpcKt.PartValidationServiceCoroutineStub,
    private val maintenanceWorkOrderValidationService: WorkOrderValidationServiceGrpcKt.WorkOrderValidationServiceCoroutineStub,
    private val metricsMetricSeriesService: MetricSeriesServiceGrpcKt.MetricSeriesServiceCoroutineStub,
    private val telematicsTelemetryReadingCalculationService: TelemetryReadingCalculationServiceGrpcKt.TelemetryReadingCalculationServiceCoroutineStub,
) : ZoneCalculationServiceGrpcKt.ZoneCalculationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(ZoneCalculationController::class.java)

    private val invokeExplainZoneCalculation3: suspend (ExplainDispatchAssignmentCalculationRequest) -> ExplainDispatchAssignmentCalculationResponse = { payload ->
        dispatchDispatchAssignmentCalculationService.explainDispatchAssignmentCalculation(payload)
    }

    private val clientForSimulateZone1 by lazy { dispatchDispatchAssignmentCalculationService }

    /** Calculates a zone. */
    override suspend fun calculateZone(request: CalculateZoneRequest): CalculateZoneResponse = handleCalculateZone(request)

    /** Compares a zone. */
    override suspend fun compareZoneOptions(request: CompareZoneOptionsRequest): CompareZoneOptionsResponse =
        RpcBoundary.run("acme.dispatch.v1.ZoneCalculationService/CompareZoneOptions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CompareZoneOptions", request)
            log.atInfo().addKeyValue("operation", "CompareZoneOptions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.zone.zoneId).log("Recorded audit detail")
            val outbound0 = explainProofOfDeliveryDecisionRequest {
                this.context = request.context
                this.proofOfDelivery = proofOfDelivery {
                    this.audit = auditInfo {
                        this.createdAt = request.zone.audit.createdAt
                        this.updatedAt = request.zone.audit.updatedAt
                    }
                    this.billingAddress = postalAddress {
                        this.lines.addAll(request.details.destination.linesList)
                        this.recipient = request.details.destination.recipient
                    }
                }
            }
            val reply0 = deliveriesProofOfDeliveryValidationService.explainProofOfDeliveryDecision(outbound0)
            val outbound1 = explainDispatchAssignmentCalculationRequest {}.dispatchCopy {
                this.context = request.context
                this.dispatchAssignment = dispatchAssignment {
                    this.assignee = userRef {
                        this.displayName = request.zone.manager.displayName
                    }
                    this.comments.addAll(listOf(dispatchAssignmentComment {
                        this.createdAt = reply0.proofOfDelivery.audit.createdAt
                    }))
                    this.requester = contactInfo {
                        this.email = reply0.proofOfDelivery.counterparty.email
                    }
                }
            }
            val reply1 = dispatchDispatchAssignmentCalculationService.explainDispatchAssignmentCalculation(outbound1)
            val outbound2 = request.toEstimateDispatchAssignmentFromCompareZoneOptions2(reply0)
            val reply2 = dispatchDispatchAssignmentCalculationService.estimateDispatchAssignment(outbound2)
            val outbound3 = scoreWorkOrderRequest {
                this.context = request.context
                this.workOrder = workOrder {
                    this.customer = driverRef {
                        this.displayName = when (reply1.dispatchAssignment.originCase) {
                            DispatchAssignment.OriginCase.CUSTOMER -> reply1.dispatchAssignment.customer.displayName
                            else -> ""
                        }
                        this.email = reply2.dispatchAssignment.assignee.email
                    }
                    this.requester = contactInfo {
                        this.email = request.zone.manager.email
                    }
                    this.sla = workOrderSla {
                        this.policyId = reply2.dispatchAssignment.sla.policyId
                    }
                }
            }
            val reply3 = maintenanceWorkOrderValidationService.scoreWorkOrder(outbound3)
            compareZoneOptionsResponse {
                this.result = operationResult {
                    this.message = reply2.result.message
                    this.status = "ok"
                }
                this.zone = zone {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.proofOfDelivery.audit.updatedAt
                    }
                    this.children.addAll(listOf(zone {
                        this.audit = auditInfo {
                            this.createdBy = reply2.dispatchAssignment.audit.createdBy
                            this.updatedAt = reply3.workOrder.audit.updatedAt
                        }
                        this.children.addAll(listOf(zone {
                            this.audit = auditInfo {
                                this.etag = reply1.dispatchAssignment.audit.etag
                            }
                        }))
                        this.manager = userRef {
                            this.email = reply0.proofOfDelivery.counterparty.email
                        }
                    }))
                    this.name = request.zone.name
                    this.zoneId = request.zoneId
                }
            }
        }

    /** Estimates a zone. */
    override suspend fun estimateZone(request: EstimateZoneRequest): EstimateZoneResponse {
        return handleEstimateZone(request)
    }

    /** Explains a zone. */
    override suspend fun explainZoneCalculation(request: ExplainZoneCalculationRequest): ExplainZoneCalculationResponse = handleExplainZoneCalculation(request)

    /** Returns a zone. */
    override suspend fun getZoneRates(request: GetZoneRatesRequest): GetZoneRatesResponse =
        RpcBoundary.run("acme.dispatch.v1.ZoneCalculationService/GetZoneRates") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetZoneRates", request)
            log.atInfo().addKeyValue("operation", "GetZoneRates").log("Handling transport request")
            val outbound0 = calculateDispatchAssignmentRequest {
                this.context = request.context
                this.dispatchAssignment = dispatchAssignment {
                    this.assignee = userRef {
                        this.userId = request.context.actor.userId
                    }
                    this.requester = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
            }
            val invoke0: suspend (CalculateDispatchAssignmentRequest, Metadata) -> CalculateDispatchAssignmentResponse = dispatchDispatchAssignmentCalculationService::calculateDispatchAssignment
            val reply0 = invoke0(outbound0, Metadata())
            getZoneRatesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.zone = zone {
                    this.children.addAll(listOf(zone {
                        this.children.addAll(listOf(zone {
                            this.manager = userRef {
                                this.email = reply0.dispatchAssignment.assignee.email
                            }
                        }))
                        this.manager = userRef {
                            this.userId = reply0.dispatchAssignment.assignee.userId
                        }
                    }))
                    this.zoneId = request.zoneId
                }
            }
        }

    /** Quotes a zone. */
    override suspend fun quoteZone(request: QuoteZoneRequest): QuoteZoneResponse {
        return handleQuoteZone(request)
    }

    /** Recalculates a zone. */
    override suspend fun recalculateZones(request: RecalculateZonesRequest): RecalculateZonesResponse = handleRecalculateZones(request)

    /** Simulates a zone. */
    override suspend fun simulateZone(request: SimulateZoneRequest): SimulateZoneResponse =
        RpcBoundary.run("acme.dispatch.v1.ZoneCalculationService/SimulateZone") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SimulateZone", request)
            log.atInfo().addKeyValue("operation", "SimulateZone").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.zone.zoneId).log("Recorded audit detail")
            val outbound0 = watchDocumentsRequest {
                this.context = request.context
                this.filter = WatchDocumentsRequestKt.filter {
                    this.region = request.zone.location.region
                }
                this.subscriber = contactInfo {
                    this.email = request.zone.manager.email
                }
            }
            documentsDocumentService.watchDocuments(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = simulateDispatchAssignmentRequest {
                this.context = request.context
                this.details = SimulateDispatchAssignmentRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.details.destination.region
                    }
                }
                this.dispatchAssignment = dispatchAssignment {
                    this.audit = auditInfo {
                        this.createdAt = request.zone.audit.createdAt
                        this.updatedAt = request.zone.audit.updatedAt
                        this.updatedBy = request.zone.audit.updatedBy
                    }
                }
            }
            val reply1 = clientForSimulateZone1.simulateDispatchAssignment(outbound1)
            simulateZoneResponse {
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = "ok"
                }
                this.zone = zone {
                    this.location = request.zone.location
                    this.zoneId = request.zoneId
                }
            }
        }

    private suspend fun handleCalculateZone(request: CalculateZoneRequest): CalculateZoneResponse =
        RpcBoundary.run("acme.dispatch.v1.ZoneCalculationService/CalculateZone") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CalculateZone", request)
            log.atInfo().addKeyValue("operation", "CalculateZone").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.zone.zoneId).log("Recorded audit detail")
            val outbound0 = checkPartEligibilityRequest {
                this.context = request.context
                this.part = part {
                    this.address = postalAddress {
                        this.lines.addAll(request.zone.location.linesList)
                        this.postalCode = request.details.destination.postalCode
                        this.recipient = request.zone.location.recipient
                    }
                    this.owner = userRef {
                        this.userId = request.zone.manager.userId
                    }
                }
            }
            val reply0 = maintenancePartValidationService.checkPartEligibility(outbound0)
            calculateZoneResponse {
                this.breakdown = CalculateZoneResponseKt.breakdown {
                    this.currencyCode = request.details.currencyCode
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.zone = zone {
                    this.children.addAll(listOf(zone {
                        this.location = postalAddress {
                            this.postalCode = reply0.part.address.postalCode
                        }
                    }))
                    this.zoneId = request.zoneId
                }
            }
        }

    private fun CompareZoneOptionsRequest.toEstimateDispatchAssignmentFromCompareZoneOptions2(reply0: ExplainProofOfDeliveryDecisionResponse): EstimateDispatchAssignmentRequest {
        val source = this
        return estimateDispatchAssignmentRequest {
            this.context = source.context
            this.dispatchAssignment = dispatchAssignment {
                this.assignee = userRef {
                    this.displayName = reply0.proofOfDelivery.counterparty.displayName
                }
                this.audit = auditInfo {
                    this.updatedBy = source.zone.audit.updatedBy
                }
            }
        }
    }

    private suspend fun handleEstimateZone(request: EstimateZoneRequest): EstimateZoneResponse =
        RpcBoundary.run("acme.dispatch.v1.ZoneCalculationService/EstimateZone") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EstimateZone", request)
            log.atInfo().addKeyValue("operation", "EstimateZone").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.zone.zoneId).log("Recorded audit detail")
            val outbound0 = updateMetricSeriesRequest {
                this.context = request.context
                this.metricSeries = metricSeries {
                    this.address = postalAddress {
                        this.region = request.zone.location.region
                    }
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = metricsMetricSeriesService.updateMetricSeries(envelope0.unpack(UpdateMetricSeriesRequest::class.java))
            val outbound1 = recalculateDispatchAssignmentsRequest {
                this.context = request.context
                this.details = RecalculateDispatchAssignmentsRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = request.details.destination.countryCode
                        this.locality = reply0.metricSeries.address.locality
                    }
                }
                this.dispatchAssignment = dispatchAssignment {
                    this.assignee = userRef {
                        this.userId = reply0.metricSeries.owner.userId
                    }
                }
            }
            val reply1 = dispatchDispatchAssignmentCalculationService.recalculateDispatchAssignments(WireCopies.json(outbound1))
            estimateZoneResponse {
                this.breakdown = EstimateZoneResponseKt.breakdown {
                    this.currencyCode = request.details.currencyCode
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.zone = zone {
                    this.children.addAll(listOf(zone {
                        this.audit = auditInfo {
                            this.createdBy = reply0.metricSeries.audit.createdBy
                        }
                        this.children.addAll(listOf(zone {
                            this.audit = auditInfo {
                                this.updatedBy = reply1.dispatchAssignment.audit.updatedBy
                            }
                        }))
                    }))
                    this.zoneId = request.zoneId
                }
            }
        }

    private data class ExplainZoneCalculation0Command(
        val context: RequestContext,
        val partAuditUpdatedAt: Timestamp,
        val partAuditCreatedAt: Timestamp
    ) {
        fun toRequest(): NormalizePartRequest {
            val command = this
            return normalizePartRequest {
                this.context = command.context
                this.part = part {
                    this.audit = auditInfo {
                        this.createdAt = command.partAuditCreatedAt
                        this.updatedAt = command.partAuditUpdatedAt
                    }
                }
            }
        }
    }

    private suspend fun handleExplainZoneCalculation(request: ExplainZoneCalculationRequest): ExplainZoneCalculationResponse =
        RpcBoundary.run("acme.dispatch.v1.ZoneCalculationService/ExplainZoneCalculation") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainZoneCalculation", request)
            log.atInfo().addKeyValue("operation", "ExplainZoneCalculation").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.zone.zoneId).log("Recorded audit detail")
            val outbound0 = ExplainZoneCalculation0Command(
                context = request.context,
                partAuditUpdatedAt = request.zone.audit.updatedAt,
                partAuditCreatedAt = request.zone.audit.createdAt
            ).toRequest()
            val reply0 = maintenancePartValidationService.normalizePart(outbound0)
            val outbound1 = explainDispatchOrderDecisionRequest {
                this.context = request.context
                this.details = ExplainDispatchOrderDecisionRequestKt.details {
                    this.contact = contactInfo {
                        this.phone = request.details.contact.phone
                    }
                }
                this.dispatchOrder = dispatchOrder {
                    this.billingAddress = postalAddress {
                        this.countryCode = reply0.part.address.countryCode
                        this.recipient = reply0.part.address.recipient
                    }
                    this.counterparty = zoneRef {
                        this.displayName = request.zone.manager.displayName
                    }
                }
            }
            val command1: Submission<ExplainDispatchOrderDecisionRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = dispatchDispatchOrderValidationService.explainDispatchOrderDecision(payload1)
            val outbound2 = checkPartEligibilityRequest {
                this.context = request.context
                this.part = part {
                    this.audit = auditInfo {
                        this.etag = reply1.dispatchOrder.audit.etag
                        this.updatedAt = request.zone.audit.updatedAt
                        this.updatedBy = reply0.part.audit.updatedBy
                    }
                    this.owner = userRef {
                        this.email = request.zone.manager.email
                    }
                }
            }
            val adapter2 = OutboundPort<CheckPartEligibilityRequest, CheckPartEligibilityResponse> { payload ->
                maintenancePartValidationService.checkPartEligibility(payload)
            }
            val port2 = DelegatingPort(adapter2)
            val reply2 = port2.execute(outbound2)
            val outbound3 = explainDispatchAssignmentCalculationRequest {
                this.context = request.context
                this.dispatchAssignment = dispatchAssignment {
                    this.assignee = userRef {
                        this.email = request.zone.manager.email
                    }
                    this.audit = auditInfo {
                        this.createdAt = reply1.dispatchOrder.audit.createdAt
                        this.etag = request.zone.audit.etag
                    }
                }
            }
            val reply3 = invokeExplainZoneCalculation3(outbound3)
            explainZoneCalculationResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = reply3.result.status
                }
                this.zone = zone {
                    this.audit = auditInfo {
                        this.updatedAt = reply2.part.audit.updatedAt
                    }
                    this.children.addAll(listOf(zone {
                        this.audit = auditInfo {
                            this.etag = reply1.dispatchOrder.audit.etag
                        }
                        this.location = postalAddress {
                            this.countryCode = reply0.part.address.countryCode
                            this.region = reply1.dispatchOrder.billingAddress.region
                        }
                        this.manager = userRef {
                            this.displayName = reply2.part.owner.displayName
                        }
                    }))
                    this.depth = request.zone.depth
                    this.zoneId = request.zoneId
                }
            }
        }

    private suspend fun handleQuoteZone(request: QuoteZoneRequest): QuoteZoneResponse =
        RpcBoundary.run("acme.dispatch.v1.ZoneCalculationService/QuoteZone") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "QuoteZone", request)
            log.atInfo().addKeyValue("operation", "QuoteZone").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.zone.zoneId).log("Recorded audit detail")
            val outbound0 = QuoteDispatchAssignmentRequest.newBuilder().apply {
                setContext(request.context)
                setDetails(QuoteDispatchAssignmentRequestKt.details {
                    this.destination = postalAddress {
                        this.locality = request.details.destination.locality
                        this.postalCode = request.zone.location.postalCode
                    }
                })
                setDispatchAssignment(dispatchAssignment {
                    this.audit = auditInfo {
                        this.updatedBy = request.zone.audit.updatedBy
                    }
                    this.customer = driverRef {
                        this.email = request.zone.manager.email
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> dispatchDispatchAssignmentCalculationService.quoteDispatchAssignment(value) }
            val outbound1 = RequestFactory.create {
                simulateTelemetryReadingRequest {
                    this.context = request.context
                    this.details = SimulateTelemetryReadingRequestKt.details {
                        this.destination = postalAddress {
                            this.countryCode = request.zone.location.countryCode
                        }
                    }
                }
            }
            val reply1 = telematicsTelemetryReadingCalculationService.simulateTelemetryReading(outbound1)
            quoteZoneResponse {
                this.breakdown = QuoteZoneResponseKt.breakdown {
                    this.currencyCode = reply1.breakdown.currencyCode
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.zone = zone {
                    this.children.addAll(listOf(zone {
                        this.children.addAll(listOf(zone {
                            this.manager = userRef {
                                this.email = reply0.dispatchAssignment.requester.email
                            }
                        }))
                    }))
                    this.parentId = request.zone.parentId
                    this.zoneId = request.zoneId
                }
            }
        }

    private suspend fun handleRecalculateZones(request: RecalculateZonesRequest): RecalculateZonesResponse =
        RpcBoundary.run("acme.dispatch.v1.ZoneCalculationService/RecalculateZones") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecalculateZones", request)
            log.atInfo().addKeyValue("operation", "RecalculateZones").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.zone.zoneId).log("Recorded audit detail")
            val outbound0 = simulateDispatchAssignmentRequest {
                this.context = request.context
                this.details = SimulateDispatchAssignmentRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(request.zone.location.linesList)
                        this.locality = request.zone.location.locality
                        this.recipient = request.zone.location.recipient
                    }
                }
                this.dispatchAssignment = dispatchAssignment {
                    this.requester = contactInfo {
                        this.email = request.zone.manager.email
                    }
                }
            }
            val reply0 = ClientRegistry.lookup("acme.dispatch.v1.DispatchAssignmentCalculationService", DispatchAssignmentCalculationServiceGrpcKt.DispatchAssignmentCalculationServiceCoroutineStub::class.java).simulateDispatchAssignment(outbound0)
            val outbound1 = cloneAdmissionRequest {
                this.admission = admission {
                    this.attributes.putAll(request.zone.attributesMap)
                    this.correlationId = reply0.result.correlationId
                    this.subject = gateScanRef {
                        this.displayName = reply0.dispatchAssignment.customer.displayName
                    }
                }
                this.context = request.context
            }
            val reply1 = admissionsAdmissionService.cloneAdmission(WireCopies.reflective(outbound1))
            recalculateZonesResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
                this.zone = zone {
                    this.depth = request.zone.depth
                    this.manager = userRef {
                        this.userId = reply0.dispatchAssignment.assignee.userId
                    }
                    this.zoneId = request.zoneId
                }
            }
        }
}
