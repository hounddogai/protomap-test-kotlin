package com.acme.transport.linehaul.laneplan.mapping

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lane plan group calculation. */
interface LanePlanGroupModifier {
    fun describe(): String
    fun adjust(item: LanePlanGroup, value: Double): Double
}

data class TitleLanePlanGroupAdjuster(var factors: MutableMap<String, Double>) : LanePlanGroupModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: LanePlanGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapLanePlanGroupAdjuster(var cap: Double) : LanePlanGroupModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LanePlanGroup, value: Double): Double = minOf(value, cap)
}

data class TieredLanePlanGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LanePlanGroupModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LanePlanGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumLanePlanGroupAdjuster(var minimum: Double) : LanePlanGroupModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LanePlanGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the lane plan group steps in order and keeps a trace of each. */
data class LanePlanGroupEngine(
    var steps: MutableList<LanePlanGroupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LanePlanGroup): Double {
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

    fun evaluateAll(items: MutableList<LanePlanGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLanePlanGroupEngine(): LanePlanGroupEngine {
    return LanePlanGroupEngine(
        steps = mutableListOf<LanePlanGroupModifier>(
            TitleLanePlanGroupAdjuster(mutableMapOf<String, Double>("south" to 1.11, "online" to 0.85)),
            CapLanePlanGroupAdjuster(31937.0),
            TieredLanePlanGroupAdjuster(
                mutableListOf<Double>(571.0, 2319.0, 6177.0, 6377.0),
                mutableListOf<Double>(0.91, 0.91, 0.93, 1.05),
            ),
            MinimumLanePlanGroupAdjuster(11.3),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 59806.6,
    )
}
