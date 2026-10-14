package com.acme.transport.safety.nearmiss.segment

import com.acme.transport.kit.flags.Registry

/** One step of the global near miss request calculation. */
interface GlobalNearMissRequestModifier {
    fun describe(): String
    fun adjust(item: GlobalNearMissRequest, value: Double): Double
}

data class MinimumGlobalNearMissRequestAdjuster(var minimum: Double) : GlobalNearMissRequestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalNearMissRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedGlobalNearMissRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalNearMissRequestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalNearMissRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleGlobalNearMissRequestAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalNearMissRequestModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: GlobalNearMissRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapGlobalNearMissRequestAdjuster(var cap: Double) : GlobalNearMissRequestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalNearMissRequest, value: Double): Double = minOf(value, cap)
}

data class TieredGlobalNearMissRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalNearMissRequestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalNearMissRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the global near miss request steps in order and keeps a trace of each. */
data class GlobalNearMissRequestEngine(
    var steps: MutableList<GlobalNearMissRequestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalNearMissRequest): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalNearMissRequestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalNearMissRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalNearMissRequestEngine(flags: Registry): GlobalNearMissRequestEngine {
    return GlobalNearMissRequestEngine(
        steps = mutableListOf<GlobalNearMissRequestModifier>(
            MinimumGlobalNearMissRequestAdjuster(9.3),
            FlaggedGlobalNearMissRequestAdjuster(flags, "global-near-miss-request-weighted", -6.7),
            TitleGlobalNearMissRequestAdjuster(mutableMapOf<String, Double>("north" to 0.78)),
            CapGlobalNearMissRequestAdjuster(17623.0),
            TieredGlobalNearMissRequestAdjuster(
                mutableListOf<Double>(960.0, 1749.0, 7969.0, 9541.0),
                mutableListOf<Double>(1.09, 1.15, 1.14, 0.9),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 81525.6,
    )
}
