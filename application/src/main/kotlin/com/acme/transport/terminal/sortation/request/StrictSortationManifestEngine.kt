package com.acme.transport.terminal.sortation.request

import com.acme.transport.kit.flags.Registry

/** One step of the strict sortation manifest calculation. */
interface StrictSortationManifestModifier {
    fun describe(): String
    fun adjust(item: StrictSortationManifest, value: Double): Double
}

data class RegionStrictSortationManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictSortationManifestModifier {
    override fun describe(): String = "by region"

    override fun adjust(item: StrictSortationManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class StrictSortationManifestThresholdOffsetAdjuster(
    var weight: Double,
) : StrictSortationManifestModifier {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: StrictSortationManifest, value: Double): Double = value + item.threshold * weight
}

data class MinimumStrictSortationManifestAdjuster(var minimum: Double) : StrictSortationManifestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictSortationManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedStrictSortationManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictSortationManifestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictSortationManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the strict sortation manifest steps in order and keeps a trace of each. */
data class StrictSortationManifestEngine(
    var steps: MutableList<StrictSortationManifestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictSortationManifest): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictSortationManifestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<StrictSortationManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictSortationManifestEngine(flags: Registry): StrictSortationManifestEngine {
    return StrictSortationManifestEngine(
        steps = mutableListOf<StrictSortationManifestModifier>(
            RegionStrictSortationManifestAdjuster(
                mutableMapOf<String, Double>("north" to 1.04, "online" to 0.82, "central" to 0.71),
            ),
            StrictSortationManifestThresholdOffsetAdjuster(0.84),
            MinimumStrictSortationManifestAdjuster(36.9),
            FlaggedStrictSortationManifestAdjuster(flags, "strict-sortation-manifest-weighted", -5.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 91952.2,
    )
}
