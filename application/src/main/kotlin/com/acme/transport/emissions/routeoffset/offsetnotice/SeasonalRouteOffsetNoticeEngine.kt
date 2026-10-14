package com.acme.transport.emissions.routeoffset.offsetnotice

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal route offset notice calculation. */
interface SeasonalRouteOffsetNoticeModifier {
    fun describe(): String
    fun adjust(item: SeasonalRouteOffsetNotice, value: Double): Double
}

data class TieredSeasonalRouteOffsetNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalRouteOffsetNoticeModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalRouteOffsetNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentSeasonalRouteOffsetNoticeAdjuster(var percent: Double) : SeasonalRouteOffsetNoticeModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalRouteOffsetNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingSeasonalRouteOffsetNoticeAdjuster(var step: Double) : SeasonalRouteOffsetNoticeModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalRouteOffsetNotice, value: Double): Double = roundTo(value, step)
}

/** Runs the seasonal route offset notice steps in order and keeps a trace of each. */
data class SeasonalRouteOffsetNoticeEngine(
    var steps: MutableList<SeasonalRouteOffsetNoticeModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalRouteOffsetNotice): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalRouteOffsetNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalRouteOffsetNoticeEngine(): SeasonalRouteOffsetNoticeEngine {
    return SeasonalRouteOffsetNoticeEngine(
        steps = mutableListOf<SeasonalRouteOffsetNoticeModifier>(
            TieredSeasonalRouteOffsetNoticeAdjuster(
                mutableListOf<Double>(2673.0, 7016.0, 7639.0),
                mutableListOf<Double>(1.07, 1.01, 1.09),
            ),
            PercentSeasonalRouteOffsetNoticeAdjuster(-18.4),
            RoundingSeasonalRouteOffsetNoticeAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 1544.5,
    )
}
