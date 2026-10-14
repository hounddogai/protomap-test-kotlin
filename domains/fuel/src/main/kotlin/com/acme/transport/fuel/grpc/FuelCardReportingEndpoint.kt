package com.acme.transport.fuel.grpc

import com.acme.common.v1.Money
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.customers.v1.ComparePreferenceOptionsRequest
import com.acme.customers.v1.ComparePreferenceOptionsResponse
import com.acme.customers.v1.PreferenceCalculationServiceGrpcKt
import com.acme.customers.v1.comparePreferenceOptionsRequest
import com.acme.customers.v1.preference
import com.acme.deliveries.v1.AdvanceProofOfDeliveryWorkflowRequest
import com.acme.deliveries.v1.AdvanceProofOfDeliveryWorkflowRequestKt
import com.acme.deliveries.v1.AdvanceProofOfDeliveryWorkflowResponse
import com.acme.deliveries.v1.ProofOfDeliveryApprovalServiceGrpcKt
import com.acme.deliveries.v1.ProofOfDeliveryWorkflowServiceGrpcKt
import com.acme.deliveries.v1.advanceProofOfDeliveryWorkflowRequest
import com.acme.deliveries.v1.delegateProofOfDeliveryApprovalRequest
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.proofOfDeliveryLine
import com.acme.deliveries.v1.proofOfDeliveryLineTax
import com.acme.dispatch.v1.ZoneValidationServiceGrpcKt
import com.acme.dispatch.v1.checkZoneEligibilityRequest
import com.acme.dispatch.v1.zone
import com.acme.drivers.v1.DriverServiceGrpcKt
import com.acme.drivers.v1.batchGetDriversRequest
import com.acme.drivers.v1.copy as driversCopy
import com.acme.drivers.v1.driverRef
import com.acme.fuel.v1.CountFuelCardTotalsRequest
import com.acme.fuel.v1.CountFuelCardTotalsResponse
import com.acme.fuel.v1.DeleteFuelCardReportRequest
import com.acme.fuel.v1.DeleteFuelCardReportResponse
import com.acme.fuel.v1.ExportFuelCardsRequestKt
import com.acme.fuel.v1.FetchFuelCardReportRequest
import com.acme.fuel.v1.FetchFuelCardReportResponse
import com.acme.fuel.v1.FuelCardExportServiceGrpcKt
import com.acme.fuel.v1.FuelCardImportServiceGrpcKt
import com.acme.fuel.v1.FuelCardQueryServiceGrpcKt
import com.acme.fuel.v1.FuelCardReportingServiceGrpcKt
import com.acme.fuel.v1.FuelPolicy
import com.acme.fuel.v1.FuelPolicyValidationServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionSyncServiceGrpcKt
import com.acme.fuel.v1.GenerateFuelCardReportRequest
import com.acme.fuel.v1.GenerateFuelCardReportResponse
import com.acme.fuel.v1.ListFuelCardReportsRequest
import com.acme.fuel.v1.ListFuelCardReportsResponse
import com.acme.fuel.v1.ListFuelTransactionConflictsRequest
import com.acme.fuel.v1.ListFuelTransactionConflictsRequestKt
import com.acme.fuel.v1.ListFuelTransactionConflictsResponse
import com.acme.fuel.v1.NormalizeFuelPolicyRequest
import com.acme.fuel.v1.NormalizeFuelPolicyRequestKt
import com.acme.fuel.v1.ScheduleFuelCardReportRequest
import com.acme.fuel.v1.ScheduleFuelCardReportResponse
import com.acme.fuel.v1.ShareFuelCardReportRequest
import com.acme.fuel.v1.ShareFuelCardReportResponse
import com.acme.fuel.v1.ShareFuelCardReportResponseKt
import com.acme.fuel.v1.StreamFuelCardExportRequestKt
import com.acme.fuel.v1.StreamFuelCardMetricsRequest
import com.acme.fuel.v1.StreamFuelCardMetricsResponse
import com.acme.fuel.v1.countFuelCardTotalsResponse
import com.acme.fuel.v1.deleteFuelCardReportResponse
import com.acme.fuel.v1.exportFuelCardsRequest
import com.acme.fuel.v1.fetchFuelCardReportResponse
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.generateFuelCardReportResponse
import com.acme.fuel.v1.listFuelCardReportsResponse
import com.acme.fuel.v1.listFuelTransactionConflictsRequest
import com.acme.fuel.v1.mapFuelCardImportColumnsRequest
import com.acme.fuel.v1.normalizeFuelPolicyRequest
import com.acme.fuel.v1.scheduleFuelCardReportResponse
import com.acme.fuel.v1.shareFuelCardReportResponse
import com.acme.fuel.v1.streamFuelCardExportRequest
import com.acme.fuel.v1.streamFuelCardMetricsResponse
import com.acme.identity.v1.userRef
import com.acme.metrics.v1.ListMetricSeriesesRequest
import com.acme.metrics.v1.ListMetricSeriesesRequestKt
import com.acme.metrics.v1.MetricSeriesServiceGrpcKt
import com.acme.metrics.v1.listMetricSeriesesRequest
import com.acme.transport.fuel.FuelRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.venues.v1.ValidateVenueRequestKt
import com.acme.venues.v1.VenueValidationServiceGrpcKt
import com.acme.venues.v1.validateVenueRequest
import com.acme.yards.v1.AdvanceYardMoveWorkflowRequest
import com.acme.yards.v1.CommentYardApprovalRequest
import com.acme.yards.v1.YardApprovalServiceGrpcKt
import com.acme.yards.v1.YardMoveWorkflowServiceGrpcKt
import com.acme.yards.v1.advanceYardMoveWorkflowRequest
import com.acme.yards.v1.commentYardApprovalRequest
import com.acme.yards.v1.yard
import com.acme.yards.v1.yardMove
import com.google.protobuf.Any as ProtoAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Builds and schedules reports about fuel cards. */
class FuelCardReportingEndpoint(
    private val store: FuelRepository,
    private val workers: CoroutineScope,
    private val customersPreferenceCalculationService: PreferenceCalculationServiceGrpcKt.PreferenceCalculationServiceCoroutineStub,
    private val deliveriesProofOfDeliveryApprovalService: ProofOfDeliveryApprovalServiceGrpcKt.ProofOfDeliveryApprovalServiceCoroutineStub,
    private val deliveriesProofOfDeliveryWorkflowService: ProofOfDeliveryWorkflowServiceGrpcKt.ProofOfDeliveryWorkflowServiceCoroutineStub,
    private val dispatchZoneValidationService: ZoneValidationServiceGrpcKt.ZoneValidationServiceCoroutineStub,
    private val driversDriverService: DriverServiceGrpcKt.DriverServiceCoroutineStub,
    private val fuelFuelCardExportService: FuelCardExportServiceGrpcKt.FuelCardExportServiceCoroutineStub,
    private val fuelFuelCardImportService: FuelCardImportServiceGrpcKt.FuelCardImportServiceCoroutineStub,
    private val fuelFuelCardQueryService: FuelCardQueryServiceGrpcKt.FuelCardQueryServiceCoroutineStub,
    private val fuelFuelPolicyValidationService: FuelPolicyValidationServiceGrpcKt.FuelPolicyValidationServiceCoroutineStub,
    private val fuelFuelTransactionSyncService: FuelTransactionSyncServiceGrpcKt.FuelTransactionSyncServiceCoroutineStub,
    private val metricsMetricSeriesService: MetricSeriesServiceGrpcKt.MetricSeriesServiceCoroutineStub,
    private val venuesVenueValidationService: VenueValidationServiceGrpcKt.VenueValidationServiceCoroutineStub,
    private val yardsYardApprovalService: YardApprovalServiceGrpcKt.YardApprovalServiceCoroutineStub,
    private val yardsYardMoveWorkflowService: YardMoveWorkflowServiceGrpcKt.YardMoveWorkflowServiceCoroutineStub,
) : FuelCardReportingServiceGrpcKt.FuelCardReportingServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(FuelCardReportingEndpoint::class.java)

    private val invokeScheduleFuelCardReport1: suspend (ListFuelTransactionConflictsRequest) -> ListFuelTransactionConflictsResponse = { payload ->
        fuelFuelTransactionSyncService.listFuelTransactionConflicts(payload)
    }

    private val workerForStreamFuelCardMetrics1 = ReplyMailbox<ComparePreferenceOptionsRequest, ComparePreferenceOptionsResponse>(workers) { payload ->
        withTimeout(5_000) { customersPreferenceCalculationService.comparePreferenceOptions(payload) }
    }

    /** Counts fuel cards by status. */
    override suspend fun countFuelCardTotals(request: CountFuelCardTotalsRequest): CountFuelCardTotalsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardReportingService/CountFuelCardTotals") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CountFuelCardTotals", request)
            log.atInfo().addKeyValue("operation", "CountFuelCardTotals").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            countFuelCardTotalsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Deletes a fuel card. */
    override suspend fun deleteFuelCardReport(request: DeleteFuelCardReportRequest): DeleteFuelCardReportResponse {
        return handleDeleteFuelCardReport(request)
    }

    /** Returns a fuel card. */
    override suspend fun fetchFuelCardReport(request: FetchFuelCardReportRequest): FetchFuelCardReportResponse = handleFetchFuelCardReport(request)

    /** Generates a fuel card. */
    override suspend fun generateFuelCardReport(request: GenerateFuelCardReportRequest): GenerateFuelCardReportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardReportingService/GenerateFuelCardReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GenerateFuelCardReport", request)
            log.atInfo().addKeyValue("operation", "GenerateFuelCardReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCardId).log("Recorded audit detail")
            val outbound0 = normalizeFuelPolicyRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = fuelFuelPolicyValidationService.normalizeFuelPolicy(envelope0.unpack(NormalizeFuelPolicyRequest::class.java))
            val outbound1 = mapFuelCardImportColumnsRequest {
                this.context = request.context
                this.fuelCard = fuelCard {
                    this.contact = contactInfo {
                        this.email = reply0.fuelPolicy.owner.email
                    }
                }
            }
            val reply1 = fuelFuelCardImportService.mapFuelCardImportColumns(WireCopies.json(outbound1))
            val outbound2 = GenerateFuelCardReport2Command(
                context = request.context,
                yardOwnerEmail = reply1.fuelCard.holder.email,
                yardAuditUpdatedBy = reply0.fuelPolicy.audit.updatedBy
            ).toRequest()
            val reply2 = yardsYardApprovalService.commentYardApproval(outbound2)
            val outbound3 = advanceYardMoveWorkflowRequest {
                this.context = request.context
                this.yardMove = yardMove {
                    this.assignee = userRef {
                        this.email = reply0.fuelPolicy.owner.email
                    }
                    this.audit = auditInfo {
                        this.createdAt = reply1.fuelCard.audit.createdAt
                    }
                }
            }
            val command3: Submission<AdvanceYardMoveWorkflowRequest> = Submission.Live(outbound3)
            val payload3 = when (val command = command3) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply3 = yardsYardMoveWorkflowService.advanceYardMoveWorkflow(payload3)
            generateFuelCardReportResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply1.result.detailsMap)
                    this.referenceIds.addAll((reply1.result.referenceIdsList) + (reply3.result.referenceIdsList))
                    this.status = reply3.outcome.status
                }
            }
        }

    /** Lists fuel cards a page at a time. */
    override suspend fun listFuelCardReports(request: ListFuelCardReportsRequest): ListFuelCardReportsResponse {
        return handleListFuelCardReports(request)
    }

    /** Schedules a fuel card. */
    override suspend fun scheduleFuelCardReport(request: ScheduleFuelCardReportRequest): ScheduleFuelCardReportResponse = handleScheduleFuelCardReport(request)

    /** Shares a fuel card. */
    override suspend fun shareFuelCardReport(request: ShareFuelCardReportRequest): ShareFuelCardReportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardReportingService/ShareFuelCardReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ShareFuelCardReport", request)
            log.atInfo().addKeyValue("operation", "ShareFuelCardReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.recipient.email).log("Recorded audit detail")
            val outbound0 = streamFuelCardExportRequest {
                this.context = request.context
                this.filter = StreamFuelCardExportRequestKt.filter {
                    this.createdAfter = request.context.deadline
                    this.labels.putAll(request.annotationsMap)
                    this.ownerId = request.details.templateId
                }
            }
            fuelFuelCardExportService.streamFuelCardExport(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = ListMetricSeriesesRequest.newBuilder().apply {
                setContext(request.context)
                setFilter(ListMetricSeriesesRequestKt.filter {
                    this.createdAfter = request.context.deadline
                    this.labels.putAll(request.details.variablesMap)
                    this.ownerId = request.details.templateId
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> metricsMetricSeriesService.listMetricSerieses(value) }
            val outbound2 = RequestFactory.create {
                validateVenueRequest {
                    this.context = request.context
                    this.details = ValidateVenueRequestKt.details {
                        this.contact = contactInfo {
                            this.name = request.details.recipient.name
                            this.phone = request.details.recipient.phone
                            this.preferredChannel = request.details.recipient.preferredChannel
                        }
                    }
                }
            }
            val reply2 = venuesVenueValidationService.validateVenue(outbound2)
            val outbound3 = checkZoneEligibilityRequest {
                this.context = request.context
                this.zone = zone {
                    this.children.addAll(listOf(zone {
                        this.children.addAll(listOf(zone {
                            this.audit = auditInfo {
                                this.etag = reply2.venue.audit.etag
                            }
                            this.manager = userRef {
                                this.email = reply2.venue.owner.email
                            }
                        }))
                    }))
                }
            }
            val reply3 = ClientRegistry.lookup("acme.dispatch.v1.ZoneValidationService", ZoneValidationServiceGrpcKt.ZoneValidationServiceCoroutineStub::class.java).checkZoneEligibility(outbound3)
            shareFuelCardReportResponse {
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.updatedBy = reply3.zone.audit.updatedBy
                    }
                    this.contact = contactInfo {
                        this.preferredChannel = request.details.recipient.preferredChannel
                    }
                    this.fuelCardId = request.fuelCardId
                }
                this.outcome = ShareFuelCardReportResponseKt.outcome {
                    this.referenceId = reply2.outcome.referenceId
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.message = reply3.outcome.message
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    /** Streams the changes of fuel cards as they happen. */
    override fun streamFuelCardMetrics(request: StreamFuelCardMetricsRequest): Flow<StreamFuelCardMetricsResponse> = flow {
        emit(handleStreamFuelCardMetrics(request))
    }

    private suspend fun handleDeleteFuelCardReport(request: DeleteFuelCardReportRequest): DeleteFuelCardReportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardReportingService/DeleteFuelCardReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DeleteFuelCardReport", request)
            log.atInfo().addKeyValue("operation", "DeleteFuelCardReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCardId).log("Recorded audit detail")
            val outbound0 = batchGetDriversRequest {}.driversCopy {
                this.context = request.context
            }
            val reply0 = driversDriverService.batchGetDrivers(outbound0)
            deleteFuelCardReportResponse {
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    private fun FetchFuelCardReportRequest.toNormalizeFuelPolicyFromFetchFuelCardReport0(): NormalizeFuelPolicyRequest {
        val source = this
        return normalizeFuelPolicyRequest {
            this.context = source.context
            this.details = NormalizeFuelPolicyRequestKt.details {
                this.contact = contactInfo {
                    this.email = source.context.actor.email
                }
            }
        }
    }

    private suspend fun handleFetchFuelCardReport(request: FetchFuelCardReportRequest): FetchFuelCardReportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardReportingService/FetchFuelCardReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "FetchFuelCardReport", request)
            log.atInfo().addKeyValue("operation", "FetchFuelCardReport").log("Handling transport request")
            val outbound0 = request.toNormalizeFuelPolicyFromFetchFuelCardReport0()
            val reply0 = fuelFuelPolicyValidationService.normalizeFuelPolicy(outbound0)
            val outbound1 = delegateProofOfDeliveryApprovalRequest {
                this.context = request.context
                this.proofOfDelivery = proofOfDelivery {
                    this.counterparty = driverRef {
                        this.email = reply0.fuelPolicy.owner.email
                    }
                    this.lines.addAll(listOf(proofOfDeliveryLine {
                        this.tax = proofOfDeliveryLineTax {
                            this.amount = when (reply0.fuelPolicy.actionCase) {
                                FuelPolicy.ActionCase.DISCOUNT -> reply0.fuelPolicy.discount.amount
                                else -> Money.getDefaultInstance()
                            }
                        }
                    }))
                }
            }
            val reply1 = deliveriesProofOfDeliveryApprovalService.delegateProofOfDeliveryApproval(outbound1)
            fetchFuelCardReportResponse {
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.createdBy = reply0.fuelPolicy.audit.createdBy
                        this.etag = reply0.fuelPolicy.audit.etag
                    }
                    this.fuelCardId = request.fuelCardId
                }
                this.result = operationResult {
                    this.message = reply1.outcome.message
                    this.status = "ok"
                }
            }
        }

    private data class GenerateFuelCardReport2Command(
        val context: RequestContext,
        val yardOwnerEmail: String,
        val yardAuditUpdatedBy: String
    ) {
        fun toRequest(): CommentYardApprovalRequest {
            val command = this
            return commentYardApprovalRequest {
                this.context = command.context
                this.yard = yard {
                    this.audit = auditInfo {
                        this.updatedBy = command.yardAuditUpdatedBy
                    }
                    this.owner = userRef {
                        this.email = command.yardOwnerEmail
                    }
                }
            }
        }
    }

    private suspend fun handleListFuelCardReports(request: ListFuelCardReportsRequest): ListFuelCardReportsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardReportingService/ListFuelCardReports") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListFuelCardReports", request)
            log.atInfo().addKeyValue("operation", "ListFuelCardReports").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            listFuelCardReportsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleScheduleFuelCardReport(request: ScheduleFuelCardReportRequest): ScheduleFuelCardReportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardReportingService/ScheduleFuelCardReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScheduleFuelCardReport", request)
            log.atInfo().addKeyValue("operation", "ScheduleFuelCardReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCardId).log("Recorded audit detail")
            val outbound0 = advanceProofOfDeliveryWorkflowRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.details = AdvanceProofOfDeliveryWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
                this.proofOfDelivery = proofOfDelivery {
                    this.lines.addAll(listOf(proofOfDeliveryLine {
                        this.attributes.putAll(request.details.filtersMap)
                    }))
                }
                this.reason = request.reason
            }
            val adapter0 = OutboundPort<AdvanceProofOfDeliveryWorkflowRequest, AdvanceProofOfDeliveryWorkflowResponse> { payload ->
                deliveriesProofOfDeliveryWorkflowService.advanceProofOfDeliveryWorkflow(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = listFuelTransactionConflictsRequest {
                this.context = request.context
                this.filter = ListFuelTransactionConflictsRequestKt.filter {
                    this.createdAfter = reply0.proofOfDelivery.dueAt
                    this.ownerId = reply0.outcome.referenceId
                    this.region = reply0.proofOfDelivery.billingAddress.region
                }
            }
            val reply1 = invokeScheduleFuelCardReport1(outbound1)
            scheduleFuelCardReportResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleStreamFuelCardMetrics(request: StreamFuelCardMetricsRequest): StreamFuelCardMetricsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardReportingService/StreamFuelCardMetrics") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StreamFuelCardMetrics", request)
            log.atInfo().addKeyValue("operation", "StreamFuelCardMetrics").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.ownerId).log("Recorded audit detail")
            val outbound0 = exportFuelCardsRequest {
                this.context = request.context
                this.filter = ExportFuelCardsRequestKt.filter {
                    this.createdAfter = request.filter.createdAfter
                    this.labels.putAll(request.filter.labelsMap)
                    this.query = request.filter.query
                    this.region = request.filter.region
                }
            }
            fuelFuelCardQueryService.exportFuelCards(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = comparePreferenceOptionsRequest {
                this.context = request.context
                this.preference = preference {
                    this.owner = userRef {
                        this.email = request.subscriber.email
                    }
                }
            }
            val reply1 = workerForStreamFuelCardMetrics1.submit(outbound1)
            streamFuelCardMetricsResponse {
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.createdBy = reply1.preference.audit.createdBy
                    }
                    this.contact = contactInfo {
                        this.name = request.subscriber.name
                        this.phone = request.subscriber.phone
                    }
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.status = "ok"
                }
            }
        }
}
