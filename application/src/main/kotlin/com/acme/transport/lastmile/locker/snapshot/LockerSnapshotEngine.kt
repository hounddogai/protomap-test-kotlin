package com.acme.transport.lastmile.locker.snapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the locker snapshot calculation. */
interface LockerSnapshotModifier {
    fun describe(): String
    fun adjust(item: LockerSnapshot, value: Double): Double
}

data class MinimumLockerSnapshotAdjuster(var minimum: Double) : LockerSnapshotModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LockerSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedLockerSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LockerSnapshotModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LockerSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingLockerSnapshotAdjuster(var step: Double) : LockerSnapshotModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LockerSnapshot, value: Double): Double = roundTo(value, step)
}

data class CapLockerSnapshotAdjuster(var cap: Double) : LockerSnapshotModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LockerSnapshot, value: Double): Double = minOf(value, cap)
}

data class PercentLockerSnapshotAdjuster(var percent: Double) : LockerSnapshotModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LockerSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the locker snapshot steps in order and keeps a trace of each. */
data class LockerSnapshotEngine(
    var steps: MutableList<LockerSnapshotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LockerSnapshot): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LockerSnapshotModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LockerSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLockerSnapshotEngine(flags: Registry): LockerSnapshotEngine {
    return LockerSnapshotEngine(
        steps = mutableListOf<LockerSnapshotModifier>(
            MinimumLockerSnapshotAdjuster(18.8),
            FlaggedLockerSnapshotAdjuster(flags, "locker-snapshot-pending", 4.1),
            RoundingLockerSnapshotAdjuster(0.1),
            CapLockerSnapshotAdjuster(13170.0),
            PercentLockerSnapshotAdjuster(14.2),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 21173.2,
    )
}
