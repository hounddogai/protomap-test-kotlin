package com.acme.transport.telematics.grpc

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.contracts.v1.ContractServiceGrpcKt
import com.acme.contracts.v1.CreateContractRequest
import com.acme.contracts.v1.contract
import com.acme.contracts.v1.createContractRequest
import com.acme.customers.v1.ConsentRecordCalculationServiceGrpcKt
import com.acme.customers.v1.CreateCustomerRequest
import com.acme.customers.v1.CustomerCalculationServiceGrpcKt
import com.acme.customers.v1.CustomerSegmentCalculationServiceGrpcKt
import com.acme.customers.v1.CustomerSegmentValidationServiceGrpcKt
import com.acme.customers.v1.CustomerServiceGrpcKt
import com.acme.customers.v1.EstimateConsentRecordRequest
import com.acme.customers.v1.EstimateConsentRecordResponse
import com.acme.customers.v1.ExplainCustomerCalculationRequest
import com.acme.customers.v1.ExplainCustomerCalculationResponse
import com.acme.customers.v1.PreviewCustomerSegmentRequest
import com.acme.customers.v1.PreviewCustomerSegmentRequestKt
import com.acme.customers.v1.batchGetCustomersRequest
import com.acme.customers.v1.consentRecord
import com.acme.customers.v1.createCustomerRequest
import com.acme.customers.v1.customer
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.customerSegment
import com.acme.customers.v1.estimateConsentRecordRequest
import com.acme.customers.v1.explainCustomerCalculationRequest
import com.acme.customers.v1.householdRef
import com.acme.customers.v1.previewCustomerSegmentRequest
import com.acme.customers.v1.quoteCustomerSegmentRequest
import com.acme.customers.v1.simulateCustomerSegmentRequest
import com.acme.featureflags.v1.CheckFeatureFlagEligibilityRequestKt
import com.acme.featureflags.v1.FeatureFlagValidationServiceGrpcKt
import com.acme.featureflags.v1.checkFeatureFlagEligibilityRequest
import com.acme.fuel.v1.FuelCardSyncServiceGrpcKt
import com.acme.fuel.v1.ListFuelCardConflictsRequestKt
import com.acme.fuel.v1.listFuelCardConflictsRequest
import com.acme.identity.v1.userRef
import com.acme.telematics.v1.BrowseTelemetryReadingsRequestKt
import com.acme.telematics.v1.BrowseTelemetryReadingsResponse
import com.acme.telematics.v1.CancelTripLogExportRequest
import com.acme.telematics.v1.CancelTripLogExportResponse
import com.acme.telematics.v1.CancelTripLogExportResponseKt
import com.acme.telematics.v1.CountGeoFencesRequest
import com.acme.telematics.v1.CountGeoFencesRequestKt
import com.acme.telematics.v1.CountGeoFencesResponse
import com.acme.telematics.v1.EstimateTelemetryReadingRequest
import com.acme.telematics.v1.GeoFenceQueryServiceGrpcKt
import com.acme.telematics.v1.GetTripLogExportRequest
import com.acme.telematics.v1.GetTripLogExportResponse
import com.acme.telematics.v1.ListTelemetryReadingImportsRequestKt
import com.acme.telematics.v1.ListTripLogExportsRequest
import com.acme.telematics.v1.ListTripLogExportsResponse
import com.acme.telematics.v1.RetryTelemetryReadingImportRequest
import com.acme.telematics.v1.RetryTelemetryReadingImportRequestKt
import com.acme.telematics.v1.RetryTripLogExportRequest
import com.acme.telematics.v1.RetryTripLogExportResponse
import com.acme.telematics.v1.RetryTripLogExportResponseKt
import com.acme.telematics.v1.ScheduleTripLogExportRequest
import com.acme.telematics.v1.ScheduleTripLogExportResponse
import com.acme.telematics.v1.ShareTripLogExportRequest
import com.acme.telematics.v1.ShareTripLogExportResponse
import com.acme.telematics.v1.StreamTripLogExportRequest
import com.acme.telematics.v1.StreamTripLogExportResponse
import com.acme.telematics.v1.SummarizeTelemetryReadingsRequestKt
import com.acme.telematics.v1.TelemetryReadingCalculationServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingImportServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingQueryServiceGrpcKt
import com.acme.telematics.v1.TripLogExportServiceGrpcKt
import com.acme.telematics.v1.TripLogLine
import com.acme.telematics.v1.browseTelemetryReadingsRequest
import com.acme.telematics.v1.cancelTripLogExportResponse
import com.acme.telematics.v1.countGeoFencesRequest
import com.acme.telematics.v1.estimateTelemetryReadingRequest
import com.acme.telematics.v1.geoFenceRef
import com.acme.telematics.v1.getTripLogExportResponse
import com.acme.telematics.v1.listTelemetryReadingImportsRequest
import com.acme.telematics.v1.listTripLogExportsResponse
import com.acme.telematics.v1.retryTelemetryReadingImportRequest
import com.acme.telematics.v1.retryTripLogExportResponse
import com.acme.telematics.v1.scheduleTripLogExportResponse
import com.acme.telematics.v1.shareTripLogExportResponse
import com.acme.telematics.v1.streamTripLogExportResponse
import com.acme.telematics.v1.summarizeTelemetryReadingsRequest
import com.acme.telematics.v1.telemetryReading
import com.acme.telematics.v1.tripLog
import com.acme.telematics.v1.tripLogLine
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.transport.telematics.TelematicsRepository
import com.google.protobuf.Any as ProtoAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Exports trip logs to files and other systems. */
class TripLogExportEndpoint(
    private val store: TelematicsRepository,
    private val workers: CoroutineScope,
    private val contractsContractService: ContractServiceGrpcKt.ContractServiceCoroutineStub,
    private val customersConsentRecordCalculationService: ConsentRecordCalculationServiceGrpcKt.ConsentRecordCalculationServiceCoroutineStub,
    private val customersCustomerCalculationService: CustomerCalculationServiceGrpcKt.CustomerCalculationServiceCoroutineStub,
    private val customersCustomerSegmentCalculationService: CustomerSegmentCalculationServiceGrpcKt.CustomerSegmentCalculationServiceCoroutineStub,
    private val customersCustomerSegmentValidationService: CustomerSegmentValidationServiceGrpcKt.CustomerSegmentValidationServiceCoroutineStub,
    private val customersCustomerService: CustomerServiceGrpcKt.CustomerServiceCoroutineStub,
    private val featureflagsFeatureFlagValidationService: FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub,
    private val fuelFuelCardSyncService: FuelCardSyncServiceGrpcKt.FuelCardSyncServiceCoroutineStub,
    private val telematicsGeoFenceQueryService: GeoFenceQueryServiceGrpcKt.GeoFenceQueryServiceCoroutineStub,
    private val telematicsTelemetryReadingCalculationService: TelemetryReadingCalculationServiceGrpcKt.TelemetryReadingCalculationServiceCoroutineStub,
    private val telematicsTelemetryReadingImportService: TelemetryReadingImportServiceGrpcKt.TelemetryReadingImportServiceCoroutineStub,
    private val telematicsTelemetryReadingQueryService: TelemetryReadingQueryServiceGrpcKt.TelemetryReadingQueryServiceCoroutineStub,
) : TripLogExportServiceGrpcKt.TripLogExportServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(TripLogExportEndpoint::class.java)

    private val invokeRetryTripLogExport0: suspend (CountGeoFencesRequest) -> CountGeoFencesResponse = { payload ->
        telematicsGeoFenceQueryService.countGeoFences(payload)
    }

    private val workerForShareTripLogExport1 = ReplyMailbox<EstimateConsentRecordRequest, EstimateConsentRecordResponse>(workers) { payload ->
        withTimeout(5_000) { customersConsentRecordCalculationService.estimateConsentRecord(payload) }
    }

    private val clientForStreamTripLogExport0 by lazy { telematicsTelemetryReadingQueryService }

    /** Cancels a trip log. */
    override suspend fun cancelTripLogExport(request: CancelTripLogExportRequest): CancelTripLogExportResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogExportService/CancelTripLogExport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CancelTripLogExport", request)
            log.atInfo().addKeyValue("operation", "CancelTripLogExport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = request.toEstimateTelemetryReadingFromCancelTripLogExport0()
            val reply0 = telematicsTelemetryReadingCalculationService.estimateTelemetryReading(outbound0)
            val outbound1 = quoteCustomerSegmentRequest {
                this.context = request.context
                this.customerSegment = customerSegment {
                    this.owner = userRef {
                        this.email = request.details.notifyContact.email
                    }
                }
            }
            val reply1 = customersCustomerSegmentCalculationService.quoteCustomerSegment(outbound1)
            val outbound2 = createCustomerRequest {
                this.context = request.context
                this.customer = customer {
                    this.audit = auditInfo {
                        this.etag = reply1.customerSegment.audit.etag
                    }
                    this.contact = contactInfo {
                        this.preferredChannel = request.details.notifyContact.preferredChannel
                    }
                    this.relationshipManager = userRef {
                        this.email = reply1.customerSegment.owner.email
                        this.userId = request.details.newOwner.userId
                    }
                }
            }
            val envelope2 = ProtoAny.pack(outbound2)
            val reply2 = customersCustomerService.createCustomer(envelope2.unpack(CreateCustomerRequest::class.java))
            val outbound3 = listFuelCardConflictsRequest {
                this.context = request.context
                this.filter = ListFuelCardConflictsRequestKt.filter {
                    this.region = reply2.customer.primaryAddress.region
                }
            }
            val reply3 = fuelFuelCardSyncService.listFuelCardConflicts(WireCopies.json(outbound3))
            cancelTripLogExportResponse {
                this.outcome = CancelTripLogExportResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.message = reply2.result.message
                }
                this.result = operationResult {
                    this.referenceIds.addAll((reply0.result.referenceIdsList) + (reply3.result.referenceIdsList))
                    this.status = reply3.result.status
                }
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.createdAt = reply1.customerSegment.audit.createdAt
                    }
                    this.total = reply0.breakdown.total
                    this.tripLogId = request.tripLogId
                }
            }
        }

    /** Returns a trip log. */
    override suspend fun getTripLogExport(request: GetTripLogExportRequest): GetTripLogExportResponse {
        return handleGetTripLogExport(request)
    }

    /** Lists trip logs a page at a time. */
    override suspend fun listTripLogExports(request: ListTripLogExportsRequest): ListTripLogExportsResponse = handleListTripLogExports(request)

    /** Retrys a trip log. */
    override suspend fun retryTripLogExport(request: RetryTripLogExportRequest): RetryTripLogExportResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogExportService/RetryTripLogExport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RetryTripLogExport", request)
            log.atInfo().addKeyValue("operation", "RetryTripLogExport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLog.tripLogId).log("Recorded audit detail")
            val outbound0 = countGeoFencesRequest {
                this.context = request.context
                this.filter = CountGeoFencesRequestKt.filter {
                    this.createdAfter = request.tripLog.audit.updatedAt
                    this.region = request.tripLog.billingAddress.region
                }
            }
            val reply0 = invokeRetryTripLogExport0(outbound0)
            var reply1 = BrowseTelemetryReadingsResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<TripLogLine>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound1 = browseTelemetryReadingsRequest {
                            this.context = request.context
                            this.filter = BrowseTelemetryReadingsRequestKt.filter {
                                this.labels.putAll(item.attributesMap)
                                this.ownerId = item.lineId
                                this.region = request.tripLog.billingAddress.region
                            }
                        }
                        reply1 = telematicsTelemetryReadingQueryService.browseTelemetryReadings(outbound1)
                    }
                }
                try {
                    request.tripLog.linesList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound2 = CreateContractRequest.newBuilder().apply {
                setContext(request.context)
                setContract(contract {
                    this.audit = auditInfo {
                        this.createdBy = request.tripLog.audit.createdBy
                    }
                    this.billingAddress = postalAddress {
                        this.lines.addAll(request.tripLog.billingAddress.linesList)
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply2 = outbound2.let { value -> contractsContractService.createContract(value) }
            val outbound3 = RequestFactory.create {
                listTelemetryReadingImportsRequest {
                    this.context = request.context
                    this.filter = ListTelemetryReadingImportsRequestKt.filter {
                        this.createdAfter = request.tripLog.audit.updatedAt
                        this.ownerId = request.tripLog.counterparty.customerId
                        this.region = reply2.contract.billingAddress.region
                    }
                }
            }
            val reply3 = telematicsTelemetryReadingImportService.listTelemetryReadingImports(outbound3)
            retryTripLogExportResponse {
                this.outcome = RetryTripLogExportResponseKt.outcome {
                    this.message = reply0.result.message
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.details.putAll(reply3.result.detailsMap)
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.billingAddress = reply2.contract.billingAddress
                    this.metadata.putAll(request.tripLog.metadataMap)
                    this.tripLogId = request.tripLogId
                }
            }
        }

    /** Schedules a trip log. */
    override suspend fun scheduleTripLogExport(request: ScheduleTripLogExportRequest): ScheduleTripLogExportResponse {
        return handleScheduleTripLogExport(request)
    }

    /** Shares a trip log. */
    override suspend fun shareTripLogExport(request: ShareTripLogExportRequest): ShareTripLogExportResponse = handleShareTripLogExport(request)

    /** Streams every matching trip log. */
    override fun streamTripLogExport(request: StreamTripLogExportRequest): Flow<StreamTripLogExportResponse> = flow {
        emit(handleStreamTripLogExport(request))
    }

    private fun CancelTripLogExportRequest.toEstimateTelemetryReadingFromCancelTripLogExport0(): EstimateTelemetryReadingRequest {
        val source = this
        return estimateTelemetryReadingRequest {
            this.annotations.putAll(source.annotationsMap)
            this.context = source.context
            this.telemetryReading = telemetryReading {
                this.subject = geoFenceRef {
                    this.displayName = source.details.newOwner.displayName
                }
            }
        }
    }

    private data class GetTripLogExport0Command(
        val context: RequestContext,
        val detailsNotifyEmail: String,
        val telemetryReadingCorrelationId: String
    ) {
        fun toRequest(): RetryTelemetryReadingImportRequest {
            val command = this
            return retryTelemetryReadingImportRequest {
                this.context = command.context
                this.details = RetryTelemetryReadingImportRequestKt.details {
                    this.notify = contactInfo {
                        this.email = command.detailsNotifyEmail
                    }
                }
                this.telemetryReading = telemetryReading {
                    this.correlationId = command.telemetryReadingCorrelationId
                }
            }
        }
    }

    private suspend fun handleGetTripLogExport(request: GetTripLogExportRequest): GetTripLogExportResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogExportService/GetTripLogExport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetTripLogExport", request)
            log.atInfo().addKeyValue("operation", "GetTripLogExport").log("Handling transport request")
            val outbound0 = GetTripLogExport0Command(
                context = request.context,
                detailsNotifyEmail = request.context.actor.email,
                telemetryReadingCorrelationId = request.tripLogId
            ).toRequest()
            val reply0 = telematicsTelemetryReadingImportService.retryTelemetryReadingImport(outbound0)
            getTripLogExportResponse {
                this.result = operationResult {
                    this.status = reply0.outcome.status
                }
                this.tripLog = tripLog {
                    this.counterparty = customerRef {
                        this.displayName = reply0.telemetryReading.subject.displayName
                    }
                    this.tripLogId = request.tripLogId
                }
            }
        }

    private suspend fun handleListTripLogExports(request: ListTripLogExportsRequest): ListTripLogExportsResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogExportService/ListTripLogExports") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListTripLogExports", request)
            log.atInfo().addKeyValue("operation", "ListTripLogExports").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = previewCustomerSegmentRequest {
                this.context = request.context
                this.customerSegment = customerSegment {
                    this.owner = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                }
                this.details = PreviewCustomerSegmentRequestKt.details {
                    this.locale = request.context.locale
                }
            }
            val command0: Submission<PreviewCustomerSegmentRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = customersCustomerSegmentValidationService.previewCustomerSegment(payload0)
            val outbound1 = explainCustomerCalculationRequest {
                this.context = request.context
                this.customer = customer {
                    this.audit = auditInfo {
                        this.createdAt = reply0.customerSegment.audit.createdAt
                        this.etag = reply0.customerSegment.audit.etag
                    }
                    this.primaryAddress = postalAddress {
                        this.region = request.filter.region
                    }
                    this.relationshipManager = userRef {
                        this.displayName = reply0.customerSegment.owner.displayName
                    }
                }
            }
            val adapter1 = OutboundPort<ExplainCustomerCalculationRequest, ExplainCustomerCalculationResponse> { payload ->
                customersCustomerCalculationService.explainCustomerCalculation(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            listTripLogExportsResponse {
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
                this.tripLogs.addAll(listOf(tripLog {
                    this.billingAddress = postalAddress {
                        this.recipient = reply1.customer.primaryAddress.recipient
                    }
                    this.counterparty = customerRef {
                        this.email = reply0.customerSegment.owner.email
                    }
                }))
            }
        }

    private suspend fun handleScheduleTripLogExport(request: ScheduleTripLogExportRequest): ScheduleTripLogExportResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogExportService/ScheduleTripLogExport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScheduleTripLogExport", request)
            log.atInfo().addKeyValue("operation", "ScheduleTripLogExport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLogId).log("Recorded audit detail")
            val outbound0 = summarizeTelemetryReadingsRequest {
                this.context = request.context
                this.details = SummarizeTelemetryReadingsRequestKt.details {
                    this.filters.putAll(request.details.filtersMap)
                    this.period = request.details.period
                }
                this.reason = request.reason
            }
            val reply0 = ClientRegistry.lookup("acme.telematics.v1.TelemetryReadingQueryService", TelemetryReadingQueryServiceGrpcKt.TelemetryReadingQueryServiceCoroutineStub::class.java).summarizeTelemetryReadings(outbound0)
            scheduleTripLogExportResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleShareTripLogExport(request: ShareTripLogExportRequest): ShareTripLogExportResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogExportService/ShareTripLogExport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ShareTripLogExport", request)
            log.atInfo().addKeyValue("operation", "ShareTripLogExport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.recipient.phone).log("Recorded audit detail")
            val outbound0 = simulateCustomerSegmentRequest {
                this.context = request.context
                this.customerSegment = customerSegment {
                    this.owner = userRef {
                        this.email = request.details.recipient.email
                    }
                }
            }
            val reply0 = customersCustomerSegmentCalculationService.simulateCustomerSegment(WireCopies.reflective(outbound0))
            val outbound1 = estimateConsentRecordRequest {
                this.consentRecord = consentRecord {
                    this.subject = householdRef {
                        this.displayName = reply0.customerSegment.owner.displayName
                    }
                }
                this.context = request.context
            }
            val reply1 = workerForShareTripLogExport1.submit(outbound1)
            shareTripLogExportResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.counterparty = customerRef {
                        this.email = request.details.recipient.email
                    }
                    this.lines.addAll(listOf(tripLogLine {
                        this.attributes.putAll(reply1.consentRecord.attributesMap)
                    }))
                    this.total = reply0.breakdown.total
                    this.tripLogId = request.tripLogId
                }
            }
        }

    private suspend fun handleStreamTripLogExport(request: StreamTripLogExportRequest): StreamTripLogExportResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogExportService/StreamTripLogExport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StreamTripLogExport", request)
            log.atInfo().addKeyValue("operation", "StreamTripLogExport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = summarizeTelemetryReadingsRequest {
                this.annotations.putAll(request.context.baggageMap)
                this.context = request.context
                this.details = SummarizeTelemetryReadingsRequestKt.details {
                    this.filters.putAll(request.filter.labelsMap)
                    this.format = request.format
                }
            }
            val reply0 = clientForStreamTripLogExport0.summarizeTelemetryReadings(outbound0)
            val outbound1 = checkFeatureFlagEligibilityRequest {
                this.context = request.context
                this.details = CheckFeatureFlagEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply1 = flowOf(outbound1).map { value -> featureflagsFeatureFlagValidationService.checkFeatureFlagEligibility(value) }.single()
            val outbound2 = batchGetCustomersRequest {
                this.context = request.context
            }
            val reply2 = withTimeout(5_000) { customersCustomerService.batchGetCustomers(outbound2) }
            streamTripLogExportResponse {
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.billingAddress = postalAddress {
                        this.region = request.filter.region
                    }
                    this.lines.addAll(listOf(tripLogLine {
                        this.description = reply1.featureFlag.description
                    }))
                }
            }
        }
}
