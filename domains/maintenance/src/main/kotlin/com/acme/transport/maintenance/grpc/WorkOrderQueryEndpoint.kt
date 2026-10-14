package com.acme.transport.maintenance.grpc

import com.acme.authorization.v1.AccessPolicyQueryServiceGrpcKt
import com.acme.authorization.v1.ExportAccessPoliciesRequestKt
import com.acme.authorization.v1.exportAccessPoliciesRequest
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.drivers.v1.driverRef
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.BrowseWorkOrdersRequest
import com.acme.maintenance.v1.BrowseWorkOrdersResponse
import com.acme.maintenance.v1.EstimateWorkOrderRequestKt
import com.acme.maintenance.v1.ExplainPartDecisionRequest
import com.acme.maintenance.v1.ExplainPartDecisionRequestKt
import com.acme.maintenance.v1.ExplainServiceScheduleCalculationRequest
import com.acme.maintenance.v1.ExportWorkOrdersRequest
import com.acme.maintenance.v1.ExportWorkOrdersResponse
import com.acme.maintenance.v1.FindWorkOrdersRequest
import com.acme.maintenance.v1.FindWorkOrdersResponse
import com.acme.maintenance.v1.LookupWorkOrderRequest
import com.acme.maintenance.v1.LookupWorkOrderResponse
import com.acme.maintenance.v1.PartCalculationServiceGrpcKt
import com.acme.maintenance.v1.PartValidationServiceGrpcKt
import com.acme.maintenance.v1.ResolveWorkOrdersRequest
import com.acme.maintenance.v1.ResolveWorkOrdersResponse
import com.acme.maintenance.v1.ServiceScheduleCalculationServiceGrpcKt
import com.acme.maintenance.v1.SummarizeWorkOrdersRequest
import com.acme.maintenance.v1.SummarizeWorkOrdersResponse
import com.acme.maintenance.v1.ValidatePartRequestKt
import com.acme.maintenance.v1.ValidateWorkOrderRequestKt
import com.acme.maintenance.v1.WorkOrderCalculationServiceGrpcKt
import com.acme.maintenance.v1.WorkOrderQueryServiceGrpcKt
import com.acme.maintenance.v1.WorkOrderValidationServiceGrpcKt
import com.acme.maintenance.v1.browseWorkOrdersResponse
import com.acme.maintenance.v1.copy as maintenanceCopy
import com.acme.maintenance.v1.estimateWorkOrderRequest
import com.acme.maintenance.v1.explainPartDecisionRequest
import com.acme.maintenance.v1.explainServiceScheduleCalculationRequest
import com.acme.maintenance.v1.exportWorkOrdersResponse
import com.acme.maintenance.v1.findWorkOrdersResponse
import com.acme.maintenance.v1.lookupWorkOrderResponse
import com.acme.maintenance.v1.part
import com.acme.maintenance.v1.resolveWorkOrdersResponse
import com.acme.maintenance.v1.serviceSchedule
import com.acme.maintenance.v1.serviceScheduleParticipant
import com.acme.maintenance.v1.simulatePartRequest
import com.acme.maintenance.v1.summarizeWorkOrdersResponse
import com.acme.maintenance.v1.validatePartRequest
import com.acme.maintenance.v1.validateWorkOrderRequest
import com.acme.maintenance.v1.workOrder
import com.acme.shows.v1.ShowServiceGrpcKt
import com.acme.shows.v1.batchGetShowsRequest
import com.acme.transport.maintenance.MaintenanceRepository
import com.acme.transport.platform.RpcBoundary
import com.google.protobuf.Any as ProtoAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Answers read-only questions about work orders, for screens and reports. */
class WorkOrderQueryEndpoint(
    private val store: MaintenanceRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyQueryService: AccessPolicyQueryServiceGrpcKt.AccessPolicyQueryServiceCoroutineStub,
    private val maintenancePartCalculationService: PartCalculationServiceGrpcKt.PartCalculationServiceCoroutineStub,
    private val maintenancePartValidationService: PartValidationServiceGrpcKt.PartValidationServiceCoroutineStub,
    private val maintenanceServiceScheduleCalculationService: ServiceScheduleCalculationServiceGrpcKt.ServiceScheduleCalculationServiceCoroutineStub,
    private val maintenanceWorkOrderCalculationService: WorkOrderCalculationServiceGrpcKt.WorkOrderCalculationServiceCoroutineStub,
    private val maintenanceWorkOrderValidationService: WorkOrderValidationServiceGrpcKt.WorkOrderValidationServiceCoroutineStub,
    private val showsShowService: ShowServiceGrpcKt.ShowServiceCoroutineStub,
) : WorkOrderQueryServiceGrpcKt.WorkOrderQueryServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(WorkOrderQueryEndpoint::class.java)

    /** Lists work orders a page at a time. */
    override suspend fun browseWorkOrders(request: BrowseWorkOrdersRequest): BrowseWorkOrdersResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderQueryService/BrowseWorkOrders") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BrowseWorkOrders", request)
            log.atInfo().addKeyValue("operation", "BrowseWorkOrders").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            browseWorkOrdersResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Streams every matching work order. */
    override fun exportWorkOrders(request: ExportWorkOrdersRequest): Flow<ExportWorkOrdersResponse> = flow {
        emit(handleExportWorkOrders(request))
    }

    /** Lists work orders a page at a time. */
    override suspend fun findWorkOrders(request: FindWorkOrdersRequest): FindWorkOrdersResponse = handleFindWorkOrders(request)

    /** Returns a work order. */
    override suspend fun lookupWorkOrder(request: LookupWorkOrderRequest): LookupWorkOrderResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderQueryService/LookupWorkOrder") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LookupWorkOrder", request)
            log.atInfo().addKeyValue("operation", "LookupWorkOrder").log("Handling transport request")
            val outbound0 = batchGetShowsRequest {
                this.context = request.context
            }
            val reply0 = showsShowService.batchGetShows(outbound0)
            val outbound1 = validateWorkOrderRequest {
                this.context = request.context
                this.details = ValidateWorkOrderRequestKt.details {
                    this.locale = request.context.locale
                }
                this.workOrder = workOrder {
                    this.assignee = userRef {
                        this.userId = request.context.actor.userId
                    }
                    this.customer = driverRef {
                        this.email = request.context.actor.email
                    }
                    this.workOrderId = request.workOrderId
                }
            }
            val reply1 = maintenanceWorkOrderValidationService.validateWorkOrder(outbound1)
            lookupWorkOrderResponse {
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
                this.workOrder = workOrder {
                    this.requester = contactInfo {
                        this.name = reply1.workOrder.requester.name
                    }
                    this.workOrderId = request.workOrderId
                }
            }
        }

    /** Returns several work orders at once. */
    override suspend fun resolveWorkOrders(request: ResolveWorkOrdersRequest): ResolveWorkOrdersResponse {
        return handleResolveWorkOrders(request)
    }

    /** Summarizes a work order. */
    override suspend fun summarizeWorkOrders(request: SummarizeWorkOrdersRequest): SummarizeWorkOrdersResponse = handleSummarizeWorkOrders(request)

    private suspend fun handleExportWorkOrders(request: ExportWorkOrdersRequest): ExportWorkOrdersResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderQueryService/ExportWorkOrders") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExportWorkOrders", request)
            log.atInfo().addKeyValue("operation", "ExportWorkOrders").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = estimateWorkOrderRequest {
                this.context = request.context
                this.details = EstimateWorkOrderRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.filter.region
                    }
                }
                this.workOrder = workOrder {
                    this.assignee = userRef {
                        this.userId = request.context.actor.userId
                    }
                    this.requester = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = withTimeout(5_000) { maintenanceWorkOrderCalculationService.estimateWorkOrder(outbound0) }
            val configure1: ValidatePartRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.details = ValidatePartRequestKt.details {
                    this.contact = contactInfo {
                        this.preferredChannel = reply0.workOrder.requester.preferredChannel
                    }
                }
                this.part = part {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.workOrder.audit.updatedBy
                    }
                    this.owner = userRef {
                        this.displayName = reply0.workOrder.customer.displayName
                        this.email = reply0.workOrder.requester.email
                    }
                }
            }
            val outbound1 = validatePartRequest(configure1)
            val reply1 = maintenancePartValidationService.validatePart(outbound1)
            exportWorkOrdersResponse {
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.status = "ok"
                }
                this.workOrder = workOrder {
                    this.priority = reply0.workOrder.priority
                }
            }
        }

    private suspend fun handleFindWorkOrders(request: FindWorkOrdersRequest): FindWorkOrdersResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderQueryService/FindWorkOrders") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "FindWorkOrders", request)
            log.atInfo().addKeyValue("operation", "FindWorkOrders").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            findWorkOrdersResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleResolveWorkOrders(request: ResolveWorkOrdersRequest): ResolveWorkOrdersResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderQueryService/ResolveWorkOrders") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResolveWorkOrders", request)
            log.atInfo().addKeyValue("operation", "ResolveWorkOrders").log("Handling transport request")
            val outbound0 = simulatePartRequest {}.maintenanceCopy {
                this.context = request.context
                this.part = part {
                    this.audit = auditInfo {
                        this.createdAt = request.context.deadline
                    }
                    this.owner = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply0 = maintenancePartCalculationService.simulatePart(outbound0)
            resolveWorkOrdersResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.workOrders.addAll(listOf(workOrder {
                    this.audit = auditInfo {
                        this.createdAt = reply0.part.audit.createdAt
                    }
                }))
            }
        }

    private fun SummarizeWorkOrdersRequest.toExplainPartDecisionFromSummarizeWorkOrders0(): ExplainPartDecisionRequest {
        val source = this
        return explainPartDecisionRequest {
            this.annotations.putAll(source.annotationsMap)
            this.context = source.context
            this.details = ExplainPartDecisionRequestKt.details {
                this.contact = contactInfo {
                    this.email = source.context.actor.email
                }
            }
            this.reason = source.reason
        }
    }

    private suspend fun handleSummarizeWorkOrders(request: SummarizeWorkOrdersRequest): SummarizeWorkOrdersResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderQueryService/SummarizeWorkOrders") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SummarizeWorkOrders", request)
            log.atInfo().addKeyValue("operation", "SummarizeWorkOrders").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.workOrderId).log("Recorded audit detail")
            val outbound0 = request.toExplainPartDecisionFromSummarizeWorkOrders0()
            val reply0 = maintenancePartValidationService.explainPartDecision(outbound0)
            val outbound1 = exportAccessPoliciesRequest {
                this.context = request.context
                this.filter = ExportAccessPoliciesRequestKt.filter {
                    this.region = reply0.part.address.region
                }
                this.format = request.details.format
            }
            authorizationAccessPolicyQueryService.exportAccessPolicies(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound2 = explainServiceScheduleCalculationRequest {
                this.context = request.context
                this.serviceSchedule = serviceSchedule {
                    this.audit = auditInfo {
                        this.createdAt = reply0.part.audit.createdAt
                        this.updatedAt = reply0.part.audit.updatedAt
                    }
                    this.participants.addAll(listOf(serviceScheduleParticipant {
                        this.contact = contactInfo {
                            this.email = reply0.part.owner.email
                        }
                    }))
                }
            }
            val envelope2 = ProtoAny.pack(outbound2)
            val reply2 = maintenanceServiceScheduleCalculationService.explainServiceScheduleCalculation(envelope2.unpack(ExplainServiceScheduleCalculationRequest::class.java))
            summarizeWorkOrdersResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.referenceIds.addAll(reply2.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }
}
