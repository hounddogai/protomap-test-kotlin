package com.acme.transport.driverapp.api

import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.LintAuditEventsRequestKt
import com.acme.audit.v1.lintAuditEventsRequest
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.customers.v1.ApproveCustomerSegmentRequest
import com.acme.customers.v1.ApproveCustomerSegmentRequestKt
import com.acme.customers.v1.ApproveCustomerSegmentResponse
import com.acme.customers.v1.ConsentRecordApprovalServiceGrpcKt
import com.acme.customers.v1.CustomerSegmentApprovalServiceGrpcKt
import com.acme.customers.v1.ListPendingConsentRecordsRequestKt
import com.acme.customers.v1.approveCustomerSegmentRequest
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.listPendingConsentRecordsRequest
import com.acme.dispatch.v1.ZoneApprovalServiceGrpcKt
import com.acme.dispatch.v1.approveZoneRequest
import com.acme.dispatch.v1.zone
import com.acme.driverapp.v1.ComposeDriverHomePageRequest
import com.acme.driverapp.v1.ComposeDriverHomePageResponse
import com.acme.driverapp.v1.ConnectDriverHomeSourceRequestKt
import com.acme.driverapp.v1.DelegateDriverHomeApprovalResponse
import com.acme.driverapp.v1.DelegateDriverInboxApprovalRequest
import com.acme.driverapp.v1.DelegateDriverInboxApprovalRequestKt
import com.acme.driverapp.v1.DriverHomeApprovalServiceGrpcKt
import com.acme.driverapp.v1.DriverHomeComment
import com.acme.driverapp.v1.DriverHomeGatewayServiceGrpcKt
import com.acme.driverapp.v1.DriverHomeIntegrationServiceGrpcKt
import com.acme.driverapp.v1.DriverHomeNotificationServiceGrpcKt
import com.acme.driverapp.v1.DriverHomeQueryServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxApprovalServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxServiceGrpcKt
import com.acme.driverapp.v1.ExportDriverHomesRequestKt
import com.acme.driverapp.v1.FetchDriverHomeViewRequest
import com.acme.driverapp.v1.FetchDriverHomeViewResponse
import com.acme.driverapp.v1.GetDriverHomeSummaryRequest
import com.acme.driverapp.v1.GetDriverHomeSummaryResponse
import com.acme.driverapp.v1.ResolveDriverHomesRequest
import com.acme.driverapp.v1.ResolveDriverHomesResponse
import com.acme.driverapp.v1.StreamDriverHomeUpdatesRequest
import com.acme.driverapp.v1.StreamDriverHomeUpdatesResponse
import com.acme.driverapp.v1.SubmitDriverHomeFormRequest
import com.acme.driverapp.v1.SubmitDriverHomeFormResponse
import com.acme.driverapp.v1.SummarizeDriverHomesRequestKt
import com.acme.driverapp.v1.TrackDriverHomeRequest
import com.acme.driverapp.v1.TrackDriverHomeResponse
import com.acme.driverapp.v1.UploadDriverHomeAttachmentsRequest
import com.acme.driverapp.v1.UploadDriverHomeAttachmentsResponse
import com.acme.driverapp.v1.composeDriverHomePageResponse
import com.acme.driverapp.v1.connectDriverHomeSourceRequest
import com.acme.driverapp.v1.copy as driverappCopy
import com.acme.driverapp.v1.createDriverInboxRequest
import com.acme.driverapp.v1.delegateDriverHomeApprovalRequest
import com.acme.driverapp.v1.delegateDriverInboxApprovalRequest
import com.acme.driverapp.v1.driverHome
import com.acme.driverapp.v1.driverHomeComment
import com.acme.driverapp.v1.driverInbox
import com.acme.driverapp.v1.exportDriverHomesRequest
import com.acme.driverapp.v1.fetchDriverHomeViewResponse
import com.acme.driverapp.v1.getDriverHomeSummaryResponse
import com.acme.driverapp.v1.lookupDriverHomeRequest
import com.acme.driverapp.v1.resolveDriverHomesRequest
import com.acme.driverapp.v1.streamDriverHomeNoticesRequest
import com.acme.driverapp.v1.streamDriverHomeUpdatesResponse
import com.acme.driverapp.v1.submitDriverHomeFormResponse
import com.acme.driverapp.v1.summarizeDriverHomesRequest
import com.acme.driverapp.v1.trackDriverHomeResponse
import com.acme.driverapp.v1.uploadDriverHomeAttachmentsResponse
import com.acme.drivers.v1.DriverLicenseApprovalServiceGrpcKt
import com.acme.drivers.v1.approveDriverLicenseRequest
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverLicenseLine
import com.acme.drivers.v1.driverLicenseLineTax
import com.acme.drivers.v1.driverRef
import com.acme.edgegateway.v1.RouteBindingServiceGrpcKt
import com.acme.edgegateway.v1.SearchRouteBindingsRequestKt
import com.acme.edgegateway.v1.SearchRouteBindingsResponse
import com.acme.edgegateway.v1.searchRouteBindingsRequest
import com.acme.featureflags.v1.CheckFeatureFlagEligibilityResponse
import com.acme.featureflags.v1.FeatureFlagValidationServiceGrpcKt
import com.acme.featureflags.v1.checkFeatureFlagEligibilityRequest
import com.acme.featureflags.v1.featureFlag
import com.acme.featureflags.v1.featureFlagBlockAction
import com.acme.identity.v1.userRef
import com.acme.metrics.v1.MetricSeriesValidationServiceGrpcKt
import com.acme.metrics.v1.PreviewMetricSeriesRequest
import com.acme.metrics.v1.PreviewMetricSeriesRequestKt
import com.acme.metrics.v1.metricSeries
import com.acme.metrics.v1.previewMetricSeriesRequest
import com.acme.patientapp.v1.FindPatientHomesRequest
import com.acme.patientapp.v1.FindPatientHomesRequestKt
import com.acme.patientapp.v1.FindPatientHomesResponse
import com.acme.patientapp.v1.PatientHomeQueryServiceGrpcKt
import com.acme.patientapp.v1.findPatientHomesRequest
import com.acme.transport.driverapp.DriverappRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** The edge API that apps call for driver homes. */
class DriverHomeGatewayGrpcService(
    private val store: DriverappRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventValidationService: AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub,
    private val customersConsentRecordApprovalService: ConsentRecordApprovalServiceGrpcKt.ConsentRecordApprovalServiceCoroutineStub,
    private val customersCustomerSegmentApprovalService: CustomerSegmentApprovalServiceGrpcKt.CustomerSegmentApprovalServiceCoroutineStub,
    private val dispatchZoneApprovalService: ZoneApprovalServiceGrpcKt.ZoneApprovalServiceCoroutineStub,
    private val driverappDriverHomeApprovalService: DriverHomeApprovalServiceGrpcKt.DriverHomeApprovalServiceCoroutineStub,
    private val driverappDriverHomeIntegrationService: DriverHomeIntegrationServiceGrpcKt.DriverHomeIntegrationServiceCoroutineStub,
    private val driverappDriverHomeNotificationService: DriverHomeNotificationServiceGrpcKt.DriverHomeNotificationServiceCoroutineStub,
    private val driverappDriverHomeQueryService: DriverHomeQueryServiceGrpcKt.DriverHomeQueryServiceCoroutineStub,
    private val driverappDriverInboxApprovalService: DriverInboxApprovalServiceGrpcKt.DriverInboxApprovalServiceCoroutineStub,
    private val driverappDriverInboxService: DriverInboxServiceGrpcKt.DriverInboxServiceCoroutineStub,
    private val driversDriverLicenseApprovalService: DriverLicenseApprovalServiceGrpcKt.DriverLicenseApprovalServiceCoroutineStub,
    private val edgegatewayRouteBindingService: RouteBindingServiceGrpcKt.RouteBindingServiceCoroutineStub,
    private val featureflagsFeatureFlagValidationService: FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub,
    private val metricsMetricSeriesValidationService: MetricSeriesValidationServiceGrpcKt.MetricSeriesValidationServiceCoroutineStub,
    private val patientappPatientHomeQueryService: PatientHomeQueryServiceGrpcKt.PatientHomeQueryServiceCoroutineStub,
) : DriverHomeGatewayServiceGrpcKt.DriverHomeGatewayServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DriverHomeGatewayGrpcService::class.java)

    private val invokeComposeDriverHomePage0: suspend (FindPatientHomesRequest) -> FindPatientHomesResponse = { payload ->
        patientappPatientHomeQueryService.findPatientHomes(payload)
    }

    private val workerForStreamDriverHomeUpdates0 = ReplyMailbox<ApproveCustomerSegmentRequest, ApproveCustomerSegmentResponse>(workers) { payload ->
        withTimeout(5_000) { customersCustomerSegmentApprovalService.approveCustomerSegment(payload) }
    }

    private val clientForStreamDriverHomeUpdates1 by lazy { driversDriverLicenseApprovalService }

    /** Composes a driver home. */
    override suspend fun composeDriverHomePage(request: ComposeDriverHomePageRequest): ComposeDriverHomePageResponse {
        return handleComposeDriverHomePage(request)
    }

    /** Fetchs a driver home. */
    override suspend fun fetchDriverHomeView(request: FetchDriverHomeViewRequest): FetchDriverHomeViewResponse = handleFetchDriverHomeView(request)

    /** Returns a driver home. */
    override suspend fun getDriverHomeSummary(request: GetDriverHomeSummaryRequest): GetDriverHomeSummaryResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeGatewayService/GetDriverHomeSummary") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetDriverHomeSummary", request)
            log.atInfo().addKeyValue("operation", "GetDriverHomeSummary").log("Handling transport request")
            val outbound0 = createDriverInboxRequest {
                this.context = request.context
                this.driverInbox = driverInbox {
                    this.counterparty = customerRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = driverappDriverInboxService.createDriverInbox(WireCopies.reflective(outbound0))
            getDriverHomeSummaryResponse {
                this.driverHome = driverHome {
                    this.assignee = userRef {
                        this.displayName = reply0.driverInbox.counterparty.displayName
                    }
                    this.comments.addAll(listOf(driverHomeComment {
                        this.createdAt = reply0.driverInbox.audit.createdAt
                    }))
                    this.driverHomeId = request.driverHomeId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Streams the changes of driver homes as they happen. */
    override fun streamDriverHomeUpdates(request: StreamDriverHomeUpdatesRequest): Flow<StreamDriverHomeUpdatesResponse> = flow {
        emit(handleStreamDriverHomeUpdates(request))
    }

    /** Submits a driver home. */
    override suspend fun submitDriverHomeForm(request: SubmitDriverHomeFormRequest): SubmitDriverHomeFormResponse = handleSubmitDriverHomeForm(request)

    /** Tracks a driver home. */
    override suspend fun trackDriverHome(request: TrackDriverHomeRequest): TrackDriverHomeResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeGatewayService/TrackDriverHome") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "TrackDriverHome", request)
            log.atInfo().addKeyValue("operation", "TrackDriverHome").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverHome.driverHomeId).log("Recorded audit detail")
            var reply0 = SearchRouteBindingsResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.driverHome.commentsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound0 = searchRouteBindingsRequest {
                                this.context = request.context
                                this.filter = SearchRouteBindingsRequestKt.filter {
                                    this.createdAfter = item.createdAt
                                    this.ownerId = item.commentId
                                }
                            }
                            edgegatewayRouteBindingService.searchRouteBindings(outbound0)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply0 = it }
                }
            }
            val outbound1 = delegateDriverHomeApprovalRequest {}.driverappCopy {
                this.context = request.context
                this.driverHome = driverHome {
                    this.customer = customerRef {
                        this.email = request.driverHome.requester.email
                    }
                }
            }
            val reply1 = driverappDriverHomeApprovalService.delegateDriverHomeApproval(outbound1)
            val outbound2 = request.toPreviewMetricSeriesFromTrackDriverHome2(reply1)
            val reply2 = metricsMetricSeriesValidationService.previewMetricSeries(outbound2)
            request.driverHome.commentsList.asFlow().map { item ->
                val outbound3 = streamDriverHomeNoticesRequest {
                    this.context = request.context
                    this.since = item.createdAt
                    this.subscriber = contactInfo {
                        this.email = item.authorEmail
                        this.preferredChannel = reply1.driverHome.requester.preferredChannel
                    }
                }
                driverappDriverHomeNotificationService.streamDriverHomeNotices(outbound3).collect { event ->
                    log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
                }
            }.collect()
            trackDriverHomeResponse {
                this.driverHome = driverHome {
                    this.audit = auditInfo {
                        this.updatedBy = reply1.driverHome.audit.updatedBy
                    }
                    this.comments.addAll(listOf(driverHomeComment {
                        this.createdAt = reply2.metricSeries.audit.createdAt
                    }))
                    this.driverHomeId = request.driverHomeId
                    this.priority = request.driverHome.priority
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    /** Takes a stream of driver homes and answers once at the end. */
    override suspend fun uploadDriverHomeAttachments(requests: Flow<UploadDriverHomeAttachmentsRequest>): UploadDriverHomeAttachmentsResponse {
        var answer = UploadDriverHomeAttachmentsResponse.getDefaultInstance()
        requests.map { request -> handleUploadDriverHomeAttachments(request) }.collect { answer = it }
        return answer
    }

    private suspend fun handleComposeDriverHomePage(request: ComposeDriverHomePageRequest): ComposeDriverHomePageResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeGatewayService/ComposeDriverHomePage") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ComposeDriverHomePage", request)
            log.atInfo().addKeyValue("operation", "ComposeDriverHomePage").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverHome.driverHomeId).log("Recorded audit detail")
            val outbound0 = findPatientHomesRequest {
                this.context = request.context
                this.filter = FindPatientHomesRequestKt.filter {
                    this.createdAfter = request.driverHome.sla.dueAt
                    this.ownerId = request.driverHome.assignee.userId
                }
            }
            val reply0 = invokeComposeDriverHomePage0(outbound0)
            val outbound1 = resolveDriverHomesRequest {
                this.context = request.context
            }
            val invoke1: suspend (ResolveDriverHomesRequest, Metadata) -> ResolveDriverHomesResponse = driverappDriverHomeQueryService::resolveDriverHomes
            val reply1 = invoke1(outbound1, Metadata())
            composeDriverHomePageResponse {
                this.driverHome = driverHome {
                    this.driverHomeId = request.driverHomeId
                    this.priority = request.driverHome.priority
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.details.putAll(reply1.result.detailsMap)
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleFetchDriverHomeView(request: FetchDriverHomeViewRequest): FetchDriverHomeViewResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeGatewayService/FetchDriverHomeView") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "FetchDriverHomeView", request)
            log.atInfo().addKeyValue("operation", "FetchDriverHomeView").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverHome.subject).log("Recorded audit detail")
            val outbound0 = DelegateDriverInboxApprovalRequest.newBuilder().apply {
                setContext(request.context)
                setDetails(DelegateDriverInboxApprovalRequestKt.details {
                    this.approver = userRef {
                        this.email = request.driverHome.customer.email
                        this.userId = request.driverHome.assignee.userId
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> driverappDriverInboxApprovalService.delegateDriverInboxApproval(value) }
            val outbound1 = RequestFactory.create {
                lookupDriverHomeRequest {
                    this.context = request.context
                    this.driverHomeId = request.driverHome.driverHomeId
                }
            }
            val reply1 = driverappDriverHomeQueryService.lookupDriverHome(outbound1)
            val outbound2 = approveZoneRequest {
                this.context = request.context
                this.zone = zone {
                    this.children.addAll(listOf(zone {
                        this.children.addAll(listOf(zone {
                            this.audit = auditInfo {
                                this.createdAt = reply1.driverHome.audit.createdAt
                            }
                            this.manager = userRef {
                                this.displayName = reply1.driverHome.assignee.displayName
                                this.email = request.driverHome.customer.email
                                this.userId = request.driverHome.assignee.userId
                            }
                        }))
                    }))
                }
            }
            val reply2 = ClientRegistry.lookup("acme.dispatch.v1.ZoneApprovalService", ZoneApprovalServiceGrpcKt.ZoneApprovalServiceCoroutineStub::class.java).approveZone(outbound2)
            fetchDriverHomeViewResponse {
                this.driverHome = driverHome {
                    this.assignee = request.driverHome.assignee
                    this.customFields.putAll(reply1.driverHome.customFieldsMap)
                    this.driverHomeId = request.driverHomeId
                    this.requester = contactInfo {
                        this.email = reply2.zone.manager.email
                    }
                }
                this.result = operationResult {
                    this.message = reply2.outcome.message
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleStreamDriverHomeUpdates(request: StreamDriverHomeUpdatesRequest): StreamDriverHomeUpdatesResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeGatewayService/StreamDriverHomeUpdates") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StreamDriverHomeUpdates", request)
            log.atInfo().addKeyValue("operation", "StreamDriverHomeUpdates").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = approveCustomerSegmentRequest {
                this.context = request.context
                this.details = ApproveCustomerSegmentRequestKt.details {
                    this.approver = userRef {
                        this.email = request.subscriber.email
                    }
                }
            }
            val reply0 = workerForStreamDriverHomeUpdates0.submit(outbound0)
            val outbound1 = approveDriverLicenseRequest {
                this.context = request.context
                this.driverLicense = driverLicense {
                    this.counterparty = driverRef {
                        this.displayName = reply0.customerSegment.owner.displayName
                    }
                    this.lines.addAll(listOf(driverLicenseLine {
                        this.description = reply0.customerSegment.description
                        this.tax = driverLicenseLineTax {
                            this.amount = reply0.customerSegment.discount.amount
                        }
                    }))
                }
            }
            val reply1 = clientForStreamDriverHomeUpdates1.approveDriverLicense(outbound1)
            val outbound2 = exportDriverHomesRequest {
                this.context = request.context
                this.filter = ExportDriverHomesRequestKt.filter {
                    this.createdAfter = request.filter.createdAfter
                    this.labels.putAll(request.filter.labelsMap)
                    this.region = reply1.driverLicense.billingAddress.region
                }
            }
            driverappDriverHomeQueryService.exportDriverHomes(outbound2).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            streamDriverHomeUpdatesResponse {
                this.driverHome = driverHome {
                    this.audit = auditInfo {
                        this.createdBy = reply1.driverLicense.audit.createdBy
                    }
                    this.priority = reply0.customerSegment.priority
                    this.requester = contactInfo {
                        this.email = request.subscriber.email
                        this.preferredChannel = request.subscriber.preferredChannel
                    }
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSubmitDriverHomeForm(request: SubmitDriverHomeFormRequest): SubmitDriverHomeFormResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeGatewayService/SubmitDriverHomeForm") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SubmitDriverHomeForm", request)
            log.atInfo().addKeyValue("operation", "SubmitDriverHomeForm").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverHome.subject).log("Recorded audit detail")
            val outbound0 = lintAuditEventsRequest {
                this.context = request.context
                this.details = LintAuditEventsRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.driverHome.requester.email
                    }
                }
            }
            val reply0 = withTimeout(5_000) { auditAuditEventValidationService.lintAuditEvents(outbound0) }
            val configure1: ConnectDriverHomeSourceRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.driverHomeId = request.driverHome.driverHomeId
            }
            val outbound1 = connectDriverHomeSourceRequest(configure1)
            val reply1 = driverappDriverHomeIntegrationService.connectDriverHomeSource(outbound1)
            val outbound2 = summarizeDriverHomesRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.details = SummarizeDriverHomesRequestKt.details {
                    this.filters.putAll(request.driverHome.customFieldsMap)
                }
                this.driverHomeId = request.driverHome.driverHomeId
                this.reason = request.reason
            }
            val reply2 = driverappDriverHomeQueryService.summarizeDriverHomes(outbound2)
            submitDriverHomeFormResponse {
                this.driverHome = driverHome {
                    this.customFields.putAll(reply1.driverHome.customFieldsMap)
                    this.driverHomeId = request.driverHomeId
                    this.status = request.driverHome.status
                }
                this.result = operationResult {
                    this.correlationId = reply0.auditEvent.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = reply2.result.status
                }
            }
        }

    private fun TrackDriverHomeRequest.toPreviewMetricSeriesFromTrackDriverHome2(reply1: DelegateDriverHomeApprovalResponse): PreviewMetricSeriesRequest {
        val source = this
        return previewMetricSeriesRequest {
            this.context = source.context
            this.details = PreviewMetricSeriesRequestKt.details {
                this.contact = contactInfo {
                    this.email = reply1.driverHome.requester.email
                    this.name = source.driverHome.requester.name
                }
            }
            this.metricSeries = metricSeries {
                this.owner = userRef {
                    this.email = reply1.driverHome.assignee.email
                }
            }
        }
    }

    private suspend fun handleUploadDriverHomeAttachments(request: UploadDriverHomeAttachmentsRequest): UploadDriverHomeAttachmentsResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeGatewayService/UploadDriverHomeAttachments") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "UploadDriverHomeAttachments", request)
            log.atInfo().addKeyValue("operation", "UploadDriverHomeAttachments").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.header.source).log("Recorded audit detail")
            var reply0 = CheckFeatureFlagEligibilityResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<DriverHomeComment>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound0 = checkFeatureFlagEligibilityRequest {
                            this.context = request.context
                            this.featureFlag = featureFlag {
                                this.audit = auditInfo {
                                    this.createdAt = item.createdAt
                                }
                                this.block = featureFlagBlockAction {
                                    this.notifyEmail = item.authorEmail
                                }
                            }
                        }
                        reply0 = featureflagsFeatureFlagValidationService.checkFeatureFlagEligibility(outbound0)
                    }
                }
                try {
                    request.record.commentsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound1 = listPendingConsentRecordsRequest {
                this.context = request.context
                this.filter = ListPendingConsentRecordsRequestKt.filter {
                    this.createdAfter = reply0.featureFlag.audit.updatedAt
                    this.labels.putAll(request.record.customFieldsMap)
                    this.ownerId = request.record.customer.customerId
                }
            }
            val reply1 = customersConsentRecordApprovalService.listPendingConsentRecords(WireCopies.json(outbound1))
            uploadDriverHomeAttachmentsResponse {
                this.result = operationResult {
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }
}
