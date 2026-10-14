package com.acme.transport.lastmile.attempt.claim

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the attempt entry calculation. */
interface AttemptEntryAdjuster {
    fun describe(): String
    fun adjust(item: AttemptEntry, value: Double): Double
}

data class MinimumAttemptEntryAdjuster(var minimum: Double) : AttemptEntryAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AttemptEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredAttemptEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AttemptEntryAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AttemptEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentAttemptEntryAdjuster(var percent: Double) : AttemptEntryAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AttemptEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class UnitAttemptEntryAdjuster(var factors: MutableMap<String, Double>) : AttemptEntryAdjuster {
    override fun describe(): String = "by unit"

    override fun adjust(item: AttemptEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the attempt entry steps in order and keeps a trace of each. */
data class AttemptEntryEngine(
    var steps: MutableList<AttemptEntryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AttemptEntry): Double {
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

    fun evaluateAll(items: MutableList<AttemptEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAttemptEntryEngine(): AttemptEntryEngine {
    return AttemptEntryEngine(
        steps = mutableListOf<AttemptEntryAdjuster>(
            MinimumAttemptEntryAdjuster(35.8),
            TieredAttemptEntryAdjuster(
                mutableListOf<Double>(207.0, 6199.0, 6920.0),
                mutableListOf<Double>(1.18, 1.13, 0.81),
            ),
            PercentAttemptEntryAdjuster(6.2),
            UnitAttemptEntryAdjuster(mutableMapOf<String, Double>("south" to 1.03)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 30427.7,
    )
}
