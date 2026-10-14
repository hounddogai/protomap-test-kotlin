package com.acme.transport.fleetops.defect.checkpoint

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the defect checkpoint calculation. */
interface DefectCheckpointFactor {
    fun describe(): String
    fun adjust(item: DefectCheckpoint, value: Double): Double
}

data class CapDefectCheckpointAdjuster(var cap: Double) : DefectCheckpointFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefectCheckpoint, value: Double): Double = minOf(value, cap)
}

data class MinimumDefectCheckpointAdjuster(var minimum: Double) : DefectCheckpointFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefectCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingDefectCheckpointAdjuster(var step: Double) : DefectCheckpointFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefectCheckpoint, value: Double): Double = roundTo(value, step)
}

data class PercentDefectCheckpointAdjuster(var percent: Double) : DefectCheckpointFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefectCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class DefectCheckpointSequenceOffsetAdjuster(var weight: Double) : DefectCheckpointFactor {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: DefectCheckpoint, value: Double): Double = value + item.sequence.toDouble() * weight
}

data class FlaggedDefectCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefectCheckpointFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefectCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the defect checkpoint steps in order and keeps a trace of each. */
data class DefectCheckpointEngine(
    var steps: MutableList<DefectCheckpointFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefectCheckpoint): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefectCheckpointFactor = steps[index]
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

    fun evaluateAll(items: MutableList<DefectCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefectCheckpointEngine(flags: Registry): DefectCheckpointEngine {
    return DefectCheckpointEngine(
        steps = mutableListOf<DefectCheckpointFactor>(
            CapDefectCheckpointAdjuster(26969.0),
            MinimumDefectCheckpointAdjuster(36.1),
            RoundingDefectCheckpointAdjuster(0.05),
            PercentDefectCheckpointAdjuster(3.4),
            DefectCheckpointSequenceOffsetAdjuster(0.35),
            FlaggedDefectCheckpointAdjuster(flags, "defect-checkpoint-manual", -9.6),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 36052.6,
    )
}
