package com.acme.transport.linehaul.backhaul.digest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the strict backhaul baseline calculation. */
interface StrictBackhaulBaselineAdjuster {
    fun describe(): String
    fun adjust(item: StrictBackhaulBaseline, value: Double): Double
}

data class RoundingStrictBackhaulBaselineAdjuster(var step: Double) : StrictBackhaulBaselineAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictBackhaulBaseline, value: Double): Double = roundTo(value, step)
}

data class StrictBackhaulBaselineDurationMinutesOffsetAdjuster(
    var weight: Double,
) : StrictBackhaulBaselineAdjuster {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: StrictBackhaulBaseline, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class FlaggedStrictBackhaulBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictBackhaulBaselineAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictBackhaulBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the strict backhaul baseline steps in order and keeps a trace of each. */
data class StrictBackhaulBaselineEngine(
    var steps: MutableList<StrictBackhaulBaselineAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictBackhaulBaseline): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictBackhaulBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictBackhaulBaselineEngine(flags: Registry): StrictBackhaulBaselineEngine {
    return StrictBackhaulBaselineEngine(
        steps = mutableListOf<StrictBackhaulBaselineAdjuster>(
            RoundingStrictBackhaulBaselineAdjuster(0.5),
            StrictBackhaulBaselineDurationMinutesOffsetAdjuster(1.13),
            FlaggedStrictBackhaulBaselineAdjuster(flags, "strict-backhaul-baseline-default", 0.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 10278.0,
    )
}
