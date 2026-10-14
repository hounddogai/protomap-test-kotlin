package com.acme.transport.terminal.dwell.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient dwell schedule is in its life cycle. */
enum class LenientDwellScheduleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A lenient dwell schedule as the dwell team keeps it. */
data class LenientDwellSchedule(
    var code: String,
    var label: String,
    var ratio: Double,
    var channel: String,
    var lane: String,
    var rate: Double,
    var status: LenientDwellScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientDwellScheduleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientDwellScheduleStatus): Boolean {
        when (status) {
            LenientDwellScheduleStatus.PLANNED -> return next == LenientDwellScheduleStatus.IN_PROGRESS || next == LenientDwellScheduleStatus.COMPLETED
            LenientDwellScheduleStatus.SCHEDULED -> {
                when (next) {
                    LenientDwellScheduleStatus.IN_PROGRESS, LenientDwellScheduleStatus.COMPLETED -> return true
                }
                return false
            }
            LenientDwellScheduleStatus.IN_PROGRESS -> {
                when (next) {
                    LenientDwellScheduleStatus.COMPLETED, LenientDwellScheduleStatus.CANCELLED -> return true
                }
                return false
            }
            LenientDwellScheduleStatus.COMPLETED -> {
                when (next) {
                    LenientDwellScheduleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientDwellScheduleStatus) {
        if (!canMoveTo(next)) {
            error("lenient dwell schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedTotal(): Double = round((ratio + rate) * 100) / 100

    fun snapshot(): LenientDwellSchedule {
        return LenientDwellSchedule(
            code = code,
            label = label,
            ratio = ratio,
            channel = channel,
            lane = lane,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a lenient dwell schedule in status planned. */
fun newLenientDwellSchedule(code: String, label: String): LenientDwellSchedule {
    return LenientDwellSchedule(
        code = code,
        label = label,
        ratio = 0.0,
        channel = "",
        lane = "",
        rate = 0.0,
        status = LenientDwellScheduleStatus.PLANNED,
    )
}
