package com.acme.transport.driverapp.adapters

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.ConsentRecordApprovalServiceGrpcKt
import com.acme.customers.v1.EscalateConsentRecordRequestKt
import com.acme.customers.v1.HouseholdApprovalServiceGrpcKt
import com.acme.customers.v1.commentHouseholdApprovalRequest
import com.acme.customers.v1.consentRecord
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.escalateConsentRecordRequest
import com.acme.customers.v1.household
import com.acme.customers.v1.householdRef
import com.acme.dispatch.v1.DispatchOrderApprovalServiceGrpcKt
import com.acme.dispatch.v1.EscalateDispatchOrderRequest
import com.acme.dispatch.v1.EscalateDispatchOrderRequestKt
import com.acme.dispatch.v1.dispatchOrder
import com.acme.dispatch.v1.escalateDispatchOrderRequest
import com.acme.driverapp.v1.AdvanceDriverInboxWorkflowRequest
import com.acme.driverapp.v1.AdvanceDriverInboxWorkflowResponse
import com.acme.driverapp.v1.AdvanceDriverInboxWorkflowResponseKt
import com.acme.driverapp.v1.CancelDriverInboxWorkflowRequest
import com.acme.driverapp.v1.CancelDriverInboxWorkflowResponse
import com.acme.driverapp.v1.CommentDriverInboxApprovalRequestKt
import com.acme.driverapp.v1.CountDriverInboxTotalsRequestKt
import com.acme.driverapp.v1.CountDriverInboxTotalsResponse
import com.acme.driverapp.v1.DelegateDriverHomeApprovalRequest
import com.acme.driverapp.v1.DelegateDriverHomeApprovalRequestKt
import com.acme.driverapp.v1.DelegateDriverInboxApprovalRequestKt
import com.acme.driverapp.v1.DeleteDriverInboxReportRequest
import com.acme.driverapp.v1.DriverHomeApprovalServiceGrpcKt
import com.acme.driverapp.v1.DriverHomeWorkflowServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxApprovalServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxReportingServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxWorkflowServiceGrpcKt
import com.acme.driverapp.v1.EscalateDriverInboxResponse
import com.acme.driverapp.v1.InspectDriverInboxWorkflowRequest
import com.acme.driverapp.v1.InspectDriverInboxWorkflowResponse
import com.acme.driverapp.v1.ListDriverInboxReportsRequestKt
import com.acme.driverapp.v1.ListDriverInboxWorkflowsRequest
import com.acme.driverapp.v1.ListDriverInboxWorkflowsResponse
import com.acme.driverapp.v1.ListPendingDriverInboxesRequestKt
import com.acme.driverapp.v1.PauseDriverHomeWorkflowRequest
import com.acme.driverapp.v1.ResumeDriverInboxWorkflowRequest
import com.acme.driverapp.v1.ResumeDriverInboxWorkflowResponse
import com.acme.driverapp.v1.RetryDriverInboxWorkflowRequest
import com.acme.driverapp.v1.RetryDriverInboxWorkflowResponse
import com.acme.driverapp.v1.ScheduleDriverInboxReportRequest
import com.acme.driverapp.v1.ScheduleDriverInboxReportRequestKt
import com.acme.driverapp.v1.ScheduleDriverInboxReportResponse
import com.acme.driverapp.v1.SignalDriverInboxWorkflowRequest
import com.acme.driverapp.v1.SignalDriverInboxWorkflowResponse
import com.acme.driverapp.v1.SignalDriverInboxWorkflowResponseKt
import com.acme.driverapp.v1.StartDriverInboxWorkflowRequest
import com.acme.driverapp.v1.StartDriverInboxWorkflowResponse
import com.acme.driverapp.v1.StartDriverInboxWorkflowResponseKt
import com.acme.driverapp.v1.advanceDriverInboxWorkflowResponse
import com.acme.driverapp.v1.cancelDriverInboxWorkflowResponse
import com.acme.driverapp.v1.commentDriverInboxApprovalRequest
import com.acme.driverapp.v1.copy as driverappCopy
import com.acme.driverapp.v1.countDriverInboxTotalsRequest
import com.acme.driverapp.v1.delegateDriverHomeApprovalRequest
import com.acme.driverapp.v1.delegateDriverInboxApprovalRequest
import com.acme.driverapp.v1.deleteDriverInboxReportRequest
import com.acme.driverapp.v1.driverHome
import com.acme.driverapp.v1.driverHomeComment
import com.acme.driverapp.v1.driverHomeSla
import com.acme.driverapp.v1.driverInbox
import com.acme.driverapp.v1.driverInboxLine
import com.acme.driverapp.v1.driverInboxLineTax
import com.acme.driverapp.v1.escalateDriverInboxRequest
import com.acme.driverapp.v1.generateDriverInboxReportRequest
import com.acme.driverapp.v1.inspectDriverInboxWorkflowResponse
import com.acme.driverapp.v1.listDriverInboxReportsRequest
import com.acme.driverapp.v1.listDriverInboxWorkflowsResponse
import com.acme.driverapp.v1.listPendingDriverInboxesRequest
import com.acme.driverapp.v1.pauseDriverHomeWorkflowRequest
import com.acme.driverapp.v1.resumeDriverInboxWorkflowResponse
import com.acme.driverapp.v1.retryDriverInboxWorkflowResponse
import com.acme.driverapp.v1.scheduleDriverInboxReportRequest
import com.acme.driverapp.v1.signalDriverInboxWorkflowResponse
import com.acme.driverapp.v1.startDriverInboxWorkflowResponse
import com.acme.featureflags.v1.ExportFeatureFlagsRequestKt
import com.acme.featureflags.v1.FeatureFlagQueryServiceGrpcKt
import com.acme.featureflags.v1.FeatureFlagValidationServiceGrpcKt
import com.acme.featureflags.v1.LintFeatureFlagsRequest
import com.acme.featureflags.v1.LintFeatureFlagsResponse
import com.acme.featureflags.v1.exportFeatureFlagsRequest
import com.acme.featureflags.v1.featureFlag
import com.acme.featureflags.v1.lintFeatureFlagsRequest
import com.acme.identity.v1.userRef
import com.acme.metrics.v1.ListMetricSeriesesRequest
import com.acme.metrics.v1.ListMetricSeriesesRequestKt
import com.acme.metrics.v1.MetricSeriesServiceGrpcKt
import com.acme.metrics.v1.listMetricSeriesesRequest
import com.acme.notifications.v1.NotificationValidationServiceGrpcKt
import com.acme.notifications.v1.PreviewNotificationRequest
import com.acme.notifications.v1.PreviewNotificationResponse
import com.acme.notifications.v1.notification
import com.acme.notifications.v1.notificationComment
import com.acme.notifications.v1.notificationTemplateRef
import com.acme.notifications.v1.previewNotificationRequest
import com.acme.routing.v1.ListPendingStopsRequestKt
import com.acme.routing.v1.StopApprovalServiceGrpcKt
import com.acme.routing.v1.listPendingStopsRequest
import com.acme.telematics.v1.TripLogImportServiceGrpcKt
import com.acme.telematics.v1.tripLog
import com.acme.telematics.v1.tripLogCardSettlement
import com.acme.telematics.v1.validateTripLogImportRequest
import com.acme.transport.driverapp.DriverappRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.google.protobuf.Any as ProtoAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Runs the long-lived workflow that takes a driver inbox from start to finish. */
class DriverInboxWorkflowController(
    private val store: DriverappRepository,
    private val workers: CoroutineScope,
    private val customersConsentRecordApprovalService: ConsentRecordApprovalServiceGrpcKt.ConsentRecordApprovalServiceCoroutineStub,
    private val customersHouseholdApprovalService: HouseholdApprovalServiceGrpcKt.HouseholdApprovalServiceCoroutineStub,
    private val dispatchDispatchOrderApprovalService: DispatchOrderApprovalServiceGrpcKt.DispatchOrderApprovalServiceCoroutineStub,
    private val driverappDriverHomeApprovalService: DriverHomeApprovalServiceGrpcKt.DriverHomeApprovalServiceCoroutineStub,
    private val driverappDriverHomeWorkflowService: DriverHomeWorkflowServiceGrpcKt.DriverHomeWorkflowServiceCoroutineStub,
    private val driverappDriverInboxApprovalService: DriverInboxApprovalServiceGrpcKt.DriverInboxApprovalServiceCoroutineStub,
    private val driverappDriverInboxReportingService: DriverInboxReportingServiceGrpcKt.DriverInboxReportingServiceCoroutineStub,
    private val featureflagsFeatureFlagQueryService: FeatureFlagQueryServiceGrpcKt.FeatureFlagQueryServiceCoroutineStub,
    private val featureflagsFeatureFlagValidationService: FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub,
    private val metricsMetricSeriesService: MetricSeriesServiceGrpcKt.MetricSeriesServiceCoroutineStub,
    private val notificationsNotificationValidationService: NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub,
    private val routingStopApprovalService: StopApprovalServiceGrpcKt.StopApprovalServiceCoroutineStub,
    private val telematicsTripLogImportService: TripLogImportServiceGrpcKt.TripLogImportServiceCoroutineStub,
) : DriverInboxWorkflowServiceGrpcKt.DriverInboxWorkflowServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DriverInboxWorkflowController::class.java)

    private val clientForAdvanceDriverInboxWorkflow0 by lazy { telematicsTripLogImportService }

    private val invokeRetryDriverInboxWorkflow0: suspend (LintFeatureFlagsRequest) -> LintFeatureFlagsResponse = { payload ->
        featureflagsFeatureFlagValidationService.lintFeatureFlags(payload)
    }

    private val workerForStartDriverInboxWorkflow0 = ReplyMailbox<PreviewNotificationRequest, PreviewNotificationResponse>(workers) { payload ->
        withTimeout(5_000) { notificationsNotificationValidationService.previewNotification(payload) }
    }

    private val clientForStartDriverInboxWorkflow1 by lazy { driverappDriverInboxReportingService }

    /** Advances a driver inbox. */
    override suspend fun advanceDriverInboxWorkflow(request: AdvanceDriverInboxWorkflowRequest): AdvanceDriverInboxWorkflowResponse = handleAdvanceDriverInboxWorkflow(request)

    /** Cancels a driver inbox. */
    override suspend fun cancelDriverInboxWorkflow(request: CancelDriverInboxWorkflowRequest): CancelDriverInboxWorkflowResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxWorkflowService/CancelDriverInboxWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CancelDriverInboxWorkflow", request)
            log.atInfo().addKeyValue("operation", "CancelDriverInboxWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverInbox.driverInboxId).log("Recorded audit detail")
            val outbound0 = delegateDriverHomeApprovalRequest {
                this.context = request.context
                this.driverHome = driverHome {
                    this.customer = customerRef {
                        this.email = request.details.notify.email
                    }
                    this.requester = contactInfo {
                        this.phone = request.details.notify.phone
                    }
                }
            }
            val reply0 = driverappDriverHomeApprovalService.delegateDriverHomeApproval(outbound0)
            var reply1 = CountDriverInboxTotalsResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.driverInbox.linesList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound1 = countDriverInboxTotalsRequest {
                                this.context = request.context
                                this.filter = CountDriverInboxTotalsRequestKt.filter {
                                    this.labels.putAll(item.attributesMap)
                                    this.region = request.driverInbox.billingAddress.region
                                }
                            }
                            driverappDriverInboxReportingService.countDriverInboxTotals(outbound1)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply1 = it }
                }
            }
            val outbound2 = commentDriverInboxApprovalRequest {}.driverappCopy {
                this.context = request.context
                this.details = CommentDriverInboxApprovalRequestKt.details {
                    this.approver = userRef {
                        this.displayName = reply0.driverHome.assignee.displayName
                        this.email = reply0.driverHome.customer.email
                    }
                }
                this.driverInbox = driverInbox {
                    this.billingAddress = postalAddress {
                        this.postalCode = request.driverInbox.billingAddress.postalCode
                    }
                }
            }
            val reply2 = driverappDriverInboxApprovalService.commentDriverInboxApproval(outbound2)
            cancelDriverInboxWorkflowResponse {
                this.driverInbox = driverInbox {
                    this.billingAddress = postalAddress {
                        this.recipient = reply2.driverInbox.billingAddress.recipient
                    }
                    this.counterparty = customerRef {
                        this.email = reply0.driverHome.assignee.email
                    }
                    this.driverInboxId = request.driverInboxId
                    this.metadata.putAll(request.driverInbox.metadataMap)
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Returns a driver inbox. */
    override suspend fun inspectDriverInboxWorkflow(request: InspectDriverInboxWorkflowRequest): InspectDriverInboxWorkflowResponse {
        return handleInspectDriverInboxWorkflow(request)
    }

    /** Lists driver inboxes a page at a time. */
    override suspend fun listDriverInboxWorkflows(request: ListDriverInboxWorkflowsRequest): ListDriverInboxWorkflowsResponse = handleListDriverInboxWorkflows(request)

    /** Resumes a driver inbox. */
    override suspend fun resumeDriverInboxWorkflow(request: ResumeDriverInboxWorkflowRequest): ResumeDriverInboxWorkflowResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxWorkflowService/ResumeDriverInboxWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResumeDriverInboxWorkflow", request)
            log.atInfo().addKeyValue("operation", "ResumeDriverInboxWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverInbox.driverInboxId).log("Recorded audit detail")
            val outbound0 = escalateConsentRecordRequest {
                this.consentRecord = consentRecord {
                    this.subject = householdRef {
                        this.displayName = request.driverInbox.counterparty.displayName
                    }
                }
                this.context = request.context
                this.details = EscalateConsentRecordRequestKt.details {
                    this.approver = userRef {
                        this.email = request.driverInbox.counterparty.email
                    }
                    this.approverEmail = request.details.notify.email
                }
            }
            val reply0 = customersConsentRecordApprovalService.escalateConsentRecord(WireCopies.json(outbound0))
            val outbound1 = ResumeDriverInboxWorkflow1Command(
                context = request.context,
                filterRegion = request.driverInbox.billingAddress.region
            ).toRequest()
            val reply1 = metricsMetricSeriesService.listMetricSerieses(outbound1)
            val outbound2 = deleteDriverInboxReportRequest {
                this.context = request.context
                this.driverInboxId = request.driverInboxId
                this.etag = request.driverInbox.audit.etag
                this.reason = request.reason
            }
            val command2: Submission<DeleteDriverInboxReportRequest> = Submission.Live(outbound2)
            val payload2 = when (val command = command2) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply2 = driverappDriverInboxReportingService.deleteDriverInboxReport(payload2)
            val outbound3 = scheduleDriverInboxReportRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.details = ScheduleDriverInboxReportRequestKt.details {
                    this.filters.putAll(reply0.consentRecord.attributesMap)
                }
                this.driverInboxId = request.driverInboxId
                this.reason = request.reason
            }
            val adapter3 = OutboundPort<ScheduleDriverInboxReportRequest, ScheduleDriverInboxReportResponse> { payload ->
                driverappDriverInboxReportingService.scheduleDriverInboxReport(payload)
            }
            val port3 = DelegatingPort(adapter3)
            val reply3 = port3.execute(outbound3)
            resumeDriverInboxWorkflowResponse {
                this.driverInbox = driverInbox {
                    this.driverInboxId = request.driverInboxId
                    this.notes = request.driverInbox.notes
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply2.result.message
                    this.status = reply1.result.status
                }
            }
        }

    /** Retrys a driver inbox. */
    override suspend fun retryDriverInboxWorkflow(request: RetryDriverInboxWorkflowRequest): RetryDriverInboxWorkflowResponse {
        return handleRetryDriverInboxWorkflow(request)
    }

    /** Signals a driver inbox. */
    override suspend fun signalDriverInboxWorkflow(request: SignalDriverInboxWorkflowRequest): SignalDriverInboxWorkflowResponse = handleSignalDriverInboxWorkflow(request)

    /** Starts a driver inbox. */
    override suspend fun startDriverInboxWorkflow(request: StartDriverInboxWorkflowRequest): StartDriverInboxWorkflowResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxWorkflowService/StartDriverInboxWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StartDriverInboxWorkflow", request)
            log.atInfo().addKeyValue("operation", "StartDriverInboxWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverInbox.counterparty.customerId).log("Recorded audit detail")
            val outbound0 = previewNotificationRequest {
                this.context = request.context
                this.notification = notification {
                    this.comments.addAll(listOf(notificationComment {
                        this.createdAt = request.driverInbox.audit.createdAt
                    }))
                    this.customer = notificationTemplateRef {
                        this.number = request.driverInbox.number
                    }
                    this.requester = contactInfo {
                        this.email = request.driverInbox.counterparty.email
                        this.preferredChannel = request.details.notify.preferredChannel
                    }
                }
            }
            val reply0 = workerForStartDriverInboxWorkflow0.submit(outbound0)
            val outbound1 = generateDriverInboxReportRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.driverInboxId = request.driverInbox.driverInboxId
                this.reason = request.reason
            }
            val reply1 = clientForStartDriverInboxWorkflow1.generateDriverInboxReport(outbound1)
            startDriverInboxWorkflowResponse {
                this.driverInbox = driverInbox {
                    this.driverInboxId = request.driverInboxId
                    this.number = reply0.notification.customer.number
                    this.total = request.driverInbox.total
                }
                this.outcome = StartDriverInboxWorkflowResponseKt.outcome {
                    this.nextSteps.addAll(reply0.outcome.nextStepsList)
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleAdvanceDriverInboxWorkflow(request: AdvanceDriverInboxWorkflowRequest): AdvanceDriverInboxWorkflowResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxWorkflowService/AdvanceDriverInboxWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "AdvanceDriverInboxWorkflow", request)
            log.atInfo().addKeyValue("operation", "AdvanceDriverInboxWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverInbox.driverInboxId).log("Recorded audit detail")
            val outbound0 = validateTripLogImportRequest {
                this.context = request.context
                this.tripLog = tripLog {
                    this.billingAddress = postalAddress {
                        this.recipient = request.driverInbox.billingAddress.recipient
                    }
                    this.card = tripLogCardSettlement {
                        this.last4 = request.driverInbox.card.last4
                    }
                }
            }
            val reply0 = clientForAdvanceDriverInboxWorkflow0.validateTripLogImport(outbound0)
            val outbound1 = exportFeatureFlagsRequest {
                this.context = request.context
                this.filter = ExportFeatureFlagsRequestKt.filter {
                    this.createdAfter = reply0.tripLog.dueAt
                    this.ownerId = reply0.outcome.referenceId
                    this.region = reply0.tripLog.billingAddress.region
                }
            }
            featureflagsFeatureFlagQueryService.exportFeatureFlags(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound2 = delegateDriverHomeApprovalRequest {
                this.context = request.context
                this.details = DelegateDriverHomeApprovalRequestKt.details {
                    this.approver = userRef {
                        this.email = request.details.notify.email
                    }
                }
                this.driverHome = driverHome {
                    this.requester = contactInfo {
                        this.phone = request.details.notify.phone
                    }
                }
            }
            val reply2 = withTimeout(5_000) { driverappDriverHomeApprovalService.delegateDriverHomeApproval(outbound2) }
            val configure3: ListPendingDriverInboxesRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.filter = ListPendingDriverInboxesRequestKt.filter {
                    this.createdAfter = reply0.tripLog.audit.updatedAt
                    this.labels.putAll(request.driverInbox.metadataMap)
                    this.ownerId = reply0.tripLog.counterparty.customerId
                    this.region = request.driverInbox.billingAddress.region
                }
            }
            val outbound3 = listPendingDriverInboxesRequest(configure3)
            val reply3 = driverappDriverInboxApprovalService.listPendingDriverInboxes(outbound3)
            advanceDriverInboxWorkflowResponse {
                this.driverInbox = driverInbox {
                    this.billingAddress = request.driverInbox.billingAddress
                    this.counterparty = customerRef {
                        this.email = reply0.tripLog.counterparty.email
                    }
                    this.driverInboxId = request.driverInboxId
                    this.dueAt = reply2.driverHome.sla.dueAt
                }
                this.outcome = AdvanceDriverInboxWorkflowResponseKt.outcome {
                    this.message = reply3.result.message
                    this.status = reply3.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private fun InspectDriverInboxWorkflowRequest.toPauseDriverHomeWorkflowFromInspectDriverInboxWorkflow0(): PauseDriverHomeWorkflowRequest {
        val source = this
        return pauseDriverHomeWorkflowRequest {
            this.context = source.context
            this.driverHome = driverHome {
                this.assignee = userRef {
                    this.userId = source.context.actor.userId
                }
                this.externalReference = source.driverInboxId
                this.requester = contactInfo {
                    this.email = source.context.actor.email
                }
                this.sla = driverHomeSla {
                    this.dueAt = source.context.deadline
                }
            }
        }
    }

    private suspend fun handleInspectDriverInboxWorkflow(request: InspectDriverInboxWorkflowRequest): InspectDriverInboxWorkflowResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxWorkflowService/InspectDriverInboxWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "InspectDriverInboxWorkflow", request)
            log.atInfo().addKeyValue("operation", "InspectDriverInboxWorkflow").log("Handling transport request")
            val outbound0 = request.toPauseDriverHomeWorkflowFromInspectDriverInboxWorkflow0()
            val reply0 = driverappDriverHomeWorkflowService.pauseDriverHomeWorkflow(outbound0)
            inspectDriverInboxWorkflowResponse {
                this.driverInbox = driverInbox {
                    this.counterparty = customerRef {
                        this.email = reply0.driverHome.assignee.email
                    }
                    this.driverInboxId = request.driverInboxId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleListDriverInboxWorkflows(request: ListDriverInboxWorkflowsRequest): ListDriverInboxWorkflowsResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxWorkflowService/ListDriverInboxWorkflows") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListDriverInboxWorkflows", request)
            log.atInfo().addKeyValue("operation", "ListDriverInboxWorkflows").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = commentHouseholdApprovalRequest {
                this.context = request.context
                this.household = household {
                    this.children.addAll(listOf(household {
                        this.children.addAll(listOf(household {
                            this.location = postalAddress {
                                this.region = request.filter.region
                            }
                            this.manager = userRef {
                                this.email = request.context.actor.email
                                this.userId = request.context.actor.userId
                            }
                        }))
                    }))
                }
            }
            val reply0 = customersHouseholdApprovalService.commentHouseholdApproval(outbound0)
            val outbound1 = escalateDispatchOrderRequest {
                this.context = request.context
                this.details = EscalateDispatchOrderRequestKt.details {
                    this.approver = userRef {
                        this.email = reply0.household.manager.email
                    }
                }
                this.dispatchOrder = dispatchOrder {
                    this.billingAddress = postalAddress {
                        this.countryCode = reply0.household.location.countryCode
                        this.locality = reply0.household.location.locality
                        this.postalCode = reply0.household.location.postalCode
                    }
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = dispatchDispatchOrderApprovalService.escalateDispatchOrder(envelope1.unpack(EscalateDispatchOrderRequest::class.java))
            listDriverInboxWorkflowsResponse {
                this.driverInboxes.addAll(listOf(driverInbox {
                    this.billingAddress = postalAddress {
                        this.lines.addAll(reply1.dispatchOrder.billingAddress.linesList)
                    }
                    this.lines.addAll(listOf(driverInboxLine {
                        this.attributes.putAll(reply0.household.attributesMap)
                    }))
                }))
                this.result = operationResult {
                    this.status = reply0.outcome.status
                }
            }
        }

    private data class ResumeDriverInboxWorkflow1Command(
        val context: RequestContext,
        val filterRegion: String
    ) {
        fun toRequest(): ListMetricSeriesesRequest {
            val command = this
            return listMetricSeriesesRequest {
                this.context = command.context
                this.filter = ListMetricSeriesesRequestKt.filter {
                    this.region = command.filterRegion
                }
            }
        }
    }

    private suspend fun handleRetryDriverInboxWorkflow(request: RetryDriverInboxWorkflowRequest): RetryDriverInboxWorkflowResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxWorkflowService/RetryDriverInboxWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RetryDriverInboxWorkflow", request)
            log.atInfo().addKeyValue("operation", "RetryDriverInboxWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverInbox.driverInboxId).log("Recorded audit detail")
            val outbound0 = lintFeatureFlagsRequest {
                this.context = request.context
                this.featureFlag = featureFlag {
                    this.audit = auditInfo {
                        this.createdBy = request.driverInbox.audit.createdBy
                        this.etag = request.driverInbox.audit.etag
                    }
                }
            }
            val reply0 = invokeRetryDriverInboxWorkflow0(outbound0)
            var reply1 = EscalateDriverInboxResponse.getDefaultInstance()
            request.driverInbox.linesList.asFlow().map { item ->
                val outbound1 = escalateDriverInboxRequest {
                    this.context = request.context
                    this.driverInbox = driverInbox {
                        this.lines.addAll(listOf(driverInboxLine {
                            this.tax = driverInboxLineTax {
                                this.amount = item.tax.amount
                                this.jurisdiction = item.tax.jurisdiction
                                this.rate = item.tax.rate
                            }
                        }))
                    }
                }
                driverappDriverInboxApprovalService.escalateDriverInbox(outbound1)
            }.collect { reply1 = it }
            val outbound2 = DelegateDriverHomeApprovalRequest.newBuilder().apply {
                setContext(request.context)
                setDriverHome(driverHome {
                    this.comments.addAll(listOf(driverHomeComment {
                        this.createdAt = reply0.featureFlag.audit.createdAt
                    }))
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply2 = outbound2.let { value -> driverappDriverHomeApprovalService.delegateDriverHomeApproval(value) }
            retryDriverInboxWorkflowResponse {
                this.driverInbox = driverInbox {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.featureFlag.audit.updatedBy
                    }
                    this.counterparty = customerRef {
                        this.customerId = reply2.driverHome.customer.customerId
                        this.email = reply2.driverHome.customer.email
                    }
                    this.driverInboxId = request.driverInboxId
                    this.lines.addAll(listOf(driverInboxLine {
                        this.amount = reply0.featureFlag.discount.amount
                    }))
                    this.status = request.driverInbox.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSignalDriverInboxWorkflow(request: SignalDriverInboxWorkflowRequest): SignalDriverInboxWorkflowResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverInboxWorkflowService/SignalDriverInboxWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SignalDriverInboxWorkflow", request)
            log.atInfo().addKeyValue("operation", "SignalDriverInboxWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driverInbox.number).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                listPendingStopsRequest {
                    this.context = request.context
                    this.filter = ListPendingStopsRequestKt.filter {
                        this.region = request.driverInbox.billingAddress.region
                    }
                }
            }
            val reply0 = routingStopApprovalService.listPendingStops(outbound0)
            val outbound1 = listDriverInboxReportsRequest {
                this.context = request.context
                this.filter = ListDriverInboxReportsRequestKt.filter {
                    this.createdAfter = request.driverInbox.audit.updatedAt
                    this.labels.putAll(request.driverInbox.metadataMap)
                    this.ownerId = request.driverInbox.driverInboxId
                    this.region = request.driverInbox.billingAddress.region
                }
            }
            val reply1 = ClientRegistry.lookup("acme.driverapp.v1.DriverInboxReportingService", DriverInboxReportingServiceGrpcKt.DriverInboxReportingServiceCoroutineStub::class.java).listDriverInboxReports(outbound1)
            val outbound2 = delegateDriverInboxApprovalRequest {
                this.context = request.context
                this.details = DelegateDriverInboxApprovalRequestKt.details {
                    this.approver = userRef {
                        this.displayName = request.driverInbox.counterparty.displayName
                    }
                }
                this.driverInbox = driverInbox {
                    this.audit = auditInfo {
                        this.createdBy = request.driverInbox.audit.createdBy
                        this.updatedAt = request.driverInbox.audit.updatedAt
                    }
                    this.counterparty = customerRef {
                        this.email = request.details.notify.email
                    }
                }
            }
            val reply2 = driverappDriverInboxApprovalService.delegateDriverInboxApproval(WireCopies.reflective(outbound2))
            signalDriverInboxWorkflowResponse {
                this.driverInbox = driverInbox {
                    this.audit = auditInfo {
                        this.createdAt = reply2.driverInbox.audit.createdAt
                    }
                    this.counterparty = customerRef {
                        this.customerId = reply2.driverInbox.counterparty.customerId
                    }
                    this.driverInboxId = request.driverInboxId
                    this.dueAt = request.driverInbox.dueAt
                }
                this.outcome = SignalDriverInboxWorkflowResponseKt.outcome {
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }
}
