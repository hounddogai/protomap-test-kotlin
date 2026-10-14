package com.acme.transport.customs.hold.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered hold variance records of customs's hold work, and the rules, stores, and calculations around
// them.

/** Where a tiered hold variance is in its life cycle. */
enum class TieredHoldVarianceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One tiered hold variance, with what the planners need to act on it. */
data class TieredHoldVariance(
    var code: String,
    var name: String,
    var priority: Int,
    var reviewer: String,
    var retries: Int,
    var source: String,
    var status: TieredHoldVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredHoldVarianceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredHoldVarianceStatus): Boolean {
        when (status) {
            TieredHoldVarianceStatus.REQUESTED -> {
                when (next) {
                    TieredHoldVarianceStatus.GRANTED, TieredHoldVarianceStatus.EXPIRED -> return true
                }
                return false
            }
            TieredHoldVarianceStatus.GRANTED -> return next == TieredHoldVarianceStatus.REVOKED
            TieredHoldVarianceStatus.REVOKED -> return next == TieredHoldVarianceStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: TieredHoldVarianceStatus) {
        if (!canMoveTo(next)) {
            error("tiered hold variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun adjustedExposure(): Double = round((retries.toDouble() - priority.toDouble()) * 100) / 100

    fun snapshot(): TieredHoldVariance {
        return TieredHoldVariance(
            code = code,
            name = name,
            priority = priority,
            reviewer = reviewer,
            retries = retries,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a tiered hold variance in status requested. */
fun newTieredHoldVariance(code: String, name: String): TieredHoldVariance {
    return TieredHoldVariance(
        code = code,
        name = name,
        priority = 0,
        reviewer = "",
        retries = 0,
        source = "",
        status = TieredHoldVarianceStatus.REQUESTED,
    )
}
