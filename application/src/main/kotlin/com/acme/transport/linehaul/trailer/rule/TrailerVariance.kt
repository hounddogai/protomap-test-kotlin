package com.acme.transport.linehaul.trailer.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer variance is in its life cycle. */
enum class TrailerVarianceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A trailer variance. Its code is unique within a tenant. */
data class TrailerVariance(
    var code: String,
    var title: String,
    var lane: String,
    var threshold: Double,
    var minimum: Double,
    var ceiling: Double,
    var status: TrailerVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerVarianceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerVarianceStatus): Boolean {
        when (status) {
            TrailerVarianceStatus.IDLE -> return next == TrailerVarianceStatus.WARMING
            TrailerVarianceStatus.WARMING -> return next == TrailerVarianceStatus.DRAINING || next == TrailerVarianceStatus.STOPPED
            TrailerVarianceStatus.READY -> {
                when (next) {
                    TrailerVarianceStatus.DRAINING -> return true
                }
                return false
            }
            TrailerVarianceStatus.DRAINING -> {
                when (next) {
                    TrailerVarianceStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerVarianceStatus) {
        if (!canMoveTo(next)) {
            error("trailer variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun weightedExposure(): Double = round((threshold + minimum) * 100) / 100

    fun snapshot(): TrailerVariance {
        return TrailerVariance(
            code = code,
            title = title,
            lane = lane,
            threshold = threshold,
            minimum = minimum,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a trailer variance in status idle. */
fun newTrailerVariance(code: String, title: String): TrailerVariance {
    return TrailerVariance(
        code = code,
        title = title,
        lane = "",
        threshold = 0.0,
        minimum = 0.0,
        ceiling = 0.0,
        status = TrailerVarianceStatus.IDLE,
    )
}
