package com.acme.transport.routing.adapters

import com.acme.authorization.v1.AccessPolicyQueryServiceGrpcKt
import com.acme.authorization.v1.ExportAccessPoliciesRequestKt
import com.acme.authorization.v1.exportAccessPoliciesRequest
import com.acme.catalog.v1.CalculateProductRequestKt
import com.acme.catalog.v1.EstimateProductRequestKt
import com.acme.catalog.v1.Product
import com.acme.catalog.v1.ProductCalculationServiceGrpcKt
import com.acme.catalog.v1.calculateProductRequest
import com.acme.catalog.v1.copy as catalogCopy
import com.acme.catalog.v1.estimateProductRequest
import com.acme.catalog.v1.product
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.devices.v1.ExportManagedDevicesRequestKt
import com.acme.devices.v1.ManagedDeviceQueryServiceGrpcKt
import com.acme.devices.v1.exportManagedDevicesRequest
import com.acme.dispatch.v1.CommentDispatchOrderApprovalRequestKt
import com.acme.dispatch.v1.DispatchOrderApprovalServiceGrpcKt
import com.acme.dispatch.v1.commentDispatchOrderApprovalRequest
import com.acme.documents.v1.DocumentServiceGrpcKt
import com.acme.documents.v1.WatchDocumentsRequestKt
import com.acme.documents.v1.watchDocumentsRequest
import com.acme.drivers.v1.DriverCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverQueryServiceGrpcKt
import com.acme.drivers.v1.DriverValidationServiceGrpcKt
import com.acme.drivers.v1.SuggestDriversRequestKt
import com.acme.drivers.v1.ValidateDriverRequestKt
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.estimateDriverRequest
import com.acme.drivers.v1.suggestDriversRequest
import com.acme.drivers.v1.validateDriverRequest
import com.acme.featureflags.v1.ExplainFeatureFlagDecisionRequestKt
import com.acme.featureflags.v1.ExplainFeatureFlagDecisionResponse
import com.acme.featureflags.v1.FeatureFlagValidationServiceGrpcKt
import com.acme.featureflags.v1.explainFeatureFlagDecisionRequest
import com.acme.fuel.v1.FuelCardApprovalServiceGrpcKt
import com.acme.fuel.v1.SubmitFuelCardRequestKt
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.submitFuelCardRequest
import com.acme.identity.v1.FindUsersRequest
import com.acme.identity.v1.FindUsersRequestKt
import com.acme.identity.v1.FindUsersResponse
import com.acme.identity.v1.UserQueryServiceGrpcKt
import com.acme.identity.v1.findUsersRequest
import com.acme.identity.v1.userRef
import com.acme.routing.v1.ApproveStopRequest
import com.acme.routing.v1.ApproveStopResponse
import com.acme.routing.v1.ApproveStopResponseKt
import com.acme.routing.v1.BrowseRoutesRequestKt
import com.acme.routing.v1.CloneRouteResponse
import com.acme.routing.v1.CountRoutesRequestKt
import com.acme.routing.v1.DelegateRouteApprovalRequestKt
import com.acme.routing.v1.DelegateRouteApprovalResponse
import com.acme.routing.v1.DelegateStopApprovalRequest
import com.acme.routing.v1.DelegateStopApprovalResponse
import com.acme.routing.v1.DelegateStopApprovalResponseKt
import com.acme.routing.v1.EscalateStopRequest
import com.acme.routing.v1.EscalateStopResponse
import com.acme.routing.v1.EscalateStopResponseKt
import com.acme.routing.v1.GetStopApprovalStatusRequest
import com.acme.routing.v1.GetStopApprovalStatusResponse
import com.acme.routing.v1.ListPendingStopsRequest
import com.acme.routing.v1.ListPendingStopsResponse
import com.acme.routing.v1.RecallStopRequest
import com.acme.routing.v1.RecallStopResponse
import com.acme.routing.v1.RecallStopResponseKt
import com.acme.routing.v1.RejectStopRequest
import com.acme.routing.v1.RejectStopResponse
import com.acme.routing.v1.RouteApprovalServiceGrpcKt
import com.acme.routing.v1.RouteQueryServiceGrpcKt
import com.acme.routing.v1.RouteServiceGrpcKt
import com.acme.routing.v1.StopApprovalServiceGrpcKt
import com.acme.routing.v1.StopParticipant
import com.acme.routing.v1.SubmitStopRequest
import com.acme.routing.v1.SubmitStopResponse
import com.acme.routing.v1.SubmitStopResponseKt
import com.acme.routing.v1.SuggestRoutesRequest
import com.acme.routing.v1.SuggestRoutesRequestKt
import com.acme.routing.v1.approveStopResponse
import com.acme.routing.v1.browseRoutesRequest
import com.acme.routing.v1.cloneRouteRequest
import com.acme.routing.v1.countRoutesRequest
import com.acme.routing.v1.delegateRouteApprovalRequest
import com.acme.routing.v1.delegateStopApprovalResponse
import com.acme.routing.v1.escalateStopResponse
import com.acme.routing.v1.getStopApprovalStatusResponse
import com.acme.routing.v1.listPendingStopsResponse
import com.acme.routing.v1.recallStopResponse
import com.acme.routing.v1.rejectStopResponse
import com.acme.routing.v1.route
import com.acme.routing.v1.routeCardSettlement
import com.acme.routing.v1.stop
import com.acme.routing.v1.stopParticipant
import com.acme.routing.v1.submitStopResponse
import com.acme.routing.v1.suggestRoutesRequest
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.transport.routing.RoutingRepository
import com.acme.yards.v1.AdvanceYardWorkflowRequestKt
import com.acme.yards.v1.StreamYardMoveMetricsRequestKt
import com.acme.yards.v1.YardMoveReportingServiceGrpcKt
import com.acme.yards.v1.YardWorkflowServiceGrpcKt
import com.acme.yards.v1.advanceYardWorkflowRequest
import com.acme.yards.v1.streamYardMoveMetricsRequest
import com.acme.yards.v1.yard
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

/** Routes stops through approval, with escalation and delegation. */
class StopApprovalController(
    private val store: RoutingRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyQueryService: AccessPolicyQueryServiceGrpcKt.AccessPolicyQueryServiceCoroutineStub,
    private val catalogProductCalculationService: ProductCalculationServiceGrpcKt.ProductCalculationServiceCoroutineStub,
    private val devicesManagedDeviceQueryService: ManagedDeviceQueryServiceGrpcKt.ManagedDeviceQueryServiceCoroutineStub,
    private val dispatchDispatchOrderApprovalService: DispatchOrderApprovalServiceGrpcKt.DispatchOrderApprovalServiceCoroutineStub,
    private val documentsDocumentService: DocumentServiceGrpcKt.DocumentServiceCoroutineStub,
    private val driversDriverCalculationService: DriverCalculationServiceGrpcKt.DriverCalculationServiceCoroutineStub,
    private val driversDriverQueryService: DriverQueryServiceGrpcKt.DriverQueryServiceCoroutineStub,
    private val driversDriverValidationService: DriverValidationServiceGrpcKt.DriverValidationServiceCoroutineStub,
    private val featureflagsFeatureFlagValidationService: FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub,
    private val fuelFuelCardApprovalService: FuelCardApprovalServiceGrpcKt.FuelCardApprovalServiceCoroutineStub,
    private val identityUserQueryService: UserQueryServiceGrpcKt.UserQueryServiceCoroutineStub,
    private val routingRouteApprovalService: RouteApprovalServiceGrpcKt.RouteApprovalServiceCoroutineStub,
    private val routingRouteQueryService: RouteQueryServiceGrpcKt.RouteQueryServiceCoroutineStub,
    private val routingRouteService: RouteServiceGrpcKt.RouteServiceCoroutineStub,
    private val yardsYardMoveReportingService: YardMoveReportingServiceGrpcKt.YardMoveReportingServiceCoroutineStub,
    private val yardsYardWorkflowService: YardWorkflowServiceGrpcKt.YardWorkflowServiceCoroutineStub,
) : StopApprovalServiceGrpcKt.StopApprovalServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(StopApprovalController::class.java)

    /** Approves a stop. */
    override suspend fun approveStop(request: ApproveStopRequest): ApproveStopResponse = handleApproveStop(request)

    /** Delegates a stop. */
    override suspend fun delegateStopApproval(request: DelegateStopApprovalRequest): DelegateStopApprovalResponse =
        RpcBoundary.run("acme.routing.v1.StopApprovalService/DelegateStopApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DelegateStopApproval", request)
            log.atInfo().addKeyValue("operation", "DelegateStopApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.stop.stopId).log("Recorded audit detail")
            val outbound0 = FindUsersRequest.newBuilder().apply {
                setContext(request.context)
                setFilter(FindUsersRequestKt.filter {
                    this.createdAfter = request.details.effectiveAt
                    this.ownerId = request.details.approver.userId
                    this.region = request.stop.location.region
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> identityUserQueryService.findUsers(value) }
            val outbound1 = RequestFactory.create {
                validateDriverRequest {
                    this.context = request.context
                    this.details = ValidateDriverRequestKt.details {
                        this.contact = contactInfo {
                            this.email = request.stop.organizer.email
                        }
                    }
                    this.driver = driver {
                        this.primaryAddress = postalAddress {
                            this.countryCode = request.stop.location.countryCode
                        }
                    }
                }
            }
            val reply1 = driversDriverValidationService.validateDriver(outbound1)
            delegateStopApprovalResponse {
                this.outcome = DelegateStopApprovalResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.status = reply0.result.status
                }
                this.stop = stop {
                    this.stopId = request.stopId
                }
            }
        }

    /** Escalates a stop. */
    override suspend fun escalateStop(request: EscalateStopRequest): EscalateStopResponse {
        return handleEscalateStop(request)
    }

    /** Returns a stop. */
    override suspend fun getStopApprovalStatus(request: GetStopApprovalStatusRequest): GetStopApprovalStatusResponse = handleGetStopApprovalStatus(request)

    /** Lists stops a page at a time. */
    override suspend fun listPendingStops(request: ListPendingStopsRequest): ListPendingStopsResponse =
        RpcBoundary.run("acme.routing.v1.StopApprovalService/ListPendingStops") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListPendingStops", request)
            log.atInfo().addKeyValue("operation", "ListPendingStops").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = advanceYardWorkflowRequest {
                this.context = request.context
                this.details = AdvanceYardWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
                this.yard = yard {
                    this.address = postalAddress {
                        this.region = request.filter.region
                    }
                }
            }
            val reply0 = flowOf(outbound0).map { value -> yardsYardWorkflowService.advanceYardWorkflow(value) }.single()
            listPendingStopsResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
                this.stops.addAll(listOf(stop {
                    this.location = postalAddress {
                        this.countryCode = reply0.yard.address.countryCode
                    }
                }))
            }
        }

    /** Recalls a stop. */
    override suspend fun recallStop(request: RecallStopRequest): RecallStopResponse {
        return handleRecallStop(request)
    }

    /** Rejects a stop. */
    override suspend fun rejectStop(request: RejectStopRequest): RejectStopResponse = handleRejectStop(request)

    /** Submits a stop. */
    override suspend fun submitStop(request: SubmitStopRequest): SubmitStopResponse =
        RpcBoundary.run("acme.routing.v1.StopApprovalService/SubmitStop") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SubmitStop", request)
            log.atInfo().addKeyValue("operation", "SubmitStop").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.stop.stopId).log("Recorded audit detail")
            request.stop.participantsList.asFlow().map { item ->
                val outbound0 = exportManagedDevicesRequest {
                    this.context = request.context
                    this.filter = ExportManagedDevicesRequestKt.filter {
                        this.ownerId = item.participantId
                        this.region = request.stop.location.region
                    }
                }
                devicesManagedDeviceQueryService.exportManagedDevices(outbound0).collect { event ->
                    log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
                }
            }.collect()
            val outbound1 = browseRoutesRequest {
                this.context = request.context
                this.filter = BrowseRoutesRequestKt.filter {
                    this.region = request.stop.location.region
                }
            }
            val reply1 = routingRouteQueryService.browseRoutes(WireCopies.json(outbound1))
            submitStopResponse {
                this.outcome = SubmitStopResponseKt.outcome {
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.stop = stop {
                    this.organizer = request.stop.organizer
                    this.stopId = request.stopId
                }
            }
        }

    private suspend fun handleApproveStop(request: ApproveStopRequest): ApproveStopResponse =
        RpcBoundary.run("acme.routing.v1.StopApprovalService/ApproveStop") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ApproveStop", request)
            log.atInfo().addKeyValue("operation", "ApproveStop").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.stop.stopId).log("Recorded audit detail")
            val outbound0 = suggestRoutesRequest {
                this.context = request.context
                this.filter = SuggestRoutesRequestKt.filter {
                    this.region = request.stop.location.region
                }
            }
            val command0: Submission<SuggestRoutesRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = routingRouteQueryService.suggestRoutes(payload0)
            val outbound1 = watchDocumentsRequest {
                this.context = request.context
                this.filter = WatchDocumentsRequestKt.filter {
                    this.createdAfter = request.details.effectiveAt
                    this.region = request.stop.location.region
                }
                this.since = request.stop.audit.createdAt
                this.subscriber = contactInfo {
                    this.email = request.stop.organizer.email
                }
            }
            documentsDocumentService.watchDocuments(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            var reply2 = DelegateRouteApprovalResponse.getDefaultInstance()
            request.stop.participantsList.asFlow().map { item ->
                val outbound2 = delegateRouteApprovalRequest {
                    this.context = request.context
                    this.details = DelegateRouteApprovalRequestKt.details {
                        this.approver = userRef {
                            this.email = item.contact.email
                        }
                    }
                    this.route = route {
                        this.audit = auditInfo {
                            this.etag = request.stop.audit.etag
                        }
                    }
                }
                routingRouteApprovalService.delegateRouteApproval(outbound2)
            }.collect { reply2 = it }
            val outbound3 = findUsersRequest {
                this.context = request.context
                this.filter = FindUsersRequestKt.filter {
                    this.createdAfter = request.stop.audit.createdAt
                    this.ownerId = request.details.approver.userId
                    this.region = reply2.route.billingAddress.region
                }
            }
            val invoke3: suspend (FindUsersRequest, Metadata) -> FindUsersResponse = identityUserQueryService::findUsers
            val reply3 = invoke3(outbound3, Metadata())
            approveStopResponse {
                this.outcome = ApproveStopResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.referenceIds.addAll(reply3.result.referenceIdsList)
                    this.status = "ok"
                }
                this.stop = stop {
                    this.metadata.putAll(request.stop.metadataMap)
                    this.stopId = request.stopId
                }
            }
        }

    private suspend fun handleEscalateStop(request: EscalateStopRequest): EscalateStopResponse =
        RpcBoundary.run("acme.routing.v1.StopApprovalService/EscalateStop") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EscalateStop", request)
            log.atInfo().addKeyValue("operation", "EscalateStop").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.stop.stopId).log("Recorded audit detail")
            var reply0 = ExplainFeatureFlagDecisionResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<StopParticipant>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound0 = explainFeatureFlagDecisionRequest {
                            this.context = request.context
                            this.details = ExplainFeatureFlagDecisionRequestKt.details {
                                this.contact = contactInfo {
                                    this.name = item.contact.name
                                    this.phone = item.contact.phone
                                    this.preferredChannel = item.contact.preferredChannel
                                }
                            }
                        }
                        reply0 = featureflagsFeatureFlagValidationService.explainFeatureFlagDecision(outbound0)
                    }
                }
                try {
                    request.stop.participantsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound1 = submitFuelCardRequest {
                this.context = request.context
                this.details = SubmitFuelCardRequestKt.details {
                    this.approver = userRef {
                        this.displayName = request.stop.organizer.displayName
                    }
                }
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.createdAt = reply0.featureFlag.audit.createdAt
                        this.updatedAt = reply0.featureFlag.audit.updatedAt
                    }
                    this.holder = driverRef {
                        this.displayName = request.details.approver.displayName
                    }
                }
            }
            val reply1 = fuelFuelCardApprovalService.submitFuelCard(WireCopies.reflective(outbound1))
            var reply2 = CloneRouteResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.stop.participantsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound2 = cloneRouteRequest {
                                this.context = request.context
                                this.route = route {
                                    this.card = routeCardSettlement {
                                        this.token = reply1.fuelCard.card.token
                                    }
                                    this.counterparty = driverRef {
                                        this.email = item.contact.email
                                    }
                                }
                            }
                            routingRouteService.cloneRoute(outbound2)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply2 = it }
                }
            }
            escalateStopResponse {
                this.outcome = EscalateStopResponseKt.outcome {
                    this.message = reply1.result.message
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.stop = stop {
                    this.metadata.putAll(request.stop.metadataMap)
                    this.organizer = userRef {
                        this.email = reply1.fuelCard.holder.email
                    }
                    this.stopId = request.stopId
                }
            }
        }

    private suspend fun handleGetStopApprovalStatus(request: GetStopApprovalStatusRequest): GetStopApprovalStatusResponse =
        RpcBoundary.run("acme.routing.v1.StopApprovalService/GetStopApprovalStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetStopApprovalStatus", request)
            log.atInfo().addKeyValue("operation", "GetStopApprovalStatus").log("Handling transport request")
            val outbound0 = exportAccessPoliciesRequest {
                this.context = request.context
                this.filter = ExportAccessPoliciesRequestKt.filter {
                    this.createdAfter = request.context.deadline
                    this.labels.putAll(request.context.baggageMap)
                    this.ownerId = request.stopId
                }
            }
            authorizationAccessPolicyQueryService.exportAccessPolicies(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            getStopApprovalStatusResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.stop = stop {
                    this.stopId = request.stopId
                }
            }
        }

    private suspend fun handleRecallStop(request: RecallStopRequest): RecallStopResponse =
        RpcBoundary.run("acme.routing.v1.StopApprovalService/RecallStop") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecallStop", request)
            log.atInfo().addKeyValue("operation", "RecallStop").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.stop.stopId).log("Recorded audit detail")
            val outbound0 = commentDispatchOrderApprovalRequest {
                this.context = request.context
                this.details = CommentDispatchOrderApprovalRequestKt.details {
                    this.approver = userRef {
                        this.displayName = request.stop.organizer.displayName
                        this.email = request.details.approver.email
                    }
                }
            }
            val reply0 = withTimeout(5_000) { dispatchDispatchOrderApprovalService.commentDispatchOrderApproval(outbound0) }
            val configure1: CalculateProductRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.details = CalculateProductRequestKt.details {
                    this.destination = postalAddress {
                        this.recipient = request.stop.location.recipient
                    }
                }
                this.product = product {
                    this.address = postalAddress {
                        this.countryCode = request.stop.location.countryCode
                        this.recipient = reply0.dispatchOrder.billingAddress.recipient
                    }
                    this.owner = userRef {
                        this.userId = request.stop.organizer.userId
                    }
                }
            }
            val outbound1 = calculateProductRequest(configure1)
            val reply1 = catalogProductCalculationService.calculateProduct(outbound1)
            val outbound2 = estimateDriverRequest {
                this.context = request.context
                this.driver = driver {
                    this.primaryAddress = postalAddress {
                        this.countryCode = reply0.dispatchOrder.billingAddress.countryCode
                    }
                }
            }
            val reply2 = driversDriverCalculationService.estimateDriver(outbound2)
            val outbound3 = suggestDriversRequest {
                this.context = request.context
                this.filter = SuggestDriversRequestKt.filter {
                    this.createdAfter = reply2.driver.audit.updatedAt
                    this.labels.putAll(reply2.driver.attributesMap)
                    this.region = reply0.dispatchOrder.billingAddress.region
                }
            }
            val reply3 = driversDriverQueryService.suggestDrivers(outbound3)
            recallStopResponse {
                this.outcome = RecallStopResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply3.result.detailsMap)
                    this.referenceIds.addAll(reply3.result.referenceIdsList)
                    this.status = "ok"
                }
                this.stop = stop {
                    this.location = postalAddress {
                        this.postalCode = reply1.product.address.postalCode
                        this.recipient = reply0.dispatchOrder.billingAddress.recipient
                    }
                    this.organizer = userRef {
                        this.displayName = reply1.product.owner.displayName
                        this.email = reply2.driver.contact.email
                    }
                    this.stopId = request.stopId
                }
            }
        }

    private suspend fun handleRejectStop(request: RejectStopRequest): RejectStopResponse =
        RpcBoundary.run("acme.routing.v1.StopApprovalService/RejectStop") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RejectStop", request)
            log.atInfo().addKeyValue("operation", "RejectStop").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.stop.stopId).log("Recorded audit detail")
            val outbound0 = estimateProductRequest {}.catalogCopy {
                this.context = request.context
                this.details = EstimateProductRequestKt.details {
                    this.destination = postalAddress {
                        this.recipient = request.stop.location.recipient
                    }
                }
                this.product = product {
                    this.audit = auditInfo {
                        this.createdAt = request.stop.audit.createdAt
                        this.updatedBy = request.stop.audit.updatedBy
                    }
                }
            }
            val reply0 = catalogProductCalculationService.estimateProduct(outbound0)
            val outbound1 = countRoutesRequest {
                this.context = request.context
                this.filter = CountRoutesRequestKt.filter {
                    this.createdAfter = request.stop.audit.createdAt
                    this.ownerId = request.stop.organizer.userId
                    this.region = when (reply0.product.locatorCase) {
                        Product.LocatorCase.ADDRESS -> reply0.product.address.region
                        else -> ""
                    }
                }
            }
            val reply1 = routingRouteQueryService.countRoutes(outbound1)
            val outbound2 = streamYardMoveMetricsRequest {
                this.context = request.context
                this.filter = StreamYardMoveMetricsRequestKt.filter {
                    this.region = reply0.product.address.region
                }
                this.subscriber = contactInfo {
                    this.email = reply0.product.owner.email
                }
            }
            yardsYardMoveReportingService.streamYardMoveMetrics(outbound2).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            rejectStopResponse {
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = "ok"
                }
                this.stop = stop {
                    this.location = request.stop.location
                    this.participants.addAll(listOf(stopParticipant {
                        this.contact = contactInfo {
                            this.email = reply0.product.owner.email
                        }
                    }))
                    this.stopId = request.stopId
                }
            }
        }
}
