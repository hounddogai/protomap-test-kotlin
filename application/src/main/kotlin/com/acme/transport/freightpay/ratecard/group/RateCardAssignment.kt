package com.acme.transport.freightpay.ratecard.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rate card assignment is in its life cycle. */
enum class RateCardAssignmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One rate card assignment, with what the planners need to act on it. */
data class RateCardAssignment(
    var code: String,
    var title: String,
    var amount: Double,
    var ownerId: String,
    var visible: Boolean,
    var score: Double,
    var driverId: String,
    var version: Int,
    var distanceKm: Double,
    var status: RateCardAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardAssignmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardAssignmentStatus): Boolean {
        when (status) {
            RateCardAssignmentStatus.IDLE -> return next == RateCardAssignmentStatus.READY || next == RateCardAssignmentStatus.DRAINING
            RateCardAssignmentStatus.WARMING -> {
                when (next) {
                    RateCardAssignmentStatus.READY, RateCardAssignmentStatus.DRAINING -> return true
                }
                return false
            }
            RateCardAssignmentStatus.READY -> return next == RateCardAssignmentStatus.STOPPED
            RateCardAssignmentStatus.DRAINING -> {
                when (next) {
                    RateCardAssignmentStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RateCardAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("rate card assignment $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code ($title)"

    fun effectiveValue(): Double = round(score * distanceKm * 100) / 100

    fun snapshot(): RateCardAssignment {
        return RateCardAssignment(
            code = code,
            title = title,
            amount = amount,
            ownerId = ownerId,
            visible = visible,
            score = score,
            driverId = driverId,
            version = version,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rate card assignment in status idle. */
fun newRateCardAssignment(code: String, title: String): RateCardAssignment {
    return RateCardAssignment(
        code = code,
        title = title,
        amount = 0.0,
        ownerId = "",
        visible = false,
        score = 0.0,
        driverId = "",
        version = 0,
        distanceKm = 0.0,
        status = RateCardAssignmentStatus.IDLE,
    )
}
