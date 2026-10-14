package com.acme.transport.telematics.grpc

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.CompareCustomerSegmentOptionsRequest
import com.acme.customers.v1.CompareCustomerSegmentOptionsRequestKt
import com.acme.customers.v1.CustomerSegmentCalculationServiceGrpcKt
import com.acme.customers.v1.HouseholdQueryServiceGrpcKt
import com.acme.customers.v1.SummarizeHouseholdsRequestKt
import com.acme.customers.v1.compareCustomerSegmentOptionsRequest
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.customerSegment
import com.acme.customers.v1.summarizeHouseholdsRequest
import com.acme.featureflags.v1.FeatureFlagValidationServiceGrpcKt
import com.acme.featureflags.v1.ValidateFeatureFlagRequest
import com.acme.featureflags.v1.ValidateFeatureFlagResponse
import com.acme.featureflags.v1.featureFlag
import com.acme.featureflags.v1.validateFeatureFlagRequest
import com.acme.fuel.v1.FuelCardExportServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionExportServiceGrpcKt
import com.acme.fuel.v1.ShareFuelTransactionExportRequest
import com.acme.fuel.v1.ShareFuelTransactionExportRequestKt
import com.acme.fuel.v1.ShareFuelTransactionExportResponse
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.fuelCardBankFunding
import com.acme.fuel.v1.retryFuelCardExportRequest
import com.acme.fuel.v1.shareFuelTransactionExportRequest
import com.acme.identity.v1.UserValidationServiceGrpcKt
import com.acme.identity.v1.getUserRulesRequest
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.FindLedgerAccountsRequest
import com.acme.ledger.v1.FindLedgerAccountsRequestKt
import com.acme.ledger.v1.FindLedgerAccountsResponse
import com.acme.ledger.v1.LedgerAccountQueryServiceGrpcKt
import com.acme.ledger.v1.findLedgerAccountsRequest
import com.acme.maintenance.v1.ApproveWorkOrderRequest
import com.acme.maintenance.v1.ApproveWorkOrderRequestKt
import com.acme.maintenance.v1.WorkOrderApprovalServiceGrpcKt
import com.acme.maintenance.v1.approveWorkOrderRequest
import com.acme.maintenance.v1.workOrder
import com.acme.telematics.v1.AdvanceGeoFenceWorkflowResponse
import com.acme.telematics.v1.AdvanceTripLogWorkflowRequest
import com.acme.telematics.v1.AdvanceTripLogWorkflowResponse
import com.acme.telematics.v1.ApproveTelemetryReadingRequest
import com.acme.telematics.v1.ApproveTelemetryReadingRequestKt
import com.acme.telematics.v1.ApproveTelemetryReadingResponse
import com.acme.telematics.v1.CancelTripLogWorkflowRequest
import com.acme.telematics.v1.CancelTripLogWorkflowResponse
import com.acme.telematics.v1.GeoFenceApprovalServiceGrpcKt
import com.acme.telematics.v1.GeoFenceWorkflowServiceGrpcKt
import com.acme.telematics.v1.InspectTripLogWorkflowRequest
import com.acme.telematics.v1.InspectTripLogWorkflowResponse
import com.acme.telematics.v1.ListTripLogWorkflowsRequest
import com.acme.telematics.v1.ListTripLogWorkflowsResponse
import com.acme.telematics.v1.PauseTripLogWorkflowRequest
import com.acme.telematics.v1.PauseTripLogWorkflowResponse
import com.acme.telematics.v1.PauseTripLogWorkflowResponseKt
import com.acme.telematics.v1.ReinstateTripLogRequest
import com.acme.telematics.v1.ReinstateTripLogRequestKt
import com.acme.telematics.v1.RejectTripLogResponse
import com.acme.telematics.v1.ResumeTripLogWorkflowRequest
import com.acme.telematics.v1.ResumeTripLogWorkflowResponse
import com.acme.telematics.v1.RetryTripLogWorkflowRequest
import com.acme.telematics.v1.RetryTripLogWorkflowResponse
import com.acme.telematics.v1.RetryTripLogWorkflowResponseKt
import com.acme.telematics.v1.StartTripLogWorkflowRequest
import com.acme.telematics.v1.StartTripLogWorkflowResponse
import com.acme.telematics.v1.StartTripLogWorkflowResponseKt
import com.acme.telematics.v1.TelemetryReadingApprovalServiceGrpcKt
import com.acme.telematics.v1.TripLog
import com.acme.telematics.v1.TripLogApprovalServiceGrpcKt
import com.acme.telematics.v1.TripLogAttachment
import com.acme.telematics.v1.TripLogLifecycleServiceGrpcKt
import com.acme.telematics.v1.TripLogWorkflowServiceGrpcKt
import com.acme.telematics.v1.advanceGeoFenceWorkflowRequest
import com.acme.telematics.v1.advanceTripLogWorkflowResponse
import com.acme.telematics.v1.approveTelemetryReadingRequest
import com.acme.telematics.v1.cancelTripLogWorkflowResponse
import com.acme.telematics.v1.commentGeoFenceApprovalRequest
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.geoFenceDiscountAction
import com.acme.telematics.v1.geoFenceRef
import com.acme.telematics.v1.inspectTripLogWorkflowResponse
import com.acme.telematics.v1.listTripLogWorkflowsResponse
import com.acme.telematics.v1.pauseTripLogWorkflowResponse
import com.acme.telematics.v1.reinstateTripLogRequest
import com.acme.telematics.v1.rejectTripLogRequest
import com.acme.telematics.v1.resumeTripLogWorkflowResponse
import com.acme.telematics.v1.retryTripLogWorkflowResponse
import com.acme.telematics.v1.startTripLogWorkflowResponse
import com.acme.telematics.v1.telemetryReading
import com.acme.telematics.v1.tripLog
import com.acme.telematics.v1.tripLogAttachment
import com.acme.telematics.v1.tripLogLine
import com.acme.telematics.v1.tripLogLineTax
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.transport.telematics.TelematicsRepository
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Runs the long-lived workflow that takes a trip log from start to finish. */
class TripLogWorkflowEndpoint(
    private val store: TelematicsRepository,
    private val workers: CoroutineScope,
    private val customersCustomerSegmentCalculationService: CustomerSegmentCalculationServiceGrpcKt.CustomerSegmentCalculationServiceCoroutineStub,
    private val customersHouseholdQueryService: HouseholdQueryServiceGrpcKt.HouseholdQueryServiceCoroutineStub,
    private val featureflagsFeatureFlagValidationService: FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub,
    private val fuelFuelCardExportService: FuelCardExportServiceGrpcKt.FuelCardExportServiceCoroutineStub,
    private val fuelFuelTransactionExportService: FuelTransactionExportServiceGrpcKt.FuelTransactionExportServiceCoroutineStub,
    private val identityUserValidationService: UserValidationServiceGrpcKt.UserValidationServiceCoroutineStub,
    private val ledgerLedgerAccountQueryService: LedgerAccountQueryServiceGrpcKt.LedgerAccountQueryServiceCoroutineStub,
    private val maintenanceWorkOrderApprovalService: WorkOrderApprovalServiceGrpcKt.WorkOrderApprovalServiceCoroutineStub,
    private val telematicsGeoFenceApprovalService: GeoFenceApprovalServiceGrpcKt.GeoFenceApprovalServiceCoroutineStub,
    private val telematicsGeoFenceWorkflowService: GeoFenceWorkflowServiceGrpcKt.GeoFenceWorkflowServiceCoroutineStub,
    private val telematicsTelemetryReadingApprovalService: TelemetryReadingApprovalServiceGrpcKt.TelemetryReadingApprovalServiceCoroutineStub,
    private val telematicsTripLogApprovalService: TripLogApprovalServiceGrpcKt.TripLogApprovalServiceCoroutineStub,
    private val telematicsTripLogLifecycleService: TripLogLifecycleServiceGrpcKt.TripLogLifecycleServiceCoroutineStub,
) : TripLogWorkflowServiceGrpcKt.TripLogWorkflowServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(TripLogWorkflowEndpoint::class.java)

    private val invokePauseTripLogWorkflow0: suspend (ValidateFeatureFlagRequest) -> ValidateFeatureFlagResponse = { payload ->
        featureflagsFeatureFlagValidationService.validateFeatureFlag(payload)
    }

    private val workerForRetryTripLogWorkflow3 = ReplyMailbox<FindLedgerAccountsRequest, FindLedgerAccountsResponse>(workers) { payload ->
        withTimeout(5_000) { ledgerLedgerAccountQueryService.findLedgerAccounts(payload) }
    }

    private val clientForStartTripLogWorkflow0 by lazy { customersHouseholdQueryService }

    /** Advances a trip log. */
    override suspend fun advanceTripLogWorkflow(request: AdvanceTripLogWorkflowRequest): AdvanceTripLogWorkflowResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogWorkflowService/AdvanceTripLogWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "AdvanceTripLogWorkflow", request)
            log.atInfo().addKeyValue("operation", "AdvanceTripLogWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLog.tripLogId).log("Recorded audit detail")
            val outbound0 = retryFuelCardExportRequest {
                this.context = request.context
                this.fuelCard = fuelCard {
                    this.bank = fuelCardBankFunding {
                        this.bic = when (request.tripLog.settlementCase) {
                            TripLog.SettlementCase.BANK -> request.tripLog.bank.bic
                            else -> ""
                        }
                    }
                }
            }
            val reply0 = fuelFuelCardExportService.retryFuelCardExport(outbound0)
            advanceTripLogWorkflowResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.etag = reply0.fuelCard.audit.etag
                    }
                    this.status = request.tripLog.status
                    this.tripLogId = request.tripLogId
                }
            }
        }

    /** Cancels a trip log. */
    override suspend fun cancelTripLogWorkflow(request: CancelTripLogWorkflowRequest): CancelTripLogWorkflowResponse {
        return handleCancelTripLogWorkflow(request)
    }

    /** Returns a trip log. */
    override suspend fun inspectTripLogWorkflow(request: InspectTripLogWorkflowRequest): InspectTripLogWorkflowResponse = handleInspectTripLogWorkflow(request)

    /** Lists trip logs a page at a time. */
    override suspend fun listTripLogWorkflows(request: ListTripLogWorkflowsRequest): ListTripLogWorkflowsResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogWorkflowService/ListTripLogWorkflows") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListTripLogWorkflows", request)
            log.atInfo().addKeyValue("operation", "ListTripLogWorkflows").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = compareCustomerSegmentOptionsRequest {
                this.context = request.context
                this.details = CompareCustomerSegmentOptionsRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.filter.region
                    }
                }
            }
            val command0: Submission<CompareCustomerSegmentOptionsRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = customersCustomerSegmentCalculationService.compareCustomerSegmentOptions(payload0)
            val outbound1 = shareFuelTransactionExportRequest {
                this.annotations.putAll(request.filter.labelsMap)
                this.context = request.context
                this.details = ShareFuelTransactionExportRequestKt.details {
                    this.recipient = contactInfo {
                        this.email = reply0.customerSegment.owner.email
                    }
                    this.variables.putAll(reply0.customerSegment.parametersMap)
                }
                this.reason = reply0.customerSegment.block.reason
            }
            val adapter1 = OutboundPort<ShareFuelTransactionExportRequest, ShareFuelTransactionExportResponse> { payload ->
                fuelFuelTransactionExportService.shareFuelTransactionExport(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            listTripLogWorkflowsResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
                this.tripLogs.addAll(listOf(tripLog {
                    this.audit = auditInfo {
                        this.createdBy = reply0.customerSegment.audit.createdBy
                    }
                    this.lines.addAll(listOf(tripLogLine {
                        this.attributes.putAll(reply1.fuelTransaction.attributesMap)
                    }))
                }))
            }
        }

    /** Pauses a trip log. */
    override suspend fun pauseTripLogWorkflow(request: PauseTripLogWorkflowRequest): PauseTripLogWorkflowResponse {
        return handlePauseTripLogWorkflow(request)
    }

    /** Resumes a trip log. */
    override suspend fun resumeTripLogWorkflow(request: ResumeTripLogWorkflowRequest): ResumeTripLogWorkflowResponse = handleResumeTripLogWorkflow(request)

    /** Retrys a trip log. */
    override suspend fun retryTripLogWorkflow(request: RetryTripLogWorkflowRequest): RetryTripLogWorkflowResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogWorkflowService/RetryTripLogWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RetryTripLogWorkflow", request)
            log.atInfo().addKeyValue("operation", "RetryTripLogWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLog.tripLogId).log("Recorded audit detail")
            var reply0 = RejectTripLogResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<TripLogAttachment>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound0 = rejectTripLogRequest {
                            this.context = request.context
                            this.tripLog = tripLog {
                                this.attachments.addAll(listOf(tripLogAttachment {
                                    this.uri = item.uri
                                }))
                            }
                        }
                        reply0 = telematicsTripLogApprovalService.rejectTripLog(outbound0)
                    }
                }
                try {
                    request.tripLog.attachmentsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound1 = compareCustomerSegmentOptionsRequest {
                this.context = request.context
                this.customerSegment = customerSegment {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.tripLog.audit.updatedBy
                    }
                }
                this.details = CompareCustomerSegmentOptionsRequestKt.details {
                    this.destination = postalAddress {
                        this.postalCode = request.tripLog.billingAddress.postalCode
                    }
                }
            }
            val reply1 = ClientRegistry.lookup("acme.customers.v1.CustomerSegmentCalculationService", CustomerSegmentCalculationServiceGrpcKt.CustomerSegmentCalculationServiceCoroutineStub::class.java).compareCustomerSegmentOptions(outbound1)
            val outbound2 = getUserRulesRequest {
                this.context = request.context
                this.userId = request.context.actor.userId
            }
            val reply2 = identityUserValidationService.getUserRules(WireCopies.reflective(outbound2))
            val outbound3 = findLedgerAccountsRequest {
                this.context = request.context
                this.filter = FindLedgerAccountsRequestKt.filter {
                    this.createdAfter = reply1.customerSegment.audit.createdAt
                    this.region = reply0.tripLog.billingAddress.region
                }
            }
            val reply3 = workerForRetryTripLogWorkflow3.submit(outbound3)
            retryTripLogWorkflowResponse {
                this.outcome = RetryTripLogWorkflowResponseKt.outcome {
                    this.status = reply3.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.billingAddress = postalAddress {
                        this.locality = reply2.user.primaryAddress.locality
                    }
                    this.counterparty = customerRef {
                        this.displayName = reply2.user.relationshipManager.displayName
                    }
                    this.lines.addAll(listOf(tripLogLine {
                        this.tax = tripLogLineTax {
                            this.amount = reply1.customerSegment.discount.amount
                        }
                    }))
                    this.status = request.tripLog.status
                    this.tripLogId = request.tripLogId
                }
            }
        }

    /** Starts a trip log. */
    override suspend fun startTripLogWorkflow(request: StartTripLogWorkflowRequest): StartTripLogWorkflowResponse {
        return handleStartTripLogWorkflow(request)
    }

    private suspend fun handleCancelTripLogWorkflow(request: CancelTripLogWorkflowRequest): CancelTripLogWorkflowResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogWorkflowService/CancelTripLogWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CancelTripLogWorkflow", request)
            log.atInfo().addKeyValue("operation", "CancelTripLogWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLog.tripLogId).log("Recorded audit detail")
            var reply0 = AdvanceGeoFenceWorkflowResponse.getDefaultInstance()
            request.tripLog.linesList.asFlow().map { item ->
                val outbound0 = advanceGeoFenceWorkflowRequest {
                    this.context = request.context
                    this.geoFence = geoFence {
                        this.audit = auditInfo {
                            this.updatedAt = request.tripLog.audit.updatedAt
                        }
                        this.description = item.description
                        this.discount = geoFenceDiscountAction {
                            this.amount = item.amount
                        }
                    }
                }
                telematicsGeoFenceWorkflowService.advanceGeoFenceWorkflow(outbound0)
            }.collect { reply0 = it }
            cancelTripLogWorkflowResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.metadata.putAll(request.tripLog.metadataMap)
                    this.tripLogId = request.tripLogId
                }
            }
        }

    private data class InspectTripLogWorkflow0Command(
        val context: RequestContext,
        val detailsApproverEmail: String,
        val workOrderAssigneeUserId: String
    ) {
        fun toRequest(): ApproveWorkOrderRequest {
            val command = this
            return approveWorkOrderRequest {
                this.context = command.context
                this.details = ApproveWorkOrderRequestKt.details {
                    this.approver = userRef {
                        this.email = command.detailsApproverEmail
                    }
                }
                this.workOrder = workOrder {
                    this.assignee = userRef {
                        this.userId = command.workOrderAssigneeUserId
                    }
                }
            }
        }
    }

    private suspend fun handleInspectTripLogWorkflow(request: InspectTripLogWorkflowRequest): InspectTripLogWorkflowResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogWorkflowService/InspectTripLogWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "InspectTripLogWorkflow", request)
            log.atInfo().addKeyValue("operation", "InspectTripLogWorkflow").log("Handling transport request")
            val outbound0 = InspectTripLogWorkflow0Command(
                context = request.context,
                detailsApproverEmail = request.context.actor.email,
                workOrderAssigneeUserId = request.context.actor.userId
            ).toRequest()
            val reply0 = maintenanceWorkOrderApprovalService.approveWorkOrder(outbound0)
            inspectTripLogWorkflowResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.workOrder.audit.updatedBy
                    }
                    this.tripLogId = request.tripLogId
                }
            }
        }

    private suspend fun handlePauseTripLogWorkflow(request: PauseTripLogWorkflowRequest): PauseTripLogWorkflowResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogWorkflowService/PauseTripLogWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PauseTripLogWorkflow", request)
            log.atInfo().addKeyValue("operation", "PauseTripLogWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLog.tripLogId).log("Recorded audit detail")
            val outbound0 = validateFeatureFlagRequest {
                this.context = request.context
                this.featureFlag = featureFlag {
                    this.audit = auditInfo {
                        this.updatedBy = request.tripLog.audit.updatedBy
                    }
                    this.owner = userRef {
                        this.email = request.details.notify.email
                    }
                }
            }
            val reply0 = invokePauseTripLogWorkflow0(outbound0)
            val outbound1 = approveTelemetryReadingRequest {
                this.context = request.context
                this.details = ApproveTelemetryReadingRequestKt.details {
                    this.approver = userRef {
                        this.displayName = request.tripLog.counterparty.displayName
                        this.email = request.details.notify.email
                        this.userId = reply0.featureFlag.owner.userId
                    }
                }
                this.telemetryReading = telemetryReading {
                    this.subject = geoFenceRef {
                        this.displayName = reply0.featureFlag.owner.displayName
                    }
                }
            }
            val invoke1: suspend (ApproveTelemetryReadingRequest, Metadata) -> ApproveTelemetryReadingResponse = telematicsTelemetryReadingApprovalService::approveTelemetryReading
            val reply1 = invoke1(outbound1, Metadata())
            pauseTripLogWorkflowResponse {
                this.outcome = PauseTripLogWorkflowResponseKt.outcome {
                    this.referenceId = reply1.outcome.referenceId
                }
                this.result = operationResult {
                    this.status = reply1.outcome.status
                }
                this.tripLog = tripLog {
                    this.dueAt = request.tripLog.dueAt
                    this.lines.addAll(listOf(tripLogLine {
                        this.tax = tripLogLineTax {
                            this.amount = reply0.featureFlag.discount.amount
                        }
                    }))
                    this.tripLogId = request.tripLogId
                }
            }
        }

    private suspend fun handleResumeTripLogWorkflow(request: ResumeTripLogWorkflowRequest): ResumeTripLogWorkflowResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogWorkflowService/ResumeTripLogWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResumeTripLogWorkflow", request)
            log.atInfo().addKeyValue("operation", "ResumeTripLogWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLog.number).log("Recorded audit detail")
            val outbound0 = ReinstateTripLogRequest.newBuilder().apply {
                setContext(request.context)
                setDetails(ReinstateTripLogRequestKt.details {
                    this.notifyContact = contactInfo {
                        this.preferredChannel = request.details.notify.preferredChannel
                    }
                })
                setTripLogId(request.tripLog.tripLogId)
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> telematicsTripLogLifecycleService.reinstateTripLog(value) }
            resumeTripLogWorkflowResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.counterparty = request.tripLog.counterparty
                    this.notes = reply0.tripLog.notes
                    this.number = reply0.tripLog.number
                    this.tripLogId = request.tripLogId
                }
            }
        }

    private suspend fun handleStartTripLogWorkflow(request: StartTripLogWorkflowRequest): StartTripLogWorkflowResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogWorkflowService/StartTripLogWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StartTripLogWorkflow", request)
            log.atInfo().addKeyValue("operation", "StartTripLogWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLog.tripLogId).log("Recorded audit detail")
            val outbound0 = summarizeHouseholdsRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.details = SummarizeHouseholdsRequestKt.details {
                    this.filters.putAll(request.details.parametersMap)
                }
                this.reason = request.reason
            }
            val reply0 = clientForStartTripLogWorkflow0.summarizeHouseholds(outbound0)
            val outbound1 = commentGeoFenceApprovalRequest {
                this.context = request.context
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.etag = request.tripLog.audit.etag
                    }
                }
            }
            val reply1 = flowOf(outbound1).map { value -> telematicsGeoFenceApprovalService.commentGeoFenceApproval(value) }.single()
            startTripLogWorkflowResponse {
                this.outcome = StartTripLogWorkflowResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.updatedBy = reply1.geoFence.audit.updatedBy
                    }
                    this.dueAt = request.tripLog.dueAt
                    this.tripLogId = request.tripLogId
                }
            }
        }
}
