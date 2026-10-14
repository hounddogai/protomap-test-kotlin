package com.acme.transport.safety.nearmiss.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered near miss adjustment is in its life cycle. */
enum class TieredNearMissAdjustmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A tiered near miss adjustment as the near miss team keeps it. */
data class TieredNearMissAdjustment(
    var code: String,
    var name: String,
    var attempts: Int,
    var category: String,
    var summary: String,
    var distanceKm: Double,
    var ceiling: Double,
    var status: TieredNearMissAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredNearMissAdjustmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredNearMissAdjustmentStatus): Boolean {
        when (status) {
            TieredNearMissAdjustmentStatus.PROPOSED -> return next == TieredNearMissAdjustmentStatus.APPROVED || next == TieredNearMissAdjustmentStatus.REJECTED
            TieredNearMissAdjustmentStatus.APPROVED -> return next == TieredNearMissAdjustmentStatus.REJECTED || next == TieredNearMissAdjustmentStatus.WITHDRAWN
            TieredNearMissAdjustmentStatus.REJECTED -> {
                when (next) {
                    TieredNearMissAdjustmentStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredNearMissAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("tiered near miss adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tiered near miss adjustment"} $code"

    fun projectedVolume(): Double = round(ceiling * attempts.toDouble() * 100) / 100

    fun snapshot(): TieredNearMissAdjustment {
        return TieredNearMissAdjustment(
            code = code,
            name = name,
            attempts = attempts,
            category = category,
            summary = summary,
            distanceKm = distanceKm,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a tiered near miss adjustment in status proposed. */
fun newTieredNearMissAdjustment(code: String, name: String): TieredNearMissAdjustment {
    return TieredNearMissAdjustment(
        code = code,
        name = name,
        attempts = 0,
        category = "",
        summary = "",
        distanceKm = 0.0,
        ceiling = 0.0,
        status = TieredNearMissAdjustmentStatus.PROPOSED,
    )
}
