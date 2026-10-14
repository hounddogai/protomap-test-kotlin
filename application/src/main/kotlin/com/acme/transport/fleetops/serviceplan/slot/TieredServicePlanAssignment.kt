package com.acme.transport.fleetops.serviceplan.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered service plan assignment is in its life cycle. */
enum class TieredServicePlanAssignmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One tiered service plan assignment, with what the planners need to act on it. */
data class TieredServicePlanAssignment(
    var code: String,
    var name: String,
    var stopCount: Int,
    var maximum: Double,
    var floorValue: Double,
    var visible: Boolean,
    var etaMinutes: Int,
    var status: TieredServicePlanAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredServicePlanAssignmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredServicePlanAssignmentStatus): Boolean {
        when (status) {
            TieredServicePlanAssignmentStatus.REQUESTED -> return next == TieredServicePlanAssignmentStatus.EXPIRED
            TieredServicePlanAssignmentStatus.GRANTED -> {
                when (next) {
                    TieredServicePlanAssignmentStatus.REVOKED, TieredServicePlanAssignmentStatus.EXPIRED -> return true
                }
                return false
            }
            TieredServicePlanAssignmentStatus.REVOKED -> return next == TieredServicePlanAssignmentStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: TieredServicePlanAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("tiered service plan assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveLoad(): Double = round((floorValue - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): TieredServicePlanAssignment {
        return TieredServicePlanAssignment(
            code = code,
            name = name,
            stopCount = stopCount,
            maximum = maximum,
            floorValue = floorValue,
            visible = visible,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered service plan assignment in status requested. */
fun newTieredServicePlanAssignment(code: String, name: String): TieredServicePlanAssignment {
    return TieredServicePlanAssignment(
        code = code,
        name = name,
        stopCount = 0,
        maximum = 0.0,
        floorValue = 0.0,
        visible = false,
        etaMinutes = 0,
        status = TieredServicePlanAssignmentStatus.REQUESTED,
    )
}
