package com.acme.transport.safety.violation.summary

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global violation batch calculation. */
interface GlobalViolationBatchStep {
    fun describe(): String
    fun adjust(item: GlobalViolationBatch, value: Double): Double
}

data class CountryGlobalViolationBatchAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalViolationBatchStep {
    override fun describe(): String = "by country"

    override fun adjust(item: GlobalViolationBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumGlobalViolationBatchAdjuster(var minimum: Double) : GlobalViolationBatchStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalViolationBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class GlobalViolationBatchEtaMinutesOffsetAdjuster(var weight: Double) : GlobalViolationBatchStep {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: GlobalViolationBatch, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

data class RoundingGlobalViolationBatchAdjuster(var step: Double) : GlobalViolationBatchStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalViolationBatch, value: Double): Double = roundTo(value, step)
}

data class TieredGlobalViolationBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalViolationBatchStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalViolationBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the global violation batch steps in order and keeps a trace of each. */
data class GlobalViolationBatchEngine(
    var steps: MutableList<GlobalViolationBatchStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalViolationBatch): Double {
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

    fun evaluateAll(items: MutableList<GlobalViolationBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalViolationBatchEngine(): GlobalViolationBatchEngine {
    return GlobalViolationBatchEngine(
        steps = mutableListOf<GlobalViolationBatchStep>(
            CountryGlobalViolationBatchAdjuster(
                mutableMapOf<String, Double>("south" to 0.72, "central" to 0.78),
            ),
            MinimumGlobalViolationBatchAdjuster(12.2),
            GlobalViolationBatchEtaMinutesOffsetAdjuster(1.45),
            RoundingGlobalViolationBatchAdjuster(1.0),
            TieredGlobalViolationBatchAdjuster(
                mutableListOf<Double>(3966.0, 4385.0),
                mutableListOf<Double>(0.8, 0.92),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 90937.6,
    )
}
