package com.acme.transport.emissions.evcharge.batchedchargeslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback ev charge slot records of emissions's ev charge work, and the rules, stores, and calculations
// around them.

/** Where a fallback ev charge slot is in its life cycle. */
enum class FallbackEvChargeSlotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a fallback ev charge slot and its current status. */
data class FallbackEvChargeSlot(
    var code: String,
    var label: String,
    var channel: String,
    var depot: String,
    var score: Double,
    var ownerId: String,
    var segment: String,
    var attempts: Int,
    var durationMinutes: Int,
    var status: FallbackEvChargeSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackEvChargeSlotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackEvChargeSlotStatus): Boolean {
        when (status) {
            FallbackEvChargeSlotStatus.CREATED -> {
                when (next) {
                    FallbackEvChargeSlotStatus.PUBLISHED, FallbackEvChargeSlotStatus.RETIRED -> return true
                }
                return false
            }
            FallbackEvChargeSlotStatus.VALIDATED -> {
                when (next) {
                    FallbackEvChargeSlotStatus.PUBLISHED, FallbackEvChargeSlotStatus.RETIRED -> return true
                }
                return false
            }
            FallbackEvChargeSlotStatus.PUBLISHED -> return next == FallbackEvChargeSlotStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: FallbackEvChargeSlotStatus) {
        if (!canMoveTo(next)) {
            error("fallback ev charge slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedExposure(): Double = round((durationMinutes.toDouble() - score) * 100) / 100

    fun snapshot(): FallbackEvChargeSlot {
        return FallbackEvChargeSlot(
            code = code,
            label = label,
            channel = channel,
            depot = depot,
            score = score,
            ownerId = ownerId,
            segment = segment,
            attempts = attempts,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a fallback ev charge slot in status created. */
fun newFallbackEvChargeSlot(code: String, label: String): FallbackEvChargeSlot {
    return FallbackEvChargeSlot(
        code = code,
        label = label,
        channel = "",
        depot = "",
        score = 0.0,
        ownerId = "",
        segment = "",
        attempts = 0,
        durationMinutes = 0,
        status = FallbackEvChargeSlotStatus.CREATED,
    )
}
