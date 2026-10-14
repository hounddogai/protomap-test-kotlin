package com.acme.transport.network.transittime.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy transit time rollup records of network's transit time work, and the rules, stores, and
// calculations around them.

/** Where a legacy transit time rollup is in its life cycle. */
enum class LegacyTransitTimeRollupStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A legacy transit time rollup as the transit time team keeps it. */
data class LegacyTransitTimeRollup(
    var code: String,
    var label: String,
    var minimum: Double,
    var sequence: Int,
    var startsAt: Int,
    var status: LegacyTransitTimeRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyTransitTimeRollupStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyTransitTimeRollupStatus): Boolean {
        when (status) {
            LegacyTransitTimeRollupStatus.IDLE -> {
                when (next) {
                    LegacyTransitTimeRollupStatus.DRAINING, LegacyTransitTimeRollupStatus.STOPPED -> return true
                }
                return false
            }
            LegacyTransitTimeRollupStatus.WARMING -> {
                when (next) {
                    LegacyTransitTimeRollupStatus.READY, LegacyTransitTimeRollupStatus.STOPPED -> return true
                }
                return false
            }
            LegacyTransitTimeRollupStatus.READY -> {
                when (next) {
                    LegacyTransitTimeRollupStatus.DRAINING, LegacyTransitTimeRollupStatus.STOPPED -> return true
                }
                return false
            }
            LegacyTransitTimeRollupStatus.DRAINING -> return next == LegacyTransitTimeRollupStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: LegacyTransitTimeRollupStatus) {
        if (!canMoveTo(next)) {
            error("legacy transit time rollup $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedValue(): Double = round((sequence.toDouble() - startsAt.toDouble()) * 100) / 100

    fun snapshot(): LegacyTransitTimeRollup {
        return LegacyTransitTimeRollup(
            code = code,
            label = label,
            minimum = minimum,
            sequence = sequence,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy transit time rollup in status idle. */
fun newLegacyTransitTimeRollup(code: String, label: String): LegacyTransitTimeRollup {
    return LegacyTransitTimeRollup(
        code = code,
        label = label,
        minimum = 0.0,
        sequence = 0,
        startsAt = 0,
        status = LegacyTransitTimeRollupStatus.IDLE,
    )
}
