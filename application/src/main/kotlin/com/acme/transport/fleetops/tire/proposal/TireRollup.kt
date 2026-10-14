package com.acme.transport.fleetops.tire.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tire rollup is in its life cycle. */
enum class TireRollupStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A tire rollup as the tire team keeps it. */
data class TireRollup(
    var code: String,
    var label: String,
    var distanceKm: Double,
    var ceiling: Double,
    var dock: String,
    var category: String,
    var segment: String,
    var status: TireRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireRollupStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TireRollupStatus): Boolean {
        when (status) {
            TireRollupStatus.PROPOSED -> return next == TireRollupStatus.APPROVED || next == TireRollupStatus.REJECTED
            TireRollupStatus.APPROVED -> {
                when (next) {
                    TireRollupStatus.WITHDRAWN -> return true
                }
                return false
            }
            TireRollupStatus.REJECTED -> {
                when (next) {
                    TireRollupStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TireRollupStatus) {
        if (!canMoveTo(next)) {
            error("tire rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedExposure(): Double = round((ceiling + distanceKm) * 100) / 100

    fun snapshot(): TireRollup {
        return TireRollup(
            code = code,
            label = label,
            distanceKm = distanceKm,
            ceiling = ceiling,
            dock = dock,
            category = category,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a tire rollup in status proposed. */
fun newTireRollup(code: String, label: String): TireRollup {
    return TireRollup(
        code = code,
        label = label,
        distanceKm = 0.0,
        ceiling = 0.0,
        dock = "",
        category = "",
        segment = "",
        status = TireRollupStatus.PROPOSED,
    )
}
