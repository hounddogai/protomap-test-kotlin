package com.acme.transport.maintenance.grpc

import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.DeliveryApprovalServiceGrpcKt
import com.acme.deliveries.v1.SubmitDeliveryRequestKt
import com.acme.deliveries.v1.submitDeliveryRequest
import com.acme.drivers.v1.CalculateDriverLicenseRequestKt
import com.acme.drivers.v1.DriverLicenseCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverServiceGrpcKt
import com.acme.drivers.v1.DutyStatusCalculationServiceGrpcKt
import com.acme.drivers.v1.ExplainDutyStatusCalculationRequest
import com.acme.drivers.v1.ExplainDutyStatusCalculationRequestKt
import com.acme.drivers.v1.ExplainDutyStatusCalculationResponse
import com.acme.drivers.v1.ListDriversRequestKt
import com.acme.drivers.v1.calculateDriverLicenseRequest
import com.acme.drivers.v1.copy as driversCopy
import com.acme.drivers.v1.driverLicenseRef
import com.acme.drivers.v1.dutyStatus
import com.acme.drivers.v1.explainDutyStatusCalculationRequest
import com.acme.drivers.v1.listDriversRequest
import com.acme.identity.v1.PreviewUserRequestKt
import com.acme.identity.v1.PreviewUserResponse
import com.acme.identity.v1.UserValidationServiceGrpcKt
import com.acme.identity.v1.previewUserRequest
import com.acme.identity.v1.user
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.AdvanceWorkOrderWorkflowRequest
import com.acme.maintenance.v1.AdvanceWorkOrderWorkflowResponse
import com.acme.maintenance.v1.AdvanceWorkOrderWorkflowResponseKt
import com.acme.maintenance.v1.ApproveServiceScheduleRequest
import com.acme.maintenance.v1.DelegateServiceScheduleApprovalRequest
import com.acme.maintenance.v1.DelegateServiceScheduleApprovalResponse
import com.acme.maintenance.v1.InspectWorkOrderWorkflowRequest
import com.acme.maintenance.v1.InspectWorkOrderWorkflowResponse
import com.acme.maintenance.v1.ListWorkOrderWorkflowsRequest
import com.acme.maintenance.v1.ListWorkOrderWorkflowsResponse
import com.acme.maintenance.v1.PauseWorkOrderWorkflowRequest
import com.acme.maintenance.v1.PauseWorkOrderWorkflowResponse
import com.acme.maintenance.v1.ResumeWorkOrderWorkflowRequest
import com.acme.maintenance.v1.ResumeWorkOrderWorkflowResponse
import com.acme.maintenance.v1.ResumeWorkOrderWorkflowResponseKt
import com.acme.maintenance.v1.RetryWorkOrderWorkflowRequest
import com.acme.maintenance.v1.RetryWorkOrderWorkflowResponse
import com.acme.maintenance.v1.ScheduleWorkOrderExpiryResponse
import com.acme.maintenance.v1.ServiceScheduleApprovalServiceGrpcKt
import com.acme.maintenance.v1.SignalWorkOrderWorkflowRequest
import com.acme.maintenance.v1.SignalWorkOrderWorkflowResponse
import com.acme.maintenance.v1.StartWorkOrderWorkflowRequest
import com.acme.maintenance.v1.StartWorkOrderWorkflowResponse
import com.acme.maintenance.v1.StartWorkOrderWorkflowResponseKt
import com.acme.maintenance.v1.WorkOrder
import com.acme.maintenance.v1.WorkOrderApprovalServiceGrpcKt
import com.acme.maintenance.v1.WorkOrderComment
import com.acme.maintenance.v1.WorkOrderLifecycleServiceGrpcKt
import com.acme.maintenance.v1.WorkOrderWorkflowServiceGrpcKt
import com.acme.maintenance.v1.advanceWorkOrderWorkflowResponse
import com.acme.maintenance.v1.approveServiceScheduleRequest
import com.acme.maintenance.v1.approveWorkOrderRequest
import com.acme.maintenance.v1.delegateServiceScheduleApprovalRequest
import com.acme.maintenance.v1.inspectWorkOrderWorkflowResponse
import com.acme.maintenance.v1.listWorkOrderWorkflowsResponse
import com.acme.maintenance.v1.pauseWorkOrderWorkflowResponse
import com.acme.maintenance.v1.resumeWorkOrderWorkflowResponse
import com.acme.maintenance.v1.retryWorkOrderWorkflowResponse
import com.acme.maintenance.v1.scheduleWorkOrderExpiryRequest
import com.acme.maintenance.v1.serviceSchedule
import com.acme.maintenance.v1.serviceScheduleParticipant
import com.acme.maintenance.v1.signalWorkOrderWorkflowResponse
import com.acme.maintenance.v1.startWorkOrderWorkflowResponse
import com.acme.maintenance.v1.workOrder
import com.acme.maintenance.v1.workOrderSla
import com.acme.maintenance.v1.workOrderStatusChange
import com.acme.metrics.v1.MetricSeriesQueryServiceGrpcKt
import com.acme.metrics.v1.SummarizeMetricSeriesesRequestKt
import com.acme.metrics.v1.summarizeMetricSeriesesRequest
import com.acme.notifications.v1.ListNotificationsRequestKt
import com.acme.notifications.v1.NormalizeNotificationRequestKt
import com.acme.notifications.v1.NotificationServiceGrpcKt
import com.acme.notifications.v1.NotificationValidationServiceGrpcKt
import com.acme.notifications.v1.ValidateNotificationRequest
import com.acme.notifications.v1.ValidateNotificationResponse
import com.acme.notifications.v1.listNotificationsRequest
import com.acme.notifications.v1.normalizeNotificationRequest
import com.acme.notifications.v1.notification
import com.acme.notifications.v1.notificationSla
import com.acme.notifications.v1.validateNotificationRequest
import com.acme.search.v1.SearchIndexQueryServiceGrpcKt
import com.acme.search.v1.SummarizeSearchIndexesRequestKt
import com.acme.search.v1.summarizeSearchIndexesRequest
import com.acme.surveys.v1.SurveyServiceGrpcKt
import com.acme.surveys.v1.watchSurveysRequest
import com.acme.transport.maintenance.MaintenanceRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.YardMoveWorkflowServiceGrpcKt
import com.acme.yards.v1.startYardMoveWorkflowRequest
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

/** Runs the long-lived workflow that takes a work order from start to finish. */
class WorkOrderWorkflowEndpoint(
    private val store: MaintenanceRepository,
    private val workers: CoroutineScope,
    private val deliveriesDeliveryApprovalService: DeliveryApprovalServiceGrpcKt.DeliveryApprovalServiceCoroutineStub,
    private val driversDriverLicenseCalculationService: DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub,
    private val driversDriverService: DriverServiceGrpcKt.DriverServiceCoroutineStub,
    private val driversDutyStatusCalculationService: DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineStub,
    private val identityUserValidationService: UserValidationServiceGrpcKt.UserValidationServiceCoroutineStub,
    private val maintenanceServiceScheduleApprovalService: ServiceScheduleApprovalServiceGrpcKt.ServiceScheduleApprovalServiceCoroutineStub,
    private val maintenanceWorkOrderApprovalService: WorkOrderApprovalServiceGrpcKt.WorkOrderApprovalServiceCoroutineStub,
    private val maintenanceWorkOrderLifecycleService: WorkOrderLifecycleServiceGrpcKt.WorkOrderLifecycleServiceCoroutineStub,
    private val metricsMetricSeriesQueryService: MetricSeriesQueryServiceGrpcKt.MetricSeriesQueryServiceCoroutineStub,
    private val notificationsNotificationService: NotificationServiceGrpcKt.NotificationServiceCoroutineStub,
    private val notificationsNotificationValidationService: NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub,
    private val searchSearchIndexQueryService: SearchIndexQueryServiceGrpcKt.SearchIndexQueryServiceCoroutineStub,
    private val surveysSurveyService: SurveyServiceGrpcKt.SurveyServiceCoroutineStub,
    private val yardsYardMoveWorkflowService: YardMoveWorkflowServiceGrpcKt.YardMoveWorkflowServiceCoroutineStub,
) : WorkOrderWorkflowServiceGrpcKt.WorkOrderWorkflowServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(WorkOrderWorkflowEndpoint::class.java)

    private val invokeAdvanceWorkOrderWorkflow1: suspend (ValidateNotificationRequest) -> ValidateNotificationResponse = { payload ->
        notificationsNotificationValidationService.validateNotification(payload)
    }

    private val clientForResumeWorkOrderWorkflow0 by lazy { yardsYardMoveWorkflowService }

    /** Advances a work order. */
    override suspend fun advanceWorkOrderWorkflow(request: AdvanceWorkOrderWorkflowRequest): AdvanceWorkOrderWorkflowResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderWorkflowService/AdvanceWorkOrderWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "AdvanceWorkOrderWorkflow", request)
            log.atInfo().addKeyValue("operation", "AdvanceWorkOrderWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.workOrder.workOrderId).log("Recorded audit detail")
            val outbound0 = delegateServiceScheduleApprovalRequest {
                this.context = request.context
                this.serviceSchedule = serviceSchedule {
                    this.audit = auditInfo {
                        this.updatedAt = request.workOrder.audit.updatedAt
                    }
                    this.participants.addAll(listOf(serviceScheduleParticipant {
                        this.contact = contactInfo {
                            this.email = request.workOrder.requester.email
                            this.name = request.details.notify.name
                            this.phone = request.details.notify.phone
                        }
                    }))
                }
            }
            val adapter0 = OutboundPort<DelegateServiceScheduleApprovalRequest, DelegateServiceScheduleApprovalResponse> { payload ->
                maintenanceServiceScheduleApprovalService.delegateServiceScheduleApproval(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = validateNotificationRequest {
                this.context = request.context
                this.notification = notification {
                    this.assignee = userRef {
                        this.displayName = reply0.serviceSchedule.organizer.displayName
                    }
                    this.requester = contactInfo {
                        this.preferredChannel = request.details.notify.preferredChannel
                    }
                    this.sla = notificationSla {
                        this.pausedFor = request.workOrder.sla.pausedFor
                    }
                }
            }
            val reply1 = invokeAdvanceWorkOrderWorkflow1(outbound1)
            val outbound2 = explainDutyStatusCalculationRequest {
                this.context = request.context
                this.details = ExplainDutyStatusCalculationRequestKt.details {
                    this.contact = contactInfo {
                        this.email = reply1.notification.requester.email
                        this.phone = request.details.notify.phone
                    }
                }
                this.dutyStatus = dutyStatus {
                    this.subject = driverLicenseRef {
                        this.displayName = request.workOrder.customer.displayName
                        this.number = reply1.notification.customer.number
                    }
                }
            }
            val invoke2: suspend (ExplainDutyStatusCalculationRequest, Metadata) -> ExplainDutyStatusCalculationResponse = driversDutyStatusCalculationService::explainDutyStatusCalculation
            val reply2 = invoke2(outbound2, Metadata())
            val outbound3 = ApproveServiceScheduleRequest.newBuilder().apply {
                setContext(request.context)
                setServiceSchedule(serviceSchedule {
                    this.participants.addAll(listOf(serviceScheduleParticipant {
                        this.contact = contactInfo {
                            this.email = reply1.notification.assignee.email
                            this.phone = request.details.notify.phone
                        }
                    }))
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply3 = outbound3.let { value -> maintenanceServiceScheduleApprovalService.approveServiceSchedule(value) }
            advanceWorkOrderWorkflowResponse {
                this.outcome = AdvanceWorkOrderWorkflowResponseKt.outcome {
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.correlationId = reply2.dutyStatus.correlationId
                    this.status = reply2.result.status
                }
                this.workOrder = workOrder {
                    this.audit = auditInfo {
                        this.createdBy = reply3.serviceSchedule.audit.createdBy
                    }
                    this.requester = request.workOrder.requester
                    this.sla = workOrderSla {
                        this.pausedFor = reply1.notification.sla.pausedFor
                    }
                    this.workOrderId = request.workOrderId
                }
            }
        }

    /** Returns a work order. */
    override suspend fun inspectWorkOrderWorkflow(request: InspectWorkOrderWorkflowRequest): InspectWorkOrderWorkflowResponse {
        return handleInspectWorkOrderWorkflow(request)
    }

    /** Lists work orders a page at a time. */
    override suspend fun listWorkOrderWorkflows(request: ListWorkOrderWorkflowsRequest): ListWorkOrderWorkflowsResponse = handleListWorkOrderWorkflows(request)

    /** Pauses a work order. */
    override suspend fun pauseWorkOrderWorkflow(request: PauseWorkOrderWorkflowRequest): PauseWorkOrderWorkflowResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderWorkflowService/PauseWorkOrderWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PauseWorkOrderWorkflow", request)
            log.atInfo().addKeyValue("operation", "PauseWorkOrderWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.workOrder.workOrderId).log("Recorded audit detail")
            var reply0 = ScheduleWorkOrderExpiryResponse.getDefaultInstance()
            request.workOrder.historyList.asFlow().map { item ->
                val outbound0 = scheduleWorkOrderExpiryRequest {
                    this.context = request.context
                    this.workOrder = workOrder {
                        this.history.addAll(listOf(workOrderStatusChange {
                            this.changedAt = item.changedAt
                            this.changedBy = item.changedBy
                            this.fromStatus = item.fromStatus
                            this.toStatus = item.toStatus
                        }))
                    }
                }
                maintenanceWorkOrderLifecycleService.scheduleWorkOrderExpiry(outbound0)
            }.collect { reply0 = it }
            pauseWorkOrderWorkflowResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.workOrder = workOrder {
                    this.customFields.putAll(request.workOrder.customFieldsMap)
                    this.workOrderId = request.workOrderId
                }
            }
        }

    /** Resumes a work order. */
    override suspend fun resumeWorkOrderWorkflow(request: ResumeWorkOrderWorkflowRequest): ResumeWorkOrderWorkflowResponse {
        return handleResumeWorkOrderWorkflow(request)
    }

    /** Retrys a work order. */
    override suspend fun retryWorkOrderWorkflow(request: RetryWorkOrderWorkflowRequest): RetryWorkOrderWorkflowResponse = handleRetryWorkOrderWorkflow(request)

    /** Signals a work order. */
    override suspend fun signalWorkOrderWorkflow(request: SignalWorkOrderWorkflowRequest): SignalWorkOrderWorkflowResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderWorkflowService/SignalWorkOrderWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SignalWorkOrderWorkflow", request)
            log.atInfo().addKeyValue("operation", "SignalWorkOrderWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.workOrder.workOrderId).log("Recorded audit detail")
            var reply0 = PreviewUserResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<WorkOrderComment>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound0 = previewUserRequest {
                            this.context = request.context
                            this.details = PreviewUserRequestKt.details {
                                this.contact = contactInfo {
                                    this.preferredChannel = request.details.notify.preferredChannel
                                }
                            }
                            this.user = user {
                                this.audit = auditInfo {
                                    this.createdAt = item.createdAt
                                    this.etag = request.workOrder.audit.etag
                                }
                                this.contact = contactInfo {
                                    this.email = item.authorEmail
                                }
                            }
                        }
                        reply0 = identityUserValidationService.previewUser(outbound0)
                    }
                }
                try {
                    request.workOrder.commentsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound1 = approveWorkOrderRequest {
                this.context = request.context
                this.workOrder = workOrder {
                    this.audit = auditInfo {
                        this.updatedBy = request.workOrder.audit.updatedBy
                    }
                }
            }
            val reply1 = maintenanceWorkOrderApprovalService.approveWorkOrder(outbound1)
            signalWorkOrderWorkflowResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.workOrder = workOrder {
                    this.customFields.putAll(request.workOrder.customFieldsMap)
                    this.sla = workOrderSla {
                        this.pausedFor = reply1.workOrder.sla.pausedFor
                    }
                    this.workOrderId = request.workOrderId
                }
            }
        }

    /** Starts a work order. */
    override suspend fun startWorkOrderWorkflow(request: StartWorkOrderWorkflowRequest): StartWorkOrderWorkflowResponse {
        return handleStartWorkOrderWorkflow(request)
    }

    private suspend fun handleInspectWorkOrderWorkflow(request: InspectWorkOrderWorkflowRequest): InspectWorkOrderWorkflowResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderWorkflowService/InspectWorkOrderWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "InspectWorkOrderWorkflow", request)
            log.atInfo().addKeyValue("operation", "InspectWorkOrderWorkflow").log("Handling transport request")
            val outbound0 = RequestFactory.create {
                delegateServiceScheduleApprovalRequest {
                    this.context = request.context
                    this.serviceSchedule = serviceSchedule {
                        this.participants.addAll(listOf(serviceScheduleParticipant {
                            this.contact = contactInfo {
                                this.email = request.context.actor.email
                            }
                        }))
                    }
                }
            }
            val reply0 = maintenanceServiceScheduleApprovalService.delegateServiceScheduleApproval(outbound0)
            inspectWorkOrderWorkflowResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
                this.workOrder = workOrder {
                    this.workOrderId = request.workOrderId
                }
            }
        }

    private suspend fun handleListWorkOrderWorkflows(request: ListWorkOrderWorkflowsRequest): ListWorkOrderWorkflowsResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderWorkflowService/ListWorkOrderWorkflows") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListWorkOrderWorkflows", request)
            log.atInfo().addKeyValue("operation", "ListWorkOrderWorkflows").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = calculateDriverLicenseRequest {
                this.context = request.context
                this.details = CalculateDriverLicenseRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.filter.region
                    }
                }
            }
            val reply0 = ClientRegistry.lookup("acme.drivers.v1.DriverLicenseCalculationService", DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub::class.java).calculateDriverLicense(outbound0)
            val outbound1 = listNotificationsRequest {
                this.context = request.context
                this.filter = ListNotificationsRequestKt.filter {
                    this.query = request.filter.query
                }
            }
            val reply1 = notificationsNotificationService.listNotifications(WireCopies.reflective(outbound1))
            listWorkOrderWorkflowsResponse {
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = "ok"
                }
                this.workOrders.addAll(listOf(workOrder {
                    this.assignee = userRef {
                        this.displayName = reply0.driverLicense.counterparty.displayName
                    }
                }))
            }
        }

    private suspend fun handleResumeWorkOrderWorkflow(request: ResumeWorkOrderWorkflowRequest): ResumeWorkOrderWorkflowResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderWorkflowService/ResumeWorkOrderWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResumeWorkOrderWorkflow", request)
            log.atInfo().addKeyValue("operation", "ResumeWorkOrderWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.workOrder.workOrderId).log("Recorded audit detail")
            val outbound0 = startYardMoveWorkflowRequest {
                this.context = request.context
                this.yardMove = yardMove {
                    this.audit = auditInfo {
                        this.updatedAt = request.workOrder.audit.updatedAt
                        this.updatedBy = request.workOrder.audit.updatedBy
                    }
                }
            }
            val reply0 = clientForResumeWorkOrderWorkflow0.startYardMoveWorkflow(outbound0)
            val outbound1 = summarizeSearchIndexesRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.details = SummarizeSearchIndexesRequestKt.details {
                    this.filters.putAll(request.details.parametersMap)
                }
                this.reason = request.reason
            }
            val reply1 = flowOf(outbound1).map { value -> searchSearchIndexQueryService.summarizeSearchIndexes(value) }.single()
            resumeWorkOrderWorkflowResponse {
                this.outcome = ResumeWorkOrderWorkflowResponseKt.outcome {
                    this.message = reply1.result.message
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.message = reply1.result.message
                    this.status = "ok"
                }
                this.workOrder = workOrder {
                    this.status = request.workOrder.status
                    this.workOrderId = request.workOrderId
                }
            }
        }

    private suspend fun handleRetryWorkOrderWorkflow(request: RetryWorkOrderWorkflowRequest): RetryWorkOrderWorkflowResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderWorkflowService/RetryWorkOrderWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RetryWorkOrderWorkflow", request)
            log.atInfo().addKeyValue("operation", "RetryWorkOrderWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.workOrder.workOrderId).log("Recorded audit detail")
            val outbound0 = summarizeMetricSeriesesRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.details = SummarizeMetricSeriesesRequestKt.details {
                    this.filters.putAll(request.details.parametersMap)
                }
                this.reason = request.reason
            }
            val reply0 = withTimeout(5_000) { metricsMetricSeriesQueryService.summarizeMetricSerieses(outbound0) }
            val configure1: NormalizeNotificationRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.details = NormalizeNotificationRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.details.notify.email
                        this.name = request.details.notify.name
                    }
                }
                this.notification = notification {
                    this.requester = contactInfo {
                        this.phone = request.workOrder.requester.phone
                    }
                }
            }
            val outbound1 = normalizeNotificationRequest(configure1)
            val reply1 = notificationsNotificationValidationService.normalizeNotification(outbound1)
            retryWorkOrderWorkflowResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
                this.workOrder = workOrder {
                    this.assignee = userRef {
                        this.displayName = reply1.notification.customer.displayName
                    }
                    this.audit = auditInfo {
                        this.updatedAt = reply1.notification.audit.updatedAt
                    }
                    this.priority = request.workOrder.priority
                    this.workOrderId = request.workOrderId
                }
            }
        }

    private suspend fun handleStartWorkOrderWorkflow(request: StartWorkOrderWorkflowRequest): StartWorkOrderWorkflowResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderWorkflowService/StartWorkOrderWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StartWorkOrderWorkflow", request)
            log.atInfo().addKeyValue("operation", "StartWorkOrderWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.workOrder.workOrderId).log("Recorded audit detail")
            val outbound0 = listDriversRequest {}.driversCopy {
                this.context = request.context
                this.filter = ListDriversRequestKt.filter {
                    this.createdAfter = request.workOrder.audit.updatedAt
                    this.labels.putAll(request.details.parametersMap)
                    this.ownerId = request.workOrder.assignee.userId
                }
            }
            val reply0 = driversDriverService.listDrivers(outbound0)
            val outbound1 = watchSurveysRequest {
                this.context = request.context
                this.subscriber = contactInfo {
                    this.email = request.workOrder.customer.email
                    this.name = request.details.notify.name
                    this.phone = request.workOrder.requester.phone
                }
            }
            surveysSurveyService.watchSurveys(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound2 = summarizeMetricSeriesesRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
            }
            val reply2 = metricsMetricSeriesQueryService.summarizeMetricSerieses(outbound2)
            val outbound3 = submitDeliveryRequest {
                this.context = request.context
                this.details = SubmitDeliveryRequestKt.details {
                    this.approver = userRef {
                        this.displayName = when (request.workOrder.originCase) {
                            WorkOrder.OriginCase.CUSTOMER -> request.workOrder.customer.displayName
                            else -> ""
                        }
                        this.email = request.details.notify.email
                    }
                }
            }
            val reply3 = deliveriesDeliveryApprovalService.submitDelivery(outbound3)
            startWorkOrderWorkflowResponse {
                this.outcome = StartWorkOrderWorkflowResponseKt.outcome {
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.message = reply3.result.message
                    this.referenceIds.addAll((reply0.result.referenceIdsList) + (reply2.result.referenceIdsList))
                    this.status = "ok"
                }
                this.workOrder = workOrder {
                    this.subject = request.workOrder.subject
                    this.workOrderId = request.workOrderId
                }
            }
        }
}
