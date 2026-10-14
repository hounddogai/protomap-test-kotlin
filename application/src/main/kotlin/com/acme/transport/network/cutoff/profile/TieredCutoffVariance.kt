package com.acme.transport.network.cutoff.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered cutoff variance is in its life cycle. */
enum class TieredCutoffVarianceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A tiered cutoff variance. Its code is unique within a tenant. */
data class TieredCutoffVariance(
    var code: String,
    var title: String,
    var currency: String,
    var retries: Int,
    var score: Double,
    var fuelLiters: Double,
    var segment: String,
    var expiresAt: Int,
    var status: TieredCutoffVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredCutoffVarianceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredCutoffVarianceStatus): Boolean {
        when (status) {
            TieredCutoffVarianceStatus.REQUESTED -> return next == TieredCutoffVarianceStatus.REVOKED
            TieredCutoffVarianceStatus.GRANTED -> return next == TieredCutoffVarianceStatus.REVOKED || next == TieredCutoffVarianceStatus.EXPIRED
            TieredCutoffVarianceStatus.REVOKED -> return next == TieredCutoffVarianceStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: TieredCutoffVarianceStatus) {
        if (!canMoveTo(next)) {
            error("tiered cutoff variance $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"tiered cutoff variance"} $code"

    fun adjustedExposure(): Double = round((score - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): TieredCutoffVariance {
        return TieredCutoffVariance(
            code = code,
            title = title,
            currency = currency,
            retries = retries,
            score = score,
            fuelLiters = fuelLiters,
            segment = segment,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a tiered cutoff variance in status requested. */
fun newTieredCutoffVariance(code: String, title: String): TieredCutoffVariance {
    return TieredCutoffVariance(
        code = code,
        title = title,
        currency = "",
        retries = 0,
        score = 0.0,
        fuelLiters = 0.0,
        segment = "",
        expiresAt = 0,
        status = TieredCutoffVarianceStatus.REQUESTED,
    )
}
