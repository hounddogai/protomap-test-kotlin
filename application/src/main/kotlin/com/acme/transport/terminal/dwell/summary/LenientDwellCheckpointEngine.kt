package com.acme.transport.terminal.dwell.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient dwell checkpoint calculation. */
interface LenientDwellCheckpointStep {
    fun describe(): String
    fun adjust(item: LenientDwellCheckpoint, value: Double): Double
}

data class FlaggedLenientDwellCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientDwellCheckpointStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientDwellCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentLenientDwellCheckpointAdjuster(var percent: Double) : LenientDwellCheckpointStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientDwellCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapLenientDwellCheckpointAdjuster(var cap: Double) : LenientDwellCheckpointStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientDwellCheckpoint, value: Double): Double = minOf(value, cap)
}

data class LabelLenientDwellCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientDwellCheckpointStep {
    override fun describe(): String = "by label"

    override fun adjust(item: LenientDwellCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the lenient dwell checkpoint steps in order and keeps a trace of each. */
data class LenientDwellCheckpointEngine(
    var steps: MutableList<LenientDwellCheckpointStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientDwellCheckpoint): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientDwellCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientDwellCheckpointEngine(flags: Registry): LenientDwellCheckpointEngine {
    return LenientDwellCheckpointEngine(
        steps = mutableListOf<LenientDwellCheckpointStep>(
            FlaggedLenientDwellCheckpointAdjuster(flags, "lenient-dwell-checkpoint-tiered", -1.4),
            PercentLenientDwellCheckpointAdjuster(4.9),
            CapLenientDwellCheckpointAdjuster(29801.0),
            LabelLenientDwellCheckpointAdjuster(mutableMapOf<String, Double>("west" to 0.98)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 18691.5,
    )
}
