package com.acme.transport.driverapp.grpc

import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.customers.v1.AdvanceCustomerWorkflowRequest
import com.acme.customers.v1.CustomerApprovalServiceGrpcKt
import com.acme.customers.v1.CustomerSegmentApprovalServiceGrpcKt
import com.acme.customers.v1.CustomerWorkflowServiceGrpcKt
import com.acme.customers.v1.DelegateCustomerApprovalRequestKt
import com.acme.customers.v1.RecallCustomerSegmentRequestKt
import com.acme.customers.v1.advanceCustomerWorkflowRequest
import com.acme.customers.v1.customer
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.customerSegment
import com.acme.customers.v1.delegateCustomerApprovalRequest
import com.acme.customers.v1.recallCustomerSegmentRequest
import com.acme.datasets.v1.DatasetServiceGrpcKt
import com.acme.datasets.v1.createDatasetRequest
import com.acme.datasets.v1.dataset
import com.acme.datasets.v1.datasetComponent
import com.acme.documents.v1.DocumentValidationServiceGrpcKt
import com.acme.documents.v1.LintDocumentsRequest
import com.acme.documents.v1.document
import com.acme.documents.v1.lintDocumentsRequest
import com.acme.driverapp.v1.ConnectDriverHomeSourceRequest
import com.acme.driverapp.v1.ConnectDriverHomeSourceResponse
import com.acme.driverapp.v1.DeleteDriverHomeReportRequest
import com.acme.driverapp.v1.DeleteDriverHomeReportResponse
import com.acme.driverapp.v1.DisconnectDriverInboxSourceRequest
import com.acme.driverapp.v1.DisconnectDriverInboxSourceResponse
import com.acme.driverapp.v1.DriverHomeApprovalServiceGrpcKt
import com.acme.driverapp.v1.DriverHomeIntegrationServiceGrpcKt
import com.acme.driverapp.v1.DriverHomeNotificationServiceGrpcKt
import com.acme.driverapp.v1.DriverHomeReportingServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxIntegrationServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxWorkflowServiceGrpcKt
import com.acme.driverapp.v1.GetDriverHomePreferencesRequest
import com.acme.driverapp.v1.GetDriverHomePreferencesResponse
import com.acme.driverapp.v1.ListDriverHomeSubscriptionsRequest
import com.acme.driverapp.v1.ListDriverHomeSubscriptionsResponse
import com.acme.driverapp.v1.NotifyDriverHomeWatchersRequest
import com.acme.driverapp.v1.NotifyDriverHomeWatchersResponse
import com.acme.driverapp.v1.NotifyDriverHomeWatchersResponseKt
import com.acme.driverapp.v1.SendDriverHomeTestNoticeRequest
import com.acme.driverapp.v1.SendDriverHomeTestNoticeResponse
import com.acme.driverapp.v1.SendDriverHomeTestNoticeResponseKt
import com.acme.driverapp.v1.SignalDriverInboxWorkflowRequest
import com.acme.driverapp.v1.SignalDriverInboxWorkflowRequestKt
import com.acme.driverapp.v1.StreamDriverHomeNoticesRequest
import com.acme.driverapp.v1.StreamDriverHomeNoticesResponse
import com.acme.driverapp.v1.SubmitDriverHomeResponse
import com.acme.driverapp.v1.SubscribeDriverHomeUpdatesRequest
import com.acme.driverapp.v1.SubscribeDriverHomeUpdatesResponse
import com.acme.driverapp.v1.SubscribeDriverHomeUpdatesResponseKt
import com.acme.driverapp.v1.UnsubscribeDriverHomeUpdatesRequest
import com.acme.driverapp.v1.UnsubscribeDriverHomeUpdatesResponse
import com.acme.driverapp.v1.UnsubscribeDriverHomeUpdatesResponseKt
import com.acme.driverapp.v1.connectDriverHomeSourceRequest
import com.acme.driverapp.v1.copy as driverappCopy
import com.acme.driverapp.v1.deleteDriverHomeReportRequest
import com.acme.driverapp.v1.disconnectDriverHomeSourceRequest
import com.acme.driverapp.v1.disconnectDriverInboxSourceRequest
import com.acme.driverapp.v1.driverHome
import com.acme.driverapp.v1.driverInbox
import com.acme.driverapp.v1.getDriverHomeApprovalStatusRequest
import com.acme.driverapp.v1.getDriverHomePreferencesResponse
import com.acme.driverapp.v1.listDriverHomeSubscriptionsResponse
import com.acme.driverapp.v1.notifyDriverHomeWatchersResponse
import com.acme.driverapp.v1.sendDriverHomeTestNoticeResponse
import com.acme.driverapp.v1.signalDriverInboxWorkflowRequest
import com.acme.driverapp.v1.streamDriverHomeNoticesResponse
import com.acme.driverapp.v1.submitDriverHomeRequest
import com.acme.driverapp.v1.subscribeDriverHomeUpdatesResponse
import com.acme.driverapp.v1.unsubscribeDriverHomeUpdatesResponse
import com.acme.drivers.v1.CountDriverLicenseTotalsRequest
import com.acme.drivers.v1.CountDriverLicenseTotalsRequestKt
import com.acme.drivers.v1.CountDriverLicenseTotalsResponse
import com.acme.drivers.v1.DriverLicenseReportingServiceGrpcKt
import com.acme.drivers.v1.countDriverLicenseTotalsRequest
import com.acme.fuel.v1.FuelTransactionSyncServiceGrpcKt
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.syncFuelTransactionsRequest
import com.acme.identity.v1.userRef
import com.acme.metrics.v1.ListMetricSeriesesRequestKt
import com.acme.metrics.v1.MetricSeriesServiceGrpcKt
import com.acme.metrics.v1.listMetricSeriesesRequest
import com.acme.transport.driverapp.DriverappRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Tells the people who follow a driver home what changed. */
class DriverHomeNotificationEndpoint(
    private val store: DriverappRepository,
    private val workers: CoroutineScope,
    private val customersCustomerApprovalService: CustomerApprovalServiceGrpcKt.CustomerApprovalServiceCoroutineStub,
    private val customersCustomerSegmentApprovalService: CustomerSegmentApprovalServiceGrpcKt.CustomerSegmentApprovalServiceCoroutineStub,
    private val customersCustomerWorkflowService: CustomerWorkflowServiceGrpcKt.CustomerWorkflowServiceCoroutineStub,
    private val datasetsDatasetService: DatasetServiceGrpcKt.DatasetServiceCoroutineStub,
    private val documentsDocumentValidationService: DocumentValidationServiceGrpcKt.DocumentValidationServiceCoroutineStub,
    private val driverappDriverHomeApprovalService: DriverHomeApprovalServiceGrpcKt.DriverHomeApprovalServiceCoroutineStub,
    private val driverappDriverHomeIntegrationService: DriverHomeIntegrationServiceGrpcKt.DriverHomeIntegrationServiceCoroutineStub,
    private val driverappDriverHomeReportingService: DriverHomeReportingServiceGrpcKt.DriverHomeReportingServiceCoroutineStub,
    private val driverappDriverInboxIntegrationService: DriverInboxIntegrationServiceGrpcKt.DriverInboxIntegrationServiceCoroutineStub,
    private val driverappDriverInboxWorkflowService: DriverInboxWorkflowServiceGrpcKt.DriverInboxWorkflowServiceCoroutineStub,
    private val driversDriverLicenseReportingService: DriverLicenseReportingServiceGrpcKt.DriverLicenseReportingServiceCoroutineStub,
    private val fuelFuelTransactionSyncService: FuelTransactionSyncServiceGrpcKt.FuelTransactionSyncServiceCoroutineStub,
    private val metricsMetricSeriesService: MetricSeriesServiceGrpcKt.MetricSeriesServiceCoroutineStub,
) : DriverHomeNotificationServiceGrpcKt.DriverHomeNotificationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DriverHomeNotificationEndpoint::class.java)

    private val invokeNotifyDriverHomeWatchers1: suspend (DeleteDriverHomeReportRequest) -> DeleteDriverHomeReportResponse = { payload ->
        driverappDriverHomeReportingService.deleteDriverHomeReport(payload)
    }

    private val workerForStreamDriverHomeNotices2 = ReplyMailbox<ConnectDriverHomeSourceRequest, ConnectDriverHomeSourceResponse>(workers) { payload ->
        withTimeout(5_000) { driverappDriverHomeIntegrationService.connectDriverHomeSource(payload) }
    }

    private val clientForStreamDriverHomeNotices3 by lazy { datasetsDatasetService }

    /** Returns a driver home. */
    override suspend fun getDriverHomePreferences(request: GetDriverHomePreferencesRequest): GetDriverHomePreferencesResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeNotificationService/GetDriverHomePreferences") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetDriverHomePreferences", request)
            log.atInfo().addKeyValue("operation", "GetDriverHomePreferences").log("Handling transport request")
            val outbound0 = signalDriverInboxWorkflowRequest {
                this.context = request.context
                this.details = SignalDriverInboxWorkflowRequestKt.details {
                    this.parameters.putAll(request.context.baggageMap)
                }
                this.driverInbox = driverInbox {
                    this.audit = auditInfo {
                        this.updatedAt = request.context.deadline
                    }
                    this.counterparty = customerRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val command0: Submission<SignalDriverInboxWorkflowRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = driverappDriverInboxWorkflowService.signalDriverInboxWorkflow(payload0)
            getDriverHomePreferencesResponse {
                this.driverHome = driverHome {
                    this.driverHomeId = request.driverHomeId
                }
                this.result = operationResult {
                    this.message = reply0.outcome.message
                    this.status = "ok"
                }
            }
        }

    /** Lists driver homes a page at a time. */
    override suspend fun listDriverHomeSubscriptions(request: ListDriverHomeSubscriptionsRequest): ListDriverHomeSubscriptionsResponse {
        return handleListDriverHomeSubscriptions(request)
    }

    /** Notifys a driver home. */
    override suspend fun notifyDriverHomeWatchers(request: NotifyDriverHomeWatchersRequest): NotifyDriverHomeWatchersResponse = handleNotifyDriverHomeWatchers(request)

    /** Sends a driver home. */
    override suspend fun sendDriverHomeTestNotice(request: SendDriverHomeTestNoticeRequest): SendDriverHomeTestNoticeResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeNotificationService/SendDriverHomeTestNotice") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SendDriverHomeTestNotice", request)
            log.atInfo().addKeyValue("operation", "SendDriverHomeTestNotice").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.recipient.phone).log("Recorded audit detail")
            val outbound0 = AdvanceCustomerWorkflowRequest.newBuilder().apply {
                setContext(request.context)
                setCustomer(customer {
                    this.relationshipManager = userRef {
                        this.email = request.details.recipient.email
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> customersCustomerWorkflowService.advanceCustomerWorkflow(value) }
            val outbound1 = RequestFactory.create {
                delegateCustomerApprovalRequest {
                    this.context = request.context
                    this.customer = customer {
                        this.contact = contactInfo {
                            this.name = request.details.recipient.name
                        }
                        this.relationshipManager = userRef {
                            this.userId = reply0.customer.relationshipManager.userId
                        }
                    }
                    this.details = DelegateCustomerApprovalRequestKt.details {
                        this.approver = userRef {
                            this.email = reply0.customer.contact.email
                        }
                    }
                }
            }
            val reply1 = customersCustomerApprovalService.delegateCustomerApproval(outbound1)
            sendDriverHomeTestNoticeResponse {
                this.driverHome = driverHome {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.customer.audit.updatedAt
                    }
                    this.driverHomeId = request.driverHomeId
                    this.requester = contactInfo {
                        this.email = request.details.recipient.email
                        this.preferredChannel = reply0.customer.contact.preferredChannel
                    }
                }
                this.outcome = SendDriverHomeTestNoticeResponseKt.outcome {
                    this.message = reply1.result.message
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Streams the changes of driver homes as they happen. */
    override fun streamDriverHomeNotices(request: StreamDriverHomeNoticesRequest): Flow<StreamDriverHomeNoticesResponse> = flow {
        emit(handleStreamDriverHomeNotices(request))
    }

    /** Subscribes a driver home. */
    override suspend fun subscribeDriverHomeUpdates(request: SubscribeDriverHomeUpdatesRequest): SubscribeDriverHomeUpdatesResponse = handleSubscribeDriverHomeUpdates(request)

    /** Unsubscribes a driver home. */
    override suspend fun unsubscribeDriverHomeUpdates(request: UnsubscribeDriverHomeUpdatesRequest): UnsubscribeDriverHomeUpdatesResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeNotificationService/UnsubscribeDriverHomeUpdates") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "UnsubscribeDriverHomeUpdates", request)
            log.atInfo().addKeyValue("operation", "UnsubscribeDriverHomeUpdates").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.recipient.phone).log("Recorded audit detail")
            val outbound0 = listMetricSeriesesRequest {
                this.context = request.context
                this.filter = ListMetricSeriesesRequestKt.filter {
                    this.createdAfter = request.context.deadline
                    this.labels.putAll(request.details.variablesMap)
                    this.ownerId = request.driverHomeId
                }
            }
            val reply0 = metricsMetricSeriesService.listMetricSerieses(outbound0)
            val outbound1 = submitDriverHomeRequest {
                this.context = request.context
                this.driverHome = driverHome {
                    this.requester = contactInfo {
                        this.preferredChannel = request.details.recipient.preferredChannel
                    }
                }
            }
            val reply1 = driverappDriverHomeApprovalService.submitDriverHome(outbound1)
            val outbound2 = disconnectDriverHomeSourceRequest {}.driverappCopy {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.driverHomeId = request.driverHomeId
            }
            val reply2 = driverappDriverHomeIntegrationService.disconnectDriverHomeSource(outbound2)
            val outbound3 = request.toLintDocumentsFromUnsubscribeDriverHomeUpdates3(reply1)
            val reply3 = documentsDocumentValidationService.lintDocuments(outbound3)
            unsubscribeDriverHomeUpdatesResponse {
                this.driverHome = driverHome {
                    this.assignee = userRef {
                        this.email = request.details.recipient.email
                    }
                    this.audit = auditInfo {
                        this.updatedAt = reply2.driverHome.audit.updatedAt
                    }
                    this.driverHomeId = request.driverHomeId
                    this.tags.addAll(reply2.driverHome.tagsList)
                }
                this.outcome = UnsubscribeDriverHomeUpdatesResponseKt.outcome {
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply1.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleListDriverHomeSubscriptions(request: ListDriverHomeSubscriptionsRequest): ListDriverHomeSubscriptionsResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeNotificationService/ListDriverHomeSubscriptions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListDriverHomeSubscriptions", request)
            log.atInfo().addKeyValue("operation", "ListDriverHomeSubscriptions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            listDriverHomeSubscriptionsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleNotifyDriverHomeWatchers(request: NotifyDriverHomeWatchersRequest): NotifyDriverHomeWatchersResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeNotificationService/NotifyDriverHomeWatchers") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "NotifyDriverHomeWatchers", request)
            log.atInfo().addKeyValue("operation", "NotifyDriverHomeWatchers").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.recipient.email).log("Recorded audit detail")
            val outbound0 = disconnectDriverInboxSourceRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.reason = request.reason
            }
            val adapter0 = OutboundPort<DisconnectDriverInboxSourceRequest, DisconnectDriverInboxSourceResponse> { payload ->
                driverappDriverInboxIntegrationService.disconnectDriverInboxSource(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = deleteDriverHomeReportRequest {
                this.context = request.context
                this.driverHomeId = request.driverHomeId
                this.etag = reply0.driverInbox.audit.etag
                this.reason = request.reason
            }
            val reply1 = invokeNotifyDriverHomeWatchers1(outbound1)
            val outbound2 = countDriverLicenseTotalsRequest {
                this.context = request.context
                this.filter = CountDriverLicenseTotalsRequestKt.filter {
                    this.createdAfter = reply0.driverInbox.dueAt
                    this.labels.putAll(reply1.result.detailsMap)
                    this.region = reply0.driverInbox.billingAddress.region
                }
            }
            val invoke2: suspend (CountDriverLicenseTotalsRequest, Metadata) -> CountDriverLicenseTotalsResponse = driversDriverLicenseReportingService::countDriverLicenseTotals
            val reply2 = invoke2(outbound2, Metadata())
            notifyDriverHomeWatchersResponse {
                this.driverHome = driverHome {
                    this.driverHomeId = request.driverHomeId
                    this.requester = contactInfo {
                        this.phone = request.details.recipient.phone
                    }
                }
                this.outcome = NotifyDriverHomeWatchersResponseKt.outcome {
                    this.status = reply0.outcome.status
                }
                this.result = operationResult {
                    this.details.putAll(reply2.result.detailsMap)
                    this.message = reply0.outcome.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleStreamDriverHomeNotices(request: StreamDriverHomeNoticesRequest): StreamDriverHomeNoticesResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeNotificationService/StreamDriverHomeNotices") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StreamDriverHomeNotices", request)
            log.atInfo().addKeyValue("operation", "StreamDriverHomeNotices").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = syncFuelTransactionsRequest {
                this.context = request.context
                this.fuelTransaction = fuelTransaction {
                    this.occurredAt = request.filter.createdAfter
                    this.subject = customerRef {
                        this.email = request.subscriber.email
                    }
                }
            }
            val reply0 = ClientRegistry.lookup("acme.fuel.v1.FuelTransactionSyncService", FuelTransactionSyncServiceGrpcKt.FuelTransactionSyncServiceCoroutineStub::class.java).syncFuelTransactions(outbound0)
            val outbound1 = recallCustomerSegmentRequest {
                this.context = request.context
                this.customerSegment = customerSegment {
                    this.owner = userRef {
                        this.displayName = reply0.fuelTransaction.subject.displayName
                    }
                }
                this.details = RecallCustomerSegmentRequestKt.details {
                    this.approver = userRef {
                        this.email = reply0.fuelTransaction.subject.email
                    }
                    this.effectiveAt = reply0.outcome.effectiveAt
                }
            }
            val reply1 = customersCustomerSegmentApprovalService.recallCustomerSegment(WireCopies.reflective(outbound1))
            val outbound2 = connectDriverHomeSourceRequest {
                this.context = request.context
                this.reason = reply1.customerSegment.block.reason
            }
            val reply2 = workerForStreamDriverHomeNotices2.submit(outbound2)
            val outbound3 = createDatasetRequest {
                this.context = request.context
                this.dataset = dataset {
                    this.audit = auditInfo {
                        this.createdBy = reply1.customerSegment.audit.createdBy
                    }
                    this.components.putAll(mapOf("primary" to datasetComponent {
                        this.description = reply1.customerSegment.description
                    }))
                    this.owner = userRef {
                        this.email = reply0.fuelTransaction.subject.email
                    }
                }
            }
            val reply3 = clientForStreamDriverHomeNotices3.createDataset(outbound3)
            streamDriverHomeNoticesResponse {
                this.driverHome = driverHome {
                    this.assignee = userRef {
                        this.email = reply1.customerSegment.owner.email
                    }
                    this.audit = auditInfo {
                        this.updatedBy = reply3.dataset.audit.updatedBy
                    }
                    this.requester = contactInfo {
                        this.email = request.subscriber.email
                        this.preferredChannel = request.subscriber.preferredChannel
                    }
                    this.subject = reply2.driverHome.subject
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSubscribeDriverHomeUpdates(request: SubscribeDriverHomeUpdatesRequest): SubscribeDriverHomeUpdatesResponse =
        RpcBoundary.run("acme.driverapp.v1.DriverHomeNotificationService/SubscribeDriverHomeUpdates") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SubscribeDriverHomeUpdates", request)
            log.atInfo().addKeyValue("operation", "SubscribeDriverHomeUpdates").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.recipient.phone).log("Recorded audit detail")
            val outbound0 = getDriverHomeApprovalStatusRequest {
                this.context = request.context
                this.driverHomeId = request.driverHomeId
            }
            val reply0 = flowOf(outbound0).map { value -> driverappDriverHomeApprovalService.getDriverHomeApprovalStatus(value) }.single()
            val outbound1 = syncFuelTransactionsRequest {
                this.context = request.context
                this.fuelTransaction = fuelTransaction {
                    this.subject = customerRef {
                        this.customerId = reply0.driverHome.customer.customerId
                    }
                }
            }
            val reply1 = withTimeout(5_000) { fuelFuelTransactionSyncService.syncFuelTransactions(outbound1) }
            val configure2: CountDriverLicenseTotalsRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.filter = CountDriverLicenseTotalsRequestKt.filter {
                    this.createdAfter = reply0.driverHome.audit.updatedAt
                    this.ownerId = reply1.result.correlationId
                }
            }
            val outbound2 = countDriverLicenseTotalsRequest(configure2)
            val reply2 = driversDriverLicenseReportingService.countDriverLicenseTotals(outbound2)
            subscribeDriverHomeUpdatesResponse {
                this.driverHome = driverHome {
                    this.assignee = userRef {
                        this.email = request.details.recipient.email
                    }
                    this.driverHomeId = request.driverHomeId
                }
                this.outcome = SubscribeDriverHomeUpdatesResponseKt.outcome {
                    this.message = reply2.result.message
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = reply1.outcome.status
                }
            }
        }

    private fun UnsubscribeDriverHomeUpdatesRequest.toLintDocumentsFromUnsubscribeDriverHomeUpdates3(reply1: SubmitDriverHomeResponse): LintDocumentsRequest {
        val source = this
        return lintDocumentsRequest {
            this.context = source.context
            this.document = document {
                this.audit = auditInfo {
                    this.createdBy = reply1.driverHome.audit.createdBy
                    this.updatedAt = reply1.driverHome.audit.updatedAt
                }
            }
        }
    }
}
