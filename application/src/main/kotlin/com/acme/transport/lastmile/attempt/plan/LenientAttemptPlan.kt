package com.acme.transport.lastmile.attempt.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient attempt plan records of lastmile's attempt work, and the rules, stores, and calculations around
// them.

/** Where a lenient attempt plan is in its life cycle. */
enum class LenientAttemptPlanStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One lenient attempt plan, with what the planners need to act on it. */
data class LenientAttemptPlan(
    var code: String,
    var name: String,
    var priority: Int,
    var quantity: Int,
    var startsAt: Int,
    var unit: String,
    var timezone: String,
    var minimum: Double,
    var description: String,
    var ceiling: Double,
    var status: LenientAttemptPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientAttemptPlanStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientAttemptPlanStatus): Boolean {
        when (status) {
            LenientAttemptPlanStatus.REQUESTED -> {
                when (next) {
                    LenientAttemptPlanStatus.REVOKED, LenientAttemptPlanStatus.EXPIRED -> return true
                }
                return false
            }
            LenientAttemptPlanStatus.GRANTED -> return next == LenientAttemptPlanStatus.EXPIRED
            LenientAttemptPlanStatus.REVOKED -> {
                when (next) {
                    LenientAttemptPlanStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientAttemptPlanStatus) {
        if (!canMoveTo(next)) {
            error("lenient attempt plan $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun adjustedExposure(): Double = round(priority.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): LenientAttemptPlan {
        return LenientAttemptPlan(
            code = code,
            name = name,
            priority = priority,
            quantity = quantity,
            startsAt = startsAt,
            unit = unit,
            timezone = timezone,
            minimum = minimum,
            description = description,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a lenient attempt plan in status requested. */
fun newLenientAttemptPlan(code: String, name: String): LenientAttemptPlan {
    return LenientAttemptPlan(
        code = code,
        name = name,
        priority = 0,
        quantity = 0,
        startsAt = 0,
        unit = "",
        timezone = "",
        minimum = 0.0,
        description = "",
        ceiling = 0.0,
        status = LenientAttemptPlanStatus.REQUESTED,
    )
}
