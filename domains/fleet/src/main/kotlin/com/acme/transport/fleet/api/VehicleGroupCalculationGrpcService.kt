package com.acme.transport.fleet.api

import com.acme.admissions.v1.AdmissionServiceGrpcKt
import com.acme.admissions.v1.batchGetAdmissionsRequest
import com.acme.admissions.v1.copy as admissionsCopy
import com.acme.authorization.v1.AccessPolicyServiceGrpcKt
import com.acme.authorization.v1.BatchGetAccessPoliciesRequest
import com.acme.authorization.v1.BatchGetAccessPoliciesResponse
import com.acme.authorization.v1.UpdateAccessPolicyRequestKt
import com.acme.authorization.v1.accessPolicy
import com.acme.authorization.v1.batchGetAccessPoliciesRequest
import com.acme.authorization.v1.updateAccessPolicyRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.customerRef
import com.acme.documents.v1.DocumentValidationServiceGrpcKt
import com.acme.documents.v1.document
import com.acme.documents.v1.normalizeDocumentRequest
import com.acme.drivers.v1.CompareDriverLicenseOptionsRequest
import com.acme.drivers.v1.CompareDriverLicenseOptionsRequestKt
import com.acme.drivers.v1.CompareDriverLicenseOptionsResponse
import com.acme.drivers.v1.DriverLicenseCalculationServiceGrpcKt
import com.acme.drivers.v1.DutyStatusCalculationServiceGrpcKt
import com.acme.drivers.v1.QuoteDutyStatusRequestKt
import com.acme.drivers.v1.compareDriverLicenseOptionsRequest
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.quoteDutyStatusRequest
import com.acme.fleet.v1.CalculateVehicleGroupRequest
import com.acme.fleet.v1.CalculateVehicleGroupResponse
import com.acme.fleet.v1.CompareVehicleGroupOptionsRequest
import com.acme.fleet.v1.CompareVehicleGroupOptionsResponse
import com.acme.fleet.v1.CompareVehicleGroupOptionsResponseKt
import com.acme.fleet.v1.EstimateVehicleGroupRequest
import com.acme.fleet.v1.EstimateVehicleGroupResponse
import com.acme.fleet.v1.ExplainVehicleGroupCalculationRequest
import com.acme.fleet.v1.ExplainVehicleGroupCalculationResponse
import com.acme.fleet.v1.ExplainVehicleGroupCalculationResponseKt
import com.acme.fleet.v1.GetVehicleGroupRatesRequest
import com.acme.fleet.v1.GetVehicleGroupRatesResponse
import com.acme.fleet.v1.QuoteVehicleGroupRequest
import com.acme.fleet.v1.QuoteVehicleGroupResponse
import com.acme.fleet.v1.RecalculateVehicleGroupsRequest
import com.acme.fleet.v1.RecalculateVehicleGroupsResponse
import com.acme.fleet.v1.VehicleGroupCalculationServiceGrpcKt
import com.acme.fleet.v1.calculateVehicleGroupResponse
import com.acme.fleet.v1.compareVehicleGroupOptionsResponse
import com.acme.fleet.v1.estimateVehicleGroupResponse
import com.acme.fleet.v1.explainVehicleGroupCalculationResponse
import com.acme.fleet.v1.getVehicleGroupRatesResponse
import com.acme.fleet.v1.quoteVehicleGroupResponse
import com.acme.fleet.v1.recalculateVehicleGroupsResponse
import com.acme.fleet.v1.vehicleGroup
import com.acme.fuel.v1.FuelTransactionValidationServiceGrpcKt
import com.acme.fuel.v1.ScoreFuelTransactionRequest
import com.acme.fuel.v1.ScoreFuelTransactionRequestKt
import com.acme.fuel.v1.ScoreFuelTransactionResponse
import com.acme.fuel.v1.ValidateFuelTransactionRequestKt
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.scoreFuelTransactionRequest
import com.acme.fuel.v1.validateFuelTransactionRequest
import com.acme.identity.v1.BatchGetUsersRequest
import com.acme.identity.v1.LintUsersRequest
import com.acme.identity.v1.PreviewUserRequest
import com.acme.identity.v1.PreviewUserRequestKt
import com.acme.identity.v1.PreviewUserResponse
import com.acme.identity.v1.UserServiceGrpcKt
import com.acme.identity.v1.UserValidationServiceGrpcKt
import com.acme.identity.v1.batchGetUsersRequest
import com.acme.identity.v1.lintUsersRequest
import com.acme.identity.v1.previewUserRequest
import com.acme.identity.v1.user
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.FindLedgerAccountsRequestKt
import com.acme.ledger.v1.LedgerAccountQueryServiceGrpcKt
import com.acme.ledger.v1.findLedgerAccountsRequest
import com.acme.metrics.v1.MetricSeriesQueryServiceGrpcKt
import com.acme.metrics.v1.SuggestMetricSeriesesRequestKt
import com.acme.metrics.v1.suggestMetricSeriesesRequest
import com.acme.routing.v1.EstimateRoutePlanRequestKt
import com.acme.routing.v1.RouteCalculationServiceGrpcKt
import com.acme.routing.v1.RoutePlanCalculationServiceGrpcKt
import com.acme.routing.v1.estimateRoutePlanRequest
import com.acme.routing.v1.estimateRouteRequest
import com.acme.routing.v1.route
import com.acme.routing.v1.routePlan
import com.acme.search.v1.SearchIndexServiceGrpcKt
import com.acme.search.v1.SearchSearchIndexesRequest
import com.acme.search.v1.SearchSearchIndexesRequestKt
import com.acme.search.v1.searchSearchIndexesRequest
import com.acme.sponsorships.v1.SponsorQueryServiceGrpcKt
import com.acme.sponsorships.v1.resolveSponsorsRequest
import com.acme.transport.fleet.FleetRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.google.protobuf.Timestamp
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Computes the figures of a vehicle group, such as totals, taxes, and estimates. */
class VehicleGroupCalculationGrpcService(
    private val store: FleetRepository,
    private val workers: CoroutineScope,
    private val admissionsAdmissionService: AdmissionServiceGrpcKt.AdmissionServiceCoroutineStub,
    private val authorizationAccessPolicyService: AccessPolicyServiceGrpcKt.AccessPolicyServiceCoroutineStub,
    private val documentsDocumentValidationService: DocumentValidationServiceGrpcKt.DocumentValidationServiceCoroutineStub,
    private val driversDriverLicenseCalculationService: DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub,
    private val driversDutyStatusCalculationService: DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineStub,
    private val fuelFuelTransactionValidationService: FuelTransactionValidationServiceGrpcKt.FuelTransactionValidationServiceCoroutineStub,
    private val identityUserService: UserServiceGrpcKt.UserServiceCoroutineStub,
    private val identityUserValidationService: UserValidationServiceGrpcKt.UserValidationServiceCoroutineStub,
    private val ledgerLedgerAccountQueryService: LedgerAccountQueryServiceGrpcKt.LedgerAccountQueryServiceCoroutineStub,
    private val metricsMetricSeriesQueryService: MetricSeriesQueryServiceGrpcKt.MetricSeriesQueryServiceCoroutineStub,
    private val routingRouteCalculationService: RouteCalculationServiceGrpcKt.RouteCalculationServiceCoroutineStub,
    private val routingRoutePlanCalculationService: RoutePlanCalculationServiceGrpcKt.RoutePlanCalculationServiceCoroutineStub,
    private val searchSearchIndexService: SearchIndexServiceGrpcKt.SearchIndexServiceCoroutineStub,
    private val sponsorshipsSponsorQueryService: SponsorQueryServiceGrpcKt.SponsorQueryServiceCoroutineStub,
) : VehicleGroupCalculationServiceGrpcKt.VehicleGroupCalculationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(VehicleGroupCalculationGrpcService::class.java)

    private val invokeCompareVehicleGroupOptions1: suspend (CompareDriverLicenseOptionsRequest) -> CompareDriverLicenseOptionsResponse = { payload ->
        driversDriverLicenseCalculationService.compareDriverLicenseOptions(payload)
    }

    private val workerForExplainVehicleGroupCalculation2 = ReplyMailbox<BatchGetAccessPoliciesRequest, BatchGetAccessPoliciesResponse>(workers) { payload ->
        withTimeout(5_000) { authorizationAccessPolicyService.batchGetAccessPolicies(payload) }
    }

    private val clientForExplainVehicleGroupCalculation3 by lazy { fuelFuelTransactionValidationService }

    /** Calculates a vehicle group. */
    override suspend fun calculateVehicleGroup(request: CalculateVehicleGroupRequest): CalculateVehicleGroupResponse {
        return handleCalculateVehicleGroup(request)
    }

    /** Compares a vehicle group. */
    override suspend fun compareVehicleGroupOptions(request: CompareVehicleGroupOptionsRequest): CompareVehicleGroupOptionsResponse = handleCompareVehicleGroupOptions(request)

    /** Estimates a vehicle group. */
    override suspend fun estimateVehicleGroup(request: EstimateVehicleGroupRequest): EstimateVehicleGroupResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupCalculationService/EstimateVehicleGroup") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EstimateVehicleGroup", request)
            log.atInfo().addKeyValue("operation", "EstimateVehicleGroup").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicleGroup.vehicleGroupId).log("Recorded audit detail")
            val outbound0 = BatchGetUsersRequest.newBuilder().apply {
                setContext(request.context)
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> identityUserService.batchGetUsers(value) }
            val outbound1 = RequestFactory.create {
                quoteDutyStatusRequest {
                    this.context = request.context
                    this.details = QuoteDutyStatusRequestKt.details {
                        this.destination = postalAddress {
                            this.countryCode = request.details.destination.countryCode
                            this.locality = request.details.destination.locality
                            this.region = request.vehicleGroup.location.region
                        }
                    }
                }
            }
            val reply1 = driversDutyStatusCalculationService.quoteDutyStatus(outbound1)
            estimateVehicleGroupResponse {
                this.result = operationResult {
                    this.correlationId = reply1.dutyStatus.correlationId
                    this.status = reply0.result.status
                }
                this.vehicleGroup = vehicleGroup {
                    this.attributes.putAll(request.vehicleGroup.attributesMap)
                    this.vehicleGroupId = request.vehicleGroupId
                }
            }
        }

    /** Explains a vehicle group. */
    override suspend fun explainVehicleGroupCalculation(request: ExplainVehicleGroupCalculationRequest): ExplainVehicleGroupCalculationResponse {
        return handleExplainVehicleGroupCalculation(request)
    }

    /** Returns a vehicle group. */
    override suspend fun getVehicleGroupRates(request: GetVehicleGroupRatesRequest): GetVehicleGroupRatesResponse = handleGetVehicleGroupRates(request)

    /** Quotes a vehicle group. */
    override suspend fun quoteVehicleGroup(request: QuoteVehicleGroupRequest): QuoteVehicleGroupResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupCalculationService/QuoteVehicleGroup") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "QuoteVehicleGroup", request)
            log.atInfo().addKeyValue("operation", "QuoteVehicleGroup").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicleGroup.vehicleGroupId).log("Recorded audit detail")
            val outbound0 = normalizeDocumentRequest {
                this.context = request.context
                this.document = document {
                    this.audit = auditInfo {
                        this.createdBy = request.vehicleGroup.audit.createdBy
                    }
                    this.billingAddress = postalAddress {
                        this.recipient = request.details.destination.recipient
                        this.region = request.vehicleGroup.location.region
                    }
                }
            }
            val reply0 = withTimeout(5_000) { documentsDocumentValidationService.normalizeDocument(outbound0) }
            val configure1: UpdateAccessPolicyRequestKt.Dsl.() -> Unit = {
                this.accessPolicy = accessPolicy {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.document.audit.updatedAt
                    }
                }
                this.context = request.context
            }
            val outbound1 = updateAccessPolicyRequest(configure1)
            val reply1 = authorizationAccessPolicyService.updateAccessPolicy(outbound1)
            val outbound2 = findLedgerAccountsRequest {
                this.context = request.context
                this.filter = FindLedgerAccountsRequestKt.filter {
                    this.createdAfter = reply1.accessPolicy.audit.createdAt
                    this.region = reply0.document.billingAddress.region
                }
            }
            val reply2 = ledgerLedgerAccountQueryService.findLedgerAccounts(outbound2)
            quoteVehicleGroupResponse {
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.message = reply0.outcome.message
                    this.status = "ok"
                }
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.audit = auditInfo {
                            this.createdAt = reply1.accessPolicy.audit.createdAt
                        }
                        this.children.addAll(listOf(vehicleGroup {
                            this.manager = userRef {
                                this.userId = reply1.accessPolicy.owner.userId
                            }
                        }))
                    }))
                    this.name = request.vehicleGroup.name
                    this.vehicleGroupId = request.vehicleGroupId
                }
            }
        }

    /** Recalculates a vehicle group. */
    override suspend fun recalculateVehicleGroups(request: RecalculateVehicleGroupsRequest): RecalculateVehicleGroupsResponse {
        return handleRecalculateVehicleGroups(request)
    }

    private data class CalculateVehicleGroup0Command(
        val context: RequestContext,
        val filterRegion: String,
        val filterCreatedAfter: Timestamp
    ) {
        fun toRequest(): SearchSearchIndexesRequest {
            val command = this
            return searchSearchIndexesRequest {
                this.context = command.context
                this.filter = SearchSearchIndexesRequestKt.filter {
                    this.createdAfter = command.filterCreatedAfter
                    this.region = command.filterRegion
                }
            }
        }
    }

    private suspend fun handleCalculateVehicleGroup(request: CalculateVehicleGroupRequest): CalculateVehicleGroupResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupCalculationService/CalculateVehicleGroup") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CalculateVehicleGroup", request)
            log.atInfo().addKeyValue("operation", "CalculateVehicleGroup").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicleGroup.vehicleGroupId).log("Recorded audit detail")
            val outbound0 = CalculateVehicleGroup0Command(
                context = request.context,
                filterRegion = request.vehicleGroup.location.region,
                filterCreatedAfter = request.vehicleGroup.audit.updatedAt
            ).toRequest()
            val reply0 = searchSearchIndexService.searchSearchIndexes(outbound0)
            val outbound1 = lintUsersRequest {
                this.context = request.context
                this.user = user {
                    this.audit = auditInfo {
                        this.updatedAt = request.vehicleGroup.audit.updatedAt
                    }
                    this.contact = contactInfo {
                        this.email = request.vehicleGroup.manager.email
                    }
                    this.primaryAddress = postalAddress {
                        this.countryCode = request.details.destination.countryCode
                        this.recipient = request.vehicleGroup.location.recipient
                    }
                }
            }
            val command1: Submission<LintUsersRequest> = Submission.Live(outbound1)
            val payload1 = when (val command = command1) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply1 = identityUserValidationService.lintUsers(payload1)
            calculateVehicleGroupResponse {
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.audit = auditInfo {
                            this.etag = reply1.user.audit.etag
                        }
                        this.location = postalAddress {
                            this.countryCode = reply1.user.primaryAddress.countryCode
                        }
                    }))
                    this.parentId = request.vehicleGroup.parentId
                    this.vehicleGroupId = request.vehicleGroupId
                }
            }
        }

    private suspend fun handleCompareVehicleGroupOptions(request: CompareVehicleGroupOptionsRequest): CompareVehicleGroupOptionsResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupCalculationService/CompareVehicleGroupOptions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CompareVehicleGroupOptions", request)
            log.atInfo().addKeyValue("operation", "CompareVehicleGroupOptions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicleGroup.vehicleGroupId).log("Recorded audit detail")
            val outbound0 = previewUserRequest {
                this.context = request.context
                this.details = PreviewUserRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.vehicleGroup.manager.email
                    }
                }
                this.user = user {
                    this.audit = auditInfo {
                        this.etag = request.vehicleGroup.audit.etag
                    }
                }
            }
            val adapter0 = OutboundPort<PreviewUserRequest, PreviewUserResponse> { payload ->
                identityUserValidationService.previewUser(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = compareDriverLicenseOptionsRequest {
                this.context = request.context
                this.details = CompareDriverLicenseOptionsRequestKt.details {
                    this.destination = postalAddress {
                        this.locality = request.vehicleGroup.location.locality
                    }
                }
                this.driverLicense = driverLicense {
                    this.billingAddress = postalAddress {
                        this.postalCode = reply0.user.primaryAddress.postalCode
                        this.recipient = request.vehicleGroup.location.recipient
                    }
                }
            }
            val reply1 = invokeCompareVehicleGroupOptions1(outbound1)
            val outbound2 = scoreFuelTransactionRequest {
                this.context = request.context
                this.details = ScoreFuelTransactionRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(reply0.user.primaryAddress.linesList)
                        this.postalCode = reply0.user.primaryAddress.postalCode
                    }
                }
            }
            val invoke2: suspend (ScoreFuelTransactionRequest, Metadata) -> ScoreFuelTransactionResponse = fuelFuelTransactionValidationService::scoreFuelTransaction
            val reply2 = invoke2(outbound2, Metadata())
            compareVehicleGroupOptionsResponse {
                this.breakdown = CompareVehicleGroupOptionsResponseKt.breakdown {
                    this.currencyCode = request.details.currencyCode
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.location = postalAddress {
                                this.countryCode = reply1.driverLicense.billingAddress.countryCode
                                this.recipient = reply1.driverLicense.billingAddress.recipient
                            }
                        }))
                    }))
                    this.vehicleGroupId = request.vehicleGroupId
                }
            }
        }

    private suspend fun handleExplainVehicleGroupCalculation(request: ExplainVehicleGroupCalculationRequest): ExplainVehicleGroupCalculationResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupCalculationService/ExplainVehicleGroupCalculation") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainVehicleGroupCalculation", request)
            log.atInfo().addKeyValue("operation", "ExplainVehicleGroupCalculation").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicleGroup.vehicleGroupId).log("Recorded audit detail")
            val outbound0 = estimateRoutePlanRequest {
                this.context = request.context
                this.details = EstimateRoutePlanRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = request.vehicleGroup.location.countryCode
                        this.postalCode = request.vehicleGroup.location.postalCode
                    }
                }
                this.routePlan = routePlan {
                    this.audit = auditInfo {
                        this.etag = request.vehicleGroup.audit.etag
                    }
                    this.billingAddress = postalAddress {
                        this.locality = request.vehicleGroup.location.locality
                    }
                }
            }
            val reply0 = ClientRegistry.lookup("acme.routing.v1.RoutePlanCalculationService", RoutePlanCalculationServiceGrpcKt.RoutePlanCalculationServiceCoroutineStub::class.java).estimateRoutePlan(outbound0)
            val outbound1 = estimateRouteRequest {
                this.context = request.context
                this.route = route {
                    this.billingAddress = postalAddress {
                        this.postalCode = reply0.routePlan.billingAddress.postalCode
                    }
                }
            }
            val reply1 = routingRouteCalculationService.estimateRoute(WireCopies.reflective(outbound1))
            val outbound2 = batchGetAccessPoliciesRequest {
                this.context = request.context
            }
            val reply2 = workerForExplainVehicleGroupCalculation2.submit(outbound2)
            val outbound3 = validateFuelTransactionRequest {
                this.context = request.context
                this.details = ValidateFuelTransactionRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.details.contact.email
                    }
                    this.locale = request.details.locale
                }
                this.fuelTransaction = fuelTransaction {
                    this.subject = customerRef {
                        this.displayName = request.vehicleGroup.manager.displayName
                        this.email = reply0.routePlan.counterparty.email
                    }
                }
            }
            val reply3 = clientForExplainVehicleGroupCalculation3.validateFuelTransaction(outbound3)
            explainVehicleGroupCalculationResponse {
                this.outcome = ExplainVehicleGroupCalculationResponseKt.outcome {
                    this.status = reply2.result.status
                }
                this.result = operationResult {
                    this.details.putAll(reply2.result.detailsMap)
                    this.status = reply1.result.status
                }
                this.vehicleGroup = vehicleGroup {
                    this.attributes.putAll(request.vehicleGroup.attributesMap)
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.audit = auditInfo {
                                this.etag = reply1.route.audit.etag
                            }
                            this.location = postalAddress {
                                this.region = reply0.routePlan.billingAddress.region
                            }
                        }))
                        this.manager = userRef {
                            this.displayName = reply0.routePlan.counterparty.displayName
                        }
                    }))
                    this.vehicleGroupId = request.vehicleGroupId
                }
            }
        }

    private suspend fun handleGetVehicleGroupRates(request: GetVehicleGroupRatesRequest): GetVehicleGroupRatesResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupCalculationService/GetVehicleGroupRates") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetVehicleGroupRates", request)
            log.atInfo().addKeyValue("operation", "GetVehicleGroupRates").log("Handling transport request")
            val outbound0 = resolveSponsorsRequest {
                this.context = request.context
            }
            val reply0 = flowOf(outbound0).map { value -> sponsorshipsSponsorQueryService.resolveSponsors(value) }.single()
            getVehicleGroupRatesResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
                this.vehicleGroup = vehicleGroup {
                    this.vehicleGroupId = request.vehicleGroupId
                }
            }
        }

    private suspend fun handleRecalculateVehicleGroups(request: RecalculateVehicleGroupsRequest): RecalculateVehicleGroupsResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupCalculationService/RecalculateVehicleGroups") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecalculateVehicleGroups", request)
            log.atInfo().addKeyValue("operation", "RecalculateVehicleGroups").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicleGroup.vehicleGroupId).log("Recorded audit detail")
            val outbound0 = suggestMetricSeriesesRequest {
                this.context = request.context
                this.filter = SuggestMetricSeriesesRequestKt.filter {
                    this.createdAfter = request.vehicleGroup.audit.createdAt
                    this.region = request.details.destination.region
                }
            }
            val reply0 = metricsMetricSeriesQueryService.suggestMetricSerieses(outbound0)
            val outbound1 = batchGetAdmissionsRequest {}.admissionsCopy {
                this.context = request.context
            }
            val reply1 = admissionsAdmissionService.batchGetAdmissions(outbound1)
            recalculateVehicleGroupsResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.message = reply1.result.message
                    this.status = "ok"
                }
                this.vehicleGroup = vehicleGroup {
                    this.name = request.vehicleGroup.name
                    this.vehicleGroupId = request.vehicleGroupId
                }
            }
        }
}
