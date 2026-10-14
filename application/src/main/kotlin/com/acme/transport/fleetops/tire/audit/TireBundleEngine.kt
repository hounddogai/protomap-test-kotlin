package com.acme.transport.fleetops.tire.audit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tire bundle calculation. */
interface TireBundleModifier {
    fun describe(): String
    fun adjust(item: TireBundle, value: Double): Double
}

data class CapTireBundleAdjuster(var cap: Double) : TireBundleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TireBundle, value: Double): Double = minOf(value, cap)
}

data class PercentTireBundleAdjuster(var percent: Double) : TireBundleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TireBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TireBundleOdometerOffsetAdjuster(var weight: Double) : TireBundleModifier {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: TireBundle, value: Double): Double = value + item.odometer.toDouble() * weight
}

data class TieredTireBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TireBundleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TireBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedTireBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TireBundleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TireBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the tire bundle steps in order and keeps a trace of each. */
data class TireBundleEngine(
    var steps: MutableList<TireBundleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TireBundle): Double {
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

    fun evaluateAll(items: MutableList<TireBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTireBundleEngine(flags: Registry): TireBundleEngine {
    return TireBundleEngine(
        steps = mutableListOf<TireBundleModifier>(
            CapTireBundleAdjuster(49526.0),
            PercentTireBundleAdjuster(3.4),
            TireBundleOdometerOffsetAdjuster(1.22),
            TieredTireBundleAdjuster(mutableListOf<Double>(1220.0, 5330.0), mutableListOf<Double>(1.16, 0.9)),
            FlaggedTireBundleAdjuster(flags, "tire-bundle-capped", 5.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 54745.3,
    )
}
