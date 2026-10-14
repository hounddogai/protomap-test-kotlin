package com.acme.transport.linehaul.trailer.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient trailer variance is in its life cycle. */
enum class LenientTrailerVarianceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A lenient trailer variance. Its code is unique within a tenant. */
data class LenientTrailerVariance(
    var code: String,
    var name: String,
    var graceDays: Int,
    var limit: Int,
    var startsAt: Int,
    var tags: MutableList<String>,
    var currency: String,
    var status: LenientTrailerVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientTrailerVarianceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientTrailerVarianceStatus): Boolean {
        when (status) {
            LenientTrailerVarianceStatus.IDLE -> {
                when (next) {
                    LenientTrailerVarianceStatus.WARMING, LenientTrailerVarianceStatus.STOPPED -> return true
                }
                return false
            }
            LenientTrailerVarianceStatus.WARMING -> return next == LenientTrailerVarianceStatus.DRAINING
            LenientTrailerVarianceStatus.READY -> {
                when (next) {
                    LenientTrailerVarianceStatus.DRAINING, LenientTrailerVarianceStatus.STOPPED -> return true
                }
                return false
            }
            LenientTrailerVarianceStatus.DRAINING -> return next == LenientTrailerVarianceStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: LenientTrailerVarianceStatus) {
        if (!canMoveTo(next)) {
            error("lenient trailer variance $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"lenient trailer variance"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedValue(): Double = round((graceDays.toDouble() - startsAt.toDouble()) * 100) / 100

    fun snapshot(): LenientTrailerVariance {
        return LenientTrailerVariance(
            code = code,
            name = name,
            graceDays = graceDays,
            limit = limit,
            startsAt = startsAt,
            tags = tags,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lenient trailer variance in status idle. */
fun newLenientTrailerVariance(code: String, name: String): LenientTrailerVariance {
    return LenientTrailerVariance(
        code = code,
        name = name,
        graceDays = 0,
        limit = 0,
        startsAt = 0,
        tags = mutableListOf<String>(),
        currency = "",
        status = LenientTrailerVarianceStatus.IDLE,
    )
}
