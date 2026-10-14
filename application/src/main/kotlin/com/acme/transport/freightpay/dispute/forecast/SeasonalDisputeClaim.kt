package com.acme.transport.freightpay.dispute.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal dispute claim is in its life cycle. */
enum class SeasonalDisputeClaimStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A seasonal dispute claim as the dispute team keeps it. */
data class SeasonalDisputeClaim(
    var code: String,
    var name: String,
    var carrier: String,
    var timezone: String,
    var windowDays: Int,
    var distanceKm: Double,
    var locale: String,
    var status: SeasonalDisputeClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalDisputeClaimStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalDisputeClaimStatus): Boolean {
        when (status) {
            SeasonalDisputeClaimStatus.REQUESTED -> {
                when (next) {
                    SeasonalDisputeClaimStatus.GRANTED -> return true
                }
                return false
            }
            SeasonalDisputeClaimStatus.GRANTED -> {
                when (next) {
                    SeasonalDisputeClaimStatus.REVOKED, SeasonalDisputeClaimStatus.EXPIRED -> return true
                }
                return false
            }
            SeasonalDisputeClaimStatus.REVOKED -> {
                when (next) {
                    SeasonalDisputeClaimStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalDisputeClaimStatus) {
        if (!canMoveTo(next)) {
            error("seasonal dispute claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun weightedTotal(): Double = round(windowDays.toDouble() * distanceKm * 100) / 100

    fun snapshot(): SeasonalDisputeClaim {
        return SeasonalDisputeClaim(
            code = code,
            name = name,
            carrier = carrier,
            timezone = timezone,
            windowDays = windowDays,
            distanceKm = distanceKm,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a seasonal dispute claim in status requested. */
fun newSeasonalDisputeClaim(code: String, name: String): SeasonalDisputeClaim {
    return SeasonalDisputeClaim(
        code = code,
        name = name,
        carrier = "",
        timezone = "",
        windowDays = 0,
        distanceKm = 0.0,
        locale = "",
        status = SeasonalDisputeClaimStatus.REQUESTED,
    )
}
