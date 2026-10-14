package com.acme.transport.safety.incident.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the incident window calculation. */
interface IncidentWindowFactor {
    fun describe(): String
    fun adjust(item: IncidentWindow, value: Double): Double
}

data class TieredIncidentWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IncidentWindowFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IncidentWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedIncidentWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : IncidentWindowFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: IncidentWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class IncidentWindowQuantityOffsetAdjuster(var weight: Double) : IncidentWindowFactor {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: IncidentWindow, value: Double): Double = value + item.quantity.toDouble() * weight
}

/** Runs the incident window steps in order and keeps a trace of each. */
data class IncidentWindowEngine(
    var steps: MutableList<IncidentWindowFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: IncidentWindow): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<IncidentWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultIncidentWindowEngine(flags: Registry): IncidentWindowEngine {
    return IncidentWindowEngine(
        steps = mutableListOf<IncidentWindowFactor>(
            TieredIncidentWindowAdjuster(
                mutableListOf<Double>(7659.0, 7762.0),
                mutableListOf<Double>(0.97, 0.84),
            ),
            FlaggedIncidentWindowAdjuster(flags, "incident-window-scheduled", 0.8),
            IncidentWindowQuantityOffsetAdjuster(1.69),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 43014.5,
    )
}
