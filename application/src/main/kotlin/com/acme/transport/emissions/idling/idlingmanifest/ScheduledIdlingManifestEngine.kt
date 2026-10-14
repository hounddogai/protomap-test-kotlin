package com.acme.transport.emissions.idling.idlingmanifest

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled idling manifest calculation. */
interface ScheduledIdlingManifestFactor {
    fun describe(): String
    fun adjust(item: ScheduledIdlingManifest, value: Double): Double
}

data class MinimumScheduledIdlingManifestAdjuster(var minimum: Double) : ScheduledIdlingManifestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledIdlingManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentScheduledIdlingManifestAdjuster(var percent: Double) : ScheduledIdlingManifestFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledIdlingManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapScheduledIdlingManifestAdjuster(var cap: Double) : ScheduledIdlingManifestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledIdlingManifest, value: Double): Double = minOf(value, cap)
}

/** Runs the scheduled idling manifest steps in order and keeps a trace of each. */
data class ScheduledIdlingManifestEngine(
    var steps: MutableList<ScheduledIdlingManifestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledIdlingManifest): Double {
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

    fun evaluateAll(items: MutableList<ScheduledIdlingManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledIdlingManifestEngine(): ScheduledIdlingManifestEngine {
    return ScheduledIdlingManifestEngine(
        steps = mutableListOf<ScheduledIdlingManifestFactor>(
            MinimumScheduledIdlingManifestAdjuster(19.6),
            PercentScheduledIdlingManifestAdjuster(0.9),
            CapScheduledIdlingManifestAdjuster(17130.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 25653.7,
    )
}
