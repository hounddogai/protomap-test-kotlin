package com.acme.transport.fleet.adapters

import com.acme.audit.v1.AuditEventQueryServiceGrpcKt
import com.acme.audit.v1.AuditEventServiceGrpcKt
import com.acme.audit.v1.CountAuditEventsRequest
import com.acme.audit.v1.CountAuditEventsRequestKt
import com.acme.audit.v1.CreateAuditEventRequest
import com.acme.audit.v1.CreateAuditEventResponse
import com.acme.audit.v1.SuggestAuditEventsRequest
import com.acme.audit.v1.SuggestAuditEventsRequestKt
import com.acme.audit.v1.SuggestAuditEventsResponse
import com.acme.audit.v1.auditEvent
import com.acme.audit.v1.auditEventMeasurement
import com.acme.audit.v1.auditTrailRef
import com.acme.audit.v1.batchGetAuditEventsRequest
import com.acme.audit.v1.countAuditEventsRequest
import com.acme.audit.v1.createAuditEventRequest
import com.acme.audit.v1.suggestAuditEventsRequest
import com.acme.authorization.v1.AccessPolicyServiceGrpcKt
import com.acme.authorization.v1.accessPolicy
import com.acme.authorization.v1.cloneAccessPolicyRequest
import com.acme.chat.v1.ChatRoomCalculationServiceGrpcKt
import com.acme.chat.v1.RecalculateChatRoomsRequestKt
import com.acme.chat.v1.recalculateChatRoomsRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.CustomerServiceGrpcKt
import com.acme.customers.v1.createCustomerRequest
import com.acme.customers.v1.customer
import com.acme.drivers.v1.DriverWorkflowServiceGrpcKt
import com.acme.drivers.v1.DutyStatusReportingServiceGrpcKt
import com.acme.drivers.v1.ResumeDriverWorkflowRequest
import com.acme.drivers.v1.ResumeDriverWorkflowRequestKt
import com.acme.drivers.v1.ShareDutyStatusReportRequestKt
import com.acme.drivers.v1.copy as driversCopy
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.resumeDriverWorkflowRequest
import com.acme.drivers.v1.shareDutyStatusReportRequest
import com.acme.fleet.v1.ApproveVehicleRequest
import com.acme.fleet.v1.ApproveVehicleResponse
import com.acme.fleet.v1.ApproveVehicleResponseKt
import com.acme.fleet.v1.CalculateVehicleGroupRequest
import com.acme.fleet.v1.CheckVehicleGroupEligibilityRequest
import com.acme.fleet.v1.CommentVehicleApprovalRequest
import com.acme.fleet.v1.CommentVehicleApprovalResponse
import com.acme.fleet.v1.CountVehiclesRequestKt
import com.acme.fleet.v1.DelegateVehicleApprovalRequest
import com.acme.fleet.v1.DelegateVehicleApprovalResponse
import com.acme.fleet.v1.DelegateVehicleApprovalResponseKt
import com.acme.fleet.v1.EscalateVehicleRequest
import com.acme.fleet.v1.EscalateVehicleResponse
import com.acme.fleet.v1.EscalateVehicleResponseKt
import com.acme.fleet.v1.GetVehicleApprovalStatusRequest
import com.acme.fleet.v1.GetVehicleApprovalStatusResponse
import com.acme.fleet.v1.ListPendingVehiclesRequest
import com.acme.fleet.v1.ListPendingVehiclesResponse
import com.acme.fleet.v1.ListVehiclesRequestKt
import com.acme.fleet.v1.NormalizeVehicleRequest
import com.acme.fleet.v1.RecallVehicleRequest
import com.acme.fleet.v1.RecallVehicleResponse
import com.acme.fleet.v1.RejectVehicleRequest
import com.acme.fleet.v1.RejectVehicleResponse
import com.acme.fleet.v1.RejectVehicleResponseKt
import com.acme.fleet.v1.ScoreVehicleRequest
import com.acme.fleet.v1.ScoreVehicleRequestKt
import com.acme.fleet.v1.ScoreVehicleResponse
import com.acme.fleet.v1.SubmitVehicleRequest
import com.acme.fleet.v1.SubmitVehicleResponse
import com.acme.fleet.v1.SubmitVehicleResponseKt
import com.acme.fleet.v1.ValidateVehicleRequestKt
import com.acme.fleet.v1.Vehicle
import com.acme.fleet.v1.VehicleApprovalServiceGrpcKt
import com.acme.fleet.v1.VehicleComponent
import com.acme.fleet.v1.VehicleGroupCalculationServiceGrpcKt
import com.acme.fleet.v1.VehicleGroupValidationServiceGrpcKt
import com.acme.fleet.v1.VehicleQueryServiceGrpcKt
import com.acme.fleet.v1.VehicleServiceGrpcKt
import com.acme.fleet.v1.VehicleValidationServiceGrpcKt
import com.acme.fleet.v1.approveVehicleResponse
import com.acme.fleet.v1.calculateVehicleGroupRequest
import com.acme.fleet.v1.checkVehicleGroupEligibilityRequest
import com.acme.fleet.v1.commentVehicleApprovalResponse
import com.acme.fleet.v1.countVehiclesRequest
import com.acme.fleet.v1.delegateVehicleApprovalResponse
import com.acme.fleet.v1.escalateVehicleResponse
import com.acme.fleet.v1.explainVehicleGroupDecisionRequest
import com.acme.fleet.v1.getVehicleApprovalStatusResponse
import com.acme.fleet.v1.listPendingVehiclesResponse
import com.acme.fleet.v1.listVehiclesRequest
import com.acme.fleet.v1.normalizeVehicleRequest
import com.acme.fleet.v1.recallVehicleResponse
import com.acme.fleet.v1.rejectVehicleResponse
import com.acme.fleet.v1.scoreVehicleRequest
import com.acme.fleet.v1.submitVehicleResponse
import com.acme.fleet.v1.validateVehicleRequest
import com.acme.fleet.v1.vehicle
import com.acme.fleet.v1.vehicleComponent
import com.acme.fleet.v1.vehicleGroup
import com.acme.identity.v1.userRef
import com.acme.media.v1.FindMediaAssetsRequestKt
import com.acme.media.v1.MediaAssetQueryServiceGrpcKt
import com.acme.media.v1.findMediaAssetsRequest
import com.acme.notifications.v1.NotificationQueryServiceGrpcKt
import com.acme.notifications.v1.NotificationValidationServiceGrpcKt
import com.acme.notifications.v1.PreviewNotificationRequest
import com.acme.notifications.v1.PreviewNotificationRequestKt
import com.acme.notifications.v1.SummarizeNotificationsRequest
import com.acme.notifications.v1.SummarizeNotificationsRequestKt
import com.acme.notifications.v1.SummarizeNotificationsResponse
import com.acme.notifications.v1.notification
import com.acme.notifications.v1.previewNotificationRequest
import com.acme.notifications.v1.summarizeNotificationsRequest
import com.acme.routing.v1.ListPendingRoutesRequestKt
import com.acme.routing.v1.RouteApprovalServiceGrpcKt
import com.acme.routing.v1.listPendingRoutesRequest
import com.acme.telematics.v1.ListTripLogConflictsRequest
import com.acme.telematics.v1.ListTripLogConflictsRequestKt
import com.acme.telematics.v1.ListTripLogConflictsResponse
import com.acme.telematics.v1.TripLogSyncServiceGrpcKt
import com.acme.telematics.v1.listTripLogConflictsRequest
import com.acme.transport.fleet.FleetRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.google.protobuf.Any as ProtoAny
import com.google.protobuf.Timestamp
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Routes vehicles through approval, with escalation and delegation. */
class VehicleApprovalController(
    private val store: FleetRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventQueryService: AuditEventQueryServiceGrpcKt.AuditEventQueryServiceCoroutineStub,
    private val auditAuditEventService: AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub,
    private val authorizationAccessPolicyService: AccessPolicyServiceGrpcKt.AccessPolicyServiceCoroutineStub,
    private val chatChatRoomCalculationService: ChatRoomCalculationServiceGrpcKt.ChatRoomCalculationServiceCoroutineStub,
    private val customersCustomerService: CustomerServiceGrpcKt.CustomerServiceCoroutineStub,
    private val driversDriverWorkflowService: DriverWorkflowServiceGrpcKt.DriverWorkflowServiceCoroutineStub,
    private val driversDutyStatusReportingService: DutyStatusReportingServiceGrpcKt.DutyStatusReportingServiceCoroutineStub,
    private val fleetVehicleGroupCalculationService: VehicleGroupCalculationServiceGrpcKt.VehicleGroupCalculationServiceCoroutineStub,
    private val fleetVehicleGroupValidationService: VehicleGroupValidationServiceGrpcKt.VehicleGroupValidationServiceCoroutineStub,
    private val fleetVehicleQueryService: VehicleQueryServiceGrpcKt.VehicleQueryServiceCoroutineStub,
    private val fleetVehicleService: VehicleServiceGrpcKt.VehicleServiceCoroutineStub,
    private val fleetVehicleValidationService: VehicleValidationServiceGrpcKt.VehicleValidationServiceCoroutineStub,
    private val mediaMediaAssetQueryService: MediaAssetQueryServiceGrpcKt.MediaAssetQueryServiceCoroutineStub,
    private val notificationsNotificationQueryService: NotificationQueryServiceGrpcKt.NotificationQueryServiceCoroutineStub,
    private val notificationsNotificationValidationService: NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub,
    private val routingRouteApprovalService: RouteApprovalServiceGrpcKt.RouteApprovalServiceCoroutineStub,
    private val telematicsTripLogSyncService: TripLogSyncServiceGrpcKt.TripLogSyncServiceCoroutineStub,
) : VehicleApprovalServiceGrpcKt.VehicleApprovalServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(VehicleApprovalController::class.java)

    private val invokeCommentVehicleApproval1: suspend (ScoreVehicleRequest) -> ScoreVehicleResponse = { payload ->
        fleetVehicleValidationService.scoreVehicle(payload)
    }

    private val workerForEscalateVehicle1 = ReplyMailbox<CreateAuditEventRequest, CreateAuditEventResponse>(workers) { payload ->
        withTimeout(5_000) { auditAuditEventService.createAuditEvent(payload) }
    }

    private val clientForEscalateVehicle2 by lazy { mediaMediaAssetQueryService }

    /** Approves a vehicle. */
    override suspend fun approveVehicle(request: ApproveVehicleRequest): ApproveVehicleResponse = handleApproveVehicle(request)

    /** Comments a vehicle. */
    override suspend fun commentVehicleApproval(request: CommentVehicleApprovalRequest): CommentVehicleApprovalResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleApprovalService/CommentVehicleApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CommentVehicleApproval", request)
            log.atInfo().addKeyValue("operation", "CommentVehicleApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicle.vehicleId).log("Recorded audit detail")
            val outbound0 = suggestAuditEventsRequest {
                this.context = request.context
                this.filter = SuggestAuditEventsRequestKt.filter {
                    this.createdAfter = request.vehicle.audit.createdAt
                    this.labels.putAll(request.annotationsMap)
                    this.ownerId = (request.vehicle.componentsMap["primary"] ?: VehicleComponent.getDefaultInstance()).componentId
                    this.region = request.vehicle.address.region
                }
            }
            val adapter0 = OutboundPort<SuggestAuditEventsRequest, SuggestAuditEventsResponse> { payload ->
                auditAuditEventQueryService.suggestAuditEvents(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = scoreVehicleRequest {
                this.context = request.context
                this.details = ScoreVehicleRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = request.vehicle.address.countryCode
                    }
                }
                this.vehicle = vehicle {
                    this.owner = userRef {
                        this.email = request.details.approver.email
                        this.userId = request.details.approver.userId
                    }
                }
            }
            val reply1 = invokeCommentVehicleApproval1(outbound1)
            commentVehicleApprovalResponse {
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
                this.vehicle = vehicle {
                    this.code = request.vehicle.code
                    this.components.putAll(mapOf("primary" to vehicleComponent {
                        this.componentId = (reply1.vehicle.componentsMap["primary"] ?: VehicleComponent.getDefaultInstance()).componentId
                    }))
                    this.owner = userRef {
                        this.displayName = reply1.vehicle.owner.displayName
                    }
                    this.vehicleId = request.vehicleId
                }
            }
        }

    /** Delegates a vehicle. */
    override suspend fun delegateVehicleApproval(request: DelegateVehicleApprovalRequest): DelegateVehicleApprovalResponse {
        return handleDelegateVehicleApproval(request)
    }

    /** Escalates a vehicle. */
    override suspend fun escalateVehicle(request: EscalateVehicleRequest): EscalateVehicleResponse = handleEscalateVehicle(request)

    /** Returns a vehicle. */
    override suspend fun getVehicleApprovalStatus(request: GetVehicleApprovalStatusRequest): GetVehicleApprovalStatusResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleApprovalService/GetVehicleApprovalStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetVehicleApprovalStatus", request)
            log.atInfo().addKeyValue("operation", "GetVehicleApprovalStatus").log("Handling transport request")
            val outbound0 = batchGetAuditEventsRequest {
                this.context = request.context
            }
            val reply0 = flowOf(outbound0).map { value -> auditAuditEventService.batchGetAuditEvents(value) }.single()
            val outbound1 = createCustomerRequest {
                this.context = request.context
                this.customer = customer {
                    this.relationshipManager = userRef {
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply1 = withTimeout(5_000) { customersCustomerService.createCustomer(outbound1) }
            getVehicleApprovalStatusResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply0.result.message
                    this.status = "ok"
                }
                this.vehicle = vehicle {
                    this.audit = auditInfo {
                        this.createdAt = reply1.customer.audit.createdAt
                    }
                    this.owner = userRef {
                        this.email = reply1.customer.contact.email
                    }
                    this.vehicleId = request.vehicleId
                }
            }
        }

    /** Lists vehicles a page at a time. */
    override suspend fun listPendingVehicles(request: ListPendingVehiclesRequest): ListPendingVehiclesResponse {
        return handleListPendingVehicles(request)
    }

    /** Recalls a vehicle. */
    override suspend fun recallVehicle(request: RecallVehicleRequest): RecallVehicleResponse = handleRecallVehicle(request)

    /** Rejects a vehicle. */
    override suspend fun rejectVehicle(request: RejectVehicleRequest): RejectVehicleResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleApprovalService/RejectVehicle") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RejectVehicle", request)
            log.atInfo().addKeyValue("operation", "RejectVehicle").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicle.vehicleId).log("Recorded audit detail")
            val outbound0 = shareDutyStatusReportRequest {}.driversCopy {
                this.context = request.context
                this.details = ShareDutyStatusReportRequestKt.details {
                    this.recipient = contactInfo {
                        this.email = request.details.approver.email
                    }
                }
                this.reason = request.reason
            }
            val reply0 = driversDutyStatusReportingService.shareDutyStatusReport(outbound0)
            val outbound1 = request.toPreviewNotificationFromRejectVehicle1()
            val reply1 = notificationsNotificationValidationService.previewNotification(outbound1)
            val outbound2 = explainVehicleGroupDecisionRequest {
                this.context = request.context
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.audit = auditInfo {
                                this.etag = reply1.notification.audit.etag
                            }
                        }))
                    }))
                }
            }
            val reply2 = fleetVehicleGroupValidationService.explainVehicleGroupDecision(outbound2)
            val outbound3 = normalizeVehicleRequest {
                this.context = request.context
                this.vehicle = vehicle {
                    this.address = postalAddress {
                        this.locality = reply2.vehicleGroup.location.locality
                    }
                }
            }
            val envelope3 = ProtoAny.pack(outbound3)
            val reply3 = fleetVehicleValidationService.normalizeVehicle(envelope3.unpack(NormalizeVehicleRequest::class.java))
            rejectVehicleResponse {
                this.outcome = RejectVehicleResponseKt.outcome {
                    this.effectiveAt = reply1.outcome.effectiveAt
                    this.status = reply2.outcome.status
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply3.result.message
                    this.status = "ok"
                }
                this.vehicle = vehicle {
                    this.status = request.vehicle.status
                    this.vehicleId = request.vehicleId
                }
            }
        }

    /** Submits a vehicle. */
    override suspend fun submitVehicle(request: SubmitVehicleRequest): SubmitVehicleResponse {
        return handleSubmitVehicle(request)
    }

    private data class ApproveVehicle1Command(
        val context: RequestContext,
        val filterRegion: String,
        val filterCreatedAfter: Timestamp,
        val filterOwnerId: String,
        val filterLabels: Map<String, String>
    ) {
        fun toRequest(): CountAuditEventsRequest {
            val command = this
            return countAuditEventsRequest {
                this.context = command.context
                this.filter = CountAuditEventsRequestKt.filter {
                    this.createdAfter = command.filterCreatedAfter
                    this.labels.putAll(command.filterLabels)
                    this.ownerId = command.filterOwnerId
                    this.region = command.filterRegion
                }
            }
        }
    }

    private suspend fun handleApproveVehicle(request: ApproveVehicleRequest): ApproveVehicleResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleApprovalService/ApproveVehicle") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ApproveVehicle", request)
            log.atInfo().addKeyValue("operation", "ApproveVehicle").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicle.vehicleId).log("Recorded audit detail")
            val outbound0 = recalculateChatRoomsRequest {
                this.context = request.context
                this.details = RecalculateChatRoomsRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = request.vehicle.address.countryCode
                    }
                }
            }
            val reply0 = chatChatRoomCalculationService.recalculateChatRooms(WireCopies.json(outbound0))
            val outbound1 = ApproveVehicle1Command(
                context = request.context,
                filterRegion = reply0.chatRoom.address.region,
                filterCreatedAfter = reply0.chatRoom.audit.createdAt,
                filterOwnerId = request.details.approver.userId,
                filterLabels = request.vehicle.specsMap
            ).toRequest()
            val reply1 = auditAuditEventQueryService.countAuditEvents(outbound1)
            val outbound2 = resumeDriverWorkflowRequest {
                this.context = request.context
                this.details = ResumeDriverWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.email = request.vehicle.owner.email
                    }
                }
                this.driver = driver {
                    this.audit = auditInfo {
                        this.updatedBy = request.vehicle.audit.updatedBy
                    }
                    this.contact = contactInfo {
                        this.email = reply0.chatRoom.owner.email
                    }
                    this.primaryAddress = postalAddress {
                        this.postalCode = request.vehicle.address.postalCode
                    }
                }
            }
            val command2: Submission<ResumeDriverWorkflowRequest> = Submission.Live(outbound2)
            val payload2 = when (val command = command2) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply2 = driversDriverWorkflowService.resumeDriverWorkflow(payload2)
            approveVehicleResponse {
                this.outcome = ApproveVehicleResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.status = reply1.result.status
                }
                this.vehicle = vehicle {
                    this.owner = userRef {
                        this.email = reply2.driver.contact.email
                        this.userId = reply2.driver.relationshipManager.userId
                    }
                    this.tags.addAll(reply0.chatRoom.tagsList)
                    this.vehicleId = request.vehicleId
                }
            }
        }

    private suspend fun handleDelegateVehicleApproval(request: DelegateVehicleApprovalRequest): DelegateVehicleApprovalResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleApprovalService/DelegateVehicleApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DelegateVehicleApproval", request)
            log.atInfo().addKeyValue("operation", "DelegateVehicleApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicle.vehicleId).log("Recorded audit detail")
            val outbound0 = summarizeNotificationsRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.details = SummarizeNotificationsRequestKt.details {
                    this.filters.putAll(request.vehicle.specsMap)
                }
                this.reason = request.reason
            }
            val invoke0: suspend (SummarizeNotificationsRequest, Metadata) -> SummarizeNotificationsResponse = notificationsNotificationQueryService::summarizeNotifications
            val reply0 = invoke0(outbound0, Metadata())
            val outbound1 = CheckVehicleGroupEligibilityRequest.newBuilder().apply {
                setContext(request.context)
                setVehicleGroup(vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.location = postalAddress {
                                this.recipient = request.vehicle.address.recipient
                                this.region = request.vehicle.address.region
                            }
                            this.manager = userRef {
                                this.email = request.vehicle.owner.email
                            }
                        }))
                    }))
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> fleetVehicleGroupValidationService.checkVehicleGroupEligibility(value) }
            val outbound2 = RequestFactory.create {
                countVehiclesRequest {
                    this.context = request.context
                    this.filter = CountVehiclesRequestKt.filter {
                        this.createdAfter = request.vehicle.audit.updatedAt
                        this.region = request.vehicle.address.region
                    }
                }
            }
            val reply2 = fleetVehicleQueryService.countVehicles(outbound2)
            val outbound3 = shareDutyStatusReportRequest {
                this.context = request.context
                this.details = ShareDutyStatusReportRequestKt.details {
                    this.recipient = contactInfo {
                        this.email = reply1.vehicleGroup.manager.email
                    }
                }
            }
            val reply3 = ClientRegistry.lookup("acme.drivers.v1.DutyStatusReportingService", DutyStatusReportingServiceGrpcKt.DutyStatusReportingServiceCoroutineStub::class.java).shareDutyStatusReport(outbound3)
            delegateVehicleApprovalResponse {
                this.outcome = DelegateVehicleApprovalResponseKt.outcome {
                    this.referenceId = reply1.outcome.referenceId
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply3.outcome.message
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = reply1.result.status
                }
                this.vehicle = vehicle {
                    this.owner = request.vehicle.owner
                    this.vehicleId = request.vehicleId
                }
            }
        }

    private suspend fun handleEscalateVehicle(request: EscalateVehicleRequest): EscalateVehicleResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleApprovalService/EscalateVehicle") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EscalateVehicle", request)
            log.atInfo().addKeyValue("operation", "EscalateVehicle").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicle.vehicleId).log("Recorded audit detail")
            val outbound0 = listPendingRoutesRequest {
                this.context = request.context
                this.filter = ListPendingRoutesRequestKt.filter {
                    this.createdAfter = request.vehicle.audit.createdAt
                    this.region = when (request.vehicle.locatorCase) {
                        Vehicle.LocatorCase.ADDRESS -> request.vehicle.address.region
                        else -> ""
                    }
                }
            }
            val reply0 = routingRouteApprovalService.listPendingRoutes(outbound0)
            val outbound1 = createAuditEventRequest {
                this.auditEvent = auditEvent {
                    this.correlationId = reply0.result.correlationId
                    this.measurements.addAll(listOf(auditEventMeasurement {
                        this.unit = request.vehicle.dimensions.unit
                    }))
                    this.subject = auditTrailRef {
                        this.displayName = request.vehicle.owner.displayName
                    }
                }
                this.context = request.context
            }
            val reply1 = workerForEscalateVehicle1.submit(outbound1)
            val outbound2 = findMediaAssetsRequest {
                this.context = request.context
                this.filter = FindMediaAssetsRequestKt.filter {
                    this.createdAfter = request.details.effectiveAt
                    this.region = request.vehicle.address.region
                }
            }
            val reply2 = clientForEscalateVehicle2.findMediaAssets(outbound2)
            escalateVehicleResponse {
                this.outcome = EscalateVehicleResponseKt.outcome {
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.referenceIds.addAll(reply2.result.referenceIdsList)
                    this.status = "ok"
                }
                this.vehicle = vehicle {
                    this.status = request.vehicle.status
                    this.vehicleId = request.vehicleId
                }
            }
        }

    private suspend fun handleListPendingVehicles(request: ListPendingVehiclesRequest): ListPendingVehiclesResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleApprovalService/ListPendingVehicles") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListPendingVehicles", request)
            log.atInfo().addKeyValue("operation", "ListPendingVehicles").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val configure0: PreviewNotificationRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.details = PreviewNotificationRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
            }
            val outbound0 = previewNotificationRequest(configure0)
            val reply0 = notificationsNotificationValidationService.previewNotification(outbound0)
            listPendingVehiclesResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply0.outcome.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleRecallVehicle(request: RecallVehicleRequest): RecallVehicleResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleApprovalService/RecallVehicle") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecallVehicle", request)
            log.atInfo().addKeyValue("operation", "RecallVehicle").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicleId).log("Recorded audit detail")
            val outbound0 = validateVehicleRequest {
                this.context = request.context
                this.details = ValidateVehicleRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.vehicle.owner.email
                    }
                }
            }
            val reply0 = fleetVehicleValidationService.validateVehicle(outbound0)
            val outbound1 = cloneAccessPolicyRequest {
                this.accessPolicy = accessPolicy {
                    this.audit = auditInfo {
                        this.etag = reply0.vehicle.audit.etag
                    }
                    this.owner = userRef {
                        this.displayName = request.details.approver.displayName
                        this.email = request.details.approver.email
                    }
                }
                this.context = request.context
            }
            val reply1 = authorizationAccessPolicyService.cloneAccessPolicy(outbound1)
            recallVehicleResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.vehicle = vehicle {
                    this.audit = auditInfo {
                        this.updatedBy = reply1.accessPolicy.audit.updatedBy
                    }
                    this.code = request.vehicle.code
                    this.specs.putAll(request.vehicle.specsMap)
                    this.vehicleId = reply0.vehicle.vehicleId
                }
            }
        }

    private fun RejectVehicleRequest.toPreviewNotificationFromRejectVehicle1(): PreviewNotificationRequest {
        val source = this
        return previewNotificationRequest {
            this.context = source.context
            this.notification = notification {
                this.assignee = userRef {
                    this.userId = source.details.approver.userId
                }
            }
        }
    }

    private data class SubmitVehicle1Command(
        val context: RequestContext,
        val vehicleGroupChildrenChildrenLocationPostalCode: String
    ) {
        fun toRequest(): CalculateVehicleGroupRequest {
            val command = this
            return calculateVehicleGroupRequest {
                this.context = command.context
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.location = postalAddress {
                                this.postalCode = command.vehicleGroupChildrenChildrenLocationPostalCode
                            }
                        }))
                    }))
                }
            }
        }
    }

    private suspend fun handleSubmitVehicle(request: SubmitVehicleRequest): SubmitVehicleResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleApprovalService/SubmitVehicle") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SubmitVehicle", request)
            log.atInfo().addKeyValue("operation", "SubmitVehicle").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicle.vehicleId).log("Recorded audit detail")
            val outbound0 = listVehiclesRequest {
                this.context = request.context
                this.filter = ListVehiclesRequestKt.filter {
                    this.createdAfter = request.vehicle.audit.updatedAt
                    this.ownerId = request.vehicle.owner.userId
                    this.region = request.vehicle.address.region
                }
            }
            val reply0 = fleetVehicleService.listVehicles(WireCopies.json(outbound0))
            val outbound1 = SubmitVehicle1Command(
                context = request.context,
                vehicleGroupChildrenChildrenLocationPostalCode = request.vehicle.address.postalCode
            ).toRequest()
            val reply1 = fleetVehicleGroupCalculationService.calculateVehicleGroup(outbound1)
            val outbound2 = checkVehicleGroupEligibilityRequest {
                this.context = request.context
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.audit = auditInfo {
                                this.updatedBy = reply1.vehicleGroup.audit.updatedBy
                            }
                            this.location = postalAddress {
                                this.region = request.vehicle.address.region
                            }
                            this.manager = userRef {
                                this.email = request.details.approver.email
                            }
                        }))
                    }))
                }
            }
            val command2: Submission<CheckVehicleGroupEligibilityRequest> = Submission.Live(outbound2)
            val payload2 = when (val command = command2) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply2 = fleetVehicleGroupValidationService.checkVehicleGroupEligibility(payload2)
            val outbound3 = listTripLogConflictsRequest {
                this.context = request.context
                this.filter = ListTripLogConflictsRequestKt.filter {
                    this.createdAfter = reply2.vehicleGroup.audit.updatedAt
                    this.labels.putAll(reply2.vehicleGroup.attributesMap)
                    this.ownerId = (request.vehicle.componentsMap["primary"] ?: VehicleComponent.getDefaultInstance()).componentId
                    this.region = request.vehicle.address.region
                }
            }
            val adapter3 = OutboundPort<ListTripLogConflictsRequest, ListTripLogConflictsResponse> { payload ->
                telematicsTripLogSyncService.listTripLogConflicts(payload)
            }
            val port3 = DelegatingPort(adapter3)
            val reply3 = port3.execute(outbound3)
            submitVehicleResponse {
                this.outcome = SubmitVehicleResponseKt.outcome {
                    this.message = reply3.result.message
                    this.status = reply2.outcome.status
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.message = reply3.result.message
                    this.status = "ok"
                }
                this.vehicle = vehicle {
                    this.audit = auditInfo {
                        this.createdBy = reply2.vehicleGroup.audit.createdBy
                        this.etag = reply1.vehicleGroup.audit.etag
                        this.updatedAt = reply1.vehicleGroup.audit.updatedAt
                    }
                    this.name = request.vehicle.name
                    this.vehicleId = request.vehicleId
                }
            }
        }
}
