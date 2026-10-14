package com.acme.transport.freightpay.fuelsurcharge.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the legacy fuel surcharge window calculation. */
interface LegacyFuelSurchargeWindowAdjuster {
    fun describe(): String
    fun adjust(item: LegacyFuelSurchargeWindow, value: Double): Double
}

data class CapLegacyFuelSurchargeWindowAdjuster(var cap: Double) : LegacyFuelSurchargeWindowAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyFuelSurchargeWindow, value: Double): Double = minOf(value, cap)
}

data class FlaggedLegacyFuelSurchargeWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyFuelSurchargeWindowAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyFuelSurchargeWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LegacyFuelSurchargeWindowStartsAtOffsetAdjuster(
    var weight: Double,
) : LegacyFuelSurchargeWindowAdjuster {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: LegacyFuelSurchargeWindow, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

/** Runs the legacy fuel surcharge window steps in order and keeps a trace of each. */
data class LegacyFuelSurchargeWindowEngine(
    var steps: MutableList<LegacyFuelSurchargeWindowAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyFuelSurchargeWindow): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyFuelSurchargeWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyFuelSurchargeWindowEngine(flags: Registry): LegacyFuelSurchargeWindowEngine {
    return LegacyFuelSurchargeWindowEngine(
        steps = mutableListOf<LegacyFuelSurchargeWindowAdjuster>(
            CapLegacyFuelSurchargeWindowAdjuster(18728.0),
            FlaggedLegacyFuelSurchargeWindowAdjuster(flags, "legacy-fuel-surcharge-window-strict", -7.9),
            LegacyFuelSurchargeWindowStartsAtOffsetAdjuster(1.78),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 18011.1,
    )
}
