package com.acme.transport.network.hub.draft

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the hub draft calculation. */
interface HubDraftFactor {
    fun describe(): String
    fun adjust(item: HubDraft, value: Double): Double
}

data class CapHubDraftAdjuster(var cap: Double) : HubDraftFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: HubDraft, value: Double): Double = minOf(value, cap)
}

data class TieredHubDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : HubDraftFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: HubDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingHubDraftAdjuster(var step: Double) : HubDraftFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: HubDraft, value: Double): Double = roundTo(value, step)
}

data class MinimumHubDraftAdjuster(var minimum: Double) : HubDraftFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: HubDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameHubDraftAdjuster(var factors: MutableMap<String, Double>) : HubDraftFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: HubDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the hub draft steps in order and keeps a trace of each. */
data class HubDraftEngine(
    var steps: MutableList<HubDraftFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: HubDraft): Double {
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

    fun evaluateAll(items: MutableList<HubDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultHubDraftEngine(): HubDraftEngine {
    return HubDraftEngine(
        steps = mutableListOf<HubDraftFactor>(
            CapHubDraftAdjuster(9893.0),
            TieredHubDraftAdjuster(
                mutableListOf<Double>(1653.0, 4651.0, 4852.0),
                mutableListOf<Double>(1.17, 1.01, 1.09),
            ),
            RoundingHubDraftAdjuster(5.0),
            MinimumHubDraftAdjuster(5.2),
            NameHubDraftAdjuster(mutableMapOf<String, Double>("retail" to 0.83)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 3824.3,
    )
}
