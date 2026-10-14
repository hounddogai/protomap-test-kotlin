package com.acme.transport.freightpay.accessorial.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a accessorial variance is in its life cycle. */
enum class AccessorialVarianceStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One accessorial variance, with what the planners need to act on it. */
data class AccessorialVariance(
    var code: String,
    var label: String,
    var approved: Boolean,
    var limit: Int,
    var ratio: Double,
    var windowDays: Int,
    var rate: Double,
    var threshold: Double,
    var weight: Double,
    var status: AccessorialVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialVarianceStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialVarianceStatus): Boolean {
        when (status) {
            AccessorialVarianceStatus.OPEN -> {
                when (next) {
                    AccessorialVarianceStatus.REOPENED, AccessorialVarianceStatus.ARCHIVED -> return true
                }
                return false
            }
            AccessorialVarianceStatus.IN_REVIEW -> return next == AccessorialVarianceStatus.RESOLVED || next == AccessorialVarianceStatus.REOPENED
            AccessorialVarianceStatus.RESOLVED -> return next == AccessorialVarianceStatus.REOPENED || next == AccessorialVarianceStatus.ARCHIVED
            AccessorialVarianceStatus.REOPENED -> return next == AccessorialVarianceStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: AccessorialVarianceStatus) {
        if (!canMoveTo(next)) {
            error("accessorial variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedVolume(): Double = round((ratio - threshold) * 100) / 100

    fun snapshot(): AccessorialVariance {
        return AccessorialVariance(
            code = code,
            label = label,
            approved = approved,
            limit = limit,
            ratio = ratio,
            windowDays = windowDays,
            rate = rate,
            threshold = threshold,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a accessorial variance in status open. */
fun newAccessorialVariance(code: String, label: String): AccessorialVariance {
    return AccessorialVariance(
        code = code,
        label = label,
        approved = false,
        limit = 0,
        ratio = 0.0,
        windowDays = 0,
        rate = 0.0,
        threshold = 0.0,
        weight = 0.0,
        status = AccessorialVarianceStatus.OPEN,
    )
}
