package com.acme.transport.crewing.restbreak.milestone

import com.acme.transport.kit.text.normalize

// The legacy rest break milestone records of crewing's rest break work, and the rules, stores, and
// calculations around them.

/** Where a legacy rest break milestone is in its life cycle. */
enum class LegacyRestBreakMilestoneStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A legacy rest break milestone. Its code is unique within a tenant. */
data class LegacyRestBreakMilestone(
    var code: String,
    var name: String,
    var durationMinutes: Int,
    var active: Boolean,
    var attributes: MutableMap<String, String>,
    var depot: String,
    var status: LegacyRestBreakMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyRestBreakMilestoneStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyRestBreakMilestoneStatus): Boolean {
        when (status) {
            LegacyRestBreakMilestoneStatus.DRAFT -> return next == LegacyRestBreakMilestoneStatus.SUSPENDED
            LegacyRestBreakMilestoneStatus.PENDING -> return next == LegacyRestBreakMilestoneStatus.CLOSED
            LegacyRestBreakMilestoneStatus.ACTIVE -> {
                when (next) {
                    LegacyRestBreakMilestoneStatus.SUSPENDED, LegacyRestBreakMilestoneStatus.CLOSED -> return true
                }
                return false
            }
            LegacyRestBreakMilestoneStatus.SUSPENDED -> return next == LegacyRestBreakMilestoneStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LegacyRestBreakMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("legacy rest break milestone $code cannot move from $status to $next")
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

    fun measuredValue(): Double = durationMinutes.toDouble()

    fun snapshot(): LegacyRestBreakMilestone {
        return LegacyRestBreakMilestone(
            code = code,
            name = name,
            durationMinutes = durationMinutes,
            active = active,
            attributes = attributes,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a legacy rest break milestone in status draft. */
fun newLegacyRestBreakMilestone(code: String, name: String): LegacyRestBreakMilestone {
    return LegacyRestBreakMilestone(
        code = code,
        name = name,
        durationMinutes = 0,
        active = false,
        attributes = mutableMapOf<String, String>(),
        depot = "",
        status = LegacyRestBreakMilestoneStatus.DRAFT,
    )
}
