package com.acme.transport.drivers.grpc

import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.PreviewAuditEventRequestKt
import com.acme.audit.v1.ValidateAuditEventRequestKt
import com.acme.audit.v1.ValidateAuditEventResponse
import com.acme.audit.v1.auditEvent
import com.acme.audit.v1.auditTrailRef
import com.acme.audit.v1.previewAuditEventRequest
import com.acme.audit.v1.validateAuditEventRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.configuration.v1.ConfigSetCalculationServiceGrpcKt
import com.acme.configuration.v1.configSet
import com.acme.configuration.v1.quoteConfigSetRequest
import com.acme.dispatch.v1.CountDispatchOrdersRequestKt
import com.acme.dispatch.v1.DispatchAssignmentCalculationServiceGrpcKt
import com.acme.dispatch.v1.DispatchOrderQueryServiceGrpcKt
import com.acme.dispatch.v1.EstimateDispatchAssignmentRequest
import com.acme.dispatch.v1.EstimateDispatchAssignmentRequestKt
import com.acme.dispatch.v1.countDispatchOrdersRequest
import com.acme.dispatch.v1.dispatchAssignment
import com.acme.dispatch.v1.estimateDispatchAssignmentRequest
import com.acme.drivers.v1.CalculateDriverLicenseRequest
import com.acme.drivers.v1.CalculateDriverLicenseResponse
import com.acme.drivers.v1.CalculateDriverLicenseResponseKt
import com.acme.drivers.v1.CalculateDriverRequest
import com.acme.drivers.v1.CalculateDriverRequestKt
import com.acme.drivers.v1.CalculateDriverResponse
import com.acme.drivers.v1.CompareDriverLicenseOptionsRequest
import com.acme.drivers.v1.CompareDriverLicenseOptionsResponse
import com.acme.drivers.v1.CompareDriverLicenseOptionsResponseKt
import com.acme.drivers.v1.DriverCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseLine
import com.acme.drivers.v1.EstimateDriverLicenseRequest
import com.acme.drivers.v1.EstimateDriverLicenseResponse
import com.acme.drivers.v1.ExplainDriverCalculationRequest
import com.acme.drivers.v1.ExplainDriverCalculationResponse
import com.acme.drivers.v1.ExplainDriverLicenseCalculationRequest
import com.acme.drivers.v1.ExplainDriverLicenseCalculationResponse
import com.acme.drivers.v1.GetDriverLicenseRatesRequest
import com.acme.drivers.v1.GetDriverLicenseRatesResponse
import com.acme.drivers.v1.GetDriverRatesRequest
import com.acme.drivers.v1.QuoteDriverLicenseRequest
import com.acme.drivers.v1.QuoteDriverLicenseResponse
import com.acme.drivers.v1.RecalculateDriverLicensesRequest
import com.acme.drivers.v1.RecalculateDriverLicensesResponse
import com.acme.drivers.v1.SimulateDriverLicenseRequest
import com.acme.drivers.v1.SimulateDriverLicenseResponse
import com.acme.drivers.v1.SimulateDriverLicenseResponseKt
import com.acme.drivers.v1.SimulateDriverRequest
import com.acme.drivers.v1.SimulateDriverRequestKt
import com.acme.drivers.v1.calculateDriverLicenseResponse
import com.acme.drivers.v1.calculateDriverRequest
import com.acme.drivers.v1.compareDriverLicenseOptionsResponse
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.estimateDriverLicenseResponse
import com.acme.drivers.v1.explainDriverCalculationRequest
import com.acme.drivers.v1.explainDriverLicenseCalculationResponse
import com.acme.drivers.v1.getDriverLicenseRatesResponse
import com.acme.drivers.v1.getDriverRatesRequest
import com.acme.drivers.v1.quoteDriverLicenseResponse
import com.acme.drivers.v1.quoteDriverRequest
import com.acme.drivers.v1.recalculateDriverLicensesResponse
import com.acme.drivers.v1.simulateDriverLicenseResponse
import com.acme.drivers.v1.simulateDriverRequest
import com.acme.identity.v1.BrowseUsersRequestKt
import com.acme.identity.v1.UserQueryServiceGrpcKt
import com.acme.identity.v1.browseUsersRequest
import com.acme.identity.v1.userRef
import com.acme.metrics.v1.LintMetricSeriesesRequest
import com.acme.metrics.v1.LintMetricSeriesesRequestKt
import com.acme.metrics.v1.MetricSeriesValidationServiceGrpcKt
import com.acme.metrics.v1.lintMetricSeriesesRequest
import com.acme.metrics.v1.metricSeries
import com.acme.transport.drivers.DriversRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.DockCalculationServiceGrpcKt
import com.acme.yards.v1.EstimateDockRequestKt
import com.acme.yards.v1.ExportYardsRequestKt
import com.acme.yards.v1.SimulateDockRequest
import com.acme.yards.v1.YardQueryServiceGrpcKt
import com.acme.yards.v1.dock
import com.acme.yards.v1.estimateDockRequest
import com.acme.yards.v1.exportYardsRequest
import com.acme.yards.v1.simulateDockRequest
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Computes the figures of a driver license, such as totals, taxes, and estimates. */
class DriverLicenseCalculationEndpoint(
    private val store: DriversRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventValidationService: AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub,
    private val configurationConfigSetCalculationService: ConfigSetCalculationServiceGrpcKt.ConfigSetCalculationServiceCoroutineStub,
    private val dispatchDispatchAssignmentCalculationService: DispatchAssignmentCalculationServiceGrpcKt.DispatchAssignmentCalculationServiceCoroutineStub,
    private val dispatchDispatchOrderQueryService: DispatchOrderQueryServiceGrpcKt.DispatchOrderQueryServiceCoroutineStub,
    private val driversDriverCalculationService: DriverCalculationServiceGrpcKt.DriverCalculationServiceCoroutineStub,
    private val identityUserQueryService: UserQueryServiceGrpcKt.UserQueryServiceCoroutineStub,
    private val metricsMetricSeriesValidationService: MetricSeriesValidationServiceGrpcKt.MetricSeriesValidationServiceCoroutineStub,
    private val yardsDockCalculationService: DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub,
    private val yardsYardQueryService: YardQueryServiceGrpcKt.YardQueryServiceCoroutineStub,
) : DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DriverLicenseCalculationEndpoint::class.java)

    private val workerForQuoteDriverLicense2 = ReplyMailbox<CalculateDriverRequest, CalculateDriverResponse>(workers) { payload ->
        withTimeout(5_000) { driversDriverCalculationService.calculateDriver(payload) }
    }

    private val clientForRecalculateDriverLicenses0 by lazy { driversDriverCalculationService }

    /** Calculates a driver license. */
    override suspend fun calculateDriverLicense(request: CalculateDriverLicenseRequest): CalculateDriverLicenseResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseCalculationService/CalculateDriverLicense") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CalculateDriverLicense", request)
            log.atInfo().addKeyValue("operation", "CalculateDriverLicense").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverLicense.driverLicenseId).log("Recorded audit detail")
            val outbound0 = request.toGetDriverRatesFromCalculateDriverLicense0()
            val reply0 = driversDriverCalculationService.getDriverRates(outbound0)
            calculateDriverLicenseResponse {
                this.breakdown = CalculateDriverLicenseResponseKt.breakdown {
                    this.currencyCode = request.details.currencyCode
                }
                this.driverLicense = driverLicense {
                    this.audit = auditInfo {
                        this.createdBy = reply0.driver.audit.createdBy
                    }
                    this.billingAddress = postalAddress {
                        this.region = reply0.driver.primaryAddress.region
                    }
                    this.driverLicenseId = request.driverLicenseId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Compares a driver license. */
    override suspend fun compareDriverLicenseOptions(request: CompareDriverLicenseOptionsRequest): CompareDriverLicenseOptionsResponse {
        return handleCompareDriverLicenseOptions(request)
    }

    /** Estimates a driver license. */
    override suspend fun estimateDriverLicense(request: EstimateDriverLicenseRequest): EstimateDriverLicenseResponse = handleEstimateDriverLicense(request)

    /** Explains a driver license. */
    override suspend fun explainDriverLicenseCalculation(request: ExplainDriverLicenseCalculationRequest): ExplainDriverLicenseCalculationResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseCalculationService/ExplainDriverLicenseCalculation") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainDriverLicenseCalculation", request)
            log.atInfo().addKeyValue("operation", "ExplainDriverLicenseCalculation").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverLicense.driverLicenseId).log("Recorded audit detail")
            var reply0 = ValidateAuditEventResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<DriverLicenseLine>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound0 = validateAuditEventRequest {
                            this.auditEvent = auditEvent {
                                this.attributes.putAll(item.attributesMap)
                            }
                            this.context = request.context
                            this.details = ValidateAuditEventRequestKt.details {
                                this.contact = contactInfo {
                                    this.email = request.details.contact.email
                                }
                            }
                        }
                        reply0 = auditAuditEventValidationService.validateAuditEvent(outbound0)
                    }
                }
                try {
                    request.driverLicense.linesList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound1 = explainDriverCalculationRequest {
                this.context = request.context
                this.driver = driver {
                    this.contact = contactInfo {
                        this.preferredChannel = request.details.contact.preferredChannel
                    }
                    this.primaryAddress = postalAddress {
                        this.postalCode = request.driverLicense.billingAddress.postalCode
                        this.region = request.driverLicense.billingAddress.region
                    }
                    this.relationshipManager = userRef {
                        this.email = request.details.contact.email
                    }
                }
            }
            val invoke1: suspend (ExplainDriverCalculationRequest, Metadata) -> ExplainDriverCalculationResponse = driversDriverCalculationService::explainDriverCalculation
            val reply1 = invoke1(outbound1, Metadata())
            explainDriverLicenseCalculationResponse {
                this.driverLicense = driverLicense {
                    this.driverLicenseId = request.driverLicenseId
                    this.notes = request.driverLicense.notes
                }
                this.result = operationResult {
                    this.status = reply1.result.status
                }
            }
        }

    /** Returns a driver license. */
    override suspend fun getDriverLicenseRates(request: GetDriverLicenseRatesRequest): GetDriverLicenseRatesResponse {
        return handleGetDriverLicenseRates(request)
    }

    /** Quotes a driver license. */
    override suspend fun quoteDriverLicense(request: QuoteDriverLicenseRequest): QuoteDriverLicenseResponse = handleQuoteDriverLicense(request)

    /** Recalculates a driver license. */
    override suspend fun recalculateDriverLicenses(request: RecalculateDriverLicensesRequest): RecalculateDriverLicensesResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseCalculationService/RecalculateDriverLicenses") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecalculateDriverLicenses", request)
            log.atInfo().addKeyValue("operation", "RecalculateDriverLicenses").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverLicense.driverLicenseId).log("Recorded audit detail")
            val outbound0 = explainDriverCalculationRequest {
                this.context = request.context
                this.driver = driver {
                    this.audit = auditInfo {
                        this.createdAt = request.driverLicense.audit.createdAt
                    }
                }
            }
            val reply0 = clientForRecalculateDriverLicenses0.explainDriverCalculation(outbound0)
            recalculateDriverLicensesResponse {
                this.driverLicense = driverLicense {
                    this.counterparty = request.driverLicense.counterparty
                    this.driverLicenseId = request.driverLicenseId
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    /** Simulates a driver license. */
    override suspend fun simulateDriverLicense(request: SimulateDriverLicenseRequest): SimulateDriverLicenseResponse {
        return handleSimulateDriverLicense(request)
    }

    private fun CalculateDriverLicenseRequest.toGetDriverRatesFromCalculateDriverLicense0(): GetDriverRatesRequest {
        val source = this
        return getDriverRatesRequest {
            this.context = source.context
            this.driverId = source.driverLicense.counterparty.driverId
        }
    }

    private data class CompareDriverLicenseOptions3Command(
        val context: RequestContext,
        val driverPrimaryAddressRegion: String,
        val driverPrimaryAddressCountryCode: String,
        val detailsDestinationRecipient: String
    ) {
        fun toRequest(): SimulateDriverRequest {
            val command = this
            return simulateDriverRequest {
                this.context = command.context
                this.details = SimulateDriverRequestKt.details {
                    this.destination = postalAddress {
                        this.recipient = command.detailsDestinationRecipient
                    }
                }
                this.driver = driver {
                    this.primaryAddress = postalAddress {
                        this.countryCode = command.driverPrimaryAddressCountryCode
                        this.region = command.driverPrimaryAddressRegion
                    }
                }
            }
        }
    }

    private suspend fun handleCompareDriverLicenseOptions(request: CompareDriverLicenseOptionsRequest): CompareDriverLicenseOptionsResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseCalculationService/CompareDriverLicenseOptions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CompareDriverLicenseOptions", request)
            log.atInfo().addKeyValue("operation", "CompareDriverLicenseOptions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverLicense.driverLicenseId).log("Recorded audit detail")
            val outbound0 = browseUsersRequest {
                this.context = request.context
                this.filter = BrowseUsersRequestKt.filter {
                    this.createdAfter = request.driverLicense.issuedAt
                    this.region = request.driverLicense.billingAddress.region
                }
            }
            val reply0 = identityUserQueryService.browseUsers(outbound0)
            val outbound1 = simulateDockRequest {
                this.context = request.context
                this.dock = dock {
                    this.address = postalAddress {
                        this.lines.addAll(request.driverLicense.billingAddress.linesList)
                        this.region = request.details.destination.region
                    }
                    this.owner = userRef {
                        this.displayName = request.driverLicense.counterparty.displayName
                    }
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = yardsDockCalculationService.simulateDock(envelope1.unpack(SimulateDockRequest::class.java))
            val outbound2 = getDriverRatesRequest {
                this.context = request.context
                this.driverId = request.driverLicense.counterparty.driverId
            }
            val reply2 = driversDriverCalculationService.getDriverRates(WireCopies.json(outbound2))
            val outbound3 = CompareDriverLicenseOptions3Command(
                context = request.context,
                driverPrimaryAddressRegion = request.details.destination.region,
                driverPrimaryAddressCountryCode = request.driverLicense.billingAddress.countryCode,
                detailsDestinationRecipient = request.details.destination.recipient
            ).toRequest()
            val reply3 = driversDriverCalculationService.simulateDriver(outbound3)
            compareDriverLicenseOptionsResponse {
                this.breakdown = CompareDriverLicenseOptionsResponseKt.breakdown {
                    this.total = request.driverLicense.total
                }
                this.driverLicense = driverLicense {
                    this.audit = auditInfo {
                        this.createdAt = reply2.driver.audit.createdAt
                        this.createdBy = reply1.dock.audit.createdBy
                    }
                    this.billingAddress = postalAddress {
                        this.locality = reply1.dock.address.locality
                    }
                    this.counterparty = driverRef {
                        this.driverId = reply3.driver.driverId
                    }
                    this.driverLicenseId = request.driverLicenseId
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleEstimateDriverLicense(request: EstimateDriverLicenseRequest): EstimateDriverLicenseResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseCalculationService/EstimateDriverLicense") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EstimateDriverLicense", request)
            log.atInfo().addKeyValue("operation", "EstimateDriverLicense").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverLicense.driverLicenseId).log("Recorded audit detail")
            val outbound0 = estimateDispatchAssignmentRequest {
                this.context = request.context
                this.details = EstimateDispatchAssignmentRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(request.details.destination.linesList)
                    }
                }
                this.dispatchAssignment = dispatchAssignment {
                    this.assignee = userRef {
                        this.displayName = request.driverLicense.counterparty.displayName
                    }
                    this.audit = auditInfo {
                        this.createdAt = request.driverLicense.audit.createdAt
                    }
                }
            }
            val command0: Submission<EstimateDispatchAssignmentRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = dispatchDispatchAssignmentCalculationService.estimateDispatchAssignment(payload0)
            request.driverLicense.linesList.asFlow().map { item ->
                val outbound1 = exportYardsRequest {
                    this.context = request.context
                    this.filter = ExportYardsRequestKt.filter {
                        this.ownerId = item.lineId
                        this.region = request.details.destination.region
                    }
                }
                yardsYardQueryService.exportYards(outbound1).collect { event ->
                    log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
                }
            }.collect()
            estimateDriverLicenseResponse {
                this.driverLicense = driverLicense {
                    this.billingAddress = request.driverLicense.billingAddress
                    this.counterparty = driverRef {
                        this.displayName = reply0.dispatchAssignment.assignee.displayName
                    }
                    this.driverLicenseId = request.driverLicenseId
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleGetDriverLicenseRates(request: GetDriverLicenseRatesRequest): GetDriverLicenseRatesResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseCalculationService/GetDriverLicenseRates") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetDriverLicenseRates", request)
            log.atInfo().addKeyValue("operation", "GetDriverLicenseRates").log("Handling transport request")
            val outbound0 = LintMetricSeriesesRequest.newBuilder().apply {
                setContext(request.context)
                setDetails(LintMetricSeriesesRequestKt.details {
                    this.locale = request.context.locale
                })
                setMetricSeries(metricSeries {
                    this.audit = auditInfo {
                        this.updatedAt = request.context.deadline
                    }
                    this.owner = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> metricsMetricSeriesValidationService.lintMetricSerieses(value) }
            val outbound1 = RequestFactory.create {
                previewAuditEventRequest {
                    this.auditEvent = auditEvent {
                        this.subject = auditTrailRef {
                            this.displayName = reply0.metricSeries.owner.displayName
                        }
                    }
                    this.context = request.context
                    this.details = PreviewAuditEventRequestKt.details {
                        this.contact = contactInfo {
                            this.email = reply0.metricSeries.owner.email
                        }
                    }
                }
            }
            val reply1 = auditAuditEventValidationService.previewAuditEvent(outbound1)
            getDriverLicenseRatesResponse {
                this.driverLicense = driverLicense {
                    this.driverLicenseId = request.driverLicenseId
                }
                this.result = operationResult {
                    this.message = reply1.outcome.message
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleQuoteDriverLicense(request: QuoteDriverLicenseRequest): QuoteDriverLicenseResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseCalculationService/QuoteDriverLicense") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "QuoteDriverLicense", request)
            log.atInfo().addKeyValue("operation", "QuoteDriverLicense").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverLicense.driverLicenseId).log("Recorded audit detail")
            val outbound0 = getDriverRatesRequest {
                this.context = request.context
                this.driverId = request.driverLicense.counterparty.driverId
            }
            val reply0 = ClientRegistry.lookup("acme.drivers.v1.DriverCalculationService", DriverCalculationServiceGrpcKt.DriverCalculationServiceCoroutineStub::class.java).getDriverRates(outbound0)
            val outbound1 = countDispatchOrdersRequest {
                this.context = request.context
                this.filter = CountDispatchOrdersRequestKt.filter {
                    this.createdAfter = request.driverLicense.audit.updatedAt
                    this.labels.putAll(request.driverLicense.metadataMap)
                    this.region = reply0.driver.primaryAddress.region
                }
            }
            val reply1 = dispatchDispatchOrderQueryService.countDispatchOrders(WireCopies.reflective(outbound1))
            val outbound2 = calculateDriverRequest {
                this.context = request.context
                this.details = CalculateDriverRequestKt.details {
                    this.destination = postalAddress {
                        this.locality = reply0.driver.primaryAddress.locality
                    }
                }
                this.driver = driver {
                    this.primaryAddress = postalAddress {
                        this.region = reply0.driver.primaryAddress.region
                    }
                }
            }
            val reply2 = workerForQuoteDriverLicense2.submit(outbound2)
            quoteDriverLicenseResponse {
                this.driverLicense = driverLicense {
                    this.audit = auditInfo {
                        this.createdBy = reply0.driver.audit.createdBy
                    }
                    this.counterparty = driverRef {
                        this.displayName = reply0.driver.relationshipManager.displayName
                    }
                    this.driverLicenseId = request.driverLicenseId
                    this.notes = request.driverLicense.notes
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSimulateDriverLicense(request: SimulateDriverLicenseRequest): SimulateDriverLicenseResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseCalculationService/SimulateDriverLicense") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SimulateDriverLicense", request)
            log.atInfo().addKeyValue("operation", "SimulateDriverLicense").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverLicense.driverLicenseId).log("Recorded audit detail")
            val outbound0 = explainDriverCalculationRequest {
                this.context = request.context
                this.driver = driver {
                    this.contact = contactInfo {
                        this.email = request.driverLicense.counterparty.email
                    }
                    this.primaryAddress = postalAddress {
                        this.recipient = request.details.destination.recipient
                    }
                }
            }
            val reply0 = flowOf(outbound0).map { value -> driversDriverCalculationService.explainDriverCalculation(value) }.single()
            val outbound1 = quoteDriverRequest {
                this.context = request.context
                this.driver = driver {
                    this.contact = contactInfo {
                        this.name = reply0.driver.contact.name
                    }
                    this.primaryAddress = postalAddress {
                        this.recipient = request.driverLicense.billingAddress.recipient
                    }
                }
            }
            val reply1 = withTimeout(5_000) { driversDriverCalculationService.quoteDriver(outbound1) }
            val configure2: EstimateDockRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.details = EstimateDockRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(request.details.destination.linesList)
                        this.postalCode = reply1.driver.primaryAddress.postalCode
                    }
                }
                this.dock = dock {
                    this.address = postalAddress {
                        this.region = request.details.destination.region
                    }
                    this.owner = userRef {
                        this.displayName = reply0.driver.relationshipManager.displayName
                    }
                }
            }
            val outbound2 = estimateDockRequest(configure2)
            val reply2 = yardsDockCalculationService.estimateDock(outbound2)
            val outbound3 = quoteConfigSetRequest {
                this.configSet = configSet {
                    this.children.addAll(listOf(configSet {
                        this.children.addAll(listOf(configSet {
                            this.audit = auditInfo {
                                this.updatedBy = reply1.driver.audit.updatedBy
                            }
                            this.location = postalAddress {
                                this.lines.addAll(reply1.driver.primaryAddress.linesList)
                                this.recipient = reply2.dock.address.recipient
                            }
                            this.manager = userRef {
                                this.userId = reply1.driver.relationshipManager.userId
                            }
                        }))
                    }))
                }
                this.context = request.context
            }
            val reply3 = configurationConfigSetCalculationService.quoteConfigSet(outbound3)
            simulateDriverLicenseResponse {
                this.breakdown = SimulateDriverLicenseResponseKt.breakdown {
                    this.currencyCode = request.details.currencyCode
                    this.total = reply2.breakdown.total
                }
                this.driverLicense = driverLicense {
                    this.billingAddress = postalAddress {
                        this.lines.addAll(reply3.configSet.location.linesList)
                        this.locality = reply3.configSet.location.locality
                    }
                    this.driverLicenseId = request.driverLicenseId
                    this.total = reply2.breakdown.total
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = reply0.outcome.status
                }
            }
        }
}
