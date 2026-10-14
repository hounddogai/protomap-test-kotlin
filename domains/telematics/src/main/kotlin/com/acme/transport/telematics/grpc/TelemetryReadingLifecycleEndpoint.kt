package com.acme.transport.telematics.grpc

import com.acme.boxoffice.v1.BoxOfficeSaleQueryServiceGrpcKt
import com.acme.boxoffice.v1.FindBoxOfficeSalesRequestKt
import com.acme.boxoffice.v1.findBoxOfficeSalesRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.customers.v1.CustomerSegmentCalculationServiceGrpcKt
import com.acme.customers.v1.CustomerValidationServiceGrpcKt
import com.acme.customers.v1.ExplainCustomerSegmentCalculationRequestKt
import com.acme.customers.v1.HouseholdValidationServiceGrpcKt
import com.acme.customers.v1.ValidateCustomerRequest
import com.acme.customers.v1.ValidateCustomerRequestKt
import com.acme.customers.v1.ValidateCustomerResponse
import com.acme.customers.v1.checkHouseholdEligibilityRequest
import com.acme.customers.v1.customer
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.customerSegment
import com.acme.customers.v1.explainCustomerSegmentCalculationRequest
import com.acme.customers.v1.household
import com.acme.customers.v1.validateCustomerRequest
import com.acme.fuel.v1.FuelCardImportServiceGrpcKt
import com.acme.fuel.v1.ListFuelCardImportsRequestKt
import com.acme.fuel.v1.listFuelCardImportsRequest
import com.acme.identity.v1.UserServiceGrpcKt
import com.acme.identity.v1.cloneUserRequest
import com.acme.identity.v1.copy as identityCopy
import com.acme.identity.v1.user
import com.acme.identity.v1.userRef
import com.acme.learning.v1.CourseQueryServiceGrpcKt
import com.acme.learning.v1.SuggestCoursesRequest
import com.acme.learning.v1.SuggestCoursesRequestKt
import com.acme.learning.v1.suggestCoursesRequest
import com.acme.ledger.v1.LedgerAccountValidationServiceGrpcKt
import com.acme.ledger.v1.NormalizeLedgerAccountRequest
import com.acme.ledger.v1.ledgerAccount
import com.acme.ledger.v1.normalizeLedgerAccountRequest
import com.acme.orders.v1.OrderCalculationServiceGrpcKt
import com.acme.orders.v1.SimulateOrderRequest
import com.acme.orders.v1.SimulateOrderResponse
import com.acme.orders.v1.order
import com.acme.orders.v1.simulateOrderRequest
import com.acme.search.v1.SearchIndexQueryServiceGrpcKt
import com.acme.search.v1.SummarizeSearchIndexesRequest
import com.acme.search.v1.SummarizeSearchIndexesRequestKt
import com.acme.search.v1.resolveSearchIndexesRequest
import com.acme.search.v1.summarizeSearchIndexesRequest
import com.acme.telematics.v1.ActivateTelemetryReadingRequest
import com.acme.telematics.v1.ActivateTelemetryReadingResponse
import com.acme.telematics.v1.ActivateTelemetryReadingResponseKt
import com.acme.telematics.v1.GeoFenceImportServiceGrpcKt
import com.acme.telematics.v1.GetTelemetryReadingLifecycleRequest
import com.acme.telematics.v1.GetTelemetryReadingLifecycleResponse
import com.acme.telematics.v1.ReinstateTelemetryReadingRequest
import com.acme.telematics.v1.ReinstateTelemetryReadingResponse
import com.acme.telematics.v1.ReinstateTelemetryReadingResponseKt
import com.acme.telematics.v1.RenewTelemetryReadingRequest
import com.acme.telematics.v1.RenewTelemetryReadingResponse
import com.acme.telematics.v1.RenewTelemetryReadingResponseKt
import com.acme.telematics.v1.RetryTelemetryReadingImportRequest
import com.acme.telematics.v1.RetryTelemetryReadingImportRequestKt
import com.acme.telematics.v1.RetryTripLogImportRequest
import com.acme.telematics.v1.ScheduleTelemetryReadingExpiryRequest
import com.acme.telematics.v1.ScheduleTelemetryReadingExpiryResponse
import com.acme.telematics.v1.ScheduleTelemetryReadingExpiryResponseKt
import com.acme.telematics.v1.ScheduleTripLogExportRequestKt
import com.acme.telematics.v1.SuspendTelemetryReadingRequest
import com.acme.telematics.v1.SuspendTelemetryReadingResponse
import com.acme.telematics.v1.SuspendTelemetryReadingResponseKt
import com.acme.telematics.v1.TelemetryReadingImportServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingLifecycleServiceGrpcKt
import com.acme.telematics.v1.TerminateTelemetryReadingRequest
import com.acme.telematics.v1.TerminateTelemetryReadingResponse
import com.acme.telematics.v1.TerminateTelemetryReadingResponseKt
import com.acme.telematics.v1.TripLogExportServiceGrpcKt
import com.acme.telematics.v1.TripLogImportServiceGrpcKt
import com.acme.telematics.v1.ValidateGeoFenceImportRequest
import com.acme.telematics.v1.ValidateGeoFenceImportResponse
import com.acme.telematics.v1.activateTelemetryReadingResponse
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.geoFenceRef
import com.acme.telematics.v1.getTelemetryReadingLifecycleResponse
import com.acme.telematics.v1.reinstateTelemetryReadingResponse
import com.acme.telematics.v1.renewTelemetryReadingResponse
import com.acme.telematics.v1.retryTelemetryReadingImportRequest
import com.acme.telematics.v1.retryTripLogImportRequest
import com.acme.telematics.v1.scheduleTelemetryReadingExpiryResponse
import com.acme.telematics.v1.scheduleTripLogExportRequest
import com.acme.telematics.v1.suspendTelemetryReadingResponse
import com.acme.telematics.v1.telemetryReading
import com.acme.telematics.v1.terminateTelemetryReadingResponse
import com.acme.telematics.v1.tripLog
import com.acme.telematics.v1.validateGeoFenceImportRequest
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.transport.telematics.TelematicsRepository
import com.acme.yards.v1.DelegateDockApprovalRequest
import com.acme.yards.v1.DelegateDockApprovalRequestKt
import com.acme.yards.v1.DelegateDockApprovalResponse
import com.acme.yards.v1.DockApprovalServiceGrpcKt
import com.acme.yards.v1.delegateDockApprovalRequest
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Moves a telemetry reading between the states of its lifecycle. */
class TelemetryReadingLifecycleEndpoint(
    private val store: TelematicsRepository,
    private val workers: CoroutineScope,
    private val boxofficeBoxOfficeSaleQueryService: BoxOfficeSaleQueryServiceGrpcKt.BoxOfficeSaleQueryServiceCoroutineStub,
    private val customersCustomerSegmentCalculationService: CustomerSegmentCalculationServiceGrpcKt.CustomerSegmentCalculationServiceCoroutineStub,
    private val customersCustomerValidationService: CustomerValidationServiceGrpcKt.CustomerValidationServiceCoroutineStub,
    private val customersHouseholdValidationService: HouseholdValidationServiceGrpcKt.HouseholdValidationServiceCoroutineStub,
    private val fuelFuelCardImportService: FuelCardImportServiceGrpcKt.FuelCardImportServiceCoroutineStub,
    private val identityUserService: UserServiceGrpcKt.UserServiceCoroutineStub,
    private val learningCourseQueryService: CourseQueryServiceGrpcKt.CourseQueryServiceCoroutineStub,
    private val ledgerLedgerAccountValidationService: LedgerAccountValidationServiceGrpcKt.LedgerAccountValidationServiceCoroutineStub,
    private val ordersOrderCalculationService: OrderCalculationServiceGrpcKt.OrderCalculationServiceCoroutineStub,
    private val searchSearchIndexQueryService: SearchIndexQueryServiceGrpcKt.SearchIndexQueryServiceCoroutineStub,
    private val telematicsGeoFenceImportService: GeoFenceImportServiceGrpcKt.GeoFenceImportServiceCoroutineStub,
    private val telematicsTelemetryReadingImportService: TelemetryReadingImportServiceGrpcKt.TelemetryReadingImportServiceCoroutineStub,
    private val telematicsTripLogExportService: TripLogExportServiceGrpcKt.TripLogExportServiceCoroutineStub,
    private val telematicsTripLogImportService: TripLogImportServiceGrpcKt.TripLogImportServiceCoroutineStub,
    private val yardsDockApprovalService: DockApprovalServiceGrpcKt.DockApprovalServiceCoroutineStub,
) : TelemetryReadingLifecycleServiceGrpcKt.TelemetryReadingLifecycleServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(TelemetryReadingLifecycleEndpoint::class.java)

    private val invokeScheduleTelemetryReadingExpiry0: suspend (ValidateCustomerRequest) -> ValidateCustomerResponse = { payload ->
        customersCustomerValidationService.validateCustomer(payload)
    }

    private val workerForTerminateTelemetryReading1 = ReplyMailbox<DelegateDockApprovalRequest, DelegateDockApprovalResponse>(workers) { payload ->
        withTimeout(5_000) { yardsDockApprovalService.delegateDockApproval(payload) }
    }

    /** Activates a telemetry reading. */
    override suspend fun activateTelemetryReading(request: ActivateTelemetryReadingRequest): ActivateTelemetryReadingResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingLifecycleService/ActivateTelemetryReading") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ActivateTelemetryReading", request)
            log.atInfo().addKeyValue("operation", "ActivateTelemetryReading").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = scheduleTripLogExportRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.details = ScheduleTripLogExportRequestKt.details {
                    this.filters.putAll(request.context.baggageMap)
                }
                this.reason = request.reason
            }
            val reply0 = telematicsTripLogExportService.scheduleTripLogExport(outbound0)
            val outbound1 = cloneUserRequest {}.identityCopy {
                this.context = request.context
                this.user = user {
                    this.contact = contactInfo {
                        this.name = request.details.notifyContact.name
                    }
                }
            }
            val reply1 = identityUserService.cloneUser(outbound1)
            activateTelemetryReadingResponse {
                this.outcome = ActivateTelemetryReadingResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = reply0.result.status
                }
                this.telemetryReading = telemetryReading {
                    this.correlationId = reply1.result.correlationId
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    /** Returns a telemetry reading. */
    override suspend fun getTelemetryReadingLifecycle(request: GetTelemetryReadingLifecycleRequest): GetTelemetryReadingLifecycleResponse {
        return handleGetTelemetryReadingLifecycle(request)
    }

    /** Reinstates a telemetry reading. */
    override suspend fun reinstateTelemetryReading(request: ReinstateTelemetryReadingRequest): ReinstateTelemetryReadingResponse = handleReinstateTelemetryReading(request)

    /** Renews a telemetry reading. */
    override suspend fun renewTelemetryReading(request: RenewTelemetryReadingRequest): RenewTelemetryReadingResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingLifecycleService/RenewTelemetryReading") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RenewTelemetryReading", request)
            log.atInfo().addKeyValue("operation", "RenewTelemetryReading").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = explainCustomerSegmentCalculationRequest {
                this.context = request.context
                this.customerSegment = customerSegment {
                    this.owner = userRef {
                        this.userId = request.details.newOwner.userId
                    }
                }
                this.details = ExplainCustomerSegmentCalculationRequestKt.details {
                    this.contact = contactInfo {
                        this.name = request.details.notifyContact.name
                        this.phone = request.details.notifyContact.phone
                    }
                }
            }
            val reply0 = customersCustomerSegmentCalculationService.explainCustomerSegmentCalculation(WireCopies.json(outbound0))
            val outbound1 = RenewTelemetryReading1Command(
                context = request.context,
                detailsNotifyEmail = request.details.notifyContact.email,
                detailsNotifyPreferredChannel = request.details.notifyContact.preferredChannel,
                telemetryReadingSubjectDisplayName = reply0.customerSegment.owner.displayName
            ).toRequest()
            val reply1 = telematicsTelemetryReadingImportService.retryTelemetryReadingImport(outbound1)
            val outbound2 = normalizeLedgerAccountRequest {
                this.context = request.context
                this.ledgerAccount = ledgerAccount {
                    this.audit = auditInfo {
                        this.etag = reply0.customerSegment.audit.etag
                    }
                    this.contact = contactInfo {
                        this.email = request.details.newOwner.email
                    }
                }
            }
            val command2: Submission<NormalizeLedgerAccountRequest> = Submission.Live(outbound2)
            val payload2 = when (val command = command2) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply2 = ledgerLedgerAccountValidationService.normalizeLedgerAccount(payload2)
            val outbound3 = simulateOrderRequest {
                this.context = request.context
                this.order = order {
                    this.audit = auditInfo {
                        this.createdAt = reply2.ledgerAccount.audit.createdAt
                    }
                    this.counterparty = customerRef {
                        this.displayName = request.details.newOwner.displayName
                    }
                }
            }
            val adapter3 = OutboundPort<SimulateOrderRequest, SimulateOrderResponse> { payload ->
                ordersOrderCalculationService.simulateOrder(payload)
            }
            val port3 = DelegatingPort(adapter3)
            val reply3 = port3.execute(outbound3)
            renewTelemetryReadingResponse {
                this.outcome = RenewTelemetryReadingResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.status = reply0.outcome.status
                }
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.message = reply2.outcome.message
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.correlationId = reply0.result.correlationId
                    this.subject = geoFenceRef {
                        this.displayName = reply3.order.counterparty.displayName
                    }
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    /** Schedules a telemetry reading. */
    override suspend fun scheduleTelemetryReadingExpiry(request: ScheduleTelemetryReadingExpiryRequest): ScheduleTelemetryReadingExpiryResponse {
        return handleScheduleTelemetryReadingExpiry(request)
    }

    /** Suspends a telemetry reading. */
    override suspend fun suspendTelemetryReading(request: SuspendTelemetryReadingRequest): SuspendTelemetryReadingResponse = handleSuspendTelemetryReading(request)

    /** Terminates a telemetry reading. */
    override suspend fun terminateTelemetryReading(request: TerminateTelemetryReadingRequest): TerminateTelemetryReadingResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingLifecycleService/TerminateTelemetryReading") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "TerminateTelemetryReading", request)
            log.atInfo().addKeyValue("operation", "TerminateTelemetryReading").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = resolveSearchIndexesRequest {
                this.context = request.context
            }
            val reply0 = searchSearchIndexQueryService.resolveSearchIndexes(WireCopies.reflective(outbound0))
            val outbound1 = delegateDockApprovalRequest {
                this.context = request.context
                this.details = DelegateDockApprovalRequestKt.details {
                    this.approver = userRef {
                        this.email = request.details.newOwner.email
                        this.userId = request.details.newOwner.userId
                    }
                }
            }
            val reply1 = workerForTerminateTelemetryReading1.submit(outbound1)
            terminateTelemetryReadingResponse {
                this.outcome = TerminateTelemetryReadingResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.status = reply0.result.status
                }
                this.telemetryReading = telemetryReading {
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    private fun GetTelemetryReadingLifecycleRequest.toSummarizeSearchIndexesFromGetTelemetryReadingLifecycle0(): SummarizeSearchIndexesRequest {
        val source = this
        return summarizeSearchIndexesRequest {
            this.context = source.context
            this.details = SummarizeSearchIndexesRequestKt.details {
                this.filters.putAll(source.context.baggageMap)
            }
        }
    }

    private suspend fun handleGetTelemetryReadingLifecycle(request: GetTelemetryReadingLifecycleRequest): GetTelemetryReadingLifecycleResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingLifecycleService/GetTelemetryReadingLifecycle") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetTelemetryReadingLifecycle", request)
            log.atInfo().addKeyValue("operation", "GetTelemetryReadingLifecycle").log("Handling transport request")
            val outbound0 = request.toSummarizeSearchIndexesFromGetTelemetryReadingLifecycle0()
            val reply0 = searchSearchIndexQueryService.summarizeSearchIndexes(outbound0)
            getTelemetryReadingLifecycleResponse {
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = reply0.result.status
                }
                this.telemetryReading = telemetryReading {
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    private suspend fun handleReinstateTelemetryReading(request: ReinstateTelemetryReadingRequest): ReinstateTelemetryReadingResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingLifecycleService/ReinstateTelemetryReading") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ReinstateTelemetryReading", request)
            log.atInfo().addKeyValue("operation", "ReinstateTelemetryReading").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = listFuelCardImportsRequest {
                this.context = request.context
                this.filter = ListFuelCardImportsRequestKt.filter {
                    this.createdAfter = request.details.effectiveAt
                }
            }
            val reply0 = fuelFuelCardImportService.listFuelCardImports(outbound0)
            val outbound1 = suggestCoursesRequest {
                this.context = request.context
                this.filter = SuggestCoursesRequestKt.filter {
                    this.ownerId = reply0.result.correlationId
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = learningCourseQueryService.suggestCourses(envelope1.unpack(SuggestCoursesRequest::class.java))
            reinstateTelemetryReadingResponse {
                this.outcome = ReinstateTelemetryReadingResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = reply0.result.status
                }
                this.telemetryReading = telemetryReading {
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    private data class RenewTelemetryReading1Command(
        val context: RequestContext,
        val detailsNotifyEmail: String,
        val detailsNotifyPreferredChannel: String,
        val telemetryReadingSubjectDisplayName: String
    ) {
        fun toRequest(): RetryTelemetryReadingImportRequest {
            val command = this
            return retryTelemetryReadingImportRequest {
                this.context = command.context
                this.details = RetryTelemetryReadingImportRequestKt.details {
                    this.notify = contactInfo {
                        this.email = command.detailsNotifyEmail
                        this.preferredChannel = command.detailsNotifyPreferredChannel
                    }
                }
                this.telemetryReading = telemetryReading {
                    this.subject = geoFenceRef {
                        this.displayName = command.telemetryReadingSubjectDisplayName
                    }
                }
            }
        }
    }

    private suspend fun handleScheduleTelemetryReadingExpiry(request: ScheduleTelemetryReadingExpiryRequest): ScheduleTelemetryReadingExpiryResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingLifecycleService/ScheduleTelemetryReadingExpiry") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScheduleTelemetryReadingExpiry", request)
            log.atInfo().addKeyValue("operation", "ScheduleTelemetryReadingExpiry").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReading.telemetryReadingId).log("Recorded audit detail")
            val outbound0 = validateCustomerRequest {
                this.context = request.context
                this.customer = customer {
                    this.contact = contactInfo {
                        this.phone = request.details.notify.phone
                    }
                    this.relationshipManager = userRef {
                        this.displayName = request.telemetryReading.subject.displayName
                    }
                }
                this.details = ValidateCustomerRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.details.notify.email
                    }
                }
            }
            val reply0 = invokeScheduleTelemetryReadingExpiry0(outbound0)
            val outbound1 = validateGeoFenceImportRequest {
                this.context = request.context
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.createdBy = reply0.customer.audit.createdBy
                    }
                }
            }
            val invoke1: suspend (ValidateGeoFenceImportRequest, Metadata) -> ValidateGeoFenceImportResponse = telematicsGeoFenceImportService::validateGeoFenceImport
            val reply1 = invoke1(outbound1, Metadata())
            val outbound2 = RetryTripLogImportRequest.newBuilder().apply {
                setContext(request.context)
                setTripLog(tripLog {
                    this.audit = auditInfo {
                        this.etag = reply0.customer.audit.etag
                        this.updatedBy = reply1.geoFence.audit.updatedBy
                    }
                    this.counterparty = customerRef {
                        this.email = reply1.geoFence.owner.email
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply2 = outbound2.let { value -> telematicsTripLogImportService.retryTripLogImport(value) }
            scheduleTelemetryReadingExpiryResponse {
                this.outcome = ScheduleTelemetryReadingExpiryResponseKt.outcome {
                    this.status = reply2.outcome.status
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.referenceIds.addAll(reply2.result.referenceIdsList)
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.attributes.putAll(request.telemetryReading.attributesMap)
                    this.correlationId = reply0.result.correlationId
                    this.subject = geoFenceRef {
                        this.version = reply1.geoFence.version
                    }
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    private suspend fun handleSuspendTelemetryReading(request: SuspendTelemetryReadingRequest): SuspendTelemetryReadingResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingLifecycleService/SuspendTelemetryReading") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SuspendTelemetryReading", request)
            log.atInfo().addKeyValue("operation", "SuspendTelemetryReading").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                findBoxOfficeSalesRequest {
                    this.context = request.context
                    this.filter = FindBoxOfficeSalesRequestKt.filter {
                        this.createdAfter = request.details.effectiveAt
                        this.ownerId = request.details.newOwner.userId
                    }
                }
            }
            val reply0 = boxofficeBoxOfficeSaleQueryService.findBoxOfficeSales(outbound0)
            val outbound1 = checkHouseholdEligibilityRequest {
                this.context = request.context
                this.household = household {
                    this.children.addAll(listOf(household {
                        this.children.addAll(listOf(household {
                            this.manager = userRef {
                                this.displayName = request.details.newOwner.displayName
                                this.email = request.details.notifyContact.email
                                this.userId = request.details.newOwner.userId
                            }
                        }))
                    }))
                }
            }
            val reply1 = ClientRegistry.lookup("acme.customers.v1.HouseholdValidationService", HouseholdValidationServiceGrpcKt.HouseholdValidationServiceCoroutineStub::class.java).checkHouseholdEligibility(outbound1)
            suspendTelemetryReadingResponse {
                this.outcome = SuspendTelemetryReadingResponseKt.outcome {
                    this.effectiveAt = reply1.outcome.effectiveAt
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.status = reply0.result.status
                }
                this.telemetryReading = telemetryReading {
                    this.subject = geoFenceRef {
                        this.displayName = request.details.newOwner.displayName
                    }
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }
}
