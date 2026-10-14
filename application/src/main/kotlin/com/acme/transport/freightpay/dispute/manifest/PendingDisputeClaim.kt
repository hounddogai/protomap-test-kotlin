package com.acme.transport.freightpay.dispute.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending dispute claim is in its life cycle. */
enum class PendingDisputeClaimStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A pending dispute claim as the dispute team keeps it. */
data class PendingDisputeClaim(
    var code: String,
    var name: String,
    var lane: String,
    var timezone: String,
    var odometer: Int,
    var stopCount: Int,
    var routeCode: String,
    var expiresAt: Int,
    var notes: String,
    var status: PendingDisputeClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingDisputeClaimStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingDisputeClaimStatus): Boolean {
        when (status) {
            PendingDisputeClaimStatus.OPEN -> return next == PendingDisputeClaimStatus.IN_REVIEW || next == PendingDisputeClaimStatus.REOPENED
            PendingDisputeClaimStatus.IN_REVIEW -> return next == PendingDisputeClaimStatus.REOPENED
            PendingDisputeClaimStatus.RESOLVED -> {
                when (next) {
                    PendingDisputeClaimStatus.REOPENED -> return true
                }
                return false
            }
            PendingDisputeClaimStatus.REOPENED -> return next == PendingDisputeClaimStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: PendingDisputeClaimStatus) {
        if (!canMoveTo(next)) {
            error("pending dispute claim $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun adjustedLoad(): Double = round(odometer.toDouble() * expiresAt.toDouble() * 100) / 100

    fun snapshot(): PendingDisputeClaim {
        return PendingDisputeClaim(
            code = code,
            name = name,
            lane = lane,
            timezone = timezone,
            odometer = odometer,
            stopCount = stopCount,
            routeCode = routeCode,
            expiresAt = expiresAt,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a pending dispute claim in status open. */
fun newPendingDisputeClaim(code: String, name: String): PendingDisputeClaim {
    return PendingDisputeClaim(
        code = code,
        name = name,
        lane = "",
        timezone = "",
        odometer = 0,
        stopCount = 0,
        routeCode = "",
        expiresAt = 0,
        notes = "",
        status = PendingDisputeClaimStatus.OPEN,
    )
}
