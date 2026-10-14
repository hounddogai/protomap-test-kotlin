package com.acme.transport.fleetops.defect.budget

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled defect threshold calculation. */
interface ScheduledDefectThresholdModifier {
    fun describe(): String
    fun adjust(item: ScheduledDefectThreshold, value: Double): Double
}

data class ChannelScheduledDefectThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledDefectThresholdModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: ScheduledDefectThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumScheduledDefectThresholdAdjuster(var minimum: Double) : ScheduledDefectThresholdModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledDefectThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentScheduledDefectThresholdAdjuster(var percent: Double) : ScheduledDefectThresholdModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledDefectThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the scheduled defect threshold steps in order and keeps a trace of each. */
data class ScheduledDefectThresholdEngine(
    var steps: MutableList<ScheduledDefectThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledDefectThreshold): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledDefectThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledDefectThresholdEngine(): ScheduledDefectThresholdEngine {
    return ScheduledDefectThresholdEngine(
        steps = mutableListOf<ScheduledDefectThresholdModifier>(
            ChannelScheduledDefectThresholdAdjuster(mutableMapOf<String, Double>("central" to 0.98)),
            MinimumScheduledDefectThresholdAdjuster(34.6),
            PercentScheduledDefectThresholdAdjuster(9.2),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 45930.6,
    )
}
