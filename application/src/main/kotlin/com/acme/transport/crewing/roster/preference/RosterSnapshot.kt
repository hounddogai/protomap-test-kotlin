package com.acme.transport.crewing.roster.preference

import com.acme.transport.kit.text.normalize

/** Where a roster snapshot is in its life cycle. */
enum class RosterSnapshotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a roster snapshot and its current status. */
data class RosterSnapshot(
    var code: String,
    var name: String,
    var active: Boolean,
    var region: String,
    var carrier: String,
    var retries: Int,
    var reviewer: String,
    var currency: String,
    var status: RosterSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterSnapshotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterSnapshotStatus): Boolean {
        when (status) {
            RosterSnapshotStatus.PLANNED -> {
                when (next) {
                    RosterSnapshotStatus.COMPLETED -> return true
                }
                return false
            }
            RosterSnapshotStatus.SCHEDULED -> {
                when (next) {
                    RosterSnapshotStatus.IN_PROGRESS, RosterSnapshotStatus.CANCELLED -> return true
                }
                return false
            }
            RosterSnapshotStatus.IN_PROGRESS -> return next == RosterSnapshotStatus.COMPLETED || next == RosterSnapshotStatus.CANCELLED
            RosterSnapshotStatus.COMPLETED -> {
                when (next) {
                    RosterSnapshotStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RosterSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("roster snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = retries.toDouble()

    fun snapshot(): RosterSnapshot {
        return RosterSnapshot(
            code = code,
            name = name,
            active = active,
            region = region,
            carrier = carrier,
            retries = retries,
            reviewer = reviewer,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a roster snapshot in status planned. */
fun newRosterSnapshot(code: String, name: String): RosterSnapshot {
    return RosterSnapshot(
        code = code,
        name = name,
        active = false,
        region = "",
        carrier = "",
        retries = 0,
        reviewer = "",
        currency = "",
        status = RosterSnapshotStatus.PLANNED,
    )
}
