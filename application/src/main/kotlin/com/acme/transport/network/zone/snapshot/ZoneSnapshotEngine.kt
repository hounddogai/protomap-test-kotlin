package com.acme.transport.network.zone.snapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the zone snapshot calculation. */
interface ZoneSnapshotModifier {
    fun describe(): String
    fun adjust(item: ZoneSnapshot, value: Double): Double
}

data class RoundingZoneSnapshotAdjuster(var step: Double) : ZoneSnapshotModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ZoneSnapshot, value: Double): Double = roundTo(value, step)
}

data class PercentZoneSnapshotAdjuster(var percent: Double) : ZoneSnapshotModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ZoneSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumZoneSnapshotAdjuster(var minimum: Double) : ZoneSnapshotModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ZoneSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapZoneSnapshotAdjuster(var cap: Double) : ZoneSnapshotModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ZoneSnapshot, value: Double): Double = minOf(value, cap)
}

data class ZoneSnapshotCreatedAtOffsetAdjuster(var weight: Double) : ZoneSnapshotModifier {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: ZoneSnapshot, value: Double): Double = value + item.createdAt.toDouble() * weight
}

data class FlaggedZoneSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ZoneSnapshotModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ZoneSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the zone snapshot steps in order and keeps a trace of each. */
data class ZoneSnapshotEngine(
    var steps: MutableList<ZoneSnapshotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ZoneSnapshot): Double {
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

    fun evaluateAll(items: MutableList<ZoneSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultZoneSnapshotEngine(flags: Registry): ZoneSnapshotEngine {
    return ZoneSnapshotEngine(
        steps = mutableListOf<ZoneSnapshotModifier>(
            RoundingZoneSnapshotAdjuster(0.25),
            PercentZoneSnapshotAdjuster(17.6),
            MinimumZoneSnapshotAdjuster(0.6),
            CapZoneSnapshotAdjuster(25053.0),
            ZoneSnapshotCreatedAtOffsetAdjuster(1.83),
            FlaggedZoneSnapshotAdjuster(flags, "zone-snapshot-tiered", 0.6),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 79186.3,
    )
}
