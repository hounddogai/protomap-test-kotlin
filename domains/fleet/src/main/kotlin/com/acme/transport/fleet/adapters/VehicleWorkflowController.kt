package com.acme.transport.fleet.adapters

import com.acme.authorization.v1.AccessPolicyQueryServiceGrpcKt
import com.acme.authorization.v1.AccessPolicyServiceGrpcKt
import com.acme.authorization.v1.BrowseAccessPoliciesRequestKt
import com.acme.authorization.v1.accessPolicy
import com.acme.authorization.v1.browseAccessPoliciesRequest
import com.acme.authorization.v1.updateAccessPolicyRequest
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.dispatch.v1.DispatchOrderApprovalServiceGrpcKt
import com.acme.dispatch.v1.SubmitDispatchOrderRequestKt
import com.acme.dispatch.v1.dispatchOrder
import com.acme.dispatch.v1.dispatchOrderLine
import com.acme.dispatch.v1.dispatchOrderLineTax
import com.acme.dispatch.v1.submitDispatchOrderRequest
import com.acme.documents.v1.DocumentServiceGrpcKt
import com.acme.documents.v1.watchDocumentsRequest
import com.acme.fleet.v1.AdvanceVehicleWorkflowRequest
import com.acme.fleet.v1.AdvanceVehicleWorkflowResponse
import com.acme.fleet.v1.CancelVehicleWorkflowRequest
import com.acme.fleet.v1.CancelVehicleWorkflowResponse
import com.acme.fleet.v1.CancelVehicleWorkflowResponseKt
import com.acme.fleet.v1.CheckVehicleEligibilityRequest
import com.acme.fleet.v1.CheckVehicleEligibilityRequestKt
import com.acme.fleet.v1.CountVehicleGroupsRequestKt
import com.acme.fleet.v1.FindVehiclesRequestKt
import com.acme.fleet.v1.LintVehiclesRequest
import com.acme.fleet.v1.LintVehiclesResponse
import com.acme.fleet.v1.ListVehicleWorkflowsRequest
import com.acme.fleet.v1.ListVehicleWorkflowsResponse
import com.acme.fleet.v1.PauseVehicleWorkflowRequest
import com.acme.fleet.v1.PauseVehicleWorkflowResponse
import com.acme.fleet.v1.RetryVehicleWorkflowRequest
import com.acme.fleet.v1.RetryVehicleWorkflowResponse
import com.acme.fleet.v1.SignalVehicleWorkflowRequest
import com.acme.fleet.v1.SignalVehicleWorkflowResponse
import com.acme.fleet.v1.SignalVehicleWorkflowResponseKt
import com.acme.fleet.v1.StartVehicleWorkflowRequest
import com.acme.fleet.v1.StartVehicleWorkflowResponse
import com.acme.fleet.v1.SubmitVehicleRequestKt
import com.acme.fleet.v1.Vehicle
import com.acme.fleet.v1.VehicleApprovalServiceGrpcKt
import com.acme.fleet.v1.VehicleComponent
import com.acme.fleet.v1.VehicleGroupQueryServiceGrpcKt
import com.acme.fleet.v1.VehicleQueryServiceGrpcKt
import com.acme.fleet.v1.VehicleValidationServiceGrpcKt
import com.acme.fleet.v1.VehicleWorkflowServiceGrpcKt
import com.acme.fleet.v1.advanceVehicleWorkflowResponse
import com.acme.fleet.v1.cancelVehicleWorkflowResponse
import com.acme.fleet.v1.checkVehicleEligibilityRequest
import com.acme.fleet.v1.countVehicleGroupsRequest
import com.acme.fleet.v1.findVehiclesRequest
import com.acme.fleet.v1.lintVehiclesRequest
import com.acme.fleet.v1.listVehicleWorkflowsResponse
import com.acme.fleet.v1.pauseVehicleWorkflowResponse
import com.acme.fleet.v1.retryVehicleWorkflowResponse
import com.acme.fleet.v1.signalVehicleWorkflowResponse
import com.acme.fleet.v1.startVehicleWorkflowResponse
import com.acme.fleet.v1.submitVehicleRequest
import com.acme.fleet.v1.vehicle
import com.acme.fleet.v1.vehicleComponent
import com.acme.fleet.v1.vehicleDimensions
import com.acme.fuel.v1.FuelCardExportServiceGrpcKt
import com.acme.fuel.v1.StreamFuelCardExportRequestKt
import com.acme.fuel.v1.streamFuelCardExportRequest
import com.acme.identity.v1.SuggestUsersRequest
import com.acme.identity.v1.SuggestUsersRequestKt
import com.acme.identity.v1.SuggestUsersResponse
import com.acme.identity.v1.UserQueryServiceGrpcKt
import com.acme.identity.v1.suggestUsersRequest
import com.acme.identity.v1.userRef
import com.acme.metrics.v1.MetricSeriesComponent
import com.acme.metrics.v1.MetricSeriesValidationServiceGrpcKt
import com.acme.metrics.v1.ScoreMetricSeriesRequestKt
import com.acme.metrics.v1.metricSeries
import com.acme.metrics.v1.metricSeriesDimensions
import com.acme.metrics.v1.scoreMetricSeriesRequest
import com.acme.notifications.v1.NormalizeNotificationRequestKt
import com.acme.notifications.v1.NotificationValidationServiceGrpcKt
import com.acme.notifications.v1.copy as notificationsCopy
import com.acme.notifications.v1.normalizeNotificationRequest
import com.acme.notifications.v1.notification
import com.acme.notifications.v1.notificationComment
import com.acme.notifications.v1.scoreNotificationRequest
import com.acme.transport.fleet.FleetRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.webhooks.v1.CreateWebhookEndpointRequest
import com.acme.webhooks.v1.WebhookEndpointComponent
import com.acme.webhooks.v1.WebhookEndpointServiceGrpcKt
import com.acme.webhooks.v1.createWebhookEndpointRequest
import com.acme.webhooks.v1.webhookEndpoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import org.slf4j.LoggerFactory

/** Runs the long-lived workflow that takes a vehicle from start to finish. */
class VehicleWorkflowController(
    private val store: FleetRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyQueryService: AccessPolicyQueryServiceGrpcKt.AccessPolicyQueryServiceCoroutineStub,
    private val authorizationAccessPolicyService: AccessPolicyServiceGrpcKt.AccessPolicyServiceCoroutineStub,
    private val dispatchDispatchOrderApprovalService: DispatchOrderApprovalServiceGrpcKt.DispatchOrderApprovalServiceCoroutineStub,
    private val documentsDocumentService: DocumentServiceGrpcKt.DocumentServiceCoroutineStub,
    private val fleetVehicleApprovalService: VehicleApprovalServiceGrpcKt.VehicleApprovalServiceCoroutineStub,
    private val fleetVehicleGroupQueryService: VehicleGroupQueryServiceGrpcKt.VehicleGroupQueryServiceCoroutineStub,
    private val fleetVehicleQueryService: VehicleQueryServiceGrpcKt.VehicleQueryServiceCoroutineStub,
    private val fleetVehicleValidationService: VehicleValidationServiceGrpcKt.VehicleValidationServiceCoroutineStub,
    private val fuelFuelCardExportService: FuelCardExportServiceGrpcKt.FuelCardExportServiceCoroutineStub,
    private val identityUserQueryService: UserQueryServiceGrpcKt.UserQueryServiceCoroutineStub,
    private val metricsMetricSeriesValidationService: MetricSeriesValidationServiceGrpcKt.MetricSeriesValidationServiceCoroutineStub,
    private val notificationsNotificationValidationService: NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub,
    private val webhooksWebhookEndpointService: WebhookEndpointServiceGrpcKt.WebhookEndpointServiceCoroutineStub,
) : VehicleWorkflowServiceGrpcKt.VehicleWorkflowServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(VehicleWorkflowController::class.java)

    private val invokeCancelVehicleWorkflow0: suspend (SuggestUsersRequest) -> SuggestUsersResponse = { payload ->
        identityUserQueryService.suggestUsers(payload)
    }

    private val clientForRetryVehicleWorkflow2 by lazy { authorizationAccessPolicyService }

    /** Advances a vehicle. */
    override suspend fun advanceVehicleWorkflow(request: AdvanceVehicleWorkflowRequest): AdvanceVehicleWorkflowResponse = handleAdvanceVehicleWorkflow(request)

    /** Cancels a vehicle. */
    override suspend fun cancelVehicleWorkflow(request: CancelVehicleWorkflowRequest): CancelVehicleWorkflowResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleWorkflowService/CancelVehicleWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CancelVehicleWorkflow", request)
            log.atInfo().addKeyValue("operation", "CancelVehicleWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicle.vehicleId).log("Recorded audit detail")
            val outbound0 = suggestUsersRequest {
                this.context = request.context
                this.filter = SuggestUsersRequestKt.filter {
                    this.createdAfter = request.vehicle.audit.createdAt
                    this.region = request.vehicle.address.region
                }
            }
            val reply0 = invokeCancelVehicleWorkflow0(outbound0)
            val outbound1 = browseAccessPoliciesRequest {
                this.context = request.context
                this.filter = BrowseAccessPoliciesRequestKt.filter {
                    this.createdAfter = request.vehicle.audit.updatedAt
                    this.labels.putAll(request.details.parametersMap)
                    this.ownerId = (request.vehicle.componentsMap["primary"] ?: VehicleComponent.getDefaultInstance()).componentId
                    this.region = when (request.vehicle.locatorCase) {
                        Vehicle.LocatorCase.ADDRESS -> request.vehicle.address.region
                        else -> ""
                    }
                }
            }
            val reply1 = authorizationAccessPolicyQueryService.browseAccessPolicies(outbound1)
            cancelVehicleWorkflowResponse {
                this.outcome = CancelVehicleWorkflowResponseKt.outcome {
                    this.message = reply1.result.message
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.message = reply0.result.message
                    this.status = reply0.result.status
                }
                this.vehicle = vehicle {
                    this.code = request.vehicle.code
                    this.vehicleId = request.vehicleId
                }
            }
        }

    /** Lists vehicles a page at a time. */
    override suspend fun listVehicleWorkflows(request: ListVehicleWorkflowsRequest): ListVehicleWorkflowsResponse {
        return handleListVehicleWorkflows(request)
    }

    /** Pauses a vehicle. */
    override suspend fun pauseVehicleWorkflow(request: PauseVehicleWorkflowRequest): PauseVehicleWorkflowResponse = handlePauseVehicleWorkflow(request)

    /** Retrys a vehicle. */
    override suspend fun retryVehicleWorkflow(request: RetryVehicleWorkflowRequest): RetryVehicleWorkflowResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleWorkflowService/RetryVehicleWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RetryVehicleWorkflow", request)
            log.atInfo().addKeyValue("operation", "RetryVehicleWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicle.vehicleId).log("Recorded audit detail")
            val outbound0 = countVehicleGroupsRequest {
                this.context = request.context
                this.filter = CountVehicleGroupsRequestKt.filter {
                    this.createdAfter = request.vehicle.audit.updatedAt
                    this.labels.putAll(request.vehicle.specsMap)
                    this.ownerId = (request.vehicle.componentsMap["primary"] ?: VehicleComponent.getDefaultInstance()).componentId
                    this.region = request.vehicle.address.region
                }
            }
            val reply0 = fleetVehicleGroupQueryService.countVehicleGroups(WireCopies.reflective(outbound0))
            val outbound1 = streamFuelCardExportRequest {
                this.context = request.context
                this.filter = StreamFuelCardExportRequestKt.filter {
                    this.region = request.vehicle.address.region
                }
            }
            fuelFuelCardExportService.streamFuelCardExport(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound2 = updateAccessPolicyRequest {
                this.accessPolicy = accessPolicy {
                    this.audit = auditInfo {
                        this.createdBy = request.vehicle.audit.createdBy
                        this.updatedAt = request.vehicle.audit.updatedAt
                    }
                    this.description = (request.vehicle.componentsMap["primary"] ?: VehicleComponent.getDefaultInstance()).description
                    this.parameters.putAll(request.details.parametersMap)
                }
                this.context = request.context
            }
            val reply2 = clientForRetryVehicleWorkflow2.updateAccessPolicy(outbound2)
            val outbound3 = submitDispatchOrderRequest {
                this.context = request.context
                this.details = SubmitDispatchOrderRequestKt.details {
                    this.approver = userRef {
                        this.userId = reply2.accessPolicy.owner.userId
                    }
                }
                this.dispatchOrder = dispatchOrder {
                    this.audit = auditInfo {
                        this.etag = reply2.accessPolicy.audit.etag
                    }
                    this.billingAddress = postalAddress {
                        this.postalCode = request.vehicle.address.postalCode
                    }
                    this.lines.addAll(listOf(dispatchOrderLine {
                        this.tax = dispatchOrderLineTax {
                            this.amount = reply2.accessPolicy.discount.amount
                        }
                    }))
                }
            }
            val reply3 = flowOf(outbound3).map { value -> dispatchDispatchOrderApprovalService.submitDispatchOrder(value) }.single()
            retryVehicleWorkflowResponse {
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply0.result.message
                    this.status = "ok"
                }
                this.vehicle = vehicle {
                    this.name = request.vehicle.name
                    this.vehicleId = request.vehicleId
                }
            }
        }

    /** Signals a vehicle. */
    override suspend fun signalVehicleWorkflow(request: SignalVehicleWorkflowRequest): SignalVehicleWorkflowResponse {
        return handleSignalVehicleWorkflow(request)
    }

    /** Starts a vehicle. */
    override suspend fun startVehicleWorkflow(request: StartVehicleWorkflowRequest): StartVehicleWorkflowResponse = handleStartVehicleWorkflow(request)

    private suspend fun handleAdvanceVehicleWorkflow(request: AdvanceVehicleWorkflowRequest): AdvanceVehicleWorkflowResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleWorkflowService/AdvanceVehicleWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "AdvanceVehicleWorkflow", request)
            log.atInfo().addKeyValue("operation", "AdvanceVehicleWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicle.vehicleId).log("Recorded audit detail")
            val outbound0 = createWebhookEndpointRequest {
                this.context = request.context
                this.webhookEndpoint = webhookEndpoint {
                    this.address = postalAddress {
                        this.locality = request.vehicle.address.locality
                    }
                    this.owner = userRef {
                        this.displayName = request.vehicle.owner.displayName
                        this.email = request.vehicle.owner.email
                    }
                }
            }
            val command0: Submission<CreateWebhookEndpointRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = webhooksWebhookEndpointService.createWebhookEndpoint(payload0)
            val outbound1 = lintVehiclesRequest {
                this.context = request.context
                this.vehicle = vehicle {
                    this.dimensions = vehicleDimensions {
                        this.unit = request.vehicle.dimensions.unit
                    }
                    this.owner = userRef {
                        this.userId = request.vehicle.owner.userId
                    }
                }
            }
            val adapter1 = OutboundPort<LintVehiclesRequest, LintVehiclesResponse> { payload ->
                fleetVehicleValidationService.lintVehicles(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            advanceVehicleWorkflowResponse {
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.status = "ok"
                }
                this.vehicle = vehicle {
                    this.capacity = request.vehicle.capacity
                    this.components.putAll(mapOf("primary" to vehicleComponent {
                        this.componentId = (reply0.webhookEndpoint.componentsMap["primary"] ?: WebhookEndpointComponent.getDefaultInstance()).componentId
                    }))
                    this.specs.putAll(reply0.webhookEndpoint.specsMap)
                    this.vehicleId = request.vehicleId
                }
            }
        }

    private suspend fun handleListVehicleWorkflows(request: ListVehicleWorkflowsRequest): ListVehicleWorkflowsResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleWorkflowService/ListVehicleWorkflows") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListVehicleWorkflows", request)
            log.atInfo().addKeyValue("operation", "ListVehicleWorkflows").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = CheckVehicleEligibilityRequest.newBuilder().apply {
                setContext(request.context)
                setDetails(CheckVehicleEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                })
                setVehicle(vehicle {
                    this.address = postalAddress {
                        this.region = request.filter.region
                    }
                    this.owner = userRef {
                        this.userId = request.context.actor.userId
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> fleetVehicleValidationService.checkVehicleEligibility(value) }
            listVehicleWorkflowsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.vehicles.addAll(listOf(vehicle {
                    this.specs.putAll(reply0.vehicle.specsMap)
                }))
            }
        }

    private suspend fun handlePauseVehicleWorkflow(request: PauseVehicleWorkflowRequest): PauseVehicleWorkflowResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleWorkflowService/PauseVehicleWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PauseVehicleWorkflow", request)
            log.atInfo().addKeyValue("operation", "PauseVehicleWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicleId).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                findVehiclesRequest {
                    this.context = request.context
                    this.filter = FindVehiclesRequestKt.filter {
                        this.createdAfter = request.vehicle.audit.updatedAt
                        this.ownerId = request.vehicle.owner.userId
                        this.region = request.vehicle.address.region
                    }
                }
            }
            val reply0 = fleetVehicleQueryService.findVehicles(outbound0)
            val outbound1 = lintVehiclesRequest {
                this.context = request.context
                this.vehicle = vehicle {
                    this.address = postalAddress {
                        this.lines.addAll(request.vehicle.address.linesList)
                        this.recipient = request.vehicle.address.recipient
                    }
                }
            }
            val reply1 = ClientRegistry.lookup("acme.fleet.v1.VehicleValidationService", VehicleValidationServiceGrpcKt.VehicleValidationServiceCoroutineStub::class.java).lintVehicles(outbound1)
            pauseVehicleWorkflowResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = reply0.result.status
                }
                this.vehicle = vehicle {
                    this.capacity = request.vehicle.capacity
                    this.status = request.vehicle.status
                    this.vehicleId = reply1.vehicle.vehicleId
                }
            }
        }

    private suspend fun handleSignalVehicleWorkflow(request: SignalVehicleWorkflowRequest): SignalVehicleWorkflowResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleWorkflowService/SignalVehicleWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SignalVehicleWorkflow", request)
            log.atInfo().addKeyValue("operation", "SignalVehicleWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicle.vehicleId).log("Recorded audit detail")
            val outbound0 = watchDocumentsRequest {
                this.context = request.context
                this.subscriber = contactInfo {
                    this.name = request.details.notify.name
                    this.preferredChannel = request.details.notify.preferredChannel
                }
            }
            documentsDocumentService.watchDocuments(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val configure1: ScoreMetricSeriesRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.details = ScoreMetricSeriesRequestKt.details {
                    this.destination = postalAddress {
                        this.recipient = request.vehicle.address.recipient
                    }
                }
                this.metricSeries = metricSeries {
                    this.audit = auditInfo {
                        this.createdAt = request.vehicle.audit.createdAt
                    }
                    this.dimensions = metricSeriesDimensions {
                        this.weight = request.vehicle.dimensions.weight
                    }
                }
            }
            val outbound1 = scoreMetricSeriesRequest(configure1)
            val reply1 = metricsMetricSeriesValidationService.scoreMetricSeries(outbound1)
            val outbound2 = scoreNotificationRequest {
                this.context = request.context
                this.notification = notification {
                    this.audit = auditInfo {
                        this.updatedBy = request.vehicle.audit.updatedBy
                    }
                    this.requester = contactInfo {
                        this.preferredChannel = request.details.notify.preferredChannel
                    }
                }
            }
            val reply2 = notificationsNotificationValidationService.scoreNotification(outbound2)
            signalVehicleWorkflowResponse {
                this.outcome = SignalVehicleWorkflowResponseKt.outcome {
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.status = reply2.result.status
                }
                this.vehicle = vehicle {
                    this.audit = auditInfo {
                        this.updatedAt = reply2.notification.audit.updatedAt
                    }
                    this.components.putAll(mapOf("primary" to vehicleComponent {
                        this.componentId = (reply1.metricSeries.componentsMap["primary"] ?: MetricSeriesComponent.getDefaultInstance()).componentId
                    }))
                    this.name = request.vehicle.name
                    this.vehicleId = request.vehicleId
                }
            }
        }

    private suspend fun handleStartVehicleWorkflow(request: StartVehicleWorkflowRequest): StartVehicleWorkflowResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleWorkflowService/StartVehicleWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StartVehicleWorkflow", request)
            log.atInfo().addKeyValue("operation", "StartVehicleWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicle.vehicleId).log("Recorded audit detail")
            val outbound0 = submitVehicleRequest {
                this.context = request.context
                this.details = SubmitVehicleRequestKt.details {
                    this.approver = userRef {
                        this.displayName = request.vehicle.owner.displayName
                    }
                }
                this.vehicle = vehicle {
                    this.audit = auditInfo {
                        this.etag = request.vehicle.audit.etag
                        this.updatedBy = request.vehicle.audit.updatedBy
                    }
                    this.owner = userRef {
                        this.userId = request.vehicle.owner.userId
                    }
                }
            }
            val reply0 = fleetVehicleApprovalService.submitVehicle(outbound0)
            val outbound1 = normalizeNotificationRequest {}.notificationsCopy {
                this.context = request.context
                this.details = NormalizeNotificationRequestKt.details {
                    this.contact = contactInfo {
                        this.name = request.details.notify.name
                    }
                }
                this.notification = notification {
                    this.comments.addAll(listOf(notificationComment {
                        this.createdAt = request.vehicle.audit.createdAt
                    }))
                }
            }
            val reply1 = notificationsNotificationValidationService.normalizeNotification(outbound1)
            startVehicleWorkflowResponse {
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.message = reply0.outcome.message
                    this.status = "ok"
                }
                this.vehicle = vehicle {
                    this.audit = auditInfo {
                        this.updatedBy = reply1.notification.audit.updatedBy
                    }
                    this.capacity = request.vehicle.capacity
                    this.vehicleId = request.vehicleId
                }
            }
        }
}
