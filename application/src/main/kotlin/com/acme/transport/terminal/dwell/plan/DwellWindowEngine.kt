package com.acme.transport.terminal.dwell.plan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the dwell window calculation. */
interface DwellWindowModifier {
    fun describe(): String
    fun adjust(item: DwellWindow, value: Double): Double
}

data class CapDwellWindowAdjuster(var cap: Double) : DwellWindowModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DwellWindow, value: Double): Double = minOf(value, cap)
}

data class PercentDwellWindowAdjuster(var percent: Double) : DwellWindowModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DwellWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingDwellWindowAdjuster(var step: Double) : DwellWindowModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DwellWindow, value: Double): Double = roundTo(value, step)
}

data class FlaggedDwellWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DwellWindowModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DwellWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class UnitDwellWindowAdjuster(var factors: MutableMap<String, Double>) : DwellWindowModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: DwellWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumDwellWindowAdjuster(var minimum: Double) : DwellWindowModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DwellWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the dwell window steps in order and keeps a trace of each. */
data class DwellWindowEngine(
    var steps: MutableList<DwellWindowModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DwellWindow): Double {
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

    fun evaluateAll(items: MutableList<DwellWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDwellWindowEngine(flags: Registry): DwellWindowEngine {
    return DwellWindowEngine(
        steps = mutableListOf<DwellWindowModifier>(
            CapDwellWindowAdjuster(32114.0),
            PercentDwellWindowAdjuster(-10.1),
            RoundingDwellWindowAdjuster(0.5),
            FlaggedDwellWindowAdjuster(flags, "dwell-window-legacy", 2.6),
            UnitDwellWindowAdjuster(mutableMapOf<String, Double>("west" to 1.24)),
            MinimumDwellWindowAdjuster(14.8),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 28325.7,
    )
}
