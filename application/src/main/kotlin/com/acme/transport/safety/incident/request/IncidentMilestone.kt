package com.acme.transport.safety.incident.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a incident milestone is in its life cycle. */
enum class IncidentMilestoneStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A incident milestone as the incident team keeps it. */
data class IncidentMilestone(
    var code: String,
    var name: String,
    var quantity: Int,
    var budget: Double,
    var limit: Int,
    var rate: Double,
    var tags: MutableList<String>,
    var depot: String,
    var status: IncidentMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentMilestoneStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentMilestoneStatus): Boolean {
        when (status) {
            IncidentMilestoneStatus.REQUESTED -> return next == IncidentMilestoneStatus.REVOKED
            IncidentMilestoneStatus.GRANTED -> {
                when (next) {
                    IncidentMilestoneStatus.EXPIRED -> return true
                }
                return false
            }
            IncidentMilestoneStatus.REVOKED -> {
                when (next) {
                    IncidentMilestoneStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IncidentMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("incident milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveLoad(): Double = round((quantity.toDouble() + budget) * 100) / 100

    fun snapshot(): IncidentMilestone {
        return IncidentMilestone(
            code = code,
            name = name,
            quantity = quantity,
            budget = budget,
            limit = limit,
            rate = rate,
            tags = tags,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a incident milestone in status requested. */
fun newIncidentMilestone(code: String, name: String): IncidentMilestone {
    return IncidentMilestone(
        code = code,
        name = name,
        quantity = 0,
        budget = 0.0,
        limit = 0,
        rate = 0.0,
        tags = mutableListOf<String>(),
        depot = "",
        status = IncidentMilestoneStatus.REQUESTED,
    )
}
