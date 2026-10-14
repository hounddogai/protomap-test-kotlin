package com.acme.transport.network.cutoff.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capped cutoff target calculation. */
interface CappedCutoffTargetAdjuster {
    fun describe(): String
    fun adjust(item: CappedCutoffTarget, value: Double): Double
}

data class FlaggedCappedCutoffTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedCutoffTargetAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedCutoffTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CappedCutoffTargetMinimumOffsetAdjuster(var weight: Double) : CappedCutoffTargetAdjuster {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: CappedCutoffTarget, value: Double): Double = value + item.minimum * weight
}

data class MinimumCappedCutoffTargetAdjuster(var minimum: Double) : CappedCutoffTargetAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedCutoffTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RegionCappedCutoffTargetAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedCutoffTargetAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: CappedCutoffTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentCappedCutoffTargetAdjuster(var percent: Double) : CappedCutoffTargetAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedCutoffTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the capped cutoff target steps in order and keeps a trace of each. */
data class CappedCutoffTargetEngine(
    var steps: MutableList<CappedCutoffTargetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedCutoffTarget): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedCutoffTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedCutoffTargetEngine(flags: Registry): CappedCutoffTargetEngine {
    return CappedCutoffTargetEngine(
        steps = mutableListOf<CappedCutoffTargetAdjuster>(
            FlaggedCappedCutoffTargetAdjuster(flags, "capped-cutoff-target-weighted", -3.0),
            CappedCutoffTargetMinimumOffsetAdjuster(1.19),
            MinimumCappedCutoffTargetAdjuster(37.9),
            RegionCappedCutoffTargetAdjuster(mutableMapOf<String, Double>("central" to 0.85)),
            PercentCappedCutoffTargetAdjuster(10.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 39018.4,
    )
}
