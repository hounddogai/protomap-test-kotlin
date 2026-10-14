package com.acme.transport.fleet.adapters

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.concessions.v1.ConcessionStand
import com.acme.concessions.v1.ConcessionStandServiceGrpcKt
import com.acme.concessions.v1.UpdateConcessionStandRequest
import com.acme.concessions.v1.concessionStand
import com.acme.concessions.v1.updateConcessionStandRequest
import com.acme.dispatch.v1.DispatchAssignmentCalculationServiceGrpcKt
import com.acme.dispatch.v1.EstimateDispatchAssignmentRequest
import com.acme.dispatch.v1.EstimateDispatchAssignmentResponse
import com.acme.dispatch.v1.dispatchAssignment
import com.acme.dispatch.v1.dispatchAssignmentSla
import com.acme.dispatch.v1.estimateDispatchAssignmentRequest
import com.acme.documents.v1.CheckDocumentEligibilityRequestKt
import com.acme.documents.v1.DocumentValidationServiceGrpcKt
import com.acme.documents.v1.checkDocumentEligibilityRequest
import com.acme.drivers.v1.DutyStatusValidationServiceGrpcKt
import com.acme.drivers.v1.NormalizeDutyStatusRequestKt
import com.acme.drivers.v1.driverLicenseRef
import com.acme.drivers.v1.dutyStatus
import com.acme.drivers.v1.normalizeDutyStatusRequest
import com.acme.fleet.v1.BrowseVehicleGroupsRequest
import com.acme.fleet.v1.BrowseVehicleGroupsResponse
import com.acme.fleet.v1.CalculateInspectionRequestKt
import com.acme.fleet.v1.CheckVehicleGroupEligibilityRequest
import com.acme.fleet.v1.CompareInspectionOptionsRequest
import com.acme.fleet.v1.CompareInspectionOptionsRequestKt
import com.acme.fleet.v1.CompareInspectionOptionsResponse
import com.acme.fleet.v1.CountVehicleGroupsRequest
import com.acme.fleet.v1.CountVehicleGroupsResponse
import com.acme.fleet.v1.EstimateVehicleGroupRequestKt
import com.acme.fleet.v1.ExplainInspectionCalculationRequest
import com.acme.fleet.v1.ExplainInspectionCalculationResponse
import com.acme.fleet.v1.ExportVehicleGroupsRequest
import com.acme.fleet.v1.ExportVehicleGroupsResponse
import com.acme.fleet.v1.FindVehicleGroupsRequest
import com.acme.fleet.v1.FindVehicleGroupsResponse
import com.acme.fleet.v1.InspectionCalculationServiceGrpcKt
import com.acme.fleet.v1.LookupVehicleGroupRequest
import com.acme.fleet.v1.LookupVehicleGroupResponse
import com.acme.fleet.v1.PeekVehicleGroupRequest
import com.acme.fleet.v1.PeekVehicleGroupResponse
import com.acme.fleet.v1.ResolveVehicleGroupsRequest
import com.acme.fleet.v1.ResolveVehicleGroupsResponse
import com.acme.fleet.v1.ResolveVehiclesRequest
import com.acme.fleet.v1.ResolveVehiclesResponse
import com.acme.fleet.v1.SuggestVehicleGroupsRequest
import com.acme.fleet.v1.SuggestVehicleGroupsResponse
import com.acme.fleet.v1.SummarizeVehicleGroupsRequest
import com.acme.fleet.v1.SummarizeVehicleGroupsResponse
import com.acme.fleet.v1.VehicleGroupCalculationServiceGrpcKt
import com.acme.fleet.v1.VehicleGroupQueryServiceGrpcKt
import com.acme.fleet.v1.VehicleGroupValidationServiceGrpcKt
import com.acme.fleet.v1.VehicleQueryServiceGrpcKt
import com.acme.fleet.v1.browseVehicleGroupsResponse
import com.acme.fleet.v1.calculateInspectionRequest
import com.acme.fleet.v1.checkVehicleGroupEligibilityRequest
import com.acme.fleet.v1.compareInspectionOptionsRequest
import com.acme.fleet.v1.countVehicleGroupsResponse
import com.acme.fleet.v1.estimateVehicleGroupRequest
import com.acme.fleet.v1.explainInspectionCalculationRequest
import com.acme.fleet.v1.exportVehicleGroupsResponse
import com.acme.fleet.v1.findVehicleGroupsResponse
import com.acme.fleet.v1.inspection
import com.acme.fleet.v1.inspectionSla
import com.acme.fleet.v1.lookupVehicleGroupResponse
import com.acme.fleet.v1.peekVehicleGroupResponse
import com.acme.fleet.v1.resolveVehicleGroupsResponse
import com.acme.fleet.v1.resolveVehiclesRequest
import com.acme.fleet.v1.suggestVehicleGroupsResponse
import com.acme.fleet.v1.summarizeVehicleGroupsResponse
import com.acme.fleet.v1.vehicleGroup
import com.acme.fleet.v1.vehicleGroupRef
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.ServiceScheduleValidationServiceGrpcKt
import com.acme.maintenance.v1.ValidateServiceScheduleRequest
import com.acme.maintenance.v1.serviceSchedule
import com.acme.maintenance.v1.validateServiceScheduleRequest
import com.acme.notifications.v1.ListNotificationsRequestKt
import com.acme.notifications.v1.NotificationServiceGrpcKt
import com.acme.notifications.v1.listNotificationsRequest
import com.acme.search.v1.FindSearchIndexesRequestKt
import com.acme.search.v1.SearchIndexQueryServiceGrpcKt
import com.acme.search.v1.SummarizeSearchIndexesRequest
import com.acme.search.v1.SummarizeSearchIndexesRequestKt
import com.acme.search.v1.findSearchIndexesRequest
import com.acme.search.v1.summarizeSearchIndexesRequest
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
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Answers read-only questions about vehicle groups, for screens and reports. */
class VehicleGroupQueryController(
    private val store: FleetRepository,
    private val workers: CoroutineScope,
    private val concessionsConcessionStandService: ConcessionStandServiceGrpcKt.ConcessionStandServiceCoroutineStub,
    private val dispatchDispatchAssignmentCalculationService: DispatchAssignmentCalculationServiceGrpcKt.DispatchAssignmentCalculationServiceCoroutineStub,
    private val documentsDocumentValidationService: DocumentValidationServiceGrpcKt.DocumentValidationServiceCoroutineStub,
    private val driversDutyStatusValidationService: DutyStatusValidationServiceGrpcKt.DutyStatusValidationServiceCoroutineStub,
    private val fleetInspectionCalculationService: InspectionCalculationServiceGrpcKt.InspectionCalculationServiceCoroutineStub,
    private val fleetVehicleGroupCalculationService: VehicleGroupCalculationServiceGrpcKt.VehicleGroupCalculationServiceCoroutineStub,
    private val fleetVehicleGroupValidationService: VehicleGroupValidationServiceGrpcKt.VehicleGroupValidationServiceCoroutineStub,
    private val fleetVehicleQueryService: VehicleQueryServiceGrpcKt.VehicleQueryServiceCoroutineStub,
    private val maintenanceServiceScheduleValidationService: ServiceScheduleValidationServiceGrpcKt.ServiceScheduleValidationServiceCoroutineStub,
    private val notificationsNotificationService: NotificationServiceGrpcKt.NotificationServiceCoroutineStub,
    private val searchSearchIndexQueryService: SearchIndexQueryServiceGrpcKt.SearchIndexQueryServiceCoroutineStub,
) : VehicleGroupQueryServiceGrpcKt.VehicleGroupQueryServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(VehicleGroupQueryController::class.java)

    private val invokeFindVehicleGroups1: suspend (CompareInspectionOptionsRequest) -> CompareInspectionOptionsResponse = { payload ->
        fleetInspectionCalculationService.compareInspectionOptions(payload)
    }

    private val workerForSummarizeVehicleGroups0 = ReplyMailbox<ExplainInspectionCalculationRequest, ExplainInspectionCalculationResponse>(workers) { payload ->
        withTimeout(5_000) { fleetInspectionCalculationService.explainInspectionCalculation(payload) }
    }

    private val clientForSummarizeVehicleGroups1 by lazy { notificationsNotificationService }

    /** Lists vehicle groups a page at a time. */
    override suspend fun browseVehicleGroups(request: BrowseVehicleGroupsRequest): BrowseVehicleGroupsResponse = handleBrowseVehicleGroups(request)

    /** Counts vehicle groups by status. */
    override suspend fun countVehicleGroups(request: CountVehicleGroupsRequest): CountVehicleGroupsResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupQueryService/CountVehicleGroups") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CountVehicleGroups", request)
            log.atInfo().addKeyValue("operation", "CountVehicleGroups").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = request.toUpdateConcessionStandFromCountVehicleGroups0()
            val reply0 = concessionsConcessionStandService.updateConcessionStand(outbound0)
            val outbound1 = calculateInspectionRequest {
                this.context = request.context
                this.details = CalculateInspectionRequestKt.details {
                    this.destination = postalAddress {
                        this.locality = when (reply0.concessionStand.locatorCase) {
                            ConcessionStand.LocatorCase.ADDRESS -> reply0.concessionStand.address.locality
                            else -> ""
                        }
                        this.recipient = when (reply0.concessionStand.locatorCase) {
                            ConcessionStand.LocatorCase.ADDRESS -> reply0.concessionStand.address.recipient
                            else -> ""
                        }
                    }
                }
                this.inspection = inspection {
                    this.assignee = userRef {
                        this.email = reply0.concessionStand.owner.email
                        this.userId = reply0.concessionStand.owner.userId
                    }
                }
            }
            val reply1 = fleetInspectionCalculationService.calculateInspection(outbound1)
            countVehicleGroupsResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    /** Streams every matching vehicle group. */
    override fun exportVehicleGroups(request: ExportVehicleGroupsRequest): Flow<ExportVehicleGroupsResponse> = flow {
        emit(handleExportVehicleGroups(request))
    }

    /** Lists vehicle groups a page at a time. */
    override suspend fun findVehicleGroups(request: FindVehicleGroupsRequest): FindVehicleGroupsResponse = handleFindVehicleGroups(request)

    /** Returns a vehicle group. */
    override suspend fun lookupVehicleGroup(request: LookupVehicleGroupRequest): LookupVehicleGroupResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupQueryService/LookupVehicleGroup") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LookupVehicleGroup", request)
            log.atInfo().addKeyValue("operation", "LookupVehicleGroup").log("Handling transport request")
            val outbound0 = estimateDispatchAssignmentRequest {
                this.context = request.context
                this.dispatchAssignment = dispatchAssignment {
                    this.assignee = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                    this.sla = dispatchAssignmentSla {
                        this.dueAt = request.context.deadline
                    }
                    this.subject = request.vehicleGroupId
                }
            }
            val invoke0: suspend (EstimateDispatchAssignmentRequest, Metadata) -> EstimateDispatchAssignmentResponse = dispatchDispatchAssignmentCalculationService::estimateDispatchAssignment
            val reply0 = invoke0(outbound0, Metadata())
            val outbound1 = CompareInspectionOptionsRequest.newBuilder().apply {
                setContext(request.context)
                setInspection(inspection {
                    this.assignee = userRef {
                        this.email = reply0.dispatchAssignment.assignee.email
                        this.userId = reply0.dispatchAssignment.assignee.userId
                    }
                    this.requester = contactInfo {
                        this.name = reply0.dispatchAssignment.requester.name
                    }
                    this.sla = inspectionSla {
                        this.dueAt = reply0.dispatchAssignment.sla.dueAt
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> fleetInspectionCalculationService.compareInspectionOptions(value) }
            lookupVehicleGroupResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.audit = auditInfo {
                                this.etag = reply1.inspection.audit.etag
                            }
                            this.manager = userRef {
                                this.displayName = reply0.dispatchAssignment.customer.displayName
                            }
                        }))
                        this.manager = userRef {
                            this.displayName = reply0.dispatchAssignment.assignee.displayName
                        }
                    }))
                    this.vehicleGroupId = request.vehicleGroupId
                }
            }
        }

    /** Returns a vehicle group. */
    override suspend fun peekVehicleGroup(request: PeekVehicleGroupRequest): PeekVehicleGroupResponse {
        return handlePeekVehicleGroup(request)
    }

    /** Returns several vehicle groups at once. */
    override suspend fun resolveVehicleGroups(request: ResolveVehicleGroupsRequest): ResolveVehicleGroupsResponse = handleResolveVehicleGroups(request)

    /** Lists vehicle groups a page at a time. */
    override suspend fun suggestVehicleGroups(request: SuggestVehicleGroupsRequest): SuggestVehicleGroupsResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupQueryService/SuggestVehicleGroups") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SuggestVehicleGroups", request)
            log.atInfo().addKeyValue("operation", "SuggestVehicleGroups").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = estimateVehicleGroupRequest {
                this.context = request.context
                this.details = EstimateVehicleGroupRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.filter.region
                    }
                }
            }
            val reply0 = fleetVehicleGroupCalculationService.estimateVehicleGroup(WireCopies.reflective(outbound0))
            suggestVehicleGroupsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.vehicleGroups.addAll(listOf(vehicleGroup {
                    this.audit = auditInfo {
                        this.createdAt = reply0.vehicleGroup.audit.createdAt
                    }
                    this.children.addAll(listOf(vehicleGroup {
                        this.audit = auditInfo {
                            this.updatedAt = reply0.vehicleGroup.audit.updatedAt
                        }
                    }))
                }))
            }
        }

    /** Summarizes a vehicle group. */
    override suspend fun summarizeVehicleGroups(request: SummarizeVehicleGroupsRequest): SummarizeVehicleGroupsResponse {
        return handleSummarizeVehicleGroups(request)
    }

    private suspend fun handleBrowseVehicleGroups(request: BrowseVehicleGroupsRequest): BrowseVehicleGroupsResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupQueryService/BrowseVehicleGroups") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BrowseVehicleGroups", request)
            log.atInfo().addKeyValue("operation", "BrowseVehicleGroups").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            browseVehicleGroupsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private fun CountVehicleGroupsRequest.toUpdateConcessionStandFromCountVehicleGroups0(): UpdateConcessionStandRequest {
        val source = this
        return updateConcessionStandRequest {
            this.concessionStand = concessionStand {
                this.address = postalAddress {
                    this.region = source.filter.region
                }
                this.owner = userRef {
                    this.email = source.context.actor.email
                    this.userId = source.context.actor.userId
                }
            }
            this.context = source.context
        }
    }

    private data class ExportVehicleGroups2Command(
        val context: RequestContext,
        val serviceScheduleLocationLines: List<String>
    ) {
        fun toRequest(): ValidateServiceScheduleRequest {
            val command = this
            return validateServiceScheduleRequest {
                this.context = command.context
                this.serviceSchedule = serviceSchedule {
                    this.location = postalAddress {
                        this.lines.addAll(command.serviceScheduleLocationLines)
                    }
                }
            }
        }
    }

    private suspend fun handleExportVehicleGroups(request: ExportVehicleGroupsRequest): ExportVehicleGroupsResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupQueryService/ExportVehicleGroups") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExportVehicleGroups", request)
            log.atInfo().addKeyValue("operation", "ExportVehicleGroups").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = checkVehicleGroupEligibilityRequest {
                this.context = request.context
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.manager = userRef {
                                this.email = request.context.actor.email
                            }
                        }))
                        this.location = postalAddress {
                            this.region = request.filter.region
                        }
                        this.manager = userRef {
                            this.userId = request.context.actor.userId
                        }
                    }))
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = fleetVehicleGroupValidationService.checkVehicleGroupEligibility(envelope0.unpack(CheckVehicleGroupEligibilityRequest::class.java))
            val outbound1 = normalizeDutyStatusRequest {
                this.context = request.context
                this.details = NormalizeDutyStatusRequestKt.details {
                    this.contact = contactInfo {
                        this.email = reply0.vehicleGroup.manager.email
                    }
                }
                this.dutyStatus = dutyStatus {
                    this.attributes.putAll(reply0.vehicleGroup.attributesMap)
                    this.correlationId = reply0.result.correlationId
                    this.subject = driverLicenseRef {
                        this.displayName = reply0.vehicleGroup.manager.displayName
                    }
                }
            }
            val reply1 = driversDutyStatusValidationService.normalizeDutyStatus(WireCopies.json(outbound1))
            val outbound2 = ExportVehicleGroups2Command(
                context = request.context,
                serviceScheduleLocationLines = reply0.vehicleGroup.location.linesList
            ).toRequest()
            val reply2 = maintenanceServiceScheduleValidationService.validateServiceSchedule(outbound2)
            val outbound3 = summarizeSearchIndexesRequest {
                this.context = request.context
                this.details = SummarizeSearchIndexesRequestKt.details {
                    this.format = request.format
                }
            }
            val command3: Submission<SummarizeSearchIndexesRequest> = Submission.Live(outbound3)
            val payload3 = when (val command = command3) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply3 = searchSearchIndexQueryService.summarizeSearchIndexes(payload3)
            exportVehicleGroupsResponse {
                this.result = operationResult {
                    this.correlationId = reply1.dutyStatus.correlationId
                    this.details.putAll(reply1.result.detailsMap)
                    this.referenceIds.addAll(reply3.result.referenceIdsList)
                    this.status = reply0.outcome.status
                }
                this.vehicleGroup = vehicleGroup {
                    this.audit = auditInfo {
                        this.etag = reply2.serviceSchedule.audit.etag
                    }
                    this.location = postalAddress {
                        this.region = request.filter.region
                    }
                }
            }
        }

    private suspend fun handleFindVehicleGroups(request: FindVehicleGroupsRequest): FindVehicleGroupsResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupQueryService/FindVehicleGroups") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "FindVehicleGroups", request)
            log.atInfo().addKeyValue("operation", "FindVehicleGroups").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = resolveVehiclesRequest {
                this.context = request.context
            }
            val adapter0 = OutboundPort<ResolveVehiclesRequest, ResolveVehiclesResponse> { payload ->
                fleetVehicleQueryService.resolveVehicles(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = compareInspectionOptionsRequest {
                this.context = request.context
                this.details = CompareInspectionOptionsRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.filter.region
                    }
                }
                this.inspection = inspection {
                    this.assignee = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply1 = invokeFindVehicleGroups1(outbound1)
            findVehicleGroupsResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
                this.vehicleGroups.addAll(listOf(vehicleGroup {
                    this.audit = auditInfo {
                        this.etag = reply1.inspection.audit.etag
                    }
                }))
            }
        }

    private suspend fun handlePeekVehicleGroup(request: PeekVehicleGroupRequest): PeekVehicleGroupResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupQueryService/PeekVehicleGroup") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PeekVehicleGroup", request)
            log.atInfo().addKeyValue("operation", "PeekVehicleGroup").log("Handling transport request")
            val outbound0 = RequestFactory.create {
                findSearchIndexesRequest {
                    this.context = request.context
                    this.filter = FindSearchIndexesRequestKt.filter {
                        this.createdAfter = request.context.deadline
                        this.ownerId = request.vehicleGroupId
                    }
                }
            }
            val reply0 = searchSearchIndexQueryService.findSearchIndexes(outbound0)
            peekVehicleGroupResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
                this.vehicleGroup = vehicleGroup {
                    this.vehicleGroupId = request.vehicleGroupId
                }
            }
        }

    private suspend fun handleResolveVehicleGroups(request: ResolveVehicleGroupsRequest): ResolveVehicleGroupsResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupQueryService/ResolveVehicleGroups") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResolveVehicleGroups", request)
            log.atInfo().addKeyValue("operation", "ResolveVehicleGroups").log("Handling transport request")
            val outbound0 = checkDocumentEligibilityRequest {
                this.context = request.context
                this.details = CheckDocumentEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                    this.locale = request.context.locale
                }
            }
            val reply0 = ClientRegistry.lookup("acme.documents.v1.DocumentValidationService", DocumentValidationServiceGrpcKt.DocumentValidationServiceCoroutineStub::class.java).checkDocumentEligibility(outbound0)
            resolveVehicleGroupsResponse {
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
                this.vehicleGroups.addAll(listOf(vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.audit = auditInfo {
                                this.etag = reply0.document.audit.etag
                            }
                        }))
                    }))
                }))
            }
        }

    private suspend fun handleSummarizeVehicleGroups(request: SummarizeVehicleGroupsRequest): SummarizeVehicleGroupsResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupQueryService/SummarizeVehicleGroups") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SummarizeVehicleGroups", request)
            log.atInfo().addKeyValue("operation", "SummarizeVehicleGroups").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.format).log("Recorded audit detail")
            val outbound0 = explainInspectionCalculationRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.inspection = inspection {
                    this.customer = vehicleGroupRef {
                        this.vehicleGroupId = request.vehicleGroupId
                    }
                }
            }
            val reply0 = workerForSummarizeVehicleGroups0.submit(outbound0)
            val outbound1 = listNotificationsRequest {
                this.context = request.context
                this.filter = ListNotificationsRequestKt.filter {
                    this.createdAfter = reply0.inspection.audit.createdAt
                    this.ownerId = reply0.inspection.customer.vehicleGroupId
                }
            }
            val reply1 = clientForSummarizeVehicleGroups1.listNotifications(outbound1)
            summarizeVehicleGroupsResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.message = reply1.result.message
                    this.status = reply0.outcome.status
                }
            }
        }
}
