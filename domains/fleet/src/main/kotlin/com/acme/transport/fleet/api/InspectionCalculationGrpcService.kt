package com.acme.transport.fleet.api

import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.LintAuditEventsRequestKt
import com.acme.audit.v1.lintAuditEventsRequest
import com.acme.certificates.v1.CertificateCalculationServiceGrpcKt
import com.acme.certificates.v1.CertificateServiceGrpcKt
import com.acme.certificates.v1.WatchCertificatesRequestKt
import com.acme.certificates.v1.certificate
import com.acme.certificates.v1.certificateLine
import com.acme.certificates.v1.estimateCertificateRequest
import com.acme.certificates.v1.watchCertificatesRequest
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.documents.v1.DocumentValidationServiceGrpcKt
import com.acme.documents.v1.checkDocumentEligibilityRequest
import com.acme.documents.v1.document
import com.acme.drivers.v1.driverRef
import com.acme.fleet.v1.CalculateInspectionRequest
import com.acme.fleet.v1.CalculateInspectionResponse
import com.acme.fleet.v1.CompareInspectionOptionsRequest
import com.acme.fleet.v1.CompareInspectionOptionsResponse
import com.acme.fleet.v1.EstimateInspectionRequest
import com.acme.fleet.v1.EstimateInspectionResponse
import com.acme.fleet.v1.EstimateInspectionResponseKt
import com.acme.fleet.v1.ExplainInspectionCalculationRequest
import com.acme.fleet.v1.ExplainInspectionCalculationResponse
import com.acme.fleet.v1.ExplainInspectionCalculationResponseKt
import com.acme.fleet.v1.InspectionCalculationServiceGrpcKt
import com.acme.fleet.v1.NormalizeVehicleRequest
import com.acme.fleet.v1.NormalizeVehicleResponse
import com.acme.fleet.v1.QuoteInspectionRequest
import com.acme.fleet.v1.QuoteInspectionResponse
import com.acme.fleet.v1.QuoteVehicleGroupRequest
import com.acme.fleet.v1.QuoteVehicleGroupResponse
import com.acme.fleet.v1.RecalculateVehicleGroupsRequest
import com.acme.fleet.v1.SimulateInspectionRequest
import com.acme.fleet.v1.SimulateInspectionResponse
import com.acme.fleet.v1.VehicleGroupCalculationServiceGrpcKt
import com.acme.fleet.v1.VehicleValidationServiceGrpcKt
import com.acme.fleet.v1.calculateInspectionResponse
import com.acme.fleet.v1.calculateVehicleGroupRequest
import com.acme.fleet.v1.compareInspectionOptionsResponse
import com.acme.fleet.v1.estimateInspectionResponse
import com.acme.fleet.v1.explainInspectionCalculationResponse
import com.acme.fleet.v1.explainVehicleGroupCalculationRequest
import com.acme.fleet.v1.inspection
import com.acme.fleet.v1.inspectionComment
import com.acme.fleet.v1.normalizeVehicleRequest
import com.acme.fleet.v1.quoteInspectionResponse
import com.acme.fleet.v1.quoteVehicleGroupRequest
import com.acme.fleet.v1.recalculateVehicleGroupsRequest
import com.acme.fleet.v1.simulateInspectionResponse
import com.acme.fleet.v1.vehicle
import com.acme.fleet.v1.vehicleGroup
import com.acme.fuel.v1.FuelCardCalculationServiceGrpcKt
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.simulateFuelCardRequest
import com.acme.identity.v1.UserValidationServiceGrpcKt
import com.acme.identity.v1.scoreUserRequest
import com.acme.identity.v1.user
import com.acme.identity.v1.userRef
import com.acme.metrics.v1.MetricSeries
import com.acme.metrics.v1.MetricSeriesComponent
import com.acme.metrics.v1.MetricSeriesValidationServiceGrpcKt
import com.acme.metrics.v1.NormalizeMetricSeriesRequest
import com.acme.metrics.v1.NormalizeMetricSeriesRequestKt
import com.acme.metrics.v1.NormalizeMetricSeriesResponse
import com.acme.metrics.v1.normalizeMetricSeriesRequest
import com.acme.notifications.v1.NotificationQueryServiceGrpcKt
import com.acme.notifications.v1.ResolveNotificationsRequest
import com.acme.notifications.v1.ResolveNotificationsResponse
import com.acme.notifications.v1.resolveNotificationsRequest
import com.acme.transport.fleet.FleetRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.DockValidationServiceGrpcKt
import com.acme.yards.v1.ExplainDockDecisionRequestKt
import com.acme.yards.v1.ExplainDockDecisionResponse
import com.acme.yards.v1.dock
import com.acme.yards.v1.explainDockDecisionRequest
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Computes the figures of an inspection, such as totals, taxes, and estimates. */
class InspectionCalculationGrpcService(
    private val store: FleetRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventValidationService: AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub,
    private val certificatesCertificateCalculationService: CertificateCalculationServiceGrpcKt.CertificateCalculationServiceCoroutineStub,
    private val certificatesCertificateService: CertificateServiceGrpcKt.CertificateServiceCoroutineStub,
    private val documentsDocumentValidationService: DocumentValidationServiceGrpcKt.DocumentValidationServiceCoroutineStub,
    private val fleetVehicleGroupCalculationService: VehicleGroupCalculationServiceGrpcKt.VehicleGroupCalculationServiceCoroutineStub,
    private val fleetVehicleValidationService: VehicleValidationServiceGrpcKt.VehicleValidationServiceCoroutineStub,
    private val fuelFuelCardCalculationService: FuelCardCalculationServiceGrpcKt.FuelCardCalculationServiceCoroutineStub,
    private val identityUserValidationService: UserValidationServiceGrpcKt.UserValidationServiceCoroutineStub,
    private val metricsMetricSeriesValidationService: MetricSeriesValidationServiceGrpcKt.MetricSeriesValidationServiceCoroutineStub,
    private val notificationsNotificationQueryService: NotificationQueryServiceGrpcKt.NotificationQueryServiceCoroutineStub,
    private val yardsDockValidationService: DockValidationServiceGrpcKt.DockValidationServiceCoroutineStub,
) : InspectionCalculationServiceGrpcKt.InspectionCalculationServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(InspectionCalculationGrpcService::class.java)

    private val invokeCalculateInspection2: suspend (NormalizeVehicleRequest) -> NormalizeVehicleResponse = { payload ->
        fleetVehicleValidationService.normalizeVehicle(payload)
    }

    private val workerForQuoteInspection0 = ReplyMailbox<QuoteVehicleGroupRequest, QuoteVehicleGroupResponse>(workers) { payload ->
        withTimeout(5_000) { fleetVehicleGroupCalculationService.quoteVehicleGroup(payload) }
    }

    /** Calculates an inspection. */
    override suspend fun calculateInspection(request: CalculateInspectionRequest): CalculateInspectionResponse {
        return handleCalculateInspection(request)
    }

    /** Compares an inspection. */
    override suspend fun compareInspectionOptions(request: CompareInspectionOptionsRequest): CompareInspectionOptionsResponse = handleCompareInspectionOptions(request)

    /** Estimates an inspection. */
    override suspend fun estimateInspection(request: EstimateInspectionRequest): EstimateInspectionResponse =
        RpcBoundary.run("acme.fleet.v1.InspectionCalculationService/EstimateInspection") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "EstimateInspection", request)
            log.atInfo().addKeyValue("operation", "EstimateInspection").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.inspection.inspectionId).log("Recorded audit detail")
            val outbound0 = RequestFactory.create {
                scoreUserRequest {
                    this.context = request.context
                    this.user = user {
                        this.audit = auditInfo {
                            this.etag = request.inspection.audit.etag
                        }
                        this.primaryAddress = postalAddress {
                            this.recipient = request.details.destination.recipient
                        }
                        this.relationshipManager = userRef {
                            this.displayName = request.inspection.assignee.displayName
                        }
                    }
                }
            }
            val reply0 = identityUserValidationService.scoreUser(outbound0)
            val outbound1 = calculateVehicleGroupRequest {
                this.context = request.context
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.manager = userRef {
                                this.displayName = request.inspection.assignee.displayName
                            }
                        }))
                    }))
                }
            }
            val reply1 = ClientRegistry.lookup("acme.fleet.v1.VehicleGroupCalculationService", VehicleGroupCalculationServiceGrpcKt.VehicleGroupCalculationServiceCoroutineStub::class.java).calculateVehicleGroup(outbound1)
            estimateInspectionResponse {
                this.breakdown = EstimateInspectionResponseKt.breakdown {
                    this.total = reply1.breakdown.total
                }
                this.inspection = inspection {
                    this.assignee = userRef {
                        this.email = reply0.user.contact.email
                        this.userId = reply1.vehicleGroup.manager.userId
                    }
                    this.customFields.putAll(request.inspection.customFieldsMap)
                    this.inspectionId = request.inspectionId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Explains an inspection. */
    override suspend fun explainInspectionCalculation(request: ExplainInspectionCalculationRequest): ExplainInspectionCalculationResponse {
        return handleExplainInspectionCalculation(request)
    }

    /** Quotes an inspection. */
    override suspend fun quoteInspection(request: QuoteInspectionRequest): QuoteInspectionResponse = handleQuoteInspection(request)

    /** Simulates an inspection. */
    override suspend fun simulateInspection(request: SimulateInspectionRequest): SimulateInspectionResponse =
        RpcBoundary.run("acme.fleet.v1.InspectionCalculationService/SimulateInspection") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SimulateInspection", request)
            log.atInfo().addKeyValue("operation", "SimulateInspection").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.inspection.inspectionId).log("Recorded audit detail")
            val outbound0 = simulateFuelCardRequest {
                this.context = request.context
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.etag = request.inspection.audit.etag
                    }
                    this.holder = driverRef {
                        this.displayName = request.inspection.customer.displayName
                    }
                }
            }
            val reply0 = withTimeout(5_000) { fuelFuelCardCalculationService.simulateFuelCard(outbound0) }
            var reply1 = ExplainDockDecisionResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.inspection.commentsList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound1 = explainDockDecisionRequest {
                                this.context = request.context
                                this.details = ExplainDockDecisionRequestKt.details {
                                    this.contact = contactInfo {
                                        this.email = item.authorEmail
                                    }
                                }
                                this.dock = dock {
                                    this.address = postalAddress {
                                        this.recipient = request.details.destination.recipient
                                    }
                                    this.audit = auditInfo {
                                        this.createdAt = item.createdAt
                                    }
                                    this.owner = userRef {
                                        this.email = reply0.fuelCard.holder.email
                                    }
                                }
                            }
                            yardsDockValidationService.explainDockDecision(outbound1)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply1 = it }
                }
            }
            val outbound2 = lintAuditEventsRequest {
                this.context = request.context
                this.details = LintAuditEventsRequestKt.details {
                    this.contact = contactInfo {
                        this.email = reply0.fuelCard.holder.email
                        this.name = request.inspection.requester.name
                    }
                }
            }
            val reply2 = auditAuditEventValidationService.lintAuditEvents(outbound2)
            val outbound3 = quoteVehicleGroupRequest {
                this.context = request.context
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.location = postalAddress {
                                this.region = reply1.dock.address.region
                            }
                            this.manager = userRef {
                                this.displayName = reply0.fuelCard.holder.displayName
                                this.email = reply1.dock.owner.email
                            }
                        }))
                    }))
                }
            }
            val reply3 = fleetVehicleGroupCalculationService.quoteVehicleGroup(outbound3)
            simulateInspectionResponse {
                this.inspection = inspection {
                    this.comments.addAll(listOf(inspectionComment {
                        this.createdAt = reply3.vehicleGroup.audit.createdAt
                    }))
                    this.inspectionId = request.inspectionId
                    this.requester = contactInfo {
                        this.phone = reply0.fuelCard.contact.phone
                    }
                    this.status = request.inspection.status
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply2.result.referenceIdsList)
                    this.status = reply2.result.status
                }
            }
        }

    private suspend fun handleCalculateInspection(request: CalculateInspectionRequest): CalculateInspectionResponse =
        RpcBoundary.run("acme.fleet.v1.InspectionCalculationService/CalculateInspection") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CalculateInspection", request)
            log.atInfo().addKeyValue("operation", "CalculateInspection").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.inspection.inspectionId).log("Recorded audit detail")
            val outbound0 = recalculateVehicleGroupsRequest {
                this.context = request.context
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.audit = auditInfo {
                                this.etag = request.inspection.audit.etag
                            }
                            this.manager = userRef {
                                this.email = request.inspection.requester.email
                            }
                        }))
                    }))
                }
            }
            val command0: Submission<RecalculateVehicleGroupsRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = fleetVehicleGroupCalculationService.recalculateVehicleGroups(payload0)
            val outbound1 = resolveNotificationsRequest {
                this.context = request.context
            }
            val adapter1 = OutboundPort<ResolveNotificationsRequest, ResolveNotificationsResponse> { payload ->
                notificationsNotificationQueryService.resolveNotifications(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            val outbound2 = normalizeVehicleRequest {
                this.context = request.context
                this.vehicle = vehicle {
                    this.audit = auditInfo {
                        this.createdAt = request.inspection.audit.createdAt
                    }
                    this.owner = userRef {
                        this.email = reply0.vehicleGroup.manager.email
                    }
                }
            }
            val reply2 = invokeCalculateInspection2(outbound2)
            calculateInspectionResponse {
                this.inspection = inspection {
                    this.assignee = userRef {
                        this.displayName = reply0.vehicleGroup.manager.displayName
                    }
                    this.inspectionId = request.inspectionId
                    this.subject = request.inspection.subject
                }
                this.result = operationResult {
                    this.correlationId = reply2.result.correlationId
                    this.status = reply1.result.status
                }
            }
        }

    private suspend fun handleCompareInspectionOptions(request: CompareInspectionOptionsRequest): CompareInspectionOptionsResponse =
        RpcBoundary.run("acme.fleet.v1.InspectionCalculationService/CompareInspectionOptions") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CompareInspectionOptions", request)
            log.atInfo().addKeyValue("operation", "CompareInspectionOptions").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.inspection.inspectionId).log("Recorded audit detail")
            val outbound0 = normalizeMetricSeriesRequest {
                this.context = request.context
                this.details = NormalizeMetricSeriesRequestKt.details {
                    this.contact = contactInfo {
                        this.name = request.inspection.requester.name
                        this.preferredChannel = request.inspection.requester.preferredChannel
                    }
                }
            }
            val invoke0: suspend (NormalizeMetricSeriesRequest, Metadata) -> NormalizeMetricSeriesResponse = metricsMetricSeriesValidationService::normalizeMetricSeries
            val reply0 = invoke0(outbound0, Metadata())
            val outbound1 = estimateCertificateRequest {
                this.certificate = certificate {
                    this.audit = auditInfo {
                        this.createdAt = reply0.metricSeries.audit.createdAt
                    }
                    this.billingAddress = postalAddress {
                        this.recipient = when (reply0.metricSeries.locatorCase) {
                            MetricSeries.LocatorCase.ADDRESS -> reply0.metricSeries.address.recipient
                            else -> ""
                        }
                        this.region = when (reply0.metricSeries.locatorCase) {
                            MetricSeries.LocatorCase.ADDRESS -> reply0.metricSeries.address.region
                            else -> ""
                        }
                    }
                    this.lines.addAll(listOf(certificateLine {
                        this.quantity = (reply0.metricSeries.componentsMap["primary"] ?: MetricSeriesComponent.getDefaultInstance()).quantity
                    }))
                }
                this.context = request.context
            }
            val reply1 = certificatesCertificateCalculationService.estimateCertificate(outbound1)
            compareInspectionOptionsResponse {
                this.inspection = inspection {
                    this.audit = auditInfo {
                        this.createdBy = reply0.metricSeries.audit.createdBy
                        this.updatedAt = reply1.certificate.audit.updatedAt
                        this.updatedBy = reply0.metricSeries.audit.updatedBy
                    }
                    this.inspectionId = request.inspectionId
                    this.requester = request.inspection.requester
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply1.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleExplainInspectionCalculation(request: ExplainInspectionCalculationRequest): ExplainInspectionCalculationResponse =
        RpcBoundary.run("acme.fleet.v1.InspectionCalculationService/ExplainInspectionCalculation") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExplainInspectionCalculation", request)
            log.atInfo().addKeyValue("operation", "ExplainInspectionCalculation").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.inspection.inspectionId).log("Recorded audit detail")
            val outbound0 = explainVehicleGroupCalculationRequest {
                this.context = request.context
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.audit = auditInfo {
                            this.updatedBy = request.inspection.audit.updatedBy
                        }
                        this.children.addAll(listOf(vehicleGroup {
                            this.children.addAll(listOf(vehicleGroup {
                                this.vehicleGroupId = request.inspection.customer.vehicleGroupId
                            }))
                            this.manager = userRef {
                                this.displayName = request.inspection.assignee.displayName
                            }
                        }))
                    }))
                }
            }
            val reply0 = fleetVehicleGroupCalculationService.explainVehicleGroupCalculation(WireCopies.reflective(outbound0))
            explainInspectionCalculationResponse {
                this.inspection = inspection {
                    this.customFields.putAll(request.inspection.customFieldsMap)
                    this.inspectionId = request.inspectionId
                }
                this.outcome = ExplainInspectionCalculationResponseKt.outcome {
                    this.status = reply0.outcome.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleQuoteInspection(request: QuoteInspectionRequest): QuoteInspectionResponse =
        RpcBoundary.run("acme.fleet.v1.InspectionCalculationService/QuoteInspection") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "QuoteInspection", request)
            log.atInfo().addKeyValue("operation", "QuoteInspection").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.inspection.inspectionId).log("Recorded audit detail")
            val outbound0 = quoteVehicleGroupRequest {
                this.context = request.context
                this.vehicleGroup = vehicleGroup {
                    this.children.addAll(listOf(vehicleGroup {
                        this.children.addAll(listOf(vehicleGroup {
                            this.location = postalAddress {
                                this.postalCode = request.details.destination.postalCode
                            }
                            this.manager = userRef {
                                this.userId = request.inspection.assignee.userId
                            }
                        }))
                    }))
                }
            }
            val reply0 = workerForQuoteInspection0.submit(outbound0)
            val outbound1 = watchCertificatesRequest {
                this.context = request.context
                this.filter = WatchCertificatesRequestKt.filter {
                    this.region = request.details.destination.region
                }
                this.subscriber = contactInfo {
                    this.email = request.inspection.assignee.email
                }
            }
            certificatesCertificateService.watchCertificates(outbound1).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound2 = checkDocumentEligibilityRequest {
                this.context = request.context
                this.document = document {
                    this.billingAddress = postalAddress {
                        this.lines.addAll(request.details.destination.linesList)
                        this.locality = reply0.vehicleGroup.location.locality
                        this.postalCode = request.details.destination.postalCode
                        this.region = reply0.vehicleGroup.location.region
                    }
                }
            }
            val reply2 = flowOf(outbound2).map { value -> documentsDocumentValidationService.checkDocumentEligibility(value) }.single()
            quoteInspectionResponse {
                this.inspection = inspection {
                    this.assignee = userRef {
                        this.email = reply0.vehicleGroup.manager.email
                    }
                    this.comments.addAll(listOf(inspectionComment {
                        this.createdAt = reply2.document.audit.createdAt
                    }))
                    this.inspectionId = request.inspectionId
                    this.subject = request.inspection.subject
                }
                this.result = operationResult {
                    this.status = reply2.outcome.status
                }
            }
        }
}
