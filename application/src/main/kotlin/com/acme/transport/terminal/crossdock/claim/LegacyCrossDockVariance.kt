package com.acme.transport.terminal.crossdock.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy cross dock variance is in its life cycle. */
enum class LegacyCrossDockVarianceStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A legacy cross dock variance as the cross dock team keeps it. */
data class LegacyCrossDockVariance(
    var code: String,
    var title: String,
    var region: String,
    var minimum: Double,
    var contactEmail: String,
    var graceDays: Int,
    var currency: String,
    var distanceKm: Double,
    var summary: String,
    var odometer: Int,
    var status: LegacyCrossDockVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyCrossDockVarianceStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyCrossDockVarianceStatus): Boolean {
        when (status) {
            LegacyCrossDockVarianceStatus.OPEN -> {
                when (next) {
                    LegacyCrossDockVarianceStatus.IN_REVIEW, LegacyCrossDockVarianceStatus.REOPENED -> return true
                }
                return false
            }
            LegacyCrossDockVarianceStatus.IN_REVIEW -> {
                when (next) {
                    LegacyCrossDockVarianceStatus.RESOLVED -> return true
                }
                return false
            }
            LegacyCrossDockVarianceStatus.RESOLVED -> return next == LegacyCrossDockVarianceStatus.REOPENED
            LegacyCrossDockVarianceStatus.REOPENED -> return next == LegacyCrossDockVarianceStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: LegacyCrossDockVarianceStatus) {
        if (!canMoveTo(next)) {
            error("legacy cross dock variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"legacy cross dock variance"} $code"

    fun adjustedLoad(): Double = round((minimum + odometer.toDouble()) * 100) / 100

    fun snapshot(): LegacyCrossDockVariance {
        return LegacyCrossDockVariance(
            code = code,
            title = title,
            region = region,
            minimum = minimum,
            contactEmail = contactEmail,
            graceDays = graceDays,
            currency = currency,
            distanceKm = distanceKm,
            summary = summary,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a legacy cross dock variance in status open. */
fun newLegacyCrossDockVariance(code: String, title: String): LegacyCrossDockVariance {
    return LegacyCrossDockVariance(
        code = code,
        title = title,
        region = "",
        minimum = 0.0,
        contactEmail = "",
        graceDays = 0,
        currency = "",
        distanceKm = 0.0,
        summary = "",
        odometer = 0,
        status = LegacyCrossDockVarianceStatus.OPEN,
    )
}
