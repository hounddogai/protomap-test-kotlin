package com.acme.transport.fleetops.inspection.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient inspection segment is in its life cycle. */
enum class LenientInspectionSegmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One lenient inspection segment, with what the planners need to act on it. */
data class LenientInspectionSegment(
    var code: String,
    var label: String,
    var locale: String,
    var score: Double,
    var routeCode: String,
    var sequence: Int,
    var status: LenientInspectionSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientInspectionSegmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientInspectionSegmentStatus): Boolean {
        when (status) {
            LenientInspectionSegmentStatus.REQUESTED -> return next == LenientInspectionSegmentStatus.GRANTED
            LenientInspectionSegmentStatus.GRANTED -> {
                when (next) {
                    LenientInspectionSegmentStatus.EXPIRED -> return true
                }
                return false
            }
            LenientInspectionSegmentStatus.REVOKED -> {
                when (next) {
                    LenientInspectionSegmentStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientInspectionSegmentStatus) {
        if (!canMoveTo(next)) {
            error("lenient inspection segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"lenient inspection segment"} $code"

    fun projectedVolume(): Double = round(score * sequence.toDouble() * 100) / 100

    fun snapshot(): LenientInspectionSegment {
        return LenientInspectionSegment(
            code = code,
            label = label,
            locale = locale,
            score = score,
            routeCode = routeCode,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a lenient inspection segment in status requested. */
fun newLenientInspectionSegment(code: String, label: String): LenientInspectionSegment {
    return LenientInspectionSegment(
        code = code,
        label = label,
        locale = "",
        score = 0.0,
        routeCode = "",
        sequence = 0,
        status = LenientInspectionSegmentStatus.REQUESTED,
    )
}
