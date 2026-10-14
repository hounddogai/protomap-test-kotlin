package com.acme.transport.driverapp.adapters

import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.ConsentRecordApprovalServiceGrpcKt
import com.acme.customers.v1.EscalateConsentRecordRequestKt
import com.acme.customers.v1.SubmitConsentRecordRequest
import com.acme.customers.v1.SubmitConsentRecordRequestKt
import com.acme.customers.v1.consentRecord
import com.acme.customers.v1.consentRecordMeasurement
import com.acme.customers.v1.escalateConsentRecordRequest
import com.acme.customers.v1.submitConsentRecordRequest
import com.acme.datasets.v1.DatasetQueryServiceGrpcKt
import com.acme.datasets.v1.SummarizeDatasetsRequestKt
import com.acme.datasets.v1.summarizeDatasetsRequest
import com.acme.driverapp.v1.BrowseDriverInboxesRequest
import com.acme.driverapp.v1.BrowseDriverInboxesResponse
import com.acme.driverapp.v1.ConnectDriverInboxSourceRequest
import com.acme.driverapp.v1.ConnectDriverInboxSourceRequestKt
import com.acme.driverapp.v1.ConnectDriverInboxSourceResponse
import com.acme.driverapp.v1.CountDriverInboxesRequest
import com.acme.driverapp.v1.CountDriverInboxesResponse
import com.acme.driverapp.v1.DriverInboxIntegrationServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxQueryServiceGrpcKt
import com.acme.driverapp.v1.FindDriverInboxesRequest
import com.acme.driverapp.v1.FindDriverInboxesResponse
import com.acme.driverapp.v1.LookupDriverInboxRequest
import com.acme.driverapp.v1.LookupDriverInboxResponse
import com.acme.driverapp.v1.PeekDriverInboxRequest
import com.acme.driverapp.v1.PeekDriverInboxResponse
import com.acme.driverapp.v1.ResolveDriverInboxesRequest
import com.acme.driverapp.v1.ResolveDriverInboxesResponse
import com.acme.driverapp.v1.SuggestDriverInboxesRequest
import com.acme.driverapp.v1.SuggestDriverInboxesResponse
import com.acme.driverapp.v1.SummarizeDriverInboxesRequest
import com.acme.driverapp.v1.SummarizeDriverInboxesResponse
import com.acme.driverapp.v1.browseDriverInboxesResponse
import com.acme.driverapp.v1.connectDriverInboxSourceRequest
import com.acme.driverapp.v1.countDriverInboxesResponse
import com.acme.driverapp.v1.driverInbox
import com.acme.driverapp.v1.findDriverInboxesResponse
import com.acme.driverapp.v1.lookupDriverInboxResponse
import com.acme.driverapp.v1.peekDriverInboxResponse
import com.acme.driverapp.v1.resolveDriverInboxesResponse
import com.acme.driverapp.v1.suggestDriverInboxesResponse
import com.acme.driverapp.v1.summarizeDriverInboxesResponse
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.WorkOrderApprovalServiceGrpcKt
import com.acme.maintenance.v1.approveWorkOrderRequest
import com.acme.maintenance.v1.escalateWorkOrderRequest
import com.acme.maintenance.v1.workOrder
import com.acme.maintenance.v1.workOrderComment
import com.acme.routing.v1.RoutePlanLifecycleServiceGrpcKt
import com.acme.routing.v1.RouteWorkflowServiceGrpcKt
import com.acme.routing.v1.ScheduleRoutePlanExpiryRequest
import com.acme.routing.v1.ScheduleRoutePlanExpiryResponse
import com.acme.routing.v1.route
import com.acme.routing.v1.routeLine
import com.acme.routing.v1.scheduleRoutePlanExpiryRequest
import com.acme.routing.v1.startRouteWorkflowRequest
import com.acme.search.v1.ExplainSearchIndexDecisionRequest
import com.acme.search.v1.ExplainSearchIndexDecisionRequestKt
import com.acme.search.v1.ExplainSearchIndexDecisionResponse
import com.acme.search.v1.SearchIndexValidationServiceGrpcKt
import com.acme.search.v1.explainSearchIndexDecisionRequest
import com.acme.transport.driverapp.DriverappRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Answers read-only questions about driver inboxes, for screens and reports. */
class DriverInboxQueryController(
    private val store: DriverappRepository,
    private val workers: CoroutineScope,
    private val customersConsentRecordApprovalService: ConsentRecordApprovalServiceGrpcKt.ConsentRecordApprovalServiceCoroutineStub,
    private val datasetsDatasetQueryService: DatasetQueryServiceGrpcKt.DatasetQueryServiceCoroutineStub,
    private val driverappDriverInboxIntegrationService: DriverInboxIntegrationServiceGrpcKt.DriverInboxIntegrationServiceCoroutineStub,
    private val maintenanceWorkOrderApprovalService: WorkOrderApprovalServiceGrpcKt.WorkOrderApprovalServiceCoroutineStub,
    private val routingRoutePlanLifecycleService: RoutePlanLifecycleServiceGrpcKt.RoutePlanLifecycleServiceCoroutineStub,
    private val routingRouteWorkflowService: RouteWorkflowServiceGrpcKt.RouteWorkflowServiceCoroutineStub,
    private val searchSearchIndexValidationService: SearchIndexValidationServiceGrpcKt.SearchIndexValidationServiceCoroutineStub,
) : DriverInboxQueryServiceGrpcKt.DriverInboxQueryServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DriverInboxQueryController::class.java)

    private val invokeBrowseDriverInboxes0: suspend (ConnectDriverInboxSourceRequest) -> ConnectDriverInboxSourceResponse = { payload ->
        driverappDriverInboxIntegrationService.connectDriverInboxSource(payload)
    }

    private val workerForSummarizeDriverInboxes1 = ReplyMailbox<ScheduleRoutePlanExpiryRequest, ScheduleRoutePlanExpiryResponse>(workers) { payload ->
        withTimeout(5_000) { routingRoutePlanLifecycleService.scheduleRoutePlanExpiry(payload) }
    }

    private val clientForSummarizeDriverInboxes2 by lazy { maintenanceWorkOrderApprovalService }

    /** Lists driver inboxes a page at a time. */
    override suspend fun browseDriverInboxes(request: BrowseDriverInboxesRequest): BrowseDriverInboxesResponse = handleBrowseDriverInboxes(request)

    /** Counts driver inboxes by status. */
    override suspend fun countDriverInboxes(request: CountDriverInboxesRequest): CountDriverInboxesResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxQueryService/CountDriverInboxes") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CountDriverInboxes", request)
            log.atInfo().addKeyValue("operation", "CountDriverInboxes").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            countDriverInboxesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Lists driver inboxes a page at a time. */
    override suspend fun findDriverInboxes(request: FindDriverInboxesRequest): FindDriverInboxesResponse {
        return handleFindDriverInboxes(request)
    }

    /** Returns a driver inbox. */
    override suspend fun lookupDriverInbox(request: LookupDriverInboxRequest): LookupDriverInboxResponse = handleLookupDriverInbox(request)

    /** Returns a driver inbox. */
    override suspend fun peekDriverInbox(request: PeekDriverInboxRequest): PeekDriverInboxResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxQueryService/PeekDriverInbox") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PeekDriverInbox", request)
            log.atInfo().addKeyValue("operation", "PeekDriverInbox").log("Handling transport request")
            val outbound0 = SubmitConsentRecordRequest.newBuilder().apply {
                setContext(request.context)
                setDetails(SubmitConsentRecordRequestKt.details {
                    this.approver = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> customersConsentRecordApprovalService.submitConsentRecord(value) }
            peekDriverInboxResponse {
                this.driverInbox = driverInbox {
                    this.driverInboxId = request.driverInboxId
                }
                this.result = operationResult {
                    this.status = reply0.outcome.status
                }
            }
        }

    /** Returns several driver inboxes at once. */
    override suspend fun resolveDriverInboxes(request: ResolveDriverInboxesRequest): ResolveDriverInboxesResponse {
        return handleResolveDriverInboxes(request)
    }

    /** Lists driver inboxes a page at a time. */
    override suspend fun suggestDriverInboxes(request: SuggestDriverInboxesRequest): SuggestDriverInboxesResponse = handleSuggestDriverInboxes(request)

    /** Summarizes a driver inbox. */
    override suspend fun summarizeDriverInboxes(request: SummarizeDriverInboxesRequest): SummarizeDriverInboxesResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxQueryService/SummarizeDriverInboxes") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SummarizeDriverInboxes", request)
            log.atInfo().addKeyValue("operation", "SummarizeDriverInboxes").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverInboxId).log("Recorded audit detail")
            val outbound0 = summarizeDatasetsRequest {
                this.context = request.context
                this.details = SummarizeDatasetsRequestKt.details {
                    this.filters.putAll(request.details.filtersMap)
                    this.period = request.details.period
                    this.recipientEmails.addAll(request.details.recipientEmailsList)
                }
            }
            val reply0 = datasetsDatasetQueryService.summarizeDatasets(WireCopies.reflective(outbound0))
            val outbound1 = scheduleRoutePlanExpiryRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.reason = request.reason
            }
            val reply1 = workerForSummarizeDriverInboxes1.submit(outbound1)
            val outbound2 = escalateWorkOrderRequest {
                this.context = request.context
                this.workOrder = workOrder {
                    this.assignee = userRef {
                        this.email = reply1.routePlan.counterparty.email
                    }
                    this.audit = auditInfo {
                        this.createdAt = reply1.routePlan.audit.createdAt
                        this.updatedAt = reply1.routePlan.audit.updatedAt
                        this.updatedBy = reply1.routePlan.audit.updatedBy
                    }
                }
            }
            val reply2 = clientForSummarizeDriverInboxes2.escalateWorkOrder(outbound2)
            val outbound3 = approveWorkOrderRequest {
                this.context = request.context
                this.workOrder = workOrder {
                    this.audit = auditInfo {
                        this.etag = reply1.routePlan.audit.etag
                    }
                    this.comments.addAll(listOf(workOrderComment {
                        this.createdAt = reply1.routePlan.audit.createdAt
                    }))
                }
            }
            val reply3 = flowOf(outbound3).map { value -> maintenanceWorkOrderApprovalService.approveWorkOrder(value) }.single()
            summarizeDriverInboxesResponse {
                this.period = request.details.period
                this.reportId = reply0.reportId
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.message = reply2.result.message
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleBrowseDriverInboxes(request: BrowseDriverInboxesRequest): BrowseDriverInboxesResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxQueryService/BrowseDriverInboxes") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BrowseDriverInboxes", request)
            log.atInfo().addKeyValue("operation", "BrowseDriverInboxes").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = connectDriverInboxSourceRequest {
                this.context = request.context
                this.details = ConnectDriverInboxSourceRequestKt.details {
                    this.receivedAt = request.filter.createdAfter
                }
            }
            val reply0 = invokeBrowseDriverInboxes0(outbound0)
            browseDriverInboxesResponse {
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleFindDriverInboxes(request: FindDriverInboxesRequest): FindDriverInboxesResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxQueryService/FindDriverInboxes") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "FindDriverInboxes", request)
            log.atInfo().addKeyValue("operation", "FindDriverInboxes").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            findDriverInboxesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleLookupDriverInbox(request: LookupDriverInboxRequest): LookupDriverInboxResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxQueryService/LookupDriverInbox") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LookupDriverInbox", request)
            log.atInfo().addKeyValue("operation", "LookupDriverInbox").log("Handling transport request")
            val outbound0 = explainSearchIndexDecisionRequest {
                this.context = request.context
                this.details = ExplainSearchIndexDecisionRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
            }
            val invoke0: suspend (ExplainSearchIndexDecisionRequest, Metadata) -> ExplainSearchIndexDecisionResponse = searchSearchIndexValidationService::explainSearchIndexDecision
            val reply0 = invoke0(outbound0, Metadata())
            lookupDriverInboxResponse {
                this.driverInbox = driverInbox {
                    this.billingAddress = postalAddress {
                        this.lines.addAll(reply0.searchIndex.address.linesList)
                    }
                    this.driverInboxId = request.driverInboxId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleResolveDriverInboxes(request: ResolveDriverInboxesRequest): ResolveDriverInboxesResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxQueryService/ResolveDriverInboxes") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResolveDriverInboxes", request)
            log.atInfo().addKeyValue("operation", "ResolveDriverInboxes").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            resolveDriverInboxesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSuggestDriverInboxes(request: SuggestDriverInboxesRequest): SuggestDriverInboxesResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxQueryService/SuggestDriverInboxes") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SuggestDriverInboxes", request)
            log.atInfo().addKeyValue("operation", "SuggestDriverInboxes").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                escalateConsentRecordRequest {
                    this.consentRecord = consentRecord {
                        this.correlationId = request.filter.ownerId
                        this.measurements.addAll(listOf(consentRecordMeasurement {
                            this.observedAt = request.filter.createdAfter
                        }))
                    }
                    this.context = request.context
                    this.details = EscalateConsentRecordRequestKt.details {
                        this.approver = userRef {
                            this.email = request.context.actor.email
                            this.userId = request.context.actor.userId
                        }
                    }
                }
            }
            val reply0 = customersConsentRecordApprovalService.escalateConsentRecord(outbound0)
            val outbound1 = startRouteWorkflowRequest {
                this.context = request.context
                this.route = route {
                    this.lines.addAll(listOf(routeLine {
                        this.attributes.putAll(reply0.consentRecord.attributesMap)
                    }))
                }
            }
            val reply1 = ClientRegistry.lookup("acme.routing.v1.RouteWorkflowService", RouteWorkflowServiceGrpcKt.RouteWorkflowServiceCoroutineStub::class.java).startRouteWorkflow(outbound1)
            suggestDriverInboxesResponse {
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = reply1.result.status
                }
            }
        }
}
