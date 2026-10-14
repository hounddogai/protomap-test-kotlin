package com.acme.transport.lastmile.drop.notice

import com.acme.transport.kit.flags.Registry

/** One step of the manual drop bundle calculation. */
interface ManualDropBundleStep {
    fun describe(): String
    fun adjust(item: ManualDropBundle, value: Double): Double
}

data class FlaggedManualDropBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualDropBundleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualDropBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumManualDropBundleAdjuster(var minimum: Double) : ManualDropBundleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualDropBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapManualDropBundleAdjuster(var cap: Double) : ManualDropBundleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualDropBundle, value: Double): Double = minOf(value, cap)
}

data class NameManualDropBundleAdjuster(var factors: MutableMap<String, Double>) : ManualDropBundleStep {
    override fun describe(): String = "by name"

    override fun adjust(item: ManualDropBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentManualDropBundleAdjuster(var percent: Double) : ManualDropBundleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualDropBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the manual drop bundle steps in order and keeps a trace of each. */
data class ManualDropBundleEngine(
    var steps: MutableList<ManualDropBundleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualDropBundle): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualDropBundleStep = steps[index]
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

    fun evaluateAll(items: MutableList<ManualDropBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualDropBundleEngine(flags: Registry): ManualDropBundleEngine {
    return ManualDropBundleEngine(
        steps = mutableListOf<ManualDropBundleStep>(
            FlaggedManualDropBundleAdjuster(flags, "manual-drop-bundle-seasonal", -5.1),
            MinimumManualDropBundleAdjuster(36.0),
            CapManualDropBundleAdjuster(37463.0),
            NameManualDropBundleAdjuster(
                mutableMapOf<String, Double>("east" to 1.03, "north" to 1.31, "online" to 1.17),
            ),
            PercentManualDropBundleAdjuster(-6.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 57732.7,
    )
}
