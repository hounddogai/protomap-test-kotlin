package com.acme.transport.terminal.dwell.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the primary dwell snapshot calculation. */
interface PrimaryDwellSnapshotFactor {
    fun describe(): String
    fun adjust(item: PrimaryDwellSnapshot, value: Double): Double
}

data class PercentPrimaryDwellSnapshotAdjuster(var percent: Double) : PrimaryDwellSnapshotFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryDwellSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedPrimaryDwellSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryDwellSnapshotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryDwellSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumPrimaryDwellSnapshotAdjuster(var minimum: Double) : PrimaryDwellSnapshotFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryDwellSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingPrimaryDwellSnapshotAdjuster(var step: Double) : PrimaryDwellSnapshotFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryDwellSnapshot, value: Double): Double = roundTo(value, step)
}

data class CapPrimaryDwellSnapshotAdjuster(var cap: Double) : PrimaryDwellSnapshotFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryDwellSnapshot, value: Double): Double = minOf(value, cap)
}

data class TieredPrimaryDwellSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryDwellSnapshotFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryDwellSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the primary dwell snapshot steps in order and keeps a trace of each. */
data class PrimaryDwellSnapshotEngine(
    var steps: MutableList<PrimaryDwellSnapshotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimaryDwellSnapshot): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryDwellSnapshotFactor = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryDwellSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimaryDwellSnapshotEngine(flags: Registry): PrimaryDwellSnapshotEngine {
    return PrimaryDwellSnapshotEngine(
        steps = mutableListOf<PrimaryDwellSnapshotFactor>(
            PercentPrimaryDwellSnapshotAdjuster(-9.4),
            FlaggedPrimaryDwellSnapshotAdjuster(flags, "primary-dwell-snapshot-strict", -4.8),
            MinimumPrimaryDwellSnapshotAdjuster(15.0),
            RoundingPrimaryDwellSnapshotAdjuster(5.0),
            CapPrimaryDwellSnapshotAdjuster(18813.0),
            TieredPrimaryDwellSnapshotAdjuster(
                mutableListOf<Double>(1203.0, 3295.0, 6577.0, 8138.0),
                mutableListOf<Double>(0.87, 1.1, 0.89, 1.02),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 12848.1,
    )
}
