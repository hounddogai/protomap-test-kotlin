package com.acme.transport.network.cutoff.forecast

import com.acme.transport.kit.text.normalize

// The default cutoff forecast records of network's cutoff work, and the rules, stores, and calculations
// around them.

/** Where a default cutoff forecast is in its life cycle. */
enum class DefaultCutoffForecastStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A default cutoff forecast as the cutoff team keeps it. */
data class DefaultCutoffForecast(
    var code: String,
    var label: String,
    var phone: String,
    var title: String,
    var category: String,
    var shiftCode: String,
    var contactEmail: String,
    var quantity: Int,
    var status: DefaultCutoffForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultCutoffForecastStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultCutoffForecastStatus): Boolean {
        when (status) {
            DefaultCutoffForecastStatus.CREATED -> return next == DefaultCutoffForecastStatus.RETIRED
            DefaultCutoffForecastStatus.VALIDATED -> return next == DefaultCutoffForecastStatus.PUBLISHED
            DefaultCutoffForecastStatus.PUBLISHED -> {
                when (next) {
                    DefaultCutoffForecastStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultCutoffForecastStatus) {
        if (!canMoveTo(next)) {
            error("default cutoff forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"default cutoff forecast"} $code"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): DefaultCutoffForecast {
        return DefaultCutoffForecast(
            code = code,
            label = label,
            phone = phone,
            title = title,
            category = category,
            shiftCode = shiftCode,
            contactEmail = contactEmail,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a default cutoff forecast in status created. */
fun newDefaultCutoffForecast(code: String, label: String): DefaultCutoffForecast {
    return DefaultCutoffForecast(
        code = code,
        label = label,
        phone = "",
        title = "",
        category = "",
        shiftCode = "",
        contactEmail = "",
        quantity = 0,
        status = DefaultCutoffForecastStatus.CREATED,
    )
}
