package com.acme.transport.safety.incident.incidentoverride

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capped incident tier calculation. */
interface CappedIncidentTierStep {
    fun describe(): String
    fun adjust(item: CappedIncidentTier, value: Double): Double
}

data class CappedIncidentTierBudgetOffsetAdjuster(var weight: Double) : CappedIncidentTierStep {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: CappedIncidentTier, value: Double): Double = value + item.budget * weight
}

data class PercentCappedIncidentTierAdjuster(var percent: Double) : CappedIncidentTierStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedIncidentTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredCappedIncidentTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedIncidentTierStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedIncidentTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the capped incident tier steps in order and keeps a trace of each. */
data class CappedIncidentTierEngine(
    var steps: MutableList<CappedIncidentTierStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedIncidentTier): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedIncidentTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedIncidentTierEngine(): CappedIncidentTierEngine {
    return CappedIncidentTierEngine(
        steps = mutableListOf<CappedIncidentTierStep>(
            CappedIncidentTierBudgetOffsetAdjuster(1.09),
            PercentCappedIncidentTierAdjuster(-0.6),
            TieredCappedIncidentTierAdjuster(
                mutableListOf<Double>(1577.0, 3649.0),
                mutableListOf<Double>(1.2, 1.02),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 78618.5,
    )
}
