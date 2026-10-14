package com.acme.transport.network.node.nodesnapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the regional node snapshot calculation. */
interface RegionalNodeSnapshotStep {
    fun describe(): String
    fun adjust(item: RegionalNodeSnapshot, value: Double): Double
}

data class FlaggedRegionalNodeSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalNodeSnapshotStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalNodeSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameRegionalNodeSnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalNodeSnapshotStep {
    override fun describe(): String = "by name"

    override fun adjust(item: RegionalNodeSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingRegionalNodeSnapshotAdjuster(var step: Double) : RegionalNodeSnapshotStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalNodeSnapshot, value: Double): Double = roundTo(value, step)
}

data class MinimumRegionalNodeSnapshotAdjuster(var minimum: Double) : RegionalNodeSnapshotStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalNodeSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RegionalNodeSnapshotAmountOffsetAdjuster(var weight: Double) : RegionalNodeSnapshotStep {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: RegionalNodeSnapshot, value: Double): Double = value + item.amount * weight
}

/** Runs the regional node snapshot steps in order and keeps a trace of each. */
data class RegionalNodeSnapshotEngine(
    var steps: MutableList<RegionalNodeSnapshotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalNodeSnapshot): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalNodeSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalNodeSnapshotEngine(flags: Registry): RegionalNodeSnapshotEngine {
    return RegionalNodeSnapshotEngine(
        steps = mutableListOf<RegionalNodeSnapshotStep>(
            FlaggedRegionalNodeSnapshotAdjuster(flags, "regional-node-snapshot-capped", -0.9),
            NameRegionalNodeSnapshotAdjuster(
                mutableMapOf<String, Double>("east" to 0.8, "retail" to 1.12, "west" to 0.82),
            ),
            RoundingRegionalNodeSnapshotAdjuster(0.5),
            MinimumRegionalNodeSnapshotAdjuster(37.1),
            RegionalNodeSnapshotAmountOffsetAdjuster(0.08),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 37931.3,
    )
}
