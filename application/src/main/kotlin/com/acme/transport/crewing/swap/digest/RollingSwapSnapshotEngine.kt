package com.acme.transport.crewing.swap.digest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling swap snapshot calculation. */
interface RollingSwapSnapshotModifier {
    fun describe(): String
    fun adjust(item: RollingSwapSnapshot, value: Double): Double
}

data class MinimumRollingSwapSnapshotAdjuster(var minimum: Double) : RollingSwapSnapshotModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingSwapSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedRollingSwapSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingSwapSnapshotModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingSwapSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RollingSwapSnapshotMaximumOffsetAdjuster(var weight: Double) : RollingSwapSnapshotModifier {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: RollingSwapSnapshot, value: Double): Double = value + item.maximum * weight
}

data class CategoryRollingSwapSnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingSwapSnapshotModifier {
    override fun describe(): String = "by category"

    override fun adjust(item: RollingSwapSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingRollingSwapSnapshotAdjuster(var step: Double) : RollingSwapSnapshotModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingSwapSnapshot, value: Double): Double = roundTo(value, step)
}

/** Runs the rolling swap snapshot steps in order and keeps a trace of each. */
data class RollingSwapSnapshotEngine(
    var steps: MutableList<RollingSwapSnapshotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RollingSwapSnapshot): Double {
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

    fun evaluateAll(items: MutableList<RollingSwapSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRollingSwapSnapshotEngine(flags: Registry): RollingSwapSnapshotEngine {
    return RollingSwapSnapshotEngine(
        steps = mutableListOf<RollingSwapSnapshotModifier>(
            MinimumRollingSwapSnapshotAdjuster(22.9),
            FlaggedRollingSwapSnapshotAdjuster(flags, "rolling-swap-snapshot-rolling", 9.0),
            RollingSwapSnapshotMaximumOffsetAdjuster(0.7),
            CategoryRollingSwapSnapshotAdjuster(mutableMapOf<String, Double>("online" to 1.05, "west" to 1.07)),
            RoundingRollingSwapSnapshotAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 18991.0,
    )
}
