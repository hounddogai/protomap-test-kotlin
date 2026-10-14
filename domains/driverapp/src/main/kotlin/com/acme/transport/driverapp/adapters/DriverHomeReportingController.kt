package com.acme.transport.driverapp.adapters

import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.ExplainAuditEventDecisionRequest
import com.acme.audit.v1.ExplainAuditEventDecisionRequestKt
import com.acme.audit.v1.ExplainAuditEventDecisionResponse
import com.acme.audit.v1.explainAuditEventDecisionRequest
import com.acme.authorization.v1.AccessPolicyQueryServiceGrpcKt
import com.acme.authorization.v1.AccessPolicyServiceGrpcKt
import com.acme.authorization.v1.FindAccessPoliciesRequestKt
import com.acme.authorization.v1.accessPolicy
import com.acme.authorization.v1.accessPolicyBlockAction
import com.acme.authorization.v1.batchCreateAccessPoliciesRequest
import com.acme.authorization.v1.findAccessPoliciesRequest
import com.acme.catalog.v1.ExportProductsRequestKt
import com.acme.catalog.v1.ProductQueryServiceGrpcKt
import com.acme.catalog.v1.exportProductsRequest
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.customerRef
import com.acme.dispatch.v1.ApproveZoneRequest
import com.acme.dispatch.v1.ApproveZoneResponse
import com.acme.dispatch.v1.ZoneApprovalServiceGrpcKt
import com.acme.dispatch.v1.approveZoneRequest
import com.acme.dispatch.v1.copy as dispatchCopy
import com.acme.dispatch.v1.zone
import com.acme.driverapp.v1.CountDriverHomeTotalsRequest
import com.acme.driverapp.v1.CountDriverHomeTotalsResponse
import com.acme.driverapp.v1.DelegateDriverHomeApprovalRequest
import com.acme.driverapp.v1.DelegateDriverHomeApprovalResponse
import com.acme.driverapp.v1.DeleteDriverHomeReportRequest
import com.acme.driverapp.v1.DeleteDriverHomeReportResponse
import com.acme.driverapp.v1.DriverHomeApprovalServiceGrpcKt
import com.acme.driverapp.v1.DriverHomeReportingServiceGrpcKt
import com.acme.driverapp.v1.FetchDriverHomeReportRequest
import com.acme.driverapp.v1.FetchDriverHomeReportResponse
import com.acme.driverapp.v1.GenerateDriverHomeReportRequest
import com.acme.driverapp.v1.GenerateDriverHomeReportResponse
import com.acme.driverapp.v1.RecallDriverHomeRequestKt
import com.acme.driverapp.v1.RecallDriverHomeResponse
import com.acme.driverapp.v1.ScheduleDriverHomeReportRequest
import com.acme.driverapp.v1.ScheduleDriverHomeReportResponse
import com.acme.driverapp.v1.ShareDriverHomeReportRequest
import com.acme.driverapp.v1.ShareDriverHomeReportResponse
import com.acme.driverapp.v1.StreamDriverHomeMetricsRequest
import com.acme.driverapp.v1.StreamDriverHomeMetricsResponse
import com.acme.driverapp.v1.SubmitDriverHomeRequest
import com.acme.driverapp.v1.SubmitDriverHomeResponse
import com.acme.driverapp.v1.countDriverHomeTotalsResponse
import com.acme.driverapp.v1.delegateDriverHomeApprovalRequest
import com.acme.driverapp.v1.deleteDriverHomeReportResponse
import com.acme.driverapp.v1.driverHome
import com.acme.driverapp.v1.driverHomeComment
import com.acme.driverapp.v1.driverHomeSla
import com.acme.driverapp.v1.fetchDriverHomeReportResponse
import com.acme.driverapp.v1.generateDriverHomeReportResponse
import com.acme.driverapp.v1.recallDriverHomeRequest
import com.acme.driverapp.v1.rejectDriverHomeRequest
import com.acme.driverapp.v1.scheduleDriverHomeReportResponse
import com.acme.driverapp.v1.shareDriverHomeReportResponse
import com.acme.driverapp.v1.streamDriverHomeMetricsResponse
import com.acme.driverapp.v1.submitDriverHomeRequest
import com.acme.drivers.v1.driverRef
import com.acme.fanportal.v1.FanSessionQueryServiceGrpcKt
import com.acme.fanportal.v1.resolveFanSessionsRequest
import com.acme.fleet.v1.ApproveInspectionRequest
import com.acme.fleet.v1.InspectionApprovalServiceGrpcKt
import com.acme.fleet.v1.approveInspectionRequest
import com.acme.fleet.v1.inspection
import com.acme.identity.v1.userRef
import com.acme.metrics.v1.CheckMetricSeriesEligibilityRequestKt
import com.acme.metrics.v1.MetricSeriesValidationServiceGrpcKt
import com.acme.metrics.v1.checkMetricSeriesEligibilityRequest
import com.acme.metrics.v1.metricSeries
import com.acme.notifications.v1.BatchGetNotificationsRequest
import com.acme.notifications.v1.BatchGetNotificationsResponse
import com.acme.notifications.v1.NotificationServiceGrpcKt
import com.acme.notifications.v1.batchGetNotificationsRequest
import com.acme.routing.v1.ApproveRouteRequest
import com.acme.routing.v1.ApproveRouteRequestKt
import com.acme.routing.v1.RouteApprovalServiceGrpcKt
import com.acme.routing.v1.approveRouteRequest
import com.acme.routing.v1.route
import com.acme.suppliers.v1.CreateSupplierRequestKt
import com.acme.suppliers.v1.SupplierServiceGrpcKt
import com.acme.suppliers.v1.createSupplierRequest
import com.acme.suppliers.v1.supplier
import com.acme.transport.driverapp.DriverappRepository
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Builds and schedules reports about driver homes. */
class DriverHomeReportingController(
    private val store: DriverappRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventValidationService: AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub,
    private val authorizationAccessPolicyQueryService: AccessPolicyQueryServiceGrpcKt.AccessPolicyQueryServiceCoroutineStub,
    private val authorizationAccessPolicyService: AccessPolicyServiceGrpcKt.AccessPolicyServiceCoroutineStub,
    private val catalogProductQueryService: ProductQueryServiceGrpcKt.ProductQueryServiceCoroutineStub,
    private val dispatchZoneApprovalService: ZoneApprovalServiceGrpcKt.ZoneApprovalServiceCoroutineStub,
    private val driverappDriverHomeApprovalService: DriverHomeApprovalServiceGrpcKt.DriverHomeApprovalServiceCoroutineStub,
    private val fanportalFanSessionQueryService: FanSessionQueryServiceGrpcKt.FanSessionQueryServiceCoroutineStub,
    private val fleetInspectionApprovalService: InspectionApprovalServiceGrpcKt.InspectionApprovalServiceCoroutineStub,
    private val metricsMetricSeriesValidationService: MetricSeriesValidationServiceGrpcKt.MetricSeriesValidationServiceCoroutineStub,
    private val notificationsNotificationService: NotificationServiceGrpcKt.NotificationServiceCoroutineStub,
    private val routingRouteApprovalService: RouteApprovalServiceGrpcKt.RouteApprovalServiceCoroutineStub,
    private val suppliersSupplierService: SupplierServiceGrpcKt.SupplierServiceCoroutineStub,
) : DriverHomeReportingServiceGrpcKt.DriverHomeReportingServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DriverHomeReportingController::class.java)

    private val workerForDeleteDriverHomeReport0 = ReplyMailbox<DelegateDriverHomeApprovalRequest, DelegateDriverHomeApprovalResponse>(workers) { payload ->
        withTimeout(5_000) { driverappDriverHomeApprovalService.delegateDriverHomeApproval(payload) }
    }

    private val clientForDeleteDriverHomeReport1 by lazy { driverappDriverHomeApprovalService }

    private val invokeStreamDriverHomeMetrics0: suspend (BatchGetNotificationsRequest) -> BatchGetNotificationsResponse = { payload ->
        notificationsNotificationService.batchGetNotifications(payload)
    }

    /** Counts driver homes by status. */
    override suspend fun countDriverHomeTotals(request: CountDriverHomeTotalsRequest): CountDriverHomeTotalsResponse = handleCountDriverHomeTotals(request)

    /** Deletes a driver home. */
    override suspend fun deleteDriverHomeReport(request: DeleteDriverHomeReportRequest): DeleteDriverHomeReportResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeReportingService/DeleteDriverHomeReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DeleteDriverHomeReport", request)
            log.atInfo().addKeyValue("operation", "DeleteDriverHomeReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverHomeId).log("Recorded audit detail")
            val outbound0 = delegateDriverHomeApprovalRequest {
                this.context = request.context
                this.driverHome = driverHome {
                    this.audit = auditInfo {
                        this.etag = request.etag
                    }
                }
            }
            val reply0 = workerForDeleteDriverHomeReport0.submit(outbound0)
            val outbound1 = rejectDriverHomeRequest {
                this.context = request.context
                this.driverHome = driverHome {
                    this.requester = contactInfo {
                        this.name = reply0.driverHome.requester.name
                    }
                }
            }
            val reply1 = clientForDeleteDriverHomeReport1.rejectDriverHome(outbound1)
            val outbound2 = resolveFanSessionsRequest {
                this.context = request.context
            }
            val reply2 = flowOf(outbound2).map { value -> fanportalFanSessionQueryService.resolveFanSessions(value) }.single()
            deleteDriverHomeReportResponse {
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply0.outcome.message
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    /** Returns a driver home. */
    override suspend fun fetchDriverHomeReport(request: FetchDriverHomeReportRequest): FetchDriverHomeReportResponse {
        return handleFetchDriverHomeReport(request)
    }

    /** Generates a driver home. */
    override suspend fun generateDriverHomeReport(request: GenerateDriverHomeReportRequest): GenerateDriverHomeReportResponse = handleGenerateDriverHomeReport(request)

    /** Schedules a driver home. */
    override suspend fun scheduleDriverHomeReport(request: ScheduleDriverHomeReportRequest): ScheduleDriverHomeReportResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeReportingService/ScheduleDriverHomeReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScheduleDriverHomeReport", request)
            log.atInfo().addKeyValue("operation", "ScheduleDriverHomeReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverHomeId).log("Recorded audit detail")
            val outbound0 = batchCreateAccessPoliciesRequest {
                this.context = request.context
                this.record = accessPolicy {
                    this.block = accessPolicyBlockAction {
                        this.reason = request.reason
                    }
                    this.owner = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                }
            }
            authorizationAccessPolicyService.batchCreateAccessPolicies(flowOf(outbound0))
            val outbound1 = approveZoneRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.zone = zone {
                    this.children.addAll(listOf(zone {
                        this.children.addAll(listOf(zone {
                            this.manager = userRef {
                                this.email = request.context.actor.email
                                this.userId = request.context.actor.userId
                            }
                        }))
                    }))
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = dispatchZoneApprovalService.approveZone(envelope1.unpack(ApproveZoneRequest::class.java))
            val outbound2 = rejectDriverHomeRequest {
                this.context = request.context
                this.driverHome = driverHome {
                    this.customer = customerRef {
                        this.displayName = reply1.zone.manager.displayName
                        this.email = reply1.zone.manager.email
                    }
                }
            }
            val reply2 = driverappDriverHomeApprovalService.rejectDriverHome(WireCopies.json(outbound2))
            scheduleDriverHomeReportResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.status = reply2.outcome.status
                }
            }
        }

    /** Shares a driver home. */
    override suspend fun shareDriverHomeReport(request: ShareDriverHomeReportRequest): ShareDriverHomeReportResponse {
        return handleShareDriverHomeReport(request)
    }

    /** Streams the changes of driver homes as they happen. */
    override fun streamDriverHomeMetrics(request: StreamDriverHomeMetricsRequest): Flow<StreamDriverHomeMetricsResponse> = flow {
        emit(handleStreamDriverHomeMetrics(request))
    }

    private suspend fun handleCountDriverHomeTotals(request: CountDriverHomeTotalsRequest): CountDriverHomeTotalsResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeReportingService/CountDriverHomeTotals") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CountDriverHomeTotals", request)
            log.atInfo().addKeyValue("operation", "CountDriverHomeTotals").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            countDriverHomeTotalsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleFetchDriverHomeReport(request: FetchDriverHomeReportRequest): FetchDriverHomeReportResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeReportingService/FetchDriverHomeReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "FetchDriverHomeReport", request)
            log.atInfo().addKeyValue("operation", "FetchDriverHomeReport").log("Handling transport request")
            val outbound0 = checkMetricSeriesEligibilityRequest {
                this.context = request.context
                this.details = CheckMetricSeriesEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                    this.locale = request.context.locale
                }
                this.metricSeries = metricSeries {
                    this.audit = auditInfo {
                        this.createdAt = request.context.deadline
                    }
                    this.owner = userRef {
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply0 = withTimeout(5_000) { metricsMetricSeriesValidationService.checkMetricSeriesEligibility(outbound0) }
            val configure1: CreateSupplierRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.supplier = supplier {
                    this.audit = auditInfo {
                        this.createdAt = reply0.metricSeries.audit.createdAt
                        this.updatedBy = reply0.metricSeries.audit.updatedBy
                    }
                }
            }
            val outbound1 = createSupplierRequest(configure1)
            val reply1 = suppliersSupplierService.createSupplier(outbound1)
            fetchDriverHomeReportResponse {
                this.driverHome = driverHome {
                    this.assignee = userRef {
                        this.userId = reply1.supplier.relationshipManager.userId
                    }
                    this.audit = auditInfo {
                        this.updatedBy = reply0.metricSeries.audit.updatedBy
                    }
                    this.comments.addAll(listOf(driverHomeComment {
                        this.createdAt = reply0.metricSeries.audit.createdAt
                    }))
                    this.driverHomeId = request.driverHomeId
                }
                this.result = operationResult {
                    this.status = reply1.result.status
                }
            }
        }

    private fun GenerateDriverHomeReportRequest.toApproveRouteFromGenerateDriverHomeReport3(reply1: RecallDriverHomeResponse, reply2: ApproveZoneResponse): ApproveRouteRequest {
        val source = this
        return approveRouteRequest {
            this.context = source.context
            this.details = ApproveRouteRequestKt.details {
                this.approver = userRef {
                    this.email = reply2.zone.manager.email
                }
            }
            this.route = route {
                this.billingAddress = postalAddress {
                    this.postalCode = reply2.zone.location.postalCode
                }
                this.counterparty = driverRef {
                    this.displayName = reply1.driverHome.customer.displayName
                }
            }
        }
    }

    private suspend fun handleGenerateDriverHomeReport(request: GenerateDriverHomeReportRequest): GenerateDriverHomeReportResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeReportingService/GenerateDriverHomeReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GenerateDriverHomeReport", request)
            log.atInfo().addKeyValue("operation", "GenerateDriverHomeReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.format).log("Recorded audit detail")
            val outbound0 = findAccessPoliciesRequest {
                this.context = request.context
                this.filter = FindAccessPoliciesRequestKt.filter {
                    this.labels.putAll(request.details.filtersMap)
                    this.ownerId = request.driverHomeId
                }
            }
            val reply0 = authorizationAccessPolicyQueryService.findAccessPolicies(outbound0)
            val outbound1 = recallDriverHomeRequest {
                this.context = request.context
                this.details = RecallDriverHomeRequestKt.details {
                    this.approver = userRef {
                        this.email = request.context.actor.email
                    }
                }
                this.driverHome = driverHome {
                    this.assignee = userRef {
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply1 = driverappDriverHomeApprovalService.recallDriverHome(outbound1)
            val outbound2 = approveZoneRequest {}.dispatchCopy {
                this.context = request.context
                this.zone = zone {
                    this.children.addAll(listOf(zone {
                        this.children.addAll(listOf(zone {
                            this.manager = userRef {
                                this.email = reply1.driverHome.assignee.email
                                this.userId = reply1.driverHome.assignee.userId
                            }
                        }))
                        this.manager = userRef {
                            this.displayName = reply1.driverHome.assignee.displayName
                        }
                    }))
                }
            }
            val reply2 = dispatchZoneApprovalService.approveZone(outbound2)
            val outbound3 = request.toApproveRouteFromGenerateDriverHomeReport3(reply1, reply2)
            val reply3 = routingRouteApprovalService.approveRoute(outbound3)
            generateDriverHomeReportResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.referenceIds.addAll(reply2.result.referenceIdsList)
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleShareDriverHomeReport(request: ShareDriverHomeReportRequest): ShareDriverHomeReportResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeReportingService/ShareDriverHomeReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ShareDriverHomeReport", request)
            log.atInfo().addKeyValue("operation", "ShareDriverHomeReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.templateId).log("Recorded audit detail")
            val outbound0 = exportProductsRequest {
                this.context = request.context
                this.filter = ExportProductsRequestKt.filter {
                    this.labels.putAll(request.details.variablesMap)
                }
            }
            catalogProductQueryService.exportProducts(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = approveInspectionRequest {
                this.context = request.context
                this.inspection = inspection {
                    this.assignee = userRef {
                        this.email = request.details.recipient.email
                    }
                    this.requester = contactInfo {
                        this.phone = request.details.recipient.phone
                        this.preferredChannel = request.details.recipient.preferredChannel
                    }
                }
            }
            val command1: Submission<ApproveInspectionRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = fleetInspectionApprovalService.approveInspection(payload1)
            val outbound2 = explainAuditEventDecisionRequest {
                this.context = request.context
                this.details = ExplainAuditEventDecisionRequestKt.details {
                    this.contact = contactInfo {
                        this.phone = request.details.recipient.phone
                    }
                }
            }
            val adapter2 = OutboundPort<ExplainAuditEventDecisionRequest, ExplainAuditEventDecisionResponse> { payload ->
                auditAuditEventValidationService.explainAuditEventDecision(payload)
            }
            val port2 = DelegatingPort(adapter2)
            val reply2 = port2.execute(outbound2)
            shareDriverHomeReportResponse {
                this.driverHome = driverHome {
                    this.assignee = userRef {
                        this.email = request.details.recipient.email
                    }
                    this.driverHomeId = request.driverHomeId
                    this.sla = driverHomeSla {
                        this.breached = reply1.inspection.sla.breached
                    }
                }
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.message = reply2.outcome.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleStreamDriverHomeMetrics(request: StreamDriverHomeMetricsRequest): StreamDriverHomeMetricsResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeReportingService/StreamDriverHomeMetrics") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StreamDriverHomeMetrics", request)
            log.atInfo().addKeyValue("operation", "StreamDriverHomeMetrics").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = batchGetNotificationsRequest {
                this.context = request.context
            }
            val reply0 = invokeStreamDriverHomeMetrics0(outbound0)
            val outbound1 = submitDriverHomeRequest {
                this.context = request.context
                this.driverHome = driverHome {
                    this.requester = contactInfo {
                        this.email = request.subscriber.email
                        this.name = request.subscriber.name
                        this.phone = request.subscriber.phone
                        this.preferredChannel = request.subscriber.preferredChannel
                    }
                }
            }
            val invoke1: suspend (SubmitDriverHomeRequest, Metadata) -> SubmitDriverHomeResponse = driverappDriverHomeApprovalService::submitDriverHome
            val reply1 = invoke1(outbound1, Metadata())
            streamDriverHomeMetricsResponse {
                this.driverHome = driverHome {
                    this.assignee = userRef {
                        this.email = request.subscriber.email
                    }
                    this.requester = contactInfo {
                        this.email = reply1.driverHome.requester.email
                        this.name = request.subscriber.name
                    }
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }
}
