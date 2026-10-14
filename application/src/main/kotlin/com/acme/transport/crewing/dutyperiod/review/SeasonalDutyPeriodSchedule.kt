package com.acme.transport.crewing.dutyperiod.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal duty period schedule is in its life cycle. */
enum class SeasonalDutyPeriodScheduleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a seasonal duty period schedule and its current status. */
data class SeasonalDutyPeriodSchedule(
    var code: String,
    var name: String,
    var carrier: String,
    var rate: Double,
    var threshold: Double,
    var driverId: String,
    var maximum: Double,
    var sequence: Int,
    var status: SeasonalDutyPeriodScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalDutyPeriodScheduleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalDutyPeriodScheduleStatus): Boolean {
        when (status) {
            SeasonalDutyPeriodScheduleStatus.REQUESTED -> return next == SeasonalDutyPeriodScheduleStatus.GRANTED || next == SeasonalDutyPeriodScheduleStatus.REVOKED
            SeasonalDutyPeriodScheduleStatus.GRANTED -> return next == SeasonalDutyPeriodScheduleStatus.REVOKED || next == SeasonalDutyPeriodScheduleStatus.EXPIRED
            SeasonalDutyPeriodScheduleStatus.REVOKED -> {
                when (next) {
                    SeasonalDutyPeriodScheduleStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalDutyPeriodScheduleStatus) {
        if (!canMoveTo(next)) {
            error("seasonal duty period schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun netVolume(): Double = round((sequence.toDouble() - threshold) * 100) / 100

    fun snapshot(): SeasonalDutyPeriodSchedule {
        return SeasonalDutyPeriodSchedule(
            code = code,
            name = name,
            carrier = carrier,
            rate = rate,
            threshold = threshold,
            driverId = driverId,
            maximum = maximum,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal duty period schedule in status requested. */
fun newSeasonalDutyPeriodSchedule(code: String, name: String): SeasonalDutyPeriodSchedule {
    return SeasonalDutyPeriodSchedule(
        code = code,
        name = name,
        carrier = "",
        rate = 0.0,
        threshold = 0.0,
        driverId = "",
        maximum = 0.0,
        sequence = 0,
        status = SeasonalDutyPeriodScheduleStatus.REQUESTED,
    )
}
