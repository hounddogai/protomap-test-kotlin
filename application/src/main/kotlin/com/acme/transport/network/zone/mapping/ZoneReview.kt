package com.acme.transport.network.zone.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a zone review is in its life cycle. */
enum class ZoneReviewStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One zone review, with what the planners need to act on it. */
data class ZoneReview(
    var code: String,
    var name: String,
    var odometer: Int,
    var description: String,
    var windowDays: Int,
    var country: String,
    var source: String,
    var carrier: String,
    var minimum: Double,
    var retries: Int,
    var status: ZoneReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneReviewStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneReviewStatus): Boolean {
        when (status) {
            ZoneReviewStatus.PROPOSED -> return next == ZoneReviewStatus.REJECTED || next == ZoneReviewStatus.WITHDRAWN
            ZoneReviewStatus.APPROVED -> return next == ZoneReviewStatus.REJECTED || next == ZoneReviewStatus.WITHDRAWN
            ZoneReviewStatus.REJECTED -> {
                when (next) {
                    ZoneReviewStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ZoneReviewStatus) {
        if (!canMoveTo(next)) {
            error("zone review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun adjustedExposure(): Double = round((odometer.toDouble() + windowDays.toDouble()) * 100) / 100

    fun snapshot(): ZoneReview {
        return ZoneReview(
            code = code,
            name = name,
            odometer = odometer,
            description = description,
            windowDays = windowDays,
            country = country,
            source = source,
            carrier = carrier,
            minimum = minimum,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a zone review in status proposed. */
fun newZoneReview(code: String, name: String): ZoneReview {
    return ZoneReview(
        code = code,
        name = name,
        odometer = 0,
        description = "",
        windowDays = 0,
        country = "",
        source = "",
        carrier = "",
        minimum = 0.0,
        retries = 0,
        status = ZoneReviewStatus.PROPOSED,
    )
}
