package com.acme.transport.safety.incident.target

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped incident target calculation. */
interface CappedIncidentTargetFactor {
    fun describe(): String
    fun adjust(item: CappedIncidentTarget, value: Double): Double
}

data class CapCappedIncidentTargetAdjuster(var cap: Double) : CappedIncidentTargetFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedIncidentTarget, value: Double): Double = minOf(value, cap)
}

data class CappedIncidentTargetLoadKgOffsetAdjuster(var weight: Double) : CappedIncidentTargetFactor {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: CappedIncidentTarget, value: Double): Double = value + item.loadKg * weight
}

data class RoundingCappedIncidentTargetAdjuster(var step: Double) : CappedIncidentTargetFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedIncidentTarget, value: Double): Double = roundTo(value, step)
}

data class MinimumCappedIncidentTargetAdjuster(var minimum: Double) : CappedIncidentTargetFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedIncidentTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentCappedIncidentTargetAdjuster(var percent: Double) : CappedIncidentTargetFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedIncidentTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class DepotCappedIncidentTargetAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedIncidentTargetFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: CappedIncidentTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the capped incident target steps in order and keeps a trace of each. */
data class CappedIncidentTargetEngine(
    var steps: MutableList<CappedIncidentTargetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CappedIncidentTarget): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedIncidentTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCappedIncidentTargetEngine(): CappedIncidentTargetEngine {
    return CappedIncidentTargetEngine(
        steps = mutableListOf<CappedIncidentTargetFactor>(
            CapCappedIncidentTargetAdjuster(23124.0),
            CappedIncidentTargetLoadKgOffsetAdjuster(1.46),
            RoundingCappedIncidentTargetAdjuster(0.25),
            MinimumCappedIncidentTargetAdjuster(6.8),
            PercentCappedIncidentTargetAdjuster(9.3),
            DepotCappedIncidentTargetAdjuster(
                mutableMapOf<String, Double>("west" to 1.22, "north" to 1.29, "south" to 1.16),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 14954.7,
    )
}
