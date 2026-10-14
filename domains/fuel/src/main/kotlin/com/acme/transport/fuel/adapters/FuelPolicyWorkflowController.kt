package com.acme.transport.fuel.adapters

import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.deliveries.v1.DeliveryApprovalServiceGrpcKt
import com.acme.deliveries.v1.DeliveryExceptionApprovalServiceGrpcKt
import com.acme.deliveries.v1.EscalateProofOfDeliveryRequestKt
import com.acme.deliveries.v1.ProofOfDeliveryApprovalServiceGrpcKt
import com.acme.deliveries.v1.SubmitDeliveryRequest
import com.acme.deliveries.v1.SubmitDeliveryResponse
import com.acme.deliveries.v1.delivery
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.deliveryLine
import com.acme.deliveries.v1.deliveryRef
import com.acme.deliveries.v1.escalateProofOfDeliveryRequest
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.proofOfDeliveryBankSettlement
import com.acme.deliveries.v1.rejectDeliveryExceptionRequest
import com.acme.deliveries.v1.submitDeliveryRequest
import com.acme.drivers.v1.BrowseDriverLicensesRequestKt
import com.acme.drivers.v1.DriverLicenseQueryServiceGrpcKt
import com.acme.drivers.v1.SuggestDriverLicensesRequest
import com.acme.drivers.v1.SuggestDriverLicensesRequestKt
import com.acme.drivers.v1.browseDriverLicensesRequest
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.suggestDriverLicensesRequest
import com.acme.fuel.v1.AdvanceFuelPolicyWorkflowRequest
import com.acme.fuel.v1.AdvanceFuelPolicyWorkflowResponse
import com.acme.fuel.v1.AdvanceFuelPolicyWorkflowResponseKt
import com.acme.fuel.v1.CancelFuelPolicyWorkflowRequest
import com.acme.fuel.v1.CancelFuelPolicyWorkflowResponse
import com.acme.fuel.v1.CancelFuelPolicyWorkflowResponseKt
import com.acme.fuel.v1.FuelCard
import com.acme.fuel.v1.FuelCardExportServiceGrpcKt
import com.acme.fuel.v1.FuelCardLifecycleServiceGrpcKt
import com.acme.fuel.v1.FuelCardReportingServiceGrpcKt
import com.acme.fuel.v1.FuelCardSyncServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyApprovalServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyLifecycleServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyWorkflowServiceGrpcKt
import com.acme.fuel.v1.InspectFuelPolicyWorkflowRequest
import com.acme.fuel.v1.InspectFuelPolicyWorkflowResponse
import com.acme.fuel.v1.ListFuelCardReportsRequestKt
import com.acme.fuel.v1.RecallFuelPolicyRequestKt
import com.acme.fuel.v1.ResolveFuelCardConflictRequest
import com.acme.fuel.v1.RetryFuelPolicyWorkflowRequest
import com.acme.fuel.v1.RetryFuelPolicyWorkflowResponse
import com.acme.fuel.v1.RetryFuelPolicyWorkflowResponseKt
import com.acme.fuel.v1.ScheduleFuelCardExportRequest
import com.acme.fuel.v1.ScheduleFuelCardExportRequestKt
import com.acme.fuel.v1.ScheduleFuelCardExportResponse
import com.acme.fuel.v1.ShareFuelCardExportRequestKt
import com.acme.fuel.v1.SignalFuelPolicyWorkflowRequest
import com.acme.fuel.v1.SignalFuelPolicyWorkflowResponse
import com.acme.fuel.v1.SignalFuelPolicyWorkflowResponseKt
import com.acme.fuel.v1.StartFuelPolicyWorkflowRequest
import com.acme.fuel.v1.StartFuelPolicyWorkflowResponse
import com.acme.fuel.v1.StartFuelPolicyWorkflowResponseKt
import com.acme.fuel.v1.SuspendFuelPolicyRequest
import com.acme.fuel.v1.SuspendFuelPolicyRequestKt
import com.acme.fuel.v1.SuspendFuelPolicyResponse
import com.acme.fuel.v1.advanceFuelPolicyWorkflowResponse
import com.acme.fuel.v1.cancelFuelPolicyWorkflowResponse
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.inspectFuelPolicyWorkflowResponse
import com.acme.fuel.v1.listFuelCardReportsRequest
import com.acme.fuel.v1.recallFuelPolicyRequest
import com.acme.fuel.v1.resolveFuelCardConflictRequest
import com.acme.fuel.v1.retryFuelPolicyWorkflowResponse
import com.acme.fuel.v1.scheduleFuelCardExpiryRequest
import com.acme.fuel.v1.scheduleFuelCardExportRequest
import com.acme.fuel.v1.scheduleFuelCardReportRequest
import com.acme.fuel.v1.shareFuelCardExportRequest
import com.acme.fuel.v1.signalFuelPolicyWorkflowResponse
import com.acme.fuel.v1.startFuelPolicyWorkflowResponse
import com.acme.fuel.v1.suspendFuelPolicyRequest
import com.acme.identity.v1.UserQueryServiceGrpcKt
import com.acme.identity.v1.resolveUsersRequest
import com.acme.identity.v1.userRef
import com.acme.personalization.v1.EstimateProfileResponse
import com.acme.personalization.v1.ProfileCalculationServiceGrpcKt
import com.acme.personalization.v1.copy as personalizationCopy
import com.acme.personalization.v1.estimateProfileRequest
import com.acme.personalization.v1.profile
import com.acme.transport.fuel.FuelRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.RecallYardMoveRequestKt
import com.acme.yards.v1.YardMoveApprovalServiceGrpcKt
import com.acme.yards.v1.recallYardMoveRequest
import com.acme.yards.v1.yardMove
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Runs the long-lived workflow that takes a fuel policy from start to finish. */
class FuelPolicyWorkflowController(
    private val store: FuelRepository,
    private val workers: CoroutineScope,
    private val deliveriesDeliveryApprovalService: DeliveryApprovalServiceGrpcKt.DeliveryApprovalServiceCoroutineStub,
    private val deliveriesDeliveryExceptionApprovalService: DeliveryExceptionApprovalServiceGrpcKt.DeliveryExceptionApprovalServiceCoroutineStub,
    private val deliveriesProofOfDeliveryApprovalService: ProofOfDeliveryApprovalServiceGrpcKt.ProofOfDeliveryApprovalServiceCoroutineStub,
    private val driversDriverLicenseQueryService: DriverLicenseQueryServiceGrpcKt.DriverLicenseQueryServiceCoroutineStub,
    private val fuelFuelCardExportService: FuelCardExportServiceGrpcKt.FuelCardExportServiceCoroutineStub,
    private val fuelFuelCardLifecycleService: FuelCardLifecycleServiceGrpcKt.FuelCardLifecycleServiceCoroutineStub,
    private val fuelFuelCardReportingService: FuelCardReportingServiceGrpcKt.FuelCardReportingServiceCoroutineStub,
    private val fuelFuelCardSyncService: FuelCardSyncServiceGrpcKt.FuelCardSyncServiceCoroutineStub,
    private val fuelFuelPolicyApprovalService: FuelPolicyApprovalServiceGrpcKt.FuelPolicyApprovalServiceCoroutineStub,
    private val fuelFuelPolicyLifecycleService: FuelPolicyLifecycleServiceGrpcKt.FuelPolicyLifecycleServiceCoroutineStub,
    private val identityUserQueryService: UserQueryServiceGrpcKt.UserQueryServiceCoroutineStub,
    private val personalizationProfileCalculationService: ProfileCalculationServiceGrpcKt.ProfileCalculationServiceCoroutineStub,
    private val yardsYardMoveApprovalService: YardMoveApprovalServiceGrpcKt.YardMoveApprovalServiceCoroutineStub,
) : FuelPolicyWorkflowServiceGrpcKt.FuelPolicyWorkflowServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(FuelPolicyWorkflowController::class.java)

    private val invokeAdvanceFuelPolicyWorkflow0: suspend (SubmitDeliveryRequest) -> SubmitDeliveryResponse = { payload ->
        deliveriesDeliveryApprovalService.submitDelivery(payload)
    }

    private val workerForCancelFuelPolicyWorkflow2 = ReplyMailbox<SuspendFuelPolicyRequest, SuspendFuelPolicyResponse>(workers) { payload ->
        withTimeout(5_000) { fuelFuelPolicyLifecycleService.suspendFuelPolicy(payload) }
    }

    private val clientForInspectFuelPolicyWorkflow0 by lazy { fuelFuelCardReportingService }

    /** Advances a fuel policy. */
    override suspend fun advanceFuelPolicyWorkflow(request: AdvanceFuelPolicyWorkflowRequest): AdvanceFuelPolicyWorkflowResponse = handleAdvanceFuelPolicyWorkflow(request)

    /** Cancels a fuel policy. */
    override suspend fun cancelFuelPolicyWorkflow(request: CancelFuelPolicyWorkflowRequest): CancelFuelPolicyWorkflowResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyWorkflowService/CancelFuelPolicyWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CancelFuelPolicyWorkflow", request)
            log.atInfo().addKeyValue("operation", "CancelFuelPolicyWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicy.name).log("Recorded audit detail")
            val outbound0 = scheduleFuelCardExpiryRequest {
                this.context = request.context
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.etag = request.fuelPolicy.audit.etag
                    }
                    this.contact = contactInfo {
                        this.email = request.details.notify.email
                    }
                }
            }
            val reply0 = ClientRegistry.lookup("acme.fuel.v1.FuelCardLifecycleService", FuelCardLifecycleServiceGrpcKt.FuelCardLifecycleServiceCoroutineStub::class.java).scheduleFuelCardExpiry(outbound0)
            val outbound1 = shareFuelCardExportRequest {
                this.context = request.context
                this.details = ShareFuelCardExportRequestKt.details {
                    this.recipient = contactInfo {
                        this.email = request.details.notify.email
                        this.phone = request.details.notify.phone
                        this.preferredChannel = reply0.fuelCard.contact.preferredChannel
                    }
                }
            }
            val reply1 = fuelFuelCardExportService.shareFuelCardExport(WireCopies.reflective(outbound1))
            val outbound2 = suspendFuelPolicyRequest {
                this.context = request.context
                this.details = SuspendFuelPolicyRequestKt.details {
                    this.newOwner = userRef {
                        this.displayName = request.fuelPolicy.owner.displayName
                    }
                    this.notifyContact = contactInfo {
                        this.email = reply1.fuelCard.holder.email
                        this.phone = reply0.fuelCard.contact.phone
                    }
                }
                this.fuelPolicyId = request.fuelPolicy.fuelPolicyId
            }
            val reply2 = workerForCancelFuelPolicyWorkflow2.submit(outbound2)
            cancelFuelPolicyWorkflowResponse {
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.etag = reply2.fuelPolicy.audit.etag
                        this.updatedAt = reply0.fuelCard.audit.updatedAt
                    }
                    this.fuelPolicyId = request.fuelPolicyId
                    this.owner = userRef {
                        this.email = reply1.fuelCard.contact.email
                    }
                    this.priority = request.fuelPolicy.priority
                }
                this.outcome = CancelFuelPolicyWorkflowResponseKt.outcome {
                    this.message = reply2.outcome.message
                    this.nextSteps.addAll(reply0.outcome.nextStepsList)
                    this.referenceId = reply1.outcome.referenceId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Returns a fuel policy. */
    override suspend fun inspectFuelPolicyWorkflow(request: InspectFuelPolicyWorkflowRequest): InspectFuelPolicyWorkflowResponse {
        return handleInspectFuelPolicyWorkflow(request)
    }

    /** Retrys a fuel policy. */
    override suspend fun retryFuelPolicyWorkflow(request: RetryFuelPolicyWorkflowRequest): RetryFuelPolicyWorkflowResponse = handleRetryFuelPolicyWorkflow(request)

    /** Signals a fuel policy. */
    override suspend fun signalFuelPolicyWorkflow(request: SignalFuelPolicyWorkflowRequest): SignalFuelPolicyWorkflowResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyWorkflowService/SignalFuelPolicyWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SignalFuelPolicyWorkflow", request)
            log.atInfo().addKeyValue("operation", "SignalFuelPolicyWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicy.fuelPolicyId).log("Recorded audit detail")
            val outbound0 = scheduleFuelCardReportRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.reason = request.fuelPolicy.block.reason
            }
            val reply0 = fuelFuelCardReportingService.scheduleFuelCardReport(outbound0)
            signalFuelPolicyWorkflowResponse {
                this.fuelPolicy = fuelPolicy {
                    this.fuelPolicyId = request.fuelPolicyId
                    this.priority = request.fuelPolicy.priority
                }
                this.outcome = SignalFuelPolicyWorkflowResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Starts a fuel policy. */
    override suspend fun startFuelPolicyWorkflow(request: StartFuelPolicyWorkflowRequest): StartFuelPolicyWorkflowResponse {
        return handleStartFuelPolicyWorkflow(request)
    }

    private suspend fun handleAdvanceFuelPolicyWorkflow(request: AdvanceFuelPolicyWorkflowRequest): AdvanceFuelPolicyWorkflowResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyWorkflowService/AdvanceFuelPolicyWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "AdvanceFuelPolicyWorkflow", request)
            log.atInfo().addKeyValue("operation", "AdvanceFuelPolicyWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicy.fuelPolicyId).log("Recorded audit detail")
            val outbound0 = submitDeliveryRequest {
                this.context = request.context
                this.delivery = delivery {
                    this.audit = auditInfo {
                        this.createdAt = request.fuelPolicy.audit.createdAt
                    }
                    this.lines.addAll(listOf(deliveryLine {
                        this.amount = request.fuelPolicy.discount.amount
                    }))
                }
            }
            val reply0 = invokeAdvanceFuelPolicyWorkflow0(outbound0)
            val outbound1 = scheduleFuelCardExportRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.details = ScheduleFuelCardExportRequestKt.details {
                    this.filters.putAll(request.fuelPolicy.parametersMap)
                }
                this.reason = request.fuelPolicy.block.reason
            }
            val invoke1: suspend (ScheduleFuelCardExportRequest, Metadata) -> ScheduleFuelCardExportResponse = fuelFuelCardExportService::scheduleFuelCardExport
            val reply1 = invoke1(outbound1, Metadata())
            val outbound2 = SuggestDriverLicensesRequest.newBuilder().apply {
                setContext(request.context)
                setFilter(SuggestDriverLicensesRequestKt.filter {
                    this.createdAfter = request.fuelPolicy.audit.updatedAt
                    this.region = reply0.delivery.billingAddress.region
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply2 = outbound2.let { value -> driversDriverLicenseQueryService.suggestDriverLicenses(value) }
            val outbound3 = RequestFactory.create {
                resolveUsersRequest {
                    this.context = request.context
                }
            }
            val reply3 = identityUserQueryService.resolveUsers(outbound3)
            advanceFuelPolicyWorkflowResponse {
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.delivery.audit.updatedAt
                    }
                    this.fuelPolicyId = request.fuelPolicyId
                    this.status = request.fuelPolicy.status
                }
                this.outcome = AdvanceFuelPolicyWorkflowResponseKt.outcome {
                    this.message = reply3.result.message
                    this.status = reply2.result.status
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = reply3.result.status
                }
            }
        }

    private suspend fun handleInspectFuelPolicyWorkflow(request: InspectFuelPolicyWorkflowRequest): InspectFuelPolicyWorkflowResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyWorkflowService/InspectFuelPolicyWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "InspectFuelPolicyWorkflow", request)
            log.atInfo().addKeyValue("operation", "InspectFuelPolicyWorkflow").log("Handling transport request")
            val outbound0 = listFuelCardReportsRequest {
                this.context = request.context
                this.filter = ListFuelCardReportsRequestKt.filter {
                    this.createdAfter = request.context.deadline
                    this.labels.putAll(request.context.baggageMap)
                    this.ownerId = request.fuelPolicyId
                }
            }
            val reply0 = clientForInspectFuelPolicyWorkflow0.listFuelCardReports(outbound0)
            inspectFuelPolicyWorkflowResponse {
                this.fuelPolicy = fuelPolicy {
                    this.fuelPolicyId = request.fuelPolicyId
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleRetryFuelPolicyWorkflow(request: RetryFuelPolicyWorkflowRequest): RetryFuelPolicyWorkflowResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyWorkflowService/RetryFuelPolicyWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RetryFuelPolicyWorkflow", request)
            log.atInfo().addKeyValue("operation", "RetryFuelPolicyWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicyId).log("Recorded audit detail")
            val outbound0 = browseDriverLicensesRequest {
                this.context = request.context
                this.filter = BrowseDriverLicensesRequestKt.filter {
                    this.createdAfter = request.fuelPolicy.audit.createdAt
                    this.ownerId = request.fuelPolicy.owner.userId
                }
            }
            val reply0 = flowOf(outbound0).map { value -> driversDriverLicenseQueryService.browseDriverLicenses(value) }.single()
            val outbound1 = recallYardMoveRequest {
                this.context = request.context
                this.details = RecallYardMoveRequestKt.details {
                    this.approver = userRef {
                        this.userId = request.fuelPolicy.owner.userId
                    }
                }
                this.yardMove = yardMove {
                    this.audit = auditInfo {
                        this.updatedBy = request.fuelPolicy.audit.updatedBy
                    }
                    this.requester = contactInfo {
                        this.email = request.fuelPolicy.owner.email
                    }
                }
            }
            val reply1 = withTimeout(5_000) { yardsYardMoveApprovalService.recallYardMove(outbound1) }
            val configure2: RecallFuelPolicyRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.owner = userRef {
                        this.displayName = reply1.yardMove.assignee.displayName
                    }
                }
            }
            val outbound2 = recallFuelPolicyRequest(configure2)
            val reply2 = fuelFuelPolicyApprovalService.recallFuelPolicy(outbound2)
            retryFuelPolicyWorkflowResponse {
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.etag = reply1.yardMove.audit.etag
                    }
                    this.effective = request.fuelPolicy.effective
                    this.fuelPolicyId = reply2.fuelPolicy.fuelPolicyId
                    this.owner = userRef {
                        this.email = reply1.yardMove.assignee.email
                    }
                    this.priority = request.fuelPolicy.priority
                }
                this.outcome = RetryFuelPolicyWorkflowResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
            }
        }

    private fun StartFuelPolicyWorkflowRequest.toResolveFuelCardConflictFromStartFuelPolicyWorkflow2(reply1: EstimateProfileResponse): ResolveFuelCardConflictRequest {
        val source = this
        return resolveFuelCardConflictRequest {
            this.context = source.context
            this.fuelCard = fuelCard {
                this.audit = auditInfo {
                    this.createdAt = reply1.profile.audit.createdAt
                }
                this.contact = contactInfo {
                    this.preferredChannel = source.details.notify.preferredChannel
                }
                this.holder = driverRef {
                    this.displayName = source.fuelPolicy.owner.displayName
                }
            }
        }
    }

    private suspend fun handleStartFuelPolicyWorkflow(request: StartFuelPolicyWorkflowRequest): StartFuelPolicyWorkflowResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyWorkflowService/StartFuelPolicyWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StartFuelPolicyWorkflow", request)
            log.atInfo().addKeyValue("operation", "StartFuelPolicyWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicy.fuelPolicyId).log("Recorded audit detail")
            val outbound0 = rejectDeliveryExceptionRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.assignee = userRef {
                        this.userId = request.fuelPolicy.owner.userId
                    }
                    this.audit = auditInfo {
                        this.createdAt = request.fuelPolicy.audit.createdAt
                        this.updatedAt = request.fuelPolicy.audit.updatedAt
                    }
                    this.customer = deliveryRef {
                        this.displayName = request.fuelPolicy.owner.displayName
                    }
                }
            }
            val reply0 = deliveriesDeliveryExceptionApprovalService.rejectDeliveryException(outbound0)
            val outbound1 = estimateProfileRequest {}.personalizationCopy {
                this.context = request.context
                this.profile = profile {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.deliveryException.audit.updatedAt
                    }
                    this.contact = contactInfo {
                        this.preferredChannel = reply0.deliveryException.requester.preferredChannel
                    }
                    this.relationshipManager = userRef {
                        this.email = reply0.deliveryException.requester.email
                        this.userId = reply0.deliveryException.assignee.userId
                    }
                }
            }
            val reply1 = personalizationProfileCalculationService.estimateProfile(outbound1)
            val outbound2 = request.toResolveFuelCardConflictFromStartFuelPolicyWorkflow2(reply1)
            val reply2 = fuelFuelCardSyncService.resolveFuelCardConflict(outbound2)
            val outbound3 = escalateProofOfDeliveryRequest {
                this.context = request.context
                this.details = EscalateProofOfDeliveryRequestKt.details {
                    this.approver = userRef {
                        this.userId = reply0.deliveryException.assignee.userId
                    }
                }
                this.proofOfDelivery = proofOfDelivery {
                    this.audit = auditInfo {
                        this.updatedAt = request.fuelPolicy.audit.updatedAt
                    }
                    this.bank = proofOfDeliveryBankSettlement {
                        this.iban = when (reply2.fuelCard.fundingCase) {
                            FuelCard.FundingCase.BANK -> reply2.fuelCard.bank.iban
                            else -> ""
                        }
                    }
                }
            }
            val reply3 = deliveriesProofOfDeliveryApprovalService.escalateProofOfDelivery(outbound3)
            startFuelPolicyWorkflowResponse {
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.updatedBy = reply3.proofOfDelivery.audit.updatedBy
                    }
                    this.fuelPolicyId = request.fuelPolicyId
                    this.owner = userRef {
                        this.displayName = reply0.deliveryException.assignee.displayName
                    }
                    this.parameters.putAll(request.fuelPolicy.parametersMap)
                }
                this.outcome = StartFuelPolicyWorkflowResponseKt.outcome {
                    this.nextSteps.addAll(reply2.outcome.nextStepsList)
                    this.referenceId = reply3.outcome.referenceId
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.message = reply1.result.message
                    this.status = "ok"
                }
            }
        }
}
