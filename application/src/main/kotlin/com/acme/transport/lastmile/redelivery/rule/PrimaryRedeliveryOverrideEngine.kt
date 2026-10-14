package com.acme.transport.lastmile.redelivery.rule

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the primary redelivery override calculation. */
interface PrimaryRedeliveryOverrideStep {
    fun describe(): String
    fun adjust(item: PrimaryRedeliveryOverride, value: Double): Double
}

data class LanePrimaryRedeliveryOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryRedeliveryOverrideStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: PrimaryRedeliveryOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredPrimaryRedeliveryOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryRedeliveryOverrideStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryRedeliveryOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentPrimaryRedeliveryOverrideAdjuster(var percent: Double) : PrimaryRedeliveryOverrideStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryRedeliveryOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class PrimaryRedeliveryOverridePriorityOffsetAdjuster(var weight: Double) : PrimaryRedeliveryOverrideStep {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: PrimaryRedeliveryOverride, value: Double): Double {
        return value + item.priority.toDouble() * weight
    }
}

data class MinimumPrimaryRedeliveryOverrideAdjuster(var minimum: Double) : PrimaryRedeliveryOverrideStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryRedeliveryOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingPrimaryRedeliveryOverrideAdjuster(var step: Double) : PrimaryRedeliveryOverrideStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryRedeliveryOverride, value: Double): Double = roundTo(value, step)
}

/** Runs the primary redelivery override steps in order and keeps a trace of each. */
data class PrimaryRedeliveryOverrideEngine(
    var steps: MutableList<PrimaryRedeliveryOverrideStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryRedeliveryOverride): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryRedeliveryOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryRedeliveryOverrideEngine(): PrimaryRedeliveryOverrideEngine {
    return PrimaryRedeliveryOverrideEngine(
        steps = mutableListOf<PrimaryRedeliveryOverrideStep>(
            LanePrimaryRedeliveryOverrideAdjuster(mutableMapOf<String, Double>("central" to 0.73)),
            TieredPrimaryRedeliveryOverrideAdjuster(
                mutableListOf<Double>(5095.0, 5733.0, 6052.0),
                mutableListOf<Double>(0.84, 0.95, 1.11),
            ),
            PercentPrimaryRedeliveryOverrideAdjuster(24.8),
            PrimaryRedeliveryOverridePriorityOffsetAdjuster(1.69),
            MinimumPrimaryRedeliveryOverrideAdjuster(5.6),
            RoundingPrimaryRedeliveryOverrideAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 19721.8,
    )
}
