package com.acme.transport.network.zone.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The zone forecast records of network's zone work, and the rules, stores, and calculations around them.

/** Where a zone forecast is in its life cycle. */
enum class ZoneForecastStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A zone forecast as the zone team keeps it. */
data class ZoneForecast(
    var code: String,
    var label: String,
    var reviewer: String,
    var pallets: Int,
    var expiresAt: Int,
    var ratio: Double,
    var depot: String,
    var ceiling: Double,
    var carrier: String,
    var quantity: Int,
    var status: ZoneForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneForecastStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneForecastStatus): Boolean {
        when (status) {
            ZoneForecastStatus.REQUESTED -> return next == ZoneForecastStatus.REVOKED
            ZoneForecastStatus.GRANTED -> {
                when (next) {
                    ZoneForecastStatus.REVOKED, ZoneForecastStatus.EXPIRED -> return true
                }
                return false
            }
            ZoneForecastStatus.REVOKED -> {
                when (next) {
                    ZoneForecastStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ZoneForecastStatus) {
        if (!canMoveTo(next)) {
            error("zone forecast $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedVolume(): Double = round((pallets.toDouble() + ceiling) * 100) / 100

    fun snapshot(): ZoneForecast {
        return ZoneForecast(
            code = code,
            label = label,
            reviewer = reviewer,
            pallets = pallets,
            expiresAt = expiresAt,
            ratio = ratio,
            depot = depot,
            ceiling = ceiling,
            carrier = carrier,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a zone forecast in status requested. */
fun newZoneForecast(code: String, label: String): ZoneForecast {
    return ZoneForecast(
        code = code,
        label = label,
        reviewer = "",
        pallets = 0,
        expiresAt = 0,
        ratio = 0.0,
        depot = "",
        ceiling = 0.0,
        carrier = "",
        quantity = 0,
        status = ZoneForecastStatus.REQUESTED,
    )
}
