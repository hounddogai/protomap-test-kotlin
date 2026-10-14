package com.acme.transport.network.zone.schedule

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered zone manifest calculation. */
interface TieredZoneManifestStep {
    fun describe(): String
    fun adjust(item: TieredZoneManifest, value: Double): Double
}

data class PercentTieredZoneManifestAdjuster(var percent: Double) : TieredZoneManifestStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredZoneManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapTieredZoneManifestAdjuster(var cap: Double) : TieredZoneManifestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredZoneManifest, value: Double): Double = minOf(value, cap)
}

data class MinimumTieredZoneManifestAdjuster(var minimum: Double) : TieredZoneManifestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredZoneManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the tiered zone manifest steps in order and keeps a trace of each. */
data class TieredZoneManifestEngine(
    var steps: MutableList<TieredZoneManifestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TieredZoneManifest): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredZoneManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTieredZoneManifestEngine(): TieredZoneManifestEngine {
    return TieredZoneManifestEngine(
        steps = mutableListOf<TieredZoneManifestStep>(
            PercentTieredZoneManifestAdjuster(18.8),
            CapTieredZoneManifestAdjuster(6783.0),
            MinimumTieredZoneManifestAdjuster(22.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 29711.8,
    )
}
