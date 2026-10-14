package com.acme.transport.drivers.adapters

import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.customerRef
import com.acme.drivers.v1.CountDutyStatusTotalsRequest
import com.acme.drivers.v1.CountDutyStatusTotalsResponse
import com.acme.drivers.v1.DeleteDutyStatusReportRequest
import com.acme.drivers.v1.DeleteDutyStatusReportResponse
import com.acme.drivers.v1.DriverLicenseQueryServiceGrpcKt
import com.acme.drivers.v1.DriverQueryServiceGrpcKt
import com.acme.drivers.v1.DutyStatusReportingServiceGrpcKt
import com.acme.drivers.v1.FetchDutyStatusReportRequest
import com.acme.drivers.v1.FetchDutyStatusReportResponse
import com.acme.drivers.v1.FindDriverLicensesRequestKt
import com.acme.drivers.v1.FindDriversRequestKt
import com.acme.drivers.v1.GenerateDutyStatusReportRequest
import com.acme.drivers.v1.GenerateDutyStatusReportResponse
import com.acme.drivers.v1.ListDutyStatusReportsRequest
import com.acme.drivers.v1.ListDutyStatusReportsResponse
import com.acme.drivers.v1.ScheduleDutyStatusReportRequest
import com.acme.drivers.v1.ScheduleDutyStatusReportResponse
import com.acme.drivers.v1.ShareDutyStatusReportRequest
import com.acme.drivers.v1.ShareDutyStatusReportResponse
import com.acme.drivers.v1.StreamDutyStatusMetricsRequest
import com.acme.drivers.v1.StreamDutyStatusMetricsResponse
import com.acme.drivers.v1.SummarizeDriverLicensesRequestKt
import com.acme.drivers.v1.countDutyStatusTotalsResponse
import com.acme.drivers.v1.deleteDutyStatusReportResponse
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.dutyStatus
import com.acme.drivers.v1.fetchDutyStatusReportResponse
import com.acme.drivers.v1.findDriverLicensesRequest
import com.acme.drivers.v1.findDriversRequest
import com.acme.drivers.v1.generateDutyStatusReportResponse
import com.acme.drivers.v1.listDutyStatusReportsResponse
import com.acme.drivers.v1.scheduleDutyStatusReportResponse
import com.acme.drivers.v1.shareDutyStatusReportResponse
import com.acme.drivers.v1.streamDutyStatusMetricsResponse
import com.acme.drivers.v1.summarizeDriverLicensesRequest
import com.acme.fuel.v1.FuelCardSyncServiceGrpcKt
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.resolveFuelCardConflictRequest
import com.acme.identity.v1.userRef
import com.acme.returns.v1.CloneReturnRequestRequestKt
import com.acme.returns.v1.ListReturnRequestsRequestKt
import com.acme.returns.v1.ReturnRequestServiceGrpcKt
import com.acme.returns.v1.cloneReturnRequestRequest
import com.acme.returns.v1.listReturnRequestsRequest
import com.acme.returns.v1.returnRequest
import com.acme.returns.v1.returnRequestLine
import com.acme.routing.v1.AdvanceStopWorkflowRequest
import com.acme.routing.v1.AdvanceStopWorkflowRequestKt
import com.acme.routing.v1.DelegateRouteApprovalRequestKt
import com.acme.routing.v1.RouteApprovalServiceGrpcKt
import com.acme.routing.v1.StopWorkflowServiceGrpcKt
import com.acme.routing.v1.advanceStopWorkflowRequest
import com.acme.routing.v1.delegateRouteApprovalRequest
import com.acme.routing.v1.retryStopWorkflowRequest
import com.acme.routing.v1.route
import com.acme.routing.v1.stop
import com.acme.routing.v1.stopParticipant
import com.acme.telematics.v1.ApproveGeoFenceRequest
import com.acme.telematics.v1.ApproveGeoFenceRequestKt
import com.acme.telematics.v1.GeoFenceApprovalServiceGrpcKt
import com.acme.telematics.v1.approveGeoFenceRequest
import com.acme.telematics.v1.geoFence
import com.acme.transport.drivers.DriversRepository
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.venues.v1.BrowseVenuesRequestKt
import com.acme.venues.v1.VenueQueryServiceGrpcKt
import com.acme.venues.v1.browseVenuesRequest
import com.acme.venues.v1.copy as venuesCopy
import com.google.protobuf.Any as ProtoAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Builds and schedules reports about duty statuses. */
class DutyStatusReportingController(
    private val store: DriversRepository,
    private val workers: CoroutineScope,
    private val driversDriverLicenseQueryService: DriverLicenseQueryServiceGrpcKt.DriverLicenseQueryServiceCoroutineStub,
    private val driversDriverQueryService: DriverQueryServiceGrpcKt.DriverQueryServiceCoroutineStub,
    private val fuelFuelCardSyncService: FuelCardSyncServiceGrpcKt.FuelCardSyncServiceCoroutineStub,
    private val returnsReturnRequestService: ReturnRequestServiceGrpcKt.ReturnRequestServiceCoroutineStub,
    private val routingRouteApprovalService: RouteApprovalServiceGrpcKt.RouteApprovalServiceCoroutineStub,
    private val routingStopWorkflowService: StopWorkflowServiceGrpcKt.StopWorkflowServiceCoroutineStub,
    private val telematicsGeoFenceApprovalService: GeoFenceApprovalServiceGrpcKt.GeoFenceApprovalServiceCoroutineStub,
    private val venuesVenueQueryService: VenueQueryServiceGrpcKt.VenueQueryServiceCoroutineStub,
) : DutyStatusReportingServiceGrpcKt.DutyStatusReportingServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DutyStatusReportingController::class.java)

    private val clientForCountDutyStatusTotals0 by lazy { routingRouteApprovalService }

    /** Counts duty statuses by status. */
    override suspend fun countDutyStatusTotals(request: CountDutyStatusTotalsRequest): CountDutyStatusTotalsResponse = handleCountDutyStatusTotals(request)

    /** Deletes a duty status. */
    override suspend fun deleteDutyStatusReport(request: DeleteDutyStatusReportRequest): DeleteDutyStatusReportResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusReportingService/DeleteDutyStatusReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DeleteDutyStatusReport", request)
            log.atInfo().addKeyValue("operation", "DeleteDutyStatusReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.reason).log("Recorded audit detail")
            val outbound0 = findDriverLicensesRequest {
                this.context = request.context
                this.filter = FindDriverLicensesRequestKt.filter {
                    this.createdAfter = request.context.deadline
                    this.labels.putAll(request.context.baggageMap)
                    this.ownerId = request.dutyStatusId
                }
            }
            val reply0 = withTimeout(5_000) { driversDriverLicenseQueryService.findDriverLicenses(outbound0) }
            val configure1: CloneReturnRequestRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.returnRequest = returnRequest {
                    this.audit = auditInfo {
                        this.etag = request.etag
                        this.updatedAt = request.context.deadline
                    }
                    this.counterparty = customerRef {
                        this.email = request.context.actor.email
                    }
                    this.lines.addAll(listOf(returnRequestLine {
                        this.attributes.putAll(reply0.result.detailsMap)
                    }))
                }
            }
            val outbound1 = cloneReturnRequestRequest(configure1)
            val reply1 = returnsReturnRequestService.cloneReturnRequest(outbound1)
            deleteDutyStatusReportResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = reply1.result.status
                }
            }
        }

    /** Returns a duty status. */
    override suspend fun fetchDutyStatusReport(request: FetchDutyStatusReportRequest): FetchDutyStatusReportResponse {
        return handleFetchDutyStatusReport(request)
    }

    /** Generates a duty status. */
    override suspend fun generateDutyStatusReport(request: GenerateDutyStatusReportRequest): GenerateDutyStatusReportResponse = handleGenerateDutyStatusReport(request)

    /** Lists duty statuses a page at a time. */
    override suspend fun listDutyStatusReports(request: ListDutyStatusReportsRequest): ListDutyStatusReportsResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusReportingService/ListDutyStatusReports") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListDutyStatusReports", request)
            log.atInfo().addKeyValue("operation", "ListDutyStatusReports").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            listDutyStatusReportsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Schedules a duty status. */
    override suspend fun scheduleDutyStatusReport(request: ScheduleDutyStatusReportRequest): ScheduleDutyStatusReportResponse {
        return handleScheduleDutyStatusReport(request)
    }

    /** Shares a duty status. */
    override suspend fun shareDutyStatusReport(request: ShareDutyStatusReportRequest): ShareDutyStatusReportResponse = handleShareDutyStatusReport(request)

    /** Streams the changes of duty statuses as they happen. */
    override fun streamDutyStatusMetrics(request: StreamDutyStatusMetricsRequest): Flow<StreamDutyStatusMetricsResponse> = flow {
        emit(handleStreamDutyStatusMetrics(request))
    }

    private suspend fun handleCountDutyStatusTotals(request: CountDutyStatusTotalsRequest): CountDutyStatusTotalsResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusReportingService/CountDutyStatusTotals") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CountDutyStatusTotals", request)
            log.atInfo().addKeyValue("operation", "CountDutyStatusTotals").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = delegateRouteApprovalRequest {
                this.context = request.context
                this.details = DelegateRouteApprovalRequestKt.details {
                    this.approver = userRef {
                        this.userId = request.context.actor.userId
                    }
                }
                this.route = route {
                    this.billingAddress = postalAddress {
                        this.region = request.filter.region
                    }
                    this.counterparty = driverRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = clientForCountDutyStatusTotals0.delegateRouteApproval(outbound0)
            val outbound1 = findDriversRequest {
                this.context = request.context
                this.filter = FindDriversRequestKt.filter {
                    this.createdAfter = request.filter.createdAfter
                    this.ownerId = request.filter.ownerId
                }
            }
            val reply1 = flowOf(outbound1).map { value -> driversDriverQueryService.findDrivers(value) }.single()
            countDutyStatusTotalsResponse {
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.message = reply0.outcome.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleFetchDutyStatusReport(request: FetchDutyStatusReportRequest): FetchDutyStatusReportResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusReportingService/FetchDutyStatusReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "FetchDutyStatusReport", request)
            log.atInfo().addKeyValue("operation", "FetchDutyStatusReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            fetchDutyStatusReportResponse {
                this.dutyStatus = dutyStatus {
                    this.dutyStatusId = request.dutyStatusId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleGenerateDutyStatusReport(request: GenerateDutyStatusReportRequest): GenerateDutyStatusReportResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusReportingService/GenerateDutyStatusReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GenerateDutyStatusReport", request)
            log.atInfo().addKeyValue("operation", "GenerateDutyStatusReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dutyStatusId).log("Recorded audit detail")
            val outbound0 = resolveFuelCardConflictRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.fuelCard = fuelCard {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                    this.labels.putAll(request.details.filtersMap)
                }
                this.reason = request.reason
            }
            val reply0 = fuelFuelCardSyncService.resolveFuelCardConflict(outbound0)
            val outbound1 = retryStopWorkflowRequest {
                this.context = request.context
                this.stop = stop {
                    this.participants.addAll(listOf(stopParticipant {
                        this.contact = contactInfo {
                            this.name = reply0.fuelCard.contact.name
                        }
                    }))
                }
            }
            val reply1 = routingStopWorkflowService.retryStopWorkflow(outbound1)
            generateDutyStatusReportResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
            }
        }

    private fun ScheduleDutyStatusReportRequest.toAdvanceStopWorkflowFromScheduleDutyStatusReport1(): AdvanceStopWorkflowRequest {
        val source = this
        return advanceStopWorkflowRequest {
            this.annotations.putAll(source.annotationsMap)
            this.context = source.context
            this.details = AdvanceStopWorkflowRequestKt.details {
                this.notify = contactInfo {
                    this.email = source.context.actor.email
                }
            }
            this.reason = source.reason
        }
    }

    private suspend fun handleScheduleDutyStatusReport(request: ScheduleDutyStatusReportRequest): ScheduleDutyStatusReportResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusReportingService/ScheduleDutyStatusReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScheduleDutyStatusReport", request)
            log.atInfo().addKeyValue("operation", "ScheduleDutyStatusReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.format).log("Recorded audit detail")
            val outbound0 = browseVenuesRequest {}.venuesCopy {
                this.context = request.context
                this.filter = BrowseVenuesRequestKt.filter {
                    this.createdAfter = request.context.deadline
                    this.labels.putAll(request.details.filtersMap)
                    this.ownerId = request.dutyStatusId
                }
            }
            val reply0 = venuesVenueQueryService.browseVenues(outbound0)
            val outbound1 = request.toAdvanceStopWorkflowFromScheduleDutyStatusReport1()
            val reply1 = routingStopWorkflowService.advanceStopWorkflow(outbound1)
            scheduleDutyStatusReportResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply1.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleShareDutyStatusReport(request: ShareDutyStatusReportRequest): ShareDutyStatusReportResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusReportingService/ShareDutyStatusReport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ShareDutyStatusReport", request)
            log.atInfo().addKeyValue("operation", "ShareDutyStatusReport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.recipient.email).log("Recorded audit detail")
            val outbound0 = listReturnRequestsRequest {
                this.context = request.context
                this.filter = ListReturnRequestsRequestKt.filter {
                    this.createdAfter = request.context.deadline
                    this.labels.putAll(request.details.variablesMap)
                    this.ownerId = request.details.templateId
                }
            }
            val reply0 = returnsReturnRequestService.listReturnRequests(outbound0)
            shareDutyStatusReportResponse {
                this.dutyStatus = dutyStatus {
                    this.dutyStatusId = request.dutyStatusId
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleStreamDutyStatusMetrics(request: StreamDutyStatusMetricsRequest): StreamDutyStatusMetricsResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusReportingService/StreamDutyStatusMetrics") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StreamDutyStatusMetrics", request)
            log.atInfo().addKeyValue("operation", "StreamDutyStatusMetrics").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = approveGeoFenceRequest {
                this.context = request.context
                this.details = ApproveGeoFenceRequestKt.details {
                    this.approver = userRef {
                        this.email = request.subscriber.email
                        this.userId = request.context.actor.userId
                    }
                }
                this.geoFence = geoFence {
                    this.owner = userRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = telematicsGeoFenceApprovalService.approveGeoFence(envelope0.unpack(ApproveGeoFenceRequest::class.java))
            val outbound1 = summarizeDriverLicensesRequest {
                this.context = request.context
                this.details = SummarizeDriverLicensesRequestKt.details {
                    this.period = reply0.geoFence.effective
                }
                this.reason = reply0.geoFence.block.reason
            }
            val reply1 = driversDriverLicenseQueryService.summarizeDriverLicenses(WireCopies.json(outbound1))
            streamDutyStatusMetricsResponse {
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }
}
