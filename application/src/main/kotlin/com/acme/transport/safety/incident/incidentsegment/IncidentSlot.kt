package com.acme.transport.safety.incident.incidentsegment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a incident slot is in its life cycle. */
enum class IncidentSlotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One incident slot, with what the planners need to act on it. */
data class IncidentSlot(
    var code: String,
    var title: String,
    var cost: Double,
    var pinned: Boolean,
    var externalRef: String,
    var stopCount: Int,
    var status: IncidentSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentSlotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentSlotStatus): Boolean {
        when (status) {
            IncidentSlotStatus.PLANNED -> return next == IncidentSlotStatus.IN_PROGRESS
            IncidentSlotStatus.SCHEDULED -> return next == IncidentSlotStatus.IN_PROGRESS
            IncidentSlotStatus.IN_PROGRESS -> {
                when (next) {
                    IncidentSlotStatus.CANCELLED -> return true
                }
                return false
            }
            IncidentSlotStatus.COMPLETED -> return next == IncidentSlotStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: IncidentSlotStatus) {
        if (!canMoveTo(next)) {
            error("incident slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun weightedTotal(): Double = round((stopCount.toDouble() - cost) * 100) / 100

    fun snapshot(): IncidentSlot {
        return IncidentSlot(
            code = code,
            title = title,
            cost = cost,
            pinned = pinned,
            externalRef = externalRef,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a incident slot in status planned. */
fun newIncidentSlot(code: String, title: String): IncidentSlot {
    return IncidentSlot(
        code = code,
        title = title,
        cost = 0.0,
        pinned = false,
        externalRef = "",
        stopCount = 0,
        status = IncidentSlotStatus.PLANNED,
    )
}
