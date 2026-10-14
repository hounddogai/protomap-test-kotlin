package com.acme.transport.crewing.restbreak.checkpoint

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled rest break checkpoint calculation. */
interface ScheduledRestBreakCheckpointModifier {
    fun describe(): String
    fun adjust(item: ScheduledRestBreakCheckpoint, value: Double): Double
}

data class LaneScheduledRestBreakCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledRestBreakCheckpointModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: ScheduledRestBreakCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapScheduledRestBreakCheckpointAdjuster(var cap: Double) : ScheduledRestBreakCheckpointModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledRestBreakCheckpoint, value: Double): Double = minOf(value, cap)
}

data class FlaggedScheduledRestBreakCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledRestBreakCheckpointModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledRestBreakCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the scheduled rest break checkpoint steps in order and keeps a trace of each. */
data class ScheduledRestBreakCheckpointEngine(
    var steps: MutableList<ScheduledRestBreakCheckpointModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledRestBreakCheckpoint): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledRestBreakCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledRestBreakCheckpointEngine(flags: Registry): ScheduledRestBreakCheckpointEngine {
    return ScheduledRestBreakCheckpointEngine(
        steps = mutableListOf<ScheduledRestBreakCheckpointModifier>(
            LaneScheduledRestBreakCheckpointAdjuster(mutableMapOf<String, Double>("north" to 1.12)),
            CapScheduledRestBreakCheckpointAdjuster(9712.0),
            FlaggedScheduledRestBreakCheckpointAdjuster(flags, "scheduled-rest-break-checkpoint-weighted", -9.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 57034.7,
    )
}
