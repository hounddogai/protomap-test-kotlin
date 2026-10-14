package com.acme.transport.emissions.evcharge.agreement

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the ev charge segment calculation. */
interface EvChargeSegmentStep {
    fun describe(): String
    fun adjust(item: EvChargeSegment, value: Double): Double
}

data class MinimumEvChargeSegmentAdjuster(var minimum: Double) : EvChargeSegmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EvChargeSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameEvChargeSegmentAdjuster(var factors: MutableMap<String, Double>) : EvChargeSegmentStep {
    override fun describe(): String = "by name"

    override fun adjust(item: EvChargeSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class EvChargeSegmentUpdatedAtOffsetAdjuster(var weight: Double) : EvChargeSegmentStep {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: EvChargeSegment, value: Double): Double = value + item.updatedAt.toDouble() * weight
}

data class CapEvChargeSegmentAdjuster(var cap: Double) : EvChargeSegmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EvChargeSegment, value: Double): Double = minOf(value, cap)
}

data class TieredEvChargeSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EvChargeSegmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EvChargeSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingEvChargeSegmentAdjuster(var step: Double) : EvChargeSegmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EvChargeSegment, value: Double): Double = roundTo(value, step)
}

/** Runs the ev charge segment steps in order and keeps a trace of each. */
data class EvChargeSegmentEngine(
    var steps: MutableList<EvChargeSegmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: EvChargeSegment): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<EvChargeSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultEvChargeSegmentEngine(): EvChargeSegmentEngine {
    return EvChargeSegmentEngine(
        steps = mutableListOf<EvChargeSegmentStep>(
            MinimumEvChargeSegmentAdjuster(44.0),
            NameEvChargeSegmentAdjuster(
                mutableMapOf<String, Double>("central" to 1.19, "online" to 0.96, "north" to 1.11),
            ),
            EvChargeSegmentUpdatedAtOffsetAdjuster(0.14),
            CapEvChargeSegmentAdjuster(11109.0),
            TieredEvChargeSegmentAdjuster(
                mutableListOf<Double>(6372.0, 9692.0),
                mutableListOf<Double>(1.11, 1.17),
            ),
            RoundingEvChargeSegmentAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 53087.0,
    )
}
