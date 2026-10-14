package com.acme.transport.terminal.dwell.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling dwell mapping calculation. */
interface RollingDwellMappingFactor {
    fun describe(): String
    fun adjust(item: RollingDwellMapping, value: Double): Double
}

data class RollingDwellMappingRetriesOffsetAdjuster(var weight: Double) : RollingDwellMappingFactor {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: RollingDwellMapping, value: Double): Double = value + item.retries.toDouble() * weight
}

data class CapRollingDwellMappingAdjuster(var cap: Double) : RollingDwellMappingFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingDwellMapping, value: Double): Double = minOf(value, cap)
}

data class TieredRollingDwellMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingDwellMappingFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingDwellMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumRollingDwellMappingAdjuster(var minimum: Double) : RollingDwellMappingFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingDwellMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedRollingDwellMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingDwellMappingFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingDwellMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LaneRollingDwellMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingDwellMappingFactor {
    override fun describe(): String = "by lane"

    override fun adjust(item: RollingDwellMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the rolling dwell mapping steps in order and keeps a trace of each. */
data class RollingDwellMappingEngine(
    var steps: MutableList<RollingDwellMappingFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingDwellMapping): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingDwellMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingDwellMappingEngine(flags: Registry): RollingDwellMappingEngine {
    return RollingDwellMappingEngine(
        steps = mutableListOf<RollingDwellMappingFactor>(
            RollingDwellMappingRetriesOffsetAdjuster(1.48),
            CapRollingDwellMappingAdjuster(34481.0),
            TieredRollingDwellMappingAdjuster(
                mutableListOf<Double>(2325.0, 7804.0),
                mutableListOf<Double>(1.02, 0.84),
            ),
            MinimumRollingDwellMappingAdjuster(33.6),
            FlaggedRollingDwellMappingAdjuster(flags, "rolling-dwell-mapping-adaptive", -8.1),
            LaneRollingDwellMappingAdjuster(
                mutableMapOf<String, Double>("north" to 1.15, "south" to 0.83, "online" to 0.74),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 8075.4,
    )
}
