package com.acme.transport.freightpay.accessorial.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fallback accessorial milestone calculation. */
interface FallbackAccessorialMilestoneAdjuster {
    fun describe(): String
    fun adjust(item: FallbackAccessorialMilestone, value: Double): Double
}

data class FlaggedFallbackAccessorialMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackAccessorialMilestoneAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackAccessorialMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapFallbackAccessorialMilestoneAdjuster(var cap: Double) : FallbackAccessorialMilestoneAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackAccessorialMilestone, value: Double): Double = minOf(value, cap)
}

data class PercentFallbackAccessorialMilestoneAdjuster(
    var percent: Double,
) : FallbackAccessorialMilestoneAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackAccessorialMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleFallbackAccessorialMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackAccessorialMilestoneAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: FallbackAccessorialMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredFallbackAccessorialMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackAccessorialMilestoneAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackAccessorialMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FallbackAccessorialMilestoneWeightOffsetAdjuster(
    var weight: Double,
) : FallbackAccessorialMilestoneAdjuster {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: FallbackAccessorialMilestone, value: Double): Double = value + item.weight * weight
}

/** Runs the fallback accessorial milestone steps in order and keeps a trace of each. */
data class FallbackAccessorialMilestoneEngine(
    var steps: MutableList<FallbackAccessorialMilestoneAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackAccessorialMilestone): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackAccessorialMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackAccessorialMilestoneEngine(flags: Registry): FallbackAccessorialMilestoneEngine {
    return FallbackAccessorialMilestoneEngine(
        steps = mutableListOf<FallbackAccessorialMilestoneAdjuster>(
            FlaggedFallbackAccessorialMilestoneAdjuster(flags, "fallback-accessorial-milestone-tiered", -2.2),
            CapFallbackAccessorialMilestoneAdjuster(49049.0),
            PercentFallbackAccessorialMilestoneAdjuster(16.2),
            TitleFallbackAccessorialMilestoneAdjuster(mutableMapOf<String, Double>("online" to 1.02)),
            TieredFallbackAccessorialMilestoneAdjuster(
                mutableListOf<Double>(3709.0, 5286.0),
                mutableListOf<Double>(1.02, 1.05),
            ),
            FallbackAccessorialMilestoneWeightOffsetAdjuster(0.59),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 30079.0,
    )
}
