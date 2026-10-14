package com.acme.transport.fleet.adapters

import com.acme.audit.v1.AuditEventQueryServiceGrpcKt
import com.acme.audit.v1.FindAuditEventsRequestKt
import com.acme.audit.v1.findAuditEventsRequest
import com.acme.checkout.v1.CheckoutSessionQueryServiceGrpcKt
import com.acme.checkout.v1.CountCheckoutSessionsRequest
import com.acme.checkout.v1.CountCheckoutSessionsRequestKt
import com.acme.checkout.v1.countCheckoutSessionsRequest
import com.acme.common.v1.PersonName
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.featureflags.v1.BatchGetFeatureFlagsRequest
import com.acme.featureflags.v1.BatchGetFeatureFlagsResponse
import com.acme.featureflags.v1.FeatureFlagServiceGrpcKt
import com.acme.featureflags.v1.batchGetFeatureFlagsRequest
import com.acme.featurestore.v1.FeatureViewQueryServiceGrpcKt
import com.acme.featurestore.v1.SuggestFeatureViewsRequest
import com.acme.featurestore.v1.SuggestFeatureViewsRequestKt
import com.acme.featurestore.v1.SuggestFeatureViewsResponse
import com.acme.featurestore.v1.suggestFeatureViewsRequest
import com.acme.fleet.v1.ActivateInspectionRequest
import com.acme.fleet.v1.ActivateInspectionResponse
import com.acme.fleet.v1.ActivateInspectionResponseKt
import com.acme.fleet.v1.EstimateVehicleGroupRequest
import com.acme.fleet.v1.ExpireInspectionRequest
import com.acme.fleet.v1.ExpireInspectionResponse
import com.acme.fleet.v1.ExpireInspectionResponseKt
import com.acme.fleet.v1.ExportVehiclesRequestKt
import com.acme.fleet.v1.FindVehiclesRequest
import com.acme.fleet.v1.FindVehiclesRequestKt
import com.acme.fleet.v1.GetInspectionLifecycleRequest
import com.acme.fleet.v1.GetInspectionLifecycleResponse
import com.acme.fleet.v1.InspectionCalculationServiceGrpcKt
import com.acme.fleet.v1.InspectionLifecycleServiceGrpcKt
import com.acme.fleet.v1.QuoteInspectionRequestKt
import com.acme.fleet.v1.QuoteVehicleGroupRequest
import com.acme.fleet.v1.QuoteVehicleGroupResponse
import com.acme.fleet.v1.ReinstateInspectionRequest
import com.acme.fleet.v1.ReinstateInspectionResponse
import com.acme.fleet.v1.ReinstateInspectionResponseKt
import com.acme.fleet.v1.RenewInspectionRequest
import com.acme.fleet.v1.RenewInspectionResponse
import com.acme.fleet.v1.RenewInspectionResponseKt
import com.acme.fleet.v1.ResolveVehiclesRequest
import com.acme.fleet.v1.ResolveVehiclesResponse
import com.acme.fleet.v1.ScheduleInspectionExpiryRequest
import com.acme.fleet.v1.ScheduleInspectionExpiryResponse
import com.acme.fleet.v1.SuggestVehicleGroupsRequestKt
import com.acme.fleet.v1.SummarizeVehiclesRequestKt
import com.acme.fleet.v1.SuspendInspectionRequest
import com.acme.fleet.v1.SuspendInspectionResponse
import com.acme.fleet.v1.SuspendInspectionResponseKt
import com.acme.fleet.v1.TerminateInspectionRequest
import com.acme.fleet.v1.TerminateInspectionResponse
import com.acme.fleet.v1.TerminateInspectionResponseKt
import com.acme.fleet.v1.VehicleGroupCalculationServiceGrpcKt
import com.acme.fleet.v1.VehicleGroupQueryServiceGrpcKt
import com.acme.fleet.v1.VehicleQueryServiceGrpcKt
import com.acme.fleet.v1.VehicleServiceGrpcKt
import com.acme.fleet.v1.activateInspectionResponse
import com.acme.fleet.v1.batchGetVehiclesRequest
import com.acme.fleet.v1.copy as fleetCopy
import com.acme.fleet.v1.estimateVehicleGroupRequest
import com.acme.fleet.v1.expireInspectionResponse
import com.acme.fleet.v1.exportVehiclesRequest
import com.acme.fleet.v1.findVehiclesRequest
import com.acme.fleet.v1.getInspectionLifecycleResponse
import com.acme.fleet.v1.inspection
import com.acme.fleet.v1.inspectionSla
import com.acme.fleet.v1.quoteInspectionRequest
import com.acme.fleet.v1.quoteVehicleGroupRequest
import com.acme.fleet.v1.reinstateInspectionResponse
import com.acme.fleet.v1.renewInspectionResponse
import com.acme.fleet.v1.resolveVehiclesRequest
import com.acme.fleet.v1.scheduleInspectionExpiryResponse
import com.acme.fleet.v1.suggestVehicleGroupsRequest
import com.acme.fleet.v1.summarizeVehiclesRequest
import com.acme.fleet.v1.suspendInspectionResponse
import com.acme.fleet.v1.terminateInspectionResponse
import com.acme.fleet.v1.vehicleGroup
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.PartLifecycleServiceGrpcKt
import com.acme.maintenance.v1.SchedulePartExpiryRequest
import com.acme.maintenance.v1.SchedulePartExpiryRequestKt
import com.acme.maintenance.v1.part
import com.acme.maintenance.v1.schedulePartExpiryRequest
import com.acme.search.v1.PreviewSearchIndexRequestKt
import com.acme.search.v1.SearchIndexValidationServiceGrpcKt
import com.acme.search.v1.previewSearchIndexRequest
import com.acme.search.v1.searchIndex
import com.acme.surveys.v1.CountSurveysRequestKt
import com.acme.surveys.v1.SurveyQueryServiceGrpcKt
import com.acme.surveys.v1.countSurveysRequest
import com.acme.transport.fleet.FleetRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Moves an inspection between the states of its lifecycle. */
class InspectionLifecycleController(
    private val store: FleetRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventQueryService: AuditEventQueryServiceGrpcKt.AuditEventQueryServiceCoroutineStub,
    private val checkoutCheckoutSessionQueryService: CheckoutSessionQueryServiceGrpcKt.CheckoutSessionQueryServiceCoroutineStub,
    private val featureflagsFeatureFlagService: FeatureFlagServiceGrpcKt.FeatureFlagServiceCoroutineStub,
    private val featurestoreFeatureViewQueryService: FeatureViewQueryServiceGrpcKt.FeatureViewQueryServiceCoroutineStub,
    private val fleetInspectionCalculationService: InspectionCalculationServiceGrpcKt.InspectionCalculationServiceCoroutineStub,
    private val fleetVehicleGroupCalculationService: VehicleGroupCalculationServiceGrpcKt.VehicleGroupCalculationServiceCoroutineStub,
    private val fleetVehicleGroupQueryService: VehicleGroupQueryServiceGrpcKt.VehicleGroupQueryServiceCoroutineStub,
    private val fleetVehicleQueryService: VehicleQueryServiceGrpcKt.VehicleQueryServiceCoroutineStub,
    private val fleetVehicleService: VehicleServiceGrpcKt.VehicleServiceCoroutineStub,
    private val maintenancePartLifecycleService: PartLifecycleServiceGrpcKt.PartLifecycleServiceCoroutineStub,
    private val searchSearchIndexValidationService: SearchIndexValidationServiceGrpcKt.SearchIndexValidationServiceCoroutineStub,
    private val surveysSurveyQueryService: SurveyQueryServiceGrpcKt.SurveyQueryServiceCoroutineStub,
) : InspectionLifecycleServiceGrpcKt.InspectionLifecycleServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(InspectionLifecycleController::class.java)

    private val invokeRenewInspection1: suspend (SuggestFeatureViewsRequest) -> SuggestFeatureViewsResponse = { payload ->
        featurestoreFeatureViewQueryService.suggestFeatureViews(payload)
    }

    private val workerForSuspendInspection1 = ReplyMailbox<BatchGetFeatureFlagsRequest, BatchGetFeatureFlagsResponse>(workers) { payload ->
        withTimeout(5_000) { featureflagsFeatureFlagService.batchGetFeatureFlags(payload) }
    }

    private val clientForSuspendInspection2 by lazy { auditAuditEventQueryService }

    /** Activates an inspection. */
    override suspend fun activateInspection(request: ActivateInspectionRequest): ActivateInspectionResponse = handleActivateInspection(request)

    /** Expires an inspection. */
    override suspend fun expireInspection(request: ExpireInspectionRequest): ExpireInspectionResponse =
        RpcBoundary.run("acme.fleet.v1.InspectionLifecycleService/ExpireInspection") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExpireInspection", request)
            log.atInfo().addKeyValue("operation", "ExpireInspection").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = countSurveysRequest {
                this.context = request.context
                this.filter = CountSurveysRequestKt.filter {
                    this.createdAfter = request.details.effectiveAt
                }
            }
            val reply0 = surveysSurveyQueryService.countSurveys(outbound0)
            val outbound1 = exportVehiclesRequest {
                this.context = request.context
                this.filter = ExportVehiclesRequestKt.filter {
                    this.createdAfter = request.details.effectiveAt
                    this.labels.putAll(reply0.result.detailsMap)
                    this.ownerId = request.details.newOwner.userId
                }
            }
            fleetVehicleQueryService.exportVehicles(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            expireInspectionResponse {
                this.inspection = inspection {
                    this.inspectionId = request.inspectionId
                }
                this.outcome = ExpireInspectionResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    /** Returns an inspection. */
    override suspend fun getInspectionLifecycle(request: GetInspectionLifecycleRequest): GetInspectionLifecycleResponse {
        return handleGetInspectionLifecycle(request)
    }

    /** Reinstates an inspection. */
    override suspend fun reinstateInspection(request: ReinstateInspectionRequest): ReinstateInspectionResponse = handleReinstateInspection(request)

    /** Renews an inspection. */
    override suspend fun renewInspection(request: RenewInspectionRequest): RenewInspectionResponse =
        RpcBoundary.run("acme.fleet.v1.InspectionLifecycleService/RenewInspection") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RenewInspection", request)
            log.atInfo().addKeyValue("operation", "RenewInspection").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = quoteVehicleGroupRequest {
                this.context = request.context
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.manager = userRef {
                                this.displayName = request.details.newOwner.displayName
                                this.email = request.details.newOwner.email
                                this.userId = request.details.newOwner.userId
                            }
                        }))
                    }))
                }
            }
            val adapter0 = OutboundPort<QuoteVehicleGroupRequest, QuoteVehicleGroupResponse> { payload ->
                fleetVehicleGroupCalculationService.quoteVehicleGroup(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = suggestFeatureViewsRequest {
                this.context = request.context
                this.filter = SuggestFeatureViewsRequestKt.filter {
                    this.createdAfter = reply0.vehicleGroup.audit.updatedAt
                    this.region = reply0.vehicleGroup.location.region
                }
            }
            val reply1 = invokeRenewInspection1(outbound1)
            val outbound2 = resolveVehiclesRequest {
                this.context = request.context
            }
            val invoke2: suspend (ResolveVehiclesRequest, Metadata) -> ResolveVehiclesResponse = fleetVehicleQueryService::resolveVehicles
            val reply2 = invoke2(outbound2, Metadata())
            val outbound3 = EstimateVehicleGroupRequest.newBuilder().apply {
                setContext(request.context)
                setVehicleGroup(vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.audit = auditInfo {
                                this.createdAt = reply0.vehicleGroup.audit.createdAt
                                this.createdBy = reply0.vehicleGroup.audit.createdBy
                            }
                            this.location = postalAddress {
                                this.recipient = reply0.vehicleGroup.location.recipient
                            }
                            this.manager = userRef {
                                this.email = request.details.newOwner.email
                            }
                        }))
                    }))
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply3 = outbound3.let { value -> fleetVehicleGroupCalculationService.estimateVehicleGroup(value) }
            renewInspectionResponse {
                this.inspection = inspection {
                    this.assignee = userRef {
                        this.displayName = reply0.vehicleGroup.manager.displayName
                    }
                    this.audit = auditInfo {
                        this.etag = reply3.vehicleGroup.audit.etag
                    }
                    this.inspectionId = request.inspectionId
                    this.requester = contactInfo {
                        this.email = reply3.vehicleGroup.manager.email
                    }
                }
                this.outcome = RenewInspectionResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.message = reply1.result.message
                    this.status = reply2.result.status
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.referenceIds.addAll(reply2.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    /** Schedules an inspection. */
    override suspend fun scheduleInspectionExpiry(request: ScheduleInspectionExpiryRequest): ScheduleInspectionExpiryResponse {
        return handleScheduleInspectionExpiry(request)
    }

    /** Suspends an inspection. */
    override suspend fun suspendInspection(request: SuspendInspectionRequest): SuspendInspectionResponse = handleSuspendInspection(request)

    /** Terminates an inspection. */
    override suspend fun terminateInspection(request: TerminateInspectionRequest): TerminateInspectionResponse =
        RpcBoundary.run("acme.fleet.v1.InspectionLifecycleService/TerminateInspection") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "TerminateInspection", request)
            log.atInfo().addKeyValue("operation", "TerminateInspection").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = summarizeVehiclesRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.details = SummarizeVehiclesRequestKt.details {
                    this.filters.putAll(request.context.baggageMap)
                }
                this.reason = request.reason
            }
            val reply0 = flowOf(outbound0).map { value -> fleetVehicleQueryService.summarizeVehicles(value) }.single()
            terminateInspectionResponse {
                this.inspection = inspection {
                    this.inspectionId = request.inspectionId
                }
                this.outcome = TerminateInspectionResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }

    private fun ActivateInspectionRequest.toCountCheckoutSessionsFromActivateInspection1(): CountCheckoutSessionsRequest {
        val source = this
        return countCheckoutSessionsRequest {
            this.context = source.context
            this.filter = CountCheckoutSessionsRequestKt.filter {
                this.ownerId = source.details.newOwner.userId
            }
        }
    }

    private suspend fun handleActivateInspection(request: ActivateInspectionRequest): ActivateInspectionResponse =
        RpcBoundary.run("acme.fleet.v1.InspectionLifecycleService/ActivateInspection") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ActivateInspection", request)
            log.atInfo().addKeyValue("operation", "ActivateInspection").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = suggestVehicleGroupsRequest {}.fleetCopy {
                this.context = request.context
                this.filter = SuggestVehicleGroupsRequestKt.filter {
                    this.createdAfter = request.details.effectiveAt
                    this.labels.putAll(request.annotationsMap)
                    this.ownerId = request.details.newOwner.userId
                }
            }
            val reply0 = fleetVehicleGroupQueryService.suggestVehicleGroups(outbound0)
            val outbound1 = request.toCountCheckoutSessionsFromActivateInspection1()
            val reply1 = checkoutCheckoutSessionQueryService.countCheckoutSessions(outbound1)
            activateInspectionResponse {
                this.inspection = inspection {
                    this.inspectionId = request.inspectionId
                }
                this.outcome = ActivateInspectionResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleGetInspectionLifecycle(request: GetInspectionLifecycleRequest): GetInspectionLifecycleResponse =
        RpcBoundary.run("acme.fleet.v1.InspectionLifecycleService/GetInspectionLifecycle") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetInspectionLifecycle", request)
            log.atInfo().addKeyValue("operation", "GetInspectionLifecycle").log("Handling transport request")
            val outbound0 = summarizeVehiclesRequest {
                this.annotations.putAll(request.context.baggageMap)
                this.context = request.context
            }
            val reply0 = fleetVehicleQueryService.summarizeVehicles(WireCopies.json(outbound0))
            getInspectionLifecycleResponse {
                this.inspection = inspection {
                    this.inspectionId = request.inspectionId
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = reply0.result.status
                }
            }
        }

    private data class ReinstateInspection0Command(
        val context: RequestContext,
        val detailsNotifyName: PersonName,
        val partOwnerDisplayName: String,
        val detailsNotifyEmail: String
    ) {
        fun toRequest(): SchedulePartExpiryRequest {
            val command = this
            return schedulePartExpiryRequest {
                this.context = command.context
                this.details = SchedulePartExpiryRequestKt.details {
                    this.notify = contactInfo {
                        this.email = command.detailsNotifyEmail
                        this.name = command.detailsNotifyName
                    }
                }
                this.part = part {
                    this.owner = userRef {
                        this.displayName = command.partOwnerDisplayName
                    }
                }
            }
        }
    }

    private suspend fun handleReinstateInspection(request: ReinstateInspectionRequest): ReinstateInspectionResponse =
        RpcBoundary.run("acme.fleet.v1.InspectionLifecycleService/ReinstateInspection") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ReinstateInspection", request)
            log.atInfo().addKeyValue("operation", "ReinstateInspection").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = ReinstateInspection0Command(
                context = request.context,
                detailsNotifyName = request.details.notifyContact.name,
                partOwnerDisplayName = request.details.newOwner.displayName,
                detailsNotifyEmail = request.details.newOwner.email
            ).toRequest()
            val reply0 = maintenancePartLifecycleService.schedulePartExpiry(outbound0)
            val outbound1 = findVehiclesRequest {
                this.context = request.context
                this.filter = FindVehiclesRequestKt.filter {
                    this.createdAfter = reply0.part.audit.createdAt
                    this.ownerId = request.details.newOwner.userId
                    this.region = reply0.part.address.region
                }
            }
            val command1: Submission<FindVehiclesRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = fleetVehicleQueryService.findVehicles(payload1)
            reinstateInspectionResponse {
                this.inspection = inspection {
                    this.inspectionId = request.inspectionId
                }
                this.outcome = ReinstateInspectionResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = reply0.outcome.status
                }
            }
        }

    private suspend fun handleScheduleInspectionExpiry(request: ScheduleInspectionExpiryRequest): ScheduleInspectionExpiryResponse =
        RpcBoundary.run("acme.fleet.v1.InspectionLifecycleService/ScheduleInspectionExpiry") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScheduleInspectionExpiry", request)
            log.atInfo().addKeyValue("operation", "ScheduleInspectionExpiry").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.inspection.inspectionId).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                previewSearchIndexRequest {
                    this.context = request.context
                    this.details = PreviewSearchIndexRequestKt.details {
                        this.contact = contactInfo {
                            this.email = request.inspection.assignee.email
                            this.name = request.details.notify.name
                        }
                    }
                    this.searchIndex = searchIndex {
                        this.audit = auditInfo {
                            this.updatedAt = request.inspection.audit.updatedAt
                        }
                        this.owner = userRef {
                            this.email = request.details.notify.email
                        }
                    }
                }
            }
            val reply0 = searchSearchIndexValidationService.previewSearchIndex(outbound0)
            val outbound1 = quoteInspectionRequest {
                this.context = request.context
                this.details = QuoteInspectionRequestKt.details {
                    this.destination = postalAddress {
                        this.postalCode = reply0.searchIndex.address.postalCode
                    }
                }
                this.inspection = inspection {
                    this.assignee = userRef {
                        this.displayName = request.inspection.assignee.displayName
                        this.email = request.inspection.requester.email
                    }
                    this.requester = contactInfo {
                        this.email = request.details.notify.email
                    }
                }
            }
            val reply1 = ClientRegistry.lookup("acme.fleet.v1.InspectionCalculationService", InspectionCalculationServiceGrpcKt.InspectionCalculationServiceCoroutineStub::class.java).quoteInspection(outbound1)
            scheduleInspectionExpiryResponse {
                this.inspection = inspection {
                    this.inspectionId = request.inspectionId
                    this.priority = request.inspection.priority
                    this.requester = contactInfo {
                        this.name = reply1.inspection.requester.name
                    }
                    this.sla = inspectionSla {
                        this.breached = reply1.inspection.sla.breached
                    }
                    this.tags.addAll(reply0.searchIndex.tagsList)
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSuspendInspection(request: SuspendInspectionRequest): SuspendInspectionResponse =
        RpcBoundary.run("acme.fleet.v1.InspectionLifecycleService/SuspendInspection") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SuspendInspection", request)
            log.atInfo().addKeyValue("operation", "SuspendInspection").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = batchGetVehiclesRequest {
                this.context = request.context
            }
            val reply0 = fleetVehicleService.batchGetVehicles(WireCopies.reflective(outbound0))
            val outbound1 = batchGetFeatureFlagsRequest {
                this.context = request.context
            }
            val reply1 = workerForSuspendInspection1.submit(outbound1)
            val outbound2 = findAuditEventsRequest {
                this.context = request.context
                this.filter = FindAuditEventsRequestKt.filter {
                    this.ownerId = reply0.result.correlationId
                }
            }
            val reply2 = clientForSuspendInspection2.findAuditEvents(outbound2)
            suspendInspectionResponse {
                this.inspection = inspection {
                    this.inspectionId = request.inspectionId
                }
                this.outcome = SuspendInspectionResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply1.result.message
                    this.status = reply0.result.status
                }
            }
        }
}
