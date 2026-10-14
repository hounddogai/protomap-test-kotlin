package com.acme.transport.fleet.adapters

import com.acme.admissions.v1.AdmissionCalculationServiceGrpcKt
import com.acme.admissions.v1.ExplainAdmissionCalculationRequestKt
import com.acme.admissions.v1.admission
import com.acme.admissions.v1.explainAdmissionCalculationRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.pageRequest
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.customerRef
import com.acme.deliveries.v1.ProofOfDeliveryValidationServiceGrpcKt
import com.acme.deliveries.v1.previewProofOfDeliveryRequest
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.documents.v1.DocumentValidationServiceGrpcKt
import com.acme.documents.v1.ValidateDocumentRequestKt
import com.acme.documents.v1.document
import com.acme.documents.v1.validateDocumentRequest
import com.acme.drivers.v1.driverRef
import com.acme.fleet.v1.BrowseVehiclesRequest
import com.acme.fleet.v1.BrowseVehiclesResponse
import com.acme.fleet.v1.CheckInspectionEligibilityRequest
import com.acme.fleet.v1.CheckVehicleEligibilityRequest
import com.acme.fleet.v1.CheckVehicleEligibilityResponse
import com.acme.fleet.v1.CountVehiclesRequest
import com.acme.fleet.v1.CountVehiclesResponse
import com.acme.fleet.v1.ExportVehiclesRequest
import com.acme.fleet.v1.ExportVehiclesResponse
import com.acme.fleet.v1.FindVehiclesRequest
import com.acme.fleet.v1.FindVehiclesResponse
import com.acme.fleet.v1.InspectionCalculationServiceGrpcKt
import com.acme.fleet.v1.InspectionValidationServiceGrpcKt
import com.acme.fleet.v1.LookupVehicleRequest
import com.acme.fleet.v1.LookupVehicleResponse
import com.acme.fleet.v1.PeekVehicleRequest
import com.acme.fleet.v1.PeekVehicleResponse
import com.acme.fleet.v1.PreviewInspectionRequest
import com.acme.fleet.v1.ResolveVehiclesRequest
import com.acme.fleet.v1.ResolveVehiclesResponse
import com.acme.fleet.v1.SummarizeVehiclesRequest
import com.acme.fleet.v1.SummarizeVehiclesResponse
import com.acme.fleet.v1.VehicleQueryServiceGrpcKt
import com.acme.fleet.v1.VehicleValidationServiceGrpcKt
import com.acme.fleet.v1.browseVehiclesResponse
import com.acme.fleet.v1.checkInspectionEligibilityRequest
import com.acme.fleet.v1.checkVehicleEligibilityRequest
import com.acme.fleet.v1.compareInspectionOptionsRequest
import com.acme.fleet.v1.countVehiclesResponse
import com.acme.fleet.v1.exportVehiclesResponse
import com.acme.fleet.v1.findVehiclesResponse
import com.acme.fleet.v1.inspection
import com.acme.fleet.v1.inspectionComment
import com.acme.fleet.v1.inspectionSla
import com.acme.fleet.v1.lookupVehicleResponse
import com.acme.fleet.v1.peekVehicleResponse
import com.acme.fleet.v1.previewInspectionRequest
import com.acme.fleet.v1.resolveVehiclesResponse
import com.acme.fleet.v1.summarizeVehiclesResponse
import com.acme.fleet.v1.vehicle
import com.acme.fleet.v1.vehicleGroupRef
import com.acme.identity.v1.userRef
import com.acme.metrics.v1.MetricSeriesQueryServiceGrpcKt
import com.acme.metrics.v1.SummarizeMetricSeriesesRequest
import com.acme.metrics.v1.SummarizeMetricSeriesesRequestKt
import com.acme.metrics.v1.SummarizeMetricSeriesesResponse
import com.acme.metrics.v1.summarizeMetricSeriesesRequest
import com.acme.notifications.v1.ListNotificationsRequest
import com.acme.notifications.v1.ListNotificationsRequestKt
import com.acme.notifications.v1.ListNotificationsResponse
import com.acme.notifications.v1.NotificationServiceGrpcKt
import com.acme.notifications.v1.listNotificationsRequest
import com.acme.routing.v1.PreviewRoutePlanRequest
import com.acme.routing.v1.RoutePlanValidationServiceGrpcKt
import com.acme.routing.v1.previewRoutePlanRequest
import com.acme.routing.v1.routePlan
import com.acme.search.v1.CountSearchIndexesRequest
import com.acme.search.v1.CountSearchIndexesRequestKt
import com.acme.search.v1.CountSearchIndexesResponse
import com.acme.search.v1.SearchIndexQueryServiceGrpcKt
import com.acme.search.v1.countSearchIndexesRequest
import com.acme.telematics.v1.CompareTripLogOptionsRequestKt
import com.acme.telematics.v1.ExplainTripLogCalculationRequestKt
import com.acme.telematics.v1.TripLogCalculationServiceGrpcKt
import com.acme.telematics.v1.compareTripLogOptionsRequest
import com.acme.telematics.v1.explainTripLogCalculationRequest
import com.acme.telematics.v1.tripLog
import com.acme.transport.fleet.FleetRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.google.protobuf.Timestamp
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Answers read-only questions about vehicles, for screens and reports. */
class VehicleQueryController(
    private val store: FleetRepository,
    private val workers: CoroutineScope,
    private val admissionsAdmissionCalculationService: AdmissionCalculationServiceGrpcKt.AdmissionCalculationServiceCoroutineStub,
    private val deliveriesProofOfDeliveryValidationService: ProofOfDeliveryValidationServiceGrpcKt.ProofOfDeliveryValidationServiceCoroutineStub,
    private val documentsDocumentValidationService: DocumentValidationServiceGrpcKt.DocumentValidationServiceCoroutineStub,
    private val fleetInspectionCalculationService: InspectionCalculationServiceGrpcKt.InspectionCalculationServiceCoroutineStub,
    private val fleetInspectionValidationService: InspectionValidationServiceGrpcKt.InspectionValidationServiceCoroutineStub,
    private val fleetVehicleValidationService: VehicleValidationServiceGrpcKt.VehicleValidationServiceCoroutineStub,
    private val metricsMetricSeriesQueryService: MetricSeriesQueryServiceGrpcKt.MetricSeriesQueryServiceCoroutineStub,
    private val notificationsNotificationService: NotificationServiceGrpcKt.NotificationServiceCoroutineStub,
    private val routingRoutePlanValidationService: RoutePlanValidationServiceGrpcKt.RoutePlanValidationServiceCoroutineStub,
    private val searchSearchIndexQueryService: SearchIndexQueryServiceGrpcKt.SearchIndexQueryServiceCoroutineStub,
    private val telematicsTripLogCalculationService: TripLogCalculationServiceGrpcKt.TripLogCalculationServiceCoroutineStub,
) : VehicleQueryServiceGrpcKt.VehicleQueryServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(VehicleQueryController::class.java)

    private val invokeCountVehicles1: suspend (SummarizeMetricSeriesesRequest) -> SummarizeMetricSeriesesResponse = { payload ->
        metricsMetricSeriesQueryService.summarizeMetricSerieses(payload)
    }

    private val workerForPeekVehicle0 = ReplyMailbox<CountSearchIndexesRequest, CountSearchIndexesResponse>(workers) { payload ->
        withTimeout(5_000) { searchSearchIndexQueryService.countSearchIndexes(payload) }
    }

    private val clientForPeekVehicle1 by lazy { admissionsAdmissionCalculationService }

    /** Lists vehicles a page at a time. */
    override suspend fun browseVehicles(request: BrowseVehiclesRequest): BrowseVehiclesResponse = handleBrowseVehicles(request)

    /** Counts vehicles by status. */
    override suspend fun countVehicles(request: CountVehiclesRequest): CountVehiclesResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleQueryService/CountVehicles") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CountVehicles", request)
            log.atInfo().addKeyValue("operation", "CountVehicles").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = listNotificationsRequest {
                this.context = request.context
                this.page = pageRequest {
                    this.pageSize = request.page.pageSize
                    this.pageToken = request.page.pageToken
                }
            }
            val adapter0 = OutboundPort<ListNotificationsRequest, ListNotificationsResponse> { payload ->
                notificationsNotificationService.listNotifications(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = summarizeMetricSeriesesRequest {
                this.annotations.putAll(reply0.result.detailsMap)
                this.context = request.context
                this.details = SummarizeMetricSeriesesRequestKt.details {
                    this.filters.putAll(request.filter.labelsMap)
                }
            }
            val reply1 = invokeCountVehicles1(outbound1)
            countVehiclesResponse {
                this.result = operationResult {
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = reply0.result.status
                }
            }
        }

    /** Streams every matching vehicle. */
    override fun exportVehicles(request: ExportVehiclesRequest): Flow<ExportVehiclesResponse> = flow {
        emit(handleExportVehicles(request))
    }

    /** Lists vehicles a page at a time. */
    override suspend fun findVehicles(request: FindVehiclesRequest): FindVehiclesResponse = handleFindVehicles(request)

    /** Returns a vehicle. */
    override suspend fun lookupVehicle(request: LookupVehicleRequest): LookupVehicleResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleQueryService/LookupVehicle") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LookupVehicle", request)
            log.atInfo().addKeyValue("operation", "LookupVehicle").log("Handling transport request")
            val outbound0 = validateDocumentRequest {
                this.context = request.context
                this.details = ValidateDocumentRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                    this.locale = request.context.locale
                }
                this.document = document {
                    this.audit = auditInfo {
                        this.createdAt = request.context.deadline
                    }
                    this.metadata.putAll(request.context.baggageMap)
                }
            }
            val reply0 = documentsDocumentValidationService.validateDocument(WireCopies.reflective(outbound0))
            lookupVehicleResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
                this.vehicle = vehicle {
                    this.vehicleId = request.vehicleId
                }
            }
        }

    /** Returns a vehicle. */
    override suspend fun peekVehicle(request: PeekVehicleRequest): PeekVehicleResponse {
        return handlePeekVehicle(request)
    }

    /** Returns several vehicles at once. */
    override suspend fun resolveVehicles(request: ResolveVehiclesRequest): ResolveVehiclesResponse = handleResolveVehicles(request)

    /** Summarizes a vehicle. */
    override suspend fun summarizeVehicles(request: SummarizeVehiclesRequest): SummarizeVehiclesResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleQueryService/SummarizeVehicles") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SummarizeVehicles", request)
            log.atInfo().addKeyValue("operation", "SummarizeVehicles").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicleId).log("Recorded audit detail")
            val outbound0 = previewProofOfDeliveryRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.proofOfDelivery = proofOfDelivery {
                    this.counterparty = driverRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = withTimeout(5_000) { deliveriesProofOfDeliveryValidationService.previewProofOfDelivery(outbound0) }
            val configure1: ListNotificationsRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.filter = ListNotificationsRequestKt.filter {
                    this.createdAfter = reply0.outcome.effectiveAt
                    this.ownerId = reply0.proofOfDelivery.counterparty.driverId
                    this.region = reply0.proofOfDelivery.billingAddress.region
                }
            }
            val outbound1 = listNotificationsRequest(configure1)
            val reply1 = notificationsNotificationService.listNotifications(outbound1)
            val outbound2 = compareInspectionOptionsRequest {
                this.context = request.context
                this.inspection = inspection {
                    this.assignee = userRef {
                        this.displayName = reply0.proofOfDelivery.counterparty.displayName
                    }
                }
            }
            val reply2 = fleetInspectionCalculationService.compareInspectionOptions(outbound2)
            summarizeVehiclesResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.details.putAll(reply1.result.detailsMap)
                    this.message = reply0.outcome.message
                    this.status = "ok"
                }
            }
        }

    private data class BrowseVehicles0Command(
        val context: RequestContext,
        val inspectionRequesterEmail: String,
        val inspectionAssigneeUserId: String,
        val inspectionSlaDueAt: Timestamp
    ) {
        fun toRequest(): PreviewInspectionRequest {
            val command = this
            return previewInspectionRequest {
                this.context = command.context
                this.inspection = inspection {
                    this.assignee = userRef {
                        this.userId = command.inspectionAssigneeUserId
                    }
                    this.requester = contactInfo {
                        this.email = command.inspectionRequesterEmail
                    }
                    this.sla = inspectionSla {
                        this.dueAt = command.inspectionSlaDueAt
                    }
                }
            }
        }
    }

    private suspend fun handleBrowseVehicles(request: BrowseVehiclesRequest): BrowseVehiclesResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleQueryService/BrowseVehicles") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BrowseVehicles", request)
            log.atInfo().addKeyValue("operation", "BrowseVehicles").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = BrowseVehicles0Command(
                context = request.context,
                inspectionRequesterEmail = request.context.actor.email,
                inspectionAssigneeUserId = request.context.actor.userId,
                inspectionSlaDueAt = request.filter.createdAfter
            ).toRequest()
            val reply0 = fleetInspectionValidationService.previewInspection(outbound0)
            val outbound1 = previewRoutePlanRequest {
                this.context = request.context
                this.routePlan = routePlan {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.inspection.audit.updatedBy
                    }
                }
            }
            val command1: Submission<PreviewRoutePlanRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = routingRoutePlanValidationService.previewRoutePlan(payload1)
            browseVehiclesResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
                this.vehicles.addAll(listOf(vehicle {
                    this.owner = userRef {
                        this.email = reply0.inspection.assignee.email
                    }
                }))
            }
        }

    private suspend fun handleExportVehicles(request: ExportVehiclesRequest): ExportVehiclesResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleQueryService/ExportVehicles") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExportVehicles", request)
            log.atInfo().addKeyValue("operation", "ExportVehicles").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = checkVehicleEligibilityRequest {
                this.context = request.context
                this.vehicle = vehicle {
                    this.address = postalAddress {
                        this.region = request.filter.region
                    }
                    this.owner = userRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val invoke0: suspend (CheckVehicleEligibilityRequest, Metadata) -> CheckVehicleEligibilityResponse = fleetVehicleValidationService::checkVehicleEligibility
            val reply0 = invoke0(outbound0, Metadata())
            val outbound1 = CheckInspectionEligibilityRequest.newBuilder().apply {
                setContext(request.context)
                setInspection(inspection {
                    this.audit = auditInfo {
                        this.etag = reply0.vehicle.audit.etag
                        this.updatedBy = reply0.vehicle.audit.updatedBy
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> fleetInspectionValidationService.checkInspectionEligibility(value) }
            exportVehiclesResponse {
                this.result = operationResult {
                    this.status = reply1.result.status
                }
                this.vehicle = vehicle {
                    this.audit = auditInfo {
                        this.createdAt = reply0.vehicle.audit.createdAt
                    }
                    this.owner = userRef {
                        this.displayName = reply0.vehicle.owner.displayName
                    }
                }
            }
        }

    private suspend fun handleFindVehicles(request: FindVehiclesRequest): FindVehiclesResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleQueryService/FindVehicles") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "FindVehicles", request)
            log.atInfo().addKeyValue("operation", "FindVehicles").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                compareTripLogOptionsRequest {
                    this.annotations.putAll(request.filter.labelsMap)
                    this.context = request.context
                    this.details = CompareTripLogOptionsRequestKt.details {
                        this.destination = postalAddress {
                            this.region = request.filter.region
                        }
                    }
                    this.tripLog = tripLog {
                        this.audit = auditInfo {
                            this.createdAt = request.filter.createdAfter
                        }
                        this.counterparty = customerRef {
                            this.email = request.context.actor.email
                        }
                    }
                }
            }
            val reply0 = telematicsTripLogCalculationService.compareTripLogOptions(outbound0)
            val outbound1 = checkInspectionEligibilityRequest {
                this.context = request.context
                this.inspection = inspection {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.tripLog.audit.updatedBy
                    }
                    this.comments.addAll(listOf(inspectionComment {
                        this.createdAt = reply0.tripLog.audit.createdAt
                    }))
                    this.customer = vehicleGroupRef {
                        this.displayName = reply0.tripLog.counterparty.displayName
                    }
                    this.sla = inspectionSla {
                        this.dueAt = reply0.tripLog.dueAt
                    }
                }
            }
            val reply1 = ClientRegistry.lookup("acme.fleet.v1.InspectionValidationService", InspectionValidationServiceGrpcKt.InspectionValidationServiceCoroutineStub::class.java).checkInspectionEligibility(outbound1)
            findVehiclesResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = reply1.result.status
                }
                this.vehicles.addAll(listOf(vehicle {
                    this.audit = auditInfo {
                        this.createdBy = reply0.tripLog.audit.createdBy
                    }
                }))
            }
        }

    private suspend fun handlePeekVehicle(request: PeekVehicleRequest): PeekVehicleResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleQueryService/PeekVehicle") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PeekVehicle", request)
            log.atInfo().addKeyValue("operation", "PeekVehicle").log("Handling transport request")
            val outbound0 = countSearchIndexesRequest {
                this.context = request.context
                this.filter = CountSearchIndexesRequestKt.filter {
                    this.createdAfter = request.context.deadline
                    this.ownerId = request.vehicleId
                }
            }
            val reply0 = workerForPeekVehicle0.submit(outbound0)
            val outbound1 = explainAdmissionCalculationRequest {
                this.admission = admission {
                    this.correlationId = reply0.result.correlationId
                }
                this.context = request.context
                this.details = ExplainAdmissionCalculationRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply1 = clientForPeekVehicle1.explainAdmissionCalculation(outbound1)
            peekVehicleResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = reply0.result.status
                }
                this.vehicle = vehicle {
                    this.vehicleId = request.vehicleId
                }
            }
        }

    private suspend fun handleResolveVehicles(request: ResolveVehiclesRequest): ResolveVehiclesResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleQueryService/ResolveVehicles") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResolveVehicles", request)
            log.atInfo().addKeyValue("operation", "ResolveVehicles").log("Handling transport request")
            val outbound0 = explainTripLogCalculationRequest {
                this.context = request.context
                this.details = ExplainTripLogCalculationRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                    this.locale = request.context.locale
                }
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.updatedAt = request.context.deadline
                    }
                }
            }
            val reply0 = flowOf(outbound0).map { value -> telematicsTripLogCalculationService.explainTripLogCalculation(value) }.single()
            resolveVehiclesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.vehicles.addAll(listOf(vehicle {
                    this.audit = auditInfo {
                        this.createdAt = reply0.tripLog.audit.createdAt
                        this.updatedAt = reply0.tripLog.audit.updatedAt
                    }
                }))
            }
        }
}
