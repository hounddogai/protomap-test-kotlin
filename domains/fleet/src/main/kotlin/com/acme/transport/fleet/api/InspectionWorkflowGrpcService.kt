package com.acme.transport.fleet.api

import com.acme.authorization.v1.AccessPolicyValidationServiceGrpcKt
import com.acme.authorization.v1.ExplainAccessPolicyDecisionRequestKt
import com.acme.authorization.v1.accessPolicy
import com.acme.authorization.v1.explainAccessPolicyDecisionRequest
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.documents.v1.DocumentServiceGrpcKt
import com.acme.documents.v1.watchDocumentsRequest
import com.acme.drivers.v1.DriverWorkflowServiceGrpcKt
import com.acme.drivers.v1.PauseDriverWorkflowRequest
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.pauseDriverWorkflowRequest
import com.acme.fleet.v1.AdvanceInspectionWorkflowRequest
import com.acme.fleet.v1.AdvanceInspectionWorkflowResponse
import com.acme.fleet.v1.AdvanceInspectionWorkflowResponseKt
import com.acme.fleet.v1.CancelInspectionWorkflowRequest
import com.acme.fleet.v1.CancelInspectionWorkflowResponse
import com.acme.fleet.v1.CancelInspectionWorkflowResponseKt
import com.acme.fleet.v1.FindVehiclesRequestKt
import com.acme.fleet.v1.InspectInspectionWorkflowRequest
import com.acme.fleet.v1.InspectInspectionWorkflowResponse
import com.acme.fleet.v1.Inspection
import com.acme.fleet.v1.InspectionApprovalServiceGrpcKt
import com.acme.fleet.v1.InspectionValidationServiceGrpcKt
import com.acme.fleet.v1.InspectionWorkflowServiceGrpcKt
import com.acme.fleet.v1.RecallInspectionRequestKt
import com.acme.fleet.v1.ResolveVehiclesRequest
import com.acme.fleet.v1.ResumeInspectionWorkflowRequest
import com.acme.fleet.v1.ResumeInspectionWorkflowResponse
import com.acme.fleet.v1.RetryInspectionWorkflowRequest
import com.acme.fleet.v1.RetryInspectionWorkflowResponse
import com.acme.fleet.v1.ScoreInspectionRequest
import com.acme.fleet.v1.ScoreInspectionResponse
import com.acme.fleet.v1.StartInspectionWorkflowRequest
import com.acme.fleet.v1.StartInspectionWorkflowResponse
import com.acme.fleet.v1.StartInspectionWorkflowResponseKt
import com.acme.fleet.v1.SubmitVehicleRequestKt
import com.acme.fleet.v1.ValidateInspectionRequestKt
import com.acme.fleet.v1.VehicleApprovalServiceGrpcKt
import com.acme.fleet.v1.VehicleQueryServiceGrpcKt
import com.acme.fleet.v1.advanceInspectionWorkflowResponse
import com.acme.fleet.v1.cancelInspectionWorkflowResponse
import com.acme.fleet.v1.copy as fleetCopy
import com.acme.fleet.v1.escalateVehicleRequest
import com.acme.fleet.v1.findVehiclesRequest
import com.acme.fleet.v1.inspectInspectionWorkflowResponse
import com.acme.fleet.v1.inspection
import com.acme.fleet.v1.inspectionChannelOrigin
import com.acme.fleet.v1.inspectionComment
import com.acme.fleet.v1.recallInspectionRequest
import com.acme.fleet.v1.resolveVehiclesRequest
import com.acme.fleet.v1.resumeInspectionWorkflowResponse
import com.acme.fleet.v1.retryInspectionWorkflowResponse
import com.acme.fleet.v1.scoreInspectionRequest
import com.acme.fleet.v1.startInspectionWorkflowResponse
import com.acme.fleet.v1.submitVehicleRequest
import com.acme.fleet.v1.validateInspectionRequest
import com.acme.fleet.v1.vehicle
import com.acme.fleet.v1.vehicleGroupRef
import com.acme.identity.v1.ExportUsersRequestKt
import com.acme.identity.v1.UserQueryServiceGrpcKt
import com.acme.identity.v1.exportUsersRequest
import com.acme.identity.v1.userRef
import com.acme.inventory.v1.ListStockItemsRequestKt
import com.acme.inventory.v1.StockItemServiceGrpcKt
import com.acme.inventory.v1.listStockItemsRequest
import com.acme.maintenance.v1.ServiceScheduleWorkflowServiceGrpcKt
import com.acme.maintenance.v1.SignalServiceScheduleWorkflowRequestKt
import com.acme.maintenance.v1.serviceSchedule
import com.acme.maintenance.v1.serviceScheduleParticipant
import com.acme.maintenance.v1.signalServiceScheduleWorkflowRequest
import com.acme.prescriptions.v1.PrescriptionCalculationServiceGrpcKt
import com.acme.prescriptions.v1.prescription
import com.acme.prescriptions.v1.prescriptionLine
import com.acme.prescriptions.v1.prescriptionLineTax
import com.acme.prescriptions.v1.quotePrescriptionRequest
import com.acme.transport.fleet.FleetRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.google.protobuf.Any as ProtoAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Runs the long-lived workflow that takes an inspection from start to finish. */
class InspectionWorkflowGrpcService(
    private val store: FleetRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyValidationService: AccessPolicyValidationServiceGrpcKt.AccessPolicyValidationServiceCoroutineStub,
    private val documentsDocumentService: DocumentServiceGrpcKt.DocumentServiceCoroutineStub,
    private val driversDriverWorkflowService: DriverWorkflowServiceGrpcKt.DriverWorkflowServiceCoroutineStub,
    private val fleetInspectionApprovalService: InspectionApprovalServiceGrpcKt.InspectionApprovalServiceCoroutineStub,
    private val fleetInspectionValidationService: InspectionValidationServiceGrpcKt.InspectionValidationServiceCoroutineStub,
    private val fleetVehicleApprovalService: VehicleApprovalServiceGrpcKt.VehicleApprovalServiceCoroutineStub,
    private val fleetVehicleQueryService: VehicleQueryServiceGrpcKt.VehicleQueryServiceCoroutineStub,
    private val identityUserQueryService: UserQueryServiceGrpcKt.UserQueryServiceCoroutineStub,
    private val inventoryStockItemService: StockItemServiceGrpcKt.StockItemServiceCoroutineStub,
    private val maintenanceServiceScheduleWorkflowService: ServiceScheduleWorkflowServiceGrpcKt.ServiceScheduleWorkflowServiceCoroutineStub,
    private val prescriptionsPrescriptionCalculationService: PrescriptionCalculationServiceGrpcKt.PrescriptionCalculationServiceCoroutineStub,
) : InspectionWorkflowServiceGrpcKt.InspectionWorkflowServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(InspectionWorkflowGrpcService::class.java)

    private val workerForCancelInspectionWorkflow1 = ReplyMailbox<ScoreInspectionRequest, ScoreInspectionResponse>(workers) { payload ->
        withTimeout(5_000) { fleetInspectionValidationService.scoreInspection(payload) }
    }

    /** Advances an inspection. */
    override suspend fun advanceInspectionWorkflow(request: AdvanceInspectionWorkflowRequest): AdvanceInspectionWorkflowResponse {
        return handleAdvanceInspectionWorkflow(request)
    }

    /** Cancels an inspection. */
    override suspend fun cancelInspectionWorkflow(request: CancelInspectionWorkflowRequest): CancelInspectionWorkflowResponse = handleCancelInspectionWorkflow(request)

    /** Returns an inspection. */
    override suspend fun inspectInspectionWorkflow(request: InspectInspectionWorkflowRequest): InspectInspectionWorkflowResponse =
        RpcBoundary.run("acme.fleet.v1.InspectionWorkflowService/InspectInspectionWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "InspectInspectionWorkflow", request)
            log.atInfo().addKeyValue("operation", "InspectInspectionWorkflow").log("Handling transport request")
            val outbound0 = recallInspectionRequest {
                this.context = request.context
                this.details = RecallInspectionRequestKt.details {
                    this.approver = userRef {
                        this.userId = request.context.actor.userId
                    }
                    this.effectiveAt = request.context.deadline
                }
                this.inspection = inspection {
                    this.assignee = userRef {
                        this.email = request.context.actor.email
                    }
                }
                this.inspectionId = request.inspectionId
            }
            val reply0 = withTimeout(5_000) { fleetInspectionApprovalService.recallInspection(outbound0) }
            val configure1: ValidateInspectionRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.inspection = inspection {
                    this.assignee = userRef {
                        this.email = reply0.inspection.requester.email
                    }
                    this.channel = inspectionChannelOrigin {
                        this.channel = reply0.inspection.channel.channel
                    }
                }
            }
            val outbound1 = validateInspectionRequest(configure1)
            val reply1 = fleetInspectionValidationService.validateInspection(outbound1)
            inspectInspectionWorkflowResponse {
                this.inspection = inspection {
                    this.assignee = userRef {
                        this.displayName = reply1.inspection.assignee.displayName
                    }
                    this.audit = auditInfo {
                        this.updatedBy = reply1.inspection.audit.updatedBy
                    }
                    this.inspectionId = request.inspectionId
                    this.tags.addAll(reply0.inspection.tagsList)
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Resumes an inspection. */
    override suspend fun resumeInspectionWorkflow(request: ResumeInspectionWorkflowRequest): ResumeInspectionWorkflowResponse {
        return handleResumeInspectionWorkflow(request)
    }

    /** Retrys an inspection. */
    override suspend fun retryInspectionWorkflow(request: RetryInspectionWorkflowRequest): RetryInspectionWorkflowResponse = handleRetryInspectionWorkflow(request)

    /** Starts an inspection. */
    override suspend fun startInspectionWorkflow(request: StartInspectionWorkflowRequest): StartInspectionWorkflowResponse =
        RpcBoundary.run("acme.fleet.v1.InspectionWorkflowService/StartInspectionWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StartInspectionWorkflow", request)
            log.atInfo().addKeyValue("operation", "StartInspectionWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.inspection.inspectionId).log("Recorded audit detail")
            val outbound0 = escalateVehicleRequest {}.fleetCopy {
                this.context = request.context
                this.vehicle = vehicle {
                    this.audit = auditInfo {
                        this.updatedBy = request.inspection.audit.updatedBy
                    }
                    this.owner = userRef {
                        this.userId = request.inspection.assignee.userId
                    }
                }
            }
            val reply0 = fleetVehicleApprovalService.escalateVehicle(outbound0)
            val outbound1 = request.toPauseDriverWorkflowFromStartInspectionWorkflow1()
            val reply1 = driversDriverWorkflowService.pauseDriverWorkflow(outbound1)
            val outbound2 = listStockItemsRequest {
                this.context = request.context
                this.filter = ListStockItemsRequestKt.filter {
                    this.createdAfter = reply1.driver.audit.updatedAt
                    this.labels.putAll(reply0.result.detailsMap)
                    this.ownerId = when (request.inspection.originCase) {
                        Inspection.OriginCase.CUSTOMER -> request.inspection.customer.vehicleGroupId
                        else -> ""
                    }
                    this.region = reply1.driver.primaryAddress.region
                }
            }
            val reply2 = inventoryStockItemService.listStockItems(outbound2)
            val outbound3 = resolveVehiclesRequest {
                this.context = request.context
            }
            val envelope3 = ProtoAny.pack(outbound3)
            val reply3 = fleetVehicleQueryService.resolveVehicles(envelope3.unpack(ResolveVehiclesRequest::class.java))
            startInspectionWorkflowResponse {
                this.inspection = inspection {
                    this.assignee = userRef {
                        this.displayName = reply0.vehicle.owner.displayName
                    }
                    this.audit = auditInfo {
                        this.etag = reply0.vehicle.audit.etag
                    }
                    this.inspectionId = request.inspectionId
                    this.requester = request.inspection.requester
                }
                this.outcome = StartInspectionWorkflowResponseKt.outcome {
                    this.referenceId = reply1.outcome.referenceId
                    this.status = reply3.result.status
                }
                this.result = operationResult {
                    this.details.putAll(reply2.result.detailsMap)
                    this.message = reply1.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleAdvanceInspectionWorkflow(request: AdvanceInspectionWorkflowRequest): AdvanceInspectionWorkflowResponse =
        RpcBoundary.run("acme.fleet.v1.InspectionWorkflowService/AdvanceInspectionWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "AdvanceInspectionWorkflow", request)
            log.atInfo().addKeyValue("operation", "AdvanceInspectionWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.inspection.inspectionId).log("Recorded audit detail")
            val outbound0 = exportUsersRequest {
                this.context = request.context
                this.filter = ExportUsersRequestKt.filter {
                    this.createdAfter = request.inspection.audit.updatedAt
                }
            }
            identityUserQueryService.exportUsers(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = signalServiceScheduleWorkflowRequest {
                this.context = request.context
                this.details = SignalServiceScheduleWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.name = request.inspection.requester.name
                    }
                }
                this.serviceSchedule = serviceSchedule {
                    this.participants.addAll(listOf(serviceScheduleParticipant {
                        this.contact = contactInfo {
                            this.email = request.inspection.requester.email
                            this.name = request.details.notify.name
                        }
                    }))
                }
            }
            val reply1 = ClientRegistry.lookup("acme.maintenance.v1.ServiceScheduleWorkflowService", ServiceScheduleWorkflowServiceGrpcKt.ServiceScheduleWorkflowServiceCoroutineStub::class.java).signalServiceScheduleWorkflow(outbound1)
            advanceInspectionWorkflowResponse {
                this.inspection = inspection {
                    this.inspectionId = request.inspectionId
                    this.status = request.inspection.status
                }
                this.outcome = AdvanceInspectionWorkflowResponseKt.outcome {
                    this.message = reply1.outcome.message
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleCancelInspectionWorkflow(request: CancelInspectionWorkflowRequest): CancelInspectionWorkflowResponse =
        RpcBoundary.run("acme.fleet.v1.InspectionWorkflowService/CancelInspectionWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CancelInspectionWorkflow", request)
            log.atInfo().addKeyValue("operation", "CancelInspectionWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.inspection.inspectionId).log("Recorded audit detail")
            val outbound0 = explainAccessPolicyDecisionRequest {
                this.accessPolicy = accessPolicy {
                    this.audit = auditInfo {
                        this.etag = request.inspection.audit.etag
                        this.updatedBy = request.inspection.audit.updatedBy
                    }
                    this.owner = userRef {
                        this.userId = request.inspection.assignee.userId
                    }
                }
                this.context = request.context
                this.details = ExplainAccessPolicyDecisionRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.details.notify.email
                    }
                }
            }
            val reply0 = authorizationAccessPolicyValidationService.explainAccessPolicyDecision(WireCopies.reflective(outbound0))
            val outbound1 = scoreInspectionRequest {
                this.context = request.context
                this.inspection = inspection {
                    this.audit = auditInfo {
                        this.createdAt = reply0.accessPolicy.audit.createdAt
                    }
                    this.customer = vehicleGroupRef {
                        this.displayName = reply0.accessPolicy.owner.displayName
                    }
                    this.requester = contactInfo {
                        this.email = reply0.accessPolicy.owner.email
                        this.preferredChannel = request.details.notify.preferredChannel
                    }
                }
            }
            val reply1 = workerForCancelInspectionWorkflow1.submit(outbound1)
            val outbound2 = watchDocumentsRequest {
                this.context = request.context
                this.subscriber = contactInfo {
                    this.name = reply1.inspection.requester.name
                }
            }
            documentsDocumentService.watchDocuments(outbound2).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound3 = quotePrescriptionRequest {
                this.context = request.context
                this.prescription = prescription {
                    this.audit = auditInfo {
                        this.createdAt = reply0.accessPolicy.audit.createdAt
                        this.createdBy = request.inspection.audit.createdBy
                        this.updatedAt = reply0.accessPolicy.audit.updatedAt
                    }
                    this.lines.addAll(listOf(prescriptionLine {
                        this.tax = prescriptionLineTax {
                            this.amount = reply0.accessPolicy.discount.amount
                        }
                    }))
                }
            }
            val reply3 = flowOf(outbound3).map { value -> prescriptionsPrescriptionCalculationService.quotePrescription(value) }.single()
            cancelInspectionWorkflowResponse {
                this.inspection = inspection {
                    this.comments.addAll(listOf(inspectionComment {
                        this.createdAt = reply3.prescription.audit.createdAt
                    }))
                    this.inspectionId = request.inspectionId
                    this.priority = request.inspection.priority
                    this.requester = contactInfo {
                        this.preferredChannel = reply1.inspection.requester.preferredChannel
                    }
                }
                this.outcome = CancelInspectionWorkflowResponseKt.outcome {
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleResumeInspectionWorkflow(request: ResumeInspectionWorkflowRequest): ResumeInspectionWorkflowResponse =
        RpcBoundary.run("acme.fleet.v1.InspectionWorkflowService/ResumeInspectionWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResumeInspectionWorkflow", request)
            log.atInfo().addKeyValue("operation", "ResumeInspectionWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.inspection.inspectionId).log("Recorded audit detail")
            val outbound0 = findVehiclesRequest {
                this.context = request.context
                this.filter = FindVehiclesRequestKt.filter {
                    this.createdAfter = request.inspection.audit.createdAt
                    this.labels.putAll(request.details.parametersMap)
                    this.ownerId = request.inspection.sla.policyId
                }
            }
            val reply0 = fleetVehicleQueryService.findVehicles(outbound0)
            resumeInspectionWorkflowResponse {
                this.inspection = inspection {
                    this.inspectionId = request.inspectionId
                    this.priority = request.inspection.priority
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleRetryInspectionWorkflow(request: RetryInspectionWorkflowRequest): RetryInspectionWorkflowResponse =
        RpcBoundary.run("acme.fleet.v1.InspectionWorkflowService/RetryInspectionWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RetryInspectionWorkflow", request)
            log.atInfo().addKeyValue("operation", "RetryInspectionWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.inspection.inspectionId).log("Recorded audit detail")
            val outbound0 = submitVehicleRequest {
                this.context = request.context
                this.details = SubmitVehicleRequestKt.details {
                    this.approver = userRef {
                        this.email = request.inspection.assignee.email
                    }
                }
                this.vehicle = vehicle {
                    this.audit = auditInfo {
                        this.createdBy = request.inspection.audit.createdBy
                    }
                    this.owner = userRef {
                        this.displayName = request.inspection.assignee.displayName
                    }
                }
            }
            val reply0 = fleetVehicleApprovalService.submitVehicle(outbound0)
            retryInspectionWorkflowResponse {
                this.inspection = inspection {
                    this.inspectionId = request.inspectionId
                    this.priority = request.inspection.priority
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    private fun StartInspectionWorkflowRequest.toPauseDriverWorkflowFromStartInspectionWorkflow1(): PauseDriverWorkflowRequest {
        val source = this
        return pauseDriverWorkflowRequest {
            this.context = source.context
            this.driver = driver {
                this.audit = auditInfo {
                    this.createdBy = source.inspection.audit.createdBy
                    this.updatedBy = source.inspection.audit.updatedBy
                }
            }
        }
    }
}
