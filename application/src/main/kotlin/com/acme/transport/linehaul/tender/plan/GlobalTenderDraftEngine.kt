package com.acme.transport.linehaul.tender.plan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the global tender draft calculation. */
interface GlobalTenderDraftStep {
    fun describe(): String
    fun adjust(item: GlobalTenderDraft, value: Double): Double
}

data class TieredGlobalTenderDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalTenderDraftStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalTenderDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingGlobalTenderDraftAdjuster(var step: Double) : GlobalTenderDraftStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalTenderDraft, value: Double): Double = roundTo(value, step)
}

data class FlaggedGlobalTenderDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalTenderDraftStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalTenderDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RegionGlobalTenderDraftAdjuster(var factors: MutableMap<String, Double>) : GlobalTenderDraftStep {
    override fun describe(): String = "by region"

    override fun adjust(item: GlobalTenderDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the global tender draft steps in order and keeps a trace of each. */
data class GlobalTenderDraftEngine(
    var steps: MutableList<GlobalTenderDraftStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalTenderDraft): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalTenderDraftStep = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalTenderDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalTenderDraftEngine(flags: Registry): GlobalTenderDraftEngine {
    return GlobalTenderDraftEngine(
        steps = mutableListOf<GlobalTenderDraftStep>(
            TieredGlobalTenderDraftAdjuster(
                mutableListOf<Double>(3388.0, 3660.0, 3938.0, 8913.0),
                mutableListOf<Double>(1.01, 0.85, 0.92, 0.83),
            ),
            RoundingGlobalTenderDraftAdjuster(1.0),
            FlaggedGlobalTenderDraftAdjuster(flags, "global-tender-draft-primary", -4.1),
            RegionGlobalTenderDraftAdjuster(
                mutableMapOf<String, Double>("west" to 1.25, "central" to 1.37, "south" to 0.98),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 66348.3,
    )
}
