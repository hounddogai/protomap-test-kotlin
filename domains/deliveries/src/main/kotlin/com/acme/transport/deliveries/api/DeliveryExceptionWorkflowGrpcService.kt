package com.acme.transport.deliveries.api

import com.acme.authorization.v1.AccessPolicyServiceGrpcKt
import com.acme.authorization.v1.watchAccessPoliciesRequest
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.AdvanceDeliveryExceptionWorkflowRequest
import com.acme.deliveries.v1.AdvanceDeliveryExceptionWorkflowResponse
import com.acme.deliveries.v1.CancelDeliveryExceptionWorkflowRequest
import com.acme.deliveries.v1.CancelDeliveryExceptionWorkflowResponse
import com.acme.deliveries.v1.CompareProofOfDeliveryOptionsRequest
import com.acme.deliveries.v1.CompareProofOfDeliveryOptionsResponse
import com.acme.deliveries.v1.DeliveryApprovalServiceGrpcKt
import com.acme.deliveries.v1.DeliveryExceptionComment
import com.acme.deliveries.v1.DeliveryExceptionValidationServiceGrpcKt
import com.acme.deliveries.v1.DeliveryExceptionWorkflowServiceGrpcKt
import com.acme.deliveries.v1.EstimateProofOfDeliveryResponse
import com.acme.deliveries.v1.InspectDeliveryExceptionWorkflowRequest
import com.acme.deliveries.v1.InspectDeliveryExceptionWorkflowResponse
import com.acme.deliveries.v1.LintDeliveryExceptionsRequestKt
import com.acme.deliveries.v1.ListDeliveryExceptionWorkflowsRequest
import com.acme.deliveries.v1.ListDeliveryExceptionWorkflowsResponse
import com.acme.deliveries.v1.PauseDeliveryExceptionWorkflowRequest
import com.acme.deliveries.v1.PauseDeliveryExceptionWorkflowResponse
import com.acme.deliveries.v1.ProofOfDeliveryCalculationServiceGrpcKt
import com.acme.deliveries.v1.ProofOfDeliveryQueryServiceGrpcKt
import com.acme.deliveries.v1.ResumeDeliveryExceptionWorkflowRequest
import com.acme.deliveries.v1.ResumeDeliveryExceptionWorkflowResponse
import com.acme.deliveries.v1.ResumeDeliveryExceptionWorkflowResponseKt
import com.acme.deliveries.v1.RetryDeliveryExceptionWorkflowRequest
import com.acme.deliveries.v1.RetryDeliveryExceptionWorkflowResponse
import com.acme.deliveries.v1.RetryDeliveryExceptionWorkflowResponseKt
import com.acme.deliveries.v1.SignalDeliveryExceptionWorkflowRequest
import com.acme.deliveries.v1.SignalDeliveryExceptionWorkflowResponse
import com.acme.deliveries.v1.SignalDeliveryExceptionWorkflowResponseKt
import com.acme.deliveries.v1.StartDeliveryExceptionWorkflowRequest
import com.acme.deliveries.v1.StartDeliveryExceptionWorkflowResponse
import com.acme.deliveries.v1.SuggestProofOfDeliveriesRequestKt
import com.acme.deliveries.v1.advanceDeliveryExceptionWorkflowResponse
import com.acme.deliveries.v1.cancelDeliveryExceptionWorkflowResponse
import com.acme.deliveries.v1.compareProofOfDeliveryOptionsRequest
import com.acme.deliveries.v1.delivery
import com.acme.deliveries.v1.deliveryCardSettlement
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.estimateProofOfDeliveryRequest
import com.acme.deliveries.v1.inspectDeliveryExceptionWorkflowResponse
import com.acme.deliveries.v1.lintDeliveryExceptionsRequest
import com.acme.deliveries.v1.listDeliveryExceptionWorkflowsResponse
import com.acme.deliveries.v1.pauseDeliveryExceptionWorkflowResponse
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.rejectDeliveryRequest
import com.acme.deliveries.v1.resumeDeliveryExceptionWorkflowResponse
import com.acme.deliveries.v1.retryDeliveryExceptionWorkflowResponse
import com.acme.deliveries.v1.signalDeliveryExceptionWorkflowResponse
import com.acme.deliveries.v1.startDeliveryExceptionWorkflowResponse
import com.acme.deliveries.v1.suggestProofOfDeliveriesRequest
import com.acme.drivers.v1.BrowseDriverLicensesRequestKt
import com.acme.drivers.v1.DriverLicense
import com.acme.drivers.v1.DriverLicenseQueryServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseValidationServiceGrpcKt
import com.acme.drivers.v1.DutyStatusCalculationServiceGrpcKt
import com.acme.drivers.v1.ValidateDriverLicenseRequestKt
import com.acme.drivers.v1.browseDriverLicensesRequest
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverLicenseBankSettlement
import com.acme.drivers.v1.driverLicenseRef
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.dutyStatus
import com.acme.drivers.v1.estimateDutyStatusRequest
import com.acme.drivers.v1.validateDriverLicenseRequest
import com.acme.fuel.v1.FuelPolicyValidationServiceGrpcKt
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.previewFuelPolicyRequest
import com.acme.identity.v1.userRef
import com.acme.transport.deliveries.DeliveriesRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.CountYardMoveTotalsRequest
import com.acme.yards.v1.CountYardMoveTotalsRequestKt
import com.acme.yards.v1.CountYardMoveTotalsResponse
import com.acme.yards.v1.YardMoveReportingServiceGrpcKt
import com.acme.yards.v1.countYardMoveTotalsRequest
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Runs the long-lived workflow that takes a delivery exception from start to finish. */
class DeliveryExceptionWorkflowGrpcService(
    private val store: DeliveriesRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyService: AccessPolicyServiceGrpcKt.AccessPolicyServiceCoroutineStub,
    private val deliveriesDeliveryApprovalService: DeliveryApprovalServiceGrpcKt.DeliveryApprovalServiceCoroutineStub,
    private val deliveriesDeliveryExceptionValidationService: DeliveryExceptionValidationServiceGrpcKt.DeliveryExceptionValidationServiceCoroutineStub,
    private val deliveriesProofOfDeliveryCalculationService: ProofOfDeliveryCalculationServiceGrpcKt.ProofOfDeliveryCalculationServiceCoroutineStub,
    private val deliveriesProofOfDeliveryQueryService: ProofOfDeliveryQueryServiceGrpcKt.ProofOfDeliveryQueryServiceCoroutineStub,
    private val driversDriverLicenseQueryService: DriverLicenseQueryServiceGrpcKt.DriverLicenseQueryServiceCoroutineStub,
    private val driversDriverLicenseValidationService: DriverLicenseValidationServiceGrpcKt.DriverLicenseValidationServiceCoroutineStub,
    private val driversDutyStatusCalculationService: DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineStub,
    private val fuelFuelPolicyValidationService: FuelPolicyValidationServiceGrpcKt.FuelPolicyValidationServiceCoroutineStub,
    private val yardsYardMoveReportingService: YardMoveReportingServiceGrpcKt.YardMoveReportingServiceCoroutineStub,
) : DeliveryExceptionWorkflowServiceGrpcKt.DeliveryExceptionWorkflowServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DeliveryExceptionWorkflowGrpcService::class.java)

    private val workerForResumeDeliveryExceptionWorkflow0 = ReplyMailbox<CountYardMoveTotalsRequest, CountYardMoveTotalsResponse>(workers) { payload ->
        withTimeout(5_000) { yardsYardMoveReportingService.countYardMoveTotals(payload) }
    }

    /** Advances a delivery exception. */
    override suspend fun advanceDeliveryExceptionWorkflow(request: AdvanceDeliveryExceptionWorkflowRequest): AdvanceDeliveryExceptionWorkflowResponse {
        return handleAdvanceDeliveryExceptionWorkflow(request)
    }

    /** Cancels a delivery exception. */
    override suspend fun cancelDeliveryExceptionWorkflow(request: CancelDeliveryExceptionWorkflowRequest): CancelDeliveryExceptionWorkflowResponse = handleCancelDeliveryExceptionWorkflow(request)

    /** Returns a delivery exception. */
    override suspend fun inspectDeliveryExceptionWorkflow(request: InspectDeliveryExceptionWorkflowRequest): InspectDeliveryExceptionWorkflowResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryExceptionWorkflowService/InspectDeliveryExceptionWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "InspectDeliveryExceptionWorkflow", request)
            log.atInfo().addKeyValue("operation", "InspectDeliveryExceptionWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            inspectDeliveryExceptionWorkflowResponse {
                this.deliveryException = deliveryException {
                    this.deliveryExceptionId = request.deliveryExceptionId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Lists delivery exceptions a page at a time. */
    override suspend fun listDeliveryExceptionWorkflows(request: ListDeliveryExceptionWorkflowsRequest): ListDeliveryExceptionWorkflowsResponse {
        return handleListDeliveryExceptionWorkflows(request)
    }

    /** Pauses a delivery exception. */
    override suspend fun pauseDeliveryExceptionWorkflow(request: PauseDeliveryExceptionWorkflowRequest): PauseDeliveryExceptionWorkflowResponse = handlePauseDeliveryExceptionWorkflow(request)

    /** Resumes a delivery exception. */
    override suspend fun resumeDeliveryExceptionWorkflow(request: ResumeDeliveryExceptionWorkflowRequest): ResumeDeliveryExceptionWorkflowResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryExceptionWorkflowService/ResumeDeliveryExceptionWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResumeDeliveryExceptionWorkflow", request)
            log.atInfo().addKeyValue("operation", "ResumeDeliveryExceptionWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.deliveryException.deliveryExceptionId).log("Recorded audit detail")
            val outbound0 = countYardMoveTotalsRequest {
                this.context = request.context
                this.filter = CountYardMoveTotalsRequestKt.filter {
                    this.createdAfter = request.deliveryException.sla.dueAt
                    this.labels.putAll(request.details.parametersMap)
                    this.ownerId = request.deliveryException.sla.policyId
                }
            }
            val reply0 = workerForResumeDeliveryExceptionWorkflow0.submit(outbound0)
            resumeDeliveryExceptionWorkflowResponse {
                this.deliveryException = deliveryException {
                    this.assignee = request.deliveryException.assignee
                    this.deliveryExceptionId = request.deliveryExceptionId
                }
                this.outcome = ResumeDeliveryExceptionWorkflowResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    /** Retrys a delivery exception. */
    override suspend fun retryDeliveryExceptionWorkflow(request: RetryDeliveryExceptionWorkflowRequest): RetryDeliveryExceptionWorkflowResponse {
        return handleRetryDeliveryExceptionWorkflow(request)
    }

    /** Signals a delivery exception. */
    override suspend fun signalDeliveryExceptionWorkflow(request: SignalDeliveryExceptionWorkflowRequest): SignalDeliveryExceptionWorkflowResponse = handleSignalDeliveryExceptionWorkflow(request)

    /** Starts a delivery exception. */
    override suspend fun startDeliveryExceptionWorkflow(request: StartDeliveryExceptionWorkflowRequest): StartDeliveryExceptionWorkflowResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryExceptionWorkflowService/StartDeliveryExceptionWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StartDeliveryExceptionWorkflow", request)
            log.atInfo().addKeyValue("operation", "StartDeliveryExceptionWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.deliveryException.deliveryExceptionId).log("Recorded audit detail")
            val outbound0 = validateDriverLicenseRequest {
                this.context = request.context
                this.details = ValidateDriverLicenseRequestKt.details {
                    this.contact = contactInfo {
                        this.name = request.details.notify.name
                    }
                }
                this.driverLicense = driverLicense {
                    this.audit = auditInfo {
                        this.createdAt = request.deliveryException.audit.createdAt
                    }
                    this.counterparty = driverRef {
                        this.email = request.details.notify.email
                    }
                }
            }
            val reply0 = driversDriverLicenseValidationService.validateDriverLicense(outbound0)
            val outbound1 = rejectDeliveryRequest {
                this.context = request.context
                this.delivery = delivery {
                    this.billingAddress = postalAddress {
                        this.locality = reply0.driverLicense.billingAddress.locality
                    }
                    this.card = deliveryCardSettlement {
                        this.holderName = when (reply0.driverLicense.settlementCase) {
                            DriverLicense.SettlementCase.CARD -> reply0.driverLicense.card.holderName
                            else -> ""
                        }
                        this.last4 = when (reply0.driverLicense.settlementCase) {
                            DriverLicense.SettlementCase.CARD -> reply0.driverLicense.card.last4
                            else -> ""
                        }
                        this.network = when (reply0.driverLicense.settlementCase) {
                            DriverLicense.SettlementCase.CARD -> reply0.driverLicense.card.network
                            else -> ""
                        }
                    }
                }
            }
            val reply1 = deliveriesDeliveryApprovalService.rejectDelivery(outbound1)
            startDeliveryExceptionWorkflowResponse {
                this.deliveryException = deliveryException {
                    this.audit = auditInfo {
                        this.updatedBy = reply1.delivery.audit.updatedBy
                    }
                    this.deliveryExceptionId = request.deliveryExceptionId
                    this.status = request.deliveryException.status
                }
                this.result = operationResult {
                    this.message = reply0.outcome.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleAdvanceDeliveryExceptionWorkflow(request: AdvanceDeliveryExceptionWorkflowRequest): AdvanceDeliveryExceptionWorkflowResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryExceptionWorkflowService/AdvanceDeliveryExceptionWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "AdvanceDeliveryExceptionWorkflow", request)
            log.atInfo().addKeyValue("operation", "AdvanceDeliveryExceptionWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.deliveryException.deliveryExceptionId).log("Recorded audit detail")
            val outbound0 = compareProofOfDeliveryOptionsRequest {
                this.context = request.context
                this.proofOfDelivery = proofOfDelivery {
                    this.audit = auditInfo {
                        this.createdAt = request.deliveryException.audit.createdAt
                    }
                    this.counterparty = driverRef {
                        this.email = request.deliveryException.assignee.email
                    }
                }
            }
            val invoke0: suspend (CompareProofOfDeliveryOptionsRequest, Metadata) -> CompareProofOfDeliveryOptionsResponse = deliveriesProofOfDeliveryCalculationService::compareProofOfDeliveryOptions
            val reply0 = invoke0(outbound0, Metadata())
            val outbound1 = watchAccessPoliciesRequest {
                this.context = request.context
                this.subscriber = contactInfo {
                    this.email = reply0.proofOfDelivery.counterparty.email
                    this.name = request.deliveryException.requester.name
                }
            }
            authorizationAccessPolicyService.watchAccessPolicies(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            advanceDeliveryExceptionWorkflowResponse {
                this.deliveryException = deliveryException {
                    this.assignee = request.deliveryException.assignee
                    this.audit = auditInfo {
                        this.updatedBy = reply0.proofOfDelivery.audit.updatedBy
                    }
                    this.deliveryExceptionId = request.deliveryExceptionId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleCancelDeliveryExceptionWorkflow(request: CancelDeliveryExceptionWorkflowRequest): CancelDeliveryExceptionWorkflowResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryExceptionWorkflowService/CancelDeliveryExceptionWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CancelDeliveryExceptionWorkflow", request)
            log.atInfo().addKeyValue("operation", "CancelDeliveryExceptionWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.deliveryException.subject).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                browseDriverLicensesRequest {
                    this.context = request.context
                    this.filter = BrowseDriverLicensesRequestKt.filter {
                        this.createdAfter = request.deliveryException.sla.dueAt
                        this.ownerId = request.deliveryException.deliveryExceptionId
                    }
                }
            }
            val reply0 = driversDriverLicenseQueryService.browseDriverLicenses(outbound0)
            val outbound1 = suggestProofOfDeliveriesRequest {
                this.context = request.context
                this.filter = SuggestProofOfDeliveriesRequestKt.filter {
                    this.createdAfter = request.deliveryException.sla.dueAt
                    this.labels.putAll(reply0.result.detailsMap)
                    this.ownerId = request.deliveryException.sla.policyId
                }
            }
            val reply1 = ClientRegistry.lookup("acme.deliveries.v1.ProofOfDeliveryQueryService", ProofOfDeliveryQueryServiceGrpcKt.ProofOfDeliveryQueryServiceCoroutineStub::class.java).suggestProofOfDeliveries(outbound1)
            cancelDeliveryExceptionWorkflowResponse {
                this.deliveryException = deliveryException {
                    this.deliveryExceptionId = request.deliveryExceptionId
                    this.status = request.deliveryException.status
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply1.result.message
                    this.status = reply1.result.status
                }
            }
        }

    private suspend fun handleListDeliveryExceptionWorkflows(request: ListDeliveryExceptionWorkflowsRequest): ListDeliveryExceptionWorkflowsResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryExceptionWorkflowService/ListDeliveryExceptionWorkflows") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListDeliveryExceptionWorkflows", request)
            log.atInfo().addKeyValue("operation", "ListDeliveryExceptionWorkflows").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            listDeliveryExceptionWorkflowsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handlePauseDeliveryExceptionWorkflow(request: PauseDeliveryExceptionWorkflowRequest): PauseDeliveryExceptionWorkflowResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryExceptionWorkflowService/PauseDeliveryExceptionWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PauseDeliveryExceptionWorkflow", request)
            log.atInfo().addKeyValue("operation", "PauseDeliveryExceptionWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.deliveryException.deliveryExceptionId).log("Recorded audit detail")
            val outbound0 = lintDeliveryExceptionsRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.requester = contactInfo {
                        this.email = request.deliveryException.assignee.email
                        this.phone = request.deliveryException.requester.phone
                    }
                }
                this.details = LintDeliveryExceptionsRequestKt.details {
                    this.contact = contactInfo {
                        this.name = request.details.notify.name
                    }
                }
            }
            val reply0 = deliveriesDeliveryExceptionValidationService.lintDeliveryExceptions(WireCopies.reflective(outbound0))
            pauseDeliveryExceptionWorkflowResponse {
                this.deliveryException = deliveryException {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.deliveryException.audit.updatedAt
                    }
                    this.customFields.putAll(request.deliveryException.customFieldsMap)
                    this.deliveryExceptionId = request.deliveryExceptionId
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleRetryDeliveryExceptionWorkflow(request: RetryDeliveryExceptionWorkflowRequest): RetryDeliveryExceptionWorkflowResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryExceptionWorkflowService/RetryDeliveryExceptionWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RetryDeliveryExceptionWorkflow", request)
            log.atInfo().addKeyValue("operation", "RetryDeliveryExceptionWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.deliveryException.deliveryExceptionId).log("Recorded audit detail")
            var reply0 = EstimateProofOfDeliveryResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<DeliveryExceptionComment>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound0 = estimateProofOfDeliveryRequest {
                            this.context = request.context
                            this.proofOfDelivery = proofOfDelivery {
                                this.audit = auditInfo {
                                    this.createdAt = item.createdAt
                                }
                            }
                        }
                        reply0 = deliveriesProofOfDeliveryCalculationService.estimateProofOfDelivery(outbound0)
                    }
                }
                try {
                    request.deliveryException.commentsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound1 = validateDriverLicenseRequest {
                this.context = request.context
                this.details = ValidateDriverLicenseRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.deliveryException.assignee.email
                    }
                }
                this.driverLicense = driverLicense {
                    this.bank = driverLicenseBankSettlement {
                        this.bic = reply0.proofOfDelivery.bank.bic
                    }
                }
            }
            val reply1 = flowOf(outbound1).map { value -> driversDriverLicenseValidationService.validateDriverLicense(value) }.single()
            val outbound2 = previewFuelPolicyRequest {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.createdBy = reply0.proofOfDelivery.audit.createdBy
                        this.updatedAt = reply0.proofOfDelivery.audit.updatedAt
                    }
                    this.owner = userRef {
                        this.email = request.deliveryException.requester.email
                    }
                }
            }
            val reply2 = withTimeout(5_000) { fuelFuelPolicyValidationService.previewFuelPolicy(outbound2) }
            retryDeliveryExceptionWorkflowResponse {
                this.deliveryException = deliveryException {
                    this.audit = auditInfo {
                        this.updatedBy = reply2.fuelPolicy.audit.updatedBy
                    }
                    this.deliveryExceptionId = request.deliveryExceptionId
                    this.requester = request.deliveryException.requester
                }
                this.outcome = RetryDeliveryExceptionWorkflowResponseKt.outcome {
                    this.status = reply2.outcome.status
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSignalDeliveryExceptionWorkflow(request: SignalDeliveryExceptionWorkflowRequest): SignalDeliveryExceptionWorkflowResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryExceptionWorkflowService/SignalDeliveryExceptionWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SignalDeliveryExceptionWorkflow", request)
            log.atInfo().addKeyValue("operation", "SignalDeliveryExceptionWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.deliveryException.deliveryExceptionId).log("Recorded audit detail")
            val configure0: SuggestProofOfDeliveriesRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.filter = SuggestProofOfDeliveriesRequestKt.filter {
                    this.createdAfter = request.deliveryException.sla.dueAt
                    this.labels.putAll(request.annotationsMap)
                    this.ownerId = request.deliveryException.customer.deliveryId
                }
            }
            val outbound0 = suggestProofOfDeliveriesRequest(configure0)
            val reply0 = deliveriesProofOfDeliveryQueryService.suggestProofOfDeliveries(outbound0)
            val outbound1 = estimateDutyStatusRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.dutyStatus = dutyStatus {
                    this.correlationId = reply0.result.correlationId
                    this.subject = driverLicenseRef {
                        this.displayName = request.deliveryException.assignee.displayName
                        this.number = request.deliveryException.customer.number
                    }
                }
            }
            val reply1 = driversDutyStatusCalculationService.estimateDutyStatus(outbound1)
            signalDeliveryExceptionWorkflowResponse {
                this.deliveryException = deliveryException {
                    this.deliveryExceptionId = request.deliveryExceptionId
                    this.status = request.deliveryException.status
                }
                this.outcome = SignalDeliveryExceptionWorkflowResponseKt.outcome {
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = reply1.result.status
                }
            }
        }
}
