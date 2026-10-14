package com.acme.transport.deliveries.api

import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.ApproveDeliveryExceptionRequest
import com.acme.deliveries.v1.ApproveDeliveryExceptionResponse
import com.acme.deliveries.v1.ApproveDeliveryExceptionResponseKt
import com.acme.deliveries.v1.CalculateProofOfDeliveryResponse
import com.acme.deliveries.v1.CommentDeliveryApprovalRequestKt
import com.acme.deliveries.v1.DelegateProofOfDeliveryApprovalRequest
import com.acme.deliveries.v1.DelegateProofOfDeliveryApprovalRequestKt
import com.acme.deliveries.v1.DelegateProofOfDeliveryApprovalResponse
import com.acme.deliveries.v1.DeliveryApprovalServiceGrpcKt
import com.acme.deliveries.v1.DeliveryExceptionApprovalServiceGrpcKt
import com.acme.deliveries.v1.DeliveryExceptionStatusChange
import com.acme.deliveries.v1.DeliveryServiceGrpcKt
import com.acme.deliveries.v1.EscalateDeliveryExceptionRequest
import com.acme.deliveries.v1.EscalateDeliveryExceptionResponse
import com.acme.deliveries.v1.EscalateDeliveryExceptionResponseKt
import com.acme.deliveries.v1.EscalateProofOfDeliveryRequestKt
import com.acme.deliveries.v1.GetDeliveryExceptionApprovalStatusRequest
import com.acme.deliveries.v1.GetDeliveryExceptionApprovalStatusResponse
import com.acme.deliveries.v1.ListDeliveriesRequestKt
import com.acme.deliveries.v1.ProofOfDeliveryApprovalServiceGrpcKt
import com.acme.deliveries.v1.ProofOfDeliveryCalculationServiceGrpcKt
import com.acme.deliveries.v1.ProofOfDeliveryValidationServiceGrpcKt
import com.acme.deliveries.v1.RecalculateProofOfDeliveriesResponse
import com.acme.deliveries.v1.RecallDeliveryExceptionRequest
import com.acme.deliveries.v1.RecallDeliveryExceptionResponse
import com.acme.deliveries.v1.RecallDeliveryExceptionResponseKt
import com.acme.deliveries.v1.RejectDeliveryExceptionRequest
import com.acme.deliveries.v1.RejectDeliveryExceptionResponse
import com.acme.deliveries.v1.RejectDeliveryExceptionResponseKt
import com.acme.deliveries.v1.SubmitDeliveryExceptionRequest
import com.acme.deliveries.v1.SubmitDeliveryExceptionResponse
import com.acme.deliveries.v1.approveDeliveryExceptionResponse
import com.acme.deliveries.v1.calculateProofOfDeliveryRequest
import com.acme.deliveries.v1.commentDeliveryApprovalRequest
import com.acme.deliveries.v1.copy as deliveriesCopy
import com.acme.deliveries.v1.delegateProofOfDeliveryApprovalRequest
import com.acme.deliveries.v1.delivery
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.deliveryExceptionComment
import com.acme.deliveries.v1.deliveryExceptionSla
import com.acme.deliveries.v1.escalateDeliveryExceptionResponse
import com.acme.deliveries.v1.escalateProofOfDeliveryRequest
import com.acme.deliveries.v1.getDeliveryExceptionApprovalStatusResponse
import com.acme.deliveries.v1.listDeliveriesRequest
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.recalculateProofOfDeliveriesRequest
import com.acme.deliveries.v1.recallDeliveryExceptionResponse
import com.acme.deliveries.v1.rejectDeliveryExceptionResponse
import com.acme.deliveries.v1.scoreProofOfDeliveryRequest
import com.acme.deliveries.v1.submitDeliveryExceptionResponse
import com.acme.drivers.v1.DriverCalculationServiceGrpcKt
import com.acme.drivers.v1.EstimateDriverResponse
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.estimateDriverRequest
import com.acme.fleet.v1.ScoreVehicleRequest
import com.acme.fleet.v1.VehicleValidationServiceGrpcKt
import com.acme.fleet.v1.scoreVehicleRequest
import com.acme.fleet.v1.vehicle
import com.acme.identity.v1.userRef
import com.acme.notifications.v1.NotificationValidationServiceGrpcKt
import com.acme.notifications.v1.ScoreNotificationRequestKt
import com.acme.notifications.v1.notification
import com.acme.notifications.v1.scoreNotificationRequest
import com.acme.search.v1.NormalizeSearchIndexRequest
import com.acme.search.v1.SearchIndex
import com.acme.search.v1.SearchIndexValidationServiceGrpcKt
import com.acme.search.v1.ValidateSearchIndexRequestKt
import com.acme.search.v1.normalizeSearchIndexRequest
import com.acme.search.v1.searchIndex
import com.acme.search.v1.validateSearchIndexRequest
import com.acme.transport.deliveries.DeliveriesRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.warehousing.v1.WarehouseServiceGrpcKt
import com.acme.warehousing.v1.WatchWarehousesRequestKt
import com.acme.warehousing.v1.watchWarehousesRequest
import com.acme.yards.v1.ApproveYardMoveRequest
import com.acme.yards.v1.ApproveYardMoveResponse
import com.acme.yards.v1.YardMoveApprovalServiceGrpcKt
import com.acme.yards.v1.YardWorkflowServiceGrpcKt
import com.acme.yards.v1.advanceYardWorkflowRequest
import com.acme.yards.v1.approveYardMoveRequest
import com.acme.yards.v1.yard
import com.acme.yards.v1.yardDimensions
import com.acme.yards.v1.yardMove
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

/** Routes delivery exceptions through approval, with escalation and delegation. */
class DeliveryExceptionApprovalGrpcService(
    private val store: DeliveriesRepository,
    private val workers: CoroutineScope,
    private val deliveriesDeliveryApprovalService: DeliveryApprovalServiceGrpcKt.DeliveryApprovalServiceCoroutineStub,
    private val deliveriesDeliveryService: DeliveryServiceGrpcKt.DeliveryServiceCoroutineStub,
    private val deliveriesProofOfDeliveryApprovalService: ProofOfDeliveryApprovalServiceGrpcKt.ProofOfDeliveryApprovalServiceCoroutineStub,
    private val deliveriesProofOfDeliveryCalculationService: ProofOfDeliveryCalculationServiceGrpcKt.ProofOfDeliveryCalculationServiceCoroutineStub,
    private val deliveriesProofOfDeliveryValidationService: ProofOfDeliveryValidationServiceGrpcKt.ProofOfDeliveryValidationServiceCoroutineStub,
    private val driversDriverCalculationService: DriverCalculationServiceGrpcKt.DriverCalculationServiceCoroutineStub,
    private val fleetVehicleValidationService: VehicleValidationServiceGrpcKt.VehicleValidationServiceCoroutineStub,
    private val notificationsNotificationValidationService: NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub,
    private val searchSearchIndexValidationService: SearchIndexValidationServiceGrpcKt.SearchIndexValidationServiceCoroutineStub,
    private val warehousingWarehouseService: WarehouseServiceGrpcKt.WarehouseServiceCoroutineStub,
    private val yardsYardMoveApprovalService: YardMoveApprovalServiceGrpcKt.YardMoveApprovalServiceCoroutineStub,
    private val yardsYardWorkflowService: YardWorkflowServiceGrpcKt.YardWorkflowServiceCoroutineStub,
) : DeliveryExceptionApprovalServiceGrpcKt.DeliveryExceptionApprovalServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DeliveryExceptionApprovalGrpcService::class.java)

    private val workerForEscalateDeliveryException1 = ReplyMailbox<DelegateProofOfDeliveryApprovalRequest, DelegateProofOfDeliveryApprovalResponse>(workers) { payload ->
        withTimeout(5_000) { deliveriesProofOfDeliveryApprovalService.delegateProofOfDeliveryApproval(payload) }
    }

    /** Approves a delivery exception. */
    override suspend fun approveDeliveryException(request: ApproveDeliveryExceptionRequest): ApproveDeliveryExceptionResponse {
        return handleApproveDeliveryException(request)
    }

    /** Escalates a delivery exception. */
    override suspend fun escalateDeliveryException(request: EscalateDeliveryExceptionRequest): EscalateDeliveryExceptionResponse = handleEscalateDeliveryException(request)

    /** Returns a delivery exception. */
    override suspend fun getDeliveryExceptionApprovalStatus(request: GetDeliveryExceptionApprovalStatusRequest): GetDeliveryExceptionApprovalStatusResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryExceptionApprovalService/GetDeliveryExceptionApprovalStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetDeliveryExceptionApprovalStatus", request)
            log.atInfo().addKeyValue("operation", "GetDeliveryExceptionApprovalStatus").log("Handling transport request")
            val outbound0 = scoreProofOfDeliveryRequest {
                this.context = request.context
                this.proofOfDelivery = proofOfDelivery {
                    this.counterparty = driverRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = withTimeout(5_000) { deliveriesProofOfDeliveryValidationService.scoreProofOfDelivery(outbound0) }
            getDeliveryExceptionApprovalStatusResponse {
                this.deliveryException = deliveryException {
                    this.deliveryExceptionId = request.deliveryExceptionId
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    /** Recalls a delivery exception. */
    override suspend fun recallDeliveryException(request: RecallDeliveryExceptionRequest): RecallDeliveryExceptionResponse {
        return handleRecallDeliveryException(request)
    }

    /** Rejects a delivery exception. */
    override suspend fun rejectDeliveryException(request: RejectDeliveryExceptionRequest): RejectDeliveryExceptionResponse = handleRejectDeliveryException(request)

    /** Submits a delivery exception. */
    override suspend fun submitDeliveryException(request: SubmitDeliveryExceptionRequest): SubmitDeliveryExceptionResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryExceptionApprovalService/SubmitDeliveryException") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SubmitDeliveryException", request)
            log.atInfo().addKeyValue("operation", "SubmitDeliveryException").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.deliveryException.deliveryExceptionId).log("Recorded audit detail")
            val outbound0 = recalculateProofOfDeliveriesRequest {}.deliveriesCopy {
                this.context = request.context
                this.proofOfDelivery = proofOfDelivery {
                    this.audit = auditInfo {
                        this.updatedAt = request.deliveryException.audit.updatedAt
                        this.updatedBy = request.deliveryException.audit.updatedBy
                    }
                    this.counterparty = driverRef {
                        this.displayName = request.deliveryException.assignee.displayName
                        this.email = request.deliveryException.requester.email
                    }
                }
            }
            val reply0 = deliveriesProofOfDeliveryCalculationService.recalculateProofOfDeliveries(outbound0)
            val outbound1 = request.toNormalizeSearchIndexFromSubmitDeliveryException1(reply0)
            val reply1 = searchSearchIndexValidationService.normalizeSearchIndex(outbound1)
            val outbound2 = scoreNotificationRequest {
                this.context = request.context
                this.details = ScoreNotificationRequestKt.details {
                    this.destination = postalAddress {
                        this.postalCode = when (reply1.searchIndex.locatorCase) {
                            SearchIndex.LocatorCase.ADDRESS -> reply1.searchIndex.address.postalCode
                            else -> ""
                        }
                    }
                }
                this.notification = notification {
                    this.requester = contactInfo {
                        this.name = request.deliveryException.requester.name
                        this.preferredChannel = request.deliveryException.requester.preferredChannel
                    }
                }
            }
            val reply2 = notificationsNotificationValidationService.scoreNotification(outbound2)
            submitDeliveryExceptionResponse {
                this.deliveryException = deliveryException {
                    this.assignee = userRef {
                        this.displayName = reply0.proofOfDelivery.counterparty.displayName
                    }
                    this.deliveryExceptionId = request.deliveryExceptionId
                    this.requester = contactInfo {
                        this.email = reply1.searchIndex.owner.email
                    }
                    this.sla = deliveryExceptionSla {
                        this.pausedFor = reply2.notification.sla.pausedFor
                    }
                    this.status = request.deliveryException.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleApproveDeliveryException(request: ApproveDeliveryExceptionRequest): ApproveDeliveryExceptionResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryExceptionApprovalService/ApproveDeliveryException") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ApproveDeliveryException", request)
            log.atInfo().addKeyValue("operation", "ApproveDeliveryException").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.deliveryException.deliveryExceptionId).log("Recorded audit detail")
            val outbound0 = approveYardMoveRequest {
                this.context = request.context
                this.yardMove = yardMove {
                    this.assignee = userRef {
                        this.displayName = request.deliveryException.customer.displayName
                    }
                    this.audit = auditInfo {
                        this.createdAt = request.deliveryException.audit.createdAt
                    }
                    this.requester = contactInfo {
                        this.email = request.details.approver.email
                    }
                }
            }
            val invoke0: suspend (ApproveYardMoveRequest, Metadata) -> ApproveYardMoveResponse = yardsYardMoveApprovalService::approveYardMove
            val reply0 = invoke0(outbound0, Metadata())
            val outbound1 = ScoreVehicleRequest.newBuilder().apply {
                setContext(request.context)
                setVehicle(vehicle {
                    this.audit = auditInfo {
                        this.etag = reply0.yardMove.audit.etag
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> fleetVehicleValidationService.scoreVehicle(value) }
            val outbound2 = RequestFactory.create {
                commentDeliveryApprovalRequest {
                    this.context = request.context
                    this.delivery = delivery {
                        this.audit = auditInfo {
                            this.etag = reply1.vehicle.audit.etag
                        }
                        this.billingAddress = postalAddress {
                            this.region = reply1.vehicle.address.region
                        }
                    }
                    this.details = CommentDeliveryApprovalRequestKt.details {
                        this.approver = userRef {
                            this.displayName = request.deliveryException.assignee.displayName
                            this.email = reply1.vehicle.owner.email
                        }
                    }
                }
            }
            val reply2 = deliveriesDeliveryApprovalService.commentDeliveryApproval(outbound2)
            val outbound3 = advanceYardWorkflowRequest {
                this.context = request.context
                this.yard = yard {
                    this.audit = auditInfo {
                        this.etag = request.deliveryException.audit.etag
                    }
                    this.dimensions = yardDimensions {
                        this.width = reply1.vehicle.dimensions.width
                    }
                }
            }
            val reply3 = ClientRegistry.lookup("acme.yards.v1.YardWorkflowService", YardWorkflowServiceGrpcKt.YardWorkflowServiceCoroutineStub::class.java).advanceYardWorkflow(outbound3)
            approveDeliveryExceptionResponse {
                this.deliveryException = deliveryException {
                    this.audit = auditInfo {
                        this.createdBy = reply1.vehicle.audit.createdBy
                    }
                    this.comments.addAll(listOf(deliveryExceptionComment {
                        this.createdAt = reply1.vehicle.audit.createdAt
                    }))
                    this.customFields.putAll(request.deliveryException.customFieldsMap)
                    this.deliveryExceptionId = request.deliveryExceptionId
                    this.priority = reply2.delivery.counterparty.priority
                }
                this.outcome = ApproveDeliveryExceptionResponseKt.outcome {
                    this.effectiveAt = reply0.outcome.effectiveAt
                    this.nextSteps.addAll(reply3.outcome.nextStepsList)
                }
                this.result = operationResult {
                    this.message = reply3.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleEscalateDeliveryException(request: EscalateDeliveryExceptionRequest): EscalateDeliveryExceptionResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryExceptionApprovalService/EscalateDeliveryException") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EscalateDeliveryException", request)
            log.atInfo().addKeyValue("operation", "EscalateDeliveryException").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.deliveryException.deliveryExceptionId).log("Recorded audit detail")
            val outbound0 = listDeliveriesRequest {
                this.context = request.context
                this.filter = ListDeliveriesRequestKt.filter {
                    this.createdAfter = request.deliveryException.audit.createdAt
                    this.ownerId = request.deliveryException.customer.deliveryId
                }
            }
            val reply0 = deliveriesDeliveryService.listDeliveries(WireCopies.reflective(outbound0))
            val outbound1 = delegateProofOfDeliveryApprovalRequest {
                this.context = request.context
                this.details = DelegateProofOfDeliveryApprovalRequestKt.details {
                    this.approver = userRef {
                        this.displayName = request.deliveryException.customer.displayName
                    }
                }
            }
            val reply1 = workerForEscalateDeliveryException1.submit(outbound1)
            var reply2 = EstimateDriverResponse.getDefaultInstance()
            request.deliveryException.commentsList.asFlow().map { item ->
                val outbound2 = estimateDriverRequest {
                    this.context = request.context
                    this.driver = driver {
                        this.audit = auditInfo {
                            this.createdAt = item.createdAt
                        }
                        this.primaryAddress = postalAddress {
                            this.lines.addAll(reply1.proofOfDelivery.billingAddress.linesList)
                        }
                        this.relationshipManager = userRef {
                            this.email = item.authorEmail
                        }
                    }
                }
                driversDriverCalculationService.estimateDriver(outbound2)
            }.collect { reply2 = it }
            val outbound3 = escalateProofOfDeliveryRequest {
                this.context = request.context
                this.details = EscalateProofOfDeliveryRequestKt.details {
                    this.approver = userRef {
                        this.userId = request.details.approver.userId
                    }
                }
            }
            val reply3 = flowOf(outbound3).map { value -> deliveriesProofOfDeliveryApprovalService.escalateProofOfDelivery(value) }.single()
            escalateDeliveryExceptionResponse {
                this.deliveryException = deliveryException {
                    this.assignee = request.deliveryException.assignee
                    this.audit = auditInfo {
                        this.createdAt = reply1.proofOfDelivery.audit.createdAt
                    }
                    this.comments.addAll(listOf(deliveryExceptionComment {
                        this.createdAt = reply3.proofOfDelivery.audit.createdAt
                    }))
                    this.deliveryExceptionId = request.deliveryExceptionId
                }
                this.outcome = EscalateDeliveryExceptionResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleRecallDeliveryException(request: RecallDeliveryExceptionRequest): RecallDeliveryExceptionResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryExceptionApprovalService/RecallDeliveryException") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecallDeliveryException", request)
            log.atInfo().addKeyValue("operation", "RecallDeliveryException").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.deliveryException.deliveryExceptionId).log("Recorded audit detail")
            var reply0 = CalculateProofOfDeliveryResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<DeliveryExceptionStatusChange>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound0 = calculateProofOfDeliveryRequest {
                            this.context = request.context
                            this.proofOfDelivery = proofOfDelivery {
                                this.audit = auditInfo {
                                    this.createdBy = request.deliveryException.audit.createdBy
                                    this.updatedAt = item.changedAt
                                }
                                this.counterparty = driverRef {
                                    this.email = request.deliveryException.assignee.email
                                }
                            }
                        }
                        reply0 = deliveriesProofOfDeliveryCalculationService.calculateProofOfDelivery(outbound0)
                    }
                }
                try {
                    request.deliveryException.historyList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound1 = watchWarehousesRequest {
                this.context = request.context
                this.filter = WatchWarehousesRequestKt.filter {
                    this.region = reply0.proofOfDelivery.billingAddress.region
                }
                this.subscriber = contactInfo {
                    this.email = reply0.proofOfDelivery.counterparty.email
                    this.name = request.deliveryException.requester.name
                    this.phone = request.deliveryException.requester.phone
                }
            }
            warehousingWarehouseService.watchWarehouses(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            recallDeliveryExceptionResponse {
                this.deliveryException = deliveryException {
                    this.deliveryExceptionId = request.deliveryExceptionId
                }
                this.outcome = RecallDeliveryExceptionResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleRejectDeliveryException(request: RejectDeliveryExceptionRequest): RejectDeliveryExceptionResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryExceptionApprovalService/RejectDeliveryException") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RejectDeliveryException", request)
            log.atInfo().addKeyValue("operation", "RejectDeliveryException").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.deliveryException.deliveryExceptionId).log("Recorded audit detail")
            val outbound0 = validateSearchIndexRequest {
                this.context = request.context
                this.details = ValidateSearchIndexRequestKt.details {
                    this.contact = contactInfo {
                        this.name = request.deliveryException.requester.name
                    }
                }
                this.searchIndex = searchIndex {
                    this.audit = auditInfo {
                        this.createdAt = request.deliveryException.audit.createdAt
                    }
                    this.owner = userRef {
                        this.displayName = request.deliveryException.assignee.displayName
                        this.email = request.details.approver.email
                    }
                }
            }
            val reply0 = searchSearchIndexValidationService.validateSearchIndex(outbound0)
            rejectDeliveryExceptionResponse {
                this.deliveryException = deliveryException {
                    this.assignee = request.deliveryException.assignee
                    this.deliveryExceptionId = request.deliveryExceptionId
                }
                this.outcome = RejectDeliveryExceptionResponseKt.outcome {
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.message = reply0.outcome.message
                    this.status = "ok"
                }
            }
        }

    private fun SubmitDeliveryExceptionRequest.toNormalizeSearchIndexFromSubmitDeliveryException1(reply0: RecalculateProofOfDeliveriesResponse): NormalizeSearchIndexRequest {
        val source = this
        return normalizeSearchIndexRequest {
            this.context = source.context
            this.searchIndex = searchIndex {
                this.address = postalAddress {
                    this.lines.addAll(reply0.proofOfDelivery.billingAddress.linesList)
                }
                this.audit = auditInfo {
                    this.createdBy = reply0.proofOfDelivery.audit.createdBy
                }
                this.owner = userRef {
                    this.email = reply0.proofOfDelivery.counterparty.email
                }
            }
        }
    }
}
