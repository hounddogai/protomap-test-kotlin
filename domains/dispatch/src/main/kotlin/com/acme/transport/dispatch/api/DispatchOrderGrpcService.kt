package com.acme.transport.dispatch.api

import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.pageInfo
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.customerRef
import com.acme.dispatch.v1.BatchCreateDispatchOrdersRequest
import com.acme.dispatch.v1.BatchCreateDispatchOrdersResponse
import com.acme.dispatch.v1.BatchGetDispatchOrdersRequest
import com.acme.dispatch.v1.BatchGetDispatchOrdersResponse
import com.acme.dispatch.v1.BrowseDispatchAssignmentsRequest
import com.acme.dispatch.v1.BrowseDispatchAssignmentsRequestKt
import com.acme.dispatch.v1.BrowseDispatchAssignmentsResponse
import com.acme.dispatch.v1.BrowseDispatchOrdersRequest
import com.acme.dispatch.v1.BrowseDispatchOrdersRequestKt
import com.acme.dispatch.v1.CloneDispatchOrderRequest
import com.acme.dispatch.v1.CloneDispatchOrderResponse
import com.acme.dispatch.v1.CreateDispatchOrderRequest
import com.acme.dispatch.v1.CreateDispatchOrderResponse
import com.acme.dispatch.v1.DeleteDispatchOrderRequest
import com.acme.dispatch.v1.DeleteDispatchOrderResponse
import com.acme.dispatch.v1.DispatchAssignmentCalculationServiceGrpcKt
import com.acme.dispatch.v1.DispatchAssignmentQueryServiceGrpcKt
import com.acme.dispatch.v1.DispatchAssignmentValidationServiceGrpcKt
import com.acme.dispatch.v1.DispatchOrderLine
import com.acme.dispatch.v1.DispatchOrderQueryServiceGrpcKt
import com.acme.dispatch.v1.DispatchOrderServiceGrpcKt
import com.acme.dispatch.v1.DispatchOrderValidationServiceGrpcKt
import com.acme.dispatch.v1.ExplainDispatchOrderDecisionResponse
import com.acme.dispatch.v1.GetDispatchOrderRequest
import com.acme.dispatch.v1.GetDispatchOrderResponse
import com.acme.dispatch.v1.NormalizeDispatchOrderResponse
import com.acme.dispatch.v1.PreviewDispatchOrderRequestKt
import com.acme.dispatch.v1.SearchDispatchOrdersRequest
import com.acme.dispatch.v1.SearchDispatchOrdersResponse
import com.acme.dispatch.v1.UpdateDispatchOrderRequest
import com.acme.dispatch.v1.UpdateDispatchOrderResponse
import com.acme.dispatch.v1.ValidateDispatchAssignmentRequest
import com.acme.dispatch.v1.ValidateDispatchAssignmentRequestKt
import com.acme.dispatch.v1.WatchDispatchOrdersRequest
import com.acme.dispatch.v1.WatchDispatchOrdersResponse
import com.acme.dispatch.v1.ZoneValidationServiceGrpcKt
import com.acme.dispatch.v1.batchCreateDispatchOrdersResponse
import com.acme.dispatch.v1.batchGetDispatchOrdersResponse
import com.acme.dispatch.v1.browseDispatchAssignmentsRequest
import com.acme.dispatch.v1.browseDispatchOrdersRequest
import com.acme.dispatch.v1.cloneDispatchOrderResponse
import com.acme.dispatch.v1.copy as dispatchCopy
import com.acme.dispatch.v1.createDispatchOrderResponse
import com.acme.dispatch.v1.deleteDispatchOrderResponse
import com.acme.dispatch.v1.dispatchAssignment
import com.acme.dispatch.v1.dispatchOrder
import com.acme.dispatch.v1.dispatchOrderAttachment
import com.acme.dispatch.v1.dispatchOrderLine
import com.acme.dispatch.v1.dispatchOrderLineTax
import com.acme.dispatch.v1.estimateDispatchAssignmentRequest
import com.acme.dispatch.v1.explainDispatchOrderDecisionRequest
import com.acme.dispatch.v1.getDispatchOrderResponse
import com.acme.dispatch.v1.getDispatchOrderRulesRequest
import com.acme.dispatch.v1.lintZonesRequest
import com.acme.dispatch.v1.normalizeDispatchOrderRequest
import com.acme.dispatch.v1.previewDispatchOrderRequest
import com.acme.dispatch.v1.resolveDispatchAssignmentsRequest
import com.acme.dispatch.v1.searchDispatchOrdersResponse
import com.acme.dispatch.v1.updateDispatchOrderResponse
import com.acme.dispatch.v1.validateDispatchAssignmentRequest
import com.acme.dispatch.v1.watchDispatchOrdersResponse
import com.acme.dispatch.v1.zone
import com.acme.drivers.v1.driverRef
import com.acme.fuel.v1.CreateFuelCardRequest
import com.acme.fuel.v1.CreateFuelCardResponse
import com.acme.fuel.v1.EstimateFuelTransactionRequestKt
import com.acme.fuel.v1.FuelCardQueryServiceGrpcKt
import com.acme.fuel.v1.FuelCardServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionCalculationServiceGrpcKt
import com.acme.fuel.v1.SuggestFuelCardsRequestKt
import com.acme.fuel.v1.SuggestFuelCardsResponse
import com.acme.fuel.v1.createFuelCardRequest
import com.acme.fuel.v1.estimateFuelTransactionRequest
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.suggestFuelCardsRequest
import com.acme.identity.v1.userRef
import com.acme.keymanagement.v1.CryptoKeyServiceGrpcKt
import com.acme.keymanagement.v1.SearchCryptoKeysRequestKt
import com.acme.keymanagement.v1.searchCryptoKeysRequest
import com.acme.maintenance.v1.CountServiceSchedulesRequest
import com.acme.maintenance.v1.CountServiceSchedulesRequestKt
import com.acme.maintenance.v1.CountServiceSchedulesResponse
import com.acme.maintenance.v1.ServiceScheduleQueryServiceGrpcKt
import com.acme.maintenance.v1.countServiceSchedulesRequest
import com.acme.metrics.v1.BrowseMetricSeriesesRequest
import com.acme.metrics.v1.BrowseMetricSeriesesRequestKt
import com.acme.metrics.v1.MetricSeriesQueryServiceGrpcKt
import com.acme.metrics.v1.browseMetricSeriesesRequest
import com.acme.notifications.v1.BrowseNotificationsRequestKt
import com.acme.notifications.v1.NotificationQueryServiceGrpcKt
import com.acme.notifications.v1.browseNotificationsRequest
import com.acme.prescriptions.v1.ComparePrescriptionOptionsRequest
import com.acme.prescriptions.v1.PrescriptionCalculationServiceGrpcKt
import com.acme.prescriptions.v1.comparePrescriptionOptionsRequest
import com.acme.prescriptions.v1.prescription
import com.acme.search.v1.BrowseSearchIndexesRequestKt
import com.acme.search.v1.SearchIndexQueryServiceGrpcKt
import com.acme.search.v1.browseSearchIndexesRequest
import com.acme.telematics.v1.GeoFenceValidationServiceGrpcKt
import com.acme.telematics.v1.LintGeoFencesRequestKt
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.lintGeoFencesRequest
import com.acme.transport.dispatch.DispatchRepository
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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Manages dispatch orders: creates, reads, changes, and lists them. */
class DispatchOrderGrpcService(
    private val store: DispatchRepository,
    private val workers: CoroutineScope,
    private val dispatchDispatchAssignmentCalculationService: DispatchAssignmentCalculationServiceGrpcKt.DispatchAssignmentCalculationServiceCoroutineStub,
    private val dispatchDispatchAssignmentQueryService: DispatchAssignmentQueryServiceGrpcKt.DispatchAssignmentQueryServiceCoroutineStub,
    private val dispatchDispatchAssignmentValidationService: DispatchAssignmentValidationServiceGrpcKt.DispatchAssignmentValidationServiceCoroutineStub,
    private val dispatchDispatchOrderQueryService: DispatchOrderQueryServiceGrpcKt.DispatchOrderQueryServiceCoroutineStub,
    private val dispatchDispatchOrderValidationService: DispatchOrderValidationServiceGrpcKt.DispatchOrderValidationServiceCoroutineStub,
    private val dispatchZoneValidationService: ZoneValidationServiceGrpcKt.ZoneValidationServiceCoroutineStub,
    private val fuelFuelCardQueryService: FuelCardQueryServiceGrpcKt.FuelCardQueryServiceCoroutineStub,
    private val fuelFuelCardService: FuelCardServiceGrpcKt.FuelCardServiceCoroutineStub,
    private val fuelFuelTransactionCalculationService: FuelTransactionCalculationServiceGrpcKt.FuelTransactionCalculationServiceCoroutineStub,
    private val keymanagementCryptoKeyService: CryptoKeyServiceGrpcKt.CryptoKeyServiceCoroutineStub,
    private val maintenanceServiceScheduleQueryService: ServiceScheduleQueryServiceGrpcKt.ServiceScheduleQueryServiceCoroutineStub,
    private val metricsMetricSeriesQueryService: MetricSeriesQueryServiceGrpcKt.MetricSeriesQueryServiceCoroutineStub,
    private val notificationsNotificationQueryService: NotificationQueryServiceGrpcKt.NotificationQueryServiceCoroutineStub,
    private val prescriptionsPrescriptionCalculationService: PrescriptionCalculationServiceGrpcKt.PrescriptionCalculationServiceCoroutineStub,
    private val searchSearchIndexQueryService: SearchIndexQueryServiceGrpcKt.SearchIndexQueryServiceCoroutineStub,
    private val telematicsGeoFenceValidationService: GeoFenceValidationServiceGrpcKt.GeoFenceValidationServiceCoroutineStub,
) : DispatchOrderServiceGrpcKt.DispatchOrderServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DispatchOrderGrpcService::class.java)

    private val invokeCreateDispatchOrder2: suspend (BrowseDispatchAssignmentsRequest) -> BrowseDispatchAssignmentsResponse = { payload ->
        dispatchDispatchAssignmentQueryService.browseDispatchAssignments(payload)
    }

    private val workerForSearchDispatchOrders1 = ReplyMailbox<CreateFuelCardRequest, CreateFuelCardResponse>(workers) { payload ->
        withTimeout(5_000) { fuelFuelCardService.createFuelCard(payload) }
    }

    private val clientForUpdateDispatchOrder0 by lazy { searchSearchIndexQueryService }

    /** Takes a stream of dispatch orders and answers once at the end. */
    override suspend fun batchCreateDispatchOrders(requests: Flow<BatchCreateDispatchOrdersRequest>): BatchCreateDispatchOrdersResponse {
        var answer = BatchCreateDispatchOrdersResponse.getDefaultInstance()
        requests.map { request -> handleBatchCreateDispatchOrders(request) }.collect { answer = it }
        return answer
    }

    /** Returns several dispatch orders at once. */
    override suspend fun batchGetDispatchOrders(request: BatchGetDispatchOrdersRequest): BatchGetDispatchOrdersResponse = handleBatchGetDispatchOrders(request)

    /** Creates a dispatch order. */
    override suspend fun cloneDispatchOrder(request: CloneDispatchOrderRequest): CloneDispatchOrderResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderService/CloneDispatchOrder") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CloneDispatchOrder", request)
            log.atInfo().addKeyValue("operation", "CloneDispatchOrder").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchOrder.number).log("Recorded audit detail")
            val outbound0 = browseMetricSeriesesRequest {
                this.context = request.context
                this.filter = BrowseMetricSeriesesRequestKt.filter {
                    this.createdAfter = request.dispatchOrder.dueAt
                    this.labels.putAll(request.dispatchOrder.metadataMap)
                    this.ownerId = request.dispatchOrder.counterparty.zoneId
                    this.region = request.dispatchOrder.billingAddress.region
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = metricsMetricSeriesQueryService.browseMetricSerieses(envelope0.unpack(BrowseMetricSeriesesRequest::class.java))
            val outbound1 = getDispatchOrderRulesRequest {
                this.context = request.context
                this.dispatchOrderId = request.dispatchOrder.dispatchOrderId
            }
            val reply1 = dispatchDispatchOrderValidationService.getDispatchOrderRules(WireCopies.json(outbound1))
            var reply2 = ExplainDispatchOrderDecisionResponse.getDefaultInstance()
            request.dispatchOrder.attachmentsList.asFlow().map { item ->
                val outbound2 = explainDispatchOrderDecisionRequest {
                    this.context = request.context
                    this.dispatchOrder = dispatchOrder {
                        this.attachments.addAll(listOf(dispatchOrderAttachment {
                            this.fileName = item.fileName
                            this.uri = item.uri
                        }))
                    }
                }
                dispatchDispatchOrderValidationService.explainDispatchOrderDecision(outbound2)
            }.collect { reply2 = it }
            cloneDispatchOrderResponse {
                this.dispatchOrder = dispatchOrder {
                    this.audit = auditInfo {
                        this.createdBy = reply1.dispatchOrder.audit.createdBy
                    }
                    this.billingAddress = reply1.dispatchOrder.billingAddress
                    this.issuedAt = request.dispatchOrder.issuedAt
                    this.status = request.dispatchOrder.status
                }
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }

    /** Creates a dispatch order. */
    override suspend fun createDispatchOrder(request: CreateDispatchOrderRequest): CreateDispatchOrderResponse {
        return handleCreateDispatchOrder(request)
    }

    /** Deletes a dispatch order. */
    override suspend fun deleteDispatchOrder(request: DeleteDispatchOrderRequest): DeleteDispatchOrderResponse = handleDeleteDispatchOrder(request)

    /** Returns a dispatch order. */
    override suspend fun getDispatchOrder(request: GetDispatchOrderRequest): GetDispatchOrderResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderService/GetDispatchOrder") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetDispatchOrder", request)
            log.atInfo().addKeyValue("operation", "GetDispatchOrder").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            getDispatchOrderResponse {
                this.dispatchOrder = dispatchOrder {
                    this.dispatchOrderId = request.dispatchOrderId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Lists dispatch orders a page at a time. */
    override suspend fun searchDispatchOrders(request: SearchDispatchOrdersRequest): SearchDispatchOrdersResponse {
        return handleSearchDispatchOrders(request)
    }

    /** Changes a dispatch order. */
    override suspend fun updateDispatchOrder(request: UpdateDispatchOrderRequest): UpdateDispatchOrderResponse = handleUpdateDispatchOrder(request)

    /** Streams the changes of dispatch orders as they happen. */
    override fun watchDispatchOrders(request: WatchDispatchOrdersRequest): Flow<WatchDispatchOrdersResponse> = flow {
        emit(handleWatchDispatchOrders(request))
    }

    private fun BatchCreateDispatchOrdersRequest.toValidateDispatchAssignmentFromBatchCreateDispatchOrders3(reply0: NormalizeDispatchOrderResponse): ValidateDispatchAssignmentRequest {
        val source = this
        return validateDispatchAssignmentRequest {
            this.context = source.context
            this.details = ValidateDispatchAssignmentRequestKt.details {
                this.contact = contactInfo {
                    this.preferredChannel = source.header.submittedBy.preferredChannel
                }
            }
            this.dispatchAssignment = dispatchAssignment {
                this.assignee = userRef {
                    this.displayName = reply0.dispatchOrder.counterparty.displayName
                }
                this.audit = auditInfo {
                    this.updatedAt = source.record.audit.updatedAt
                }
                this.requester = contactInfo {
                    this.email = source.header.submittedBy.email
                }
            }
        }
    }

    private suspend fun handleBatchCreateDispatchOrders(request: BatchCreateDispatchOrdersRequest): BatchCreateDispatchOrdersResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderService/BatchCreateDispatchOrders") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BatchCreateDispatchOrders", request)
            log.atInfo().addKeyValue("operation", "BatchCreateDispatchOrders").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.header.source).log("Recorded audit detail")
            var reply0 = NormalizeDispatchOrderResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.record.linesList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound0 = normalizeDispatchOrderRequest {
                                this.context = request.context
                                this.dispatchOrder = dispatchOrder {
                                    this.lines.addAll(listOf(dispatchOrderLine {
                                        this.lineId = item.lineId
                                        this.tax = dispatchOrderLineTax {
                                            this.amount = item.tax.amount
                                            this.jurisdiction = item.tax.jurisdiction
                                        }
                                        this.unitPrice = item.unitPrice
                                    }))
                                }
                            }
                            dispatchDispatchOrderValidationService.normalizeDispatchOrder(outbound0)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply0 = it }
                }
            }
            val outbound1 = browseNotificationsRequest {
                this.context = request.context
                this.filter = BrowseNotificationsRequestKt.filter {
                    this.createdAfter = reply0.dispatchOrder.audit.updatedAt
                    this.labels.putAll(request.record.metadataMap)
                    this.region = reply0.dispatchOrder.billingAddress.region
                }
            }
            val reply1 = notificationsNotificationQueryService.browseNotifications(outbound1)
            val outbound2 = resolveDispatchAssignmentsRequest {}.dispatchCopy {
                this.context = request.context
            }
            val reply2 = dispatchDispatchAssignmentQueryService.resolveDispatchAssignments(outbound2)
            val outbound3 = request.toValidateDispatchAssignmentFromBatchCreateDispatchOrders3(reply0)
            val reply3 = dispatchDispatchAssignmentValidationService.validateDispatchAssignment(outbound3)
            batchCreateDispatchOrdersResponse {
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = reply2.result.status
                }
            }
        }

    private suspend fun handleBatchGetDispatchOrders(request: BatchGetDispatchOrdersRequest): BatchGetDispatchOrdersResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderService/BatchGetDispatchOrders") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BatchGetDispatchOrders", request)
            log.atInfo().addKeyValue("operation", "BatchGetDispatchOrders").log("Handling transport request")
            val outbound0 = browseDispatchAssignmentsRequest {
                this.context = request.context
                this.filter = BrowseDispatchAssignmentsRequestKt.filter {
                    this.createdAfter = request.context.deadline
                    this.labels.putAll(request.context.baggageMap)
                    this.ownerId = request.context.actor.userId
                }
            }
            val reply0 = dispatchDispatchAssignmentQueryService.browseDispatchAssignments(outbound0)
            batchGetDispatchOrdersResponse {
                this.pageInfo = pageInfo {
                    this.totalSize = reply0.pageInfo.totalSize
                }
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleCreateDispatchOrder(request: CreateDispatchOrderRequest): CreateDispatchOrderResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderService/CreateDispatchOrder") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CreateDispatchOrder", request)
            log.atInfo().addKeyValue("operation", "CreateDispatchOrder").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchOrder.dispatchOrderId).log("Recorded audit detail")
            val outbound0 = browseDispatchOrdersRequest {
                this.context = request.context
                this.filter = BrowseDispatchOrdersRequestKt.filter {
                    this.createdAfter = request.dispatchOrder.audit.updatedAt
                    this.region = request.dispatchOrder.billingAddress.region
                }
            }
            val command0: Submission<BrowseDispatchOrdersRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = dispatchDispatchOrderQueryService.browseDispatchOrders(payload0)
            val outbound1 = countServiceSchedulesRequest {
                this.context = request.context
                this.filter = CountServiceSchedulesRequestKt.filter {
                    this.createdAfter = request.dispatchOrder.audit.updatedAt
                    this.labels.putAll(reply0.result.detailsMap)
                    this.region = request.dispatchOrder.billingAddress.region
                }
            }
            val adapter1 = OutboundPort<CountServiceSchedulesRequest, CountServiceSchedulesResponse> { payload ->
                maintenanceServiceScheduleQueryService.countServiceSchedules(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            val outbound2 = browseDispatchAssignmentsRequest {
                this.context = request.context
                this.filter = BrowseDispatchAssignmentsRequestKt.filter {
                    this.createdAfter = request.dispatchOrder.dueAt
                    this.ownerId = request.dispatchOrder.counterparty.zoneId
                    this.region = request.dispatchOrder.billingAddress.region
                }
            }
            val reply2 = invokeCreateDispatchOrder2(outbound2)
            var reply3 = SuggestFuelCardsResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<DispatchOrderLine>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound3 = suggestFuelCardsRequest {
                            this.context = request.context
                            this.filter = SuggestFuelCardsRequestKt.filter {
                                this.labels.putAll(item.attributesMap)
                                this.ownerId = item.lineId
                                this.region = request.dispatchOrder.billingAddress.region
                            }
                        }
                        reply3 = fuelFuelCardQueryService.suggestFuelCards(outbound3)
                    }
                }
                try {
                    request.dispatchOrder.linesList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            createDispatchOrderResponse {
                this.dispatchOrder = dispatchOrder {
                    this.dueAt = request.dispatchOrder.dueAt
                    this.number = request.dispatchOrder.number
                }
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.referenceIds.addAll(reply2.result.referenceIdsList)
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleDeleteDispatchOrder(request: DeleteDispatchOrderRequest): DeleteDispatchOrderResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderService/DeleteDispatchOrder") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DeleteDispatchOrder", request)
            log.atInfo().addKeyValue("operation", "DeleteDispatchOrder").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchOrderId).log("Recorded audit detail")
            val outbound0 = ComparePrescriptionOptionsRequest.newBuilder().apply {
                setContext(request.context)
                setPrescription(prescription {
                    this.audit = auditInfo {
                        this.etag = request.etag
                    }
                })
                setReason(request.reason)
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> prescriptionsPrescriptionCalculationService.comparePrescriptionOptions(value) }
            val outbound1 = RequestFactory.create {
                lintZonesRequest {
                    this.context = request.context
                    this.zone = zone {
                        this.children.addAll(listOf(zone {
                            this.children.addAll(listOf(zone {
                                this.audit = auditInfo {
                                    this.createdAt = reply0.prescription.audit.createdAt
                                }
                                this.location = postalAddress {
                                    this.countryCode = reply0.prescription.billingAddress.countryCode
                                }
                                this.manager = userRef {
                                    this.email = reply0.prescription.counterparty.email
                                }
                            }))
                            this.location = postalAddress {
                                this.lines.addAll(reply0.prescription.billingAddress.linesList)
                            }
                        }))
                    }
                }
            }
            val reply1 = dispatchZoneValidationService.lintZones(outbound1)
            val outbound2 = searchCryptoKeysRequest {
                this.context = request.context
                this.filter = SearchCryptoKeysRequestKt.filter {
                    this.region = reply0.prescription.billingAddress.region
                }
            }
            val reply2 = ClientRegistry.lookup("acme.keymanagement.v1.CryptoKeyService", CryptoKeyServiceGrpcKt.CryptoKeyServiceCoroutineStub::class.java).searchCryptoKeys(outbound2)
            deleteDispatchOrderResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.message = reply2.result.message
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleSearchDispatchOrders(request: SearchDispatchOrdersRequest): SearchDispatchOrdersResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderService/SearchDispatchOrders") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SearchDispatchOrders", request)
            log.atInfo().addKeyValue("operation", "SearchDispatchOrders").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = lintGeoFencesRequest {
                this.context = request.context
                this.details = LintGeoFencesRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                    this.locale = request.context.locale
                }
                this.geoFence = geoFence {
                    this.owner = userRef {
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply0 = telematicsGeoFenceValidationService.lintGeoFences(WireCopies.reflective(outbound0))
            val outbound1 = createFuelCardRequest {
                this.context = request.context
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.etag = reply0.geoFence.audit.etag
                    }
                    this.holder = driverRef {
                        this.email = reply0.geoFence.owner.email
                    }
                }
            }
            val reply1 = workerForSearchDispatchOrders1.submit(outbound1)
            searchDispatchOrdersResponse {
                this.dispatchOrders.addAll(listOf(dispatchOrder {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.geoFence.audit.updatedBy
                    }
                }))
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleUpdateDispatchOrder(request: UpdateDispatchOrderRequest): UpdateDispatchOrderResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderService/UpdateDispatchOrder") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "UpdateDispatchOrder", request)
            log.atInfo().addKeyValue("operation", "UpdateDispatchOrder").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dispatchOrder.dispatchOrderId).log("Recorded audit detail")
            val outbound0 = browseSearchIndexesRequest {
                this.context = request.context
                this.filter = BrowseSearchIndexesRequestKt.filter {
                    this.createdAfter = request.dispatchOrder.dueAt
                    this.region = request.dispatchOrder.billingAddress.region
                }
            }
            val reply0 = clientForUpdateDispatchOrder0.browseSearchIndexes(outbound0)
            updateDispatchOrderResponse {
                this.dispatchOrder = dispatchOrder {
                    this.issuedAt = request.dispatchOrder.issuedAt
                    this.notes = request.dispatchOrder.notes
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleWatchDispatchOrders(request: WatchDispatchOrdersRequest): WatchDispatchOrdersResponse =
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderService/WatchDispatchOrders") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "WatchDispatchOrders", request)
            log.atInfo().addKeyValue("operation", "WatchDispatchOrders").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = previewDispatchOrderRequest {
                this.context = request.context
                this.details = PreviewDispatchOrderRequestKt.details {
                    this.contact = contactInfo {
                        this.preferredChannel = request.subscriber.preferredChannel
                    }
                }
            }
            val reply0 = flowOf(outbound0).map { value -> dispatchDispatchOrderValidationService.previewDispatchOrder(value) }.single()
            val outbound1 = estimateDispatchAssignmentRequest {
                this.context = request.context
                this.dispatchAssignment = dispatchAssignment {
                    this.assignee = userRef {
                        this.displayName = reply0.dispatchOrder.counterparty.displayName
                    }
                }
            }
            val reply1 = withTimeout(5_000) { dispatchDispatchAssignmentCalculationService.estimateDispatchAssignment(outbound1) }
            val configure2: EstimateFuelTransactionRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.details = EstimateFuelTransactionRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = reply0.dispatchOrder.billingAddress.countryCode
                        this.lines.addAll(reply0.dispatchOrder.billingAddress.linesList)
                    }
                }
                this.fuelTransaction = fuelTransaction {
                    this.subject = customerRef {
                        this.displayName = reply1.dispatchAssignment.customer.displayName
                        this.email = reply1.dispatchAssignment.assignee.email
                    }
                }
            }
            val outbound2 = estimateFuelTransactionRequest(configure2)
            val reply2 = fuelFuelTransactionCalculationService.estimateFuelTransaction(outbound2)
            watchDispatchOrdersResponse {
                this.dispatchOrder = dispatchOrder {
                    this.audit = auditInfo {
                        this.createdBy = reply1.dispatchAssignment.audit.createdBy
                        this.updatedAt = reply0.dispatchOrder.audit.updatedAt
                    }
                    this.billingAddress = postalAddress {
                        this.region = request.filter.region
                    }
                    this.total = reply0.dispatchOrder.total
                }
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = "ok"
                }
            }
        }
}
