package com.acme.transport.fuel.api

import com.acme.authorization.v1.AccessPolicyValidationServiceGrpcKt
import com.acme.authorization.v1.NormalizeAccessPolicyRequest
import com.acme.authorization.v1.accessPolicy
import com.acme.authorization.v1.normalizeAccessPolicyRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.customers.v1.ConsentRecordValidationServiceGrpcKt
import com.acme.customers.v1.ExplainConsentRecordDecisionRequestKt
import com.acme.customers.v1.HouseholdCalculationServiceGrpcKt
import com.acme.customers.v1.NormalizeConsentRecordRequestKt
import com.acme.customers.v1.QuoteHouseholdRequest
import com.acme.customers.v1.copy as customersCopy
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.explainConsentRecordDecisionRequest
import com.acme.customers.v1.household
import com.acme.customers.v1.normalizeConsentRecordRequest
import com.acme.customers.v1.quoteHouseholdRequest
import com.acme.datasets.v1.DatasetQueryServiceGrpcKt
import com.acme.datasets.v1.SummarizeDatasetsRequestKt
import com.acme.datasets.v1.summarizeDatasetsRequest
import com.acme.deliveries.v1.DeliveryServiceGrpcKt
import com.acme.deliveries.v1.ListPendingProofOfDeliveriesRequestKt
import com.acme.deliveries.v1.ProofOfDeliveryApprovalServiceGrpcKt
import com.acme.deliveries.v1.UpdateDeliveryRequest
import com.acme.deliveries.v1.delivery
import com.acme.deliveries.v1.deliveryExceptionRef
import com.acme.deliveries.v1.deliveryLine
import com.acme.deliveries.v1.listPendingProofOfDeliveriesRequest
import com.acme.deliveries.v1.updateDeliveryRequest
import com.acme.drivers.v1.DutyStatusCalculationServiceGrpcKt
import com.acme.drivers.v1.QuoteDutyStatusRequest
import com.acme.drivers.v1.QuoteDutyStatusResponse
import com.acme.drivers.v1.driverLicenseRef
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.dutyStatus
import com.acme.drivers.v1.quoteDutyStatusRequest
import com.acme.fuel.v1.ActivateFuelPolicyRequest
import com.acme.fuel.v1.ActivateFuelPolicyResponse
import com.acme.fuel.v1.ActivateFuelPolicyResponseKt
import com.acme.fuel.v1.ExpireFuelPolicyRequest
import com.acme.fuel.v1.ExpireFuelPolicyResponse
import com.acme.fuel.v1.ExpireFuelPolicyResponseKt
import com.acme.fuel.v1.FuelCardCalculationServiceGrpcKt
import com.acme.fuel.v1.FuelCardImportServiceGrpcKt
import com.acme.fuel.v1.FuelCardSyncServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyImportServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyLifecycleServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyValidationServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionImportServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionSyncServiceGrpcKt
import com.acme.fuel.v1.GetFuelPolicyLifecycleRequest
import com.acme.fuel.v1.GetFuelPolicyLifecycleResponse
import com.acme.fuel.v1.ListFuelPolicyImportsRequestKt
import com.acme.fuel.v1.ReinstateFuelPolicyRequest
import com.acme.fuel.v1.ReinstateFuelPolicyResponse
import com.acme.fuel.v1.ReinstateFuelPolicyResponseKt
import com.acme.fuel.v1.RenewFuelPolicyRequest
import com.acme.fuel.v1.RenewFuelPolicyResponse
import com.acme.fuel.v1.RenewFuelPolicyResponseKt
import com.acme.fuel.v1.RetryFuelTransactionImportRequest
import com.acme.fuel.v1.RetryFuelTransactionImportRequestKt
import com.acme.fuel.v1.RetryFuelTransactionImportResponse
import com.acme.fuel.v1.ScheduleFuelPolicyExpiryRequest
import com.acme.fuel.v1.ScheduleFuelPolicyExpiryResponse
import com.acme.fuel.v1.SuspendFuelPolicyRequest
import com.acme.fuel.v1.SuspendFuelPolicyResponse
import com.acme.fuel.v1.SuspendFuelPolicyResponseKt
import com.acme.fuel.v1.SyncFuelCardsRequest
import com.acme.fuel.v1.TerminateFuelPolicyRequest
import com.acme.fuel.v1.TerminateFuelPolicyResponse
import com.acme.fuel.v1.TerminateFuelPolicyResponseKt
import com.acme.fuel.v1.ValidateFuelPolicyRequest
import com.acme.fuel.v1.ValidateFuelPolicyResponse
import com.acme.fuel.v1.activateFuelPolicyResponse
import com.acme.fuel.v1.expireFuelPolicyResponse
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.fuelCardBankFunding
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.fuelPolicyRouteAction
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.getFuelPolicyLifecycleResponse
import com.acme.fuel.v1.listFuelPolicyImportsRequest
import com.acme.fuel.v1.reconcileFuelTransactionsRequest
import com.acme.fuel.v1.reinstateFuelPolicyResponse
import com.acme.fuel.v1.renewFuelPolicyResponse
import com.acme.fuel.v1.retryFuelTransactionImportRequest
import com.acme.fuel.v1.scheduleFuelPolicyExpiryResponse
import com.acme.fuel.v1.simulateFuelCardRequest
import com.acme.fuel.v1.suspendFuelPolicyResponse
import com.acme.fuel.v1.syncFuelCardsRequest
import com.acme.fuel.v1.terminateFuelPolicyResponse
import com.acme.fuel.v1.validateFuelCardImportRequest
import com.acme.fuel.v1.validateFuelPolicyRequest
import com.acme.identity.v1.ExplainUserDecisionRequestKt
import com.acme.identity.v1.ScoreUserRequest
import com.acme.identity.v1.ScoreUserResponse
import com.acme.identity.v1.UserValidationServiceGrpcKt
import com.acme.identity.v1.explainUserDecisionRequest
import com.acme.identity.v1.scoreUserRequest
import com.acme.identity.v1.user
import com.acme.identity.v1.userRef
import com.acme.notifications.v1.NotificationQueryServiceGrpcKt
import com.acme.notifications.v1.SummarizeNotificationsRequest
import com.acme.notifications.v1.summarizeNotificationsRequest
import com.acme.secrets.v1.FindSecretsRequestKt
import com.acme.secrets.v1.SecretQueryServiceGrpcKt
import com.acme.secrets.v1.findSecretsRequest
import com.acme.shows.v1.CompareShowOptionsRequestKt
import com.acme.shows.v1.ShowCalculationServiceGrpcKt
import com.acme.shows.v1.compareShowOptionsRequest
import com.acme.shows.v1.show
import com.acme.shows.v1.showParticipant
import com.acme.transport.fuel.FuelRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.venues.v1.SearchVenuesRequestKt
import com.acme.venues.v1.VenueServiceGrpcKt
import com.acme.venues.v1.searchVenuesRequest
import com.acme.yards.v1.ListYardMoveReportsRequestKt
import com.acme.yards.v1.YardMoveReportingServiceGrpcKt
import com.acme.yards.v1.listYardMoveReportsRequest
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Moves a fuel policy between the states of its lifecycle. */
class FuelPolicyLifecycleGrpcService(
    private val store: FuelRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyValidationService: AccessPolicyValidationServiceGrpcKt.AccessPolicyValidationServiceCoroutineStub,
    private val customersConsentRecordValidationService: ConsentRecordValidationServiceGrpcKt.ConsentRecordValidationServiceCoroutineStub,
    private val customersHouseholdCalculationService: HouseholdCalculationServiceGrpcKt.HouseholdCalculationServiceCoroutineStub,
    private val datasetsDatasetQueryService: DatasetQueryServiceGrpcKt.DatasetQueryServiceCoroutineStub,
    private val deliveriesDeliveryService: DeliveryServiceGrpcKt.DeliveryServiceCoroutineStub,
    private val deliveriesProofOfDeliveryApprovalService: ProofOfDeliveryApprovalServiceGrpcKt.ProofOfDeliveryApprovalServiceCoroutineStub,
    private val driversDutyStatusCalculationService: DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineStub,
    private val fuelFuelCardCalculationService: FuelCardCalculationServiceGrpcKt.FuelCardCalculationServiceCoroutineStub,
    private val fuelFuelCardImportService: FuelCardImportServiceGrpcKt.FuelCardImportServiceCoroutineStub,
    private val fuelFuelCardSyncService: FuelCardSyncServiceGrpcKt.FuelCardSyncServiceCoroutineStub,
    private val fuelFuelPolicyImportService: FuelPolicyImportServiceGrpcKt.FuelPolicyImportServiceCoroutineStub,
    private val fuelFuelPolicyValidationService: FuelPolicyValidationServiceGrpcKt.FuelPolicyValidationServiceCoroutineStub,
    private val fuelFuelTransactionImportService: FuelTransactionImportServiceGrpcKt.FuelTransactionImportServiceCoroutineStub,
    private val fuelFuelTransactionSyncService: FuelTransactionSyncServiceGrpcKt.FuelTransactionSyncServiceCoroutineStub,
    private val identityUserValidationService: UserValidationServiceGrpcKt.UserValidationServiceCoroutineStub,
    private val notificationsNotificationQueryService: NotificationQueryServiceGrpcKt.NotificationQueryServiceCoroutineStub,
    private val secretsSecretQueryService: SecretQueryServiceGrpcKt.SecretQueryServiceCoroutineStub,
    private val showsShowCalculationService: ShowCalculationServiceGrpcKt.ShowCalculationServiceCoroutineStub,
    private val venuesVenueService: VenueServiceGrpcKt.VenueServiceCoroutineStub,
    private val yardsYardMoveReportingService: YardMoveReportingServiceGrpcKt.YardMoveReportingServiceCoroutineStub,
) : FuelPolicyLifecycleServiceGrpcKt.FuelPolicyLifecycleServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(FuelPolicyLifecycleGrpcService::class.java)

    private val invokeRenewFuelPolicy1: suspend (QuoteDutyStatusRequest) -> QuoteDutyStatusResponse = { payload ->
        driversDutyStatusCalculationService.quoteDutyStatus(payload)
    }

    private val workerForSuspendFuelPolicy1 = ReplyMailbox<ScoreUserRequest, ScoreUserResponse>(workers) { payload ->
        withTimeout(5_000) { identityUserValidationService.scoreUser(payload) }
    }

    private val clientForSuspendFuelPolicy2 by lazy { customersConsentRecordValidationService }

    /** Activates a fuel policy. */
    override suspend fun activateFuelPolicy(request: ActivateFuelPolicyRequest): ActivateFuelPolicyResponse {
        return handleActivateFuelPolicy(request)
    }

    /** Expires a fuel policy. */
    override suspend fun expireFuelPolicy(request: ExpireFuelPolicyRequest): ExpireFuelPolicyResponse = handleExpireFuelPolicy(request)

    /** Returns a fuel policy. */
    override suspend fun getFuelPolicyLifecycle(request: GetFuelPolicyLifecycleRequest): GetFuelPolicyLifecycleResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyLifecycleService/GetFuelPolicyLifecycle") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetFuelPolicyLifecycle", request)
            log.atInfo().addKeyValue("operation", "GetFuelPolicyLifecycle").log("Handling transport request")
            val outbound0 = simulateFuelCardRequest {
                this.context = request.context
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.createdAt = request.context.deadline
                    }
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                    this.externalAccountId = request.context.traceId
                }
            }
            val reply0 = fuelFuelCardCalculationService.simulateFuelCard(outbound0)
            getFuelPolicyLifecycleResponse {
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.fuelCard.audit.updatedBy
                    }
                    this.fuelPolicyId = request.fuelPolicyId
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
            }
        }

    /** Reinstates a fuel policy. */
    override suspend fun reinstateFuelPolicy(request: ReinstateFuelPolicyRequest): ReinstateFuelPolicyResponse {
        return handleReinstateFuelPolicy(request)
    }

    /** Renews a fuel policy. */
    override suspend fun renewFuelPolicy(request: RenewFuelPolicyRequest): RenewFuelPolicyResponse = handleRenewFuelPolicy(request)

    /** Schedules a fuel policy. */
    override suspend fun scheduleFuelPolicyExpiry(request: ScheduleFuelPolicyExpiryRequest): ScheduleFuelPolicyExpiryResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyLifecycleService/ScheduleFuelPolicyExpiry") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScheduleFuelPolicyExpiry", request)
            log.atInfo().addKeyValue("operation", "ScheduleFuelPolicyExpiry").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicy.fuelPolicyId).log("Recorded audit detail")
            val outbound0 = validateFuelPolicyRequest {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.createdBy = request.fuelPolicy.audit.createdBy
                        this.updatedBy = request.fuelPolicy.audit.updatedBy
                    }
                    this.owner = userRef {
                        this.userId = request.fuelPolicy.owner.userId
                    }
                    this.route = fuelPolicyRouteAction {
                        this.target = request.fuelPolicy.route.target
                    }
                }
            }
            val invoke0: suspend (ValidateFuelPolicyRequest, Metadata) -> ValidateFuelPolicyResponse = fuelFuelPolicyValidationService::validateFuelPolicy
            val reply0 = invoke0(outbound0, Metadata())
            val outbound1 = QuoteHouseholdRequest.newBuilder().apply {
                setContext(request.context)
                setHousehold(household {
                    this.children.addAll(listOf(household {
                        this.children.addAll(listOf(household {
                            this.audit = auditInfo {
                                this.createdAt = reply0.fuelPolicy.audit.createdAt
                                this.createdBy = reply0.fuelPolicy.audit.createdBy
                            }
                            this.manager = userRef {
                                this.email = reply0.fuelPolicy.owner.email
                            }
                        }))
                        this.manager = userRef {
                            this.displayName = reply0.fuelPolicy.owner.displayName
                        }
                    }))
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> customersHouseholdCalculationService.quoteHousehold(value) }
            val outbound2 = RequestFactory.create {
                summarizeDatasetsRequest {
                    this.annotations.putAll(request.annotationsMap)
                    this.context = request.context
                    this.details = SummarizeDatasetsRequestKt.details {
                        this.period = reply0.fuelPolicy.effective
                    }
                    this.reason = request.fuelPolicy.block.reason
                }
            }
            val reply2 = datasetsDatasetQueryService.summarizeDatasets(outbound2)
            val outbound3 = validateFuelCardImportRequest {
                this.context = request.context
                this.fuelCard = fuelCard {
                    this.holder = driverRef {
                        this.displayName = reply0.fuelPolicy.owner.displayName
                    }
                }
            }
            val reply3 = ClientRegistry.lookup("acme.fuel.v1.FuelCardImportService", FuelCardImportServiceGrpcKt.FuelCardImportServiceCoroutineStub::class.java).validateFuelCardImport(outbound3)
            scheduleFuelPolicyExpiryResponse {
                this.fuelPolicy = fuelPolicy {
                    this.effective = reply0.fuelPolicy.effective
                    this.fuelPolicyId = request.fuelPolicyId
                    this.owner = userRef {
                        this.email = reply3.fuelCard.holder.email
                    }
                    this.parameters.putAll(request.details.parametersMap)
                    this.status = reply0.fuelPolicy.status
                }
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.details.putAll(reply1.result.detailsMap)
                    this.message = reply2.result.message
                    this.status = "ok"
                }
            }
        }

    /** Suspends a fuel policy. */
    override suspend fun suspendFuelPolicy(request: SuspendFuelPolicyRequest): SuspendFuelPolicyResponse {
        return handleSuspendFuelPolicy(request)
    }

    /** Terminates a fuel policy. */
    override suspend fun terminateFuelPolicy(request: TerminateFuelPolicyRequest): TerminateFuelPolicyResponse = handleTerminateFuelPolicy(request)

    private suspend fun handleActivateFuelPolicy(request: ActivateFuelPolicyRequest): ActivateFuelPolicyResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyLifecycleService/ActivateFuelPolicy") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ActivateFuelPolicy", request)
            log.atInfo().addKeyValue("operation", "ActivateFuelPolicy").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = listFuelPolicyImportsRequest {
                this.context = request.context
                this.filter = ListFuelPolicyImportsRequestKt.filter {
                    this.ownerId = request.details.newOwner.userId
                }
            }
            val reply0 = withTimeout(5_000) { fuelFuelPolicyImportService.listFuelPolicyImports(outbound0) }
            val configure1: CompareShowOptionsRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.show = show {
                    this.organizer = userRef {
                        this.userId = request.details.newOwner.userId
                    }
                    this.participants.addAll(listOf(showParticipant {
                        this.contact = contactInfo {
                            this.email = request.details.newOwner.email
                            this.phone = request.details.notifyContact.phone
                            this.preferredChannel = request.details.notifyContact.preferredChannel
                        }
                    }))
                }
            }
            val outbound1 = compareShowOptionsRequest(configure1)
            val reply1 = showsShowCalculationService.compareShowOptions(outbound1)
            activateFuelPolicyResponse {
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.etag = reply1.show.audit.etag
                    }
                    this.fuelPolicyId = request.fuelPolicyId
                }
                this.outcome = ActivateFuelPolicyResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }

    private fun ExpireFuelPolicyRequest.toNormalizeAccessPolicyFromExpireFuelPolicy3(): NormalizeAccessPolicyRequest {
        val source = this
        return normalizeAccessPolicyRequest {
            this.accessPolicy = accessPolicy {
                this.owner = userRef {
                    this.userId = source.details.newOwner.userId
                }
            }
            this.context = source.context
        }
    }

    private suspend fun handleExpireFuelPolicy(request: ExpireFuelPolicyRequest): ExpireFuelPolicyResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyLifecycleService/ExpireFuelPolicy") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExpireFuelPolicy", request)
            log.atInfo().addKeyValue("operation", "ExpireFuelPolicy").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = listYardMoveReportsRequest {
                this.context = request.context
                this.filter = ListYardMoveReportsRequestKt.filter {
                    this.createdAfter = request.details.effectiveAt
                    this.labels.putAll(request.annotationsMap)
                }
            }
            val reply0 = yardsYardMoveReportingService.listYardMoveReports(outbound0)
            val outbound1 = searchVenuesRequest {
                this.context = request.context
                this.filter = SearchVenuesRequestKt.filter {
                    this.createdAfter = request.details.effectiveAt
                    this.labels.putAll(reply0.result.detailsMap)
                    this.ownerId = request.details.newOwner.userId
                }
            }
            val reply1 = venuesVenueService.searchVenues(outbound1)
            val outbound2 = explainConsentRecordDecisionRequest {}.customersCopy {
                this.context = request.context
                this.details = ExplainConsentRecordDecisionRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.details.notifyContact.email
                        this.name = request.details.notifyContact.name
                        this.phone = request.details.notifyContact.phone
                        this.preferredChannel = request.details.notifyContact.preferredChannel
                    }
                }
            }
            val reply2 = customersConsentRecordValidationService.explainConsentRecordDecision(outbound2)
            val outbound3 = request.toNormalizeAccessPolicyFromExpireFuelPolicy3()
            val reply3 = authorizationAccessPolicyValidationService.normalizeAccessPolicy(outbound3)
            expireFuelPolicyResponse {
                this.fuelPolicy = fuelPolicy {
                    this.description = reply3.accessPolicy.description
                    this.fuelPolicyId = request.fuelPolicyId
                }
                this.outcome = ExpireFuelPolicyResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.nextSteps.addAll(reply2.outcome.nextStepsList)
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply1.result.message
                    this.status = "ok"
                }
            }
        }

    private data class ReinstateFuelPolicy2Command(
        val context: RequestContext,
        val deliveryLinesAttributes: Map<String, String>,
        val deliveryCounterpartyDisplayName: String
    ) {
        fun toRequest(): UpdateDeliveryRequest {
            val command = this
            return updateDeliveryRequest {
                this.context = command.context
                this.delivery = delivery {
                    this.counterparty = deliveryExceptionRef {
                        this.displayName = command.deliveryCounterpartyDisplayName
                    }
                    this.lines.addAll(listOf(deliveryLine {
                        this.attributes.putAll(command.deliveryLinesAttributes)
                    }))
                }
            }
        }
    }

    private suspend fun handleReinstateFuelPolicy(request: ReinstateFuelPolicyRequest): ReinstateFuelPolicyResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyLifecycleService/ReinstateFuelPolicy") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ReinstateFuelPolicy", request)
            log.atInfo().addKeyValue("operation", "ReinstateFuelPolicy").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = summarizeNotificationsRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.reason = request.reason
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = notificationsNotificationQueryService.summarizeNotifications(envelope0.unpack(SummarizeNotificationsRequest::class.java))
            val outbound1 = reconcileFuelTransactionsRequest {
                this.context = request.context
                this.fuelTransaction = fuelTransaction {
                    this.subject = customerRef {
                        this.displayName = request.details.newOwner.displayName
                        this.email = request.details.newOwner.email
                    }
                }
            }
            val reply1 = fuelFuelTransactionSyncService.reconcileFuelTransactions(WireCopies.json(outbound1))
            val outbound2 = ReinstateFuelPolicy2Command(
                context = request.context,
                deliveryLinesAttributes = reply1.fuelTransaction.attributesMap,
                deliveryCounterpartyDisplayName = request.details.newOwner.displayName
            ).toRequest()
            val reply2 = deliveriesDeliveryService.updateDelivery(outbound2)
            val outbound3 = syncFuelCardsRequest {
                this.context = request.context
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.etag = reply2.delivery.audit.etag
                    }
                    this.bank = fuelCardBankFunding {
                        this.accountHolder = reply2.delivery.bank.accountHolder
                        this.iban = reply2.delivery.bank.iban
                    }
                    this.contact = contactInfo {
                        this.email = request.details.newOwner.email
                    }
                }
            }
            val command3: Submission<SyncFuelCardsRequest> = Submission.Live(outbound3)
            val payload3 = when (val command = command3) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply3 = fuelFuelCardSyncService.syncFuelCards(payload3)
            reinstateFuelPolicyResponse {
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.createdAt = reply2.delivery.audit.createdAt
                    }
                    this.fuelPolicyId = request.fuelPolicyId
                    this.owner = userRef {
                        this.email = reply3.fuelCard.holder.email
                        this.userId = request.details.newOwner.userId
                    }
                }
                this.outcome = ReinstateFuelPolicyResponseKt.outcome {
                    this.effectiveAt = reply3.outcome.effectiveAt
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.details.putAll(reply2.result.detailsMap)
                    this.message = reply1.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleRenewFuelPolicy(request: RenewFuelPolicyRequest): RenewFuelPolicyResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyLifecycleService/RenewFuelPolicy") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RenewFuelPolicy", request)
            log.atInfo().addKeyValue("operation", "RenewFuelPolicy").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = retryFuelTransactionImportRequest {
                this.context = request.context
                this.details = RetryFuelTransactionImportRequestKt.details {
                    this.notify = contactInfo {
                        this.phone = request.details.notifyContact.phone
                    }
                }
            }
            val adapter0 = OutboundPort<RetryFuelTransactionImportRequest, RetryFuelTransactionImportResponse> { payload ->
                fuelFuelTransactionImportService.retryFuelTransactionImport(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = quoteDutyStatusRequest {
                this.context = request.context
                this.dutyStatus = dutyStatus {
                    this.source = reply0.fuelTransaction.source
                    this.subject = driverLicenseRef {
                        this.displayName = request.details.newOwner.displayName
                    }
                }
            }
            val reply1 = invokeRenewFuelPolicy1(outbound1)
            renewFuelPolicyResponse {
                this.fuelPolicy = fuelPolicy {
                    this.fuelPolicyId = request.fuelPolicyId
                }
                this.outcome = RenewFuelPolicyResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.correlationId = reply0.fuelTransaction.correlationId
                    this.details.putAll(reply1.result.detailsMap)
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSuspendFuelPolicy(request: SuspendFuelPolicyRequest): SuspendFuelPolicyResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyLifecycleService/SuspendFuelPolicy") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SuspendFuelPolicy", request)
            log.atInfo().addKeyValue("operation", "SuspendFuelPolicy").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = listPendingProofOfDeliveriesRequest {
                this.context = request.context
                this.filter = ListPendingProofOfDeliveriesRequestKt.filter {
                    this.createdAfter = request.details.effectiveAt
                    this.labels.putAll(request.annotationsMap)
                    this.ownerId = request.details.newOwner.userId
                }
            }
            val reply0 = deliveriesProofOfDeliveryApprovalService.listPendingProofOfDeliveries(WireCopies.reflective(outbound0))
            val outbound1 = scoreUserRequest {
                this.context = request.context
                this.user = user {
                    this.contact = contactInfo {
                        this.email = request.details.notifyContact.email
                        this.phone = request.details.notifyContact.phone
                        this.preferredChannel = request.details.notifyContact.preferredChannel
                    }
                    this.name = request.details.notifyContact.name
                }
            }
            val reply1 = workerForSuspendFuelPolicy1.submit(outbound1)
            val outbound2 = normalizeConsentRecordRequest {
                this.context = request.context
                this.details = NormalizeConsentRecordRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.details.notifyContact.email
                        this.phone = reply1.user.contact.phone
                        this.preferredChannel = request.details.notifyContact.preferredChannel
                    }
                }
            }
            val reply2 = clientForSuspendFuelPolicy2.normalizeConsentRecord(outbound2)
            suspendFuelPolicyResponse {
                this.fuelPolicy = fuelPolicy {
                    this.fuelPolicyId = request.fuelPolicyId
                    this.owner = userRef {
                        this.displayName = reply2.consentRecord.subject.displayName
                    }
                }
                this.outcome = SuspendFuelPolicyResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.message = reply2.result.message
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleTerminateFuelPolicy(request: TerminateFuelPolicyRequest): TerminateFuelPolicyResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyLifecycleService/TerminateFuelPolicy") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "TerminateFuelPolicy", request)
            log.atInfo().addKeyValue("operation", "TerminateFuelPolicy").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = findSecretsRequest {
                this.context = request.context
                this.filter = FindSecretsRequestKt.filter {
                    this.createdAfter = request.details.effectiveAt
                }
            }
            val reply0 = flowOf(outbound0).map { value -> secretsSecretQueryService.findSecrets(value) }.single()
            val outbound1 = listPendingProofOfDeliveriesRequest {
                this.context = request.context
                this.filter = ListPendingProofOfDeliveriesRequestKt.filter {
                    this.ownerId = request.details.newOwner.userId
                }
            }
            val reply1 = withTimeout(5_000) { deliveriesProofOfDeliveryApprovalService.listPendingProofOfDeliveries(outbound1) }
            val configure2: ExplainUserDecisionRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.user = user {
                    this.contact = contactInfo {
                        this.email = request.details.notifyContact.email
                        this.name = request.details.notifyContact.name
                        this.phone = request.details.notifyContact.phone
                    }
                    this.relationshipManager = userRef {
                        this.displayName = request.details.newOwner.displayName
                    }
                }
            }
            val outbound2 = explainUserDecisionRequest(configure2)
            val reply2 = identityUserValidationService.explainUserDecision(outbound2)
            terminateFuelPolicyResponse {
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.updatedAt = reply2.user.audit.updatedAt
                    }
                    this.fuelPolicyId = request.fuelPolicyId
                }
                this.outcome = TerminateFuelPolicyResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.message = reply1.result.message
                    this.status = reply0.result.status
                }
            }
        }
}
