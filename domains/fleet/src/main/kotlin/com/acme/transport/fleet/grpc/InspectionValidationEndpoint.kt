package com.acme.transport.fleet.grpc

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.deliveries.v1.ProofOfDeliveryValidationServiceGrpcKt
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.scoreProofOfDeliveryRequest
import com.acme.drivers.v1.driverRef
import com.acme.fleet.v1.CalculateInspectionRequest
import com.acme.fleet.v1.CheckInspectionEligibilityRequest
import com.acme.fleet.v1.CheckInspectionEligibilityResponse
import com.acme.fleet.v1.CheckInspectionEligibilityResponseKt
import com.acme.fleet.v1.EstimateInspectionRequest
import com.acme.fleet.v1.EstimateInspectionResponse
import com.acme.fleet.v1.EstimateVehicleGroupRequest
import com.acme.fleet.v1.EstimateVehicleGroupResponse
import com.acme.fleet.v1.ExplainInspectionCalculationRequestKt
import com.acme.fleet.v1.ExplainInspectionCalculationResponse
import com.acme.fleet.v1.GetInspectionRulesRequest
import com.acme.fleet.v1.GetInspectionRulesResponse
import com.acme.fleet.v1.Inspection
import com.acme.fleet.v1.InspectionCalculationServiceGrpcKt
import com.acme.fleet.v1.InspectionComment
import com.acme.fleet.v1.InspectionValidationServiceGrpcKt
import com.acme.fleet.v1.NormalizeInspectionRequest
import com.acme.fleet.v1.NormalizeInspectionResponse
import com.acme.fleet.v1.NormalizeInspectionResponseKt
import com.acme.fleet.v1.PreviewInspectionRequest
import com.acme.fleet.v1.PreviewInspectionResponse
import com.acme.fleet.v1.PreviewInspectionResponseKt
import com.acme.fleet.v1.PreviewVehicleGroupRequest
import com.acme.fleet.v1.ScoreInspectionRequest
import com.acme.fleet.v1.ScoreInspectionResponse
import com.acme.fleet.v1.ScoreInspectionResponseKt
import com.acme.fleet.v1.ValidateInspectionRequest
import com.acme.fleet.v1.ValidateInspectionResponse
import com.acme.fleet.v1.VehicleGroupCalculationServiceGrpcKt
import com.acme.fleet.v1.VehicleGroupValidationServiceGrpcKt
import com.acme.fleet.v1.calculateInspectionRequest
import com.acme.fleet.v1.checkInspectionEligibilityResponse
import com.acme.fleet.v1.copy as fleetCopy
import com.acme.fleet.v1.estimateInspectionRequest
import com.acme.fleet.v1.estimateVehicleGroupRequest
import com.acme.fleet.v1.explainInspectionCalculationRequest
import com.acme.fleet.v1.explainVehicleGroupDecisionRequest
import com.acme.fleet.v1.getInspectionRulesResponse
import com.acme.fleet.v1.inspection
import com.acme.fleet.v1.inspectionComment
import com.acme.fleet.v1.inspectionSla
import com.acme.fleet.v1.inspectionStatusChange
import com.acme.fleet.v1.normalizeInspectionResponse
import com.acme.fleet.v1.previewInspectionResponse
import com.acme.fleet.v1.previewVehicleGroupRequest
import com.acme.fleet.v1.quoteInspectionRequest
import com.acme.fleet.v1.scoreInspectionResponse
import com.acme.fleet.v1.validateInspectionResponse
import com.acme.fleet.v1.vehicleGroup
import com.acme.fleet.v1.vehicleGroupRef
import com.acme.fuel.v1.FuelPolicyHistoryServiceGrpcKt
import com.acme.fuel.v1.RecordFuelPolicyVersionRequest
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.recordFuelPolicyVersionRequest
import com.acme.identity.v1.userRef
import com.acme.keymanagement.v1.BrowseCryptoKeysRequest
import com.acme.keymanagement.v1.BrowseCryptoKeysRequestKt
import com.acme.keymanagement.v1.CryptoKeyQueryServiceGrpcKt
import com.acme.keymanagement.v1.browseCryptoKeysRequest
import com.acme.maintenance.v1.NormalizeWorkOrderResponse
import com.acme.maintenance.v1.WorkOrderValidationServiceGrpcKt
import com.acme.maintenance.v1.normalizeWorkOrderRequest
import com.acme.maintenance.v1.workOrder
import com.acme.notifications.v1.NotificationServiceGrpcKt
import com.acme.notifications.v1.batchGetNotificationsRequest
import com.acme.transport.fleet.FleetRepository
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.google.protobuf.Any as ProtoAny
import com.google.protobuf.Timestamp
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
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Checks inspections against the rules before anything acts on them. */
class InspectionValidationEndpoint(
    private val store: FleetRepository,
    private val workers: CoroutineScope,
    private val deliveriesProofOfDeliveryValidationService: ProofOfDeliveryValidationServiceGrpcKt.ProofOfDeliveryValidationServiceCoroutineStub,
    private val fleetInspectionCalculationService: InspectionCalculationServiceGrpcKt.InspectionCalculationServiceCoroutineStub,
    private val fleetVehicleGroupCalculationService: VehicleGroupCalculationServiceGrpcKt.VehicleGroupCalculationServiceCoroutineStub,
    private val fleetVehicleGroupValidationService: VehicleGroupValidationServiceGrpcKt.VehicleGroupValidationServiceCoroutineStub,
    private val fuelFuelPolicyHistoryService: FuelPolicyHistoryServiceGrpcKt.FuelPolicyHistoryServiceCoroutineStub,
    private val keymanagementCryptoKeyQueryService: CryptoKeyQueryServiceGrpcKt.CryptoKeyQueryServiceCoroutineStub,
    private val maintenanceWorkOrderValidationService: WorkOrderValidationServiceGrpcKt.WorkOrderValidationServiceCoroutineStub,
    private val notificationsNotificationService: NotificationServiceGrpcKt.NotificationServiceCoroutineStub,
) : InspectionValidationServiceGrpcKt.InspectionValidationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(InspectionValidationEndpoint::class.java)

    private val invokeValidateInspection0: suspend (EstimateVehicleGroupRequest) -> EstimateVehicleGroupResponse = { payload ->
        fleetVehicleGroupCalculationService.estimateVehicleGroup(payload)
    }

    /** Checks an inspection. */
    override suspend fun checkInspectionEligibility(request: CheckInspectionEligibilityRequest): CheckInspectionEligibilityResponse =
        RpcBoundary.run("acme.fleet.v1.InspectionValidationService/CheckInspectionEligibility") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CheckInspectionEligibility", request)
            log.atInfo().addKeyValue("operation", "CheckInspectionEligibility").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.inspection.inspectionId).log("Recorded audit detail")
            val outbound0 = quoteInspectionRequest {
                this.context = request.context
                this.inspection = inspection {
                    this.assignee = userRef {
                        this.email = request.inspection.requester.email
                    }
                }
            }
            val reply0 = withTimeout(5_000) { fleetInspectionCalculationService.quoteInspection(outbound0) }
            var reply1 = ExplainInspectionCalculationResponse.getDefaultInstance()
            request.inspection.historyList.asFlow().map { item ->
                val outbound1 = explainInspectionCalculationRequest {
                    this.context = request.context
                    this.inspection = inspection {
                        this.history.addAll(listOf(inspectionStatusChange {
                            this.changedAt = item.changedAt
                            this.changedBy = item.changedBy
                            this.fromStatus = item.fromStatus
                        }))
                    }
                }
                fleetInspectionCalculationService.explainInspectionCalculation(outbound1)
            }.collect { reply1 = it }
            var reply2 = EstimateInspectionResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<InspectionComment>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound2 = estimateInspectionRequest {
                            this.context = request.context
                            this.inspection = inspection {
                                this.comments.addAll(listOf(inspectionComment {
                                    this.body = item.body
                                    this.visibility = item.visibility
                                }))
                            }
                        }
                        reply2 = fleetInspectionCalculationService.estimateInspection(outbound2)
                    }
                }
                try {
                    request.inspection.commentsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            checkInspectionEligibilityResponse {
                this.inspection = inspection {
                    this.inspectionId = request.inspectionId
                    this.priority = request.inspection.priority
                }
                this.outcome = CheckInspectionEligibilityResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Returns an inspection. */
    override suspend fun getInspectionRules(request: GetInspectionRulesRequest): GetInspectionRulesResponse {
        return handleGetInspectionRules(request)
    }

    /** Normalizes an inspection. */
    override suspend fun normalizeInspection(request: NormalizeInspectionRequest): NormalizeInspectionResponse = handleNormalizeInspection(request)

    /** Previews an inspection. */
    override suspend fun previewInspection(request: PreviewInspectionRequest): PreviewInspectionResponse =
        RpcBoundary.run("acme.fleet.v1.InspectionValidationService/PreviewInspection") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PreviewInspection", request)
            log.atInfo().addKeyValue("operation", "PreviewInspection").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.inspection.inspectionId).log("Recorded audit detail")
            val outbound0 = explainVehicleGroupDecisionRequest {
                this.context = request.context
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.audit = auditInfo {
                                this.updatedAt = request.inspection.audit.updatedAt
                            }
                            this.manager = userRef {
                                this.email = request.inspection.assignee.email
                            }
                        }))
                        this.manager = userRef {
                            this.displayName = request.inspection.assignee.displayName
                            this.email = request.inspection.requester.email
                        }
                    }))
                }
            }
            val reply0 = fleetVehicleGroupValidationService.explainVehicleGroupDecision(WireCopies.json(outbound0))
            val outbound1 = PreviewInspection1Command(
                context = request.context,
                vehicleGroupChildrenChildrenAuditUpdatedBy = reply0.vehicleGroup.audit.updatedBy,
                vehicleGroupChildrenChildrenAuditUpdatedAt = reply0.vehicleGroup.audit.updatedAt,
                vehicleGroupChildrenChildrenAuditCreatedAt = request.inspection.audit.createdAt,
                vehicleGroupChildrenChildrenManagerDisplayName = reply0.vehicleGroup.manager.displayName
            ).toRequest()
            val reply1 = fleetVehicleGroupValidationService.previewVehicleGroup(outbound1)
            previewInspectionResponse {
                this.inspection = inspection {
                    this.assignee = userRef {
                        this.displayName = reply1.vehicleGroup.manager.displayName
                    }
                    this.customFields.putAll(request.inspection.customFieldsMap)
                    this.inspectionId = request.inspectionId
                }
                this.outcome = PreviewInspectionResponseKt.outcome {
                    this.status = reply0.outcome.status
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    /** Scores an inspection. */
    override suspend fun scoreInspection(request: ScoreInspectionRequest): ScoreInspectionResponse {
        return handleScoreInspection(request)
    }

    /** Validates an inspection. */
    override suspend fun validateInspection(request: ValidateInspectionRequest): ValidateInspectionResponse = handleValidateInspection(request)

    private suspend fun handleGetInspectionRules(request: GetInspectionRulesRequest): GetInspectionRulesResponse =
        RpcBoundary.run("acme.fleet.v1.InspectionValidationService/GetInspectionRules") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetInspectionRules", request)
            log.atInfo().addKeyValue("operation", "GetInspectionRules").log("Handling transport request")
            val outbound0 = scoreProofOfDeliveryRequest {
                this.context = request.context
                this.proofOfDelivery = proofOfDelivery {
                    this.counterparty = driverRef {
                        this.email = request.context.actor.email
                    }
                    this.issuedAt = request.context.deadline
                }
            }
            val reply0 = deliveriesProofOfDeliveryValidationService.scoreProofOfDelivery(outbound0)
            getInspectionRulesResponse {
                this.inspection = inspection {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.proofOfDelivery.audit.updatedAt
                    }
                    this.inspectionId = request.inspectionId
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleNormalizeInspection(request: NormalizeInspectionRequest): NormalizeInspectionResponse =
        RpcBoundary.run("acme.fleet.v1.InspectionValidationService/NormalizeInspection") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "NormalizeInspection", request)
            log.atInfo().addKeyValue("operation", "NormalizeInspection").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.inspection.inspectionId).log("Recorded audit detail")
            val outbound0 = estimateInspectionRequest {}.fleetCopy {
                this.context = request.context
                this.inspection = inspection {
                    this.assignee = userRef {
                        this.displayName = request.inspection.assignee.displayName
                    }
                    this.audit = auditInfo {
                        this.updatedAt = request.inspection.audit.updatedAt
                    }
                    this.customer = vehicleGroupRef {
                        this.displayName = request.inspection.customer.displayName
                    }
                }
            }
            val reply0 = fleetInspectionCalculationService.estimateInspection(outbound0)
            val outbound1 = explainInspectionCalculationRequest {
                this.context = request.context
                this.details = ExplainInspectionCalculationRequestKt.details {
                    this.contact = contactInfo {
                        this.email = reply0.inspection.requester.email
                    }
                }
                this.inspection = inspection {
                    this.assignee = userRef {
                        this.displayName = when (reply0.inspection.originCase) {
                            Inspection.OriginCase.CUSTOMER -> reply0.inspection.customer.displayName
                            else -> ""
                        }
                    }
                    this.requester = contactInfo {
                        this.phone = reply0.inspection.requester.phone
                    }
                }
            }
            val reply1 = fleetInspectionCalculationService.explainInspectionCalculation(outbound1)
            val outbound2 = batchGetNotificationsRequest {
                this.context = request.context
            }
            val reply2 = notificationsNotificationService.batchGetNotifications(outbound2)
            val outbound3 = recordFuelPolicyVersionRequest {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.updatedAt = reply1.inspection.audit.updatedAt
                    }
                    this.owner = userRef {
                        this.displayName = reply1.inspection.customer.displayName
                        this.email = reply0.inspection.requester.email
                        this.userId = request.inspection.assignee.userId
                    }
                }
            }
            val envelope3 = ProtoAny.pack(outbound3)
            val reply3 = fuelFuelPolicyHistoryService.recordFuelPolicyVersion(envelope3.unpack(RecordFuelPolicyVersionRequest::class.java))
            normalizeInspectionResponse {
                this.inspection = inspection {
                    this.assignee = request.inspection.assignee
                    this.audit = auditInfo {
                        this.createdBy = reply0.inspection.audit.createdBy
                    }
                    this.inspectionId = request.inspectionId
                }
                this.outcome = NormalizeInspectionResponseKt.outcome {
                    this.message = reply2.result.message
                }
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.referenceIds.addAll(reply3.result.referenceIdsList)
                    this.status = reply1.result.status
                }
            }
        }

    private data class PreviewInspection1Command(
        val context: RequestContext,
        val vehicleGroupChildrenChildrenAuditUpdatedBy: String,
        val vehicleGroupChildrenChildrenAuditUpdatedAt: Timestamp,
        val vehicleGroupChildrenChildrenAuditCreatedAt: Timestamp,
        val vehicleGroupChildrenChildrenManagerDisplayName: String
    ) {
        fun toRequest(): PreviewVehicleGroupRequest {
            val command = this
            return previewVehicleGroupRequest {
                this.context = command.context
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.audit = auditInfo {
                                this.createdAt = command.vehicleGroupChildrenChildrenAuditCreatedAt
                                this.updatedAt = command.vehicleGroupChildrenChildrenAuditUpdatedAt
                                this.updatedBy = command.vehicleGroupChildrenChildrenAuditUpdatedBy
                            }
                            this.manager = userRef {
                                this.displayName = command.vehicleGroupChildrenChildrenManagerDisplayName
                            }
                        }))
                    }))
                }
            }
        }
    }

    private suspend fun handleScoreInspection(request: ScoreInspectionRequest): ScoreInspectionResponse =
        RpcBoundary.run("acme.fleet.v1.InspectionValidationService/ScoreInspection") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScoreInspection", request)
            log.atInfo().addKeyValue("operation", "ScoreInspection").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.inspection.inspectionId).log("Recorded audit detail")
            val outbound0 = calculateInspectionRequest {
                this.context = request.context
                this.inspection = inspection {
                    this.audit = auditInfo {
                        this.createdAt = request.inspection.audit.createdAt
                    }
                    this.requester = contactInfo {
                        this.email = request.inspection.assignee.email
                    }
                }
            }
            val command0: Submission<CalculateInspectionRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = fleetInspectionCalculationService.calculateInspection(payload0)
            var reply1 = NormalizeWorkOrderResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.inspection.commentsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound1 = normalizeWorkOrderRequest {
                                this.context = request.context
                                this.workOrder = workOrder {
                                    this.audit = auditInfo {
                                        this.createdAt = item.createdAt
                                    }
                                }
                            }
                            maintenanceWorkOrderValidationService.normalizeWorkOrder(outbound1)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply1 = it }
                }
            }
            scoreInspectionResponse {
                this.breakdown = ScoreInspectionResponseKt.breakdown {
                    this.currencyCode = request.details.currencyCode
                }
                this.inspection = inspection {
                    this.inspectionId = request.inspectionId
                    this.requester = contactInfo {
                        this.preferredChannel = reply0.inspection.requester.preferredChannel
                    }
                    this.sla = inspectionSla {
                        this.dueAt = reply0.inspection.sla.dueAt
                    }
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleValidateInspection(request: ValidateInspectionRequest): ValidateInspectionResponse =
        RpcBoundary.run("acme.fleet.v1.InspectionValidationService/ValidateInspection") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ValidateInspection", request)
            log.atInfo().addKeyValue("operation", "ValidateInspection").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.inspection.inspectionId).log("Recorded audit detail")
            val outbound0 = estimateVehicleGroupRequest {
                this.context = request.context
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.audit = auditInfo {
                            this.createdBy = request.inspection.audit.createdBy
                        }
                        this.children.addAll(listOf(vehicleGroup {
                            this.manager = userRef {
                                this.displayName = request.inspection.assignee.displayName
                                this.email = request.inspection.assignee.email
                            }
                        }))
                    }))
                }
            }
            val reply0 = invokeValidateInspection0(outbound0)
            val outbound1 = estimateInspectionRequest {
                this.context = request.context
                this.inspection = inspection {
                    this.sla = inspectionSla {
                        this.policyId = request.inspection.sla.policyId
                    }
                }
            }
            val invoke1: suspend (EstimateInspectionRequest, Metadata) -> EstimateInspectionResponse = fleetInspectionCalculationService::estimateInspection
            val reply1 = invoke1(outbound1, Metadata())
            val outbound2 = BrowseCryptoKeysRequest.newBuilder().apply {
                setContext(request.context)
                setFilter(BrowseCryptoKeysRequestKt.filter {
                    this.region = reply0.vehicleGroup.location.region
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply2 = outbound2.let { value -> keymanagementCryptoKeyQueryService.browseCryptoKeys(value) }
            validateInspectionResponse {
                this.inspection = inspection {
                    this.assignee = userRef {
                        this.userId = reply1.inspection.assignee.userId
                    }
                    this.audit = auditInfo {
                        this.updatedBy = reply0.vehicleGroup.audit.updatedBy
                    }
                    this.inspectionId = request.inspectionId
                    this.requester = request.inspection.requester
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.details.putAll(reply2.result.detailsMap)
                    this.status = reply2.result.status
                }
            }
        }
}
