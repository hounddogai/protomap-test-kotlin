package com.acme.transport.emissions.idling.idlingcheckpoint

import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback idling checkpoint calculation. */
interface FallbackIdlingCheckpointStep {
    fun describe(): String
    fun adjust(item: FallbackIdlingCheckpoint, value: Double): Double
}

data class RegionFallbackIdlingCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackIdlingCheckpointStep {
    override fun describe(): String = "by region"

    override fun adjust(item: FallbackIdlingCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FallbackIdlingCheckpointFloorValueOffsetAdjuster(var weight: Double) : FallbackIdlingCheckpointStep {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: FallbackIdlingCheckpoint, value: Double): Double = value + item.floorValue * weight
}

data class CapFallbackIdlingCheckpointAdjuster(var cap: Double) : FallbackIdlingCheckpointStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackIdlingCheckpoint, value: Double): Double = minOf(value, cap)
}

data class PercentFallbackIdlingCheckpointAdjuster(var percent: Double) : FallbackIdlingCheckpointStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackIdlingCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingFallbackIdlingCheckpointAdjuster(var step: Double) : FallbackIdlingCheckpointStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackIdlingCheckpoint, value: Double): Double = roundTo(value, step)
}

/** Runs the fallback idling checkpoint steps in order and keeps a trace of each. */
data class FallbackIdlingCheckpointEngine(
    var steps: MutableList<FallbackIdlingCheckpointStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackIdlingCheckpoint): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackIdlingCheckpointStep = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackIdlingCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackIdlingCheckpointEngine(): FallbackIdlingCheckpointEngine {
    return FallbackIdlingCheckpointEngine(
        steps = mutableListOf<FallbackIdlingCheckpointStep>(
            RegionFallbackIdlingCheckpointAdjuster(mutableMapOf<String, Double>("central" to 1.22)),
            FallbackIdlingCheckpointFloorValueOffsetAdjuster(0.19),
            CapFallbackIdlingCheckpointAdjuster(47286.0),
            PercentFallbackIdlingCheckpointAdjuster(-19.5),
            RoundingFallbackIdlingCheckpointAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 76891.4,
    )
}
