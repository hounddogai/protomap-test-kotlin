package com.acme.transport.emissions.evcharge.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy ev charge mapping calculation. */
interface LegacyEvChargeMappingAdjuster {
    fun describe(): String
    fun adjust(item: LegacyEvChargeMapping, value: Double): Double
}

data class PercentLegacyEvChargeMappingAdjuster(var percent: Double) : LegacyEvChargeMappingAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyEvChargeMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingLegacyEvChargeMappingAdjuster(var step: Double) : LegacyEvChargeMappingAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyEvChargeMapping, value: Double): Double = roundTo(value, step)
}

data class FlaggedLegacyEvChargeMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyEvChargeMappingAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyEvChargeMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredLegacyEvChargeMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyEvChargeMappingAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyEvChargeMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapLegacyEvChargeMappingAdjuster(var cap: Double) : LegacyEvChargeMappingAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyEvChargeMapping, value: Double): Double = minOf(value, cap)
}

data class MinimumLegacyEvChargeMappingAdjuster(var minimum: Double) : LegacyEvChargeMappingAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyEvChargeMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the legacy ev charge mapping steps in order and keeps a trace of each. */
data class LegacyEvChargeMappingEngine(
    var steps: MutableList<LegacyEvChargeMappingAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LegacyEvChargeMapping): Double {
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

    fun evaluateAll(items: MutableList<LegacyEvChargeMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLegacyEvChargeMappingEngine(flags: Registry): LegacyEvChargeMappingEngine {
    return LegacyEvChargeMappingEngine(
        steps = mutableListOf<LegacyEvChargeMappingAdjuster>(
            PercentLegacyEvChargeMappingAdjuster(-12.7),
            RoundingLegacyEvChargeMappingAdjuster(0.25),
            FlaggedLegacyEvChargeMappingAdjuster(flags, "legacy-ev-charge-mapping-capped", -7.4),
            TieredLegacyEvChargeMappingAdjuster(
                mutableListOf<Double>(677.0, 4823.0),
                mutableListOf<Double>(0.97, 1.09),
            ),
            CapLegacyEvChargeMappingAdjuster(46020.0),
            MinimumLegacyEvChargeMappingAdjuster(49.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 60216.8,
    )
}
