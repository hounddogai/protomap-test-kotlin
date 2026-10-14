package com.acme.transport.deliveries.adapters

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.BrowseDeliveriesRequest
import com.acme.deliveries.v1.BrowseDeliveriesResponse
import com.acme.deliveries.v1.CheckDeliveryExceptionEligibilityRequestKt
import com.acme.deliveries.v1.CountDeliveriesRequest
import com.acme.deliveries.v1.CountDeliveriesResponse
import com.acme.deliveries.v1.DeliveryExceptionValidationServiceGrpcKt
import com.acme.deliveries.v1.DeliveryQueryServiceGrpcKt
import com.acme.deliveries.v1.ExportDeliveriesRequest
import com.acme.deliveries.v1.ExportDeliveriesResponse
import com.acme.deliveries.v1.FindDeliveriesRequest
import com.acme.deliveries.v1.FindDeliveriesResponse
import com.acme.deliveries.v1.LintProofOfDeliveriesRequest
import com.acme.deliveries.v1.LintProofOfDeliveriesRequestKt
import com.acme.deliveries.v1.LookupDeliveryRequest
import com.acme.deliveries.v1.LookupDeliveryResponse
import com.acme.deliveries.v1.NormalizeProofOfDeliveryRequest
import com.acme.deliveries.v1.NormalizeProofOfDeliveryRequestKt
import com.acme.deliveries.v1.NormalizeProofOfDeliveryResponse
import com.acme.deliveries.v1.PeekDeliveryRequest
import com.acme.deliveries.v1.PeekDeliveryResponse
import com.acme.deliveries.v1.ProofOfDeliveryValidationServiceGrpcKt
import com.acme.deliveries.v1.ResolveDeliveriesRequest
import com.acme.deliveries.v1.ResolveDeliveriesResponse
import com.acme.deliveries.v1.ScoreDeliveryExceptionRequestKt
import com.acme.deliveries.v1.ScoreProofOfDeliveryRequest
import com.acme.deliveries.v1.ScoreProofOfDeliveryResponse
import com.acme.deliveries.v1.SuggestDeliveriesRequest
import com.acme.deliveries.v1.SuggestDeliveriesResponse
import com.acme.deliveries.v1.SummarizeDeliveriesRequest
import com.acme.deliveries.v1.SummarizeDeliveriesResponse
import com.acme.deliveries.v1.ValidateDeliveryExceptionRequest
import com.acme.deliveries.v1.browseDeliveriesResponse
import com.acme.deliveries.v1.checkDeliveryExceptionEligibilityRequest
import com.acme.deliveries.v1.countDeliveriesResponse
import com.acme.deliveries.v1.delivery
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.deliveryExceptionRef
import com.acme.deliveries.v1.deliveryRef
import com.acme.deliveries.v1.exportDeliveriesResponse
import com.acme.deliveries.v1.findDeliveriesResponse
import com.acme.deliveries.v1.lintProofOfDeliveriesRequest
import com.acme.deliveries.v1.lookupDeliveryResponse
import com.acme.deliveries.v1.normalizeProofOfDeliveryRequest
import com.acme.deliveries.v1.peekDeliveryResponse
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.resolveDeliveriesResponse
import com.acme.deliveries.v1.scoreDeliveryExceptionRequest
import com.acme.deliveries.v1.scoreProofOfDeliveryRequest
import com.acme.deliveries.v1.suggestDeliveriesResponse
import com.acme.deliveries.v1.summarizeDeliveriesResponse
import com.acme.deliveries.v1.validateDeliveryExceptionRequest
import com.acme.drivers.v1.driverRef
import com.acme.identity.v1.CheckUserEligibilityRequest
import com.acme.identity.v1.NormalizeUserRequest
import com.acme.identity.v1.UserValidationServiceGrpcKt
import com.acme.identity.v1.checkUserEligibilityRequest
import com.acme.identity.v1.lintUsersRequest
import com.acme.identity.v1.normalizeUserRequest
import com.acme.identity.v1.user
import com.acme.identity.v1.userRef
import com.acme.keymanagement.v1.BrowseCryptoKeysRequestKt
import com.acme.keymanagement.v1.CryptoKeyQueryServiceGrpcKt
import com.acme.keymanagement.v1.browseCryptoKeysRequest
import com.acme.transport.deliveries.DeliveriesRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.venues.v1.CountVenuesRequest
import com.acme.venues.v1.CountVenuesRequestKt
import com.acme.venues.v1.CountVenuesResponse
import com.acme.venues.v1.VenueQueryServiceGrpcKt
import com.acme.venues.v1.countVenuesRequest
import com.acme.yards.v1.DockCalculationServiceGrpcKt
import com.acme.yards.v1.dock
import com.acme.yards.v1.recalculateDocksRequest
import com.google.protobuf.Any as ProtoAny
import com.google.protobuf.Timestamp
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.slf4j.LoggerFactory

/** Answers read-only questions about deliveries, for screens and reports. */
class DeliveryQueryController(
    private val store: DeliveriesRepository,
    private val workers: CoroutineScope,
    private val deliveriesDeliveryExceptionValidationService: DeliveryExceptionValidationServiceGrpcKt.DeliveryExceptionValidationServiceCoroutineStub,
    private val deliveriesProofOfDeliveryValidationService: ProofOfDeliveryValidationServiceGrpcKt.ProofOfDeliveryValidationServiceCoroutineStub,
    private val identityUserValidationService: UserValidationServiceGrpcKt.UserValidationServiceCoroutineStub,
    private val keymanagementCryptoKeyQueryService: CryptoKeyQueryServiceGrpcKt.CryptoKeyQueryServiceCoroutineStub,
    private val venuesVenueQueryService: VenueQueryServiceGrpcKt.VenueQueryServiceCoroutineStub,
    private val yardsDockCalculationService: DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub,
) : DeliveryQueryServiceGrpcKt.DeliveryQueryServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DeliveryQueryController::class.java)

    private val invokePeekDelivery0: suspend (ScoreProofOfDeliveryRequest) -> ScoreProofOfDeliveryResponse = { payload ->
        deliveriesProofOfDeliveryValidationService.scoreProofOfDelivery(payload)
    }

    /** Lists deliveries a page at a time. */
    override suspend fun browseDeliveries(request: BrowseDeliveriesRequest): BrowseDeliveriesResponse = handleBrowseDeliveries(request)

    /** Counts deliveries by status. */
    override suspend fun countDeliveries(request: CountDeliveriesRequest): CountDeliveriesResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryQueryService/CountDeliveries") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CountDeliveries", request)
            log.atInfo().addKeyValue("operation", "CountDeliveries").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            countDeliveriesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Streams every matching delivery. */
    override fun exportDeliveries(request: ExportDeliveriesRequest): Flow<ExportDeliveriesResponse> = flow {
        emit(handleExportDeliveries(request))
    }

    /** Lists deliveries a page at a time. */
    override suspend fun findDeliveries(request: FindDeliveriesRequest): FindDeliveriesResponse = handleFindDeliveries(request)

    /** Returns a delivery. */
    override suspend fun lookupDelivery(request: LookupDeliveryRequest): LookupDeliveryResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryQueryService/LookupDelivery") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LookupDelivery", request)
            log.atInfo().addKeyValue("operation", "LookupDelivery").log("Handling transport request")
            val outbound0 = normalizeProofOfDeliveryRequest {
                this.context = request.context
                this.details = NormalizeProofOfDeliveryRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                    this.locale = request.context.locale
                }
            }
            val adapter0 = OutboundPort<NormalizeProofOfDeliveryRequest, NormalizeProofOfDeliveryResponse> { payload ->
                deliveriesProofOfDeliveryValidationService.normalizeProofOfDelivery(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            lookupDeliveryResponse {
                this.delivery = delivery {
                    this.deliveryId = request.deliveryId
                }
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }

    /** Returns a delivery. */
    override suspend fun peekDelivery(request: PeekDeliveryRequest): PeekDeliveryResponse {
        return handlePeekDelivery(request)
    }

    /** Returns several deliveries at once. */
    override suspend fun resolveDeliveries(request: ResolveDeliveriesRequest): ResolveDeliveriesResponse = handleResolveDeliveries(request)

    /** Lists deliveries a page at a time. */
    override suspend fun suggestDeliveries(request: SuggestDeliveriesRequest): SuggestDeliveriesResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryQueryService/SuggestDeliveries") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SuggestDeliveries", request)
            log.atInfo().addKeyValue("operation", "SuggestDeliveries").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            suggestDeliveriesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Summarizes a delivery. */
    override suspend fun summarizeDeliveries(request: SummarizeDeliveriesRequest): SummarizeDeliveriesResponse {
        return handleSummarizeDeliveries(request)
    }

    private fun BrowseDeliveriesRequest.toCheckUserEligibilityFromBrowseDeliveries0(): CheckUserEligibilityRequest {
        val source = this
        return checkUserEligibilityRequest {
            this.context = source.context
            this.user = user {
                this.primaryAddress = postalAddress {
                    this.region = source.filter.region
                }
            }
        }
    }

    private suspend fun handleBrowseDeliveries(request: BrowseDeliveriesRequest): BrowseDeliveriesResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryQueryService/BrowseDeliveries") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BrowseDeliveries", request)
            log.atInfo().addKeyValue("operation", "BrowseDeliveries").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = request.toCheckUserEligibilityFromBrowseDeliveries0()
            val reply0 = identityUserValidationService.checkUserEligibility(outbound0)
            val outbound1 = checkDeliveryExceptionEligibilityRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.requester = contactInfo {
                        this.email = reply0.user.relationshipManager.email
                    }
                }
                this.details = CheckDeliveryExceptionEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.email = reply0.user.contact.email
                    }
                }
            }
            val reply1 = deliveriesDeliveryExceptionValidationService.checkDeliveryExceptionEligibility(outbound1)
            browseDeliveriesResponse {
                this.deliveries.addAll(listOf(delivery {
                    this.audit = auditInfo {
                        this.etag = reply0.user.audit.etag
                    }
                    this.counterparty = deliveryExceptionRef {
                        this.deliveryExceptionId = reply1.deliveryException.deliveryExceptionId
                    }
                }))
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleExportDeliveries(request: ExportDeliveriesRequest): ExportDeliveriesResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryQueryService/ExportDeliveries") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExportDeliveries", request)
            log.atInfo().addKeyValue("operation", "ExportDeliveries").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = lintProofOfDeliveriesRequest {
                this.context = request.context
                this.details = LintProofOfDeliveriesRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
                this.proofOfDelivery = proofOfDelivery {
                    this.audit = auditInfo {
                        this.updatedAt = request.filter.createdAfter
                    }
                    this.billingAddress = postalAddress {
                        this.region = request.filter.region
                    }
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = deliveriesProofOfDeliveryValidationService.lintProofOfDeliveries(envelope0.unpack(LintProofOfDeliveriesRequest::class.java))
            val outbound1 = scoreDeliveryExceptionRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.audit = auditInfo {
                        this.createdAt = reply0.proofOfDelivery.audit.createdAt
                        this.updatedAt = reply0.proofOfDelivery.audit.updatedAt
                    }
                }
                this.details = ScoreDeliveryExceptionRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(reply0.proofOfDelivery.billingAddress.linesList)
                        this.postalCode = reply0.proofOfDelivery.billingAddress.postalCode
                    }
                }
            }
            val reply1 = deliveriesDeliveryExceptionValidationService.scoreDeliveryException(WireCopies.json(outbound1))
            exportDeliveriesResponse {
                this.delivery = delivery {
                    this.audit = auditInfo {
                        this.createdAt = reply1.deliveryException.audit.createdAt
                        this.updatedBy = reply0.proofOfDelivery.audit.updatedBy
                    }
                    this.billingAddress = postalAddress {
                        this.region = request.filter.region
                    }
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private data class FindDeliveries0Command(
        val context: RequestContext,
        val proofOfDeliveryBillingAddressRegion: String,
        val proofOfDeliveryCounterpartyEmail: String,
        val proofOfDeliveryAuditCreatedAt: Timestamp
    ) {
        fun toRequest(): ScoreProofOfDeliveryRequest {
            val command = this
            return scoreProofOfDeliveryRequest {
                this.context = command.context
                this.proofOfDelivery = proofOfDelivery {
                    this.audit = auditInfo {
                        this.createdAt = command.proofOfDeliveryAuditCreatedAt
                    }
                    this.billingAddress = postalAddress {
                        this.region = command.proofOfDeliveryBillingAddressRegion
                    }
                    this.counterparty = driverRef {
                        this.email = command.proofOfDeliveryCounterpartyEmail
                    }
                }
            }
        }
    }

    private suspend fun handleFindDeliveries(request: FindDeliveriesRequest): FindDeliveriesResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryQueryService/FindDeliveries") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "FindDeliveries", request)
            log.atInfo().addKeyValue("operation", "FindDeliveries").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = FindDeliveries0Command(
                context = request.context,
                proofOfDeliveryBillingAddressRegion = request.filter.region,
                proofOfDeliveryCounterpartyEmail = request.context.actor.email,
                proofOfDeliveryAuditCreatedAt = request.filter.createdAfter
            ).toRequest()
            val reply0 = deliveriesProofOfDeliveryValidationService.scoreProofOfDelivery(outbound0)
            val outbound1 = normalizeUserRequest {
                this.context = request.context
                this.user = user {
                    this.primaryAddress = postalAddress {
                        this.postalCode = reply0.proofOfDelivery.billingAddress.postalCode
                    }
                }
            }
            val command1: Submission<NormalizeUserRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = identityUserValidationService.normalizeUser(payload1)
            findDeliveriesResponse {
                this.deliveries.addAll(listOf(delivery {
                    this.audit = auditInfo {
                        this.etag = reply1.user.audit.etag
                    }
                    this.billingAddress = postalAddress {
                        this.locality = reply0.proofOfDelivery.billingAddress.locality
                    }
                }))
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
            }
        }

    private suspend fun handlePeekDelivery(request: PeekDeliveryRequest): PeekDeliveryResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryQueryService/PeekDelivery") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PeekDelivery", request)
            log.atInfo().addKeyValue("operation", "PeekDelivery").log("Handling transport request")
            val outbound0 = scoreProofOfDeliveryRequest {
                this.context = request.context
                this.proofOfDelivery = proofOfDelivery {
                    this.counterparty = driverRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = invokePeekDelivery0(outbound0)
            peekDeliveryResponse {
                this.delivery = delivery {
                    this.billingAddress = postalAddress {
                        this.postalCode = reply0.proofOfDelivery.billingAddress.postalCode
                    }
                    this.deliveryId = request.deliveryId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleResolveDeliveries(request: ResolveDeliveriesRequest): ResolveDeliveriesResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryQueryService/ResolveDeliveries") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResolveDeliveries", request)
            log.atInfo().addKeyValue("operation", "ResolveDeliveries").log("Handling transport request")
            val outbound0 = countVenuesRequest {
                this.context = request.context
                this.filter = CountVenuesRequestKt.filter {
                    this.createdAfter = request.context.deadline
                    this.labels.putAll(request.context.baggageMap)
                    this.ownerId = request.context.tenantId
                }
            }
            val invoke0: suspend (CountVenuesRequest, Metadata) -> CountVenuesResponse = venuesVenueQueryService::countVenues
            val reply0 = invoke0(outbound0, Metadata())
            resolveDeliveriesResponse {
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleSummarizeDeliveries(request: SummarizeDeliveriesRequest): SummarizeDeliveriesResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryQueryService/SummarizeDeliveries") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SummarizeDeliveries", request)
            log.atInfo().addKeyValue("operation", "SummarizeDeliveries").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.format).log("Recorded audit detail")
            val outbound0 = ValidateDeliveryExceptionRequest.newBuilder().apply {
                putAllAnnotations(request.annotationsMap)
                setContext(request.context)
                setDeliveryException(deliveryException {
                    this.assignee = userRef {
                        this.email = request.context.actor.email
                    }
                    this.customer = deliveryRef {
                        this.deliveryId = request.deliveryId
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> deliveriesDeliveryExceptionValidationService.validateDeliveryException(value) }
            val outbound1 = RequestFactory.create {
                browseCryptoKeysRequest {
                    this.context = request.context
                    this.filter = BrowseCryptoKeysRequestKt.filter {
                        this.createdAfter = reply0.deliveryException.sla.dueAt
                    }
                }
            }
            val reply1 = keymanagementCryptoKeyQueryService.browseCryptoKeys(outbound1)
            val outbound2 = recalculateDocksRequest {
                this.context = request.context
                this.dock = dock {
                    this.audit = auditInfo {
                        this.etag = reply0.deliveryException.audit.etag
                        this.updatedBy = reply0.deliveryException.audit.updatedBy
                    }
                    this.owner = userRef {
                        this.displayName = reply0.deliveryException.assignee.displayName
                        this.email = reply0.deliveryException.requester.email
                    }
                }
            }
            val reply2 = ClientRegistry.lookup("acme.yards.v1.DockCalculationService", DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub::class.java).recalculateDocks(outbound2)
            val outbound3 = lintUsersRequest {
                this.context = request.context
                this.user = user {
                    this.audit = auditInfo {
                        this.updatedBy = reply2.dock.audit.updatedBy
                    }
                    this.relationshipManager = userRef {
                        this.displayName = reply2.dock.owner.displayName
                    }
                }
            }
            val reply3 = identityUserValidationService.lintUsers(WireCopies.reflective(outbound3))
            summarizeDeliveriesResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply1.result.detailsMap)
                    this.message = reply2.result.message
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }
}
