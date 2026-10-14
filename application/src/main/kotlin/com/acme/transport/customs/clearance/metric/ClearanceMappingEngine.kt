package com.acme.transport.customs.clearance.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the clearance mapping calculation. */
interface ClearanceMappingAdjuster {
    fun describe(): String
    fun adjust(item: ClearanceMapping, value: Double): Double
}

data class RoundingClearanceMappingAdjuster(var step: Double) : ClearanceMappingAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ClearanceMapping, value: Double): Double = roundTo(value, step)
}

data class CapClearanceMappingAdjuster(var cap: Double) : ClearanceMappingAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ClearanceMapping, value: Double): Double = minOf(value, cap)
}

data class ClearanceMappingLimitOffsetAdjuster(var weight: Double) : ClearanceMappingAdjuster {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: ClearanceMapping, value: Double): Double = value + item.limit.toDouble() * weight
}

data class FlaggedClearanceMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ClearanceMappingAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ClearanceMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredClearanceMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ClearanceMappingAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ClearanceMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleClearanceMappingAdjuster(var factors: MutableMap<String, Double>) : ClearanceMappingAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: ClearanceMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the clearance mapping steps in order and keeps a trace of each. */
data class ClearanceMappingEngine(
    var steps: MutableList<ClearanceMappingAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ClearanceMapping): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ClearanceMappingAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ClearanceMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultClearanceMappingEngine(flags: Registry): ClearanceMappingEngine {
    return ClearanceMappingEngine(
        steps = mutableListOf<ClearanceMappingAdjuster>(
            RoundingClearanceMappingAdjuster(0.25),
            CapClearanceMappingAdjuster(33477.0),
            ClearanceMappingLimitOffsetAdjuster(0.02),
            FlaggedClearanceMappingAdjuster(flags, "clearance-mapping-global", 5.4),
            TieredClearanceMappingAdjuster(
                mutableListOf<Double>(4017.0, 4237.0, 6957.0),
                mutableListOf<Double>(1.12, 1.06, 1.17),
            ),
            TitleClearanceMappingAdjuster(
                mutableMapOf<String, Double>("south" to 0.92, "online" to 1.16, "west" to 0.91),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 3083.7,
    )
}
