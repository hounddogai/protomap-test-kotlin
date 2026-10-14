package com.acme.transport.crewing.restbreak.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered rest break threshold records of crewing's rest break work, and the rules, stores, and
// calculations around them.

/** Where a tiered rest break threshold is in its life cycle. */
enum class TieredRestBreakThresholdStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a tiered rest break threshold and its current status. */
data class TieredRestBreakThreshold(
    var code: String,
    var title: String,
    var minimum: Double,
    var active: Boolean,
    var retries: Int,
    var windowDays: Int,
    var version: Int,
    var status: TieredRestBreakThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredRestBreakThresholdStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredRestBreakThresholdStatus): Boolean {
        when (status) {
            TieredRestBreakThresholdStatus.OPEN -> {
                when (next) {
                    TieredRestBreakThresholdStatus.IN_REVIEW, TieredRestBreakThresholdStatus.RESOLVED -> return true
                }
                return false
            }
            TieredRestBreakThresholdStatus.IN_REVIEW -> {
                when (next) {
                    TieredRestBreakThresholdStatus.REOPENED, TieredRestBreakThresholdStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredRestBreakThresholdStatus.RESOLVED -> {
                when (next) {
                    TieredRestBreakThresholdStatus.REOPENED -> return true
                }
                return false
            }
            TieredRestBreakThresholdStatus.REOPENED -> return next == TieredRestBreakThresholdStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: TieredRestBreakThresholdStatus) {
        if (!canMoveTo(next)) {
            error("tiered rest break threshold $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"tiered rest break threshold"} $code"

    fun weightedTotal(): Double = round(windowDays.toDouble() * retries.toDouble() * 100) / 100

    fun snapshot(): TieredRestBreakThreshold {
        return TieredRestBreakThreshold(
            code = code,
            title = title,
            minimum = minimum,
            active = active,
            retries = retries,
            windowDays = windowDays,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tiered rest break threshold in status open. */
fun newTieredRestBreakThreshold(code: String, title: String): TieredRestBreakThreshold {
    return TieredRestBreakThreshold(
        code = code,
        title = title,
        minimum = 0.0,
        active = false,
        retries = 0,
        windowDays = 0,
        version = 0,
        status = TieredRestBreakThresholdStatus.OPEN,
    )
}
