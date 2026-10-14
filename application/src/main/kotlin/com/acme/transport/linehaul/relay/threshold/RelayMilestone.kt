package com.acme.transport.linehaul.relay.threshold

import com.acme.transport.kit.text.normalize

/** Where a relay milestone is in its life cycle. */
enum class RelayMilestoneStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a relay milestone and its current status. */
data class RelayMilestone(
    var code: String,
    var name: String,
    var contactEmail: String,
    var driverId: String,
    var capacity: Int,
    var status: RelayMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayMilestoneStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayMilestoneStatus): Boolean {
        when (status) {
            RelayMilestoneStatus.OPEN -> return next == RelayMilestoneStatus.RESOLVED || next == RelayMilestoneStatus.REOPENED
            RelayMilestoneStatus.IN_REVIEW -> {
                when (next) {
                    RelayMilestoneStatus.ARCHIVED -> return true
                }
                return false
            }
            RelayMilestoneStatus.RESOLVED -> return next == RelayMilestoneStatus.REOPENED || next == RelayMilestoneStatus.ARCHIVED
            RelayMilestoneStatus.REOPENED -> {
                when (next) {
                    RelayMilestoneStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RelayMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("relay milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = capacity.toDouble()

    fun snapshot(): RelayMilestone {
        return RelayMilestone(
            code = code,
            name = name,
            contactEmail = contactEmail,
            driverId = driverId,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a relay milestone in status open. */
fun newRelayMilestone(code: String, name: String): RelayMilestone {
    return RelayMilestone(
        code = code,
        name = name,
        contactEmail = "",
        driverId = "",
        capacity = 0,
        status = RelayMilestoneStatus.OPEN,
    )
}
