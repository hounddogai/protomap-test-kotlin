package com.acme.transport.network.cutoff.cutofftarget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional cutoff override calculation. */
interface RegionalCutoffOverrideAdjuster {
    fun describe(): String
    fun adjust(item: RegionalCutoffOverride, value: Double): Double
}

data class LaneRegionalCutoffOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalCutoffOverrideAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: RegionalCutoffOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentRegionalCutoffOverrideAdjuster(var percent: Double) : RegionalCutoffOverrideAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalCutoffOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RegionalCutoffOverrideBudgetOffsetAdjuster(var weight: Double) : RegionalCutoffOverrideAdjuster {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: RegionalCutoffOverride, value: Double): Double = value + item.budget * weight
}

data class CapRegionalCutoffOverrideAdjuster(var cap: Double) : RegionalCutoffOverrideAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalCutoffOverride, value: Double): Double = minOf(value, cap)
}

data class FlaggedRegionalCutoffOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalCutoffOverrideAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalCutoffOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the regional cutoff override steps in order and keeps a trace of each. */
data class RegionalCutoffOverrideEngine(
    var steps: MutableList<RegionalCutoffOverrideAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalCutoffOverride): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalCutoffOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalCutoffOverrideEngine(flags: Registry): RegionalCutoffOverrideEngine {
    return RegionalCutoffOverrideEngine(
        steps = mutableListOf<RegionalCutoffOverrideAdjuster>(
            LaneRegionalCutoffOverrideAdjuster(
                mutableMapOf<String, Double>("west" to 1.28, "central" to 1.03, "online" to 0.94),
            ),
            PercentRegionalCutoffOverrideAdjuster(-15.0),
            RegionalCutoffOverrideBudgetOffsetAdjuster(0.06),
            CapRegionalCutoffOverrideAdjuster(30425.0),
            FlaggedRegionalCutoffOverrideAdjuster(flags, "regional-cutoff-override-pending", -9.6),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 77300.7,
    )
}
