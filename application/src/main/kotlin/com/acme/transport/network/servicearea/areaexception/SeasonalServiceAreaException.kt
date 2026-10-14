package com.acme.transport.network.servicearea.areaexception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal service area exception records of network's service area work, and the rules, stores, and
// calculations around them.

/** Where a seasonal service area exception is in its life cycle. */
enum class SeasonalServiceAreaExceptionStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One seasonal service area exception, with what the planners need to act on it. */
data class SeasonalServiceAreaException(
    var code: String,
    var name: String,
    var budget: Double,
    var attempts: Int,
    var etaMinutes: Int,
    var status: SeasonalServiceAreaExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalServiceAreaExceptionStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalServiceAreaExceptionStatus): Boolean {
        when (status) {
            SeasonalServiceAreaExceptionStatus.OPEN -> {
                when (next) {
                    SeasonalServiceAreaExceptionStatus.IN_REVIEW, SeasonalServiceAreaExceptionStatus.ARCHIVED -> return true
                }
                return false
            }
            SeasonalServiceAreaExceptionStatus.IN_REVIEW -> {
                when (next) {
                    SeasonalServiceAreaExceptionStatus.RESOLVED, SeasonalServiceAreaExceptionStatus.REOPENED -> return true
                }
                return false
            }
            SeasonalServiceAreaExceptionStatus.RESOLVED -> return next == SeasonalServiceAreaExceptionStatus.ARCHIVED
            SeasonalServiceAreaExceptionStatus.REOPENED -> {
                when (next) {
                    SeasonalServiceAreaExceptionStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalServiceAreaExceptionStatus) {
        if (!canMoveTo(next)) {
            error("seasonal service area exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun effectiveExposure(): Double = round((etaMinutes.toDouble() - budget) * 100) / 100

    fun snapshot(): SeasonalServiceAreaException {
        return SeasonalServiceAreaException(
            code = code,
            name = name,
            budget = budget,
            attempts = attempts,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal service area exception in status open. */
fun newSeasonalServiceAreaException(code: String, name: String): SeasonalServiceAreaException {
    return SeasonalServiceAreaException(
        code = code,
        name = name,
        budget = 0.0,
        attempts = 0,
        etaMinutes = 0,
        status = SeasonalServiceAreaExceptionStatus.OPEN,
    )
}
