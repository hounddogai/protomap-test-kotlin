package com.acme.transport.emissions.idling.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a idling schedule is in its life cycle. */
enum class IdlingScheduleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One idling schedule, with what the planners need to act on it. */
data class IdlingSchedule(
    var code: String,
    var name: String,
    var amount: Double,
    var description: String,
    var dock: String,
    var endsAt: Int,
    var reviewer: String,
    var status: IdlingScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingScheduleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingScheduleStatus): Boolean {
        when (status) {
            IdlingScheduleStatus.PROPOSED -> return next == IdlingScheduleStatus.APPROVED || next == IdlingScheduleStatus.WITHDRAWN
            IdlingScheduleStatus.APPROVED -> {
                when (next) {
                    IdlingScheduleStatus.REJECTED, IdlingScheduleStatus.WITHDRAWN -> return true
                }
                return false
            }
            IdlingScheduleStatus.REJECTED -> {
                when (next) {
                    IdlingScheduleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IdlingScheduleStatus) {
        if (!canMoveTo(next)) {
            error("idling schedule $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun effectiveVolume(): Double = round((endsAt.toDouble() + amount) * 100) / 100

    fun snapshot(): IdlingSchedule {
        return IdlingSchedule(
            code = code,
            name = name,
            amount = amount,
            description = description,
            dock = dock,
            endsAt = endsAt,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a idling schedule in status proposed. */
fun newIdlingSchedule(code: String, name: String): IdlingSchedule {
    return IdlingSchedule(
        code = code,
        name = name,
        amount = 0.0,
        description = "",
        dock = "",
        endsAt = 0,
        reviewer = "",
        status = IdlingScheduleStatus.PROPOSED,
    )
}
