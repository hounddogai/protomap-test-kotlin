package com.acme.transport.deliveries.api

import com.acme.authorization.v1.AccessPolicyQueryServiceGrpcKt
import com.acme.authorization.v1.FindAccessPoliciesRequest
import com.acme.authorization.v1.FindAccessPoliciesRequestKt
import com.acme.authorization.v1.findAccessPoliciesRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.deliveries.v1.AdvanceProofOfDeliveryWorkflowRequest
import com.acme.deliveries.v1.AdvanceProofOfDeliveryWorkflowResponse
import com.acme.deliveries.v1.CancelProofOfDeliveryWorkflowRequest
import com.acme.deliveries.v1.CancelProofOfDeliveryWorkflowResponse
import com.acme.deliveries.v1.CancelProofOfDeliveryWorkflowResponseKt
import com.acme.deliveries.v1.DeliveryApprovalServiceGrpcKt
import com.acme.deliveries.v1.DeliveryExceptionApprovalServiceGrpcKt
import com.acme.deliveries.v1.DeliveryQueryServiceGrpcKt
import com.acme.deliveries.v1.DeliveryServiceGrpcKt
import com.acme.deliveries.v1.DeliveryValidationServiceGrpcKt
import com.acme.deliveries.v1.InspectProofOfDeliveryWorkflowRequest
import com.acme.deliveries.v1.InspectProofOfDeliveryWorkflowResponse
import com.acme.deliveries.v1.ListProofOfDeliveryWorkflowsRequest
import com.acme.deliveries.v1.ListProofOfDeliveryWorkflowsResponse
import com.acme.deliveries.v1.PauseProofOfDeliveryWorkflowRequest
import com.acme.deliveries.v1.PauseProofOfDeliveryWorkflowResponse
import com.acme.deliveries.v1.ProofOfDeliveryApprovalServiceGrpcKt
import com.acme.deliveries.v1.ProofOfDeliveryQueryServiceGrpcKt
import com.acme.deliveries.v1.ProofOfDeliveryWorkflowServiceGrpcKt
import com.acme.deliveries.v1.RecallDeliveryExceptionRequest
import com.acme.deliveries.v1.RecallDeliveryExceptionResponse
import com.acme.deliveries.v1.ResolveProofOfDeliveriesRequest
import com.acme.deliveries.v1.ResolveProofOfDeliveriesResponse
import com.acme.deliveries.v1.ResumeProofOfDeliveryWorkflowRequest
import com.acme.deliveries.v1.ResumeProofOfDeliveryWorkflowResponse
import com.acme.deliveries.v1.RetryProofOfDeliveryWorkflowRequest
import com.acme.deliveries.v1.RetryProofOfDeliveryWorkflowResponse
import com.acme.deliveries.v1.RetryProofOfDeliveryWorkflowResponseKt
import com.acme.deliveries.v1.SignalProofOfDeliveryWorkflowRequest
import com.acme.deliveries.v1.SignalProofOfDeliveryWorkflowResponse
import com.acme.deliveries.v1.SignalProofOfDeliveryWorkflowResponseKt
import com.acme.deliveries.v1.StartProofOfDeliveryWorkflowRequest
import com.acme.deliveries.v1.StartProofOfDeliveryWorkflowResponse
import com.acme.deliveries.v1.SubmitDeliveryResponse
import com.acme.deliveries.v1.SuggestDeliveriesRequest
import com.acme.deliveries.v1.SuggestDeliveriesRequestKt
import com.acme.deliveries.v1.SuggestDeliveriesResponse
import com.acme.deliveries.v1.SuggestProofOfDeliveriesRequest
import com.acme.deliveries.v1.SuggestProofOfDeliveriesRequestKt
import com.acme.deliveries.v1.ValidateDeliveryRequestKt
import com.acme.deliveries.v1.advanceProofOfDeliveryWorkflowResponse
import com.acme.deliveries.v1.approveProofOfDeliveryRequest
import com.acme.deliveries.v1.batchGetDeliveriesRequest
import com.acme.deliveries.v1.cancelProofOfDeliveryWorkflowResponse
import com.acme.deliveries.v1.delivery
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.deliveryLine
import com.acme.deliveries.v1.deliveryLineTax
import com.acme.deliveries.v1.inspectProofOfDeliveryWorkflowResponse
import com.acme.deliveries.v1.listProofOfDeliveryWorkflowsResponse
import com.acme.deliveries.v1.pauseProofOfDeliveryWorkflowResponse
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.proofOfDeliveryLine
import com.acme.deliveries.v1.recallDeliveryExceptionRequest
import com.acme.deliveries.v1.resolveProofOfDeliveriesRequest
import com.acme.deliveries.v1.resumeProofOfDeliveryWorkflowResponse
import com.acme.deliveries.v1.retryProofOfDeliveryWorkflowResponse
import com.acme.deliveries.v1.signalProofOfDeliveryWorkflowResponse
import com.acme.deliveries.v1.startProofOfDeliveryWorkflowResponse
import com.acme.deliveries.v1.submitDeliveryRequest
import com.acme.deliveries.v1.suggestDeliveriesRequest
import com.acme.deliveries.v1.suggestProofOfDeliveriesRequest
import com.acme.deliveries.v1.validateDeliveryRequest
import com.acme.drivers.v1.BrowseDriverLicensesRequest
import com.acme.drivers.v1.BrowseDriverLicensesRequestKt
import com.acme.drivers.v1.DriverLicenseQueryServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseValidationServiceGrpcKt
import com.acme.drivers.v1.DutyStatusCalculationServiceGrpcKt
import com.acme.drivers.v1.DutyStatusValidationServiceGrpcKt
import com.acme.drivers.v1.ExplainDriverLicenseDecisionResponse
import com.acme.drivers.v1.NormalizeDutyStatusRequestKt
import com.acme.drivers.v1.NormalizeDutyStatusResponse
import com.acme.drivers.v1.RecalculateDutyStatusesRequestKt
import com.acme.drivers.v1.browseDriverLicensesRequest
import com.acme.drivers.v1.copy as driversCopy
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverLicenseRef
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.dutyStatus
import com.acme.drivers.v1.explainDriverLicenseDecisionRequest
import com.acme.drivers.v1.normalizeDutyStatusRequest
import com.acme.drivers.v1.recalculateDutyStatusesRequest
import com.acme.identity.v1.userRef
import com.acme.notifications.v1.NotificationServiceGrpcKt
import com.acme.notifications.v1.SearchNotificationsRequestKt
import com.acme.notifications.v1.searchNotificationsRequest
import com.acme.transport.deliveries.DeliveriesRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.workplaces.v1.BuildingQueryServiceGrpcKt
import com.acme.workplaces.v1.SummarizeBuildingsRequestKt
import com.acme.workplaces.v1.summarizeBuildingsRequest
import com.acme.yards.v1.ApproveYardMoveRequest
import com.acme.yards.v1.RejectYardMoveRequestKt
import com.acme.yards.v1.YardMoveApprovalServiceGrpcKt
import com.acme.yards.v1.approveYardMoveRequest
import com.acme.yards.v1.delegateYardMoveApprovalRequest
import com.acme.yards.v1.rejectYardMoveRequest
import com.acme.yards.v1.yardMove
import com.acme.yards.v1.yardMoveComment
import com.acme.yards.v1.yardMoveSla
import com.google.protobuf.Any as ProtoAny
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Runs the long-lived workflow that takes a proof of delivery from start to finish. */
class ProofOfDeliveryWorkflowGrpcService(
    private val store: DeliveriesRepository,
    private val workers: CoroutineScope,
    private val authorizationAccessPolicyQueryService: AccessPolicyQueryServiceGrpcKt.AccessPolicyQueryServiceCoroutineStub,
    private val deliveriesDeliveryApprovalService: DeliveryApprovalServiceGrpcKt.DeliveryApprovalServiceCoroutineStub,
    private val deliveriesDeliveryExceptionApprovalService: DeliveryExceptionApprovalServiceGrpcKt.DeliveryExceptionApprovalServiceCoroutineStub,
    private val deliveriesDeliveryQueryService: DeliveryQueryServiceGrpcKt.DeliveryQueryServiceCoroutineStub,
    private val deliveriesDeliveryService: DeliveryServiceGrpcKt.DeliveryServiceCoroutineStub,
    private val deliveriesDeliveryValidationService: DeliveryValidationServiceGrpcKt.DeliveryValidationServiceCoroutineStub,
    private val deliveriesProofOfDeliveryApprovalService: ProofOfDeliveryApprovalServiceGrpcKt.ProofOfDeliveryApprovalServiceCoroutineStub,
    private val deliveriesProofOfDeliveryQueryService: ProofOfDeliveryQueryServiceGrpcKt.ProofOfDeliveryQueryServiceCoroutineStub,
    private val driversDriverLicenseQueryService: DriverLicenseQueryServiceGrpcKt.DriverLicenseQueryServiceCoroutineStub,
    private val driversDriverLicenseValidationService: DriverLicenseValidationServiceGrpcKt.DriverLicenseValidationServiceCoroutineStub,
    private val driversDutyStatusCalculationService: DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineStub,
    private val driversDutyStatusValidationService: DutyStatusValidationServiceGrpcKt.DutyStatusValidationServiceCoroutineStub,
    private val notificationsNotificationService: NotificationServiceGrpcKt.NotificationServiceCoroutineStub,
    private val workplacesBuildingQueryService: BuildingQueryServiceGrpcKt.BuildingQueryServiceCoroutineStub,
    private val yardsYardMoveApprovalService: YardMoveApprovalServiceGrpcKt.YardMoveApprovalServiceCoroutineStub,
) : ProofOfDeliveryWorkflowServiceGrpcKt.ProofOfDeliveryWorkflowServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(ProofOfDeliveryWorkflowGrpcService::class.java)

    private val invokeAdvanceProofOfDeliveryWorkflow0: suspend (ResolveProofOfDeliveriesRequest) -> ResolveProofOfDeliveriesResponse = { payload ->
        deliveriesProofOfDeliveryQueryService.resolveProofOfDeliveries(payload)
    }

    private val workerForPauseProofOfDeliveryWorkflow1 = ReplyMailbox<RecallDeliveryExceptionRequest, RecallDeliveryExceptionResponse>(workers) { payload ->
        withTimeout(5_000) { deliveriesDeliveryExceptionApprovalService.recallDeliveryException(payload) }
    }

    private val clientForResumeProofOfDeliveryWorkflow0 by lazy { yardsYardMoveApprovalService }

    /** Advances a proof of delivery. */
    override suspend fun advanceProofOfDeliveryWorkflow(request: AdvanceProofOfDeliveryWorkflowRequest): AdvanceProofOfDeliveryWorkflowResponse {
        return handleAdvanceProofOfDeliveryWorkflow(request)
    }

    /** Cancels a proof of delivery. */
    override suspend fun cancelProofOfDeliveryWorkflow(request: CancelProofOfDeliveryWorkflowRequest): CancelProofOfDeliveryWorkflowResponse = handleCancelProofOfDeliveryWorkflow(request)

    /** Returns a proof of delivery. */
    override suspend fun inspectProofOfDeliveryWorkflow(request: InspectProofOfDeliveryWorkflowRequest): InspectProofOfDeliveryWorkflowResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryWorkflowService/InspectProofOfDeliveryWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "InspectProofOfDeliveryWorkflow", request)
            log.atInfo().addKeyValue("operation", "InspectProofOfDeliveryWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            inspectProofOfDeliveryWorkflowResponse {
                this.proofOfDelivery = proofOfDelivery {
                    this.proofOfDeliveryId = request.proofOfDeliveryId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Lists proof of deliveries a page at a time. */
    override suspend fun listProofOfDeliveryWorkflows(request: ListProofOfDeliveryWorkflowsRequest): ListProofOfDeliveryWorkflowsResponse {
        return handleListProofOfDeliveryWorkflows(request)
    }

    /** Pauses a proof of delivery. */
    override suspend fun pauseProofOfDeliveryWorkflow(request: PauseProofOfDeliveryWorkflowRequest): PauseProofOfDeliveryWorkflowResponse = handlePauseProofOfDeliveryWorkflow(request)

    /** Resumes a proof of delivery. */
    override suspend fun resumeProofOfDeliveryWorkflow(request: ResumeProofOfDeliveryWorkflowRequest): ResumeProofOfDeliveryWorkflowResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryWorkflowService/ResumeProofOfDeliveryWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ResumeProofOfDeliveryWorkflow", request)
            log.atInfo().addKeyValue("operation", "ResumeProofOfDeliveryWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.proofOfDelivery.proofOfDeliveryId).log("Recorded audit detail")
            val outbound0 = delegateYardMoveApprovalRequest {
                this.context = request.context
                this.yardMove = yardMove {
                    this.audit = auditInfo {
                        this.createdBy = request.proofOfDelivery.audit.createdBy
                        this.updatedAt = request.proofOfDelivery.audit.updatedAt
                    }
                    this.comments.addAll(listOf(yardMoveComment {
                        this.createdAt = request.proofOfDelivery.audit.createdAt
                    }))
                    this.sla = yardMoveSla {
                        this.dueAt = request.proofOfDelivery.dueAt
                    }
                }
            }
            val reply0 = clientForResumeProofOfDeliveryWorkflow0.delegateYardMoveApproval(outbound0)
            val outbound1 = recalculateDutyStatusesRequest {
                this.context = request.context
                this.details = RecalculateDutyStatusesRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = request.proofOfDelivery.billingAddress.countryCode
                    }
                }
            }
            val reply1 = flowOf(outbound1).map { value -> driversDutyStatusCalculationService.recalculateDutyStatuses(value) }.single()
            val outbound2 = validateDeliveryRequest {
                this.context = request.context
                this.details = ValidateDeliveryRequestKt.details {
                    this.contact = contactInfo {
                        this.preferredChannel = request.details.notify.preferredChannel
                    }
                }
            }
            val reply2 = withTimeout(5_000) { deliveriesDeliveryValidationService.validateDelivery(outbound2) }
            var reply3 = NormalizeDutyStatusResponse.getDefaultInstance()
            request.proofOfDelivery.linesList.asFlow().map { item ->
                val outbound3 = normalizeDutyStatusRequest {
                    this.context = request.context
                    this.dutyStatus = dutyStatus {
                        this.attributes.putAll(item.attributesMap)
                    }
                }
                driversDutyStatusValidationService.normalizeDutyStatus(outbound3)
            }.collect { reply3 = it }
            resumeProofOfDeliveryWorkflowResponse {
                this.proofOfDelivery = proofOfDelivery {
                    this.audit = auditInfo {
                        this.updatedBy = reply2.delivery.audit.updatedBy
                    }
                    this.number = reply1.dutyStatus.subject.number
                    this.proofOfDeliveryId = request.proofOfDeliveryId
                    this.status = request.proofOfDelivery.status
                }
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }

    /** Retrys a proof of delivery. */
    override suspend fun retryProofOfDeliveryWorkflow(request: RetryProofOfDeliveryWorkflowRequest): RetryProofOfDeliveryWorkflowResponse {
        return handleRetryProofOfDeliveryWorkflow(request)
    }

    /** Signals a proof of delivery. */
    override suspend fun signalProofOfDeliveryWorkflow(request: SignalProofOfDeliveryWorkflowRequest): SignalProofOfDeliveryWorkflowResponse = handleSignalProofOfDeliveryWorkflow(request)

    /** Starts a proof of delivery. */
    override suspend fun startProofOfDeliveryWorkflow(request: StartProofOfDeliveryWorkflowRequest): StartProofOfDeliveryWorkflowResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryWorkflowService/StartProofOfDeliveryWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "StartProofOfDeliveryWorkflow", request)
            log.atInfo().addKeyValue("operation", "StartProofOfDeliveryWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.proofOfDelivery.proofOfDeliveryId).log("Recorded audit detail")
            val outbound0 = approveProofOfDeliveryRequest {
                this.context = request.context
                this.proofOfDelivery = proofOfDelivery {
                    this.billingAddress = postalAddress {
                        this.postalCode = request.proofOfDelivery.billingAddress.postalCode
                    }
                    this.counterparty = driverRef {
                        this.displayName = request.proofOfDelivery.counterparty.displayName
                    }
                }
            }
            val reply0 = deliveriesProofOfDeliveryApprovalService.approveProofOfDelivery(outbound0)
            val outbound1 = findAccessPoliciesRequest {
                this.context = request.context
                this.filter = FindAccessPoliciesRequestKt.filter {
                    this.region = request.proofOfDelivery.billingAddress.region
                }
            }
            val envelope1 = ProtoAny.pack(outbound1)
            val reply1 = authorizationAccessPolicyQueryService.findAccessPolicies(envelope1.unpack(FindAccessPoliciesRequest::class.java))
            val outbound2 = searchNotificationsRequest {
                this.context = request.context
                this.filter = SearchNotificationsRequestKt.filter {
                    this.createdAfter = request.proofOfDelivery.audit.createdAt
                    this.labels.putAll(reply0.proofOfDelivery.metadataMap)
                    this.ownerId = request.proofOfDelivery.counterparty.driverId
                    this.region = reply0.proofOfDelivery.billingAddress.region
                }
            }
            val reply2 = notificationsNotificationService.searchNotifications(WireCopies.json(outbound2))
            val outbound3 = StartProofOfDeliveryWorkflow3Command(
                context = request.context,
                yardMoveRequesterEmail = request.details.notify.email
            ).toRequest()
            val reply3 = yardsYardMoveApprovalService.approveYardMove(outbound3)
            startProofOfDeliveryWorkflowResponse {
                this.proofOfDelivery = proofOfDelivery {
                    this.audit = auditInfo {
                        this.updatedBy = reply0.proofOfDelivery.audit.updatedBy
                    }
                    this.proofOfDeliveryId = request.proofOfDeliveryId
                    this.total = request.proofOfDelivery.total
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.referenceIds.addAll(reply2.result.referenceIdsList)
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleAdvanceProofOfDeliveryWorkflow(request: AdvanceProofOfDeliveryWorkflowRequest): AdvanceProofOfDeliveryWorkflowResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryWorkflowService/AdvanceProofOfDeliveryWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "AdvanceProofOfDeliveryWorkflow", request)
            log.atInfo().addKeyValue("operation", "AdvanceProofOfDeliveryWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.proofOfDelivery.proofOfDeliveryId).log("Recorded audit detail")
            val outbound0 = resolveProofOfDeliveriesRequest {
                this.context = request.context
            }
            val reply0 = invokeAdvanceProofOfDeliveryWorkflow0(outbound0)
            val outbound1 = suggestDeliveriesRequest {
                this.context = request.context
                this.filter = SuggestDeliveriesRequestKt.filter {
                    this.createdAfter = request.proofOfDelivery.audit.createdAt
                    this.region = request.proofOfDelivery.billingAddress.region
                }
            }
            val invoke1: suspend (SuggestDeliveriesRequest, Metadata) -> SuggestDeliveriesResponse = deliveriesDeliveryQueryService::suggestDeliveries
            val reply1 = invoke1(outbound1, Metadata())
            advanceProofOfDeliveryWorkflowResponse {
                this.proofOfDelivery = proofOfDelivery {
                    this.proofOfDeliveryId = request.proofOfDeliveryId
                    this.status = request.proofOfDelivery.status
                }
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.message = reply0.result.message
                    this.status = reply1.result.status
                }
            }
        }

    private suspend fun handleCancelProofOfDeliveryWorkflow(request: CancelProofOfDeliveryWorkflowRequest): CancelProofOfDeliveryWorkflowResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryWorkflowService/CancelProofOfDeliveryWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CancelProofOfDeliveryWorkflow", request)
            log.atInfo().addKeyValue("operation", "CancelProofOfDeliveryWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.proofOfDelivery.proofOfDeliveryId).log("Recorded audit detail")
            val outbound0 = SuggestProofOfDeliveriesRequest.newBuilder().apply {
                setContext(request.context)
                setFilter(SuggestProofOfDeliveriesRequestKt.filter {
                    this.region = request.proofOfDelivery.billingAddress.region
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> deliveriesProofOfDeliveryQueryService.suggestProofOfDeliveries(value) }
            val outbound1 = RequestFactory.create {
                normalizeDutyStatusRequest {
                    this.context = request.context
                    this.details = NormalizeDutyStatusRequestKt.details {
                        this.contact = contactInfo {
                            this.email = request.details.notify.email
                            this.phone = request.details.notify.phone
                        }
                    }
                    this.dutyStatus = dutyStatus {
                        this.subject = driverLicenseRef {
                            this.displayName = request.proofOfDelivery.counterparty.displayName
                            this.number = request.proofOfDelivery.number
                        }
                    }
                }
            }
            val reply1 = driversDutyStatusValidationService.normalizeDutyStatus(outbound1)
            cancelProofOfDeliveryWorkflowResponse {
                this.outcome = CancelProofOfDeliveryWorkflowResponseKt.outcome {
                    this.status = reply0.result.status
                }
                this.proofOfDelivery = proofOfDelivery {
                    this.lines.addAll(listOf(proofOfDeliveryLine {
                        this.attributes.putAll(reply1.dutyStatus.attributesMap)
                    }))
                    this.metadata.putAll(request.proofOfDelivery.metadataMap)
                    this.proofOfDeliveryId = request.proofOfDeliveryId
                }
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = reply0.result.status
                }
            }
        }

    private suspend fun handleListProofOfDeliveryWorkflows(request: ListProofOfDeliveryWorkflowsRequest): ListProofOfDeliveryWorkflowsResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryWorkflowService/ListProofOfDeliveryWorkflows") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListProofOfDeliveryWorkflows", request)
            log.atInfo().addKeyValue("operation", "ListProofOfDeliveryWorkflows").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = summarizeBuildingsRequest {
                this.annotations.putAll(request.context.baggageMap)
                this.context = request.context
                this.details = SummarizeBuildingsRequestKt.details {
                    this.filters.putAll(request.filter.labelsMap)
                }
            }
            val reply0 = ClientRegistry.lookup("acme.workplaces.v1.BuildingQueryService", BuildingQueryServiceGrpcKt.BuildingQueryServiceCoroutineStub::class.java).summarizeBuildings(outbound0)
            listProofOfDeliveryWorkflowsResponse {
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handlePauseProofOfDeliveryWorkflow(request: PauseProofOfDeliveryWorkflowRequest): PauseProofOfDeliveryWorkflowResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryWorkflowService/PauseProofOfDeliveryWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "PauseProofOfDeliveryWorkflow", request)
            log.atInfo().addKeyValue("operation", "PauseProofOfDeliveryWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.proofOfDelivery.proofOfDeliveryId).log("Recorded audit detail")
            var reply0 = SubmitDeliveryResponse.getDefaultInstance()
            coroutineScope {
                val permits = Semaphore(8)
                request.proofOfDelivery.linesList.chunked(32).forEach { batch ->
                    batch.map { item ->
                        async { permits.withPermit {
                            val outbound0 = submitDeliveryRequest {
                                this.context = request.context
                                this.delivery = delivery {
                                    this.lines.addAll(listOf(deliveryLine {
                                        this.lineId = item.lineId
                                        this.tax = deliveryLineTax {
                                            this.amount = item.amount
                                        }
                                    }))
                                }
                            }
                            deliveriesDeliveryApprovalService.submitDelivery(outbound0)
                        } }
                    }.awaitAll().lastOrNull()?.let { reply0 = it }
                }
            }
            val outbound1 = recallDeliveryExceptionRequest {
                this.context = request.context
                this.deliveryException = deliveryException {
                    this.requester = contactInfo {
                        this.email = request.details.notify.email
                    }
                }
            }
            val reply1 = workerForPauseProofOfDeliveryWorkflow1.submit(outbound1)
            pauseProofOfDeliveryWorkflowResponse {
                this.proofOfDelivery = proofOfDelivery {
                    this.counterparty = driverRef {
                        this.email = reply1.deliveryException.assignee.email
                    }
                    this.proofOfDeliveryId = request.proofOfDeliveryId
                    this.total = request.proofOfDelivery.total
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleRetryProofOfDeliveryWorkflow(request: RetryProofOfDeliveryWorkflowRequest): RetryProofOfDeliveryWorkflowResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryWorkflowService/RetryProofOfDeliveryWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RetryProofOfDeliveryWorkflow", request)
            log.atInfo().addKeyValue("operation", "RetryProofOfDeliveryWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.proofOfDelivery.proofOfDeliveryId).log("Recorded audit detail")
            val outbound0 = batchGetDeliveriesRequest {
                this.context = request.context
            }
            val reply0 = deliveriesDeliveryService.batchGetDeliveries(outbound0)
            val outbound1 = rejectYardMoveRequest {
                this.context = request.context
                this.details = RejectYardMoveRequestKt.details {
                    this.approver = userRef {
                        this.displayName = request.proofOfDelivery.counterparty.displayName
                    }
                }
                this.yardMove = yardMove {
                    this.requester = contactInfo {
                        this.phone = request.details.notify.phone
                    }
                }
            }
            val reply1 = yardsYardMoveApprovalService.rejectYardMove(outbound1)
            retryProofOfDeliveryWorkflowResponse {
                this.outcome = RetryProofOfDeliveryWorkflowResponseKt.outcome {
                    this.effectiveAt = reply1.outcome.effectiveAt
                    this.message = reply1.outcome.message
                }
                this.proofOfDelivery = proofOfDelivery {
                    this.billingAddress = request.proofOfDelivery.billingAddress
                    this.proofOfDeliveryId = request.proofOfDeliveryId
                }
                this.result = operationResult {
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = "ok"
                }
            }
        }

    private fun SignalProofOfDeliveryWorkflowRequest.toBrowseDriverLicensesFromSignalProofOfDeliveryWorkflow1(reply0: ExplainDriverLicenseDecisionResponse): BrowseDriverLicensesRequest {
        val source = this
        return browseDriverLicensesRequest {
            this.context = source.context
            this.filter = BrowseDriverLicensesRequestKt.filter {
                this.createdAfter = reply0.outcome.effectiveAt
                this.labels.putAll(reply0.result.detailsMap)
                this.ownerId = reply0.outcome.referenceId
                this.region = source.proofOfDelivery.billingAddress.region
            }
        }
    }

    private suspend fun handleSignalProofOfDeliveryWorkflow(request: SignalProofOfDeliveryWorkflowRequest): SignalProofOfDeliveryWorkflowResponse =
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryWorkflowService/SignalProofOfDeliveryWorkflow") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SignalProofOfDeliveryWorkflow", request)
            log.atInfo().addKeyValue("operation", "SignalProofOfDeliveryWorkflow").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.proofOfDelivery.proofOfDeliveryId).log("Recorded audit detail")
            val outbound0 = explainDriverLicenseDecisionRequest {}.driversCopy {
                this.context = request.context
                this.driverLicense = driverLicense {
                    this.billingAddress = postalAddress {
                        this.countryCode = request.proofOfDelivery.billingAddress.countryCode
                    }
                }
            }
            val reply0 = driversDriverLicenseValidationService.explainDriverLicenseDecision(outbound0)
            val outbound1 = request.toBrowseDriverLicensesFromSignalProofOfDeliveryWorkflow1(reply0)
            val reply1 = driversDriverLicenseQueryService.browseDriverLicenses(outbound1)
            signalProofOfDeliveryWorkflowResponse {
                this.outcome = SignalProofOfDeliveryWorkflowResponseKt.outcome {
                    this.nextSteps.addAll(reply0.outcome.nextStepsList)
                }
                this.proofOfDelivery = proofOfDelivery {
                    this.proofOfDeliveryId = request.proofOfDeliveryId
                    this.total = request.proofOfDelivery.total
                }
                this.result = operationResult {
                    this.message = reply1.result.message
                    this.status = "ok"
                }
            }
        }

    private data class StartProofOfDeliveryWorkflow3Command(
        val context: RequestContext,
        val yardMoveRequesterEmail: String
    ) {
        fun toRequest(): ApproveYardMoveRequest {
            val command = this
            return approveYardMoveRequest {
                this.context = command.context
                this.yardMove = yardMove {
                    this.requester = contactInfo {
                        this.email = command.yardMoveRequesterEmail
                    }
                }
            }
        }
    }
}
