package com.acme.transport.crewing.roster.rosterquota

import com.acme.transport.kit.text.normalize

/** Where a roster checkpoint is in its life cycle. */
enum class RosterCheckpointStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One roster checkpoint, with what the planners need to act on it. */
data class RosterCheckpoint(
    var code: String,
    var name: String,
    var dock: String,
    var capacity: Int,
    var pinned: Boolean,
    var contactEmail: String,
    var status: RosterCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterCheckpointStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterCheckpointStatus): Boolean {
        when (status) {
            RosterCheckpointStatus.DRAFT -> {
                when (next) {
                    RosterCheckpointStatus.PENDING -> return true
                }
                return false
            }
            RosterCheckpointStatus.PENDING -> {
                when (next) {
                    RosterCheckpointStatus.ACTIVE, RosterCheckpointStatus.SUSPENDED -> return true
                }
                return false
            }
            RosterCheckpointStatus.ACTIVE -> return next == RosterCheckpointStatus.SUSPENDED
            RosterCheckpointStatus.SUSPENDED -> return next == RosterCheckpointStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RosterCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("roster checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"roster checkpoint"} $code"

    fun measuredValue(): Double = capacity.toDouble()

    fun snapshot(): RosterCheckpoint {
        return RosterCheckpoint(
            code = code,
            name = name,
            dock = dock,
            capacity = capacity,
            pinned = pinned,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a roster checkpoint in status draft. */
fun newRosterCheckpoint(code: String, name: String): RosterCheckpoint {
    return RosterCheckpoint(
        code = code,
        name = name,
        dock = "",
        capacity = 0,
        pinned = false,
        contactEmail = "",
        status = RosterCheckpointStatus.DRAFT,
    )
}
