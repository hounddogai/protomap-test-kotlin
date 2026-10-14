package com.acme.transport.lastmile.locker.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The locker snapshot records of lastmile's locker work, and the rules, stores, and calculations around them.

/** Where a locker snapshot is in its life cycle. */
enum class LockerSnapshotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A locker snapshot as the locker team keeps it. */
data class LockerSnapshot(
    var code: String,
    var title: String,
    var endsAt: Int,
    var revision: Int,
    var ownerId: String,
    var visible: Boolean,
    var notes: String,
    var odometer: Int,
    var status: LockerSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerSnapshotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerSnapshotStatus): Boolean {
        when (status) {
            LockerSnapshotStatus.IDLE -> {
                when (next) {
                    LockerSnapshotStatus.DRAINING -> return true
                }
                return false
            }
            LockerSnapshotStatus.WARMING -> return next == LockerSnapshotStatus.DRAINING
            LockerSnapshotStatus.READY -> {
                when (next) {
                    LockerSnapshotStatus.DRAINING, LockerSnapshotStatus.STOPPED -> return true
                }
                return false
            }
            LockerSnapshotStatus.DRAINING -> {
                when (next) {
                    LockerSnapshotStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LockerSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("locker snapshot $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code: $title"

    fun weightedLoad(): Double = round((revision.toDouble() - endsAt.toDouble()) * 100) / 100

    fun snapshot(): LockerSnapshot {
        return LockerSnapshot(
            code = code,
            title = title,
            endsAt = endsAt,
            revision = revision,
            ownerId = ownerId,
            visible = visible,
            notes = notes,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a locker snapshot in status idle. */
fun newLockerSnapshot(code: String, title: String): LockerSnapshot {
    return LockerSnapshot(
        code = code,
        title = title,
        endsAt = 0,
        revision = 0,
        ownerId = "",
        visible = false,
        notes = "",
        odometer = 0,
        status = LockerSnapshotStatus.IDLE,
    )
}
