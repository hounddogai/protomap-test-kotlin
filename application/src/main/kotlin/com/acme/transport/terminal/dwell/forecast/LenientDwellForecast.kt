package com.acme.transport.terminal.dwell.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient dwell forecast records of terminal's dwell work, and the rules, stores, and calculations around
// them.

/** Where a lenient dwell forecast is in its life cycle. */
enum class LenientDwellForecastStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a lenient dwell forecast and its current status. */
data class LenientDwellForecast(
    var code: String,
    var name: String,
    var pallets: Int,
    var discount: Double,
    var createdAt: Int,
    var pinned: Boolean,
    var threshold: Double,
    var cost: Double,
    var expiresAt: Int,
    var carrier: String,
    var status: LenientDwellForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientDwellForecastStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientDwellForecastStatus): Boolean {
        when (status) {
            LenientDwellForecastStatus.CREATED -> return next == LenientDwellForecastStatus.VALIDATED || next == LenientDwellForecastStatus.RETIRED
            LenientDwellForecastStatus.VALIDATED -> {
                when (next) {
                    LenientDwellForecastStatus.PUBLISHED, LenientDwellForecastStatus.RETIRED -> return true
                }
                return false
            }
            LenientDwellForecastStatus.PUBLISHED -> return next == LenientDwellForecastStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LenientDwellForecastStatus) {
        if (!canMoveTo(next)) {
            error("lenient dwell forecast $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun effectiveVolume(): Double = round((cost + createdAt.toDouble()) * 100) / 100

    fun snapshot(): LenientDwellForecast {
        return LenientDwellForecast(
            code = code,
            name = name,
            pallets = pallets,
            discount = discount,
            createdAt = createdAt,
            pinned = pinned,
            threshold = threshold,
            cost = cost,
            expiresAt = expiresAt,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lenient dwell forecast in status created. */
fun newLenientDwellForecast(code: String, name: String): LenientDwellForecast {
    return LenientDwellForecast(
        code = code,
        name = name,
        pallets = 0,
        discount = 0.0,
        createdAt = 0,
        pinned = false,
        threshold = 0.0,
        cost = 0.0,
        expiresAt = 0,
        carrier = "",
        status = LenientDwellForecastStatus.CREATED,
    )
}
