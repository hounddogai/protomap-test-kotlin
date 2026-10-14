package com.acme.transport.drivers.api

import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.documents.v1.DocumentServiceGrpcKt
import com.acme.documents.v1.ListDocumentsRequest
import com.acme.documents.v1.ListDocumentsRequestKt
import com.acme.documents.v1.listDocumentsRequest
import com.acme.drivers.v1.AdvanceDriverWorkflowRequestKt
import com.acme.drivers.v1.AdvanceDutyStatusWorkflowRequest
import com.acme.drivers.v1.AdvanceDutyStatusWorkflowResponse
import com.acme.drivers.v1.AdvanceDutyStatusWorkflowResponseKt
import com.acme.drivers.v1.CommentDriverLicenseApprovalRequest
import com.acme.drivers.v1.CommentDriverLicenseApprovalRequestKt
import com.acme.drivers.v1.DriverLicenseApprovalServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseLifecycleServiceGrpcKt
import com.acme.drivers.v1.DriverServiceGrpcKt
import com.acme.drivers.v1.DriverWorkflowServiceGrpcKt
import com.acme.drivers.v1.DutyStatusCalculationServiceGrpcKt
import com.acme.drivers.v1.DutyStatusReportingServiceGrpcKt
import com.acme.drivers.v1.DutyStatusWorkflowServiceGrpcKt
import com.acme.drivers.v1.ExplainDutyStatusCalculationRequestKt
import com.acme.drivers.v1.InspectDutyStatusWorkflowRequest
import com.acme.drivers.v1.InspectDutyStatusWorkflowResponse
import com.acme.drivers.v1.ListDutyStatusWorkflowsRequest
import com.acme.drivers.v1.ListDutyStatusWorkflowsResponse
import com.acme.drivers.v1.PauseDutyStatusWorkflowRequest
import com.acme.drivers.v1.PauseDutyStatusWorkflowResponse
import com.acme.drivers.v1.PauseDutyStatusWorkflowResponseKt
import com.acme.drivers.v1.RenewDriverLicenseRequest
import com.acme.drivers.v1.RenewDriverLicenseRequestKt
import com.acme.drivers.v1.RetryDutyStatusWorkflowRequest
import com.acme.drivers.v1.RetryDutyStatusWorkflowResponse
import com.acme.drivers.v1.RetryDutyStatusWorkflowResponseKt
import com.acme.drivers.v1.StartDutyStatusWorkflowRequest
import com.acme.drivers.v1.StartDutyStatusWorkflowResponse
import com.acme.drivers.v1.StartDutyStatusWorkflowResponseKt
import com.acme.drivers.v1.advanceDriverWorkflowRequest
import com.acme.drivers.v1.advanceDutyStatusWorkflowResponse
import com.acme.drivers.v1.cloneDriverRequest
import com.acme.drivers.v1.commentDriverLicenseApprovalRequest
import com.acme.drivers.v1.copy as driversCopy
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.driverLicenseRef
import com.acme.drivers.v1.driverNationalId
import com.acme.drivers.v1.dutyStatus
import com.acme.drivers.v1.explainDutyStatusCalculationRequest
import com.acme.drivers.v1.getDriverLicenseLifecycleRequest
import com.acme.drivers.v1.inspectDutyStatusWorkflowResponse
import com.acme.drivers.v1.listDutyStatusWorkflowsResponse
import com.acme.drivers.v1.pauseDutyStatusWorkflowResponse
import com.acme.drivers.v1.renewDriverLicenseRequest
import com.acme.drivers.v1.retryDutyStatusWorkflowResponse
import com.acme.drivers.v1.startDutyStatusWorkflowResponse
import com.acme.drivers.v1.streamDutyStatusMetricsRequest
import com.acme.featureflags.v1.ExportFeatureFlagsRequestKt
import com.acme.featureflags.v1.FeatureFlagQueryServiceGrpcKt
import com.acme.featureflags.v1.exportFeatureFlagsRequest
import com.acme.identity.v1.FindUsersRequestKt
import com.acme.identity.v1.FindUsersResponse
import com.acme.identity.v1.UserQueryServiceGrpcKt
import com.acme.identity.v1.findUsersRequest
import com.acme.identity.v1.userRef
import com.acme.metrics.v1.MetricSeriesQueryServiceGrpcKt
import com.acme.metrics.v1.summarizeMetricSeriesesRequest
import com.acme.search.v1.BrowseSearchIndexesRequestKt
import com.acme.search.v1.FindSearchIndexesRequestKt
import com.acme.search.v1.SearchIndexQueryServiceGrpcKt
import com.acme.search.v1.browseSearchIndexesRequest
import com.acme.search.v1.findSearchIndexesRequest
import com.acme.transport.drivers.DriversRepository
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.ListPendingYardsRequest
import com.acme.yards.v1.ListPendingYardsRequestKt
import com.acme.yards.v1.ListPendingYardsResponse
import com.acme.yards.v1.YardApprovalServiceGrpcKt
import com.acme.yards.v1.listPendingYardsRequest
import com.google.protobuf.Any as ProtoAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Runs the long-lived workflow that takes a duty status from start to finish. */
class DutyStatusWorkflowGrpcService(
    private val store: DriversRepository,
    private val workers: CoroutineScope,
    private val documentsDocumentService: DocumentServiceGrpcKt.DocumentServiceCoroutineStub,
    private val driversDriverLicenseApprovalService: DriverLicenseApprovalServiceGrpcKt.DriverLicenseApprovalServiceCoroutineStub,
    private val driversDriverLicenseLifecycleService: DriverLicenseLifecycleServiceGrpcKt.DriverLicenseLifecycleServiceCoroutineStub,
    private val driversDriverService: DriverServiceGrpcKt.DriverServiceCoroutineStub,
    private val driversDriverWorkflowService: DriverWorkflowServiceGrpcKt.DriverWorkflowServiceCoroutineStub,
    private val driversDutyStatusCalculationService: DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineStub,
    private val driversDutyStatusReportingService: DutyStatusReportingServiceGrpcKt.DutyStatusReportingServiceCoroutineStub,
    private val featureflagsFeatureFlagQueryService: FeatureFlagQueryServiceGrpcKt.FeatureFlagQueryServiceCoroutineStub,
    private val identityUserQueryService: UserQueryServiceGrpcKt.UserQueryServiceCoroutineStub,
    private val metricsMetricSeriesQueryService: MetricSeriesQueryServiceGrpcKt.MetricSeriesQueryServiceCoroutineStub,
    private val searchSearchIndexQueryService: SearchIndexQueryServiceGrpcKt.SearchIndexQueryServiceCoroutineStub,
    private val yardsYardApprovalService: YardApprovalServiceGrpcKt.YardApprovalServiceCoroutineStub,
) : DutyStatusWorkflowServiceGrpcKt.DutyStatusWorkflowServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DutyStatusWorkflowGrpcService::class.java)

    private val clientForAdvanceDutyStatusWorkflow0 by lazy { driversDriverService }

    /** Advances a duty status. */
    override suspend fun advanceDutyStatusWorkflow(request: AdvanceDutyStatusWorkflowRequest): AdvanceDutyStatusWorkflowResponse {
        return handleAdvanceDutyStatusWorkflow(request)
    }

    /** Returns a duty status. */
    override suspend fun inspectDutyStatusWorkflow(request: InspectDutyStatusWorkflowRequest): InspectDutyStatusWorkflowResponse = handleInspectDutyStatusWorkflow(request)

    /** Lists duty statuses a page at a time. */
    override suspend fun listDutyStatusWorkflows(request: ListDutyStatusWorkflowsRequest): ListDutyStatusWorkflowsResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusWorkflowService/ListDutyStatusWorkflows") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListDutyStatusWorkflows", request)
            log.atInfo().addKeyValue("operation", "ListDutyStatusWorkflows").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = exportFeatureFlagsRequest {
                this.context = request.context
                this.filter = ExportFeatureFlagsRequestKt.filter {
                    this.createdAfter = request.filter.createdAfter
                    this.ownerId = request.filter.ownerId
                    this.query = request.filter.query
                }
            }
            featureflagsFeatureFlagQueryService.exportFeatureFlags(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = explainDutyStatusCalculationRequest {
                this.context = request.context
                this.details = ExplainDutyStatusCalculationRequestKt.details {
                    this.locale = request.context.locale
                }
            }
            val reply1 = driversDutyStatusCalculationService.explainDutyStatusCalculation(outbound1)
            listDutyStatusWorkflowsResponse {
                this.dutyStatuses.addAll(listOf(dutyStatus {
                    this.source = reply1.dutyStatus.source
                }))
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Pauses a duty status. */
    override suspend fun pauseDutyStatusWorkflow(request: PauseDutyStatusWorkflowRequest): PauseDutyStatusWorkflowResponse {
        return handlePauseDutyStatusWorkflow(request)
    }

    /** Retrys a duty status. */
    override suspend fun retryDutyStatusWorkflow(request: RetryDutyStatusWorkflowRequest): RetryDutyStatusWorkflowResponse = handleRetryDutyStatusWorkflow(request)

    /** Starts a duty status. */
    override suspend fun startDutyStatusWorkflow(request: StartDutyStatusWorkflowRequest): StartDutyStatusWorkflowResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusWorkflowService/StartDutyStatusWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StartDutyStatusWorkflow", request)
            log.atInfo().addKeyValue("operation", "StartDutyStatusWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dutyStatus.dutyStatusId).log("Recorded audit detail")
            var reply0 = FindUsersResponse.getDefaultInstance()
            request.dutyStatus.measurementsList.asFlow().map { item ->
                val outbound0 = findUsersRequest {
                    this.context = request.context
                    this.filter = FindUsersRequestKt.filter {
                        this.createdAfter = item.observedAt
                        this.labels.putAll(request.dutyStatus.attributesMap)
                        this.ownerId = request.dutyStatus.correlationId
                    }
                }
                identityUserQueryService.findUsers(outbound0)
            }.collect { reply0 = it }
            val outbound1 = listDocumentsRequest {
                this.context = request.context
                this.filter = ListDocumentsRequestKt.filter {
                    this.createdAfter = request.dutyStatus.occurredAt
                    this.labels.putAll(reply0.result.detailsMap)
                    this.ownerId = request.dutyStatus.subject.driverLicenseId
                }
            }
            val command1: Submission<ListDocumentsRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = documentsDocumentService.listDocuments(payload1)
            val outbound2 = listPendingYardsRequest {
                this.context = request.context
                this.filter = ListPendingYardsRequestKt.filter {
                    this.createdAfter = request.dutyStatus.occurredAt
                    this.labels.putAll(request.dutyStatus.attributesMap)
                    this.ownerId = reply1.result.correlationId
                }
            }
            val adapter2 = OutboundPort<ListPendingYardsRequest, ListPendingYardsResponse> { payload ->
                yardsYardApprovalService.listPendingYards(payload)
            }
            val port2 = DelegatingPort(adapter2)
            val reply2 = port2.execute(outbound2)
            startDutyStatusWorkflowResponse {
                this.dutyStatus = dutyStatus {
                    this.dutyStatusId = request.dutyStatusId
                    this.occurredAt = request.dutyStatus.occurredAt
                }
                this.outcome = StartDutyStatusWorkflowResponseKt.outcome {
                    this.message = reply2.result.message
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply2.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleAdvanceDutyStatusWorkflow(request: AdvanceDutyStatusWorkflowRequest): AdvanceDutyStatusWorkflowResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusWorkflowService/AdvanceDutyStatusWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "AdvanceDutyStatusWorkflow", request)
            log.atInfo().addKeyValue("operation", "AdvanceDutyStatusWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dutyStatus.dutyStatusId).log("Recorded audit detail")
            val outbound0 = cloneDriverRequest {
                this.context = request.context
                this.driver = driver {
                    this.contact = contactInfo {
                        this.phone = request.details.notify.phone
                    }
                    this.nationalId = driverNationalId {
                        this.number = request.dutyStatus.subject.number
                    }
                    this.relationshipManager = userRef {
                        this.displayName = request.dutyStatus.subject.displayName
                    }
                }
            }
            val reply0 = clientForAdvanceDutyStatusWorkflow0.cloneDriver(outbound0)
            advanceDutyStatusWorkflowResponse {
                this.dutyStatus = dutyStatus {
                    this.dutyStatusId = request.dutyStatusId
                    this.source = request.dutyStatus.source
                }
                this.outcome = AdvanceDutyStatusWorkflowResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleInspectDutyStatusWorkflow(request: InspectDutyStatusWorkflowRequest): InspectDutyStatusWorkflowResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusWorkflowService/InspectDutyStatusWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "InspectDutyStatusWorkflow", request)
            log.atInfo().addKeyValue("operation", "InspectDutyStatusWorkflow").log("Handling transport request")
            val outbound0 = browseSearchIndexesRequest {
                this.context = request.context
                this.filter = BrowseSearchIndexesRequestKt.filter {
                    this.labels.putAll(request.context.baggageMap)
                    this.ownerId = request.dutyStatusId
                }
            }
            val reply0 = flowOf(outbound0).map { value -> searchSearchIndexQueryService.browseSearchIndexes(value) }.single()
            val outbound1 = findSearchIndexesRequest {
                this.context = request.context
                this.filter = FindSearchIndexesRequestKt.filter {
                    this.ownerId = request.dutyStatusId
                }
            }
            val reply1 = withTimeout(5_000) { searchSearchIndexQueryService.findSearchIndexes(outbound1) }
            inspectDutyStatusWorkflowResponse {
                this.dutyStatus = dutyStatus {
                    this.dutyStatusId = request.dutyStatusId
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = reply1.result.status
                }
            }
        }

    private suspend fun handlePauseDutyStatusWorkflow(request: PauseDutyStatusWorkflowRequest): PauseDutyStatusWorkflowResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusWorkflowService/PauseDutyStatusWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PauseDutyStatusWorkflow", request)
            log.atInfo().addKeyValue("operation", "PauseDutyStatusWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dutyStatus.dutyStatusId).log("Recorded audit detail")
            val outbound0 = streamDutyStatusMetricsRequest {
                this.context = request.context
                this.subscriber = contactInfo {
                    this.email = request.details.notify.email
                    this.name = request.details.notify.name
                    this.phone = request.details.notify.phone
                    this.preferredChannel = request.details.notify.preferredChannel
                }
            }
            driversDutyStatusReportingService.streamDutyStatusMetrics(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = advanceDriverWorkflowRequest {}.driversCopy {
                this.context = request.context
                this.details = AdvanceDriverWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.email = request.details.notify.email
                    }
                }
                this.driver = driver {
                    this.contact = contactInfo {
                        this.name = request.details.notify.name
                    }
                    this.nationalId = driverNationalId {
                        this.number = request.dutyStatus.subject.number
                    }
                }
            }
            val reply1 = driversDriverWorkflowService.advanceDriverWorkflow(outbound1)
            pauseDutyStatusWorkflowResponse {
                this.dutyStatus = dutyStatus {
                    this.dutyStatusId = request.dutyStatusId
                    this.sequence = request.dutyStatus.sequence
                    this.subject = driverLicenseRef {
                        this.number = reply1.driver.nationalId.number
                    }
                }
                this.outcome = PauseDutyStatusWorkflowResponseKt.outcome {
                    this.effectiveAt = reply1.outcome.effectiveAt
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private fun RetryDutyStatusWorkflowRequest.toCommentDriverLicenseApprovalFromRetryDutyStatusWorkflow0(): CommentDriverLicenseApprovalRequest {
        val source = this
        return commentDriverLicenseApprovalRequest {
            this.context = source.context
            this.details = CommentDriverLicenseApprovalRequestKt.details {
                this.approver = userRef {
                    this.displayName = source.dutyStatus.subject.displayName
                }
            }
        }
    }

    private suspend fun handleRetryDutyStatusWorkflow(request: RetryDutyStatusWorkflowRequest): RetryDutyStatusWorkflowResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusWorkflowService/RetryDutyStatusWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RetryDutyStatusWorkflow", request)
            log.atInfo().addKeyValue("operation", "RetryDutyStatusWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dutyStatus.dutyStatusId).log("Recorded audit detail")
            val outbound0 = request.toCommentDriverLicenseApprovalFromRetryDutyStatusWorkflow0()
            val reply0 = driversDriverLicenseApprovalService.commentDriverLicenseApproval(outbound0)
            val outbound1 = summarizeMetricSeriesesRequest {
                this.context = request.context
                this.reason = request.reason
            }
            val reply1 = metricsMetricSeriesQueryService.summarizeMetricSerieses(outbound1)
            val outbound2 = renewDriverLicenseRequest {
                this.context = request.context
                this.details = RenewDriverLicenseRequestKt.details {
                    this.newOwner = userRef {
                        this.displayName = request.dutyStatus.subject.displayName
                    }
                    this.notifyContact = contactInfo {
                        this.email = request.details.notify.email
                        this.name = request.details.notify.name
                        this.phone = request.details.notify.phone
                    }
                }
                this.driverLicenseId = request.dutyStatus.subject.driverLicenseId
            }
            val envelope2 = ProtoAny.pack(outbound2)
            val reply2 = driversDriverLicenseLifecycleService.renewDriverLicense(envelope2.unpack(RenewDriverLicenseRequest::class.java))
            val outbound3 = getDriverLicenseLifecycleRequest {
                this.context = request.context
                this.driverLicenseId = request.dutyStatus.subject.driverLicenseId
            }
            val reply3 = driversDriverLicenseLifecycleService.getDriverLicenseLifecycle(WireCopies.json(outbound3))
            retryDutyStatusWorkflowResponse {
                this.dutyStatus = dutyStatus {
                    this.attributes.putAll(request.dutyStatus.attributesMap)
                    this.dutyStatusId = request.dutyStatusId
                    this.subject = driverLicenseRef {
                        this.displayName = reply0.driverLicense.counterparty.displayName
                        this.number = reply2.driverLicense.number
                    }
                }
                this.outcome = RetryDutyStatusWorkflowResponseKt.outcome {
                    this.message = reply3.result.message
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }
}
