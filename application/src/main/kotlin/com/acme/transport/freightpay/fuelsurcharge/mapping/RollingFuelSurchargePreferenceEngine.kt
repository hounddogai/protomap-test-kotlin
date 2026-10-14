package com.acme.transport.freightpay.fuelsurcharge.mapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling fuel surcharge preference calculation. */
interface RollingFuelSurchargePreferenceModifier {
    fun describe(): String
    fun adjust(item: RollingFuelSurchargePreference, value: Double): Double
}

data class RoundingRollingFuelSurchargePreferenceAdjuster(
    var step: Double,
) : RollingFuelSurchargePreferenceModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingFuelSurchargePreference, value: Double): Double = roundTo(value, step)
}

data class FlaggedRollingFuelSurchargePreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingFuelSurchargePreferenceModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingFuelSurchargePreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRollingFuelSurchargePreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingFuelSurchargePreferenceModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingFuelSurchargePreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the rolling fuel surcharge preference steps in order and keeps a trace of each. */
data class RollingFuelSurchargePreferenceEngine(
    var steps: MutableList<RollingFuelSurchargePreferenceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingFuelSurchargePreference): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingFuelSurchargePreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingFuelSurchargePreferenceEngine(flags: Registry): RollingFuelSurchargePreferenceEngine {
    return RollingFuelSurchargePreferenceEngine(
        steps = mutableListOf<RollingFuelSurchargePreferenceModifier>(
            RoundingRollingFuelSurchargePreferenceAdjuster(1.0),
            FlaggedRollingFuelSurchargePreferenceAdjuster(
                flags,
                "rolling-fuel-surcharge-preference-tiered",
                -6.4,
            ),
            TieredRollingFuelSurchargePreferenceAdjuster(
                mutableListOf<Double>(758.0, 8330.0),
                mutableListOf<Double>(1.18, 0.83),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 60785.8,
    )
}
