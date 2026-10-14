package com.acme.transport.customs.hold.fallbackholdaudit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual hold window calculation. */
interface ManualHoldWindowFactor {
    fun describe(): String
    fun adjust(item: ManualHoldWindow, value: Double): Double
}

data class PercentManualHoldWindowAdjuster(var percent: Double) : ManualHoldWindowFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualHoldWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingManualHoldWindowAdjuster(var step: Double) : ManualHoldWindowFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualHoldWindow, value: Double): Double = roundTo(value, step)
}

data class ManualHoldWindowQuantityOffsetAdjuster(var weight: Double) : ManualHoldWindowFactor {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: ManualHoldWindow, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class TieredManualHoldWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualHoldWindowFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualHoldWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedManualHoldWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualHoldWindowFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualHoldWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CountryManualHoldWindowAdjuster(var factors: MutableMap<String, Double>) : ManualHoldWindowFactor {
    override fun describe(): String = "by country"

    override fun adjust(item: ManualHoldWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the manual hold window steps in order and keeps a trace of each. */
data class ManualHoldWindowEngine(
    var steps: MutableList<ManualHoldWindowFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualHoldWindow): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualHoldWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualHoldWindowEngine(flags: Registry): ManualHoldWindowEngine {
    return ManualHoldWindowEngine(
        steps = mutableListOf<ManualHoldWindowFactor>(
            PercentManualHoldWindowAdjuster(20.4),
            RoundingManualHoldWindowAdjuster(5.0),
            ManualHoldWindowQuantityOffsetAdjuster(0.96),
            TieredManualHoldWindowAdjuster(
                mutableListOf<Double>(191.0, 2357.0, 3131.0, 6822.0),
                mutableListOf<Double>(0.97, 1.08, 1.15, 1.06),
            ),
            FlaggedManualHoldWindowAdjuster(flags, "manual-hold-window-default", 9.5),
            CountryManualHoldWindowAdjuster(mutableMapOf<String, Double>("online" to 1.35)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 58228.4,
    )
}
