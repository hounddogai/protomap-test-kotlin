package com.acme.transport.safety.coaching.coachingsnapshot

import com.acme.transport.kit.flags.Registry

/** One step of the primary coaching snapshot calculation. */
interface PrimaryCoachingSnapshotStep {
    fun describe(): String
    fun adjust(item: PrimaryCoachingSnapshot, value: Double): Double
}

data class CapPrimaryCoachingSnapshotAdjuster(var cap: Double) : PrimaryCoachingSnapshotStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryCoachingSnapshot, value: Double): Double = minOf(value, cap)
}

data class PrimaryCoachingSnapshotRatioOffsetAdjuster(var weight: Double) : PrimaryCoachingSnapshotStep {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: PrimaryCoachingSnapshot, value: Double): Double = value + item.ratio * weight
}

data class PercentPrimaryCoachingSnapshotAdjuster(var percent: Double) : PrimaryCoachingSnapshotStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryCoachingSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumPrimaryCoachingSnapshotAdjuster(var minimum: Double) : PrimaryCoachingSnapshotStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryCoachingSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedPrimaryCoachingSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryCoachingSnapshotStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryCoachingSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NamePrimaryCoachingSnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryCoachingSnapshotStep {
    override fun describe(): String = "by name"

    override fun adjust(item: PrimaryCoachingSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the primary coaching snapshot steps in order and keeps a trace of each. */
data class PrimaryCoachingSnapshotEngine(
    var steps: MutableList<PrimaryCoachingSnapshotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimaryCoachingSnapshot): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryCoachingSnapshotStep = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryCoachingSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimaryCoachingSnapshotEngine(flags: Registry): PrimaryCoachingSnapshotEngine {
    return PrimaryCoachingSnapshotEngine(
        steps = mutableListOf<PrimaryCoachingSnapshotStep>(
            CapPrimaryCoachingSnapshotAdjuster(39480.0),
            PrimaryCoachingSnapshotRatioOffsetAdjuster(1.41),
            PercentPrimaryCoachingSnapshotAdjuster(-18.1),
            MinimumPrimaryCoachingSnapshotAdjuster(16.5),
            FlaggedPrimaryCoachingSnapshotAdjuster(flags, "primary-coaching-snapshot-regional", 0.5),
            NamePrimaryCoachingSnapshotAdjuster(mutableMapOf<String, Double>("retail" to 0.84)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 18420.7,
    )
}
