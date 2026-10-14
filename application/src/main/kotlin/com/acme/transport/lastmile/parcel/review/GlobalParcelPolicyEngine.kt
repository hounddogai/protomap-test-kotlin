package com.acme.transport.lastmile.parcel.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the global parcel policy calculation. */
interface GlobalParcelPolicyStep {
    fun describe(): String
    fun adjust(item: GlobalParcelPolicy, value: Double): Double
}

data class FlaggedGlobalParcelPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalParcelPolicyStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalParcelPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LaneGlobalParcelPolicyAdjuster(var factors: MutableMap<String, Double>) : GlobalParcelPolicyStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: GlobalParcelPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredGlobalParcelPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalParcelPolicyStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalParcelPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapGlobalParcelPolicyAdjuster(var cap: Double) : GlobalParcelPolicyStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalParcelPolicy, value: Double): Double = minOf(value, cap)
}

data class MinimumGlobalParcelPolicyAdjuster(var minimum: Double) : GlobalParcelPolicyStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalParcelPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class GlobalParcelPolicyPriorityOffsetAdjuster(var weight: Double) : GlobalParcelPolicyStep {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: GlobalParcelPolicy, value: Double): Double = value + item.priority.toDouble() * weight
}

/** Runs the global parcel policy steps in order and keeps a trace of each. */
data class GlobalParcelPolicyEngine(
    var steps: MutableList<GlobalParcelPolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalParcelPolicy): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalParcelPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalParcelPolicyEngine(flags: Registry): GlobalParcelPolicyEngine {
    return GlobalParcelPolicyEngine(
        steps = mutableListOf<GlobalParcelPolicyStep>(
            FlaggedGlobalParcelPolicyAdjuster(flags, "global-parcel-policy-weighted", -0.3),
            LaneGlobalParcelPolicyAdjuster(
                mutableMapOf<String, Double>("online" to 0.95, "west" to 0.97, "central" to 1.33),
            ),
            TieredGlobalParcelPolicyAdjuster(
                mutableListOf<Double>(1272.0, 7436.0, 9863.0),
                mutableListOf<Double>(0.89, 1.05, 0.95),
            ),
            CapGlobalParcelPolicyAdjuster(40693.0),
            MinimumGlobalParcelPolicyAdjuster(30.2),
            GlobalParcelPolicyPriorityOffsetAdjuster(0.08),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 44011.9,
    )
}
