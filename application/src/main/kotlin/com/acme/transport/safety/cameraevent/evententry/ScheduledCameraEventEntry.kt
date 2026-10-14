package com.acme.transport.safety.cameraevent.evententry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled camera event entry records of safety's camera event work, and the rules, stores, and
// calculations around them.

/** Where a scheduled camera event entry is in its life cycle. */
enum class ScheduledCameraEventEntryStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One scheduled camera event entry, with what the planners need to act on it. */
data class ScheduledCameraEventEntry(
    var code: String,
    var title: String,
    var depot: String,
    var dock: String,
    var country: String,
    var quantity: Int,
    var routeCode: String,
    var windowDays: Int,
    var capacity: Int,
    var status: ScheduledCameraEventEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledCameraEventEntryStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledCameraEventEntryStatus): Boolean {
        when (status) {
            ScheduledCameraEventEntryStatus.PLANNED -> return next == ScheduledCameraEventEntryStatus.IN_PROGRESS || next == ScheduledCameraEventEntryStatus.CANCELLED
            ScheduledCameraEventEntryStatus.SCHEDULED -> return next == ScheduledCameraEventEntryStatus.CANCELLED
            ScheduledCameraEventEntryStatus.IN_PROGRESS -> {
                when (next) {
                    ScheduledCameraEventEntryStatus.COMPLETED, ScheduledCameraEventEntryStatus.CANCELLED -> return true
                }
                return false
            }
            ScheduledCameraEventEntryStatus.COMPLETED -> return next == ScheduledCameraEventEntryStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ScheduledCameraEventEntryStatus) {
        if (!canMoveTo(next)) {
            error("scheduled camera event entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun netExposure(): Double = round((windowDays.toDouble() - quantity.toDouble()) * 100) / 100

    fun snapshot(): ScheduledCameraEventEntry {
        return ScheduledCameraEventEntry(
            code = code,
            title = title,
            depot = depot,
            dock = dock,
            country = country,
            quantity = quantity,
            routeCode = routeCode,
            windowDays = windowDays,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a scheduled camera event entry in status planned. */
fun newScheduledCameraEventEntry(code: String, title: String): ScheduledCameraEventEntry {
    return ScheduledCameraEventEntry(
        code = code,
        title = title,
        depot = "",
        dock = "",
        country = "",
        quantity = 0,
        routeCode = "",
        windowDays = 0,
        capacity = 0,
        status = ScheduledCameraEventEntryStatus.PLANNED,
    )
}
