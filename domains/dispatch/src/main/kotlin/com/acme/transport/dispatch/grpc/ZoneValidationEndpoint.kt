package com.acme.transport.dispatch.grpc

import com.acme.audit.v1.AuditEventQueryServiceGrpcKt
import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.BrowseAuditEventsRequest
import com.acme.audit.v1.BrowseAuditEventsRequestKt
import com.acme.audit.v1.NormalizeAuditEventRequestKt
import com.acme.audit.v1.browseAuditEventsRequest
import com.acme.audit.v1.normalizeAuditEventRequest
import com.acme.authorization.v1.AccessPolicyQueryServiceGrpcKt
import com.acme.authorization.v1.ExportAccessPoliciesRequestKt
import com.acme.authorization.v1.exportAccessPoliciesRequest
import com.acme.certificates.v1.CertificateCalculationServiceGrpcKt
import com.acme.certificates.v1.QuoteCertificateRequest
import com.acme.certificates.v1.QuoteCertificateRequestKt
import com.acme.certificates.v1.QuoteCertificateResponse
import com.acme.certificates.v1.certificate
import com.acme.certificates.v1.quoteCertificateRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.customerRef
import com.acme.dispatch.v1.CalculateZoneRequest
import com.acme.dispatch.v1.CalculateZoneResponse
import com.acme.dispatch.v1.CheckZoneEligibilityRequest
import com.acme.dispatch.v1.CheckZoneEligibilityResponse
import com.acme.dispatch.v1.ExplainZoneDecisionRequest
import com.acme.dispatch.v1.ExplainZoneDecisionResponse
import com.acme.dispatch.v1.GetZoneRulesRequest
import com.acme.dispatch.v1.GetZoneRulesResponse
import com.acme.dispatch.v1.LintZonesRequest
import com.acme.dispatch.v1.LintZonesResponse
import com.acme.dispatch.v1.LintZonesResponseKt
import com.acme.dispatch.v1.NormalizeZoneRequest
import com.acme.dispatch.v1.NormalizeZoneResponse
import com.acme.dispatch.v1.NormalizeZoneResponseKt
import com.acme.dispatch.v1.PreviewZoneRequest
import com.acme.dispatch.v1.PreviewZoneResponse
import com.acme.dispatch.v1.QuoteZoneRequest
import com.acme.dispatch.v1.ScoreZoneRequest
import com.acme.dispatch.v1.ScoreZoneResponse
import com.acme.dispatch.v1.ScoreZoneResponseKt
import com.acme.dispatch.v1.ValidateZoneRequest
import com.acme.dispatch.v1.ValidateZoneResponse
import com.acme.dispatch.v1.ValidateZoneResponseKt
import com.acme.dispatch.v1.ZoneCalculationServiceGrpcKt
import com.acme.dispatch.v1.ZoneValidationServiceGrpcKt
import com.acme.dispatch.v1.calculateZoneRequest
import com.acme.dispatch.v1.checkZoneEligibilityResponse
import com.acme.dispatch.v1.explainZoneDecisionResponse
import com.acme.dispatch.v1.getZoneRulesResponse
import com.acme.dispatch.v1.lintZonesResponse
import com.acme.dispatch.v1.normalizeZoneResponse
import com.acme.dispatch.v1.previewZoneResponse
import com.acme.dispatch.v1.quoteZoneRequest
import com.acme.dispatch.v1.scoreZoneResponse
import com.acme.dispatch.v1.validateZoneResponse
import com.acme.dispatch.v1.zone
import com.acme.documents.v1.DocumentQueryServiceGrpcKt
import com.acme.documents.v1.SuggestDocumentsRequestKt
import com.acme.documents.v1.suggestDocumentsRequest
import com.acme.featureflags.v1.FeatureFlagServiceGrpcKt
import com.acme.featureflags.v1.createFeatureFlagRequest
import com.acme.featureflags.v1.featureFlag
import com.acme.fuel.v1.AnnotateFuelPolicyVersionRequest
import com.acme.fuel.v1.CalculateFuelTransactionRequest
import com.acme.fuel.v1.CalculateFuelTransactionRequestKt
import com.acme.fuel.v1.CountFuelCardsRequestKt
import com.acme.fuel.v1.ExplainFuelTransactionCalculationRequestKt
import com.acme.fuel.v1.FuelCardQueryServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyHistoryServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionCalculationServiceGrpcKt
import com.acme.fuel.v1.RecalculateFuelTransactionsRequestKt
import com.acme.fuel.v1.annotateFuelPolicyVersionRequest
import com.acme.fuel.v1.calculateFuelTransactionRequest
import com.acme.fuel.v1.countFuelCardsRequest
import com.acme.fuel.v1.explainFuelTransactionCalculationRequest
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.recalculateFuelTransactionsRequest
import com.acme.identity.v1.CountUsersRequestKt
import com.acme.identity.v1.UserQueryServiceGrpcKt
import com.acme.identity.v1.countUsersRequest
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.ListWorkOrdersRequestKt
import com.acme.maintenance.v1.ServiceScheduleValidationServiceGrpcKt
import com.acme.maintenance.v1.ValidateServiceScheduleRequestKt
import com.acme.maintenance.v1.WorkOrderServiceGrpcKt
import com.acme.maintenance.v1.copy as maintenanceCopy
import com.acme.maintenance.v1.listWorkOrdersRequest
import com.acme.maintenance.v1.serviceSchedule
import com.acme.maintenance.v1.serviceScheduleParticipant
import com.acme.maintenance.v1.validateServiceScheduleRequest
import com.acme.telematics.v1.LintTripLogsRequestKt
import com.acme.telematics.v1.TripLogValidationServiceGrpcKt
import com.acme.telematics.v1.lintTripLogsRequest
import com.acme.telematics.v1.tripLog
import com.acme.transport.dispatch.DispatchRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.BrowseYardsRequest
import com.acme.yards.v1.BrowseYardsRequestKt
import com.acme.yards.v1.CountYardsRequest
import com.acme.yards.v1.CountYardsRequestKt
import com.acme.yards.v1.CountYardsResponse
import com.acme.yards.v1.YardQueryServiceGrpcKt
import com.acme.yards.v1.browseYardsRequest
import com.acme.yards.v1.countYardsRequest
import com.google.protobuf.Any as ProtoAny
import com.google.protobuf.Timestamp
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Checks zones against the rules before anything acts on them. */
class ZoneValidationEndpoint(
    private val store: DispatchRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventQueryService: AuditEventQueryServiceGrpcKt.AuditEventQueryServiceCoroutineStub,
    private val auditAuditEventValidationService: AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub,
    private val authorizationAccessPolicyQueryService: AccessPolicyQueryServiceGrpcKt.AccessPolicyQueryServiceCoroutineStub,
    private val certificatesCertificateCalculationService: CertificateCalculationServiceGrpcKt.CertificateCalculationServiceCoroutineStub,
    private val dispatchZoneCalculationService: ZoneCalculationServiceGrpcKt.ZoneCalculationServiceCoroutineStub,
    private val documentsDocumentQueryService: DocumentQueryServiceGrpcKt.DocumentQueryServiceCoroutineStub,
    private val featureflagsFeatureFlagService: FeatureFlagServiceGrpcKt.FeatureFlagServiceCoroutineStub,
    private val fuelFuelCardQueryService: FuelCardQueryServiceGrpcKt.FuelCardQueryServiceCoroutineStub,
    private val fuelFuelPolicyHistoryService: FuelPolicyHistoryServiceGrpcKt.FuelPolicyHistoryServiceCoroutineStub,
    private val fuelFuelTransactionCalculationService: FuelTransactionCalculationServiceGrpcKt.FuelTransactionCalculationServiceCoroutineStub,
    private val identityUserQueryService: UserQueryServiceGrpcKt.UserQueryServiceCoroutineStub,
    private val maintenanceServiceScheduleValidationService: ServiceScheduleValidationServiceGrpcKt.ServiceScheduleValidationServiceCoroutineStub,
    private val maintenanceWorkOrderService: WorkOrderServiceGrpcKt.WorkOrderServiceCoroutineStub,
    private val telematicsTripLogValidationService: TripLogValidationServiceGrpcKt.TripLogValidationServiceCoroutineStub,
    private val yardsYardQueryService: YardQueryServiceGrpcKt.YardQueryServiceCoroutineStub,
) : ZoneValidationServiceGrpcKt.ZoneValidationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(ZoneValidationEndpoint::class.java)

    private val invokeScoreZone1: suspend (QuoteCertificateRequest) -> QuoteCertificateResponse = { payload ->
        certificatesCertificateCalculationService.quoteCertificate(payload)
    }

    /** Checks a zone. */
    override suspend fun checkZoneEligibility(request: CheckZoneEligibilityRequest): CheckZoneEligibilityResponse =
        RpcBoundary.run("acme.dispatch.v1.ZoneValidationService/CheckZoneEligibility") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CheckZoneEligibility", request)
            log.atInfo().addKeyValue("operation", "CheckZoneEligibility").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.zone.zoneId).log("Recorded audit detail")
            val outbound0 = createFeatureFlagRequest {
                this.context = request.context
                this.featureFlag = featureFlag {
                    this.audit = auditInfo {
                        this.etag = request.zone.audit.etag
                    }
                    this.owner = userRef {
                        this.displayName = request.zone.manager.displayName
                    }
                }
            }
            val reply0 = flowOf(outbound0).map { value -> featureflagsFeatureFlagService.createFeatureFlag(value) }.single()
            checkZoneEligibilityResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.zone = zone {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.featureFlag.audit.updatedBy
                    }
                    this.manager = request.zone.manager
                    this.zoneId = request.zoneId
                }
            }
        }

    /** Explains a zone. */
    override suspend fun explainZoneDecision(request: ExplainZoneDecisionRequest): ExplainZoneDecisionResponse {
        return handleExplainZoneDecision(request)
    }

    /** Returns a zone. */
    override suspend fun getZoneRules(request: GetZoneRulesRequest): GetZoneRulesResponse = handleGetZoneRules(request)

    /** Lints a zone. */
    override suspend fun lintZones(request: LintZonesRequest): LintZonesResponse =
        RpcBoundary.run("acme.dispatch.v1.ZoneValidationService/LintZones") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LintZones", request)
            log.atInfo().addKeyValue("operation", "LintZones").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.zone.name).log("Recorded audit detail")
            val outbound0 = request.toBrowseYardsFromLintZones0()
            val reply0 = yardsYardQueryService.browseYards(outbound0)
            val outbound1 = listWorkOrdersRequest {
                this.context = request.context
                this.filter = ListWorkOrdersRequestKt.filter {
                    this.createdAfter = request.zone.audit.createdAt
                    this.labels.putAll(request.zone.attributesMap)
                    this.ownerId = reply0.result.correlationId
                    this.region = request.zone.location.region
                }
            }
            val reply1 = maintenanceWorkOrderService.listWorkOrders(outbound1)
            lintZonesResponse {
                this.outcome = LintZonesResponseKt.outcome {
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply1.result.message
                    this.status = "ok"
                }
                this.zone = zone {
                    this.location = request.zone.location
                    this.zoneId = request.zoneId
                }
            }
        }

    /** Normalizes a zone. */
    override suspend fun normalizeZone(request: NormalizeZoneRequest): NormalizeZoneResponse {
        return handleNormalizeZone(request)
    }

    /** Previews a zone. */
    override suspend fun previewZone(request: PreviewZoneRequest): PreviewZoneResponse = handlePreviewZone(request)

    /** Scores a zone. */
    override suspend fun scoreZone(request: ScoreZoneRequest): ScoreZoneResponse =
        RpcBoundary.run("acme.dispatch.v1.ZoneValidationService/ScoreZone") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScoreZone", request)
            log.atInfo().addKeyValue("operation", "ScoreZone").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.zone.zoneId).log("Recorded audit detail")
            val outbound0 = calculateZoneRequest {
                this.context = request.context
                this.zone = zone {
                    this.children.addAll(listOf(zone {
                        this.children.addAll(listOf(zone {
                            this.audit = auditInfo {
                                this.createdBy = request.zone.audit.createdBy
                                this.etag = request.zone.audit.etag
                            }
                            this.location = postalAddress {
                                this.postalCode = request.details.destination.postalCode
                            }
                            this.manager = userRef {
                                this.email = request.zone.manager.email
                            }
                        }))
                    }))
                }
            }
            val adapter0 = OutboundPort<CalculateZoneRequest, CalculateZoneResponse> { payload ->
                dispatchZoneCalculationService.calculateZone(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = quoteCertificateRequest {
                this.certificate = certificate {
                    this.billingAddress = postalAddress {
                        this.recipient = request.details.destination.recipient
                        this.region = request.details.destination.region
                    }
                }
                this.context = request.context
                this.details = QuoteCertificateRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(request.zone.location.linesList)
                    }
                }
            }
            val reply1 = invokeScoreZone1(outbound1)
            val outbound2 = countYardsRequest {
                this.context = request.context
                this.filter = CountYardsRequestKt.filter {
                    this.createdAfter = request.zone.audit.createdAt
                    this.region = reply0.zone.location.region
                }
            }
            val invoke2: suspend (CountYardsRequest, Metadata) -> CountYardsResponse = yardsYardQueryService::countYards
            val reply2 = invoke2(outbound2, Metadata())
            scoreZoneResponse {
                this.breakdown = ScoreZoneResponseKt.breakdown {
                    this.currencyCode = request.details.currencyCode
                }
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.referenceIds.addAll(reply2.result.referenceIdsList)
                    this.status = "ok"
                }
                this.zone = zone {
                    this.status = reply0.zone.status
                    this.zoneId = request.zoneId
                }
            }
        }

    /** Validates a zone. */
    override suspend fun validateZone(request: ValidateZoneRequest): ValidateZoneResponse {
        return handleValidateZone(request)
    }

    private suspend fun handleExplainZoneDecision(request: ExplainZoneDecisionRequest): ExplainZoneDecisionResponse =
        RpcBoundary.run("acme.dispatch.v1.ZoneValidationService/ExplainZoneDecision") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainZoneDecision", request)
            log.atInfo().addKeyValue("operation", "ExplainZoneDecision").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.zone.zoneId).log("Recorded audit detail")
            val outbound0 = countFuelCardsRequest {
                this.context = request.context
                this.filter = CountFuelCardsRequestKt.filter {
                    this.createdAfter = request.zone.audit.createdAt
                    this.labels.putAll(request.annotationsMap)
                    this.ownerId = request.zone.manager.userId
                    this.region = request.zone.location.region
                }
            }
            val reply0 = withTimeout(5_000) { fuelFuelCardQueryService.countFuelCards(outbound0) }
            val configure1: NormalizeAuditEventRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.details = NormalizeAuditEventRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.zone.manager.email
                        this.name = request.details.contact.name
                    }
                    this.locale = request.details.locale
                }
            }
            val outbound1 = normalizeAuditEventRequest(configure1)
            val reply1 = auditAuditEventValidationService.normalizeAuditEvent(outbound1)
            val outbound2 = recalculateFuelTransactionsRequest {
                this.context = request.context
                this.details = RecalculateFuelTransactionsRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(request.zone.location.linesList)
                        this.locality = request.zone.location.locality
                    }
                }
                this.fuelTransaction = fuelTransaction {
                    this.subject = customerRef {
                        this.displayName = reply1.auditEvent.subject.displayName
                        this.email = request.zone.manager.email
                    }
                }
            }
            val reply2 = fuelFuelTransactionCalculationService.recalculateFuelTransactions(outbound2)
            val outbound3 = exportAccessPoliciesRequest {
                this.context = request.context
                this.filter = ExportAccessPoliciesRequestKt.filter {
                    this.createdAfter = reply1.auditEvent.occurredAt
                    this.ownerId = reply1.result.correlationId
                    this.region = request.zone.location.region
                }
            }
            authorizationAccessPolicyQueryService.exportAccessPolicies(outbound3).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            explainZoneDecisionResponse {
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = reply0.result.status
                }
                this.zone = zone {
                    this.children.addAll(listOf(zone {
                        this.children.addAll(listOf(zone {
                            this.attributes.putAll(reply1.auditEvent.attributesMap)
                            this.manager = userRef {
                                this.email = reply2.fuelTransaction.subject.email
                            }
                        }))
                        this.manager = userRef {
                            this.displayName = reply1.auditEvent.subject.displayName
                        }
                    }))
                    this.location = request.zone.location
                    this.zoneId = request.zoneId
                }
            }
        }

    private suspend fun handleGetZoneRules(request: GetZoneRulesRequest): GetZoneRulesResponse =
        RpcBoundary.run("acme.dispatch.v1.ZoneValidationService/GetZoneRules") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetZoneRules", request)
            log.atInfo().addKeyValue("operation", "GetZoneRules").log("Handling transport request")
            val outbound0 = validateServiceScheduleRequest {}.maintenanceCopy {
                this.context = request.context
                this.details = ValidateServiceScheduleRequestKt.details {
                    this.locale = request.context.locale
                }
                this.serviceSchedule = serviceSchedule {
                    this.organizer = userRef {
                        this.userId = request.context.actor.userId
                    }
                    this.participants.addAll(listOf(serviceScheduleParticipant {
                        this.contact = contactInfo {
                            this.email = request.context.actor.email
                        }
                    }))
                }
            }
            val reply0 = maintenanceServiceScheduleValidationService.validateServiceSchedule(outbound0)
            getZoneRulesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.zone = zone {
                    this.children.addAll(listOf(zone {
                        this.audit = auditInfo {
                            this.updatedBy = reply0.serviceSchedule.audit.updatedBy
                        }
                    }))
                    this.zoneId = request.zoneId
                }
            }
        }

    private fun LintZonesRequest.toBrowseYardsFromLintZones0(): BrowseYardsRequest {
        val source = this
        return browseYardsRequest {
            this.context = source.context
            this.filter = BrowseYardsRequestKt.filter {
                this.createdAfter = source.zone.audit.createdAt
                this.labels.putAll(source.zone.attributesMap)
                this.ownerId = source.zone.zoneId
                this.region = source.zone.location.region
            }
        }
    }

    private suspend fun handleNormalizeZone(request: NormalizeZoneRequest): NormalizeZoneResponse =
        RpcBoundary.run("acme.dispatch.v1.ZoneValidationService/NormalizeZone") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "NormalizeZone", request)
            log.atInfo().addKeyValue("operation", "NormalizeZone").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.zone.zoneId).log("Recorded audit detail")
            val outbound0 = calculateFuelTransactionRequest {
                this.context = request.context
                this.details = CalculateFuelTransactionRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(request.zone.location.linesList)
                        this.postalCode = request.zone.location.postalCode
                        this.recipient = request.zone.location.recipient
                    }
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = fuelFuelTransactionCalculationService.calculateFuelTransaction(envelope0.unpack(CalculateFuelTransactionRequest::class.java))
            val outbound1 = lintTripLogsRequest {
                this.context = request.context
                this.details = LintTripLogsRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.zone.manager.email
                        this.preferredChannel = request.details.contact.preferredChannel
                    }
                }
                this.tripLog = tripLog {
                    this.counterparty = customerRef {
                        this.email = reply0.fuelTransaction.subject.email
                    }
                }
            }
            val reply1 = telematicsTripLogValidationService.lintTripLogs(WireCopies.json(outbound1))
            normalizeZoneResponse {
                this.outcome = NormalizeZoneResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.zone = zone {
                    this.children.addAll(listOf(zone {
                        this.children.addAll(listOf(zone {
                            this.location = postalAddress {
                                this.locality = reply1.tripLog.billingAddress.locality
                            }
                            this.manager = userRef {
                                this.displayName = reply0.fuelTransaction.subject.displayName
                            }
                        }))
                    }))
                    this.name = request.zone.name
                    this.zoneId = request.zoneId
                }
            }
        }

    private data class PreviewZone0Command(
        val context: RequestContext,
        val zoneChildrenChildrenManagerEmail: String,
        val zoneChildrenChildrenLocationRecipient: String,
        val zoneChildrenAuditUpdatedAt: Timestamp,
        val zoneChildrenChildrenAuditUpdatedBy: String
    ) {
        fun toRequest(): QuoteZoneRequest {
            val command = this
            return quoteZoneRequest {
                this.context = command.context
                this.zone = zone {
                    this.children.addAll(listOf(zone {
                        this.audit = auditInfo {
                            this.updatedAt = command.zoneChildrenAuditUpdatedAt
                        }
                        this.children.addAll(listOf(zone {
                            this.audit = auditInfo {
                                this.updatedBy = command.zoneChildrenChildrenAuditUpdatedBy
                            }
                            this.location = postalAddress {
                                this.recipient = command.zoneChildrenChildrenLocationRecipient
                            }
                            this.manager = userRef {
                                this.email = command.zoneChildrenChildrenManagerEmail
                            }
                        }))
                    }))
                }
            }
        }
    }

    private suspend fun handlePreviewZone(request: PreviewZoneRequest): PreviewZoneResponse =
        RpcBoundary.run("acme.dispatch.v1.ZoneValidationService/PreviewZone") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PreviewZone", request)
            log.atInfo().addKeyValue("operation", "PreviewZone").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.zone.zoneId).log("Recorded audit detail")
            val outbound0 = PreviewZone0Command(
                context = request.context,
                zoneChildrenChildrenManagerEmail = request.zone.manager.email,
                zoneChildrenChildrenLocationRecipient = request.zone.location.recipient,
                zoneChildrenAuditUpdatedAt = request.zone.audit.updatedAt,
                zoneChildrenChildrenAuditUpdatedBy = request.zone.audit.updatedBy
            ).toRequest()
            val reply0 = dispatchZoneCalculationService.quoteZone(outbound0)
            val outbound1 = annotateFuelPolicyVersionRequest {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.etag = reply0.zone.audit.etag
                        this.updatedBy = request.zone.audit.updatedBy
                    }
                }
            }
            val command1: Submission<AnnotateFuelPolicyVersionRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = fuelFuelPolicyHistoryService.annotateFuelPolicyVersion(payload1)
            previewZoneResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.zone = zone {
                    this.audit = auditInfo {
                        this.updatedAt = reply1.fuelPolicy.audit.updatedAt
                    }
                    this.children.addAll(listOf(zone {
                        this.name = reply0.zone.name
                    }))
                    this.status = request.zone.status
                    this.zoneId = request.zoneId
                }
            }
        }

    private suspend fun handleValidateZone(request: ValidateZoneRequest): ValidateZoneResponse =
        RpcBoundary.run("acme.dispatch.v1.ZoneValidationService/ValidateZone") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ValidateZone", request)
            log.atInfo().addKeyValue("operation", "ValidateZone").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.zone.zoneId).log("Recorded audit detail")
            val outbound0 = BrowseAuditEventsRequest.newBuilder().apply {
                setContext(request.context)
                setFilter(BrowseAuditEventsRequestKt.filter {
                    this.region = request.zone.location.region
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> auditAuditEventQueryService.browseAuditEvents(value) }
            val outbound1 = RequestFactory.create {
                suggestDocumentsRequest {
                    this.context = request.context
                    this.filter = SuggestDocumentsRequestKt.filter {
                        this.createdAfter = request.zone.audit.updatedAt
                        this.ownerId = request.zone.manager.userId
                        this.region = request.zone.location.region
                    }
                }
            }
            val reply1 = documentsDocumentQueryService.suggestDocuments(outbound1)
            val outbound2 = countUsersRequest {
                this.context = request.context
                this.filter = CountUsersRequestKt.filter {
                    this.createdAfter = request.zone.audit.updatedAt
                    this.region = request.zone.location.region
                }
            }
            val reply2 = ClientRegistry.lookup("acme.identity.v1.UserQueryService", UserQueryServiceGrpcKt.UserQueryServiceCoroutineStub::class.java).countUsers(outbound2)
            val outbound3 = explainFuelTransactionCalculationRequest {
                this.context = request.context
                this.details = ExplainFuelTransactionCalculationRequestKt.details {
                    this.contact = contactInfo {
                        this.name = request.details.contact.name
                    }
                }
                this.fuelTransaction = fuelTransaction {
                    this.subject = customerRef {
                        this.displayName = request.zone.manager.displayName
                        this.email = request.zone.manager.email
                    }
                }
            }
            val reply3 = fuelFuelTransactionCalculationService.explainFuelTransactionCalculation(WireCopies.reflective(outbound3))
            validateZoneResponse {
                this.outcome = ValidateZoneResponseKt.outcome {
                    this.nextSteps.addAll(reply3.outcome.nextStepsList)
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.details.putAll(reply2.result.detailsMap)
                    this.message = reply0.result.message
                    this.status = "ok"
                }
                this.zone = zone {
                    this.children.addAll(listOf(zone {
                        this.children.addAll(listOf(zone {
                            this.attributes.putAll(reply3.fuelTransaction.attributesMap)
                        }))
                    }))
                    this.manager = request.zone.manager
                    this.zoneId = request.zoneId
                }
            }
        }
}
