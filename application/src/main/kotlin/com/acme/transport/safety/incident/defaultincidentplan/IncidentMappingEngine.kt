package com.acme.transport.safety.incident.defaultincidentplan

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the incident mapping calculation. */
interface IncidentMappingStep {
    fun describe(): String
    fun adjust(item: IncidentMapping, value: Double): Double
}

data class TieredIncidentMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IncidentMappingStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IncidentMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentIncidentMappingAdjuster(var percent: Double) : IncidentMappingStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IncidentMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class IncidentMappingBatchSizeOffsetAdjuster(var weight: Double) : IncidentMappingStep {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: IncidentMapping, value: Double): Double = value + item.batchSize.toDouble() * weight
}

/** Runs the incident mapping steps in order and keeps a trace of each. */
data class IncidentMappingEngine(
    var steps: MutableList<IncidentMappingStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: IncidentMapping): Double {
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

    fun evaluateAll(items: MutableList<IncidentMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultIncidentMappingEngine(): IncidentMappingEngine {
    return IncidentMappingEngine(
        steps = mutableListOf<IncidentMappingStep>(
            TieredIncidentMappingAdjuster(
                mutableListOf<Double>(4004.0, 9245.0),
                mutableListOf<Double>(1.01, 1.14),
            ),
            PercentIncidentMappingAdjuster(-24.9),
            IncidentMappingBatchSizeOffsetAdjuster(1.64),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 41021.9,
    )
}
