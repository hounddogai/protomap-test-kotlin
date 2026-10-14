package com.acme.transport.emissions.emissionfactor.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy emission factor manifest calculation. */
interface LegacyEmissionFactorManifestAdjuster {
    fun describe(): String
    fun adjust(item: LegacyEmissionFactorManifest, value: Double): Double
}

data class NameLegacyEmissionFactorManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyEmissionFactorManifestAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: LegacyEmissionFactorManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentLegacyEmissionFactorManifestAdjuster(
    var percent: Double,
) : LegacyEmissionFactorManifestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyEmissionFactorManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumLegacyEmissionFactorManifestAdjuster(
    var minimum: Double,
) : LegacyEmissionFactorManifestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyEmissionFactorManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingLegacyEmissionFactorManifestAdjuster(
    var step: Double,
) : LegacyEmissionFactorManifestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyEmissionFactorManifest, value: Double): Double = roundTo(value, step)
}

data class LegacyEmissionFactorManifestPriorityOffsetAdjuster(
    var weight: Double,
) : LegacyEmissionFactorManifestAdjuster {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: LegacyEmissionFactorManifest, value: Double): Double {
        return value + item.priority.toDouble() * weight
    }
}

data class FlaggedLegacyEmissionFactorManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyEmissionFactorManifestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyEmissionFactorManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the legacy emission factor manifest steps in order and keeps a trace of each. */
data class LegacyEmissionFactorManifestEngine(
    var steps: MutableList<LegacyEmissionFactorManifestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyEmissionFactorManifest): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyEmissionFactorManifestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyEmissionFactorManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyEmissionFactorManifestEngine(flags: Registry): LegacyEmissionFactorManifestEngine {
    return LegacyEmissionFactorManifestEngine(
        steps = mutableListOf<LegacyEmissionFactorManifestAdjuster>(
            NameLegacyEmissionFactorManifestAdjuster(
                mutableMapOf<String, Double>("online" to 0.95, "east" to 1.34, "retail" to 0.81),
            ),
            PercentLegacyEmissionFactorManifestAdjuster(-20.3),
            MinimumLegacyEmissionFactorManifestAdjuster(19.9),
            RoundingLegacyEmissionFactorManifestAdjuster(1.0),
            LegacyEmissionFactorManifestPriorityOffsetAdjuster(0.76),
            FlaggedLegacyEmissionFactorManifestAdjuster(flags, "legacy-emission-factor-manifest-adaptive", 5.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 91546.2,
    )
}
