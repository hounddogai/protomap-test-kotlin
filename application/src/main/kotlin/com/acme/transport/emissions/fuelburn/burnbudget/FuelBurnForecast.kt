package com.acme.transport.emissions.fuelburn.burnbudget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel burn forecast is in its life cycle. */
enum class FuelBurnForecastStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a fuel burn forecast and its current status. */
data class FuelBurnForecast(
    var code: String,
    var title: String,
    var archived: Boolean,
    var attributes: MutableMap<String, String>,
    var loadKg: Double,
    var currency: String,
    var startsAt: Int,
    var contactEmail: String,
    var status: FuelBurnForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnForecastStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnForecastStatus): Boolean {
        when (status) {
            FuelBurnForecastStatus.REQUESTED -> {
                when (next) {
                    FuelBurnForecastStatus.GRANTED, FuelBurnForecastStatus.EXPIRED -> return true
                }
                return false
            }
            FuelBurnForecastStatus.GRANTED -> {
                when (next) {
                    FuelBurnForecastStatus.REVOKED, FuelBurnForecastStatus.EXPIRED -> return true
                }
                return false
            }
            FuelBurnForecastStatus.REVOKED -> return next == FuelBurnForecastStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: FuelBurnForecastStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn forecast $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netVolume(): Double = round((loadKg + startsAt.toDouble()) * 100) / 100

    fun snapshot(): FuelBurnForecast {
        return FuelBurnForecast(
            code = code,
            title = title,
            archived = archived,
            attributes = attributes,
            loadKg = loadKg,
            currency = currency,
            startsAt = startsAt,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fuel burn forecast in status requested. */
fun newFuelBurnForecast(code: String, title: String): FuelBurnForecast {
    return FuelBurnForecast(
        code = code,
        title = title,
        archived = false,
        attributes = mutableMapOf<String, String>(),
        loadKg = 0.0,
        currency = "",
        startsAt = 0,
        contactEmail = "",
        status = FuelBurnForecastStatus.REQUESTED,
    )
}
