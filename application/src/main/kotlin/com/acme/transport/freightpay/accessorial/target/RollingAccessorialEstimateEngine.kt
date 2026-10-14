package com.acme.transport.freightpay.accessorial.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling accessorial estimate calculation. */
interface RollingAccessorialEstimateModifier {
    fun describe(): String
    fun adjust(item: RollingAccessorialEstimate, value: Double): Double
}

data class UnitRollingAccessorialEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingAccessorialEstimateModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: RollingAccessorialEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentRollingAccessorialEstimateAdjuster(var percent: Double) : RollingAccessorialEstimateModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingAccessorialEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapRollingAccessorialEstimateAdjuster(var cap: Double) : RollingAccessorialEstimateModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingAccessorialEstimate, value: Double): Double = minOf(value, cap)
}

data class RoundingRollingAccessorialEstimateAdjuster(var step: Double) : RollingAccessorialEstimateModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingAccessorialEstimate, value: Double): Double = roundTo(value, step)
}

data class FlaggedRollingAccessorialEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingAccessorialEstimateModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingAccessorialEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the rolling accessorial estimate steps in order and keeps a trace of each. */
data class RollingAccessorialEstimateEngine(
    var steps: MutableList<RollingAccessorialEstimateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingAccessorialEstimate): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingAccessorialEstimateModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RollingAccessorialEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingAccessorialEstimateEngine(flags: Registry): RollingAccessorialEstimateEngine {
    return RollingAccessorialEstimateEngine(
        steps = mutableListOf<RollingAccessorialEstimateModifier>(
            UnitRollingAccessorialEstimateAdjuster(
                mutableMapOf<String, Double>("online" to 1.28, "south" to 0.84),
            ),
            PercentRollingAccessorialEstimateAdjuster(18.5),
            CapRollingAccessorialEstimateAdjuster(36692.0),
            RoundingRollingAccessorialEstimateAdjuster(0.5),
            FlaggedRollingAccessorialEstimateAdjuster(flags, "rolling-accessorial-estimate-primary", 9.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 25501.7,
    )
}
