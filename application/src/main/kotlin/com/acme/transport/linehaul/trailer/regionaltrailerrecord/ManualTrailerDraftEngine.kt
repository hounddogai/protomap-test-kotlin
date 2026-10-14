package com.acme.transport.linehaul.trailer.regionaltrailerrecord

import com.acme.transport.kit.flags.Registry

/** One step of the manual trailer draft calculation. */
interface ManualTrailerDraftModifier {
    fun describe(): String
    fun adjust(item: ManualTrailerDraft, value: Double): Double
}

data class FlaggedManualTrailerDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualTrailerDraftModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualTrailerDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumManualTrailerDraftAdjuster(var minimum: Double) : ManualTrailerDraftModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualTrailerDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredManualTrailerDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualTrailerDraftModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualTrailerDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the manual trailer draft steps in order and keeps a trace of each. */
data class ManualTrailerDraftEngine(
    var steps: MutableList<ManualTrailerDraftModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualTrailerDraft): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualTrailerDraftModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ManualTrailerDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualTrailerDraftEngine(flags: Registry): ManualTrailerDraftEngine {
    return ManualTrailerDraftEngine(
        steps = mutableListOf<ManualTrailerDraftModifier>(
            FlaggedManualTrailerDraftAdjuster(flags, "manual-trailer-draft-strict", -5.4),
            MinimumManualTrailerDraftAdjuster(15.7),
            TieredManualTrailerDraftAdjuster(
                mutableListOf<Double>(1138.0, 2833.0, 7176.0, 7669.0),
                mutableListOf<Double>(0.92, 0.84, 1.09, 0.83),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 45060.4,
    )
}
