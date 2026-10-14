package com.acme.transport.safety.incident.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The incident group records of safety's incident work, and the rules, stores, and calculations around them.

/** Where a incident group is in its life cycle. */
enum class IncidentGroupStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a incident group and its current status. */
data class IncidentGroup(
    var code: String,
    var title: String,
    var odometer: Int,
    var approved: Boolean,
    var shiftCode: String,
    var lane: String,
    var vehicleId: String,
    var floorValue: Double,
    var label: String,
    var ceiling: Double,
    var status: IncidentGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentGroupStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentGroupStatus): Boolean {
        when (status) {
            IncidentGroupStatus.OPEN -> {
                when (next) {
                    IncidentGroupStatus.ARCHIVED -> return true
                }
                return false
            }
            IncidentGroupStatus.IN_REVIEW -> return next == IncidentGroupStatus.RESOLVED
            IncidentGroupStatus.RESOLVED -> return next == IncidentGroupStatus.REOPENED || next == IncidentGroupStatus.ARCHIVED
            IncidentGroupStatus.REOPENED -> {
                when (next) {
                    IncidentGroupStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IncidentGroupStatus) {
        if (!canMoveTo(next)) {
            error("incident group $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$title]"

    fun weightedLoad(): Double = round((floorValue + ceiling) * 100) / 100

    fun snapshot(): IncidentGroup {
        return IncidentGroup(
            code = code,
            title = title,
            odometer = odometer,
            approved = approved,
            shiftCode = shiftCode,
            lane = lane,
            vehicleId = vehicleId,
            floorValue = floorValue,
            label = label,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a incident group in status open. */
fun newIncidentGroup(code: String, title: String): IncidentGroup {
    return IncidentGroup(
        code = code,
        title = title,
        odometer = 0,
        approved = false,
        shiftCode = "",
        lane = "",
        vehicleId = "",
        floorValue = 0.0,
        label = "",
        ceiling = 0.0,
        status = IncidentGroupStatus.OPEN,
    )
}
