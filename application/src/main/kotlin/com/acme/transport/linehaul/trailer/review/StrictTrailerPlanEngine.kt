package com.acme.transport.linehaul.trailer.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the strict trailer plan calculation. */
interface StrictTrailerPlanAdjuster {
    fun describe(): String
    fun adjust(item: StrictTrailerPlan, value: Double): Double
}

data class SegmentStrictTrailerPlanAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictTrailerPlanAdjuster {
    override fun describe(): String = "by segment"

    override fun adjust(item: StrictTrailerPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedStrictTrailerPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictTrailerPlanAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictTrailerPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentStrictTrailerPlanAdjuster(var percent: Double) : StrictTrailerPlanAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictTrailerPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredStrictTrailerPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictTrailerPlanAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictTrailerPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the strict trailer plan steps in order and keeps a trace of each. */
data class StrictTrailerPlanEngine(
    var steps: MutableList<StrictTrailerPlanAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: StrictTrailerPlan): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictTrailerPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultStrictTrailerPlanEngine(flags: Registry): StrictTrailerPlanEngine {
    return StrictTrailerPlanEngine(
        steps = mutableListOf<StrictTrailerPlanAdjuster>(
            SegmentStrictTrailerPlanAdjuster(mutableMapOf<String, Double>("central" to 1.33)),
            FlaggedStrictTrailerPlanAdjuster(flags, "strict-trailer-plan-default", -2.4),
            PercentStrictTrailerPlanAdjuster(9.6),
            TieredStrictTrailerPlanAdjuster(
                mutableListOf<Double>(1973.0, 6015.0, 7565.0, 8276.0),
                mutableListOf<Double>(0.94, 1.12, 0.93, 0.82),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 58706.4,
    )
}
