package com.acme.transport.terminal.sortation.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling sortation window records of terminal's sortation work, and the rules, stores, and calculations
// around them.

/** Where a rolling sortation window is in its life cycle. */
enum class RollingSortationWindowStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One rolling sortation window, with what the planners need to act on it. */
data class RollingSortationWindow(
    var code: String,
    var name: String,
    var graceDays: Int,
    var approved: Boolean,
    var active: Boolean,
    var dock: String,
    var stopCount: Int,
    var pinned: Boolean,
    var status: RollingSortationWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingSortationWindowStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingSortationWindowStatus): Boolean {
        when (status) {
            RollingSortationWindowStatus.OPEN -> return next == RollingSortationWindowStatus.IN_REVIEW || next == RollingSortationWindowStatus.ARCHIVED
            RollingSortationWindowStatus.IN_REVIEW -> return next == RollingSortationWindowStatus.RESOLVED || next == RollingSortationWindowStatus.REOPENED
            RollingSortationWindowStatus.RESOLVED -> {
                when (next) {
                    RollingSortationWindowStatus.REOPENED, RollingSortationWindowStatus.ARCHIVED -> return true
                }
                return false
            }
            RollingSortationWindowStatus.REOPENED -> return next == RollingSortationWindowStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RollingSortationWindowStatus) {
        if (!canMoveTo(next)) {
            error("rolling sortation window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rolling sortation window"} $code"

    fun projectedLoad(): Double = round((stopCount.toDouble() + graceDays.toDouble()) * 100) / 100

    fun snapshot(): RollingSortationWindow {
        return RollingSortationWindow(
            code = code,
            name = name,
            graceDays = graceDays,
            approved = approved,
            active = active,
            dock = dock,
            stopCount = stopCount,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling sortation window in status open. */
fun newRollingSortationWindow(code: String, name: String): RollingSortationWindow {
    return RollingSortationWindow(
        code = code,
        name = name,
        graceDays = 0,
        approved = false,
        active = false,
        dock = "",
        stopCount = 0,
        pinned = false,
        status = RollingSortationWindowStatus.OPEN,
    )
}
