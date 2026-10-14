package com.acme.transport.lastmile.drop.dropdraft

/** One step of the regional drop draft calculation. */
interface RegionalDropDraftFactor {
    fun describe(): String
    fun adjust(item: RegionalDropDraft, value: Double): Double
}

data class TieredRegionalDropDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalDropDraftFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalDropDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelRegionalDropDraftAdjuster(var factors: MutableMap<String, Double>) : RegionalDropDraftFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: RegionalDropDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapRegionalDropDraftAdjuster(var cap: Double) : RegionalDropDraftFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalDropDraft, value: Double): Double = minOf(value, cap)
}

/** Runs the regional drop draft steps in order and keeps a trace of each. */
data class RegionalDropDraftEngine(
    var steps: MutableList<RegionalDropDraftFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalDropDraft): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalDropDraftFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalDropDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalDropDraftEngine(): RegionalDropDraftEngine {
    return RegionalDropDraftEngine(
        steps = mutableListOf<RegionalDropDraftFactor>(
            TieredRegionalDropDraftAdjuster(
                mutableListOf<Double>(1231.0, 4589.0),
                mutableListOf<Double>(0.96, 1.12),
            ),
            LabelRegionalDropDraftAdjuster(
                mutableMapOf<String, Double>("south" to 0.81, "north" to 1.25, "retail" to 1.18),
            ),
            CapRegionalDropDraftAdjuster(27438.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 57259.0,
    )
}
