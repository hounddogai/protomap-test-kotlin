package com.acme.transport.fuel.adapters

import com.acme.audit.v1.AuditEventServiceGrpcKt
import com.acme.audit.v1.auditEvent
import com.acme.audit.v1.auditTrailRef
import com.acme.audit.v1.copy as auditCopy
import com.acme.audit.v1.createAuditEventRequest
import com.acme.audit.v1.updateAuditEventRequest
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.AdvanceProofOfDeliveryWorkflowRequestKt
import com.acme.deliveries.v1.DeliveryExceptionApprovalServiceGrpcKt
import com.acme.deliveries.v1.ProofOfDeliveryWorkflowServiceGrpcKt
import com.acme.deliveries.v1.advanceProofOfDeliveryWorkflowRequest
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.submitDeliveryExceptionRequest
import com.acme.fuel.v1.CalculateFuelPolicyRequest
import com.acme.fuel.v1.CalculateFuelPolicyRequestKt
import com.acme.fuel.v1.CancelFuelCardExportRequest
import com.acme.fuel.v1.CancelFuelCardExportResponse
import com.acme.fuel.v1.CancelFuelCardExportResponseKt
import com.acme.fuel.v1.FuelCardExportServiceGrpcKt
import com.acme.fuel.v1.FuelCardLimit
import com.acme.fuel.v1.FuelCardValidationServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyCalculationServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyValidationServiceGrpcKt
import com.acme.fuel.v1.GetFuelCardExportRequest
import com.acme.fuel.v1.GetFuelCardExportResponse
import com.acme.fuel.v1.ListFuelCardExportsRequest
import com.acme.fuel.v1.ListFuelCardExportsResponse
import com.acme.fuel.v1.PreviewFuelCardRequest
import com.acme.fuel.v1.PreviewFuelCardRequestKt
import com.acme.fuel.v1.RetryFuelCardExportRequest
import com.acme.fuel.v1.RetryFuelCardExportResponse
import com.acme.fuel.v1.ScheduleFuelCardExportRequest
import com.acme.fuel.v1.ScheduleFuelCardExportResponse
import com.acme.fuel.v1.ShareFuelCardExportRequest
import com.acme.fuel.v1.ShareFuelCardExportResponse
import com.acme.fuel.v1.StreamFuelCardExportRequest
import com.acme.fuel.v1.StreamFuelCardExportResponse
import com.acme.fuel.v1.calculateFuelPolicyRequest
import com.acme.fuel.v1.cancelFuelCardExportResponse
import com.acme.fuel.v1.checkFuelCardEligibilityRequest
import com.acme.fuel.v1.compareFuelPolicyOptionsRequest
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.fuelCardLimit
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.fuelPolicyBlockAction
import com.acme.fuel.v1.getFuelCardExportResponse
import com.acme.fuel.v1.listFuelCardExportsResponse
import com.acme.fuel.v1.previewFuelCardRequest
import com.acme.fuel.v1.retryFuelCardExportResponse
import com.acme.fuel.v1.scheduleFuelCardExportResponse
import com.acme.fuel.v1.scoreFuelPolicyRequest
import com.acme.fuel.v1.shareFuelCardExportResponse
import com.acme.fuel.v1.streamFuelCardExportResponse
import com.acme.identity.v1.userRef
import com.acme.notifications.v1.CreateNotificationRequestKt
import com.acme.notifications.v1.NotificationServiceGrpcKt
import com.acme.notifications.v1.createNotificationRequest
import com.acme.notifications.v1.notification
import com.acme.transport.fuel.FuelRepository
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.DockWorkflowServiceGrpcKt
import com.acme.yards.v1.RejectYardRequest
import com.acme.yards.v1.RejectYardResponse
import com.acme.yards.v1.StartDockWorkflowRequestKt
import com.acme.yards.v1.StartDockWorkflowResponse
import com.acme.yards.v1.YardApprovalServiceGrpcKt
import com.acme.yards.v1.dock
import com.acme.yards.v1.rejectYardRequest
import com.acme.yards.v1.startDockWorkflowRequest
import com.acme.yards.v1.yard
import com.google.protobuf.Any as ProtoAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Exports fuel cards to files and other systems. */
class FuelCardExportController(
    private val store: FuelRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventService: AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub,
    private val deliveriesDeliveryExceptionApprovalService: DeliveryExceptionApprovalServiceGrpcKt.DeliveryExceptionApprovalServiceCoroutineStub,
    private val deliveriesProofOfDeliveryWorkflowService: ProofOfDeliveryWorkflowServiceGrpcKt.ProofOfDeliveryWorkflowServiceCoroutineStub,
    private val fuelFuelCardValidationService: FuelCardValidationServiceGrpcKt.FuelCardValidationServiceCoroutineStub,
    private val fuelFuelPolicyCalculationService: FuelPolicyCalculationServiceGrpcKt.FuelPolicyCalculationServiceCoroutineStub,
    private val fuelFuelPolicyValidationService: FuelPolicyValidationServiceGrpcKt.FuelPolicyValidationServiceCoroutineStub,
    private val notificationsNotificationService: NotificationServiceGrpcKt.NotificationServiceCoroutineStub,
    private val yardsDockWorkflowService: DockWorkflowServiceGrpcKt.DockWorkflowServiceCoroutineStub,
    private val yardsYardApprovalService: YardApprovalServiceGrpcKt.YardApprovalServiceCoroutineStub,
) : FuelCardExportServiceGrpcKt.FuelCardExportServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(FuelCardExportController::class.java)

    private val workerForGetFuelCardExport0 = ReplyMailbox<RejectYardRequest, RejectYardResponse>(workers) { payload ->
        withTimeout(5_000) { yardsYardApprovalService.rejectYard(payload) }
    }

    private val clientForGetFuelCardExport1 by lazy { fuelFuelCardValidationService }

    /** Cancels a fuel card. */
    override suspend fun cancelFuelCardExport(request: CancelFuelCardExportRequest): CancelFuelCardExportResponse = handleCancelFuelCardExport(request)

    /** Returns a fuel card. */
    override suspend fun getFuelCardExport(request: GetFuelCardExportRequest): GetFuelCardExportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardExportService/GetFuelCardExport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetFuelCardExport", request)
            log.atInfo().addKeyValue("operation", "GetFuelCardExport").log("Handling transport request")
            val outbound0 = rejectYardRequest {
                this.context = request.context
                this.yard = yard {
                    this.owner = userRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = workerForGetFuelCardExport0.submit(outbound0)
            val outbound1 = checkFuelCardEligibilityRequest {
                this.context = request.context
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.createdAt = reply0.yard.audit.createdAt
                    }
                }
            }
            val reply1 = clientForGetFuelCardExport1.checkFuelCardEligibility(outbound1)
            getFuelCardExportResponse {
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.createdAt = reply0.yard.audit.createdAt
                    }
                    this.fuelCardId = request.fuelCardId
                    this.labels.putAll(reply1.fuelCard.labelsMap)
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply1.result.message
                    this.status = "ok"
                }
            }
        }

    /** Lists fuel cards a page at a time. */
    override suspend fun listFuelCardExports(request: ListFuelCardExportsRequest): ListFuelCardExportsResponse {
        return handleListFuelCardExports(request)
    }

    /** Retrys a fuel card. */
    override suspend fun retryFuelCardExport(request: RetryFuelCardExportRequest): RetryFuelCardExportResponse = handleRetryFuelCardExport(request)

    /** Schedules a fuel card. */
    override suspend fun scheduleFuelCardExport(request: ScheduleFuelCardExportRequest): ScheduleFuelCardExportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardExportService/ScheduleFuelCardExport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScheduleFuelCardExport", request)
            log.atInfo().addKeyValue("operation", "ScheduleFuelCardExport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCardId).log("Recorded audit detail")
            val outbound0 = advanceProofOfDeliveryWorkflowRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.details = AdvanceProofOfDeliveryWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
                this.proofOfDelivery = proofOfDelivery {
                    this.metadata.putAll(request.details.filtersMap)
                }
                this.reason = request.reason
            }
            val reply0 = deliveriesProofOfDeliveryWorkflowService.advanceProofOfDeliveryWorkflow(outbound0)
            val outbound1 = updateAuditEventRequest {}.auditCopy {
                this.auditEvent = auditEvent {
                    this.correlationId = reply0.result.correlationId
                    this.subject = auditTrailRef {
                        this.displayName = reply0.proofOfDelivery.counterparty.displayName
                        this.number = reply0.proofOfDelivery.number
                    }
                }
                this.context = request.context
            }
            val reply1 = auditAuditEventService.updateAuditEvent(outbound1)
            scheduleFuelCardExportResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.correlationId = reply1.auditEvent.correlationId
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = reply0.outcome.status
                }
            }
        }

    /** Shares a fuel card. */
    override suspend fun shareFuelCardExport(request: ShareFuelCardExportRequest): ShareFuelCardExportResponse {
        return handleShareFuelCardExport(request)
    }

    /** Streams every matching fuel card. */
    override fun streamFuelCardExport(request: StreamFuelCardExportRequest): Flow<StreamFuelCardExportResponse> = flow {
        emit(handleStreamFuelCardExport(request))
    }

    private suspend fun handleCancelFuelCardExport(request: CancelFuelCardExportRequest): CancelFuelCardExportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardExportService/CancelFuelCardExport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CancelFuelCardExport", request)
            log.atInfo().addKeyValue("operation", "CancelFuelCardExport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = compareFuelPolicyOptionsRequest {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.block = fuelPolicyBlockAction {
                        this.reason = request.reason
                    }
                    this.owner = userRef {
                        this.displayName = request.details.newOwner.displayName
                        this.email = request.details.notifyContact.email
                        this.userId = request.details.newOwner.userId
                    }
                }
            }
            val reply0 = fuelFuelPolicyCalculationService.compareFuelPolicyOptions(WireCopies.reflective(outbound0))
            cancelFuelCardExportResponse {
                this.fuelCard = fuelCard {
                    this.fuelCardId = request.fuelCardId
                    this.limits.addAll(listOf(fuelCardLimit {
                        this.amount = reply0.fuelPolicy.discount.amount
                    }))
                }
                this.outcome = CancelFuelCardExportResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleListFuelCardExports(request: ListFuelCardExportsRequest): ListFuelCardExportsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardExportService/ListFuelCardExports") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListFuelCardExports", request)
            log.atInfo().addKeyValue("operation", "ListFuelCardExports").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = submitDeliveryExceptionRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.assignee = userRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = flowOf(outbound0).map { value -> deliveriesDeliveryExceptionApprovalService.submitDeliveryException(value) }.single()
            val outbound1 = scoreFuelPolicyRequest {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.createdBy = reply0.deliveryException.audit.createdBy
                        this.updatedAt = reply0.deliveryException.audit.updatedAt
                    }
                    this.owner = userRef {
                        this.email = reply0.deliveryException.requester.email
                        this.userId = reply0.deliveryException.assignee.userId
                    }
                }
            }
            val reply1 = withTimeout(5_000) { fuelFuelPolicyValidationService.scoreFuelPolicy(outbound1) }
            listFuelCardExportsResponse {
                this.fuelCards.addAll(listOf(fuelCard {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.deliveryException.audit.updatedAt
                    }
                    this.limits.addAll(listOf(fuelCardLimit {
                        this.amount = reply1.fuelPolicy.discount.amount
                    }))
                }))
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleRetryFuelCardExport(request: RetryFuelCardExportRequest): RetryFuelCardExportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardExportService/RetryFuelCardExport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RetryFuelCardExport", request)
            log.atInfo().addKeyValue("operation", "RetryFuelCardExport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCard.fuelCardId).log("Recorded audit detail")
            val configure0: CreateNotificationRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.notification = notification {
                    this.audit = auditInfo {
                        this.createdAt = request.fuelCard.audit.createdAt
                        this.createdBy = request.fuelCard.audit.createdBy
                    }
                    this.requester = contactInfo {
                        this.name = request.fuelCard.contact.name
                    }
                }
            }
            val outbound0 = createNotificationRequest(configure0)
            val reply0 = notificationsNotificationService.createNotification(outbound0)
            var reply1 = StartDockWorkflowResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<FuelCardLimit>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound1 = startDockWorkflowRequest {
                            this.context = request.context
                            this.details = StartDockWorkflowRequestKt.details {
                                this.notify = contactInfo {
                                    this.name = reply0.notification.requester.name
                                }
                            }
                            this.dock = dock {
                                this.name = item.name
                            }
                        }
                        reply1 = yardsDockWorkflowService.startDockWorkflow(outbound1)
                    }
                }
                try {
                    request.fuelCard.limitsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            retryFuelCardExportResponse {
                this.fuelCard = fuelCard {
                    this.contact = contactInfo {
                        this.name = reply0.notification.requester.name
                    }
                    this.fuelCardId = request.fuelCardId
                    this.holder = request.fuelCard.holder
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private fun ShareFuelCardExportRequest.toPreviewFuelCardFromShareFuelCardExport0(): PreviewFuelCardRequest {
        val source = this
        return previewFuelCardRequest {
            this.context = source.context
            this.details = PreviewFuelCardRequestKt.details {
                this.contact = contactInfo {
                    this.phone = source.details.recipient.phone
                    this.preferredChannel = source.details.recipient.preferredChannel
                }
            }
            this.fuelCard = fuelCard {
                this.contact = contactInfo {
                    this.email = source.details.recipient.email
                    this.name = source.details.recipient.name
                }
            }
        }
    }

    private suspend fun handleShareFuelCardExport(request: ShareFuelCardExportRequest): ShareFuelCardExportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardExportService/ShareFuelCardExport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ShareFuelCardExport", request)
            log.atInfo().addKeyValue("operation", "ShareFuelCardExport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.templateId).log("Recorded audit detail")
            val outbound0 = request.toPreviewFuelCardFromShareFuelCardExport0()
            val reply0 = fuelFuelCardValidationService.previewFuelCard(outbound0)
            shareFuelCardExportResponse {
                this.fuelCard = fuelCard {
                    this.contact = contactInfo {
                        this.preferredChannel = request.details.recipient.preferredChannel
                    }
                    this.fuelCardId = request.fuelCardId
                    this.openedAt = reply0.fuelCard.openedAt
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleStreamFuelCardExport(request: StreamFuelCardExportRequest): StreamFuelCardExportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardExportService/StreamFuelCardExport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StreamFuelCardExport", request)
            log.atInfo().addKeyValue("operation", "StreamFuelCardExport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = createAuditEventRequest {
                this.auditEvent = auditEvent {
                    this.occurredAt = request.filter.createdAfter
                    this.source = request.filter.ownerId
                }
                this.context = request.context
            }
            val reply0 = auditAuditEventService.createAuditEvent(outbound0)
            val outbound1 = calculateFuelPolicyRequest {
                this.context = request.context
                this.details = CalculateFuelPolicyRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.filter.region
                    }
                }
                this.fuelPolicy = fuelPolicy {
                    this.owner = userRef {
                        this.displayName = reply0.auditEvent.subject.displayName
                    }
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = fuelFuelPolicyCalculationService.calculateFuelPolicy(envelope1.unpack(CalculateFuelPolicyRequest::class.java))
            streamFuelCardExportResponse {
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.updatedBy = reply1.fuelPolicy.audit.updatedBy
                    }
                    this.labels.putAll(request.filter.labelsMap)
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }
}
