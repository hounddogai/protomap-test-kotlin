package com.acme.transport.drivers.adapters

import com.acme.audit.v1.AuditEventQueryServiceGrpcKt
import com.acme.audit.v1.SuggestAuditEventsRequest
import com.acme.audit.v1.SuggestAuditEventsRequestKt
import com.acme.audit.v1.suggestAuditEventsRequest
import com.acme.authorization.v1.AccessPolicyQueryServiceGrpcKt
import com.acme.authorization.v1.AccessPolicyServiceGrpcKt
import com.acme.authorization.v1.BrowseAccessPoliciesRequestKt
import com.acme.authorization.v1.accessPolicy
import com.acme.authorization.v1.browseAccessPoliciesRequest
import com.acme.authorization.v1.copy as authorizationCopy
import com.acme.authorization.v1.createAccessPolicyRequest
import com.acme.careplans.v1.CarePlanQueryServiceGrpcKt
import com.acme.careplans.v1.ExportCarePlansRequestKt
import com.acme.careplans.v1.exportCarePlansRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.corporateevents.v1.CorporateEventCalculationServiceGrpcKt
import com.acme.corporateevents.v1.SimulateCorporateEventRequestKt
import com.acme.corporateevents.v1.corporateEvent
import com.acme.corporateevents.v1.corporateEventParticipant
import com.acme.corporateevents.v1.simulateCorporateEventRequest
import com.acme.documents.v1.BrowseDocumentsRequestKt
import com.acme.documents.v1.BrowseDocumentsResponse
import com.acme.documents.v1.DocumentQueryServiceGrpcKt
import com.acme.documents.v1.browseDocumentsRequest
import com.acme.drivers.v1.ApproveDutyStatusRequest
import com.acme.drivers.v1.ApproveDutyStatusResponse
import com.acme.drivers.v1.ApproveDutyStatusResponseKt
import com.acme.drivers.v1.CommentDutyStatusApprovalRequest
import com.acme.drivers.v1.CommentDutyStatusApprovalResponse
import com.acme.drivers.v1.CommentDutyStatusApprovalResponseKt
import com.acme.drivers.v1.DelegateDutyStatusApprovalRequest
import com.acme.drivers.v1.DelegateDutyStatusApprovalResponse
import com.acme.drivers.v1.DelegateDutyStatusApprovalResponseKt
import com.acme.drivers.v1.DeleteDutyStatusReportRequest
import com.acme.drivers.v1.DriverApprovalServiceGrpcKt
import com.acme.drivers.v1.DutyStatusApprovalServiceGrpcKt
import com.acme.drivers.v1.DutyStatusCalculationServiceGrpcKt
import com.acme.drivers.v1.DutyStatusLifecycleServiceGrpcKt
import com.acme.drivers.v1.DutyStatusMeasurement
import com.acme.drivers.v1.DutyStatusReportingServiceGrpcKt
import com.acme.drivers.v1.EscalateDriverRequestKt
import com.acme.drivers.v1.ListPendingDutyStatusesRequest
import com.acme.drivers.v1.ListPendingDutyStatusesResponse
import com.acme.drivers.v1.RecalculateDutyStatusesResponse
import com.acme.drivers.v1.RecallDutyStatusRequest
import com.acme.drivers.v1.RecallDutyStatusResponse
import com.acme.drivers.v1.RejectDutyStatusRequest
import com.acme.drivers.v1.RejectDutyStatusResponse
import com.acme.drivers.v1.SubmitDutyStatusRequest
import com.acme.drivers.v1.SubmitDutyStatusResponse
import com.acme.drivers.v1.SuspendDutyStatusRequestKt
import com.acme.drivers.v1.approveDutyStatusResponse
import com.acme.drivers.v1.commentDutyStatusApprovalResponse
import com.acme.drivers.v1.delegateDutyStatusApprovalResponse
import com.acme.drivers.v1.deleteDutyStatusReportRequest
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.driverNationalId
import com.acme.drivers.v1.dutyStatus
import com.acme.drivers.v1.dutyStatusMeasurement
import com.acme.drivers.v1.escalateDriverRequest
import com.acme.drivers.v1.listPendingDutyStatusesResponse
import com.acme.drivers.v1.recalculateDutyStatusesRequest
import com.acme.drivers.v1.recallDutyStatusResponse
import com.acme.drivers.v1.rejectDutyStatusResponse
import com.acme.drivers.v1.submitDutyStatusResponse
import com.acme.drivers.v1.suspendDutyStatusRequest
import com.acme.fuel.v1.CommentFuelTransactionApprovalRequest
import com.acme.fuel.v1.CommentFuelTransactionApprovalRequestKt
import com.acme.fuel.v1.FuelTransactionApprovalServiceGrpcKt
import com.acme.fuel.v1.commentFuelTransactionApprovalRequest
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.fuelTransactionMeasurement
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.LedgerAccountServiceGrpcKt
import com.acme.ledger.v1.WatchLedgerAccountsRequestKt
import com.acme.ledger.v1.watchLedgerAccountsRequest
import com.acme.search.v1.CountSearchIndexesRequestKt
import com.acme.search.v1.SearchIndexQueryServiceGrpcKt
import com.acme.search.v1.countSearchIndexesRequest
import com.acme.transport.drivers.DriversRepository
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.DockApprovalServiceGrpcKt
import com.acme.yards.v1.dock
import com.acme.yards.v1.recallDockRequest
import com.google.protobuf.Any as ProtoAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Routes duty statuses through approval, with escalation and delegation. */
class DutyStatusApprovalController(
    private val store: DriversRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventQueryService: AuditEventQueryServiceGrpcKt.AuditEventQueryServiceCoroutineStub,
    private val authorizationAccessPolicyQueryService: AccessPolicyQueryServiceGrpcKt.AccessPolicyQueryServiceCoroutineStub,
    private val authorizationAccessPolicyService: AccessPolicyServiceGrpcKt.AccessPolicyServiceCoroutineStub,
    private val careplansCarePlanQueryService: CarePlanQueryServiceGrpcKt.CarePlanQueryServiceCoroutineStub,
    private val corporateeventsCorporateEventCalculationService: CorporateEventCalculationServiceGrpcKt.CorporateEventCalculationServiceCoroutineStub,
    private val documentsDocumentQueryService: DocumentQueryServiceGrpcKt.DocumentQueryServiceCoroutineStub,
    private val driversDriverApprovalService: DriverApprovalServiceGrpcKt.DriverApprovalServiceCoroutineStub,
    private val driversDutyStatusCalculationService: DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineStub,
    private val driversDutyStatusLifecycleService: DutyStatusLifecycleServiceGrpcKt.DutyStatusLifecycleServiceCoroutineStub,
    private val driversDutyStatusReportingService: DutyStatusReportingServiceGrpcKt.DutyStatusReportingServiceCoroutineStub,
    private val fuelFuelTransactionApprovalService: FuelTransactionApprovalServiceGrpcKt.FuelTransactionApprovalServiceCoroutineStub,
    private val ledgerLedgerAccountService: LedgerAccountServiceGrpcKt.LedgerAccountServiceCoroutineStub,
    private val searchSearchIndexQueryService: SearchIndexQueryServiceGrpcKt.SearchIndexQueryServiceCoroutineStub,
    private val yardsDockApprovalService: DockApprovalServiceGrpcKt.DockApprovalServiceCoroutineStub,
) : DutyStatusApprovalServiceGrpcKt.DutyStatusApprovalServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DutyStatusApprovalController::class.java)

    private val clientForApproveDutyStatus1 by lazy { yardsDockApprovalService }

    /** Approves a duty status. */
    override suspend fun approveDutyStatus(request: ApproveDutyStatusRequest): ApproveDutyStatusResponse = handleApproveDutyStatus(request)

    /** Comments a duty status. */
    override suspend fun commentDutyStatusApproval(request: CommentDutyStatusApprovalRequest): CommentDutyStatusApprovalResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusApprovalService/CommentDutyStatusApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CommentDutyStatusApproval", request)
            log.atInfo().addKeyValue("operation", "CommentDutyStatusApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dutyStatus.dutyStatusId).log("Recorded audit detail")
            val outbound0 = suspendDutyStatusRequest {
                this.context = request.context
                this.details = SuspendDutyStatusRequestKt.details {
                    this.notifyContact = contactInfo {
                        this.email = request.details.approver.email
                    }
                }
                this.dutyStatusId = request.dutyStatusId
            }
            val reply0 = flowOf(outbound0).map { value -> driversDutyStatusLifecycleService.suspendDutyStatus(value) }.single()
            val outbound1 = escalateDriverRequest {
                this.context = request.context
                this.details = EscalateDriverRequestKt.details {
                    this.approver = userRef {
                        this.displayName = reply0.dutyStatus.subject.displayName
                    }
                }
                this.driver = driver {
                    this.contact = contactInfo {
                        this.email = request.details.approver.email
                    }
                    this.nationalId = driverNationalId {
                        this.number = reply0.dutyStatus.subject.number
                    }
                    this.relationshipManager = userRef {
                        this.displayName = request.details.approver.displayName
                    }
                }
            }
            val reply1 = withTimeout(5_000) { driversDriverApprovalService.escalateDriver(outbound1) }
            val configure2: SimulateCorporateEventRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.corporateEvent = corporateEvent {
                    this.participants.addAll(listOf(corporateEventParticipant {
                        this.contact = contactInfo {
                            this.email = request.details.approver.email
                        }
                    }))
                }
            }
            val outbound2 = simulateCorporateEventRequest(configure2)
            val reply2 = corporateeventsCorporateEventCalculationService.simulateCorporateEvent(outbound2)
            commentDutyStatusApprovalResponse {
                this.dutyStatus = dutyStatus {
                    this.dutyStatusId = request.dutyStatusId
                }
                this.outcome = CommentDutyStatusApprovalResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.message = reply2.result.message
                    this.referenceId = reply1.outcome.referenceId
                    this.status = reply1.outcome.status
                }
                this.result = operationResult {
                    this.message = reply2.result.message
                    this.status = reply0.outcome.status
                }
            }
        }

    /** Delegates a duty status. */
    override suspend fun delegateDutyStatusApproval(request: DelegateDutyStatusApprovalRequest): DelegateDutyStatusApprovalResponse {
        return handleDelegateDutyStatusApproval(request)
    }

    /** Lists duty statuses a page at a time. */
    override suspend fun listPendingDutyStatuses(request: ListPendingDutyStatusesRequest): ListPendingDutyStatusesResponse = handleListPendingDutyStatuses(request)

    /** Recalls a duty status. */
    override suspend fun recallDutyStatus(request: RecallDutyStatusRequest): RecallDutyStatusResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusApprovalService/RecallDutyStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecallDutyStatus", request)
            log.atInfo().addKeyValue("operation", "RecallDutyStatus").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dutyStatus.dutyStatusId).log("Recorded audit detail")
            val outbound0 = watchLedgerAccountsRequest {
                this.context = request.context
                this.filter = WatchLedgerAccountsRequestKt.filter {
                    this.createdAfter = request.details.effectiveAt
                    this.labels.putAll(request.dutyStatus.attributesMap)
                    this.ownerId = request.dutyStatus.subject.driverLicenseId
                }
                this.subscriber = contactInfo {
                    this.email = request.details.approver.email
                }
            }
            ledgerLedgerAccountService.watchLedgerAccounts(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = suggestAuditEventsRequest {
                this.context = request.context
                this.filter = SuggestAuditEventsRequestKt.filter {
                    this.createdAfter = request.details.effectiveAt
                    this.labels.putAll(request.annotationsMap)
                    this.ownerId = request.dutyStatus.subject.driverLicenseId
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = auditAuditEventQueryService.suggestAuditEvents(envelope1.unpack(SuggestAuditEventsRequest::class.java))
            recallDutyStatusResponse {
                this.dutyStatus = dutyStatus {
                    this.dutyStatusId = request.dutyStatusId
                    this.kind = request.dutyStatus.kind
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    /** Rejects a duty status. */
    override suspend fun rejectDutyStatus(request: RejectDutyStatusRequest): RejectDutyStatusResponse {
        return handleRejectDutyStatus(request)
    }

    /** Submits a duty status. */
    override suspend fun submitDutyStatus(request: SubmitDutyStatusRequest): SubmitDutyStatusResponse = handleSubmitDutyStatus(request)

    private suspend fun handleApproveDutyStatus(request: ApproveDutyStatusRequest): ApproveDutyStatusResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusApprovalService/ApproveDutyStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ApproveDutyStatus", request)
            log.atInfo().addKeyValue("operation", "ApproveDutyStatus").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dutyStatus.dutyStatusId).log("Recorded audit detail")
            var reply0 = RecalculateDutyStatusesResponse.getDefaultInstance()
            request.dutyStatus.measurementsList.asFlow().map { item ->
                val outbound0 = recalculateDutyStatusesRequest {
                    this.context = request.context
                    this.dutyStatus = dutyStatus {
                        this.measurements.addAll(listOf(dutyStatusMeasurement {
                            this.metric = item.metric
                            this.observedAt = item.observedAt
                            this.unit = item.unit
                            this.value = item.value
                        }))
                    }
                }
                driversDutyStatusCalculationService.recalculateDutyStatuses(outbound0)
            }.collect { reply0 = it }
            val outbound1 = recallDockRequest {
                this.context = request.context
                this.dock = dock {
                    this.owner = userRef {
                        this.userId = request.details.approver.userId
                    }
                }
            }
            val reply1 = clientForApproveDutyStatus1.recallDock(outbound1)
            approveDutyStatusResponse {
                this.dutyStatus = dutyStatus {
                    this.dutyStatusId = request.dutyStatusId
                    this.source = request.dutyStatus.source
                }
                this.outcome = ApproveDutyStatusResponseKt.outcome {
                    this.message = reply1.result.message
                }
                this.result = operationResult {
                    this.status = reply1.outcome.status
                }
            }
        }

    private suspend fun handleDelegateDutyStatusApproval(request: DelegateDutyStatusApprovalRequest): DelegateDutyStatusApprovalResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusApprovalService/DelegateDutyStatusApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DelegateDutyStatusApproval", request)
            log.atInfo().addKeyValue("operation", "DelegateDutyStatusApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dutyStatus.dutyStatusId).log("Recorded audit detail")
            val outbound0 = countSearchIndexesRequest {
                this.context = request.context
                this.filter = CountSearchIndexesRequestKt.filter {
                    this.createdAfter = request.dutyStatus.occurredAt
                    this.labels.putAll(request.dutyStatus.attributesMap)
                    this.ownerId = request.dutyStatus.correlationId
                }
            }
            val reply0 = searchSearchIndexQueryService.countSearchIndexes(outbound0)
            val outbound1 = browseAccessPoliciesRequest {
                this.context = request.context
                this.filter = BrowseAccessPoliciesRequestKt.filter {
                    this.createdAfter = request.details.effectiveAt
                    this.labels.putAll(reply0.result.detailsMap)
                    this.ownerId = request.dutyStatus.subject.driverLicenseId
                }
            }
            val reply1 = authorizationAccessPolicyQueryService.browseAccessPolicies(outbound1)
            val outbound2 = createAccessPolicyRequest {}.authorizationCopy {
                this.accessPolicy = accessPolicy {
                    this.owner = userRef {
                        this.displayName = request.details.approver.displayName
                        this.email = request.details.approver.email
                    }
                }
                this.context = request.context
            }
            val reply2 = authorizationAccessPolicyService.createAccessPolicy(outbound2)
            delegateDutyStatusApprovalResponse {
                this.dutyStatus = dutyStatus {
                    this.correlationId = reply0.result.correlationId
                    this.dutyStatusId = request.dutyStatusId
                }
                this.outcome = DelegateDutyStatusApprovalResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private fun ListPendingDutyStatusesRequest.toCommentFuelTransactionApprovalFromListPendingDutyStatuses0(): CommentFuelTransactionApprovalRequest {
        val source = this
        return commentFuelTransactionApprovalRequest {
            this.context = source.context
            this.details = CommentFuelTransactionApprovalRequestKt.details {
                this.approver = userRef {
                    this.email = source.context.actor.email
                    this.userId = source.context.actor.userId
                }
            }
            this.fuelTransaction = fuelTransaction {
                this.measurements.addAll(listOf(fuelTransactionMeasurement {
                    this.observedAt = source.filter.createdAfter
                }))
                this.source = source.filter.ownerId
            }
        }
    }

    private suspend fun handleListPendingDutyStatuses(request: ListPendingDutyStatusesRequest): ListPendingDutyStatusesResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusApprovalService/ListPendingDutyStatuses") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListPendingDutyStatuses", request)
            log.atInfo().addKeyValue("operation", "ListPendingDutyStatuses").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = request.toCommentFuelTransactionApprovalFromListPendingDutyStatuses0()
            val reply0 = fuelFuelTransactionApprovalService.commentFuelTransactionApproval(outbound0)
            listPendingDutyStatusesResponse {
                this.dutyStatuses.addAll(listOf(dutyStatus {
                    this.attributes.putAll(reply0.fuelTransaction.attributesMap)
                    this.source = reply0.fuelTransaction.source
                }))
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private data class RejectDutyStatus1Command(
        val context: RequestContext,
        val dutyStatusId: String,
        val reason: String
    ) {
        fun toRequest(): DeleteDutyStatusReportRequest {
            val command = this
            return deleteDutyStatusReportRequest {
                this.context = command.context
                this.dutyStatusId = command.dutyStatusId
                this.reason = command.reason
            }
        }
    }

    private suspend fun handleRejectDutyStatus(request: RejectDutyStatusRequest): RejectDutyStatusResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusApprovalService/RejectDutyStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RejectDutyStatus", request)
            log.atInfo().addKeyValue("operation", "RejectDutyStatus").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dutyStatus.kind).log("Recorded audit detail")
            val outbound0 = exportCarePlansRequest {
                this.context = request.context
                this.filter = ExportCarePlansRequestKt.filter {
                    this.createdAfter = request.details.effectiveAt
                    this.labels.putAll(request.dutyStatus.attributesMap)
                }
            }
            careplansCarePlanQueryService.exportCarePlans(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = RejectDutyStatus1Command(
                context = request.context,
                dutyStatusId = request.dutyStatus.dutyStatusId,
                reason = request.reason
            ).toRequest()
            val reply1 = driversDutyStatusReportingService.deleteDutyStatusReport(outbound1)
            rejectDutyStatusResponse {
                this.dutyStatus = dutyStatus {
                    this.dutyStatusId = request.dutyStatusId
                }
                this.result = operationResult {
                    this.correlationId = request.dutyStatus.correlationId
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSubmitDutyStatus(request: SubmitDutyStatusRequest): SubmitDutyStatusResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusApprovalService/SubmitDutyStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SubmitDutyStatus", request)
            log.atInfo().addKeyValue("operation", "SubmitDutyStatus").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dutyStatus.dutyStatusId).log("Recorded audit detail")
            var reply0 = BrowseDocumentsResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<DutyStatusMeasurement>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound0 = browseDocumentsRequest {
                            this.context = request.context
                            this.filter = BrowseDocumentsRequestKt.filter {
                                this.createdAfter = item.observedAt
                                this.ownerId = request.details.approver.userId
                            }
                        }
                        reply0 = documentsDocumentQueryService.browseDocuments(outbound0)
                    }
                }
                try {
                    request.dutyStatus.measurementsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            submitDutyStatusResponse {
                this.dutyStatus = dutyStatus {
                    this.dutyStatusId = request.dutyStatusId
                }
                this.result = operationResult {
                    this.correlationId = request.dutyStatus.correlationId
                    this.status = "ok"
                }
            }
        }
}
