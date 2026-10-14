package com.acme.transport.fleetops.inspection.group

import com.acme.transport.kit.numbers.roundTo

/** One step of the inspection window calculation. */
interface InspectionWindowAdjuster {
    fun describe(): String
    fun adjust(item: InspectionWindow, value: Double): Double
}

data class TieredInspectionWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionWindowAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: InspectionWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingInspectionWindowAdjuster(var step: Double) : InspectionWindowAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: InspectionWindow, value: Double): Double = roundTo(value, step)
}

data class CapInspectionWindowAdjuster(var cap: Double) : InspectionWindowAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionWindow, value: Double): Double = minOf(value, cap)
}

data class PercentInspectionWindowAdjuster(var percent: Double) : InspectionWindowAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: InspectionWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the inspection window steps in order and keeps a trace of each. */
data class InspectionWindowEngine(
    var steps: MutableList<InspectionWindowAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: InspectionWindow): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: InspectionWindowAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<InspectionWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultInspectionWindowEngine(): InspectionWindowEngine {
    return InspectionWindowEngine(
        steps = mutableListOf<InspectionWindowAdjuster>(
            TieredInspectionWindowAdjuster(
                mutableListOf<Double>(902.0, 1583.0, 4813.0, 5431.0),
                mutableListOf<Double>(1.19, 0.95, 1.2, 0.93),
            ),
            RoundingInspectionWindowAdjuster(0.5),
            CapInspectionWindowAdjuster(26997.0),
            PercentInspectionWindowAdjuster(-20.6),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 7100.7,
    )
}
