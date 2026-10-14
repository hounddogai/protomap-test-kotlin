package com.acme.transport.fuel.api

import com.acme.authorization.v1.AccessPolicyQueryServiceGrpcKt
import com.acme.authorization.v1.resolveAccessPoliciesRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.contracts.v1.CloneContractRequest
import com.acme.contracts.v1.CloneContractResponse
import com.acme.contracts.v1.ContractServiceGrpcKt
import com.acme.contracts.v1.cloneContractRequest
import com.acme.contracts.v1.contract
import com.acme.contracts.v1.contractCardSettlement
import com.acme.customers.v1.customerRef
import com.acme.drivers.v1.CountDriversRequestKt
import com.acme.drivers.v1.CountDriversResponse
import com.acme.drivers.v1.DriverCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverQueryServiceGrpcKt
import com.acme.drivers.v1.DriverServiceGrpcKt
import com.acme.drivers.v1.EstimateDriverRequest
import com.acme.drivers.v1.countDriversRequest
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.estimateDriverRequest
import com.acme.drivers.v1.watchDriversRequest
import com.acme.featureflags.v1.FeatureFlagValidationServiceGrpcKt
import com.acme.featureflags.v1.ValidateFeatureFlagRequestKt
import com.acme.featureflags.v1.featureFlag
import com.acme.featureflags.v1.validateFeatureFlagRequest
import com.acme.fuel.v1.ActivateFuelTransactionRequest
import com.acme.fuel.v1.ActivateFuelTransactionResponse
import com.acme.fuel.v1.ActivateFuelTransactionResponseKt
import com.acme.fuel.v1.ExpireFuelTransactionRequest
import com.acme.fuel.v1.ExpireFuelTransactionResponse
import com.acme.fuel.v1.ExpireFuelTransactionResponseKt
import com.acme.fuel.v1.FuelCardSyncServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionImportServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionLifecycleServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionQueryServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionSyncServiceGrpcKt
import com.acme.fuel.v1.GetFuelTransactionLifecycleRequest
import com.acme.fuel.v1.GetFuelTransactionLifecycleResponse
import com.acme.fuel.v1.ListFuelTransactionImportsRequestKt
import com.acme.fuel.v1.MapFuelTransactionImportColumnsRequestKt
import com.acme.fuel.v1.ReinstateFuelTransactionRequest
import com.acme.fuel.v1.ReinstateFuelTransactionResponse
import com.acme.fuel.v1.ReinstateFuelTransactionResponseKt
import com.acme.fuel.v1.RenewFuelTransactionRequest
import com.acme.fuel.v1.RenewFuelTransactionResponse
import com.acme.fuel.v1.RenewFuelTransactionResponseKt
import com.acme.fuel.v1.RetryFuelTransactionImportRequestKt
import com.acme.fuel.v1.ScheduleFuelTransactionExpiryRequest
import com.acme.fuel.v1.ScheduleFuelTransactionExpiryResponse
import com.acme.fuel.v1.SuspendFuelTransactionRequest
import com.acme.fuel.v1.SuspendFuelTransactionResponse
import com.acme.fuel.v1.SuspendFuelTransactionResponseKt
import com.acme.fuel.v1.TerminateFuelTransactionRequest
import com.acme.fuel.v1.TerminateFuelTransactionResponse
import com.acme.fuel.v1.TerminateFuelTransactionResponseKt
import com.acme.fuel.v1.activateFuelTransactionResponse
import com.acme.fuel.v1.expireFuelTransactionResponse
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.getFuelTransactionLifecycleResponse
import com.acme.fuel.v1.listFuelTransactionImportsRequest
import com.acme.fuel.v1.lookupFuelTransactionRequest
import com.acme.fuel.v1.mapFuelTransactionImportColumnsRequest
import com.acme.fuel.v1.reinstateFuelTransactionResponse
import com.acme.fuel.v1.renewFuelTransactionResponse
import com.acme.fuel.v1.retryFuelTransactionImportRequest
import com.acme.fuel.v1.scheduleFuelTransactionExpiryResponse
import com.acme.fuel.v1.suspendFuelTransactionResponse
import com.acme.fuel.v1.syncFuelCardsRequest
import com.acme.fuel.v1.syncFuelTransactionsRequest
import com.acme.fuel.v1.terminateFuelTransactionResponse
import com.acme.identity.v1.userRef
import com.acme.transport.fuel.FuelRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.ApproveYardRequest
import com.acme.yards.v1.ApproveYardRequestKt
import com.acme.yards.v1.DockApprovalServiceGrpcKt
import com.acme.yards.v1.SubmitYardRequest
import com.acme.yards.v1.YardApprovalServiceGrpcKt
import com.acme.yards.v1.approveYardRequest
import com.acme.yards.v1.dock
import com.acme.yards.v1.submitDockRequest
import com.acme.yards.v1.submitYardRequest
import com.acme.yards.v1.yard
import com.google.protobuf.Any as ProtoAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Moves a fuel transaction between the states of its lifecycle. */
class FuelTransactionLifecycleGrpcService(
    private val store: FuelRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyQueryService: AccessPolicyQueryServiceGrpcKt.AccessPolicyQueryServiceCoroutineStub,
    private val contractsContractService: ContractServiceGrpcKt.ContractServiceCoroutineStub,
    private val driversDriverCalculationService: DriverCalculationServiceGrpcKt.DriverCalculationServiceCoroutineStub,
    private val driversDriverQueryService: DriverQueryServiceGrpcKt.DriverQueryServiceCoroutineStub,
    private val driversDriverService: DriverServiceGrpcKt.DriverServiceCoroutineStub,
    private val featureflagsFeatureFlagValidationService: FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub,
    private val fuelFuelCardSyncService: FuelCardSyncServiceGrpcKt.FuelCardSyncServiceCoroutineStub,
    private val fuelFuelTransactionImportService: FuelTransactionImportServiceGrpcKt.FuelTransactionImportServiceCoroutineStub,
    private val fuelFuelTransactionQueryService: FuelTransactionQueryServiceGrpcKt.FuelTransactionQueryServiceCoroutineStub,
    private val fuelFuelTransactionSyncService: FuelTransactionSyncServiceGrpcKt.FuelTransactionSyncServiceCoroutineStub,
    private val yardsDockApprovalService: DockApprovalServiceGrpcKt.DockApprovalServiceCoroutineStub,
    private val yardsYardApprovalService: YardApprovalServiceGrpcKt.YardApprovalServiceCoroutineStub,
) : FuelTransactionLifecycleServiceGrpcKt.FuelTransactionLifecycleServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(FuelTransactionLifecycleGrpcService::class.java)

    private val workerForExpireFuelTransaction1 = ReplyMailbox<CloneContractRequest, CloneContractResponse>(workers) { payload ->
        withTimeout(5_000) { contractsContractService.cloneContract(payload) }
    }

    /** Activates a fuel transaction. */
    override suspend fun activateFuelTransaction(request: ActivateFuelTransactionRequest): ActivateFuelTransactionResponse {
        return handleActivateFuelTransaction(request)
    }

    /** Expires a fuel transaction. */
    override suspend fun expireFuelTransaction(request: ExpireFuelTransactionRequest): ExpireFuelTransactionResponse = handleExpireFuelTransaction(request)

    /** Returns a fuel transaction. */
    override suspend fun getFuelTransactionLifecycle(request: GetFuelTransactionLifecycleRequest): GetFuelTransactionLifecycleResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionLifecycleService/GetFuelTransactionLifecycle") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetFuelTransactionLifecycle", request)
            log.atInfo().addKeyValue("operation", "GetFuelTransactionLifecycle").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            getFuelTransactionLifecycleResponse {
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Reinstates a fuel transaction. */
    override suspend fun reinstateFuelTransaction(request: ReinstateFuelTransactionRequest): ReinstateFuelTransactionResponse {
        return handleReinstateFuelTransaction(request)
    }

    /** Renews a fuel transaction. */
    override suspend fun renewFuelTransaction(request: RenewFuelTransactionRequest): RenewFuelTransactionResponse = handleRenewFuelTransaction(request)

    /** Schedules a fuel transaction. */
    override suspend fun scheduleFuelTransactionExpiry(request: ScheduleFuelTransactionExpiryRequest): ScheduleFuelTransactionExpiryResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionLifecycleService/ScheduleFuelTransactionExpiry") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScheduleFuelTransactionExpiry", request)
            log.atInfo().addKeyValue("operation", "ScheduleFuelTransactionExpiry").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransaction.fuelTransactionId).log("Recorded audit detail")
            var reply0 = CountDriversResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.fuelTransaction.measurementsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound0 = countDriversRequest {
                                this.context = request.context
                                this.filter = CountDriversRequestKt.filter {
                                    this.createdAfter = item.observedAt
                                    this.ownerId = request.fuelTransaction.subject.customerId
                                }
                            }
                            driversDriverQueryService.countDrivers(outbound0)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply0 = it }
                }
            }
            scheduleFuelTransactionExpiryResponse {
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                    this.kind = request.fuelTransaction.kind
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Suspends a fuel transaction. */
    override suspend fun suspendFuelTransaction(request: SuspendFuelTransactionRequest): SuspendFuelTransactionResponse {
        return handleSuspendFuelTransaction(request)
    }

    /** Terminates a fuel transaction. */
    override suspend fun terminateFuelTransaction(request: TerminateFuelTransactionRequest): TerminateFuelTransactionResponse = handleTerminateFuelTransaction(request)

    private suspend fun handleActivateFuelTransaction(request: ActivateFuelTransactionRequest): ActivateFuelTransactionResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionLifecycleService/ActivateFuelTransaction") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ActivateFuelTransaction", request)
            log.atInfo().addKeyValue("operation", "ActivateFuelTransaction").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = EstimateDriverRequest.newBuilder().apply {
                setContext(request.context)
                setDriver(driver {
                    this.contact = contactInfo {
                        this.email = request.details.newOwner.email
                        this.name = request.details.notifyContact.name
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> driversDriverCalculationService.estimateDriver(value) }
            val outbound1 = RequestFactory.create {
                lookupFuelTransactionRequest {
                    this.context = request.context
                    this.fuelTransactionId = request.fuelTransactionId
                }
            }
            val reply1 = fuelFuelTransactionQueryService.lookupFuelTransaction(outbound1)
            val outbound2 = submitDockRequest {
                this.context = request.context
                this.dock = dock {
                    this.address = postalAddress {
                        this.countryCode = reply0.driver.primaryAddress.countryCode
                        this.recipient = reply0.driver.primaryAddress.recipient
                    }
                    this.owner = userRef {
                        this.email = reply0.driver.contact.email
                    }
                }
            }
            val reply2 = ClientRegistry.lookup("acme.yards.v1.DockApprovalService", DockApprovalServiceGrpcKt.DockApprovalServiceCoroutineStub::class.java).submitDock(outbound2)
            activateFuelTransactionResponse {
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = reply1.fuelTransaction.fuelTransactionId
                    this.source = reply1.fuelTransaction.source
                    this.subject = customerRef {
                        this.email = request.details.newOwner.email
                    }
                }
                this.outcome = ActivateFuelTransactionResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.message = reply2.result.message
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleExpireFuelTransaction(request: ExpireFuelTransactionRequest): ExpireFuelTransactionResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionLifecycleService/ExpireFuelTransaction") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExpireFuelTransaction", request)
            log.atInfo().addKeyValue("operation", "ExpireFuelTransaction").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = syncFuelCardsRequest {
                this.context = request.context
                this.fuelCard = fuelCard {
                    this.contact = contactInfo {
                        this.email = request.details.notifyContact.email
                        this.name = request.details.notifyContact.name
                        this.preferredChannel = request.details.notifyContact.preferredChannel
                    }
                    this.holder = driverRef {
                        this.email = request.details.newOwner.email
                    }
                }
            }
            val reply0 = fuelFuelCardSyncService.syncFuelCards(WireCopies.reflective(outbound0))
            val outbound1 = cloneContractRequest {
                this.context = request.context
                this.contract = contract {
                    this.card = contractCardSettlement {
                        this.last4 = reply0.fuelCard.card.last4
                        this.token = reply0.fuelCard.card.token
                    }
                }
            }
            val reply1 = workerForExpireFuelTransaction1.submit(outbound1)
            val outbound2 = watchDriversRequest {
                this.context = request.context
                this.subscriber = contactInfo {
                    this.email = reply0.fuelCard.holder.email
                }
            }
            driversDriverService.watchDrivers(outbound2).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            expireFuelTransactionResponse {
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                }
                this.outcome = ExpireFuelTransactionResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.message = reply1.result.message
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleReinstateFuelTransaction(request: ReinstateFuelTransactionRequest): ReinstateFuelTransactionResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionLifecycleService/ReinstateFuelTransaction") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ReinstateFuelTransaction", request)
            log.atInfo().addKeyValue("operation", "ReinstateFuelTransaction").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = retryFuelTransactionImportRequest {
                this.context = request.context
                this.details = RetryFuelTransactionImportRequestKt.details {
                    this.notify = contactInfo {
                        this.email = request.details.notifyContact.email
                        this.name = request.details.notifyContact.name
                        this.preferredChannel = request.details.notifyContact.preferredChannel
                    }
                }
                this.fuelTransaction = fuelTransaction {
                    this.subject = customerRef {
                        this.email = request.details.newOwner.email
                    }
                }
            }
            val reply0 = flowOf(outbound0).map { value -> fuelFuelTransactionImportService.retryFuelTransactionImport(value) }.single()
            val outbound1 = resolveAccessPoliciesRequest {
                this.context = request.context
            }
            val reply1 = withTimeout(5_000) { authorizationAccessPolicyQueryService.resolveAccessPolicies(outbound1) }
            reinstateFuelTransactionResponse {
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                }
                this.outcome = ReinstateFuelTransactionResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.message = reply0.outcome.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleRenewFuelTransaction(request: RenewFuelTransactionRequest): RenewFuelTransactionResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionLifecycleService/RenewFuelTransaction") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RenewFuelTransaction", request)
            log.atInfo().addKeyValue("operation", "RenewFuelTransaction").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val configure0: ValidateFeatureFlagRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.details = ValidateFeatureFlagRequestKt.details {
                    this.contact = contactInfo {
                        this.name = request.details.notifyContact.name
                        this.phone = request.details.notifyContact.phone
                    }
                }
            }
            val outbound0 = validateFeatureFlagRequest(configure0)
            val reply0 = featureflagsFeatureFlagValidationService.validateFeatureFlag(outbound0)
            val outbound1 = listFuelTransactionImportsRequest {
                this.context = request.context
                this.filter = ListFuelTransactionImportsRequestKt.filter {
                    this.createdAfter = reply0.outcome.effectiveAt
                }
            }
            val reply1 = fuelFuelTransactionImportService.listFuelTransactionImports(outbound1)
            val outbound2 = mapFuelTransactionImportColumnsRequest {
                this.context = request.context
                this.details = MapFuelTransactionImportColumnsRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.details.notifyContact.email
                    }
                }
                this.fuelTransaction = fuelTransaction {
                    this.subject = customerRef {
                        this.email = reply0.featureFlag.owner.email
                    }
                }
            }
            val reply2 = fuelFuelTransactionImportService.mapFuelTransactionImportColumns(outbound2)
            renewFuelTransactionResponse {
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                    this.subject = customerRef {
                        this.email = request.details.newOwner.email
                    }
                }
                this.outcome = RenewFuelTransactionResponseKt.outcome {
                    this.effectiveAt = reply0.outcome.effectiveAt
                }
                this.result = operationResult {
                    this.details.putAll(reply2.result.detailsMap)
                    this.status = reply1.result.status
                }
            }
        }

    private fun SuspendFuelTransactionRequest.toEstimateDriverFromSuspendFuelTransaction0(): EstimateDriverRequest {
        val source = this
        return estimateDriverRequest {
            this.context = source.context
            this.driver = driver {
                this.contact = contactInfo {
                    this.preferredChannel = source.details.notifyContact.preferredChannel
                }
                this.relationshipManager = userRef {
                    this.email = source.details.newOwner.email
                }
            }
        }
    }

    private suspend fun handleSuspendFuelTransaction(request: SuspendFuelTransactionRequest): SuspendFuelTransactionResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionLifecycleService/SuspendFuelTransaction") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SuspendFuelTransaction", request)
            log.atInfo().addKeyValue("operation", "SuspendFuelTransaction").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = request.toEstimateDriverFromSuspendFuelTransaction0()
            val reply0 = driversDriverCalculationService.estimateDriver(outbound0)
            val outbound1 = validateFeatureFlagRequest {
                this.context = request.context
                this.details = ValidateFeatureFlagRequestKt.details {
                    this.contact = contactInfo {
                        this.email = reply0.driver.contact.email
                        this.phone = reply0.driver.contact.phone
                        this.preferredChannel = reply0.driver.contact.preferredChannel
                    }
                }
                this.featureFlag = featureFlag {
                    this.owner = userRef {
                        this.email = request.details.notifyContact.email
                    }
                }
            }
            val reply1 = featureflagsFeatureFlagValidationService.validateFeatureFlag(outbound1)
            val outbound2 = submitYardRequest {
                this.context = request.context
                this.yard = yard {
                    this.audit = auditInfo {
                        this.etag = reply1.featureFlag.audit.etag
                    }
                    this.owner = userRef {
                        this.email = reply1.featureFlag.owner.email
                    }
                }
            }
            val envelope2 = ProtoAny.pack(outbound2)
            val reply2 = yardsYardApprovalService.submitYard(envelope2.unpack(SubmitYardRequest::class.java))
            suspendFuelTransactionResponse {
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                }
                this.outcome = SuspendFuelTransactionResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.status = reply2.outcome.status
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = reply0.result.status
                }
            }
        }

    private data class TerminateFuelTransaction1Command(
        val context: RequestContext,
        val detailsApproverEmail: String
    ) {
        fun toRequest(): ApproveYardRequest {
            val command = this
            return approveYardRequest {
                this.context = command.context
                this.details = ApproveYardRequestKt.details {
                    this.approver = userRef {
                        this.email = command.detailsApproverEmail
                    }
                }
            }
        }
    }

    private suspend fun handleTerminateFuelTransaction(request: TerminateFuelTransactionRequest): TerminateFuelTransactionResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionLifecycleService/TerminateFuelTransaction") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "TerminateFuelTransaction", request)
            log.atInfo().addKeyValue("operation", "TerminateFuelTransaction").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = syncFuelTransactionsRequest {
                this.context = request.context
                this.fuelTransaction = fuelTransaction {
                    this.subject = customerRef {
                        this.displayName = request.details.newOwner.displayName
                        this.email = request.details.newOwner.email
                    }
                }
                this.reason = request.reason
            }
            val reply0 = fuelFuelTransactionSyncService.syncFuelTransactions(WireCopies.json(outbound0))
            val outbound1 = TerminateFuelTransaction1Command(
                context = request.context,
                detailsApproverEmail = request.details.newOwner.email
            ).toRequest()
            val reply1 = yardsYardApprovalService.approveYard(outbound1)
            terminateFuelTransactionResponse {
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                    this.subject = customerRef {
                        this.displayName = reply0.fuelTransaction.subject.displayName
                        this.email = request.details.notifyContact.email
                    }
                }
                this.outcome = TerminateFuelTransactionResponseKt.outcome {
                    this.effectiveAt = reply1.outcome.effectiveAt
                }
                this.result = operationResult {
                    this.status = reply1.outcome.status
                }
            }
        }
}
