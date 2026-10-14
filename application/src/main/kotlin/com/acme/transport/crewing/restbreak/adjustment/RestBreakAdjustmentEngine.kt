package com.acme.transport.crewing.restbreak.adjustment

import com.acme.transport.kit.numbers.roundTo

/** One step of the rest break adjustment calculation. */
interface RestBreakAdjustmentModifier {
    fun describe(): String
    fun adjust(item: RestBreakAdjustment, value: Double): Double
}

data class PercentRestBreakAdjustmentAdjuster(var percent: Double) : RestBreakAdjustmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RestBreakAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LabelRestBreakAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : RestBreakAdjustmentModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: RestBreakAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RestBreakAdjustmentWeightOffsetAdjuster(var weight: Double) : RestBreakAdjustmentModifier {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: RestBreakAdjustment, value: Double): Double = value + item.weight * weight
}

data class RoundingRestBreakAdjustmentAdjuster(var step: Double) : RestBreakAdjustmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RestBreakAdjustment, value: Double): Double = roundTo(value, step)
}

/** Runs the rest break adjustment steps in order and keeps a trace of each. */
data class RestBreakAdjustmentEngine(
    var steps: MutableList<RestBreakAdjustmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RestBreakAdjustment): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RestBreakAdjustmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RestBreakAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRestBreakAdjustmentEngine(): RestBreakAdjustmentEngine {
    return RestBreakAdjustmentEngine(
        steps = mutableListOf<RestBreakAdjustmentModifier>(
            PercentRestBreakAdjustmentAdjuster(17.1),
            LabelRestBreakAdjustmentAdjuster(
                mutableMapOf<String, Double>("online" to 1.15, "north" to 1.4, "west" to 1.36),
            ),
            RestBreakAdjustmentWeightOffsetAdjuster(0.89),
            RoundingRestBreakAdjustmentAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 85429.1,
    )
}
