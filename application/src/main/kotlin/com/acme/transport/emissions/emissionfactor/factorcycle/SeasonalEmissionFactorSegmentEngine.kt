package com.acme.transport.emissions.emissionfactor.factorcycle

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal emission factor segment calculation. */
interface SeasonalEmissionFactorSegmentAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalEmissionFactorSegment, value: Double): Double
}

data class NameSeasonalEmissionFactorSegmentAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalEmissionFactorSegmentAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: SeasonalEmissionFactorSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapSeasonalEmissionFactorSegmentAdjuster(var cap: Double) : SeasonalEmissionFactorSegmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalEmissionFactorSegment, value: Double): Double = minOf(value, cap)
}

data class SeasonalEmissionFactorSegmentQuantityOffsetAdjuster(
    var weight: Double,
) : SeasonalEmissionFactorSegmentAdjuster {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: SeasonalEmissionFactorSegment, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

data class RoundingSeasonalEmissionFactorSegmentAdjuster(
    var step: Double,
) : SeasonalEmissionFactorSegmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalEmissionFactorSegment, value: Double): Double = roundTo(value, step)
}

data class PercentSeasonalEmissionFactorSegmentAdjuster(
    var percent: Double,
) : SeasonalEmissionFactorSegmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalEmissionFactorSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the seasonal emission factor segment steps in order and keeps a trace of each. */
data class SeasonalEmissionFactorSegmentEngine(
    var steps: MutableList<SeasonalEmissionFactorSegmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalEmissionFactorSegment): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalEmissionFactorSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalEmissionFactorSegmentEngine(): SeasonalEmissionFactorSegmentEngine {
    return SeasonalEmissionFactorSegmentEngine(
        steps = mutableListOf<SeasonalEmissionFactorSegmentAdjuster>(
            NameSeasonalEmissionFactorSegmentAdjuster(mutableMapOf<String, Double>("central" to 1.15)),
            CapSeasonalEmissionFactorSegmentAdjuster(29489.0),
            SeasonalEmissionFactorSegmentQuantityOffsetAdjuster(0.21),
            RoundingSeasonalEmissionFactorSegmentAdjuster(1.0),
            PercentSeasonalEmissionFactorSegmentAdjuster(16.3),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 75723.0,
    )
}
