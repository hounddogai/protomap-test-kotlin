package com.acme.transport.lastmile.parcel.cycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the default parcel milestone calculation. */
interface DefaultParcelMilestoneModifier {
    fun describe(): String
    fun adjust(item: DefaultParcelMilestone, value: Double): Double
}

data class DefaultParcelMilestoneExpiresAtOffsetAdjuster(var weight: Double) : DefaultParcelMilestoneModifier {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: DefaultParcelMilestone, value: Double): Double {
        return value + item.expiresAt.toDouble() * weight
    }
}

data class SourceDefaultParcelMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultParcelMilestoneModifier {
    override fun describe(): String = "by source"

    override fun adjust(item: DefaultParcelMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedDefaultParcelMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultParcelMilestoneModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultParcelMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredDefaultParcelMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultParcelMilestoneModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultParcelMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumDefaultParcelMilestoneAdjuster(var minimum: Double) : DefaultParcelMilestoneModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultParcelMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the default parcel milestone steps in order and keeps a trace of each. */
data class DefaultParcelMilestoneEngine(
    var steps: MutableList<DefaultParcelMilestoneModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefaultParcelMilestone): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultParcelMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefaultParcelMilestoneEngine(flags: Registry): DefaultParcelMilestoneEngine {
    return DefaultParcelMilestoneEngine(
        steps = mutableListOf<DefaultParcelMilestoneModifier>(
            DefaultParcelMilestoneExpiresAtOffsetAdjuster(0.81),
            SourceDefaultParcelMilestoneAdjuster(mutableMapOf<String, Double>("east" to 1.08, "retail" to 1.01)),
            FlaggedDefaultParcelMilestoneAdjuster(flags, "default-parcel-milestone-regional", -4.2),
            TieredDefaultParcelMilestoneAdjuster(
                mutableListOf<Double>(4377.0, 4589.0, 5470.0),
                mutableListOf<Double>(0.96, 1.19, 0.86),
            ),
            MinimumDefaultParcelMilestoneAdjuster(42.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 14693.9,
    )
}
