package com.acme.transport.customs.clearance.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback clearance manifest calculation. */
interface FallbackClearanceManifestAdjuster {
    fun describe(): String
    fun adjust(item: FallbackClearanceManifest, value: Double): Double
}

data class PercentFallbackClearanceManifestAdjuster(var percent: Double) : FallbackClearanceManifestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackClearanceManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredFallbackClearanceManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackClearanceManifestAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackClearanceManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingFallbackClearanceManifestAdjuster(var step: Double) : FallbackClearanceManifestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackClearanceManifest, value: Double): Double = roundTo(value, step)
}

data class FlaggedFallbackClearanceManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackClearanceManifestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackClearanceManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the fallback clearance manifest steps in order and keeps a trace of each. */
data class FallbackClearanceManifestEngine(
    var steps: MutableList<FallbackClearanceManifestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackClearanceManifest): Double {
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

    fun evaluateAll(items: MutableList<FallbackClearanceManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackClearanceManifestEngine(flags: Registry): FallbackClearanceManifestEngine {
    return FallbackClearanceManifestEngine(
        steps = mutableListOf<FallbackClearanceManifestAdjuster>(
            PercentFallbackClearanceManifestAdjuster(17.4),
            TieredFallbackClearanceManifestAdjuster(
                mutableListOf<Double>(1259.0, 4941.0, 5665.0, 8165.0),
                mutableListOf<Double>(0.83, 1.0, 1.04, 0.92),
            ),
            RoundingFallbackClearanceManifestAdjuster(0.5),
            FlaggedFallbackClearanceManifestAdjuster(flags, "fallback-clearance-manifest-capped", -6.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 40769.3,
    )
}
