package com.acme.transport.emissions.evcharge.forecast

import com.acme.transport.kit.text.normalize

// The seasonal ev charge forecast records of emissions's ev charge work, and the rules, stores, and
// calculations around them.

/** Where a seasonal ev charge forecast is in its life cycle. */
enum class SeasonalEvChargeForecastStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A seasonal ev charge forecast as the ev charge team keeps it. */
data class SeasonalEvChargeForecast(
    var code: String,
    var label: String,
    var name: String,
    var ceiling: Double,
    var externalRef: String,
    var tags: MutableList<String>,
    var checksum: String,
    var status: SeasonalEvChargeForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalEvChargeForecastStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalEvChargeForecastStatus): Boolean {
        when (status) {
            SeasonalEvChargeForecastStatus.CREATED -> return next == SeasonalEvChargeForecastStatus.VALIDATED || next == SeasonalEvChargeForecastStatus.PUBLISHED
            SeasonalEvChargeForecastStatus.VALIDATED -> return next == SeasonalEvChargeForecastStatus.PUBLISHED
            SeasonalEvChargeForecastStatus.PUBLISHED -> return next == SeasonalEvChargeForecastStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: SeasonalEvChargeForecastStatus) {
        if (!canMoveTo(next)) {
            error("seasonal ev charge forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = ceiling

    fun snapshot(): SeasonalEvChargeForecast {
        return SeasonalEvChargeForecast(
            code = code,
            label = label,
            name = name,
            ceiling = ceiling,
            externalRef = externalRef,
            tags = tags,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal ev charge forecast in status created. */
fun newSeasonalEvChargeForecast(code: String, label: String): SeasonalEvChargeForecast {
    return SeasonalEvChargeForecast(
        code = code,
        label = label,
        name = "",
        ceiling = 0.0,
        externalRef = "",
        tags = mutableListOf<String>(),
        checksum = "",
        status = SeasonalEvChargeForecastStatus.CREATED,
    )
}
