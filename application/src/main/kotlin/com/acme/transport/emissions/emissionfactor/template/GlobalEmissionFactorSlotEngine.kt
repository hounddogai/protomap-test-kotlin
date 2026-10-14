package com.acme.transport.emissions.emissionfactor.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the global emission factor slot calculation. */
interface GlobalEmissionFactorSlotModifier {
    fun describe(): String
    fun adjust(item: GlobalEmissionFactorSlot, value: Double): Double
}

data class CapGlobalEmissionFactorSlotAdjuster(var cap: Double) : GlobalEmissionFactorSlotModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalEmissionFactorSlot, value: Double): Double = minOf(value, cap)
}

data class PercentGlobalEmissionFactorSlotAdjuster(var percent: Double) : GlobalEmissionFactorSlotModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalEmissionFactorSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class GlobalEmissionFactorSlotRevisionOffsetAdjuster(
    var weight: Double,
) : GlobalEmissionFactorSlotModifier {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: GlobalEmissionFactorSlot, value: Double): Double {
        return value + item.revision.toDouble() * weight
    }
}

data class TieredGlobalEmissionFactorSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalEmissionFactorSlotModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalEmissionFactorSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedGlobalEmissionFactorSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalEmissionFactorSlotModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalEmissionFactorSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the global emission factor slot steps in order and keeps a trace of each. */
data class GlobalEmissionFactorSlotEngine(
    var steps: MutableList<GlobalEmissionFactorSlotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalEmissionFactorSlot): Double {
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

    fun evaluateAll(items: MutableList<GlobalEmissionFactorSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalEmissionFactorSlotEngine(flags: Registry): GlobalEmissionFactorSlotEngine {
    return GlobalEmissionFactorSlotEngine(
        steps = mutableListOf<GlobalEmissionFactorSlotModifier>(
            CapGlobalEmissionFactorSlotAdjuster(33181.0),
            PercentGlobalEmissionFactorSlotAdjuster(12.4),
            GlobalEmissionFactorSlotRevisionOffsetAdjuster(0.17),
            TieredGlobalEmissionFactorSlotAdjuster(
                mutableListOf<Double>(158.0, 2154.0, 4851.0, 6344.0),
                mutableListOf<Double>(0.87, 0.89, 0.85, 0.85),
            ),
            FlaggedGlobalEmissionFactorSlotAdjuster(flags, "global-emission-factor-slot-fallback", -2.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 71357.4,
    )
}
