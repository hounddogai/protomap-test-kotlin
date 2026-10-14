package com.acme.transport.customs.inspection.inspectionrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback inspection segment is in its life cycle. */
enum class FallbackInspectionSegmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One fallback inspection segment, with what the planners need to act on it. */
data class FallbackInspectionSegment(
    var code: String,
    var title: String,
    var budget: Double,
    var segment: String,
    var minimum: Double,
    var startsAt: Int,
    var endsAt: Int,
    var stopCount: Int,
    var status: FallbackInspectionSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackInspectionSegmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackInspectionSegmentStatus): Boolean {
        when (status) {
            FallbackInspectionSegmentStatus.PROPOSED -> {
                when (next) {
                    FallbackInspectionSegmentStatus.APPROVED, FallbackInspectionSegmentStatus.REJECTED -> return true
                }
                return false
            }
            FallbackInspectionSegmentStatus.APPROVED -> return next == FallbackInspectionSegmentStatus.REJECTED || next == FallbackInspectionSegmentStatus.WITHDRAWN
            FallbackInspectionSegmentStatus.REJECTED -> return next == FallbackInspectionSegmentStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: FallbackInspectionSegmentStatus) {
        if (!canMoveTo(next)) {
            error("fallback inspection segment $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"fallback inspection segment"} $code"

    fun projectedVolume(): Double = round((minimum - startsAt.toDouble()) * 100) / 100

    fun snapshot(): FallbackInspectionSegment {
        return FallbackInspectionSegment(
            code = code,
            title = title,
            budget = budget,
            segment = segment,
            minimum = minimum,
            startsAt = startsAt,
            endsAt = endsAt,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a fallback inspection segment in status proposed. */
fun newFallbackInspectionSegment(code: String, title: String): FallbackInspectionSegment {
    return FallbackInspectionSegment(
        code = code,
        title = title,
        budget = 0.0,
        segment = "",
        minimum = 0.0,
        startsAt = 0,
        endsAt = 0,
        stopCount = 0,
        status = FallbackInspectionSegmentStatus.PROPOSED,
    )
}
