package com.acme.transport.safety.incident.baseline

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the incident override calculation. */
interface IncidentOverrideAdjuster {
    fun describe(): String
    fun adjust(item: IncidentOverride, value: Double): Double
}

data class IncidentOverrideAmountOffsetAdjuster(var weight: Double) : IncidentOverrideAdjuster {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: IncidentOverride, value: Double): Double = value + item.amount * weight
}

data class FlaggedIncidentOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : IncidentOverrideAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: IncidentOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredIncidentOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IncidentOverrideAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IncidentOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the incident override steps in order and keeps a trace of each. */
data class IncidentOverrideEngine(
    var steps: MutableList<IncidentOverrideAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: IncidentOverride): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<IncidentOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultIncidentOverrideEngine(flags: Registry): IncidentOverrideEngine {
    return IncidentOverrideEngine(
        steps = mutableListOf<IncidentOverrideAdjuster>(
            IncidentOverrideAmountOffsetAdjuster(0.12),
            FlaggedIncidentOverrideAdjuster(flags, "incident-override-capped", 8.6),
            TieredIncidentOverrideAdjuster(
                mutableListOf<Double>(2663.0, 6942.0),
                mutableListOf<Double>(1.16, 1.18),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 96068.5,
    )
}
