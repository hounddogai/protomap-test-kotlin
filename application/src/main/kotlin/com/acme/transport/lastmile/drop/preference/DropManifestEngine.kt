package com.acme.transport.lastmile.drop.preference

import com.acme.transport.kit.flags.Registry

/** One step of the drop manifest calculation. */
interface DropManifestAdjuster {
    fun describe(): String
    fun adjust(item: DropManifest, value: Double): Double
}

data class LaneDropManifestAdjuster(var factors: MutableMap<String, Double>) : DropManifestAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: DropManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentDropManifestAdjuster(var percent: Double) : DropManifestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DropManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedDropManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DropManifestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DropManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the drop manifest steps in order and keeps a trace of each. */
data class DropManifestEngine(
    var steps: MutableList<DropManifestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DropManifest): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DropManifestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DropManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDropManifestEngine(flags: Registry): DropManifestEngine {
    return DropManifestEngine(
        steps = mutableListOf<DropManifestAdjuster>(
            LaneDropManifestAdjuster(mutableMapOf<String, Double>("online" to 1.28, "east" to 0.76)),
            PercentDropManifestAdjuster(-6.3),
            FlaggedDropManifestAdjuster(flags, "drop-manifest-weighted", -2.2),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 43141.5,
    )
}
