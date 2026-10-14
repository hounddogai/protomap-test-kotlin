package com.acme.transport.terminal.traileryard.tier

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted trailer yard snapshot calculation. */
interface WeightedTrailerYardSnapshotModifier {
    fun describe(): String
    fun adjust(item: WeightedTrailerYardSnapshot, value: Double): Double
}

data class TieredWeightedTrailerYardSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedTrailerYardSnapshotModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedTrailerYardSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class UnitWeightedTrailerYardSnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedTrailerYardSnapshotModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: WeightedTrailerYardSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingWeightedTrailerYardSnapshotAdjuster(var step: Double) : WeightedTrailerYardSnapshotModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedTrailerYardSnapshot, value: Double): Double = roundTo(value, step)
}

/** Runs the weighted trailer yard snapshot steps in order and keeps a trace of each. */
data class WeightedTrailerYardSnapshotEngine(
    var steps: MutableList<WeightedTrailerYardSnapshotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: WeightedTrailerYardSnapshot): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedTrailerYardSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultWeightedTrailerYardSnapshotEngine(): WeightedTrailerYardSnapshotEngine {
    return WeightedTrailerYardSnapshotEngine(
        steps = mutableListOf<WeightedTrailerYardSnapshotModifier>(
            TieredWeightedTrailerYardSnapshotAdjuster(
                mutableListOf<Double>(4030.0, 5555.0),
                mutableListOf<Double>(0.98, 1.19),
            ),
            UnitWeightedTrailerYardSnapshotAdjuster(
                mutableMapOf<String, Double>("east" to 1.37, "south" to 0.73, "online" to 1.26),
            ),
            RoundingWeightedTrailerYardSnapshotAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 45774.1,
    )
}
