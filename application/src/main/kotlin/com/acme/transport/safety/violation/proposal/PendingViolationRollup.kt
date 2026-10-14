package com.acme.transport.safety.violation.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending violation rollup is in its life cycle. */
enum class PendingViolationRollupStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One pending violation rollup, with what the planners need to act on it. */
data class PendingViolationRollup(
    var code: String,
    var name: String,
    var attempts: Int,
    var limit: Int,
    var priority: Int,
    var status: PendingViolationRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingViolationRollupStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingViolationRollupStatus): Boolean {
        when (status) {
            PendingViolationRollupStatus.IDLE -> return next == PendingViolationRollupStatus.WARMING || next == PendingViolationRollupStatus.READY
            PendingViolationRollupStatus.WARMING -> return next == PendingViolationRollupStatus.READY || next == PendingViolationRollupStatus.DRAINING
            PendingViolationRollupStatus.READY -> return next == PendingViolationRollupStatus.DRAINING || next == PendingViolationRollupStatus.STOPPED
            PendingViolationRollupStatus.DRAINING -> {
                when (next) {
                    PendingViolationRollupStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingViolationRollupStatus) {
        if (!canMoveTo(next)) {
            error("pending violation rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun adjustedLoad(): Double = round((priority.toDouble() + limit.toDouble()) * 100) / 100

    fun snapshot(): PendingViolationRollup {
        return PendingViolationRollup(
            code = code,
            name = name,
            attempts = attempts,
            limit = limit,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending violation rollup in status idle. */
fun newPendingViolationRollup(code: String, name: String): PendingViolationRollup {
    return PendingViolationRollup(
        code = code,
        name = name,
        attempts = 0,
        limit = 0,
        priority = 0,
        status = PendingViolationRollupStatus.IDLE,
    )
}
