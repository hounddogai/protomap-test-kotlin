package com.acme.transport.crewing.restbreak.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rest break plan is in its life cycle. */
enum class RestBreakPlanStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One rest break plan, with what the planners need to act on it. */
data class RestBreakPlan(
    var code: String,
    var name: String,
    var expiresAt: Int,
    var timezone: String,
    var archived: Boolean,
    var ratio: Double,
    var source: String,
    var status: RestBreakPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakPlanStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakPlanStatus): Boolean {
        when (status) {
            RestBreakPlanStatus.PLANNED -> {
                when (next) {
                    RestBreakPlanStatus.SCHEDULED, RestBreakPlanStatus.COMPLETED -> return true
                }
                return false
            }
            RestBreakPlanStatus.SCHEDULED -> return next == RestBreakPlanStatus.COMPLETED || next == RestBreakPlanStatus.CANCELLED
            RestBreakPlanStatus.IN_PROGRESS -> {
                when (next) {
                    RestBreakPlanStatus.COMPLETED, RestBreakPlanStatus.CANCELLED -> return true
                }
                return false
            }
            RestBreakPlanStatus.COMPLETED -> return next == RestBreakPlanStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RestBreakPlanStatus) {
        if (!canMoveTo(next)) {
            error("rest break plan $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun adjustedValue(): Double = round(ratio * expiresAt.toDouble() * 100) / 100

    fun snapshot(): RestBreakPlan {
        return RestBreakPlan(
            code = code,
            name = name,
            expiresAt = expiresAt,
            timezone = timezone,
            archived = archived,
            ratio = ratio,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a rest break plan in status planned. */
fun newRestBreakPlan(code: String, name: String): RestBreakPlan {
    return RestBreakPlan(
        code = code,
        name = name,
        expiresAt = 0,
        timezone = "",
        archived = false,
        ratio = 0.0,
        source = "",
        status = RestBreakPlanStatus.PLANNED,
    )
}
