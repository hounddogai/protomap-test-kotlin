package com.acme.transport.crewing.availability.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient availability manifest calculation. */
interface LenientAvailabilityManifestStep {
    fun describe(): String
    fun adjust(item: LenientAvailabilityManifest, value: Double): Double
}

data class PercentLenientAvailabilityManifestAdjuster(var percent: Double) : LenientAvailabilityManifestStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientAvailabilityManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapLenientAvailabilityManifestAdjuster(var cap: Double) : LenientAvailabilityManifestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientAvailabilityManifest, value: Double): Double = minOf(value, cap)
}

data class TieredLenientAvailabilityManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientAvailabilityManifestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientAvailabilityManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingLenientAvailabilityManifestAdjuster(var step: Double) : LenientAvailabilityManifestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientAvailabilityManifest, value: Double): Double = roundTo(value, step)
}

data class FlaggedLenientAvailabilityManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientAvailabilityManifestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientAvailabilityManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumLenientAvailabilityManifestAdjuster(var minimum: Double) : LenientAvailabilityManifestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientAvailabilityManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the lenient availability manifest steps in order and keeps a trace of each. */
data class LenientAvailabilityManifestEngine(
    var steps: MutableList<LenientAvailabilityManifestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientAvailabilityManifest): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientAvailabilityManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientAvailabilityManifestEngine(flags: Registry): LenientAvailabilityManifestEngine {
    return LenientAvailabilityManifestEngine(
        steps = mutableListOf<LenientAvailabilityManifestStep>(
            PercentLenientAvailabilityManifestAdjuster(-23.2),
            CapLenientAvailabilityManifestAdjuster(6787.0),
            TieredLenientAvailabilityManifestAdjuster(
                mutableListOf<Double>(2253.0, 4214.0, 6667.0),
                mutableListOf<Double>(1.14, 0.93, 1.11),
            ),
            RoundingLenientAvailabilityManifestAdjuster(5.0),
            FlaggedLenientAvailabilityManifestAdjuster(flags, "lenient-availability-manifest-capped", 5.2),
            MinimumLenientAvailabilityManifestAdjuster(36.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 48289.1,
    )
}
