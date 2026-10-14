package com.acme.transport.linehaul.backhaul.adjustment

/** One step of the primary backhaul estimate calculation. */
interface PrimaryBackhaulEstimateStep {
    fun describe(): String
    fun adjust(item: PrimaryBackhaulEstimate, value: Double): Double
}

data class PrimaryBackhaulEstimateThresholdOffsetAdjuster(var weight: Double) : PrimaryBackhaulEstimateStep {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: PrimaryBackhaulEstimate, value: Double): Double = value + item.threshold * weight
}

data class TieredPrimaryBackhaulEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryBackhaulEstimateStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryBackhaulEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumPrimaryBackhaulEstimateAdjuster(var minimum: Double) : PrimaryBackhaulEstimateStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryBackhaulEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentPrimaryBackhaulEstimateAdjuster(var percent: Double) : PrimaryBackhaulEstimateStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryBackhaulEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the primary backhaul estimate steps in order and keeps a trace of each. */
data class PrimaryBackhaulEstimateEngine(
    var steps: MutableList<PrimaryBackhaulEstimateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryBackhaulEstimate): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryBackhaulEstimateStep = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryBackhaulEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryBackhaulEstimateEngine(): PrimaryBackhaulEstimateEngine {
    return PrimaryBackhaulEstimateEngine(
        steps = mutableListOf<PrimaryBackhaulEstimateStep>(
            PrimaryBackhaulEstimateThresholdOffsetAdjuster(0.53),
            TieredPrimaryBackhaulEstimateAdjuster(
                mutableListOf<Double>(1974.0, 2389.0, 2739.0, 9317.0),
                mutableListOf<Double>(0.81, 1.01, 0.93, 1.07),
            ),
            MinimumPrimaryBackhaulEstimateAdjuster(46.0),
            PercentPrimaryBackhaulEstimateAdjuster(3.6),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 37554.0,
    )
}
