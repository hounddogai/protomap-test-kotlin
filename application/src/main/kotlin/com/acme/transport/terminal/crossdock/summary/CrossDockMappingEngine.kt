package com.acme.transport.terminal.crossdock.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the cross dock mapping calculation. */
interface CrossDockMappingStep {
    fun describe(): String
    fun adjust(item: CrossDockMapping, value: Double): Double
}

data class TieredCrossDockMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CrossDockMappingStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CrossDockMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingCrossDockMappingAdjuster(var step: Double) : CrossDockMappingStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CrossDockMapping, value: Double): Double = roundTo(value, step)
}

data class CrossDockMappingDiscountOffsetAdjuster(var weight: Double) : CrossDockMappingStep {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: CrossDockMapping, value: Double): Double = value + item.discount * weight
}

data class FlaggedCrossDockMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CrossDockMappingStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CrossDockMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumCrossDockMappingAdjuster(var minimum: Double) : CrossDockMappingStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CrossDockMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapCrossDockMappingAdjuster(var cap: Double) : CrossDockMappingStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CrossDockMapping, value: Double): Double = minOf(value, cap)
}

/** Runs the cross dock mapping steps in order and keeps a trace of each. */
data class CrossDockMappingEngine(
    var steps: MutableList<CrossDockMappingStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CrossDockMapping): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CrossDockMappingStep = steps[index]
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

    fun evaluateAll(items: MutableList<CrossDockMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCrossDockMappingEngine(flags: Registry): CrossDockMappingEngine {
    return CrossDockMappingEngine(
        steps = mutableListOf<CrossDockMappingStep>(
            TieredCrossDockMappingAdjuster(
                mutableListOf<Double>(1932.0, 4436.0, 5785.0, 8492.0),
                mutableListOf<Double>(0.83, 1.05, 0.91, 1.14),
            ),
            RoundingCrossDockMappingAdjuster(0.1),
            CrossDockMappingDiscountOffsetAdjuster(0.86),
            FlaggedCrossDockMappingAdjuster(flags, "cross-dock-mapping-fallback", -6.5),
            MinimumCrossDockMappingAdjuster(7.8),
            CapCrossDockMappingAdjuster(26460.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 31487.1,
    )
}
