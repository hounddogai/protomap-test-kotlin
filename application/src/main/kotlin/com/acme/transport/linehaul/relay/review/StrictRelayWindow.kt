package com.acme.transport.linehaul.relay.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict relay window is in its life cycle. */
enum class StrictRelayWindowStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One strict relay window, with what the planners need to act on it. */
data class StrictRelayWindow(
    var code: String,
    var label: String,
    var priority: Int,
    var approved: Boolean,
    var floorValue: Double,
    var cost: Double,
    var graceDays: Int,
    var budget: Double,
    var status: StrictRelayWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictRelayWindowStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictRelayWindowStatus): Boolean {
        when (status) {
            StrictRelayWindowStatus.OPEN -> return next == StrictRelayWindowStatus.IN_REVIEW
            StrictRelayWindowStatus.IN_REVIEW -> {
                when (next) {
                    StrictRelayWindowStatus.RESOLVED -> return true
                }
                return false
            }
            StrictRelayWindowStatus.RESOLVED -> return next == StrictRelayWindowStatus.REOPENED || next == StrictRelayWindowStatus.ARCHIVED
            StrictRelayWindowStatus.REOPENED -> {
                when (next) {
                    StrictRelayWindowStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictRelayWindowStatus) {
        if (!canMoveTo(next)) {
            error("strict relay window $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"strict relay window"} $code"

    fun weightedValue(): Double = round(budget * graceDays.toDouble() * 100) / 100

    fun snapshot(): StrictRelayWindow {
        return StrictRelayWindow(
            code = code,
            label = label,
            priority = priority,
            approved = approved,
            floorValue = floorValue,
            cost = cost,
            graceDays = graceDays,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a strict relay window in status open. */
fun newStrictRelayWindow(code: String, label: String): StrictRelayWindow {
    return StrictRelayWindow(
        code = code,
        label = label,
        priority = 0,
        approved = false,
        floorValue = 0.0,
        cost = 0.0,
        graceDays = 0,
        budget = 0.0,
        status = StrictRelayWindowStatus.OPEN,
    )
}
