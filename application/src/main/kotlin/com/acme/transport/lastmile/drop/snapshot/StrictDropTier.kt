package com.acme.transport.lastmile.drop.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict drop tier is in its life cycle. */
enum class StrictDropTierStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a strict drop tier and its current status. */
data class StrictDropTier(
    var code: String,
    var title: String,
    var unit: String,
    var driverId: String,
    var cost: Double,
    var minimum: Double,
    var name: String,
    var quantity: Int,
    var status: StrictDropTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDropTierStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDropTierStatus): Boolean {
        when (status) {
            StrictDropTierStatus.PROPOSED -> return next == StrictDropTierStatus.APPROVED
            StrictDropTierStatus.APPROVED -> return next == StrictDropTierStatus.WITHDRAWN
            StrictDropTierStatus.REJECTED -> {
                when (next) {
                    StrictDropTierStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictDropTierStatus) {
        if (!canMoveTo(next)) {
            error("strict drop tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"strict drop tier"} $code"

    fun weightedLoad(): Double = round((quantity.toDouble() - cost) * 100) / 100

    fun snapshot(): StrictDropTier {
        return StrictDropTier(
            code = code,
            title = title,
            unit = unit,
            driverId = driverId,
            cost = cost,
            minimum = minimum,
            name = name,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a strict drop tier in status proposed. */
fun newStrictDropTier(code: String, title: String): StrictDropTier {
    return StrictDropTier(
        code = code,
        title = title,
        unit = "",
        driverId = "",
        cost = 0.0,
        minimum = 0.0,
        name = "",
        quantity = 0,
        status = StrictDropTierStatus.PROPOSED,
    )
}
