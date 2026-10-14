package com.acme.transport.fuel.grpc

import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.customers.v1.HouseholdQueryServiceGrpcKt
import com.acme.customers.v1.summarizeHouseholdsRequest
import com.acme.fuel.v1.CancelFuelPolicyImportRequest
import com.acme.fuel.v1.CancelFuelPolicyImportResponse
import com.acme.fuel.v1.CancelFuelPolicyImportResponseKt
import com.acme.fuel.v1.FuelPolicyExportServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyHistoryServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyImportServiceGrpcKt
import com.acme.fuel.v1.GetFuelPolicyImportStatusRequest
import com.acme.fuel.v1.GetFuelPolicyImportStatusResponse
import com.acme.fuel.v1.ImportFuelPoliciesRequest
import com.acme.fuel.v1.ImportFuelPoliciesResponse
import com.acme.fuel.v1.ListFuelPolicyExportsRequest
import com.acme.fuel.v1.ListFuelPolicyExportsRequestKt
import com.acme.fuel.v1.ListFuelPolicyImportsRequest
import com.acme.fuel.v1.ListFuelPolicyImportsResponse
import com.acme.fuel.v1.PruneFuelPolicyVersionsRequest
import com.acme.fuel.v1.PruneFuelPolicyVersionsResponse
import com.acme.fuel.v1.RetryFuelPolicyImportRequest
import com.acme.fuel.v1.RetryFuelPolicyImportResponse
import com.acme.fuel.v1.ValidateFuelPolicyImportRequest
import com.acme.fuel.v1.ValidateFuelPolicyImportResponse
import com.acme.fuel.v1.cancelFuelPolicyImportResponse
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.getFuelPolicyImportStatusResponse
import com.acme.fuel.v1.importFuelPoliciesResponse
import com.acme.fuel.v1.listFuelPolicyExportsRequest
import com.acme.fuel.v1.listFuelPolicyImportsResponse
import com.acme.fuel.v1.pruneFuelPolicyVersionsRequest
import com.acme.fuel.v1.retryFuelPolicyImportResponse
import com.acme.fuel.v1.validateFuelPolicyImportResponse
import com.acme.identity.v1.userRef
import com.acme.payroll.v1.ComparePayRunOptionsRequestKt
import com.acme.payroll.v1.PayRunCalculationServiceGrpcKt
import com.acme.payroll.v1.comparePayRunOptionsRequest
import com.acme.transport.fuel.FuelRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RequestFactory
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.yards.v1.ApproveDockRequestKt
import com.acme.yards.v1.DockApprovalServiceGrpcKt
import com.acme.yards.v1.DockComponent
import com.acme.yards.v1.DockLifecycleServiceGrpcKt
import com.acme.yards.v1.ScheduleDockExpiryRequestKt
import com.acme.yards.v1.SubmitDockRequest
import com.acme.yards.v1.SubmitDockResponse
import com.acme.yards.v1.YardMoveReportingServiceGrpcKt
import com.acme.yards.v1.approveDockRequest
import com.acme.yards.v1.dock
import com.acme.yards.v1.dockComponent
import com.acme.yards.v1.generateYardMoveReportRequest
import com.acme.yards.v1.scheduleDockExpiryRequest
import com.acme.yards.v1.submitDockRequest
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Takes fuel policies in bulk from partners and legacy systems. */
class FuelPolicyImportEndpoint(
    private val store: FuelRepository,
    private val workers: CoroutineScope,
    private val customersHouseholdQueryService: HouseholdQueryServiceGrpcKt.HouseholdQueryServiceCoroutineStub,
    private val fuelFuelPolicyExportService: FuelPolicyExportServiceGrpcKt.FuelPolicyExportServiceCoroutineStub,
    private val fuelFuelPolicyHistoryService: FuelPolicyHistoryServiceGrpcKt.FuelPolicyHistoryServiceCoroutineStub,
    private val payrollPayRunCalculationService: PayRunCalculationServiceGrpcKt.PayRunCalculationServiceCoroutineStub,
    private val yardsDockApprovalService: DockApprovalServiceGrpcKt.DockApprovalServiceCoroutineStub,
    private val yardsDockLifecycleService: DockLifecycleServiceGrpcKt.DockLifecycleServiceCoroutineStub,
    private val yardsYardMoveReportingService: YardMoveReportingServiceGrpcKt.YardMoveReportingServiceCoroutineStub,
) : FuelPolicyImportServiceGrpcKt.FuelPolicyImportServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(FuelPolicyImportEndpoint::class.java)

    private val workerForListFuelPolicyImports1 = ReplyMailbox<SubmitDockRequest, SubmitDockResponse>(workers) { payload ->
        withTimeout(5_000) { yardsDockApprovalService.submitDock(payload) }
    }

    private val clientForRetryFuelPolicyImport0 by lazy { yardsYardMoveReportingService }

    /** Cancels a fuel policy. */
    override suspend fun cancelFuelPolicyImport(request: CancelFuelPolicyImportRequest): CancelFuelPolicyImportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyImportService/CancelFuelPolicyImport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CancelFuelPolicyImport", request)
            log.atInfo().addKeyValue("operation", "CancelFuelPolicyImport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = pruneFuelPolicyVersionsRequest {
                this.context = request.context
                this.fuelPolicyId = request.fuelPolicyId
                this.reason = request.reason
            }
            val invoke0: suspend (PruneFuelPolicyVersionsRequest, Metadata) -> PruneFuelPolicyVersionsResponse = fuelFuelPolicyHistoryService::pruneFuelPolicyVersions
            val reply0 = invoke0(outbound0, Metadata())
            cancelFuelPolicyImportResponse {
                this.fuelPolicy = fuelPolicy {
                    this.fuelPolicyId = request.fuelPolicyId
                    this.owner = userRef {
                        this.displayName = request.details.newOwner.displayName
                    }
                }
                this.outcome = CancelFuelPolicyImportResponseKt.outcome {
                    this.effectiveAt = reply0.effectiveAt
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Returns a fuel policy. */
    override suspend fun getFuelPolicyImportStatus(request: GetFuelPolicyImportStatusRequest): GetFuelPolicyImportStatusResponse {
        return handleGetFuelPolicyImportStatus(request)
    }

    /** Takes a stream of fuel policies and answers once at the end. */
    override suspend fun importFuelPolicies(requests: Flow<ImportFuelPoliciesRequest>): ImportFuelPoliciesResponse {
        var answer = ImportFuelPoliciesResponse.getDefaultInstance()
        requests.map { request -> handleImportFuelPolicies(request) }.collect { answer = it }
        return answer
    }

    /** Lists fuel policies a page at a time. */
    override suspend fun listFuelPolicyImports(request: ListFuelPolicyImportsRequest): ListFuelPolicyImportsResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyImportService/ListFuelPolicyImports") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ListFuelPolicyImports", request)
            log.atInfo().addKeyValue("operation", "ListFuelPolicyImports").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = approveDockRequest {
                this.context = request.context
                this.details = ApproveDockRequestKt.details {
                    this.approver = userRef {
                        this.userId = request.context.actor.userId
                    }
                }
                this.dock = dock {
                    this.address = postalAddress {
                        this.region = request.filter.region
                    }
                    this.audit = auditInfo {
                        this.createdAt = request.filter.createdAfter
                    }
                    this.owner = userRef {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = yardsDockApprovalService.approveDock(WireCopies.reflective(outbound0))
            val outbound1 = submitDockRequest {
                this.context = request.context
                this.dock = dock {
                    this.components.putAll(mapOf("primary" to dockComponent {
                        this.description = (reply0.dock.componentsMap["primary"] ?: DockComponent.getDefaultInstance()).description
                    }))
                }
            }
            val reply1 = workerForListFuelPolicyImports1.submit(outbound1)
            listFuelPolicyImportsResponse {
                this.fuelPolicies.addAll(listOf(fuelPolicy {
                    this.owner = userRef {
                        this.displayName = reply1.dock.owner.displayName
                        this.userId = reply0.dock.owner.userId
                    }
                }))
                this.result = operationResult {
                    this.message = reply0.result.message
                    this.status = reply1.outcome.status
                }
            }
        }

    /** Retrys a fuel policy. */
    override suspend fun retryFuelPolicyImport(request: RetryFuelPolicyImportRequest): RetryFuelPolicyImportResponse {
        return handleRetryFuelPolicyImport(request)
    }

    /** Validates a fuel policy. */
    override suspend fun validateFuelPolicyImport(request: ValidateFuelPolicyImportRequest): ValidateFuelPolicyImportResponse = handleValidateFuelPolicyImport(request)

    private suspend fun handleGetFuelPolicyImportStatus(request: GetFuelPolicyImportStatusRequest): GetFuelPolicyImportStatusResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyImportService/GetFuelPolicyImportStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetFuelPolicyImportStatus", request)
            log.atInfo().addKeyValue("operation", "GetFuelPolicyImportStatus").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            getFuelPolicyImportStatusResponse {
                this.fuelPolicy = fuelPolicy {
                    this.fuelPolicyId = request.fuelPolicyId
                }
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleImportFuelPolicies(request: ImportFuelPoliciesRequest): ImportFuelPoliciesResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyImportService/ImportFuelPolicies") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ImportFuelPolicies", request)
            log.atInfo().addKeyValue("operation", "ImportFuelPolicies").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.header.source).log("Recorded audit detail")
            val outbound0 = ListFuelPolicyExportsRequest.newBuilder().apply {
                setContext(request.context)
                setFilter(ListFuelPolicyExportsRequestKt.filter {
                    this.createdAfter = request.record.audit.createdAt
                    this.labels.putAll(request.record.parametersMap)
                    this.ownerId = request.record.owner.userId
                })
            }.build().also { built ->
                log.atDebug().addKeyValue("bytes", built.serializedSize).log("Prepared downstream request")
            }
            val reply0 = outbound0.let { value -> fuelFuelPolicyExportService.listFuelPolicyExports(value) }
            val outbound1 = RequestFactory.create {
                scheduleDockExpiryRequest {
                    this.context = request.context
                    this.details = ScheduleDockExpiryRequestKt.details {
                        this.notify = contactInfo {
                            this.email = request.header.submittedBy.email
                            this.preferredChannel = request.header.submittedBy.preferredChannel
                        }
                    }
                    this.dock = dock {
                        this.audit = auditInfo {
                            this.createdBy = request.record.audit.createdBy
                        }
                        this.owner = userRef {
                            this.email = request.record.owner.email
                        }
                    }
                }
            }
            val reply1 = yardsDockLifecycleService.scheduleDockExpiry(outbound1)
            val outbound2 = comparePayRunOptionsRequest {
                this.context = request.context
                this.details = ComparePayRunOptionsRequestKt.details {
                    this.destination = postalAddress {
                        this.locality = reply1.dock.address.locality
                    }
                }
            }
            val reply2 = ClientRegistry.lookup("acme.payroll.v1.PayRunCalculationService", PayRunCalculationServiceGrpcKt.PayRunCalculationServiceCoroutineStub::class.java).comparePayRunOptions(outbound2)
            importFuelPoliciesResponse {
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.message = reply1.result.message
                    this.status = reply1.outcome.status
                }
            }
        }

    private suspend fun handleRetryFuelPolicyImport(request: RetryFuelPolicyImportRequest): RetryFuelPolicyImportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyImportService/RetryFuelPolicyImport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RetryFuelPolicyImport", request)
            log.atInfo().addKeyValue("operation", "RetryFuelPolicyImport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicy.fuelPolicyId).log("Recorded audit detail")
            val outbound0 = generateYardMoveReportRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
                this.reason = request.fuelPolicy.block.reason
            }
            val reply0 = clientForRetryFuelPolicyImport0.generateYardMoveReport(outbound0)
            retryFuelPolicyImportResponse {
                this.fuelPolicy = fuelPolicy {
                    this.fuelPolicyId = request.fuelPolicyId
                    this.parameters.putAll(request.fuelPolicy.parametersMap)
                }
                this.result = operationResult {
                    this.details.putAll(reply0.result.detailsMap)
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleValidateFuelPolicyImport(request: ValidateFuelPolicyImportRequest): ValidateFuelPolicyImportResponse =
        RpcBoundary.run("acme.fuel.v1.FuelPolicyImportService/ValidateFuelPolicyImport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ValidateFuelPolicyImport", request)
            log.atInfo().addKeyValue("operation", "ValidateFuelPolicyImport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.fuelPolicy.fuelPolicyId).log("Recorded audit detail")
            val outbound0 = summarizeHouseholdsRequest {
                this.annotations.putAll(request.annotationsMap)
                this.context = request.context
            }
            val reply0 = flowOf(outbound0).map { value -> customersHouseholdQueryService.summarizeHouseholds(value) }.single()
            validateFuelPolicyImportResponse {
                this.fuelPolicy = fuelPolicy {
                    this.fuelPolicyId = request.fuelPolicyId
                    this.parameters.putAll(request.fuelPolicy.parametersMap)
                }
                this.result = operationResult {
                    this.status = reply0.result.status
                }
            }
        }
}
