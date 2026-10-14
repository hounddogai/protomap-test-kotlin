package com.acme.transport.emissions.idling.window

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the idling window calculation. */
interface IdlingWindowStep {
    fun describe(): String
    fun adjust(item: IdlingWindow, value: Double): Double
}

data class FlaggedIdlingWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : IdlingWindowStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: IdlingWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapIdlingWindowAdjuster(var cap: Double) : IdlingWindowStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: IdlingWindow, value: Double): Double = minOf(value, cap)
}

data class CountryIdlingWindowAdjuster(var factors: MutableMap<String, Double>) : IdlingWindowStep {
    override fun describe(): String = "by country"

    override fun adjust(item: IdlingWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredIdlingWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IdlingWindowStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IdlingWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the idling window steps in order and keeps a trace of each. */
data class IdlingWindowEngine(
    var steps: MutableList<IdlingWindowStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: IdlingWindow): Double {
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

    fun evaluateAll(items: MutableList<IdlingWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultIdlingWindowEngine(flags: Registry): IdlingWindowEngine {
    return IdlingWindowEngine(
        steps = mutableListOf<IdlingWindowStep>(
            FlaggedIdlingWindowAdjuster(flags, "idling-window-pending", 1.7),
            CapIdlingWindowAdjuster(44687.0),
            CountryIdlingWindowAdjuster(mutableMapOf<String, Double>("online" to 1.1)),
            TieredIdlingWindowAdjuster(
                mutableListOf<Double>(351.0, 450.0, 4032.0, 8153.0),
                mutableListOf<Double>(0.84, 1.17, 0.88, 0.95),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 55597.4,
    )
}
