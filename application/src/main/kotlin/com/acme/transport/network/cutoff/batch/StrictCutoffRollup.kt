package com.acme.transport.network.cutoff.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict cutoff rollup is in its life cycle. */
enum class StrictCutoffRollupStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A strict cutoff rollup. Its code is unique within a tenant. */
data class StrictCutoffRollup(
    var code: String,
    var label: String,
    var maximum: Double,
    var floorValue: Double,
    var depot: String,
    var version: Int,
    var status: StrictCutoffRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictCutoffRollupStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictCutoffRollupStatus): Boolean {
        when (status) {
            StrictCutoffRollupStatus.IDLE -> return next == StrictCutoffRollupStatus.WARMING || next == StrictCutoffRollupStatus.DRAINING
            StrictCutoffRollupStatus.WARMING -> {
                when (next) {
                    StrictCutoffRollupStatus.DRAINING, StrictCutoffRollupStatus.STOPPED -> return true
                }
                return false
            }
            StrictCutoffRollupStatus.READY -> return next == StrictCutoffRollupStatus.DRAINING
            StrictCutoffRollupStatus.DRAINING -> return next == StrictCutoffRollupStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: StrictCutoffRollupStatus) {
        if (!canMoveTo(next)) {
            error("strict cutoff rollup $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedValue(): Double = round((maximum - version.toDouble()) * 100) / 100

    fun snapshot(): StrictCutoffRollup {
        return StrictCutoffRollup(
            code = code,
            label = label,
            maximum = maximum,
            floorValue = floorValue,
            depot = depot,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a strict cutoff rollup in status idle. */
fun newStrictCutoffRollup(code: String, label: String): StrictCutoffRollup {
    return StrictCutoffRollup(
        code = code,
        label = label,
        maximum = 0.0,
        floorValue = 0.0,
        depot = "",
        version = 0,
        status = StrictCutoffRollupStatus.IDLE,
    )
}
