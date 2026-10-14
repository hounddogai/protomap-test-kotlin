package com.acme.transport.terminal.crossdock.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped cross dock snapshot calculation. */
interface CappedCrossDockSnapshotAdjuster {
    fun describe(): String
    fun adjust(item: CappedCrossDockSnapshot, value: Double): Double
}

data class MinimumCappedCrossDockSnapshotAdjuster(var minimum: Double) : CappedCrossDockSnapshotAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedCrossDockSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredCappedCrossDockSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedCrossDockSnapshotAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedCrossDockSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingCappedCrossDockSnapshotAdjuster(var step: Double) : CappedCrossDockSnapshotAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedCrossDockSnapshot, value: Double): Double = roundTo(value, step)
}

data class CapCappedCrossDockSnapshotAdjuster(var cap: Double) : CappedCrossDockSnapshotAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedCrossDockSnapshot, value: Double): Double = minOf(value, cap)
}

data class FlaggedCappedCrossDockSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedCrossDockSnapshotAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedCrossDockSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the capped cross dock snapshot steps in order and keeps a trace of each. */
data class CappedCrossDockSnapshotEngine(
    var steps: MutableList<CappedCrossDockSnapshotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedCrossDockSnapshot): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedCrossDockSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedCrossDockSnapshotEngine(flags: Registry): CappedCrossDockSnapshotEngine {
    return CappedCrossDockSnapshotEngine(
        steps = mutableListOf<CappedCrossDockSnapshotAdjuster>(
            MinimumCappedCrossDockSnapshotAdjuster(37.0),
            TieredCappedCrossDockSnapshotAdjuster(
                mutableListOf<Double>(375.0, 1912.0),
                mutableListOf<Double>(0.91, 0.97),
            ),
            RoundingCappedCrossDockSnapshotAdjuster(0.05),
            CapCappedCrossDockSnapshotAdjuster(3603.0),
            FlaggedCappedCrossDockSnapshotAdjuster(flags, "capped-cross-dock-snapshot-legacy", 6.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 71534.7,
    )
}
