package com.acme.transport.safety.coaching.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive coaching cycle is in its life cycle. */
enum class AdaptiveCoachingCycleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A adaptive coaching cycle. Its code is unique within a tenant. */
data class AdaptiveCoachingCycle(
    var code: String,
    var name: String,
    var trailerId: String,
    var budget: Double,
    var threshold: Double,
    var status: AdaptiveCoachingCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveCoachingCycleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveCoachingCycleStatus): Boolean {
        when (status) {
            AdaptiveCoachingCycleStatus.IDLE -> return next == AdaptiveCoachingCycleStatus.WARMING || next == AdaptiveCoachingCycleStatus.READY
            AdaptiveCoachingCycleStatus.WARMING -> return next == AdaptiveCoachingCycleStatus.READY
            AdaptiveCoachingCycleStatus.READY -> return next == AdaptiveCoachingCycleStatus.DRAINING || next == AdaptiveCoachingCycleStatus.STOPPED
            AdaptiveCoachingCycleStatus.DRAINING -> {
                when (next) {
                    AdaptiveCoachingCycleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveCoachingCycleStatus) {
        if (!canMoveTo(next)) {
            error("adaptive coaching cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun adjustedVolume(): Double = round((threshold - budget) * 100) / 100

    fun snapshot(): AdaptiveCoachingCycle {
        return AdaptiveCoachingCycle(
            code = code,
            name = name,
            trailerId = trailerId,
            budget = budget,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive coaching cycle in status idle. */
fun newAdaptiveCoachingCycle(code: String, name: String): AdaptiveCoachingCycle {
    return AdaptiveCoachingCycle(
        code = code,
        name = name,
        trailerId = "",
        budget = 0.0,
        threshold = 0.0,
        status = AdaptiveCoachingCycleStatus.IDLE,
    )
}
