package com.acme.transport.fuel.grpc

import com.acme.authorization.v1.AccessPolicyQueryServiceGrpcKt
import com.acme.authorization.v1.resolveAccessPoliciesRequest
import com.acme.budgeting.v1.BudgetCalculationServiceGrpcKt
import com.acme.budgeting.v1.EstimateBudgetResponse
import com.acme.budgeting.v1.budget
import com.acme.budgeting.v1.budgetLimit
import com.acme.budgeting.v1.estimateBudgetRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.ApproveDeliveryExceptionRequest
import com.acme.deliveries.v1.ApproveDeliveryExceptionRequestKt
import com.acme.deliveries.v1.ApproveDeliveryExceptionResponse
import com.acme.deliveries.v1.CommentProofOfDeliveryApprovalResponse
import com.acme.deliveries.v1.DeliveryExceptionApprovalServiceGrpcKt
import com.acme.deliveries.v1.PauseProofOfDeliveryWorkflowRequest
import com.acme.deliveries.v1.PauseProofOfDeliveryWorkflowRequestKt
import com.acme.deliveries.v1.ProofOfDeliveryApprovalServiceGrpcKt
import com.acme.deliveries.v1.ProofOfDeliveryWorkflowServiceGrpcKt
import com.acme.deliveries.v1.approveDeliveryExceptionRequest
import com.acme.deliveries.v1.commentProofOfDeliveryApprovalRequest
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.pauseProofOfDeliveryWorkflowRequest
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.proofOfDeliveryCardSettlement
import com.acme.deliveries.v1.proofOfDeliveryLine
import com.acme.deliveries.v1.proofOfDeliveryLineTax
import com.acme.drivers.v1.BrowseDriversRequestKt
import com.acme.drivers.v1.CalculateDriverLicenseRequest
import com.acme.drivers.v1.CheckDriverEligibilityRequest
import com.acme.drivers.v1.CheckDriverEligibilityRequestKt
import com.acme.drivers.v1.CheckDriverEligibilityResponse
import com.acme.drivers.v1.DriverLicenseCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverQueryServiceGrpcKt
import com.acme.drivers.v1.DriverValidationServiceGrpcKt
import com.acme.drivers.v1.FindDriversRequestKt
import com.acme.drivers.v1.QuoteDriverLicenseResponse
import com.acme.drivers.v1.browseDriversRequest
import com.acme.drivers.v1.calculateDriverLicenseRequest
import com.acme.drivers.v1.checkDriverEligibilityRequest
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverLicenseBankSettlement
import com.acme.drivers.v1.driverLicenseCardSettlement
import com.acme.drivers.v1.driverLicenseLine
import com.acme.drivers.v1.driverLicenseLineTax
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.findDriversRequest
import com.acme.drivers.v1.quoteDriverLicenseRequest
import com.acme.featureflags.v1.CountFeatureFlagsRequest
import com.acme.featureflags.v1.CountFeatureFlagsRequestKt
import com.acme.featureflags.v1.CountFeatureFlagsResponse
import com.acme.featureflags.v1.FeatureFlagQueryServiceGrpcKt
import com.acme.featureflags.v1.countFeatureFlagsRequest
import com.acme.fuel.v1.ApproveFuelCardRequest
import com.acme.fuel.v1.ApproveFuelCardResponse
import com.acme.fuel.v1.ApproveFuelCardResponseKt
import com.acme.fuel.v1.BrowseFuelTransactionsRequestKt
import com.acme.fuel.v1.CancelFuelCardImportRequest
import com.acme.fuel.v1.CancelFuelCardImportRequestKt
import com.acme.fuel.v1.CommentFuelCardApprovalRequest
import com.acme.fuel.v1.CommentFuelCardApprovalResponse
import com.acme.fuel.v1.CommentFuelCardApprovalResponseKt
import com.acme.fuel.v1.DelegateFuelCardApprovalRequest
import com.acme.fuel.v1.DelegateFuelCardApprovalResponse
import com.acme.fuel.v1.DelegateFuelCardApprovalResponseKt
import com.acme.fuel.v1.FuelCardApprovalServiceGrpcKt
import com.acme.fuel.v1.FuelCardImportServiceGrpcKt
import com.acme.fuel.v1.FuelCardLimit
import com.acme.fuel.v1.FuelCardSyncServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionQueryServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionReportingServiceGrpcKt
import com.acme.fuel.v1.GetFuelCardApprovalStatusRequest
import com.acme.fuel.v1.GetFuelCardApprovalStatusResponse
import com.acme.fuel.v1.ListPendingFuelCardsRequest
import com.acme.fuel.v1.ListPendingFuelCardsResponse
import com.acme.fuel.v1.RecallFuelCardRequest
import com.acme.fuel.v1.RecallFuelCardResponse
import com.acme.fuel.v1.RecallFuelCardResponseKt
import com.acme.fuel.v1.RejectFuelCardRequest
import com.acme.fuel.v1.RejectFuelCardResponse
import com.acme.fuel.v1.RejectFuelCardResponseKt
import com.acme.fuel.v1.ResolveFuelCardConflictRequest
import com.acme.fuel.v1.SubmitFuelCardRequest
import com.acme.fuel.v1.SubmitFuelCardResponse
import com.acme.fuel.v1.SubmitFuelCardResponseKt
import com.acme.fuel.v1.approveFuelCardResponse
import com.acme.fuel.v1.browseFuelTransactionsRequest
import com.acme.fuel.v1.cancelFuelCardImportRequest
import com.acme.fuel.v1.commentFuelCardApprovalResponse
import com.acme.fuel.v1.delegateFuelCardApprovalResponse
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.fuelCardCardFunding
import com.acme.fuel.v1.generateFuelTransactionReportRequest
import com.acme.fuel.v1.getFuelCardApprovalStatusResponse
import com.acme.fuel.v1.listPendingFuelCardsResponse
import com.acme.fuel.v1.mapFuelCardImportColumnsRequest
import com.acme.fuel.v1.pullFuelCardsRequest
import com.acme.fuel.v1.recallFuelCardResponse
import com.acme.fuel.v1.rejectFuelCardResponse
import com.acme.fuel.v1.resolveFuelCardConflictRequest
import com.acme.fuel.v1.submitFuelCardResponse
import com.acme.identity.v1.UserValidationServiceGrpcKt
import com.acme.identity.v1.copy as identityCopy
import com.acme.identity.v1.explainUserDecisionRequest
import com.acme.identity.v1.user
import com.acme.identity.v1.userRef
import com.acme.transport.fuel.FuelRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.CommentDockApprovalRequest
import com.acme.yards.v1.CommentDockApprovalRequestKt
import com.acme.yards.v1.DockApprovalServiceGrpcKt
import com.acme.yards.v1.commentDockApprovalRequest
import com.acme.yards.v1.dock
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

/** Routes fuel cards through approval, with escalation and delegation. */
class FuelCardApprovalEndpoint(
    private val store: FuelRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyQueryService: AccessPolicyQueryServiceGrpcKt.AccessPolicyQueryServiceCoroutineStub,
    private val budgetingBudgetCalculationService: BudgetCalculationServiceGrpcKt.BudgetCalculationServiceCoroutineStub,
    private val deliveriesDeliveryExceptionApprovalService: DeliveryExceptionApprovalServiceGrpcKt.DeliveryExceptionApprovalServiceCoroutineStub,
    private val deliveriesProofOfDeliveryApprovalService: ProofOfDeliveryApprovalServiceGrpcKt.ProofOfDeliveryApprovalServiceCoroutineStub,
    private val deliveriesProofOfDeliveryWorkflowService: ProofOfDeliveryWorkflowServiceGrpcKt.ProofOfDeliveryWorkflowServiceCoroutineStub,
    private val driversDriverLicenseCalculationService: DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub,
    private val driversDriverQueryService: DriverQueryServiceGrpcKt.DriverQueryServiceCoroutineStub,
    private val driversDriverValidationService: DriverValidationServiceGrpcKt.DriverValidationServiceCoroutineStub,
    private val featureflagsFeatureFlagQueryService: FeatureFlagQueryServiceGrpcKt.FeatureFlagQueryServiceCoroutineStub,
    private val fuelFuelCardImportService: FuelCardImportServiceGrpcKt.FuelCardImportServiceCoroutineStub,
    private val fuelFuelCardSyncService: FuelCardSyncServiceGrpcKt.FuelCardSyncServiceCoroutineStub,
    private val fuelFuelTransactionQueryService: FuelTransactionQueryServiceGrpcKt.FuelTransactionQueryServiceCoroutineStub,
    private val fuelFuelTransactionReportingService: FuelTransactionReportingServiceGrpcKt.FuelTransactionReportingServiceCoroutineStub,
    private val identityUserValidationService: UserValidationServiceGrpcKt.UserValidationServiceCoroutineStub,
    private val yardsDockApprovalService: DockApprovalServiceGrpcKt.DockApprovalServiceCoroutineStub,
) : FuelCardApprovalServiceGrpcKt.FuelCardApprovalServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(FuelCardApprovalEndpoint::class.java)

    private val invokeCommentFuelCardApproval3: suspend (ApproveDeliveryExceptionRequest) -> ApproveDeliveryExceptionResponse = { payload ->
        deliveriesDeliveryExceptionApprovalService.approveDeliveryException(payload)
    }

    private val workerForRecallFuelCard0 = ReplyMailbox<CheckDriverEligibilityRequest, CheckDriverEligibilityResponse>(workers) { payload ->
        withTimeout(5_000) { driversDriverValidationService.checkDriverEligibility(payload) }
    }

    /** Approves a fuel card. */
    override suspend fun approveFuelCard(request: ApproveFuelCardRequest): ApproveFuelCardResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardApprovalService/ApproveFuelCard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ApproveFuelCard", request)
            log.atInfo().addKeyValue("operation", "ApproveFuelCard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCard.fuelCardId).log("Recorded audit detail")
            val outbound0 = request.toResolveFuelCardConflictFromApproveFuelCard0()
            val reply0 = fuelFuelCardSyncService.resolveFuelCardConflict(outbound0)
            val outbound1 = browseDriversRequest {
                this.context = request.context
                this.filter = BrowseDriversRequestKt.filter {
                    this.createdAfter = reply0.fuelCard.audit.updatedAt
                    this.labels.putAll(reply0.fuelCard.labelsMap)
                    this.ownerId = request.fuelCard.holder.driverId
                }
            }
            val reply1 = driversDriverQueryService.browseDrivers(outbound1)
            val outbound2 = pauseProofOfDeliveryWorkflowRequest {
                this.context = request.context
                this.details = PauseProofOfDeliveryWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.email = reply0.fuelCard.contact.email
                    }
                }
            }
            val envelope2 = ProtoAny.pack(outbound2)
            val reply2 = deliveriesProofOfDeliveryWorkflowService.pauseProofOfDeliveryWorkflow(envelope2.unpack(PauseProofOfDeliveryWorkflowRequest::class.java))
            val outbound3 = mapFuelCardImportColumnsRequest {
                this.context = request.context
                this.fuelCard = fuelCard {
                    this.holder = driverRef {
                        this.displayName = reply0.fuelCard.holder.displayName
                    }
                }
            }
            val reply3 = fuelFuelCardImportService.mapFuelCardImportColumns(WireCopies.json(outbound3))
            approveFuelCardResponse {
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.etag = reply2.proofOfDelivery.audit.etag
                        this.updatedAt = reply3.fuelCard.audit.updatedAt
                    }
                    this.contact = request.fuelCard.contact
                    this.fuelCardId = request.fuelCardId
                    this.openedAt = reply3.fuelCard.openedAt
                }
                this.outcome = ApproveFuelCardResponseKt.outcome {
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.status = reply0.outcome.status
                }
            }
        }

    /** Comments a fuel card. */
    override suspend fun commentFuelCardApproval(request: CommentFuelCardApprovalRequest): CommentFuelCardApprovalResponse {
        return handleCommentFuelCardApproval(request)
    }

    /** Delegates a fuel card. */
    override suspend fun delegateFuelCardApproval(request: DelegateFuelCardApprovalRequest): DelegateFuelCardApprovalResponse = handleDelegateFuelCardApproval(request)

    /** Returns a fuel card. */
    override suspend fun getFuelCardApprovalStatus(request: GetFuelCardApprovalStatusRequest): GetFuelCardApprovalStatusResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardApprovalService/GetFuelCardApprovalStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetFuelCardApprovalStatus", request)
            log.atInfo().addKeyValue("operation", "GetFuelCardApprovalStatus").log("Handling transport request")
            val outbound0 = RequestFactory.create {
                mapFuelCardImportColumnsRequest {
                    this.context = request.context
                    this.fuelCard = fuelCard {
                        this.holder = driverRef {
                            this.email = request.context.actor.email
                        }
                    }
                    this.fuelCardId = request.fuelCardId
                }
            }
            val reply0 = fuelFuelCardImportService.mapFuelCardImportColumns(outbound0)
            getFuelCardApprovalStatusResponse {
                this.fuelCard = fuelCard {
                    this.fuelCardId = request.fuelCardId
                    this.holder = driverRef {
                        this.email = reply0.fuelCard.holder.email
                    }
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Lists fuel cards a page at a time. */
    override suspend fun listPendingFuelCards(request: ListPendingFuelCardsRequest): ListPendingFuelCardsResponse {
        return handleListPendingFuelCards(request)
    }

    /** Recalls a fuel card. */
    override suspend fun recallFuelCard(request: RecallFuelCardRequest): RecallFuelCardResponse = handleRecallFuelCard(request)

    /** Rejects a fuel card. */
    override suspend fun rejectFuelCard(request: RejectFuelCardRequest): RejectFuelCardResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardApprovalService/RejectFuelCard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RejectFuelCard", request)
            log.atInfo().addKeyValue("operation", "RejectFuelCard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCard.holder.driverId).log("Recorded audit detail")
            val configure0: FindDriversRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.filter = FindDriversRequestKt.filter {
                    this.createdAfter = request.fuelCard.audit.updatedAt
                    this.labels.putAll(request.fuelCard.labelsMap)
                    this.ownerId = request.fuelCard.fuelCardId
                }
            }
            val outbound0 = findDriversRequest(configure0)
            val reply0 = driversDriverQueryService.findDrivers(outbound0)
            var reply1 = QuoteDriverLicenseResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.fuelCard.limitsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound1 = quoteDriverLicenseRequest {
                                this.context = request.context
                                this.driverLicense = driverLicense {
                                    this.audit = auditInfo {
                                        this.updatedAt = request.fuelCard.audit.updatedAt
                                    }
                                    this.bank = driverLicenseBankSettlement {
                                        this.bic = request.fuelCard.bank.bic
                                    }
                                    this.lines.addAll(listOf(driverLicenseLine {
                                        this.tax = driverLicenseLineTax {
                                            this.amount = item.amount
                                        }
                                    }))
                                }
                            }
                            driversDriverLicenseCalculationService.quoteDriverLicense(outbound1)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply1 = it }
                }
            }
            rejectFuelCardResponse {
                this.fuelCard = fuelCard {
                    this.contact = request.fuelCard.contact
                    this.fuelCardId = request.fuelCardId
                }
                this.outcome = RejectFuelCardResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }

    /** Submits a fuel card. */
    override suspend fun submitFuelCard(request: SubmitFuelCardRequest): SubmitFuelCardResponse {
        return handleSubmitFuelCard(request)
    }

    private fun ApproveFuelCardRequest.toResolveFuelCardConflictFromApproveFuelCard0(): ResolveFuelCardConflictRequest {
        val source = this
        return resolveFuelCardConflictRequest {
            this.context = source.context
            this.fuelCard = fuelCard {
                this.card = fuelCardCardFunding {
                    this.network = source.fuelCard.card.network
                }
            }
        }
    }

    private data class CommentFuelCardApproval0Command(
        val context: RequestContext,
        val driverLicenseCardHolderName: String,
        val driverLicenseCardNetwork: String,
        val driverLicenseCounterpartyDisplayName: String
    ) {
        fun toRequest(): CalculateDriverLicenseRequest {
            val command = this
            return calculateDriverLicenseRequest {
                this.context = command.context
                this.driverLicense = driverLicense {
                    this.card = driverLicenseCardSettlement {
                        this.holderName = command.driverLicenseCardHolderName
                        this.network = command.driverLicenseCardNetwork
                    }
                    this.counterparty = driverRef {
                        this.displayName = command.driverLicenseCounterpartyDisplayName
                    }
                }
            }
        }
    }

    private suspend fun handleCommentFuelCardApproval(request: CommentFuelCardApprovalRequest): CommentFuelCardApprovalResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardApprovalService/CommentFuelCardApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CommentFuelCardApproval", request)
            log.atInfo().addKeyValue("operation", "CommentFuelCardApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCard.holder.driverId).log("Recorded audit detail")
            val outbound0 = CommentFuelCardApproval0Command(
                context = request.context,
                driverLicenseCardHolderName = request.fuelCard.card.holderName,
                driverLicenseCardNetwork = request.fuelCard.card.network,
                driverLicenseCounterpartyDisplayName = request.details.approver.displayName
            ).toRequest()
            val reply0 = driversDriverLicenseCalculationService.calculateDriverLicense(outbound0)
            val outbound1 = cancelFuelCardImportRequest {
                this.context = request.context
                this.details = CancelFuelCardImportRequestKt.details {
                    this.newOwner = userRef {
                        this.displayName = request.fuelCard.holder.displayName
                    }
                }
                this.fuelCardId = request.fuelCard.fuelCardId
            }
            val command1: Submission<CancelFuelCardImportRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = fuelFuelCardImportService.cancelFuelCardImport(payload1)
            val outbound2 = countFeatureFlagsRequest {
                this.context = request.context
                this.filter = CountFeatureFlagsRequestKt.filter {
                    this.labels.putAll(reply1.fuelCard.labelsMap)
                    this.region = reply0.driverLicense.billingAddress.region
                }
            }
            val adapter2 = OutboundPort<CountFeatureFlagsRequest, CountFeatureFlagsResponse> { payload ->
                featureflagsFeatureFlagQueryService.countFeatureFlags(payload)
            }
            val port2 = DelegatingPort(adapter2)
            val reply2 = port2.execute(outbound2)
            val outbound3 = approveDeliveryExceptionRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.assignee = userRef {
                        this.displayName = request.details.approver.displayName
                    }
                }
                this.details = ApproveDeliveryExceptionRequestKt.details {
                    this.approver = userRef {
                        this.displayName = reply1.fuelCard.holder.displayName
                    }
                }
            }
            val reply3 = invokeCommentFuelCardApproval3(outbound3)
            commentFuelCardApprovalResponse {
                this.fuelCard = fuelCard {
                    this.currencyCode = request.fuelCard.currencyCode
                    this.fuelCardId = request.fuelCardId
                }
                this.outcome = CommentFuelCardApprovalResponseKt.outcome {
                    this.message = reply1.outcome.message
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.details.putAll(reply2.result.detailsMap)
                    this.referenceIds.addAll(reply3.result.referenceIdsList)
                    this.status = reply2.result.status
                }
            }
        }

    private suspend fun handleDelegateFuelCardApproval(request: DelegateFuelCardApprovalRequest): DelegateFuelCardApprovalResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardApprovalService/DelegateFuelCardApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DelegateFuelCardApproval", request)
            log.atInfo().addKeyValue("operation", "DelegateFuelCardApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCard.fuelCardId).log("Recorded audit detail")
            var reply0 = EstimateBudgetResponse.getDefaultInstance()
            request.fuelCard.limitsList.asFlow().map { item ->
                val outbound0 = estimateBudgetRequest {
                    this.budget = budget {
                        this.limits.addAll(listOf(budgetLimit {
                            this.amount = item.amount
                            this.name = item.name
                            this.period = item.period
                        }))
                    }
                    this.context = request.context
                }
                budgetingBudgetCalculationService.estimateBudget(outbound0)
            }.collect { reply0 = it }
            val outbound1 = CommentDockApprovalRequest.newBuilder().apply {
                setContext(request.context)
                setDetails(CommentDockApprovalRequestKt.details {
                    this.approver = userRef {
                        this.userId = request.details.approver.userId
                    }
                })
                setDock(dock {
                    this.audit = auditInfo {
                        this.createdBy = request.fuelCard.audit.createdBy
                        this.etag = request.fuelCard.audit.etag
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> yardsDockApprovalService.commentDockApproval(value) }
            delegateFuelCardApprovalResponse {
                this.fuelCard = fuelCard {
                    this.balance = request.fuelCard.balance
                    this.fuelCardId = request.fuelCardId
                }
                this.outcome = DelegateFuelCardApprovalResponseKt.outcome {
                    this.message = reply1.outcome.message
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleListPendingFuelCards(request: ListPendingFuelCardsRequest): ListPendingFuelCardsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardApprovalService/ListPendingFuelCards") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListPendingFuelCards", request)
            log.atInfo().addKeyValue("operation", "ListPendingFuelCards").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = commentDockApprovalRequest {
                this.context = request.context
                this.dock = dock {
                    this.address = postalAddress {
                        this.region = request.filter.region
                    }
                }
            }
            val reply0 = ClientRegistry.lookup("acme.yards.v1.DockApprovalService", DockApprovalServiceGrpcKt.DockApprovalServiceCoroutineStub::class.java).commentDockApproval(outbound0)
            val outbound1 = resolveAccessPoliciesRequest {
                this.context = request.context
            }
            val reply1 = authorizationAccessPolicyQueryService.resolveAccessPolicies(WireCopies.reflective(outbound1))
            listPendingFuelCardsResponse {
                this.fuelCards.addAll(listOf(fuelCard {
                    this.holder = driverRef {
                        this.displayName = reply0.dock.owner.displayName
                    }
                }))
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleRecallFuelCard(request: RecallFuelCardRequest): RecallFuelCardResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardApprovalService/RecallFuelCard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecallFuelCard", request)
            log.atInfo().addKeyValue("operation", "RecallFuelCard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCard.fuelCardId).log("Recorded audit detail")
            val outbound0 = checkDriverEligibilityRequest {
                this.context = request.context
                this.details = CheckDriverEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.name = request.fuelCard.contact.name
                    }
                }
                this.driver = driver {
                    this.contact = contactInfo {
                        this.phone = request.fuelCard.contact.phone
                    }
                }
            }
            val reply0 = workerForRecallFuelCard0.submit(outbound0)
            var reply1 = CommentProofOfDeliveryApprovalResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<FuelCardLimit>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound1 = commentProofOfDeliveryApprovalRequest {
                            this.context = request.context
                            this.proofOfDelivery = proofOfDelivery {
                                this.card = proofOfDeliveryCardSettlement {
                                    this.last4 = request.fuelCard.card.last4
                                }
                                this.counterparty = driverRef {
                                    this.email = request.fuelCard.holder.email
                                }
                                this.lines.addAll(listOf(proofOfDeliveryLine {
                                    this.tax = proofOfDeliveryLineTax {
                                        this.amount = item.amount
                                    }
                                }))
                            }
                        }
                        reply1 = deliveriesProofOfDeliveryApprovalService.commentProofOfDeliveryApproval(outbound1)
                    }
                }
                try {
                    request.fuelCard.limitsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound2 = browseFuelTransactionsRequest {
                this.context = request.context
                this.filter = BrowseFuelTransactionsRequestKt.filter {
                    this.createdAfter = request.fuelCard.audit.updatedAt
                    this.labels.putAll(request.fuelCard.labelsMap)
                    this.region = reply0.driver.primaryAddress.region
                }
            }
            val reply2 = flowOf(outbound2).map { value -> fuelFuelTransactionQueryService.browseFuelTransactions(value) }.single()
            val outbound3 = generateFuelTransactionReportRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
            }
            val reply3 = withTimeout(5_000) { fuelFuelTransactionReportingService.generateFuelTransactionReport(outbound3) }
            recallFuelCardResponse {
                this.fuelCard = fuelCard {
                    this.fuelCardId = request.fuelCardId
                    this.holder = request.fuelCard.holder
                }
                this.outcome = RecallFuelCardResponseKt.outcome {
                    this.message = reply0.outcome.message
                }
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.details.putAll(reply2.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSubmitFuelCard(request: SubmitFuelCardRequest): SubmitFuelCardResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardApprovalService/SubmitFuelCard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SubmitFuelCard", request)
            log.atInfo().addKeyValue("operation", "SubmitFuelCard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCard.fuelCardId).log("Recorded audit detail")
            val outbound0 = pullFuelCardsRequest {
                this.context = request.context
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.etag = request.fuelCard.audit.etag
                    }
                    this.card = fuelCardCardFunding {
                        this.holderName = request.fuelCard.card.holderName
                        this.network = request.fuelCard.card.network
                    }
                    this.holder = driverRef {
                        this.email = request.details.approver.email
                    }
                }
            }
            val reply0 = fuelFuelCardSyncService.pullFuelCards(outbound0)
            val outbound1 = explainUserDecisionRequest {}.identityCopy {
                this.context = request.context
                this.user = user {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.fuelCard.audit.updatedAt
                    }
                    this.contact = contactInfo {
                        this.email = reply0.fuelCard.holder.email
                        this.preferredChannel = reply0.fuelCard.contact.preferredChannel
                    }
                }
            }
            val reply1 = identityUserValidationService.explainUserDecision(outbound1)
            submitFuelCardResponse {
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.createdAt = reply1.user.audit.createdAt
                    }
                    this.currencyCode = request.fuelCard.currencyCode
                    this.fuelCardId = request.fuelCardId
                    this.openedAt = reply0.fuelCard.openedAt
                }
                this.outcome = SubmitFuelCardResponseKt.outcome {
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }
}
