package com.acme.transport.linehaul.laneplan.request

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the lane plan rollup calculation. */
interface LanePlanRollupAdjuster {
    fun describe(): String
    fun adjust(item: LanePlanRollup, value: Double): Double
}

data class RoundingLanePlanRollupAdjuster(var step: Double) : LanePlanRollupAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LanePlanRollup, value: Double): Double = roundTo(value, step)
}

data class FlaggedLanePlanRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LanePlanRollupAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LanePlanRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapLanePlanRollupAdjuster(var cap: Double) : LanePlanRollupAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LanePlanRollup, value: Double): Double = minOf(value, cap)
}

/** Runs the lane plan rollup steps in order and keeps a trace of each. */
data class LanePlanRollupEngine(
    var steps: MutableList<LanePlanRollupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LanePlanRollup): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LanePlanRollupAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LanePlanRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLanePlanRollupEngine(flags: Registry): LanePlanRollupEngine {
    return LanePlanRollupEngine(
        steps = mutableListOf<LanePlanRollupAdjuster>(
            RoundingLanePlanRollupAdjuster(0.05),
            FlaggedLanePlanRollupAdjuster(flags, "lane-plan-rollup-strict", 6.1),
            CapLanePlanRollupAdjuster(5977.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 80367.2,
    )
}
