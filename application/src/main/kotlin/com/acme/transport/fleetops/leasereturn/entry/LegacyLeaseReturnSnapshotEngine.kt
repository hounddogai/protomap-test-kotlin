package com.acme.transport.fleetops.leasereturn.entry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy lease return snapshot calculation. */
interface LegacyLeaseReturnSnapshotFactor {
    fun describe(): String
    fun adjust(item: LegacyLeaseReturnSnapshot, value: Double): Double
}

data class CapLegacyLeaseReturnSnapshotAdjuster(var cap: Double) : LegacyLeaseReturnSnapshotFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyLeaseReturnSnapshot, value: Double): Double = minOf(value, cap)
}

data class RoundingLegacyLeaseReturnSnapshotAdjuster(var step: Double) : LegacyLeaseReturnSnapshotFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyLeaseReturnSnapshot, value: Double): Double = roundTo(value, step)
}

data class FlaggedLegacyLeaseReturnSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyLeaseReturnSnapshotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyLeaseReturnSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredLegacyLeaseReturnSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyLeaseReturnSnapshotFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyLeaseReturnSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumLegacyLeaseReturnSnapshotAdjuster(var minimum: Double) : LegacyLeaseReturnSnapshotFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyLeaseReturnSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelLegacyLeaseReturnSnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyLeaseReturnSnapshotFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: LegacyLeaseReturnSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the legacy lease return snapshot steps in order and keeps a trace of each. */
data class LegacyLeaseReturnSnapshotEngine(
    var steps: MutableList<LegacyLeaseReturnSnapshotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyLeaseReturnSnapshot): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyLeaseReturnSnapshotFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyLeaseReturnSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyLeaseReturnSnapshotEngine(flags: Registry): LegacyLeaseReturnSnapshotEngine {
    return LegacyLeaseReturnSnapshotEngine(
        steps = mutableListOf<LegacyLeaseReturnSnapshotFactor>(
            CapLegacyLeaseReturnSnapshotAdjuster(6736.0),
            RoundingLegacyLeaseReturnSnapshotAdjuster(5.0),
            FlaggedLegacyLeaseReturnSnapshotAdjuster(flags, "legacy-lease-return-snapshot-global", 9.1),
            TieredLegacyLeaseReturnSnapshotAdjuster(
                mutableListOf<Double>(5822.0, 6515.0),
                mutableListOf<Double>(0.83, 1.1),
            ),
            MinimumLegacyLeaseReturnSnapshotAdjuster(16.6),
            LabelLegacyLeaseReturnSnapshotAdjuster(
                mutableMapOf<String, Double>("north" to 1.24, "online" to 1.16, "south" to 1.14),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 79423.4,
    )
}
