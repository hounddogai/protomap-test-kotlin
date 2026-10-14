package com.acme.transport.crewing.swap.entry

/** One step of the fallback swap manifest calculation. */
interface FallbackSwapManifestModifier {
    fun describe(): String
    fun adjust(item: FallbackSwapManifest, value: Double): Double
}

data class MinimumFallbackSwapManifestAdjuster(var minimum: Double) : FallbackSwapManifestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackSwapManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RegionFallbackSwapManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackSwapManifestModifier {
    override fun describe(): String = "by region"

    override fun adjust(item: FallbackSwapManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FallbackSwapManifestUpdatedAtOffsetAdjuster(var weight: Double) : FallbackSwapManifestModifier {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: FallbackSwapManifest, value: Double): Double {
        return value + item.updatedAt.toDouble() * weight
    }
}

data class CapFallbackSwapManifestAdjuster(var cap: Double) : FallbackSwapManifestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackSwapManifest, value: Double): Double = minOf(value, cap)
}

/** Runs the fallback swap manifest steps in order and keeps a trace of each. */
data class FallbackSwapManifestEngine(
    var steps: MutableList<FallbackSwapManifestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FallbackSwapManifest): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackSwapManifestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackSwapManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFallbackSwapManifestEngine(): FallbackSwapManifestEngine {
    return FallbackSwapManifestEngine(
        steps = mutableListOf<FallbackSwapManifestModifier>(
            MinimumFallbackSwapManifestAdjuster(14.0),
            RegionFallbackSwapManifestAdjuster(
                mutableMapOf<String, Double>("east" to 1.06, "central" to 1.21, "retail" to 1.17),
            ),
            FallbackSwapManifestUpdatedAtOffsetAdjuster(0.36),
            CapFallbackSwapManifestAdjuster(12111.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 73424.4,
    )
}
