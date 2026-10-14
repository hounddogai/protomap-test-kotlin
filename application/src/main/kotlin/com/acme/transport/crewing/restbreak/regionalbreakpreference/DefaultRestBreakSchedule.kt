package com.acme.transport.crewing.restbreak.regionalbreakpreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default rest break schedule is in its life cycle. */
enum class DefaultRestBreakScheduleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A default rest break schedule as the rest break team keeps it. */
data class DefaultRestBreakSchedule(
    var code: String,
    var label: String,
    var distanceKm: Double,
    var channel: String,
    var durationMinutes: Int,
    var pinned: Boolean,
    var status: DefaultRestBreakScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultRestBreakScheduleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultRestBreakScheduleStatus): Boolean {
        when (status) {
            DefaultRestBreakScheduleStatus.NEW -> {
                when (next) {
                    DefaultRestBreakScheduleStatus.DONE -> return true
                }
                return false
            }
            DefaultRestBreakScheduleStatus.QUEUED -> {
                when (next) {
                    DefaultRestBreakScheduleStatus.RUNNING, DefaultRestBreakScheduleStatus.FAILED -> return true
                }
                return false
            }
            DefaultRestBreakScheduleStatus.RUNNING -> {
                when (next) {
                    DefaultRestBreakScheduleStatus.DONE, DefaultRestBreakScheduleStatus.FAILED -> return true
                }
                return false
            }
            DefaultRestBreakScheduleStatus.DONE -> {
                when (next) {
                    DefaultRestBreakScheduleStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultRestBreakScheduleStatus) {
        if (!canMoveTo(next)) {
            error("default rest break schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedValue(): Double = round((durationMinutes.toDouble() - distanceKm) * 100) / 100

    fun snapshot(): DefaultRestBreakSchedule {
        return DefaultRestBreakSchedule(
            code = code,
            label = label,
            distanceKm = distanceKm,
            channel = channel,
            durationMinutes = durationMinutes,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a default rest break schedule in status new. */
fun newDefaultRestBreakSchedule(code: String, label: String): DefaultRestBreakSchedule {
    return DefaultRestBreakSchedule(
        code = code,
        label = label,
        distanceKm = 0.0,
        channel = "",
        durationMinutes = 0,
        pinned = false,
        status = DefaultRestBreakScheduleStatus.NEW,
    )
}
