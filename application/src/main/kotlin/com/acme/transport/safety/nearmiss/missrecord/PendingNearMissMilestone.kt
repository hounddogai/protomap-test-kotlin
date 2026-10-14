package com.acme.transport.safety.nearmiss.missrecord

import com.acme.transport.kit.text.normalize

/** Where a pending near miss milestone is in its life cycle. */
enum class PendingNearMissMilestoneStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A pending near miss milestone as the near miss team keeps it. */
data class PendingNearMissMilestone(
    var code: String,
    var name: String,
    var country: String,
    var archived: Boolean,
    var maximum: Double,
    var attributes: MutableMap<String, String>,
    var status: PendingNearMissMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingNearMissMilestoneStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingNearMissMilestoneStatus): Boolean {
        when (status) {
            PendingNearMissMilestoneStatus.PROPOSED -> return next == PendingNearMissMilestoneStatus.APPROVED || next == PendingNearMissMilestoneStatus.REJECTED
            PendingNearMissMilestoneStatus.APPROVED -> {
                when (next) {
                    PendingNearMissMilestoneStatus.REJECTED -> return true
                }
                return false
            }
            PendingNearMissMilestoneStatus.REJECTED -> {
                when (next) {
                    PendingNearMissMilestoneStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingNearMissMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("pending near miss milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = maximum

    fun snapshot(): PendingNearMissMilestone {
        return PendingNearMissMilestone(
            code = code,
            name = name,
            country = country,
            archived = archived,
            maximum = maximum,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a pending near miss milestone in status proposed. */
fun newPendingNearMissMilestone(code: String, name: String): PendingNearMissMilestone {
    return PendingNearMissMilestone(
        code = code,
        name = name,
        country = "",
        archived = false,
        maximum = 0.0,
        attributes = mutableMapOf<String, String>(),
        status = PendingNearMissMilestoneStatus.PROPOSED,
    )
}
