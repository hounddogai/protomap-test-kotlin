package com.acme.transport.maintenance.adapters

import com.acme.authorization.v1.AccessPolicyServiceGrpcKt
import com.acme.authorization.v1.CloneAccessPolicyRequest
import com.acme.authorization.v1.CloneAccessPolicyResponse
import com.acme.authorization.v1.accessPolicy
import com.acme.authorization.v1.cloneAccessPolicyRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.concessions.v1.ConcessionStandServiceGrpcKt
import com.acme.concessions.v1.cloneConcessionStandRequest
import com.acme.concessions.v1.concessionStand
import com.acme.deliveries.v1.DeliveryExceptionApprovalServiceGrpcKt
import com.acme.deliveries.v1.EscalateDeliveryExceptionRequest
import com.acme.deliveries.v1.EscalateDeliveryExceptionRequestKt
import com.acme.deliveries.v1.ProofOfDeliveryWorkflowServiceGrpcKt
import com.acme.deliveries.v1.ResumeProofOfDeliveryWorkflowRequestKt
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.escalateDeliveryExceptionRequest
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.resumeProofOfDeliveryWorkflowRequest
import com.acme.documents.v1.DocumentValidationServiceGrpcKt
import com.acme.documents.v1.LintDocumentsRequest
import com.acme.documents.v1.document
import com.acme.documents.v1.lintDocumentsRequest
import com.acme.drivers.v1.DriverLicenseValidationServiceGrpcKt
import com.acme.drivers.v1.DutyStatusValidationServiceGrpcKt
import com.acme.drivers.v1.ExplainDutyStatusDecisionRequest
import com.acme.drivers.v1.ExplainDutyStatusDecisionRequestKt
import com.acme.drivers.v1.ExplainDutyStatusDecisionResponse
import com.acme.drivers.v1.ValidateDutyStatusRequest
import com.acme.drivers.v1.ValidateDutyStatusRequestKt
import com.acme.drivers.v1.ValidateDutyStatusResponse
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverLicenseRef
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.dutyStatus
import com.acme.drivers.v1.explainDutyStatusDecisionRequest
import com.acme.drivers.v1.scoreDriverLicenseRequest
import com.acme.drivers.v1.validateDutyStatusRequest
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.LedgerAccountServiceGrpcKt
import com.acme.ledger.v1.WatchLedgerAccountsRequestKt
import com.acme.ledger.v1.watchLedgerAccountsRequest
import com.acme.maintenance.v1.ActivateWorkOrderRequest
import com.acme.maintenance.v1.ActivateWorkOrderResponse
import com.acme.maintenance.v1.ActivateWorkOrderResponseKt
import com.acme.maintenance.v1.ExpireWorkOrderRequest
import com.acme.maintenance.v1.ExpireWorkOrderResponse
import com.acme.maintenance.v1.ExpireWorkOrderResponseKt
import com.acme.maintenance.v1.ExplainWorkOrderCalculationRequest
import com.acme.maintenance.v1.GetWorkOrderLifecycleRequest
import com.acme.maintenance.v1.GetWorkOrderLifecycleResponse
import com.acme.maintenance.v1.PartCalculationServiceGrpcKt
import com.acme.maintenance.v1.PartValidationServiceGrpcKt
import com.acme.maintenance.v1.QuoteServiceScheduleRequest
import com.acme.maintenance.v1.QuoteServiceScheduleResponse
import com.acme.maintenance.v1.ReinstateWorkOrderRequest
import com.acme.maintenance.v1.ReinstateWorkOrderResponse
import com.acme.maintenance.v1.ReinstateWorkOrderResponseKt
import com.acme.maintenance.v1.RenewWorkOrderRequest
import com.acme.maintenance.v1.RenewWorkOrderResponse
import com.acme.maintenance.v1.RenewWorkOrderResponseKt
import com.acme.maintenance.v1.ScheduleWorkOrderExpiryRequest
import com.acme.maintenance.v1.ScheduleWorkOrderExpiryResponse
import com.acme.maintenance.v1.ScheduleWorkOrderExpiryResponseKt
import com.acme.maintenance.v1.ServiceScheduleCalculationServiceGrpcKt
import com.acme.maintenance.v1.SuspendWorkOrderRequest
import com.acme.maintenance.v1.SuspendWorkOrderResponse
import com.acme.maintenance.v1.SuspendWorkOrderResponseKt
import com.acme.maintenance.v1.ValidatePartRequest
import com.acme.maintenance.v1.ValidatePartRequestKt
import com.acme.maintenance.v1.WorkOrderCalculationServiceGrpcKt
import com.acme.maintenance.v1.WorkOrderLifecycleServiceGrpcKt
import com.acme.maintenance.v1.activateWorkOrderResponse
import com.acme.maintenance.v1.comparePartOptionsRequest
import com.acme.maintenance.v1.copy as maintenanceCopy
import com.acme.maintenance.v1.estimatePartRequest
import com.acme.maintenance.v1.estimateServiceScheduleRequest
import com.acme.maintenance.v1.expireWorkOrderResponse
import com.acme.maintenance.v1.explainWorkOrderCalculationRequest
import com.acme.maintenance.v1.getWorkOrderLifecycleResponse
import com.acme.maintenance.v1.part
import com.acme.maintenance.v1.quoteServiceScheduleRequest
import com.acme.maintenance.v1.reinstateWorkOrderResponse
import com.acme.maintenance.v1.renewWorkOrderResponse
import com.acme.maintenance.v1.scheduleWorkOrderExpiryResponse
import com.acme.maintenance.v1.serviceSchedule
import com.acme.maintenance.v1.serviceScheduleParticipant
import com.acme.maintenance.v1.suspendWorkOrderResponse
import com.acme.maintenance.v1.validatePartRequest
import com.acme.maintenance.v1.workOrder
import com.acme.maintenance.v1.workOrderComment
import com.acme.seating.v1.SeatMapCalculationServiceGrpcKt
import com.acme.seating.v1.explainSeatMapCalculationRequest
import com.acme.seating.v1.seatMap
import com.acme.transport.maintenance.MaintenanceRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Moves a work order between the states of its lifecycle. */
class WorkOrderLifecycleController(
    private val store: MaintenanceRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyService: AccessPolicyServiceGrpcKt.AccessPolicyServiceCoroutineStub,
    private val concessionsConcessionStandService: ConcessionStandServiceGrpcKt.ConcessionStandServiceCoroutineStub,
    private val deliveriesDeliveryExceptionApprovalService: DeliveryExceptionApprovalServiceGrpcKt.DeliveryExceptionApprovalServiceCoroutineStub,
    private val deliveriesProofOfDeliveryWorkflowService: ProofOfDeliveryWorkflowServiceGrpcKt.ProofOfDeliveryWorkflowServiceCoroutineStub,
    private val documentsDocumentValidationService: DocumentValidationServiceGrpcKt.DocumentValidationServiceCoroutineStub,
    private val driversDriverLicenseValidationService: DriverLicenseValidationServiceGrpcKt.DriverLicenseValidationServiceCoroutineStub,
    private val driversDutyStatusValidationService: DutyStatusValidationServiceGrpcKt.DutyStatusValidationServiceCoroutineStub,
    private val ledgerLedgerAccountService: LedgerAccountServiceGrpcKt.LedgerAccountServiceCoroutineStub,
    private val maintenancePartCalculationService: PartCalculationServiceGrpcKt.PartCalculationServiceCoroutineStub,
    private val maintenancePartValidationService: PartValidationServiceGrpcKt.PartValidationServiceCoroutineStub,
    private val maintenanceServiceScheduleCalculationService: ServiceScheduleCalculationServiceGrpcKt.ServiceScheduleCalculationServiceCoroutineStub,
    private val maintenanceWorkOrderCalculationService: WorkOrderCalculationServiceGrpcKt.WorkOrderCalculationServiceCoroutineStub,
    private val seatingSeatMapCalculationService: SeatMapCalculationServiceGrpcKt.SeatMapCalculationServiceCoroutineStub,
) : WorkOrderLifecycleServiceGrpcKt.WorkOrderLifecycleServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(WorkOrderLifecycleController::class.java)

    private val invokeReinstateWorkOrder1: suspend (CloneAccessPolicyRequest) -> CloneAccessPolicyResponse = { payload ->
        authorizationAccessPolicyService.cloneAccessPolicy(payload)
    }

    private val workerForScheduleWorkOrderExpiry1 = ReplyMailbox<QuoteServiceScheduleRequest, QuoteServiceScheduleResponse>(workers) { payload ->
        withTimeout(5_000) { maintenanceServiceScheduleCalculationService.quoteServiceSchedule(payload) }
    }

    private val clientForSuspendWorkOrder0 by lazy { concessionsConcessionStandService }

    /** Activates a work order. */
    override suspend fun activateWorkOrder(request: ActivateWorkOrderRequest): ActivateWorkOrderResponse = handleActivateWorkOrder(request)

    /** Expires a work order. */
    override suspend fun expireWorkOrder(request: ExpireWorkOrderRequest): ExpireWorkOrderResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderLifecycleService/ExpireWorkOrder") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExpireWorkOrder", request)
            log.atInfo().addKeyValue("operation", "ExpireWorkOrder").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = request.toQuoteServiceScheduleFromExpireWorkOrder0()
            val reply0 = maintenanceServiceScheduleCalculationService.quoteServiceSchedule(outbound0)
            val outbound1 = estimateServiceScheduleRequest {
                this.context = request.context
                this.serviceSchedule = serviceSchedule {
                    this.participants.addAll(listOf(serviceScheduleParticipant {
                        this.contact = contactInfo {
                            this.email = request.details.newOwner.email
                        }
                    }))
                }
            }
            val reply1 = maintenanceServiceScheduleCalculationService.estimateServiceSchedule(outbound1)
            val outbound2 = lintDocumentsRequest {
                this.context = request.context
                this.document = document {
                    this.audit = auditInfo {
                        this.createdBy = reply1.serviceSchedule.audit.createdBy
                        this.updatedAt = reply1.serviceSchedule.audit.updatedAt
                        this.updatedBy = reply0.serviceSchedule.audit.updatedBy
                    }
                    this.billingAddress = postalAddress {
                        this.region = reply0.serviceSchedule.location.region
                    }
                }
            }
            val envelope2 = ProtoAny.pack(outbound2)
            val reply2 = documentsDocumentValidationService.lintDocuments(envelope2.unpack(LintDocumentsRequest::class.java))
            val outbound3 = watchLedgerAccountsRequest {
                this.context = request.context
                this.filter = WatchLedgerAccountsRequestKt.filter {
                    this.region = reply2.document.billingAddress.region
                }
                this.subscriber = contactInfo {
                    this.email = request.details.notifyContact.email
                    this.name = request.details.notifyContact.name
                    this.phone = request.details.notifyContact.phone
                }
            }
            ledgerLedgerAccountService.watchLedgerAccounts(outbound3).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            expireWorkOrderResponse {
                this.outcome = ExpireWorkOrderResponseKt.outcome {
                    this.effectiveAt = reply2.outcome.effectiveAt
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
                this.workOrder = workOrder {
                    this.assignee = userRef {
                        this.displayName = reply0.serviceSchedule.organizer.displayName
                    }
                    this.requester = contactInfo {
                        this.email = reply1.serviceSchedule.organizer.email
                        this.preferredChannel = request.details.notifyContact.preferredChannel
                    }
                    this.workOrderId = request.workOrderId
                }
            }
        }

    /** Returns a work order. */
    override suspend fun getWorkOrderLifecycle(request: GetWorkOrderLifecycleRequest): GetWorkOrderLifecycleResponse {
        return handleGetWorkOrderLifecycle(request)
    }

    /** Reinstates a work order. */
    override suspend fun reinstateWorkOrder(request: ReinstateWorkOrderRequest): ReinstateWorkOrderResponse = handleReinstateWorkOrder(request)

    /** Renews a work order. */
    override suspend fun renewWorkOrder(request: RenewWorkOrderRequest): RenewWorkOrderResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderLifecycleService/RenewWorkOrder") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RenewWorkOrder", request)
            log.atInfo().addKeyValue("operation", "RenewWorkOrder").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = EscalateDeliveryExceptionRequest.newBuilder().apply {
                setContext(request.context)
                setDeliveryException(deliveryException {
                    this.assignee = userRef {
                        this.userId = request.details.newOwner.userId
                    }
                    this.requester = contactInfo {
                        this.email = request.details.newOwner.email
                    }
                })
                setDetails(EscalateDeliveryExceptionRequestKt.details {
                    this.approver = userRef {
                        this.email = request.details.notifyContact.email
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> deliveriesDeliveryExceptionApprovalService.escalateDeliveryException(value) }
            val outbound1 = RequestFactory.create {
                explainDutyStatusDecisionRequest {
                    this.context = request.context
                    this.details = ExplainDutyStatusDecisionRequestKt.details {
                        this.contact = contactInfo {
                            this.email = reply0.deliveryException.requester.email
                            this.name = reply0.deliveryException.requester.name
                        }
                    }
                }
            }
            val reply1 = driversDutyStatusValidationService.explainDutyStatusDecision(outbound1)
            val outbound2 = estimatePartRequest {
                this.context = request.context
                this.part = part {
                    this.owner = userRef {
                        this.displayName = reply1.dutyStatus.subject.displayName
                        this.email = request.details.notifyContact.email
                    }
                }
            }
            val reply2 = ClientRegistry.lookup("acme.maintenance.v1.PartCalculationService", PartCalculationServiceGrpcKt.PartCalculationServiceCoroutineStub::class.java).estimatePart(outbound2)
            renewWorkOrderResponse {
                this.outcome = RenewWorkOrderResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.workOrder = workOrder {
                    this.assignee = userRef {
                        this.displayName = reply0.deliveryException.customer.displayName
                        this.userId = reply2.part.owner.userId
                    }
                    this.comments.addAll(listOf(workOrderComment {
                        this.createdAt = reply2.part.audit.createdAt
                    }))
                    this.requester = contactInfo {
                        this.email = reply0.deliveryException.requester.email
                    }
                    this.workOrderId = request.workOrderId
                }
            }
        }

    /** Schedules a work order. */
    override suspend fun scheduleWorkOrderExpiry(request: ScheduleWorkOrderExpiryRequest): ScheduleWorkOrderExpiryResponse {
        return handleScheduleWorkOrderExpiry(request)
    }

    /** Suspends a work order. */
    override suspend fun suspendWorkOrder(request: SuspendWorkOrderRequest): SuspendWorkOrderResponse = handleSuspendWorkOrder(request)

    private suspend fun handleActivateWorkOrder(request: ActivateWorkOrderRequest): ActivateWorkOrderResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderLifecycleService/ActivateWorkOrder") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ActivateWorkOrder", request)
            log.atInfo().addKeyValue("operation", "ActivateWorkOrder").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = explainSeatMapCalculationRequest {
                this.context = request.context
                this.seatMap = seatMap {
                    this.children.addAll(listOf(seatMap {
                        this.children.addAll(listOf(seatMap {
                            this.manager = userRef {
                                this.displayName = request.details.newOwner.displayName
                            }
                        }))
                        this.manager = userRef {
                            this.email = request.details.notifyContact.email
                        }
                    }))
                }
            }
            val reply0 = seatingSeatMapCalculationService.explainSeatMapCalculation(outbound0)
            val outbound1 = comparePartOptionsRequest {}.maintenanceCopy {
                this.context = request.context
                this.part = part {
                    this.address = postalAddress {
                        this.recipient = reply0.seatMap.location.recipient
                        this.region = reply0.seatMap.location.region
                    }
                }
            }
            val reply1 = maintenancePartCalculationService.comparePartOptions(outbound1)
            activateWorkOrderResponse {
                this.outcome = ActivateWorkOrderResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
                this.workOrder = workOrder {
                    this.audit = auditInfo {
                        this.createdBy = reply1.part.audit.createdBy
                    }
                    this.requester = contactInfo {
                        this.email = reply0.seatMap.manager.email
                    }
                    this.workOrderId = request.workOrderId
                }
            }
        }

    private fun ExpireWorkOrderRequest.toQuoteServiceScheduleFromExpireWorkOrder0(): QuoteServiceScheduleRequest {
        val source = this
        return quoteServiceScheduleRequest {
            this.context = source.context
            this.serviceSchedule = serviceSchedule {
                this.participants.addAll(listOf(serviceScheduleParticipant {
                    this.contact = contactInfo {
                        this.email = source.details.notifyContact.email
                        this.phone = source.details.notifyContact.phone
                        this.preferredChannel = source.details.notifyContact.preferredChannel
                    }
                }))
            }
        }
    }

    private data class GetWorkOrderLifecycle0Command(
        val context: RequestContext,
        val detailsContactEmail: String,
        val detailsLocale: String
    ) {
        fun toRequest(): ValidatePartRequest {
            val command = this
            return validatePartRequest {
                this.context = command.context
                this.details = ValidatePartRequestKt.details {
                    this.contact = contactInfo {
                        this.email = command.detailsContactEmail
                    }
                    this.locale = command.detailsLocale
                }
            }
        }
    }

    private suspend fun handleGetWorkOrderLifecycle(request: GetWorkOrderLifecycleRequest): GetWorkOrderLifecycleResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderLifecycleService/GetWorkOrderLifecycle") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetWorkOrderLifecycle", request)
            log.atInfo().addKeyValue("operation", "GetWorkOrderLifecycle").log("Handling transport request")
            val outbound0 = GetWorkOrderLifecycle0Command(
                context = request.context,
                detailsContactEmail = request.context.actor.email,
                detailsLocale = request.context.locale
            ).toRequest()
            val reply0 = maintenancePartValidationService.validatePart(outbound0)
            val outbound1 = explainWorkOrderCalculationRequest {
                this.context = request.context
                this.workOrder = workOrder {
                    this.assignee = userRef {
                        this.displayName = reply0.part.owner.displayName
                        this.userId = reply0.part.owner.userId
                    }
                }
            }
            val command1: Submission<ExplainWorkOrderCalculationRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = maintenanceWorkOrderCalculationService.explainWorkOrderCalculation(payload1)
            getWorkOrderLifecycleResponse {
                this.result = operationResult {
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = reply1.result.status
                }
                this.workOrder = workOrder {
                    this.requester = contactInfo {
                        this.email = reply0.part.owner.email
                    }
                    this.workOrderId = request.workOrderId
                }
            }
        }

    private suspend fun handleReinstateWorkOrder(request: ReinstateWorkOrderRequest): ReinstateWorkOrderResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderLifecycleService/ReinstateWorkOrder") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ReinstateWorkOrder", request)
            log.atInfo().addKeyValue("operation", "ReinstateWorkOrder").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = validateDutyStatusRequest {
                this.context = request.context
                this.details = ValidateDutyStatusRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.details.newOwner.email
                    }
                }
            }
            val adapter0 = OutboundPort<ValidateDutyStatusRequest, ValidateDutyStatusResponse> { payload ->
                driversDutyStatusValidationService.validateDutyStatus(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = cloneAccessPolicyRequest {
                this.accessPolicy = accessPolicy {
                    this.owner = userRef {
                        this.displayName = reply0.dutyStatus.subject.displayName
                        this.email = request.details.newOwner.email
                        this.userId = request.details.newOwner.userId
                    }
                }
                this.context = request.context
            }
            val reply1 = invokeReinstateWorkOrder1(outbound1)
            val outbound2 = explainDutyStatusDecisionRequest {
                this.context = request.context
                this.details = ExplainDutyStatusDecisionRequestKt.details {
                    this.contact = contactInfo {
                        this.phone = request.details.notifyContact.phone
                    }
                }
                this.dutyStatus = dutyStatus {
                    this.subject = driverLicenseRef {
                        this.driverLicenseId = reply0.dutyStatus.subject.driverLicenseId
                    }
                }
            }
            val invoke2: suspend (ExplainDutyStatusDecisionRequest, Metadata) -> ExplainDutyStatusDecisionResponse = driversDutyStatusValidationService::explainDutyStatusDecision
            val reply2 = invoke2(outbound2, Metadata())
            reinstateWorkOrderResponse {
                this.outcome = ReinstateWorkOrderResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.message = reply2.result.message
                    this.status = reply0.result.status
                }
                this.workOrder = workOrder {
                    this.audit = auditInfo {
                        this.createdAt = reply1.accessPolicy.audit.createdAt
                    }
                    this.workOrderId = request.workOrderId
                }
            }
        }

    private suspend fun handleScheduleWorkOrderExpiry(request: ScheduleWorkOrderExpiryRequest): ScheduleWorkOrderExpiryResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderLifecycleService/ScheduleWorkOrderExpiry") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScheduleWorkOrderExpiry", request)
            log.atInfo().addKeyValue("operation", "ScheduleWorkOrderExpiry").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.workOrder.workOrderId).log("Recorded audit detail")
            val outbound0 = scoreDriverLicenseRequest {
                this.context = request.context
                this.driverLicense = driverLicense {
                    this.audit = auditInfo {
                        this.createdAt = request.workOrder.audit.createdAt
                        this.createdBy = request.workOrder.audit.createdBy
                    }
                    this.counterparty = driverRef {
                        this.displayName = request.workOrder.customer.displayName
                        this.email = request.workOrder.customer.email
                    }
                }
            }
            val reply0 = driversDriverLicenseValidationService.scoreDriverLicense(WireCopies.reflective(outbound0))
            val outbound1 = quoteServiceScheduleRequest {
                this.context = request.context
                this.serviceSchedule = serviceSchedule {
                    this.participants.addAll(listOf(serviceScheduleParticipant {
                        this.contact = contactInfo {
                            this.email = request.workOrder.requester.email
                        }
                    }))
                }
            }
            val reply1 = workerForScheduleWorkOrderExpiry1.submit(outbound1)
            scheduleWorkOrderExpiryResponse {
                this.outcome = ScheduleWorkOrderExpiryResponseKt.outcome {
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.workOrder = workOrder {
                    this.audit = auditInfo {
                        this.createdAt = reply0.driverLicense.audit.createdAt
                    }
                    this.comments.addAll(listOf(workOrderComment {
                        this.createdAt = reply0.driverLicense.audit.createdAt
                    }))
                    this.customFields.putAll(request.workOrder.customFieldsMap)
                    this.workOrderId = request.workOrderId
                }
            }
        }

    private suspend fun handleSuspendWorkOrder(request: SuspendWorkOrderRequest): SuspendWorkOrderResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderLifecycleService/SuspendWorkOrder") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SuspendWorkOrder", request)
            log.atInfo().addKeyValue("operation", "SuspendWorkOrder").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = cloneConcessionStandRequest {
                this.concessionStand = concessionStand {
                    this.audit = auditInfo {
                        this.createdAt = request.details.effectiveAt
                    }
                    this.owner = userRef {
                        this.displayName = request.details.newOwner.displayName
                        this.email = request.details.notifyContact.email
                        this.userId = request.details.newOwner.userId
                    }
                }
                this.context = request.context
            }
            val reply0 = clientForSuspendWorkOrder0.cloneConcessionStand(outbound0)
            val outbound1 = resumeProofOfDeliveryWorkflowRequest {
                this.context = request.context
                this.details = ResumeProofOfDeliveryWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.email = request.details.newOwner.email
                    }
                }
                this.proofOfDelivery = proofOfDelivery {
                    this.audit = auditInfo {
                        this.createdBy = reply0.concessionStand.audit.createdBy
                    }
                    this.billingAddress = postalAddress {
                        this.lines.addAll(reply0.concessionStand.address.linesList)
                        this.recipient = reply0.concessionStand.address.recipient
                    }
                }
            }
            val reply1 = flowOf(outbound1).map { value -> deliveriesProofOfDeliveryWorkflowService.resumeProofOfDeliveryWorkflow(value) }.single()
            suspendWorkOrderResponse {
                this.outcome = SuspendWorkOrderResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.referenceId = reply1.outcome.referenceId
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
                this.workOrder = workOrder {
                    this.audit = auditInfo {
                        this.etag = reply1.proofOfDelivery.audit.etag
                        this.updatedAt = reply0.concessionStand.audit.updatedAt
                    }
                    this.workOrderId = request.workOrderId
                }
            }
        }
}
