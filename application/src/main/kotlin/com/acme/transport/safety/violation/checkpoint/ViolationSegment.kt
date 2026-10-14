package com.acme.transport.safety.violation.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a violation segment is in its life cycle. */
enum class ViolationSegmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One violation segment, with what the planners need to act on it. */
data class ViolationSegment(
    var code: String,
    var title: String,
    var score: Double,
    var name: String,
    var margin: Double,
    var status: ViolationSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationSegmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationSegmentStatus): Boolean {
        when (status) {
            ViolationSegmentStatus.REQUESTED -> {
                when (next) {
                    ViolationSegmentStatus.REVOKED -> return true
                }
                return false
            }
            ViolationSegmentStatus.GRANTED -> return next == ViolationSegmentStatus.REVOKED || next == ViolationSegmentStatus.EXPIRED
            ViolationSegmentStatus.REVOKED -> {
                when (next) {
                    ViolationSegmentStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ViolationSegmentStatus) {
        if (!canMoveTo(next)) {
            error("violation segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun adjustedLoad(): Double = round(score * margin * 100) / 100

    fun snapshot(): ViolationSegment {
        return ViolationSegment(
            code = code,
            title = title,
            score = score,
            name = name,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a violation segment in status requested. */
fun newViolationSegment(code: String, title: String): ViolationSegment {
    return ViolationSegment(
        code = code,
        title = title,
        score = 0.0,
        name = "",
        margin = 0.0,
        status = ViolationSegmentStatus.REQUESTED,
    )
}
