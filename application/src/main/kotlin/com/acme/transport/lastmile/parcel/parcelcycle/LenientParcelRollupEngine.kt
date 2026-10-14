package com.acme.transport.lastmile.parcel.parcelcycle

import com.acme.transport.kit.flags.Registry

/** One step of the lenient parcel rollup calculation. */
interface LenientParcelRollupStep {
    fun describe(): String
    fun adjust(item: LenientParcelRollup, value: Double): Double
}

data class TieredLenientParcelRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientParcelRollupStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientParcelRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LenientParcelRollupCostOffsetAdjuster(var weight: Double) : LenientParcelRollupStep {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: LenientParcelRollup, value: Double): Double = value + item.cost * weight
}

data class PercentLenientParcelRollupAdjuster(var percent: Double) : LenientParcelRollupStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientParcelRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedLenientParcelRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientParcelRollupStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientParcelRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the lenient parcel rollup steps in order and keeps a trace of each. */
data class LenientParcelRollupEngine(
    var steps: MutableList<LenientParcelRollupStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LenientParcelRollup): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientParcelRollupStep = steps[index]
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

    fun evaluateAll(items: MutableList<LenientParcelRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLenientParcelRollupEngine(flags: Registry): LenientParcelRollupEngine {
    return LenientParcelRollupEngine(
        steps = mutableListOf<LenientParcelRollupStep>(
            TieredLenientParcelRollupAdjuster(
                mutableListOf<Double>(1953.0, 3791.0, 8380.0, 9686.0),
                mutableListOf<Double>(0.82, 1.12, 1.13, 1.01),
            ),
            LenientParcelRollupCostOffsetAdjuster(1.94),
            PercentLenientParcelRollupAdjuster(4.4),
            FlaggedLenientParcelRollupAdjuster(flags, "lenient-parcel-rollup-primary", 1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 13240.8,
    )
}
