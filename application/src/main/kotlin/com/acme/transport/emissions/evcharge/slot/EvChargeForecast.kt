package com.acme.transport.emissions.evcharge.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a ev charge forecast is in its life cycle. */
enum class EvChargeForecastStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A ev charge forecast. Its code is unique within a tenant. */
data class EvChargeForecast(
    var code: String,
    var title: String,
    var country: String,
    var expiresAt: Int,
    var reviewer: String,
    var distanceKm: Double,
    var timezone: String,
    var checksum: String,
    var amount: Double,
    var status: EvChargeForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeForecastStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeForecastStatus): Boolean {
        when (status) {
            EvChargeForecastStatus.PROPOSED -> {
                when (next) {
                    EvChargeForecastStatus.APPROVED, EvChargeForecastStatus.REJECTED -> return true
                }
                return false
            }
            EvChargeForecastStatus.APPROVED -> {
                when (next) {
                    EvChargeForecastStatus.WITHDRAWN -> return true
                }
                return false
            }
            EvChargeForecastStatus.REJECTED -> {
                when (next) {
                    EvChargeForecastStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EvChargeForecastStatus) {
        if (!canMoveTo(next)) {
            error("ev charge forecast $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun weightedExposure(): Double = round((expiresAt.toDouble() + amount) * 100) / 100

    fun snapshot(): EvChargeForecast {
        return EvChargeForecast(
            code = code,
            title = title,
            country = country,
            expiresAt = expiresAt,
            reviewer = reviewer,
            distanceKm = distanceKm,
            timezone = timezone,
            checksum = checksum,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a ev charge forecast in status proposed. */
fun newEvChargeForecast(code: String, title: String): EvChargeForecast {
    return EvChargeForecast(
        code = code,
        title = title,
        country = "",
        expiresAt = 0,
        reviewer = "",
        distanceKm = 0.0,
        timezone = "",
        checksum = "",
        amount = 0.0,
        status = EvChargeForecastStatus.PROPOSED,
    )
}
