package com.acme.transport.fleet.grpc

import com.acme.authorization.v1.AccessPolicyQueryServiceGrpcKt
import com.acme.authorization.v1.ExportAccessPoliciesRequestKt
import com.acme.authorization.v1.exportAccessPoliciesRequest
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.customers.v1.customerRef
import com.acme.documents.v1.DocumentValidationServiceGrpcKt
import com.acme.documents.v1.ValidateDocumentRequest
import com.acme.documents.v1.ValidateDocumentRequestKt
import com.acme.documents.v1.ValidateDocumentResponse
import com.acme.documents.v1.validateDocumentRequest
import com.acme.drivers.v1.DriverLicenseWorkflowServiceGrpcKt
import com.acme.drivers.v1.advanceDriverLicenseWorkflowRequest
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverRef
import com.acme.featureflags.v1.FeatureFlagServiceGrpcKt
import com.acme.featureflags.v1.copy as featureflagsCopy
import com.acme.featureflags.v1.createFeatureFlagRequest
import com.acme.featureflags.v1.featureFlag
import com.acme.fleet.v1.ApproveInspectionRequest
import com.acme.fleet.v1.ApproveInspectionResponse
import com.acme.fleet.v1.CommentInspectionApprovalRequest
import com.acme.fleet.v1.CommentInspectionApprovalResponse
import com.acme.fleet.v1.CommentInspectionApprovalResponseKt
import com.acme.fleet.v1.DelegateInspectionApprovalRequest
import com.acme.fleet.v1.DelegateInspectionApprovalResponse
import com.acme.fleet.v1.EscalateInspectionRequest
import com.acme.fleet.v1.EscalateInspectionResponse
import com.acme.fleet.v1.FindVehiclesRequest
import com.acme.fleet.v1.FindVehiclesRequestKt
import com.acme.fleet.v1.GetInspectionApprovalStatusRequest
import com.acme.fleet.v1.GetInspectionApprovalStatusResponse
import com.acme.fleet.v1.InspectionApprovalServiceGrpcKt
import com.acme.fleet.v1.InspectionLifecycleServiceGrpcKt
import com.acme.fleet.v1.InspectionStatusChange
import com.acme.fleet.v1.ListPendingInspectionsRequest
import com.acme.fleet.v1.ListPendingInspectionsResponse
import com.acme.fleet.v1.RecallInspectionRequest
import com.acme.fleet.v1.RecallInspectionResponse
import com.acme.fleet.v1.ReinstateInspectionRequestKt
import com.acme.fleet.v1.RejectInspectionRequest
import com.acme.fleet.v1.RejectInspectionResponse
import com.acme.fleet.v1.RejectInspectionResponseKt
import com.acme.fleet.v1.ResolveVehiclesRequest
import com.acme.fleet.v1.ResolveVehiclesResponse
import com.acme.fleet.v1.SubmitInspectionRequest
import com.acme.fleet.v1.SubmitInspectionResponse
import com.acme.fleet.v1.SubmitInspectionResponseKt
import com.acme.fleet.v1.SummarizeVehiclesRequest
import com.acme.fleet.v1.SummarizeVehiclesRequestKt
import com.acme.fleet.v1.VehicleGroupApprovalServiceGrpcKt
import com.acme.fleet.v1.VehicleQueryServiceGrpcKt
import com.acme.fleet.v1.approveInspectionResponse
import com.acme.fleet.v1.commentInspectionApprovalResponse
import com.acme.fleet.v1.delegateInspectionApprovalResponse
import com.acme.fleet.v1.delegateVehicleGroupApprovalRequest
import com.acme.fleet.v1.escalateInspectionResponse
import com.acme.fleet.v1.findVehiclesRequest
import com.acme.fleet.v1.getInspectionApprovalStatusResponse
import com.acme.fleet.v1.inspection
import com.acme.fleet.v1.inspectionComment
import com.acme.fleet.v1.inspectionSla
import com.acme.fleet.v1.listPendingInspectionsResponse
import com.acme.fleet.v1.recallInspectionResponse
import com.acme.fleet.v1.reinstateInspectionRequest
import com.acme.fleet.v1.rejectInspectionResponse
import com.acme.fleet.v1.resolveVehiclesRequest
import com.acme.fleet.v1.submitInspectionResponse
import com.acme.fleet.v1.summarizeVehiclesRequest
import com.acme.fleet.v1.vehicleGroup
import com.acme.fuel.v1.FuelPolicyReportingServiceGrpcKt
import com.acme.fuel.v1.ShareFuelPolicyReportRequestKt
import com.acme.fuel.v1.ShareFuelPolicyReportResponse
import com.acme.fuel.v1.shareFuelPolicyReportRequest
import com.acme.fuel.v1.streamFuelPolicyMetricsRequest
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.LedgerAccountServiceGrpcKt
import com.acme.ledger.v1.ListLedgerAccountsRequestKt
import com.acme.ledger.v1.listLedgerAccountsRequest
import com.acme.maintenance.v1.StartWorkOrderWorkflowRequestKt
import com.acme.maintenance.v1.WorkOrderWorkflowServiceGrpcKt
import com.acme.maintenance.v1.startWorkOrderWorkflowRequest
import com.acme.maintenance.v1.workOrder
import com.acme.metrics.v1.ListMetricSeriesesRequestKt
import com.acme.metrics.v1.ListMetricSeriesesResponse
import com.acme.metrics.v1.MetricSeriesServiceGrpcKt
import com.acme.metrics.v1.listMetricSeriesesRequest
import com.acme.search.v1.CreateSearchIndexResponse
import com.acme.search.v1.SearchIndexServiceGrpcKt
import com.acme.search.v1.createSearchIndexRequest
import com.acme.search.v1.searchIndex
import com.acme.telematics.v1.TripLogImportServiceGrpcKt
import com.acme.telematics.v1.mapTripLogImportColumnsRequest
import com.acme.telematics.v1.tripLog
import com.acme.transport.fleet.FleetRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.venues.v1.SearchVenuesRequestKt
import com.acme.venues.v1.VenueServiceGrpcKt
import com.acme.venues.v1.VenueValidationServiceGrpcKt
import com.acme.venues.v1.scoreVenueRequest
import com.acme.venues.v1.searchVenuesRequest
import com.acme.venues.v1.venue
import com.acme.workplaces.v1.BuildingQueryServiceGrpcKt
import com.acme.workplaces.v1.FindBuildingsRequestKt
import com.acme.workplaces.v1.FindBuildingsResponse
import com.acme.workplaces.v1.findBuildingsRequest
import com.google.protobuf.Any as ProtoAny
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

/** Routes inspections through approval, with escalation and delegation. */
class InspectionApprovalEndpoint(
    private val store: FleetRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyQueryService: AccessPolicyQueryServiceGrpcKt.AccessPolicyQueryServiceCoroutineStub,
    private val documentsDocumentValidationService: DocumentValidationServiceGrpcKt.DocumentValidationServiceCoroutineStub,
    private val driversDriverLicenseWorkflowService: DriverLicenseWorkflowServiceGrpcKt.DriverLicenseWorkflowServiceCoroutineStub,
    private val featureflagsFeatureFlagService: FeatureFlagServiceGrpcKt.FeatureFlagServiceCoroutineStub,
    private val fleetInspectionLifecycleService: InspectionLifecycleServiceGrpcKt.InspectionLifecycleServiceCoroutineStub,
    private val fleetVehicleGroupApprovalService: VehicleGroupApprovalServiceGrpcKt.VehicleGroupApprovalServiceCoroutineStub,
    private val fleetVehicleQueryService: VehicleQueryServiceGrpcKt.VehicleQueryServiceCoroutineStub,
    private val fuelFuelPolicyReportingService: FuelPolicyReportingServiceGrpcKt.FuelPolicyReportingServiceCoroutineStub,
    private val ledgerLedgerAccountService: LedgerAccountServiceGrpcKt.LedgerAccountServiceCoroutineStub,
    private val maintenanceWorkOrderWorkflowService: WorkOrderWorkflowServiceGrpcKt.WorkOrderWorkflowServiceCoroutineStub,
    private val metricsMetricSeriesService: MetricSeriesServiceGrpcKt.MetricSeriesServiceCoroutineStub,
    private val searchSearchIndexService: SearchIndexServiceGrpcKt.SearchIndexServiceCoroutineStub,
    private val telematicsTripLogImportService: TripLogImportServiceGrpcKt.TripLogImportServiceCoroutineStub,
    private val venuesVenueService: VenueServiceGrpcKt.VenueServiceCoroutineStub,
    private val venuesVenueValidationService: VenueValidationServiceGrpcKt.VenueValidationServiceCoroutineStub,
    private val workplacesBuildingQueryService: BuildingQueryServiceGrpcKt.BuildingQueryServiceCoroutineStub,
) : InspectionApprovalServiceGrpcKt.InspectionApprovalServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(InspectionApprovalEndpoint::class.java)

    private val workerForDelegateInspectionApproval2 = ReplyMailbox<ResolveVehiclesRequest, ResolveVehiclesResponse>(workers) { payload ->
        withTimeout(5_000) { fleetVehicleQueryService.resolveVehicles(payload) }
    }

    private val clientForEscalateInspection0 by lazy { venuesVenueService }

    /** Approves an inspection. */
    override suspend fun approveInspection(request: ApproveInspectionRequest): ApproveInspectionResponse =
        RpcBoundary.run("acme.fleet.v1.InspectionApprovalService/ApproveInspection") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ApproveInspection", request)
            log.atInfo().addKeyValue("operation", "ApproveInspection").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.inspection.inspectionId).log("Recorded audit detail")
            var reply0 = ListMetricSeriesesResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<InspectionStatusChange>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound0 = listMetricSeriesesRequest {
                            this.context = request.context
                            this.filter = ListMetricSeriesesRequestKt.filter {
                                this.createdAfter = item.changedAt
                            }
                        }
                        reply0 = metricsMetricSeriesService.listMetricSerieses(outbound0)
                    }
                }
                try {
                    request.inspection.historyList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound1 = validateDocumentRequest {
                this.context = request.context
                this.details = ValidateDocumentRequestKt.details {
                    this.contact = contactInfo {
                        this.phone = request.inspection.requester.phone
                    }
                }
            }
            val invoke1: suspend (ValidateDocumentRequest, Metadata) -> ValidateDocumentResponse = documentsDocumentValidationService::validateDocument
            val reply1 = invoke1(outbound1, Metadata())
            approveInspectionResponse {
                this.inspection = inspection {
                    this.inspectionId = request.inspectionId
                    this.sla = inspectionSla {
                        this.dueAt = reply1.document.dueAt
                    }
                    this.subject = request.inspection.subject
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.status = "ok"
                }
            }
        }

    /** Comments an inspection. */
    override suspend fun commentInspectionApproval(request: CommentInspectionApprovalRequest): CommentInspectionApprovalResponse {
        return handleCommentInspectionApproval(request)
    }

    /** Delegates an inspection. */
    override suspend fun delegateInspectionApproval(request: DelegateInspectionApprovalRequest): DelegateInspectionApprovalResponse = handleDelegateInspectionApproval(request)

    /** Escalates an inspection. */
    override suspend fun escalateInspection(request: EscalateInspectionRequest): EscalateInspectionResponse =
        RpcBoundary.run("acme.fleet.v1.InspectionApprovalService/EscalateInspection") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EscalateInspection", request)
            log.atInfo().addKeyValue("operation", "EscalateInspection").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.inspection.inspectionId).log("Recorded audit detail")
            val outbound0 = searchVenuesRequest {
                this.context = request.context
                this.filter = SearchVenuesRequestKt.filter {
                    this.createdAfter = request.inspection.audit.createdAt
                }
            }
            val reply0 = clientForEscalateInspection0.searchVenues(outbound0)
            val outbound1 = delegateVehicleGroupApprovalRequest {
                this.context = request.context
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.audit = auditInfo {
                                this.etag = request.inspection.audit.etag
                            }
                            this.manager = userRef {
                                this.email = request.inspection.requester.email
                            }
                        }))
                        this.manager = userRef {
                            this.displayName = request.inspection.assignee.displayName
                            this.email = request.details.approver.email
                        }
                    }))
                }
            }
            val reply1 = flowOf(outbound1).map { value -> fleetVehicleGroupApprovalService.delegateVehicleGroupApproval(value) }.single()
            var reply2 = ShareFuelPolicyReportResponse.getDefaultInstance()
            request.inspection.commentsList.asFlow().map { item ->
                val outbound2 = shareFuelPolicyReportRequest {
                    this.context = request.context
                    this.details = ShareFuelPolicyReportRequestKt.details {
                        this.channel = request.inspection.channel.channel
                        this.recipient = contactInfo {
                            this.email = item.authorEmail
                            this.name = request.inspection.requester.name
                            this.phone = request.inspection.requester.phone
                        }
                    }
                }
                fuelFuelPolicyReportingService.shareFuelPolicyReport(outbound2)
            }.collect { reply2 = it }
            escalateInspectionResponse {
                this.inspection = inspection {
                    this.inspectionId = request.inspectionId
                    this.status = request.inspection.status
                }
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }

    /** Returns an inspection. */
    override suspend fun getInspectionApprovalStatus(request: GetInspectionApprovalStatusRequest): GetInspectionApprovalStatusResponse {
        return handleGetInspectionApprovalStatus(request)
    }

    /** Lists inspections a page at a time. */
    override suspend fun listPendingInspections(request: ListPendingInspectionsRequest): ListPendingInspectionsResponse = handleListPendingInspections(request)

    /** Recalls an inspection. */
    override suspend fun recallInspection(request: RecallInspectionRequest): RecallInspectionResponse =
        RpcBoundary.run("acme.fleet.v1.InspectionApprovalService/RecallInspection") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecallInspection", request)
            log.atInfo().addKeyValue("operation", "RecallInspection").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.inspection.inspectionId).log("Recorded audit detail")
            val outbound0 = exportAccessPoliciesRequest {
                this.context = request.context
                this.filter = ExportAccessPoliciesRequestKt.filter {
                    this.createdAfter = request.details.effectiveAt
                    this.ownerId = request.details.approver.userId
                }
            }
            authorizationAccessPolicyQueryService.exportAccessPolicies(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = advanceDriverLicenseWorkflowRequest {
                this.context = request.context
                this.driverLicense = driverLicense {
                    this.audit = auditInfo {
                        this.updatedAt = request.inspection.audit.updatedAt
                    }
                    this.counterparty = driverRef {
                        this.displayName = request.inspection.assignee.displayName
                    }
                }
            }
            val reply1 = driversDriverLicenseWorkflowService.advanceDriverLicenseWorkflow(outbound1)
            val outbound2 = createFeatureFlagRequest {}.featureflagsCopy {
                this.context = request.context
                this.featureFlag = featureFlag {
                    this.audit = auditInfo {
                        this.createdBy = reply1.driverLicense.audit.createdBy
                        this.updatedBy = request.inspection.audit.updatedBy
                    }
                    this.owner = userRef {
                        this.displayName = request.inspection.customer.displayName
                        this.email = request.inspection.requester.email
                    }
                }
            }
            val reply2 = featureflagsFeatureFlagService.createFeatureFlag(outbound2)
            recallInspectionResponse {
                this.inspection = inspection {
                    this.audit = auditInfo {
                        this.updatedBy = reply1.driverLicense.audit.updatedBy
                    }
                    this.comments.addAll(listOf(inspectionComment {
                        this.createdAt = reply2.featureFlag.audit.createdAt
                    }))
                    this.inspectionId = request.inspectionId
                    this.priority = request.inspection.priority
                    this.requester = contactInfo {
                        this.email = reply2.featureFlag.owner.email
                    }
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Rejects an inspection. */
    override suspend fun rejectInspection(request: RejectInspectionRequest): RejectInspectionResponse {
        return handleRejectInspection(request)
    }

    /** Submits an inspection. */
    override suspend fun submitInspection(request: SubmitInspectionRequest): SubmitInspectionResponse = handleSubmitInspection(request)

    private suspend fun handleCommentInspectionApproval(request: CommentInspectionApprovalRequest): CommentInspectionApprovalResponse =
        RpcBoundary.run("acme.fleet.v1.InspectionApprovalService/CommentInspectionApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CommentInspectionApproval", request)
            log.atInfo().addKeyValue("operation", "CommentInspectionApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.inspection.inspectionId).log("Recorded audit detail")
            val outbound0 = FindVehiclesRequest.newBuilder().apply {
                setContext(request.context)
                setFilter(FindVehiclesRequestKt.filter {
                    this.createdAfter = request.inspection.audit.updatedAt
                    this.ownerId = request.inspection.customer.vehicleGroupId
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> fleetVehicleQueryService.findVehicles(value) }
            var reply1 = CreateSearchIndexResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.inspection.historyList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound1 = createSearchIndexRequest {
                                this.context = request.context
                                this.searchIndex = searchIndex {
                                    this.audit = auditInfo {
                                        this.createdAt = item.changedAt
                                    }
                                }
                            }
                            searchSearchIndexService.createSearchIndex(outbound1)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply1 = it }
                }
            }
            commentInspectionApprovalResponse {
                this.inspection = inspection {
                    this.assignee = request.inspection.assignee
                    this.inspectionId = request.inspectionId
                }
                this.outcome = CommentInspectionApprovalResponseKt.outcome {
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleDelegateInspectionApproval(request: DelegateInspectionApprovalRequest): DelegateInspectionApprovalResponse =
        RpcBoundary.run("acme.fleet.v1.InspectionApprovalService/DelegateInspectionApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DelegateInspectionApproval", request)
            log.atInfo().addKeyValue("operation", "DelegateInspectionApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.inspection.inspectionId).log("Recorded audit detail")
            val outbound0 = listLedgerAccountsRequest {
                this.context = request.context
                this.filter = ListLedgerAccountsRequestKt.filter {
                    this.createdAfter = request.inspection.audit.createdAt
                }
            }
            val reply0 = ClientRegistry.lookup("acme.ledger.v1.LedgerAccountService", LedgerAccountServiceGrpcKt.LedgerAccountServiceCoroutineStub::class.java).listLedgerAccounts(outbound0)
            val outbound1 = scoreVenueRequest {
                this.context = request.context
                this.venue = venue {
                    this.audit = auditInfo {
                        this.createdBy = request.inspection.audit.createdBy
                        this.updatedBy = request.inspection.audit.updatedBy
                    }
                    this.owner = userRef {
                        this.displayName = request.details.approver.displayName
                        this.userId = request.inspection.assignee.userId
                    }
                }
            }
            val reply1 = venuesVenueValidationService.scoreVenue(WireCopies.reflective(outbound1))
            val outbound2 = resolveVehiclesRequest {
                this.context = request.context
            }
            val reply2 = workerForDelegateInspectionApproval2.submit(outbound2)
            delegateInspectionApprovalResponse {
                this.inspection = inspection {
                    this.assignee = request.inspection.assignee
                    this.audit = auditInfo {
                        this.createdAt = reply1.venue.audit.createdAt
                    }
                    this.inspectionId = request.inspectionId
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleGetInspectionApprovalStatus(request: GetInspectionApprovalStatusRequest): GetInspectionApprovalStatusResponse =
        RpcBoundary.run("acme.fleet.v1.InspectionApprovalService/GetInspectionApprovalStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetInspectionApprovalStatus", request)
            log.atInfo().addKeyValue("operation", "GetInspectionApprovalStatus").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            getInspectionApprovalStatusResponse {
                this.inspection = inspection {
                    this.inspectionId = request.inspectionId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleListPendingInspections(request: ListPendingInspectionsRequest): ListPendingInspectionsResponse =
        RpcBoundary.run("acme.fleet.v1.InspectionApprovalService/ListPendingInspections") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListPendingInspections", request)
            log.atInfo().addKeyValue("operation", "ListPendingInspections").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val configure0: StartWorkOrderWorkflowRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.workOrder = workOrder {
                    this.assignee = userRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val outbound0 = startWorkOrderWorkflowRequest(configure0)
            val reply0 = maintenanceWorkOrderWorkflowService.startWorkOrderWorkflow(outbound0)
            listPendingInspectionsResponse {
                this.inspections.addAll(listOf(inspection {
                    this.assignee = userRef {
                        this.displayName = reply0.workOrder.assignee.displayName
                    }
                    this.requester = contactInfo {
                        this.name = reply0.workOrder.requester.name
                    }
                }))
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleRejectInspection(request: RejectInspectionRequest): RejectInspectionResponse =
        RpcBoundary.run("acme.fleet.v1.InspectionApprovalService/RejectInspection") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RejectInspection", request)
            log.atInfo().addKeyValue("operation", "RejectInspection").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.inspection.inspectionId).log("Recorded audit detail")
            var reply0 = FindBuildingsResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<InspectionStatusChange>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound0 = findBuildingsRequest {
                            this.context = request.context
                            this.filter = FindBuildingsRequestKt.filter {
                                this.createdAfter = item.changedAt
                                this.labels.putAll(request.inspection.customFieldsMap)
                                this.ownerId = request.details.approver.userId
                            }
                        }
                        reply0 = workplacesBuildingQueryService.findBuildings(outbound0)
                    }
                }
                try {
                    request.inspection.historyList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound1 = mapTripLogImportColumnsRequest {
                this.context = request.context
                this.tripLog = tripLog {
                    this.counterparty = customerRef {
                        this.displayName = request.inspection.assignee.displayName
                    }
                }
            }
            val reply1 = telematicsTripLogImportService.mapTripLogImportColumns(outbound1)
            val outbound2 = summarizeVehiclesRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.details = SummarizeVehiclesRequestKt.details {
                    this.filters.putAll(reply0.result.detailsMap)
                }
                this.reason = request.reason
            }
            val envelope2 = ProtoAny.pack(outbound2)
            val reply2 = fleetVehicleQueryService.summarizeVehicles(envelope2.unpack(SummarizeVehiclesRequest::class.java))
            val outbound3 = reinstateInspectionRequest {
                this.context = request.context
                this.details = ReinstateInspectionRequestKt.details {
                    this.newOwner = userRef {
                        this.displayName = request.inspection.assignee.displayName
                        this.userId = request.details.approver.userId
                    }
                    this.notifyContact = contactInfo {
                        this.preferredChannel = request.inspection.requester.preferredChannel
                    }
                }
                this.inspectionId = request.inspectionId
            }
            val reply3 = fleetInspectionLifecycleService.reinstateInspection(WireCopies.json(outbound3))
            rejectInspectionResponse {
                this.inspection = inspection {
                    this.audit = auditInfo {
                        this.updatedBy = reply1.tripLog.audit.updatedBy
                    }
                    this.inspectionId = request.inspectionId
                    this.requester = contactInfo {
                        this.email = reply3.inspection.requester.email
                    }
                }
                this.outcome = RejectInspectionResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.message = reply1.outcome.message
                    this.status = reply2.result.status
                }
                this.result = operationResult {
                    this.status = reply2.result.status
                }
            }
        }

    private suspend fun handleSubmitInspection(request: SubmitInspectionRequest): SubmitInspectionResponse =
        RpcBoundary.run("acme.fleet.v1.InspectionApprovalService/SubmitInspection") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SubmitInspection", request)
            log.atInfo().addKeyValue("operation", "SubmitInspection").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.inspection.inspectionId).log("Recorded audit detail")
            val outbound0 = streamFuelPolicyMetricsRequest {
                this.context = request.context
                this.subscriber = contactInfo {
                    this.name = request.inspection.requester.name
                    this.preferredChannel = request.inspection.requester.preferredChannel
                }
            }
            fuelFuelPolicyReportingService.streamFuelPolicyMetrics(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            submitInspectionResponse {
                this.inspection = inspection {
                    this.inspectionId = request.inspectionId
                }
                this.outcome = SubmitInspectionResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }
}
