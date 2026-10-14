package com.acme.transport.customs.broker.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal broker review records of customs's broker work, and the rules, stores, and calculations around
// them.

/** Where a seasonal broker review is in its life cycle. */
enum class SeasonalBrokerReviewStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A seasonal broker review as the broker team keeps it. */
data class SeasonalBrokerReview(
    var code: String,
    var name: String,
    var rate: Double,
    var odometer: Int,
    var source: String,
    var routeCode: String,
    var batchSize: Int,
    var status: SeasonalBrokerReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalBrokerReviewStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalBrokerReviewStatus): Boolean {
        when (status) {
            SeasonalBrokerReviewStatus.IDLE -> return next == SeasonalBrokerReviewStatus.DRAINING
            SeasonalBrokerReviewStatus.WARMING -> return next == SeasonalBrokerReviewStatus.READY || next == SeasonalBrokerReviewStatus.STOPPED
            SeasonalBrokerReviewStatus.READY -> {
                when (next) {
                    SeasonalBrokerReviewStatus.STOPPED -> return true
                }
                return false
            }
            SeasonalBrokerReviewStatus.DRAINING -> return next == SeasonalBrokerReviewStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: SeasonalBrokerReviewStatus) {
        if (!canMoveTo(next)) {
            error("seasonal broker review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun effectiveExposure(): Double = round((batchSize.toDouble() + rate) * 100) / 100

    fun snapshot(): SeasonalBrokerReview {
        return SeasonalBrokerReview(
            code = code,
            name = name,
            rate = rate,
            odometer = odometer,
            source = source,
            routeCode = routeCode,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a seasonal broker review in status idle. */
fun newSeasonalBrokerReview(code: String, name: String): SeasonalBrokerReview {
    return SeasonalBrokerReview(
        code = code,
        name = name,
        rate = 0.0,
        odometer = 0,
        source = "",
        routeCode = "",
        batchSize = 0,
        status = SeasonalBrokerReviewStatus.IDLE,
    )
}
