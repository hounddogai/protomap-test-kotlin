package com.acme.transport.safety.incident.snapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the strict incident snapshot calculation. */
interface StrictIncidentSnapshotStep {
    fun describe(): String
    fun adjust(item: StrictIncidentSnapshot, value: Double): Double
}

data class RoundingStrictIncidentSnapshotAdjuster(var step: Double) : StrictIncidentSnapshotStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictIncidentSnapshot, value: Double): Double = roundTo(value, step)
}

data class CapStrictIncidentSnapshotAdjuster(var cap: Double) : StrictIncidentSnapshotStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictIncidentSnapshot, value: Double): Double = minOf(value, cap)
}

data class TitleStrictIncidentSnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictIncidentSnapshotStep {
    override fun describe(): String = "by title"

    override fun adjust(item: StrictIncidentSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class StrictIncidentSnapshotWeightOffsetAdjuster(var weight: Double) : StrictIncidentSnapshotStep {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: StrictIncidentSnapshot, value: Double): Double = value + item.weight * weight
}

data class FlaggedStrictIncidentSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictIncidentSnapshotStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictIncidentSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredStrictIncidentSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictIncidentSnapshotStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictIncidentSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the strict incident snapshot steps in order and keeps a trace of each. */
data class StrictIncidentSnapshotEngine(
    var steps: MutableList<StrictIncidentSnapshotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: StrictIncidentSnapshot): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictIncidentSnapshotStep = steps[index]
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

    fun evaluateAll(items: MutableList<StrictIncidentSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultStrictIncidentSnapshotEngine(flags: Registry): StrictIncidentSnapshotEngine {
    return StrictIncidentSnapshotEngine(
        steps = mutableListOf<StrictIncidentSnapshotStep>(
            RoundingStrictIncidentSnapshotAdjuster(0.05),
            CapStrictIncidentSnapshotAdjuster(28996.0),
            TitleStrictIncidentSnapshotAdjuster(mutableMapOf<String, Double>("west" to 0.88, "north" to 1.2)),
            StrictIncidentSnapshotWeightOffsetAdjuster(0.24),
            FlaggedStrictIncidentSnapshotAdjuster(flags, "strict-incident-snapshot-manual", -8.6),
            TieredStrictIncidentSnapshotAdjuster(
                mutableListOf<Double>(1667.0, 2498.0, 3895.0, 6960.0),
                mutableListOf<Double>(0.97, 1.12, 0.81, 1.09),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 76553.7,
    )
}
