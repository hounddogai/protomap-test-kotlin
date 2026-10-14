package com.acme.transport.freightpay.ratecard.threshold

import com.acme.transport.kit.numbers.roundTo

/** One step of the strict rate card threshold calculation. */
interface StrictRateCardThresholdFactor {
    fun describe(): String
    fun adjust(item: StrictRateCardThreshold, value: Double): Double
}

data class PercentStrictRateCardThresholdAdjuster(var percent: Double) : StrictRateCardThresholdFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictRateCardThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class DepotStrictRateCardThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictRateCardThresholdFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: StrictRateCardThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapStrictRateCardThresholdAdjuster(var cap: Double) : StrictRateCardThresholdFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictRateCardThreshold, value: Double): Double = minOf(value, cap)
}

data class StrictRateCardThresholdLoadKgOffsetAdjuster(var weight: Double) : StrictRateCardThresholdFactor {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: StrictRateCardThreshold, value: Double): Double = value + item.loadKg * weight
}

data class RoundingStrictRateCardThresholdAdjuster(var step: Double) : StrictRateCardThresholdFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictRateCardThreshold, value: Double): Double = roundTo(value, step)
}

/** Runs the strict rate card threshold steps in order and keeps a trace of each. */
data class StrictRateCardThresholdEngine(
    var steps: MutableList<StrictRateCardThresholdFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictRateCardThreshold): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictRateCardThresholdFactor = steps[index]
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

    fun evaluateAll(items: MutableList<StrictRateCardThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictRateCardThresholdEngine(): StrictRateCardThresholdEngine {
    return StrictRateCardThresholdEngine(
        steps = mutableListOf<StrictRateCardThresholdFactor>(
            PercentStrictRateCardThresholdAdjuster(-3.0),
            DepotStrictRateCardThresholdAdjuster(mutableMapOf<String, Double>("west" to 0.88)),
            CapStrictRateCardThresholdAdjuster(3783.0),
            StrictRateCardThresholdLoadKgOffsetAdjuster(0.71),
            RoundingStrictRateCardThresholdAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 72215.5,
    )
}
