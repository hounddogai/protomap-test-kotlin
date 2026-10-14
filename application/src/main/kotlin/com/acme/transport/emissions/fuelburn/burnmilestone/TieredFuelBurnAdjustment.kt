package com.acme.transport.emissions.fuelburn.burnmilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered fuel burn adjustment is in its life cycle. */
enum class TieredFuelBurnAdjustmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One tiered fuel burn adjustment, with what the planners need to act on it. */
data class TieredFuelBurnAdjustment(
    var code: String,
    var label: String,
    var attempts: Int,
    var updatedAt: Int,
    var pinned: Boolean,
    var floorValue: Double,
    var fuelLiters: Double,
    var contactEmail: String,
    var ratio: Double,
    var phone: String,
    var status: TieredFuelBurnAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredFuelBurnAdjustmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredFuelBurnAdjustmentStatus): Boolean {
        when (status) {
            TieredFuelBurnAdjustmentStatus.OPEN -> {
                when (next) {
                    TieredFuelBurnAdjustmentStatus.RESOLVED, TieredFuelBurnAdjustmentStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredFuelBurnAdjustmentStatus.IN_REVIEW -> {
                when (next) {
                    TieredFuelBurnAdjustmentStatus.REOPENED, TieredFuelBurnAdjustmentStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredFuelBurnAdjustmentStatus.RESOLVED -> return next == TieredFuelBurnAdjustmentStatus.REOPENED
            TieredFuelBurnAdjustmentStatus.REOPENED -> return next == TieredFuelBurnAdjustmentStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: TieredFuelBurnAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("tiered fuel burn adjustment $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"tiered fuel burn adjustment"} $code"

    fun projectedVolume(): Double = round(ratio * updatedAt.toDouble() * 100) / 100

    fun snapshot(): TieredFuelBurnAdjustment {
        return TieredFuelBurnAdjustment(
            code = code,
            label = label,
            attempts = attempts,
            updatedAt = updatedAt,
            pinned = pinned,
            floorValue = floorValue,
            fuelLiters = fuelLiters,
            contactEmail = contactEmail,
            ratio = ratio,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tiered fuel burn adjustment in status open. */
fun newTieredFuelBurnAdjustment(code: String, label: String): TieredFuelBurnAdjustment {
    return TieredFuelBurnAdjustment(
        code = code,
        label = label,
        attempts = 0,
        updatedAt = 0,
        pinned = false,
        floorValue = 0.0,
        fuelLiters = 0.0,
        contactEmail = "",
        ratio = 0.0,
        phone = "",
        status = TieredFuelBurnAdjustmentStatus.OPEN,
    )
}
