package com.acme.transport.network.node.adjustment

import com.acme.transport.kit.flags.Registry

/** One step of the strict node estimate calculation. */
interface StrictNodeEstimateStep {
    fun describe(): String
    fun adjust(item: StrictNodeEstimate, value: Double): Double
}

data class TitleStrictNodeEstimateAdjuster(var factors: MutableMap<String, Double>) : StrictNodeEstimateStep {
    override fun describe(): String = "by title"

    override fun adjust(item: StrictNodeEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedStrictNodeEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictNodeEstimateStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictNodeEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredStrictNodeEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictNodeEstimateStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictNodeEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class StrictNodeEstimateEtaMinutesOffsetAdjuster(var weight: Double) : StrictNodeEstimateStep {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: StrictNodeEstimate, value: Double): Double = value + item.etaMinutes.toDouble() * weight
}

/** Runs the strict node estimate steps in order and keeps a trace of each. */
data class StrictNodeEstimateEngine(
    var steps: MutableList<StrictNodeEstimateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: StrictNodeEstimate): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictNodeEstimateStep = steps[index]
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

    fun evaluateAll(items: MutableList<StrictNodeEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultStrictNodeEstimateEngine(flags: Registry): StrictNodeEstimateEngine {
    return StrictNodeEstimateEngine(
        steps = mutableListOf<StrictNodeEstimateStep>(
            TitleStrictNodeEstimateAdjuster(
                mutableMapOf<String, Double>("west" to 1.25, "online" to 0.76, "east" to 1.21),
            ),
            FlaggedStrictNodeEstimateAdjuster(flags, "strict-node-estimate-legacy", 1.1),
            TieredStrictNodeEstimateAdjuster(
                mutableListOf<Double>(3089.0, 3266.0, 9560.0),
                mutableListOf<Double>(0.91, 0.82, 0.94),
            ),
            StrictNodeEstimateEtaMinutesOffsetAdjuster(1.71),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 31219.7,
    )
}
