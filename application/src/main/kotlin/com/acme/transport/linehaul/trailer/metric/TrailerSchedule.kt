package com.acme.transport.linehaul.trailer.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer schedule is in its life cycle. */
enum class TrailerScheduleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One trailer schedule, with what the planners need to act on it. */
data class TrailerSchedule(
    var code: String,
    var name: String,
    var ceiling: Double,
    var durationMinutes: Int,
    var externalRef: String,
    var channel: String,
    var status: TrailerScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerScheduleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerScheduleStatus): Boolean {
        when (status) {
            TrailerScheduleStatus.NEW -> return next == TrailerScheduleStatus.DONE
            TrailerScheduleStatus.QUEUED -> return next == TrailerScheduleStatus.DONE
            TrailerScheduleStatus.RUNNING -> return next == TrailerScheduleStatus.DONE
            TrailerScheduleStatus.DONE -> return next == TrailerScheduleStatus.FAILED
        }
        return false
    }

    fun moveTo(next: TrailerScheduleStatus) {
        if (!canMoveTo(next)) {
            error("trailer schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"trailer schedule"} $code"

    fun weightedValue(): Double = round((ceiling - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): TrailerSchedule {
        return TrailerSchedule(
            code = code,
            name = name,
            ceiling = ceiling,
            durationMinutes = durationMinutes,
            externalRef = externalRef,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a trailer schedule in status new. */
fun newTrailerSchedule(code: String, name: String): TrailerSchedule {
    return TrailerSchedule(
        code = code,
        name = name,
        ceiling = 0.0,
        durationMinutes = 0,
        externalRef = "",
        channel = "",
        status = TrailerScheduleStatus.NEW,
    )
}
