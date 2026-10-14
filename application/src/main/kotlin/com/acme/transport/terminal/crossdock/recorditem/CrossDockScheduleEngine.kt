package com.acme.transport.terminal.crossdock.recorditem

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the cross dock schedule calculation. */
interface CrossDockScheduleModifier {
    fun describe(): String
    fun adjust(item: CrossDockSchedule, value: Double): Double
}

data class CapCrossDockScheduleAdjuster(var cap: Double) : CrossDockScheduleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CrossDockSchedule, value: Double): Double = minOf(value, cap)
}

data class CrossDockScheduleRatioOffsetAdjuster(var weight: Double) : CrossDockScheduleModifier {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: CrossDockSchedule, value: Double): Double = value + item.ratio * weight
}

data class PercentCrossDockScheduleAdjuster(var percent: Double) : CrossDockScheduleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CrossDockSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredCrossDockScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CrossDockScheduleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CrossDockSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the cross dock schedule steps in order and keeps a trace of each. */
data class CrossDockScheduleEngine(
    var steps: MutableList<CrossDockScheduleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CrossDockSchedule): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CrossDockSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCrossDockScheduleEngine(): CrossDockScheduleEngine {
    return CrossDockScheduleEngine(
        steps = mutableListOf<CrossDockScheduleModifier>(
            CapCrossDockScheduleAdjuster(19078.0),
            CrossDockScheduleRatioOffsetAdjuster(1.25),
            PercentCrossDockScheduleAdjuster(6.5),
            TieredCrossDockScheduleAdjuster(
                mutableListOf<Double>(1654.0, 2684.0, 6964.0),
                mutableListOf<Double>(1.09, 1.15, 1.2),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 86239.3,
    )
}
