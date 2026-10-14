package com.acme.transport.freightpay.ratecard.cardbundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient rate card forecast is in its life cycle. */
enum class LenientRateCardForecastStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A lenient rate card forecast. Its code is unique within a tenant. */
data class LenientRateCardForecast(
    var code: String,
    var title: String,
    var dock: String,
    var unit: String,
    var approved: Boolean,
    var country: String,
    var weight: Double,
    var version: Int,
    var rate: Double,
    var ratio: Double,
    var status: LenientRateCardForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientRateCardForecastStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientRateCardForecastStatus): Boolean {
        when (status) {
            LenientRateCardForecastStatus.CREATED -> return next == LenientRateCardForecastStatus.PUBLISHED
            LenientRateCardForecastStatus.VALIDATED -> return next == LenientRateCardForecastStatus.PUBLISHED || next == LenientRateCardForecastStatus.RETIRED
            LenientRateCardForecastStatus.PUBLISHED -> return next == LenientRateCardForecastStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LenientRateCardForecastStatus) {
        if (!canMoveTo(next)) {
            error("lenient rate card forecast $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $title"

    fun projectedTotal(): Double = round((weight + version.toDouble()) * 100) / 100

    fun snapshot(): LenientRateCardForecast {
        return LenientRateCardForecast(
            code = code,
            title = title,
            dock = dock,
            unit = unit,
            approved = approved,
            country = country,
            weight = weight,
            version = version,
            rate = rate,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a lenient rate card forecast in status created. */
fun newLenientRateCardForecast(code: String, title: String): LenientRateCardForecast {
    return LenientRateCardForecast(
        code = code,
        title = title,
        dock = "",
        unit = "",
        approved = false,
        country = "",
        weight = 0.0,
        version = 0,
        rate = 0.0,
        ratio = 0.0,
        status = LenientRateCardForecastStatus.CREATED,
    )
}
