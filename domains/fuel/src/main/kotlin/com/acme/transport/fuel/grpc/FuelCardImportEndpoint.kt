package com.acme.transport.fuel.grpc

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.CustomerSegmentCalculationServiceGrpcKt
import com.acme.customers.v1.ExplainCustomerSegmentCalculationRequestKt
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.explainCustomerSegmentCalculationRequest
import com.acme.fuel.v1.BrowseFuelTransactionsRequest
import com.acme.fuel.v1.BrowseFuelTransactionsRequestKt
import com.acme.fuel.v1.BrowseFuelTransactionsResponse
import com.acme.fuel.v1.CancelFuelCardImportRequest
import com.acme.fuel.v1.CancelFuelCardImportResponse
import com.acme.fuel.v1.CancelFuelCardImportResponseKt
import com.acme.fuel.v1.EstimateFuelPolicyRequest
import com.acme.fuel.v1.EstimateFuelPolicyRequestKt
import com.acme.fuel.v1.ExplainFuelTransactionCalculationRequest
import com.acme.fuel.v1.ExplainFuelTransactionCalculationRequestKt
import com.acme.fuel.v1.ExplainFuelTransactionCalculationResponse
import com.acme.fuel.v1.FuelCardCalculationServiceGrpcKt
import com.acme.fuel.v1.FuelCardExportServiceGrpcKt
import com.acme.fuel.v1.FuelCardImportServiceGrpcKt
import com.acme.fuel.v1.FuelCardValidationServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyCalculationServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionCalculationServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionQueryServiceGrpcKt
import com.acme.fuel.v1.GetFuelCardImportStatusRequest
import com.acme.fuel.v1.GetFuelCardImportStatusResponse
import com.acme.fuel.v1.ImportFuelCardsRequest
import com.acme.fuel.v1.ImportFuelCardsResponse
import com.acme.fuel.v1.ImportFuelCardsResponseKt
import com.acme.fuel.v1.ListFuelCardExportsRequestKt
import com.acme.fuel.v1.ListFuelCardImportsRequest
import com.acme.fuel.v1.ListFuelCardImportsResponse
import com.acme.fuel.v1.MapFuelCardImportColumnsRequest
import com.acme.fuel.v1.MapFuelCardImportColumnsResponse
import com.acme.fuel.v1.MapFuelCardImportColumnsResponseKt
import com.acme.fuel.v1.QuoteFuelCardRequest
import com.acme.fuel.v1.QuoteFuelCardResponse
import com.acme.fuel.v1.RetryFuelCardImportRequest
import com.acme.fuel.v1.RetryFuelCardImportResponse
import com.acme.fuel.v1.RetryFuelCardImportResponseKt
import com.acme.fuel.v1.SimulateFuelTransactionRequest
import com.acme.fuel.v1.ValidateFuelCardImportRequest
import com.acme.fuel.v1.ValidateFuelCardImportResponse
import com.acme.fuel.v1.browseFuelTransactionsRequest
import com.acme.fuel.v1.cancelFuelCardImportResponse
import com.acme.fuel.v1.estimateFuelPolicyRequest
import com.acme.fuel.v1.explainFuelTransactionCalculationRequest
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.getFuelCardExportRequest
import com.acme.fuel.v1.getFuelCardImportStatusResponse
import com.acme.fuel.v1.importFuelCardsResponse
import com.acme.fuel.v1.listFuelCardExportsRequest
import com.acme.fuel.v1.listFuelCardImportsResponse
import com.acme.fuel.v1.mapFuelCardImportColumnsResponse
import com.acme.fuel.v1.quoteFuelCardRequest
import com.acme.fuel.v1.retryFuelCardImportResponse
import com.acme.fuel.v1.simulateFuelTransactionRequest
import com.acme.fuel.v1.validateFuelCardImportResponse
import com.acme.fuel.v1.validateFuelCardRequest
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.LedgerAccountValidationServiceGrpcKt
import com.acme.ledger.v1.NormalizeLedgerAccountResponse
import com.acme.ledger.v1.ledgerAccount
import com.acme.ledger.v1.ledgerAccountLimit
import com.acme.ledger.v1.normalizeLedgerAccountRequest
import com.acme.notifications.v1.NotificationValidationServiceGrpcKt
import com.acme.notifications.v1.ValidateNotificationRequest
import com.acme.notifications.v1.ValidateNotificationRequestKt
import com.acme.notifications.v1.notification
import com.acme.notifications.v1.previewNotificationRequest
import com.acme.notifications.v1.validateNotificationRequest
import com.acme.performers.v1.CountPerformersRequestKt
import com.acme.performers.v1.PerformerQueryServiceGrpcKt
import com.acme.performers.v1.countPerformersRequest
import com.acme.transport.fuel.FuelRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Takes fuel cards in bulk from partners and legacy systems. */
class FuelCardImportEndpoint(
    private val store: FuelRepository,
    private val workers: CoroutineScope,
    private val customersCustomerSegmentCalculationService: CustomerSegmentCalculationServiceGrpcKt.CustomerSegmentCalculationServiceCoroutineStub,
    private val fuelFuelCardCalculationService: FuelCardCalculationServiceGrpcKt.FuelCardCalculationServiceCoroutineStub,
    private val fuelFuelCardExportService: FuelCardExportServiceGrpcKt.FuelCardExportServiceCoroutineStub,
    private val fuelFuelCardValidationService: FuelCardValidationServiceGrpcKt.FuelCardValidationServiceCoroutineStub,
    private val fuelFuelPolicyCalculationService: FuelPolicyCalculationServiceGrpcKt.FuelPolicyCalculationServiceCoroutineStub,
    private val fuelFuelTransactionCalculationService: FuelTransactionCalculationServiceGrpcKt.FuelTransactionCalculationServiceCoroutineStub,
    private val fuelFuelTransactionQueryService: FuelTransactionQueryServiceGrpcKt.FuelTransactionQueryServiceCoroutineStub,
    private val ledgerLedgerAccountValidationService: LedgerAccountValidationServiceGrpcKt.LedgerAccountValidationServiceCoroutineStub,
    private val notificationsNotificationValidationService: NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub,
    private val performersPerformerQueryService: PerformerQueryServiceGrpcKt.PerformerQueryServiceCoroutineStub,
) : FuelCardImportServiceGrpcKt.FuelCardImportServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(FuelCardImportEndpoint::class.java)

    private val invokeImportFuelCards2: suspend (QuoteFuelCardRequest) -> QuoteFuelCardResponse = { payload ->
        fuelFuelCardCalculationService.quoteFuelCard(payload)
    }

    private val workerForRetryFuelCardImport1 = ReplyMailbox<BrowseFuelTransactionsRequest, BrowseFuelTransactionsResponse>(workers) { payload ->
        withTimeout(5_000) { fuelFuelTransactionQueryService.browseFuelTransactions(payload) }
    }

    private val clientForRetryFuelCardImport2 by lazy { customersCustomerSegmentCalculationService }

    /** Cancels a fuel card. */
    override suspend fun cancelFuelCardImport(request: CancelFuelCardImportRequest): CancelFuelCardImportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardImportService/CancelFuelCardImport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CancelFuelCardImport", request)
            log.atInfo().addKeyValue("operation", "CancelFuelCardImport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = validateFuelCardRequest {
                this.context = request.context
                this.fuelCard = fuelCard {
                    this.contact = contactInfo {
                        this.preferredChannel = request.details.notifyContact.preferredChannel
                    }
                }
            }
            val reply0 = fuelFuelCardValidationService.validateFuelCard(WireCopies.json(outbound0))
            cancelFuelCardImportResponse {
                this.fuelCard = fuelCard {
                    this.currencyCode = reply0.fuelCard.currencyCode
                    this.fuelCardId = request.fuelCardId
                }
                this.outcome = CancelFuelCardImportResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.message = reply0.outcome.message
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Returns a fuel card. */
    override suspend fun getFuelCardImportStatus(request: GetFuelCardImportStatusRequest): GetFuelCardImportStatusResponse {
        return handleGetFuelCardImportStatus(request)
    }

    /** Takes a stream of fuel cards and answers once at the end. */
    override suspend fun importFuelCards(requests: Flow<ImportFuelCardsRequest>): ImportFuelCardsResponse {
        var answer = ImportFuelCardsResponse.getDefaultInstance()
        requests.map { request -> handleImportFuelCards(request) }.collect { answer = it }
        return answer
    }

    /** Lists fuel cards a page at a time. */
    override suspend fun listFuelCardImports(request: ListFuelCardImportsRequest): ListFuelCardImportsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardImportService/ListFuelCardImports") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListFuelCardImports", request)
            log.atInfo().addKeyValue("operation", "ListFuelCardImports").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = EstimateFuelPolicyRequest.newBuilder().apply {
                setContext(request.context)
                setDetails(EstimateFuelPolicyRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.filter.region
                    }
                })
                setFuelPolicy(fuelPolicy {
                    this.owner = userRef {
                        this.email = request.context.actor.email
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> fuelFuelPolicyCalculationService.estimateFuelPolicy(value) }
            listFuelCardImportsResponse {
                this.fuelCards.addAll(listOf(fuelCard {
                    this.audit = auditInfo {
                        this.etag = reply0.fuelPolicy.audit.etag
                    }
                    this.contact = contactInfo {
                        this.email = reply0.fuelPolicy.owner.email
                    }
                }))
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Maps a fuel card. */
    override suspend fun mapFuelCardImportColumns(request: MapFuelCardImportColumnsRequest): MapFuelCardImportColumnsResponse {
        return handleMapFuelCardImportColumns(request)
    }

    /** Retrys a fuel card. */
    override suspend fun retryFuelCardImport(request: RetryFuelCardImportRequest): RetryFuelCardImportResponse = handleRetryFuelCardImport(request)

    /** Validates a fuel card. */
    override suspend fun validateFuelCardImport(request: ValidateFuelCardImportRequest): ValidateFuelCardImportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardImportService/ValidateFuelCardImport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ValidateFuelCardImport", request)
            log.atInfo().addKeyValue("operation", "ValidateFuelCardImport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCard.fuelCardId).log("Recorded audit detail")
            val outbound0 = countPerformersRequest {
                this.context = request.context
                this.filter = CountPerformersRequestKt.filter {
                    this.createdAfter = request.fuelCard.openedAt
                    this.labels.putAll(request.fuelCard.labelsMap)
                }
            }
            val reply0 = flowOf(outbound0).map { value -> performersPerformerQueryService.countPerformers(value) }.single()
            validateFuelCardImportResponse {
                this.fuelCard = fuelCard {
                    this.fuelCardId = request.fuelCardId
                    this.holder = request.fuelCard.holder
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private data class GetFuelCardImportStatus0Command(
        val context: RequestContext,
        val notificationAssigneeUserId: String,
        val notificationAssigneeEmail: String,
        val detailsLocale: String,
        val notificationSubject: String
    ) {
        fun toRequest(): ValidateNotificationRequest {
            val command = this
            return validateNotificationRequest {
                this.context = command.context
                this.details = ValidateNotificationRequestKt.details {
                    this.locale = command.detailsLocale
                }
                this.notification = notification {
                    this.assignee = userRef {
                        this.email = command.notificationAssigneeEmail
                        this.userId = command.notificationAssigneeUserId
                    }
                    this.subject = command.notificationSubject
                }
            }
        }
    }

    private suspend fun handleGetFuelCardImportStatus(request: GetFuelCardImportStatusRequest): GetFuelCardImportStatusResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardImportService/GetFuelCardImportStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetFuelCardImportStatus", request)
            log.atInfo().addKeyValue("operation", "GetFuelCardImportStatus").log("Handling transport request")
            val outbound0 = GetFuelCardImportStatus0Command(
                context = request.context,
                notificationAssigneeUserId = request.context.actor.userId,
                notificationAssigneeEmail = request.context.actor.email,
                detailsLocale = request.context.locale,
                notificationSubject = request.fuelCardId
            ).toRequest()
            val reply0 = notificationsNotificationValidationService.validateNotification(outbound0)
            getFuelCardImportStatusResponse {
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.etag = reply0.notification.audit.etag
                    }
                    this.contact = contactInfo {
                        this.name = reply0.notification.requester.name
                    }
                    this.fuelCardId = request.fuelCardId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleImportFuelCards(request: ImportFuelCardsRequest): ImportFuelCardsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardImportService/ImportFuelCards") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ImportFuelCards", request)
            log.atInfo().addKeyValue("operation", "ImportFuelCards").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.header.source).log("Recorded audit detail")
            val outbound0 = simulateFuelTransactionRequest {
                this.context = request.context
                this.fuelTransaction = fuelTransaction {
                    this.subject = customerRef {
                        this.displayName = request.record.holder.displayName
                    }
                }
            }
            val command0: Submission<SimulateFuelTransactionRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = fuelFuelTransactionCalculationService.simulateFuelTransaction(payload0)
            val outbound1 = explainFuelTransactionCalculationRequest {
                this.context = request.context
                this.details = ExplainFuelTransactionCalculationRequestKt.details {
                    this.contact = contactInfo {
                        this.phone = request.record.contact.phone
                    }
                }
                this.fuelTransaction = fuelTransaction {
                    this.subject = customerRef {
                        this.customerId = reply0.fuelTransaction.subject.customerId
                        this.email = reply0.fuelTransaction.subject.email
                    }
                }
            }
            val adapter1 = OutboundPort<ExplainFuelTransactionCalculationRequest, ExplainFuelTransactionCalculationResponse> { payload ->
                fuelFuelTransactionCalculationService.explainFuelTransactionCalculation(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            val outbound2 = quoteFuelCardRequest {
                this.context = request.context
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.updatedBy = request.record.audit.updatedBy
                    }
                }
            }
            val reply2 = invokeImportFuelCards2(outbound2)
            var reply3 = NormalizeLedgerAccountResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.record.limitsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound3 = normalizeLedgerAccountRequest {
                                this.context = request.context
                                this.ledgerAccount = ledgerAccount {
                                    this.limits.addAll(listOf(ledgerAccountLimit {
                                        this.amount = item.amount
                                        this.period = item.period
                                    }))
                                }
                            }
                            ledgerLedgerAccountValidationService.normalizeLedgerAccount(outbound3)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply3 = it }
                }
            }
            importFuelCardsResponse {
                this.rejections.addAll(listOf(ImportFuelCardsResponseKt.rejection {
                    this.fuelCardId = reply2.fuelCard.fuelCardId
                }))
                this.result = operationResult {
                    this.correlationId = reply0.fuelTransaction.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply2.result.message
                    this.status = reply1.outcome.status
                }
            }
        }

    private suspend fun handleMapFuelCardImportColumns(request: MapFuelCardImportColumnsRequest): MapFuelCardImportColumnsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardImportService/MapFuelCardImportColumns") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "MapFuelCardImportColumns", request)
            log.atInfo().addKeyValue("operation", "MapFuelCardImportColumns").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCard.holder.driverId).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                listFuelCardExportsRequest {
                    this.context = request.context
                    this.filter = ListFuelCardExportsRequestKt.filter {
                        this.createdAfter = request.fuelCard.audit.updatedAt
                        this.labels.putAll(request.fuelCard.labelsMap)
                        this.ownerId = request.fuelCard.fuelCardId
                    }
                }
            }
            val reply0 = fuelFuelCardExportService.listFuelCardExports(outbound0)
            val outbound1 = previewNotificationRequest {
                this.context = request.context
                this.notification = notification {
                    this.assignee = userRef {
                        this.email = request.details.contact.email
                    }
                    this.audit = auditInfo {
                        this.updatedAt = request.fuelCard.audit.updatedAt
                    }
                    this.requester = contactInfo {
                        this.name = request.details.contact.name
                    }
                }
            }
            val reply1 = ClientRegistry.lookup("acme.notifications.v1.NotificationValidationService", NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub::class.java).previewNotification(outbound1)
            mapFuelCardImportColumnsResponse {
                this.fuelCard = fuelCard {
                    this.available = request.fuelCard.available
                    this.contact = contactInfo {
                        this.email = reply1.notification.assignee.email
                    }
                    this.fuelCardId = request.fuelCardId
                }
                this.outcome = MapFuelCardImportColumnsResponseKt.outcome {
                    this.message = reply0.result.message
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleRetryFuelCardImport(request: RetryFuelCardImportRequest): RetryFuelCardImportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardImportService/RetryFuelCardImport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RetryFuelCardImport", request)
            log.atInfo().addKeyValue("operation", "RetryFuelCardImport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCard.fuelCardId).log("Recorded audit detail")
            val outbound0 = getFuelCardExportRequest {
                this.context = request.context
                this.fuelCardId = request.fuelCardId
            }
            val reply0 = fuelFuelCardExportService.getFuelCardExport(WireCopies.reflective(outbound0))
            val outbound1 = browseFuelTransactionsRequest {
                this.context = request.context
                this.filter = BrowseFuelTransactionsRequestKt.filter {
                    this.labels.putAll(request.fuelCard.labelsMap)
                }
            }
            val reply1 = workerForRetryFuelCardImport1.submit(outbound1)
            val outbound2 = explainCustomerSegmentCalculationRequest {
                this.context = request.context
                this.details = ExplainCustomerSegmentCalculationRequestKt.details {
                    this.contact = contactInfo {
                        this.name = request.fuelCard.contact.name
                    }
                }
            }
            val reply2 = clientForRetryFuelCardImport2.explainCustomerSegmentCalculation(outbound2)
            retryFuelCardImportResponse {
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.etag = reply2.customerSegment.audit.etag
                    }
                    this.balance = request.fuelCard.balance
                    this.fuelCardId = reply0.fuelCard.fuelCardId
                    this.status = request.fuelCard.status
                }
                this.outcome = RetryFuelCardImportResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }
}
