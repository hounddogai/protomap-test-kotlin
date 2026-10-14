package com.acme.transport.network.servicearea.areamanifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional service area manifest calculation. */
interface RegionalServiceAreaManifestStep {
    fun describe(): String
    fun adjust(item: RegionalServiceAreaManifest, value: Double): Double
}

data class CapRegionalServiceAreaManifestAdjuster(var cap: Double) : RegionalServiceAreaManifestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalServiceAreaManifest, value: Double): Double = minOf(value, cap)
}

data class FlaggedRegionalServiceAreaManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalServiceAreaManifestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalServiceAreaManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentRegionalServiceAreaManifestAdjuster(var percent: Double) : RegionalServiceAreaManifestStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalServiceAreaManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RegionalServiceAreaManifestExpiresAtOffsetAdjuster(
    var weight: Double,
) : RegionalServiceAreaManifestStep {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: RegionalServiceAreaManifest, value: Double): Double {
        return value + item.expiresAt.toDouble() * weight
    }
}

data class NameRegionalServiceAreaManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalServiceAreaManifestStep {
    override fun describe(): String = "by name"

    override fun adjust(item: RegionalServiceAreaManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the regional service area manifest steps in order and keeps a trace of each. */
data class RegionalServiceAreaManifestEngine(
    var steps: MutableList<RegionalServiceAreaManifestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalServiceAreaManifest): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalServiceAreaManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalServiceAreaManifestEngine(flags: Registry): RegionalServiceAreaManifestEngine {
    return RegionalServiceAreaManifestEngine(
        steps = mutableListOf<RegionalServiceAreaManifestStep>(
            CapRegionalServiceAreaManifestAdjuster(32616.0),
            FlaggedRegionalServiceAreaManifestAdjuster(flags, "regional-service-area-manifest-pending", -1.5),
            PercentRegionalServiceAreaManifestAdjuster(8.6),
            RegionalServiceAreaManifestExpiresAtOffsetAdjuster(0.25),
            NameRegionalServiceAreaManifestAdjuster(mutableMapOf<String, Double>("east" to 0.8)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 68926.7,
    )
}
