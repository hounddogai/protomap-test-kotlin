package com.acme.transport.customs.tariff.overrideitem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tariff override calculation. */
interface TariffOverrideStep {
    fun describe(): String
    fun adjust(item: TariffOverride, value: Double): Double
}

data class CapTariffOverrideAdjuster(var cap: Double) : TariffOverrideStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TariffOverride, value: Double): Double = minOf(value, cap)
}

data class FlaggedTariffOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TariffOverrideStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TariffOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredTariffOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TariffOverrideStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TariffOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the tariff override steps in order and keeps a trace of each. */
data class TariffOverrideEngine(
    var steps: MutableList<TariffOverrideStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TariffOverride): Double {
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

    fun evaluateAll(items: MutableList<TariffOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTariffOverrideEngine(flags: Registry): TariffOverrideEngine {
    return TariffOverrideEngine(
        steps = mutableListOf<TariffOverrideStep>(
            CapTariffOverrideAdjuster(15730.0),
            FlaggedTariffOverrideAdjuster(flags, "tariff-override-rolling", -3.2),
            TieredTariffOverrideAdjuster(
                mutableListOf<Double>(2448.0, 3071.0, 7037.0, 8938.0),
                mutableListOf<Double>(0.95, 1.18, 1.09, 0.83),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 58610.7,
    )
}
