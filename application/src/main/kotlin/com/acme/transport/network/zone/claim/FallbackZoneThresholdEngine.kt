package com.acme.transport.network.zone.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback zone threshold calculation. */
interface FallbackZoneThresholdModifier {
    fun describe(): String
    fun adjust(item: FallbackZoneThreshold, value: Double): Double
}

data class RoundingFallbackZoneThresholdAdjuster(var step: Double) : FallbackZoneThresholdModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackZoneThreshold, value: Double): Double = roundTo(value, step)
}

data class FlaggedFallbackZoneThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackZoneThresholdModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackZoneThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class FallbackZoneThresholdRatioOffsetAdjuster(var weight: Double) : FallbackZoneThresholdModifier {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: FallbackZoneThreshold, value: Double): Double = value + item.ratio * weight
}

data class CapFallbackZoneThresholdAdjuster(var cap: Double) : FallbackZoneThresholdModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackZoneThreshold, value: Double): Double = minOf(value, cap)
}

data class UnitFallbackZoneThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackZoneThresholdModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: FallbackZoneThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the fallback zone threshold steps in order and keeps a trace of each. */
data class FallbackZoneThresholdEngine(
    var steps: MutableList<FallbackZoneThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackZoneThreshold): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackZoneThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackZoneThresholdEngine(flags: Registry): FallbackZoneThresholdEngine {
    return FallbackZoneThresholdEngine(
        steps = mutableListOf<FallbackZoneThresholdModifier>(
            RoundingFallbackZoneThresholdAdjuster(0.25),
            FlaggedFallbackZoneThresholdAdjuster(flags, "fallback-zone-threshold-weighted", 7.7),
            FallbackZoneThresholdRatioOffsetAdjuster(0.83),
            CapFallbackZoneThresholdAdjuster(2010.0),
            UnitFallbackZoneThresholdAdjuster(mutableMapOf<String, Double>("south" to 0.78, "west" to 1.01)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 36920.3,
    )
}
