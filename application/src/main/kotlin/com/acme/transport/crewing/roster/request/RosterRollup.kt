package com.acme.transport.crewing.roster.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a roster rollup is in its life cycle. */
enum class RosterRollupStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a roster rollup and its current status. */
data class RosterRollup(
    var code: String,
    var label: String,
    var quantity: Int,
    var depot: String,
    var attempts: Int,
    var weight: Double,
    var floorValue: Double,
    var ceiling: Double,
    var region: String,
    var retries: Int,
    var channel: String,
    var status: RosterRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterRollupStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterRollupStatus): Boolean {
        when (status) {
            RosterRollupStatus.CREATED -> {
                when (next) {
                    RosterRollupStatus.VALIDATED -> return true
                }
                return false
            }
            RosterRollupStatus.VALIDATED -> return next == RosterRollupStatus.PUBLISHED || next == RosterRollupStatus.RETIRED
            RosterRollupStatus.PUBLISHED -> return next == RosterRollupStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RosterRollupStatus) {
        if (!canMoveTo(next)) {
            error("roster rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedLoad(): Double = round((attempts.toDouble() - floorValue) * 100) / 100

    fun snapshot(): RosterRollup {
        return RosterRollup(
            code = code,
            label = label,
            quantity = quantity,
            depot = depot,
            attempts = attempts,
            weight = weight,
            floorValue = floorValue,
            ceiling = ceiling,
            region = region,
            retries = retries,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a roster rollup in status created. */
fun newRosterRollup(code: String, label: String): RosterRollup {
    return RosterRollup(
        code = code,
        label = label,
        quantity = 0,
        depot = "",
        attempts = 0,
        weight = 0.0,
        floorValue = 0.0,
        ceiling = 0.0,
        region = "",
        retries = 0,
        channel = "",
        status = RosterRollupStatus.CREATED,
    )
}
