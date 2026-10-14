package com.acme.transport.safety.scorecard.policy

import com.acme.transport.kit.numbers.roundTo

/** One step of the global scorecard manifest calculation. */
interface GlobalScorecardManifestAdjuster {
    fun describe(): String
    fun adjust(item: GlobalScorecardManifest, value: Double): Double
}

data class TieredGlobalScorecardManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalScorecardManifestAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalScorecardManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ChannelGlobalScorecardManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalScorecardManifestAdjuster {
    override fun describe(): String = "by channel"

    override fun adjust(item: GlobalScorecardManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingGlobalScorecardManifestAdjuster(var step: Double) : GlobalScorecardManifestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalScorecardManifest, value: Double): Double = roundTo(value, step)
}

/** Runs the global scorecard manifest steps in order and keeps a trace of each. */
data class GlobalScorecardManifestEngine(
    var steps: MutableList<GlobalScorecardManifestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalScorecardManifest): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalScorecardManifestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalScorecardManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalScorecardManifestEngine(): GlobalScorecardManifestEngine {
    return GlobalScorecardManifestEngine(
        steps = mutableListOf<GlobalScorecardManifestAdjuster>(
            TieredGlobalScorecardManifestAdjuster(
                mutableListOf<Double>(5018.0, 7265.0, 7981.0, 9730.0),
                mutableListOf<Double>(1.18, 0.81, 0.85, 1.11),
            ),
            ChannelGlobalScorecardManifestAdjuster(mutableMapOf<String, Double>("east" to 1.07)),
            RoundingGlobalScorecardManifestAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 66354.8,
    )
}
