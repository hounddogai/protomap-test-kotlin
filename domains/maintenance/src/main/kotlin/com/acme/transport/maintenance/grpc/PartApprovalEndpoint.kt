package com.acme.transport.maintenance.grpc

import com.acme.audit.v1.AuditEventServiceGrpcKt
import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.CheckAuditEventEligibilityRequest
import com.acme.audit.v1.CheckAuditEventEligibilityRequestKt
import com.acme.audit.v1.NormalizeAuditEventRequest
import com.acme.audit.v1.NormalizeAuditEventRequestKt
import com.acme.audit.v1.NormalizeAuditEventResponse
import com.acme.audit.v1.WatchAuditEventsRequestKt
import com.acme.audit.v1.auditEvent
import com.acme.audit.v1.auditTrailRef
import com.acme.audit.v1.checkAuditEventEligibilityRequest
import com.acme.audit.v1.normalizeAuditEventRequest
import com.acme.audit.v1.watchAuditEventsRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.drivers.v1.CalculateDriverLicenseResponse
import com.acme.drivers.v1.CheckDriverLicenseEligibilityRequest
import com.acme.drivers.v1.CompareDutyStatusOptionsRequestKt
import com.acme.drivers.v1.DriverCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseValidationServiceGrpcKt
import com.acme.drivers.v1.DutyStatusCalculationServiceGrpcKt
import com.acme.drivers.v1.calculateDriverLicenseRequest
import com.acme.drivers.v1.checkDriverLicenseEligibilityRequest
import com.acme.drivers.v1.compareDutyStatusOptionsRequest
import com.acme.drivers.v1.copy as driversCopy
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverLicenseLine
import com.acme.drivers.v1.driverLicenseLineTax
import com.acme.drivers.v1.driverLicenseRef
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.dutyStatus
import com.acme.drivers.v1.explainDriverCalculationRequest
import com.acme.drivers.v1.lintDriverLicensesRequest
import com.acme.expenses.v1.ExpenseReportCalculationServiceGrpcKt
import com.acme.expenses.v1.QuoteExpenseReportRequestKt
import com.acme.expenses.v1.expenseReport
import com.acme.expenses.v1.quoteExpenseReportRequest
import com.acme.featureflags.v1.CheckFeatureFlagEligibilityRequestKt
import com.acme.featureflags.v1.FeatureFlagServiceGrpcKt
import com.acme.featureflags.v1.FeatureFlagValidationServiceGrpcKt
import com.acme.featureflags.v1.batchGetFeatureFlagsRequest
import com.acme.featureflags.v1.checkFeatureFlagEligibilityRequest
import com.acme.featureflags.v1.featureFlag
import com.acme.fuel.v1.FuelCardExportServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyReportingServiceGrpcKt
import com.acme.fuel.v1.ListFuelPolicyReportsRequestKt
import com.acme.fuel.v1.ShareFuelCardExportRequestKt
import com.acme.fuel.v1.listFuelPolicyReportsRequest
import com.acme.fuel.v1.shareFuelCardExportRequest
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.ApprovePartRequest
import com.acme.maintenance.v1.ApprovePartResponse
import com.acme.maintenance.v1.ApprovePartResponseKt
import com.acme.maintenance.v1.CommentPartApprovalRequest
import com.acme.maintenance.v1.CommentPartApprovalResponse
import com.acme.maintenance.v1.CommentPartApprovalResponseKt
import com.acme.maintenance.v1.CommentServiceScheduleApprovalRequest
import com.acme.maintenance.v1.CommentServiceScheduleApprovalResponse
import com.acme.maintenance.v1.DelegatePartApprovalRequest
import com.acme.maintenance.v1.DelegatePartApprovalResponse
import com.acme.maintenance.v1.DelegatePartApprovalResponseKt
import com.acme.maintenance.v1.FetchPartReportRequest
import com.acme.maintenance.v1.GetPartApprovalStatusRequest
import com.acme.maintenance.v1.GetPartApprovalStatusResponse
import com.acme.maintenance.v1.ListPendingPartsRequest
import com.acme.maintenance.v1.ListPendingPartsResponse
import com.acme.maintenance.v1.Part
import com.acme.maintenance.v1.PartApprovalServiceGrpcKt
import com.acme.maintenance.v1.PartComponent
import com.acme.maintenance.v1.PartReportingServiceGrpcKt
import com.acme.maintenance.v1.RecallPartRequest
import com.acme.maintenance.v1.RecallPartResponse
import com.acme.maintenance.v1.RejectPartRequest
import com.acme.maintenance.v1.RejectPartResponse
import com.acme.maintenance.v1.RejectPartResponseKt
import com.acme.maintenance.v1.ServiceScheduleApprovalServiceGrpcKt
import com.acme.maintenance.v1.SubmitPartRequest
import com.acme.maintenance.v1.SubmitPartResponse
import com.acme.maintenance.v1.SubmitPartResponseKt
import com.acme.maintenance.v1.ValidateWorkOrderRequest
import com.acme.maintenance.v1.ValidateWorkOrderRequestKt
import com.acme.maintenance.v1.WorkOrderValidationServiceGrpcKt
import com.acme.maintenance.v1.approvePartResponse
import com.acme.maintenance.v1.commentPartApprovalResponse
import com.acme.maintenance.v1.commentServiceScheduleApprovalRequest
import com.acme.maintenance.v1.delegatePartApprovalResponse
import com.acme.maintenance.v1.fetchPartReportRequest
import com.acme.maintenance.v1.getPartApprovalStatusResponse
import com.acme.maintenance.v1.listPendingPartsResponse
import com.acme.maintenance.v1.part
import com.acme.maintenance.v1.partComponent
import com.acme.maintenance.v1.recallPartResponse
import com.acme.maintenance.v1.rejectPartResponse
import com.acme.maintenance.v1.serviceSchedule
import com.acme.maintenance.v1.serviceScheduleParticipant
import com.acme.maintenance.v1.submitPartResponse
import com.acme.maintenance.v1.validateWorkOrderRequest
import com.acme.maintenance.v1.workOrder
import com.acme.metrics.v1.CloneMetricSeriesRequest
import com.acme.metrics.v1.CloneMetricSeriesResponse
import com.acme.metrics.v1.MetricSeriesComponent
import com.acme.metrics.v1.MetricSeriesServiceGrpcKt
import com.acme.metrics.v1.cloneMetricSeriesRequest
import com.acme.metrics.v1.metricSeries
import com.acme.transport.maintenance.MaintenanceRepository
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.DockComponent
import com.acme.yards.v1.DockLifecycleServiceGrpcKt
import com.acme.yards.v1.dock
import com.acme.yards.v1.scheduleDockExpiryRequest
import com.google.protobuf.Any as ProtoAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Routes parts through approval, with escalation and delegation. */
class PartApprovalEndpoint(
    private val store: MaintenanceRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventService: AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub,
    private val auditAuditEventValidationService: AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub,
    private val driversDriverCalculationService: DriverCalculationServiceGrpcKt.DriverCalculationServiceCoroutineStub,
    private val driversDriverLicenseCalculationService: DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub,
    private val driversDriverLicenseValidationService: DriverLicenseValidationServiceGrpcKt.DriverLicenseValidationServiceCoroutineStub,
    private val driversDutyStatusCalculationService: DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineStub,
    private val expensesExpenseReportCalculationService: ExpenseReportCalculationServiceGrpcKt.ExpenseReportCalculationServiceCoroutineStub,
    private val featureflagsFeatureFlagService: FeatureFlagServiceGrpcKt.FeatureFlagServiceCoroutineStub,
    private val featureflagsFeatureFlagValidationService: FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub,
    private val fuelFuelCardExportService: FuelCardExportServiceGrpcKt.FuelCardExportServiceCoroutineStub,
    private val fuelFuelPolicyReportingService: FuelPolicyReportingServiceGrpcKt.FuelPolicyReportingServiceCoroutineStub,
    private val maintenancePartReportingService: PartReportingServiceGrpcKt.PartReportingServiceCoroutineStub,
    private val maintenanceServiceScheduleApprovalService: ServiceScheduleApprovalServiceGrpcKt.ServiceScheduleApprovalServiceCoroutineStub,
    private val maintenanceWorkOrderValidationService: WorkOrderValidationServiceGrpcKt.WorkOrderValidationServiceCoroutineStub,
    private val metricsMetricSeriesService: MetricSeriesServiceGrpcKt.MetricSeriesServiceCoroutineStub,
    private val yardsDockLifecycleService: DockLifecycleServiceGrpcKt.DockLifecycleServiceCoroutineStub,
) : PartApprovalServiceGrpcKt.PartApprovalServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(PartApprovalEndpoint::class.java)

    private val workerForApprovePart1 = ReplyMailbox<CommentServiceScheduleApprovalRequest, CommentServiceScheduleApprovalResponse>(workers) { payload ->
        withTimeout(5_000) { maintenanceServiceScheduleApprovalService.commentServiceScheduleApproval(payload) }
    }

    private val clientForCommentPartApproval0 by lazy { fuelFuelCardExportService }

    private val invokeSubmitPart2: suspend (CloneMetricSeriesRequest) -> CloneMetricSeriesResponse = { payload ->
        metricsMetricSeriesService.cloneMetricSeries(payload)
    }

    /** Approves a part. */
    override suspend fun approvePart(request: ApprovePartRequest): ApprovePartResponse =
        RpcBoundary.run("acme.maintenance.v1.PartApprovalService/ApprovePart") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ApprovePart", request)
            log.atInfo().addKeyValue("operation", "ApprovePart").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.part.partId).log("Recorded audit detail")
            val outbound0 = batchGetFeatureFlagsRequest {
                this.context = request.context
            }
            val reply0 = featureflagsFeatureFlagService.batchGetFeatureFlags(WireCopies.reflective(outbound0))
            val outbound1 = commentServiceScheduleApprovalRequest {
                this.context = request.context
                this.serviceSchedule = serviceSchedule {
                    this.location = postalAddress {
                        this.postalCode = request.part.address.postalCode
                    }
                    this.participants.addAll(listOf(serviceScheduleParticipant {
                        this.contact = contactInfo {
                            this.email = request.details.approver.email
                        }
                    }))
                }
            }
            val reply1 = workerForApprovePart1.submit(outbound1)
            approvePartResponse {
                this.outcome = ApprovePartResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.part = part {
                    this.partId = request.partId
                    this.specs.putAll(request.part.specsMap)
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = reply1.result.status
                }
            }
        }

    /** Comments a part. */
    override suspend fun commentPartApproval(request: CommentPartApprovalRequest): CommentPartApprovalResponse {
        return handleCommentPartApproval(request)
    }

    /** Delegates a part. */
    override suspend fun delegatePartApproval(request: DelegatePartApprovalRequest): DelegatePartApprovalResponse = handleDelegatePartApproval(request)

    /** Returns a part. */
    override suspend fun getPartApprovalStatus(request: GetPartApprovalStatusRequest): GetPartApprovalStatusResponse =
        RpcBoundary.run("acme.maintenance.v1.PartApprovalService/GetPartApprovalStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetPartApprovalStatus", request)
            log.atInfo().addKeyValue("operation", "GetPartApprovalStatus").log("Handling transport request")
            val outbound0 = calculateDriverLicenseRequest {}.driversCopy {
                this.context = request.context
                this.driverLicense = driverLicense {
                    this.counterparty = driverRef {
                        this.email = request.context.actor.email
                    }
                    this.dueAt = request.context.deadline
                    this.lines.addAll(listOf(driverLicenseLine {
                        this.attributes.putAll(request.context.baggageMap)
                    }))
                }
            }
            val reply0 = driversDriverLicenseCalculationService.calculateDriverLicense(outbound0)
            val outbound1 = request.toCheckAuditEventEligibilityFromGetPartApprovalStatus1(reply0)
            val reply1 = auditAuditEventValidationService.checkAuditEventEligibility(outbound1)
            getPartApprovalStatusResponse {
                this.part = part {
                    this.partId = request.partId
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    /** Lists parts a page at a time. */
    override suspend fun listPendingParts(request: ListPendingPartsRequest): ListPendingPartsResponse {
        return handleListPendingParts(request)
    }

    /** Recalls a part. */
    override suspend fun recallPart(request: RecallPartRequest): RecallPartResponse = handleRecallPart(request)

    /** Rejects a part. */
    override suspend fun rejectPart(request: RejectPartRequest): RejectPartResponse =
        RpcBoundary.run("acme.maintenance.v1.PartApprovalService/RejectPart") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RejectPart", request)
            log.atInfo().addKeyValue("operation", "RejectPart").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.part.code).log("Recorded audit detail")
            val outbound0 = explainDriverCalculationRequest {
                this.context = request.context
                this.driver = driver {
                    this.primaryAddress = postalAddress {
                        this.region = request.part.address.region
                    }
                    this.relationshipManager = userRef {
                        this.displayName = request.part.owner.displayName
                    }
                }
            }
            val reply0 = driversDriverCalculationService.explainDriverCalculation(WireCopies.json(outbound0))
            val outbound1 = RejectPart1Command(
                context = request.context,
                partId = request.part.partId
            ).toRequest()
            val reply1 = maintenancePartReportingService.fetchPartReport(outbound1)
            rejectPartResponse {
                this.outcome = RejectPartResponseKt.outcome {
                    this.message = reply1.result.message
                }
                this.part = part {
                    this.audit = auditInfo {
                        this.etag = reply0.driver.audit.etag
                    }
                    this.capacity = request.part.capacity
                    this.components.putAll(mapOf("primary" to partComponent {
                        this.componentId = (reply1.part.componentsMap["primary"] ?: PartComponent.getDefaultInstance()).componentId
                    }))
                    this.partId = request.partId
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    /** Submits a part. */
    override suspend fun submitPart(request: SubmitPartRequest): SubmitPartResponse {
        return handleSubmitPart(request)
    }

    private suspend fun handleCommentPartApproval(request: CommentPartApprovalRequest): CommentPartApprovalResponse =
        RpcBoundary.run("acme.maintenance.v1.PartApprovalService/CommentPartApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CommentPartApproval", request)
            log.atInfo().addKeyValue("operation", "CommentPartApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.part.partId).log("Recorded audit detail")
            val outbound0 = shareFuelCardExportRequest {
                this.context = request.context
                this.details = ShareFuelCardExportRequestKt.details {
                    this.recipient = contactInfo {
                        this.email = request.details.approver.email
                    }
                }
                this.reason = request.reason
            }
            val reply0 = clientForCommentPartApproval0.shareFuelCardExport(outbound0)
            val outbound1 = scheduleDockExpiryRequest {
                this.context = request.context
                this.dock = dock {
                    this.audit = auditInfo {
                        this.createdAt = reply0.fuelCard.audit.createdAt
                    }
                    this.owner = userRef {
                        this.displayName = request.part.owner.displayName
                        this.email = request.details.approver.email
                    }
                }
            }
            val reply1 = flowOf(outbound1).map { value -> yardsDockLifecycleService.scheduleDockExpiry(value) }.single()
            commentPartApprovalResponse {
                this.outcome = CommentPartApprovalResponseKt.outcome {
                    this.nextSteps.addAll(reply0.outcome.nextStepsList)
                }
                this.part = part {
                    this.audit = auditInfo {
                        this.etag = reply0.fuelCard.audit.etag
                    }
                    this.components.putAll(mapOf("primary" to partComponent {
                        this.description = (reply1.dock.componentsMap["primary"] ?: DockComponent.getDefaultInstance()).description
                    }))
                    this.name = request.part.name
                    this.partId = request.partId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleDelegatePartApproval(request: DelegatePartApprovalRequest): DelegatePartApprovalResponse =
        RpcBoundary.run("acme.maintenance.v1.PartApprovalService/DelegatePartApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DelegatePartApproval", request)
            log.atInfo().addKeyValue("operation", "DelegatePartApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.part.partId).log("Recorded audit detail")
            val outbound0 = listFuelPolicyReportsRequest {
                this.context = request.context
                this.filter = ListFuelPolicyReportsRequestKt.filter {
                    this.region = request.part.address.region
                }
            }
            val reply0 = withTimeout(5_000) { fuelFuelPolicyReportingService.listFuelPolicyReports(outbound0) }
            val configure1: CheckFeatureFlagEligibilityRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.featureFlag = featureFlag {
                    this.audit = auditInfo {
                        this.etag = request.part.audit.etag
                    }
                    this.owner = userRef {
                        this.displayName = request.details.approver.displayName
                        this.userId = request.details.approver.userId
                    }
                }
            }
            val outbound1 = checkFeatureFlagEligibilityRequest(configure1)
            val reply1 = featureflagsFeatureFlagValidationService.checkFeatureFlagEligibility(outbound1)
            val outbound2 = lintDriverLicensesRequest {
                this.context = request.context
                this.driverLicense = driverLicense {
                    this.audit = auditInfo {
                        this.createdAt = reply1.featureFlag.audit.createdAt
                    }
                    this.lines.addAll(listOf(driverLicenseLine {
                        this.tax = driverLicenseLineTax {
                            this.amount = reply1.featureFlag.discount.amount
                        }
                    }))
                }
            }
            val reply2 = driversDriverLicenseValidationService.lintDriverLicenses(outbound2)
            val outbound3 = compareDutyStatusOptionsRequest {
                this.context = request.context
                this.details = CompareDutyStatusOptionsRequestKt.details {
                    this.destination = postalAddress {
                        this.postalCode = request.part.address.postalCode
                    }
                }
                this.dutyStatus = dutyStatus {
                    this.subject = driverLicenseRef {
                        this.displayName = request.part.owner.displayName
                    }
                }
            }
            val reply3 = driversDutyStatusCalculationService.compareDutyStatusOptions(outbound3)
            delegatePartApprovalResponse {
                this.outcome = DelegatePartApprovalResponseKt.outcome {
                    this.message = reply3.result.message
                    this.referenceId = reply1.outcome.referenceId
                }
                this.part = part {
                    this.audit = auditInfo {
                        this.updatedAt = reply1.featureFlag.audit.updatedAt
                    }
                    this.owner = userRef {
                        this.displayName = reply2.driverLicense.counterparty.displayName
                    }
                    this.partId = request.partId
                    this.specs.putAll(request.part.specsMap)
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.referenceIds.addAll((reply0.result.referenceIdsList) + (reply3.result.referenceIdsList))
                    this.status = "ok"
                }
            }
        }

    private fun GetPartApprovalStatusRequest.toCheckAuditEventEligibilityFromGetPartApprovalStatus1(reply0: CalculateDriverLicenseResponse): CheckAuditEventEligibilityRequest {
        val source = this
        return checkAuditEventEligibilityRequest {
            this.auditEvent = auditEvent {
                this.correlationId = reply0.result.correlationId
                this.subject = auditTrailRef {
                    this.displayName = reply0.driverLicense.counterparty.displayName
                }
            }
            this.context = source.context
            this.details = CheckAuditEventEligibilityRequestKt.details {
                this.contact = contactInfo {
                    this.email = reply0.driverLicense.counterparty.email
                }
            }
        }
    }

    private suspend fun handleListPendingParts(request: ListPendingPartsRequest): ListPendingPartsResponse =
        RpcBoundary.run("acme.maintenance.v1.PartApprovalService/ListPendingParts") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListPendingParts", request)
            log.atInfo().addKeyValue("operation", "ListPendingParts").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            listPendingPartsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleRecallPart(request: RecallPartRequest): RecallPartResponse =
        RpcBoundary.run("acme.maintenance.v1.PartApprovalService/RecallPart") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecallPart", request)
            log.atInfo().addKeyValue("operation", "RecallPart").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.part.partId).log("Recorded audit detail")
            val outbound0 = quoteExpenseReportRequest {
                this.context = request.context
                this.details = QuoteExpenseReportRequestKt.details {
                    this.destination = postalAddress {
                        this.recipient = when (request.part.locatorCase) {
                            Part.LocatorCase.ADDRESS -> request.part.address.recipient
                            else -> ""
                        }
                    }
                }
                this.expenseReport = expenseReport {
                    this.audit = auditInfo {
                        this.updatedBy = request.part.audit.updatedBy
                    }
                }
            }
            val reply0 = expensesExpenseReportCalculationService.quoteExpenseReport(outbound0)
            val outbound1 = checkDriverLicenseEligibilityRequest {
                this.context = request.context
                this.driverLicense = driverLicense {
                    this.billingAddress = postalAddress {
                        this.locality = reply0.expenseReport.billingAddress.locality
                    }
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = driversDriverLicenseValidationService.checkDriverLicenseEligibility(envelope1.unpack(CheckDriverLicenseEligibilityRequest::class.java))
            recallPartResponse {
                this.part = part {
                    this.audit = auditInfo {
                        this.createdAt = reply1.driverLicense.audit.createdAt
                        this.updatedAt = reply0.expenseReport.audit.updatedAt
                        this.updatedBy = reply0.expenseReport.audit.updatedBy
                    }
                    this.code = request.part.code
                    this.partId = request.partId
                }
                this.result = operationResult {
                    this.status = reply1.result.status
                }
            }
        }

    private data class RejectPart1Command(
        val context: RequestContext,
        val partId: String
    ) {
        fun toRequest(): FetchPartReportRequest {
            val command = this
            return fetchPartReportRequest {
                this.context = command.context
                this.partId = command.partId
            }
        }
    }

    private suspend fun handleSubmitPart(request: SubmitPartRequest): SubmitPartResponse =
        RpcBoundary.run("acme.maintenance.v1.PartApprovalService/SubmitPart") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SubmitPart", request)
            log.atInfo().addKeyValue("operation", "SubmitPart").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.part.partId).log("Recorded audit detail")
            val outbound0 = validateWorkOrderRequest {
                this.context = request.context
                this.details = ValidateWorkOrderRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.part.owner.email
                    }
                }
                this.workOrder = workOrder {
                    this.requester = contactInfo {
                        this.email = request.details.approver.email
                    }
                }
            }
            val command0: Submission<ValidateWorkOrderRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = maintenanceWorkOrderValidationService.validateWorkOrder(payload0)
            val outbound1 = normalizeAuditEventRequest {
                this.context = request.context
                this.details = NormalizeAuditEventRequestKt.details {
                    this.contact = contactInfo {
                        this.email = reply0.workOrder.requester.email
                    }
                }
            }
            val adapter1 = OutboundPort<NormalizeAuditEventRequest, NormalizeAuditEventResponse> { payload ->
                auditAuditEventValidationService.normalizeAuditEvent(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            val outbound2 = cloneMetricSeriesRequest {
                this.context = request.context
                this.metricSeries = metricSeries {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.workOrder.audit.updatedAt
                    }
                    this.owner = userRef {
                        this.displayName = reply1.auditEvent.subject.displayName
                        this.email = reply0.workOrder.customer.email
                    }
                }
            }
            val reply2 = invokeSubmitPart2(outbound2)
            val outbound3 = watchAuditEventsRequest {
                this.context = request.context
                this.filter = WatchAuditEventsRequestKt.filter {
                    this.region = reply2.metricSeries.address.region
                }
                this.subscriber = contactInfo {
                    this.email = reply2.metricSeries.owner.email
                    this.phone = reply0.workOrder.requester.phone
                    this.preferredChannel = reply0.workOrder.requester.preferredChannel
                }
            }
            auditAuditEventService.watchAuditEvents(outbound3).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            submitPartResponse {
                this.outcome = SubmitPartResponseKt.outcome {
                    this.effectiveAt = reply1.outcome.effectiveAt
                    this.status = reply0.result.status
                }
                this.part = part {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.workOrder.audit.updatedBy
                    }
                    this.components.putAll(mapOf("primary" to partComponent {
                        this.quantity = (reply2.metricSeries.componentsMap["primary"] ?: MetricSeriesComponent.getDefaultInstance()).quantity
                    }))
                    this.partId = request.partId
                    this.specs.putAll(request.part.specsMap)
                }
                this.result = operationResult {
                    this.status = reply1.result.status
                }
            }
        }
}
