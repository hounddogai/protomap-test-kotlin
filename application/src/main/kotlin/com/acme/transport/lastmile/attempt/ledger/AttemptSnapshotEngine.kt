package com.acme.transport.lastmile.attempt.ledger

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the attempt snapshot calculation. */
interface AttemptSnapshotModifier {
    fun describe(): String
    fun adjust(item: AttemptSnapshot, value: Double): Double
}

data class PercentAttemptSnapshotAdjuster(var percent: Double) : AttemptSnapshotModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AttemptSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumAttemptSnapshotAdjuster(var minimum: Double) : AttemptSnapshotModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AttemptSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredAttemptSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AttemptSnapshotModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AttemptSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RegionAttemptSnapshotAdjuster(var factors: MutableMap<String, Double>) : AttemptSnapshotModifier {
    override fun describe(): String = "by region"

    override fun adjust(item: AttemptSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the attempt snapshot steps in order and keeps a trace of each. */
data class AttemptSnapshotEngine(
    var steps: MutableList<AttemptSnapshotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AttemptSnapshot): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AttemptSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAttemptSnapshotEngine(): AttemptSnapshotEngine {
    return AttemptSnapshotEngine(
        steps = mutableListOf<AttemptSnapshotModifier>(
            PercentAttemptSnapshotAdjuster(-4.9),
            MinimumAttemptSnapshotAdjuster(32.9),
            TieredAttemptSnapshotAdjuster(
                mutableListOf<Double>(5662.0, 8560.0, 8696.0),
                mutableListOf<Double>(0.87, 0.93, 0.96),
            ),
            RegionAttemptSnapshotAdjuster(mutableMapOf<String, Double>("online" to 0.95, "north" to 1.0)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 50082.5,
    )
}
