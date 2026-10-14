package com.acme.transport.terminal.dockdoor.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default dock door schedule records of terminal's dock door work, and the rules, stores, and
// calculations around them.

/** Where a default dock door schedule is in its life cycle. */
enum class DefaultDockDoorScheduleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A default dock door schedule as the dock door team keeps it. */
data class DefaultDockDoorSchedule(
    var code: String,
    var name: String,
    var phone: String,
    var odometer: Int,
    var windowDays: Int,
    var approved: Boolean,
    var batchSize: Int,
    var tags: MutableList<String>,
    var archived: Boolean,
    var floorValue: Double,
    var status: DefaultDockDoorScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultDockDoorScheduleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultDockDoorScheduleStatus): Boolean {
        when (status) {
            DefaultDockDoorScheduleStatus.CREATED -> {
                when (next) {
                    DefaultDockDoorScheduleStatus.VALIDATED -> return true
                }
                return false
            }
            DefaultDockDoorScheduleStatus.VALIDATED -> {
                when (next) {
                    DefaultDockDoorScheduleStatus.RETIRED -> return true
                }
                return false
            }
            DefaultDockDoorScheduleStatus.PUBLISHED -> {
                when (next) {
                    DefaultDockDoorScheduleStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultDockDoorScheduleStatus) {
        if (!canMoveTo(next)) {
            error("default dock door schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"default dock door schedule"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netVolume(): Double = round((floorValue + odometer.toDouble()) * 100) / 100

    fun snapshot(): DefaultDockDoorSchedule {
        return DefaultDockDoorSchedule(
            code = code,
            name = name,
            phone = phone,
            odometer = odometer,
            windowDays = windowDays,
            approved = approved,
            batchSize = batchSize,
            tags = tags,
            archived = archived,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a default dock door schedule in status created. */
fun newDefaultDockDoorSchedule(code: String, name: String): DefaultDockDoorSchedule {
    return DefaultDockDoorSchedule(
        code = code,
        name = name,
        phone = "",
        odometer = 0,
        windowDays = 0,
        approved = false,
        batchSize = 0,
        tags = mutableListOf<String>(),
        archived = false,
        floorValue = 0.0,
        status = DefaultDockDoorScheduleStatus.CREATED,
    )
}
