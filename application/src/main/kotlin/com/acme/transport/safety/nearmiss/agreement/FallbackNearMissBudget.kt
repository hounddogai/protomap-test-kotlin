package com.acme.transport.safety.nearmiss.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback near miss budget is in its life cycle. */
enum class FallbackNearMissBudgetStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A fallback near miss budget. Its code is unique within a tenant. */
data class FallbackNearMissBudget(
    var code: String,
    var label: String,
    var stopCount: Int,
    var tags: MutableList<String>,
    var windowDays: Int,
    var approved: Boolean,
    var durationMinutes: Int,
    var priority: Int,
    var quantity: Int,
    var routeCode: String,
    var archived: Boolean,
    var status: FallbackNearMissBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackNearMissBudgetStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackNearMissBudgetStatus): Boolean {
        when (status) {
            FallbackNearMissBudgetStatus.PROPOSED -> return next == FallbackNearMissBudgetStatus.REJECTED
            FallbackNearMissBudgetStatus.APPROVED -> {
                when (next) {
                    FallbackNearMissBudgetStatus.WITHDRAWN -> return true
                }
                return false
            }
            FallbackNearMissBudgetStatus.REJECTED -> return next == FallbackNearMissBudgetStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: FallbackNearMissBudgetStatus) {
        if (!canMoveTo(next)) {
            error("fallback near miss budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"fallback near miss budget"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedExposure(): Double = round((quantity.toDouble() - stopCount.toDouble()) * 100) / 100

    fun snapshot(): FallbackNearMissBudget {
        return FallbackNearMissBudget(
            code = code,
            label = label,
            stopCount = stopCount,
            tags = tags,
            windowDays = windowDays,
            approved = approved,
            durationMinutes = durationMinutes,
            priority = priority,
            quantity = quantity,
            routeCode = routeCode,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback near miss budget in status proposed. */
fun newFallbackNearMissBudget(code: String, label: String): FallbackNearMissBudget {
    return FallbackNearMissBudget(
        code = code,
        label = label,
        stopCount = 0,
        tags = mutableListOf<String>(),
        windowDays = 0,
        approved = false,
        durationMinutes = 0,
        priority = 0,
        quantity = 0,
        routeCode = "",
        archived = false,
        status = FallbackNearMissBudgetStatus.PROPOSED,
    )
}
