package com.acme.transport.maintenance.grpc

import com.acme.appointments.v1.appointmentRef
import com.acme.authorization.v1.AccessPolicyServiceGrpcKt
import com.acme.authorization.v1.CreateAccessPolicyRequest
import com.acme.authorization.v1.accessPolicy
import com.acme.authorization.v1.createAccessPolicyRequest
import com.acme.careplans.v1.CarePlanServiceGrpcKt
import com.acme.careplans.v1.carePlan
import com.acme.careplans.v1.cloneCarePlanRequest
import com.acme.common.v1.PersonName
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.DeliveryApprovalServiceGrpcKt
import com.acme.deliveries.v1.delegateDeliveryApprovalRequest
import com.acme.deliveries.v1.delivery
import com.acme.documents.v1.BatchGetDocumentsRequest
import com.acme.documents.v1.DocumentServiceGrpcKt
import com.acme.documents.v1.batchGetDocumentsRequest
import com.acme.drivers.v1.DriverCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseQueryServiceGrpcKt
import com.acme.drivers.v1.DutyStatusCalculationServiceGrpcKt
import com.acme.drivers.v1.ExplainDriverCalculationRequestKt
import com.acme.drivers.v1.ExplainDutyStatusCalculationRequestKt
import com.acme.drivers.v1.ExplainDutyStatusCalculationResponse
import com.acme.drivers.v1.SuggestDriverLicensesRequestKt
import com.acme.drivers.v1.SuggestDriverLicensesResponse
import com.acme.drivers.v1.driverLicenseRef
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.dutyStatus
import com.acme.drivers.v1.dutyStatusMeasurement
import com.acme.drivers.v1.explainDriverCalculationRequest
import com.acme.drivers.v1.explainDutyStatusCalculationRequest
import com.acme.drivers.v1.suggestDriverLicensesRequest
import com.acme.featureflags.v1.FeatureFlagValidationServiceGrpcKt
import com.acme.featureflags.v1.NormalizeFeatureFlagRequest
import com.acme.featureflags.v1.NormalizeFeatureFlagRequestKt
import com.acme.featureflags.v1.NormalizeFeatureFlagResponse
import com.acme.featureflags.v1.featureFlag
import com.acme.featureflags.v1.normalizeFeatureFlagRequest
import com.acme.fuel.v1.AdvanceFuelPolicyWorkflowRequestKt
import com.acme.fuel.v1.AdvanceFuelPolicyWorkflowResponse
import com.acme.fuel.v1.CountFuelTransactionTotalsRequestKt
import com.acme.fuel.v1.FuelCardImportServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyWorkflowServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionReportingServiceGrpcKt
import com.acme.fuel.v1.MapFuelCardImportColumnsRequest
import com.acme.fuel.v1.MapFuelCardImportColumnsRequestKt
import com.acme.fuel.v1.MapFuelCardImportColumnsResponse
import com.acme.fuel.v1.advanceFuelPolicyWorkflowRequest
import com.acme.fuel.v1.copy as fuelCopy
import com.acme.fuel.v1.countFuelTransactionTotalsRequest
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.mapFuelCardImportColumnsRequest
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.ApproveWorkOrderRequest
import com.acme.maintenance.v1.ApproveWorkOrderResponse
import com.acme.maintenance.v1.ApproveWorkOrderResponseKt
import com.acme.maintenance.v1.CommentWorkOrderApprovalRequest
import com.acme.maintenance.v1.CommentWorkOrderApprovalResponse
import com.acme.maintenance.v1.CommentWorkOrderApprovalResponseKt
import com.acme.maintenance.v1.DelegateWorkOrderApprovalRequest
import com.acme.maintenance.v1.DelegateWorkOrderApprovalResponse
import com.acme.maintenance.v1.DelegateWorkOrderApprovalResponseKt
import com.acme.maintenance.v1.EscalateWorkOrderRequest
import com.acme.maintenance.v1.EscalateWorkOrderResponse
import com.acme.maintenance.v1.EscalateWorkOrderResponseKt
import com.acme.maintenance.v1.EstimatePartRequest
import com.acme.maintenance.v1.EstimatePartResponse
import com.acme.maintenance.v1.ExpireWorkOrderRequest
import com.acme.maintenance.v1.ExpireWorkOrderRequestKt
import com.acme.maintenance.v1.GetWorkOrderApprovalStatusRequest
import com.acme.maintenance.v1.GetWorkOrderApprovalStatusResponse
import com.acme.maintenance.v1.ListPendingWorkOrdersRequest
import com.acme.maintenance.v1.ListPendingWorkOrdersResponse
import com.acme.maintenance.v1.PartCalculationServiceGrpcKt
import com.acme.maintenance.v1.QuoteWorkOrderRequest
import com.acme.maintenance.v1.RecallWorkOrderRequest
import com.acme.maintenance.v1.RecallWorkOrderResponse
import com.acme.maintenance.v1.ServiceScheduleValidationServiceGrpcKt
import com.acme.maintenance.v1.SubmitWorkOrderRequest
import com.acme.maintenance.v1.SubmitWorkOrderResponse
import com.acme.maintenance.v1.SubmitWorkOrderResponseKt
import com.acme.maintenance.v1.WorkOrderApprovalServiceGrpcKt
import com.acme.maintenance.v1.WorkOrderCalculationServiceGrpcKt
import com.acme.maintenance.v1.WorkOrderComment
import com.acme.maintenance.v1.WorkOrderLifecycleServiceGrpcKt
import com.acme.maintenance.v1.WorkOrderServiceGrpcKt
import com.acme.maintenance.v1.approveWorkOrderResponse
import com.acme.maintenance.v1.commentWorkOrderApprovalResponse
import com.acme.maintenance.v1.delegateWorkOrderApprovalResponse
import com.acme.maintenance.v1.escalateWorkOrderResponse
import com.acme.maintenance.v1.estimatePartRequest
import com.acme.maintenance.v1.expireWorkOrderRequest
import com.acme.maintenance.v1.explainServiceScheduleDecisionRequest
import com.acme.maintenance.v1.getWorkOrderApprovalStatusResponse
import com.acme.maintenance.v1.getWorkOrderLifecycleRequest
import com.acme.maintenance.v1.getWorkOrderRequest
import com.acme.maintenance.v1.listPendingWorkOrdersResponse
import com.acme.maintenance.v1.part
import com.acme.maintenance.v1.partComponent
import com.acme.maintenance.v1.quoteWorkOrderRequest
import com.acme.maintenance.v1.recallWorkOrderResponse
import com.acme.maintenance.v1.serviceSchedule
import com.acme.maintenance.v1.serviceScheduleParticipant
import com.acme.maintenance.v1.simulatePartRequest
import com.acme.maintenance.v1.submitWorkOrderResponse
import com.acme.maintenance.v1.workOrder
import com.acme.maintenance.v1.workOrderSla
import com.acme.metrics.v1.LintMetricSeriesesRequest
import com.acme.metrics.v1.LintMetricSeriesesResponse
import com.acme.metrics.v1.MetricSeriesValidationServiceGrpcKt
import com.acme.metrics.v1.lintMetricSeriesesRequest
import com.acme.metrics.v1.metricSeries
import com.acme.subscriptions.v1.SubscriptionQueryServiceGrpcKt
import com.acme.subscriptions.v1.SuggestSubscriptionsRequestKt
import com.acme.subscriptions.v1.suggestSubscriptionsRequest
import com.acme.transport.maintenance.MaintenanceRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.google.protobuf.Any as ProtoAny
import com.google.protobuf.Timestamp
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Routes work orders through approval, with escalation and delegation. */
class WorkOrderApprovalEndpoint(
    private val store: MaintenanceRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyService: AccessPolicyServiceGrpcKt.AccessPolicyServiceCoroutineStub,
    private val careplansCarePlanService: CarePlanServiceGrpcKt.CarePlanServiceCoroutineStub,
    private val deliveriesDeliveryApprovalService: DeliveryApprovalServiceGrpcKt.DeliveryApprovalServiceCoroutineStub,
    private val documentsDocumentService: DocumentServiceGrpcKt.DocumentServiceCoroutineStub,
    private val driversDriverCalculationService: DriverCalculationServiceGrpcKt.DriverCalculationServiceCoroutineStub,
    private val driversDriverLicenseQueryService: DriverLicenseQueryServiceGrpcKt.DriverLicenseQueryServiceCoroutineStub,
    private val driversDutyStatusCalculationService: DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineStub,
    private val featureflagsFeatureFlagValidationService: FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub,
    private val fuelFuelCardImportService: FuelCardImportServiceGrpcKt.FuelCardImportServiceCoroutineStub,
    private val fuelFuelPolicyWorkflowService: FuelPolicyWorkflowServiceGrpcKt.FuelPolicyWorkflowServiceCoroutineStub,
    private val fuelFuelTransactionReportingService: FuelTransactionReportingServiceGrpcKt.FuelTransactionReportingServiceCoroutineStub,
    private val maintenancePartCalculationService: PartCalculationServiceGrpcKt.PartCalculationServiceCoroutineStub,
    private val maintenanceServiceScheduleValidationService: ServiceScheduleValidationServiceGrpcKt.ServiceScheduleValidationServiceCoroutineStub,
    private val maintenanceWorkOrderCalculationService: WorkOrderCalculationServiceGrpcKt.WorkOrderCalculationServiceCoroutineStub,
    private val maintenanceWorkOrderLifecycleService: WorkOrderLifecycleServiceGrpcKt.WorkOrderLifecycleServiceCoroutineStub,
    private val maintenanceWorkOrderService: WorkOrderServiceGrpcKt.WorkOrderServiceCoroutineStub,
    private val metricsMetricSeriesValidationService: MetricSeriesValidationServiceGrpcKt.MetricSeriesValidationServiceCoroutineStub,
    private val subscriptionsSubscriptionQueryService: SubscriptionQueryServiceGrpcKt.SubscriptionQueryServiceCoroutineStub,
) : WorkOrderApprovalServiceGrpcKt.WorkOrderApprovalServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(WorkOrderApprovalEndpoint::class.java)

    private val invokeEscalateWorkOrder1: suspend (EstimatePartRequest) -> EstimatePartResponse = { payload ->
        maintenancePartCalculationService.estimatePart(payload)
    }

    private val workerForSubmitWorkOrder0 = ReplyMailbox<NormalizeFeatureFlagRequest, NormalizeFeatureFlagResponse>(workers) { payload ->
        withTimeout(5_000) { featureflagsFeatureFlagValidationService.normalizeFeatureFlag(payload) }
    }

    /** Approves a work order. */
    override suspend fun approveWorkOrder(request: ApproveWorkOrderRequest): ApproveWorkOrderResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderApprovalService/ApproveWorkOrder") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ApproveWorkOrder", request)
            log.atInfo().addKeyValue("operation", "ApproveWorkOrder").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.workOrder.workOrderId).log("Recorded audit detail")
            var reply0 = SuggestDriverLicensesResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.workOrder.commentsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound0 = suggestDriverLicensesRequest {
                                this.context = request.context
                                this.filter = SuggestDriverLicensesRequestKt.filter {
                                    this.createdAfter = item.createdAt
                                    this.labels.putAll(request.workOrder.customFieldsMap)
                                    this.ownerId = item.commentId
                                }
                            }
                            driversDriverLicenseQueryService.suggestDriverLicenses(outbound0)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply0 = it }
                }
            }
            val outbound1 = explainDriverCalculationRequest {
                this.context = request.context
                this.details = ExplainDriverCalculationRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.workOrder.customer.email
                    }
                }
            }
            val reply1 = driversDriverCalculationService.explainDriverCalculation(outbound1)
            val outbound2 = countFuelTransactionTotalsRequest {}.fuelCopy {
                this.context = request.context
                this.filter = CountFuelTransactionTotalsRequestKt.filter {
                    this.region = reply1.driver.primaryAddress.region
                }
            }
            val reply2 = fuelFuelTransactionReportingService.countFuelTransactionTotals(outbound2)
            approveWorkOrderResponse {
                this.outcome = ApproveWorkOrderResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.message = reply1.result.message
                }
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.status = "ok"
                }
                this.workOrder = workOrder {
                    this.workOrderId = request.workOrderId
                }
            }
        }

    /** Comments a work order. */
    override suspend fun commentWorkOrderApproval(request: CommentWorkOrderApprovalRequest): CommentWorkOrderApprovalResponse {
        return handleCommentWorkOrderApproval(request)
    }

    /** Delegates a work order. */
    override suspend fun delegateWorkOrderApproval(request: DelegateWorkOrderApprovalRequest): DelegateWorkOrderApprovalResponse = handleDelegateWorkOrderApproval(request)

    /** Escalates a work order. */
    override suspend fun escalateWorkOrder(request: EscalateWorkOrderRequest): EscalateWorkOrderResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderApprovalService/EscalateWorkOrder") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EscalateWorkOrder", request)
            log.atInfo().addKeyValue("operation", "EscalateWorkOrder").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.workOrder.workOrderId).log("Recorded audit detail")
            val outbound0 = mapFuelCardImportColumnsRequest {
                this.context = request.context
                this.details = MapFuelCardImportColumnsRequestKt.details {
                    this.contact = contactInfo {
                        this.phone = request.workOrder.requester.phone
                    }
                }
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.etag = request.workOrder.audit.etag
                        this.updatedBy = request.workOrder.audit.updatedBy
                    }
                    this.holder = driverRef {
                        this.email = request.workOrder.requester.email
                    }
                }
            }
            val adapter0 = OutboundPort<MapFuelCardImportColumnsRequest, MapFuelCardImportColumnsResponse> { payload ->
                fuelFuelCardImportService.mapFuelCardImportColumns(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = estimatePartRequest {
                this.context = request.context
                this.part = part {
                    this.audit = auditInfo {
                        this.createdAt = request.workOrder.audit.createdAt
                        this.createdBy = request.workOrder.audit.createdBy
                    }
                    this.owner = userRef {
                        this.email = reply0.fuelCard.contact.email
                    }
                }
            }
            val reply1 = invokeEscalateWorkOrder1(outbound1)
            escalateWorkOrderResponse {
                this.outcome = EscalateWorkOrderResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.referenceIds.addAll((reply0.result.referenceIdsList) + (reply1.result.referenceIdsList))
                    this.status = "ok"
                }
                this.workOrder = workOrder {
                    this.assignee = userRef {
                        this.displayName = reply1.part.owner.displayName
                    }
                    this.workOrderId = request.workOrderId
                }
            }
        }

    /** Returns a work order. */
    override suspend fun getWorkOrderApprovalStatus(request: GetWorkOrderApprovalStatusRequest): GetWorkOrderApprovalStatusResponse {
        return handleGetWorkOrderApprovalStatus(request)
    }

    /** Lists work orders a page at a time. */
    override suspend fun listPendingWorkOrders(request: ListPendingWorkOrdersRequest): ListPendingWorkOrdersResponse = handleListPendingWorkOrders(request)

    /** Recalls a work order. */
    override suspend fun recallWorkOrder(request: RecallWorkOrderRequest): RecallWorkOrderResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderApprovalService/RecallWorkOrder") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecallWorkOrder", request)
            log.atInfo().addKeyValue("operation", "RecallWorkOrder").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.workOrder.workOrderId).log("Recorded audit detail")
            val outbound0 = QuoteWorkOrderRequest.newBuilder().apply {
                setContext(request.context)
                setWorkOrder(workOrder {
                    this.sla = workOrderSla {
                        this.breached = request.workOrder.sla.breached
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> maintenanceWorkOrderCalculationService.quoteWorkOrder(value) }
            val outbound1 = RequestFactory.create {
                delegateDeliveryApprovalRequest {
                    this.context = request.context
                    this.delivery = delivery {
                        this.audit = auditInfo {
                            this.createdBy = reply0.workOrder.audit.createdBy
                            this.updatedBy = reply0.workOrder.audit.updatedBy
                        }
                    }
                }
            }
            val reply1 = deliveriesDeliveryApprovalService.delegateDeliveryApproval(outbound1)
            val outbound2 = explainServiceScheduleDecisionRequest {
                this.context = request.context
                this.serviceSchedule = serviceSchedule {
                    this.participants.addAll(listOf(serviceScheduleParticipant {
                        this.contact = contactInfo {
                            this.email = request.details.approver.email
                            this.preferredChannel = reply0.workOrder.requester.preferredChannel
                        }
                    }))
                }
            }
            val reply2 = ClientRegistry.lookup("acme.maintenance.v1.ServiceScheduleValidationService", ServiceScheduleValidationServiceGrpcKt.ServiceScheduleValidationServiceCoroutineStub::class.java).explainServiceScheduleDecision(outbound2)
            val outbound3 = getWorkOrderRequest {
                this.context = request.context
                this.workOrderId = request.workOrderId
            }
            val reply3 = maintenanceWorkOrderService.getWorkOrder(WireCopies.reflective(outbound3))
            recallWorkOrderResponse {
                this.result = operationResult {
                    this.referenceIds.addAll(reply2.result.referenceIdsList)
                    this.status = reply3.result.status
                }
                this.workOrder = workOrder {
                    this.assignee = userRef {
                        this.email = reply0.workOrder.requester.email
                    }
                    this.audit = auditInfo {
                        this.createdBy = reply1.delivery.audit.createdBy
                        this.updatedAt = reply0.workOrder.audit.updatedAt
                    }
                    this.customFields.putAll(request.workOrder.customFieldsMap)
                    this.status = reply3.workOrder.status
                    this.workOrderId = request.workOrderId
                }
            }
        }

    /** Submits a work order. */
    override suspend fun submitWorkOrder(request: SubmitWorkOrderRequest): SubmitWorkOrderResponse {
        return handleSubmitWorkOrder(request)
    }

    private fun CommentWorkOrderApprovalRequest.toCreateAccessPolicyFromCommentWorkOrderApproval0(): CreateAccessPolicyRequest {
        val source = this
        return createAccessPolicyRequest {
            this.accessPolicy = accessPolicy {
                this.audit = auditInfo {
                    this.updatedAt = source.workOrder.audit.updatedAt
                }
                this.owner = userRef {
                    this.email = source.workOrder.customer.email
                }
            }
            this.context = source.context
        }
    }

    private suspend fun handleCommentWorkOrderApproval(request: CommentWorkOrderApprovalRequest): CommentWorkOrderApprovalResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderApprovalService/CommentWorkOrderApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CommentWorkOrderApproval", request)
            log.atInfo().addKeyValue("operation", "CommentWorkOrderApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.workOrder.workOrderId).log("Recorded audit detail")
            val outbound0 = request.toCreateAccessPolicyFromCommentWorkOrderApproval0()
            val reply0 = authorizationAccessPolicyService.createAccessPolicy(outbound0)
            val outbound1 = simulatePartRequest {
                this.context = request.context
                this.part = part {
                    this.audit = auditInfo {
                        this.createdBy = reply0.accessPolicy.audit.createdBy
                    }
                    this.components.putAll(mapOf("primary" to partComponent {
                        this.description = reply0.accessPolicy.description
                    }))
                    this.owner = userRef {
                        this.displayName = request.details.approver.displayName
                        this.email = request.details.approver.email
                    }
                }
            }
            val reply1 = maintenancePartCalculationService.simulatePart(outbound1)
            val outbound2 = batchGetDocumentsRequest {
                this.context = request.context
            }
            val envelope2 = ProtoAny.pack(outbound2)
            val reply2 = documentsDocumentService.batchGetDocuments(envelope2.unpack(BatchGetDocumentsRequest::class.java))
            val outbound3 = cloneCarePlanRequest {
                this.carePlan = carePlan {
                    this.billingAddress = postalAddress {
                        this.postalCode = reply1.part.address.postalCode
                        this.recipient = reply1.part.address.recipient
                        this.region = reply1.part.address.region
                    }
                    this.counterparty = appointmentRef {
                        this.displayName = request.workOrder.customer.displayName
                    }
                }
                this.context = request.context
            }
            val reply3 = careplansCarePlanService.cloneCarePlan(WireCopies.json(outbound3))
            commentWorkOrderApprovalResponse {
                this.outcome = CommentWorkOrderApprovalResponseKt.outcome {
                    this.message = reply2.result.message
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply2.result.message
                    this.status = "ok"
                }
                this.workOrder = workOrder {
                    this.assignee = userRef {
                        this.displayName = reply3.carePlan.counterparty.displayName
                        this.email = reply1.part.owner.email
                        this.userId = reply0.accessPolicy.owner.userId
                    }
                    this.subject = request.workOrder.subject
                    this.workOrderId = request.workOrderId
                }
            }
        }

    private data class DelegateWorkOrderApproval0Command(
        val context: RequestContext,
        val workOrderId: String,
        val detailsNewOwnerDisplayName: String,
        val detailsNotifyContactName: PersonName,
        val detailsNotifyContactEmail: String,
        val detailsEffectiveAt: Timestamp
    ) {
        fun toRequest(): ExpireWorkOrderRequest {
            val command = this
            return expireWorkOrderRequest {
                this.context = command.context
                this.details = ExpireWorkOrderRequestKt.details {
                    this.effectiveAt = command.detailsEffectiveAt
                    this.newOwner = userRef {
                        this.displayName = command.detailsNewOwnerDisplayName
                    }
                    this.notifyContact = contactInfo {
                        this.email = command.detailsNotifyContactEmail
                        this.name = command.detailsNotifyContactName
                    }
                }
                this.workOrderId = command.workOrderId
            }
        }
    }

    private suspend fun handleDelegateWorkOrderApproval(request: DelegateWorkOrderApprovalRequest): DelegateWorkOrderApprovalResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderApprovalService/DelegateWorkOrderApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DelegateWorkOrderApproval", request)
            log.atInfo().addKeyValue("operation", "DelegateWorkOrderApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.workOrder.subject).log("Recorded audit detail")
            val outbound0 = DelegateWorkOrderApproval0Command(
                context = request.context,
                workOrderId = request.workOrder.workOrderId,
                detailsNewOwnerDisplayName = request.workOrder.assignee.displayName,
                detailsNotifyContactName = request.workOrder.requester.name,
                detailsNotifyContactEmail = request.details.approver.email,
                detailsEffectiveAt = request.details.effectiveAt
            ).toRequest()
            val reply0 = maintenanceWorkOrderLifecycleService.expireWorkOrder(outbound0)
            var reply1 = ExplainDutyStatusCalculationResponse.getDefaultInstance()
            request.workOrder.historyList.asFlow().map { item ->
                val outbound1 = explainDutyStatusCalculationRequest {
                    this.context = request.context
                    this.details = ExplainDutyStatusCalculationRequestKt.details {
                        this.contact = contactInfo {
                            this.email = request.workOrder.customer.email
                            this.phone = reply0.workOrder.requester.phone
                        }
                    }
                    this.dutyStatus = dutyStatus {
                        this.measurements.addAll(listOf(dutyStatusMeasurement {
                            this.observedAt = item.changedAt
                        }))
                        this.subject = driverLicenseRef {
                            this.displayName = reply0.workOrder.assignee.displayName
                        }
                    }
                }
                driversDutyStatusCalculationService.explainDutyStatusCalculation(outbound1)
            }.collect { reply1 = it }
            delegateWorkOrderApprovalResponse {
                this.outcome = DelegateWorkOrderApprovalResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.workOrder = workOrder {
                    this.requester = contactInfo {
                        this.name = reply0.workOrder.requester.name
                    }
                    this.workOrderId = request.workOrderId
                }
            }
        }

    private suspend fun handleGetWorkOrderApprovalStatus(request: GetWorkOrderApprovalStatusRequest): GetWorkOrderApprovalStatusResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderApprovalService/GetWorkOrderApprovalStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetWorkOrderApprovalStatus", request)
            log.atInfo().addKeyValue("operation", "GetWorkOrderApprovalStatus").log("Handling transport request")
            val outbound0 = lintMetricSeriesesRequest {
                this.context = request.context
                this.metricSeries = metricSeries {
                    this.owner = userRef {
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val invoke0: suspend (LintMetricSeriesesRequest, Metadata) -> LintMetricSeriesesResponse = metricsMetricSeriesValidationService::lintMetricSerieses
            val reply0 = invoke0(outbound0, Metadata())
            getWorkOrderApprovalStatusResponse {
                this.result = operationResult {
                    this.status = reply0.result.status
                }
                this.workOrder = workOrder {
                    this.audit = auditInfo {
                        this.createdAt = reply0.metricSeries.audit.createdAt
                    }
                    this.workOrderId = request.workOrderId
                }
            }
        }

    private suspend fun handleListPendingWorkOrders(request: ListPendingWorkOrdersRequest): ListPendingWorkOrdersResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderApprovalService/ListPendingWorkOrders") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListPendingWorkOrders", request)
            log.atInfo().addKeyValue("operation", "ListPendingWorkOrders").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            listPendingWorkOrdersResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSubmitWorkOrder(request: SubmitWorkOrderRequest): SubmitWorkOrderResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderApprovalService/SubmitWorkOrder") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SubmitWorkOrder", request)
            log.atInfo().addKeyValue("operation", "SubmitWorkOrder").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.workOrder.subject).log("Recorded audit detail")
            val outbound0 = normalizeFeatureFlagRequest {
                this.context = request.context
                this.details = NormalizeFeatureFlagRequestKt.details {
                    this.contact = contactInfo {
                        this.preferredChannel = request.workOrder.requester.preferredChannel
                    }
                }
                this.featureFlag = featureFlag {
                    this.audit = auditInfo {
                        this.updatedAt = request.workOrder.audit.updatedAt
                    }
                    this.owner = userRef {
                        this.email = request.workOrder.requester.email
                    }
                }
            }
            val reply0 = workerForSubmitWorkOrder0.submit(outbound0)
            var reply1 = AdvanceFuelPolicyWorkflowResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<WorkOrderComment>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound1 = advanceFuelPolicyWorkflowRequest {
                            this.context = request.context
                            this.details = AdvanceFuelPolicyWorkflowRequestKt.details {
                                this.notify = contactInfo {
                                    this.email = item.authorEmail
                                }
                            }
                            this.fuelPolicy = fuelPolicy {
                                this.audit = auditInfo {
                                    this.createdAt = item.createdAt
                                    this.updatedBy = request.workOrder.audit.updatedBy
                                }
                            }
                        }
                        reply1 = fuelFuelPolicyWorkflowService.advanceFuelPolicyWorkflow(outbound1)
                    }
                }
                try {
                    request.workOrder.commentsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound2 = suggestSubscriptionsRequest {
                this.context = request.context
                this.filter = SuggestSubscriptionsRequestKt.filter {
                    this.createdAfter = request.workOrder.audit.updatedAt
                    this.labels.putAll(reply1.result.detailsMap)
                    this.ownerId = request.details.approver.userId
                }
            }
            val reply2 = flowOf(outbound2).map { value -> subscriptionsSubscriptionQueryService.suggestSubscriptions(value) }.single()
            val outbound3 = getWorkOrderLifecycleRequest {
                this.context = request.context
                this.workOrderId = request.workOrder.workOrderId
            }
            val reply3 = withTimeout(5_000) { maintenanceWorkOrderLifecycleService.getWorkOrderLifecycle(outbound3) }
            submitWorkOrderResponse {
                this.outcome = SubmitWorkOrderResponseKt.outcome {
                    this.message = reply0.outcome.message
                }
                this.result = operationResult {
                    this.message = reply2.result.message
                    this.referenceIds.addAll(reply2.result.referenceIdsList)
                    this.status = reply3.result.status
                }
                this.workOrder = workOrder {
                    this.requester = request.workOrder.requester
                    this.sla = workOrderSla {
                        this.policyId = reply3.workOrder.sla.policyId
                    }
                    this.workOrderId = request.workOrderId
                }
            }
        }
}
