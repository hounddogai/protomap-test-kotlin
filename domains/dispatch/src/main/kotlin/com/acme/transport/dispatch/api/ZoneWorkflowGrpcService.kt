package com.acme.transport.dispatch.api

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.customerRef
import com.acme.dispatch.v1.AdvanceZoneWorkflowRequest
import com.acme.dispatch.v1.AdvanceZoneWorkflowResponse
import com.acme.dispatch.v1.AdvanceZoneWorkflowResponseKt
import com.acme.dispatch.v1.CalculateZoneRequestKt
import com.acme.dispatch.v1.CancelZoneWorkflowRequest
import com.acme.dispatch.v1.CancelZoneWorkflowResponse
import com.acme.dispatch.v1.CompareDispatchAssignmentOptionsRequestKt
import com.acme.dispatch.v1.DispatchAssignmentApprovalServiceGrpcKt
import com.acme.dispatch.v1.DispatchAssignmentCalculationServiceGrpcKt
import com.acme.dispatch.v1.DispatchAssignmentWorkflowServiceGrpcKt
import com.acme.dispatch.v1.DispatchOrderQueryServiceGrpcKt
import com.acme.dispatch.v1.DispatchOrderServiceGrpcKt
import com.acme.dispatch.v1.GetZoneApprovalStatusRequest
import com.acme.dispatch.v1.InspectZoneWorkflowRequest
import com.acme.dispatch.v1.InspectZoneWorkflowResponse
import com.acme.dispatch.v1.ListDispatchAssignmentWorkflowsRequestKt
import com.acme.dispatch.v1.PauseZoneWorkflowRequest
import com.acme.dispatch.v1.PauseZoneWorkflowResponse
import com.acme.dispatch.v1.PauseZoneWorkflowResponseKt
import com.acme.dispatch.v1.RecallDispatchAssignmentRequest
import com.acme.dispatch.v1.ResumeZoneWorkflowRequest
import com.acme.dispatch.v1.ResumeZoneWorkflowResponse
import com.acme.dispatch.v1.SignalZoneWorkflowRequest
import com.acme.dispatch.v1.SignalZoneWorkflowResponse
import com.acme.dispatch.v1.StartZoneWorkflowRequest
import com.acme.dispatch.v1.StartZoneWorkflowResponse
import com.acme.dispatch.v1.SummarizeDispatchOrdersRequest
import com.acme.dispatch.v1.UpdateDispatchOrderRequest
import com.acme.dispatch.v1.UpdateDispatchOrderResponse
import com.acme.dispatch.v1.ZoneApprovalServiceGrpcKt
import com.acme.dispatch.v1.ZoneCalculationServiceGrpcKt
import com.acme.dispatch.v1.ZoneWorkflowServiceGrpcKt
import com.acme.dispatch.v1.advanceZoneWorkflowResponse
import com.acme.dispatch.v1.calculateZoneRequest
import com.acme.dispatch.v1.cancelZoneWorkflowResponse
import com.acme.dispatch.v1.commentDispatchAssignmentApprovalRequest
import com.acme.dispatch.v1.compareDispatchAssignmentOptionsRequest
import com.acme.dispatch.v1.compareZoneOptionsRequest
import com.acme.dispatch.v1.copy as dispatchCopy
import com.acme.dispatch.v1.dispatchAssignment
import com.acme.dispatch.v1.dispatchOrder
import com.acme.dispatch.v1.getZoneApprovalStatusRequest
import com.acme.dispatch.v1.inspectZoneWorkflowResponse
import com.acme.dispatch.v1.listDispatchAssignmentWorkflowsRequest
import com.acme.dispatch.v1.pauseZoneWorkflowResponse
import com.acme.dispatch.v1.recallDispatchAssignmentRequest
import com.acme.dispatch.v1.resumeZoneWorkflowResponse
import com.acme.dispatch.v1.signalZoneWorkflowResponse
import com.acme.dispatch.v1.startZoneWorkflowResponse
import com.acme.dispatch.v1.summarizeDispatchOrdersRequest
import com.acme.dispatch.v1.updateDispatchOrderRequest
import com.acme.dispatch.v1.zone
import com.acme.drivers.v1.CheckDriverLicenseEligibilityRequest
import com.acme.drivers.v1.CheckDriverLicenseEligibilityRequestKt
import com.acme.drivers.v1.DriverLicenseValidationServiceGrpcKt
import com.acme.drivers.v1.DriverQueryServiceGrpcKt
import com.acme.drivers.v1.FindDriversRequestKt
import com.acme.drivers.v1.checkDriverLicenseEligibilityRequest
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.findDriversRequest
import com.acme.fuel.v1.FuelTransactionSyncServiceGrpcKt
import com.acme.fuel.v1.PushFuelTransactionRequest
import com.acme.fuel.v1.PushFuelTransactionResponse
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.pushFuelTransactionRequest
import com.acme.identity.v1.userRef
import com.acme.notifications.v1.ExportNotificationsRequestKt
import com.acme.notifications.v1.NotificationQueryServiceGrpcKt
import com.acme.notifications.v1.exportNotificationsRequest
import com.acme.telematics.v1.TelemetryReadingApprovalServiceGrpcKt
import com.acme.telematics.v1.escalateTelemetryReadingRequest
import com.acme.telematics.v1.geoFenceRef
import com.acme.telematics.v1.telemetryReading
import com.acme.transport.dispatch.DispatchRepository
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.yards.v1.ListYardMoveWorkflowsRequestKt
import com.acme.yards.v1.YardMoveWorkflowServiceGrpcKt
import com.acme.yards.v1.listYardMoveWorkflowsRequest
import com.google.protobuf.Any as ProtoAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Runs the long-lived workflow that takes a zone from start to finish. */
class ZoneWorkflowGrpcService(
    private val store: DispatchRepository,
    private val workers: CoroutineScope,
    private val dispatchDispatchAssignmentApprovalService: DispatchAssignmentApprovalServiceGrpcKt.DispatchAssignmentApprovalServiceCoroutineStub,
    private val dispatchDispatchAssignmentCalculationService: DispatchAssignmentCalculationServiceGrpcKt.DispatchAssignmentCalculationServiceCoroutineStub,
    private val dispatchDispatchAssignmentWorkflowService: DispatchAssignmentWorkflowServiceGrpcKt.DispatchAssignmentWorkflowServiceCoroutineStub,
    private val dispatchDispatchOrderQueryService: DispatchOrderQueryServiceGrpcKt.DispatchOrderQueryServiceCoroutineStub,
    private val dispatchDispatchOrderService: DispatchOrderServiceGrpcKt.DispatchOrderServiceCoroutineStub,
    private val dispatchZoneApprovalService: ZoneApprovalServiceGrpcKt.ZoneApprovalServiceCoroutineStub,
    private val dispatchZoneCalculationService: ZoneCalculationServiceGrpcKt.ZoneCalculationServiceCoroutineStub,
    private val driversDriverLicenseValidationService: DriverLicenseValidationServiceGrpcKt.DriverLicenseValidationServiceCoroutineStub,
    private val driversDriverQueryService: DriverQueryServiceGrpcKt.DriverQueryServiceCoroutineStub,
    private val fuelFuelTransactionSyncService: FuelTransactionSyncServiceGrpcKt.FuelTransactionSyncServiceCoroutineStub,
    private val notificationsNotificationQueryService: NotificationQueryServiceGrpcKt.NotificationQueryServiceCoroutineStub,
    private val telematicsTelemetryReadingApprovalService: TelemetryReadingApprovalServiceGrpcKt.TelemetryReadingApprovalServiceCoroutineStub,
    private val yardsYardMoveWorkflowService: YardMoveWorkflowServiceGrpcKt.YardMoveWorkflowServiceCoroutineStub,
) : ZoneWorkflowServiceGrpcKt.ZoneWorkflowServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(ZoneWorkflowGrpcService::class.java)

    private val workerForAdvanceZoneWorkflow0 = ReplyMailbox<PushFuelTransactionRequest, PushFuelTransactionResponse>(workers) { payload ->
        withTimeout(5_000) { fuelFuelTransactionSyncService.pushFuelTransaction(payload) }
    }

    private val clientForAdvanceZoneWorkflow1 by lazy { telematicsTelemetryReadingApprovalService }

    /** Advances a zone. */
    override suspend fun advanceZoneWorkflow(request: AdvanceZoneWorkflowRequest): AdvanceZoneWorkflowResponse {
        return handleAdvanceZoneWorkflow(request)
    }

    /** Cancels a zone. */
    override suspend fun cancelZoneWorkflow(request: CancelZoneWorkflowRequest): CancelZoneWorkflowResponse = handleCancelZoneWorkflow(request)

    /** Returns a zone. */
    override suspend fun inspectZoneWorkflow(request: InspectZoneWorkflowRequest): InspectZoneWorkflowResponse =
        RpcBoundary.run("acme.dispatch.v1.ZoneWorkflowService/InspectZoneWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "InspectZoneWorkflow", request)
            log.atInfo().addKeyValue("operation", "InspectZoneWorkflow").log("Handling transport request")
            val outbound0 = listDispatchAssignmentWorkflowsRequest {
                this.context = request.context
                this.filter = ListDispatchAssignmentWorkflowsRequestKt.filter {
                    this.createdAfter = request.context.deadline
                    this.labels.putAll(request.context.baggageMap)
                    this.ownerId = request.context.tenantId
                }
            }
            val reply0 = dispatchDispatchAssignmentWorkflowService.listDispatchAssignmentWorkflows(outbound0)
            inspectZoneWorkflowResponse {
                this.result = operationResult {
                    this.status = reply0.result.status
                }
                this.zone = zone {
                    this.zoneId = request.zoneId
                }
            }
        }

    /** Pauses a zone. */
    override suspend fun pauseZoneWorkflow(request: PauseZoneWorkflowRequest): PauseZoneWorkflowResponse {
        return handlePauseZoneWorkflow(request)
    }

    /** Resumes a zone. */
    override suspend fun resumeZoneWorkflow(request: ResumeZoneWorkflowRequest): ResumeZoneWorkflowResponse = handleResumeZoneWorkflow(request)

    /** Signals a zone. */
    override suspend fun signalZoneWorkflow(request: SignalZoneWorkflowRequest): SignalZoneWorkflowResponse =
        RpcBoundary.run("acme.dispatch.v1.ZoneWorkflowService/SignalZoneWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SignalZoneWorkflow", request)
            log.atInfo().addKeyValue("operation", "SignalZoneWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.zone.zoneId).log("Recorded audit detail")
            val outbound0 = SignalZoneWorkflow0Command(
                context = request.context,
                reason = request.reason,
                annotations = request.annotationsMap
            ).toRequest()
            val reply0 = dispatchDispatchOrderQueryService.summarizeDispatchOrders(outbound0)
            signalZoneWorkflowResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.message = reply0.result.message
                    this.status = "ok"
                }
                this.zone = zone {
                    this.attributes.putAll(request.zone.attributesMap)
                    this.zoneId = request.zoneId
                }
            }
        }

    /** Starts a zone. */
    override suspend fun startZoneWorkflow(request: StartZoneWorkflowRequest): StartZoneWorkflowResponse {
        return handleStartZoneWorkflow(request)
    }

    private suspend fun handleAdvanceZoneWorkflow(request: AdvanceZoneWorkflowRequest): AdvanceZoneWorkflowResponse =
        RpcBoundary.run("acme.dispatch.v1.ZoneWorkflowService/AdvanceZoneWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "AdvanceZoneWorkflow", request)
            log.atInfo().addKeyValue("operation", "AdvanceZoneWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.zone.zoneId).log("Recorded audit detail")
            val outbound0 = pushFuelTransactionRequest {
                this.context = request.context
                this.fuelTransaction = fuelTransaction {
                    this.attributes.putAll(request.zone.attributesMap)
                    this.subject = customerRef {
                        this.displayName = request.zone.manager.displayName
                        this.email = request.details.notify.email
                    }
                }
            }
            val reply0 = workerForAdvanceZoneWorkflow0.submit(outbound0)
            val outbound1 = escalateTelemetryReadingRequest {
                this.context = request.context
                this.telemetryReading = telemetryReading {
                    this.subject = geoFenceRef {
                        this.displayName = request.zone.manager.displayName
                    }
                }
            }
            val reply1 = clientForAdvanceZoneWorkflow1.escalateTelemetryReading(outbound1)
            val outbound2 = commentDispatchAssignmentApprovalRequest {
                this.context = request.context
                this.dispatchAssignment = dispatchAssignment {
                    this.customer = driverRef {
                        this.displayName = request.zone.manager.displayName
                    }
                }
            }
            val reply2 = flowOf(outbound2).map { value -> dispatchDispatchAssignmentApprovalService.commentDispatchAssignmentApproval(value) }.single()
            advanceZoneWorkflowResponse {
                this.outcome = AdvanceZoneWorkflowResponseKt.outcome {
                    this.message = reply2.outcome.message
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
                this.zone = zone {
                    this.audit = auditInfo {
                        this.createdAt = reply2.dispatchAssignment.audit.createdAt
                    }
                    this.children.addAll(listOf(zone {
                        this.children.addAll(listOf(zone {
                            this.children.addAll(listOf(zone {
                                this.attributes.putAll(reply1.telemetryReading.attributesMap)
                            }))
                        }))
                    }))
                    this.manager = userRef {
                        this.email = reply0.fuelTransaction.subject.email
                    }
                    this.status = request.zone.status
                    this.zoneId = request.zoneId
                }
            }
        }

    private suspend fun handleCancelZoneWorkflow(request: CancelZoneWorkflowRequest): CancelZoneWorkflowResponse =
        RpcBoundary.run("acme.dispatch.v1.ZoneWorkflowService/CancelZoneWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CancelZoneWorkflow", request)
            log.atInfo().addKeyValue("operation", "CancelZoneWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.zone.zoneId).log("Recorded audit detail")
            val outbound0 = listYardMoveWorkflowsRequest {
                this.context = request.context
                this.filter = ListYardMoveWorkflowsRequestKt.filter {
                    this.createdAfter = request.zone.audit.createdAt
                    this.labels.putAll(request.annotationsMap)
                    this.ownerId = request.zone.manager.userId
                    this.region = request.zone.location.region
                }
            }
            val reply0 = withTimeout(5_000) { yardsYardMoveWorkflowService.listYardMoveWorkflows(outbound0) }
            val configure1: CalculateZoneRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.zone = zone {
                    this.children.addAll(listOf(zone {
                        this.children.addAll(listOf(zone {
                            this.location = postalAddress {
                                this.locality = request.zone.location.locality
                            }
                            this.manager = userRef {
                                this.email = request.details.notify.email
                            }
                        }))
                    }))
                }
            }
            val outbound1 = calculateZoneRequest(configure1)
            val reply1 = dispatchZoneCalculationService.calculateZone(outbound1)
            cancelZoneWorkflowResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply0.result.message
                    this.status = "ok"
                }
                this.zone = zone {
                    this.children.addAll(listOf(zone {
                        this.audit = auditInfo {
                            this.etag = reply1.zone.audit.etag
                        }
                    }))
                    this.location = request.zone.location
                    this.name = reply1.zone.name
                    this.zoneId = request.zoneId
                }
            }
        }

    private fun PauseZoneWorkflowRequest.toRecallDispatchAssignmentFromPauseZoneWorkflow2(): RecallDispatchAssignmentRequest {
        val source = this
        return recallDispatchAssignmentRequest {
            this.context = source.context
            this.dispatchAssignment = dispatchAssignment {
                this.requester = contactInfo {
                    this.phone = source.details.notify.phone
                }
            }
        }
    }

    private suspend fun handlePauseZoneWorkflow(request: PauseZoneWorkflowRequest): PauseZoneWorkflowResponse =
        RpcBoundary.run("acme.dispatch.v1.ZoneWorkflowService/PauseZoneWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PauseZoneWorkflow", request)
            log.atInfo().addKeyValue("operation", "PauseZoneWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.zone.zoneId).log("Recorded audit detail")
            val outbound0 = findDriversRequest {
                this.context = request.context
                this.filter = FindDriversRequestKt.filter {
                    this.createdAfter = request.zone.audit.createdAt
                    this.ownerId = request.zone.parentId
                    this.region = request.zone.location.region
                }
            }
            val reply0 = driversDriverQueryService.findDrivers(outbound0)
            val outbound1 = compareZoneOptionsRequest {}.dispatchCopy {
                this.context = request.context
                this.zone = zone {
                    this.children.addAll(listOf(zone {
                        this.children.addAll(listOf(zone {
                            this.location = postalAddress {
                                this.recipient = request.zone.location.recipient
                            }
                            this.manager = userRef {
                                this.userId = request.zone.manager.userId
                            }
                        }))
                    }))
                }
            }
            val reply1 = dispatchZoneCalculationService.compareZoneOptions(outbound1)
            val outbound2 = request.toRecallDispatchAssignmentFromPauseZoneWorkflow2()
            val reply2 = dispatchDispatchAssignmentApprovalService.recallDispatchAssignment(outbound2)
            val outbound3 = compareDispatchAssignmentOptionsRequest {
                this.context = request.context
                this.details = CompareDispatchAssignmentOptionsRequestKt.details {
                    this.destination = postalAddress {
                        this.locality = request.zone.location.locality
                    }
                }
                this.dispatchAssignment = dispatchAssignment {
                    this.audit = auditInfo {
                        this.updatedBy = request.zone.audit.updatedBy
                    }
                }
            }
            val reply3 = dispatchDispatchAssignmentCalculationService.compareDispatchAssignmentOptions(outbound3)
            pauseZoneWorkflowResponse {
                this.outcome = PauseZoneWorkflowResponseKt.outcome {
                    this.status = reply2.outcome.status
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
                this.zone = zone {
                    this.children.addAll(listOf(zone {
                        this.audit = auditInfo {
                            this.updatedAt = reply3.dispatchAssignment.audit.updatedAt
                        }
                        this.children.addAll(listOf(zone {
                            this.audit = auditInfo {
                                this.createdBy = reply2.dispatchAssignment.audit.createdBy
                            }
                            this.manager = userRef {
                                this.email = reply1.zone.manager.email
                            }
                        }))
                    }))
                    this.name = request.zone.name
                    this.zoneId = request.zoneId
                }
            }
        }

    private suspend fun handleResumeZoneWorkflow(request: ResumeZoneWorkflowRequest): ResumeZoneWorkflowResponse =
        RpcBoundary.run("acme.dispatch.v1.ZoneWorkflowService/ResumeZoneWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResumeZoneWorkflow", request)
            log.atInfo().addKeyValue("operation", "ResumeZoneWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.zone.zoneId).log("Recorded audit detail")
            val outbound0 = checkDriverLicenseEligibilityRequest {
                this.context = request.context
                this.details = CheckDriverLicenseEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.zone.manager.email
                    }
                }
                this.driverLicense = driverLicense {
                    this.counterparty = driverRef {
                        this.displayName = request.zone.manager.displayName
                    }
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = driversDriverLicenseValidationService.checkDriverLicenseEligibility(envelope0.unpack(CheckDriverLicenseEligibilityRequest::class.java))
            val outbound1 = exportNotificationsRequest {
                this.context = request.context
                this.filter = ExportNotificationsRequestKt.filter {
                    this.createdAfter = reply0.driverLicense.dueAt
                    this.labels.putAll(request.zone.attributesMap)
                    this.ownerId = reply0.driverLicense.counterparty.driverId
                    this.region = reply0.driverLicense.billingAddress.region
                }
            }
            notificationsNotificationQueryService.exportNotifications(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            resumeZoneWorkflowResponse {
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
                this.zone = zone {
                    this.children.addAll(listOf(zone {
                        this.children.addAll(listOf(zone {
                            this.location = postalAddress {
                                this.region = reply0.driverLicense.billingAddress.region
                            }
                        }))
                    }))
                    this.location = request.zone.location
                    this.zoneId = request.zoneId
                }
            }
        }

    private data class SignalZoneWorkflow0Command(
        val context: RequestContext,
        val reason: String,
        val annotations: Map<String, String>
    ) {
        fun toRequest(): SummarizeDispatchOrdersRequest {
            val command = this
            return summarizeDispatchOrdersRequest {
                this.annotations.putAll(command.annotations)
                this.context = command.context
                this.reason = command.reason
            }
        }
    }

    private suspend fun handleStartZoneWorkflow(request: StartZoneWorkflowRequest): StartZoneWorkflowResponse =
        RpcBoundary.run("acme.dispatch.v1.ZoneWorkflowService/StartZoneWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StartZoneWorkflow", request)
            log.atInfo().addKeyValue("operation", "StartZoneWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.zone.name).log("Recorded audit detail")
            val outbound0 = getZoneApprovalStatusRequest {
                this.context = request.context
                this.zoneId = request.zone.zoneId
            }
            val command0: Submission<GetZoneApprovalStatusRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = dispatchZoneApprovalService.getZoneApprovalStatus(payload0)
            val outbound1 = updateDispatchOrderRequest {
                this.context = request.context
                this.dispatchOrder = dispatchOrder {
                    this.billingAddress = postalAddress {
                        this.locality = request.zone.location.locality
                    }
                }
            }
            val adapter1 = OutboundPort<UpdateDispatchOrderRequest, UpdateDispatchOrderResponse> { payload ->
                dispatchDispatchOrderService.updateDispatchOrder(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            startZoneWorkflowResponse {
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.message = reply0.result.message
                    this.status = "ok"
                }
                this.zone = zone {
                    this.attributes.putAll(request.zone.attributesMap)
                    this.children.addAll(listOf(zone {
                        this.audit = auditInfo {
                            this.etag = reply1.dispatchOrder.audit.etag
                        }
                    }))
                    this.zoneId = request.zoneId
                }
            }
        }
}
