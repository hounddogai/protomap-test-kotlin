package com.acme.transport.fleetops.tire.tireclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted tire cycle is in its life cycle. */
enum class WeightedTireCycleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A weighted tire cycle. Its code is unique within a tenant. */
data class WeightedTireCycle(
    var code: String,
    var label: String,
    var graceDays: Int,
    var amount: Double,
    var ceiling: Double,
    var status: WeightedTireCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedTireCycleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedTireCycleStatus): Boolean {
        when (status) {
            WeightedTireCycleStatus.IDLE -> {
                when (next) {
                    WeightedTireCycleStatus.READY -> return true
                }
                return false
            }
            WeightedTireCycleStatus.WARMING -> return next == WeightedTireCycleStatus.DRAINING
            WeightedTireCycleStatus.READY -> {
                when (next) {
                    WeightedTireCycleStatus.DRAINING -> return true
                }
                return false
            }
            WeightedTireCycleStatus.DRAINING -> return next == WeightedTireCycleStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: WeightedTireCycleStatus) {
        if (!canMoveTo(next)) {
            error("weighted tire cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"weighted tire cycle"} $code"

    fun effectiveExposure(): Double = round(ceiling * graceDays.toDouble() * 100) / 100

    fun snapshot(): WeightedTireCycle {
        return WeightedTireCycle(
            code = code,
            label = label,
            graceDays = graceDays,
            amount = amount,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a weighted tire cycle in status idle. */
fun newWeightedTireCycle(code: String, label: String): WeightedTireCycle {
    return WeightedTireCycle(
        code = code,
        label = label,
        graceDays = 0,
        amount = 0.0,
        ceiling = 0.0,
        status = WeightedTireCycleStatus.IDLE,
    )
}
