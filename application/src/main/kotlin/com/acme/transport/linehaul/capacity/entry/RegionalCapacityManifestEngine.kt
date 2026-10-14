package com.acme.transport.linehaul.capacity.entry

/** One step of the regional capacity manifest calculation. */
interface RegionalCapacityManifestFactor {
    fun describe(): String
    fun adjust(item: RegionalCapacityManifest, value: Double): Double
}

data class SourceRegionalCapacityManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalCapacityManifestFactor {
    override fun describe(): String = "by source"

    override fun adjust(item: RegionalCapacityManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredRegionalCapacityManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalCapacityManifestFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalCapacityManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumRegionalCapacityManifestAdjuster(var minimum: Double) : RegionalCapacityManifestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalCapacityManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the regional capacity manifest steps in order and keeps a trace of each. */
data class RegionalCapacityManifestEngine(
    var steps: MutableList<RegionalCapacityManifestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalCapacityManifest): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalCapacityManifestFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalCapacityManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalCapacityManifestEngine(): RegionalCapacityManifestEngine {
    return RegionalCapacityManifestEngine(
        steps = mutableListOf<RegionalCapacityManifestFactor>(
            SourceRegionalCapacityManifestAdjuster(mutableMapOf<String, Double>("central" to 0.73)),
            TieredRegionalCapacityManifestAdjuster(
                mutableListOf<Double>(628.0, 1674.0, 2360.0),
                mutableListOf<Double>(0.97, 0.85, 0.85),
            ),
            MinimumRegionalCapacityManifestAdjuster(5.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 44131.3,
    )
}
