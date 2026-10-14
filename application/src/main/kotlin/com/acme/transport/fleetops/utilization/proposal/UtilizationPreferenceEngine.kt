package com.acme.transport.fleetops.utilization.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the utilization preference calculation. */
interface UtilizationPreferenceAdjuster {
    fun describe(): String
    fun adjust(item: UtilizationPreference, value: Double): Double
}

data class RoundingUtilizationPreferenceAdjuster(var step: Double) : UtilizationPreferenceAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: UtilizationPreference, value: Double): Double = roundTo(value, step)
}

data class PercentUtilizationPreferenceAdjuster(var percent: Double) : UtilizationPreferenceAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: UtilizationPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedUtilizationPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : UtilizationPreferenceAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: UtilizationPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumUtilizationPreferenceAdjuster(var minimum: Double) : UtilizationPreferenceAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: UtilizationPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapUtilizationPreferenceAdjuster(var cap: Double) : UtilizationPreferenceAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: UtilizationPreference, value: Double): Double = minOf(value, cap)
}

data class UtilizationPreferenceCreatedAtOffsetAdjuster(var weight: Double) : UtilizationPreferenceAdjuster {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: UtilizationPreference, value: Double): Double {
        return value + item.createdAt.toDouble() * weight
    }
}

/** Runs the utilization preference steps in order and keeps a trace of each. */
data class UtilizationPreferenceEngine(
    var steps: MutableList<UtilizationPreferenceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: UtilizationPreference): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<UtilizationPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultUtilizationPreferenceEngine(flags: Registry): UtilizationPreferenceEngine {
    return UtilizationPreferenceEngine(
        steps = mutableListOf<UtilizationPreferenceAdjuster>(
            RoundingUtilizationPreferenceAdjuster(0.25),
            PercentUtilizationPreferenceAdjuster(24.2),
            FlaggedUtilizationPreferenceAdjuster(flags, "utilization-preference-batched", -8.6),
            MinimumUtilizationPreferenceAdjuster(39.6),
            CapUtilizationPreferenceAdjuster(34455.0),
            UtilizationPreferenceCreatedAtOffsetAdjuster(1.72),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 87793.5,
    )
}
