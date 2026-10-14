package com.acme.transport.safety.coaching.agreement

import com.acme.transport.kit.flags.Registry

/** One step of the legacy coaching snapshot calculation. */
interface LegacyCoachingSnapshotStep {
    fun describe(): String
    fun adjust(item: LegacyCoachingSnapshot, value: Double): Double
}

data class MinimumLegacyCoachingSnapshotAdjuster(var minimum: Double) : LegacyCoachingSnapshotStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyCoachingSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedLegacyCoachingSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyCoachingSnapshotStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyCoachingSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LegacyCoachingSnapshotRetriesOffsetAdjuster(var weight: Double) : LegacyCoachingSnapshotStep {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: LegacyCoachingSnapshot, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

/** Runs the legacy coaching snapshot steps in order and keeps a trace of each. */
data class LegacyCoachingSnapshotEngine(
    var steps: MutableList<LegacyCoachingSnapshotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LegacyCoachingSnapshot): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyCoachingSnapshotStep = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyCoachingSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLegacyCoachingSnapshotEngine(flags: Registry): LegacyCoachingSnapshotEngine {
    return LegacyCoachingSnapshotEngine(
        steps = mutableListOf<LegacyCoachingSnapshotStep>(
            MinimumLegacyCoachingSnapshotAdjuster(48.0),
            FlaggedLegacyCoachingSnapshotAdjuster(flags, "legacy-coaching-snapshot-adaptive", 5.0),
            LegacyCoachingSnapshotRetriesOffsetAdjuster(0.33),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 77326.6,
    )
}
