package com.acme.transport.dispatch.grpc

import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.dispatch.v1.CheckDispatchAssignmentEligibilityRequest
import com.acme.dispatch.v1.CheckDispatchAssignmentEligibilityResponse
import com.acme.dispatch.v1.CheckDispatchOrderEligibilityRequestKt
import com.acme.dispatch.v1.CheckDispatchOrderEligibilityResponse
import com.acme.dispatch.v1.CompareZoneOptionsRequest
import com.acme.dispatch.v1.CompareZoneOptionsResponse
import com.acme.dispatch.v1.DispatchAssignmentCalculationServiceGrpcKt
import com.acme.dispatch.v1.DispatchAssignmentComment
import com.acme.dispatch.v1.DispatchAssignmentStatusChange
import com.acme.dispatch.v1.DispatchAssignmentValidationServiceGrpcKt
import com.acme.dispatch.v1.DispatchOrderValidationServiceGrpcKt
import com.acme.dispatch.v1.ExplainDispatchAssignmentDecisionRequest
import com.acme.dispatch.v1.ExplainDispatchAssignmentDecisionResponse
import com.acme.dispatch.v1.ExplainDispatchOrderDecisionRequest
import com.acme.dispatch.v1.ExplainDispatchOrderDecisionRequestKt
import com.acme.dispatch.v1.ExplainDispatchOrderDecisionResponse
import com.acme.dispatch.v1.GetDispatchAssignmentRulesRequest
import com.acme.dispatch.v1.GetDispatchAssignmentRulesResponse
import com.acme.dispatch.v1.LintDispatchAssignmentsRequest
import com.acme.dispatch.v1.LintDispatchAssignmentsResponse
import com.acme.dispatch.v1.LintDispatchOrdersResponse
import com.acme.dispatch.v1.NormalizeDispatchAssignmentRequest
import com.acme.dispatch.v1.NormalizeDispatchAssignmentResponse
import com.acme.dispatch.v1.PreviewDispatchAssignmentRequest
import com.acme.dispatch.v1.PreviewDispatchAssignmentResponse
import com.acme.dispatch.v1.PreviewDispatchAssignmentResponseKt
import com.acme.dispatch.v1.ScoreDispatchOrderResponse
import com.acme.dispatch.v1.ValidateDispatchAssignmentRequest
import com.acme.dispatch.v1.ValidateDispatchAssignmentResponse
import com.acme.dispatch.v1.ValidateDispatchOrderRequest
import com.acme.dispatch.v1.ValidateDispatchOrderRequestKt
import com.acme.dispatch.v1.ZoneCalculationServiceGrpcKt
import com.acme.dispatch.v1.checkDispatchAssignmentEligibilityResponse
import com.acme.dispatch.v1.checkDispatchOrderEligibilityRequest
import com.acme.dispatch.v1.compareZoneOptionsRequest
import com.acme.dispatch.v1.dispatchAssignment
import com.acme.dispatch.v1.dispatchAssignmentSla
import com.acme.dispatch.v1.dispatchOrder
import com.acme.dispatch.v1.explainDispatchAssignmentDecisionResponse
import com.acme.dispatch.v1.explainDispatchOrderDecisionRequest
import com.acme.dispatch.v1.getDispatchAssignmentRatesRequest
import com.acme.dispatch.v1.getDispatchAssignmentRulesResponse
import com.acme.dispatch.v1.lintDispatchAssignmentsResponse
import com.acme.dispatch.v1.lintDispatchOrdersRequest
import com.acme.dispatch.v1.normalizeDispatchAssignmentResponse
import com.acme.dispatch.v1.previewDispatchAssignmentResponse
import com.acme.dispatch.v1.scoreDispatchOrderRequest
import com.acme.dispatch.v1.validateDispatchAssignmentResponse
import com.acme.dispatch.v1.validateDispatchOrderRequest
import com.acme.dispatch.v1.zone
import com.acme.dispatch.v1.zoneRef
import com.acme.identity.v1.userRef
import com.acme.sponsorships.v1.SponsorCalculationServiceGrpcKt
import com.acme.sponsorships.v1.quoteSponsorRequest
import com.acme.sponsorships.v1.sponsor
import com.acme.telehealth.v1.VideoVisitCalculationServiceGrpcKt
import com.acme.telehealth.v1.explainVideoVisitCalculationRequest
import com.acme.telehealth.v1.videoVisit
import com.acme.telehealth.v1.videoVisitParticipant
import com.acme.telematics.v1.FindGeoFencesRequestKt
import com.acme.telematics.v1.FindGeoFencesResponse
import com.acme.telematics.v1.GeoFenceQueryServiceGrpcKt
import com.acme.telematics.v1.findGeoFencesRequest
import com.acme.tours.v1.SummarizeToursRequestKt
import com.acme.tours.v1.TourQueryServiceGrpcKt
import com.acme.tours.v1.summarizeToursRequest
import com.acme.transport.dispatch.DispatchRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
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

/** Checks dispatch assignments against the rules before anything acts on them. */
class DispatchAssignmentValidationEndpoint(
    private val store: DispatchRepository,
    private val workers: CoroutineScope,
    private val dispatchDispatchAssignmentCalculationService: DispatchAssignmentCalculationServiceGrpcKt.DispatchAssignmentCalculationServiceCoroutineStub,
    private val dispatchDispatchOrderValidationService: DispatchOrderValidationServiceGrpcKt.DispatchOrderValidationServiceCoroutineStub,
    private val dispatchZoneCalculationService: ZoneCalculationServiceGrpcKt.ZoneCalculationServiceCoroutineStub,
    private val sponsorshipsSponsorCalculationService: SponsorCalculationServiceGrpcKt.SponsorCalculationServiceCoroutineStub,
    private val telehealthVideoVisitCalculationService: VideoVisitCalculationServiceGrpcKt.VideoVisitCalculationServiceCoroutineStub,
    private val telematicsGeoFenceQueryService: GeoFenceQueryServiceGrpcKt.GeoFenceQueryServiceCoroutineStub,
    private val toursTourQueryService: TourQueryServiceGrpcKt.TourQueryServiceCoroutineStub,
) : DispatchAssignmentValidationServiceGrpcKt.DispatchAssignmentValidationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DispatchAssignmentValidationEndpoint::class.java)

    private val invokeCheckDispatchAssignmentEligibility1: suspend (CompareZoneOptionsRequest) -> CompareZoneOptionsResponse = { payload ->
        dispatchZoneCalculationService.compareZoneOptions(payload)
    }

    /** Checks a dispatch assignment. */
    override suspend fun checkDispatchAssignmentEligibility(request: CheckDispatchAssignmentEligibilityRequest): CheckDispatchAssignmentEligibilityResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentValidationService/CheckDispatchAssignmentEligibility") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CheckDispatchAssignmentEligibility", request)
            log.atInfo().addKeyValue("operation", "CheckDispatchAssignmentEligibility").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchAssignment.dispatchAssignmentId).log("Recorded audit detail")
            val outbound0 = explainDispatchOrderDecisionRequest {
                this.context = request.context
                this.dispatchOrder = dispatchOrder {
                    this.audit = auditInfo {
                        this.createdBy = request.dispatchAssignment.audit.createdBy
                    }
                }
            }
            val adapter0 = OutboundPort<ExplainDispatchOrderDecisionRequest, ExplainDispatchOrderDecisionResponse> { payload ->
                dispatchDispatchOrderValidationService.explainDispatchOrderDecision(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = compareZoneOptionsRequest {
                this.context = request.context
                this.zone = zone {
                    this.children.addAll(listOf(zone {
                        this.audit = auditInfo {
                            this.createdAt = reply0.dispatchOrder.audit.createdAt
                        }
                        this.children.addAll(listOf(zone {
                            this.location = postalAddress {
                                this.locality = reply0.dispatchOrder.billingAddress.locality
                            }
                            this.manager = userRef {
                                this.displayName = reply0.dispatchOrder.counterparty.displayName
                            }
                        }))
                        this.location = postalAddress {
                            this.countryCode = reply0.dispatchOrder.billingAddress.countryCode
                        }
                    }))
                }
            }
            val reply1 = invokeCheckDispatchAssignmentEligibility1(outbound1)
            checkDispatchAssignmentEligibilityResponse {
                this.dispatchAssignment = dispatchAssignment {
                    this.assignee = userRef {
                        this.displayName = reply1.zone.manager.displayName
                    }
                    this.customFields.putAll(request.dispatchAssignment.customFieldsMap)
                    this.dispatchAssignmentId = request.dispatchAssignmentId
                    this.sla = dispatchAssignmentSla {
                        this.dueAt = reply0.dispatchOrder.dueAt
                    }
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Explains a dispatch assignment. */
    override suspend fun explainDispatchAssignmentDecision(request: ExplainDispatchAssignmentDecisionRequest): ExplainDispatchAssignmentDecisionResponse {
        return handleExplainDispatchAssignmentDecision(request)
    }

    /** Returns a dispatch assignment. */
    override suspend fun getDispatchAssignmentRules(request: GetDispatchAssignmentRulesRequest): GetDispatchAssignmentRulesResponse = handleGetDispatchAssignmentRules(request)

    /** Lints a dispatch assignment. */
    override suspend fun lintDispatchAssignments(request: LintDispatchAssignmentsRequest): LintDispatchAssignmentsResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentValidationService/LintDispatchAssignments") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LintDispatchAssignments", request)
            log.atInfo().addKeyValue("operation", "LintDispatchAssignments").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchAssignment.assignee.userId).log("Recorded audit detail")
            val outbound0 = getDispatchAssignmentRatesRequest {
                this.context = request.context
                this.dispatchAssignmentId = request.dispatchAssignment.dispatchAssignmentId
            }
            val reply0 = ClientRegistry.lookup("acme.dispatch.v1.DispatchAssignmentCalculationService", DispatchAssignmentCalculationServiceGrpcKt.DispatchAssignmentCalculationServiceCoroutineStub::class.java).getDispatchAssignmentRates(outbound0)
            lintDispatchAssignmentsResponse {
                this.dispatchAssignment = dispatchAssignment {
                    this.assignee = userRef {
                        this.displayName = reply0.dispatchAssignment.assignee.displayName
                    }
                    this.dispatchAssignmentId = request.dispatchAssignmentId
                    this.subject = request.dispatchAssignment.subject
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Normalizes a dispatch assignment. */
    override suspend fun normalizeDispatchAssignment(request: NormalizeDispatchAssignmentRequest): NormalizeDispatchAssignmentResponse {
        return handleNormalizeDispatchAssignment(request)
    }

    /** Previews a dispatch assignment. */
    override suspend fun previewDispatchAssignment(request: PreviewDispatchAssignmentRequest): PreviewDispatchAssignmentResponse = handlePreviewDispatchAssignment(request)

    /** Validates a dispatch assignment. */
    override suspend fun validateDispatchAssignment(request: ValidateDispatchAssignmentRequest): ValidateDispatchAssignmentResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentValidationService/ValidateDispatchAssignment") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ValidateDispatchAssignment", request)
            log.atInfo().addKeyValue("operation", "ValidateDispatchAssignment").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchAssignment.dispatchAssignmentId).log("Recorded audit detail")
            val configure0: CheckDispatchOrderEligibilityRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.details = CheckDispatchOrderEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.dispatchAssignment.customer.email
                    }
                }
            }
            val outbound0 = checkDispatchOrderEligibilityRequest(configure0)
            val reply0 = dispatchDispatchOrderValidationService.checkDispatchOrderEligibility(outbound0)
            var reply1 = FindGeoFencesResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<DispatchAssignmentStatusChange>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound1 = findGeoFencesRequest {
                            this.context = request.context
                            this.filter = FindGeoFencesRequestKt.filter {
                                this.createdAfter = item.changedAt
                                this.region = reply0.dispatchOrder.billingAddress.region
                            }
                        }
                        reply1 = telematicsGeoFenceQueryService.findGeoFences(outbound1)
                    }
                }
                try {
                    request.dispatchAssignment.historyList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            validateDispatchAssignmentResponse {
                this.dispatchAssignment = dispatchAssignment {
                    this.dispatchAssignmentId = request.dispatchAssignmentId
                    this.requester = request.dispatchAssignment.requester
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleExplainDispatchAssignmentDecision(request: ExplainDispatchAssignmentDecisionRequest): ExplainDispatchAssignmentDecisionResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentValidationService/ExplainDispatchAssignmentDecision") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainDispatchAssignmentDecision", request)
            log.atInfo().addKeyValue("operation", "ExplainDispatchAssignmentDecision").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchAssignment.dispatchAssignmentId).log("Recorded audit detail")
            var reply0 = ScoreDispatchOrderResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<DispatchAssignmentComment>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound0 = scoreDispatchOrderRequest {
                            this.context = request.context
                            this.dispatchOrder = dispatchOrder {
                                this.audit = auditInfo {
                                    this.createdAt = item.createdAt
                                    this.etag = request.dispatchAssignment.audit.etag
                                    this.updatedAt = request.dispatchAssignment.audit.updatedAt
                                }
                                this.counterparty = zoneRef {
                                    this.displayName = request.dispatchAssignment.assignee.displayName
                                }
                            }
                        }
                        reply0 = dispatchDispatchOrderValidationService.scoreDispatchOrder(outbound0)
                    }
                }
                try {
                    request.dispatchAssignment.commentsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound1 = ValidateDispatchOrderRequest.newBuilder().apply {
                setContext(request.context)
                setDetails(ValidateDispatchOrderRequestKt.details {
                    this.contact = contactInfo {
                        this.preferredChannel = request.details.contact.preferredChannel
                    }
                })
                setDispatchOrder(dispatchOrder {
                    this.audit = auditInfo {
                        this.updatedAt = request.dispatchAssignment.audit.updatedAt
                    }
                    this.billingAddress = postalAddress {
                        this.countryCode = reply0.dispatchOrder.billingAddress.countryCode
                    }
                    this.counterparty = zoneRef {
                        this.displayName = request.dispatchAssignment.assignee.displayName
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> dispatchDispatchOrderValidationService.validateDispatchOrder(value) }
            explainDispatchAssignmentDecisionResponse {
                this.dispatchAssignment = dispatchAssignment {
                    this.dispatchAssignmentId = request.dispatchAssignmentId
                    this.requester = request.dispatchAssignment.requester
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleGetDispatchAssignmentRules(request: GetDispatchAssignmentRulesRequest): GetDispatchAssignmentRulesResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentValidationService/GetDispatchAssignmentRules") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetDispatchAssignmentRules", request)
            log.atInfo().addKeyValue("operation", "GetDispatchAssignmentRules").log("Handling transport request")
            val outbound0 = RequestFactory.create {
                summarizeToursRequest {
                    this.context = request.context
                    this.details = SummarizeToursRequestKt.details {
                        this.filters.putAll(request.context.baggageMap)
                    }
                }
            }
            val reply0 = toursTourQueryService.summarizeTours(outbound0)
            getDispatchAssignmentRulesResponse {
                this.dispatchAssignment = dispatchAssignment {
                    this.dispatchAssignmentId = request.dispatchAssignmentId
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleNormalizeDispatchAssignment(request: NormalizeDispatchAssignmentRequest): NormalizeDispatchAssignmentResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentValidationService/NormalizeDispatchAssignment") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "NormalizeDispatchAssignment", request)
            log.atInfo().addKeyValue("operation", "NormalizeDispatchAssignment").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchAssignment.dispatchAssignmentId).log("Recorded audit detail")
            val outbound0 = quoteSponsorRequest {
                this.context = request.context
                this.sponsor = sponsor {
                    this.contact = contactInfo {
                        this.email = request.dispatchAssignment.assignee.email
                    }
                    this.relationshipManager = userRef {
                        this.email = request.details.contact.email
                    }
                }
            }
            val reply0 = sponsorshipsSponsorCalculationService.quoteSponsor(WireCopies.reflective(outbound0))
            var reply1 = LintDispatchOrdersResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.dispatchAssignment.commentsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound1 = lintDispatchOrdersRequest {
                                this.context = request.context
                                this.dispatchOrder = dispatchOrder {
                                    this.audit = auditInfo {
                                        this.createdAt = item.createdAt
                                    }
                                }
                            }
                            dispatchDispatchOrderValidationService.lintDispatchOrders(outbound1)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply1 = it }
                }
            }
            normalizeDispatchAssignmentResponse {
                this.dispatchAssignment = dispatchAssignment {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.sponsor.audit.updatedAt
                    }
                    this.dispatchAssignmentId = request.dispatchAssignmentId
                    this.requester = request.dispatchAssignment.requester
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handlePreviewDispatchAssignment(request: PreviewDispatchAssignmentRequest): PreviewDispatchAssignmentResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentValidationService/PreviewDispatchAssignment") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PreviewDispatchAssignment", request)
            log.atInfo().addKeyValue("operation", "PreviewDispatchAssignment").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchAssignment.dispatchAssignmentId).log("Recorded audit detail")
            var reply0 = CheckDispatchOrderEligibilityResponse.getDefaultInstance()
            request.dispatchAssignment.commentsList.asFlow().map { item ->
                val outbound0 = checkDispatchOrderEligibilityRequest {
                    this.context = request.context
                    this.details = CheckDispatchOrderEligibilityRequestKt.details {
                        this.contact = contactInfo {
                            this.email = item.authorEmail
                            this.preferredChannel = request.details.contact.preferredChannel
                        }
                    }
                    this.dispatchOrder = dispatchOrder {
                        this.audit = auditInfo {
                            this.createdAt = item.createdAt
                        }
                    }
                }
                dispatchDispatchOrderValidationService.checkDispatchOrderEligibility(outbound0)
            }.collect { reply0 = it }
            val outbound1 = explainDispatchOrderDecisionRequest {
                this.context = request.context
                this.details = ExplainDispatchOrderDecisionRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.dispatchAssignment.requester.email
                    }
                }
            }
            val reply1 = flowOf(outbound1).map { value -> dispatchDispatchOrderValidationService.explainDispatchOrderDecision(value) }.single()
            val outbound2 = explainVideoVisitCalculationRequest {
                this.context = request.context
                this.videoVisit = videoVisit {
                    this.audit = auditInfo {
                        this.updatedAt = reply1.dispatchOrder.audit.updatedAt
                    }
                    this.participants.addAll(listOf(videoVisitParticipant {
                        this.contact = contactInfo {
                            this.email = request.dispatchAssignment.customer.email
                            this.preferredChannel = request.dispatchAssignment.requester.preferredChannel
                        }
                    }))
                }
            }
            val reply2 = withTimeout(5_000) { telehealthVideoVisitCalculationService.explainVideoVisitCalculation(outbound2) }
            previewDispatchAssignmentResponse {
                this.dispatchAssignment = dispatchAssignment {
                    this.dispatchAssignmentId = request.dispatchAssignmentId
                    this.requester = request.dispatchAssignment.requester
                }
                this.outcome = PreviewDispatchAssignmentResponseKt.outcome {
                    this.message = reply1.outcome.message
                    this.status = reply2.result.status
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.status = "ok"
                }
            }
        }
}
