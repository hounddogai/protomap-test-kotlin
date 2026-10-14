package com.acme.transport.network.zone.mapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the zone mapping calculation. */
interface ZoneMappingModifier {
    fun describe(): String
    fun adjust(item: ZoneMapping, value: Double): Double
}

data class FlaggedZoneMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ZoneMappingModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ZoneMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapZoneMappingAdjuster(var cap: Double) : ZoneMappingModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ZoneMapping, value: Double): Double = minOf(value, cap)
}

data class TieredZoneMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ZoneMappingModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ZoneMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingZoneMappingAdjuster(var step: Double) : ZoneMappingModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ZoneMapping, value: Double): Double = roundTo(value, step)
}

/** Runs the zone mapping steps in order and keeps a trace of each. */
data class ZoneMappingEngine(
    var steps: MutableList<ZoneMappingModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ZoneMapping): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ZoneMappingModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ZoneMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultZoneMappingEngine(flags: Registry): ZoneMappingEngine {
    return ZoneMappingEngine(
        steps = mutableListOf<ZoneMappingModifier>(
            FlaggedZoneMappingAdjuster(flags, "zone-mapping-pending", -4.4),
            CapZoneMappingAdjuster(20621.0),
            TieredZoneMappingAdjuster(
                mutableListOf<Double>(1771.0, 3089.0, 8937.0),
                mutableListOf<Double>(1.1, 1.12, 0.84),
            ),
            RoundingZoneMappingAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 1821.6,
    )
}
