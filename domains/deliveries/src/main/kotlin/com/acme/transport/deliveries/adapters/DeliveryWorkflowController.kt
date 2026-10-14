package com.acme.transport.deliveries.adapters

import com.acme.common.v1.PersonName
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.community.v1.CloneForumRequest
import com.acme.community.v1.CloneForumResponse
import com.acme.community.v1.ForumServiceGrpcKt
import com.acme.community.v1.cloneForumRequest
import com.acme.community.v1.forum
import com.acme.concessions.v1.ConcessionStandCalculationServiceGrpcKt
import com.acme.concessions.v1.SimulateConcessionStandResponse
import com.acme.concessions.v1.concessionStand
import com.acme.concessions.v1.concessionStandComponent
import com.acme.concessions.v1.simulateConcessionStandRequest
import com.acme.deliveries.v1.AdvanceDeliveryWorkflowRequest
import com.acme.deliveries.v1.AdvanceDeliveryWorkflowResponse
import com.acme.deliveries.v1.AdvanceDeliveryWorkflowResponseKt
import com.acme.deliveries.v1.CancelDeliveryWorkflowRequest
import com.acme.deliveries.v1.CancelDeliveryWorkflowResponse
import com.acme.deliveries.v1.CancelDeliveryWorkflowResponseKt
import com.acme.deliveries.v1.DeliveryAttachment
import com.acme.deliveries.v1.DeliveryExceptionValidationServiceGrpcKt
import com.acme.deliveries.v1.DeliveryServiceGrpcKt
import com.acme.deliveries.v1.DeliveryWorkflowServiceGrpcKt
import com.acme.deliveries.v1.InspectDeliveryWorkflowRequest
import com.acme.deliveries.v1.InspectDeliveryWorkflowResponse
import com.acme.deliveries.v1.ListDeliveryWorkflowsRequest
import com.acme.deliveries.v1.ListDeliveryWorkflowsResponse
import com.acme.deliveries.v1.PauseDeliveryWorkflowRequest
import com.acme.deliveries.v1.PauseDeliveryWorkflowResponse
import com.acme.deliveries.v1.SignalDeliveryWorkflowRequest
import com.acme.deliveries.v1.SignalDeliveryWorkflowResponse
import com.acme.deliveries.v1.StartDeliveryWorkflowRequest
import com.acme.deliveries.v1.StartDeliveryWorkflowResponse
import com.acme.deliveries.v1.ValidateDeliveryExceptionRequest
import com.acme.deliveries.v1.ValidateDeliveryExceptionRequestKt
import com.acme.deliveries.v1.advanceDeliveryWorkflowResponse
import com.acme.deliveries.v1.cancelDeliveryWorkflowResponse
import com.acme.deliveries.v1.delivery
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.deliveryExceptionRef
import com.acme.deliveries.v1.getDeliveryRequest
import com.acme.deliveries.v1.inspectDeliveryWorkflowResponse
import com.acme.deliveries.v1.listDeliveryWorkflowsResponse
import com.acme.deliveries.v1.pauseDeliveryWorkflowResponse
import com.acme.deliveries.v1.signalDeliveryWorkflowResponse
import com.acme.deliveries.v1.startDeliveryWorkflowResponse
import com.acme.deliveries.v1.validateDeliveryExceptionRequest
import com.acme.deliveries.v1.watchDeliveriesRequest
import com.acme.drivers.v1.BrowseDriverLicensesRequestKt
import com.acme.drivers.v1.CalculateDriverLicenseRequest
import com.acme.drivers.v1.CalculateDriverLicenseRequestKt
import com.acme.drivers.v1.CalculateDriverLicenseResponse
import com.acme.drivers.v1.CalculateDutyStatusRequest
import com.acme.drivers.v1.CalculateDutyStatusRequestKt
import com.acme.drivers.v1.DriverLicenseCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseQueryServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseValidationServiceGrpcKt
import com.acme.drivers.v1.DutyStatusCalculationServiceGrpcKt
import com.acme.drivers.v1.PreviewDriverLicenseResponse
import com.acme.drivers.v1.browseDriverLicensesRequest
import com.acme.drivers.v1.calculateDriverLicenseRequest
import com.acme.drivers.v1.calculateDutyStatusRequest
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverLicenseAttachment
import com.acme.drivers.v1.previewDriverLicenseRequest
import com.acme.featureflags.v1.ExplainFeatureFlagDecisionRequest
import com.acme.featureflags.v1.ExplainFeatureFlagDecisionRequestKt
import com.acme.featureflags.v1.ExplainFeatureFlagDecisionResponse
import com.acme.featureflags.v1.FeatureFlagValidationServiceGrpcKt
import com.acme.featureflags.v1.explainFeatureFlagDecisionRequest
import com.acme.featureflags.v1.featureFlag
import com.acme.fuel.v1.FuelCardServiceGrpcKt
import com.acme.fuel.v1.ListFuelCardsRequestKt
import com.acme.fuel.v1.listFuelCardsRequest
import com.acme.ledger.v1.FindLedgerAccountsRequestKt
import com.acme.ledger.v1.FindLedgerAccountsResponse
import com.acme.ledger.v1.LedgerAccountQueryServiceGrpcKt
import com.acme.ledger.v1.findLedgerAccountsRequest
import com.acme.media.v1.MediaAssetCalculationServiceGrpcKt
import com.acme.media.v1.mediaAsset
import com.acme.media.v1.recalculateMediaAssetsRequest
import com.acme.transport.deliveries.DeliveriesRepository
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Runs the long-lived workflow that takes a delivery from start to finish. */
class DeliveryWorkflowController(
    private val store: DeliveriesRepository,
    private val workers: CoroutineScope,
    private val communityForumService: ForumServiceGrpcKt.ForumServiceCoroutineStub,
    private val concessionsConcessionStandCalculationService: ConcessionStandCalculationServiceGrpcKt.ConcessionStandCalculationServiceCoroutineStub,
    private val deliveriesDeliveryExceptionValidationService: DeliveryExceptionValidationServiceGrpcKt.DeliveryExceptionValidationServiceCoroutineStub,
    private val deliveriesDeliveryService: DeliveryServiceGrpcKt.DeliveryServiceCoroutineStub,
    private val driversDriverLicenseCalculationService: DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub,
    private val driversDriverLicenseQueryService: DriverLicenseQueryServiceGrpcKt.DriverLicenseQueryServiceCoroutineStub,
    private val driversDriverLicenseValidationService: DriverLicenseValidationServiceGrpcKt.DriverLicenseValidationServiceCoroutineStub,
    private val driversDutyStatusCalculationService: DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineStub,
    private val featureflagsFeatureFlagValidationService: FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub,
    private val fuelFuelCardService: FuelCardServiceGrpcKt.FuelCardServiceCoroutineStub,
    private val ledgerLedgerAccountQueryService: LedgerAccountQueryServiceGrpcKt.LedgerAccountQueryServiceCoroutineStub,
    private val mediaMediaAssetCalculationService: MediaAssetCalculationServiceGrpcKt.MediaAssetCalculationServiceCoroutineStub,
) : DeliveryWorkflowServiceGrpcKt.DeliveryWorkflowServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DeliveryWorkflowController::class.java)

    private val invokeCancelDeliveryWorkflow1: suspend (ExplainFeatureFlagDecisionRequest) -> ExplainFeatureFlagDecisionResponse = { payload ->
        featureflagsFeatureFlagValidationService.explainFeatureFlagDecision(payload)
    }

    private val workerForSignalDeliveryWorkflow1 = ReplyMailbox<CloneForumRequest, CloneForumResponse>(workers) { payload ->
        withTimeout(5_000) { communityForumService.cloneForum(payload) }
    }

    /** Advances a delivery. */
    override suspend fun advanceDeliveryWorkflow(request: AdvanceDeliveryWorkflowRequest): AdvanceDeliveryWorkflowResponse = handleAdvanceDeliveryWorkflow(request)

    /** Cancels a delivery. */
    override suspend fun cancelDeliveryWorkflow(request: CancelDeliveryWorkflowRequest): CancelDeliveryWorkflowResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryWorkflowService/CancelDeliveryWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CancelDeliveryWorkflow", request)
            log.atInfo().addKeyValue("operation", "CancelDeliveryWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.delivery.deliveryId).log("Recorded audit detail")
            val outbound0 = calculateDriverLicenseRequest {
                this.context = request.context
                this.details = CalculateDriverLicenseRequestKt.details {
                    this.destination = postalAddress {
                        this.locality = request.delivery.billingAddress.locality
                        this.recipient = request.delivery.billingAddress.recipient
                    }
                }
            }
            val adapter0 = OutboundPort<CalculateDriverLicenseRequest, CalculateDriverLicenseResponse> { payload ->
                driversDriverLicenseCalculationService.calculateDriverLicense(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = explainFeatureFlagDecisionRequest {
                this.context = request.context
                this.details = ExplainFeatureFlagDecisionRequestKt.details {
                    this.contact = contactInfo {
                        this.phone = request.details.notify.phone
                        this.preferredChannel = request.details.notify.preferredChannel
                    }
                }
                this.featureFlag = featureFlag {
                    this.audit = auditInfo {
                        this.createdBy = request.delivery.audit.createdBy
                        this.etag = request.delivery.audit.etag
                    }
                }
            }
            val reply1 = invokeCancelDeliveryWorkflow1(outbound1)
            cancelDeliveryWorkflowResponse {
                this.delivery = delivery {
                    this.audit = auditInfo {
                        this.updatedAt = reply1.featureFlag.audit.updatedAt
                    }
                    this.deliveryId = request.deliveryId
                    this.total = request.delivery.total
                }
                this.outcome = CancelDeliveryWorkflowResponseKt.outcome {
                    this.status = reply1.outcome.status
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    /** Returns a delivery. */
    override suspend fun inspectDeliveryWorkflow(request: InspectDeliveryWorkflowRequest): InspectDeliveryWorkflowResponse {
        return handleInspectDeliveryWorkflow(request)
    }

    /** Lists deliveries a page at a time. */
    override suspend fun listDeliveryWorkflows(request: ListDeliveryWorkflowsRequest): ListDeliveryWorkflowsResponse = handleListDeliveryWorkflows(request)

    /** Pauses a delivery. */
    override suspend fun pauseDeliveryWorkflow(request: PauseDeliveryWorkflowRequest): PauseDeliveryWorkflowResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryWorkflowService/PauseDeliveryWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PauseDeliveryWorkflow", request)
            log.atInfo().addKeyValue("operation", "PauseDeliveryWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.delivery.deliveryId).log("Recorded audit detail")
            var reply0 = FindLedgerAccountsResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.delivery.linesList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound0 = findLedgerAccountsRequest {
                                this.context = request.context
                                this.filter = FindLedgerAccountsRequestKt.filter {
                                    this.createdAfter = request.delivery.audit.createdAt
                                    this.labels.putAll(item.attributesMap)
                                    this.ownerId = item.lineId
                                    this.region = request.delivery.billingAddress.region
                                }
                            }
                            ledgerLedgerAccountQueryService.findLedgerAccounts(outbound0)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply0 = it }
                }
            }
            val outbound1 = CalculateDutyStatusRequest.newBuilder().apply {
                setContext(request.context)
                setDetails(CalculateDutyStatusRequestKt.details {
                    this.destination = postalAddress {
                        this.recipient = request.delivery.billingAddress.recipient
                        this.region = request.delivery.billingAddress.region
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> driversDutyStatusCalculationService.calculateDutyStatus(value) }
            val outbound2 = RequestFactory.create {
                browseDriverLicensesRequest {
                    this.context = request.context
                    this.filter = BrowseDriverLicensesRequestKt.filter {
                        this.region = request.delivery.billingAddress.region
                    }
                }
            }
            val reply2 = driversDriverLicenseQueryService.browseDriverLicenses(outbound2)
            var reply3 = SimulateConcessionStandResponse.getDefaultInstance()
            request.delivery.linesList.asFlow().map { item ->
                val outbound3 = simulateConcessionStandRequest {
                    this.concessionStand = concessionStand {
                        this.components.putAll(mapOf("primary" to concessionStandComponent {
                            this.quantity = item.quantity
                        }))
                    }
                    this.context = request.context
                }
                concessionsConcessionStandCalculationService.simulateConcessionStand(outbound3)
            }.collect { reply3 = it }
            pauseDeliveryWorkflowResponse {
                this.delivery = delivery {
                    this.deliveryId = request.deliveryId
                    this.number = request.delivery.number
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.message = reply2.result.message
                    this.status = "ok"
                }
            }
        }

    /** Signals a delivery. */
    override suspend fun signalDeliveryWorkflow(request: SignalDeliveryWorkflowRequest): SignalDeliveryWorkflowResponse {
        return handleSignalDeliveryWorkflow(request)
    }

    /** Starts a delivery. */
    override suspend fun startDeliveryWorkflow(request: StartDeliveryWorkflowRequest): StartDeliveryWorkflowResponse = handleStartDeliveryWorkflow(request)

    private data class AdvanceDeliveryWorkflow1Command(
        val context: RequestContext,
        val deliveryExceptionRequesterPhone: String,
        val detailsContactName: PersonName
    ) {
        fun toRequest(): ValidateDeliveryExceptionRequest {
            val command = this
            return validateDeliveryExceptionRequest {
                this.context = command.context
                this.deliveryException = deliveryException {
                    this.requester = contactInfo {
                        this.phone = command.deliveryExceptionRequesterPhone
                    }
                }
                this.details = ValidateDeliveryExceptionRequestKt.details {
                    this.contact = contactInfo {
                        this.name = command.detailsContactName
                    }
                }
            }
        }
    }

    private suspend fun handleAdvanceDeliveryWorkflow(request: AdvanceDeliveryWorkflowRequest): AdvanceDeliveryWorkflowResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryWorkflowService/AdvanceDeliveryWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "AdvanceDeliveryWorkflow", request)
            log.atInfo().addKeyValue("operation", "AdvanceDeliveryWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.delivery.deliveryId).log("Recorded audit detail")
            val outbound0 = listFuelCardsRequest {
                this.context = request.context
                this.filter = ListFuelCardsRequestKt.filter {
                    this.region = request.delivery.billingAddress.region
                }
            }
            val reply0 = fuelFuelCardService.listFuelCards(WireCopies.json(outbound0))
            val outbound1 = AdvanceDeliveryWorkflow1Command(
                context = request.context,
                deliveryExceptionRequesterPhone = request.details.notify.phone,
                detailsContactName = request.details.notify.name
            ).toRequest()
            val reply1 = deliveriesDeliveryExceptionValidationService.validateDeliveryException(outbound1)
            val outbound2 = watchDeliveriesRequest {
                this.context = request.context
                this.subscriber = contactInfo {
                    this.email = reply1.deliveryException.requester.email
                    this.name = request.details.notify.name
                    this.phone = request.details.notify.phone
                    this.preferredChannel = reply1.deliveryException.requester.preferredChannel
                }
            }
            deliveriesDeliveryService.watchDeliveries(outbound2).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            advanceDeliveryWorkflowResponse {
                this.delivery = delivery {
                    this.counterparty = deliveryExceptionRef {
                        this.displayName = reply1.deliveryException.customer.displayName
                    }
                    this.deliveryId = request.deliveryId
                    this.number = request.delivery.number
                }
                this.outcome = AdvanceDeliveryWorkflowResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleInspectDeliveryWorkflow(request: InspectDeliveryWorkflowRequest): InspectDeliveryWorkflowResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryWorkflowService/InspectDeliveryWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "InspectDeliveryWorkflow", request)
            log.atInfo().addKeyValue("operation", "InspectDeliveryWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            inspectDeliveryWorkflowResponse {
                this.delivery = delivery {
                    this.deliveryId = request.deliveryId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleListDeliveryWorkflows(request: ListDeliveryWorkflowsRequest): ListDeliveryWorkflowsResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryWorkflowService/ListDeliveryWorkflows") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListDeliveryWorkflows", request)
            log.atInfo().addKeyValue("operation", "ListDeliveryWorkflows").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            listDeliveryWorkflowsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSignalDeliveryWorkflow(request: SignalDeliveryWorkflowRequest): SignalDeliveryWorkflowResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryWorkflowService/SignalDeliveryWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SignalDeliveryWorkflow", request)
            log.atInfo().addKeyValue("operation", "SignalDeliveryWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.delivery.number).log("Recorded audit detail")
            val outbound0 = getDeliveryRequest {
                this.context = request.context
                this.deliveryId = request.delivery.deliveryId
            }
            val reply0 = deliveriesDeliveryService.getDelivery(WireCopies.reflective(outbound0))
            val outbound1 = cloneForumRequest {
                this.context = request.context
                this.forum = forum {
                    this.children.addAll(listOf(forum {
                        this.children.addAll(listOf(forum {
                            this.audit = auditInfo {
                                this.createdBy = request.delivery.audit.createdBy
                                this.updatedBy = request.delivery.audit.updatedBy
                            }
                            this.location = postalAddress {
                                this.postalCode = reply0.delivery.billingAddress.postalCode
                                this.recipient = reply0.delivery.billingAddress.recipient
                            }
                        }))
                    }))
                }
            }
            val reply1 = workerForSignalDeliveryWorkflow1.submit(outbound1)
            signalDeliveryWorkflowResponse {
                this.delivery = delivery {
                    this.billingAddress = postalAddress {
                        this.countryCode = reply1.forum.location.countryCode
                        this.region = reply1.forum.location.region
                    }
                    this.deliveryId = request.deliveryId
                    this.issuedAt = request.delivery.issuedAt
                    this.number = reply0.delivery.number
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleStartDeliveryWorkflow(request: StartDeliveryWorkflowRequest): StartDeliveryWorkflowResponse =
        RpcBoundary.run("acme.deliveries.v1.DeliveryWorkflowService/StartDeliveryWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StartDeliveryWorkflow", request)
            log.atInfo().addKeyValue("operation", "StartDeliveryWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.delivery.deliveryId).log("Recorded audit detail")
            var reply0 = PreviewDriverLicenseResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<DeliveryAttachment>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound0 = previewDriverLicenseRequest {
                            this.context = request.context
                            this.driverLicense = driverLicense {
                                this.attachments.addAll(listOf(driverLicenseAttachment {
                                    this.checksum = item.checksum
                                    this.sizeBytes = item.sizeBytes
                                }))
                            }
                        }
                        reply0 = driversDriverLicenseValidationService.previewDriverLicense(outbound0)
                    }
                }
                try {
                    request.delivery.attachmentsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound1 = recalculateMediaAssetsRequest {
                this.context = request.context
                this.mediaAsset = mediaAsset {
                    this.address = postalAddress {
                        this.lines.addAll(request.delivery.billingAddress.linesList)
                        this.region = request.delivery.billingAddress.region
                    }
                    this.audit = auditInfo {
                        this.updatedBy = reply0.driverLicense.audit.updatedBy
                    }
                }
            }
            val reply1 = flowOf(outbound1).map { value -> mediaMediaAssetCalculationService.recalculateMediaAssets(value) }.single()
            startDeliveryWorkflowResponse {
                this.delivery = delivery {
                    this.audit = auditInfo {
                        this.updatedBy = reply1.mediaAsset.audit.updatedBy
                    }
                    this.counterparty = request.delivery.counterparty
                    this.deliveryId = request.deliveryId
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.status = "ok"
                }
            }
        }
}
