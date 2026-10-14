package com.acme.transport.crewing.dutyperiod.draft

import com.acme.transport.kit.text.normalize

/** Where a rolling duty period reservation is in its life cycle. */
enum class RollingDutyPeriodReservationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a rolling duty period reservation and its current status. */
data class RollingDutyPeriodReservation(
    var code: String,
    var label: String,
    var summary: String,
    var notes: String,
    var budget: Double,
    var locale: String,
    var description: String,
    var lane: String,
    var status: RollingDutyPeriodReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDutyPeriodReservationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDutyPeriodReservationStatus): Boolean {
        when (status) {
            RollingDutyPeriodReservationStatus.IDLE -> {
                when (next) {
                    RollingDutyPeriodReservationStatus.WARMING, RollingDutyPeriodReservationStatus.DRAINING -> return true
                }
                return false
            }
            RollingDutyPeriodReservationStatus.WARMING -> return next == RollingDutyPeriodReservationStatus.DRAINING || next == RollingDutyPeriodReservationStatus.STOPPED
            RollingDutyPeriodReservationStatus.READY -> {
                when (next) {
                    RollingDutyPeriodReservationStatus.DRAINING, RollingDutyPeriodReservationStatus.STOPPED -> return true
                }
                return false
            }
            RollingDutyPeriodReservationStatus.DRAINING -> {
                when (next) {
                    RollingDutyPeriodReservationStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingDutyPeriodReservationStatus) {
        if (!canMoveTo(next)) {
            error("rolling duty period reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = budget

    fun snapshot(): RollingDutyPeriodReservation {
        return RollingDutyPeriodReservation(
            code = code,
            label = label,
            summary = summary,
            notes = notes,
            budget = budget,
            locale = locale,
            description = description,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a rolling duty period reservation in status idle. */
fun newRollingDutyPeriodReservation(code: String, label: String): RollingDutyPeriodReservation {
    return RollingDutyPeriodReservation(
        code = code,
        label = label,
        summary = "",
        notes = "",
        budget = 0.0,
        locale = "",
        description = "",
        lane = "",
        status = RollingDutyPeriodReservationStatus.IDLE,
    )
}
