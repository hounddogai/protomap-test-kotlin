package com.acme.transport.maintenance.api

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.concessions.v1.ConcessionStandQueryServiceGrpcKt
import com.acme.concessions.v1.ExportConcessionStandsRequestKt
import com.acme.concessions.v1.exportConcessionStandsRequest
import com.acme.deliveries.v1.CountProofOfDeliveriesRequestKt
import com.acme.deliveries.v1.ProofOfDeliveryQueryServiceGrpcKt
import com.acme.deliveries.v1.countProofOfDeliveriesRequest
import com.acme.featureflags.v1.FeatureFlagValidationServiceGrpcKt
import com.acme.featureflags.v1.ValidateFeatureFlagRequestKt
import com.acme.featureflags.v1.featureFlag
import com.acme.featureflags.v1.validateFeatureFlagRequest
import com.acme.fuel.v1.CalculateFuelTransactionRequestKt
import com.acme.fuel.v1.FuelTransactionCalculationServiceGrpcKt
import com.acme.fuel.v1.calculateFuelTransactionRequest
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.FindLedgerAccountsRequestKt
import com.acme.ledger.v1.LedgerAccountQueryServiceGrpcKt
import com.acme.ledger.v1.findLedgerAccountsRequest
import com.acme.maintenance.v1.EstimatePartRequest
import com.acme.maintenance.v1.EstimateServiceScheduleRequest
import com.acme.maintenance.v1.EstimateServiceScheduleRequestKt
import com.acme.maintenance.v1.EstimateServiceScheduleResponse
import com.acme.maintenance.v1.ExplainServiceScheduleCalculationRequest
import com.acme.maintenance.v1.ExplainServiceScheduleDecisionRequest
import com.acme.maintenance.v1.ExplainServiceScheduleDecisionResponse
import com.acme.maintenance.v1.ExplainServiceScheduleDecisionResponseKt
import com.acme.maintenance.v1.GetServiceScheduleRatesRequest
import com.acme.maintenance.v1.GetServiceScheduleRatesResponse
import com.acme.maintenance.v1.GetServiceScheduleRulesRequest
import com.acme.maintenance.v1.GetServiceScheduleRulesResponse
import com.acme.maintenance.v1.LintServiceSchedulesRequest
import com.acme.maintenance.v1.LintServiceSchedulesResponse
import com.acme.maintenance.v1.NormalizeServiceScheduleRequest
import com.acme.maintenance.v1.NormalizeServiceScheduleResponse
import com.acme.maintenance.v1.NormalizeServiceScheduleResponseKt
import com.acme.maintenance.v1.PartCalculationServiceGrpcKt
import com.acme.maintenance.v1.PreviewServiceScheduleRequest
import com.acme.maintenance.v1.PreviewServiceScheduleResponse
import com.acme.maintenance.v1.QuoteServiceScheduleRequest
import com.acme.maintenance.v1.QuoteServiceScheduleRequestKt
import com.acme.maintenance.v1.QuoteServiceScheduleResponse
import com.acme.maintenance.v1.RecalculateServiceSchedulesRequest
import com.acme.maintenance.v1.ServiceScheduleCalculationServiceGrpcKt
import com.acme.maintenance.v1.ServiceScheduleValidationServiceGrpcKt
import com.acme.maintenance.v1.SimulateServiceScheduleRequestKt
import com.acme.maintenance.v1.ValidateServiceScheduleRequest
import com.acme.maintenance.v1.ValidateServiceScheduleResponse
import com.acme.maintenance.v1.ValidateServiceScheduleResponseKt
import com.acme.maintenance.v1.estimatePartRequest
import com.acme.maintenance.v1.estimateServiceScheduleRequest
import com.acme.maintenance.v1.explainServiceScheduleCalculationRequest
import com.acme.maintenance.v1.explainServiceScheduleDecisionResponse
import com.acme.maintenance.v1.getServiceScheduleRatesRequest
import com.acme.maintenance.v1.getServiceScheduleRulesResponse
import com.acme.maintenance.v1.lintServiceSchedulesResponse
import com.acme.maintenance.v1.normalizeServiceScheduleResponse
import com.acme.maintenance.v1.part
import com.acme.maintenance.v1.partComponent
import com.acme.maintenance.v1.previewServiceScheduleResponse
import com.acme.maintenance.v1.quoteServiceScheduleRequest
import com.acme.maintenance.v1.recalculateServiceSchedulesRequest
import com.acme.maintenance.v1.serviceSchedule
import com.acme.maintenance.v1.serviceScheduleDailyRule
import com.acme.maintenance.v1.serviceScheduleParticipant
import com.acme.maintenance.v1.simulateServiceScheduleRequest
import com.acme.maintenance.v1.validateServiceScheduleResponse
import com.acme.transport.maintenance.MaintenanceRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.ExplainYardMoveDecisionRequest
import com.acme.yards.v1.YardMoveValidationServiceGrpcKt
import com.acme.yards.v1.explainYardMoveDecisionRequest
import com.acme.yards.v1.yardMove
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

/** Checks service schedules against the rules before anything acts on them. */
class ServiceScheduleValidationGrpcService(
    private val store: MaintenanceRepository,
    private val workers: CoroutineScope,
    private val concessionsConcessionStandQueryService: ConcessionStandQueryServiceGrpcKt.ConcessionStandQueryServiceCoroutineStub,
    private val deliveriesProofOfDeliveryQueryService: ProofOfDeliveryQueryServiceGrpcKt.ProofOfDeliveryQueryServiceCoroutineStub,
    private val featureflagsFeatureFlagValidationService: FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub,
    private val fuelFuelTransactionCalculationService: FuelTransactionCalculationServiceGrpcKt.FuelTransactionCalculationServiceCoroutineStub,
    private val ledgerLedgerAccountQueryService: LedgerAccountQueryServiceGrpcKt.LedgerAccountQueryServiceCoroutineStub,
    private val maintenancePartCalculationService: PartCalculationServiceGrpcKt.PartCalculationServiceCoroutineStub,
    private val maintenanceServiceScheduleCalculationService: ServiceScheduleCalculationServiceGrpcKt.ServiceScheduleCalculationServiceCoroutineStub,
    private val yardsYardMoveValidationService: YardMoveValidationServiceGrpcKt.YardMoveValidationServiceCoroutineStub,
) : ServiceScheduleValidationServiceGrpcKt.ServiceScheduleValidationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(ServiceScheduleValidationGrpcService::class.java)

    private val workerForPreviewServiceSchedule0 = ReplyMailbox<QuoteServiceScheduleRequest, QuoteServiceScheduleResponse>(workers) { payload ->
        withTimeout(5_000) { maintenanceServiceScheduleCalculationService.quoteServiceSchedule(payload) }
    }

    private val clientForPreviewServiceSchedule1 by lazy { ledgerLedgerAccountQueryService }

    /** Explains a service schedule. */
    override suspend fun explainServiceScheduleDecision(request: ExplainServiceScheduleDecisionRequest): ExplainServiceScheduleDecisionResponse {
        return handleExplainServiceScheduleDecision(request)
    }

    /** Returns a service schedule. */
    override suspend fun getServiceScheduleRules(request: GetServiceScheduleRulesRequest): GetServiceScheduleRulesResponse = handleGetServiceScheduleRules(request)

    /** Lints a service schedule. */
    override suspend fun lintServiceSchedules(request: LintServiceSchedulesRequest): LintServiceSchedulesResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleValidationService/LintServiceSchedules") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LintServiceSchedules", request)
            log.atInfo().addKeyValue("operation", "LintServiceSchedules").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.serviceSchedule.title).log("Recorded audit detail")
            val outbound0 = estimateServiceScheduleRequest {
                this.context = request.context
                this.details = EstimateServiceScheduleRequestKt.details {
                    this.destination = postalAddress {
                        this.recipient = request.serviceSchedule.location.recipient
                    }
                }
                this.serviceSchedule = serviceSchedule {
                    this.location = postalAddress {
                        this.locality = request.serviceSchedule.location.locality
                        this.postalCode = request.serviceSchedule.location.postalCode
                    }
                }
            }
            val adapter0 = OutboundPort<EstimateServiceScheduleRequest, EstimateServiceScheduleResponse> { payload ->
                maintenanceServiceScheduleCalculationService.estimateServiceSchedule(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = exportConcessionStandsRequest {
                this.context = request.context
                this.filter = ExportConcessionStandsRequestKt.filter {
                    this.region = request.serviceSchedule.location.region
                }
            }
            concessionsConcessionStandQueryService.exportConcessionStands(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound2 = getServiceScheduleRatesRequest {
                this.context = request.context
                this.serviceScheduleId = request.serviceSchedule.serviceScheduleId
            }
            val invoke2: suspend (GetServiceScheduleRatesRequest, Metadata) -> GetServiceScheduleRatesResponse = maintenanceServiceScheduleCalculationService::getServiceScheduleRates
            val reply2 = invoke2(outbound2, Metadata())
            lintServiceSchedulesResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
                this.serviceSchedule = serviceSchedule {
                    this.location = postalAddress {
                        this.lines.addAll(reply2.serviceSchedule.location.linesList)
                    }
                    this.metadata.putAll(request.serviceSchedule.metadataMap)
                    this.serviceScheduleId = request.serviceScheduleId
                }
            }
        }

    /** Normalizes a service schedule. */
    override suspend fun normalizeServiceSchedule(request: NormalizeServiceScheduleRequest): NormalizeServiceScheduleResponse {
        return handleNormalizeServiceSchedule(request)
    }

    /** Previews a service schedule. */
    override suspend fun previewServiceSchedule(request: PreviewServiceScheduleRequest): PreviewServiceScheduleResponse = handlePreviewServiceSchedule(request)

    /** Validates a service schedule. */
    override suspend fun validateServiceSchedule(request: ValidateServiceScheduleRequest): ValidateServiceScheduleResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleValidationService/ValidateServiceSchedule") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ValidateServiceSchedule", request)
            log.atInfo().addKeyValue("operation", "ValidateServiceSchedule").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.serviceSchedule.serviceScheduleId).log("Recorded audit detail")
            val outbound0 = validateFeatureFlagRequest {
                this.context = request.context
                this.details = ValidateFeatureFlagRequestKt.details {
                    this.contact = contactInfo {
                        this.preferredChannel = request.details.contact.preferredChannel
                    }
                }
                this.featureFlag = featureFlag {
                    this.audit = auditInfo {
                        this.etag = request.serviceSchedule.audit.etag
                    }
                }
            }
            val reply0 = flowOf(outbound0).map { value -> featureflagsFeatureFlagValidationService.validateFeatureFlag(value) }.single()
            val outbound1 = quoteServiceScheduleRequest {
                this.context = request.context
                this.details = QuoteServiceScheduleRequestKt.details {
                    this.destination = postalAddress {
                        this.locality = request.serviceSchedule.location.locality
                    }
                }
                this.serviceSchedule = serviceSchedule {
                    this.audit = auditInfo {
                        this.updatedAt = request.serviceSchedule.audit.updatedAt
                    }
                    this.participants.addAll(listOf(serviceScheduleParticipant {
                        this.contact = contactInfo {
                            this.email = reply0.featureFlag.owner.email
                        }
                    }))
                }
            }
            val reply1 = withTimeout(5_000) { maintenanceServiceScheduleCalculationService.quoteServiceSchedule(outbound1) }
            validateServiceScheduleResponse {
                this.outcome = ValidateServiceScheduleResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = "ok"
                }
                this.serviceSchedule = serviceSchedule {
                    this.serviceScheduleId = request.serviceScheduleId
                    this.title = request.serviceSchedule.title
                }
            }
        }

    private suspend fun handleExplainServiceScheduleDecision(request: ExplainServiceScheduleDecisionRequest): ExplainServiceScheduleDecisionResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleValidationService/ExplainServiceScheduleDecision") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainServiceScheduleDecision", request)
            log.atInfo().addKeyValue("operation", "ExplainServiceScheduleDecision").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.serviceSchedule.serviceScheduleId).log("Recorded audit detail")
            val outbound0 = explainServiceScheduleCalculationRequest {
                this.context = request.context
                this.serviceSchedule = serviceSchedule {
                    this.audit = auditInfo {
                        this.createdBy = request.serviceSchedule.audit.createdBy
                    }
                    this.daily = serviceScheduleDailyRule {
                        this.interval = request.serviceSchedule.daily.interval
                    }
                    this.participants.addAll(listOf(serviceScheduleParticipant {
                        this.contact = contactInfo {
                            this.email = request.serviceSchedule.organizer.email
                        }
                    }))
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = maintenanceServiceScheduleCalculationService.explainServiceScheduleCalculation(envelope0.unpack(ExplainServiceScheduleCalculationRequest::class.java))
            val outbound1 = calculateFuelTransactionRequest {
                this.context = request.context
                this.details = CalculateFuelTransactionRequestKt.details {
                    this.destination = postalAddress {
                        this.postalCode = request.serviceSchedule.location.postalCode
                    }
                }
            }
            val reply1 = fuelFuelTransactionCalculationService.calculateFuelTransaction(WireCopies.json(outbound1))
            explainServiceScheduleDecisionResponse {
                this.outcome = ExplainServiceScheduleDecisionResponseKt.outcome {
                    this.message = reply1.result.message
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.serviceSchedule = serviceSchedule {
                    this.serviceScheduleId = request.serviceScheduleId
                    this.status = request.serviceSchedule.status
                    this.window = reply0.serviceSchedule.window
                }
            }
        }

    private data class GetServiceScheduleRules0Command(
        val context: RequestContext,
        val partOwnerEmail: String,
        val partOwnerUserId: String,
        val partAuditUpdatedAt: Timestamp,
        val partComponentsComponentId: String
    ) {
        fun toRequest(): EstimatePartRequest {
            val command = this
            return estimatePartRequest {
                this.context = command.context
                this.part = part {
                    this.audit = auditInfo {
                        this.updatedAt = command.partAuditUpdatedAt
                    }
                    this.components.putAll(mapOf("primary" to partComponent {
                        this.componentId = command.partComponentsComponentId
                    }))
                    this.owner = userRef {
                        this.email = command.partOwnerEmail
                        this.userId = command.partOwnerUserId
                    }
                }
            }
        }
    }

    private suspend fun handleGetServiceScheduleRules(request: GetServiceScheduleRulesRequest): GetServiceScheduleRulesResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleValidationService/GetServiceScheduleRules") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetServiceScheduleRules", request)
            log.atInfo().addKeyValue("operation", "GetServiceScheduleRules").log("Handling transport request")
            val outbound0 = GetServiceScheduleRules0Command(
                context = request.context,
                partOwnerEmail = request.context.actor.email,
                partOwnerUserId = request.context.actor.userId,
                partAuditUpdatedAt = request.context.deadline,
                partComponentsComponentId = request.serviceScheduleId
            ).toRequest()
            val reply0 = maintenancePartCalculationService.estimatePart(outbound0)
            val outbound1 = explainYardMoveDecisionRequest {
                this.context = request.context
                this.yardMove = yardMove {
                    this.audit = auditInfo {
                        this.createdAt = reply0.part.audit.createdAt
                    }
                }
            }
            val command1: Submission<ExplainYardMoveDecisionRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = yardsYardMoveValidationService.explainYardMoveDecision(payload1)
            getServiceScheduleRulesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.serviceSchedule = serviceSchedule {
                    this.location = postalAddress {
                        this.lines.addAll(reply0.part.address.linesList)
                    }
                    this.organizer = userRef {
                        this.email = reply1.yardMove.assignee.email
                    }
                    this.participants.addAll(listOf(serviceScheduleParticipant {
                        this.contact = contactInfo {
                            this.email = reply1.yardMove.assignee.email
                        }
                    }))
                    this.serviceScheduleId = request.serviceScheduleId
                }
            }
        }

    private suspend fun handleNormalizeServiceSchedule(request: NormalizeServiceScheduleRequest): NormalizeServiceScheduleResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleValidationService/NormalizeServiceSchedule") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "NormalizeServiceSchedule", request)
            log.atInfo().addKeyValue("operation", "NormalizeServiceSchedule").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.serviceSchedule.serviceScheduleId).log("Recorded audit detail")
            val outbound0 = RecalculateServiceSchedulesRequest.newBuilder().apply {
                setContext(request.context)
                setServiceSchedule(serviceSchedule {
                    this.participants.addAll(listOf(serviceScheduleParticipant {
                        this.contact = contactInfo {
                            this.email = request.serviceSchedule.organizer.email
                            this.name = request.details.contact.name
                        }
                    }))
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> maintenanceServiceScheduleCalculationService.recalculateServiceSchedules(value) }
            val outbound1 = RequestFactory.create {
                countProofOfDeliveriesRequest {
                    this.context = request.context
                    this.filter = CountProofOfDeliveriesRequestKt.filter {
                        this.region = reply0.serviceSchedule.location.region
                    }
                }
            }
            val reply1 = deliveriesProofOfDeliveryQueryService.countProofOfDeliveries(outbound1)
            val outbound2 = getServiceScheduleRatesRequest {
                this.context = request.context
                this.serviceScheduleId = request.serviceScheduleId
            }
            val reply2 = ClientRegistry.lookup("acme.maintenance.v1.ServiceScheduleCalculationService", ServiceScheduleCalculationServiceGrpcKt.ServiceScheduleCalculationServiceCoroutineStub::class.java).getServiceScheduleRates(outbound2)
            val outbound3 = simulateServiceScheduleRequest {
                this.context = request.context
                this.details = SimulateServiceScheduleRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(reply2.serviceSchedule.location.linesList)
                    }
                }
                this.serviceSchedule = serviceSchedule {
                    this.location = postalAddress {
                        this.postalCode = reply2.serviceSchedule.location.postalCode
                        this.recipient = reply2.serviceSchedule.location.recipient
                    }
                    this.participants.addAll(listOf(serviceScheduleParticipant {
                        this.contact = contactInfo {
                            this.name = request.details.contact.name
                        }
                    }))
                }
            }
            val reply3 = maintenanceServiceScheduleCalculationService.simulateServiceSchedule(WireCopies.reflective(outbound3))
            normalizeServiceScheduleResponse {
                this.outcome = NormalizeServiceScheduleResponseKt.outcome {
                    this.message = reply1.result.message
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.serviceSchedule = serviceSchedule {
                    this.audit = auditInfo {
                        this.createdAt = reply3.serviceSchedule.audit.createdAt
                        this.updatedBy = reply0.serviceSchedule.audit.updatedBy
                    }
                    this.organizer = reply2.serviceSchedule.organizer
                    this.serviceScheduleId = request.serviceScheduleId
                    this.status = request.serviceSchedule.status
                }
            }
        }

    private suspend fun handlePreviewServiceSchedule(request: PreviewServiceScheduleRequest): PreviewServiceScheduleResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleValidationService/PreviewServiceSchedule") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PreviewServiceSchedule", request)
            log.atInfo().addKeyValue("operation", "PreviewServiceSchedule").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.serviceSchedule.serviceScheduleId).log("Recorded audit detail")
            val outbound0 = quoteServiceScheduleRequest {
                this.context = request.context
                this.serviceSchedule = serviceSchedule {
                    this.participants.addAll(listOf(serviceScheduleParticipant {
                        this.contact = contactInfo {
                            this.phone = request.details.contact.phone
                        }
                    }))
                }
            }
            val reply0 = workerForPreviewServiceSchedule0.submit(outbound0)
            val outbound1 = findLedgerAccountsRequest {
                this.context = request.context
                this.filter = FindLedgerAccountsRequestKt.filter {
                    this.createdAfter = request.serviceSchedule.audit.updatedAt
                    this.labels.putAll(request.serviceSchedule.metadataMap)
                    this.ownerId = reply0.serviceSchedule.organizer.userId
                    this.region = request.serviceSchedule.location.region
                }
            }
            val reply1 = clientForPreviewServiceSchedule1.findLedgerAccounts(outbound1)
            previewServiceScheduleResponse {
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.message = reply0.result.message
                    this.status = reply0.result.status
                }
                this.serviceSchedule = serviceSchedule {
                    this.location = request.serviceSchedule.location
                    this.serviceScheduleId = request.serviceScheduleId
                }
            }
        }
}
