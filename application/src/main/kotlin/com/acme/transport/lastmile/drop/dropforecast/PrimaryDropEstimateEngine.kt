package com.acme.transport.lastmile.drop.dropforecast

import com.acme.transport.kit.numbers.roundTo

/** One step of the primary drop estimate calculation. */
interface PrimaryDropEstimateAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryDropEstimate, value: Double): Double
}

data class RoundingPrimaryDropEstimateAdjuster(var step: Double) : PrimaryDropEstimateAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryDropEstimate, value: Double): Double = roundTo(value, step)
}

data class PercentPrimaryDropEstimateAdjuster(var percent: Double) : PrimaryDropEstimateAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryDropEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LocalePrimaryDropEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryDropEstimateAdjuster {
    override fun describe(): String = "by locale"

    override fun adjust(item: PrimaryDropEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PrimaryDropEstimateVersionOffsetAdjuster(var weight: Double) : PrimaryDropEstimateAdjuster {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: PrimaryDropEstimate, value: Double): Double = value + item.version.toDouble() * weight
}

/** Runs the primary drop estimate steps in order and keeps a trace of each. */
data class PrimaryDropEstimateEngine(
    var steps: MutableList<PrimaryDropEstimateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryDropEstimate): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryDropEstimateAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryDropEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryDropEstimateEngine(): PrimaryDropEstimateEngine {
    return PrimaryDropEstimateEngine(
        steps = mutableListOf<PrimaryDropEstimateAdjuster>(
            RoundingPrimaryDropEstimateAdjuster(0.05),
            PercentPrimaryDropEstimateAdjuster(-22.9),
            LocalePrimaryDropEstimateAdjuster(
                mutableMapOf<String, Double>("north" to 1.16, "east" to 0.93, "central" to 1.17),
            ),
            PrimaryDropEstimateVersionOffsetAdjuster(0.33),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 22504.5,
    )
}
