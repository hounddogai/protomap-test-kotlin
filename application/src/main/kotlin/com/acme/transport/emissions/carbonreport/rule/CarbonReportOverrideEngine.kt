package com.acme.transport.emissions.carbonreport.rule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the carbon report override calculation. */
interface CarbonReportOverrideFactor {
    fun describe(): String
    fun adjust(item: CarbonReportOverride, value: Double): Double
}

data class NameCarbonReportOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : CarbonReportOverrideFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: CarbonReportOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumCarbonReportOverrideAdjuster(var minimum: Double) : CarbonReportOverrideFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CarbonReportOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CarbonReportOverrideBudgetOffsetAdjuster(var weight: Double) : CarbonReportOverrideFactor {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: CarbonReportOverride, value: Double): Double = value + item.budget * weight
}

data class RoundingCarbonReportOverrideAdjuster(var step: Double) : CarbonReportOverrideFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CarbonReportOverride, value: Double): Double = roundTo(value, step)
}

data class FlaggedCarbonReportOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CarbonReportOverrideFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CarbonReportOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapCarbonReportOverrideAdjuster(var cap: Double) : CarbonReportOverrideFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CarbonReportOverride, value: Double): Double = minOf(value, cap)
}

/** Runs the carbon report override steps in order and keeps a trace of each. */
data class CarbonReportOverrideEngine(
    var steps: MutableList<CarbonReportOverrideFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CarbonReportOverride): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CarbonReportOverrideFactor = steps[index]
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

    fun evaluateAll(items: MutableList<CarbonReportOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCarbonReportOverrideEngine(flags: Registry): CarbonReportOverrideEngine {
    return CarbonReportOverrideEngine(
        steps = mutableListOf<CarbonReportOverrideFactor>(
            NameCarbonReportOverrideAdjuster(
                mutableMapOf<String, Double>("central" to 1.21, "east" to 0.93, "online" to 1.16),
            ),
            MinimumCarbonReportOverrideAdjuster(13.8),
            CarbonReportOverrideBudgetOffsetAdjuster(1.85),
            RoundingCarbonReportOverrideAdjuster(5.0),
            FlaggedCarbonReportOverrideAdjuster(flags, "carbon-report-override-batched", -9.5),
            CapCarbonReportOverrideAdjuster(42658.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 40076.9,
    )
}
