package com.acme.transport.drivers.api

import com.acme.appointments.v1.AppointmentQueryServiceGrpcKt
import com.acme.appointments.v1.ExportAppointmentsRequestKt
import com.acme.appointments.v1.FindAppointmentsRequest
import com.acme.appointments.v1.FindAppointmentsRequestKt
import com.acme.appointments.v1.exportAppointmentsRequest
import com.acme.appointments.v1.findAppointmentsRequest
import com.acme.audit.v1.AuditEventQueryServiceGrpcKt
import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.ResolveAuditEventsRequest
import com.acme.audit.v1.ScoreAuditEventRequestKt
import com.acme.audit.v1.auditEvent
import com.acme.audit.v1.auditTrailRef
import com.acme.audit.v1.resolveAuditEventsRequest
import com.acme.audit.v1.scoreAuditEventRequest
import com.acme.authorization.v1.AccessPolicyQueryServiceGrpcKt
import com.acme.authorization.v1.BrowseAccessPoliciesRequestKt
import com.acme.authorization.v1.browseAccessPoliciesRequest
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.DeliveryServiceGrpcKt
import com.acme.deliveries.v1.ListDeliveriesRequest
import com.acme.deliveries.v1.ListDeliveriesRequestKt
import com.acme.deliveries.v1.ListDeliveriesResponse
import com.acme.deliveries.v1.ProofOfDeliveryCalculationServiceGrpcKt
import com.acme.deliveries.v1.RecalculateProofOfDeliveriesRequest
import com.acme.deliveries.v1.RecalculateProofOfDeliveriesResponse
import com.acme.deliveries.v1.SimulateProofOfDeliveryRequestKt
import com.acme.deliveries.v1.listDeliveriesRequest
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.recalculateProofOfDeliveriesRequest
import com.acme.deliveries.v1.simulateProofOfDeliveryRequest
import com.acme.dispatch.v1.ZoneCalculationServiceGrpcKt
import com.acme.dispatch.v1.calculateZoneRequest
import com.acme.dispatch.v1.zone
import com.acme.drivers.v1.CalculateDriverRequest
import com.acme.drivers.v1.CalculateDriverResponse
import com.acme.drivers.v1.DriverCalculationServiceGrpcKt
import com.acme.drivers.v1.EstimateDriverRequest
import com.acme.drivers.v1.EstimateDriverResponse
import com.acme.drivers.v1.ExplainDriverCalculationRequest
import com.acme.drivers.v1.ExplainDriverCalculationResponse
import com.acme.drivers.v1.GetDriverRatesRequest
import com.acme.drivers.v1.GetDriverRatesResponse
import com.acme.drivers.v1.QuoteDriverRequest
import com.acme.drivers.v1.QuoteDriverResponse
import com.acme.drivers.v1.SimulateDriverRequest
import com.acme.drivers.v1.SimulateDriverResponse
import com.acme.drivers.v1.SimulateDriverResponseKt
import com.acme.drivers.v1.calculateDriverResponse
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.estimateDriverResponse
import com.acme.drivers.v1.explainDriverCalculationResponse
import com.acme.drivers.v1.getDriverRatesResponse
import com.acme.drivers.v1.quoteDriverResponse
import com.acme.drivers.v1.simulateDriverResponse
import com.acme.featureflags.v1.ExportFeatureFlagsRequestKt
import com.acme.featureflags.v1.FeatureFlagQueryServiceGrpcKt
import com.acme.featureflags.v1.exportFeatureFlagsRequest
import com.acme.identity.v1.UserServiceGrpcKt
import com.acme.identity.v1.cloneUserRequest
import com.acme.identity.v1.user
import com.acme.identity.v1.userPreferences
import com.acme.identity.v1.userRef
import com.acme.ledger.v1.LedgerAccountServiceGrpcKt
import com.acme.ledger.v1.watchLedgerAccountsRequest
import com.acme.notifications.v1.NotificationQueryServiceGrpcKt
import com.acme.notifications.v1.copy as notificationsCopy
import com.acme.notifications.v1.resolveNotificationsRequest
import com.acme.search.v1.SearchIndexQueryServiceGrpcKt
import com.acme.search.v1.SummarizeSearchIndexesRequestKt
import com.acme.search.v1.summarizeSearchIndexesRequest
import com.acme.sponsorships.v1.ExplainSponsorCalculationRequestKt
import com.acme.sponsorships.v1.SponsorCalculationServiceGrpcKt
import com.acme.sponsorships.v1.explainSponsorCalculationRequest
import com.acme.transport.drivers.DriversRepository
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Computes the figures of a driver, such as totals, taxes, and estimates. */
class DriverCalculationGrpcService(
    private val store: DriversRepository,
    private val workers: CoroutineScope,
    private val appointmentsAppointmentQueryService: AppointmentQueryServiceGrpcKt.AppointmentQueryServiceCoroutineStub,
    private val auditAuditEventQueryService: AuditEventQueryServiceGrpcKt.AuditEventQueryServiceCoroutineStub,
    private val auditAuditEventValidationService: AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub,
    private val authorizationAccessPolicyQueryService: AccessPolicyQueryServiceGrpcKt.AccessPolicyQueryServiceCoroutineStub,
    private val deliveriesDeliveryService: DeliveryServiceGrpcKt.DeliveryServiceCoroutineStub,
    private val deliveriesProofOfDeliveryCalculationService: ProofOfDeliveryCalculationServiceGrpcKt.ProofOfDeliveryCalculationServiceCoroutineStub,
    private val dispatchZoneCalculationService: ZoneCalculationServiceGrpcKt.ZoneCalculationServiceCoroutineStub,
    private val featureflagsFeatureFlagQueryService: FeatureFlagQueryServiceGrpcKt.FeatureFlagQueryServiceCoroutineStub,
    private val identityUserService: UserServiceGrpcKt.UserServiceCoroutineStub,
    private val ledgerLedgerAccountService: LedgerAccountServiceGrpcKt.LedgerAccountServiceCoroutineStub,
    private val notificationsNotificationQueryService: NotificationQueryServiceGrpcKt.NotificationQueryServiceCoroutineStub,
    private val searchSearchIndexQueryService: SearchIndexQueryServiceGrpcKt.SearchIndexQueryServiceCoroutineStub,
    private val sponsorshipsSponsorCalculationService: SponsorCalculationServiceGrpcKt.SponsorCalculationServiceCoroutineStub,
) : DriverCalculationServiceGrpcKt.DriverCalculationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DriverCalculationGrpcService::class.java)

    private val workerForEstimateDriver3 = ReplyMailbox<ListDeliveriesRequest, ListDeliveriesResponse>(workers) { payload ->
        withTimeout(5_000) { deliveriesDeliveryService.listDeliveries(payload) }
    }

    /** Calculates a driver. */
    override suspend fun calculateDriver(request: CalculateDriverRequest): CalculateDriverResponse {
        return handleCalculateDriver(request)
    }

    /** Estimates a driver. */
    override suspend fun estimateDriver(request: EstimateDriverRequest): EstimateDriverResponse = handleEstimateDriver(request)

    /** Explains a driver. */
    override suspend fun explainDriverCalculation(request: ExplainDriverCalculationRequest): ExplainDriverCalculationResponse =
        RpcBoundary.run("acme.drivers.v1.DriverCalculationService/ExplainDriverCalculation") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainDriverCalculation", request)
            log.atInfo().addKeyValue("operation", "ExplainDriverCalculation").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driver.driverId).log("Recorded audit detail")
            val outbound0 = exportFeatureFlagsRequest {
                this.context = request.context
                this.filter = ExportFeatureFlagsRequestKt.filter {
                    this.region = request.driver.primaryAddress.region
                }
            }
            featureflagsFeatureFlagQueryService.exportFeatureFlags(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = scoreAuditEventRequest {
                this.auditEvent = auditEvent {
                    this.subject = auditTrailRef {
                        this.displayName = request.driver.relationshipManager.displayName
                        this.number = request.driver.passport.number
                    }
                }
                this.context = request.context
                this.details = ScoreAuditEventRequestKt.details {
                    this.destination = postalAddress {
                        this.locality = request.driver.primaryAddress.locality
                        this.region = request.driver.primaryAddress.region
                    }
                }
            }
            val reply1 = flowOf(outbound1).map { value -> auditAuditEventValidationService.scoreAuditEvent(value) }.single()
            explainDriverCalculationResponse {
                this.driver = driver {
                    this.contact = request.driver.contact
                    this.driverId = request.driverId
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    /** Returns a driver. */
    override suspend fun getDriverRates(request: GetDriverRatesRequest): GetDriverRatesResponse {
        return handleGetDriverRates(request)
    }

    /** Quotes a driver. */
    override suspend fun quoteDriver(request: QuoteDriverRequest): QuoteDriverResponse = handleQuoteDriver(request)

    /** Simulates a driver. */
    override suspend fun simulateDriver(request: SimulateDriverRequest): SimulateDriverResponse =
        RpcBoundary.run("acme.drivers.v1.DriverCalculationService/SimulateDriver") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SimulateDriver", request)
            log.atInfo().addKeyValue("operation", "SimulateDriver").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driver.contact.email).log("Recorded audit detail")
            val outbound0 = browseAccessPoliciesRequest {
                this.context = request.context
                this.filter = BrowseAccessPoliciesRequestKt.filter {
                    this.createdAfter = request.driver.passport.expiresAt
                    this.ownerId = request.driver.driverId
                    this.region = request.details.destination.region
                }
            }
            val reply0 = authorizationAccessPolicyQueryService.browseAccessPolicies(outbound0)
            val outbound1 = resolveNotificationsRequest {}.notificationsCopy {
                this.context = request.context
            }
            val reply1 = notificationsNotificationQueryService.resolveNotifications(outbound1)
            val outbound2 = request.toResolveAuditEventsFromSimulateDriver2()
            val reply2 = auditAuditEventQueryService.resolveAuditEvents(outbound2)
            val outbound3 = watchLedgerAccountsRequest {
                this.context = request.context
                this.subscriber = contactInfo {
                    this.name = request.driver.contact.name
                }
            }
            ledgerLedgerAccountService.watchLedgerAccounts(outbound3).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            simulateDriverResponse {
                this.breakdown = SimulateDriverResponseKt.breakdown {
                    this.currencyCode = request.details.currencyCode
                }
                this.driver = driver {
                    this.driverId = request.driverId
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply2.result.message
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleCalculateDriver(request: CalculateDriverRequest): CalculateDriverResponse =
        RpcBoundary.run("acme.drivers.v1.DriverCalculationService/CalculateDriver") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CalculateDriver", request)
            log.atInfo().addKeyValue("operation", "CalculateDriver").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driver.driverId).log("Recorded audit detail")
            val outbound0 = recalculateProofOfDeliveriesRequest {
                this.context = request.context
                this.proofOfDelivery = proofOfDelivery {
                    this.billingAddress = postalAddress {
                        this.lines.addAll(request.details.destination.linesList)
                    }
                }
            }
            val invoke0: suspend (RecalculateProofOfDeliveriesRequest, Metadata) -> RecalculateProofOfDeliveriesResponse = deliveriesProofOfDeliveryCalculationService::recalculateProofOfDeliveries
            val reply0 = invoke0(outbound0, Metadata())
            val outbound1 = FindAppointmentsRequest.newBuilder().apply {
                setContext(request.context)
                setFilter(FindAppointmentsRequestKt.filter {
                    this.createdAfter = reply0.proofOfDelivery.audit.updatedAt
                    this.labels.putAll(reply0.proofOfDelivery.metadataMap)
                    this.region = request.driver.primaryAddress.region
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply1 = outbound1.let { value -> appointmentsAppointmentQueryService.findAppointments(value) }
            calculateDriverResponse {
                this.driver = driver {
                    this.driverId = request.driverId
                    this.primaryAddress = request.driver.primaryAddress
                    this.relationshipManager = userRef {
                        this.displayName = reply0.proofOfDelivery.counterparty.displayName
                    }
                }
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleEstimateDriver(request: EstimateDriverRequest): EstimateDriverResponse =
        RpcBoundary.run("acme.drivers.v1.DriverCalculationService/EstimateDriver") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EstimateDriver", request)
            log.atInfo().addKeyValue("operation", "EstimateDriver").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driver.driverId).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                calculateZoneRequest {
                    this.context = request.context
                    this.zone = zone {
                        this.children.addAll(listOf(zone {
                            this.children.addAll(listOf(zone {
                                this.location = postalAddress {
                                    this.postalCode = request.driver.primaryAddress.postalCode
                                    this.recipient = request.driver.primaryAddress.recipient
                                }
                                this.manager = userRef {
                                    this.displayName = request.driver.relationshipManager.displayName
                                    this.email = request.driver.contact.email
                                }
                            }))
                        }))
                    }
                }
            }
            val reply0 = dispatchZoneCalculationService.calculateZone(outbound0)
            val outbound1 = exportAppointmentsRequest {
                this.context = request.context
                this.filter = ExportAppointmentsRequestKt.filter {
                    this.createdAfter = reply0.zone.audit.createdAt
                    this.ownerId = reply0.zone.manager.userId
                    this.region = reply0.zone.location.region
                }
            }
            appointmentsAppointmentQueryService.exportAppointments(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound2 = cloneUserRequest {
                this.context = request.context
                this.user = user {
                    this.preferences = userPreferences {
                        this.language = request.driver.preferences.language
                    }
                }
            }
            val reply2 = identityUserService.cloneUser(WireCopies.reflective(outbound2))
            val outbound3 = listDeliveriesRequest {
                this.context = request.context
                this.filter = ListDeliveriesRequestKt.filter {
                    this.region = reply2.user.primaryAddress.region
                }
            }
            val reply3 = workerForEstimateDriver3.submit(outbound3)
            estimateDriverResponse {
                this.driver = driver {
                    this.attributes.putAll(reply0.zone.attributesMap)
                    this.contact = reply2.user.contact
                    this.driverId = request.driverId
                    this.name = request.driver.name
                }
                this.result = operationResult {
                    this.details.putAll(reply3.result.detailsMap)
                    this.message = reply3.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleGetDriverRates(request: GetDriverRatesRequest): GetDriverRatesResponse =
        RpcBoundary.run("acme.drivers.v1.DriverCalculationService/GetDriverRates") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetDriverRates", request)
            log.atInfo().addKeyValue("operation", "GetDriverRates").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            getDriverRatesResponse {
                this.driver = driver {
                    this.driverId = request.driverId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleQuoteDriver(request: QuoteDriverRequest): QuoteDriverResponse =
        RpcBoundary.run("acme.drivers.v1.DriverCalculationService/QuoteDriver") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "QuoteDriver", request)
            log.atInfo().addKeyValue("operation", "QuoteDriver").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.driver.driverId).log("Recorded audit detail")
            val outbound0 = simulateProofOfDeliveryRequest {
                this.context = request.context
                this.details = SimulateProofOfDeliveryRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = request.driver.primaryAddress.countryCode
                        this.locality = request.driver.primaryAddress.locality
                    }
                }
                this.proofOfDelivery = proofOfDelivery {
                    this.billingAddress = postalAddress {
                        this.lines.addAll(request.details.destination.linesList)
                        this.postalCode = request.details.destination.postalCode
                    }
                }
            }
            val reply0 = withTimeout(5_000) { deliveriesProofOfDeliveryCalculationService.simulateProofOfDelivery(outbound0) }
            val configure1: ExplainSponsorCalculationRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.details = ExplainSponsorCalculationRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.driver.contact.email
                    }
                }
            }
            val outbound1 = explainSponsorCalculationRequest(configure1)
            val reply1 = sponsorshipsSponsorCalculationService.explainSponsorCalculation(outbound1)
            val outbound2 = summarizeSearchIndexesRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.details = SummarizeSearchIndexesRequestKt.details {
                    this.period = request.driver.preferences.quietHours
                }
                this.reason = request.reason
            }
            val reply2 = searchSearchIndexQueryService.summarizeSearchIndexes(outbound2)
            quoteDriverResponse {
                this.driver = driver {
                    this.audit = auditInfo {
                        this.createdAt = reply0.proofOfDelivery.audit.createdAt
                    }
                    this.contact = reply1.sponsor.contact
                    this.driverId = request.driverId
                    this.primaryAddress = request.driver.primaryAddress
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.referenceIds.addAll(reply2.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private fun SimulateDriverRequest.toResolveAuditEventsFromSimulateDriver2(): ResolveAuditEventsRequest {
        val source = this
        return resolveAuditEventsRequest {
            this.context = source.context
        }
    }
}
