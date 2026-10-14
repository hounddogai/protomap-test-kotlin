package com.acme.transport.drivers.api

import com.acme.certificates.v1.CertificateServiceGrpcKt
import com.acme.certificates.v1.CreateCertificateResponse
import com.acme.certificates.v1.SearchCertificatesRequestKt
import com.acme.certificates.v1.certificate
import com.acme.certificates.v1.certificateAttachment
import com.acme.certificates.v1.createCertificateRequest
import com.acme.certificates.v1.searchCertificatesRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.dispatch.v1.CheckDispatchOrderEligibilityRequestKt
import com.acme.dispatch.v1.DispatchAssignmentCalculationServiceGrpcKt
import com.acme.dispatch.v1.DispatchOrderValidationServiceGrpcKt
import com.acme.dispatch.v1.ExplainDispatchAssignmentCalculationRequestKt
import com.acme.dispatch.v1.checkDispatchOrderEligibilityRequest
import com.acme.dispatch.v1.copy as dispatchCopy
import com.acme.dispatch.v1.dispatchAssignment
import com.acme.dispatch.v1.dispatchAssignmentComment
import com.acme.dispatch.v1.dispatchOrder
import com.acme.dispatch.v1.dispatchOrderCardSettlement
import com.acme.dispatch.v1.explainDispatchAssignmentCalculationRequest
import com.acme.drivers.v1.CalculateDriverRequest
import com.acme.drivers.v1.CalculateDriverRequestKt
import com.acme.drivers.v1.CalculateDriverResponse
import com.acme.drivers.v1.CheckDriverLicenseEligibilityRequest
import com.acme.drivers.v1.CheckDriverLicenseEligibilityResponse
import com.acme.drivers.v1.DriverCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverLicense
import com.acme.drivers.v1.DriverLicenseValidationServiceGrpcKt
import com.acme.drivers.v1.DutyStatusCalculationServiceGrpcKt
import com.acme.drivers.v1.EstimateDriverRequestKt
import com.acme.drivers.v1.EstimateDriverResponse
import com.acme.drivers.v1.ExplainDriverLicenseDecisionRequest
import com.acme.drivers.v1.ExplainDriverLicenseDecisionResponse
import com.acme.drivers.v1.GetDriverLicenseRulesRequest
import com.acme.drivers.v1.GetDriverLicenseRulesResponse
import com.acme.drivers.v1.LintDriverLicensesRequest
import com.acme.drivers.v1.LintDriverLicensesResponse
import com.acme.drivers.v1.NormalizeDriverLicenseRequest
import com.acme.drivers.v1.NormalizeDriverLicenseResponse
import com.acme.drivers.v1.PreviewDriverLicenseRequest
import com.acme.drivers.v1.PreviewDriverLicenseResponse
import com.acme.drivers.v1.RecalculateDutyStatusesRequestKt
import com.acme.drivers.v1.ScoreDriverLicenseRequest
import com.acme.drivers.v1.ScoreDriverLicenseResponse
import com.acme.drivers.v1.SimulateDriverRequestKt
import com.acme.drivers.v1.ValidateDriverLicenseRequest
import com.acme.drivers.v1.ValidateDriverLicenseResponse
import com.acme.drivers.v1.ValidateDriverLicenseResponseKt
import com.acme.drivers.v1.calculateDriverRequest
import com.acme.drivers.v1.checkDriverLicenseEligibilityResponse
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverLicenseLine
import com.acme.drivers.v1.driverLicenseRef
import com.acme.drivers.v1.driverPassport
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.dutyStatus
import com.acme.drivers.v1.estimateDriverRequest
import com.acme.drivers.v1.explainDriverCalculationRequest
import com.acme.drivers.v1.explainDriverLicenseDecisionResponse
import com.acme.drivers.v1.getDriverLicenseRulesResponse
import com.acme.drivers.v1.lintDriverLicensesResponse
import com.acme.drivers.v1.normalizeDriverLicenseResponse
import com.acme.drivers.v1.previewDriverLicenseResponse
import com.acme.drivers.v1.quoteDriverRequest
import com.acme.drivers.v1.recalculateDutyStatusesRequest
import com.acme.drivers.v1.scoreDriverLicenseResponse
import com.acme.drivers.v1.simulateDriverRequest
import com.acme.drivers.v1.validateDriverLicenseResponse
import com.acme.identity.v1.SuggestUsersRequestKt
import com.acme.identity.v1.UserQueryServiceGrpcKt
import com.acme.identity.v1.suggestUsersRequest
import com.acme.identity.v1.userRef
import com.acme.metrics.v1.CloneMetricSeriesRequestKt
import com.acme.metrics.v1.MetricSeriesServiceGrpcKt
import com.acme.metrics.v1.cloneMetricSeriesRequest
import com.acme.metrics.v1.metricSeries
import com.acme.sponsorships.v1.SponsorServiceGrpcKt
import com.acme.sponsorships.v1.UpdateSponsorRequest
import com.acme.sponsorships.v1.sponsor
import com.acme.sponsorships.v1.sponsorNationalId
import com.acme.sponsorships.v1.updateSponsorRequest
import com.acme.telematics.v1.ExplainTelemetryReadingDecisionRequest
import com.acme.telematics.v1.ExplainTelemetryReadingDecisionRequestKt
import com.acme.telematics.v1.GeoFenceCalculationServiceGrpcKt
import com.acme.telematics.v1.QuoteGeoFenceRequest
import com.acme.telematics.v1.QuoteGeoFenceRequestKt
import com.acme.telematics.v1.TelemetryReadingValidationServiceGrpcKt
import com.acme.telematics.v1.explainTelemetryReadingDecisionRequest
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.quoteGeoFenceRequest
import com.acme.transport.drivers.DriversRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.venues.v1.SuggestVenuesRequest
import com.acme.venues.v1.SuggestVenuesRequestKt
import com.acme.venues.v1.VenueQueryServiceGrpcKt
import com.acme.venues.v1.suggestVenuesRequest
import com.google.protobuf.Any as ProtoAny
import com.google.protobuf.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Checks driver licenses against the rules before anything acts on them. */
class DriverLicenseValidationGrpcService(
    private val store: DriversRepository,
    private val workers: CoroutineScope,
    private val certificatesCertificateService: CertificateServiceGrpcKt.CertificateServiceCoroutineStub,
    private val dispatchDispatchAssignmentCalculationService: DispatchAssignmentCalculationServiceGrpcKt.DispatchAssignmentCalculationServiceCoroutineStub,
    private val dispatchDispatchOrderValidationService: DispatchOrderValidationServiceGrpcKt.DispatchOrderValidationServiceCoroutineStub,
    private val driversDriverCalculationService: DriverCalculationServiceGrpcKt.DriverCalculationServiceCoroutineStub,
    private val driversDutyStatusCalculationService: DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineStub,
    private val identityUserQueryService: UserQueryServiceGrpcKt.UserQueryServiceCoroutineStub,
    private val metricsMetricSeriesService: MetricSeriesServiceGrpcKt.MetricSeriesServiceCoroutineStub,
    private val sponsorshipsSponsorService: SponsorServiceGrpcKt.SponsorServiceCoroutineStub,
    private val telematicsGeoFenceCalculationService: GeoFenceCalculationServiceGrpcKt.GeoFenceCalculationServiceCoroutineStub,
    private val telematicsTelemetryReadingValidationService: TelemetryReadingValidationServiceGrpcKt.TelemetryReadingValidationServiceCoroutineStub,
    private val venuesVenueQueryService: VenueQueryServiceGrpcKt.VenueQueryServiceCoroutineStub,
) : DriverLicenseValidationServiceGrpcKt.DriverLicenseValidationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DriverLicenseValidationGrpcService::class.java)

    private val workerForGetDriverLicenseRules1 = ReplyMailbox<CalculateDriverRequest, CalculateDriverResponse>(workers) { payload ->
        withTimeout(5_000) { driversDriverCalculationService.calculateDriver(payload) }
    }

    private val clientForLintDriverLicenses0 by lazy { driversDriverCalculationService }

    /** Checks a driver license. */
    override suspend fun checkDriverLicenseEligibility(request: CheckDriverLicenseEligibilityRequest): CheckDriverLicenseEligibilityResponse {
        return handleCheckDriverLicenseEligibility(request)
    }

    /** Explains a driver license. */
    override suspend fun explainDriverLicenseDecision(request: ExplainDriverLicenseDecisionRequest): ExplainDriverLicenseDecisionResponse = handleExplainDriverLicenseDecision(request)

    /** Returns a driver license. */
    override suspend fun getDriverLicenseRules(request: GetDriverLicenseRulesRequest): GetDriverLicenseRulesResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseValidationService/GetDriverLicenseRules") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetDriverLicenseRules", request)
            log.atInfo().addKeyValue("operation", "GetDriverLicenseRules").log("Handling transport request")
            val outbound0 = simulateDriverRequest {
                this.context = request.context
                this.driver = driver {
                    this.relationshipManager = userRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = driversDriverCalculationService.simulateDriver(WireCopies.reflective(outbound0))
            val outbound1 = calculateDriverRequest {
                this.context = request.context
                this.details = CalculateDriverRequestKt.details {
                    this.destination = postalAddress {
                        this.postalCode = reply0.driver.primaryAddress.postalCode
                    }
                }
                this.driver = driver {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.driver.audit.updatedBy
                    }
                    this.passport = driverPassport {
                        this.expiresAt = reply0.driver.passport.expiresAt
                    }
                    this.relationshipManager = userRef {
                        this.displayName = reply0.driver.relationshipManager.displayName
                    }
                }
            }
            val reply1 = workerForGetDriverLicenseRules1.submit(outbound1)
            getDriverLicenseRulesResponse {
                this.driverLicense = driverLicense {
                    this.audit = auditInfo {
                        this.createdAt = reply1.driver.audit.createdAt
                    }
                    this.counterparty = driverRef {
                        this.displayName = reply0.driver.relationshipManager.displayName
                    }
                    this.driverLicenseId = request.driverLicenseId
                    this.number = reply1.driver.passport.number
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Lints a driver license. */
    override suspend fun lintDriverLicenses(request: LintDriverLicensesRequest): LintDriverLicensesResponse {
        return handleLintDriverLicenses(request)
    }

    /** Normalizes a driver license. */
    override suspend fun normalizeDriverLicense(request: NormalizeDriverLicenseRequest): NormalizeDriverLicenseResponse = handleNormalizeDriverLicense(request)

    /** Previews a driver license. */
    override suspend fun previewDriverLicense(request: PreviewDriverLicenseRequest): PreviewDriverLicenseResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseValidationService/PreviewDriverLicense") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PreviewDriverLicense", request)
            log.atInfo().addKeyValue("operation", "PreviewDriverLicense").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverLicense.driverLicenseId).log("Recorded audit detail")
            val outbound0 = simulateDriverRequest {
                this.context = request.context
                this.details = SimulateDriverRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = request.driverLicense.billingAddress.countryCode
                    }
                }
                this.driver = driver {
                    this.audit = auditInfo {
                        this.createdAt = request.driverLicense.audit.createdAt
                        this.createdBy = request.driverLicense.audit.createdBy
                    }
                }
            }
            val reply0 = driversDriverCalculationService.simulateDriver(outbound0)
            val outbound1 = estimateDriverRequest {
                this.context = request.context
                this.details = EstimateDriverRequestKt.details {
                    this.destination = postalAddress {
                        this.locality = request.driverLicense.billingAddress.locality
                    }
                }
            }
            val reply1 = driversDriverCalculationService.estimateDriver(outbound1)
            val outbound2 = explainDispatchAssignmentCalculationRequest {}.dispatchCopy {
                this.context = request.context
                this.details = ExplainDispatchAssignmentCalculationRequestKt.details {
                    this.contact = contactInfo {
                        this.name = reply0.driver.contact.name
                    }
                }
                this.dispatchAssignment = dispatchAssignment {
                    this.assignee = userRef {
                        this.displayName = request.driverLicense.counterparty.displayName
                    }
                    this.comments.addAll(listOf(dispatchAssignmentComment {
                        this.createdAt = reply0.driver.audit.createdAt
                    }))
                }
            }
            val reply2 = dispatchDispatchAssignmentCalculationService.explainDispatchAssignmentCalculation(outbound2)
            val outbound3 = request.toExplainTelemetryReadingDecisionFromPreviewDriverLicense3(reply1)
            val reply3 = telematicsTelemetryReadingValidationService.explainTelemetryReadingDecision(outbound3)
            previewDriverLicenseResponse {
                this.driverLicense = driverLicense {
                    this.audit = auditInfo {
                        this.createdAt = reply1.driver.audit.createdAt
                        this.updatedBy = reply2.dispatchAssignment.audit.updatedBy
                    }
                    this.counterparty = driverRef {
                        this.email = reply0.driver.contact.email
                    }
                    this.driverLicenseId = request.driverLicenseId
                    this.metadata.putAll(request.driverLicense.metadataMap)
                }
                this.result = operationResult {
                    this.message = reply3.result.message
                    this.status = reply3.result.status
                }
            }
        }

    /** Scores a driver license. */
    override suspend fun scoreDriverLicense(request: ScoreDriverLicenseRequest): ScoreDriverLicenseResponse {
        return handleScoreDriverLicense(request)
    }

    /** Validates a driver license. */
    override suspend fun validateDriverLicense(request: ValidateDriverLicenseRequest): ValidateDriverLicenseResponse = handleValidateDriverLicense(request)

    private suspend fun handleCheckDriverLicenseEligibility(request: CheckDriverLicenseEligibilityRequest): CheckDriverLicenseEligibilityResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseValidationService/CheckDriverLicenseEligibility") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CheckDriverLicenseEligibility", request)
            log.atInfo().addKeyValue("operation", "CheckDriverLicenseEligibility").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverLicense.driverLicenseId).log("Recorded audit detail")
            val outbound0 = UpdateSponsorRequest.newBuilder().apply {
                setContext(request.context)
                setSponsor(sponsor {
                    this.audit = auditInfo {
                        this.etag = request.driverLicense.audit.etag
                    }
                    this.contact = contactInfo {
                        this.phone = request.details.contact.phone
                        this.preferredChannel = request.details.contact.preferredChannel
                    }
                    this.nationalId = sponsorNationalId {
                        this.number = request.driverLicense.number
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> sponsorshipsSponsorService.updateSponsor(value) }
            val outbound1 = checkDispatchOrderEligibilityRequest {
                this.context = request.context
                this.details = CheckDispatchOrderEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.details.contact.email
                    }
                }
                this.dispatchOrder = dispatchOrder {
                    this.card = dispatchOrderCardSettlement {
                        this.network = when (request.driverLicense.settlementCase) {
                            DriverLicense.SettlementCase.CARD -> request.driverLicense.card.network
                            else -> ""
                        }
                    }
                }
            }
            val reply1 = dispatchDispatchOrderValidationService.checkDispatchOrderEligibility(outbound1)
            checkDriverLicenseEligibilityResponse {
                this.driverLicense = driverLicense {
                    this.audit = auditInfo {
                        this.etag = reply1.dispatchOrder.audit.etag
                    }
                    this.billingAddress = postalAddress {
                        this.lines.addAll(reply0.sponsor.primaryAddress.linesList)
                    }
                    this.driverLicenseId = request.driverLicenseId
                    this.number = request.driverLicense.number
                }
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleExplainDriverLicenseDecision(request: ExplainDriverLicenseDecisionRequest): ExplainDriverLicenseDecisionResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseValidationService/ExplainDriverLicenseDecision") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainDriverLicenseDecision", request)
            log.atInfo().addKeyValue("operation", "ExplainDriverLicenseDecision").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverLicense.driverLicenseId).log("Recorded audit detail")
            val outbound0 = searchCertificatesRequest {
                this.context = request.context
                this.filter = SearchCertificatesRequestKt.filter {
                    this.region = request.driverLicense.billingAddress.region
                }
            }
            val reply0 = ClientRegistry.lookup("acme.certificates.v1.CertificateService", CertificateServiceGrpcKt.CertificateServiceCoroutineStub::class.java).searchCertificates(outbound0)
            explainDriverLicenseDecisionResponse {
                this.driverLicense = driverLicense {
                    this.driverLicenseId = request.driverLicenseId
                    this.status = request.driverLicense.status
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleLintDriverLicenses(request: LintDriverLicensesRequest): LintDriverLicensesResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseValidationService/LintDriverLicenses") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LintDriverLicenses", request)
            log.atInfo().addKeyValue("operation", "LintDriverLicenses").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverLicense.driverLicenseId).log("Recorded audit detail")
            val outbound0 = quoteDriverRequest {
                this.context = request.context
                this.driver = driver {
                    this.audit = auditInfo {
                        this.updatedAt = request.driverLicense.audit.updatedAt
                    }
                    this.primaryAddress = postalAddress {
                        this.postalCode = request.driverLicense.billingAddress.postalCode
                    }
                    this.relationshipManager = userRef {
                        this.displayName = request.driverLicense.counterparty.displayName
                        this.email = request.driverLicense.counterparty.email
                    }
                }
            }
            val reply0 = clientForLintDriverLicenses0.quoteDriver(outbound0)
            var reply1 = CreateCertificateResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.driverLicense.attachmentsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound1 = createCertificateRequest {
                                this.certificate = certificate {
                                    this.attachments.addAll(listOf(certificateAttachment {
                                        this.checksum = item.checksum
                                        this.fileName = item.fileName
                                        this.sizeBytes = item.sizeBytes
                                    }))
                                }
                                this.context = request.context
                            }
                            certificatesCertificateService.createCertificate(outbound1)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply1 = it }
                }
            }
            lintDriverLicensesResponse {
                this.driverLicense = driverLicense {
                    this.billingAddress = postalAddress {
                        this.lines.addAll(reply0.driver.primaryAddress.linesList)
                    }
                    this.driverLicenseId = request.driverLicenseId
                    this.number = request.driverLicense.number
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleNormalizeDriverLicense(request: NormalizeDriverLicenseRequest): NormalizeDriverLicenseResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseValidationService/NormalizeDriverLicense") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "NormalizeDriverLicense", request)
            log.atInfo().addKeyValue("operation", "NormalizeDriverLicense").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverLicense.driverLicenseId).log("Recorded audit detail")
            val outbound0 = recalculateDutyStatusesRequest {
                this.context = request.context
                this.details = RecalculateDutyStatusesRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(request.driverLicense.billingAddress.linesList)
                        this.postalCode = request.driverLicense.billingAddress.postalCode
                    }
                }
                this.dutyStatus = dutyStatus {
                    this.subject = driverLicenseRef {
                        this.number = request.driverLicense.number
                    }
                }
            }
            val reply0 = withTimeout(5_000) { driversDutyStatusCalculationService.recalculateDutyStatuses(outbound0) }
            val configure1: CloneMetricSeriesRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.metricSeries = metricSeries {
                    this.owner = userRef {
                        this.email = request.details.contact.email
                    }
                }
            }
            val outbound1 = cloneMetricSeriesRequest(configure1)
            val reply1 = metricsMetricSeriesService.cloneMetricSeries(outbound1)
            normalizeDriverLicenseResponse {
                this.driverLicense = driverLicense {
                    this.billingAddress = postalAddress {
                        this.locality = reply1.metricSeries.address.locality
                        this.postalCode = reply1.metricSeries.address.postalCode
                    }
                    this.counterparty = driverRef {
                        this.displayName = reply0.dutyStatus.subject.displayName
                    }
                    this.driverLicenseId = request.driverLicenseId
                    this.notes = request.driverLicense.notes
                }
                this.result = operationResult {
                    this.correlationId = reply0.dutyStatus.correlationId
                    this.status = "ok"
                }
            }
        }

    private fun PreviewDriverLicenseRequest.toExplainTelemetryReadingDecisionFromPreviewDriverLicense3(reply1: EstimateDriverResponse): ExplainTelemetryReadingDecisionRequest {
        val source = this
        return explainTelemetryReadingDecisionRequest {
            this.context = source.context
            this.details = ExplainTelemetryReadingDecisionRequestKt.details {
                this.contact = contactInfo {
                    this.email = source.details.contact.email
                    this.name = reply1.driver.contact.name
                }
            }
        }
    }

    private suspend fun handleScoreDriverLicense(request: ScoreDriverLicenseRequest): ScoreDriverLicenseResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseValidationService/ScoreDriverLicense") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScoreDriverLicense", request)
            log.atInfo().addKeyValue("operation", "ScoreDriverLicense").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverLicense.driverLicenseId).log("Recorded audit detail")
            val outbound0 = explainDriverCalculationRequest {
                this.context = request.context
                this.driver = driver {
                    this.audit = auditInfo {
                        this.createdBy = request.driverLicense.audit.createdBy
                    }
                    this.primaryAddress = postalAddress {
                        this.recipient = request.driverLicense.billingAddress.recipient
                    }
                    this.relationshipManager = userRef {
                        this.email = request.driverLicense.counterparty.email
                    }
                }
            }
            val reply0 = driversDriverCalculationService.explainDriverCalculation(outbound0)
            val outbound1 = quoteGeoFenceRequest {
                this.context = request.context
                this.details = QuoteGeoFenceRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(request.driverLicense.billingAddress.linesList)
                        this.postalCode = request.details.destination.postalCode
                    }
                }
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.createdAt = request.driverLicense.audit.createdAt
                        this.createdBy = request.driverLicense.audit.createdBy
                    }
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = telematicsGeoFenceCalculationService.quoteGeoFence(envelope1.unpack(QuoteGeoFenceRequest::class.java))
            scoreDriverLicenseResponse {
                this.driverLicense = driverLicense {
                    this.counterparty = driverRef {
                        this.email = reply1.geoFence.owner.email
                    }
                    this.driverLicenseId = request.driverLicenseId
                    this.issuedAt = request.driverLicense.issuedAt
                    this.lines.addAll(listOf(driverLicenseLine {
                        this.attributes.putAll(reply0.driver.attributesMap)
                    }))
                    this.number = reply0.driver.nationalId.number
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private data class ValidateDriverLicense1Command(
        val context: RequestContext,
        val filterRegion: String,
        val filterCreatedAfter: Timestamp
    ) {
        fun toRequest(): SuggestVenuesRequest {
            val command = this
            return suggestVenuesRequest {
                this.context = command.context
                this.filter = SuggestVenuesRequestKt.filter {
                    this.createdAfter = command.filterCreatedAfter
                    this.region = command.filterRegion
                }
            }
        }
    }

    private suspend fun handleValidateDriverLicense(request: ValidateDriverLicenseRequest): ValidateDriverLicenseResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseValidationService/ValidateDriverLicense") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ValidateDriverLicense", request)
            log.atInfo().addKeyValue("operation", "ValidateDriverLicense").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverLicense.driverLicenseId).log("Recorded audit detail")
            val outbound0 = suggestUsersRequest {
                this.context = request.context
                this.filter = SuggestUsersRequestKt.filter {
                    this.createdAfter = request.driverLicense.audit.updatedAt
                    this.region = request.driverLicense.billingAddress.region
                }
            }
            val reply0 = identityUserQueryService.suggestUsers(WireCopies.json(outbound0))
            val outbound1 = ValidateDriverLicense1Command(
                context = request.context,
                filterRegion = request.driverLicense.billingAddress.region,
                filterCreatedAfter = request.driverLicense.dueAt
            ).toRequest()
            val reply1 = venuesVenueQueryService.suggestVenues(outbound1)
            validateDriverLicenseResponse {
                this.driverLicense = driverLicense {
                    this.driverLicenseId = request.driverLicenseId
                    this.issuedAt = request.driverLicense.issuedAt
                }
                this.outcome = ValidateDriverLicenseResponseKt.outcome {
                    this.message = reply0.result.message
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = "ok"
                }
            }
        }
}
