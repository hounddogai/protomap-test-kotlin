package com.acme.transport.drivers.grpc

import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.drivers.v1.CalculateDriverLicenseResponse
import com.acme.drivers.v1.DriverCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseValidationServiceGrpcKt
import com.acme.drivers.v1.DriverValidationServiceGrpcKt
import com.acme.drivers.v1.DutyStatusMeasurement
import com.acme.drivers.v1.DutyStatusValidationServiceGrpcKt
import com.acme.drivers.v1.EstimateDriverLicenseRequest
import com.acme.drivers.v1.ExplainDriverLicenseDecisionRequestKt
import com.acme.drivers.v1.ExplainDutyStatusDecisionRequest
import com.acme.drivers.v1.ExplainDutyStatusDecisionResponse
import com.acme.drivers.v1.GetDutyStatusRulesRequest
import com.acme.drivers.v1.GetDutyStatusRulesResponse
import com.acme.drivers.v1.NormalizeDriverRequest
import com.acme.drivers.v1.NormalizeDriverRequestKt
import com.acme.drivers.v1.NormalizeDriverResponse
import com.acme.drivers.v1.NormalizeDutyStatusRequest
import com.acme.drivers.v1.NormalizeDutyStatusResponse
import com.acme.drivers.v1.NormalizeDutyStatusResponseKt
import com.acme.drivers.v1.PreviewDutyStatusRequest
import com.acme.drivers.v1.PreviewDutyStatusResponse
import com.acme.drivers.v1.PreviewDutyStatusResponseKt
import com.acme.drivers.v1.QuoteDriverResponse
import com.acme.drivers.v1.ScoreDutyStatusRequest
import com.acme.drivers.v1.ScoreDutyStatusResponse
import com.acme.drivers.v1.ScoreDutyStatusResponseKt
import com.acme.drivers.v1.SimulateDriverLicenseRequest
import com.acme.drivers.v1.SimulateDriverLicenseResponse
import com.acme.drivers.v1.ValidateDutyStatusRequest
import com.acme.drivers.v1.ValidateDutyStatusResponse
import com.acme.drivers.v1.calculateDriverLicenseRequest
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverLicenseLine
import com.acme.drivers.v1.driverLicenseRef
import com.acme.drivers.v1.driverPassport
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.dutyStatus
import com.acme.drivers.v1.estimateDriverLicenseRequest
import com.acme.drivers.v1.explainDriverLicenseDecisionRequest
import com.acme.drivers.v1.explainDutyStatusDecisionResponse
import com.acme.drivers.v1.getDutyStatusRulesResponse
import com.acme.drivers.v1.normalizeDriverRequest
import com.acme.drivers.v1.normalizeDutyStatusResponse
import com.acme.drivers.v1.previewDutyStatusResponse
import com.acme.drivers.v1.quoteDriverRequest
import com.acme.drivers.v1.scoreDutyStatusResponse
import com.acme.drivers.v1.simulateDriverLicenseRequest
import com.acme.drivers.v1.validateDutyStatusResponse
import com.acme.identity.v1.GetUserRequest
import com.acme.identity.v1.GetUserResponse
import com.acme.identity.v1.UserServiceGrpcKt
import com.acme.identity.v1.getUserRequest
import com.acme.identity.v1.userRef
import com.acme.notifications.v1.CreateNotificationResponse
import com.acme.notifications.v1.NotificationServiceGrpcKt
import com.acme.notifications.v1.createNotificationRequest
import com.acme.notifications.v1.notification
import com.acme.search.v1.SearchIndexValidationServiceGrpcKt
import com.acme.search.v1.ValidateSearchIndexRequest
import com.acme.search.v1.ValidateSearchIndexRequestKt
import com.acme.search.v1.validateSearchIndexRequest
import com.acme.transport.drivers.DriversRepository
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.venues.v1.CountVenuesRequestKt
import com.acme.venues.v1.VenueQueryServiceGrpcKt
import com.acme.venues.v1.countVenuesRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Checks duty statuses against the rules before anything acts on them. */
class DutyStatusValidationEndpoint(
    private val store: DriversRepository,
    private val workers: CoroutineScope,
    private val driversDriverCalculationService: DriverCalculationServiceGrpcKt.DriverCalculationServiceCoroutineStub,
    private val driversDriverLicenseCalculationService: DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub,
    private val driversDriverLicenseValidationService: DriverLicenseValidationServiceGrpcKt.DriverLicenseValidationServiceCoroutineStub,
    private val driversDriverValidationService: DriverValidationServiceGrpcKt.DriverValidationServiceCoroutineStub,
    private val identityUserService: UserServiceGrpcKt.UserServiceCoroutineStub,
    private val notificationsNotificationService: NotificationServiceGrpcKt.NotificationServiceCoroutineStub,
    private val searchSearchIndexValidationService: SearchIndexValidationServiceGrpcKt.SearchIndexValidationServiceCoroutineStub,
    private val venuesVenueQueryService: VenueQueryServiceGrpcKt.VenueQueryServiceCoroutineStub,
) : DutyStatusValidationServiceGrpcKt.DutyStatusValidationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DutyStatusValidationEndpoint::class.java)

    private val invokeNormalizeDutyStatus1: suspend (SimulateDriverLicenseRequest) -> SimulateDriverLicenseResponse = { payload ->
        driversDriverLicenseCalculationService.simulateDriverLicense(payload)
    }

    private val workerForValidateDutyStatus0 = ReplyMailbox<NormalizeDriverRequest, NormalizeDriverResponse>(workers) { payload ->
        withTimeout(5_000) { driversDriverValidationService.normalizeDriver(payload) }
    }

    /** Explains a duty status. */
    override suspend fun explainDutyStatusDecision(request: ExplainDutyStatusDecisionRequest): ExplainDutyStatusDecisionResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusValidationService/ExplainDutyStatusDecision") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainDutyStatusDecision", request)
            log.atInfo().addKeyValue("operation", "ExplainDutyStatusDecision").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dutyStatus.dutyStatusId).log("Recorded audit detail")
            var reply0 = CalculateDriverLicenseResponse.getDefaultInstance()
            request.dutyStatus.measurementsList.asFlow().map { item ->
                val outbound0 = calculateDriverLicenseRequest {
                    this.context = request.context
                    this.driverLicense = driverLicense {
                        this.audit = auditInfo {
                            this.updatedAt = item.observedAt
                        }
                        this.counterparty = driverRef {
                            this.displayName = request.dutyStatus.subject.displayName
                            this.email = request.details.contact.email
                        }
                        this.number = request.dutyStatus.subject.number
                    }
                }
                driversDriverLicenseCalculationService.calculateDriverLicense(outbound0)
            }.collect { reply0 = it }
            explainDutyStatusDecisionResponse {
                this.dutyStatus = dutyStatus {
                    this.dutyStatusId = request.dutyStatusId
                    this.sequence = request.dutyStatus.sequence
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Returns a duty status. */
    override suspend fun getDutyStatusRules(request: GetDutyStatusRulesRequest): GetDutyStatusRulesResponse {
        return handleGetDutyStatusRules(request)
    }

    /** Normalizes a duty status. */
    override suspend fun normalizeDutyStatus(request: NormalizeDutyStatusRequest): NormalizeDutyStatusResponse = handleNormalizeDutyStatus(request)

    /** Previews a duty status. */
    override suspend fun previewDutyStatus(request: PreviewDutyStatusRequest): PreviewDutyStatusResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusValidationService/PreviewDutyStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PreviewDutyStatus", request)
            log.atInfo().addKeyValue("operation", "PreviewDutyStatus").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dutyStatus.dutyStatusId).log("Recorded audit detail")
            var reply0 = CreateNotificationResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<DutyStatusMeasurement>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound0 = createNotificationRequest {
                            this.context = request.context
                            this.notification = notification {
                                this.audit = auditInfo {
                                    this.updatedAt = item.observedAt
                                }
                                this.requester = contactInfo {
                                    this.email = request.details.contact.email
                                    this.name = request.details.contact.name
                                }
                            }
                        }
                        reply0 = notificationsNotificationService.createNotification(outbound0)
                    }
                }
                try {
                    request.dutyStatus.measurementsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound1 = ValidateSearchIndexRequest.newBuilder().apply {
                setContext(request.context)
                setDetails(ValidateSearchIndexRequestKt.details {
                    this.contact = contactInfo {
                        this.name = reply0.notification.requester.name
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> searchSearchIndexValidationService.validateSearchIndex(value) }
            previewDutyStatusResponse {
                this.dutyStatus = dutyStatus {
                    this.dutyStatusId = request.dutyStatusId
                    this.subject = request.dutyStatus.subject
                }
                this.outcome = PreviewDutyStatusResponseKt.outcome {
                    this.effectiveAt = reply1.outcome.effectiveAt
                }
                this.result = operationResult {
                    this.status = reply1.result.status
                }
            }
        }

    /** Scores a duty status. */
    override suspend fun scoreDutyStatus(request: ScoreDutyStatusRequest): ScoreDutyStatusResponse {
        return handleScoreDutyStatus(request)
    }

    /** Validates a duty status. */
    override suspend fun validateDutyStatus(request: ValidateDutyStatusRequest): ValidateDutyStatusResponse = handleValidateDutyStatus(request)

    private suspend fun handleGetDutyStatusRules(request: GetDutyStatusRulesRequest): GetDutyStatusRulesResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusValidationService/GetDutyStatusRules") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetDutyStatusRules", request)
            log.atInfo().addKeyValue("operation", "GetDutyStatusRules").log("Handling transport request")
            val outbound0 = estimateDriverLicenseRequest {
                this.context = request.context
                this.driverLicense = driverLicense {
                    this.audit = auditInfo {
                        this.createdAt = request.context.deadline
                    }
                    this.counterparty = driverRef {
                        this.email = request.context.actor.email
                    }
                    this.lines.addAll(listOf(driverLicenseLine {
                        this.attributes.putAll(request.context.baggageMap)
                    }))
                }
            }
            val command0: Submission<EstimateDriverLicenseRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = driversDriverLicenseCalculationService.estimateDriverLicense(payload0)
            getDutyStatusRulesResponse {
                this.dutyStatus = dutyStatus {
                    this.dutyStatusId = request.dutyStatusId
                    this.subject = driverLicenseRef {
                        this.displayName = reply0.driverLicense.counterparty.displayName
                        this.number = reply0.driverLicense.number
                    }
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleNormalizeDutyStatus(request: NormalizeDutyStatusRequest): NormalizeDutyStatusResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusValidationService/NormalizeDutyStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "NormalizeDutyStatus", request)
            log.atInfo().addKeyValue("operation", "NormalizeDutyStatus").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dutyStatus.dutyStatusId).log("Recorded audit detail")
            val outbound0 = getUserRequest {
                this.context = request.context
                this.userId = request.context.actor.userId
            }
            val adapter0 = OutboundPort<GetUserRequest, GetUserResponse> { payload ->
                identityUserService.getUser(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = simulateDriverLicenseRequest {
                this.context = request.context
                this.driverLicense = driverLicense {
                    this.billingAddress = postalAddress {
                        this.countryCode = reply0.user.primaryAddress.countryCode
                    }
                }
            }
            val reply1 = invokeNormalizeDutyStatus1(outbound1)
            normalizeDutyStatusResponse {
                this.dutyStatus = dutyStatus {
                    this.dutyStatusId = request.dutyStatusId
                    this.source = request.dutyStatus.source
                    this.subject = driverLicenseRef {
                        this.number = reply0.user.nationalId.number
                    }
                }
                this.outcome = NormalizeDutyStatusResponseKt.outcome {
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleScoreDutyStatus(request: ScoreDutyStatusRequest): ScoreDutyStatusResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusValidationService/ScoreDutyStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScoreDutyStatus", request)
            log.atInfo().addKeyValue("operation", "ScoreDutyStatus").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dutyStatus.dutyStatusId).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                countVenuesRequest {
                    this.context = request.context
                    this.filter = CountVenuesRequestKt.filter {
                        this.ownerId = request.dutyStatusId
                        this.region = request.details.destination.region
                    }
                }
            }
            val reply0 = venuesVenueQueryService.countVenues(outbound0)
            var reply1 = QuoteDriverResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.dutyStatus.measurementsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound1 = quoteDriverRequest {
                                this.context = request.context
                                this.driver = driver {
                                    this.passport = driverPassport {
                                        this.expiresAt = item.observedAt
                                        this.number = request.dutyStatus.subject.number
                                    }
                                    this.primaryAddress = postalAddress {
                                        this.postalCode = request.details.destination.postalCode
                                    }
                                    this.relationshipManager = userRef {
                                        this.displayName = request.dutyStatus.subject.displayName
                                    }
                                }
                            }
                            driversDriverCalculationService.quoteDriver(outbound1)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply1 = it }
                }
            }
            val outbound2 = explainDriverLicenseDecisionRequest {
                this.context = request.context
                this.details = ExplainDriverLicenseDecisionRequestKt.details {
                    this.contact = contactInfo {
                        this.email = reply1.driver.contact.email
                    }
                }
                this.driverLicense = driverLicense {
                    this.audit = auditInfo {
                        this.updatedBy = reply1.driver.audit.updatedBy
                    }
                    this.counterparty = driverRef {
                        this.displayName = reply1.driver.relationshipManager.displayName
                        this.email = reply1.driver.relationshipManager.email
                    }
                }
            }
            val reply2 = driversDriverLicenseValidationService.explainDriverLicenseDecision(WireCopies.reflective(outbound2))
            scoreDutyStatusResponse {
                this.breakdown = ScoreDutyStatusResponseKt.breakdown {
                    this.total = reply2.driverLicense.total
                }
                this.dutyStatus = dutyStatus {
                    this.dutyStatusId = request.dutyStatusId
                    this.kind = request.dutyStatus.kind
                }
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleValidateDutyStatus(request: ValidateDutyStatusRequest): ValidateDutyStatusResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusValidationService/ValidateDutyStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ValidateDutyStatus", request)
            log.atInfo().addKeyValue("operation", "ValidateDutyStatus").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dutyStatus.dutyStatusId).log("Recorded audit detail")
            val outbound0 = normalizeDriverRequest {
                this.context = request.context
                this.details = NormalizeDriverRequestKt.details {
                    this.contact = contactInfo {
                        this.preferredChannel = request.details.contact.preferredChannel
                    }
                }
                this.driver = driver {
                    this.contact = contactInfo {
                        this.name = request.details.contact.name
                    }
                    this.passport = driverPassport {
                        this.number = request.dutyStatus.subject.number
                    }
                    this.relationshipManager = userRef {
                        this.displayName = request.dutyStatus.subject.displayName
                    }
                }
            }
            val reply0 = workerForValidateDutyStatus0.submit(outbound0)
            validateDutyStatusResponse {
                this.dutyStatus = dutyStatus {
                    this.correlationId = reply0.result.correlationId
                    this.dutyStatusId = request.dutyStatusId
                    this.kind = request.dutyStatus.kind
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }
}
