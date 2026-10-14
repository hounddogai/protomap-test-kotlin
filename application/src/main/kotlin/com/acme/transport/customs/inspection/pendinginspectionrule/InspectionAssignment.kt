package com.acme.transport.customs.inspection.pendinginspectionrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection assignment is in its life cycle. */
enum class InspectionAssignmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A inspection assignment as the inspection team keeps it. */
data class InspectionAssignment(
    var code: String,
    var label: String,
    var externalRef: String,
    var etaMinutes: Int,
    var score: Double,
    var country: String,
    var ratio: Double,
    var status: InspectionAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionAssignmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionAssignmentStatus): Boolean {
        when (status) {
            InspectionAssignmentStatus.IDLE -> {
                when (next) {
                    InspectionAssignmentStatus.DRAINING -> return true
                }
                return false
            }
            InspectionAssignmentStatus.WARMING -> return next == InspectionAssignmentStatus.READY || next == InspectionAssignmentStatus.DRAINING
            InspectionAssignmentStatus.READY -> {
                when (next) {
                    InspectionAssignmentStatus.DRAINING, InspectionAssignmentStatus.STOPPED -> return true
                }
                return false
            }
            InspectionAssignmentStatus.DRAINING -> return next == InspectionAssignmentStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: InspectionAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("inspection assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedTotal(): Double = round((ratio + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): InspectionAssignment {
        return InspectionAssignment(
            code = code,
            label = label,
            externalRef = externalRef,
            etaMinutes = etaMinutes,
            score = score,
            country = country,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a inspection assignment in status idle. */
fun newInspectionAssignment(code: String, label: String): InspectionAssignment {
    return InspectionAssignment(
        code = code,
        label = label,
        externalRef = "",
        etaMinutes = 0,
        score = 0.0,
        country = "",
        ratio = 0.0,
        status = InspectionAssignmentStatus.IDLE,
    )
}
