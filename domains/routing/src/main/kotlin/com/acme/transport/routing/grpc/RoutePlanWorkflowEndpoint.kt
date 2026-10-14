package com.acme.transport.routing.grpc

import com.acme.audit.v1.AuditEventQueryServiceGrpcKt
import com.acme.audit.v1.ResolveAuditEventsRequest
import com.acme.audit.v1.resolveAuditEventsRequest
import com.acme.authorization.v1.AccessPolicyQueryServiceGrpcKt
import com.acme.authorization.v1.summarizeAccessPoliciesRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.DelegateProofOfDeliveryApprovalRequest
import com.acme.deliveries.v1.DelegateProofOfDeliveryApprovalResponse
import com.acme.deliveries.v1.ProofOfDeliveryApprovalServiceGrpcKt
import com.acme.deliveries.v1.delegateProofOfDeliveryApprovalRequest
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.drivers.v1.CalculateDriverLicenseRequest
import com.acme.drivers.v1.CalculateDutyStatusRequestKt
import com.acme.drivers.v1.DriverLicenseCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverQueryServiceGrpcKt
import com.acme.drivers.v1.DutyStatusCalculationServiceGrpcKt
import com.acme.drivers.v1.EstimateDriverLicenseRequest
import com.acme.drivers.v1.RecalculateDutyStatusesRequestKt
import com.acme.drivers.v1.ResolveDriversRequest
import com.acme.drivers.v1.calculateDriverLicenseRequest
import com.acme.drivers.v1.calculateDutyStatusRequest
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverLicenseBankSettlement
import com.acme.drivers.v1.driverLicenseRef
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.dutyStatus
import com.acme.drivers.v1.estimateDriverLicenseRequest
import com.acme.drivers.v1.explainDutyStatusCalculationRequest
import com.acme.drivers.v1.recalculateDutyStatusesRequest
import com.acme.drivers.v1.resolveDriversRequest
import com.acme.identity.v1.CloneUserRequestKt
import com.acme.identity.v1.UserServiceGrpcKt
import com.acme.identity.v1.cloneUserRequest
import com.acme.identity.v1.user
import com.acme.identity.v1.userRef
import com.acme.notifications.v1.NotificationValidationServiceGrpcKt
import com.acme.notifications.v1.notification
import com.acme.notifications.v1.previewNotificationRequest
import com.acme.notifications.v1.scoreNotificationRequest
import com.acme.performance.v1.BrowseReviewsRequestKt
import com.acme.performance.v1.BrowseReviewsResponse
import com.acme.performance.v1.ReviewQueryServiceGrpcKt
import com.acme.performance.v1.browseReviewsRequest
import com.acme.routing.v1.AdvanceRoutePlanWorkflowRequest
import com.acme.routing.v1.AdvanceRoutePlanWorkflowResponse
import com.acme.routing.v1.CancelRoutePlanWorkflowRequest
import com.acme.routing.v1.CancelRoutePlanWorkflowResponse
import com.acme.routing.v1.InspectRoutePlanWorkflowRequest
import com.acme.routing.v1.InspectRoutePlanWorkflowResponse
import com.acme.routing.v1.ListRoutePlanWorkflowsRequest
import com.acme.routing.v1.ListRoutePlanWorkflowsResponse
import com.acme.routing.v1.PauseRoutePlanWorkflowRequest
import com.acme.routing.v1.PauseRoutePlanWorkflowResponse
import com.acme.routing.v1.PreviewRoutePlanResponse
import com.acme.routing.v1.QuoteStopRequest
import com.acme.routing.v1.QuoteStopResponse
import com.acme.routing.v1.ResumeRoutePlanWorkflowRequest
import com.acme.routing.v1.ResumeRoutePlanWorkflowResponse
import com.acme.routing.v1.RetryRoutePlanWorkflowRequest
import com.acme.routing.v1.RetryRoutePlanWorkflowResponse
import com.acme.routing.v1.RoutePlanApprovalServiceGrpcKt
import com.acme.routing.v1.RoutePlanLine
import com.acme.routing.v1.RoutePlanValidationServiceGrpcKt
import com.acme.routing.v1.RoutePlanWorkflowServiceGrpcKt
import com.acme.routing.v1.RouteWorkflowServiceGrpcKt
import com.acme.routing.v1.SignalRoutePlanWorkflowRequest
import com.acme.routing.v1.SignalRoutePlanWorkflowResponse
import com.acme.routing.v1.SimulateStopRequest
import com.acme.routing.v1.SimulateStopRequestKt
import com.acme.routing.v1.SimulateStopResponse
import com.acme.routing.v1.StartRoutePlanWorkflowRequest
import com.acme.routing.v1.StartRoutePlanWorkflowResponse
import com.acme.routing.v1.StartRoutePlanWorkflowResponseKt
import com.acme.routing.v1.StopApprovalServiceGrpcKt
import com.acme.routing.v1.StopCalculationServiceGrpcKt
import com.acme.routing.v1.SubmitStopRequest
import com.acme.routing.v1.SubmitStopResponse
import com.acme.routing.v1.advanceRoutePlanWorkflowResponse
import com.acme.routing.v1.approveRoutePlanRequest
import com.acme.routing.v1.cancelRoutePlanWorkflowResponse
import com.acme.routing.v1.inspectRoutePlanWorkflowResponse
import com.acme.routing.v1.listRoutePlanWorkflowsResponse
import com.acme.routing.v1.pauseRoutePlanWorkflowResponse
import com.acme.routing.v1.previewRoutePlanRequest
import com.acme.routing.v1.quoteStopRequest
import com.acme.routing.v1.resumeRoutePlanWorkflowResponse
import com.acme.routing.v1.retryRoutePlanWorkflowResponse
import com.acme.routing.v1.retryRouteWorkflowRequest
import com.acme.routing.v1.route
import com.acme.routing.v1.routePlan
import com.acme.routing.v1.routePlanBankSettlement
import com.acme.routing.v1.routePlanLine
import com.acme.routing.v1.routePlanLineTax
import com.acme.routing.v1.signalRoutePlanWorkflowResponse
import com.acme.routing.v1.simulateStopRequest
import com.acme.routing.v1.startRoutePlanWorkflowResponse
import com.acme.routing.v1.stop
import com.acme.routing.v1.stopParticipant
import com.acme.routing.v1.submitStopRequest
import com.acme.search.v1.SearchIndexServiceGrpcKt
import com.acme.search.v1.searchIndex
import com.acme.search.v1.updateSearchIndexRequest
import com.acme.telematics.v1.GeoFenceWorkflowServiceGrpcKt
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.signalGeoFenceWorkflowRequest
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.transport.routing.RoutingRepository
import com.google.protobuf.Any as ProtoAny
import com.google.protobuf.Timestamp
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

/** Runs the long-lived workflow that takes a route plan from start to finish. */
class RoutePlanWorkflowEndpoint(
    private val store: RoutingRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventQueryService: AuditEventQueryServiceGrpcKt.AuditEventQueryServiceCoroutineStub,
    private val authorizationAccessPolicyQueryService: AccessPolicyQueryServiceGrpcKt.AccessPolicyQueryServiceCoroutineStub,
    private val deliveriesProofOfDeliveryApprovalService: ProofOfDeliveryApprovalServiceGrpcKt.ProofOfDeliveryApprovalServiceCoroutineStub,
    private val driversDriverLicenseCalculationService: DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub,
    private val driversDriverQueryService: DriverQueryServiceGrpcKt.DriverQueryServiceCoroutineStub,
    private val driversDutyStatusCalculationService: DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineStub,
    private val identityUserService: UserServiceGrpcKt.UserServiceCoroutineStub,
    private val notificationsNotificationValidationService: NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub,
    private val performanceReviewQueryService: ReviewQueryServiceGrpcKt.ReviewQueryServiceCoroutineStub,
    private val routingRoutePlanApprovalService: RoutePlanApprovalServiceGrpcKt.RoutePlanApprovalServiceCoroutineStub,
    private val routingRoutePlanValidationService: RoutePlanValidationServiceGrpcKt.RoutePlanValidationServiceCoroutineStub,
    private val routingRouteWorkflowService: RouteWorkflowServiceGrpcKt.RouteWorkflowServiceCoroutineStub,
    private val routingStopApprovalService: StopApprovalServiceGrpcKt.StopApprovalServiceCoroutineStub,
    private val routingStopCalculationService: StopCalculationServiceGrpcKt.StopCalculationServiceCoroutineStub,
    private val searchSearchIndexService: SearchIndexServiceGrpcKt.SearchIndexServiceCoroutineStub,
    private val telematicsGeoFenceWorkflowService: GeoFenceWorkflowServiceGrpcKt.GeoFenceWorkflowServiceCoroutineStub,
) : RoutePlanWorkflowServiceGrpcKt.RoutePlanWorkflowServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(RoutePlanWorkflowEndpoint::class.java)

    private val invokeRetryRoutePlanWorkflow2: suspend (SimulateStopRequest) -> SimulateStopResponse = { payload ->
        routingStopCalculationService.simulateStop(payload)
    }

    private val workerForStartRoutePlanWorkflow1 = ReplyMailbox<DelegateProofOfDeliveryApprovalRequest, DelegateProofOfDeliveryApprovalResponse>(workers) { payload ->
        withTimeout(5_000) { deliveriesProofOfDeliveryApprovalService.delegateProofOfDeliveryApproval(payload) }
    }

    private val clientForStartRoutePlanWorkflow2 by lazy { notificationsNotificationValidationService }

    /** Advances a route plan. */
    override suspend fun advanceRoutePlanWorkflow(request: AdvanceRoutePlanWorkflowRequest): AdvanceRoutePlanWorkflowResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanWorkflowService/AdvanceRoutePlanWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "AdvanceRoutePlanWorkflow", request)
            log.atInfo().addKeyValue("operation", "AdvanceRoutePlanWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.routePlan.routePlanId).log("Recorded audit detail")
            val outbound0 = signalGeoFenceWorkflowRequest {
                this.context = request.context
                this.geoFence = geoFence {
                    this.owner = userRef {
                        this.displayName = request.routePlan.counterparty.displayName
                    }
                }
            }
            val reply0 = flowOf(outbound0).map { value -> telematicsGeoFenceWorkflowService.signalGeoFenceWorkflow(value) }.single()
            val outbound1 = updateSearchIndexRequest {
                this.context = request.context
                this.searchIndex = searchIndex {
                    this.address = postalAddress {
                        this.countryCode = request.routePlan.billingAddress.countryCode
                    }
                    this.audit = auditInfo {
                        this.createdAt = request.routePlan.audit.createdAt
                    }
                    this.owner = userRef {
                        this.displayName = request.routePlan.counterparty.displayName
                    }
                }
            }
            val reply1 = withTimeout(5_000) { searchSearchIndexService.updateSearchIndex(outbound1) }
            val configure2: CloneUserRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.user = user {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.geoFence.audit.updatedAt
                    }
                    this.contact = contactInfo {
                        this.name = request.details.notify.name
                    }
                    this.primaryAddress = postalAddress {
                        this.recipient = request.routePlan.billingAddress.recipient
                    }
                    this.relationshipManager = userRef {
                        this.displayName = reply1.searchIndex.owner.displayName
                    }
                }
            }
            val outbound2 = cloneUserRequest(configure2)
            val reply2 = identityUserService.cloneUser(outbound2)
            advanceRoutePlanWorkflowResponse {
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.status = "ok"
                }
                this.routePlan = routePlan {
                    this.audit = auditInfo {
                        this.updatedAt = reply2.user.audit.updatedAt
                    }
                    this.billingAddress = postalAddress {
                        this.countryCode = reply1.searchIndex.address.countryCode
                    }
                    this.lines.addAll(listOf(routePlanLine {
                        this.description = reply0.geoFence.description
                    }))
                    this.routePlanId = request.routePlanId
                    this.total = request.routePlan.total
                }
            }
        }

    /** Cancels a route plan. */
    override suspend fun cancelRoutePlanWorkflow(request: CancelRoutePlanWorkflowRequest): CancelRoutePlanWorkflowResponse {
        return handleCancelRoutePlanWorkflow(request)
    }

    /** Returns a route plan. */
    override suspend fun inspectRoutePlanWorkflow(request: InspectRoutePlanWorkflowRequest): InspectRoutePlanWorkflowResponse = handleInspectRoutePlanWorkflow(request)

    /** Lists route plans a page at a time. */
    override suspend fun listRoutePlanWorkflows(request: ListRoutePlanWorkflowsRequest): ListRoutePlanWorkflowsResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanWorkflowService/ListRoutePlanWorkflows") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListRoutePlanWorkflows", request)
            log.atInfo().addKeyValue("operation", "ListRoutePlanWorkflows").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            listRoutePlanWorkflowsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Pauses a route plan. */
    override suspend fun pauseRoutePlanWorkflow(request: PauseRoutePlanWorkflowRequest): PauseRoutePlanWorkflowResponse {
        return handlePauseRoutePlanWorkflow(request)
    }

    /** Resumes a route plan. */
    override suspend fun resumeRoutePlanWorkflow(request: ResumeRoutePlanWorkflowRequest): ResumeRoutePlanWorkflowResponse = handleResumeRoutePlanWorkflow(request)

    /** Retrys a route plan. */
    override suspend fun retryRoutePlanWorkflow(request: RetryRoutePlanWorkflowRequest): RetryRoutePlanWorkflowResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanWorkflowService/RetryRoutePlanWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RetryRoutePlanWorkflow", request)
            log.atInfo().addKeyValue("operation", "RetryRoutePlanWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.routePlan.routePlanId).log("Recorded audit detail")
            var reply0 = PreviewRoutePlanResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<RoutePlanLine>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound0 = previewRoutePlanRequest {
                            this.context = request.context
                            this.routePlan = routePlan {
                                this.lines.addAll(listOf(routePlanLine {
                                    this.tax = routePlanLineTax {
                                        this.jurisdiction = item.tax.jurisdiction
                                    }
                                }))
                            }
                        }
                        reply0 = routingRoutePlanValidationService.previewRoutePlan(outbound0)
                    }
                }
                try {
                    request.routePlan.linesList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound1 = quoteStopRequest {
                this.context = request.context
                this.stop = stop {
                    this.audit = auditInfo {
                        this.createdAt = reply0.routePlan.audit.createdAt
                    }
                    this.participants.addAll(listOf(stopParticipant {
                        this.contact = contactInfo {
                            this.email = reply0.routePlan.counterparty.email
                        }
                    }))
                }
            }
            val adapter1 = OutboundPort<QuoteStopRequest, QuoteStopResponse> { payload ->
                routingStopCalculationService.quoteStop(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            val outbound2 = simulateStopRequest {
                this.context = request.context
                this.details = SimulateStopRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(reply1.stop.location.linesList)
                    }
                }
                this.stop = stop {
                    this.participants.addAll(listOf(stopParticipant {
                        this.contact = contactInfo {
                            this.email = request.details.notify.email
                            this.preferredChannel = request.details.notify.preferredChannel
                        }
                    }))
                }
            }
            val reply2 = invokeRetryRoutePlanWorkflow2(outbound2)
            val outbound3 = submitStopRequest {
                this.context = request.context
                this.stop = stop {
                    this.participants.addAll(listOf(stopParticipant {
                        this.contact = contactInfo {
                            this.name = request.details.notify.name
                            this.phone = request.details.notify.phone
                        }
                    }))
                }
            }
            val invoke3: suspend (SubmitStopRequest, Metadata) -> SubmitStopResponse = routingStopApprovalService::submitStop
            val reply3 = invoke3(outbound3, Metadata())
            retryRoutePlanWorkflowResponse {
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = reply3.outcome.status
                }
                this.routePlan = routePlan {
                    this.billingAddress = postalAddress {
                        this.lines.addAll(reply2.stop.location.linesList)
                        this.recipient = reply1.stop.location.recipient
                    }
                    this.notes = request.routePlan.notes
                    this.routePlanId = request.routePlanId
                }
            }
        }

    /** Signals a route plan. */
    override suspend fun signalRoutePlanWorkflow(request: SignalRoutePlanWorkflowRequest): SignalRoutePlanWorkflowResponse {
        return handleSignalRoutePlanWorkflow(request)
    }

    /** Starts a route plan. */
    override suspend fun startRoutePlanWorkflow(request: StartRoutePlanWorkflowRequest): StartRoutePlanWorkflowResponse = handleStartRoutePlanWorkflow(request)

    private suspend fun handleCancelRoutePlanWorkflow(request: CancelRoutePlanWorkflowRequest): CancelRoutePlanWorkflowResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanWorkflowService/CancelRoutePlanWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CancelRoutePlanWorkflow", request)
            log.atInfo().addKeyValue("operation", "CancelRoutePlanWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.routePlan.routePlanId).log("Recorded audit detail")
            val outbound0 = retryRouteWorkflowRequest {
                this.context = request.context
                this.route = route {
                    this.billingAddress = postalAddress {
                        this.postalCode = request.routePlan.billingAddress.postalCode
                    }
                }
            }
            val reply0 = routingRouteWorkflowService.retryRouteWorkflow(outbound0)
            cancelRoutePlanWorkflowResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.routePlan = routePlan {
                    this.billingAddress = postalAddress {
                        this.locality = reply0.route.billingAddress.locality
                    }
                    this.routePlanId = request.routePlanId
                    this.status = request.routePlan.status
                }
            }
        }

    private suspend fun handleInspectRoutePlanWorkflow(request: InspectRoutePlanWorkflowRequest): InspectRoutePlanWorkflowResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanWorkflowService/InspectRoutePlanWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "InspectRoutePlanWorkflow", request)
            log.atInfo().addKeyValue("operation", "InspectRoutePlanWorkflow").log("Handling transport request")
            val outbound0 = scoreNotificationRequest {
                this.context = request.context
                this.notification = notification {
                    this.assignee = userRef {
                        this.userId = request.context.actor.userId
                    }
                    this.requester = contactInfo {
                        this.email = request.context.actor.email
                    }
                    this.subject = request.routePlanId
                }
            }
            val reply0 = notificationsNotificationValidationService.scoreNotification(outbound0)
            inspectRoutePlanWorkflowResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.routePlan = routePlan {
                    this.audit = auditInfo {
                        this.createdAt = reply0.notification.audit.createdAt
                    }
                    this.counterparty = driverRef {
                        this.email = reply0.notification.assignee.email
                    }
                    this.routePlanId = request.routePlanId
                }
            }
        }

    private fun PauseRoutePlanWorkflowRequest.toResolveDriversFromPauseRoutePlanWorkflow1(): ResolveDriversRequest {
        val source = this
        return resolveDriversRequest {
            this.context = source.context
        }
    }

    private suspend fun handlePauseRoutePlanWorkflow(request: PauseRoutePlanWorkflowRequest): PauseRoutePlanWorkflowResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanWorkflowService/PauseRoutePlanWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PauseRoutePlanWorkflow", request)
            log.atInfo().addKeyValue("operation", "PauseRoutePlanWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.routePlan.routePlanId).log("Recorded audit detail")
            var reply0 = BrowseReviewsResponse.getDefaultInstance()
            request.routePlan.linesList.asFlow().map { item ->
                val outbound0 = browseReviewsRequest {
                    this.context = request.context
                    this.filter = BrowseReviewsRequestKt.filter {
                        this.createdAfter = request.routePlan.audit.createdAt
                        this.labels.putAll(item.attributesMap)
                        this.ownerId = item.lineId
                        this.region = request.routePlan.billingAddress.region
                    }
                }
                performanceReviewQueryService.browseReviews(outbound0)
            }.collect { reply0 = it }
            val outbound1 = request.toResolveDriversFromPauseRoutePlanWorkflow1()
            val reply1 = driversDriverQueryService.resolveDrivers(outbound1)
            pauseRoutePlanWorkflowResponse {
                this.result = operationResult {
                    this.status = reply1.result.status
                }
                this.routePlan = routePlan {
                    this.routePlanId = request.routePlanId
                    this.total = request.routePlan.total
                }
            }
        }

    private data class ResumeRoutePlanWorkflow3Command(
        val context: RequestContext,
        val driverLicenseBillingAddressLines: List<String>,
        val driverLicenseBillingAddressLocality: String,
        val driverLicenseBankAccountHolder: String,
        val driverLicenseAuditCreatedAt: Timestamp
    ) {
        fun toRequest(): EstimateDriverLicenseRequest {
            val command = this
            return estimateDriverLicenseRequest {
                this.context = command.context
                this.driverLicense = driverLicense {
                    this.audit = auditInfo {
                        this.createdAt = command.driverLicenseAuditCreatedAt
                    }
                    this.bank = driverLicenseBankSettlement {
                        this.accountHolder = command.driverLicenseBankAccountHolder
                    }
                    this.billingAddress = postalAddress {
                        this.lines.addAll(command.driverLicenseBillingAddressLines)
                        this.locality = command.driverLicenseBillingAddressLocality
                    }
                }
            }
        }
    }

    private suspend fun handleResumeRoutePlanWorkflow(request: ResumeRoutePlanWorkflowRequest): ResumeRoutePlanWorkflowResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanWorkflowService/ResumeRoutePlanWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResumeRoutePlanWorkflow", request)
            log.atInfo().addKeyValue("operation", "ResumeRoutePlanWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.routePlan.routePlanId).log("Recorded audit detail")
            val outbound0 = summarizeAccessPoliciesRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
            }
            val reply0 = authorizationAccessPolicyQueryService.summarizeAccessPolicies(outbound0)
            val outbound1 = calculateDriverLicenseRequest {
                this.context = request.context
                this.driverLicense = driverLicense {
                    this.billingAddress = postalAddress {
                        this.recipient = request.routePlan.billingAddress.recipient
                        this.region = request.routePlan.billingAddress.region
                    }
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = driversDriverLicenseCalculationService.calculateDriverLicense(envelope1.unpack(CalculateDriverLicenseRequest::class.java))
            val outbound2 = recalculateDutyStatusesRequest {
                this.context = request.context
                this.details = RecalculateDutyStatusesRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = request.routePlan.billingAddress.countryCode
                        this.lines.addAll(reply1.driverLicense.billingAddress.linesList)
                    }
                }
                this.dutyStatus = dutyStatus {
                    this.subject = driverLicenseRef {
                        this.displayName = request.routePlan.counterparty.displayName
                    }
                }
            }
            val reply2 = driversDutyStatusCalculationService.recalculateDutyStatuses(WireCopies.json(outbound2))
            val outbound3 = ResumeRoutePlanWorkflow3Command(
                context = request.context,
                driverLicenseBillingAddressLines = request.routePlan.billingAddress.linesList,
                driverLicenseBillingAddressLocality = reply1.driverLicense.billingAddress.locality,
                driverLicenseBankAccountHolder = reply1.driverLicense.bank.accountHolder,
                driverLicenseAuditCreatedAt = reply1.driverLicense.audit.createdAt
            ).toRequest()
            val reply3 = driversDriverLicenseCalculationService.estimateDriverLicense(outbound3)
            resumeRoutePlanWorkflowResponse {
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply2.result.message
                    this.status = "ok"
                }
                this.routePlan = routePlan {
                    this.billingAddress = reply3.driverLicense.billingAddress
                    this.counterparty = driverRef {
                        this.displayName = reply1.driverLicense.counterparty.displayName
                    }
                    this.metadata.putAll(request.routePlan.metadataMap)
                    this.routePlanId = request.routePlanId
                }
            }
        }

    private suspend fun handleSignalRoutePlanWorkflow(request: SignalRoutePlanWorkflowRequest): SignalRoutePlanWorkflowResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanWorkflowService/SignalRoutePlanWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SignalRoutePlanWorkflow", request)
            log.atInfo().addKeyValue("operation", "SignalRoutePlanWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.routePlan.routePlanId).log("Recorded audit detail")
            val outbound0 = ResolveAuditEventsRequest.newBuilder().apply {
                setContext(request.context)
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> auditAuditEventQueryService.resolveAuditEvents(value) }
            val outbound1 = RequestFactory.create {
                approveRoutePlanRequest {
                    this.context = request.context
                    this.routePlan = routePlan {
                        this.bank = routePlanBankSettlement {
                            this.bic = request.routePlan.bank.bic
                        }
                        this.billingAddress = postalAddress {
                            this.countryCode = request.routePlan.billingAddress.countryCode
                        }
                    }
                }
            }
            val reply1 = routingRoutePlanApprovalService.approveRoutePlan(outbound1)
            val outbound2 = explainDutyStatusCalculationRequest {
                this.context = request.context
                this.dutyStatus = dutyStatus {
                    this.subject = driverLicenseRef {
                        this.displayName = request.routePlan.counterparty.displayName
                        this.number = reply1.routePlan.number
                    }
                }
            }
            val reply2 = ClientRegistry.lookup("acme.drivers.v1.DutyStatusCalculationService", DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineStub::class.java).explainDutyStatusCalculation(outbound2)
            signalRoutePlanWorkflowResponse {
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
                this.routePlan = routePlan {
                    this.audit = auditInfo {
                        this.updatedBy = reply1.routePlan.audit.updatedBy
                    }
                    this.billingAddress = reply1.routePlan.billingAddress
                    this.number = request.routePlan.number
                    this.routePlanId = request.routePlanId
                }
            }
        }

    private suspend fun handleStartRoutePlanWorkflow(request: StartRoutePlanWorkflowRequest): StartRoutePlanWorkflowResponse =
        RpcBoundary.run("acme.routing.v1.RoutePlanWorkflowService/StartRoutePlanWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StartRoutePlanWorkflow", request)
            log.atInfo().addKeyValue("operation", "StartRoutePlanWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.routePlan.routePlanId).log("Recorded audit detail")
            val outbound0 = calculateDutyStatusRequest {
                this.context = request.context
                this.details = CalculateDutyStatusRequestKt.details {
                    this.destination = postalAddress {
                        this.locality = request.routePlan.billingAddress.locality
                    }
                }
                this.dutyStatus = dutyStatus {
                    this.subject = driverLicenseRef {
                        this.number = request.routePlan.number
                    }
                }
            }
            val reply0 = driversDutyStatusCalculationService.calculateDutyStatus(WireCopies.reflective(outbound0))
            val outbound1 = delegateProofOfDeliveryApprovalRequest {
                this.context = request.context
                this.proofOfDelivery = proofOfDelivery {
                    this.billingAddress = postalAddress {
                        this.region = request.routePlan.billingAddress.region
                    }
                }
            }
            val reply1 = workerForStartRoutePlanWorkflow1.submit(outbound1)
            val outbound2 = previewNotificationRequest {
                this.context = request.context
                this.notification = notification {
                    this.audit = auditInfo {
                        this.updatedAt = request.routePlan.audit.updatedAt
                    }
                }
            }
            val reply2 = clientForStartRoutePlanWorkflow2.previewNotification(outbound2)
            startRoutePlanWorkflowResponse {
                this.outcome = StartRoutePlanWorkflowResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.correlationId = reply0.dutyStatus.correlationId
                    this.status = reply1.outcome.status
                }
                this.routePlan = routePlan {
                    this.counterparty = driverRef {
                        this.email = reply2.notification.requester.email
                    }
                    this.dueAt = reply2.notification.sla.dueAt
                    this.routePlanId = request.routePlanId
                    this.total = request.routePlan.total
                }
            }
        }
}
