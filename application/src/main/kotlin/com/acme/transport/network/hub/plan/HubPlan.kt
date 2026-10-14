package com.acme.transport.network.hub.plan

import com.acme.transport.kit.text.normalize

// The hub plan records of network's hub work, and the rules, stores, and calculations around them.

/** Where a hub plan is in its life cycle. */
enum class HubPlanStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A hub plan as the hub team keeps it. */
data class HubPlan(
    var code: String,
    var name: String,
    var attributes: MutableMap<String, String>,
    var score: Double,
    var phone: String,
    var status: HubPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubPlanStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: HubPlanStatus): Boolean {
        when (status) {
            HubPlanStatus.PROPOSED -> {
                when (next) {
                    HubPlanStatus.APPROVED -> return true
                }
                return false
            }
            HubPlanStatus.APPROVED -> return next == HubPlanStatus.REJECTED
            HubPlanStatus.REJECTED -> {
                when (next) {
                    HubPlanStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HubPlanStatus) {
        if (!canMoveTo(next)) {
            error("hub plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = score

    fun snapshot(): HubPlan {
        return HubPlan(
            code = code,
            name = name,
            attributes = attributes,
            score = score,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a hub plan in status proposed. */
fun newHubPlan(code: String, name: String): HubPlan {
    return HubPlan(
        code = code,
        name = name,
        attributes = mutableMapOf<String, String>(),
        score = 0.0,
        phone = "",
        status = HubPlanStatus.PROPOSED,
    )
}
