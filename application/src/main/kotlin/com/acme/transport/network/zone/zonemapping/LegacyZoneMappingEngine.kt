package com.acme.transport.network.zone.zonemapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy zone mapping calculation. */
interface LegacyZoneMappingModifier {
    fun describe(): String
    fun adjust(item: LegacyZoneMapping, value: Double): Double
}

data class RoundingLegacyZoneMappingAdjuster(var step: Double) : LegacyZoneMappingModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyZoneMapping, value: Double): Double = roundTo(value, step)
}

data class TieredLegacyZoneMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyZoneMappingModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyZoneMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LegacyZoneMappingAttemptsOffsetAdjuster(var weight: Double) : LegacyZoneMappingModifier {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: LegacyZoneMapping, value: Double): Double = value + item.attempts.toDouble() * weight
}

data class FlaggedLegacyZoneMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyZoneMappingModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyZoneMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapLegacyZoneMappingAdjuster(var cap: Double) : LegacyZoneMappingModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyZoneMapping, value: Double): Double = minOf(value, cap)
}

data class CountryLegacyZoneMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyZoneMappingModifier {
    override fun describe(): String = "by country"

    override fun adjust(item: LegacyZoneMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the legacy zone mapping steps in order and keeps a trace of each. */
data class LegacyZoneMappingEngine(
    var steps: MutableList<LegacyZoneMappingModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyZoneMapping): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyZoneMappingModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyZoneMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyZoneMappingEngine(flags: Registry): LegacyZoneMappingEngine {
    return LegacyZoneMappingEngine(
        steps = mutableListOf<LegacyZoneMappingModifier>(
            RoundingLegacyZoneMappingAdjuster(0.25),
            TieredLegacyZoneMappingAdjuster(
                mutableListOf<Double>(1455.0, 1869.0, 5052.0, 8107.0),
                mutableListOf<Double>(0.84, 1.16, 0.81, 1.09),
            ),
            LegacyZoneMappingAttemptsOffsetAdjuster(1.39),
            FlaggedLegacyZoneMappingAdjuster(flags, "legacy-zone-mapping-rolling", -4.4),
            CapLegacyZoneMappingAdjuster(33530.0),
            CountryLegacyZoneMappingAdjuster(mutableMapOf<String, Double>("online" to 1.07)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 7686.2,
    )
}
