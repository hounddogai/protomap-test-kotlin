package com.acme.transport.network.cutoff.weightedcutoffentry

import com.acme.transport.kit.flags.Registry

/** One step of the cutoff override calculation. */
interface CutoffOverrideAdjuster {
    fun describe(): String
    fun adjust(item: CutoffOverride, value: Double): Double
}

data class UnitCutoffOverrideAdjuster(var factors: MutableMap<String, Double>) : CutoffOverrideAdjuster {
    override fun describe(): String = "by unit"

    override fun adjust(item: CutoffOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentCutoffOverrideAdjuster(var percent: Double) : CutoffOverrideAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CutoffOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedCutoffOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CutoffOverrideAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CutoffOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredCutoffOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CutoffOverrideAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CutoffOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapCutoffOverrideAdjuster(var cap: Double) : CutoffOverrideAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CutoffOverride, value: Double): Double = minOf(value, cap)
}

data class MinimumCutoffOverrideAdjuster(var minimum: Double) : CutoffOverrideAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CutoffOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the cutoff override steps in order and keeps a trace of each. */
data class CutoffOverrideEngine(
    var steps: MutableList<CutoffOverrideAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CutoffOverride): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CutoffOverrideAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CutoffOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCutoffOverrideEngine(flags: Registry): CutoffOverrideEngine {
    return CutoffOverrideEngine(
        steps = mutableListOf<CutoffOverrideAdjuster>(
            UnitCutoffOverrideAdjuster(
                mutableMapOf<String, Double>("online" to 0.79, "central" to 0.79, "retail" to 1.12),
            ),
            PercentCutoffOverrideAdjuster(-6.5),
            FlaggedCutoffOverrideAdjuster(flags, "cutoff-override-tiered", 1.4),
            TieredCutoffOverrideAdjuster(
                mutableListOf<Double>(4460.0, 7509.0),
                mutableListOf<Double>(0.83, 0.85),
            ),
            CapCutoffOverrideAdjuster(20417.0),
            MinimumCutoffOverrideAdjuster(22.4),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 61725.8,
    )
}
