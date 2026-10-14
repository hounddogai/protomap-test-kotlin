package com.acme.transport.freightpay.ratecard.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rate card override is in its life cycle. */
enum class RateCardOverrideStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A rate card override as the rate card team keeps it. */
data class RateCardOverride(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var distanceKm: Double,
    var reviewer: String,
    var fuelLiters: Double,
    var odometer: Int,
    var shiftCode: String,
    var budget: Double,
    var stopCount: Int,
    var status: RateCardOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardOverrideStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardOverrideStatus): Boolean {
        when (status) {
            RateCardOverrideStatus.REQUESTED -> return next == RateCardOverrideStatus.EXPIRED
            RateCardOverrideStatus.GRANTED -> {
                when (next) {
                    RateCardOverrideStatus.EXPIRED -> return true
                }
                return false
            }
            RateCardOverrideStatus.REVOKED -> {
                when (next) {
                    RateCardOverrideStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RateCardOverrideStatus) {
        if (!canMoveTo(next)) {
            error("rate card override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedVolume(): Double = round((fuelLiters - budget) * 100) / 100

    fun snapshot(): RateCardOverride {
        return RateCardOverride(
            code = code,
            title = title,
            attributes = attributes,
            distanceKm = distanceKm,
            reviewer = reviewer,
            fuelLiters = fuelLiters,
            odometer = odometer,
            shiftCode = shiftCode,
            budget = budget,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rate card override in status requested. */
fun newRateCardOverride(code: String, title: String): RateCardOverride {
    return RateCardOverride(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        distanceKm = 0.0,
        reviewer = "",
        fuelLiters = 0.0,
        odometer = 0,
        shiftCode = "",
        budget = 0.0,
        stopCount = 0,
        status = RateCardOverrideStatus.REQUESTED,
    )
}
