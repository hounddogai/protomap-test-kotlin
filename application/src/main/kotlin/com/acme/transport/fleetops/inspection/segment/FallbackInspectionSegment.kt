package com.acme.transport.fleetops.inspection.segment

import com.acme.transport.kit.text.normalize

/** Where a fallback inspection segment is in its life cycle. */
enum class FallbackInspectionSegmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A fallback inspection segment. Its code is unique within a tenant. */
data class FallbackInspectionSegment(
    var code: String,
    var label: String,
    var shiftCode: String,
    var revision: Int,
    var ownerId: String,
    var approved: Boolean,
    var status: FallbackInspectionSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackInspectionSegmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackInspectionSegmentStatus): Boolean {
        when (status) {
            FallbackInspectionSegmentStatus.OPEN -> return next == FallbackInspectionSegmentStatus.IN_REVIEW || next == FallbackInspectionSegmentStatus.REOPENED
            FallbackInspectionSegmentStatus.IN_REVIEW -> {
                when (next) {
                    FallbackInspectionSegmentStatus.ARCHIVED -> return true
                }
                return false
            }
            FallbackInspectionSegmentStatus.RESOLVED -> return next == FallbackInspectionSegmentStatus.REOPENED || next == FallbackInspectionSegmentStatus.ARCHIVED
            FallbackInspectionSegmentStatus.REOPENED -> {
                when (next) {
                    FallbackInspectionSegmentStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackInspectionSegmentStatus) {
        if (!canMoveTo(next)) {
            error("fallback inspection segment $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = revision.toDouble()

    fun snapshot(): FallbackInspectionSegment {
        return FallbackInspectionSegment(
            code = code,
            label = label,
            shiftCode = shiftCode,
            revision = revision,
            ownerId = ownerId,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback inspection segment in status open. */
fun newFallbackInspectionSegment(code: String, label: String): FallbackInspectionSegment {
    return FallbackInspectionSegment(
        code = code,
        label = label,
        shiftCode = "",
        revision = 0,
        ownerId = "",
        approved = false,
        status = FallbackInspectionSegmentStatus.OPEN,
    )
}
