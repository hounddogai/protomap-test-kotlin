package com.acme.transport.network.zone.group

import com.acme.transport.kit.numbers.roundTo

/** One step of the zone manifest calculation. */
interface ZoneManifestFactor {
    fun describe(): String
    fun adjust(item: ZoneManifest, value: Double): Double
}

data class RoundingZoneManifestAdjuster(var step: Double) : ZoneManifestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ZoneManifest, value: Double): Double = roundTo(value, step)
}

data class TieredZoneManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ZoneManifestFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ZoneManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentZoneManifestAdjuster(var percent: Double) : ZoneManifestFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ZoneManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapZoneManifestAdjuster(var cap: Double) : ZoneManifestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ZoneManifest, value: Double): Double = minOf(value, cap)
}

data class NameZoneManifestAdjuster(var factors: MutableMap<String, Double>) : ZoneManifestFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: ZoneManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumZoneManifestAdjuster(var minimum: Double) : ZoneManifestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ZoneManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the zone manifest steps in order and keeps a trace of each. */
data class ZoneManifestEngine(
    var steps: MutableList<ZoneManifestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ZoneManifest): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ZoneManifestFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ZoneManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultZoneManifestEngine(): ZoneManifestEngine {
    return ZoneManifestEngine(
        steps = mutableListOf<ZoneManifestFactor>(
            RoundingZoneManifestAdjuster(0.5),
            TieredZoneManifestAdjuster(
                mutableListOf<Double>(4876.0, 6446.0, 7484.0),
                mutableListOf<Double>(1.14, 1.17, 0.8),
            ),
            PercentZoneManifestAdjuster(10.7),
            CapZoneManifestAdjuster(16973.0),
            NameZoneManifestAdjuster(mutableMapOf<String, Double>("online" to 1.01)),
            MinimumZoneManifestAdjuster(14.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 33396.4,
    )
}
