package com.acme.transport.crewing.availability.audit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy availability group calculation. */
interface LegacyAvailabilityGroupFactor {
    fun describe(): String
    fun adjust(item: LegacyAvailabilityGroup, value: Double): Double
}

data class LegacyAvailabilityGroupQuantityOffsetAdjuster(var weight: Double) : LegacyAvailabilityGroupFactor {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: LegacyAvailabilityGroup, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

data class RoundingLegacyAvailabilityGroupAdjuster(var step: Double) : LegacyAvailabilityGroupFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyAvailabilityGroup, value: Double): Double = roundTo(value, step)
}

data class FlaggedLegacyAvailabilityGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyAvailabilityGroupFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyAvailabilityGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapLegacyAvailabilityGroupAdjuster(var cap: Double) : LegacyAvailabilityGroupFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyAvailabilityGroup, value: Double): Double = minOf(value, cap)
}

data class TieredLegacyAvailabilityGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyAvailabilityGroupFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyAvailabilityGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the legacy availability group steps in order and keeps a trace of each. */
data class LegacyAvailabilityGroupEngine(
    var steps: MutableList<LegacyAvailabilityGroupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyAvailabilityGroup): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyAvailabilityGroupFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyAvailabilityGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyAvailabilityGroupEngine(flags: Registry): LegacyAvailabilityGroupEngine {
    return LegacyAvailabilityGroupEngine(
        steps = mutableListOf<LegacyAvailabilityGroupFactor>(
            LegacyAvailabilityGroupQuantityOffsetAdjuster(0.41),
            RoundingLegacyAvailabilityGroupAdjuster(5.0),
            FlaggedLegacyAvailabilityGroupAdjuster(flags, "legacy-availability-group-capped", 5.4),
            CapLegacyAvailabilityGroupAdjuster(31145.0),
            TieredLegacyAvailabilityGroupAdjuster(
                mutableListOf<Double>(1661.0, 2334.0, 8375.0, 8790.0),
                mutableListOf<Double>(0.83, 1.14, 1.04, 1.11),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 27427.7,
    )
}
