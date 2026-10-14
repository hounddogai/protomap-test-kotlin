package com.acme.transport.network.cutoff.regionalcutoffsegment

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending cutoff segment calculation. */
interface PendingCutoffSegmentModifier {
    fun describe(): String
    fun adjust(item: PendingCutoffSegment, value: Double): Double
}

data class PendingCutoffSegmentBudgetOffsetAdjuster(var weight: Double) : PendingCutoffSegmentModifier {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: PendingCutoffSegment, value: Double): Double = value + item.budget * weight
}

data class LocalePendingCutoffSegmentAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingCutoffSegmentModifier {
    override fun describe(): String = "by locale"

    override fun adjust(item: PendingCutoffSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumPendingCutoffSegmentAdjuster(var minimum: Double) : PendingCutoffSegmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingCutoffSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingPendingCutoffSegmentAdjuster(var step: Double) : PendingCutoffSegmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingCutoffSegment, value: Double): Double = roundTo(value, step)
}

data class TieredPendingCutoffSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingCutoffSegmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingCutoffSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapPendingCutoffSegmentAdjuster(var cap: Double) : PendingCutoffSegmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingCutoffSegment, value: Double): Double = minOf(value, cap)
}

/** Runs the pending cutoff segment steps in order and keeps a trace of each. */
data class PendingCutoffSegmentEngine(
    var steps: MutableList<PendingCutoffSegmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingCutoffSegment): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingCutoffSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingCutoffSegmentEngine(): PendingCutoffSegmentEngine {
    return PendingCutoffSegmentEngine(
        steps = mutableListOf<PendingCutoffSegmentModifier>(
            PendingCutoffSegmentBudgetOffsetAdjuster(1.26),
            LocalePendingCutoffSegmentAdjuster(mutableMapOf<String, Double>("online" to 1.16)),
            MinimumPendingCutoffSegmentAdjuster(15.5),
            RoundingPendingCutoffSegmentAdjuster(1.0),
            TieredPendingCutoffSegmentAdjuster(
                mutableListOf<Double>(7282.0, 7502.0, 7732.0, 8294.0),
                mutableListOf<Double>(0.96, 0.89, 0.83, 1.18),
            ),
            CapPendingCutoffSegmentAdjuster(4492.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 74379.6,
    )
}
