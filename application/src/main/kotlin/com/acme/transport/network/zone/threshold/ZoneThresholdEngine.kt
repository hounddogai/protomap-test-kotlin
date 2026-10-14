package com.acme.transport.network.zone.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the zone threshold calculation. */
interface ZoneThresholdStep {
    fun describe(): String
    fun adjust(item: ZoneThreshold, value: Double): Double
}

data class PercentZoneThresholdAdjuster(var percent: Double) : ZoneThresholdStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ZoneThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapZoneThresholdAdjuster(var cap: Double) : ZoneThresholdStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ZoneThreshold, value: Double): Double = minOf(value, cap)
}

data class LabelZoneThresholdAdjuster(var factors: MutableMap<String, Double>) : ZoneThresholdStep {
    override fun describe(): String = "by label"

    override fun adjust(item: ZoneThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedZoneThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ZoneThresholdStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ZoneThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the zone threshold steps in order and keeps a trace of each. */
data class ZoneThresholdEngine(
    var steps: MutableList<ZoneThresholdStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ZoneThreshold): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ZoneThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultZoneThresholdEngine(flags: Registry): ZoneThresholdEngine {
    return ZoneThresholdEngine(
        steps = mutableListOf<ZoneThresholdStep>(
            PercentZoneThresholdAdjuster(23.1),
            CapZoneThresholdAdjuster(17233.0),
            LabelZoneThresholdAdjuster(mutableMapOf<String, Double>("north" to 1.07, "east" to 0.9)),
            FlaggedZoneThresholdAdjuster(flags, "zone-threshold-rolling", 3.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 64276.2,
    )
}
