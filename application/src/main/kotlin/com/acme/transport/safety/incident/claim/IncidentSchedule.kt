package com.acme.transport.safety.incident.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a incident schedule is in its life cycle. */
enum class IncidentScheduleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A incident schedule. Its code is unique within a tenant. */
data class IncidentSchedule(
    var code: String,
    var title: String,
    var windowDays: Int,
    var vehicleId: String,
    var priority: Int,
    var status: IncidentScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentScheduleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentScheduleStatus): Boolean {
        when (status) {
            IncidentScheduleStatus.PLANNED -> {
                when (next) {
                    IncidentScheduleStatus.SCHEDULED, IncidentScheduleStatus.COMPLETED -> return true
                }
                return false
            }
            IncidentScheduleStatus.SCHEDULED -> {
                when (next) {
                    IncidentScheduleStatus.IN_PROGRESS, IncidentScheduleStatus.CANCELLED -> return true
                }
                return false
            }
            IncidentScheduleStatus.IN_PROGRESS -> return next == IncidentScheduleStatus.COMPLETED
            IncidentScheduleStatus.COMPLETED -> return next == IncidentScheduleStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: IncidentScheduleStatus) {
        if (!canMoveTo(next)) {
            error("incident schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"incident schedule"} $code"

    fun projectedValue(): Double = round((priority.toDouble() - windowDays.toDouble()) * 100) / 100

    fun snapshot(): IncidentSchedule {
        return IncidentSchedule(
            code = code,
            title = title,
            windowDays = windowDays,
            vehicleId = vehicleId,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a incident schedule in status planned. */
fun newIncidentSchedule(code: String, title: String): IncidentSchedule {
    return IncidentSchedule(
        code = code,
        title = title,
        windowDays = 0,
        vehicleId = "",
        priority = 0,
        status = IncidentScheduleStatus.PLANNED,
    )
}
