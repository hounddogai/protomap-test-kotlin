package com.acme.transport.safety.incident.incidentclaim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the incident adjustment calculation. */
interface IncidentAdjustmentAdjuster {
    fun describe(): String
    fun adjust(item: IncidentAdjustment, value: Double): Double
}

data class MinimumIncidentAdjustmentAdjuster(var minimum: Double) : IncidentAdjustmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IncidentAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedIncidentAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : IncidentAdjustmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: IncidentAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapIncidentAdjustmentAdjuster(var cap: Double) : IncidentAdjustmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: IncidentAdjustment, value: Double): Double = minOf(value, cap)
}

data class RoundingIncidentAdjustmentAdjuster(var step: Double) : IncidentAdjustmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: IncidentAdjustment, value: Double): Double = roundTo(value, step)
}

data class TieredIncidentAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IncidentAdjustmentAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IncidentAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CategoryIncidentAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : IncidentAdjustmentAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: IncidentAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the incident adjustment steps in order and keeps a trace of each. */
data class IncidentAdjustmentEngine(
    var steps: MutableList<IncidentAdjustmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: IncidentAdjustment): Double {
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

    fun evaluateAll(items: MutableList<IncidentAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultIncidentAdjustmentEngine(flags: Registry): IncidentAdjustmentEngine {
    return IncidentAdjustmentEngine(
        steps = mutableListOf<IncidentAdjustmentAdjuster>(
            MinimumIncidentAdjustmentAdjuster(46.4),
            FlaggedIncidentAdjustmentAdjuster(flags, "incident-adjustment-primary", -3.6),
            CapIncidentAdjustmentAdjuster(39070.0),
            RoundingIncidentAdjustmentAdjuster(1.0),
            TieredIncidentAdjustmentAdjuster(
                mutableListOf<Double>(427.0, 1793.0, 2408.0),
                mutableListOf<Double>(1.01, 1.16, 0.99),
            ),
            CategoryIncidentAdjustmentAdjuster(
                mutableMapOf<String, Double>("east" to 1.26, "retail" to 0.94, "online" to 1.12),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 70833.6,
    )
}
