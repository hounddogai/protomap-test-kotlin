package com.acme.transport.fleetops.tire.profile

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tire score calculation. */
interface TireScoreStep {
    fun describe(): String
    fun adjust(item: TireScore, value: Double): Double
}

data class CapTireScoreAdjuster(var cap: Double) : TireScoreStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TireScore, value: Double): Double = minOf(value, cap)
}

data class PercentTireScoreAdjuster(var percent: Double) : TireScoreStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TireScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TireScoreBudgetOffsetAdjuster(var weight: Double) : TireScoreStep {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: TireScore, value: Double): Double = value + item.budget * weight
}

/** Runs the tire score steps in order and keeps a trace of each. */
data class TireScoreEngine(
    var steps: MutableList<TireScoreStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TireScore): Double {
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

    fun evaluateAll(items: MutableList<TireScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTireScoreEngine(): TireScoreEngine {
    return TireScoreEngine(
        steps = mutableListOf<TireScoreStep>(
            CapTireScoreAdjuster(47294.0),
            PercentTireScoreAdjuster(21.0),
            TireScoreBudgetOffsetAdjuster(0.39),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 74448.7,
    )
}
