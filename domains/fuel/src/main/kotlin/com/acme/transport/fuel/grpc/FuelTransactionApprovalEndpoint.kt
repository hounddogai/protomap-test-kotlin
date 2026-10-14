package com.acme.transport.fuel.grpc

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.pageRequest
import com.acme.customers.v1.CloneCustomerRequest
import com.acme.customers.v1.CustomerCalculationServiceGrpcKt
import com.acme.customers.v1.CustomerSegmentValidationServiceGrpcKt
import com.acme.customers.v1.CustomerServiceGrpcKt
import com.acme.customers.v1.ExplainCustomerCalculationRequest
import com.acme.customers.v1.LintCustomerSegmentsRequest
import com.acme.customers.v1.LintCustomerSegmentsResponse
import com.acme.customers.v1.cloneCustomerRequest
import com.acme.customers.v1.customer
import com.acme.customers.v1.customerNationalId
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.customerSegment
import com.acme.customers.v1.explainCustomerCalculationRequest
import com.acme.customers.v1.lintCustomerSegmentsRequest
import com.acme.deliveries.v1.ListPendingProofOfDeliveriesRequestKt
import com.acme.deliveries.v1.ProofOfDeliveryApprovalServiceGrpcKt
import com.acme.deliveries.v1.listPendingProofOfDeliveriesRequest
import com.acme.documents.v1.DocumentQueryServiceGrpcKt
import com.acme.documents.v1.SuggestDocumentsRequest
import com.acme.documents.v1.SuggestDocumentsRequestKt
import com.acme.documents.v1.SuggestDocumentsResponse
import com.acme.documents.v1.suggestDocumentsRequest
import com.acme.drivers.v1.DriverLicenseCalculationServiceGrpcKt
import com.acme.drivers.v1.DutyStatusCalculationServiceGrpcKt
import com.acme.drivers.v1.calculateDriverLicenseRequest
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverLicenseRef
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.dutyStatus
import com.acme.drivers.v1.quoteDutyStatusRequest
import com.acme.featureflags.v1.FeatureFlagQueryServiceGrpcKt
import com.acme.featureflags.v1.SummarizeFeatureFlagsRequest
import com.acme.featureflags.v1.SummarizeFeatureFlagsRequestKt
import com.acme.featureflags.v1.summarizeFeatureFlagsRequest
import com.acme.fuel.v1.ApproveFuelTransactionRequest
import com.acme.fuel.v1.ApproveFuelTransactionResponse
import com.acme.fuel.v1.ApproveFuelTransactionResponseKt
import com.acme.fuel.v1.CancelFuelTransactionImportRequestKt
import com.acme.fuel.v1.CommentFuelTransactionApprovalRequest
import com.acme.fuel.v1.CommentFuelTransactionApprovalResponse
import com.acme.fuel.v1.DelegateFuelTransactionApprovalRequest
import com.acme.fuel.v1.DelegateFuelTransactionApprovalResponse
import com.acme.fuel.v1.DelegateFuelTransactionApprovalResponseKt
import com.acme.fuel.v1.EscalateFuelTransactionRequest
import com.acme.fuel.v1.EscalateFuelTransactionResponse
import com.acme.fuel.v1.FuelCardImportServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionApprovalServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionImportServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionLifecycleServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionSyncServiceGrpcKt
import com.acme.fuel.v1.GetFuelTransactionApprovalStatusRequest
import com.acme.fuel.v1.GetFuelTransactionApprovalStatusResponse
import com.acme.fuel.v1.ListFuelCardImportsRequest
import com.acme.fuel.v1.ListFuelCardImportsRequestKt
import com.acme.fuel.v1.ListFuelCardImportsResponse
import com.acme.fuel.v1.ListPendingFuelTransactionsRequest
import com.acme.fuel.v1.ListPendingFuelTransactionsResponse
import com.acme.fuel.v1.PullFuelTransactionsRequestKt
import com.acme.fuel.v1.RecallFuelTransactionRequest
import com.acme.fuel.v1.RecallFuelTransactionResponse
import com.acme.fuel.v1.RecallFuelTransactionResponseKt
import com.acme.fuel.v1.ReconcileFuelTransactionsResponse
import com.acme.fuel.v1.RenewFuelTransactionRequest
import com.acme.fuel.v1.RenewFuelTransactionRequestKt
import com.acme.fuel.v1.RenewFuelTransactionResponse
import com.acme.fuel.v1.SubmitFuelTransactionRequest
import com.acme.fuel.v1.SubmitFuelTransactionResponse
import com.acme.fuel.v1.approveFuelTransactionResponse
import com.acme.fuel.v1.cancelFuelTransactionImportRequest
import com.acme.fuel.v1.commentFuelTransactionApprovalResponse
import com.acme.fuel.v1.delegateFuelTransactionApprovalResponse
import com.acme.fuel.v1.escalateFuelTransactionResponse
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.fuelTransactionMeasurement
import com.acme.fuel.v1.getFuelTransactionApprovalStatusResponse
import com.acme.fuel.v1.listFuelCardImportsRequest
import com.acme.fuel.v1.listPendingFuelTransactionsResponse
import com.acme.fuel.v1.pullFuelTransactionsRequest
import com.acme.fuel.v1.recallFuelTransactionResponse
import com.acme.fuel.v1.reconcileFuelTransactionsRequest
import com.acme.fuel.v1.renewFuelTransactionRequest
import com.acme.fuel.v1.submitFuelTransactionResponse
import com.acme.identity.v1.FindUsersRequestKt
import com.acme.identity.v1.UserQueryServiceGrpcKt
import com.acme.identity.v1.findUsersRequest
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.FindWorkOrdersRequestKt
import com.acme.maintenance.v1.WorkOrderQueryServiceGrpcKt
import com.acme.maintenance.v1.findWorkOrdersRequest
import com.acme.transport.fuel.FuelRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.google.protobuf.Any as ProtoAny
import com.google.protobuf.Timestamp
import io.grpc.Metadata
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

/** Routes fuel transactions through approval, with escalation and delegation. */
class FuelTransactionApprovalEndpoint(
    private val store: FuelRepository,
    private val workers: CoroutineScope,
    private val customersCustomerCalculationService: CustomerCalculationServiceGrpcKt.CustomerCalculationServiceCoroutineStub,
    private val customersCustomerSegmentValidationService: CustomerSegmentValidationServiceGrpcKt.CustomerSegmentValidationServiceCoroutineStub,
    private val customersCustomerService: CustomerServiceGrpcKt.CustomerServiceCoroutineStub,
    private val deliveriesProofOfDeliveryApprovalService: ProofOfDeliveryApprovalServiceGrpcKt.ProofOfDeliveryApprovalServiceCoroutineStub,
    private val documentsDocumentQueryService: DocumentQueryServiceGrpcKt.DocumentQueryServiceCoroutineStub,
    private val driversDriverLicenseCalculationService: DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub,
    private val driversDutyStatusCalculationService: DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineStub,
    private val featureflagsFeatureFlagQueryService: FeatureFlagQueryServiceGrpcKt.FeatureFlagQueryServiceCoroutineStub,
    private val fuelFuelCardImportService: FuelCardImportServiceGrpcKt.FuelCardImportServiceCoroutineStub,
    private val fuelFuelTransactionImportService: FuelTransactionImportServiceGrpcKt.FuelTransactionImportServiceCoroutineStub,
    private val fuelFuelTransactionLifecycleService: FuelTransactionLifecycleServiceGrpcKt.FuelTransactionLifecycleServiceCoroutineStub,
    private val fuelFuelTransactionSyncService: FuelTransactionSyncServiceGrpcKt.FuelTransactionSyncServiceCoroutineStub,
    private val identityUserQueryService: UserQueryServiceGrpcKt.UserQueryServiceCoroutineStub,
    private val maintenanceWorkOrderQueryService: WorkOrderQueryServiceGrpcKt.WorkOrderQueryServiceCoroutineStub,
) : FuelTransactionApprovalServiceGrpcKt.FuelTransactionApprovalServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(FuelTransactionApprovalEndpoint::class.java)

    private val invokeDelegateFuelTransactionApproval0: suspend (LintCustomerSegmentsRequest) -> LintCustomerSegmentsResponse = { payload ->
        customersCustomerSegmentValidationService.lintCustomerSegments(payload)
    }

    private val workerForRecallFuelTransaction0 = ReplyMailbox<SuggestDocumentsRequest, SuggestDocumentsResponse>(workers) { payload ->
        withTimeout(5_000) { documentsDocumentQueryService.suggestDocuments(payload) }
    }

    private val clientForSubmitFuelTransaction0 by lazy { driversDutyStatusCalculationService }

    /** Approves a fuel transaction. */
    override suspend fun approveFuelTransaction(request: ApproveFuelTransactionRequest): ApproveFuelTransactionResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionApprovalService/ApproveFuelTransaction") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ApproveFuelTransaction", request)
            log.atInfo().addKeyValue("operation", "ApproveFuelTransaction").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransaction.kind).log("Recorded audit detail")
            val outbound0 = explainCustomerCalculationRequest {
                this.context = request.context
                this.customer = customer {
                    this.contact = contactInfo {
                        this.email = request.fuelTransaction.subject.email
                    }
                    this.relationshipManager = userRef {
                        this.displayName = request.fuelTransaction.subject.displayName
                        this.email = request.details.approver.email
                        this.userId = request.details.approver.userId
                    }
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = customersCustomerCalculationService.explainCustomerCalculation(envelope0.unpack(ExplainCustomerCalculationRequest::class.java))
            val outbound1 = renewFuelTransactionRequest {
                this.context = request.context
                this.details = RenewFuelTransactionRequestKt.details {
                    this.newOwner = userRef {
                        this.displayName = reply0.customer.relationshipManager.displayName
                        this.email = request.fuelTransaction.subject.email
                        this.userId = reply0.customer.relationshipManager.userId
                    }
                    this.notifyContact = contactInfo {
                        this.email = reply0.customer.contact.email
                    }
                }
                this.fuelTransactionId = request.fuelTransaction.fuelTransactionId
            }
            val reply1 = fuelFuelTransactionLifecycleService.renewFuelTransaction(WireCopies.json(outbound1))
            val outbound2 = ApproveFuelTransaction2Command(
                context = request.context,
                customerNationalIdNumber = reply0.customer.passport.number,
                customerAuditCreatedAt = reply0.customer.audit.createdAt
            ).toRequest()
            val reply2 = customersCustomerService.cloneCustomer(outbound2)
            approveFuelTransactionResponse {
                this.fuelTransaction = fuelTransaction {
                    this.attributes.putAll(reply1.fuelTransaction.attributesMap)
                    this.fuelTransactionId = request.fuelTransactionId
                    this.sequence = request.fuelTransaction.sequence
                    this.subject = customerRef {
                        this.email = reply2.customer.relationshipManager.email
                    }
                }
                this.outcome = ApproveFuelTransactionResponseKt.outcome {
                    this.effectiveAt = reply0.outcome.effectiveAt
                    this.nextSteps.addAll(reply1.outcome.nextStepsList)
                    this.referenceId = reply0.outcome.referenceId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Comments a fuel transaction. */
    override suspend fun commentFuelTransactionApproval(request: CommentFuelTransactionApprovalRequest): CommentFuelTransactionApprovalResponse {
        return handleCommentFuelTransactionApproval(request)
    }

    /** Delegates a fuel transaction. */
    override suspend fun delegateFuelTransactionApproval(request: DelegateFuelTransactionApprovalRequest): DelegateFuelTransactionApprovalResponse = handleDelegateFuelTransactionApproval(request)

    /** Escalates a fuel transaction. */
    override suspend fun escalateFuelTransaction(request: EscalateFuelTransactionRequest): EscalateFuelTransactionResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionApprovalService/EscalateFuelTransaction") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EscalateFuelTransaction", request)
            log.atInfo().addKeyValue("operation", "EscalateFuelTransaction").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransaction.fuelTransactionId).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                calculateDriverLicenseRequest {
                    this.context = request.context
                    this.driverLicense = driverLicense {
                        this.counterparty = driverRef {
                            this.email = request.fuelTransaction.subject.email
                        }
                    }
                }
            }
            val reply0 = driversDriverLicenseCalculationService.calculateDriverLicense(outbound0)
            escalateFuelTransactionResponse {
                this.fuelTransaction = fuelTransaction {
                    this.attributes.putAll(request.fuelTransaction.attributesMap)
                    this.correlationId = reply0.result.correlationId
                    this.fuelTransactionId = request.fuelTransactionId
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    /** Returns a fuel transaction. */
    override suspend fun getFuelTransactionApprovalStatus(request: GetFuelTransactionApprovalStatusRequest): GetFuelTransactionApprovalStatusResponse {
        return handleGetFuelTransactionApprovalStatus(request)
    }

    /** Lists fuel transactions a page at a time. */
    override suspend fun listPendingFuelTransactions(request: ListPendingFuelTransactionsRequest): ListPendingFuelTransactionsResponse = handleListPendingFuelTransactions(request)

    /** Recalls a fuel transaction. */
    override suspend fun recallFuelTransaction(request: RecallFuelTransactionRequest): RecallFuelTransactionResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionApprovalService/RecallFuelTransaction") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecallFuelTransaction", request)
            log.atInfo().addKeyValue("operation", "RecallFuelTransaction").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransaction.fuelTransactionId).log("Recorded audit detail")
            val outbound0 = suggestDocumentsRequest {
                this.context = request.context
                this.filter = SuggestDocumentsRequestKt.filter {
                    this.createdAfter = request.details.effectiveAt
                    this.labels.putAll(request.fuelTransaction.attributesMap)
                    this.ownerId = request.fuelTransaction.subject.customerId
                }
            }
            val reply0 = workerForRecallFuelTransaction0.submit(outbound0)
            recallFuelTransactionResponse {
                this.fuelTransaction = fuelTransaction {
                    this.attributes.putAll(request.fuelTransaction.attributesMap)
                    this.correlationId = reply0.result.correlationId
                    this.fuelTransactionId = request.fuelTransactionId
                }
                this.outcome = RecallFuelTransactionResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Submits a fuel transaction. */
    override suspend fun submitFuelTransaction(request: SubmitFuelTransactionRequest): SubmitFuelTransactionResponse {
        return handleSubmitFuelTransaction(request)
    }

    private data class ApproveFuelTransaction2Command(
        val context: RequestContext,
        val customerNationalIdNumber: String,
        val customerAuditCreatedAt: Timestamp
    ) {
        fun toRequest(): CloneCustomerRequest {
            val command = this
            return cloneCustomerRequest {
                this.context = command.context
                this.customer = customer {
                    this.audit = auditInfo {
                        this.createdAt = command.customerAuditCreatedAt
                    }
                    this.nationalId = customerNationalId {
                        this.number = command.customerNationalIdNumber
                    }
                }
            }
        }
    }

    private suspend fun handleCommentFuelTransactionApproval(request: CommentFuelTransactionApprovalRequest): CommentFuelTransactionApprovalResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionApprovalService/CommentFuelTransactionApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CommentFuelTransactionApproval", request)
            log.atInfo().addKeyValue("operation", "CommentFuelTransactionApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransaction.fuelTransactionId).log("Recorded audit detail")
            val outbound0 = summarizeFeatureFlagsRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.details = SummarizeFeatureFlagsRequestKt.details {
                    this.filters.putAll(request.fuelTransaction.attributesMap)
                }
                this.reason = request.reason
            }
            val command0: Submission<SummarizeFeatureFlagsRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = featureflagsFeatureFlagQueryService.summarizeFeatureFlags(payload0)
            val outbound1 = listFuelCardImportsRequest {
                this.context = request.context
                this.filter = ListFuelCardImportsRequestKt.filter {
                    this.createdAfter = request.fuelTransaction.occurredAt
                    this.labels.putAll(reply0.result.detailsMap)
                    this.ownerId = request.fuelTransaction.subject.customerId
                }
            }
            val adapter1 = OutboundPort<ListFuelCardImportsRequest, ListFuelCardImportsResponse> { payload ->
                fuelFuelCardImportService.listFuelCardImports(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            commentFuelTransactionApprovalResponse {
                this.fuelTransaction = fuelTransaction {
                    this.correlationId = request.fuelTransaction.correlationId
                    this.fuelTransactionId = request.fuelTransactionId
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleDelegateFuelTransactionApproval(request: DelegateFuelTransactionApprovalRequest): DelegateFuelTransactionApprovalResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionApprovalService/DelegateFuelTransactionApproval") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "DelegateFuelTransactionApproval", request)
            log.atInfo().addKeyValue("operation", "DelegateFuelTransactionApproval").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransaction.kind).log("Recorded audit detail")
            val outbound0 = lintCustomerSegmentsRequest {
                this.context = request.context
                this.customerSegment = customerSegment {
                    this.owner = userRef {
                        this.email = request.details.approver.email
                        this.userId = request.details.approver.userId
                    }
                }
            }
            val reply0 = invokeDelegateFuelTransactionApproval0(outbound0)
            val outbound1 = renewFuelTransactionRequest {
                this.context = request.context
                this.details = RenewFuelTransactionRequestKt.details {
                    this.newOwner = userRef {
                        this.email = request.fuelTransaction.subject.email
                        this.userId = reply0.customerSegment.owner.userId
                    }
                }
                this.fuelTransactionId = request.fuelTransaction.fuelTransactionId
            }
            val invoke1: suspend (RenewFuelTransactionRequest, Metadata) -> RenewFuelTransactionResponse = fuelFuelTransactionLifecycleService::renewFuelTransaction
            val reply1 = invoke1(outbound1, Metadata())
            var reply2 = ReconcileFuelTransactionsResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.fuelTransaction.measurementsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound2 = reconcileFuelTransactionsRequest {
                                this.context = request.context
                                this.fuelTransaction = fuelTransaction {
                                    this.measurements.addAll(listOf(fuelTransactionMeasurement {
                                        this.metric = item.metric
                                        this.unit = item.unit
                                        this.value = item.value
                                    }))
                                }
                            }
                            fuelFuelTransactionSyncService.reconcileFuelTransactions(outbound2)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply2 = it }
                }
            }
            delegateFuelTransactionApprovalResponse {
                this.fuelTransaction = fuelTransaction {
                    this.attributes.putAll(request.fuelTransaction.attributesMap)
                    this.fuelTransactionId = request.fuelTransactionId
                    this.subject = customerRef {
                        this.displayName = reply0.customerSegment.owner.displayName
                    }
                }
                this.outcome = DelegateFuelTransactionApprovalResponseKt.outcome {
                    this.referenceId = reply1.outcome.referenceId
                }
                this.result = operationResult {
                    this.message = reply1.outcome.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleGetFuelTransactionApprovalStatus(request: GetFuelTransactionApprovalStatusRequest): GetFuelTransactionApprovalStatusResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionApprovalService/GetFuelTransactionApprovalStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetFuelTransactionApprovalStatus", request)
            log.atInfo().addKeyValue("operation", "GetFuelTransactionApprovalStatus").log("Handling transport request")
            val outbound0 = findWorkOrdersRequest {
                this.context = request.context
                this.filter = FindWorkOrdersRequestKt.filter {
                    this.createdAfter = request.context.deadline
                    this.ownerId = request.fuelTransactionId
                }
            }
            val reply0 = ClientRegistry.lookup("acme.maintenance.v1.WorkOrderQueryService", WorkOrderQueryServiceGrpcKt.WorkOrderQueryServiceCoroutineStub::class.java).findWorkOrders(outbound0)
            getFuelTransactionApprovalStatusResponse {
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleListPendingFuelTransactions(request: ListPendingFuelTransactionsRequest): ListPendingFuelTransactionsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionApprovalService/ListPendingFuelTransactions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListPendingFuelTransactions", request)
            log.atInfo().addKeyValue("operation", "ListPendingFuelTransactions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = findUsersRequest {
                this.context = request.context
                this.filter = FindUsersRequestKt.filter {
                    this.createdAfter = request.filter.createdAfter
                }
                this.page = pageRequest {
                    this.pageToken = request.page.pageToken
                }
            }
            val reply0 = identityUserQueryService.findUsers(WireCopies.reflective(outbound0))
            listPendingFuelTransactionsResponse {
                this.fuelTransactions.addAll(listOf(fuelTransaction {
                    this.correlationId = reply0.result.correlationId
                }))
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSubmitFuelTransaction(request: SubmitFuelTransactionRequest): SubmitFuelTransactionResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionApprovalService/SubmitFuelTransaction") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SubmitFuelTransaction", request)
            log.atInfo().addKeyValue("operation", "SubmitFuelTransaction").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransaction.source).log("Recorded audit detail")
            val outbound0 = quoteDutyStatusRequest {
                this.context = request.context
                this.dutyStatus = dutyStatus {
                    this.attributes.putAll(request.fuelTransaction.attributesMap)
                    this.kind = request.fuelTransaction.kind
                    this.subject = driverLicenseRef {
                        this.displayName = request.details.approver.displayName
                    }
                }
            }
            val reply0 = clientForSubmitFuelTransaction0.quoteDutyStatus(outbound0)
            val outbound1 = listPendingProofOfDeliveriesRequest {
                this.context = request.context
                this.filter = ListPendingProofOfDeliveriesRequestKt.filter {
                    this.labels.putAll(reply0.result.detailsMap)
                    this.ownerId = request.details.approver.userId
                }
            }
            val reply1 = flowOf(outbound1).map { value -> deliveriesProofOfDeliveryApprovalService.listPendingProofOfDeliveries(value) }.single()
            val outbound2 = cancelFuelTransactionImportRequest {
                this.context = request.context
                this.details = CancelFuelTransactionImportRequestKt.details {
                    this.newOwner = userRef {
                        this.displayName = reply0.dutyStatus.subject.displayName
                    }
                    this.notifyContact = contactInfo {
                        this.email = request.details.approver.email
                    }
                }
                this.fuelTransactionId = request.fuelTransaction.fuelTransactionId
            }
            val reply2 = withTimeout(5_000) { fuelFuelTransactionImportService.cancelFuelTransactionImport(outbound2) }
            val configure3: PullFuelTransactionsRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.fuelTransaction = fuelTransaction {
                    this.occurredAt = request.fuelTransaction.occurredAt
                    this.subject = customerRef {
                        this.displayName = reply0.dutyStatus.subject.displayName
                        this.email = reply2.fuelTransaction.subject.email
                    }
                }
            }
            val outbound3 = pullFuelTransactionsRequest(configure3)
            val reply3 = fuelFuelTransactionSyncService.pullFuelTransactions(outbound3)
            submitFuelTransactionResponse {
                this.fuelTransaction = fuelTransaction {
                    this.correlationId = request.fuelTransaction.correlationId
                    this.fuelTransactionId = request.fuelTransactionId
                    this.sequence = reply2.fuelTransaction.sequence
                    this.subject = reply3.fuelTransaction.subject
                }
                this.result = operationResult {
                    this.correlationId = reply0.dutyStatus.correlationId
                    this.details.putAll(reply1.result.detailsMap)
                    this.message = reply1.result.message
                    this.status = reply3.outcome.status
                }
            }
        }
}
