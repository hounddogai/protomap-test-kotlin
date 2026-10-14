package com.acme.transport.terminal.sortation.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual sortation rollup records of terminal's sortation work, and the rules, stores, and calculations
// around them.

/** Where a manual sortation rollup is in its life cycle. */
enum class ManualSortationRollupStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A manual sortation rollup as the sortation team keeps it. */
data class ManualSortationRollup(
    var code: String,
    var name: String,
    var lane: String,
    var score: Double,
    var attempts: Int,
    var budget: Double,
    var odometer: Int,
    var visible: Boolean,
    var depot: String,
    var status: ManualSortationRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualSortationRollupStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualSortationRollupStatus): Boolean {
        when (status) {
            ManualSortationRollupStatus.CREATED -> return next == ManualSortationRollupStatus.VALIDATED || next == ManualSortationRollupStatus.PUBLISHED
            ManualSortationRollupStatus.VALIDATED -> return next == ManualSortationRollupStatus.PUBLISHED
            ManualSortationRollupStatus.PUBLISHED -> {
                when (next) {
                    ManualSortationRollupStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualSortationRollupStatus) {
        if (!canMoveTo(next)) {
            error("manual sortation rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun netVolume(): Double = round((odometer.toDouble() + budget) * 100) / 100

    fun snapshot(): ManualSortationRollup {
        return ManualSortationRollup(
            code = code,
            name = name,
            lane = lane,
            score = score,
            attempts = attempts,
            budget = budget,
            odometer = odometer,
            visible = visible,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a manual sortation rollup in status created. */
fun newManualSortationRollup(code: String, name: String): ManualSortationRollup {
    return ManualSortationRollup(
        code = code,
        name = name,
        lane = "",
        score = 0.0,
        attempts = 0,
        budget = 0.0,
        odometer = 0,
        visible = false,
        depot = "",
        status = ManualSortationRollupStatus.CREATED,
    )
}
