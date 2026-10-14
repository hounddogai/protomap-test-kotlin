package com.acme.transport.terminal.dockdoor.rollingdoorsnapshot

import com.acme.transport.kit.text.normalize

// The seasonal dock door snapshot records of terminal's dock door work, and the rules, stores, and
// calculations around them.

/** Where a seasonal dock door snapshot is in its life cycle. */
enum class SeasonalDockDoorSnapshotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One seasonal dock door snapshot, with what the planners need to act on it. */
data class SeasonalDockDoorSnapshot(
    var code: String,
    var title: String,
    var phone: String,
    var fuelLiters: Double,
    var archived: Boolean,
    var status: SeasonalDockDoorSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalDockDoorSnapshotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalDockDoorSnapshotStatus): Boolean {
        when (status) {
            SeasonalDockDoorSnapshotStatus.CREATED -> return next == SeasonalDockDoorSnapshotStatus.PUBLISHED || next == SeasonalDockDoorSnapshotStatus.RETIRED
            SeasonalDockDoorSnapshotStatus.VALIDATED -> return next == SeasonalDockDoorSnapshotStatus.RETIRED
            SeasonalDockDoorSnapshotStatus.PUBLISHED -> {
                when (next) {
                    SeasonalDockDoorSnapshotStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalDockDoorSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("seasonal dock door snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"seasonal dock door snapshot"} $code"

    fun measuredValue(): Double = fuelLiters

    fun snapshot(): SeasonalDockDoorSnapshot {
        return SeasonalDockDoorSnapshot(
            code = code,
            title = title,
            phone = phone,
            fuelLiters = fuelLiters,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a seasonal dock door snapshot in status created. */
fun newSeasonalDockDoorSnapshot(code: String, title: String): SeasonalDockDoorSnapshot {
    return SeasonalDockDoorSnapshot(
        code = code,
        title = title,
        phone = "",
        fuelLiters = 0.0,
        archived = false,
        status = SeasonalDockDoorSnapshotStatus.CREATED,
    )
}
