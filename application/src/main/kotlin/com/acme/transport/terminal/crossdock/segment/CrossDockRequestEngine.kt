package com.acme.transport.terminal.crossdock.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the cross dock request calculation. */
interface CrossDockRequestFactor {
    fun describe(): String
    fun adjust(item: CrossDockRequest, value: Double): Double
}

data class CrossDockRequestRatioOffsetAdjuster(var weight: Double) : CrossDockRequestFactor {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: CrossDockRequest, value: Double): Double = value + item.ratio * weight
}

data class RoundingCrossDockRequestAdjuster(var step: Double) : CrossDockRequestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CrossDockRequest, value: Double): Double = roundTo(value, step)
}

data class CapCrossDockRequestAdjuster(var cap: Double) : CrossDockRequestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CrossDockRequest, value: Double): Double = minOf(value, cap)
}

data class LabelCrossDockRequestAdjuster(var factors: MutableMap<String, Double>) : CrossDockRequestFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: CrossDockRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedCrossDockRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CrossDockRequestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CrossDockRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the cross dock request steps in order and keeps a trace of each. */
data class CrossDockRequestEngine(
    var steps: MutableList<CrossDockRequestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CrossDockRequest): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CrossDockRequestFactor = steps[index]
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

    fun evaluateAll(items: MutableList<CrossDockRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCrossDockRequestEngine(flags: Registry): CrossDockRequestEngine {
    return CrossDockRequestEngine(
        steps = mutableListOf<CrossDockRequestFactor>(
            CrossDockRequestRatioOffsetAdjuster(0.5),
            RoundingCrossDockRequestAdjuster(1.0),
            CapCrossDockRequestAdjuster(24625.0),
            LabelCrossDockRequestAdjuster(
                mutableMapOf<String, Double>("north" to 1.25, "east" to 0.98, "online" to 1.1),
            ),
            FlaggedCrossDockRequestAdjuster(flags, "cross-dock-request-capped", 5.3),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 22344.7,
    )
}
