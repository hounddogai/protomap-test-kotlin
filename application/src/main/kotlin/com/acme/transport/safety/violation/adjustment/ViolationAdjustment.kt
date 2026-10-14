package com.acme.transport.safety.violation.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The violation adjustment records of safety's violation work, and the rules, stores, and calculations around
// them.

/** Where a violation adjustment is in its life cycle. */
enum class ViolationAdjustmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A violation adjustment as the violation team keeps it. */
data class ViolationAdjustment(
    var code: String,
    var name: String,
    var endsAt: Int,
    var limit: Int,
    var driverId: String,
    var status: ViolationAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationAdjustmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationAdjustmentStatus): Boolean {
        when (status) {
            ViolationAdjustmentStatus.PROPOSED -> return next == ViolationAdjustmentStatus.WITHDRAWN
            ViolationAdjustmentStatus.APPROVED -> return next == ViolationAdjustmentStatus.REJECTED || next == ViolationAdjustmentStatus.WITHDRAWN
            ViolationAdjustmentStatus.REJECTED -> {
                when (next) {
                    ViolationAdjustmentStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ViolationAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("violation adjustment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun adjustedTotal(): Double = round(limit.toDouble() * endsAt.toDouble() * 100) / 100

    fun snapshot(): ViolationAdjustment {
        return ViolationAdjustment(
            code = code,
            name = name,
            endsAt = endsAt,
            limit = limit,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a violation adjustment in status proposed. */
fun newViolationAdjustment(code: String, name: String): ViolationAdjustment {
    return ViolationAdjustment(
        code = code,
        name = name,
        endsAt = 0,
        limit = 0,
        driverId = "",
        status = ViolationAdjustmentStatus.PROPOSED,
    )
}
