package com.acme.transport.safety.nearmiss.plan

import com.acme.transport.kit.flags.Registry

/** One step of the near miss plan calculation. */
interface NearMissPlanModifier {
    fun describe(): String
    fun adjust(item: NearMissPlan, value: Double): Double
}

data class MinimumNearMissPlanAdjuster(var minimum: Double) : NearMissPlanModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NearMissPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NearMissPlanRateOffsetAdjuster(var weight: Double) : NearMissPlanModifier {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: NearMissPlan, value: Double): Double = value + item.rate * weight
}

data class PercentNearMissPlanAdjuster(var percent: Double) : NearMissPlanModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: NearMissPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapNearMissPlanAdjuster(var cap: Double) : NearMissPlanModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NearMissPlan, value: Double): Double = minOf(value, cap)
}

data class FlaggedNearMissPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : NearMissPlanModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: NearMissPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the near miss plan steps in order and keeps a trace of each. */
data class NearMissPlanEngine(
    var steps: MutableList<NearMissPlanModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: NearMissPlan): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: NearMissPlanModifier = steps[index]
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

    fun evaluateAll(items: MutableList<NearMissPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultNearMissPlanEngine(flags: Registry): NearMissPlanEngine {
    return NearMissPlanEngine(
        steps = mutableListOf<NearMissPlanModifier>(
            MinimumNearMissPlanAdjuster(8.5),
            NearMissPlanRateOffsetAdjuster(0.86),
            PercentNearMissPlanAdjuster(21.4),
            CapNearMissPlanAdjuster(33724.0),
            FlaggedNearMissPlanAdjuster(flags, "near-miss-plan-primary", -0.9),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 5890.3,
    )
}
