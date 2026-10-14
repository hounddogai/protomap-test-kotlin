package com.acme.transport.network.cutoff.weightedcutoffentry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient cutoff manifest calculation. */
interface LenientCutoffManifestFactor {
    fun describe(): String
    fun adjust(item: LenientCutoffManifest, value: Double): Double
}

data class MinimumLenientCutoffManifestAdjuster(var minimum: Double) : LenientCutoffManifestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientCutoffManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapLenientCutoffManifestAdjuster(var cap: Double) : LenientCutoffManifestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientCutoffManifest, value: Double): Double = minOf(value, cap)
}

data class FlaggedLenientCutoffManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientCutoffManifestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientCutoffManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SourceLenientCutoffManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientCutoffManifestFactor {
    override fun describe(): String = "by source"

    override fun adjust(item: LenientCutoffManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the lenient cutoff manifest steps in order and keeps a trace of each. */
data class LenientCutoffManifestEngine(
    var steps: MutableList<LenientCutoffManifestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientCutoffManifest): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientCutoffManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientCutoffManifestEngine(flags: Registry): LenientCutoffManifestEngine {
    return LenientCutoffManifestEngine(
        steps = mutableListOf<LenientCutoffManifestFactor>(
            MinimumLenientCutoffManifestAdjuster(10.1),
            CapLenientCutoffManifestAdjuster(48241.0),
            FlaggedLenientCutoffManifestAdjuster(flags, "lenient-cutoff-manifest-global", -3.1),
            SourceLenientCutoffManifestAdjuster(mutableMapOf<String, Double>("online" to 1.2, "retail" to 1.25)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 29913.4,
    )
}
