package com.acme.transport.fleetops.tire.batch

import com.acme.transport.kit.text.normalize

/** Where a capped tire snapshot is in its life cycle. */
enum class CappedTireSnapshotStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One capped tire snapshot, with what the planners need to act on it. */
data class CappedTireSnapshot(
    var code: String,
    var name: String,
    var priority: Int,
    var trailerId: String,
    var active: Boolean,
    var country: String,
    var status: CappedTireSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedTireSnapshotStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedTireSnapshotStatus): Boolean {
        when (status) {
            CappedTireSnapshotStatus.DRAFT -> return next == CappedTireSnapshotStatus.ACTIVE
            CappedTireSnapshotStatus.PENDING -> {
                when (next) {
                    CappedTireSnapshotStatus.ACTIVE -> return true
                }
                return false
            }
            CappedTireSnapshotStatus.ACTIVE -> {
                when (next) {
                    CappedTireSnapshotStatus.SUSPENDED, CappedTireSnapshotStatus.CLOSED -> return true
                }
                return false
            }
            CappedTireSnapshotStatus.SUSPENDED -> return next == CappedTireSnapshotStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CappedTireSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("capped tire snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = priority.toDouble()

    fun snapshot(): CappedTireSnapshot {
        return CappedTireSnapshot(
            code = code,
            name = name,
            priority = priority,
            trailerId = trailerId,
            active = active,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a capped tire snapshot in status draft. */
fun newCappedTireSnapshot(code: String, name: String): CappedTireSnapshot {
    return CappedTireSnapshot(
        code = code,
        name = name,
        priority = 0,
        trailerId = "",
        active = false,
        country = "",
        status = CappedTireSnapshotStatus.DRAFT,
    )
}
