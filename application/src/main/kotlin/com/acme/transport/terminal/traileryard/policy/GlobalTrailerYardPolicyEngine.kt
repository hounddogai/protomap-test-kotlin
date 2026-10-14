package com.acme.transport.terminal.traileryard.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global trailer yard policy calculation. */
interface GlobalTrailerYardPolicyModifier {
    fun describe(): String
    fun adjust(item: GlobalTrailerYardPolicy, value: Double): Double
}

data class RoundingGlobalTrailerYardPolicyAdjuster(var step: Double) : GlobalTrailerYardPolicyModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalTrailerYardPolicy, value: Double): Double = roundTo(value, step)
}

data class FlaggedGlobalTrailerYardPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalTrailerYardPolicyModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalTrailerYardPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredGlobalTrailerYardPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalTrailerYardPolicyModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalTrailerYardPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class GlobalTrailerYardPolicyMaximumOffsetAdjuster(var weight: Double) : GlobalTrailerYardPolicyModifier {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: GlobalTrailerYardPolicy, value: Double): Double = value + item.maximum * weight
}

/** Runs the global trailer yard policy steps in order and keeps a trace of each. */
data class GlobalTrailerYardPolicyEngine(
    var steps: MutableList<GlobalTrailerYardPolicyModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalTrailerYardPolicy): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalTrailerYardPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalTrailerYardPolicyEngine(flags: Registry): GlobalTrailerYardPolicyEngine {
    return GlobalTrailerYardPolicyEngine(
        steps = mutableListOf<GlobalTrailerYardPolicyModifier>(
            RoundingGlobalTrailerYardPolicyAdjuster(0.1),
            FlaggedGlobalTrailerYardPolicyAdjuster(flags, "global-trailer-yard-policy-legacy", -4.4),
            TieredGlobalTrailerYardPolicyAdjuster(
                mutableListOf<Double>(3813.0, 7269.0),
                mutableListOf<Double>(0.97, 1.12),
            ),
            GlobalTrailerYardPolicyMaximumOffsetAdjuster(1.15),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 95033.2,
    )
}
