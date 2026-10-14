package com.acme.transport.freightpay.accessorial.target

import com.acme.transport.kit.numbers.roundTo

/** One step of the global accessorial target calculation. */
interface GlobalAccessorialTargetModifier {
    fun describe(): String
    fun adjust(item: GlobalAccessorialTarget, value: Double): Double
}

data class PercentGlobalAccessorialTargetAdjuster(var percent: Double) : GlobalAccessorialTargetModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalAccessorialTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingGlobalAccessorialTargetAdjuster(var step: Double) : GlobalAccessorialTargetModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalAccessorialTarget, value: Double): Double = roundTo(value, step)
}

data class SourceGlobalAccessorialTargetAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalAccessorialTargetModifier {
    override fun describe(): String = "by source"

    override fun adjust(item: GlobalAccessorialTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapGlobalAccessorialTargetAdjuster(var cap: Double) : GlobalAccessorialTargetModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalAccessorialTarget, value: Double): Double = minOf(value, cap)
}

data class MinimumGlobalAccessorialTargetAdjuster(var minimum: Double) : GlobalAccessorialTargetModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalAccessorialTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the global accessorial target steps in order and keeps a trace of each. */
data class GlobalAccessorialTargetEngine(
    var steps: MutableList<GlobalAccessorialTargetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalAccessorialTarget): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalAccessorialTargetModifier = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalAccessorialTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalAccessorialTargetEngine(): GlobalAccessorialTargetEngine {
    return GlobalAccessorialTargetEngine(
        steps = mutableListOf<GlobalAccessorialTargetModifier>(
            PercentGlobalAccessorialTargetAdjuster(-24.0),
            RoundingGlobalAccessorialTargetAdjuster(0.25),
            SourceGlobalAccessorialTargetAdjuster(
                mutableMapOf<String, Double>("east" to 1.17, "retail" to 0.88),
            ),
            CapGlobalAccessorialTargetAdjuster(46337.0),
            MinimumGlobalAccessorialTargetAdjuster(39.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 95649.6,
    )
}
