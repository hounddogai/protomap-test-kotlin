package com.acme.transport.drivers.adapters

import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.PreviewAuditEventRequestKt
import com.acme.audit.v1.previewAuditEventRequest
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.drivers.v1.BatchCreateDriversRequest
import com.acme.drivers.v1.BatchCreateDriversResponse
import com.acme.drivers.v1.BatchGetDriversRequest
import com.acme.drivers.v1.BatchGetDriversResponse
import com.acme.drivers.v1.BrowseDriverLicensesRequestKt
import com.acme.drivers.v1.CalculateDriverLicenseRequestKt
import com.acme.drivers.v1.CloneDriverRequest
import com.acme.drivers.v1.CloneDriverResponse
import com.acme.drivers.v1.CreateDriverRequest
import com.acme.drivers.v1.CreateDriverResponse
import com.acme.drivers.v1.DriverLicenseCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseQueryServiceGrpcKt
import com.acme.drivers.v1.DriverQueryServiceGrpcKt
import com.acme.drivers.v1.DriverServiceGrpcKt
import com.acme.drivers.v1.GetDriverRequest
import com.acme.drivers.v1.GetDriverResponse
import com.acme.drivers.v1.ListDriversRequest
import com.acme.drivers.v1.ListDriversResponse
import com.acme.drivers.v1.QuoteDriverLicenseRequestKt
import com.acme.drivers.v1.UpdateDriverRequest
import com.acme.drivers.v1.UpdateDriverResponse
import com.acme.drivers.v1.WatchDriversRequest
import com.acme.drivers.v1.WatchDriversResponse
import com.acme.drivers.v1.batchCreateDriversResponse
import com.acme.drivers.v1.batchGetDriversResponse
import com.acme.drivers.v1.browseDriverLicensesRequest
import com.acme.drivers.v1.calculateDriverLicenseRequest
import com.acme.drivers.v1.cloneDriverResponse
import com.acme.drivers.v1.createDriverResponse
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.getDriverResponse
import com.acme.drivers.v1.listDriversResponse
import com.acme.drivers.v1.lookupDriverRequest
import com.acme.drivers.v1.quoteDriverLicenseRequest
import com.acme.drivers.v1.recalculateDriverLicensesRequest
import com.acme.drivers.v1.resolveDriversRequest
import com.acme.drivers.v1.updateDriverResponse
import com.acme.drivers.v1.watchDriversResponse
import com.acme.fuel.v1.ExplainFuelCardCalculationRequestKt
import com.acme.fuel.v1.FuelCardCalculationServiceGrpcKt
import com.acme.fuel.v1.SimulateFuelCardRequest
import com.acme.fuel.v1.SimulateFuelCardRequestKt
import com.acme.fuel.v1.explainFuelCardCalculationRequest
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.fuelCardCardFunding
import com.acme.fuel.v1.simulateFuelCardRequest
import com.acme.identity.v1.UserServiceGrpcKt
import com.acme.identity.v1.cloneUserRequest
import com.acme.identity.v1.user
import com.acme.identity.v1.userPreferences
import com.acme.identity.v1.userRef
import com.acme.metrics.v1.MetricSeriesServiceGrpcKt
import com.acme.metrics.v1.UpdateMetricSeriesRequest
import com.acme.metrics.v1.UpdateMetricSeriesResponse
import com.acme.metrics.v1.copy as metricsCopy
import com.acme.metrics.v1.metricSeries
import com.acme.metrics.v1.updateMetricSeriesRequest
import com.acme.routing.v1.BatchGetRoutesRequest
import com.acme.routing.v1.RouteServiceGrpcKt
import com.acme.routing.v1.batchGetRoutesRequest
import com.acme.telematics.v1.GeoFenceValidationServiceGrpcKt
import com.acme.telematics.v1.ValidateGeoFenceRequestKt
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.validateGeoFenceRequest
import com.acme.transport.drivers.DriversRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.ValidateYardMoveRequest
import com.acme.yards.v1.YardMoveValidationServiceGrpcKt
import com.acme.yards.v1.validateYardMoveRequest
import com.acme.yards.v1.yardMove
import com.google.protobuf.Any as ProtoAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Manages drivers: creates, reads, changes, and lists them. */
class DriverController(
    private val store: DriversRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventValidationService: AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub,
    private val driversDriverLicenseCalculationService: DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub,
    private val driversDriverLicenseQueryService: DriverLicenseQueryServiceGrpcKt.DriverLicenseQueryServiceCoroutineStub,
    private val driversDriverQueryService: DriverQueryServiceGrpcKt.DriverQueryServiceCoroutineStub,
    private val fuelFuelCardCalculationService: FuelCardCalculationServiceGrpcKt.FuelCardCalculationServiceCoroutineStub,
    private val identityUserService: UserServiceGrpcKt.UserServiceCoroutineStub,
    private val metricsMetricSeriesService: MetricSeriesServiceGrpcKt.MetricSeriesServiceCoroutineStub,
    private val routingRouteService: RouteServiceGrpcKt.RouteServiceCoroutineStub,
    private val telematicsGeoFenceValidationService: GeoFenceValidationServiceGrpcKt.GeoFenceValidationServiceCoroutineStub,
    private val yardsYardMoveValidationService: YardMoveValidationServiceGrpcKt.YardMoveValidationServiceCoroutineStub,
) : DriverServiceGrpcKt.DriverServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DriverController::class.java)

    private val workerForBatchGetDrivers0 = ReplyMailbox<UpdateMetricSeriesRequest, UpdateMetricSeriesResponse>(workers) { payload ->
        withTimeout(5_000) { metricsMetricSeriesService.updateMetricSeries(payload) }
    }

    private val clientForCloneDriver0 by lazy { driversDriverLicenseCalculationService }

    /** Takes a stream of drivers and answers once at the end. */
    override suspend fun batchCreateDrivers(requests: Flow<BatchCreateDriversRequest>): BatchCreateDriversResponse {
        var answer = BatchCreateDriversResponse.getDefaultInstance()
        requests.map { request -> handleBatchCreateDrivers(request) }.collect { answer = it }
        return answer
    }

    /** Returns several drivers at once. */
    override suspend fun batchGetDrivers(request: BatchGetDriversRequest): BatchGetDriversResponse =
        RpcBoundary.run("acme.drivers.v1.DriverService/BatchGetDrivers") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BatchGetDrivers", request)
            log.atInfo().addKeyValue("operation", "BatchGetDrivers").log("Handling transport request")
            val outbound0 = updateMetricSeriesRequest {
                this.context = request.context
                this.metricSeries = metricSeries {
                    this.owner = userRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = workerForBatchGetDrivers0.submit(outbound0)
            batchGetDriversResponse {
                this.drivers.addAll(listOf(driver {
                    this.audit = auditInfo {
                        this.etag = reply0.metricSeries.audit.etag
                    }
                }))
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Creates a driver. */
    override suspend fun cloneDriver(request: CloneDriverRequest): CloneDriverResponse {
        return handleCloneDriver(request)
    }

    /** Creates a driver. */
    override suspend fun createDriver(request: CreateDriverRequest): CreateDriverResponse = handleCreateDriver(request)

    /** Returns a driver. */
    override suspend fun getDriver(request: GetDriverRequest): GetDriverResponse =
        RpcBoundary.run("acme.drivers.v1.DriverService/GetDriver") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetDriver", request)
            log.atInfo().addKeyValue("operation", "GetDriver").log("Handling transport request")
            val outbound0 = updateMetricSeriesRequest {}.metricsCopy {
                this.context = request.context
                this.metricSeries = metricSeries {
                    this.audit = auditInfo {
                        this.updatedAt = request.context.deadline
                    }
                    this.owner = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply0 = metricsMetricSeriesService.updateMetricSeries(outbound0)
            getDriverResponse {
                this.driver = driver {
                    this.driverId = request.driverId
                    this.primaryAddress = postalAddress {
                        this.lines.addAll(reply0.metricSeries.address.linesList)
                    }
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Lists drivers a page at a time. */
    override suspend fun listDrivers(request: ListDriversRequest): ListDriversResponse {
        return handleListDrivers(request)
    }

    /** Changes a driver. */
    override suspend fun updateDriver(request: UpdateDriverRequest): UpdateDriverResponse = handleUpdateDriver(request)

    /** Streams the changes of drivers as they happen. */
    override fun watchDrivers(request: WatchDriversRequest): Flow<WatchDriversResponse> = flow {
        emit(handleWatchDrivers(request))
    }

    private suspend fun handleBatchCreateDrivers(request: BatchCreateDriversRequest): BatchCreateDriversResponse =
        RpcBoundary.run("acme.drivers.v1.DriverService/BatchCreateDrivers") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BatchCreateDrivers", request)
            log.atInfo().addKeyValue("operation", "BatchCreateDrivers").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.header.source).log("Recorded audit detail")
            val outbound0 = SimulateFuelCardRequest.newBuilder().apply {
                setContext(request.context)
                setDetails(SimulateFuelCardRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(request.record.primaryAddress.linesList)
                        this.locality = request.record.primaryAddress.locality
                    }
                })
                setFuelCard(fuelCard {
                    this.contact = contactInfo {
                        this.email = request.record.contact.email
                    }
                    this.holder = driverRef {
                        this.displayName = request.record.relationshipManager.displayName
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> fuelFuelCardCalculationService.simulateFuelCard(value) }
            val outbound1 = RequestFactory.create {
                browseDriverLicensesRequest {
                    this.context = request.context
                    this.filter = BrowseDriverLicensesRequestKt.filter {
                        this.createdAfter = reply0.fuelCard.audit.updatedAt
                        this.labels.putAll(reply0.fuelCard.labelsMap)
                        this.region = request.record.primaryAddress.region
                    }
                }
            }
            val reply1 = driversDriverLicenseQueryService.browseDriverLicenses(outbound1)
            val outbound2 = previewAuditEventRequest {
                this.context = request.context
                this.details = PreviewAuditEventRequestKt.details {
                    this.contact = contactInfo {
                        this.phone = request.record.contact.phone
                    }
                }
            }
            val reply2 = ClientRegistry.lookup("acme.audit.v1.AuditEventValidationService", AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub::class.java).previewAuditEvent(outbound2)
            val outbound3 = recalculateDriverLicensesRequest {
                this.context = request.context
                this.driverLicense = driverLicense {
                    this.counterparty = driverRef {
                        this.email = request.record.relationshipManager.email
                    }
                }
            }
            val reply3 = driversDriverLicenseCalculationService.recalculateDriverLicenses(WireCopies.reflective(outbound3))
            batchCreateDriversResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply3.result.detailsMap)
                    this.message = reply0.result.message
                    this.status = reply1.result.status
                }
            }
        }

    private suspend fun handleCloneDriver(request: CloneDriverRequest): CloneDriverResponse =
        RpcBoundary.run("acme.drivers.v1.DriverService/CloneDriver") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CloneDriver", request)
            log.atInfo().addKeyValue("operation", "CloneDriver").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driver.contact.email).log("Recorded audit detail")
            val outbound0 = calculateDriverLicenseRequest {
                this.context = request.context
                this.details = CalculateDriverLicenseRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.driver.primaryAddress.region
                    }
                }
                this.driverLicense = driverLicense {
                    this.billingAddress = postalAddress {
                        this.lines.addAll(request.driver.primaryAddress.linesList)
                    }
                    this.counterparty = driverRef {
                        this.displayName = request.driver.relationshipManager.displayName
                        this.email = request.driver.relationshipManager.email
                    }
                }
            }
            val reply0 = clientForCloneDriver0.calculateDriverLicense(outbound0)
            val outbound1 = lookupDriverRequest {
                this.context = request.context
                this.driverId = request.driver.driverId
            }
            val reply1 = flowOf(outbound1).map { value -> driversDriverQueryService.lookupDriver(value) }.single()
            val outbound2 = simulateFuelCardRequest {
                this.context = request.context
                this.details = SimulateFuelCardRequestKt.details {
                    this.destination = postalAddress {
                        this.lines.addAll(reply0.driverLicense.billingAddress.linesList)
                        this.postalCode = request.driver.primaryAddress.postalCode
                    }
                }
                this.fuelCard = fuelCard {
                    this.card = fuelCardCardFunding {
                        this.holderName = reply0.driverLicense.card.holderName
                    }
                }
            }
            val reply2 = withTimeout(5_000) { fuelFuelCardCalculationService.simulateFuelCard(outbound2) }
            cloneDriverResponse {
                this.driver = driver {
                    this.attributes.putAll(request.driver.attributesMap)
                    this.contact = contactInfo {
                        this.name = request.driver.name
                        this.phone = reply1.driver.contact.phone
                    }
                    this.primaryAddress = postalAddress {
                        this.postalCode = reply0.driverLicense.billingAddress.postalCode
                    }
                    this.relationshipManager = reply1.driver.relationshipManager
                }
                this.result = operationResult {
                    this.message = reply2.result.message
                    this.status = reply2.result.status
                }
            }
        }

    private suspend fun handleCreateDriver(request: CreateDriverRequest): CreateDriverResponse =
        RpcBoundary.run("acme.drivers.v1.DriverService/CreateDriver") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CreateDriver", request)
            log.atInfo().addKeyValue("operation", "CreateDriver").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driver.driverId).log("Recorded audit detail")
            val configure0: QuoteDriverLicenseRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.details = QuoteDriverLicenseRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = request.driver.primaryAddress.countryCode
                        this.recipient = request.driver.primaryAddress.recipient
                    }
                }
                this.driverLicense = driverLicense {
                    this.audit = auditInfo {
                        this.etag = request.driver.audit.etag
                    }
                    this.billingAddress = postalAddress {
                        this.locality = request.driver.primaryAddress.locality
                    }
                }
            }
            val outbound0 = quoteDriverLicenseRequest(configure0)
            val reply0 = driversDriverLicenseCalculationService.quoteDriverLicense(outbound0)
            val outbound1 = resolveDriversRequest {
                this.context = request.context
            }
            val reply1 = driversDriverQueryService.resolveDrivers(outbound1)
            val outbound2 = cloneUserRequest {
                this.context = request.context
                this.user = user {
                    this.preferences = userPreferences {
                        this.channels.addAll(request.driver.preferences.channelsList)
                    }
                    this.primaryAddress = postalAddress {
                        this.postalCode = reply0.driverLicense.billingAddress.postalCode
                    }
                    this.relationshipManager = userRef {
                        this.email = request.driver.contact.email
                    }
                }
            }
            val reply2 = identityUserService.cloneUser(outbound2)
            createDriverResponse {
                this.driver = driver {
                    this.contact = contactInfo {
                        this.name = request.driver.name
                    }
                    this.primaryAddress = postalAddress {
                        this.lines.addAll(reply0.driverLicense.billingAddress.linesList)
                    }
                    this.relationshipManager = userRef {
                        this.email = reply2.user.relationshipManager.email
                        this.userId = reply2.user.relationshipManager.userId
                    }
                    this.status = request.driver.status
                }
                this.result = operationResult {
                    this.status = reply1.result.status
                }
            }
        }

    private fun ListDriversRequest.toBatchGetRoutesFromListDrivers0(): BatchGetRoutesRequest {
        val source = this
        return batchGetRoutesRequest {
            this.context = source.context
        }
    }

    private suspend fun handleListDrivers(request: ListDriversRequest): ListDriversResponse =
        RpcBoundary.run("acme.drivers.v1.DriverService/ListDrivers") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListDrivers", request)
            log.atInfo().addKeyValue("operation", "ListDrivers").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = request.toBatchGetRoutesFromListDrivers0()
            val reply0 = routingRouteService.batchGetRoutes(outbound0)
            listDriversResponse {
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleUpdateDriver(request: UpdateDriverRequest): UpdateDriverResponse =
        RpcBoundary.run("acme.drivers.v1.DriverService/UpdateDriver") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "UpdateDriver", request)
            log.atInfo().addKeyValue("operation", "UpdateDriver").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driver.driverId).log("Recorded audit detail")
            val outbound0 = validateGeoFenceRequest {
                this.context = request.context
                this.details = ValidateGeoFenceRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.driver.contact.email
                        this.name = request.driver.name
                    }
                }
                this.geoFence = geoFence {
                    this.owner = userRef {
                        this.email = request.driver.relationshipManager.email
                    }
                }
            }
            val reply0 = telematicsGeoFenceValidationService.validateGeoFence(outbound0)
            updateDriverResponse {
                this.driver = driver {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.geoFence.audit.updatedBy
                    }
                    this.contact = request.driver.contact
                    this.primaryAddress = request.driver.primaryAddress
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleWatchDrivers(request: WatchDriversRequest): WatchDriversResponse =
        RpcBoundary.run("acme.drivers.v1.DriverService/WatchDrivers") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "WatchDrivers", request)
            log.atInfo().addKeyValue("operation", "WatchDrivers").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = validateYardMoveRequest {
                this.context = request.context
                this.yardMove = yardMove {
                    this.requester = contactInfo {
                        this.email = request.subscriber.email
                        this.name = request.subscriber.name
                    }
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = yardsYardMoveValidationService.validateYardMove(envelope0.unpack(ValidateYardMoveRequest::class.java))
            val outbound1 = explainFuelCardCalculationRequest {
                this.context = request.context
                this.details = ExplainFuelCardCalculationRequestKt.details {
                    this.contact = contactInfo {
                        this.email = reply0.yardMove.requester.email
                    }
                }
            }
            val reply1 = fuelFuelCardCalculationService.explainFuelCardCalculation(WireCopies.json(outbound1))
            watchDriversResponse {
                this.driver = driver {
                    this.audit = auditInfo {
                        this.createdBy = reply1.fuelCard.audit.createdBy
                    }
                    this.contact = contactInfo {
                        this.email = reply0.yardMove.assignee.email
                        this.preferredChannel = request.subscriber.preferredChannel
                    }
                    this.name = request.subscriber.name
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = "ok"
                }
            }
        }
}
