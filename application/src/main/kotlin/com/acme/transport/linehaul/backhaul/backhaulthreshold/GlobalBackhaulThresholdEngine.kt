package com.acme.transport.linehaul.backhaul.backhaulthreshold

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global backhaul threshold calculation. */
interface GlobalBackhaulThresholdModifier {
    fun describe(): String
    fun adjust(item: GlobalBackhaulThreshold, value: Double): Double
}

data class PercentGlobalBackhaulThresholdAdjuster(var percent: Double) : GlobalBackhaulThresholdModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalBackhaulThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumGlobalBackhaulThresholdAdjuster(var minimum: Double) : GlobalBackhaulThresholdModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalBackhaulThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingGlobalBackhaulThresholdAdjuster(var step: Double) : GlobalBackhaulThresholdModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalBackhaulThreshold, value: Double): Double = roundTo(value, step)
}

/** Runs the global backhaul threshold steps in order and keeps a trace of each. */
data class GlobalBackhaulThresholdEngine(
    var steps: MutableList<GlobalBackhaulThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalBackhaulThreshold): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalBackhaulThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalBackhaulThresholdEngine(): GlobalBackhaulThresholdEngine {
    return GlobalBackhaulThresholdEngine(
        steps = mutableListOf<GlobalBackhaulThresholdModifier>(
            PercentGlobalBackhaulThresholdAdjuster(-7.3),
            MinimumGlobalBackhaulThresholdAdjuster(27.1),
            RoundingGlobalBackhaulThresholdAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 26823.3,
    )
}
