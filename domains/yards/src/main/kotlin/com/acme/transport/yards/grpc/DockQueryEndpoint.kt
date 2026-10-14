package com.acme.transport.yards.grpc

import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.identity.v1.userRef
import com.acme.metrics.v1.MetricSeriesServiceGrpcKt
import com.acme.metrics.v1.metricSeries
import com.acme.metrics.v1.metricSeriesComponent
import com.acme.metrics.v1.updateMetricSeriesRequest
import com.acme.seating.v1.SeatMapQueryServiceGrpcKt
import com.acme.seating.v1.SuggestSeatMapsRequestKt
import com.acme.seating.v1.suggestSeatMapsRequest
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.transport.yards.YardsRepository
import com.acme.venues.v1.SummarizeVenuesRequest
import com.acme.venues.v1.SummarizeVenuesRequestKt
import com.acme.venues.v1.SummarizeVenuesResponse
import com.acme.venues.v1.VenueQueryServiceGrpcKt
import com.acme.venues.v1.summarizeVenuesRequest
import com.acme.yards.v1.BrowseDocksRequest
import com.acme.yards.v1.BrowseDocksResponse
import com.acme.yards.v1.CompareDockOptionsRequestKt
import com.acme.yards.v1.CountDocksRequest
import com.acme.yards.v1.CountDocksResponse
import com.acme.yards.v1.CountYardsRequestKt
import com.acme.yards.v1.DockCalculationServiceGrpcKt
import com.acme.yards.v1.DockComponent
import com.acme.yards.v1.DockQueryServiceGrpcKt
import com.acme.yards.v1.DockValidationServiceGrpcKt
import com.acme.yards.v1.FindDocksRequest
import com.acme.yards.v1.FindDocksResponse
import com.acme.yards.v1.LookupDockRequest
import com.acme.yards.v1.LookupDockResponse
import com.acme.yards.v1.PeekDockRequest
import com.acme.yards.v1.PeekDockResponse
import com.acme.yards.v1.PreviewDockRequest
import com.acme.yards.v1.PreviewDockRequestKt
import com.acme.yards.v1.SuggestDocksRequest
import com.acme.yards.v1.SuggestDocksResponse
import com.acme.yards.v1.SummarizeDocksRequest
import com.acme.yards.v1.SummarizeDocksResponse
import com.acme.yards.v1.ValidateDockRequestKt
import com.acme.yards.v1.ValidateYardRequest
import com.acme.yards.v1.ValidateYardResponse
import com.acme.yards.v1.YardQueryServiceGrpcKt
import com.acme.yards.v1.YardValidationServiceGrpcKt
import com.acme.yards.v1.browseDocksResponse
import com.acme.yards.v1.compareDockOptionsRequest
import com.acme.yards.v1.countDocksResponse
import com.acme.yards.v1.countYardsRequest
import com.acme.yards.v1.dock
import com.acme.yards.v1.dockDimensions
import com.acme.yards.v1.explainDockCalculationRequest
import com.acme.yards.v1.findDocksResponse
import com.acme.yards.v1.lookupDockResponse
import com.acme.yards.v1.peekDockResponse
import com.acme.yards.v1.previewDockRequest
import com.acme.yards.v1.suggestDocksResponse
import com.acme.yards.v1.summarizeDocksResponse
import com.acme.yards.v1.validateDockRequest
import com.acme.yards.v1.validateYardRequest
import com.acme.yards.v1.yard
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Answers read-only questions about docks, for screens and reports. */
class DockQueryEndpoint(
    private val store: YardsRepository,
    private val workers: CoroutineScope,
    private val metricsMetricSeriesService: MetricSeriesServiceGrpcKt.MetricSeriesServiceCoroutineStub,
    private val seatingSeatMapQueryService: SeatMapQueryServiceGrpcKt.SeatMapQueryServiceCoroutineStub,
    private val venuesVenueQueryService: VenueQueryServiceGrpcKt.VenueQueryServiceCoroutineStub,
    private val yardsDockCalculationService: DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub,
    private val yardsDockValidationService: DockValidationServiceGrpcKt.DockValidationServiceCoroutineStub,
    private val yardsYardQueryService: YardQueryServiceGrpcKt.YardQueryServiceCoroutineStub,
    private val yardsYardValidationService: YardValidationServiceGrpcKt.YardValidationServiceCoroutineStub,
) : DockQueryServiceGrpcKt.DockQueryServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DockQueryEndpoint::class.java)

    private val workerForLookupDock0 = ReplyMailbox<SummarizeVenuesRequest, SummarizeVenuesResponse>(workers) { payload ->
        withTimeout(5_000) { venuesVenueQueryService.summarizeVenues(payload) }
    }

    private val clientForPeekDock0 by lazy { seatingSeatMapQueryService }

    /** Lists docks a page at a time. */
    override suspend fun browseDocks(request: BrowseDocksRequest): BrowseDocksResponse =
        RpcBoundary.run("acme.yards.v1.DockQueryService/BrowseDocks") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BrowseDocks", request)
            log.atInfo().addKeyValue("operation", "BrowseDocks").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = validateYardRequest {
                this.context = request.context
                this.yard = yard {
                    this.address = postalAddress {
                        this.region = request.filter.region
                    }
                }
            }
            val invoke0: suspend (ValidateYardRequest, Metadata) -> ValidateYardResponse = yardsYardValidationService::validateYard
            val reply0 = invoke0(outbound0, Metadata())
            browseDocksResponse {
                this.docks.addAll(listOf(dock {
                    this.capacity = reply0.yard.capacity
                }))
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }

    /** Counts docks by status. */
    override suspend fun countDocks(request: CountDocksRequest): CountDocksResponse {
        return handleCountDocks(request)
    }

    /** Lists docks a page at a time. */
    override suspend fun findDocks(request: FindDocksRequest): FindDocksResponse = handleFindDocks(request)

    /** Returns a dock. */
    override suspend fun lookupDock(request: LookupDockRequest): LookupDockResponse =
        RpcBoundary.run("acme.yards.v1.DockQueryService/LookupDock") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LookupDock", request)
            log.atInfo().addKeyValue("operation", "LookupDock").log("Handling transport request")
            val outbound0 = summarizeVenuesRequest {
                this.context = request.context
                this.details = SummarizeVenuesRequestKt.details {
                    this.filters.putAll(request.context.baggageMap)
                }
            }
            val reply0 = workerForLookupDock0.submit(outbound0)
            lookupDockResponse {
                this.dock = dock {
                    this.dockId = request.dockId
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    /** Returns a dock. */
    override suspend fun peekDock(request: PeekDockRequest): PeekDockResponse {
        return handlePeekDock(request)
    }

    /** Lists docks a page at a time. */
    override suspend fun suggestDocks(request: SuggestDocksRequest): SuggestDocksResponse = handleSuggestDocks(request)

    /** Summarizes a dock. */
    override suspend fun summarizeDocks(request: SummarizeDocksRequest): SummarizeDocksResponse =
        RpcBoundary.run("acme.yards.v1.DockQueryService/SummarizeDocks") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SummarizeDocks", request)
            log.atInfo().addKeyValue("operation", "SummarizeDocks").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.cron).log("Recorded audit detail")
            val outbound0 = explainDockCalculationRequest {
                this.context = request.context
                this.dock = dock {
                    this.dockId = request.dockId
                }
            }
            val reply0 = flowOf(outbound0).map { value -> yardsDockCalculationService.explainDockCalculation(value) }.single()
            val outbound1 = summarizeVenuesRequest {
                this.context = request.context
                this.details = SummarizeVenuesRequestKt.details {
                    this.format = request.details.format
                    this.period = request.details.period
                    this.recipientEmails.addAll(request.details.recipientEmailsList)
                }
            }
            val reply1 = withTimeout(5_000) { venuesVenueQueryService.summarizeVenues(outbound1) }
            val configure2: ValidateDockRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.details = ValidateDockRequestKt.details {
                    this.contact = contactInfo {
                        this.email = reply0.dock.owner.email
                    }
                }
                this.dock = dock {
                    this.address = postalAddress {
                        this.postalCode = reply0.dock.address.postalCode
                    }
                }
            }
            val outbound2 = validateDockRequest(configure2)
            val reply2 = yardsDockValidationService.validateDock(outbound2)
            summarizeDocksResponse {
                this.period = reply1.period
                this.result = operationResult {
                    this.details.putAll(reply2.result.detailsMap)
                    this.referenceIds.addAll(reply2.result.referenceIdsList)
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleCountDocks(request: CountDocksRequest): CountDocksResponse =
        RpcBoundary.run("acme.yards.v1.DockQueryService/CountDocks") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CountDocks", request)
            log.atInfo().addKeyValue("operation", "CountDocks").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = PreviewDockRequest.newBuilder().apply {
                setContext(request.context)
                setDetails(PreviewDockRequestKt.details {
                    this.locale = request.context.locale
                })
                setDock(dock {
                    this.address = postalAddress {
                        this.region = request.filter.region
                    }
                    this.owner = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> yardsDockValidationService.previewDock(value) }
            val outbound1 = RequestFactory.create {
                countYardsRequest {
                    this.context = request.context
                    this.filter = CountYardsRequestKt.filter {
                        this.labels.putAll(request.filter.labelsMap)
                        this.ownerId = request.filter.ownerId
                        this.region = reply0.dock.address.region
                    }
                }
            }
            val reply1 = yardsYardQueryService.countYards(outbound1)
            countDocksResponse {
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleFindDocks(request: FindDocksRequest): FindDocksResponse =
        RpcBoundary.run("acme.yards.v1.DockQueryService/FindDocks") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "FindDocks", request)
            log.atInfo().addKeyValue("operation", "FindDocks").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = compareDockOptionsRequest {
                this.context = request.context
                this.details = CompareDockOptionsRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.filter.region
                    }
                }
                this.dock = dock {
                    this.owner = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply0 = ClientRegistry.lookup("acme.yards.v1.DockCalculationService", DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub::class.java).compareDockOptions(outbound0)
            val outbound1 = updateMetricSeriesRequest {
                this.context = request.context
                this.metricSeries = metricSeries {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.dock.audit.updatedBy
                    }
                    this.components.putAll(mapOf("primary" to metricSeriesComponent {
                        this.description = (reply0.dock.componentsMap["primary"] ?: DockComponent.getDefaultInstance()).description
                    }))
                    this.owner = userRef {
                        this.displayName = reply0.dock.owner.displayName
                    }
                }
            }
            val reply1 = metricsMetricSeriesService.updateMetricSeries(WireCopies.reflective(outbound1))
            findDocksResponse {
                this.docks.addAll(listOf(dock {
                    this.dimensions = dockDimensions {
                        this.weight = reply1.metricSeries.dimensions.weight
                    }
                    this.specs.putAll(reply1.metricSeries.specsMap)
                    this.status = reply0.dock.status
                }))
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handlePeekDock(request: PeekDockRequest): PeekDockResponse =
        RpcBoundary.run("acme.yards.v1.DockQueryService/PeekDock") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PeekDock", request)
            log.atInfo().addKeyValue("operation", "PeekDock").log("Handling transport request")
            val outbound0 = suggestSeatMapsRequest {
                this.context = request.context
                this.filter = SuggestSeatMapsRequestKt.filter {
                    this.createdAfter = request.context.deadline
                    this.ownerId = request.dockId
                }
            }
            val reply0 = clientForPeekDock0.suggestSeatMaps(outbound0)
            peekDockResponse {
                this.dock = dock {
                    this.dockId = request.dockId
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleSuggestDocks(request: SuggestDocksRequest): SuggestDocksResponse =
        RpcBoundary.run("acme.yards.v1.DockQueryService/SuggestDocks") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SuggestDocks", request)
            log.atInfo().addKeyValue("operation", "SuggestDocks").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            suggestDocksResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }
}
