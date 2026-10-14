package com.acme.transport.terminal.crossdock.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default cross dock schedule is in its life cycle. */
enum class DefaultCrossDockScheduleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One default cross dock schedule, with what the planners need to act on it. */
data class DefaultCrossDockSchedule(
    var code: String,
    var title: String,
    var routeCode: String,
    var budget: Double,
    var checksum: String,
    var cost: Double,
    var priority: Int,
    var summary: String,
    var loadKg: Double,
    var status: DefaultCrossDockScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultCrossDockScheduleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultCrossDockScheduleStatus): Boolean {
        when (status) {
            DefaultCrossDockScheduleStatus.IDLE -> {
                when (next) {
                    DefaultCrossDockScheduleStatus.WARMING -> return true
                }
                return false
            }
            DefaultCrossDockScheduleStatus.WARMING -> return next == DefaultCrossDockScheduleStatus.READY || next == DefaultCrossDockScheduleStatus.DRAINING
            DefaultCrossDockScheduleStatus.READY -> return next == DefaultCrossDockScheduleStatus.DRAINING
            DefaultCrossDockScheduleStatus.DRAINING -> return next == DefaultCrossDockScheduleStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: DefaultCrossDockScheduleStatus) {
        if (!canMoveTo(next)) {
            error("default cross dock schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun effectiveValue(): Double = round(priority.toDouble() * loadKg * 100) / 100

    fun snapshot(): DefaultCrossDockSchedule {
        return DefaultCrossDockSchedule(
            code = code,
            title = title,
            routeCode = routeCode,
            budget = budget,
            checksum = checksum,
            cost = cost,
            priority = priority,
            summary = summary,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a default cross dock schedule in status idle. */
fun newDefaultCrossDockSchedule(code: String, title: String): DefaultCrossDockSchedule {
    return DefaultCrossDockSchedule(
        code = code,
        title = title,
        routeCode = "",
        budget = 0.0,
        checksum = "",
        cost = 0.0,
        priority = 0,
        summary = "",
        loadKg = 0.0,
        status = DefaultCrossDockScheduleStatus.IDLE,
    )
}
