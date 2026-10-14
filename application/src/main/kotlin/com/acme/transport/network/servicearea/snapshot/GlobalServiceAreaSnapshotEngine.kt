package com.acme.transport.network.servicearea.snapshot

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global service area snapshot calculation. */
interface GlobalServiceAreaSnapshotStep {
    fun describe(): String
    fun adjust(item: GlobalServiceAreaSnapshot, value: Double): Double
}

data class PercentGlobalServiceAreaSnapshotAdjuster(var percent: Double) : GlobalServiceAreaSnapshotStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalServiceAreaSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LaneGlobalServiceAreaSnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalServiceAreaSnapshotStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: GlobalServiceAreaSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumGlobalServiceAreaSnapshotAdjuster(var minimum: Double) : GlobalServiceAreaSnapshotStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalServiceAreaSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class GlobalServiceAreaSnapshotMaximumOffsetAdjuster(var weight: Double) : GlobalServiceAreaSnapshotStep {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: GlobalServiceAreaSnapshot, value: Double): Double = value + item.maximum * weight
}

data class CapGlobalServiceAreaSnapshotAdjuster(var cap: Double) : GlobalServiceAreaSnapshotStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalServiceAreaSnapshot, value: Double): Double = minOf(value, cap)
}

data class RoundingGlobalServiceAreaSnapshotAdjuster(var step: Double) : GlobalServiceAreaSnapshotStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalServiceAreaSnapshot, value: Double): Double = roundTo(value, step)
}

/** Runs the global service area snapshot steps in order and keeps a trace of each. */
data class GlobalServiceAreaSnapshotEngine(
    var steps: MutableList<GlobalServiceAreaSnapshotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalServiceAreaSnapshot): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalServiceAreaSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalServiceAreaSnapshotEngine(): GlobalServiceAreaSnapshotEngine {
    return GlobalServiceAreaSnapshotEngine(
        steps = mutableListOf<GlobalServiceAreaSnapshotStep>(
            PercentGlobalServiceAreaSnapshotAdjuster(-15.6),
            LaneGlobalServiceAreaSnapshotAdjuster(
                mutableMapOf<String, Double>("online" to 1.28, "west" to 1.38),
            ),
            MinimumGlobalServiceAreaSnapshotAdjuster(38.6),
            GlobalServiceAreaSnapshotMaximumOffsetAdjuster(0.66),
            CapGlobalServiceAreaSnapshotAdjuster(13419.0),
            RoundingGlobalServiceAreaSnapshotAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 81117.7,
    )
}
