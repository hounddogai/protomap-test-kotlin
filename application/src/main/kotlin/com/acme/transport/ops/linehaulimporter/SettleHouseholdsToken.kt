package com.acme.transport.ops.linehaulimporter

import com.acme.customers.v1.HouseholdQueryServiceGrpcKt
import com.acme.customers.v1.suggestHouseholdsRequest
import com.acme.dispatch.v1.DispatchAssignmentQueryServiceGrpcKt
import com.acme.dispatch.v1.FindDispatchAssignmentsRequestKt
import com.acme.dispatch.v1.findDispatchAssignmentsRequest
import com.acme.drivers.v1.CheckDriverEligibilityRequestKt
import com.acme.drivers.v1.DriverValidationServiceGrpcKt
import com.acme.drivers.v1.checkDriverEligibilityRequest
import com.acme.search.v1.SearchIndexValidationServiceGrpcKt
import com.acme.search.v1.previewSearchIndexRequest
import com.acme.search.v1.searchIndex
import io.grpc.ManagedChannel

/** Reads acme.customers.v1.HouseholdQueryService/SuggestHouseholds and hands what it keeps to the sink of its kind. */
suspend fun settleHouseholdsToken(channel: ManagedChannel) {
    val origin = HouseholdQueryServiceGrpcKt.HouseholdQueryServiceCoroutineStub(channel)
    val reply = origin.suggestHouseholds(suggestHouseholdsRequest { this.orderBy = "current-settle-households-token" })
    val record = SettleHouseholdsTokenRecord(
        nextPageToken = reply.pageInfo.nextPageToken,
        correlationId = reply.result.correlationId,
        status = reply.result.status,
    )
    val registry = mutableMapOf<String, SettleHouseholdsTokenTarget>()
    for (registration in listOf(
        SettleHouseholdsTokenRegistration("dispatch-assignment-query", SettleHouseholdsTokenDispatchAssignmentQuery(DispatchAssignmentQueryServiceGrpcKt.DispatchAssignmentQueryServiceCoroutineStub(channel))),
        SettleHouseholdsTokenRegistration("driver-validation", SettleHouseholdsTokenDriverValidation(DriverValidationServiceGrpcKt.DriverValidationServiceCoroutineStub(channel))),
        SettleHouseholdsTokenRegistration("search-index-validation", SettleHouseholdsTokenSearchIndexValidation(SearchIndexValidationServiceGrpcKt.SearchIndexValidationServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["search-index-validation"] ?: return
    sink.write(record)
}

/** Where settleHouseholdsToken hands each record. */
interface SettleHouseholdsTokenTarget {
    suspend fun write(rec: SettleHouseholdsTokenRecord)
}

/** What settleHouseholdsToken keeps of each SuggestHouseholds response. */
data class SettleHouseholdsTokenRecord(val nextPageToken: String, val correlationId: String, val status: String)

/** Passes records to acme.dispatch.v1.DispatchAssignmentQueryService. */
class SettleHouseholdsTokenDispatchAssignmentQuery(private val stub: DispatchAssignmentQueryServiceGrpcKt.DispatchAssignmentQueryServiceCoroutineStub) : SettleHouseholdsTokenTarget {
    override suspend fun write(rec: SettleHouseholdsTokenRecord) {
        val request = findDispatchAssignmentsRequest {
            this.filter = FindDispatchAssignmentsRequestKt.filter { this.query = rec.correlationId; this.region = rec.nextPageToken }
            this.orderBy = rec.status
        }
        stub.findDispatchAssignments(request)
    }
}

/** Hands records to acme.drivers.v1.DriverValidationService. */
class SettleHouseholdsTokenDriverValidation(private val stub: DriverValidationServiceGrpcKt.DriverValidationServiceCoroutineStub) : SettleHouseholdsTokenTarget {
    override suspend fun write(rec: SettleHouseholdsTokenRecord) {
        val request = checkDriverEligibilityRequest {
            this.details = CheckDriverEligibilityRequestKt.details { this.locale = rec.correlationId }
            this.reason = rec.nextPageToken
        }
        stub.checkDriverEligibility(request)
    }
}

/** Passes records to acme.search.v1.SearchIndexValidationService. */
class SettleHouseholdsTokenSearchIndexValidation(private val stub: SearchIndexValidationServiceGrpcKt.SearchIndexValidationServiceCoroutineStub) : SettleHouseholdsTokenTarget {
    override suspend fun write(rec: SettleHouseholdsTokenRecord) {
        val request = previewSearchIndexRequest {
            this.reason = rec.correlationId
            this.searchIndex = searchIndex { this.code = rec.nextPageToken; this.name = rec.status }
        }
        stub.previewSearchIndex(request)
    }
}

/** A sink and the kind of record it takes. */
class SettleHouseholdsTokenRegistration(val kind: String, val sink: SettleHouseholdsTokenTarget)
