package com.acme.transport.fleetops.utilization.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending utilization digest calculation. */
interface PendingUtilizationDigestFactor {
    fun describe(): String
    fun adjust(item: PendingUtilizationDigest, value: Double): Double
}

data class MinimumPendingUtilizationDigestAdjuster(var minimum: Double) : PendingUtilizationDigestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingUtilizationDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingPendingUtilizationDigestAdjuster(var step: Double) : PendingUtilizationDigestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingUtilizationDigest, value: Double): Double = roundTo(value, step)
}

data class FlaggedPendingUtilizationDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingUtilizationDigestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingUtilizationDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the pending utilization digest steps in order and keeps a trace of each. */
data class PendingUtilizationDigestEngine(
    var steps: MutableList<PendingUtilizationDigestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PendingUtilizationDigest): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingUtilizationDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPendingUtilizationDigestEngine(flags: Registry): PendingUtilizationDigestEngine {
    return PendingUtilizationDigestEngine(
        steps = mutableListOf<PendingUtilizationDigestFactor>(
            MinimumPendingUtilizationDigestAdjuster(25.8),
            RoundingPendingUtilizationDigestAdjuster(5.0),
            FlaggedPendingUtilizationDigestAdjuster(flags, "pending-utilization-digest-default", 9.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 9552.9,
    )
}
