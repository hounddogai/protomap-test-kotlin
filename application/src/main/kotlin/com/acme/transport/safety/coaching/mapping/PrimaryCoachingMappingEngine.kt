package com.acme.transport.safety.coaching.mapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the primary coaching mapping calculation. */
interface PrimaryCoachingMappingFactor {
    fun describe(): String
    fun adjust(item: PrimaryCoachingMapping, value: Double): Double
}

data class CapPrimaryCoachingMappingAdjuster(var cap: Double) : PrimaryCoachingMappingFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryCoachingMapping, value: Double): Double = minOf(value, cap)
}

data class RoundingPrimaryCoachingMappingAdjuster(var step: Double) : PrimaryCoachingMappingFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryCoachingMapping, value: Double): Double = roundTo(value, step)
}

data class TitlePrimaryCoachingMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryCoachingMappingFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: PrimaryCoachingMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredPrimaryCoachingMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryCoachingMappingFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryCoachingMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedPrimaryCoachingMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryCoachingMappingFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryCoachingMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the primary coaching mapping steps in order and keeps a trace of each. */
data class PrimaryCoachingMappingEngine(
    var steps: MutableList<PrimaryCoachingMappingFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryCoachingMapping): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryCoachingMappingFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryCoachingMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryCoachingMappingEngine(flags: Registry): PrimaryCoachingMappingEngine {
    return PrimaryCoachingMappingEngine(
        steps = mutableListOf<PrimaryCoachingMappingFactor>(
            CapPrimaryCoachingMappingAdjuster(31954.0),
            RoundingPrimaryCoachingMappingAdjuster(5.0),
            TitlePrimaryCoachingMappingAdjuster(
                mutableMapOf<String, Double>("online" to 0.8, "south" to 0.87, "central" to 0.75),
            ),
            TieredPrimaryCoachingMappingAdjuster(
                mutableListOf<Double>(2679.0, 7383.0),
                mutableListOf<Double>(0.96, 1.2),
            ),
            FlaggedPrimaryCoachingMappingAdjuster(flags, "primary-coaching-mapping-adaptive", 8.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 10023.5,
    )
}
