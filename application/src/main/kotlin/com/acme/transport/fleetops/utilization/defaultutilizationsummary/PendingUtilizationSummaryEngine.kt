package com.acme.transport.fleetops.utilization.defaultutilizationsummary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the pending utilization summary calculation. */
interface PendingUtilizationSummaryAdjuster {
    fun describe(): String
    fun adjust(item: PendingUtilizationSummary, value: Double): Double
}

data class FlaggedPendingUtilizationSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingUtilizationSummaryAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingUtilizationSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PendingUtilizationSummaryLimitOffsetAdjuster(
    var weight: Double,
) : PendingUtilizationSummaryAdjuster {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: PendingUtilizationSummary, value: Double): Double {
        return value + item.limit.toDouble() * weight
    }
}

data class CapPendingUtilizationSummaryAdjuster(var cap: Double) : PendingUtilizationSummaryAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingUtilizationSummary, value: Double): Double = minOf(value, cap)
}

data class MinimumPendingUtilizationSummaryAdjuster(var minimum: Double) : PendingUtilizationSummaryAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingUtilizationSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the pending utilization summary steps in order and keeps a trace of each. */
data class PendingUtilizationSummaryEngine(
    var steps: MutableList<PendingUtilizationSummaryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PendingUtilizationSummary): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingUtilizationSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPendingUtilizationSummaryEngine(flags: Registry): PendingUtilizationSummaryEngine {
    return PendingUtilizationSummaryEngine(
        steps = mutableListOf<PendingUtilizationSummaryAdjuster>(
            FlaggedPendingUtilizationSummaryAdjuster(flags, "pending-utilization-summary-fallback", -9.2),
            PendingUtilizationSummaryLimitOffsetAdjuster(0.66),
            CapPendingUtilizationSummaryAdjuster(30553.0),
            MinimumPendingUtilizationSummaryAdjuster(9.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 76009.7,
    )
}
