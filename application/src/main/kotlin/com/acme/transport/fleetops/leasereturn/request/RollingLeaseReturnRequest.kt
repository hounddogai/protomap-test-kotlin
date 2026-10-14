package com.acme.transport.fleetops.leasereturn.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling lease return request records of fleetops's lease return work, and the rules, stores, and
// calculations around them.

/** Where a rolling lease return request is in its life cycle. */
enum class RollingLeaseReturnRequestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One rolling lease return request, with what the planners need to act on it. */
data class RollingLeaseReturnRequest(
    var code: String,
    var name: String,
    var minimum: Double,
    var channel: String,
    var limit: Int,
    var createdAt: Int,
    var carrier: String,
    var distanceKm: Double,
    var status: RollingLeaseReturnRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingLeaseReturnRequestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingLeaseReturnRequestStatus): Boolean {
        when (status) {
            RollingLeaseReturnRequestStatus.OPEN -> return next == RollingLeaseReturnRequestStatus.ARCHIVED
            RollingLeaseReturnRequestStatus.IN_REVIEW -> return next == RollingLeaseReturnRequestStatus.RESOLVED || next == RollingLeaseReturnRequestStatus.ARCHIVED
            RollingLeaseReturnRequestStatus.RESOLVED -> return next == RollingLeaseReturnRequestStatus.ARCHIVED
            RollingLeaseReturnRequestStatus.REOPENED -> {
                when (next) {
                    RollingLeaseReturnRequestStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingLeaseReturnRequestStatus) {
        if (!canMoveTo(next)) {
            error("rolling lease return request $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun netLoad(): Double = round(distanceKm * createdAt.toDouble() * 100) / 100

    fun snapshot(): RollingLeaseReturnRequest {
        return RollingLeaseReturnRequest(
            code = code,
            name = name,
            minimum = minimum,
            channel = channel,
            limit = limit,
            createdAt = createdAt,
            carrier = carrier,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a rolling lease return request in status open. */
fun newRollingLeaseReturnRequest(code: String, name: String): RollingLeaseReturnRequest {
    return RollingLeaseReturnRequest(
        code = code,
        name = name,
        minimum = 0.0,
        channel = "",
        limit = 0,
        createdAt = 0,
        carrier = "",
        distanceKm = 0.0,
        status = RollingLeaseReturnRequestStatus.OPEN,
    )
}
