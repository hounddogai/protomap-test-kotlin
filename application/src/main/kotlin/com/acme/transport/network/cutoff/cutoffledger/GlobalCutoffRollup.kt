package com.acme.transport.network.cutoff.cutoffledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global cutoff rollup is in its life cycle. */
enum class GlobalCutoffRollupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One global cutoff rollup, with what the planners need to act on it. */
data class GlobalCutoffRollup(
    var code: String,
    var title: String,
    var quantity: Int,
    var source: String,
    var budget: Double,
    var lane: String,
    var reviewer: String,
    var region: String,
    var status: GlobalCutoffRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalCutoffRollupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalCutoffRollupStatus): Boolean {
        when (status) {
            GlobalCutoffRollupStatus.PLANNED -> return next == GlobalCutoffRollupStatus.IN_PROGRESS
            GlobalCutoffRollupStatus.SCHEDULED -> {
                when (next) {
                    GlobalCutoffRollupStatus.IN_PROGRESS -> return true
                }
                return false
            }
            GlobalCutoffRollupStatus.IN_PROGRESS -> return next == GlobalCutoffRollupStatus.CANCELLED
            GlobalCutoffRollupStatus.COMPLETED -> {
                when (next) {
                    GlobalCutoffRollupStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalCutoffRollupStatus) {
        if (!canMoveTo(next)) {
            error("global cutoff rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun adjustedValue(): Double = round(budget * quantity.toDouble() * 100) / 100

    fun snapshot(): GlobalCutoffRollup {
        return GlobalCutoffRollup(
            code = code,
            title = title,
            quantity = quantity,
            source = source,
            budget = budget,
            lane = lane,
            reviewer = reviewer,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a global cutoff rollup in status planned. */
fun newGlobalCutoffRollup(code: String, title: String): GlobalCutoffRollup {
    return GlobalCutoffRollup(
        code = code,
        title = title,
        quantity = 0,
        source = "",
        budget = 0.0,
        lane = "",
        reviewer = "",
        region = "",
        status = GlobalCutoffRollupStatus.PLANNED,
    )
}
