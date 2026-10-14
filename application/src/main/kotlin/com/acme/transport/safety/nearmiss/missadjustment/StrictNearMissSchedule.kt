package com.acme.transport.safety.nearmiss.missadjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict near miss schedule is in its life cycle. */
enum class StrictNearMissScheduleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A strict near miss schedule as the near miss team keeps it. */
data class StrictNearMissSchedule(
    var code: String,
    var name: String,
    var channel: String,
    var endsAt: Int,
    var visible: Boolean,
    var category: String,
    var createdAt: Int,
    var reviewer: String,
    var status: StrictNearMissScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictNearMissScheduleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictNearMissScheduleStatus): Boolean {
        when (status) {
            StrictNearMissScheduleStatus.PLANNED -> return next == StrictNearMissScheduleStatus.SCHEDULED || next == StrictNearMissScheduleStatus.CANCELLED
            StrictNearMissScheduleStatus.SCHEDULED -> return next == StrictNearMissScheduleStatus.COMPLETED || next == StrictNearMissScheduleStatus.CANCELLED
            StrictNearMissScheduleStatus.IN_PROGRESS -> return next == StrictNearMissScheduleStatus.COMPLETED || next == StrictNearMissScheduleStatus.CANCELLED
            StrictNearMissScheduleStatus.COMPLETED -> return next == StrictNearMissScheduleStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: StrictNearMissScheduleStatus) {
        if (!canMoveTo(next)) {
            error("strict near miss schedule $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun weightedLoad(): Double = round(endsAt.toDouble() * createdAt.toDouble() * 100) / 100

    fun snapshot(): StrictNearMissSchedule {
        return StrictNearMissSchedule(
            code = code,
            name = name,
            channel = channel,
            endsAt = endsAt,
            visible = visible,
            category = category,
            createdAt = createdAt,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a strict near miss schedule in status planned. */
fun newStrictNearMissSchedule(code: String, name: String): StrictNearMissSchedule {
    return StrictNearMissSchedule(
        code = code,
        name = name,
        channel = "",
        endsAt = 0,
        visible = false,
        category = "",
        createdAt = 0,
        reviewer = "",
        status = StrictNearMissScheduleStatus.PLANNED,
    )
}
