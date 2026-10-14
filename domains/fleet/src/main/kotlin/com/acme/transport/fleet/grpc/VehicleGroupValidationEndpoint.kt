package com.acme.transport.fleet.grpc

import com.acme.authorization.v1.AccessPolicyServiceGrpcKt
import com.acme.authorization.v1.ListAccessPoliciesRequestKt
import com.acme.authorization.v1.copy as authorizationCopy
import com.acme.authorization.v1.listAccessPoliciesRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.NormalizeProofOfDeliveryRequest
import com.acme.deliveries.v1.ProofOfDeliveryValidationServiceGrpcKt
import com.acme.deliveries.v1.normalizeProofOfDeliveryRequest
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.fleet.v1.CalculateVehicleGroupRequest
import com.acme.fleet.v1.CalculateVehicleGroupResponse
import com.acme.fleet.v1.CheckVehicleGroupEligibilityRequest
import com.acme.fleet.v1.CheckVehicleGroupEligibilityResponse
import com.acme.fleet.v1.CheckVehicleGroupEligibilityResponseKt
import com.acme.fleet.v1.ExplainVehicleGroupCalculationRequest
import com.acme.fleet.v1.ExplainVehicleGroupCalculationRequestKt
import com.acme.fleet.v1.ExplainVehicleGroupDecisionRequest
import com.acme.fleet.v1.ExplainVehicleGroupDecisionResponse
import com.acme.fleet.v1.GetVehicleGroupRatesRequest
import com.acme.fleet.v1.GetVehicleGroupRatesResponse
import com.acme.fleet.v1.GetVehicleGroupRulesRequest
import com.acme.fleet.v1.GetVehicleGroupRulesResponse
import com.acme.fleet.v1.InspectionCalculationServiceGrpcKt
import com.acme.fleet.v1.LintVehicleGroupsRequest
import com.acme.fleet.v1.LintVehicleGroupsResponse
import com.acme.fleet.v1.LintVehicleGroupsResponseKt
import com.acme.fleet.v1.PreviewVehicleGroupRequest
import com.acme.fleet.v1.PreviewVehicleGroupResponse
import com.acme.fleet.v1.QuoteVehicleGroupRequest
import com.acme.fleet.v1.ValidateVehicleGroupRequest
import com.acme.fleet.v1.ValidateVehicleGroupResponse
import com.acme.fleet.v1.VehicleGroupCalculationServiceGrpcKt
import com.acme.fleet.v1.VehicleGroupValidationServiceGrpcKt
import com.acme.fleet.v1.calculateVehicleGroupRequest
import com.acme.fleet.v1.checkVehicleGroupEligibilityResponse
import com.acme.fleet.v1.compareVehicleGroupOptionsRequest
import com.acme.fleet.v1.explainInspectionCalculationRequest
import com.acme.fleet.v1.explainVehicleGroupCalculationRequest
import com.acme.fleet.v1.explainVehicleGroupDecisionResponse
import com.acme.fleet.v1.getVehicleGroupRatesRequest
import com.acme.fleet.v1.getVehicleGroupRulesResponse
import com.acme.fleet.v1.inspection
import com.acme.fleet.v1.lintVehicleGroupsResponse
import com.acme.fleet.v1.previewVehicleGroupResponse
import com.acme.fleet.v1.quoteVehicleGroupRequest
import com.acme.fleet.v1.validateVehicleGroupResponse
import com.acme.fleet.v1.vehicleGroup
import com.acme.fleet.v1.vehicleGroupRef
import com.acme.fuel.v1.FuelPolicyValidationServiceGrpcKt
import com.acme.fuel.v1.LintFuelPoliciesRequest
import com.acme.fuel.v1.LintFuelPoliciesResponse
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.lintFuelPoliciesRequest
import com.acme.identity.v1.userRef
import com.acme.notifications.v1.CheckNotificationEligibilityRequestKt
import com.acme.notifications.v1.NotificationValidationServiceGrpcKt
import com.acme.notifications.v1.checkNotificationEligibilityRequest
import com.acme.notifications.v1.notification
import com.acme.notifications.v1.notificationComment
import com.acme.seating.v1.SeatMapQueryServiceGrpcKt
import com.acme.seating.v1.SummarizeSeatMapsRequestKt
import com.acme.seating.v1.summarizeSeatMapsRequest
import com.acme.transport.fleet.FleetRepository
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.venues.v1.VenueServiceGrpcKt
import com.acme.venues.v1.updateVenueRequest
import com.acme.venues.v1.venue
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Checks vehicle groups against the rules before anything acts on them. */
class VehicleGroupValidationEndpoint(
    private val store: FleetRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyService: AccessPolicyServiceGrpcKt.AccessPolicyServiceCoroutineStub,
    private val deliveriesProofOfDeliveryValidationService: ProofOfDeliveryValidationServiceGrpcKt.ProofOfDeliveryValidationServiceCoroutineStub,
    private val fleetInspectionCalculationService: InspectionCalculationServiceGrpcKt.InspectionCalculationServiceCoroutineStub,
    private val fleetVehicleGroupCalculationService: VehicleGroupCalculationServiceGrpcKt.VehicleGroupCalculationServiceCoroutineStub,
    private val fuelFuelPolicyValidationService: FuelPolicyValidationServiceGrpcKt.FuelPolicyValidationServiceCoroutineStub,
    private val notificationsNotificationValidationService: NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub,
    private val seatingSeatMapQueryService: SeatMapQueryServiceGrpcKt.SeatMapQueryServiceCoroutineStub,
    private val venuesVenueService: VenueServiceGrpcKt.VenueServiceCoroutineStub,
) : VehicleGroupValidationServiceGrpcKt.VehicleGroupValidationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(VehicleGroupValidationEndpoint::class.java)

    private val invokeValidateVehicleGroup2: suspend (GetVehicleGroupRatesRequest) -> GetVehicleGroupRatesResponse = { payload ->
        fleetVehicleGroupCalculationService.getVehicleGroupRates(payload)
    }

    /** Checks a vehicle group. */
    override suspend fun checkVehicleGroupEligibility(request: CheckVehicleGroupEligibilityRequest): CheckVehicleGroupEligibilityResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupValidationService/CheckVehicleGroupEligibility") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CheckVehicleGroupEligibility", request)
            log.atInfo().addKeyValue("operation", "CheckVehicleGroupEligibility").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicleGroup.vehicleGroupId).log("Recorded audit detail")
            val outbound0 = explainVehicleGroupCalculationRequest {
                this.context = request.context
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.children.addAll(listOf(vehicleGroup {
                                this.location = request.vehicleGroup.location
                            }))
                            this.location = postalAddress {
                                this.postalCode = request.vehicleGroup.location.postalCode
                                this.region = request.vehicleGroup.location.region
                            }
                            this.manager = userRef {
                                this.email = request.details.contact.email
                            }
                        }))
                    }))
                }
            }
            val reply0 = flowOf(outbound0).map { value -> fleetVehicleGroupCalculationService.explainVehicleGroupCalculation(value) }.single()
            val outbound1 = explainInspectionCalculationRequest {
                this.context = request.context
                this.inspection = inspection {
                    this.customer = vehicleGroupRef {
                        this.displayName = reply0.vehicleGroup.manager.displayName
                    }
                    this.requester = contactInfo {
                        this.name = request.details.contact.name
                    }
                }
            }
            val reply1 = withTimeout(5_000) { fleetInspectionCalculationService.explainInspectionCalculation(outbound1) }
            checkVehicleGroupEligibilityResponse {
                this.outcome = CheckVehicleGroupEligibilityResponseKt.outcome {
                    this.message = reply0.outcome.message
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.vehicleGroup = vehicleGroup {
                    this.audit = auditInfo {
                        this.createdBy = reply1.inspection.audit.createdBy
                    }
                    this.children.addAll(listOf(vehicleGroup {
                        this.status = reply0.vehicleGroup.status
                    }))
                    this.name = request.vehicleGroup.name
                    this.vehicleGroupId = request.vehicleGroupId
                }
            }
        }

    /** Explains a vehicle group. */
    override suspend fun explainVehicleGroupDecision(request: ExplainVehicleGroupDecisionRequest): ExplainVehicleGroupDecisionResponse {
        return handleExplainVehicleGroupDecision(request)
    }

    /** Returns a vehicle group. */
    override suspend fun getVehicleGroupRules(request: GetVehicleGroupRulesRequest): GetVehicleGroupRulesResponse = handleGetVehicleGroupRules(request)

    /** Lints a vehicle group. */
    override suspend fun lintVehicleGroups(request: LintVehicleGroupsRequest): LintVehicleGroupsResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupValidationService/LintVehicleGroups") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LintVehicleGroups", request)
            log.atInfo().addKeyValue("operation", "LintVehicleGroups").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicleGroup.vehicleGroupId).log("Recorded audit detail")
            val outbound0 = listAccessPoliciesRequest {}.authorizationCopy {
                this.context = request.context
                this.filter = ListAccessPoliciesRequestKt.filter {
                    this.region = request.vehicleGroup.location.region
                }
            }
            val reply0 = authorizationAccessPolicyService.listAccessPolicies(outbound0)
            val outbound1 = request.toQuoteVehicleGroupFromLintVehicleGroups1()
            val reply1 = fleetVehicleGroupCalculationService.quoteVehicleGroup(outbound1)
            lintVehicleGroupsResponse {
                this.outcome = LintVehicleGroupsResponseKt.outcome {
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.status = reply0.result.status
                }
                this.vehicleGroup = vehicleGroup {
                    this.attributes.putAll(request.vehicleGroup.attributesMap)
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.location = postalAddress {
                                this.region = reply1.vehicleGroup.location.region
                            }
                        }))
                        this.depth = reply1.vehicleGroup.depth
                    }))
                    this.vehicleGroupId = request.vehicleGroupId
                }
            }
        }

    /** Previews a vehicle group. */
    override suspend fun previewVehicleGroup(request: PreviewVehicleGroupRequest): PreviewVehicleGroupResponse {
        return handlePreviewVehicleGroup(request)
    }

    /** Validates a vehicle group. */
    override suspend fun validateVehicleGroup(request: ValidateVehicleGroupRequest): ValidateVehicleGroupResponse = handleValidateVehicleGroup(request)

    private suspend fun handleExplainVehicleGroupDecision(request: ExplainVehicleGroupDecisionRequest): ExplainVehicleGroupDecisionResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupValidationService/ExplainVehicleGroupDecision") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainVehicleGroupDecision", request)
            log.atInfo().addKeyValue("operation", "ExplainVehicleGroupDecision").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicleGroup.vehicleGroupId).log("Recorded audit detail")
            val configure0: ExplainVehicleGroupCalculationRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.manager = userRef {
                                this.userId = request.vehicleGroup.manager.userId
                            }
                        }))
                    }))
                }
            }
            val outbound0 = explainVehicleGroupCalculationRequest(configure0)
            val reply0 = fleetVehicleGroupCalculationService.explainVehicleGroupCalculation(outbound0)
            val outbound1 = updateVenueRequest {
                this.context = request.context
                this.venue = venue {
                    this.audit = auditInfo {
                        this.createdAt = request.vehicleGroup.audit.createdAt
                        this.etag = request.vehicleGroup.audit.etag
                    }
                    this.owner = userRef {
                        this.displayName = request.vehicleGroup.manager.displayName
                        this.email = request.vehicleGroup.manager.email
                    }
                }
            }
            val reply1 = venuesVenueService.updateVenue(outbound1)
            explainVehicleGroupDecisionResponse {
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = "ok"
                }
                this.vehicleGroup = vehicleGroup {
                    this.attributes.putAll(request.vehicleGroup.attributesMap)
                    this.audit = auditInfo {
                        this.createdBy = reply1.venue.audit.createdBy
                    }
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.audit = auditInfo {
                                this.etag = reply0.vehicleGroup.audit.etag
                            }
                        }))
                    }))
                    this.vehicleGroupId = request.vehicleGroupId
                }
            }
        }

    private suspend fun handleGetVehicleGroupRules(request: GetVehicleGroupRulesRequest): GetVehicleGroupRulesResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupValidationService/GetVehicleGroupRules") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetVehicleGroupRules", request)
            log.atInfo().addKeyValue("operation", "GetVehicleGroupRules").log("Handling transport request")
            val outbound0 = summarizeSeatMapsRequest {
                this.context = request.context
                this.details = SummarizeSeatMapsRequestKt.details {
                    this.filters.putAll(request.context.baggageMap)
                }
            }
            val reply0 = seatingSeatMapQueryService.summarizeSeatMaps(outbound0)
            getVehicleGroupRulesResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
                this.vehicleGroup = vehicleGroup {
                    this.vehicleGroupId = request.vehicleGroupId
                }
            }
        }

    private fun LintVehicleGroupsRequest.toQuoteVehicleGroupFromLintVehicleGroups1(): QuoteVehicleGroupRequest {
        val source = this
        return quoteVehicleGroupRequest {
            this.context = source.context
            this.vehicleGroup = vehicleGroup {
                this.children.addAll(listOf(vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.audit = auditInfo {
                            this.createdAt = source.vehicleGroup.audit.createdAt
                            this.updatedAt = source.vehicleGroup.audit.updatedAt
                        }
                        this.manager = userRef {
                            this.email = source.vehicleGroup.manager.email
                        }
                    }))
                }))
            }
        }
    }

    private data class PreviewVehicleGroup3Command(
        val context: RequestContext,
        val vehicleGroupChildrenChildrenLocationCountryCode: String
    ) {
        fun toRequest(): QuoteVehicleGroupRequest {
            val command = this
            return quoteVehicleGroupRequest {
                this.context = command.context
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.location = postalAddress {
                                this.countryCode = command.vehicleGroupChildrenChildrenLocationCountryCode
                            }
                        }))
                    }))
                }
            }
        }
    }

    private suspend fun handlePreviewVehicleGroup(request: PreviewVehicleGroupRequest): PreviewVehicleGroupResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupValidationService/PreviewVehicleGroup") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PreviewVehicleGroup", request)
            log.atInfo().addKeyValue("operation", "PreviewVehicleGroup").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicleGroup.vehicleGroupId).log("Recorded audit detail")
            val outbound0 = checkNotificationEligibilityRequest {
                this.context = request.context
                this.details = CheckNotificationEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.details.contact.email
                    }
                }
                this.notification = notification {
                    this.comments.addAll(listOf(notificationComment {
                        this.createdAt = request.vehicleGroup.audit.createdAt
                    }))
                    this.requester = contactInfo {
                        this.email = request.vehicleGroup.manager.email
                        this.preferredChannel = request.details.contact.preferredChannel
                    }
                }
            }
            val reply0 = notificationsNotificationValidationService.checkNotificationEligibility(outbound0)
            val outbound1 = explainVehicleGroupCalculationRequest {
                this.context = request.context
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.manager = userRef {
                                this.email = reply0.notification.requester.email
                            }
                        }))
                    }))
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = fleetVehicleGroupCalculationService.explainVehicleGroupCalculation(envelope1.unpack(ExplainVehicleGroupCalculationRequest::class.java))
            val outbound2 = compareVehicleGroupOptionsRequest {
                this.context = request.context
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.audit = auditInfo {
                                this.createdBy = reply1.vehicleGroup.audit.createdBy
                            }
                            this.location = postalAddress {
                                this.recipient = reply1.vehicleGroup.location.recipient
                            }
                            this.manager = userRef {
                                this.email = reply0.notification.requester.email
                            }
                        }))
                    }))
                }
            }
            val reply2 = fleetVehicleGroupCalculationService.compareVehicleGroupOptions(WireCopies.json(outbound2))
            val outbound3 = PreviewVehicleGroup3Command(
                context = request.context,
                vehicleGroupChildrenChildrenLocationCountryCode = reply2.vehicleGroup.location.countryCode
            ).toRequest()
            val reply3 = fleetVehicleGroupCalculationService.quoteVehicleGroup(outbound3)
            previewVehicleGroupResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
                this.vehicleGroup = vehicleGroup {
                    this.attributes.putAll(request.vehicleGroup.attributesMap)
                    this.audit = auditInfo {
                        this.updatedAt = reply1.vehicleGroup.audit.updatedAt
                    }
                    this.location = postalAddress {
                        this.locality = reply3.vehicleGroup.location.locality
                    }
                    this.manager = reply1.vehicleGroup.manager
                    this.path.addAll(reply2.vehicleGroup.pathList)
                    this.vehicleGroupId = request.vehicleGroupId
                }
            }
        }

    private suspend fun handleValidateVehicleGroup(request: ValidateVehicleGroupRequest): ValidateVehicleGroupResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleGroupValidationService/ValidateVehicleGroup") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ValidateVehicleGroup", request)
            log.atInfo().addKeyValue("operation", "ValidateVehicleGroup").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicleGroup.name).log("Recorded audit detail")
            val outbound0 = normalizeProofOfDeliveryRequest {
                this.context = request.context
                this.proofOfDelivery = proofOfDelivery {
                    this.audit = auditInfo {
                        this.etag = request.vehicleGroup.audit.etag
                    }
                }
            }
            val command0: Submission<NormalizeProofOfDeliveryRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = deliveriesProofOfDeliveryValidationService.normalizeProofOfDelivery(payload0)
            val outbound1 = calculateVehicleGroupRequest {
                this.context = request.context
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.audit = auditInfo {
                                this.updatedBy = request.vehicleGroup.audit.updatedBy
                            }
                        }))
                    }))
                }
            }
            val adapter1 = OutboundPort<CalculateVehicleGroupRequest, CalculateVehicleGroupResponse> { payload ->
                fleetVehicleGroupCalculationService.calculateVehicleGroup(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            val outbound2 = getVehicleGroupRatesRequest {
                this.context = request.context
                this.vehicleGroupId = request.vehicleGroup.vehicleGroupId
            }
            val reply2 = invokeValidateVehicleGroup2(outbound2)
            val outbound3 = lintFuelPoliciesRequest {
                this.context = request.context
                this.fuelPolicy = fuelPolicy {
                    this.owner = userRef {
                        this.userId = reply2.vehicleGroup.manager.userId
                    }
                }
            }
            val invoke3: suspend (LintFuelPoliciesRequest, Metadata) -> LintFuelPoliciesResponse = fuelFuelPolicyValidationService::lintFuelPolicies
            val reply3 = invoke3(outbound3, Metadata())
            validateVehicleGroupResponse {
                this.result = operationResult {
                    this.details.putAll(reply2.result.detailsMap)
                    this.status = "ok"
                }
                this.vehicleGroup = vehicleGroup {
                    this.attributes.putAll(request.vehicleGroup.attributesMap)
                    this.children.addAll(listOf(vehicleGroup {
                        this.audit = auditInfo {
                            this.createdBy = reply1.vehicleGroup.audit.createdBy
                            this.updatedAt = reply3.fuelPolicy.audit.updatedAt
                        }
                        this.location = postalAddress {
                            this.locality = reply0.proofOfDelivery.billingAddress.locality
                        }
                    }))
                    this.vehicleGroupId = request.vehicleGroupId
                }
            }
        }
}
