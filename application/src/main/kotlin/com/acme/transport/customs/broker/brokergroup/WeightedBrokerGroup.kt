package com.acme.transport.customs.broker.brokergroup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted broker group records of customs's broker work, and the rules, stores, and calculations around
// them.

/** Where a weighted broker group is in its life cycle. */
enum class WeightedBrokerGroupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a weighted broker group and its current status. */
data class WeightedBrokerGroup(
    var code: String,
    var title: String,
    var etaMinutes: Int,
    var attempts: Int,
    var approved: Boolean,
    var status: WeightedBrokerGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedBrokerGroupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedBrokerGroupStatus): Boolean {
        when (status) {
            WeightedBrokerGroupStatus.PLANNED -> {
                when (next) {
                    WeightedBrokerGroupStatus.IN_PROGRESS, WeightedBrokerGroupStatus.COMPLETED -> return true
                }
                return false
            }
            WeightedBrokerGroupStatus.SCHEDULED -> {
                when (next) {
                    WeightedBrokerGroupStatus.IN_PROGRESS, WeightedBrokerGroupStatus.COMPLETED -> return true
                }
                return false
            }
            WeightedBrokerGroupStatus.IN_PROGRESS -> return next == WeightedBrokerGroupStatus.COMPLETED || next == WeightedBrokerGroupStatus.CANCELLED
            WeightedBrokerGroupStatus.COMPLETED -> return next == WeightedBrokerGroupStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: WeightedBrokerGroupStatus) {
        if (!canMoveTo(next)) {
            error("weighted broker group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun projectedExposure(): Double = round((etaMinutes.toDouble() - attempts.toDouble()) * 100) / 100

    fun snapshot(): WeightedBrokerGroup {
        return WeightedBrokerGroup(
            code = code,
            title = title,
            etaMinutes = etaMinutes,
            attempts = attempts,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a weighted broker group in status planned. */
fun newWeightedBrokerGroup(code: String, title: String): WeightedBrokerGroup {
    return WeightedBrokerGroup(
        code = code,
        title = title,
        etaMinutes = 0,
        attempts = 0,
        approved = false,
        status = WeightedBrokerGroupStatus.PLANNED,
    )
}
