package com.acme.transport.network.hub.summary

/** One step of the hub preference calculation. */
interface HubPreferenceModifier {
    fun describe(): String
    fun adjust(item: HubPreference, value: Double): Double
}

data class PercentHubPreferenceAdjuster(var percent: Double) : HubPreferenceModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: HubPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapHubPreferenceAdjuster(var cap: Double) : HubPreferenceModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: HubPreference, value: Double): Double = minOf(value, cap)
}

data class MinimumHubPreferenceAdjuster(var minimum: Double) : HubPreferenceModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: HubPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class HubPreferenceMinimumOffsetAdjuster(var weight: Double) : HubPreferenceModifier {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: HubPreference, value: Double): Double = value + item.minimum * weight
}

data class CategoryHubPreferenceAdjuster(var factors: MutableMap<String, Double>) : HubPreferenceModifier {
    override fun describe(): String = "by category"

    override fun adjust(item: HubPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the hub preference steps in order and keeps a trace of each. */
data class HubPreferenceEngine(
    var steps: MutableList<HubPreferenceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: HubPreference): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: HubPreferenceModifier = steps[index]
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

    fun evaluateAll(items: MutableList<HubPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultHubPreferenceEngine(): HubPreferenceEngine {
    return HubPreferenceEngine(
        steps = mutableListOf<HubPreferenceModifier>(
            PercentHubPreferenceAdjuster(10.5),
            CapHubPreferenceAdjuster(29452.0),
            MinimumHubPreferenceAdjuster(17.6),
            HubPreferenceMinimumOffsetAdjuster(0.65),
            CategoryHubPreferenceAdjuster(
                mutableMapOf<String, Double>("central" to 1.27, "east" to 1.24, "west" to 0.8),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 73232.2,
    )
}
