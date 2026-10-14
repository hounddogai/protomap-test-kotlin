package com.acme.transport.freightpay.accessorial.accessorialentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback accessorial variance is in its life cycle. */
enum class FallbackAccessorialVarianceStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One fallback accessorial variance, with what the planners need to act on it. */
data class FallbackAccessorialVariance(
    var code: String,
    var label: String,
    var unit: String,
    var pinned: Boolean,
    var contactEmail: String,
    var floorValue: Double,
    var startsAt: Int,
    var reviewer: String,
    var archived: Boolean,
    var status: FallbackAccessorialVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackAccessorialVarianceStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackAccessorialVarianceStatus): Boolean {
        when (status) {
            FallbackAccessorialVarianceStatus.OPEN -> {
                when (next) {
                    FallbackAccessorialVarianceStatus.REOPENED -> return true
                }
                return false
            }
            FallbackAccessorialVarianceStatus.IN_REVIEW -> return next == FallbackAccessorialVarianceStatus.REOPENED
            FallbackAccessorialVarianceStatus.RESOLVED -> {
                when (next) {
                    FallbackAccessorialVarianceStatus.REOPENED -> return true
                }
                return false
            }
            FallbackAccessorialVarianceStatus.REOPENED -> {
                when (next) {
                    FallbackAccessorialVarianceStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackAccessorialVarianceStatus) {
        if (!canMoveTo(next)) {
            error("fallback accessorial variance $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"fallback accessorial variance"} $code"

    fun weightedVolume(): Double = round((startsAt.toDouble() - floorValue) * 100) / 100

    fun snapshot(): FallbackAccessorialVariance {
        return FallbackAccessorialVariance(
            code = code,
            label = label,
            unit = unit,
            pinned = pinned,
            contactEmail = contactEmail,
            floorValue = floorValue,
            startsAt = startsAt,
            reviewer = reviewer,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a fallback accessorial variance in status open. */
fun newFallbackAccessorialVariance(code: String, label: String): FallbackAccessorialVariance {
    return FallbackAccessorialVariance(
        code = code,
        label = label,
        unit = "",
        pinned = false,
        contactEmail = "",
        floorValue = 0.0,
        startsAt = 0,
        reviewer = "",
        archived = false,
        status = FallbackAccessorialVarianceStatus.OPEN,
    )
}
