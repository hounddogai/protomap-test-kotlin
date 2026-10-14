package com.acme.transport.emissions.evcharge.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient ev charge window calculation. */
interface LenientEvChargeWindowModifier {
    fun describe(): String
    fun adjust(item: LenientEvChargeWindow, value: Double): Double
}

data class TieredLenientEvChargeWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientEvChargeWindowModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientEvChargeWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedLenientEvChargeWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientEvChargeWindowModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientEvChargeWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LenientEvChargeWindowQuantityOffsetAdjuster(var weight: Double) : LenientEvChargeWindowModifier {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: LenientEvChargeWindow, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

data class PercentLenientEvChargeWindowAdjuster(var percent: Double) : LenientEvChargeWindowModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientEvChargeWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapLenientEvChargeWindowAdjuster(var cap: Double) : LenientEvChargeWindowModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientEvChargeWindow, value: Double): Double = minOf(value, cap)
}

/** Runs the lenient ev charge window steps in order and keeps a trace of each. */
data class LenientEvChargeWindowEngine(
    var steps: MutableList<LenientEvChargeWindowModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientEvChargeWindow): Double {
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

    fun evaluateAll(items: MutableList<LenientEvChargeWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientEvChargeWindowEngine(flags: Registry): LenientEvChargeWindowEngine {
    return LenientEvChargeWindowEngine(
        steps = mutableListOf<LenientEvChargeWindowModifier>(
            TieredLenientEvChargeWindowAdjuster(
                mutableListOf<Double>(4516.0, 5563.0, 6319.0, 9039.0),
                mutableListOf<Double>(0.83, 1.19, 1.15, 1.06),
            ),
            FlaggedLenientEvChargeWindowAdjuster(flags, "lenient-ev-charge-window-weighted", 4.0),
            LenientEvChargeWindowQuantityOffsetAdjuster(1.93),
            PercentLenientEvChargeWindowAdjuster(-14.9),
            CapLenientEvChargeWindowAdjuster(21638.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 96309.7,
    )
}
