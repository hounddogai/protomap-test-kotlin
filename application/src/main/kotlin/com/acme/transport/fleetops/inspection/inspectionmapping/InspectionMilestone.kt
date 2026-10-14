package com.acme.transport.fleetops.inspection.inspectionmapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection milestone is in its life cycle. */
enum class InspectionMilestoneStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A inspection milestone as the inspection team keeps it. */
data class InspectionMilestone(
    var code: String,
    var title: String,
    var priority: Int,
    var category: String,
    var graceDays: Int,
    var odometer: Int,
    var status: InspectionMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionMilestoneStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionMilestoneStatus): Boolean {
        when (status) {
            InspectionMilestoneStatus.IDLE -> {
                when (next) {
                    InspectionMilestoneStatus.READY, InspectionMilestoneStatus.DRAINING -> return true
                }
                return false
            }
            InspectionMilestoneStatus.WARMING -> {
                when (next) {
                    InspectionMilestoneStatus.DRAINING -> return true
                }
                return false
            }
            InspectionMilestoneStatus.READY -> return next == InspectionMilestoneStatus.DRAINING
            InspectionMilestoneStatus.DRAINING -> {
                when (next) {
                    InspectionMilestoneStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("inspection milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"inspection milestone"} $code"

    fun effectiveVolume(): Double = round((graceDays.toDouble() - odometer.toDouble()) * 100) / 100

    fun snapshot(): InspectionMilestone {
        return InspectionMilestone(
            code = code,
            title = title,
            priority = priority,
            category = category,
            graceDays = graceDays,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a inspection milestone in status idle. */
fun newInspectionMilestone(code: String, title: String): InspectionMilestone {
    return InspectionMilestone(
        code = code,
        title = title,
        priority = 0,
        category = "",
        graceDays = 0,
        odometer = 0,
        status = InspectionMilestoneStatus.IDLE,
    )
}
