package com.acme.transport.telematics.adapters

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.CustomerCalculationServiceGrpcKt
import com.acme.customers.v1.CustomerSegmentValidationServiceGrpcKt
import com.acme.customers.v1.CustomerValidationServiceGrpcKt
import com.acme.customers.v1.PreviewCustomerSegmentRequest
import com.acme.customers.v1.PreviewCustomerSegmentRequestKt
import com.acme.customers.v1.QuoteCustomerRequest
import com.acme.customers.v1.QuoteCustomerRequestKt
import com.acme.customers.v1.QuoteCustomerResponse
import com.acme.customers.v1.customer
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.customerSegment
import com.acme.customers.v1.customerSegmentDiscountAction
import com.acme.customers.v1.explainCustomerDecisionRequest
import com.acme.customers.v1.previewCustomerSegmentRequest
import com.acme.customers.v1.quoteCustomerRequest
import com.acme.featureflags.v1.FeatureFlagValidationServiceGrpcKt
import com.acme.featureflags.v1.featureFlag
import com.acme.featureflags.v1.scoreFeatureFlagRequest
import com.acme.identity.v1.ListUsersRequestKt
import com.acme.identity.v1.UserServiceGrpcKt
import com.acme.identity.v1.listUsersRequest
import com.acme.identity.v1.userRef
import com.acme.insurance.v1.PolicyCalculationServiceGrpcKt
import com.acme.insurance.v1.RecalculatePoliciesRequest
import com.acme.insurance.v1.RecalculatePoliciesResponse
import com.acme.insurance.v1.policy
import com.acme.insurance.v1.recalculatePoliciesRequest
import com.acme.risk.v1.CloneRiskRegisterRequest
import com.acme.risk.v1.RiskRegisterServiceGrpcKt
import com.acme.risk.v1.cloneRiskRegisterRequest
import com.acme.risk.v1.riskRegister
import com.acme.telematics.v1.CancelGeoFenceImportRequest
import com.acme.telematics.v1.CancelGeoFenceImportResponse
import com.acme.telematics.v1.CancelGeoFenceImportResponseKt
import com.acme.telematics.v1.CheckTripLogEligibilityRequestKt
import com.acme.telematics.v1.CompareGeoFenceOptionsResponse
import com.acme.telematics.v1.EstimateTripLogRequestKt
import com.acme.telematics.v1.GeoFenceCalculationServiceGrpcKt
import com.acme.telematics.v1.GeoFenceCondition
import com.acme.telematics.v1.GeoFenceImportServiceGrpcKt
import com.acme.telematics.v1.GeoFenceValidationServiceGrpcKt
import com.acme.telematics.v1.GetGeoFenceImportStatusRequest
import com.acme.telematics.v1.GetGeoFenceImportStatusResponse
import com.acme.telematics.v1.ImportGeoFencesRequest
import com.acme.telematics.v1.ImportGeoFencesResponse
import com.acme.telematics.v1.ListGeoFenceImportsRequest
import com.acme.telematics.v1.ListGeoFenceImportsResponse
import com.acme.telematics.v1.ListTripLogExportsRequestKt
import com.acme.telematics.v1.MapGeoFenceImportColumnsRequest
import com.acme.telematics.v1.MapGeoFenceImportColumnsResponse
import com.acme.telematics.v1.PreviewGeoFenceRequest
import com.acme.telematics.v1.PreviewGeoFenceRequestKt
import com.acme.telematics.v1.QuoteGeoFenceResponse
import com.acme.telematics.v1.RetryGeoFenceImportRequest
import com.acme.telematics.v1.RetryGeoFenceImportResponse
import com.acme.telematics.v1.SearchTelemetryReadingsRequest
import com.acme.telematics.v1.SearchTelemetryReadingsRequestKt
import com.acme.telematics.v1.SearchTelemetryReadingsResponse
import com.acme.telematics.v1.StreamTripLogExportRequestKt
import com.acme.telematics.v1.TelemetryReadingServiceGrpcKt
import com.acme.telematics.v1.TripLogCalculationServiceGrpcKt
import com.acme.telematics.v1.TripLogExportServiceGrpcKt
import com.acme.telematics.v1.TripLogValidationServiceGrpcKt
import com.acme.telematics.v1.ValidateGeoFenceImportRequest
import com.acme.telematics.v1.ValidateGeoFenceImportResponse
import com.acme.telematics.v1.ValidateGeoFenceImportResponseKt
import com.acme.telematics.v1.cancelGeoFenceImportResponse
import com.acme.telematics.v1.checkTripLogEligibilityRequest
import com.acme.telematics.v1.compareGeoFenceOptionsRequest
import com.acme.telematics.v1.compareTripLogOptionsRequest
import com.acme.telematics.v1.estimateTripLogRequest
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.geoFenceCondition
import com.acme.telematics.v1.getGeoFenceImportStatusResponse
import com.acme.telematics.v1.importGeoFencesResponse
import com.acme.telematics.v1.listGeoFenceImportsResponse
import com.acme.telematics.v1.listTripLogExportsRequest
import com.acme.telematics.v1.mapGeoFenceImportColumnsResponse
import com.acme.telematics.v1.previewGeoFenceRequest
import com.acme.telematics.v1.quoteGeoFenceRequest
import com.acme.telematics.v1.quoteTripLogRequest
import com.acme.telematics.v1.retryGeoFenceImportResponse
import com.acme.telematics.v1.searchTelemetryReadingsRequest
import com.acme.telematics.v1.streamTripLogExportRequest
import com.acme.telematics.v1.tripLog
import com.acme.telematics.v1.validateGeoFenceImportResponse
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.transport.telematics.TelematicsRepository
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Takes geo fences in bulk from partners and legacy systems. */
class GeoFenceImportController(
    private val store: TelematicsRepository,
    private val workers: CoroutineScope,
    private val customersCustomerCalculationService: CustomerCalculationServiceGrpcKt.CustomerCalculationServiceCoroutineStub,
    private val customersCustomerSegmentValidationService: CustomerSegmentValidationServiceGrpcKt.CustomerSegmentValidationServiceCoroutineStub,
    private val customersCustomerValidationService: CustomerValidationServiceGrpcKt.CustomerValidationServiceCoroutineStub,
    private val featureflagsFeatureFlagValidationService: FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub,
    private val identityUserService: UserServiceGrpcKt.UserServiceCoroutineStub,
    private val insurancePolicyCalculationService: PolicyCalculationServiceGrpcKt.PolicyCalculationServiceCoroutineStub,
    private val riskRiskRegisterService: RiskRegisterServiceGrpcKt.RiskRegisterServiceCoroutineStub,
    private val telematicsGeoFenceCalculationService: GeoFenceCalculationServiceGrpcKt.GeoFenceCalculationServiceCoroutineStub,
    private val telematicsGeoFenceValidationService: GeoFenceValidationServiceGrpcKt.GeoFenceValidationServiceCoroutineStub,
    private val telematicsTelemetryReadingService: TelemetryReadingServiceGrpcKt.TelemetryReadingServiceCoroutineStub,
    private val telematicsTripLogCalculationService: TripLogCalculationServiceGrpcKt.TripLogCalculationServiceCoroutineStub,
    private val telematicsTripLogExportService: TripLogExportServiceGrpcKt.TripLogExportServiceCoroutineStub,
    private val telematicsTripLogValidationService: TripLogValidationServiceGrpcKt.TripLogValidationServiceCoroutineStub,
) : GeoFenceImportServiceGrpcKt.GeoFenceImportServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(GeoFenceImportController::class.java)

    private val invokeGetGeoFenceImportStatus0: suspend (RecalculatePoliciesRequest) -> RecalculatePoliciesResponse = { payload ->
        insurancePolicyCalculationService.recalculatePolicies(payload)
    }

    private val clientForMapGeoFenceImportColumns1 by lazy { telematicsGeoFenceValidationService }

    /** Cancels a geo fence. */
    override suspend fun cancelGeoFenceImport(request: CancelGeoFenceImportRequest): CancelGeoFenceImportResponse = handleCancelGeoFenceImport(request)

    /** Returns a geo fence. */
    override suspend fun getGeoFenceImportStatus(request: GetGeoFenceImportStatusRequest): GetGeoFenceImportStatusResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceImportService/GetGeoFenceImportStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetGeoFenceImportStatus", request)
            log.atInfo().addKeyValue("operation", "GetGeoFenceImportStatus").log("Handling transport request")
            val outbound0 = recalculatePoliciesRequest {
                this.annotations.putAll(request.context.baggageMap)
                this.context = request.context
                this.policy = policy {
                    this.audit = auditInfo {
                        this.updatedAt = request.context.deadline
                    }
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                    this.externalAccountId = request.geoFenceId
                }
            }
            val reply0 = invokeGetGeoFenceImportStatus0(outbound0)
            val outbound1 = searchTelemetryReadingsRequest {
                this.context = request.context
                this.filter = SearchTelemetryReadingsRequestKt.filter {
                    this.createdAfter = reply0.policy.audit.updatedAt
                    this.labels.putAll(reply0.policy.labelsMap)
                    this.ownerId = reply0.policy.holder.coverageId
                }
            }
            val invoke1: suspend (SearchTelemetryReadingsRequest, Metadata) -> SearchTelemetryReadingsResponse = telematicsTelemetryReadingService::searchTelemetryReadings
            val reply1 = invoke1(outbound1, Metadata())
            getGeoFenceImportStatusResponse {
                this.geoFence = geoFence {
                    this.geoFenceId = request.geoFenceId
                    this.version = reply0.policy.holder.version
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = reply1.result.status
                }
            }
        }

    /** Takes a stream of geo fences and answers once at the end. */
    override suspend fun importGeoFences(requests: Flow<ImportGeoFencesRequest>): ImportGeoFencesResponse {
        var answer = ImportGeoFencesResponse.getDefaultInstance()
        requests.map { request -> handleImportGeoFences(request) }.collect { answer = it }
        return answer
    }

    /** Lists geo fences a page at a time. */
    override suspend fun listGeoFenceImports(request: ListGeoFenceImportsRequest): ListGeoFenceImportsResponse = handleListGeoFenceImports(request)

    /** Maps a geo fence. */
    override suspend fun mapGeoFenceImportColumns(request: MapGeoFenceImportColumnsRequest): MapGeoFenceImportColumnsResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceImportService/MapGeoFenceImportColumns") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "MapGeoFenceImportColumns", request)
            log.atInfo().addKeyValue("operation", "MapGeoFenceImportColumns").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.geoFence.geoFenceId).log("Recorded audit detail")
            val outbound0 = streamTripLogExportRequest {
                this.context = request.context
                this.filter = StreamTripLogExportRequestKt.filter {
                    this.createdAfter = request.geoFence.audit.updatedAt
                    this.ownerId = request.geoFence.owner.userId
                }
            }
            telematicsTripLogExportService.streamTripLogExport(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = previewGeoFenceRequest {
                this.context = request.context
                this.details = PreviewGeoFenceRequestKt.details {
                    this.contact = contactInfo {
                        this.preferredChannel = request.details.contact.preferredChannel
                    }
                }
                this.geoFence = geoFence {
                    this.owner = userRef {
                        this.email = request.details.contact.email
                    }
                }
            }
            val reply1 = clientForMapGeoFenceImportColumns1.previewGeoFence(outbound1)
            val outbound2 = explainCustomerDecisionRequest {
                this.context = request.context
                this.customer = customer {
                    this.contact = contactInfo {
                        this.preferredChannel = request.details.contact.preferredChannel
                    }
                }
            }
            val reply2 = flowOf(outbound2).map { value -> customersCustomerValidationService.explainCustomerDecision(value) }.single()
            mapGeoFenceImportColumnsResponse {
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.updatedBy = reply2.customer.audit.updatedBy
                    }
                    this.geoFenceId = request.geoFenceId
                    this.status = request.geoFence.status
                }
                this.result = operationResult {
                    this.status = reply1.outcome.status
                }
            }
        }

    /** Retrys a geo fence. */
    override suspend fun retryGeoFenceImport(request: RetryGeoFenceImportRequest): RetryGeoFenceImportResponse {
        return handleRetryGeoFenceImport(request)
    }

    /** Validates a geo fence. */
    override suspend fun validateGeoFenceImport(request: ValidateGeoFenceImportRequest): ValidateGeoFenceImportResponse = handleValidateGeoFenceImport(request)

    private data class CancelGeoFenceImport1Command(
        val context: RequestContext,
        val riskRegisterChildrenChildrenAuditUpdatedBy: String,
        val riskRegisterChildrenChildrenManagerDisplayName: String,
        val riskRegisterChildrenChildrenManagerEmail: String
    ) {
        fun toRequest(): CloneRiskRegisterRequest {
            val command = this
            return cloneRiskRegisterRequest {
                this.context = command.context
                this.riskRegister = riskRegister {
                    this.children.addAll(listOf(riskRegister {
                        this.children.addAll(listOf(riskRegister {
                            this.audit = auditInfo {
                                this.updatedBy = command.riskRegisterChildrenChildrenAuditUpdatedBy
                            }
                            this.manager = userRef {
                                this.displayName = command.riskRegisterChildrenChildrenManagerDisplayName
                                this.email = command.riskRegisterChildrenChildrenManagerEmail
                            }
                        }))
                    }))
                }
            }
        }
    }

    private suspend fun handleCancelGeoFenceImport(request: CancelGeoFenceImportRequest): CancelGeoFenceImportResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceImportService/CancelGeoFenceImport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CancelGeoFenceImport", request)
            log.atInfo().addKeyValue("operation", "CancelGeoFenceImport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = scoreFeatureFlagRequest {
                this.context = request.context
                this.featureFlag = featureFlag {
                    this.owner = userRef {
                        this.email = request.details.notifyContact.email
                    }
                }
            }
            val reply0 = featureflagsFeatureFlagValidationService.scoreFeatureFlag(WireCopies.json(outbound0))
            val outbound1 = CancelGeoFenceImport1Command(
                context = request.context,
                riskRegisterChildrenChildrenAuditUpdatedBy = reply0.featureFlag.audit.updatedBy,
                riskRegisterChildrenChildrenManagerDisplayName = request.details.newOwner.displayName,
                riskRegisterChildrenChildrenManagerEmail = request.details.newOwner.email
            ).toRequest()
            val reply1 = riskRiskRegisterService.cloneRiskRegister(outbound1)
            val outbound2 = previewGeoFenceRequest {
                this.context = request.context
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.createdAt = reply0.featureFlag.audit.createdAt
                        this.etag = reply0.featureFlag.audit.etag
                    }
                    this.owner = userRef {
                        this.email = reply0.featureFlag.owner.email
                        this.userId = request.details.newOwner.userId
                    }
                }
            }
            val command2: Submission<PreviewGeoFenceRequest> = Submission.Live(outbound2)
            val payload2 = when (val command = command2) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply2 = telematicsGeoFenceValidationService.previewGeoFence(payload2)
            val outbound3 = quoteCustomerRequest {
                this.context = request.context
                this.customer = customer {
                    this.audit = auditInfo {
                        this.createdAt = reply2.geoFence.audit.createdAt
                    }
                    this.relationshipManager = userRef {
                        this.email = reply1.riskRegister.manager.email
                    }
                }
                this.details = QuoteCustomerRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(reply1.riskRegister.location.linesList)
                    }
                }
            }
            val adapter3 = OutboundPort<QuoteCustomerRequest, QuoteCustomerResponse> { payload ->
                customersCustomerCalculationService.quoteCustomer(payload)
            }
            val port3 = DelegatingPort(adapter3)
            val reply3 = port3.execute(outbound3)
            cancelGeoFenceImportResponse {
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.createdAt = reply0.featureFlag.audit.createdAt
                        this.updatedAt = reply3.customer.audit.updatedAt
                        this.updatedBy = reply2.geoFence.audit.updatedBy
                    }
                    this.geoFenceId = request.geoFenceId
                    this.owner = userRef {
                        this.displayName = reply1.riskRegister.manager.displayName
                        this.userId = request.details.newOwner.userId
                    }
                }
                this.outcome = CancelGeoFenceImportResponseKt.outcome {
                    this.effectiveAt = reply2.outcome.effectiveAt
                    this.status = reply3.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleImportGeoFences(request: ImportGeoFencesRequest): ImportGeoFencesResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceImportService/ImportGeoFences") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ImportGeoFences", request)
            log.atInfo().addKeyValue("operation", "ImportGeoFences").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.header.source).log("Recorded audit detail")
            val outbound0 = PreviewCustomerSegmentRequest.newBuilder().apply {
                setContext(request.context)
                setCustomerSegment(customerSegment {
                    this.audit = auditInfo {
                        this.etag = request.record.audit.etag
                    }
                    this.discount = customerSegmentDiscountAction {
                        this.amount = request.record.discount.amount
                    }
                    this.owner = userRef {
                        this.userId = request.record.owner.userId
                    }
                })
                setDetails(PreviewCustomerSegmentRequestKt.details {
                    this.contact = contactInfo {
                        this.phone = request.header.submittedBy.phone
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> customersCustomerSegmentValidationService.previewCustomerSegment(value) }
            val outbound1 = RequestFactory.create {
                checkTripLogEligibilityRequest {
                    this.context = request.context
                    this.details = CheckTripLogEligibilityRequestKt.details {
                        this.contact = contactInfo {
                            this.phone = request.header.submittedBy.phone
                        }
                    }
                    this.tripLog = tripLog {
                        this.counterparty = customerRef {
                            this.email = reply0.customerSegment.owner.email
                        }
                    }
                }
            }
            val reply1 = telematicsTripLogValidationService.checkTripLogEligibility(outbound1)
            val outbound2 = compareTripLogOptionsRequest {
                this.context = request.context
                this.tripLog = tripLog {
                    this.billingAddress = postalAddress {
                        this.locality = reply1.tripLog.billingAddress.locality
                    }
                }
            }
            val reply2 = ClientRegistry.lookup("acme.telematics.v1.TripLogCalculationService", TripLogCalculationServiceGrpcKt.TripLogCalculationServiceCoroutineStub::class.java).compareTripLogOptions(outbound2)
            importGeoFencesResponse {
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.message = reply1.outcome.message
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = reply0.outcome.status
                }
            }
        }

    private suspend fun handleListGeoFenceImports(request: ListGeoFenceImportsRequest): ListGeoFenceImportsResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceImportService/ListGeoFenceImports") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListGeoFenceImports", request)
            log.atInfo().addKeyValue("operation", "ListGeoFenceImports").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = estimateTripLogRequest {
                this.context = request.context
                this.details = EstimateTripLogRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.filter.region
                    }
                }
                this.tripLog = tripLog {
                    this.counterparty = customerRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = telematicsTripLogCalculationService.estimateTripLog(WireCopies.reflective(outbound0))
            listGeoFenceImportsResponse {
                this.geoFences.addAll(listOf(geoFence {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.tripLog.audit.updatedBy
                    }
                }))
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleRetryGeoFenceImport(request: RetryGeoFenceImportRequest): RetryGeoFenceImportResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceImportService/RetryGeoFenceImport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RetryGeoFenceImport", request)
            log.atInfo().addKeyValue("operation", "RetryGeoFenceImport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.geoFence.geoFenceId).log("Recorded audit detail")
            val outbound0 = quoteTripLogRequest {
                this.context = request.context
                this.tripLog = tripLog {
                    this.counterparty = customerRef {
                        this.displayName = request.geoFence.owner.displayName
                    }
                }
            }
            val reply0 = withTimeout(5_000) { telematicsTripLogCalculationService.quoteTripLog(outbound0) }
            val configure1: ListUsersRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.filter = ListUsersRequestKt.filter {
                    this.createdAfter = request.geoFence.audit.updatedAt
                    this.region = reply0.tripLog.billingAddress.region
                }
            }
            val outbound1 = listUsersRequest(configure1)
            val reply1 = identityUserService.listUsers(outbound1)
            retryGeoFenceImportResponse {
                this.geoFence = geoFence {
                    this.effective = request.geoFence.effective
                    this.geoFenceId = request.geoFenceId
                    this.owner = userRef {
                        this.email = reply0.tripLog.counterparty.email
                    }
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    private fun ValidateGeoFenceImportRequest.toSearchTelemetryReadingsFromValidateGeoFenceImport3(): SearchTelemetryReadingsRequest {
        val source = this
        return searchTelemetryReadingsRequest {
            this.context = source.context
            this.filter = SearchTelemetryReadingsRequestKt.filter {
                this.createdAfter = source.geoFence.audit.updatedAt
            }
        }
    }

    private suspend fun handleValidateGeoFenceImport(request: ValidateGeoFenceImportRequest): ValidateGeoFenceImportResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceImportService/ValidateGeoFenceImport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ValidateGeoFenceImport", request)
            log.atInfo().addKeyValue("operation", "ValidateGeoFenceImport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.geoFence.geoFenceId).log("Recorded audit detail")
            val outbound0 = listTripLogExportsRequest {
                this.context = request.context
                this.filter = ListTripLogExportsRequestKt.filter {
                    this.createdAfter = request.geoFence.audit.updatedAt
                }
            }
            val reply0 = telematicsTripLogExportService.listTripLogExports(outbound0)
            var reply1 = QuoteGeoFenceResponse.getDefaultInstance()
            request.geoFence.conditionsList.asFlow().map { item ->
                val outbound1 = quoteGeoFenceRequest {
                    this.context = request.context
                    this.geoFence = geoFence {
                        this.conditions.addAll(listOf(geoFenceCondition {
                            this.attribute = item.attribute
                            this.negate = item.negate
                            this.value = item.value
                        }))
                    }
                }
                telematicsGeoFenceCalculationService.quoteGeoFence(outbound1)
            }.collect { reply1 = it }
            var reply2 = CompareGeoFenceOptionsResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<GeoFenceCondition>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound2 = compareGeoFenceOptionsRequest {
                            this.context = request.context
                            this.geoFence = geoFence {
                                this.conditions.addAll(listOf(geoFenceCondition {
                                    this.value = item.value
                                }))
                            }
                        }
                        reply2 = telematicsGeoFenceCalculationService.compareGeoFenceOptions(outbound2)
                    }
                }
                try {
                    request.geoFence.conditionsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound3 = request.toSearchTelemetryReadingsFromValidateGeoFenceImport3()
            val reply3 = telematicsTelemetryReadingService.searchTelemetryReadings(outbound3)
            validateGeoFenceImportResponse {
                this.geoFence = geoFence {
                    this.geoFenceId = request.geoFenceId
                    this.owner = request.geoFence.owner
                }
                this.outcome = ValidateGeoFenceImportResponseKt.outcome {
                    this.status = reply3.result.status
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = reply0.result.status
                }
            }
        }
}
