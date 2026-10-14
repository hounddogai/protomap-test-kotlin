package com.acme.transport.network.zone.baseline

import com.acme.transport.kit.text.normalize

/** Where a manual zone checkpoint is in its life cycle. */
enum class ManualZoneCheckpointStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A manual zone checkpoint as the zone team keeps it. */
data class ManualZoneCheckpoint(
    var code: String,
    var title: String,
    var budget: Double,
    var locale: String,
    var active: Boolean,
    var category: String,
    var label: String,
    var status: ManualZoneCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualZoneCheckpointStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualZoneCheckpointStatus): Boolean {
        when (status) {
            ManualZoneCheckpointStatus.IDLE -> return next == ManualZoneCheckpointStatus.READY || next == ManualZoneCheckpointStatus.STOPPED
            ManualZoneCheckpointStatus.WARMING -> {
                when (next) {
                    ManualZoneCheckpointStatus.READY, ManualZoneCheckpointStatus.STOPPED -> return true
                }
                return false
            }
            ManualZoneCheckpointStatus.READY -> return next == ManualZoneCheckpointStatus.DRAINING
            ManualZoneCheckpointStatus.DRAINING -> return next == ManualZoneCheckpointStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ManualZoneCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("manual zone checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"manual zone checkpoint"} $code"

    fun measuredValue(): Double = budget

    fun snapshot(): ManualZoneCheckpoint {
        return ManualZoneCheckpoint(
            code = code,
            title = title,
            budget = budget,
            locale = locale,
            active = active,
            category = category,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a manual zone checkpoint in status idle. */
fun newManualZoneCheckpoint(code: String, title: String): ManualZoneCheckpoint {
    return ManualZoneCheckpoint(
        code = code,
        title = title,
        budget = 0.0,
        locale = "",
        active = false,
        category = "",
        label = "",
        status = ManualZoneCheckpointStatus.IDLE,
    )
}
