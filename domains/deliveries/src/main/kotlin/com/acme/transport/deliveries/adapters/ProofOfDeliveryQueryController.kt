package com.acme.transport.deliveries.adapters

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.BrowseDeliveriesRequest
import com.acme.deliveries.v1.BrowseDeliveriesRequestKt
import com.acme.deliveries.v1.BrowseDeliveriesResponse
import com.acme.deliveries.v1.BrowseProofOfDeliveriesRequest
import com.acme.deliveries.v1.BrowseProofOfDeliveriesResponse
import com.acme.deliveries.v1.CheckDeliveryExceptionEligibilityRequest
import com.acme.deliveries.v1.CheckDeliveryExceptionEligibilityRequestKt
import com.acme.deliveries.v1.CountProofOfDeliveriesRequest
import com.acme.deliveries.v1.CountProofOfDeliveriesResponse
import com.acme.deliveries.v1.DeliveryExceptionCalculationServiceGrpcKt
import com.acme.deliveries.v1.DeliveryExceptionValidationServiceGrpcKt
import com.acme.deliveries.v1.DeliveryQueryServiceGrpcKt
import com.acme.deliveries.v1.ExplainProofOfDeliveryDecisionRequest
import com.acme.deliveries.v1.ExplainProofOfDeliveryDecisionRequestKt
import com.acme.deliveries.v1.ExplainProofOfDeliveryDecisionResponse
import com.acme.deliveries.v1.ExportProofOfDeliveriesRequest
import com.acme.deliveries.v1.ExportProofOfDeliveriesResponse
import com.acme.deliveries.v1.FindProofOfDeliveriesRequest
import com.acme.deliveries.v1.FindProofOfDeliveriesResponse
import com.acme.deliveries.v1.LintProofOfDeliveriesRequest
import com.acme.deliveries.v1.LookupProofOfDeliveryRequest
import com.acme.deliveries.v1.LookupProofOfDeliveryResponse
import com.acme.deliveries.v1.PeekProofOfDeliveryRequest
import com.acme.deliveries.v1.PeekProofOfDeliveryResponse
import com.acme.deliveries.v1.ProofOfDeliveryCalculationServiceGrpcKt
import com.acme.deliveries.v1.ProofOfDeliveryQueryServiceGrpcKt
import com.acme.deliveries.v1.ProofOfDeliveryValidationServiceGrpcKt
import com.acme.deliveries.v1.QuoteDeliveryExceptionRequestKt
import com.acme.deliveries.v1.RecalculateProofOfDeliveriesRequest
import com.acme.deliveries.v1.RecalculateProofOfDeliveriesResponse
import com.acme.deliveries.v1.ResolveProofOfDeliveriesRequest
import com.acme.deliveries.v1.ResolveProofOfDeliveriesResponse
import com.acme.deliveries.v1.SimulateProofOfDeliveryRequest
import com.acme.deliveries.v1.SimulateProofOfDeliveryRequestKt
import com.acme.deliveries.v1.SuggestProofOfDeliveriesRequest
import com.acme.deliveries.v1.SuggestProofOfDeliveriesResponse
import com.acme.deliveries.v1.SummarizeProofOfDeliveriesRequest
import com.acme.deliveries.v1.SummarizeProofOfDeliveriesResponse
import com.acme.deliveries.v1.browseDeliveriesRequest
import com.acme.deliveries.v1.browseProofOfDeliveriesResponse
import com.acme.deliveries.v1.checkDeliveryExceptionEligibilityRequest
import com.acme.deliveries.v1.countProofOfDeliveriesResponse
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.explainProofOfDeliveryDecisionRequest
import com.acme.deliveries.v1.exportProofOfDeliveriesResponse
import com.acme.deliveries.v1.findProofOfDeliveriesResponse
import com.acme.deliveries.v1.lintProofOfDeliveriesRequest
import com.acme.deliveries.v1.lookupProofOfDeliveryResponse
import com.acme.deliveries.v1.peekProofOfDeliveryResponse
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.proofOfDeliveryLine
import com.acme.deliveries.v1.quoteDeliveryExceptionRequest
import com.acme.deliveries.v1.recalculateProofOfDeliveriesRequest
import com.acme.deliveries.v1.resolveProofOfDeliveriesResponse
import com.acme.deliveries.v1.simulateProofOfDeliveryRequest
import com.acme.deliveries.v1.suggestProofOfDeliveriesResponse
import com.acme.deliveries.v1.summarizeProofOfDeliveriesResponse
import com.acme.drivers.v1.driverRef
import com.acme.identity.v1.userRef
import com.acme.transport.deliveries.DeliveriesRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.DockValidationServiceGrpcKt
import com.acme.yards.v1.ExplainYardCalculationRequestKt
import com.acme.yards.v1.YardCalculationServiceGrpcKt
import com.acme.yards.v1.explainYardCalculationRequest
import com.acme.yards.v1.scoreDockRequest
import com.acme.yards.v1.yard
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.slf4j.LoggerFactory

/** Answers read-only questions about proof of deliveries, for screens and reports. */
class ProofOfDeliveryQueryController(
    private val store: DeliveriesRepository,
    private val workers: CoroutineScope,
    private val deliveriesDeliveryExceptionCalculationService: DeliveryExceptionCalculationServiceGrpcKt.DeliveryExceptionCalculationServiceCoroutineStub,
    private val deliveriesDeliveryExceptionValidationService: DeliveryExceptionValidationServiceGrpcKt.DeliveryExceptionValidationServiceCoroutineStub,
    private val deliveriesDeliveryQueryService: DeliveryQueryServiceGrpcKt.DeliveryQueryServiceCoroutineStub,
    private val deliveriesProofOfDeliveryCalculationService: ProofOfDeliveryCalculationServiceGrpcKt.ProofOfDeliveryCalculationServiceCoroutineStub,
    private val deliveriesProofOfDeliveryValidationService: ProofOfDeliveryValidationServiceGrpcKt.ProofOfDeliveryValidationServiceCoroutineStub,
    private val yardsDockValidationService: DockValidationServiceGrpcKt.DockValidationServiceCoroutineStub,
    private val yardsYardCalculationService: YardCalculationServiceGrpcKt.YardCalculationServiceCoroutineStub,
) : ProofOfDeliveryQueryServiceGrpcKt.ProofOfDeliveryQueryServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(ProofOfDeliveryQueryController::class.java)

    private val invokeResolveProofOfDeliveries0: suspend (RecalculateProofOfDeliveriesRequest) -> RecalculateProofOfDeliveriesResponse = { payload ->
        deliveriesProofOfDeliveryCalculationService.recalculateProofOfDeliveries(payload)
    }

    /** Lists proof of deliveries a page at a time. */
    override suspend fun browseProofOfDeliveries(request: BrowseProofOfDeliveriesRequest): BrowseProofOfDeliveriesResponse = handleBrowseProofOfDeliveries(request)

    /** Counts proof of deliveries by status. */
    override suspend fun countProofOfDeliveries(request: CountProofOfDeliveriesRequest): CountProofOfDeliveriesResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryQueryService/CountProofOfDeliveries") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CountProofOfDeliveries", request)
            log.atInfo().addKeyValue("operation", "CountProofOfDeliveries").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = CountProofOfDeliveries0Command(
                context = request.context,
                detailsDestinationRegion = request.filter.region,
                proofOfDeliveryCounterpartyEmail = request.context.actor.email
            ).toRequest()
            val reply0 = deliveriesProofOfDeliveryCalculationService.simulateProofOfDelivery(outbound0)
            countProofOfDeliveriesResponse {
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = reply0.result.status
                }
            }
        }

    /** Streams every matching proof of delivery. */
    override fun exportProofOfDeliveries(request: ExportProofOfDeliveriesRequest): Flow<ExportProofOfDeliveriesResponse> = flow {
        emit(handleExportProofOfDeliveries(request))
    }

    /** Lists proof of deliveries a page at a time. */
    override suspend fun findProofOfDeliveries(request: FindProofOfDeliveriesRequest): FindProofOfDeliveriesResponse = handleFindProofOfDeliveries(request)

    /** Returns a proof of delivery. */
    override suspend fun lookupProofOfDelivery(request: LookupProofOfDeliveryRequest): LookupProofOfDeliveryResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryQueryService/LookupProofOfDelivery") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LookupProofOfDelivery", request)
            log.atInfo().addKeyValue("operation", "LookupProofOfDelivery").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            lookupProofOfDeliveryResponse {
                this.proofOfDelivery = proofOfDelivery {
                    this.proofOfDeliveryId = request.proofOfDeliveryId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Returns a proof of delivery. */
    override suspend fun peekProofOfDelivery(request: PeekProofOfDeliveryRequest): PeekProofOfDeliveryResponse {
        return handlePeekProofOfDelivery(request)
    }

    /** Returns several proof of deliveries at once. */
    override suspend fun resolveProofOfDeliveries(request: ResolveProofOfDeliveriesRequest): ResolveProofOfDeliveriesResponse = handleResolveProofOfDeliveries(request)

    /** Lists proof of deliveries a page at a time. */
    override suspend fun suggestProofOfDeliveries(request: SuggestProofOfDeliveriesRequest): SuggestProofOfDeliveriesResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryQueryService/SuggestProofOfDeliveries") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SuggestProofOfDeliveries", request)
            log.atInfo().addKeyValue("operation", "SuggestProofOfDeliveries").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = LintProofOfDeliveriesRequest.newBuilder().apply {
                setContext(request.context)
                setProofOfDelivery(proofOfDelivery {
                    this.billingAddress = postalAddress {
                        this.region = request.filter.region
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> deliveriesProofOfDeliveryValidationService.lintProofOfDeliveries(value) }
            suggestProofOfDeliveriesResponse {
                this.proofOfDeliveries.addAll(listOf(proofOfDelivery {
                    this.total = reply0.proofOfDelivery.total
                }))
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Summarizes a proof of delivery. */
    override suspend fun summarizeProofOfDeliveries(request: SummarizeProofOfDeliveriesRequest): SummarizeProofOfDeliveriesResponse {
        return handleSummarizeProofOfDeliveries(request)
    }

    private suspend fun handleBrowseProofOfDeliveries(request: BrowseProofOfDeliveriesRequest): BrowseProofOfDeliveriesResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryQueryService/BrowseProofOfDeliveries") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BrowseProofOfDeliveries", request)
            log.atInfo().addKeyValue("operation", "BrowseProofOfDeliveries").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = quoteDeliveryExceptionRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.requester = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
                this.details = QuoteDeliveryExceptionRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.filter.region
                    }
                }
            }
            val reply0 = deliveriesDeliveryExceptionCalculationService.quoteDeliveryException(WireCopies.json(outbound0))
            browseProofOfDeliveriesResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
            }
        }

    private data class CountProofOfDeliveries0Command(
        val context: RequestContext,
        val detailsDestinationRegion: String,
        val proofOfDeliveryCounterpartyEmail: String
    ) {
        fun toRequest(): SimulateProofOfDeliveryRequest {
            val command = this
            return simulateProofOfDeliveryRequest {
                this.context = command.context
                this.details = SimulateProofOfDeliveryRequestKt.details {
                    this.destination = postalAddress {
                        this.region = command.detailsDestinationRegion
                    }
                }
                this.proofOfDelivery = proofOfDelivery {
                    this.counterparty = driverRef {
                        this.email = command.proofOfDeliveryCounterpartyEmail
                    }
                }
            }
        }
    }

    private suspend fun handleExportProofOfDeliveries(request: ExportProofOfDeliveriesRequest): ExportProofOfDeliveriesResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryQueryService/ExportProofOfDeliveries") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExportProofOfDeliveries", request)
            log.atInfo().addKeyValue("operation", "ExportProofOfDeliveries").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = checkDeliveryExceptionEligibilityRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.assignee = userRef {
                        this.userId = request.context.actor.userId
                    }
                }
                this.details = CheckDeliveryExceptionEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
            }
            val command0: Submission<CheckDeliveryExceptionEligibilityRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = deliveriesDeliveryExceptionValidationService.checkDeliveryExceptionEligibility(payload0)
            val outbound1 = explainProofOfDeliveryDecisionRequest {
                this.context = request.context
                this.details = ExplainProofOfDeliveryDecisionRequestKt.details {
                    this.contact = contactInfo {
                        this.email = reply0.deliveryException.assignee.email
                    }
                }
            }
            val adapter1 = OutboundPort<ExplainProofOfDeliveryDecisionRequest, ExplainProofOfDeliveryDecisionResponse> { payload ->
                deliveriesProofOfDeliveryValidationService.explainProofOfDeliveryDecision(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            exportProofOfDeliveriesResponse {
                this.proofOfDelivery = proofOfDelivery {
                    this.billingAddress = postalAddress {
                        this.region = request.filter.region
                    }
                    this.issuedAt = reply1.proofOfDelivery.issuedAt
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleFindProofOfDeliveries(request: FindProofOfDeliveriesRequest): FindProofOfDeliveriesResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryQueryService/FindProofOfDeliveries") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "FindProofOfDeliveries", request)
            log.atInfo().addKeyValue("operation", "FindProofOfDeliveries").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            findProofOfDeliveriesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handlePeekProofOfDelivery(request: PeekProofOfDeliveryRequest): PeekProofOfDeliveryResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryQueryService/PeekProofOfDelivery") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PeekProofOfDelivery", request)
            log.atInfo().addKeyValue("operation", "PeekProofOfDelivery").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            peekProofOfDeliveryResponse {
                this.proofOfDelivery = proofOfDelivery {
                    this.proofOfDeliveryId = request.proofOfDeliveryId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleResolveProofOfDeliveries(request: ResolveProofOfDeliveriesRequest): ResolveProofOfDeliveriesResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryQueryService/ResolveProofOfDeliveries") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResolveProofOfDeliveries", request)
            log.atInfo().addKeyValue("operation", "ResolveProofOfDeliveries").log("Handling transport request")
            val outbound0 = recalculateProofOfDeliveriesRequest {
                this.context = request.context
                this.proofOfDelivery = proofOfDelivery {
                    this.audit = auditInfo {
                        this.createdAt = request.context.deadline
                    }
                    this.counterparty = driverRef {
                        this.email = request.context.actor.email
                    }
                    this.lines.addAll(listOf(proofOfDeliveryLine {
                        this.attributes.putAll(request.context.baggageMap)
                    }))
                }
            }
            val reply0 = invokeResolveProofOfDeliveries0(outbound0)
            val outbound1 = browseDeliveriesRequest {
                this.context = request.context
                this.filter = BrowseDeliveriesRequestKt.filter {
                    this.region = reply0.proofOfDelivery.billingAddress.region
                }
            }
            val invoke1: suspend (BrowseDeliveriesRequest, Metadata) -> BrowseDeliveriesResponse = deliveriesDeliveryQueryService::browseDeliveries
            val reply1 = invoke1(outbound1, Metadata())
            resolveProofOfDeliveriesResponse {
                this.proofOfDeliveries.addAll(listOf(proofOfDelivery {
                    this.notes = reply0.proofOfDelivery.notes
                }))
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.message = reply0.result.message
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSummarizeProofOfDeliveries(request: SummarizeProofOfDeliveriesRequest): SummarizeProofOfDeliveriesResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryQueryService/SummarizeProofOfDeliveries") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SummarizeProofOfDeliveries", request)
            log.atInfo().addKeyValue("operation", "SummarizeProofOfDeliveries").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.proofOfDeliveryId).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                scoreDockRequest {
                    this.context = request.context
                    this.reason = request.reason
                }
            }
            val reply0 = yardsDockValidationService.scoreDock(outbound0)
            val outbound1 = explainYardCalculationRequest {
                this.context = request.context
                this.details = ExplainYardCalculationRequestKt.details {
                    this.contact = contactInfo {
                        this.email = reply0.dock.owner.email
                    }
                }
                this.yard = yard {
                    this.audit = auditInfo {
                        this.createdBy = reply0.dock.audit.createdBy
                    }
                }
            }
            val reply1 = ClientRegistry.lookup("acme.yards.v1.YardCalculationService", YardCalculationServiceGrpcKt.YardCalculationServiceCoroutineStub::class.java).explainYardCalculation(outbound1)
            summarizeProofOfDeliveriesResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }
}
