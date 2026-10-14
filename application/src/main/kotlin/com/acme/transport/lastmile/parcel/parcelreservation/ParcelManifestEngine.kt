package com.acme.transport.lastmile.parcel.parcelreservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the parcel manifest calculation. */
interface ParcelManifestAdjuster {
    fun describe(): String
    fun adjust(item: ParcelManifest, value: Double): Double
}

data class TieredParcelManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ParcelManifestAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ParcelManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapParcelManifestAdjuster(var cap: Double) : ParcelManifestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ParcelManifest, value: Double): Double = minOf(value, cap)
}

data class FlaggedParcelManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ParcelManifestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ParcelManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ParcelManifestRetriesOffsetAdjuster(var weight: Double) : ParcelManifestAdjuster {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: ParcelManifest, value: Double): Double = value + item.retries.toDouble() * weight
}

/** Runs the parcel manifest steps in order and keeps a trace of each. */
data class ParcelManifestEngine(
    var steps: MutableList<ParcelManifestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ParcelManifest): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ParcelManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultParcelManifestEngine(flags: Registry): ParcelManifestEngine {
    return ParcelManifestEngine(
        steps = mutableListOf<ParcelManifestAdjuster>(
            TieredParcelManifestAdjuster(
                mutableListOf<Double>(3827.0, 4786.0),
                mutableListOf<Double>(0.96, 1.19),
            ),
            CapParcelManifestAdjuster(29364.0),
            FlaggedParcelManifestAdjuster(flags, "parcel-manifest-pending", -7.8),
            ParcelManifestRetriesOffsetAdjuster(1.47),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 16382.2,
    )
}
