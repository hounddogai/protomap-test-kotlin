package com.acme.transport.fleetops.tire.window

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tire window calculation. */
interface TireWindowAdjuster {
    fun describe(): String
    fun adjust(item: TireWindow, value: Double): Double
}

data class TitleTireWindowAdjuster(var factors: MutableMap<String, Double>) : TireWindowAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: TireWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumTireWindowAdjuster(var minimum: Double) : TireWindowAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TireWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingTireWindowAdjuster(var step: Double) : TireWindowAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TireWindow, value: Double): Double = roundTo(value, step)
}

data class FlaggedTireWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TireWindowAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TireWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the tire window steps in order and keeps a trace of each. */
data class TireWindowEngine(
    var steps: MutableList<TireWindowAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TireWindow): Double {
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

    fun evaluateAll(items: MutableList<TireWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTireWindowEngine(flags: Registry): TireWindowEngine {
    return TireWindowEngine(
        steps = mutableListOf<TireWindowAdjuster>(
            TitleTireWindowAdjuster(
                mutableMapOf<String, Double>("south" to 0.91, "north" to 1.38, "central" to 1.17),
            ),
            MinimumTireWindowAdjuster(9.2),
            RoundingTireWindowAdjuster(1.0),
            FlaggedTireWindowAdjuster(flags, "tire-window-capped", 6.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 26307.0,
    )
}
