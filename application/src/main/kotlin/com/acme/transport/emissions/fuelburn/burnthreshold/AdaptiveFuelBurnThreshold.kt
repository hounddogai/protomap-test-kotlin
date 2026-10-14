package com.acme.transport.emissions.fuelburn.burnthreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive fuel burn threshold records of emissions's fuel burn work, and the rules, stores, and
// calculations around them.

/** Where a adaptive fuel burn threshold is in its life cycle. */
enum class AdaptiveFuelBurnThresholdStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A adaptive fuel burn threshold as the fuel burn team keeps it. */
data class AdaptiveFuelBurnThreshold(
    var code: String,
    var label: String,
    var source: String,
    var rate: Double,
    var depot: String,
    var carrier: String,
    var cost: Double,
    var title: String,
    var priority: Int,
    var batchSize: Int,
    var contactEmail: String,
    var status: AdaptiveFuelBurnThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveFuelBurnThresholdStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveFuelBurnThresholdStatus): Boolean {
        when (status) {
            AdaptiveFuelBurnThresholdStatus.PROPOSED -> {
                when (next) {
                    AdaptiveFuelBurnThresholdStatus.APPROVED, AdaptiveFuelBurnThresholdStatus.REJECTED -> return true
                }
                return false
            }
            AdaptiveFuelBurnThresholdStatus.APPROVED -> return next == AdaptiveFuelBurnThresholdStatus.REJECTED
            AdaptiveFuelBurnThresholdStatus.REJECTED -> {
                when (next) {
                    AdaptiveFuelBurnThresholdStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveFuelBurnThresholdStatus) {
        if (!canMoveTo(next)) {
            error("adaptive fuel burn threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun netLoad(): Double = round((priority.toDouble() - batchSize.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveFuelBurnThreshold {
        return AdaptiveFuelBurnThreshold(
            code = code,
            label = label,
            source = source,
            rate = rate,
            depot = depot,
            carrier = carrier,
            cost = cost,
            title = title,
            priority = priority,
            batchSize = batchSize,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a adaptive fuel burn threshold in status proposed. */
fun newAdaptiveFuelBurnThreshold(code: String, label: String): AdaptiveFuelBurnThreshold {
    return AdaptiveFuelBurnThreshold(
        code = code,
        label = label,
        source = "",
        rate = 0.0,
        depot = "",
        carrier = "",
        cost = 0.0,
        title = "",
        priority = 0,
        batchSize = 0,
        contactEmail = "",
        status = AdaptiveFuelBurnThresholdStatus.PROPOSED,
    )
}
