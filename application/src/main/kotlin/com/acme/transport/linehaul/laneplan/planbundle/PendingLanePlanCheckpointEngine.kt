package com.acme.transport.linehaul.laneplan.planbundle

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the pending lane plan checkpoint calculation. */
interface PendingLanePlanCheckpointFactor {
    fun describe(): String
    fun adjust(item: PendingLanePlanCheckpoint, value: Double): Double
}

data class MinimumPendingLanePlanCheckpointAdjuster(var minimum: Double) : PendingLanePlanCheckpointFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingLanePlanCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PendingLanePlanCheckpointLimitOffsetAdjuster(var weight: Double) : PendingLanePlanCheckpointFactor {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: PendingLanePlanCheckpoint, value: Double): Double {
        return value + item.limit.toDouble() * weight
    }
}

data class TieredPendingLanePlanCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingLanePlanCheckpointFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingLanePlanCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the pending lane plan checkpoint steps in order and keeps a trace of each. */
data class PendingLanePlanCheckpointEngine(
    var steps: MutableList<PendingLanePlanCheckpointFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingLanePlanCheckpoint): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingLanePlanCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingLanePlanCheckpointEngine(): PendingLanePlanCheckpointEngine {
    return PendingLanePlanCheckpointEngine(
        steps = mutableListOf<PendingLanePlanCheckpointFactor>(
            MinimumPendingLanePlanCheckpointAdjuster(33.3),
            PendingLanePlanCheckpointLimitOffsetAdjuster(0.72),
            TieredPendingLanePlanCheckpointAdjuster(
                mutableListOf<Double>(6021.0, 6247.0, 7599.0),
                mutableListOf<Double>(0.84, 0.93, 0.99),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 90423.9,
    )
}
