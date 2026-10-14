package com.acme.transport.network.cutoff.cutoffsegment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling cutoff segment records of network's cutoff work, and the rules, stores, and calculations around
// them.

/** Where a rolling cutoff segment is in its life cycle. */
enum class RollingCutoffSegmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a rolling cutoff segment and its current status. */
data class RollingCutoffSegment(
    var code: String,
    var title: String,
    var odometer: Int,
    var driverId: String,
    var phone: String,
    var threshold: Double,
    var score: Double,
    var carrier: String,
    var graceDays: Int,
    var quantity: Int,
    var endsAt: Int,
    var status: RollingCutoffSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingCutoffSegmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingCutoffSegmentStatus): Boolean {
        when (status) {
            RollingCutoffSegmentStatus.OPEN -> return next == RollingCutoffSegmentStatus.RESOLVED
            RollingCutoffSegmentStatus.IN_REVIEW -> return next == RollingCutoffSegmentStatus.RESOLVED
            RollingCutoffSegmentStatus.RESOLVED -> return next == RollingCutoffSegmentStatus.REOPENED || next == RollingCutoffSegmentStatus.ARCHIVED
            RollingCutoffSegmentStatus.REOPENED -> {
                when (next) {
                    RollingCutoffSegmentStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingCutoffSegmentStatus) {
        if (!canMoveTo(next)) {
            error("rolling cutoff segment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun effectiveTotal(): Double = round(graceDays.toDouble() * quantity.toDouble() * 100) / 100

    fun snapshot(): RollingCutoffSegment {
        return RollingCutoffSegment(
            code = code,
            title = title,
            odometer = odometer,
            driverId = driverId,
            phone = phone,
            threshold = threshold,
            score = score,
            carrier = carrier,
            graceDays = graceDays,
            quantity = quantity,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling cutoff segment in status open. */
fun newRollingCutoffSegment(code: String, title: String): RollingCutoffSegment {
    return RollingCutoffSegment(
        code = code,
        title = title,
        odometer = 0,
        driverId = "",
        phone = "",
        threshold = 0.0,
        score = 0.0,
        carrier = "",
        graceDays = 0,
        quantity = 0,
        endsAt = 0,
        status = RollingCutoffSegmentStatus.OPEN,
    )
}
