package com.acme.transport.terminal.dockdoor.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dock door schedule is in its life cycle. */
enum class DockDoorScheduleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A dock door schedule as the dock door team keeps it. */
data class DockDoorSchedule(
    var code: String,
    var name: String,
    var endsAt: Int,
    var title: String,
    var priority: Int,
    var summary: String,
    var status: DockDoorScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorScheduleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorScheduleStatus): Boolean {
        when (status) {
            DockDoorScheduleStatus.NEW -> return next == DockDoorScheduleStatus.QUEUED || next == DockDoorScheduleStatus.DONE
            DockDoorScheduleStatus.QUEUED -> {
                when (next) {
                    DockDoorScheduleStatus.DONE -> return true
                }
                return false
            }
            DockDoorScheduleStatus.RUNNING -> {
                when (next) {
                    DockDoorScheduleStatus.DONE -> return true
                }
                return false
            }
            DockDoorScheduleStatus.DONE -> return next == DockDoorScheduleStatus.FAILED
        }
        return false
    }

    fun moveTo(next: DockDoorScheduleStatus) {
        if (!canMoveTo(next)) {
            error("dock door schedule $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"dock door schedule"} $code"

    fun weightedExposure(): Double = round(priority.toDouble() * endsAt.toDouble() * 100) / 100

    fun snapshot(): DockDoorSchedule {
        return DockDoorSchedule(
            code = code,
            name = name,
            endsAt = endsAt,
            title = title,
            priority = priority,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a dock door schedule in status new. */
fun newDockDoorSchedule(code: String, name: String): DockDoorSchedule {
    return DockDoorSchedule(
        code = code,
        name = name,
        endsAt = 0,
        title = "",
        priority = 0,
        summary = "",
        status = DockDoorScheduleStatus.NEW,
    )
}
