package com.acme.transport.dispatch.api

import com.acme.audit.v1.AuditEventServiceGrpcKt
import com.acme.audit.v1.batchGetAuditEventsRequest
import com.acme.boxoffice.v1.BoxOfficeSale
import com.acme.boxoffice.v1.BoxOfficeSaleCalculationServiceGrpcKt
import com.acme.boxoffice.v1.RecalculateBoxOfficeSalesRequest
import com.acme.boxoffice.v1.RecalculateBoxOfficeSalesResponse
import com.acme.boxoffice.v1.boxOfficeSale
import com.acme.boxoffice.v1.recalculateBoxOfficeSalesRequest
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.dispatch.v1.AdvanceDispatchAssignmentWorkflowRequest
import com.acme.dispatch.v1.AdvanceDispatchAssignmentWorkflowResponse
import com.acme.dispatch.v1.CancelDispatchAssignmentWorkflowRequest
import com.acme.dispatch.v1.CancelDispatchAssignmentWorkflowResponse
import com.acme.dispatch.v1.CancelDispatchAssignmentWorkflowResponseKt
import com.acme.dispatch.v1.DispatchAssignmentComment
import com.acme.dispatch.v1.DispatchAssignmentWorkflowServiceGrpcKt
import com.acme.dispatch.v1.DispatchOrderApprovalServiceGrpcKt
import com.acme.dispatch.v1.DispatchOrderServiceGrpcKt
import com.acme.dispatch.v1.DispatchOrderValidationServiceGrpcKt
import com.acme.dispatch.v1.DispatchOrderWorkflowServiceGrpcKt
import com.acme.dispatch.v1.EscalateDispatchOrderRequest
import com.acme.dispatch.v1.EscalateDispatchOrderRequestKt
import com.acme.dispatch.v1.EscalateDispatchOrderResponse
import com.acme.dispatch.v1.ExplainZoneCalculationRequest
import com.acme.dispatch.v1.InspectDispatchAssignmentWorkflowRequest
import com.acme.dispatch.v1.InspectDispatchAssignmentWorkflowResponse
import com.acme.dispatch.v1.LintDispatchOrdersRequestKt
import com.acme.dispatch.v1.ListDispatchAssignmentWorkflowsRequest
import com.acme.dispatch.v1.ListDispatchAssignmentWorkflowsResponse
import com.acme.dispatch.v1.ListPendingZonesRequestKt
import com.acme.dispatch.v1.PauseDispatchOrderWorkflowRequestKt
import com.acme.dispatch.v1.ResumeDispatchAssignmentWorkflowRequest
import com.acme.dispatch.v1.ResumeDispatchAssignmentWorkflowResponse
import com.acme.dispatch.v1.RetryDispatchAssignmentWorkflowRequest
import com.acme.dispatch.v1.RetryDispatchAssignmentWorkflowResponse
import com.acme.dispatch.v1.RetryDispatchAssignmentWorkflowResponseKt
import com.acme.dispatch.v1.SignalDispatchAssignmentWorkflowRequest
import com.acme.dispatch.v1.SignalDispatchAssignmentWorkflowResponse
import com.acme.dispatch.v1.StartDispatchAssignmentWorkflowRequest
import com.acme.dispatch.v1.StartDispatchAssignmentWorkflowResponse
import com.acme.dispatch.v1.StartDispatchAssignmentWorkflowResponseKt
import com.acme.dispatch.v1.ZoneApprovalServiceGrpcKt
import com.acme.dispatch.v1.ZoneCalculationServiceGrpcKt
import com.acme.dispatch.v1.advanceDispatchAssignmentWorkflowResponse
import com.acme.dispatch.v1.cancelDispatchAssignmentWorkflowResponse
import com.acme.dispatch.v1.copy as dispatchCopy
import com.acme.dispatch.v1.createDispatchOrderRequest
import com.acme.dispatch.v1.dispatchAssignment
import com.acme.dispatch.v1.dispatchAssignmentSla
import com.acme.dispatch.v1.dispatchOrder
import com.acme.dispatch.v1.dispatchOrderCardSettlement
import com.acme.dispatch.v1.dispatchOrderLine
import com.acme.dispatch.v1.dispatchOrderLineTax
import com.acme.dispatch.v1.escalateDispatchOrderRequest
import com.acme.dispatch.v1.explainZoneCalculationRequest
import com.acme.dispatch.v1.inspectDispatchAssignmentWorkflowResponse
import com.acme.dispatch.v1.lintDispatchOrdersRequest
import com.acme.dispatch.v1.listDispatchAssignmentWorkflowsResponse
import com.acme.dispatch.v1.listPendingZonesRequest
import com.acme.dispatch.v1.pauseDispatchOrderWorkflowRequest
import com.acme.dispatch.v1.resumeDispatchAssignmentWorkflowResponse
import com.acme.dispatch.v1.retryDispatchAssignmentWorkflowResponse
import com.acme.dispatch.v1.signalDispatchAssignmentWorkflowResponse
import com.acme.dispatch.v1.signalDispatchOrderWorkflowRequest
import com.acme.dispatch.v1.startDispatchAssignmentWorkflowResponse
import com.acme.dispatch.v1.zone
import com.acme.dispatch.v1.zoneRef
import com.acme.documents.v1.CreateDocumentRequest
import com.acme.documents.v1.CreateDocumentResponse
import com.acme.documents.v1.DocumentServiceGrpcKt
import com.acme.documents.v1.createDocumentRequest
import com.acme.documents.v1.document
import com.acme.drivers.v1.CountDriversRequestKt
import com.acme.drivers.v1.DriverQueryServiceGrpcKt
import com.acme.drivers.v1.DriverValidationServiceGrpcKt
import com.acme.drivers.v1.PreviewDriverRequestKt
import com.acme.drivers.v1.PreviewDriverResponse
import com.acme.drivers.v1.countDriversRequest
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.previewDriverRequest
import com.acme.fuel.v1.FuelCardApprovalServiceGrpcKt
import com.acme.fuel.v1.ListPendingFuelCardsRequest
import com.acme.fuel.v1.ListPendingFuelCardsRequestKt
import com.acme.fuel.v1.listPendingFuelCardsRequest
import com.acme.identity.v1.userRef
import com.acme.localization.v1.TranslationKeyCalculationServiceGrpcKt
import com.acme.localization.v1.calculateTranslationKeyRequest
import com.acme.localization.v1.translationKey
import com.acme.metrics.v1.CloneMetricSeriesResponse
import com.acme.metrics.v1.MetricSeriesServiceGrpcKt
import com.acme.metrics.v1.cloneMetricSeriesRequest
import com.acme.metrics.v1.metricSeries
import com.acme.telematics.v1.GeoFenceImportServiceGrpcKt
import com.acme.telematics.v1.ValidateGeoFenceImportResponse
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.geoFenceBlockAction
import com.acme.telematics.v1.validateGeoFenceImportRequest
import com.acme.transport.dispatch.DispatchRepository
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.venues.v1.ExplainVenueDecisionRequest
import com.acme.venues.v1.VenueValidationServiceGrpcKt
import com.acme.venues.v1.explainVenueDecisionRequest
import com.acme.venues.v1.venue
import com.google.protobuf.Any as ProtoAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Runs the long-lived workflow that takes a dispatch assignment from start to finish. */
class DispatchAssignmentWorkflowGrpcService(
    private val store: DispatchRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventService: AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub,
    private val boxofficeBoxOfficeSaleCalculationService: BoxOfficeSaleCalculationServiceGrpcKt.BoxOfficeSaleCalculationServiceCoroutineStub,
    private val dispatchDispatchOrderApprovalService: DispatchOrderApprovalServiceGrpcKt.DispatchOrderApprovalServiceCoroutineStub,
    private val dispatchDispatchOrderService: DispatchOrderServiceGrpcKt.DispatchOrderServiceCoroutineStub,
    private val dispatchDispatchOrderValidationService: DispatchOrderValidationServiceGrpcKt.DispatchOrderValidationServiceCoroutineStub,
    private val dispatchDispatchOrderWorkflowService: DispatchOrderWorkflowServiceGrpcKt.DispatchOrderWorkflowServiceCoroutineStub,
    private val dispatchZoneApprovalService: ZoneApprovalServiceGrpcKt.ZoneApprovalServiceCoroutineStub,
    private val dispatchZoneCalculationService: ZoneCalculationServiceGrpcKt.ZoneCalculationServiceCoroutineStub,
    private val documentsDocumentService: DocumentServiceGrpcKt.DocumentServiceCoroutineStub,
    private val driversDriverQueryService: DriverQueryServiceGrpcKt.DriverQueryServiceCoroutineStub,
    private val driversDriverValidationService: DriverValidationServiceGrpcKt.DriverValidationServiceCoroutineStub,
    private val fuelFuelCardApprovalService: FuelCardApprovalServiceGrpcKt.FuelCardApprovalServiceCoroutineStub,
    private val localizationTranslationKeyCalculationService: TranslationKeyCalculationServiceGrpcKt.TranslationKeyCalculationServiceCoroutineStub,
    private val metricsMetricSeriesService: MetricSeriesServiceGrpcKt.MetricSeriesServiceCoroutineStub,
    private val telematicsGeoFenceImportService: GeoFenceImportServiceGrpcKt.GeoFenceImportServiceCoroutineStub,
    private val venuesVenueValidationService: VenueValidationServiceGrpcKt.VenueValidationServiceCoroutineStub,
) : DispatchAssignmentWorkflowServiceGrpcKt.DispatchAssignmentWorkflowServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DispatchAssignmentWorkflowGrpcService::class.java)

    private val invokeRetryDispatchAssignmentWorkflow1: suspend (RecalculateBoxOfficeSalesRequest) -> RecalculateBoxOfficeSalesResponse = { payload ->
        boxofficeBoxOfficeSaleCalculationService.recalculateBoxOfficeSales(payload)
    }

    private val workerForStartDispatchAssignmentWorkflow0 = ReplyMailbox<CreateDocumentRequest, CreateDocumentResponse>(workers) { payload ->
        withTimeout(5_000) { documentsDocumentService.createDocument(payload) }
    }

    private val clientForStartDispatchAssignmentWorkflow1 by lazy { dispatchDispatchOrderValidationService }

    /** Advances a dispatch assignment. */
    override suspend fun advanceDispatchAssignmentWorkflow(request: AdvanceDispatchAssignmentWorkflowRequest): AdvanceDispatchAssignmentWorkflowResponse {
        return handleAdvanceDispatchAssignmentWorkflow(request)
    }

    /** Cancels a dispatch assignment. */
    override suspend fun cancelDispatchAssignmentWorkflow(request: CancelDispatchAssignmentWorkflowRequest): CancelDispatchAssignmentWorkflowResponse = handleCancelDispatchAssignmentWorkflow(request)

    /** Returns a dispatch assignment. */
    override suspend fun inspectDispatchAssignmentWorkflow(request: InspectDispatchAssignmentWorkflowRequest): InspectDispatchAssignmentWorkflowResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentWorkflowService/InspectDispatchAssignmentWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "InspectDispatchAssignmentWorkflow", request)
            log.atInfo().addKeyValue("operation", "InspectDispatchAssignmentWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            inspectDispatchAssignmentWorkflowResponse {
                this.dispatchAssignment = dispatchAssignment {
                    this.dispatchAssignmentId = request.dispatchAssignmentId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Lists dispatch assignments a page at a time. */
    override suspend fun listDispatchAssignmentWorkflows(request: ListDispatchAssignmentWorkflowsRequest): ListDispatchAssignmentWorkflowsResponse {
        return handleListDispatchAssignmentWorkflows(request)
    }

    /** Resumes a dispatch assignment. */
    override suspend fun resumeDispatchAssignmentWorkflow(request: ResumeDispatchAssignmentWorkflowRequest): ResumeDispatchAssignmentWorkflowResponse = handleResumeDispatchAssignmentWorkflow(request)

    /** Retrys a dispatch assignment. */
    override suspend fun retryDispatchAssignmentWorkflow(request: RetryDispatchAssignmentWorkflowRequest): RetryDispatchAssignmentWorkflowResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentWorkflowService/RetryDispatchAssignmentWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RetryDispatchAssignmentWorkflow", request)
            log.atInfo().addKeyValue("operation", "RetryDispatchAssignmentWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchAssignment.dispatchAssignmentId).log("Recorded audit detail")
            val outbound0 = escalateDispatchOrderRequest {
                this.context = request.context
                this.details = EscalateDispatchOrderRequestKt.details {
                    this.approver = userRef {
                        this.email = request.details.notify.email
                    }
                }
                this.dispatchOrder = dispatchOrder {
                    this.audit = auditInfo {
                        this.createdAt = request.dispatchAssignment.audit.createdAt
                        this.updatedBy = request.dispatchAssignment.audit.updatedBy
                    }
                }
            }
            val adapter0 = OutboundPort<EscalateDispatchOrderRequest, EscalateDispatchOrderResponse> { payload ->
                dispatchDispatchOrderApprovalService.escalateDispatchOrder(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = recalculateBoxOfficeSalesRequest {
                this.boxOfficeSale = boxOfficeSale {
                    this.audit = auditInfo {
                        this.createdAt = reply0.dispatchOrder.audit.createdAt
                        this.createdBy = request.dispatchAssignment.audit.createdBy
                        this.updatedAt = reply0.dispatchOrder.audit.updatedAt
                    }
                }
                this.context = request.context
            }
            val reply1 = invokeRetryDispatchAssignmentWorkflow1(outbound1)
            val outbound2 = signalDispatchOrderWorkflowRequest {
                this.context = request.context
                this.dispatchOrder = dispatchOrder {
                    this.billingAddress = postalAddress {
                        this.locality = reply0.dispatchOrder.billingAddress.locality
                    }
                    this.card = dispatchOrderCardSettlement {
                        this.network = when (reply1.boxOfficeSale.settlementCase) {
                            BoxOfficeSale.SettlementCase.CARD -> reply1.boxOfficeSale.card.network
                            else -> ""
                        }
                    }
                }
            }
            val reply2 = dispatchDispatchOrderWorkflowService.signalDispatchOrderWorkflow(outbound2)
            retryDispatchAssignmentWorkflowResponse {
                this.dispatchAssignment = dispatchAssignment {
                    this.assignee = userRef {
                        this.displayName = reply2.dispatchOrder.counterparty.displayName
                    }
                    this.audit = auditInfo {
                        this.createdAt = reply2.dispatchOrder.audit.createdAt
                        this.etag = reply1.boxOfficeSale.audit.etag
                    }
                    this.customFields.putAll(request.dispatchAssignment.customFieldsMap)
                    this.dispatchAssignmentId = request.dispatchAssignmentId
                    this.sla = dispatchAssignmentSla {
                        this.dueAt = reply0.dispatchOrder.dueAt
                    }
                }
                this.outcome = RetryDispatchAssignmentWorkflowResponseKt.outcome {
                    this.message = reply1.result.message
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Signals a dispatch assignment. */
    override suspend fun signalDispatchAssignmentWorkflow(request: SignalDispatchAssignmentWorkflowRequest): SignalDispatchAssignmentWorkflowResponse {
        return handleSignalDispatchAssignmentWorkflow(request)
    }

    /** Starts a dispatch assignment. */
    override suspend fun startDispatchAssignmentWorkflow(request: StartDispatchAssignmentWorkflowRequest): StartDispatchAssignmentWorkflowResponse = handleStartDispatchAssignmentWorkflow(request)

    private suspend fun handleAdvanceDispatchAssignmentWorkflow(request: AdvanceDispatchAssignmentWorkflowRequest): AdvanceDispatchAssignmentWorkflowResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentWorkflowService/AdvanceDispatchAssignmentWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "AdvanceDispatchAssignmentWorkflow", request)
            log.atInfo().addKeyValue("operation", "AdvanceDispatchAssignmentWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchAssignment.dispatchAssignmentId).log("Recorded audit detail")
            val outbound0 = listPendingZonesRequest {
                this.context = request.context
                this.filter = ListPendingZonesRequestKt.filter {
                    this.createdAfter = request.dispatchAssignment.audit.createdAt
                    this.ownerId = request.dispatchAssignment.assignee.userId
                }
            }
            val reply0 = dispatchZoneApprovalService.listPendingZones(outbound0)
            val outbound1 = pauseDispatchOrderWorkflowRequest {}.dispatchCopy {
                this.context = request.context
                this.details = PauseDispatchOrderWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.email = request.dispatchAssignment.customer.email
                    }
                }
            }
            val reply1 = dispatchDispatchOrderWorkflowService.pauseDispatchOrderWorkflow(outbound1)
            advanceDispatchAssignmentWorkflowResponse {
                this.dispatchAssignment = dispatchAssignment {
                    this.dispatchAssignmentId = request.dispatchAssignmentId
                    this.priority = request.dispatchAssignment.priority
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private fun CancelDispatchAssignmentWorkflowRequest.toListPendingFuelCardsFromCancelDispatchAssignmentWorkflow0(): ListPendingFuelCardsRequest {
        val source = this
        return listPendingFuelCardsRequest {
            this.context = source.context
            this.filter = ListPendingFuelCardsRequestKt.filter {
                this.createdAfter = source.dispatchAssignment.audit.createdAt
                this.labels.putAll(source.details.parametersMap)
                this.ownerId = source.dispatchAssignment.assignee.userId
            }
        }
    }

    private suspend fun handleCancelDispatchAssignmentWorkflow(request: CancelDispatchAssignmentWorkflowRequest): CancelDispatchAssignmentWorkflowResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentWorkflowService/CancelDispatchAssignmentWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CancelDispatchAssignmentWorkflow", request)
            log.atInfo().addKeyValue("operation", "CancelDispatchAssignmentWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchAssignment.dispatchAssignmentId).log("Recorded audit detail")
            val outbound0 = request.toListPendingFuelCardsFromCancelDispatchAssignmentWorkflow0()
            val reply0 = fuelFuelCardApprovalService.listPendingFuelCards(outbound0)
            val outbound1 = batchGetAuditEventsRequest {
                this.context = request.context
            }
            val reply1 = auditAuditEventService.batchGetAuditEvents(outbound1)
            val outbound2 = explainVenueDecisionRequest {
                this.context = request.context
                this.venue = venue {
                    this.audit = auditInfo {
                        this.createdBy = request.dispatchAssignment.audit.createdBy
                    }
                    this.owner = userRef {
                        this.email = request.dispatchAssignment.customer.email
                        this.userId = request.dispatchAssignment.assignee.userId
                    }
                }
            }
            val envelope2 = ProtoAny.pack(outbound2)
            val reply2 = venuesVenueValidationService.explainVenueDecision(envelope2.unpack(ExplainVenueDecisionRequest::class.java))
            cancelDispatchAssignmentWorkflowResponse {
                this.dispatchAssignment = dispatchAssignment {
                    this.assignee = request.dispatchAssignment.assignee
                    this.audit = auditInfo {
                        this.createdAt = reply2.venue.audit.createdAt
                    }
                    this.dispatchAssignmentId = request.dispatchAssignmentId
                }
                this.outcome = CancelDispatchAssignmentWorkflowResponseKt.outcome {
                    this.status = reply2.result.status
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleListDispatchAssignmentWorkflows(request: ListDispatchAssignmentWorkflowsRequest): ListDispatchAssignmentWorkflowsResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentWorkflowService/ListDispatchAssignmentWorkflows") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListDispatchAssignmentWorkflows", request)
            log.atInfo().addKeyValue("operation", "ListDispatchAssignmentWorkflows").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            listDispatchAssignmentWorkflowsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleResumeDispatchAssignmentWorkflow(request: ResumeDispatchAssignmentWorkflowRequest): ResumeDispatchAssignmentWorkflowResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentWorkflowService/ResumeDispatchAssignmentWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResumeDispatchAssignmentWorkflow", request)
            log.atInfo().addKeyValue("operation", "ResumeDispatchAssignmentWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchAssignment.dispatchAssignmentId).log("Recorded audit detail")
            val outbound0 = calculateTranslationKeyRequest {
                this.context = request.context
                this.translationKey = translationKey {
                    this.audit = auditInfo {
                        this.createdAt = request.dispatchAssignment.audit.createdAt
                        this.createdBy = request.dispatchAssignment.audit.createdBy
                    }
                    this.owner = userRef {
                        this.displayName = request.dispatchAssignment.customer.displayName
                    }
                }
            }
            val reply0 = localizationTranslationKeyCalculationService.calculateTranslationKey(WireCopies.json(outbound0))
            var reply1 = CloneMetricSeriesResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.dispatchAssignment.commentsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound1 = cloneMetricSeriesRequest {
                                this.context = request.context
                                this.metricSeries = metricSeries {
                                    this.audit = auditInfo {
                                        this.createdAt = item.createdAt
                                    }
                                    this.owner = userRef {
                                        this.email = item.authorEmail
                                    }
                                }
                            }
                            metricsMetricSeriesService.cloneMetricSeries(outbound1)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply1 = it }
                }
            }
            val outbound2 = explainZoneCalculationRequest {
                this.context = request.context
                this.zone = zone {
                    this.children.addAll(listOf(zone {
                        this.children.addAll(listOf(zone {
                            this.audit = auditInfo {
                                this.updatedBy = reply1.metricSeries.audit.updatedBy
                            }
                            this.location = postalAddress {
                                this.locality = reply1.metricSeries.address.locality
                            }
                            this.manager = userRef {
                                this.displayName = request.dispatchAssignment.assignee.displayName
                            }
                        }))
                    }))
                }
            }
            val command2: Submission<ExplainZoneCalculationRequest> = Submission.Live(outbound2)
            val payload2 = when (val command = command2) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply2 = dispatchZoneCalculationService.explainZoneCalculation(payload2)
            resumeDispatchAssignmentWorkflowResponse {
                this.dispatchAssignment = dispatchAssignment {
                    this.audit = auditInfo {
                        this.createdAt = reply0.translationKey.audit.createdAt
                    }
                    this.customFields.putAll(request.dispatchAssignment.customFieldsMap)
                    this.dispatchAssignmentId = request.dispatchAssignmentId
                    this.requester = contactInfo {
                        this.email = reply2.zone.manager.email
                    }
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSignalDispatchAssignmentWorkflow(request: SignalDispatchAssignmentWorkflowRequest): SignalDispatchAssignmentWorkflowResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentWorkflowService/SignalDispatchAssignmentWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SignalDispatchAssignmentWorkflow", request)
            log.atInfo().addKeyValue("operation", "SignalDispatchAssignmentWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchAssignment.dispatchAssignmentId).log("Recorded audit detail")
            var reply0 = ValidateGeoFenceImportResponse.getDefaultInstance()
            request.dispatchAssignment.commentsList.asFlow().map { item ->
                val outbound0 = validateGeoFenceImportRequest {
                    this.context = request.context
                    this.geoFence = geoFence {
                        this.audit = auditInfo {
                            this.createdAt = item.createdAt
                            this.updatedAt = request.dispatchAssignment.audit.updatedAt
                            this.updatedBy = request.dispatchAssignment.audit.updatedBy
                        }
                        this.block = geoFenceBlockAction {
                            this.notifyEmail = item.authorEmail
                        }
                    }
                }
                telematicsGeoFenceImportService.validateGeoFenceImport(outbound0)
            }.collect { reply0 = it }
            val outbound1 = RequestFactory.create {
                countDriversRequest {
                    this.context = request.context
                    this.filter = CountDriversRequestKt.filter {
                        this.createdAfter = reply0.geoFence.audit.createdAt
                    }
                }
            }
            val reply1 = driversDriverQueryService.countDrivers(outbound1)
            var reply2 = PreviewDriverResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<DispatchAssignmentComment>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound2 = previewDriverRequest {
                            this.context = request.context
                            this.details = PreviewDriverRequestKt.details {
                                this.contact = contactInfo {
                                    this.email = item.authorEmail
                                }
                            }
                            this.driver = driver {
                                this.audit = auditInfo {
                                    this.createdAt = item.createdAt
                                    this.etag = reply0.geoFence.audit.etag
                                }
                                this.contact = contactInfo {
                                    this.email = request.details.notify.email
                                }
                            }
                        }
                        reply2 = driversDriverValidationService.previewDriver(outbound2)
                    }
                }
                try {
                    request.dispatchAssignment.commentsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound3 = createDispatchOrderRequest {
                this.context = request.context
                this.dispatchOrder = dispatchOrder {
                    this.counterparty = zoneRef {
                        this.displayName = request.dispatchAssignment.customer.displayName
                    }
                    this.lines.addAll(listOf(dispatchOrderLine {
                        this.tax = dispatchOrderLineTax {
                            this.amount = reply0.geoFence.discount.amount
                        }
                    }))
                }
            }
            val reply3 = dispatchDispatchOrderService.createDispatchOrder(WireCopies.reflective(outbound3))
            signalDispatchAssignmentWorkflowResponse {
                this.dispatchAssignment = dispatchAssignment {
                    this.audit = auditInfo {
                        this.createdBy = reply3.dispatchOrder.audit.createdBy
                    }
                    this.customFields.putAll(request.dispatchAssignment.customFieldsMap)
                    this.dispatchAssignmentId = request.dispatchAssignmentId
                    this.sla = dispatchAssignmentSla {
                        this.dueAt = reply3.dispatchOrder.dueAt
                    }
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.message = reply1.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleStartDispatchAssignmentWorkflow(request: StartDispatchAssignmentWorkflowRequest): StartDispatchAssignmentWorkflowResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentWorkflowService/StartDispatchAssignmentWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StartDispatchAssignmentWorkflow", request)
            log.atInfo().addKeyValue("operation", "StartDispatchAssignmentWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchAssignment.dispatchAssignmentId).log("Recorded audit detail")
            val outbound0 = createDocumentRequest {
                this.context = request.context
                this.document = document {
                    this.audit = auditInfo {
                        this.createdBy = request.dispatchAssignment.audit.createdBy
                        this.etag = request.dispatchAssignment.audit.etag
                        this.updatedBy = request.dispatchAssignment.audit.updatedBy
                    }
                }
            }
            val reply0 = workerForStartDispatchAssignmentWorkflow0.submit(outbound0)
            val outbound1 = lintDispatchOrdersRequest {
                this.context = request.context
                this.details = LintDispatchOrdersRequestKt.details {
                    this.contact = contactInfo {
                        this.name = request.dispatchAssignment.requester.name
                    }
                }
                this.dispatchOrder = dispatchOrder {
                    this.audit = auditInfo {
                        this.updatedAt = request.dispatchAssignment.audit.updatedAt
                    }
                    this.billingAddress = postalAddress {
                        this.countryCode = reply0.document.billingAddress.countryCode
                    }
                    this.card = dispatchOrderCardSettlement {
                        this.network = reply0.document.card.network
                    }
                }
            }
            val reply1 = clientForStartDispatchAssignmentWorkflow1.lintDispatchOrders(outbound1)
            startDispatchAssignmentWorkflowResponse {
                this.dispatchAssignment = dispatchAssignment {
                    this.audit = auditInfo {
                        this.etag = reply1.dispatchOrder.audit.etag
                        this.updatedBy = reply0.document.audit.updatedBy
                    }
                    this.customFields.putAll(request.dispatchAssignment.customFieldsMap)
                    this.dispatchAssignmentId = request.dispatchAssignmentId
                }
                this.outcome = StartDispatchAssignmentWorkflowResponseKt.outcome {
                    this.effectiveAt = reply1.outcome.effectiveAt
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }
}
