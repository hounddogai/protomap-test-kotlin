package com.acme.transport.emissions.fuelburn.rule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient fuel burn window calculation. */
interface LenientFuelBurnWindowAdjuster {
    fun describe(): String
    fun adjust(item: LenientFuelBurnWindow, value: Double): Double
}

data class LenientFuelBurnWindowRetriesOffsetAdjuster(var weight: Double) : LenientFuelBurnWindowAdjuster {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: LenientFuelBurnWindow, value: Double): Double = value + item.retries.toDouble() * weight
}

data class TitleLenientFuelBurnWindowAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientFuelBurnWindowAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: LenientFuelBurnWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentLenientFuelBurnWindowAdjuster(var percent: Double) : LenientFuelBurnWindowAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientFuelBurnWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedLenientFuelBurnWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientFuelBurnWindowAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientFuelBurnWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapLenientFuelBurnWindowAdjuster(var cap: Double) : LenientFuelBurnWindowAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientFuelBurnWindow, value: Double): Double = minOf(value, cap)
}

data class RoundingLenientFuelBurnWindowAdjuster(var step: Double) : LenientFuelBurnWindowAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientFuelBurnWindow, value: Double): Double = roundTo(value, step)
}

/** Runs the lenient fuel burn window steps in order and keeps a trace of each. */
data class LenientFuelBurnWindowEngine(
    var steps: MutableList<LenientFuelBurnWindowAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientFuelBurnWindow): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientFuelBurnWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientFuelBurnWindowEngine(flags: Registry): LenientFuelBurnWindowEngine {
    return LenientFuelBurnWindowEngine(
        steps = mutableListOf<LenientFuelBurnWindowAdjuster>(
            LenientFuelBurnWindowRetriesOffsetAdjuster(1.0),
            TitleLenientFuelBurnWindowAdjuster(
                mutableMapOf<String, Double>("west" to 1.16, "central" to 0.97, "online" to 1.08),
            ),
            PercentLenientFuelBurnWindowAdjuster(-9.5),
            FlaggedLenientFuelBurnWindowAdjuster(flags, "lenient-fuel-burn-window-global", 9.2),
            CapLenientFuelBurnWindowAdjuster(17761.0),
            RoundingLenientFuelBurnWindowAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 24742.8,
    )
}
