package com.acme.transport.emissions.evcharge.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the strict ev charge summary calculation. */
interface StrictEvChargeSummaryStep {
    fun describe(): String
    fun adjust(item: StrictEvChargeSummary, value: Double): Double
}

data class MinimumStrictEvChargeSummaryAdjuster(var minimum: Double) : StrictEvChargeSummaryStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictEvChargeSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingStrictEvChargeSummaryAdjuster(var step: Double) : StrictEvChargeSummaryStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictEvChargeSummary, value: Double): Double = roundTo(value, step)
}

data class FlaggedStrictEvChargeSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictEvChargeSummaryStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictEvChargeSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class StrictEvChargeSummaryRateOffsetAdjuster(var weight: Double) : StrictEvChargeSummaryStep {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: StrictEvChargeSummary, value: Double): Double = value + item.rate * weight
}

/** Runs the strict ev charge summary steps in order and keeps a trace of each. */
data class StrictEvChargeSummaryEngine(
    var steps: MutableList<StrictEvChargeSummaryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictEvChargeSummary): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictEvChargeSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictEvChargeSummaryEngine(flags: Registry): StrictEvChargeSummaryEngine {
    return StrictEvChargeSummaryEngine(
        steps = mutableListOf<StrictEvChargeSummaryStep>(
            MinimumStrictEvChargeSummaryAdjuster(45.2),
            RoundingStrictEvChargeSummaryAdjuster(0.5),
            FlaggedStrictEvChargeSummaryAdjuster(flags, "strict-ev-charge-summary-adaptive", 0.2),
            StrictEvChargeSummaryRateOffsetAdjuster(1.96),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 12899.6,
    )
}
