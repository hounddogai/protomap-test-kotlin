package com.acme.transport.safety.nearmiss.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the near miss policy calculation. */
interface NearMissPolicyAdjuster {
    fun describe(): String
    fun adjust(item: NearMissPolicy, value: Double): Double
}

data class PercentNearMissPolicyAdjuster(var percent: Double) : NearMissPolicyAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: NearMissPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedNearMissPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : NearMissPolicyAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: NearMissPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NearMissPolicyScoreOffsetAdjuster(var weight: Double) : NearMissPolicyAdjuster {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: NearMissPolicy, value: Double): Double = value + item.score * weight
}

data class UnitNearMissPolicyAdjuster(var factors: MutableMap<String, Double>) : NearMissPolicyAdjuster {
    override fun describe(): String = "by unit"

    override fun adjust(item: NearMissPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapNearMissPolicyAdjuster(var cap: Double) : NearMissPolicyAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NearMissPolicy, value: Double): Double = minOf(value, cap)
}

/** Runs the near miss policy steps in order and keeps a trace of each. */
data class NearMissPolicyEngine(
    var steps: MutableList<NearMissPolicyAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: NearMissPolicy): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<NearMissPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultNearMissPolicyEngine(flags: Registry): NearMissPolicyEngine {
    return NearMissPolicyEngine(
        steps = mutableListOf<NearMissPolicyAdjuster>(
            PercentNearMissPolicyAdjuster(-0.4),
            FlaggedNearMissPolicyAdjuster(flags, "near-miss-policy-weighted", 0.8),
            NearMissPolicyScoreOffsetAdjuster(1.07),
            UnitNearMissPolicyAdjuster(
                mutableMapOf<String, Double>("online" to 0.71, "east" to 1.17, "south" to 1.14),
            ),
            CapNearMissPolicyAdjuster(3626.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 74913.2,
    )
}
