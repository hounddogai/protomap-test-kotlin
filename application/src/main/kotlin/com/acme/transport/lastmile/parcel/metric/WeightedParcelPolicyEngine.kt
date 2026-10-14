package com.acme.transport.lastmile.parcel.metric

import com.acme.transport.kit.flags.Registry

/** One step of the weighted parcel policy calculation. */
interface WeightedParcelPolicyFactor {
    fun describe(): String
    fun adjust(item: WeightedParcelPolicy, value: Double): Double
}

data class TieredWeightedParcelPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedParcelPolicyFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedParcelPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DepotWeightedParcelPolicyAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedParcelPolicyFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: WeightedParcelPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapWeightedParcelPolicyAdjuster(var cap: Double) : WeightedParcelPolicyFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedParcelPolicy, value: Double): Double = minOf(value, cap)
}

data class FlaggedWeightedParcelPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedParcelPolicyFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedParcelPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the weighted parcel policy steps in order and keeps a trace of each. */
data class WeightedParcelPolicyEngine(
    var steps: MutableList<WeightedParcelPolicyFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedParcelPolicy): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedParcelPolicyFactor = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedParcelPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedParcelPolicyEngine(flags: Registry): WeightedParcelPolicyEngine {
    return WeightedParcelPolicyEngine(
        steps = mutableListOf<WeightedParcelPolicyFactor>(
            TieredWeightedParcelPolicyAdjuster(
                mutableListOf<Double>(1433.0, 7162.0),
                mutableListOf<Double>(1.0, 1.04),
            ),
            DepotWeightedParcelPolicyAdjuster(mutableMapOf<String, Double>("online" to 1.15)),
            CapWeightedParcelPolicyAdjuster(28235.0),
            FlaggedWeightedParcelPolicyAdjuster(flags, "weighted-parcel-policy-capped", 6.9),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 54602.4,
    )
}
