package com.acme.transport.deliveries.api

import com.acme.careplans.v1.CarePlanQueryServiceGrpcKt
import com.acme.careplans.v1.ResolveCarePlansRequest
import com.acme.careplans.v1.ResolveCarePlansResponse
import com.acme.careplans.v1.resolveCarePlansRequest
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.CalculateProofOfDeliveryRequest
import com.acme.deliveries.v1.CalculateProofOfDeliveryResponse
import com.acme.deliveries.v1.CompareProofOfDeliveryOptionsRequest
import com.acme.deliveries.v1.CompareProofOfDeliveryOptionsResponse
import com.acme.deliveries.v1.EstimateProofOfDeliveryRequest
import com.acme.deliveries.v1.EstimateProofOfDeliveryResponse
import com.acme.deliveries.v1.ExplainProofOfDeliveryCalculationRequest
import com.acme.deliveries.v1.ExplainProofOfDeliveryCalculationResponse
import com.acme.deliveries.v1.GetProofOfDeliveryRatesRequest
import com.acme.deliveries.v1.GetProofOfDeliveryRatesResponse
import com.acme.deliveries.v1.ProofOfDeliveryCalculationServiceGrpcKt
import com.acme.deliveries.v1.QuoteProofOfDeliveryRequest
import com.acme.deliveries.v1.QuoteProofOfDeliveryResponse
import com.acme.deliveries.v1.RecalculateProofOfDeliveriesRequest
import com.acme.deliveries.v1.RecalculateProofOfDeliveriesResponse
import com.acme.deliveries.v1.SimulateProofOfDeliveryRequest
import com.acme.deliveries.v1.SimulateProofOfDeliveryResponse
import com.acme.deliveries.v1.calculateProofOfDeliveryResponse
import com.acme.deliveries.v1.compareProofOfDeliveryOptionsResponse
import com.acme.deliveries.v1.estimateProofOfDeliveryResponse
import com.acme.deliveries.v1.explainProofOfDeliveryCalculationResponse
import com.acme.deliveries.v1.getProofOfDeliveryRatesResponse
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.proofOfDeliveryLine
import com.acme.deliveries.v1.quoteProofOfDeliveryResponse
import com.acme.deliveries.v1.recalculateProofOfDeliveriesResponse
import com.acme.deliveries.v1.simulateProofOfDeliveryResponse
import com.acme.documents.v1.CreateDocumentRequest
import com.acme.documents.v1.CreateDocumentResponse
import com.acme.documents.v1.DocumentServiceGrpcKt
import com.acme.documents.v1.createDocumentRequest
import com.acme.documents.v1.document
import com.acme.drivers.v1.driverRef
import com.acme.featureflags.v1.FeatureFlagServiceGrpcKt
import com.acme.featureflags.v1.FeatureFlagValidationServiceGrpcKt
import com.acme.featureflags.v1.LintFeatureFlagsRequestKt
import com.acme.featureflags.v1.LintFeatureFlagsResponse
import com.acme.featureflags.v1.cloneFeatureFlagRequest
import com.acme.featureflags.v1.featureFlag
import com.acme.featureflags.v1.featureFlagDiscountAction
import com.acme.featureflags.v1.lintFeatureFlagsRequest
import com.acme.featureflags.v1.normalizeFeatureFlagRequest
import com.acme.identity.v1.userRef
import com.acme.metrics.v1.MetricSeriesServiceGrpcKt
import com.acme.metrics.v1.SearchMetricSeriesesRequestKt
import com.acme.metrics.v1.copy as metricsCopy
import com.acme.metrics.v1.searchMetricSeriesesRequest
import com.acme.notifications.v1.CheckNotificationEligibilityRequestKt
import com.acme.notifications.v1.NotificationValidationServiceGrpcKt
import com.acme.notifications.v1.checkNotificationEligibilityRequest
import com.acme.notifications.v1.notification
import com.acme.patients.v1.FindPatientsRequestKt
import com.acme.patients.v1.PatientQueryServiceGrpcKt
import com.acme.patients.v1.findPatientsRequest
import com.acme.search.v1.CheckSearchIndexEligibilityRequestKt
import com.acme.search.v1.SearchIndexValidationServiceGrpcKt
import com.acme.search.v1.checkSearchIndexEligibilityRequest
import com.acme.search.v1.searchIndex
import com.acme.seating.v1.SeatMapServiceGrpcKt
import com.acme.seating.v1.createSeatMapRequest
import com.acme.seating.v1.seatMap
import com.acme.transport.deliveries.DeliveriesRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.BrowseYardsRequest
import com.acme.yards.v1.BrowseYardsRequestKt
import com.acme.yards.v1.CalculateDockRequest
import com.acme.yards.v1.CalculateDockRequestKt
import com.acme.yards.v1.CalculateYardMoveRequest
import com.acme.yards.v1.CalculateYardMoveRequestKt
import com.acme.yards.v1.CompareYardMoveOptionsRequest
import com.acme.yards.v1.CompareYardMoveOptionsRequestKt
import com.acme.yards.v1.CompareYardMoveOptionsResponse
import com.acme.yards.v1.DockCalculationServiceGrpcKt
import com.acme.yards.v1.DockComponent
import com.acme.yards.v1.DockQueryServiceGrpcKt
import com.acme.yards.v1.ExportYardsRequestKt
import com.acme.yards.v1.SuggestDocksRequestKt
import com.acme.yards.v1.YardMoveCalculationServiceGrpcKt
import com.acme.yards.v1.YardQueryServiceGrpcKt
import com.acme.yards.v1.browseYardsRequest
import com.acme.yards.v1.calculateDockRequest
import com.acme.yards.v1.calculateYardMoveRequest
import com.acme.yards.v1.compareDockOptionsRequest
import com.acme.yards.v1.compareYardMoveOptionsRequest
import com.acme.yards.v1.dock
import com.acme.yards.v1.exportYardsRequest
import com.acme.yards.v1.suggestDocksRequest
import com.acme.yards.v1.yardMove
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Computes the figures of a proof of delivery, such as totals, taxes, and estimates. */
class ProofOfDeliveryCalculationGrpcService(
    private val store: DeliveriesRepository,
    private val workers: CoroutineScope,
    private val careplansCarePlanQueryService: CarePlanQueryServiceGrpcKt.CarePlanQueryServiceCoroutineStub,
    private val documentsDocumentService: DocumentServiceGrpcKt.DocumentServiceCoroutineStub,
    private val featureflagsFeatureFlagService: FeatureFlagServiceGrpcKt.FeatureFlagServiceCoroutineStub,
    private val featureflagsFeatureFlagValidationService: FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub,
    private val metricsMetricSeriesService: MetricSeriesServiceGrpcKt.MetricSeriesServiceCoroutineStub,
    private val notificationsNotificationValidationService: NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub,
    private val patientsPatientQueryService: PatientQueryServiceGrpcKt.PatientQueryServiceCoroutineStub,
    private val searchSearchIndexValidationService: SearchIndexValidationServiceGrpcKt.SearchIndexValidationServiceCoroutineStub,
    private val seatingSeatMapService: SeatMapServiceGrpcKt.SeatMapServiceCoroutineStub,
    private val yardsDockCalculationService: DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub,
    private val yardsDockQueryService: DockQueryServiceGrpcKt.DockQueryServiceCoroutineStub,
    private val yardsYardMoveCalculationService: YardMoveCalculationServiceGrpcKt.YardMoveCalculationServiceCoroutineStub,
    private val yardsYardQueryService: YardQueryServiceGrpcKt.YardQueryServiceCoroutineStub,
) : ProofOfDeliveryCalculationServiceGrpcKt.ProofOfDeliveryCalculationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(ProofOfDeliveryCalculationGrpcService::class.java)

    private val invokeCalculateProofOfDelivery0: suspend (ResolveCarePlansRequest) -> ResolveCarePlansResponse = { payload ->
        careplansCarePlanQueryService.resolveCarePlans(payload)
    }

    private val workerForEstimateProofOfDelivery1 = ReplyMailbox<CreateDocumentRequest, CreateDocumentResponse>(workers) { payload ->
        withTimeout(5_000) { documentsDocumentService.createDocument(payload) }
    }

    /** Calculates a proof of delivery. */
    override suspend fun calculateProofOfDelivery(request: CalculateProofOfDeliveryRequest): CalculateProofOfDeliveryResponse {
        return handleCalculateProofOfDelivery(request)
    }

    /** Compares a proof of delivery. */
    override suspend fun compareProofOfDeliveryOptions(request: CompareProofOfDeliveryOptionsRequest): CompareProofOfDeliveryOptionsResponse = handleCompareProofOfDeliveryOptions(request)

    /** Estimates a proof of delivery. */
    override suspend fun estimateProofOfDelivery(request: EstimateProofOfDeliveryRequest): EstimateProofOfDeliveryResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryCalculationService/EstimateProofOfDelivery") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EstimateProofOfDelivery", request)
            log.atInfo().addKeyValue("operation", "EstimateProofOfDelivery").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.proofOfDelivery.proofOfDeliveryId).log("Recorded audit detail")
            val outbound0 = createSeatMapRequest {
                this.context = request.context
                this.seatMap = seatMap {
                    this.children.addAll(listOf(seatMap {
                        this.children.addAll(listOf(seatMap {
                            this.location = postalAddress {
                                this.locality = request.proofOfDelivery.billingAddress.locality
                            }
                            this.manager = userRef {
                                this.displayName = request.proofOfDelivery.counterparty.displayName
                            }
                        }))
                    }))
                }
            }
            val reply0 = seatingSeatMapService.createSeatMap(WireCopies.reflective(outbound0))
            val outbound1 = createDocumentRequest {
                this.context = request.context
                this.document = document {
                    this.billingAddress = postalAddress {
                        this.recipient = request.details.destination.recipient
                    }
                }
            }
            val reply1 = workerForEstimateProofOfDelivery1.submit(outbound1)
            estimateProofOfDeliveryResponse {
                this.proofOfDelivery = proofOfDelivery {
                    this.billingAddress = postalAddress {
                        this.postalCode = reply1.document.billingAddress.postalCode
                        this.region = reply1.document.billingAddress.region
                    }
                    this.dueAt = request.proofOfDelivery.dueAt
                    this.lines.addAll(listOf(proofOfDeliveryLine {
                        this.attributes.putAll(reply0.seatMap.attributesMap)
                    }))
                    this.proofOfDeliveryId = request.proofOfDeliveryId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Explains a proof of delivery. */
    override suspend fun explainProofOfDeliveryCalculation(request: ExplainProofOfDeliveryCalculationRequest): ExplainProofOfDeliveryCalculationResponse {
        return handleExplainProofOfDeliveryCalculation(request)
    }

    /** Returns a proof of delivery. */
    override suspend fun getProofOfDeliveryRates(request: GetProofOfDeliveryRatesRequest): GetProofOfDeliveryRatesResponse = handleGetProofOfDeliveryRates(request)

    /** Quotes a proof of delivery. */
    override suspend fun quoteProofOfDelivery(request: QuoteProofOfDeliveryRequest): QuoteProofOfDeliveryResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryCalculationService/QuoteProofOfDelivery") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "QuoteProofOfDelivery", request)
            log.atInfo().addKeyValue("operation", "QuoteProofOfDelivery").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.proofOfDelivery.proofOfDeliveryId).log("Recorded audit detail")
            val outbound0 = searchMetricSeriesesRequest {}.metricsCopy {
                this.context = request.context
                this.filter = SearchMetricSeriesesRequestKt.filter {
                    this.region = request.details.destination.region
                }
            }
            val reply0 = metricsMetricSeriesService.searchMetricSerieses(outbound0)
            quoteProofOfDeliveryResponse {
                this.proofOfDelivery = proofOfDelivery {
                    this.proofOfDeliveryId = request.proofOfDeliveryId
                    this.status = request.proofOfDelivery.status
                }
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }

    /** Recalculates a proof of delivery. */
    override suspend fun recalculateProofOfDeliveries(request: RecalculateProofOfDeliveriesRequest): RecalculateProofOfDeliveriesResponse {
        return handleRecalculateProofOfDeliveries(request)
    }

    /** Simulates a proof of delivery. */
    override suspend fun simulateProofOfDelivery(request: SimulateProofOfDeliveryRequest): SimulateProofOfDeliveryResponse = handleSimulateProofOfDelivery(request)

    private suspend fun handleCalculateProofOfDelivery(request: CalculateProofOfDeliveryRequest): CalculateProofOfDeliveryResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryCalculationService/CalculateProofOfDelivery") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CalculateProofOfDelivery", request)
            log.atInfo().addKeyValue("operation", "CalculateProofOfDelivery").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.proofOfDelivery.proofOfDeliveryId).log("Recorded audit detail")
            val outbound0 = resolveCarePlansRequest {
                this.context = request.context
            }
            val reply0 = invokeCalculateProofOfDelivery0(outbound0)
            val outbound1 = compareYardMoveOptionsRequest {
                this.context = request.context
                this.details = CompareYardMoveOptionsRequestKt.details {
                    this.destination = postalAddress {
                        this.postalCode = request.details.destination.postalCode
                    }
                }
            }
            val invoke1: suspend (CompareYardMoveOptionsRequest, Metadata) -> CompareYardMoveOptionsResponse = yardsYardMoveCalculationService::compareYardMoveOptions
            val reply1 = invoke1(outbound1, Metadata())
            calculateProofOfDeliveryResponse {
                this.proofOfDelivery = proofOfDelivery {
                    this.audit = auditInfo {
                        this.updatedAt = reply1.yardMove.audit.updatedAt
                    }
                    this.dueAt = reply1.yardMove.sla.dueAt
                    this.proofOfDeliveryId = request.proofOfDeliveryId
                    this.total = request.proofOfDelivery.total
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleCompareProofOfDeliveryOptions(request: CompareProofOfDeliveryOptionsRequest): CompareProofOfDeliveryOptionsResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryCalculationService/CompareProofOfDeliveryOptions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CompareProofOfDeliveryOptions", request)
            log.atInfo().addKeyValue("operation", "CompareProofOfDeliveryOptions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.proofOfDelivery.proofOfDeliveryId).log("Recorded audit detail")
            val outbound0 = CalculateYardMoveRequest.newBuilder().apply {
                setContext(request.context)
                setDetails(CalculateYardMoveRequestKt.details {
                    this.destination = postalAddress {
                        this.postalCode = request.details.destination.postalCode
                    }
                })
                setYardMove(yardMove {
                    this.audit = auditInfo {
                        this.etag = request.proofOfDelivery.audit.etag
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> yardsYardMoveCalculationService.calculateYardMove(value) }
            val outbound1 = RequestFactory.create {
                cloneFeatureFlagRequest {
                    this.context = request.context
                    this.featureFlag = featureFlag {
                        this.audit = auditInfo {
                            this.etag = request.proofOfDelivery.audit.etag
                            this.updatedAt = request.proofOfDelivery.audit.updatedAt
                        }
                        this.owner = userRef {
                            this.displayName = reply0.yardMove.customer.displayName
                        }
                    }
                }
            }
            val reply1 = featureflagsFeatureFlagService.cloneFeatureFlag(outbound1)
            val outbound2 = findPatientsRequest {
                this.context = request.context
                this.filter = FindPatientsRequestKt.filter {
                    this.createdAfter = reply0.yardMove.sla.dueAt
                    this.region = request.details.destination.region
                }
            }
            val reply2 = ClientRegistry.lookup("acme.patients.v1.PatientQueryService", PatientQueryServiceGrpcKt.PatientQueryServiceCoroutineStub::class.java).findPatients(outbound2)
            compareProofOfDeliveryOptionsResponse {
                this.proofOfDelivery = proofOfDelivery {
                    this.audit = auditInfo {
                        this.createdAt = reply0.yardMove.audit.createdAt
                    }
                    this.counterparty = driverRef {
                        this.email = reply0.yardMove.requester.email
                    }
                    this.metadata.putAll(request.proofOfDelivery.metadataMap)
                    this.proofOfDeliveryId = request.proofOfDeliveryId
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.message = reply2.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleExplainProofOfDeliveryCalculation(request: ExplainProofOfDeliveryCalculationRequest): ExplainProofOfDeliveryCalculationResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryCalculationService/ExplainProofOfDeliveryCalculation") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainProofOfDeliveryCalculation", request)
            log.atInfo().addKeyValue("operation", "ExplainProofOfDeliveryCalculation").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.proofOfDelivery.proofOfDeliveryId).log("Recorded audit detail")
            val outbound0 = exportYardsRequest {
                this.context = request.context
                this.filter = ExportYardsRequestKt.filter {
                    this.createdAfter = request.proofOfDelivery.audit.updatedAt
                    this.ownerId = request.proofOfDelivery.counterparty.driverId
                    this.region = request.proofOfDelivery.billingAddress.region
                }
            }
            yardsYardQueryService.exportYards(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            var reply1 = LintFeatureFlagsResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.proofOfDelivery.linesList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound1 = lintFeatureFlagsRequest {
                                this.context = request.context
                                this.details = LintFeatureFlagsRequestKt.details {
                                    this.contact = contactInfo {
                                        this.email = request.proofOfDelivery.counterparty.email
                                    }
                                }
                                this.featureFlag = featureFlag {
                                    this.audit = auditInfo {
                                        this.createdBy = request.proofOfDelivery.audit.createdBy
                                    }
                                    this.description = item.description
                                    this.discount = featureFlagDiscountAction {
                                        this.amount = item.tax.amount
                                    }
                                }
                            }
                            featureflagsFeatureFlagValidationService.lintFeatureFlags(outbound1)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply1 = it }
                }
            }
            val outbound2 = checkNotificationEligibilityRequest {
                this.context = request.context
                this.details = CheckNotificationEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.phone = request.details.contact.phone
                    }
                }
                this.notification = notification {
                    this.assignee = userRef {
                        this.email = reply1.featureFlag.owner.email
                        this.userId = reply1.featureFlag.owner.userId
                    }
                }
            }
            val reply2 = withTimeout(5_000) { notificationsNotificationValidationService.checkNotificationEligibility(outbound2) }
            val configure3: CheckSearchIndexEligibilityRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.details = CheckSearchIndexEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.details.contact.email
                        this.phone = request.details.contact.phone
                    }
                }
                this.searchIndex = searchIndex {
                    this.address = postalAddress {
                        this.postalCode = request.proofOfDelivery.billingAddress.postalCode
                    }
                    this.owner = userRef {
                        this.displayName = request.proofOfDelivery.counterparty.displayName
                    }
                }
            }
            val outbound3 = checkSearchIndexEligibilityRequest(configure3)
            val reply3 = searchSearchIndexValidationService.checkSearchIndexEligibility(outbound3)
            explainProofOfDeliveryCalculationResponse {
                this.proofOfDelivery = proofOfDelivery {
                    this.audit = auditInfo {
                        this.updatedAt = reply3.searchIndex.audit.updatedAt
                    }
                    this.billingAddress = postalAddress {
                        this.recipient = reply3.searchIndex.address.recipient
                    }
                    this.counterparty = driverRef {
                        this.displayName = reply2.notification.assignee.displayName
                        this.email = reply2.notification.requester.email
                    }
                    this.issuedAt = request.proofOfDelivery.issuedAt
                    this.proofOfDeliveryId = request.proofOfDeliveryId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleGetProofOfDeliveryRates(request: GetProofOfDeliveryRatesRequest): GetProofOfDeliveryRatesResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryCalculationService/GetProofOfDeliveryRates") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetProofOfDeliveryRates", request)
            log.atInfo().addKeyValue("operation", "GetProofOfDeliveryRates").log("Handling transport request")
            val outbound0 = compareDockOptionsRequest {
                this.context = request.context
                this.dock = dock {
                    this.owner = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply0 = yardsDockCalculationService.compareDockOptions(outbound0)
            val outbound1 = normalizeFeatureFlagRequest {
                this.context = request.context
                this.featureFlag = featureFlag {
                    this.audit = auditInfo {
                        this.etag = reply0.dock.audit.etag
                    }
                    this.owner = userRef {
                        this.userId = reply0.dock.owner.userId
                    }
                }
            }
            val reply1 = featureflagsFeatureFlagValidationService.normalizeFeatureFlag(outbound1)
            getProofOfDeliveryRatesResponse {
                this.proofOfDelivery = proofOfDelivery {
                    this.audit = auditInfo {
                        this.createdBy = reply0.dock.audit.createdBy
                    }
                    this.counterparty = driverRef {
                        this.displayName = reply1.featureFlag.owner.displayName
                    }
                    this.proofOfDeliveryId = request.proofOfDeliveryId
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.status = "ok"
                }
            }
        }

    private fun RecalculateProofOfDeliveriesRequest.toCalculateDockFromRecalculateProofOfDeliveries0(): CalculateDockRequest {
        val source = this
        return calculateDockRequest {
            this.context = source.context
            this.details = CalculateDockRequestKt.details {
                this.destination = postalAddress {
                    this.countryCode = source.proofOfDelivery.billingAddress.countryCode
                    this.lines.addAll(source.proofOfDelivery.billingAddress.linesList)
                }
            }
        }
    }

    private suspend fun handleRecalculateProofOfDeliveries(request: RecalculateProofOfDeliveriesRequest): RecalculateProofOfDeliveriesResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryCalculationService/RecalculateProofOfDeliveries") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecalculateProofOfDeliveries", request)
            log.atInfo().addKeyValue("operation", "RecalculateProofOfDeliveries").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.proofOfDelivery.proofOfDeliveryId).log("Recorded audit detail")
            val outbound0 = request.toCalculateDockFromRecalculateProofOfDeliveries0()
            val reply0 = yardsDockCalculationService.calculateDock(outbound0)
            recalculateProofOfDeliveriesResponse {
                this.proofOfDelivery = proofOfDelivery {
                    this.billingAddress = request.proofOfDelivery.billingAddress
                    this.lines.addAll(listOf(proofOfDeliveryLine {
                        this.description = (reply0.dock.componentsMap["primary"] ?: DockComponent.getDefaultInstance()).description
                    }))
                    this.proofOfDeliveryId = request.proofOfDeliveryId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSimulateProofOfDelivery(request: SimulateProofOfDeliveryRequest): SimulateProofOfDeliveryResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryCalculationService/SimulateProofOfDelivery") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SimulateProofOfDelivery", request)
            log.atInfo().addKeyValue("operation", "SimulateProofOfDelivery").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.proofOfDelivery.proofOfDeliveryId).log("Recorded audit detail")
            val outbound0 = suggestDocksRequest {
                this.context = request.context
                this.filter = SuggestDocksRequestKt.filter {
                    this.createdAfter = request.proofOfDelivery.audit.updatedAt
                    this.ownerId = request.proofOfDeliveryId
                    this.region = request.proofOfDelivery.billingAddress.region
                }
            }
            val reply0 = yardsDockQueryService.suggestDocks(outbound0)
            val outbound1 = browseYardsRequest {
                this.context = request.context
                this.filter = BrowseYardsRequestKt.filter {
                    this.createdAfter = request.proofOfDelivery.audit.updatedAt
                    this.ownerId = reply0.result.correlationId
                    this.region = request.details.destination.region
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = yardsYardQueryService.browseYards(envelope1.unpack(BrowseYardsRequest::class.java))
            simulateProofOfDeliveryResponse {
                this.proofOfDelivery = proofOfDelivery {
                    this.proofOfDeliveryId = request.proofOfDeliveryId
                    this.total = request.proofOfDelivery.total
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }
}
