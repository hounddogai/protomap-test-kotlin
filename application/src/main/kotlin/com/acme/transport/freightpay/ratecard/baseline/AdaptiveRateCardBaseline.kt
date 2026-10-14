package com.acme.transport.freightpay.ratecard.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive rate card baseline records of freightpay's rate card work, and the rules, stores, and
// calculations around them.

/** Where a adaptive rate card baseline is in its life cycle. */
enum class AdaptiveRateCardBaselineStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A adaptive rate card baseline as the rate card team keeps it. */
data class AdaptiveRateCardBaseline(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var channel: String,
    var ceiling: Double,
    var vehicleId: String,
    var label: String,
    var status: AdaptiveRateCardBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveRateCardBaselineStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveRateCardBaselineStatus): Boolean {
        when (status) {
            AdaptiveRateCardBaselineStatus.OPEN -> return next == AdaptiveRateCardBaselineStatus.IN_REVIEW || next == AdaptiveRateCardBaselineStatus.RESOLVED
            AdaptiveRateCardBaselineStatus.IN_REVIEW -> {
                when (next) {
                    AdaptiveRateCardBaselineStatus.RESOLVED, AdaptiveRateCardBaselineStatus.ARCHIVED -> return true
                }
                return false
            }
            AdaptiveRateCardBaselineStatus.RESOLVED -> {
                when (next) {
                    AdaptiveRateCardBaselineStatus.REOPENED, AdaptiveRateCardBaselineStatus.ARCHIVED -> return true
                }
                return false
            }
            AdaptiveRateCardBaselineStatus.REOPENED -> {
                when (next) {
                    AdaptiveRateCardBaselineStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveRateCardBaselineStatus) {
        if (!canMoveTo(next)) {
            error("adaptive rate card baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$name]"

    fun weightedVolume(): Double = round((fuelLiters - ceiling) * 100) / 100

    fun snapshot(): AdaptiveRateCardBaseline {
        return AdaptiveRateCardBaseline(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            channel = channel,
            ceiling = ceiling,
            vehicleId = vehicleId,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a adaptive rate card baseline in status open. */
fun newAdaptiveRateCardBaseline(code: String, name: String): AdaptiveRateCardBaseline {
    return AdaptiveRateCardBaseline(
        code = code,
        name = name,
        fuelLiters = 0.0,
        channel = "",
        ceiling = 0.0,
        vehicleId = "",
        label = "",
        status = AdaptiveRateCardBaselineStatus.OPEN,
    )
}
