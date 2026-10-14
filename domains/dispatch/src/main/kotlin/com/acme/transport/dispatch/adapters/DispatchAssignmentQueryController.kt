package com.acme.transport.dispatch.adapters

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.pageInfo
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.customerRef
import com.acme.deliveries.v1.DeliveryServiceGrpcKt
import com.acme.deliveries.v1.ListDeliveriesRequest
import com.acme.deliveries.v1.ListDeliveriesRequestKt
import com.acme.deliveries.v1.listDeliveriesRequest
import com.acme.dispatch.v1.BrowseDispatchAssignmentsRequest
import com.acme.dispatch.v1.BrowseDispatchAssignmentsResponse
import com.acme.dispatch.v1.CountDispatchAssignmentsRequest
import com.acme.dispatch.v1.CountDispatchAssignmentsResponse
import com.acme.dispatch.v1.DispatchAssignmentQueryServiceGrpcKt
import com.acme.dispatch.v1.DispatchAssignmentValidationServiceGrpcKt
import com.acme.dispatch.v1.DispatchOrderQueryServiceGrpcKt
import com.acme.dispatch.v1.ExportDispatchAssignmentsRequest
import com.acme.dispatch.v1.ExportDispatchAssignmentsResponse
import com.acme.dispatch.v1.FindDispatchAssignmentsRequest
import com.acme.dispatch.v1.FindDispatchAssignmentsResponse
import com.acme.dispatch.v1.LookupDispatchAssignmentRequest
import com.acme.dispatch.v1.LookupDispatchAssignmentResponse
import com.acme.dispatch.v1.NormalizeDispatchAssignmentRequestKt
import com.acme.dispatch.v1.PeekDispatchAssignmentRequest
import com.acme.dispatch.v1.PeekDispatchAssignmentResponse
import com.acme.dispatch.v1.PreviewZoneRequest
import com.acme.dispatch.v1.ResolveDispatchAssignmentsRequest
import com.acme.dispatch.v1.ResolveDispatchAssignmentsResponse
import com.acme.dispatch.v1.SummarizeDispatchAssignmentsRequest
import com.acme.dispatch.v1.SummarizeDispatchAssignmentsResponse
import com.acme.dispatch.v1.SummarizeDispatchOrdersRequestKt
import com.acme.dispatch.v1.ZoneCalculationServiceGrpcKt
import com.acme.dispatch.v1.ZoneValidationServiceGrpcKt
import com.acme.dispatch.v1.browseDispatchAssignmentsResponse
import com.acme.dispatch.v1.countDispatchAssignmentsResponse
import com.acme.dispatch.v1.dispatchAssignment
import com.acme.dispatch.v1.explainZoneCalculationRequest
import com.acme.dispatch.v1.exportDispatchAssignmentsResponse
import com.acme.dispatch.v1.findDispatchAssignmentsResponse
import com.acme.dispatch.v1.lookupDispatchAssignmentResponse
import com.acme.dispatch.v1.normalizeDispatchAssignmentRequest
import com.acme.dispatch.v1.peekDispatchAssignmentResponse
import com.acme.dispatch.v1.previewZoneRequest
import com.acme.dispatch.v1.resolveDispatchAssignmentsResponse
import com.acme.dispatch.v1.summarizeDispatchAssignmentsResponse
import com.acme.dispatch.v1.summarizeDispatchOrdersRequest
import com.acme.dispatch.v1.zone
import com.acme.fuel.v1.FuelTransactionCalculationServiceGrpcKt
import com.acme.fuel.v1.RecalculateFuelTransactionsRequest
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.fuelTransactionMeasurement
import com.acme.fuel.v1.recalculateFuelTransactionsRequest
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.CloneLedgerAccountRequestKt
import com.acme.ledger.v1.LedgerAccountServiceGrpcKt
import com.acme.ledger.v1.cloneLedgerAccountRequest
import com.acme.ledger.v1.ledgerAccount
import com.acme.metrics.v1.ExplainMetricSeriesDecisionRequestKt
import com.acme.metrics.v1.MetricSeriesValidationServiceGrpcKt
import com.acme.metrics.v1.copy as metricsCopy
import com.acme.metrics.v1.explainMetricSeriesDecisionRequest
import com.acme.metrics.v1.metricSeries
import com.acme.seating.v1.SeatMapCalculationServiceGrpcKt
import com.acme.seating.v1.quoteSeatMapRequest
import com.acme.seating.v1.seatMap
import com.acme.telematics.v1.BrowseTelemetryReadingsRequest
import com.acme.telematics.v1.BrowseTelemetryReadingsRequestKt
import com.acme.telematics.v1.TelemetryReadingQueryServiceGrpcKt
import com.acme.telematics.v1.browseTelemetryReadingsRequest
import com.acme.transport.dispatch.DispatchRepository
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.google.protobuf.Any as ProtoAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Answers read-only questions about dispatch assignments, for screens and reports. */
class DispatchAssignmentQueryController(
    private val store: DispatchRepository,
    private val workers: CoroutineScope,
    private val deliveriesDeliveryService: DeliveryServiceGrpcKt.DeliveryServiceCoroutineStub,
    private val dispatchDispatchAssignmentValidationService: DispatchAssignmentValidationServiceGrpcKt.DispatchAssignmentValidationServiceCoroutineStub,
    private val dispatchDispatchOrderQueryService: DispatchOrderQueryServiceGrpcKt.DispatchOrderQueryServiceCoroutineStub,
    private val dispatchZoneCalculationService: ZoneCalculationServiceGrpcKt.ZoneCalculationServiceCoroutineStub,
    private val dispatchZoneValidationService: ZoneValidationServiceGrpcKt.ZoneValidationServiceCoroutineStub,
    private val fuelFuelTransactionCalculationService: FuelTransactionCalculationServiceGrpcKt.FuelTransactionCalculationServiceCoroutineStub,
    private val ledgerLedgerAccountService: LedgerAccountServiceGrpcKt.LedgerAccountServiceCoroutineStub,
    private val metricsMetricSeriesValidationService: MetricSeriesValidationServiceGrpcKt.MetricSeriesValidationServiceCoroutineStub,
    private val seatingSeatMapCalculationService: SeatMapCalculationServiceGrpcKt.SeatMapCalculationServiceCoroutineStub,
    private val telematicsTelemetryReadingQueryService: TelemetryReadingQueryServiceGrpcKt.TelemetryReadingQueryServiceCoroutineStub,
) : DispatchAssignmentQueryServiceGrpcKt.DispatchAssignmentQueryServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DispatchAssignmentQueryController::class.java)

    /** Lists dispatch assignments a page at a time. */
    override suspend fun browseDispatchAssignments(request: BrowseDispatchAssignmentsRequest): BrowseDispatchAssignmentsResponse = handleBrowseDispatchAssignments(request)

    /** Counts dispatch assignments by status. */
    override suspend fun countDispatchAssignments(request: CountDispatchAssignmentsRequest): CountDispatchAssignmentsResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentQueryService/CountDispatchAssignments") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CountDispatchAssignments", request)
            log.atInfo().addKeyValue("operation", "CountDispatchAssignments").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = explainZoneCalculationRequest {
                this.context = request.context
                this.zone = zone {
                    this.children.addAll(listOf(zone {
                        this.children.addAll(listOf(zone {
                            this.audit = auditInfo {
                                this.updatedAt = request.filter.createdAfter
                            }
                            this.location = postalAddress {
                                this.region = request.filter.region
                            }
                            this.manager = userRef {
                                this.email = request.context.actor.email
                            }
                        }))
                        this.manager = userRef {
                            this.userId = request.context.actor.userId
                        }
                    }))
                }
            }
            val reply0 = withTimeout(5_000) { dispatchZoneCalculationService.explainZoneCalculation(outbound0) }
            countDispatchAssignmentsResponse {
                this.result = operationResult {
                    this.message = reply0.outcome.message
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    /** Streams every matching dispatch assignment. */
    override fun exportDispatchAssignments(request: ExportDispatchAssignmentsRequest): Flow<ExportDispatchAssignmentsResponse> = flow {
        emit(handleExportDispatchAssignments(request))
    }

    /** Lists dispatch assignments a page at a time. */
    override suspend fun findDispatchAssignments(request: FindDispatchAssignmentsRequest): FindDispatchAssignmentsResponse = handleFindDispatchAssignments(request)

    /** Returns a dispatch assignment. */
    override suspend fun lookupDispatchAssignment(request: LookupDispatchAssignmentRequest): LookupDispatchAssignmentResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentQueryService/LookupDispatchAssignment") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LookupDispatchAssignment", request)
            log.atInfo().addKeyValue("operation", "LookupDispatchAssignment").log("Handling transport request")
            val outbound0 = explainMetricSeriesDecisionRequest {}.metricsCopy {
                this.context = request.context
                this.details = ExplainMetricSeriesDecisionRequestKt.details {
                    this.locale = request.context.locale
                }
                this.metricSeries = metricSeries {
                    this.owner = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply0 = metricsMetricSeriesValidationService.explainMetricSeriesDecision(outbound0)
            lookupDispatchAssignmentResponse {
                this.dispatchAssignment = dispatchAssignment {
                    this.assignee = userRef {
                        this.userId = reply0.metricSeries.owner.userId
                    }
                    this.dispatchAssignmentId = request.dispatchAssignmentId
                }
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }

    /** Returns a dispatch assignment. */
    override suspend fun peekDispatchAssignment(request: PeekDispatchAssignmentRequest): PeekDispatchAssignmentResponse {
        return handlePeekDispatchAssignment(request)
    }

    /** Returns several dispatch assignments at once. */
    override suspend fun resolveDispatchAssignments(request: ResolveDispatchAssignmentsRequest): ResolveDispatchAssignmentsResponse = handleResolveDispatchAssignments(request)

    /** Summarizes a dispatch assignment. */
    override suspend fun summarizeDispatchAssignments(request: SummarizeDispatchAssignmentsRequest): SummarizeDispatchAssignmentsResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentQueryService/SummarizeDispatchAssignments") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SummarizeDispatchAssignments", request)
            log.atInfo().addKeyValue("operation", "SummarizeDispatchAssignments").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchAssignmentId).log("Recorded audit detail")
            val outbound0 = summarizeDispatchOrdersRequest {
                this.context = request.context
                this.details = SummarizeDispatchOrdersRequestKt.details {
                    this.cron = request.details.cron
                    this.filters.putAll(request.details.filtersMap)
                }
            }
            val reply0 = dispatchDispatchOrderQueryService.summarizeDispatchOrders(WireCopies.json(outbound0))
            val outbound1 = SummarizeDispatchAssignments1Command(
                context = request.context,
                zoneChildrenChildrenManagerEmail = request.context.actor.email
            ).toRequest()
            val reply1 = dispatchZoneValidationService.previewZone(outbound1)
            val outbound2 = listDeliveriesRequest {
                this.context = request.context
                this.filter = ListDeliveriesRequestKt.filter {
                    this.createdAfter = reply1.zone.audit.updatedAt
                    this.region = reply1.zone.location.region
                }
            }
            val command2: Submission<ListDeliveriesRequest> = Submission.Live(outbound2)
            val payload2 = when (val command = command2) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply2 = deliveriesDeliveryService.listDeliveries(payload2)
            summarizeDispatchAssignmentsResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = "ok"
                }
                this.summary.putAll(reply0.summaryMap)
            }
        }

    private suspend fun handleBrowseDispatchAssignments(request: BrowseDispatchAssignmentsRequest): BrowseDispatchAssignmentsResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentQueryService/BrowseDispatchAssignments") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BrowseDispatchAssignments", request)
            log.atInfo().addKeyValue("operation", "BrowseDispatchAssignments").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            browseDispatchAssignmentsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleExportDispatchAssignments(request: ExportDispatchAssignmentsRequest): ExportDispatchAssignmentsResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentQueryService/ExportDispatchAssignments") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExportDispatchAssignments", request)
            log.atInfo().addKeyValue("operation", "ExportDispatchAssignments").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val configure0: CloneLedgerAccountRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.ledgerAccount = ledgerAccount {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                    this.labels.putAll(request.filter.labelsMap)
                }
            }
            val outbound0 = cloneLedgerAccountRequest(configure0)
            val reply0 = ledgerLedgerAccountService.cloneLedgerAccount(outbound0)
            val outbound1 = summarizeDispatchOrdersRequest {
                this.annotations.putAll(request.filter.labelsMap)
                this.context = request.context
                this.details = SummarizeDispatchOrdersRequestKt.details {
                    this.filters.putAll(reply0.result.detailsMap)
                    this.format = request.format
                }
            }
            val reply1 = dispatchDispatchOrderQueryService.summarizeDispatchOrders(outbound1)
            exportDispatchAssignmentsResponse {
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleFindDispatchAssignments(request: FindDispatchAssignmentsRequest): FindDispatchAssignmentsResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentQueryService/FindDispatchAssignments") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "FindDispatchAssignments", request)
            log.atInfo().addKeyValue("operation", "FindDispatchAssignments").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = normalizeDispatchAssignmentRequest {
                this.context = request.context
                this.details = NormalizeDispatchAssignmentRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                    this.locale = request.context.locale
                }
            }
            val reply0 = dispatchDispatchAssignmentValidationService.normalizeDispatchAssignment(outbound0)
            findDispatchAssignmentsResponse {
                this.dispatchAssignments.addAll(listOf(dispatchAssignment {
                    this.assignee = userRef {
                        this.email = reply0.dispatchAssignment.assignee.email
                    }
                    this.priority = reply0.dispatchAssignment.priority
                }))
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private fun PeekDispatchAssignmentRequest.toRecalculateFuelTransactionsFromPeekDispatchAssignment0(): RecalculateFuelTransactionsRequest {
        val source = this
        return recalculateFuelTransactionsRequest {
            this.context = source.context
            this.fuelTransaction = fuelTransaction {
                this.attributes.putAll(source.context.baggageMap)
                this.measurements.addAll(listOf(fuelTransactionMeasurement {
                    this.observedAt = source.context.deadline
                }))
                this.source = source.dispatchAssignmentId
                this.subject = customerRef {
                    this.email = source.context.actor.email
                }
            }
        }
    }

    private suspend fun handlePeekDispatchAssignment(request: PeekDispatchAssignmentRequest): PeekDispatchAssignmentResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentQueryService/PeekDispatchAssignment") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PeekDispatchAssignment", request)
            log.atInfo().addKeyValue("operation", "PeekDispatchAssignment").log("Handling transport request")
            val outbound0 = request.toRecalculateFuelTransactionsFromPeekDispatchAssignment0()
            val reply0 = fuelFuelTransactionCalculationService.recalculateFuelTransactions(outbound0)
            peekDispatchAssignmentResponse {
                this.dispatchAssignment = dispatchAssignment {
                    this.dispatchAssignmentId = request.dispatchAssignmentId
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleResolveDispatchAssignments(request: ResolveDispatchAssignmentsRequest): ResolveDispatchAssignmentsResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentQueryService/ResolveDispatchAssignments") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResolveDispatchAssignments", request)
            log.atInfo().addKeyValue("operation", "ResolveDispatchAssignments").log("Handling transport request")
            val outbound0 = quoteSeatMapRequest {
                this.context = request.context
                this.seatMap = seatMap {
                    this.children.addAll(listOf(seatMap {
                        this.children.addAll(listOf(seatMap {
                            this.manager = userRef {
                                this.userId = request.context.actor.userId
                            }
                        }))
                    }))
                }
            }
            val reply0 = seatingSeatMapCalculationService.quoteSeatMap(outbound0)
            val outbound1 = browseTelemetryReadingsRequest {
                this.context = request.context
                this.filter = BrowseTelemetryReadingsRequestKt.filter {
                    this.createdAfter = reply0.seatMap.audit.createdAt
                    this.labels.putAll(reply0.result.detailsMap)
                    this.ownerId = reply0.seatMap.manager.userId
                    this.region = reply0.seatMap.location.region
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = telematicsTelemetryReadingQueryService.browseTelemetryReadings(envelope1.unpack(BrowseTelemetryReadingsRequest::class.java))
            resolveDispatchAssignmentsResponse {
                this.dispatchAssignments.addAll(listOf(dispatchAssignment {
                    this.assignee = userRef {
                        this.displayName = reply0.seatMap.manager.displayName
                    }
                    this.audit = auditInfo {
                        this.etag = reply0.seatMap.audit.etag
                    }
                }))
                this.pageInfo = pageInfo {
                    this.totalSize = reply1.pageInfo.totalSize
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private data class SummarizeDispatchAssignments1Command(
        val context: RequestContext,
        val zoneChildrenChildrenManagerEmail: String
    ) {
        fun toRequest(): PreviewZoneRequest {
            val command = this
            return previewZoneRequest {
                this.context = command.context
                this.zone = zone {
                    this.children.addAll(listOf(zone {
                        this.children.addAll(listOf(zone {
                            this.manager = userRef {
                                this.email = command.zoneChildrenChildrenManagerEmail
                            }
                        }))
                    }))
                }
            }
        }
    }
}
