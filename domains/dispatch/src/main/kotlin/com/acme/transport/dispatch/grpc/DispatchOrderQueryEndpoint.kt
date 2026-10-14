package com.acme.transport.dispatch.grpc

import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.dispatch.v1.BrowseDispatchOrdersRequest
import com.acme.dispatch.v1.BrowseDispatchOrdersResponse
import com.acme.dispatch.v1.CountDispatchOrdersRequest
import com.acme.dispatch.v1.CountDispatchOrdersResponse
import com.acme.dispatch.v1.DispatchAssignmentValidationServiceGrpcKt
import com.acme.dispatch.v1.DispatchOrderQueryServiceGrpcKt
import com.acme.dispatch.v1.ExportDispatchOrdersRequest
import com.acme.dispatch.v1.ExportDispatchOrdersResponse
import com.acme.dispatch.v1.LookupDispatchOrderRequest
import com.acme.dispatch.v1.LookupDispatchOrderResponse
import com.acme.dispatch.v1.NormalizeDispatchAssignmentRequestKt
import com.acme.dispatch.v1.PeekDispatchOrderRequest
import com.acme.dispatch.v1.PeekDispatchOrderResponse
import com.acme.dispatch.v1.ResolveDispatchOrdersRequest
import com.acme.dispatch.v1.ResolveDispatchOrdersResponse
import com.acme.dispatch.v1.SuggestDispatchOrdersRequest
import com.acme.dispatch.v1.SuggestDispatchOrdersResponse
import com.acme.dispatch.v1.SummarizeDispatchOrdersRequest
import com.acme.dispatch.v1.SummarizeDispatchOrdersResponse
import com.acme.dispatch.v1.browseDispatchOrdersResponse
import com.acme.dispatch.v1.countDispatchOrdersResponse
import com.acme.dispatch.v1.dispatchAssignment
import com.acme.dispatch.v1.dispatchOrder
import com.acme.dispatch.v1.exportDispatchOrdersResponse
import com.acme.dispatch.v1.lintDispatchAssignmentsRequest
import com.acme.dispatch.v1.lookupDispatchOrderResponse
import com.acme.dispatch.v1.normalizeDispatchAssignmentRequest
import com.acme.dispatch.v1.peekDispatchOrderResponse
import com.acme.dispatch.v1.resolveDispatchOrdersResponse
import com.acme.dispatch.v1.suggestDispatchOrdersResponse
import com.acme.dispatch.v1.summarizeDispatchOrdersResponse
import com.acme.dispatch.v1.validateDispatchAssignmentRequest
import com.acme.dispatch.v1.zoneRef
import com.acme.documents.v1.DocumentValidationServiceGrpcKt
import com.acme.documents.v1.ValidateDocumentRequest
import com.acme.documents.v1.ValidateDocumentRequestKt
import com.acme.documents.v1.ValidateDocumentResponse
import com.acme.documents.v1.document
import com.acme.documents.v1.validateDocumentRequest
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.ExplainServiceScheduleDecisionRequest
import com.acme.maintenance.v1.ExplainServiceScheduleDecisionResponse
import com.acme.maintenance.v1.ServiceScheduleValidationServiceGrpcKt
import com.acme.maintenance.v1.explainServiceScheduleDecisionRequest
import com.acme.maintenance.v1.serviceSchedule
import com.acme.maintenance.v1.serviceScheduleParticipant
import com.acme.notifications.v1.NotificationQueryServiceGrpcKt
import com.acme.notifications.v1.SummarizeNotificationsRequestKt
import com.acme.notifications.v1.summarizeNotificationsRequest
import com.acme.telematics.v1.LintTripLogsRequestKt
import com.acme.telematics.v1.TripLogValidationServiceGrpcKt
import com.acme.telematics.v1.lintTripLogsRequest
import com.acme.transport.dispatch.DispatchRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.venues.v1.UpdateVenueRequest
import com.acme.venues.v1.UpdateVenueResponse
import com.acme.venues.v1.VenueServiceGrpcKt
import com.acme.venues.v1.updateVenueRequest
import com.acme.venues.v1.venue
import com.acme.yards.v1.CheckDockEligibilityRequest
import com.acme.yards.v1.CompareYardOptionsRequestKt
import com.acme.yards.v1.DockValidationServiceGrpcKt
import com.acme.yards.v1.YardCalculationServiceGrpcKt
import com.acme.yards.v1.checkDockEligibilityRequest
import com.acme.yards.v1.compareYardOptionsRequest
import com.acme.yards.v1.dock
import com.acme.yards.v1.yard
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Answers read-only questions about dispatch orders, for screens and reports. */
class DispatchOrderQueryEndpoint(
    private val store: DispatchRepository,
    private val workers: CoroutineScope,
    private val dispatchDispatchAssignmentValidationService: DispatchAssignmentValidationServiceGrpcKt.DispatchAssignmentValidationServiceCoroutineStub,
    private val documentsDocumentValidationService: DocumentValidationServiceGrpcKt.DocumentValidationServiceCoroutineStub,
    private val maintenanceServiceScheduleValidationService: ServiceScheduleValidationServiceGrpcKt.ServiceScheduleValidationServiceCoroutineStub,
    private val notificationsNotificationQueryService: NotificationQueryServiceGrpcKt.NotificationQueryServiceCoroutineStub,
    private val telematicsTripLogValidationService: TripLogValidationServiceGrpcKt.TripLogValidationServiceCoroutineStub,
    private val venuesVenueService: VenueServiceGrpcKt.VenueServiceCoroutineStub,
    private val yardsDockValidationService: DockValidationServiceGrpcKt.DockValidationServiceCoroutineStub,
    private val yardsYardCalculationService: YardCalculationServiceGrpcKt.YardCalculationServiceCoroutineStub,
) : DispatchOrderQueryServiceGrpcKt.DispatchOrderQueryServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DispatchOrderQueryEndpoint::class.java)

    private val invokeBrowseDispatchOrders0: suspend (ExplainServiceScheduleDecisionRequest) -> ExplainServiceScheduleDecisionResponse = { payload ->
        maintenanceServiceScheduleValidationService.explainServiceScheduleDecision(payload)
    }

    private val workerForResolveDispatchOrders0 = ReplyMailbox<UpdateVenueRequest, UpdateVenueResponse>(workers) { payload ->
        withTimeout(5_000) { venuesVenueService.updateVenue(payload) }
    }

    private val clientForResolveDispatchOrders1 by lazy { dispatchDispatchAssignmentValidationService }

    /** Lists dispatch orders a page at a time. */
    override suspend fun browseDispatchOrders(request: BrowseDispatchOrdersRequest): BrowseDispatchOrdersResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderQueryService/BrowseDispatchOrders") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BrowseDispatchOrders", request)
            log.atInfo().addKeyValue("operation", "BrowseDispatchOrders").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = explainServiceScheduleDecisionRequest {
                this.context = request.context
                this.serviceSchedule = serviceSchedule {
                    this.audit = auditInfo {
                        this.updatedAt = request.filter.createdAfter
                    }
                    this.location = postalAddress {
                        this.region = request.filter.region
                    }
                    this.organizer = userRef {
                        this.userId = request.context.actor.userId
                    }
                    this.participants.addAll(listOf(serviceScheduleParticipant {
                        this.contact = contactInfo {
                            this.email = request.context.actor.email
                        }
                    }))
                }
            }
            val reply0 = invokeBrowseDispatchOrders0(outbound0)
            browseDispatchOrdersResponse {
                this.dispatchOrders.addAll(listOf(dispatchOrder {
                    this.billingAddress = postalAddress {
                        this.lines.addAll(reply0.serviceSchedule.location.linesList)
                        this.locality = reply0.serviceSchedule.location.locality
                    }
                }))
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Counts dispatch orders by status. */
    override suspend fun countDispatchOrders(request: CountDispatchOrdersRequest): CountDispatchOrdersResponse {
        return handleCountDispatchOrders(request)
    }

    /** Streams every matching dispatch order. */
    override fun exportDispatchOrders(request: ExportDispatchOrdersRequest): Flow<ExportDispatchOrdersResponse> = flow {
        emit(handleExportDispatchOrders(request))
    }

    /** Returns a dispatch order. */
    override suspend fun lookupDispatchOrder(request: LookupDispatchOrderRequest): LookupDispatchOrderResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderQueryService/LookupDispatchOrder") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LookupDispatchOrder", request)
            log.atInfo().addKeyValue("operation", "LookupDispatchOrder").log("Handling transport request")
            val outbound0 = normalizeDispatchAssignmentRequest {
                this.context = request.context
                this.details = NormalizeDispatchAssignmentRequestKt.details {
                    this.locale = request.context.locale
                }
                this.dispatchAssignment = dispatchAssignment {
                    this.assignee = userRef {
                        this.userId = request.context.actor.userId
                    }
                    this.requester = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = ClientRegistry.lookup("acme.dispatch.v1.DispatchAssignmentValidationService", DispatchAssignmentValidationServiceGrpcKt.DispatchAssignmentValidationServiceCoroutineStub::class.java).normalizeDispatchAssignment(outbound0)
            lookupDispatchOrderResponse {
                this.dispatchOrder = dispatchOrder {
                    this.counterparty = zoneRef {
                        this.displayName = reply0.dispatchAssignment.customer.displayName
                    }
                    this.dispatchOrderId = request.dispatchOrderId
                }
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }

    /** Returns a dispatch order. */
    override suspend fun peekDispatchOrder(request: PeekDispatchOrderRequest): PeekDispatchOrderResponse {
        return handlePeekDispatchOrder(request)
    }

    /** Returns several dispatch orders at once. */
    override suspend fun resolveDispatchOrders(request: ResolveDispatchOrdersRequest): ResolveDispatchOrdersResponse = handleResolveDispatchOrders(request)

    /** Lists dispatch orders a page at a time. */
    override suspend fun suggestDispatchOrders(request: SuggestDispatchOrdersRequest): SuggestDispatchOrdersResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderQueryService/SuggestDispatchOrders") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SuggestDispatchOrders", request)
            log.atInfo().addKeyValue("operation", "SuggestDispatchOrders").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = compareYardOptionsRequest {
                this.context = request.context
                this.details = CompareYardOptionsRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.filter.region
                    }
                }
                this.yard = yard {
                    this.owner = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply0 = flowOf(outbound0).map { value -> yardsYardCalculationService.compareYardOptions(value) }.single()
            suggestDispatchOrdersResponse {
                this.dispatchOrders.addAll(listOf(dispatchOrder {
                    this.total = reply0.breakdown.total
                }))
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Summarizes a dispatch order. */
    override suspend fun summarizeDispatchOrders(request: SummarizeDispatchOrdersRequest): SummarizeDispatchOrdersResponse {
        return handleSummarizeDispatchOrders(request)
    }

    private suspend fun handleCountDispatchOrders(request: CountDispatchOrdersRequest): CountDispatchOrdersResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderQueryService/CountDispatchOrders") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CountDispatchOrders", request)
            log.atInfo().addKeyValue("operation", "CountDispatchOrders").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = validateDocumentRequest {
                this.context = request.context
                this.details = ValidateDocumentRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
                this.document = document {
                    this.billingAddress = postalAddress {
                        this.region = request.filter.region
                    }
                }
            }
            val invoke0: suspend (ValidateDocumentRequest, Metadata) -> ValidateDocumentResponse = documentsDocumentValidationService::validateDocument
            val reply0 = invoke0(outbound0, Metadata())
            countDispatchOrdersResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.message = reply0.outcome.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleExportDispatchOrders(request: ExportDispatchOrdersRequest): ExportDispatchOrdersResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderQueryService/ExportDispatchOrders") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExportDispatchOrders", request)
            log.atInfo().addKeyValue("operation", "ExportDispatchOrders").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = CheckDockEligibilityRequest.newBuilder().apply {
                setContext(request.context)
                setDock(dock {
                    this.address = postalAddress {
                        this.region = request.filter.region
                    }
                    this.owner = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> yardsDockValidationService.checkDockEligibility(value) }
            val outbound1 = RequestFactory.create {
                lintDispatchAssignmentsRequest {
                    this.context = request.context
                    this.dispatchAssignment = dispatchAssignment {
                        this.assignee = userRef {
                            this.displayName = reply0.dock.owner.displayName
                            this.email = reply0.dock.owner.email
                        }
                        this.audit = auditInfo {
                            this.updatedAt = reply0.dock.audit.updatedAt
                            this.updatedBy = reply0.dock.audit.updatedBy
                        }
                    }
                }
            }
            val reply1 = dispatchDispatchAssignmentValidationService.lintDispatchAssignments(outbound1)
            exportDispatchOrdersResponse {
                this.dispatchOrder = dispatchOrder {
                    this.audit = auditInfo {
                        this.updatedBy = reply1.dispatchAssignment.audit.updatedBy
                    }
                    this.billingAddress = postalAddress {
                        this.postalCode = reply0.dock.address.postalCode
                        this.region = request.filter.region
                    }
                    this.counterparty = zoneRef {
                        this.displayName = reply1.dispatchAssignment.assignee.displayName
                    }
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handlePeekDispatchOrder(request: PeekDispatchOrderRequest): PeekDispatchOrderResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderQueryService/PeekDispatchOrder") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PeekDispatchOrder", request)
            log.atInfo().addKeyValue("operation", "PeekDispatchOrder").log("Handling transport request")
            val outbound0 = validateDispatchAssignmentRequest {
                this.context = request.context
                this.dispatchAssignment = dispatchAssignment {
                    this.assignee = userRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = dispatchDispatchAssignmentValidationService.validateDispatchAssignment(WireCopies.reflective(outbound0))
            peekDispatchOrderResponse {
                this.dispatchOrder = dispatchOrder {
                    this.dispatchOrderId = request.dispatchOrderId
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = reply0.outcome.status
                }
            }
        }

    private suspend fun handleResolveDispatchOrders(request: ResolveDispatchOrdersRequest): ResolveDispatchOrdersResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderQueryService/ResolveDispatchOrders") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResolveDispatchOrders", request)
            log.atInfo().addKeyValue("operation", "ResolveDispatchOrders").log("Handling transport request")
            val outbound0 = updateVenueRequest {
                this.context = request.context
                this.venue = venue {
                    this.audit = auditInfo {
                        this.createdAt = request.context.deadline
                    }
                    this.owner = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply0 = workerForResolveDispatchOrders0.submit(outbound0)
            val outbound1 = normalizeDispatchAssignmentRequest {
                this.context = request.context
                this.dispatchAssignment = dispatchAssignment {
                    this.assignee = userRef {
                        this.email = reply0.venue.owner.email
                    }
                }
            }
            val reply1 = clientForResolveDispatchOrders1.normalizeDispatchAssignment(outbound1)
            resolveDispatchOrdersResponse {
                this.dispatchOrders.addAll(listOf(dispatchOrder {
                    this.audit = auditInfo {
                        this.updatedAt = reply1.dispatchAssignment.audit.updatedAt
                        this.updatedBy = reply1.dispatchAssignment.audit.updatedBy
                    }
                    this.billingAddress = postalAddress {
                        this.locality = reply0.venue.address.locality
                    }
                }))
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSummarizeDispatchOrders(request: SummarizeDispatchOrdersRequest): SummarizeDispatchOrdersResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderQueryService/SummarizeDispatchOrders") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SummarizeDispatchOrders", request)
            log.atInfo().addKeyValue("operation", "SummarizeDispatchOrders").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchOrderId).log("Recorded audit detail")
            val outbound0 = lintTripLogsRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.details = LintTripLogsRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = withTimeout(5_000) { telematicsTripLogValidationService.lintTripLogs(outbound0) }
            val configure1: SummarizeNotificationsRequestKt.Dsl.() -> Unit = {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.details = SummarizeNotificationsRequestKt.details {
                    this.filters.putAll(request.details.filtersMap)
                    this.period = request.details.period
                }
            }
            val outbound1 = summarizeNotificationsRequest(configure1)
            val reply1 = notificationsNotificationQueryService.summarizeNotifications(outbound1)
            summarizeDispatchOrdersResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.referenceIds.addAll((reply0.result.referenceIdsList) + (reply1.result.referenceIdsList))
                    this.status = reply0.outcome.status
                }
            }
        }
}
