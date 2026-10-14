package com.acme.transport.terminal.crossdock.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the cross dock exception calculation. */
interface CrossDockExceptionStep {
    fun describe(): String
    fun adjust(item: CrossDockException, value: Double): Double
}

data class RoundingCrossDockExceptionAdjuster(var step: Double) : CrossDockExceptionStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CrossDockException, value: Double): Double = roundTo(value, step)
}

data class TieredCrossDockExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CrossDockExceptionStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CrossDockException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapCrossDockExceptionAdjuster(var cap: Double) : CrossDockExceptionStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CrossDockException, value: Double): Double = minOf(value, cap)
}

data class PercentCrossDockExceptionAdjuster(var percent: Double) : CrossDockExceptionStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CrossDockException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedCrossDockExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CrossDockExceptionStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CrossDockException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CrossDockExceptionStopCountOffsetAdjuster(var weight: Double) : CrossDockExceptionStep {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: CrossDockException, value: Double): Double = value + item.stopCount.toDouble() * weight
}

/** Runs the cross dock exception steps in order and keeps a trace of each. */
data class CrossDockExceptionEngine(
    var steps: MutableList<CrossDockExceptionStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CrossDockException): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CrossDockExceptionStep = steps[index]
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

    fun evaluateAll(items: MutableList<CrossDockException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCrossDockExceptionEngine(flags: Registry): CrossDockExceptionEngine {
    return CrossDockExceptionEngine(
        steps = mutableListOf<CrossDockExceptionStep>(
            RoundingCrossDockExceptionAdjuster(0.1),
            TieredCrossDockExceptionAdjuster(
                mutableListOf<Double>(818.0, 5139.0),
                mutableListOf<Double>(1.02, 0.88),
            ),
            CapCrossDockExceptionAdjuster(36642.0),
            PercentCrossDockExceptionAdjuster(-21.8),
            FlaggedCrossDockExceptionAdjuster(flags, "cross-dock-exception-scheduled", 5.9),
            CrossDockExceptionStopCountOffsetAdjuster(1.07),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 84274.2,
    )
}
