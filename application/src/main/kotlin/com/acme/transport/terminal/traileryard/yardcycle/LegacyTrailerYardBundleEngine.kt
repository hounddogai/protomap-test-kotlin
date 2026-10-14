package com.acme.transport.terminal.traileryard.yardcycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy trailer yard bundle calculation. */
interface LegacyTrailerYardBundleStep {
    fun describe(): String
    fun adjust(item: LegacyTrailerYardBundle, value: Double): Double
}

data class RoundingLegacyTrailerYardBundleAdjuster(var step: Double) : LegacyTrailerYardBundleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyTrailerYardBundle, value: Double): Double = roundTo(value, step)
}

data class FlaggedLegacyTrailerYardBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyTrailerYardBundleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyTrailerYardBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentLegacyTrailerYardBundleAdjuster(var percent: Double) : LegacyTrailerYardBundleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyTrailerYardBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapLegacyTrailerYardBundleAdjuster(var cap: Double) : LegacyTrailerYardBundleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyTrailerYardBundle, value: Double): Double = minOf(value, cap)
}

/** Runs the legacy trailer yard bundle steps in order and keeps a trace of each. */
data class LegacyTrailerYardBundleEngine(
    var steps: MutableList<LegacyTrailerYardBundleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyTrailerYardBundle): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyTrailerYardBundleStep = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyTrailerYardBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyTrailerYardBundleEngine(flags: Registry): LegacyTrailerYardBundleEngine {
    return LegacyTrailerYardBundleEngine(
        steps = mutableListOf<LegacyTrailerYardBundleStep>(
            RoundingLegacyTrailerYardBundleAdjuster(0.1),
            FlaggedLegacyTrailerYardBundleAdjuster(flags, "legacy-trailer-yard-bundle-global", 9.1),
            PercentLegacyTrailerYardBundleAdjuster(-7.0),
            CapLegacyTrailerYardBundleAdjuster(22544.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 62929.2,
    )
}
