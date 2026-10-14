package com.acme.transport.network.transittime.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback transit time baseline is in its life cycle. */
enum class FallbackTransitTimeBaselineStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A fallback transit time baseline as the transit time team keeps it. */
data class FallbackTransitTimeBaseline(
    var code: String,
    var name: String,
    var locale: String,
    var label: String,
    var stopCount: Int,
    var retries: Int,
    var sequence: Int,
    var rate: Double,
    var batchSize: Int,
    var status: FallbackTransitTimeBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackTransitTimeBaselineStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackTransitTimeBaselineStatus): Boolean {
        when (status) {
            FallbackTransitTimeBaselineStatus.REQUESTED -> return next == FallbackTransitTimeBaselineStatus.GRANTED || next == FallbackTransitTimeBaselineStatus.REVOKED
            FallbackTransitTimeBaselineStatus.GRANTED -> return next == FallbackTransitTimeBaselineStatus.EXPIRED
            FallbackTransitTimeBaselineStatus.REVOKED -> return next == FallbackTransitTimeBaselineStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: FallbackTransitTimeBaselineStatus) {
        if (!canMoveTo(next)) {
            error("fallback transit time baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $name"

    fun adjustedTotal(): Double = round((retries.toDouble() + batchSize.toDouble()) * 100) / 100

    fun snapshot(): FallbackTransitTimeBaseline {
        return FallbackTransitTimeBaseline(
            code = code,
            name = name,
            locale = locale,
            label = label,
            stopCount = stopCount,
            retries = retries,
            sequence = sequence,
            rate = rate,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a fallback transit time baseline in status requested. */
fun newFallbackTransitTimeBaseline(code: String, name: String): FallbackTransitTimeBaseline {
    return FallbackTransitTimeBaseline(
        code = code,
        name = name,
        locale = "",
        label = "",
        stopCount = 0,
        retries = 0,
        sequence = 0,
        rate = 0.0,
        batchSize = 0,
        status = FallbackTransitTimeBaselineStatus.REQUESTED,
    )
}
