package com.acme.transport.telematics.grpc

import com.acme.authorization.v1.AccessPolicyServiceGrpcKt
import com.acme.authorization.v1.CreateAccessPolicyRequest
import com.acme.authorization.v1.accessPolicy
import com.acme.authorization.v1.accessPolicyBlockAction
import com.acme.authorization.v1.createAccessPolicyRequest
import com.acme.careplans.v1.CarePlanQueryServiceGrpcKt
import com.acme.careplans.v1.CountCarePlansRequest
import com.acme.careplans.v1.CountCarePlansRequestKt
import com.acme.careplans.v1.CountCarePlansResponse
import com.acme.careplans.v1.countCarePlansRequest
import com.acme.checkout.v1.CheckoutSessionServiceGrpcKt
import com.acme.checkout.v1.UpdateCheckoutSessionRequest
import com.acme.checkout.v1.checkoutSession
import com.acme.checkout.v1.updateCheckoutSessionRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.CustomerSegmentCalculationServiceGrpcKt
import com.acme.customers.v1.CustomerServiceGrpcKt
import com.acme.customers.v1.EstimateCustomerSegmentRequest
import com.acme.customers.v1.EstimateCustomerSegmentResponse
import com.acme.customers.v1.ListCustomersRequestKt
import com.acme.customers.v1.ListCustomersResponse
import com.acme.customers.v1.SimulateCustomerSegmentRequest
import com.acme.customers.v1.customerSegment
import com.acme.customers.v1.estimateCustomerSegmentRequest
import com.acme.customers.v1.listCustomersRequest
import com.acme.customers.v1.simulateCustomerSegmentRequest
import com.acme.deliveries.v1.DeliveryExceptionWorkflowServiceGrpcKt
import com.acme.deliveries.v1.RetryDeliveryExceptionWorkflowRequest
import com.acme.deliveries.v1.RetryDeliveryExceptionWorkflowResponse
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.deliveryExceptionSla
import com.acme.deliveries.v1.retryDeliveryExceptionWorkflowRequest
import com.acme.drivers.v1.driverRef
import com.acme.fuel.v1.FuelPolicyImportServiceGrpcKt
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.validateFuelPolicyImportRequest
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.PartWorkflowServiceGrpcKt
import com.acme.maintenance.v1.PauseWorkOrderWorkflowRequestKt
import com.acme.maintenance.v1.ResumeWorkOrderWorkflowRequestKt
import com.acme.maintenance.v1.ResumeWorkOrderWorkflowResponse
import com.acme.maintenance.v1.WorkOrderWorkflowServiceGrpcKt
import com.acme.maintenance.v1.part
import com.acme.maintenance.v1.pauseWorkOrderWorkflowRequest
import com.acme.maintenance.v1.resumeWorkOrderWorkflowRequest
import com.acme.maintenance.v1.retryPartWorkflowRequest
import com.acme.maintenance.v1.workOrder
import com.acme.maintenance.v1.workOrderComment
import com.acme.telematics.v1.AdvanceTelemetryReadingWorkflowRequest
import com.acme.telematics.v1.AdvanceTelemetryReadingWorkflowResponse
import com.acme.telematics.v1.AdvanceTelemetryReadingWorkflowResponseKt
import com.acme.telematics.v1.EscalateTelemetryReadingRequest
import com.acme.telematics.v1.EscalateTelemetryReadingRequestKt
import com.acme.telematics.v1.EscalateTelemetryReadingResponse
import com.acme.telematics.v1.GeoFenceImportServiceGrpcKt
import com.acme.telematics.v1.ListTelemetryReadingWorkflowsRequest
import com.acme.telematics.v1.ListTelemetryReadingWorkflowsResponse
import com.acme.telematics.v1.PauseTelemetryReadingWorkflowRequest
import com.acme.telematics.v1.PauseTelemetryReadingWorkflowResponse
import com.acme.telematics.v1.PauseTelemetryReadingWorkflowResponseKt
import com.acme.telematics.v1.RejectTelemetryReadingRequestKt
import com.acme.telematics.v1.ResumeTelemetryReadingWorkflowRequest
import com.acme.telematics.v1.ResumeTelemetryReadingWorkflowResponse
import com.acme.telematics.v1.ResumeTelemetryReadingWorkflowResponseKt
import com.acme.telematics.v1.RetryTelemetryReadingWorkflowRequest
import com.acme.telematics.v1.RetryTelemetryReadingWorkflowResponse
import com.acme.telematics.v1.RetryTelemetryReadingWorkflowResponseKt
import com.acme.telematics.v1.StartTelemetryReadingWorkflowRequest
import com.acme.telematics.v1.StartTelemetryReadingWorkflowResponse
import com.acme.telematics.v1.StartTelemetryReadingWorkflowResponseKt
import com.acme.telematics.v1.TelemetryReadingApprovalServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingMeasurement
import com.acme.telematics.v1.TelemetryReadingWorkflowServiceGrpcKt
import com.acme.telematics.v1.ValidateGeoFenceImportRequestKt
import com.acme.telematics.v1.advanceTelemetryReadingWorkflowResponse
import com.acme.telematics.v1.escalateTelemetryReadingRequest
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.geoFenceDiscountAction
import com.acme.telematics.v1.geoFenceRef
import com.acme.telematics.v1.listTelemetryReadingWorkflowsResponse
import com.acme.telematics.v1.pauseTelemetryReadingWorkflowResponse
import com.acme.telematics.v1.rejectTelemetryReadingRequest
import com.acme.telematics.v1.resumeTelemetryReadingWorkflowResponse
import com.acme.telematics.v1.retryTelemetryReadingWorkflowResponse
import com.acme.telematics.v1.startTelemetryReadingWorkflowResponse
import com.acme.telematics.v1.telemetryReading
import com.acme.telematics.v1.validateGeoFenceImportRequest
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.transport.telematics.TelematicsRepository
import com.acme.treasury.v1.CashPositionCalculationServiceGrpcKt
import com.acme.treasury.v1.cashPosition
import com.acme.treasury.v1.cashPositionLimit
import com.acme.treasury.v1.simulateCashPositionRequest
import com.acme.venues.v1.SearchVenuesRequestKt
import com.acme.venues.v1.SearchVenuesResponse
import com.acme.venues.v1.VenueServiceGrpcKt
import com.acme.venues.v1.searchVenuesRequest
import io.grpc.Metadata
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

/** Runs the long-lived workflow that takes a telemetry reading from start to finish. */
class TelemetryReadingWorkflowEndpoint(
    private val store: TelematicsRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyService: AccessPolicyServiceGrpcKt.AccessPolicyServiceCoroutineStub,
    private val careplansCarePlanQueryService: CarePlanQueryServiceGrpcKt.CarePlanQueryServiceCoroutineStub,
    private val checkoutCheckoutSessionService: CheckoutSessionServiceGrpcKt.CheckoutSessionServiceCoroutineStub,
    private val customersCustomerSegmentCalculationService: CustomerSegmentCalculationServiceGrpcKt.CustomerSegmentCalculationServiceCoroutineStub,
    private val customersCustomerService: CustomerServiceGrpcKt.CustomerServiceCoroutineStub,
    private val deliveriesDeliveryExceptionWorkflowService: DeliveryExceptionWorkflowServiceGrpcKt.DeliveryExceptionWorkflowServiceCoroutineStub,
    private val fuelFuelPolicyImportService: FuelPolicyImportServiceGrpcKt.FuelPolicyImportServiceCoroutineStub,
    private val maintenancePartWorkflowService: PartWorkflowServiceGrpcKt.PartWorkflowServiceCoroutineStub,
    private val maintenanceWorkOrderWorkflowService: WorkOrderWorkflowServiceGrpcKt.WorkOrderWorkflowServiceCoroutineStub,
    private val telematicsGeoFenceImportService: GeoFenceImportServiceGrpcKt.GeoFenceImportServiceCoroutineStub,
    private val telematicsTelemetryReadingApprovalService: TelemetryReadingApprovalServiceGrpcKt.TelemetryReadingApprovalServiceCoroutineStub,
    private val treasuryCashPositionCalculationService: CashPositionCalculationServiceGrpcKt.CashPositionCalculationServiceCoroutineStub,
    private val venuesVenueService: VenueServiceGrpcKt.VenueServiceCoroutineStub,
) : TelemetryReadingWorkflowServiceGrpcKt.TelemetryReadingWorkflowServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(TelemetryReadingWorkflowEndpoint::class.java)

    private val invokePauseTelemetryReadingWorkflow1: suspend (CountCarePlansRequest) -> CountCarePlansResponse = { payload ->
        careplansCarePlanQueryService.countCarePlans(payload)
    }

    private val workerForRetryTelemetryReadingWorkflow1 = ReplyMailbox<RetryDeliveryExceptionWorkflowRequest, RetryDeliveryExceptionWorkflowResponse>(workers) { payload ->
        withTimeout(5_000) { deliveriesDeliveryExceptionWorkflowService.retryDeliveryExceptionWorkflow(payload) }
    }

    /** Advances a telemetry reading. */
    override suspend fun advanceTelemetryReadingWorkflow(request: AdvanceTelemetryReadingWorkflowRequest): AdvanceTelemetryReadingWorkflowResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingWorkflowService/AdvanceTelemetryReadingWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "AdvanceTelemetryReadingWorkflow", request)
            log.atInfo().addKeyValue("operation", "AdvanceTelemetryReadingWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReading.telemetryReadingId).log("Recorded audit detail")
            val outbound0 = pauseWorkOrderWorkflowRequest {
                this.context = request.context
                this.details = PauseWorkOrderWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.name = request.details.notify.name
                    }
                }
                this.workOrder = workOrder {
                    this.assignee = userRef {
                        this.displayName = request.telemetryReading.subject.displayName
                    }
                    this.requester = contactInfo {
                        this.email = request.details.notify.email
                        this.phone = request.details.notify.phone
                    }
                }
            }
            val reply0 = maintenanceWorkOrderWorkflowService.pauseWorkOrderWorkflow(WireCopies.json(outbound0))
            val outbound1 = AdvanceTelemetryReadingWorkflow1Command(
                context = request.context,
                customerSegmentOwnerDisplayName = request.telemetryReading.subject.displayName,
                customerSegmentOwnerUserId = reply0.workOrder.assignee.userId
            ).toRequest()
            val reply1 = customersCustomerSegmentCalculationService.simulateCustomerSegment(outbound1)
            advanceTelemetryReadingWorkflowResponse {
                this.outcome = AdvanceTelemetryReadingWorkflowResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.sequence = request.telemetryReading.sequence
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    /** Lists telemetry readings a page at a time. */
    override suspend fun listTelemetryReadingWorkflows(request: ListTelemetryReadingWorkflowsRequest): ListTelemetryReadingWorkflowsResponse {
        return handleListTelemetryReadingWorkflows(request)
    }

    /** Pauses a telemetry reading. */
    override suspend fun pauseTelemetryReadingWorkflow(request: PauseTelemetryReadingWorkflowRequest): PauseTelemetryReadingWorkflowResponse = handlePauseTelemetryReadingWorkflow(request)

    /** Resumes a telemetry reading. */
    override suspend fun resumeTelemetryReadingWorkflow(request: ResumeTelemetryReadingWorkflowRequest): ResumeTelemetryReadingWorkflowResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingWorkflowService/ResumeTelemetryReadingWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResumeTelemetryReadingWorkflow", request)
            log.atInfo().addKeyValue("operation", "ResumeTelemetryReadingWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReading.telemetryReadingId).log("Recorded audit detail")
            val outbound0 = escalateTelemetryReadingRequest {
                this.context = request.context
                this.details = EscalateTelemetryReadingRequestKt.details {
                    this.approver = userRef {
                        this.displayName = request.telemetryReading.subject.displayName
                        this.email = request.details.notify.email
                    }
                }
                this.telemetryReading = telemetryReading {
                    this.attributes.putAll(request.telemetryReading.attributesMap)
                    this.subject = geoFenceRef {
                        this.geoFenceId = request.telemetryReading.subject.geoFenceId
                    }
                }
            }
            val invoke0: suspend (EscalateTelemetryReadingRequest, Metadata) -> EscalateTelemetryReadingResponse = telematicsTelemetryReadingApprovalService::escalateTelemetryReading
            val reply0 = invoke0(outbound0, Metadata())
            val outbound1 = CreateAccessPolicyRequest.newBuilder().apply {
                setAccessPolicy(accessPolicy {
                    this.block = accessPolicyBlockAction {
                        this.reason = request.reason
                    }
                    this.owner = userRef {
                        this.displayName = request.telemetryReading.subject.displayName
                        this.email = request.details.notify.email
                    }
                    this.version = reply0.telemetryReading.subject.version
                })
                setContext(request.context)
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> authorizationAccessPolicyService.createAccessPolicy(value) }
            val outbound2 = RequestFactory.create {
                simulateCashPositionRequest {
                    this.cashPosition = cashPosition {
                        this.audit = auditInfo {
                            this.createdBy = reply1.accessPolicy.audit.createdBy
                        }
                        this.contact = contactInfo {
                            this.email = reply1.accessPolicy.owner.email
                            this.phone = request.details.notify.phone
                        }
                        this.limits.addAll(listOf(cashPositionLimit {
                            this.amount = reply1.accessPolicy.discount.amount
                        }))
                    }
                    this.context = request.context
                }
            }
            val reply2 = treasuryCashPositionCalculationService.simulateCashPosition(outbound2)
            val outbound3 = rejectTelemetryReadingRequest {
                this.context = request.context
                this.details = RejectTelemetryReadingRequestKt.details {
                    this.approver = userRef {
                        this.email = reply2.cashPosition.contact.email
                    }
                }
            }
            val reply3 = ClientRegistry.lookup("acme.telematics.v1.TelemetryReadingApprovalService", TelemetryReadingApprovalServiceGrpcKt.TelemetryReadingApprovalServiceCoroutineStub::class.java).rejectTelemetryReading(outbound3)
            resumeTelemetryReadingWorkflowResponse {
                this.outcome = ResumeTelemetryReadingWorkflowResponseKt.outcome {
                    this.referenceId = reply0.outcome.referenceId
                    this.status = reply2.result.status
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = reply2.result.status
                }
                this.telemetryReading = telemetryReading {
                    this.correlationId = reply1.result.correlationId
                    this.occurredAt = request.telemetryReading.occurredAt
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    /** Retrys a telemetry reading. */
    override suspend fun retryTelemetryReadingWorkflow(request: RetryTelemetryReadingWorkflowRequest): RetryTelemetryReadingWorkflowResponse {
        return handleRetryTelemetryReadingWorkflow(request)
    }

    /** Starts a telemetry reading. */
    override suspend fun startTelemetryReadingWorkflow(request: StartTelemetryReadingWorkflowRequest): StartTelemetryReadingWorkflowResponse = handleStartTelemetryReadingWorkflow(request)

    private data class AdvanceTelemetryReadingWorkflow1Command(
        val context: RequestContext,
        val customerSegmentOwnerDisplayName: String,
        val customerSegmentOwnerUserId: String
    ) {
        fun toRequest(): SimulateCustomerSegmentRequest {
            val command = this
            return simulateCustomerSegmentRequest {
                this.context = command.context
                this.customerSegment = customerSegment {
                    this.owner = userRef {
                        this.displayName = command.customerSegmentOwnerDisplayName
                        this.userId = command.customerSegmentOwnerUserId
                    }
                }
            }
        }
    }

    private suspend fun handleListTelemetryReadingWorkflows(request: ListTelemetryReadingWorkflowsRequest): ListTelemetryReadingWorkflowsResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingWorkflowService/ListTelemetryReadingWorkflows") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListTelemetryReadingWorkflows", request)
            log.atInfo().addKeyValue("operation", "ListTelemetryReadingWorkflows").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = updateCheckoutSessionRequest {
                this.checkoutSession = checkoutSession {
                    this.billingAddress = postalAddress {
                        this.region = request.filter.region
                    }
                    this.dueAt = request.filter.createdAfter
                }
                this.context = request.context
            }
            val command0: Submission<UpdateCheckoutSessionRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = checkoutCheckoutSessionService.updateCheckoutSession(payload0)
            listTelemetryReadingWorkflowsResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.telemetryReadings.addAll(listOf(telemetryReading {
                    this.correlationId = reply0.result.correlationId
                    this.subject = geoFenceRef {
                        this.displayName = reply0.checkoutSession.counterparty.displayName
                    }
                }))
            }
        }

    private suspend fun handlePauseTelemetryReadingWorkflow(request: PauseTelemetryReadingWorkflowRequest): PauseTelemetryReadingWorkflowResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingWorkflowService/PauseTelemetryReadingWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PauseTelemetryReadingWorkflow", request)
            log.atInfo().addKeyValue("operation", "PauseTelemetryReadingWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReading.telemetryReadingId).log("Recorded audit detail")
            val outbound0 = estimateCustomerSegmentRequest {
                this.context = request.context
                this.customerSegment = customerSegment {
                    this.owner = userRef {
                        this.displayName = request.telemetryReading.subject.displayName
                        this.email = request.details.notify.email
                    }
                    this.parameters.putAll(request.details.parametersMap)
                    this.version = request.telemetryReading.subject.version
                }
            }
            val adapter0 = OutboundPort<EstimateCustomerSegmentRequest, EstimateCustomerSegmentResponse> { payload ->
                customersCustomerSegmentCalculationService.estimateCustomerSegment(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = countCarePlansRequest {
                this.context = request.context
                this.filter = CountCarePlansRequestKt.filter {
                    this.createdAfter = reply0.customerSegment.audit.updatedAt
                    this.labels.putAll(reply0.result.detailsMap)
                    this.ownerId = request.telemetryReading.subject.geoFenceId
                }
            }
            val reply1 = invokePauseTelemetryReadingWorkflow1(outbound1)
            pauseTelemetryReadingWorkflowResponse {
                this.outcome = PauseTelemetryReadingWorkflowResponseKt.outcome {
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.correlationId = reply0.result.correlationId
                    this.sequence = request.telemetryReading.sequence
                    this.subject = geoFenceRef {
                        this.version = reply0.customerSegment.version
                    }
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    private suspend fun handleRetryTelemetryReadingWorkflow(request: RetryTelemetryReadingWorkflowRequest): RetryTelemetryReadingWorkflowResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingWorkflowService/RetryTelemetryReadingWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RetryTelemetryReadingWorkflow", request)
            log.atInfo().addKeyValue("operation", "RetryTelemetryReadingWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReading.telemetryReadingId).log("Recorded audit detail")
            var reply0 = ResumeWorkOrderWorkflowResponse.getDefaultInstance()
            coroutineScope {
                val channel = Channel<TelemetryReadingMeasurement>(16)
                val consumer = launch {
                    for (item in channel) {
                        val outbound0 = resumeWorkOrderWorkflowRequest {
                            this.context = request.context
                            this.details = ResumeWorkOrderWorkflowRequestKt.details {
                                this.notify = contactInfo {
                                    this.preferredChannel = request.details.notify.preferredChannel
                                }
                            }
                            this.workOrder = workOrder {
                                this.comments.addAll(listOf(workOrderComment {
                                    this.createdAt = item.observedAt
                                }))
                                this.customer = driverRef {
                                    this.email = request.details.notify.email
                                }
                                this.requester = contactInfo {
                                    this.phone = request.details.notify.phone
                                }
                            }
                        }
                        reply0 = maintenanceWorkOrderWorkflowService.resumeWorkOrderWorkflow(outbound0)
                    }
                }
                try {
                    request.telemetryReading.measurementsList.forEach { channel.send(it) }
                } finally {
                    channel.close()
                }
                consumer.join()
            }
            val outbound1 = retryDeliveryExceptionWorkflowRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.sla = deliveryExceptionSla {
                        this.policyId = reply0.workOrder.sla.policyId
                    }
                }
            }
            val reply1 = workerForRetryTelemetryReadingWorkflow1.submit(outbound1)
            var reply2 = SearchVenuesResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.telemetryReading.measurementsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound2 = searchVenuesRequest {
                                this.context = request.context
                                this.filter = SearchVenuesRequestKt.filter {
                                    this.createdAfter = item.observedAt
                                    this.labels.putAll(reply1.result.detailsMap)
                                }
                            }
                            venuesVenueService.searchVenues(outbound2)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply2 = it }
                }
            }
            retryTelemetryReadingWorkflowResponse {
                this.outcome = RetryTelemetryReadingWorkflowResponseKt.outcome {
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.sequence = request.telemetryReading.sequence
                    this.subject = geoFenceRef {
                        this.displayName = reply1.deliveryException.customer.displayName
                    }
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }

    private suspend fun handleStartTelemetryReadingWorkflow(request: StartTelemetryReadingWorkflowRequest): StartTelemetryReadingWorkflowResponse =
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingWorkflowService/StartTelemetryReadingWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StartTelemetryReadingWorkflow", request)
            log.atInfo().addKeyValue("operation", "StartTelemetryReadingWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.telemetryReading.telemetryReadingId).log("Recorded audit detail")
            val outbound0 = validateFuelPolicyImportRequest {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.owner = userRef {
                        this.displayName = request.telemetryReading.subject.displayName
                    }
                }
            }
            val reply0 = flowOf(outbound0).map { value -> fuelFuelPolicyImportService.validateFuelPolicyImport(value) }.single()
            val outbound1 = validateGeoFenceImportRequest {
                this.context = request.context
                this.details = ValidateGeoFenceImportRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.details.notify.email
                    }
                }
                this.geoFence = geoFence {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.fuelPolicy.audit.updatedBy
                    }
                    this.discount = geoFenceDiscountAction {
                        this.amount = reply0.fuelPolicy.discount.amount
                        this.percent = reply0.fuelPolicy.discount.percent
                    }
                }
            }
            val reply1 = withTimeout(5_000) { telematicsGeoFenceImportService.validateGeoFenceImport(outbound1) }
            var reply2 = ListCustomersResponse.getDefaultInstance()
            request.telemetryReading.measurementsList.asFlow().map { item ->
                val outbound2 = listCustomersRequest {
                    this.context = request.context
                    this.filter = ListCustomersRequestKt.filter {
                        this.createdAfter = item.observedAt
                        this.labels.putAll(request.details.parametersMap)
                        this.ownerId = reply0.outcome.referenceId
                    }
                }
                customersCustomerService.listCustomers(outbound2)
            }.collect { reply2 = it }
            val outbound3 = retryPartWorkflowRequest {
                this.context = request.context
                this.part = part {
                    this.audit = auditInfo {
                        this.etag = reply1.geoFence.audit.etag
                    }
                }
            }
            val reply3 = maintenancePartWorkflowService.retryPartWorkflow(outbound3)
            startTelemetryReadingWorkflowResponse {
                this.outcome = StartTelemetryReadingWorkflowResponseKt.outcome {
                    this.nextSteps.addAll(reply1.outcome.nextStepsList)
                    this.status = reply3.outcome.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.telemetryReading = telemetryReading {
                    this.correlationId = request.telemetryReading.correlationId
                    this.subject = geoFenceRef {
                        this.version = reply0.fuelPolicy.version
                    }
                    this.telemetryReadingId = request.telemetryReadingId
                }
            }
        }
}
