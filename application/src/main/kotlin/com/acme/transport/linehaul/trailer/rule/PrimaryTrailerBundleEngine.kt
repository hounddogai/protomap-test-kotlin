package com.acme.transport.linehaul.trailer.rule

import com.acme.transport.kit.flags.Registry

/** One step of the primary trailer bundle calculation. */
interface PrimaryTrailerBundleAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryTrailerBundle, value: Double): Double
}

data class FlaggedPrimaryTrailerBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryTrailerBundleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryTrailerBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LocalePrimaryTrailerBundleAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryTrailerBundleAdjuster {
    override fun describe(): String = "by locale"

    override fun adjust(item: PrimaryTrailerBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentPrimaryTrailerBundleAdjuster(var percent: Double) : PrimaryTrailerBundleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryTrailerBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the primary trailer bundle steps in order and keeps a trace of each. */
data class PrimaryTrailerBundleEngine(
    var steps: MutableList<PrimaryTrailerBundleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PrimaryTrailerBundle): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryTrailerBundleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryTrailerBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPrimaryTrailerBundleEngine(flags: Registry): PrimaryTrailerBundleEngine {
    return PrimaryTrailerBundleEngine(
        steps = mutableListOf<PrimaryTrailerBundleAdjuster>(
            FlaggedPrimaryTrailerBundleAdjuster(flags, "primary-trailer-bundle-legacy", -4.1),
            LocalePrimaryTrailerBundleAdjuster(mutableMapOf<String, Double>("retail" to 1.1)),
            PercentPrimaryTrailerBundleAdjuster(9.3),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 5249.0,
    )
}
