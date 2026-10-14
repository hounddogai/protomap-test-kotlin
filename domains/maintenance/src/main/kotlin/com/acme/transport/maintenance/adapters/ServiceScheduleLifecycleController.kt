package com.acme.transport.maintenance.adapters

import com.acme.assets.v1.FixedAssetCalculationServiceGrpcKt
import com.acme.assets.v1.calculateFixedAssetRequest
import com.acme.assets.v1.fixedAsset
import com.acme.assets.v1.fixedAssetComponent
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.ApproveProofOfDeliveryRequest
import com.acme.deliveries.v1.ApproveProofOfDeliveryRequestKt
import com.acme.deliveries.v1.CancelProofOfDeliveryWorkflowRequestKt
import com.acme.deliveries.v1.ListProofOfDeliveryWorkflowsRequestKt
import com.acme.deliveries.v1.ProofOfDeliveryApprovalServiceGrpcKt
import com.acme.deliveries.v1.ProofOfDeliveryWorkflowServiceGrpcKt
import com.acme.deliveries.v1.approveProofOfDeliveryRequest
import com.acme.deliveries.v1.cancelProofOfDeliveryWorkflowRequest
import com.acme.deliveries.v1.listProofOfDeliveryWorkflowsRequest
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.drivers.v1.BatchGetDriversRequest
import com.acme.drivers.v1.CompareDutyStatusOptionsRequestKt
import com.acme.drivers.v1.DriverServiceGrpcKt
import com.acme.drivers.v1.DutyStatusCalculationServiceGrpcKt
import com.acme.drivers.v1.batchGetDriversRequest
import com.acme.drivers.v1.compareDutyStatusOptionsRequest
import com.acme.drivers.v1.driverLicenseRef
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.dutyStatus
import com.acme.identity.v1.userRef
import com.acme.localization.v1.CalculateTranslationKeyRequest
import com.acme.localization.v1.TranslationKeyCalculationServiceGrpcKt
import com.acme.localization.v1.calculateTranslationKeyRequest
import com.acme.localization.v1.translationKey
import com.acme.maintenance.v1.ActivateServiceScheduleRequest
import com.acme.maintenance.v1.ActivateServiceScheduleResponse
import com.acme.maintenance.v1.ActivateServiceScheduleResponseKt
import com.acme.maintenance.v1.ExplainServiceScheduleCalculationRequest
import com.acme.maintenance.v1.ExplainServiceScheduleCalculationResponse
import com.acme.maintenance.v1.ExplainServiceScheduleDecisionRequest
import com.acme.maintenance.v1.FindWorkOrdersRequest
import com.acme.maintenance.v1.FindWorkOrdersRequestKt
import com.acme.maintenance.v1.GetServiceScheduleLifecycleRequest
import com.acme.maintenance.v1.GetServiceScheduleLifecycleResponse
import com.acme.maintenance.v1.QuoteWorkOrderRequest
import com.acme.maintenance.v1.QuoteWorkOrderResponse
import com.acme.maintenance.v1.RecalculateWorkOrdersRequestKt
import com.acme.maintenance.v1.ReinstateServiceScheduleRequest
import com.acme.maintenance.v1.ReinstateServiceScheduleResponse
import com.acme.maintenance.v1.ReinstateServiceScheduleResponseKt
import com.acme.maintenance.v1.RenewServiceScheduleRequest
import com.acme.maintenance.v1.RenewServiceScheduleResponse
import com.acme.maintenance.v1.RenewServiceScheduleResponseKt
import com.acme.maintenance.v1.ScheduleServiceScheduleExpiryRequest
import com.acme.maintenance.v1.ScheduleServiceScheduleExpiryResponse
import com.acme.maintenance.v1.ServiceScheduleCalculationServiceGrpcKt
import com.acme.maintenance.v1.ServiceScheduleLifecycleServiceGrpcKt
import com.acme.maintenance.v1.ServiceScheduleValidationServiceGrpcKt
import com.acme.maintenance.v1.SimulateWorkOrderRequest
import com.acme.maintenance.v1.SimulateWorkOrderResponse
import com.acme.maintenance.v1.SuspendServiceScheduleRequest
import com.acme.maintenance.v1.SuspendServiceScheduleResponse
import com.acme.maintenance.v1.SuspendServiceScheduleResponseKt
import com.acme.maintenance.v1.TerminateServiceScheduleRequest
import com.acme.maintenance.v1.TerminateServiceScheduleResponse
import com.acme.maintenance.v1.TerminateServiceScheduleResponseKt
import com.acme.maintenance.v1.WorkOrderCalculationServiceGrpcKt
import com.acme.maintenance.v1.WorkOrderLifecycleServiceGrpcKt
import com.acme.maintenance.v1.WorkOrderQueryServiceGrpcKt
import com.acme.maintenance.v1.activateServiceScheduleResponse
import com.acme.maintenance.v1.explainServiceScheduleCalculationRequest
import com.acme.maintenance.v1.explainServiceScheduleDecisionRequest
import com.acme.maintenance.v1.findWorkOrdersRequest
import com.acme.maintenance.v1.getServiceScheduleLifecycleResponse
import com.acme.maintenance.v1.getServiceScheduleRulesRequest
import com.acme.maintenance.v1.quoteWorkOrderRequest
import com.acme.maintenance.v1.recalculateWorkOrdersRequest
import com.acme.maintenance.v1.reinstateServiceScheduleResponse
import com.acme.maintenance.v1.renewServiceScheduleResponse
import com.acme.maintenance.v1.scheduleServiceScheduleExpiryResponse
import com.acme.maintenance.v1.scheduleWorkOrderExpiryRequest
import com.acme.maintenance.v1.serviceSchedule
import com.acme.maintenance.v1.serviceScheduleParticipant
import com.acme.maintenance.v1.simulateWorkOrderRequest
import com.acme.maintenance.v1.suspendServiceScheduleResponse
import com.acme.maintenance.v1.terminateServiceScheduleResponse
import com.acme.maintenance.v1.workOrder
import com.acme.maintenance.v1.workOrderComment
import com.acme.metrics.v1.LintMetricSeriesesRequest
import com.acme.metrics.v1.LintMetricSeriesesRequestKt
import com.acme.metrics.v1.LintMetricSeriesesResponse
import com.acme.metrics.v1.MetricSeriesValidationServiceGrpcKt
import com.acme.metrics.v1.ScoreMetricSeriesRequestKt
import com.acme.metrics.v1.lintMetricSeriesesRequest
import com.acme.metrics.v1.metricSeries
import com.acme.metrics.v1.scoreMetricSeriesRequest
import com.acme.search.v1.CreateSearchIndexRequestKt
import com.acme.search.v1.SearchIndexServiceGrpcKt
import com.acme.search.v1.createSearchIndexRequest
import com.acme.search.v1.searchIndex
import com.acme.transport.maintenance.MaintenanceRepository
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.DockApprovalServiceGrpcKt
import com.acme.yards.v1.copy as yardsCopy
import com.acme.yards.v1.dock
import com.acme.yards.v1.escalateDockRequest
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Moves a service schedule between the states of its lifecycle. */
class ServiceScheduleLifecycleController(
    private val store: MaintenanceRepository,
    private val workers: CoroutineScope,
    private val assetsFixedAssetCalculationService: FixedAssetCalculationServiceGrpcKt.FixedAssetCalculationServiceCoroutineStub,
    private val deliveriesProofOfDeliveryApprovalService: ProofOfDeliveryApprovalServiceGrpcKt.ProofOfDeliveryApprovalServiceCoroutineStub,
    private val deliveriesProofOfDeliveryWorkflowService: ProofOfDeliveryWorkflowServiceGrpcKt.ProofOfDeliveryWorkflowServiceCoroutineStub,
    private val driversDriverService: DriverServiceGrpcKt.DriverServiceCoroutineStub,
    private val driversDutyStatusCalculationService: DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineStub,
    private val localizationTranslationKeyCalculationService: TranslationKeyCalculationServiceGrpcKt.TranslationKeyCalculationServiceCoroutineStub,
    private val maintenanceServiceScheduleCalculationService: ServiceScheduleCalculationServiceGrpcKt.ServiceScheduleCalculationServiceCoroutineStub,
    private val maintenanceServiceScheduleValidationService: ServiceScheduleValidationServiceGrpcKt.ServiceScheduleValidationServiceCoroutineStub,
    private val maintenanceWorkOrderCalculationService: WorkOrderCalculationServiceGrpcKt.WorkOrderCalculationServiceCoroutineStub,
    private val maintenanceWorkOrderLifecycleService: WorkOrderLifecycleServiceGrpcKt.WorkOrderLifecycleServiceCoroutineStub,
    private val maintenanceWorkOrderQueryService: WorkOrderQueryServiceGrpcKt.WorkOrderQueryServiceCoroutineStub,
    private val metricsMetricSeriesValidationService: MetricSeriesValidationServiceGrpcKt.MetricSeriesValidationServiceCoroutineStub,
    private val searchSearchIndexService: SearchIndexServiceGrpcKt.SearchIndexServiceCoroutineStub,
    private val yardsDockApprovalService: DockApprovalServiceGrpcKt.DockApprovalServiceCoroutineStub,
) : ServiceScheduleLifecycleServiceGrpcKt.ServiceScheduleLifecycleServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(ServiceScheduleLifecycleController::class.java)

    private val workerForActivateServiceSchedule0 = ReplyMailbox<QuoteWorkOrderRequest, QuoteWorkOrderResponse>(workers) { payload ->
        withTimeout(5_000) { maintenanceWorkOrderCalculationService.quoteWorkOrder(payload) }
    }

    private val clientForActivateServiceSchedule1 by lazy { deliveriesProofOfDeliveryWorkflowService }

    private val invokeSuspendServiceSchedule1: suspend (ExplainServiceScheduleCalculationRequest) -> ExplainServiceScheduleCalculationResponse = { payload ->
        maintenanceServiceScheduleCalculationService.explainServiceScheduleCalculation(payload)
    }

    /** Activates a service schedule. */
    override suspend fun activateServiceSchedule(request: ActivateServiceScheduleRequest): ActivateServiceScheduleResponse = handleActivateServiceSchedule(request)

    /** Returns a service schedule. */
    override suspend fun getServiceScheduleLifecycle(request: GetServiceScheduleLifecycleRequest): GetServiceScheduleLifecycleResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleLifecycleService/GetServiceScheduleLifecycle") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetServiceScheduleLifecycle", request)
            log.atInfo().addKeyValue("operation", "GetServiceScheduleLifecycle").log("Handling transport request")
            val outbound0 = calculateFixedAssetRequest {
                this.context = request.context
                this.fixedAsset = fixedAsset {
                    this.audit = auditInfo {
                        this.updatedAt = request.context.deadline
                    }
                    this.components.putAll(mapOf("primary" to fixedAssetComponent {
                        this.componentId = request.serviceScheduleId
                    }))
                    this.owner = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply0 = flowOf(outbound0).map { value -> assetsFixedAssetCalculationService.calculateFixedAsset(value) }.single()
            val outbound1 = recalculateWorkOrdersRequest {
                this.context = request.context
                this.details = RecalculateWorkOrdersRequestKt.details {
                    this.destination = postalAddress {
                        this.recipient = reply0.fixedAsset.address.recipient
                    }
                }
                this.workOrder = workOrder {
                    this.audit = auditInfo {
                        this.etag = reply0.fixedAsset.audit.etag
                    }
                }
            }
            val reply1 = withTimeout(5_000) { maintenanceWorkOrderCalculationService.recalculateWorkOrders(outbound1) }
            getServiceScheduleLifecycleResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.serviceSchedule = serviceSchedule {
                    this.audit = auditInfo {
                        this.etag = reply0.fixedAsset.audit.etag
                    }
                    this.organizer = userRef {
                        this.email = reply1.workOrder.assignee.email
                    }
                    this.serviceScheduleId = request.serviceScheduleId
                }
            }
        }

    /** Reinstates a service schedule. */
    override suspend fun reinstateServiceSchedule(request: ReinstateServiceScheduleRequest): ReinstateServiceScheduleResponse {
        return handleReinstateServiceSchedule(request)
    }

    /** Renews a service schedule. */
    override suspend fun renewServiceSchedule(request: RenewServiceScheduleRequest): RenewServiceScheduleResponse = handleRenewServiceSchedule(request)

    /** Schedules a service schedule. */
    override suspend fun scheduleServiceScheduleExpiry(request: ScheduleServiceScheduleExpiryRequest): ScheduleServiceScheduleExpiryResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleLifecycleService/ScheduleServiceScheduleExpiry") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScheduleServiceScheduleExpiry", request)
            log.atInfo().addKeyValue("operation", "ScheduleServiceScheduleExpiry").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.serviceSchedule.serviceScheduleId).log("Recorded audit detail")
            val outbound0 = ScheduleServiceScheduleExpiry0Command(
                context = request.context
            ).toRequest()
            val reply0 = driversDriverService.batchGetDrivers(outbound0)
            val outbound1 = approveProofOfDeliveryRequest {
                this.context = request.context
                this.details = ApproveProofOfDeliveryRequestKt.details {
                    this.approver = userRef {
                        this.displayName = request.serviceSchedule.organizer.displayName
                    }
                }
                this.proofOfDelivery = proofOfDelivery {
                    this.audit = auditInfo {
                        this.etag = request.serviceSchedule.audit.etag
                    }
                    this.billingAddress = postalAddress {
                        this.locality = request.serviceSchedule.location.locality
                    }
                }
            }
            val command1: Submission<ApproveProofOfDeliveryRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = deliveriesProofOfDeliveryApprovalService.approveProofOfDelivery(payload1)
            scheduleServiceScheduleExpiryResponse {
                this.result = operationResult {
                    this.status = reply0.result.status
                }
                this.serviceSchedule = serviceSchedule {
                    this.location = request.serviceSchedule.location
                    this.metadata.putAll(reply1.proofOfDelivery.metadataMap)
                    this.serviceScheduleId = request.serviceScheduleId
                }
            }
        }

    /** Suspends a service schedule. */
    override suspend fun suspendServiceSchedule(request: SuspendServiceScheduleRequest): SuspendServiceScheduleResponse {
        return handleSuspendServiceSchedule(request)
    }

    /** Terminates a service schedule. */
    override suspend fun terminateServiceSchedule(request: TerminateServiceScheduleRequest): TerminateServiceScheduleResponse = handleTerminateServiceSchedule(request)

    private suspend fun handleActivateServiceSchedule(request: ActivateServiceScheduleRequest): ActivateServiceScheduleResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleLifecycleService/ActivateServiceSchedule") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ActivateServiceSchedule", request)
            log.atInfo().addKeyValue("operation", "ActivateServiceSchedule").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = quoteWorkOrderRequest {
                this.context = request.context
                this.workOrder = workOrder {
                    this.assignee = userRef {
                        this.displayName = request.details.newOwner.displayName
                    }
                }
            }
            val reply0 = workerForActivateServiceSchedule0.submit(outbound0)
            val outbound1 = listProofOfDeliveryWorkflowsRequest {
                this.context = request.context
                this.filter = ListProofOfDeliveryWorkflowsRequestKt.filter {
                    this.createdAfter = reply0.workOrder.audit.createdAt
                }
            }
            val reply1 = clientForActivateServiceSchedule1.listProofOfDeliveryWorkflows(outbound1)
            activateServiceScheduleResponse {
                this.outcome = ActivateServiceScheduleResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = "ok"
                }
                this.serviceSchedule = serviceSchedule {
                    this.audit = auditInfo {
                        this.createdBy = reply0.workOrder.audit.createdBy
                    }
                    this.serviceScheduleId = request.serviceScheduleId
                }
            }
        }

    private suspend fun handleReinstateServiceSchedule(request: ReinstateServiceScheduleRequest): ReinstateServiceScheduleResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleLifecycleService/ReinstateServiceSchedule") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ReinstateServiceSchedule", request)
            log.atInfo().addKeyValue("operation", "ReinstateServiceSchedule").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val configure0: CreateSearchIndexRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.searchIndex = searchIndex {
                    this.owner = userRef {
                        this.displayName = request.details.newOwner.displayName
                        this.email = request.details.newOwner.email
                        this.userId = request.details.newOwner.userId
                    }
                }
            }
            val outbound0 = createSearchIndexRequest(configure0)
            val reply0 = searchSearchIndexService.createSearchIndex(outbound0)
            val outbound1 = compareDutyStatusOptionsRequest {
                this.context = request.context
                this.details = CompareDutyStatusOptionsRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = reply0.searchIndex.address.countryCode
                        this.postalCode = reply0.searchIndex.address.postalCode
                        this.region = reply0.searchIndex.address.region
                    }
                }
                this.dutyStatus = dutyStatus {
                    this.subject = driverLicenseRef {
                        this.displayName = request.details.newOwner.displayName
                    }
                }
            }
            val reply1 = driversDutyStatusCalculationService.compareDutyStatusOptions(outbound1)
            val outbound2 = getServiceScheduleRulesRequest {
                this.context = request.context
                this.serviceScheduleId = request.serviceScheduleId
            }
            val reply2 = maintenanceServiceScheduleValidationService.getServiceScheduleRules(outbound2)
            val outbound3 = escalateDockRequest {}.yardsCopy {
                this.context = request.context
                this.dock = dock {
                    this.address = postalAddress {
                        this.countryCode = reply2.serviceSchedule.location.countryCode
                    }
                    this.audit = auditInfo {
                        this.etag = reply2.serviceSchedule.audit.etag
                    }
                    this.owner = userRef {
                        this.displayName = request.details.newOwner.displayName
                    }
                }
            }
            val reply3 = yardsDockApprovalService.escalateDock(outbound3)
            reinstateServiceScheduleResponse {
                this.outcome = ReinstateServiceScheduleResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.correlationId = reply3.result.correlationId
                    this.message = reply1.result.message
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
                this.serviceSchedule = serviceSchedule {
                    this.audit = auditInfo {
                        this.createdBy = reply0.searchIndex.audit.createdBy
                    }
                    this.organizer = reply2.serviceSchedule.organizer
                    this.serviceScheduleId = request.serviceScheduleId
                }
            }
        }

    private fun RenewServiceScheduleRequest.toExplainServiceScheduleDecisionFromRenewServiceSchedule0(): ExplainServiceScheduleDecisionRequest {
        val source = this
        return explainServiceScheduleDecisionRequest {
            this.context = source.context
            this.serviceSchedule = serviceSchedule {
                this.participants.addAll(listOf(serviceScheduleParticipant {
                    this.contact = contactInfo {
                        this.email = source.details.notifyContact.email
                    }
                }))
            }
        }
    }

    private suspend fun handleRenewServiceSchedule(request: RenewServiceScheduleRequest): RenewServiceScheduleResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleLifecycleService/RenewServiceSchedule") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RenewServiceSchedule", request)
            log.atInfo().addKeyValue("operation", "RenewServiceSchedule").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = request.toExplainServiceScheduleDecisionFromRenewServiceSchedule0()
            val reply0 = maintenanceServiceScheduleValidationService.explainServiceScheduleDecision(outbound0)
            val outbound1 = scoreMetricSeriesRequest {
                this.context = request.context
                this.details = ScoreMetricSeriesRequestKt.details {
                    this.destination = postalAddress {
                        this.postalCode = reply0.serviceSchedule.location.postalCode
                    }
                }
                this.metricSeries = metricSeries {
                    this.address = postalAddress {
                        this.locality = reply0.serviceSchedule.location.locality
                    }
                }
            }
            val reply1 = metricsMetricSeriesValidationService.scoreMetricSeries(outbound1)
            val outbound2 = calculateTranslationKeyRequest {
                this.context = request.context
                this.translationKey = translationKey {
                    this.address = postalAddress {
                        this.recipient = reply0.serviceSchedule.location.recipient
                    }
                }
            }
            val envelope2 = ProtoAny.pack(outbound2)
            val reply2 = localizationTranslationKeyCalculationService.calculateTranslationKey(envelope2.unpack(CalculateTranslationKeyRequest::class.java))
            val outbound3 = scheduleWorkOrderExpiryRequest {
                this.context = request.context
                this.workOrder = workOrder {
                    this.assignee = userRef {
                        this.userId = reply1.metricSeries.owner.userId
                    }
                    this.comments.addAll(listOf(workOrderComment {
                        this.createdAt = reply2.translationKey.audit.createdAt
                    }))
                    this.requester = contactInfo {
                        this.email = request.details.notifyContact.email
                    }
                }
            }
            val reply3 = maintenanceWorkOrderLifecycleService.scheduleWorkOrderExpiry(WireCopies.json(outbound3))
            renewServiceScheduleResponse {
                this.outcome = RenewServiceScheduleResponseKt.outcome {
                    this.effectiveAt = reply3.outcome.effectiveAt
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
                this.serviceSchedule = serviceSchedule {
                    this.audit = auditInfo {
                        this.createdBy = reply3.workOrder.audit.createdBy
                        this.updatedBy = reply2.translationKey.audit.updatedBy
                    }
                    this.organizer = userRef {
                        this.email = reply2.translationKey.owner.email
                        this.userId = request.details.newOwner.userId
                    }
                    this.serviceScheduleId = request.serviceScheduleId
                }
            }
        }

    private data class ScheduleServiceScheduleExpiry0Command(
        val context: RequestContext
    ) {
        fun toRequest(): BatchGetDriversRequest {
            val command = this
            return batchGetDriversRequest {
                this.context = command.context
            }
        }
    }

    private suspend fun handleSuspendServiceSchedule(request: SuspendServiceScheduleRequest): SuspendServiceScheduleResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleLifecycleService/SuspendServiceSchedule") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SuspendServiceSchedule", request)
            log.atInfo().addKeyValue("operation", "SuspendServiceSchedule").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = lintMetricSeriesesRequest {
                this.context = request.context
                this.details = LintMetricSeriesesRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.details.notifyContact.email
                        this.name = request.details.notifyContact.name
                    }
                }
                this.metricSeries = metricSeries {
                    this.owner = userRef {
                        this.displayName = request.details.newOwner.displayName
                        this.userId = request.details.newOwner.userId
                    }
                }
            }
            val adapter0 = OutboundPort<LintMetricSeriesesRequest, LintMetricSeriesesResponse> { payload ->
                metricsMetricSeriesValidationService.lintMetricSerieses(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = explainServiceScheduleCalculationRequest {
                this.context = request.context
                this.serviceSchedule = serviceSchedule {
                    this.organizer = userRef {
                        this.displayName = reply0.metricSeries.owner.displayName
                    }
                    this.participants.addAll(listOf(serviceScheduleParticipant {
                        this.contact = contactInfo {
                            this.email = request.details.newOwner.email
                            this.phone = request.details.notifyContact.phone
                        }
                    }))
                }
            }
            val reply1 = invokeSuspendServiceSchedule1(outbound1)
            val outbound2 = simulateWorkOrderRequest {
                this.context = request.context
                this.workOrder = workOrder {
                    this.comments.addAll(listOf(workOrderComment {
                        this.createdAt = reply0.metricSeries.audit.createdAt
                    }))
                }
            }
            val invoke2: suspend (SimulateWorkOrderRequest, Metadata) -> SimulateWorkOrderResponse = maintenanceWorkOrderCalculationService::simulateWorkOrder
            val reply2 = invoke2(outbound2, Metadata())
            suspendServiceScheduleResponse {
                this.outcome = SuspendServiceScheduleResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
                this.serviceSchedule = serviceSchedule {
                    this.organizer = reply1.serviceSchedule.organizer
                    this.serviceScheduleId = request.serviceScheduleId
                    this.title = reply1.serviceSchedule.title
                }
            }
        }

    private suspend fun handleTerminateServiceSchedule(request: TerminateServiceScheduleRequest): TerminateServiceScheduleResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleLifecycleService/TerminateServiceSchedule") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "TerminateServiceSchedule", request)
            log.atInfo().addKeyValue("operation", "TerminateServiceSchedule").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = FindWorkOrdersRequest.newBuilder().apply {
                setContext(request.context)
                setFilter(FindWorkOrdersRequestKt.filter {
                    this.createdAfter = request.details.effectiveAt
                    this.ownerId = request.details.newOwner.userId
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> maintenanceWorkOrderQueryService.findWorkOrders(value) }
            val outbound1 = RequestFactory.create {
                cancelProofOfDeliveryWorkflowRequest {
                    this.context = request.context
                    this.details = CancelProofOfDeliveryWorkflowRequestKt.details {
                        this.notify = contactInfo {
                            this.name = request.details.notifyContact.name
                            this.phone = request.details.notifyContact.phone
                            this.preferredChannel = request.details.notifyContact.preferredChannel
                        }
                    }
                    this.proofOfDelivery = proofOfDelivery {
                        this.counterparty = driverRef {
                            this.email = request.details.notifyContact.email
                        }
                    }
                }
            }
            val reply1 = deliveriesProofOfDeliveryWorkflowService.cancelProofOfDeliveryWorkflow(outbound1)
            terminateServiceScheduleResponse {
                this.outcome = TerminateServiceScheduleResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.message = reply1.outcome.message
                    this.status = reply0.result.status
                }
                this.serviceSchedule = serviceSchedule {
                    this.serviceScheduleId = request.serviceScheduleId
                }
            }
        }
}
