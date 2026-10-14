package com.acme.transport.network.hub.primaryhubsummary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global hub summary calculation. */
interface GlobalHubSummaryFactor {
    fun describe(): String
    fun adjust(item: GlobalHubSummary, value: Double): Double
}

data class PercentGlobalHubSummaryAdjuster(var percent: Double) : GlobalHubSummaryFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalHubSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedGlobalHubSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalHubSummaryFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalHubSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapGlobalHubSummaryAdjuster(var cap: Double) : GlobalHubSummaryFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalHubSummary, value: Double): Double = minOf(value, cap)
}

data class RoundingGlobalHubSummaryAdjuster(var step: Double) : GlobalHubSummaryFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalHubSummary, value: Double): Double = roundTo(value, step)
}

data class GlobalHubSummaryThresholdOffsetAdjuster(var weight: Double) : GlobalHubSummaryFactor {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: GlobalHubSummary, value: Double): Double = value + item.threshold * weight
}

/** Runs the global hub summary steps in order and keeps a trace of each. */
data class GlobalHubSummaryEngine(
    var steps: MutableList<GlobalHubSummaryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalHubSummary): Double {
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

    fun evaluateAll(items: MutableList<GlobalHubSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalHubSummaryEngine(flags: Registry): GlobalHubSummaryEngine {
    return GlobalHubSummaryEngine(
        steps = mutableListOf<GlobalHubSummaryFactor>(
            PercentGlobalHubSummaryAdjuster(-20.8),
            FlaggedGlobalHubSummaryAdjuster(flags, "global-hub-summary-weighted", 2.8),
            CapGlobalHubSummaryAdjuster(3387.0),
            RoundingGlobalHubSummaryAdjuster(0.25),
            GlobalHubSummaryThresholdOffsetAdjuster(0.94),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 34532.3,
    )
}
