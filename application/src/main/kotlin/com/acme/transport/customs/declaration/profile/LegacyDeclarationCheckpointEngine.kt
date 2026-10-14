package com.acme.transport.customs.declaration.profile

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the legacy declaration checkpoint calculation. */
interface LegacyDeclarationCheckpointStep {
    fun describe(): String
    fun adjust(item: LegacyDeclarationCheckpoint, value: Double): Double
}

data class TieredLegacyDeclarationCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyDeclarationCheckpointStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyDeclarationCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumLegacyDeclarationCheckpointAdjuster(var minimum: Double) : LegacyDeclarationCheckpointStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyDeclarationCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameLegacyDeclarationCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyDeclarationCheckpointStep {
    override fun describe(): String = "by name"

    override fun adjust(item: LegacyDeclarationCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentLegacyDeclarationCheckpointAdjuster(var percent: Double) : LegacyDeclarationCheckpointStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyDeclarationCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LegacyDeclarationCheckpointVersionOffsetAdjuster(
    var weight: Double,
) : LegacyDeclarationCheckpointStep {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: LegacyDeclarationCheckpoint, value: Double): Double {
        return value + item.version.toDouble() * weight
    }
}

data class CapLegacyDeclarationCheckpointAdjuster(var cap: Double) : LegacyDeclarationCheckpointStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyDeclarationCheckpoint, value: Double): Double = minOf(value, cap)
}

/** Runs the legacy declaration checkpoint steps in order and keeps a trace of each. */
data class LegacyDeclarationCheckpointEngine(
    var steps: MutableList<LegacyDeclarationCheckpointStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyDeclarationCheckpoint): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyDeclarationCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyDeclarationCheckpointEngine(): LegacyDeclarationCheckpointEngine {
    return LegacyDeclarationCheckpointEngine(
        steps = mutableListOf<LegacyDeclarationCheckpointStep>(
            TieredLegacyDeclarationCheckpointAdjuster(
                mutableListOf<Double>(3236.0, 9464.0),
                mutableListOf<Double>(1.07, 0.8),
            ),
            MinimumLegacyDeclarationCheckpointAdjuster(30.3),
            NameLegacyDeclarationCheckpointAdjuster(
                mutableMapOf<String, Double>("east" to 0.74, "online" to 1.32, "central" to 0.82),
            ),
            PercentLegacyDeclarationCheckpointAdjuster(21.1),
            LegacyDeclarationCheckpointVersionOffsetAdjuster(0.08),
            CapLegacyDeclarationCheckpointAdjuster(33905.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 63718.3,
    )
}
