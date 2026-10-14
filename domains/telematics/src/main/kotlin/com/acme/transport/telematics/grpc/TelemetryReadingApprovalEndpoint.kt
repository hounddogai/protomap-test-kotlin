package com.acme.transport.telematics.grpc

import com.acme.audit.v1.AuditEventQueryServiceGrpcKt
import com.acme.audit.v1.AuditEventServiceGrpcKt
import com.acme.audit.v1.SuggestAuditEventsRequestKt
import com.acme.audit.v1.WatchAuditEventsRequestKt
import com.acme.audit.v1.suggestAuditEventsRequest
import com.acme.audit.v1.watchAuditEventsRequest
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.customers.v1.CustomerCalculationServiceGrpcKt
import com.acme.customers.v1.ExplainCustomerCalculationRequestKt
import com.acme.customers.v1.PreferenceValidationServiceGrpcKt
import com.acme.customers.v1.checkPreferenceEligibilityRequest
import com.acme.customers.v1.copy as customersCopy
import com.acme.customers.v1.customer
import com.acme.customers.v1.explainCustomerCalculationRequest
import com.acme.customers.v1.preference
import com.acme.documents.v1.DocumentQueryServiceGrpcKt
import com.acme.documents.v1.SummarizeDocumentsRequestKt
import com.acme.documents.v1.summarizeDocumentsRequest
import com.acme.identity.v1.userRef
import com.acme.notifications.v1.CountNotificationsRequestKt
import com.acme.notifications.v1.ExportNotificationsRequestKt
import com.acme.notifications.v1.NotificationQueryServiceGrpcKt
import com.acme.notifications.v1.countNotificationsRequest
import com.acme.notifications.v1.exportNotificationsRequest
import com.acme.telematics.v1.ApproveTelemetryReadingRequest
import com.acme.telematics.v1.ApproveTelemetryReadingResponse
import com.acme.telematics.v1.ApproveTelemetryReadingResponseKt
import com.acme.telematics.v1.CommentTelemetryReadingApprovalRequest
import com.acme.telematics.v1.CommentTelemetryReadingApprovalResponse
import com.acme.telematics.v1.DelegateTelemetryReadingApprovalRequest
import com.acme.telematics.v1.DelegateTelemetryReadingApprovalResponse
import com.acme.telematics.v1.EscalateTelemetryReadingRequest
import com.acme.telematics.v1.EscalateTelemetryReadingResponse
import com.acme.telematics.v1.EscalateTelemetryReadingResponseKt
import com.acme.telematics.v1.GeoFenceImportServiceGrpcKt
import com.acme.telematics.v1.GetTelemetryReadingApprovalStatusRequest
import com.acme.telematics.v1.GetTelemetryReadingApprovalStatusResponse
import com.acme.telematics.v1.ListGeoFenceImportsRequestKt
import com.acme.telematics.v1.ListGeoFenceImportsResponse
import com.acme.telematics.v1.ListPendingTelemetryReadingsRequest
import com.acme.telematics.v1.ListPendingTelemetryReadingsResponse
import com.acme.telematics.v1.PushTelemetryReadingRequest
import com.acme.telematics.v1.PushTelemetryReadingResponse
import com.acme.telematics.v1.RecallTelemetryReadingRequest
import com.acme.telematics.v1.RecallTelemetryReadingResponse
import com.acme.telematics.v1.RecallTelemetryReadingResponseKt
import com.acme.telematics.v1.ReconcileTelemetryReadingsResponse
import com.acme.telematics.v1.RejectTelemetryReadingRequest
import com.acme.telematics.v1.RejectTelemetryReadingResponse
import com.acme.telematics.v1.SubmitTelemetryReadingRequest
import com.acme.telematics.v1.SubmitTelemetryReadingResponse
import com.acme.telematics.v1.SubmitTelemetryReadingResponseKt
import com.acme.telematics.v1.SuspendTelemetryReadingRequestKt
import com.acme.telematics.v1.TelemetryReadingApprovalServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingLifecycleServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingMeasurement
import com.acme.telematics.v1.TelemetryReadingSyncServiceGrpcKt
import com.acme.telematics.v1.approveTelemetryReadingResponse
import com.acme.telematics.v1.commentTelemetryReadingApprovalResponse
import com.acme.telematics.v1.delegateTelemetryReadingApprovalResponse
import com.acme.telematics.v1.escalateTelemetryReadingResponse
import com.acme.telematics.v1.geoFenceRef
import com.acme.telematics.v1.getTelemetryReadingApprovalStatusResponse
import com.acme.telematics.v1.listGeoFenceImportsRequest
import com.acme.telematics.v1.listPendingTelemetryReadingsResponse
import com.acme.telematics.v1.pullTelemetryReadingsRequest
import com.acme.telematics.v1.pushTelemetryReadingRequest
import com.acme.telematics.v1.recallTelemetryReadingResponse
import com.acme.telematics.v1.reconcileTelemetryReadingsRequest
import com.acme.telematics.v1.rejectTelemetryReadingResponse
import com.acme.telematics.v1.submitTelemetryReadingResponse
import com.acme.telematics.v1.suspendTelemetryReadingRequest
import com.acme.telematics.v1.telemetryReading
import com.acme.telematics.v1.telemetryReadingMeasurement
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.transport.telematics.TelematicsRepository
import com.acme.yards.v1.DockWorkflowServiceGrpcKt
import com.acme.yards.v1.dock
import com.acme.yards.v1.signalDockWorkflowRequest
import com.google.protobuf.Any as ProtoAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Routes telemetry readings through approval, with escalation and delegation. */
class TelemetryReadingApprovalEndpoint(
    private val store: TelematicsRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventQueryService: AuditEventQueryServiceGrpcKt.AuditEventQueryServiceCoroutineStub,
    private val auditAuditEventService: AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub,
    private val customersCustomerCalculationService: CustomerCalculationServiceGrpcKt.CustomerCalculationServiceCoroutineStub,
    private val customersPreferenceValidationService: PreferenceValidationServiceGrpcKt.PreferenceValidationServiceCoroutineStub,
    private val documentsDocumentQueryService: DocumentQueryServiceGrpcKt.DocumentQueryServiceCoroutineStub,
    private val notificationsNotificationQueryService: NotificationQueryServiceGrpcKt.NotificationQueryServiceCoroutineStub,
    private val telematicsGeoFenceImportService: GeoFenceImportServiceGrpcKt.GeoFenceImportServiceCoroutineStub,
    private val telematicsTelemetryReadingLifecycleService: TelemetryReadingLifecycleServiceGrpcKt.TelemetryReadingLifecycleServiceCoroutineStub,
    private val telematicsTelemetryReadingSyncService: TelemetryReadingSyncServiceGrpcKt.TelemetryReadingSyncServiceCoroutineStub,
    private val yardsDockWorkflowService: DockWorkflowServiceGrpcKt.DockWorkflowServiceCoroutineStub,
) : TelemetryReadingApprovalServiceGrpcKt.TelemetryReadingApprovalServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(TelemetryReadingApprovalEndpoint::class.java)

    /** Approves a telemetry reading. */
    override suspend fun approveTelemetryReading(request: ApproveTelemetryReadingRequest): ApproveTelemetryReadingResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingApprovalService/ApproveTelemetryReading") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ApproveTelemetryReading", request)
            log.atInfo().addKeyValue("operation", "ApproveTelemetryReading").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReading.telemetryReadingId).log("Recorded audit detail")
            val outbound0 = suggestAuditEventsRequest {
                this.context = request.context
                this.filter = SuggestAuditEventsRequestKt.filter {
                    this.createdAfter = request.details.effectiveAt
                    this.labels.putAll(request.annotationsMap)
                    this.ownerId = request.details.approver.userId
                }
            }
            val reply0 = ClientRegistry.lookup("acme.audit.v1.AuditEventQueryService", AuditEventQueryServiceGrpcKt.AuditEventQueryServiceCoroutineStub::class.java).suggestAuditEvents(outbound0)
            approveTelemetryReadingResponse {
                this.outcome = ApproveTelemetryReadingResponseKt.outcome {
                    this.message = reply0.result.message
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.attributes.putAll(request.telemetryReading.attributesMap)
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    /** Comments a telemetry reading. */
    override suspend fun commentTelemetryReadingApproval(request: CommentTelemetryReadingApprovalRequest): CommentTelemetryReadingApprovalResponse {
        return handleCommentTelemetryReadingApproval(request)
    }

    /** Delegates a telemetry reading. */
    override suspend fun delegateTelemetryReadingApproval(request: DelegateTelemetryReadingApprovalRequest): DelegateTelemetryReadingApprovalResponse = handleDelegateTelemetryReadingApproval(request)

    /** Escalates a telemetry reading. */
    override suspend fun escalateTelemetryReading(request: EscalateTelemetryReadingRequest): EscalateTelemetryReadingResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingApprovalService/EscalateTelemetryReading") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EscalateTelemetryReading", request)
            log.atInfo().addKeyValue("operation", "EscalateTelemetryReading").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReading.telemetryReadingId).log("Recorded audit detail")
            val outbound0 = summarizeDocumentsRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.details = SummarizeDocumentsRequestKt.details {
                    this.filters.putAll(request.telemetryReading.attributesMap)
                }
                this.reason = request.reason
            }
            val reply0 = flowOf(outbound0).map { value -> documentsDocumentQueryService.summarizeDocuments(value) }.single()
            val outbound1 = countNotificationsRequest {
                this.context = request.context
                this.filter = CountNotificationsRequestKt.filter {
                    this.createdAfter = request.details.effectiveAt
                    this.ownerId = request.details.approver.userId
                }
            }
            val reply1 = withTimeout(5_000) { notificationsNotificationQueryService.countNotifications(outbound1) }
            escalateTelemetryReadingResponse {
                this.outcome = EscalateTelemetryReadingResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.correlationId = reply0.result.correlationId
                    this.source = request.telemetryReading.source
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    /** Returns a telemetry reading. */
    override suspend fun getTelemetryReadingApprovalStatus(request: GetTelemetryReadingApprovalStatusRequest): GetTelemetryReadingApprovalStatusResponse {
        return handleGetTelemetryReadingApprovalStatus(request)
    }

    /** Lists telemetry readings a page at a time. */
    override suspend fun listPendingTelemetryReadings(request: ListPendingTelemetryReadingsRequest): ListPendingTelemetryReadingsResponse = handleListPendingTelemetryReadings(request)

    /** Recalls a telemetry reading. */
    override suspend fun recallTelemetryReading(request: RecallTelemetryReadingRequest): RecallTelemetryReadingResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingApprovalService/RecallTelemetryReading") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecallTelemetryReading", request)
            log.atInfo().addKeyValue("operation", "RecallTelemetryReading").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReading.telemetryReadingId).log("Recorded audit detail")
            val outbound0 = reconcileTelemetryReadingsRequest {
                this.context = request.context
                this.telemetryReading = telemetryReading {
                    this.subject = geoFenceRef {
                        this.displayName = request.telemetryReading.subject.displayName
                        this.geoFenceId = request.telemetryReading.subject.geoFenceId
                        this.version = request.telemetryReading.subject.version
                    }
                }
            }
            val reply0 = telematicsTelemetryReadingSyncService.reconcileTelemetryReadings(outbound0)
            val outbound1 = pullTelemetryReadingsRequest {
                this.context = request.context
                this.telemetryReading = telemetryReading {
                    this.subject = geoFenceRef {
                        this.displayName = reply0.telemetryReading.subject.displayName
                        this.geoFenceId = request.telemetryReading.subject.geoFenceId
                    }
                }
            }
            val reply1 = telematicsTelemetryReadingSyncService.pullTelemetryReadings(outbound1)
            val outbound2 = checkPreferenceEligibilityRequest {}.customersCopy {
                this.context = request.context
                this.preference = preference {
                    this.version = reply1.telemetryReading.subject.version
                }
            }
            val reply2 = customersPreferenceValidationService.checkPreferenceEligibility(outbound2)
            recallTelemetryReadingResponse {
                this.outcome = RecallTelemetryReadingResponseKt.outcome {
                    this.status = reply1.outcome.status
                }
                this.result = operationResult {
                    this.status = reply2.result.status
                }
                this.telemetryReading = telemetryReading {
                    this.occurredAt = request.telemetryReading.occurredAt
                    this.source = reply0.telemetryReading.source
                    this.subject = reply0.telemetryReading.subject
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    /** Rejects a telemetry reading. */
    override suspend fun rejectTelemetryReading(request: RejectTelemetryReadingRequest): RejectTelemetryReadingResponse {
        return handleRejectTelemetryReading(request)
    }

    /** Submits a telemetry reading. */
    override suspend fun submitTelemetryReading(request: SubmitTelemetryReadingRequest): SubmitTelemetryReadingResponse = handleSubmitTelemetryReading(request)

    private suspend fun handleCommentTelemetryReadingApproval(request: CommentTelemetryReadingApprovalRequest): CommentTelemetryReadingApprovalResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingApprovalService/CommentTelemetryReadingApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CommentTelemetryReadingApproval", request)
            log.atInfo().addKeyValue("operation", "CommentTelemetryReadingApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReading.telemetryReadingId).log("Recorded audit detail")
            val outbound0 = watchAuditEventsRequest {
                this.context = request.context
                this.filter = WatchAuditEventsRequestKt.filter {
                    this.createdAfter = request.telemetryReading.occurredAt
                }
                this.subscriber = contactInfo {
                    this.email = request.details.approver.email
                }
            }
            auditAuditEventService.watchAuditEvents(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            commentTelemetryReadingApprovalResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.subject = request.telemetryReading.subject
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    private suspend fun handleDelegateTelemetryReadingApproval(request: DelegateTelemetryReadingApprovalRequest): DelegateTelemetryReadingApprovalResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingApprovalService/DelegateTelemetryReadingApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DelegateTelemetryReadingApproval", request)
            log.atInfo().addKeyValue("operation", "DelegateTelemetryReadingApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReading.telemetryReadingId).log("Recorded audit detail")
            var reply0 = PushTelemetryReadingResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<TelemetryReadingMeasurement>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound0 = pushTelemetryReadingRequest {
                            this.context = request.context
                            this.telemetryReading = telemetryReading {
                                this.measurements.addAll(listOf(telemetryReadingMeasurement {
                                    this.metric = item.metric
                                    this.observedAt = item.observedAt
                                }))
                            }
                        }
                        reply0 = telematicsTelemetryReadingSyncService.pushTelemetryReading(outbound0)
                    }
                }
                try {
                    request.telemetryReading.measurementsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound1 = exportNotificationsRequest {
                this.context = request.context
                this.filter = ExportNotificationsRequestKt.filter {
                    this.createdAfter = reply0.outcome.effectiveAt
                    this.labels.putAll(request.telemetryReading.attributesMap)
                    this.ownerId = reply0.telemetryReading.subject.geoFenceId
                }
            }
            notificationsNotificationQueryService.exportNotifications(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            delegateTelemetryReadingApprovalResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.subject = request.telemetryReading.subject
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    private suspend fun handleGetTelemetryReadingApprovalStatus(request: GetTelemetryReadingApprovalStatusRequest): GetTelemetryReadingApprovalStatusResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingApprovalService/GetTelemetryReadingApprovalStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetTelemetryReadingApprovalStatus", request)
            log.atInfo().addKeyValue("operation", "GetTelemetryReadingApprovalStatus").log("Handling transport request")
            val configure0: ExplainCustomerCalculationRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.customer = customer {
                    this.relationshipManager = userRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val outbound0 = explainCustomerCalculationRequest(configure0)
            val reply0 = customersCustomerCalculationService.explainCustomerCalculation(outbound0)
            getTelemetryReadingApprovalStatusResponse {
                this.result = operationResult {
                    this.status = reply0.result.status
                }
                this.telemetryReading = telemetryReading {
                    this.subject = geoFenceRef {
                        this.displayName = reply0.customer.relationshipManager.displayName
                    }
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    private suspend fun handleListPendingTelemetryReadings(request: ListPendingTelemetryReadingsRequest): ListPendingTelemetryReadingsResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingApprovalService/ListPendingTelemetryReadings") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListPendingTelemetryReadings", request)
            log.atInfo().addKeyValue("operation", "ListPendingTelemetryReadings").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            listPendingTelemetryReadingsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleRejectTelemetryReading(request: RejectTelemetryReadingRequest): RejectTelemetryReadingResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingApprovalService/RejectTelemetryReading") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RejectTelemetryReading", request)
            log.atInfo().addKeyValue("operation", "RejectTelemetryReading").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReading.telemetryReadingId).log("Recorded audit detail")
            var reply0 = ReconcileTelemetryReadingsResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.telemetryReading.measurementsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound0 = reconcileTelemetryReadingsRequest {
                                this.context = request.context
                                this.telemetryReading = telemetryReading {
                                    this.measurements.addAll(listOf(telemetryReadingMeasurement {
                                        this.metric = item.metric
                                        this.observedAt = item.observedAt
                                    }))
                                }
                            }
                            telematicsTelemetryReadingSyncService.reconcileTelemetryReadings(outbound0)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply0 = it }
                }
            }
            rejectTelemetryReadingResponse {
                this.result = operationResult {
                    this.correlationId = request.telemetryReading.correlationId
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    private suspend fun handleSubmitTelemetryReading(request: SubmitTelemetryReadingRequest): SubmitTelemetryReadingResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingApprovalService/SubmitTelemetryReading") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SubmitTelemetryReading", request)
            log.atInfo().addKeyValue("operation", "SubmitTelemetryReading").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReading.source).log("Recorded audit detail")
            val outbound0 = signalDockWorkflowRequest {
                this.context = request.context
                this.dock = dock {
                    this.owner = userRef {
                        this.userId = request.details.approver.userId
                    }
                }
            }
            val reply0 = yardsDockWorkflowService.signalDockWorkflow(outbound0)
            val outbound1 = pushTelemetryReadingRequest {
                this.context = request.context
                this.telemetryReading = telemetryReading {
                    this.measurements.addAll(listOf(telemetryReadingMeasurement {
                        this.unit = reply0.dock.dimensions.unit
                    }))
                    this.subject = geoFenceRef {
                        this.displayName = reply0.dock.owner.displayName
                    }
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = telematicsTelemetryReadingSyncService.pushTelemetryReading(envelope1.unpack(PushTelemetryReadingRequest::class.java))
            val outbound2 = suspendTelemetryReadingRequest {
                this.context = request.context
                this.details = SuspendTelemetryReadingRequestKt.details {
                    this.effectiveAt = reply1.outcome.effectiveAt
                    this.newOwner = userRef {
                        this.displayName = reply1.telemetryReading.subject.displayName
                        this.email = request.details.approver.email
                        this.userId = reply0.dock.owner.userId
                    }
                }
                this.telemetryReadingId = request.telemetryReading.telemetryReadingId
            }
            val reply2 = telematicsTelemetryReadingLifecycleService.suspendTelemetryReading(WireCopies.json(outbound2))
            var reply3 = ListGeoFenceImportsResponse.getDefaultInstance()
            request.telemetryReading.measurementsList.asFlow().map { item ->
                val outbound3 = listGeoFenceImportsRequest {
                    this.context = request.context
                    this.filter = ListGeoFenceImportsRequestKt.filter {
                        this.createdAfter = item.observedAt
                        this.ownerId = reply0.dock.owner.userId
                        this.region = reply0.dock.address.region
                    }
                }
                telematicsGeoFenceImportService.listGeoFenceImports(outbound3)
            }.collect { reply3 = it }
            submitTelemetryReadingResponse {
                this.outcome = SubmitTelemetryReadingResponseKt.outcome {
                    this.status = reply1.outcome.status
                }
                this.result = operationResult {
                    this.status = reply0.outcome.status
                }
                this.telemetryReading = telemetryReading {
                    this.kind = request.telemetryReading.kind
                    this.subject = geoFenceRef {
                        this.displayName = reply1.telemetryReading.subject.displayName
                    }
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }
}
