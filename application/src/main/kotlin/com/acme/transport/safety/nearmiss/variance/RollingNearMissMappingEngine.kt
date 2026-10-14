package com.acme.transport.safety.nearmiss.variance

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling near miss mapping calculation. */
interface RollingNearMissMappingModifier {
    fun describe(): String
    fun adjust(item: RollingNearMissMapping, value: Double): Double
}

data class PercentRollingNearMissMappingAdjuster(var percent: Double) : RollingNearMissMappingModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingNearMissMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CountryRollingNearMissMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingNearMissMappingModifier {
    override fun describe(): String = "by country"

    override fun adjust(item: RollingNearMissMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RollingNearMissMappingEndsAtOffsetAdjuster(var weight: Double) : RollingNearMissMappingModifier {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: RollingNearMissMapping, value: Double): Double = value + item.endsAt.toDouble() * weight
}

data class TieredRollingNearMissMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingNearMissMappingModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingNearMissMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapRollingNearMissMappingAdjuster(var cap: Double) : RollingNearMissMappingModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingNearMissMapping, value: Double): Double = minOf(value, cap)
}

/** Runs the rolling near miss mapping steps in order and keeps a trace of each. */
data class RollingNearMissMappingEngine(
    var steps: MutableList<RollingNearMissMappingModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RollingNearMissMapping): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingNearMissMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRollingNearMissMappingEngine(): RollingNearMissMappingEngine {
    return RollingNearMissMappingEngine(
        steps = mutableListOf<RollingNearMissMappingModifier>(
            PercentRollingNearMissMappingAdjuster(-9.1),
            CountryRollingNearMissMappingAdjuster(mutableMapOf<String, Double>("west" to 1.17, "east" to 1.39)),
            RollingNearMissMappingEndsAtOffsetAdjuster(1.52),
            TieredRollingNearMissMappingAdjuster(
                mutableListOf<Double>(5153.0, 7631.0),
                mutableListOf<Double>(0.94, 1.1),
            ),
            CapRollingNearMissMappingAdjuster(15008.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 13448.4,
    )
}
