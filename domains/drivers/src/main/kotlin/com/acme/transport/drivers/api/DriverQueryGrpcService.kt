package com.acme.transport.drivers.api

import com.acme.authorization.v1.AccessPolicyQueryServiceGrpcKt
import com.acme.authorization.v1.ExportAccessPoliciesRequestKt
import com.acme.authorization.v1.exportAccessPoliciesRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.dispatch.v1.PreviewZoneRequest
import com.acme.dispatch.v1.PreviewZoneRequestKt
import com.acme.dispatch.v1.ZoneValidationServiceGrpcKt
import com.acme.dispatch.v1.copy as dispatchCopy
import com.acme.dispatch.v1.previewZoneRequest
import com.acme.dispatch.v1.zone
import com.acme.drivers.v1.BrowseDriversRequest
import com.acme.drivers.v1.BrowseDriversResponse
import com.acme.drivers.v1.CalculateDriverRequestKt
import com.acme.drivers.v1.CountDriversRequest
import com.acme.drivers.v1.CountDriversResponse
import com.acme.drivers.v1.DriverCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseValidationServiceGrpcKt
import com.acme.drivers.v1.DriverQueryServiceGrpcKt
import com.acme.drivers.v1.DriverValidationServiceGrpcKt
import com.acme.drivers.v1.ExplainDriverCalculationRequest
import com.acme.drivers.v1.ExplainDriverCalculationResponse
import com.acme.drivers.v1.ExportDriversRequest
import com.acme.drivers.v1.ExportDriversResponse
import com.acme.drivers.v1.FindDriversRequest
import com.acme.drivers.v1.FindDriversResponse
import com.acme.drivers.v1.GetDriverRatesRequest
import com.acme.drivers.v1.GetDriverRatesResponse
import com.acme.drivers.v1.LookupDriverRequest
import com.acme.drivers.v1.LookupDriverResponse
import com.acme.drivers.v1.PeekDriverRequest
import com.acme.drivers.v1.PeekDriverResponse
import com.acme.drivers.v1.PreviewDriverLicenseRequest
import com.acme.drivers.v1.ResolveDriversRequest
import com.acme.drivers.v1.ResolveDriversResponse
import com.acme.drivers.v1.SuggestDriversRequest
import com.acme.drivers.v1.SuggestDriversResponse
import com.acme.drivers.v1.SummarizeDriversRequest
import com.acme.drivers.v1.SummarizeDriversResponse
import com.acme.drivers.v1.browseDriversResponse
import com.acme.drivers.v1.calculateDriverRequest
import com.acme.drivers.v1.countDriversResponse
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverPreferences
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.explainDriverCalculationRequest
import com.acme.drivers.v1.exportDriversResponse
import com.acme.drivers.v1.findDriversResponse
import com.acme.drivers.v1.getDriverRatesRequest
import com.acme.drivers.v1.lookupDriverResponse
import com.acme.drivers.v1.peekDriverResponse
import com.acme.drivers.v1.previewDriverLicenseRequest
import com.acme.drivers.v1.quoteDriverRequest
import com.acme.drivers.v1.resolveDriversResponse
import com.acme.drivers.v1.scoreDriverRequest
import com.acme.drivers.v1.suggestDriversResponse
import com.acme.drivers.v1.summarizeDriversResponse
import com.acme.identity.v1.userRef
import com.acme.routing.v1.PreviewRoutePlanRequest
import com.acme.routing.v1.PreviewRoutePlanRequestKt
import com.acme.routing.v1.RoutePlanValidationServiceGrpcKt
import com.acme.routing.v1.ValidateRoutePlanRequestKt
import com.acme.routing.v1.previewRoutePlanRequest
import com.acme.routing.v1.routePlan
import com.acme.routing.v1.validateRoutePlanRequest
import com.acme.search.v1.LintSearchIndexesRequest
import com.acme.search.v1.LintSearchIndexesResponse
import com.acme.search.v1.SearchIndexValidationServiceGrpcKt
import com.acme.search.v1.lintSearchIndexesRequest
import com.acme.search.v1.searchIndex
import com.acme.transport.drivers.DriversRepository
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.slf4j.LoggerFactory

/** Answers read-only questions about drivers, for screens and reports. */
class DriverQueryGrpcService(
    private val store: DriversRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyQueryService: AccessPolicyQueryServiceGrpcKt.AccessPolicyQueryServiceCoroutineStub,
    private val dispatchZoneValidationService: ZoneValidationServiceGrpcKt.ZoneValidationServiceCoroutineStub,
    private val driversDriverCalculationService: DriverCalculationServiceGrpcKt.DriverCalculationServiceCoroutineStub,
    private val driversDriverLicenseValidationService: DriverLicenseValidationServiceGrpcKt.DriverLicenseValidationServiceCoroutineStub,
    private val driversDriverValidationService: DriverValidationServiceGrpcKt.DriverValidationServiceCoroutineStub,
    private val routingRoutePlanValidationService: RoutePlanValidationServiceGrpcKt.RoutePlanValidationServiceCoroutineStub,
    private val searchSearchIndexValidationService: SearchIndexValidationServiceGrpcKt.SearchIndexValidationServiceCoroutineStub,
) : DriverQueryServiceGrpcKt.DriverQueryServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DriverQueryGrpcService::class.java)

    private val invokeSummarizeDrivers2: suspend (ExplainDriverCalculationRequest) -> ExplainDriverCalculationResponse = { payload ->
        driversDriverCalculationService.explainDriverCalculation(payload)
    }

    /** Lists drivers a page at a time. */
    override suspend fun browseDrivers(request: BrowseDriversRequest): BrowseDriversResponse {
        return handleBrowseDrivers(request)
    }

    /** Counts drivers by status. */
    override suspend fun countDrivers(request: CountDriversRequest): CountDriversResponse = handleCountDrivers(request)

    /** Streams every matching driver. */
    override fun exportDrivers(request: ExportDriversRequest): Flow<ExportDriversResponse> = flow {
        emit(handleExportDrivers(request))
    }

    /** Lists drivers a page at a time. */
    override suspend fun findDrivers(request: FindDriversRequest): FindDriversResponse {
        return handleFindDrivers(request)
    }

    /** Returns a driver. */
    override suspend fun lookupDriver(request: LookupDriverRequest): LookupDriverResponse = handleLookupDriver(request)

    /** Returns a driver. */
    override suspend fun peekDriver(request: PeekDriverRequest): PeekDriverResponse =
        RpcBoundary.run("acme.drivers.v1.DriverQueryService/PeekDriver") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PeekDriver", request)
            log.atInfo().addKeyValue("operation", "PeekDriver").log("Handling transport request")
            val outbound0 = request.toExplainDriverCalculationFromPeekDriver0()
            val reply0 = driversDriverCalculationService.explainDriverCalculation(outbound0)
            val outbound1 = scoreDriverRequest {
                this.context = request.context
                this.driver = driver {
                    this.preferences = driverPreferences {
                        this.channels.addAll(reply0.driver.preferences.channelsList)
                    }
                }
            }
            val reply1 = driversDriverValidationService.scoreDriver(outbound1)
            peekDriverResponse {
                this.driver = driver {
                    this.audit = auditInfo {
                        this.updatedBy = reply1.driver.audit.updatedBy
                    }
                    this.contact = contactInfo {
                        this.email = reply0.driver.contact.email
                    }
                    this.driverId = request.driverId
                }
                this.result = operationResult {
                    this.message = reply0.outcome.message
                    this.status = "ok"
                }
            }
        }

    /** Returns several drivers at once. */
    override suspend fun resolveDrivers(request: ResolveDriversRequest): ResolveDriversResponse {
        return handleResolveDrivers(request)
    }

    /** Lists drivers a page at a time. */
    override suspend fun suggestDrivers(request: SuggestDriversRequest): SuggestDriversResponse = handleSuggestDrivers(request)

    /** Summarizes a driver. */
    override suspend fun summarizeDrivers(request: SummarizeDriversRequest): SummarizeDriversResponse =
        RpcBoundary.run("acme.drivers.v1.DriverQueryService/SummarizeDrivers") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SummarizeDrivers", request)
            log.atInfo().addKeyValue("operation", "SummarizeDrivers").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.format).log("Recorded audit detail")
            val outbound0 = previewDriverLicenseRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.driverLicense = driverLicense {
                    this.counterparty = driverRef {
                        this.driverId = request.driverId
                    }
                }
                this.reason = request.reason
            }
            val command0: Submission<PreviewDriverLicenseRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = driversDriverLicenseValidationService.previewDriverLicense(payload0)
            val outbound1 = lintSearchIndexesRequest {
                this.context = request.context
                this.searchIndex = searchIndex {
                    this.address = postalAddress {
                        this.recipient = reply0.driverLicense.billingAddress.recipient
                    }
                }
            }
            val adapter1 = OutboundPort<LintSearchIndexesRequest, LintSearchIndexesResponse> { payload ->
                searchSearchIndexValidationService.lintSearchIndexes(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            val outbound2 = explainDriverCalculationRequest {
                this.context = request.context
                this.driver = driver {
                    this.audit = auditInfo {
                        this.createdBy = reply0.driverLicense.audit.createdBy
                    }
                    this.primaryAddress = postalAddress {
                        this.locality = reply0.driverLicense.billingAddress.locality
                    }
                }
            }
            val reply2 = invokeSummarizeDrivers2(outbound2)
            val outbound3 = getDriverRatesRequest {
                this.context = request.context
                this.driverId = request.driverId
            }
            val invoke3: suspend (GetDriverRatesRequest, Metadata) -> GetDriverRatesResponse = driversDriverCalculationService::getDriverRates
            val reply3 = invoke3(outbound3, Metadata())
            summarizeDriversResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply3.result.detailsMap)
                    this.status = reply1.result.status
                }
            }
        }

    private suspend fun handleBrowseDrivers(request: BrowseDriversRequest): BrowseDriversResponse =
        RpcBoundary.run("acme.drivers.v1.DriverQueryService/BrowseDrivers") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BrowseDrivers", request)
            log.atInfo().addKeyValue("operation", "BrowseDrivers").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val configure0: ValidateRoutePlanRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.routePlan = routePlan {
                    this.billingAddress = postalAddress {
                        this.region = request.filter.region
                    }
                }
            }
            val outbound0 = validateRoutePlanRequest(configure0)
            val reply0 = routingRoutePlanValidationService.validateRoutePlan(outbound0)
            browseDriversResponse {
                this.drivers.addAll(listOf(driver {
                    this.primaryAddress = postalAddress {
                        this.countryCode = reply0.routePlan.billingAddress.countryCode
                    }
                }))
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleCountDrivers(request: CountDriversRequest): CountDriversResponse =
        RpcBoundary.run("acme.drivers.v1.DriverQueryService/CountDrivers") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CountDrivers", request)
            log.atInfo().addKeyValue("operation", "CountDrivers").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = exportAccessPoliciesRequest {
                this.context = request.context
                this.filter = ExportAccessPoliciesRequestKt.filter {
                    this.createdAfter = request.filter.createdAfter
                    this.ownerId = request.filter.ownerId
                    this.region = request.filter.region
                }
            }
            authorizationAccessPolicyQueryService.exportAccessPolicies(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            countDriversResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleExportDrivers(request: ExportDriversRequest): ExportDriversResponse =
        RpcBoundary.run("acme.drivers.v1.DriverQueryService/ExportDrivers") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExportDrivers", request)
            log.atInfo().addKeyValue("operation", "ExportDrivers").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = calculateDriverRequest {
                this.context = request.context
                this.details = CalculateDriverRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.filter.region
                    }
                }
                this.driver = driver {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                    this.relationshipManager = userRef {
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply0 = driversDriverCalculationService.calculateDriver(outbound0)
            exportDriversResponse {
                this.driver = driver {
                    this.primaryAddress = postalAddress {
                        this.recipient = reply0.driver.primaryAddress.recipient
                        this.region = reply0.driver.primaryAddress.region
                    }
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleFindDrivers(request: FindDriversRequest): FindDriversResponse =
        RpcBoundary.run("acme.drivers.v1.DriverQueryService/FindDrivers") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "FindDrivers", request)
            log.atInfo().addKeyValue("operation", "FindDrivers").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            findDriversResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleLookupDriver(request: LookupDriverRequest): LookupDriverResponse =
        RpcBoundary.run("acme.drivers.v1.DriverQueryService/LookupDriver") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LookupDriver", request)
            log.atInfo().addKeyValue("operation", "LookupDriver").log("Handling transport request")
            val outbound0 = previewZoneRequest {}.dispatchCopy {
                this.context = request.context
                this.details = PreviewZoneRequestKt.details {
                    this.locale = request.context.locale
                }
                this.zone = zone {
                    this.children.addAll(listOf(zone {
                        this.children.addAll(listOf(zone {
                            this.audit = auditInfo {
                                this.createdAt = request.context.deadline
                            }
                            this.manager = userRef {
                                this.email = request.context.actor.email
                            }
                        }))
                        this.manager = userRef {
                            this.userId = request.context.actor.userId
                        }
                    }))
                }
            }
            val reply0 = dispatchZoneValidationService.previewZone(outbound0)
            lookupDriverResponse {
                this.driver = driver {
                    this.driverId = request.driverId
                    this.primaryAddress = postalAddress {
                        this.recipient = reply0.zone.location.recipient
                    }
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
            }
        }

    private fun PeekDriverRequest.toExplainDriverCalculationFromPeekDriver0(): ExplainDriverCalculationRequest {
        val source = this
        return explainDriverCalculationRequest {
            this.context = source.context
            this.driver = driver {
                this.driverId = source.driverId
                this.relationshipManager = userRef {
                    this.email = source.context.actor.email
                    this.userId = source.context.actor.userId
                }
            }
        }
    }

    private suspend fun handleResolveDrivers(request: ResolveDriversRequest): ResolveDriversResponse =
        RpcBoundary.run("acme.drivers.v1.DriverQueryService/ResolveDrivers") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResolveDrivers", request)
            log.atInfo().addKeyValue("operation", "ResolveDrivers").log("Handling transport request")
            val outbound0 = previewRoutePlanRequest {
                this.context = request.context
                this.details = PreviewRoutePlanRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                    this.locale = request.context.locale
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = routingRoutePlanValidationService.previewRoutePlan(envelope0.unpack(PreviewRoutePlanRequest::class.java))
            resolveDriversResponse {
                this.drivers.addAll(listOf(driver {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.routePlan.audit.updatedBy
                    }
                }))
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }

    private data class SuggestDrivers1Command(
        val context: RequestContext,
        val zoneChildrenChildrenAuditCreatedBy: String,
        val zoneChildrenChildrenManagerUserId: String
    ) {
        fun toRequest(): PreviewZoneRequest {
            val command = this
            return previewZoneRequest {
                this.context = command.context
                this.zone = zone {
                    this.children.addAll(listOf(zone {
                        this.children.addAll(listOf(zone {
                            this.audit = auditInfo {
                                this.createdBy = command.zoneChildrenChildrenAuditCreatedBy
                            }
                            this.manager = userRef {
                                this.userId = command.zoneChildrenChildrenManagerUserId
                            }
                        }))
                    }))
                }
            }
        }
    }

    private suspend fun handleSuggestDrivers(request: SuggestDriversRequest): SuggestDriversResponse =
        RpcBoundary.run("acme.drivers.v1.DriverQueryService/SuggestDrivers") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SuggestDrivers", request)
            log.atInfo().addKeyValue("operation", "SuggestDrivers").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = quoteDriverRequest {
                this.context = request.context
                this.driver = driver {
                    this.primaryAddress = postalAddress {
                        this.region = request.filter.region
                    }
                }
            }
            val reply0 = driversDriverCalculationService.quoteDriver(WireCopies.json(outbound0))
            val outbound1 = SuggestDrivers1Command(
                context = request.context,
                zoneChildrenChildrenAuditCreatedBy = reply0.driver.audit.createdBy,
                zoneChildrenChildrenManagerUserId = reply0.driver.relationshipManager.userId
            ).toRequest()
            val reply1 = dispatchZoneValidationService.previewZone(outbound1)
            suggestDriversResponse {
                this.drivers.addAll(listOf(driver {
                    this.audit = auditInfo {
                        this.createdAt = reply1.zone.audit.createdAt
                    }
                    this.contact = contactInfo {
                        this.phone = reply0.driver.contact.phone
                    }
                }))
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = reply0.result.status
                }
            }
        }
}
