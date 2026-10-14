package com.acme.transport.fuel.api

import com.acme.appointments.v1.appointmentRef
import com.acme.careplans.v1.CarePlanCalculationServiceGrpcKt
import com.acme.careplans.v1.SimulateCarePlanRequest
import com.acme.careplans.v1.SimulateCarePlanResponse
import com.acme.careplans.v1.carePlan
import com.acme.careplans.v1.carePlanCardSettlement
import com.acme.careplans.v1.carePlanLine
import com.acme.careplans.v1.simulateCarePlanRequest
import com.acme.certificates.v1.CertificateServiceGrpcKt
import com.acme.certificates.v1.ListCertificatesRequest
import com.acme.certificates.v1.ListCertificatesRequestKt
import com.acme.certificates.v1.listCertificatesRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.ExplainProofOfDeliveryCalculationRequestKt
import com.acme.deliveries.v1.ProofOfDeliveryCalculationServiceGrpcKt
import com.acme.deliveries.v1.explainProofOfDeliveryCalculationRequest
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.proofOfDeliveryCardSettlement
import com.acme.drivers.v1.driverRef
import com.acme.featureflags.v1.FeatureFlagQueryServiceGrpcKt
import com.acme.featureflags.v1.SummarizeFeatureFlagsRequest
import com.acme.featureflags.v1.SummarizeFeatureFlagsResponse
import com.acme.featureflags.v1.summarizeFeatureFlagsRequest
import com.acme.fuel.v1.CalculateFuelCardRequest
import com.acme.fuel.v1.CalculateFuelCardResponse
import com.acme.fuel.v1.CalculateFuelCardResponseKt
import com.acme.fuel.v1.EstimateFuelCardRequest
import com.acme.fuel.v1.EstimateFuelCardResponse
import com.acme.fuel.v1.ExplainFuelCardCalculationRequest
import com.acme.fuel.v1.ExplainFuelCardCalculationResponse
import com.acme.fuel.v1.FuelCard
import com.acme.fuel.v1.FuelCardCalculationServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyHistoryServiceGrpcKt
import com.acme.fuel.v1.GetFuelCardRatesRequest
import com.acme.fuel.v1.GetFuelCardRatesResponse
import com.acme.fuel.v1.QuoteFuelCardRequest
import com.acme.fuel.v1.QuoteFuelCardResponse
import com.acme.fuel.v1.RecordFuelPolicyVersionResponse
import com.acme.fuel.v1.SimulateFuelCardRequest
import com.acme.fuel.v1.SimulateFuelCardResponse
import com.acme.fuel.v1.calculateFuelCardResponse
import com.acme.fuel.v1.estimateFuelCardResponse
import com.acme.fuel.v1.explainFuelCardCalculationResponse
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.fuelCardLimit
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.fuelPolicyDiscountAction
import com.acme.fuel.v1.getFuelCardRatesResponse
import com.acme.fuel.v1.quoteFuelCardResponse
import com.acme.fuel.v1.recordFuelPolicyVersionRequest
import com.acme.fuel.v1.simulateFuelCardResponse
import com.acme.identity.v1.userRef
import com.acme.notifications.v1.NotificationValidationServiceGrpcKt
import com.acme.notifications.v1.ScoreNotificationRequestKt
import com.acme.notifications.v1.notification
import com.acme.notifications.v1.scoreNotificationRequest
import com.acme.search.v1.CountSearchIndexesRequest
import com.acme.search.v1.CountSearchIndexesRequestKt
import com.acme.search.v1.SearchIndexQueryServiceGrpcKt
import com.acme.search.v1.countSearchIndexesRequest
import com.acme.secrets.v1.SecretServiceGrpcKt
import com.acme.secrets.v1.UpdateSecretRequest
import com.acme.secrets.v1.UpdateSecretResponse
import com.acme.secrets.v1.secret
import com.acme.secrets.v1.secretBankFunding
import com.acme.secrets.v1.secretLimit
import com.acme.secrets.v1.updateSecretRequest
import com.acme.transport.fuel.FuelRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.venues.v1.VenueServiceGrpcKt
import com.acme.venues.v1.cloneVenueRequest
import com.acme.venues.v1.venue
import com.acme.yards.v1.BrowseYardsRequest
import com.acme.yards.v1.BrowseYardsRequestKt
import com.acme.yards.v1.DockCalculationServiceGrpcKt
import com.acme.yards.v1.ValidateYardRequest
import com.acme.yards.v1.YardQueryServiceGrpcKt
import com.acme.yards.v1.YardValidationServiceGrpcKt
import com.acme.yards.v1.browseYardsRequest
import com.acme.yards.v1.compareDockOptionsRequest
import com.acme.yards.v1.dock
import com.acme.yards.v1.validateYardRequest
import com.acme.yards.v1.yard
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import org.slf4j.LoggerFactory

/** Computes the figures of a fuel card, such as totals, taxes, and estimates. */
class FuelCardCalculationGrpcService(
    private val store: FuelRepository,
    private val workers: CoroutineScope,
    private val careplansCarePlanCalculationService: CarePlanCalculationServiceGrpcKt.CarePlanCalculationServiceCoroutineStub,
    private val certificatesCertificateService: CertificateServiceGrpcKt.CertificateServiceCoroutineStub,
    private val deliveriesProofOfDeliveryCalculationService: ProofOfDeliveryCalculationServiceGrpcKt.ProofOfDeliveryCalculationServiceCoroutineStub,
    private val featureflagsFeatureFlagQueryService: FeatureFlagQueryServiceGrpcKt.FeatureFlagQueryServiceCoroutineStub,
    private val fuelFuelPolicyHistoryService: FuelPolicyHistoryServiceGrpcKt.FuelPolicyHistoryServiceCoroutineStub,
    private val notificationsNotificationValidationService: NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub,
    private val searchSearchIndexQueryService: SearchIndexQueryServiceGrpcKt.SearchIndexQueryServiceCoroutineStub,
    private val secretsSecretService: SecretServiceGrpcKt.SecretServiceCoroutineStub,
    private val venuesVenueService: VenueServiceGrpcKt.VenueServiceCoroutineStub,
    private val yardsDockCalculationService: DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub,
    private val yardsYardQueryService: YardQueryServiceGrpcKt.YardQueryServiceCoroutineStub,
    private val yardsYardValidationService: YardValidationServiceGrpcKt.YardValidationServiceCoroutineStub,
) : FuelCardCalculationServiceGrpcKt.FuelCardCalculationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(FuelCardCalculationGrpcService::class.java)

    private val invokeQuoteFuelCard2: suspend (UpdateSecretRequest) -> UpdateSecretResponse = { payload ->
        secretsSecretService.updateSecret(payload)
    }

    /** Calculates a fuel card. */
    override suspend fun calculateFuelCard(request: CalculateFuelCardRequest): CalculateFuelCardResponse {
        return handleCalculateFuelCard(request)
    }

    /** Estimates a fuel card. */
    override suspend fun estimateFuelCard(request: EstimateFuelCardRequest): EstimateFuelCardResponse = handleEstimateFuelCard(request)

    /** Explains a fuel card. */
    override suspend fun explainFuelCardCalculation(request: ExplainFuelCardCalculationRequest): ExplainFuelCardCalculationResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardCalculationService/ExplainFuelCardCalculation") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainFuelCardCalculation", request)
            log.atInfo().addKeyValue("operation", "ExplainFuelCardCalculation").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCard.fuelCardId).log("Recorded audit detail")
            val outbound0 = compareDockOptionsRequest {
                this.context = request.context
                this.dock = dock {
                    this.owner = userRef {
                        this.email = request.fuelCard.holder.email
                    }
                }
            }
            val reply0 = yardsDockCalculationService.compareDockOptions(WireCopies.json(outbound0))
            explainFuelCardCalculationResponse {
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.updatedAt = reply0.dock.audit.updatedAt
                    }
                    this.fuelCardId = request.fuelCardId
                    this.openedAt = request.fuelCard.openedAt
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Returns a fuel card. */
    override suspend fun getFuelCardRates(request: GetFuelCardRatesRequest): GetFuelCardRatesResponse {
        return handleGetFuelCardRates(request)
    }

    /** Quotes a fuel card. */
    override suspend fun quoteFuelCard(request: QuoteFuelCardRequest): QuoteFuelCardResponse = handleQuoteFuelCard(request)

    /** Simulates a fuel card. */
    override suspend fun simulateFuelCard(request: SimulateFuelCardRequest): SimulateFuelCardResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardCalculationService/SimulateFuelCard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SimulateFuelCard", request)
            log.atInfo().addKeyValue("operation", "SimulateFuelCard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCard.fuelCardId).log("Recorded audit detail")
            val outbound0 = CountSearchIndexesRequest.newBuilder().apply {
                setContext(request.context)
                setFilter(CountSearchIndexesRequestKt.filter {
                    this.createdAfter = request.fuelCard.audit.updatedAt
                    this.labels.putAll(request.fuelCard.labelsMap)
                    this.region = request.details.destination.region
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> searchSearchIndexQueryService.countSearchIndexes(value) }
            val outbound1 = RequestFactory.create {
                scoreNotificationRequest {
                    this.context = request.context
                    this.details = ScoreNotificationRequestKt.details {
                        this.destination = postalAddress {
                            this.countryCode = request.details.destination.countryCode
                        }
                    }
                    this.notification = notification {
                        this.audit = auditInfo {
                            this.createdBy = request.fuelCard.audit.createdBy
                        }
                    }
                }
            }
            val reply1 = notificationsNotificationValidationService.scoreNotification(outbound1)
            val outbound2 = cloneVenueRequest {
                this.context = request.context
                this.venue = venue {
                    this.address = postalAddress {
                        this.locality = request.details.destination.locality
                    }
                    this.audit = auditInfo {
                        this.createdAt = reply1.notification.audit.createdAt
                        this.updatedAt = request.fuelCard.audit.updatedAt
                    }
                    this.owner = userRef {
                        this.displayName = request.fuelCard.holder.displayName
                    }
                }
            }
            val reply2 = ClientRegistry.lookup("acme.venues.v1.VenueService", VenueServiceGrpcKt.VenueServiceCoroutineStub::class.java).cloneVenue(outbound2)
            simulateFuelCardResponse {
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.updatedBy = reply2.venue.audit.updatedBy
                    }
                    this.fuelCardId = request.fuelCardId
                    this.holder = driverRef {
                        this.email = reply1.notification.assignee.email
                    }
                    this.openedAt = request.fuelCard.openedAt
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = reply0.result.status
                }
            }
        }

    private fun CalculateFuelCardRequest.toListCertificatesFromCalculateFuelCard0(): ListCertificatesRequest {
        val source = this
        return listCertificatesRequest {
            this.context = source.context
            this.filter = ListCertificatesRequestKt.filter {
                this.labels.putAll(source.fuelCard.labelsMap)
                this.region = source.details.destination.region
            }
        }
    }

    private suspend fun handleCalculateFuelCard(request: CalculateFuelCardRequest): CalculateFuelCardResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardCalculationService/CalculateFuelCard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CalculateFuelCard", request)
            log.atInfo().addKeyValue("operation", "CalculateFuelCard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCard.fuelCardId).log("Recorded audit detail")
            val outbound0 = request.toListCertificatesFromCalculateFuelCard0()
            val reply0 = certificatesCertificateService.listCertificates(outbound0)
            calculateFuelCardResponse {
                this.breakdown = CalculateFuelCardResponseKt.breakdown {
                    this.currencyCode = request.details.currencyCode
                }
                this.fuelCard = fuelCard {
                    this.fuelCardId = request.fuelCardId
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleEstimateFuelCard(request: EstimateFuelCardRequest): EstimateFuelCardResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardCalculationService/EstimateFuelCard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EstimateFuelCard", request)
            log.atInfo().addKeyValue("operation", "EstimateFuelCard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCard.fuelCardId).log("Recorded audit detail")
            val outbound0 = explainProofOfDeliveryCalculationRequest {
                this.context = request.context
                this.details = ExplainProofOfDeliveryCalculationRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.fuelCard.holder.email
                        this.name = request.fuelCard.contact.name
                    }
                }
                this.proofOfDelivery = proofOfDelivery {
                    this.billingAddress = postalAddress {
                        this.lines.addAll(request.details.destination.linesList)
                    }
                    this.card = proofOfDeliveryCardSettlement {
                        this.last4 = when (request.fuelCard.fundingCase) {
                            FuelCard.FundingCase.CARD -> request.fuelCard.card.last4
                            else -> ""
                        }
                    }
                }
            }
            val reply0 = deliveriesProofOfDeliveryCalculationService.explainProofOfDeliveryCalculation(outbound0)
            val outbound1 = browseYardsRequest {
                this.context = request.context
                this.filter = BrowseYardsRequestKt.filter {
                    this.region = reply0.proofOfDelivery.billingAddress.region
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = yardsYardQueryService.browseYards(envelope1.unpack(BrowseYardsRequest::class.java))
            estimateFuelCardResponse {
                this.fuelCard = fuelCard {
                    this.fuelCardId = request.fuelCardId
                    this.labels.putAll(request.fuelCard.labelsMap)
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = reply0.result.status
                }
            }
        }

    private data class GetFuelCardRates0Command(
        val context: RequestContext,
        val yardOwnerEmail: String
    ) {
        fun toRequest(): ValidateYardRequest {
            val command = this
            return validateYardRequest {
                this.context = command.context
                this.yard = yard {
                    this.owner = userRef {
                        this.email = command.yardOwnerEmail
                    }
                }
            }
        }
    }

    private suspend fun handleGetFuelCardRates(request: GetFuelCardRatesRequest): GetFuelCardRatesResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardCalculationService/GetFuelCardRates") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetFuelCardRates", request)
            log.atInfo().addKeyValue("operation", "GetFuelCardRates").log("Handling transport request")
            val outbound0 = GetFuelCardRates0Command(
                context = request.context,
                yardOwnerEmail = request.context.actor.email
            ).toRequest()
            val reply0 = yardsYardValidationService.validateYard(outbound0)
            getFuelCardRatesResponse {
                this.fuelCard = fuelCard {
                    this.fuelCardId = request.fuelCardId
                    this.limits.addAll(listOf(fuelCardLimit {
                        this.name = reply0.yard.name
                    }))
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleQuoteFuelCard(request: QuoteFuelCardRequest): QuoteFuelCardResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardCalculationService/QuoteFuelCard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "QuoteFuelCard", request)
            log.atInfo().addKeyValue("operation", "QuoteFuelCard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCard.fuelCardId).log("Recorded audit detail")
            var reply0 = RecordFuelPolicyVersionResponse.getDefaultInstance()
            request.fuelCard.limitsList.asFlow().map { item ->
                val outbound0 = recordFuelPolicyVersionRequest {
                    this.context = request.context
                    this.fuelPolicy = fuelPolicy {
                        this.discount = fuelPolicyDiscountAction {
                            this.amount = item.amount
                        }
                        this.name = item.name
                    }
                }
                fuelFuelPolicyHistoryService.recordFuelPolicyVersion(outbound0)
            }.collect { reply0 = it }
            val outbound1 = simulateCarePlanRequest {
                this.carePlan = carePlan {
                    this.card = carePlanCardSettlement {
                        this.token = request.fuelCard.card.token
                    }
                    this.counterparty = appointmentRef {
                        this.displayName = reply0.fuelPolicy.owner.displayName
                    }
                    this.lines.addAll(listOf(carePlanLine {
                        this.amount = reply0.fuelPolicy.discount.amount
                    }))
                }
                this.context = request.context
            }
            val adapter1 = OutboundPort<SimulateCarePlanRequest, SimulateCarePlanResponse> { payload ->
                careplansCarePlanCalculationService.simulateCarePlan(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            val outbound2 = updateSecretRequest {
                this.context = request.context
                this.secret = secret {
                    this.bank = secretBankFunding {
                        this.iban = reply1.carePlan.bank.iban
                    }
                    this.holder = userRef {
                        this.email = reply0.fuelPolicy.owner.email
                    }
                    this.limits.addAll(listOf(secretLimit {
                        this.amount = reply0.fuelPolicy.discount.amount
                    }))
                }
            }
            val reply2 = invokeQuoteFuelCard2(outbound2)
            val outbound3 = summarizeFeatureFlagsRequest {
                this.context = request.context
                this.reason = reply0.fuelPolicy.block.reason
            }
            val invoke3: suspend (SummarizeFeatureFlagsRequest, Metadata) -> SummarizeFeatureFlagsResponse = featureflagsFeatureFlagQueryService::summarizeFeatureFlags
            val reply3 = invoke3(outbound3, Metadata())
            quoteFuelCardResponse {
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.etag = reply1.carePlan.audit.etag
                        this.updatedAt = reply2.secret.audit.updatedAt
                    }
                    this.balance = request.fuelCard.balance
                    this.contact = contactInfo {
                        this.email = reply2.secret.holder.email
                    }
                    this.currencyCode = reply1.breakdown.currencyCode
                    this.fuelCardId = request.fuelCardId
                }
                this.result = operationResult {
                    this.message = reply3.result.message
                    this.status = "ok"
                }
            }
        }
}
