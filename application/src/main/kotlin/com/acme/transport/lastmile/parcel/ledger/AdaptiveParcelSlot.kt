package com.acme.transport.lastmile.parcel.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive parcel slot is in its life cycle. */
enum class AdaptiveParcelSlotStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a adaptive parcel slot and its current status. */
data class AdaptiveParcelSlot(
    var code: String,
    var label: String,
    var distanceKm: Double,
    var unit: String,
    var batchSize: Int,
    var budget: Double,
    var expiresAt: Int,
    var status: AdaptiveParcelSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveParcelSlotStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveParcelSlotStatus): Boolean {
        when (status) {
            AdaptiveParcelSlotStatus.REQUESTED -> {
                when (next) {
                    AdaptiveParcelSlotStatus.GRANTED, AdaptiveParcelSlotStatus.REVOKED -> return true
                }
                return false
            }
            AdaptiveParcelSlotStatus.GRANTED -> {
                when (next) {
                    AdaptiveParcelSlotStatus.REVOKED, AdaptiveParcelSlotStatus.EXPIRED -> return true
                }
                return false
            }
            AdaptiveParcelSlotStatus.REVOKED -> return next == AdaptiveParcelSlotStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: AdaptiveParcelSlotStatus) {
        if (!canMoveTo(next)) {
            error("adaptive parcel slot $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun netExposure(): Double = round((expiresAt.toDouble() - budget) * 100) / 100

    fun snapshot(): AdaptiveParcelSlot {
        return AdaptiveParcelSlot(
            code = code,
            label = label,
            distanceKm = distanceKm,
            unit = unit,
            batchSize = batchSize,
            budget = budget,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a adaptive parcel slot in status requested. */
fun newAdaptiveParcelSlot(code: String, label: String): AdaptiveParcelSlot {
    return AdaptiveParcelSlot(
        code = code,
        label = label,
        distanceKm = 0.0,
        unit = "",
        batchSize = 0,
        budget = 0.0,
        expiresAt = 0,
        status = AdaptiveParcelSlotStatus.REQUESTED,
    )
}
