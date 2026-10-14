package com.acme.transport.linehaul.laneplan.item

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered lane plan override calculation. */
interface TieredLanePlanOverrideAdjuster {
    fun describe(): String
    fun adjust(item: TieredLanePlanOverride, value: Double): Double
}

data class CategoryTieredLanePlanOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredLanePlanOverrideAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: TieredLanePlanOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapTieredLanePlanOverrideAdjuster(var cap: Double) : TieredLanePlanOverrideAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredLanePlanOverride, value: Double): Double = minOf(value, cap)
}

data class FlaggedTieredLanePlanOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredLanePlanOverrideAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredLanePlanOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredLanePlanOverrideSequenceOffsetAdjuster(var weight: Double) : TieredLanePlanOverrideAdjuster {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: TieredLanePlanOverride, value: Double): Double {
        return value + item.sequence.toDouble() * weight
    }
}

/** Runs the tiered lane plan override steps in order and keeps a trace of each. */
data class TieredLanePlanOverrideEngine(
    var steps: MutableList<TieredLanePlanOverrideAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredLanePlanOverride): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredLanePlanOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredLanePlanOverrideEngine(flags: Registry): TieredLanePlanOverrideEngine {
    return TieredLanePlanOverrideEngine(
        steps = mutableListOf<TieredLanePlanOverrideAdjuster>(
            CategoryTieredLanePlanOverrideAdjuster(mutableMapOf<String, Double>("online" to 0.75)),
            CapTieredLanePlanOverrideAdjuster(27135.0),
            FlaggedTieredLanePlanOverrideAdjuster(flags, "tiered-lane-plan-override-lenient", -6.7),
            TieredLanePlanOverrideSequenceOffsetAdjuster(0.46),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 88389.3,
    )
}
