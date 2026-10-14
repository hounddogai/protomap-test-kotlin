package com.acme.transport.crewing.roster.rosterrule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the capped roster manifest calculation. */
interface CappedRosterManifestAdjuster {
    fun describe(): String
    fun adjust(item: CappedRosterManifest, value: Double): Double
}

data class MinimumCappedRosterManifestAdjuster(var minimum: Double) : CappedRosterManifestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedRosterManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CategoryCappedRosterManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedRosterManifestAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: CappedRosterManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingCappedRosterManifestAdjuster(var step: Double) : CappedRosterManifestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedRosterManifest, value: Double): Double = roundTo(value, step)
}

data class PercentCappedRosterManifestAdjuster(var percent: Double) : CappedRosterManifestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedRosterManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedCappedRosterManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedRosterManifestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedRosterManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapCappedRosterManifestAdjuster(var cap: Double) : CappedRosterManifestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedRosterManifest, value: Double): Double = minOf(value, cap)
}

/** Runs the capped roster manifest steps in order and keeps a trace of each. */
data class CappedRosterManifestEngine(
    var steps: MutableList<CappedRosterManifestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedRosterManifest): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedRosterManifestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CappedRosterManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedRosterManifestEngine(flags: Registry): CappedRosterManifestEngine {
    return CappedRosterManifestEngine(
        steps = mutableListOf<CappedRosterManifestAdjuster>(
            MinimumCappedRosterManifestAdjuster(4.8),
            CategoryCappedRosterManifestAdjuster(
                mutableMapOf<String, Double>("online" to 0.89, "south" to 1.06, "central" to 1.38),
            ),
            RoundingCappedRosterManifestAdjuster(0.25),
            PercentCappedRosterManifestAdjuster(4.3),
            FlaggedCappedRosterManifestAdjuster(flags, "capped-roster-manifest-batched", 7.8),
            CapCappedRosterManifestAdjuster(13163.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 72586.8,
    )
}
