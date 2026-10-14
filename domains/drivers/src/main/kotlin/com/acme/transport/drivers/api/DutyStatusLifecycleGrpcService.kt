package com.acme.transport.drivers.api

import com.acme.authorization.v1.AccessPolicyServiceGrpcKt
import com.acme.authorization.v1.accessPolicy
import com.acme.authorization.v1.batchCreateAccessPoliciesRequest
import com.acme.authorization.v1.cloneAccessPolicyRequest
import com.acme.common.v1.PersonName
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.drivers.v1.ActivateDutyStatusRequest
import com.acme.drivers.v1.ActivateDutyStatusResponse
import com.acme.drivers.v1.ActivateDutyStatusResponseKt
import com.acme.drivers.v1.CountDriverLicenseTotalsRequest
import com.acme.drivers.v1.CountDriverLicenseTotalsRequestKt
import com.acme.drivers.v1.DriverLicenseCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseReportingServiceGrpcKt
import com.acme.drivers.v1.DriverLifecycleServiceGrpcKt
import com.acme.drivers.v1.DriverServiceGrpcKt
import com.acme.drivers.v1.DutyStatusCalculationServiceGrpcKt
import com.acme.drivers.v1.DutyStatusLifecycleServiceGrpcKt
import com.acme.drivers.v1.DutyStatusValidationServiceGrpcKt
import com.acme.drivers.v1.ExpireDutyStatusRequest
import com.acme.drivers.v1.ExpireDutyStatusResponse
import com.acme.drivers.v1.ExpireDutyStatusResponseKt
import com.acme.drivers.v1.ExplainDutyStatusCalculationRequest
import com.acme.drivers.v1.ExplainDutyStatusCalculationRequestKt
import com.acme.drivers.v1.GenerateDriverLicenseReportRequest
import com.acme.drivers.v1.GenerateDriverLicenseReportResponse
import com.acme.drivers.v1.GetDutyStatusLifecycleRequest
import com.acme.drivers.v1.GetDutyStatusLifecycleResponse
import com.acme.drivers.v1.PreviewDutyStatusRequestKt
import com.acme.drivers.v1.PreviewDutyStatusResponse
import com.acme.drivers.v1.QuoteDriverLicenseRequest
import com.acme.drivers.v1.QuoteDriverLicenseRequestKt
import com.acme.drivers.v1.ReinstateDutyStatusRequest
import com.acme.drivers.v1.ReinstateDutyStatusResponse
import com.acme.drivers.v1.ReinstateDutyStatusResponseKt
import com.acme.drivers.v1.RenewDutyStatusRequest
import com.acme.drivers.v1.RenewDutyStatusResponse
import com.acme.drivers.v1.RenewDutyStatusResponseKt
import com.acme.drivers.v1.ScheduleDriverExpiryRequestKt
import com.acme.drivers.v1.ScheduleDutyStatusExpiryRequest
import com.acme.drivers.v1.ScheduleDutyStatusExpiryResponse
import com.acme.drivers.v1.ScheduleDutyStatusExpiryResponseKt
import com.acme.drivers.v1.SuspendDutyStatusRequest
import com.acme.drivers.v1.SuspendDutyStatusResponse
import com.acme.drivers.v1.SuspendDutyStatusResponseKt
import com.acme.drivers.v1.TerminateDutyStatusRequest
import com.acme.drivers.v1.TerminateDutyStatusResponse
import com.acme.drivers.v1.TerminateDutyStatusResponseKt
import com.acme.drivers.v1.activateDutyStatusResponse
import com.acme.drivers.v1.copy as driversCopy
import com.acme.drivers.v1.countDriverLicenseTotalsRequest
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverLicenseLine
import com.acme.drivers.v1.driverLicenseRef
import com.acme.drivers.v1.dutyStatus
import com.acme.drivers.v1.expireDutyStatusResponse
import com.acme.drivers.v1.explainDutyStatusCalculationRequest
import com.acme.drivers.v1.generateDriverLicenseReportRequest
import com.acme.drivers.v1.getDutyStatusLifecycleResponse
import com.acme.drivers.v1.previewDutyStatusRequest
import com.acme.drivers.v1.quoteDriverLicenseRequest
import com.acme.drivers.v1.reinstateDutyStatusResponse
import com.acme.drivers.v1.renewDutyStatusResponse
import com.acme.drivers.v1.scheduleDriverExpiryRequest
import com.acme.drivers.v1.scheduleDutyStatusExpiryResponse
import com.acme.drivers.v1.suspendDutyStatusResponse
import com.acme.drivers.v1.terminateDutyStatusResponse
import com.acme.drivers.v1.watchDriversRequest
import com.acme.fuel.v1.FuelCardExportServiceGrpcKt
import com.acme.fuel.v1.ShareFuelCardExportRequestKt
import com.acme.fuel.v1.shareFuelCardExportRequest
import com.acme.identity.v1.PreviewUserRequest
import com.acme.identity.v1.UserValidationServiceGrpcKt
import com.acme.identity.v1.previewUserRequest
import com.acme.identity.v1.user
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.CreateLedgerAccountResponse
import com.acme.ledger.v1.LedgerAccountServiceGrpcKt
import com.acme.ledger.v1.createLedgerAccountRequest
import com.acme.ledger.v1.ledgerAccount
import com.acme.maintenance.v1.PartReportingServiceGrpcKt
import com.acme.maintenance.v1.generatePartReportRequest
import com.acme.notifications.v1.FindNotificationsRequest
import com.acme.notifications.v1.FindNotificationsRequestKt
import com.acme.notifications.v1.FindNotificationsResponse
import com.acme.notifications.v1.NotificationQueryServiceGrpcKt
import com.acme.notifications.v1.findNotificationsRequest
import com.acme.transport.drivers.DriversRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.google.protobuf.Any as ProtoAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Moves a duty status between the states of its lifecycle. */
class DutyStatusLifecycleGrpcService(
    private val store: DriversRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyService: AccessPolicyServiceGrpcKt.AccessPolicyServiceCoroutineStub,
    private val driversDriverLicenseCalculationService: DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub,
    private val driversDriverLicenseReportingService: DriverLicenseReportingServiceGrpcKt.DriverLicenseReportingServiceCoroutineStub,
    private val driversDriverLifecycleService: DriverLifecycleServiceGrpcKt.DriverLifecycleServiceCoroutineStub,
    private val driversDriverService: DriverServiceGrpcKt.DriverServiceCoroutineStub,
    private val driversDutyStatusCalculationService: DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineStub,
    private val driversDutyStatusValidationService: DutyStatusValidationServiceGrpcKt.DutyStatusValidationServiceCoroutineStub,
    private val fuelFuelCardExportService: FuelCardExportServiceGrpcKt.FuelCardExportServiceCoroutineStub,
    private val identityUserValidationService: UserValidationServiceGrpcKt.UserValidationServiceCoroutineStub,
    private val ledgerLedgerAccountService: LedgerAccountServiceGrpcKt.LedgerAccountServiceCoroutineStub,
    private val maintenancePartReportingService: PartReportingServiceGrpcKt.PartReportingServiceCoroutineStub,
    private val notificationsNotificationQueryService: NotificationQueryServiceGrpcKt.NotificationQueryServiceCoroutineStub,
) : DutyStatusLifecycleServiceGrpcKt.DutyStatusLifecycleServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DutyStatusLifecycleGrpcService::class.java)

    private val invokeScheduleDutyStatusExpiry0: suspend (GenerateDriverLicenseReportRequest) -> GenerateDriverLicenseReportResponse = { payload ->
        driversDriverLicenseReportingService.generateDriverLicenseReport(payload)
    }

    private val workerForTerminateDutyStatus2 = ReplyMailbox<FindNotificationsRequest, FindNotificationsResponse>(workers) { payload ->
        withTimeout(5_000) { notificationsNotificationQueryService.findNotifications(payload) }
    }

    /** Activates a duty status. */
    override suspend fun activateDutyStatus(request: ActivateDutyStatusRequest): ActivateDutyStatusResponse {
        return handleActivateDutyStatus(request)
    }

    /** Expires a duty status. */
    override suspend fun expireDutyStatus(request: ExpireDutyStatusRequest): ExpireDutyStatusResponse = handleExpireDutyStatus(request)

    /** Returns a duty status. */
    override suspend fun getDutyStatusLifecycle(request: GetDutyStatusLifecycleRequest): GetDutyStatusLifecycleResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusLifecycleService/GetDutyStatusLifecycle") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetDutyStatusLifecycle", request)
            log.atInfo().addKeyValue("operation", "GetDutyStatusLifecycle").log("Handling transport request")
            val outbound0 = countDriverLicenseTotalsRequest {
                this.context = request.context
                this.filter = CountDriverLicenseTotalsRequestKt.filter {
                    this.ownerId = request.context.actor.userId
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = driversDriverLicenseReportingService.countDriverLicenseTotals(envelope0.unpack(CountDriverLicenseTotalsRequest::class.java))
            getDutyStatusLifecycleResponse {
                this.dutyStatus = dutyStatus {
                    this.dutyStatusId = request.dutyStatusId
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = reply0.result.status
                }
            }
        }

    /** Reinstates a duty status. */
    override suspend fun reinstateDutyStatus(request: ReinstateDutyStatusRequest): ReinstateDutyStatusResponse {
        return handleReinstateDutyStatus(request)
    }

    /** Renews a duty status. */
    override suspend fun renewDutyStatus(request: RenewDutyStatusRequest): RenewDutyStatusResponse = handleRenewDutyStatus(request)

    /** Schedules a duty status. */
    override suspend fun scheduleDutyStatusExpiry(request: ScheduleDutyStatusExpiryRequest): ScheduleDutyStatusExpiryResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusLifecycleService/ScheduleDutyStatusExpiry") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScheduleDutyStatusExpiry", request)
            log.atInfo().addKeyValue("operation", "ScheduleDutyStatusExpiry").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dutyStatus.kind).log("Recorded audit detail")
            val outbound0 = generateDriverLicenseReportRequest {
                this.context = request.context
                this.reason = request.reason
            }
            val reply0 = invokeScheduleDutyStatusExpiry0(outbound0)
            var reply1 = CreateLedgerAccountResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.dutyStatus.measurementsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound1 = createLedgerAccountRequest {
                                this.context = request.context
                                this.ledgerAccount = ledgerAccount {
                                    this.audit = auditInfo {
                                        this.createdAt = item.observedAt
                                    }
                                    this.contact = contactInfo {
                                        this.name = request.details.notify.name
                                    }
                                }
                            }
                            ledgerLedgerAccountService.createLedgerAccount(outbound1)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply1 = it }
                }
            }
            val outbound2 = CountDriverLicenseTotalsRequest.newBuilder().apply {
                setContext(request.context)
                setFilter(CountDriverLicenseTotalsRequestKt.filter {
                    this.createdAfter = reply1.ledgerAccount.audit.createdAt
                    this.labels.putAll(reply1.ledgerAccount.labelsMap)
                    this.ownerId = request.dutyStatus.dutyStatusId
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply2 = outbound2.let { value -> driversDriverLicenseReportingService.countDriverLicenseTotals(value) }
            scheduleDutyStatusExpiryResponse {
                this.dutyStatus = dutyStatus {
                    this.dutyStatusId = request.dutyStatusId
                    this.source = request.dutyStatus.source
                }
                this.outcome = ScheduleDutyStatusExpiryResponseKt.outcome {
                    this.status = reply2.result.status
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply2.result.message
                    this.status = "ok"
                }
            }
        }

    /** Suspends a duty status. */
    override suspend fun suspendDutyStatus(request: SuspendDutyStatusRequest): SuspendDutyStatusResponse {
        return handleSuspendDutyStatus(request)
    }

    /** Terminates a duty status. */
    override suspend fun terminateDutyStatus(request: TerminateDutyStatusRequest): TerminateDutyStatusResponse = handleTerminateDutyStatus(request)

    private fun ActivateDutyStatusRequest.toExplainDutyStatusCalculationFromActivateDutyStatus1(reply0: PreviewDutyStatusResponse): ExplainDutyStatusCalculationRequest {
        val source = this
        return explainDutyStatusCalculationRequest {
            this.context = source.context
            this.details = ExplainDutyStatusCalculationRequestKt.details {
                this.contact = contactInfo {
                    this.email = source.details.newOwner.email
                    this.preferredChannel = source.details.notifyContact.preferredChannel
                }
            }
            this.dutyStatus = dutyStatus {
                this.subject = driverLicenseRef {
                    this.displayName = source.details.newOwner.displayName
                    this.number = reply0.dutyStatus.subject.number
                }
            }
        }
    }

    private suspend fun handleActivateDutyStatus(request: ActivateDutyStatusRequest): ActivateDutyStatusResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusLifecycleService/ActivateDutyStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ActivateDutyStatus", request)
            log.atInfo().addKeyValue("operation", "ActivateDutyStatus").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = previewDutyStatusRequest {}.driversCopy {
                this.context = request.context
                this.details = PreviewDutyStatusRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.details.notifyContact.email
                        this.name = request.details.notifyContact.name
                        this.phone = request.details.notifyContact.phone
                    }
                }
            }
            val reply0 = driversDutyStatusValidationService.previewDutyStatus(outbound0)
            val outbound1 = request.toExplainDutyStatusCalculationFromActivateDutyStatus1(reply0)
            val reply1 = driversDutyStatusCalculationService.explainDutyStatusCalculation(outbound1)
            activateDutyStatusResponse {
                this.dutyStatus = dutyStatus {
                    this.correlationId = reply1.result.correlationId
                    this.dutyStatusId = request.dutyStatusId
                    this.sequence = reply1.dutyStatus.sequence
                    this.source = reply0.dutyStatus.source
                    this.subject = driverLicenseRef {
                        this.number = reply0.dutyStatus.subject.number
                    }
                }
                this.outcome = ActivateDutyStatusResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleExpireDutyStatus(request: ExpireDutyStatusRequest): ExpireDutyStatusResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusLifecycleService/ExpireDutyStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExpireDutyStatus", request)
            log.atInfo().addKeyValue("operation", "ExpireDutyStatus").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = watchDriversRequest {
                this.context = request.context
                this.subscriber = contactInfo {
                    this.email = request.details.newOwner.email
                    this.name = request.details.notifyContact.name
                    this.phone = request.details.notifyContact.phone
                    this.preferredChannel = request.details.notifyContact.preferredChannel
                }
            }
            driversDriverService.watchDrivers(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            expireDutyStatusResponse {
                this.dutyStatus = dutyStatus {
                    this.dutyStatusId = request.dutyStatusId
                }
                this.outcome = ExpireDutyStatusResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private data class ReinstateDutyStatus1Command(
        val context: RequestContext,
        val userContactName: PersonName,
        val userRelationshipManagerDisplayName: String,
        val userContactEmail: String
    ) {
        fun toRequest(): PreviewUserRequest {
            val command = this
            return previewUserRequest {
                this.context = command.context
                this.user = user {
                    this.contact = contactInfo {
                        this.email = command.userContactEmail
                        this.name = command.userContactName
                    }
                    this.relationshipManager = userRef {
                        this.displayName = command.userRelationshipManagerDisplayName
                    }
                }
            }
        }
    }

    private suspend fun handleReinstateDutyStatus(request: ReinstateDutyStatusRequest): ReinstateDutyStatusResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusLifecycleService/ReinstateDutyStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ReinstateDutyStatus", request)
            log.atInfo().addKeyValue("operation", "ReinstateDutyStatus").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = cloneAccessPolicyRequest {
                this.accessPolicy = accessPolicy {
                    this.owner = userRef {
                        this.email = request.details.notifyContact.email
                        this.userId = request.details.newOwner.userId
                    }
                }
                this.context = request.context
            }
            val reply0 = authorizationAccessPolicyService.cloneAccessPolicy(WireCopies.json(outbound0))
            val outbound1 = ReinstateDutyStatus1Command(
                context = request.context,
                userContactName = request.details.notifyContact.name,
                userRelationshipManagerDisplayName = reply0.accessPolicy.owner.displayName,
                userContactEmail = request.details.notifyContact.email
            ).toRequest()
            val reply1 = identityUserValidationService.previewUser(outbound1)
            val outbound2 = quoteDriverLicenseRequest {
                this.context = request.context
                this.details = QuoteDriverLicenseRequestKt.details {
                    this.destination = postalAddress {
                        this.postalCode = reply1.user.primaryAddress.postalCode
                    }
                }
                this.driverLicense = driverLicense {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.accessPolicy.audit.updatedBy
                    }
                    this.billingAddress = postalAddress {
                        this.region = reply1.user.primaryAddress.region
                    }
                    this.lines.addAll(listOf(driverLicenseLine {
                        this.amount = reply0.accessPolicy.discount.amount
                    }))
                }
            }
            val command2: Submission<QuoteDriverLicenseRequest> = Submission.Live(outbound2)
            val payload2 = when (val command = command2) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply2 = driversDriverLicenseCalculationService.quoteDriverLicense(payload2)
            reinstateDutyStatusResponse {
                this.dutyStatus = dutyStatus {
                    this.dutyStatusId = request.dutyStatusId
                    this.subject = driverLicenseRef {
                        this.number = reply1.user.passport.number
                    }
                }
                this.outcome = ReinstateDutyStatusResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = reply2.result.status
                }
            }
        }

    private suspend fun handleRenewDutyStatus(request: RenewDutyStatusRequest): RenewDutyStatusResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusLifecycleService/RenewDutyStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RenewDutyStatus", request)
            log.atInfo().addKeyValue("operation", "RenewDutyStatus").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = batchCreateAccessPoliciesRequest {
                this.context = request.context
                this.record = accessPolicy {
                    this.owner = userRef {
                        this.email = request.details.newOwner.email
                    }
                }
            }
            authorizationAccessPolicyService.batchCreateAccessPolicies(flowOf(outbound0))
            renewDutyStatusResponse {
                this.dutyStatus = dutyStatus {
                    this.dutyStatusId = request.dutyStatusId
                }
                this.outcome = RenewDutyStatusResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSuspendDutyStatus(request: SuspendDutyStatusRequest): SuspendDutyStatusResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusLifecycleService/SuspendDutyStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SuspendDutyStatus", request)
            log.atInfo().addKeyValue("operation", "SuspendDutyStatus").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                generatePartReportRequest {
                    this.context = request.context
                    this.reason = request.reason
                }
            }
            val reply0 = maintenancePartReportingService.generatePartReport(outbound0)
            suspendDutyStatusResponse {
                this.dutyStatus = dutyStatus {
                    this.dutyStatusId = request.dutyStatusId
                }
                this.outcome = SuspendDutyStatusResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleTerminateDutyStatus(request: TerminateDutyStatusRequest): TerminateDutyStatusResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusLifecycleService/TerminateDutyStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "TerminateDutyStatus", request)
            log.atInfo().addKeyValue("operation", "TerminateDutyStatus").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = shareFuelCardExportRequest {
                this.context = request.context
                this.details = ShareFuelCardExportRequestKt.details {
                    this.recipient = contactInfo {
                        this.phone = request.details.notifyContact.phone
                        this.preferredChannel = request.details.notifyContact.preferredChannel
                    }
                }
            }
            val reply0 = ClientRegistry.lookup("acme.fuel.v1.FuelCardExportService", FuelCardExportServiceGrpcKt.FuelCardExportServiceCoroutineStub::class.java).shareFuelCardExport(outbound0)
            val outbound1 = scheduleDriverExpiryRequest {
                this.context = request.context
                this.details = ScheduleDriverExpiryRequestKt.details {
                    this.notify = contactInfo {
                        this.email = reply0.fuelCard.contact.email
                        this.name = reply0.fuelCard.contact.name
                    }
                }
                this.driver = driver {
                    this.audit = auditInfo {
                        this.createdBy = reply0.fuelCard.audit.createdBy
                    }
                    this.relationshipManager = userRef {
                        this.displayName = request.details.newOwner.displayName
                    }
                }
            }
            val reply1 = driversDriverLifecycleService.scheduleDriverExpiry(WireCopies.reflective(outbound1))
            val outbound2 = findNotificationsRequest {
                this.context = request.context
                this.filter = FindNotificationsRequestKt.filter {
                    this.labels.putAll(reply0.fuelCard.labelsMap)
                    this.region = reply1.driver.primaryAddress.region
                }
            }
            val reply2 = workerForTerminateDutyStatus2.submit(outbound2)
            terminateDutyStatusResponse {
                this.dutyStatus = dutyStatus {
                    this.dutyStatusId = request.dutyStatusId
                }
                this.outcome = TerminateDutyStatusResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.message = reply0.outcome.message
                    this.referenceId = reply1.outcome.referenceId
                    this.status = reply0.outcome.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }
}
