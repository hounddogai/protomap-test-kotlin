package com.acme.transport.lastmile.parcel.parcelcycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the default parcel manifest calculation. */
interface DefaultParcelManifestAdjuster {
    fun describe(): String
    fun adjust(item: DefaultParcelManifest, value: Double): Double
}

data class RoundingDefaultParcelManifestAdjuster(var step: Double) : DefaultParcelManifestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultParcelManifest, value: Double): Double = roundTo(value, step)
}

data class FlaggedDefaultParcelManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultParcelManifestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultParcelManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapDefaultParcelManifestAdjuster(var cap: Double) : DefaultParcelManifestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultParcelManifest, value: Double): Double = minOf(value, cap)
}

/** Runs the default parcel manifest steps in order and keeps a trace of each. */
data class DefaultParcelManifestEngine(
    var steps: MutableList<DefaultParcelManifestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefaultParcelManifest): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultParcelManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefaultParcelManifestEngine(flags: Registry): DefaultParcelManifestEngine {
    return DefaultParcelManifestEngine(
        steps = mutableListOf<DefaultParcelManifestAdjuster>(
            RoundingDefaultParcelManifestAdjuster(0.5),
            FlaggedDefaultParcelManifestAdjuster(flags, "default-parcel-manifest-weighted", 2.8),
            CapDefaultParcelManifestAdjuster(857.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 60900.6,
    )
}
