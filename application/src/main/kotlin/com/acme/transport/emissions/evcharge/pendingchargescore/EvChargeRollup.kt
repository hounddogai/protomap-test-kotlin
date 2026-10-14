package com.acme.transport.emissions.evcharge.pendingchargescore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a ev charge rollup is in its life cycle. */
enum class EvChargeRollupStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a ev charge rollup and its current status. */
data class EvChargeRollup(
    var code: String,
    var name: String,
    var cost: Double,
    var endsAt: Int,
    var minimum: Double,
    var status: EvChargeRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeRollupStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeRollupStatus): Boolean {
        when (status) {
            EvChargeRollupStatus.OPEN -> {
                when (next) {
                    EvChargeRollupStatus.ARCHIVED -> return true
                }
                return false
            }
            EvChargeRollupStatus.IN_REVIEW -> return next == EvChargeRollupStatus.RESOLVED
            EvChargeRollupStatus.RESOLVED -> {
                when (next) {
                    EvChargeRollupStatus.ARCHIVED -> return true
                }
                return false
            }
            EvChargeRollupStatus.REOPENED -> return next == EvChargeRollupStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: EvChargeRollupStatus) {
        if (!canMoveTo(next)) {
            error("ev charge rollup $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun projectedLoad(): Double = round((endsAt.toDouble() + minimum) * 100) / 100

    fun snapshot(): EvChargeRollup {
        return EvChargeRollup(
            code = code,
            name = name,
            cost = cost,
            endsAt = endsAt,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a ev charge rollup in status open. */
fun newEvChargeRollup(code: String, name: String): EvChargeRollup {
    return EvChargeRollup(
        code = code,
        name = name,
        cost = 0.0,
        endsAt = 0,
        minimum = 0.0,
        status = EvChargeRollupStatus.OPEN,
    )
}
