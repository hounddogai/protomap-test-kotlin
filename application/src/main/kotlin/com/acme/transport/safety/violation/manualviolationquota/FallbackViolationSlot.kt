package com.acme.transport.safety.violation.manualviolationquota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback violation slot is in its life cycle. */
enum class FallbackViolationSlotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One fallback violation slot, with what the planners need to act on it. */
data class FallbackViolationSlot(
    var code: String,
    var title: String,
    var locale: String,
    var ceiling: Double,
    var priority: Int,
    var reviewer: String,
    var windowDays: Int,
    var status: FallbackViolationSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackViolationSlotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackViolationSlotStatus): Boolean {
        when (status) {
            FallbackViolationSlotStatus.OPEN -> return next == FallbackViolationSlotStatus.REOPENED || next == FallbackViolationSlotStatus.ARCHIVED
            FallbackViolationSlotStatus.IN_REVIEW -> {
                when (next) {
                    FallbackViolationSlotStatus.REOPENED, FallbackViolationSlotStatus.ARCHIVED -> return true
                }
                return false
            }
            FallbackViolationSlotStatus.RESOLVED -> {
                when (next) {
                    FallbackViolationSlotStatus.REOPENED, FallbackViolationSlotStatus.ARCHIVED -> return true
                }
                return false
            }
            FallbackViolationSlotStatus.REOPENED -> return next == FallbackViolationSlotStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: FallbackViolationSlotStatus) {
        if (!canMoveTo(next)) {
            error("fallback violation slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fallback violation slot"} $code"

    fun weightedLoad(): Double = round(ceiling * windowDays.toDouble() * 100) / 100

    fun snapshot(): FallbackViolationSlot {
        return FallbackViolationSlot(
            code = code,
            title = title,
            locale = locale,
            ceiling = ceiling,
            priority = priority,
            reviewer = reviewer,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a fallback violation slot in status open. */
fun newFallbackViolationSlot(code: String, title: String): FallbackViolationSlot {
    return FallbackViolationSlot(
        code = code,
        title = title,
        locale = "",
        ceiling = 0.0,
        priority = 0,
        reviewer = "",
        windowDays = 0,
        status = FallbackViolationSlotStatus.OPEN,
    )
}
