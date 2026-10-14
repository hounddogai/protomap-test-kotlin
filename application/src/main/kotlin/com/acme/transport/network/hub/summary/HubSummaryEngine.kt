package com.acme.transport.network.hub.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the hub summary calculation. */
interface HubSummaryFactor {
    fun describe(): String
    fun adjust(item: HubSummary, value: Double): Double
}

data class LabelHubSummaryAdjuster(var factors: MutableMap<String, Double>) : HubSummaryFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: HubSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedHubSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : HubSummaryFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: HubSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingHubSummaryAdjuster(var step: Double) : HubSummaryFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: HubSummary, value: Double): Double = roundTo(value, step)
}

data class CapHubSummaryAdjuster(var cap: Double) : HubSummaryFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: HubSummary, value: Double): Double = minOf(value, cap)
}

/** Runs the hub summary steps in order and keeps a trace of each. */
data class HubSummaryEngine(
    var steps: MutableList<HubSummaryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: HubSummary): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<HubSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultHubSummaryEngine(flags: Registry): HubSummaryEngine {
    return HubSummaryEngine(
        steps = mutableListOf<HubSummaryFactor>(
            LabelHubSummaryAdjuster(
                mutableMapOf<String, Double>("online" to 1.14, "west" to 0.88, "central" to 0.75),
            ),
            FlaggedHubSummaryAdjuster(flags, "hub-summary-batched", 3.1),
            RoundingHubSummaryAdjuster(0.1),
            CapHubSummaryAdjuster(11957.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 79256.5,
    )
}
