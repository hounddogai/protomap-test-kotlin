package com.acme.transport.telematics.api

import com.acme.authorization.v1.AccessPolicyQueryServiceGrpcKt
import com.acme.authorization.v1.ExportAccessPoliciesRequestKt
import com.acme.authorization.v1.exportAccessPoliciesRequest
import com.acme.careplans.v1.CarePlanQueryServiceGrpcKt
import com.acme.careplans.v1.FindCarePlansRequest
import com.acme.careplans.v1.FindCarePlansRequestKt
import com.acme.careplans.v1.findCarePlansRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.pageRequest
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.customerRef
import com.acme.deliveries.v1.DeliveryExceptionCalculationServiceGrpcKt
import com.acme.deliveries.v1.QuoteDeliveryExceptionRequest
import com.acme.deliveries.v1.QuoteDeliveryExceptionResponse
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.deliveryRef
import com.acme.deliveries.v1.quoteDeliveryExceptionRequest
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.PartValidationServiceGrpcKt
import com.acme.maintenance.v1.explainPartDecisionRequest
import com.acme.maintenance.v1.part
import com.acme.telematics.v1.BrowseGeoFencesRequest
import com.acme.telematics.v1.BrowseGeoFencesResponse
import com.acme.telematics.v1.CalculateGeoFenceRequest
import com.acme.telematics.v1.CalculateGeoFenceResponse
import com.acme.telematics.v1.CountGeoFencesRequest
import com.acme.telematics.v1.CountGeoFencesResponse
import com.acme.telematics.v1.CountTelemetryReadingsRequest
import com.acme.telematics.v1.CountTelemetryReadingsRequestKt
import com.acme.telematics.v1.CountTelemetryReadingsResponse
import com.acme.telematics.v1.ExportGeoFencesRequest
import com.acme.telematics.v1.ExportGeoFencesResponse
import com.acme.telematics.v1.ExportTelemetryReadingsRequestKt
import com.acme.telematics.v1.FindGeoFencesRequest
import com.acme.telematics.v1.FindGeoFencesResponse
import com.acme.telematics.v1.GeoFenceCalculationServiceGrpcKt
import com.acme.telematics.v1.GeoFenceQueryServiceGrpcKt
import com.acme.telematics.v1.GeoFenceValidationServiceGrpcKt
import com.acme.telematics.v1.LookupGeoFenceRequest
import com.acme.telematics.v1.LookupGeoFenceResponse
import com.acme.telematics.v1.PeekGeoFenceRequest
import com.acme.telematics.v1.PeekGeoFenceResponse
import com.acme.telematics.v1.QuoteGeoFenceRequest
import com.acme.telematics.v1.RecalculateTripLogsRequest
import com.acme.telematics.v1.RecalculateTripLogsRequestKt
import com.acme.telematics.v1.ResolveGeoFencesRequest
import com.acme.telematics.v1.ResolveGeoFencesResponse
import com.acme.telematics.v1.SuggestGeoFencesRequest
import com.acme.telematics.v1.SuggestGeoFencesResponse
import com.acme.telematics.v1.SummarizeGeoFencesRequest
import com.acme.telematics.v1.SummarizeGeoFencesResponse
import com.acme.telematics.v1.TelemetryReadingQueryServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingValidationServiceGrpcKt
import com.acme.telematics.v1.TripLogCalculationServiceGrpcKt
import com.acme.telematics.v1.ValidateTelemetryReadingRequest
import com.acme.telematics.v1.ValidateTelemetryReadingRequestKt
import com.acme.telematics.v1.browseGeoFencesResponse
import com.acme.telematics.v1.calculateGeoFenceRequest
import com.acme.telematics.v1.calculateTripLogRequest
import com.acme.telematics.v1.countGeoFencesResponse
import com.acme.telematics.v1.countTelemetryReadingsRequest
import com.acme.telematics.v1.exportGeoFencesResponse
import com.acme.telematics.v1.exportTelemetryReadingsRequest
import com.acme.telematics.v1.findGeoFencesResponse
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.geoFenceBlockAction
import com.acme.telematics.v1.geoFenceRef
import com.acme.telematics.v1.lookupGeoFenceResponse
import com.acme.telematics.v1.normalizeGeoFenceRequest
import com.acme.telematics.v1.peekGeoFenceResponse
import com.acme.telematics.v1.quoteGeoFenceRequest
import com.acme.telematics.v1.recalculateTripLogsRequest
import com.acme.telematics.v1.resolveGeoFencesResponse
import com.acme.telematics.v1.simulateTripLogRequest
import com.acme.telematics.v1.suggestGeoFencesResponse
import com.acme.telematics.v1.summarizeGeoFencesResponse
import com.acme.telematics.v1.telemetryReading
import com.acme.telematics.v1.tripLog
import com.acme.telematics.v1.validateTelemetryReadingRequest
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.transport.telematics.TelematicsRepository
import com.acme.yards.v1.SimulateYardMoveRequestKt
import com.acme.yards.v1.SimulateYardMoveResponse
import com.acme.yards.v1.YardMoveCalculationServiceGrpcKt
import com.acme.yards.v1.copy as yardsCopy
import com.acme.yards.v1.simulateYardMoveRequest
import com.google.protobuf.Any as ProtoAny
import com.google.protobuf.Timestamp
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.slf4j.LoggerFactory

/** Answers read-only questions about geo fences, for screens and reports. */
class GeoFenceQueryGrpcService(
    private val store: TelematicsRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyQueryService: AccessPolicyQueryServiceGrpcKt.AccessPolicyQueryServiceCoroutineStub,
    private val careplansCarePlanQueryService: CarePlanQueryServiceGrpcKt.CarePlanQueryServiceCoroutineStub,
    private val deliveriesDeliveryExceptionCalculationService: DeliveryExceptionCalculationServiceGrpcKt.DeliveryExceptionCalculationServiceCoroutineStub,
    private val maintenancePartValidationService: PartValidationServiceGrpcKt.PartValidationServiceCoroutineStub,
    private val telematicsGeoFenceCalculationService: GeoFenceCalculationServiceGrpcKt.GeoFenceCalculationServiceCoroutineStub,
    private val telematicsGeoFenceValidationService: GeoFenceValidationServiceGrpcKt.GeoFenceValidationServiceCoroutineStub,
    private val telematicsTelemetryReadingQueryService: TelemetryReadingQueryServiceGrpcKt.TelemetryReadingQueryServiceCoroutineStub,
    private val telematicsTelemetryReadingValidationService: TelemetryReadingValidationServiceGrpcKt.TelemetryReadingValidationServiceCoroutineStub,
    private val telematicsTripLogCalculationService: TripLogCalculationServiceGrpcKt.TripLogCalculationServiceCoroutineStub,
    private val yardsYardMoveCalculationService: YardMoveCalculationServiceGrpcKt.YardMoveCalculationServiceCoroutineStub,
) : GeoFenceQueryServiceGrpcKt.GeoFenceQueryServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(GeoFenceQueryGrpcService::class.java)

    private val invokeResolveGeoFences0: suspend (CalculateGeoFenceRequest) -> CalculateGeoFenceResponse = { payload ->
        telematicsGeoFenceCalculationService.calculateGeoFence(payload)
    }

    /** Lists geo fences a page at a time. */
    override suspend fun browseGeoFences(request: BrowseGeoFencesRequest): BrowseGeoFencesResponse {
        return handleBrowseGeoFences(request)
    }

    /** Counts geo fences by status. */
    override suspend fun countGeoFences(request: CountGeoFencesRequest): CountGeoFencesResponse = handleCountGeoFences(request)

    /** Streams every matching geo fence. */
    override fun exportGeoFences(request: ExportGeoFencesRequest): Flow<ExportGeoFencesResponse> = flow {
        emit(handleExportGeoFences(request))
    }

    /** Lists geo fences a page at a time. */
    override suspend fun findGeoFences(request: FindGeoFencesRequest): FindGeoFencesResponse {
        return handleFindGeoFences(request)
    }

    /** Returns a geo fence. */
    override suspend fun lookupGeoFence(request: LookupGeoFenceRequest): LookupGeoFenceResponse = handleLookupGeoFence(request)

    /** Returns a geo fence. */
    override suspend fun peekGeoFence(request: PeekGeoFenceRequest): PeekGeoFenceResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceQueryService/PeekGeoFence") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PeekGeoFence", request)
            log.atInfo().addKeyValue("operation", "PeekGeoFence").log("Handling transport request")
            val outbound0 = countTelemetryReadingsRequest {
                this.context = request.context
                this.filter = CountTelemetryReadingsRequestKt.filter {
                    this.createdAfter = request.context.deadline
                    this.labels.putAll(request.context.baggageMap)
                    this.ownerId = request.geoFenceId
                }
            }
            val adapter0 = OutboundPort<CountTelemetryReadingsRequest, CountTelemetryReadingsResponse> { payload ->
                telematicsTelemetryReadingQueryService.countTelemetryReadings(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            peekGeoFenceResponse {
                this.geoFence = geoFence {
                    this.geoFenceId = request.geoFenceId
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    /** Returns several geo fences at once. */
    override suspend fun resolveGeoFences(request: ResolveGeoFencesRequest): ResolveGeoFencesResponse {
        return handleResolveGeoFences(request)
    }

    /** Lists geo fences a page at a time. */
    override suspend fun suggestGeoFences(request: SuggestGeoFencesRequest): SuggestGeoFencesResponse = handleSuggestGeoFences(request)

    /** Summarizes a geo fence. */
    override suspend fun summarizeGeoFences(request: SummarizeGeoFencesRequest): SummarizeGeoFencesResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceQueryService/SummarizeGeoFences") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SummarizeGeoFences", request)
            log.atInfo().addKeyValue("operation", "SummarizeGeoFences").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.format).log("Recorded audit detail")
            val outbound0 = normalizeGeoFenceRequest {
                this.context = request.context
                this.geoFence = geoFence {
                    this.block = geoFenceBlockAction {
                        this.reason = request.reason
                    }
                    this.geoFenceId = request.geoFenceId
                }
            }
            val reply0 = ClientRegistry.lookup("acme.telematics.v1.GeoFenceValidationService", GeoFenceValidationServiceGrpcKt.GeoFenceValidationServiceCoroutineStub::class.java).normalizeGeoFence(outbound0)
            val outbound1 = explainPartDecisionRequest {
                this.context = request.context
                this.part = part {
                    this.audit = auditInfo {
                        this.createdBy = reply0.geoFence.audit.createdBy
                        this.etag = reply0.geoFence.audit.etag
                    }
                    this.owner = userRef {
                        this.email = reply0.geoFence.owner.email
                    }
                }
            }
            val reply1 = maintenancePartValidationService.explainPartDecision(WireCopies.reflective(outbound1))
            summarizeGeoFencesResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.message = reply0.outcome.message
                    this.status = reply1.result.status
                }
            }
        }

    private fun BrowseGeoFencesRequest.toValidateTelemetryReadingFromBrowseGeoFences1(reply0: SimulateYardMoveResponse): ValidateTelemetryReadingRequest {
        val source = this
        return validateTelemetryReadingRequest {
            this.context = source.context
            this.details = ValidateTelemetryReadingRequestKt.details {
                this.contact = contactInfo {
                    this.email = reply0.yardMove.assignee.email
                }
            }
            this.telemetryReading = telemetryReading {
                this.subject = geoFenceRef {
                    this.displayName = reply0.yardMove.assignee.displayName
                }
            }
        }
    }

    private suspend fun handleBrowseGeoFences(request: BrowseGeoFencesRequest): BrowseGeoFencesResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceQueryService/BrowseGeoFences") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BrowseGeoFences", request)
            log.atInfo().addKeyValue("operation", "BrowseGeoFences").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = simulateYardMoveRequest {}.yardsCopy {
                this.context = request.context
                this.details = SimulateYardMoveRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.filter.region
                    }
                }
            }
            val reply0 = yardsYardMoveCalculationService.simulateYardMove(outbound0)
            val outbound1 = request.toValidateTelemetryReadingFromBrowseGeoFences1(reply0)
            val reply1 = telematicsTelemetryReadingValidationService.validateTelemetryReading(outbound1)
            browseGeoFencesResponse {
                this.geoFences.addAll(listOf(geoFence {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.yardMove.audit.updatedAt
                    }
                }))
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleCountGeoFences(request: CountGeoFencesRequest): CountGeoFencesResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceQueryService/CountGeoFences") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CountGeoFences", request)
            log.atInfo().addKeyValue("operation", "CountGeoFences").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = exportTelemetryReadingsRequest {
                this.context = request.context
                this.filter = ExportTelemetryReadingsRequestKt.filter {
                    this.region = request.filter.region
                }
            }
            telematicsTelemetryReadingQueryService.exportTelemetryReadings(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = findCarePlansRequest {
                this.context = request.context
                this.filter = FindCarePlansRequestKt.filter {
                    this.createdAfter = request.filter.createdAfter
                    this.ownerId = request.filter.ownerId
                }
                this.page = pageRequest {
                    this.pageToken = request.page.pageToken
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = careplansCarePlanQueryService.findCarePlans(envelope1.unpack(FindCarePlansRequest::class.java))
            countGeoFencesResponse {
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = reply1.result.status
                }
            }
        }

    private data class ExportGeoFences1Command(
        val context: RequestContext,
        val geoFenceAuditUpdatedBy: String,
        val geoFenceOwnerDisplayName: String,
        val geoFenceAuditUpdatedAt: Timestamp,
        val geoFenceAuditEtag: String
    ) {
        fun toRequest(): QuoteGeoFenceRequest {
            val command = this
            return quoteGeoFenceRequest {
                this.context = command.context
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.etag = command.geoFenceAuditEtag
                        this.updatedAt = command.geoFenceAuditUpdatedAt
                        this.updatedBy = command.geoFenceAuditUpdatedBy
                    }
                    this.owner = userRef {
                        this.displayName = command.geoFenceOwnerDisplayName
                    }
                }
            }
        }
    }

    private suspend fun handleExportGeoFences(request: ExportGeoFencesRequest): ExportGeoFencesResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceQueryService/ExportGeoFences") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExportGeoFences", request)
            log.atInfo().addKeyValue("operation", "ExportGeoFences").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = simulateTripLogRequest {
                this.context = request.context
                this.tripLog = tripLog {
                    this.billingAddress = postalAddress {
                        this.region = request.filter.region
                    }
                    this.counterparty = customerRef {
                        this.email = request.context.actor.email
                    }
                    this.dueAt = request.filter.createdAfter
                    this.metadata.putAll(request.filter.labelsMap)
                }
            }
            val reply0 = telematicsTripLogCalculationService.simulateTripLog(WireCopies.json(outbound0))
            val outbound1 = ExportGeoFences1Command(
                context = request.context,
                geoFenceAuditUpdatedBy = reply0.tripLog.audit.updatedBy,
                geoFenceOwnerDisplayName = reply0.tripLog.counterparty.displayName,
                geoFenceAuditUpdatedAt = reply0.tripLog.audit.updatedAt,
                geoFenceAuditEtag = reply0.tripLog.audit.etag
            ).toRequest()
            val reply1 = telematicsGeoFenceCalculationService.quoteGeoFence(outbound1)
            exportGeoFencesResponse {
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.tripLog.audit.updatedBy
                    }
                    this.effective = reply1.geoFence.effective
                    this.owner = userRef {
                        this.userId = reply1.geoFence.owner.userId
                    }
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleFindGeoFences(request: FindGeoFencesRequest): FindGeoFencesResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceQueryService/FindGeoFences") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "FindGeoFences", request)
            log.atInfo().addKeyValue("operation", "FindGeoFences").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            findGeoFencesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleLookupGeoFence(request: LookupGeoFenceRequest): LookupGeoFenceResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceQueryService/LookupGeoFence") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LookupGeoFence", request)
            log.atInfo().addKeyValue("operation", "LookupGeoFence").log("Handling transport request")
            val outbound0 = exportAccessPoliciesRequest {
                this.context = request.context
                this.filter = ExportAccessPoliciesRequestKt.filter {
                    this.createdAfter = request.context.deadline
                    this.labels.putAll(request.context.baggageMap)
                    this.ownerId = request.geoFenceId
                }
            }
            authorizationAccessPolicyQueryService.exportAccessPolicies(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            lookupGeoFenceResponse {
                this.geoFence = geoFence {
                    this.geoFenceId = request.geoFenceId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleResolveGeoFences(request: ResolveGeoFencesRequest): ResolveGeoFencesResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceQueryService/ResolveGeoFences") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResolveGeoFences", request)
            log.atInfo().addKeyValue("operation", "ResolveGeoFences").log("Handling transport request")
            val outbound0 = calculateGeoFenceRequest {
                this.context = request.context
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.createdAt = request.context.deadline
                    }
                    this.owner = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply0 = invokeResolveGeoFences0(outbound0)
            val outbound1 = quoteDeliveryExceptionRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.audit = auditInfo {
                        this.createdAt = reply0.geoFence.audit.createdAt
                    }
                    this.customer = deliveryRef {
                        this.displayName = reply0.geoFence.owner.displayName
                    }
                }
            }
            val invoke1: suspend (QuoteDeliveryExceptionRequest, Metadata) -> QuoteDeliveryExceptionResponse = deliveriesDeliveryExceptionCalculationService::quoteDeliveryException
            val reply1 = invoke1(outbound1, Metadata())
            resolveGeoFencesResponse {
                this.geoFences.addAll(listOf(geoFence {
                    this.name = reply0.geoFence.name
                    this.owner = userRef {
                        this.email = reply1.deliveryException.requester.email
                        this.userId = reply0.geoFence.owner.userId
                    }
                }))
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSuggestGeoFences(request: SuggestGeoFencesRequest): SuggestGeoFencesResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceQueryService/SuggestGeoFences") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SuggestGeoFences", request)
            log.atInfo().addKeyValue("operation", "SuggestGeoFences").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = RecalculateTripLogsRequest.newBuilder().apply {
                setContext(request.context)
                setDetails(RecalculateTripLogsRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.filter.region
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> telematicsTripLogCalculationService.recalculateTripLogs(value) }
            val outbound1 = RequestFactory.create {
                calculateTripLogRequest {
                    this.context = request.context
                    this.tripLog = tripLog {
                        this.audit = auditInfo {
                            this.etag = reply0.tripLog.audit.etag
                            this.updatedBy = reply0.tripLog.audit.updatedBy
                        }
                    }
                }
            }
            val reply1 = telematicsTripLogCalculationService.calculateTripLog(outbound1)
            suggestGeoFencesResponse {
                this.geoFences.addAll(listOf(geoFence {
                    this.audit = auditInfo {
                        this.createdBy = reply0.tripLog.audit.createdBy
                        this.etag = reply0.tripLog.audit.etag
                    }
                }))
                this.result = operationResult {
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }
}
