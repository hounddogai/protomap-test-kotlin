package com.acme.transport.terminal.traileryard.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling trailer yard window calculation. */
interface RollingTrailerYardWindowModifier {
    fun describe(): String
    fun adjust(item: RollingTrailerYardWindow, value: Double): Double
}

data class RoundingRollingTrailerYardWindowAdjuster(var step: Double) : RollingTrailerYardWindowModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingTrailerYardWindow, value: Double): Double = roundTo(value, step)
}

data class FlaggedRollingTrailerYardWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingTrailerYardWindowModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingTrailerYardWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RollingTrailerYardWindowBudgetOffsetAdjuster(var weight: Double) : RollingTrailerYardWindowModifier {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: RollingTrailerYardWindow, value: Double): Double = value + item.budget * weight
}

/** Runs the rolling trailer yard window steps in order and keeps a trace of each. */
data class RollingTrailerYardWindowEngine(
    var steps: MutableList<RollingTrailerYardWindowModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingTrailerYardWindow): Double {
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

    fun evaluateAll(items: MutableList<RollingTrailerYardWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingTrailerYardWindowEngine(flags: Registry): RollingTrailerYardWindowEngine {
    return RollingTrailerYardWindowEngine(
        steps = mutableListOf<RollingTrailerYardWindowModifier>(
            RoundingRollingTrailerYardWindowAdjuster(0.1),
            FlaggedRollingTrailerYardWindowAdjuster(flags, "rolling-trailer-yard-window-fallback", 3.0),
            RollingTrailerYardWindowBudgetOffsetAdjuster(0.22),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 21494.2,
    )
}
