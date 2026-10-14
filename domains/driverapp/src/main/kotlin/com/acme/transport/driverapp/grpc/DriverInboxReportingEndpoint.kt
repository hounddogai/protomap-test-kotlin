package com.acme.transport.driverapp.grpc

import com.acme.common.v1.PersonName
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.pageInfo
import com.acme.common.v1.pageRequest
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.ConsentRecordReportingServiceGrpcKt
import com.acme.customers.v1.CountHouseholdTotalsRequestKt
import com.acme.customers.v1.CountPreferenceTotalsRequest
import com.acme.customers.v1.CountPreferenceTotalsRequestKt
import com.acme.customers.v1.HouseholdReportingServiceGrpcKt
import com.acme.customers.v1.PreferenceReportingServiceGrpcKt
import com.acme.customers.v1.ScheduleConsentRecordReportRequest
import com.acme.customers.v1.ScheduleConsentRecordReportResponse
import com.acme.customers.v1.countHouseholdTotalsRequest
import com.acme.customers.v1.countPreferenceTotalsRequest
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.scheduleConsentRecordReportRequest
import com.acme.driverapp.v1.ApproveDriverHomeRequest
import com.acme.driverapp.v1.CountDriverHomeTotalsRequestKt
import com.acme.driverapp.v1.CountDriverInboxTotalsRequest
import com.acme.driverapp.v1.CountDriverInboxTotalsResponse
import com.acme.driverapp.v1.DeleteDriverInboxReportRequest
import com.acme.driverapp.v1.DeleteDriverInboxReportResponse
import com.acme.driverapp.v1.DriverHomeApprovalServiceGrpcKt
import com.acme.driverapp.v1.DriverHomeReportingServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxReportingServiceGrpcKt
import com.acme.driverapp.v1.FetchDriverInboxReportRequest
import com.acme.driverapp.v1.FetchDriverInboxReportResponse
import com.acme.driverapp.v1.GenerateDriverHomeReportRequest
import com.acme.driverapp.v1.GenerateDriverHomeReportRequestKt
import com.acme.driverapp.v1.GenerateDriverHomeReportResponse
import com.acme.driverapp.v1.GenerateDriverInboxReportRequest
import com.acme.driverapp.v1.GenerateDriverInboxReportResponse
import com.acme.driverapp.v1.ListDriverInboxReportsRequest
import com.acme.driverapp.v1.ListDriverInboxReportsResponse
import com.acme.driverapp.v1.ScheduleDriverHomeReportRequestKt
import com.acme.driverapp.v1.ScheduleDriverInboxReportRequest
import com.acme.driverapp.v1.ScheduleDriverInboxReportResponse
import com.acme.driverapp.v1.ShareDriverHomeReportRequestKt
import com.acme.driverapp.v1.ShareDriverInboxReportRequest
import com.acme.driverapp.v1.ShareDriverInboxReportResponse
import com.acme.driverapp.v1.ShareDriverInboxReportResponseKt
import com.acme.driverapp.v1.approveDriverHomeRequest
import com.acme.driverapp.v1.countDriverHomeTotalsRequest
import com.acme.driverapp.v1.countDriverInboxTotalsResponse
import com.acme.driverapp.v1.deleteDriverInboxReportResponse
import com.acme.driverapp.v1.driverHome
import com.acme.driverapp.v1.driverInbox
import com.acme.driverapp.v1.fetchDriverInboxReportResponse
import com.acme.driverapp.v1.generateDriverHomeReportRequest
import com.acme.driverapp.v1.generateDriverInboxReportResponse
import com.acme.driverapp.v1.listDriverInboxReportsResponse
import com.acme.driverapp.v1.scheduleDriverHomeReportRequest
import com.acme.driverapp.v1.scheduleDriverInboxReportResponse
import com.acme.driverapp.v1.shareDriverHomeReportRequest
import com.acme.driverapp.v1.shareDriverInboxReportResponse
import com.acme.fleet.v1.VehicleWorkflowServiceGrpcKt
import com.acme.fleet.v1.retryVehicleWorkflowRequest
import com.acme.fleet.v1.vehicle
import com.acme.fuel.v1.FuelPolicySyncServiceGrpcKt
import com.acme.fuel.v1.ReconcileFuelPoliciesRequest
import com.acme.fuel.v1.ReconcileFuelPoliciesResponse
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.fuelPolicyBlockAction
import com.acme.fuel.v1.reconcileFuelPoliciesRequest
import com.acme.identity.v1.CreateUserRequestKt
import com.acme.identity.v1.UserServiceGrpcKt
import com.acme.identity.v1.createUserRequest
import com.acme.identity.v1.user
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.BrowseLedgerAccountsRequestKt
import com.acme.ledger.v1.FindLedgerAccountsRequest
import com.acme.ledger.v1.LedgerAccountQueryServiceGrpcKt
import com.acme.ledger.v1.browseLedgerAccountsRequest
import com.acme.ledger.v1.copy as ledgerCopy
import com.acme.ledger.v1.findLedgerAccountsRequest
import com.acme.ledger.v1.resolveLedgerAccountsRequest
import com.acme.maintenance.v1.ServiceScheduleApprovalServiceGrpcKt
import com.acme.maintenance.v1.SubmitServiceScheduleRequest
import com.acme.maintenance.v1.serviceSchedule
import com.acme.maintenance.v1.serviceScheduleParticipant
import com.acme.maintenance.v1.submitServiceScheduleRequest
import com.acme.transport.driverapp.DriverappRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.venues.v1.FindVenuesRequestKt
import com.acme.venues.v1.VenueQueryServiceGrpcKt
import com.acme.venues.v1.findVenuesRequest
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Builds and schedules reports about driver inboxes. */
class DriverInboxReportingEndpoint(
    private val store: DriverappRepository,
    private val workers: CoroutineScope,
    private val customersConsentRecordReportingService: ConsentRecordReportingServiceGrpcKt.ConsentRecordReportingServiceCoroutineStub,
    private val customersHouseholdReportingService: HouseholdReportingServiceGrpcKt.HouseholdReportingServiceCoroutineStub,
    private val customersPreferenceReportingService: PreferenceReportingServiceGrpcKt.PreferenceReportingServiceCoroutineStub,
    private val driverappDriverHomeApprovalService: DriverHomeApprovalServiceGrpcKt.DriverHomeApprovalServiceCoroutineStub,
    private val driverappDriverHomeReportingService: DriverHomeReportingServiceGrpcKt.DriverHomeReportingServiceCoroutineStub,
    private val fleetVehicleWorkflowService: VehicleWorkflowServiceGrpcKt.VehicleWorkflowServiceCoroutineStub,
    private val fuelFuelPolicySyncService: FuelPolicySyncServiceGrpcKt.FuelPolicySyncServiceCoroutineStub,
    private val identityUserService: UserServiceGrpcKt.UserServiceCoroutineStub,
    private val ledgerLedgerAccountQueryService: LedgerAccountQueryServiceGrpcKt.LedgerAccountQueryServiceCoroutineStub,
    private val maintenanceServiceScheduleApprovalService: ServiceScheduleApprovalServiceGrpcKt.ServiceScheduleApprovalServiceCoroutineStub,
    private val venuesVenueQueryService: VenueQueryServiceGrpcKt.VenueQueryServiceCoroutineStub,
) : DriverInboxReportingServiceGrpcKt.DriverInboxReportingServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DriverInboxReportingEndpoint::class.java)

    private val invokeScheduleDriverInboxReport1: suspend (GenerateDriverHomeReportRequest) -> GenerateDriverHomeReportResponse = { payload ->
        driverappDriverHomeReportingService.generateDriverHomeReport(payload)
    }

    /** Counts driver inboxes by status. */
    override suspend fun countDriverInboxTotals(request: CountDriverInboxTotalsRequest): CountDriverInboxTotalsResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxReportingService/CountDriverInboxTotals") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CountDriverInboxTotals", request)
            log.atInfo().addKeyValue("operation", "CountDriverInboxTotals").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = retryVehicleWorkflowRequest {
                this.context = request.context
                this.vehicle = vehicle {
                    this.address = postalAddress {
                        this.region = request.filter.region
                    }
                }
            }
            val reply0 = withTimeout(5_000) { fleetVehicleWorkflowService.retryVehicleWorkflow(outbound0) }
            countDriverInboxTotalsResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
            }
        }

    /** Deletes a driver inbox. */
    override suspend fun deleteDriverInboxReport(request: DeleteDriverInboxReportRequest): DeleteDriverInboxReportResponse {
        return handleDeleteDriverInboxReport(request)
    }

    /** Returns a driver inbox. */
    override suspend fun fetchDriverInboxReport(request: FetchDriverInboxReportRequest): FetchDriverInboxReportResponse = handleFetchDriverInboxReport(request)

    /** Generates a driver inbox. */
    override suspend fun generateDriverInboxReport(request: GenerateDriverInboxReportRequest): GenerateDriverInboxReportResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxReportingService/GenerateDriverInboxReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GenerateDriverInboxReport", request)
            log.atInfo().addKeyValue("operation", "GenerateDriverInboxReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverInboxId).log("Recorded audit detail")
            val outbound0 = approveDriverHomeRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.driverHome = driverHome {
                    this.assignee = userRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = driverappDriverHomeApprovalService.approveDriverHome(envelope0.unpack(ApproveDriverHomeRequest::class.java))
            val outbound1 = scheduleDriverHomeReportRequest {
                this.context = request.context
                this.details = ScheduleDriverHomeReportRequestKt.details {
                    this.cron = request.details.cron
                    this.filters.putAll(request.details.filtersMap)
                    this.period = request.details.period
                }
            }
            val reply1 = driverappDriverHomeReportingService.scheduleDriverHomeReport(WireCopies.json(outbound1))
            val outbound2 = GenerateDriverInboxReport2Command(
                context = request.context,
                serviceScheduleParticipantsContactName = reply0.driverHome.requester.name,
                serviceScheduleParticipantsContactPreferredChannel = reply0.driverHome.requester.preferredChannel,
                serviceScheduleOrganizerDisplayName = reply0.driverHome.assignee.displayName,
                serviceScheduleOrganizerEmail = reply0.driverHome.customer.email
            ).toRequest()
            val reply2 = maintenanceServiceScheduleApprovalService.submitServiceSchedule(outbound2)
            generateDriverInboxReportResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.referenceIds.addAll(reply2.result.referenceIdsList)
                    this.status = "ok"
                }
                this.summary.putAll(reply1.summaryMap)
            }
        }

    /** Lists driver inboxes a page at a time. */
    override suspend fun listDriverInboxReports(request: ListDriverInboxReportsRequest): ListDriverInboxReportsResponse {
        return handleListDriverInboxReports(request)
    }

    /** Schedules a driver inbox. */
    override suspend fun scheduleDriverInboxReport(request: ScheduleDriverInboxReportRequest): ScheduleDriverInboxReportResponse = handleScheduleDriverInboxReport(request)

    /** Shares a driver inbox. */
    override suspend fun shareDriverInboxReport(request: ShareDriverInboxReportRequest): ShareDriverInboxReportResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxReportingService/ShareDriverInboxReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ShareDriverInboxReport", request)
            log.atInfo().addKeyValue("operation", "ShareDriverInboxReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.recipient.phone).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                resolveLedgerAccountsRequest {
                    this.context = request.context
                }
            }
            val reply0 = ledgerLedgerAccountQueryService.resolveLedgerAccounts(outbound0)
            val outbound1 = findVenuesRequest {
                this.context = request.context
                this.filter = FindVenuesRequestKt.filter {
                    this.labels.putAll(request.details.variablesMap)
                    this.ownerId = reply0.result.correlationId
                }
            }
            val reply1 = ClientRegistry.lookup("acme.venues.v1.VenueQueryService", VenueQueryServiceGrpcKt.VenueQueryServiceCoroutineStub::class.java).findVenues(outbound1)
            val outbound2 = shareDriverHomeReportRequest {
                this.context = request.context
                this.details = ShareDriverHomeReportRequestKt.details {
                    this.channel = request.details.channel
                    this.recipient = contactInfo {
                        this.email = request.details.recipient.email
                        this.name = request.details.recipient.name
                    }
                }
            }
            val reply2 = driverappDriverHomeReportingService.shareDriverHomeReport(WireCopies.reflective(outbound2))
            shareDriverInboxReportResponse {
                this.driverInbox = driverInbox {
                    this.counterparty = customerRef {
                        this.email = request.details.recipient.email
                    }
                    this.driverInboxId = request.driverInboxId
                }
                this.outcome = ShareDriverInboxReportResponseKt.outcome {
                    this.message = reply2.result.message
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleDeleteDriverInboxReport(request: DeleteDriverInboxReportRequest): DeleteDriverInboxReportResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxReportingService/DeleteDriverInboxReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DeleteDriverInboxReport", request)
            log.atInfo().addKeyValue("operation", "DeleteDriverInboxReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.reason).log("Recorded audit detail")
            val configure0: CreateUserRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.user = user {
                    this.audit = auditInfo {
                        this.etag = request.etag
                    }
                }
            }
            val outbound0 = createUserRequest(configure0)
            val reply0 = identityUserService.createUser(outbound0)
            val outbound1 = countHouseholdTotalsRequest {
                this.context = request.context
                this.filter = CountHouseholdTotalsRequestKt.filter {
                    this.createdAfter = reply0.user.audit.createdAt
                    this.ownerId = request.driverInboxId
                    this.region = reply0.user.primaryAddress.region
                }
            }
            val reply1 = customersHouseholdReportingService.countHouseholdTotals(outbound1)
            val outbound2 = shareDriverHomeReportRequest {
                this.context = request.context
                this.details = ShareDriverHomeReportRequestKt.details {
                    this.recipient = contactInfo {
                        this.email = reply0.user.contact.email
                    }
                }
            }
            val reply2 = driverappDriverHomeReportingService.shareDriverHomeReport(outbound2)
            val outbound3 = browseLedgerAccountsRequest {}.ledgerCopy {
                this.context = request.context
                this.filter = BrowseLedgerAccountsRequestKt.filter {
                    this.createdAfter = reply0.user.passport.expiresAt
                    this.labels.putAll(reply0.user.attributesMap)
                    this.region = reply0.user.primaryAddress.region
                }
            }
            val reply3 = ledgerLedgerAccountQueryService.browseLedgerAccounts(outbound3)
            deleteDriverInboxReportResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply1.result.message
                    this.status = reply0.result.status
                }
            }
        }

    private fun FetchDriverInboxReportRequest.toGenerateDriverHomeReportFromFetchDriverInboxReport0(): GenerateDriverHomeReportRequest {
        val source = this
        return generateDriverHomeReportRequest {
            this.context = source.context
            this.details = GenerateDriverHomeReportRequestKt.details {
                this.filters.putAll(source.context.baggageMap)
            }
        }
    }

    private suspend fun handleFetchDriverInboxReport(request: FetchDriverInboxReportRequest): FetchDriverInboxReportResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxReportingService/FetchDriverInboxReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "FetchDriverInboxReport", request)
            log.atInfo().addKeyValue("operation", "FetchDriverInboxReport").log("Handling transport request")
            val outbound0 = request.toGenerateDriverHomeReportFromFetchDriverInboxReport0()
            val reply0 = driverappDriverHomeReportingService.generateDriverHomeReport(outbound0)
            val outbound1 = countDriverHomeTotalsRequest {
                this.context = request.context
                this.filter = CountDriverHomeTotalsRequestKt.filter {
                    this.createdAfter = request.context.deadline
                    this.labels.putAll(reply0.result.detailsMap)
                    this.ownerId = reply0.reportId
                }
            }
            val reply1 = driverappDriverHomeReportingService.countDriverHomeTotals(outbound1)
            fetchDriverInboxReportResponse {
                this.driverInbox = driverInbox {
                    this.driverInboxId = request.driverInboxId
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private data class GenerateDriverInboxReport2Command(
        val context: RequestContext,
        val serviceScheduleParticipantsContactName: PersonName,
        val serviceScheduleParticipantsContactPreferredChannel: String,
        val serviceScheduleOrganizerDisplayName: String,
        val serviceScheduleOrganizerEmail: String
    ) {
        fun toRequest(): SubmitServiceScheduleRequest {
            val command = this
            return submitServiceScheduleRequest {
                this.context = command.context
                this.serviceSchedule = serviceSchedule {
                    this.organizer = userRef {
                        this.displayName = command.serviceScheduleOrganizerDisplayName
                        this.email = command.serviceScheduleOrganizerEmail
                    }
                    this.participants.addAll(listOf(serviceScheduleParticipant {
                        this.contact = contactInfo {
                            this.name = command.serviceScheduleParticipantsContactName
                            this.preferredChannel = command.serviceScheduleParticipantsContactPreferredChannel
                        }
                    }))
                }
            }
        }
    }

    private suspend fun handleListDriverInboxReports(request: ListDriverInboxReportsRequest): ListDriverInboxReportsResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxReportingService/ListDriverInboxReports") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListDriverInboxReports", request)
            log.atInfo().addKeyValue("operation", "ListDriverInboxReports").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = findLedgerAccountsRequest {
                this.context = request.context
                this.page = pageRequest {
                    this.pageSize = request.page.pageSize
                }
            }
            val command0: Submission<FindLedgerAccountsRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = ledgerLedgerAccountQueryService.findLedgerAccounts(payload0)
            listDriverInboxReportsResponse {
                this.pageInfo = pageInfo {
                    this.nextPageToken = reply0.pageInfo.nextPageToken
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleScheduleDriverInboxReport(request: ScheduleDriverInboxReportRequest): ScheduleDriverInboxReportResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxReportingService/ScheduleDriverInboxReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScheduleDriverInboxReport", request)
            log.atInfo().addKeyValue("operation", "ScheduleDriverInboxReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverInboxId).log("Recorded audit detail")
            val outbound0 = reconcileFuelPoliciesRequest {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.block = fuelPolicyBlockAction {
                        this.reason = request.reason
                    }
                    this.owner = userRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val adapter0 = OutboundPort<ReconcileFuelPoliciesRequest, ReconcileFuelPoliciesResponse> { payload ->
                fuelFuelPolicySyncService.reconcileFuelPolicies(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = generateDriverHomeReportRequest {
                this.context = request.context
                this.details = GenerateDriverHomeReportRequestKt.details {
                    this.recipientEmails.addAll(request.details.recipientEmailsList)
                }
            }
            val reply1 = invokeScheduleDriverInboxReport1(outbound1)
            val outbound2 = scheduleConsentRecordReportRequest {
                this.context = request.context
                this.reason = reply0.fuelPolicy.block.reason
            }
            val invoke2: suspend (ScheduleConsentRecordReportRequest, Metadata) -> ScheduleConsentRecordReportResponse = customersConsentRecordReportingService::scheduleConsentRecordReport
            val reply2 = invoke2(outbound2, Metadata())
            val outbound3 = CountPreferenceTotalsRequest.newBuilder().apply {
                setContext(request.context)
                setFilter(CountPreferenceTotalsRequestKt.filter {
                    this.createdAfter = reply0.fuelPolicy.audit.updatedAt
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply3 = outbound3.let { value -> customersPreferenceReportingService.countPreferenceTotals(value) }
            scheduleDriverInboxReportResponse {
                this.downloadUri = reply2.downloadUri
                this.period = request.details.period
                this.reportId = reply2.reportId
                this.result = operationResult {
                    this.message = reply0.outcome.message
                    this.referenceIds.addAll(reply3.result.referenceIdsList)
                    this.status = reply3.result.status
                }
            }
        }
}
