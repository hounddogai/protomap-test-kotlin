package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered proof of delivery rollup records of lastmile's proof of delivery work, and the rules, stores,
// and calculations around them.

/** Where a tiered proof of delivery rollup is in its life cycle. */
enum class TieredProofOfDeliveryRollupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A tiered proof of delivery rollup as the proof of delivery team keeps it. */
data class TieredProofOfDeliveryRollup(
    var code: String,
    var name: String,
    var attempts: Int,
    var createdAt: Int,
    var driverId: String,
    var floorValue: Double,
    var region: String,
    var reviewer: String,
    var status: TieredProofOfDeliveryRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredProofOfDeliveryRollupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredProofOfDeliveryRollupStatus): Boolean {
        when (status) {
            TieredProofOfDeliveryRollupStatus.PLANNED -> return next == TieredProofOfDeliveryRollupStatus.IN_PROGRESS
            TieredProofOfDeliveryRollupStatus.SCHEDULED -> return next == TieredProofOfDeliveryRollupStatus.IN_PROGRESS || next == TieredProofOfDeliveryRollupStatus.CANCELLED
            TieredProofOfDeliveryRollupStatus.IN_PROGRESS -> return next == TieredProofOfDeliveryRollupStatus.COMPLETED || next == TieredProofOfDeliveryRollupStatus.CANCELLED
            TieredProofOfDeliveryRollupStatus.COMPLETED -> return next == TieredProofOfDeliveryRollupStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TieredProofOfDeliveryRollupStatus) {
        if (!canMoveTo(next)) {
            error("tiered proof of delivery rollup $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun projectedVolume(): Double = round(attempts.toDouble() * floorValue * 100) / 100

    fun snapshot(): TieredProofOfDeliveryRollup {
        return TieredProofOfDeliveryRollup(
            code = code,
            name = name,
            attempts = attempts,
            createdAt = createdAt,
            driverId = driverId,
            floorValue = floorValue,
            region = region,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a tiered proof of delivery rollup in status planned. */
fun newTieredProofOfDeliveryRollup(code: String, name: String): TieredProofOfDeliveryRollup {
    return TieredProofOfDeliveryRollup(
        code = code,
        name = name,
        attempts = 0,
        createdAt = 0,
        driverId = "",
        floorValue = 0.0,
        region = "",
        reviewer = "",
        status = TieredProofOfDeliveryRollupStatus.PLANNED,
    )
}
