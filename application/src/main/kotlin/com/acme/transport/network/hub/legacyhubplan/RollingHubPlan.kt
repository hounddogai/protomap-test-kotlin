package com.acme.transport.network.hub.legacyhubplan

import com.acme.transport.kit.text.normalize

// The rolling hub plan records of network's hub work, and the rules, stores, and calculations around them.

/** Where a rolling hub plan is in its life cycle. */
enum class RollingHubPlanStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A rolling hub plan. Its code is unique within a tenant. */
data class RollingHubPlan(
    var code: String,
    var label: String,
    var title: String,
    var budget: Double,
    var phone: String,
    var tags: MutableList<String>,
    var status: RollingHubPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingHubPlanStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingHubPlanStatus): Boolean {
        when (status) {
            RollingHubPlanStatus.PROPOSED -> return next == RollingHubPlanStatus.APPROVED || next == RollingHubPlanStatus.WITHDRAWN
            RollingHubPlanStatus.APPROVED -> {
                when (next) {
                    RollingHubPlanStatus.REJECTED -> return true
                }
                return false
            }
            RollingHubPlanStatus.REJECTED -> return next == RollingHubPlanStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RollingHubPlanStatus) {
        if (!canMoveTo(next)) {
            error("rolling hub plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = budget

    fun snapshot(): RollingHubPlan {
        return RollingHubPlan(
            code = code,
            label = label,
            title = title,
            budget = budget,
            phone = phone,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rolling hub plan in status proposed. */
fun newRollingHubPlan(code: String, label: String): RollingHubPlan {
    return RollingHubPlan(
        code = code,
        label = label,
        title = "",
        budget = 0.0,
        phone = "",
        tags = mutableListOf<String>(),
        status = RollingHubPlanStatus.PROPOSED,
    )
}
