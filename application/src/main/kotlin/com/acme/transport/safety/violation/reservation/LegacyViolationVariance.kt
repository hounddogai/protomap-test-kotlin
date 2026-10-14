package com.acme.transport.safety.violation.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy violation variance is in its life cycle. */
enum class LegacyViolationVarianceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A legacy violation variance as the violation team keeps it. */
data class LegacyViolationVariance(
    var code: String,
    var title: String,
    var updatedAt: Int,
    var pallets: Int,
    var label: String,
    var startsAt: Int,
    var status: LegacyViolationVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyViolationVarianceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyViolationVarianceStatus): Boolean {
        when (status) {
            LegacyViolationVarianceStatus.IDLE -> return next == LegacyViolationVarianceStatus.DRAINING
            LegacyViolationVarianceStatus.WARMING -> return next == LegacyViolationVarianceStatus.READY
            LegacyViolationVarianceStatus.READY -> {
                when (next) {
                    LegacyViolationVarianceStatus.DRAINING -> return true
                }
                return false
            }
            LegacyViolationVarianceStatus.DRAINING -> {
                when (next) {
                    LegacyViolationVarianceStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyViolationVarianceStatus) {
        if (!canMoveTo(next)) {
            error("legacy violation variance $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"legacy violation variance"} $code"

    fun netLoad(): Double = round((updatedAt.toDouble() + pallets.toDouble()) * 100) / 100

    fun snapshot(): LegacyViolationVariance {
        return LegacyViolationVariance(
            code = code,
            title = title,
            updatedAt = updatedAt,
            pallets = pallets,
            label = label,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy violation variance in status idle. */
fun newLegacyViolationVariance(code: String, title: String): LegacyViolationVariance {
    return LegacyViolationVariance(
        code = code,
        title = title,
        updatedAt = 0,
        pallets = 0,
        label = "",
        startsAt = 0,
        status = LegacyViolationVarianceStatus.IDLE,
    )
}
