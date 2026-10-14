package com.acme.transport.telematics.grpc

import com.acme.authorization.v1.AccessPolicyQueryServiceGrpcKt
import com.acme.authorization.v1.ExportAccessPoliciesRequestKt
import com.acme.authorization.v1.exportAccessPoliciesRequest
import com.acme.common.v1.Money
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.customers.v1.CustomerSegment
import com.acme.customers.v1.CustomerSegmentCalculationServiceGrpcKt
import com.acme.customers.v1.CustomerSegmentValidationServiceGrpcKt
import com.acme.customers.v1.EstimateHouseholdRequest
import com.acme.customers.v1.HouseholdCalculationServiceGrpcKt
import com.acme.customers.v1.LintCustomerSegmentsRequest
import com.acme.customers.v1.LintCustomerSegmentsResponse
import com.acme.customers.v1.calculateCustomerSegmentRequest
import com.acme.customers.v1.customerSegment
import com.acme.customers.v1.customerSegmentBlockAction
import com.acme.customers.v1.estimateHouseholdRequest
import com.acme.customers.v1.explainHouseholdCalculationRequest
import com.acme.customers.v1.household
import com.acme.customers.v1.lintCustomerSegmentsRequest
import com.acme.fuel.v1.CommentFuelPolicyApprovalRequest
import com.acme.fuel.v1.CommentFuelPolicyApprovalResponse
import com.acme.fuel.v1.FuelPolicyApprovalServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyExportServiceGrpcKt
import com.acme.fuel.v1.commentFuelPolicyApprovalRequest
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.fuelPolicyRouteAction
import com.acme.fuel.v1.retryFuelPolicyExportRequest
import com.acme.identity.v1.SearchUsersRequest
import com.acme.identity.v1.SearchUsersRequestKt
import com.acme.identity.v1.UserServiceGrpcKt
import com.acme.identity.v1.searchUsersRequest
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.LedgerAccountServiceGrpcKt
import com.acme.ledger.v1.createLedgerAccountRequest
import com.acme.ledger.v1.ledgerAccount
import com.acme.ledger.v1.ledgerAccountBankFunding
import com.acme.metrics.v1.CountMetricSeriesesRequestKt
import com.acme.metrics.v1.MetricSeriesQueryServiceGrpcKt
import com.acme.metrics.v1.copy as metricsCopy
import com.acme.metrics.v1.countMetricSeriesesRequest
import com.acme.onboarding.v1.OnboardingPlanServiceGrpcKt
import com.acme.onboarding.v1.WatchOnboardingPlansRequestKt
import com.acme.onboarding.v1.watchOnboardingPlansRequest
import com.acme.telematics.v1.ApproveGeoFenceRequest
import com.acme.telematics.v1.ApproveGeoFenceResponse
import com.acme.telematics.v1.CommentGeoFenceApprovalRequest
import com.acme.telematics.v1.CommentGeoFenceApprovalResponse
import com.acme.telematics.v1.CommentGeoFenceApprovalResponseKt
import com.acme.telematics.v1.DelegateGeoFenceApprovalRequest
import com.acme.telematics.v1.DelegateGeoFenceApprovalResponse
import com.acme.telematics.v1.DelegateGeoFenceApprovalResponseKt
import com.acme.telematics.v1.ExpireGeoFenceRequestKt
import com.acme.telematics.v1.GeoFenceApprovalServiceGrpcKt
import com.acme.telematics.v1.GeoFenceImportServiceGrpcKt
import com.acme.telematics.v1.GeoFenceLifecycleServiceGrpcKt
import com.acme.telematics.v1.GeoFenceQueryServiceGrpcKt
import com.acme.telematics.v1.GeoFenceReportingServiceGrpcKt
import com.acme.telematics.v1.RecallGeoFenceRequest
import com.acme.telematics.v1.RecallGeoFenceResponse
import com.acme.telematics.v1.RecallGeoFenceResponseKt
import com.acme.telematics.v1.RejectGeoFenceRequest
import com.acme.telematics.v1.RejectGeoFenceResponse
import com.acme.telematics.v1.RejectGeoFenceResponseKt
import com.acme.telematics.v1.SubmitGeoFenceRequest
import com.acme.telematics.v1.SubmitGeoFenceResponse
import com.acme.telematics.v1.TripLogSyncServiceGrpcKt
import com.acme.telematics.v1.approveGeoFenceResponse
import com.acme.telematics.v1.commentGeoFenceApprovalResponse
import com.acme.telematics.v1.delegateGeoFenceApprovalResponse
import com.acme.telematics.v1.expireGeoFenceRequest
import com.acme.telematics.v1.generateGeoFenceReportRequest
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.geoFenceRouteAction
import com.acme.telematics.v1.pushTripLogRequest
import com.acme.telematics.v1.recallGeoFenceResponse
import com.acme.telematics.v1.rejectGeoFenceResponse
import com.acme.telematics.v1.submitGeoFenceResponse
import com.acme.telematics.v1.summarizeGeoFencesRequest
import com.acme.telematics.v1.tripLog
import com.acme.telematics.v1.tripLogLine
import com.acme.telematics.v1.tripLogLineTax
import com.acme.telematics.v1.validateGeoFenceImportRequest
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.transport.telematics.TelematicsRepository
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Routes geo fences through approval, with escalation and delegation. */
class GeoFenceApprovalEndpoint(
    private val store: TelematicsRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyQueryService: AccessPolicyQueryServiceGrpcKt.AccessPolicyQueryServiceCoroutineStub,
    private val customersCustomerSegmentCalculationService: CustomerSegmentCalculationServiceGrpcKt.CustomerSegmentCalculationServiceCoroutineStub,
    private val customersCustomerSegmentValidationService: CustomerSegmentValidationServiceGrpcKt.CustomerSegmentValidationServiceCoroutineStub,
    private val customersHouseholdCalculationService: HouseholdCalculationServiceGrpcKt.HouseholdCalculationServiceCoroutineStub,
    private val fuelFuelPolicyApprovalService: FuelPolicyApprovalServiceGrpcKt.FuelPolicyApprovalServiceCoroutineStub,
    private val fuelFuelPolicyExportService: FuelPolicyExportServiceGrpcKt.FuelPolicyExportServiceCoroutineStub,
    private val identityUserService: UserServiceGrpcKt.UserServiceCoroutineStub,
    private val ledgerLedgerAccountService: LedgerAccountServiceGrpcKt.LedgerAccountServiceCoroutineStub,
    private val metricsMetricSeriesQueryService: MetricSeriesQueryServiceGrpcKt.MetricSeriesQueryServiceCoroutineStub,
    private val onboardingOnboardingPlanService: OnboardingPlanServiceGrpcKt.OnboardingPlanServiceCoroutineStub,
    private val telematicsGeoFenceImportService: GeoFenceImportServiceGrpcKt.GeoFenceImportServiceCoroutineStub,
    private val telematicsGeoFenceLifecycleService: GeoFenceLifecycleServiceGrpcKt.GeoFenceLifecycleServiceCoroutineStub,
    private val telematicsGeoFenceQueryService: GeoFenceQueryServiceGrpcKt.GeoFenceQueryServiceCoroutineStub,
    private val telematicsGeoFenceReportingService: GeoFenceReportingServiceGrpcKt.GeoFenceReportingServiceCoroutineStub,
    private val telematicsTripLogSyncService: TripLogSyncServiceGrpcKt.TripLogSyncServiceCoroutineStub,
) : GeoFenceApprovalServiceGrpcKt.GeoFenceApprovalServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(GeoFenceApprovalEndpoint::class.java)

    private val workerForCommentGeoFenceApproval1 = ReplyMailbox<LintCustomerSegmentsRequest, LintCustomerSegmentsResponse>(workers) { payload ->
        withTimeout(5_000) { customersCustomerSegmentValidationService.lintCustomerSegments(payload) }
    }

    /** Approves a geo fence. */
    override suspend fun approveGeoFence(request: ApproveGeoFenceRequest): ApproveGeoFenceResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceApprovalService/ApproveGeoFence") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ApproveGeoFence", request)
            log.atInfo().addKeyValue("operation", "ApproveGeoFence").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.geoFence.name).log("Recorded audit detail")
            val outbound0 = commentFuelPolicyApprovalRequest {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.etag = request.geoFence.audit.etag
                    }
                    this.owner = userRef {
                        this.email = request.geoFence.owner.email
                    }
                    this.route = fuelPolicyRouteAction {
                        this.weight = request.geoFence.route.weight
                    }
                }
            }
            val invoke0: suspend (CommentFuelPolicyApprovalRequest, Metadata) -> CommentFuelPolicyApprovalResponse = fuelFuelPolicyApprovalService::commentFuelPolicyApproval
            val reply0 = invoke0(outbound0, Metadata())
            val outbound1 = EstimateHouseholdRequest.newBuilder().apply {
                setContext(request.context)
                setHousehold(household {
                    this.children.addAll(listOf(household {
                        this.children.addAll(listOf(household {
                            this.audit = auditInfo {
                                this.updatedBy = request.geoFence.audit.updatedBy
                            }
                            this.manager = userRef {
                                this.email = request.geoFence.owner.email
                            }
                        }))
                    }))
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> customersHouseholdCalculationService.estimateHousehold(value) }
            val outbound2 = RequestFactory.create {
                retryFuelPolicyExportRequest {
                    this.context = request.context
                    this.fuelPolicy = fuelPolicy {
                        this.audit = auditInfo {
                            this.updatedAt = request.geoFence.audit.updatedAt
                        }
                    }
                }
            }
            val reply2 = fuelFuelPolicyExportService.retryFuelPolicyExport(outbound2)
            val outbound3 = generateGeoFenceReportRequest {
                this.context = request.context
                this.geoFenceId = request.geoFence.geoFenceId
                this.reason = reply0.fuelPolicy.block.reason
            }
            val reply3 = ClientRegistry.lookup("acme.telematics.v1.GeoFenceReportingService", GeoFenceReportingServiceGrpcKt.GeoFenceReportingServiceCoroutineStub::class.java).generateGeoFenceReport(outbound3)
            approveGeoFenceResponse {
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.createdAt = reply1.household.audit.createdAt
                    }
                    this.geoFenceId = request.geoFenceId
                    this.owner = request.geoFence.owner
                    this.version = reply0.fuelPolicy.version
                }
                this.result = operationResult {
                    this.message = reply3.result.message
                    this.referenceIds.addAll((reply1.result.referenceIdsList) + (reply3.result.referenceIdsList))
                    this.status = "ok"
                }
            }
        }

    /** Comments a geo fence. */
    override suspend fun commentGeoFenceApproval(request: CommentGeoFenceApprovalRequest): CommentGeoFenceApprovalResponse {
        return handleCommentGeoFenceApproval(request)
    }

    /** Delegates a geo fence. */
    override suspend fun delegateGeoFenceApproval(request: DelegateGeoFenceApprovalRequest): DelegateGeoFenceApprovalResponse = handleDelegateGeoFenceApproval(request)

    /** Recalls a geo fence. */
    override suspend fun recallGeoFence(request: RecallGeoFenceRequest): RecallGeoFenceResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceApprovalService/RecallGeoFence") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecallGeoFence", request)
            log.atInfo().addKeyValue("operation", "RecallGeoFence").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.geoFence.geoFenceId).log("Recorded audit detail")
            val outbound0 = watchOnboardingPlansRequest {
                this.context = request.context
                this.filter = WatchOnboardingPlansRequestKt.filter {
                    this.createdAfter = request.geoFence.audit.updatedAt
                }
                this.subscriber = contactInfo {
                    this.email = request.geoFence.owner.email
                }
            }
            onboardingOnboardingPlanService.watchOnboardingPlans(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = explainHouseholdCalculationRequest {
                this.context = request.context
                this.household = household {
                    this.children.addAll(listOf(household {
                        this.children.addAll(listOf(household {
                            this.audit = auditInfo {
                                this.updatedBy = request.geoFence.audit.updatedBy
                            }
                            this.manager = userRef {
                                this.displayName = request.details.approver.displayName
                                this.email = request.geoFence.owner.email
                            }
                        }))
                    }))
                }
            }
            val reply1 = customersHouseholdCalculationService.explainHouseholdCalculation(outbound1)
            recallGeoFenceResponse {
                this.geoFence = geoFence {
                    this.geoFenceId = request.geoFenceId
                    this.priority = request.geoFence.priority
                }
                this.outcome = RecallGeoFenceResponseKt.outcome {
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Rejects a geo fence. */
    override suspend fun rejectGeoFence(request: RejectGeoFenceRequest): RejectGeoFenceResponse {
        return handleRejectGeoFence(request)
    }

    /** Submits a geo fence. */
    override suspend fun submitGeoFence(request: SubmitGeoFenceRequest): SubmitGeoFenceResponse = handleSubmitGeoFence(request)

    private suspend fun handleCommentGeoFenceApproval(request: CommentGeoFenceApprovalRequest): CommentGeoFenceApprovalResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceApprovalService/CommentGeoFenceApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CommentGeoFenceApproval", request)
            log.atInfo().addKeyValue("operation", "CommentGeoFenceApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.geoFence.geoFenceId).log("Recorded audit detail")
            val outbound0 = validateGeoFenceImportRequest {
                this.context = request.context
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.updatedBy = request.geoFence.audit.updatedBy
                    }
                    this.owner = userRef {
                        this.userId = request.geoFence.owner.userId
                    }
                    this.route = geoFenceRouteAction {
                        this.target = request.geoFence.route.target
                    }
                }
            }
            val reply0 = telematicsGeoFenceImportService.validateGeoFenceImport(WireCopies.reflective(outbound0))
            val outbound1 = lintCustomerSegmentsRequest {
                this.context = request.context
                this.customerSegment = customerSegment {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.geoFence.audit.updatedAt
                    }
                    this.owner = userRef {
                        this.displayName = reply0.geoFence.owner.displayName
                    }
                }
            }
            val reply1 = workerForCommentGeoFenceApproval1.submit(outbound1)
            val outbound2 = pushTripLogRequest {
                this.context = request.context
                this.tripLog = tripLog {
                    this.lines.addAll(listOf(tripLogLine {
                        this.tax = tripLogLineTax {
                            this.amount = when (reply1.customerSegment.actionCase) {
                                CustomerSegment.ActionCase.DISCOUNT -> reply1.customerSegment.discount.amount
                                else -> Money.getDefaultInstance()
                            }
                        }
                    }))
                }
            }
            val reply2 = telematicsTripLogSyncService.pushTripLog(outbound2)
            val outbound3 = createLedgerAccountRequest {
                this.context = request.context
                this.ledgerAccount = ledgerAccount {
                    this.audit = auditInfo {
                        this.createdAt = reply0.geoFence.audit.createdAt
                        this.etag = reply0.geoFence.audit.etag
                    }
                    this.bank = ledgerAccountBankFunding {
                        this.accountHolder = reply2.tripLog.bank.accountHolder
                    }
                    this.contact = contactInfo {
                        this.email = reply0.geoFence.owner.email
                    }
                }
            }
            val reply3 = flowOf(outbound3).map { value -> ledgerLedgerAccountService.createLedgerAccount(value) }.single()
            commentGeoFenceApprovalResponse {
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.createdBy = reply2.tripLog.audit.createdBy
                    }
                    this.description = request.geoFence.description
                    this.geoFenceId = request.geoFenceId
                    this.owner = userRef {
                        this.email = reply3.ledgerAccount.contact.email
                    }
                    this.scopes.addAll(reply1.customerSegment.scopesList)
                }
                this.outcome = CommentGeoFenceApprovalResponseKt.outcome {
                    this.message = reply0.outcome.message
                }
                this.result = operationResult {
                    this.details.putAll(reply3.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleDelegateGeoFenceApproval(request: DelegateGeoFenceApprovalRequest): DelegateGeoFenceApprovalResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceApprovalService/DelegateGeoFenceApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DelegateGeoFenceApproval", request)
            log.atInfo().addKeyValue("operation", "DelegateGeoFenceApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.geoFence.geoFenceId).log("Recorded audit detail")
            val outbound0 = validateGeoFenceImportRequest {
                this.context = request.context
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.updatedBy = request.geoFence.audit.updatedBy
                    }
                }
            }
            val reply0 = withTimeout(5_000) { telematicsGeoFenceImportService.validateGeoFenceImport(outbound0) }
            delegateGeoFenceApprovalResponse {
                this.geoFence = geoFence {
                    this.effective = reply0.geoFence.effective
                    this.geoFenceId = request.geoFenceId
                }
                this.outcome = DelegateGeoFenceApprovalResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private fun RejectGeoFenceRequest.toSearchUsersFromRejectGeoFence2(): SearchUsersRequest {
        val source = this
        return searchUsersRequest {
            this.context = source.context
            this.filter = SearchUsersRequestKt.filter {
                this.createdAfter = source.geoFence.audit.createdAt
                this.ownerId = source.details.approver.userId
            }
        }
    }

    private suspend fun handleRejectGeoFence(request: RejectGeoFenceRequest): RejectGeoFenceResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceApprovalService/RejectGeoFence") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RejectGeoFence", request)
            log.atInfo().addKeyValue("operation", "RejectGeoFence").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.geoFence.description).log("Recorded audit detail")
            val outbound0 = calculateCustomerSegmentRequest {
                this.context = request.context
                this.customerSegment = customerSegment {
                    this.block = customerSegmentBlockAction {
                        this.notifyEmail = request.geoFence.block.notifyEmail
                    }
                }
            }
            val reply0 = customersCustomerSegmentCalculationService.calculateCustomerSegment(outbound0)
            val outbound1 = countMetricSeriesesRequest {}.metricsCopy {
                this.context = request.context
                this.filter = CountMetricSeriesesRequestKt.filter {
                    this.createdAfter = reply0.customerSegment.audit.createdAt
                }
            }
            val reply1 = metricsMetricSeriesQueryService.countMetricSerieses(outbound1)
            val outbound2 = request.toSearchUsersFromRejectGeoFence2()
            val reply2 = identityUserService.searchUsers(outbound2)
            val outbound3 = expireGeoFenceRequest {
                this.context = request.context
                this.details = ExpireGeoFenceRequestKt.details {
                    this.newOwner = userRef {
                        this.displayName = reply0.customerSegment.owner.displayName
                        this.email = request.details.approver.email
                        this.userId = request.details.approver.userId
                    }
                    this.notifyContact = contactInfo {
                        this.email = reply0.customerSegment.owner.email
                    }
                }
                this.geoFenceId = request.geoFence.geoFenceId
            }
            val reply3 = telematicsGeoFenceLifecycleService.expireGeoFence(outbound3)
            rejectGeoFenceResponse {
                this.geoFence = geoFence {
                    this.geoFenceId = request.geoFenceId
                    this.name = request.geoFence.name
                    this.owner = userRef {
                        this.email = reply0.customerSegment.owner.email
                    }
                    this.parameters.putAll(reply0.customerSegment.parametersMap)
                }
                this.outcome = RejectGeoFenceResponseKt.outcome {
                    this.nextSteps.addAll(reply3.outcome.nextStepsList)
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSubmitGeoFence(request: SubmitGeoFenceRequest): SubmitGeoFenceResponse =
        RpcBoundary.run("acme.telematics.v1.GeoFenceApprovalService/SubmitGeoFence") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SubmitGeoFence", request)
            log.atInfo().addKeyValue("operation", "SubmitGeoFence").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.geoFence.name).log("Recorded audit detail")
            val outbound0 = exportAccessPoliciesRequest {
                this.context = request.context
                this.filter = ExportAccessPoliciesRequestKt.filter {
                    this.createdAfter = request.geoFence.audit.createdAt
                    this.labels.putAll(request.geoFence.parametersMap)
                    this.ownerId = request.geoFenceId
                }
            }
            authorizationAccessPolicyQueryService.exportAccessPolicies(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = summarizeGeoFencesRequest {
                this.context = request.context
                this.geoFenceId = request.geoFence.geoFenceId
            }
            val reply1 = telematicsGeoFenceQueryService.summarizeGeoFences(WireCopies.json(outbound1))
            submitGeoFenceResponse {
                this.geoFence = geoFence {
                    this.description = request.geoFence.description
                    this.geoFenceId = request.geoFenceId
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = "ok"
                }
            }
        }
}
