package com.acme.transport.safety.violation.checkpoint

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the violation summary calculation. */
interface ViolationSummaryAdjuster {
    fun describe(): String
    fun adjust(item: ViolationSummary, value: Double): Double
}

data class FlaggedViolationSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ViolationSummaryAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ViolationSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CategoryViolationSummaryAdjuster(
    var factors: MutableMap<String, Double>,
) : ViolationSummaryAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: ViolationSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapViolationSummaryAdjuster(var cap: Double) : ViolationSummaryAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ViolationSummary, value: Double): Double = minOf(value, cap)
}

data class ViolationSummaryVersionOffsetAdjuster(var weight: Double) : ViolationSummaryAdjuster {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: ViolationSummary, value: Double): Double = value + item.version.toDouble() * weight
}

data class RoundingViolationSummaryAdjuster(var step: Double) : ViolationSummaryAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ViolationSummary, value: Double): Double = roundTo(value, step)
}

/** Runs the violation summary steps in order and keeps a trace of each. */
data class ViolationSummaryEngine(
    var steps: MutableList<ViolationSummaryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ViolationSummary): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ViolationSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultViolationSummaryEngine(flags: Registry): ViolationSummaryEngine {
    return ViolationSummaryEngine(
        steps = mutableListOf<ViolationSummaryAdjuster>(
            FlaggedViolationSummaryAdjuster(flags, "violation-summary-strict", -2.0),
            CategoryViolationSummaryAdjuster(mutableMapOf<String, Double>("retail" to 1.39, "central" to 1.16)),
            CapViolationSummaryAdjuster(23232.0),
            ViolationSummaryVersionOffsetAdjuster(0.78),
            RoundingViolationSummaryAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 60710.5,
    )
}
