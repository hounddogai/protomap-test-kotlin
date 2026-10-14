package com.acme.transport.drivers.api

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.customerRef
import com.acme.dispatch.v1.SignalZoneWorkflowRequest
import com.acme.dispatch.v1.ZoneWorkflowServiceGrpcKt
import com.acme.dispatch.v1.signalZoneWorkflowRequest
import com.acme.dispatch.v1.zone
import com.acme.drivers.v1.AdvanceDriverWorkflowRequest
import com.acme.drivers.v1.AdvanceDriverWorkflowResponse
import com.acme.drivers.v1.CancelDriverWorkflowRequest
import com.acme.drivers.v1.CancelDriverWorkflowResponse
import com.acme.drivers.v1.CancelDriverWorkflowResponseKt
import com.acme.drivers.v1.DriverApprovalServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseReportingServiceGrpcKt
import com.acme.drivers.v1.DriverLifecycleServiceGrpcKt
import com.acme.drivers.v1.DriverWorkflowServiceGrpcKt
import com.acme.drivers.v1.DutyStatusCalculationServiceGrpcKt
import com.acme.drivers.v1.DutyStatusLifecycleServiceGrpcKt
import com.acme.drivers.v1.EstimateDutyStatusRequest
import com.acme.drivers.v1.EstimateDutyStatusRequestKt
import com.acme.drivers.v1.InspectDriverWorkflowRequest
import com.acme.drivers.v1.InspectDriverWorkflowResponse
import com.acme.drivers.v1.ListDriverLicenseReportsRequest
import com.acme.drivers.v1.ListDriverLicenseReportsRequestKt
import com.acme.drivers.v1.ListDriverWorkflowsRequest
import com.acme.drivers.v1.ListDriverWorkflowsResponse
import com.acme.drivers.v1.PauseDriverWorkflowRequest
import com.acme.drivers.v1.PauseDriverWorkflowResponse
import com.acme.drivers.v1.PauseDriverWorkflowResponseKt
import com.acme.drivers.v1.RenewDriverRequest
import com.acme.drivers.v1.RenewDriverRequestKt
import com.acme.drivers.v1.RenewDriverResponse
import com.acme.drivers.v1.ResumeDriverWorkflowRequest
import com.acme.drivers.v1.ResumeDriverWorkflowResponse
import com.acme.drivers.v1.RetryDriverWorkflowRequest
import com.acme.drivers.v1.RetryDriverWorkflowResponse
import com.acme.drivers.v1.ScheduleDutyStatusExpiryRequestKt
import com.acme.drivers.v1.SignalDriverWorkflowRequest
import com.acme.drivers.v1.SignalDriverWorkflowResponse
import com.acme.drivers.v1.SignalDriverWorkflowResponseKt
import com.acme.drivers.v1.SimulateDutyStatusRequestKt
import com.acme.drivers.v1.StartDriverWorkflowRequest
import com.acme.drivers.v1.StartDriverWorkflowResponse
import com.acme.drivers.v1.StartDriverWorkflowResponseKt
import com.acme.drivers.v1.StreamDriverLicenseMetricsRequestKt
import com.acme.drivers.v1.SuspendDriverRequestKt
import com.acme.drivers.v1.advanceDriverWorkflowResponse
import com.acme.drivers.v1.cancelDriverWorkflowResponse
import com.acme.drivers.v1.copy as driversCopy
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.driverLicenseRef
import com.acme.drivers.v1.driverPassport
import com.acme.drivers.v1.driverPreferences
import com.acme.drivers.v1.dutyStatus
import com.acme.drivers.v1.escalateDriverRequest
import com.acme.drivers.v1.estimateDutyStatusRequest
import com.acme.drivers.v1.inspectDriverWorkflowResponse
import com.acme.drivers.v1.listDriverLicenseReportsRequest
import com.acme.drivers.v1.listDriverWorkflowsResponse
import com.acme.drivers.v1.pauseDriverWorkflowResponse
import com.acme.drivers.v1.renewDriverRequest
import com.acme.drivers.v1.resumeDriverWorkflowResponse
import com.acme.drivers.v1.retryDriverWorkflowResponse
import com.acme.drivers.v1.scheduleDutyStatusExpiryRequest
import com.acme.drivers.v1.signalDriverWorkflowResponse
import com.acme.drivers.v1.simulateDutyStatusRequest
import com.acme.drivers.v1.startDriverWorkflowResponse
import com.acme.drivers.v1.streamDriverLicenseMetricsRequest
import com.acme.drivers.v1.suspendDriverRequest
import com.acme.fuel.v1.FuelTransactionSyncServiceGrpcKt
import com.acme.fuel.v1.ResolveFuelTransactionConflictRequest
import com.acme.fuel.v1.ResolveFuelTransactionConflictResponse
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.resolveFuelTransactionConflictRequest
import com.acme.identity.v1.userRef
import com.acme.search.v1.SearchIndexQueryServiceGrpcKt
import com.acme.search.v1.SuggestSearchIndexesRequestKt
import com.acme.search.v1.suggestSearchIndexesRequest
import com.acme.telematics.v1.CancelGeoFenceWorkflowRequestKt
import com.acme.telematics.v1.GeoFenceWorkflowServiceGrpcKt
import com.acme.telematics.v1.RetryGeoFenceWorkflowRequest
import com.acme.telematics.v1.RetryGeoFenceWorkflowRequestKt
import com.acme.telematics.v1.RetryGeoFenceWorkflowResponse
import com.acme.telematics.v1.RetryTripLogImportRequest
import com.acme.telematics.v1.TripLogImportServiceGrpcKt
import com.acme.telematics.v1.cancelGeoFenceWorkflowRequest
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.retryGeoFenceWorkflowRequest
import com.acme.telematics.v1.retryTripLogImportRequest
import com.acme.telematics.v1.tripLog
import com.acme.transport.drivers.DriversRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import org.slf4j.LoggerFactory

/** Runs the long-lived workflow that takes a driver from start to finish. */
class DriverWorkflowGrpcService(
    private val store: DriversRepository,
    private val workers: CoroutineScope,
    private val dispatchZoneWorkflowService: ZoneWorkflowServiceGrpcKt.ZoneWorkflowServiceCoroutineStub,
    private val driversDriverApprovalService: DriverApprovalServiceGrpcKt.DriverApprovalServiceCoroutineStub,
    private val driversDriverLicenseReportingService: DriverLicenseReportingServiceGrpcKt.DriverLicenseReportingServiceCoroutineStub,
    private val driversDriverLifecycleService: DriverLifecycleServiceGrpcKt.DriverLifecycleServiceCoroutineStub,
    private val driversDutyStatusCalculationService: DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineStub,
    private val driversDutyStatusLifecycleService: DutyStatusLifecycleServiceGrpcKt.DutyStatusLifecycleServiceCoroutineStub,
    private val fuelFuelTransactionSyncService: FuelTransactionSyncServiceGrpcKt.FuelTransactionSyncServiceCoroutineStub,
    private val searchSearchIndexQueryService: SearchIndexQueryServiceGrpcKt.SearchIndexQueryServiceCoroutineStub,
    private val telematicsGeoFenceWorkflowService: GeoFenceWorkflowServiceGrpcKt.GeoFenceWorkflowServiceCoroutineStub,
    private val telematicsTripLogImportService: TripLogImportServiceGrpcKt.TripLogImportServiceCoroutineStub,
) : DriverWorkflowServiceGrpcKt.DriverWorkflowServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DriverWorkflowGrpcService::class.java)

    private val invokeResumeDriverWorkflow0: suspend (RenewDriverRequest) -> RenewDriverResponse = { payload ->
        driversDriverLifecycleService.renewDriver(payload)
    }

    /** Advances a driver. */
    override suspend fun advanceDriverWorkflow(request: AdvanceDriverWorkflowRequest): AdvanceDriverWorkflowResponse {
        return handleAdvanceDriverWorkflow(request)
    }

    /** Cancels a driver. */
    override suspend fun cancelDriverWorkflow(request: CancelDriverWorkflowRequest): CancelDriverWorkflowResponse = handleCancelDriverWorkflow(request)

    /** Returns a driver. */
    override suspend fun inspectDriverWorkflow(request: InspectDriverWorkflowRequest): InspectDriverWorkflowResponse =
        RpcBoundary.run("acme.drivers.v1.DriverWorkflowService/InspectDriverWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "InspectDriverWorkflow", request)
            log.atInfo().addKeyValue("operation", "InspectDriverWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            inspectDriverWorkflowResponse {
                this.driver = driver {
                    this.driverId = request.driverId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Lists drivers a page at a time. */
    override suspend fun listDriverWorkflows(request: ListDriverWorkflowsRequest): ListDriverWorkflowsResponse {
        return handleListDriverWorkflows(request)
    }

    /** Pauses a driver. */
    override suspend fun pauseDriverWorkflow(request: PauseDriverWorkflowRequest): PauseDriverWorkflowResponse = handlePauseDriverWorkflow(request)

    /** Resumes a driver. */
    override suspend fun resumeDriverWorkflow(request: ResumeDriverWorkflowRequest): ResumeDriverWorkflowResponse =
        RpcBoundary.run("acme.drivers.v1.DriverWorkflowService/ResumeDriverWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResumeDriverWorkflow", request)
            log.atInfo().addKeyValue("operation", "ResumeDriverWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driver.driverId).log("Recorded audit detail")
            val outbound0 = renewDriverRequest {
                this.context = request.context
                this.details = RenewDriverRequestKt.details {
                    this.notifyContact = contactInfo {
                        this.email = request.driver.relationshipManager.email
                        this.preferredChannel = request.driver.contact.preferredChannel
                    }
                }
                this.driverId = request.driverId
            }
            val reply0 = invokeResumeDriverWorkflow0(outbound0)
            resumeDriverWorkflowResponse {
                this.driver = driver {
                    this.driverId = request.driverId
                    this.primaryAddress = request.driver.primaryAddress
                    this.status = reply0.driver.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Retrys a driver. */
    override suspend fun retryDriverWorkflow(request: RetryDriverWorkflowRequest): RetryDriverWorkflowResponse {
        return handleRetryDriverWorkflow(request)
    }

    /** Signals a driver. */
    override suspend fun signalDriverWorkflow(request: SignalDriverWorkflowRequest): SignalDriverWorkflowResponse = handleSignalDriverWorkflow(request)

    /** Starts a driver. */
    override suspend fun startDriverWorkflow(request: StartDriverWorkflowRequest): StartDriverWorkflowResponse =
        RpcBoundary.run("acme.drivers.v1.DriverWorkflowService/StartDriverWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StartDriverWorkflow", request)
            log.atInfo().addKeyValue("operation", "StartDriverWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driver.driverId).log("Recorded audit detail")
            val outbound0 = simulateDutyStatusRequest {
                this.context = request.context
                this.details = SimulateDutyStatusRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(request.driver.primaryAddress.linesList)
                        this.postalCode = request.driver.primaryAddress.postalCode
                    }
                }
                this.dutyStatus = dutyStatus {
                    this.subject = driverLicenseRef {
                        this.number = request.driver.nationalId.number
                    }
                }
            }
            val reply0 = driversDutyStatusCalculationService.simulateDutyStatus(WireCopies.reflective(outbound0))
            startDriverWorkflowResponse {
                this.driver = driver {
                    this.driverId = request.driverId
                    this.relationshipManager = userRef {
                        this.displayName = reply0.dutyStatus.subject.displayName
                    }
                    this.status = request.driver.status
                }
                this.outcome = StartDriverWorkflowResponseKt.outcome {
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleAdvanceDriverWorkflow(request: AdvanceDriverWorkflowRequest): AdvanceDriverWorkflowResponse =
        RpcBoundary.run("acme.drivers.v1.DriverWorkflowService/AdvanceDriverWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "AdvanceDriverWorkflow", request)
            log.atInfo().addKeyValue("operation", "AdvanceDriverWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driver.driverId).log("Recorded audit detail")
            val outbound0 = escalateDriverRequest {}.driversCopy {
                this.context = request.context
                this.driver = driver {
                    this.passport = driverPassport {
                        this.expiresAt = request.driver.passport.expiresAt
                    }
                    this.relationshipManager = userRef {
                        this.email = request.driver.contact.email
                    }
                }
            }
            val reply0 = driversDriverApprovalService.escalateDriver(outbound0)
            val outbound1 = streamDriverLicenseMetricsRequest {
                this.context = request.context
                this.filter = StreamDriverLicenseMetricsRequestKt.filter {
                    this.region = reply0.driver.primaryAddress.region
                }
                this.subscriber = contactInfo {
                    this.email = reply0.driver.relationshipManager.email
                }
            }
            driversDriverLicenseReportingService.streamDriverLicenseMetrics(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            advanceDriverWorkflowResponse {
                this.driver = driver {
                    this.contact = contactInfo {
                        this.email = reply0.driver.relationshipManager.email
                    }
                    this.driverId = request.driverId
                    this.preferences = driverPreferences {
                        this.language = reply0.driver.preferences.language
                    }
                    this.status = request.driver.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleCancelDriverWorkflow(request: CancelDriverWorkflowRequest): CancelDriverWorkflowResponse =
        RpcBoundary.run("acme.drivers.v1.DriverWorkflowService/CancelDriverWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CancelDriverWorkflow", request)
            log.atInfo().addKeyValue("operation", "CancelDriverWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driver.driverId).log("Recorded audit detail")
            val outbound0 = cancelGeoFenceWorkflowRequest {
                this.context = request.context
                this.details = CancelGeoFenceWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.phone = request.details.notify.phone
                    }
                }
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.createdAt = request.driver.audit.createdAt
                        this.createdBy = request.driver.audit.createdBy
                    }
                    this.owner = userRef {
                        this.email = request.driver.contact.email
                    }
                }
            }
            val reply0 = telematicsGeoFenceWorkflowService.cancelGeoFenceWorkflow(outbound0)
            val outbound1 = signalZoneWorkflowRequest {
                this.context = request.context
                this.zone = zone {
                    this.children.addAll(listOf(zone {
                        this.children.addAll(listOf(zone {
                            this.audit = auditInfo {
                                this.etag = reply0.geoFence.audit.etag
                            }
                            this.location = postalAddress {
                                this.postalCode = request.driver.primaryAddress.postalCode
                            }
                            this.manager = userRef {
                                this.displayName = reply0.geoFence.owner.displayName
                            }
                        }))
                    }))
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = dispatchZoneWorkflowService.signalZoneWorkflow(envelope1.unpack(SignalZoneWorkflowRequest::class.java))
            cancelDriverWorkflowResponse {
                this.driver = driver {
                    this.audit = auditInfo {
                        this.updatedAt = reply1.zone.audit.updatedAt
                    }
                    this.driverId = request.driverId
                    this.name = request.driver.name
                    this.relationshipManager = userRef {
                        this.userId = reply0.geoFence.owner.userId
                    }
                }
                this.outcome = CancelDriverWorkflowResponseKt.outcome {
                    this.status = reply0.outcome.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private data class ListDriverWorkflows1Command(
        val context: RequestContext,
        val filterRegion: String
    ) {
        fun toRequest(): ListDriverLicenseReportsRequest {
            val command = this
            return listDriverLicenseReportsRequest {
                this.context = command.context
                this.filter = ListDriverLicenseReportsRequestKt.filter {
                    this.region = command.filterRegion
                }
            }
        }
    }

    private suspend fun handleListDriverWorkflows(request: ListDriverWorkflowsRequest): ListDriverWorkflowsResponse =
        RpcBoundary.run("acme.drivers.v1.DriverWorkflowService/ListDriverWorkflows") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListDriverWorkflows", request)
            log.atInfo().addKeyValue("operation", "ListDriverWorkflows").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = scheduleDutyStatusExpiryRequest {
                this.context = request.context
                this.details = ScheduleDutyStatusExpiryRequestKt.details {
                    this.notify = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
                this.dutyStatus = dutyStatus {
                    this.attributes.putAll(request.filter.labelsMap)
                }
            }
            val reply0 = driversDutyStatusLifecycleService.scheduleDutyStatusExpiry(WireCopies.json(outbound0))
            val outbound1 = ListDriverWorkflows1Command(
                context = request.context,
                filterRegion = request.filter.region
            ).toRequest()
            val reply1 = driversDriverLicenseReportingService.listDriverLicenseReports(outbound1)
            listDriverWorkflowsResponse {
                this.result = operationResult {
                    this.referenceIds.addAll((reply0.result.referenceIdsList) + (reply1.result.referenceIdsList))
                    this.status = "ok"
                }
            }
        }

    private suspend fun handlePauseDriverWorkflow(request: PauseDriverWorkflowRequest): PauseDriverWorkflowResponse =
        RpcBoundary.run("acme.drivers.v1.DriverWorkflowService/PauseDriverWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PauseDriverWorkflow", request)
            log.atInfo().addKeyValue("operation", "PauseDriverWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driver.driverId).log("Recorded audit detail")
            val outbound0 = retryTripLogImportRequest {
                this.context = request.context
                this.tripLog = tripLog {
                    this.billingAddress = postalAddress {
                        this.region = request.driver.primaryAddress.region
                    }
                }
            }
            val command0: Submission<RetryTripLogImportRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = telematicsTripLogImportService.retryTripLogImport(payload0)
            val outbound1 = retryGeoFenceWorkflowRequest {
                this.context = request.context
                this.details = RetryGeoFenceWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.email = request.driver.contact.email
                    }
                }
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.createdAt = request.driver.audit.createdAt
                    }
                    this.owner = userRef {
                        this.displayName = reply0.tripLog.counterparty.displayName
                    }
                }
            }
            val adapter1 = OutboundPort<RetryGeoFenceWorkflowRequest, RetryGeoFenceWorkflowResponse> { payload ->
                telematicsGeoFenceWorkflowService.retryGeoFenceWorkflow(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            pauseDriverWorkflowResponse {
                this.driver = driver {
                    this.driverId = request.driverId
                    this.primaryAddress = request.driver.primaryAddress
                }
                this.outcome = PauseDriverWorkflowResponseKt.outcome {
                    this.effectiveAt = reply1.outcome.effectiveAt
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleRetryDriverWorkflow(request: RetryDriverWorkflowRequest): RetryDriverWorkflowResponse =
        RpcBoundary.run("acme.drivers.v1.DriverWorkflowService/RetryDriverWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RetryDriverWorkflow", request)
            log.atInfo().addKeyValue("operation", "RetryDriverWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driver.driverId).log("Recorded audit detail")
            val outbound0 = resolveFuelTransactionConflictRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.fuelTransaction = fuelTransaction {
                    this.attributes.putAll(request.driver.attributesMap)
                    this.subject = customerRef {
                        this.email = request.driver.contact.email
                    }
                }
            }
            val invoke0: suspend (ResolveFuelTransactionConflictRequest, Metadata) -> ResolveFuelTransactionConflictResponse = fuelFuelTransactionSyncService::resolveFuelTransactionConflict
            val reply0 = invoke0(outbound0, Metadata())
            val outbound1 = EstimateDutyStatusRequest.newBuilder().apply {
                setContext(request.context)
                setDetails(EstimateDutyStatusRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = request.driver.primaryAddress.countryCode
                        this.lines.addAll(request.driver.primaryAddress.linesList)
                        this.locality = request.driver.primaryAddress.locality
                        this.postalCode = request.driver.primaryAddress.postalCode
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> driversDutyStatusCalculationService.estimateDutyStatus(value) }
            retryDriverWorkflowResponse {
                this.driver = driver {
                    this.driverId = request.driverId
                    this.name = request.driver.name
                    this.relationshipManager = userRef {
                        this.displayName = reply1.dutyStatus.subject.displayName
                    }
                }
                this.result = operationResult {
                    this.message = reply0.outcome.message
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSignalDriverWorkflow(request: SignalDriverWorkflowRequest): SignalDriverWorkflowResponse =
        RpcBoundary.run("acme.drivers.v1.DriverWorkflowService/SignalDriverWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SignalDriverWorkflow", request)
            log.atInfo().addKeyValue("operation", "SignalDriverWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driver.contact.phone).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                suspendDriverRequest {
                    this.context = request.context
                    this.details = SuspendDriverRequestKt.details {
                        this.newOwner = userRef {
                            this.email = request.driver.relationshipManager.email
                        }
                        this.notifyContact = contactInfo {
                            this.email = request.driver.contact.email
                            this.name = request.details.notify.name
                            this.preferredChannel = request.driver.contact.preferredChannel
                        }
                    }
                    this.driverId = request.driver.driverId
                }
            }
            val reply0 = driversDriverLifecycleService.suspendDriver(outbound0)
            val outbound1 = suggestSearchIndexesRequest {
                this.context = request.context
                this.filter = SuggestSearchIndexesRequestKt.filter {
                    this.createdAfter = reply0.driver.audit.updatedAt
                    this.ownerId = request.driver.relationshipManager.userId
                    this.region = request.driver.primaryAddress.region
                }
            }
            val reply1 = ClientRegistry.lookup("acme.search.v1.SearchIndexQueryService", SearchIndexQueryServiceGrpcKt.SearchIndexQueryServiceCoroutineStub::class.java).suggestSearchIndexes(outbound1)
            signalDriverWorkflowResponse {
                this.driver = driver {
                    this.audit = auditInfo {
                        this.createdBy = reply0.driver.audit.createdBy
                    }
                    this.driverId = request.driverId
                    this.relationshipManager = request.driver.relationshipManager
                }
                this.outcome = SignalDriverWorkflowResponseKt.outcome {
                    this.referenceId = reply0.outcome.referenceId
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = "ok"
                }
            }
        }
}
