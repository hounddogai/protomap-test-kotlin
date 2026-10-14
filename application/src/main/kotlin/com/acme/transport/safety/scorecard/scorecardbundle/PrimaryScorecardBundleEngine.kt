package com.acme.transport.safety.scorecard.scorecardbundle

import com.acme.transport.kit.flags.Registry

/** One step of the primary scorecard bundle calculation. */
interface PrimaryScorecardBundleModifier {
    fun describe(): String
    fun adjust(item: PrimaryScorecardBundle, value: Double): Double
}

data class PrimaryScorecardBundlePalletsOffsetAdjuster(var weight: Double) : PrimaryScorecardBundleModifier {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: PrimaryScorecardBundle, value: Double): Double {
        return value + item.pallets.toDouble() * weight
    }
}

data class SegmentPrimaryScorecardBundleAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryScorecardBundleModifier {
    override fun describe(): String = "by segment"

    override fun adjust(item: PrimaryScorecardBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentPrimaryScorecardBundleAdjuster(var percent: Double) : PrimaryScorecardBundleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryScorecardBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedPrimaryScorecardBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryScorecardBundleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryScorecardBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the primary scorecard bundle steps in order and keeps a trace of each. */
data class PrimaryScorecardBundleEngine(
    var steps: MutableList<PrimaryScorecardBundleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PrimaryScorecardBundle): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryScorecardBundleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryScorecardBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPrimaryScorecardBundleEngine(flags: Registry): PrimaryScorecardBundleEngine {
    return PrimaryScorecardBundleEngine(
        steps = mutableListOf<PrimaryScorecardBundleModifier>(
            PrimaryScorecardBundlePalletsOffsetAdjuster(1.19),
            SegmentPrimaryScorecardBundleAdjuster(
                mutableMapOf<String, Double>("central" to 0.73, "west" to 1.09),
            ),
            PercentPrimaryScorecardBundleAdjuster(22.7),
            FlaggedPrimaryScorecardBundleAdjuster(flags, "primary-scorecard-bundle-strict", 0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 98355.0,
    )
}
