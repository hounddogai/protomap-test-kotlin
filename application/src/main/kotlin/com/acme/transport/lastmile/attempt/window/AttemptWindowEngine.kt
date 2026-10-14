package com.acme.transport.lastmile.attempt.window

import com.acme.transport.kit.numbers.roundTo

/** One step of the attempt window calculation. */
interface AttemptWindowModifier {
    fun describe(): String
    fun adjust(item: AttemptWindow, value: Double): Double
}

data class TieredAttemptWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AttemptWindowModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AttemptWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SegmentAttemptWindowAdjuster(var factors: MutableMap<String, Double>) : AttemptWindowModifier {
    override fun describe(): String = "by segment"

    override fun adjust(item: AttemptWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapAttemptWindowAdjuster(var cap: Double) : AttemptWindowModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AttemptWindow, value: Double): Double = minOf(value, cap)
}

data class PercentAttemptWindowAdjuster(var percent: Double) : AttemptWindowModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AttemptWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingAttemptWindowAdjuster(var step: Double) : AttemptWindowModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AttemptWindow, value: Double): Double = roundTo(value, step)
}

data class AttemptWindowAmountOffsetAdjuster(var weight: Double) : AttemptWindowModifier {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: AttemptWindow, value: Double): Double = value + item.amount * weight
}

/** Runs the attempt window steps in order and keeps a trace of each. */
data class AttemptWindowEngine(
    var steps: MutableList<AttemptWindowModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AttemptWindow): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AttemptWindowModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AttemptWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAttemptWindowEngine(): AttemptWindowEngine {
    return AttemptWindowEngine(
        steps = mutableListOf<AttemptWindowModifier>(
            TieredAttemptWindowAdjuster(
                mutableListOf<Double>(6108.0, 6347.0, 8209.0, 8809.0),
                mutableListOf<Double>(0.94, 1.0, 1.1, 0.89),
            ),
            SegmentAttemptWindowAdjuster(mutableMapOf<String, Double>("north" to 0.72, "central" to 1.28)),
            CapAttemptWindowAdjuster(18067.0),
            PercentAttemptWindowAdjuster(19.9),
            RoundingAttemptWindowAdjuster(0.05),
            AttemptWindowAmountOffsetAdjuster(0.73),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 12397.2,
    )
}
