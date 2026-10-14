package com.acme.transport.telematics.adapters

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.deliveries.v1.DeliveryExceptionWorkflowServiceGrpcKt
import com.acme.deliveries.v1.StartDeliveryExceptionWorkflowRequest
import com.acme.deliveries.v1.StartDeliveryExceptionWorkflowResponse
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.deliveryExceptionStatusChange
import com.acme.deliveries.v1.startDeliveryExceptionWorkflowRequest
import com.acme.identity.v1.BrowseUsersRequest
import com.acme.identity.v1.BrowseUsersRequestKt
import com.acme.identity.v1.ExportUsersRequestKt
import com.acme.identity.v1.UserQueryServiceGrpcKt
import com.acme.identity.v1.browseUsersRequest
import com.acme.identity.v1.exportUsersRequest
import com.acme.identity.v1.userRef
import com.acme.licensing.v1.SoftwareLicenseServiceGrpcKt
import com.acme.licensing.v1.watchSoftwareLicensesRequest
import com.acme.metrics.v1.CountMetricSeriesesRequest
import com.acme.metrics.v1.CountMetricSeriesesRequestKt
import com.acme.metrics.v1.CountMetricSeriesesResponse
import com.acme.metrics.v1.MetricSeriesQueryServiceGrpcKt
import com.acme.metrics.v1.countMetricSeriesesRequest
import com.acme.payroll.v1.PayRunCalculationServiceGrpcKt
import com.acme.payroll.v1.QuotePayRunRequest
import com.acme.payroll.v1.payRun
import com.acme.payroll.v1.payRunLine
import com.acme.payroll.v1.payRunLineTax
import com.acme.payroll.v1.quotePayRunRequest
import com.acme.telematics.v1.AdvanceGeoFenceWorkflowRequest
import com.acme.telematics.v1.AdvanceGeoFenceWorkflowResponse
import com.acme.telematics.v1.CancelGeoFenceWorkflowRequest
import com.acme.telematics.v1.CancelGeoFenceWorkflowResponse
import com.acme.telematics.v1.GeoFenceSyncServiceGrpcKt
import com.acme.telematics.v1.GeoFenceWorkflowServiceGrpcKt
import com.acme.telematics.v1.InspectGeoFenceWorkflowRequest
import com.acme.telematics.v1.InspectGeoFenceWorkflowResponse
import com.acme.telematics.v1.RetryGeoFenceWorkflowRequest
import com.acme.telematics.v1.RetryGeoFenceWorkflowResponse
import com.acme.telematics.v1.SignalGeoFenceWorkflowRequest
import com.acme.telematics.v1.SignalGeoFenceWorkflowResponse
import com.acme.telematics.v1.StartGeoFenceWorkflowRequest
import com.acme.telematics.v1.StartGeoFenceWorkflowResponse
import com.acme.telematics.v1.advanceGeoFenceWorkflowResponse
import com.acme.telematics.v1.cancelGeoFenceWorkflowResponse
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.getGeoFenceSyncStateRequest
import com.acme.telematics.v1.inspectGeoFenceWorkflowResponse
import com.acme.telematics.v1.retryGeoFenceWorkflowResponse
import com.acme.telematics.v1.signalGeoFenceWorkflowResponse
import com.acme.telematics.v1.startGeoFenceWorkflowResponse
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.transport.telematics.TelematicsRepository
import com.acme.venues.v1.FindVenuesRequest
import com.acme.venues.v1.FindVenuesRequestKt
import com.acme.venues.v1.VenueQueryServiceGrpcKt
import com.acme.venues.v1.VenueServiceGrpcKt
import com.acme.venues.v1.cloneVenueRequest
import com.acme.venues.v1.findVenuesRequest
import com.acme.venues.v1.venue
import com.google.protobuf.Any as ProtoAny
import com.google.protobuf.Timestamp
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import org.slf4j.LoggerFactory

/** Runs the long-lived workflow that takes a geo fence from start to finish. */
class GeoFenceWorkflowController(
    private val store: TelematicsRepository,
    private val workers: CoroutineScope,
    private val deliveriesDeliveryExceptionWorkflowService: DeliveryExceptionWorkflowServiceGrpcKt.DeliveryExceptionWorkflowServiceCoroutineStub,
    private val identityUserQueryService: UserQueryServiceGrpcKt.UserQueryServiceCoroutineStub,
    private val licensingSoftwareLicenseService: SoftwareLicenseServiceGrpcKt.SoftwareLicenseServiceCoroutineStub,
    private val metricsMetricSeriesQueryService: MetricSeriesQueryServiceGrpcKt.MetricSeriesQueryServiceCoroutineStub,
    private val payrollPayRunCalculationService: PayRunCalculationServiceGrpcKt.PayRunCalculationServiceCoroutineStub,
    private val telematicsGeoFenceSyncService: GeoFenceSyncServiceGrpcKt.GeoFenceSyncServiceCoroutineStub,
    private val venuesVenueQueryService: VenueQueryServiceGrpcKt.VenueQueryServiceCoroutineStub,
    private val venuesVenueService: VenueServiceGrpcKt.VenueServiceCoroutineStub,
) : GeoFenceWorkflowServiceGrpcKt.GeoFenceWorkflowServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(GeoFenceWorkflowController::class.java)

    /** Advances a geo fence. */
    override suspend fun advanceGeoFenceWorkflow(request: AdvanceGeoFenceWorkflowRequest): AdvanceGeoFenceWorkflowResponse = handleAdvanceGeoFenceWorkflow(request)

    /** Cancels a geo fence. */
    override suspend fun cancelGeoFenceWorkflow(request: CancelGeoFenceWorkflowRequest): CancelGeoFenceWorkflowResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceWorkflowService/CancelGeoFenceWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CancelGeoFenceWorkflow", request)
            log.atInfo().addKeyValue("operation", "CancelGeoFenceWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.geoFence.geoFenceId).log("Recorded audit detail")
            val outbound0 = CancelGeoFenceWorkflow0Command(
                context = request.context,
                filterCreatedAfter = request.geoFence.audit.updatedAt,
                filterOwnerId = request.geoFence.owner.userId,
                filterLabels = request.annotationsMap
            ).toRequest()
            val reply0 = identityUserQueryService.browseUsers(outbound0)
            val outbound1 = watchSoftwareLicensesRequest {
                this.context = request.context
                this.subscriber = contactInfo {
                    this.email = request.geoFence.owner.email
                    this.name = request.details.notify.name
                    this.phone = request.details.notify.phone
                    this.preferredChannel = request.details.notify.preferredChannel
                }
            }
            licensingSoftwareLicenseService.watchSoftwareLicenses(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            cancelGeoFenceWorkflowResponse {
                this.geoFence = geoFence {
                    this.geoFenceId = request.geoFenceId
                    this.status = request.geoFence.status
                }
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }

    /** Returns a geo fence. */
    override suspend fun inspectGeoFenceWorkflow(request: InspectGeoFenceWorkflowRequest): InspectGeoFenceWorkflowResponse {
        return handleInspectGeoFenceWorkflow(request)
    }

    /** Retrys a geo fence. */
    override suspend fun retryGeoFenceWorkflow(request: RetryGeoFenceWorkflowRequest): RetryGeoFenceWorkflowResponse = handleRetryGeoFenceWorkflow(request)

    /** Signals a geo fence. */
    override suspend fun signalGeoFenceWorkflow(request: SignalGeoFenceWorkflowRequest): SignalGeoFenceWorkflowResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceWorkflowService/SignalGeoFenceWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SignalGeoFenceWorkflow", request)
            log.atInfo().addKeyValue("operation", "SignalGeoFenceWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.geoFence.geoFenceId).log("Recorded audit detail")
            val outbound0 = FindVenuesRequest.newBuilder().apply {
                setContext(request.context)
                setFilter(FindVenuesRequestKt.filter {
                    this.createdAfter = request.geoFence.audit.updatedAt
                    this.labels.putAll(request.details.parametersMap)
                    this.ownerId = request.geoFence.owner.userId
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> venuesVenueQueryService.findVenues(value) }
            signalGeoFenceWorkflowResponse {
                this.geoFence = geoFence {
                    this.geoFenceId = request.geoFenceId
                    this.parameters.putAll(request.details.parametersMap)
                }
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }

    /** Starts a geo fence. */
    override suspend fun startGeoFenceWorkflow(request: StartGeoFenceWorkflowRequest): StartGeoFenceWorkflowResponse {
        return handleStartGeoFenceWorkflow(request)
    }

    private suspend fun handleAdvanceGeoFenceWorkflow(request: AdvanceGeoFenceWorkflowRequest): AdvanceGeoFenceWorkflowResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceWorkflowService/AdvanceGeoFenceWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "AdvanceGeoFenceWorkflow", request)
            log.atInfo().addKeyValue("operation", "AdvanceGeoFenceWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.geoFence.name).log("Recorded audit detail")
            val outbound0 = quotePayRunRequest {
                this.context = request.context
                this.payRun = payRun {
                    this.lines.addAll(listOf(payRunLine {
                        this.tax = payRunLineTax {
                            this.amount = request.geoFence.discount.amount
                        }
                    }))
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = payrollPayRunCalculationService.quotePayRun(envelope0.unpack(QuotePayRunRequest::class.java))
            val outbound1 = getGeoFenceSyncStateRequest {
                this.context = request.context
                this.geoFenceId = request.geoFence.geoFenceId
            }
            val reply1 = telematicsGeoFenceSyncService.getGeoFenceSyncState(WireCopies.json(outbound1))
            advanceGeoFenceWorkflowResponse {
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.createdAt = reply1.geoFence.audit.createdAt
                    }
                    this.description = request.geoFence.description
                    this.geoFenceId = request.geoFenceId
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private data class CancelGeoFenceWorkflow0Command(
        val context: RequestContext,
        val filterCreatedAfter: Timestamp,
        val filterOwnerId: String,
        val filterLabels: Map<String, String>
    ) {
        fun toRequest(): BrowseUsersRequest {
            val command = this
            return browseUsersRequest {
                this.context = command.context
                this.filter = BrowseUsersRequestKt.filter {
                    this.createdAfter = command.filterCreatedAfter
                    this.labels.putAll(command.filterLabels)
                    this.ownerId = command.filterOwnerId
                }
            }
        }
    }

    private suspend fun handleInspectGeoFenceWorkflow(request: InspectGeoFenceWorkflowRequest): InspectGeoFenceWorkflowResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceWorkflowService/InspectGeoFenceWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "InspectGeoFenceWorkflow", request)
            log.atInfo().addKeyValue("operation", "InspectGeoFenceWorkflow").log("Handling transport request")
            val outbound0 = startDeliveryExceptionWorkflowRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.assignee = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                    this.history.addAll(listOf(deliveryExceptionStatusChange {
                        this.changedAt = request.context.deadline
                    }))
                }
            }
            val adapter0 = OutboundPort<StartDeliveryExceptionWorkflowRequest, StartDeliveryExceptionWorkflowResponse> { payload ->
                deliveriesDeliveryExceptionWorkflowService.startDeliveryExceptionWorkflow(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            inspectGeoFenceWorkflowResponse {
                this.geoFence = geoFence {
                    this.geoFenceId = request.geoFenceId
                    this.owner = userRef {
                        this.email = reply0.deliveryException.requester.email
                    }
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleRetryGeoFenceWorkflow(request: RetryGeoFenceWorkflowRequest): RetryGeoFenceWorkflowResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceWorkflowService/RetryGeoFenceWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RetryGeoFenceWorkflow", request)
            log.atInfo().addKeyValue("operation", "RetryGeoFenceWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.geoFence.name).log("Recorded audit detail")
            val outbound0 = exportUsersRequest {
                this.context = request.context
                this.filter = ExportUsersRequestKt.filter {
                    this.createdAfter = request.geoFence.audit.updatedAt
                    this.labels.putAll(request.geoFence.parametersMap)
                    this.ownerId = request.geoFence.owner.userId
                }
            }
            identityUserQueryService.exportUsers(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = countMetricSeriesesRequest {
                this.context = request.context
                this.filter = CountMetricSeriesesRequestKt.filter {
                    this.createdAfter = request.geoFence.audit.updatedAt
                    this.ownerId = request.geoFence.geoFenceId
                }
            }
            val invoke1: suspend (CountMetricSeriesesRequest, Metadata) -> CountMetricSeriesesResponse = metricsMetricSeriesQueryService::countMetricSerieses
            val reply1 = invoke1(outbound1, Metadata())
            retryGeoFenceWorkflowResponse {
                this.geoFence = geoFence {
                    this.geoFenceId = request.geoFenceId
                    this.priority = request.geoFence.priority
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.status = reply1.result.status
                }
            }
        }

    private suspend fun handleStartGeoFenceWorkflow(request: StartGeoFenceWorkflowRequest): StartGeoFenceWorkflowResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceWorkflowService/StartGeoFenceWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StartGeoFenceWorkflow", request)
            log.atInfo().addKeyValue("operation", "StartGeoFenceWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.geoFence.geoFenceId).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                cloneVenueRequest {
                    this.context = request.context
                    this.venue = venue {
                        this.audit = auditInfo {
                            this.createdAt = request.geoFence.audit.createdAt
                            this.etag = request.geoFence.audit.etag
                        }
                        this.owner = userRef {
                            this.displayName = request.geoFence.owner.displayName
                            this.email = request.geoFence.owner.email
                        }
                    }
                }
            }
            val reply0 = venuesVenueService.cloneVenue(outbound0)
            startGeoFenceWorkflowResponse {
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.etag = reply0.venue.audit.etag
                    }
                    this.description = request.geoFence.description
                    this.geoFenceId = request.geoFenceId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }
}
