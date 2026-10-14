package com.acme.transport.fleet.grpc

import com.acme.assets.v1.CountFixedAssetsRequestKt
import com.acme.assets.v1.FixedAssetQueryServiceGrpcKt
import com.acme.assets.v1.countFixedAssetsRequest
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.pageRequest
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.DeliveryExceptionApprovalServiceGrpcKt
import com.acme.deliveries.v1.RecallDeliveryExceptionRequest
import com.acme.deliveries.v1.RecallDeliveryExceptionRequestKt
import com.acme.deliveries.v1.RecallDeliveryExceptionResponse
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.deliveryExceptionComment
import com.acme.deliveries.v1.recallDeliveryExceptionRequest
import com.acme.fleet.v1.ApproveVehicleGroupRequest
import com.acme.fleet.v1.ApproveVehicleGroupResponse
import com.acme.fleet.v1.ApproveVehicleResponse
import com.acme.fleet.v1.CloneVehicleRequest
import com.acme.fleet.v1.CommentVehicleGroupApprovalRequest
import com.acme.fleet.v1.CommentVehicleGroupApprovalResponse
import com.acme.fleet.v1.CommentVehicleGroupApprovalResponseKt
import com.acme.fleet.v1.DelegateVehicleGroupApprovalRequest
import com.acme.fleet.v1.DelegateVehicleGroupApprovalResponse
import com.acme.fleet.v1.EscalateVehicleGroupRequest
import com.acme.fleet.v1.EscalateVehicleGroupResponse
import com.acme.fleet.v1.GetVehicleGroupApprovalStatusRequest
import com.acme.fleet.v1.GetVehicleGroupApprovalStatusResponse
import com.acme.fleet.v1.Inspection
import com.acme.fleet.v1.InspectionCalculationServiceGrpcKt
import com.acme.fleet.v1.InspectionLifecycleServiceGrpcKt
import com.acme.fleet.v1.ListPendingVehicleGroupsRequest
import com.acme.fleet.v1.ListPendingVehicleGroupsResponse
import com.acme.fleet.v1.ListVehiclesRequestKt
import com.acme.fleet.v1.QuoteInspectionRequest
import com.acme.fleet.v1.RecallVehicleGroupRequest
import com.acme.fleet.v1.RecallVehicleGroupResponse
import com.acme.fleet.v1.RecallVehicleGroupResponseKt
import com.acme.fleet.v1.RejectVehicleGroupRequest
import com.acme.fleet.v1.RejectVehicleGroupResponse
import com.acme.fleet.v1.RejectVehicleGroupResponseKt
import com.acme.fleet.v1.ScheduleInspectionExpiryRequestKt
import com.acme.fleet.v1.SearchVehiclesRequestKt
import com.acme.fleet.v1.SubmitVehicleGroupRequest
import com.acme.fleet.v1.SubmitVehicleGroupResponse
import com.acme.fleet.v1.ValidateVehicleRequestKt
import com.acme.fleet.v1.VehicleApprovalServiceGrpcKt
import com.acme.fleet.v1.VehicleComponent
import com.acme.fleet.v1.VehicleGroupApprovalServiceGrpcKt
import com.acme.fleet.v1.VehicleServiceGrpcKt
import com.acme.fleet.v1.VehicleValidationServiceGrpcKt
import com.acme.fleet.v1.WatchVehiclesRequestKt
import com.acme.fleet.v1.approveVehicleGroupResponse
import com.acme.fleet.v1.approveVehicleRequest
import com.acme.fleet.v1.cloneVehicleRequest
import com.acme.fleet.v1.commentVehicleGroupApprovalResponse
import com.acme.fleet.v1.copy as fleetCopy
import com.acme.fleet.v1.delegateVehicleGroupApprovalResponse
import com.acme.fleet.v1.escalateVehicleGroupResponse
import com.acme.fleet.v1.getVehicleGroupApprovalStatusResponse
import com.acme.fleet.v1.inspection
import com.acme.fleet.v1.listPendingVehicleGroupsResponse
import com.acme.fleet.v1.listVehiclesRequest
import com.acme.fleet.v1.quoteInspectionRequest
import com.acme.fleet.v1.recallVehicleGroupResponse
import com.acme.fleet.v1.rejectVehicleGroupResponse
import com.acme.fleet.v1.scheduleInspectionExpiryRequest
import com.acme.fleet.v1.scoreVehicleRequest
import com.acme.fleet.v1.searchVehiclesRequest
import com.acme.fleet.v1.submitVehicleGroupResponse
import com.acme.fleet.v1.validateVehicleRequest
import com.acme.fleet.v1.vehicle
import com.acme.fleet.v1.vehicleGroup
import com.acme.fleet.v1.watchVehiclesRequest
import com.acme.fuel.v1.FuelPolicySyncServiceGrpcKt
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.syncFuelPoliciesRequest
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.LedgerAccountServiceGrpcKt
import com.acme.ledger.v1.WatchLedgerAccountsRequestKt
import com.acme.ledger.v1.watchLedgerAccountsRequest
import com.acme.lending.v1.ExportLoanApplicationsRequestKt
import com.acme.lending.v1.LoanApplicationQueryServiceGrpcKt
import com.acme.lending.v1.exportLoanApplicationsRequest
import com.acme.metrics.v1.MetricSeriesServiceGrpcKt
import com.acme.metrics.v1.WatchMetricSeriesesRequestKt
import com.acme.metrics.v1.watchMetricSeriesesRequest
import com.acme.search.v1.ExplainSearchIndexDecisionRequest
import com.acme.search.v1.SearchIndexValidationServiceGrpcKt
import com.acme.search.v1.explainSearchIndexDecisionRequest
import com.acme.search.v1.scoreSearchIndexRequest
import com.acme.search.v1.searchIndex
import com.acme.transport.fleet.FleetRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.venues.v1.ScoreVenueRequestKt
import com.acme.venues.v1.VenueValidationServiceGrpcKt
import com.acme.venues.v1.scoreVenueRequest
import com.acme.venues.v1.venue
import com.acme.venues.v1.venueComponent
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Routes vehicle groups through approval, with escalation and delegation. */
class VehicleGroupApprovalEndpoint(
    private val store: FleetRepository,
    private val workers: CoroutineScope,
    private val assetsFixedAssetQueryService: FixedAssetQueryServiceGrpcKt.FixedAssetQueryServiceCoroutineStub,
    private val deliveriesDeliveryExceptionApprovalService: DeliveryExceptionApprovalServiceGrpcKt.DeliveryExceptionApprovalServiceCoroutineStub,
    private val fleetInspectionCalculationService: InspectionCalculationServiceGrpcKt.InspectionCalculationServiceCoroutineStub,
    private val fleetInspectionLifecycleService: InspectionLifecycleServiceGrpcKt.InspectionLifecycleServiceCoroutineStub,
    private val fleetVehicleApprovalService: VehicleApprovalServiceGrpcKt.VehicleApprovalServiceCoroutineStub,
    private val fleetVehicleService: VehicleServiceGrpcKt.VehicleServiceCoroutineStub,
    private val fleetVehicleValidationService: VehicleValidationServiceGrpcKt.VehicleValidationServiceCoroutineStub,
    private val fuelFuelPolicySyncService: FuelPolicySyncServiceGrpcKt.FuelPolicySyncServiceCoroutineStub,
    private val ledgerLedgerAccountService: LedgerAccountServiceGrpcKt.LedgerAccountServiceCoroutineStub,
    private val lendingLoanApplicationQueryService: LoanApplicationQueryServiceGrpcKt.LoanApplicationQueryServiceCoroutineStub,
    private val metricsMetricSeriesService: MetricSeriesServiceGrpcKt.MetricSeriesServiceCoroutineStub,
    private val searchSearchIndexValidationService: SearchIndexValidationServiceGrpcKt.SearchIndexValidationServiceCoroutineStub,
    private val venuesVenueValidationService: VenueValidationServiceGrpcKt.VenueValidationServiceCoroutineStub,
) : VehicleGroupApprovalServiceGrpcKt.VehicleGroupApprovalServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(VehicleGroupApprovalEndpoint::class.java)

    /** Approves a vehicle group. */
    override suspend fun approveVehicleGroup(request: ApproveVehicleGroupRequest): ApproveVehicleGroupResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupApprovalService/ApproveVehicleGroup") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ApproveVehicleGroup", request)
            log.atInfo().addKeyValue("operation", "ApproveVehicleGroup").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicleGroup.vehicleGroupId).log("Recorded audit detail")
            val outbound0 = watchLedgerAccountsRequest {
                this.context = request.context
                this.filter = WatchLedgerAccountsRequestKt.filter {
                    this.createdAfter = request.vehicleGroup.audit.createdAt
                    this.region = request.vehicleGroup.location.region
                }
                this.subscriber = contactInfo {
                    this.email = request.vehicleGroup.manager.email
                }
            }
            ledgerLedgerAccountService.watchLedgerAccounts(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            approveVehicleGroupResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.vehicleGroup = vehicleGroup {
                    this.name = request.vehicleGroup.name
                    this.vehicleGroupId = request.vehicleGroupId
                }
            }
        }

    /** Comments a vehicle group. */
    override suspend fun commentVehicleGroupApproval(request: CommentVehicleGroupApprovalRequest): CommentVehicleGroupApprovalResponse {
        return handleCommentVehicleGroupApproval(request)
    }

    /** Delegates a vehicle group. */
    override suspend fun delegateVehicleGroupApproval(request: DelegateVehicleGroupApprovalRequest): DelegateVehicleGroupApprovalResponse = handleDelegateVehicleGroupApproval(request)

    /** Escalates a vehicle group. */
    override suspend fun escalateVehicleGroup(request: EscalateVehicleGroupRequest): EscalateVehicleGroupResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupApprovalService/EscalateVehicleGroup") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EscalateVehicleGroup", request)
            log.atInfo().addKeyValue("operation", "EscalateVehicleGroup").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicleGroup.vehicleGroupId).log("Recorded audit detail")
            val outbound0 = watchMetricSeriesesRequest {
                this.context = request.context
                this.filter = WatchMetricSeriesesRequestKt.filter {
                    this.region = request.vehicleGroup.location.region
                }
                this.subscriber = contactInfo {
                    this.email = request.details.approver.email
                }
            }
            metricsMetricSeriesService.watchMetricSerieses(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = scoreVehicleRequest {
                this.context = request.context
                this.vehicle = vehicle {
                    this.address = postalAddress {
                        this.postalCode = request.vehicleGroup.location.postalCode
                    }
                }
            }
            val reply1 = flowOf(outbound1).map { value -> fleetVehicleValidationService.scoreVehicle(value) }.single()
            escalateVehicleGroupResponse {
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.status = "ok"
                }
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.location = postalAddress {
                            this.region = reply1.vehicle.address.region
                        }
                    }))
                    this.manager = request.vehicleGroup.manager
                    this.vehicleGroupId = request.vehicleGroupId
                }
            }
        }

    /** Returns a vehicle group. */
    override suspend fun getVehicleGroupApprovalStatus(request: GetVehicleGroupApprovalStatusRequest): GetVehicleGroupApprovalStatusResponse {
        return handleGetVehicleGroupApprovalStatus(request)
    }

    /** Lists vehicle groups a page at a time. */
    override suspend fun listPendingVehicleGroups(request: ListPendingVehicleGroupsRequest): ListPendingVehicleGroupsResponse = handleListPendingVehicleGroups(request)

    /** Recalls a vehicle group. */
    override suspend fun recallVehicleGroup(request: RecallVehicleGroupRequest): RecallVehicleGroupResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupApprovalService/RecallVehicleGroup") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecallVehicleGroup", request)
            log.atInfo().addKeyValue("operation", "RecallVehicleGroup").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicleGroup.vehicleGroupId).log("Recorded audit detail")
            val outbound0 = searchVehiclesRequest {
                this.context = request.context
                this.filter = SearchVehiclesRequestKt.filter {
                    this.createdAfter = request.vehicleGroup.audit.updatedAt
                    this.region = request.vehicleGroup.location.region
                }
            }
            val reply0 = fleetVehicleService.searchVehicles(outbound0)
            val outbound1 = watchVehiclesRequest {
                this.context = request.context
                this.filter = WatchVehiclesRequestKt.filter {
                    this.createdAfter = request.vehicleGroup.audit.updatedAt
                    this.region = request.vehicleGroup.location.region
                }
                this.since = request.vehicleGroup.audit.createdAt
                this.subscriber = contactInfo {
                    this.email = request.vehicleGroup.manager.email
                }
            }
            fleetVehicleService.watchVehicles(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            recallVehicleGroupResponse {
                this.outcome = RecallVehicleGroupResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.vehicleGroup = vehicleGroup {
                    this.vehicleGroupId = request.vehicleGroupId
                }
            }
        }

    /** Rejects a vehicle group. */
    override suspend fun rejectVehicleGroup(request: RejectVehicleGroupRequest): RejectVehicleGroupResponse {
        return handleRejectVehicleGroup(request)
    }

    /** Submits a vehicle group. */
    override suspend fun submitVehicleGroup(request: SubmitVehicleGroupRequest): SubmitVehicleGroupResponse = handleSubmitVehicleGroup(request)

    private suspend fun handleCommentVehicleGroupApproval(request: CommentVehicleGroupApprovalRequest): CommentVehicleGroupApprovalResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupApprovalService/CommentVehicleGroupApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CommentVehicleGroupApproval", request)
            log.atInfo().addKeyValue("operation", "CommentVehicleGroupApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicleGroup.vehicleGroupId).log("Recorded audit detail")
            val outbound0 = recallDeliveryExceptionRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.assignee = userRef {
                        this.userId = request.details.approver.userId
                    }
                    this.comments.addAll(listOf(deliveryExceptionComment {
                        this.createdAt = request.vehicleGroup.audit.createdAt
                    }))
                    this.requester = contactInfo {
                        this.email = request.details.approver.email
                    }
                }
                this.details = RecallDeliveryExceptionRequestKt.details {
                    this.approver = userRef {
                        this.displayName = request.vehicleGroup.manager.displayName
                    }
                }
            }
            val invoke0: suspend (RecallDeliveryExceptionRequest, Metadata) -> RecallDeliveryExceptionResponse = deliveriesDeliveryExceptionApprovalService::recallDeliveryException
            val reply0 = invoke0(outbound0, Metadata())
            val outbound1 = CloneVehicleRequest.newBuilder().apply {
                setContext(request.context)
                setVehicle(vehicle {
                    this.audit = auditInfo {
                        this.createdBy = reply0.deliveryException.audit.createdBy
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> fleetVehicleService.cloneVehicle(value) }
            val outbound2 = RequestFactory.create {
                countFixedAssetsRequest {
                    this.context = request.context
                    this.filter = CountFixedAssetsRequestKt.filter {
                        this.createdAfter = request.details.effectiveAt
                        this.region = reply1.vehicle.address.region
                    }
                }
            }
            val reply2 = assetsFixedAssetQueryService.countFixedAssets(outbound2)
            val outbound3 = scoreVenueRequest {
                this.context = request.context
                this.details = ScoreVenueRequestKt.details {
                    this.destination = postalAddress {
                        this.postalCode = reply1.vehicle.address.postalCode
                    }
                }
                this.venue = venue {
                    this.components.putAll(mapOf("primary" to venueComponent {
                        this.description = (reply1.vehicle.componentsMap["primary"] ?: VehicleComponent.getDefaultInstance()).description
                    }))
                    this.owner = userRef {
                        this.displayName = request.details.approver.displayName
                    }
                }
            }
            val reply3 = ClientRegistry.lookup("acme.venues.v1.VenueValidationService", VenueValidationServiceGrpcKt.VenueValidationServiceCoroutineStub::class.java).scoreVenue(outbound3)
            commentVehicleGroupApprovalResponse {
                this.outcome = CommentVehicleGroupApprovalResponseKt.outcome {
                    this.message = reply3.result.message
                }
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.details.putAll(reply2.result.detailsMap)
                    this.message = reply0.outcome.message
                    this.status = "ok"
                }
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.location = postalAddress {
                                this.postalCode = reply1.vehicle.address.postalCode
                            }
                        }))
                    }))
                    this.manager = userRef {
                        this.userId = reply1.vehicle.owner.userId
                    }
                    this.parentId = request.vehicleGroup.parentId
                    this.vehicleGroupId = request.vehicleGroupId
                }
            }
        }

    private suspend fun handleDelegateVehicleGroupApproval(request: DelegateVehicleGroupApprovalRequest): DelegateVehicleGroupApprovalResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupApprovalService/DelegateVehicleGroupApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DelegateVehicleGroupApproval", request)
            log.atInfo().addKeyValue("operation", "DelegateVehicleGroupApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicleGroup.vehicleGroupId).log("Recorded audit detail")
            val outbound0 = scheduleInspectionExpiryRequest {
                this.context = request.context
                this.details = ScheduleInspectionExpiryRequestKt.details {
                    this.notify = contactInfo {
                        this.email = request.vehicleGroup.manager.email
                    }
                }
                this.inspection = inspection {
                    this.assignee = userRef {
                        this.userId = request.details.approver.userId
                    }
                    this.audit = auditInfo {
                        this.updatedAt = request.vehicleGroup.audit.updatedAt
                    }
                }
            }
            val reply0 = fleetInspectionLifecycleService.scheduleInspectionExpiry(WireCopies.reflective(outbound0))
            val outbound1 = syncFuelPoliciesRequest {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.createdAt = reply0.inspection.audit.createdAt
                        this.createdBy = reply0.inspection.audit.createdBy
                    }
                    this.owner = userRef {
                        this.displayName = when (reply0.inspection.originCase) {
                            Inspection.OriginCase.CUSTOMER -> reply0.inspection.customer.displayName
                            else -> ""
                        }
                        this.userId = request.details.approver.userId
                    }
                }
            }
            val reply1 = fuelFuelPolicySyncService.syncFuelPolicies(outbound1)
            delegateVehicleGroupApprovalResponse {
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = "ok"
                }
                this.vehicleGroup = vehicleGroup {
                    this.depth = request.vehicleGroup.depth
                    this.manager = userRef {
                        this.userId = reply0.inspection.assignee.userId
                    }
                    this.vehicleGroupId = request.vehicleGroupId
                }
            }
        }

    private suspend fun handleGetVehicleGroupApprovalStatus(request: GetVehicleGroupApprovalStatusRequest): GetVehicleGroupApprovalStatusResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupApprovalService/GetVehicleGroupApprovalStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetVehicleGroupApprovalStatus", request)
            log.atInfo().addKeyValue("operation", "GetVehicleGroupApprovalStatus").log("Handling transport request")
            val outbound0 = scoreSearchIndexRequest {
                this.context = request.context
                this.searchIndex = searchIndex {
                    this.owner = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply0 = withTimeout(5_000) { searchSearchIndexValidationService.scoreSearchIndex(outbound0) }
            getVehicleGroupApprovalStatusResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.manager = userRef {
                                this.email = reply0.searchIndex.owner.email
                            }
                            this.name = reply0.searchIndex.name
                        }))
                    }))
                    this.vehicleGroupId = request.vehicleGroupId
                }
            }
        }

    private suspend fun handleListPendingVehicleGroups(request: ListPendingVehicleGroupsRequest): ListPendingVehicleGroupsResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupApprovalService/ListPendingVehicleGroups") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListPendingVehicleGroups", request)
            log.atInfo().addKeyValue("operation", "ListPendingVehicleGroups").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.ownerId).log("Recorded audit detail")
            val configure0: ListVehiclesRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.filter = ListVehiclesRequestKt.filter {
                    this.query = request.filter.query
                }
                this.page = pageRequest {
                    this.pageSize = request.page.pageSize
                    this.pageToken = request.page.pageToken
                }
            }
            val outbound0 = listVehiclesRequest(configure0)
            val reply0 = fleetVehicleService.listVehicles(outbound0)
            listPendingVehicleGroupsResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private fun RejectVehicleGroupRequest.toExplainSearchIndexDecisionFromRejectVehicleGroup1(reply0: ApproveVehicleResponse): ExplainSearchIndexDecisionRequest {
        val source = this
        return explainSearchIndexDecisionRequest {
            this.context = source.context
            this.searchIndex = searchIndex {
                this.audit = auditInfo {
                    this.createdBy = reply0.vehicle.audit.createdBy
                }
                this.owner = userRef {
                    this.displayName = source.details.approver.displayName
                }
            }
        }
    }

    private suspend fun handleRejectVehicleGroup(request: RejectVehicleGroupRequest): RejectVehicleGroupResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupApprovalService/RejectVehicleGroup") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RejectVehicleGroup", request)
            log.atInfo().addKeyValue("operation", "RejectVehicleGroup").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicleGroup.vehicleGroupId).log("Recorded audit detail")
            val outbound0 = approveVehicleRequest {}.fleetCopy {
                this.context = request.context
                this.vehicle = vehicle {
                    this.address = postalAddress {
                        this.lines.addAll(request.vehicleGroup.location.linesList)
                    }
                }
            }
            val reply0 = fleetVehicleApprovalService.approveVehicle(outbound0)
            val outbound1 = request.toExplainSearchIndexDecisionFromRejectVehicleGroup1(reply0)
            val reply1 = searchSearchIndexValidationService.explainSearchIndexDecision(outbound1)
            rejectVehicleGroupResponse {
                this.outcome = RejectVehicleGroupResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.vehicleGroup = vehicleGroup {
                    this.audit = auditInfo {
                        this.etag = reply1.searchIndex.audit.etag
                    }
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.audit = auditInfo {
                                this.etag = reply0.vehicle.audit.etag
                            }
                            this.location = postalAddress {
                                this.postalCode = reply0.vehicle.address.postalCode
                            }
                        }))
                    }))
                    this.vehicleGroupId = request.vehicleGroupId
                }
            }
        }

    private suspend fun handleSubmitVehicleGroup(request: SubmitVehicleGroupRequest): SubmitVehicleGroupResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupApprovalService/SubmitVehicleGroup") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SubmitVehicleGroup", request)
            log.atInfo().addKeyValue("operation", "SubmitVehicleGroup").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicleGroup.vehicleGroupId).log("Recorded audit detail")
            val outbound0 = exportLoanApplicationsRequest {
                this.context = request.context
                this.filter = ExportLoanApplicationsRequestKt.filter {
                    this.createdAfter = request.vehicleGroup.audit.updatedAt
                    this.labels.putAll(request.vehicleGroup.attributesMap)
                    this.ownerId = request.vehicleGroup.manager.userId
                    this.region = request.vehicleGroup.location.region
                }
            }
            lendingLoanApplicationQueryService.exportLoanApplications(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = quoteInspectionRequest {
                this.context = request.context
                this.inspection = inspection {
                    this.assignee = userRef {
                        this.userId = request.vehicleGroup.manager.userId
                    }
                    this.audit = auditInfo {
                        this.createdAt = request.vehicleGroup.audit.createdAt
                        this.updatedAt = request.vehicleGroup.audit.updatedAt
                    }
                    this.requester = contactInfo {
                        this.email = request.vehicleGroup.manager.email
                    }
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = fleetInspectionCalculationService.quoteInspection(envelope1.unpack(QuoteInspectionRequest::class.java))
            val outbound2 = validateVehicleRequest {
                this.context = request.context
                this.details = ValidateVehicleRequestKt.details {
                    this.contact = contactInfo {
                        this.preferredChannel = reply1.inspection.requester.preferredChannel
                    }
                }
            }
            val reply2 = fleetVehicleValidationService.validateVehicle(WireCopies.json(outbound2))
            submitVehicleGroupResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.audit = auditInfo {
                                this.createdBy = reply1.inspection.audit.createdBy
                            }
                            this.manager = userRef {
                                this.displayName = reply1.inspection.assignee.displayName
                            }
                        }))
                        this.location = postalAddress {
                            this.postalCode = reply2.vehicle.address.postalCode
                        }
                    }))
                    this.depth = request.vehicleGroup.depth
                    this.vehicleGroupId = request.vehicleGroupId
                }
            }
        }
}
