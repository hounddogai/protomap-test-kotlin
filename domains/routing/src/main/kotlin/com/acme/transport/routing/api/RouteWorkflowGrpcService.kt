package com.acme.transport.routing.api

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.dispatch.v1.RecallZoneRequest
import com.acme.dispatch.v1.RecallZoneResponse
import com.acme.dispatch.v1.ZoneApprovalServiceGrpcKt
import com.acme.dispatch.v1.recallZoneRequest
import com.acme.dispatch.v1.zone
import com.acme.documents.v1.CheckDocumentEligibilityRequest
import com.acme.documents.v1.CheckDocumentEligibilityRequestKt
import com.acme.documents.v1.DocumentValidationServiceGrpcKt
import com.acme.documents.v1.checkDocumentEligibilityRequest
import com.acme.documents.v1.document
import com.acme.documents.v1.documentBankSettlement
import com.acme.documents.v1.documentFolderRef
import com.acme.drivers.v1.DriverCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverQueryServiceGrpcKt
import com.acme.drivers.v1.DriverValidationServiceGrpcKt
import com.acme.drivers.v1.DutyStatusCalculationServiceGrpcKt
import com.acme.drivers.v1.ExplainDutyStatusCalculationRequestKt
import com.acme.drivers.v1.GetDriverRulesRequest
import com.acme.drivers.v1.SummarizeDriversRequestKt
import com.acme.drivers.v1.driverLicenseRef
import com.acme.drivers.v1.dutyStatus
import com.acme.drivers.v1.explainDutyStatusCalculationRequest
import com.acme.drivers.v1.getDriverRatesRequest
import com.acme.drivers.v1.getDriverRulesRequest
import com.acme.drivers.v1.summarizeDriversRequest
import com.acme.fuel.v1.FuelPolicyApprovalServiceGrpcKt
import com.acme.fuel.v1.ListPendingFuelPoliciesRequestKt
import com.acme.fuel.v1.listPendingFuelPoliciesRequest
import com.acme.identity.v1.CountUsersRequestKt
import com.acme.identity.v1.UserQueryServiceGrpcKt
import com.acme.identity.v1.countUsersRequest
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.LedgerAccountServiceGrpcKt
import com.acme.ledger.v1.ListLedgerAccountsRequestKt
import com.acme.ledger.v1.SearchLedgerAccountsRequestKt
import com.acme.ledger.v1.listLedgerAccountsRequest
import com.acme.ledger.v1.searchLedgerAccountsRequest
import com.acme.metrics.v1.MetricSeriesServiceGrpcKt
import com.acme.metrics.v1.cloneMetricSeriesRequest
import com.acme.metrics.v1.metricSeries
import com.acme.routing.v1.AdvanceRouteWorkflowRequest
import com.acme.routing.v1.AdvanceRouteWorkflowResponse
import com.acme.routing.v1.CancelRouteWorkflowRequest
import com.acme.routing.v1.CancelRouteWorkflowResponse
import com.acme.routing.v1.DelegateStopApprovalRequestKt
import com.acme.routing.v1.EscalateStopRequestKt
import com.acme.routing.v1.ExplainRouteCalculationResponse
import com.acme.routing.v1.InspectRouteWorkflowRequest
import com.acme.routing.v1.InspectRouteWorkflowResponse
import com.acme.routing.v1.ListRouteWorkflowsRequest
import com.acme.routing.v1.ListRouteWorkflowsResponse
import com.acme.routing.v1.PauseRouteWorkflowRequest
import com.acme.routing.v1.PauseRouteWorkflowResponse
import com.acme.routing.v1.PauseRouteWorkflowResponseKt
import com.acme.routing.v1.RetryRouteWorkflowRequest
import com.acme.routing.v1.RetryRouteWorkflowResponse
import com.acme.routing.v1.RouteCalculationServiceGrpcKt
import com.acme.routing.v1.RouteWorkflowServiceGrpcKt
import com.acme.routing.v1.SignalRouteWorkflowRequest
import com.acme.routing.v1.SignalRouteWorkflowResponse
import com.acme.routing.v1.StartRouteWorkflowRequest
import com.acme.routing.v1.StartRouteWorkflowResponse
import com.acme.routing.v1.StopApprovalServiceGrpcKt
import com.acme.routing.v1.advanceRouteWorkflowResponse
import com.acme.routing.v1.cancelRouteWorkflowResponse
import com.acme.routing.v1.copy as routingCopy
import com.acme.routing.v1.delegateStopApprovalRequest
import com.acme.routing.v1.escalateStopRequest
import com.acme.routing.v1.explainRouteCalculationRequest
import com.acme.routing.v1.inspectRouteWorkflowResponse
import com.acme.routing.v1.listRouteWorkflowsResponse
import com.acme.routing.v1.pauseRouteWorkflowResponse
import com.acme.routing.v1.retryRouteWorkflowResponse
import com.acme.routing.v1.route
import com.acme.routing.v1.routeLine
import com.acme.routing.v1.routeLineTax
import com.acme.routing.v1.signalRouteWorkflowResponse
import com.acme.routing.v1.startRouteWorkflowResponse
import com.acme.routing.v1.stop
import com.acme.routing.v1.stopParticipant
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.transport.routing.RoutingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Runs the long-lived workflow that takes a route from start to finish. */
class RouteWorkflowGrpcService(
    private val store: RoutingRepository,
    private val workers: CoroutineScope,
    private val dispatchZoneApprovalService: ZoneApprovalServiceGrpcKt.ZoneApprovalServiceCoroutineStub,
    private val documentsDocumentValidationService: DocumentValidationServiceGrpcKt.DocumentValidationServiceCoroutineStub,
    private val driversDriverCalculationService: DriverCalculationServiceGrpcKt.DriverCalculationServiceCoroutineStub,
    private val driversDriverQueryService: DriverQueryServiceGrpcKt.DriverQueryServiceCoroutineStub,
    private val driversDriverValidationService: DriverValidationServiceGrpcKt.DriverValidationServiceCoroutineStub,
    private val driversDutyStatusCalculationService: DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineStub,
    private val fuelFuelPolicyApprovalService: FuelPolicyApprovalServiceGrpcKt.FuelPolicyApprovalServiceCoroutineStub,
    private val identityUserQueryService: UserQueryServiceGrpcKt.UserQueryServiceCoroutineStub,
    private val ledgerLedgerAccountService: LedgerAccountServiceGrpcKt.LedgerAccountServiceCoroutineStub,
    private val metricsMetricSeriesService: MetricSeriesServiceGrpcKt.MetricSeriesServiceCoroutineStub,
    private val routingRouteCalculationService: RouteCalculationServiceGrpcKt.RouteCalculationServiceCoroutineStub,
    private val routingStopApprovalService: StopApprovalServiceGrpcKt.StopApprovalServiceCoroutineStub,
) : RouteWorkflowServiceGrpcKt.RouteWorkflowServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(RouteWorkflowGrpcService::class.java)

    private val workerForCancelRouteWorkflow0 = ReplyMailbox<RecallZoneRequest, RecallZoneResponse>(workers) { payload ->
        withTimeout(5_000) { dispatchZoneApprovalService.recallZone(payload) }
    }

    private val clientForCancelRouteWorkflow1 by lazy { driversDriverCalculationService }

    /** Advances a route. */
    override suspend fun advanceRouteWorkflow(request: AdvanceRouteWorkflowRequest): AdvanceRouteWorkflowResponse {
        return handleAdvanceRouteWorkflow(request)
    }

    /** Cancels a route. */
    override suspend fun cancelRouteWorkflow(request: CancelRouteWorkflowRequest): CancelRouteWorkflowResponse = handleCancelRouteWorkflow(request)

    /** Returns a route. */
    override suspend fun inspectRouteWorkflow(request: InspectRouteWorkflowRequest): InspectRouteWorkflowResponse =
        RpcBoundary.run("acme.routing.v1.RouteWorkflowService/InspectRouteWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "InspectRouteWorkflow", request)
            log.atInfo().addKeyValue("operation", "InspectRouteWorkflow").log("Handling transport request")
            val outbound0 = listPendingFuelPoliciesRequest {
                this.context = request.context
                this.filter = ListPendingFuelPoliciesRequestKt.filter {
                    this.createdAfter = request.context.deadline
                    this.labels.putAll(request.context.baggageMap)
                    this.ownerId = request.routeId
                }
            }
            val reply0 = flowOf(outbound0).map { value -> fuelFuelPolicyApprovalService.listPendingFuelPolicies(value) }.single()
            inspectRouteWorkflowResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
                this.route = route {
                    this.routeId = request.routeId
                }
            }
        }

    /** Lists routes a page at a time. */
    override suspend fun listRouteWorkflows(request: ListRouteWorkflowsRequest): ListRouteWorkflowsResponse {
        return handleListRouteWorkflows(request)
    }

    /** Pauses a route. */
    override suspend fun pauseRouteWorkflow(request: PauseRouteWorkflowRequest): PauseRouteWorkflowResponse = handlePauseRouteWorkflow(request)

    /** Retrys a route. */
    override suspend fun retryRouteWorkflow(request: RetryRouteWorkflowRequest): RetryRouteWorkflowResponse =
        RpcBoundary.run("acme.routing.v1.RouteWorkflowService/RetryRouteWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RetryRouteWorkflow", request)
            log.atInfo().addKeyValue("operation", "RetryRouteWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.route.routeId).log("Recorded audit detail")
            val outbound0 = escalateStopRequest {}.routingCopy {
                this.context = request.context
                this.details = EscalateStopRequestKt.details {
                    this.approver = userRef {
                        this.displayName = request.route.counterparty.displayName
                    }
                }
                this.stop = stop {
                    this.audit = auditInfo {
                        this.createdBy = request.route.audit.createdBy
                    }
                }
            }
            val reply0 = routingStopApprovalService.escalateStop(outbound0)
            val outbound1 = request.toCheckDocumentEligibilityFromRetryRouteWorkflow1()
            val reply1 = documentsDocumentValidationService.checkDocumentEligibility(outbound1)
            val outbound2 = listLedgerAccountsRequest {
                this.context = request.context
                this.filter = ListLedgerAccountsRequestKt.filter {
                    this.createdAfter = request.route.audit.createdAt
                    this.region = reply0.stop.location.region
                }
            }
            val reply2 = ledgerLedgerAccountService.listLedgerAccounts(outbound2)
            retryRouteWorkflowResponse {
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.details.putAll(reply2.result.detailsMap)
                    this.status = "ok"
                }
                this.route = route {
                    this.billingAddress = postalAddress {
                        this.postalCode = reply0.stop.location.postalCode
                    }
                    this.dueAt = request.route.dueAt
                    this.routeId = request.routeId
                    this.total = reply1.document.total
                }
            }
        }

    /** Signals a route. */
    override suspend fun signalRouteWorkflow(request: SignalRouteWorkflowRequest): SignalRouteWorkflowResponse {
        return handleSignalRouteWorkflow(request)
    }

    /** Starts a route. */
    override suspend fun startRouteWorkflow(request: StartRouteWorkflowRequest): StartRouteWorkflowResponse = handleStartRouteWorkflow(request)

    private suspend fun handleAdvanceRouteWorkflow(request: AdvanceRouteWorkflowRequest): AdvanceRouteWorkflowResponse =
        RpcBoundary.run("acme.routing.v1.RouteWorkflowService/AdvanceRouteWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "AdvanceRouteWorkflow", request)
            log.atInfo().addKeyValue("operation", "AdvanceRouteWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.route.routeId).log("Recorded audit detail")
            val outbound0 = cloneMetricSeriesRequest {
                this.context = request.context
                this.metricSeries = metricSeries {
                    this.audit = auditInfo {
                        this.createdBy = request.route.audit.createdBy
                        this.updatedAt = request.route.audit.updatedAt
                    }
                    this.owner = userRef {
                        this.email = request.details.notify.email
                    }
                }
            }
            val reply0 = metricsMetricSeriesService.cloneMetricSeries(WireCopies.reflective(outbound0))
            advanceRouteWorkflowResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.route = route {
                    this.audit = auditInfo {
                        this.createdBy = reply0.metricSeries.audit.createdBy
                    }
                    this.billingAddress = postalAddress {
                        this.recipient = reply0.metricSeries.address.recipient
                    }
                    this.issuedAt = request.route.issuedAt
                    this.routeId = request.routeId
                }
            }
        }

    private suspend fun handleCancelRouteWorkflow(request: CancelRouteWorkflowRequest): CancelRouteWorkflowResponse =
        RpcBoundary.run("acme.routing.v1.RouteWorkflowService/CancelRouteWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CancelRouteWorkflow", request)
            log.atInfo().addKeyValue("operation", "CancelRouteWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.route.routeId).log("Recorded audit detail")
            val outbound0 = recallZoneRequest {
                this.context = request.context
                this.zone = zone {
                    this.children.addAll(listOf(zone {
                        this.audit = auditInfo {
                            this.createdBy = request.route.audit.createdBy
                        }
                        this.children.addAll(listOf(zone {
                            this.audit = auditInfo {
                                this.createdAt = request.route.audit.createdAt
                            }
                        }))
                    }))
                }
            }
            val reply0 = workerForCancelRouteWorkflow0.submit(outbound0)
            val outbound1 = getDriverRatesRequest {
                this.context = request.context
                this.driverId = request.route.counterparty.driverId
            }
            val reply1 = clientForCancelRouteWorkflow1.getDriverRates(outbound1)
            cancelRouteWorkflowResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.route = route {
                    this.audit = auditInfo {
                        this.etag = reply0.zone.audit.etag
                    }
                    this.billingAddress = postalAddress {
                        this.lines.addAll(reply1.driver.primaryAddress.linesList)
                    }
                    this.dueAt = request.route.dueAt
                    this.routeId = request.routeId
                }
            }
        }

    private suspend fun handleListRouteWorkflows(request: ListRouteWorkflowsRequest): ListRouteWorkflowsResponse =
        RpcBoundary.run("acme.routing.v1.RouteWorkflowService/ListRouteWorkflows") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListRouteWorkflows", request)
            log.atInfo().addKeyValue("operation", "ListRouteWorkflows").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = searchLedgerAccountsRequest {
                this.context = request.context
                this.filter = SearchLedgerAccountsRequestKt.filter {
                    this.query = request.filter.query
                }
            }
            val reply0 = withTimeout(5_000) { ledgerLedgerAccountService.searchLedgerAccounts(outbound0) }
            listRouteWorkflowsResponse {
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handlePauseRouteWorkflow(request: PauseRouteWorkflowRequest): PauseRouteWorkflowResponse =
        RpcBoundary.run("acme.routing.v1.RouteWorkflowService/PauseRouteWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PauseRouteWorkflow", request)
            log.atInfo().addKeyValue("operation", "PauseRouteWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.route.routeId).log("Recorded audit detail")
            val configure0: DelegateStopApprovalRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.stop = stop {
                    this.organizer = userRef {
                        this.displayName = request.route.counterparty.displayName
                    }
                    this.participants.addAll(listOf(stopParticipant {
                        this.contact = contactInfo {
                            this.email = request.route.counterparty.email
                            this.name = request.details.notify.name
                            this.preferredChannel = request.details.notify.preferredChannel
                        }
                    }))
                }
            }
            val outbound0 = delegateStopApprovalRequest(configure0)
            val reply0 = routingStopApprovalService.delegateStopApproval(outbound0)
            val outbound1 = explainDutyStatusCalculationRequest {
                this.context = request.context
                this.details = ExplainDutyStatusCalculationRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.details.notify.email
                        this.name = request.details.notify.name
                    }
                }
                this.dutyStatus = dutyStatus {
                    this.subject = driverLicenseRef {
                        this.displayName = reply0.stop.organizer.displayName
                    }
                }
            }
            val reply1 = driversDutyStatusCalculationService.explainDutyStatusCalculation(outbound1)
            val outbound2 = countUsersRequest {
                this.context = request.context
                this.filter = CountUsersRequestKt.filter {
                    this.region = reply0.stop.location.region
                }
            }
            val reply2 = identityUserQueryService.countUsers(outbound2)
            pauseRouteWorkflowResponse {
                this.outcome = PauseRouteWorkflowResponseKt.outcome {
                    this.nextSteps.addAll(reply1.outcome.nextStepsList)
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.referenceIds.addAll(reply2.result.referenceIdsList)
                    this.status = "ok"
                }
                this.route = route {
                    this.counterparty = request.route.counterparty
                    this.metadata.putAll(reply0.stop.metadataMap)
                    this.routeId = request.routeId
                }
            }
        }

    private fun RetryRouteWorkflowRequest.toCheckDocumentEligibilityFromRetryRouteWorkflow1(): CheckDocumentEligibilityRequest {
        val source = this
        return checkDocumentEligibilityRequest {
            this.context = source.context
            this.details = CheckDocumentEligibilityRequestKt.details {
                this.contact = contactInfo {
                    this.email = source.details.notify.email
                }
            }
            this.document = document {
                this.audit = auditInfo {
                    this.updatedBy = source.route.audit.updatedBy
                }
                this.bank = documentBankSettlement {
                    this.bic = source.route.bank.bic
                }
                this.counterparty = documentFolderRef {
                    this.displayName = source.route.counterparty.displayName
                }
            }
        }
    }

    private suspend fun handleSignalRouteWorkflow(request: SignalRouteWorkflowRequest): SignalRouteWorkflowResponse =
        RpcBoundary.run("acme.routing.v1.RouteWorkflowService/SignalRouteWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SignalRouteWorkflow", request)
            log.atInfo().addKeyValue("operation", "SignalRouteWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.route.routeId).log("Recorded audit detail")
            var reply0 = ExplainRouteCalculationResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.route.linesList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound0 = explainRouteCalculationRequest {
                                this.context = request.context
                                this.route = route {
                                    this.lines.addAll(listOf(routeLine {
                                        this.amount = item.tax.amount
                                        this.attributes.putAll(item.attributesMap)
                                        this.tax = routeLineTax {
                                            this.jurisdiction = item.tax.jurisdiction
                                        }
                                    }))
                                }
                            }
                            routingRouteCalculationService.explainRouteCalculation(outbound0)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply0 = it }
                }
            }
            val outbound1 = summarizeDriversRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.details = SummarizeDriversRequestKt.details {
                    this.filters.putAll(reply0.route.metadataMap)
                }
                this.driverId = request.route.counterparty.driverId
                this.reason = request.reason
            }
            val reply1 = driversDriverQueryService.summarizeDrivers(WireCopies.json(outbound1))
            signalRouteWorkflowResponse {
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = "ok"
                }
                this.route = route {
                    this.billingAddress = request.route.billingAddress
                    this.routeId = request.routeId
                }
            }
        }

    private data class StartRouteWorkflow0Command(
        val context: RequestContext,
        val driverId: String
    ) {
        fun toRequest(): GetDriverRulesRequest {
            val command = this
            return getDriverRulesRequest {
                this.context = command.context
                this.driverId = command.driverId
            }
        }
    }

    private suspend fun handleStartRouteWorkflow(request: StartRouteWorkflowRequest): StartRouteWorkflowResponse =
        RpcBoundary.run("acme.routing.v1.RouteWorkflowService/StartRouteWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StartRouteWorkflow", request)
            log.atInfo().addKeyValue("operation", "StartRouteWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.route.routeId).log("Recorded audit detail")
            val outbound0 = StartRouteWorkflow0Command(
                context = request.context,
                driverId = request.route.counterparty.driverId
            ).toRequest()
            val reply0 = driversDriverValidationService.getDriverRules(outbound0)
            startRouteWorkflowResponse {
                this.result = operationResult {
                    this.status = reply0.result.status
                }
                this.route = route {
                    this.audit = auditInfo {
                        this.createdAt = reply0.driver.audit.createdAt
                    }
                    this.counterparty = request.route.counterparty
                    this.routeId = request.routeId
                }
            }
        }
}
