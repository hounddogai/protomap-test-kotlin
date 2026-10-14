package com.acme.transport.emissions.evcharge.chargebaseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted ev charge target is in its life cycle. */
enum class WeightedEvChargeTargetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a weighted ev charge target and its current status. */
data class WeightedEvChargeTarget(
    var code: String,
    var name: String,
    var trailerId: String,
    var externalRef: String,
    var updatedAt: Int,
    var channel: String,
    var currency: String,
    var priority: Int,
    var status: WeightedEvChargeTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedEvChargeTargetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedEvChargeTargetStatus): Boolean {
        when (status) {
            WeightedEvChargeTargetStatus.PLANNED -> {
                when (next) {
                    WeightedEvChargeTargetStatus.SCHEDULED, WeightedEvChargeTargetStatus.CANCELLED -> return true
                }
                return false
            }
            WeightedEvChargeTargetStatus.SCHEDULED -> {
                when (next) {
                    WeightedEvChargeTargetStatus.CANCELLED -> return true
                }
                return false
            }
            WeightedEvChargeTargetStatus.IN_PROGRESS -> {
                when (next) {
                    WeightedEvChargeTargetStatus.CANCELLED -> return true
                }
                return false
            }
            WeightedEvChargeTargetStatus.COMPLETED -> return next == WeightedEvChargeTargetStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: WeightedEvChargeTargetStatus) {
        if (!canMoveTo(next)) {
            error("weighted ev charge target $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun projectedTotal(): Double = round((priority.toDouble() - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): WeightedEvChargeTarget {
        return WeightedEvChargeTarget(
            code = code,
            name = name,
            trailerId = trailerId,
            externalRef = externalRef,
            updatedAt = updatedAt,
            channel = channel,
            currency = currency,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a weighted ev charge target in status planned. */
fun newWeightedEvChargeTarget(code: String, name: String): WeightedEvChargeTarget {
    return WeightedEvChargeTarget(
        code = code,
        name = name,
        trailerId = "",
        externalRef = "",
        updatedAt = 0,
        channel = "",
        currency = "",
        priority = 0,
        status = WeightedEvChargeTargetStatus.PLANNED,
    )
}
