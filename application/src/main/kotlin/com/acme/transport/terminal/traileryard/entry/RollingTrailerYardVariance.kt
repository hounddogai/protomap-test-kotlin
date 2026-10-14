package com.acme.transport.terminal.traileryard.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling trailer yard variance is in its life cycle. */
enum class RollingTrailerYardVarianceStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A rolling trailer yard variance as the trailer yard team keeps it. */
data class RollingTrailerYardVariance(
    var code: String,
    var name: String,
    var trailerId: String,
    var country: String,
    var weight: Double,
    var startsAt: Int,
    var status: RollingTrailerYardVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingTrailerYardVarianceStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingTrailerYardVarianceStatus): Boolean {
        when (status) {
            RollingTrailerYardVarianceStatus.OPEN -> {
                when (next) {
                    RollingTrailerYardVarianceStatus.RESOLVED, RollingTrailerYardVarianceStatus.REOPENED -> return true
                }
                return false
            }
            RollingTrailerYardVarianceStatus.IN_REVIEW -> return next == RollingTrailerYardVarianceStatus.REOPENED
            RollingTrailerYardVarianceStatus.RESOLVED -> return next == RollingTrailerYardVarianceStatus.REOPENED || next == RollingTrailerYardVarianceStatus.ARCHIVED
            RollingTrailerYardVarianceStatus.REOPENED -> return next == RollingTrailerYardVarianceStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RollingTrailerYardVarianceStatus) {
        if (!canMoveTo(next)) {
            error("rolling trailer yard variance $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun effectiveVolume(): Double = round(startsAt.toDouble() * weight * 100) / 100

    fun snapshot(): RollingTrailerYardVariance {
        return RollingTrailerYardVariance(
            code = code,
            name = name,
            trailerId = trailerId,
            country = country,
            weight = weight,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a rolling trailer yard variance in status open. */
fun newRollingTrailerYardVariance(code: String, name: String): RollingTrailerYardVariance {
    return RollingTrailerYardVariance(
        code = code,
        name = name,
        trailerId = "",
        country = "",
        weight = 0.0,
        startsAt = 0,
        status = RollingTrailerYardVarianceStatus.OPEN,
    )
}
