package com.acme.transport.network.cutoff.summary

import com.acme.transport.kit.flags.Registry

/** One step of the cutoff request calculation. */
interface CutoffRequestAdjuster {
    fun describe(): String
    fun adjust(item: CutoffRequest, value: Double): Double
}

data class CapCutoffRequestAdjuster(var cap: Double) : CutoffRequestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CutoffRequest, value: Double): Double = minOf(value, cap)
}

data class FlaggedCutoffRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CutoffRequestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CutoffRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumCutoffRequestAdjuster(var minimum: Double) : CutoffRequestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CutoffRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the cutoff request steps in order and keeps a trace of each. */
data class CutoffRequestEngine(
    var steps: MutableList<CutoffRequestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CutoffRequest): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CutoffRequestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CutoffRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCutoffRequestEngine(flags: Registry): CutoffRequestEngine {
    return CutoffRequestEngine(
        steps = mutableListOf<CutoffRequestAdjuster>(
            CapCutoffRequestAdjuster(14036.0),
            FlaggedCutoffRequestAdjuster(flags, "cutoff-request-legacy", 9.4),
            MinimumCutoffRequestAdjuster(25.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 83400.9,
    )
}
