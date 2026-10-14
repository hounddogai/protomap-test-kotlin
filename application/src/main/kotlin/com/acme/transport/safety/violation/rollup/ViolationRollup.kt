package com.acme.transport.safety.violation.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The violation rollup records of safety's violation work, and the rules, stores, and calculations around
// them.

/** Where a violation rollup is in its life cycle. */
enum class ViolationRollupStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One violation rollup, with what the planners need to act on it. */
data class ViolationRollup(
    var code: String,
    var label: String,
    var priority: Int,
    var distanceKm: Double,
    var stopCount: Int,
    var reviewer: String,
    var status: ViolationRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationRollupStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationRollupStatus): Boolean {
        when (status) {
            ViolationRollupStatus.IDLE -> return next == ViolationRollupStatus.WARMING
            ViolationRollupStatus.WARMING -> return next == ViolationRollupStatus.READY
            ViolationRollupStatus.READY -> return next == ViolationRollupStatus.STOPPED
            ViolationRollupStatus.DRAINING -> return next == ViolationRollupStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ViolationRollupStatus) {
        if (!canMoveTo(next)) {
            error("violation rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"violation rollup"} $code"

    fun weightedLoad(): Double = round((stopCount.toDouble() - distanceKm) * 100) / 100

    fun snapshot(): ViolationRollup {
        return ViolationRollup(
            code = code,
            label = label,
            priority = priority,
            distanceKm = distanceKm,
            stopCount = stopCount,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a violation rollup in status idle. */
fun newViolationRollup(code: String, label: String): ViolationRollup {
    return ViolationRollup(
        code = code,
        label = label,
        priority = 0,
        distanceKm = 0.0,
        stopCount = 0,
        reviewer = "",
        status = ViolationRollupStatus.IDLE,
    )
}
