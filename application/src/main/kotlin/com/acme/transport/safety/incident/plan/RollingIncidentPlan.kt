package com.acme.transport.safety.incident.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling incident plan records of safety's incident work, and the rules, stores, and calculations around
// them.

/** Where a rolling incident plan is in its life cycle. */
enum class RollingIncidentPlanStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A rolling incident plan as the incident team keeps it. */
data class RollingIncidentPlan(
    var code: String,
    var name: String,
    var tags: MutableList<String>,
    var ceiling: Double,
    var notes: String,
    var approved: Boolean,
    var threshold: Double,
    var status: RollingIncidentPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingIncidentPlanStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingIncidentPlanStatus): Boolean {
        when (status) {
            RollingIncidentPlanStatus.OPEN -> {
                when (next) {
                    RollingIncidentPlanStatus.IN_REVIEW, RollingIncidentPlanStatus.RESOLVED -> return true
                }
                return false
            }
            RollingIncidentPlanStatus.IN_REVIEW -> return next == RollingIncidentPlanStatus.REOPENED || next == RollingIncidentPlanStatus.ARCHIVED
            RollingIncidentPlanStatus.RESOLVED -> {
                when (next) {
                    RollingIncidentPlanStatus.ARCHIVED -> return true
                }
                return false
            }
            RollingIncidentPlanStatus.REOPENED -> return next == RollingIncidentPlanStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RollingIncidentPlanStatus) {
        if (!canMoveTo(next)) {
            error("rolling incident plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveValue(): Double = round(threshold * ceiling * 100) / 100

    fun snapshot(): RollingIncidentPlan {
        return RollingIncidentPlan(
            code = code,
            name = name,
            tags = tags,
            ceiling = ceiling,
            notes = notes,
            approved = approved,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling incident plan in status open. */
fun newRollingIncidentPlan(code: String, name: String): RollingIncidentPlan {
    return RollingIncidentPlan(
        code = code,
        name = name,
        tags = mutableListOf<String>(),
        ceiling = 0.0,
        notes = "",
        approved = false,
        threshold = 0.0,
        status = RollingIncidentPlanStatus.OPEN,
    )
}
