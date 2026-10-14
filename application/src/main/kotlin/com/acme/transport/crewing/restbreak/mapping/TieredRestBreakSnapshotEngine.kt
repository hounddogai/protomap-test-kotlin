package com.acme.transport.crewing.restbreak.mapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the tiered rest break snapshot calculation. */
interface TieredRestBreakSnapshotStep {
    fun describe(): String
    fun adjust(item: TieredRestBreakSnapshot, value: Double): Double
}

data class RoundingTieredRestBreakSnapshotAdjuster(var step: Double) : TieredRestBreakSnapshotStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredRestBreakSnapshot, value: Double): Double = roundTo(value, step)
}

data class TieredRestBreakSnapshotCreatedAtOffsetAdjuster(var weight: Double) : TieredRestBreakSnapshotStep {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: TieredRestBreakSnapshot, value: Double): Double {
        return value + item.createdAt.toDouble() * weight
    }
}

data class CapTieredRestBreakSnapshotAdjuster(var cap: Double) : TieredRestBreakSnapshotStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredRestBreakSnapshot, value: Double): Double = minOf(value, cap)
}

data class TieredTieredRestBreakSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredRestBreakSnapshotStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredRestBreakSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedTieredRestBreakSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredRestBreakSnapshotStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredRestBreakSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the tiered rest break snapshot steps in order and keeps a trace of each. */
data class TieredRestBreakSnapshotEngine(
    var steps: MutableList<TieredRestBreakSnapshotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredRestBreakSnapshot): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredRestBreakSnapshotStep = steps[index]
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

    fun evaluateAll(items: MutableList<TieredRestBreakSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredRestBreakSnapshotEngine(flags: Registry): TieredRestBreakSnapshotEngine {
    return TieredRestBreakSnapshotEngine(
        steps = mutableListOf<TieredRestBreakSnapshotStep>(
            RoundingTieredRestBreakSnapshotAdjuster(1.0),
            TieredRestBreakSnapshotCreatedAtOffsetAdjuster(0.85),
            CapTieredRestBreakSnapshotAdjuster(31003.0),
            TieredTieredRestBreakSnapshotAdjuster(
                mutableListOf<Double>(2032.0, 5587.0),
                mutableListOf<Double>(1.04, 0.82),
            ),
            FlaggedTieredRestBreakSnapshotAdjuster(flags, "tiered-rest-break-snapshot-global", -4.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 11384.2,
    )
}
