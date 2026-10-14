package com.acme.transport.fleetops.tire.tiremanifest

import com.acme.transport.kit.flags.Registry

/** One step of the legacy tire threshold calculation. */
interface LegacyTireThresholdModifier {
    fun describe(): String
    fun adjust(item: LegacyTireThreshold, value: Double): Double
}

data class FlaggedLegacyTireThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyTireThresholdModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyTireThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LocaleLegacyTireThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyTireThresholdModifier {
    override fun describe(): String = "by locale"

    override fun adjust(item: LegacyTireThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentLegacyTireThresholdAdjuster(var percent: Double) : LegacyTireThresholdModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyTireThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapLegacyTireThresholdAdjuster(var cap: Double) : LegacyTireThresholdModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyTireThreshold, value: Double): Double = minOf(value, cap)
}

/** Runs the legacy tire threshold steps in order and keeps a trace of each. */
data class LegacyTireThresholdEngine(
    var steps: MutableList<LegacyTireThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyTireThreshold): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyTireThresholdModifier = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyTireThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyTireThresholdEngine(flags: Registry): LegacyTireThresholdEngine {
    return LegacyTireThresholdEngine(
        steps = mutableListOf<LegacyTireThresholdModifier>(
            FlaggedLegacyTireThresholdAdjuster(flags, "legacy-tire-threshold-weighted", 2.1),
            LocaleLegacyTireThresholdAdjuster(mutableMapOf<String, Double>("east" to 1.25, "online" to 0.84)),
            PercentLegacyTireThresholdAdjuster(-9.0),
            CapLegacyTireThresholdAdjuster(30931.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 64848.1,
    )
}
