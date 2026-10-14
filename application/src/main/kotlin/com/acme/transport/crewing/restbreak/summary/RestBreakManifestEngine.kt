package com.acme.transport.crewing.restbreak.summary

/** One step of the rest break manifest calculation. */
interface RestBreakManifestAdjuster {
    fun describe(): String
    fun adjust(item: RestBreakManifest, value: Double): Double
}

data class MinimumRestBreakManifestAdjuster(var minimum: Double) : RestBreakManifestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RestBreakManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapRestBreakManifestAdjuster(var cap: Double) : RestBreakManifestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RestBreakManifest, value: Double): Double = minOf(value, cap)
}

data class PercentRestBreakManifestAdjuster(var percent: Double) : RestBreakManifestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RestBreakManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RestBreakManifestExpiresAtOffsetAdjuster(var weight: Double) : RestBreakManifestAdjuster {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: RestBreakManifest, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

/** Runs the rest break manifest steps in order and keeps a trace of each. */
data class RestBreakManifestEngine(
    var steps: MutableList<RestBreakManifestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RestBreakManifest): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RestBreakManifestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RestBreakManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRestBreakManifestEngine(): RestBreakManifestEngine {
    return RestBreakManifestEngine(
        steps = mutableListOf<RestBreakManifestAdjuster>(
            MinimumRestBreakManifestAdjuster(31.3),
            CapRestBreakManifestAdjuster(36681.0),
            PercentRestBreakManifestAdjuster(7.3),
            RestBreakManifestExpiresAtOffsetAdjuster(1.59),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 80407.9,
    )
}
