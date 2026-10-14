package com.acme.transport.customs.broker.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The broker estimate records of customs's broker work, and the rules, stores, and calculations around them.

/** Where a broker estimate is in its life cycle. */
enum class BrokerEstimateStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a broker estimate and its current status. */
data class BrokerEstimate(
    var code: String,
    var label: String,
    var driverId: String,
    var weight: Double,
    var channel: String,
    var startsAt: Int,
    var status: BrokerEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerEstimateStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerEstimateStatus): Boolean {
        when (status) {
            BrokerEstimateStatus.PROPOSED -> {
                when (next) {
                    BrokerEstimateStatus.APPROVED, BrokerEstimateStatus.WITHDRAWN -> return true
                }
                return false
            }
            BrokerEstimateStatus.APPROVED -> return next == BrokerEstimateStatus.REJECTED || next == BrokerEstimateStatus.WITHDRAWN
            BrokerEstimateStatus.REJECTED -> return next == BrokerEstimateStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: BrokerEstimateStatus) {
        if (!canMoveTo(next)) {
            error("broker estimate $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"broker estimate"} $code"

    fun projectedVolume(): Double = round((startsAt.toDouble() - weight) * 100) / 100

    fun snapshot(): BrokerEstimate {
        return BrokerEstimate(
            code = code,
            label = label,
            driverId = driverId,
            weight = weight,
            channel = channel,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a broker estimate in status proposed. */
fun newBrokerEstimate(code: String, label: String): BrokerEstimate {
    return BrokerEstimate(
        code = code,
        label = label,
        driverId = "",
        weight = 0.0,
        channel = "",
        startsAt = 0,
        status = BrokerEstimateStatus.PROPOSED,
    )
}
