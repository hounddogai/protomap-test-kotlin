package com.acme.transport.linehaul.backhaul.threshold

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the backhaul snapshot calculation. */
interface BackhaulSnapshotModifier {
    fun describe(): String
    fun adjust(item: BackhaulSnapshot, value: Double): Double
}

data class RoundingBackhaulSnapshotAdjuster(var step: Double) : BackhaulSnapshotModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BackhaulSnapshot, value: Double): Double = roundTo(value, step)
}

data class CountryBackhaulSnapshotAdjuster(var factors: MutableMap<String, Double>) : BackhaulSnapshotModifier {
    override fun describe(): String = "by country"

    override fun adjust(item: BackhaulSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredBackhaulSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BackhaulSnapshotModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BackhaulSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapBackhaulSnapshotAdjuster(var cap: Double) : BackhaulSnapshotModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BackhaulSnapshot, value: Double): Double = minOf(value, cap)
}

/** Runs the backhaul snapshot steps in order and keeps a trace of each. */
data class BackhaulSnapshotEngine(
    var steps: MutableList<BackhaulSnapshotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BackhaulSnapshot): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BackhaulSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBackhaulSnapshotEngine(): BackhaulSnapshotEngine {
    return BackhaulSnapshotEngine(
        steps = mutableListOf<BackhaulSnapshotModifier>(
            RoundingBackhaulSnapshotAdjuster(1.0),
            CountryBackhaulSnapshotAdjuster(
                mutableMapOf<String, Double>("west" to 0.75, "north" to 1.03, "online" to 1.17),
            ),
            TieredBackhaulSnapshotAdjuster(
                mutableListOf<Double>(2208.0, 2789.0, 9058.0),
                mutableListOf<Double>(1.03, 1.0, 0.92),
            ),
            CapBackhaulSnapshotAdjuster(20114.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 54398.4,
    )
}
