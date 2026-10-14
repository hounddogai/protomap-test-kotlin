package com.acme.transport.freightpay.fuelsurcharge.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the global fuel surcharge threshold calculation. */
interface GlobalFuelSurchargeThresholdStep {
    fun describe(): String
    fun adjust(item: GlobalFuelSurchargeThreshold, value: Double): Double
}

data class FlaggedGlobalFuelSurchargeThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalFuelSurchargeThresholdStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalFuelSurchargeThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredGlobalFuelSurchargeThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalFuelSurchargeThresholdStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalFuelSurchargeThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapGlobalFuelSurchargeThresholdAdjuster(var cap: Double) : GlobalFuelSurchargeThresholdStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalFuelSurchargeThreshold, value: Double): Double = minOf(value, cap)
}

data class MinimumGlobalFuelSurchargeThresholdAdjuster(var minimum: Double) : GlobalFuelSurchargeThresholdStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalFuelSurchargeThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the global fuel surcharge threshold steps in order and keeps a trace of each. */
data class GlobalFuelSurchargeThresholdEngine(
    var steps: MutableList<GlobalFuelSurchargeThresholdStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalFuelSurchargeThreshold): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalFuelSurchargeThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalFuelSurchargeThresholdEngine(flags: Registry): GlobalFuelSurchargeThresholdEngine {
    return GlobalFuelSurchargeThresholdEngine(
        steps = mutableListOf<GlobalFuelSurchargeThresholdStep>(
            FlaggedGlobalFuelSurchargeThresholdAdjuster(flags, "global-fuel-surcharge-threshold-strict", 2.7),
            TieredGlobalFuelSurchargeThresholdAdjuster(
                mutableListOf<Double>(4247.0, 6844.0, 9365.0),
                mutableListOf<Double>(0.87, 0.87, 1.08),
            ),
            CapGlobalFuelSurchargeThresholdAdjuster(45369.0),
            MinimumGlobalFuelSurchargeThresholdAdjuster(5.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 89205.7,
    )
}
