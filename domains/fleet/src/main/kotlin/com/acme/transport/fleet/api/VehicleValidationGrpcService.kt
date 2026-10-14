package com.acme.transport.fleet.api

import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.featureflags.v1.CountFeatureFlagsRequest
import com.acme.featureflags.v1.CountFeatureFlagsRequestKt
import com.acme.featureflags.v1.FeatureFlagQueryServiceGrpcKt
import com.acme.featureflags.v1.countFeatureFlagsRequest
import com.acme.fleet.v1.CheckVehicleEligibilityRequest
import com.acme.fleet.v1.CheckVehicleEligibilityResponse
import com.acme.fleet.v1.ExplainVehicleDecisionRequest
import com.acme.fleet.v1.ExplainVehicleDecisionResponse
import com.acme.fleet.v1.ExplainVehicleGroupCalculationRequestKt
import com.acme.fleet.v1.GetVehicleRulesRequest
import com.acme.fleet.v1.GetVehicleRulesResponse
import com.acme.fleet.v1.LintVehiclesRequest
import com.acme.fleet.v1.LintVehiclesResponse
import com.acme.fleet.v1.NormalizeVehicleRequest
import com.acme.fleet.v1.NormalizeVehicleResponse
import com.acme.fleet.v1.PreviewVehicleRequest
import com.acme.fleet.v1.PreviewVehicleResponse
import com.acme.fleet.v1.PreviewVehicleResponseKt
import com.acme.fleet.v1.RecalculateVehicleGroupsRequest
import com.acme.fleet.v1.ScoreVehicleRequest
import com.acme.fleet.v1.ScoreVehicleResponse
import com.acme.fleet.v1.ValidateVehicleRequest
import com.acme.fleet.v1.ValidateVehicleResponse
import com.acme.fleet.v1.Vehicle
import com.acme.fleet.v1.VehicleGroupCalculationServiceGrpcKt
import com.acme.fleet.v1.VehicleValidationServiceGrpcKt
import com.acme.fleet.v1.calculateVehicleGroupRequest
import com.acme.fleet.v1.checkVehicleEligibilityResponse
import com.acme.fleet.v1.compareVehicleGroupOptionsRequest
import com.acme.fleet.v1.copy as fleetCopy
import com.acme.fleet.v1.estimateVehicleGroupRequest
import com.acme.fleet.v1.explainVehicleDecisionResponse
import com.acme.fleet.v1.explainVehicleGroupCalculationRequest
import com.acme.fleet.v1.getVehicleRulesResponse
import com.acme.fleet.v1.lintVehiclesResponse
import com.acme.fleet.v1.normalizeVehicleResponse
import com.acme.fleet.v1.previewVehicleResponse
import com.acme.fleet.v1.quoteVehicleGroupRequest
import com.acme.fleet.v1.recalculateVehicleGroupsRequest
import com.acme.fleet.v1.scoreVehicleResponse
import com.acme.fleet.v1.validateVehicleResponse
import com.acme.fleet.v1.vehicle
import com.acme.fleet.v1.vehicleGroup
import com.acme.identity.v1.PeekUserRequest
import com.acme.identity.v1.UserQueryServiceGrpcKt
import com.acme.identity.v1.peekUserRequest
import com.acme.identity.v1.userRef
import com.acme.telematics.v1.SummarizeTelemetryReadingsRequestKt
import com.acme.telematics.v1.TelemetryReadingQueryServiceGrpcKt
import com.acme.telematics.v1.summarizeTelemetryReadingsRequest
import com.acme.transport.fleet.FleetRepository
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.google.protobuf.Any as ProtoAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Checks vehicles against the rules before anything acts on them. */
class VehicleValidationGrpcService(
    private val store: FleetRepository,
    private val workers: CoroutineScope,
    private val featureflagsFeatureFlagQueryService: FeatureFlagQueryServiceGrpcKt.FeatureFlagQueryServiceCoroutineStub,
    private val fleetVehicleGroupCalculationService: VehicleGroupCalculationServiceGrpcKt.VehicleGroupCalculationServiceCoroutineStub,
    private val identityUserQueryService: UserQueryServiceGrpcKt.UserQueryServiceCoroutineStub,
    private val telematicsTelemetryReadingQueryService: TelemetryReadingQueryServiceGrpcKt.TelemetryReadingQueryServiceCoroutineStub,
) : VehicleValidationServiceGrpcKt.VehicleValidationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(VehicleValidationGrpcService::class.java)

    /** Checks a vehicle. */
    override suspend fun checkVehicleEligibility(request: CheckVehicleEligibilityRequest): CheckVehicleEligibilityResponse {
        return handleCheckVehicleEligibility(request)
    }

    /** Explains a vehicle. */
    override suspend fun explainVehicleDecision(request: ExplainVehicleDecisionRequest): ExplainVehicleDecisionResponse = handleExplainVehicleDecision(request)

    /** Returns a vehicle. */
    override suspend fun getVehicleRules(request: GetVehicleRulesRequest): GetVehicleRulesResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleValidationService/GetVehicleRules") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetVehicleRules", request)
            log.atInfo().addKeyValue("operation", "GetVehicleRules").log("Handling transport request")
            val outbound0 = summarizeTelemetryReadingsRequest {
                this.context = request.context
                this.details = SummarizeTelemetryReadingsRequestKt.details {
                    this.filters.putAll(request.context.baggageMap)
                }
            }
            val reply0 = telematicsTelemetryReadingQueryService.summarizeTelemetryReadings(outbound0)
            val outbound1 = estimateVehicleGroupRequest {
                this.context = request.context
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.manager = userRef {
                                this.email = request.context.actor.email
                                this.userId = request.context.actor.userId
                            }
                        }))
                    }))
                }
            }
            val reply1 = fleetVehicleGroupCalculationService.estimateVehicleGroup(outbound1)
            getVehicleRulesResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.message = reply1.result.message
                    this.status = reply0.result.status
                }
                this.vehicle = vehicle {
                    this.owner = userRef {
                        this.email = reply1.vehicleGroup.manager.email
                    }
                    this.vehicleId = request.vehicleId
                }
            }
        }

    /** Lints a vehicle. */
    override suspend fun lintVehicles(request: LintVehiclesRequest): LintVehiclesResponse {
        return handleLintVehicles(request)
    }

    /** Normalizes a vehicle. */
    override suspend fun normalizeVehicle(request: NormalizeVehicleRequest): NormalizeVehicleResponse = handleNormalizeVehicle(request)

    /** Previews a vehicle. */
    override suspend fun previewVehicle(request: PreviewVehicleRequest): PreviewVehicleResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleValidationService/PreviewVehicle") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PreviewVehicle", request)
            log.atInfo().addKeyValue("operation", "PreviewVehicle").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicle.vehicleId).log("Recorded audit detail")
            val outbound0 = calculateVehicleGroupRequest {
                this.context = request.context
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.location = postalAddress {
                                this.postalCode = when (request.vehicle.locatorCase) {
                                    Vehicle.LocatorCase.ADDRESS -> request.vehicle.address.postalCode
                                    else -> ""
                                }
                            }
                            this.manager = userRef {
                                this.displayName = request.vehicle.owner.displayName
                                this.userId = request.vehicle.owner.userId
                            }
                        }))
                    }))
                }
            }
            val reply0 = fleetVehicleGroupCalculationService.calculateVehicleGroup(outbound0)
            val outbound1 = recalculateVehicleGroupsRequest {
                this.context = request.context
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.audit = auditInfo {
                                this.createdAt = request.vehicle.audit.createdAt
                                this.etag = request.vehicle.audit.etag
                                this.updatedAt = request.vehicle.audit.updatedAt
                            }
                            this.location = postalAddress {
                                this.recipient = reply0.vehicleGroup.location.recipient
                            }
                        }))
                    }))
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = fleetVehicleGroupCalculationService.recalculateVehicleGroups(envelope1.unpack(RecalculateVehicleGroupsRequest::class.java))
            previewVehicleResponse {
                this.outcome = PreviewVehicleResponseKt.outcome {
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.vehicle = vehicle {
                    this.audit = auditInfo {
                        this.etag = reply1.vehicleGroup.audit.etag
                        this.updatedAt = reply0.vehicleGroup.audit.updatedAt
                    }
                    this.code = request.vehicle.code
                    this.vehicleId = request.vehicleId
                }
            }
        }

    /** Scores a vehicle. */
    override suspend fun scoreVehicle(request: ScoreVehicleRequest): ScoreVehicleResponse {
        return handleScoreVehicle(request)
    }

    /** Validates a vehicle. */
    override suspend fun validateVehicle(request: ValidateVehicleRequest): ValidateVehicleResponse = handleValidateVehicle(request)

    private suspend fun handleCheckVehicleEligibility(request: CheckVehicleEligibilityRequest): CheckVehicleEligibilityResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleValidationService/CheckVehicleEligibility") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CheckVehicleEligibility", request)
            log.atInfo().addKeyValue("operation", "CheckVehicleEligibility").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicle.vehicleId).log("Recorded audit detail")
            val outbound0 = compareVehicleGroupOptionsRequest {
                this.context = request.context
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.manager = userRef {
                                this.email = request.vehicle.owner.email
                            }
                        }))
                    }))
                }
            }
            val reply0 = flowOf(outbound0).map { value -> fleetVehicleGroupCalculationService.compareVehicleGroupOptions(value) }.single()
            val outbound1 = estimateVehicleGroupRequest {
                this.context = request.context
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.audit = auditInfo {
                                this.updatedAt = request.vehicle.audit.updatedAt
                            }
                            this.location = postalAddress {
                                this.recipient = reply0.vehicleGroup.location.recipient
                            }
                            this.manager = userRef {
                                this.userId = reply0.vehicleGroup.manager.userId
                            }
                        }))
                    }))
                }
            }
            val reply1 = withTimeout(5_000) { fleetVehicleGroupCalculationService.estimateVehicleGroup(outbound1) }
            checkVehicleEligibilityResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.vehicle = vehicle {
                    this.audit = auditInfo {
                        this.createdAt = reply0.vehicleGroup.audit.createdAt
                    }
                    this.name = reply1.vehicleGroup.name
                    this.owner = userRef {
                        this.userId = reply1.vehicleGroup.manager.userId
                    }
                    this.specs.putAll(request.vehicle.specsMap)
                    this.vehicleId = request.vehicleId
                }
            }
        }

    private suspend fun handleExplainVehicleDecision(request: ExplainVehicleDecisionRequest): ExplainVehicleDecisionResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleValidationService/ExplainVehicleDecision") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainVehicleDecision", request)
            log.atInfo().addKeyValue("operation", "ExplainVehicleDecision").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicle.vehicleId).log("Recorded audit detail")
            val configure0: ExplainVehicleGroupCalculationRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.audit = auditInfo {
                                this.updatedAt = request.vehicle.audit.updatedAt
                            }
                            this.location = postalAddress {
                                this.region = request.vehicle.address.region
                            }
                        }))
                    }))
                }
            }
            val outbound0 = explainVehicleGroupCalculationRequest(configure0)
            val reply0 = fleetVehicleGroupCalculationService.explainVehicleGroupCalculation(outbound0)
            explainVehicleDecisionResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.status = "ok"
                }
                this.vehicle = vehicle {
                    this.status = request.vehicle.status
                    this.vehicleId = request.vehicleId
                }
            }
        }

    private suspend fun handleLintVehicles(request: LintVehiclesRequest): LintVehiclesResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleValidationService/LintVehicles") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LintVehicles", request)
            log.atInfo().addKeyValue("operation", "LintVehicles").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicle.vehicleId).log("Recorded audit detail")
            val outbound0 = quoteVehicleGroupRequest {}.fleetCopy {
                this.context = request.context
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.location = postalAddress {
                                this.postalCode = request.vehicle.address.postalCode
                            }
                        }))
                    }))
                }
            }
            val reply0 = fleetVehicleGroupCalculationService.quoteVehicleGroup(outbound0)
            lintVehiclesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.vehicle = vehicle {
                    this.code = request.vehicle.code
                    this.owner = userRef {
                        this.displayName = reply0.vehicleGroup.manager.displayName
                    }
                    this.vehicleId = request.vehicleId
                }
            }
        }

    private fun NormalizeVehicleRequest.toCountFeatureFlagsFromNormalizeVehicle0(): CountFeatureFlagsRequest {
        val source = this
        return countFeatureFlagsRequest {
            this.context = source.context
            this.filter = CountFeatureFlagsRequestKt.filter {
                this.region = source.vehicle.address.region
            }
        }
    }

    private suspend fun handleNormalizeVehicle(request: NormalizeVehicleRequest): NormalizeVehicleResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleValidationService/NormalizeVehicle") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "NormalizeVehicle", request)
            log.atInfo().addKeyValue("operation", "NormalizeVehicle").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicle.vehicleId).log("Recorded audit detail")
            val outbound0 = request.toCountFeatureFlagsFromNormalizeVehicle0()
            val reply0 = featureflagsFeatureFlagQueryService.countFeatureFlags(outbound0)
            normalizeVehicleResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
                this.vehicle = vehicle {
                    this.code = request.vehicle.code
                    this.vehicleId = request.vehicleId
                }
            }
        }

    private suspend fun handleScoreVehicle(request: ScoreVehicleRequest): ScoreVehicleResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleValidationService/ScoreVehicle") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScoreVehicle", request)
            log.atInfo().addKeyValue("operation", "ScoreVehicle").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicle.vehicleId).log("Recorded audit detail")
            val outbound0 = calculateVehicleGroupRequest {
                this.context = request.context
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.audit = auditInfo {
                                this.etag = request.vehicle.audit.etag
                            }
                            this.location = postalAddress {
                                this.lines.addAll(request.details.destination.linesList)
                            }
                        }))
                    }))
                }
            }
            val reply0 = fleetVehicleGroupCalculationService.calculateVehicleGroup(WireCopies.json(outbound0))
            scoreVehicleResponse {
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
                this.vehicle = vehicle {
                    this.code = request.vehicle.code
                    this.vehicleId = request.vehicleId
                }
            }
        }

    private data class ValidateVehicle0Command(
        val context: RequestContext,
        val userId: String
    ) {
        fun toRequest(): PeekUserRequest {
            val command = this
            return peekUserRequest {
                this.context = command.context
                this.userId = command.userId
            }
        }
    }

    private suspend fun handleValidateVehicle(request: ValidateVehicleRequest): ValidateVehicleResponse =
        RpcBoundary.run("acme.fleet.v1.VehicleValidationService/ValidateVehicle") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ValidateVehicle", request)
            log.atInfo().addKeyValue("operation", "ValidateVehicle").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.vehicle.vehicleId).log("Recorded audit detail")
            val outbound0 = ValidateVehicle0Command(
                context = request.context,
                userId = request.vehicle.owner.userId
            ).toRequest()
            val reply0 = identityUserQueryService.peekUser(outbound0)
            validateVehicleResponse {
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = reply0.result.status
                }
                this.vehicle = vehicle {
                    this.status = request.vehicle.status
                    this.vehicleId = request.vehicleId
                }
            }
        }
}
