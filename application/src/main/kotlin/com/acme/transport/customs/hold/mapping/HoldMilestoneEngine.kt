package com.acme.transport.customs.hold.mapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the hold milestone calculation. */
interface HoldMilestoneFactor {
    fun describe(): String
    fun adjust(item: HoldMilestone, value: Double): Double
}

data class RoundingHoldMilestoneAdjuster(var step: Double) : HoldMilestoneFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: HoldMilestone, value: Double): Double = roundTo(value, step)
}

data class CapHoldMilestoneAdjuster(var cap: Double) : HoldMilestoneFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: HoldMilestone, value: Double): Double = minOf(value, cap)
}

data class FlaggedHoldMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : HoldMilestoneFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: HoldMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the hold milestone steps in order and keeps a trace of each. */
data class HoldMilestoneEngine(
    var steps: MutableList<HoldMilestoneFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: HoldMilestone): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: HoldMilestoneFactor = steps[index]
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

    fun evaluateAll(items: MutableList<HoldMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultHoldMilestoneEngine(flags: Registry): HoldMilestoneEngine {
    return HoldMilestoneEngine(
        steps = mutableListOf<HoldMilestoneFactor>(
            RoundingHoldMilestoneAdjuster(0.1),
            CapHoldMilestoneAdjuster(25910.0),
            FlaggedHoldMilestoneAdjuster(flags, "hold-milestone-global", -2.6),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 34670.6,
    )
}
