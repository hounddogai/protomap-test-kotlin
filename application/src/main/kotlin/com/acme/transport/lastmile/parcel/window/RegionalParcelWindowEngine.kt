package com.acme.transport.lastmile.parcel.window

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the regional parcel window calculation. */
interface RegionalParcelWindowAdjuster {
    fun describe(): String
    fun adjust(item: RegionalParcelWindow, value: Double): Double
}

data class PercentRegionalParcelWindowAdjuster(var percent: Double) : RegionalParcelWindowAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalParcelWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapRegionalParcelWindowAdjuster(var cap: Double) : RegionalParcelWindowAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalParcelWindow, value: Double): Double = minOf(value, cap)
}

data class FlaggedRegionalParcelWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalParcelWindowAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalParcelWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RegionalParcelWindowDurationMinutesOffsetAdjuster(
    var weight: Double,
) : RegionalParcelWindowAdjuster {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: RegionalParcelWindow, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class RoundingRegionalParcelWindowAdjuster(var step: Double) : RegionalParcelWindowAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalParcelWindow, value: Double): Double = roundTo(value, step)
}

/** Runs the regional parcel window steps in order and keeps a trace of each. */
data class RegionalParcelWindowEngine(
    var steps: MutableList<RegionalParcelWindowAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalParcelWindow): Double {
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

    fun evaluateAll(items: MutableList<RegionalParcelWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalParcelWindowEngine(flags: Registry): RegionalParcelWindowEngine {
    return RegionalParcelWindowEngine(
        steps = mutableListOf<RegionalParcelWindowAdjuster>(
            PercentRegionalParcelWindowAdjuster(22.4),
            CapRegionalParcelWindowAdjuster(10771.0),
            FlaggedRegionalParcelWindowAdjuster(flags, "regional-parcel-window-global", -7.7),
            RegionalParcelWindowDurationMinutesOffsetAdjuster(1.18),
            RoundingRegionalParcelWindowAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 8961.7,
    )
}
