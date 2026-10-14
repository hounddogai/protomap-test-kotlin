package com.acme.transport.terminal.dockdoor.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dock door snapshot is in its life cycle. */
enum class DockDoorSnapshotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A dock door snapshot as the dock door team keeps it. */
data class DockDoorSnapshot(
    var code: String,
    var name: String,
    var dock: String,
    var ceiling: Double,
    var trailerId: String,
    var phone: String,
    var graceDays: Int,
    var maximum: Double,
    var limit: Int,
    var budget: Double,
    var status: DockDoorSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorSnapshotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorSnapshotStatus): Boolean {
        when (status) {
            DockDoorSnapshotStatus.CREATED -> {
                when (next) {
                    DockDoorSnapshotStatus.VALIDATED -> return true
                }
                return false
            }
            DockDoorSnapshotStatus.VALIDATED -> return next == DockDoorSnapshotStatus.RETIRED
            DockDoorSnapshotStatus.PUBLISHED -> {
                when (next) {
                    DockDoorSnapshotStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DockDoorSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("dock door snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"dock door snapshot"} $code"

    fun effectiveLoad(): Double = round((limit.toDouble() - maximum) * 100) / 100

    fun snapshot(): DockDoorSnapshot {
        return DockDoorSnapshot(
            code = code,
            name = name,
            dock = dock,
            ceiling = ceiling,
            trailerId = trailerId,
            phone = phone,
            graceDays = graceDays,
            maximum = maximum,
            limit = limit,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a dock door snapshot in status created. */
fun newDockDoorSnapshot(code: String, name: String): DockDoorSnapshot {
    return DockDoorSnapshot(
        code = code,
        name = name,
        dock = "",
        ceiling = 0.0,
        trailerId = "",
        phone = "",
        graceDays = 0,
        maximum = 0.0,
        limit = 0,
        budget = 0.0,
        status = DockDoorSnapshotStatus.CREATED,
    )
}
