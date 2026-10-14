package com.acme.transport.fleetops.utilization.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive utilization rollup records of fleetops's utilization work, and the rules, stores, and
// calculations around them.

/** Where a adaptive utilization rollup is in its life cycle. */
enum class AdaptiveUtilizationRollupStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A adaptive utilization rollup. Its code is unique within a tenant. */
data class AdaptiveUtilizationRollup(
    var code: String,
    var name: String,
    var quantity: Int,
    var windowDays: Int,
    var priority: Int,
    var trailerId: String,
    var ceiling: Double,
    var segment: String,
    var ratio: Double,
    var status: AdaptiveUtilizationRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveUtilizationRollupStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveUtilizationRollupStatus): Boolean {
        when (status) {
            AdaptiveUtilizationRollupStatus.NEW -> {
                when (next) {
                    AdaptiveUtilizationRollupStatus.RUNNING -> return true
                }
                return false
            }
            AdaptiveUtilizationRollupStatus.QUEUED -> {
                when (next) {
                    AdaptiveUtilizationRollupStatus.FAILED -> return true
                }
                return false
            }
            AdaptiveUtilizationRollupStatus.RUNNING -> return next == AdaptiveUtilizationRollupStatus.DONE || next == AdaptiveUtilizationRollupStatus.FAILED
            AdaptiveUtilizationRollupStatus.DONE -> return next == AdaptiveUtilizationRollupStatus.FAILED
        }
        return false
    }

    fun moveTo(next: AdaptiveUtilizationRollupStatus) {
        if (!canMoveTo(next)) {
            error("adaptive utilization rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun effectiveExposure(): Double = round(ceiling * priority.toDouble() * 100) / 100

    fun snapshot(): AdaptiveUtilizationRollup {
        return AdaptiveUtilizationRollup(
            code = code,
            name = name,
            quantity = quantity,
            windowDays = windowDays,
            priority = priority,
            trailerId = trailerId,
            ceiling = ceiling,
            segment = segment,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a adaptive utilization rollup in status new. */
fun newAdaptiveUtilizationRollup(code: String, name: String): AdaptiveUtilizationRollup {
    return AdaptiveUtilizationRollup(
        code = code,
        name = name,
        quantity = 0,
        windowDays = 0,
        priority = 0,
        trailerId = "",
        ceiling = 0.0,
        segment = "",
        ratio = 0.0,
        status = AdaptiveUtilizationRollupStatus.NEW,
    )
}
