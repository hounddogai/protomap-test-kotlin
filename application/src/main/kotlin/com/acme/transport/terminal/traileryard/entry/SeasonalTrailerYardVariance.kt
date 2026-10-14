package com.acme.transport.terminal.traileryard.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal trailer yard variance is in its life cycle. */
enum class SeasonalTrailerYardVarianceStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A seasonal trailer yard variance. Its code is unique within a tenant. */
data class SeasonalTrailerYardVariance(
    var code: String,
    var name: String,
    var cost: Double,
    var driverId: String,
    var description: String,
    var weight: Double,
    var etaMinutes: Int,
    var amount: Double,
    var margin: Double,
    var ownerId: String,
    var country: String,
    var status: SeasonalTrailerYardVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalTrailerYardVarianceStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalTrailerYardVarianceStatus): Boolean {
        when (status) {
            SeasonalTrailerYardVarianceStatus.DRAFT -> return next == SeasonalTrailerYardVarianceStatus.PENDING
            SeasonalTrailerYardVarianceStatus.PENDING -> {
                when (next) {
                    SeasonalTrailerYardVarianceStatus.ACTIVE -> return true
                }
                return false
            }
            SeasonalTrailerYardVarianceStatus.ACTIVE -> return next == SeasonalTrailerYardVarianceStatus.SUSPENDED
            SeasonalTrailerYardVarianceStatus.SUSPENDED -> {
                when (next) {
                    SeasonalTrailerYardVarianceStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalTrailerYardVarianceStatus) {
        if (!canMoveTo(next)) {
            error("seasonal trailer yard variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"seasonal trailer yard variance"} $code"

    fun projectedLoad(): Double = round(amount * margin * 100) / 100

    fun snapshot(): SeasonalTrailerYardVariance {
        return SeasonalTrailerYardVariance(
            code = code,
            name = name,
            cost = cost,
            driverId = driverId,
            description = description,
            weight = weight,
            etaMinutes = etaMinutes,
            amount = amount,
            margin = margin,
            ownerId = ownerId,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a seasonal trailer yard variance in status draft. */
fun newSeasonalTrailerYardVariance(code: String, name: String): SeasonalTrailerYardVariance {
    return SeasonalTrailerYardVariance(
        code = code,
        name = name,
        cost = 0.0,
        driverId = "",
        description = "",
        weight = 0.0,
        etaMinutes = 0,
        amount = 0.0,
        margin = 0.0,
        ownerId = "",
        country = "",
        status = SeasonalTrailerYardVarianceStatus.DRAFT,
    )
}
