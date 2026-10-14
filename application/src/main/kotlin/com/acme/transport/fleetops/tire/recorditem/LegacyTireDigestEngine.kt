package com.acme.transport.fleetops.tire.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy tire digest calculation. */
interface LegacyTireDigestModifier {
    fun describe(): String
    fun adjust(item: LegacyTireDigest, value: Double): Double
}

data class FlaggedLegacyTireDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyTireDigestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyTireDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentLegacyTireDigestAdjuster(var percent: Double) : LegacyTireDigestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyTireDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LabelLegacyTireDigestAdjuster(var factors: MutableMap<String, Double>) : LegacyTireDigestModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: LegacyTireDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingLegacyTireDigestAdjuster(var step: Double) : LegacyTireDigestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyTireDigest, value: Double): Double = roundTo(value, step)
}

data class MinimumLegacyTireDigestAdjuster(var minimum: Double) : LegacyTireDigestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyTireDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LegacyTireDigestDistanceKmOffsetAdjuster(var weight: Double) : LegacyTireDigestModifier {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: LegacyTireDigest, value: Double): Double = value + item.distanceKm * weight
}

/** Runs the legacy tire digest steps in order and keeps a trace of each. */
data class LegacyTireDigestEngine(
    var steps: MutableList<LegacyTireDigestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LegacyTireDigest): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyTireDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLegacyTireDigestEngine(flags: Registry): LegacyTireDigestEngine {
    return LegacyTireDigestEngine(
        steps = mutableListOf<LegacyTireDigestModifier>(
            FlaggedLegacyTireDigestAdjuster(flags, "legacy-tire-digest-regional", 2.7),
            PercentLegacyTireDigestAdjuster(18.6),
            LabelLegacyTireDigestAdjuster(mutableMapOf<String, Double>("east" to 0.84, "north" to 0.79)),
            RoundingLegacyTireDigestAdjuster(0.25),
            MinimumLegacyTireDigestAdjuster(13.4),
            LegacyTireDigestDistanceKmOffsetAdjuster(0.26),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 98035.6,
    )
}
