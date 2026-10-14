package com.acme.transport.fuel.grpc

import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.ConsentRecordCalculationServiceGrpcKt
import com.acme.customers.v1.CustomerServiceGrpcKt
import com.acme.customers.v1.EstimateConsentRecordResponse
import com.acme.customers.v1.consentRecord
import com.acme.customers.v1.consentRecordMeasurement
import com.acme.customers.v1.customer
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.estimateConsentRecordRequest
import com.acme.customers.v1.updateCustomerRequest
import com.acme.deliveries.v1.DeliveryWorkflowServiceGrpcKt
import com.acme.deliveries.v1.SignalDeliveryWorkflowRequestKt
import com.acme.deliveries.v1.signalDeliveryWorkflowRequest
import com.acme.documents.v1.DocumentServiceGrpcKt
import com.acme.documents.v1.batchGetDocumentsRequest
import com.acme.drivers.v1.DriverLicenseValidationServiceGrpcKt
import com.acme.drivers.v1.ValidateDriverLicenseRequest
import com.acme.drivers.v1.ValidateDriverLicenseRequestKt
import com.acme.drivers.v1.ValidateDriverLicenseResponse
import com.acme.drivers.v1.validateDriverLicenseRequest
import com.acme.employees.v1.employeeRef
import com.acme.featureflags.v1.FeatureFlagQueryServiceGrpcKt
import com.acme.featureflags.v1.SuggestFeatureFlagsRequestKt
import com.acme.featureflags.v1.SuggestFeatureFlagsResponse
import com.acme.featureflags.v1.suggestFeatureFlagsRequest
import com.acme.fuel.v1.BrowseFuelTransactionsRequest
import com.acme.fuel.v1.BrowseFuelTransactionsRequestKt
import com.acme.fuel.v1.BrowseFuelTransactionsResponse
import com.acme.fuel.v1.CancelFuelTransactionImportRequest
import com.acme.fuel.v1.CancelFuelTransactionImportResponse
import com.acme.fuel.v1.CancelFuelTransactionImportResponseKt
import com.acme.fuel.v1.FuelCardExportServiceGrpcKt
import com.acme.fuel.v1.FuelCardImportServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyExportServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionImportServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionMeasurement
import com.acme.fuel.v1.FuelTransactionQueryServiceGrpcKt
import com.acme.fuel.v1.GetFuelTransactionImportStatusRequest
import com.acme.fuel.v1.GetFuelTransactionImportStatusResponse
import com.acme.fuel.v1.ImportFuelTransactionsRequest
import com.acme.fuel.v1.ImportFuelTransactionsResponse
import com.acme.fuel.v1.ListFuelPolicyExportsRequestKt
import com.acme.fuel.v1.ListFuelTransactionImportsRequest
import com.acme.fuel.v1.ListFuelTransactionImportsResponse
import com.acme.fuel.v1.MapFuelTransactionImportColumnsRequest
import com.acme.fuel.v1.MapFuelTransactionImportColumnsResponse
import com.acme.fuel.v1.MapFuelTransactionImportColumnsResponseKt
import com.acme.fuel.v1.RetryFuelCardExportResponse
import com.acme.fuel.v1.RetryFuelCardImportRequest
import com.acme.fuel.v1.RetryFuelTransactionImportRequest
import com.acme.fuel.v1.RetryFuelTransactionImportResponse
import com.acme.fuel.v1.RetryFuelTransactionImportResponseKt
import com.acme.fuel.v1.StreamFuelCardExportRequestKt
import com.acme.fuel.v1.StreamFuelPolicyExportRequestKt
import com.acme.fuel.v1.ValidateFuelTransactionImportRequest
import com.acme.fuel.v1.ValidateFuelTransactionImportResponse
import com.acme.fuel.v1.ValidateFuelTransactionImportResponseKt
import com.acme.fuel.v1.browseFuelTransactionsRequest
import com.acme.fuel.v1.cancelFuelTransactionImportResponse
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.getFuelTransactionImportStatusResponse
import com.acme.fuel.v1.importFuelTransactionsResponse
import com.acme.fuel.v1.listFuelPolicyExportsRequest
import com.acme.fuel.v1.listFuelTransactionImportsResponse
import com.acme.fuel.v1.mapFuelTransactionImportColumnsResponse
import com.acme.fuel.v1.retryFuelCardExportRequest
import com.acme.fuel.v1.retryFuelCardImportRequest
import com.acme.fuel.v1.retryFuelTransactionImportResponse
import com.acme.fuel.v1.streamFuelCardExportRequest
import com.acme.fuel.v1.streamFuelPolicyExportRequest
import com.acme.fuel.v1.validateFuelTransactionImportResponse
import com.acme.transport.fuel.FuelRepository
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.travel.v1.TripCalculationServiceGrpcKt
import com.acme.travel.v1.recalculateTripsRequest
import com.acme.travel.v1.trip
import com.acme.venues.v1.NormalizeVenueRequestKt
import com.acme.venues.v1.NormalizeVenueResponse
import com.acme.venues.v1.VenueValidationServiceGrpcKt
import com.acme.venues.v1.normalizeVenueRequest
import com.acme.venues.v1.venue
import com.acme.venues.v1.venueDimensions
import com.acme.yards.v1.DockApprovalServiceGrpcKt
import com.acme.yards.v1.SubmitDockResponse
import com.acme.yards.v1.dock
import com.acme.yards.v1.dockDimensions
import com.acme.yards.v1.submitDockRequest
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Takes fuel transactions in bulk from partners and legacy systems. */
class FuelTransactionImportEndpoint(
    private val store: FuelRepository,
    private val workers: CoroutineScope,
    private val customersConsentRecordCalculationService: ConsentRecordCalculationServiceGrpcKt.ConsentRecordCalculationServiceCoroutineStub,
    private val customersCustomerService: CustomerServiceGrpcKt.CustomerServiceCoroutineStub,
    private val deliveriesDeliveryWorkflowService: DeliveryWorkflowServiceGrpcKt.DeliveryWorkflowServiceCoroutineStub,
    private val documentsDocumentService: DocumentServiceGrpcKt.DocumentServiceCoroutineStub,
    private val driversDriverLicenseValidationService: DriverLicenseValidationServiceGrpcKt.DriverLicenseValidationServiceCoroutineStub,
    private val featureflagsFeatureFlagQueryService: FeatureFlagQueryServiceGrpcKt.FeatureFlagQueryServiceCoroutineStub,
    private val fuelFuelCardExportService: FuelCardExportServiceGrpcKt.FuelCardExportServiceCoroutineStub,
    private val fuelFuelCardImportService: FuelCardImportServiceGrpcKt.FuelCardImportServiceCoroutineStub,
    private val fuelFuelPolicyExportService: FuelPolicyExportServiceGrpcKt.FuelPolicyExportServiceCoroutineStub,
    private val fuelFuelTransactionQueryService: FuelTransactionQueryServiceGrpcKt.FuelTransactionQueryServiceCoroutineStub,
    private val travelTripCalculationService: TripCalculationServiceGrpcKt.TripCalculationServiceCoroutineStub,
    private val venuesVenueValidationService: VenueValidationServiceGrpcKt.VenueValidationServiceCoroutineStub,
    private val yardsDockApprovalService: DockApprovalServiceGrpcKt.DockApprovalServiceCoroutineStub,
) : FuelTransactionImportServiceGrpcKt.FuelTransactionImportServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(FuelTransactionImportEndpoint::class.java)

    private val invokeRetryFuelTransactionImport2: suspend (ValidateDriverLicenseRequest) -> ValidateDriverLicenseResponse = { payload ->
        driversDriverLicenseValidationService.validateDriverLicense(payload)
    }

    /** Cancels a fuel transaction. */
    override suspend fun cancelFuelTransactionImport(request: CancelFuelTransactionImportRequest): CancelFuelTransactionImportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionImportService/CancelFuelTransactionImport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CancelFuelTransactionImport", request)
            log.atInfo().addKeyValue("operation", "CancelFuelTransactionImport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = streamFuelCardExportRequest {
                this.context = request.context
                this.filter = StreamFuelCardExportRequestKt.filter {
                    this.createdAfter = request.details.effectiveAt
                    this.labels.putAll(request.annotationsMap)
                    this.ownerId = request.details.newOwner.userId
                }
            }
            fuelFuelCardExportService.streamFuelCardExport(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = recalculateTripsRequest {
                this.context = request.context
                this.trip = trip {
                    this.counterparty = employeeRef {
                        this.email = request.details.notifyContact.email
                    }
                }
            }
            val reply1 = withTimeout(5_000) { travelTripCalculationService.recalculateTrips(outbound1) }
            val configure2: ListFuelPolicyExportsRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.filter = ListFuelPolicyExportsRequestKt.filter {
                    this.createdAfter = reply1.trip.audit.updatedAt
                    this.labels.putAll(reply1.trip.metadataMap)
                    this.ownerId = reply1.trip.tripId
                    this.region = reply1.trip.billingAddress.region
                }
            }
            val outbound2 = listFuelPolicyExportsRequest(configure2)
            val reply2 = fuelFuelPolicyExportService.listFuelPolicyExports(outbound2)
            cancelFuelTransactionImportResponse {
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                    this.subject = customerRef {
                        this.email = reply1.trip.counterparty.email
                    }
                }
                this.outcome = CancelFuelTransactionImportResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = reply2.result.status
                }
            }
        }

    /** Returns a fuel transaction. */
    override suspend fun getFuelTransactionImportStatus(request: GetFuelTransactionImportStatusRequest): GetFuelTransactionImportStatusResponse {
        return handleGetFuelTransactionImportStatus(request)
    }

    /** Takes a stream of fuel transactions and answers once at the end. */
    override suspend fun importFuelTransactions(requests: Flow<ImportFuelTransactionsRequest>): ImportFuelTransactionsResponse {
        var answer = ImportFuelTransactionsResponse.getDefaultInstance()
        requests.map { request -> handleImportFuelTransactions(request) }.collect { answer = it }
        return answer
    }

    /** Lists fuel transactions a page at a time. */
    override suspend fun listFuelTransactionImports(request: ListFuelTransactionImportsRequest): ListFuelTransactionImportsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionImportService/ListFuelTransactionImports") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListFuelTransactionImports", request)
            log.atInfo().addKeyValue("operation", "ListFuelTransactionImports").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = request.toRetryFuelCardImportFromListFuelTransactionImports0()
            val reply0 = fuelFuelCardImportService.retryFuelCardImport(outbound0)
            listFuelTransactionImportsResponse {
                this.fuelTransactions.addAll(listOf(fuelTransaction {
                    this.subject = customerRef {
                        this.displayName = reply0.fuelCard.holder.displayName
                    }
                }))
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
            }
        }

    /** Maps a fuel transaction. */
    override suspend fun mapFuelTransactionImportColumns(request: MapFuelTransactionImportColumnsRequest): MapFuelTransactionImportColumnsResponse {
        return handleMapFuelTransactionImportColumns(request)
    }

    /** Retrys a fuel transaction. */
    override suspend fun retryFuelTransactionImport(request: RetryFuelTransactionImportRequest): RetryFuelTransactionImportResponse = handleRetryFuelTransactionImport(request)

    /** Validates a fuel transaction. */
    override suspend fun validateFuelTransactionImport(request: ValidateFuelTransactionImportRequest): ValidateFuelTransactionImportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionImportService/ValidateFuelTransactionImport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ValidateFuelTransactionImport", request)
            log.atInfo().addKeyValue("operation", "ValidateFuelTransactionImport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransaction.fuelTransactionId).log("Recorded audit detail")
            val outbound0 = browseFuelTransactionsRequest {
                this.context = request.context
                this.filter = BrowseFuelTransactionsRequestKt.filter {
                    this.createdAfter = request.fuelTransaction.occurredAt
                    this.labels.putAll(request.fuelTransaction.attributesMap)
                    this.ownerId = request.fuelTransaction.subject.customerId
                }
            }
            val invoke0: suspend (BrowseFuelTransactionsRequest, Metadata) -> BrowseFuelTransactionsResponse = fuelFuelTransactionQueryService::browseFuelTransactions
            val reply0 = invoke0(outbound0, Metadata())
            validateFuelTransactionImportResponse {
                this.fuelTransaction = fuelTransaction {
                    this.correlationId = reply0.result.correlationId
                    this.fuelTransactionId = request.fuelTransactionId
                    this.occurredAt = request.fuelTransaction.occurredAt
                }
                this.outcome = ValidateFuelTransactionImportResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleGetFuelTransactionImportStatus(request: GetFuelTransactionImportStatusRequest): GetFuelTransactionImportStatusResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionImportService/GetFuelTransactionImportStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetFuelTransactionImportStatus", request)
            log.atInfo().addKeyValue("operation", "GetFuelTransactionImportStatus").log("Handling transport request")
            val outbound0 = signalDeliveryWorkflowRequest {
                this.context = request.context
                this.details = SignalDeliveryWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = deliveriesDeliveryWorkflowService.signalDeliveryWorkflow(outbound0)
            getFuelTransactionImportStatusResponse {
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleImportFuelTransactions(request: ImportFuelTransactionsRequest): ImportFuelTransactionsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionImportService/ImportFuelTransactions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ImportFuelTransactions", request)
            log.atInfo().addKeyValue("operation", "ImportFuelTransactions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.header.source).log("Recorded audit detail")
            val outbound0 = batchGetDocumentsRequest {
                this.context = request.context
            }
            val reply0 = documentsDocumentService.batchGetDocuments(outbound0)
            var reply1 = NormalizeVenueResponse.getDefaultInstance()
            request.record.measurementsList.asFlow().map { item ->
                val outbound1 = normalizeVenueRequest {
                    this.context = request.context
                    this.details = NormalizeVenueRequestKt.details {
                        this.contact = contactInfo {
                            this.email = request.record.subject.email
                            this.name = request.header.submittedBy.name
                            this.preferredChannel = request.header.submittedBy.preferredChannel
                        }
                    }
                    this.venue = venue {
                        this.dimensions = venueDimensions {
                            this.unit = item.unit
                        }
                    }
                }
                venuesVenueValidationService.normalizeVenue(outbound1)
            }.collect { reply1 = it }
            importFuelTransactionsResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private fun ListFuelTransactionImportsRequest.toRetryFuelCardImportFromListFuelTransactionImports0(): RetryFuelCardImportRequest {
        val source = this
        return retryFuelCardImportRequest {
            this.context = source.context
            this.fuelCard = fuelCard {
                this.contact = contactInfo {
                    this.email = source.context.actor.email
                }
                this.labels.putAll(source.filter.labelsMap)
            }
        }
    }

    private suspend fun handleMapFuelTransactionImportColumns(request: MapFuelTransactionImportColumnsRequest): MapFuelTransactionImportColumnsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionImportService/MapFuelTransactionImportColumns") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "MapFuelTransactionImportColumns", request)
            log.atInfo().addKeyValue("operation", "MapFuelTransactionImportColumns").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransaction.fuelTransactionId).log("Recorded audit detail")
            val outbound0 = streamFuelPolicyExportRequest {
                this.context = request.context
                this.filter = StreamFuelPolicyExportRequestKt.filter {
                    this.createdAfter = request.fuelTransaction.occurredAt
                    this.ownerId = request.fuelTransaction.correlationId
                }
            }
            fuelFuelPolicyExportService.streamFuelPolicyExport(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            var reply1 = SubmitDockResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<FuelTransactionMeasurement>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound1 = submitDockRequest {
                            this.context = request.context
                            this.dock = dock {
                                this.dimensions = dockDimensions {
                                    this.unit = item.unit
                                }
                            }
                        }
                        reply1 = yardsDockApprovalService.submitDock(outbound1)
                    }
                }
                try {
                    request.fuelTransaction.measurementsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound2 = updateCustomerRequest {
                this.context = request.context
                this.customer = customer {
                    this.audit = auditInfo {
                        this.createdBy = reply1.dock.audit.createdBy
                    }
                    this.contact = contactInfo {
                        this.email = request.details.contact.email
                    }
                    this.primaryAddress = postalAddress {
                        this.lines.addAll(reply1.dock.address.linesList)
                        this.recipient = reply1.dock.address.recipient
                    }
                }
            }
            val reply2 = customersCustomerService.updateCustomer(WireCopies.json(outbound2))
            var reply3 = RetryFuelCardExportResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.fuelTransaction.measurementsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound3 = retryFuelCardExportRequest {
                                this.context = request.context
                                this.fuelCard = fuelCard {
                                    this.audit = auditInfo {
                                        this.updatedAt = item.observedAt
                                    }
                                }
                            }
                            fuelFuelCardExportService.retryFuelCardExport(outbound3)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply3 = it }
                }
            }
            mapFuelTransactionImportColumnsResponse {
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                    this.subject = request.fuelTransaction.subject
                }
                this.outcome = MapFuelTransactionImportColumnsResponseKt.outcome {
                    this.status = reply2.result.status
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply2.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleRetryFuelTransactionImport(request: RetryFuelTransactionImportRequest): RetryFuelTransactionImportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionImportService/RetryFuelTransactionImport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RetryFuelTransactionImport", request)
            log.atInfo().addKeyValue("operation", "RetryFuelTransactionImport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransaction.kind).log("Recorded audit detail")
            var reply0 = EstimateConsentRecordResponse.getDefaultInstance()
            request.fuelTransaction.measurementsList.asFlow().map { item ->
                val outbound0 = estimateConsentRecordRequest {
                    this.consentRecord = consentRecord {
                        this.measurements.addAll(listOf(consentRecordMeasurement {
                            this.metric = item.metric
                            this.observedAt = item.observedAt
                            this.unit = item.unit
                            this.value = item.value
                        }))
                    }
                    this.context = request.context
                }
                customersConsentRecordCalculationService.estimateConsentRecord(outbound0)
            }.collect { reply0 = it }
            var reply1 = SuggestFeatureFlagsResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<FuelTransactionMeasurement>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound1 = suggestFeatureFlagsRequest {
                            this.context = request.context
                            this.filter = SuggestFeatureFlagsRequestKt.filter {
                                this.createdAfter = item.observedAt
                                this.ownerId = request.fuelTransaction.fuelTransactionId
                            }
                        }
                        reply1 = featureflagsFeatureFlagQueryService.suggestFeatureFlags(outbound1)
                    }
                }
                try {
                    request.fuelTransaction.measurementsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound2 = validateDriverLicenseRequest {
                this.context = request.context
                this.details = ValidateDriverLicenseRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.details.notify.email
                    }
                }
            }
            val reply2 = invokeRetryFuelTransactionImport2(outbound2)
            retryFuelTransactionImportResponse {
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                    this.sequence = request.fuelTransaction.sequence
                    this.subject = customerRef {
                        this.email = reply2.driverLicense.counterparty.email
                    }
                }
                this.outcome = RetryFuelTransactionImportResponseKt.outcome {
                    this.status = reply2.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }
}
