package com.acme.transport.linehaul.relay.relaythreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a relay forecast is in its life cycle. */
enum class RelayForecastStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A relay forecast. Its code is unique within a tenant. */
data class RelayForecast(
    var code: String,
    var label: String,
    var timezone: String,
    var distanceKm: Double,
    var pinned: Boolean,
    var lane: String,
    var segment: String,
    var amount: Double,
    var driverId: String,
    var createdAt: Int,
    var retries: Int,
    var status: RelayForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayForecastStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayForecastStatus): Boolean {
        when (status) {
            RelayForecastStatus.CREATED -> {
                when (next) {
                    RelayForecastStatus.VALIDATED -> return true
                }
                return false
            }
            RelayForecastStatus.VALIDATED -> return next == RelayForecastStatus.RETIRED
            RelayForecastStatus.PUBLISHED -> return next == RelayForecastStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RelayForecastStatus) {
        if (!canMoveTo(next)) {
            error("relay forecast $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedLoad(): Double = round((retries.toDouble() - amount) * 100) / 100

    fun snapshot(): RelayForecast {
        return RelayForecast(
            code = code,
            label = label,
            timezone = timezone,
            distanceKm = distanceKm,
            pinned = pinned,
            lane = lane,
            segment = segment,
            amount = amount,
            driverId = driverId,
            createdAt = createdAt,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a relay forecast in status created. */
fun newRelayForecast(code: String, label: String): RelayForecast {
    return RelayForecast(
        code = code,
        label = label,
        timezone = "",
        distanceKm = 0.0,
        pinned = false,
        lane = "",
        segment = "",
        amount = 0.0,
        driverId = "",
        createdAt = 0,
        retries = 0,
        status = RelayForecastStatus.CREATED,
    )
}
