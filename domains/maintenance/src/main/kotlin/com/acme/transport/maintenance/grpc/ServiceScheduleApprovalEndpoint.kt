package com.acme.transport.maintenance.grpc

import com.acme.authorization.v1.AccessPolicyValidationServiceGrpcKt
import com.acme.authorization.v1.ExplainAccessPolicyDecisionRequestKt
import com.acme.authorization.v1.accessPolicy
import com.acme.authorization.v1.explainAccessPolicyDecisionRequest
import com.acme.authorization.v1.lintAccessPoliciesRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.DeliveryApprovalServiceGrpcKt
import com.acme.deliveries.v1.delivery
import com.acme.deliveries.v1.deliveryExceptionRef
import com.acme.deliveries.v1.submitDeliveryRequest
import com.acme.documents.v1.DocumentQueryServiceGrpcKt
import com.acme.documents.v1.FindDocumentsRequestKt
import com.acme.documents.v1.copy as documentsCopy
import com.acme.documents.v1.findDocumentsRequest
import com.acme.drivers.v1.DriverCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverValidationServiceGrpcKt
import com.acme.drivers.v1.DutyStatusCalculationServiceGrpcKt
import com.acme.drivers.v1.EstimateDriverResponse
import com.acme.drivers.v1.EstimateDutyStatusRequestKt
import com.acme.drivers.v1.NormalizeDriverRequest
import com.acme.drivers.v1.NormalizeDriverResponse
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.estimateDriverRequest
import com.acme.drivers.v1.estimateDutyStatusRequest
import com.acme.drivers.v1.normalizeDriverRequest
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.LedgerAccountValidationServiceGrpcKt
import com.acme.ledger.v1.ValidateLedgerAccountRequest
import com.acme.ledger.v1.ValidateLedgerAccountRequestKt
import com.acme.ledger.v1.ledgerAccount
import com.acme.ledger.v1.postingBatchRef
import com.acme.ledger.v1.validateLedgerAccountRequest
import com.acme.maintenance.v1.ApproveServiceScheduleRequest
import com.acme.maintenance.v1.ApproveServiceScheduleResponse
import com.acme.maintenance.v1.CalculatePartRequest
import com.acme.maintenance.v1.CalculatePartRequestKt
import com.acme.maintenance.v1.CalculatePartResponse
import com.acme.maintenance.v1.CommentServiceScheduleApprovalRequest
import com.acme.maintenance.v1.CommentServiceScheduleApprovalResponse
import com.acme.maintenance.v1.DelegateServiceScheduleApprovalRequest
import com.acme.maintenance.v1.DelegateServiceScheduleApprovalResponse
import com.acme.maintenance.v1.DelegateWorkOrderApprovalRequestKt
import com.acme.maintenance.v1.EscalateServiceScheduleRequest
import com.acme.maintenance.v1.EscalateServiceScheduleResponse
import com.acme.maintenance.v1.EstimatePartRequest
import com.acme.maintenance.v1.GetServiceScheduleApprovalStatusRequest
import com.acme.maintenance.v1.GetServiceScheduleApprovalStatusResponse
import com.acme.maintenance.v1.ListPartReportsRequest
import com.acme.maintenance.v1.ListPartReportsRequestKt
import com.acme.maintenance.v1.ListPartReportsResponse
import com.acme.maintenance.v1.ListPendingServiceSchedulesRequest
import com.acme.maintenance.v1.ListPendingServiceSchedulesResponse
import com.acme.maintenance.v1.PartCalculationServiceGrpcKt
import com.acme.maintenance.v1.PartComponent
import com.acme.maintenance.v1.PartReportingServiceGrpcKt
import com.acme.maintenance.v1.RecalculateWorkOrdersRequestKt
import com.acme.maintenance.v1.RecallServiceScheduleRequest
import com.acme.maintenance.v1.RecallServiceScheduleResponse
import com.acme.maintenance.v1.RecallServiceScheduleResponseKt
import com.acme.maintenance.v1.RecallWorkOrderRequest
import com.acme.maintenance.v1.RecallWorkOrderResponse
import com.acme.maintenance.v1.ScheduleWorkOrderExpiryRequestKt
import com.acme.maintenance.v1.ServiceScheduleApprovalServiceGrpcKt
import com.acme.maintenance.v1.SubmitServiceScheduleRequest
import com.acme.maintenance.v1.SubmitServiceScheduleResponse
import com.acme.maintenance.v1.WorkOrderApprovalServiceGrpcKt
import com.acme.maintenance.v1.WorkOrderCalculationServiceGrpcKt
import com.acme.maintenance.v1.WorkOrderLifecycleServiceGrpcKt
import com.acme.maintenance.v1.approveServiceScheduleResponse
import com.acme.maintenance.v1.calculatePartRequest
import com.acme.maintenance.v1.commentServiceScheduleApprovalResponse
import com.acme.maintenance.v1.delegateServiceScheduleApprovalResponse
import com.acme.maintenance.v1.delegateWorkOrderApprovalRequest
import com.acme.maintenance.v1.escalateServiceScheduleResponse
import com.acme.maintenance.v1.estimatePartRequest
import com.acme.maintenance.v1.getServiceScheduleApprovalStatusResponse
import com.acme.maintenance.v1.listPartReportsRequest
import com.acme.maintenance.v1.listPendingServiceSchedulesResponse
import com.acme.maintenance.v1.part
import com.acme.maintenance.v1.recalculateWorkOrdersRequest
import com.acme.maintenance.v1.recallServiceScheduleResponse
import com.acme.maintenance.v1.recallWorkOrderRequest
import com.acme.maintenance.v1.scheduleWorkOrderExpiryRequest
import com.acme.maintenance.v1.serviceSchedule
import com.acme.maintenance.v1.serviceScheduleParticipant
import com.acme.maintenance.v1.submitServiceScheduleResponse
import com.acme.maintenance.v1.workOrder
import com.acme.risk.v1.BrowseRiskRegistersRequest
import com.acme.risk.v1.BrowseRiskRegistersRequestKt
import com.acme.risk.v1.RiskRegisterQueryServiceGrpcKt
import com.acme.risk.v1.browseRiskRegistersRequest
import com.acme.transport.maintenance.MaintenanceRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.GenerateYardMoveReportRequestKt
import com.acme.yards.v1.YardMoveReportingServiceGrpcKt
import com.acme.yards.v1.generateYardMoveReportRequest
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Routes service schedules through approval, with escalation and delegation. */
class ServiceScheduleApprovalEndpoint(
    private val store: MaintenanceRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyValidationService: AccessPolicyValidationServiceGrpcKt.AccessPolicyValidationServiceCoroutineStub,
    private val deliveriesDeliveryApprovalService: DeliveryApprovalServiceGrpcKt.DeliveryApprovalServiceCoroutineStub,
    private val documentsDocumentQueryService: DocumentQueryServiceGrpcKt.DocumentQueryServiceCoroutineStub,
    private val driversDriverCalculationService: DriverCalculationServiceGrpcKt.DriverCalculationServiceCoroutineStub,
    private val driversDriverValidationService: DriverValidationServiceGrpcKt.DriverValidationServiceCoroutineStub,
    private val driversDutyStatusCalculationService: DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineStub,
    private val ledgerLedgerAccountValidationService: LedgerAccountValidationServiceGrpcKt.LedgerAccountValidationServiceCoroutineStub,
    private val maintenancePartCalculationService: PartCalculationServiceGrpcKt.PartCalculationServiceCoroutineStub,
    private val maintenancePartReportingService: PartReportingServiceGrpcKt.PartReportingServiceCoroutineStub,
    private val maintenanceWorkOrderApprovalService: WorkOrderApprovalServiceGrpcKt.WorkOrderApprovalServiceCoroutineStub,
    private val maintenanceWorkOrderCalculationService: WorkOrderCalculationServiceGrpcKt.WorkOrderCalculationServiceCoroutineStub,
    private val maintenanceWorkOrderLifecycleService: WorkOrderLifecycleServiceGrpcKt.WorkOrderLifecycleServiceCoroutineStub,
    private val riskRiskRegisterQueryService: RiskRegisterQueryServiceGrpcKt.RiskRegisterQueryServiceCoroutineStub,
    private val yardsYardMoveReportingService: YardMoveReportingServiceGrpcKt.YardMoveReportingServiceCoroutineStub,
) : ServiceScheduleApprovalServiceGrpcKt.ServiceScheduleApprovalServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(ServiceScheduleApprovalEndpoint::class.java)

    private val invokeCommentServiceScheduleApproval1: suspend (ListPartReportsRequest) -> ListPartReportsResponse = { payload ->
        maintenancePartReportingService.listPartReports(payload)
    }

    private val workerForListPendingServiceSchedules1 = ReplyMailbox<NormalizeDriverRequest, NormalizeDriverResponse>(workers) { payload ->
        withTimeout(5_000) { driversDriverValidationService.normalizeDriver(payload) }
    }

    private val clientForRecallServiceSchedule0 by lazy { driversDriverCalculationService }

    /** Approves a service schedule. */
    override suspend fun approveServiceSchedule(request: ApproveServiceScheduleRequest): ApproveServiceScheduleResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleApprovalService/ApproveServiceSchedule") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ApproveServiceSchedule", request)
            log.atInfo().addKeyValue("operation", "ApproveServiceSchedule").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.serviceSchedule.serviceScheduleId).log("Recorded audit detail")
            var reply0 = EstimateDriverResponse.getDefaultInstance()
            request.serviceSchedule.participantsList.asFlow().map { item ->
                val outbound0 = estimateDriverRequest {
                    this.context = request.context
                    this.driver = driver {
                        this.contact = contactInfo {
                            this.preferredChannel = item.contact.preferredChannel
                        }
                        this.relationshipManager = userRef {
                            this.email = item.contact.email
                        }
                    }
                }
                driversDriverCalculationService.estimateDriver(outbound0)
            }.collect { reply0 = it }
            val outbound1 = ApproveServiceSchedule1Command(
                context = request.context,
                partAddressRecipient = reply0.driver.primaryAddress.recipient
            ).toRequest()
            val reply1 = maintenancePartCalculationService.estimatePart(outbound1)
            val outbound2 = validateLedgerAccountRequest {
                this.context = request.context
                this.details = ValidateLedgerAccountRequestKt.details {
                    this.contact = contactInfo {
                        this.phone = reply0.driver.contact.phone
                    }
                }
                this.ledgerAccount = ledgerAccount {
                    this.holder = postingBatchRef {
                        this.displayName = request.details.approver.displayName
                    }
                }
            }
            val command2: Submission<ValidateLedgerAccountRequest> = Submission.Live(outbound2)
            val payload2 = when (val command = command2) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply2 = ledgerLedgerAccountValidationService.validateLedgerAccount(payload2)
            approveServiceScheduleResponse {
                this.result = operationResult {
                    this.message = reply2.result.message
                    this.status = "ok"
                }
                this.serviceSchedule = serviceSchedule {
                    this.audit = auditInfo {
                        this.etag = reply1.part.audit.etag
                    }
                    this.location = postalAddress {
                        this.lines.addAll(reply1.part.address.linesList)
                    }
                    this.serviceScheduleId = request.serviceScheduleId
                    this.window = request.serviceSchedule.window
                }
            }
        }

    /** Comments a service schedule. */
    override suspend fun commentServiceScheduleApproval(request: CommentServiceScheduleApprovalRequest): CommentServiceScheduleApprovalResponse {
        return handleCommentServiceScheduleApproval(request)
    }

    /** Delegates a service schedule. */
    override suspend fun delegateServiceScheduleApproval(request: DelegateServiceScheduleApprovalRequest): DelegateServiceScheduleApprovalResponse = handleDelegateServiceScheduleApproval(request)

    /** Escalates a service schedule. */
    override suspend fun escalateServiceSchedule(request: EscalateServiceScheduleRequest): EscalateServiceScheduleResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleApprovalService/EscalateServiceSchedule") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EscalateServiceSchedule", request)
            log.atInfo().addKeyValue("operation", "EscalateServiceSchedule").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.serviceSchedule.serviceScheduleId).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                estimateDutyStatusRequest {
                    this.context = request.context
                    this.details = EstimateDutyStatusRequestKt.details {
                        this.destination = postalAddress {
                            this.countryCode = request.serviceSchedule.location.countryCode
                        }
                    }
                }
            }
            val reply0 = driversDutyStatusCalculationService.estimateDutyStatus(outbound0)
            val outbound1 = delegateWorkOrderApprovalRequest {
                this.context = request.context
                this.details = DelegateWorkOrderApprovalRequestKt.details {
                    this.approver = userRef {
                        this.displayName = reply0.dutyStatus.subject.displayName
                    }
                }
                this.workOrder = workOrder {
                    this.audit = auditInfo {
                        this.createdAt = request.serviceSchedule.audit.createdAt
                    }
                    this.customer = driverRef {
                        this.displayName = request.details.approver.displayName
                        this.email = request.details.approver.email
                    }
                }
            }
            val reply1 = ClientRegistry.lookup("acme.maintenance.v1.WorkOrderApprovalService", WorkOrderApprovalServiceGrpcKt.WorkOrderApprovalServiceCoroutineStub::class.java).delegateWorkOrderApproval(outbound1)
            escalateServiceScheduleResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
                this.serviceSchedule = serviceSchedule {
                    this.metadata.putAll(request.serviceSchedule.metadataMap)
                    this.organizer = userRef {
                        this.email = reply1.workOrder.customer.email
                    }
                    this.participants.addAll(listOf(serviceScheduleParticipant {
                        this.contact = contactInfo {
                            this.name = reply1.workOrder.requester.name
                        }
                    }))
                    this.serviceScheduleId = request.serviceScheduleId
                }
            }
        }

    /** Returns a service schedule. */
    override suspend fun getServiceScheduleApprovalStatus(request: GetServiceScheduleApprovalStatusRequest): GetServiceScheduleApprovalStatusResponse {
        return handleGetServiceScheduleApprovalStatus(request)
    }

    /** Lists service schedules a page at a time. */
    override suspend fun listPendingServiceSchedules(request: ListPendingServiceSchedulesRequest): ListPendingServiceSchedulesResponse = handleListPendingServiceSchedules(request)

    /** Recalls a service schedule. */
    override suspend fun recallServiceSchedule(request: RecallServiceScheduleRequest): RecallServiceScheduleResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleApprovalService/RecallServiceSchedule") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecallServiceSchedule", request)
            log.atInfo().addKeyValue("operation", "RecallServiceSchedule").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.serviceSchedule.serviceScheduleId).log("Recorded audit detail")
            val outbound0 = estimateDriverRequest {
                this.context = request.context
                this.driver = driver {
                    this.primaryAddress = postalAddress {
                        this.postalCode = request.serviceSchedule.location.postalCode
                    }
                    this.relationshipManager = userRef {
                        this.displayName = request.serviceSchedule.organizer.displayName
                        this.userId = request.details.approver.userId
                    }
                }
            }
            val reply0 = clientForRecallServiceSchedule0.estimateDriver(outbound0)
            val outbound1 = lintAccessPoliciesRequest {
                this.accessPolicy = accessPolicy {
                    this.audit = auditInfo {
                        this.updatedBy = request.serviceSchedule.audit.updatedBy
                    }
                }
                this.context = request.context
            }
            val reply1 = flowOf(outbound1).map { value -> authorizationAccessPolicyValidationService.lintAccessPolicies(value) }.single()
            val outbound2 = submitDeliveryRequest {
                this.context = request.context
                this.delivery = delivery {
                    this.audit = auditInfo {
                        this.etag = request.serviceSchedule.audit.etag
                        this.updatedBy = request.serviceSchedule.audit.updatedBy
                    }
                    this.billingAddress = postalAddress {
                        this.region = reply0.driver.primaryAddress.region
                    }
                    this.counterparty = deliveryExceptionRef {
                        this.displayName = reply0.driver.relationshipManager.displayName
                    }
                }
            }
            val reply2 = withTimeout(5_000) { deliveriesDeliveryApprovalService.submitDelivery(outbound2) }
            val configure3: GenerateYardMoveReportRequestKt.Dsl.() -> Unit = {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.details = GenerateYardMoveReportRequestKt.details {
                    this.filters.putAll(reply1.accessPolicy.parametersMap)
                    this.period = reply0.driver.preferences.quietHours
                }
                this.reason = reply1.accessPolicy.block.reason
            }
            val outbound3 = generateYardMoveReportRequest(configure3)
            val reply3 = yardsYardMoveReportingService.generateYardMoveReport(outbound3)
            recallServiceScheduleResponse {
                this.outcome = RecallServiceScheduleResponseKt.outcome {
                    this.message = reply1.result.message
                }
                this.result = operationResult {
                    this.correlationId = reply3.result.correlationId
                    this.status = "ok"
                }
                this.serviceSchedule = serviceSchedule {
                    this.audit = auditInfo {
                        this.createdBy = reply2.delivery.audit.createdBy
                    }
                    this.organizer = userRef {
                        this.email = reply0.driver.contact.email
                    }
                    this.serviceScheduleId = request.serviceScheduleId
                    this.status = request.serviceSchedule.status
                }
            }
        }

    /** Submits a service schedule. */
    override suspend fun submitServiceSchedule(request: SubmitServiceScheduleRequest): SubmitServiceScheduleResponse {
        return handleSubmitServiceSchedule(request)
    }

    private data class ApproveServiceSchedule1Command(
        val context: RequestContext,
        val partAddressRecipient: String
    ) {
        fun toRequest(): EstimatePartRequest {
            val command = this
            return estimatePartRequest {
                this.context = command.context
                this.part = part {
                    this.address = postalAddress {
                        this.recipient = command.partAddressRecipient
                    }
                }
            }
        }
    }

    private suspend fun handleCommentServiceScheduleApproval(request: CommentServiceScheduleApprovalRequest): CommentServiceScheduleApprovalResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleApprovalService/CommentServiceScheduleApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CommentServiceScheduleApproval", request)
            log.atInfo().addKeyValue("operation", "CommentServiceScheduleApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.serviceSchedule.serviceScheduleId).log("Recorded audit detail")
            val outbound0 = calculatePartRequest {
                this.context = request.context
                this.details = CalculatePartRequestKt.details {
                    this.destination = postalAddress {
                        this.locality = request.serviceSchedule.location.locality
                        this.region = request.serviceSchedule.location.region
                    }
                }
                this.part = part {
                    this.owner = userRef {
                        this.displayName = request.details.approver.displayName
                    }
                }
            }
            val adapter0 = OutboundPort<CalculatePartRequest, CalculatePartResponse> { payload ->
                maintenancePartCalculationService.calculatePart(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = listPartReportsRequest {
                this.context = request.context
                this.filter = ListPartReportsRequestKt.filter {
                    this.createdAfter = reply0.part.audit.createdAt
                    this.labels.putAll(reply0.part.specsMap)
                    this.ownerId = (reply0.part.componentsMap["primary"] ?: PartComponent.getDefaultInstance()).componentId
                    this.region = request.serviceSchedule.location.region
                }
            }
            val reply1 = invokeCommentServiceScheduleApproval1(outbound1)
            commentServiceScheduleApprovalResponse {
                this.result = operationResult {
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = reply1.result.status
                }
                this.serviceSchedule = serviceSchedule {
                    this.location = postalAddress {
                        this.postalCode = reply0.part.address.postalCode
                    }
                    this.metadata.putAll(request.serviceSchedule.metadataMap)
                    this.serviceScheduleId = request.serviceScheduleId
                }
            }
        }

    private suspend fun handleDelegateServiceScheduleApproval(request: DelegateServiceScheduleApprovalRequest): DelegateServiceScheduleApprovalResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleApprovalService/DelegateServiceScheduleApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DelegateServiceScheduleApproval", request)
            log.atInfo().addKeyValue("operation", "DelegateServiceScheduleApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.serviceSchedule.serviceScheduleId).log("Recorded audit detail")
            val outbound0 = recallWorkOrderRequest {
                this.context = request.context
                this.workOrder = workOrder {
                    this.audit = auditInfo {
                        this.createdAt = request.serviceSchedule.audit.createdAt
                    }
                    this.customer = driverRef {
                        this.displayName = request.details.approver.displayName
                    }
                }
            }
            val invoke0: suspend (RecallWorkOrderRequest, Metadata) -> RecallWorkOrderResponse = maintenanceWorkOrderApprovalService::recallWorkOrder
            val reply0 = invoke0(outbound0, Metadata())
            val outbound1 = BrowseRiskRegistersRequest.newBuilder().apply {
                setContext(request.context)
                setFilter(BrowseRiskRegistersRequestKt.filter {
                    this.createdAfter = reply0.workOrder.audit.updatedAt
                    this.labels.putAll(reply0.workOrder.customFieldsMap)
                    this.ownerId = reply0.workOrder.sla.policyId
                    this.region = request.serviceSchedule.location.region
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> riskRiskRegisterQueryService.browseRiskRegisters(value) }
            delegateServiceScheduleApprovalResponse {
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.details.putAll(reply1.result.detailsMap)
                    this.message = reply0.result.message
                    this.status = "ok"
                }
                this.serviceSchedule = serviceSchedule {
                    this.serviceScheduleId = request.serviceScheduleId
                    this.window = request.serviceSchedule.window
                }
            }
        }

    private suspend fun handleGetServiceScheduleApprovalStatus(request: GetServiceScheduleApprovalStatusRequest): GetServiceScheduleApprovalStatusResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleApprovalService/GetServiceScheduleApprovalStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetServiceScheduleApprovalStatus", request)
            log.atInfo().addKeyValue("operation", "GetServiceScheduleApprovalStatus").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            getServiceScheduleApprovalStatusResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.serviceSchedule = serviceSchedule {
                    this.serviceScheduleId = request.serviceScheduleId
                }
            }
        }

    private suspend fun handleListPendingServiceSchedules(request: ListPendingServiceSchedulesRequest): ListPendingServiceSchedulesResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleApprovalService/ListPendingServiceSchedules") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListPendingServiceSchedules", request)
            log.atInfo().addKeyValue("operation", "ListPendingServiceSchedules").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = scheduleWorkOrderExpiryRequest {
                this.context = request.context
                this.details = ScheduleWorkOrderExpiryRequestKt.details {
                    this.notify = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
                this.workOrder = workOrder {
                    this.assignee = userRef {
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply0 = maintenanceWorkOrderLifecycleService.scheduleWorkOrderExpiry(WireCopies.reflective(outbound0))
            val outbound1 = normalizeDriverRequest {
                this.context = request.context
                this.driver = driver {
                    this.audit = auditInfo {
                        this.createdAt = reply0.workOrder.audit.createdAt
                        this.updatedAt = reply0.workOrder.audit.updatedAt
                    }
                }
            }
            val reply1 = workerForListPendingServiceSchedules1.submit(outbound1)
            listPendingServiceSchedulesResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
                this.serviceSchedules.addAll(listOf(serviceSchedule {
                    this.audit = auditInfo {
                        this.createdAt = reply0.workOrder.audit.createdAt
                    }
                    this.location = postalAddress {
                        this.locality = reply1.driver.primaryAddress.locality
                    }
                    this.participants.addAll(listOf(serviceScheduleParticipant {
                        this.contact = contactInfo {
                            this.email = reply1.driver.relationshipManager.email
                        }
                    }))
                }))
            }
        }

    private suspend fun handleSubmitServiceSchedule(request: SubmitServiceScheduleRequest): SubmitServiceScheduleResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleApprovalService/SubmitServiceSchedule") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SubmitServiceSchedule", request)
            log.atInfo().addKeyValue("operation", "SubmitServiceSchedule").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.serviceSchedule.serviceScheduleId).log("Recorded audit detail")
            val outbound0 = recalculateWorkOrdersRequest {
                this.context = request.context
                this.details = RecalculateWorkOrdersRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(request.serviceSchedule.location.linesList)
                    }
                }
                this.workOrder = workOrder {
                    this.assignee = userRef {
                        this.userId = request.details.approver.userId
                    }
                    this.audit = auditInfo {
                        this.etag = request.serviceSchedule.audit.etag
                    }
                }
            }
            val reply0 = maintenanceWorkOrderCalculationService.recalculateWorkOrders(outbound0)
            val outbound1 = explainAccessPolicyDecisionRequest {
                this.accessPolicy = accessPolicy {
                    this.audit = auditInfo {
                        this.createdAt = reply0.workOrder.audit.createdAt
                        this.etag = request.serviceSchedule.audit.etag
                        this.updatedBy = reply0.workOrder.audit.updatedBy
                    }
                }
                this.context = request.context
                this.details = ExplainAccessPolicyDecisionRequestKt.details {
                    this.contact = contactInfo {
                        this.name = reply0.workOrder.requester.name
                    }
                }
            }
            val reply1 = authorizationAccessPolicyValidationService.explainAccessPolicyDecision(outbound1)
            val outbound2 = findDocumentsRequest {}.documentsCopy {
                this.context = request.context
                this.filter = FindDocumentsRequestKt.filter {
                    this.createdAfter = reply0.workOrder.audit.updatedAt
                    this.ownerId = reply0.workOrder.assignee.userId
                    this.region = request.serviceSchedule.location.region
                }
            }
            val reply2 = documentsDocumentQueryService.findDocuments(outbound2)
            submitServiceScheduleResponse {
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.message = reply2.result.message
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = reply2.result.status
                }
                this.serviceSchedule = serviceSchedule {
                    this.metadata.putAll(request.serviceSchedule.metadataMap)
                    this.organizer = userRef {
                        this.displayName = reply0.workOrder.customer.displayName
                    }
                    this.serviceScheduleId = request.serviceScheduleId
                }
            }
        }
}
