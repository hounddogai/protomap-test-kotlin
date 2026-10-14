package com.acme.transport.terminal.crossdock.snapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the default cross dock snapshot calculation. */
interface DefaultCrossDockSnapshotFactor {
    fun describe(): String
    fun adjust(item: DefaultCrossDockSnapshot, value: Double): Double
}

data class FlaggedDefaultCrossDockSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultCrossDockSnapshotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultCrossDockSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentDefaultCrossDockSnapshotAdjuster(var percent: Double) : DefaultCrossDockSnapshotFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultCrossDockSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LaneDefaultCrossDockSnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultCrossDockSnapshotFactor {
    override fun describe(): String = "by lane"

    override fun adjust(item: DefaultCrossDockSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumDefaultCrossDockSnapshotAdjuster(var minimum: Double) : DefaultCrossDockSnapshotFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultCrossDockSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredDefaultCrossDockSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultCrossDockSnapshotFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultCrossDockSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the default cross dock snapshot steps in order and keeps a trace of each. */
data class DefaultCrossDockSnapshotEngine(
    var steps: MutableList<DefaultCrossDockSnapshotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefaultCrossDockSnapshot): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultCrossDockSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefaultCrossDockSnapshotEngine(flags: Registry): DefaultCrossDockSnapshotEngine {
    return DefaultCrossDockSnapshotEngine(
        steps = mutableListOf<DefaultCrossDockSnapshotFactor>(
            FlaggedDefaultCrossDockSnapshotAdjuster(flags, "default-cross-dock-snapshot-rolling", 8.8),
            PercentDefaultCrossDockSnapshotAdjuster(-11.0),
            LaneDefaultCrossDockSnapshotAdjuster(mutableMapOf<String, Double>("online" to 1.03)),
            MinimumDefaultCrossDockSnapshotAdjuster(14.3),
            TieredDefaultCrossDockSnapshotAdjuster(
                mutableListOf<Double>(6034.0, 8794.0, 8872.0),
                mutableListOf<Double>(1.06, 0.85, 0.97),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 93248.2,
    )
}
