package com.acme.transport.terminal.traileryard.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback trailer yard variance is in its life cycle. */
enum class FallbackTrailerYardVarianceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A fallback trailer yard variance as the trailer yard team keeps it. */
data class FallbackTrailerYardVariance(
    var code: String,
    var title: String,
    var lane: String,
    var windowDays: Int,
    var summary: String,
    var batchSize: Int,
    var minimum: Double,
    var version: Int,
    var etaMinutes: Int,
    var amount: Double,
    var status: FallbackTrailerYardVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackTrailerYardVarianceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackTrailerYardVarianceStatus): Boolean {
        when (status) {
            FallbackTrailerYardVarianceStatus.REQUESTED -> return next == FallbackTrailerYardVarianceStatus.REVOKED || next == FallbackTrailerYardVarianceStatus.EXPIRED
            FallbackTrailerYardVarianceStatus.GRANTED -> return next == FallbackTrailerYardVarianceStatus.REVOKED
            FallbackTrailerYardVarianceStatus.REVOKED -> return next == FallbackTrailerYardVarianceStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: FallbackTrailerYardVarianceStatus) {
        if (!canMoveTo(next)) {
            error("fallback trailer yard variance $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $title"

    fun adjustedTotal(): Double = round((etaMinutes.toDouble() - version.toDouble()) * 100) / 100

    fun snapshot(): FallbackTrailerYardVariance {
        return FallbackTrailerYardVariance(
            code = code,
            title = title,
            lane = lane,
            windowDays = windowDays,
            summary = summary,
            batchSize = batchSize,
            minimum = minimum,
            version = version,
            etaMinutes = etaMinutes,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a fallback trailer yard variance in status requested. */
fun newFallbackTrailerYardVariance(code: String, title: String): FallbackTrailerYardVariance {
    return FallbackTrailerYardVariance(
        code = code,
        title = title,
        lane = "",
        windowDays = 0,
        summary = "",
        batchSize = 0,
        minimum = 0.0,
        version = 0,
        etaMinutes = 0,
        amount = 0.0,
        status = FallbackTrailerYardVarianceStatus.REQUESTED,
    )
}
