package com.acme.transport.terminal.crossdock.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the cross dock override calculation. */
interface CrossDockOverrideModifier {
    fun describe(): String
    fun adjust(item: CrossDockOverride, value: Double): Double
}

data class RoundingCrossDockOverrideAdjuster(var step: Double) : CrossDockOverrideModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CrossDockOverride, value: Double): Double = roundTo(value, step)
}

data class CrossDockOverrideAmountOffsetAdjuster(var weight: Double) : CrossDockOverrideModifier {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: CrossDockOverride, value: Double): Double = value + item.amount * weight
}

data class CapCrossDockOverrideAdjuster(var cap: Double) : CrossDockOverrideModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CrossDockOverride, value: Double): Double = minOf(value, cap)
}

data class FlaggedCrossDockOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CrossDockOverrideModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CrossDockOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumCrossDockOverrideAdjuster(var minimum: Double) : CrossDockOverrideModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CrossDockOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the cross dock override steps in order and keeps a trace of each. */
data class CrossDockOverrideEngine(
    var steps: MutableList<CrossDockOverrideModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CrossDockOverride): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CrossDockOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCrossDockOverrideEngine(flags: Registry): CrossDockOverrideEngine {
    return CrossDockOverrideEngine(
        steps = mutableListOf<CrossDockOverrideModifier>(
            RoundingCrossDockOverrideAdjuster(5.0),
            CrossDockOverrideAmountOffsetAdjuster(0.08),
            CapCrossDockOverrideAdjuster(42215.0),
            FlaggedCrossDockOverrideAdjuster(flags, "cross-dock-override-capped", -1.3),
            MinimumCrossDockOverrideAdjuster(9.9),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 67748.7,
    )
}
