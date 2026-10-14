package com.acme.transport.terminal.crossdock.summary

/** One step of the legacy cross dock summary calculation. */
interface LegacyCrossDockSummaryAdjuster {
    fun describe(): String
    fun adjust(item: LegacyCrossDockSummary, value: Double): Double
}

data class LaneLegacyCrossDockSummaryAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyCrossDockSummaryAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: LegacyCrossDockSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class LegacyCrossDockSummaryRetriesOffsetAdjuster(var weight: Double) : LegacyCrossDockSummaryAdjuster {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: LegacyCrossDockSummary, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

data class CapLegacyCrossDockSummaryAdjuster(var cap: Double) : LegacyCrossDockSummaryAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyCrossDockSummary, value: Double): Double = minOf(value, cap)
}

/** Runs the legacy cross dock summary steps in order and keeps a trace of each. */
data class LegacyCrossDockSummaryEngine(
    var steps: MutableList<LegacyCrossDockSummaryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LegacyCrossDockSummary): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyCrossDockSummaryAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyCrossDockSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLegacyCrossDockSummaryEngine(): LegacyCrossDockSummaryEngine {
    return LegacyCrossDockSummaryEngine(
        steps = mutableListOf<LegacyCrossDockSummaryAdjuster>(
            LaneLegacyCrossDockSummaryAdjuster(mutableMapOf<String, Double>("retail" to 0.7, "online" to 0.98)),
            LegacyCrossDockSummaryRetriesOffsetAdjuster(1.97),
            CapLegacyCrossDockSummaryAdjuster(11682.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 34872.3,
    )
}
