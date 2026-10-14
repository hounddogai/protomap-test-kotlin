package com.acme.transport.crewing.restbreak.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback rest break cycle is in its life cycle. */
enum class FallbackRestBreakCycleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A fallback rest break cycle. Its code is unique within a tenant. */
data class FallbackRestBreakCycle(
    var code: String,
    var name: String,
    var odometer: Int,
    var createdAt: Int,
    var revision: Int,
    var ratio: Double,
    var ceiling: Double,
    var status: FallbackRestBreakCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackRestBreakCycleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackRestBreakCycleStatus): Boolean {
        when (status) {
            FallbackRestBreakCycleStatus.PLANNED -> return next == FallbackRestBreakCycleStatus.SCHEDULED
            FallbackRestBreakCycleStatus.SCHEDULED -> return next == FallbackRestBreakCycleStatus.IN_PROGRESS || next == FallbackRestBreakCycleStatus.COMPLETED
            FallbackRestBreakCycleStatus.IN_PROGRESS -> {
                when (next) {
                    FallbackRestBreakCycleStatus.COMPLETED, FallbackRestBreakCycleStatus.CANCELLED -> return true
                }
                return false
            }
            FallbackRestBreakCycleStatus.COMPLETED -> return next == FallbackRestBreakCycleStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: FallbackRestBreakCycleStatus) {
        if (!canMoveTo(next)) {
            error("fallback rest break cycle $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "${"fallback rest break cycle"} $code"

    fun adjustedLoad(): Double = round(ratio * ceiling * 100) / 100

    fun snapshot(): FallbackRestBreakCycle {
        return FallbackRestBreakCycle(
            code = code,
            name = name,
            odometer = odometer,
            createdAt = createdAt,
            revision = revision,
            ratio = ratio,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fallback rest break cycle in status planned. */
fun newFallbackRestBreakCycle(code: String, name: String): FallbackRestBreakCycle {
    return FallbackRestBreakCycle(
        code = code,
        name = name,
        odometer = 0,
        createdAt = 0,
        revision = 0,
        ratio = 0.0,
        ceiling = 0.0,
        status = FallbackRestBreakCycleStatus.PLANNED,
    )
}
