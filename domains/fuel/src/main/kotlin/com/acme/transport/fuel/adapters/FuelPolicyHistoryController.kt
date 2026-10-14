package com.acme.transport.fuel.adapters

import com.acme.audit.v1.AuditEventServiceGrpcKt
import com.acme.audit.v1.batchGetAuditEventsRequest
import com.acme.authorization.v1.AccessPolicyValidationServiceGrpcKt
import com.acme.authorization.v1.ScoreAccessPolicyResponse
import com.acme.authorization.v1.ValidateAccessPolicyRequest
import com.acme.authorization.v1.ValidateAccessPolicyRequestKt
import com.acme.authorization.v1.ValidateAccessPolicyResponse
import com.acme.authorization.v1.accessPolicy
import com.acme.authorization.v1.accessPolicyCondition
import com.acme.authorization.v1.accessPolicyDiscountAction
import com.acme.authorization.v1.checkAccessPolicyEligibilityRequest
import com.acme.authorization.v1.lintAccessPoliciesRequest
import com.acme.authorization.v1.scoreAccessPolicyRequest
import com.acme.authorization.v1.validateAccessPolicyRequest
import com.acme.careplans.v1.CarePlanQueryServiceGrpcKt
import com.acme.careplans.v1.SummarizeCarePlansRequest
import com.acme.careplans.v1.SummarizeCarePlansRequestKt
import com.acme.careplans.v1.summarizeCarePlansRequest
import com.acme.common.v1.Money
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.deliveries.v1.DeliveryExceptionCalculationServiceGrpcKt
import com.acme.deliveries.v1.SimulateDeliveryExceptionRequest
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.simulateDeliveryExceptionRequest
import com.acme.fuel.v1.AnnotateFuelPolicyVersionRequest
import com.acme.fuel.v1.AnnotateFuelPolicyVersionResponse
import com.acme.fuel.v1.CompareFuelPolicyVersionsRequest
import com.acme.fuel.v1.CompareFuelPolicyVersionsResponse
import com.acme.fuel.v1.FuelPolicy
import com.acme.fuel.v1.FuelPolicyHistoryServiceGrpcKt
import com.acme.fuel.v1.GetFuelPolicyVersionRequest
import com.acme.fuel.v1.GetFuelPolicyVersionResponse
import com.acme.fuel.v1.ListFuelPolicyVersionsRequest
import com.acme.fuel.v1.ListFuelPolicyVersionsResponse
import com.acme.fuel.v1.PruneFuelPolicyVersionsRequest
import com.acme.fuel.v1.PruneFuelPolicyVersionsResponse
import com.acme.fuel.v1.RecordFuelPolicyVersionRequest
import com.acme.fuel.v1.RecordFuelPolicyVersionResponse
import com.acme.fuel.v1.RevertFuelPolicyRequest
import com.acme.fuel.v1.RevertFuelPolicyResponse
import com.acme.fuel.v1.annotateFuelPolicyVersionResponse
import com.acme.fuel.v1.compareFuelPolicyVersionsResponse
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.getFuelPolicyVersionResponse
import com.acme.fuel.v1.listFuelPolicyVersionsResponse
import com.acme.fuel.v1.pruneFuelPolicyVersionsResponse
import com.acme.fuel.v1.recordFuelPolicyVersionResponse
import com.acme.fuel.v1.revertFuelPolicyResponse
import com.acme.identity.v1.LookupUserRequest
import com.acme.identity.v1.UserQueryServiceGrpcKt
import com.acme.identity.v1.lookupUserRequest
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.ExportLedgerAccountsRequestKt
import com.acme.ledger.v1.LedgerAccountQueryServiceGrpcKt
import com.acme.ledger.v1.LedgerAccountServiceGrpcKt
import com.acme.ledger.v1.ListLedgerAccountsRequestKt
import com.acme.ledger.v1.exportLedgerAccountsRequest
import com.acme.ledger.v1.listLedgerAccountsRequest
import com.acme.transport.fuel.FuelRepository
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.CalculateYardRequestKt
import com.acme.yards.v1.CloneYardResponse
import com.acme.yards.v1.ExplainYardMoveCalculationRequest
import com.acme.yards.v1.ExplainYardMoveCalculationResponse
import com.acme.yards.v1.YardCalculationServiceGrpcKt
import com.acme.yards.v1.YardComponent
import com.acme.yards.v1.YardMoveCalculationServiceGrpcKt
import com.acme.yards.v1.YardServiceGrpcKt
import com.acme.yards.v1.calculateYardRequest
import com.acme.yards.v1.cloneYardRequest
import com.acme.yards.v1.copy as yardsCopy
import com.acme.yards.v1.explainYardMoveCalculationRequest
import com.acme.yards.v1.yard
import com.acme.yards.v1.yardComponent
import com.acme.yards.v1.yardMove
import com.google.protobuf.Any as ProtoAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.LoggerFactory

/** Keeps every version of a fuel policy and restores earlier ones. */
class FuelPolicyHistoryController(
    private val store: FuelRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventService: AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub,
    private val authorizationAccessPolicyValidationService: AccessPolicyValidationServiceGrpcKt.AccessPolicyValidationServiceCoroutineStub,
    private val careplansCarePlanQueryService: CarePlanQueryServiceGrpcKt.CarePlanQueryServiceCoroutineStub,
    private val deliveriesDeliveryExceptionCalculationService: DeliveryExceptionCalculationServiceGrpcKt.DeliveryExceptionCalculationServiceCoroutineStub,
    private val identityUserQueryService: UserQueryServiceGrpcKt.UserQueryServiceCoroutineStub,
    private val ledgerLedgerAccountQueryService: LedgerAccountQueryServiceGrpcKt.LedgerAccountQueryServiceCoroutineStub,
    private val ledgerLedgerAccountService: LedgerAccountServiceGrpcKt.LedgerAccountServiceCoroutineStub,
    private val yardsYardCalculationService: YardCalculationServiceGrpcKt.YardCalculationServiceCoroutineStub,
    private val yardsYardMoveCalculationService: YardMoveCalculationServiceGrpcKt.YardMoveCalculationServiceCoroutineStub,
    private val yardsYardService: YardServiceGrpcKt.YardServiceCoroutineStub,
) : FuelPolicyHistoryServiceGrpcKt.FuelPolicyHistoryServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(FuelPolicyHistoryController::class.java)

    private val invokeRevertFuelPolicy0: suspend (ExplainYardMoveCalculationRequest) -> ExplainYardMoveCalculationResponse = { payload ->
        yardsYardMoveCalculationService.explainYardMoveCalculation(payload)
    }

    /** Annotates a fuel policy. */
    override suspend fun annotateFuelPolicyVersion(request: AnnotateFuelPolicyVersionRequest): AnnotateFuelPolicyVersionResponse = handleAnnotateFuelPolicyVersion(request)

    /** Compares a fuel policy. */
    override suspend fun compareFuelPolicyVersions(request: CompareFuelPolicyVersionsRequest): CompareFuelPolicyVersionsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyHistoryService/CompareFuelPolicyVersions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CompareFuelPolicyVersions", request)
            log.atInfo().addKeyValue("operation", "CompareFuelPolicyVersions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicy.fuelPolicyId).log("Recorded audit detail")
            val outbound0 = lintAccessPoliciesRequest {
                this.accessPolicy = accessPolicy {
                    this.audit = auditInfo {
                        this.etag = request.fuelPolicy.audit.etag
                        this.updatedBy = request.fuelPolicy.audit.updatedBy
                    }
                    this.discount = accessPolicyDiscountAction {
                        this.amount = when (request.fuelPolicy.actionCase) {
                            FuelPolicy.ActionCase.DISCOUNT -> request.fuelPolicy.discount.amount
                            else -> Money.getDefaultInstance()
                        }
                    }
                    this.owner = userRef {
                        this.email = request.fuelPolicy.owner.email
                    }
                }
                this.context = request.context
            }
            val reply0 = authorizationAccessPolicyValidationService.lintAccessPolicies(outbound0)
            val outbound1 = listLedgerAccountsRequest {
                this.context = request.context
                this.filter = ListLedgerAccountsRequestKt.filter {
                    this.createdAfter = reply0.accessPolicy.audit.createdAt
                }
            }
            val reply1 = ledgerLedgerAccountService.listLedgerAccounts(outbound1)
            compareFuelPolicyVersionsResponse {
                this.fuelPolicy = fuelPolicy {
                    this.fuelPolicyId = request.fuelPolicyId
                    this.priority = reply0.accessPolicy.priority
                    this.status = request.fuelPolicy.status
                }
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = "ok"
                }
            }
        }

    /** Returns a fuel policy. */
    override suspend fun getFuelPolicyVersion(request: GetFuelPolicyVersionRequest): GetFuelPolicyVersionResponse {
        return handleGetFuelPolicyVersion(request)
    }

    /** Lists fuel policies a page at a time. */
    override suspend fun listFuelPolicyVersions(request: ListFuelPolicyVersionsRequest): ListFuelPolicyVersionsResponse = handleListFuelPolicyVersions(request)

    /** Deletes a fuel policy. */
    override suspend fun pruneFuelPolicyVersions(request: PruneFuelPolicyVersionsRequest): PruneFuelPolicyVersionsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyHistoryService/PruneFuelPolicyVersions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PruneFuelPolicyVersions", request)
            log.atInfo().addKeyValue("operation", "PruneFuelPolicyVersions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicyId).log("Recorded audit detail")
            val outbound0 = simulateDeliveryExceptionRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.audit = auditInfo {
                        this.etag = request.etag
                    }
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = deliveriesDeliveryExceptionCalculationService.simulateDeliveryException(envelope0.unpack(SimulateDeliveryExceptionRequest::class.java))
            val outbound1 = batchGetAuditEventsRequest {
                this.context = request.context
            }
            val reply1 = auditAuditEventService.batchGetAuditEvents(WireCopies.json(outbound1))
            val outbound2 = exportLedgerAccountsRequest {
                this.context = request.context
                this.filter = ExportLedgerAccountsRequestKt.filter {
                    this.createdAfter = reply0.deliveryException.audit.createdAt
                    this.labels.putAll(reply0.result.detailsMap)
                    this.ownerId = reply0.deliveryException.customer.deliveryId
                }
            }
            ledgerLedgerAccountQueryService.exportLedgerAccounts(outbound2).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound3 = lookupUserRequest {
                this.context = request.context
                this.userId = request.context.actor.userId
            }
            val command3: Submission<LookupUserRequest> = Submission.Live(outbound3)
            val payload3 = when (val command = command3) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply3 = identityUserQueryService.lookupUser(payload3)
            pruneFuelPolicyVersionsResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply0.result.message
                    this.referenceIds.addAll(reply3.result.referenceIdsList)
                    this.status = reply1.result.status
                }
            }
        }

    /** Records a fuel policy. */
    override suspend fun recordFuelPolicyVersion(request: RecordFuelPolicyVersionRequest): RecordFuelPolicyVersionResponse {
        return handleRecordFuelPolicyVersion(request)
    }

    /** Reverts a fuel policy. */
    override suspend fun revertFuelPolicy(request: RevertFuelPolicyRequest): RevertFuelPolicyResponse = handleRevertFuelPolicy(request)

    private suspend fun handleAnnotateFuelPolicyVersion(request: AnnotateFuelPolicyVersionRequest): AnnotateFuelPolicyVersionResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyHistoryService/AnnotateFuelPolicyVersion") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "AnnotateFuelPolicyVersion", request)
            log.atInfo().addKeyValue("operation", "AnnotateFuelPolicyVersion").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicy.fuelPolicyId).log("Recorded audit detail")
            var reply0 = ScoreAccessPolicyResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.fuelPolicy.conditionsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound0 = scoreAccessPolicyRequest {
                                this.accessPolicy = accessPolicy {
                                    this.conditions.addAll(listOf(accessPolicyCondition {
                                        this.attribute = item.attribute
                                        this.negate = item.negate
                                        this.operator = item.operator
                                        this.value = item.value
                                    }))
                                }
                                this.context = request.context
                            }
                            authorizationAccessPolicyValidationService.scoreAccessPolicy(outbound0)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply0 = it }
                }
            }
            val configure1: CalculateYardRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.details = CalculateYardRequestKt.details {
                    this.currencyCode = reply0.breakdown.currencyCode
                }
                this.yard = yard {
                    this.audit = auditInfo {
                        this.createdBy = request.fuelPolicy.audit.createdBy
                        this.updatedAt = reply0.accessPolicy.audit.updatedAt
                    }
                    this.owner = userRef {
                        this.email = request.fuelPolicy.owner.email
                    }
                }
            }
            val outbound1 = calculateYardRequest(configure1)
            val reply1 = yardsYardCalculationService.calculateYard(outbound1)
            annotateFuelPolicyVersionResponse {
                this.fuelPolicy = fuelPolicy {
                    this.fuelPolicyId = request.fuelPolicyId
                    this.owner = userRef {
                        this.displayName = reply1.yard.owner.displayName
                    }
                    this.parameters.putAll(request.fuelPolicy.parametersMap)
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private fun GetFuelPolicyVersionRequest.toSummarizeCarePlansFromGetFuelPolicyVersion1(reply0: CloneYardResponse): SummarizeCarePlansRequest {
        val source = this
        return summarizeCarePlansRequest {
            this.annotations.putAll(reply0.yard.specsMap)
            this.context = source.context
            this.details = SummarizeCarePlansRequestKt.details {
                this.filters.putAll(reply0.result.detailsMap)
            }
        }
    }

    private suspend fun handleGetFuelPolicyVersion(request: GetFuelPolicyVersionRequest): GetFuelPolicyVersionResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyHistoryService/GetFuelPolicyVersion") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetFuelPolicyVersion", request)
            log.atInfo().addKeyValue("operation", "GetFuelPolicyVersion").log("Handling transport request")
            val outbound0 = cloneYardRequest {}.yardsCopy {
                this.context = request.context
                this.yard = yard {
                    this.audit = auditInfo {
                        this.createdAt = request.context.deadline
                    }
                    this.components.putAll(mapOf("primary" to yardComponent {
                        this.componentId = request.fuelPolicyId
                    }))
                    this.owner = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply0 = yardsYardService.cloneYard(outbound0)
            val outbound1 = request.toSummarizeCarePlansFromGetFuelPolicyVersion1(reply0)
            val reply1 = careplansCarePlanQueryService.summarizeCarePlans(outbound1)
            getFuelPolicyVersionResponse {
                this.fuelPolicy = fuelPolicy {
                    this.description = (reply0.yard.componentsMap["primary"] ?: YardComponent.getDefaultInstance()).description
                    this.fuelPolicyId = request.fuelPolicyId
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = reply1.result.status
                }
            }
        }

    private suspend fun handleListFuelPolicyVersions(request: ListFuelPolicyVersionsRequest): ListFuelPolicyVersionsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyHistoryService/ListFuelPolicyVersions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListFuelPolicyVersions", request)
            log.atInfo().addKeyValue("operation", "ListFuelPolicyVersions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = checkAccessPolicyEligibilityRequest {
                this.accessPolicy = accessPolicy {
                    this.owner = userRef {
                        this.userId = request.context.actor.userId
                    }
                }
                this.context = request.context
            }
            val reply0 = authorizationAccessPolicyValidationService.checkAccessPolicyEligibility(outbound0)
            listFuelPolicyVersionsResponse {
                this.fuelPolicies.addAll(listOf(fuelPolicy {
                    this.owner = reply0.accessPolicy.owner
                    this.parameters.putAll(reply0.accessPolicy.parametersMap)
                }))
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleRecordFuelPolicyVersion(request: RecordFuelPolicyVersionRequest): RecordFuelPolicyVersionResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyHistoryService/RecordFuelPolicyVersion") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecordFuelPolicyVersion", request)
            log.atInfo().addKeyValue("operation", "RecordFuelPolicyVersion").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicy.fuelPolicyId).log("Recorded audit detail")
            val outbound0 = validateAccessPolicyRequest {
                this.accessPolicy = accessPolicy {
                    this.owner = userRef {
                        this.displayName = request.fuelPolicy.owner.displayName
                    }
                }
                this.context = request.context
                this.details = ValidateAccessPolicyRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.fuelPolicy.owner.email
                    }
                }
            }
            val adapter0 = OutboundPort<ValidateAccessPolicyRequest, ValidateAccessPolicyResponse> { payload ->
                authorizationAccessPolicyValidationService.validateAccessPolicy(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            recordFuelPolicyVersionResponse {
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.createdBy = reply0.accessPolicy.audit.createdBy
                    }
                    this.description = request.fuelPolicy.description
                    this.fuelPolicyId = request.fuelPolicyId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleRevertFuelPolicy(request: RevertFuelPolicyRequest): RevertFuelPolicyResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyHistoryService/RevertFuelPolicy") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RevertFuelPolicy", request)
            log.atInfo().addKeyValue("operation", "RevertFuelPolicy").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicy.fuelPolicyId).log("Recorded audit detail")
            val outbound0 = explainYardMoveCalculationRequest {
                this.context = request.context
                this.yardMove = yardMove {
                    this.audit = auditInfo {
                        this.etag = request.fuelPolicy.audit.etag
                        this.updatedAt = request.fuelPolicy.audit.updatedAt
                    }
                }
            }
            val reply0 = invokeRevertFuelPolicy0(outbound0)
            revertFuelPolicyResponse {
                this.fuelPolicy = fuelPolicy {
                    this.description = request.fuelPolicy.description
                    this.fuelPolicyId = request.fuelPolicyId
                    this.owner = userRef {
                        this.displayName = reply0.yardMove.customer.displayName
                    }
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }
}
