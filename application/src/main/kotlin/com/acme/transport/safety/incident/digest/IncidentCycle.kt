package com.acme.transport.safety.incident.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a incident cycle is in its life cycle. */
enum class IncidentCycleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a incident cycle and its current status. */
data class IncidentCycle(
    var code: String,
    var label: String,
    var minimum: Double,
    var description: String,
    var timezone: String,
    var windowDays: Int,
    var rate: Double,
    var etaMinutes: Int,
    var status: IncidentCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentCycleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentCycleStatus): Boolean {
        when (status) {
            IncidentCycleStatus.PLANNED -> return next == IncidentCycleStatus.SCHEDULED
            IncidentCycleStatus.SCHEDULED -> return next == IncidentCycleStatus.COMPLETED || next == IncidentCycleStatus.CANCELLED
            IncidentCycleStatus.IN_PROGRESS -> return next == IncidentCycleStatus.COMPLETED
            IncidentCycleStatus.COMPLETED -> return next == IncidentCycleStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: IncidentCycleStatus) {
        if (!canMoveTo(next)) {
            error("incident cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun netVolume(): Double = round((windowDays.toDouble() - rate) * 100) / 100

    fun snapshot(): IncidentCycle {
        return IncidentCycle(
            code = code,
            label = label,
            minimum = minimum,
            description = description,
            timezone = timezone,
            windowDays = windowDays,
            rate = rate,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a incident cycle in status planned. */
fun newIncidentCycle(code: String, label: String): IncidentCycle {
    return IncidentCycle(
        code = code,
        label = label,
        minimum = 0.0,
        description = "",
        timezone = "",
        windowDays = 0,
        rate = 0.0,
        etaMinutes = 0,
        status = IncidentCycleStatus.PLANNED,
    )
}
