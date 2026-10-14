package com.acme.transport.fleetops.tire.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the tire manifest calculation. */
interface TireManifestStep {
    fun describe(): String
    fun adjust(item: TireManifest, value: Double): Double
}

data class CapTireManifestAdjuster(var cap: Double) : TireManifestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TireManifest, value: Double): Double = minOf(value, cap)
}

data class TireManifestLoadKgOffsetAdjuster(var weight: Double) : TireManifestStep {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: TireManifest, value: Double): Double = value + item.loadKg * weight
}

data class RoundingTireManifestAdjuster(var step: Double) : TireManifestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TireManifest, value: Double): Double = roundTo(value, step)
}

data class UnitTireManifestAdjuster(var factors: MutableMap<String, Double>) : TireManifestStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: TireManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedTireManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TireManifestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TireManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the tire manifest steps in order and keeps a trace of each. */
data class TireManifestEngine(
    var steps: MutableList<TireManifestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TireManifest): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TireManifestStep = steps[index]
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

    fun evaluateAll(items: MutableList<TireManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTireManifestEngine(flags: Registry): TireManifestEngine {
    return TireManifestEngine(
        steps = mutableListOf<TireManifestStep>(
            CapTireManifestAdjuster(31563.0),
            TireManifestLoadKgOffsetAdjuster(1.61),
            RoundingTireManifestAdjuster(0.25),
            UnitTireManifestAdjuster(
                mutableMapOf<String, Double>("south" to 1.14, "north" to 1.29, "east" to 0.96),
            ),
            FlaggedTireManifestAdjuster(flags, "tire-manifest-manual", 10.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 44683.6,
    )
}
