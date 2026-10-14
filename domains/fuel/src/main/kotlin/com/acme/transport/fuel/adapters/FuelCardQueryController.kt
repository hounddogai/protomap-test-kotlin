package com.acme.transport.fuel.adapters

import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.pageInfo
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.customerRef
import com.acme.drivers.v1.driverRef
import com.acme.fuel.v1.BrowseFuelCardsRequest
import com.acme.fuel.v1.BrowseFuelCardsResponse
import com.acme.fuel.v1.CalculateFuelCardRequest
import com.acme.fuel.v1.CalculateFuelCardResponse
import com.acme.fuel.v1.CompareFuelPolicyOptionsRequestKt
import com.acme.fuel.v1.CountFuelCardsRequest
import com.acme.fuel.v1.CountFuelCardsResponse
import com.acme.fuel.v1.ExportFuelCardsRequest
import com.acme.fuel.v1.ExportFuelCardsResponse
import com.acme.fuel.v1.FindFuelCardsRequest
import com.acme.fuel.v1.FindFuelCardsResponse
import com.acme.fuel.v1.FuelCardCalculationServiceGrpcKt
import com.acme.fuel.v1.FuelCardQueryServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyCalculationServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyHistoryServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyValidationServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionCalculationServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionValidationServiceGrpcKt
import com.acme.fuel.v1.ListFuelPolicyVersionsRequestKt
import com.acme.fuel.v1.LookupFuelCardRequest
import com.acme.fuel.v1.LookupFuelCardResponse
import com.acme.fuel.v1.PeekFuelCardRequest
import com.acme.fuel.v1.PeekFuelCardResponse
import com.acme.fuel.v1.ResolveFuelCardsRequest
import com.acme.fuel.v1.ResolveFuelCardsResponse
import com.acme.fuel.v1.SuggestFuelCardsRequest
import com.acme.fuel.v1.SuggestFuelCardsResponse
import com.acme.fuel.v1.SummarizeFuelCardsRequest
import com.acme.fuel.v1.SummarizeFuelCardsResponse
import com.acme.fuel.v1.ValidateFuelTransactionRequestKt
import com.acme.fuel.v1.browseFuelCardsResponse
import com.acme.fuel.v1.calculateFuelCardRequest
import com.acme.fuel.v1.calculateFuelTransactionRequest
import com.acme.fuel.v1.compareFuelPolicyOptionsRequest
import com.acme.fuel.v1.countFuelCardsResponse
import com.acme.fuel.v1.exportFuelCardsResponse
import com.acme.fuel.v1.findFuelCardsResponse
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.fuelTransactionMeasurement
import com.acme.fuel.v1.listFuelPolicyVersionsRequest
import com.acme.fuel.v1.lookupFuelCardResponse
import com.acme.fuel.v1.normalizeFuelTransactionRequest
import com.acme.fuel.v1.peekFuelCardResponse
import com.acme.fuel.v1.resolveFuelCardsResponse
import com.acme.fuel.v1.scoreFuelPolicyRequest
import com.acme.fuel.v1.scoreFuelTransactionRequest
import com.acme.fuel.v1.suggestFuelCardsResponse
import com.acme.fuel.v1.summarizeFuelCardsResponse
import com.acme.fuel.v1.validateFuelTransactionRequest
import com.acme.identity.v1.userRef
import com.acme.transport.fuel.FuelRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.SimulateYardMoveRequestKt
import com.acme.yards.v1.YardMoveCalculationServiceGrpcKt
import com.acme.yards.v1.simulateYardMoveRequest
import com.acme.yards.v1.yardMove
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Answers read-only questions about fuel cards, for screens and reports. */
class FuelCardQueryController(
    private val store: FuelRepository,
    private val workers: CoroutineScope,
    private val fuelFuelCardCalculationService: FuelCardCalculationServiceGrpcKt.FuelCardCalculationServiceCoroutineStub,
    private val fuelFuelPolicyCalculationService: FuelPolicyCalculationServiceGrpcKt.FuelPolicyCalculationServiceCoroutineStub,
    private val fuelFuelPolicyHistoryService: FuelPolicyHistoryServiceGrpcKt.FuelPolicyHistoryServiceCoroutineStub,
    private val fuelFuelPolicyValidationService: FuelPolicyValidationServiceGrpcKt.FuelPolicyValidationServiceCoroutineStub,
    private val fuelFuelTransactionCalculationService: FuelTransactionCalculationServiceGrpcKt.FuelTransactionCalculationServiceCoroutineStub,
    private val fuelFuelTransactionValidationService: FuelTransactionValidationServiceGrpcKt.FuelTransactionValidationServiceCoroutineStub,
    private val yardsYardMoveCalculationService: YardMoveCalculationServiceGrpcKt.YardMoveCalculationServiceCoroutineStub,
) : FuelCardQueryServiceGrpcKt.FuelCardQueryServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(FuelCardQueryController::class.java)

    private val workerForLookupFuelCard0 = ReplyMailbox<CalculateFuelCardRequest, CalculateFuelCardResponse>(workers) { payload ->
        withTimeout(5_000) { fuelFuelCardCalculationService.calculateFuelCard(payload) }
    }

    private val clientForLookupFuelCard1 by lazy { fuelFuelTransactionValidationService }

    /** Lists fuel cards a page at a time. */
    override suspend fun browseFuelCards(request: BrowseFuelCardsRequest): BrowseFuelCardsResponse = handleBrowseFuelCards(request)

    /** Counts fuel cards by status. */
    override suspend fun countFuelCards(request: CountFuelCardsRequest): CountFuelCardsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardQueryService/CountFuelCards") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CountFuelCards", request)
            log.atInfo().addKeyValue("operation", "CountFuelCards").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = simulateYardMoveRequest {
                this.context = request.context
                this.details = SimulateYardMoveRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.filter.region
                    }
                }
                this.yardMove = yardMove {
                    this.assignee = userRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = ClientRegistry.lookup("acme.yards.v1.YardMoveCalculationService", YardMoveCalculationServiceGrpcKt.YardMoveCalculationServiceCoroutineStub::class.java).simulateYardMove(outbound0)
            countFuelCardsResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    /** Streams every matching fuel card. */
    override fun exportFuelCards(request: ExportFuelCardsRequest): Flow<ExportFuelCardsResponse> = flow {
        emit(handleExportFuelCards(request))
    }

    /** Lists fuel cards a page at a time. */
    override suspend fun findFuelCards(request: FindFuelCardsRequest): FindFuelCardsResponse = handleFindFuelCards(request)

    /** Returns a fuel card. */
    override suspend fun lookupFuelCard(request: LookupFuelCardRequest): LookupFuelCardResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardQueryService/LookupFuelCard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LookupFuelCard", request)
            log.atInfo().addKeyValue("operation", "LookupFuelCard").log("Handling transport request")
            val outbound0 = calculateFuelCardRequest {
                this.context = request.context
                this.fuelCard = fuelCard {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                    this.fuelCardId = request.fuelCardId
                }
            }
            val reply0 = workerForLookupFuelCard0.submit(outbound0)
            val outbound1 = validateFuelTransactionRequest {
                this.context = request.context
                this.fuelTransaction = fuelTransaction {
                    this.subject = customerRef {
                        this.email = reply0.fuelCard.holder.email
                    }
                }
            }
            val reply1 = clientForLookupFuelCard1.validateFuelTransaction(outbound1)
            lookupFuelCardResponse {
                this.fuelCard = fuelCard {
                    this.currencyCode = reply0.breakdown.currencyCode
                    this.fuelCardId = request.fuelCardId
                    this.holder = driverRef {
                        this.displayName = reply1.fuelTransaction.subject.displayName
                    }
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = reply1.outcome.status
                }
            }
        }

    /** Returns a fuel card. */
    override suspend fun peekFuelCard(request: PeekFuelCardRequest): PeekFuelCardResponse {
        return handlePeekFuelCard(request)
    }

    /** Returns several fuel cards at once. */
    override suspend fun resolveFuelCards(request: ResolveFuelCardsRequest): ResolveFuelCardsResponse = handleResolveFuelCards(request)

    /** Lists fuel cards a page at a time. */
    override suspend fun suggestFuelCards(request: SuggestFuelCardsRequest): SuggestFuelCardsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardQueryService/SuggestFuelCards") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SuggestFuelCards", request)
            log.atInfo().addKeyValue("operation", "SuggestFuelCards").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = listFuelPolicyVersionsRequest {
                this.context = request.context
                this.filter = ListFuelPolicyVersionsRequestKt.filter {
                    this.createdAfter = request.filter.createdAfter
                    this.labels.putAll(request.filter.labelsMap)
                }
            }
            val reply0 = withTimeout(5_000) { fuelFuelPolicyHistoryService.listFuelPolicyVersions(outbound0) }
            val configure1: CompareFuelPolicyOptionsRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.owner = userRef {
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val outbound1 = compareFuelPolicyOptionsRequest(configure1)
            val reply1 = fuelFuelPolicyCalculationService.compareFuelPolicyOptions(outbound1)
            suggestFuelCardsResponse {
                this.fuelCards.addAll(listOf(fuelCard {
                    this.holder = driverRef {
                        this.displayName = reply1.fuelPolicy.owner.displayName
                    }
                }))
                this.pageInfo = pageInfo {
                    this.totalSize = reply0.pageInfo.totalSize
                }
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }

    /** Summarizes a fuel card. */
    override suspend fun summarizeFuelCards(request: SummarizeFuelCardsRequest): SummarizeFuelCardsResponse {
        return handleSummarizeFuelCards(request)
    }

    private suspend fun handleBrowseFuelCards(request: BrowseFuelCardsRequest): BrowseFuelCardsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardQueryService/BrowseFuelCards") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BrowseFuelCards", request)
            log.atInfo().addKeyValue("operation", "BrowseFuelCards").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                validateFuelTransactionRequest {
                    this.context = request.context
                    this.details = ValidateFuelTransactionRequestKt.details {
                        this.contact = contactInfo {
                            this.email = request.context.actor.email
                        }
                        this.locale = request.context.locale
                    }
                    this.fuelTransaction = fuelTransaction {
                        this.measurements.addAll(listOf(fuelTransactionMeasurement {
                            this.observedAt = request.filter.createdAfter
                        }))
                    }
                }
            }
            val reply0 = fuelFuelTransactionValidationService.validateFuelTransaction(outbound0)
            browseFuelCardsResponse {
                this.fuelCards.addAll(listOf(fuelCard {
                    this.contact = contactInfo {
                        this.email = reply0.fuelTransaction.subject.email
                    }
                }))
                this.result = operationResult {
                    this.status = reply0.outcome.status
                }
            }
        }

    private suspend fun handleExportFuelCards(request: ExportFuelCardsRequest): ExportFuelCardsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardQueryService/ExportFuelCards") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExportFuelCards", request)
            log.atInfo().addKeyValue("operation", "ExportFuelCards").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = normalizeFuelTransactionRequest {
                this.context = request.context
                this.fuelTransaction = fuelTransaction {
                    this.subject = customerRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = fuelFuelTransactionValidationService.normalizeFuelTransaction(WireCopies.reflective(outbound0))
            exportFuelCardsResponse {
                this.fuelCard = fuelCard {
                    this.labels.putAll(request.filter.labelsMap)
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.sequence = reply0.fuelTransaction.sequence
            }
        }

    private suspend fun handleFindFuelCards(request: FindFuelCardsRequest): FindFuelCardsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardQueryService/FindFuelCards") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "FindFuelCards", request)
            log.atInfo().addKeyValue("operation", "FindFuelCards").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            findFuelCardsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handlePeekFuelCard(request: PeekFuelCardRequest): PeekFuelCardResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardQueryService/PeekFuelCard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PeekFuelCard", request)
            log.atInfo().addKeyValue("operation", "PeekFuelCard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            peekFuelCardResponse {
                this.fuelCard = fuelCard {
                    this.fuelCardId = request.fuelCardId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleResolveFuelCards(request: ResolveFuelCardsRequest): ResolveFuelCardsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardQueryService/ResolveFuelCards") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResolveFuelCards", request)
            log.atInfo().addKeyValue("operation", "ResolveFuelCards").log("Handling transport request")
            val outbound0 = scoreFuelTransactionRequest {
                this.context = request.context
                this.fuelTransaction = fuelTransaction {
                    this.occurredAt = request.context.deadline
                    this.source = request.context.actor.userId
                    this.subject = customerRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = flowOf(outbound0).map { value -> fuelFuelTransactionValidationService.scoreFuelTransaction(value) }.single()
            resolveFuelCardsResponse {
                this.fuelCards.addAll(listOf(fuelCard {
                    this.holder = driverRef {
                        this.displayName = reply0.fuelTransaction.subject.displayName
                    }
                }))
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSummarizeFuelCards(request: SummarizeFuelCardsRequest): SummarizeFuelCardsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardQueryService/SummarizeFuelCards") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SummarizeFuelCards", request)
            log.atInfo().addKeyValue("operation", "SummarizeFuelCards").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCardId).log("Recorded audit detail")
            val outbound0 = calculateFuelTransactionRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.fuelTransaction = fuelTransaction {
                    this.subject = customerRef {
                        this.email = request.context.actor.email
                    }
                }
                this.reason = request.reason
            }
            val reply0 = fuelFuelTransactionCalculationService.calculateFuelTransaction(outbound0)
            val outbound1 = scoreFuelPolicyRequest {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.owner = userRef {
                        this.email = reply0.fuelTransaction.subject.email
                    }
                }
            }
            val reply1 = fuelFuelPolicyValidationService.scoreFuelPolicy(outbound1)
            summarizeFuelCardsResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.correlationId = reply0.fuelTransaction.correlationId
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }
}
