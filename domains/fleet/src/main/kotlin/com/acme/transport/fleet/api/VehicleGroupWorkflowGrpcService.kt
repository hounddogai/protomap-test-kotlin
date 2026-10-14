package com.acme.transport.fleet.api

import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.pageRequest
import com.acme.common.v1.postalAddress
import com.acme.featureflags.v1.CheckFeatureFlagEligibilityRequestKt
import com.acme.featureflags.v1.FeatureFlagValidationServiceGrpcKt
import com.acme.featureflags.v1.checkFeatureFlagEligibilityRequest
import com.acme.featureflags.v1.featureFlag
import com.acme.fleet.v1.AdvanceVehicleGroupWorkflowRequest
import com.acme.fleet.v1.AdvanceVehicleGroupWorkflowResponse
import com.acme.fleet.v1.ApproveInspectionRequest
import com.acme.fleet.v1.ApproveInspectionRequestKt
import com.acme.fleet.v1.InspectVehicleGroupWorkflowRequest
import com.acme.fleet.v1.InspectVehicleGroupWorkflowResponse
import com.acme.fleet.v1.InspectionApprovalServiceGrpcKt
import com.acme.fleet.v1.InspectionCalculationServiceGrpcKt
import com.acme.fleet.v1.InspectionValidationServiceGrpcKt
import com.acme.fleet.v1.ListPendingVehiclesRequestKt
import com.acme.fleet.v1.ListVehicleGroupWorkflowsRequest
import com.acme.fleet.v1.ListVehicleGroupWorkflowsResponse
import com.acme.fleet.v1.PauseVehicleGroupWorkflowRequest
import com.acme.fleet.v1.PauseVehicleGroupWorkflowResponse
import com.acme.fleet.v1.PauseVehicleWorkflowRequestKt
import com.acme.fleet.v1.PreviewInspectionRequestKt
import com.acme.fleet.v1.ResumeVehicleGroupWorkflowRequest
import com.acme.fleet.v1.ResumeVehicleGroupWorkflowResponse
import com.acme.fleet.v1.RetryVehicleGroupWorkflowRequest
import com.acme.fleet.v1.RetryVehicleGroupWorkflowResponse
import com.acme.fleet.v1.RetryVehicleGroupWorkflowResponseKt
import com.acme.fleet.v1.SignalVehicleGroupWorkflowRequest
import com.acme.fleet.v1.SignalVehicleGroupWorkflowResponse
import com.acme.fleet.v1.StartVehicleGroupWorkflowRequest
import com.acme.fleet.v1.StartVehicleGroupWorkflowResponse
import com.acme.fleet.v1.StartVehicleWorkflowRequest
import com.acme.fleet.v1.VehicleApprovalServiceGrpcKt
import com.acme.fleet.v1.VehicleGroupQueryServiceGrpcKt
import com.acme.fleet.v1.VehicleGroupWorkflowServiceGrpcKt
import com.acme.fleet.v1.VehicleQueryServiceGrpcKt
import com.acme.fleet.v1.VehicleWorkflowServiceGrpcKt
import com.acme.fleet.v1.advanceVehicleGroupWorkflowResponse
import com.acme.fleet.v1.approveInspectionRequest
import com.acme.fleet.v1.compareInspectionOptionsRequest
import com.acme.fleet.v1.copy as fleetCopy
import com.acme.fleet.v1.inspectVehicleGroupWorkflowResponse
import com.acme.fleet.v1.inspection
import com.acme.fleet.v1.listPendingVehiclesRequest
import com.acme.fleet.v1.listVehicleGroupWorkflowsResponse
import com.acme.fleet.v1.pauseVehicleGroupWorkflowResponse
import com.acme.fleet.v1.pauseVehicleWorkflowRequest
import com.acme.fleet.v1.peekVehicleGroupRequest
import com.acme.fleet.v1.previewInspectionRequest
import com.acme.fleet.v1.resolveVehiclesRequest
import com.acme.fleet.v1.resumeVehicleGroupWorkflowResponse
import com.acme.fleet.v1.retryVehicleGroupWorkflowResponse
import com.acme.fleet.v1.signalVehicleGroupWorkflowResponse
import com.acme.fleet.v1.startVehicleGroupWorkflowResponse
import com.acme.fleet.v1.startVehicleWorkflowRequest
import com.acme.fleet.v1.vehicle
import com.acme.fleet.v1.vehicleGroup
import com.acme.fleet.v1.vehicleGroupRef
import com.acme.fuel.v1.CountFuelPolicyTotalsRequestKt
import com.acme.fuel.v1.FuelPolicyImportServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyReportingServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionSyncServiceGrpcKt
import com.acme.fuel.v1.ListFuelPolicyImportsRequestKt
import com.acme.fuel.v1.ListFuelTransactionConflictsRequest
import com.acme.fuel.v1.ListFuelTransactionConflictsRequestKt
import com.acme.fuel.v1.countFuelPolicyTotalsRequest
import com.acme.fuel.v1.listFuelPolicyImportsRequest
import com.acme.fuel.v1.listFuelTransactionConflictsRequest
import com.acme.identity.v1.userRef
import com.acme.metrics.v1.CheckMetricSeriesEligibilityRequestKt
import com.acme.metrics.v1.MetricSeriesValidationServiceGrpcKt
import com.acme.metrics.v1.checkMetricSeriesEligibilityRequest
import com.acme.metrics.v1.metricSeries
import com.acme.telematics.v1.ListTripLogExportsRequest
import com.acme.telematics.v1.ListTripLogExportsResponse
import com.acme.telematics.v1.StreamTripLogExportRequestKt
import com.acme.telematics.v1.TripLogExportServiceGrpcKt
import com.acme.telematics.v1.listTripLogExportsRequest
import com.acme.telematics.v1.streamTripLogExportRequest
import com.acme.transport.fleet.FleetRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.google.protobuf.Any as ProtoAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Runs the long-lived workflow that takes a vehicle group from start to finish. */
class VehicleGroupWorkflowGrpcService(
    private val store: FleetRepository,
    private val workers: CoroutineScope,
    private val featureflagsFeatureFlagValidationService: FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub,
    private val fleetInspectionApprovalService: InspectionApprovalServiceGrpcKt.InspectionApprovalServiceCoroutineStub,
    private val fleetInspectionCalculationService: InspectionCalculationServiceGrpcKt.InspectionCalculationServiceCoroutineStub,
    private val fleetInspectionValidationService: InspectionValidationServiceGrpcKt.InspectionValidationServiceCoroutineStub,
    private val fleetVehicleApprovalService: VehicleApprovalServiceGrpcKt.VehicleApprovalServiceCoroutineStub,
    private val fleetVehicleGroupQueryService: VehicleGroupQueryServiceGrpcKt.VehicleGroupQueryServiceCoroutineStub,
    private val fleetVehicleQueryService: VehicleQueryServiceGrpcKt.VehicleQueryServiceCoroutineStub,
    private val fleetVehicleWorkflowService: VehicleWorkflowServiceGrpcKt.VehicleWorkflowServiceCoroutineStub,
    private val fuelFuelPolicyImportService: FuelPolicyImportServiceGrpcKt.FuelPolicyImportServiceCoroutineStub,
    private val fuelFuelPolicyReportingService: FuelPolicyReportingServiceGrpcKt.FuelPolicyReportingServiceCoroutineStub,
    private val fuelFuelTransactionSyncService: FuelTransactionSyncServiceGrpcKt.FuelTransactionSyncServiceCoroutineStub,
    private val metricsMetricSeriesValidationService: MetricSeriesValidationServiceGrpcKt.MetricSeriesValidationServiceCoroutineStub,
    private val telematicsTripLogExportService: TripLogExportServiceGrpcKt.TripLogExportServiceCoroutineStub,
) : VehicleGroupWorkflowServiceGrpcKt.VehicleGroupWorkflowServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(VehicleGroupWorkflowGrpcService::class.java)

    private val workerForListVehicleGroupWorkflows0 = ReplyMailbox<ListTripLogExportsRequest, ListTripLogExportsResponse>(workers) { payload ->
        withTimeout(5_000) { telematicsTripLogExportService.listTripLogExports(payload) }
    }

    private val clientForPauseVehicleGroupWorkflow0 by lazy { metricsMetricSeriesValidationService }

    /** Advances a vehicle group. */
    override suspend fun advanceVehicleGroupWorkflow(request: AdvanceVehicleGroupWorkflowRequest): AdvanceVehicleGroupWorkflowResponse {
        return handleAdvanceVehicleGroupWorkflow(request)
    }

    /** Returns a vehicle group. */
    override suspend fun inspectVehicleGroupWorkflow(request: InspectVehicleGroupWorkflowRequest): InspectVehicleGroupWorkflowResponse = handleInspectVehicleGroupWorkflow(request)

    /** Lists vehicle groups a page at a time. */
    override suspend fun listVehicleGroupWorkflows(request: ListVehicleGroupWorkflowsRequest): ListVehicleGroupWorkflowsResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupWorkflowService/ListVehicleGroupWorkflows") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListVehicleGroupWorkflows", request)
            log.atInfo().addKeyValue("operation", "ListVehicleGroupWorkflows").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = listTripLogExportsRequest {
                this.context = request.context
                this.page = pageRequest {
                    this.pageSize = request.page.pageSize
                }
            }
            val reply0 = workerForListVehicleGroupWorkflows0.submit(outbound0)
            listVehicleGroupWorkflowsResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    /** Pauses a vehicle group. */
    override suspend fun pauseVehicleGroupWorkflow(request: PauseVehicleGroupWorkflowRequest): PauseVehicleGroupWorkflowResponse {
        return handlePauseVehicleGroupWorkflow(request)
    }

    /** Resumes a vehicle group. */
    override suspend fun resumeVehicleGroupWorkflow(request: ResumeVehicleGroupWorkflowRequest): ResumeVehicleGroupWorkflowResponse = handleResumeVehicleGroupWorkflow(request)

    /** Retrys a vehicle group. */
    override suspend fun retryVehicleGroupWorkflow(request: RetryVehicleGroupWorkflowRequest): RetryVehicleGroupWorkflowResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupWorkflowService/RetryVehicleGroupWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RetryVehicleGroupWorkflow", request)
            log.atInfo().addKeyValue("operation", "RetryVehicleGroupWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicleGroup.vehicleGroupId).log("Recorded audit detail")
            val configure0: ListFuelPolicyImportsRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.filter = ListFuelPolicyImportsRequestKt.filter {
                    this.createdAfter = request.vehicleGroup.audit.createdAt
                    this.region = request.vehicleGroup.location.region
                }
            }
            val outbound0 = listFuelPolicyImportsRequest(configure0)
            val reply0 = fuelFuelPolicyImportService.listFuelPolicyImports(outbound0)
            val outbound1 = checkFeatureFlagEligibilityRequest {
                this.context = request.context
                this.details = CheckFeatureFlagEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.preferredChannel = request.details.notify.preferredChannel
                    }
                }
                this.featureFlag = featureFlag {
                    this.audit = auditInfo {
                        this.etag = request.vehicleGroup.audit.etag
                        this.updatedBy = request.vehicleGroup.audit.updatedBy
                    }
                }
            }
            val reply1 = featureflagsFeatureFlagValidationService.checkFeatureFlagEligibility(outbound1)
            retryVehicleGroupWorkflowResponse {
                this.outcome = RetryVehicleGroupWorkflowResponseKt.outcome {
                    this.message = reply0.result.message
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.vehicleGroup = vehicleGroup {
                    this.audit = auditInfo {
                        this.updatedAt = reply1.featureFlag.audit.updatedAt
                    }
                    this.manager = request.vehicleGroup.manager
                    this.vehicleGroupId = request.vehicleGroupId
                }
            }
        }

    /** Signals a vehicle group. */
    override suspend fun signalVehicleGroupWorkflow(request: SignalVehicleGroupWorkflowRequest): SignalVehicleGroupWorkflowResponse {
        return handleSignalVehicleGroupWorkflow(request)
    }

    /** Starts a vehicle group. */
    override suspend fun startVehicleGroupWorkflow(request: StartVehicleGroupWorkflowRequest): StartVehicleGroupWorkflowResponse = handleStartVehicleGroupWorkflow(request)

    private suspend fun handleAdvanceVehicleGroupWorkflow(request: AdvanceVehicleGroupWorkflowRequest): AdvanceVehicleGroupWorkflowResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupWorkflowService/AdvanceVehicleGroupWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "AdvanceVehicleGroupWorkflow", request)
            log.atInfo().addKeyValue("operation", "AdvanceVehicleGroupWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicleGroup.vehicleGroupId).log("Recorded audit detail")
            val outbound0 = ApproveInspectionRequest.newBuilder().apply {
                setContext(request.context)
                setDetails(ApproveInspectionRequestKt.details {
                    this.approver = userRef {
                        this.email = request.details.notify.email
                    }
                })
                setInspection(inspection {
                    this.assignee = userRef {
                        this.email = request.vehicleGroup.manager.email
                    }
                    this.audit = auditInfo {
                        this.createdBy = request.vehicleGroup.audit.createdBy
                        this.updatedBy = request.vehicleGroup.audit.updatedBy
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> fleetInspectionApprovalService.approveInspection(value) }
            val outbound1 = RequestFactory.create {
                countFuelPolicyTotalsRequest {
                    this.context = request.context
                    this.filter = CountFuelPolicyTotalsRequestKt.filter {
                        this.region = request.vehicleGroup.location.region
                    }
                }
            }
            val reply1 = fuelFuelPolicyReportingService.countFuelPolicyTotals(outbound1)
            advanceVehicleGroupWorkflowResponse {
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.message = reply1.result.message
                    this.status = "ok"
                }
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.audit = auditInfo {
                            this.createdBy = reply0.inspection.audit.createdBy
                        }
                    }))
                    this.parentId = request.vehicleGroup.parentId
                    this.vehicleGroupId = request.vehicleGroupId
                }
            }
        }

    private suspend fun handleInspectVehicleGroupWorkflow(request: InspectVehicleGroupWorkflowRequest): InspectVehicleGroupWorkflowResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupWorkflowService/InspectVehicleGroupWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "InspectVehicleGroupWorkflow", request)
            log.atInfo().addKeyValue("operation", "InspectVehicleGroupWorkflow").log("Handling transport request")
            val outbound0 = previewInspectionRequest {
                this.context = request.context
                this.details = PreviewInspectionRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                    this.locale = request.context.locale
                }
                this.inspection = inspection {
                    this.customer = vehicleGroupRef {
                        this.vehicleGroupId = request.vehicleGroupId
                    }
                }
            }
            val reply0 = ClientRegistry.lookup("acme.fleet.v1.InspectionValidationService", InspectionValidationServiceGrpcKt.InspectionValidationServiceCoroutineStub::class.java).previewInspection(outbound0)
            val outbound1 = peekVehicleGroupRequest {
                this.context = request.context
                this.vehicleGroupId = request.vehicleGroupId
            }
            val reply1 = fleetVehicleGroupQueryService.peekVehicleGroup(WireCopies.reflective(outbound1))
            inspectVehicleGroupWorkflowResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.audit = auditInfo {
                            this.createdBy = reply0.inspection.audit.createdBy
                        }
                        this.children.addAll(listOf(vehicleGroup {
                            this.children.addAll(listOf(vehicleGroup {
                                this.depth = reply1.vehicleGroup.depth
                            }))
                        }))
                    }))
                    this.manager = userRef {
                        this.email = reply0.inspection.requester.email
                    }
                    this.vehicleGroupId = request.vehicleGroupId
                }
            }
        }

    private suspend fun handlePauseVehicleGroupWorkflow(request: PauseVehicleGroupWorkflowRequest): PauseVehicleGroupWorkflowResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupWorkflowService/PauseVehicleGroupWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PauseVehicleGroupWorkflow", request)
            log.atInfo().addKeyValue("operation", "PauseVehicleGroupWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicleGroup.vehicleGroupId).log("Recorded audit detail")
            val outbound0 = checkMetricSeriesEligibilityRequest {
                this.context = request.context
                this.details = CheckMetricSeriesEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.preferredChannel = request.details.notify.preferredChannel
                    }
                }
                this.metricSeries = metricSeries {
                    this.address = postalAddress {
                        this.countryCode = request.vehicleGroup.location.countryCode
                    }
                    this.audit = auditInfo {
                        this.updatedBy = request.vehicleGroup.audit.updatedBy
                    }
                    this.owner = userRef {
                        this.userId = request.vehicleGroup.manager.userId
                    }
                }
            }
            val reply0 = clientForPauseVehicleGroupWorkflow0.checkMetricSeriesEligibility(outbound0)
            pauseVehicleGroupWorkflowResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.vehicleGroup = vehicleGroup {
                    this.audit = auditInfo {
                        this.createdBy = reply0.metricSeries.audit.createdBy
                    }
                    this.children.addAll(listOf(vehicleGroup {
                        this.location = postalAddress {
                            this.countryCode = reply0.metricSeries.address.countryCode
                        }
                    }))
                    this.name = request.vehicleGroup.name
                    this.vehicleGroupId = request.vehicleGroupId
                }
            }
        }

    private suspend fun handleResumeVehicleGroupWorkflow(request: ResumeVehicleGroupWorkflowRequest): ResumeVehicleGroupWorkflowResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupWorkflowService/ResumeVehicleGroupWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResumeVehicleGroupWorkflow", request)
            log.atInfo().addKeyValue("operation", "ResumeVehicleGroupWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicleGroup.vehicleGroupId).log("Recorded audit detail")
            val outbound0 = pauseVehicleWorkflowRequest {
                this.context = request.context
                this.details = PauseVehicleWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.phone = request.details.notify.phone
                    }
                }
                this.vehicle = vehicle {
                    this.address = postalAddress {
                        this.recipient = request.vehicleGroup.location.recipient
                    }
                    this.owner = userRef {
                        this.email = request.details.notify.email
                    }
                }
            }
            val reply0 = flowOf(outbound0).map { value -> fleetVehicleWorkflowService.pauseVehicleWorkflow(value) }.single()
            val outbound1 = streamTripLogExportRequest {
                this.context = request.context
                this.filter = StreamTripLogExportRequestKt.filter {
                    this.createdAfter = reply0.outcome.effectiveAt
                    this.labels.putAll(reply0.vehicle.specsMap)
                    this.ownerId = reply0.vehicle.owner.userId
                    this.region = request.vehicleGroup.location.region
                }
            }
            telematicsTripLogExportService.streamTripLogExport(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            resumeVehicleGroupWorkflowResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.audit = auditInfo {
                                this.etag = reply0.vehicle.audit.etag
                            }
                        }))
                        this.location = postalAddress {
                            this.lines.addAll(reply0.vehicle.address.linesList)
                        }
                    }))
                    this.parentId = request.vehicleGroup.parentId
                    this.vehicleGroupId = request.vehicleGroupId
                }
            }
        }

    private suspend fun handleSignalVehicleGroupWorkflow(request: SignalVehicleGroupWorkflowRequest): SignalVehicleGroupWorkflowResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupWorkflowService/SignalVehicleGroupWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SignalVehicleGroupWorkflow", request)
            log.atInfo().addKeyValue("operation", "SignalVehicleGroupWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicleGroup.vehicleGroupId).log("Recorded audit detail")
            val outbound0 = resolveVehiclesRequest {
                this.context = request.context
            }
            val reply0 = fleetVehicleQueryService.resolveVehicles(outbound0)
            val outbound1 = listPendingVehiclesRequest {}.fleetCopy {
                this.context = request.context
                this.filter = ListPendingVehiclesRequestKt.filter {
                    this.createdAfter = request.vehicleGroup.audit.createdAt
                    this.region = request.vehicleGroup.location.region
                }
            }
            val reply1 = fleetVehicleApprovalService.listPendingVehicles(outbound1)
            signalVehicleGroupWorkflowResponse {
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = reply0.result.status
                }
                this.vehicleGroup = vehicleGroup {
                    this.attributes.putAll(request.vehicleGroup.attributesMap)
                    this.vehicleGroupId = request.vehicleGroupId
                }
            }
        }

    private fun StartVehicleGroupWorkflowRequest.toListFuelTransactionConflictsFromStartVehicleGroupWorkflow0(): ListFuelTransactionConflictsRequest {
        val source = this
        return listFuelTransactionConflictsRequest {
            this.context = source.context
            this.filter = ListFuelTransactionConflictsRequestKt.filter {
                this.createdAfter = source.vehicleGroup.audit.createdAt
                this.region = source.vehicleGroup.location.region
            }
        }
    }

    private suspend fun handleStartVehicleGroupWorkflow(request: StartVehicleGroupWorkflowRequest): StartVehicleGroupWorkflowResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupWorkflowService/StartVehicleGroupWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StartVehicleGroupWorkflow", request)
            log.atInfo().addKeyValue("operation", "StartVehicleGroupWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicleGroup.vehicleGroupId).log("Recorded audit detail")
            val outbound0 = request.toListFuelTransactionConflictsFromStartVehicleGroupWorkflow0()
            val reply0 = fuelFuelTransactionSyncService.listFuelTransactionConflicts(outbound0)
            val outbound1 = compareInspectionOptionsRequest {
                this.context = request.context
                this.inspection = inspection {
                    this.requester = contactInfo {
                        this.preferredChannel = request.details.notify.preferredChannel
                    }
                }
            }
            val reply1 = fleetInspectionCalculationService.compareInspectionOptions(outbound1)
            val outbound2 = startVehicleWorkflowRequest {
                this.context = request.context
                this.vehicle = vehicle {
                    this.owner = userRef {
                        this.displayName = request.vehicleGroup.manager.displayName
                    }
                }
            }
            val envelope2 = ProtoAny.pack(outbound2)
            val reply2 = fleetVehicleWorkflowService.startVehicleWorkflow(envelope2.unpack(StartVehicleWorkflowRequest::class.java))
            val outbound3 = previewInspectionRequest {
                this.context = request.context
                this.inspection = inspection {
                    this.assignee = userRef {
                        this.email = reply1.inspection.requester.email
                    }
                    this.audit = auditInfo {
                        this.etag = reply2.vehicle.audit.etag
                        this.updatedAt = reply2.vehicle.audit.updatedAt
                    }
                }
            }
            val reply3 = fleetInspectionValidationService.previewInspection(WireCopies.json(outbound3))
            startVehicleGroupWorkflowResponse {
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = reply3.result.status
                }
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.audit = auditInfo {
                                this.createdAt = reply1.inspection.audit.createdAt
                            }
                        }))
                        this.manager = userRef {
                            this.displayName = reply1.inspection.assignee.displayName
                        }
                    }))
                    this.parentId = request.vehicleGroup.parentId
                    this.vehicleGroupId = request.vehicleGroupId
                }
            }
        }
}
