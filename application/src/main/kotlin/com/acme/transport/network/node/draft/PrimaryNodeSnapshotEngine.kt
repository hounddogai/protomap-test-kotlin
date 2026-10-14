package com.acme.transport.network.node.draft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the primary node snapshot calculation. */
interface PrimaryNodeSnapshotStep {
    fun describe(): String
    fun adjust(item: PrimaryNodeSnapshot, value: Double): Double
}

data class FlaggedPrimaryNodeSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryNodeSnapshotStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryNodeSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumPrimaryNodeSnapshotAdjuster(var minimum: Double) : PrimaryNodeSnapshotStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryNodeSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredPrimaryNodeSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryNodeSnapshotStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryNodeSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the primary node snapshot steps in order and keeps a trace of each. */
data class PrimaryNodeSnapshotEngine(
    var steps: MutableList<PrimaryNodeSnapshotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryNodeSnapshot): Double {
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

    fun evaluateAll(items: MutableList<PrimaryNodeSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryNodeSnapshotEngine(flags: Registry): PrimaryNodeSnapshotEngine {
    return PrimaryNodeSnapshotEngine(
        steps = mutableListOf<PrimaryNodeSnapshotStep>(
            FlaggedPrimaryNodeSnapshotAdjuster(flags, "primary-node-snapshot-regional", -8.4),
            MinimumPrimaryNodeSnapshotAdjuster(41.0),
            TieredPrimaryNodeSnapshotAdjuster(
                mutableListOf<Double>(2328.0, 8129.0, 8901.0),
                mutableListOf<Double>(0.91, 0.83, 1.18),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 71163.8,
    )
}
