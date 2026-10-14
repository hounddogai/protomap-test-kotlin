package com.acme.transport.terminal.sortation.tier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the sortation manifest calculation. */
interface SortationManifestModifier {
    fun describe(): String
    fun adjust(item: SortationManifest, value: Double): Double
}

data class MinimumSortationManifestAdjuster(var minimum: Double) : SortationManifestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SortationManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedSortationManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SortationManifestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SortationManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingSortationManifestAdjuster(var step: Double) : SortationManifestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SortationManifest, value: Double): Double = roundTo(value, step)
}

/** Runs the sortation manifest steps in order and keeps a trace of each. */
data class SortationManifestEngine(
    var steps: MutableList<SortationManifestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SortationManifest): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SortationManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSortationManifestEngine(flags: Registry): SortationManifestEngine {
    return SortationManifestEngine(
        steps = mutableListOf<SortationManifestModifier>(
            MinimumSortationManifestAdjuster(40.0),
            FlaggedSortationManifestAdjuster(flags, "sortation-manifest-weighted", -9.2),
            RoundingSortationManifestAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 40785.4,
    )
}
