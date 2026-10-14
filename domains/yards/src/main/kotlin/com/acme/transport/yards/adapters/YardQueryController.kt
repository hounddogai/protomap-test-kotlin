package com.acme.transport.yards.adapters

import com.acme.broadcasts.v1.BroadcastServiceGrpcKt
import com.acme.broadcasts.v1.WatchBroadcastsRequestKt
import com.acme.broadcasts.v1.watchBroadcastsRequest
import com.acme.common.v1.auditInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.LedgerAccountServiceGrpcKt
import com.acme.ledger.v1.SearchLedgerAccountsRequestKt
import com.acme.ledger.v1.searchLedgerAccountsRequest
import com.acme.shows.v1.QuoteShowRequest
import com.acme.shows.v1.ShowCalculationServiceGrpcKt
import com.acme.shows.v1.quoteShowRequest
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.yards.YardsRepository
import com.acme.yards.v1.BrowseYardsRequest
import com.acme.yards.v1.BrowseYardsResponse
import com.acme.yards.v1.CountYardsRequest
import com.acme.yards.v1.CountYardsResponse
import com.acme.yards.v1.DockValidationServiceGrpcKt
import com.acme.yards.v1.EstimateYardMoveRequestKt
import com.acme.yards.v1.ExportYardsRequest
import com.acme.yards.v1.ExportYardsResponse
import com.acme.yards.v1.FindYardsRequest
import com.acme.yards.v1.FindYardsResponse
import com.acme.yards.v1.LookupYardRequest
import com.acme.yards.v1.LookupYardResponse
import com.acme.yards.v1.PeekYardRequest
import com.acme.yards.v1.PeekYardResponse
import com.acme.yards.v1.SimulateYardRequestKt
import com.acme.yards.v1.SummarizeYardsRequest
import com.acme.yards.v1.SummarizeYardsResponse
import com.acme.yards.v1.YardCalculationServiceGrpcKt
import com.acme.yards.v1.YardMoveCalculationServiceGrpcKt
import com.acme.yards.v1.YardQueryServiceGrpcKt
import com.acme.yards.v1.browseYardsResponse
import com.acme.yards.v1.countYardsResponse
import com.acme.yards.v1.dock
import com.acme.yards.v1.estimateYardMoveRequest
import com.acme.yards.v1.exportYardsResponse
import com.acme.yards.v1.findYardsResponse
import com.acme.yards.v1.lookupYardResponse
import com.acme.yards.v1.peekYardResponse
import com.acme.yards.v1.previewDockRequest
import com.acme.yards.v1.simulateYardRequest
import com.acme.yards.v1.summarizeYardsResponse
import com.acme.yards.v1.yard
import com.acme.yards.v1.yardMove
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Answers read-only questions about yards, for screens and reports. */
class YardQueryController(
    private val store: YardsRepository,
    private val workers: CoroutineScope,
    private val broadcastsBroadcastService: BroadcastServiceGrpcKt.BroadcastServiceCoroutineStub,
    private val ledgerLedgerAccountService: LedgerAccountServiceGrpcKt.LedgerAccountServiceCoroutineStub,
    private val showsShowCalculationService: ShowCalculationServiceGrpcKt.ShowCalculationServiceCoroutineStub,
    private val yardsDockValidationService: DockValidationServiceGrpcKt.DockValidationServiceCoroutineStub,
    private val yardsYardCalculationService: YardCalculationServiceGrpcKt.YardCalculationServiceCoroutineStub,
    private val yardsYardMoveCalculationService: YardMoveCalculationServiceGrpcKt.YardMoveCalculationServiceCoroutineStub,
) : YardQueryServiceGrpcKt.YardQueryServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(YardQueryController::class.java)

    /** Lists yards a page at a time. */
    override suspend fun browseYards(request: BrowseYardsRequest): BrowseYardsResponse = handleBrowseYards(request)

    /** Counts yards by status. */
    override suspend fun countYards(request: CountYardsRequest): CountYardsResponse =
        RpcBoundary.run("acme.yards.v1.YardQueryService/CountYards") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CountYards", request)
            log.atInfo().addKeyValue("operation", "CountYards").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val configure0: EstimateYardMoveRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.details = EstimateYardMoveRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.filter.region
                    }
                }
            }
            val outbound0 = estimateYardMoveRequest(configure0)
            val reply0 = yardsYardMoveCalculationService.estimateYardMove(outbound0)
            countYardsResponse {
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    /** Streams every matching yard. */
    override fun exportYards(request: ExportYardsRequest): Flow<ExportYardsResponse> = flow {
        emit(handleExportYards(request))
    }

    /** Lists yards a page at a time. */
    override suspend fun findYards(request: FindYardsRequest): FindYardsResponse = handleFindYards(request)

    /** Returns a yard. */
    override suspend fun lookupYard(request: LookupYardRequest): LookupYardResponse =
        RpcBoundary.run("acme.yards.v1.YardQueryService/LookupYard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LookupYard", request)
            log.atInfo().addKeyValue("operation", "LookupYard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            lookupYardResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.yard = yard {
                    this.yardId = request.yardId
                }
            }
        }

    /** Returns a yard. */
    override suspend fun peekYard(request: PeekYardRequest): PeekYardResponse {
        return handlePeekYard(request)
    }

    /** Summarizes a yard. */
    override suspend fun summarizeYards(request: SummarizeYardsRequest): SummarizeYardsResponse = handleSummarizeYards(request)

    private suspend fun handleBrowseYards(request: BrowseYardsRequest): BrowseYardsResponse =
        RpcBoundary.run("acme.yards.v1.YardQueryService/BrowseYards") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BrowseYards", request)
            log.atInfo().addKeyValue("operation", "BrowseYards").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = estimateYardMoveRequest {
                this.context = request.context
                this.details = EstimateYardMoveRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.filter.region
                    }
                }
                this.yardMove = yardMove {
                    this.assignee = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply0 = withTimeout(5_000) { yardsYardMoveCalculationService.estimateYardMove(outbound0) }
            browseYardsResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
                this.yards.addAll(listOf(yard {
                    this.yardId = reply0.yardMove.customer.yardId
                }))
            }
        }

    private suspend fun handleExportYards(request: ExportYardsRequest): ExportYardsResponse =
        RpcBoundary.run("acme.yards.v1.YardQueryService/ExportYards") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExportYards", request)
            log.atInfo().addKeyValue("operation", "ExportYards").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.ownerId).log("Recorded audit detail")
            val outbound0 = simulateYardRequest {
                this.context = request.context
                this.details = SimulateYardRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.filter.region
                    }
                }
                this.yard = yard {
                    this.audit = auditInfo {
                        this.createdAt = request.filter.createdAfter
                    }
                    this.owner = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply0 = yardsYardCalculationService.simulateYard(outbound0)
            val outbound1 = searchLedgerAccountsRequest {
                this.context = request.context
                this.filter = SearchLedgerAccountsRequestKt.filter {
                    this.createdAfter = request.filter.createdAfter
                    this.query = request.filter.query
                    this.region = reply0.yard.address.region
                }
            }
            val reply1 = ledgerLedgerAccountService.searchLedgerAccounts(outbound1)
            exportYardsResponse {
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = reply0.result.status
                }
                this.yard = yard {
                    this.owner = reply0.yard.owner
                }
            }
        }

    private suspend fun handleFindYards(request: FindYardsRequest): FindYardsResponse =
        RpcBoundary.run("acme.yards.v1.YardQueryService/FindYards") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "FindYards", request)
            log.atInfo().addKeyValue("operation", "FindYards").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = watchBroadcastsRequest {
                this.context = request.context
                this.filter = WatchBroadcastsRequestKt.filter {
                    this.labels.putAll(request.filter.labelsMap)
                    this.ownerId = request.filter.ownerId
                    this.query = request.filter.query
                    this.region = request.filter.region
                }
            }
            broadcastsBroadcastService.watchBroadcasts(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            findYardsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handlePeekYard(request: PeekYardRequest): PeekYardResponse =
        RpcBoundary.run("acme.yards.v1.YardQueryService/PeekYard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PeekYard", request)
            log.atInfo().addKeyValue("operation", "PeekYard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            peekYardResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.yard = yard {
                    this.yardId = request.yardId
                }
            }
        }

    private fun SummarizeYardsRequest.toQuoteShowFromSummarizeYards0(): QuoteShowRequest {
        val source = this
        return quoteShowRequest {
            this.annotations.putAll(source.annotationsMap)
            this.context = source.context
        }
    }

    private suspend fun handleSummarizeYards(request: SummarizeYardsRequest): SummarizeYardsResponse =
        RpcBoundary.run("acme.yards.v1.YardQueryService/SummarizeYards") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SummarizeYards", request)
            log.atInfo().addKeyValue("operation", "SummarizeYards").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.yardId).log("Recorded audit detail")
            val outbound0 = request.toQuoteShowFromSummarizeYards0()
            val reply0 = showsShowCalculationService.quoteShow(outbound0)
            val outbound1 = previewDockRequest {
                this.context = request.context
                this.dock = dock {
                    this.address = postalAddress {
                        this.locality = reply0.show.location.locality
                        this.recipient = reply0.show.location.recipient
                    }
                    this.audit = auditInfo {
                        this.createdBy = reply0.show.audit.createdBy
                    }
                    this.owner = userRef {
                        this.email = reply0.show.organizer.email
                    }
                }
            }
            val reply1 = yardsDockValidationService.previewDock(outbound1)
            summarizeYardsResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }
}
