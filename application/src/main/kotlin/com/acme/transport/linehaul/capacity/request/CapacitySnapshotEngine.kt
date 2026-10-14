package com.acme.transport.linehaul.capacity.request

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capacity snapshot calculation. */
interface CapacitySnapshotStep {
    fun describe(): String
    fun adjust(item: CapacitySnapshot, value: Double): Double
}

data class CapCapacitySnapshotAdjuster(var cap: Double) : CapacitySnapshotStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CapacitySnapshot, value: Double): Double = minOf(value, cap)
}

data class LabelCapacitySnapshotAdjuster(var factors: MutableMap<String, Double>) : CapacitySnapshotStep {
    override fun describe(): String = "by label"

    override fun adjust(item: CapacitySnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentCapacitySnapshotAdjuster(var percent: Double) : CapacitySnapshotStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CapacitySnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the capacity snapshot steps in order and keeps a trace of each. */
data class CapacitySnapshotEngine(
    var steps: MutableList<CapacitySnapshotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CapacitySnapshot): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CapacitySnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCapacitySnapshotEngine(): CapacitySnapshotEngine {
    return CapacitySnapshotEngine(
        steps = mutableListOf<CapacitySnapshotStep>(
            CapCapacitySnapshotAdjuster(27954.0),
            LabelCapacitySnapshotAdjuster(mutableMapOf<String, Double>("south" to 0.96, "east" to 0.97)),
            PercentCapacitySnapshotAdjuster(7.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 39826.6,
    )
}
