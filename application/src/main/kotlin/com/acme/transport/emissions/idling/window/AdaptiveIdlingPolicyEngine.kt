package com.acme.transport.emissions.idling.window

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive idling policy calculation. */
interface AdaptiveIdlingPolicyModifier {
    fun describe(): String
    fun adjust(item: AdaptiveIdlingPolicy, value: Double): Double
}

data class MinimumAdaptiveIdlingPolicyAdjuster(var minimum: Double) : AdaptiveIdlingPolicyModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveIdlingPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingAdaptiveIdlingPolicyAdjuster(var step: Double) : AdaptiveIdlingPolicyModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveIdlingPolicy, value: Double): Double = roundTo(value, step)
}

data class PercentAdaptiveIdlingPolicyAdjuster(var percent: Double) : AdaptiveIdlingPolicyModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveIdlingPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapAdaptiveIdlingPolicyAdjuster(var cap: Double) : AdaptiveIdlingPolicyModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveIdlingPolicy, value: Double): Double = minOf(value, cap)
}

data class TieredAdaptiveIdlingPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveIdlingPolicyModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveIdlingPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ChannelAdaptiveIdlingPolicyAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveIdlingPolicyModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: AdaptiveIdlingPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the adaptive idling policy steps in order and keeps a trace of each. */
data class AdaptiveIdlingPolicyEngine(
    var steps: MutableList<AdaptiveIdlingPolicyModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveIdlingPolicy): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveIdlingPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveIdlingPolicyEngine(): AdaptiveIdlingPolicyEngine {
    return AdaptiveIdlingPolicyEngine(
        steps = mutableListOf<AdaptiveIdlingPolicyModifier>(
            MinimumAdaptiveIdlingPolicyAdjuster(46.3),
            RoundingAdaptiveIdlingPolicyAdjuster(5.0),
            PercentAdaptiveIdlingPolicyAdjuster(23.0),
            CapAdaptiveIdlingPolicyAdjuster(26851.0),
            TieredAdaptiveIdlingPolicyAdjuster(
                mutableListOf<Double>(858.0, 7280.0, 8538.0),
                mutableListOf<Double>(1.15, 1.12, 1.17),
            ),
            ChannelAdaptiveIdlingPolicyAdjuster(mutableMapOf<String, Double>("online" to 1.12, "retail" to 1.2)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 14511.2,
    )
}
