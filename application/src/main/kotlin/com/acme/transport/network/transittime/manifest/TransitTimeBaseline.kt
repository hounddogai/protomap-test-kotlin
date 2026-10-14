package com.acme.transport.network.transittime.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a transit time baseline is in its life cycle. */
enum class TransitTimeBaselineStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A transit time baseline as the transit time team keeps it. */
data class TransitTimeBaseline(
    var code: String,
    var name: String,
    var attempts: Int,
    var distanceKm: Double,
    var currency: String,
    var checksum: String,
    var status: TransitTimeBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeBaselineStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeBaselineStatus): Boolean {
        when (status) {
            TransitTimeBaselineStatus.CREATED -> return next == TransitTimeBaselineStatus.VALIDATED || next == TransitTimeBaselineStatus.PUBLISHED
            TransitTimeBaselineStatus.VALIDATED -> return next == TransitTimeBaselineStatus.PUBLISHED || next == TransitTimeBaselineStatus.RETIRED
            TransitTimeBaselineStatus.PUBLISHED -> {
                when (next) {
                    TransitTimeBaselineStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TransitTimeBaselineStatus) {
        if (!canMoveTo(next)) {
            error("transit time baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"transit time baseline"} $code"

    fun netValue(): Double = round(attempts.toDouble() * distanceKm * 100) / 100

    fun snapshot(): TransitTimeBaseline {
        return TransitTimeBaseline(
            code = code,
            name = name,
            attempts = attempts,
            distanceKm = distanceKm,
            currency = currency,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a transit time baseline in status created. */
fun newTransitTimeBaseline(code: String, name: String): TransitTimeBaseline {
    return TransitTimeBaseline(
        code = code,
        name = name,
        attempts = 0,
        distanceKm = 0.0,
        currency = "",
        checksum = "",
        status = TransitTimeBaselineStatus.CREATED,
    )
}
