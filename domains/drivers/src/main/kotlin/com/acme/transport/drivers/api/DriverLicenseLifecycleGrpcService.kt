package com.acme.transport.drivers.api

import com.acme.common.v1.PersonName
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.drivers.v1.ActivateDriverLicenseRequest
import com.acme.drivers.v1.ActivateDriverLicenseResponse
import com.acme.drivers.v1.ActivateDriverLicenseResponseKt
import com.acme.drivers.v1.CountDriverLicenseTotalsRequest
import com.acme.drivers.v1.CountDriverLicenseTotalsRequestKt
import com.acme.drivers.v1.DriverLicenseLifecycleServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseReportingServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseValidationServiceGrpcKt
import com.acme.drivers.v1.DriverQueryServiceGrpcKt
import com.acme.drivers.v1.DriverServiceGrpcKt
import com.acme.drivers.v1.DutyStatusCalculationServiceGrpcKt
import com.acme.drivers.v1.ExpireDriverLicenseRequest
import com.acme.drivers.v1.ExpireDriverLicenseResponse
import com.acme.drivers.v1.ExpireDriverLicenseResponseKt
import com.acme.drivers.v1.FetchDriverLicenseReportRequest
import com.acme.drivers.v1.FetchDriverLicenseReportResponse
import com.acme.drivers.v1.GetDriverLicenseLifecycleRequest
import com.acme.drivers.v1.GetDriverLicenseLifecycleResponse
import com.acme.drivers.v1.LintDriverLicensesRequest
import com.acme.drivers.v1.LintDriverLicensesRequestKt
import com.acme.drivers.v1.ReinstateDriverLicenseRequest
import com.acme.drivers.v1.ReinstateDriverLicenseResponse
import com.acme.drivers.v1.ReinstateDriverLicenseResponseKt
import com.acme.drivers.v1.RenewDriverLicenseRequest
import com.acme.drivers.v1.RenewDriverLicenseResponse
import com.acme.drivers.v1.RenewDriverLicenseResponseKt
import com.acme.drivers.v1.SummarizeDriversRequest
import com.acme.drivers.v1.SummarizeDriversRequestKt
import com.acme.drivers.v1.SummarizeDriversResponse
import com.acme.drivers.v1.SuspendDriverLicenseRequest
import com.acme.drivers.v1.SuspendDriverLicenseResponse
import com.acme.drivers.v1.SuspendDriverLicenseResponseKt
import com.acme.drivers.v1.TerminateDriverLicenseRequest
import com.acme.drivers.v1.TerminateDriverLicenseResponse
import com.acme.drivers.v1.TerminateDriverLicenseResponseKt
import com.acme.drivers.v1.UpdateDriverResponse
import com.acme.drivers.v1.activateDriverLicenseResponse
import com.acme.drivers.v1.countDriverLicenseTotalsRequest
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverLicenseLine
import com.acme.drivers.v1.driverLicenseRef
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.dutyStatus
import com.acme.drivers.v1.expireDriverLicenseResponse
import com.acme.drivers.v1.fetchDriverLicenseReportRequest
import com.acme.drivers.v1.getDriverLicenseLifecycleResponse
import com.acme.drivers.v1.lintDriverLicensesRequest
import com.acme.drivers.v1.reinstateDriverLicenseResponse
import com.acme.drivers.v1.renewDriverLicenseResponse
import com.acme.drivers.v1.simulateDutyStatusRequest
import com.acme.drivers.v1.streamDriverLicenseMetricsRequest
import com.acme.drivers.v1.summarizeDriversRequest
import com.acme.drivers.v1.suspendDriverLicenseResponse
import com.acme.drivers.v1.terminateDriverLicenseResponse
import com.acme.drivers.v1.updateDriverRequest
import com.acme.featureflags.v1.FeatureFlagQueryServiceGrpcKt
import com.acme.featureflags.v1.SummarizeFeatureFlagsRequestKt
import com.acme.featureflags.v1.summarizeFeatureFlagsRequest
import com.acme.fuel.v1.FuelTransactionImportServiceGrpcKt
import com.acme.fuel.v1.ListFuelTransactionImportsRequest
import com.acme.fuel.v1.ListFuelTransactionImportsRequestKt
import com.acme.fuel.v1.listFuelTransactionImportsRequest
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.LedgerAccountValidationServiceGrpcKt
import com.acme.ledger.v1.ScoreLedgerAccountRequest
import com.acme.ledger.v1.ScoreLedgerAccountRequestKt
import com.acme.ledger.v1.ledgerAccount
import com.acme.ledger.v1.scoreLedgerAccountRequest
import com.acme.notifications.v1.ExplainNotificationDecisionRequestKt
import com.acme.notifications.v1.ExplainNotificationDecisionResponse
import com.acme.notifications.v1.NotificationValidationServiceGrpcKt
import com.acme.notifications.v1.copy as notificationsCopy
import com.acme.notifications.v1.explainNotificationDecisionRequest
import com.acme.notifications.v1.notification
import com.acme.transport.drivers.DriversRepository
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.venues.v1.NormalizeVenueRequestKt
import com.acme.venues.v1.VenueValidationServiceGrpcKt
import com.acme.venues.v1.normalizeVenueRequest
import com.acme.venues.v1.venue
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import org.slf4j.LoggerFactory

/** Moves a driver license between the states of its lifecycle. */
class DriverLicenseLifecycleGrpcService(
    private val store: DriversRepository,
    private val workers: CoroutineScope,
    private val driversDriverLicenseReportingService: DriverLicenseReportingServiceGrpcKt.DriverLicenseReportingServiceCoroutineStub,
    private val driversDriverLicenseValidationService: DriverLicenseValidationServiceGrpcKt.DriverLicenseValidationServiceCoroutineStub,
    private val driversDriverQueryService: DriverQueryServiceGrpcKt.DriverQueryServiceCoroutineStub,
    private val driversDriverService: DriverServiceGrpcKt.DriverServiceCoroutineStub,
    private val driversDutyStatusCalculationService: DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineStub,
    private val featureflagsFeatureFlagQueryService: FeatureFlagQueryServiceGrpcKt.FeatureFlagQueryServiceCoroutineStub,
    private val fuelFuelTransactionImportService: FuelTransactionImportServiceGrpcKt.FuelTransactionImportServiceCoroutineStub,
    private val ledgerLedgerAccountValidationService: LedgerAccountValidationServiceGrpcKt.LedgerAccountValidationServiceCoroutineStub,
    private val notificationsNotificationValidationService: NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub,
    private val venuesVenueValidationService: VenueValidationServiceGrpcKt.VenueValidationServiceCoroutineStub,
) : DriverLicenseLifecycleServiceGrpcKt.DriverLicenseLifecycleServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DriverLicenseLifecycleGrpcService::class.java)

    private val invokeSuspendDriverLicense0: suspend (FetchDriverLicenseReportRequest) -> FetchDriverLicenseReportResponse = { payload ->
        driversDriverLicenseReportingService.fetchDriverLicenseReport(payload)
    }

    /** Activates a driver license. */
    override suspend fun activateDriverLicense(request: ActivateDriverLicenseRequest): ActivateDriverLicenseResponse {
        return handleActivateDriverLicense(request)
    }

    /** Expires a driver license. */
    override suspend fun expireDriverLicense(request: ExpireDriverLicenseRequest): ExpireDriverLicenseResponse = handleExpireDriverLicense(request)

    /** Returns a driver license. */
    override suspend fun getDriverLicenseLifecycle(request: GetDriverLicenseLifecycleRequest): GetDriverLicenseLifecycleResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseLifecycleService/GetDriverLicenseLifecycle") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetDriverLicenseLifecycle", request)
            log.atInfo().addKeyValue("operation", "GetDriverLicenseLifecycle").log("Handling transport request")
            val outbound0 = summarizeFeatureFlagsRequest {
                this.context = request.context
                this.details = SummarizeFeatureFlagsRequestKt.details {
                    this.filters.putAll(request.context.baggageMap)
                }
            }
            val reply0 = featureflagsFeatureFlagQueryService.summarizeFeatureFlags(WireCopies.json(outbound0))
            getDriverLicenseLifecycleResponse {
                this.driverLicense = driverLicense {
                    this.driverLicenseId = request.driverLicenseId
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
            }
        }

    /** Reinstates a driver license. */
    override suspend fun reinstateDriverLicense(request: ReinstateDriverLicenseRequest): ReinstateDriverLicenseResponse {
        return handleReinstateDriverLicense(request)
    }

    /** Renews a driver license. */
    override suspend fun renewDriverLicense(request: RenewDriverLicenseRequest): RenewDriverLicenseResponse = handleRenewDriverLicense(request)

    /** Suspends a driver license. */
    override suspend fun suspendDriverLicense(request: SuspendDriverLicenseRequest): SuspendDriverLicenseResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseLifecycleService/SuspendDriverLicense") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SuspendDriverLicense", request)
            log.atInfo().addKeyValue("operation", "SuspendDriverLicense").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = fetchDriverLicenseReportRequest {
                this.context = request.context
                this.driverLicenseId = request.driverLicenseId
            }
            val reply0 = invokeSuspendDriverLicense0(outbound0)
            val outbound1 = summarizeDriversRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.details = SummarizeDriversRequestKt.details {
                    this.filters.putAll(reply0.driverLicense.metadataMap)
                }
                this.driverId = reply0.driverLicense.counterparty.driverId
                this.reason = request.reason
            }
            val invoke1: suspend (SummarizeDriversRequest, Metadata) -> SummarizeDriversResponse = driversDriverQueryService::summarizeDrivers
            val reply1 = invoke1(outbound1, Metadata())
            suspendDriverLicenseResponse {
                this.driverLicense = driverLicense {
                    this.driverLicenseId = request.driverLicenseId
                    this.notes = reply0.driverLicense.notes
                }
                this.outcome = SuspendDriverLicenseResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = reply1.result.status
                }
            }
        }

    /** Terminates a driver license. */
    override suspend fun terminateDriverLicense(request: TerminateDriverLicenseRequest): TerminateDriverLicenseResponse {
        return handleTerminateDriverLicense(request)
    }

    private fun ActivateDriverLicenseRequest.toScoreLedgerAccountFromActivateDriverLicense2(reply0: UpdateDriverResponse, reply1: ExplainNotificationDecisionResponse): ScoreLedgerAccountRequest {
        val source = this
        return scoreLedgerAccountRequest {
            this.context = source.context
            this.details = ScoreLedgerAccountRequestKt.details {
                this.destination = postalAddress {
                    this.lines.addAll(reply0.driver.primaryAddress.linesList)
                    this.postalCode = reply0.driver.primaryAddress.postalCode
                }
            }
            this.ledgerAccount = ledgerAccount {
                this.contact = contactInfo {
                    this.preferredChannel = reply1.notification.requester.preferredChannel
                }
            }
        }
    }

    private suspend fun handleActivateDriverLicense(request: ActivateDriverLicenseRequest): ActivateDriverLicenseResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseLifecycleService/ActivateDriverLicense") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ActivateDriverLicense", request)
            log.atInfo().addKeyValue("operation", "ActivateDriverLicense").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = updateDriverRequest {
                this.context = request.context
                this.driver = driver {
                    this.contact = contactInfo {
                        this.email = request.details.notifyContact.email
                        this.phone = request.details.notifyContact.phone
                    }
                    this.relationshipManager = userRef {
                        this.email = request.details.newOwner.email
                    }
                }
            }
            val reply0 = driversDriverService.updateDriver(outbound0)
            val outbound1 = explainNotificationDecisionRequest {}.notificationsCopy {
                this.context = request.context
                this.details = ExplainNotificationDecisionRequestKt.details {
                    this.contact = contactInfo {
                        this.phone = request.details.notifyContact.phone
                    }
                }
                this.notification = notification {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.driver.audit.updatedAt
                    }
                }
            }
            val reply1 = notificationsNotificationValidationService.explainNotificationDecision(outbound1)
            val outbound2 = request.toScoreLedgerAccountFromActivateDriverLicense2(reply0, reply1)
            val reply2 = ledgerLedgerAccountValidationService.scoreLedgerAccount(outbound2)
            activateDriverLicenseResponse {
                this.driverLicense = driverLicense {
                    this.counterparty = driverRef {
                        this.email = reply1.notification.requester.email
                    }
                    this.driverLicenseId = request.driverLicenseId
                }
                this.outcome = ActivateDriverLicenseResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleExpireDriverLicense(request: ExpireDriverLicenseRequest): ExpireDriverLicenseResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseLifecycleService/ExpireDriverLicense") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExpireDriverLicense", request)
            log.atInfo().addKeyValue("operation", "ExpireDriverLicense").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = simulateDutyStatusRequest {
                this.context = request.context
                this.dutyStatus = dutyStatus {
                    this.subject = driverLicenseRef {
                        this.displayName = request.details.newOwner.displayName
                        this.driverLicenseId = request.driverLicenseId
                    }
                }
            }
            val reply0 = driversDutyStatusCalculationService.simulateDutyStatus(outbound0)
            val outbound1 = countDriverLicenseTotalsRequest {
                this.context = request.context
                this.filter = CountDriverLicenseTotalsRequestKt.filter {
                    this.createdAfter = reply0.dutyStatus.occurredAt
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = driversDriverLicenseReportingService.countDriverLicenseTotals(envelope1.unpack(CountDriverLicenseTotalsRequest::class.java))
            expireDriverLicenseResponse {
                this.driverLicense = driverLicense {
                    this.driverLicenseId = request.driverLicenseId
                    this.lines.addAll(listOf(driverLicenseLine {
                        this.attributes.putAll(reply0.dutyStatus.attributesMap)
                    }))
                }
                this.outcome = ExpireDriverLicenseResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private data class ReinstateDriverLicense0Command(
        val context: RequestContext,
        val detailsContactName: PersonName
    ) {
        fun toRequest(): LintDriverLicensesRequest {
            val command = this
            return lintDriverLicensesRequest {
                this.context = command.context
                this.details = LintDriverLicensesRequestKt.details {
                    this.contact = contactInfo {
                        this.name = command.detailsContactName
                    }
                }
            }
        }
    }

    private suspend fun handleReinstateDriverLicense(request: ReinstateDriverLicenseRequest): ReinstateDriverLicenseResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseLifecycleService/ReinstateDriverLicense") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ReinstateDriverLicense", request)
            log.atInfo().addKeyValue("operation", "ReinstateDriverLicense").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = ReinstateDriverLicense0Command(
                context = request.context,
                detailsContactName = request.details.notifyContact.name
            ).toRequest()
            val reply0 = driversDriverLicenseValidationService.lintDriverLicenses(outbound0)
            reinstateDriverLicenseResponse {
                this.driverLicense = driverLicense {
                    this.driverLicenseId = request.driverLicenseId
                }
                this.outcome = ReinstateDriverLicenseResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.status = reply0.outcome.status
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleRenewDriverLicense(request: RenewDriverLicenseRequest): RenewDriverLicenseResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseLifecycleService/RenewDriverLicense") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RenewDriverLicense", request)
            log.atInfo().addKeyValue("operation", "RenewDriverLicense").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = countDriverLicenseTotalsRequest {
                this.context = request.context
                this.filter = CountDriverLicenseTotalsRequestKt.filter {
                    this.createdAfter = request.details.effectiveAt
                }
            }
            val command0: Submission<CountDriverLicenseTotalsRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = driversDriverLicenseReportingService.countDriverLicenseTotals(payload0)
            val outbound1 = streamDriverLicenseMetricsRequest {
                this.context = request.context
                this.subscriber = contactInfo {
                    this.email = request.details.newOwner.email
                    this.name = request.details.notifyContact.name
                    this.phone = request.details.notifyContact.phone
                }
            }
            driversDriverLicenseReportingService.streamDriverLicenseMetrics(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            renewDriverLicenseResponse {
                this.driverLicense = driverLicense {
                    this.driverLicenseId = request.driverLicenseId
                }
                this.outcome = RenewDriverLicenseResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleTerminateDriverLicense(request: TerminateDriverLicenseRequest): TerminateDriverLicenseResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseLifecycleService/TerminateDriverLicense") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "TerminateDriverLicense", request)
            log.atInfo().addKeyValue("operation", "TerminateDriverLicense").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = ListFuelTransactionImportsRequest.newBuilder().apply {
                setContext(request.context)
                setFilter(ListFuelTransactionImportsRequestKt.filter {
                    this.createdAfter = request.details.effectiveAt
                    this.labels.putAll(request.annotationsMap)
                    this.ownerId = request.details.newOwner.userId
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> fuelFuelTransactionImportService.listFuelTransactionImports(value) }
            val outbound1 = RequestFactory.create {
                normalizeVenueRequest {
                    this.context = request.context
                    this.details = NormalizeVenueRequestKt.details {
                        this.contact = contactInfo {
                            this.preferredChannel = request.details.notifyContact.preferredChannel
                        }
                    }
                    this.venue = venue {
                        this.owner = userRef {
                            this.displayName = request.details.newOwner.displayName
                            this.email = request.details.newOwner.email
                        }
                    }
                }
            }
            val reply1 = venuesVenueValidationService.normalizeVenue(outbound1)
            terminateDriverLicenseResponse {
                this.driverLicense = driverLicense {
                    this.driverLicenseId = request.driverLicenseId
                }
                this.outcome = TerminateDriverLicenseResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }
}
