package com.acme.transport.linehaul.backhaul.audit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual backhaul baseline calculation. */
interface ManualBackhaulBaselineModifier {
    fun describe(): String
    fun adjust(item: ManualBackhaulBaseline, value: Double): Double
}

data class TieredManualBackhaulBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualBackhaulBaselineModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualBackhaulBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ManualBackhaulBaselineRateOffsetAdjuster(var weight: Double) : ManualBackhaulBaselineModifier {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: ManualBackhaulBaseline, value: Double): Double = value + item.rate * weight
}

data class FlaggedManualBackhaulBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualBackhaulBaselineModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualBackhaulBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingManualBackhaulBaselineAdjuster(var step: Double) : ManualBackhaulBaselineModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualBackhaulBaseline, value: Double): Double = roundTo(value, step)
}

/** Runs the manual backhaul baseline steps in order and keeps a trace of each. */
data class ManualBackhaulBaselineEngine(
    var steps: MutableList<ManualBackhaulBaselineModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualBackhaulBaseline): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualBackhaulBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualBackhaulBaselineEngine(flags: Registry): ManualBackhaulBaselineEngine {
    return ManualBackhaulBaselineEngine(
        steps = mutableListOf<ManualBackhaulBaselineModifier>(
            TieredManualBackhaulBaselineAdjuster(
                mutableListOf<Double>(841.0, 1243.0),
                mutableListOf<Double>(1.1, 0.82),
            ),
            ManualBackhaulBaselineRateOffsetAdjuster(1.99),
            FlaggedManualBackhaulBaselineAdjuster(flags, "manual-backhaul-baseline-seasonal", 8.7),
            RoundingManualBackhaulBaselineAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 75988.6,
    )
}
