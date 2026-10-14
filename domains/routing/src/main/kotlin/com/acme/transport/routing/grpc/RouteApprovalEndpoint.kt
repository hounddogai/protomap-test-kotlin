package com.acme.transport.routing.grpc

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.drivers.v1.DriverLicenseValidationServiceGrpcKt
import com.acme.drivers.v1.DriverValidationServiceGrpcKt
import com.acme.drivers.v1.DutyStatusCalculationServiceGrpcKt
import com.acme.drivers.v1.DutyStatusValidationServiceGrpcKt
import com.acme.drivers.v1.EstimateDutyStatusRequestKt
import com.acme.drivers.v1.NormalizeDriverLicenseRequest
import com.acme.drivers.v1.NormalizeDriverRequestKt
import com.acme.drivers.v1.ValidateDriverResponse
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverLicenseRef
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.dutyStatus
import com.acme.drivers.v1.estimateDutyStatusRequest
import com.acme.drivers.v1.normalizeDriverLicenseRequest
import com.acme.drivers.v1.normalizeDriverRequest
import com.acme.drivers.v1.previewDutyStatusRequest
import com.acme.drivers.v1.validateDriverRequest
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.ListPartWorkflowsRequest
import com.acme.maintenance.v1.ListPartWorkflowsRequestKt
import com.acme.maintenance.v1.ListPartWorkflowsResponse
import com.acme.maintenance.v1.PartWorkflowServiceGrpcKt
import com.acme.maintenance.v1.listPartWorkflowsRequest
import com.acme.routing.v1.ApproveRouteRequest
import com.acme.routing.v1.ApproveRouteResponse
import com.acme.routing.v1.ApproveRouteResponseKt
import com.acme.routing.v1.CalculateStopRequest
import com.acme.routing.v1.CalculateStopRequestKt
import com.acme.routing.v1.CheckStopEligibilityRequest
import com.acme.routing.v1.CommentRouteApprovalRequest
import com.acme.routing.v1.CommentRouteApprovalResponse
import com.acme.routing.v1.CommentRouteApprovalResponseKt
import com.acme.routing.v1.CompareStopOptionsRequestKt
import com.acme.routing.v1.DelegateRouteApprovalRequest
import com.acme.routing.v1.DelegateRouteApprovalResponse
import com.acme.routing.v1.DelegateRouteApprovalResponseKt
import com.acme.routing.v1.DeleteRouteRequest
import com.acme.routing.v1.DeleteRouteResponse
import com.acme.routing.v1.EscalateRouteRequest
import com.acme.routing.v1.EscalateRouteResponse
import com.acme.routing.v1.ExplainStopDecisionRequest
import com.acme.routing.v1.ExplainStopDecisionResponse
import com.acme.routing.v1.GetRouteApprovalStatusRequest
import com.acme.routing.v1.GetRouteApprovalStatusResponse
import com.acme.routing.v1.ListPendingRoutesRequest
import com.acme.routing.v1.ListPendingRoutesResponse
import com.acme.routing.v1.RecallRouteRequest
import com.acme.routing.v1.RecallRouteResponse
import com.acme.routing.v1.RejectRouteRequest
import com.acme.routing.v1.RejectRouteResponse
import com.acme.routing.v1.RejectRouteResponseKt
import com.acme.routing.v1.RouteApprovalServiceGrpcKt
import com.acme.routing.v1.RouteServiceGrpcKt
import com.acme.routing.v1.ScoreStopRequestKt
import com.acme.routing.v1.SimulateStopRequestKt
import com.acme.routing.v1.Stop
import com.acme.routing.v1.StopCalculationServiceGrpcKt
import com.acme.routing.v1.StopValidationServiceGrpcKt
import com.acme.routing.v1.SubmitRouteRequest
import com.acme.routing.v1.SubmitRouteResponse
import com.acme.routing.v1.SubmitRouteResponseKt
import com.acme.routing.v1.approveRouteResponse
import com.acme.routing.v1.calculateStopRequest
import com.acme.routing.v1.checkStopEligibilityRequest
import com.acme.routing.v1.commentRouteApprovalResponse
import com.acme.routing.v1.compareStopOptionsRequest
import com.acme.routing.v1.delegateRouteApprovalResponse
import com.acme.routing.v1.deleteRouteRequest
import com.acme.routing.v1.escalateRouteResponse
import com.acme.routing.v1.explainStopDecisionRequest
import com.acme.routing.v1.getRouteApprovalStatusResponse
import com.acme.routing.v1.listPendingRoutesResponse
import com.acme.routing.v1.quoteStopRequest
import com.acme.routing.v1.recallRouteResponse
import com.acme.routing.v1.rejectRouteResponse
import com.acme.routing.v1.route
import com.acme.routing.v1.routeLine
import com.acme.routing.v1.scoreStopRequest
import com.acme.routing.v1.simulateStopRequest
import com.acme.routing.v1.stop
import com.acme.routing.v1.stopDailyRule
import com.acme.routing.v1.stopParticipant
import com.acme.routing.v1.stopWeeklyRule
import com.acme.routing.v1.submitRouteResponse
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.transport.routing.RoutingRepository
import com.acme.yards.v1.DockWorkflowServiceGrpcKt
import com.acme.yards.v1.dock
import com.acme.yards.v1.startDockWorkflowRequest
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Routes routes through approval, with escalation and delegation. */
class RouteApprovalEndpoint(
    private val store: RoutingRepository,
    private val workers: CoroutineScope,
    private val driversDriverLicenseValidationService: DriverLicenseValidationServiceGrpcKt.DriverLicenseValidationServiceCoroutineStub,
    private val driversDriverValidationService: DriverValidationServiceGrpcKt.DriverValidationServiceCoroutineStub,
    private val driversDutyStatusCalculationService: DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineStub,
    private val driversDutyStatusValidationService: DutyStatusValidationServiceGrpcKt.DutyStatusValidationServiceCoroutineStub,
    private val maintenancePartWorkflowService: PartWorkflowServiceGrpcKt.PartWorkflowServiceCoroutineStub,
    private val routingRouteService: RouteServiceGrpcKt.RouteServiceCoroutineStub,
    private val routingStopCalculationService: StopCalculationServiceGrpcKt.StopCalculationServiceCoroutineStub,
    private val routingStopValidationService: StopValidationServiceGrpcKt.StopValidationServiceCoroutineStub,
    private val yardsDockWorkflowService: DockWorkflowServiceGrpcKt.DockWorkflowServiceCoroutineStub,
) : RouteApprovalServiceGrpcKt.RouteApprovalServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(RouteApprovalEndpoint::class.java)

    private val invokeCommentRouteApproval3: suspend (ExplainStopDecisionRequest) -> ExplainStopDecisionResponse = { payload ->
        routingStopValidationService.explainStopDecision(payload)
    }

    private val clientForRecallRoute0 by lazy { routingStopCalculationService }

    /** Approves a route. */
    override suspend fun approveRoute(request: ApproveRouteRequest): ApproveRouteResponse =
        RpcBoundary.run("acme.routing.v1.RouteApprovalService/ApproveRoute") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ApproveRoute", request)
            log.atInfo().addKeyValue("operation", "ApproveRoute").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.route.routeId).log("Recorded audit detail")
            val outbound0 = quoteStopRequest {
                this.context = request.context
                this.stop = stop {
                    this.audit = auditInfo {
                        this.etag = request.route.audit.etag
                    }
                    this.location = postalAddress {
                        this.lines.addAll(request.route.billingAddress.linesList)
                        this.locality = request.route.billingAddress.locality
                    }
                    this.participants.addAll(listOf(stopParticipant {
                        this.contact = contactInfo {
                            this.email = request.details.approver.email
                        }
                    }))
                }
            }
            val reply0 = routingStopCalculationService.quoteStop(WireCopies.json(outbound0))
            approveRouteResponse {
                this.outcome = ApproveRouteResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.status = reply0.result.status
                }
                this.route = route {
                    this.billingAddress = request.route.billingAddress
                    this.routeId = request.routeId
                }
            }
        }

    /** Comments a route. */
    override suspend fun commentRouteApproval(request: CommentRouteApprovalRequest): CommentRouteApprovalResponse {
        return handleCommentRouteApproval(request)
    }

    /** Delegates a route. */
    override suspend fun delegateRouteApproval(request: DelegateRouteApprovalRequest): DelegateRouteApprovalResponse = handleDelegateRouteApproval(request)

    /** Escalates a route. */
    override suspend fun escalateRoute(request: EscalateRouteRequest): EscalateRouteResponse =
        RpcBoundary.run("acme.routing.v1.RouteApprovalService/EscalateRoute") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EscalateRoute", request)
            log.atInfo().addKeyValue("operation", "EscalateRoute").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.route.routeId).log("Recorded audit detail")
            val outbound0 = normalizeDriverRequest {
                this.context = request.context
                this.details = NormalizeDriverRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.route.counterparty.email
                    }
                }
                this.driver = driver {
                    this.primaryAddress = postalAddress {
                        this.lines.addAll(request.route.billingAddress.linesList)
                        this.postalCode = request.route.billingAddress.postalCode
                    }
                    this.relationshipManager = userRef {
                        this.displayName = request.route.counterparty.displayName
                    }
                }
            }
            val reply0 = ClientRegistry.lookup("acme.drivers.v1.DriverValidationService", DriverValidationServiceGrpcKt.DriverValidationServiceCoroutineStub::class.java).normalizeDriver(outbound0)
            escalateRouteResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
                this.route = route {
                    this.counterparty = driverRef {
                        this.email = reply0.driver.contact.email
                    }
                    this.routeId = request.routeId
                    this.total = request.route.total
                }
            }
        }

    /** Returns a route. */
    override suspend fun getRouteApprovalStatus(request: GetRouteApprovalStatusRequest): GetRouteApprovalStatusResponse {
        return handleGetRouteApprovalStatus(request)
    }

    /** Lists routes a page at a time. */
    override suspend fun listPendingRoutes(request: ListPendingRoutesRequest): ListPendingRoutesResponse = handleListPendingRoutes(request)

    /** Recalls a route. */
    override suspend fun recallRoute(request: RecallRouteRequest): RecallRouteResponse =
        RpcBoundary.run("acme.routing.v1.RouteApprovalService/RecallRoute") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecallRoute", request)
            log.atInfo().addKeyValue("operation", "RecallRoute").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.route.routeId).log("Recorded audit detail")
            val outbound0 = compareStopOptionsRequest {
                this.context = request.context
                this.details = CompareStopOptionsRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = request.route.billingAddress.countryCode
                    }
                }
                this.stop = stop {
                    this.location = postalAddress {
                        this.locality = request.route.billingAddress.locality
                    }
                    this.organizer = userRef {
                        this.email = request.route.counterparty.email
                    }
                }
            }
            val reply0 = clientForRecallRoute0.compareStopOptions(outbound0)
            var reply1 = ValidateDriverResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.route.linesList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound1 = validateDriverRequest {
                                this.context = request.context
                                this.driver = driver {
                                    this.attributes.putAll(item.attributesMap)
                                    this.primaryAddress = postalAddress {
                                        this.locality = reply0.stop.location.locality
                                    }
                                }
                            }
                            driversDriverValidationService.validateDriver(outbound1)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply1 = it }
                }
            }
            recallRouteResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.route = route {
                    this.audit = auditInfo {
                        this.etag = reply0.stop.audit.etag
                    }
                    this.billingAddress = postalAddress {
                        this.recipient = reply0.stop.location.recipient
                    }
                    this.notes = request.route.notes
                    this.routeId = request.routeId
                }
            }
        }

    /** Rejects a route. */
    override suspend fun rejectRoute(request: RejectRouteRequest): RejectRouteResponse {
        return handleRejectRoute(request)
    }

    /** Submits a route. */
    override suspend fun submitRoute(request: SubmitRouteRequest): SubmitRouteResponse = handleSubmitRoute(request)

    private data class CommentRouteApproval0Command(
        val context: RequestContext,
        val driverLicenseBillingAddressRecipient: String,
        val driverLicenseCounterpartyEmail: String,
        val driverLicenseBillingAddressLocality: String,
        val driverLicenseAuditUpdatedBy: String
    ) {
        fun toRequest(): NormalizeDriverLicenseRequest {
            val command = this
            return normalizeDriverLicenseRequest {
                this.context = command.context
                this.driverLicense = driverLicense {
                    this.audit = auditInfo {
                        this.updatedBy = command.driverLicenseAuditUpdatedBy
                    }
                    this.billingAddress = postalAddress {
                        this.locality = command.driverLicenseBillingAddressLocality
                        this.recipient = command.driverLicenseBillingAddressRecipient
                    }
                    this.counterparty = driverRef {
                        this.email = command.driverLicenseCounterpartyEmail
                    }
                }
            }
        }
    }

    private suspend fun handleCommentRouteApproval(request: CommentRouteApprovalRequest): CommentRouteApprovalResponse =
        RpcBoundary.run("acme.routing.v1.RouteApprovalService/CommentRouteApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CommentRouteApproval", request)
            log.atInfo().addKeyValue("operation", "CommentRouteApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.route.routeId).log("Recorded audit detail")
            val outbound0 = CommentRouteApproval0Command(
                context = request.context,
                driverLicenseBillingAddressRecipient = request.route.billingAddress.recipient,
                driverLicenseCounterpartyEmail = request.details.approver.email,
                driverLicenseBillingAddressLocality = request.route.billingAddress.locality,
                driverLicenseAuditUpdatedBy = request.route.audit.updatedBy
            ).toRequest()
            val reply0 = driversDriverLicenseValidationService.normalizeDriverLicense(outbound0)
            val outbound1 = checkStopEligibilityRequest {
                this.context = request.context
                this.stop = stop {
                    this.audit = auditInfo {
                        this.createdAt = reply0.driverLicense.audit.createdAt
                    }
                    this.organizer = userRef {
                        this.displayName = request.details.approver.displayName
                    }
                    this.participants.addAll(listOf(stopParticipant {
                        this.contact = contactInfo {
                            this.email = request.route.counterparty.email
                        }
                    }))
                }
            }
            val command1: Submission<CheckStopEligibilityRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = routingStopValidationService.checkStopEligibility(payload1)
            val outbound2 = listPartWorkflowsRequest {
                this.context = request.context
                this.filter = ListPartWorkflowsRequestKt.filter {
                    this.createdAfter = reply0.driverLicense.audit.updatedAt
                    this.ownerId = reply0.driverLicense.counterparty.driverId
                    this.region = reply0.driverLicense.billingAddress.region
                }
            }
            val adapter2 = OutboundPort<ListPartWorkflowsRequest, ListPartWorkflowsResponse> { payload ->
                maintenancePartWorkflowService.listPartWorkflows(payload)
            }
            val port2 = DelegatingPort(adapter2)
            val reply2 = port2.execute(outbound2)
            val outbound3 = explainStopDecisionRequest {
                this.context = request.context
                this.stop = stop {
                    this.daily = stopDailyRule {
                        this.interval = reply1.stop.daily.interval
                    }
                    this.location = postalAddress {
                        this.countryCode = reply0.driverLicense.billingAddress.countryCode
                    }
                    this.participants.addAll(listOf(stopParticipant {
                        this.contact = contactInfo {
                            this.email = reply1.stop.organizer.email
                        }
                    }))
                }
            }
            val reply3 = invokeCommentRouteApproval3(outbound3)
            commentRouteApprovalResponse {
                this.outcome = CommentRouteApprovalResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.message = reply2.result.message
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = reply2.result.status
                }
                this.route = route {
                    this.billingAddress = postalAddress {
                        this.recipient = reply1.stop.location.recipient
                    }
                    this.number = reply0.driverLicense.number
                    this.routeId = request.routeId
                }
            }
        }

    private suspend fun handleDelegateRouteApproval(request: DelegateRouteApprovalRequest): DelegateRouteApprovalResponse =
        RpcBoundary.run("acme.routing.v1.RouteApprovalService/DelegateRouteApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DelegateRouteApproval", request)
            log.atInfo().addKeyValue("operation", "DelegateRouteApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.route.counterparty.driverId).log("Recorded audit detail")
            val outbound0 = deleteRouteRequest {
                this.context = request.context
                this.etag = request.route.audit.etag
                this.reason = request.reason
                this.routeId = request.route.routeId
            }
            val invoke0: suspend (DeleteRouteRequest, Metadata) -> DeleteRouteResponse = routingRouteService::deleteRoute
            val reply0 = invoke0(outbound0, Metadata())
            val outbound1 = CalculateStopRequest.newBuilder().apply {
                setContext(request.context)
                setDetails(CalculateStopRequestKt.details {
                    this.destination = postalAddress {
                        this.recipient = request.route.billingAddress.recipient
                    }
                })
                setStop(stop {
                    this.organizer = userRef {
                        this.displayName = request.route.counterparty.displayName
                        this.email = request.route.counterparty.email
                    }
                    this.participants.addAll(listOf(stopParticipant {
                        this.contact = contactInfo {
                            this.email = request.details.approver.email
                        }
                    }))
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> routingStopCalculationService.calculateStop(value) }
            val outbound2 = RequestFactory.create {
                previewDutyStatusRequest {
                    this.context = request.context
                    this.dutyStatus = dutyStatus {
                        this.subject = driverLicenseRef {
                            this.displayName = request.route.counterparty.displayName
                            this.number = request.route.number
                        }
                    }
                }
            }
            val reply2 = driversDutyStatusValidationService.previewDutyStatus(outbound2)
            delegateRouteApprovalResponse {
                this.outcome = DelegateRouteApprovalResponseKt.outcome {
                    this.message = reply0.result.message
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.status = reply1.result.status
                }
                this.route = route {
                    this.notes = request.route.notes
                    this.routeId = request.routeId
                }
            }
        }

    private suspend fun handleGetRouteApprovalStatus(request: GetRouteApprovalStatusRequest): GetRouteApprovalStatusResponse =
        RpcBoundary.run("acme.routing.v1.RouteApprovalService/GetRouteApprovalStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetRouteApprovalStatus", request)
            log.atInfo().addKeyValue("operation", "GetRouteApprovalStatus").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            getRouteApprovalStatusResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.route = route {
                    this.routeId = request.routeId
                }
            }
        }

    private suspend fun handleListPendingRoutes(request: ListPendingRoutesRequest): ListPendingRoutesResponse =
        RpcBoundary.run("acme.routing.v1.RouteApprovalService/ListPendingRoutes") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListPendingRoutes", request)
            log.atInfo().addKeyValue("operation", "ListPendingRoutes").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = simulateStopRequest {
                this.context = request.context
                this.details = SimulateStopRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.filter.region
                    }
                }
                this.stop = stop {
                    this.organizer = userRef {
                        this.userId = request.context.actor.userId
                    }
                    this.participants.addAll(listOf(stopParticipant {
                        this.contact = contactInfo {
                            this.email = request.context.actor.email
                        }
                    }))
                }
            }
            val reply0 = routingStopCalculationService.simulateStop(WireCopies.reflective(outbound0))
            val outbound1 = calculateStopRequest {
                this.context = request.context
                this.stop = stop {
                    this.location = postalAddress {
                        this.countryCode = reply0.stop.location.countryCode
                        this.locality = reply0.stop.location.locality
                    }
                    this.participants.addAll(listOf(stopParticipant {
                        this.contact = contactInfo {
                            this.email = reply0.stop.organizer.email
                        }
                    }))
                    this.weekly = stopWeeklyRule {
                        this.interval = when (reply0.stop.recurrenceCase) {
                            Stop.RecurrenceCase.WEEKLY -> reply0.stop.weekly.interval
                            else -> 0
                        }
                    }
                }
            }
            val reply1 = routingStopCalculationService.calculateStop(outbound1)
            listPendingRoutesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.routes.addAll(listOf(route {
                    this.audit = auditInfo {
                        this.etag = reply0.stop.audit.etag
                    }
                    this.counterparty = driverRef {
                        this.email = reply1.stop.organizer.email
                    }
                }))
            }
        }

    private suspend fun handleRejectRoute(request: RejectRouteRequest): RejectRouteResponse =
        RpcBoundary.run("acme.routing.v1.RouteApprovalService/RejectRoute") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RejectRoute", request)
            log.atInfo().addKeyValue("operation", "RejectRoute").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.route.routeId).log("Recorded audit detail")
            val outbound0 = scoreStopRequest {
                this.context = request.context
                this.details = ScoreStopRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = request.route.billingAddress.countryCode
                    }
                }
                this.stop = stop {
                    this.audit = auditInfo {
                        this.updatedBy = request.route.audit.updatedBy
                    }
                    this.location = postalAddress {
                        this.region = request.route.billingAddress.region
                    }
                    this.participants.addAll(listOf(stopParticipant {
                        this.contact = contactInfo {
                            this.email = request.details.approver.email
                        }
                    }))
                }
            }
            val reply0 = withTimeout(5_000) { routingStopValidationService.scoreStop(outbound0) }
            val configure1: EstimateDutyStatusRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.dutyStatus = dutyStatus {
                    this.subject = driverLicenseRef {
                        this.displayName = reply0.stop.organizer.displayName
                    }
                }
            }
            val outbound1 = estimateDutyStatusRequest(configure1)
            val reply1 = driversDutyStatusCalculationService.estimateDutyStatus(outbound1)
            rejectRouteResponse {
                this.outcome = RejectRouteResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.route = route {
                    this.counterparty = driverRef {
                        this.displayName = reply0.stop.organizer.displayName
                    }
                    this.lines.addAll(listOf(routeLine {
                        this.attributes.putAll(reply1.dutyStatus.attributesMap)
                    }))
                    this.metadata.putAll(request.route.metadataMap)
                    this.routeId = request.routeId
                }
            }
        }

    private suspend fun handleSubmitRoute(request: SubmitRouteRequest): SubmitRouteResponse =
        RpcBoundary.run("acme.routing.v1.RouteApprovalService/SubmitRoute") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SubmitRoute", request)
            log.atInfo().addKeyValue("operation", "SubmitRoute").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.route.routeId).log("Recorded audit detail")
            val outbound0 = startDockWorkflowRequest {
                this.context = request.context
                this.dock = dock {
                    this.address = postalAddress {
                        this.countryCode = request.route.billingAddress.countryCode
                    }
                }
            }
            val reply0 = yardsDockWorkflowService.startDockWorkflow(outbound0)
            submitRouteResponse {
                this.outcome = SubmitRouteResponseKt.outcome {
                    this.referenceId = reply0.outcome.referenceId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.route = route {
                    this.billingAddress = postalAddress {
                        this.locality = reply0.dock.address.locality
                    }
                    this.counterparty = request.route.counterparty
                    this.routeId = request.routeId
                }
            }
        }
}
