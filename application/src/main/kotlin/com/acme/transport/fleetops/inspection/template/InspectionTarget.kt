package com.acme.transport.fleetops.inspection.template

import com.acme.transport.kit.text.normalize

/** Where a inspection target is in its life cycle. */
enum class InspectionTargetStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One inspection target, with what the planners need to act on it. */
data class InspectionTarget(
    var code: String,
    var title: String,
    var ownerId: String,
    var vehicleId: String,
    var odometer: Int,
    var status: InspectionTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionTargetStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionTargetStatus): Boolean {
        when (status) {
            InspectionTargetStatus.REQUESTED -> {
                when (next) {
                    InspectionTargetStatus.EXPIRED -> return true
                }
                return false
            }
            InspectionTargetStatus.GRANTED -> {
                when (next) {
                    InspectionTargetStatus.REVOKED -> return true
                }
                return false
            }
            InspectionTargetStatus.REVOKED -> return next == InspectionTargetStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: InspectionTargetStatus) {
        if (!canMoveTo(next)) {
            error("inspection target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = odometer.toDouble()

    fun snapshot(): InspectionTarget {
        return InspectionTarget(
            code = code,
            title = title,
            ownerId = ownerId,
            vehicleId = vehicleId,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a inspection target in status requested. */
fun newInspectionTarget(code: String, title: String): InspectionTarget {
    return InspectionTarget(
        code = code,
        title = title,
        ownerId = "",
        vehicleId = "",
        odometer = 0,
        status = InspectionTargetStatus.REQUESTED,
    )
}
