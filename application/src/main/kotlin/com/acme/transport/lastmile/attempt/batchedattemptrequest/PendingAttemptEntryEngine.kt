package com.acme.transport.lastmile.attempt.batchedattemptrequest

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending attempt entry calculation. */
interface PendingAttemptEntryFactor {
    fun describe(): String
    fun adjust(item: PendingAttemptEntry, value: Double): Double
}

data class RoundingPendingAttemptEntryAdjuster(var step: Double) : PendingAttemptEntryFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingAttemptEntry, value: Double): Double = roundTo(value, step)
}

data class MinimumPendingAttemptEntryAdjuster(var minimum: Double) : PendingAttemptEntryFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingAttemptEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PendingAttemptEntryBudgetOffsetAdjuster(var weight: Double) : PendingAttemptEntryFactor {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: PendingAttemptEntry, value: Double): Double = value + item.budget * weight
}

data class CapPendingAttemptEntryAdjuster(var cap: Double) : PendingAttemptEntryFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingAttemptEntry, value: Double): Double = minOf(value, cap)
}

data class TieredPendingAttemptEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingAttemptEntryFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingAttemptEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the pending attempt entry steps in order and keeps a trace of each. */
data class PendingAttemptEntryEngine(
    var steps: MutableList<PendingAttemptEntryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingAttemptEntry): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingAttemptEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingAttemptEntryEngine(): PendingAttemptEntryEngine {
    return PendingAttemptEntryEngine(
        steps = mutableListOf<PendingAttemptEntryFactor>(
            RoundingPendingAttemptEntryAdjuster(0.1),
            MinimumPendingAttemptEntryAdjuster(16.7),
            PendingAttemptEntryBudgetOffsetAdjuster(1.69),
            CapPendingAttemptEntryAdjuster(39038.0),
            TieredPendingAttemptEntryAdjuster(
                mutableListOf<Double>(5814.0, 6975.0, 9378.0),
                mutableListOf<Double>(1.15, 1.06, 1.02),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 63702.1,
    )
}
