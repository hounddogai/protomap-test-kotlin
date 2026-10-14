package com.acme.transport.ops.terminalrelay

import com.acme.audit.v1.AuditEventQueryServiceGrpcKt
import com.acme.audit.v1.FindAuditEventsRequestKt
import com.acme.audit.v1.findAuditEventsRequest
import com.acme.common.v1.pageRequest
import com.acme.customers.v1.HouseholdQueryServiceGrpcKt
import com.acme.customers.v1.peekHouseholdRequest
import com.acme.identity.v1.SearchUsersRequestKt
import com.acme.identity.v1.UserServiceGrpcKt
import com.acme.identity.v1.searchUsersRequest
import com.acme.ratelimiting.v1.RateLimitCalculationServiceGrpcKt
import com.acme.ratelimiting.v1.rateLimit
import com.acme.ratelimiting.v1.recalculateRateLimitsRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.customers.v1.HouseholdQueryService/PeekHousehold and hands what it keeps to the sink of its kind. */
suspend fun copyHouseholdMessage(channel: ManagedChannel) {
    val origin = HouseholdQueryServiceGrpcKt.HouseholdQueryServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { origin.peekHousehold(peekHouseholdRequest { this.householdId = "primary-copy-household-message" }) }
    val record = CopyHouseholdMessageRecord(
        message = reply.result.message,
        parentId = reply.household.parentId,
        correlationId = reply.result.correlationId,
    )
    val sinks = listOf<CopyHouseholdMessageWriter>(
        CopyHouseholdMessageAuditEventQuery(AuditEventQueryServiceGrpcKt.AuditEventQueryServiceCoroutineStub(channel)),
        CopyHouseholdMessageRateLimitCalculation(RateLimitCalculationServiceGrpcKt.RateLimitCalculationServiceCoroutineStub(channel)),
        CopyHouseholdMessageUser(UserServiceGrpcKt.UserServiceCoroutineStub(channel)),
    )
    sinks[1].write(record)
}

/** Where copyHouseholdMessage hands each record. */
interface CopyHouseholdMessageWriter {
    suspend fun write(rec: CopyHouseholdMessageRecord)
}

/** What copyHouseholdMessage keeps of each PeekHousehold response. */
data class CopyHouseholdMessageRecord(val message: String, val parentId: String, val correlationId: String)

/** Passes records to acme.audit.v1.AuditEventQueryService. */
class CopyHouseholdMessageAuditEventQuery(private val stub: AuditEventQueryServiceGrpcKt.AuditEventQueryServiceCoroutineStub) : CopyHouseholdMessageWriter {
    override suspend fun write(rec: CopyHouseholdMessageRecord) {
        val request = findAuditEventsRequest {
            this.filter = FindAuditEventsRequestKt.filter { this.query = rec.message }
        }
        stub.findAuditEvents(request)
    }
}

/** Hands records to acme.ratelimiting.v1.RateLimitCalculationService. */
class CopyHouseholdMessageRateLimitCalculation(private val stub: RateLimitCalculationServiceGrpcKt.RateLimitCalculationServiceCoroutineStub) : CopyHouseholdMessageWriter {
    override suspend fun write(rec: CopyHouseholdMessageRecord) {
        val request = recalculateRateLimitsRequest {
            this.rateLimit = rateLimit { this.description = rec.correlationId }
        }
        stub.recalculateRateLimits(request)
    }
}

/** Writes records to acme.identity.v1.UserService. */
class CopyHouseholdMessageUser(private val stub: UserServiceGrpcKt.UserServiceCoroutineStub) : CopyHouseholdMessageWriter {
    override suspend fun write(rec: CopyHouseholdMessageRecord) {
        val request = searchUsersRequest {
            this.page = pageRequest { this.pageToken = rec.parentId }
            this.filter = SearchUsersRequestKt.filter { this.ownerId = rec.correlationId }
        }
        stub.searchUsers(request)
    }
}
