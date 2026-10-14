package com.acme.transport.safety.incident.manifest

import com.acme.transport.kit.flags.Registry

/** One step of the incident manifest calculation. */
interface IncidentManifestAdjuster {
    fun describe(): String
    fun adjust(item: IncidentManifest, value: Double): Double
}

data class IncidentManifestCapacityOffsetAdjuster(var weight: Double) : IncidentManifestAdjuster {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: IncidentManifest, value: Double): Double = value + item.capacity.toDouble() * weight
}

data class DepotIncidentManifestAdjuster(var factors: MutableMap<String, Double>) : IncidentManifestAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: IncidentManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredIncidentManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IncidentManifestAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IncidentManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapIncidentManifestAdjuster(var cap: Double) : IncidentManifestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: IncidentManifest, value: Double): Double = minOf(value, cap)
}

data class FlaggedIncidentManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : IncidentManifestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: IncidentManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the incident manifest steps in order and keeps a trace of each. */
data class IncidentManifestEngine(
    var steps: MutableList<IncidentManifestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: IncidentManifest): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: IncidentManifestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<IncidentManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultIncidentManifestEngine(flags: Registry): IncidentManifestEngine {
    return IncidentManifestEngine(
        steps = mutableListOf<IncidentManifestAdjuster>(
            IncidentManifestCapacityOffsetAdjuster(1.4),
            DepotIncidentManifestAdjuster(mutableMapOf<String, Double>("south" to 1.17, "north" to 0.88)),
            TieredIncidentManifestAdjuster(
                mutableListOf<Double>(990.0, 6724.0, 8434.0),
                mutableListOf<Double>(1.01, 1.05, 1.06),
            ),
            CapIncidentManifestAdjuster(24683.0),
            FlaggedIncidentManifestAdjuster(flags, "incident-manifest-seasonal", 7.6),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 73221.3,
    )
}
