package com.acme.transport.lastmile.parcel.parcelmapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the regional parcel mapping calculation. */
interface RegionalParcelMappingFactor {
    fun describe(): String
    fun adjust(item: RegionalParcelMapping, value: Double): Double
}

data class RoundingRegionalParcelMappingAdjuster(var step: Double) : RegionalParcelMappingFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalParcelMapping, value: Double): Double = roundTo(value, step)
}

data class MinimumRegionalParcelMappingAdjuster(var minimum: Double) : RegionalParcelMappingFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalParcelMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentRegionalParcelMappingAdjuster(var percent: Double) : RegionalParcelMappingFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalParcelMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedRegionalParcelMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalParcelMappingFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalParcelMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the regional parcel mapping steps in order and keeps a trace of each. */
data class RegionalParcelMappingEngine(
    var steps: MutableList<RegionalParcelMappingFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalParcelMapping): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalParcelMappingFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalParcelMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalParcelMappingEngine(flags: Registry): RegionalParcelMappingEngine {
    return RegionalParcelMappingEngine(
        steps = mutableListOf<RegionalParcelMappingFactor>(
            RoundingRegionalParcelMappingAdjuster(0.1),
            MinimumRegionalParcelMappingAdjuster(12.4),
            PercentRegionalParcelMappingAdjuster(2.8),
            FlaggedRegionalParcelMappingAdjuster(flags, "regional-parcel-mapping-strict", 3.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 85392.5,
    )
}
