package com.acme.transport.drivers.adapters

import com.acme.common.v1.auditInfo
import com.acme.common.v1.contactInfo
import com.acme.common.v1.operationResult
import com.acme.common.v1.postalAddress
import com.acme.drivers.v1.BrowseDriverLicensesRequest
import com.acme.drivers.v1.BrowseDriverLicensesResponse
import com.acme.drivers.v1.DriverLicenseQueryServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseValidationServiceGrpcKt
import com.acme.drivers.v1.DriverQueryServiceGrpcKt
import com.acme.drivers.v1.DutyStatusCalculationServiceGrpcKt
import com.acme.drivers.v1.DutyStatusValidationServiceGrpcKt
import com.acme.drivers.v1.ExportDriverLicensesRequest
import com.acme.drivers.v1.ExportDriverLicensesResponse
import com.acme.drivers.v1.FindDriverLicensesRequest
import com.acme.drivers.v1.FindDriverLicensesResponse
import com.acme.drivers.v1.LookupDriverLicenseRequest
import com.acme.drivers.v1.LookupDriverLicenseResponse
import com.acme.drivers.v1.NormalizeDriverLicenseRequestKt
import com.acme.drivers.v1.NormalizeDutyStatusRequestKt
import com.acme.drivers.v1.QuoteDutyStatusRequestKt
import com.acme.drivers.v1.ResolveDriversRequest
import com.acme.drivers.v1.ResolveDriversResponse
import com.acme.drivers.v1.SuggestDriverLicensesRequest
import com.acme.drivers.v1.SuggestDriverLicensesResponse
import com.acme.drivers.v1.SummarizeDriverLicensesRequest
import com.acme.drivers.v1.SummarizeDriverLicensesResponse
import com.acme.drivers.v1.ValidateDutyStatusRequestKt
import com.acme.drivers.v1.browseDriverLicensesResponse
import com.acme.drivers.v1.copy as driversCopy
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.driverLicenseLine
import com.acme.drivers.v1.driverLicenseRef
import com.acme.drivers.v1.driverRef
import com.acme.drivers.v1.dutyStatus
import com.acme.drivers.v1.dutyStatusMeasurement
import com.acme.drivers.v1.exportDriverLicensesResponse
import com.acme.drivers.v1.findDriverLicensesResponse
import com.acme.drivers.v1.lintDriverLicensesRequest
import com.acme.drivers.v1.lookupDriverLicenseResponse
import com.acme.drivers.v1.normalizeDriverLicenseRequest
import com.acme.drivers.v1.normalizeDutyStatusRequest
import com.acme.drivers.v1.quoteDutyStatusRequest
import com.acme.drivers.v1.resolveDriversRequest
import com.acme.drivers.v1.scoreDriverLicenseRequest
import com.acme.drivers.v1.suggestDriverLicensesResponse
import com.acme.drivers.v1.summarizeDriverLicensesResponse
import com.acme.drivers.v1.validateDutyStatusRequest
import com.acme.identity.v1.userRef
import com.acme.search.v1.SearchIndexValidationServiceGrpcKt
import com.acme.search.v1.previewSearchIndexRequest
import com.acme.search.v1.searchIndex
import com.acme.telematics.v1.TripLogValidationServiceGrpcKt
import com.acme.telematics.v1.tripLog
import com.acme.telematics.v1.validateTripLogRequest
import com.acme.transport.drivers.DriversRepository
import com.acme.transport.platform.ClientRegistry
import com.acme.transport.platform.ReplyMailbox
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.WireCopies
import com.acme.venues.v1.SuggestVenuesRequestKt
import com.acme.venues.v1.VenueQueryServiceGrpcKt
import com.acme.venues.v1.suggestVenuesRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/** Answers read-only questions about driver licenses, for screens and reports. */
class DriverLicenseQueryController(
    private val store: DriversRepository,
    private val workers: CoroutineScope,
    private val driversDriverLicenseValidationService: DriverLicenseValidationServiceGrpcKt.DriverLicenseValidationServiceCoroutineStub,
    private val driversDriverQueryService: DriverQueryServiceGrpcKt.DriverQueryServiceCoroutineStub,
    private val driversDutyStatusCalculationService: DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineStub,
    private val driversDutyStatusValidationService: DutyStatusValidationServiceGrpcKt.DutyStatusValidationServiceCoroutineStub,
    private val searchSearchIndexValidationService: SearchIndexValidationServiceGrpcKt.SearchIndexValidationServiceCoroutineStub,
    private val telematicsTripLogValidationService: TripLogValidationServiceGrpcKt.TripLogValidationServiceCoroutineStub,
    private val venuesVenueQueryService: VenueQueryServiceGrpcKt.VenueQueryServiceCoroutineStub,
) : DriverLicenseQueryServiceGrpcKt.DriverLicenseQueryServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(DriverLicenseQueryController::class.java)

    private val workerForExportDriverLicenses1 = ReplyMailbox<ResolveDriversRequest, ResolveDriversResponse>(workers) { payload ->
        withTimeout(5_000) { driversDriverQueryService.resolveDrivers(payload) }
    }

    private val clientForExportDriverLicenses2 by lazy { telematicsTripLogValidationService }

    /** Lists driver licenses a page at a time. */
    override suspend fun browseDriverLicenses(request: BrowseDriverLicensesRequest): BrowseDriverLicensesResponse = handleBrowseDriverLicenses(request)

    /** Streams every matching driver license. */
    override fun exportDriverLicenses(request: ExportDriverLicensesRequest): Flow<ExportDriverLicensesResponse> = flow {
        emit(handleExportDriverLicenses(request))
    }

    /** Lists driver licenses a page at a time. */
    override suspend fun findDriverLicenses(request: FindDriverLicensesRequest): FindDriverLicensesResponse {
        return handleFindDriverLicenses(request)
    }

    /** Returns a driver license. */
    override suspend fun lookupDriverLicense(request: LookupDriverLicenseRequest): LookupDriverLicenseResponse = handleLookupDriverLicense(request)

    /** Lists driver licenses a page at a time. */
    override suspend fun suggestDriverLicenses(request: SuggestDriverLicensesRequest): SuggestDriverLicensesResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseQueryService/SuggestDriverLicenses") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SuggestDriverLicenses", request)
            log.atInfo().addKeyValue("operation", "SuggestDriverLicenses").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.context.tenantId).log("Recorded audit detail")
            suggestDriverLicensesResponse {
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    /** Summarizes a driver license. */
    override suspend fun summarizeDriverLicenses(request: SummarizeDriverLicensesRequest): SummarizeDriverLicensesResponse {
        return handleSummarizeDriverLicenses(request)
    }

    private suspend fun handleBrowseDriverLicenses(request: BrowseDriverLicensesRequest): BrowseDriverLicensesResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseQueryService/BrowseDriverLicenses") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "BrowseDriverLicenses", request)
            log.atInfo().addKeyValue("operation", "BrowseDriverLicenses").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = previewSearchIndexRequest {
                this.context = request.context
                this.searchIndex = searchIndex {
                    this.address = postalAddress {
                        this.region = request.filter.region
                    }
                    this.audit = auditInfo {
                        this.updatedAt = request.filter.createdAfter
                    }
                    this.owner = userRef {
                        this.email = request.context.actor.email
                        this.userId = request.context.actor.userId
                    }
                }
            }
            val reply0 = ClientRegistry.lookup("acme.search.v1.SearchIndexValidationService", SearchIndexValidationServiceGrpcKt.SearchIndexValidationServiceCoroutineStub::class.java).previewSearchIndex(outbound0)
            browseDriverLicensesResponse {
                this.driverLicenses.addAll(listOf(driverLicense {
                    this.audit = auditInfo {
                        this.createdAt = reply0.searchIndex.audit.createdAt
                    }
                }))
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleExportDriverLicenses(request: ExportDriverLicensesRequest): ExportDriverLicensesResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseQueryService/ExportDriverLicenses") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "ExportDriverLicenses", request)
            log.atInfo().addKeyValue("operation", "ExportDriverLicenses").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.filter.query).log("Recorded audit detail")
            val outbound0 = quoteDutyStatusRequest {
                this.annotations.putAll(request.filter.labelsMap)
                this.context = request.context
                this.details = QuoteDutyStatusRequestKt.details {
                    this.destination = postalAddress {
                        this.region = request.filter.region
                    }
                }
                this.dutyStatus = dutyStatus {
                    this.measurements.addAll(listOf(dutyStatusMeasurement {
                        this.observedAt = request.filter.createdAfter
                    }))
                    this.source = request.filter.ownerId
                }
            }
            val reply0 = driversDutyStatusCalculationService.quoteDutyStatus(WireCopies.reflective(outbound0))
            val outbound1 = resolveDriversRequest {
                this.context = request.context
            }
            val reply1 = workerForExportDriverLicenses1.submit(outbound1)
            val outbound2 = validateTripLogRequest {
                this.context = request.context
                this.tripLog = tripLog {
                    this.number = reply0.dutyStatus.subject.number
                }
            }
            val reply2 = clientForExportDriverLicenses2.validateTripLog(outbound2)
            exportDriverLicensesResponse {
                this.driverLicense = driverLicense {
                    this.audit = auditInfo {
                        this.updatedAt = reply2.tripLog.audit.updatedAt
                    }
                    this.billingAddress = postalAddress {
                        this.region = request.filter.region
                    }
                    this.lines.addAll(listOf(driverLicenseLine {
                        this.attributes.putAll(reply0.dutyStatus.attributesMap)
                    }))
                }
                this.result = operationResult {
                    this.correlationId = reply1.result.correlationId
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleFindDriverLicenses(request: FindDriverLicensesRequest): FindDriverLicensesResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseQueryService/FindDriverLicenses") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "FindDriverLicenses", request)
            log.atInfo().addKeyValue("operation", "FindDriverLicenses").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.page.pageToken).log("Recorded audit detail")
            val outbound0 = normalizeDutyStatusRequest {
                this.context = request.context
                this.details = NormalizeDutyStatusRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
            }
            val reply0 = flowOf(outbound0).map { value -> driversDutyStatusValidationService.normalizeDutyStatus(value) }.single()
            val outbound1 = lintDriverLicensesRequest {
                this.context = request.context
                this.driverLicense = driverLicense {
                    this.number = reply0.dutyStatus.subject.number
                }
            }
            val reply1 = withTimeout(5_000) { driversDriverLicenseValidationService.lintDriverLicenses(outbound1) }
            findDriverLicensesResponse {
                this.driverLicenses.addAll(listOf(driverLicense {
                    this.counterparty = driverRef {
                        this.driverId = reply1.driverLicense.counterparty.driverId
                    }
                    this.lines.addAll(listOf(driverLicenseLine {
                        this.attributes.putAll(reply0.dutyStatus.attributesMap)
                    }))
                    this.notes = reply1.driverLicense.notes
                }))
                this.result = operationResult {
                    this.status = "ok"
                }
            }
        }

    private suspend fun handleLookupDriverLicense(request: LookupDriverLicenseRequest): LookupDriverLicenseResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseQueryService/LookupDriverLicense") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "LookupDriverLicense", request)
            log.atInfo().addKeyValue("operation", "LookupDriverLicense").log("Handling transport request")
            val configure0: NormalizeDriverLicenseRequestKt.Dsl.() -> Unit = {
                this.context = request.context
                this.details = NormalizeDriverLicenseRequestKt.details {
                    this.contact = contactInfo {
                        this.email = request.context.actor.email
                    }
                }
                this.driverLicense = driverLicense {
                    this.driverLicenseId = request.driverLicenseId
                }
            }
            val outbound0 = normalizeDriverLicenseRequest(configure0)
            val reply0 = driversDriverLicenseValidationService.normalizeDriverLicense(outbound0)
            val outbound1 = validateDutyStatusRequest {
                this.context = request.context
                this.details = ValidateDutyStatusRequestKt.details {
                    this.contact = contactInfo {
                        this.email = reply0.driverLicense.counterparty.email
                    }
                }
                this.dutyStatus = dutyStatus {
                    this.subject = driverLicenseRef {
                        this.displayName = reply0.driverLicense.counterparty.displayName
                    }
                }
            }
            val reply1 = driversDutyStatusValidationService.validateDutyStatus(outbound1)
            lookupDriverLicenseResponse {
                this.driverLicense = driverLicense {
                    this.driverLicenseId = reply1.dutyStatus.subject.driverLicenseId
                    this.notes = reply0.driverLicense.notes
                }
                this.result = operationResult {
                    this.status = reply0.outcome.status
                }
            }
        }

    private suspend fun handleSummarizeDriverLicenses(request: SummarizeDriverLicensesRequest): SummarizeDriverLicensesResponse =
        RpcBoundary.run("acme.drivers.v1.DriverLicenseQueryService/SummarizeDriverLicenses") {
            RpcBoundary.validate(request.context)
            store.record(request.context.tenantId, "SummarizeDriverLicenses", request)
            log.atInfo().addKeyValue("operation", "SummarizeDriverLicenses").log("Handling transport request")
            log.atDebug().addKeyValue("auditDetail", request.details.format).log("Recorded audit detail")
            val outbound0 = suggestVenuesRequest {
                this.context = request.context
                this.filter = SuggestVenuesRequestKt.filter {
                    this.createdAfter = request.context.deadline
                    this.labels.putAll(request.details.filtersMap)
                    this.ownerId = request.driverLicenseId
                }
            }
            val reply0 = venuesVenueQueryService.suggestVenues(outbound0)
            val outbound1 = scoreDriverLicenseRequest {}.driversCopy {
                this.context = request.context
                this.driverLicenseId = request.driverLicenseId
            }
            val reply1 = driversDriverLicenseValidationService.scoreDriverLicense(outbound1)
            summarizeDriverLicensesResponse {
                this.period = request.details.period
                this.result = operationResult {
                    this.referenceIds.addAll((reply0.result.referenceIdsList) + (reply1.result.referenceIdsList))
                    this.status = "ok"
                }
            }
        }
}
