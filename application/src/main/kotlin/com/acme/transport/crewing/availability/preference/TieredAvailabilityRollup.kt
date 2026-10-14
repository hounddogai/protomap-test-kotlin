package com.acme.transport.crewing.availability.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered availability rollup is in its life cycle. */
enum class TieredAvailabilityRollupStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One tiered availability rollup, with what the planners need to act on it. */
data class TieredAvailabilityRollup(
    var code: String,
    var name: String,
    var version: Int,
    var margin: Double,
    var visible: Boolean,
    var segment: String,
    var retries: Int,
    var minimum: Double,
    var status: TieredAvailabilityRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredAvailabilityRollupStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredAvailabilityRollupStatus): Boolean {
        when (status) {
            TieredAvailabilityRollupStatus.OPEN -> return next == TieredAvailabilityRollupStatus.RESOLVED || next == TieredAvailabilityRollupStatus.REOPENED
            TieredAvailabilityRollupStatus.IN_REVIEW -> {
                when (next) {
                    TieredAvailabilityRollupStatus.REOPENED, TieredAvailabilityRollupStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredAvailabilityRollupStatus.RESOLVED -> return next == TieredAvailabilityRollupStatus.ARCHIVED
            TieredAvailabilityRollupStatus.REOPENED -> return next == TieredAvailabilityRollupStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: TieredAvailabilityRollupStatus) {
        if (!canMoveTo(next)) {
            error("tiered availability rollup $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$name]"

    fun weightedValue(): Double = round((minimum - margin) * 100) / 100

    fun snapshot(): TieredAvailabilityRollup {
        return TieredAvailabilityRollup(
            code = code,
            name = name,
            version = version,
            margin = margin,
            visible = visible,
            segment = segment,
            retries = retries,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a tiered availability rollup in status open. */
fun newTieredAvailabilityRollup(code: String, name: String): TieredAvailabilityRollup {
    return TieredAvailabilityRollup(
        code = code,
        name = name,
        version = 0,
        margin = 0.0,
        visible = false,
        segment = "",
        retries = 0,
        minimum = 0.0,
        status = TieredAvailabilityRollupStatus.OPEN,
    )
}
