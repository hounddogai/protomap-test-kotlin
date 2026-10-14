package com.acme.transport.network.hub.snapshot

import com.acme.transport.kit.flags.Registry

/** One step of the weighted hub draft calculation. */
interface WeightedHubDraftFactor {
    fun describe(): String
    fun adjust(item: WeightedHubDraft, value: Double): Double
}

data class LabelWeightedHubDraftAdjuster(var factors: MutableMap<String, Double>) : WeightedHubDraftFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: WeightedHubDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredWeightedHubDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedHubDraftFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedHubDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class WeightedHubDraftVersionOffsetAdjuster(var weight: Double) : WeightedHubDraftFactor {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: WeightedHubDraft, value: Double): Double = value + item.version.toDouble() * weight
}

data class CapWeightedHubDraftAdjuster(var cap: Double) : WeightedHubDraftFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedHubDraft, value: Double): Double = minOf(value, cap)
}

data class FlaggedWeightedHubDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedHubDraftFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedHubDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentWeightedHubDraftAdjuster(var percent: Double) : WeightedHubDraftFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedHubDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the weighted hub draft steps in order and keeps a trace of each. */
data class WeightedHubDraftEngine(
    var steps: MutableList<WeightedHubDraftFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedHubDraft): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedHubDraftFactor = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedHubDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedHubDraftEngine(flags: Registry): WeightedHubDraftEngine {
    return WeightedHubDraftEngine(
        steps = mutableListOf<WeightedHubDraftFactor>(
            LabelWeightedHubDraftAdjuster(mutableMapOf<String, Double>("retail" to 1.31, "south" to 1.04)),
            TieredWeightedHubDraftAdjuster(
                mutableListOf<Double>(82.0, 95.0, 242.0, 7771.0),
                mutableListOf<Double>(0.97, 1.02, 1.06, 0.93),
            ),
            WeightedHubDraftVersionOffsetAdjuster(0.33),
            CapWeightedHubDraftAdjuster(33077.0),
            FlaggedWeightedHubDraftAdjuster(flags, "weighted-hub-draft-seasonal", 4.6),
            PercentWeightedHubDraftAdjuster(-19.6),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 30819.2,
    )
}
