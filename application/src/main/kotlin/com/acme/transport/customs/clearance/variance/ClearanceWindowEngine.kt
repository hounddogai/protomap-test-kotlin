package com.acme.transport.customs.clearance.variance

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the clearance window calculation. */
interface ClearanceWindowAdjuster {
    fun describe(): String
    fun adjust(item: ClearanceWindow, value: Double): Double
}

data class TieredClearanceWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ClearanceWindowAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ClearanceWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingClearanceWindowAdjuster(var step: Double) : ClearanceWindowAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ClearanceWindow, value: Double): Double = roundTo(value, step)
}

data class LabelClearanceWindowAdjuster(var factors: MutableMap<String, Double>) : ClearanceWindowAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: ClearanceWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentClearanceWindowAdjuster(var percent: Double) : ClearanceWindowAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ClearanceWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapClearanceWindowAdjuster(var cap: Double) : ClearanceWindowAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ClearanceWindow, value: Double): Double = minOf(value, cap)
}

/** Runs the clearance window steps in order and keeps a trace of each. */
data class ClearanceWindowEngine(
    var steps: MutableList<ClearanceWindowAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ClearanceWindow): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ClearanceWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultClearanceWindowEngine(): ClearanceWindowEngine {
    return ClearanceWindowEngine(
        steps = mutableListOf<ClearanceWindowAdjuster>(
            TieredClearanceWindowAdjuster(
                mutableListOf<Double>(3083.0, 3216.0, 8174.0),
                mutableListOf<Double>(0.8, 0.96, 0.96),
            ),
            RoundingClearanceWindowAdjuster(0.25),
            LabelClearanceWindowAdjuster(
                mutableMapOf<String, Double>("retail" to 1.28, "east" to 1.17, "west" to 0.96),
            ),
            PercentClearanceWindowAdjuster(0.8),
            CapClearanceWindowAdjuster(40614.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 60110.6,
    )
}
