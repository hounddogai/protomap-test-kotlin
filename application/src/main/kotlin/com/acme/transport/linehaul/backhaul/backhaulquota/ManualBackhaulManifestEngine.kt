package com.acme.transport.linehaul.backhaul.backhaulquota

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual backhaul manifest calculation. */
interface ManualBackhaulManifestStep {
    fun describe(): String
    fun adjust(item: ManualBackhaulManifest, value: Double): Double
}

data class MinimumManualBackhaulManifestAdjuster(var minimum: Double) : ManualBackhaulManifestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualBackhaulManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ManualBackhaulManifestStopCountOffsetAdjuster(var weight: Double) : ManualBackhaulManifestStep {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: ManualBackhaulManifest, value: Double): Double {
        return value + item.stopCount.toDouble() * weight
    }
}

data class LaneManualBackhaulManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualBackhaulManifestStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: ManualBackhaulManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredManualBackhaulManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualBackhaulManifestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualBackhaulManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapManualBackhaulManifestAdjuster(var cap: Double) : ManualBackhaulManifestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualBackhaulManifest, value: Double): Double = minOf(value, cap)
}

/** Runs the manual backhaul manifest steps in order and keeps a trace of each. */
data class ManualBackhaulManifestEngine(
    var steps: MutableList<ManualBackhaulManifestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualBackhaulManifest): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualBackhaulManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualBackhaulManifestEngine(): ManualBackhaulManifestEngine {
    return ManualBackhaulManifestEngine(
        steps = mutableListOf<ManualBackhaulManifestStep>(
            MinimumManualBackhaulManifestAdjuster(34.3),
            ManualBackhaulManifestStopCountOffsetAdjuster(0.8),
            LaneManualBackhaulManifestAdjuster(
                mutableMapOf<String, Double>("east" to 0.91, "central" to 1.29, "south" to 1.3),
            ),
            TieredManualBackhaulManifestAdjuster(
                mutableListOf<Double>(751.0, 2188.0, 6318.0),
                mutableListOf<Double>(1.16, 0.92, 0.91),
            ),
            CapManualBackhaulManifestAdjuster(49528.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 5259.7,
    )
}
