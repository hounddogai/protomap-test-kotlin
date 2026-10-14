package com.acme.transport.terminal.dwell.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the global dwell exception calculation. */
interface GlobalDwellExceptionStep {
    fun describe(): String
    fun adjust(item: GlobalDwellException, value: Double): Double
}

data class CapGlobalDwellExceptionAdjuster(var cap: Double) : GlobalDwellExceptionStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalDwellException, value: Double): Double = minOf(value, cap)
}

data class RoundingGlobalDwellExceptionAdjuster(var step: Double) : GlobalDwellExceptionStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalDwellException, value: Double): Double = roundTo(value, step)
}

data class MinimumGlobalDwellExceptionAdjuster(var minimum: Double) : GlobalDwellExceptionStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalDwellException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedGlobalDwellExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalDwellExceptionStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalDwellException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the global dwell exception steps in order and keeps a trace of each. */
data class GlobalDwellExceptionEngine(
    var steps: MutableList<GlobalDwellExceptionStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalDwellException): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalDwellExceptionStep = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalDwellException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalDwellExceptionEngine(flags: Registry): GlobalDwellExceptionEngine {
    return GlobalDwellExceptionEngine(
        steps = mutableListOf<GlobalDwellExceptionStep>(
            CapGlobalDwellExceptionAdjuster(32690.0),
            RoundingGlobalDwellExceptionAdjuster(0.5),
            MinimumGlobalDwellExceptionAdjuster(7.3),
            FlaggedGlobalDwellExceptionAdjuster(flags, "global-dwell-exception-pending", 2.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 11308.2,
    )
}
