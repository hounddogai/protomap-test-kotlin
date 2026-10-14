package com.acme.transport.safety.nearmiss.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled near miss schedule records of safety's near miss work, and the rules, stores, and
// calculations around them.

/** Where a scheduled near miss schedule is in its life cycle. */
enum class ScheduledNearMissScheduleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One scheduled near miss schedule, with what the planners need to act on it. */
data class ScheduledNearMissSchedule(
    var code: String,
    var label: String,
    var shiftCode: String,
    var segment: String,
    var name: String,
    var stopCount: Int,
    var endsAt: Int,
    var sequence: Int,
    var quantity: Int,
    var channel: String,
    var expiresAt: Int,
    var status: ScheduledNearMissScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledNearMissScheduleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledNearMissScheduleStatus): Boolean {
        when (status) {
            ScheduledNearMissScheduleStatus.REQUESTED -> return next == ScheduledNearMissScheduleStatus.REVOKED || next == ScheduledNearMissScheduleStatus.EXPIRED
            ScheduledNearMissScheduleStatus.GRANTED -> return next == ScheduledNearMissScheduleStatus.REVOKED
            ScheduledNearMissScheduleStatus.REVOKED -> return next == ScheduledNearMissScheduleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ScheduledNearMissScheduleStatus) {
        if (!canMoveTo(next)) {
            error("scheduled near miss schedule $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveExposure(): Double = round((stopCount.toDouble() + endsAt.toDouble()) * 100) / 100

    fun snapshot(): ScheduledNearMissSchedule {
        return ScheduledNearMissSchedule(
            code = code,
            label = label,
            shiftCode = shiftCode,
            segment = segment,
            name = name,
            stopCount = stopCount,
            endsAt = endsAt,
            sequence = sequence,
            quantity = quantity,
            channel = channel,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a scheduled near miss schedule in status requested. */
fun newScheduledNearMissSchedule(code: String, label: String): ScheduledNearMissSchedule {
    return ScheduledNearMissSchedule(
        code = code,
        label = label,
        shiftCode = "",
        segment = "",
        name = "",
        stopCount = 0,
        endsAt = 0,
        sequence = 0,
        quantity = 0,
        channel = "",
        expiresAt = 0,
        status = ScheduledNearMissScheduleStatus.REQUESTED,
    )
}
