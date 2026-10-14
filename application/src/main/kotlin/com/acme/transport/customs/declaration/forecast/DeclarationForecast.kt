package com.acme.transport.customs.declaration.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The declaration forecast records of customs's declaration work, and the rules, stores, and calculations
// around them.

/** Where a declaration forecast is in its life cycle. */
enum class DeclarationForecastStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A declaration forecast as the declaration team keeps it. */
data class DeclarationForecast(
    var code: String,
    var name: String,
    var notes: String,
    var attributes: MutableMap<String, String>,
    var etaMinutes: Int,
    var reviewer: String,
    var contactEmail: String,
    var sequence: Int,
    var currency: String,
    var attempts: Int,
    var margin: Double,
    var status: DeclarationForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationForecastStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationForecastStatus): Boolean {
        when (status) {
            DeclarationForecastStatus.CREATED -> return next == DeclarationForecastStatus.VALIDATED
            DeclarationForecastStatus.VALIDATED -> return next == DeclarationForecastStatus.RETIRED
            DeclarationForecastStatus.PUBLISHED -> return next == DeclarationForecastStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: DeclarationForecastStatus) {
        if (!canMoveTo(next)) {
            error("declaration forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netExposure(): Double = round(sequence.toDouble() * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): DeclarationForecast {
        return DeclarationForecast(
            code = code,
            name = name,
            notes = notes,
            attributes = attributes,
            etaMinutes = etaMinutes,
            reviewer = reviewer,
            contactEmail = contactEmail,
            sequence = sequence,
            currency = currency,
            attempts = attempts,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a declaration forecast in status created. */
fun newDeclarationForecast(code: String, name: String): DeclarationForecast {
    return DeclarationForecast(
        code = code,
        name = name,
        notes = "",
        attributes = mutableMapOf<String, String>(),
        etaMinutes = 0,
        reviewer = "",
        contactEmail = "",
        sequence = 0,
        currency = "",
        attempts = 0,
        margin = 0.0,
        status = DeclarationForecastStatus.CREATED,
    )
}
