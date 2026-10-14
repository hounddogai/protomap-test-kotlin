package com.acme.transport.crewing.roster.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict roster request is in its life cycle. */
enum class StrictRosterRequestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One strict roster request, with what the planners need to act on it. */
data class StrictRosterRequest(
    var code: String,
    var label: String,
    var expiresAt: Int,
    var fuelLiters: Double,
    var lane: String,
    var status: StrictRosterRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictRosterRequestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictRosterRequestStatus): Boolean {
        when (status) {
            StrictRosterRequestStatus.PROPOSED -> return next == StrictRosterRequestStatus.APPROVED || next == StrictRosterRequestStatus.REJECTED
            StrictRosterRequestStatus.APPROVED -> {
                when (next) {
                    StrictRosterRequestStatus.REJECTED -> return true
                }
                return false
            }
            StrictRosterRequestStatus.REJECTED -> return next == StrictRosterRequestStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: StrictRosterRequestStatus) {
        if (!canMoveTo(next)) {
            error("strict roster request $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedValue(): Double = round((expiresAt.toDouble() - fuelLiters) * 100) / 100

    fun snapshot(): StrictRosterRequest {
        return StrictRosterRequest(
            code = code,
            label = label,
            expiresAt = expiresAt,
            fuelLiters = fuelLiters,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a strict roster request in status proposed. */
fun newStrictRosterRequest(code: String, label: String): StrictRosterRequest {
    return StrictRosterRequest(
        code = code,
        label = label,
        expiresAt = 0,
        fuelLiters = 0.0,
        lane = "",
        status = StrictRosterRequestStatus.PROPOSED,
    )
}
