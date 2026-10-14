package com.acme.transport.emissions.evcharge.score

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the ev charge mapping calculation. */
interface EvChargeMappingFactor {
    fun describe(): String
    fun adjust(item: EvChargeMapping, value: Double): Double
}

data class MinimumEvChargeMappingAdjuster(var minimum: Double) : EvChargeMappingFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EvChargeMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingEvChargeMappingAdjuster(var step: Double) : EvChargeMappingFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EvChargeMapping, value: Double): Double = roundTo(value, step)
}

data class NameEvChargeMappingAdjuster(var factors: MutableMap<String, Double>) : EvChargeMappingFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: EvChargeMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedEvChargeMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EvChargeMappingFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EvChargeMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredEvChargeMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EvChargeMappingFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EvChargeMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapEvChargeMappingAdjuster(var cap: Double) : EvChargeMappingFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EvChargeMapping, value: Double): Double = minOf(value, cap)
}

/** Runs the ev charge mapping steps in order and keeps a trace of each. */
data class EvChargeMappingEngine(
    var steps: MutableList<EvChargeMappingFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: EvChargeMapping): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<EvChargeMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultEvChargeMappingEngine(flags: Registry): EvChargeMappingEngine {
    return EvChargeMappingEngine(
        steps = mutableListOf<EvChargeMappingFactor>(
            MinimumEvChargeMappingAdjuster(0.4),
            RoundingEvChargeMappingAdjuster(0.5),
            NameEvChargeMappingAdjuster(mutableMapOf<String, Double>("east" to 1.34)),
            FlaggedEvChargeMappingAdjuster(flags, "ev-charge-mapping-lenient", -5.9),
            TieredEvChargeMappingAdjuster(
                mutableListOf<Double>(1945.0, 4099.0, 6474.0),
                mutableListOf<Double>(1.1, 0.96, 1.18),
            ),
            CapEvChargeMappingAdjuster(43697.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 26478.0,
    )
}
