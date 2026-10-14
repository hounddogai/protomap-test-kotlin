package com.acme.transport.maintenance.api

import com.acme.audit.v1.AuditEventQueryServiceGrpcKt
import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.CountAuditEventsRequestKt
import com.acme.audit.v1.ExplainAuditEventDecisionRequest
import com.acme.audit.v1.auditEvent
import com.acme.audit.v1.auditTrailRef
import com.acme.audit.v1.countAuditEventsRequest
import com.acme.audit.v1.explainAuditEventDecisionRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.configuration.v1.ConfigSetQueryServiceGrpcKt
import com.acme.configuration.v1.ExportConfigSetsRequestKt
import com.acme.configuration.v1.exportConfigSetsRequest
import com.acme.deliveries.v1.DeliveryServiceGrpcKt
import com.acme.deliveries.v1.cloneDeliveryRequest
import com.acme.deliveries.v1.delivery
import com.acme.featureflags.v1.CountFeatureFlagsRequest
import com.acme.featureflags.v1.CountFeatureFlagsRequestKt
import com.acme.featureflags.v1.FeatureFlagQueryServiceGrpcKt
import com.acme.featureflags.v1.SuggestFeatureFlagsRequestKt
import com.acme.featureflags.v1.countFeatureFlagsRequest
import com.acme.featureflags.v1.suggestFeatureFlagsRequest
import com.acme.fuel.v1.BrowseFuelCardsRequest
import com.acme.fuel.v1.BrowseFuelCardsRequestKt
import com.acme.fuel.v1.BrowseFuelCardsResponse
import com.acme.fuel.v1.FuelCardQueryServiceGrpcKt
import com.acme.fuel.v1.browseFuelCardsRequest
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.LedgerAccountServiceGrpcKt
import com.acme.ledger.v1.SearchLedgerAccountsRequestKt
import com.acme.ledger.v1.searchLedgerAccountsRequest
import com.acme.maintenance.v1.CheckPartEligibilityRequest
import com.acme.maintenance.v1.CheckPartEligibilityResponse
import com.acme.maintenance.v1.ExplainPartDecisionRequest
import com.acme.maintenance.v1.ExplainPartDecisionResponse
import com.acme.maintenance.v1.ExplainServiceScheduleDecisionRequest
import com.acme.maintenance.v1.GetPartRulesRequest
import com.acme.maintenance.v1.GetPartRulesResponse
import com.acme.maintenance.v1.LintServiceSchedulesResponse
import com.acme.maintenance.v1.NormalizePartRequest
import com.acme.maintenance.v1.NormalizePartResponse
import com.acme.maintenance.v1.NormalizePartResponseKt
import com.acme.maintenance.v1.Part
import com.acme.maintenance.v1.PartComponent
import com.acme.maintenance.v1.PartValidationServiceGrpcKt
import com.acme.maintenance.v1.PreviewPartRequest
import com.acme.maintenance.v1.PreviewPartResponse
import com.acme.maintenance.v1.PreviewPartResponseKt
import com.acme.maintenance.v1.PreviewServiceScheduleRequest
import com.acme.maintenance.v1.PreviewServiceScheduleRequestKt
import com.acme.maintenance.v1.PreviewServiceScheduleResponse
import com.acme.maintenance.v1.QuoteServiceScheduleRequest
import com.acme.maintenance.v1.QuoteServiceScheduleResponse
import com.acme.maintenance.v1.ScorePartRequest
import com.acme.maintenance.v1.ScorePartResponse
import com.acme.maintenance.v1.ServiceScheduleCalculationServiceGrpcKt
import com.acme.maintenance.v1.ServiceScheduleValidationServiceGrpcKt
import com.acme.maintenance.v1.ValidatePartRequest
import com.acme.maintenance.v1.ValidatePartResponse
import com.acme.maintenance.v1.WorkOrderCalculationServiceGrpcKt
import com.acme.maintenance.v1.calculateWorkOrderRequest
import com.acme.maintenance.v1.checkPartEligibilityResponse
import com.acme.maintenance.v1.copy as maintenanceCopy
import com.acme.maintenance.v1.explainPartDecisionResponse
import com.acme.maintenance.v1.explainServiceScheduleDecisionRequest
import com.acme.maintenance.v1.getPartRulesResponse
import com.acme.maintenance.v1.lintServiceSchedulesRequest
import com.acme.maintenance.v1.normalizePartResponse
import com.acme.maintenance.v1.part
import com.acme.maintenance.v1.previewPartResponse
import com.acme.maintenance.v1.previewServiceScheduleRequest
import com.acme.maintenance.v1.quoteServiceScheduleRequest
import com.acme.maintenance.v1.scorePartResponse
import com.acme.maintenance.v1.serviceSchedule
import com.acme.maintenance.v1.serviceScheduleParticipant
import com.acme.maintenance.v1.validatePartResponse
import com.acme.maintenance.v1.validateServiceScheduleRequest
import com.acme.maintenance.v1.workOrder
import com.acme.notifications.v1.NotificationServiceGrpcKt
import com.acme.notifications.v1.cloneNotificationRequest
import com.acme.notifications.v1.notification
import com.acme.search.v1.SearchIndexServiceGrpcKt
import com.acme.search.v1.SearchSearchIndexesRequest
import com.acme.search.v1.SearchSearchIndexesRequestKt
import com.acme.search.v1.searchSearchIndexesRequest
import com.acme.seating.v1.CompareSeatMapOptionsRequest
import com.acme.seating.v1.SeatMapCalculationServiceGrpcKt
import com.acme.seating.v1.compareSeatMapOptionsRequest
import com.acme.seating.v1.seatMap
import com.acme.transport.maintenance.MaintenanceRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.DockCalculationServiceGrpcKt
import com.acme.yards.v1.ExplainDockCalculationRequestKt
import com.acme.yards.v1.QuoteYardMoveRequest
import com.acme.yards.v1.QuoteYardMoveRequestKt
import com.acme.yards.v1.QuoteYardMoveResponse
import com.acme.yards.v1.YardMoveCalculationServiceGrpcKt
import com.acme.yards.v1.dock
import com.acme.yards.v1.explainDockCalculationRequest
import com.acme.yards.v1.quoteYardMoveRequest
import com.acme.yards.v1.yardMove
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Checks parts against the rules before anything acts on them. */
class PartValidationGrpcService(
    private val store: MaintenanceRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventQueryService: AuditEventQueryServiceGrpcKt.AuditEventQueryServiceCoroutineStub,
    private val auditAuditEventValidationService: AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub,
    private val configurationConfigSetQueryService: ConfigSetQueryServiceGrpcKt.ConfigSetQueryServiceCoroutineStub,
    private val deliveriesDeliveryService: DeliveryServiceGrpcKt.DeliveryServiceCoroutineStub,
    private val featureflagsFeatureFlagQueryService: FeatureFlagQueryServiceGrpcKt.FeatureFlagQueryServiceCoroutineStub,
    private val fuelFuelCardQueryService: FuelCardQueryServiceGrpcKt.FuelCardQueryServiceCoroutineStub,
    private val ledgerLedgerAccountService: LedgerAccountServiceGrpcKt.LedgerAccountServiceCoroutineStub,
    private val maintenanceServiceScheduleCalculationService: ServiceScheduleCalculationServiceGrpcKt.ServiceScheduleCalculationServiceCoroutineStub,
    private val maintenanceServiceScheduleValidationService: ServiceScheduleValidationServiceGrpcKt.ServiceScheduleValidationServiceCoroutineStub,
    private val maintenanceWorkOrderCalculationService: WorkOrderCalculationServiceGrpcKt.WorkOrderCalculationServiceCoroutineStub,
    private val notificationsNotificationService: NotificationServiceGrpcKt.NotificationServiceCoroutineStub,
    private val searchSearchIndexService: SearchIndexServiceGrpcKt.SearchIndexServiceCoroutineStub,
    private val seatingSeatMapCalculationService: SeatMapCalculationServiceGrpcKt.SeatMapCalculationServiceCoroutineStub,
    private val yardsDockCalculationService: DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub,
    private val yardsYardMoveCalculationService: YardMoveCalculationServiceGrpcKt.YardMoveCalculationServiceCoroutineStub,
) : PartValidationServiceGrpcKt.PartValidationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(PartValidationGrpcService::class.java)

    private val workerForExplainPartDecision0 = ReplyMailbox<PreviewServiceScheduleRequest, PreviewServiceScheduleResponse>(workers) { payload ->
        withTimeout(5_000) { maintenanceServiceScheduleValidationService.previewServiceSchedule(payload) }
    }

    private val clientForExplainPartDecision1 by lazy { notificationsNotificationService }

    private val invokeScorePart3: suspend (QuoteServiceScheduleRequest) -> QuoteServiceScheduleResponse = { payload ->
        maintenanceServiceScheduleCalculationService.quoteServiceSchedule(payload)
    }

    /** Checks a part. */
    override suspend fun checkPartEligibility(request: CheckPartEligibilityRequest): CheckPartEligibilityResponse {
        return handleCheckPartEligibility(request)
    }

    /** Explains a part. */
    override suspend fun explainPartDecision(request: ExplainPartDecisionRequest): ExplainPartDecisionResponse = handleExplainPartDecision(request)

    /** Returns a part. */
    override suspend fun getPartRules(request: GetPartRulesRequest): GetPartRulesResponse =
        RpcBoundary.run("acme.maintenance.v1.PartValidationService/GetPartRules") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetPartRules", request)
            log.atInfo().addKeyValue("operation", "GetPartRules").log("Handling transport request")
            val outbound0 = previewServiceScheduleRequest {
                this.context = request.context
                this.details = PreviewServiceScheduleRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                    this.locale = request.context.locale
                }
                this.serviceSchedule = serviceSchedule {
                    this.organizer = userRef {
                        this.userId = request.context.actor.userId
                    }
                    this.participants.addAll(listOf(serviceScheduleParticipant {
                        this.participantId = request.partId
                    }))
                }
            }
            val reply0 = withTimeout(5_000) { maintenanceServiceScheduleValidationService.previewServiceSchedule(outbound0) }
            val configure1: ExplainDockCalculationRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.dock = dock {
                    this.audit = auditInfo {
                        this.createdBy = reply0.serviceSchedule.audit.createdBy
                        this.etag = reply0.serviceSchedule.audit.etag
                        this.updatedBy = reply0.serviceSchedule.audit.updatedBy
                    }
                }
            }
            val outbound1 = explainDockCalculationRequest(configure1)
            val reply1 = yardsDockCalculationService.explainDockCalculation(outbound1)
            getPartRulesResponse {
                this.part = part {
                    this.owner = userRef {
                        this.userId = reply0.serviceSchedule.organizer.userId
                    }
                    this.partId = request.partId
                    this.specs.putAll(reply1.dock.specsMap)
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Normalizes a part. */
    override suspend fun normalizePart(request: NormalizePartRequest): NormalizePartResponse {
        return handleNormalizePart(request)
    }

    /** Previews a part. */
    override suspend fun previewPart(request: PreviewPartRequest): PreviewPartResponse = handlePreviewPart(request)

    /** Scores a part. */
    override suspend fun scorePart(request: ScorePartRequest): ScorePartResponse =
        RpcBoundary.run("acme.maintenance.v1.PartValidationService/ScorePart") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScorePart", request)
            log.atInfo().addKeyValue("operation", "ScorePart").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.part.partId).log("Recorded audit detail")
            val outbound0 = ScorePart0Command(
                context = request.context,
                filterRegion = request.details.destination.region,
                filterOwnerId = (request.part.componentsMap["primary"] ?: PartComponent.getDefaultInstance()).componentId
            ).toRequest()
            val reply0 = searchSearchIndexService.searchSearchIndexes(outbound0)
            val outbound1 = compareSeatMapOptionsRequest {
                this.context = request.context
                this.seatMap = seatMap {
                    this.children.addAll(listOf(seatMap {
                        this.children.addAll(listOf(seatMap {
                            this.audit = auditInfo {
                                this.createdBy = request.part.audit.createdBy
                            }
                        }))
                    }))
                }
            }
            val command1: Submission<CompareSeatMapOptionsRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = seatingSeatMapCalculationService.compareSeatMapOptions(payload1)
            val outbound2 = browseFuelCardsRequest {
                this.context = request.context
                this.filter = BrowseFuelCardsRequestKt.filter {
                    this.createdAfter = request.part.audit.updatedAt
                    this.ownerId = reply1.seatMap.manager.userId
                    this.region = request.part.address.region
                }
            }
            val adapter2 = OutboundPort<BrowseFuelCardsRequest, BrowseFuelCardsResponse> { payload ->
                fuelFuelCardQueryService.browseFuelCards(payload)
            }
            val port2 = DelegatingPort(adapter2)
            val reply2 = port2.execute(outbound2)
            val outbound3 = quoteServiceScheduleRequest {
                this.context = request.context
                this.serviceSchedule = serviceSchedule {
                    this.audit = auditInfo {
                        this.createdAt = reply1.seatMap.audit.createdAt
                        this.createdBy = reply1.seatMap.audit.createdBy
                    }
                    this.location = postalAddress {
                        this.recipient = reply1.seatMap.location.recipient
                    }
                }
            }
            val reply3 = invokeScorePart3(outbound3)
            scorePartResponse {
                this.part = part {
                    this.audit = auditInfo {
                        this.updatedAt = reply1.seatMap.audit.updatedAt
                        this.updatedBy = reply3.serviceSchedule.audit.updatedBy
                    }
                    this.capacity = request.part.capacity
                    this.owner = userRef {
                        this.userId = reply3.serviceSchedule.organizer.userId
                    }
                    this.partId = request.partId
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply2.result.referenceIdsList)
                    this.status = reply0.result.status
                }
            }
        }

    /** Validates a part. */
    override suspend fun validatePart(request: ValidatePartRequest): ValidatePartResponse {
        return handleValidatePart(request)
    }

    private suspend fun handleCheckPartEligibility(request: CheckPartEligibilityRequest): CheckPartEligibilityResponse =
        RpcBoundary.run("acme.maintenance.v1.PartValidationService/CheckPartEligibility") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CheckPartEligibility", request)
            log.atInfo().addKeyValue("operation", "CheckPartEligibility").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.part.partId).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                countAuditEventsRequest {
                    this.context = request.context
                    this.filter = CountAuditEventsRequestKt.filter {
                        this.createdAfter = request.part.audit.updatedAt
                        this.region = request.part.address.region
                    }
                }
            }
            val reply0 = auditAuditEventQueryService.countAuditEvents(outbound0)
            val outbound1 = searchLedgerAccountsRequest {
                this.context = request.context
                this.filter = SearchLedgerAccountsRequestKt.filter {
                    this.createdAfter = request.part.audit.updatedAt
                    this.labels.putAll(request.annotationsMap)
                    this.ownerId = request.part.owner.userId
                    this.region = request.part.address.region
                }
            }
            val reply1 = ClientRegistry.lookup("acme.ledger.v1.LedgerAccountService", LedgerAccountServiceGrpcKt.LedgerAccountServiceCoroutineStub::class.java).searchLedgerAccounts(outbound1)
            val outbound2 = exportConfigSetsRequest {
                this.context = request.context
                this.filter = ExportConfigSetsRequestKt.filter {
                    this.createdAfter = request.part.audit.createdAt
                    this.labels.putAll(reply1.result.detailsMap)
                    this.ownerId = (request.part.componentsMap["primary"] ?: PartComponent.getDefaultInstance()).componentId
                    this.region = request.part.address.region
                }
            }
            configurationConfigSetQueryService.exportConfigSets(outbound2).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            checkPartEligibilityResponse {
                this.part = part {
                    this.name = request.part.name
                    this.partId = request.partId
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleExplainPartDecision(request: ExplainPartDecisionRequest): ExplainPartDecisionResponse =
        RpcBoundary.run("acme.maintenance.v1.PartValidationService/ExplainPartDecision") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainPartDecision", request)
            log.atInfo().addKeyValue("operation", "ExplainPartDecision").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.part.partId).log("Recorded audit detail")
            val outbound0 = previewServiceScheduleRequest {
                this.context = request.context
                this.details = PreviewServiceScheduleRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.part.owner.email
                        this.name = request.details.contact.name
                    }
                }
                this.serviceSchedule = serviceSchedule {
                    this.location = postalAddress {
                        this.region = request.part.address.region
                    }
                    this.participants.addAll(listOf(serviceScheduleParticipant {
                        this.contact = contactInfo {
                            this.preferredChannel = request.details.contact.preferredChannel
                        }
                    }))
                }
            }
            val reply0 = workerForExplainPartDecision0.submit(outbound0)
            val outbound1 = cloneNotificationRequest {
                this.context = request.context
                this.notification = notification {
                    this.audit = auditInfo {
                        this.createdBy = request.part.audit.createdBy
                        this.updatedAt = request.part.audit.updatedAt
                    }
                    this.requester = contactInfo {
                        this.email = request.details.contact.email
                        this.preferredChannel = request.details.contact.preferredChannel
                    }
                }
            }
            val reply1 = clientForExplainPartDecision1.cloneNotification(outbound1)
            val outbound2 = calculateWorkOrderRequest {
                this.context = request.context
                this.workOrder = workOrder {
                    this.audit = auditInfo {
                        this.etag = request.part.audit.etag
                        this.updatedBy = reply0.serviceSchedule.audit.updatedBy
                    }
                }
            }
            val reply2 = flowOf(outbound2).map { value -> maintenanceWorkOrderCalculationService.calculateWorkOrder(value) }.single()
            explainPartDecisionResponse {
                this.part = part {
                    this.audit = auditInfo {
                        this.createdBy = reply0.serviceSchedule.audit.createdBy
                    }
                    this.owner = userRef {
                        this.email = reply2.workOrder.assignee.email
                        this.userId = reply2.workOrder.assignee.userId
                    }
                    this.partId = request.partId
                    this.status = request.part.status
                    this.tags.addAll(reply1.notification.tagsList)
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private fun NormalizePartRequest.toExplainServiceScheduleDecisionFromNormalizePart3(reply0: LintServiceSchedulesResponse): ExplainServiceScheduleDecisionRequest {
        val source = this
        return explainServiceScheduleDecisionRequest {
            this.context = source.context
            this.serviceSchedule = serviceSchedule {
                this.location = postalAddress {
                    this.recipient = reply0.serviceSchedule.location.recipient
                }
                this.participants.addAll(listOf(serviceScheduleParticipant {
                    this.contact = contactInfo {
                        this.email = source.details.contact.email
                    }
                }))
            }
        }
    }

    private suspend fun handleNormalizePart(request: NormalizePartRequest): NormalizePartResponse =
        RpcBoundary.run("acme.maintenance.v1.PartValidationService/NormalizePart") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "NormalizePart", request)
            log.atInfo().addKeyValue("operation", "NormalizePart").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.part.partId).log("Recorded audit detail")
            val outbound0 = lintServiceSchedulesRequest {
                this.context = request.context
                this.serviceSchedule = serviceSchedule {
                    this.participants.addAll(listOf(serviceScheduleParticipant {
                        this.contact = contactInfo {
                            this.email = request.part.owner.email
                            this.phone = request.details.contact.phone
                        }
                    }))
                }
            }
            val reply0 = maintenanceServiceScheduleValidationService.lintServiceSchedules(outbound0)
            val outbound1 = suggestFeatureFlagsRequest {
                this.context = request.context
                this.filter = SuggestFeatureFlagsRequestKt.filter {
                    this.createdAfter = request.part.audit.updatedAt
                    this.labels.putAll(reply0.result.detailsMap)
                    this.ownerId = reply0.serviceSchedule.organizer.userId
                    this.region = request.part.address.region
                }
            }
            val reply1 = featureflagsFeatureFlagQueryService.suggestFeatureFlags(outbound1)
            val outbound2 = validateServiceScheduleRequest {}.maintenanceCopy {
                this.context = request.context
                this.serviceSchedule = serviceSchedule {
                    this.organizer = userRef {
                        this.email = request.details.contact.email
                    }
                }
            }
            val reply2 = maintenanceServiceScheduleValidationService.validateServiceSchedule(outbound2)
            val outbound3 = request.toExplainServiceScheduleDecisionFromNormalizePart3(reply0)
            val reply3 = maintenanceServiceScheduleValidationService.explainServiceScheduleDecision(outbound3)
            normalizePartResponse {
                this.outcome = NormalizePartResponseKt.outcome {
                    this.message = reply1.result.message
                }
                this.part = part {
                    this.audit = auditInfo {
                        this.etag = reply0.serviceSchedule.audit.etag
                        this.updatedBy = reply3.serviceSchedule.audit.updatedBy
                    }
                    this.partId = request.partId
                    this.specs.putAll(request.part.specsMap)
                }
                this.result = operationResult {
                    this.details.putAll(reply3.result.detailsMap)
                    this.referenceIds.addAll((reply0.result.referenceIdsList) + (reply2.result.referenceIdsList))
                    this.status = "ok"
                }
            }
        }

    private suspend fun handlePreviewPart(request: PreviewPartRequest): PreviewPartResponse =
        RpcBoundary.run("acme.maintenance.v1.PartValidationService/PreviewPart") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PreviewPart", request)
            log.atInfo().addKeyValue("operation", "PreviewPart").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.part.partId).log("Recorded audit detail")
            val outbound0 = previewServiceScheduleRequest {
                this.context = request.context
                this.details = PreviewServiceScheduleRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.part.owner.email
                    }
                }
                this.serviceSchedule = serviceSchedule {
                    this.location = postalAddress {
                        this.locality = when (request.part.locatorCase) {
                            Part.LocatorCase.ADDRESS -> request.part.address.locality
                            else -> ""
                        }
                    }
                }
            }
            val reply0 = maintenanceServiceScheduleValidationService.previewServiceSchedule(outbound0)
            val outbound1 = explainAuditEventDecisionRequest {
                this.auditEvent = auditEvent {
                    this.subject = auditTrailRef {
                        this.displayName = request.part.owner.displayName
                    }
                }
                this.context = request.context
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = auditAuditEventValidationService.explainAuditEventDecision(envelope1.unpack(ExplainAuditEventDecisionRequest::class.java))
            val outbound2 = cloneDeliveryRequest {
                this.context = request.context
                this.delivery = delivery {
                    this.audit = auditInfo {
                        this.etag = reply0.serviceSchedule.audit.etag
                    }
                    this.billingAddress = postalAddress {
                        this.region = reply0.serviceSchedule.location.region
                    }
                }
            }
            val reply2 = deliveriesDeliveryService.cloneDelivery(WireCopies.json(outbound2))
            previewPartResponse {
                this.outcome = PreviewPartResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.part = part {
                    this.name = request.part.name
                    this.partId = request.partId
                }
                this.result = operationResult {
                    this.details.putAll(reply2.result.detailsMap)
                    this.message = reply1.result.message
                    this.referenceIds.addAll(reply2.result.referenceIdsList)
                    this.status = reply0.outcome.status
                }
            }
        }

    private data class ScorePart0Command(
        val context: RequestContext,
        val filterRegion: String,
        val filterOwnerId: String
    ) {
        fun toRequest(): SearchSearchIndexesRequest {
            val command = this
            return searchSearchIndexesRequest {
                this.context = command.context
                this.filter = SearchSearchIndexesRequestKt.filter {
                    this.ownerId = command.filterOwnerId
                    this.region = command.filterRegion
                }
            }
        }
    }

    private suspend fun handleValidatePart(request: ValidatePartRequest): ValidatePartResponse =
        RpcBoundary.run("acme.maintenance.v1.PartValidationService/ValidatePart") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ValidatePart", request)
            log.atInfo().addKeyValue("operation", "ValidatePart").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.part.partId).log("Recorded audit detail")
            val outbound0 = quoteYardMoveRequest {
                this.context = request.context
                this.details = QuoteYardMoveRequestKt.details {
                    this.destination = postalAddress {
                        this.locality = request.part.address.locality
                        this.recipient = request.part.address.recipient
                    }
                }
                this.yardMove = yardMove {
                    this.requester = contactInfo {
                        this.name = request.details.contact.name
                    }
                }
            }
            val invoke0: suspend (QuoteYardMoveRequest, Metadata) -> QuoteYardMoveResponse = yardsYardMoveCalculationService::quoteYardMove
            val reply0 = invoke0(outbound0, Metadata())
            val outbound1 = CountFeatureFlagsRequest.newBuilder().apply {
                setContext(request.context)
                setFilter(CountFeatureFlagsRequestKt.filter {
                    this.createdAfter = reply0.yardMove.sla.dueAt
                    this.labels.putAll(reply0.result.detailsMap)
                    this.ownerId = reply0.yardMove.sla.policyId
                    this.region = request.part.address.region
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> featureflagsFeatureFlagQueryService.countFeatureFlags(value) }
            validatePartResponse {
                this.part = part {
                    this.owner = userRef {
                        this.displayName = reply0.yardMove.customer.displayName
                    }
                    this.partId = request.partId
                    this.status = request.part.status
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply1.result.message
                    this.status = "ok"
                }
            }
        }
}
