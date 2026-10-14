package com.acme.transport.fleetops.serviceplan.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the regional service plan baseline calculation. */
interface RegionalServicePlanBaselineAdjuster {
    fun describe(): String
    fun adjust(item: RegionalServicePlanBaseline, value: Double): Double
}

data class RoundingRegionalServicePlanBaselineAdjuster(var step: Double) : RegionalServicePlanBaselineAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalServicePlanBaseline, value: Double): Double = roundTo(value, step)
}

data class PercentRegionalServicePlanBaselineAdjuster(
    var percent: Double,
) : RegionalServicePlanBaselineAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalServicePlanBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RegionalServicePlanBaselineMaximumOffsetAdjuster(
    var weight: Double,
) : RegionalServicePlanBaselineAdjuster {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: RegionalServicePlanBaseline, value: Double): Double = value + item.maximum * weight
}

data class UnitRegionalServicePlanBaselineAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalServicePlanBaselineAdjuster {
    override fun describe(): String = "by unit"

    override fun adjust(item: RegionalServicePlanBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRegionalServicePlanBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalServicePlanBaselineAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalServicePlanBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRegionalServicePlanBaselineAdjuster(var cap: Double) : RegionalServicePlanBaselineAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalServicePlanBaseline, value: Double): Double = minOf(value, cap)
}

/** Runs the regional service plan baseline steps in order and keeps a trace of each. */
data class RegionalServicePlanBaselineEngine(
    var steps: MutableList<RegionalServicePlanBaselineAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalServicePlanBaseline): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalServicePlanBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalServicePlanBaselineEngine(flags: Registry): RegionalServicePlanBaselineEngine {
    return RegionalServicePlanBaselineEngine(
        steps = mutableListOf<RegionalServicePlanBaselineAdjuster>(
            RoundingRegionalServicePlanBaselineAdjuster(0.5),
            PercentRegionalServicePlanBaselineAdjuster(-0.2),
            RegionalServicePlanBaselineMaximumOffsetAdjuster(0.57),
            UnitRegionalServicePlanBaselineAdjuster(
                mutableMapOf<String, Double>("retail" to 1.07, "north" to 0.86, "online" to 0.98),
            ),
            FlaggedRegionalServicePlanBaselineAdjuster(flags, "regional-service-plan-baseline-lenient", -0.6),
            CapRegionalServicePlanBaselineAdjuster(23655.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 55196.5,
    )
}
