package com.acme.transport.fleetops.defect.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the defect snapshot calculation. */
interface DefectSnapshotStep {
    fun describe(): String
    fun adjust(item: DefectSnapshot, value: Double): Double
}

data class MinimumDefectSnapshotAdjuster(var minimum: Double) : DefectSnapshotStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefectSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedDefectSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefectSnapshotStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefectSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingDefectSnapshotAdjuster(var step: Double) : DefectSnapshotStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefectSnapshot, value: Double): Double = roundTo(value, step)
}

/** Runs the defect snapshot steps in order and keeps a trace of each. */
data class DefectSnapshotEngine(
    var steps: MutableList<DefectSnapshotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefectSnapshot): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefectSnapshotStep = steps[index]
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

    fun evaluateAll(items: MutableList<DefectSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefectSnapshotEngine(flags: Registry): DefectSnapshotEngine {
    return DefectSnapshotEngine(
        steps = mutableListOf<DefectSnapshotStep>(
            MinimumDefectSnapshotAdjuster(48.2),
            FlaggedDefectSnapshotAdjuster(flags, "defect-snapshot-default", -8.0),
            RoundingDefectSnapshotAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 18448.5,
    )
}
