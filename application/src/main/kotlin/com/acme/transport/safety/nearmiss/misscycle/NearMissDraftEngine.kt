package com.acme.transport.safety.nearmiss.misscycle

/** One step of the near miss draft calculation. */
interface NearMissDraftAdjuster {
    fun describe(): String
    fun adjust(item: NearMissDraft, value: Double): Double
}

data class CategoryNearMissDraftAdjuster(var factors: MutableMap<String, Double>) : NearMissDraftAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: NearMissDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class NearMissDraftExpiresAtOffsetAdjuster(var weight: Double) : NearMissDraftAdjuster {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: NearMissDraft, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

data class TieredNearMissDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : NearMissDraftAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: NearMissDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumNearMissDraftAdjuster(var minimum: Double) : NearMissDraftAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NearMissDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the near miss draft steps in order and keeps a trace of each. */
data class NearMissDraftEngine(
    var steps: MutableList<NearMissDraftAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: NearMissDraft): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: NearMissDraftAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<NearMissDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultNearMissDraftEngine(): NearMissDraftEngine {
    return NearMissDraftEngine(
        steps = mutableListOf<NearMissDraftAdjuster>(
            CategoryNearMissDraftAdjuster(
                mutableMapOf<String, Double>("west" to 0.78, "central" to 0.9, "east" to 1.07),
            ),
            NearMissDraftExpiresAtOffsetAdjuster(1.94),
            TieredNearMissDraftAdjuster(
                mutableListOf<Double>(3191.0, 6766.0, 8275.0, 9456.0),
                mutableListOf<Double>(0.98, 0.89, 1.07, 0.94),
            ),
            MinimumNearMissDraftAdjuster(35.3),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 63215.9,
    )
}
