package com.acme.transport.linehaul.trailer.snapshot

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the trailer snapshot calculation. */
interface TrailerSnapshotAdjuster {
    fun describe(): String
    fun adjust(item: TrailerSnapshot, value: Double): Double
}

data class TrailerSnapshotScoreOffsetAdjuster(var weight: Double) : TrailerSnapshotAdjuster {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: TrailerSnapshot, value: Double): Double = value + item.score * weight
}

data class TieredTrailerSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TrailerSnapshotAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TrailerSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingTrailerSnapshotAdjuster(var step: Double) : TrailerSnapshotAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TrailerSnapshot, value: Double): Double = roundTo(value, step)
}

data class MinimumTrailerSnapshotAdjuster(var minimum: Double) : TrailerSnapshotAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TrailerSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class UnitTrailerSnapshotAdjuster(var factors: MutableMap<String, Double>) : TrailerSnapshotAdjuster {
    override fun describe(): String = "by unit"

    override fun adjust(item: TrailerSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the trailer snapshot steps in order and keeps a trace of each. */
data class TrailerSnapshotEngine(
    var steps: MutableList<TrailerSnapshotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TrailerSnapshot): Double {
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

    fun evaluateAll(items: MutableList<TrailerSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTrailerSnapshotEngine(): TrailerSnapshotEngine {
    return TrailerSnapshotEngine(
        steps = mutableListOf<TrailerSnapshotAdjuster>(
            TrailerSnapshotScoreOffsetAdjuster(0.49),
            TieredTrailerSnapshotAdjuster(
                mutableListOf<Double>(1154.0, 3170.0, 7545.0, 8870.0),
                mutableListOf<Double>(0.99, 1.16, 0.83, 0.81),
            ),
            RoundingTrailerSnapshotAdjuster(5.0),
            MinimumTrailerSnapshotAdjuster(37.5),
            UnitTrailerSnapshotAdjuster(mutableMapOf<String, Double>("east" to 0.81)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 89977.8,
    )
}
