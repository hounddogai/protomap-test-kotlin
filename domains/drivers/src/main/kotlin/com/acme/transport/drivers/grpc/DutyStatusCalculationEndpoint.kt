package com.acme.transport.drivers.grpc

import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.configuration.v1.ConfigSetCalculationServiceGrpcKt
import com.acme.configuration.v1.SimulateConfigSetRequest
import com.acme.configuration.v1.SimulateConfigSetResponse
import com.acme.configuration.v1.configSet
import com.acme.configuration.v1.simulateConfigSetRequest
import com.acme.dispatch.v1.DispatchOrderQueryServiceGrpcKt
import com.acme.dispatch.v1.resolveDispatchOrdersRequest
import com.acme.drivers.v1.CalculateDutyStatusRequest
import com.acme.drivers.v1.CalculateDutyStatusResponse
import com.acme.drivers.v1.CompareDutyStatusOptionsRequest
import com.acme.drivers.v1.CompareDutyStatusOptionsResponse
import com.acme.drivers.v1.DriverCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverValidationServiceGrpcKt
import com.acme.drivers.v1.DutyStatusCalculationServiceGrpcKt
import com.acme.drivers.v1.EstimateDriverRequest
import com.acme.drivers.v1.EstimateDriverRequestKt
import com.acme.drivers.v1.EstimateDriverResponse
import com.acme.drivers.v1.EstimateDutyStatusRequest
import com.acme.drivers.v1.EstimateDutyStatusResponse
import com.acme.drivers.v1.EstimateDutyStatusResponseKt
import com.acme.drivers.v1.ExplainDriverDecisionRequest
import com.acme.drivers.v1.ExplainDutyStatusCalculationRequest
import com.acme.drivers.v1.ExplainDutyStatusCalculationResponse
import com.acme.drivers.v1.GetDriverLicenseRatesRequest
import com.acme.drivers.v1.GetDriverLicenseRatesResponse
import com.acme.drivers.v1.QuoteDriverLicenseRequestKt
import com.acme.drivers.v1.QuoteDutyStatusRequest
import com.acme.drivers.v1.QuoteDutyStatusResponse
import com.acme.drivers.v1.QuoteDutyStatusResponseKt
import com.acme.drivers.v1.RecalculateDutyStatusesRequest
import com.acme.drivers.v1.RecalculateDutyStatusesResponse
import com.acme.drivers.v1.SimulateDutyStatusRequest
import com.acme.drivers.v1.SimulateDutyStatusResponse
import com.acme.drivers.v1.calculateDutyStatusResponse
import com.acme.drivers.v1.compareDriverLicenseOptionsRequest
import com.acme.drivers.v1.compareDutyStatusOptionsResponse
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverLicenseRef
import com.acme.drivers.v1.driverNationalId
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.dutyStatus
import com.acme.drivers.v1.estimateDriverLicenseRequest
import com.acme.drivers.v1.estimateDriverRequest
import com.acme.drivers.v1.estimateDutyStatusResponse
import com.acme.drivers.v1.explainDriverDecisionRequest
import com.acme.drivers.v1.explainDutyStatusCalculationResponse
import com.acme.drivers.v1.getDriverLicenseRatesRequest
import com.acme.drivers.v1.quoteDriverLicenseRequest
import com.acme.drivers.v1.quoteDutyStatusResponse
import com.acme.drivers.v1.recalculateDutyStatusesResponse
import com.acme.drivers.v1.simulateDutyStatusResponse
import com.acme.fuel.v1.FuelPolicyValidationServiceGrpcKt
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.lintFuelPoliciesRequest
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.BatchGetWorkOrdersRequest
import com.acme.maintenance.v1.BatchGetWorkOrdersResponse
import com.acme.maintenance.v1.WorkOrderServiceGrpcKt
import com.acme.maintenance.v1.batchGetWorkOrdersRequest
import com.acme.notifications.v1.NotificationServiceGrpcKt
import com.acme.notifications.v1.createNotificationRequest
import com.acme.notifications.v1.notification
import com.acme.notifications.v1.notificationTemplateRef
import com.acme.transport.drivers.DriversRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.venues.v1.VenueServiceGrpcKt
import com.acme.venues.v1.createVenueRequest
import com.acme.venues.v1.venue
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Computes the figures of a duty status, such as totals, taxes, and estimates. */
class DutyStatusCalculationEndpoint(
    private val store: DriversRepository,
    private val workers: CoroutineScope,
    private val configurationConfigSetCalculationService: ConfigSetCalculationServiceGrpcKt.ConfigSetCalculationServiceCoroutineStub,
    private val dispatchDispatchOrderQueryService: DispatchOrderQueryServiceGrpcKt.DispatchOrderQueryServiceCoroutineStub,
    private val driversDriverCalculationService: DriverCalculationServiceGrpcKt.DriverCalculationServiceCoroutineStub,
    private val driversDriverLicenseCalculationService: DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub,
    private val driversDriverValidationService: DriverValidationServiceGrpcKt.DriverValidationServiceCoroutineStub,
    private val fuelFuelPolicyValidationService: FuelPolicyValidationServiceGrpcKt.FuelPolicyValidationServiceCoroutineStub,
    private val maintenanceWorkOrderService: WorkOrderServiceGrpcKt.WorkOrderServiceCoroutineStub,
    private val notificationsNotificationService: NotificationServiceGrpcKt.NotificationServiceCoroutineStub,
    private val venuesVenueService: VenueServiceGrpcKt.VenueServiceCoroutineStub,
) : DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DutyStatusCalculationEndpoint::class.java)

    private val invokeCalculateDutyStatus1: suspend (GetDriverLicenseRatesRequest) -> GetDriverLicenseRatesResponse = { payload ->
        driversDriverLicenseCalculationService.getDriverLicenseRates(payload)
    }

    private val workerForExplainDutyStatusCalculation0 = ReplyMailbox<EstimateDriverRequest, EstimateDriverResponse>(workers) { payload ->
        withTimeout(5_000) { driversDriverCalculationService.estimateDriver(payload) }
    }

    private val clientForQuoteDutyStatus0 by lazy { driversDriverLicenseCalculationService }

    /** Calculates a duty status. */
    override suspend fun calculateDutyStatus(request: CalculateDutyStatusRequest): CalculateDutyStatusResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusCalculationService/CalculateDutyStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CalculateDutyStatus", request)
            log.atInfo().addKeyValue("operation", "CalculateDutyStatus").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dutyStatus.dutyStatusId).log("Recorded audit detail")
            val outbound0 = batchGetWorkOrdersRequest {
                this.context = request.context
            }
            val adapter0 = OutboundPort<BatchGetWorkOrdersRequest, BatchGetWorkOrdersResponse> { payload ->
                maintenanceWorkOrderService.batchGetWorkOrders(payload)
            }
            val port0 = DelegatingPort(adapter0)
            val reply0 = port0.execute(outbound0)
            val outbound1 = getDriverLicenseRatesRequest {
                this.context = request.context
                this.driverLicenseId = request.dutyStatus.subject.driverLicenseId
            }
            val reply1 = invokeCalculateDutyStatus1(outbound1)
            calculateDutyStatusResponse {
                this.dutyStatus = dutyStatus {
                    this.dutyStatusId = request.dutyStatusId
                    this.kind = request.dutyStatus.kind
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    /** Compares a duty status. */
    override suspend fun compareDutyStatusOptions(request: CompareDutyStatusOptionsRequest): CompareDutyStatusOptionsResponse {
        return handleCompareDutyStatusOptions(request)
    }

    /** Estimates a duty status. */
    override suspend fun estimateDutyStatus(request: EstimateDutyStatusRequest): EstimateDutyStatusResponse = handleEstimateDutyStatus(request)

    /** Explains a duty status. */
    override suspend fun explainDutyStatusCalculation(request: ExplainDutyStatusCalculationRequest): ExplainDutyStatusCalculationResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusCalculationService/ExplainDutyStatusCalculation") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainDutyStatusCalculation", request)
            log.atInfo().addKeyValue("operation", "ExplainDutyStatusCalculation").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dutyStatus.dutyStatusId).log("Recorded audit detail")
            val outbound0 = estimateDriverRequest {
                this.context = request.context
                this.driver = driver {
                    this.contact = contactInfo {
                        this.phone = request.details.contact.phone
                    }
                    this.relationshipManager = userRef {
                        this.displayName = request.dutyStatus.subject.displayName
                    }
                }
            }
            val reply0 = workerForExplainDutyStatusCalculation0.submit(outbound0)
            explainDutyStatusCalculationResponse {
                this.dutyStatus = dutyStatus {
                    this.attributes.putAll(reply0.driver.attributesMap)
                    this.dutyStatusId = request.dutyStatusId
                    this.sequence = request.dutyStatus.sequence
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Quotes a duty status. */
    override suspend fun quoteDutyStatus(request: QuoteDutyStatusRequest): QuoteDutyStatusResponse {
        return handleQuoteDutyStatus(request)
    }

    /** Recalculates a duty status. */
    override suspend fun recalculateDutyStatuses(request: RecalculateDutyStatusesRequest): RecalculateDutyStatusesResponse = handleRecalculateDutyStatuses(request)

    /** Simulates a duty status. */
    override suspend fun simulateDutyStatus(request: SimulateDutyStatusRequest): SimulateDutyStatusResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusCalculationService/SimulateDutyStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SimulateDutyStatus", request)
            log.atInfo().addKeyValue("operation", "SimulateDutyStatus").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dutyStatus.dutyStatusId).log("Recorded audit detail")
            val outbound0 = compareDriverLicenseOptionsRequest {
                this.context = request.context
                this.driverLicense = driverLicense {
                    this.billingAddress = postalAddress {
                        this.postalCode = request.details.destination.postalCode
                    }
                }
            }
            val reply0 = driversDriverLicenseCalculationService.compareDriverLicenseOptions(outbound0)
            simulateDutyStatusResponse {
                this.dutyStatus = dutyStatus {
                    this.attributes.putAll(request.dutyStatus.attributesMap)
                    this.dutyStatusId = request.dutyStatusId
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleCompareDutyStatusOptions(request: CompareDutyStatusOptionsRequest): CompareDutyStatusOptionsResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusCalculationService/CompareDutyStatusOptions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CompareDutyStatusOptions", request)
            log.atInfo().addKeyValue("operation", "CompareDutyStatusOptions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dutyStatus.dutyStatusId).log("Recorded audit detail")
            val outbound0 = simulateConfigSetRequest {
                this.configSet = configSet {
                    this.children.addAll(listOf(configSet {
                        this.children.addAll(listOf(configSet {
                            this.location = postalAddress {
                                this.lines.addAll(request.details.destination.linesList)
                                this.locality = request.details.destination.locality
                                this.postalCode = request.details.destination.postalCode
                            }
                        }))
                    }))
                }
                this.context = request.context
            }
            val invoke0: suspend (SimulateConfigSetRequest, Metadata) -> SimulateConfigSetResponse = configurationConfigSetCalculationService::simulateConfigSet
            val reply0 = invoke0(outbound0, Metadata())
            compareDutyStatusOptionsResponse {
                this.dutyStatus = dutyStatus {
                    this.attributes.putAll(reply0.configSet.attributesMap)
                    this.dutyStatusId = request.dutyStatusId
                    this.source = request.dutyStatus.source
                    this.subject = driverLicenseRef {
                        this.displayName = reply0.configSet.manager.displayName
                    }
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleEstimateDutyStatus(request: EstimateDutyStatusRequest): EstimateDutyStatusResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusCalculationService/EstimateDutyStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EstimateDutyStatus", request)
            log.atInfo().addKeyValue("operation", "EstimateDutyStatus").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dutyStatus.dutyStatusId).log("Recorded audit detail")
            val outbound0 = ExplainDriverDecisionRequest.newBuilder().apply {
                setContext(request.context)
                setDriver(driver {
                    this.nationalId = driverNationalId {
                        this.number = request.dutyStatus.subject.number
                    }
                    this.primaryAddress = postalAddress {
                        this.recipient = request.details.destination.recipient
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> driversDriverValidationService.explainDriverDecision(value) }
            val outbound1 = RequestFactory.create {
                createNotificationRequest {
                    this.context = request.context
                    this.notification = notification {
                        this.assignee = userRef {
                            this.email = reply0.driver.contact.email
                        }
                        this.customer = notificationTemplateRef {
                            this.number = reply0.driver.nationalId.number
                        }
                    }
                }
            }
            val reply1 = notificationsNotificationService.createNotification(outbound1)
            val outbound2 = getDriverLicenseRatesRequest {
                this.context = request.context
                this.driverLicenseId = request.dutyStatus.subject.driverLicenseId
            }
            val reply2 = ClientRegistry.lookup("acme.drivers.v1.DriverLicenseCalculationService", DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub::class.java).getDriverLicenseRates(outbound2)
            val outbound3 = createVenueRequest {
                this.context = request.context
                this.venue = venue {
                    this.audit = auditInfo {
                        this.createdBy = reply2.driverLicense.audit.createdBy
                    }
                    this.owner = userRef {
                        this.displayName = reply2.driverLicense.counterparty.displayName
                    }
                }
            }
            val reply3 = venuesVenueService.createVenue(WireCopies.reflective(outbound3))
            estimateDutyStatusResponse {
                this.breakdown = EstimateDutyStatusResponseKt.breakdown {
                    this.total = reply2.driverLicense.total
                }
                this.dutyStatus = dutyStatus {
                    this.correlationId = reply1.result.correlationId
                    this.dutyStatusId = request.dutyStatusId
                    this.source = request.dutyStatus.source
                    this.subject = driverLicenseRef {
                        this.displayName = reply1.notification.customer.displayName
                    }
                }
                this.result = operationResult {
                    this.message = reply0.outcome.message
                    this.status = reply0.outcome.status
                }
            }
        }

    private suspend fun handleQuoteDutyStatus(request: QuoteDutyStatusRequest): QuoteDutyStatusResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusCalculationService/QuoteDutyStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "QuoteDutyStatus", request)
            log.atInfo().addKeyValue("operation", "QuoteDutyStatus").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dutyStatus.dutyStatusId).log("Recorded audit detail")
            val outbound0 = quoteDriverLicenseRequest {
                this.context = request.context
                this.details = QuoteDriverLicenseRequestKt.details {
                    this.destination = postalAddress {
                        this.locality = request.details.destination.locality
                        this.region = request.details.destination.region
                    }
                }
            }
            val reply0 = clientForQuoteDutyStatus0.quoteDriverLicense(outbound0)
            val outbound1 = resolveDispatchOrdersRequest {
                this.context = request.context
            }
            val reply1 = flowOf(outbound1).map { value -> dispatchDispatchOrderQueryService.resolveDispatchOrders(value) }.single()
            val outbound2 = lintFuelPoliciesRequest {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.audit = auditInfo {
                        this.createdBy = reply0.driverLicense.audit.createdBy
                        this.updatedBy = reply0.driverLicense.audit.updatedBy
                    }
                    this.owner = userRef {
                        this.email = reply0.driverLicense.counterparty.email
                    }
                }
            }
            val reply2 = withTimeout(5_000) { fuelFuelPolicyValidationService.lintFuelPolicies(outbound2) }
            val configure3: EstimateDriverRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.driver = driver {
                    this.contact = contactInfo {
                        this.email = reply0.driverLicense.counterparty.email
                    }
                    this.primaryAddress = postalAddress {
                        this.postalCode = request.details.destination.postalCode
                    }
                }
            }
            val outbound3 = estimateDriverRequest(configure3)
            val reply3 = driversDriverCalculationService.estimateDriver(outbound3)
            quoteDutyStatusResponse {
                this.breakdown = QuoteDutyStatusResponseKt.breakdown {
                    this.currencyCode = request.details.currencyCode
                    this.lines.addAll(listOf(QuoteDutyStatusResponseKt.BreakdownKt.line {
                        this.amount = reply2.fuelPolicy.discount.amount
                    }))
                }
                this.dutyStatus = dutyStatus {
                    this.correlationId = reply1.result.correlationId
                    this.dutyStatusId = request.dutyStatusId
                    this.subject = driverLicenseRef {
                        this.displayName = reply3.driver.relationshipManager.displayName
                    }
                }
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleRecalculateDutyStatuses(request: RecalculateDutyStatusesRequest): RecalculateDutyStatusesResponse =
        RpcBoundary.run("acme.drivers.v1.DutyStatusCalculationService/RecalculateDutyStatuses") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RecalculateDutyStatuses", request)
            log.atInfo().addKeyValue("operation", "RecalculateDutyStatuses").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.dutyStatus.dutyStatusId).log("Recorded audit detail")
            val outbound0 = estimateDriverLicenseRequest {
                this.context = request.context
                this.driverLicense = driverLicense {
                    this.counterparty = driverRef {
                        this.displayName = request.dutyStatus.subject.displayName
                    }
                }
            }
            val reply0 = driversDriverLicenseCalculationService.estimateDriverLicense(outbound0)
            recalculateDutyStatusesResponse {
                this.dutyStatus = dutyStatus {
                    this.dutyStatusId = request.dutyStatusId
                    this.source = request.dutyStatus.source
                    this.subject = driverLicenseRef {
                        this.displayName = reply0.driverLicense.counterparty.displayName
                    }
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }
}
