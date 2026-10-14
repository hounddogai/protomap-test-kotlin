package com.acme.transport.terminal.traileryard.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal trailer yard rollup records of terminal's trailer yard work, and the rules, stores, and
// calculations around them.

/** Where a seasonal trailer yard rollup is in its life cycle. */
enum class SeasonalTrailerYardRollupStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A seasonal trailer yard rollup as the trailer yard team keeps it. */
data class SeasonalTrailerYardRollup(
    var code: String,
    var label: String,
    var threshold: Double,
    var archived: Boolean,
    var priority: Int,
    var description: String,
    var cost: Double,
    var dock: String,
    var status: SeasonalTrailerYardRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalTrailerYardRollupStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalTrailerYardRollupStatus): Boolean {
        when (status) {
            SeasonalTrailerYardRollupStatus.PROPOSED -> {
                when (next) {
                    SeasonalTrailerYardRollupStatus.APPROVED -> return true
                }
                return false
            }
            SeasonalTrailerYardRollupStatus.APPROVED -> {
                when (next) {
                    SeasonalTrailerYardRollupStatus.REJECTED, SeasonalTrailerYardRollupStatus.WITHDRAWN -> return true
                }
                return false
            }
            SeasonalTrailerYardRollupStatus.REJECTED -> {
                when (next) {
                    SeasonalTrailerYardRollupStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalTrailerYardRollupStatus) {
        if (!canMoveTo(next)) {
            error("seasonal trailer yard rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedValue(): Double = round(cost * threshold * 100) / 100

    fun snapshot(): SeasonalTrailerYardRollup {
        return SeasonalTrailerYardRollup(
            code = code,
            label = label,
            threshold = threshold,
            archived = archived,
            priority = priority,
            description = description,
            cost = cost,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal trailer yard rollup in status proposed. */
fun newSeasonalTrailerYardRollup(code: String, label: String): SeasonalTrailerYardRollup {
    return SeasonalTrailerYardRollup(
        code = code,
        label = label,
        threshold = 0.0,
        archived = false,
        priority = 0,
        description = "",
        cost = 0.0,
        dock = "",
        status = SeasonalTrailerYardRollupStatus.PROPOSED,
    )
}
