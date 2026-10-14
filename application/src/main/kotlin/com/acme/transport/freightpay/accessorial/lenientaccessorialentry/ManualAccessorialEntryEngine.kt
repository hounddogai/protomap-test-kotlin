package com.acme.transport.freightpay.accessorial.lenientaccessorialentry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual accessorial entry calculation. */
interface ManualAccessorialEntryModifier {
    fun describe(): String
    fun adjust(item: ManualAccessorialEntry, value: Double): Double
}

data class CapManualAccessorialEntryAdjuster(var cap: Double) : ManualAccessorialEntryModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualAccessorialEntry, value: Double): Double = minOf(value, cap)
}

data class PercentManualAccessorialEntryAdjuster(var percent: Double) : ManualAccessorialEntryModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualAccessorialEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedManualAccessorialEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualAccessorialEntryModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualAccessorialEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingManualAccessorialEntryAdjuster(var step: Double) : ManualAccessorialEntryModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualAccessorialEntry, value: Double): Double = roundTo(value, step)
}

data class MinimumManualAccessorialEntryAdjuster(var minimum: Double) : ManualAccessorialEntryModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualAccessorialEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the manual accessorial entry steps in order and keeps a trace of each. */
data class ManualAccessorialEntryEngine(
    var steps: MutableList<ManualAccessorialEntryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualAccessorialEntry): Double {
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

    fun evaluateAll(items: MutableList<ManualAccessorialEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualAccessorialEntryEngine(flags: Registry): ManualAccessorialEntryEngine {
    return ManualAccessorialEntryEngine(
        steps = mutableListOf<ManualAccessorialEntryModifier>(
            CapManualAccessorialEntryAdjuster(42329.0),
            PercentManualAccessorialEntryAdjuster(0.8),
            FlaggedManualAccessorialEntryAdjuster(flags, "manual-accessorial-entry-legacy", -2.4),
            RoundingManualAccessorialEntryAdjuster(0.5),
            MinimumManualAccessorialEntryAdjuster(21.9),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 48002.7,
    )
}
