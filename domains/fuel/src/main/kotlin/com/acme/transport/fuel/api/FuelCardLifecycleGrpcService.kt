package com.acme.transport.fuel.api

import com.acme.audit.v1.AuditEventQueryServiceGrpcKt
import com.acme.audit.v1.FindAuditEventsRequest
import com.acme.audit.v1.FindAuditEventsRequestKt
import com.acme.audit.v1.findAuditEventsRequest
import com.acme.authorization.v1.AccessPolicyValidationServiceGrpcKt
import com.acme.authorization.v1.LintAccessPoliciesRequestKt
import com.acme.authorization.v1.lintAccessPoliciesRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.CreateCustomerRequest
import com.acme.customers.v1.CustomerCalculationServiceGrpcKt
import com.acme.customers.v1.CustomerSegmentCalculationServiceGrpcKt
import com.acme.customers.v1.CustomerSegmentValidationServiceGrpcKt
import com.acme.customers.v1.CustomerServiceGrpcKt
import com.acme.customers.v1.EstimateCustomerSegmentResponse
import com.acme.customers.v1.ExplainCustomerSegmentDecisionRequestKt
import com.acme.customers.v1.PreferenceValidationServiceGrpcKt
import com.acme.customers.v1.ValidatePreferenceRequest
import com.acme.customers.v1.ValidatePreferenceRequestKt
import com.acme.customers.v1.copy as customersCopy
import com.acme.customers.v1.createCustomerRequest
import com.acme.customers.v1.customer
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.customerSegment
import com.acme.customers.v1.estimateCustomerSegmentRequest
import com.acme.customers.v1.explainCustomerSegmentDecisionRequest
import com.acme.customers.v1.simulateCustomerRequest
import com.acme.customers.v1.validatePreferenceRequest
import com.acme.deliveries.v1.DeliveryExceptionApprovalServiceGrpcKt
import com.acme.deliveries.v1.EscalateDeliveryExceptionRequest
import com.acme.deliveries.v1.EscalateDeliveryExceptionRequestKt
import com.acme.deliveries.v1.EscalateDeliveryExceptionResponse
import com.acme.deliveries.v1.ProofOfDeliveryWorkflowServiceGrpcKt
import com.acme.deliveries.v1.ResumeProofOfDeliveryWorkflowRequest
import com.acme.deliveries.v1.ResumeProofOfDeliveryWorkflowRequestKt
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.escalateDeliveryExceptionRequest
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.resumeProofOfDeliveryWorkflowRequest
import com.acme.drivers.v1.DriverLicenseCalculationServiceGrpcKt
import com.acme.drivers.v1.compareDriverLicenseOptionsRequest
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverRef
import com.acme.fuel.v1.ActivateFuelCardRequest
import com.acme.fuel.v1.ActivateFuelCardResponse
import com.acme.fuel.v1.ActivateFuelCardResponseKt
import com.acme.fuel.v1.ExpireFuelCardRequest
import com.acme.fuel.v1.ExpireFuelCardResponse
import com.acme.fuel.v1.ExpireFuelCardResponseKt
import com.acme.fuel.v1.FuelCardImportServiceGrpcKt
import com.acme.fuel.v1.FuelCardLifecycleServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionExportServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionImportServiceGrpcKt
import com.acme.fuel.v1.ListFuelCardImportsRequestKt
import com.acme.fuel.v1.MapFuelTransactionImportColumnsRequestKt
import com.acme.fuel.v1.ReinstateFuelCardRequest
import com.acme.fuel.v1.ReinstateFuelCardResponse
import com.acme.fuel.v1.ReinstateFuelCardResponseKt
import com.acme.fuel.v1.RenewFuelCardRequest
import com.acme.fuel.v1.RenewFuelCardResponse
import com.acme.fuel.v1.RenewFuelCardResponseKt
import com.acme.fuel.v1.ScheduleFuelCardExpiryRequest
import com.acme.fuel.v1.ScheduleFuelCardExpiryResponse
import com.acme.fuel.v1.ScheduleFuelTransactionExportRequest
import com.acme.fuel.v1.ScheduleFuelTransactionExportRequestKt
import com.acme.fuel.v1.ScheduleFuelTransactionExportResponse
import com.acme.fuel.v1.SuspendFuelCardRequest
import com.acme.fuel.v1.SuspendFuelCardResponse
import com.acme.fuel.v1.SuspendFuelCardResponseKt
import com.acme.fuel.v1.TerminateFuelCardRequest
import com.acme.fuel.v1.TerminateFuelCardResponse
import com.acme.fuel.v1.TerminateFuelCardResponseKt
import com.acme.fuel.v1.activateFuelCardResponse
import com.acme.fuel.v1.expireFuelCardResponse
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.listFuelCardImportsRequest
import com.acme.fuel.v1.mapFuelTransactionImportColumnsRequest
import com.acme.fuel.v1.reinstateFuelCardResponse
import com.acme.fuel.v1.renewFuelCardResponse
import com.acme.fuel.v1.scheduleFuelCardExpiryResponse
import com.acme.fuel.v1.scheduleFuelTransactionExportRequest
import com.acme.fuel.v1.suspendFuelCardResponse
import com.acme.fuel.v1.terminateFuelCardResponse
import com.acme.identity.v1.CheckUserEligibilityRequest
import com.acme.identity.v1.CheckUserEligibilityRequestKt
import com.acme.identity.v1.ExportUsersRequestKt
import com.acme.identity.v1.UserQueryServiceGrpcKt
import com.acme.identity.v1.UserValidationServiceGrpcKt
import com.acme.identity.v1.checkUserEligibilityRequest
import com.acme.identity.v1.exportUsersRequest
import com.acme.identity.v1.userRef
import com.acme.transport.fuel.FuelRepository
import com.acme.transport.platform.DelegatingPort
import com.acme.transport.platform.OutboundPort
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Submission
import com.acme.transport.platform.WireCopies
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Moves a fuel card between the states of its lifecycle. */
class FuelCardLifecycleGrpcService(
    private val store: FuelRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventQueryService: AuditEventQueryServiceGrpcKt.AuditEventQueryServiceCoroutineStub,
    private val authorizationAccessPolicyValidationService: AccessPolicyValidationServiceGrpcKt.AccessPolicyValidationServiceCoroutineStub,
    private val customersCustomerCalculationService: CustomerCalculationServiceGrpcKt.CustomerCalculationServiceCoroutineStub,
    private val customersCustomerSegmentCalculationService: CustomerSegmentCalculationServiceGrpcKt.CustomerSegmentCalculationServiceCoroutineStub,
    private val customersCustomerSegmentValidationService: CustomerSegmentValidationServiceGrpcKt.CustomerSegmentValidationServiceCoroutineStub,
    private val customersCustomerService: CustomerServiceGrpcKt.CustomerServiceCoroutineStub,
    private val customersPreferenceValidationService: PreferenceValidationServiceGrpcKt.PreferenceValidationServiceCoroutineStub,
    private val deliveriesDeliveryExceptionApprovalService: DeliveryExceptionApprovalServiceGrpcKt.DeliveryExceptionApprovalServiceCoroutineStub,
    private val deliveriesProofOfDeliveryWorkflowService: ProofOfDeliveryWorkflowServiceGrpcKt.ProofOfDeliveryWorkflowServiceCoroutineStub,
    private val driversDriverLicenseCalculationService: DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub,
    private val fuelFuelCardImportService: FuelCardImportServiceGrpcKt.FuelCardImportServiceCoroutineStub,
    private val fuelFuelTransactionExportService: FuelTransactionExportServiceGrpcKt.FuelTransactionExportServiceCoroutineStub,
    private val fuelFuelTransactionImportService: FuelTransactionImportServiceGrpcKt.FuelTransactionImportServiceCoroutineStub,
    private val identityUserQueryService: UserQueryServiceGrpcKt.UserQueryServiceCoroutineStub,
    private val identityUserValidationService: UserValidationServiceGrpcKt.UserValidationServiceCoroutineStub,
) : FuelCardLifecycleServiceGrpcKt.FuelCardLifecycleServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(FuelCardLifecycleGrpcService::class.java)

    /** Activates a fuel card. */
    override suspend fun activateFuelCard(request: ActivateFuelCardRequest): ActivateFuelCardResponse {
        return handleActivateFuelCard(request)
    }

    /** Expires a fuel card. */
    override suspend fun expireFuelCard(request: ExpireFuelCardRequest): ExpireFuelCardResponse = handleExpireFuelCard(request)

    /** Reinstates a fuel card. */
    override suspend fun reinstateFuelCard(request: ReinstateFuelCardRequest): ReinstateFuelCardResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardLifecycleService/ReinstateFuelCard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ReinstateFuelCard", request)
            log.atInfo().addKeyValue("operation", "ReinstateFuelCard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = explainCustomerSegmentDecisionRequest {
                this.context = request.context
                this.customerSegment = customerSegment {
                    this.owner = userRef {
                        this.email = request.details.newOwner.email
                        this.userId = request.details.newOwner.userId
                    }
                }
                this.details = ExplainCustomerSegmentDecisionRequestKt.details {
                    this.contact = contactInfo {
                        this.name = request.details.notifyContact.name
                    }
                }
            }
            val reply0 = customersCustomerSegmentValidationService.explainCustomerSegmentDecision(outbound0)
            reinstateFuelCardResponse {
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.createdAt = reply0.customerSegment.audit.createdAt
                    }
                    this.fuelCardId = request.fuelCardId
                }
                this.outcome = ReinstateFuelCardResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Renews a fuel card. */
    override suspend fun renewFuelCard(request: RenewFuelCardRequest): RenewFuelCardResponse {
        return handleRenewFuelCard(request)
    }

    /** Schedules a fuel card. */
    override suspend fun scheduleFuelCardExpiry(request: ScheduleFuelCardExpiryRequest): ScheduleFuelCardExpiryResponse = handleScheduleFuelCardExpiry(request)

    /** Suspends a fuel card. */
    override suspend fun suspendFuelCard(request: SuspendFuelCardRequest): SuspendFuelCardResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardLifecycleService/SuspendFuelCard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SuspendFuelCard", request)
            log.atInfo().addKeyValue("operation", "SuspendFuelCard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = exportUsersRequest {
                this.context = request.context
                this.filter = ExportUsersRequestKt.filter {
                    this.createdAfter = request.details.effectiveAt
                    this.labels.putAll(request.annotationsMap)
                    this.ownerId = request.fuelCardId
                }
            }
            identityUserQueryService.exportUsers(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            val outbound1 = escalateDeliveryExceptionRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.assignee = userRef {
                        this.email = request.details.notifyContact.email
                    }
                    this.requester = contactInfo {
                        this.name = request.details.notifyContact.name
                        this.preferredChannel = request.details.notifyContact.preferredChannel
                    }
                }
                this.details = EscalateDeliveryExceptionRequestKt.details {
                    this.approver = userRef {
                        this.userId = request.details.newOwner.userId
                    }
                }
            }
            val invoke1: suspend (EscalateDeliveryExceptionRequest, Metadata) -> EscalateDeliveryExceptionResponse = deliveriesDeliveryExceptionApprovalService::escalateDeliveryException
            val reply1 = invoke1(outbound1, Metadata())
            suspendFuelCardResponse {
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.updatedBy = reply1.deliveryException.audit.updatedBy
                    }
                    this.fuelCardId = request.fuelCardId
                }
                this.outcome = SuspendFuelCardResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Terminates a fuel card. */
    override suspend fun terminateFuelCard(request: TerminateFuelCardRequest): TerminateFuelCardResponse {
        return handleTerminateFuelCard(request)
    }

    private suspend fun handleActivateFuelCard(request: ActivateFuelCardRequest): ActivateFuelCardResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardLifecycleService/ActivateFuelCard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ActivateFuelCard", request)
            log.atInfo().addKeyValue("operation", "ActivateFuelCard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = simulateCustomerRequest {
                this.context = request.context
                this.customer = customer {
                    this.relationshipManager = userRef {
                        this.email = request.details.newOwner.email
                    }
                }
            }
            val reply0 = withTimeout(5_000) { customersCustomerCalculationService.simulateCustomer(outbound0) }
            val configure1: ListFuelCardImportsRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.filter = ListFuelCardImportsRequestKt.filter {
                    this.createdAfter = reply0.customer.passport.expiresAt
                    this.labels.putAll(reply0.customer.attributesMap)
                    this.region = reply0.customer.primaryAddress.region
                }
            }
            val outbound1 = listFuelCardImportsRequest(configure1)
            val reply1 = fuelFuelCardImportService.listFuelCardImports(outbound1)
            val outbound2 = compareDriverLicenseOptionsRequest {
                this.context = request.context
                this.driverLicense = driverLicense {
                    this.billingAddress = postalAddress {
                        this.region = reply0.customer.primaryAddress.region
                    }
                }
            }
            val reply2 = driversDriverLicenseCalculationService.compareDriverLicenseOptions(outbound2)
            val outbound3 = mapFuelTransactionImportColumnsRequest {
                this.context = request.context
                this.details = MapFuelTransactionImportColumnsRequestKt.details {
                    this.contact = contactInfo {
                        this.name = reply0.customer.contact.name
                    }
                }
                this.fuelTransaction = fuelTransaction {
                    this.subject = customerRef {
                        this.email = request.details.newOwner.email
                    }
                }
            }
            val reply3 = fuelFuelTransactionImportService.mapFuelTransactionImportColumns(outbound3)
            activateFuelCardResponse {
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.createdBy = reply2.driverLicense.audit.createdBy
                    }
                    this.contact = contactInfo {
                        this.email = reply3.fuelTransaction.subject.email
                    }
                    this.currencyCode = reply0.breakdown.currencyCode
                    this.fuelCardId = request.fuelCardId
                }
                this.outcome = ActivateFuelCardResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.correlationId = reply3.result.correlationId
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    private fun ExpireFuelCardRequest.toFindAuditEventsFromExpireFuelCard1(reply0: EstimateCustomerSegmentResponse): FindAuditEventsRequest {
        val source = this
        return findAuditEventsRequest {
            this.context = source.context
            this.filter = FindAuditEventsRequestKt.filter {
                this.createdAfter = reply0.customerSegment.audit.createdAt
            }
        }
    }

    private suspend fun handleExpireFuelCard(request: ExpireFuelCardRequest): ExpireFuelCardResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardLifecycleService/ExpireFuelCard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExpireFuelCard", request)
            log.atInfo().addKeyValue("operation", "ExpireFuelCard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = estimateCustomerSegmentRequest {}.customersCopy {
                this.context = request.context
                this.customerSegment = customerSegment {
                    this.owner = userRef {
                        this.userId = request.details.newOwner.userId
                    }
                }
            }
            val reply0 = customersCustomerSegmentCalculationService.estimateCustomerSegment(outbound0)
            val outbound1 = request.toFindAuditEventsFromExpireFuelCard1(reply0)
            val reply1 = auditAuditEventQueryService.findAuditEvents(outbound1)
            expireFuelCardResponse {
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.createdBy = reply0.customerSegment.audit.createdBy
                    }
                    this.contact = contactInfo {
                        this.email = reply0.customerSegment.owner.email
                    }
                    this.fuelCardId = request.fuelCardId
                }
                this.outcome = ExpireFuelCardResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.status = reply1.result.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private data class RenewFuelCard2Command(
        val context: RequestContext,
        val detailsContactEmail: String
    ) {
        fun toRequest(): ValidatePreferenceRequest {
            val command = this
            return validatePreferenceRequest {
                this.context = command.context
                this.details = ValidatePreferenceRequestKt.details {
                    this.contact = contactInfo {
                        this.email = command.detailsContactEmail
                    }
                }
            }
        }
    }

    private suspend fun handleRenewFuelCard(request: RenewFuelCardRequest): RenewFuelCardResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardLifecycleService/RenewFuelCard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RenewFuelCard", request)
            log.atInfo().addKeyValue("operation", "RenewFuelCard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = createCustomerRequest {
                this.context = request.context
                this.customer = customer {
                    this.contact = contactInfo {
                        this.name = request.details.notifyContact.name
                    }
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = customersCustomerService.createCustomer(envelope0.unpack(CreateCustomerRequest::class.java))
            val outbound1 = lintAccessPoliciesRequest {
                this.context = request.context
                this.details = LintAccessPoliciesRequestKt.details {
                    this.contact = contactInfo {
                        this.name = reply0.customer.contact.name
                        this.preferredChannel = request.details.notifyContact.preferredChannel
                    }
                }
            }
            val reply1 = authorizationAccessPolicyValidationService.lintAccessPolicies(WireCopies.json(outbound1))
            val outbound2 = RenewFuelCard2Command(
                context = request.context,
                detailsContactEmail = reply0.customer.relationshipManager.email
            ).toRequest()
            val reply2 = customersPreferenceValidationService.validatePreference(outbound2)
            renewFuelCardResponse {
                this.fuelCard = fuelCard {
                    this.audit = auditInfo {
                        this.etag = reply1.accessPolicy.audit.etag
                    }
                    this.contact = contactInfo {
                        this.preferredChannel = reply0.customer.contact.preferredChannel
                    }
                    this.fuelCardId = request.fuelCardId
                    this.holder = driverRef {
                        this.displayName = reply2.preference.owner.displayName
                        this.email = reply1.accessPolicy.owner.email
                    }
                }
                this.outcome = RenewFuelCardResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.message = reply2.outcome.message
                }
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleScheduleFuelCardExpiry(request: ScheduleFuelCardExpiryRequest): ScheduleFuelCardExpiryResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardLifecycleService/ScheduleFuelCardExpiry") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ScheduleFuelCardExpiry", request)
            log.atInfo().addKeyValue("operation", "ScheduleFuelCardExpiry").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelCard.fuelCardId).log("Recorded audit detail")
            val outbound0 = checkUserEligibilityRequest {
                this.context = request.context
                this.details = CheckUserEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.fuelCard.contact.email
                        this.phone = request.fuelCard.contact.phone
                    }
                }
            }
            val command0: Submission<CheckUserEligibilityRequest> = Submission.Live(outbound0)
            val payload0 = when (val command = command0) {
                is Submission.Live -> command.payload
                is Submission.Replay -> command.payload
            }
            val reply0 = identityUserValidationService.checkUserEligibility(payload0)
            val outbound1 = scheduleFuelTransactionExportRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.details = ScheduleFuelTransactionExportRequestKt.details {
                    this.filters.putAll(reply0.result.detailsMap)
                    this.period = reply0.user.preferences.quietHours
                }
                this.reason = request.reason
            }
            val adapter1 = OutboundPort<ScheduleFuelTransactionExportRequest, ScheduleFuelTransactionExportResponse> { payload ->
                fuelFuelTransactionExportService.scheduleFuelTransactionExport(payload)
            }
            val port1 = DelegatingPort(adapter1)
            val reply1 = port1.execute(outbound1)
            scheduleFuelCardExpiryResponse {
                this.fuelCard = fuelCard {
                    this.fuelCardId = request.fuelCardId
                    this.holder = driverRef {
                        this.displayName = reply0.user.relationshipManager.displayName
                    }
                    this.status = request.fuelCard.status
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleTerminateFuelCard(request: TerminateFuelCardRequest): TerminateFuelCardResponse =
        RpcBoundary.run("acme.fuel.v1.FuelCardLifecycleService/TerminateFuelCard") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "TerminateFuelCard", request)
            log.atInfo().addKeyValue("operation", "TerminateFuelCard").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = ResumeProofOfDeliveryWorkflowRequest.newBuilder().apply {
                setContext(request.context)
                setDetails(ResumeProofOfDeliveryWorkflowRequestKt.details {
                    this.notify = contactInfo {
                        this.name = request.details.notifyContact.name
                        this.phone = request.details.notifyContact.phone
                        this.preferredChannel = request.details.notifyContact.preferredChannel
                    }
                })
                setProofOfDelivery(proofOfDelivery {
                    this.counterparty = driverRef {
                        this.displayName = request.details.newOwner.displayName
                    }
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> deliveriesProofOfDeliveryWorkflowService.resumeProofOfDeliveryWorkflow(value) }
            terminateFuelCardResponse {
                this.fuelCard = fuelCard {
                    this.fuelCardId = request.fuelCardId
                }
                this.outcome = TerminateFuelCardResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                    this.message = reply0.outcome.message
                    this.nextSteps.addAll(reply0.outcome.nextStepsList)
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }
}
