package com.acme.transport.crewing.restbreak.rollup

import com.acme.transport.kit.text.normalize

// The rest break rollup records of crewing's rest break work, and the rules, stores, and calculations around
// them.

/** Where a rest break rollup is in its life cycle. */
enum class RestBreakRollupStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A rest break rollup as the rest break team keeps it. */
data class RestBreakRollup(
    var code: String,
    var label: String,
    var amount: Double,
    var ownerId: String,
    var region: String,
    var status: RestBreakRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakRollupStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakRollupStatus): Boolean {
        when (status) {
            RestBreakRollupStatus.OPEN -> return next == RestBreakRollupStatus.IN_REVIEW || next == RestBreakRollupStatus.ARCHIVED
            RestBreakRollupStatus.IN_REVIEW -> {
                when (next) {
                    RestBreakRollupStatus.REOPENED, RestBreakRollupStatus.ARCHIVED -> return true
                }
                return false
            }
            RestBreakRollupStatus.RESOLVED -> {
                when (next) {
                    RestBreakRollupStatus.REOPENED, RestBreakRollupStatus.ARCHIVED -> return true
                }
                return false
            }
            RestBreakRollupStatus.REOPENED -> return next == RestBreakRollupStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RestBreakRollupStatus) {
        if (!canMoveTo(next)) {
            error("rest break rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = amount

    fun snapshot(): RestBreakRollup {
        return RestBreakRollup(
            code = code,
            label = label,
            amount = amount,
            ownerId = ownerId,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a rest break rollup in status open. */
fun newRestBreakRollup(code: String, label: String): RestBreakRollup {
    return RestBreakRollup(
        code = code,
        label = label,
        amount = 0.0,
        ownerId = "",
        region = "",
        status = RestBreakRollupStatus.OPEN,
    )
}
