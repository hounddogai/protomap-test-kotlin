package com.acme.transport.yards.adapters

import com.acme.authorization.v1.AccessPolicyServiceGrpcKt
import com.acme.authorization.v1.accessPolicy
import com.acme.authorization.v1.updateAccessPolicyRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.dispatch.v1.CheckDispatchAssignmentEligibilityRequest
import com.acme.dispatch.v1.CheckDispatchAssignmentEligibilityResponse
import com.acme.dispatch.v1.DispatchAssignmentValidationServiceGrpcKt
import com.acme.dispatch.v1.PreviewDispatchAssignmentRequest
import com.acme.dispatch.v1.PreviewDispatchAssignmentRequestKt
import com.acme.dispatch.v1.ZoneCalculationServiceGrpcKt
import com.acme.dispatch.v1.checkDispatchAssignmentEligibilityRequest
import com.acme.dispatch.v1.dispatchAssignment
import com.acme.dispatch.v1.previewDispatchAssignmentRequest
import com.acme.dispatch.v1.recalculateZonesRequest
import com.acme.dispatch.v1.zone
import com.acme.drivers.v1.driverRef
import com.acme.identity.v1.GetUserRulesRequest
import com.acme.identity.v1.GetUserRulesResponse
import com.acme.identity.v1.NormalizeUserRequest
import com.acme.identity.v1.UserValidationServiceGrpcKt
import com.acme.identity.v1.getUserRulesRequest
import com.acme.identity.v1.normalizeUserRequest
import com.acme.identity.v1.user
import com.acme.identity.v1.userPassport
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.CountLedgerAccountsRequestKt
import com.acme.ledger.v1.LedgerAccountQueryServiceGrpcKt
import com.acme.ledger.v1.copy as ledgerCopy
import com.acme.ledger.v1.countLedgerAccountsRequest
import com.acme.ledger.v1.resolveLedgerAccountsRequest
import com.acme.maintenance.v1.RecalculateWorkOrdersRequestKt
import com.acme.maintenance.v1.WorkOrderCalculationServiceGrpcKt
import com.acme.maintenance.v1.recalculateWorkOrdersRequest
import com.acme.maintenance.v1.workOrder
import com.acme.metrics.v1.CheckMetricSeriesEligibilityRequestKt
import com.acme.metrics.v1.CountMetricSeriesesRequest
import com.acme.metrics.v1.CountMetricSeriesesRequestKt
import com.acme.metrics.v1.MetricSeriesQueryServiceGrpcKt
import com.acme.metrics.v1.MetricSeriesValidationServiceGrpcKt
import com.acme.metrics.v1.checkMetricSeriesEligibilityRequest
import com.acme.metrics.v1.countMetricSeriesesRequest
import com.acme.metrics.v1.metricSeries
import com.acme.routing.v1.ExplainRoutePlanCalculationRequest
import com.acme.routing.v1.ExplainRoutePlanCalculationRequestKt
import com.acme.routing.v1.ExplainRoutePlanCalculationResponse
import com.acme.routing.v1.RoutePlanCalculationServiceGrpcKt
import com.acme.routing.v1.explainRoutePlanCalculationRequest
import com.acme.routing.v1.routePlan
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.transport.yards.YardsRepository
import com.acme.venues.v1.VenueServiceGrpcKt
import com.acme.venues.v1.createVenueRequest
import com.acme.venues.v1.venue
import com.acme.yards.v1.CountYardMoveTotalsRequest
import com.acme.yards.v1.CountYardMoveTotalsResponse
import com.acme.yards.v1.DeleteYardMoveReportRequest
import com.acme.yards.v1.DeleteYardMoveReportResponse
import com.acme.yards.v1.DockValidationServiceGrpcKt
import com.acme.yards.v1.ExplainYardDecisionRequest
import com.acme.yards.v1.FetchYardMoveReportRequest
import com.acme.yards.v1.FetchYardMoveReportResponse
import com.acme.yards.v1.GenerateYardMoveReportRequest
import com.acme.yards.v1.GenerateYardMoveReportResponse
import com.acme.yards.v1.LintDocksRequest
import com.acme.yards.v1.LintDocksRequestKt
import com.acme.yards.v1.ListYardMoveReportsRequest
import com.acme.yards.v1.ListYardMoveReportsResponse
import com.acme.yards.v1.PreviewDockRequestKt
import com.acme.yards.v1.ScheduleYardMoveReportRequest
import com.acme.yards.v1.ScheduleYardMoveReportResponse
import com.acme.yards.v1.ShareYardMoveReportRequest
import com.acme.yards.v1.ShareYardMoveReportResponse
import com.acme.yards.v1.ShareYardMoveReportResponseKt
import com.acme.yards.v1.StreamYardMoveMetricsRequest
import com.acme.yards.v1.StreamYardMoveMetricsResponse
import com.acme.yards.v1.YardMoveReportingServiceGrpcKt
import com.acme.yards.v1.YardValidationServiceGrpcKt
import com.acme.yards.v1.countYardMoveTotalsResponse
import com.acme.yards.v1.deleteYardMoveReportResponse
import com.acme.yards.v1.dock
import com.acme.yards.v1.explainYardDecisionRequest
import com.acme.yards.v1.fetchYardMoveReportResponse
import com.acme.yards.v1.generateYardMoveReportResponse
import com.acme.yards.v1.lintDocksRequest
import com.acme.yards.v1.listYardMoveReportsResponse
import com.acme.yards.v1.previewDockRequest
import com.acme.yards.v1.scheduleYardMoveReportResponse
import com.acme.yards.v1.scoreYardRequest
import com.acme.yards.v1.shareYardMoveReportResponse
import com.acme.yards.v1.streamYardMoveMetricsResponse
import com.acme.yards.v1.yard
import com.acme.yards.v1.yardMove
import com.google.protobuf.Any as ProtoAny
import com.google.protobuf.Timestamp
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Builds and schedules reports about yard moves. */
class YardMoveReportingController(
    private val store: YardsRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyService: AccessPolicyServiceGrpcKt.AccessPolicyServiceCoroutineStub,
    private val dispatchDispatchAssignmentValidationService: DispatchAssignmentValidationServiceGrpcKt.DispatchAssignmentValidationServiceCoroutineStub,
    private val dispatchZoneCalculationService: ZoneCalculationServiceGrpcKt.ZoneCalculationServiceCoroutineStub,
    private val identityUserValidationService: UserValidationServiceGrpcKt.UserValidationServiceCoroutineStub,
    private val ledgerLedgerAccountQueryService: LedgerAccountQueryServiceGrpcKt.LedgerAccountQueryServiceCoroutineStub,
    private val maintenanceWorkOrderCalculationService: WorkOrderCalculationServiceGrpcKt.WorkOrderCalculationServiceCoroutineStub,
    private val metricsMetricSeriesQueryService: MetricSeriesQueryServiceGrpcKt.MetricSeriesQueryServiceCoroutineStub,
    private val metricsMetricSeriesValidationService: MetricSeriesValidationServiceGrpcKt.MetricSeriesValidationServiceCoroutineStub,
    private val routingRoutePlanCalculationService: RoutePlanCalculationServiceGrpcKt.RoutePlanCalculationServiceCoroutineStub,
    private val venuesVenueService: VenueServiceGrpcKt.VenueServiceCoroutineStub,
    private val yardsDockValidationService: DockValidationServiceGrpcKt.DockValidationServiceCoroutineStub,
    private val yardsYardValidationService: YardValidationServiceGrpcKt.YardValidationServiceCoroutineStub,
) : YardMoveReportingServiceGrpcKt.YardMoveReportingServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(YardMoveReportingController::class.java)

    private val invokeShareYardMoveReport2: suspend (ExplainRoutePlanCalculationRequest) -> ExplainRoutePlanCalculationResponse = { payload ->
        routingRoutePlanCalculationService.explainRoutePlanCalculation(payload)
    }

    /** Counts yard moves by status. */
    override suspend fun countYardMoveTotals(request: CountYardMoveTotalsRequest): CountYardMoveTotalsResponse = handleCountYardMoveTotals(request)

    /** Deletes a yard move. */
    override suspend fun deleteYardMoveReport(request: DeleteYardMoveReportRequest): DeleteYardMoveReportResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveReportingService/DeleteYardMoveReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DeleteYardMoveReport", request)
            log.atInfo().addKeyValue("operation", "DeleteYardMoveReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yardMoveId).log("Recorded audit detail")
            val outbound0 = updateAccessPolicyRequest {
                this.accessPolicy = accessPolicy {
                    this.audit = auditInfo {
                        this.etag = request.etag
                    }
                }
                this.context = request.context
            }
            val reply0 = flowOf(outbound0).map { value -> authorizationAccessPolicyService.updateAccessPolicy(value) }.single()
            val outbound1 = previewDockRequest {
                this.context = request.context
                this.details = PreviewDockRequestKt.details {
                    this.contact = contactInfo {
                        this.email = reply0.accessPolicy.owner.email
                    }
                }
                this.dock = dock {
                    this.audit = auditInfo {
                        this.createdAt = reply0.accessPolicy.audit.createdAt
                    }
                }
            }
            val reply1 = withTimeout(5_000) { yardsDockValidationService.previewDock(outbound1) }
            deleteYardMoveReportResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply0.result.message
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    /** Returns a yard move. */
    override suspend fun fetchYardMoveReport(request: FetchYardMoveReportRequest): FetchYardMoveReportResponse {
        return handleFetchYardMoveReport(request)
    }

    /** Generates a yard move. */
    override suspend fun generateYardMoveReport(request: GenerateYardMoveReportRequest): GenerateYardMoveReportResponse = handleGenerateYardMoveReport(request)

    /** Lists yard moves a page at a time. */
    override suspend fun listYardMoveReports(request: ListYardMoveReportsRequest): ListYardMoveReportsResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveReportingService/ListYardMoveReports") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListYardMoveReports", request)
            log.atInfo().addKeyValue("operation", "ListYardMoveReports").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = request.toNormalizeUserFromListYardMoveReports0()
            val reply0 = identityUserValidationService.normalizeUser(outbound0)
            listYardMoveReportsResponse {
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    /** Schedules a yard move. */
    override suspend fun scheduleYardMoveReport(request: ScheduleYardMoveReportRequest): ScheduleYardMoveReportResponse {
        return handleScheduleYardMoveReport(request)
    }

    /** Shares a yard move. */
    override suspend fun shareYardMoveReport(request: ShareYardMoveReportRequest): ShareYardMoveReportResponse = handleShareYardMoveReport(request)

    /** Streams the changes of yard moves as they happen. */
    override fun streamYardMoveMetrics(request: StreamYardMoveMetricsRequest): Flow<StreamYardMoveMetricsResponse> = flow {
        emit(handleStreamYardMoveMetrics(request))
    }

    private suspend fun handleCountYardMoveTotals(request: CountYardMoveTotalsRequest): CountYardMoveTotalsResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveReportingService/CountYardMoveTotals") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CountYardMoveTotals", request)
            log.atInfo().addKeyValue("operation", "CountYardMoveTotals").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            countYardMoveTotalsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleFetchYardMoveReport(request: FetchYardMoveReportRequest): FetchYardMoveReportResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveReportingService/FetchYardMoveReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "FetchYardMoveReport", request)
            log.atInfo().addKeyValue("operation", "FetchYardMoveReport").log("Handling transport request")
            val configure0: CheckMetricSeriesEligibilityRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.details = CheckMetricSeriesEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                    this.locale = request.context.locale
                }
                this.metricSeries = metricSeries {
                    this.owner = userRef {
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val outbound0 = checkMetricSeriesEligibilityRequest(configure0)
            val reply0 = metricsMetricSeriesValidationService.checkMetricSeriesEligibility(outbound0)
            fetchYardMoveReportResponse {
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
                this.yardMove = yardMove {
                    this.yardMoveId = request.yardMoveId
                }
            }
        }

    private suspend fun handleGenerateYardMoveReport(request: GenerateYardMoveReportRequest): GenerateYardMoveReportResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveReportingService/GenerateYardMoveReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GenerateYardMoveReport", request)
            log.atInfo().addKeyValue("operation", "GenerateYardMoveReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yardMoveId).log("Recorded audit detail")
            val outbound0 = createVenueRequest {
                this.context = request.context
                this.venue = venue {
                    this.owner = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply0 = venuesVenueService.createVenue(outbound0)
            val outbound1 = scoreYardRequest {
                this.context = request.context
                this.yard = yard {
                    this.address = postalAddress {
                        this.countryCode = reply0.venue.address.countryCode
                    }
                }
            }
            val reply1 = yardsYardValidationService.scoreYard(outbound1)
            val outbound2 = countLedgerAccountsRequest {}.ledgerCopy {
                this.context = request.context
                this.filter = CountLedgerAccountsRequestKt.filter {
                    this.createdAfter = reply1.yard.audit.updatedAt
                    this.region = reply1.yard.address.region
                }
            }
            val reply2 = ledgerLedgerAccountQueryService.countLedgerAccounts(outbound2)
            generateYardMoveReportResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply1.result.message
                    this.status = reply2.result.status
                }
            }
        }

    private fun ListYardMoveReportsRequest.toNormalizeUserFromListYardMoveReports0(): NormalizeUserRequest {
        val source = this
        return normalizeUserRequest {
            this.context = source.context
            this.user = user {
                this.contact = contactInfo {
                    this.email = source.context.actor.email
                }
                this.passport = userPassport {
                    this.expiresAt = source.filter.createdAfter
                }
                this.primaryAddress = postalAddress {
                    this.region = source.filter.region
                }
                this.relationshipManager = userRef {
                    this.userId = source.context.actor.userId
                }
            }
        }
    }

    private data class ScheduleYardMoveReport3Command(
        val context: RequestContext,
        val filterRegion: String,
        val filterCreatedAfter: Timestamp
    ) {
        fun toRequest(): CountMetricSeriesesRequest {
            val command = this
            return countMetricSeriesesRequest {
                this.context = command.context
                this.filter = CountMetricSeriesesRequestKt.filter {
                    this.createdAfter = command.filterCreatedAfter
                    this.region = command.filterRegion
                }
            }
        }
    }

    private suspend fun handleScheduleYardMoveReport(request: ScheduleYardMoveReportRequest): ScheduleYardMoveReportResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveReportingService/ScheduleYardMoveReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScheduleYardMoveReport", request)
            log.atInfo().addKeyValue("operation", "ScheduleYardMoveReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yardMoveId).log("Recorded audit detail")
            val outbound0 = recalculateZonesRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.reason = request.reason
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
            val reply0 = dispatchZoneCalculationService.recalculateZones(outbound0)
            val outbound1 = explainYardDecisionRequest {
                this.context = request.context
                this.yard = yard {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.zone.audit.updatedBy
                    }
                    this.owner = userRef {
                        this.userId = reply0.zone.manager.userId
                    }
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = yardsYardValidationService.explainYardDecision(envelope1.unpack(ExplainYardDecisionRequest::class.java))
            val outbound2 = recalculateWorkOrdersRequest {
                this.context = request.context
                this.details = RecalculateWorkOrdersRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = reply1.yard.address.countryCode
                        this.postalCode = reply1.yard.address.postalCode
                    }
                }
                this.workOrder = workOrder {
                    this.requester = contactInfo {
                        this.email = reply1.yard.owner.email
                    }
                }
            }
            val reply2 = maintenanceWorkOrderCalculationService.recalculateWorkOrders(WireCopies.json(outbound2))
            val outbound3 = ScheduleYardMoveReport3Command(
                context = request.context,
                filterRegion = reply0.zone.location.region,
                filterCreatedAfter = reply2.workOrder.audit.updatedAt
            ).toRequest()
            val reply3 = metricsMetricSeriesQueryService.countMetricSerieses(outbound3)
            scheduleYardMoveReportResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.details.putAll(reply2.result.detailsMap)
                    this.message = reply1.result.message
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleShareYardMoveReport(request: ShareYardMoveReportRequest): ShareYardMoveReportResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveReportingService/ShareYardMoveReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ShareYardMoveReport", request)
            log.atInfo().addKeyValue("operation", "ShareYardMoveReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.templateId).log("Recorded audit detail")
            val outbound0 = lintDocksRequest {
                this.context = request.context
                this.details = LintDocksRequestKt.details {
                    this.contact = contactInfo {
                        this.name = request.details.recipient.name
                        this.phone = request.details.recipient.phone
                        this.preferredChannel = request.details.recipient.preferredChannel
                    }
                }
                this.dock = dock {
                    this.owner = userRef {
                        this.email = request.details.recipient.email
                    }
                }
            }
            val command0: Submission<LintDocksRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = yardsDockValidationService.lintDocks(payload0)
            val outbound1 = getUserRulesRequest {
                this.context = request.context
                this.userId = request.context.actor.userId
            }
            val adapter1 = OutboundPort<GetUserRulesRequest, GetUserRulesResponse> { payload ->
                identityUserValidationService.getUserRules(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            val outbound2 = explainRoutePlanCalculationRequest {
                this.context = request.context
                this.details = ExplainRoutePlanCalculationRequestKt.details {
                    this.contact = contactInfo {
                        this.preferredChannel = reply1.user.contact.preferredChannel
                    }
                }
                this.routePlan = routePlan {
                    this.audit = auditInfo {
                        this.createdAt = reply0.dock.audit.createdAt
                        this.updatedBy = reply0.dock.audit.updatedBy
                    }
                    this.counterparty = driverRef {
                        this.displayName = reply0.dock.owner.displayName
                    }
                }
            }
            val reply2 = invokeShareYardMoveReport2(outbound2)
            val outbound3 = checkDispatchAssignmentEligibilityRequest {
                this.context = request.context
                this.dispatchAssignment = dispatchAssignment {
                    this.audit = auditInfo {
                        this.etag = reply0.dock.audit.etag
                    }
                    this.customer = driverRef {
                        this.driverId = reply2.routePlan.counterparty.driverId
                    }
                }
            }
            val invoke3: suspend (CheckDispatchAssignmentEligibilityRequest, Metadata) -> CheckDispatchAssignmentEligibilityResponse = dispatchDispatchAssignmentValidationService::checkDispatchAssignmentEligibility
            val reply3 = invoke3(outbound3, Metadata())
            shareYardMoveReportResponse {
                this.outcome = ShareYardMoveReportResponseKt.outcome {
                    this.status = reply0.outcome.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.yardMove = yardMove {
                    this.assignee = userRef {
                        this.displayName = reply2.routePlan.counterparty.displayName
                    }
                    this.audit = auditInfo {
                        this.createdBy = reply0.dock.audit.createdBy
                    }
                    this.requester = contactInfo {
                        this.name = request.details.recipient.name
                        this.phone = reply1.user.contact.phone
                    }
                    this.yardMoveId = request.yardMoveId
                }
            }
        }

    private suspend fun handleStreamYardMoveMetrics(request: StreamYardMoveMetricsRequest): StreamYardMoveMetricsResponse =
        RpcBoundary.run("acme.yards.v1.YardMoveReportingService/StreamYardMoveMetrics") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StreamYardMoveMetrics", request)
            log.atInfo().addKeyValue("operation", "StreamYardMoveMetrics").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = PreviewDispatchAssignmentRequest.newBuilder().apply {
                setContext(request.context)
                setDetails(PreviewDispatchAssignmentRequestKt.details {
                    this.contact = contactInfo {
                        this.name = request.subscriber.name
                        this.preferredChannel = request.subscriber.preferredChannel
                    }
                })
                setDispatchAssignment(dispatchAssignment {
                    this.requester = contactInfo {
                        this.email = request.subscriber.email
                        this.phone = request.subscriber.phone
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> dispatchDispatchAssignmentValidationService.previewDispatchAssignment(value) }
            val outbound1 = RequestFactory.create {
                resolveLedgerAccountsRequest {
                    this.context = request.context
                }
            }
            val reply1 = ledgerLedgerAccountQueryService.resolveLedgerAccounts(outbound1)
            streamYardMoveMetricsResponse {
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
                this.yardMove = yardMove {
                    this.assignee = userRef {
                        this.email = request.subscriber.email
                    }
                    this.requester = reply0.dispatchAssignment.requester
                }
            }
        }
}
