package com.acme.transport.customs.broker.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a broker variance is in its life cycle. */
enum class BrokerVarianceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One broker variance, with what the planners need to act on it. */
data class BrokerVariance(
    var code: String,
    var title: String,
    var sequence: Int,
    var description: String,
    var maximum: Double,
    var trailerId: String,
    var quantity: Int,
    var driverId: String,
    var capacity: Int,
    var summary: String,
    var status: BrokerVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerVarianceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerVarianceStatus): Boolean {
        when (status) {
            BrokerVarianceStatus.PROPOSED -> {
                when (next) {
                    BrokerVarianceStatus.REJECTED, BrokerVarianceStatus.WITHDRAWN -> return true
                }
                return false
            }
            BrokerVarianceStatus.APPROVED -> {
                when (next) {
                    BrokerVarianceStatus.REJECTED, BrokerVarianceStatus.WITHDRAWN -> return true
                }
                return false
            }
            BrokerVarianceStatus.REJECTED -> return next == BrokerVarianceStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: BrokerVarianceStatus) {
        if (!canMoveTo(next)) {
            error("broker variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun adjustedExposure(): Double = round((capacity.toDouble() - maximum) * 100) / 100

    fun snapshot(): BrokerVariance {
        return BrokerVariance(
            code = code,
            title = title,
            sequence = sequence,
            description = description,
            maximum = maximum,
            trailerId = trailerId,
            quantity = quantity,
            driverId = driverId,
            capacity = capacity,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a broker variance in status proposed. */
fun newBrokerVariance(code: String, title: String): BrokerVariance {
    return BrokerVariance(
        code = code,
        title = title,
        sequence = 0,
        description = "",
        maximum = 0.0,
        trailerId = "",
        quantity = 0,
        driverId = "",
        capacity = 0,
        summary = "",
        status = BrokerVarianceStatus.PROPOSED,
    )
}
