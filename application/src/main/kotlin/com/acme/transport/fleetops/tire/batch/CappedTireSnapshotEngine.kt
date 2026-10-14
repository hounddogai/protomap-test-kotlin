package com.acme.transport.fleetops.tire.batch

import com.acme.transport.kit.numbers.roundTo

/** One step of the capped tire snapshot calculation. */
interface CappedTireSnapshotStep {
    fun describe(): String
    fun adjust(item: CappedTireSnapshot, value: Double): Double
}

data class MinimumCappedTireSnapshotAdjuster(var minimum: Double) : CappedTireSnapshotStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedTireSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CountryCappedTireSnapshotAdjuster(var factors: MutableMap<String, Double>) : CappedTireSnapshotStep {
    override fun describe(): String = "by country"

    override fun adjust(item: CappedTireSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingCappedTireSnapshotAdjuster(var step: Double) : CappedTireSnapshotStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedTireSnapshot, value: Double): Double = roundTo(value, step)
}

data class CappedTireSnapshotPriorityOffsetAdjuster(var weight: Double) : CappedTireSnapshotStep {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: CappedTireSnapshot, value: Double): Double = value + item.priority.toDouble() * weight
}

data class CapCappedTireSnapshotAdjuster(var cap: Double) : CappedTireSnapshotStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedTireSnapshot, value: Double): Double = minOf(value, cap)
}

data class TieredCappedTireSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedTireSnapshotStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedTireSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the capped tire snapshot steps in order and keeps a trace of each. */
data class CappedTireSnapshotEngine(
    var steps: MutableList<CappedTireSnapshotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedTireSnapshot): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedTireSnapshotStep = steps[index]
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

    fun evaluateAll(items: MutableList<CappedTireSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedTireSnapshotEngine(): CappedTireSnapshotEngine {
    return CappedTireSnapshotEngine(
        steps = mutableListOf<CappedTireSnapshotStep>(
            MinimumCappedTireSnapshotAdjuster(20.0),
            CountryCappedTireSnapshotAdjuster(mutableMapOf<String, Double>("central" to 0.86, "south" to 0.85)),
            RoundingCappedTireSnapshotAdjuster(5.0),
            CappedTireSnapshotPriorityOffsetAdjuster(1.31),
            CapCappedTireSnapshotAdjuster(30409.0),
            TieredCappedTireSnapshotAdjuster(
                mutableListOf<Double>(1831.0, 3714.0, 4972.0, 5336.0),
                mutableListOf<Double>(1.14, 1.14, 1.16, 0.9),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 50586.8,
    )
}
