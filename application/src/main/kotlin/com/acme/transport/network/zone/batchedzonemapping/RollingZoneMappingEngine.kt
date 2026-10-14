package com.acme.transport.network.zone.batchedzonemapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling zone mapping calculation. */
interface RollingZoneMappingFactor {
    fun describe(): String
    fun adjust(item: RollingZoneMapping, value: Double): Double
}

data class RollingZoneMappingMaximumOffsetAdjuster(var weight: Double) : RollingZoneMappingFactor {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: RollingZoneMapping, value: Double): Double = value + item.maximum * weight
}

data class TieredRollingZoneMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingZoneMappingFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingZoneMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedRollingZoneMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingZoneMappingFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingZoneMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRollingZoneMappingAdjuster(var cap: Double) : RollingZoneMappingFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingZoneMapping, value: Double): Double = minOf(value, cap)
}

data class PercentRollingZoneMappingAdjuster(var percent: Double) : RollingZoneMappingFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingZoneMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRollingZoneMappingAdjuster(var minimum: Double) : RollingZoneMappingFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingZoneMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the rolling zone mapping steps in order and keeps a trace of each. */
data class RollingZoneMappingEngine(
    var steps: MutableList<RollingZoneMappingFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingZoneMapping): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingZoneMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingZoneMappingEngine(flags: Registry): RollingZoneMappingEngine {
    return RollingZoneMappingEngine(
        steps = mutableListOf<RollingZoneMappingFactor>(
            RollingZoneMappingMaximumOffsetAdjuster(1.55),
            TieredRollingZoneMappingAdjuster(
                mutableListOf<Double>(3643.0, 5509.0, 6649.0),
                mutableListOf<Double>(1.03, 1.01, 1.09),
            ),
            FlaggedRollingZoneMappingAdjuster(flags, "rolling-zone-mapping-default", 9.1),
            CapRollingZoneMappingAdjuster(7510.0),
            PercentRollingZoneMappingAdjuster(-10.7),
            MinimumRollingZoneMappingAdjuster(24.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 88173.5,
    )
}
