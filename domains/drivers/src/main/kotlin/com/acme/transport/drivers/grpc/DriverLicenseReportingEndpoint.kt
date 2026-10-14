package com.acme.transport.drivers.grpc

import com.acme.audit.v1.AuditEventQueryServiceGrpcKt
import com.acme.audit.v1.FindAuditEventsRequest
import com.acme.audit.v1.FindAuditEventsRequestKt
import com.acme.audit.v1.findAuditEventsRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.AdvanceProofOfDeliveryWorkflowRequest
import com.acme.deliveries.v1.AdvanceProofOfDeliveryWorkflowRequestKt
import com.acme.deliveries.v1.AdvanceProofOfDeliveryWorkflowResponse
import com.acme.deliveries.v1.DeliveryExceptionWorkflowServiceGrpcKt
import com.acme.deliveries.v1.ProofOfDeliveryWorkflowServiceGrpcKt
import com.acme.deliveries.v1.StartDeliveryExceptionWorkflowRequest
import com.acme.deliveries.v1.advanceProofOfDeliveryWorkflowRequest
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.startDeliveryExceptionWorkflowRequest
import com.acme.drivers.v1.CountDriverLicenseTotalsRequest
import com.acme.drivers.v1.CountDriverLicenseTotalsResponse
import com.acme.drivers.v1.DriverCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseQueryServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseReportingServiceGrpcKt
import com.acme.drivers.v1.DriverServiceGrpcKt
import com.acme.drivers.v1.EstimateDriverRequest
import com.acme.drivers.v1.ExplainDriverCalculationRequest
import com.acme.drivers.v1.FetchDriverLicenseReportRequest
import com.acme.drivers.v1.FetchDriverLicenseReportResponse
import com.acme.drivers.v1.GenerateDriverLicenseReportRequest
import com.acme.drivers.v1.GenerateDriverLicenseReportResponse
import com.acme.drivers.v1.ListDriverLicenseReportsRequest
import com.acme.drivers.v1.ListDriverLicenseReportsResponse
import com.acme.drivers.v1.LookupDriverLicenseRequest
import com.acme.drivers.v1.LookupDriverLicenseResponse
import com.acme.drivers.v1.ShareDriverLicenseReportRequest
import com.acme.drivers.v1.ShareDriverLicenseReportResponse
import com.acme.drivers.v1.ShareDriverLicenseReportResponseKt
import com.acme.drivers.v1.StreamDriverLicenseMetricsRequest
import com.acme.drivers.v1.StreamDriverLicenseMetricsResponse
import com.acme.drivers.v1.cloneDriverRequest
import com.acme.drivers.v1.countDriverLicenseTotalsResponse
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverPreferences
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.estimateDriverRequest
import com.acme.drivers.v1.explainDriverCalculationRequest
import com.acme.drivers.v1.fetchDriverLicenseReportResponse
import com.acme.drivers.v1.generateDriverLicenseReportResponse
import com.acme.drivers.v1.listDriverLicenseReportsResponse
import com.acme.drivers.v1.lookupDriverLicenseRequest
import com.acme.drivers.v1.shareDriverLicenseReportResponse
import com.acme.drivers.v1.streamDriverLicenseMetricsResponse
import com.acme.fuel.v1.FuelPolicyExportServiceGrpcKt
import com.acme.fuel.v1.RetryFuelPolicyExportRequestKt
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.retryFuelPolicyExportRequest
import com.acme.identity.v1.userRef
import com.acme.telematics.v1.ResumeTelemetryReadingWorkflowRequest
import com.acme.telematics.v1.ResumeTelemetryReadingWorkflowRequestKt
import com.acme.telematics.v1.ResumeTelemetryReadingWorkflowResponse
import com.acme.telematics.v1.TelemetryReadingWorkflowServiceGrpcKt
import com.acme.telematics.v1.resumeTelemetryReadingWorkflowRequest
import com.acme.transport.drivers.DriversRepository
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.slf4j.LoggerFactory

/** Builds and schedules reports about driver licenses. */
class DriverLicenseReportingEndpoint(
    private val store: DriversRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventQueryService: AuditEventQueryServiceGrpcKt.AuditEventQueryServiceCoroutineStub,
    private val deliveriesDeliveryExceptionWorkflowService: DeliveryExceptionWorkflowServiceGrpcKt.DeliveryExceptionWorkflowServiceCoroutineStub,
    private val deliveriesProofOfDeliveryWorkflowService: ProofOfDeliveryWorkflowServiceGrpcKt.ProofOfDeliveryWorkflowServiceCoroutineStub,
    private val driversDriverCalculationService: DriverCalculationServiceGrpcKt.DriverCalculationServiceCoroutineStub,
    private val driversDriverLicenseQueryService: DriverLicenseQueryServiceGrpcKt.DriverLicenseQueryServiceCoroutineStub,
    private val driversDriverService: DriverServiceGrpcKt.DriverServiceCoroutineStub,
    private val fuelFuelPolicyExportService: FuelPolicyExportServiceGrpcKt.FuelPolicyExportServiceCoroutineStub,
    private val telematicsTelemetryReadingWorkflowService: TelemetryReadingWorkflowServiceGrpcKt.TelemetryReadingWorkflowServiceCoroutineStub,
) : DriverLicenseReportingServiceGrpcKt.DriverLicenseReportingServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DriverLicenseReportingEndpoint::class.java)

    private val invokeShareDriverLicenseReport3: suspend (AdvanceProofOfDeliveryWorkflowRequest) -> AdvanceProofOfDeliveryWorkflowResponse = { payload ->
        deliveriesProofOfDeliveryWorkflowService.advanceProofOfDeliveryWorkflow(payload)
    }

    /** Counts driver licenses by status. */
    override suspend fun countDriverLicenseTotals(request: CountDriverLicenseTotalsRequest): CountDriverLicenseTotalsResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseReportingService/CountDriverLicenseTotals") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CountDriverLicenseTotals", request)
            log.atInfo().addKeyValue("operation", "CountDriverLicenseTotals").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = request.toFindAuditEventsFromCountDriverLicenseTotals0()
            val reply0 = auditAuditEventQueryService.findAuditEvents(outbound0)
            countDriverLicenseTotalsResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = reply0.result.status
                }
            }
        }

    /** Returns a driver license. */
    override suspend fun fetchDriverLicenseReport(request: FetchDriverLicenseReportRequest): FetchDriverLicenseReportResponse {
        return handleFetchDriverLicenseReport(request)
    }

    /** Generates a driver license. */
    override suspend fun generateDriverLicenseReport(request: GenerateDriverLicenseReportRequest): GenerateDriverLicenseReportResponse = handleGenerateDriverLicenseReport(request)

    /** Lists driver licenses a page at a time. */
    override suspend fun listDriverLicenseReports(request: ListDriverLicenseReportsRequest): ListDriverLicenseReportsResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseReportingService/ListDriverLicenseReports") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListDriverLicenseReports", request)
            log.atInfo().addKeyValue("operation", "ListDriverLicenseReports").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            listDriverLicenseReportsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Shares a driver license. */
    override suspend fun shareDriverLicenseReport(request: ShareDriverLicenseReportRequest): ShareDriverLicenseReportResponse {
        return handleShareDriverLicenseReport(request)
    }

    /** Streams the changes of driver licenses as they happen. */
    override fun streamDriverLicenseMetrics(request: StreamDriverLicenseMetricsRequest): Flow<StreamDriverLicenseMetricsResponse> = flow {
        emit(handleStreamDriverLicenseMetrics(request))
    }

    private fun CountDriverLicenseTotalsRequest.toFindAuditEventsFromCountDriverLicenseTotals0(): FindAuditEventsRequest {
        val source = this
        return findAuditEventsRequest {
            this.context = source.context
            this.filter = FindAuditEventsRequestKt.filter {
                this.labels.putAll(source.filter.labelsMap)
                this.query = source.filter.query
            }
        }
    }

    private suspend fun handleFetchDriverLicenseReport(request: FetchDriverLicenseReportRequest): FetchDriverLicenseReportResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseReportingService/FetchDriverLicenseReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "FetchDriverLicenseReport", request)
            log.atInfo().addKeyValue("operation", "FetchDriverLicenseReport").log("Handling transport request")
            val outbound0 = retryFuelPolicyExportRequest {
                this.context = request.context
                this.details = RetryFuelPolicyExportRequestKt.details {
                    this.notify = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.updatedAt = request.context.deadline
                    }
                    this.owner = userRef {
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply0 = fuelFuelPolicyExportService.retryFuelPolicyExport(outbound0)
            fetchDriverLicenseReportResponse {
                this.driverLicense = driverLicense {
                    this.driverLicenseId = request.driverLicenseId
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleGenerateDriverLicenseReport(request: GenerateDriverLicenseReportRequest): GenerateDriverLicenseReportResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseReportingService/GenerateDriverLicenseReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GenerateDriverLicenseReport", request)
            log.atInfo().addKeyValue("operation", "GenerateDriverLicenseReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverLicenseId).log("Recorded audit detail")
            val outbound0 = explainDriverCalculationRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.driver = driver {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
                this.reason = request.reason
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = driversDriverCalculationService.explainDriverCalculation(envelope0.unpack(ExplainDriverCalculationRequest::class.java))
            val outbound1 = cloneDriverRequest {
                this.context = request.context
                this.driver = driver {
                    this.contact = contactInfo {
                        this.preferredChannel = reply0.driver.contact.preferredChannel
                    }
                    this.preferences = driverPreferences {
                        this.language = reply0.driver.preferences.language
                    }
                    this.primaryAddress = postalAddress {
                        this.recipient = reply0.driver.primaryAddress.recipient
                    }
                }
            }
            val reply1 = driversDriverService.cloneDriver(WireCopies.json(outbound1))
            generateDriverLicenseReportResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.referenceIds.addAll((reply0.result.referenceIdsList) + (reply1.result.referenceIdsList))
                    this.status = "ok"
                }
            }
        }

    private data class ShareDriverLicenseReport0Command(
        val context: RequestContext,
        val driverContactPreferredChannel: String,
        val driverRelationshipManagerEmail: String
    ) {
        fun toRequest(): EstimateDriverRequest {
            val command = this
            return estimateDriverRequest {
                this.context = command.context
                this.driver = driver {
                    this.contact = contactInfo {
                        this.preferredChannel = command.driverContactPreferredChannel
                    }
                    this.relationshipManager = userRef {
                        this.email = command.driverRelationshipManagerEmail
                    }
                }
            }
        }
    }

    private suspend fun handleShareDriverLicenseReport(request: ShareDriverLicenseReportRequest): ShareDriverLicenseReportResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseReportingService/ShareDriverLicenseReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ShareDriverLicenseReport", request)
            log.atInfo().addKeyValue("operation", "ShareDriverLicenseReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.recipient.phone).log("Recorded audit detail")
            val outbound0 = ShareDriverLicenseReport0Command(
                context = request.context,
                driverContactPreferredChannel = request.details.recipient.preferredChannel,
                driverRelationshipManagerEmail = request.details.recipient.email
            ).toRequest()
            val reply0 = driversDriverCalculationService.estimateDriver(outbound0)
            val outbound1 = startDeliveryExceptionWorkflowRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.requester = contactInfo {
                        this.phone = reply0.driver.contact.phone
                    }
                }
            }
            val command1: Submission<StartDeliveryExceptionWorkflowRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = deliveriesDeliveryExceptionWorkflowService.startDeliveryExceptionWorkflow(payload1)
            val outbound2 = lookupDriverLicenseRequest {
                this.context = request.context
                this.driverLicenseId = request.driverLicenseId
            }
            val adapter2 = OutboundPort<LookupDriverLicenseRequest, LookupDriverLicenseResponse> { payload ->
                driversDriverLicenseQueryService.lookupDriverLicense(payload)
            }
            val port2 = DelegatingPort(adapter2)
            val reply2 = port2.execute(outbound2)
            val outbound3 = advanceProofOfDeliveryWorkflowRequest {
                this.context = request.context
                this.details = AdvanceProofOfDeliveryWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.email = reply0.driver.contact.email
                    }
                }
                this.proofOfDelivery = proofOfDelivery {
                    this.audit = auditInfo {
                        this.createdAt = reply0.driver.audit.createdAt
                        this.etag = reply2.driverLicense.audit.etag
                    }
                }
            }
            val reply3 = invokeShareDriverLicenseReport3(outbound3)
            shareDriverLicenseReportResponse {
                this.driverLicense = driverLicense {
                    this.audit = auditInfo {
                        this.etag = reply1.deliveryException.audit.etag
                    }
                    this.counterparty = driverRef {
                        this.displayName = reply0.driver.relationshipManager.displayName
                        this.email = reply2.driverLicense.counterparty.email
                    }
                    this.driverLicenseId = request.driverLicenseId
                    this.issuedAt = reply3.proofOfDelivery.issuedAt
                }
                this.outcome = ShareDriverLicenseReportResponseKt.outcome {
                    this.status = reply3.result.status
                }
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleStreamDriverLicenseMetrics(request: StreamDriverLicenseMetricsRequest): StreamDriverLicenseMetricsResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseReportingService/StreamDriverLicenseMetrics") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StreamDriverLicenseMetrics", request)
            log.atInfo().addKeyValue("operation", "StreamDriverLicenseMetrics").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = resumeTelemetryReadingWorkflowRequest {
                this.context = request.context
                this.details = ResumeTelemetryReadingWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.email = request.subscriber.email
                        this.name = request.subscriber.name
                        this.phone = request.subscriber.phone
                    }
                }
            }
            val invoke0: suspend (ResumeTelemetryReadingWorkflowRequest, Metadata) -> ResumeTelemetryReadingWorkflowResponse = telematicsTelemetryReadingWorkflowService::resumeTelemetryReadingWorkflow
            val reply0 = invoke0(outbound0, Metadata())
            streamDriverLicenseMetricsResponse {
                this.driverLicense = driverLicense {
                    this.billingAddress = postalAddress {
                        this.region = request.filter.region
                    }
                    this.counterparty = driverRef {
                        this.email = request.subscriber.email
                    }
                }
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }
}
