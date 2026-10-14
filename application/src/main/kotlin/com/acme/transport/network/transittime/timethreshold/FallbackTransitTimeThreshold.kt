package com.acme.transport.network.transittime.timethreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback transit time threshold records of network's transit time work, and the rules, stores, and
// calculations around them.

/** Where a fallback transit time threshold is in its life cycle. */
enum class FallbackTransitTimeThresholdStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a fallback transit time threshold and its current status. */
data class FallbackTransitTimeThreshold(
    var code: String,
    var name: String,
    var source: String,
    var cost: Double,
    var sequence: Int,
    var etaMinutes: Int,
    var expiresAt: Int,
    var maximum: Double,
    var category: String,
    var status: FallbackTransitTimeThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackTransitTimeThresholdStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackTransitTimeThresholdStatus): Boolean {
        when (status) {
            FallbackTransitTimeThresholdStatus.OPEN -> {
                when (next) {
                    FallbackTransitTimeThresholdStatus.IN_REVIEW -> return true
                }
                return false
            }
            FallbackTransitTimeThresholdStatus.IN_REVIEW -> {
                when (next) {
                    FallbackTransitTimeThresholdStatus.REOPENED -> return true
                }
                return false
            }
            FallbackTransitTimeThresholdStatus.RESOLVED -> {
                when (next) {
                    FallbackTransitTimeThresholdStatus.REOPENED, FallbackTransitTimeThresholdStatus.ARCHIVED -> return true
                }
                return false
            }
            FallbackTransitTimeThresholdStatus.REOPENED -> return next == FallbackTransitTimeThresholdStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: FallbackTransitTimeThresholdStatus) {
        if (!canMoveTo(next)) {
            error("fallback transit time threshold $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun netVolume(): Double = round((sequence.toDouble() - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): FallbackTransitTimeThreshold {
        return FallbackTransitTimeThreshold(
            code = code,
            name = name,
            source = source,
            cost = cost,
            sequence = sequence,
            etaMinutes = etaMinutes,
            expiresAt = expiresAt,
            maximum = maximum,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a fallback transit time threshold in status open. */
fun newFallbackTransitTimeThreshold(code: String, name: String): FallbackTransitTimeThreshold {
    return FallbackTransitTimeThreshold(
        code = code,
        name = name,
        source = "",
        cost = 0.0,
        sequence = 0,
        etaMinutes = 0,
        expiresAt = 0,
        maximum = 0.0,
        category = "",
        status = FallbackTransitTimeThresholdStatus.OPEN,
    )
}
