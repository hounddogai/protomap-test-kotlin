package com.acme.transport.driverapp.api

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.corporateevents.v1.CorporateEventServiceGrpcKt
import com.acme.corporateevents.v1.CreateCorporateEventRequest
import com.acme.corporateevents.v1.corporateEvent
import com.acme.corporateevents.v1.corporateEventParticipant
import com.acme.corporateevents.v1.createCorporateEventRequest
import com.acme.customers.v1.PreferenceSyncServiceGrpcKt
import com.acme.customers.v1.ResolvePreferenceConflictRequest
import com.acme.customers.v1.ResolvePreferenceConflictResponse
import com.acme.customers.v1.preference
import com.acme.customers.v1.resolvePreferenceConflictRequest
import com.acme.driverapp.v1.BrowseDriverHomesRequest
import com.acme.driverapp.v1.BrowseDriverHomesResponse
import com.acme.driverapp.v1.ConnectDriverHomeSourceRequestKt
import com.acme.driverapp.v1.CountDriverHomesRequest
import com.acme.driverapp.v1.CountDriverHomesResponse
import com.acme.driverapp.v1.DisconnectDriverInboxSourceRequest
import com.acme.driverapp.v1.DisconnectDriverInboxSourceRequestKt
import com.acme.driverapp.v1.DriverHomeApprovalServiceGrpcKt
import com.acme.driverapp.v1.DriverHomeIntegrationServiceGrpcKt
import com.acme.driverapp.v1.DriverHomeQueryServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxApprovalServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxIntegrationServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxWorkflowServiceGrpcKt
import com.acme.driverapp.v1.ExportDriverHomesRequest
import com.acme.driverapp.v1.ExportDriverHomesResponse
import com.acme.driverapp.v1.FindDriverHomesRequest
import com.acme.driverapp.v1.FindDriverHomesResponse
import com.acme.driverapp.v1.LookupDriverHomeRequest
import com.acme.driverapp.v1.LookupDriverHomeResponse
import com.acme.driverapp.v1.PeekDriverHomeRequest
import com.acme.driverapp.v1.PeekDriverHomeResponse
import com.acme.driverapp.v1.ResolveDriverHomesRequest
import com.acme.driverapp.v1.ResolveDriverHomesResponse
import com.acme.driverapp.v1.RetryDriverInboxWorkflowRequest
import com.acme.driverapp.v1.RetryDriverInboxWorkflowRequestKt
import com.acme.driverapp.v1.RetryDriverInboxWorkflowResponse
import com.acme.driverapp.v1.SummarizeDriverHomesRequest
import com.acme.driverapp.v1.SummarizeDriverHomesResponse
import com.acme.driverapp.v1.acknowledgeDriverHomeEventRequest
import com.acme.driverapp.v1.approveDriverInboxRequest
import com.acme.driverapp.v1.browseDriverHomesResponse
import com.acme.driverapp.v1.connectDriverHomeSourceRequest
import com.acme.driverapp.v1.countDriverHomesResponse
import com.acme.driverapp.v1.disconnectDriverInboxSourceRequest
import com.acme.driverapp.v1.driverHome
import com.acme.driverapp.v1.driverHomeComment
import com.acme.driverapp.v1.driverInbox
import com.acme.driverapp.v1.exportDriverHomesResponse
import com.acme.driverapp.v1.findDriverHomesResponse
import com.acme.driverapp.v1.lookupDriverHomeResponse
import com.acme.driverapp.v1.peekDriverHomeResponse
import com.acme.driverapp.v1.resolveDriverHomesResponse
import com.acme.driverapp.v1.retryDriverInboxWorkflowRequest
import com.acme.driverapp.v1.submitDriverHomeRequest
import com.acme.driverapp.v1.summarizeDriverHomesResponse
import com.acme.drivers.v1.driverRef
import com.acme.fanportal.v1.FanSessionServiceGrpcKt
import com.acme.fanportal.v1.UpdateFanSessionRequest
import com.acme.fanportal.v1.UpdateFanSessionResponse
import com.acme.fanportal.v1.fanSession
import com.acme.fanportal.v1.updateFanSessionRequest
import com.acme.identity.v1.userRef
import com.acme.routing.v1.CancelRouteWorkflowRequest
import com.acme.routing.v1.RouteWorkflowServiceGrpcKt
import com.acme.routing.v1.cancelRouteWorkflowRequest
import com.acme.routing.v1.route
import com.acme.routing.v1.routeLine
import com.acme.search.v1.SearchIndexQueryServiceGrpcKt
import com.acme.search.v1.SummarizeSearchIndexesRequestKt
import com.acme.search.v1.summarizeSearchIndexesRequest
import com.acme.transport.driverapp.DriverappRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.google.protobuf.Any as ProtoAny
import com.google.protobuf.Timestamp
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.slf4j.LoggerFactory

/** Answers read-only questions about driver homes, for screens and reports. */
class DriverHomeQueryGrpcService(
    private val store: DriverappRepository,
    private val workers: CoroutineScope,
    private val corporateeventsCorporateEventService: CorporateEventServiceGrpcKt.CorporateEventServiceCoroutineStub,
    private val customersPreferenceSyncService: PreferenceSyncServiceGrpcKt.PreferenceSyncServiceCoroutineStub,
    private val driverappDriverHomeApprovalService: DriverHomeApprovalServiceGrpcKt.DriverHomeApprovalServiceCoroutineStub,
    private val driverappDriverHomeIntegrationService: DriverHomeIntegrationServiceGrpcKt.DriverHomeIntegrationServiceCoroutineStub,
    private val driverappDriverInboxApprovalService: DriverInboxApprovalServiceGrpcKt.DriverInboxApprovalServiceCoroutineStub,
    private val driverappDriverInboxIntegrationService: DriverInboxIntegrationServiceGrpcKt.DriverInboxIntegrationServiceCoroutineStub,
    private val driverappDriverInboxWorkflowService: DriverInboxWorkflowServiceGrpcKt.DriverInboxWorkflowServiceCoroutineStub,
    private val fanportalFanSessionService: FanSessionServiceGrpcKt.FanSessionServiceCoroutineStub,
    private val routingRouteWorkflowService: RouteWorkflowServiceGrpcKt.RouteWorkflowServiceCoroutineStub,
    private val searchSearchIndexQueryService: SearchIndexQueryServiceGrpcKt.SearchIndexQueryServiceCoroutineStub,
) : DriverHomeQueryServiceGrpcKt.DriverHomeQueryServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DriverHomeQueryGrpcService::class.java)

    private val invokeFindDriverHomes0: suspend (RetryDriverInboxWorkflowRequest) -> RetryDriverInboxWorkflowResponse = { payload ->
        driverappDriverInboxWorkflowService.retryDriverInboxWorkflow(payload)
    }

    /** Lists driver homes a page at a time. */
    override suspend fun browseDriverHomes(request: BrowseDriverHomesRequest): BrowseDriverHomesResponse {
        return handleBrowseDriverHomes(request)
    }

    /** Counts driver homes by status. */
    override suspend fun countDriverHomes(request: CountDriverHomesRequest): CountDriverHomesResponse = handleCountDriverHomes(request)

    /** Streams every matching driver home. */
    override fun exportDriverHomes(request: ExportDriverHomesRequest): Flow<ExportDriverHomesResponse> = flow {
        emit(handleExportDriverHomes(request))
    }

    /** Lists driver homes a page at a time. */
    override suspend fun findDriverHomes(request: FindDriverHomesRequest): FindDriverHomesResponse {
        return handleFindDriverHomes(request)
    }

    /** Returns a driver home. */
    override suspend fun lookupDriverHome(request: LookupDriverHomeRequest): LookupDriverHomeResponse = handleLookupDriverHome(request)

    /** Returns a driver home. */
    override suspend fun peekDriverHome(request: PeekDriverHomeRequest): PeekDriverHomeResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeQueryService/PeekDriverHome") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PeekDriverHome", request)
            log.atInfo().addKeyValue("operation", "PeekDriverHome").log("Handling transport request")
            val outbound0 = DisconnectDriverInboxSourceRequest.newBuilder().apply {
                setContext(request.context)
                setDetails(DisconnectDriverInboxSourceRequestKt.details {
                    this.receivedAt = request.context.deadline
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> driverappDriverInboxIntegrationService.disconnectDriverInboxSource(value) }
            val outbound1 = RequestFactory.create {
                summarizeSearchIndexesRequest {
                    this.annotations.putAll(reply0.result.detailsMap)
                    this.context = request.context
                    this.details = SummarizeSearchIndexesRequestKt.details {
                        this.filters.putAll(reply0.driverInbox.metadataMap)
                    }
                }
            }
            val reply1 = searchSearchIndexQueryService.summarizeSearchIndexes(outbound1)
            peekDriverHomeResponse {
                this.driverHome = driverHome {
                    this.audit = auditInfo {
                        this.createdAt = reply0.driverInbox.audit.createdAt
                    }
                    this.driverHomeId = request.driverHomeId
                }
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    /** Returns several driver homes at once. */
    override suspend fun resolveDriverHomes(request: ResolveDriverHomesRequest): ResolveDriverHomesResponse {
        return handleResolveDriverHomes(request)
    }

    /** Summarizes a driver home. */
    override suspend fun summarizeDriverHomes(request: SummarizeDriverHomesRequest): SummarizeDriverHomesResponse = handleSummarizeDriverHomes(request)

    private suspend fun handleBrowseDriverHomes(request: BrowseDriverHomesRequest): BrowseDriverHomesResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeQueryService/BrowseDriverHomes") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BrowseDriverHomes", request)
            log.atInfo().addKeyValue("operation", "BrowseDriverHomes").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = submitDriverHomeRequest {
                this.context = request.context
                this.driverHome = driverHome {
                    this.assignee = userRef {
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply0 = driverappDriverHomeApprovalService.submitDriverHome(outbound0)
            val outbound1 = createCorporateEventRequest {
                this.context = request.context
                this.corporateEvent = corporateEvent {
                    this.participants.addAll(listOf(corporateEventParticipant {
                        this.contact = contactInfo {
                            this.name = reply0.driverHome.requester.name
                        }
                    }))
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = corporateeventsCorporateEventService.createCorporateEvent(envelope1.unpack(CreateCorporateEventRequest::class.java))
            browseDriverHomesResponse {
                this.result = operationResult {
                    this.message = reply0.outcome.message
                    this.status = reply1.result.status
                }
            }
        }

    private suspend fun handleCountDriverHomes(request: CountDriverHomesRequest): CountDriverHomesResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeQueryService/CountDriverHomes") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CountDriverHomes", request)
            log.atInfo().addKeyValue("operation", "CountDriverHomes").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = connectDriverHomeSourceRequest {
                this.annotations.putAll(request.filter.labelsMap)
                this.context = request.context
                this.details = ConnectDriverHomeSourceRequestKt.details {
                    this.messageId = request.filter.ownerId
                    this.receivedAt = request.filter.createdAfter
                }
            }
            val reply0 = driverappDriverHomeIntegrationService.connectDriverHomeSource(WireCopies.json(outbound0))
            countDriverHomesResponse {
                this.result = operationResult {
                    this.message = reply0.outcome.message
                    this.status = "ok"
                }
            }
        }

    private data class ExportDriverHomes0Command(
        val context: RequestContext,
        val routeBillingAddressRegion: String,
        val routeCounterpartyEmail: String,
        val routeAuditCreatedAt: Timestamp,
        val routeLinesAttributes: Map<String, String>
    ) {
        fun toRequest(): CancelRouteWorkflowRequest {
            val command = this
            return cancelRouteWorkflowRequest {
                this.context = command.context
                this.route = route {
                    this.audit = auditInfo {
                        this.createdAt = command.routeAuditCreatedAt
                    }
                    this.billingAddress = postalAddress {
                        this.region = command.routeBillingAddressRegion
                    }
                    this.counterparty = driverRef {
                        this.email = command.routeCounterpartyEmail
                    }
                    this.lines.addAll(listOf(routeLine {
                        this.attributes.putAll(command.routeLinesAttributes)
                    }))
                }
            }
        }
    }

    private suspend fun handleExportDriverHomes(request: ExportDriverHomesRequest): ExportDriverHomesResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeQueryService/ExportDriverHomes") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExportDriverHomes", request)
            log.atInfo().addKeyValue("operation", "ExportDriverHomes").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = ExportDriverHomes0Command(
                context = request.context,
                routeBillingAddressRegion = request.filter.region,
                routeCounterpartyEmail = request.context.actor.email,
                routeAuditCreatedAt = request.filter.createdAfter,
                routeLinesAttributes = request.filter.labelsMap
            ).toRequest()
            val reply0 = routingRouteWorkflowService.cancelRouteWorkflow(outbound0)
            val outbound1 = createCorporateEventRequest {
                this.context = request.context
                this.corporateEvent = corporateEvent {
                    this.location = postalAddress {
                        this.locality = reply0.route.billingAddress.locality
                    }
                }
            }
            val command1: Submission<CreateCorporateEventRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = corporateeventsCorporateEventService.createCorporateEvent(payload1)
            val outbound2 = resolvePreferenceConflictRequest {
                this.context = request.context
                this.preference = preference {
                    this.audit = auditInfo {
                        this.etag = reply1.corporateEvent.audit.etag
                    }
                    this.owner = userRef {
                        this.displayName = reply1.corporateEvent.organizer.displayName
                    }
                }
            }
            val adapter2 = OutboundPort<ResolvePreferenceConflictRequest, ResolvePreferenceConflictResponse> { payload ->
                customersPreferenceSyncService.resolvePreferenceConflict(payload)
            }
            val port2 = DelegatingPort(adapter2)
            val reply2 = port2.execute(outbound2)
            exportDriverHomesResponse {
                this.driverHome = driverHome {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.route.audit.updatedBy
                    }
                    this.comments.addAll(listOf(driverHomeComment {
                        this.createdAt = reply1.corporateEvent.audit.createdAt
                    }))
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = reply2.outcome.status
                }
            }
        }

    private suspend fun handleFindDriverHomes(request: FindDriverHomesRequest): FindDriverHomesResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeQueryService/FindDriverHomes") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "FindDriverHomes", request)
            log.atInfo().addKeyValue("operation", "FindDriverHomes").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = retryDriverInboxWorkflowRequest {
                this.context = request.context
                this.details = RetryDriverInboxWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
                this.driverInbox = driverInbox {
                    this.billingAddress = postalAddress {
                        this.region = request.filter.region
                    }
                }
            }
            val reply0 = invokeFindDriverHomes0(outbound0)
            findDriverHomesResponse {
                this.driverHomes.addAll(listOf(driverHome {
                    this.assignee = userRef {
                        this.displayName = reply0.driverInbox.counterparty.displayName
                    }
                }))
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleLookupDriverHome(request: LookupDriverHomeRequest): LookupDriverHomeResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeQueryService/LookupDriverHome") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LookupDriverHome", request)
            log.atInfo().addKeyValue("operation", "LookupDriverHome").log("Handling transport request")
            val outbound0 = updateFanSessionRequest {
                this.context = request.context
                this.fanSession = fanSession {
                    this.assignee = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                    this.audit = auditInfo {
                        this.updatedAt = request.context.deadline
                    }
                    this.subject = request.driverHomeId
                }
            }
            val invoke0: suspend (UpdateFanSessionRequest, Metadata) -> UpdateFanSessionResponse = fanportalFanSessionService::updateFanSession
            val reply0 = invoke0(outbound0, Metadata())
            lookupDriverHomeResponse {
                this.driverHome = driverHome {
                    this.driverHomeId = request.driverHomeId
                }
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleResolveDriverHomes(request: ResolveDriverHomesRequest): ResolveDriverHomesResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeQueryService/ResolveDriverHomes") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResolveDriverHomes", request)
            log.atInfo().addKeyValue("operation", "ResolveDriverHomes").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            resolveDriverHomesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSummarizeDriverHomes(request: SummarizeDriverHomesRequest): SummarizeDriverHomesResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeQueryService/SummarizeDriverHomes") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SummarizeDriverHomes", request)
            log.atInfo().addKeyValue("operation", "SummarizeDriverHomes").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverHomeId).log("Recorded audit detail")
            val outbound0 = acknowledgeDriverHomeEventRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
            }
            val reply0 = ClientRegistry.lookup("acme.driverapp.v1.DriverHomeIntegrationService", DriverHomeIntegrationServiceGrpcKt.DriverHomeIntegrationServiceCoroutineStub::class.java).acknowledgeDriverHomeEvent(outbound0)
            val outbound1 = approveDriverInboxRequest {
                this.context = request.context
                this.driverInbox = driverInbox {
                    this.audit = auditInfo {
                        this.createdBy = reply0.driverHome.audit.createdBy
                    }
                }
            }
            val reply1 = driverappDriverInboxApprovalService.approveDriverInbox(WireCopies.reflective(outbound1))
            summarizeDriverHomesResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply1.result.message
                    this.status = "ok"
                }
            }
        }
}
