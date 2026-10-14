package com.acme.transport.linehaul.laneplan.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive lane plan target calculation. */
interface AdaptiveLanePlanTargetModifier {
    fun describe(): String
    fun adjust(item: AdaptiveLanePlanTarget, value: Double): Double
}

data class PercentAdaptiveLanePlanTargetAdjuster(var percent: Double) : AdaptiveLanePlanTargetModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveLanePlanTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedAdaptiveLanePlanTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveLanePlanTargetModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveLanePlanTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredAdaptiveLanePlanTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveLanePlanTargetModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveLanePlanTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingAdaptiveLanePlanTargetAdjuster(var step: Double) : AdaptiveLanePlanTargetModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveLanePlanTarget, value: Double): Double = roundTo(value, step)
}

data class CategoryAdaptiveLanePlanTargetAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveLanePlanTargetModifier {
    override fun describe(): String = "by category"

    override fun adjust(item: AdaptiveLanePlanTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapAdaptiveLanePlanTargetAdjuster(var cap: Double) : AdaptiveLanePlanTargetModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveLanePlanTarget, value: Double): Double = minOf(value, cap)
}

/** Runs the adaptive lane plan target steps in order and keeps a trace of each. */
data class AdaptiveLanePlanTargetEngine(
    var steps: MutableList<AdaptiveLanePlanTargetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveLanePlanTarget): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveLanePlanTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveLanePlanTargetEngine(flags: Registry): AdaptiveLanePlanTargetEngine {
    return AdaptiveLanePlanTargetEngine(
        steps = mutableListOf<AdaptiveLanePlanTargetModifier>(
            PercentAdaptiveLanePlanTargetAdjuster(24.3),
            FlaggedAdaptiveLanePlanTargetAdjuster(flags, "adaptive-lane-plan-target-capped", -6.1),
            TieredAdaptiveLanePlanTargetAdjuster(
                mutableListOf<Double>(598.0, 9097.0),
                mutableListOf<Double>(1.02, 0.92),
            ),
            RoundingAdaptiveLanePlanTargetAdjuster(5.0),
            CategoryAdaptiveLanePlanTargetAdjuster(
                mutableMapOf<String, Double>("central" to 0.95, "south" to 1.18, "retail" to 1.24),
            ),
            CapAdaptiveLanePlanTargetAdjuster(11673.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 25649.2,
    )
}
