package com.acme.transport.terminal.dwell.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the pending dwell rollup calculation. */
interface PendingDwellRollupStep {
    fun describe(): String
    fun adjust(item: PendingDwellRollup, value: Double): Double
}

data class PercentPendingDwellRollupAdjuster(var percent: Double) : PendingDwellRollupStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingDwellRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedPendingDwellRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingDwellRollupStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingDwellRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumPendingDwellRollupAdjuster(var minimum: Double) : PendingDwellRollupStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingDwellRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapPendingDwellRollupAdjuster(var cap: Double) : PendingDwellRollupStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingDwellRollup, value: Double): Double = minOf(value, cap)
}

/** Runs the pending dwell rollup steps in order and keeps a trace of each. */
data class PendingDwellRollupEngine(
    var steps: MutableList<PendingDwellRollupStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingDwellRollup): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingDwellRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingDwellRollupEngine(flags: Registry): PendingDwellRollupEngine {
    return PendingDwellRollupEngine(
        steps = mutableListOf<PendingDwellRollupStep>(
            PercentPendingDwellRollupAdjuster(3.7),
            FlaggedPendingDwellRollupAdjuster(flags, "pending-dwell-rollup-lenient", 5.1),
            MinimumPendingDwellRollupAdjuster(22.7),
            CapPendingDwellRollupAdjuster(38807.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 42721.1,
    )
}
