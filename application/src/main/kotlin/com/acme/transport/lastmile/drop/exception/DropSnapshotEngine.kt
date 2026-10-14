package com.acme.transport.lastmile.drop.exception

import com.acme.transport.kit.flags.Registry

/** One step of the drop snapshot calculation. */
interface DropSnapshotAdjuster {
    fun describe(): String
    fun adjust(item: DropSnapshot, value: Double): Double
}

data class PercentDropSnapshotAdjuster(var percent: Double) : DropSnapshotAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DropSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class DropSnapshotSequenceOffsetAdjuster(var weight: Double) : DropSnapshotAdjuster {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: DropSnapshot, value: Double): Double = value + item.sequence.toDouble() * weight
}

data class CapDropSnapshotAdjuster(var cap: Double) : DropSnapshotAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DropSnapshot, value: Double): Double = minOf(value, cap)
}

data class FlaggedDropSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DropSnapshotAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DropSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredDropSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DropSnapshotAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DropSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumDropSnapshotAdjuster(var minimum: Double) : DropSnapshotAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DropSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the drop snapshot steps in order and keeps a trace of each. */
data class DropSnapshotEngine(
    var steps: MutableList<DropSnapshotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DropSnapshot): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DropSnapshotAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DropSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDropSnapshotEngine(flags: Registry): DropSnapshotEngine {
    return DropSnapshotEngine(
        steps = mutableListOf<DropSnapshotAdjuster>(
            PercentDropSnapshotAdjuster(11.2),
            DropSnapshotSequenceOffsetAdjuster(0.61),
            CapDropSnapshotAdjuster(2703.0),
            FlaggedDropSnapshotAdjuster(flags, "drop-snapshot-adaptive", 9.8),
            TieredDropSnapshotAdjuster(
                mutableListOf<Double>(4350.0, 4777.0, 8657.0, 9619.0),
                mutableListOf<Double>(1.2, 1.03, 1.1, 1.14),
            ),
            MinimumDropSnapshotAdjuster(44.6),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 84875.7,
    )
}
