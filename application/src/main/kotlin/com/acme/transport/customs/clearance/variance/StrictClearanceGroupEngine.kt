package com.acme.transport.customs.clearance.variance

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the strict clearance group calculation. */
interface StrictClearanceGroupFactor {
    fun describe(): String
    fun adjust(item: StrictClearanceGroup, value: Double): Double
}

data class CapStrictClearanceGroupAdjuster(var cap: Double) : StrictClearanceGroupFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictClearanceGroup, value: Double): Double = minOf(value, cap)
}

data class StrictClearanceGroupEtaMinutesOffsetAdjuster(var weight: Double) : StrictClearanceGroupFactor {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: StrictClearanceGroup, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

data class RoundingStrictClearanceGroupAdjuster(var step: Double) : StrictClearanceGroupFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictClearanceGroup, value: Double): Double = roundTo(value, step)
}

data class PercentStrictClearanceGroupAdjuster(var percent: Double) : StrictClearanceGroupFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictClearanceGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the strict clearance group steps in order and keeps a trace of each. */
data class StrictClearanceGroupEngine(
    var steps: MutableList<StrictClearanceGroupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictClearanceGroup): Double {
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

    fun evaluateAll(items: MutableList<StrictClearanceGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictClearanceGroupEngine(): StrictClearanceGroupEngine {
    return StrictClearanceGroupEngine(
        steps = mutableListOf<StrictClearanceGroupFactor>(
            CapStrictClearanceGroupAdjuster(38775.0),
            StrictClearanceGroupEtaMinutesOffsetAdjuster(1.83),
            RoundingStrictClearanceGroupAdjuster(0.25),
            PercentStrictClearanceGroupAdjuster(17.6),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 80084.9,
    )
}
