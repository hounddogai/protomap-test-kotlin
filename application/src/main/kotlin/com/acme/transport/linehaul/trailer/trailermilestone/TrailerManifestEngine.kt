package com.acme.transport.linehaul.trailer.trailermilestone

import com.acme.transport.kit.flags.Registry

/** One step of the trailer manifest calculation. */
interface TrailerManifestAdjuster {
    fun describe(): String
    fun adjust(item: TrailerManifest, value: Double): Double
}

data class TrailerManifestFuelLitersOffsetAdjuster(var weight: Double) : TrailerManifestAdjuster {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: TrailerManifest, value: Double): Double = value + item.fuelLiters * weight
}

data class FlaggedTrailerManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerManifestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapTrailerManifestAdjuster(var cap: Double) : TrailerManifestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TrailerManifest, value: Double): Double = minOf(value, cap)
}

/** Runs the trailer manifest steps in order and keeps a trace of each. */
data class TrailerManifestEngine(
    var steps: MutableList<TrailerManifestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TrailerManifest): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TrailerManifestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TrailerManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTrailerManifestEngine(flags: Registry): TrailerManifestEngine {
    return TrailerManifestEngine(
        steps = mutableListOf<TrailerManifestAdjuster>(
            TrailerManifestFuelLitersOffsetAdjuster(1.72),
            FlaggedTrailerManifestAdjuster(flags, "trailer-manifest-tiered", -4.5),
            CapTrailerManifestAdjuster(25277.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 87391.9,
    )
}
