package com.acme.transport.terminal.traileryard.manifest

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the trailer yard manifest calculation. */
interface TrailerYardManifestAdjuster {
    fun describe(): String
    fun adjust(item: TrailerYardManifest, value: Double): Double
}

data class TrailerYardManifestGraceDaysOffsetAdjuster(var weight: Double) : TrailerYardManifestAdjuster {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: TrailerYardManifest, value: Double): Double = value + item.graceDays.toDouble() * weight
}

data class TitleTrailerYardManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : TrailerYardManifestAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: TrailerYardManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumTrailerYardManifestAdjuster(var minimum: Double) : TrailerYardManifestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TrailerYardManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredTrailerYardManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TrailerYardManifestAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TrailerYardManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the trailer yard manifest steps in order and keeps a trace of each. */
data class TrailerYardManifestEngine(
    var steps: MutableList<TrailerYardManifestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TrailerYardManifest): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TrailerYardManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTrailerYardManifestEngine(): TrailerYardManifestEngine {
    return TrailerYardManifestEngine(
        steps = mutableListOf<TrailerYardManifestAdjuster>(
            TrailerYardManifestGraceDaysOffsetAdjuster(1.62),
            TitleTrailerYardManifestAdjuster(
                mutableMapOf<String, Double>("online" to 1.28, "north" to 0.95, "east" to 0.86),
            ),
            MinimumTrailerYardManifestAdjuster(44.2),
            TieredTrailerYardManifestAdjuster(
                mutableListOf<Double>(2600.0, 3706.0, 3898.0, 7271.0),
                mutableListOf<Double>(1.04, 0.94, 0.81, 0.96),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 24504.6,
    )
}
