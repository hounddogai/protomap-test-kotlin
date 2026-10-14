package com.acme.transport.safety.scorecard.scorecardnotice

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scorecard group calculation. */
interface ScorecardGroupFactor {
    fun describe(): String
    fun adjust(item: ScorecardGroup, value: Double): Double
}

data class PercentScorecardGroupAdjuster(var percent: Double) : ScorecardGroupFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScorecardGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredScorecardGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScorecardGroupFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScorecardGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapScorecardGroupAdjuster(var cap: Double) : ScorecardGroupFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScorecardGroup, value: Double): Double = minOf(value, cap)
}

/** Runs the scorecard group steps in order and keeps a trace of each. */
data class ScorecardGroupEngine(
    var steps: MutableList<ScorecardGroupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScorecardGroup): Double {
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

    fun evaluateAll(items: MutableList<ScorecardGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScorecardGroupEngine(): ScorecardGroupEngine {
    return ScorecardGroupEngine(
        steps = mutableListOf<ScorecardGroupFactor>(
            PercentScorecardGroupAdjuster(11.0),
            TieredScorecardGroupAdjuster(
                mutableListOf<Double>(9239.0, 9861.0),
                mutableListOf<Double>(1.18, 0.86),
            ),
            CapScorecardGroupAdjuster(1460.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 86764.5,
    )
}
