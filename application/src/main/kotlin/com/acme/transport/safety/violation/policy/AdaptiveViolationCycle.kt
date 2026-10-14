package com.acme.transport.safety.violation.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive violation cycle is in its life cycle. */
enum class AdaptiveViolationCycleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One adaptive violation cycle, with what the planners need to act on it. */
data class AdaptiveViolationCycle(
    var code: String,
    var title: String,
    var version: Int,
    var capacity: Int,
    var budget: Double,
    var attempts: Int,
    var status: AdaptiveViolationCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveViolationCycleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveViolationCycleStatus): Boolean {
        when (status) {
            AdaptiveViolationCycleStatus.DRAFT -> {
                when (next) {
                    AdaptiveViolationCycleStatus.PENDING, AdaptiveViolationCycleStatus.SUSPENDED -> return true
                }
                return false
            }
            AdaptiveViolationCycleStatus.PENDING -> {
                when (next) {
                    AdaptiveViolationCycleStatus.CLOSED -> return true
                }
                return false
            }
            AdaptiveViolationCycleStatus.ACTIVE -> {
                when (next) {
                    AdaptiveViolationCycleStatus.CLOSED -> return true
                }
                return false
            }
            AdaptiveViolationCycleStatus.SUSPENDED -> {
                when (next) {
                    AdaptiveViolationCycleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveViolationCycleStatus) {
        if (!canMoveTo(next)) {
            error("adaptive violation cycle $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"adaptive violation cycle"} $code"

    fun netExposure(): Double = round((attempts.toDouble() + budget) * 100) / 100

    fun snapshot(): AdaptiveViolationCycle {
        return AdaptiveViolationCycle(
            code = code,
            title = title,
            version = version,
            capacity = capacity,
            budget = budget,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive violation cycle in status draft. */
fun newAdaptiveViolationCycle(code: String, title: String): AdaptiveViolationCycle {
    return AdaptiveViolationCycle(
        code = code,
        title = title,
        version = 0,
        capacity = 0,
        budget = 0.0,
        attempts = 0,
        status = AdaptiveViolationCycleStatus.DRAFT,
    )
}
