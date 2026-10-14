package com.acme.transport.safety.coaching.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered coaching schedule is in its life cycle. */
enum class TieredCoachingScheduleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a tiered coaching schedule and its current status. */
data class TieredCoachingSchedule(
    var code: String,
    var label: String,
    var endsAt: Int,
    var externalRef: String,
    var loadKg: Double,
    var status: TieredCoachingScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredCoachingScheduleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredCoachingScheduleStatus): Boolean {
        when (status) {
            TieredCoachingScheduleStatus.IDLE -> {
                when (next) {
                    TieredCoachingScheduleStatus.WARMING, TieredCoachingScheduleStatus.STOPPED -> return true
                }
                return false
            }
            TieredCoachingScheduleStatus.WARMING -> {
                when (next) {
                    TieredCoachingScheduleStatus.STOPPED -> return true
                }
                return false
            }
            TieredCoachingScheduleStatus.READY -> {
                when (next) {
                    TieredCoachingScheduleStatus.DRAINING, TieredCoachingScheduleStatus.STOPPED -> return true
                }
                return false
            }
            TieredCoachingScheduleStatus.DRAINING -> return next == TieredCoachingScheduleStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: TieredCoachingScheduleStatus) {
        if (!canMoveTo(next)) {
            error("tiered coaching schedule $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"tiered coaching schedule"} $code"

    fun effectiveVolume(): Double = round(loadKg * endsAt.toDouble() * 100) / 100

    fun snapshot(): TieredCoachingSchedule {
        return TieredCoachingSchedule(
            code = code,
            label = label,
            endsAt = endsAt,
            externalRef = externalRef,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tiered coaching schedule in status idle. */
fun newTieredCoachingSchedule(code: String, label: String): TieredCoachingSchedule {
    return TieredCoachingSchedule(
        code = code,
        label = label,
        endsAt = 0,
        externalRef = "",
        loadKg = 0.0,
        status = TieredCoachingScheduleStatus.IDLE,
    )
}
