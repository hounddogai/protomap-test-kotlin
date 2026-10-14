package com.acme.transport.safety.coaching.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The coaching adjustment records of safety's coaching work, and the rules, stores, and calculations around
// them.

/** Where a coaching adjustment is in its life cycle. */
enum class CoachingAdjustmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A coaching adjustment as the coaching team keeps it. */
data class CoachingAdjustment(
    var code: String,
    var name: String,
    var amount: Double,
    var etaMinutes: Int,
    var pallets: Int,
    var threshold: Double,
    var source: String,
    var createdAt: Int,
    var status: CoachingAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingAdjustmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingAdjustmentStatus): Boolean {
        when (status) {
            CoachingAdjustmentStatus.PROPOSED -> {
                when (next) {
                    CoachingAdjustmentStatus.APPROVED -> return true
                }
                return false
            }
            CoachingAdjustmentStatus.APPROVED -> {
                when (next) {
                    CoachingAdjustmentStatus.WITHDRAWN -> return true
                }
                return false
            }
            CoachingAdjustmentStatus.REJECTED -> return next == CoachingAdjustmentStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: CoachingAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("coaching adjustment $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun projectedExposure(): Double = round((threshold + pallets.toDouble()) * 100) / 100

    fun snapshot(): CoachingAdjustment {
        return CoachingAdjustment(
            code = code,
            name = name,
            amount = amount,
            etaMinutes = etaMinutes,
            pallets = pallets,
            threshold = threshold,
            source = source,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a coaching adjustment in status proposed. */
fun newCoachingAdjustment(code: String, name: String): CoachingAdjustment {
    return CoachingAdjustment(
        code = code,
        name = name,
        amount = 0.0,
        etaMinutes = 0,
        pallets = 0,
        threshold = 0.0,
        source = "",
        createdAt = 0,
        status = CoachingAdjustmentStatus.PROPOSED,
    )
}
