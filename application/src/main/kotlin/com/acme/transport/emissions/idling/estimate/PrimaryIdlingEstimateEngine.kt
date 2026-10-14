package com.acme.transport.emissions.idling.estimate

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the primary idling estimate calculation. */
interface PrimaryIdlingEstimateAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryIdlingEstimate, value: Double): Double
}

data class PrimaryIdlingEstimateAmountOffsetAdjuster(var weight: Double) : PrimaryIdlingEstimateAdjuster {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: PrimaryIdlingEstimate, value: Double): Double = value + item.amount * weight
}

data class LabelPrimaryIdlingEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryIdlingEstimateAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: PrimaryIdlingEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentPrimaryIdlingEstimateAdjuster(var percent: Double) : PrimaryIdlingEstimateAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryIdlingEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapPrimaryIdlingEstimateAdjuster(var cap: Double) : PrimaryIdlingEstimateAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryIdlingEstimate, value: Double): Double = minOf(value, cap)
}

data class MinimumPrimaryIdlingEstimateAdjuster(var minimum: Double) : PrimaryIdlingEstimateAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryIdlingEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingPrimaryIdlingEstimateAdjuster(var step: Double) : PrimaryIdlingEstimateAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryIdlingEstimate, value: Double): Double = roundTo(value, step)
}

/** Runs the primary idling estimate steps in order and keeps a trace of each. */
data class PrimaryIdlingEstimateEngine(
    var steps: MutableList<PrimaryIdlingEstimateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimaryIdlingEstimate): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryIdlingEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimaryIdlingEstimateEngine(): PrimaryIdlingEstimateEngine {
    return PrimaryIdlingEstimateEngine(
        steps = mutableListOf<PrimaryIdlingEstimateAdjuster>(
            PrimaryIdlingEstimateAmountOffsetAdjuster(1.94),
            LabelPrimaryIdlingEstimateAdjuster(mutableMapOf<String, Double>("retail" to 0.98)),
            PercentPrimaryIdlingEstimateAdjuster(11.7),
            CapPrimaryIdlingEstimateAdjuster(7091.0),
            MinimumPrimaryIdlingEstimateAdjuster(49.2),
            RoundingPrimaryIdlingEstimateAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 67027.4,
    )
}
