package com.acme.transport.terminal.sortation.strictsortationdigest

import com.acme.transport.kit.text.normalize

/** Where a fallback sortation score is in its life cycle. */
enum class FallbackSortationScoreStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A fallback sortation score as the sortation team keeps it. */
data class FallbackSortationScore(
    var code: String,
    var label: String,
    var trailerId: String,
    var summary: String,
    var maximum: Double,
    var visible: Boolean,
    var status: FallbackSortationScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackSortationScoreStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackSortationScoreStatus): Boolean {
        when (status) {
            FallbackSortationScoreStatus.IDLE -> {
                when (next) {
                    FallbackSortationScoreStatus.WARMING, FallbackSortationScoreStatus.READY -> return true
                }
                return false
            }
            FallbackSortationScoreStatus.WARMING -> {
                when (next) {
                    FallbackSortationScoreStatus.READY, FallbackSortationScoreStatus.DRAINING -> return true
                }
                return false
            }
            FallbackSortationScoreStatus.READY -> {
                when (next) {
                    FallbackSortationScoreStatus.STOPPED -> return true
                }
                return false
            }
            FallbackSortationScoreStatus.DRAINING -> return next == FallbackSortationScoreStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: FallbackSortationScoreStatus) {
        if (!canMoveTo(next)) {
            error("fallback sortation score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = maximum

    fun snapshot(): FallbackSortationScore {
        return FallbackSortationScore(
            code = code,
            label = label,
            trailerId = trailerId,
            summary = summary,
            maximum = maximum,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback sortation score in status idle. */
fun newFallbackSortationScore(code: String, label: String): FallbackSortationScore {
    return FallbackSortationScore(
        code = code,
        label = label,
        trailerId = "",
        summary = "",
        maximum = 0.0,
        visible = false,
        status = FallbackSortationScoreStatus.IDLE,
    )
}
