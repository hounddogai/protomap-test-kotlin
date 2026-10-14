package com.acme.transport.freightpay.accessorial.policy

import com.acme.transport.kit.numbers.roundTo

/** One step of the accessorial target calculation. */
interface AccessorialTargetModifier {
    fun describe(): String
    fun adjust(item: AccessorialTarget, value: Double): Double
}

data class CapAccessorialTargetAdjuster(var cap: Double) : AccessorialTargetModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AccessorialTarget, value: Double): Double = minOf(value, cap)
}

data class TieredAccessorialTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AccessorialTargetModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AccessorialTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingAccessorialTargetAdjuster(var step: Double) : AccessorialTargetModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AccessorialTarget, value: Double): Double = roundTo(value, step)
}

data class LabelAccessorialTargetAdjuster(var factors: MutableMap<String, Double>) : AccessorialTargetModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: AccessorialTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentAccessorialTargetAdjuster(var percent: Double) : AccessorialTargetModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AccessorialTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the accessorial target steps in order and keeps a trace of each. */
data class AccessorialTargetEngine(
    var steps: MutableList<AccessorialTargetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AccessorialTarget): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AccessorialTargetModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AccessorialTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAccessorialTargetEngine(): AccessorialTargetEngine {
    return AccessorialTargetEngine(
        steps = mutableListOf<AccessorialTargetModifier>(
            CapAccessorialTargetAdjuster(5960.0),
            TieredAccessorialTargetAdjuster(
                mutableListOf<Double>(6016.0, 7406.0, 7585.0),
                mutableListOf<Double>(0.82, 0.82, 0.82),
            ),
            RoundingAccessorialTargetAdjuster(0.05),
            LabelAccessorialTargetAdjuster(
                mutableMapOf<String, Double>("north" to 1.06, "retail" to 1.25, "south" to 1.0),
            ),
            PercentAccessorialTargetAdjuster(8.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 37666.7,
    )
}
