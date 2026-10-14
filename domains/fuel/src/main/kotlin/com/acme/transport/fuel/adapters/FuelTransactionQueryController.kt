package com.acme.transport.fuel.adapters

import com.acme.authorization.v1.AccessPolicyQueryServiceGrpcKt
import com.acme.authorization.v1.FindAccessPoliciesRequest
import com.acme.authorization.v1.FindAccessPoliciesRequestKt
import com.acme.authorization.v1.FindAccessPoliciesResponse
import com.acme.authorization.v1.findAccessPoliciesRequest
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.customers.v1.customerRef
import com.acme.fuel.v1.BrowseFuelTransactionsRequest
import com.acme.fuel.v1.BrowseFuelTransactionsResponse
import com.acme.fuel.v1.CheckFuelCardEligibilityRequest
import com.acme.fuel.v1.CheckFuelCardEligibilityRequestKt
import com.acme.fuel.v1.CheckFuelCardEligibilityResponse
import com.acme.fuel.v1.CountFuelTransactionsRequest
import com.acme.fuel.v1.CountFuelTransactionsResponse
import com.acme.fuel.v1.ExportFuelTransactionsRequest
import com.acme.fuel.v1.ExportFuelTransactionsResponse
import com.acme.fuel.v1.FindFuelTransactionsRequest
import com.acme.fuel.v1.FindFuelTransactionsResponse
import com.acme.fuel.v1.FuelCardValidationServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyValidationServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionQueryServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionValidationServiceGrpcKt
import com.acme.fuel.v1.LookupFuelTransactionRequest
import com.acme.fuel.v1.LookupFuelTransactionResponse
import com.acme.fuel.v1.PeekFuelTransactionRequest
import com.acme.fuel.v1.PeekFuelTransactionResponse
import com.acme.fuel.v1.ResolveFuelTransactionsRequest
import com.acme.fuel.v1.ResolveFuelTransactionsResponse
import com.acme.fuel.v1.SuggestFuelTransactionsRequest
import com.acme.fuel.v1.SuggestFuelTransactionsResponse
import com.acme.fuel.v1.SummarizeFuelTransactionsRequest
import com.acme.fuel.v1.SummarizeFuelTransactionsResponse
import com.acme.fuel.v1.ValidateFuelPolicyRequest
import com.acme.fuel.v1.ValidateFuelPolicyResponse
import com.acme.fuel.v1.ValidateFuelTransactionRequest
import com.acme.fuel.v1.ValidateFuelTransactionRequestKt
import com.acme.fuel.v1.browseFuelTransactionsResponse
import com.acme.fuel.v1.checkFuelCardEligibilityRequest
import com.acme.fuel.v1.countFuelTransactionsResponse
import com.acme.fuel.v1.exportFuelTransactionsResponse
import com.acme.fuel.v1.findFuelTransactionsResponse
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.lookupFuelTransactionResponse
import com.acme.fuel.v1.peekFuelTransactionResponse
import com.acme.fuel.v1.resolveFuelTransactionsResponse
import com.acme.fuel.v1.scoreFuelTransactionRequest
import com.acme.fuel.v1.suggestFuelTransactionsResponse
import com.acme.fuel.v1.summarizeFuelTransactionsResponse
import com.acme.fuel.v1.validateFuelPolicyRequest
import com.acme.fuel.v1.validateFuelTransactionRequest
import com.acme.identity.v1.userRef
import com.acme.metrics.v1.ListMetricSeriesesRequest
import com.acme.metrics.v1.ListMetricSeriesesRequestKt
import com.acme.metrics.v1.MetricSeriesServiceGrpcKt
import com.acme.metrics.v1.listMetricSeriesesRequest
import com.acme.transport.fuel.FuelRepository
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.slf4j.LoggerFactory

/** Answers read-only questions about fuel transactions, for screens and reports. */
class FuelTransactionQueryController(
    private val store: FuelRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyQueryService: AccessPolicyQueryServiceGrpcKt.AccessPolicyQueryServiceCoroutineStub,
    private val fuelFuelCardValidationService: FuelCardValidationServiceGrpcKt.FuelCardValidationServiceCoroutineStub,
    private val fuelFuelPolicyValidationService: FuelPolicyValidationServiceGrpcKt.FuelPolicyValidationServiceCoroutineStub,
    private val fuelFuelTransactionValidationService: FuelTransactionValidationServiceGrpcKt.FuelTransactionValidationServiceCoroutineStub,
    private val metricsMetricSeriesService: MetricSeriesServiceGrpcKt.MetricSeriesServiceCoroutineStub,
) : FuelTransactionQueryServiceGrpcKt.FuelTransactionQueryServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(FuelTransactionQueryController::class.java)

    private val invokeLookupFuelTransaction0: suspend (FindAccessPoliciesRequest) -> FindAccessPoliciesResponse = { payload ->
        authorizationAccessPolicyQueryService.findAccessPolicies(payload)
    }

    /** Lists fuel transactions a page at a time. */
    override suspend fun browseFuelTransactions(request: BrowseFuelTransactionsRequest): BrowseFuelTransactionsResponse = handleBrowseFuelTransactions(request)

    /** Counts fuel transactions by status. */
    override suspend fun countFuelTransactions(request: CountFuelTransactionsRequest): CountFuelTransactionsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionQueryService/CountFuelTransactions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CountFuelTransactions", request)
            log.atInfo().addKeyValue("operation", "CountFuelTransactions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            countFuelTransactionsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Streams every matching fuel transaction. */
    override fun exportFuelTransactions(request: ExportFuelTransactionsRequest): Flow<ExportFuelTransactionsResponse> = flow {
        emit(handleExportFuelTransactions(request))
    }

    /** Lists fuel transactions a page at a time. */
    override suspend fun findFuelTransactions(request: FindFuelTransactionsRequest): FindFuelTransactionsResponse = handleFindFuelTransactions(request)

    /** Returns a fuel transaction. */
    override suspend fun lookupFuelTransaction(request: LookupFuelTransactionRequest): LookupFuelTransactionResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionQueryService/LookupFuelTransaction") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LookupFuelTransaction", request)
            log.atInfo().addKeyValue("operation", "LookupFuelTransaction").log("Handling transport request")
            val outbound0 = findAccessPoliciesRequest {
                this.context = request.context
                this.filter = FindAccessPoliciesRequestKt.filter {
                    this.createdAfter = request.context.deadline
                    this.labels.putAll(request.context.baggageMap)
                    this.ownerId = request.fuelTransactionId
                }
            }
            val reply0 = invokeLookupFuelTransaction0(outbound0)
            lookupFuelTransactionResponse {
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = reply0.result.status
                }
            }
        }

    /** Returns a fuel transaction. */
    override suspend fun peekFuelTransaction(request: PeekFuelTransactionRequest): PeekFuelTransactionResponse {
        return handlePeekFuelTransaction(request)
    }

    /** Returns several fuel transactions at once. */
    override suspend fun resolveFuelTransactions(request: ResolveFuelTransactionsRequest): ResolveFuelTransactionsResponse = handleResolveFuelTransactions(request)

    /** Lists fuel transactions a page at a time. */
    override suspend fun suggestFuelTransactions(request: SuggestFuelTransactionsRequest): SuggestFuelTransactionsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionQueryService/SuggestFuelTransactions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SuggestFuelTransactions", request)
            log.atInfo().addKeyValue("operation", "SuggestFuelTransactions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            suggestFuelTransactionsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Summarizes a fuel transaction. */
    override suspend fun summarizeFuelTransactions(request: SummarizeFuelTransactionsRequest): SummarizeFuelTransactionsResponse {
        return handleSummarizeFuelTransactions(request)
    }

    private suspend fun handleBrowseFuelTransactions(request: BrowseFuelTransactionsRequest): BrowseFuelTransactionsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionQueryService/BrowseFuelTransactions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BrowseFuelTransactions", request)
            log.atInfo().addKeyValue("operation", "BrowseFuelTransactions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = listMetricSeriesesRequest {
                this.context = request.context
                this.filter = ListMetricSeriesesRequestKt.filter {
                    this.query = request.filter.query
                    this.region = request.filter.region
                }
            }
            val command0: Submission<ListMetricSeriesesRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = metricsMetricSeriesService.listMetricSerieses(payload0)
            browseFuelTransactionsResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleExportFuelTransactions(request: ExportFuelTransactionsRequest): ExportFuelTransactionsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionQueryService/ExportFuelTransactions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExportFuelTransactions", request)
            log.atInfo().addKeyValue("operation", "ExportFuelTransactions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = checkFuelCardEligibilityRequest {
                this.context = request.context
                this.details = CheckFuelCardEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
                this.fuelCard = fuelCard {
                    this.labels.putAll(request.filter.labelsMap)
                }
            }
            val adapter0 = OutboundPort<CheckFuelCardEligibilityRequest, CheckFuelCardEligibilityResponse> { payload ->
                fuelFuelCardValidationService.checkFuelCardEligibility(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            exportFuelTransactionsResponse {
                this.result = operationResult {
                    this.status = reply0.outcome.status
                }
            }
        }

    private suspend fun handleFindFuelTransactions(request: FindFuelTransactionsRequest): FindFuelTransactionsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionQueryService/FindFuelTransactions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "FindFuelTransactions", request)
            log.atInfo().addKeyValue("operation", "FindFuelTransactions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            findFuelTransactionsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handlePeekFuelTransaction(request: PeekFuelTransactionRequest): PeekFuelTransactionResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionQueryService/PeekFuelTransaction") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PeekFuelTransaction", request)
            log.atInfo().addKeyValue("operation", "PeekFuelTransaction").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            peekFuelTransactionResponse {
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleResolveFuelTransactions(request: ResolveFuelTransactionsRequest): ResolveFuelTransactionsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionQueryService/ResolveFuelTransactions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResolveFuelTransactions", request)
            log.atInfo().addKeyValue("operation", "ResolveFuelTransactions").log("Handling transport request")
            val outbound0 = validateFuelPolicyRequest {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.owner = userRef {
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val invoke0: suspend (ValidateFuelPolicyRequest, Metadata) -> ValidateFuelPolicyResponse = fuelFuelPolicyValidationService::validateFuelPolicy
            val reply0 = invoke0(outbound0, Metadata())
            resolveFuelTransactionsResponse {
                this.fuelTransactions.addAll(listOf(fuelTransaction {
                    this.subject = customerRef {
                        this.email = reply0.fuelPolicy.owner.email
                    }
                }))
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSummarizeFuelTransactions(request: SummarizeFuelTransactionsRequest): SummarizeFuelTransactionsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionQueryService/SummarizeFuelTransactions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SummarizeFuelTransactions", request)
            log.atInfo().addKeyValue("operation", "SummarizeFuelTransactions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.format).log("Recorded audit detail")
            val outbound0 = ValidateFuelTransactionRequest.newBuilder().apply {
                setContext(request.context)
                setDetails(ValidateFuelTransactionRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                })
                setFuelTransaction(fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> fuelFuelTransactionValidationService.validateFuelTransaction(value) }
            val outbound1 = RequestFactory.create {
                scoreFuelTransactionRequest {
                    this.context = request.context
                    this.fuelTransaction = fuelTransaction {
                        this.subject = customerRef {
                            this.customerId = reply0.fuelTransaction.subject.customerId
                            this.displayName = reply0.fuelTransaction.subject.displayName
                            this.email = reply0.fuelTransaction.subject.email
                        }
                    }
                }
            }
            val reply1 = fuelFuelTransactionValidationService.scoreFuelTransaction(outbound1)
            summarizeFuelTransactionsResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.referenceIds.addAll((reply0.result.referenceIdsList) + (reply1.result.referenceIdsList))
                    this.status = "ok"
                }
            }
        }
}
