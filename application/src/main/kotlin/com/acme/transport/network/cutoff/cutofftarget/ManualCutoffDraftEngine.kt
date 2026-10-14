package com.acme.transport.network.cutoff.cutofftarget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual cutoff draft calculation. */
interface ManualCutoffDraftStep {
    fun describe(): String
    fun adjust(item: ManualCutoffDraft, value: Double): Double
}

data class RoundingManualCutoffDraftAdjuster(var step: Double) : ManualCutoffDraftStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualCutoffDraft, value: Double): Double = roundTo(value, step)
}

data class CountryManualCutoffDraftAdjuster(var factors: MutableMap<String, Double>) : ManualCutoffDraftStep {
    override fun describe(): String = "by country"

    override fun adjust(item: ManualCutoffDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedManualCutoffDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualCutoffDraftStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualCutoffDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapManualCutoffDraftAdjuster(var cap: Double) : ManualCutoffDraftStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualCutoffDraft, value: Double): Double = minOf(value, cap)
}

data class TieredManualCutoffDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualCutoffDraftStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualCutoffDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the manual cutoff draft steps in order and keeps a trace of each. */
data class ManualCutoffDraftEngine(
    var steps: MutableList<ManualCutoffDraftStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualCutoffDraft): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualCutoffDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualCutoffDraftEngine(flags: Registry): ManualCutoffDraftEngine {
    return ManualCutoffDraftEngine(
        steps = mutableListOf<ManualCutoffDraftStep>(
            RoundingManualCutoffDraftAdjuster(0.05),
            CountryManualCutoffDraftAdjuster(mutableMapOf<String, Double>("south" to 1.13)),
            FlaggedManualCutoffDraftAdjuster(flags, "manual-cutoff-draft-manual", -7.1),
            CapManualCutoffDraftAdjuster(35304.0),
            TieredManualCutoffDraftAdjuster(
                mutableListOf<Double>(3586.0, 6558.0, 7052.0),
                mutableListOf<Double>(0.92, 0.81, 0.93),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 8307.7,
    )
}
