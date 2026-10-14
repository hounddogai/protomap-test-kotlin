package com.acme.transport.linehaul.backhaul.backhaulbatch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the backhaul mapping calculation. */
interface BackhaulMappingModifier {
    fun describe(): String
    fun adjust(item: BackhaulMapping, value: Double): Double
}

data class CapBackhaulMappingAdjuster(var cap: Double) : BackhaulMappingModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BackhaulMapping, value: Double): Double = minOf(value, cap)
}

data class FlaggedBackhaulMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BackhaulMappingModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BackhaulMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredBackhaulMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BackhaulMappingModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BackhaulMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class BackhaulMappingWeightOffsetAdjuster(var weight: Double) : BackhaulMappingModifier {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: BackhaulMapping, value: Double): Double = value + item.weight * weight
}

/** Runs the backhaul mapping steps in order and keeps a trace of each. */
data class BackhaulMappingEngine(
    var steps: MutableList<BackhaulMappingModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BackhaulMapping): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BackhaulMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBackhaulMappingEngine(flags: Registry): BackhaulMappingEngine {
    return BackhaulMappingEngine(
        steps = mutableListOf<BackhaulMappingModifier>(
            CapBackhaulMappingAdjuster(35295.0),
            FlaggedBackhaulMappingAdjuster(flags, "backhaul-mapping-rolling", -4.7),
            TieredBackhaulMappingAdjuster(
                mutableListOf<Double>(778.0, 5608.0, 8068.0, 8809.0),
                mutableListOf<Double>(0.82, 0.94, 1.13, 0.94),
            ),
            BackhaulMappingWeightOffsetAdjuster(1.52),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 10360.0,
    )
}
