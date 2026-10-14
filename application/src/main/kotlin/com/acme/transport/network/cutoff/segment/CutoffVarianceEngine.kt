package com.acme.transport.network.cutoff.segment

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the cutoff variance calculation. */
interface CutoffVarianceStep {
    fun describe(): String
    fun adjust(item: CutoffVariance, value: Double): Double
}

data class PercentCutoffVarianceAdjuster(var percent: Double) : CutoffVarianceStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CutoffVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredCutoffVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CutoffVarianceStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CutoffVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumCutoffVarianceAdjuster(var minimum: Double) : CutoffVarianceStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CutoffVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapCutoffVarianceAdjuster(var cap: Double) : CutoffVarianceStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CutoffVariance, value: Double): Double = minOf(value, cap)
}

data class RoundingCutoffVarianceAdjuster(var step: Double) : CutoffVarianceStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CutoffVariance, value: Double): Double = roundTo(value, step)
}

/** Runs the cutoff variance steps in order and keeps a trace of each. */
data class CutoffVarianceEngine(
    var steps: MutableList<CutoffVarianceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CutoffVariance): Double {
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

    fun evaluateAll(items: MutableList<CutoffVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCutoffVarianceEngine(): CutoffVarianceEngine {
    return CutoffVarianceEngine(
        steps = mutableListOf<CutoffVarianceStep>(
            PercentCutoffVarianceAdjuster(19.7),
            TieredCutoffVarianceAdjuster(
                mutableListOf<Double>(2099.0, 7314.0, 8575.0),
                mutableListOf<Double>(0.97, 0.9, 0.95),
            ),
            MinimumCutoffVarianceAdjuster(19.6),
            CapCutoffVarianceAdjuster(22133.0),
            RoundingCutoffVarianceAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 31644.0,
    )
}
