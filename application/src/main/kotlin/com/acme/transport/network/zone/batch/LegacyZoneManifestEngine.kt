package com.acme.transport.network.zone.batch

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy zone manifest calculation. */
interface LegacyZoneManifestAdjuster {
    fun describe(): String
    fun adjust(item: LegacyZoneManifest, value: Double): Double
}

data class LegacyZoneManifestWeightOffsetAdjuster(var weight: Double) : LegacyZoneManifestAdjuster {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: LegacyZoneManifest, value: Double): Double = value + item.weight * weight
}

data class PercentLegacyZoneManifestAdjuster(var percent: Double) : LegacyZoneManifestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyZoneManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingLegacyZoneManifestAdjuster(var step: Double) : LegacyZoneManifestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyZoneManifest, value: Double): Double = roundTo(value, step)
}

data class CapLegacyZoneManifestAdjuster(var cap: Double) : LegacyZoneManifestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyZoneManifest, value: Double): Double = minOf(value, cap)
}

data class LabelLegacyZoneManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyZoneManifestAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: LegacyZoneManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumLegacyZoneManifestAdjuster(var minimum: Double) : LegacyZoneManifestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyZoneManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the legacy zone manifest steps in order and keeps a trace of each. */
data class LegacyZoneManifestEngine(
    var steps: MutableList<LegacyZoneManifestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyZoneManifest): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyZoneManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyZoneManifestEngine(): LegacyZoneManifestEngine {
    return LegacyZoneManifestEngine(
        steps = mutableListOf<LegacyZoneManifestAdjuster>(
            LegacyZoneManifestWeightOffsetAdjuster(1.2),
            PercentLegacyZoneManifestAdjuster(22.7),
            RoundingLegacyZoneManifestAdjuster(0.25),
            CapLegacyZoneManifestAdjuster(17957.0),
            LabelLegacyZoneManifestAdjuster(mutableMapOf<String, Double>("south" to 0.72, "west" to 1.22)),
            MinimumLegacyZoneManifestAdjuster(41.6),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 16036.0,
    )
}
