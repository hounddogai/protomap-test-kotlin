package com.acme.transport.safety.nearmiss.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive near miss segment records of safety's near miss work, and the rules, stores, and calculations
// around them.

/** Where a adaptive near miss segment is in its life cycle. */
enum class AdaptiveNearMissSegmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One adaptive near miss segment, with what the planners need to act on it. */
data class AdaptiveNearMissSegment(
    var code: String,
    var title: String,
    var visible: Boolean,
    var weight: Double,
    var pallets: Int,
    var ratio: Double,
    var status: AdaptiveNearMissSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveNearMissSegmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveNearMissSegmentStatus): Boolean {
        when (status) {
            AdaptiveNearMissSegmentStatus.PLANNED -> return next == AdaptiveNearMissSegmentStatus.CANCELLED
            AdaptiveNearMissSegmentStatus.SCHEDULED -> {
                when (next) {
                    AdaptiveNearMissSegmentStatus.IN_PROGRESS, AdaptiveNearMissSegmentStatus.CANCELLED -> return true
                }
                return false
            }
            AdaptiveNearMissSegmentStatus.IN_PROGRESS -> {
                when (next) {
                    AdaptiveNearMissSegmentStatus.COMPLETED -> return true
                }
                return false
            }
            AdaptiveNearMissSegmentStatus.COMPLETED -> return next == AdaptiveNearMissSegmentStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: AdaptiveNearMissSegmentStatus) {
        if (!canMoveTo(next)) {
            error("adaptive near miss segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"adaptive near miss segment"} $code"

    fun netExposure(): Double = round(ratio * weight * 100) / 100

    fun snapshot(): AdaptiveNearMissSegment {
        return AdaptiveNearMissSegment(
            code = code,
            title = title,
            visible = visible,
            weight = weight,
            pallets = pallets,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive near miss segment in status planned. */
fun newAdaptiveNearMissSegment(code: String, title: String): AdaptiveNearMissSegment {
    return AdaptiveNearMissSegment(
        code = code,
        title = title,
        visible = false,
        weight = 0.0,
        pallets = 0,
        ratio = 0.0,
        status = AdaptiveNearMissSegmentStatus.PLANNED,
    )
}
