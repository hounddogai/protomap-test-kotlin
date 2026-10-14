package com.acme.transport.terminal.traileryard.budget

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual trailer yard schedule calculation. */
interface ManualTrailerYardScheduleFactor {
    fun describe(): String
    fun adjust(item: ManualTrailerYardSchedule, value: Double): Double
}

data class RoundingManualTrailerYardScheduleAdjuster(var step: Double) : ManualTrailerYardScheduleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualTrailerYardSchedule, value: Double): Double = roundTo(value, step)
}

data class CapManualTrailerYardScheduleAdjuster(var cap: Double) : ManualTrailerYardScheduleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualTrailerYardSchedule, value: Double): Double = minOf(value, cap)
}

data class LabelManualTrailerYardScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualTrailerYardScheduleFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: ManualTrailerYardSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ManualTrailerYardScheduleRateOffsetAdjuster(var weight: Double) : ManualTrailerYardScheduleFactor {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: ManualTrailerYardSchedule, value: Double): Double = value + item.rate * weight
}

data class PercentManualTrailerYardScheduleAdjuster(var percent: Double) : ManualTrailerYardScheduleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualTrailerYardSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the manual trailer yard schedule steps in order and keeps a trace of each. */
data class ManualTrailerYardScheduleEngine(
    var steps: MutableList<ManualTrailerYardScheduleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualTrailerYardSchedule): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualTrailerYardSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualTrailerYardScheduleEngine(): ManualTrailerYardScheduleEngine {
    return ManualTrailerYardScheduleEngine(
        steps = mutableListOf<ManualTrailerYardScheduleFactor>(
            RoundingManualTrailerYardScheduleAdjuster(0.5),
            CapManualTrailerYardScheduleAdjuster(41909.0),
            LabelManualTrailerYardScheduleAdjuster(
                mutableMapOf<String, Double>("online" to 1.35, "south" to 1.29, "north" to 0.98),
            ),
            ManualTrailerYardScheduleRateOffsetAdjuster(1.13),
            PercentManualTrailerYardScheduleAdjuster(-9.3),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 79589.7,
    )
}
