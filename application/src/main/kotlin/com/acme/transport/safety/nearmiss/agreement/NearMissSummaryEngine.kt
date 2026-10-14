package com.acme.transport.safety.nearmiss.agreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the near miss summary calculation. */
interface NearMissSummaryStep {
    fun describe(): String
    fun adjust(item: NearMissSummary, value: Double): Double
}

data class CapNearMissSummaryAdjuster(var cap: Double) : NearMissSummaryStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NearMissSummary, value: Double): Double = minOf(value, cap)
}

data class MinimumNearMissSummaryAdjuster(var minimum: Double) : NearMissSummaryStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NearMissSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingNearMissSummaryAdjuster(var step: Double) : NearMissSummaryStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NearMissSummary, value: Double): Double = roundTo(value, step)
}

data class FlaggedNearMissSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : NearMissSummaryStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: NearMissSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the near miss summary steps in order and keeps a trace of each. */
data class NearMissSummaryEngine(
    var steps: MutableList<NearMissSummaryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: NearMissSummary): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<NearMissSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultNearMissSummaryEngine(flags: Registry): NearMissSummaryEngine {
    return NearMissSummaryEngine(
        steps = mutableListOf<NearMissSummaryStep>(
            CapNearMissSummaryAdjuster(7437.0),
            MinimumNearMissSummaryAdjuster(23.3),
            RoundingNearMissSummaryAdjuster(0.25),
            FlaggedNearMissSummaryAdjuster(flags, "near-miss-summary-primary", 2.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 34795.2,
    )
}
