package com.acme.transport.network.hub.hubdraft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the manual hub rollup calculation. */
interface ManualHubRollupModifier {
    fun describe(): String
    fun adjust(item: ManualHubRollup, value: Double): Double
}

data class CapManualHubRollupAdjuster(var cap: Double) : ManualHubRollupModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualHubRollup, value: Double): Double = minOf(value, cap)
}

data class FlaggedManualHubRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualHubRollupModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualHubRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingManualHubRollupAdjuster(var step: Double) : ManualHubRollupModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualHubRollup, value: Double): Double = roundTo(value, step)
}

/** Runs the manual hub rollup steps in order and keeps a trace of each. */
data class ManualHubRollupEngine(
    var steps: MutableList<ManualHubRollupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ManualHubRollup): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualHubRollupModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ManualHubRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultManualHubRollupEngine(flags: Registry): ManualHubRollupEngine {
    return ManualHubRollupEngine(
        steps = mutableListOf<ManualHubRollupModifier>(
            CapManualHubRollupAdjuster(39308.0),
            FlaggedManualHubRollupAdjuster(flags, "manual-hub-rollup-primary", 5.7),
            RoundingManualHubRollupAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 92398.7,
    )
}
