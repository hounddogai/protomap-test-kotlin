package com.acme.transport.maintenance.api

import com.acme.broadcasts.v1.BroadcastServiceGrpcKt
import com.acme.broadcasts.v1.broadcast
import com.acme.broadcasts.v1.updateBroadcastRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.drivers.v1.driverRef
import com.acme.featureflags.v1.CheckFeatureFlagEligibilityRequest
import com.acme.featureflags.v1.CheckFeatureFlagEligibilityRequestKt
import com.acme.featureflags.v1.FeatureFlagValidationServiceGrpcKt
import com.acme.featureflags.v1.checkFeatureFlagEligibilityRequest
import com.acme.fuel.v1.FuelPolicyHistoryServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyValidationServiceGrpcKt
import com.acme.fuel.v1.LintFuelPoliciesRequest
import com.acme.fuel.v1.LintFuelPoliciesRequestKt
import com.acme.fuel.v1.LintFuelPoliciesResponse
import com.acme.fuel.v1.RecordFuelPolicyVersionRequest
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.lintFuelPoliciesRequest
import com.acme.fuel.v1.recordFuelPolicyVersionRequest
import com.acme.identity.v1.userRef
import com.acme.keymanagement.v1.CryptoKeyServiceGrpcKt
import com.acme.keymanagement.v1.watchCryptoKeysRequest
import com.acme.maintenance.v1.CalculateServiceScheduleRequest
import com.acme.maintenance.v1.CalculateServiceScheduleResponse
import com.acme.maintenance.v1.EstimateServiceScheduleRequest
import com.acme.maintenance.v1.EstimateServiceScheduleResponse
import com.acme.maintenance.v1.EstimateWorkOrderRequest
import com.acme.maintenance.v1.EstimateWorkOrderRequestKt
import com.acme.maintenance.v1.ExplainServiceScheduleCalculationRequest
import com.acme.maintenance.v1.ExplainServiceScheduleCalculationResponse
import com.acme.maintenance.v1.ExplainWorkOrderCalculationRequestKt
import com.acme.maintenance.v1.GetServiceScheduleRatesRequest
import com.acme.maintenance.v1.GetServiceScheduleRatesResponse
import com.acme.maintenance.v1.QuoteServiceScheduleRequest
import com.acme.maintenance.v1.QuoteServiceScheduleResponse
import com.acme.maintenance.v1.QuoteWorkOrderRequest
import com.acme.maintenance.v1.QuoteWorkOrderRequestKt
import com.acme.maintenance.v1.RecalculateServiceSchedulesRequest
import com.acme.maintenance.v1.RecalculateServiceSchedulesResponse
import com.acme.maintenance.v1.RecalculateWorkOrdersRequest
import com.acme.maintenance.v1.RecalculateWorkOrdersRequestKt
import com.acme.maintenance.v1.RecalculateWorkOrdersResponse
import com.acme.maintenance.v1.ServiceScheduleCalculationServiceGrpcKt
import com.acme.maintenance.v1.ServiceScheduleParticipant
import com.acme.maintenance.v1.SimulateServiceScheduleRequest
import com.acme.maintenance.v1.SimulateServiceScheduleResponse
import com.acme.maintenance.v1.SimulateServiceScheduleResponseKt
import com.acme.maintenance.v1.WorkOrderCalculationServiceGrpcKt
import com.acme.maintenance.v1.calculateServiceScheduleResponse
import com.acme.maintenance.v1.estimateServiceScheduleResponse
import com.acme.maintenance.v1.estimateWorkOrderRequest
import com.acme.maintenance.v1.explainServiceScheduleCalculationResponse
import com.acme.maintenance.v1.explainWorkOrderCalculationRequest
import com.acme.maintenance.v1.getServiceScheduleRatesResponse
import com.acme.maintenance.v1.quoteServiceScheduleResponse
import com.acme.maintenance.v1.quoteWorkOrderRequest
import com.acme.maintenance.v1.recalculateServiceSchedulesResponse
import com.acme.maintenance.v1.recalculateWorkOrdersRequest
import com.acme.maintenance.v1.serviceSchedule
import com.acme.maintenance.v1.serviceScheduleParticipant
import com.acme.maintenance.v1.simulateServiceScheduleResponse
import com.acme.maintenance.v1.workOrder
import com.acme.search.v1.CountSearchIndexesRequestKt
import com.acme.search.v1.CountSearchIndexesResponse
import com.acme.search.v1.SearchIndexQueryServiceGrpcKt
import com.acme.search.v1.SummarizeSearchIndexesRequestKt
import com.acme.search.v1.countSearchIndexesRequest
import com.acme.search.v1.resolveSearchIndexesRequest
import com.acme.search.v1.summarizeSearchIndexesRequest
import com.acme.transport.maintenance.MaintenanceRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

/** Computes the figures of a service schedule, such as totals, taxes, and estimates. */
class ServiceScheduleCalculationGrpcService(
    private val store: MaintenanceRepository,
    private val workers: CoroutineScope,
    private val broadcastsBroadcastService: BroadcastServiceGrpcKt.BroadcastServiceCoroutineStub,
    private val featureflagsFeatureFlagValidationService: FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub,
    private val fuelFuelPolicyHistoryService: FuelPolicyHistoryServiceGrpcKt.FuelPolicyHistoryServiceCoroutineStub,
    private val fuelFuelPolicyValidationService: FuelPolicyValidationServiceGrpcKt.FuelPolicyValidationServiceCoroutineStub,
    private val keymanagementCryptoKeyService: CryptoKeyServiceGrpcKt.CryptoKeyServiceCoroutineStub,
    private val maintenanceWorkOrderCalculationService: WorkOrderCalculationServiceGrpcKt.WorkOrderCalculationServiceCoroutineStub,
    private val searchSearchIndexQueryService: SearchIndexQueryServiceGrpcKt.SearchIndexQueryServiceCoroutineStub,
) : ServiceScheduleCalculationServiceGrpcKt.ServiceScheduleCalculationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(ServiceScheduleCalculationGrpcService::class.java)

    private val invokeQuoteServiceSchedule2: suspend (RecalculateWorkOrdersRequest) -> RecalculateWorkOrdersResponse = { payload ->
        maintenanceWorkOrderCalculationService.recalculateWorkOrders(payload)
    }

    /** Calculates a service schedule. */
    override suspend fun calculateServiceSchedule(request: CalculateServiceScheduleRequest): CalculateServiceScheduleResponse {
        return handleCalculateServiceSchedule(request)
    }

    /** Estimates a service schedule. */
    override suspend fun estimateServiceSchedule(request: EstimateServiceScheduleRequest): EstimateServiceScheduleResponse = handleEstimateServiceSchedule(request)

    /** Explains a service schedule. */
    override suspend fun explainServiceScheduleCalculation(request: ExplainServiceScheduleCalculationRequest): ExplainServiceScheduleCalculationResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleCalculationService/ExplainServiceScheduleCalculation") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainServiceScheduleCalculation", request)
            log.atInfo().addKeyValue("operation", "ExplainServiceScheduleCalculation").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.serviceSchedule.serviceScheduleId).log("Recorded audit detail")
            val outbound0 = recordFuelPolicyVersionRequest {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.owner = userRef {
                        this.email = request.serviceSchedule.organizer.email
                    }
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = fuelFuelPolicyHistoryService.recordFuelPolicyVersion(envelope0.unpack(RecordFuelPolicyVersionRequest::class.java))
            val outbound1 = explainWorkOrderCalculationRequest {
                this.context = request.context
                this.details = ExplainWorkOrderCalculationRequestKt.details {
                    this.contact = contactInfo {
                        this.name = request.details.contact.name
                    }
                }
                this.workOrder = workOrder {
                    this.assignee = userRef {
                        this.email = request.serviceSchedule.organizer.email
                    }
                    this.audit = auditInfo {
                        this.etag = reply0.fuelPolicy.audit.etag
                    }
                    this.requester = contactInfo {
                        this.phone = request.details.contact.phone
                    }
                }
            }
            val reply1 = maintenanceWorkOrderCalculationService.explainWorkOrderCalculation(WireCopies.json(outbound1))
            explainServiceScheduleCalculationResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.serviceSchedule = serviceSchedule {
                    this.audit = auditInfo {
                        this.createdAt = reply0.fuelPolicy.audit.createdAt
                    }
                    this.organizer = userRef {
                        this.displayName = reply0.fuelPolicy.owner.displayName
                        this.email = reply1.workOrder.customer.email
                    }
                    this.participants.addAll(listOf(serviceScheduleParticipant {
                        this.contact = contactInfo {
                            this.phone = reply1.workOrder.requester.phone
                        }
                    }))
                    this.serviceScheduleId = request.serviceScheduleId
                    this.window = request.serviceSchedule.window
                }
            }
        }

    /** Returns a service schedule. */
    override suspend fun getServiceScheduleRates(request: GetServiceScheduleRatesRequest): GetServiceScheduleRatesResponse {
        return handleGetServiceScheduleRates(request)
    }

    /** Quotes a service schedule. */
    override suspend fun quoteServiceSchedule(request: QuoteServiceScheduleRequest): QuoteServiceScheduleResponse = handleQuoteServiceSchedule(request)

    /** Recalculates a service schedule. */
    override suspend fun recalculateServiceSchedules(request: RecalculateServiceSchedulesRequest): RecalculateServiceSchedulesResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleCalculationService/RecalculateServiceSchedules") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecalculateServiceSchedules", request)
            log.atInfo().addKeyValue("operation", "RecalculateServiceSchedules").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.serviceSchedule.serviceScheduleId).log("Recorded audit detail")
            val outbound0 = lintFuelPoliciesRequest {
                this.context = request.context
                this.details = LintFuelPoliciesRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.serviceSchedule.organizer.email
                    }
                }
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.etag = request.serviceSchedule.audit.etag
                        this.updatedAt = request.serviceSchedule.audit.updatedAt
                        this.updatedBy = request.serviceSchedule.audit.updatedBy
                    }
                }
            }
            val invoke0: suspend (LintFuelPoliciesRequest, Metadata) -> LintFuelPoliciesResponse = fuelFuelPolicyValidationService::lintFuelPolicies
            val reply0 = invoke0(outbound0, Metadata())
            val outbound1 = QuoteWorkOrderRequest.newBuilder().apply {
                setContext(request.context)
                setDetails(QuoteWorkOrderRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.serviceSchedule.location.region
                    }
                })
                setWorkOrder(workOrder {
                    this.audit = auditInfo {
                        this.createdAt = request.serviceSchedule.audit.createdAt
                        this.updatedBy = request.serviceSchedule.audit.updatedBy
                    }
                    this.customer = driverRef {
                        this.email = reply0.fuelPolicy.owner.email
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> maintenanceWorkOrderCalculationService.quoteWorkOrder(value) }
            recalculateServiceSchedulesResponse {
                this.result = operationResult {
                    this.message = reply0.outcome.message
                    this.status = "ok"
                }
                this.serviceSchedule = serviceSchedule {
                    this.audit = auditInfo {
                        this.updatedAt = reply1.workOrder.audit.updatedAt
                    }
                    this.organizer = request.serviceSchedule.organizer
                    this.serviceScheduleId = request.serviceScheduleId
                }
            }
        }

    /** Simulates a service schedule. */
    override suspend fun simulateServiceSchedule(request: SimulateServiceScheduleRequest): SimulateServiceScheduleResponse {
        return handleSimulateServiceSchedule(request)
    }

    private suspend fun handleCalculateServiceSchedule(request: CalculateServiceScheduleRequest): CalculateServiceScheduleResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleCalculationService/CalculateServiceSchedule") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CalculateServiceSchedule", request)
            log.atInfo().addKeyValue("operation", "CalculateServiceSchedule").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.serviceSchedule.serviceScheduleId).log("Recorded audit detail")
            var reply0 = CountSearchIndexesResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<ServiceScheduleParticipant>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound0 = countSearchIndexesRequest {
                            this.context = request.context
                            this.filter = CountSearchIndexesRequestKt.filter {
                                this.createdAfter = request.serviceSchedule.audit.createdAt
                                this.labels.putAll(request.serviceSchedule.metadataMap)
                                this.ownerId = item.participantId
                                this.region = request.details.destination.region
                            }
                        }
                        reply0 = searchSearchIndexQueryService.countSearchIndexes(outbound0)
                    }
                }
                try {
                    request.serviceSchedule.participantsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            calculateServiceScheduleResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.serviceSchedule = serviceSchedule {
                    this.metadata.putAll(request.serviceSchedule.metadataMap)
                    this.serviceScheduleId = request.serviceScheduleId
                }
            }
        }

    private suspend fun handleEstimateServiceSchedule(request: EstimateServiceScheduleRequest): EstimateServiceScheduleResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleCalculationService/EstimateServiceSchedule") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EstimateServiceSchedule", request)
            log.atInfo().addKeyValue("operation", "EstimateServiceSchedule").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.serviceSchedule.serviceScheduleId).log("Recorded audit detail")
            val outbound0 = updateBroadcastRequest {
                this.broadcast = broadcast {
                    this.location = postalAddress {
                        this.lines.addAll(request.details.destination.linesList)
                    }
                }
                this.context = request.context
            }
            val reply0 = broadcastsBroadcastService.updateBroadcast(outbound0)
            estimateServiceScheduleResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.serviceSchedule = serviceSchedule {
                    this.location = request.serviceSchedule.location
                    this.organizer = reply0.broadcast.organizer
                    this.serviceScheduleId = request.serviceScheduleId
                }
            }
        }

    private data class GetServiceScheduleRates0Command(
        val context: RequestContext,
        val detailsContactEmail: String,
        val detailsLocale: String
    ) {
        fun toRequest(): CheckFeatureFlagEligibilityRequest {
            val command = this
            return checkFeatureFlagEligibilityRequest {
                this.context = command.context
                this.details = CheckFeatureFlagEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.email = command.detailsContactEmail
                    }
                    this.locale = command.detailsLocale
                }
            }
        }
    }

    private suspend fun handleGetServiceScheduleRates(request: GetServiceScheduleRatesRequest): GetServiceScheduleRatesResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleCalculationService/GetServiceScheduleRates") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetServiceScheduleRates", request)
            log.atInfo().addKeyValue("operation", "GetServiceScheduleRates").log("Handling transport request")
            val outbound0 = GetServiceScheduleRates0Command(
                context = request.context,
                detailsContactEmail = request.context.actor.email,
                detailsLocale = request.context.locale
            ).toRequest()
            val reply0 = featureflagsFeatureFlagValidationService.checkFeatureFlagEligibility(outbound0)
            getServiceScheduleRatesResponse {
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
                this.serviceSchedule = serviceSchedule {
                    this.serviceScheduleId = request.serviceScheduleId
                }
            }
        }

    private suspend fun handleQuoteServiceSchedule(request: QuoteServiceScheduleRequest): QuoteServiceScheduleResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleCalculationService/QuoteServiceSchedule") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "QuoteServiceSchedule", request)
            log.atInfo().addKeyValue("operation", "QuoteServiceSchedule").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.serviceSchedule.serviceScheduleId).log("Recorded audit detail")
            val outbound0 = estimateWorkOrderRequest {
                this.context = request.context
                this.details = EstimateWorkOrderRequestKt.details {
                    this.destination = postalAddress {
                        this.postalCode = request.details.destination.postalCode
                    }
                }
                this.workOrder = workOrder {
                    this.audit = auditInfo {
                        this.createdAt = request.serviceSchedule.audit.createdAt
                    }
                }
            }
            val command0: Submission<EstimateWorkOrderRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = maintenanceWorkOrderCalculationService.estimateWorkOrder(payload0)
            val outbound1 = watchCryptoKeysRequest {
                this.context = request.context
                this.subscriber = contactInfo {
                    this.email = reply0.workOrder.assignee.email
                    this.name = reply0.workOrder.requester.name
                    this.phone = reply0.workOrder.requester.phone
                    this.preferredChannel = reply0.workOrder.requester.preferredChannel
                }
            }
            keymanagementCryptoKeyService.watchCryptoKeys(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound2 = recalculateWorkOrdersRequest {
                this.context = request.context
                this.details = RecalculateWorkOrdersRequestKt.details {
                    this.destination = postalAddress {
                        this.locality = request.serviceSchedule.location.locality
                    }
                }
                this.workOrder = workOrder {
                    this.assignee = userRef {
                        this.userId = reply0.workOrder.assignee.userId
                    }
                    this.audit = auditInfo {
                        this.updatedAt = reply0.workOrder.audit.updatedAt
                    }
                    this.requester = contactInfo {
                        this.email = reply0.workOrder.customer.email
                    }
                }
            }
            val reply2 = invokeQuoteServiceSchedule2(outbound2)
            quoteServiceScheduleResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
                this.serviceSchedule = serviceSchedule {
                    this.organizer = userRef {
                        this.displayName = reply0.workOrder.customer.displayName
                    }
                    this.serviceScheduleId = request.serviceScheduleId
                    this.status = request.serviceSchedule.status
                }
            }
        }

    private suspend fun handleSimulateServiceSchedule(request: SimulateServiceScheduleRequest): SimulateServiceScheduleResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleCalculationService/SimulateServiceSchedule") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SimulateServiceSchedule", request)
            log.atInfo().addKeyValue("operation", "SimulateServiceSchedule").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.serviceSchedule.serviceScheduleId).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                summarizeSearchIndexesRequest {
                    this.annotations.putAll(request.annotationsMap)
                    this.context = request.context
                    this.details = SummarizeSearchIndexesRequestKt.details {
                        this.filters.putAll(request.serviceSchedule.metadataMap)
                        this.period = request.serviceSchedule.window
                    }
                    this.reason = request.reason
                }
            }
            val reply0 = searchSearchIndexQueryService.summarizeSearchIndexes(outbound0)
            val outbound1 = countSearchIndexesRequest {
                this.context = request.context
                this.filter = CountSearchIndexesRequestKt.filter {
                    this.createdAfter = request.serviceSchedule.audit.createdAt
                    this.region = request.details.destination.region
                }
            }
            val reply1 = ClientRegistry.lookup("acme.search.v1.SearchIndexQueryService", SearchIndexQueryServiceGrpcKt.SearchIndexQueryServiceCoroutineStub::class.java).countSearchIndexes(outbound1)
            val outbound2 = resolveSearchIndexesRequest {
                this.context = request.context
            }
            val reply2 = searchSearchIndexQueryService.resolveSearchIndexes(WireCopies.reflective(outbound2))
            simulateServiceScheduleResponse {
                this.breakdown = SimulateServiceScheduleResponseKt.breakdown {
                    this.currencyCode = request.details.currencyCode
                }
                this.result = operationResult {
                    this.details.putAll(reply2.result.detailsMap)
                    this.message = reply2.result.message
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = reply0.result.status
                }
                this.serviceSchedule = serviceSchedule {
                    this.serviceScheduleId = request.serviceScheduleId
                }
            }
        }
}
