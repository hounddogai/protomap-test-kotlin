package com.acme.transport.maintenance.api

import com.acme.authorization.v1.AccessPolicyQueryServiceGrpcKt
import com.acme.authorization.v1.resolveAccessPoliciesRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.BrowseDeliveriesRequest
import com.acme.deliveries.v1.BrowseDeliveriesRequestKt
import com.acme.deliveries.v1.DeliveryQueryServiceGrpcKt
import com.acme.deliveries.v1.browseDeliveriesRequest
import com.acme.fuel.v1.FindFuelTransactionsRequest
import com.acme.fuel.v1.FindFuelTransactionsRequestKt
import com.acme.fuel.v1.FuelTransactionQueryServiceGrpcKt
import com.acme.fuel.v1.findFuelTransactionsRequest
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.LedgerAccountServiceGrpcKt
import com.acme.ledger.v1.SearchLedgerAccountsRequest
import com.acme.ledger.v1.SearchLedgerAccountsRequestKt
import com.acme.ledger.v1.SearchLedgerAccountsResponse
import com.acme.ledger.v1.searchLedgerAccountsRequest
import com.acme.maintenance.v1.BatchCreateWorkOrdersRequest
import com.acme.maintenance.v1.BatchCreateWorkOrdersResponse
import com.acme.maintenance.v1.BatchGetWorkOrdersRequest
import com.acme.maintenance.v1.BatchGetWorkOrdersResponse
import com.acme.maintenance.v1.BrowseWorkOrdersRequestKt
import com.acme.maintenance.v1.CalculateWorkOrderRequestKt
import com.acme.maintenance.v1.CreateWorkOrderRequest
import com.acme.maintenance.v1.CreateWorkOrderResponse
import com.acme.maintenance.v1.DeleteWorkOrderRequest
import com.acme.maintenance.v1.DeleteWorkOrderResponse
import com.acme.maintenance.v1.EstimateWorkOrderRequest
import com.acme.maintenance.v1.EstimateWorkOrderResponse
import com.acme.maintenance.v1.GetWorkOrderRequest
import com.acme.maintenance.v1.GetWorkOrderResponse
import com.acme.maintenance.v1.ListWorkOrdersRequest
import com.acme.maintenance.v1.ListWorkOrdersResponse
import com.acme.maintenance.v1.PartCalculationServiceGrpcKt
import com.acme.maintenance.v1.ServiceScheduleCalculationServiceGrpcKt
import com.acme.maintenance.v1.SimulatePartRequest
import com.acme.maintenance.v1.SimulatePartResponse
import com.acme.maintenance.v1.WatchWorkOrdersRequest
import com.acme.maintenance.v1.WatchWorkOrdersResponse
import com.acme.maintenance.v1.WorkOrderCalculationServiceGrpcKt
import com.acme.maintenance.v1.WorkOrderQueryServiceGrpcKt
import com.acme.maintenance.v1.WorkOrderServiceGrpcKt
import com.acme.maintenance.v1.batchCreateWorkOrdersResponse
import com.acme.maintenance.v1.batchGetWorkOrdersResponse
import com.acme.maintenance.v1.browseWorkOrdersRequest
import com.acme.maintenance.v1.calculateWorkOrderRequest
import com.acme.maintenance.v1.createWorkOrderResponse
import com.acme.maintenance.v1.deleteWorkOrderResponse
import com.acme.maintenance.v1.estimateWorkOrderRequest
import com.acme.maintenance.v1.getWorkOrderResponse
import com.acme.maintenance.v1.listWorkOrdersResponse
import com.acme.maintenance.v1.part
import com.acme.maintenance.v1.serviceSchedule
import com.acme.maintenance.v1.serviceScheduleParticipant
import com.acme.maintenance.v1.simulatePartRequest
import com.acme.maintenance.v1.simulateServiceScheduleRequest
import com.acme.maintenance.v1.watchWorkOrdersResponse
import com.acme.maintenance.v1.workOrder
import com.acme.maintenance.v1.workOrderSla
import com.acme.transport.maintenance.MaintenanceRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.ValidateYardRequest
import com.acme.yards.v1.YardValidationServiceGrpcKt
import com.acme.yards.v1.validateYardRequest
import com.acme.yards.v1.yard
import com.google.protobuf.Timestamp
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.slf4j.LoggerFactory

/** Manages work orders: creates, reads, changes, and lists them. */
class WorkOrderGrpcService(
    private val store: MaintenanceRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyQueryService: AccessPolicyQueryServiceGrpcKt.AccessPolicyQueryServiceCoroutineStub,
    private val deliveriesDeliveryQueryService: DeliveryQueryServiceGrpcKt.DeliveryQueryServiceCoroutineStub,
    private val fuelFuelTransactionQueryService: FuelTransactionQueryServiceGrpcKt.FuelTransactionQueryServiceCoroutineStub,
    private val ledgerLedgerAccountService: LedgerAccountServiceGrpcKt.LedgerAccountServiceCoroutineStub,
    private val maintenancePartCalculationService: PartCalculationServiceGrpcKt.PartCalculationServiceCoroutineStub,
    private val maintenanceServiceScheduleCalculationService: ServiceScheduleCalculationServiceGrpcKt.ServiceScheduleCalculationServiceCoroutineStub,
    private val maintenanceWorkOrderCalculationService: WorkOrderCalculationServiceGrpcKt.WorkOrderCalculationServiceCoroutineStub,
    private val maintenanceWorkOrderQueryService: WorkOrderQueryServiceGrpcKt.WorkOrderQueryServiceCoroutineStub,
    private val yardsYardValidationService: YardValidationServiceGrpcKt.YardValidationServiceCoroutineStub,
) : WorkOrderServiceGrpcKt.WorkOrderServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(WorkOrderGrpcService::class.java)

    private val invokeCreateWorkOrder2: suspend (EstimateWorkOrderRequest) -> EstimateWorkOrderResponse = { payload ->
        maintenanceWorkOrderCalculationService.estimateWorkOrder(payload)
    }

    /** Takes a stream of work orders and answers once at the end. */
    override suspend fun batchCreateWorkOrders(requests: Flow<BatchCreateWorkOrdersRequest>): BatchCreateWorkOrdersResponse {
        var answer = BatchCreateWorkOrdersResponse.getDefaultInstance()
        requests.map { request -> handleBatchCreateWorkOrders(request) }.collect { answer = it }
        return answer
    }

    /** Returns several work orders at once. */
    override suspend fun batchGetWorkOrders(request: BatchGetWorkOrdersRequest): BatchGetWorkOrdersResponse = handleBatchGetWorkOrders(request)

    /** Creates a work order. */
    override suspend fun createWorkOrder(request: CreateWorkOrderRequest): CreateWorkOrderResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderService/CreateWorkOrder") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CreateWorkOrder", request)
            log.atInfo().addKeyValue("operation", "CreateWorkOrder").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.workOrder.assignee.userId).log("Recorded audit detail")
            val outbound0 = findFuelTransactionsRequest {
                this.context = request.context
                this.filter = FindFuelTransactionsRequestKt.filter {
                    this.createdAfter = request.workOrder.audit.updatedAt
                    this.ownerId = request.workOrder.sla.policyId
                }
            }
            val command0: Submission<FindFuelTransactionsRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = fuelFuelTransactionQueryService.findFuelTransactions(payload0)
            val outbound1 = searchLedgerAccountsRequest {
                this.context = request.context
                this.filter = SearchLedgerAccountsRequestKt.filter {
                    this.createdAfter = request.workOrder.sla.dueAt
                    this.labels.putAll(request.workOrder.customFieldsMap)
                    this.ownerId = request.workOrder.workOrderId
                }
            }
            val adapter1 = OutboundPort<SearchLedgerAccountsRequest, SearchLedgerAccountsResponse> { payload ->
                ledgerLedgerAccountService.searchLedgerAccounts(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            val outbound2 = estimateWorkOrderRequest {
                this.context = request.context
                this.workOrder = workOrder {
                    this.audit = auditInfo {
                        this.updatedBy = request.workOrder.audit.updatedBy
                    }
                    this.requester = contactInfo {
                        this.email = request.workOrder.requester.email
                    }
                }
            }
            val reply2 = invokeCreateWorkOrder2(outbound2)
            val outbound3 = simulatePartRequest {
                this.context = request.context
                this.part = part {
                    this.owner = userRef {
                        this.displayName = request.workOrder.customer.displayName
                    }
                }
            }
            val invoke3: suspend (SimulatePartRequest, Metadata) -> SimulatePartResponse = maintenancePartCalculationService::simulatePart
            val reply3 = invoke3(outbound3, Metadata())
            createWorkOrderResponse {
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.referenceIds.addAll((reply0.result.referenceIdsList) + (reply1.result.referenceIdsList))
                    this.status = reply0.result.status
                }
                this.workOrder = workOrder {
                    this.sla = workOrderSla {
                        this.dueAt = reply2.workOrder.sla.dueAt
                    }
                    this.status = request.workOrder.status
                    this.subject = request.workOrder.subject
                }
            }
        }

    /** Deletes a work order. */
    override suspend fun deleteWorkOrder(request: DeleteWorkOrderRequest): DeleteWorkOrderResponse {
        return handleDeleteWorkOrder(request)
    }

    /** Returns a work order. */
    override suspend fun getWorkOrder(request: GetWorkOrderRequest): GetWorkOrderResponse = handleGetWorkOrder(request)

    /** Lists work orders a page at a time. */
    override suspend fun listWorkOrders(request: ListWorkOrdersRequest): ListWorkOrdersResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderService/ListWorkOrders") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListWorkOrders", request)
            log.atInfo().addKeyValue("operation", "ListWorkOrders").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            listWorkOrdersResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Streams the changes of work orders as they happen. */
    override fun watchWorkOrders(request: WatchWorkOrdersRequest): Flow<WatchWorkOrdersResponse> = flow {
        emit(handleWatchWorkOrders(request))
    }

    private suspend fun handleBatchCreateWorkOrders(request: BatchCreateWorkOrdersRequest): BatchCreateWorkOrdersResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderService/BatchCreateWorkOrders") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BatchCreateWorkOrders", request)
            log.atInfo().addKeyValue("operation", "BatchCreateWorkOrders").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.header.source).log("Recorded audit detail")
            val outbound0 = resolveAccessPoliciesRequest {
                this.context = request.context
            }
            val reply0 = authorizationAccessPolicyQueryService.resolveAccessPolicies(WireCopies.json(outbound0))
            batchCreateWorkOrdersResponse {
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }

    private data class BatchGetWorkOrders0Command(
        val context: RequestContext,
        val filterOwnerId: String,
        val filterCreatedAfter: Timestamp
    ) {
        fun toRequest(): BrowseDeliveriesRequest {
            val command = this
            return browseDeliveriesRequest {
                this.context = command.context
                this.filter = BrowseDeliveriesRequestKt.filter {
                    this.createdAfter = command.filterCreatedAfter
                    this.ownerId = command.filterOwnerId
                }
            }
        }
    }

    private suspend fun handleBatchGetWorkOrders(request: BatchGetWorkOrdersRequest): BatchGetWorkOrdersResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderService/BatchGetWorkOrders") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BatchGetWorkOrders", request)
            log.atInfo().addKeyValue("operation", "BatchGetWorkOrders").log("Handling transport request")
            val outbound0 = BatchGetWorkOrders0Command(
                context = request.context,
                filterOwnerId = request.context.actor.userId,
                filterCreatedAfter = request.context.deadline
            ).toRequest()
            val reply0 = deliveriesDeliveryQueryService.browseDeliveries(outbound0)
            batchGetWorkOrdersResponse {
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleDeleteWorkOrder(request: DeleteWorkOrderRequest): DeleteWorkOrderResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderService/DeleteWorkOrder") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DeleteWorkOrder", request)
            log.atInfo().addKeyValue("operation", "DeleteWorkOrder").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.workOrderId).log("Recorded audit detail")
            val outbound0 = ValidateYardRequest.newBuilder().apply {
                setContext(request.context)
                setReason(request.reason)
                setYard(yard {
                    this.audit = auditInfo {
                        this.etag = request.etag
                    }
                    this.owner = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> yardsYardValidationService.validateYard(value) }
            val outbound1 = RequestFactory.create {
                calculateWorkOrderRequest {
                    this.context = request.context
                    this.details = CalculateWorkOrderRequestKt.details {
                        this.destination = postalAddress {
                            this.locality = reply0.yard.address.locality
                        }
                    }
                }
            }
            val reply1 = maintenanceWorkOrderCalculationService.calculateWorkOrder(outbound1)
            deleteWorkOrderResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = reply1.result.status
                }
            }
        }

    private suspend fun handleGetWorkOrder(request: GetWorkOrderRequest): GetWorkOrderResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderService/GetWorkOrder") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetWorkOrder", request)
            log.atInfo().addKeyValue("operation", "GetWorkOrder").log("Handling transport request")
            val outbound0 = browseWorkOrdersRequest {
                this.context = request.context
                this.filter = BrowseWorkOrdersRequestKt.filter {
                    this.createdAfter = request.context.deadline
                    this.ownerId = request.workOrderId
                }
            }
            val reply0 = ClientRegistry.lookup("acme.maintenance.v1.WorkOrderQueryService", WorkOrderQueryServiceGrpcKt.WorkOrderQueryServiceCoroutineStub::class.java).browseWorkOrders(outbound0)
            getWorkOrderResponse {
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
                this.workOrder = workOrder {
                    this.workOrderId = request.workOrderId
                }
            }
        }

    private suspend fun handleWatchWorkOrders(request: WatchWorkOrdersRequest): WatchWorkOrdersResponse =
        RpcBoundary.run("acme.maintenance.v1.WorkOrderService/WatchWorkOrders") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "WatchWorkOrders", request)
            log.atInfo().addKeyValue("operation", "WatchWorkOrders").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = simulateServiceScheduleRequest {
                this.context = request.context
                this.serviceSchedule = serviceSchedule {
                    this.participants.addAll(listOf(serviceScheduleParticipant {
                        this.contact = contactInfo {
                            this.phone = request.subscriber.phone
                        }
                    }))
                }
            }
            val reply0 = maintenanceServiceScheduleCalculationService.simulateServiceSchedule(WireCopies.reflective(outbound0))
            watchWorkOrdersResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
                this.workOrder = workOrder {
                    this.audit = auditInfo {
                        this.etag = reply0.serviceSchedule.audit.etag
                    }
                    this.requester = contactInfo {
                        this.phone = request.subscriber.phone
                        this.preferredChannel = request.subscriber.preferredChannel
                    }
                }
            }
        }
}
