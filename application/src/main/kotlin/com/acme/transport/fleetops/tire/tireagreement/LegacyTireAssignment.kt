package com.acme.transport.fleetops.tire.tireagreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy tire assignment is in its life cycle. */
enum class LegacyTireAssignmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A legacy tire assignment. Its code is unique within a tenant. */
data class LegacyTireAssignment(
    var code: String,
    var title: String,
    var segment: String,
    var pallets: Int,
    var etaMinutes: Int,
    var status: LegacyTireAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyTireAssignmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyTireAssignmentStatus): Boolean {
        when (status) {
            LegacyTireAssignmentStatus.PLANNED -> return next == LegacyTireAssignmentStatus.IN_PROGRESS || next == LegacyTireAssignmentStatus.CANCELLED
            LegacyTireAssignmentStatus.SCHEDULED -> return next == LegacyTireAssignmentStatus.CANCELLED
            LegacyTireAssignmentStatus.IN_PROGRESS -> return next == LegacyTireAssignmentStatus.COMPLETED || next == LegacyTireAssignmentStatus.CANCELLED
            LegacyTireAssignmentStatus.COMPLETED -> return next == LegacyTireAssignmentStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LegacyTireAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("legacy tire assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"legacy tire assignment"} $code"

    fun projectedValue(): Double = round(etaMinutes.toDouble() * pallets.toDouble() * 100) / 100

    fun snapshot(): LegacyTireAssignment {
        return LegacyTireAssignment(
            code = code,
            title = title,
            segment = segment,
            pallets = pallets,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a legacy tire assignment in status planned. */
fun newLegacyTireAssignment(code: String, title: String): LegacyTireAssignment {
    return LegacyTireAssignment(
        code = code,
        title = title,
        segment = "",
        pallets = 0,
        etaMinutes = 0,
        status = LegacyTireAssignmentStatus.PLANNED,
    )
}
