package com.acme.transport.emissions.idling.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending idling forecast is in its life cycle. */
enum class PendingIdlingForecastStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One pending idling forecast, with what the planners need to act on it. */
data class PendingIdlingForecast(
    var code: String,
    var title: String,
    var score: Double,
    var loadKg: Double,
    var externalRef: String,
    var unit: String,
    var weight: Double,
    var archived: Boolean,
    var quantity: Int,
    var status: PendingIdlingForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingIdlingForecastStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingIdlingForecastStatus): Boolean {
        when (status) {
            PendingIdlingForecastStatus.DRAFT -> return next == PendingIdlingForecastStatus.SUSPENDED
            PendingIdlingForecastStatus.PENDING -> return next == PendingIdlingForecastStatus.ACTIVE || next == PendingIdlingForecastStatus.CLOSED
            PendingIdlingForecastStatus.ACTIVE -> return next == PendingIdlingForecastStatus.SUSPENDED || next == PendingIdlingForecastStatus.CLOSED
            PendingIdlingForecastStatus.SUSPENDED -> {
                when (next) {
                    PendingIdlingForecastStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingIdlingForecastStatus) {
        if (!canMoveTo(next)) {
            error("pending idling forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun effectiveTotal(): Double = round((quantity.toDouble() - score) * 100) / 100

    fun snapshot(): PendingIdlingForecast {
        return PendingIdlingForecast(
            code = code,
            title = title,
            score = score,
            loadKg = loadKg,
            externalRef = externalRef,
            unit = unit,
            weight = weight,
            archived = archived,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a pending idling forecast in status draft. */
fun newPendingIdlingForecast(code: String, title: String): PendingIdlingForecast {
    return PendingIdlingForecast(
        code = code,
        title = title,
        score = 0.0,
        loadKg = 0.0,
        externalRef = "",
        unit = "",
        weight = 0.0,
        archived = false,
        quantity = 0,
        status = PendingIdlingForecastStatus.DRAFT,
    )
}
