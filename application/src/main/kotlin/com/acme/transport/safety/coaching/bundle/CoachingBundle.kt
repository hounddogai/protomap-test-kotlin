package com.acme.transport.safety.coaching.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The coaching bundle records of safety's coaching work, and the rules, stores, and calculations around them.

/** Where a coaching bundle is in its life cycle. */
enum class CoachingBundleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One coaching bundle, with what the planners need to act on it. */
data class CoachingBundle(
    var code: String,
    var name: String,
    var label: String,
    var weight: Double,
    var attempts: Int,
    var priority: Int,
    var status: CoachingBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingBundleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingBundleStatus): Boolean {
        when (status) {
            CoachingBundleStatus.PLANNED -> return next == CoachingBundleStatus.IN_PROGRESS
            CoachingBundleStatus.SCHEDULED -> {
                when (next) {
                    CoachingBundleStatus.COMPLETED, CoachingBundleStatus.CANCELLED -> return true
                }
                return false
            }
            CoachingBundleStatus.IN_PROGRESS -> return next == CoachingBundleStatus.COMPLETED || next == CoachingBundleStatus.CANCELLED
            CoachingBundleStatus.COMPLETED -> {
                when (next) {
                    CoachingBundleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CoachingBundleStatus) {
        if (!canMoveTo(next)) {
            error("coaching bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"coaching bundle"} $code"

    fun weightedTotal(): Double = round((attempts.toDouble() + weight) * 100) / 100

    fun snapshot(): CoachingBundle {
        return CoachingBundle(
            code = code,
            name = name,
            label = label,
            weight = weight,
            attempts = attempts,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a coaching bundle in status planned. */
fun newCoachingBundle(code: String, name: String): CoachingBundle {
    return CoachingBundle(
        code = code,
        name = name,
        label = "",
        weight = 0.0,
        attempts = 0,
        priority = 0,
        status = CoachingBundleStatus.PLANNED,
    )
}
