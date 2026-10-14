package com.acme.transport.fuel.adapters

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.CalculateCustomerRequest
import com.acme.customers.v1.CustomerCalculationServiceGrpcKt
import com.acme.customers.v1.CustomerServiceGrpcKt
import com.acme.customers.v1.CustomerValidationServiceGrpcKt
import com.acme.customers.v1.GetCustomerRequest
import com.acme.customers.v1.HouseholdQueryServiceGrpcKt
import com.acme.customers.v1.QuoteCustomerRequestKt
import com.acme.customers.v1.calculateCustomerRequest
import com.acme.customers.v1.customer
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.getCustomerRequest
import com.acme.customers.v1.lintCustomersRequest
import com.acme.customers.v1.quoteCustomerRequest
import com.acme.customers.v1.summarizeHouseholdsRequest
import com.acme.deliveries.v1.ApproveDeliveryExceptionRequest
import com.acme.deliveries.v1.ApproveDeliveryExceptionRequestKt
import com.acme.deliveries.v1.ApproveDeliveryExceptionResponse
import com.acme.deliveries.v1.DeliveryExceptionApprovalServiceGrpcKt
import com.acme.deliveries.v1.approveDeliveryExceptionRequest
import com.acme.deliveries.v1.deliveryException
import com.acme.drivers.v1.BrowseDriverLicensesRequest
import com.acme.drivers.v1.BrowseDriverLicensesRequestKt
import com.acme.drivers.v1.DriverLicenseQueryServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseValidationServiceGrpcKt
import com.acme.drivers.v1.LintDriverLicensesRequest
import com.acme.drivers.v1.LintDriverLicensesRequestKt
import com.acme.drivers.v1.LintDriverLicensesResponse
import com.acme.drivers.v1.browseDriverLicensesRequest
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.lintDriverLicensesRequest
import com.acme.fuel.v1.CancelFuelTransactionExportRequest
import com.acme.fuel.v1.CancelFuelTransactionExportResponse
import com.acme.fuel.v1.CancelFuelTransactionExportResponseKt
import com.acme.fuel.v1.CompareFuelPolicyOptionsRequest
import com.acme.fuel.v1.FuelCardExportServiceGrpcKt
import com.acme.fuel.v1.FuelCardServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyCalculationServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionCalculationServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionExportServiceGrpcKt
import com.acme.fuel.v1.GetFuelTransactionExportRequest
import com.acme.fuel.v1.GetFuelTransactionExportResponse
import com.acme.fuel.v1.GetFuelTransactionRatesRequest
import com.acme.fuel.v1.GetFuelTransactionRatesResponse
import com.acme.fuel.v1.ListFuelTransactionExportsRequest
import com.acme.fuel.v1.ListFuelTransactionExportsResponse
import com.acme.fuel.v1.RetryFuelTransactionExportRequest
import com.acme.fuel.v1.RetryFuelTransactionExportResponse
import com.acme.fuel.v1.RetryFuelTransactionExportResponseKt
import com.acme.fuel.v1.ScheduleFuelTransactionExportRequest
import com.acme.fuel.v1.ScheduleFuelTransactionExportResponse
import com.acme.fuel.v1.ShareFuelCardExportRequestKt
import com.acme.fuel.v1.ShareFuelTransactionExportRequest
import com.acme.fuel.v1.ShareFuelTransactionExportResponse
import com.acme.fuel.v1.StreamFuelTransactionExportRequest
import com.acme.fuel.v1.StreamFuelTransactionExportResponse
import com.acme.fuel.v1.cancelFuelTransactionExportResponse
import com.acme.fuel.v1.compareFuelPolicyOptionsRequest
import com.acme.fuel.v1.createFuelCardRequest
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.fuelPolicyBlockAction
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.getFuelTransactionExportResponse
import com.acme.fuel.v1.getFuelTransactionRatesRequest
import com.acme.fuel.v1.listFuelTransactionExportsResponse
import com.acme.fuel.v1.retryFuelTransactionExportResponse
import com.acme.fuel.v1.scheduleFuelTransactionExportResponse
import com.acme.fuel.v1.shareFuelCardExportRequest
import com.acme.fuel.v1.shareFuelTransactionExportResponse
import com.acme.fuel.v1.streamFuelCardExportRequest
import com.acme.fuel.v1.streamFuelTransactionExportResponse
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
import com.acme.yards.v1.DockWorkflowServiceGrpcKt
import com.acme.yards.v1.dock
import com.acme.yards.v1.startDockWorkflowRequest
import com.google.protobuf.Any as ProtoAny
import com.google.protobuf.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Exports fuel transactions to files and other systems. */
class FuelTransactionExportController(
    private val store: FuelRepository,
    private val workers: CoroutineScope,
    private val customersCustomerCalculationService: CustomerCalculationServiceGrpcKt.CustomerCalculationServiceCoroutineStub,
    private val customersCustomerService: CustomerServiceGrpcKt.CustomerServiceCoroutineStub,
    private val customersCustomerValidationService: CustomerValidationServiceGrpcKt.CustomerValidationServiceCoroutineStub,
    private val customersHouseholdQueryService: HouseholdQueryServiceGrpcKt.HouseholdQueryServiceCoroutineStub,
    private val deliveriesDeliveryExceptionApprovalService: DeliveryExceptionApprovalServiceGrpcKt.DeliveryExceptionApprovalServiceCoroutineStub,
    private val driversDriverLicenseQueryService: DriverLicenseQueryServiceGrpcKt.DriverLicenseQueryServiceCoroutineStub,
    private val driversDriverLicenseValidationService: DriverLicenseValidationServiceGrpcKt.DriverLicenseValidationServiceCoroutineStub,
    private val fuelFuelCardExportService: FuelCardExportServiceGrpcKt.FuelCardExportServiceCoroutineStub,
    private val fuelFuelCardService: FuelCardServiceGrpcKt.FuelCardServiceCoroutineStub,
    private val fuelFuelPolicyCalculationService: FuelPolicyCalculationServiceGrpcKt.FuelPolicyCalculationServiceCoroutineStub,
    private val fuelFuelTransactionCalculationService: FuelTransactionCalculationServiceGrpcKt.FuelTransactionCalculationServiceCoroutineStub,
    private val yardsDockWorkflowService: DockWorkflowServiceGrpcKt.DockWorkflowServiceCoroutineStub,
) : FuelTransactionExportServiceGrpcKt.FuelTransactionExportServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(FuelTransactionExportController::class.java)

    private val invokeRetryFuelTransactionExport3: suspend (LintDriverLicensesRequest) -> LintDriverLicensesResponse = { payload ->
        driversDriverLicenseValidationService.lintDriverLicenses(payload)
    }

    private val workerForShareFuelTransactionExport3 = ReplyMailbox<ApproveDeliveryExceptionRequest, ApproveDeliveryExceptionResponse>(workers) { payload ->
        withTimeout(5_000) { deliveriesDeliveryExceptionApprovalService.approveDeliveryException(payload) }
    }

    private val clientForStreamFuelTransactionExport0 by lazy { customersCustomerCalculationService }

    /** Cancels a fuel transaction. */
    override suspend fun cancelFuelTransactionExport(request: CancelFuelTransactionExportRequest): CancelFuelTransactionExportResponse = handleCancelFuelTransactionExport(request)

    /** Returns a fuel transaction. */
    override suspend fun getFuelTransactionExport(request: GetFuelTransactionExportRequest): GetFuelTransactionExportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionExportService/GetFuelTransactionExport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetFuelTransactionExport", request)
            log.atInfo().addKeyValue("operation", "GetFuelTransactionExport").log("Handling transport request")
            val outbound0 = calculateCustomerRequest {
                this.context = request.context
                this.customer = customer {
                    this.audit = auditInfo {
                        this.updatedAt = request.context.deadline
                    }
                    this.relationshipManager = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = customersCustomerCalculationService.calculateCustomer(envelope0.unpack(CalculateCustomerRequest::class.java))
            getFuelTransactionExportResponse {
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                    this.subject = customerRef {
                        this.displayName = reply0.customer.relationshipManager.displayName
                    }
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Lists fuel transactions a page at a time. */
    override suspend fun listFuelTransactionExports(request: ListFuelTransactionExportsRequest): ListFuelTransactionExportsResponse {
        return handleListFuelTransactionExports(request)
    }

    /** Retrys a fuel transaction. */
    override suspend fun retryFuelTransactionExport(request: RetryFuelTransactionExportRequest): RetryFuelTransactionExportResponse = handleRetryFuelTransactionExport(request)

    /** Schedules a fuel transaction. */
    override suspend fun scheduleFuelTransactionExport(request: ScheduleFuelTransactionExportRequest): ScheduleFuelTransactionExportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionExportService/ScheduleFuelTransactionExport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScheduleFuelTransactionExport", request)
            log.atInfo().addKeyValue("operation", "ScheduleFuelTransactionExport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransactionId).log("Recorded audit detail")
            val outbound0 = streamFuelCardExportRequest {
                this.context = request.context
                this.format = request.details.format
            }
            fuelFuelCardExportService.streamFuelCardExport(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = CompareFuelPolicyOptionsRequest.newBuilder().apply {
                putAllAnnotations(request.annotationsMap)
                setContext(request.context)
                setFuelPolicy(fuelPolicy {
                    this.block = fuelPolicyBlockAction {
                        this.reason = request.reason
                    }
                    this.owner = userRef {
                        this.userId = request.context.actor.userId
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> fuelFuelPolicyCalculationService.compareFuelPolicyOptions(value) }
            scheduleFuelTransactionExportResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    /** Shares a fuel transaction. */
    override suspend fun shareFuelTransactionExport(request: ShareFuelTransactionExportRequest): ShareFuelTransactionExportResponse {
        return handleShareFuelTransactionExport(request)
    }

    /** Streams every matching fuel transaction. */
    override fun streamFuelTransactionExport(request: StreamFuelTransactionExportRequest): Flow<StreamFuelTransactionExportResponse> = flow {
        emit(handleStreamFuelTransactionExport(request))
    }

    private fun CancelFuelTransactionExportRequest.toGetFuelTransactionRatesFromCancelFuelTransactionExport0(): GetFuelTransactionRatesRequest {
        val source = this
        return getFuelTransactionRatesRequest {
            this.context = source.context
            this.fuelTransactionId = source.fuelTransactionId
        }
    }

    private suspend fun handleCancelFuelTransactionExport(request: CancelFuelTransactionExportRequest): CancelFuelTransactionExportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionExportService/CancelFuelTransactionExport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CancelFuelTransactionExport", request)
            log.atInfo().addKeyValue("operation", "CancelFuelTransactionExport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = request.toGetFuelTransactionRatesFromCancelFuelTransactionExport0()
            val reply0 = fuelFuelTransactionCalculationService.getFuelTransactionRates(outbound0)
            val outbound1 = summarizeHouseholdsRequest {
                this.context = request.context
                this.reason = request.reason
            }
            val reply1 = customersHouseholdQueryService.summarizeHouseholds(outbound1)
            cancelFuelTransactionExportResponse {
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                }
                this.outcome = CancelFuelTransactionExportResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.message = reply0.result.message
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleListFuelTransactionExports(request: ListFuelTransactionExportsRequest): ListFuelTransactionExportsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionExportService/ListFuelTransactionExports") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListFuelTransactionExports", request)
            log.atInfo().addKeyValue("operation", "ListFuelTransactionExports").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = startDockWorkflowRequest {
                this.context = request.context
                this.dock = dock {
                    this.address = postalAddress {
                        this.region = request.filter.region
                    }
                    this.owner = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply0 = yardsDockWorkflowService.startDockWorkflow(WireCopies.json(outbound0))
            listFuelTransactionExportsResponse {
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }

    private data class RetryFuelTransactionExport0Command(
        val context: RequestContext,
        val filterOwnerId: String,
        val filterCreatedAfter: Timestamp,
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

    private suspend fun handleRetryFuelTransactionExport(request: RetryFuelTransactionExportRequest): RetryFuelTransactionExportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionExportService/RetryFuelTransactionExport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RetryFuelTransactionExport", request)
            log.atInfo().addKeyValue("operation", "RetryFuelTransactionExport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransaction.fuelTransactionId).log("Recorded audit detail")
            val outbound0 = RetryFuelTransactionExport0Command(
                context = request.context,
                filterOwnerId = request.fuelTransaction.subject.customerId,
                filterCreatedAfter = request.fuelTransaction.occurredAt,
                filterLabels = request.annotationsMap
            ).toRequest()
            val reply0 = driversDriverLicenseQueryService.browseDriverLicenses(outbound0)
            val outbound1 = getCustomerRequest {
                this.context = request.context
                this.customerId = request.fuelTransaction.subject.customerId
            }
            val command1: Submission<GetCustomerRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = customersCustomerService.getCustomer(payload1)
            val outbound2 = getFuelTransactionRatesRequest {
                this.context = request.context
                this.fuelTransactionId = request.fuelTransactionId
            }
            val adapter2 = OutboundPort<GetFuelTransactionRatesRequest, GetFuelTransactionRatesResponse> { payload ->
                fuelFuelTransactionCalculationService.getFuelTransactionRates(payload)
            }
            val port2 = DelegatingPort(adapter2)
            val reply2 = port2.execute(outbound2)
            val outbound3 = lintDriverLicensesRequest {
                this.context = request.context
                this.details = LintDriverLicensesRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.fuelTransaction.subject.email
                        this.name = request.details.notify.name
                    }
                }
                this.driverLicense = driverLicense {
                    this.billingAddress = postalAddress {
                        this.lines.addAll(reply1.customer.primaryAddress.linesList)
                    }
                    this.counterparty = driverRef {
                        this.email = reply1.customer.contact.email
                    }
                }
            }
            val reply3 = invokeRetryFuelTransactionExport3(outbound3)
            retryFuelTransactionExportResponse {
                this.fuelTransaction = fuelTransaction {
                    this.attributes.putAll(reply1.customer.attributesMap)
                    this.correlationId = reply0.result.correlationId
                    this.fuelTransactionId = request.fuelTransactionId
                    this.occurredAt = request.fuelTransaction.occurredAt
                }
                this.outcome = RetryFuelTransactionExportResponseKt.outcome {
                    this.referenceId = reply3.outcome.referenceId
                }
                this.result = operationResult {
                    this.correlationId = reply3.result.correlationId
                    this.details.putAll(reply2.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleShareFuelTransactionExport(request: ShareFuelTransactionExportRequest): ShareFuelTransactionExportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionExportService/ShareFuelTransactionExport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ShareFuelTransactionExport", request)
            log.atInfo().addKeyValue("operation", "ShareFuelTransactionExport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.recipient.phone).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                lintCustomersRequest {
                    this.context = request.context
                    this.customer = customer {
                        this.contact = contactInfo {
                            this.preferredChannel = request.details.recipient.preferredChannel
                        }
                    }
                }
            }
            val reply0 = customersCustomerValidationService.lintCustomers(outbound0)
            val outbound1 = createFuelCardRequest {
                this.context = request.context
                this.fuelCard = fuelCard {
                    this.contact = contactInfo {
                        this.name = request.details.recipient.name
                    }
                }
            }
            val reply1 = ClientRegistry.lookup("acme.fuel.v1.FuelCardService", FuelCardServiceGrpcKt.FuelCardServiceCoroutineStub::class.java).createFuelCard(outbound1)
            val outbound2 = shareFuelCardExportRequest {
                this.context = request.context
                this.details = ShareFuelCardExportRequestKt.details {
                    this.recipient = contactInfo {
                        this.email = request.details.recipient.email
                        this.name = reply1.fuelCard.contact.name
                        this.phone = reply1.fuelCard.contact.phone
                        this.preferredChannel = reply1.fuelCard.contact.preferredChannel
                    }
                }
            }
            val reply2 = fuelFuelCardExportService.shareFuelCardExport(WireCopies.reflective(outbound2))
            val outbound3 = approveDeliveryExceptionRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.audit = auditInfo {
                        this.etag = reply2.fuelCard.audit.etag
                        this.updatedBy = reply2.fuelCard.audit.updatedBy
                    }
                    this.requester = contactInfo {
                        this.phone = reply0.customer.contact.phone
                    }
                }
                this.details = ApproveDeliveryExceptionRequestKt.details {
                    this.approver = userRef {
                        this.displayName = reply1.fuelCard.holder.displayName
                    }
                }
            }
            val reply3 = workerForShareFuelTransactionExport3.submit(outbound3)
            shareFuelTransactionExportResponse {
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                    this.subject = customerRef {
                        this.customerId = reply0.customer.customerId
                        this.displayName = reply3.deliveryException.assignee.displayName
                        this.email = reply1.fuelCard.holder.email
                    }
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.message = reply2.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleStreamFuelTransactionExport(request: StreamFuelTransactionExportRequest): StreamFuelTransactionExportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionExportService/StreamFuelTransactionExport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StreamFuelTransactionExport", request)
            log.atInfo().addKeyValue("operation", "StreamFuelTransactionExport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = quoteCustomerRequest {
                this.context = request.context
                this.details = QuoteCustomerRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.filter.region
                    }
                }
            }
            val reply0 = clientForStreamFuelTransactionExport0.quoteCustomer(outbound0)
            streamFuelTransactionExportResponse {
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }
}
