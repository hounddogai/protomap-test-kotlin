package com.acme.transport.emissions.evcharge.adjustment

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal ev charge adjustment calculation. */
interface SeasonalEvChargeAdjustmentFactor {
    fun describe(): String
    fun adjust(item: SeasonalEvChargeAdjustment, value: Double): Double
}

data class CapSeasonalEvChargeAdjustmentAdjuster(var cap: Double) : SeasonalEvChargeAdjustmentFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalEvChargeAdjustment, value: Double): Double = minOf(value, cap)
}

data class TieredSeasonalEvChargeAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalEvChargeAdjustmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalEvChargeAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NameSeasonalEvChargeAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalEvChargeAdjustmentFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: SeasonalEvChargeAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingSeasonalEvChargeAdjustmentAdjuster(var step: Double) : SeasonalEvChargeAdjustmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalEvChargeAdjustment, value: Double): Double = roundTo(value, step)
}

/** Runs the seasonal ev charge adjustment steps in order and keeps a trace of each. */
data class SeasonalEvChargeAdjustmentEngine(
    var steps: MutableList<SeasonalEvChargeAdjustmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SeasonalEvChargeAdjustment): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalEvChargeAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSeasonalEvChargeAdjustmentEngine(): SeasonalEvChargeAdjustmentEngine {
    return SeasonalEvChargeAdjustmentEngine(
        steps = mutableListOf<SeasonalEvChargeAdjustmentFactor>(
            CapSeasonalEvChargeAdjustmentAdjuster(34525.0),
            TieredSeasonalEvChargeAdjustmentAdjuster(
                mutableListOf<Double>(228.0, 1325.0, 6881.0),
                mutableListOf<Double>(0.87, 1.16, 1.14),
            ),
            NameSeasonalEvChargeAdjustmentAdjuster(
                mutableMapOf<String, Double>("retail" to 1.13, "central" to 1.11, "south" to 1.02),
            ),
            RoundingSeasonalEvChargeAdjustmentAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 70622.6,
    )
}
