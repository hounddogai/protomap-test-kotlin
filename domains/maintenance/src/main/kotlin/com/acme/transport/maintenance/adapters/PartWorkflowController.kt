package com.acme.transport.maintenance.adapters

import com.acme.audit.v1.AuditEventServiceGrpcKt
import com.acme.audit.v1.SearchAuditEventsRequestKt
import com.acme.audit.v1.searchAuditEventsRequest
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.DeliveryExceptionWorkflowServiceGrpcKt
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.deliveryExceptionSla
import com.acme.deliveries.v1.retryDeliveryExceptionWorkflowRequest
import com.acme.drivers.v1.DriverLicenseCalculationServiceGrpcKt
import com.acme.drivers.v1.compareDriverLicenseOptionsRequest
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverRef
import com.acme.fuel.v1.FuelCardWorkflowServiceGrpcKt
import com.acme.fuel.v1.PauseFuelCardWorkflowRequest
import com.acme.fuel.v1.PauseFuelCardWorkflowRequestKt
import com.acme.fuel.v1.PauseFuelCardWorkflowResponse
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.pauseFuelCardWorkflowRequest
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.AdvancePartWorkflowRequest
import com.acme.maintenance.v1.AdvancePartWorkflowResponse
import com.acme.maintenance.v1.AdvancePartWorkflowResponseKt
import com.acme.maintenance.v1.FindWorkOrdersRequestKt
import com.acme.maintenance.v1.GetPartApprovalStatusRequest
import com.acme.maintenance.v1.InspectPartWorkflowRequest
import com.acme.maintenance.v1.InspectPartWorkflowResponse
import com.acme.maintenance.v1.ListPartWorkflowsRequest
import com.acme.maintenance.v1.ListPartWorkflowsResponse
import com.acme.maintenance.v1.Part
import com.acme.maintenance.v1.PartApprovalServiceGrpcKt
import com.acme.maintenance.v1.PartCalculationServiceGrpcKt
import com.acme.maintenance.v1.PartComponent
import com.acme.maintenance.v1.PartReportingServiceGrpcKt
import com.acme.maintenance.v1.PartWorkflowServiceGrpcKt
import com.acme.maintenance.v1.PausePartWorkflowRequest
import com.acme.maintenance.v1.PausePartWorkflowResponse
import com.acme.maintenance.v1.PausePartWorkflowResponseKt
import com.acme.maintenance.v1.ResumePartWorkflowRequest
import com.acme.maintenance.v1.ResumePartWorkflowResponse
import com.acme.maintenance.v1.ResumeWorkOrderWorkflowRequestKt
import com.acme.maintenance.v1.RetryPartWorkflowRequest
import com.acme.maintenance.v1.RetryPartWorkflowResponse
import com.acme.maintenance.v1.SignalPartWorkflowRequest
import com.acme.maintenance.v1.SignalPartWorkflowResponse
import com.acme.maintenance.v1.SignalPartWorkflowResponseKt
import com.acme.maintenance.v1.SimulateWorkOrderRequestKt
import com.acme.maintenance.v1.StartPartWorkflowRequest
import com.acme.maintenance.v1.StartPartWorkflowResponse
import com.acme.maintenance.v1.WorkOrderCalculationServiceGrpcKt
import com.acme.maintenance.v1.WorkOrderQueryServiceGrpcKt
import com.acme.maintenance.v1.WorkOrderWorkflowServiceGrpcKt
import com.acme.maintenance.v1.advancePartWorkflowResponse
import com.acme.maintenance.v1.copy as maintenanceCopy
import com.acme.maintenance.v1.fetchPartReportRequest
import com.acme.maintenance.v1.findWorkOrdersRequest
import com.acme.maintenance.v1.getPartApprovalStatusRequest
import com.acme.maintenance.v1.getPartRatesRequest
import com.acme.maintenance.v1.inspectPartWorkflowResponse
import com.acme.maintenance.v1.listPartWorkflowsResponse
import com.acme.maintenance.v1.part
import com.acme.maintenance.v1.partDimensions
import com.acme.maintenance.v1.pausePartWorkflowResponse
import com.acme.maintenance.v1.resumePartWorkflowResponse
import com.acme.maintenance.v1.resumeWorkOrderWorkflowRequest
import com.acme.maintenance.v1.retryPartWorkflowResponse
import com.acme.maintenance.v1.signalPartWorkflowResponse
import com.acme.maintenance.v1.simulateWorkOrderRequest
import com.acme.maintenance.v1.startPartWorkflowResponse
import com.acme.maintenance.v1.workOrder
import com.acme.notifications.v1.ListNotificationsRequest
import com.acme.notifications.v1.ListNotificationsRequestKt
import com.acme.notifications.v1.NotificationServiceGrpcKt
import com.acme.notifications.v1.listNotificationsRequest
import com.acme.pipelines.v1.PipelineCalculationServiceGrpcKt
import com.acme.pipelines.v1.pipeline
import com.acme.pipelines.v1.pipelineParticipant
import com.acme.pipelines.v1.simulatePipelineRequest
import com.acme.ratelimiting.v1.ExportRateLimitsRequestKt
import com.acme.ratelimiting.v1.RateLimitQueryServiceGrpcKt
import com.acme.ratelimiting.v1.exportRateLimitsRequest
import com.acme.search.v1.SearchIndexValidationServiceGrpcKt
import com.acme.search.v1.searchIndex
import com.acme.search.v1.searchIndexComponent
import com.acme.search.v1.searchIndexDimensions
import com.acme.search.v1.validateSearchIndexRequest
import com.acme.transport.maintenance.MaintenanceRepository
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.google.protobuf.Any as ProtoAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Runs the long-lived workflow that takes a part from start to finish. */
class PartWorkflowController(
    private val store: MaintenanceRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventService: AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub,
    private val deliveriesDeliveryExceptionWorkflowService: DeliveryExceptionWorkflowServiceGrpcKt.DeliveryExceptionWorkflowServiceCoroutineStub,
    private val driversDriverLicenseCalculationService: DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub,
    private val fuelFuelCardWorkflowService: FuelCardWorkflowServiceGrpcKt.FuelCardWorkflowServiceCoroutineStub,
    private val maintenancePartApprovalService: PartApprovalServiceGrpcKt.PartApprovalServiceCoroutineStub,
    private val maintenancePartCalculationService: PartCalculationServiceGrpcKt.PartCalculationServiceCoroutineStub,
    private val maintenancePartReportingService: PartReportingServiceGrpcKt.PartReportingServiceCoroutineStub,
    private val maintenanceWorkOrderCalculationService: WorkOrderCalculationServiceGrpcKt.WorkOrderCalculationServiceCoroutineStub,
    private val maintenanceWorkOrderQueryService: WorkOrderQueryServiceGrpcKt.WorkOrderQueryServiceCoroutineStub,
    private val maintenanceWorkOrderWorkflowService: WorkOrderWorkflowServiceGrpcKt.WorkOrderWorkflowServiceCoroutineStub,
    private val notificationsNotificationService: NotificationServiceGrpcKt.NotificationServiceCoroutineStub,
    private val pipelinesPipelineCalculationService: PipelineCalculationServiceGrpcKt.PipelineCalculationServiceCoroutineStub,
    private val ratelimitingRateLimitQueryService: RateLimitQueryServiceGrpcKt.RateLimitQueryServiceCoroutineStub,
    private val searchSearchIndexValidationService: SearchIndexValidationServiceGrpcKt.SearchIndexValidationServiceCoroutineStub,
) : PartWorkflowServiceGrpcKt.PartWorkflowServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(PartWorkflowController::class.java)

    private val workerForAdvancePartWorkflow3 = ReplyMailbox<PauseFuelCardWorkflowRequest, PauseFuelCardWorkflowResponse>(workers) { payload ->
        withTimeout(5_000) { fuelFuelCardWorkflowService.pauseFuelCardWorkflow(payload) }
    }

    private val clientForListPartWorkflows0 by lazy { maintenanceWorkOrderCalculationService }

    /** Advances a part. */
    override suspend fun advancePartWorkflow(request: AdvancePartWorkflowRequest): AdvancePartWorkflowResponse = handleAdvancePartWorkflow(request)

    /** Returns a part. */
    override suspend fun inspectPartWorkflow(request: InspectPartWorkflowRequest): InspectPartWorkflowResponse =
        RpcBoundary.run("acme.maintenance.v1.PartWorkflowService/InspectPartWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "InspectPartWorkflow", request)
            log.atInfo().addKeyValue("operation", "InspectPartWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            inspectPartWorkflowResponse {
                this.part = part {
                    this.partId = request.partId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Lists parts a page at a time. */
    override suspend fun listPartWorkflows(request: ListPartWorkflowsRequest): ListPartWorkflowsResponse {
        return handleListPartWorkflows(request)
    }

    /** Pauses a part. */
    override suspend fun pausePartWorkflow(request: PausePartWorkflowRequest): PausePartWorkflowResponse = handlePausePartWorkflow(request)

    /** Resumes a part. */
    override suspend fun resumePartWorkflow(request: ResumePartWorkflowRequest): ResumePartWorkflowResponse =
        RpcBoundary.run("acme.maintenance.v1.PartWorkflowService/ResumePartWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResumePartWorkflow", request)
            log.atInfo().addKeyValue("operation", "ResumePartWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.part.partId).log("Recorded audit detail")
            val configure0: SearchAuditEventsRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.filter = SearchAuditEventsRequestKt.filter {
                    this.region = request.part.address.region
                }
            }
            val outbound0 = searchAuditEventsRequest(configure0)
            val reply0 = auditAuditEventService.searchAuditEvents(outbound0)
            val outbound1 = simulatePipelineRequest {
                this.context = request.context
                this.pipeline = pipeline {
                    this.audit = auditInfo {
                        this.etag = request.part.audit.etag
                    }
                    this.location = postalAddress {
                        this.recipient = request.part.address.recipient
                    }
                    this.participants.addAll(listOf(pipelineParticipant {
                        this.contact = contactInfo {
                            this.email = request.part.owner.email
                        }
                    }))
                }
            }
            val reply1 = pipelinesPipelineCalculationService.simulatePipeline(outbound1)
            resumePartWorkflowResponse {
                this.part = part {
                    this.capacity = request.part.capacity
                    this.partId = request.partId
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = reply1.result.status
                }
            }
        }

    /** Retrys a part. */
    override suspend fun retryPartWorkflow(request: RetryPartWorkflowRequest): RetryPartWorkflowResponse {
        return handleRetryPartWorkflow(request)
    }

    /** Signals a part. */
    override suspend fun signalPartWorkflow(request: SignalPartWorkflowRequest): SignalPartWorkflowResponse = handleSignalPartWorkflow(request)

    /** Starts a part. */
    override suspend fun startPartWorkflow(request: StartPartWorkflowRequest): StartPartWorkflowResponse =
        RpcBoundary.run("acme.maintenance.v1.PartWorkflowService/StartPartWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StartPartWorkflow", request)
            log.atInfo().addKeyValue("operation", "StartPartWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.part.partId).log("Recorded audit detail")
            val outbound0 = compareDriverLicenseOptionsRequest {
                this.context = request.context
                this.driverLicense = driverLicense {
                    this.audit = auditInfo {
                        this.createdAt = request.part.audit.createdAt
                    }
                    this.billingAddress = postalAddress {
                        this.postalCode = when (request.part.locatorCase) {
                            Part.LocatorCase.ADDRESS -> request.part.address.postalCode
                            else -> ""
                        }
                    }
                }
            }
            val reply0 = driversDriverLicenseCalculationService.compareDriverLicenseOptions(outbound0)
            val outbound1 = listNotificationsRequest {
                this.context = request.context
                this.filter = ListNotificationsRequestKt.filter {
                    this.createdAfter = request.part.audit.updatedAt
                    this.region = request.part.address.region
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = notificationsNotificationService.listNotifications(envelope1.unpack(ListNotificationsRequest::class.java))
            startPartWorkflowResponse {
                this.part = part {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.driverLicense.audit.updatedAt
                    }
                    this.code = request.part.code
                    this.partId = request.partId
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleAdvancePartWorkflow(request: AdvancePartWorkflowRequest): AdvancePartWorkflowResponse =
        RpcBoundary.run("acme.maintenance.v1.PartWorkflowService/AdvancePartWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "AdvancePartWorkflow", request)
            log.atInfo().addKeyValue("operation", "AdvancePartWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.part.code).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                getPartRatesRequest {
                    this.context = request.context
                    this.partId = request.part.partId
                }
            }
            val reply0 = maintenancePartCalculationService.getPartRates(outbound0)
            val outbound1 = exportRateLimitsRequest {
                this.context = request.context
                this.filter = ExportRateLimitsRequestKt.filter {
                    this.createdAfter = reply0.part.audit.updatedAt
                    this.labels.putAll(request.details.parametersMap)
                    this.ownerId = (request.part.componentsMap["primary"] ?: PartComponent.getDefaultInstance()).componentId
                    this.region = reply0.part.address.region
                }
            }
            ratelimitingRateLimitQueryService.exportRateLimits(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound2 = validateSearchIndexRequest {
                this.context = request.context
                this.searchIndex = searchIndex {
                    this.components.putAll(mapOf("primary" to searchIndexComponent {
                        this.componentId = (request.part.componentsMap["primary"] ?: PartComponent.getDefaultInstance()).componentId
                        this.quantity = (request.part.componentsMap["primary"] ?: PartComponent.getDefaultInstance()).quantity
                    }))
                    this.dimensions = searchIndexDimensions {
                        this.height = request.part.dimensions.height
                    }
                }
            }
            val reply2 = searchSearchIndexValidationService.validateSearchIndex(WireCopies.reflective(outbound2))
            val outbound3 = pauseFuelCardWorkflowRequest {
                this.context = request.context
                this.details = PauseFuelCardWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.preferredChannel = request.details.notify.preferredChannel
                    }
                }
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.etag = request.part.audit.etag
                        this.updatedBy = reply2.searchIndex.audit.updatedBy
                    }
                    this.holder = driverRef {
                        this.displayName = reply0.part.owner.displayName
                    }
                }
            }
            val reply3 = workerForAdvancePartWorkflow3.submit(outbound3)
            advancePartWorkflowResponse {
                this.outcome = AdvancePartWorkflowResponseKt.outcome {
                    this.nextSteps.addAll(reply3.outcome.nextStepsList)
                    this.status = reply2.outcome.status
                }
                this.part = part {
                    this.audit = auditInfo {
                        this.createdBy = reply2.searchIndex.audit.createdBy
                    }
                    this.code = reply0.part.code
                    this.owner = userRef {
                        this.userId = reply0.part.owner.userId
                    }
                    this.partId = request.partId
                    this.specs.putAll(request.part.specsMap)
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleListPartWorkflows(request: ListPartWorkflowsRequest): ListPartWorkflowsResponse =
        RpcBoundary.run("acme.maintenance.v1.PartWorkflowService/ListPartWorkflows") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListPartWorkflows", request)
            log.atInfo().addKeyValue("operation", "ListPartWorkflows").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = simulateWorkOrderRequest {
                this.context = request.context
                this.details = SimulateWorkOrderRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.filter.region
                    }
                }
                this.workOrder = workOrder {
                    this.assignee = userRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = clientForListPartWorkflows0.simulateWorkOrder(outbound0)
            val outbound1 = retryDeliveryExceptionWorkflowRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.sla = deliveryExceptionSla {
                        this.policyId = reply0.workOrder.sla.policyId
                    }
                }
            }
            val reply1 = flowOf(outbound1).map { value -> deliveriesDeliveryExceptionWorkflowService.retryDeliveryExceptionWorkflow(value) }.single()
            listPartWorkflowsResponse {
                this.parts.addAll(listOf(part {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.workOrder.audit.updatedBy
                    }
                }))
                this.result = operationResult {
                    this.message = reply1.outcome.message
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = reply1.result.status
                }
            }
        }

    private suspend fun handlePausePartWorkflow(request: PausePartWorkflowRequest): PausePartWorkflowResponse =
        RpcBoundary.run("acme.maintenance.v1.PartWorkflowService/PausePartWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PausePartWorkflow", request)
            log.atInfo().addKeyValue("operation", "PausePartWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.part.partId).log("Recorded audit detail")
            val outbound0 = resumeWorkOrderWorkflowRequest {
                this.context = request.context
                this.details = ResumeWorkOrderWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.email = request.part.owner.email
                        this.name = request.details.notify.name
                    }
                }
                this.workOrder = workOrder {
                    this.audit = auditInfo {
                        this.updatedBy = request.part.audit.updatedBy
                    }
                    this.customer = driverRef {
                        this.email = request.details.notify.email
                    }
                }
            }
            val reply0 = withTimeout(5_000) { maintenanceWorkOrderWorkflowService.resumeWorkOrderWorkflow(outbound0) }
            pausePartWorkflowResponse {
                this.outcome = PausePartWorkflowResponseKt.outcome {
                    this.effectiveAt = reply0.outcome.effectiveAt
                }
                this.part = part {
                    this.code = request.part.code
                    this.owner = userRef {
                        this.displayName = reply0.workOrder.customer.displayName
                    }
                    this.partId = request.partId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleRetryPartWorkflow(request: RetryPartWorkflowRequest): RetryPartWorkflowResponse =
        RpcBoundary.run("acme.maintenance.v1.PartWorkflowService/RetryPartWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RetryPartWorkflow", request)
            log.atInfo().addKeyValue("operation", "RetryPartWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.part.partId).log("Recorded audit detail")
            val outbound0 = fetchPartReportRequest {
                this.context = request.context
                this.partId = request.partId
            }
            val reply0 = maintenancePartReportingService.fetchPartReport(outbound0)
            retryPartWorkflowResponse {
                this.part = part {
                    this.dimensions = partDimensions {
                        this.width = reply0.part.dimensions.width
                    }
                    this.owner = userRef {
                        this.displayName = reply0.part.owner.displayName
                    }
                    this.partId = request.partId
                    this.status = request.part.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private fun SignalPartWorkflowRequest.toGetPartApprovalStatusFromSignalPartWorkflow1(): GetPartApprovalStatusRequest {
        val source = this
        return getPartApprovalStatusRequest {
            this.context = source.context
            this.partId = source.part.partId
        }
    }

    private suspend fun handleSignalPartWorkflow(request: SignalPartWorkflowRequest): SignalPartWorkflowResponse =
        RpcBoundary.run("acme.maintenance.v1.PartWorkflowService/SignalPartWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SignalPartWorkflow", request)
            log.atInfo().addKeyValue("operation", "SignalPartWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.part.code).log("Recorded audit detail")
            val outbound0 = findWorkOrdersRequest {}.maintenanceCopy {
                this.context = request.context
                this.filter = FindWorkOrdersRequestKt.filter {
                    this.region = request.part.address.region
                }
            }
            val reply0 = maintenanceWorkOrderQueryService.findWorkOrders(outbound0)
            val outbound1 = request.toGetPartApprovalStatusFromSignalPartWorkflow1()
            val reply1 = maintenancePartApprovalService.getPartApprovalStatus(outbound1)
            signalPartWorkflowResponse {
                this.outcome = SignalPartWorkflowResponseKt.outcome {
                    this.message = reply0.result.message
                }
                this.part = part {
                    this.capacity = request.part.capacity
                    this.dimensions = partDimensions {
                        this.weight = reply1.part.dimensions.weight
                    }
                    this.partId = request.partId
                }
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }
}
