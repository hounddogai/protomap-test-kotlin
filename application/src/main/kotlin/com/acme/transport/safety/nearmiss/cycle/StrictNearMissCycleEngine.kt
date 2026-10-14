package com.acme.transport.safety.nearmiss.cycle

/** One step of the strict near miss cycle calculation. */
interface StrictNearMissCycleModifier {
    fun describe(): String
    fun adjust(item: StrictNearMissCycle, value: Double): Double
}

data class CapStrictNearMissCycleAdjuster(var cap: Double) : StrictNearMissCycleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictNearMissCycle, value: Double): Double = minOf(value, cap)
}

data class StrictNearMissCycleScoreOffsetAdjuster(var weight: Double) : StrictNearMissCycleModifier {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: StrictNearMissCycle, value: Double): Double = value + item.score * weight
}

data class TitleStrictNearMissCycleAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictNearMissCycleModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: StrictNearMissCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredStrictNearMissCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictNearMissCycleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictNearMissCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the strict near miss cycle steps in order and keeps a trace of each. */
data class StrictNearMissCycleEngine(
    var steps: MutableList<StrictNearMissCycleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictNearMissCycle): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictNearMissCycleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<StrictNearMissCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictNearMissCycleEngine(): StrictNearMissCycleEngine {
    return StrictNearMissCycleEngine(
        steps = mutableListOf<StrictNearMissCycleModifier>(
            CapStrictNearMissCycleAdjuster(34962.0),
            StrictNearMissCycleScoreOffsetAdjuster(0.77),
            TitleStrictNearMissCycleAdjuster(mutableMapOf<String, Double>("east" to 0.79, "west" to 0.91)),
            TieredStrictNearMissCycleAdjuster(
                mutableListOf<Double>(4252.0, 4791.0, 7196.0),
                mutableListOf<Double>(0.86, 0.85, 0.97),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 8083.2,
    )
}
