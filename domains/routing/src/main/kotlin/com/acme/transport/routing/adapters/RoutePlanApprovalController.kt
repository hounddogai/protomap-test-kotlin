package com.acme.transport.routing.adapters

import com.acme.authorization.v1.AccessPolicyValidationServiceGrpcKt
import com.acme.authorization.v1.NormalizeAccessPolicyResponse
import com.acme.authorization.v1.accessPolicy
import com.acme.authorization.v1.accessPolicyDiscountAction
import com.acme.authorization.v1.normalizeAccessPolicyRequest
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.drivers.v1.CalculateDriverResponse
import com.acme.drivers.v1.DriverCalculationServiceGrpcKt
import com.acme.drivers.v1.DutyStatusCalculationServiceGrpcKt
import com.acme.drivers.v1.DutyStatusValidationServiceGrpcKt
import com.acme.drivers.v1.ExplainDriverCalculationRequestKt
import com.acme.drivers.v1.RecalculateDutyStatusesRequestKt
import com.acme.drivers.v1.ScoreDutyStatusRequestKt
import com.acme.drivers.v1.calculateDriverRequest
import com.acme.drivers.v1.copy as driversCopy
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.dutyStatus
import com.acme.drivers.v1.explainDriverCalculationRequest
import com.acme.drivers.v1.recalculateDutyStatusesRequest
import com.acme.drivers.v1.scoreDutyStatusRequest
import com.acme.identity.v1.userRef
import com.acme.performance.v1.ReviewQueryServiceGrpcKt
import com.acme.performance.v1.SummarizeReviewsRequestKt
import com.acme.performance.v1.summarizeReviewsRequest
import com.acme.routing.v1.ApproveRoutePlanRequest
import com.acme.routing.v1.ApproveRoutePlanResponse
import com.acme.routing.v1.CommentRoutePlanApprovalRequest
import com.acme.routing.v1.CommentRoutePlanApprovalResponse
import com.acme.routing.v1.CommentRoutePlanApprovalResponseKt
import com.acme.routing.v1.DelegateRoutePlanApprovalRequest
import com.acme.routing.v1.DelegateRoutePlanApprovalResponse
import com.acme.routing.v1.DelegateRoutePlanApprovalResponseKt
import com.acme.routing.v1.EscalateRoutePlanRequest
import com.acme.routing.v1.EscalateRoutePlanResponse
import com.acme.routing.v1.EscalateRoutePlanResponseKt
import com.acme.routing.v1.GetRoutePlanApprovalStatusRequest
import com.acme.routing.v1.GetRoutePlanApprovalStatusResponse
import com.acme.routing.v1.ListPendingRoutePlansRequest
import com.acme.routing.v1.ListPendingRoutePlansResponse
import com.acme.routing.v1.RejectRoutePlanRequest
import com.acme.routing.v1.RejectRoutePlanResponse
import com.acme.routing.v1.RejectRoutePlanResponseKt
import com.acme.routing.v1.Route
import com.acme.routing.v1.RoutePlanApprovalServiceGrpcKt
import com.acme.routing.v1.RoutePlanAttachment
import com.acme.routing.v1.RoutePlanCalculationServiceGrpcKt
import com.acme.routing.v1.RoutePlanLifecycleServiceGrpcKt
import com.acme.routing.v1.RoutePlanLine
import com.acme.routing.v1.RouteServiceGrpcKt
import com.acme.routing.v1.RouteWorkflowServiceGrpcKt
import com.acme.routing.v1.SignalRouteWorkflowResponse
import com.acme.routing.v1.StartRouteWorkflowResponse
import com.acme.routing.v1.StopApprovalServiceGrpcKt
import com.acme.routing.v1.SubmitRoutePlanRequest
import com.acme.routing.v1.SubmitRoutePlanResponse
import com.acme.routing.v1.TerminateRoutePlanRequest
import com.acme.routing.v1.TerminateRoutePlanRequestKt
import com.acme.routing.v1.approveRoutePlanResponse
import com.acme.routing.v1.commentRoutePlanApprovalResponse
import com.acme.routing.v1.compareRoutePlanOptionsRequest
import com.acme.routing.v1.createRouteRequest
import com.acme.routing.v1.delegateRoutePlanApprovalResponse
import com.acme.routing.v1.escalateRoutePlanResponse
import com.acme.routing.v1.getRoutePlanApprovalStatusResponse
import com.acme.routing.v1.listPendingRoutePlansResponse
import com.acme.routing.v1.pauseRouteWorkflowRequest
import com.acme.routing.v1.rejectRoutePlanResponse
import com.acme.routing.v1.route
import com.acme.routing.v1.routeAttachment
import com.acme.routing.v1.routeBankSettlement
import com.acme.routing.v1.routeCardSettlement
import com.acme.routing.v1.routeLine
import com.acme.routing.v1.routeLineTax
import com.acme.routing.v1.routePlan
import com.acme.routing.v1.routePlanCardSettlement
import com.acme.routing.v1.signalRouteWorkflowRequest
import com.acme.routing.v1.startRouteWorkflowRequest
import com.acme.routing.v1.stop
import com.acme.routing.v1.stopParticipant
import com.acme.routing.v1.submitRoutePlanResponse
import com.acme.routing.v1.submitStopRequest
import com.acme.routing.v1.terminateRoutePlanRequest
import com.acme.telematics.v1.GeoFenceApprovalServiceGrpcKt
import com.acme.telematics.v1.commentGeoFenceApprovalRequest
import com.acme.telematics.v1.geoFence
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.transport.routing.RoutingRepository
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

/** Routes route plans through approval, with escalation and delegation. */
class RoutePlanApprovalController(
    private val store: RoutingRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyValidationService: AccessPolicyValidationServiceGrpcKt.AccessPolicyValidationServiceCoroutineStub,
    private val driversDriverCalculationService: DriverCalculationServiceGrpcKt.DriverCalculationServiceCoroutineStub,
    private val driversDutyStatusCalculationService: DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineStub,
    private val driversDutyStatusValidationService: DutyStatusValidationServiceGrpcKt.DutyStatusValidationServiceCoroutineStub,
    private val performanceReviewQueryService: ReviewQueryServiceGrpcKt.ReviewQueryServiceCoroutineStub,
    private val routingRoutePlanCalculationService: RoutePlanCalculationServiceGrpcKt.RoutePlanCalculationServiceCoroutineStub,
    private val routingRoutePlanLifecycleService: RoutePlanLifecycleServiceGrpcKt.RoutePlanLifecycleServiceCoroutineStub,
    private val routingRouteService: RouteServiceGrpcKt.RouteServiceCoroutineStub,
    private val routingRouteWorkflowService: RouteWorkflowServiceGrpcKt.RouteWorkflowServiceCoroutineStub,
    private val routingStopApprovalService: StopApprovalServiceGrpcKt.StopApprovalServiceCoroutineStub,
    private val telematicsGeoFenceApprovalService: GeoFenceApprovalServiceGrpcKt.GeoFenceApprovalServiceCoroutineStub,
) : RoutePlanApprovalServiceGrpcKt.RoutePlanApprovalServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(RoutePlanApprovalController::class.java)

    private val clientForCommentRoutePlanApproval0 by lazy { routingStopApprovalService }

    /** Approves a route plan. */
    override suspend fun approveRoutePlan(request: ApproveRoutePlanRequest): ApproveRoutePlanResponse = handleApproveRoutePlan(request)

    /** Comments a route plan. */
    override suspend fun commentRoutePlanApproval(request: CommentRoutePlanApprovalRequest): CommentRoutePlanApprovalResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanApprovalService/CommentRoutePlanApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CommentRoutePlanApproval", request)
            log.atInfo().addKeyValue("operation", "CommentRoutePlanApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.routePlan.routePlanId).log("Recorded audit detail")
            val outbound0 = submitStopRequest {
                this.context = request.context
                this.stop = stop {
                    this.participants.addAll(listOf(stopParticipant {
                        this.contact = contactInfo {
                            this.email = request.routePlan.counterparty.email
                        }
                    }))
                }
            }
            val reply0 = clientForCommentRoutePlanApproval0.submitStop(outbound0)
            val outbound1 = pauseRouteWorkflowRequest {
                this.context = request.context
                this.route = route {
                    this.bank = routeBankSettlement {
                        this.bic = request.routePlan.bank.bic
                    }
                    this.billingAddress = postalAddress {
                        this.countryCode = request.routePlan.billingAddress.countryCode
                        this.lines.addAll(request.routePlan.billingAddress.linesList)
                        this.recipient = request.routePlan.billingAddress.recipient
                    }
                }
            }
            val reply1 = flowOf(outbound1).map { value -> routingRouteWorkflowService.pauseRouteWorkflow(value) }.single()
            commentRoutePlanApprovalResponse {
                this.outcome = CommentRoutePlanApprovalResponseKt.outcome {
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = "ok"
                }
                this.routePlan = routePlan {
                    this.counterparty = request.routePlan.counterparty
                    this.routePlanId = request.routePlanId
                }
            }
        }

    /** Delegates a route plan. */
    override suspend fun delegateRoutePlanApproval(request: DelegateRoutePlanApprovalRequest): DelegateRoutePlanApprovalResponse {
        return handleDelegateRoutePlanApproval(request)
    }

    /** Escalates a route plan. */
    override suspend fun escalateRoutePlan(request: EscalateRoutePlanRequest): EscalateRoutePlanResponse = handleEscalateRoutePlan(request)

    /** Returns a route plan. */
    override suspend fun getRoutePlanApprovalStatus(request: GetRoutePlanApprovalStatusRequest): GetRoutePlanApprovalStatusResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanApprovalService/GetRoutePlanApprovalStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetRoutePlanApprovalStatus", request)
            log.atInfo().addKeyValue("operation", "GetRoutePlanApprovalStatus").log("Handling transport request")
            val outbound0 = compareRoutePlanOptionsRequest {
                this.annotations.putAll(request.context.baggageMap)
                this.context = request.context
                this.routePlan = routePlan {
                    this.audit = auditInfo {
                        this.updatedAt = request.context.deadline
                    }
                    this.counterparty = driverRef {
                        this.email = request.context.actor.email
                    }
                }
                this.routePlanId = request.routePlanId
            }
            val reply0 = routingRoutePlanCalculationService.compareRoutePlanOptions(outbound0)
            getRoutePlanApprovalStatusResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.routePlan = routePlan {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.routePlan.audit.updatedAt
                    }
                    this.counterparty = driverRef {
                        this.displayName = reply0.routePlan.counterparty.displayName
                    }
                    this.routePlanId = request.routePlanId
                }
            }
        }

    /** Lists route plans a page at a time. */
    override suspend fun listPendingRoutePlans(request: ListPendingRoutePlansRequest): ListPendingRoutePlansResponse {
        return handleListPendingRoutePlans(request)
    }

    /** Rejects a route plan. */
    override suspend fun rejectRoutePlan(request: RejectRoutePlanRequest): RejectRoutePlanResponse = handleRejectRoutePlan(request)

    /** Submits a route plan. */
    override suspend fun submitRoutePlan(request: SubmitRoutePlanRequest): SubmitRoutePlanResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanApprovalService/SubmitRoutePlan") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SubmitRoutePlan", request)
            log.atInfo().addKeyValue("operation", "SubmitRoutePlan").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.routePlan.routePlanId).log("Recorded audit detail")
            var reply0 = NormalizeAccessPolicyResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.routePlan.linesList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound0 = normalizeAccessPolicyRequest {
                                this.accessPolicy = accessPolicy {
                                    this.audit = auditInfo {
                                        this.createdBy = request.routePlan.audit.createdBy
                                    }
                                    this.description = item.description
                                    this.discount = accessPolicyDiscountAction {
                                        this.amount = item.tax.amount
                                    }
                                    this.owner = userRef {
                                        this.displayName = request.details.approver.displayName
                                    }
                                }
                                this.context = request.context
                            }
                            authorizationAccessPolicyValidationService.normalizeAccessPolicy(outbound0)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply0 = it }
                }
            }
            var reply1 = SignalRouteWorkflowResponse.getDefaultInstance()
            request.routePlan.linesList.asFlow().map { item ->
                val outbound1 = signalRouteWorkflowRequest {
                    this.context = request.context
                    this.route = route {
                        this.lines.addAll(listOf(routeLine {
                            this.amount = item.tax.amount
                            this.tax = routeLineTax {
                                this.exemptions.addAll(item.tax.exemptionsList)
                                this.jurisdiction = item.tax.jurisdiction
                            }
                        }))
                    }
                }
                routingRouteWorkflowService.signalRouteWorkflow(outbound1)
            }.collect { reply1 = it }
            val outbound2 = createRouteRequest {
                this.context = request.context
                this.route = route {
                    this.card = routeCardSettlement {
                        this.holderName = when (reply1.route.settlementCase) {
                            Route.SettlementCase.CARD -> reply1.route.card.holderName
                            else -> ""
                        }
                    }
                    this.counterparty = driverRef {
                        this.displayName = request.details.approver.displayName
                    }
                }
            }
            val reply2 = routingRouteService.createRoute(outbound2)
            var reply3 = CalculateDriverResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<RoutePlanLine>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound3 = calculateDriverRequest {
                            this.context = request.context
                            this.driver = driver {
                                this.attributes.putAll(item.attributesMap)
                                this.audit = auditInfo {
                                    this.createdAt = reply0.accessPolicy.audit.createdAt
                                    this.updatedBy = reply2.route.audit.updatedBy
                                }
                            }
                        }
                        reply3 = driversDriverCalculationService.calculateDriver(outbound3)
                    }
                }
                try {
                    request.routePlan.linesList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            submitRoutePlanResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.routePlan = routePlan {
                    this.billingAddress = postalAddress {
                        this.locality = reply2.route.billingAddress.locality
                    }
                    this.counterparty = reply2.route.counterparty
                    this.number = request.routePlan.number
                    this.routePlanId = request.routePlanId
                }
            }
        }

    private suspend fun handleApproveRoutePlan(request: ApproveRoutePlanRequest): ApproveRoutePlanResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanApprovalService/ApproveRoutePlan") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ApproveRoutePlan", request)
            log.atInfo().addKeyValue("operation", "ApproveRoutePlan").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.routePlan.routePlanId).log("Recorded audit detail")
            val outbound0 = summarizeReviewsRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.details = SummarizeReviewsRequestKt.details {
                    this.filters.putAll(request.routePlan.metadataMap)
                }
                this.reason = request.reason
            }
            val reply0 = performanceReviewQueryService.summarizeReviews(WireCopies.reflective(outbound0))
            var reply1 = StartRouteWorkflowResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<RoutePlanAttachment>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound1 = startRouteWorkflowRequest {
                            this.context = request.context
                            this.route = route {
                                this.attachments.addAll(listOf(routeAttachment {
                                    this.checksum = item.checksum
                                    this.contentType = item.contentType
                                    this.fileName = item.fileName
                                    this.uri = item.uri
                                }))
                            }
                        }
                        reply1 = routingRouteWorkflowService.startRouteWorkflow(outbound1)
                    }
                }
                try {
                    request.routePlan.attachmentsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            approveRoutePlanResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
                this.routePlan = routePlan {
                    this.routePlanId = request.routePlanId
                    this.status = request.routePlan.status
                }
            }
        }

    private suspend fun handleDelegateRoutePlanApproval(request: DelegateRoutePlanApprovalRequest): DelegateRoutePlanApprovalResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanApprovalService/DelegateRoutePlanApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DelegateRoutePlanApproval", request)
            log.atInfo().addKeyValue("operation", "DelegateRoutePlanApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.routePlan.routePlanId).log("Recorded audit detail")
            val outbound0 = compareRoutePlanOptionsRequest {
                this.context = request.context
                this.routePlan = routePlan {
                    this.audit = auditInfo {
                        this.etag = request.routePlan.audit.etag
                    }
                    this.card = routePlanCardSettlement {
                        this.token = request.routePlan.card.token
                    }
                    this.counterparty = driverRef {
                        this.driverId = request.routePlan.counterparty.driverId
                        this.email = request.details.approver.email
                    }
                }
            }
            val reply0 = withTimeout(5_000) { routingRoutePlanCalculationService.compareRoutePlanOptions(outbound0) }
            val configure1: ExplainDriverCalculationRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.driver = driver {
                    this.primaryAddress = postalAddress {
                        this.recipient = request.routePlan.billingAddress.recipient
                    }
                    this.relationshipManager = userRef {
                        this.email = reply0.routePlan.counterparty.email
                    }
                }
            }
            val outbound1 = explainDriverCalculationRequest(configure1)
            val reply1 = driversDriverCalculationService.explainDriverCalculation(outbound1)
            delegateRoutePlanApprovalResponse {
                this.outcome = DelegateRoutePlanApprovalResponseKt.outcome {
                    this.nextSteps.addAll(reply1.outcome.nextStepsList)
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.routePlan = routePlan {
                    this.audit = auditInfo {
                        this.etag = reply0.routePlan.audit.etag
                    }
                    this.billingAddress = request.routePlan.billingAddress
                    this.routePlanId = request.routePlanId
                }
            }
        }

    private suspend fun handleEscalateRoutePlan(request: EscalateRoutePlanRequest): EscalateRoutePlanResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanApprovalService/EscalateRoutePlan") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EscalateRoutePlan", request)
            log.atInfo().addKeyValue("operation", "EscalateRoutePlan").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.routePlan.routePlanId).log("Recorded audit detail")
            val outbound0 = recalculateDutyStatusesRequest {
                this.context = request.context
                this.details = RecalculateDutyStatusesRequestKt.details {
                    this.destination = postalAddress {
                        this.locality = request.routePlan.billingAddress.locality
                    }
                }
            }
            val reply0 = driversDutyStatusCalculationService.recalculateDutyStatuses(outbound0)
            escalateRoutePlanResponse {
                this.outcome = EscalateRoutePlanResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.correlationId = reply0.dutyStatus.correlationId
                    this.status = "ok"
                }
                this.routePlan = routePlan {
                    this.routePlanId = request.routePlanId
                }
            }
        }

    private suspend fun handleListPendingRoutePlans(request: ListPendingRoutePlansRequest): ListPendingRoutePlansResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanApprovalService/ListPendingRoutePlans") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListPendingRoutePlans", request)
            log.atInfo().addKeyValue("operation", "ListPendingRoutePlans").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = scoreDutyStatusRequest {}.driversCopy {
                this.context = request.context
                this.details = ScoreDutyStatusRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.filter.region
                    }
                }
                this.dutyStatus = dutyStatus {
                    this.attributes.putAll(request.filter.labelsMap)
                    this.occurredAt = request.filter.createdAfter
                }
            }
            val reply0 = driversDutyStatusValidationService.scoreDutyStatus(outbound0)
            listPendingRoutePlansResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private fun RejectRoutePlanRequest.toTerminateRoutePlanFromRejectRoutePlan0(): TerminateRoutePlanRequest {
        val source = this
        return terminateRoutePlanRequest {
            this.context = source.context
            this.details = TerminateRoutePlanRequestKt.details {
                this.newOwner = userRef {
                    this.displayName = source.routePlan.counterparty.displayName
                }
            }
            this.routePlanId = source.routePlanId
        }
    }

    private suspend fun handleRejectRoutePlan(request: RejectRoutePlanRequest): RejectRoutePlanResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanApprovalService/RejectRoutePlan") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RejectRoutePlan", request)
            log.atInfo().addKeyValue("operation", "RejectRoutePlan").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.routePlan.routePlanId).log("Recorded audit detail")
            val outbound0 = request.toTerminateRoutePlanFromRejectRoutePlan0()
            val reply0 = routingRoutePlanLifecycleService.terminateRoutePlan(outbound0)
            val outbound1 = commentGeoFenceApprovalRequest {
                this.context = request.context
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.createdBy = reply0.routePlan.audit.createdBy
                    }
                    this.owner = userRef {
                        this.userId = request.details.approver.userId
                    }
                }
            }
            val reply1 = telematicsGeoFenceApprovalService.commentGeoFenceApproval(outbound1)
            rejectRoutePlanResponse {
                this.outcome = RejectRoutePlanResponseKt.outcome {
                    this.referenceId = reply0.outcome.referenceId
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = "ok"
                }
                this.routePlan = routePlan {
                    this.billingAddress = postalAddress {
                        this.lines.addAll(reply0.routePlan.billingAddress.linesList)
                    }
                    this.dueAt = request.routePlan.dueAt
                    this.routePlanId = request.routePlanId
                }
            }
        }
}
