package com.acme.transport.network.cutoff.audit

import com.acme.transport.kit.text.normalize

/** Where a fallback cutoff plan is in its life cycle. */
enum class FallbackCutoffPlanStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A fallback cutoff plan as the cutoff team keeps it. */
data class FallbackCutoffPlan(
    var code: String,
    var name: String,
    var routeCode: String,
    var notes: String,
    var tags: MutableList<String>,
    var rate: Double,
    var status: FallbackCutoffPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackCutoffPlanStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackCutoffPlanStatus): Boolean {
        when (status) {
            FallbackCutoffPlanStatus.REQUESTED -> return next == FallbackCutoffPlanStatus.EXPIRED
            FallbackCutoffPlanStatus.GRANTED -> {
                when (next) {
                    FallbackCutoffPlanStatus.EXPIRED -> return true
                }
                return false
            }
            FallbackCutoffPlanStatus.REVOKED -> return next == FallbackCutoffPlanStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: FallbackCutoffPlanStatus) {
        if (!canMoveTo(next)) {
            error("fallback cutoff plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fallback cutoff plan"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = rate

    fun snapshot(): FallbackCutoffPlan {
        return FallbackCutoffPlan(
            code = code,
            name = name,
            routeCode = routeCode,
            notes = notes,
            tags = tags,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fallback cutoff plan in status requested. */
fun newFallbackCutoffPlan(code: String, name: String): FallbackCutoffPlan {
    return FallbackCutoffPlan(
        code = code,
        name = name,
        routeCode = "",
        notes = "",
        tags = mutableListOf<String>(),
        rate = 0.0,
        status = FallbackCutoffPlanStatus.REQUESTED,
    )
}
