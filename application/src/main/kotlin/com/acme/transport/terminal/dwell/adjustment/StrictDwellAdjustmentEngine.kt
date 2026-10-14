package com.acme.transport.terminal.dwell.adjustment

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the strict dwell adjustment calculation. */
interface StrictDwellAdjustmentFactor {
    fun describe(): String
    fun adjust(item: StrictDwellAdjustment, value: Double): Double
}

data class TieredStrictDwellAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictDwellAdjustmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictDwellAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapStrictDwellAdjustmentAdjuster(var cap: Double) : StrictDwellAdjustmentFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictDwellAdjustment, value: Double): Double = minOf(value, cap)
}

data class RoundingStrictDwellAdjustmentAdjuster(var step: Double) : StrictDwellAdjustmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictDwellAdjustment, value: Double): Double = roundTo(value, step)
}

/** Runs the strict dwell adjustment steps in order and keeps a trace of each. */
data class StrictDwellAdjustmentEngine(
    var steps: MutableList<StrictDwellAdjustmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictDwellAdjustment): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictDwellAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictDwellAdjustmentEngine(): StrictDwellAdjustmentEngine {
    return StrictDwellAdjustmentEngine(
        steps = mutableListOf<StrictDwellAdjustmentFactor>(
            TieredStrictDwellAdjustmentAdjuster(
                mutableListOf<Double>(696.0, 2051.0, 3551.0),
                mutableListOf<Double>(1.06, 1.07, 1.06),
            ),
            CapStrictDwellAdjustmentAdjuster(37984.0),
            RoundingStrictDwellAdjustmentAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 51278.0,
    )
}
