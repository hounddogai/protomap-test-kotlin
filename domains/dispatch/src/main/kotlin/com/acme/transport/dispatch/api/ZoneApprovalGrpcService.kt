package com.acme.transport.dispatch.api

import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.datasets.v1.DatasetQueryServiceGrpcKt
import com.acme.datasets.v1.SuggestDatasetsRequestKt
import com.acme.datasets.v1.suggestDatasetsRequest
import com.acme.deliveries.v1.DeliveryExceptionApprovalServiceGrpcKt
import com.acme.deliveries.v1.SubmitDeliveryExceptionRequestKt
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.deliveryExceptionComment
import com.acme.deliveries.v1.submitDeliveryExceptionRequest
import com.acme.dispatch.v1.ApproveZoneRequest
import com.acme.dispatch.v1.ApproveZoneResponse
import com.acme.dispatch.v1.ApproveZoneResponseKt
import com.acme.dispatch.v1.CommentZoneApprovalRequest
import com.acme.dispatch.v1.CommentZoneApprovalResponse
import com.acme.dispatch.v1.CommentZoneApprovalResponseKt
import com.acme.dispatch.v1.DispatchAssignmentApprovalServiceGrpcKt
import com.acme.dispatch.v1.DispatchAssignmentQueryServiceGrpcKt
import com.acme.dispatch.v1.DispatchOrderWorkflowServiceGrpcKt
import com.acme.dispatch.v1.EscalateZoneRequest
import com.acme.dispatch.v1.EscalateZoneResponse
import com.acme.dispatch.v1.GetZoneApprovalStatusRequest
import com.acme.dispatch.v1.GetZoneApprovalStatusResponse
import com.acme.dispatch.v1.ListPendingZonesRequest
import com.acme.dispatch.v1.ListPendingZonesResponse
import com.acme.dispatch.v1.RecallDispatchAssignmentRequestKt
import com.acme.dispatch.v1.RecallZoneRequest
import com.acme.dispatch.v1.RecallZoneResponse
import com.acme.dispatch.v1.RecallZoneResponseKt
import com.acme.dispatch.v1.RetryDispatchOrderWorkflowRequest
import com.acme.dispatch.v1.RetryDispatchOrderWorkflowRequestKt
import com.acme.dispatch.v1.SubmitZoneRequest
import com.acme.dispatch.v1.SubmitZoneResponse
import com.acme.dispatch.v1.SummarizeDispatchAssignmentsRequest
import com.acme.dispatch.v1.ZoneApprovalServiceGrpcKt
import com.acme.dispatch.v1.approveZoneResponse
import com.acme.dispatch.v1.commentZoneApprovalResponse
import com.acme.dispatch.v1.dispatchAssignment
import com.acme.dispatch.v1.dispatchOrder
import com.acme.dispatch.v1.dispatchOrderLine
import com.acme.dispatch.v1.dispatchOrderLineTax
import com.acme.dispatch.v1.escalateZoneResponse
import com.acme.dispatch.v1.getZoneApprovalStatusResponse
import com.acme.dispatch.v1.listPendingZonesResponse
import com.acme.dispatch.v1.recallDispatchAssignmentRequest
import com.acme.dispatch.v1.recallZoneResponse
import com.acme.dispatch.v1.retryDispatchOrderWorkflowRequest
import com.acme.dispatch.v1.submitZoneResponse
import com.acme.dispatch.v1.summarizeDispatchAssignmentsRequest
import com.acme.dispatch.v1.zone
import com.acme.drivers.v1.DriverCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverServiceGrpcKt
import com.acme.drivers.v1.ListDriversRequest
import com.acme.drivers.v1.ListDriversRequestKt
import com.acme.drivers.v1.ListDriversResponse
import com.acme.drivers.v1.QuoteDriverRequest
import com.acme.drivers.v1.QuoteDriverResponse
import com.acme.drivers.v1.RecalculateDriverLicensesRequestKt
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.listDriversRequest
import com.acme.drivers.v1.quoteDriverRequest
import com.acme.drivers.v1.recalculateDriverLicensesRequest
import com.acme.fuel.v1.FuelPolicyImportServiceGrpcKt
import com.acme.fuel.v1.ValidateFuelPolicyImportRequest
import com.acme.fuel.v1.ValidateFuelPolicyImportRequestKt
import com.acme.fuel.v1.ValidateFuelPolicyImportResponse
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.validateFuelPolicyImportRequest
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.ListWorkOrderWorkflowsRequestKt
import com.acme.maintenance.v1.WorkOrderWorkflowServiceGrpcKt
import com.acme.maintenance.v1.listWorkOrderWorkflowsRequest
import com.acme.metrics.v1.MetricSeriesServiceGrpcKt
import com.acme.metrics.v1.WatchMetricSeriesesRequestKt
import com.acme.metrics.v1.watchMetricSeriesesRequest
import com.acme.notifications.v1.NotificationValidationServiceGrpcKt
import com.acme.notifications.v1.ValidateNotificationRequestKt
import com.acme.notifications.v1.notification
import com.acme.notifications.v1.validateNotificationRequest
import com.acme.transport.dispatch.DispatchRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.CancelDockWorkflowRequest
import com.acme.yards.v1.CancelDockWorkflowResponse
import com.acme.yards.v1.DockWorkflowServiceGrpcKt
import com.acme.yards.v1.cancelDockWorkflowRequest
import com.acme.yards.v1.dock
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Routes zones through approval, with escalation and delegation. */
class ZoneApprovalGrpcService(
    private val store: DispatchRepository,
    private val workers: CoroutineScope,
    private val datasetsDatasetQueryService: DatasetQueryServiceGrpcKt.DatasetQueryServiceCoroutineStub,
    private val deliveriesDeliveryExceptionApprovalService: DeliveryExceptionApprovalServiceGrpcKt.DeliveryExceptionApprovalServiceCoroutineStub,
    private val dispatchDispatchAssignmentApprovalService: DispatchAssignmentApprovalServiceGrpcKt.DispatchAssignmentApprovalServiceCoroutineStub,
    private val dispatchDispatchAssignmentQueryService: DispatchAssignmentQueryServiceGrpcKt.DispatchAssignmentQueryServiceCoroutineStub,
    private val dispatchDispatchOrderWorkflowService: DispatchOrderWorkflowServiceGrpcKt.DispatchOrderWorkflowServiceCoroutineStub,
    private val driversDriverCalculationService: DriverCalculationServiceGrpcKt.DriverCalculationServiceCoroutineStub,
    private val driversDriverLicenseCalculationService: DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub,
    private val driversDriverService: DriverServiceGrpcKt.DriverServiceCoroutineStub,
    private val fuelFuelPolicyImportService: FuelPolicyImportServiceGrpcKt.FuelPolicyImportServiceCoroutineStub,
    private val maintenanceWorkOrderWorkflowService: WorkOrderWorkflowServiceGrpcKt.WorkOrderWorkflowServiceCoroutineStub,
    private val metricsMetricSeriesService: MetricSeriesServiceGrpcKt.MetricSeriesServiceCoroutineStub,
    private val notificationsNotificationValidationService: NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub,
    private val yardsDockWorkflowService: DockWorkflowServiceGrpcKt.DockWorkflowServiceCoroutineStub,
) : ZoneApprovalServiceGrpcKt.ZoneApprovalServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(ZoneApprovalGrpcService::class.java)

    private val invokeCommentZoneApproval0: suspend (ValidateFuelPolicyImportRequest) -> ValidateFuelPolicyImportResponse = { payload ->
        fuelFuelPolicyImportService.validateFuelPolicyImport(payload)
    }

    private val workerForRecallZone0 = ReplyMailbox<ListDriversRequest, ListDriversResponse>(workers) { payload ->
        withTimeout(5_000) { driversDriverService.listDrivers(payload) }
    }

    private val clientForRecallZone1 by lazy { dispatchDispatchAssignmentApprovalService }

    /** Approves a zone. */
    override suspend fun approveZone(request: ApproveZoneRequest): ApproveZoneResponse {
        return handleApproveZone(request)
    }

    /** Comments a zone. */
    override suspend fun commentZoneApproval(request: CommentZoneApprovalRequest): CommentZoneApprovalResponse = handleCommentZoneApproval(request)

    /** Escalates a zone. */
    override suspend fun escalateZone(request: EscalateZoneRequest): EscalateZoneResponse =
        RpcBoundary.run("acme.dispatch.v1.ZoneApprovalService/EscalateZone") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EscalateZone", request)
            log.atInfo().addKeyValue("operation", "EscalateZone").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.zone.zoneId).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                validateNotificationRequest {
                    this.context = request.context
                    this.details = ValidateNotificationRequestKt.details {
                        this.contact = contactInfo {
                            this.email = request.zone.manager.email
                        }
                    }
                    this.notification = notification {
                        this.assignee = userRef {
                            this.email = request.details.approver.email
                            this.userId = request.details.approver.userId
                        }
                    }
                }
            }
            val reply0 = notificationsNotificationValidationService.validateNotification(outbound0)
            escalateZoneResponse {
                this.result = operationResult {
                    this.status = reply0.outcome.status
                }
                this.zone = zone {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.notification.audit.updatedAt
                    }
                    this.depth = request.zone.depth
                    this.zoneId = request.zoneId
                }
            }
        }

    /** Returns a zone. */
    override suspend fun getZoneApprovalStatus(request: GetZoneApprovalStatusRequest): GetZoneApprovalStatusResponse {
        return handleGetZoneApprovalStatus(request)
    }

    /** Lists zones a page at a time. */
    override suspend fun listPendingZones(request: ListPendingZonesRequest): ListPendingZonesResponse = handleListPendingZones(request)

    /** Recalls a zone. */
    override suspend fun recallZone(request: RecallZoneRequest): RecallZoneResponse =
        RpcBoundary.run("acme.dispatch.v1.ZoneApprovalService/RecallZone") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecallZone", request)
            log.atInfo().addKeyValue("operation", "RecallZone").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.zone.zoneId).log("Recorded audit detail")
            val outbound0 = listDriversRequest {
                this.context = request.context
                this.filter = ListDriversRequestKt.filter {
                    this.region = request.zone.location.region
                }
            }
            val reply0 = workerForRecallZone0.submit(outbound0)
            val outbound1 = recallDispatchAssignmentRequest {
                this.context = request.context
                this.details = RecallDispatchAssignmentRequestKt.details {
                    this.approver = userRef {
                        this.userId = request.zone.manager.userId
                    }
                }
                this.dispatchAssignment = dispatchAssignment {
                    this.assignee = userRef {
                        this.displayName = request.details.approver.displayName
                    }
                    this.audit = auditInfo {
                        this.updatedAt = request.zone.audit.updatedAt
                    }
                }
            }
            val reply1 = clientForRecallZone1.recallDispatchAssignment(outbound1)
            val outbound2 = submitDeliveryExceptionRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.audit = auditInfo {
                        this.createdAt = request.zone.audit.createdAt
                    }
                    this.comments.addAll(listOf(deliveryExceptionComment {
                        this.createdAt = reply1.dispatchAssignment.audit.createdAt
                    }))
                }
                this.details = SubmitDeliveryExceptionRequestKt.details {
                    this.approver = userRef {
                        this.displayName = request.details.approver.displayName
                        this.email = reply1.dispatchAssignment.customer.email
                    }
                }
            }
            val reply2 = flowOf(outbound2).map { value -> deliveriesDeliveryExceptionApprovalService.submitDeliveryException(value) }.single()
            recallZoneResponse {
                this.outcome = RecallZoneResponseKt.outcome {
                    this.referenceId = reply1.outcome.referenceId
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = "ok"
                }
                this.zone = zone {
                    this.audit = auditInfo {
                        this.updatedAt = reply2.deliveryException.audit.updatedAt
                    }
                    this.children.addAll(listOf(zone {
                        this.manager = userRef {
                            this.displayName = reply2.deliveryException.customer.displayName
                        }
                    }))
                    this.parentId = request.zone.parentId
                    this.zoneId = request.zoneId
                }
            }
        }

    /** Submits a zone. */
    override suspend fun submitZone(request: SubmitZoneRequest): SubmitZoneResponse {
        return handleSubmitZone(request)
    }

    private suspend fun handleApproveZone(request: ApproveZoneRequest): ApproveZoneResponse =
        RpcBoundary.run("acme.dispatch.v1.ZoneApprovalService/ApproveZone") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ApproveZone", request)
            log.atInfo().addKeyValue("operation", "ApproveZone").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.zone.zoneId).log("Recorded audit detail")
            val outbound0 = summarizeDispatchAssignmentsRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
            }
            val command0: Submission<SummarizeDispatchAssignmentsRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = dispatchDispatchAssignmentQueryService.summarizeDispatchAssignments(payload0)
            val outbound1 = quoteDriverRequest {
                this.context = request.context
                this.driver = driver {
                    this.audit = auditInfo {
                        this.createdAt = request.zone.audit.createdAt
                        this.updatedAt = request.zone.audit.updatedAt
                    }
                    this.relationshipManager = userRef {
                        this.email = request.details.approver.email
                    }
                }
            }
            val adapter1 = OutboundPort<QuoteDriverRequest, QuoteDriverResponse> { payload ->
                driversDriverCalculationService.quoteDriver(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            approveZoneResponse {
                this.outcome = ApproveZoneResponseKt.outcome {
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.zone = zone {
                    this.children.addAll(listOf(zone {
                        this.location = postalAddress {
                            this.locality = reply1.driver.primaryAddress.locality
                        }
                        this.manager = userRef {
                            this.email = reply1.driver.contact.email
                        }
                    }))
                    this.depth = request.zone.depth
                    this.zoneId = request.zoneId
                }
            }
        }

    private suspend fun handleCommentZoneApproval(request: CommentZoneApprovalRequest): CommentZoneApprovalResponse =
        RpcBoundary.run("acme.dispatch.v1.ZoneApprovalService/CommentZoneApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CommentZoneApproval", request)
            log.atInfo().addKeyValue("operation", "CommentZoneApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.zone.zoneId).log("Recorded audit detail")
            val outbound0 = validateFuelPolicyImportRequest {
                this.context = request.context
                this.details = ValidateFuelPolicyImportRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.zone.manager.email
                    }
                }
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.etag = request.zone.audit.etag
                        this.updatedAt = request.zone.audit.updatedAt
                    }
                }
            }
            val reply0 = invokeCommentZoneApproval0(outbound0)
            val outbound1 = cancelDockWorkflowRequest {
                this.context = request.context
                this.dock = dock {
                    this.address = postalAddress {
                        this.region = request.zone.location.region
                    }
                    this.audit = auditInfo {
                        this.createdAt = request.zone.audit.createdAt
                    }
                    this.owner = userRef {
                        this.displayName = reply0.fuelPolicy.owner.displayName
                        this.userId = reply0.fuelPolicy.owner.userId
                    }
                }
            }
            val invoke1: suspend (CancelDockWorkflowRequest, Metadata) -> CancelDockWorkflowResponse = yardsDockWorkflowService::cancelDockWorkflow
            val reply1 = invoke1(outbound1, Metadata())
            val outbound2 = RetryDispatchOrderWorkflowRequest.newBuilder().apply {
                setContext(request.context)
                setDetails(RetryDispatchOrderWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.email = reply0.fuelPolicy.owner.email
                    }
                })
                setDispatchOrder(dispatchOrder {
                    this.billingAddress = postalAddress {
                        this.postalCode = request.zone.location.postalCode
                    }
                    this.lines.addAll(listOf(dispatchOrderLine {
                        this.tax = dispatchOrderLineTax {
                            this.amount = reply0.fuelPolicy.discount.amount
                        }
                    }))
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply2 = outbound2.let { value -> dispatchDispatchOrderWorkflowService.retryDispatchOrderWorkflow(value) }
            commentZoneApprovalResponse {
                this.outcome = CommentZoneApprovalResponseKt.outcome {
                    this.referenceId = reply2.outcome.referenceId
                    this.status = reply0.outcome.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.zone = zone {
                    this.audit = auditInfo {
                        this.etag = reply0.fuelPolicy.audit.etag
                    }
                    this.parentId = request.zone.parentId
                    this.zoneId = request.zoneId
                }
            }
        }

    private suspend fun handleGetZoneApprovalStatus(request: GetZoneApprovalStatusRequest): GetZoneApprovalStatusResponse =
        RpcBoundary.run("acme.dispatch.v1.ZoneApprovalService/GetZoneApprovalStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetZoneApprovalStatus", request)
            log.atInfo().addKeyValue("operation", "GetZoneApprovalStatus").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            getZoneApprovalStatusResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.zone = zone {
                    this.zoneId = request.zoneId
                }
            }
        }

    private suspend fun handleListPendingZones(request: ListPendingZonesRequest): ListPendingZonesResponse =
        RpcBoundary.run("acme.dispatch.v1.ZoneApprovalService/ListPendingZones") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListPendingZones", request)
            log.atInfo().addKeyValue("operation", "ListPendingZones").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = recalculateDriverLicensesRequest {
                this.context = request.context
                this.details = RecalculateDriverLicensesRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.filter.region
                    }
                }
                this.driverLicense = driverLicense {
                    this.audit = auditInfo {
                        this.createdAt = request.filter.createdAfter
                    }
                    this.counterparty = driverRef {
                        this.email = request.context.actor.email
                    }
                    this.metadata.putAll(request.filter.labelsMap)
                }
            }
            val reply0 = ClientRegistry.lookup("acme.drivers.v1.DriverLicenseCalculationService", DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub::class.java).recalculateDriverLicenses(outbound0)
            val outbound1 = suggestDatasetsRequest {
                this.context = request.context
                this.filter = SuggestDatasetsRequestKt.filter {
                    this.labels.putAll(request.filter.labelsMap)
                }
            }
            val reply1 = datasetsDatasetQueryService.suggestDatasets(WireCopies.reflective(outbound1))
            listPendingZonesResponse {
                this.result = operationResult {
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
                this.zones.addAll(listOf(zone {
                    this.children.addAll(listOf(zone {
                        this.children.addAll(listOf(zone {
                            this.audit = auditInfo {
                                this.updatedBy = reply0.driverLicense.audit.updatedBy
                            }
                            this.location = postalAddress {
                                this.region = reply0.driverLicense.billingAddress.region
                            }
                        }))
                    }))
                }))
            }
        }

    private suspend fun handleSubmitZone(request: SubmitZoneRequest): SubmitZoneResponse =
        RpcBoundary.run("acme.dispatch.v1.ZoneApprovalService/SubmitZone") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SubmitZone", request)
            log.atInfo().addKeyValue("operation", "SubmitZone").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.zone.zoneId).log("Recorded audit detail")
            val outbound0 = watchMetricSeriesesRequest {
                this.context = request.context
                this.filter = WatchMetricSeriesesRequestKt.filter {
                    this.createdAfter = request.details.effectiveAt
                    this.region = request.zone.location.region
                }
                this.since = request.zone.audit.updatedAt
                this.subscriber = contactInfo {
                    this.email = request.zone.manager.email
                }
            }
            metricsMetricSeriesService.watchMetricSerieses(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val configure1: ListWorkOrderWorkflowsRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.filter = ListWorkOrderWorkflowsRequestKt.filter {
                    this.region = request.zone.location.region
                }
            }
            val outbound1 = listWorkOrderWorkflowsRequest(configure1)
            val reply1 = maintenanceWorkOrderWorkflowService.listWorkOrderWorkflows(outbound1)
            submitZoneResponse {
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = "ok"
                }
                this.zone = zone {
                    this.depth = request.zone.depth
                    this.zoneId = request.zoneId
                }
            }
        }
}
