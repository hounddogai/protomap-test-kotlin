package com.acme.transport.fleet.grpc

import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.ExplainAuditEventDecisionRequestKt
import com.acme.audit.v1.auditEvent
import com.acme.audit.v1.explainAuditEventDecisionRequest
import com.acme.authorization.v1.AccessPolicyQueryServiceGrpcKt
import com.acme.authorization.v1.SummarizeAccessPoliciesRequest
import com.acme.authorization.v1.SummarizeAccessPoliciesRequestKt
import com.acme.authorization.v1.SummarizeAccessPoliciesResponse
import com.acme.authorization.v1.summarizeAccessPoliciesRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.pageInfo
import com.acme.common.v1.postalAddress
import com.acme.dispatch.v1.CalculateDispatchAssignmentRequest
import com.acme.dispatch.v1.CalculateDispatchAssignmentRequestKt
import com.acme.dispatch.v1.DispatchAssignmentCalculationServiceGrpcKt
import com.acme.dispatch.v1.calculateDispatchAssignmentRequest
import com.acme.dispatch.v1.dispatchAssignment
import com.acme.featureflags.v1.FeatureFlagValidationServiceGrpcKt
import com.acme.featureflags.v1.featureFlag
import com.acme.featureflags.v1.lintFeatureFlagsRequest
import com.acme.fleet.v1.BatchCreateVehiclesRequest
import com.acme.fleet.v1.BatchCreateVehiclesResponse
import com.acme.fleet.v1.BatchGetVehiclesRequest
import com.acme.fleet.v1.BatchGetVehiclesResponse
import com.acme.fleet.v1.CloneVehicleRequest
import com.acme.fleet.v1.CloneVehicleResponse
import com.acme.fleet.v1.CreateVehicleRequest
import com.acme.fleet.v1.CreateVehicleResponse
import com.acme.fleet.v1.DeleteVehicleRequest
import com.acme.fleet.v1.DeleteVehicleResponse
import com.acme.fleet.v1.ExplainInspectionCalculationRequest
import com.acme.fleet.v1.ExplainInspectionCalculationResponse
import com.acme.fleet.v1.GetVehicleRequest
import com.acme.fleet.v1.GetVehicleResponse
import com.acme.fleet.v1.GetVehicleRulesRequest
import com.acme.fleet.v1.InspectionCalculationServiceGrpcKt
import com.acme.fleet.v1.InspectionValidationServiceGrpcKt
import com.acme.fleet.v1.ListVehiclesRequest
import com.acme.fleet.v1.ListVehiclesResponse
import com.acme.fleet.v1.NormalizeInspectionRequest
import com.acme.fleet.v1.ResolveVehiclesRequest
import com.acme.fleet.v1.ScoreInspectionRequest
import com.acme.fleet.v1.ScoreInspectionResponse
import com.acme.fleet.v1.SearchVehiclesRequest
import com.acme.fleet.v1.SearchVehiclesResponse
import com.acme.fleet.v1.SimulateInspectionRequestKt
import com.acme.fleet.v1.VehicleComponent
import com.acme.fleet.v1.VehicleGroupCalculationServiceGrpcKt
import com.acme.fleet.v1.VehicleQueryServiceGrpcKt
import com.acme.fleet.v1.VehicleServiceGrpcKt
import com.acme.fleet.v1.VehicleValidationServiceGrpcKt
import com.acme.fleet.v1.WatchVehiclesRequest
import com.acme.fleet.v1.WatchVehiclesResponse
import com.acme.fleet.v1.batchCreateVehiclesResponse
import com.acme.fleet.v1.batchGetVehiclesResponse
import com.acme.fleet.v1.cloneVehicleResponse
import com.acme.fleet.v1.createVehicleResponse
import com.acme.fleet.v1.deleteVehicleResponse
import com.acme.fleet.v1.explainInspectionCalculationRequest
import com.acme.fleet.v1.getVehicleResponse
import com.acme.fleet.v1.getVehicleRulesRequest
import com.acme.fleet.v1.inspection
import com.acme.fleet.v1.inspectionChannelOrigin
import com.acme.fleet.v1.inspectionSla
import com.acme.fleet.v1.listVehiclesResponse
import com.acme.fleet.v1.normalizeInspectionRequest
import com.acme.fleet.v1.peekVehicleRequest
import com.acme.fleet.v1.quoteVehicleGroupRequest
import com.acme.fleet.v1.resolveVehiclesRequest
import com.acme.fleet.v1.scoreInspectionRequest
import com.acme.fleet.v1.searchVehiclesResponse
import com.acme.fleet.v1.simulateInspectionRequest
import com.acme.fleet.v1.vehicle
import com.acme.fleet.v1.vehicleComponent
import com.acme.fleet.v1.vehicleGroup
import com.acme.fleet.v1.watchVehiclesResponse
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.ServiceScheduleValidationServiceGrpcKt
import com.acme.maintenance.v1.ValidateServiceScheduleResponse
import com.acme.maintenance.v1.copy as maintenanceCopy
import com.acme.maintenance.v1.serviceSchedule
import com.acme.maintenance.v1.serviceScheduleParticipant
import com.acme.maintenance.v1.validateServiceScheduleRequest
import com.acme.performers.v1.BatchGetPerformersRequest
import com.acme.performers.v1.BatchGetPerformersResponse
import com.acme.performers.v1.PerformerServiceGrpcKt
import com.acme.performers.v1.batchGetPerformersRequest
import com.acme.telematics.v1.ListTelemetryReadingsRequest
import com.acme.telematics.v1.ListTelemetryReadingsRequestKt
import com.acme.telematics.v1.TelemetryReadingServiceGrpcKt
import com.acme.telematics.v1.listTelemetryReadingsRequest
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Manages vehicles: creates, reads, changes, and lists them. */
class VehicleEndpoint(
    private val store: FleetRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventValidationService: AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub,
    private val authorizationAccessPolicyQueryService: AccessPolicyQueryServiceGrpcKt.AccessPolicyQueryServiceCoroutineStub,
    private val dispatchDispatchAssignmentCalculationService: DispatchAssignmentCalculationServiceGrpcKt.DispatchAssignmentCalculationServiceCoroutineStub,
    private val featureflagsFeatureFlagValidationService: FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub,
    private val fleetInspectionCalculationService: InspectionCalculationServiceGrpcKt.InspectionCalculationServiceCoroutineStub,
    private val fleetInspectionValidationService: InspectionValidationServiceGrpcKt.InspectionValidationServiceCoroutineStub,
    private val fleetVehicleGroupCalculationService: VehicleGroupCalculationServiceGrpcKt.VehicleGroupCalculationServiceCoroutineStub,
    private val fleetVehicleQueryService: VehicleQueryServiceGrpcKt.VehicleQueryServiceCoroutineStub,
    private val fleetVehicleValidationService: VehicleValidationServiceGrpcKt.VehicleValidationServiceCoroutineStub,
    private val maintenanceServiceScheduleValidationService: ServiceScheduleValidationServiceGrpcKt.ServiceScheduleValidationServiceCoroutineStub,
    private val performersPerformerService: PerformerServiceGrpcKt.PerformerServiceCoroutineStub,
    private val telematicsTelemetryReadingService: TelemetryReadingServiceGrpcKt.TelemetryReadingServiceCoroutineStub,
) : VehicleServiceGrpcKt.VehicleServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(VehicleEndpoint::class.java)

    private val invokeCreateVehicle2: suspend (ExplainInspectionCalculationRequest) -> ExplainInspectionCalculationResponse = { payload ->
        fleetInspectionCalculationService.explainInspectionCalculation(payload)
    }

    private val workerForListVehicles1 = ReplyMailbox<SummarizeAccessPoliciesRequest, SummarizeAccessPoliciesResponse>(workers) { payload ->
        withTimeout(5_000) { authorizationAccessPolicyQueryService.summarizeAccessPolicies(payload) }
    }

    private val clientForWatchVehicles0 by lazy { fleetInspectionCalculationService }

    /** Takes a stream of vehicles and answers once at the end. */
    override suspend fun batchCreateVehicles(requests: Flow<BatchCreateVehiclesRequest>): BatchCreateVehiclesResponse {
        var answer = BatchCreateVehiclesResponse.getDefaultInstance()
        requests.map { request -> handleBatchCreateVehicles(request) }.collect { answer = it }
        return answer
    }

    /** Returns several vehicles at once. */
    override suspend fun batchGetVehicles(request: BatchGetVehiclesRequest): BatchGetVehiclesResponse {
        return handleBatchGetVehicles(request)
    }

    /** Creates a vehicle. */
    override suspend fun cloneVehicle(request: CloneVehicleRequest): CloneVehicleResponse = handleCloneVehicle(request)

    /** Creates a vehicle. */
    override suspend fun createVehicle(request: CreateVehicleRequest): CreateVehicleResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleService/CreateVehicle") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CreateVehicle", request)
            log.atInfo().addKeyValue("operation", "CreateVehicle").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicle.code).log("Recorded audit detail")
            val outbound0 = normalizeInspectionRequest {
                this.context = request.context
                this.inspection = inspection {
                    this.assignee = userRef {
                        this.userId = request.vehicle.owner.userId
                    }
                    this.audit = auditInfo {
                        this.createdAt = request.vehicle.audit.createdAt
                        this.updatedBy = request.vehicle.audit.updatedBy
                    }
                }
            }
            val command0: Submission<NormalizeInspectionRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = fleetInspectionValidationService.normalizeInspection(payload0)
            val outbound1 = scoreInspectionRequest {
                this.context = request.context
                this.inspection = inspection {
                    this.audit = auditInfo {
                        this.updatedBy = request.vehicle.audit.updatedBy
                    }
                    this.channel = inspectionChannelOrigin {
                        this.address = reply0.inspection.channel.address
                    }
                    this.requester = contactInfo {
                        this.email = reply0.inspection.assignee.email
                        this.preferredChannel = reply0.inspection.requester.preferredChannel
                    }
                }
            }
            val adapter1 = OutboundPort<ScoreInspectionRequest, ScoreInspectionResponse> { payload ->
                fleetInspectionValidationService.scoreInspection(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            val outbound2 = explainInspectionCalculationRequest {
                this.context = request.context
                this.inspection = inspection {
                    this.assignee = userRef {
                        this.email = reply0.inspection.assignee.email
                    }
                    this.audit = auditInfo {
                        this.updatedAt = reply0.inspection.audit.updatedAt
                    }
                }
            }
            val reply2 = invokeCreateVehicle2(outbound2)
            createVehicleResponse {
                this.result = operationResult {
                    this.message = reply2.outcome.message
                    this.status = "ok"
                }
                this.vehicle = vehicle {
                    this.owner = userRef {
                        this.email = reply0.inspection.assignee.email
                    }
                    this.specs.putAll(request.vehicle.specsMap)
                    this.vehicleId = request.vehicle.vehicleId
                }
            }
        }

    /** Deletes a vehicle. */
    override suspend fun deleteVehicle(request: DeleteVehicleRequest): DeleteVehicleResponse {
        return handleDeleteVehicle(request)
    }

    /** Returns a vehicle. */
    override suspend fun getVehicle(request: GetVehicleRequest): GetVehicleResponse = handleGetVehicle(request)

    /** Lists vehicles a page at a time. */
    override suspend fun listVehicles(request: ListVehiclesRequest): ListVehiclesResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleService/ListVehicles") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListVehicles", request)
            log.atInfo().addKeyValue("operation", "ListVehicles").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = explainAuditEventDecisionRequest {
                this.auditEvent = auditEvent {
                    this.occurredAt = request.filter.createdAfter
                }
                this.context = request.context
                this.details = ExplainAuditEventDecisionRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = auditAuditEventValidationService.explainAuditEventDecision(WireCopies.reflective(outbound0))
            val outbound1 = summarizeAccessPoliciesRequest {
                this.annotations.putAll(reply0.result.detailsMap)
                this.context = request.context
                this.details = SummarizeAccessPoliciesRequestKt.details {
                    this.filters.putAll(reply0.auditEvent.attributesMap)
                }
            }
            val reply1 = workerForListVehicles1.submit(outbound1)
            listVehiclesResponse {
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = "ok"
                }
                this.vehicles.addAll(listOf(vehicle {
                    this.owner = userRef {
                        this.displayName = reply0.auditEvent.subject.displayName
                    }
                }))
            }
        }

    /** Lists vehicles a page at a time. */
    override suspend fun searchVehicles(request: SearchVehiclesRequest): SearchVehiclesResponse {
        return handleSearchVehicles(request)
    }

    /** Streams the changes of vehicles as they happen. */
    override fun watchVehicles(request: WatchVehiclesRequest): Flow<WatchVehiclesResponse> = flow {
        emit(handleWatchVehicles(request))
    }

    private fun BatchCreateVehiclesRequest.toCalculateDispatchAssignmentFromBatchCreateVehicles2(reply1: ValidateServiceScheduleResponse): CalculateDispatchAssignmentRequest {
        val source = this
        return calculateDispatchAssignmentRequest {
            this.context = source.context
            this.details = CalculateDispatchAssignmentRequestKt.details {
                this.destination = postalAddress {
                    this.postalCode = source.record.address.postalCode
                }
            }
            this.dispatchAssignment = dispatchAssignment {
                this.assignee = userRef {
                    this.email = reply1.serviceSchedule.organizer.email
                }
                this.audit = auditInfo {
                    this.createdBy = reply1.serviceSchedule.audit.createdBy
                }
                this.requester = contactInfo {
                    this.name = source.header.submittedBy.name
                }
            }
        }
    }

    private suspend fun handleBatchCreateVehicles(request: BatchCreateVehiclesRequest): BatchCreateVehiclesResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleService/BatchCreateVehicles") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BatchCreateVehicles", request)
            log.atInfo().addKeyValue("operation", "BatchCreateVehicles").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.header.source).log("Recorded audit detail")
            val outbound0 = quoteVehicleGroupRequest {
                this.context = request.context
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.audit = auditInfo {
                                this.createdAt = request.record.audit.createdAt
                            }
                            this.location = postalAddress {
                                this.region = request.record.address.region
                            }
                            this.manager = userRef {
                                this.userId = request.record.owner.userId
                            }
                        }))
                    }))
                }
            }
            val reply0 = fleetVehicleGroupCalculationService.quoteVehicleGroup(outbound0)
            val outbound1 = validateServiceScheduleRequest {}.maintenanceCopy {
                this.context = request.context
                this.serviceSchedule = serviceSchedule {
                    this.audit = auditInfo {
                        this.updatedAt = request.record.audit.updatedAt
                        this.updatedBy = reply0.vehicleGroup.audit.updatedBy
                    }
                    this.participants.addAll(listOf(serviceScheduleParticipant {
                        this.contact = contactInfo {
                            this.email = request.record.owner.email
                            this.name = request.header.submittedBy.name
                        }
                    }))
                }
            }
            val reply1 = maintenanceServiceScheduleValidationService.validateServiceSchedule(outbound1)
            val outbound2 = request.toCalculateDispatchAssignmentFromBatchCreateVehicles2(reply1)
            val reply2 = dispatchDispatchAssignmentCalculationService.calculateDispatchAssignment(outbound2)
            val outbound3 = explainInspectionCalculationRequest {
                this.context = request.context
                this.inspection = inspection {
                    this.assignee = userRef {
                        this.userId = reply1.serviceSchedule.organizer.userId
                    }
                    this.requester = contactInfo {
                        this.email = reply2.dispatchAssignment.requester.email
                        this.phone = reply2.dispatchAssignment.requester.phone
                    }
                    this.sla = inspectionSla {
                        this.breached = reply2.dispatchAssignment.sla.breached
                    }
                }
            }
            val reply3 = fleetInspectionCalculationService.explainInspectionCalculation(outbound3)
            batchCreateVehiclesResponse {
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply1.result.message
                    this.referenceIds.addAll(reply3.result.referenceIdsList)
                    this.status = reply3.outcome.status
                }
            }
        }

    private suspend fun handleBatchGetVehicles(request: BatchGetVehiclesRequest): BatchGetVehiclesResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleService/BatchGetVehicles") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BatchGetVehicles", request)
            log.atInfo().addKeyValue("operation", "BatchGetVehicles").log("Handling transport request")
            val outbound0 = resolveVehiclesRequest {
                this.context = request.context
                this.vehicleIds.addAll(request.vehicleIdsList)
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = fleetVehicleQueryService.resolveVehicles(envelope0.unpack(ResolveVehiclesRequest::class.java))
            batchGetVehiclesResponse {
                this.pageInfo = pageInfo {
                    this.totalSize = reply0.pageInfo.totalSize
                }
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }

    private data class CloneVehicle1Command(
        val context: RequestContext,
        val filterRegion: String,
        val filterCreatedAfter: Timestamp,
        val filterOwnerId: String
    ) {
        fun toRequest(): ListTelemetryReadingsRequest {
            val command = this
            return listTelemetryReadingsRequest {
                this.context = command.context
                this.filter = ListTelemetryReadingsRequestKt.filter {
                    this.createdAfter = command.filterCreatedAfter
                    this.ownerId = command.filterOwnerId
                    this.region = command.filterRegion
                }
            }
        }
    }

    private suspend fun handleCloneVehicle(request: CloneVehicleRequest): CloneVehicleResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleService/CloneVehicle") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CloneVehicle", request)
            log.atInfo().addKeyValue("operation", "CloneVehicle").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicle.vehicleId).log("Recorded audit detail")
            val outbound0 = lintFeatureFlagsRequest {
                this.context = request.context
                this.featureFlag = featureFlag {
                    this.description = (request.vehicle.componentsMap["primary"] ?: VehicleComponent.getDefaultInstance()).description
                    this.owner = userRef {
                        this.displayName = request.vehicle.owner.displayName
                        this.userId = request.vehicle.owner.userId
                    }
                }
            }
            val reply0 = featureflagsFeatureFlagValidationService.lintFeatureFlags(WireCopies.json(outbound0))
            val outbound1 = CloneVehicle1Command(
                context = request.context,
                filterRegion = request.vehicle.address.region,
                filterCreatedAfter = request.vehicle.audit.createdAt,
                filterOwnerId = reply0.featureFlag.featureFlagId
            ).toRequest()
            val reply1 = telematicsTelemetryReadingService.listTelemetryReadings(outbound1)
            cloneVehicleResponse {
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
                this.vehicle = vehicle {
                    this.audit = auditInfo {
                        this.etag = reply0.featureFlag.audit.etag
                    }
                    this.components.putAll(mapOf("primary" to vehicleComponent {
                        this.description = reply0.featureFlag.description
                    }))
                    this.name = request.vehicle.name
                    this.owner = request.vehicle.owner
                }
            }
        }

    private suspend fun handleDeleteVehicle(request: DeleteVehicleRequest): DeleteVehicleResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleService/DeleteVehicle") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DeleteVehicle", request)
            log.atInfo().addKeyValue("operation", "DeleteVehicle").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.reason).log("Recorded audit detail")
            val outbound0 = batchGetPerformersRequest {
                this.context = request.context
            }
            val invoke0: suspend (BatchGetPerformersRequest, Metadata) -> BatchGetPerformersResponse = performersPerformerService::batchGetPerformers
            val reply0 = invoke0(outbound0, Metadata())
            val outbound1 = GetVehicleRulesRequest.newBuilder().apply {
                setContext(request.context)
                setVehicleId(request.vehicleId)
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> fleetVehicleValidationService.getVehicleRules(value) }
            val outbound2 = RequestFactory.create {
                peekVehicleRequest {
                    this.context = request.context
                    this.vehicleId = request.vehicleId
                }
            }
            val reply2 = fleetVehicleQueryService.peekVehicle(outbound2)
            deleteVehicleResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.referenceIds.addAll(reply2.result.referenceIdsList)
                    this.status = reply1.result.status
                }
            }
        }

    private suspend fun handleGetVehicle(request: GetVehicleRequest): GetVehicleResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleService/GetVehicle") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetVehicle", request)
            log.atInfo().addKeyValue("operation", "GetVehicle").log("Handling transport request")
            val outbound0 = simulateInspectionRequest {
                this.context = request.context
                this.inspection = inspection {
                    this.assignee = userRef {
                        this.userId = request.context.actor.userId
                    }
                    this.externalReference = request.vehicleId
                    this.requester = contactInfo {
                        this.email = request.context.actor.email
                    }
                    this.sla = inspectionSla {
                        this.dueAt = request.context.deadline
                    }
                }
            }
            val reply0 = ClientRegistry.lookup("acme.fleet.v1.InspectionCalculationService", InspectionCalculationServiceGrpcKt.InspectionCalculationServiceCoroutineStub::class.java).simulateInspection(outbound0)
            getVehicleResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.vehicle = vehicle {
                    this.tags.addAll(reply0.inspection.tagsList)
                    this.vehicleId = request.vehicleId
                }
            }
        }

    private suspend fun handleSearchVehicles(request: SearchVehiclesRequest): SearchVehiclesResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleService/SearchVehicles") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SearchVehicles", request)
            log.atInfo().addKeyValue("operation", "SearchVehicles").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            searchVehiclesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleWatchVehicles(request: WatchVehiclesRequest): WatchVehiclesResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleService/WatchVehicles") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "WatchVehicles", request)
            log.atInfo().addKeyValue("operation", "WatchVehicles").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = simulateInspectionRequest {
                this.context = request.context
                this.details = SimulateInspectionRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.filter.region
                    }
                }
                this.inspection = inspection {
                    this.requester = contactInfo {
                        this.email = request.subscriber.email
                        this.name = request.subscriber.name
                        this.preferredChannel = request.subscriber.preferredChannel
                    }
                }
            }
            val reply0 = clientForWatchVehicles0.simulateInspection(outbound0)
            watchVehiclesResponse {
                this.result = operationResult {
                    this.status = reply0.result.status
                }
                this.vehicle = vehicle {
                    this.audit = auditInfo {
                        this.createdBy = reply0.inspection.audit.createdBy
                    }
                    this.owner = userRef {
                        this.email = request.subscriber.email
                    }
                }
            }
        }
}
