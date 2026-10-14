package com.acme.transport.crewing.dutyperiod.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict duty period request is in its life cycle. */
enum class StrictDutyPeriodRequestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One strict duty period request, with what the planners need to act on it. */
data class StrictDutyPeriodRequest(
    var code: String,
    var name: String,
    var timezone: String,
    var notes: String,
    var unit: String,
    var margin: Double,
    var sequence: Int,
    var priority: Int,
    var windowDays: Int,
    var vehicleId: String,
    var budget: Double,
    var status: StrictDutyPeriodRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDutyPeriodRequestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDutyPeriodRequestStatus): Boolean {
        when (status) {
            StrictDutyPeriodRequestStatus.PROPOSED -> return next == StrictDutyPeriodRequestStatus.APPROVED
            StrictDutyPeriodRequestStatus.APPROVED -> return next == StrictDutyPeriodRequestStatus.REJECTED || next == StrictDutyPeriodRequestStatus.WITHDRAWN
            StrictDutyPeriodRequestStatus.REJECTED -> return next == StrictDutyPeriodRequestStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: StrictDutyPeriodRequestStatus) {
        if (!canMoveTo(next)) {
            error("strict duty period request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"strict duty period request"} $code"

    fun effectiveTotal(): Double = round((margin - windowDays.toDouble()) * 100) / 100

    fun snapshot(): StrictDutyPeriodRequest {
        return StrictDutyPeriodRequest(
            code = code,
            name = name,
            timezone = timezone,
            notes = notes,
            unit = unit,
            margin = margin,
            sequence = sequence,
            priority = priority,
            windowDays = windowDays,
            vehicleId = vehicleId,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a strict duty period request in status proposed. */
fun newStrictDutyPeriodRequest(code: String, name: String): StrictDutyPeriodRequest {
    return StrictDutyPeriodRequest(
        code = code,
        name = name,
        timezone = "",
        notes = "",
        unit = "",
        margin = 0.0,
        sequence = 0,
        priority = 0,
        windowDays = 0,
        vehicleId = "",
        budget = 0.0,
        status = StrictDutyPeriodRequestStatus.PROPOSED,
    )
}
