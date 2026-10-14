package com.acme.transport.network.hub.batch

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the strict hub batch calculation. */
interface StrictHubBatchStep {
    fun describe(): String
    fun adjust(item: StrictHubBatch, value: Double): Double
}

data class RoundingStrictHubBatchAdjuster(var step: Double) : StrictHubBatchStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictHubBatch, value: Double): Double = roundTo(value, step)
}

data class UnitStrictHubBatchAdjuster(var factors: MutableMap<String, Double>) : StrictHubBatchStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: StrictHubBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumStrictHubBatchAdjuster(var minimum: Double) : StrictHubBatchStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictHubBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class StrictHubBatchAttemptsOffsetAdjuster(var weight: Double) : StrictHubBatchStep {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: StrictHubBatch, value: Double): Double = value + item.attempts.toDouble() * weight
}

data class PercentStrictHubBatchAdjuster(var percent: Double) : StrictHubBatchStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictHubBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredStrictHubBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictHubBatchStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictHubBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the strict hub batch steps in order and keeps a trace of each. */
data class StrictHubBatchEngine(
    var steps: MutableList<StrictHubBatchStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictHubBatch): Double {
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

    fun evaluateAll(items: MutableList<StrictHubBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictHubBatchEngine(): StrictHubBatchEngine {
    return StrictHubBatchEngine(
        steps = mutableListOf<StrictHubBatchStep>(
            RoundingStrictHubBatchAdjuster(0.1),
            UnitStrictHubBatchAdjuster(mutableMapOf<String, Double>("south" to 1.19, "north" to 1.2)),
            MinimumStrictHubBatchAdjuster(43.6),
            StrictHubBatchAttemptsOffsetAdjuster(0.15),
            PercentStrictHubBatchAdjuster(-16.7),
            TieredStrictHubBatchAdjuster(
                mutableListOf<Double>(919.0, 2719.0),
                mutableListOf<Double>(1.11, 0.92),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 75965.9,
    )
}
