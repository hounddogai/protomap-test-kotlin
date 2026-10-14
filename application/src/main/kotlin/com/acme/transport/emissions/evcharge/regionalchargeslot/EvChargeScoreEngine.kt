package com.acme.transport.emissions.evcharge.regionalchargeslot

/** One step of the ev charge score calculation. */
interface EvChargeScoreAdjuster {
    fun describe(): String
    fun adjust(item: EvChargeScore, value: Double): Double
}

data class TieredEvChargeScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EvChargeScoreAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EvChargeScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumEvChargeScoreAdjuster(var minimum: Double) : EvChargeScoreAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EvChargeScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapEvChargeScoreAdjuster(var cap: Double) : EvChargeScoreAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EvChargeScore, value: Double): Double = minOf(value, cap)
}

/** Runs the ev charge score steps in order and keeps a trace of each. */
data class EvChargeScoreEngine(
    var steps: MutableList<EvChargeScoreAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: EvChargeScore): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: EvChargeScoreAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<EvChargeScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultEvChargeScoreEngine(): EvChargeScoreEngine {
    return EvChargeScoreEngine(
        steps = mutableListOf<EvChargeScoreAdjuster>(
            TieredEvChargeScoreAdjuster(
                mutableListOf<Double>(4011.0, 8682.0),
                mutableListOf<Double>(0.87, 0.83),
            ),
            MinimumEvChargeScoreAdjuster(0.9),
            CapEvChargeScoreAdjuster(21549.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 6035.5,
    )
}
