package com.acme.transport.customs.clearance.clearanceoverride

import com.acme.transport.kit.flags.Registry

/** One step of the clearance policy calculation. */
interface ClearancePolicyStep {
    fun describe(): String
    fun adjust(item: ClearancePolicy, value: Double): Double
}

data class ClearancePolicyRateOffsetAdjuster(var weight: Double) : ClearancePolicyStep {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: ClearancePolicy, value: Double): Double = value + item.rate * weight
}

data class FlaggedClearancePolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ClearancePolicyStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ClearancePolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentClearancePolicyAdjuster(var percent: Double) : ClearancePolicyStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ClearancePolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumClearancePolicyAdjuster(var minimum: Double) : ClearancePolicyStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ClearancePolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameClearancePolicyAdjuster(var factors: MutableMap<String, Double>) : ClearancePolicyStep {
    override fun describe(): String = "by name"

    override fun adjust(item: ClearancePolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredClearancePolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ClearancePolicyStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ClearancePolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the clearance policy steps in order and keeps a trace of each. */
data class ClearancePolicyEngine(
    var steps: MutableList<ClearancePolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ClearancePolicy): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ClearancePolicyStep = steps[index]
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

    fun evaluateAll(items: MutableList<ClearancePolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultClearancePolicyEngine(flags: Registry): ClearancePolicyEngine {
    return ClearancePolicyEngine(
        steps = mutableListOf<ClearancePolicyStep>(
            ClearancePolicyRateOffsetAdjuster(1.72),
            FlaggedClearancePolicyAdjuster(flags, "clearance-policy-fallback", -9.5),
            PercentClearancePolicyAdjuster(-1.9),
            MinimumClearancePolicyAdjuster(28.8),
            NameClearancePolicyAdjuster(mutableMapOf<String, Double>("central" to 1.09, "south" to 0.96)),
            TieredClearancePolicyAdjuster(
                mutableListOf<Double>(6428.0, 9883.0),
                mutableListOf<Double>(1.02, 1.13),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 30001.8,
    )
}
