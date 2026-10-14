package com.acme.transport.crewing.swap.baseline

import com.acme.transport.kit.text.normalize

// The swap baseline records of crewing's swap work, and the rules, stores, and calculations around them.

/** Where a swap baseline is in its life cycle. */
enum class SwapBaselineStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A swap baseline. Its code is unique within a tenant. */
data class SwapBaseline(
    var code: String,
    var label: String,
    var title: String,
    var cost: Double,
    var locale: String,
    var status: SwapBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapBaselineStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapBaselineStatus): Boolean {
        when (status) {
            SwapBaselineStatus.PLANNED -> return next == SwapBaselineStatus.SCHEDULED || next == SwapBaselineStatus.COMPLETED
            SwapBaselineStatus.SCHEDULED -> return next == SwapBaselineStatus.IN_PROGRESS
            SwapBaselineStatus.IN_PROGRESS -> return next == SwapBaselineStatus.COMPLETED
            SwapBaselineStatus.COMPLETED -> return next == SwapBaselineStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: SwapBaselineStatus) {
        if (!canMoveTo(next)) {
            error("swap baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"swap baseline"} $code"

    fun measuredValue(): Double = cost

    fun snapshot(): SwapBaseline {
        return SwapBaseline(
            code = code,
            label = label,
            title = title,
            cost = cost,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a swap baseline in status planned. */
fun newSwapBaseline(code: String, label: String): SwapBaseline {
    return SwapBaseline(
        code = code,
        label = label,
        title = "",
        cost = 0.0,
        locale = "",
        status = SwapBaselineStatus.PLANNED,
    )
}
