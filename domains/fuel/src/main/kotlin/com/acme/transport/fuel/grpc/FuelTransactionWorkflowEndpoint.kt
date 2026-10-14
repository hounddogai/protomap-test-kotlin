package com.acme.transport.fuel.grpc

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.pageRequest
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.customerRef
import com.acme.deliveries.v1.DeliveryApprovalServiceGrpcKt
import com.acme.deliveries.v1.ProofOfDeliveryWorkflowServiceGrpcKt
import com.acme.deliveries.v1.RejectDeliveryRequestKt
import com.acme.deliveries.v1.cancelProofOfDeliveryWorkflowRequest
import com.acme.deliveries.v1.copy as deliveriesCopy
import com.acme.deliveries.v1.delivery
import com.acme.deliveries.v1.listProofOfDeliveryWorkflowsRequest
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.rejectDeliveryRequest
import com.acme.drivers.v1.CountDriversRequestKt
import com.acme.drivers.v1.DriverLicenseCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverQueryServiceGrpcKt
import com.acme.drivers.v1.DriverServiceGrpcKt
import com.acme.drivers.v1.EstimateDriverLicenseRequestKt
import com.acme.drivers.v1.ListDriversRequestKt
import com.acme.drivers.v1.ListDriversResponse
import com.acme.drivers.v1.countDriversRequest
import com.acme.drivers.v1.createDriverRequest
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverLicenseCardSettlement
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.estimateDriverLicenseRequest
import com.acme.drivers.v1.listDriversRequest
import com.acme.featureflags.v1.FeatureFlagValidationServiceGrpcKt
import com.acme.featureflags.v1.ScoreFeatureFlagRequest
import com.acme.featureflags.v1.ScoreFeatureFlagResponse
import com.acme.featureflags.v1.featureFlag
import com.acme.featureflags.v1.featureFlagBlockAction
import com.acme.featureflags.v1.scoreFeatureFlagRequest
import com.acme.fuel.v1.AdvanceFuelTransactionWorkflowRequest
import com.acme.fuel.v1.AdvanceFuelTransactionWorkflowResponse
import com.acme.fuel.v1.CancelFuelTransactionWorkflowRequest
import com.acme.fuel.v1.CancelFuelTransactionWorkflowResponse
import com.acme.fuel.v1.CancelFuelTransactionWorkflowResponseKt
import com.acme.fuel.v1.DelegateFuelCardApprovalRequestKt
import com.acme.fuel.v1.DelegateFuelCardApprovalResponse
import com.acme.fuel.v1.ExportFuelTransactionsRequestKt
import com.acme.fuel.v1.FuelCardApprovalServiceGrpcKt
import com.acme.fuel.v1.FuelCardCalculationServiceGrpcKt
import com.acme.fuel.v1.FuelCardExportServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionMeasurement
import com.acme.fuel.v1.FuelTransactionQueryServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionSyncServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionWorkflowServiceGrpcKt
import com.acme.fuel.v1.InspectFuelTransactionWorkflowRequest
import com.acme.fuel.v1.InspectFuelTransactionWorkflowResponse
import com.acme.fuel.v1.ListFuelTransactionWorkflowsRequest
import com.acme.fuel.v1.ListFuelTransactionWorkflowsResponse
import com.acme.fuel.v1.PauseFuelTransactionWorkflowRequest
import com.acme.fuel.v1.PauseFuelTransactionWorkflowResponse
import com.acme.fuel.v1.PauseFuelTransactionWorkflowResponseKt
import com.acme.fuel.v1.PullFuelTransactionsRequest
import com.acme.fuel.v1.ResumeFuelTransactionWorkflowRequest
import com.acme.fuel.v1.ResumeFuelTransactionWorkflowResponse
import com.acme.fuel.v1.RetryFuelTransactionWorkflowRequest
import com.acme.fuel.v1.RetryFuelTransactionWorkflowResponse
import com.acme.fuel.v1.ScheduleFuelCardExportRequest
import com.acme.fuel.v1.ScheduleFuelCardExportRequestKt
import com.acme.fuel.v1.SignalFuelTransactionWorkflowRequest
import com.acme.fuel.v1.SignalFuelTransactionWorkflowResponse
import com.acme.fuel.v1.SignalFuelTransactionWorkflowResponseKt
import com.acme.fuel.v1.StartFuelTransactionWorkflowRequest
import com.acme.fuel.v1.StartFuelTransactionWorkflowResponse
import com.acme.fuel.v1.StartFuelTransactionWorkflowResponseKt
import com.acme.fuel.v1.advanceFuelTransactionWorkflowResponse
import com.acme.fuel.v1.cancelFuelTransactionWorkflowResponse
import com.acme.fuel.v1.delegateFuelCardApprovalRequest
import com.acme.fuel.v1.exportFuelTransactionsRequest
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.inspectFuelTransactionWorkflowResponse
import com.acme.fuel.v1.listFuelTransactionWorkflowsResponse
import com.acme.fuel.v1.pauseFuelTransactionWorkflowResponse
import com.acme.fuel.v1.pullFuelTransactionsRequest
import com.acme.fuel.v1.resumeFuelTransactionWorkflowResponse
import com.acme.fuel.v1.retryFuelTransactionWorkflowResponse
import com.acme.fuel.v1.scheduleFuelCardExportRequest
import com.acme.fuel.v1.signalFuelTransactionWorkflowResponse
import com.acme.fuel.v1.simulateFuelCardRequest
import com.acme.fuel.v1.startFuelTransactionWorkflowResponse
import com.acme.identity.v1.userRef
import com.acme.inventory.v1.CountStockItemsRequest
import com.acme.inventory.v1.CountStockItemsRequestKt
import com.acme.inventory.v1.StockItemQueryServiceGrpcKt
import com.acme.inventory.v1.countStockItemsRequest
import com.acme.modelregistry.v1.RegisteredModelQueryServiceGrpcKt
import com.acme.modelregistry.v1.resolveRegisteredModelsRequest
import com.acme.notifications.v1.ExportNotificationsRequestKt
import com.acme.notifications.v1.NotificationQueryServiceGrpcKt
import com.acme.notifications.v1.exportNotificationsRequest
import com.acme.routing.v1.RouteServiceGrpcKt
import com.acme.routing.v1.cloneRouteRequest
import com.acme.routing.v1.route
import com.acme.transport.fuel.FuelRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.ListYardWorkflowsRequestKt
import com.acme.yards.v1.RecallYardMoveRequest
import com.acme.yards.v1.RecallYardMoveRequestKt
import com.acme.yards.v1.RecallYardRequestKt
import com.acme.yards.v1.YardApprovalServiceGrpcKt
import com.acme.yards.v1.YardMoveApprovalServiceGrpcKt
import com.acme.yards.v1.YardWorkflowServiceGrpcKt
import com.acme.yards.v1.listYardWorkflowsRequest
import com.acme.yards.v1.recallYardMoveRequest
import com.acme.yards.v1.recallYardRequest
import com.acme.yards.v1.yard
import com.acme.yards.v1.yardMove
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

/** Runs the long-lived workflow that takes a fuel transaction from start to finish. */
class FuelTransactionWorkflowEndpoint(
    private val store: FuelRepository,
    private val workers: CoroutineScope,
    private val deliveriesDeliveryApprovalService: DeliveryApprovalServiceGrpcKt.DeliveryApprovalServiceCoroutineStub,
    private val deliveriesProofOfDeliveryWorkflowService: ProofOfDeliveryWorkflowServiceGrpcKt.ProofOfDeliveryWorkflowServiceCoroutineStub,
    private val driversDriverLicenseCalculationService: DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub,
    private val driversDriverQueryService: DriverQueryServiceGrpcKt.DriverQueryServiceCoroutineStub,
    private val driversDriverService: DriverServiceGrpcKt.DriverServiceCoroutineStub,
    private val featureflagsFeatureFlagValidationService: FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub,
    private val fuelFuelCardApprovalService: FuelCardApprovalServiceGrpcKt.FuelCardApprovalServiceCoroutineStub,
    private val fuelFuelCardCalculationService: FuelCardCalculationServiceGrpcKt.FuelCardCalculationServiceCoroutineStub,
    private val fuelFuelCardExportService: FuelCardExportServiceGrpcKt.FuelCardExportServiceCoroutineStub,
    private val fuelFuelTransactionQueryService: FuelTransactionQueryServiceGrpcKt.FuelTransactionQueryServiceCoroutineStub,
    private val fuelFuelTransactionSyncService: FuelTransactionSyncServiceGrpcKt.FuelTransactionSyncServiceCoroutineStub,
    private val inventoryStockItemQueryService: StockItemQueryServiceGrpcKt.StockItemQueryServiceCoroutineStub,
    private val modelregistryRegisteredModelQueryService: RegisteredModelQueryServiceGrpcKt.RegisteredModelQueryServiceCoroutineStub,
    private val notificationsNotificationQueryService: NotificationQueryServiceGrpcKt.NotificationQueryServiceCoroutineStub,
    private val routingRouteService: RouteServiceGrpcKt.RouteServiceCoroutineStub,
    private val yardsYardApprovalService: YardApprovalServiceGrpcKt.YardApprovalServiceCoroutineStub,
    private val yardsYardMoveApprovalService: YardMoveApprovalServiceGrpcKt.YardMoveApprovalServiceCoroutineStub,
    private val yardsYardWorkflowService: YardWorkflowServiceGrpcKt.YardWorkflowServiceCoroutineStub,
) : FuelTransactionWorkflowServiceGrpcKt.FuelTransactionWorkflowServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(FuelTransactionWorkflowEndpoint::class.java)

    private val workerForAdvanceFuelTransactionWorkflow0 = ReplyMailbox<ScoreFeatureFlagRequest, ScoreFeatureFlagResponse>(workers) { payload ->
        withTimeout(5_000) { featureflagsFeatureFlagValidationService.scoreFeatureFlag(payload) }
    }

    private val clientForCancelFuelTransactionWorkflow0 by lazy { yardsYardApprovalService }

    /** Advances a fuel transaction. */
    override suspend fun advanceFuelTransactionWorkflow(request: AdvanceFuelTransactionWorkflowRequest): AdvanceFuelTransactionWorkflowResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionWorkflowService/AdvanceFuelTransactionWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "AdvanceFuelTransactionWorkflow", request)
            log.atInfo().addKeyValue("operation", "AdvanceFuelTransactionWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransaction.fuelTransactionId).log("Recorded audit detail")
            val outbound0 = scoreFeatureFlagRequest {
                this.context = request.context
                this.featureFlag = featureFlag {
                    this.block = featureFlagBlockAction {
                        this.reason = request.reason
                    }
                    this.owner = userRef {
                        this.displayName = request.fuelTransaction.subject.displayName
                        this.email = request.fuelTransaction.subject.email
                    }
                    this.parameters.putAll(request.details.parametersMap)
                }
            }
            val reply0 = workerForAdvanceFuelTransactionWorkflow0.submit(outbound0)
            advanceFuelTransactionWorkflowResponse {
                this.fuelTransaction = fuelTransaction {
                    this.attributes.putAll(request.fuelTransaction.attributesMap)
                    this.fuelTransactionId = request.fuelTransactionId
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = reply0.result.status
                }
            }
        }

    /** Cancels a fuel transaction. */
    override suspend fun cancelFuelTransactionWorkflow(request: CancelFuelTransactionWorkflowRequest): CancelFuelTransactionWorkflowResponse {
        return handleCancelFuelTransactionWorkflow(request)
    }

    /** Returns a fuel transaction. */
    override suspend fun inspectFuelTransactionWorkflow(request: InspectFuelTransactionWorkflowRequest): InspectFuelTransactionWorkflowResponse = handleInspectFuelTransactionWorkflow(request)

    /** Lists fuel transactions a page at a time. */
    override suspend fun listFuelTransactionWorkflows(request: ListFuelTransactionWorkflowsRequest): ListFuelTransactionWorkflowsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionWorkflowService/ListFuelTransactionWorkflows") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListFuelTransactionWorkflows", request)
            log.atInfo().addKeyValue("operation", "ListFuelTransactionWorkflows").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = listProofOfDeliveryWorkflowsRequest {
                this.context = request.context
                this.page = pageRequest {
                    this.pageToken = request.page.pageToken
                }
            }
            val reply0 = deliveriesProofOfDeliveryWorkflowService.listProofOfDeliveryWorkflows(outbound0)
            val outbound1 = cancelProofOfDeliveryWorkflowRequest {}.deliveriesCopy {
                this.context = request.context
                this.proofOfDelivery = proofOfDelivery {
                    this.audit = auditInfo {
                        this.updatedAt = request.filter.createdAfter
                    }
                    this.billingAddress = postalAddress {
                        this.region = request.filter.region
                    }
                    this.counterparty = driverRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply1 = deliveriesProofOfDeliveryWorkflowService.cancelProofOfDeliveryWorkflow(outbound1)
            listFuelTransactionWorkflowsResponse {
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.referenceIds.addAll((reply0.result.referenceIdsList) + (reply1.result.referenceIdsList))
                    this.status = reply0.result.status
                }
            }
        }

    /** Pauses a fuel transaction. */
    override suspend fun pauseFuelTransactionWorkflow(request: PauseFuelTransactionWorkflowRequest): PauseFuelTransactionWorkflowResponse {
        return handlePauseFuelTransactionWorkflow(request)
    }

    /** Resumes a fuel transaction. */
    override suspend fun resumeFuelTransactionWorkflow(request: ResumeFuelTransactionWorkflowRequest): ResumeFuelTransactionWorkflowResponse = handleResumeFuelTransactionWorkflow(request)

    /** Retrys a fuel transaction. */
    override suspend fun retryFuelTransactionWorkflow(request: RetryFuelTransactionWorkflowRequest): RetryFuelTransactionWorkflowResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionWorkflowService/RetryFuelTransactionWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RetryFuelTransactionWorkflow", request)
            log.atInfo().addKeyValue("operation", "RetryFuelTransactionWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransaction.fuelTransactionId).log("Recorded audit detail")
            val outbound0 = exportFuelTransactionsRequest {
                this.context = request.context
                this.filter = ExportFuelTransactionsRequestKt.filter {
                    this.createdAfter = request.fuelTransaction.occurredAt
                }
            }
            fuelFuelTransactionQueryService.exportFuelTransactions(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            retryFuelTransactionWorkflowResponse {
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                    this.subject = request.fuelTransaction.subject
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Signals a fuel transaction. */
    override suspend fun signalFuelTransactionWorkflow(request: SignalFuelTransactionWorkflowRequest): SignalFuelTransactionWorkflowResponse {
        return handleSignalFuelTransactionWorkflow(request)
    }

    /** Starts a fuel transaction. */
    override suspend fun startFuelTransactionWorkflow(request: StartFuelTransactionWorkflowRequest): StartFuelTransactionWorkflowResponse = handleStartFuelTransactionWorkflow(request)

    private suspend fun handleCancelFuelTransactionWorkflow(request: CancelFuelTransactionWorkflowRequest): CancelFuelTransactionWorkflowResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionWorkflowService/CancelFuelTransactionWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CancelFuelTransactionWorkflow", request)
            log.atInfo().addKeyValue("operation", "CancelFuelTransactionWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransaction.fuelTransactionId).log("Recorded audit detail")
            val outbound0 = recallYardRequest {
                this.context = request.context
                this.details = RecallYardRequestKt.details {
                    this.approver = userRef {
                        this.email = request.fuelTransaction.subject.email
                    }
                }
                this.yard = yard {
                    this.owner = userRef {
                        this.displayName = request.fuelTransaction.subject.displayName
                        this.email = request.details.notify.email
                    }
                }
            }
            val reply0 = clientForCancelFuelTransactionWorkflow0.recallYard(outbound0)
            val outbound1 = createDriverRequest {
                this.context = request.context
                this.driver = driver {
                    this.audit = auditInfo {
                        this.createdBy = reply0.yard.audit.createdBy
                    }
                    this.relationshipManager = userRef {
                        this.displayName = reply0.yard.owner.displayName
                    }
                }
            }
            val reply1 = flowOf(outbound1).map { value -> driversDriverService.createDriver(value) }.single()
            val outbound2 = rejectDeliveryRequest {
                this.context = request.context
                this.delivery = delivery {
                    this.audit = auditInfo {
                        this.createdBy = reply1.driver.audit.createdBy
                    }
                    this.billingAddress = postalAddress {
                        this.lines.addAll(reply0.yard.address.linesList)
                    }
                }
                this.details = RejectDeliveryRequestKt.details {
                    this.approver = userRef {
                        this.email = reply1.driver.relationshipManager.email
                        this.userId = reply1.driver.relationshipManager.userId
                    }
                }
            }
            val reply2 = withTimeout(5_000) { deliveriesDeliveryApprovalService.rejectDelivery(outbound2) }
            val configure3: EstimateDriverLicenseRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.details = EstimateDriverLicenseRequestKt.details {
                    this.destination = postalAddress {
                        this.locality = reply1.driver.primaryAddress.locality
                    }
                }
                this.driverLicense = driverLicense {
                    this.audit = auditInfo {
                        this.createdBy = reply0.yard.audit.createdBy
                    }
                    this.billingAddress = postalAddress {
                        this.lines.addAll(reply0.yard.address.linesList)
                    }
                    this.card = driverLicenseCardSettlement {
                        this.last4 = reply2.delivery.card.last4
                    }
                }
            }
            val outbound3 = estimateDriverLicenseRequest(configure3)
            val reply3 = driversDriverLicenseCalculationService.estimateDriverLicense(outbound3)
            cancelFuelTransactionWorkflowResponse {
                this.fuelTransaction = fuelTransaction {
                    this.attributes.putAll(reply1.driver.attributesMap)
                    this.correlationId = reply2.result.correlationId
                    this.fuelTransactionId = request.fuelTransactionId
                    this.kind = request.fuelTransaction.kind
                }
                this.outcome = CancelFuelTransactionWorkflowResponseKt.outcome {
                    this.message = reply0.outcome.message
                }
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = reply0.outcome.status
                }
            }
        }

    private suspend fun handleInspectFuelTransactionWorkflow(request: InspectFuelTransactionWorkflowRequest): InspectFuelTransactionWorkflowResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionWorkflowService/InspectFuelTransactionWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "InspectFuelTransactionWorkflow", request)
            log.atInfo().addKeyValue("operation", "InspectFuelTransactionWorkflow").log("Handling transport request")
            val outbound0 = resolveRegisteredModelsRequest {
                this.context = request.context
            }
            val reply0 = modelregistryRegisteredModelQueryService.resolveRegisteredModels(outbound0)
            inspectFuelTransactionWorkflowResponse {
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handlePauseFuelTransactionWorkflow(request: PauseFuelTransactionWorkflowRequest): PauseFuelTransactionWorkflowResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionWorkflowService/PauseFuelTransactionWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PauseFuelTransactionWorkflow", request)
            log.atInfo().addKeyValue("operation", "PauseFuelTransactionWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransaction.fuelTransactionId).log("Recorded audit detail")
            var reply0 = DelegateFuelCardApprovalResponse.getDefaultInstance()
            request.fuelTransaction.measurementsList.asFlow().map { item ->
                val outbound0 = delegateFuelCardApprovalRequest {
                    this.context = request.context
                    this.details = DelegateFuelCardApprovalRequestKt.details {
                        this.approver = userRef {
                            this.email = request.fuelTransaction.subject.email
                        }
                    }
                    this.fuelCard = fuelCard {
                        this.audit = auditInfo {
                            this.updatedAt = item.observedAt
                        }
                        this.contact = contactInfo {
                            this.name = request.details.notify.name
                        }
                    }
                }
                fuelFuelCardApprovalService.delegateFuelCardApproval(outbound0)
            }.collect { reply0 = it }
            val outbound1 = countDriversRequest {
                this.context = request.context
                this.filter = CountDriversRequestKt.filter {
                    this.labels.putAll(reply0.fuelCard.labelsMap)
                }
            }
            val reply1 = driversDriverQueryService.countDrivers(outbound1)
            val outbound2 = countStockItemsRequest {
                this.context = request.context
                this.filter = CountStockItemsRequestKt.filter {
                    this.createdAfter = reply0.fuelCard.audit.updatedAt
                    this.labels.putAll(reply0.fuelCard.labelsMap)
                    this.ownerId = request.fuelTransaction.subject.customerId
                }
            }
            val envelope2 = ProtoAny.pack(outbound2)
            val reply2 = inventoryStockItemQueryService.countStockItems(envelope2.unpack(CountStockItemsRequest::class.java))
            pauseFuelTransactionWorkflowResponse {
                this.fuelTransaction = fuelTransaction {
                    this.correlationId = reply1.result.correlationId
                    this.fuelTransactionId = request.fuelTransactionId
                    this.subject = request.fuelTransaction.subject
                }
                this.outcome = PauseFuelTransactionWorkflowResponseKt.outcome {
                    this.status = reply2.result.status
                }
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.status = "ok"
                }
            }
        }

    private data class ResumeFuelTransactionWorkflow1Command(
        val context: RequestContext,
        val fuelTransactionSubjectEmail: String
    ) {
        fun toRequest(): PullFuelTransactionsRequest {
            val command = this
            return pullFuelTransactionsRequest {
                this.context = command.context
                this.fuelTransaction = fuelTransaction {
                    this.subject = customerRef {
                        this.email = command.fuelTransactionSubjectEmail
                    }
                }
            }
        }
    }

    private suspend fun handleResumeFuelTransactionWorkflow(request: ResumeFuelTransactionWorkflowRequest): ResumeFuelTransactionWorkflowResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionWorkflowService/ResumeFuelTransactionWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResumeFuelTransactionWorkflow", request)
            log.atInfo().addKeyValue("operation", "ResumeFuelTransactionWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransaction.fuelTransactionId).log("Recorded audit detail")
            val outbound0 = simulateFuelCardRequest {
                this.context = request.context
                this.fuelCard = fuelCard {
                    this.contact = contactInfo {
                        this.email = request.details.notify.email
                        this.phone = request.details.notify.phone
                        this.preferredChannel = request.details.notify.preferredChannel
                    }
                    this.holder = driverRef {
                        this.displayName = request.fuelTransaction.subject.displayName
                    }
                }
            }
            val reply0 = fuelFuelCardCalculationService.simulateFuelCard(WireCopies.json(outbound0))
            val outbound1 = ResumeFuelTransactionWorkflow1Command(
                context = request.context,
                fuelTransactionSubjectEmail = reply0.fuelCard.holder.email
            ).toRequest()
            val reply1 = fuelFuelTransactionSyncService.pullFuelTransactions(outbound1)
            val outbound2 = scheduleFuelCardExportRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.details = ScheduleFuelCardExportRequestKt.details {
                    this.filters.putAll(request.fuelTransaction.attributesMap)
                }
                this.fuelCardId = reply0.fuelCard.fuelCardId
                this.reason = request.reason
            }
            val command2: Submission<ScheduleFuelCardExportRequest> = Submission.Live(outbound2)
            val payload2 = when (val command = command2) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply2 = fuelFuelCardExportService.scheduleFuelCardExport(payload2)
            resumeFuelTransactionWorkflowResponse {
                this.fuelTransaction = fuelTransaction {
                    this.attributes.putAll(request.fuelTransaction.attributesMap)
                    this.fuelTransactionId = request.fuelTransactionId
                    this.occurredAt = reply1.fuelTransaction.occurredAt
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.message = reply0.result.message
                    this.referenceIds.addAll(reply2.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleSignalFuelTransactionWorkflow(request: SignalFuelTransactionWorkflowRequest): SignalFuelTransactionWorkflowResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionWorkflowService/SignalFuelTransactionWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SignalFuelTransactionWorkflow", request)
            log.atInfo().addKeyValue("operation", "SignalFuelTransactionWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransaction.fuelTransactionId).log("Recorded audit detail")
            coroutineScope {
                val channel = Channel<FuelTransactionMeasurement>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound0 = exportNotificationsRequest {
                            this.context = request.context
                            this.filter = ExportNotificationsRequestKt.filter {
                                this.createdAfter = item.observedAt
                                this.labels.putAll(request.details.parametersMap)
                                this.ownerId = request.fuelTransaction.subject.customerId
                            }
                        }
                        notificationsNotificationQueryService.exportNotifications(outbound0).collect { event ->
                            log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
                        }
                    }
                }
                try {
                    request.fuelTransaction.measurementsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            var reply1 = ListDriversResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.fuelTransaction.measurementsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound1 = listDriversRequest {
                                this.context = request.context
                                this.filter = ListDriversRequestKt.filter {
                                    this.createdAfter = item.observedAt
                                    this.labels.putAll(request.fuelTransaction.attributesMap)
                                    this.ownerId = request.fuelTransaction.subject.customerId
                                }
                            }
                            driversDriverService.listDrivers(outbound1)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply1 = it }
                }
            }
            val outbound2 = RecallYardMoveRequest.newBuilder().apply {
                setContext(request.context)
                setDetails(RecallYardMoveRequestKt.details {
                    this.approver = userRef {
                        this.displayName = request.fuelTransaction.subject.displayName
                    }
                })
                setYardMove(yardMove {
                    this.requester = contactInfo {
                        this.email = request.fuelTransaction.subject.email
                        this.preferredChannel = request.details.notify.preferredChannel
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply2 = outbound2.let { value -> yardsYardMoveApprovalService.recallYardMove(value) }
            signalFuelTransactionWorkflowResponse {
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                    this.kind = request.fuelTransaction.kind
                }
                this.outcome = SignalFuelTransactionWorkflowResponseKt.outcome {
                    this.nextSteps.addAll(reply2.outcome.nextStepsList)
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleStartFuelTransactionWorkflow(request: StartFuelTransactionWorkflowRequest): StartFuelTransactionWorkflowResponse =
        RpcBoundary.run("acme.fuel.v1.FuelTransactionWorkflowService/StartFuelTransactionWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StartFuelTransactionWorkflow", request)
            log.atInfo().addKeyValue("operation", "StartFuelTransactionWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelTransaction.fuelTransactionId).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                listYardWorkflowsRequest {
                    this.context = request.context
                    this.filter = ListYardWorkflowsRequestKt.filter {
                        this.createdAfter = request.fuelTransaction.occurredAt
                        this.ownerId = request.fuelTransaction.correlationId
                    }
                }
            }
            val reply0 = yardsYardWorkflowService.listYardWorkflows(outbound0)
            val outbound1 = cloneRouteRequest {
                this.context = request.context
                this.route = route {
                    this.counterparty = driverRef {
                        this.displayName = request.fuelTransaction.subject.displayName
                        this.email = request.details.notify.email
                    }
                }
            }
            val reply1 = ClientRegistry.lookup("acme.routing.v1.RouteService", RouteServiceGrpcKt.RouteServiceCoroutineStub::class.java).cloneRoute(outbound1)
            startFuelTransactionWorkflowResponse {
                this.fuelTransaction = fuelTransaction {
                    this.fuelTransactionId = request.fuelTransactionId
                    this.occurredAt = request.fuelTransaction.occurredAt
                    this.subject = customerRef {
                        this.email = reply1.route.counterparty.email
                    }
                }
                this.outcome = StartFuelTransactionWorkflowResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }
}
