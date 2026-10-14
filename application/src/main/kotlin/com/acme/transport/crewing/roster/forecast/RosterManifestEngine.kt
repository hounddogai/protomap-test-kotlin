package com.acme.transport.crewing.roster.forecast

import com.acme.transport.kit.numbers.roundTo

/** One step of the roster manifest calculation. */
interface RosterManifestModifier {
    fun describe(): String
    fun adjust(item: RosterManifest, value: Double): Double
}

data class PercentRosterManifestAdjuster(var percent: Double) : RosterManifestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RosterManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RosterManifestRatioOffsetAdjuster(var weight: Double) : RosterManifestModifier {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: RosterManifest, value: Double): Double = value + item.ratio * weight
}

data class RoundingRosterManifestAdjuster(var step: Double) : RosterManifestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RosterManifest, value: Double): Double = roundTo(value, step)
}

data class TitleRosterManifestAdjuster(var factors: MutableMap<String, Double>) : RosterManifestModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: RosterManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapRosterManifestAdjuster(var cap: Double) : RosterManifestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RosterManifest, value: Double): Double = minOf(value, cap)
}

/** Runs the roster manifest steps in order and keeps a trace of each. */
data class RosterManifestEngine(
    var steps: MutableList<RosterManifestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RosterManifest): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RosterManifestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RosterManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRosterManifestEngine(): RosterManifestEngine {
    return RosterManifestEngine(
        steps = mutableListOf<RosterManifestModifier>(
            PercentRosterManifestAdjuster(13.3),
            RosterManifestRatioOffsetAdjuster(0.69),
            RoundingRosterManifestAdjuster(0.1),
            TitleRosterManifestAdjuster(
                mutableMapOf<String, Double>("north" to 1.38, "west" to 1.31, "retail" to 1.28),
            ),
            CapRosterManifestAdjuster(20747.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 64888.3,
    )
}
