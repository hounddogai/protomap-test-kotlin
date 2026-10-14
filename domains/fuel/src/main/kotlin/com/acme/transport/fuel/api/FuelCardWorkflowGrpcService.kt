package com.acme.transport.fuel.api

import com.acme.careplans.v1.CarePlanServiceGrpcKt
import com.acme.careplans.v1.WatchCarePlansRequestKt
import com.acme.careplans.v1.watchCarePlansRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.customers.v1.CheckCustomerEligibilityRequest
import com.acme.customers.v1.CheckCustomerEligibilityRequestKt
import com.acme.customers.v1.CustomerValidationServiceGrpcKt
import com.acme.customers.v1.PreviewCustomerResponse
import com.acme.customers.v1.checkCustomerEligibilityRequest
import com.acme.customers.v1.copy as customersCopy
import com.acme.customers.v1.customer
import com.acme.customers.v1.previewCustomerRequest
import com.acme.deliveries.v1.ApproveDeliveryExceptionRequest
import com.acme.deliveries.v1.ApproveDeliveryExceptionRequestKt
import com.acme.deliveries.v1.DeliveryExceptionApprovalServiceGrpcKt
import com.acme.deliveries.v1.RejectDeliveryExceptionRequest
import com.acme.deliveries.v1.approveDeliveryExceptionRequest
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.rejectDeliveryExceptionRequest
import com.acme.drivers.v1.driverRef
import com.acme.fuel.v1.AdvanceFuelCardWorkflowRequest
import com.acme.fuel.v1.AdvanceFuelCardWorkflowResponse
import com.acme.fuel.v1.CancelFuelCardWorkflowRequest
import com.acme.fuel.v1.CancelFuelCardWorkflowResponse
import com.acme.fuel.v1.CancelFuelCardWorkflowResponseKt
import com.acme.fuel.v1.FuelCardServiceGrpcKt
import com.acme.fuel.v1.FuelCardWorkflowServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyImportServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyValidationServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionExportServiceGrpcKt
import com.acme.fuel.v1.InspectFuelCardWorkflowRequest
import com.acme.fuel.v1.InspectFuelCardWorkflowResponse
import com.acme.fuel.v1.ListFuelCardWorkflowsRequest
import com.acme.fuel.v1.ListFuelCardWorkflowsResponse
import com.acme.fuel.v1.ListFuelPolicyImportsRequest
import com.acme.fuel.v1.ListFuelPolicyImportsRequestKt
import com.acme.fuel.v1.ListFuelPolicyImportsResponse
import com.acme.fuel.v1.PauseFuelCardWorkflowRequest
import com.acme.fuel.v1.PauseFuelCardWorkflowResponse
import com.acme.fuel.v1.PreviewFuelPolicyRequest
import com.acme.fuel.v1.ResumeFuelCardWorkflowRequest
import com.acme.fuel.v1.ResumeFuelCardWorkflowResponse
import com.acme.fuel.v1.ShareFuelTransactionExportRequestKt
import com.acme.fuel.v1.SignalFuelCardWorkflowRequest
import com.acme.fuel.v1.SignalFuelCardWorkflowResponse
import com.acme.fuel.v1.StartFuelCardWorkflowRequest
import com.acme.fuel.v1.StartFuelCardWorkflowResponse
import com.acme.fuel.v1.advanceFuelCardWorkflowResponse
import com.acme.fuel.v1.cancelFuelCardWorkflowResponse
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.fuelCardLimit
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.getFuelCardRequest
import com.acme.fuel.v1.inspectFuelCardWorkflowResponse
import com.acme.fuel.v1.listFuelCardWorkflowsResponse
import com.acme.fuel.v1.listFuelPolicyImportsRequest
import com.acme.fuel.v1.pauseFuelCardWorkflowResponse
import com.acme.fuel.v1.previewFuelPolicyRequest
import com.acme.fuel.v1.resumeFuelCardWorkflowResponse
import com.acme.fuel.v1.retryFuelPolicyImportRequest
import com.acme.fuel.v1.shareFuelTransactionExportRequest
import com.acme.fuel.v1.signalFuelCardWorkflowResponse
import com.acme.fuel.v1.startFuelCardWorkflowResponse
import com.acme.fuel.v1.watchFuelCardsRequest
import com.acme.identity.v1.userRef
import com.acme.transport.fuel.FuelRepository
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.DockWorkflowServiceGrpcKt
import com.acme.yards.v1.ListPendingYardMovesRequestKt
import com.acme.yards.v1.RetryDockWorkflowRequest
import com.acme.yards.v1.YardMoveApprovalServiceGrpcKt
import com.acme.yards.v1.dock
import com.acme.yards.v1.listPendingYardMovesRequest
import com.acme.yards.v1.retryDockWorkflowRequest
import com.google.protobuf.Any as ProtoAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import org.slf4j.LoggerFactory

/** Runs the long-lived workflow that takes a fuel card from start to finish. */
class FuelCardWorkflowGrpcService(
    private val store: FuelRepository,
    private val workers: CoroutineScope,
    private val careplansCarePlanService: CarePlanServiceGrpcKt.CarePlanServiceCoroutineStub,
    private val customersCustomerValidationService: CustomerValidationServiceGrpcKt.CustomerValidationServiceCoroutineStub,
    private val deliveriesDeliveryExceptionApprovalService: DeliveryExceptionApprovalServiceGrpcKt.DeliveryExceptionApprovalServiceCoroutineStub,
    private val fuelFuelCardService: FuelCardServiceGrpcKt.FuelCardServiceCoroutineStub,
    private val fuelFuelPolicyImportService: FuelPolicyImportServiceGrpcKt.FuelPolicyImportServiceCoroutineStub,
    private val fuelFuelPolicyValidationService: FuelPolicyValidationServiceGrpcKt.FuelPolicyValidationServiceCoroutineStub,
    private val fuelFuelTransactionExportService: FuelTransactionExportServiceGrpcKt.FuelTransactionExportServiceCoroutineStub,
    private val yardsDockWorkflowService: DockWorkflowServiceGrpcKt.DockWorkflowServiceCoroutineStub,
    private val yardsYardMoveApprovalService: YardMoveApprovalServiceGrpcKt.YardMoveApprovalServiceCoroutineStub,
) : FuelCardWorkflowServiceGrpcKt.FuelCardWorkflowServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(FuelCardWorkflowGrpcService::class.java)

    /** Advances a fuel card. */
    override suspend fun advanceFuelCardWorkflow(request: AdvanceFuelCardWorkflowRequest): AdvanceFuelCardWorkflowResponse {
        return handleAdvanceFuelCardWorkflow(request)
    }

    /** Cancels a fuel card. */
    override suspend fun cancelFuelCardWorkflow(request: CancelFuelCardWorkflowRequest): CancelFuelCardWorkflowResponse = handleCancelFuelCardWorkflow(request)

    /** Returns a fuel card. */
    override suspend fun inspectFuelCardWorkflow(request: InspectFuelCardWorkflowRequest): InspectFuelCardWorkflowResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardWorkflowService/InspectFuelCardWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "InspectFuelCardWorkflow", request)
            log.atInfo().addKeyValue("operation", "InspectFuelCardWorkflow").log("Handling transport request")
            val outbound0 = retryFuelPolicyImportRequest {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.owner = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply0 = fuelFuelPolicyImportService.retryFuelPolicyImport(outbound0)
            inspectFuelCardWorkflowResponse {
                this.fuelCard = fuelCard {
                    this.fuelCardId = request.fuelCardId
                    this.limits.addAll(listOf(fuelCardLimit {
                        this.amount = reply0.fuelPolicy.discount.amount
                    }))
                }
                this.result = operationResult {
                    this.message = reply0.outcome.message
                    this.status = "ok"
                }
            }
        }

    /** Lists fuel cards a page at a time. */
    override suspend fun listFuelCardWorkflows(request: ListFuelCardWorkflowsRequest): ListFuelCardWorkflowsResponse {
        return handleListFuelCardWorkflows(request)
    }

    /** Pauses a fuel card. */
    override suspend fun pauseFuelCardWorkflow(request: PauseFuelCardWorkflowRequest): PauseFuelCardWorkflowResponse = handlePauseFuelCardWorkflow(request)

    /** Resumes a fuel card. */
    override suspend fun resumeFuelCardWorkflow(request: ResumeFuelCardWorkflowRequest): ResumeFuelCardWorkflowResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardWorkflowService/ResumeFuelCardWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResumeFuelCardWorkflow", request)
            log.atInfo().addKeyValue("operation", "ResumeFuelCardWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCard.fuelCardId).log("Recorded audit detail")
            val outbound0 = ResumeFuelCardWorkflow0Command(
                context = request.context,
                fuelPolicyOwnerEmail = request.fuelCard.contact.email
            ).toRequest()
            val reply0 = fuelFuelPolicyValidationService.previewFuelPolicy(outbound0)
            val outbound1 = rejectDeliveryExceptionRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.fuelPolicy.audit.updatedBy
                    }
                }
            }
            val command1: Submission<RejectDeliveryExceptionRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = deliveriesDeliveryExceptionApprovalService.rejectDeliveryException(payload1)
            resumeFuelCardWorkflowResponse {
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.fuelPolicy.audit.updatedBy
                    }
                    this.contact = request.fuelCard.contact
                    this.fuelCardId = request.fuelCardId
                }
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = "ok"
                }
            }
        }

    /** Signals a fuel card. */
    override suspend fun signalFuelCardWorkflow(request: SignalFuelCardWorkflowRequest): SignalFuelCardWorkflowResponse {
        return handleSignalFuelCardWorkflow(request)
    }

    /** Starts a fuel card. */
    override suspend fun startFuelCardWorkflow(request: StartFuelCardWorkflowRequest): StartFuelCardWorkflowResponse = handleStartFuelCardWorkflow(request)

    private suspend fun handleAdvanceFuelCardWorkflow(request: AdvanceFuelCardWorkflowRequest): AdvanceFuelCardWorkflowResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardWorkflowService/AdvanceFuelCardWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "AdvanceFuelCardWorkflow", request)
            log.atInfo().addKeyValue("operation", "AdvanceFuelCardWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCard.holder.driverId).log("Recorded audit detail")
            val outbound0 = shareFuelTransactionExportRequest {
                this.context = request.context
                this.details = ShareFuelTransactionExportRequestKt.details {
                    this.recipient = contactInfo {
                        this.email = request.fuelCard.contact.email
                        this.phone = request.fuelCard.contact.phone
                        this.preferredChannel = request.details.notify.preferredChannel
                    }
                }
            }
            val reply0 = fuelFuelTransactionExportService.shareFuelTransactionExport(outbound0)
            val outbound1 = getFuelCardRequest {
                this.context = request.context
                this.fuelCardId = request.fuelCard.fuelCardId
            }
            val reply1 = fuelFuelCardService.getFuelCard(outbound1)
            advanceFuelCardWorkflowResponse {
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.createdAt = reply1.fuelCard.audit.createdAt
                    }
                    this.currencyCode = request.fuelCard.currencyCode
                    this.fuelCardId = request.fuelCardId
                    this.holder = driverRef {
                        this.email = reply1.fuelCard.holder.email
                    }
                }
                this.result = operationResult {
                    this.correlationId = reply0.fuelTransaction.correlationId
                    this.status = "ok"
                }
            }
        }

    private fun CancelFuelCardWorkflowRequest.toCheckCustomerEligibilityFromCancelFuelCardWorkflow1(reply0: PreviewCustomerResponse): CheckCustomerEligibilityRequest {
        val source = this
        return checkCustomerEligibilityRequest {
            this.context = source.context
            this.customer = customer {
                this.contact = contactInfo {
                    this.name = source.fuelCard.contact.name
                }
            }
            this.details = CheckCustomerEligibilityRequestKt.details {
                this.contact = contactInfo {
                    this.phone = reply0.customer.contact.phone
                }
            }
        }
    }

    private suspend fun handleCancelFuelCardWorkflow(request: CancelFuelCardWorkflowRequest): CancelFuelCardWorkflowResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardWorkflowService/CancelFuelCardWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CancelFuelCardWorkflow", request)
            log.atInfo().addKeyValue("operation", "CancelFuelCardWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCard.fuelCardId).log("Recorded audit detail")
            val outbound0 = previewCustomerRequest {}.customersCopy {
                this.context = request.context
                this.customer = customer {
                    this.contact = contactInfo {
                        this.email = request.fuelCard.holder.email
                    }
                }
            }
            val reply0 = customersCustomerValidationService.previewCustomer(outbound0)
            val outbound1 = request.toCheckCustomerEligibilityFromCancelFuelCardWorkflow1(reply0)
            val reply1 = customersCustomerValidationService.checkCustomerEligibility(outbound1)
            cancelFuelCardWorkflowResponse {
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.etag = reply0.customer.audit.etag
                    }
                    this.currencyCode = request.fuelCard.currencyCode
                    this.fuelCardId = request.fuelCardId
                }
                this.outcome = CancelFuelCardWorkflowResponseKt.outcome {
                    this.referenceId = reply1.outcome.referenceId
                }
                this.result = operationResult {
                    this.message = reply0.outcome.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleListFuelCardWorkflows(request: ListFuelCardWorkflowsRequest): ListFuelCardWorkflowsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardWorkflowService/ListFuelCardWorkflows") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListFuelCardWorkflows", request)
            log.atInfo().addKeyValue("operation", "ListFuelCardWorkflows").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = approveDeliveryExceptionRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.assignee = userRef {
                        this.email = request.context.actor.email
                    }
                    this.audit = auditInfo {
                        this.updatedAt = request.filter.createdAfter
                    }
                }
                this.details = ApproveDeliveryExceptionRequestKt.details {
                    this.approver = userRef {
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = deliveriesDeliveryExceptionApprovalService.approveDeliveryException(envelope0.unpack(ApproveDeliveryExceptionRequest::class.java))
            listFuelCardWorkflowsResponse {
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handlePauseFuelCardWorkflow(request: PauseFuelCardWorkflowRequest): PauseFuelCardWorkflowResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardWorkflowService/PauseFuelCardWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PauseFuelCardWorkflow", request)
            log.atInfo().addKeyValue("operation", "PauseFuelCardWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCard.fuelCardId).log("Recorded audit detail")
            val outbound0 = listPendingYardMovesRequest {
                this.context = request.context
                this.filter = ListPendingYardMovesRequestKt.filter {
                    this.createdAfter = request.fuelCard.audit.createdAt
                    this.labels.putAll(request.fuelCard.labelsMap)
                }
            }
            val reply0 = yardsYardMoveApprovalService.listPendingYardMoves(WireCopies.json(outbound0))
            pauseFuelCardWorkflowResponse {
                this.fuelCard = fuelCard {
                    this.balance = request.fuelCard.balance
                    this.fuelCardId = request.fuelCardId
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private data class ResumeFuelCardWorkflow0Command(
        val context: RequestContext,
        val fuelPolicyOwnerEmail: String
    ) {
        fun toRequest(): PreviewFuelPolicyRequest {
            val command = this
            return previewFuelPolicyRequest {
                this.context = command.context
                this.fuelPolicy = fuelPolicy {
                    this.owner = userRef {
                        this.email = command.fuelPolicyOwnerEmail
                    }
                }
            }
        }
    }

    private suspend fun handleSignalFuelCardWorkflow(request: SignalFuelCardWorkflowRequest): SignalFuelCardWorkflowResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardWorkflowService/SignalFuelCardWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SignalFuelCardWorkflow", request)
            log.atInfo().addKeyValue("operation", "SignalFuelCardWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCard.fuelCardId).log("Recorded audit detail")
            val outbound0 = listFuelPolicyImportsRequest {
                this.context = request.context
                this.filter = ListFuelPolicyImportsRequestKt.filter {
                    this.createdAfter = request.fuelCard.openedAt
                    this.labels.putAll(request.fuelCard.labelsMap)
                }
            }
            val adapter0 = OutboundPort<ListFuelPolicyImportsRequest, ListFuelPolicyImportsResponse> { payload ->
                fuelFuelPolicyImportService.listFuelPolicyImports(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = watchFuelCardsRequest {
                this.context = request.context
                this.subscriber = contactInfo {
                    this.preferredChannel = request.details.notify.preferredChannel
                }
            }
            fuelFuelCardService.watchFuelCards(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            signalFuelCardWorkflowResponse {
                this.fuelCard = fuelCard {
                    this.fuelCardId = request.fuelCardId
                    this.openedAt = request.fuelCard.openedAt
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleStartFuelCardWorkflow(request: StartFuelCardWorkflowRequest): StartFuelCardWorkflowResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardWorkflowService/StartFuelCardWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StartFuelCardWorkflow", request)
            log.atInfo().addKeyValue("operation", "StartFuelCardWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCard.fuelCardId).log("Recorded audit detail")
            val outbound0 = watchCarePlansRequest {
                this.context = request.context
                this.filter = WatchCarePlansRequestKt.filter {
                    this.labels.putAll(request.fuelCard.labelsMap)
                }
                this.subscriber = contactInfo {
                    this.email = request.fuelCard.holder.email
                    this.name = request.details.notify.name
                    this.preferredChannel = request.details.notify.preferredChannel
                }
            }
            careplansCarePlanService.watchCarePlans(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = RetryDockWorkflowRequest.newBuilder().apply {
                setContext(request.context)
                setDock(dock {
                    this.audit = auditInfo {
                        this.etag = request.fuelCard.audit.etag
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> yardsDockWorkflowService.retryDockWorkflow(value) }
            startFuelCardWorkflowResponse {
                this.fuelCard = fuelCard {
                    this.fuelCardId = request.fuelCardId
                    this.holder = driverRef {
                        this.email = reply1.dock.owner.email
                    }
                    this.openedAt = request.fuelCard.openedAt
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }
}
