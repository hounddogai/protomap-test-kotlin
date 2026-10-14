package com.acme.transport.linehaul.backhaul.preference

import com.acme.transport.kit.flags.Registry

/** One step of the backhaul estimate calculation. */
interface BackhaulEstimateAdjuster {
    fun describe(): String
    fun adjust(item: BackhaulEstimate, value: Double): Double
}

data class CapBackhaulEstimateAdjuster(var cap: Double) : BackhaulEstimateAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BackhaulEstimate, value: Double): Double = minOf(value, cap)
}

data class PercentBackhaulEstimateAdjuster(var percent: Double) : BackhaulEstimateAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BackhaulEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedBackhaulEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BackhaulEstimateAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BackhaulEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the backhaul estimate steps in order and keeps a trace of each. */
data class BackhaulEstimateEngine(
    var steps: MutableList<BackhaulEstimateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BackhaulEstimate): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BackhaulEstimateAdjuster = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BackhaulEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBackhaulEstimateEngine(flags: Registry): BackhaulEstimateEngine {
    return BackhaulEstimateEngine(
        steps = mutableListOf<BackhaulEstimateAdjuster>(
            CapBackhaulEstimateAdjuster(45866.0),
            PercentBackhaulEstimateAdjuster(8.8),
            FlaggedBackhaulEstimateAdjuster(flags, "backhaul-estimate-default", 3.2),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 24031.2,
    )
}
