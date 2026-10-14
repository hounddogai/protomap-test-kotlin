package com.acme.transport.customs.clearance.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default clearance target is in its life cycle. */
enum class DefaultClearanceTargetStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a default clearance target and its current status. */
data class DefaultClearanceTarget(
    var code: String,
    var title: String,
    var budget: Double,
    var startsAt: Int,
    var etaMinutes: Int,
    var trailerId: String,
    var priority: Int,
    var status: DefaultClearanceTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultClearanceTargetStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultClearanceTargetStatus): Boolean {
        when (status) {
            DefaultClearanceTargetStatus.PROPOSED -> {
                when (next) {
                    DefaultClearanceTargetStatus.APPROVED, DefaultClearanceTargetStatus.REJECTED -> return true
                }
                return false
            }
            DefaultClearanceTargetStatus.APPROVED -> return next == DefaultClearanceTargetStatus.WITHDRAWN
            DefaultClearanceTargetStatus.REJECTED -> {
                when (next) {
                    DefaultClearanceTargetStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultClearanceTargetStatus) {
        if (!canMoveTo(next)) {
            error("default clearance target $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun adjustedLoad(): Double = round(priority.toDouble() * budget * 100) / 100

    fun snapshot(): DefaultClearanceTarget {
        return DefaultClearanceTarget(
            code = code,
            title = title,
            budget = budget,
            startsAt = startsAt,
            etaMinutes = etaMinutes,
            trailerId = trailerId,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a default clearance target in status proposed. */
fun newDefaultClearanceTarget(code: String, title: String): DefaultClearanceTarget {
    return DefaultClearanceTarget(
        code = code,
        title = title,
        budget = 0.0,
        startsAt = 0,
        etaMinutes = 0,
        trailerId = "",
        priority = 0,
        status = DefaultClearanceTargetStatus.PROPOSED,
    )
}
