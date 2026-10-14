package com.acme.transport.customs.inspection.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The inspection milestone records of customs's inspection work, and the rules, stores, and calculations
// around them.

/** Where a inspection milestone is in its life cycle. */
enum class InspectionMilestoneStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a inspection milestone and its current status. */
data class InspectionMilestone(
    var code: String,
    var label: String,
    var budget: Double,
    var threshold: Double,
    var floorValue: Double,
    var distanceKm: Double,
    var status: InspectionMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionMilestoneStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionMilestoneStatus): Boolean {
        when (status) {
            InspectionMilestoneStatus.PLANNED -> {
                when (next) {
                    InspectionMilestoneStatus.SCHEDULED -> return true
                }
                return false
            }
            InspectionMilestoneStatus.SCHEDULED -> {
                when (next) {
                    InspectionMilestoneStatus.CANCELLED -> return true
                }
                return false
            }
            InspectionMilestoneStatus.IN_PROGRESS -> {
                when (next) {
                    InspectionMilestoneStatus.COMPLETED, InspectionMilestoneStatus.CANCELLED -> return true
                }
                return false
            }
            InspectionMilestoneStatus.COMPLETED -> {
                when (next) {
                    InspectionMilestoneStatus.CANCELLED -> return true
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

    fun displayLabel(): String = "$code / $label"

    fun projectedValue(): Double = round(budget * threshold * 100) / 100

    fun snapshot(): InspectionMilestone {
        return InspectionMilestone(
            code = code,
            label = label,
            budget = budget,
            threshold = threshold,
            floorValue = floorValue,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a inspection milestone in status planned. */
fun newInspectionMilestone(code: String, label: String): InspectionMilestone {
    return InspectionMilestone(
        code = code,
        label = label,
        budget = 0.0,
        threshold = 0.0,
        floorValue = 0.0,
        distanceKm = 0.0,
        status = InspectionMilestoneStatus.PLANNED,
    )
}
