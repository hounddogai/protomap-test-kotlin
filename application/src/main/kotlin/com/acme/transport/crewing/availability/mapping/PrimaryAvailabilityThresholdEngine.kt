package com.acme.transport.crewing.availability.mapping

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the primary availability threshold calculation. */
interface PrimaryAvailabilityThresholdAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryAvailabilityThreshold, value: Double): Double
}

data class CapPrimaryAvailabilityThresholdAdjuster(var cap: Double) : PrimaryAvailabilityThresholdAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryAvailabilityThreshold, value: Double): Double = minOf(value, cap)
}

data class LabelPrimaryAvailabilityThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryAvailabilityThresholdAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: PrimaryAvailabilityThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentPrimaryAvailabilityThresholdAdjuster(
    var percent: Double,
) : PrimaryAvailabilityThresholdAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryAvailabilityThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class PrimaryAvailabilityThresholdLoadKgOffsetAdjuster(
    var weight: Double,
) : PrimaryAvailabilityThresholdAdjuster {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: PrimaryAvailabilityThreshold, value: Double): Double = value + item.loadKg * weight
}

/** Runs the primary availability threshold steps in order and keeps a trace of each. */
data class PrimaryAvailabilityThresholdEngine(
    var steps: MutableList<PrimaryAvailabilityThresholdAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryAvailabilityThreshold): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryAvailabilityThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryAvailabilityThresholdEngine(): PrimaryAvailabilityThresholdEngine {
    return PrimaryAvailabilityThresholdEngine(
        steps = mutableListOf<PrimaryAvailabilityThresholdAdjuster>(
            CapPrimaryAvailabilityThresholdAdjuster(15130.0),
            LabelPrimaryAvailabilityThresholdAdjuster(mutableMapOf<String, Double>("retail" to 0.88)),
            PercentPrimaryAvailabilityThresholdAdjuster(-23.8),
            PrimaryAvailabilityThresholdLoadKgOffsetAdjuster(0.39),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 13223.5,
    )
}
