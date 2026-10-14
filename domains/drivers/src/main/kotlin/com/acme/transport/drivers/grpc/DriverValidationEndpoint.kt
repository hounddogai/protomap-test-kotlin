package com.acme.transport.drivers.grpc

import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.ValidateAuditEventRequest
import com.acme.audit.v1.ValidateAuditEventRequestKt
import com.acme.audit.v1.ValidateAuditEventResponse
import com.acme.audit.v1.auditEvent
import com.acme.audit.v1.auditTrailRef
import com.acme.audit.v1.validateAuditEventRequest
import com.acme.authorization.v1.AccessPolicyServiceGrpcKt
import com.acme.authorization.v1.CreateAccessPolicyRequestKt
import com.acme.authorization.v1.accessPolicy
import com.acme.authorization.v1.createAccessPolicyRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.dispatch.v1.DispatchAssignmentCalculationServiceGrpcKt
import com.acme.dispatch.v1.calculateDispatchAssignmentRequest
import com.acme.dispatch.v1.dispatchAssignment
import com.acme.documents.v1.BatchGetDocumentsRequest
import com.acme.documents.v1.DocumentServiceGrpcKt
import com.acme.documents.v1.batchGetDocumentsRequest
import com.acme.drivers.v1.CalculateDriverRequestKt
import com.acme.drivers.v1.CheckDriverEligibilityRequest
import com.acme.drivers.v1.CheckDriverEligibilityResponse
import com.acme.drivers.v1.DriverCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverValidationServiceGrpcKt
import com.acme.drivers.v1.EstimateDriverLicenseRequest
import com.acme.drivers.v1.EstimateDriverLicenseRequestKt
import com.acme.drivers.v1.EstimateDriverLicenseResponse
import com.acme.drivers.v1.ExplainDriverDecisionRequest
import com.acme.drivers.v1.ExplainDriverDecisionResponse
import com.acme.drivers.v1.ExplainDriverDecisionResponseKt
import com.acme.drivers.v1.ExplainDriverLicenseCalculationRequest
import com.acme.drivers.v1.GetDriverRulesRequest
import com.acme.drivers.v1.GetDriverRulesResponse
import com.acme.drivers.v1.LintDriversRequest
import com.acme.drivers.v1.LintDriversResponse
import com.acme.drivers.v1.NormalizeDriverRequest
import com.acme.drivers.v1.NormalizeDriverResponse
import com.acme.drivers.v1.NormalizeDriverResponseKt
import com.acme.drivers.v1.PreviewDriverRequest
import com.acme.drivers.v1.PreviewDriverResponse
import com.acme.drivers.v1.PreviewDriverResponseKt
import com.acme.drivers.v1.QuoteDriverRequest
import com.acme.drivers.v1.QuoteDriverRequestKt
import com.acme.drivers.v1.QuoteDriverResponse
import com.acme.drivers.v1.ScoreDriverRequest
import com.acme.drivers.v1.ScoreDriverResponse
import com.acme.drivers.v1.ValidateDriverRequest
import com.acme.drivers.v1.ValidateDriverResponse
import com.acme.drivers.v1.ValidateDriverResponseKt
import com.acme.drivers.v1.calculateDriverRequest
import com.acme.drivers.v1.checkDriverEligibilityResponse
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverLicenseLine
import com.acme.drivers.v1.driverLicenseLineTax
import com.acme.drivers.v1.driverPassport
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.estimateDriverLicenseRequest
import com.acme.drivers.v1.explainDriverDecisionResponse
import com.acme.drivers.v1.explainDriverLicenseCalculationRequest
import com.acme.drivers.v1.getDriverRulesResponse
import com.acme.drivers.v1.lintDriversResponse
import com.acme.drivers.v1.normalizeDriverResponse
import com.acme.drivers.v1.previewDriverResponse
import com.acme.drivers.v1.quoteDriverLicenseRequest
import com.acme.drivers.v1.quoteDriverRequest
import com.acme.drivers.v1.scoreDriverResponse
import com.acme.drivers.v1.validateDriverResponse
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.ServiceScheduleQueryServiceGrpcKt
import com.acme.maintenance.v1.resolveServiceSchedulesRequest
import com.acme.metrics.v1.CheckMetricSeriesEligibilityRequest
import com.acme.metrics.v1.CheckMetricSeriesEligibilityRequestKt
import com.acme.metrics.v1.CheckMetricSeriesEligibilityResponse
import com.acme.metrics.v1.MetricSeriesValidationServiceGrpcKt
import com.acme.metrics.v1.NormalizeMetricSeriesRequest
import com.acme.metrics.v1.NormalizeMetricSeriesRequestKt
import com.acme.metrics.v1.checkMetricSeriesEligibilityRequest
import com.acme.metrics.v1.metricSeries
import com.acme.metrics.v1.normalizeMetricSeriesRequest
import com.acme.routing.v1.CountRoutesRequestKt
import com.acme.routing.v1.RouteQueryServiceGrpcKt
import com.acme.routing.v1.countRoutesRequest
import com.acme.telematics.v1.SuggestTelemetryReadingsRequestKt
import com.acme.telematics.v1.TelemetryReadingQueryServiceGrpcKt
import com.acme.telematics.v1.suggestTelemetryReadingsRequest
import com.acme.transport.drivers.DriversRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.venues.v1.FindVenuesRequestKt
import com.acme.venues.v1.VenueQueryServiceGrpcKt
import com.acme.venues.v1.findVenuesRequest
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Checks drivers against the rules before anything acts on them. */
class DriverValidationEndpoint(
    private val store: DriversRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventValidationService: AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub,
    private val authorizationAccessPolicyService: AccessPolicyServiceGrpcKt.AccessPolicyServiceCoroutineStub,
    private val dispatchDispatchAssignmentCalculationService: DispatchAssignmentCalculationServiceGrpcKt.DispatchAssignmentCalculationServiceCoroutineStub,
    private val documentsDocumentService: DocumentServiceGrpcKt.DocumentServiceCoroutineStub,
    private val driversDriverCalculationService: DriverCalculationServiceGrpcKt.DriverCalculationServiceCoroutineStub,
    private val driversDriverLicenseCalculationService: DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub,
    private val maintenanceServiceScheduleQueryService: ServiceScheduleQueryServiceGrpcKt.ServiceScheduleQueryServiceCoroutineStub,
    private val metricsMetricSeriesValidationService: MetricSeriesValidationServiceGrpcKt.MetricSeriesValidationServiceCoroutineStub,
    private val routingRouteQueryService: RouteQueryServiceGrpcKt.RouteQueryServiceCoroutineStub,
    private val telematicsTelemetryReadingQueryService: TelemetryReadingQueryServiceGrpcKt.TelemetryReadingQueryServiceCoroutineStub,
    private val venuesVenueQueryService: VenueQueryServiceGrpcKt.VenueQueryServiceCoroutineStub,
) : DriverValidationServiceGrpcKt.DriverValidationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DriverValidationEndpoint::class.java)

    private val invokeExplainDriverDecision1: suspend (CheckMetricSeriesEligibilityRequest) -> CheckMetricSeriesEligibilityResponse = { payload ->
        metricsMetricSeriesValidationService.checkMetricSeriesEligibility(payload)
    }

    private val workerForNormalizeDriver1 = ReplyMailbox<EstimateDriverLicenseRequest, EstimateDriverLicenseResponse>(workers) { payload ->
        withTimeout(5_000) { driversDriverLicenseCalculationService.estimateDriverLicense(payload) }
    }

    private val clientForPreviewDriver0 by lazy { dispatchDispatchAssignmentCalculationService }

    /** Checks a driver. */
    override suspend fun checkDriverEligibility(request: CheckDriverEligibilityRequest): CheckDriverEligibilityResponse =
        RpcBoundary.run("acme.drivers.v1.DriverValidationService/CheckDriverEligibility") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CheckDriverEligibility", request)
            log.atInfo().addKeyValue("operation", "CheckDriverEligibility").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driver.driverId).log("Recorded audit detail")
            val outbound0 = CheckDriverEligibility0Command(
                context = request.context,
                driverLicenseAuditEtag = request.driver.audit.etag
            ).toRequest()
            val reply0 = driversDriverLicenseCalculationService.explainDriverLicenseCalculation(outbound0)
            val outbound1 = normalizeMetricSeriesRequest {
                this.context = request.context
                this.details = NormalizeMetricSeriesRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.driver.contact.email
                    }
                }
                this.metricSeries = metricSeries {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.driverLicense.audit.updatedAt
                    }
                }
            }
            val command1: Submission<NormalizeMetricSeriesRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = metricsMetricSeriesValidationService.normalizeMetricSeries(payload1)
            checkDriverEligibilityResponse {
                this.driver = driver {
                    this.audit = auditInfo {
                        this.createdBy = reply0.driverLicense.audit.createdBy
                    }
                    this.contact = contactInfo {
                        this.name = request.driver.name
                    }
                    this.driverId = request.driverId
                    this.primaryAddress = postalAddress {
                        this.lines.addAll(reply1.metricSeries.address.linesList)
                    }
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Explains a driver. */
    override suspend fun explainDriverDecision(request: ExplainDriverDecisionRequest): ExplainDriverDecisionResponse {
        return handleExplainDriverDecision(request)
    }

    /** Returns a driver. */
    override suspend fun getDriverRules(request: GetDriverRulesRequest): GetDriverRulesResponse = handleGetDriverRules(request)

    /** Lints a driver. */
    override suspend fun lintDrivers(request: LintDriversRequest): LintDriversResponse =
        RpcBoundary.run("acme.drivers.v1.DriverValidationService/LintDrivers") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LintDrivers", request)
            log.atInfo().addKeyValue("operation", "LintDrivers").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driver.driverId).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                quoteDriverLicenseRequest {
                    this.context = request.context
                    this.driverLicense = driverLicense {
                        this.audit = auditInfo {
                            this.createdAt = request.driver.audit.createdAt
                        }
                    }
                }
            }
            val reply0 = driversDriverLicenseCalculationService.quoteDriverLicense(outbound0)
            val outbound1 = calculateDriverRequest {
                this.context = request.context
                this.details = CalculateDriverRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = request.driver.primaryAddress.countryCode
                    }
                }
                this.driver = driver {
                    this.contact = contactInfo {
                        this.preferredChannel = request.details.contact.preferredChannel
                    }
                    this.passport = driverPassport {
                        this.issuingCountry = request.driver.passport.issuingCountry
                    }
                }
            }
            val reply1 = ClientRegistry.lookup("acme.drivers.v1.DriverCalculationService", DriverCalculationServiceGrpcKt.DriverCalculationServiceCoroutineStub::class.java).calculateDriver(outbound1)
            lintDriversResponse {
                this.driver = driver {
                    this.contact = contactInfo {
                        this.email = reply0.driverLicense.counterparty.email
                        this.name = request.driver.name
                    }
                    this.driverId = request.driverId
                    this.relationshipManager = userRef {
                        this.email = reply0.driverLicense.counterparty.email
                    }
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Normalizes a driver. */
    override suspend fun normalizeDriver(request: NormalizeDriverRequest): NormalizeDriverResponse {
        return handleNormalizeDriver(request)
    }

    /** Previews a driver. */
    override suspend fun previewDriver(request: PreviewDriverRequest): PreviewDriverResponse = handlePreviewDriver(request)

    /** Scores a driver. */
    override suspend fun scoreDriver(request: ScoreDriverRequest): ScoreDriverResponse =
        RpcBoundary.run("acme.drivers.v1.DriverValidationService/ScoreDriver") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScoreDriver", request)
            log.atInfo().addKeyValue("operation", "ScoreDriver").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driver.driverId).log("Recorded audit detail")
            val outbound0 = resolveServiceSchedulesRequest {
                this.context = request.context
            }
            val reply0 = withTimeout(5_000) { maintenanceServiceScheduleQueryService.resolveServiceSchedules(outbound0) }
            scoreDriverResponse {
                this.driver = driver {
                    this.driverId = request.driverId
                    this.primaryAddress = request.driver.primaryAddress
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    /** Validates a driver. */
    override suspend fun validateDriver(request: ValidateDriverRequest): ValidateDriverResponse {
        return handleValidateDriver(request)
    }

    private data class CheckDriverEligibility0Command(
        val context: RequestContext,
        val driverLicenseAuditEtag: String
    ) {
        fun toRequest(): ExplainDriverLicenseCalculationRequest {
            val command = this
            return explainDriverLicenseCalculationRequest {
                this.context = command.context
                this.driverLicense = driverLicense {
                    this.audit = auditInfo {
                        this.etag = command.driverLicenseAuditEtag
                    }
                }
            }
        }
    }

    private suspend fun handleExplainDriverDecision(request: ExplainDriverDecisionRequest): ExplainDriverDecisionResponse =
        RpcBoundary.run("acme.drivers.v1.DriverValidationService/ExplainDriverDecision") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainDriverDecision", request)
            log.atInfo().addKeyValue("operation", "ExplainDriverDecision").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driver.driverId).log("Recorded audit detail")
            val outbound0 = validateAuditEventRequest {
                this.auditEvent = auditEvent {
                    this.subject = auditTrailRef {
                        this.displayName = request.driver.relationshipManager.displayName
                        this.number = request.driver.passport.number
                    }
                }
                this.context = request.context
                this.details = ValidateAuditEventRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.driver.relationshipManager.email
                        this.preferredChannel = request.driver.contact.preferredChannel
                    }
                }
            }
            val adapter0 = OutboundPort<ValidateAuditEventRequest, ValidateAuditEventResponse> { payload ->
                auditAuditEventValidationService.validateAuditEvent(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = checkMetricSeriesEligibilityRequest {
                this.context = request.context
                this.details = CheckMetricSeriesEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.phone = request.driver.contact.phone
                    }
                }
                this.metricSeries = metricSeries {
                    this.audit = auditInfo {
                        this.etag = request.driver.audit.etag
                    }
                }
            }
            val reply1 = invokeExplainDriverDecision1(outbound1)
            val outbound2 = quoteDriverRequest {
                this.context = request.context
                this.details = QuoteDriverRequestKt.details {
                    this.destination = postalAddress {
                        this.recipient = reply1.metricSeries.address.recipient
                    }
                }
                this.driver = driver {
                    this.contact = contactInfo {
                        this.email = request.driver.contact.email
                    }
                    this.passport = driverPassport {
                        this.number = request.driver.passport.number
                    }
                    this.primaryAddress = postalAddress {
                        this.lines.addAll(reply1.metricSeries.address.linesList)
                    }
                }
            }
            val invoke2: suspend (QuoteDriverRequest, Metadata) -> QuoteDriverResponse = driversDriverCalculationService::quoteDriver
            val reply2 = invoke2(outbound2, Metadata())
            val outbound3 = BatchGetDocumentsRequest.newBuilder().apply {
                setContext(request.context)
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply3 = outbound3.let { value -> documentsDocumentService.batchGetDocuments(value) }
            explainDriverDecisionResponse {
                this.driver = driver {
                    this.audit = auditInfo {
                        this.updatedBy = reply2.driver.audit.updatedBy
                    }
                    this.driverId = request.driverId
                    this.name = request.driver.name
                }
                this.outcome = ExplainDriverDecisionResponseKt.outcome {
                    this.referenceId = reply1.outcome.referenceId
                    this.status = reply3.result.status
                }
                this.result = operationResult {
                    this.correlationId = reply0.auditEvent.correlationId
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = reply3.result.status
                }
            }
        }

    private suspend fun handleGetDriverRules(request: GetDriverRulesRequest): GetDriverRulesResponse =
        RpcBoundary.run("acme.drivers.v1.DriverValidationService/GetDriverRules") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetDriverRules", request)
            log.atInfo().addKeyValue("operation", "GetDriverRules").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            getDriverRulesResponse {
                this.driver = driver {
                    this.driverId = request.driverId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleNormalizeDriver(request: NormalizeDriverRequest): NormalizeDriverResponse =
        RpcBoundary.run("acme.drivers.v1.DriverValidationService/NormalizeDriver") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "NormalizeDriver", request)
            log.atInfo().addKeyValue("operation", "NormalizeDriver").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driver.contact.phone).log("Recorded audit detail")
            val outbound0 = countRoutesRequest {
                this.context = request.context
                this.filter = CountRoutesRequestKt.filter {
                    this.createdAfter = request.driver.passport.expiresAt
                    this.ownerId = request.driver.driverId
                    this.region = request.driver.primaryAddress.region
                }
            }
            val reply0 = routingRouteQueryService.countRoutes(WireCopies.reflective(outbound0))
            val outbound1 = estimateDriverLicenseRequest {
                this.context = request.context
                this.details = EstimateDriverLicenseRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = request.driver.primaryAddress.countryCode
                        this.lines.addAll(request.driver.primaryAddress.linesList)
                    }
                }
                this.driverLicense = driverLicense {
                    this.audit = auditInfo {
                        this.updatedAt = request.driver.audit.updatedAt
                    }
                    this.counterparty = driverRef {
                        this.email = request.driver.contact.email
                    }
                }
            }
            val reply1 = workerForNormalizeDriver1.submit(outbound1)
            normalizeDriverResponse {
                this.driver = driver {
                    this.contact = request.details.contact
                    this.driverId = request.driverId
                    this.primaryAddress = postalAddress {
                        this.postalCode = reply1.driverLicense.billingAddress.postalCode
                    }
                }
                this.outcome = NormalizeDriverResponseKt.outcome {
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
            }
        }

    private suspend fun handlePreviewDriver(request: PreviewDriverRequest): PreviewDriverResponse =
        RpcBoundary.run("acme.drivers.v1.DriverValidationService/PreviewDriver") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PreviewDriver", request)
            log.atInfo().addKeyValue("operation", "PreviewDriver").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driver.driverId).log("Recorded audit detail")
            val outbound0 = calculateDispatchAssignmentRequest {
                this.context = request.context
                this.dispatchAssignment = dispatchAssignment {
                    this.requester = contactInfo {
                        this.email = request.driver.relationshipManager.email
                        this.phone = request.driver.contact.phone
                    }
                }
            }
            val reply0 = clientForPreviewDriver0.calculateDispatchAssignment(outbound0)
            val outbound1 = findVenuesRequest {
                this.context = request.context
                this.filter = FindVenuesRequestKt.filter {
                    this.createdAfter = reply0.dispatchAssignment.sla.dueAt
                    this.ownerId = reply0.dispatchAssignment.customer.driverId
                    this.region = request.driver.primaryAddress.region
                }
            }
            val reply1 = flowOf(outbound1).map { value -> venuesVenueQueryService.findVenues(value) }.single()
            previewDriverResponse {
                this.driver = driver {
                    this.contact = contactInfo {
                        this.name = reply0.dispatchAssignment.requester.name
                    }
                    this.driverId = request.driverId
                    this.relationshipManager = request.driver.relationshipManager
                }
                this.outcome = PreviewDriverResponseKt.outcome {
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleValidateDriver(request: ValidateDriverRequest): ValidateDriverResponse =
        RpcBoundary.run("acme.drivers.v1.DriverValidationService/ValidateDriver") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ValidateDriver", request)
            log.atInfo().addKeyValue("operation", "ValidateDriver").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverId).log("Recorded audit detail")
            val configure0: CreateAccessPolicyRequestKt.Dsl.() -> Unit = {
                this.accessPolicy = accessPolicy {
                    this.audit = auditInfo {
                        this.updatedAt = request.driver.audit.updatedAt
                    }
                }
                this.context = request.context
            }
            val outbound0 = createAccessPolicyRequest(configure0)
            val reply0 = authorizationAccessPolicyService.createAccessPolicy(outbound0)
            val outbound1 = suggestTelemetryReadingsRequest {
                this.context = request.context
                this.filter = SuggestTelemetryReadingsRequestKt.filter {
                    this.createdAfter = request.driver.passport.expiresAt
                    this.labels.putAll(reply0.result.detailsMap)
                    this.ownerId = request.driver.driverId
                    this.region = request.driver.primaryAddress.region
                }
            }
            val reply1 = telematicsTelemetryReadingQueryService.suggestTelemetryReadings(outbound1)
            val outbound2 = quoteDriverLicenseRequest {
                this.context = request.context
                this.driverLicense = driverLicense {
                    this.billingAddress = postalAddress {
                        this.postalCode = request.driver.primaryAddress.postalCode
                    }
                    this.lines.addAll(listOf(driverLicenseLine {
                        this.tax = driverLicenseLineTax {
                            this.amount = reply0.accessPolicy.discount.amount
                        }
                    }))
                }
            }
            val reply2 = driversDriverLicenseCalculationService.quoteDriverLicense(outbound2)
            validateDriverResponse {
                this.driver = driver {
                    this.driverId = reply2.driverLicense.counterparty.driverId
                    this.name = request.driver.name
                    this.relationshipManager = request.driver.relationshipManager
                }
                this.outcome = ValidateDriverResponseKt.outcome {
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.message = reply1.result.message
                    this.status = reply2.result.status
                }
            }
        }
}
