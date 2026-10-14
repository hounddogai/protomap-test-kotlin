package com.acme.transport.safety.violation.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict violation forecast records of safety's violation work, and the rules, stores, and calculations
// around them.

/** Where a strict violation forecast is in its life cycle. */
enum class StrictViolationForecastStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A strict violation forecast. Its code is unique within a tenant. */
data class StrictViolationForecast(
    var code: String,
    var title: String,
    var revision: Int,
    var retries: Int,
    var contactEmail: String,
    var etaMinutes: Int,
    var status: StrictViolationForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictViolationForecastStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictViolationForecastStatus): Boolean {
        when (status) {
            StrictViolationForecastStatus.DRAFT -> return next == StrictViolationForecastStatus.PENDING || next == StrictViolationForecastStatus.ACTIVE
            StrictViolationForecastStatus.PENDING -> return next == StrictViolationForecastStatus.ACTIVE || next == StrictViolationForecastStatus.CLOSED
            StrictViolationForecastStatus.ACTIVE -> {
                when (next) {
                    StrictViolationForecastStatus.CLOSED -> return true
                }
                return false
            }
            StrictViolationForecastStatus.SUSPENDED -> return next == StrictViolationForecastStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: StrictViolationForecastStatus) {
        if (!canMoveTo(next)) {
            error("strict violation forecast $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code: $title"

    fun effectiveValue(): Double = round(retries.toDouble() * revision.toDouble() * 100) / 100

    fun snapshot(): StrictViolationForecast {
        return StrictViolationForecast(
            code = code,
            title = title,
            revision = revision,
            retries = retries,
            contactEmail = contactEmail,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict violation forecast in status draft. */
fun newStrictViolationForecast(code: String, title: String): StrictViolationForecast {
    return StrictViolationForecast(
        code = code,
        title = title,
        revision = 0,
        retries = 0,
        contactEmail = "",
        etaMinutes = 0,
        status = StrictViolationForecastStatus.DRAFT,
    )
}
