package com.acme.transport.lastmile.drop.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback drop tier records of lastmile's drop work, and the rules, stores, and calculations around
// them.

/** Where a fallback drop tier is in its life cycle. */
enum class FallbackDropTierStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A fallback drop tier as the drop team keeps it. */
data class FallbackDropTier(
    var code: String,
    var label: String,
    var revision: Int,
    var ceiling: Double,
    var source: String,
    var region: String,
    var odometer: Int,
    var trailerId: String,
    var channel: String,
    var currency: String,
    var maximum: Double,
    var status: FallbackDropTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDropTierStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDropTierStatus): Boolean {
        when (status) {
            FallbackDropTierStatus.CREATED -> return next == FallbackDropTierStatus.PUBLISHED
            FallbackDropTierStatus.VALIDATED -> return next == FallbackDropTierStatus.PUBLISHED || next == FallbackDropTierStatus.RETIRED
            FallbackDropTierStatus.PUBLISHED -> {
                when (next) {
                    FallbackDropTierStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackDropTierStatus) {
        if (!canMoveTo(next)) {
            error("fallback drop tier $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveExposure(): Double = round(revision.toDouble() * odometer.toDouble() * 100) / 100

    fun snapshot(): FallbackDropTier {
        return FallbackDropTier(
            code = code,
            label = label,
            revision = revision,
            ceiling = ceiling,
            source = source,
            region = region,
            odometer = odometer,
            trailerId = trailerId,
            channel = channel,
            currency = currency,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a fallback drop tier in status created. */
fun newFallbackDropTier(code: String, label: String): FallbackDropTier {
    return FallbackDropTier(
        code = code,
        label = label,
        revision = 0,
        ceiling = 0.0,
        source = "",
        region = "",
        odometer = 0,
        trailerId = "",
        channel = "",
        currency = "",
        maximum = 0.0,
        status = FallbackDropTierStatus.CREATED,
    )
}
