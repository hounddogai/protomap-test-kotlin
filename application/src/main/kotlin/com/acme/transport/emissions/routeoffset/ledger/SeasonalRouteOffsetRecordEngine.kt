package com.acme.transport.emissions.routeoffset.ledger

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal route offset record calculation. */
interface SeasonalRouteOffsetRecordStep {
    fun describe(): String
    fun adjust(item: SeasonalRouteOffsetRecord, value: Double): Double
}

data class PercentSeasonalRouteOffsetRecordAdjuster(var percent: Double) : SeasonalRouteOffsetRecordStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalRouteOffsetRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumSeasonalRouteOffsetRecordAdjuster(var minimum: Double) : SeasonalRouteOffsetRecordStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalRouteOffsetRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredSeasonalRouteOffsetRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalRouteOffsetRecordStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalRouteOffsetRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingSeasonalRouteOffsetRecordAdjuster(var step: Double) : SeasonalRouteOffsetRecordStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalRouteOffsetRecord, value: Double): Double = roundTo(value, step)
}

/** Runs the seasonal route offset record steps in order and keeps a trace of each. */
data class SeasonalRouteOffsetRecordEngine(
    var steps: MutableList<SeasonalRouteOffsetRecordStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SeasonalRouteOffsetRecord): Double {
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

    fun evaluateAll(items: MutableList<SeasonalRouteOffsetRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSeasonalRouteOffsetRecordEngine(): SeasonalRouteOffsetRecordEngine {
    return SeasonalRouteOffsetRecordEngine(
        steps = mutableListOf<SeasonalRouteOffsetRecordStep>(
            PercentSeasonalRouteOffsetRecordAdjuster(23.1),
            MinimumSeasonalRouteOffsetRecordAdjuster(47.6),
            TieredSeasonalRouteOffsetRecordAdjuster(
                mutableListOf<Double>(690.0, 5750.0, 9349.0),
                mutableListOf<Double>(0.88, 1.03, 1.18),
            ),
            RoundingSeasonalRouteOffsetRecordAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 50422.1,
    )
}
