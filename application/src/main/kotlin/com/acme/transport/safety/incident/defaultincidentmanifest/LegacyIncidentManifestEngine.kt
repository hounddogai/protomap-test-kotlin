package com.acme.transport.safety.incident.defaultincidentmanifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy incident manifest calculation. */
interface LegacyIncidentManifestFactor {
    fun describe(): String
    fun adjust(item: LegacyIncidentManifest, value: Double): Double
}

data class FlaggedLegacyIncidentManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyIncidentManifestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyIncidentManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapLegacyIncidentManifestAdjuster(var cap: Double) : LegacyIncidentManifestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyIncidentManifest, value: Double): Double = minOf(value, cap)
}

data class TieredLegacyIncidentManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyIncidentManifestFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyIncidentManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentLegacyIncidentManifestAdjuster(var percent: Double) : LegacyIncidentManifestFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyIncidentManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingLegacyIncidentManifestAdjuster(var step: Double) : LegacyIncidentManifestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyIncidentManifest, value: Double): Double = roundTo(value, step)
}

data class TitleLegacyIncidentManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyIncidentManifestFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: LegacyIncidentManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the legacy incident manifest steps in order and keeps a trace of each. */
data class LegacyIncidentManifestEngine(
    var steps: MutableList<LegacyIncidentManifestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyIncidentManifest): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyIncidentManifestFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyIncidentManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyIncidentManifestEngine(flags: Registry): LegacyIncidentManifestEngine {
    return LegacyIncidentManifestEngine(
        steps = mutableListOf<LegacyIncidentManifestFactor>(
            FlaggedLegacyIncidentManifestAdjuster(flags, "legacy-incident-manifest-pending", 7.9),
            CapLegacyIncidentManifestAdjuster(23859.0),
            TieredLegacyIncidentManifestAdjuster(
                mutableListOf<Double>(3376.0, 4412.0, 6154.0),
                mutableListOf<Double>(0.9, 0.92, 0.88),
            ),
            PercentLegacyIncidentManifestAdjuster(-17.4),
            RoundingLegacyIncidentManifestAdjuster(5.0),
            TitleLegacyIncidentManifestAdjuster(
                mutableMapOf<String, Double>("retail" to 1.38, "central" to 0.98, "west" to 0.83),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 54044.8,
    )
}
