package com.acme.transport.routing.adapters

import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.CheckAuditEventEligibilityRequestKt
import com.acme.audit.v1.auditEvent
import com.acme.audit.v1.auditTrailRef
import com.acme.audit.v1.checkAuditEventEligibilityRequest
import com.acme.authorization.v1.AccessPolicyValidationServiceGrpcKt
import com.acme.authorization.v1.LintAccessPoliciesRequestKt
import com.acme.authorization.v1.lintAccessPoliciesRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.drivers.v1.driverRef
import com.acme.identity.v1.CountUsersRequest
import com.acme.identity.v1.CountUsersRequestKt
import com.acme.identity.v1.CountUsersResponse
import com.acme.identity.v1.UserQueryServiceGrpcKt
import com.acme.identity.v1.countUsersRequest
import com.acme.identity.v1.userRef
import com.acme.patients.v1.CalculatePatientRequestKt
import com.acme.patients.v1.PatientCalculationServiceGrpcKt
import com.acme.patients.v1.calculatePatientRequest
import com.acme.patients.v1.patient
import com.acme.prescriptions.v1.CalculatePrescriptionRequestKt
import com.acme.prescriptions.v1.PrescriptionCalculationServiceGrpcKt
import com.acme.prescriptions.v1.calculatePrescriptionRequest
import com.acme.routing.v1.BatchCreateRoutesRequest
import com.acme.routing.v1.BatchCreateRoutesResponse
import com.acme.routing.v1.BatchGetRoutesRequest
import com.acme.routing.v1.BatchGetRoutesResponse
import com.acme.routing.v1.BrowseRoutesRequest
import com.acme.routing.v1.BrowseRoutesRequestKt
import com.acme.routing.v1.BrowseRoutesResponse
import com.acme.routing.v1.CloneRouteRequest
import com.acme.routing.v1.CloneRouteResponse
import com.acme.routing.v1.CreateRouteRequest
import com.acme.routing.v1.CreateRouteResponse
import com.acme.routing.v1.DeleteRouteRequest
import com.acme.routing.v1.DeleteRouteResponse
import com.acme.routing.v1.ExplainRouteCalculationRequest
import com.acme.routing.v1.ExplainRouteCalculationResponse
import com.acme.routing.v1.ExportRoutesRequestKt
import com.acme.routing.v1.GetRouteRequest
import com.acme.routing.v1.GetRouteResponse
import com.acme.routing.v1.ListRoutesRequest
import com.acme.routing.v1.ListRoutesResponse
import com.acme.routing.v1.RouteCalculationServiceGrpcKt
import com.acme.routing.v1.RoutePlanCalculationServiceGrpcKt
import com.acme.routing.v1.RouteQueryServiceGrpcKt
import com.acme.routing.v1.RouteServiceGrpcKt
import com.acme.routing.v1.SearchRoutesRequest
import com.acme.routing.v1.SearchRoutesResponse
import com.acme.routing.v1.StopQueryServiceGrpcKt
import com.acme.routing.v1.SuggestStopsRequestKt
import com.acme.routing.v1.SuggestStopsResponse
import com.acme.routing.v1.UpdateRouteRequest
import com.acme.routing.v1.UpdateRouteResponse
import com.acme.routing.v1.WatchRoutesRequest
import com.acme.routing.v1.WatchRoutesResponse
import com.acme.routing.v1.batchCreateRoutesResponse
import com.acme.routing.v1.batchGetRoutesResponse
import com.acme.routing.v1.browseRoutesRequest
import com.acme.routing.v1.calculateRoutePlanRequest
import com.acme.routing.v1.cloneRouteResponse
import com.acme.routing.v1.copy as routingCopy
import com.acme.routing.v1.createRouteResponse
import com.acme.routing.v1.deleteRouteResponse
import com.acme.routing.v1.explainRouteCalculationRequest
import com.acme.routing.v1.exportRoutesRequest
import com.acme.routing.v1.getRouteResponse
import com.acme.routing.v1.listRoutesResponse
import com.acme.routing.v1.route
import com.acme.routing.v1.routeCardSettlement
import com.acme.routing.v1.routeLine
import com.acme.routing.v1.routeLineTax
import com.acme.routing.v1.routePlan
import com.acme.routing.v1.routePlanCardSettlement
import com.acme.routing.v1.searchRoutesResponse
import com.acme.routing.v1.simulateRouteRequest
import com.acme.routing.v1.suggestStopsRequest
import com.acme.routing.v1.updateRouteResponse
import com.acme.routing.v1.watchRoutesResponse
import com.acme.search.v1.PreviewSearchIndexRequest
import com.acme.search.v1.ScoreSearchIndexRequest
import com.acme.search.v1.ScoreSearchIndexRequestKt
import com.acme.search.v1.SearchIndexValidationServiceGrpcKt
import com.acme.search.v1.previewSearchIndexRequest
import com.acme.search.v1.scoreSearchIndexRequest
import com.acme.search.v1.searchIndex
import com.acme.telehealth.v1.VideoVisitCalculationServiceGrpcKt
import com.acme.telehealth.v1.estimateVideoVisitRequest
import com.acme.telehealth.v1.videoVisit
import com.acme.telematics.v1.CheckGeoFenceEligibilityRequest
import com.acme.telematics.v1.CheckGeoFenceEligibilityResponse
import com.acme.telematics.v1.GeoFenceValidationServiceGrpcKt
import com.acme.telematics.v1.checkGeoFenceEligibilityRequest
import com.acme.telematics.v1.geoFence
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.transport.routing.RoutingRepository
import com.acme.venues.v1.SearchVenuesRequestKt
import com.acme.venues.v1.VenueServiceGrpcKt
import com.acme.venues.v1.searchVenuesRequest
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Manages routes: creates, reads, changes, and lists them. */
class RouteController(
    private val store: RoutingRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventValidationService: AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub,
    private val authorizationAccessPolicyValidationService: AccessPolicyValidationServiceGrpcKt.AccessPolicyValidationServiceCoroutineStub,
    private val identityUserQueryService: UserQueryServiceGrpcKt.UserQueryServiceCoroutineStub,
    private val patientsPatientCalculationService: PatientCalculationServiceGrpcKt.PatientCalculationServiceCoroutineStub,
    private val prescriptionsPrescriptionCalculationService: PrescriptionCalculationServiceGrpcKt.PrescriptionCalculationServiceCoroutineStub,
    private val routingRouteCalculationService: RouteCalculationServiceGrpcKt.RouteCalculationServiceCoroutineStub,
    private val routingRoutePlanCalculationService: RoutePlanCalculationServiceGrpcKt.RoutePlanCalculationServiceCoroutineStub,
    private val routingRouteQueryService: RouteQueryServiceGrpcKt.RouteQueryServiceCoroutineStub,
    private val routingStopQueryService: StopQueryServiceGrpcKt.StopQueryServiceCoroutineStub,
    private val searchSearchIndexValidationService: SearchIndexValidationServiceGrpcKt.SearchIndexValidationServiceCoroutineStub,
    private val telehealthVideoVisitCalculationService: VideoVisitCalculationServiceGrpcKt.VideoVisitCalculationServiceCoroutineStub,
    private val telematicsGeoFenceValidationService: GeoFenceValidationServiceGrpcKt.GeoFenceValidationServiceCoroutineStub,
    private val venuesVenueService: VenueServiceGrpcKt.VenueServiceCoroutineStub,
) : RouteServiceGrpcKt.RouteServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(RouteController::class.java)

    private val invokeCreateRoute0: suspend (ExplainRouteCalculationRequest) -> ExplainRouteCalculationResponse = { payload ->
        routingRouteCalculationService.explainRouteCalculation(payload)
    }

    private val workerForListRoutes0 = ReplyMailbox<CountUsersRequest, CountUsersResponse>(workers) { payload ->
        withTimeout(5_000) { identityUserQueryService.countUsers(payload) }
    }

    /** Takes a stream of routes and answers once at the end. */
    override suspend fun batchCreateRoutes(requests: Flow<BatchCreateRoutesRequest>): BatchCreateRoutesResponse {
        var answer = BatchCreateRoutesResponse.getDefaultInstance()
        requests.map { request -> handleBatchCreateRoutes(request) }.collect { answer = it }
        return answer
    }

    /** Returns several routes at once. */
    override suspend fun batchGetRoutes(request: BatchGetRoutesRequest): BatchGetRoutesResponse =
        RpcBoundary.run("acme.routing.v1.RouteService/BatchGetRoutes") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BatchGetRoutes", request)
            log.atInfo().addKeyValue("operation", "BatchGetRoutes").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            batchGetRoutesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Creates a route. */
    override suspend fun cloneRoute(request: CloneRouteRequest): CloneRouteResponse {
        return handleCloneRoute(request)
    }

    /** Creates a route. */
    override suspend fun createRoute(request: CreateRouteRequest): CreateRouteResponse = handleCreateRoute(request)

    /** Deletes a route. */
    override suspend fun deleteRoute(request: DeleteRouteRequest): DeleteRouteResponse =
        RpcBoundary.run("acme.routing.v1.RouteService/DeleteRoute") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DeleteRoute", request)
            log.atInfo().addKeyValue("operation", "DeleteRoute").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.routeId).log("Recorded audit detail")
            val outbound0 = estimateVideoVisitRequest {
                this.context = request.context
                this.videoVisit = videoVisit {
                    this.audit = auditInfo {
                        this.etag = request.etag
                    }
                }
            }
            val reply0 = ClientRegistry.lookup("acme.telehealth.v1.VideoVisitCalculationService", VideoVisitCalculationServiceGrpcKt.VideoVisitCalculationServiceCoroutineStub::class.java).estimateVideoVisit(outbound0)
            deleteRouteResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
            }
        }

    /** Returns a route. */
    override suspend fun getRoute(request: GetRouteRequest): GetRouteResponse {
        return handleGetRoute(request)
    }

    /** Lists routes a page at a time. */
    override suspend fun listRoutes(request: ListRoutesRequest): ListRoutesResponse = handleListRoutes(request)

    /** Lists routes a page at a time. */
    override suspend fun searchRoutes(request: SearchRoutesRequest): SearchRoutesResponse =
        RpcBoundary.run("acme.routing.v1.RouteService/SearchRoutes") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SearchRoutes", request)
            log.atInfo().addKeyValue("operation", "SearchRoutes").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = exportRoutesRequest {
                this.context = request.context
                this.filter = ExportRoutesRequestKt.filter {
                    this.createdAfter = request.filter.createdAfter
                }
            }
            routingRouteQueryService.exportRoutes(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            searchRoutesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Changes a route. */
    override suspend fun updateRoute(request: UpdateRouteRequest): UpdateRouteResponse {
        return handleUpdateRoute(request)
    }

    /** Streams the changes of routes as they happen. */
    override fun watchRoutes(request: WatchRoutesRequest): Flow<WatchRoutesResponse> = flow {
        emit(handleWatchRoutes(request))
    }

    private data class BatchCreateRoutes0Command(
        val context: RequestContext,
        val detailsDestinationLines: List<String>,
        val searchIndexAddressCountryCode: String,
        val detailsDestinationPostalCode: String,
        val searchIndexOwnerEmail: String
    ) {
        fun toRequest(): ScoreSearchIndexRequest {
            val command = this
            return scoreSearchIndexRequest {
                this.context = command.context
                this.details = ScoreSearchIndexRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(command.detailsDestinationLines)
                        this.postalCode = command.detailsDestinationPostalCode
                    }
                }
                this.searchIndex = searchIndex {
                    this.address = postalAddress {
                        this.countryCode = command.searchIndexAddressCountryCode
                    }
                    this.owner = userRef {
                        this.email = command.searchIndexOwnerEmail
                    }
                }
            }
        }
    }

    private suspend fun handleBatchCreateRoutes(request: BatchCreateRoutesRequest): BatchCreateRoutesResponse =
        RpcBoundary.run("acme.routing.v1.RouteService/BatchCreateRoutes") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BatchCreateRoutes", request)
            log.atInfo().addKeyValue("operation", "BatchCreateRoutes").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.header.source).log("Recorded audit detail")
            val outbound0 = BatchCreateRoutes0Command(
                context = request.context,
                detailsDestinationLines = request.record.billingAddress.linesList,
                searchIndexAddressCountryCode = request.record.billingAddress.countryCode,
                detailsDestinationPostalCode = request.record.billingAddress.postalCode,
                searchIndexOwnerEmail = request.header.submittedBy.email
            ).toRequest()
            val reply0 = searchSearchIndexValidationService.scoreSearchIndex(outbound0)
            var reply1 = SuggestStopsResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.record.linesList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound1 = suggestStopsRequest {
                                this.context = request.context
                                this.filter = SuggestStopsRequestKt.filter {
                                    this.ownerId = item.lineId
                                    this.region = request.record.billingAddress.region
                                }
                            }
                            routingStopQueryService.suggestStops(outbound1)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply1 = it }
                }
            }
            batchCreateRoutesResponse {
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleCloneRoute(request: CloneRouteRequest): CloneRouteResponse =
        RpcBoundary.run("acme.routing.v1.RouteService/CloneRoute") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CloneRoute", request)
            log.atInfo().addKeyValue("operation", "CloneRoute").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.route.routeId).log("Recorded audit detail")
            val outbound0 = checkGeoFenceEligibilityRequest {
                this.context = request.context
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.updatedAt = request.route.audit.updatedAt
                    }
                    this.owner = userRef {
                        this.displayName = request.route.counterparty.displayName
                    }
                }
            }
            val adapter0 = OutboundPort<CheckGeoFenceEligibilityRequest, CheckGeoFenceEligibilityResponse> { payload ->
                telematicsGeoFenceValidationService.checkGeoFenceEligibility(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            cloneRouteResponse {
                this.result = operationResult {
                    this.message = reply0.outcome.message
                    this.status = "ok"
                }
                this.route = route {
                    this.counterparty = driverRef {
                        this.email = reply0.geoFence.owner.email
                    }
                    this.status = request.route.status
                    this.total = request.route.total
                }
            }
        }

    private suspend fun handleCreateRoute(request: CreateRouteRequest): CreateRouteResponse =
        RpcBoundary.run("acme.routing.v1.RouteService/CreateRoute") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CreateRoute", request)
            log.atInfo().addKeyValue("operation", "CreateRoute").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.route.routeId).log("Recorded audit detail")
            val outbound0 = explainRouteCalculationRequest {
                this.context = request.context
                this.route = route {
                    this.billingAddress = postalAddress {
                        this.postalCode = request.route.billingAddress.postalCode
                    }
                    this.card = routeCardSettlement {
                        this.last4 = request.route.card.last4
                    }
                    this.counterparty = driverRef {
                        this.driverId = request.route.counterparty.driverId
                    }
                }
            }
            val reply0 = invokeCreateRoute0(outbound0)
            val outbound1 = browseRoutesRequest {
                this.context = request.context
                this.filter = BrowseRoutesRequestKt.filter {
                    this.createdAfter = request.route.audit.createdAt
                    this.labels.putAll(reply0.result.detailsMap)
                    this.ownerId = reply0.route.counterparty.driverId
                    this.region = request.route.billingAddress.region
                }
            }
            val invoke1: suspend (BrowseRoutesRequest, Metadata) -> BrowseRoutesResponse = routingRouteQueryService::browseRoutes
            val reply1 = invoke1(outbound1, Metadata())
            val outbound2 = PreviewSearchIndexRequest.newBuilder().apply {
                setContext(request.context)
                setSearchIndex(searchIndex {
                    this.address = postalAddress {
                        this.locality = reply0.route.billingAddress.locality
                        this.region = request.route.billingAddress.region
                    }
                    this.audit = auditInfo {
                        this.updatedBy = reply0.route.audit.updatedBy
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply2 = outbound2.let { value -> searchSearchIndexValidationService.previewSearchIndex(value) }
            val outbound3 = RequestFactory.create {
                searchVenuesRequest {
                    this.context = request.context
                    this.filter = SearchVenuesRequestKt.filter {
                        this.createdAfter = reply0.route.audit.updatedAt
                        this.region = reply0.route.billingAddress.region
                    }
                }
            }
            val reply3 = venuesVenueService.searchVenues(outbound3)
            createRouteResponse {
                this.result = operationResult {
                    this.message = reply3.result.message
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
                this.route = route {
                    this.audit = auditInfo {
                        this.createdBy = reply0.route.audit.createdBy
                        this.updatedBy = reply2.searchIndex.audit.updatedBy
                    }
                    this.billingAddress = postalAddress {
                        this.lines.addAll(reply0.route.billingAddress.linesList)
                    }
                    this.metadata.putAll(request.route.metadataMap)
                    this.notes = request.route.notes
                }
            }
        }

    private suspend fun handleGetRoute(request: GetRouteRequest): GetRouteResponse =
        RpcBoundary.run("acme.routing.v1.RouteService/GetRoute") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetRoute", request)
            log.atInfo().addKeyValue("operation", "GetRoute").log("Handling transport request")
            val outbound0 = lintAccessPoliciesRequest {
                this.context = request.context
                this.details = LintAccessPoliciesRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = authorizationAccessPolicyValidationService.lintAccessPolicies(WireCopies.reflective(outbound0))
            getRouteResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.route = route {
                    this.lines.addAll(listOf(routeLine {
                        this.tax = routeLineTax {
                            this.amount = reply0.accessPolicy.discount.amount
                        }
                    }))
                    this.routeId = request.routeId
                }
            }
        }

    private suspend fun handleListRoutes(request: ListRoutesRequest): ListRoutesResponse =
        RpcBoundary.run("acme.routing.v1.RouteService/ListRoutes") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListRoutes", request)
            log.atInfo().addKeyValue("operation", "ListRoutes").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = countUsersRequest {
                this.context = request.context
                this.filter = CountUsersRequestKt.filter {
                    this.createdAfter = request.filter.createdAfter
                    this.labels.putAll(request.filter.labelsMap)
                }
            }
            val reply0 = workerForListRoutes0.submit(outbound0)
            listRoutesResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleUpdateRoute(request: UpdateRouteRequest): UpdateRouteResponse =
        RpcBoundary.run("acme.routing.v1.RouteService/UpdateRoute") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "UpdateRoute", request)
            log.atInfo().addKeyValue("operation", "UpdateRoute").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.route.number).log("Recorded audit detail")
            val outbound0 = calculatePrescriptionRequest {
                this.context = request.context
                this.details = CalculatePrescriptionRequestKt.details {
                    this.destination = postalAddress {
                        this.locality = request.route.billingAddress.locality
                    }
                }
            }
            val reply0 = flowOf(outbound0).map { value -> prescriptionsPrescriptionCalculationService.calculatePrescription(value) }.single()
            val outbound1 = calculatePatientRequest {
                this.context = request.context
                this.details = CalculatePatientRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = reply0.prescription.billingAddress.countryCode
                    }
                }
                this.patient = patient {
                    this.audit = auditInfo {
                        this.etag = request.route.audit.etag
                    }
                    this.relationshipManager = userRef {
                        this.email = reply0.prescription.counterparty.email
                    }
                }
            }
            val reply1 = withTimeout(5_000) { patientsPatientCalculationService.calculatePatient(outbound1) }
            updateRouteResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
                this.route = route {
                    this.billingAddress = postalAddress {
                        this.recipient = reply0.prescription.billingAddress.recipient
                    }
                    this.issuedAt = request.route.issuedAt
                    this.lines.addAll(listOf(routeLine {
                        this.attributes.putAll(reply1.patient.attributesMap)
                    }))
                    this.routeId = request.route.routeId
                }
            }
        }

    private suspend fun handleWatchRoutes(request: WatchRoutesRequest): WatchRoutesResponse =
        RpcBoundary.run("acme.routing.v1.RouteService/WatchRoutes") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "WatchRoutes", request)
            log.atInfo().addKeyValue("operation", "WatchRoutes").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val configure0: SuggestStopsRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.filter = SuggestStopsRequestKt.filter {
                    this.createdAfter = request.filter.createdAfter
                }
            }
            val outbound0 = suggestStopsRequest(configure0)
            val reply0 = routingStopQueryService.suggestStops(outbound0)
            val outbound1 = simulateRouteRequest {
                this.context = request.context
                this.route = route {
                    this.billingAddress = postalAddress {
                        this.region = request.filter.region
                    }
                    this.counterparty = driverRef {
                        this.email = request.subscriber.email
                    }
                }
            }
            val reply1 = routingRouteCalculationService.simulateRoute(outbound1)
            val outbound2 = checkAuditEventEligibilityRequest {
                this.auditEvent = auditEvent {
                    this.subject = auditTrailRef {
                        this.displayName = reply1.route.counterparty.displayName
                        this.number = reply1.route.number
                    }
                }
                this.context = request.context
                this.details = CheckAuditEventEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.email = reply1.route.counterparty.email
                    }
                }
            }
            val reply2 = auditAuditEventValidationService.checkAuditEventEligibility(outbound2)
            val outbound3 = calculateRoutePlanRequest {}.routingCopy {
                this.context = request.context
                this.routePlan = routePlan {
                    this.audit = auditInfo {
                        this.createdBy = reply1.route.audit.createdBy
                    }
                    this.card = routePlanCardSettlement {
                        this.last4 = reply1.route.card.last4
                    }
                }
            }
            val reply3 = routingRoutePlanCalculationService.calculateRoutePlan(outbound3)
            watchRoutesResponse {
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
                this.route = route {
                    this.audit = auditInfo {
                        this.createdAt = reply3.routePlan.audit.createdAt
                        this.createdBy = reply3.routePlan.audit.createdBy
                    }
                    this.billingAddress = postalAddress {
                        this.region = request.filter.region
                    }
                    this.counterparty = driverRef {
                        this.displayName = reply1.route.counterparty.displayName
                        this.email = request.subscriber.email
                    }
                }
            }
        }
}
