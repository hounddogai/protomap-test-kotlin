package com.acme.transport.crewing.dutyperiod.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a duty period schedule is in its life cycle. */
enum class DutyPeriodScheduleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a duty period schedule and its current status. */
data class DutyPeriodSchedule(
    var code: String,
    var label: String,
    var graceDays: Int,
    var revision: Int,
    var capacity: Int,
    var floorValue: Double,
    var carrier: String,
    var loadKg: Double,
    var trailerId: String,
    var status: DutyPeriodScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodScheduleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodScheduleStatus): Boolean {
        when (status) {
            DutyPeriodScheduleStatus.NEW -> {
                when (next) {
                    DutyPeriodScheduleStatus.QUEUED -> return true
                }
                return false
            }
            DutyPeriodScheduleStatus.QUEUED -> {
                when (next) {
                    DutyPeriodScheduleStatus.FAILED -> return true
                }
                return false
            }
            DutyPeriodScheduleStatus.RUNNING -> return next == DutyPeriodScheduleStatus.DONE || next == DutyPeriodScheduleStatus.FAILED
            DutyPeriodScheduleStatus.DONE -> {
                when (next) {
                    DutyPeriodScheduleStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DutyPeriodScheduleStatus) {
        if (!canMoveTo(next)) {
            error("duty period schedule $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedLoad(): Double = round((graceDays.toDouble() + capacity.toDouble()) * 100) / 100

    fun snapshot(): DutyPeriodSchedule {
        return DutyPeriodSchedule(
            code = code,
            label = label,
            graceDays = graceDays,
            revision = revision,
            capacity = capacity,
            floorValue = floorValue,
            carrier = carrier,
            loadKg = loadKg,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a duty period schedule in status new. */
fun newDutyPeriodSchedule(code: String, label: String): DutyPeriodSchedule {
    return DutyPeriodSchedule(
        code = code,
        label = label,
        graceDays = 0,
        revision = 0,
        capacity = 0,
        floorValue = 0.0,
        carrier = "",
        loadKg = 0.0,
        trailerId = "",
        status = DutyPeriodScheduleStatus.NEW,
    )
}
