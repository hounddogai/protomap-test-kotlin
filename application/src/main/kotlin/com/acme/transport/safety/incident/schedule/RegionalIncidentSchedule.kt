package com.acme.transport.safety.incident.schedule

import com.acme.transport.kit.text.normalize

// The regional incident schedule records of safety's incident work, and the rules, stores, and calculations
// around them.

/** Where a regional incident schedule is in its life cycle. */
enum class RegionalIncidentScheduleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One regional incident schedule, with what the planners need to act on it. */
data class RegionalIncidentSchedule(
    var code: String,
    var name: String,
    var currency: String,
    var channel: String,
    var fuelLiters: Double,
    var status: RegionalIncidentScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalIncidentScheduleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalIncidentScheduleStatus): Boolean {
        when (status) {
            RegionalIncidentScheduleStatus.NEW -> return next == RegionalIncidentScheduleStatus.FAILED
            RegionalIncidentScheduleStatus.QUEUED -> return next == RegionalIncidentScheduleStatus.RUNNING || next == RegionalIncidentScheduleStatus.FAILED
            RegionalIncidentScheduleStatus.RUNNING -> {
                when (next) {
                    RegionalIncidentScheduleStatus.DONE, RegionalIncidentScheduleStatus.FAILED -> return true
                }
                return false
            }
            RegionalIncidentScheduleStatus.DONE -> {
                when (next) {
                    RegionalIncidentScheduleStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalIncidentScheduleStatus) {
        if (!canMoveTo(next)) {
            error("regional incident schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"regional incident schedule"} $code"

    fun measuredValue(): Double = fuelLiters

    fun snapshot(): RegionalIncidentSchedule {
        return RegionalIncidentSchedule(
            code = code,
            name = name,
            currency = currency,
            channel = channel,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a regional incident schedule in status new. */
fun newRegionalIncidentSchedule(code: String, name: String): RegionalIncidentSchedule {
    return RegionalIncidentSchedule(
        code = code,
        name = name,
        currency = "",
        channel = "",
        fuelLiters = 0.0,
        status = RegionalIncidentScheduleStatus.NEW,
    )
}
