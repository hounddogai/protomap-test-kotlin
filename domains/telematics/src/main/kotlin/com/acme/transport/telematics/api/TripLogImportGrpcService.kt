package com.acme.transport.telematics.api

import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.CheckAuditEventEligibilityRequestKt
import com.acme.audit.v1.checkAuditEventEligibilityRequest
import com.acme.common.v1.RequestContext
import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.compliance.v1.CalculateComplianceCaseRequest
import com.acme.compliance.v1.CalculateComplianceCaseRequestKt
import com.acme.compliance.v1.ComplianceCaseCalculationServiceGrpcKt
import com.acme.compliance.v1.calculateComplianceCaseRequest
import com.acme.compliance.v1.complianceCase
import com.acme.contracts.v1.ContractQueryServiceGrpcKt
import com.acme.contracts.v1.CountContractsRequestKt
import com.acme.contracts.v1.countContractsRequest
import com.acme.customers.v1.ConsentRecordCalculationServiceGrpcKt
import com.acme.customers.v1.CountHouseholdsRequestKt
import com.acme.customers.v1.EstimateConsentRecordRequestKt
import com.acme.customers.v1.ExportHouseholdsRequestKt
import com.acme.customers.v1.HouseholdQueryServiceGrpcKt
import com.acme.customers.v1.consentRecord
import com.acme.customers.v1.copy as customersCopy
import com.acme.customers.v1.countHouseholdsRequest
import com.acme.customers.v1.customerRef
import com.acme.customers.v1.estimateConsentRecordRequest
import com.acme.customers.v1.exportHouseholdsRequest
import com.acme.customers.v1.householdRef
import com.acme.identity.v1.userRef
import com.acme.maintenance.v1.ApproveServiceScheduleRequest
import com.acme.maintenance.v1.ApproveServiceScheduleRequestKt
import com.acme.maintenance.v1.ServiceScheduleApprovalServiceGrpcKt
import com.acme.maintenance.v1.approveServiceScheduleRequest
import com.acme.maintenance.v1.serviceSchedule
import com.acme.maintenance.v1.serviceScheduleParticipant
import com.acme.telematics.v1.CancelTripLogImportRequest
import com.acme.telematics.v1.CancelTripLogImportResponse
import com.acme.telematics.v1.CancelTripLogImportResponseKt
import com.acme.telematics.v1.GetTripLogImportStatusRequest
import com.acme.telematics.v1.GetTripLogImportStatusResponse
import com.acme.telematics.v1.ImportTripLogsRequest
import com.acme.telematics.v1.ImportTripLogsResponse
import com.acme.telematics.v1.MapTripLogImportColumnsRequest
import com.acme.telematics.v1.MapTripLogImportColumnsResponse
import com.acme.telematics.v1.MapTripLogImportColumnsResponseKt
import com.acme.telematics.v1.RetryTripLogImportRequest
import com.acme.telematics.v1.RetryTripLogImportResponse
import com.acme.telematics.v1.RetryTripLogImportResponseKt
import com.acme.telematics.v1.ScheduleTripLogExportRequest
import com.acme.telematics.v1.TripLogExportServiceGrpcKt
import com.acme.telematics.v1.TripLogImportServiceGrpcKt
import com.acme.telematics.v1.ValidateTripLogImportRequest
import com.acme.telematics.v1.ValidateTripLogImportResponse
import com.acme.telematics.v1.ValidateTripLogImportResponseKt
import com.acme.telematics.v1.cancelTripLogImportResponse
import com.acme.telematics.v1.getTripLogImportStatusResponse
import com.acme.telematics.v1.importTripLogsResponse
import com.acme.telematics.v1.mapTripLogImportColumnsResponse
import com.acme.telematics.v1.retryTripLogImportResponse
import com.acme.telematics.v1.scheduleTripLogExportRequest
import com.acme.telematics.v1.tripLog
import com.acme.telematics.v1.tripLogLine
import com.acme.telematics.v1.validateTripLogImportResponse
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.transport.telematics.TelematicsRepository
import com.acme.yards.v1.YardMoveLifecycleServiceGrpcKt
import com.acme.yards.v1.scheduleYardMoveExpiryRequest
import com.acme.yards.v1.yardMove
import com.google.protobuf.Any as ProtoAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import org.slf4j.LoggerFactory

/** Takes trip logs in bulk from partners and legacy systems. */
class TripLogImportGrpcService(
    private val store: TelematicsRepository,
    private val workers: CoroutineScope,
    private val auditAuditEventValidationService: AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub,
    private val complianceComplianceCaseCalculationService: ComplianceCaseCalculationServiceGrpcKt.ComplianceCaseCalculationServiceCoroutineStub,
    private val contractsContractQueryService: ContractQueryServiceGrpcKt.ContractQueryServiceCoroutineStub,
    private val customersConsentRecordCalculationService: ConsentRecordCalculationServiceGrpcKt.ConsentRecordCalculationServiceCoroutineStub,
    private val customersHouseholdQueryService: HouseholdQueryServiceGrpcKt.HouseholdQueryServiceCoroutineStub,
    private val maintenanceServiceScheduleApprovalService: ServiceScheduleApprovalServiceGrpcKt.ServiceScheduleApprovalServiceCoroutineStub,
    private val telematicsTripLogExportService: TripLogExportServiceGrpcKt.TripLogExportServiceCoroutineStub,
    private val yardsYardMoveLifecycleService: YardMoveLifecycleServiceGrpcKt.YardMoveLifecycleServiceCoroutineStub,
) : TripLogImportServiceGrpcKt.TripLogImportServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(TripLogImportGrpcService::class.java)

    /** Cancels a trip log. */
    override suspend fun cancelTripLogImport(request: CancelTripLogImportRequest): CancelTripLogImportResponse {
        return handleCancelTripLogImport(request)
    }

    /** Returns a trip log. */
    override suspend fun getTripLogImportStatus(request: GetTripLogImportStatusRequest): GetTripLogImportStatusResponse = handleGetTripLogImportStatus(request)

    /** Takes a stream of trip logs and answers once at the end. */
    override suspend fun importTripLogs(requests: Flow<ImportTripLogsRequest>): ImportTripLogsResponse {
        var answer = ImportTripLogsResponse.getDefaultInstance()
        requests.map { request -> handleImportTripLogs(request) }.collect { answer = it }
        return answer
    }

    /** Maps a trip log. */
    override suspend fun mapTripLogImportColumns(request: MapTripLogImportColumnsRequest): MapTripLogImportColumnsResponse {
        return handleMapTripLogImportColumns(request)
    }

    /** Retrys a trip log. */
    override suspend fun retryTripLogImport(request: RetryTripLogImportRequest): RetryTripLogImportResponse = handleRetryTripLogImport(request)

    /** Validates a trip log. */
    override suspend fun validateTripLogImport(request: ValidateTripLogImportRequest): ValidateTripLogImportResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogImportService/ValidateTripLogImport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ValidateTripLogImport", request)
            log.atInfo().addKeyValue("operation", "ValidateTripLogImport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLog.number).log("Recorded audit detail")
            val outbound0 = calculateComplianceCaseRequest {
                this.complianceCase = complianceCase {
                    this.audit = auditInfo {
                        this.createdBy = request.tripLog.audit.createdBy
                    }
                }
                this.context = request.context
                this.details = CalculateComplianceCaseRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.tripLog.billingAddress.region
                    }
                }
            }
            val envelope0 = ProtoAny.pack(outbound0)
            val reply0 = complianceComplianceCaseCalculationService.calculateComplianceCase(envelope0.unpack(CalculateComplianceCaseRequest::class.java))
            val outbound1 = checkAuditEventEligibilityRequest {
                this.context = request.context
                this.details = CheckAuditEventEligibilityRequestKt.details {
                    this.contact = contactInfo {
                        this.email = reply0.complianceCase.assignee.email
                        this.name = request.details.contact.name
                        this.preferredChannel = request.details.contact.preferredChannel
                    }
                }
            }
            val reply1 = auditAuditEventValidationService.checkAuditEventEligibility(WireCopies.json(outbound1))
            val outbound2 = ValidateTripLogImport2Command(
                context = request.context,
                reason = request.reason,
                tripLogId = request.tripLog.tripLogId,
                annotations = request.annotationsMap
            ).toRequest()
            val reply2 = telematicsTripLogExportService.scheduleTripLogExport(outbound2)
            validateTripLogImportResponse {
                this.outcome = ValidateTripLogImportResponseKt.outcome {
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.correlationId = reply0.result.correlationId
                    this.details.putAll(reply1.result.detailsMap)
                    this.status = reply1.result.status
                }
                this.tripLog = tripLog {
                    this.dueAt = request.tripLog.dueAt
                    this.tripLogId = request.tripLogId
                }
            }
        }

    private suspend fun handleCancelTripLogImport(request: CancelTripLogImportRequest): CancelTripLogImportResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogImportService/CancelTripLogImport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "CancelTripLogImport", request)
            log.atInfo().addKeyValue("operation", "CancelTripLogImport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.reasonCode).log("Recorded audit detail")
            val outbound0 = exportHouseholdsRequest {
                this.context = request.context
                this.filter = ExportHouseholdsRequestKt.filter {
                    this.createdAfter = request.details.effectiveAt
                    this.labels.putAll(request.annotationsMap)
                    this.ownerId = request.details.newOwner.userId
                }
            }
            customersHouseholdQueryService.exportHouseholds(outbound0).collect { event ->
                log.atDebug().addKeyValue("bytes", event.serializedSize).log("Received downstream event")
            }
            cancelTripLogImportResponse {
                this.outcome = CancelTripLogImportResponseKt.outcome {
                    this.effectiveAt = request.details.effectiveAt
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.tripLogId = request.tripLogId
                }
            }
        }

    private suspend fun handleGetTripLogImportStatus(request: GetTripLogImportStatusRequest): GetTripLogImportStatusResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogImportService/GetTripLogImportStatus") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "GetTripLogImportStatus", request)
            log.atInfo().addKeyValue("operation", "GetTripLogImportStatus").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            getTripLogImportStatusResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.tripLogId = request.tripLogId
                }
            }
        }

    private suspend fun handleImportTripLogs(request: ImportTripLogsRequest): ImportTripLogsResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogImportService/ImportTripLogs") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ImportTripLogs", request)
            log.atInfo().addKeyValue("operation", "ImportTripLogs").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.header.source).log("Recorded audit detail")
            val outbound0 = countContractsRequest {
                this.context = request.context
                this.filter = CountContractsRequestKt.filter {
                    this.createdAfter = request.record.audit.createdAt
                    this.ownerId = request.record.tripLogId
                    this.region = request.record.billingAddress.region
                }
            }
            val reply0 = contractsContractQueryService.countContracts(outbound0)
            val outbound1 = scheduleYardMoveExpiryRequest {
                this.context = request.context
                this.yardMove = yardMove {
                    this.requester = contactInfo {
                        this.email = request.record.counterparty.email
                    }
                }
            }
            val reply1 = yardsYardMoveLifecycleService.scheduleYardMoveExpiry(outbound1)
            importTripLogsResponse {
                this.result = operationResult {
                    this.details.putAll(reply1.result.detailsMap)
                    this.referenceIds.addAll(reply0.result.referenceIdsList)
                    this.status = reply0.result.status
                }
            }
        }

    private fun MapTripLogImportColumnsRequest.toApproveServiceScheduleFromMapTripLogImportColumns1(): ApproveServiceScheduleRequest {
        val source = this
        return approveServiceScheduleRequest {
            this.context = source.context
            this.details = ApproveServiceScheduleRequestKt.details {
                this.approver = userRef {
                    this.email = source.details.contact.email
                }
            }
            this.serviceSchedule = serviceSchedule {
                this.participants.addAll(listOf(serviceScheduleParticipant {
                    this.contact = contactInfo {
                        this.phone = source.details.contact.phone
                    }
                }))
            }
        }
    }

    private suspend fun handleMapTripLogImportColumns(request: MapTripLogImportColumnsRequest): MapTripLogImportColumnsResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogImportService/MapTripLogImportColumns") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "MapTripLogImportColumns", request)
            log.atInfo().addKeyValue("operation", "MapTripLogImportColumns").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLog.tripLogId).log("Recorded audit detail")
            val outbound0 = estimateConsentRecordRequest {}.customersCopy {
                this.consentRecord = consentRecord {
                    this.subject = householdRef {
                        this.displayName = request.tripLog.counterparty.displayName
                    }
                }
                this.context = request.context
                this.details = EstimateConsentRecordRequestKt.details {
                    this.destination = postalAddress {
                        this.countryCode = request.tripLog.billingAddress.countryCode
                        this.locality = request.tripLog.billingAddress.locality
                        this.recipient = request.tripLog.billingAddress.recipient
                    }
                }
            }
            val reply0 = customersConsentRecordCalculationService.estimateConsentRecord(outbound0)
            val outbound1 = request.toApproveServiceScheduleFromMapTripLogImportColumns1()
            val reply1 = maintenanceServiceScheduleApprovalService.approveServiceSchedule(outbound1)
            mapTripLogImportColumnsResponse {
                this.outcome = MapTripLogImportColumnsResponseKt.outcome {
                    this.status = reply1.outcome.status
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.counterparty = customerRef {
                        this.email = reply1.serviceSchedule.organizer.email
                    }
                    this.lines.addAll(listOf(tripLogLine {
                        this.attributes.putAll(reply0.consentRecord.attributesMap)
                    }))
                    this.number = request.tripLog.number
                    this.tripLogId = request.tripLogId
                }
            }
        }

    private suspend fun handleRetryTripLogImport(request: RetryTripLogImportRequest): RetryTripLogImportResponse =
        RpcBoundary.run("acme.telematics.v1.TripLogImportService/RetryTripLogImport") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "RetryTripLogImport", request)
            log.atInfo().addKeyValue("operation", "RetryTripLogImport").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.tripLog.tripLogId).log("Recorded audit detail")
            val outbound0 = countHouseholdsRequest {
                this.context = request.context
                this.filter = CountHouseholdsRequestKt.filter {
                    this.createdAfter = request.tripLog.audit.createdAt
                    this.ownerId = request.tripLog.counterparty.customerId
                    this.region = request.tripLog.billingAddress.region
                }
            }
            val reply0 = customersHouseholdQueryService.countHouseholds(outbound0)
            retryTripLogImportResponse {
                this.outcome = RetryTripLogImportResponseKt.outcome {
                    this.message = reply0.result.message
                }
                this.result = operationResult {
                    this.status = "ok"
                }
                this.tripLog = tripLog {
                    this.metadata.putAll(request.tripLog.metadataMap)
                    this.tripLogId = request.tripLogId
                }
            }
        }

    private data class ValidateTripLogImport2Command(
        val context: RequestContext,
        val reason: String,
        val tripLogId: String,
        val annotations: Map<String, String>
    ) {
        fun toRequest(): ScheduleTripLogExportRequest {
            val command = this
            return scheduleTripLogExportRequest {
                this.annotations.putAll(command.annotations)
                this.context = command.context
                this.reason = command.reason
                this.tripLogId = command.tripLogId
            }
        }
    }
}
