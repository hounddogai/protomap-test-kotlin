package com.acme.transport.safety.nearmiss.missestimate

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted near miss quota calculation. */
interface WeightedNearMissQuotaFactor {
    fun describe(): String
    fun adjust(item: WeightedNearMissQuota, value: Double): Double
}

data class WeightedNearMissQuotaPriorityOffsetAdjuster(var weight: Double) : WeightedNearMissQuotaFactor {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: WeightedNearMissQuota, value: Double): Double {
        return value + item.priority.toDouble() * weight
    }
}

data class PercentWeightedNearMissQuotaAdjuster(var percent: Double) : WeightedNearMissQuotaFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedNearMissQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingWeightedNearMissQuotaAdjuster(var step: Double) : WeightedNearMissQuotaFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedNearMissQuota, value: Double): Double = roundTo(value, step)
}

/** Runs the weighted near miss quota steps in order and keeps a trace of each. */
data class WeightedNearMissQuotaEngine(
    var steps: MutableList<WeightedNearMissQuotaFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedNearMissQuota): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedNearMissQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedNearMissQuotaEngine(): WeightedNearMissQuotaEngine {
    return WeightedNearMissQuotaEngine(
        steps = mutableListOf<WeightedNearMissQuotaFactor>(
            WeightedNearMissQuotaPriorityOffsetAdjuster(1.74),
            PercentWeightedNearMissQuotaAdjuster(-20.7),
            RoundingWeightedNearMissQuotaAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 41498.6,
    )
}
