package com.acme.transport.crewing.swap.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling swap plan is in its life cycle. */
enum class RollingSwapPlanStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One rolling swap plan, with what the planners need to act on it. */
data class RollingSwapPlan(
    var code: String,
    var name: String,
    var attributes: MutableMap<String, String>,
    var approved: Boolean,
    var pinned: Boolean,
    var graceDays: Int,
    var windowDays: Int,
    var currency: String,
    var stopCount: Int,
    var limit: Int,
    var status: RollingSwapPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingSwapPlanStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingSwapPlanStatus): Boolean {
        when (status) {
            RollingSwapPlanStatus.DRAFT -> {
                when (next) {
                    RollingSwapPlanStatus.ACTIVE, RollingSwapPlanStatus.CLOSED -> return true
                }
                return false
            }
            RollingSwapPlanStatus.PENDING -> return next == RollingSwapPlanStatus.SUSPENDED
            RollingSwapPlanStatus.ACTIVE -> {
                when (next) {
                    RollingSwapPlanStatus.SUSPENDED, RollingSwapPlanStatus.CLOSED -> return true
                }
                return false
            }
            RollingSwapPlanStatus.SUSPENDED -> return next == RollingSwapPlanStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RollingSwapPlanStatus) {
        if (!canMoveTo(next)) {
            error("rolling swap plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveTotal(): Double = round((limit.toDouble() + graceDays.toDouble()) * 100) / 100

    fun snapshot(): RollingSwapPlan {
        return RollingSwapPlan(
            code = code,
            name = name,
            attributes = attributes,
            approved = approved,
            pinned = pinned,
            graceDays = graceDays,
            windowDays = windowDays,
            currency = currency,
            stopCount = stopCount,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling swap plan in status draft. */
fun newRollingSwapPlan(code: String, name: String): RollingSwapPlan {
    return RollingSwapPlan(
        code = code,
        name = name,
        attributes = mutableMapOf<String, String>(),
        approved = false,
        pinned = false,
        graceDays = 0,
        windowDays = 0,
        currency = "",
        stopCount = 0,
        limit = 0,
        status = RollingSwapPlanStatus.DRAFT,
    )
}
