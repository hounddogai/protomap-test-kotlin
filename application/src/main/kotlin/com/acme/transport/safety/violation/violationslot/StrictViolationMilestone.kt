package com.acme.transport.safety.violation.violationslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict violation milestone is in its life cycle. */
enum class StrictViolationMilestoneStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A strict violation milestone as the violation team keeps it. */
data class StrictViolationMilestone(
    var code: String,
    var name: String,
    var dock: String,
    var endsAt: Int,
    var currency: String,
    var maximum: Double,
    var visible: Boolean,
    var updatedAt: Int,
    var status: StrictViolationMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictViolationMilestoneStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictViolationMilestoneStatus): Boolean {
        when (status) {
            StrictViolationMilestoneStatus.OPEN -> {
                when (next) {
                    StrictViolationMilestoneStatus.RESOLVED, StrictViolationMilestoneStatus.REOPENED -> return true
                }
                return false
            }
            StrictViolationMilestoneStatus.IN_REVIEW -> return next == StrictViolationMilestoneStatus.ARCHIVED
            StrictViolationMilestoneStatus.RESOLVED -> return next == StrictViolationMilestoneStatus.REOPENED
            StrictViolationMilestoneStatus.REOPENED -> return next == StrictViolationMilestoneStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: StrictViolationMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("strict violation milestone $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun netExposure(): Double = round((endsAt.toDouble() + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): StrictViolationMilestone {
        return StrictViolationMilestone(
            code = code,
            name = name,
            dock = dock,
            endsAt = endsAt,
            currency = currency,
            maximum = maximum,
            visible = visible,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a strict violation milestone in status open. */
fun newStrictViolationMilestone(code: String, name: String): StrictViolationMilestone {
    return StrictViolationMilestone(
        code = code,
        name = name,
        dock = "",
        endsAt = 0,
        currency = "",
        maximum = 0.0,
        visible = false,
        updatedAt = 0,
        status = StrictViolationMilestoneStatus.OPEN,
    )
}
