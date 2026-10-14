package com.acme.transport.drivers.grpc

import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.PreviewAuditEventRequestKt
import com.acme.audit.v1.auditEvent
import com.acme.audit.v1.auditTrailRef
import com.acme.audit.v1.previewAuditEventRequest
import com.acme.authorization.v1.AccessPolicyServiceGrpcKt
import com.acme.authorization.v1.ListAccessPoliciesRequestKt
import com.acme.authorization.v1.listAccessPoliciesRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.pageRequest
import com.acme.common.v1.postalAddress
import com.acme.corporateevents.v1.CorporateEventServiceGrpcKt
import com.acme.corporateevents.v1.watchCorporateEventsRequest
import com.acme.customers.v1.customerRef
import com.acme.drivers.v1.ApproveDriverRequest
import com.acme.drivers.v1.ApproveDriverResponse
import com.acme.drivers.v1.ApproveDriverResponseKt
import com.acme.drivers.v1.BrowseDriverLicensesRequest
import com.acme.drivers.v1.BrowseDriverLicensesRequestKt
import com.acme.drivers.v1.DelegateDriverApprovalRequest
import com.acme.drivers.v1.DelegateDriverApprovalResponse
import com.acme.drivers.v1.DriverApprovalServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseQueryServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseValidationServiceGrpcKt
import com.acme.drivers.v1.DriverLifecycleServiceGrpcKt
import com.acme.drivers.v1.DutyStatusReportingServiceGrpcKt
import com.acme.drivers.v1.EscalateDriverRequest
import com.acme.drivers.v1.EscalateDriverResponse
import com.acme.drivers.v1.EscalateDriverResponseKt
import com.acme.drivers.v1.EstimateDriverLicenseRequest
import com.acme.drivers.v1.EstimateDriverLicenseRequestKt
import com.acme.drivers.v1.ExpireDriverRequestKt
import com.acme.drivers.v1.GetDriverApprovalStatusRequest
import com.acme.drivers.v1.GetDriverApprovalStatusResponse
import com.acme.drivers.v1.ListPendingDriversRequest
import com.acme.drivers.v1.ListPendingDriversResponse
import com.acme.drivers.v1.RecalculateDriverLicensesRequestKt
import com.acme.drivers.v1.RecallDriverRequest
import com.acme.drivers.v1.RecallDriverResponse
import com.acme.drivers.v1.ScoreDriverLicenseRequestKt
import com.acme.drivers.v1.ShareDutyStatusReportRequest
import com.acme.drivers.v1.ShareDutyStatusReportRequestKt
import com.acme.drivers.v1.ShareDutyStatusReportResponse
import com.acme.drivers.v1.SubmitDriverRequest
import com.acme.drivers.v1.SubmitDriverResponse
import com.acme.drivers.v1.SubmitDriverResponseKt
import com.acme.drivers.v1.approveDriverResponse
import com.acme.drivers.v1.browseDriverLicensesRequest
import com.acme.drivers.v1.delegateDriverApprovalResponse
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.escalateDriverResponse
import com.acme.drivers.v1.estimateDriverLicenseRequest
import com.acme.drivers.v1.expireDriverRequest
import com.acme.drivers.v1.getDriverApprovalStatusResponse
import com.acme.drivers.v1.listPendingDriversResponse
import com.acme.drivers.v1.recalculateDriverLicensesRequest
import com.acme.drivers.v1.recallDriverResponse
import com.acme.drivers.v1.scoreDriverLicenseRequest
import com.acme.drivers.v1.shareDutyStatusReportRequest
import com.acme.drivers.v1.submitDriverResponse
import com.acme.identity.v1.userRef
import com.acme.metrics.v1.MetricSeriesValidationServiceGrpcKt
import com.acme.metrics.v1.ScoreMetricSeriesRequestKt
import com.acme.metrics.v1.metricSeries
import com.acme.metrics.v1.scoreMetricSeriesRequest
import com.acme.search.v1.SearchIndexQueryServiceGrpcKt
import com.acme.search.v1.SummarizeSearchIndexesRequest
import com.acme.search.v1.summarizeSearchIndexesRequest
import com.acme.telematics.v1.CountTripLogTotalsRequestKt
import com.acme.telematics.v1.ResumeTripLogWorkflowRequest
import com.acme.telematics.v1.ResumeTripLogWorkflowRequestKt
import com.acme.telematics.v1.StreamTripLogMetricsRequestKt
import com.acme.telematics.v1.TripLogReportingServiceGrpcKt
import com.acme.telematics.v1.TripLogWorkflowServiceGrpcKt
import com.acme.telematics.v1.countTripLogTotalsRequest
import com.acme.telematics.v1.resumeTripLogWorkflowRequest
import com.acme.telematics.v1.streamTripLogMetricsRequest
import com.acme.telematics.v1.tripLog
import com.acme.transport.drivers.DriversRepository
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.google.protobuf.Any as ProtoAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Routes drivers through approval, with escalation and delegation. */
class DriverApprovalEndpoint(
    private val store: DriversRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventValidationService: AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub,
    private val authorizationAccessPolicyService: AccessPolicyServiceGrpcKt.AccessPolicyServiceCoroutineStub,
    private val corporateeventsCorporateEventService: CorporateEventServiceGrpcKt.CorporateEventServiceCoroutineStub,
    private val driversDriverLicenseCalculationService: DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub,
    private val driversDriverLicenseQueryService: DriverLicenseQueryServiceGrpcKt.DriverLicenseQueryServiceCoroutineStub,
    private val driversDriverLicenseValidationService: DriverLicenseValidationServiceGrpcKt.DriverLicenseValidationServiceCoroutineStub,
    private val driversDriverLifecycleService: DriverLifecycleServiceGrpcKt.DriverLifecycleServiceCoroutineStub,
    private val driversDutyStatusReportingService: DutyStatusReportingServiceGrpcKt.DutyStatusReportingServiceCoroutineStub,
    private val metricsMetricSeriesValidationService: MetricSeriesValidationServiceGrpcKt.MetricSeriesValidationServiceCoroutineStub,
    private val searchSearchIndexQueryService: SearchIndexQueryServiceGrpcKt.SearchIndexQueryServiceCoroutineStub,
    private val telematicsTripLogReportingService: TripLogReportingServiceGrpcKt.TripLogReportingServiceCoroutineStub,
    private val telematicsTripLogWorkflowService: TripLogWorkflowServiceGrpcKt.TripLogWorkflowServiceCoroutineStub,
) : DriverApprovalServiceGrpcKt.DriverApprovalServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DriverApprovalEndpoint::class.java)

    private val invokeSubmitDriver2: suspend (ShareDutyStatusReportRequest) -> ShareDutyStatusReportResponse = { payload ->
        driversDutyStatusReportingService.shareDutyStatusReport(payload)
    }

    /** Approves a driver. */
    override suspend fun approveDriver(request: ApproveDriverRequest): ApproveDriverResponse =
        RpcBoundary.run("acme.drivers.v1.DriverApprovalService/ApproveDriver") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ApproveDriver", request)
            log.atInfo().addKeyValue("operation", "ApproveDriver").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driver.driverId).log("Recorded audit detail")
            val outbound0 = recalculateDriverLicensesRequest {
                this.context = request.context
                this.details = RecalculateDriverLicensesRequestKt.details {
                    this.destination = postalAddress {
                        this.locality = request.driver.primaryAddress.locality
                    }
                }
            }
            val reply0 = flowOf(outbound0).map { value -> driversDriverLicenseCalculationService.recalculateDriverLicenses(value) }.single()
            val outbound1 = scoreMetricSeriesRequest {
                this.context = request.context
                this.details = ScoreMetricSeriesRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(reply0.driverLicense.billingAddress.linesList)
                    }
                }
                this.metricSeries = metricSeries {
                    this.address = postalAddress {
                        this.region = request.driver.primaryAddress.region
                    }
                }
            }
            val reply1 = withTimeout(5_000) { metricsMetricSeriesValidationService.scoreMetricSeries(outbound1) }
            approveDriverResponse {
                this.driver = driver {
                    this.driverId = request.driverId
                    this.primaryAddress = postalAddress {
                        this.locality = reply0.driverLicense.billingAddress.locality
                    }
                }
                this.outcome = ApproveDriverResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.message = reply1.result.message
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = reply0.result.status
                }
            }
        }

    /** Delegates a driver. */
    override suspend fun delegateDriverApproval(request: DelegateDriverApprovalRequest): DelegateDriverApprovalResponse {
        return handleDelegateDriverApproval(request)
    }

    /** Escalates a driver. */
    override suspend fun escalateDriver(request: EscalateDriverRequest): EscalateDriverResponse = handleEscalateDriver(request)

    /** Returns a driver. */
    override suspend fun getDriverApprovalStatus(request: GetDriverApprovalStatusRequest): GetDriverApprovalStatusResponse =
        RpcBoundary.run("acme.drivers.v1.DriverApprovalService/GetDriverApprovalStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetDriverApprovalStatus", request)
            log.atInfo().addKeyValue("operation", "GetDriverApprovalStatus").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            getDriverApprovalStatusResponse {
                this.driver = driver {
                    this.driverId = request.driverId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Lists drivers a page at a time. */
    override suspend fun listPendingDrivers(request: ListPendingDriversRequest): ListPendingDriversResponse {
        return handleListPendingDrivers(request)
    }

    /** Recalls a driver. */
    override suspend fun recallDriver(request: RecallDriverRequest): RecallDriverResponse = handleRecallDriver(request)

    /** Submits a driver. */
    override suspend fun submitDriver(request: SubmitDriverRequest): SubmitDriverResponse =
        RpcBoundary.run("acme.drivers.v1.DriverApprovalService/SubmitDriver") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SubmitDriver", request)
            log.atInfo().addKeyValue("operation", "SubmitDriver").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driver.driverId).log("Recorded audit detail")
            val outbound0 = resumeTripLogWorkflowRequest {
                this.context = request.context
                this.details = ResumeTripLogWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.email = request.driver.relationshipManager.email
                    }
                }
                this.tripLog = tripLog {
                    this.audit = auditInfo {
                        this.createdBy = request.driver.audit.createdBy
                    }
                    this.counterparty = customerRef {
                        this.displayName = request.details.approver.displayName
                    }
                }
            }
            val command0: Submission<ResumeTripLogWorkflowRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = telematicsTripLogWorkflowService.resumeTripLogWorkflow(payload0)
            val outbound1 = watchCorporateEventsRequest {
                this.context = request.context
                this.subscriber = contactInfo {
                    this.email = request.details.approver.email
                }
            }
            corporateeventsCorporateEventService.watchCorporateEvents(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound2 = shareDutyStatusReportRequest {
                this.context = request.context
                this.details = ShareDutyStatusReportRequestKt.details {
                    this.recipient = contactInfo {
                        this.email = request.driver.relationshipManager.email
                        this.phone = request.driver.contact.phone
                        this.preferredChannel = request.driver.contact.preferredChannel
                    }
                }
            }
            val reply2 = invokeSubmitDriver2(outbound2)
            submitDriverResponse {
                this.driver = driver {
                    this.attributes.putAll(request.driver.attributesMap)
                    this.driverId = request.driverId
                }
                this.outcome = SubmitDriverResponseKt.outcome {
                    this.status = reply0.outcome.status
                }
                this.result = operationResult {
                    this.message = reply2.outcome.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleDelegateDriverApproval(request: DelegateDriverApprovalRequest): DelegateDriverApprovalResponse =
        RpcBoundary.run("acme.drivers.v1.DriverApprovalService/DelegateDriverApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DelegateDriverApproval", request)
            log.atInfo().addKeyValue("operation", "DelegateDriverApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driver.driverId).log("Recorded audit detail")
            val configure0: ScoreDriverLicenseRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.driverLicense = driverLicense {
                    this.audit = auditInfo {
                        this.createdAt = request.driver.audit.createdAt
                    }
                    this.billingAddress = postalAddress {
                        this.lines.addAll(request.driver.primaryAddress.linesList)
                    }
                }
            }
            val outbound0 = scoreDriverLicenseRequest(configure0)
            val reply0 = driversDriverLicenseValidationService.scoreDriverLicense(outbound0)
            val outbound1 = expireDriverRequest {
                this.context = request.context
                this.details = ExpireDriverRequestKt.details {
                    this.newOwner = userRef {
                        this.displayName = request.details.approver.displayName
                        this.userId = request.driver.relationshipManager.userId
                    }
                    this.notifyContact = contactInfo {
                        this.email = request.driver.relationshipManager.email
                    }
                }
                this.driverId = request.driverId
            }
            val reply1 = driversDriverLifecycleService.expireDriver(outbound1)
            delegateDriverApprovalResponse {
                this.driver = driver {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.driverLicense.audit.updatedAt
                    }
                    this.contact = contactInfo {
                        this.email = reply0.driverLicense.counterparty.email
                    }
                    this.driverId = request.driverId
                    this.primaryAddress = postalAddress {
                        this.postalCode = reply1.driver.primaryAddress.postalCode
                    }
                    this.status = request.driver.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private fun EscalateDriverRequest.toEstimateDriverLicenseFromEscalateDriver2(): EstimateDriverLicenseRequest {
        val source = this
        return estimateDriverLicenseRequest {
            this.context = source.context
            this.details = EstimateDriverLicenseRequestKt.details {
                this.destination = postalAddress {
                    this.recipient = source.driver.primaryAddress.recipient
                    this.region = source.driver.primaryAddress.region
                }
            }
            this.driverLicense = driverLicense {
                this.audit = auditInfo {
                    this.etag = source.driver.audit.etag
                }
                this.billingAddress = postalAddress {
                    this.lines.addAll(source.driver.primaryAddress.linesList)
                }
            }
        }
    }

    private suspend fun handleEscalateDriver(request: EscalateDriverRequest): EscalateDriverResponse =
        RpcBoundary.run("acme.drivers.v1.DriverApprovalService/EscalateDriver") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EscalateDriver", request)
            log.atInfo().addKeyValue("operation", "EscalateDriver").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverId).log("Recorded audit detail")
            val outbound0 = countTripLogTotalsRequest {
                this.context = request.context
                this.filter = CountTripLogTotalsRequestKt.filter {
                    this.createdAfter = request.details.effectiveAt
                    this.region = request.driver.primaryAddress.region
                }
            }
            val reply0 = telematicsTripLogReportingService.countTripLogTotals(outbound0)
            val outbound1 = streamTripLogMetricsRequest {
                this.context = request.context
                this.filter = StreamTripLogMetricsRequestKt.filter {
                    this.region = request.driver.primaryAddress.region
                }
                this.subscriber = contactInfo {
                    this.email = request.details.approver.email
                    this.name = request.driver.contact.name
                    this.phone = request.driver.contact.phone
                }
            }
            telematicsTripLogReportingService.streamTripLogMetrics(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound2 = request.toEstimateDriverLicenseFromEscalateDriver2()
            val reply2 = driversDriverLicenseCalculationService.estimateDriverLicense(outbound2)
            val outbound3 = listAccessPoliciesRequest {
                this.context = request.context
                this.filter = ListAccessPoliciesRequestKt.filter {
                    this.createdAfter = reply2.driverLicense.audit.createdAt
                    this.region = request.driver.primaryAddress.region
                }
            }
            val reply3 = authorizationAccessPolicyService.listAccessPolicies(outbound3)
            escalateDriverResponse {
                this.driver = driver {
                    this.attributes.putAll(request.driver.attributesMap)
                    this.audit = auditInfo {
                        this.updatedAt = reply2.driverLicense.audit.updatedAt
                    }
                    this.contact = contactInfo {
                        this.name = request.driver.name
                    }
                    this.driverId = reply2.driverLicense.counterparty.driverId
                }
                this.outcome = EscalateDriverResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.details.putAll(reply3.result.detailsMap)
                    this.status = reply3.result.status
                }
            }
        }

    private suspend fun handleListPendingDrivers(request: ListPendingDriversRequest): ListPendingDriversResponse =
        RpcBoundary.run("acme.drivers.v1.DriverApprovalService/ListPendingDrivers") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListPendingDrivers", request)
            log.atInfo().addKeyValue("operation", "ListPendingDrivers").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.ownerId).log("Recorded audit detail")
            val outbound0 = browseDriverLicensesRequest {
                this.context = request.context
                this.filter = BrowseDriverLicensesRequestKt.filter {
                    this.labels.putAll(request.filter.labelsMap)
                    this.query = request.filter.query
                }
                this.page = pageRequest {
                    this.pageToken = request.page.pageToken
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = driversDriverLicenseQueryService.browseDriverLicenses(envelope0.unpack(BrowseDriverLicensesRequest::class.java))
            listPendingDriversResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private data class RecallDriver1Command(
        val context: RequestContext,
        val reason: String
    ) {
        fun toRequest(): SummarizeSearchIndexesRequest {
            val command = this
            return summarizeSearchIndexesRequest {
                this.context = command.context
                this.reason = command.reason
            }
        }
    }

    private suspend fun handleRecallDriver(request: RecallDriverRequest): RecallDriverResponse =
        RpcBoundary.run("acme.drivers.v1.DriverApprovalService/RecallDriver") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecallDriver", request)
            log.atInfo().addKeyValue("operation", "RecallDriver").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driver.driverId).log("Recorded audit detail")
            val outbound0 = previewAuditEventRequest {
                this.auditEvent = auditEvent {
                    this.subject = auditTrailRef {
                        this.number = request.driver.nationalId.number
                    }
                }
                this.context = request.context
                this.details = PreviewAuditEventRequestKt.details {
                    this.contact = contactInfo {
                        this.name = request.driver.contact.name
                    }
                }
            }
            val reply0 = auditAuditEventValidationService.previewAuditEvent(WireCopies.json(outbound0))
            val outbound1 = RecallDriver1Command(
                context = request.context,
                reason = request.reason
            ).toRequest()
            val reply1 = searchSearchIndexQueryService.summarizeSearchIndexes(outbound1)
            recallDriverResponse {
                this.driver = driver {
                    this.driverId = request.driverId
                    this.name = request.driver.name
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = reply0.outcome.status
                }
            }
        }
}
