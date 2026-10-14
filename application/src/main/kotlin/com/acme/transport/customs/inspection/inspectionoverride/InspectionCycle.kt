package com.acme.transport.customs.inspection.inspectionoverride

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection cycle is in its life cycle. */
enum class InspectionCycleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One inspection cycle, with what the planners need to act on it. */
data class InspectionCycle(
    var code: String,
    var label: String,
    var depot: String,
    var contactEmail: String,
    var floorValue: Double,
    var archived: Boolean,
    var durationMinutes: Int,
    var sequence: Int,
    var summary: String,
    var status: InspectionCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionCycleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionCycleStatus): Boolean {
        when (status) {
            InspectionCycleStatus.OPEN -> {
                when (next) {
                    InspectionCycleStatus.IN_REVIEW, InspectionCycleStatus.ARCHIVED -> return true
                }
                return false
            }
            InspectionCycleStatus.IN_REVIEW -> return next == InspectionCycleStatus.REOPENED || next == InspectionCycleStatus.ARCHIVED
            InspectionCycleStatus.RESOLVED -> {
                when (next) {
                    InspectionCycleStatus.REOPENED -> return true
                }
                return false
            }
            InspectionCycleStatus.REOPENED -> {
                when (next) {
                    InspectionCycleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionCycleStatus) {
        if (!canMoveTo(next)) {
            error("inspection cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"inspection cycle"} $code"

    fun effectiveLoad(): Double = round((floorValue - sequence.toDouble()) * 100) / 100

    fun snapshot(): InspectionCycle {
        return InspectionCycle(
            code = code,
            label = label,
            depot = depot,
            contactEmail = contactEmail,
            floorValue = floorValue,
            archived = archived,
            durationMinutes = durationMinutes,
            sequence = sequence,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a inspection cycle in status open. */
fun newInspectionCycle(code: String, label: String): InspectionCycle {
    return InspectionCycle(
        code = code,
        label = label,
        depot = "",
        contactEmail = "",
        floorValue = 0.0,
        archived = false,
        durationMinutes = 0,
        sequence = 0,
        summary = "",
        status = InspectionCycleStatus.OPEN,
    )
}
