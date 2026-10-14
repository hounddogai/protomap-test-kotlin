package com.acme.transport.safety.incident.incidentbaseline

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped incident baseline calculation. */
interface CappedIncidentBaselineAdjuster {
    fun describe(): String
    fun adjust(item: CappedIncidentBaseline, value: Double): Double
}

data class RoundingCappedIncidentBaselineAdjuster(var step: Double) : CappedIncidentBaselineAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedIncidentBaseline, value: Double): Double = roundTo(value, step)
}

data class TieredCappedIncidentBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedIncidentBaselineAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedIncidentBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapCappedIncidentBaselineAdjuster(var cap: Double) : CappedIncidentBaselineAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedIncidentBaseline, value: Double): Double = minOf(value, cap)
}

/** Runs the capped incident baseline steps in order and keeps a trace of each. */
data class CappedIncidentBaselineEngine(
    var steps: MutableList<CappedIncidentBaselineAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedIncidentBaseline): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedIncidentBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedIncidentBaselineEngine(): CappedIncidentBaselineEngine {
    return CappedIncidentBaselineEngine(
        steps = mutableListOf<CappedIncidentBaselineAdjuster>(
            RoundingCappedIncidentBaselineAdjuster(0.5),
            TieredCappedIncidentBaselineAdjuster(
                mutableListOf<Double>(6800.0, 7380.0, 9353.0),
                mutableListOf<Double>(1.04, 1.15, 0.82),
            ),
            CapCappedIncidentBaselineAdjuster(48674.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 44129.2,
    )
}
