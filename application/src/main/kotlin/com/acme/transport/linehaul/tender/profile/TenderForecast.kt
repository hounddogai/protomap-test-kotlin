package com.acme.transport.linehaul.tender.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tender forecast is in its life cycle. */
enum class TenderForecastStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A tender forecast. Its code is unique within a tenant. */
data class TenderForecast(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var phone: String,
    var loadKg: Double,
    var carrier: String,
    var currency: String,
    var batchSize: Int,
    var margin: Double,
    var category: String,
    var country: String,
    var status: TenderForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderForecastStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderForecastStatus): Boolean {
        when (status) {
            TenderForecastStatus.CREATED -> {
                when (next) {
                    TenderForecastStatus.VALIDATED, TenderForecastStatus.PUBLISHED -> return true
                }
                return false
            }
            TenderForecastStatus.VALIDATED -> return next == TenderForecastStatus.RETIRED
            TenderForecastStatus.PUBLISHED -> return next == TenderForecastStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: TenderForecastStatus) {
        if (!canMoveTo(next)) {
            error("tender forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveValue(): Double = round((loadKg - margin) * 100) / 100

    fun snapshot(): TenderForecast {
        return TenderForecast(
            code = code,
            title = title,
            attributes = attributes,
            phone = phone,
            loadKg = loadKg,
            carrier = carrier,
            currency = currency,
            batchSize = batchSize,
            margin = margin,
            category = category,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a tender forecast in status created. */
fun newTenderForecast(code: String, title: String): TenderForecast {
    return TenderForecast(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        phone = "",
        loadKg = 0.0,
        carrier = "",
        currency = "",
        batchSize = 0,
        margin = 0.0,
        category = "",
        country = "",
        status = TenderForecastStatus.CREATED,
    )
}
