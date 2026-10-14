package com.acme.transport.linehaul.capacity.capacityrequest

/** One step of the lenient capacity request calculation. */
interface LenientCapacityRequestModifier {
    fun describe(): String
    fun adjust(item: LenientCapacityRequest, value: Double): Double
}

data class MinimumLenientCapacityRequestAdjuster(var minimum: Double) : LenientCapacityRequestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientCapacityRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitleLenientCapacityRequestAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientCapacityRequestModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: LenientCapacityRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredLenientCapacityRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientCapacityRequestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientCapacityRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the lenient capacity request steps in order and keeps a trace of each. */
data class LenientCapacityRequestEngine(
    var steps: MutableList<LenientCapacityRequestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientCapacityRequest): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientCapacityRequestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LenientCapacityRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientCapacityRequestEngine(): LenientCapacityRequestEngine {
    return LenientCapacityRequestEngine(
        steps = mutableListOf<LenientCapacityRequestModifier>(
            MinimumLenientCapacityRequestAdjuster(48.3),
            TitleLenientCapacityRequestAdjuster(
                mutableMapOf<String, Double>("retail" to 0.86, "east" to 0.78, "west" to 0.83),
            ),
            TieredLenientCapacityRequestAdjuster(
                mutableListOf<Double>(5484.0, 8991.0, 9252.0, 9766.0),
                mutableListOf<Double>(0.97, 0.84, 1.1, 1.01),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 39179.4,
    )
}
