package com.acme.transport.maintenance.api

import com.acme.admissions.v1.AdmissionServiceGrpcKt
import com.acme.admissions.v1.admission
import com.acme.admissions.v1.cloneAdmissionRequest
import com.acme.admissions.v1.gateScanRef
import com.acme.appointments.v1.AppointmentQueryServiceGrpcKt
import com.acme.appointments.v1.summarizeAppointmentsRequest
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.ProofOfDeliveryCalculationServiceGrpcKt
import com.acme.deliveries.v1.RecalculateProofOfDeliveriesRequestKt
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.recalculateProofOfDeliveriesRequest
import com.acme.fuel.v1.FuelPolicyHistoryServiceGrpcKt
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.recordFuelPolicyVersionRequest
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.LedgerAccountQueryServiceGrpcKt
import com.acme.ledger.v1.SummarizeLedgerAccountsRequestKt
import com.acme.ledger.v1.summarizeLedgerAccountsRequest
import com.acme.maintenance.v1.CalculatePartRequest
import com.acme.maintenance.v1.CalculatePartResponse
import com.acme.maintenance.v1.ComparePartOptionsRequest
import com.acme.maintenance.v1.ComparePartOptionsResponse
import com.acme.maintenance.v1.EstimatePartRequest
import com.acme.maintenance.v1.EstimatePartResponse
import com.acme.maintenance.v1.ExplainPartCalculationRequest
import com.acme.maintenance.v1.ExplainPartCalculationResponse
import com.acme.maintenance.v1.GetPartRatesRequest
import com.acme.maintenance.v1.GetPartRatesResponse
import com.acme.maintenance.v1.NormalizeWorkOrderRequest
import com.acme.maintenance.v1.NormalizeWorkOrderResponse
import com.acme.maintenance.v1.Part
import com.acme.maintenance.v1.PartCalculationServiceGrpcKt
import com.acme.maintenance.v1.PreviewWorkOrderRequestKt
import com.acme.maintenance.v1.QuotePartRequest
import com.acme.maintenance.v1.QuotePartResponse
import com.acme.maintenance.v1.QuotePartResponseKt
import com.acme.maintenance.v1.RecalculatePartsRequest
import com.acme.maintenance.v1.RecalculatePartsResponse
import com.acme.maintenance.v1.ServiceScheduleCalculationServiceGrpcKt
import com.acme.maintenance.v1.SimulatePartRequest
import com.acme.maintenance.v1.SimulatePartResponse
import com.acme.maintenance.v1.SimulateServiceScheduleRequestKt
import com.acme.maintenance.v1.WorkOrderValidationServiceGrpcKt
import com.acme.maintenance.v1.calculatePartResponse
import com.acme.maintenance.v1.comparePartOptionsResponse
import com.acme.maintenance.v1.copy as maintenanceCopy
import com.acme.maintenance.v1.estimatePartResponse
import com.acme.maintenance.v1.explainPartCalculationResponse
import com.acme.maintenance.v1.explainWorkOrderDecisionRequest
import com.acme.maintenance.v1.getPartRatesResponse
import com.acme.maintenance.v1.normalizeWorkOrderRequest
import com.acme.maintenance.v1.part
import com.acme.maintenance.v1.previewWorkOrderRequest
import com.acme.maintenance.v1.quotePartResponse
import com.acme.maintenance.v1.recalculatePartsResponse
import com.acme.maintenance.v1.scoreWorkOrderRequest
import com.acme.maintenance.v1.serviceSchedule
import com.acme.maintenance.v1.simulatePartResponse
import com.acme.maintenance.v1.simulateServiceScheduleRequest
import com.acme.maintenance.v1.workOrder
import com.acme.transport.maintenance.MaintenanceRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.CreateYardRequest
import com.acme.yards.v1.YardServiceGrpcKt
import com.acme.yards.v1.createYardRequest
import com.acme.yards.v1.yard
import com.acme.yards.v1.yardDimensions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Computes the figures of a part, such as totals, taxes, and estimates. */
class PartCalculationGrpcService(
    private val store: MaintenanceRepository,
    private val workers: CoroutineScope,
    private val admissionsAdmissionService: AdmissionServiceGrpcKt.AdmissionServiceCoroutineStub,
    private val appointmentsAppointmentQueryService: AppointmentQueryServiceGrpcKt.AppointmentQueryServiceCoroutineStub,
    private val deliveriesProofOfDeliveryCalculationService: ProofOfDeliveryCalculationServiceGrpcKt.ProofOfDeliveryCalculationServiceCoroutineStub,
    private val fuelFuelPolicyHistoryService: FuelPolicyHistoryServiceGrpcKt.FuelPolicyHistoryServiceCoroutineStub,
    private val ledgerLedgerAccountQueryService: LedgerAccountQueryServiceGrpcKt.LedgerAccountQueryServiceCoroutineStub,
    private val maintenanceServiceScheduleCalculationService: ServiceScheduleCalculationServiceGrpcKt.ServiceScheduleCalculationServiceCoroutineStub,
    private val maintenanceWorkOrderValidationService: WorkOrderValidationServiceGrpcKt.WorkOrderValidationServiceCoroutineStub,
    private val yardsYardService: YardServiceGrpcKt.YardServiceCoroutineStub,
) : PartCalculationServiceGrpcKt.PartCalculationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(PartCalculationGrpcService::class.java)

    private val workerForEstimatePart0 = ReplyMailbox<NormalizeWorkOrderRequest, NormalizeWorkOrderResponse>(workers) { payload ->
        withTimeout(5_000) { maintenanceWorkOrderValidationService.normalizeWorkOrder(payload) }
    }

    private val clientForExplainPartCalculation0 by lazy { maintenanceWorkOrderValidationService }

    /** Calculates a part. */
    override suspend fun calculatePart(request: CalculatePartRequest): CalculatePartResponse {
        return handleCalculatePart(request)
    }

    /** Compares a part. */
    override suspend fun comparePartOptions(request: ComparePartOptionsRequest): ComparePartOptionsResponse = handleComparePartOptions(request)

    /** Estimates a part. */
    override suspend fun estimatePart(request: EstimatePartRequest): EstimatePartResponse =
        RpcBoundary.run("acme.maintenance.v1.PartCalculationService/EstimatePart") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EstimatePart", request)
            log.atInfo().addKeyValue("operation", "EstimatePart").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.part.partId).log("Recorded audit detail")
            val outbound0 = normalizeWorkOrderRequest {
                this.context = request.context
                this.workOrder = workOrder {
                    this.audit = auditInfo {
                        this.createdBy = request.part.audit.createdBy
                        this.updatedBy = request.part.audit.updatedBy
                    }
                }
            }
            val reply0 = workerForEstimatePart0.submit(outbound0)
            estimatePartResponse {
                this.part = part {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.workOrder.audit.updatedAt
                    }
                    this.owner = request.part.owner
                    this.partId = request.partId
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    /** Explains a part. */
    override suspend fun explainPartCalculation(request: ExplainPartCalculationRequest): ExplainPartCalculationResponse {
        return handleExplainPartCalculation(request)
    }

    /** Returns a part. */
    override suspend fun getPartRates(request: GetPartRatesRequest): GetPartRatesResponse = handleGetPartRates(request)

    /** Quotes a part. */
    override suspend fun quotePart(request: QuotePartRequest): QuotePartResponse =
        RpcBoundary.run("acme.maintenance.v1.PartCalculationService/QuotePart") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "QuotePart", request)
            log.atInfo().addKeyValue("operation", "QuotePart").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.part.partId).log("Recorded audit detail")
            val configure0: PreviewWorkOrderRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.workOrder = workOrder {
                    this.audit = auditInfo {
                        this.createdBy = request.part.audit.createdBy
                        this.updatedAt = request.part.audit.updatedAt
                        this.updatedBy = request.part.audit.updatedBy
                    }
                    this.requester = contactInfo {
                        this.email = request.part.owner.email
                    }
                }
            }
            val outbound0 = previewWorkOrderRequest(configure0)
            val reply0 = maintenanceWorkOrderValidationService.previewWorkOrder(outbound0)
            val outbound1 = explainWorkOrderDecisionRequest {
                this.context = request.context
                this.workOrder = workOrder {
                    this.audit = auditInfo {
                        this.etag = reply0.workOrder.audit.etag
                    }
                    this.requester = contactInfo {
                        this.email = reply0.workOrder.customer.email
                    }
                }
            }
            val reply1 = maintenanceWorkOrderValidationService.explainWorkOrderDecision(outbound1)
            quotePartResponse {
                this.breakdown = QuotePartResponseKt.breakdown {
                    this.currencyCode = request.details.currencyCode
                }
                this.part = part {
                    this.owner = userRef {
                        this.displayName = reply1.workOrder.customer.displayName
                        this.email = reply0.workOrder.customer.email
                    }
                    this.partId = request.partId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Recalculates a part. */
    override suspend fun recalculateParts(request: RecalculatePartsRequest): RecalculatePartsResponse {
        return handleRecalculateParts(request)
    }

    /** Simulates a part. */
    override suspend fun simulatePart(request: SimulatePartRequest): SimulatePartResponse = handleSimulatePart(request)

    private suspend fun handleCalculatePart(request: CalculatePartRequest): CalculatePartResponse =
        RpcBoundary.run("acme.maintenance.v1.PartCalculationService/CalculatePart") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CalculatePart", request)
            log.atInfo().addKeyValue("operation", "CalculatePart").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.part.partId).log("Recorded audit detail")
            val outbound0 = CreateYardRequest.newBuilder().apply {
                setContext(request.context)
                setYard(yard {
                    this.audit = auditInfo {
                        this.createdAt = request.part.audit.createdAt
                    }
                    this.dimensions = yardDimensions {
                        this.height = request.part.dimensions.height
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> yardsYardService.createYard(value) }
            val outbound1 = RequestFactory.create {
                recordFuelPolicyVersionRequest {
                    this.context = request.context
                    this.fuelPolicy = fuelPolicy {
                        this.owner = userRef {
                            this.displayName = request.part.owner.displayName
                            this.email = reply0.yard.owner.email
                        }
                    }
                }
            }
            val reply1 = fuelFuelPolicyHistoryService.recordFuelPolicyVersion(outbound1)
            calculatePartResponse {
                this.part = part {
                    this.audit = auditInfo {
                        this.createdAt = reply0.yard.audit.createdAt
                    }
                    this.capacity = request.part.capacity
                    this.owner = userRef {
                        this.displayName = reply1.fuelPolicy.owner.displayName
                        this.email = reply0.yard.owner.email
                    }
                    this.partId = request.partId
                }
                this.result = operationResult {
                    this.message = reply1.outcome.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleComparePartOptions(request: ComparePartOptionsRequest): ComparePartOptionsResponse =
        RpcBoundary.run("acme.maintenance.v1.PartCalculationService/ComparePartOptions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ComparePartOptions", request)
            log.atInfo().addKeyValue("operation", "ComparePartOptions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.part.partId).log("Recorded audit detail")
            val outbound0 = simulateServiceScheduleRequest {
                this.context = request.context
                this.details = SimulateServiceScheduleRequestKt.details {
                    this.destination = postalAddress {
                        this.locality = request.part.address.locality
                        this.postalCode = request.details.destination.postalCode
                    }
                }
                this.serviceSchedule = serviceSchedule {
                    this.audit = auditInfo {
                        this.createdBy = request.part.audit.createdBy
                    }
                    this.location = postalAddress {
                        this.locality = request.details.destination.locality
                    }
                }
            }
            val reply0 = ClientRegistry.lookup("acme.maintenance.v1.ServiceScheduleCalculationService", ServiceScheduleCalculationServiceGrpcKt.ServiceScheduleCalculationServiceCoroutineStub::class.java).simulateServiceSchedule(outbound0)
            val outbound1 = cloneAdmissionRequest {
                this.admission = admission {
                    this.subject = gateScanRef {
                        this.displayName = reply0.serviceSchedule.organizer.displayName
                    }
                }
                this.context = request.context
            }
            val reply1 = admissionsAdmissionService.cloneAdmission(WireCopies.reflective(outbound1))
            comparePartOptionsResponse {
                this.part = part {
                    this.audit = auditInfo {
                        this.createdBy = reply0.serviceSchedule.audit.createdBy
                    }
                    this.capacity = request.part.capacity
                    this.owner = userRef {
                        this.email = reply0.serviceSchedule.organizer.email
                    }
                    this.partId = request.partId
                }
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleExplainPartCalculation(request: ExplainPartCalculationRequest): ExplainPartCalculationResponse =
        RpcBoundary.run("acme.maintenance.v1.PartCalculationService/ExplainPartCalculation") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainPartCalculation", request)
            log.atInfo().addKeyValue("operation", "ExplainPartCalculation").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.part.partId).log("Recorded audit detail")
            val outbound0 = scoreWorkOrderRequest {
                this.context = request.context
                this.workOrder = workOrder {
                    this.assignee = userRef {
                        this.email = request.part.owner.email
                    }
                    this.requester = contactInfo {
                        this.email = request.details.contact.email
                    }
                }
            }
            val reply0 = clientForExplainPartCalculation0.scoreWorkOrder(outbound0)
            val outbound1 = summarizeAppointmentsRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.reason = request.reason
            }
            val reply1 = flowOf(outbound1).map { value -> appointmentsAppointmentQueryService.summarizeAppointments(value) }.single()
            explainPartCalculationResponse {
                this.part = part {
                    this.audit = auditInfo {
                        this.createdAt = reply0.workOrder.audit.createdAt
                        this.etag = reply0.workOrder.audit.etag
                    }
                    this.partId = request.partId
                    this.status = request.part.status
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.status = reply1.result.status
                }
            }
        }

    private suspend fun handleGetPartRates(request: GetPartRatesRequest): GetPartRatesResponse =
        RpcBoundary.run("acme.maintenance.v1.PartCalculationService/GetPartRates") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetPartRates", request)
            log.atInfo().addKeyValue("operation", "GetPartRates").log("Handling transport request")
            val outbound0 = normalizeWorkOrderRequest {
                this.context = request.context
                this.workOrder = workOrder {
                    this.assignee = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply0 = withTimeout(5_000) { maintenanceWorkOrderValidationService.normalizeWorkOrder(outbound0) }
            getPartRatesResponse {
                this.part = part {
                    this.partId = request.partId
                    this.tags.addAll(reply0.workOrder.tagsList)
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleRecalculateParts(request: RecalculatePartsRequest): RecalculatePartsResponse =
        RpcBoundary.run("acme.maintenance.v1.PartCalculationService/RecalculateParts") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecalculateParts", request)
            log.atInfo().addKeyValue("operation", "RecalculateParts").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.part.partId).log("Recorded audit detail")
            val outbound0 = summarizeLedgerAccountsRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.details = SummarizeLedgerAccountsRequestKt.details {
                    this.filters.putAll(request.part.specsMap)
                }
                this.reason = request.reason
            }
            val reply0 = ledgerLedgerAccountQueryService.summarizeLedgerAccounts(outbound0)
            val outbound1 = scoreWorkOrderRequest {}.maintenanceCopy {
                this.context = request.context
                this.workOrder = workOrder {
                    this.audit = auditInfo {
                        this.createdAt = request.part.audit.createdAt
                        this.etag = request.part.audit.etag
                    }
                }
            }
            val reply1 = maintenanceWorkOrderValidationService.scoreWorkOrder(outbound1)
            val outbound2 = recalculateProofOfDeliveriesRequest {
                this.context = request.context
                this.details = RecalculateProofOfDeliveriesRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = when (request.part.locatorCase) {
                            Part.LocatorCase.ADDRESS -> request.part.address.countryCode
                            else -> ""
                        }
                    }
                }
                this.proofOfDelivery = proofOfDelivery {
                    this.audit = auditInfo {
                        this.createdBy = request.part.audit.createdBy
                        this.updatedAt = reply1.workOrder.audit.updatedAt
                    }
                    this.billingAddress = postalAddress {
                        this.recipient = when (request.part.locatorCase) {
                            Part.LocatorCase.ADDRESS -> request.part.address.recipient
                            else -> ""
                        }
                    }
                }
            }
            val reply2 = deliveriesProofOfDeliveryCalculationService.recalculateProofOfDeliveries(outbound2)
            recalculatePartsResponse {
                this.part = part {
                    this.owner = userRef {
                        this.displayName = reply2.proofOfDelivery.counterparty.displayName
                    }
                    this.partId = request.partId
                    this.specs.putAll(request.part.specsMap)
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply1.result.message
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSimulatePart(request: SimulatePartRequest): SimulatePartResponse =
        RpcBoundary.run("acme.maintenance.v1.PartCalculationService/SimulatePart") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SimulatePart", request)
            log.atInfo().addKeyValue("operation", "SimulatePart").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.part.partId).log("Recorded audit detail")
            val outbound0 = previewWorkOrderRequest {
                this.context = request.context
                this.workOrder = workOrder {
                    this.audit = auditInfo {
                        this.updatedAt = request.part.audit.updatedAt
                        this.updatedBy = request.part.audit.updatedBy
                    }
                }
            }
            val reply0 = maintenanceWorkOrderValidationService.previewWorkOrder(outbound0)
            simulatePartResponse {
                this.part = part {
                    this.name = request.part.name
                    this.owner = userRef {
                        this.email = reply0.workOrder.assignee.email
                    }
                    this.partId = request.partId
                    this.tags.addAll(reply0.workOrder.tagsList)
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }
}
