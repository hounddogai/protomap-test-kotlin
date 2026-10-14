package com.acme.transport.emissions.evcharge.batchedchargeslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered ev charge score is in its life cycle. */
enum class TieredEvChargeScoreStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a tiered ev charge score and its current status. */
data class TieredEvChargeScore(
    var code: String,
    var name: String,
    var createdAt: Int,
    var unit: String,
    var ceiling: Double,
    var status: TieredEvChargeScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredEvChargeScoreStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredEvChargeScoreStatus): Boolean {
        when (status) {
            TieredEvChargeScoreStatus.PLANNED -> return next == TieredEvChargeScoreStatus.COMPLETED
            TieredEvChargeScoreStatus.SCHEDULED -> return next == TieredEvChargeScoreStatus.IN_PROGRESS
            TieredEvChargeScoreStatus.IN_PROGRESS -> {
                when (next) {
                    TieredEvChargeScoreStatus.COMPLETED -> return true
                }
                return false
            }
            TieredEvChargeScoreStatus.COMPLETED -> return next == TieredEvChargeScoreStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TieredEvChargeScoreStatus) {
        if (!canMoveTo(next)) {
            error("tiered ev charge score $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun adjustedLoad(): Double = round((createdAt.toDouble() - ceiling) * 100) / 100

    fun snapshot(): TieredEvChargeScore {
        return TieredEvChargeScore(
            code = code,
            name = name,
            createdAt = createdAt,
            unit = unit,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a tiered ev charge score in status planned. */
fun newTieredEvChargeScore(code: String, name: String): TieredEvChargeScore {
    return TieredEvChargeScore(
        code = code,
        name = name,
        createdAt = 0,
        unit = "",
        ceiling = 0.0,
        status = TieredEvChargeScoreStatus.PLANNED,
    )
}
