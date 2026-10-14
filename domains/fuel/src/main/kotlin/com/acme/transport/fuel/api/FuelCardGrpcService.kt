package com.acme.transport.fuel.api

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.pageInfo
import com.acme.common.v1.pageRequest
import com.acme.customers.v1.customerRef
import com.acme.deliveries.v1.DeliveryServiceGrpcKt
import com.acme.deliveries.v1.UpdateDeliveryRequest
import com.acme.deliveries.v1.delivery
import com.acme.deliveries.v1.deliveryLine
import com.acme.deliveries.v1.updateDeliveryRequest
import com.acme.drivers.v1.driverRef
import com.acme.fuel.v1.BatchCreateFuelCardsRequest
import com.acme.fuel.v1.BatchCreateFuelCardsResponse
import com.acme.fuel.v1.BatchGetFuelCardsRequest
import com.acme.fuel.v1.BatchGetFuelCardsResponse
import com.acme.fuel.v1.CloneFuelCardRequest
import com.acme.fuel.v1.CloneFuelCardResponse
import com.acme.fuel.v1.CreateFuelCardRequest
import com.acme.fuel.v1.CreateFuelCardResponse
import com.acme.fuel.v1.FuelCardCalculationServiceGrpcKt
import com.acme.fuel.v1.FuelCardQueryServiceGrpcKt
import com.acme.fuel.v1.FuelCardServiceGrpcKt
import com.acme.fuel.v1.FuelCardValidationServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyHistoryServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionValidationServiceGrpcKt
import com.acme.fuel.v1.GetFuelCardRequest
import com.acme.fuel.v1.GetFuelCardResponse
import com.acme.fuel.v1.ListFuelCardsRequest
import com.acme.fuel.v1.ListFuelCardsResponse
import com.acme.fuel.v1.ListFuelPolicyVersionsRequest
import com.acme.fuel.v1.ListFuelPolicyVersionsRequestKt
import com.acme.fuel.v1.SearchFuelCardsRequest
import com.acme.fuel.v1.SearchFuelCardsResponse
import com.acme.fuel.v1.SimulateFuelCardRequest
import com.acme.fuel.v1.SimulateFuelCardResponse
import com.acme.fuel.v1.SuggestFuelCardsRequestKt
import com.acme.fuel.v1.ValidateFuelTransactionRequest
import com.acme.fuel.v1.ValidateFuelTransactionRequestKt
import com.acme.fuel.v1.WatchFuelCardsRequest
import com.acme.fuel.v1.WatchFuelCardsResponse
import com.acme.fuel.v1.batchCreateFuelCardsResponse
import com.acme.fuel.v1.batchGetFuelCardsResponse
import com.acme.fuel.v1.cloneFuelCardResponse
import com.acme.fuel.v1.createFuelCardResponse
import com.acme.fuel.v1.explainFuelCardDecisionRequest
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.fuelCardBankFunding
import com.acme.fuel.v1.fuelCardLimit
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.getFuelCardResponse
import com.acme.fuel.v1.listFuelCardsResponse
import com.acme.fuel.v1.listFuelPolicyVersionsRequest
import com.acme.fuel.v1.lookupFuelCardRequest
import com.acme.fuel.v1.revertFuelPolicyRequest
import com.acme.fuel.v1.searchFuelCardsResponse
import com.acme.fuel.v1.simulateFuelCardRequest
import com.acme.fuel.v1.suggestFuelCardsRequest
import com.acme.fuel.v1.validateFuelTransactionRequest
import com.acme.fuel.v1.watchFuelCardsResponse
import com.acme.identity.v1.userRef
import com.acme.notifications.v1.CheckNotificationEligibilityRequest
import com.acme.notifications.v1.CheckNotificationEligibilityRequestKt
import com.acme.notifications.v1.CheckNotificationEligibilityResponse
import com.acme.notifications.v1.NotificationValidationServiceGrpcKt
import com.acme.notifications.v1.PreviewNotificationRequestKt
import com.acme.notifications.v1.checkNotificationEligibilityRequest
import com.acme.notifications.v1.notification
import com.acme.notifications.v1.notificationComment
import com.acme.notifications.v1.previewNotificationRequest
import com.acme.search.v1.CheckSearchIndexEligibilityRequestKt
import com.acme.search.v1.PreviewSearchIndexResponse
import com.acme.search.v1.SearchIndexValidationServiceGrpcKt
import com.acme.search.v1.checkSearchIndexEligibilityRequest
import com.acme.search.v1.copy as searchCopy
import com.acme.search.v1.previewSearchIndexRequest
import com.acme.search.v1.searchIndex
import com.acme.transport.fuel.FuelRepository
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.CountYardsRequestKt
import com.acme.yards.v1.YardQueryServiceGrpcKt
import com.acme.yards.v1.countYardsRequest
import com.google.protobuf.Any as ProtoAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Manages fuel cards: creates, reads, changes, and lists them. */
class FuelCardGrpcService(
    private val store: FuelRepository,
    private val workers: CoroutineScope,
    private val deliveriesDeliveryService: DeliveryServiceGrpcKt.DeliveryServiceCoroutineStub,
    private val fuelFuelCardCalculationService: FuelCardCalculationServiceGrpcKt.FuelCardCalculationServiceCoroutineStub,
    private val fuelFuelCardQueryService: FuelCardQueryServiceGrpcKt.FuelCardQueryServiceCoroutineStub,
    private val fuelFuelCardValidationService: FuelCardValidationServiceGrpcKt.FuelCardValidationServiceCoroutineStub,
    private val fuelFuelPolicyHistoryService: FuelPolicyHistoryServiceGrpcKt.FuelPolicyHistoryServiceCoroutineStub,
    private val fuelFuelTransactionValidationService: FuelTransactionValidationServiceGrpcKt.FuelTransactionValidationServiceCoroutineStub,
    private val notificationsNotificationValidationService: NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub,
    private val searchSearchIndexValidationService: SearchIndexValidationServiceGrpcKt.SearchIndexValidationServiceCoroutineStub,
    private val yardsYardQueryService: YardQueryServiceGrpcKt.YardQueryServiceCoroutineStub,
) : FuelCardServiceGrpcKt.FuelCardServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(FuelCardGrpcService::class.java)

    private val clientForBatchCreateFuelCards0 by lazy { notificationsNotificationValidationService }

    /** Takes a stream of fuel cards and answers once at the end. */
    override suspend fun batchCreateFuelCards(requests: Flow<BatchCreateFuelCardsRequest>): BatchCreateFuelCardsResponse {
        var answer = BatchCreateFuelCardsResponse.getDefaultInstance()
        requests.map { request -> handleBatchCreateFuelCards(request) }.collect { answer = it }
        return answer
    }

    /** Returns several fuel cards at once. */
    override suspend fun batchGetFuelCards(request: BatchGetFuelCardsRequest): BatchGetFuelCardsResponse = handleBatchGetFuelCards(request)

    /** Creates a fuel card. */
    override suspend fun cloneFuelCard(request: CloneFuelCardRequest): CloneFuelCardResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardService/CloneFuelCard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CloneFuelCard", request)
            log.atInfo().addKeyValue("operation", "CloneFuelCard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCard.currencyCode).log("Recorded audit detail")
            val outbound0 = revertFuelPolicyRequest {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.etag = request.fuelCard.audit.etag
                    }
                }
            }
            val reply0 = fuelFuelPolicyHistoryService.revertFuelPolicy(outbound0)
            val outbound1 = countYardsRequest {
                this.context = request.context
                this.filter = CountYardsRequestKt.filter {
                    this.labels.putAll(request.fuelCard.labelsMap)
                }
            }
            val reply1 = yardsYardQueryService.countYards(outbound1)
            cloneFuelCardResponse {
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.createdBy = reply0.fuelPolicy.audit.createdBy
                    }
                    this.fuelCardId = request.fuelCard.fuelCardId
                    this.holder = request.fuelCard.holder
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    /** Creates a fuel card. */
    override suspend fun createFuelCard(request: CreateFuelCardRequest): CreateFuelCardResponse {
        return handleCreateFuelCard(request)
    }

    /** Returns a fuel card. */
    override suspend fun getFuelCard(request: GetFuelCardRequest): GetFuelCardResponse = handleGetFuelCard(request)

    /** Lists fuel cards a page at a time. */
    override suspend fun listFuelCards(request: ListFuelCardsRequest): ListFuelCardsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardService/ListFuelCards") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListFuelCards", request)
            log.atInfo().addKeyValue("operation", "ListFuelCards").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = suggestFuelCardsRequest {
                this.context = request.context
                this.filter = SuggestFuelCardsRequestKt.filter {
                    this.labels.putAll(request.filter.labelsMap)
                    this.statuses.addAll(request.filter.statusesList)
                }
                this.page = pageRequest {
                    this.pageToken = request.page.pageToken
                }
            }
            val reply0 = fuelFuelCardQueryService.suggestFuelCards(WireCopies.json(outbound0))
            listFuelCardsResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    /** Lists fuel cards a page at a time. */
    override suspend fun searchFuelCards(request: SearchFuelCardsRequest): SearchFuelCardsResponse {
        return handleSearchFuelCards(request)
    }

    /** Streams the changes of fuel cards as they happen. */
    override fun watchFuelCards(request: WatchFuelCardsRequest): Flow<WatchFuelCardsResponse> = flow {
        emit(handleWatchFuelCards(request))
    }

    private suspend fun handleBatchCreateFuelCards(request: BatchCreateFuelCardsRequest): BatchCreateFuelCardsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardService/BatchCreateFuelCards") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BatchCreateFuelCards", request)
            log.atInfo().addKeyValue("operation", "BatchCreateFuelCards").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.header.source).log("Recorded audit detail")
            val outbound0 = previewNotificationRequest {
                this.context = request.context
                this.details = PreviewNotificationRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.header.submittedBy.email
                    }
                }
                this.notification = notification {
                    this.assignee = userRef {
                        this.email = request.record.contact.email
                    }
                    this.comments.addAll(listOf(notificationComment {
                        this.createdAt = request.record.audit.createdAt
                    }))
                    this.requester = contactInfo {
                        this.name = request.record.contact.name
                    }
                }
            }
            val reply0 = clientForBatchCreateFuelCards0.previewNotification(outbound0)
            var reply1 = SimulateFuelCardResponse.getDefaultInstance()
            request.record.limitsList.asFlow().map { item ->
                val outbound1 = simulateFuelCardRequest {
                    this.context = request.context
                    this.fuelCard = fuelCard {
                        this.bank = fuelCardBankFunding {
                            this.accountHolder = request.record.bank.accountHolder
                        }
                        this.limits.addAll(listOf(fuelCardLimit {
                            this.amount = item.amount
                            this.name = item.name
                            this.period = item.period
                        }))
                    }
                }
                fuelFuelCardCalculationService.simulateFuelCard(outbound1)
            }.collect { reply1 = it }
            batchCreateFuelCardsResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.message = reply0.outcome.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleBatchGetFuelCards(request: BatchGetFuelCardsRequest): BatchGetFuelCardsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardService/BatchGetFuelCards") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BatchGetFuelCards", request)
            log.atInfo().addKeyValue("operation", "BatchGetFuelCards").log("Handling transport request")
            val outbound0 = explainFuelCardDecisionRequest {
                this.context = request.context
                this.fuelCard = fuelCard {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = withTimeout(5_000) { fuelFuelCardValidationService.explainFuelCardDecision(outbound0) }
            val configure1: CheckSearchIndexEligibilityRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.details = CheckSearchIndexEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.name = reply0.fuelCard.contact.name
                    }
                }
                this.searchIndex = searchIndex {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.fuelCard.audit.updatedBy
                    }
                }
            }
            val outbound1 = checkSearchIndexEligibilityRequest(configure1)
            val reply1 = searchSearchIndexValidationService.checkSearchIndexEligibility(outbound1)
            batchGetFuelCardsResponse {
                this.fuelCards.addAll(listOf(fuelCard {
                    this.audit = auditInfo {
                        this.createdBy = reply0.fuelCard.audit.createdBy
                        this.updatedBy = reply1.searchIndex.audit.updatedBy
                    }
                }))
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = reply0.result.status
                }
            }
        }

    private fun CreateFuelCardRequest.toSimulateFuelCardFromCreateFuelCard1(reply0: PreviewSearchIndexResponse): SimulateFuelCardRequest {
        val source = this
        return simulateFuelCardRequest {
            this.context = source.context
            this.fuelCard = fuelCard {
                this.audit = auditInfo {
                    this.etag = reply0.searchIndex.audit.etag
                    this.updatedBy = source.fuelCard.audit.updatedBy
                }
            }
        }
    }

    private suspend fun handleCreateFuelCard(request: CreateFuelCardRequest): CreateFuelCardResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardService/CreateFuelCard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CreateFuelCard", request)
            log.atInfo().addKeyValue("operation", "CreateFuelCard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCard.holder.driverId).log("Recorded audit detail")
            val outbound0 = previewSearchIndexRequest {}.searchCopy {
                this.context = request.context
                this.searchIndex = searchIndex {
                    this.owner = userRef {
                        this.displayName = request.fuelCard.holder.displayName
                        this.email = request.fuelCard.contact.email
                    }
                }
            }
            val reply0 = searchSearchIndexValidationService.previewSearchIndex(outbound0)
            val outbound1 = request.toSimulateFuelCardFromCreateFuelCard1(reply0)
            val reply1 = fuelFuelCardCalculationService.simulateFuelCard(outbound1)
            val outbound2 = lookupFuelCardRequest {
                this.context = request.context
                this.fuelCardId = request.fuelCard.fuelCardId
            }
            val reply2 = fuelFuelCardQueryService.lookupFuelCard(outbound2)
            createFuelCardResponse {
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.searchIndex.audit.updatedAt
                    }
                    this.contact = request.fuelCard.contact
                    this.holder = driverRef {
                        this.email = reply1.fuelCard.holder.email
                    }
                    this.openedAt = request.fuelCard.openedAt
                }
                this.result = operationResult {
                    this.details.putAll(reply2.result.detailsMap)
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleGetFuelCard(request: GetFuelCardRequest): GetFuelCardResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardService/GetFuelCard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetFuelCard", request)
            log.atInfo().addKeyValue("operation", "GetFuelCard").log("Handling transport request")
            val outbound0 = updateDeliveryRequest {
                this.context = request.context
                this.delivery = delivery {
                    this.audit = auditInfo {
                        this.updatedAt = request.context.deadline
                    }
                    this.lines.addAll(listOf(deliveryLine {
                        this.attributes.putAll(request.context.baggageMap)
                    }))
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = deliveriesDeliveryService.updateDelivery(envelope0.unpack(UpdateDeliveryRequest::class.java))
            getFuelCardResponse {
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.delivery.audit.updatedAt
                    }
                    this.fuelCardId = request.fuelCardId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private data class SearchFuelCards0Command(
        val context: RequestContext,
        val fuelTransactionSubjectEmail: String,
        val detailsLocale: String
    ) {
        fun toRequest(): ValidateFuelTransactionRequest {
            val command = this
            return validateFuelTransactionRequest {
                this.context = command.context
                this.details = ValidateFuelTransactionRequestKt.details {
                    this.locale = command.detailsLocale
                }
                this.fuelTransaction = fuelTransaction {
                    this.subject = customerRef {
                        this.email = command.fuelTransactionSubjectEmail
                    }
                }
            }
        }
    }

    private suspend fun handleSearchFuelCards(request: SearchFuelCardsRequest): SearchFuelCardsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardService/SearchFuelCards") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SearchFuelCards", request)
            log.atInfo().addKeyValue("operation", "SearchFuelCards").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.ownerId).log("Recorded audit detail")
            val outbound0 = SearchFuelCards0Command(
                context = request.context,
                fuelTransactionSubjectEmail = request.context.actor.email,
                detailsLocale = request.context.locale
            ).toRequest()
            val reply0 = fuelFuelTransactionValidationService.validateFuelTransaction(outbound0)
            val outbound1 = listFuelPolicyVersionsRequest {
                this.context = request.context
                this.filter = ListFuelPolicyVersionsRequestKt.filter {
                    this.query = request.filter.query
                }
                this.page = pageRequest {
                    this.pageSize = request.page.pageSize
                    this.pageToken = request.page.pageToken
                }
            }
            val command1: Submission<ListFuelPolicyVersionsRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = fuelFuelPolicyHistoryService.listFuelPolicyVersions(payload1)
            searchFuelCardsResponse {
                this.pageInfo = pageInfo {
                    this.nextPageToken = reply1.pageInfo.nextPageToken
                }
                this.result = operationResult {
                    this.status = reply0.outcome.status
                }
            }
        }

    private suspend fun handleWatchFuelCards(request: WatchFuelCardsRequest): WatchFuelCardsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardService/WatchFuelCards") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "WatchFuelCards", request)
            log.atInfo().addKeyValue("operation", "WatchFuelCards").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = checkNotificationEligibilityRequest {
                this.context = request.context
                this.details = CheckNotificationEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.preferredChannel = request.subscriber.preferredChannel
                    }
                }
                this.notification = notification {
                    this.requester = contactInfo {
                        this.email = request.subscriber.email
                        this.name = request.subscriber.name
                        this.phone = request.subscriber.phone
                    }
                }
            }
            val adapter0 = OutboundPort<CheckNotificationEligibilityRequest, CheckNotificationEligibilityResponse> { payload ->
                notificationsNotificationValidationService.checkNotificationEligibility(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            watchFuelCardsResponse {
                this.fuelCard = fuelCard {
                    this.contact = contactInfo {
                        this.name = reply0.notification.requester.name
                        this.phone = request.subscriber.phone
                    }
                    this.holder = driverRef {
                        this.email = request.subscriber.email
                    }
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }
}
