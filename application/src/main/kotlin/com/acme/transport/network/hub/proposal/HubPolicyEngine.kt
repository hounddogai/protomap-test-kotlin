package com.acme.transport.network.hub.proposal

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the hub policy calculation. */
interface HubPolicyModifier {
    fun describe(): String
    fun adjust(item: HubPolicy, value: Double): Double
}

data class TieredHubPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : HubPolicyModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: HubPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LaneHubPolicyAdjuster(var factors: MutableMap<String, Double>) : HubPolicyModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: HubPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentHubPolicyAdjuster(var percent: Double) : HubPolicyModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: HubPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class HubPolicyGraceDaysOffsetAdjuster(var weight: Double) : HubPolicyModifier {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: HubPolicy, value: Double): Double = value + item.graceDays.toDouble() * weight
}

data class CapHubPolicyAdjuster(var cap: Double) : HubPolicyModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: HubPolicy, value: Double): Double = minOf(value, cap)
}

data class MinimumHubPolicyAdjuster(var minimum: Double) : HubPolicyModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: HubPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the hub policy steps in order and keeps a trace of each. */
data class HubPolicyEngine(
    var steps: MutableList<HubPolicyModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: HubPolicy): Double {
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

    fun evaluateAll(items: MutableList<HubPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultHubPolicyEngine(): HubPolicyEngine {
    return HubPolicyEngine(
        steps = mutableListOf<HubPolicyModifier>(
            TieredHubPolicyAdjuster(mutableListOf<Double>(909.0, 6627.0), mutableListOf<Double>(0.96, 0.94)),
            LaneHubPolicyAdjuster(mutableMapOf<String, Double>("east" to 1.21)),
            PercentHubPolicyAdjuster(9.2),
            HubPolicyGraceDaysOffsetAdjuster(0.17),
            CapHubPolicyAdjuster(7034.0),
            MinimumHubPolicyAdjuster(6.6),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 74515.4,
    )
}
