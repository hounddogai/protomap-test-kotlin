package com.acme.transport.fuel.api

import com.acme.audit.v1.AuditEventQueryServiceGrpcKt
import com.acme.audit.v1.SuggestAuditEventsRequestKt
import com.acme.audit.v1.suggestAuditEventsRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.ConsentRecordCalculationServiceGrpcKt
import com.acme.customers.v1.ConsentRecordValidationServiceGrpcKt
import com.acme.customers.v1.QuoteConsentRecordRequest
import com.acme.customers.v1.QuoteConsentRecordRequestKt
import com.acme.customers.v1.QuoteConsentRecordResponse
import com.acme.customers.v1.consentRecord
import com.acme.customers.v1.consentRecordMeasurement
import com.acme.customers.v1.householdRef
import com.acme.customers.v1.quoteConsentRecordRequest
import com.acme.customers.v1.scoreConsentRecordRequest
import com.acme.deliveries.v1.ApproveDeliveryRequestKt
import com.acme.deliveries.v1.DeliveryApprovalServiceGrpcKt
import com.acme.deliveries.v1.DeliveryExceptionApprovalServiceGrpcKt
import com.acme.deliveries.v1.SubmitDeliveryExceptionRequest
import com.acme.deliveries.v1.SubmitDeliveryRequestKt
import com.acme.deliveries.v1.approveDeliveryRequest
import com.acme.deliveries.v1.delivery
import com.acme.deliveries.v1.deliveryBankSettlement
import com.acme.deliveries.v1.deliveryCardSettlement
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.deliveryExceptionRef
import com.acme.deliveries.v1.submitDeliveryExceptionRequest
import com.acme.deliveries.v1.submitDeliveryRequest
import com.acme.drivers.v1.BrowseDriverLicensesRequest
import com.acme.drivers.v1.BrowseDriverLicensesRequestKt
import com.acme.drivers.v1.DriverLicenseQueryServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseValidationServiceGrpcKt
import com.acme.drivers.v1.PreviewDriverLicenseRequest
import com.acme.drivers.v1.PreviewDriverLicenseResponse
import com.acme.drivers.v1.browseDriverLicensesRequest
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.previewDriverLicenseRequest
import com.acme.fuel.v1.BrowseFuelTransactionsRequestKt
import com.acme.fuel.v1.CancelFuelPolicyExportRequest
import com.acme.fuel.v1.CancelFuelPolicyExportResponse
import com.acme.fuel.v1.CancelFuelPolicyExportResponseKt
import com.acme.fuel.v1.CompareFuelPolicyVersionsRequest
import com.acme.fuel.v1.CompareFuelPolicyVersionsResponse
import com.acme.fuel.v1.FuelCard
import com.acme.fuel.v1.FuelCardExportServiceGrpcKt
import com.acme.fuel.v1.FuelCardImportServiceGrpcKt
import com.acme.fuel.v1.FuelCardValidationServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyExportServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyHistoryServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyValidationServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionExportServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionQueryServiceGrpcKt
import com.acme.fuel.v1.GetFuelPolicyExportRequest
import com.acme.fuel.v1.GetFuelPolicyExportResponse
import com.acme.fuel.v1.ListFuelCardExportsRequest
import com.acme.fuel.v1.ListFuelCardExportsRequestKt
import com.acme.fuel.v1.ListFuelCardExportsResponse
import com.acme.fuel.v1.ListFuelPolicyExportsRequest
import com.acme.fuel.v1.ListFuelPolicyExportsResponse
import com.acme.fuel.v1.ListFuelTransactionExportsRequest
import com.acme.fuel.v1.ListFuelTransactionExportsRequestKt
import com.acme.fuel.v1.RetryFuelCardImportRequest
import com.acme.fuel.v1.RetryFuelCardImportRequestKt
import com.acme.fuel.v1.RetryFuelPolicyExportRequest
import com.acme.fuel.v1.RetryFuelPolicyExportResponse
import com.acme.fuel.v1.ScheduleFuelPolicyExportRequest
import com.acme.fuel.v1.ScheduleFuelPolicyExportResponse
import com.acme.fuel.v1.ScoreFuelCardRequest
import com.acme.fuel.v1.ShareFuelPolicyExportRequest
import com.acme.fuel.v1.ShareFuelPolicyExportResponse
import com.acme.fuel.v1.ShareFuelPolicyExportResponseKt
import com.acme.fuel.v1.StreamFuelPolicyExportRequest
import com.acme.fuel.v1.StreamFuelPolicyExportResponse
import com.acme.fuel.v1.SummarizeFuelTransactionsRequestKt
import com.acme.fuel.v1.browseFuelTransactionsRequest
import com.acme.fuel.v1.cancelFuelPolicyExportResponse
import com.acme.fuel.v1.compareFuelPolicyVersionsRequest
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.getFuelPolicyExportResponse
import com.acme.fuel.v1.getFuelPolicyRulesRequest
import com.acme.fuel.v1.listFuelCardExportsRequest
import com.acme.fuel.v1.listFuelPolicyExportsResponse
import com.acme.fuel.v1.listFuelTransactionExportsRequest
import com.acme.fuel.v1.retryFuelCardImportRequest
import com.acme.fuel.v1.retryFuelPolicyExportResponse
import com.acme.fuel.v1.scheduleFuelPolicyExportResponse
import com.acme.fuel.v1.scoreFuelCardRequest
import com.acme.fuel.v1.shareFuelPolicyExportResponse
import com.acme.fuel.v1.streamFuelPolicyExportResponse
import com.acme.fuel.v1.summarizeFuelTransactionsRequest
import com.acme.identity.v1.userRef
import com.acme.transport.fuel.FuelRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.google.protobuf.Any as ProtoAny
import com.google.protobuf.Timestamp
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Exports fuel policies to files and other systems. */
class FuelPolicyExportGrpcService(
    private val store: FuelRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventQueryService: AuditEventQueryServiceGrpcKt.AuditEventQueryServiceCoroutineStub,
    private val customersConsentRecordCalculationService: ConsentRecordCalculationServiceGrpcKt.ConsentRecordCalculationServiceCoroutineStub,
    private val customersConsentRecordValidationService: ConsentRecordValidationServiceGrpcKt.ConsentRecordValidationServiceCoroutineStub,
    private val deliveriesDeliveryApprovalService: DeliveryApprovalServiceGrpcKt.DeliveryApprovalServiceCoroutineStub,
    private val deliveriesDeliveryExceptionApprovalService: DeliveryExceptionApprovalServiceGrpcKt.DeliveryExceptionApprovalServiceCoroutineStub,
    private val driversDriverLicenseQueryService: DriverLicenseQueryServiceGrpcKt.DriverLicenseQueryServiceCoroutineStub,
    private val driversDriverLicenseValidationService: DriverLicenseValidationServiceGrpcKt.DriverLicenseValidationServiceCoroutineStub,
    private val fuelFuelCardExportService: FuelCardExportServiceGrpcKt.FuelCardExportServiceCoroutineStub,
    private val fuelFuelCardImportService: FuelCardImportServiceGrpcKt.FuelCardImportServiceCoroutineStub,
    private val fuelFuelCardValidationService: FuelCardValidationServiceGrpcKt.FuelCardValidationServiceCoroutineStub,
    private val fuelFuelPolicyHistoryService: FuelPolicyHistoryServiceGrpcKt.FuelPolicyHistoryServiceCoroutineStub,
    private val fuelFuelPolicyValidationService: FuelPolicyValidationServiceGrpcKt.FuelPolicyValidationServiceCoroutineStub,
    private val fuelFuelTransactionExportService: FuelTransactionExportServiceGrpcKt.FuelTransactionExportServiceCoroutineStub,
    private val fuelFuelTransactionQueryService: FuelTransactionQueryServiceGrpcKt.FuelTransactionQueryServiceCoroutineStub,
) : FuelPolicyExportServiceGrpcKt.FuelPolicyExportServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(FuelPolicyExportGrpcService::class.java)

    private val invokeScheduleFuelPolicyExport2: suspend (ListFuelCardExportsRequest) -> ListFuelCardExportsResponse = { payload ->
        fuelFuelCardExportService.listFuelCardExports(payload)
    }

    private val workerForStreamFuelPolicyExport1 = ReplyMailbox<QuoteConsentRecordRequest, QuoteConsentRecordResponse>(workers) { payload ->
        withTimeout(5_000) { customersConsentRecordCalculationService.quoteConsentRecord(payload) }
    }

    private val clientForStreamFuelPolicyExport2 by lazy { fuelFuelTransactionQueryService }

    /** Cancels a fuel policy. */
    override suspend fun cancelFuelPolicyExport(request: CancelFuelPolicyExportRequest): CancelFuelPolicyExportResponse {
        return handleCancelFuelPolicyExport(request)
    }

    /** Returns a fuel policy. */
    override suspend fun getFuelPolicyExport(request: GetFuelPolicyExportRequest): GetFuelPolicyExportResponse = handleGetFuelPolicyExport(request)

    /** Lists fuel policies a page at a time. */
    override suspend fun listFuelPolicyExports(request: ListFuelPolicyExportsRequest): ListFuelPolicyExportsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyExportService/ListFuelPolicyExports") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListFuelPolicyExports", request)
            log.atInfo().addKeyValue("operation", "ListFuelPolicyExports").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            listFuelPolicyExportsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Retrys a fuel policy. */
    override suspend fun retryFuelPolicyExport(request: RetryFuelPolicyExportRequest): RetryFuelPolicyExportResponse {
        return handleRetryFuelPolicyExport(request)
    }

    /** Schedules a fuel policy. */
    override suspend fun scheduleFuelPolicyExport(request: ScheduleFuelPolicyExportRequest): ScheduleFuelPolicyExportResponse = handleScheduleFuelPolicyExport(request)

    /** Shares a fuel policy. */
    override suspend fun shareFuelPolicyExport(request: ShareFuelPolicyExportRequest): ShareFuelPolicyExportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyExportService/ShareFuelPolicyExport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ShareFuelPolicyExport", request)
            log.atInfo().addKeyValue("operation", "ShareFuelPolicyExport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.recipient.phone).log("Recorded audit detail")
            val outbound0 = RetryFuelCardImportRequest.newBuilder().apply {
                setContext(request.context)
                setDetails(RetryFuelCardImportRequestKt.details {
                    this.notify = contactInfo {
                        this.name = request.details.recipient.name
                        this.preferredChannel = request.details.recipient.preferredChannel
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> fuelFuelCardImportService.retryFuelCardImport(value) }
            val outbound1 = RequestFactory.create {
                suggestAuditEventsRequest {
                    this.context = request.context
                    this.filter = SuggestAuditEventsRequestKt.filter {
                        this.createdAfter = reply0.fuelCard.audit.updatedAt
                        this.labels.putAll(reply0.fuelCard.labelsMap)
                    }
                }
            }
            val reply1 = auditAuditEventQueryService.suggestAuditEvents(outbound1)
            val outbound2 = submitDeliveryRequest {
                this.context = request.context
                this.delivery = delivery {
                    this.audit = auditInfo {
                        this.etag = reply0.fuelCard.audit.etag
                    }
                    this.bank = deliveryBankSettlement {
                        this.accountHolder = reply0.fuelCard.bank.accountHolder
                        this.bic = reply0.fuelCard.bank.bic
                    }
                }
                this.details = SubmitDeliveryRequestKt.details {
                    this.approver = userRef {
                        this.email = reply0.fuelCard.holder.email
                    }
                }
            }
            val reply2 = ClientRegistry.lookup("acme.deliveries.v1.DeliveryApprovalService", DeliveryApprovalServiceGrpcKt.DeliveryApprovalServiceCoroutineStub::class.java).submitDelivery(outbound2)
            shareFuelPolicyExportResponse {
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.createdAt = reply0.fuelCard.audit.createdAt
                    }
                    this.fuelPolicyId = request.fuelPolicyId
                    this.owner = userRef {
                        this.email = request.details.recipient.email
                    }
                }
                this.outcome = ShareFuelPolicyExportResponseKt.outcome {
                    this.status = reply0.outcome.status
                }
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.status = "ok"
                }
            }
        }

    /** Streams every matching fuel policy. */
    override fun streamFuelPolicyExport(request: StreamFuelPolicyExportRequest): Flow<StreamFuelPolicyExportResponse> = flow {
        emit(handleStreamFuelPolicyExport(request))
    }

    private fun CancelFuelPolicyExportRequest.toScoreFuelCardFromCancelFuelPolicyExport0(): ScoreFuelCardRequest {
        val source = this
        return scoreFuelCardRequest {
            this.context = source.context
            this.fuelCard = fuelCard {
                this.contact = contactInfo {
                    this.email = source.details.notifyContact.email
                }
                this.holder = driverRef {
                    this.displayName = source.details.newOwner.displayName
                    this.email = source.details.newOwner.email
                }
            }
        }
    }

    private suspend fun handleCancelFuelPolicyExport(request: CancelFuelPolicyExportRequest): CancelFuelPolicyExportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyExportService/CancelFuelPolicyExport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CancelFuelPolicyExport", request)
            log.atInfo().addKeyValue("operation", "CancelFuelPolicyExport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = request.toScoreFuelCardFromCancelFuelPolicyExport0()
            val reply0 = fuelFuelCardValidationService.scoreFuelCard(outbound0)
            val outbound1 = approveDeliveryRequest {
                this.context = request.context
                this.delivery = delivery {
                    this.card = deliveryCardSettlement {
                        this.network = when (reply0.fuelCard.fundingCase) {
                            FuelCard.FundingCase.CARD -> reply0.fuelCard.card.network
                            else -> ""
                        }
                    }
                    this.counterparty = deliveryExceptionRef {
                        this.displayName = request.details.newOwner.displayName
                    }
                }
                this.details = ApproveDeliveryRequestKt.details {
                    this.approver = userRef {
                        this.displayName = reply0.fuelCard.holder.displayName
                        this.email = reply0.fuelCard.contact.email
                    }
                }
            }
            val reply1 = deliveriesDeliveryApprovalService.approveDelivery(outbound1)
            cancelFuelPolicyExportResponse {
                this.fuelPolicy = fuelPolicy {
                    this.fuelPolicyId = request.fuelPolicyId
                    this.owner = userRef {
                        this.email = reply0.fuelCard.holder.email
                    }
                }
                this.outcome = CancelFuelPolicyExportResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = reply1.outcome.status
                }
            }
        }

    private suspend fun handleGetFuelPolicyExport(request: GetFuelPolicyExportRequest): GetFuelPolicyExportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyExportService/GetFuelPolicyExport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetFuelPolicyExport", request)
            log.atInfo().addKeyValue("operation", "GetFuelPolicyExport").log("Handling transport request")
            val outbound0 = submitDeliveryExceptionRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.assignee = userRef {
                        this.userId = request.context.actor.userId
                    }
                    this.requester = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = deliveriesDeliveryExceptionApprovalService.submitDeliveryException(envelope0.unpack(SubmitDeliveryExceptionRequest::class.java))
            val outbound1 = getFuelPolicyRulesRequest {
                this.context = request.context
                this.fuelPolicyId = request.fuelPolicyId
            }
            val reply1 = fuelFuelPolicyValidationService.getFuelPolicyRules(WireCopies.json(outbound1))
            getFuelPolicyExportResponse {
                this.fuelPolicy = fuelPolicy {
                    this.fuelPolicyId = request.fuelPolicyId
                    this.name = reply1.fuelPolicy.name
                    this.owner = userRef {
                        this.displayName = reply0.deliveryException.customer.displayName
                    }
                    this.parameters.putAll(reply1.fuelPolicy.parametersMap)
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private data class RetryFuelPolicyExport0Command(
        val context: RequestContext,
        val filterCreatedAfter: Timestamp,
        val filterOwnerId: String,
        val filterLabels: Map<String, String>
    ) {
        fun toRequest(): BrowseDriverLicensesRequest {
            val command = this
            return browseDriverLicensesRequest {
                this.context = command.context
                this.filter = BrowseDriverLicensesRequestKt.filter {
                    this.createdAfter = command.filterCreatedAfter
                    this.labels.putAll(command.filterLabels)
                    this.ownerId = command.filterOwnerId
                }
            }
        }
    }

    private suspend fun handleRetryFuelPolicyExport(request: RetryFuelPolicyExportRequest): RetryFuelPolicyExportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyExportService/RetryFuelPolicyExport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RetryFuelPolicyExport", request)
            log.atInfo().addKeyValue("operation", "RetryFuelPolicyExport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicy.fuelPolicyId).log("Recorded audit detail")
            val outbound0 = RetryFuelPolicyExport0Command(
                context = request.context,
                filterCreatedAfter = request.fuelPolicy.audit.createdAt,
                filterOwnerId = request.fuelPolicy.owner.userId,
                filterLabels = request.details.parametersMap
            ).toRequest()
            val reply0 = driversDriverLicenseQueryService.browseDriverLicenses(outbound0)
            retryFuelPolicyExportResponse {
                this.fuelPolicy = fuelPolicy {
                    this.fuelPolicyId = request.fuelPolicyId
                    this.parameters.putAll(request.fuelPolicy.parametersMap)
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleScheduleFuelPolicyExport(request: ScheduleFuelPolicyExportRequest): ScheduleFuelPolicyExportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyExportService/ScheduleFuelPolicyExport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScheduleFuelPolicyExport", request)
            log.atInfo().addKeyValue("operation", "ScheduleFuelPolicyExport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.format).log("Recorded audit detail")
            val outbound0 = listFuelTransactionExportsRequest {
                this.context = request.context
                this.filter = ListFuelTransactionExportsRequestKt.filter {
                    this.createdAfter = request.context.deadline
                    this.labels.putAll(request.details.filtersMap)
                    this.ownerId = request.fuelPolicyId
                }
            }
            val command0: Submission<ListFuelTransactionExportsRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = fuelFuelTransactionExportService.listFuelTransactionExports(payload0)
            val outbound1 = previewDriverLicenseRequest {
                this.context = request.context
                this.driverLicense = driverLicense {
                    this.counterparty = driverRef {
                        this.email = request.context.actor.email
                    }
                }
                this.reason = request.reason
            }
            val adapter1 = OutboundPort<PreviewDriverLicenseRequest, PreviewDriverLicenseResponse> { payload ->
                driversDriverLicenseValidationService.previewDriverLicense(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            val outbound2 = listFuelCardExportsRequest {
                this.context = request.context
                this.filter = ListFuelCardExportsRequestKt.filter {
                    this.createdAfter = reply1.driverLicense.audit.createdAt
                    this.region = reply1.driverLicense.billingAddress.region
                }
            }
            val reply2 = invokeScheduleFuelPolicyExport2(outbound2)
            val outbound3 = compareFuelPolicyVersionsRequest {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.owner = userRef {
                        this.displayName = reply1.driverLicense.counterparty.displayName
                    }
                }
            }
            val invoke3: suspend (CompareFuelPolicyVersionsRequest, Metadata) -> CompareFuelPolicyVersionsResponse = fuelFuelPolicyHistoryService::compareFuelPolicyVersions
            val reply3 = invoke3(outbound3, Metadata())
            scheduleFuelPolicyExportResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.referenceIds.addAll((reply0.result.referenceIdsList) + (reply3.result.referenceIdsList))
                    this.status = reply1.result.status
                }
            }
        }

    private suspend fun handleStreamFuelPolicyExport(request: StreamFuelPolicyExportRequest): StreamFuelPolicyExportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyExportService/StreamFuelPolicyExport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StreamFuelPolicyExport", request)
            log.atInfo().addKeyValue("operation", "StreamFuelPolicyExport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = summarizeFuelTransactionsRequest {
                this.annotations.putAll(request.context.baggageMap)
                this.context = request.context
                this.details = SummarizeFuelTransactionsRequestKt.details {
                    this.filters.putAll(request.filter.labelsMap)
                    this.format = request.format
                }
            }
            val reply0 = fuelFuelTransactionQueryService.summarizeFuelTransactions(WireCopies.reflective(outbound0))
            val outbound1 = quoteConsentRecordRequest {
                this.consentRecord = consentRecord {
                    this.correlationId = reply0.result.correlationId
                    this.measurements.addAll(listOf(consentRecordMeasurement {
                        this.observedAt = request.filter.createdAfter
                    }))
                }
                this.context = request.context
                this.details = QuoteConsentRecordRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.filter.region
                    }
                }
            }
            val reply1 = workerForStreamFuelPolicyExport1.submit(outbound1)
            val outbound2 = browseFuelTransactionsRequest {
                this.context = request.context
                this.filter = BrowseFuelTransactionsRequestKt.filter {
                    this.region = request.filter.region
                }
            }
            val reply2 = clientForStreamFuelPolicyExport2.browseFuelTransactions(outbound2)
            val outbound3 = scoreConsentRecordRequest {
                this.consentRecord = consentRecord {
                    this.consentRecordId = reply1.consentRecord.consentRecordId
                    this.correlationId = reply0.result.correlationId
                    this.subject = householdRef {
                        this.householdId = reply1.consentRecord.subject.householdId
                        this.path = reply1.consentRecord.subject.path
                    }
                }
                this.context = request.context
            }
            val reply3 = flowOf(outbound3).map { value -> customersConsentRecordValidationService.scoreConsentRecord(value) }.single()
            streamFuelPolicyExportResponse {
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.message = reply0.result.message
                    this.status = "ok"
                }
                this.sequence = reply3.consentRecord.sequence
            }
        }
}
