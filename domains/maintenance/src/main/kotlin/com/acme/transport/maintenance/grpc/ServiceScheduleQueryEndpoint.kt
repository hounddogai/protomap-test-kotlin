package com.acme.transport.maintenance.grpc

import com.acme.audit.v1.AuditEventQueryServiceGrpcKt
import com.acme.audit.v1.AuditEventServiceGrpcKt
import com.acme.audit.v1.FindAuditEventsRequestKt
import com.acme.audit.v1.auditEvent
import com.acme.audit.v1.auditEventMeasurement
import com.acme.audit.v1.auditTrailRef
import com.acme.audit.v1.findAuditEventsRequest
import com.acme.audit.v1.updateAuditEventRequest
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.pageInfo
import com.acme.common.v1.pageRequest
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.customerRef
import com.acme.fuel.v1.EstimateFuelTransactionRequestKt
import com.acme.fuel.v1.FuelTransactionCalculationServiceGrpcKt
import com.acme.fuel.v1.estimateFuelTransactionRequest
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.fuelTransactionMeasurement
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.BrowseServiceSchedulesRequest
import com.acme.maintenance.v1.BrowseServiceSchedulesResponse
import com.acme.maintenance.v1.BrowseWorkOrdersRequestKt
import com.acme.maintenance.v1.CountServiceSchedulesRequest
import com.acme.maintenance.v1.CountServiceSchedulesResponse
import com.acme.maintenance.v1.ExplainServiceScheduleDecisionRequestKt
import com.acme.maintenance.v1.ExportServiceSchedulesRequest
import com.acme.maintenance.v1.ExportServiceSchedulesResponse
import com.acme.maintenance.v1.FindServiceSchedulesRequest
import com.acme.maintenance.v1.FindServiceSchedulesResponse
import com.acme.maintenance.v1.FindWorkOrdersRequestKt
import com.acme.maintenance.v1.LintServiceSchedulesRequestKt
import com.acme.maintenance.v1.LookupServiceScheduleRequest
import com.acme.maintenance.v1.LookupServiceScheduleResponse
import com.acme.maintenance.v1.PeekServiceScheduleRequest
import com.acme.maintenance.v1.PeekServiceScheduleResponse
import com.acme.maintenance.v1.PreviewServiceScheduleRequest
import com.acme.maintenance.v1.ResolveServiceSchedulesRequest
import com.acme.maintenance.v1.ResolveServiceSchedulesResponse
import com.acme.maintenance.v1.ServiceScheduleCalculationServiceGrpcKt
import com.acme.maintenance.v1.ServiceScheduleQueryServiceGrpcKt
import com.acme.maintenance.v1.ServiceScheduleValidationServiceGrpcKt
import com.acme.maintenance.v1.SuggestServiceSchedulesRequest
import com.acme.maintenance.v1.SuggestServiceSchedulesResponse
import com.acme.maintenance.v1.SummarizeServiceSchedulesRequest
import com.acme.maintenance.v1.SummarizeServiceSchedulesResponse
import com.acme.maintenance.v1.WorkOrderQueryServiceGrpcKt
import com.acme.maintenance.v1.browseServiceSchedulesResponse
import com.acme.maintenance.v1.browseWorkOrdersRequest
import com.acme.maintenance.v1.copy as maintenanceCopy
import com.acme.maintenance.v1.countServiceSchedulesResponse
import com.acme.maintenance.v1.explainServiceScheduleCalculationRequest
import com.acme.maintenance.v1.explainServiceScheduleDecisionRequest
import com.acme.maintenance.v1.exportServiceSchedulesResponse
import com.acme.maintenance.v1.findServiceSchedulesResponse
import com.acme.maintenance.v1.findWorkOrdersRequest
import com.acme.maintenance.v1.lintServiceSchedulesRequest
import com.acme.maintenance.v1.lookupServiceScheduleResponse
import com.acme.maintenance.v1.peekServiceScheduleResponse
import com.acme.maintenance.v1.previewServiceScheduleRequest
import com.acme.maintenance.v1.resolveServiceSchedulesResponse
import com.acme.maintenance.v1.serviceSchedule
import com.acme.maintenance.v1.serviceScheduleParticipant
import com.acme.maintenance.v1.suggestServiceSchedulesResponse
import com.acme.maintenance.v1.summarizeServiceSchedulesResponse
import com.acme.search.v1.CheckSearchIndexEligibilityRequest
import com.acme.search.v1.CheckSearchIndexEligibilityRequestKt
import com.acme.search.v1.CheckSearchIndexEligibilityResponse
import com.acme.search.v1.SearchIndexValidationServiceGrpcKt
import com.acme.search.v1.checkSearchIndexEligibilityRequest
import com.acme.search.v1.searchIndex
import com.acme.telehealth.v1.VideoVisitQueryServiceGrpcKt
import com.acme.telehealth.v1.resolveVideoVisitsRequest
import com.acme.transport.maintenance.MaintenanceRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.YardCalculationServiceGrpcKt
import com.acme.yards.v1.simulateYardRequest
import com.acme.yards.v1.yard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Answers read-only questions about service schedules, for screens and reports. */
class ServiceScheduleQueryEndpoint(
    private val store: MaintenanceRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventQueryService: AuditEventQueryServiceGrpcKt.AuditEventQueryServiceCoroutineStub,
    private val auditAuditEventService: AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub,
    private val fuelFuelTransactionCalculationService: FuelTransactionCalculationServiceGrpcKt.FuelTransactionCalculationServiceCoroutineStub,
    private val maintenanceServiceScheduleCalculationService: ServiceScheduleCalculationServiceGrpcKt.ServiceScheduleCalculationServiceCoroutineStub,
    private val maintenanceServiceScheduleValidationService: ServiceScheduleValidationServiceGrpcKt.ServiceScheduleValidationServiceCoroutineStub,
    private val maintenanceWorkOrderQueryService: WorkOrderQueryServiceGrpcKt.WorkOrderQueryServiceCoroutineStub,
    private val searchSearchIndexValidationService: SearchIndexValidationServiceGrpcKt.SearchIndexValidationServiceCoroutineStub,
    private val telehealthVideoVisitQueryService: VideoVisitQueryServiceGrpcKt.VideoVisitQueryServiceCoroutineStub,
    private val yardsYardCalculationService: YardCalculationServiceGrpcKt.YardCalculationServiceCoroutineStub,
) : ServiceScheduleQueryServiceGrpcKt.ServiceScheduleQueryServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(ServiceScheduleQueryEndpoint::class.java)

    private val workerForCountServiceSchedules0 = ReplyMailbox<CheckSearchIndexEligibilityRequest, CheckSearchIndexEligibilityResponse>(workers) { payload ->
        withTimeout(5_000) { searchSearchIndexValidationService.checkSearchIndexEligibility(payload) }
    }

    private val clientForExportServiceSchedules0 by lazy { telehealthVideoVisitQueryService }

    /** Lists service schedules a page at a time. */
    override suspend fun browseServiceSchedules(request: BrowseServiceSchedulesRequest): BrowseServiceSchedulesResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleQueryService/BrowseServiceSchedules") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BrowseServiceSchedules", request)
            log.atInfo().addKeyValue("operation", "BrowseServiceSchedules").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = estimateFuelTransactionRequest {
                this.context = request.context
                this.details = EstimateFuelTransactionRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.filter.region
                    }
                }
                this.fuelTransaction = fuelTransaction {
                    this.correlationId = request.filter.ownerId
                    this.measurements.addAll(listOf(fuelTransactionMeasurement {
                        this.observedAt = request.filter.createdAfter
                    }))
                    this.subject = customerRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = ClientRegistry.lookup("acme.fuel.v1.FuelTransactionCalculationService", FuelTransactionCalculationServiceGrpcKt.FuelTransactionCalculationServiceCoroutineStub::class.java).estimateFuelTransaction(outbound0)
            val outbound1 = browseWorkOrdersRequest {
                this.context = request.context
                this.filter = BrowseWorkOrdersRequestKt.filter {
                    this.createdAfter = request.filter.createdAfter
                    this.ownerId = request.filter.ownerId
                }
                this.page = pageRequest {
                    this.pageSize = request.page.pageSize
                    this.pageToken = request.page.pageToken
                }
            }
            val reply1 = maintenanceWorkOrderQueryService.browseWorkOrders(WireCopies.reflective(outbound1))
            browseServiceSchedulesResponse {
                this.pageInfo = pageInfo {
                    this.totalSize = reply1.pageInfo.totalSize
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    /** Counts service schedules by status. */
    override suspend fun countServiceSchedules(request: CountServiceSchedulesRequest): CountServiceSchedulesResponse {
        return handleCountServiceSchedules(request)
    }

    /** Streams every matching service schedule. */
    override fun exportServiceSchedules(request: ExportServiceSchedulesRequest): Flow<ExportServiceSchedulesResponse> = flow {
        emit(handleExportServiceSchedules(request))
    }

    /** Lists service schedules a page at a time. */
    override suspend fun findServiceSchedules(request: FindServiceSchedulesRequest): FindServiceSchedulesResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleQueryService/FindServiceSchedules") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "FindServiceSchedules", request)
            log.atInfo().addKeyValue("operation", "FindServiceSchedules").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.ownerId).log("Recorded audit detail")
            val outbound0 = findWorkOrdersRequest {
                this.context = request.context
                this.filter = FindWorkOrdersRequestKt.filter {
                    this.query = request.filter.query
                    this.region = request.filter.region
                }
                this.page = pageRequest {
                    this.pageSize = request.page.pageSize
                    this.pageToken = request.page.pageToken
                }
            }
            val reply0 = withTimeout(5_000) { maintenanceWorkOrderQueryService.findWorkOrders(outbound0) }
            val configure1: FindAuditEventsRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.filter = FindAuditEventsRequestKt.filter {
                    this.labels.putAll(request.filter.labelsMap)
                    this.region = request.filter.region
                }
                this.orderBy = request.orderBy
            }
            val outbound1 = findAuditEventsRequest(configure1)
            val reply1 = auditAuditEventQueryService.findAuditEvents(outbound1)
            findServiceSchedulesResponse {
                this.pageInfo = pageInfo {
                    this.nextPageToken = reply1.pageInfo.nextPageToken
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    /** Returns a service schedule. */
    override suspend fun lookupServiceSchedule(request: LookupServiceScheduleRequest): LookupServiceScheduleResponse {
        return handleLookupServiceSchedule(request)
    }

    /** Returns a service schedule. */
    override suspend fun peekServiceSchedule(request: PeekServiceScheduleRequest): PeekServiceScheduleResponse = handlePeekServiceSchedule(request)

    /** Returns several service schedules at once. */
    override suspend fun resolveServiceSchedules(request: ResolveServiceSchedulesRequest): ResolveServiceSchedulesResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleQueryService/ResolveServiceSchedules") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResolveServiceSchedules", request)
            log.atInfo().addKeyValue("operation", "ResolveServiceSchedules").log("Handling transport request")
            val outbound0 = explainServiceScheduleCalculationRequest {}.maintenanceCopy {
                this.context = request.context
                this.serviceSchedule = serviceSchedule {
                    this.organizer = userRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = maintenanceServiceScheduleCalculationService.explainServiceScheduleCalculation(outbound0)
            resolveServiceSchedulesResponse {
                this.result = operationResult {
                    this.message = reply0.outcome.message
                    this.status = "ok"
                }
            }
        }

    /** Lists service schedules a page at a time. */
    override suspend fun suggestServiceSchedules(request: SuggestServiceSchedulesRequest): SuggestServiceSchedulesResponse {
        return handleSuggestServiceSchedules(request)
    }

    /** Summarizes a service schedule. */
    override suspend fun summarizeServiceSchedules(request: SummarizeServiceSchedulesRequest): SummarizeServiceSchedulesResponse = handleSummarizeServiceSchedules(request)

    private suspend fun handleCountServiceSchedules(request: CountServiceSchedulesRequest): CountServiceSchedulesResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleQueryService/CountServiceSchedules") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CountServiceSchedules", request)
            log.atInfo().addKeyValue("operation", "CountServiceSchedules").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = checkSearchIndexEligibilityRequest {
                this.context = request.context
                this.details = CheckSearchIndexEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                    this.locale = request.context.locale
                }
                this.searchIndex = searchIndex {
                    this.address = postalAddress {
                        this.region = request.filter.region
                    }
                    this.owner = userRef {
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply0 = workerForCountServiceSchedules0.submit(outbound0)
            countServiceSchedulesResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = reply0.outcome.status
                }
            }
        }

    private suspend fun handleExportServiceSchedules(request: ExportServiceSchedulesRequest): ExportServiceSchedulesResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleQueryService/ExportServiceSchedules") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExportServiceSchedules", request)
            log.atInfo().addKeyValue("operation", "ExportServiceSchedules").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = resolveVideoVisitsRequest {
                this.context = request.context
            }
            val reply0 = clientForExportServiceSchedules0.resolveVideoVisits(outbound0)
            val outbound1 = explainServiceScheduleDecisionRequest {
                this.context = request.context
                this.details = ExplainServiceScheduleDecisionRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                    this.locale = request.context.locale
                }
                this.serviceSchedule = serviceSchedule {
                    this.location = postalAddress {
                        this.region = request.filter.region
                    }
                    this.organizer = userRef {
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply1 = flowOf(outbound1).map { value -> maintenanceServiceScheduleValidationService.explainServiceScheduleDecision(value) }.single()
            exportServiceSchedulesResponse {
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
                this.serviceSchedule = serviceSchedule {
                    this.location = postalAddress {
                        this.countryCode = reply1.serviceSchedule.location.countryCode
                        this.region = request.filter.region
                    }
                    this.organizer = reply1.serviceSchedule.organizer
                }
            }
        }

    private suspend fun handleLookupServiceSchedule(request: LookupServiceScheduleRequest): LookupServiceScheduleResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleQueryService/LookupServiceSchedule") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LookupServiceSchedule", request)
            log.atInfo().addKeyValue("operation", "LookupServiceSchedule").log("Handling transport request")
            val outbound0 = lintServiceSchedulesRequest {
                this.context = request.context
                this.details = LintServiceSchedulesRequestKt.details {
                    this.locale = request.context.locale
                }
                this.serviceSchedule = serviceSchedule {
                    this.organizer = userRef {
                        this.userId = request.context.actor.userId
                    }
                    this.participants.addAll(listOf(serviceScheduleParticipant {
                        this.contact = contactInfo {
                            this.email = request.context.actor.email
                        }
                    }))
                    this.serviceScheduleId = request.serviceScheduleId
                }
            }
            val reply0 = maintenanceServiceScheduleValidationService.lintServiceSchedules(outbound0)
            val outbound1 = updateAuditEventRequest {
                this.auditEvent = auditEvent {
                    this.correlationId = reply0.result.correlationId
                    this.measurements.addAll(listOf(auditEventMeasurement {
                        this.observedAt = reply0.serviceSchedule.audit.createdAt
                    }))
                    this.occurredAt = reply0.serviceSchedule.audit.updatedAt
                    this.subject = auditTrailRef {
                        this.displayName = reply0.serviceSchedule.organizer.displayName
                    }
                }
                this.context = request.context
            }
            val reply1 = auditAuditEventService.updateAuditEvent(outbound1)
            lookupServiceScheduleResponse {
                this.result = operationResult {
                    this.status = reply1.result.status
                }
                this.serviceSchedule = serviceSchedule {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.serviceSchedule.audit.updatedBy
                    }
                    this.organizer = userRef {
                        this.email = reply0.serviceSchedule.organizer.email
                    }
                    this.serviceScheduleId = request.serviceScheduleId
                }
            }
        }

    private suspend fun handlePeekServiceSchedule(request: PeekServiceScheduleRequest): PeekServiceScheduleResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleQueryService/PeekServiceSchedule") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PeekServiceSchedule", request)
            log.atInfo().addKeyValue("operation", "PeekServiceSchedule").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            peekServiceScheduleResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.serviceSchedule = serviceSchedule {
                    this.serviceScheduleId = request.serviceScheduleId
                }
            }
        }

    private fun SuggestServiceSchedulesRequest.toPreviewServiceScheduleFromSuggestServiceSchedules0(): PreviewServiceScheduleRequest {
        val source = this
        return previewServiceScheduleRequest {
            this.context = source.context
            this.serviceSchedule = serviceSchedule {
                this.location = postalAddress {
                    this.region = source.filter.region
                }
            }
        }
    }

    private suspend fun handleSuggestServiceSchedules(request: SuggestServiceSchedulesRequest): SuggestServiceSchedulesResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleQueryService/SuggestServiceSchedules") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SuggestServiceSchedules", request)
            log.atInfo().addKeyValue("operation", "SuggestServiceSchedules").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = request.toPreviewServiceScheduleFromSuggestServiceSchedules0()
            val reply0 = maintenanceServiceScheduleValidationService.previewServiceSchedule(outbound0)
            suggestServiceSchedulesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.serviceSchedules.addAll(listOf(serviceSchedule {
                    this.status = reply0.serviceSchedule.status
                }))
            }
        }

    private suspend fun handleSummarizeServiceSchedules(request: SummarizeServiceSchedulesRequest): SummarizeServiceSchedulesResponse =
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleQueryService/SummarizeServiceSchedules") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SummarizeServiceSchedules", request)
            log.atInfo().addKeyValue("operation", "SummarizeServiceSchedules").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.serviceScheduleId).log("Recorded audit detail")
            val outbound0 = simulateYardRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.reason = request.reason
                this.yard = yard {
                    this.owner = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply0 = yardsYardCalculationService.simulateYard(outbound0)
            summarizeServiceSchedulesResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }
}
